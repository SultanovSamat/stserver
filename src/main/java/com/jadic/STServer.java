package com.jadic;

import com.jadic.biz.BaseInfo;
import com.jadic.biz.ICmdBizDisposer;
import com.jadic.biz.ThreadLoadBaseinfo;
import com.jadic.biz.ThreadTerminalChargeDetail;
import com.jadic.biz.ThreadTerminalModuleStatus;
import com.jadic.biz.ThreadUploadPosDealData;
import com.jadic.cmd.req.CmdChargeDetailReq;
import com.jadic.cmd.req.CmdModuleStatusReq;
import com.jadic.db.DBOper;
import com.jadic.tcp.server.TcpChannel;
import com.jadic.tcp.server.TcpServer;
import com.jadic.utils.KKSimpleTimer;
import com.jadic.utils.SysParams;
import com.jadic.ws.WSUtil;

/**
 * @author Jadic
 * @created 2014-6-30
 */
public class STServer implements ICmdBizDisposer, IMainServer{

    private TcpServer tcpServer;
    private SysParams sysParams = SysParams.getInstance();
    private ThreadTerminalModuleStatus threadModuleStatus;
    private ThreadTerminalChargeDetail threadTransaction;
    private ThreadUploadPosDealData threadUploadPosDealData;
    private KKSimpleTimer loadBaseInfoTimer;
    
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
        loadBaseInfoTimer = new KKSimpleTimer(new ThreadLoadBaseinfo(this), 60, 60);
        loadBaseInfoTimer.start();
        threadUploadPosDealData = new ThreadUploadPosDealData();
        threadUploadPosDealData.start();
    }
    
    private void loadBaseInfo() {
        BaseInfo.getBaseInfo().updateBaseInfo(DBOper.getDBOper().queryTerminals());
    }
    
    @Override
    public TcpChannel getTcpChannelByTerminalId(int terminalId) {
        return tcpServer.getTcpChannelByTerminalId(terminalId);
    }
    
    @Override
    public TcpChannel getTcpChannelByTChannelId(int channelId) {
        return tcpServer.getTcpChannel(channelId);
    }
    
    @Override
    public void disposeCmdModuleStatus(CmdModuleStatusReq moduleStatus) {
        this.threadModuleStatus.addSingleToQueue(moduleStatus);
    }
    
    @Override
    public void disposeCmdChargeDetail(CmdChargeDetailReq transaction) {
        this.threadTransaction.addSingleToQueue(transaction);
    }
    
    public static void main(String[] args) {
        STServer stServer = new STServer();
        stServer.start();
    }
}
