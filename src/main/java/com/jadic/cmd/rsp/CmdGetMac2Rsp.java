package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author 	Jadic
 * @created 2014-7-23
 */
public class CmdGetMac2Rsp extends AbstractCmdRsp {
    
    private byte[] mac2;
    
    public CmdGetMac2Rsp() {
        mac2 = new byte[4];
    }

    @Override
    protected int getCmdBodySize() {
        return this.mac2.length;
    }

    @Override
    protected void fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeBytes(this.mac2);
    }

    public void setMac2(byte[] mac2) {
        if (isByteArraySameSize(mac2, this.mac2)) {
            System.arraycopy(this.mac2, 0, mac2, 0, mac2.length);
        }
    }
    
}
