package com.jadic.biz;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.biz.bean.TerminalBean;
import com.jadic.cmd.AbstractCmd;
import com.jadic.cmd.req.AbstractCmdReq;
import com.jadic.cmd.req.CmdAddCashBoxAmountReq;
import com.jadic.cmd.req.CmdChargeDetailReq;
import com.jadic.cmd.req.CmdCheckCityCardTypeReq;
import com.jadic.cmd.req.CmdClearCashBox;
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
import com.jadic.cmd.rsp.CmdAddCashBoxAmountRsp;
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
//        if (!tcpChannel.isLogined() && cmdFlag != Const.TER_LOGIN) {
//            dealInvalidCmd(buffer, Const.TY_RET_NO_LOGIN);
//            return;
//        }

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
        case Const.TER_CLEAR_CASH_BOX:
        	dealCmdClearCashBox(buffer);
        	break;
        case Const.TER_ADD_CASH_BOX_AMOUNT: 
        	dealCmdAddCashBoxAmount(buffer);
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
            log.info("a client login, login ret[{}], [{}], ver:{}", ret, tcpChannel, KKTool.short2HexStr(cmdReq.getVer()));
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
            WSUtil.getWsUtil().getMac2(cmdReq, cmdRsp);//"00000000"
            sendData(cmdRsp.getSendBuffer());
        } else {
            log.warn("recv cmd get mac2, but fail to dispose[{}]", tcpChannel);
        }
    }

    private void dealCmdChargeDetail(ChannelBuffer buffer) {
    	CmdChargeDetailReq cmdReq = new CmdChargeDetailReq();
    	if (cmdReq.disposeData(buffer)) {
    		byte ret = 1;
    		long recordId = 0;
    		byte chargeStatus = cmdReq.getStatus();
    		byte chargeType = cmdReq.getChargeType();
    		int terminalId = cmdReq.getTerminalId();
    		if (chargeStatus == Const.CHARGE_DETAIL_STATUS_SUCC) {//仅保存成功交易的记录，失败的记录会有退款记录
        		recordId = DBOper.getDBOper().addNewChargeDetail(cmdReq);
        		if (recordId < 0) {
        		    ret = 0;
        		}
        		
        		if (tcpChannel.getTerminalVer() <= 0x0100 && chargeType == Const.CHARGE_TYPE_CASH) {
        			int amountAdded = cmdReq.getTransAmount() / 100;//转成元
        			if (DBOper.getDBOper().addCashBoxAmount(terminalId, amountAdded)) {
        				log.info("CmdChargeDetail:succeed to add cash box amount, terminalId:{}, amountAdded:{}", terminalId, amountAdded);
        			} else {
        				log.info("CmdChargeDetail:fail to add cash box amount, terminalId:{}, amountAdded:{}", terminalId, amountAdded);
        			}
        		}
    		}
    		CmdChargeDetailRsp cmdRsp = new CmdChargeDetailRsp();
    		cmdRsp.setCmdCommonField(cmdReq);
    		cmdRsp.setRet(ret);
    		cmdRsp.setRecordId(recordId);
    		sendData(cmdRsp.getSendBuffer());
    		log.info("recv charge detail[{}]", tcpChannel);
    		if (this.cmdBizDisposer != null) {
    		    //只上传成功的现金和银行卡充值记录
    		    if (chargeStatus == Const.CHARGE_DETAIL_STATUS_SUCC && chargeType <= Const.CHARGE_TYPE_BANK_CARD) {
    		        this.cmdBizDisposer.disposeCmdChargeDetail(cmdReq);
    		    }
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
            
            if (tcpChannel.getTerminalVer() <= 0x0100 && cmdReq.getChargeType() == Const.CHARGE_TYPE_CASH) {
            	int terminalId = cmdReq.getTerminalId();
    			int amountAdded = cmdReq.getAmount() / 100;//转成元
    			if (DBOper.getDBOper().addCashBoxAmount(terminalId, amountAdded)) {
    				log.info("CmdRefund:succeed to add cash box amount, terminalId:{}, amountAdded:{}", terminalId, amountAdded);
    			} else {
    				log.info("CmdRefund:fail to add cash box amount, terminalId:{}, amountAdded:{}", terminalId, amountAdded);
    			}
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
            cmdRsp.setCityCardNo(cmdReq.getCityCardNo());
            String cityCardNo = KKTool.byteArrayToHexStr(cmdReq.getCityCardNo());
            byte cityCardType = WSUtil.getWsUtil().checkCityCardType(cityCardNo);
            cmdRsp.setType(cityCardType);
            sendData(cmdRsp.getSendBuffer());
            log.info("recv cmd check city card type:{}", cmdRsp.getType());
        } else {
            log.warn("recv cmd check city card type, but fail to dispose[{}]", tcpChannel);
        }
    }
    
    private void dealCmdClearCashBox(ChannelBuffer buffer) {
    	CmdClearCashBox cmdReq = new CmdClearCashBox();
    	if (cmdReq.disposeData(buffer)) {
    		TerminalBean terminal = getTerminal(cmdReq);
    		if (terminal != null) {
    			terminal.setTotalCashAmount(0);
    		}
    		if (DBOper.getDBOper().setCashBoxAmountZero(cmdReq.getTerminalId())) {
    			log.info("succeed to set cash box amount zero, terminalId:{}", cmdReq.getTerminalId());
    		} else {
    			log.info("fail to set cash box amount zero, terminalId:{}", cmdReq.getTerminalId());
    		}
    		
    		if (DBOper.getDBOper().addWithdrawDetail(cmdReq)) {
    		    log.info("succeed to add Withdraw Detail, terminalId:{}", cmdReq.getTerminalId());
    		} else {
    		    log.info("fail to add Withdraw Detail, terminalId:{}", cmdReq.getTerminalId());
    		}
    		sendCmdTYRetOK(cmdReq);
    	} else {
    		log.warn("recv cmd clear cash box, but fail to dispose[{}]", tcpChannel);
    	}
    }
    
    private void dealCmdAddCashBoxAmount(ChannelBuffer buffer) {
    	CmdAddCashBoxAmountReq cmdReq = new CmdAddCashBoxAmountReq();
    	if (cmdReq.disposeData(buffer)) {
    	    if (cmdReq.getAmountAdded() <= 0) {
    	        log.warn("recv cmd add cash box amount,but amount added is not greater than 0,quit");
    	        return;
    	    }
    		TerminalBean terminal = getTerminal(cmdReq);
    		int totalCashAmount = 0;
    		if (terminal != null) {
    			terminal.addCashAmount(cmdReq.getAmountAdded());
    			totalCashAmount = terminal.getTotalCashAmount();
    			if (DBOper.getDBOper().setCashBoxAmount(cmdReq.getTerminalId(), totalCashAmount)) {
    			    log.info("succeed to set cashbox amount,terminalId:{}, amountAdded:{}, totalAmount:{}", 
    			            cmdReq.getTerminalId(), cmdReq.getAmountAdded(), totalCashAmount);
    			} else {
    			    log.info("fail to add cashbox amount, terminalId:{}", cmdReq.getTerminalId());
    			}
    		}
    		CmdAddCashBoxAmountRsp cmdRsp = new CmdAddCashBoxAmountRsp();
    		cmdRsp.setCmdCommonField(cmdReq);
    		cmdRsp.setCashBoxTotalAmount(totalCashAmount);
    		sendCmd(cmdRsp);
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
    
    private void sendCmd(AbstractCmd cmd) {
    	if (cmd != null) {
    		sendData(cmd.getSendBuffer());
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
        return cmdReq == null ? null : BaseInfo.getBaseInfo().getTerminal(cmdReq.getTerminalId());
    }
    
}
