package com.bocoon.entity.cms.assist.base;

import java.io.Serializable;

/**
 * This is an object that contains data related to the jc_acquisition table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="jc_acquisition"
 */
public abstract class BaseCmsAcquisition implements Serializable {

    public static String REF = "CmsAcquisition";

    public static String PROP_USER = "user";

    public static String PROP_LINK_START = "linkStart";

    public static String PROP_DESCRIPTION_END = "descriptionEnd";

    public static String PROP_CHANNEL = "channel";

    public static String PROP_DYNAMIC_START = "dynamicStart";

    public static String PROP_CONTENT_START = "contentStart";

    public static String PROP_TYPE = "type";

    public static String PROP_PAGINATION_END = "paginationEnd";

    public static String PROP_LINKSET_START = "linksetStart";

    public static String PROP_DYNAMIC_ADDR = "dynamicAddr";

    public static String PROP_LINKSET_END = "linksetEnd";

    public static String PROP_KEYWORDS_END = "keywordsEnd";

    public static String PROP_CURR_NUM = "currNum";

    public static String PROP_QUEUE = "queue";

    public static String PROP_LINK_END = "linkEnd";

    public static String PROP_START_TIME = "startTime";

    public static String PROP_PAGINATION_START = "paginationStart";

    public static String PROP_SITE = "site";

    public static String PROP_TOTAL_ITEM = "totalItem";

    public static String PROP_CURR_ITEM = "currItem";

    public static String PROP_NAME = "name";

    public static String PROP_STATUS = "status";

    public static String PROP_PAUSE_TIME = "pauseTime";

    public static String PROP_TITLE_START = "titleStart";

    public static String PROP_TITLE_END = "titleEnd";

    public static String PROP_CONTENT_END = "contentEnd";

    public static String PROP_PAGE_ENCODING = "pageEncoding";

    public static String PROP_ID = "id";

    public static String PROP_PLAN_LIST = "planList";

    public static String PROP_END_TIME = "endTime";

    public static String PROP_KEYWORDS_START = "keywordsStart";

    public static String PROP_DESCRIPTION_START = "descriptionStart";

    public static String PROP_DYNAMIC_END = "dynamicEnd";

    public BaseCmsAcquisition() {
        initialize();
    }

    /**
	 * Constructor for primary key
	 */
    public BaseCmsAcquisition(java.lang.Integer id) {
        this.setId(id);
        initialize();
    }

    /**
	 * Constructor for required fields
	 */
    public BaseCmsAcquisition(java.lang.Integer id, com.bocoon.entity.cms.main.CmsUser user, com.bocoon.entity.cms.main.ContentType type, com.bocoon.entity.cms.main.CmsSite site, com.bocoon.entity.cms.main.Channel channel, java.lang.String name, java.lang.Integer status, java.lang.Integer currNum, java.lang.Integer currItem, java.lang.Integer totalItem, java.lang.Integer pauseTime, java.lang.String pageEncoding, java.lang.Integer queue) {
        this.setId(id);
        this.setUser(user);
        this.setType(type);
        this.setSite(site);
        this.setChannel(channel);
        this.setName(name);
        this.setStatus(status);
        this.setCurrNum(currNum);
        this.setCurrItem(currItem);
        this.setTotalItem(totalItem);
        this.setPauseTime(pauseTime);
        this.setPageEncoding(pageEncoding);
        this.setQueue(queue);
        initialize();
    }

    protected void initialize() {
    }

    private int hashCode = Integer.MIN_VALUE;

    private java.lang.Integer id;

    private java.lang.String name;

    private java.util.Date startTime;

    private java.util.Date endTime;

    private java.lang.Integer status;

    private java.lang.Integer currNum;

    private java.lang.Integer currItem;

    private java.lang.Integer totalItem;

    private java.lang.Integer pauseTime;

    private java.lang.String pageEncoding;

    private java.lang.String planList;

    private java.lang.String dynamicAddr;

    private java.lang.Integer dynamicStart;

    private java.lang.Integer dynamicEnd;

    private java.lang.String linksetStart;

    private java.lang.String linksetEnd;

    private java.lang.String linkStart;

    private java.lang.String linkEnd;

    private java.lang.String titleStart;

    private java.lang.String titleEnd;

    private java.lang.String keywordsStart;

