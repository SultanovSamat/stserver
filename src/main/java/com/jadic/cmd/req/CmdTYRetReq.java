package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 终端通用应答
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdTYRetReq extends AbstractCmdReq {
    
    private short cmdSNORsp;
    private short cmdFlagIdRsp;
    private byte ret;
    
    @Override
    protected void disposeCmdBody(ChannelBuffer channelBuffer) {
        this.cmdSNORsp = channelBuffer.readShort();
        this.cmdFlagIdRsp = channelBuffer.readShort();
        this.ret = channelBuffer.readByte();
    }

    public short getCmdSNORsp() {
        return cmdSNORsp;
    }

    public short getCmdFlagIdRsp() {
        return cmdFlagIdRsp;
    }

    public byte getRet() {
        return ret;
    }

	@Override
	protected int getCmdBodySize() {
		return 5;
	}

}
