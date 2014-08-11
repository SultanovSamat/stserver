/**
 * @author 	Jadic
 * @created 2014-2-21
 */
package com.jadic.db;

public final class SQL {

    private SQL() {
    }
    
    //查询终端信息
    public final static String QUERY_TERMINAL_INFO      = "select a.id, a.enabled, -1 as channelId " +
    		                                                "from tab_terminal a " +
    		                                                "where a.typeid=1";
    
    //上过线的终端设备ID
    public final static String QUERY_TERMINALIDS_ONLINE = "select terminalId as id " +
    		                                                "from tab_terminal_status";
    
    //更新终端离线
    public final static String UPDATE_TERMINAL_OFFLINE  = "update tab_terminal_status " +
    		                                                "set OnlineStatus = 2 " +
    		                                                "where terminalId = ? ";
    //新增充值明细
    public final static String ADD_CHARGE_DETAIL        = "insert into tab_charge_detail " +
    		                                                "(CityCardNo, CardType, BankCardNo, ChargeTime, ChargeType, " +
    		                                                " ChargeAmount, BalanceBeforeCharge, TAC, ASN, TSN, ChargeSNo, " +
    		                                                " TerminalId, PosId, SAMID, AgencyNo) " +
    		                                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    
    //增加退款明细
    public final static String ADD_REFUND               = "insert into tab_refund (CardNo, Amount, RefundTime, Status, InsertTime) " +
    		                                                "                values (?, ?, ?, 0, sysdate()) ";
    
}
