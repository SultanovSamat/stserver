package com.jadic;

import com.jadic.tcp.server.TcpChannel;

public interface IMainServer {

	public TcpChannel getTcpChannelByTerminalId(int terminalId);
	public TcpChannel getTcpChannelByTChannelId(int channelId);
	
}
