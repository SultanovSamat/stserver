package com.jadic.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.utils.KKTool;

/**
 * @author Jadic
 * @created 2012-12-5
 */
public class ExcelUtils {
    
    private final static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * get the excel file by file name, create the excel file if the file is not
     * existed and isCreatedIfNotExisted is set true return null if any
     * exception is thrown
     * 
     * @param fileName
     * @param isCreatedIfNotExisted
     * @return
     */
    public static Workbook getWorkbook(String fileName, boolean isCreatedIfNotExisted) {
        Workbook wb = null;
        File excelFile = new File(fileName);
        if (!excelFile.exists()) {
            if (isCreatedIfNotExisted) {
                wb = new HSSFWorkbook();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(excelFile);
                    wb.write(new FileOutputStream(excelFile));
                } catch (FileNotFoundException e) {
                    wb = null;
                    logger.error("GetWorkbook create new workbook InvalidFormatException", e);
                } catch (IOException e) {
                    wb = null;
                    logger.error("GetWorkbook create new workbook IOException", e);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        } else {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(excelFile);
                wb = WorkbookFactory.create(fis);
            } catch (InvalidFormatException e) {
                wb = null;
                logger.error("getWorkbook InvalidFormatException", e);
            } catch (IOException e) {
                wb = null;
                logger.error("getWorkbook IOException", e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return wb;
    }

    /**
     * create new excel file, and set data title
     * 
     * @param fileName
     * @return
     */
    public static Workbook getWorkbook4Fuzhou(String fileName) {
        Workbook wb = getWorkbook(fileName, true);
        if (wb != null) {
            Sheet sheet = wb.getSheet("交易数据明细");
            if (sheet == null) {// 新文件
                sheet = wb.createSheet("交易数据明细");
                Row row = sheet.createRow(0);
                row.setHeightInPoints(20);

                row.createCell(0).setCellValue("序号");
                sheet.setColumnWidth(0, 6 * 256);

                row.createCell(1).setCellValue("POS机终端号");
                sheet.setColumnWidth(1, 13 * 256);

                row.createCell(2).setCellValue("司机卡号");
                sheet.setColumnWidth(2, 21 * 256);

                row.createCell(3).setCellValue("司机姓名");
                sheet.setColumnWidth(3, 10 * 256);

                row.createCell(4).setCellValue("用户卡卡号");
                sheet.setColumnWidth(4, 21 * 256);

                row.createCell(5).setCellValue("用户卡有效期");
                sheet.setColumnWidth(5, 13 * 256);

                row.createCell(6).setCellValue("流水号");
                sheet.setColumnWidth(6, 10 * 256);

                row.createCell(7).setCellValue("批次号");
                sheet.setColumnWidth(7, 10 * 256);

                row.createCell(8).setCellValue("商户号");
                sheet.setColumnWidth(8, 16 * 256);

                row.createCell(9).setCellValue("55数据域");

                for (int i = 0; i <= 8; i++) {
                    setCellVertCenter(row.getCell(i));
                    setCellBackgroudColor(row.getCell(i), IndexedColors.LEMON_CHIFFON.getIndex());
                }
                setCellHoriAndVertCenter(row.getCell(9));
                setCellBackgroudColor(row.getCell(9), IndexedColors.GREY_25_PERCENT.getIndex());

                row = sheet.createRow(1);
                row.setHeightInPoints(20);
                for (int i = 0; i <= 8; i++) {
                    row.createCell(i);
                }
                row.createCell(9).setCellValue("应用密文(9F26)");
                sheet.setColumnWidth(9, 19 * 256);

                row.createCell(10).setCellValue("应用信息数据(9F27)");
                sheet.setColumnWidth(10, 18 * 256);

                row.createCell(11).setCellValue("发卡行应用数据(9F10)");
                sheet.setColumnWidth(11, 30 * 256);

                row.createCell(12).setCellValue("不可预知数(9F37)");
                sheet.setColumnWidth(12, 16 * 256);

                row.createCell(13).setCellValue("应用交易计数器(9F36)");
                sheet.setColumnWidth(13, 18 * 256);

                row.createCell(14).setCellValue("交易日期(9A)");
                sheet.setColumnWidth(14, 12 * 256);

                row.createCell(15).setCellValue("交易金额(9F02)");
                sheet.setColumnWidth(15, 14 * 256);

                row.createCell(16).setCellValue("应用交互特征(82)");
                sheet.setColumnWidth(16, 16 * 256);

                row.createCell(17).setCellValue("终端国家代码(9F1A)");
                sheet.setColumnWidth(17, 18 * 256);

                row.createCell(18).setCellValue("其它金额(9F03)");
                sheet.setColumnWidth(18, 14 * 256);

                row.createCell(19).setCellValue("终端性能(9F33)");
                sheet.setColumnWidth(19, 14 * 256);

                row.createCell(20).setCellValue("专用文件名称(84)");
                sheet.setColumnWidth(20, 20 * 256);

                row.createCell(21).setCellValue("交易序列计数器(9F41)");
                sheet.setColumnWidth(21, 20 * 256);

                row.createCell(22).setCellValue("发卡行授权码(9F74)");
                sheet.setColumnWidth(22, 18 * 256);

                row.createCell(23).setCellValue("PAN序列号(5F34)");
                sheet.setColumnWidth(23, 15 * 256);

                for (int i = 0; i <= 8; i++) {
                    setCellVertCenter(row.getCell(i));
                    setCellBackgroudColor(row.getCell(i), IndexedColors.LEMON_CHIFFON.getIndex());
                }

                for (int i = 9; i <= 23; i++) {
                    setCellVertCenter(row.getCell(i));
                    setCellBackgroudColor(row.getCell(i), IndexedColors.LIGHT_TURQUOISE.getIndex());
                }

                sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 6, 6));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 7, 7));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 8, 8));

                sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 23));
                saveWorkbook(wb, fileName);
            }
        }
        return wb;
    }

    public static Workbook getWorkbook4ShangRao(String fileName) {
        Workbook wb = getWorkbook(fileName, true);
        if (wb != null) {
            Sheet sheet = wb.getSheet("交易数据明细");
            if (sheet == null) {// 新文件
                sheet = wb.createSheet("交易数据明细");
                Row row = sheet.createRow(0);
                row.setHeightInPoints(20);

                int sIndex = 0;
                int eIndex = 0;
                int d5SIndex = 0;
                int d5EIndex = 0;
                int offset = sIndex;
                row.createCell(offset).setCellValue("序号");
                sheet.setColumnWidth(offset++, 6 * 256);

                row.createCell(offset).setCellValue("数据类型");
                sheet.setColumnWidth(offset++, 8 * 256);

                row.createCell(offset).setCellValue("交易时间");
                sheet.setColumnWidth(offset++, 16 * 256);

                row.createCell(offset).setCellValue("司机卡号");
                sheet.setColumnWidth(offset++, 21 * 256);

                row.createCell(offset).setCellValue("用户卡卡号");
                sheet.setColumnWidth(offset++, 21 * 256);

                row.createCell(offset).setCellValue("用户卡有效期");
                sheet.setColumnWidth(offset++, 13 * 256);

                row.createCell(offset).setCellValue("流水号");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("商户号");
                sheet.setColumnWidth(offset++, 16 * 256);

                row.createCell(offset).setCellValue("终端机编号");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("检索参考号");
                sheet.setColumnWidth(offset++, 25 * 256);

                row.createCell(offset).setCellValue("交易金额");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("消费前卡余额");
                sheet.setColumnWidth(offset++, 12 * 256);

                row.createCell(offset).setCellValue("等候时间");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("营运里程");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("空驶里程");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("上车日期");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("上车时间");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("下车时间");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("车牌号");
                sheet.setColumnWidth(offset++, 10 * 256);

                row.createCell(offset).setCellValue("附加费");
                sheet.setColumnWidth(offset++, 8 * 256);

                eIndex = offset - 1;

                for (int i = sIndex; i <= eIndex; i++) {
                    setCellVertCenter(row.getCell(i));
                    setCellBackgroudColor(row.getCell(i), IndexedColors.LEMON_CHIFFON.getIndex());
                }

                d5SIndex = offset;
                row.createCell(d5SIndex).setCellValue("55数据域");
                setCellHoriAndVertCenter(row.getCell(d5SIndex));
                setCellBackgroudColor(row.getCell(d5SIndex), IndexedColors.GREY_25_PERCENT.getIndex());

                row = sheet.createRow(1);
                row.setHeightInPoints(20);
                for (int i = sIndex; i <= eIndex; i++) {
                    row.createCell(i);
                }

                offset = d5SIndex;
                row.createCell(offset).setCellValue("应用密文(9F26)");
                sheet.setColumnWidth(offset++, 19 * 256);

                row.createCell(offset).setCellValue("应用信息数据(9F27)");
                sheet.setColumnWidth(offset++, 18 * 256);

                row.createCell(offset).setCellValue("发卡行应用数据(9F10)");
                sheet.setColumnWidth(offset++, 30 * 256);

                row.createCell(offset).setCellValue("不可预知数(9F37)");
                sheet.setColumnWidth(offset++, 16 * 256);

                row.createCell(offset).setCellValue("应用交易计数器(9F36)");
                sheet.setColumnWidth(offset++, 19 * 256);

                row.createCell(offset).setCellValue("交易日期(9A)");
                sheet.setColumnWidth(offset++, 12 * 256);

                row.createCell(offset).setCellValue("应用版本号(9F09)");
                sheet.setColumnWidth(offset++, 16 * 256);

                row.createCell(offset).setCellValue("交易货币代码(5F2A)");
                sheet.setColumnWidth(offset++, 19 * 256);

                row.createCell(offset).setCellValue("应用交互特征(82)");
                sheet.setColumnWidth(offset++, 16 * 256);

                row.createCell(offset).setCellValue("终端国家代码(9F1A)");
                sheet.setColumnWidth(offset++, 18 * 256);

                row.createCell(offset).setCellValue("其它金额(9F03)");
                sheet.setColumnWidth(offset++, 14 * 256);

                row.createCell(offset).setCellValue("终端性能(9F33)");
                sheet.setColumnWidth(offset++, 14 * 256);

                row.createCell(offset).setCellValue("专用文件名称(84)");
                sheet.setColumnWidth(offset++, 20 * 256);

                row.createCell(offset).setCellValue("交易序列计数器(9F41)");
                sheet.setColumnWidth(offset++, 20 * 256);

                row.createCell(offset).setCellValue("发卡行授权码(9F74)");
                sheet.setColumnWidth(offset++, 18 * 256);

                row.createCell(offset).setCellValue("PAN序列号(5F34)");
                sheet.setColumnWidth(offset++, 15 * 256);

                row.createCell(offset).setCellValue("终端验证结果(95)");
                sheet.setColumnWidth(offset++, 15 * 256);

                row.createCell(offset).setCellValue("授权金额(9F02)");
                sheet.setColumnWidth(offset++, 15 * 256);

                row.createCell(offset).setCellValue("终端类型(9F35)");
                sheet.setColumnWidth(offset++, 15 * 256);

                row.createCell(offset).setCellValue("接口设备序列号(9F1E)");
                sheet.setColumnWidth(offset++, 19 * 256);

                d5EIndex = offset - 1;

                for (int i = sIndex; i <= eIndex; i++) {
                    setCellVertCenter(row.getCell(i));
                    setCellBackgroudColor(row.getCell(i), IndexedColors.LEMON_CHIFFON.getIndex());
                    sheet.addMergedRegion(new CellRangeAddress(0, 1, i, i));
                }

                for (int i = d5SIndex; i <= d5EIndex; i++) {
                    setCellVertCenter(row.getCell(i));
                    setCellBackgroudColor(row.getCell(i), IndexedColors.LIGHT_TURQUOISE.getIndex());
                }

                sheet.addMergedRegion(new CellRangeAddress(0, 0, d5SIndex, d5EIndex));
                saveWorkbook(wb, fileName);
            }
        }
        return wb;
    }

    /**
     * save the workbook data to local file
     * 
     * @param wb
     * @param fileName
     * @return save result 0:OK 1:file is locked 2:other file not found
     *         exception 3:io exception 4: other exception
     */
    public static byte saveWorkbook(Workbook wb, String fileName) {
        byte ret = 0;
        if (wb == null) {
            return -1;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            wb.write(new FileOutputStream(fileName));
        } catch (FileNotFoundException e) {
            String exceptionTip = KKTool.getExceptionTip(e);
            if (exceptionTip.contains("另一个程序正在使用此文件，进程无法访问")) {
                ret = 1;
                logger.info("当前要保存的Excel文件可能被打开或由其他程序占用，无法保存");
            } else {
                ret = 2;
                logger.error("saveWorkbook FileNotFoundException:{}", exceptionTip);
            }
        } catch (IOException e) {
            ret = 3;
            logger.error("saveWorkbook IOException", e);
        } catch (Exception e) {
            ret = 4;
            logger.error("Save Workbook exception", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("saveWorkbook close fos IOException", e);
                }
            }
        }
        return ret;
    }

    public static void setCellHoriLeft(Cell cell) {
        if (cell != null) {
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        }
    }

    public static void setCellHoriCenter(Cell cell) {
        if (cell != null) {
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        }
    }

    public static void setCellHoriRight(Cell cell) {
        if (cell != null) {
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        }
    }

    public static void setCellVertTop(Cell cell) {
        if (cell != null) {
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        }
    }

    public static void setCellVertCenter(Cell cell) {
        if (cell != null) {
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        }
    }

    public static void setCellVertBottom(Cell cell) {
        if (cell != null) {
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
        }
    }

    public static void setCellHoriAndVertCenter(Cell cell) {
        setCellHoriCenter(cell);
        setCellVertCenter(cell);
    }

    public static void setCellBackgroudColor(Cell cell, short color) {
        if (cell != null) {
            CellStyle cellStyle = cell.getRow().getSheet().getWorkbook().createCellStyle();
            cellStyle.cloneStyleFrom(cell.getCellStyle());
            cellStyle.setFillForegroundColor(color);
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            cell.setCellStyle(cellStyle);
        }
    }

    public static void main(String[] args) {
        getWorkbook4Fuzhou("d:/aaa.xls");
    }
}
