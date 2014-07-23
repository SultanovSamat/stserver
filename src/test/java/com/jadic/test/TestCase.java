package com.jadic.test;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 	Jadic
 * @created 2014-7-22
 */
public class TestCase {
    
    private final Logger log = LoggerFactory.getLogger(TestCase.class);

    @Test
    public void testA() {
        try {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SVCCONT><CHANGERSP><HOMEDOMAIN>00</HOMEDOMAIN><RESPCODE>0000</RESPCODE><RESPDESC>112233445566</RESPDESC><MAC2>00112233445566778899</MAC2></CHANGERSP></SVCCONT>";
            Document document = DocumentHelper.parseText(xml);
            Node respCodeNode = document.selectSingleNode("//SVCCONT/CHANGERSP/RESPCODE");
            if (respCodeNode != null && respCodeNode.getText().equals("0000")) {
                Node mac2Node = document.selectSingleNode("//SVCCONT/CHANGERSP/MAC2");
                log.info("mac2:{}", mac2Node.getText());
            } else {
                Node errDescNode = document.selectSingleNode("//SVCCONT/CHANGERSP/RESPDESC");
                log.info("errDesc:{}", errDescNode.getText());
            }
            
//            log.info(respCodeNode.getText());
//            Element root = document.getRootElement();
//            log.info(root.elementText(new QName("HOMEDOMAIN", new Namespace("", "SVCCONT/CHANGERSP"))));
//            if (root != null) {
//                Element respElement = root.element("CHANGERSP");
//                if (respElement != null) {
//                    Element respCodeElement = respElement.element("RESPCODE");
//                    if (respCodeElement != null) {
//                        if (respCodeElement.getTextTrim().equals("0000")) {//success
//                            Element mac2Element = respElement.element("MAC2");
//                            log.info("mac2={}", mac2Element.getTextTrim());
//                        } else {
//                            log.info("get mac fail, error desc:{}", respElement.elementTextTrim("RESPDESC"));
//                        }
//                    }
//                }
//            }
        } catch (DocumentException e) {
            log.error("getMac2 parse xml err", e);
        }
    }
}
