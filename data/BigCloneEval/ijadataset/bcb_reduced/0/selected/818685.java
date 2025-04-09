package net.sf.webwarp.modules.partner.ui.trinidad;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import net.sf.webwarp.modules.partner.address.Address;
import net.sf.webwarp.modules.partner.address.AddressType;
import net.sf.webwarp.modules.partner.address.ContactChannelType;
import net.sf.webwarp.modules.partner.partner.Confidentiality;
import net.sf.webwarp.modules.partner.partner.NameDisplaying;
import net.sf.webwarp.modules.partner.partner.Partner;

public class PartnerDisplayBean<T extends Partner> implements Serializable {

    private static final long serialVersionUID = 5613021152542712774L;

    protected MessageSource messageSource;

    protected Locale locale;

    protected T partner;

    private List<AddressType> addressTypes;

    private List<ContactChannelType> contactChannelTypes;

    public PartnerDisplayBean(T partner, MessageSource messageSource, Locale locale, List<AddressType> addressTypes, List<ContactChannelType> contactChannelTypes) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.addressTypes = addressTypes;
        this.contactChannelTypes = contactChannelTypes;
        this.setPartner(partner);
    }

    public List<AddressDisplayBean> getAddresses() {
        List<AddressDisplayBean> displayBeans = new ArrayList<AddressDisplayBean>();
        for (AddressType type : addressTypes) {
            Class displayBeanClass = getAddressDisplayBeanClass();
            Address address = partner.getAddresses().get(type);
            if (address == null) {
                address = getAddressInstance(type);
                partner.getAddresses().put(type, address);
            }
            try {
                Constructor constructor = displayBeanClass.getConstructor(getAddressDisplayBeanConstructorSignature());
                AddressDisplayBean displayBean = (AddressDisplayBean) constructor.newInstance(address, type, messageSource, locale, contactChannelTypes);
                displayBeans.add(displayBean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return displayBeans;
    }

    protected Address getAddressInstance(AddressType type) {
        return new Address(type);
    }

    protected Class[] getAddressDisplayBeanConstructorSignature() {
        return AddressDisplayBean.constructorSignature;
    }

    protected Class getAddressDisplayBeanClass() {
        return AddressDisplayBean.class;
    }

    public Confidentiality getConfidentiality() {
        return partner.getConfidentiality();
    }

    public DateTime getCreatedAt() {
        return partner.getCreatedAt();
    }

    public String getCreatedFrom() {
        return partner.getCreatedBy();
    }

    public String getDisplayName() {
        return partner.getDisplayName();
    }

    public Long getId() {
        return partner.getId();
    }

    public Long getLatitude() {
        return partner.getLatitude();
    }

    public Locale getLocale() {
        return partner.getLocale();
    }

    public Long getLongitude() {
        return partner.getLongitude();
    }

    public Long getMandantID() {
        return partner.getMandantID();
    }

    public String getName() {
        return partner.getName();
    }

    public NameDisplaying getNameDisplayType() {
        return partner.getNameDisplayType();
    }

    public String getRemarks() {
        return partner.getRemarks();
    }

    public DateTime getUpdatedAt() {
        return partner.getUpdatedAt();
    }

    public String getUpdatedFrom() {
        return partner.getUpdatedBy();
    }

    public String getUserDefinedDislplayName() {
        return partner.getUserDefinedDislplayName();
    }

    public void setActive(boolean active) {
        partner.setActive(active);
    }

    public void setConfidentiality(Confidentiality confidentiality) {
        partner.setConfidentiality(confidentiality);
    }

    public void setCreatedAt(DateTime createdAt) {
        partner.setCreatedAt(createdAt);
    }

    public void setCreatedFrom(String createdFrom) {
        partner.setCreatedBy(createdFrom);
    }

    public void setDeleted(boolean deleted) {
        partner.setDeleted(deleted);
    }

    public void setId(Long id) {
        partner.setId(id);
    }

    public void setLatitude(Long latitude) {
        partner.setLatitude(latitude);
    }

    public void setLocale(Locale locale) {
        partner.setLocale(locale);
    }

    public void setLongitude(Long longitude) {
        partner.setLongitude(longitude);
    }

    public void setMandantID(Long mandantID) {
        partner.setMandantID(mandantID);
    }

    public void setName(String name) {
        partner.setName(name);
    }

    public void setNameDisplayType(NameDisplaying nameDisplayType) {
        partner.setNameDisplayType(nameDisplayType);
    }

    public void setRemarks(String remarks) {
        partner.setRemarks(remarks);
    }

    public void setUpdatedAt(DateTime updatedAt) {
        partner.setUpdatedAt(updatedAt);
    }

    public void setUpdatedFrom(String updatedFrom) {
        partner.setUpdatedBy(updatedFrom);
    }

    public void setUserDefinedDislplayName(String userDefinedDislplayName) {
        partner.setUserDefinedDislplayName(userDefinedDislplayName);
    }

    public T getPartner() {
        return partner;
    }

    public void setPartner(T partner) {
        this.partner = partner;
    }

    public Boolean getActive() {
        return partner.getActive();
    }

    public void setActive(Boolean active) {
        partner.setActive(active);
    }
}
