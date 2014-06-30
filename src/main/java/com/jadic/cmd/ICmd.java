package com.jadic.cmd;

import org.jboss.netty.buffer.ChannelBuffer;

public interface ICmd {

    String EMPTY_STR = "";
    byte[] ZERO_BYTES = new byte[0];

    public int getCmdSize();

    public boolean disposeData(ChannelBuffer channelBuffer);

    public ChannelBuffer getSendBuffer();

}
