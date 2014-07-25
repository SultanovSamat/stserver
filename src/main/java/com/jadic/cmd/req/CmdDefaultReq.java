package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-7-25
 */
public class CmdDefaultReq extends AbstractCmdReq {

    @Override
    protected int getCmdBodySize() {
        return 0;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        return true;
    }

}
