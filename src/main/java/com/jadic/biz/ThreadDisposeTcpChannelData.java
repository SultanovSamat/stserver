package com.jadic.biz;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.tcp.server.TcpChannel;
import com.jadic.utils.Const;
import com.jadic.utils.KKTool;

/**
 * @author Jadic
 * @created 2014-4-14
 */
public class ThreadDisposeTcpChannelData implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ThreadDisposeTcpChannelData.class);
	private TcpChannel tcpChannel;

	final static int MAX_DISPOSE_COUNT = 200000;// 线程处理每个通道一次最多连续处理次数

	public ThreadDisposeTcpChannelData(TcpChannel tcpChannel) {
		this.tcpChannel = tcpChannel;
	}

	@Override
	public void run() {
		boolean isSucc = tcpChannel.checkAndSetDisposeFlag();
		if (isSucc) {
			ChannelBuffer buffer = null;
			int disposeCount = 0;
			while ((buffer = tcpChannel.getNextBuffer()) != null) {
				disposeBuffer(buffer);
				disposeCount++;

				//if tcp channel is closed, dispose all buffer data in the queue
				if (!tcpChannel.isClosed()) {
					if (disposeCount >= MAX_DISPOSE_COUNT) {
					    log.warn("{} dispose buffer over {} times, left size:{}", tcpChannel, MAX_DISPOSE_COUNT, tcpChannel.getBufferQueueSize());
						break;
					}
				} 
			}
			if (!tcpChannel.isClosed()) {
				tcpChannel.setDisposing(false);
			} else {
				tcpChannel = null;
			}
			/*if (disposeCount > 1) {
				KKLog.info(tcpChannel + " dispose buffer " + disposeCount + " times");
			}*/
		}
	}

	private void disposeBuffer(ChannelBuffer buffer) {
		if (buffer == null || buffer.readableBytes() < Const.CMD_MIN_SIZE) {
			return;
		}

		short cmdFlag = buffer.getShort(buffer.readerIndex() + 9);
		switch (cmdFlag) {
//		case Const.UP_CONNECT_REQ: // 主链路登录请求消息 主链路
//			dealCmdUpConnectReq(buffer);
//			break;
//		case Const.UP_DISCONNECE_REQ: // 主链路注销请求消息 主链路
//			dealCmdUpDisconnectReq(buffer);
//			break;
//		case Const.UP_LINKETEST_REQ: // 主链路连接保持请求消息 主链路
//			dealCmdUpLinkTestReq(buffer);
//			break;
//		case Const.UP_EXG_MSG: // 主链路动态信息交换消息 主链路
//		case Const.UP_PLAFORM_MSG: // 主链路平台间信息交互消息 主链路
//		case Const.UP_WARN_MSG: // 主链路报警信息交互消息 主链路
//		case Const.UP_CTRL_MSG: // 主链路车辆监管消息 主链路
//		case Const.UP_BASE_MSG: // 主链路静态信息交换消息 主链路
//			short subBizCmdFlag = buffer.getShort(buffer.readerIndex() + 9 + 2 + 4 + 3 + 1 + 4 + 22);
//			if (cmdFlag == Const.DOWN_PLATFORM_MSG) {
//				subBizCmdFlag = buffer.getShort(buffer.readerIndex() + 9 + 2 + 4 + 3 + 1 + 4);
//			}
//			dispose809SubBizData(subBizCmdFlag, buffer);
//			break;
		default:
			log.warn("Unknown command flag:{}", KKTool.byteArrayToHexStr(KKTool.short2BytesBigEndian(cmdFlag)));
			break;
		}
	}

}
