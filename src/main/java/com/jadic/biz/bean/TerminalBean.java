package com.jadic.biz.bean;

/**
 * @author 	Jadic
 * @created 2014-7-18
 */
public class TerminalBean {

    private long id;
    private byte enabled;//启用状态 0：启用 1：停用
    private int channelId;//终端关联的TCP Channel Id

    @Override
    public String toString() {
        return "id:" + id + ",enabeld:" + (enabled == 0);
    }

    public byte getEnabled() {
        return enabled;
    }

    public void setEnabled(byte enabled) {
        this.enabled = enabled;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
