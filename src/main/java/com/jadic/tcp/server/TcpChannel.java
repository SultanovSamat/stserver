package com.jadic.tcp.server;

import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * wrapped with {@link org.jboss.netty.channel.Channel}
 * @author 	Jadic
 * @created 2014-4-2
 */
public class TcpChannel {

    final static int MAX_BUF_SIZE = 100000;
	
    private final static Logger log = LoggerFactory.getLogger(TcpChannel.class);

    private ITcpChannelDisposer tcpDataDisposer;
	private Queue<ChannelBuffer> bufQueue;
	private Channel channel;
	
	private volatile boolean isClosed;
	private volatile boolean isDisposing;
	private Lock lock;
	private long lastRecvDataTime;
	
	private String tcpChannelDesc;
	private long terminalId = -1;
	private short terminalVer = -1;

	public TcpChannel(Channel channel, ITcpChannelDisposer tcpDataDisposer) {
	    this.tcpDataDisposer = tcpDataDisposer;
		this.bufQueue = new LinkedBlockingQueue<ChannelBuffer>(MAX_BUF_SIZE);
		this.channel = channel;
		lock = new ReentrantLock();
		this.lastRecvDataTime = System.currentTimeMillis();
		this.isClosed = false;
		this.isDisposing = false;
		this.generateTcpChannelDesc();
	}
	
	/**
	 * add recv data from client, and submit dispose task if succeed to add
	 * @param buffer
	 * @return true if succeed to add to queue
	 */
	public boolean addRecvDataAndDispose(ChannelBuffer buffer) {
		if (addRecvData(buffer)) {
			disposeBufferData();
			return true;
		}
		return false;
	}
	
	/**
	 * add recv data from client<br>
	 * if the queue is full, remove the head element
	 * @param buffer
	 * @return true if succeed to add to queue
	 */
	public boolean addRecvData(ChannelBuffer buffer) {
		if (isClosed) {
		    log.info("{}-- is closed, adding recv data is rejected", this);
			return false;
		}
		if (buffer == null) {
			return false;
		}
		
		this.lastRecvDataTime = System.currentTimeMillis();
		
		boolean isAdded = this.bufQueue.offer(buffer);
		if (!isAdded) {
			this.bufQueue.poll();
			isAdded = this.bufQueue.offer(buffer);
			log.warn("[{}] buffer queue is full(100K), the disposing thread seems to be slow", this);
		}
		
		return isAdded;		
	}
	
	/**
	 * dispose buffer data in the queue
	 */
	public void disposeBufferData() {
		if (!isDisposing) {//avoid disposing repeatedly
			this.tcpDataDisposer.executeDisposeTask(this);
		}
	}
	
	/**
	 * send data from the wrapped channel
	 * @param buffer
	 * @return
	 */
	public boolean sendData(ChannelBuffer buffer) {
		if (channel != null && channel.isConnected()) {
			channel.write(buffer);
			return true;
		}
		return false;
	}
	
	/**
	 * close the tcp channel<br>
	 * dispose all the buffer data if the queue is not empty
	 */
	public void close() {
		if (isClosed) {
			return ;
		}
		
		this.isClosed = true;
		if (getBufferQueueSize() > 0) {
		    disposeBufferData();//the buffer data in the queue may not be all disposed
		}
		this.channel.close();
	}
	
	/**
	 * @return the first buffer in the queue
	 */
	public ChannelBuffer getNextBuffer() {
		return this.bufQueue.poll();
	}
	
	public int getBufferQueueSize() {
		return this.bufQueue.size();
	}

	public boolean isDisposing() {
		return isDisposing;
	}

	public void setDisposing(boolean isDisposing) {
		this.isDisposing = isDisposing;
	}
	
	/**
	 * if disposeFlag is false, then set the flag true, and return true<br>
	 * otherwise return false
	 * @return
	 */
	public boolean checkAndSetDisposeFlag() {
		if (isDisposing) {
			return false;
		} else {
			lock.lock();
			try {
				if (!isDisposing) {
					isDisposing = true;
					return true;
				}
				return false;
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * @return true if the current wrapped channel is closed
	 */
	public boolean isClosed() {
		return isClosed;
	}
	
	public int getId() {
		return this.channel != null ? this.channel.getId() : -1;
	}
	
	public SocketAddress getSocektAddress() {
		return this.channel != null ? this.channel.getRemoteAddress() : null;
	}
	
	@Override
	public String toString() {
		return tcpChannelDesc;
	}
	
	private void generateTcpChannelDesc() {
	    StringBuilder sBuilder = new StringBuilder();
	    sBuilder.append("terminalId:").append(this.terminalId);
	    sBuilder.append(",id:").append(getId());
	    sBuilder.append(",address:").append(getSocektAddress());
	    this.tcpChannelDesc = sBuilder.toString();
	}

	public long getLastRecvDataTime() {
		return lastRecvDataTime;
	}

    public long getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(long terminalId) {
        if (this.terminalId != terminalId) {
            this.terminalId = terminalId;
            generateTcpChannelDesc();
        }
    }

    public short getTerminalVer() {
        return terminalVer;
    }

    public void setTerminalVer(short terminalVer) {
        this.terminalVer = terminalVer;
        if (this.terminalVer == -1) {
            this.terminalVer = 0;
        }
    }
    
    public boolean isLogined() {
        return this.terminalVer != -1;
    }

}