    private java.lang.String keywordsEnd;

    private java.lang.String descriptionStart;

    private java.lang.String descriptionEnd;

    private java.lang.String contentStart;

    private java.lang.String contentEnd;

    private java.lang.String paginationStart;

    private java.lang.String paginationEnd;

    private java.lang.Integer queue;

    private com.bocoon.entity.cms.main.CmsUser user;

    private com.bocoon.entity.cms.main.ContentType type;

    private com.bocoon.entity.cms.main.CmsSite site;

    private com.bocoon.entity.cms.main.Channel channel;

    /**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="identity"
     *  column="acquisition_id"
     */
    public java.lang.Integer getId() {
        return id;
    }

    /**
	 * Set the unique identifier of this class
	 * @param id the new ID
	 */
    public void setId(java.lang.Integer id) {
        this.id = id;
        this.hashCode = Integer.MIN_VALUE;
    }

    /**
	 * Return the value associated with the column: acq_name
	 */
    public java.lang.String getName() {
        return name;
    }

    /**
	 * Set the value related to the column: acq_name
	 * @param name the acq_name value
	 */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
	 * Return the value associated with the column: start_time
	 */
    public java.util.Date getStartTime() {
        return startTime;
    }

    /**
	 * Set the value related to the column: start_time
	 * @param startTime the start_time value
	 */
    public void setStartTime(java.util.Date startTime) {
        this.startTime = startTime;
    }

    /**
	 * Return the value associated with the column: end_time
	 */
    public java.util.Date getEndTime() {
        return endTime;
    }

    /**
	 * Set the value related to the column: end_time
	 * @param endTime the end_time value
	 */
    public void setEndTime(java.util.Date endTime) {
        this.endTime = endTime;
    }

    /**
	 * Return the value associated with the column: status
	 */
    public java.lang.Integer getStatus() {
        return status;
    }

    /**
	 * Set the value related to the column: status
	 * @param status the status value
	 */
    public void setStatus(java.lang.Integer status) {
        this.status = status;
    }

    /**
	 * Return the value associated with the column: curr_num
	 */
    public java.lang.Integer getCurrNum() {
        return currNum;
    }

    /**
	 * Set the value related to the column: curr_num
	 * @param currNum the curr_num value
	 */
    public void setCurrNum(java.lang.Integer currNum) {
        this.currNum = currNum;
    }

    /**
	 * Return the value associated with the column: curr_item
	 */
    public java.lang.Integer getCurrItem() {
        return currItem;
    }

    /**
	 * Set the value related to the column: curr_item
	 * @param currItem the curr_item value
	 */
    public void setCurrItem(java.lang.Integer currItem) {
        this.currItem = currItem;
    }

    /**
	 * Return the value associated with the column: total_item
	 */
    public java.lang.Integer getTotalItem() {
        return totalItem;
    }

    /**
	 * Set the value related to the column: total_item
	 * @param totalItem the total_item value
	 */
    public void setTotalItem(java.lang.Integer totalItem) {
        this.totalItem = totalItem;
    }

    /**
	 * Return the value associated with the column: pause_time
	 */
    public java.lang.Integer getPauseTime() {
        return pauseTime;
    }

    /**
	 * Set the value related to the column: pause_time
	 * @param pauseTime the pause_time value
	 */
    public void setPauseTime(java.lang.Integer pauseTime) {
        this.pauseTime = pauseTime;
    }

    /**
	 * Return the value associated with the column: page_encoding
	 */
    public java.lang.String getPageEncoding() {
        return pageEncoding;
    }

    /**
	 * Set the value related to the column: page_encoding
	 * @param pageEncoding the page_encoding value
	 */
    public void setPageEncoding(java.lang.String pageEncoding) {
        this.pageEncoding = pageEncoding;
    }

    /**
	 * Return the value associated with the column: plan_list
	 */
    public java.lang.String getPlanList() {
        return planList;
    }

    /**
	 * Set the value related to the column: plan_list
	 * @param planList the plan_list value
	 */
    public void setPlanList(java.lang.String planList) {
        this.planList = planList;
    }

    /**
	 * Return the value associated with the column: dynamic_addr
	 */
    public java.lang.String getDynamicAddr() {
        return dynamicAddr;
    }

