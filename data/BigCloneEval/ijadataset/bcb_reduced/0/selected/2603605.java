package com.xmultra.processor.rss;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import javax.activation.MimetypesFileTypeMap;
import com.xmultra.log.Console;
import com.xmultra.processor.rss.EntityTranslator;
import com.xmultra.util.Strings;

/**
 * RssItem represents an item element within an RSS channel element.
 *
 * @author      Bob Hucker
 * @version     $Revision$
 * @since       1.4
 * @see <code>http://cyber.law.harvard.edu/rss/rss.html#hrelementsOfLtitemgt</code>
 */
public class RssItem {

    /**
    * Updated automatically by source control management.
    */
    public static final String VERSION = "@version $Revision: #2 $";

    private static final String cDataPattern = "<!\\[CDATA\\[([\\s\\S]*?)\\]\\]>";

    private static final String ampersandPattern = "&(?!(#x?\\d{1,4}|[A-Za-z][a-z]{1,7}|THORN|ETH);)";

    protected String title = null;

    private String titleValue = null;

    protected String link = null;

    protected String guid = null;

    private String guidValue = null;

    protected String description = null;

    private String author = null;

    private String category = null;

    private String comments = null;

    protected String enclosure = null;

    protected String contentEncoded = null;

    protected ArrayList<Image> images = null;

    protected String imagesText = null;

    private String pubDateStr = null;

    private Date pubDate = null;

    private String source = null;

    protected String creator = null;

    private String server = null;

    protected String url = null;

    private int roundRobinSortKey = 0;

    protected String text = null;

    private static final String titlePattern = "<title>([\\s\\S]*?)</title>";

    private static final String pubDatePattern = "<pubDate>(?:\\s*<!\\[CDATA\\[)?\\s*([\\s\\S]*?)\\s*(?:\\]\\]>\\s*)?</pubDate>";

    private static final String dcDatePattern = "<dc:date[^>]*>(?:\\s*<!\\[CDATA\\[)?\\s*([\\s\\S]*?)\\s*(?:\\]\\]>\\s*)?</dc:date>";

    private static final String authorPattern = "<author>[\\s\\S]*?</author>";

    private static final String creatorPattern = "<dc:creator[^>]*>[\\s\\S]*?</dc:creator>";

    protected static final String serverPattern = "(https?://)([\\w.]+\\.(?:com|org|net|gov|edu)|localhost)";

    private static final String linkPattern = "<link>\\s*" + serverPattern + "([\\s\\S]*?)\\s*</link>";

    private static final String guidPattern = "<guid(?:\\s+isPermaLink\\s*=\\s*\"(?:true|false)\")?>\\s*([\\s\\S]*?)\\s*</guid>";

    private static final String descriptionPattern = "<description>\\s*([\\s\\S]*?)\\s*</description>";

    static final String enclosurePattern = "(<enclosure[^>]+url=\")(https?://(?:[^/\"]+/)*)([^\"]+?)(\"[^>]*/>)";

    protected static final String imagesPattern = "( *<media:content[^>]*(?:/>|>[\\s\\S]*?</media:content>)\\s*)+";

    protected static final String contentEncodedPattern = "<content:encoded( +xmlns:content=\".+?\")?>\\s*([\\s\\S]*?)\\s*</content:encoded>";

    static final String priorityPattern = "<mngi:priority[^>]*>([\\s\\S]*?)</mngi:priority>";

    private int priority = Integer.MAX_VALUE;

    protected Strings strings = new Strings();

    protected static MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

    /**
    * Constructor
    *
    * @param text RSS item text
    */
    public RssItem(String text) throws ParseException, UnsupportedEncodingException {
        this(text, true);
    }

