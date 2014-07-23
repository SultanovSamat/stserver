package com.jadic.ws;

import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadic.cmd.req.CmdGetMac2Req;
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

    public String getMac2(CmdGetMac2Req cmdReq) {
        //for performance, ignore the xml document building
        String inputXml = "<SVCCONT><CHARGEREQ><TRADETYPECODE>%s</TRADETYPECODE><CARDNO>%s</CARDNO><TERMNO>%s</TERMNO><ASN>%s</ASN><RNDNUMBER2>%s</RNDNUMBER2><CARDTRADENO>%s</CARDTRADENO><CARDOLDBAL>%s</CARDOLDBAL><TRADEMONEY>%s</TRADEMONEY><TRADETYPE>%s</TRADETYPE><KEYVERSION>%s</KEYVERSION><ARITHINDEX>%s</ARITHINDEX><MAC1>%s</MAC1><DEPTNO>%s</DEPTNO><OPERNO>%s</OPERNO><HOSTDATE>%s</HOSTDATE><HOSTTIME>%s</HOSTTIME></CHARGEREQ></SVCCONT>";
        
        String operType = KKTool.byteToHexStr(cmdReq.getOperType());
        String cardNo = KKTool.byteArrayToHexStr(cmdReq.getCardNo());
        String termNo = KKTool.byteArrayToHexStr(cmdReq.getTermNo());
        String asn = KKTool.byteArrayToHexStr(cmdReq.getAsn());
        String randNumber = KKTool.byteArrayToHexStr(cmdReq.getRandNumber());
        String cardTradeNo = KKTool.byteArrayToHexStr(cmdReq.getCardTradNo());
        String cardOldBalance = KKTool.getFixedLenString(String.valueOf(cmdReq.getCardOldBalance()), 10, '0', true);
        String chargeAmount = KKTool.getFixedLenString(String.valueOf(cmdReq.getChargeAmount()), 10, '0', true);
        String tradeType = "02";
        String keyVersion = "01";
        String arithIndex = "00";
        String mac1 = KKTool.byteArrayToHexStr(cmdReq.getMac1());
        String deptNo = KKTool.getFixedLenString("", 10, '0', true);
        String operNo = KKTool.getFixedLenString("", 10, '0', true);
        String chargeDate = KKTool.byteArrayToHexStr(cmdReq.getChargeDate());
        String chargeTime = KKTool.byteArrayToHexStr(cmdReq.getChargeTime());
        
        Object[] args = new String[]{operType, cardNo, termNo, asn, randNumber, cardTradeNo, cardOldBalance, chargeAmount, 
                tradeType, keyVersion, arithIndex, mac1, deptNo, operNo, chargeDate, chargeTime};
        String retXml = centerProcess.callback(String.format(inputXml, args));
        
        try {
            Document document = DocumentHelper.parseText(retXml);
            Node respCodeNode = document.selectSingleNode("//SVCCONT/CHANGERSP/RESPCODE");
            if (respCodeNode != null) {
                String respCode = respCodeNode.getText();
                if (respCode.equals("0000")) {
                    Node mac2Node = document.selectSingleNode("//SVCCONT/CHANGERSP/MAC2");
                    if (mac2Node != null) {
                        log.info("succeed to get mac2:{}", mac2Node.getText());
                        return mac2Node.getText();
                    } else {
                        log.info("valid response for getting mac2, but no mac2 node found");
                    }
                } else {
                    Node errDescNode = document.selectSingleNode("//SVCCONT/CHANGERSP/RESPDESC");
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
    
    public String createXML() {
        Document document = DocumentHelper.createDocument();
        Element charegeReq = document.addElement("SVCCONT").addElement("CHARGEREQ");
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
        String inputXml = "<SVCCONT><CHARGEREQ><TRADETYPECODE>%s</TRADETYPECODE><CARDNO>%s</CARDNO><TERMNO>%s</TERMNO><ASN>%s</ASN><RNDNUMBER2>%s</RNDNUMBER2><CARDTRADENO>%s</CARDTRADENO><CARDOLDBAL>%s</CARDOLDBAL><TRADEMONEY>%s</TRADEMONEY><TRADETYPE>%s</TRADETYPE><KEYVERSION>%s</KEYVERSION><ARITHINDEX>%s</ARITHINDEX><MAC1>%s</MAC1><DEPTNO>%s</DEPTNO><OPERNO>%s</OPERNO><HOSTDATE>%s</HOSTDATE><HOSTTIME>%s</HOSTTIME></CHARGEREQ></SVCCONT>";
        Object[] args = new String[]{"00", "9150020686240037", "112233445566", "00112233445566778899", "12345678", "1234", "0000000012", "0000000050", "02", "01", "00", "12345678", "1234567890", "1234567890", "20140722", "142030"};
        return centerProcess.callback(String.format(inputXml, args));
    }

}
