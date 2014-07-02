package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdRetRsp extends AbstractCmdRsp {
    
    private short cmdSNORsp;
    private short cmdFlagIdRsp;
    private byte ret;

    @Override
    protected int getCmdBodySize() {
        return 5;
    }
    
    @Override
    protected void fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeShort(cmdSNORsp);
        channelBuffer.writeShort(cmdFlagIdRsp);
        channelBuffer.writeByte(ret);
    }

    public short getCmdSNORsp() {
        return cmdSNORsp;
    }

    public void setCmdSNORsp(short cmdSNORsp) {
        this.cmdSNORsp = cmdSNORsp;
    }

    public short getCmdFlagIdRsp() {
        return cmdFlagIdRsp;
    }

    public void setCmdFlagIdRsp(short cmdFlagIdRsp) {
        this.cmdFlagIdRsp = cmdFlagIdRsp;
    }

    public byte getRet() {
        return ret;
    }

    public void setRet(byte ret) {
        this.ret = ret;
    }

}
