package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdLoginReq extends AbstractCmdReq {
    
    private int identity;

    @Override
    protected void disposeCmdBody(ChannelBuffer channelBuffer) {
        this.identity = channelBuffer.readInt();
    }

    public int getIdentity() {
        return identity;
    }

	@Override
	protected int getCmdBodySize() {
		return 4;
	}

}
