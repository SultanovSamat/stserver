package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

public class CmdAddCashBoxAmountRsp extends AbstractCmdRsp {
	
	private int cashBoxTotalAmount;
	
	public CmdAddCashBoxAmountRsp() {
		this.cashBoxTotalAmount = 0;
	}

	@Override
	protected void setCmdFlag() {
		this.setCmdFlagId(Const.SER_ADD_CASH_BOX_AMOUNT);
	}

	@Override
	protected int getCmdBodySize() {
		return 4;
	}

	@Override
	protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
		channelBuffer.writeInt(cashBoxTotalAmount);
		return true;
	}

	public int getCashBoxTotalAmount() {
		return cashBoxTotalAmount;
	}

	public void setCashBoxTotalAmount(int cashBoxTotalAmount) {
		this.cashBoxTotalAmount = cashBoxTotalAmount;
	}

}
