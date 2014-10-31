package com.jadic.tcp.server;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.utils.KKTool;

/**
 * @author 	Jadic
 * @created 2014-4-9
 */
public class TcpServerHandler extends SimpleChannelHandler {
    
    private final static Logger log = LoggerFactory.getLogger(TcpServerHandler.class);
	
	private TcpServer tcpServer;
	
	public TcpServerHandler(TcpServer tcpServer) {
		this.tcpServer = tcpServer;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
		TcpChannel tcpChannel = this.tcpServer.getTcpChannel(ctx.getChannel().getId());
		if (tcpChannel != null) {
			tcpChannel.addRecvDataAndDispose(KKTool.getUnescapedBuffer(buffer));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		TcpChannel tcpChannel = this.tcpServer.getTcpChannel(ctx.getChannel().getId());
		log.error("{} exception caught:{}", tcpChannel, e);
		this.tcpServer.removeTcpChannel(ctx.getChannel().getId());
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		TcpChannel tcpChannel = this.tcpServer.addTcpChannel(ctx.getChannel());
		log.info("a tcp client [{}] connected to tcp server, total:{}", tcpChannel, tcpServer.getClientsCount());
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		TcpChannel tcpChannel = this.tcpServer.getTcpChannel(ctx.getChannel().getId());
		this.tcpServer.removeTcpChannel(ctx.getChannel().getId());
		log.info("a tcp client [{}] disconnected from tcp server, total:{}", tcpChannel != null ? tcpChannel : ctx.getChannel().getRemoteAddress(), tcpServer.getClientsCount());
	}

}
