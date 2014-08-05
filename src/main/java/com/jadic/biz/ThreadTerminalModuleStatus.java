package com.jadic.biz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jadic.biz.bean.ModuleStatus;
import com.jadic.cmd.req.CmdModuleStatusReq;
import com.jadic.db.DBOper;
import com.jadic.utils.KKTool;

/**
 * @author 	Jadic
 * @created 2014-8-5
 */
public class ThreadTerminalModuleStatus extends AbstractThreadDisposeDataFromQueue<CmdModuleStatusReq> {
    
    private DBOper dbOper;
    
    private Map<Long, String> terminalStatus;//用来更新设备在线状态
    
    private final long OFFLINE_TIME = 5 * 60 * 1000;
    
    public ThreadTerminalModuleStatus() {
        dbOper = DBOper.getDBOper();
        terminalStatus = new HashMap<Long, String>();
    }

    @Override
    public void run() {
        CmdModuleStatusReq cmdReq = null;
        while (!isInterrupted()) {
            while ((cmdReq = getQueuePollData()) != null) {
                disposeCmd(cmdReq);
            }
            
            checkTerminalOnlineStatus();
            waitNewData();
        }
    }
    
    private void disposeCmd(CmdModuleStatusReq cmdReq) {
        long terminalId = Long.parseLong(KKTool.byteArrayToHexStr(cmdReq.getTerminalId()));
        String oldValue = terminalStatus.put(terminalId, "1_" + System.currentTimeMillis());
        StringBuilder sqlBuilder = new StringBuilder();
        List<ModuleStatus>msList = cmdReq.getMsList();
        List<Object> params = new ArrayList<Object>();
        if (oldValue == null) {//新增   
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
        } else {
            sqlBuilder.append("update tab_terminal_status ");
            sqlBuilder.append("set onlinestatus=1, lastonlinetime=SYSDATE(), ");
            for (ModuleStatus ms : msList) {
                sqlBuilder.append(", m").append(ms.getModuleId()).append("status=?");
            }            
            sqlBuilder.append(" where terminalid=? ");
            for (ModuleStatus ms : msList) {
                params.add(ms.getModuleStatus());
            }
            params.add(terminalId);
            dbOper.updateTerminalStatus(sqlBuilder.toString(), params);
        }
    }
    
    private void checkTerminalOnlineStatus() {
        Iterator<Entry<Long, String>>iterator = terminalStatus.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Long, String> entry = iterator.next();
            long terminalId = entry.getKey();
            String value = entry.getValue();
            if (value.startsWith("1")) {//只针对在线的
                long time = Long.parseLong(value.substring(2));
                if (System.currentTimeMillis() - time >= OFFLINE_TIME) {
                    
                }
            }
        }
    }

}
