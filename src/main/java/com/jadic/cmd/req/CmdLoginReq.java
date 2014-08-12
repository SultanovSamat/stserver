package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public class CmdLoginReq extends AbstractCmdReq {
    
    private short ver;
    private byte[] posId;//BCD;
    private byte[] samId;//BCD;
    
    public CmdLoginReq() {
        this.posId = new byte[6];
        this.samId = new byte[6];
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        this.ver = channelBuffer.readShort();
        channelBuffer.readBytes(posId);
        channelBuffer.readBytes(samId);
        return true;
    }

    @Override
	protected int getCmdBodySize() {
		return 2 + posId.length + samId.length;
	}
    
    public short getVer() {
        return ver;
    }

    public byte[] getPosId() {
        return posId;
    }

    public byte[] getSamId() {
        return samId;
    }

}
