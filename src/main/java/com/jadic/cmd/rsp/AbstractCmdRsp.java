package com.jadic.cmd.rsp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.cmd.AbstractCmd;
import com.jadic.cmd.req.AbstractCmdReq;

/**
 * @author 	Jadic
 * @created 2014-7-2
 */
public abstract class AbstractCmdRsp extends AbstractCmd {
    
    private static short cmdSNo = 0;
    
    public AbstractCmdRsp() {
    	setClientType((byte)0);
    	setCmdFlag();
    }
    
    protected abstract void setCmdFlag() ;

    @Override
    protected short getNextCmdSNo() {
        if (cmdSNo >= Short.MAX_VALUE || cmdSNo < 0) {
            cmdSNo = 0;
        }
        return cmdSNo ++;
    }

    @Override
    protected boolean disposeCmdBody(ChannelBuffer channelBuffer) {
        return true;
    }
    
    public void setCmdCommonField(AbstractCmdReq cmdReq) {
        this.setClientType(cmdReq.getClientType());
        this.setTerminalId(cmdReq.getTerminalId());
        this.setCmdSNoResp(cmdReq.getCmdSNo());
    }
}
