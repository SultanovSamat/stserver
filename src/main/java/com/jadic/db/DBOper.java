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

import com.jadic.biz.bean.LongIDBean;
import com.jadic.biz.bean.TerminalBean;
import com.jadic.cmd.req.CmdChargeDetailReq;
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
    
    public List<LongIDBean> queryTerminalIdsWithOnlineTime() {
        return queryForList(SQL.QUERY_TERMINALIDS_ONLINE, null, LongIDBean.class);
    }
    
    public boolean updateTerminalStatus(String sql, List<Object> params) {
        return executeUpdateSingle(sql, params) != -1;
    }
    
    public boolean updateTerminalOffline(long terminalId) {
        List<Object> params = new ArrayList<Object>();
        params.add(terminalId);
        return executeUpdateSingle(SQL.UPDATE_TERMINAL_OFFLINE, params) != -1;
    }
    
    public long addNewChargeDetail(CmdChargeDetailReq chargeDetail) {
        List<Object> params = new ArrayList<Object>();
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
        params.add(chargeDetail.getTransSNo());
        params.add(Long.parseLong(KKTool.byteArrayToHexStr(chargeDetail.getTerminalId())));
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

    public int addNewRefund(CmdRefundReq refund) {
        List<Object> params = new ArrayList<Object>();
        params.add(KKTool.byteArrayToHexStr(refund.getCityCardNo()));
        params.add(refund.getAmount());
        params.add(refund.getChargeType());
        try {
            return (int)executeInsertAndRetrieveId(SQL.ADD_REFUND, params);
        } catch (SQLException e) {
            logger.error("addNewRefund fail", e);
        }
        return -1;
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
