package com.jadic.biz;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.biz.bean.TerminalBean;
import com.jadic.cmd.req.AbstractCmdReq;
import com.jadic.cmd.req.CmdChargeDetailReq;
import com.jadic.cmd.req.CmdCheckCityCardTypeReq;
import com.jadic.cmd.req.CmdDefaultReq;
import com.jadic.cmd.req.CmdGetMac2Req;
import com.jadic.cmd.req.CmdHeartbeatReq;
import com.jadic.cmd.req.CmdLoginReq;
import com.jadic.cmd.req.CmdModifyZHBPassReq;
import com.jadic.cmd.req.CmdModuleStatusReq;
import com.jadic.cmd.req.CmdPrepaidCardCheckReq;
import com.jadic.cmd.req.CmdQueryZHBBalanceReq;
import com.jadic.cmd.req.CmdRefundReq;
import com.jadic.cmd.req.CmdTYRetReq;
import com.jadic.cmd.rsp.CmdChargeDetailRsp;
import com.jadic.cmd.rsp.CmdCheckCityCardTypeRsp;
import com.jadic.cmd.rsp.CmdGetMac2Rsp;
import com.jadic.cmd.rsp.CmdLoginRsp;
import com.jadic.cmd.rsp.CmdModifyZHBPassRsp;
import com.jadic.cmd.rsp.CmdPrepaidCardCheckRsp;
import com.jadic.cmd.rsp.CmdQueryZHBBalanceRsp;
import com.jadic.cmd.rsp.CmdRefundRsp;
import com.jadic.cmd.rsp.CmdTYRetRsp;
import com.jadic.db.DBOper;
import com.jadic.tcp.server.TcpChannel;
import com.jadic.utils.Const;
import com.jadic.utils.KKTool;
import com.jadic.ws.WSUtil;

/**
 * @author Jadic
 * @created 2014-4-14
 */
