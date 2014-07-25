package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdHeartbeatReq extends AbstractCmdReq {
    
    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        return true;
    }

	@Override
	protected int getCmdBodySize() {
		return 0;
	}

}
