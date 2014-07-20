package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.cmd.AbstractCmd;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public abstract class AbstractCmdReq extends AbstractCmd {

    @Override
    protected void fillCmdBody(ChannelBuffer channelBuffer) {
    }
    
    @Override
    protected short getNextCmdSNo() {
        return 0;
    }

//    @Override
//    protected int getCmdBodySize() {
//        return 0;
//    }

}
