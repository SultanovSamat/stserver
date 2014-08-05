package com.jadic.biz;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.biz.bean.TerminalBean;
import com.jadic.db.DBOper;

/**
 * @author 	Jadic
 * @created 2014-7-23
 */
public class ThreadLoadBaseinfo implements Runnable {
    
    private final static Logger log = LoggerFactory.getLogger(ThreadLoadBaseinfo.class);

    @Override
    public void run() {
        List<TerminalBean> terminalList = DBOper.getDBOper().queryTerminals();
        BaseInfo.getBaseInfo().updateBaseInfo(terminalList);
        log.info("load {} terminals", terminalList.size());
    }

}
