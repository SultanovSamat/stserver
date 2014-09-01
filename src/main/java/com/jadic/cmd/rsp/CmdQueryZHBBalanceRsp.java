package com.jadic.cmd.rsp;

import com.jadic.utils.Const;

/**
 * 账户宝余额查询
 * @author 	Jadic
 * @created 2014-8-14
 */
public class CmdQueryZHBBalanceRsp extends CmdPrepaidCardCheckRsp {
    
    public CmdQueryZHBBalanceRsp() {
        setCmdFlagId(Const.SER_QUERY_ZHB_BALANCE);
    }

}
