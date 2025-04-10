package vo;

import java.util.Date;

/**
 * TChannelSale generated by MyEclipse - Hibernate Tools
 */
public class TChannelSale_vo implements java.io.Serializable {

    private Integer id;

    private String theMonth;

    private String channelCode;

    private String channelName;

    private Date updatedDay;

    private Float recompense;

    private Integer chargeAvg;

    private Integer cardSaleAvg;

    private Integer cardApplyAvg;

    /** default constructor */
    public TChannelSale_vo() {
    }

    /** full constructor */
    public TChannelSale_vo(String theMonth, String channelCode, String channelName, Date updatedDay, Float recompense, Integer chargeAvg, Integer cardSaleAvg, Integer cardApplyAvg) {
        this.theMonth = theMonth;
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.updatedDay = updatedDay;
        this.recompense = recompense;
        this.chargeAvg = chargeAvg;
        this.cardSaleAvg = cardSaleAvg;
        this.cardApplyAvg = cardApplyAvg;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTheMonth() {
        return this.theMonth;
    }

    public void setTheMonth(String theMonth) {
        this.theMonth = theMonth;
    }

    public String getChannelCode() {
        return this.channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Date getUpdatedDay() {
        return this.updatedDay;
    }

    public void setUpdatedDay(Date updatedDay) {
        this.updatedDay = updatedDay;
    }

    public Float getRecompense() {
        return this.recompense;
    }

    public void setRecompense(Float recompense) {
        this.recompense = recompense;
    }

    public Integer getChargeAvg() {
        return this.chargeAvg;
    }

    public void setChargeAvg(Integer chargeAvg) {
        this.chargeAvg = chargeAvg;
    }

    public Integer getCardSaleAvg() {
        return this.cardSaleAvg;
    }

    public void setCardSaleAvg(Integer cardSaleAvg) {
        this.cardSaleAvg = cardSaleAvg;
    }

    public Integer getCardApplyAvg() {
        return this.cardApplyAvg;
    }

    public void setCardApplyAvg(Integer cardApplyAvg) {
        this.cardApplyAvg = cardApplyAvg;
    }
}
