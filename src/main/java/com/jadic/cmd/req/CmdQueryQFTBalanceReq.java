package com.jadic.cmd.req;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 企福通卡余额查询
 * @author 	Jadic
 * @created 2014-8-14
 */
public class CmdQueryQFTBalanceReq extends AbstractCmdReq {
    
    private byte[] cityCardNo;//市民卡卡号
    private byte[] password;//企福通密码
    
    public CmdQueryQFTBalanceReq() {
        cityCardNo = new byte[8];
        password = new byte[3];
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
