package com.jadic.biz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.cmd.req.CmdChargeDetailReq;
import com.jadic.utils.Const;
import com.jadic.utils.KKTool;
import com.jadic.utils.SysParams;

/**
 * 根据终端交易数据生成交易文件 
 * 文件名规则：ZZZD + yyyymmddhhmiss + 6位批次号 + .sup 文件位置:
 * data/yyyyMMdd/file 备份位置: data/bak/file
 * 
 * @author Jadic
 * @created 2014年8月5日
 */
public class ThreadTerminalChargeDetail extends AbstractThreadDisposeDataFromQueue<CmdChargeDetailReq> {

    private final static Logger log = LoggerFactory.getLogger(ThreadTerminalChargeDetail.class);
    private final static String AGENCY_FLAG = "ZZZD";
    private final static int MAX_RECORDS_PER_FILE = 99999999;
    private final static String CITYCODE = "0519";
    private final static String DELIMITER = ",";
    private final static String S0_10 = "0000000000";
    private final static String S0_8 = "00000000";
    private final static String TRANS_STATUS = "1";
    private final static String TRANS_TYPE = "02";
    private final static String AGENCY_NO = SysParams.getInstance().getAgencyNo();//"1234";
    private final static String SAM = SysParams.getInstance().getSamId();//"11111111";
    private final static String POSID = SysParams.getInstance().getPosId();//"123456";
    private final static String OPERNO = SysParams.getInstance().getOperNo();//"1234";
    
    public final static String FILE_SNO_NAME = "fileSNo.txt";

    private int fileSNo;// 文件名中的批次号
    private String lastCheckDate;
    
    @Override
    public void run() {
        initFileSNo();
        lastCheckDate = "";
        while (!isInterrupted()) {
            checkYesterdayFileNameChanged();
            CmdChargeDetailReq chargeDetail = null;
            while ((chargeDetail = getQueuePollData()) != null) {
                disposeCmd(chargeDetail);
            }
            waitNewData();
        }
    }

    private void disposeCmd(CmdChargeDetailReq chargeDetail) {
        addChargeDetailToFile(chargeDetail);
        addChargeDetailToDB(chargeDetail);
    }
    
    private boolean addChargeDetailToDB(CmdChargeDetailReq chargeDetail) {
        return false;
    }
    
    private boolean addChargeDetailToFile(CmdChargeDetailReq chargeDetail) {
        File file = getFile();
        appendRecord(file, chargeDetail);
        int recordCount = updateFileHead(file, chargeDetail);
        if (recordCount >= MAX_RECORDS_PER_FILE) {
            updateFileNextSNo();
            if (!renameFile(file)) {
                log.info("file[{}] failed to rename", file.getName());
            }
        }
        return true;
    }
    
