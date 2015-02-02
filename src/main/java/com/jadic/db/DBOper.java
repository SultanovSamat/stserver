package com.jadic.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.biz.bean.DBSaveBean;
import com.jadic.biz.bean.IDBean;
import com.jadic.biz.bean.TerminalBean;
import com.jadic.cmd.req.CmdChargeDetailReq;
import com.jadic.cmd.req.CmdClearCashBox;
import com.jadic.cmd.req.CmdRefundReq;
import com.jadic.utils.KKTool;

/**
 * 数据库操作
 */
public final class DBOper extends DefaultDBImpl {

    private final static Logger logger = LoggerFactory.getLogger(DBOper.class);

    private final static DBOper dbOper = new DBOper();

    public synchronized static DBOper getDBOper() {
        return dbOper;
    }

    private DBOper() {
    }
    
    public List<TerminalBean> queryTerminals() {
        return queryForList(SQL.QUERY_TERMINAL_INFO, null, TerminalBean.class);
    }
    
    public List<IDBean> queryTerminalIdsWithOnlineTime() {
        return queryForList(SQL.QUERY_TERMINALIDS_ONLINE, null, IDBean.class);
    }
    
    /**
     * 更新终端状态（在线状态及各模块状态）
     * @param sql
     * @param params
     * @return
     */
    public boolean updateTerminalStatus(String sql, List<Object> params) {
        return executeUpdateSingle(sql, params) != -1;
    }
    
    /**
     * 更新终端离线
     * @param terminalId 终端编号
     * @return
     */
    public boolean updateTerminalOffline(long terminalId) {
        List<Object> params = new ArrayList<Object>();
        params.add(terminalId);
        return executeUpdateSingle(SQL.UPDATE_TERMINAL_OFFLINE, params) != -1;
    }
    
    /**
     * 增加充值明细
     * @param chargeDetail 充值明细命令
     * @return
     */
    public long addNewChargeDetail(CmdChargeDetailReq chargeDetail) {
        List<Object> params = new ArrayList<Object>();
        params.add(chargeDetail.getStatus());
        params.add(KKTool.byteArrayToHexStr(chargeDetail.getCityCardNo()));
        params.add(chargeDetail.getCardType());
        params.add(new String(chargeDetail.getBankCardNo()));
        byte[] dt = new byte[7];
        System.arraycopy(chargeDetail.getTransDate(), 0, dt, 0, 4);
        System.arraycopy(chargeDetail.getTransTime(), 0, dt, 4, 3);
        Date chargeTime = KKTool.getBCDDateTime(dt, 0);
        params.add(new Timestamp(chargeTime.getTime()));
        params.add(chargeDetail.getChargeType());
        params.add(chargeDetail.getTransAmount());
        params.add(chargeDetail.getBalanceBeforeTrans());
        params.add(KKTool.byteArrayToHexStr(chargeDetail.getTac()));
        params.add(KKTool.byteArrayToHexStr(chargeDetail.getAsn()));
        params.add(KKTool.byteArrayToHexStr(chargeDetail.getTsn()));
        params.add(Long.valueOf(KKTool.byteArrayToHexStr(chargeDetail.getTransSNo())));
        params.add(chargeDetail.getTerminalId());
        params.add("");
        params.add("");
        params.add("");
        try {
            return executeInsertAndRetrieveId(SQL.ADD_CHARGE_DETAIL, params);
        } catch (SQLException e) {
            logger.error("addNewChargeDetail fail", e);
        }
        return -1;
    }

    /**
     * 增加退款记录
     * @param refund 退款命令
     * @return
     */
    public int addNewRefund(CmdRefundReq refund) {
        List<Object> params = new ArrayList<Object>();
        params.add(KKTool.byteArrayToHexStr(refund.getCityCardNo()));
        params.add(refund.getAmount());
        Date refundTime = KKTool.getBCDDateTime(refund.getTime(), 0);
        params.add(new Timestamp(refundTime.getTime()));
        params.add(refund.getTerminalId());
        params.add(refund.getChargeType());
        try {
            return (int)executeInsertAndRetrieveId(SQL.ADD_REFUND, params);
        } catch (SQLException e) {
            logger.error("addNewRefund fail", e);
        }
        return -1;
    }
    
    /**
     * 累加钱箱现金金额
     * @param terminalId	终端编号
     * @param amountAdded	本次累加的金额 (单位:元)
     * @return
     */
    public boolean addCashBoxAmount(int terminalId, int amountAdded) {
    	List<Object> params = new ArrayList<Object>();
    	params.add(amountAdded);
    	params.add(terminalId);
    	return executeUpdateSingle(SQL.ADD_CASH_AMOUNT, params) != -1;
    }
    
    /**
     * 更新钱箱现金金额为0
     * @param terminalId	终端编号
     * @return
     */
    public boolean setCashBoxAmountZero(int terminalId) {
    	List<Object> params = new ArrayList<Object>();
    	params.add(terminalId);
    	return executeUpdateSingle(SQL.SET_CASH_AMOUNT_ZERO, params) != -1;
    }

    /**
     * 更新钱箱现金金额
     * @param terminalId	终端编号
     * @return
     */
    public boolean setCashBoxAmount(int terminalId, int totalAmount) {
        List<Object> params = new ArrayList<Object>();
        params.add(totalAmount);
        params.add(terminalId);
        return executeUpdateSingle(SQL.SET_CASH_AMOUNT, params) != -1;
    }
    
    /**
     * 增加提款操作明细
     * @param cmd
     * @return
     */
    public boolean addWithdrawDetail(CmdClearCashBox cmd) {
        List<Object> params = new ArrayList<Object>();
        params.add(cmd.getTerminalId());
        params.add(cmd.getCashAmount());
        byte[] time = cmd.getOperTime();
        int i = 0;
        
        Date operTime = KKTool.getBCDDateTime(time[i ++], time[i ++], time[i ++], time[i ++], time[i ++], time[i ++]);
        params.add(new Timestamp(operTime.getTime()));
        
        time = cmd.getLastOperTime();
        i = 0;
        operTime = KKTool.getBCDDateTime(time[i ++], time[i ++], time[i ++], time[i ++], time[i ++], time[i ++]);
        params.add(new Timestamp(operTime.getTime()));

        params.add(0);
        return executeUpdateSingle(SQL.ADD_WITHDRAW_DETAIL, params) != -1;
    }
    
    /**
     * 增加操作日志
     * @param terminalId    终端编号
     * @param logType   日志类型
     * @param logMemo   日志备注
     * @return true if succeed to add
     */
    public boolean addOperLog(int terminalId, int logType, String logMemo) {
        List<Object> params = new ArrayList<Object>();
        params.add(terminalId);
        params.add(logType);
        params.add(logMemo);
        return executeUpdateSingle(SQL.ADD_OPER_LOG, params) != -1;
    }
    
    public boolean saveDataBean(DBSaveBean dataBean) {
        return executeUpdateSingle(dataBean.getSql(), dataBean.getParams()) > 0;
    }
    
    public void test() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getMasterConnection();
            statement = connection.prepareStatement("select * from tab_users");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                logger.info("id:{}, name:{}, pass:{}, memo:{}", resultSet.getObject(1), resultSet.getObject(2),
                        resultSet.getObject(3), resultSet.getObject(4));
            }
        } catch (SQLException e) {
            logger.error("getCarInfoMaxRowscn异常", e);
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(resultSet, statement, connection);
        }
    }
    
    public static void main(String[] strings) {
    	DBOper.getDBOper().addNewRefund(new CmdRefundReq());
    }

}
