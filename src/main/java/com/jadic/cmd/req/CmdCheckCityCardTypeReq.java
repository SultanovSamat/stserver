package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 检测卡片是否记名
 * @author 	Jadic
 * @created 2014-11-6
 */
public class CmdCheckCityCardTypeReq extends AbstractCmdReq {

    private byte[] cityCardNo;//市民卡卡号
    
    public CmdCheckCityCardTypeReq() {
        cityCardNo = new byte[8];
    }
    
    @Override
    protected int getCmdBodySize() {
        return this.cityCardNo.length;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.readBytes(cityCardNo);
        return true;
    }

    public byte[] getCityCardNo() {
        return cityCardNo;
    }

}
