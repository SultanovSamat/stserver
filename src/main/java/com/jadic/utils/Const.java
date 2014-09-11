package com.jadic.utils;

public final class Const {

    public final static String EMPTY_STR = "";
    public final static String STR_UNKNOWN = "未知";
    
    /*头标识(1) + 数据头(11) + 数据体(0) + CRC校验码(1) + 尾标识(1)*/
    public final static int CMD_MIN_SIZE = 14;//命令最小长度

    public final static byte CMD_HEAD_FLAG = 0x7E;
    public final static byte CMD_END_FLAG  = 0x7E;
    
    //-----------------------------通讯命令字定义-------------------------
    public final static short TER_TY_RET                = 0x0001;//终端通用应答
    public final static short TER_HEARTBEAT             = 0x0002;//终端心跳
    public final static short TER_LOGIN                 = 0x0003;//终端注册
    public final static short TER_MODULE_STATUS         = 0x0004;//终端模块状态汇报
    public final static short TER_GET_MAC2              = 0x0005;//获取mac2
    public final static short TER_CHARGE_DETAIL         = 0x0006;//上传充值交易数据
    public final static short TER_REFUND                = 0x0007;//退款记录登记
    public final static short TER_PREPAID_CARD_CHECK    = 0x0008;//充值卡校验
    public final static short TER_QUERY_ZHB_BALANCE     = 0x0009;//账户宝余额查询
    public final static short TER_MODIFY_ZHB_PASS       = 0x000A;//修改账户宝密码
    

    public final static short SER_TY_RET                = 0x7001;//平台通用应答
    public final static short SER_LOGIN_RET             = 0x7003;//终端注册应答
    public final static short SER_GET_MAC2_RET          = 0x7005;//获取mac2应答
    public final static short SER_CHARGE_DETAIL_RET     = 0x7006;//上传充值交易数据应答，返回充值唯一编号
    public final static short SER_REFUND_RET            = 0x7007;//返回退款唯一记录编号
    public final static short SER_PREPAID_CARD_CHECK    = 0x7008;//充值卡校验应答
    public final static short SER_QUERY_ZHB_BALANCE     = 0x7009;//账户宝余额查询应答
    public final static short SER_MODIFY_ZHB_PASS       = 0x700A;//修改账户宝密码应答
    //-----------------------------通讯命令字定义-------------------------
    
    public final static byte TY_RET_OK                  = 0x00;//0：成功/确认  
    public final static byte TY_RET_FAIL                = 0x00;//1：失败  
    public final static byte TY_RET_NO_LOGIN            = 0x00;//2：未注册 
    public final static byte TY_RET_NOT_SUPPORTED       = 0x00;//3：不支持
    
    public final static byte LOGIN_RET_OK               = 0x00;//0:成功 
    public final static byte LOGIN_RET_LOGINED          = 0x01;//1:终端已被注册 
    public final static byte LOGIN_RET_TER_NOT_EXISTED  = 0x02;//2:终端不存在 
    public final static byte LOGIN_RET_TER_NOT_USE      = 0x03;//3:终端被停用 
    public final static byte LOGIN_RET_ID_INVALID       = 0x04;//4:身份不合法
    
    public final static String CHARGE_DETAIL_DIR_PARENT  = "data/chargeDetail";
    public final static String CHARGE_DETAIL_DIR         = CHARGE_DETAIL_DIR_PARENT + "/current";
    public final static String CHARGE_DETAIL_BAK_DIR     = CHARGE_DETAIL_DIR_PARENT + "/bak";
    public final static String CHARGE_DETAIL_FILE_SUFFIX = ".sup";
    
  //--------------------------------------市民卡接口调用XML字符串--------------------------------------
    //圈存,获取mac2
    public final static String WS_XML_GET_MAC2 = "<SVC>" +
                                                     "<SVCHEAD>" +
                                                       "<ORIGDOMAIN>%s</ORIGDOMAIN>" +
                                                       "<HOMEDOMAIN>%s</HOMEDOMAIN>" +
                                                       "<BIPCODE>%s</BIPCODE>" +
                                                       "<ACTIONCODE>%s</ACTIONCODE>" +
                                                       "<TRANSIDO>%s</TRANSIDO>" +
                                                       "<PROCID>%s</PROCID>" +
                                                       "<PROCESSTIME>%s</PROCESSTIME>" +
                                                     "</SVCHEAD>" +
                                                     "<SVCCONT>" +
                                                       "<CHARGEREQ>" +
                                                         "<TRADETYPECODE>%s</TRADETYPECODE>" +
                                                         "<CARDNO>%s</CARDNO>" +
                                                         "<TERMNO>%s</TERMNO>" +
                                                         "<ASN>%s</ASN>" +
                                                         "<RNDNUMBER2>%s</RNDNUMBER2>" +
                                                         "<CARDTRADENO>%s</CARDTRADENO>" +
                                                         "<CARDOLDBAL>%s</CARDOLDBAL>" +
                                                         "<TRADEMONEY>%s</TRADEMONEY>" +
                                                         "<TRADETYPE>%s</TRADETYPE>" +
                                                         "<KEYVERSION>%s</KEYVERSION>" +
                                                         "<ARITHINDEX>%s</ARITHINDEX>" +
                                                         "<MAC1>%s</MAC1>" +
                                                         "<DEPTNO>%s</DEPTNO>" +
                                                         "<OPERNO>%s</OPERNO>" +
                                                         "<HOSTDATE>%s</HOSTDATE>" +
                                                         "<HOSTTIME>%s</HOSTTIME>" +
                                                       "</CHARGEREQ>" +
                                                     "</SVCCONT>" +
                                                   "</SVC>";
   