public class ThreadDisposeTcpChannelData implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ThreadDisposeTcpChannelData.class);
    private TcpChannel tcpChannel;
    private ICmdBizDisposer cmdBizDisposer;

    final static int MAX_DISPOSE_COUNT = 20;// 线程处理每个通道一次最多连续处理次数

    public ThreadDisposeTcpChannelData(TcpChannel tcpChannel, ICmdBizDisposer cmdBizDisposer) {
        this.tcpChannel = tcpChannel;
        this.cmdBizDisposer = cmdBizDisposer;
    }

    @Override
    public void run() {
        boolean isSucc = tcpChannel.checkAndSetDisposeFlag();
        if (isSucc) {
            ChannelBuffer buffer = null;
            int disposeCount = 0;
            while ((buffer = tcpChannel.getNextBuffer()) != null) {
                disposeBuffer(buffer);
                disposeCount++;

                // if tcp channel is closed, dispose all buffer data in the
                // queue
                if (!tcpChannel.isClosed()) {
                    if (disposeCount >= MAX_DISPOSE_COUNT) {
                        log.warn("{} dispose buffer over {} times, left size:{}", tcpChannel, MAX_DISPOSE_COUNT,
                                tcpChannel.getBufferQueueSize());
                        break;
                    }
                }
            }
            if (!tcpChannel.isClosed()) {
                tcpChannel.setDisposing(false);
            } else {
                tcpChannel = null;
            }
            /*
             * if (disposeCount > 1) { KKLog.info(tcpChannel +
             * " dispose buffer " + disposeCount + " times"); }
             */
        }
    }

    private void disposeBuffer(ChannelBuffer buffer) {
        if (buffer == null || buffer.readableBytes() < Const.CMD_MIN_SIZE) {
            return;
        }

        short cmdFlag = buffer.getShort(buffer.readerIndex() + 1);

        // can't deal other cmd, unless terminal is logined
        if (!tcpChannel.isLogined() && cmdFlag != Const.TER_LOGIN) {
            dealInvalidCmd(buffer, Const.TY_RET_NO_LOGIN);
            return;
        }

        switch (cmdFlag) {
        case Const.TER_TY_RET:
            dealCmdTYRet(buffer);
            break;
        case Const.TER_HEARTBEAT:
            dealCmdHeartbeat(buffer);
            break;
        case Const.TER_LOGIN:
            dealCmdLogin(buffer);
            break;
        case Const.TER_MODULE_STATUS:
            dealCmdModuleStatus(buffer);
            break;
        case Const.TER_GET_MAC2:
            dealCmdGetMac2(buffer);
            break;
        case Const.TER_CHARGE_DETAIL:
        	dealCmdChargeDetail(buffer);
        	break;
        case Const.TER_REFUND:
            dealCmdRefund(buffer);
            break;
        case Const.TER_PREPAID_CARD_CHECK:
            dealCmdPrepaidCardCheck(buffer);
            break;
        case Const.TER_QUERY_ZHB_BALANCE:
            dealCmdQueryZHBBalance(buffer);
            break;
        case Const.TER_MODIFY_ZHB_PASS:
            dealCmdModifyZHBPass(buffer);
            break;
        case Const.TER_CHECK_CITY_CARD_TYPE:
            dealCmdCheckCityCardType(buffer);
            break;
        default:
            dealInvalidCmd(buffer, Const.TY_RET_NOT_SUPPORTED);
            log.warn("Unsupported command flag:{}", KKTool.byteArrayToHexStr(KKTool.short2BytesBigEndian(cmdFlag)));
            break;
        }
    }

    private void dealCmdTYRet(ChannelBuffer buffer) {
        CmdTYRetReq cmdReq = new CmdTYRetReq();
        if (cmdReq.disposeData(buffer)) {
            log.debug("recv ty ret[{}] ", tcpChannel);
        } else {
            log.warn("recv cmd ty ret, but fail to dispose[{}]", tcpChannel);
        }
    }

    private void dealCmdHeartbeat(ChannelBuffer buffer) {
        CmdHeartbeatReq cmdReq = new CmdHeartbeatReq();
        if (cmdReq.disposeData(buffer)) {
            log.debug("recv heartbeat[{}] ", tcpChannel);
            sendCmdTYRetOK(cmdReq);
        } else {
            log.warn("recv cmd heartbeat, but fail to dispose[{}]", tcpChannel);
        }
    }

    private void dealCmdLogin(ChannelBuffer buffer) {
        CmdLoginReq cmdReq = new CmdLoginReq();
        if (cmdReq.disposeData(buffer)) {
            // this means tcpchannel is logined
            this.tcpChannel.setTerminalVer(cmdReq.getVer());
            this.setTcpchannelTerminalId(cmdReq);
            CmdLoginRsp cmdRsp = new CmdLoginRsp();
            cmdRsp.setCmdCommonField(cmdReq);
            cmdRsp.setCmdSNoRsp(cmdReq.getCmdSNo());

            byte ret = 0;
            TerminalBean terminal = getTerminal(cmdReq);
            if (terminal != null) {
                if (terminal.getEnabled() == 1) {// 停用
                    ret = 3;
                } else if (terminal.getChannelId() == -1 || terminal.getChannelId() == tcpChannel.getId()) {
                    ret = 0;
                } else {// 被占用
                    ret = 1;
                }
            } else {//该终端号不存在
                ret = 2;
            }
            cmdRsp.setRet(ret);
            sendData(cmdRsp.getSendBuffer());
            String posId = KKTool.byteArrayToHexStr(cmdReq.getPosId());
            String samId = KKTool.byteArrayToHexStr(cmdReq.getSamId());
            log.info("a client login, login ret[{}], [{}], posId:{}, samID:{}", ret, tcpChannel, posId, samId);
        } else {
            log.warn("recv cmd login, but fail to dispose[{}]", tcpChannel);
        }
    }

    private void dealCmdModuleStatus(ChannelBuffer buffer) {
        CmdModuleStatusReq cmdReq = new CmdModuleStatusReq();
        if (cmdReq.disposeData(buffer)) {
            sendCmdTYRetOK(cmdReq);
            log.info("recv module status[{}]", tcpChannel);
            if (this.cmdBizDisposer != null) {
            	this.cmdBizDisposer.disposeCmdModuleStatus(cmdReq);
            }
        } else {
            log.warn("recv cmd module status, but fail to dispose[{}]", tcpChannel);
        }
    }

    private void dealCmdGetMac2(ChannelBuffer buffer) {
        CmdGetMac2Req cmdReq = new CmdGetMac2Req();
        if (cmdReq.disposeData(buffer)) {
            log.info("recv get mac2[{}]", tcpChannel);
            CmdGetMac2Rsp cmdRsp = new CmdGetMac2Rsp();
            cmdRsp.setCmdCommonField(cmdReq);
            String sMac2 = WSUtil.getWsUtil().getMac2(cmdReq);//"00000000"
            cmdRsp.setRet((byte)0);
            log.info("mac2:{}", sMac2);
            if (!sMac2.equals("") && (sMac2.length() > 8)) {
            	byte[] mac2 = KKTool.strToHexBytes(sMac2.substring(0, 8), 4, 'F');
            	cmdRsp.setMac2(mac2);
            	cmdRsp.setTranSNo(KKTool.strToHexBytes(sMac2.substring(8), 6, '0'));
            	cmdRsp.setRet((byte)1);
            	log.info("mac2:{}, cmdRsp.mac2:{}", KKTool.byteArrayToHexStr(mac2), KKTool.byteArrayToHexStr(cmdRsp.getMac2()));
            }
            sendData(cmdRsp.getSendBuffer());
        } else {
            log.warn("recv cmd get mac2, but fail to dispose[{}]", tcpChannel);
        }
    }

    private void dealCmdChargeDetail(ChannelBuffer buffer) {
    	CmdChargeDetailReq cmdReq = new CmdChargeDetailReq();
    	if (cmdReq.disposeData(buffer)) {
    		byte ret = 1;
    		long recordId = DBOper.getDBOper().addNewChargeDetail(cmdReq);
    		if (recordId < 0) {
    		    ret = 0;
    		}
    		CmdChargeDetailRsp cmdRsp = new CmdChargeDetailRsp();
    		cmdRsp.setCmdCommonField(cmdReq);
    		cmdRsp.setRet(ret);
    		cmdRsp.setRecordId(recordId);
    		sendData(cmdRsp.getSendBuffer());
    		log.info("recv charege detail[{}]", tcpChannel);
    		if (this.cmdBizDisposer != null) {
    			this.cmdBizDisposer.disposeCmdChargeDetail(cmdReq);
    		}
    	} else {
            log.warn("recv cmd charge detail, but fail to dispose[{}]", tcpChannel);
        }
    }
    
    private void dealCmdRefund(ChannelBuffer buffer) {
        CmdRefundReq cmdReq = new CmdRefundReq();
        if (cmdReq.disposeData(buffer)) {
            byte ret = 1;
            int recordId = DBOper.getDBOper().addNewRefund(cmdReq);
            if (recordId < 0) {
                ret = 0;
                log.info("save refund data fail[{}]", tcpChannel);
            }
            CmdRefundRsp cmdRsp = new CmdRefundRsp();
            cmdRsp.setCmdCommonField(cmdReq);
            cmdRsp.setRet(ret);
            cmdRsp.setRecordId(recordId);
            sendData(cmdRsp.getSendBuffer());
            log.info("recv cmd refund[{}]", tcpChannel);
        } else {
            log.warn("recv cmd refund, but fail to dispose[{}]", tcpChannel);
        }
    }
    
    private void dealCmdPrepaidCardCheck(ChannelBuffer buffer) {
        CmdPrepaidCardCheckReq cmdReq = new CmdPrepaidCardCheckReq();
        if (cmdReq.disposeData(buffer)) {
            CmdPrepaidCardCheckRsp cmdRsp = new CmdPrepaidCardCheckRsp();
            WSUtil.getWsUtil().checkPrepaidCard(cmdReq, cmdRsp);
//            cmdRsp.setCheckRet((byte)1);
//            cmdRsp.setAmount(10000);
            cmdRsp.setCmdCommonField(cmdReq);
            sendData(cmdRsp.getSendBuffer());
            log.info("recv cmd prepaid card check, ret:{}, amount:{}", cmdRsp.getCheckRet(), cmdRsp.getAmount());
        } else {
            log.warn("recv cmd prepaid card check, but fail to dispose[{}]", tcpChannel);
        }
    }

    private void dealCmdQueryZHBBalance(ChannelBuffer buffer) {
        CmdQueryZHBBalanceReq cmdReq = new CmdQueryZHBBalanceReq();
        if (cmdReq.disposeData(buffer)) {
            CmdQueryZHBBalanceRsp cmdRsp = new CmdQueryZHBBalanceRsp();
            WSUtil.getWsUtil().queryZHBBalance(cmdReq, cmdRsp);
            cmdRsp.setCmdCommonField(cmdReq);
            sendData(cmdRsp.getSendBuffer());
            log.info("recv cmd query zhb balance, ret:{}, amount:{}", cmdRsp.getCheckRet(), cmdRsp.getAmount());
        } else {
            log.warn("recv cmd query zhb balance, but fail to dispose[{}]", tcpChannel);
        }
    }
    
    private void dealCmdModifyZHBPass(ChannelBuffer buffer) {
        CmdModifyZHBPassReq cmdReq = new CmdModifyZHBPassReq();
        if (cmdReq.disposeData(buffer)) {
            CmdModifyZHBPassRsp cmdRsp = new CmdModifyZHBPassRsp();
            cmdRsp.setCmdCommonField(cmdReq);
            WSUtil.getWsUtil().modifyZHBPassword(cmdReq, cmdRsp);
            sendData(cmdRsp.getSendBuffer());
            log.info("recv cmd modify zhb pass, ret:{}", cmdRsp.getRet());
        } else {
            log.warn("recv cmd modify zhb pass, but fail to dispose[{}]", tcpChannel);
        }
    }
    
    private void dealCmdCheckCityCardType(ChannelBuffer buffer) {
        CmdCheckCityCardTypeReq cmdReq = new CmdCheckCityCardTypeReq();
        if (cmdReq.disposeData(buffer)) {
            CmdCheckCityCardTypeRsp cmdRsp = new CmdCheckCityCardTypeRsp();
            cmdRsp.setCmdCommonField(cmdReq);
            //
            sendData(cmdRsp.getSendBuffer());
            log.info("recv cmd check city card type:{}", cmdRsp.getType());
        } else {
            log.warn("recv cmd check city card type, but fail to dispose[{}]", tcpChannel);
        }
    }
    
    /**
     * 非法命令统一以通用应答处理
     * 
     * @param buffer
     * @param ret
     *            应答结果
     */
    private void dealInvalidCmd(ChannelBuffer buffer, byte ret) {
        CmdDefaultReq cmdReq = new CmdDefaultReq();
        if (cmdReq.disposeData(buffer)) {
            sendCmdTYRsp(cmdReq, ret);
        }
    }
    
    private void sendCmdTYRetOK(AbstractCmdReq cmdReq) {
    	sendCmdTYRsp(cmdReq, Const.TY_RET_OK);
    }

    /**
     * 回复通用应答
     * 
     * @param cmdReq
     * @param ret
     */
    private void sendCmdTYRsp(AbstractCmdReq cmdReq, byte ret) {
        CmdTYRetRsp cmdRsp = new CmdTYRetRsp();
        cmdRsp.setCmdCommonField(cmdReq);
        cmdRsp.setCmdFlagIdRsp(cmdReq.getCmdFlagId());
        cmdRsp.setCmdSNoRsp(cmdReq.getCmdSNo());
        cmdRsp.setRet(ret);
        sendData(cmdRsp.getSendBuffer());
    }

    private void sendData(ChannelBuffer buffer) {
        if (this.tcpChannel != null && !this.tcpChannel.isClosed()) {
            this.tcpChannel.sendData(KKTool.getEscapedBuffer(buffer));
        }
    }

    /**
     * 设置tcpchannel、terminal之间相互关联的值即tcpchannel.terminalId与terminal.channelId
     * 
     * @param cmdReq
     */
    private void setTcpchannelTerminalId(AbstractCmdReq cmdReq) {
        if (this.tcpChannel != null) {
            this.tcpChannel.setTerminalId(cmdReq.getTerminalId());
            TerminalBean terminal = getTerminal(cmdReq);
            if (terminal != null) {
                int oldChannelId = terminal.getChannelId();
                if (oldChannelId == -1) {
                    terminal.setChannelId(tcpChannel.getId());
                } else if (oldChannelId != tcpChannel.getId()) {
                    log.warn("channelid diff,new channelid[{}], old channelid[{}], terminalId[{}]", tcpChannel.getId(),
                            oldChannelId, terminal.getId());
                }
            } else {
                log.warn("can't find terminal bean by terminalId:{}", cmdReq.getTerminalId());
            }

        }
    }

    private TerminalBean getTerminal(AbstractCmdReq cmdReq) {
        return BaseInfo.getBaseInfo().getTerminal(cmdReq.getTerminalId());
    }
}
