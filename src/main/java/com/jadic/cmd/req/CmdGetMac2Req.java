package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author Jadic
 * @created 2014-7-23
 */
public class CmdGetMac2Req extends AbstractCmdReq {

    private byte operType;
    private byte[] cardNo;
    private byte[] password;
    private byte[] termNo;
    private byte[] asn;
    private byte[] randNumber;
    private byte[] cardTradNo;
    private int cardOldBalance;
    private int chargeAmount;
    private byte[] mac1;
    private byte[] chargeDate;
    private byte[] chargeTime;
    private byte status;//0:第一次获取mac2 1:非第一次
    private byte[] tranSNo;//交易流水号，供补写卡时采用与第一次获取mac2时采用的流水号

    public CmdGetMac2Req() {
        this.cardNo = new byte[8];
        this.password = new byte[19];
        this.termNo = new byte[6];
        this.asn = new byte[10];
        this.randNumber = new byte[4];
        this.cardTradNo = new byte[2];
        this.mac1 = new byte[4];
        this.chargeDate = new byte[4];
        this.chargeTime = new byte[3];
        this.tranSNo = new byte[6];
    }

    @Override
    protected int getCmdBodySize() {
        return 1 + cardNo.length + password.length + termNo.length + asn.length 
        		+ randNumber.length + cardTradNo.length + 4 + 4 + mac1.length
                + chargeDate.length + chargeTime.length + 1 + tranSNo.length;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        this.operType = channelBuffer.readByte();
        channelBuffer.readBytes(this.cardNo);
        channelBuffer.readBytes(this.password);
        channelBuffer.readBytes(this.termNo);
        channelBuffer.readBytes(this.asn);
        channelBuffer.readBytes(this.randNumber);
        channelBuffer.readBytes(this.cardTradNo);
        this.cardOldBalance = channelBuffer.readInt();
        this.chargeAmount = channelBuffer.readInt();
        channelBuffer.readBytes(this.mac1);
        channelBuffer.readBytes(this.chargeDate);
        channelBuffer.readBytes(this.chargeTime);
        this.status = channelBuffer.readByte();
        channelBuffer.readBytes(this.tranSNo);
        return true;
    }

    public byte getOperType() {
        return operType;
    }

    public byte[] getCardNo() {
        return cardNo;
    }

    public byte[] getPassword() {
		return password;
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

    public byte getStatus() {
        return status;
    }

    public byte[] getTranSNo() {
        return tranSNo;
    }

    public boolean isFirstReq() {
        return this.status == 0;
    }
}
