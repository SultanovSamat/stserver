package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

public class CmdTerminalEnableStatusChanged extends AbstractCmdRsp {
	
	private byte newStatus;
	
	public CmdTerminalEnableStatusChanged() {
		newStatus = 0;
	}
	
	@Override
	protected void setCmdFlag() {
		this.setCmdFlagId(Const.SER_TERM_STATUS_CHANGED);
	}

	@Override
	protected int getCmdBodySize() {
		return 1;
	}

	@Override
	protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
		channelBuffer.writeByte(this.newStatus);
		return true;
	}

	public byte getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(byte newStatus) {
		this.newStatus = newStatus;
	}

}
