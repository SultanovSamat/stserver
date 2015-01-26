package com.jadic.cmd;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.jadic.utils.Const;

/**
 * @author Jadic
 * @created 2014-5-26
 */
public abstract class AbstractCmd implements ICmd {
	
	private byte headFlag;		//头标识
	
	private short cmdFlagId;	//命令ID
	private byte clientType;	//终端类型
	private int terminalId;    //终端ID
	private short cmdBodyLen;	//消息体长度
    private short cmdSNo;		//消息流水号
    private short cmdSNoResp;   //应答消息流水号
    
    //....						  消息体
    
    //....						  校验位
    
    private byte endFlag;		//尾标识
    
    public AbstractCmd() {
    }

    @Override
    public int getCmdSize() {
        return getCmdHeadEndSize() + getCmdBodySize();
    }

    @Override
    public boolean disposeData(ChannelBuffer buffer) {
        if (buffer != null && buffer.readableBytes() >= this.getCmdSize()) {
            this.headFlag = buffer.readByte();
            this.cmdFlagId = buffer.readShort();
            this.clientType = buffer.readByte();
            this.terminalId = buffer.readInt();
            this.cmdBodyLen = buffer.readShort();
            this.cmdSNo = buffer.readShort();
            this.cmdSNoResp = buffer.readShort();
            return disposeCmdBody(buffer);
        }
        return false;
    }

    @Override
    public ChannelBuffer getSendBuffer() {
        setCmdCommonField();
        ChannelBuffer channelBuffer = ChannelBuffers.buffer(this.getCmdSize());
        fillChannelBuffer(channelBuffer);
        return channelBuffer;
    }

    private void setCmdCommonField() {
    	this.headFlag = Const.CMD_HEAD_FLAG;
        this.cmdSNo = getNextCmdSNo();
        this.cmdBodyLen = (short)this.getCmdBodySize();
        this.endFlag = Const.CMD_END_FLAG;
    }

    private boolean fillChannelBuffer(ChannelBuffer channelBuffer) {
        if (channelBuffer.writableBytes() >= this.getCmdSize()) {
            // write common fields
        	channelBuffer.writeByte(this.headFlag);
        	channelBuffer.writeShort(this.cmdFlagId);
        	channelBuffer.writeByte(this.clientType);
        	channelBuffer.writeInt(this.terminalId);
        	channelBuffer.writeShort(this.cmdBodyLen);
            channelBuffer.writeShort(this.cmdSNo);
            channelBuffer.writeShort(this.cmdSNoResp);

            if (fillCmdBody(channelBuffer)) {
                //CRC check sum
                channelBuffer.writeByte(getCRCCheckSum(channelBuffer));
                channelBuffer.writeByte(this.endFlag);
                return true;
            }
        }
        return false;
    }

    protected int getCmdHeadEndSize() {
        return 16;//1 + 2 + 1 + 4 + 2 + 2 + 2 + 1 + 1
    }
    
    private byte getCRCCheckSum(ChannelBuffer buffer) {
        return 0;
    }
    
    /**
     * 判断字节数组长度是否一致
     * @param buf1
     * @param buf2
     * @return true if both buf1 and buf2 are not null, and length of buf1 equals length of buf2
     */
    public boolean isByteArraySameSize(byte[] buf1, byte[] buf2) {
        return buf1 != null && buf2 != null && buf1.length == buf2.length;
    }

    protected abstract short getNextCmdSNo() ;

    protected abstract int getCmdBodySize();

    protected abstract boolean disposeCmdBody(ChannelBuffer channelBuffer);

    protected abstract boolean fillCmdBody(ChannelBuffer channelBuffer);

    public short getCmdFlagId() {
        return cmdFlagId;
    }

    protected void setCmdFlagId(short cmdFlagId) {
        this.cmdFlagId = cmdFlagId;
    }

    public byte getClientType() {
        return clientType;
    }

    protected void setClientType(byte clientType) {
        this.clientType = clientType;
    }

    public int getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(int terminalId) {
        this.terminalId = terminalId;
    }

    public short getCmdSNo() {
        return cmdSNo;
    }

    public short getCmdSNoResp() {
        return cmdSNoResp;
    }
    
    protected void setCmdSNoResp(short cmdSNoResp) {
        this.cmdSNoResp = cmdSNoResp;
    }

    public short getCmdBodyLen() {
        return cmdBodyLen;
    }

}
