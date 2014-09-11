package com.jadic.ws;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.cmd.req.CmdGetMac2Req;
import com.jadic.cmd.req.CmdModifyZHBPassReq;
import com.jadic.cmd.req.CmdPrepaidCardCheckReq;
import com.jadic.cmd.req.CmdQueryZHBBalanceReq;
import com.jadic.cmd.rsp.CmdModifyZHBPassRsp;
import com.jadic.cmd.rsp.CmdPrepaidCardCheckRsp;
import com.jadic.cmd.rsp.CmdQueryZHBBalanceRsp;
import com.jadic.utils.Const;
import com.jadic.utils.KKTool;
import com.jadic.utils.SysParams;
import com.jadic.ws.czsmk.CenterProcess;
import com.jadic.ws.czsmk.CenterProcessPortType;

/**
 * wsdl2java for czsmk command:
 * wsdl2java -frontend jaxws21 -d e:\test\czsmk -p com.jadic.ws.czsmk -encoding utf-8 "e:\CenterProcess.wsdl"
 * @author 	Jadic
 * @created 2014-7-17
 */
public final class WSUtil {
    
    private final static Logger log = LoggerFactory.getLogger(WSUtil.class);
    private final static byte RET_FAIL             = 0x00;//失败
    private final static byte RET_OK               = 0x01;//成功
    private final static byte RET_INVALID_PASSWORD = 0x02;//密码错误
    
    private CenterProcessPortType centerProcess;
    
    private final static WSUtil wsUtil = getWsUtil();
    
    public static WSUtil getWsUtil() {
        if (wsUtil == null) {
            synchronized (log) {
                if (wsUtil == null) {
                    return new WSUtil();
                }
            }
        }
        return wsUtil;
    }
    
    private WSUtil(){
        StringBuilder wsdlBuilder = new StringBuilder("http://");
        wsdlBuilder.append(SysParams.getInstance().getCityCardWSIp()).append(":");
        wsdlBuilder.append(SysParams.getInstance().getCityCardWSPort()).append("/");
        wsdlBuilder.append("CenterProcess.wsdl");
        
        try {
            URL url = new URL(wsdlBuilder.toString());
            log.info("url:" + url);
            centerProcess = new CenterProcess(url).getCenterProcess();
        } catch (MalformedURLException e) {
            log.error("WSUtil create url err", e);
        }
    }
    
//    private WSUtil() {
//        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
//        factoryBean.setServiceClass(CenterProcessPortType.class);
//        factoryBean.setAddress("http://10.0.4.112:9900/CenterProcess.wsdl");
//        CenterProcessPortType centerProcess = (CenterProcessPortType)factoryBean.create();
//        
//        Client proxy = ClientProxy.getClient(centerProcess);
//        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
//        HTTPClientPolicy policy = new HTTPClientPolicy();
//        policy.setConnectionTimeout(10000); //连接超时时间
//        policy.setReceiveTimeout(120000);//请求超时时间.
//        conduit.setClient(policy);
//    }

