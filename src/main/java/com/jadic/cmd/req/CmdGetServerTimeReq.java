package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2015-2-3
 */
public class CmdGetServerTimeReq extends AbstractCmdReq {

    @Override
    protected int getCmdBodySize() {
        return 0;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        return true;
    }

}
