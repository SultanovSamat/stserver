package com.jadic;

import com.jadic.tcp.server.TcpServer;
import com.jadic.utils.SysParams;

/**
 * @author 	Jadic
 * @created 2014-6-30
 */
public class STServer {
    
    private TcpServer tcpServer;
    private SysParams sysParams;
    
    public STServer() {
        sysParams = SysParams.getInstance();
        tcpServer = new TcpServer(sysParams.getLocalTcpPort());
        tcpServer.start();
    }
    
    public static void main(String[] args) {
        new STServer();
    }

}