    /**
	 * Set the value related to the column: dynamic_addr
	 * @param dynamicAddr the dynamic_addr value
	 */
    public void setDynamicAddr(java.lang.String dynamicAddr) {
        this.dynamicAddr = dynamicAddr;
    }

    /**
	 * Return the value associated with the column: dynamic_start
	 */
    public java.lang.Integer getDynamicStart() {
        return dynamicStart;
    }

    /**
	 * Set the value related to the column: dynamic_start
	 * @param dynamicStart the dynamic_start value
	 */
    public void setDynamicStart(java.lang.Integer dynamicStart) {
        this.dynamicStart = dynamicStart;
    }

    /**
	 * Return the value associated with the column: dynamic_end
	 */
    public java.lang.Integer getDynamicEnd() {
        return dynamicEnd;
    }

    /**
	 * Set the value related to the column: dynamic_end
	 * @param dynamicEnd the dynamic_end value
	 */
    public void setDynamicEnd(java.lang.Integer dynamicEnd) {
        this.dynamicEnd = dynamicEnd;
    }

    /**
	 * Return the value associated with the column: linkset_start
	 */
    public java.lang.String getLinksetStart() {
        return linksetStart;
    }

    /**
	 * Set the value related to the column: linkset_start
	 * @param linksetStart the linkset_start value
	 */
    public void setLinksetStart(java.lang.String linksetStart) {
        this.linksetStart = linksetStart;
    }

    /**
	 * Return the value associated with the column: linkset_end
	 */
    public java.lang.String getLinksetEnd() {
        return linksetEnd;
    }

    /**
	 * Set the value related to the column: linkset_end
	 * @param linksetEnd the linkset_end value
	 */
    public void setLinksetEnd(java.lang.String linksetEnd) {
        this.linksetEnd = linksetEnd;
    }

    /**
	 * Return the value associated with the column: link_start
	 */
    public java.lang.String getLinkStart() {
        return linkStart;
    }

    /**
	 * Set the value related to the column: link_start
	 * @param linkStart the link_start value
	 */
    public void setLinkStart(java.lang.String linkStart) {
        this.linkStart = linkStart;
    }

    /**
	 * Return the value associated with the column: link_end
	 */
    public java.lang.String getLinkEnd() {
        return linkEnd;
    }

    /**
	 * Set the value related to the column: link_end
	 * @param linkEnd the link_end value
	 */
    public void setLinkEnd(java.lang.String linkEnd) {
        this.linkEnd = linkEnd;
    }

    /**
	 * Return the value associated with the column: title_start
	 */
    public java.lang.String getTitleStart() {
        return titleStart;
    }

    /**
	 * Set the value related to the column: title_start
	 * @param titleStart the title_start value
	 */
    public void setTitleStart(java.lang.String titleStart) {
        this.titleStart = titleStart;
    }

    /**
	 * Return the value associated with the column: title_end
	 */
    public java.lang.String getTitleEnd() {
        return titleEnd;
    }

    /**
	 * Set the value related to the column: title_end
	 * @param titleEnd the title_end value
	 */
    public void setTitleEnd(java.lang.String titleEnd) {
        this.titleEnd = titleEnd;
    }

    /**
	 * Return the value associated with the column: keywords_start
	 */
    public java.lang.String getKeywordsStart() {
        return keywordsStart;
    }

    /**
	 * Set the value related to the column: keywords_start
	 * @param keywordsStart the keywords_start value
	 */
    public void setKeywordsStart(java.lang.String keywordsStart) {
        this.keywordsStart = keywordsStart;
    }

    /**
	 * Return the value associated with the column: keywords_end
	 */
    public java.lang.String getKeywordsEnd() {
        return keywordsEnd;
    }

    /**
	 * Set the value related to the column: keywords_end
	 * @param keywordsEnd the keywords_end value
	 */
    public void setKeywordsEnd(java.lang.String keywordsEnd) {
        this.keywordsEnd = keywordsEnd;
    }

    /**
	 * Return the value associated with the column: description_start
	 */
    public java.lang.String getDescriptionStart() {
        return descriptionStart;
    }

    /**
	 * Set the value related to the column: description_start
	 * @param descriptionStart the description_start value
	 */
    public void setDescriptionStart(java.lang.String descriptionStart) {
        this.descriptionStart = descriptionStart;
    }

