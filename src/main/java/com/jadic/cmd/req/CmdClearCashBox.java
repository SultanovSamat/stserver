package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 终端界面操作清空钱箱
 * @author Jadic
 *
 */
public class CmdClearCashBox extends AbstractCmdReq {
	
	private int cashAmount;

	@Override
	protected int getCmdBodySize() {
		return 4;
	}

	@Override
	protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
		cashAmount = channelBuffer.readInt();
		return true;
	}

	public int getCashAmount() {
		return cashAmount;
	}

}
