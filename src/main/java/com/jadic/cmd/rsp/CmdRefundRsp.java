package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

/**
 * 退款申请记录应答
 * @author 	Jadic
 * @created 2014-8-7
 */
public class CmdRefundRsp extends AbstractCmdRsp {
    
    private byte ret;//保存结果 1:成功， 其他失败
    private int recordId;//退款记录唯一编号
    
    public CmdRefundRsp() {
        this.setCmdFlagId(Const.SER_REFUND_RET);
    }

    @Override
    protected int getCmdBodySize() {
        return 5;
    }

    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeByte(ret);
        channelBuffer.writeInt(recordId);
        return true;
    }

    public byte getRet() {
        return ret;
    }

    public void setRet(byte ret) {
        this.ret = ret;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

}