    /**
	 * Return the value associated with the column: description_end
	 */
    public java.lang.String getDescriptionEnd() {
        return descriptionEnd;
    }

    /**
	 * Set the value related to the column: description_end
	 * @param descriptionEnd the description_end value
	 */
    public void setDescriptionEnd(java.lang.String descriptionEnd) {
        this.descriptionEnd = descriptionEnd;
    }

    /**
	 * Return the value associated with the column: content_start
	 */
    public java.lang.String getContentStart() {
        return contentStart;
    }

    /**
	 * Set the value related to the column: content_start
	 * @param contentStart the content_start value
	 */
    public void setContentStart(java.lang.String contentStart) {
        this.contentStart = contentStart;
    }

    /**
	 * Return the value associated with the column: content_end
	 */
    public java.lang.String getContentEnd() {
        return contentEnd;
    }

    /**
	 * Set the value related to the column: content_end
	 * @param contentEnd the content_end value
	 */
    public void setContentEnd(java.lang.String contentEnd) {
        this.contentEnd = contentEnd;
    }

    /**
	 * Return the value associated with the column: pagination_start
	 */
    public java.lang.String getPaginationStart() {
        return paginationStart;
    }

    /**
	 * Set the value related to the column: pagination_start
	 * @param paginationStart the pagination_start value
	 */
    public void setPaginationStart(java.lang.String paginationStart) {
        this.paginationStart = paginationStart;
    }

    /**
	 * Return the value associated with the column: pagination_end
	 */
    public java.lang.String getPaginationEnd() {
        return paginationEnd;
    }

    /**
	 * Set the value related to the column: pagination_end
	 * @param paginationEnd the pagination_end value
	 */
    public void setPaginationEnd(java.lang.String paginationEnd) {
        this.paginationEnd = paginationEnd;
    }

    /**
	 * Return the value associated with the column: queue
	 */
    public java.lang.Integer getQueue() {
        return queue;
    }

    /**
	 * Set the value related to the column: queue
	 * @param queue the queue value
	 */
    public void setQueue(java.lang.Integer queue) {
        this.queue = queue;
    }

    /**
	 * Return the value associated with the column: user_id
	 */
    public com.bocoon.entity.cms.main.CmsUser getUser() {
        return user;
    }

    /**
	 * Set the value related to the column: user_id
	 * @param user the user_id value
	 */
    public void setUser(com.bocoon.entity.cms.main.CmsUser user) {
        this.user = user;
    }

    /**
	 * Return the value associated with the column: type_id
	 */
    public com.bocoon.entity.cms.main.ContentType getType() {
        return type;
    }

    /**
	 * Set the value related to the column: type_id
	 * @param type the type_id value
	 */
    public void setType(com.bocoon.entity.cms.main.ContentType type) {
        this.type = type;
    }

    /**
	 * Return the value associated with the column: site_id
	 */
    public com.bocoon.entity.cms.main.CmsSite getSite() {
        return site;
    }

    /**
	 * Set the value related to the column: site_id
	 * @param site the site_id value
	 */
    public void setSite(com.bocoon.entity.cms.main.CmsSite site) {
        this.site = site;
    }

    /**
	 * Return the value associated with the column: channel_id
	 */
    public com.bocoon.entity.cms.main.Channel getChannel() {
        return channel;
    }

    /**
	 * Set the value related to the column: channel_id
	 * @param channel the channel_id value
	 */
    public void setChannel(com.bocoon.entity.cms.main.Channel channel) {
        this.channel = channel;
    }

    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof com.bocoon.entity.cms.assist.CmsAcquisition)) return false; else {
            com.bocoon.entity.cms.assist.CmsAcquisition cmsAcquisition = (com.bocoon.entity.cms.assist.CmsAcquisition) obj;
            if (null == this.getId() || null == cmsAcquisition.getId()) return false; else return (this.getId().equals(cmsAcquisition.getId()));
        }
    }

    public int hashCode() {
        if (Integer.MIN_VALUE == this.hashCode) {
            if (null == this.getId()) return super.hashCode(); else {
                String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
                this.hashCode = hashStr.hashCode();
            }
        }
        return this.hashCode;
    }

    public String toString() {
        return super.toString();
    }
}
