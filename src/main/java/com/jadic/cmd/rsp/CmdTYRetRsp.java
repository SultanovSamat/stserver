package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdTYRetRsp extends AbstractCmdRsp {
    
    private short cmdSNoRsp;
    private short cmdFlagIdRsp;
    private byte ret;

    public CmdTYRetRsp() {
        this.setCmdFlagId(Const.SER_TY_RET);
        this.setRet(Const.TY_RET_OK);
    }
    
    @Override
    protected int getCmdBodySize() {
        return 5;
    }
    
    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeShort(cmdSNoRsp);
        channelBuffer.writeShort(cmdFlagIdRsp);
        channelBuffer.writeByte(ret);
        return true;
    }

    public short getCmdSNoRsp() {
        return cmdSNoRsp;
    }

    public void setCmdSNoRsp(short cmdSNoRsp) {
        this.cmdSNoRsp = cmdSNoRsp;
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
