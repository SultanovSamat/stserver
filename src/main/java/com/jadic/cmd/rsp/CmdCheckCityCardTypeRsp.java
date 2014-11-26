package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.utils.Const;

/**
 * 检测卡片是否记名的应答
 * @author 	Jadic
 * @created 2014-11-6
 */
public class CmdCheckCityCardTypeRsp extends AbstractCmdRsp {

    private byte[] cityCardNo;//市民卡卡号
    private byte type;//0:未知 1:记名 2:不记名
    
    public CmdCheckCityCardTypeRsp() {
        cityCardNo = new byte[8];
    }

    @Override
	protected void setCmdFlag() {
    	setCmdFlagId(Const.SER_CHECK_CITY_CARD_TYPE);
	}
    
    @Override
    protected int getCmdBodySize() {
        return cityCardNo.length + 1;
    }

    @Override
    protected boolean fillCmdBody(ChannelBuffer channelBuffer) {
        channelBuffer.writeBytes(cityCardNo);
        channelBuffer.writeByte(type);
        return true;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getCityCardNo() {
        return cityCardNo;
    }

    public void setCityCardNo(byte[] cityCardNo) {
        if (isByteArraySameSize(cityCardNo, this.cityCardNo)) {
            System.arraycopy(cityCardNo, 0, this.cityCardNo, 0, cityCardNo.length);
        }
    }
}
