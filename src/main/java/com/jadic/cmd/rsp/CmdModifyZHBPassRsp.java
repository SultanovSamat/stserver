package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 修改账户宝密码应答
 * @author 	Jadic
 * @created 2014-9-1
 */
public class CmdModifyZHBPassRsp extends AbstractCmdRsp {
    
    private byte ret;//校验结果  0:失败 1:成功 2：密码错误

    @Override
    protected int getCmdBodySize() {
        return 1;
    }

    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeByte(ret);
        return true;
    }

    public byte getRet() {
        return ret;
    }

    public void setRet(byte ret) {
        this.ret = ret;
    }

}
