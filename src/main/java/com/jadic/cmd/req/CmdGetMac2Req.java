package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author Jadic
 * @created 2014-7-23
 */
public class CmdGetMac2Req extends AbstractCmdReq {

    private byte operType;
    private byte[] cardNo;
    private byte[] termNo;
    private byte[] asn;
    private byte[] randNumber;
    private byte[] cardTradNo;
    private int cardOldBalance;
    private int chargeAmount;
    private byte[] mac1;
    private byte[] chargeDate;
    private byte[] chargeTime;

    public CmdGetMac2Req() {
        this.cardNo = new byte[8];
        this.termNo = new byte[6];
        this.asn = new byte[10];
        this.randNumber = new byte[4];
        this.cardTradNo = new byte[2];
        this.mac1 = new byte[4];
        this.chargeDate = new byte[4];
        this.chargeTime = new byte[3];
    }

    @Override
    protected int getCmdBodySize() {
        return 1 + cardNo.length + termNo.length + asn.length + randNumber.length 
                + cardTradNo.length + 4 + 4 + mac1.length
                + chargeDate.length + chargeTime.length;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        this.operType = channelBuffer.readByte();
        channelBuffer.readBytes(this.cardNo);
        channelBuffer.readBytes(this.termNo);
        channelBuffer.readBytes(this.asn);
        channelBuffer.readBytes(this.randNumber);
        channelBuffer.readBytes(this.cardTradNo);
        this.cardOldBalance = channelBuffer.readInt();
        this.chargeAmount = channelBuffer.readInt();
        channelBuffer.readBytes(this.mac1);
        channelBuffer.readBytes(this.chargeDate);
        channelBuffer.readBytes(this.chargeTime);
        return true;
    }

    public byte getOperType() {
        return operType;
    }

    public byte[] getCardNo() {
        return cardNo;
    }

    public byte[] getTermNo() {
        return termNo;
    }

    public byte[] getAsn() {
        return asn;
    }

    public byte[] getRandNumber() {
        return randNumber;
    }

    public byte[] getCardTradNo() {
        return cardTradNo;
    }

    public int getCardOldBalance() {
        return cardOldBalance;
    }

    public int getChargeAmount() {
        return chargeAmount;
    }

    public byte[] getMac1() {
        return mac1;
    }

    public byte[] getChargeDate() {
        return chargeDate;
    }

    public byte[] getChargeTime() {
        return chargeTime;
    }

}
