package com.jadic.tcp.server;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.biz.BaseInfo;
import com.jadic.biz.ThreadDisposeTcpChannelData;
import com.jadic.tcp.TcpDataDecoder;
import com.jadic.utils.KKSimpleTimer;


/**
 * @author 	Jadic
 * @created 2014-5-26
 */
public class TcpServer implements ITcpChannelDisposer {
    private final static Logger log = LoggerFactory.getLogger(TcpServer.class);
    
    private final Map<Integer, TcpChannel> tcpChannels;
    private ExecutorService threadPoolDisposeTcpData;
    
    private KKSimpleTimer checkTimeoutTimer;
    private ServerBootstrap bootstrap;
    private int localPort;
    
    public TcpServer(int localPort) {
        this.localPort = localPort;
        this.tcpChannels = new ConcurrentHashMap<Integer, TcpChannel>();
        this.threadPoolDisposeTcpData = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
    
    public void start() {
        ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory(){
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("tcpDecoder", new TcpDataDecoder());
                pipeline.addLast("tcpServerHandler", new TcpServerHandler(TcpServer.this));
                return pipeline;
            }
        });
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.bindAsync(new InetSocketAddress(localPort)).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("tcp server started, listening on Port:{}", localPort);      
                } else {
                    log.info("tcp server bind port[{}] failed", localPort);
                }
            }
        });
        
        startCheckTimeoutTimer();
    }
    
    public void stop() {
        stopCheckTimeoutTimer();
        clearTcpChannels();
        bootstrap.releaseExternalResources();
    }
    
    /**
     * start timer to check all connected channels timeout 
     */
    private void startCheckTimeoutTimer() {
        this.checkTimeoutTimer = new KKSimpleTimer("superior tcp server timeout checker", new Runnable() {
            final static long TIME_OUT = 1000 * 60 * 3;
            
            @Override
            public void run() {
                if (tcpChannels.size() > 0) {
                    Iterator<Entry<Integer, TcpChannel>> iterator = tcpChannels.entrySet().iterator();
                    
                    Entry<Integer, TcpChannel> entry = null;
                    TcpChannel tcpChannel = null; 
                    while (iterator.hasNext()) {
                        entry = iterator.next();
                        tcpChannel = entry.getValue();
                        if (System.currentTimeMillis() - tcpChannel.getLastRecvDataTime() >= TIME_OUT) {
                            tcpChannel.close();
                            tcpChannels.remove(entry.getKey());
                        }
                    }
                }
            }
        }, 60, 60);
        this.checkTimeoutTimer.start();
    }
    
    private void stopCheckTimeoutTimer() {
        if (this.checkTimeoutTimer != null) {
            this.checkTimeoutTimer.stop();
            this.checkTimeoutTimer = null;
        }
    }

    /**
     * clear all connected tcp channels from map
     */
    private void clearTcpChannels() {
        if (tcpChannels.size() > 0) {
            Iterator<Entry<Integer, TcpChannel>> iterator = tcpChannels.entrySet().iterator();
            TcpChannel tcpChannel = null; 
            while (iterator.hasNext()) {
                tcpChannel = iterator.next().getValue();
                tcpChannel.close();
            }
            tcpChannels.clear();
        }
    }
    
    /**
     * add new connected tcp channel
     * @param channel new connected channel
     * @return new tcp channel
     */
    protected TcpChannel addTcpChannel(Channel channel) {
        TcpChannel tcpChannel = new TcpChannel(channel, this);
        this.tcpChannels.put(channel.getId(), tcpChannel);
        return tcpChannel;
    }
    
    /**
     * get tcp channel via channelId({@link org.jboss.netty.channel.Channel.getId})
     * @param channelId
     * @return
     */
    public TcpChannel getTcpChannel(Integer channelId) {
        return tcpChannels.get(channelId);
    }
    
    protected void removeTcpChannel(Integer channelId) {
        TcpChannel tcpChannel = tcpChannels.remove(channelId);
        if (tcpChannel != null) {
            tcpChannel.close();
            BaseInfo.getBaseInfo().initTerminalChannelId(tcpChannel.getTerminalId());
        }
    }
    
    /**
     * get current connected tcp channel count
     * @return
     */
    public int getClientsCount() {
        return this.tcpChannels.size();
    }
    
    @Override
    public void executeDisposeTask(TcpChannel tcpChannel) {
        this.threadPoolDisposeTcpData.execute(new ThreadDisposeTcpChannelData(tcpChannel));
    }
}
