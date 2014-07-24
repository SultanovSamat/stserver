package com.jadic.biz;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jadic.biz.bean.TerminalBean;

/**
 * @author 	Jadic
 * @created 2014-7-24
 */
public final class BaseInfo {
    
    //只增不减
    private Map<Long, TerminalBean> terminals;
    
    private final static BaseInfo baseInfo = new BaseInfo(); 
    
    public static BaseInfo getBaseInfo() {
        return baseInfo;
    }
    
    private BaseInfo() {
        terminals = new ConcurrentHashMap<Long, TerminalBean>();
    }
    
    public void updateBaseInfo(List<TerminalBean> terminalList) {
        TerminalBean oldTerminal = null;
        for (TerminalBean terminal : terminalList) {
            oldTerminal = terminals.get(terminal.getId());
            if (oldTerminal != null) {
                if (oldTerminal.getEnabled() != terminal.getEnabled()) {
                    oldTerminal.setEnabled(terminal.getEnabled());
                    noticeTerminalEnabled(terminal);//if enabled status changed, notice terminal
                }
            } else {
                terminals.put(terminal.getId(), terminal);
            }
        }
    }
    
    /**
     * notice terminal enabled status
     * @param terminal
     */
    private void noticeTerminalEnabled(TerminalBean terminal) {
        //TODO
    }

    public TerminalBean getTerminal(long terminalId) {
        return terminals.get(terminalId);
    }
    
    public void initTerminalChannelId(long terminalId) {
        TerminalBean terminal = terminals.get(terminalId);
        if (terminal != null) {
            terminal.setChannelId(-1);
        }
    }
}
