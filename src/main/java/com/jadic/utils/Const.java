package com.jadic.utils;

public final class Const {

    public final static String EMPTY_STR = "";
    public final static String STR_UNKNOWN = "未知";
    
    /*头标识(1) + 数据头(11) + 数据体(0) + CRC校验码(1) + 尾标识(1)*/
    public final static int CMD_MIN_SIZE = 14;//命令最小长度

    public final static byte CMD_HEAD_FLAG = 0x7E;
    public final static byte CMD_END_FLAG  = 0x7E;
    
    public final static short TER_TY_RET        = 0x0001;//终端通用应答
    public final static short SER_TY_RET        = 0x7001;//平台通用应答
    public final static short TER_HEARTBEAT     = 0x0002;//终端心跳
    public final static short TER_LOGIN         = 0x0003;//终端注册
    public final static short SER_LOGIN_RET     = 0x7003;//终端注册应答
    public final static short TER_MODULE_STATUS = 0x0004;//终端模块状态汇报
    
    public final static byte TY_RET_OK            = 0x00;//0：成功/确认  
    public final static byte TY_RET_FAIL          = 0x00;//1：失败  
    public final static byte TY_RET_INVALID       = 0x00;//2：消息有误 
    public final static byte TY_RET_NOT_SUPPORTED = 0x00;//3：不支持
    
    public final static byte LOGIN_RET_OK              = 0x00;//0:成功 
    public final static byte LOGIN_RET_LOGINED         = 0x01;//1:终端已被注册 
    public final static byte LOGIN_RET_TER_NOT_EXISTED = 0x02;//2:终端不存在 
    public final static byte LOGIN_RET_TER_NOT_USE     = 0x03;//3:终端被停用 
    public final static byte LOGIN_RET_ID_INVALID      = 0x04;//4:身份不合法
}
