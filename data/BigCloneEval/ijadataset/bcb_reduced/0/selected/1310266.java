package com.bocoon.entity.cms.main.base;

import java.io.Serializable;

/**
 * This is an object that contains data related to the jc_content table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="jc_content"
 */
public abstract class BaseContent implements Serializable {

    public static String REF = "Content";

    public static String PROP_STATUS = "status";

    public static String PROP_TYPE = "type";

    public static String PROP_RECOMMEND = "recommend";

    public static String PROP_SITE = "site";

    public static String PROP_USER = "user";

    public static String PROP_HAS_TITLE_IMG = "hasTitleImg";

    public static String PROP_SORT_DATE = "sortDate";

    public static String PROP_TOP_LEVEL = "topLevel";

    public static String PROP_COMMENTS_DAY = "commentsDay";

    public static String PROP_CONTENT_EXT = "contentExt";

    public static String PROP_VIEWS_DAY = "viewsDay";

    public static String PROP_UPS_DAY = "upsDay";

    public static String PROP_CHANNEL = "channel";

    public static String PROP_CONTENT_COUNT = "contentCount";

    public static String PROP_ID = "id";

    public static String PROP_DOWNLOADS_DAY = "downloadsDay";

    public BaseContent() {
        initialize();
    }

    /**
	 * Constructor for primary key
	 */
    public BaseContent(java.lang.Integer id) {
        this.setId(id);
        initialize();
    }

    /**
	 * Constructor for required fields
	 */
    public BaseContent(java.lang.Integer id, com.bocoon.entity.cms.main.CmsSite site, java.util.Date sortDate, java.lang.Byte topLevel, java.lang.Boolean hasTitleImg, java.lang.Boolean recommend, java.lang.Byte status, java.lang.Integer viewsDay, java.lang.Short commentsDay, java.lang.Short downloadsDay, java.lang.Short upsDay) {
        this.setId(id);
        this.setSite(site);
        this.setSortDate(sortDate);
        this.setTopLevel(topLevel);
        this.setHasTitleImg(hasTitleImg);
        this.setRecommend(recommend);
        this.setStatus(status);
        this.setViewsDay(viewsDay);
        this.setCommentsDay(commentsDay);
        this.setDownloadsDay(downloadsDay);
        this.setUpsDay(upsDay);
        initialize();
    }

    protected void initialize() {
    }

    private int hashCode = Integer.MIN_VALUE;

    private java.lang.Integer id;

    private java.util.Date sortDate;

    private java.lang.Byte topLevel;

    private java.lang.Boolean hasTitleImg;

    private java.lang.Boolean recommend;

    private java.lang.Byte status;

    private java.lang.Integer viewsDay;

    private java.lang.Short commentsDay;

    private java.lang.Short downloadsDay;

    private java.lang.Short upsDay;

    private com.bocoon.entity.cms.main.ContentExt contentExt;

    private com.bocoon.entity.cms.main.ContentCount contentCount;

    private com.bocoon.entity.cms.main.ContentType type;

    private com.bocoon.entity.cms.main.CmsSite site;

    private com.bocoon.entity.cms.main.CmsUser user;

    private com.bocoon.entity.cms.main.Channel channel;

    private java.util.Set<com.bocoon.entity.cms.main.Channel> channels;

    private java.util.Set<com.bocoon.entity.cms.main.CmsTopic> topics;

    private java.util.Set<com.bocoon.entity.cms.main.CmsGroup> viewGroups;

    private java.util.List<com.bocoon.entity.cms.main.ContentTag> tags;

    private java.util.List<com.bocoon.entity.cms.main.ContentPicture> pictures;

    private java.util.List<com.bocoon.entity.cms.main.ContentAttachment> attachments;

    private java.util.Set<com.bocoon.entity.cms.main.ContentTxt> contentTxtSet;

    private java.util.Set<com.bocoon.entity.cms.main.ContentCheck> contentCheckSet;

    private java.util.Map<java.lang.String, java.lang.String> attr;

    private java.util.Set<com.bocoon.entity.cms.main.CmsUser> collectUsers;

    /**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="identity"
     *  column="content_id"
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
	 * Return the value associated with the column: sort_date
	 */
    public java.util.Date getSortDate() {
        return sortDate;
    }