    /**
    * Special constructor allows for keeping or removing CDATA sections in input.
    *
    * @param text RSS item text
    * @param keepCDataSections flag to retain or remove CDATA markup in input
    */
    public RssItem(String text, boolean keepCDataSections) throws ParseException, UnsupportedEncodingException {
        this.text = EntityTranslator.convertToLongEntities(text);
        this.text = EntityTranslator.convertToNumericEntities(this.text);
        if (!keepCDataSections) {
            stripCDataMarkup();
        }
        cleanSpecialCharacters();
        if (strings.matches(titlePattern, this.text)) {
            setTitleAndValue(strings.toString(), strings.getGroup(1));
        }
        if (strings.matches(authorPattern, this.text)) {
            setAuthor(strings.toString());
        }
        if (strings.matches(creatorPattern, this.text)) {
            setCreator(strings.toString());
        }
        if (strings.matches(guidPattern, this.text)) {
            setGuidAndValue(strings.toString(), strings.getGroup(1));
        }
        if (strings.matches(descriptionPattern, this.text)) {
            setDescription(strings.toString());
        }
        if (strings.matches(enclosurePattern, this.text)) {
            setEnclosure(strings.toString());
        }
        if (strings.matches(imagesPattern, this.text)) {
            setImagesText(strings.toString());
        }
        if (strings.matches(contentEncodedPattern, this.text)) {
            setContentEncoded(strings.toString());
        }
        if (strings.matches(priorityPattern, this.text)) {
            setPriority(strings.getGroup(1));
        }
        if (strings.matches(linkPattern, this.text)) {
            setLink(strings.toString());
            setServer(strings.getGroup(2));
            setUrl(strings.getGroup(1) + strings.getGroup(2) + strings.getGroup(3));
        }
        fixPubDate();
    }

    private void stripCDataMarkup() {
        StringBuffer startOfText = new StringBuffer(this.text.length() + 100);
        String endOfText = this.text;
        while (strings.matches(cDataPattern, endOfText)) {
            startOfText.append(strings.getPreMatch());
            String cleanText = strings.getGroup(1);
            endOfText = strings.getPostMatch();
            cleanText = strings.substitute(ampersandPattern, "&amp;", cleanText);
            cleanText = strings.substitute("<", "&lt;", cleanText);
            cleanText = strings.substitute(">", "&gt;", cleanText);
            startOfText.append(cleanText);
        }
        startOfText.append(endOfText);
        this.text = startOfText.toString();
    }

    void cleanSpecialCharacters() {
        String cleanText = this.text;
        cleanText = cleanText.replace("\342\200\223", "&#x2013;");
        cleanText = cleanText.replace("\302\240", " ");
        cleanText = cleanText.replace("\302\247", "&#x00A7;");
        cleanText = cleanText.replace("\342\200\224", "&#x2014;");
        cleanText = cleanText.replace("\342\200\230", "&#x2018;");
        cleanText = cleanText.replace("\342\200\231", "&#x2019;");
        cleanText = cleanText.replace("\342\200\234", "&#x201C;");
        cleanText = cleanText.replace("\342\200\235", "&#x201D;");
        cleanText = cleanText.replace("\342\200\246", "&#x2026;");
        cleanText = strings.substitute("(^|>)n (?=[A-Z])", "$1&#x2022; ", cleanText);
        this.text = cleanText;
    }

    /**
     * Find the pubDate string, convert it to a numeric Date object, convert the numeric
     * Date back to a string in the default time zone and then update the full text of the item.
     */
    void fixPubDate() throws ParseException {
        SimpleDateFormat sdf = null;
        if (strings.matches(pubDatePattern, this.text)) {
            setPubDateStr(strings.getGroup(1));
            if (Character.isDigit(this.pubDateStr.charAt(this.pubDateStr.length() - 1))) {
                sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            } else {
                sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            }
            try {
                this.pubDate = sdf.parse(this.getPubDateStr());
                sdf.setTimeZone(TimeZone.getDefault());
                StringBuffer sb = new StringBuffer(30);
                setPubDateStr(sdf.format(this.pubDate, sb, new FieldPosition(0)).toString());
            } catch (ParseException pe) {
                throw new ParseException("Unparseable date \"" + this.getPubDateStr() + "\" in " + this.text.substring(0, 200), pe.getErrorOffset());
            }
        } else if (strings.matches(dcDatePattern, this.text)) {
            String dcDateStr = strings.substitute("(.+) (00:00)$", "$1+$2", strings.getGroup(1));
            dcDateStr = strings.substitute("(.+)(\\d\\d):(\\d\\d)$", "$1$2$3", dcDateStr);
            try {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
                this.pubDate = sdf.parse(dcDateStr);
                setPubDateStr(this.pubDate);
            } catch (ParseException pe) {
                throw new ParseException("Unparseable date \"" + dcDateStr + "\" in " + this.text.substring(0, 200), pe.getErrorOffset());
            }
        }
    }

