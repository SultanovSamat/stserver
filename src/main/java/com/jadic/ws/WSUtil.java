package com.jadic.ws;

import com.jadic.cmd.req.CmdGetMac2Req;
import com.jadic.cmd.req.CmdModifyZHBPassReq;
import com.jadic.cmd.req.CmdPrepaidCardCheckReq;
import com.jadic.cmd.req.CmdQueryZHBBalanceReq;
import com.jadic.cmd.rsp.CmdGetMac2Rsp;
import com.jadic.cmd.rsp.CmdModifyZHBPassRsp;
import com.jadic.cmd.rsp.CmdPrepaidCardCheckRsp;
import com.jadic.cmd.rsp.CmdQueryZHBBalanceRsp;
import com.jadic.utils.Const;
import com.jadic.utils.KKTool;
import com.jadic.utils.SysParams;
import com.jadic.ws.czsmk.CenterProcess;
import com.jadic.ws.czsmk.CenterProcessPortType;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * wsdl2java for czsmk command:
 * wsdl2java -frontend jaxws21 -d e:\test\czsmk -p com.jadic.ws.czsmk -encoding utf-8 "e:\CenterProcess.wsdl"
 * @author 	Jadic
 * @created 2014-7-17
 * 
 * 2015-07-28
 * 1.为实现分多个服务同时正常工作，增加每个服务代号（0-9），占用流水号中的1位，防止流水号冲突
 */
public final class WSUtil {
    
    private final static Logger log = LoggerFactory.getLogger(WSUtil.class);
    private final static byte RET_FAIL             = 0x00;//失败
    private final static byte RET_OK               = 0x01;//成功
    private final static byte RET_INVALID_PASSWORD = 0x02;//密码错误
    
    private final static String SNO_PREFIX = "ZZZD";//流水号、交易号的前缀 (自助终端的缩写)
    
    private final static long MAX_SNO = 99999L;
    private ExecutorService threadPool;
    private final static String SNO_FILE_NAME = "sNo4CityCardWS.txt";
    private long sNo = 0;
    private String currDate = KKTool.getCurrFormatDate("yyMMdd");
    
