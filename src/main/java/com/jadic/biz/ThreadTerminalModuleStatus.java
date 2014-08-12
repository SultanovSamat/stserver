package com.jadic.biz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.biz.bean.IDBean;
import com.jadic.biz.bean.ModuleStatus;
import com.jadic.cmd.req.CmdModuleStatusReq;
import com.jadic.db.DBOper;

/**
 * 实时更新终端模块状态
 * @author 	Jadic
 * @created 2014-8-5
 */
public class ThreadTerminalModuleStatus extends AbstractThreadDisposeDataFromQueue<CmdModuleStatusReq> {
    
    private final static Logger log = LoggerFactory.getLogger(ThreadTerminalModuleStatus.class);
    
    private DBOper dbOper;
    
    private Set<Integer> terminalIdSet;//有终端模块状态数据的终端ID集合
    
    public ThreadTerminalModuleStatus() {
        dbOper = DBOper.getDBOper();
        terminalIdSet = new HashSet<Integer>();
    }

    @Override
    public void run() {
        initTerminalIdSet();
        CmdModuleStatusReq cmdReq = null;
        while (!isInterrupted()) {
            while ((cmdReq = getQueuePollData()) != null) {
                disposeCmd(cmdReq);
            }
            waitNewData();
        }
    }
    
    /**
     * 初始从数据库中加载已经上过线的终端ID列表
     */
    private void initTerminalIdSet() {
        List<IDBean> idList = dbOper.queryTerminalIdsWithOnlineTime();
        for (IDBean idBean : idList) {
            terminalIdSet.add(idBean.getId());
        }
        log.info("terminal-with-online-time count:{}", terminalIdSet.size());
    }
    
    private void disposeCmd(CmdModuleStatusReq cmdReq) {
        int terminalId = cmdReq.getTerminalId();
        StringBuilder sqlBuilder = new StringBuilder();
        List<ModuleStatus>msList = cmdReq.getMsList();
        List<Object> params = new ArrayList<Object>();
        if (terminalIdSet.add(terminalId)) {//新增   
            sqlBuilder.append("insert into tab_terminal_status ");
            sqlBuilder.append("(terminalId, onlinestatus, lastonlinetime");
            for (ModuleStatus ms : msList) {
                sqlBuilder.append(", m").append(ms.getModuleId()).append("status");
            }            
            sqlBuilder.append(") ");
            sqlBuilder.append("values (?, 1, SYSDATE()");
            for (int i = 0; i < msList.size(); i ++) {
                sqlBuilder.append(", ?");
            }
            sqlBuilder.append(")");
            
            params.add(terminalId);
            for (ModuleStatus ms : msList) {
                params.add(ms.getModuleStatus());
            }
            dbOper.updateTerminalStatus(sqlBuilder.toString(), params);
            log.info("terminal[{}] add new module status", terminalId);
        } else {
            sqlBuilder.append("update tab_terminal_status ");
            sqlBuilder.append("set onlinestatus=1, lastonlinetime=SYSDATE()");
            for (ModuleStatus ms : msList) {
                sqlBuilder.append(", m").append(ms.getModuleId()).append("status=?");
            }            
            sqlBuilder.append(" where terminalid=? ");
            for (ModuleStatus ms : msList) {
                params.add(ms.getModuleStatus());
            }
            params.add(terminalId);
            dbOper.updateTerminalStatus(sqlBuilder.toString(), params);
            log.info("terminal[{}]update module status", terminalId);
        }
    }

}
