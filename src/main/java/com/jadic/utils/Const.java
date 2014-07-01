package com.jadic.utils;

public final class Const {

    public final static String EMPTY_STR = "";
    public final static String STR_UNKNOWN = "未知";
    
    /*头标识(1) + 数据头(11) + 数据体(0) + CRC校验码(2) + 尾标识(1)*/
    public final static int CMD_MIN_SIZE = 15;//命令最小长度

    public final static byte CMD_HEAD_FLAG = 0x7E;
    public final static byte CMD_END_FLAG  = 0x7E;
}
