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
	private byte[] lastOperTime;
	
	public CmdClearCashBox() {
		operTime = new byte[6];
		lastOperTime = new byte[6];
	}

	@Override
	protected int getCmdBodySize() {
		return 4 + operTime.length + lastOperTime.length;
	}

	@Override
	protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
		cashAmount = channelBuffer.readInt();
		channelBuffer.readBytes(operTime);
		channelBuffer.readBytes(lastOperTime);
		return true;
	}

	public int getCashAmount() {
		return cashAmount;
	}

	public byte[] getOperTime() {
		return operTime;
	}

    public byte[] getLastOperTime() {
        return lastOperTime;
    }

}
