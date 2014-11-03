package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

/**
 * @author 	Jadic
 * @created 2014-7-23
 */
public class CmdGetMac2Rsp extends AbstractCmdRsp {
    
    private byte ret;//获取结果  1：成功  其他：失败
    private byte[] mac2;
    private byte[] tranSNo;//交易流水号 格式：YKDQ+12位数字，此处只传12位数字给客户端
    
    public CmdGetMac2Rsp() {
        this.setCmdFlagId(Const.SER_GET_MAC2_RET);
        ret = 0;
        mac2 = new byte[4];
        tranSNo = new byte[6];
    }

    @Override
    protected int getCmdBodySize() {
        return 1 + this.mac2.length + this.tranSNo.length;
    }

    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeByte(this.ret);
        channelBuffer.writeBytes(this.mac2);
        channelBuffer.writeBytes(this.tranSNo);
        return true;
    }

    public void setMac2(byte[] mac2) {
        if (isByteArraySameSize(mac2, this.mac2)) {
            System.arraycopy(mac2, 0, this.mac2, 0, mac2.length);
        }
    }

    public void setTranSNo(byte[] tranSNo) {
        if (isByteArraySameSize(tranSNo, this.tranSNo)) {
            System.arraycopy(tranSNo, 0, this.tranSNo, 0, tranSNo.length);
        }
    }

    public byte getRet() {
        return ret;
    }

    public void setRet(byte ret) {
        this.ret = ret;
    }

    public byte[] getMac2() {
        return mac2;
    }

    public byte[] getTranSNo() {
        return tranSNo;
    }
    
}
