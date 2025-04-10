package org.collada.colladaschema;

import javolution.util.FastTable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}source" maxOccurs="unbounded"/>
 *             &lt;choice>
 *               &lt;sequence>
 *                 &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}sampler" maxOccurs="unbounded"/>
 *                 &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}channel" maxOccurs="unbounded"/>
 *                 &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}animation" maxOccurs="unbounded" minOccurs="0"/>
 *               &lt;/sequence>
 *               &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}animation" maxOccurs="unbounded"/>
 *             &lt;/choice>
 *           &lt;/sequence>
 *           &lt;sequence>
 *             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}sampler" maxOccurs="unbounded"/>
 *             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}channel" maxOccurs="unbounded"/>
 *             &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}animation" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;/sequence>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}animation" maxOccurs="unbounded"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "asset", "sources", "samplers", "channels", "animations", "extras" })
@XmlRootElement(name = "animation")
public class Animation {

    protected Asset asset;

    @XmlElement(name = "source")
    protected List<Source> sources;

    @XmlElement(name = "sampler")
    protected List<Sampler> samplers;

    @XmlElement(name = "channel")
    protected List<Channel> channels;

    @XmlElement(name = "animation")
    protected List<Animation> animations;

    @XmlElement(name = "extra")
    protected List<Extra> extras;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String name;

    /**
     * 
     * 						The animation element may contain an asset element.
     * 						
     * 
     * @return
     *     possible object is
     *     {@link Asset }
     *     
     */
    public Asset getAsset() {
        return asset;
    }

    /**
     * 
     * 						The animation element may contain an asset element.
     * 						
     * 
     * @param value
     *     allowed object is
     *     {@link Asset }
     *     
     */
    public void setAsset(Asset value) {
        this.asset = value;
    }

    /**
     * 
     * 								The animation element may contain any number of source elements.
     * 								Gets the value of the sources property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sources property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSources().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Source }
     * 
     * 
     */
    public List<Source> getSources() {
        if (sources == null) {
            sources = new FastTable<Source>();
        }
        return this.sources;
    }

    /**
     * Gets the value of the samplers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the samplers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSamplers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Sampler }
     * 
     * 
     */
    public List<Sampler> getSamplers() {
        if (samplers == null) {
            samplers = new FastTable<Sampler>();
        }
        return this.samplers;
    }

    /**
     * Gets the value of the channels property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the channels property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChannels().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Channel }
     * 
     * 
     */
    public List<Channel> getChannels() {
        if (channels == null) {
            channels = new FastTable<Channel>();
        }
        return this.channels;
    }

    /**
     * Gets the value of the animations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Animation }
     * 
     * 
     */
    public List<Animation> getAnimations() {
        if (animations == null) {
            animations = new FastTable<Animation>();
        }
        return this.animations;
    }

    /**
     * 
     * 						The extra element may appear any number of times.
     * 						Gets the value of the extras property.
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

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }
}
