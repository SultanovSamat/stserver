package com.jadic.biz;

import com.jadic.biz.bean.DBSaveBean;
import com.jadic.cmd.req.CmdModuleStatusReq;
import com.jadic.cmd.req.CmdChargeDetailReq;

/**
 * 终端上上传的命令涉及到的一些特殊业务处理
 * @author Jadic
 *
 */
public interface ICmdBizDisposer {
	
	public void disposeCmdModuleStatus(CmdModuleStatusReq moduleStatus);
	public void disposeCmdChargeDetail(CmdChargeDetailReq chargeDetail);
	public void saveDBAsyn(DBSaveBean dataBean);

}