    /*
     * 获取要写入的文件 获取规则：到data/yyyyMMdd目录下查找最新的文件，约定当前在写的文件未超过允许的记录数，则无后缀名
     */
    private File getFile() {
        String today = KKTool.getCurrFormatDate("yyyyMMdd");
        File dir = new File(Const.CHARGE_DETAIL_DIR);
        File file = null;
        if (dir.exists()) {//
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().matches(AGENCY_FLAG + "\\d{20}");
                }
            });
            if (files != null && files.length > 0) {
                file = files[0];
                if (!file.getName().substring(4, 12).equals(today)) {//如果当前获取的文件的日期不是当天的，则需将后缀加上供上传
                    File newFile = new File(file.getAbsolutePath() + Const.CHARGE_DETAIL_FILE_SUFFIX);
                    file.renameTo(newFile);
                    updateFileNextSNo();
                    file = null;
                }
            }
        } else {
            KKTool.createFileDir(dir.getAbsolutePath());
        }
        if (file == null) {
            file = new File(dir, getFileName());
            initFileHead(file);
        }
        return file;
    }
    
    private String getFileName() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(AGENCY_FLAG);
        sBuilder.append(KKTool.getCurrFormatDate("yyyyMMddHHmmss"));
        sBuilder.append(KKTool.getFixedLenString(String.valueOf(fileSNo), 6, '0', true));
        return sBuilder.toString();
    }
    
    /**
     * @param file
     * @return
     */
    private boolean initFileHead(File file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.append(CITYCODE).append(DELIMITER);
            writer.append(S0_8).append(DELIMITER);
            writer.append(S0_10).append(DELIMITER);
            writer.append(KKTool.getFixedLenString(String.valueOf(fileSNo), 6, '0', true)).append(DELIMITER);
            writer.append(KKTool.getCurrFormatDate("yyyyMMdd")).append(DELIMITER);
            writer.append(KKTool.getCurrFormatDate("HHmmss"));
            writer.append('\r').append('\n');
            writer.flush();
            return true;
        } catch (IOException e) {
            log.error("init file[{}] head", file.getName(), e);
        } finally {
            KKTool.closeWriterInSilence(writer);
        }
        return false;
    }
    
    /**
     * 追加一条记录到文件中
     * @param chargeDetail
     * @return
     */
    private boolean appendRecord(File file, CmdChargeDetailReq chargeDetail) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, true));
            
            writer.append(TRANS_STATUS).append(DELIMITER);
            writer.append(KKTool.getLeftFillFixedLenStr(KKTool.byteArrayToHexStr(chargeDetail.getAsn()), 20, '0')).append(DELIMITER);
            writer.append(KKTool.getLeftFillFixedLenStr(KKTool.byteArrayToHexStr(chargeDetail.getTsn()), 4, '0')).append(DELIMITER);
            writer.append(KKTool.getRightFillFixedLenStr(new String(chargeDetail.getBankCardNo()).trim(), 19, ' ')).append(DELIMITER);
            writer.append(TRANS_TYPE).append(DELIMITER);
            writer.append(KKTool.byteToHexStr(chargeDetail.getCardType())).append(DELIMITER);
            writer.append(KKTool.byteArrayToHexStr(chargeDetail.getTransDate())).append(DELIMITER);
            writer.append(KKTool.byteArrayToHexStr(chargeDetail.getTransTime())).append(DELIMITER);
            writer.append(KKTool.getLeftFillFixedLenStr(String.valueOf(chargeDetail.getTransAmount()), 10, '0')).append(DELIMITER);
            writer.append(KKTool.getLeftFillFixedLenStr(String.valueOf(chargeDetail.getBalanceBeforeTrans()), 10, '0')).append(DELIMITER);
            writer.append(AGENCY_NO).append(DELIMITER);
            writer.append(SAM).append(DELIMITER);
            writer.append(POSID).append(DELIMITER);
            writer.append(OPERNO).append(DELIMITER);
            writer.append(KKTool.byteArrayToHexStr(chargeDetail.getTac())).append(DELIMITER);
            writer.append(AGENCY_FLAG);//流水号前追加ZZZD标识
            writer.append(KKTool.getLeftFillFixedLenStr(KKTool.byteArrayToHexStr(chargeDetail.getTransSNo()), 12, '0'));
            writer.append('\r').append('\n');
            
            writer.flush();
        } catch (IOException e) {
            log.error("append record in file[{}]", file.getName(), e);
        } finally {
            KKTool.closeWriterInSilence(writer);
        }
        return true;
    }
    
    /**
     * 更新文件头，返回总记录数
     * @param file
     * @param chargeDetail
     * @return
     */
    private int updateFileHead(File file, CmdChargeDetailReq chargeDetail) {
        RandomAccessFile raf = null;
        int count = 0;
        try {
            raf = new RandomAccessFile(file, "rw");
            byte[] buf = new byte[8];
            raf.seek(5);
            raf.read(buf);
            count = Integer.parseInt(new String(buf));
            count ++;
            raf.seek(5);
            raf.writeBytes(KKTool.getLeftFillFixedLenStr(String.valueOf(count), 8, '0'));
            
            buf = new byte[10];
            raf.seek(14);
            raf.read(buf);
            int amount = Integer.parseInt(new String(buf));
            amount += chargeDetail.getTransAmount();
            raf.seek(14);
            raf.writeBytes(KKTool.getLeftFillFixedLenStr(String.valueOf(amount), 10, '0'));
        } catch (FileNotFoundException e) {
            log.error("update file[{}] head", file.getName(), e);
        } catch (IOException e) {
            log.error("update file[{}] head", file.getName(), e);
        } finally {
            KKTool.closeRandomAccessFileInSilence(raf);
        }
        return count;
    }
    
    /**
     * 初始化文件名中用到的批次号。 首次检测data目录下是否存在fileSNo文件,如果不存在,则创建
     */
    private void initFileSNo() {
        fileSNo = 1;
        KKTool.createFileDir(Const.CHARGE_DETAIL_DIR_PARENT);
        File file = new File(Const.CHARGE_DETAIL_DIR_PARENT, FILE_SNO_NAME);
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

    /**
     * 更新文件批次号到文件中，防止程序终止
     * @param newFileSNo
     */
    private void updateFileNextSNo() {
        fileSNo ++;
        if (fileSNo > 999999) {
            fileSNo = 1;
        }
        KKTool.createFileDir(Const.CHARGE_DETAIL_DIR_PARENT);
        File file = new File(Const.CHARGE_DETAIL_DIR_PARENT, FILE_SNO_NAME);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.valueOf(fileSNo));
            writer.flush();
        } catch (IOException e) {
            log.error("initFileSNo", e);
        } finally {
            KKTool.closeWriterInSilence(writer);
        }
    }
    
    /**
     * 对已满足最大记录数的文件生成后，追加后缀名，后续交易新建文件
     * @param file
     */
    private boolean renameFile(File file) {
        File newFile = new File(file.getAbsoluteFile() + Const.CHARGE_DETAIL_FILE_SUFFIX);
        return file.renameTo(newFile);
    }
    
    /**
     * 检查跨天时，前一天的文件没有达到最大记录数，而没有改后缀名的问题
     */
    private void checkYesterdayFileNameChanged() {
        String currDate = KKTool.getCurrFormatDate("yyyyMMdd");
        if (!currDate.equals(lastCheckDate)) {
            lastCheckDate = currDate;
            File dir = new File(Const.CHARGE_DETAIL_DIR);
            if (dir.exists()) {
                File[] files = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().matches(AGENCY_FLAG + "\\d{20}");
                    }
                });
                
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        String date = file.getName().substring(4, 12);
                        if (date.compareTo(currDate) < 0) {
                            File newFile = new File(file.getAbsolutePath() + Const.CHARGE_DETAIL_FILE_SUFFIX);
                            file.renameTo(newFile);
                        }
                    }
                }
            }
        }
    }

}
