package com.jadic.biz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.cmd.req.CmdChargeDetailReq;
import com.jadic.utils.KKTool;

/**
 * 根据终端交易数据生成交易文件
 * 文件名规则：ZZZD + yyyymmddhhmiss + 6位批次号 + .sup
 * 文件位置: data/yyyyMMdd/file
 * 备份位置: data/bak/file
 * @author Jadic
 * @created 2014年8月5日
 */
public class ThreadTerminalChargeDetail extends
		AbstractThreadDisposeDataFromQueue<CmdChargeDetailReq> {
    
    private final static Logger log = LoggerFactory.getLogger(ThreadTerminalChargeDetail.class);

    private int fileSNo;//文件名中的批次号
    
	@Override
	public void run() {
	    initFileSNo();
		while(!isInterrupted()) {
		    CmdChargeDetailReq chargeDetail = null;
		    while ((chargeDetail = getQueuePollData()) != null) {
		        disposeCmd(chargeDetail);
		    }
		    waitNewData();
		}
	}
	
	private void disposeCmd(CmdChargeDetailReq chargeDetail) {
	    
	}
	
	/*
	 * 获取要写入的文件
	 * 获取规则：到data/yyyyMMdd目录下查找最新的文件，约定当前在写的文件未超过允许的记录数，则无后缀名 
	 */
	private File getFile() {
	    String today = KKTool.getCurrFormatDate("yyyyMMdd");
	    File dir = new File("data/" + today);
	    
	    return null;
	}

	/**
	 * 初始化文件名中用到的批次号。
	 * 首次检测data目录下是否存在fileSNo文件,如果不存在,则创建
	 */
	private void initFileSNo() {
	    fileSNo = 1;
	    KKTool.createFileDir("data");
	    File file = new File("data/fileSNo.txt");
	    if (file.exists()) {
	        BufferedReader reader = null;
	        try {
                reader = new BufferedReader(new FileReader(file));
                fileSNo = Integer.parseInt(reader.readLine());
            } catch (FileNotFoundException e) {
                log.error("initFileSNo", e);
            } catch (IOException e) {
                log.error("initFileSNo", e);
            } finally {
                KKTool.closeReaderInSilence(reader);
            }
	    } else {
	        KKTool.createWindowsHiddenFile(file.getAbsolutePath());
	        BufferedWriter writer = null;
	        try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(String.valueOf(fileSNo));
            } catch (IOException e) {
                log.error("initFileSNo", e);
            } finally {
                KKTool.closeWriterInSilence(writer);
            }
	        
	    }
	}
	
	private void updateFileNextSNo(int fileSNo) {
	    KKTool.createFileDir("data");
        File file = new File("data/fileSNo.txt");
        if (!file.exists()) {
            KKTool.createWindowsHiddenFile(file.getAbsolutePath());
        }
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.valueOf(fileSNo));
        } catch (IOException e) {
            log.error("initFileSNo", e);
        } finally {
            KKTool.closeWriterInSilence(writer);
        }
        
	}
}
