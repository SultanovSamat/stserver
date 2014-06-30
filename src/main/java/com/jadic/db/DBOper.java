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
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.utils.Const;
import com.jadic.utils.JDBCConfig;
import com.jadic.utils.KKTool;
import com.jadic.utils.SysParams;

/**
 * 数据库操作
 */
public final class DBOper {
    
    private final static Logger logger = LoggerFactory.getLogger(DBOper.class);
    
    private final static DBOper dbOper = new DBOper();
    private DBConnPool masterPool;

    public synchronized static DBOper getDBOper() {
        return dbOper;
    }

    private DBOper() {
        List<JDBCConfig> jdbcList = SysParams.getInstance().getJdbcListCopy();
        if (jdbcList.size() <= 0) {
            logger.error("no jdbc config is set, please check");
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

    public void test() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getMasterConnection();
            statement = connection
                    .prepareStatement("select status_updatetime, pos_time from host_curstatus where status_updatetime > ?");
            statement.setObject(1, new java.sql.Timestamp(new Date().getTime()));
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                java.util.Date date = resultSet.getTimestamp("pos_time");
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(date.getTime());
            }
        } catch (SQLException e) {
            logger.error("getCarInfoMaxRowscn异常", e);
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(resultSet, statement, connection);
        }
    }

    private <T> List<T> queryForList(String sql, Object[] paramsObj, Class<T> objClass) {
        List<T> list = new ArrayList<T>();
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            connection = getMasterConnection();
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
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
        }
        return val;
    }

    private String getSetterMethod(String fieldName) {
        if (!KKTool.isStrNullOrBlank(fieldName)) {
            return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
        return Const.EMPTY_STR;
    }

    private Connection getMasterConnection() throws SQLException {
        return masterPool.getConnection();
    }

    public static void release() {
        logger.info("释放数据库连接");
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
    private Object executeQueryForObj(String sql, Object[] paramsObj) {
        logger.info("executeQueryForCount sql:{} sql参数", sql, Arrays.toString(paramsObj));
        Object obj = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = getMasterConnection();
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
            e.printStackTrace();
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(rs, statement, connection);
        }
        return obj;
    }

    /**
     * 保存操作
     * 
     * @param sql
     * @param paramsObj
     * @param paramsType
     * @return
     */
    private int executeUpdate(String sql, Object[] paramsObj, int[] paramsType) {
        logger.info("executeUpdate sql:{} sql参数", sql, Arrays.toString(paramsObj));
        int count = -1;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getMasterConnection();
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
                    logger.warn("没有找到{}类型",  paramsType[i]);
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
    private <T> T executeQueryForSingle(String sql, Object[] paramsObj, Class<T> objClass) {
        logger.info("executeQueryForSingle sql:{} sql参数", sql, Arrays.toString(paramsObj));
        T t = null;
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getMasterConnection();
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
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            KKTool.closeRS_Statement_ConnInSilence(rs, pstmt, connection);
        }
        return t;
    }

    private <T> List<T> executeQueryForMultiple(String sql, Object[] paramsObj, Class<T> objClass) {
        List<T> list = new ArrayList<T>();
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            connection = getMasterConnection();
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
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
    // **********************************************************数据库查询、保存操作********************************************************************
}