    //充值卡校验
    public final static String WS_XML_PREPAID_CARD_CHECK = "<SVC>" +
                                                                "<SVCHEAD>" +
                                                                  "<ORIGDOMAIN>%s</ORIGDOMAIN>" +
                                                                  "<HOMEDOMAIN>%s</HOMEDOMAIN>" +
                                                                  "<BIPCODE>%s</BIPCODE>" +
                                                                  "<ACTIONCODE>%s</ACTIONCODE>" +
                                                                  "<TRANSIDO>%s</TRANSIDO>" +
                                                                  "<PROCID>%s</PROCID>" +
                                                                  "<PROCESSTIME>%s</PROCESSTIME>" +
                                                                "</SVCHEAD>" +
                                                                "<SVCCONT>" +
                                                                  "<CARDVERIFYREQ>" +
                                                                    "<TRADETYPECODE>%s</TRADETYPECODE>" +
                                                                    "<CARDNO>%s</CARDNO>" +
                                                                    "<PASSWORD>%s</PASSWORD>" +
                                                                    "<DEPTNO>%s</DEPTNO>" +
                                                                    "<OPERNO>%s</OPERNO>" +
                                                                  "</CARDVERIFYREQ>" +
                                                                "</SVCCONT>" +
                                                              "</SVC>";

    //获取账户宝余额
    public final static String WS_XM_GET_ZHB_BALANCE = "<SVC>" +
                                                            "<SVCHEAD>" +
                                                              "<ORIGDOMAIN>%s</ORIGDOMAIN" +
                                                              "<HOMEDOMAIN>%s</HOMEDOMAIN>" +
                                                              "<BIPCODE>%s</BIPCODE>" +
                                                              "<ACTIONCODE>%s</ACTIONCODE>" +
                                                              "<TRANSIDO>%s</TRANSIDO>" +
                                                              "<PROCID>%s</PROCID>" +
                                                              "<PROCESSTIME>%s</PROCESSTIME>" +
                                                            "</SVCHEAD>" +
                                                            "<SVCCONT>" +
                                                              "<GROUPQUERYREQ>" +
                                                                "<TRADETYPECODE>%s</TRADETYPECODE>" +
                                                                "<CARDNO>%s</CARDNO>" +
                                                                "<TRADEMONEY>%s</TRADEMONEY>" +//TODO check if this field name is correct, maybe "password"
                                                                "<DEPTNO>%s</DEPTNO>" +
                                                                "<OPERNO>%s</OPERNO>" +
                                                              "</GROUPQUERYREQ>" +
                                                            "</SVCCONT>" +
                                                          "</SVC>"; 

    //修改账户宝密码
    public final static String WS_XML_MODIFY_ZHB_PASS = "<SVC>" +
                                                            "<SVCHEAD>" +
                                                              "<ORIGDOMAIN>%s</ORIGDOMAIN>" +
                                                              "<HOMEDOMAIN>%s</HOMEDOMAIN>" +
                                                              "<BIPCODE>%s</BIPCODE>" +
                                                              "<ACTIONCODE>%s</ACTIONCODE>" +
                                                              "<TRANSIDO>%s</TRANSIDO>" +
                                                              "<PROCID>%s</PROCID>" +
                                                              "<PROCESSTIME>%s</PROCESSTIME>" +
                                                            "</SVCHEAD>" +
                                                            "<SVCCONT>" +
                                                              "<ACCCHANGEPWDREQ>" +
                                                                "<TRADETYPECODE>%s</TRADETYPECODE>" +
                                                                "<CARDNO>%s</CARDNO>" +
                                                                "<OLDPASSWORD>%s</OLDPASSWORD>" +
                                                                "<NEWPASSWORD>%s</NEWPASSWORD>" +
                                                                "<TERMNO>%s</TERMNO><ASN>%s</ASN>" +
                                                                "<RNDNUMBER2>%s</RNDNUMBER2>" +
                                                                "<CARDTRADENO>%s</CARDTRADENO>" +
                                                                "<CARDOLDBAL>%s</CARDOLDBAL>" +
                                                                "<TRADEMONEY>%s</TRADEMONEY>" +
                                                                "<TRADETYPE>%s</TRADETYPE>" +
                                                                "<KEYVERSION>%s</KEYVERSION>" +
                                                                "<ARITHINDEX>%s</ARITHINDEX>" +
                                                                "<MAC1>%s</MAC1>" +
                                                                "<HOSTDATE>%s</HOSTDATE>" +
                                                                "<HOSTTIME>%s</HOSTTIME>" +
                                                                "<DEPTNO>%s</DEPTNO>" +
                                                                "<OPERNO>%s</OPERNO>" +
                                                              "</ACCCHANGEPWDREQ>" +
                                                            "</SVCCONT>" +
                                                          "</SVC>";
  //--------------------------------------市民卡接口调用XML字符串--------------------------------------
}
