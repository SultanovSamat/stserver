package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-8-7
 */
public class CmdRefundReq extends AbstractCmdReq {
    
    private byte[] cityCardNo;//市民卡卡号
    private int amount;//退款金额
    private byte[] time;//退款时间
    private byte chargeType;
    
    public CmdRefundReq() {
        this.cityCardNo = new byte[8];
        this.time = new byte[7];
    }

    @Override
    protected int getCmdBodySize() {
        return this.cityCardNo.length + 4 + this.time.length + 1;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.readBytes(cityCardNo);
        amount = channelBuffer.readInt();
        channelBuffer.readBytes(time);
        chargeType = channelBuffer.readByte();
        return true;
    }

    public byte[] getCityCardNo() {
        return cityCardNo;
    }

    public int getAmount() {
        return amount;
    }

    public byte[] getTime() {
        return time;
    }

    public byte getChargeType() {
        return chargeType;
    }

}
