/**
 * @author 	Jadic
 * @created 2014-2-28
 */
package com.jadic.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyExceptionHandler implements UncaughtExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);
    private final static String ERR_TIP = "An exception has been captured\n " +
                                            "Thread:{}\n " +
                                            "Exception: {}: {}:\n " +
                                            "Thread status:{}\n";

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error(ERR_TIP, t.getName(), e.getClass().getName(), KKTool.getExceptionTip(e), t.getState());
    }

}
