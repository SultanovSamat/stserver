/**
 * @author Jadic
 * @created 2012-5-24 
 */
/**
 * 
 */
package com.jadic.biz;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.ftp.FTPClientTool;
import com.jadic.ftp.FtpUploadStatus;
import com.jadic.utils.Const;
import com.jadic.utils.KKTool;
import com.jadic.utils.SysParams;

/**
 *  
 * @author Jadic
 * @created 2014年8月7日
 */
public class ThreadUploadPosDealData extends Thread {
    
    private final static Logger log = LoggerFactory.getLogger(ThreadUploadPosDealData.class);

	private SysParams sysParams;
	private FTPClientTool ftpClientTool;

	public ThreadUploadPosDealData() {
		sysParams = SysParams.getInstance();
		ftpClientTool = new FTPClientTool(sysParams.getFtpServerHost(), sysParams.getFtpServerPort(), 
										  sysParams.getFtpUserName(), sysParams.getFtpUserPass());
	}

	@Override
	public void run() {
		while (!interrupted()) {
			uploadPosDealData();
			KKTool.sleepTime(2 * 60 * 1000);
		}
	}

	private void uploadPosDealData() {
		List<File> uploadFiles = getUploadFile();
		if (uploadFiles.size() > 0) {
			try {
				ftpClientTool.connect();
				ftpClientTool.login();
				
				for (File file : uploadFiles) {
					FtpUploadStatus uploadStatus = uploadPosDealData(file);
					if (FtpUploadStatus.UPLOAD_SUCC_NEW == uploadStatus) {
						String date = file.getName().substring(4, 12);// 交易日期
						String bakDir = Const.CHARGE_DETAIL_BAK_DIR + "/" + date;
						KKTool.createFileDir(bakDir);
						
						File bakFile = new File(bakDir, file.getName());
						boolean isRenameSucceeded = file.renameTo(bakFile);
						if (!isRenameSucceeded) {// 如果未重命名成功，可能是目标文件夹下有重名的文件，则此时在文件名中加下当前时间来区分
							bakFile = new File(bakFile.getParent() + "/"
									+ file.getName() + "_"
									+ KKTool.getCurrFormatDate("yyMMddHHmmSS"));
							isRenameSucceeded = file.renameTo(bakFile);
						}
						
						if (isRenameSucceeded) {
						    log.info("Pos deal data[{}]upload succ, move this file to bak dir", file.getName());
						} else {
						    log.info("Pos deal data[{}]upload succ, but fail to move this file to bak dir", file.getName());
						}
					} else {
					    log.warn("Pos deal data[{}]upload fail, upload result:{}", file.getName(), uploadStatus);
					}
				}
			} catch (Exception e) {
			    log.error("upload data err", e);
			} finally {
				ftpClientTool.disConnect();
			}
		}
	}

	/**
	 * 获取可上传的合法格式的交易数据文件 
	 * 正确交易数据的存放路径示例:/data/chargeDetail/current/ZZZD yyyyMMdd HHmmss 123456.sup
	 * @return
	 */
	private List<File> getUploadFile() {
		List<File> uploadFiles = new ArrayList<File>();
		String dataDir = Const.CHARGE_DETAIL_DIR;

		if (!KKTool.isStrNullOrBlank(dataDir)) {
			File parentDir = new File(dataDir);
			if (parentDir.exists()) {
			    final String today = KKTool.getCurrFormatDate("yyyyMMdd");
			    
				File[] dataFiles = parentDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        String fileName = file.getName();
                        String suffix = Const.CHARGE_DETAIL_FILE_SUFFIX;
                        return fileName.matches("ZZZD\\d{20}" + suffix) && fileName.substring(4, 12).compareTo(today) < 0;
                    }
                });
				if (dataFiles != null && dataFiles.length > 0) {
				    for (File file : dataFiles)
				    uploadFiles.add(file);
				}
			}
		}
		return uploadFiles;
	}

	private FtpUploadStatus uploadPosDealData(File file) {
			if (!ftpClientTool.isConnected()) {
				ftpClientTool.connect();
				ftpClientTool.login();
			}
			
			if (ftpClientTool.isConnected()) {
				return ftpClientTool.upload(sysParams.getFtpUploadDir()	+ "/" + file.getName(),
											file.getAbsolutePath(), false);
			} else {
				return FtpUploadStatus.UPLOAD_CONNECTION_ERR;
			}
	}

}