    public String getMac2(CmdGetMac2Req cmdReq) {
        //for performance, ignore the xml document building
        String inputXml = Const.WS_XML_GET_MAC2;
        String origDomain = "A1";
        String homeDomain = "01";
        String biPCode = "0004";
        String actionCode = "0";
        String transId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String procId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String operType = KKTool.byteToHexStr(cmdReq.getOperType());
        String cardNo = KKTool.byteArrayToHexStr(cmdReq.getCardNo());
        String termNo = KKTool.byteArrayToHexStr(cmdReq.getTermNo());
        String asn = KKTool.byteArrayToHexStr(cmdReq.getAsn());
        String randNumber = KKTool.byteArrayToHexStr(cmdReq.getRandNumber());
        String cardTradeNo = KKTool.byteArrayToHexStr(cmdReq.getCardTradNo());
        String cardOldBalance = KKTool.getStrWithMaxLen(String.valueOf(cmdReq.getCardOldBalance()), 10, false);
        String chargeAmount = KKTool.getStrWithMaxLen(String.valueOf(cmdReq.getChargeAmount()), 10, false);
        String tradeType = "02";
        String keyVersion = "01";
        String arithIndex = "00";
        String mac1 = KKTool.byteArrayToHexStr(cmdReq.getMac1());
        String deptNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getAgencyNo(), 10, false);
        String operNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getOperNo(), 10, false);
        String chargeDate = KKTool.byteArrayToHexStr(cmdReq.getChargeDate());
        String chargeTime = KKTool.byteArrayToHexStr(cmdReq.getChargeTime());
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, operType, 
                cardNo, termNo, asn, randNumber, cardTradeNo, cardOldBalance, chargeAmount, 
                tradeType, keyVersion, arithIndex, mac1, deptNo, operNo, chargeDate, chargeTime};
        String input = String.format(inputXml, args);
        log.info("get mac2 input:\n{}", input);
        if (input != null) {
            return "12345678";
        }
        String retXml = centerProcess.callback(input);
        
        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVC/SVCCONT/CHANGERSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    Node mac2Node = document.selectSingleNode("//SVC/SVCCONT/CHANGERSP/MAC2");
                    if (mac2Node != null) {
                        log.info("succeed to get mac2:{}", mac2Node.getText());
                        return mac2Node.getText();
                    } else {
                        log.info("valid response for getting mac2, but no mac2 node found");
                    }
                } else {
                    Node errDescNode = document.selectSingleNode("//SVC/SVCCONT/CHANGERSP/RESPDESC");
                    log.info("fail to get mac2, respCode:{}, desc:{}", respCode, errDescNode != null ? errDescNode.getText() : "no desc");
                }
            } else {
                log.info("invalid response for getting mac2");
            }
        } catch (DocumentException e) {
            log.info("getMac2 parse xml err", e);
        }
        return "";
    }
    
    public void checkPrepaidCard(CmdPrepaidCardCheckReq cmdReq, CmdPrepaidCardCheckRsp cmdRsp) {
        String inputXml = Const.WS_XML_PREPAID_CARD_CHECK;
        
        String origDomain = "A1";
        String homeDomain = "01";
        String biPCode = "0004";
        String actionCode = "0";
        String transId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String procId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String tradeTypeCode = "00";
        String cardNo = KKTool.byteArrayToHexStr(cmdReq.getCityCardNo());
        String password = KKTool.byteArrayToHexStr(cmdReq.getPassword());
        String deptNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getAgencyNo(), 10, false);
        String operNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getOperNo(), 10, false);
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, 
                                     tradeTypeCode, cardNo, password, deptNo, operNo};
        String retXml = centerProcess.callback(String.format(inputXml, args));
        
        cmdRsp.setCheckRet(RET_FAIL);
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

    public void queryZHBBalance(CmdQueryZHBBalanceReq cmdReq, CmdQueryZHBBalanceRsp cmdRsp) {
        String inputXml = Const.WS_XM_GET_ZHB_BALANCE;
        
        String origDomain = "A1";
        String homeDomain = "01";
        String biPCode = "0004";
        String actionCode = "0";
        String transId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String procId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String tradeTypeCode = "00";
        String cardNo = KKTool.byteArrayToHexStr(cmdReq.getCityCardNo());
        String password = KKTool.byteArrayToHexStr(cmdReq.getPassword());
        String deptNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getAgencyNo(), 10, false);
        String operNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getOperNo(), 10, false);
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, 
                tradeTypeCode, cardNo, password, deptNo, operNo};
        String retXml = centerProcess.callback(String.format(inputXml, args));
        
        cmdRsp.setCheckRet(RET_FAIL);
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
    
    public void modifyZHBPassword(CmdModifyZHBPassReq cmdReq, CmdModifyZHBPassRsp cmdRsp) {
        //for performance, ignore the xml document building
        String inputXml = Const.WS_XML_MODIFY_ZHB_PASS;
        String origDomain = "A1";
        String homeDomain = "01";
        String biPCode = "0004";
        String actionCode = "0";
        String transId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String procId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
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
        String keyVersion = "01";
        String arithIndex = "00";
        String mac1 = KKTool.byteArrayToHexStr(cmdReq.getMac1());
        String deptNo = KKTool.getStrWithMaxLen("", 10, false);
        String operNo = KKTool.getStrWithMaxLen("", 10, false);
        String chargeDate = KKTool.byteArrayToHexStr(cmdReq.getChargeDate());
        String chargeTime = KKTool.byteArrayToHexStr(cmdReq.getChargeTime());
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, operType, 
                cardNo, oldPass, newPass, termNo, asn, randNumber, cardTradeNo, cardOldBalance, chargeAmount, 
                tradeType, keyVersion, arithIndex, mac1, chargeDate, chargeTime, deptNo, operNo};
        String retXml = centerProcess.callback(String.format(inputXml, args));
        
        cmdRsp.setRet(RET_FAIL);
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
                KKTool.getFixedLenString("", 30, '0', true), 
                KKTool.getFixedLenString("", 30, '0', true),
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
        
        String origDomain = "02";
        String homeDomain = "01";
        String biPCode = "0004";
        String actionCode = "0";
        String transId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String procId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String tradeTypeCode = "00";
        String cardNo = "9150020686240037";
        String password = "1234567890123456";
        String deptNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getAgencyNo(), 10, false);
        String operNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getOperNo(), 10, false);
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, 
                                     tradeTypeCode, cardNo, password, deptNo, operNo};
        String retXml = centerProcess.callback(String.format(inputXml, args));
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
        
        String origDomain = "02";
        String homeDomain = "01";
        String biPCode = "0004";
        String actionCode = "0";
        String transId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String procId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String processTime = KKTool.getCurrFormatDate("yyyyMMddHHmmss");
        String tradeTypeCode = "00";
        String cardNo = "9150020686240037";
        String password = "123456";
        String deptNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getAgencyNo(), 10, false);
        String operNo = KKTool.getStrWithMaxLen(SysParams.getInstance().getOperNo(), 10, false);
        
        Object[] args = new String[]{origDomain, homeDomain, biPCode, actionCode, transId, procId, processTime, 
                tradeTypeCode, cardNo, password, deptNo, operNo};
        String retXml = centerProcess.callback(String.format(inputXml, args));
        
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
        String origDomain = "02";
        String homeDomain = "01";
        String biPCode = "0004";
        String actionCode = "0";
        String transId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
        String procId = KKTool.getCurrFormatDate("yyyyMMddHHmmssSSS");
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
        String keyVersion = "01";
        String arithIndex = "00";
        String mac1 = "12345678";
        String deptNo = KKTool.getStrWithMaxLen("", 10, false);
        String operNo = KKTool.getStrWithMaxLen("", 10, false);
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
        log.info("test start");
        WSUtil wsUtil = WSUtil.getWsUtil();
        log.info(wsUtil.testGetMac2());
//        wsUtil.testPrepaidCardCheck();
//        wsUtil.testQueryZHBBalance();
//        wsUtil.testModifyZHBPass();
        log.info("test end");
    }
    
}
