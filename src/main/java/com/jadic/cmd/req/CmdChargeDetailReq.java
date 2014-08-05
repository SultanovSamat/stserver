package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

public class CmdChargeDetailReq extends AbstractCmdReq {

	@Override
	protected int getCmdBodySize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
		// TODO Auto-generated method stub
		return false;
	}

}
