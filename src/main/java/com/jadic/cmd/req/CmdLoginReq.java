package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdLoginReq extends AbstractCmdReq {
    
    private short ver;

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        this.ver = channelBuffer.readShort();
        return true;
    }

	public short getVer() {
        return ver;
    }

    @Override
	protected int getCmdBodySize() {
		return 2;
	}

}
