package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

public class CmdChargeDetailReq extends AbstractCmdReq {

    private byte status;            //交易状态
    private byte[] asn;             //IC应用序列号
    private byte[] tsn;             //IC交易序列号
    private byte[] bankCardNo;      //没有的话直接填19个空格，如果是充值卡直接填写16位充值卡卡号，后三位补空格
    private byte cardType;          //卡类型
    private byte[] transDate;       //交易日期
    private byte[] transTime;       //交易时间
    private int transAmount;        //交易金额
    private int balanceBeforeTrans; //交易前余额
    private byte[] tac;             //TAC认证码
    private byte[] transSNo;        //交易流水号
    private byte chargeType;        //充值类型
    private byte[] cityCardNo;      //市民卡卡号
    
    public CmdChargeDetailReq() {
        this.asn = new byte[10];
        this.tsn = new byte[2];
        this.bankCardNo = new byte[19];
        this.transDate = new byte[4];
        this.transTime = new byte[3];
        this.tac = new byte[4];
        this.transSNo = new byte[6];
        this.cityCardNo = new byte[8];
    }

	@Override
	protected int getCmdBodySize() {
		return 1 
		      + asn.length 
	          + tsn.length 
	          + bankCardNo.length 
	          + 1 
	          + transDate.length 
	          + transTime.length 
  	          + 4 
	          + 4 
	          + tac.length 
	          + transSNo.length
	          + 1
	          + cityCardNo.length;
	}

	@Override
	protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
	    status = channelBuffer.readByte();
	    channelBuffer.readBytes(asn);
	    channelBuffer.readBytes(tsn);
	    channelBuffer.readBytes(bankCardNo);
	    cardType = channelBuffer.readByte();
	    channelBuffer.readBytes(transDate);
	    channelBuffer.readBytes(transTime);
	    transAmount = channelBuffer.readInt();
	    balanceBeforeTrans = channelBuffer.readInt();
	    channelBuffer.readBytes(tac);
	    channelBuffer.readBytes(transSNo);
	    chargeType = channelBuffer.readByte();
	    channelBuffer.readBytes(cityCardNo);
		return true;
	}

    public byte getStatus() {
        return status;
    }

    public byte[] getAsn() {
        return asn;
    }

    public byte[] getTsn() {
        return tsn;
    }

    public byte[] getBankCardNo() {
        return bankCardNo;
    }

    public byte getCardType() {
        return cardType;
    }

    public byte[] getTransDate() {
        return transDate;
    }

    public byte[] getTransTime() {
        return transTime;
    }

    public int getTransAmount() {
        return transAmount;
    }

    public int getBalanceBeforeTrans() {
        return balanceBeforeTrans;
    }

    public byte[] getTac() {
        return tac;
    }

    public byte[] getTransSNo() {
        return transSNo;
    }

    public byte getChargeType() {
        return chargeType;
    }

    public byte[] getCityCardNo() {
        return cityCardNo;
    }

}