    /**
	 * Set the value related to the column: sort_date
	 * @param sortDate the sort_date value
	 */
    public void setSortDate(java.util.Date sortDate) {
        this.sortDate = sortDate;
    }

    /**
	 * Return the value associated with the column: top_level
	 */
    public java.lang.Byte getTopLevel() {
        return topLevel;
    }

    /**
	 * Set the value related to the column: top_level
	 * @param topLevel the top_level value
	 */
    public void setTopLevel(java.lang.Byte topLevel) {
        this.topLevel = topLevel;
    }

    /**
	 * Return the value associated with the column: has_title_img
	 */
    public java.lang.Boolean getHasTitleImg() {
        return hasTitleImg;
    }

    /**
	 * Set the value related to the column: has_title_img
	 * @param hasTitleImg the has_title_img value
	 */
    public void setHasTitleImg(java.lang.Boolean hasTitleImg) {
        this.hasTitleImg = hasTitleImg;
    }

    /**
	 * Return the value associated with the column: is_recommend
	 */
    public java.lang.Boolean getRecommend() {
        return recommend;
    }

    /**
	 * Set the value related to the column: is_recommend
	 * @param recommend the is_recommend value
	 */
    public void setRecommend(java.lang.Boolean recommend) {
        this.recommend = recommend;
    }

    /**
	 * Return the value associated with the column: status
	 */
    public java.lang.Byte getStatus() {
        return status;
    }

    /**
	 * Set the value related to the column: status
	 * @param status the status value
	 */
    public void setStatus(java.lang.Byte status) {
        this.status = status;
    }

    /**
	 * Return the value associated with the column: views_day
	 */
    public java.lang.Integer getViewsDay() {
        return viewsDay;
    }

    /**
	 * Set the value related to the column: views_day
	 * @param viewsDay the views_day value
	 */
    public void setViewsDay(java.lang.Integer viewsDay) {
        this.viewsDay = viewsDay;
    }

    /**
	 * Return the value associated with the column: comments_day
	 */
    public java.lang.Short getCommentsDay() {
        return commentsDay;
    }

    /**
	 * Set the value related to the column: comments_day
	 * @param commentsDay the comments_day value
	 */
    public void setCommentsDay(java.lang.Short commentsDay) {
        this.commentsDay = commentsDay;
    }

    /**
	 * Return the value associated with the column: downloads_day
	 */
    public java.lang.Short getDownloadsDay() {
        return downloadsDay;
    }

    /**
	 * Set the value related to the column: downloads_day
	 * @param downloadsDay the downloads_day value
	 */
    public void setDownloadsDay(java.lang.Short downloadsDay) {
        this.downloadsDay = downloadsDay;
    }

    /**
	 * Return the value associated with the column: ups_day
	 */
    public java.lang.Short getUpsDay() {
        return upsDay;
    }

    /**
	 * Set the value related to the column: ups_day
	 * @param upsDay the ups_day value
	 */
    public void setUpsDay(java.lang.Short upsDay) {
        this.upsDay = upsDay;
    }

    /**
	 * Return the value associated with the column: contentExt
	 */
    public com.bocoon.entity.cms.main.ContentExt getContentExt() {
        return contentExt;
    }

    /**
	 * Set the value related to the column: contentExt
	 * @param contentExt the contentExt value
	 */
    public void setContentExt(com.bocoon.entity.cms.main.ContentExt contentExt) {
        this.contentExt = contentExt;
    }

    /**
	 * Return the value associated with the column: contentCount
	 */
    public com.bocoon.entity.cms.main.ContentCount getContentCount() {
        return contentCount;
    }

