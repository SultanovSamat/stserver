package com.jadic;

import com.jadic.biz.BaseInfo;
import com.jadic.db.DBOper;
import com.jadic.tcp.server.TcpServer;
import com.jadic.utils.SysParams;

/**
 * @author Jadic
 * @created 2014-6-30
 */
public class STServer {

    private TcpServer tcpServer;
    private SysParams sysParams = SysParams.getInstance();

    public STServer() {
        loadBaseInfo();
        tcpServer = new TcpServer(sysParams.getLocalTcpPort());
    }
    
    public void start() {
        tcpServer.start();
    }
    
    private void loadBaseInfo() {
        BaseInfo.getBaseInfo().updateBaseInfo(DBOper.getDBOper().queryTerminals());
    }

    public static void main(String[] args) {
        new STServer();
    }
}