    /**
     * Set title, including surrounding tags.
     */
    protected void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set title with and without tags.
     *
     * @param title string containing title tags and value
     * @param titleValue string containing body of title tag only
     */
    protected void setTitleAndValue(String title, String titleValue) {
        this.title = title;
        this.titleValue = titleValue;
    }

    /**
     * Set title and update full text of item.
     */
    public void setTitle(String title, boolean updateText) {
        setTitle(title);
        if (updateText) {
            if (this.title != null && strings.matches(titlePattern, this.text)) {
                this.text = strings.getPreMatch() + this.title + strings.getPostMatch();
            }
        }
    }

    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Set globally unique ID string, including opening and closing tags,
     * without updating text field.
     *
     * @param guid string containing guid tags, optional attribute and value
     */
    protected void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Set guid with and without tags.
     *
     * @param guid string containing guid tags, optional attribute and value
     * @param guidValue string containing body of guid tag only
     */
    protected void setGuidAndValue(String guid, String guidValue) {
        this.guid = guid;
        this.guidValue = guidValue;
    }

    /**
     * Set globally unique ID and isPermalink attribute. Update text field.
     *
     * @param id unique ID value
     * @param isPermalink attribute value
     */
    protected void setGuid(String id, boolean isPermaLink) {
        setGuid(id);
        String guidStr = "<guid isPermaLink=\"" + new Boolean(isPermaLink).toString() + "\">" + id + "</guid>";
        if (strings.matches(guidPattern, this.text)) {
            this.text = strings.substitute(guidPattern, guidStr, this.text);
        } else if (strings.matches(linkPattern, this.text)) {
            this.text = strings.getPreMatch() + strings.toString() + guidStr + "\n" + strings.getPostMatch();
        } else if (strings.matches("</item>", this.text)) {
            this.text = strings.getPreMatch() + guidStr + "\n" + strings.toString() + strings.getPostMatch();
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDescription(String description, boolean updateText) {
        setDescription(description);
        if (updateText) {
            if (this.description != null && strings.matches(descriptionPattern, this.text)) {
                this.text = strings.getPreMatch() + this.description + strings.getPostMatch();
            }
        }
    }

    protected void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

    public void setEnclosure(String enclosure, boolean updateText) {
        setEnclosure(enclosure);
        if (updateText) {
            if (this.enclosure != null) {
                if (strings.matches(enclosurePattern, this.text)) {
                    this.text = strings.getPreMatch() + this.enclosure + strings.getPostMatch();
                } else {
                    if (strings.matches("(</pubDate>)(\\s*)", this.text)) {
                        this.text = strings.getPreMatch() + strings.getGroup(1) + strings.getGroup(2) + this.enclosure + strings.getGroup(2) + strings.getPostMatch();
                    } else if (strings.matches("</item>", this.text)) {
                        this.text = strings.getPreMatch() + this.enclosure + "\n" + strings.toString() + strings.getPostMatch();
                    }
                }
            }
        }
    }

    /**
     * Set Images arraylist to store information about images.
     *
     * @param images arrayList of Image objects
     */
    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }

    /**
     * Set Image arraylist to store information about images and update text.
     *
     * @param images arrayList of Image objects
     * @param updateText flag to update text of object
     */
    public void setImages(ArrayList<Image> images, boolean updateText) {
        setImages(images);
        if (updateText) {
            StringBuffer imagesBuffer = new StringBuffer(300);
            Iterator<Image> iter = this.images.iterator();
            while (iter.hasNext()) {
                Image image = iter.next();
                imagesBuffer.append(image.toString());
            }
            if (this.images != null && updateText) {
                setImagesText(imagesBuffer.toString(), updateText);
            }
        }
    }

    /**
     * Set text of media:content, media:description and media:text elements.
     *
     * @param imagesText string of media:content, media:description and media:text elements
     */
    public void setImagesText(String imagesText) {
        this.imagesText = imagesText;
    }

    /**
     * Set text of media:content and media:text elements.
     *
     * @param imagesText string of media:content and media:text pairs
     * @param updateText flag to update text of object
     */
    public void setImagesText(String imagesText, boolean updateText) {
        if (imagesText == null) {
            imagesText = "";
        }
        setImagesText(imagesText);
        if (updateText) {
            if (strings.matches(RssItem.imagesPattern, this.text)) {
                this.text = strings.getPreMatch() + imagesText + strings.getPostMatch();
            } else {
                if (strings.matches("(<enclosure[^>]*>)(\\s*)", this.text)) {
                    this.text = strings.getPreMatch() + strings.getGroup(1) + strings.getGroup(2) + imagesText + strings.getGroup(2) + strings.getPostMatch();
                } else if (strings.matches("</item>", this.text)) {
                    this.text = strings.getPreMatch() + imagesText + "\n" + strings.toString() + strings.getPostMatch();
                }
            }
        }
    }

    /**
     * Add media:content and media:text elements for an image and caption.
     */
    public void addImage(Image image) {
        this.images.add(image);
    }

    public void setContentEncoded(String contentEncoded) {
        this.contentEncoded = contentEncoded;
    }

    public void setContentEncoded(String contentEncoded, boolean updateText) {
        setContentEncoded(contentEncoded);
        if (updateText) {
            if (this.contentEncoded != null) {
                if (strings.matches(contentEncodedPattern, this.text)) {
                    this.text = strings.getPreMatch() + this.contentEncoded + strings.getPostMatch();
                } else {
                    if (strings.matches("(</description>)(\\s*)", this.text)) {
                        this.text = strings.getPreMatch() + strings.getGroup(1) + strings.getGroup(2) + this.contentEncoded + strings.getGroup(2) + strings.getPostMatch();
                    } else if (strings.matches("</item>", this.text)) {
                        this.text = strings.getPreMatch() + this.contentEncoded + "\n" + strings.toString() + strings.getPostMatch();
                    }
                }
            }
        }
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public void setPubDateStr(String pubDateStr) {
        this.pubDateStr = pubDateStr;
        if (strings.matches(pubDatePattern, this.text)) {
            this.text = strings.substitute(pubDatePattern, "<pubDate>" + pubDateStr + "</pubDate>", this.text);
        } else {
            this.text = strings.substitute("</item>", "<pubDate>" + pubDateStr + "</pubDate>\n</item>", this.text);
        }
    }

    public void setPubDateStr(Date pubDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        StringBuffer sb = new StringBuffer(30);
        setPubDateStr(sdf.format(pubDate, sb, new FieldPosition(0)).toString());
        setPubDate(pubDate);
    }

    public void setCurrentPubDate() {
        Date newPubDate = new Date();
        this.setPubDate(newPubDate);
        this.setPubDateStr(newPubDate);
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCreator(String creator, boolean updateText) {
        setCreator(creator);
        if (updateText) {
            if (this.creator != null) {
                if (strings.matches(creatorPattern, this.text)) {
                    this.text = strings.getPreMatch() + this.creator + strings.getPostMatch();
                } else if (strings.matches("</item>", this.text)) {
                    this.text = strings.getPreMatch() + "  " + creator + "\n" + strings.toString() + strings.getPostMatch();
                }
            }
        }
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRoundRobinSortKey(int sortKey) {
        this.roundRobinSortKey = sortKey;
    }

    public void setPriority(String priority) {
        setPriority(Integer.parseInt(priority));
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return this.title;
    }

    public String getTitleValue() {
        return this.titleValue;
    }

    public String getLink() {
        return this.link;
    }

    public String getGuid() {
        return this.guid;
    }

    public String getGuidValue() {
        return this.guidValue;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getCategory() {
        return this.category;
    }

    public String getComments() {
        return this.comments;
    }

    public String getEnclosure() {
        return this.enclosure;
    }

    public ArrayList<Image> getImages() {
        return this.images;
    }

    public String getImagesText() {
        return this.imagesText;
    }

    public String getContentEncoded() {
        return this.contentEncoded;
    }

    public Date getPubDate() {
        return this.pubDate;
    }

    public String getPubDateStr() {
        return this.pubDateStr;
    }

    public String getSource() {
        return this.source;
    }

    public String getCreator() {
        return this.creator;
    }

    public String getServer() {
        return this.server;
    }

    public String getUrl() {
        return this.url;
    }

    public String getText() {
        return this.text;
    }

    public int getRoundRobinSortKey() {
        return this.roundRobinSortKey;
    }

    public int getPriority() {
        return this.priority;
    }

    /**
     * Class to format media:content, media:description and media:text elements
     * containing an image and caption
     */
    class Image {

        private String url;

        private String width = "";

        private String height = "";

        private String fileSize = "";

        private String type = "";

        private String caption = "";

        Image(String url) {
            this.url = url;
        }

        public void setWidth(String width) {
            this.width = width;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public void setFileSize(String fileSize) {
            this.fileSize = fileSize;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getWidth() {
            return this.width;
        }

        public String getHeight() {
            return this.height;
        }

        public String getFileSize() {
            return this.fileSize;
        }

        public String getType() {
            return this.type;
        }

        public String getCaption() {
            return this.caption;
        }

        /**
         * Return image formatted as media:content element.
         * Omit optional elements for which we don't have values.
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer(200);
            buffer.append("      <media:content");
            if (this.width != null && !this.width.equals("")) {
                buffer.append(" width=\"");
                buffer.append(this.width);
                buffer.append("\"");
            }
            if (this.height != null && !this.height.equals("")) {
                buffer.append(" height=\"");
                buffer.append(this.height);
                buffer.append("\"");
            }
            if (this.fileSize != null && !this.fileSize.equals("")) {
                buffer.append(" fileSize=\"");
                buffer.append(this.fileSize);
                buffer.append("\"");
            }
            buffer.append(" medium=\"image\"");
            if (this.type == null || this.type.equals("")) {
                getImageTypeFromFileName();
            }
            if (this.type != null && !this.type.equals("")) {
                buffer.append(" type=\"");
                buffer.append(this.type);
                buffer.append("\"");
            }
            buffer.append(" url=\"");
            buffer.append(this.url);
            buffer.append("\">\n");
            if (this.caption != null && !this.caption.equals("")) {
                buffer.append("        <media:description>");
                buffer.append(this.caption);
                buffer.append("</media:description>\n");
                buffer.append("        <media:text>");
                buffer.append(this.caption);
                buffer.append("</media:text>\n");
            }
            buffer.append("      </media:content>\n");
            return buffer.toString();
        }

        /**
         * Return image formatted as enclosure element
         */
        public String toEnclosureString() {
            StringBuffer buffer = new StringBuffer(200);
            buffer.append("<enclosure length=\"" + this.fileSize + "\" type=\"" + this.type + "\" url=\"" + this.url + "\" />\n");
            return buffer.toString();
        }

        /**
         * Infer the image MIME type from the file extension.
         */
        private void getImageTypeFromFileName() {
            if (RssItem.this.strings.matchesIgnoreCase(".+/([^/]+\\.[^?]*)", this.url)) {
                String fileName = strings.getGroup(1);
                this.type = RssItem.mimeTypesMap.getContentType(fileName);
            }
        }
    }
}