    /**
	 * Set the value related to the column: contentCount
	 * @param contentCount the contentCount value
	 */
    public void setContentCount(com.bocoon.entity.cms.main.ContentCount contentCount) {
        this.contentCount = contentCount;
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

    /**
	 * Return the value associated with the column: channels
	 */
    public java.util.Set<com.bocoon.entity.cms.main.Channel> getChannels() {
        return channels;
    }

    /**
	 * Set the value related to the column: channels
	 * @param channels the channels value
	 */
    public void setChannels(java.util.Set<com.bocoon.entity.cms.main.Channel> channels) {
        this.channels = channels;
    }

    /**
	 * Return the value associated with the column: topics
	 */
    public java.util.Set<com.bocoon.entity.cms.main.CmsTopic> getTopics() {
        return topics;
    }

    /**
	 * Set the value related to the column: topics
	 * @param topics the topics value
	 */
    public void setTopics(java.util.Set<com.bocoon.entity.cms.main.CmsTopic> topics) {
        this.topics = topics;
    }

    /**
	 * Return the value associated with the column: viewGroups
	 */
    public java.util.Set<com.bocoon.entity.cms.main.CmsGroup> getViewGroups() {
        return viewGroups;
    }

    /**
	 * Set the value related to the column: viewGroups
	 * @param viewGroups the viewGroups value
	 */
    public void setViewGroups(java.util.Set<com.bocoon.entity.cms.main.CmsGroup> viewGroups) {
        this.viewGroups = viewGroups;
    }

    /**
	 * Return the value associated with the column: tags
	 */
    public java.util.List<com.bocoon.entity.cms.main.ContentTag> getTags() {
        return tags;
    }

    /**
	 * Set the value related to the column: tags
	 * @param tags the tags value
	 */
    public void setTags(java.util.List<com.bocoon.entity.cms.main.ContentTag> tags) {
        this.tags = tags;
    }

    /**
	 * Return the value associated with the column: pictures
	 */
    public java.util.List<com.bocoon.entity.cms.main.ContentPicture> getPictures() {
        return pictures;
    }

    /**
	 * Set the value related to the column: pictures
	 * @param pictures the pictures value
	 */
    public void setPictures(java.util.List<com.bocoon.entity.cms.main.ContentPicture> pictures) {
        this.pictures = pictures;
    }

    /**
	 * Return the value associated with the column: attachments
	 */
    public java.util.List<com.bocoon.entity.cms.main.ContentAttachment> getAttachments() {
        return attachments;
    }

    /**
	 * Set the value related to the column: attachments
	 * @param attachments the attachments value
	 */
    public void setAttachments(java.util.List<com.bocoon.entity.cms.main.ContentAttachment> attachments) {
        this.attachments = attachments;
    }

    /**
	 * Return the value associated with the column: contentTxtSet
	 */
    public java.util.Set<com.bocoon.entity.cms.main.ContentTxt> getContentTxtSet() {
        return contentTxtSet;
    }

    /**
	 * Set the value related to the column: contentTxtSet
	 * @param contentTxtSet the contentTxtSet value
	 */
    public void setContentTxtSet(java.util.Set<com.bocoon.entity.cms.main.ContentTxt> contentTxtSet) {
        this.contentTxtSet = contentTxtSet;
    }

    /**
	 * Return the value associated with the column: contentCheckSet
	 */
    public java.util.Set<com.bocoon.entity.cms.main.ContentCheck> getContentCheckSet() {
        return contentCheckSet;
    }

    /**
	 * Set the value related to the column: contentCheckSet
	 * @param contentCheckSet the contentCheckSet value
	 */
    public void setContentCheckSet(java.util.Set<com.bocoon.entity.cms.main.ContentCheck> contentCheckSet) {
        this.contentCheckSet = contentCheckSet;
    }

    /**
	 * Return the value associated with the column: attr
	 */
    public java.util.Map<java.lang.String, java.lang.String> getAttr() {
        return attr;
    }

    public java.util.Set<com.bocoon.entity.cms.main.CmsUser> getCollectUsers() {
        return collectUsers;
    }

    public void setCollectUsers(java.util.Set<com.bocoon.entity.cms.main.CmsUser> collectUsers) {
        this.collectUsers = collectUsers;
    }

    /**
	 * Set the value related to the column: attr
	 * @param attr the attr value
	 */
    public void setAttr(java.util.Map<java.lang.String, java.lang.String> attr) {
        this.attr = attr;
    }

    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof com.bocoon.entity.cms.main.Content)) return false; else {
            com.bocoon.entity.cms.main.Content content = (com.bocoon.entity.cms.main.Content) obj;
            if (null == this.getId() || null == content.getId()) return false; else return (this.getId().equals(content.getId()));
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
