package com.jadic.cmd.req;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

import com.jadic.biz.bean.ModuleStatus;

/**
 * @author 	Jadic
 * @created 2014-7-4
 */
public class CmdModuleStatusReq extends AbstractCmdReq {
    
    private byte moduleCount;
    private List<ModuleStatus> msList;
    
    public CmdModuleStatusReq() {
        msList = new ArrayList<ModuleStatus>();
    }

    @Override
    protected void disposeCmdBody(ChannelBuffer channelBuffer) {
        this.moduleCount = channelBuffer.readByte();
        for (int i = 0; i < this.moduleCount; i++) {
            ModuleStatus moduleStatus = new ModuleStatus();
            moduleStatus.setModuleId(channelBuffer.readByte());
            moduleStatus.setModuleStatus(channelBuffer.readByte());
        }
    }

    public byte getModuleCount() {
        return moduleCount;
    }

    public List<ModuleStatus> getMsList() {
        return msList;
    }

}
