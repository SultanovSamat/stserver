package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

/**
 * 充值明细上传应答
 * @author 	Jadic
 * @created 2014-8-7
 */
public class CmdChargeDetailRsp extends AbstractCmdRsp {
    
    private byte ret;//保存结果 1:成功， 其他失败
    private long recordId;//充值明细唯一编号

    public CmdChargeDetailRsp() {
    }
    
    @Override
	protected void setCmdFlag() {
    	this.setCmdFlagId(Const.SER_CHARGE_DETAIL_RET);
	}

	@Override
    protected int getCmdBodySize() {
        return 9;
    }

    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeByte(ret);
        channelBuffer.writeLong(recordId);
        return true;
    }

    public byte getRet() {
        return ret;
    }

    public void setRet(byte ret) {
        this.ret = ret;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

}
