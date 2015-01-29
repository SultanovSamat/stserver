package com.jadic.biz.bean;

import java.util.ArrayList;
import java.util.List;

import com.jadic.utils.Const;

/**
 * @author 	Jadic
 * @created 2015-1-29
 */
public class DBSaveBean {
    
    private String sql;
    private List<Object> params;
    
    public DBSaveBean() {
        sql = Const.EMPTY_STR;
        params = new ArrayList<Object>();
    }
    
    public DBSaveBean(String sql) {
        this();
        this.sql = sql;
    }
    
    public DBSaveBean addParam(Object param) {
        params.add(param);
        return this;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getParams() {
        return params;
    }

}
