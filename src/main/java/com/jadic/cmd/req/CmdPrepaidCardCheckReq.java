package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 充值卡校验请求
 * @author 	Jadic
 * @created 2014-8-12
 */
public class CmdPrepaidCardCheckReq extends AbstractCmdReq {
    
    private byte[] cityCardNo;//市民卡卡号
    private byte[] password;//充值卡密码
    
    public CmdPrepaidCardCheckReq() {
        cityCardNo = new byte[8];
        password = new byte[8];
    }

    @Override
    protected int getCmdBodySize() {
        return this.cityCardNo.length + this.password.length;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.readBytes(cityCardNo);
        channelBuffer.readBytes(password);
        return true;
    }

    public byte[] getCityCardNo() {
        return cityCardNo;
    }

    public byte[] getPassword() {
        return password;
    }

}
