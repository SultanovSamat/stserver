package com.jadic.db;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.utils.Const;
import com.jadic.utils.JDBCConfig;
import com.jadic.utils.KKTool;
import com.jadic.utils.SysParams;

/**
 * @author Jadic
 * @created 2014-7-18
 */
public class DefaultDBImpl {

    private final static Logger logger = LoggerFactory.getLogger(DefaultDBImpl.class);
    private DBConnPool masterPool;
    private List<JDBCConfig> jdbcList;

    public DefaultDBImpl() {
        jdbcList = SysParams.getInstance().getJdbcListCopy();
        if (jdbcList.size() <= 0) {
            logger.error("no jdbc config is set, please check");
            return;
        }
        
        getMasterPool();
    }
    
    private void getMasterPool() {
        if (masterPool != null) {
            return;
        }
        try {
            masterPool = DBConnPools.getDbConnPool(jdbcList.get(0));
        } catch (ClassNotFoundException e) {
            logger.error("DBOper create ClassNotFoundException", e);
        } catch (SQLException e) {
            logger.error("DBOper create SQLException", e);
        }        
    }

    protected <T> List<T> queryForList(String sql, Object[] paramsObj, Class<T> objClass) {
        List<T> list = new ArrayList<T>();
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            connection = getMasterConnection();
            if (connection == null) {
            	logger.warn("connection is null");
            	return list;
            }
            pstmt = connection.prepareStatement(sql);
            if (paramsObj != null) {
                for (int i = 0; i < paramsObj.length; i++) {
                    pstmt.setObject(i + 1, paramsObj[i]);
                }
            }
            Field[] fields = objClass.getDeclaredFields();
            // 执行查询
            rs = pstmt.executeQuery();
            while (rs.next()) {
                T t = objClass.newInstance();
                for (Field field : fields) {
                    try {
                        Object val = getRsVal(rs, field);
                        Method m = objClass.getMethod(getSetterMethod(field.getName()), field.getType());
                        if (val != null) {
                            m.invoke(t, val);
                        }
                    } catch (Exception e) {
                        logger.info("set field data err", e);
                    }
                }
                list.add(t);
            }
        } catch (SecurityException e) {
            logger.error("queryForList", e);
        } catch (IllegalArgumentException e) {
            logger.error("queryForList", e);
        } catch (SQLException e) {
            logger.error("queryForList", e);
        } catch (InstantiationException e) {
            logger.error("queryForList", e);
        } catch (IllegalAccessException e) {
            logger.error("queryForList", e);
        } catch (Exception e) {
            logger.error("queryForList", e);
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(rs, pstmt, connection);
        }
        return list;
    }

    private Object getRsVal(ResultSet rs, Field field) {
        Object val = null;
        String fieldType = field.getType().getName();
        String fieldName = field.getName();
        try {
            if (fieldType.equals(String.class.getName())) {
                val = rs.getString(fieldName);
            } else if (fieldType.equals(Byte.class.getName())) {
                val = rs.getByte(fieldName);
            } else if (fieldType.equals(byte.class.getName())) {
                val = rs.getByte(fieldName);
            } else if (fieldType.equals(Short.class.getName())) {
                val = rs.getShort(fieldName);
            } else if (fieldType.equals(short.class.getName())) {
                val = rs.getShort(fieldName);
            } else if (fieldType.equals(Integer.class.getName())) {
                val = rs.getInt(fieldName);
            } else if (fieldType.equals(int.class.getName())) {
                val = rs.getInt(fieldName);
            } else if (fieldType.equals(Long.class.getName())) {
                val = rs.getLong(fieldName);
            } else if (fieldType.equals(long.class.getName())) {
                val = rs.getLong(fieldName);
            } else if (fieldType.equals(BigDecimal.class.getName())) {
                val = rs.getBigDecimal(fieldName);
            } else if (fieldType.equals(Boolean.class.getName())) {
                val = rs.getBoolean(fieldName);
            } else if (fieldType.equals(boolean.class.getName())) {
                val = rs.getBoolean(fieldName);
            } else if (fieldType.equals(Date.class.getName())) {
                val = rs.getTimestamp(fieldName);
            } else if (fieldType.equals(java.sql.Date.class.getName())) {
                val = rs.getDate(fieldName);
            } else if (fieldType.equals(Double.class.getName())) {
                val = rs.getDouble(fieldName);
            } else if (fieldType.equals(double.class.getName())) {
                val = rs.getDouble(fieldName);
            } else if (fieldType.equals(Float.class.getName())) {
                val = rs.getFloat(fieldName);
            } else if (fieldType.equals(Time.class.getName())) {
                val = rs.getTime(fieldName);
            } else if (fieldType.equals(Timestamp.class.getName())) {
                val = rs.getTimestamp(fieldName);
            }
        } catch (SQLException e) {
            logger.error("getRsVal", e);
        }
        return val;
    }

    private String getSetterMethod(String fieldName) {
        if (!KKTool.isStrNullOrBlank(fieldName)) {
            return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
        return Const.EMPTY_STR;
    }

    protected Connection getMasterConnection() throws SQLException {
        getMasterPool();
        return masterPool != null ? masterPool.getConnection() : null;
    }

    public static void release() {
        logger.info("release db conn pool");
        DBConnPools.releasePools();
    }

    // **********************************************************数据库查询、保存操作********************************************************************

    /**
     * 获取个数
     * 
     * @param sql
     * @param paramsObj
     * @return
     */
    protected Object executeQueryForObj(String sql, Object[] paramsObj) {
        logger.info("executeQueryForCount sql:{} sql参数", sql, Arrays.toString(paramsObj));
        Object obj = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = getMasterConnection();
            if (connection == null) {
            	return obj;
            }
            statement = connection.prepareStatement(sql);
            if (paramsObj != null) {
                // 注入参数
                for (int i = 0; i < paramsObj.length; i++) {
                    statement.setObject(i + 1, paramsObj[i]);
                }
            }
            // 执行查询
            rs = statement.executeQuery();
            // 遍历查找的结果集
            while (rs.next()) {
                obj = rs.getObject(1);
            }
        } catch (SQLException e) {
            logger.info("executeQueryForObj", e);
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(rs, statement, connection);
        }
        return obj;
    }

    protected int executeUpdateSingle(String sql, List<Object> paramsObj) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        try {
            connection = getMasterConnection();
            if (connection == null) {
            	return -1;
            }
            pstmt = connection.prepareStatement(sql);

            if (paramsObj != null) {
                for (int i = 0; i < paramsObj.size(); i++) {
                    pstmt.setObject(i + 1, paramsObj.get(i));
                }
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("executeUpdateSingle err, sql:{}", sql, e);
        } finally {
            KKTool.closeStatementAndConnectionInSilence(pstmt, connection);
        }

        return -1;
    }

    protected int executeUpdateMulti(String sql, List<Object[]> paramsObjList) {
        if (paramsObjList == null) {
            return 0;
        }

        Connection connection = null;
        PreparedStatement pstmt = null;
        boolean autoCommit = true;
        try {
            connection = getMasterConnection();
            if (connection == null) {
            	return -1;
            }
            pstmt = connection.prepareStatement(sql);
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            for (Object[] paramsObj : paramsObjList) {
                if (paramsObj != null) {
                    for (int i = 0; i < paramsObj.length; i++) {
                        pstmt.setObject(i + 1, paramsObj[i]);
                    }
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            connection.commit();
            return 1;
        } catch (SQLException e) {
            logger.error("executeUpdateMulti err, sql:{}", sql, e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    logger.error("executeUpdate rollback err", e);
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException e) {
                    logger.error("restore autocommit err:" + KKTool.getExceptionTip(e));
                }
            }
            KKTool.closeStatementAndConnectionInSilence(pstmt, connection);
        }
        return -1;
    }

    /**
     * 保存操作
     * 
     * @param sql
     * @param paramsObj
     * @param paramsType
     * @return
     */
    protected int executeUpdate(String sql, Object[] paramsObj, int[] paramsType) {
        logger.info("executeUpdate sql:{} sql参数", sql, Arrays.toString(paramsObj));
        int count = -1;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getMasterConnection();
            if (connection == null) {
            	return -1;
            }
            statement = connection.prepareStatement(sql);
            // 注入参数
            for (int i = 0; i < paramsObj.length; i++) {
                if (paramsType[i] == Types.INTEGER) {// int
                    statement.setInt(i + 1, Integer.valueOf(paramsObj[i].toString()));
                } else if (paramsType[i] == Types.VARCHAR) {// string
                    statement.setString(i + 1, paramsObj[i].toString());
                } else if (paramsType[i] == Types.NUMERIC) {// number、long
                    statement.setLong(i + 1, Long.valueOf(paramsObj[i].toString()));
                } else if (paramsType[i] == Types.DOUBLE) {// DOUBLE
                    statement.setDouble(i + 1, Double.valueOf(paramsObj[i].toString()));
                } else if (paramsType[i] == Types.FLOAT) {// FLOAT
                    statement.setFloat(i + 1, Float.valueOf(paramsObj[i].toString()));
                } else if (paramsType[i] == Types.CLOB) {// CLOB
                    statement.setClob(i + 1, (Clob) paramsObj[i]);
                } else if (paramsType[i] == Types.BLOB) {// BLOB
                    statement.setBlob(i + 1, (Blob) paramsObj[i]);
                } else {
                    logger.warn("没有找到{}类型", paramsType[i]);
                }
            }
            count = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("saveUpPlatformMsg异常", e);
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(resultSet, statement, connection);
        }
        return count;
    }

    /**
     * 查询获取单个对象
     * 
     * @param sql
     * @param paramsObj
     * @param objClass
     * @param <T>
     * @return
     */
    protected <T> T executeQueryForSingle(String sql, Object[] paramsObj, Class<T> objClass) {
        logger.info("executeQueryForSingle sql:{} sql参数", sql, Arrays.toString(paramsObj));
        T t = null;
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getMasterConnection();
            if (connection == null) {
            	return t;
            }
            pstmt = connection.prepareStatement(sql);
            // 注入参数
            if (paramsObj != null) {
                for (int i = 0; i < paramsObj.length; i++) {
                    pstmt.setObject(i + 1, paramsObj[i]);
                }
            }
            // 执行查询
            rs = pstmt.executeQuery();
            // 获取结果集的数据
            while (rs.next()) {
                // 获取类的实例
                t = objClass.newInstance();
                // 获取所有的字段名称
                Field[] fields = objClass.getDeclaredFields();
                for (Field field : fields) {
                    // 将从数据库中取出来的值类型转换为与javabean中的字段类型一致
                    Object val = convertFieldTypeVal(rs, field);
                    // 获取需要set的方法名称
                    String methodName = getSetMethodName(field.getName());
                    // 获取方法
                    Method method = objClass.getDeclaredMethod(methodName, field.getType());
                    if (val != null) {
                        // 赋值
                        method.invoke(t, val);
                    }
                }
            }
        } catch (SecurityException e) {
            logger.info("executeQueryForSingle", e);
        } catch (IllegalArgumentException e) {
            logger.info("executeQueryForSingle", e);
        } catch (SQLException e) {
            logger.info("executeQueryForSingle", e);
        } catch (InstantiationException e) {
            logger.info("executeQueryForSingle", e);
        } catch (IllegalAccessException e) {
            logger.info("executeQueryForSingle", e);
        } catch (InvocationTargetException e) {
            logger.info("executeQueryForSingle", e);
        } catch (Exception e) {
            logger.info("executeQueryForSingle", e);
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(rs, pstmt, connection);
        }
        return t;
    }

    protected <T> List<T> executeQueryForMultiple(String sql, Object[] paramsObj, Class<T> objClass) {
        List<T> list = new ArrayList<T>();
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            connection = getMasterConnection();
            if (connection == null) {
            	return list;
            }
            pstmt = connection.prepareStatement(sql);

            if (paramsObj != null) {
                // 注入参数
                for (int i = 0; i < paramsObj.length; i++) {
                    pstmt.setObject(i + 1, paramsObj[i]);
                }
            }
            // 执行查询
            rs = pstmt.executeQuery();
            // 获取结果集的数据
            while (rs.next()) {
                T t = null;
                // 获取类的实例
                t = objClass.newInstance();
                // 获取所有的字段名称
                Field[] fields = objClass.getDeclaredFields();
                for (Field field : fields) {
                    // 将从数据库中取出来的值类型转换为与javabean中的字段类型一致
                    Object val = convertFieldTypeVal(rs, field);
                    // 获取需要set的方法名称
                    String methodName = getSetMethodName(field.getName());
                    // 获取方法
                    Method method = objClass.getDeclaredMethod(methodName, field.getType());
                    if (val != null) {
                        // 赋值
                        method.invoke(t, val);
                    }
                }
                // 向集合中添加数据
                list.add(t);
            }

        } catch (SecurityException e) {
            logger.info("executeQueryForMultiple", e);
        } catch (IllegalArgumentException e) {
            logger.info("executeQueryForMultiple", e);
        } catch (SQLException e) {
            logger.info("executeQueryForMultiple", e);
        } catch (InstantiationException e) {
            logger.info("executeQueryForMultiple", e);
        } catch (IllegalAccessException e) {
            logger.info("executeQueryForMultiple", e);
        } catch (Exception e) {
            logger.info("executeQueryForMultiple", e);
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(rs, pstmt, connection);
        }
        // 返回集合
        return list;
    }

    private Object convertFieldTypeVal(ResultSet rs, Field field) {
        Object val = null;
        String fieldType = field.getType().getName();
        String fieldName = field.getName();
        try {
            if (fieldType.equals(String.class.getName())) {
                val = rs.getString(fieldName);
            } else if (fieldType.equals(Byte.class.getName())) {
                val = rs.getByte(fieldName);
            } else if (fieldType.equals(Byte[].class.getName())) {
                byte[] bytes = rs.getBytes(fieldName);
                Byte[] objBtye = new Byte[bytes.length];
                for (int i = 0; i < bytes.length; i++) {
                    objBtye[i] = bytes[i];
                }
                val = objBtye;
            } else if (fieldType.equals(byte.class.getName())) {
                val = rs.getByte(fieldName);
            } else if (fieldType.equals(byte[].class.getName())) {
                val = rs.getBytes(fieldName);
            } else if (fieldType.equals(Short.class.getName())) {
                val = rs.getShort(fieldName);
            } else if (fieldType.equals(short.class.getName())) {
                val = rs.getShort(fieldName);
            } else if (fieldType.equals(Integer.class.getName())) {
                val = rs.getInt(fieldName);
            } else if (fieldType.equals(int.class.getName())) {
                val = rs.getInt(fieldName);
            } else if (fieldType.equals(Long.class.getName())) {
                val = rs.getLong(fieldName);
            } else if (fieldType.equals(long.class.getName())) {
                val = rs.getLong(fieldName);
            } else if (fieldType.equals(BigDecimal.class.getName())) {
                val = rs.getBigDecimal(fieldName);
            } else if (fieldType.equals(Boolean.class.getName())) {
                val = rs.getBoolean(fieldName);
            } else if (fieldType.equals(boolean.class.getName())) {
                val = rs.getBoolean(fieldName);
            } else if (fieldType.equals(Date.class.getName())) {
                val = rs.getTimestamp(fieldName);
            } else if (fieldType.equals(java.sql.Date.class.getName())) {
                val = rs.getDate(fieldName);
            } else if (fieldType.equals(Float.class.getName())) {
                val = rs.getFloat(fieldName);
            } else if (fieldType.equals(Time.class.getName())) {
                val = rs.getTime(fieldName);
            } else if (fieldType.equals(Timestamp.class.getName())) {
                val = rs.getTimestamp(fieldName);
            } else {
                logger.info("{}字段类型匹配失败", fieldName);
            }
        } catch (SQLException e) {
            logger.info("convertFieldTypeVal sql exception", e);
        }
        return val;
    }

    /**
     * 获取set方法
     * 
     * @param name
     * @return
     */
    private String getSetMethodName(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    protected long executeInsertAndRetrieveId(String sql, List<Object> params) throws SQLException {
        long id = -1;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getMasterConnection();
            if (conn != null) {
                statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                if (params != null) {
                    for (int i = 0; i < params.size(); i++) {
                        statement.setObject(i + 1, params.get(i));
                    }
                }
                statement.executeUpdate();
                rs = statement.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    id = rs.getLong(1);
                }
                logger.debug(sql);
            }
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(rs, statement, conn);
        }
        return id;
    }

}
