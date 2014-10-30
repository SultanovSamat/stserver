package com.jadic;

import com.jadic.biz.BaseInfo;
import com.jadic.biz.ICmdBizDisposer;
import com.jadic.biz.ThreadTerminalModuleStatus;
import com.jadic.biz.ThreadTerminalChargeDetail;
import com.jadic.cmd.req.CmdModuleStatusReq;
import com.jadic.cmd.req.CmdChargeDetailReq;
import com.jadic.db.DBOper;
import com.jadic.tcp.server.TcpServer;
import com.jadic.utils.SysParams;
import com.jadic.ws.WSUtil;

/**
 * @author Jadic
 * @created 2014-6-30
 */
public class STServer implements ICmdBizDisposer{

    private TcpServer tcpServer;
    private SysParams sysParams = SysParams.getInstance();
    private ThreadTerminalModuleStatus threadModuleStatus;
    private ThreadTerminalChargeDetail threadTransaction;
    
    public STServer() {
        loadBaseInfo();
        tcpServer = new TcpServer(sysParams.getLocalTcpPort(), this);
        threadModuleStatus = new ThreadTerminalModuleStatus();
        threadTransaction = new ThreadTerminalChargeDetail();
        WSUtil.getWsUtil();
    }
    
    public void start() {
        tcpServer.start();
        threadModuleStatus.start();
        threadTransaction.start();
    }
    
    private void loadBaseInfo() {
        BaseInfo.getBaseInfo().updateBaseInfo(DBOper.getDBOper().queryTerminals());
    }

    public static void main(String[] args) {
        STServer stServer = new STServer();
        stServer.start();
    }

	@Override
	public void disposeCmdModuleStatus(CmdModuleStatusReq moduleStatus) {
		this.threadModuleStatus.addSingleToQueue(moduleStatus);
	}

	@Override
	public void disposeCmdChargeDetail(CmdChargeDetailReq transaction) {
		this.threadTransaction.addSingleToQueue(transaction);
	}
}
