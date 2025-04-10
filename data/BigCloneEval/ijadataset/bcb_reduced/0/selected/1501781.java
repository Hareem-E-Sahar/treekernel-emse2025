package org.collada.colladaschema;

import javolution.util.FastTable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * If the exact format cannot be resolve via other methods then the format_hint will describe the important features of the format so that the application may select a compatable or close format
 * 
 * <p>Java class for fx_surface_format_hint_common complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fx_surface_format_hint_common">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="channels" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_channels_enum"/>
 *         &lt;element name="range" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_range_enum"/>
 *         &lt;element name="precision" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_precision_enum" minOccurs="0"/>
 *         &lt;element name="option" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_option_enum" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_surface_format_hint_common", propOrder = { "channels", "range", "precision", "options", "extras" })
public class FxSurfaceFormatHintCommon {

    @XmlElement(required = true)
    protected FxSurfaceFormatHintChannelsEnum channels;

    @XmlElement(required = true)
    protected FxSurfaceFormatHintRangeEnum range;

    protected FxSurfaceFormatHintPrecisionEnum precision;

    @XmlElement(name = "option")
    protected List<FxSurfaceFormatHintOptionEnum> options;

    @XmlElement(name = "extra")
    protected List<Extra> extras;

    /**
     * Gets the value of the channels property.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceFormatHintChannelsEnum }
     *     
     */
    public FxSurfaceFormatHintChannelsEnum getChannels() {
        return channels;
    }

    /**
     * Sets the value of the channels property.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceFormatHintChannelsEnum }
     *     
     */
    public void setChannels(FxSurfaceFormatHintChannelsEnum value) {
        this.channels = value;
    }

    /**
     * Gets the value of the range property.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceFormatHintRangeEnum }
     *     
     */
    public FxSurfaceFormatHintRangeEnum getRange() {
        return range;
    }

    /**
     * Sets the value of the range property.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceFormatHintRangeEnum }
     *     
     */
    public void setRange(FxSurfaceFormatHintRangeEnum value) {
        this.range = value;
    }

    /**
     * Gets the value of the precision property.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceFormatHintPrecisionEnum }
     *     
     */
    public FxSurfaceFormatHintPrecisionEnum getPrecision() {
        return precision;
    }

    /**
     * Sets the value of the precision property.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceFormatHintPrecisionEnum }
     *     
     */
    public void setPrecision(FxSurfaceFormatHintPrecisionEnum value) {
        this.precision = value;
    }

    /**
     * Gets the value of the options property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the options property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOptions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FxSurfaceFormatHintOptionEnum }
     * 
     * 
     */
    public List<FxSurfaceFormatHintOptionEnum> getOptions() {
        if (options == null) {
            options = new FastTable<FxSurfaceFormatHintOptionEnum>();
        }
        return this.options;
    }

    /**
     * Gets the value of the extras property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extras property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtras().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Extra }
     * 
     * 
     */
    public List<Extra> getExtras() {
        if (extras == null) {
            extras = new FastTable<Extra>();
        }
        return this.extras;
    }
}
