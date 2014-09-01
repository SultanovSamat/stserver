package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 修改账户宝密码请求
 * @author 	Jadic
 * @created 2014-9-1
 */
public class CmdModifyZHBPassReq extends AbstractCmdReq {
    
    private byte[] oldPass;
    private byte[] newPass;
    private byte[] cardNo;
    private byte[] termNo;
    private byte[] asn;
    private byte[] randNumber;
    private byte[] cardTradNo;
    private int cardOldBalance;
    private byte[] mac1;
    private byte[] chargeDate;
    private byte[] chargeTime;

    public CmdModifyZHBPassReq() {
        this.oldPass = new byte[3];
        this.newPass = new byte[3];
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
        return oldPass.length + 
                newPass.length + 
                cardNo.length + 
                termNo.length + 
                asn.length + 
                randNumber.length + 
                cardTradNo.length + 
                4 + 
                mac1.length + 
                chargeDate.length + 
                chargeTime.length;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.readBytes(oldPass);
        channelBuffer.readBytes(newPass);
        channelBuffer.readBytes(cardNo);
        channelBuffer.readBytes(termNo);
        channelBuffer.readBytes(asn);
        channelBuffer.readBytes(randNumber);
        channelBuffer.readBytes(cardTradNo);
        cardOldBalance = channelBuffer.readInt();
        channelBuffer.readBytes(mac1);
        channelBuffer.readBytes(chargeDate);
        channelBuffer.readBytes(chargeTime);
        return true;
    }

    public byte[] getOldPass() {
        return oldPass;
    }

    public byte[] getNewPass() {
        return newPass;
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
