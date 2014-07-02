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
    public boolean disposeData(ChannelBuffer channelBuffer) {
        if (channelBuffer != null && channelBuffer.readableBytes() >= this.getCmdSize()) {
            this.headFlag = channelBuffer.readByte();
            this.cmdFlagId = channelBuffer.readShort();
            this.clientType = channelBuffer.readByte();
            this.terminalId = channelBuffer.readInt();
            this.cmdBodyLen = channelBuffer.readShort();
            this.cmdSNo = channelBuffer.readShort();
            disposeCmdBody(channelBuffer);

            return true;
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
            channelBuffer.writeInt(this.cmdSNo);

            fillCmdBody(channelBuffer);

            //CRC check sum
            channelBuffer.writeByte(this.endFlag);
            return true;
        }
        return false;
    }

    protected int getCmdHeadEndSize() {
        return 14;//1 + 2 + 1 + 4 + 2 + 2 + 1 + 1
    }

    protected abstract short getNextCmdSNo() ;

    protected abstract int getCmdBodySize();

    protected abstract void disposeCmdBody(ChannelBuffer channelBuffer);

    protected abstract void fillCmdBody(ChannelBuffer channelBuffer);

    public short getCmdFlagId() {
        return cmdFlagId;
    }

    public void setCmdFlagId(short cmdFlagId) {
        this.cmdFlagId = cmdFlagId;
    }

    public byte getClientType() {
        return clientType;
    }

    public void setClientType(byte clientType) {
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

    public void setCmdSNo(short cmdSNo) {
        this.cmdSNo = cmdSNo;
    }

    public short getCmdBodyLen() {
        return cmdBodyLen;
    }

}
