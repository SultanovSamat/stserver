/**
 * @author 	Jadic
 * @created 2014-2-21
 */
package com.jadic.db;

public final class SQL {

    private SQL() {
    }
    
    public final static String QUERY_TERMINAL_INFO = "select a.id, a.enabled " +
    		                                           "from tab_terminal a" +
    		                                           "where a.typeid=1";
}
