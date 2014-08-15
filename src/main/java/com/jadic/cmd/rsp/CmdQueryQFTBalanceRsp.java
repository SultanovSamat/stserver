package com.jadic.cmd.rsp;

import com.jadic.utils.Const;

/**
 * 企福通余额查询
 * @author 	Jadic
 * @created 2014-8-14
 */
public class CmdQueryQFTBalanceRsp extends CmdPrepaidCardCheckRsp {
    
    public CmdQueryQFTBalanceRsp() {
        setCmdFlagId(Const.SER_QUERY_QFT_BALANCE);
    }

}
