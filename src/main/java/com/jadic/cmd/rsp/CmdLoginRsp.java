package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdLoginRsp extends AbstractCmdRsp {
    
    private short cmdSNoRsp;
    private byte ret;
    
    public CmdLoginRsp() {
        this.ret = Const.LOGIN_RET_OK;
    }

    @Override
	protected void setCmdFlag() {
    	this.setCmdFlagId(Const.SER_LOGIN_RET);
	}

	@Override
    protected int getCmdBodySize() {
        return 3;
    }

    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeShort(cmdSNoRsp);
        channelBuffer.writeByte(ret);
        return true;
    }

    public short getCmdSNoRsp() {
        return cmdSNoRsp;
    }

    public void setCmdSNoRsp(short cmdSNoRsp) {
        this.cmdSNoRsp = cmdSNoRsp;
    }

    public byte getRet() {
        return ret;
    }

    public void setRet(byte ret) {
        this.ret = ret;
    }

}
