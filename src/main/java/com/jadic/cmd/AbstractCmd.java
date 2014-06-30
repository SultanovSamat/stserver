package com.jadic.cmd;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author Jadic
 * @created 2014-5-26
 */
public abstract class AbstractCmd implements ICmd {

    private int cmdSNo;

    private int cmdHeadSize;
    private int cmdEndSize;

    private static int currCmdMaxSNo = 0;

    @Override
    public int getCmdSize() {
        return getCmdHeadEndSize() + getCmdBodySize();
    }

    @Override
    public boolean disposeData(ChannelBuffer channelBuffer) {
        if (channelBuffer != null && channelBuffer.readableBytes() >= this.getCmdSize()) {
            // dispose cmd head
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
        this.cmdSNo = getNextCmdSNo();
    }

    private boolean fillChannelBuffer(ChannelBuffer channelBuffer) {
        if (channelBuffer.writableBytes() >= this.getCmdSize()) {
            // write common fields
            channelBuffer.writeInt(this.cmdSNo);

            fillCmdBody(channelBuffer);

            // write cmd end
            return true;
        }
        return false;
    }

    protected int getCmdHeadEndSize() {
        return this.cmdHeadSize + this.cmdEndSize;
    }

    private static int getNextCmdSNo() {
        if (currCmdMaxSNo <= Integer.MAX_VALUE) {
            return currCmdMaxSNo++;
        } else {
            currCmdMaxSNo = 0;
            return 0;
        }
    }

    protected abstract int getCmdBodySize();

    protected abstract void disposeCmdBody(ChannelBuffer channelBuffer);

    protected abstract void fillCmdBody(ChannelBuffer channelBuffer);

}
