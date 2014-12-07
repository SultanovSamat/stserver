package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 终端界面操作清空钱箱
 * @author Jadic
 *
 */
public class CmdClearCashBox extends AbstractCmdReq {
	
	private int cashAmount;
	private byte[] operTime;
	
	public CmdClearCashBox() {
		operTime = new byte[6];
	}

	@Override
	protected int getCmdBodySize() {
		return 4 + operTime.length;
	}

	@Override
	protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
		cashAmount = channelBuffer.readInt();
		channelBuffer.readBytes(operTime);
		return true;
	}

	public int getCashAmount() {
		return cashAmount;
	}

	public byte[] getOperTime() {
		return operTime;
	}

}
