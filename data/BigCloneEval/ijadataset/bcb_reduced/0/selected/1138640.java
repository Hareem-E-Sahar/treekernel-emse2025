package vo;

/**
 * TChannel generated by MyEclipse - Hibernate Tools
 */
public class TChannel_vo implements java.io.Serializable {

    private Integer id;

    private String serviceHallCode;

    private String serviceHallName;

    private String channelType;

    private Integer starLevel;

    private String address;

    private String company;

    private String contactMan;

    private String contactTel;

    private String contactMobile;

    private Float rent;

    private String town;

    private String towncode;

    private String county;

    private String countyCode;

    private Float x;

    private Float y;

    private Integer zoom;

    /** default constructor */
    public TChannel_vo() {
    }

    /** minimal constructor */
    public TChannel_vo(String serviceHallCode, String serviceHallName, String channelType) {
        this.serviceHallCode = serviceHallCode;
        this.serviceHallName = serviceHallName;
        this.channelType = channelType;
    }

    /** full constructor */
    public TChannel_vo(String serviceHallCode, String serviceHallName, String channelType, Integer starLevel, String address, String company, String contactMan, String contactTel, String contactMobile, Float rent, String town, String towncode, String county, String countyCode, Float x, Float y, Integer zoom) {
        this.serviceHallCode = serviceHallCode;
        this.serviceHallName = serviceHallName;
        this.channelType = channelType;
        this.starLevel = starLevel;
        this.address = address;
        this.company = company;
        this.contactMan = contactMan;
        this.contactTel = contactTel;
        this.contactMobile = contactMobile;
        this.rent = rent;
        this.town = town;
        this.towncode = towncode;
        this.county = county;
        this.countyCode = countyCode;
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceHallCode() {
        return this.serviceHallCode;
    }

    public void setServiceHallCode(String serviceHallCode) {
        this.serviceHallCode = serviceHallCode;
    }

    public String getServiceHallName() {
        return this.serviceHallName;
    }

    public void setServiceHallName(String serviceHallName) {
        this.serviceHallName = serviceHallName;
    }

    public String getChannelType() {
        return this.channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public Integer getStarLevel() {
        return this.starLevel;
    }

    public void setStarLevel(Integer starLevel) {
        this.starLevel = starLevel;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompany() {
        return this.company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getContactMan() {
        return this.contactMan;
    }

    public void setContactMan(String contactMan) {
        this.contactMan = contactMan;
    }

    public String getContactTel() {
        return this.contactTel;
    }

    public void setContactTel(String contactTel) {
        this.contactTel = contactTel;
    }

    public String getContactMobile() {
        return this.contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public Float getRent() {
        return this.rent;
    }

    public void setRent(Float rent) {
        this.rent = rent;
    }

    public String getTown() {
        return this.town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getTowncode() {
        return this.towncode;
    }

    public void setTowncode(String towncode) {
        this.towncode = towncode;
    }

    public String getCounty() {
        return this.county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCountyCode() {
        return this.countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public Float getX() {
        return this.x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return this.y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Integer getZoom() {
        return this.zoom;
    }

    public void setZoom(Integer zoom) {
        this.zoom = zoom;
    }
}
