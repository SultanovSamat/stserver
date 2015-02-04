package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.KKTool;

/**
 * @author 	Jadic
 * @created 2015-2-4
 */
public class CmdRefundWithReasonReq extends CmdRefundReq {
    
    private byte reasonSize;//原因长度
    private byte[] reason;//原因ascii码
    
    public CmdRefundWithReasonReq() {
        this.reasonSize = 0;
        this.reason = ZERO_BYTES;
    }

    @Override
    protected int getCmdBodySize() {
        return super.getCmdBodySize() + 2;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        if (super.disposeCmdBody(channelBuffer)) {
            reasonSize = channelBuffer.readByte();
            if (reasonSize > 0) {
                this.reason = new byte[KKTool.byteToInt(reasonSize)];
                channelBuffer.readBytes(this.reason);
            }
            return true;
        }
        return false;
    }

    public byte getReasonSize() {
        return reasonSize;
    }

    public byte[] getReason() {
        return reason;
    }

}
