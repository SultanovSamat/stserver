package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;
import com.jadic.utils.KKTool;

/**
 * @author 	Jadic
 * @created 2015-2-3
 */
public class CmdGetServerTimeRsp extends AbstractCmdRsp {
    
    private byte[] serverTime;//2014-10-10 12:33:44
    
    public CmdGetServerTimeRsp() {
        this.serverTime = KKTool.strToHexBytes(KKTool.getCurrFormatDate("yyyyMMddHHmmss"), 7, '0');
    }

    @Override
    protected void setCmdFlag() {
        this.setCmdFlagId(Const.SER_GET_SERVER_TIME);
    }

    @Override
    protected int getCmdBodySize() {
        return this.serverTime.length;
    }

    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeBytes(this.serverTime);
        return true;
    }

    public byte[] getServerTime() {
        return serverTime;
    }

}
