
package com.jadic.ws.czsmk;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Response complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HOMEDOMAIN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TRANSIDH" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RESPCODE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RESPDESC" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Response", namespace = "urn:CenterProcess", propOrder = {
    "homedomain",
    "transidh",
    "respcode",
    "respdesc"
})
public class Response {

    @XmlElement(name = "HOMEDOMAIN", required = true)
    protected java.lang.String homedomain;
    @XmlElement(name = "TRANSIDH", required = true)
    protected java.lang.String transidh;
    @XmlElement(name = "RESPCODE", required = true)
    protected java.lang.String respcode;
    @XmlElement(name = "RESPDESC", required = true)
    protected java.lang.String respdesc;

    /**
     * 获取homedomain属性的值。
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getHOMEDOMAIN() {
        return homedomain;
    }

    /**
     * 设置homedomain属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setHOMEDOMAIN(java.lang.String value) {
        this.homedomain = value;
    }

    /**
     * 获取transidh属性的值。
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getTRANSIDH() {
        return transidh;
    }

    /**
     * 设置transidh属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setTRANSIDH(java.lang.String value) {
        this.transidh = value;
    }

    /**
     * 获取respcode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getRESPCODE() {
        return respcode;
    }

    /**
     * 设置respcode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setRESPCODE(java.lang.String value) {
        this.respcode = value;
    }

    /**
     * 获取respdesc属性的值。
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getRESPDESC() {
        return respdesc;
    }

    /**
     * 设置respdesc属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setRESPDESC(java.lang.String value) {
        this.respdesc = value;
    }

}
