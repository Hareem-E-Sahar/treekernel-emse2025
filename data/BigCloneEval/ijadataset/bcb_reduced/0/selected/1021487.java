package org.apache.nutch.parse.rss;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.parse.Parser;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.rss.structs.RSSItem;
import org.apache.nutch.parse.rss.structs.RSSChannel;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.protocol.Protocol;
import org.apache.nutch.protocol.ProtocolFactory;
import org.apache.nutch.util.LogUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.commons.feedparser.FeedParserListener;
import org.apache.commons.feedparser.FeedParser;
import org.apache.commons.feedparser.FeedParserFactory;

/**
 * 
 * @author mattmann
 * @version 1.0
 * 
 * <p>
 * RSS Parser class for nutch
 * </p>
 */
public class RSSParser implements Parser {

    public static final Log LOG = LogFactory.getLog("org.apache.nutch.parse.rss");

    private Configuration conf;

    /**
     * <p>
     * Implementation method, parses the RSS content, and then returns a
     * {@link ParseImpl}.
     * </p>
     * 
     * @param content
     *            The content to parse (hopefully an RSS content stream)
     * @return A {@link ParseImpl}which implements the {@link Parse}interface.
     */
    public Parse getParse(Content content) {
        List theRSSChannels = null;
        try {
            byte[] raw = content.getContent();
            FeedParser parser = FeedParserFactory.newFeedParser();
            FeedParserListener listener = new FeedParserListenerImpl();
            parser.parse(listener, new ByteArrayInputStream(raw), null);
            theRSSChannels = ((FeedParserListenerImpl) listener).getChannels();
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                e.printStackTrace(LogUtil.getWarnStream(LOG));
                LOG.warn("nutch:parse-rss:RSSParser Exception: " + e.getMessage());
            }
            return new ParseStatus(ParseStatus.FAILED, "Can't be handled as rss document. " + e).getEmptyParse(getConf());
        }
        StringBuffer contentTitle = new StringBuffer(), indexText = new StringBuffer();
        List theOutlinks = new Vector();
        if (theRSSChannels != null) {
            for (int i = 0; i < theRSSChannels.size(); i++) {
                RSSChannel r = (RSSChannel) theRSSChannels.get(i);
                contentTitle.append(r.getTitle());
                contentTitle.append(" ");
                indexText.append(r.getDescription());
                indexText.append(" ");
                if (r.getLink() != null) {
                    try {
                        if (r.getDescription() != null) {
                            theOutlinks.add(new Outlink(r.getLink(), r.getDescription(), getConf()));
                        } else {
                            theOutlinks.add(new Outlink(r.getLink(), "", getConf()));
                        }
                    } catch (MalformedURLException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("MalformedURL: " + r.getLink());
                            LOG.warn("Attempting to continue processing outlinks");
                            e.printStackTrace(LogUtil.getWarnStream(LOG));
                        }
                        continue;
                    }
                }
                for (int j = 0; j < r.getItems().size(); j++) {
                    RSSItem theRSSItem = (RSSItem) r.getItems().get(j);
                    indexText.append(theRSSItem.getDescription());
                    indexText.append(" ");
                    String whichLink = null;
                    if (theRSSItem.getPermalink() != null) whichLink = theRSSItem.getPermalink(); else whichLink = theRSSItem.getLink();
                    if (whichLink != null) {
                        try {
                            if (theRSSItem.getDescription() != null) {
                                theOutlinks.add(new Outlink(whichLink, theRSSItem.getDescription(), getConf()));
                            } else {
                                theOutlinks.add(new Outlink(whichLink, "", getConf()));
                            }
                        } catch (MalformedURLException e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("MalformedURL: " + whichLink);
                                LOG.warn("Attempting to continue processing outlinks");
                                e.printStackTrace(LogUtil.getWarnStream(LOG));
                            }
                            continue;
                        }
                    }
                }
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace("nutch:parse-rss:getParse:indexText=" + indexText);
                LOG.trace("nutch:parse-rss:getParse:contentTitle=" + contentTitle);
            }
        } else if (LOG.isTraceEnabled()) {
            LOG.trace("nutch:parse-rss:Error:getParse: No RSS Channels recorded!");
        }
        Outlink[] outlinks = (Outlink[]) theOutlinks.toArray(new Outlink[theOutlinks.size()]);
        if (LOG.isTraceEnabled()) {
            LOG.trace("nutch:parse-rss:getParse:found " + outlinks.length + " outlinks");
        }
        ParseData parseData = new ParseData(ParseStatus.STATUS_SUCCESS, contentTitle.toString(), outlinks, content.getMetadata());
        parseData.setConf(this.conf);
        return new ParseImpl(indexText.toString(), parseData);
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getConf() {
        return this.conf;
    }

    public static void main(String[] args) throws Exception {
        String url = args[0];
        Configuration conf = NutchConfiguration.create();
        RSSParser parser = new RSSParser();
        parser.setConf(conf);
        Protocol protocol = new ProtocolFactory(conf).getProtocol(url);
        Content content = protocol.getProtocolOutput(new Text(url), new CrawlDatum()).getContent();
        Parse parse = parser.getParse(content);
        System.out.println("data: " + parse.getData());
        System.out.println("text: " + parse.getText());
    }
}
