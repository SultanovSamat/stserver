package com.jadic.utils;


/**
 * @author 	Jadic
 * @created 2014-6-3
 */
public abstract class MyAbstractThread extends Thread {
    
    public MyAbstractThread() {
        setUncaughtExceptionHandler(new MyExceptionHandler());
        setName(this.getClass().getName());
    }
}
