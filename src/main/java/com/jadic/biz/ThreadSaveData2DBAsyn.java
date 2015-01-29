package com.jadic.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.biz.bean.DBSaveBean;
import com.jadic.db.DBOper;

/**
 * 该线程用来保存些处理返回值的insert,update操作.<br>
 * DBSaveBean中封装了调用的sql语句，及相关参数
 * @author 	Jadic
 * @created 2015-1-29
 */
public class ThreadSaveData2DBAsyn extends AbstractThreadDisposeDataFromQueue<DBSaveBean> {
    
    private Logger log = LoggerFactory.getLogger(ThreadSaveData2DBAsyn.class);

    @Override
    public void run() {
        DBSaveBean dataBean = null;
        while (!isInterrupted()) {
            while ((dataBean = getQueuePollData()) != null) {
                boolean saveResult = saveData(dataBean);
                log.debug("save data, sql:{}, result:{}", dataBean.getSql(), saveResult);
            }
            waitNewData();
        }
    }
    
    private boolean saveData(DBSaveBean dataBean) {
        return DBOper.getDBOper().saveDataBean(dataBean);
    }

}