    private final static String origDomain = "Z1";
    private final static String homeDomain = "01";
    //private final static String biPCode = "0004";
    private final static String actionCode = "0";
    private final static String keyVersion = "01";
    private final static String arithIndex = "00";
    private final static String deptNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getAgencyNo(), 10, false);
    private final static String operNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getOperNo(), 10, false);    
    private final static String SAMID = KKTool.getFixedLenString(SysParams.getInstance().getSamId(), 12, '0', true, false);
    private final static int SERVER_NO = SysParams.getInstance().getServerNo();//本服务分多个部署时，区分不同的服务，并在流水号中体现
    private final static int ACC_DIGIT = 5;//实际用来累加流水号的数字位数
    
    private URL url;
    private CenterProcessPortType centerProcess;
    
    private static WSUtil wsUtil = null;
    
    public static WSUtil getWsUtil() {
        if (wsUtil == null) {
            synchronized (log) {
                if (wsUtil == null) {
                    wsUtil = new WSUtil();
                }
            }
        }
        return wsUtil;
    }
    
    private WSUtil(){
    	initSNoFromFile();
        StringBuilder wsdlBuilder = new StringBuilder("http://");
        wsdlBuilder.append(SysParams.getInstance().getCityCardWSIp()).append(":");
        wsdlBuilder.append(SysParams.getInstance().getCityCardWSPort()).append("/");
        wsdlBuilder.append("CenterProcess.wsdl").append("?wsdl");
		try {
			url = new URL(wsdlBuilder.toString());
		} catch (MalformedURLException e) {
			log.error("new Url err", e);
		}
        log.info("url:" + url);
        createServiceClient();
        
        threadPool = Executors.newSingleThreadExecutor();
    }
    
    private void createServiceClient() {
        if (centerProcess != null) {
            return ;
        }
    	try {
    	    log.info("create WS client, url:{}", url);
            centerProcess = new CenterProcess(url).getCenterProcess();
//            Map<String, Object> requestContext = ((BindingProvider)centerProcess).getRequestContext();
//            requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, 5000);
//            requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT, 2000);
        } catch (Exception e) {
			log.error("create WSUtil err:", e);
			centerProcess = null;
		}
    }
    
    private boolean isServiceClientOK() {
    	if (centerProcess != null) {
    		return true;
    	}
    	createServiceClient();
    	
    	return centerProcess != null;
    }
    
    private long getNextSNo() {
    	sNo ++;
        if (sNo > MAX_SNO) {
        	sNo = 0;
        }    	
    	updateFileNextSNo();
    	return sNo;
    }
    
    /**
     * 初次启动时从文件中获取
     */
    private void initSNoFromFile() {
    	if (this.sNo > 0) {
    		return;
    	}
    	this.sNo = -1;
    	this.currDate = KKTool.getCurrFormatDate("yyMMdd");
        KKTool.createFileDir(Const.INITIAL_DATA_DIR);
        File file = new File(Const.INITIAL_DATA_DIR, SNO_FILE_NAME);
        if (file.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String dateSNo = reader.readLine().trim();
                if (dateSNo != null && dateSNo.length() == 12) {
                    String date = dateSNo.substring(0, 6);
                    //文件中记录的日期没有超过当前日期，则获取流水号，否则流水号从1开始重新记
                    if (this.currDate.compareTo(date) == 0) {
                        this.sNo = Long.parseLong(dateSNo.substring(12 - ACC_DIGIT));
                        if (this.sNo > MAX_SNO) {
                            this.sNo = 1;
                        }            
                    }
                }
            } catch (FileNotFoundException e) {
                log.error("initSNoFromFile", e);
            } catch (IOException e) {
                log.error("initSNoFromFile", e);
            } catch (Exception e) {
                log.error("initSNoFromFile", e);
            } finally {
                KKTool.closeReaderInSilence(reader);
            }
        } else {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(this.currDate + SERVER_NO + KKTool.getFixedLenString(String.valueOf(sNo), ACC_DIGIT, '0', true));
                writer.flush();
            } catch (IOException e) {
                log.error("initSNoFromFile", e);
            } finally {
                KKTool.closeWriterInSilence(writer);
            }

        }
    }
    
    /**
     * 更新文件批次号到文件中，防止程序终止
     */
    private void updateFileNextSNo() {
        final String date = this.currDate;
        threadPool.execute(new Runnable() {
			@Override
			public void run() {
		        KKTool.createFileDir(Const.INITIAL_DATA_DIR);
		        File file = new File(Const.INITIAL_DATA_DIR, SNO_FILE_NAME);
		        BufferedWriter writer = null;
		        try {
		            writer = new BufferedWriter(new FileWriter(file));
		            writer.write(date + SERVER_NO + KKTool.getFixedLenString(String.valueOf(sNo), ACC_DIGIT, '0', true));
		            writer.flush();
		        } catch (IOException e) {
		            log.error("updateFileNextSNo", e);
		        } finally {
		            KKTool.closeWriterInSilence(writer);
		        }
			}
		});
    }

    private String getNextTransId() {
        String date = KKTool.getCurrFormatDate("yyMMdd");
        if (date.compareTo(this.currDate) > 0) {
            this.currDate = date;
        }
    	return SNO_PREFIX + date + SERVER_NO + KKTool.getFixedLenString(String.valueOf(getNextSNo()), ACC_DIGIT, '0', true);
    }
    
    /**
     * 所有ws调用统一入口
     * @param inputXml
     * @return
     */
    private String callService(String inputXml) {
        String retXml = null;
        if (isServiceClientOK() && centerProcess != null) {
            try {
                retXml = centerProcess.callback(inputXml);
                log.debug("callService succ, input:{}, output:{}", inputXml, retXml);
            } catch (Exception e){
                log.error("call service err, input:{}", inputXml, e);
                centerProcess = null;
            }
        }
        return retXml;
    }

    /**
     * 检测市民卡种类  0xFF:未知  00:记名卡  01:不记名卡 02:无效卡 03:金福卡
     * @param cityCardNo
     * @return
     */
    public byte checkCityCardType(String cityCardNo) {
        byte type = Const.CITY_CARD_TYPE_UNKNOWN;
        if (!KKTool.isStrNullOrBlank(cityCardNo)) {
            String inputXml = Const.WS_XML_CHECK_CITY_CARD_TYPE;
            String biPCode = Const.BIPCODE_GET_CITY_CARD_TYPE;
            String transId = getNextTransId();
            String procId = transId;
            String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
            Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode,
                                         transId, procId, processTime, cityCardNo};

            String input = String.format(inputXml, args);
            String retXml = callService(input);
            if (retXml != null) {
                try {
                    Document document = DocumentHelper.parseText(retXml);
                    Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/CUSTRECTYPEQUERYRSP/CUSTRECTYPE");
                    if (respCodeNode != null) {//0000:记名卡 0001:不记名卡 0002:卡状态异常或此卡不存在 0003:金福卡
                        String respCode = respCodeNode.getText();
                        try {
                            type = Byte.parseByte(respCode);
                        } catch (Exception e) {
                            type = Const.CITY_CARD_TYPE_INVALID;
                        }
                        
//                        if (respCode.equals(Const.S_CITY_CARD_TYPE_NAMED)) {
//                            type = Const.CITY_CARD_TYPE_NAMED;//记名卡
//                        } else if (respCode.equals(Const.S_CITY_CARD_TYPE_UNNAMED)) {
//                            type = Const.CITY_CARD_TYPE_UNNAMED;//不记名卡
//                        } else if (respCode.equals(Const.S_CITY_CARD_TYPE_INVALID)){
//                            type = Const.CITY_CARD_TYPE_INVALID;//无效卡
//                        } else if (respCode.equals(Const.S_CITY_CARD_TYPE_JFCARD)) {
//                            type = Const.CITY_CARD_TYPE_JFCARD;//金福卡
//                        }
                    } else {
                        log.info("invalid response for check city card type");
                    }
                } catch (DocumentException e) {
                    log.info("getMac2 parse xml err", e);
                }
            }
        }
        return type;//默认未知卡
    }
    
    /**
     * 获取mac2<br>
     * 调整成功后，加上12位流水号<br>
     * 若有相关错误提示返回至客户端<br>
     * @param cmdReq
     * @return
     */
    public void getMac2(CmdGetMac2Req cmdReq, CmdGetMac2Rsp cmdRsp) {
        if (cmdReq == null || cmdRsp == null) {
            return ;
        }
        cmdRsp.setRet(RET_FAIL);
        
        //for performance, ignore the xml document building
        String inputXml = Const.WS_XML_GET_MAC2;
        String biPCode = Const.BIPCODE_GET_MAC2;
        String transId = null;
        if (cmdReq.isFirstReq()) {
            transId = getNextTransId();
        } else {
            transId = SNO_PREFIX + KKTool.byteArrayToHexStr(cmdReq.getTranSNo());
        }
        String procId = transId;
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String operType = KKTool.byteToHexStr(cmdReq.getOperType());
        String cardNo = KKTool.byteArrayToHexStr(cmdReq.getCardNo());
        String password = new String(cmdReq.getPassword()).trim();
        String termNo = SAMID;//KKTool.byteArrayToHexStr(cmdReq.getTermNo());
        String asn = KKTool.byteArrayToHexStr(cmdReq.getAsn());
        String randNumber = KKTool.byteArrayToHexStr(cmdReq.getRandNumber());
        String cardTradeNo = KKTool.byteArrayToHexStr(cmdReq.getCardTradNo());
        String cardOldBalance = KKTool.getStrWithMaxLen(String.valueOf(cmdReq.getCardOldBalance()), 10, false);
        String chargeAmount = KKTool.getStrWithMaxLen(String.valueOf(cmdReq.getChargeAmount()), 10, false);
        String tradeType = "02";
        String mac1 = KKTool.byteArrayToHexStr(cmdReq.getMac1());
        String chargeDate = KKTool.byteArrayToHexStr(cmdReq.getChargeDate());
        String chargeTime = KKTool.byteArrayToHexStr(cmdReq.getChargeTime());
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, operType, 
                cardNo, password, termNo, asn, randNumber, cardTradeNo, cardOldBalance, chargeAmount, 
                tradeType, keyVersion, arithIndex, mac1, deptNo, operNo, chargeDate, chargeTime};
        String input = String.format(inputXml, args);
        
        String retXml = callService(input);
        
        if (retXml == null) {
            return ;
        }
        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/CHANGERSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    Node mac2Node = document.selectSingleNode("//SVC/SVCCONT/CHANGERSP/MAC2");
                    if (mac2Node != null) {
                        String sMac2 = mac2Node.getText();
                        byte[] mac2 = KKTool.strToHexBytes(sMac2, 4, 'F');
                        cmdRsp.setMac2(mac2);
                        cmdRsp.setTranSNo(KKTool.strToHexBytes(transId.substring(4), 6, '0'));
                        cmdRsp.setRet((byte) 1);
                        log.info("succeed to get mac2:{}", mac2Node.getText());
                    } else {
                        log.info("valid response for getting mac2, but no mac2 node found");
                    }
                } else {
                    Node errDescNode = document.selectSingleNode("//SVC/SVCCONT/CHANGERSP/RESPDESC");
                    if (errDescNode != null) {
                        String errTip = errDescNode.getText();
                        int startIndex = errTip.indexOf("ErrMsg:");
                        if (startIndex >= 0) {//Calling Local Service Failed. ErrMsg:A094780516:原余额加充值金额不能大于5000元. The Service Id:0004
                            int endIndex = errTip.indexOf("The Service Id");
                            if (endIndex - 7 > startIndex) {
                                errTip = errTip.substring(startIndex + 7, endIndex);
                                cmdRsp.setErrTip(errTip.getBytes());
                            }
                        }
                    }
                    log.info("fail to get mac2, respCode:{}, desc:{}", respCode, errDescNode != null ? errDescNode.getText() : "no desc");
                }
            } else {
                log.info("invalid response for getting mac2");
            }
        } catch (DocumentException e) {
            log.info("getMac2 parse xml err", e);
        }
    }

    /**
     * 校验充值卡面额
     * @param cmdReq
     * @param cmdRsp
     */
    public void checkPrepaidCard(CmdPrepaidCardCheckReq cmdReq, CmdPrepaidCardCheckRsp cmdRsp) {
        if (cmdReq == null || cmdRsp == null) {
            return;
        }
        cmdRsp.setCheckRet(RET_FAIL);
        
        String inputXml = Const.WS_XML_PREPAID_CARD_CHECK;
        String biPCode = Const.BIPCODE_CHECK_PREPAID_CARD;
        String transId = getNextTransId();
        String procId = transId;
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String tradeTypeCode = "00";
        String cardNo = KKTool.byteArrayToHexStr(cmdReq.getCityCardNo());
        String password = KKTool.byteArrayToHexStr(cmdReq.getPassword());
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, 
                                     tradeTypeCode, cardNo, password, deptNo, operNo};
        
        String retXml = callService(String.format(inputXml, args));
        if (retXml == null) {
        	return ;
        }
        
        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/CARDVERIFYRSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    cmdRsp.setCheckRet(RET_OK);
                    Node amountNode = document.selectSingleNode("//SVC/SVCCONT/CARDVERIFYRSP/CARDMONEY");
                    if (amountNode != null) {
                        cmdRsp.setAmount(Integer.parseInt(amountNode.getText()));
                        log.debug("succeed to get amount:{}", amountNode.getText());
                    } else {
                        log.info("valid CARDVERIFYRSP, but no amount node found");
                    }
                } else if (respCode.equals("0001")) {
                    cmdRsp.setCheckRet(RET_INVALID_PASSWORD);
                } else {
                    Node errDescNode = document.selectSingleNode("//SVC/SVCCONT/CHANGERSP/RESPDESC");
                    log.info("fail to check prepaid card, respCode:{}, desc:{}", respCode, errDescNode != null ? errDescNode.getText() : "no desc");
                }
            } else {
                log.info("invalid response for checking prepaid card");
            }
        } catch (DocumentException e) {
            log.info("check prepaid card parse xml err", e);
        } catch (Exception e) {
            log.error("checkPrepaidCard err", e);
        }
    }

    /**
     * 查询账户宝余额
     * @param cmdReq
     * @param cmdRsp
     */
    public void queryZHBBalance(CmdQueryZHBBalanceReq cmdReq, CmdQueryZHBBalanceRsp cmdRsp) {
    	if (cmdReq == null || cmdRsp == null) {
    	    return;
    	}
    	cmdRsp.setCheckRet(RET_FAIL);
    	
        String inputXml = Const.WS_XM_GET_ZHB_BALANCE;
        String biPCode = Const.BIPCODE_QUERY_ZHB_BALANCE;
        String transId = getNextTransId();
        String procId = transId;
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String tradeTypeCode = "00";
        String cardNo = KKTool.byteArrayToHexStr(cmdReq.getCityCardNo());
        String password = KKTool.byteArrayToHexStr(cmdReq.getPassword());
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, 
                tradeTypeCode, cardNo, password, deptNo, operNo};
        String input = String.format(inputXml, args);
        
        String retXml = callService(input);
        if (retXml == null) {
        	return;
        }

        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/GROUPQUERYRSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    cmdRsp.setCheckRet(RET_OK);
                    Node amountNode = document.selectSingleNode("//SVC/SVCCONT/GROUPQUERYRSP/GROUPACCOUNT");
                    if (amountNode != null) {
                        cmdRsp.setAmount(Integer.parseInt(amountNode.getText()));
                        log.debug("succeed to get amount:{}", amountNode.getText());
                    } else {
                        log.info("valid GROUPQUERYRSP, but no amount node found");
                    }
                } else if (respCode.equals("0001")) {
                    cmdRsp.setCheckRet(RET_INVALID_PASSWORD);
                } else {
                    Node errDescNode = document.selectSingleNode("//SVC/SVCCONT/GROUPQUERYRSP/RESPDESC");
                    log.info("fail to query zhb balance, respCode:{}, desc:{}", respCode, errDescNode != null ? errDescNode.getText() : "no desc");
                }
            } else {
                log.info("invalid response for querying zhb balance");
            }
        } catch (DocumentException e) {
            log.info("query zhb balance parse xml err", e);
        } catch (Exception e) {
            log.error("query zhb balance err", e);
        }
    }

    /**
     * 修改账户宝密码
     * @param cmdReq
     * @param cmdRsp
     */
    public void modifyZHBPassword(CmdModifyZHBPassReq cmdReq, CmdModifyZHBPassRsp cmdRsp) {
        if (cmdReq == null || cmdRsp == null) {
            return;
        }
        cmdRsp.setRet(RET_FAIL);
        
        //for performance, ignore the xml document building
        String inputXml = Const.WS_XML_MODIFY_ZHB_PASS;
        String biPCode = Const.BIPCODE_MODIFY_ZHB_PASSWORD;
        String transId = getNextTransId();
        String procId = transId;
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String operType = "00";
        String cardNo = KKTool.byteArrayToHexStr(cmdReq.getCardNo());
        String termNo = KKTool.byteArrayToHexStr(cmdReq.getTermNo());
        String oldPass = KKTool.byteArrayToHexStr(cmdReq.getOldPass());
        String newPass = KKTool.byteArrayToHexStr(cmdReq.getNewPass());
        String asn = KKTool.byteArrayToHexStr(cmdReq.getAsn());
        String randNumber = KKTool.byteArrayToHexStr(cmdReq.getRandNumber());
        String cardTradeNo = KKTool.byteArrayToHexStr(cmdReq.getCardTradNo());
        String cardOldBalance = KKTool.getStrWithMaxLen(String.valueOf(cmdReq.getCardOldBalance()), 10, false);
        String chargeAmount = "0";
        String tradeType = "02";

        String mac1 = KKTool.byteArrayToHexStr(cmdReq.getMac1());

        String chargeDate = KKTool.byteArrayToHexStr(cmdReq.getChargeDate());
        String chargeTime = KKTool.byteArrayToHexStr(cmdReq.getChargeTime());
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, operType, 
                cardNo, oldPass, newPass, termNo, asn, randNumber, cardTradeNo, cardOldBalance, chargeAmount, 
                tradeType, keyVersion, arithIndex, mac1, chargeDate, chargeTime, deptNo, operNo};

        String retXml = callService(String.format(inputXml, args));
        if (retXml == null) {
        	return;
        }
        
        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/ACCCHANGEPWDRSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    cmdRsp.setRet(RET_OK);
                } else if (respCode.equals("0001")){
                    cmdRsp.setRet(RET_INVALID_PASSWORD);
                }
                log.warn("modify zhb pass, respcode:{}", respCode);
            } else {
                log.info("invalid response for modifying zhb pass");
            }
        } catch (DocumentException e) {
            log.info("modify zhb pass parse xml err", e);
        }
    }
    
    public static String createXML() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("SVC");
        Element head = root.addElement("SVCHEAD");
        addSonElement(head, "ORIGDOMAIN", "");
        addSonElement(head, "HOMEDOMAIN", "");
        addSonElement(head, "BIPCODE", "0004");
        addSonElement(head, "ACTIONCODE", "");
        addSonElement(head, "PROCID", "");
        addSonElement(head, "PROCESSTIME", "");
        
        Element content = root.addElement("SVCCONT");
        Element charegeReq = content.addElement("CHARGEREQ");
        addSonElement(charegeReq, "TRADETYPECODE", "00");
        addSonElement(charegeReq, "CARDNO", "");
        addSonElement(charegeReq, "TERMNO", "");
        addSonElement(charegeReq, "ASN", "");
        addSonElement(charegeReq, "RNDNUMBER2", "");
        addSonElement(charegeReq, "CARDTRADENO", "");
        addSonElement(charegeReq, "CARDOLDBAL", "");
        addSonElement(charegeReq, "TRADEMONEY", "");
        addSonElement(charegeReq, "TRADETYPE", "02");
        addSonElement(charegeReq, "KEYVERSION", "01");
        addSonElement(charegeReq, "ARITHINDEX", "00");
        addSonElement(charegeReq, "MAC1", "");
        addSonElement(charegeReq, "DEPTNO", "");
        addSonElement(charegeReq, "OPERNO", "");
        addSonElement(charegeReq, "HOSTDATE", "");
        addSonElement(charegeReq, "HOSTTIME", "");
        return document.asXML();
    }
    
    private static void addSonElement(Element parent, String sonElementName, String sonElementVal) {
        if (parent == null) {
            return;
        }
        
        parent.addElement(sonElementName).addText(sonElementVal);
    }
    
    public String testGetMac2() {
        String inputXml = Const.WS_XML_GET_MAC2;
        Object[] args = new String[]{"Y1", "01", 
                "0004", "0", 
                "YDKQ900000000001", 
                "YDKQ900000000001",
                KKTool.getCurrFormatDate("yyyyMMddHHmmss"), 
                "00", "9150020686240037", 
                "112233445566", "86000091500086240037", "F196CA64", 
                "000B", "70495", 
                "10000", "02", 
                "01", "00", 
                "92FA645A", "9998", "9998", 
                "20140911", "154115"};
        //return String.format(inputXml, args);
        String input = String.format(inputXml, args);
        log.info("testMac2 input\n{}", input);
        return centerProcess.callback(input);
    }

    public void testPrepaidCardCheck() {
        String inputXml = Const.WS_XML_PREPAID_CARD_CHECK;
        String biPCode = "0011";
        String transId = getNextTransId();
        String procId = transId;
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String tradeTypeCode = "00";
        String cardNo = "9150020686240037";
        String password = "0031256112212864";
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, 
                                     tradeTypeCode, cardNo, password, deptNo, operNo};
        String input = String.format(inputXml, args);
        log.info("input:\n{}", input);
        String retXml = centerProcess.callback(input);
        log.info("prepaidcard xml ret:" + retXml);
        
        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/CARDVERIFYRSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    log.info("respCode:0000");
                    Node amountNode = document.selectSingleNode("//SVC/SVCCONT/CARDVERIFYRSP/CARDMONEY");
                    if (amountNode != null) {
                        log.info("succeed to get amount:{}", amountNode.getText());
                    } else {
                        log.info("valid CARDVERIFYRSP, but no amount node found");
                    }
                } else if (respCode.equals("0001")) {
                    log.info("respCode:0001");
                } else {
                    Node errDescNode = document.selectSingleNode("//SVC/SVCCONT/CHANGERSP/RESPDESC");
                    log.info("fail to check prepaid card, respCode:{}, desc:{}", respCode, errDescNode != null ? errDescNode.getText() : "no desc");
                }
            } else {
                log.info("invalid response for checking prepaid card");
            }
        } catch (DocumentException e) {
            log.info("check prepaid card parse xml err", e);
        } catch (Exception e) {
            log.error("checkPrepaidCard err", e);
        }    
    }
    
    public void testQueryZHBBalance() {
        String inputXml = Const.WS_XM_GET_ZHB_BALANCE;
        String biPCode = "0007";
        String transId = getNextTransId();
        String procId = transId;
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String tradeTypeCode = "00";
        String cardNo = "9150020686240037";
        String password = "111111";
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, 
                tradeTypeCode, cardNo, password, deptNo, operNo};
        String input = String.format(inputXml, args);
        log.info("input:\n{}", input);
        String retXml = centerProcess.callback(input);
        
        log.info("testQueryZHBBalance xml ret:" + retXml);
        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/GROUPQUERYRSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    log.info("respCode:0000");
                    Node amountNode = document.selectSingleNode("//SVC/SVCCONT/GROUPQUERYRSP/GROUPACCOUNT");
                    if (amountNode != null) {
                        log.info("succeed to get amount:{}", amountNode.getText());
                    } else {
                        log.info("valid GROUPQUERYRSP, but no amount node found");
                    }
                } else if (respCode.equals("0001")) {
                    log.info("respCode:0001");
                } else {
                    Node errDescNode = document.selectSingleNode("//SVC/SVCCONT/GROUPQUERYRSP/RESPDESC");
                    log.info("fail to query zhb balance, respCode:{}, desc:{}", respCode, errDescNode != null ? errDescNode.getText() : "no desc");
                }
            } else {
                log.info("invalid response for querying zhb balance");
            }
        } catch (DocumentException e) {
            log.info("query zhb balance parse xml err", e);
        } catch (Exception e) {
            log.error("query zhb balance err", e);
        }    
    }
    
    public void testModifyZHBPass() {
        String inputXml = Const.WS_XML_MODIFY_ZHB_PASS;
        String biPCode = "0012";
        String transId = getNextTransId();
        String procId = transId;
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String operType = "00";
        String cardNo = "9150020686240037";
        String termNo = "112233445566";
        String oldPass = "123456";
        String newPass = "234567";
        String asn = "00112233445566778899";
        String randNumber = "12345678";
        String cardTradeNo = "1234";
        String cardOldBalance = "12";
        String chargeAmount = "0";
        String tradeType = "02";
        String mac1 = "12345678";
        String chargeDate = KKTool.getCurrFormatDate("yyyyMMdd");
        String chargeTime = KKTool.getCurrFormatDate("HHmmss");
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, operType, 
                cardNo, oldPass, newPass, termNo, asn, randNumber, cardTradeNo, cardOldBalance, chargeAmount, 
                tradeType, keyVersion, arithIndex, mac1, chargeDate, chargeTime, deptNo, operNo};
        String retXml = centerProcess.callback(String.format(inputXml, args));
        
        log.info("testModifyZHBPass xml ret:" + retXml);
        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/ACCCHANGEPWDRSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    log.info("respCode:0000");
                } else if (respCode.equals("0001")){
                    log.info("respCode:0001");
                }
                log.info("modify zhb pass, respcode:{}", respCode);
            } else {
                log.info("invalid response for modifying zhb pass");
            }
        } catch (DocumentException e) {
            log.info("modify zhb pass parse xml err", e);
        }    
    }
    
    public static void main(String[] arg) {
        WSUtil wsUtil = WSUtil.getWsUtil();
        wsUtil.checkCityCardType("123");
    }
    
}
