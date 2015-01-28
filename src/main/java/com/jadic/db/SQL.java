/**
 * @author 	Jadic
 * @created 2014-2-21
 */
package com.jadic.db;

public final class SQL {

    private SQL() {
    }
    
    //查询终端信息
    public final static String QUERY_TERMINAL_INFO      = "select a.id, a.enabled, -1 as channelId, CashBoxAmount as totalCashAmount " +
		                                                  "from tab_terminal a " +
		                                                  "left join tab_terminal_status b on b.terminalId = a.id " +
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
    		                                                "(Status, CityCardNo, CardType, BankCardNo, ChargeTime, ChargeType, " +
    		                                                " ChargeAmount, BalanceBeforeCharge, TAC, ASN, TSN, ChargeSNo, " +
    		                                                " TerminalId, PosId, SAMID, AgencyNo, SysTime) " +
    		                                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate()) ";
    
    //增加退款明细
    public final static String ADD_REFUND               = "insert into tab_refund " +
    		                                                "(CardNo, Amount, RefundTime, Status, InsertTime, TerminalID, ChargeType) " +
    		                                                "                values (?, ?, ?, 0, sysdate(), ?, ?) ";
    
    //有现金充值时累计现金金额
    public final static String ADD_CASH_AMOUNT		  = "update tab_terminal_status " +
    													"set CashBoxAmount = CashBoxAmount + ? " +
    													"where TerminalId = ? ";
    
    //强制设置现金累计总金额
    public final static String SET_CASH_AMOUNT		  = "update tab_terminal_status " +
    														"set CashBoxAmount = ? " +
    														"where TerminalId = ? ";
    
    //现金金额清零
    public final static String SET_CASH_AMOUNT_ZERO	  = "update tab_terminal_status " +
														    "set CashBoxAmount = 0 " +
														    "where TerminalId = ? ";
    
    //增加提款记录明细
    public final static String ADD_WITHDRAW_DETAIL      = "insert into tab_withdraw_detail " +
    		                                                "(TerminalId, WithdrawAmount, OperTime, LastOperTime, OperUserId) " +
    		                                                "values (?, ?, ?, ?, ?) ";
}
