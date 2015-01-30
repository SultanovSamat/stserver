package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2015-1-30
 */
public class CmdOperLogReq extends AbstractCmdReq {
    
    private byte logType;

    @Override
    protected int getCmdBodySize() {
        return 1;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        this.logType = channelBuffer.readByte();
        return true;
    }

    public byte getLogType() {
        return logType;
    }

}
