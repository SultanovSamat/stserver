package com.jadic.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.biz.bean.TerminalBean;
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

}
