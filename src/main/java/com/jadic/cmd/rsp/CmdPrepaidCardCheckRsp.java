package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

/**
 * @author 	Jadic
 * @created 2014-8-12
 */
public class CmdPrepaidCardCheckRsp extends AbstractCmdRsp {
    
    private byte checkRet;//校验结果  0:失败 1:成功 2：密码错误
    private int amount;//充值卡充值面额
    //private byte[] tradeSNo;//交易流水号，市民卡中心返回 直接用字符串表示，最长30位，后补0x00,如交易号是 1234，那么数据31323334..
    
    public CmdPrepaidCardCheckRsp() {
        setCmdFlagId(Const.SER_PREPAID_CARD_CHECK);
        //tradeSNo = new byte[30];
    }

    @Override
    protected int getCmdBodySize() {
        return 1 + 4;// + tradeSNo.length;
    }

    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeByte(checkRet);
        channelBuffer.writeInt(amount);
        //channelBuffer.writeBytes(tradeSNo);
        return true;
    }

    public byte getCheckRet() {
        return checkRet;
    }

    public void setCheckRet(byte checkRet) {
        this.checkRet = checkRet;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
//
//    public byte[] getTradeSNo() {
//        return tradeSNo;
//    }
//
//    public void setTradeSNo(byte[] tradeSNo) {
//        this.tradeSNo = tradeSNo;
//    }

}
