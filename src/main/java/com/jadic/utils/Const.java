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
    public final static short TER_CHECK_CITY_CARD_TYPE  = 0x000B;//检测市民卡类型 是否记名卡
    public final static short TER_CLEAR_CASH_BOX		= 0x000D;//终端操作清空钱箱
    public final static short TER_ADD_CASH_BOX_AMOUNT	= 0x000E;//增加钱箱现金额

    public final static short SER_TY_RET                = 0x7001;//平台通用应答
    public final static short SER_LOGIN_RET             = 0x7003;//终端注册应答
    public final static short SER_GET_MAC2_RET          = 0x7005;//获取mac2应答
    public final static short SER_CHARGE_DETAIL_RET     = 0x7006;//上传充值交易数据应答，返回充值唯一编号
    public final static short SER_REFUND_RET            = 0x7007;//返回退款唯一记录编号
    public final static short SER_PREPAID_CARD_CHECK    = 0x7008;//充值卡校验应答
    public final static short SER_QUERY_ZHB_BALANCE     = 0x7009;//账户宝余额查询应答
    public final static short SER_MODIFY_ZHB_PASS       = 0x700A;//修改账户宝密码应答
    public final static short SER_CHECK_CITY_CARD_TYPE  = 0x700B;//检测市民卡类型应答 是否记名卡
    public final static short SER_TERM_STATUS_CHANGED   = 0x700C;//终端状态变化
    public final static short SER_ADD_CASH_BOX_AMOUNT	= 0x700E;//增加钱箱现金额应答
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
    
    //------------------------------------充值记录状态------------------------------------
    public final static byte CHARGE_DETAIL_STATUS_SUCC  = 0x01;//1:成功
    public final static byte CHARGE_DETAIL_STATUS_MIDD  = 0x02;//2:中间状态
    public final static byte CHARGE_DETAIL_STATUS_FAIL  = 0x03;//3:失败
    
    //--------------------------------------充值类型--------------------------------------
    public final static byte CHARGE_TYPE_CASH           = 0x00;//现金充值
    public final static byte CHARGE_TYPE_BANK_CARD      = 0x01;//银行卡充值
    public final static byte CHARGE_TYPE_PREPAID_CARD   = 0x02;//充值卡充值
    public final static byte CHARGE_TYPE_ZHB            = 0x03;//企福通充值/专有账户充值
    
    public final static String INITIAL_DATA_DIR			 = "data/initData";
    public final static String CHARGE_DETAIL_DIR_PARENT  = "data/chargeDetail";
    public final static String CHARGE_DETAIL_DIR         = CHARGE_DETAIL_DIR_PARENT + "/current";
    public final static String CHARGE_DETAIL_BAK_DIR     = CHARGE_DETAIL_DIR_PARENT + "/bak";
    public final static String CHARGE_DETAIL_FILE_SUFFIX = ".sup";

    //-------------------------------市民卡卡片种类-------------------------------
    public final static String S_CITY_CARD_TYPE_NAMED   = "0000";//记名卡
    public final static String S_CITY_CARD_TYPE_UNNAMED = "0001";//不记名卡
    public final static String S_CITY_CARD_TYPE_INVALID = "0002";//无效卡
    public final static String S_CITY_CARD_TYPE_JFCARD  = "0003";//金福卡
    
    public final static byte CITY_CARD_TYPE_UNKNOWN = (byte)0xFF;//未知卡
    public final static byte CITY_CARD_TYPE_NAMED   = 0x00;//记名卡
    public final static byte CITY_CARD_TYPE_UNNAMED = 0x01;//不记名卡
    public final static byte CITY_CARD_TYPE_INVALID = 0x02;//无效卡
    public final static byte CITY_CARD_TYPE_JFCARD  = 0x03;//金福卡
    //-------------------------------市民卡卡片种类-------------------------------

    //-----------------------------常州市民卡接口业务代码-----------------------------
    public final static String BIPCODE_GET_MAC2             = "0004";//获取MAC2
    public final static String BIPCODE_QUERY_ZHB_BALANCE    = "0007";//查询账户宝余额
    public final static String BIPCODE_CHECK_PREPAID_CARD   = "0011";//校验充值卡面额
    public final static String BIPCODE_MODIFY_ZHB_PASSWORD  = "0012";//修改账户宝密码
    public final static String BIPCODE_GET_CITY_CARD_TYPE   = "0015";//获取市民卡是否记名
    //-----------------------------常州市民卡接口业务代码-----------------------------

    //--------------------------------------市民卡接口调用XML字符串--------------------------------------
    //XML共用Head部分
    public final static String WS_XML_HEAD  = "<SVC>" +
                                                "<SVCHEAD>" +
                                                  "<ORIGDOMAIN>%s</ORIGDOMAIN>" +
                                                  "<HOMEDOMAIN>%s</HOMEDOMAIN>" +
                                                  "<BIPCODE>%s</BIPCODE>" +
                                                  "<ACTIONCODE>%s</ACTIONCODE>" +
                                                  "<TRANSIDO>%s</TRANSIDO>" +
                                                  "<PROCID>%s</PROCID>" +
                                                  "<PROCESSTIME>%s</PROCESSTIME>" +
                                                "</SVCHEAD>" +
                                                "<SVCCONT>";
    public final static String WS_XML_END  =    "</SVCCONT>" +
                                              "</SVC>";
    //圈存,获取mac2
    public final static String WS_XML_GET_MAC2   = WS_XML_HEAD +
                                                   "<CHARGEREQ>" +
                                                     "<TRADETYPECODE>%s</TRADETYPECODE>" +
                                                     "<CARDNO>%s</CARDNO>" +
                                                     "<PASSWORD>%s</PASSWORD>" +
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
                                                   WS_XML_END;
   
    //充值卡校验
    public final static String WS_XML_PREPAID_CARD_CHECK = WS_XML_HEAD +
                                                          "<CARDVERIFYREQ>" +
                                                            "<TRADETYPECODE>%s</TRADETYPECODE>" +
                                                            "<CARDNO>%s</CARDNO>" +
                                                            "<PASSWORD>%s</PASSWORD>" +
                                                            "<DEPTNO>%s</DEPTNO>" +
                                                            "<OPERNO>%s</OPERNO>" +
                                                          "</CARDVERIFYREQ>" +
                                                          WS_XML_END;

    //获取账户宝余额
    public final static String WS_XM_GET_ZHB_BALANCE = WS_XML_HEAD +
                                                      "<GROUPQUERYREQ>" +
                                                        "<TRADETYPECODE>%s</TRADETYPECODE>" +
                                                        "<CARDNO>%s</CARDNO>" +
                                                        "<TRADEMONEY>%s</TRADEMONEY>" +//TODO check if this field name is correct, maybe "password"
                                                        "<DEPTNO>%s</DEPTNO>" +
                                                        "<OPERNO>%s</OPERNO>" +
                                                      "</GROUPQUERYREQ>" +
                                                      WS_XML_END;

    //修改账户宝密码
    public final static String WS_XML_MODIFY_ZHB_PASS = WS_XML_HEAD +
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
                                                      WS_XML_END;

    //检查市民卡是否记名
    public final static String WS_XML_CHECK_CITY_CARD_TYPE = WS_XML_HEAD +
                                                            "<CUSTRECTYPEQUERYREQ>" +
                                                              "<CARDNO>%s</CARDNO>" +
                                                            "</CUSTRECTYPEQUERYREQ>" +
                                                             WS_XML_END;
    //--------------------------------------市民卡接口调用XML字符串--------------------------------------
    
    //--------------------------------------操作日志类型----------------------------------------
    public final static int LOG_TYPE_TERMINAL_ONLINE    = 1;//上线
    public final static int LOG_TYPE_TERMINAL_OFFLINE   = 2;//离线
    public final static int LOG_TYPE_WITHDRAW           = 3;//提款
    public final static int LOG_TYPE_OUT_OF_SERVICE     = 4;//人工暂停服务
    public final static int LOG_TYPE_CHARGE             = 5;//充值
    public final static int LOG_TYPE_REFUND             = 6;//退款
    public final static int LOG_TYPE_QUERY_ZHB_BALANCE  = 7;//查询账户宝余额
    public final static int LOG_TYPE_MODIFY_ZHB_PASS    = 8;//修改账户宝密码
    //--------------------------------------操作日志类型----------------------------------------
}
