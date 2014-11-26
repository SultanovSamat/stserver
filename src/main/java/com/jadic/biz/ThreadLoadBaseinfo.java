package com.jadic.biz;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.IMainServer;
import com.jadic.biz.bean.TerminalBean;
import com.jadic.cmd.rsp.CmdTerminalEnableStatusChanged;
import com.jadic.db.DBOper;
import com.jadic.tcp.server.TcpChannel;

/**
 * @author 	Jadic
 * @created 2014-7-23
 */
public class ThreadLoadBaseinfo implements Runnable {
    
    private final static Logger log = LoggerFactory.getLogger(ThreadLoadBaseinfo.class);
    private IMainServer mainServer;
    
    public ThreadLoadBaseinfo(IMainServer mainServer) {
    	this.mainServer = mainServer;
    }

    @Override
    public void run() {
        List<TerminalBean> terminalList = DBOper.getDBOper().queryTerminals();
        if (terminalList != null) {
        	for (TerminalBean terminal : terminalList) {
        		boolean isEnabledStatusChanged = BaseInfo.getBaseInfo().updateBaseInfo(terminal);
        		if (isEnabledStatusChanged) {
	        		noticeTerminalEnabled(terminal);//if enabled status changed, notice terminal
        		}
        	}
        	log.info("load {} terminals", terminalList.size());
        }
    }
    
    /**
     * notice terminal enabled status
     * @param terminal
     */
    private void noticeTerminalEnabled(TerminalBean terminal) {
    	if (terminal != null && this.mainServer != null) {
    		TcpChannel tcpChannel = this.mainServer.getTcpChannelByTerminalId((int)terminal.getId());
    		if (tcpChannel != null) {
    			CmdTerminalEnableStatusChanged cmd = new CmdTerminalEnableStatusChanged();
    			cmd.setTerminalId((int)terminal.getId());
    			cmd.setNewStatus(terminal.getEnabled());
    			tcpChannel.sendData(cmd.getSendBuffer());
    		}
    		log.info("terminal[{}] enabled status changed, new status[{}]", terminal.getId(), terminal.getEnabled());
    	}
    }

}
