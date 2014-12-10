package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

public class CmdAddCashBoxAmountReq extends AbstractCmdReq {
	
	private int amountAdded;
	
	public CmdAddCashBoxAmountReq() {
		
	}

	@Override
	protected int getCmdBodySize() {
		return 4;
	}

	@Override
	protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
		this.amountAdded = channelBuffer.readInt();
		return true;
	}

	public int getAmountAdded() {
		return amountAdded;
	}

}
