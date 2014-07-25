package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 终端通用应答
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdTYRetReq extends AbstractCmdReq {
    
    private short cmdSNoRsp;
    private short cmdFlagIdRsp;
    private byte ret;
    
    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        this.cmdSNoRsp = channelBuffer.readShort();
        this.cmdFlagIdRsp = channelBuffer.readShort();
        this.ret = channelBuffer.readByte();
        return true;
    }

    public short getCmdSNoRsp() {
        return cmdSNoRsp;
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
