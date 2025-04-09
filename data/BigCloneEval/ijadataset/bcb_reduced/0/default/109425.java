import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tools.ant.util.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parser for a Bamboo RSS feed.<br/>
 * Usage: Create an instance of this class and fill the rssUrl attribute with the URI
 * to the Bamboo RSS feed. Then call the loadRssFeed method.
 * @author Arjan van der Veen <avdveen@palanthir.nl>
 */
public class BambooRssParser {

    private String rssUrl;

    public List<BambooBuild> loadRssFeed() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        System.out.println(this.rssUrl);
        Document doc = builder.parse(this.rssUrl);
        NodeList items = doc.getElementsByTagName("item");
        List<BambooBuild> builds = new ArrayList<BambooBuild>();
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            NodeList titleNodeList = item.getElementsByTagName("title");
            String title = titleNodeList.item(0).getTextContent();
            NodeList dateNodeList = item.getElementsByTagName("dc:date");
            String dateStr = dateNodeList.item(0).getTextContent();
            Date date = DateUtils.parseIso8601DateTime(dateStr);
            BambooBuild bb = this.createBambooBuild(title, date);
            builds.add(bb);
        }
        return builds;
    }

    private BambooBuild createBambooBuild(String text, Date date) {
        BambooBuild bb = new BambooBuild();
        bb.setBuildDate(date);
        Pattern pattern = Pattern.compile("(.+)-([0-9]+) (was|has) ([A-Z]+)[: ](.+)");
        Matcher matcher = pattern.matcher(text);
        boolean found = matcher.matches();
        if (found) {
            String buildplan = matcher.group(1);
            int buildnumber = Integer.parseInt(matcher.group(2));
            String status = matcher.group(4);
            bb.setBuildplan(buildplan);
            bb.setBuildnumber(buildnumber);
            bb.setBuildstatus(status);
        }
        return bb;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }
}
