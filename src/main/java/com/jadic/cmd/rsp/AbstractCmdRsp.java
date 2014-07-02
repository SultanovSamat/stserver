package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.cmd.AbstractCmd;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public abstract class AbstractCmdRsp extends AbstractCmd {
    
    private static short cmdSNo = 0;

    @Override
    protected short getNextCmdSNo() {
        if (cmdSNo >= Short.MAX_VALUE || cmdSNo < 0) {
            cmdSNo = 0;
        }
        return cmdSNo ++;
    }

    @Override
    protected void disposeCmdBody(ChannelBuffer channelBuffer) {
    }

}
