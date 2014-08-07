package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-8-7
 */
public class CmdRefundReq extends AbstractCmdReq {
    
    private byte[] cityCardNo;
    private int amount;
    private byte chargeType;
    
    public CmdRefundReq() {
        this.cityCardNo = new byte[10];
    }

    @Override
    protected int getCmdBodySize() {
        return this.cityCardNo.length + 4 + 1;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.readBytes(cityCardNo);
        amount = channelBuffer.readInt();
        chargeType = channelBuffer.readByte();
        return true;
    }

    public byte[] getCityCardNo() {
        return cityCardNo;
    }

    public int getAmount() {
        return amount;
    }

    public byte getChargeType() {
        return chargeType;
    }

}
