import net.wastl.webmail.server.*;
import net.wastl.webmail.server.http.*;
import net.wastl.webmail.ui.html.*;
import net.wastl.webmail.ui.xml.*;
import net.wastl.webmail.misc.*;
import net.wastl.webmail.exceptions.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 * Show WebMail help file
 *
 * provides: help
 * requires: content bar
 *
 * @author Sebastian Schaffert
 * @version
 */
public class WebMailHelp implements Plugin, URLHandler {

    public static final String VERSION = "2.0";

    public static final String URL = "/help";

    ExpireableCache cache;

    Storage store;

    public WebMailHelp() {
    }

    public void register(WebMailServer parent) {
        parent.getURLHandler().registerHandler(URL, this);
        cache = new ExpireableCache(20, (float) .9);
        store = parent.getStorage();
    }

    public String getName() {
        return "WebMailHelp";
    }

    public String getDescription() {
        return "This is the WebMail help content-provider.";
    }

    public String getVersion() {
        return VERSION;
    }

    public String getURL() {
        return URL;
    }

    public HTMLDocument handleURL(String suburl, HTTPSession session, HTTPRequestHeader header) throws WebMailException {
        UserData user = ((WebMailSession) session).getUser();
        Document helpdoc = (Document) cache.get(user.getPreferredLocale().getLanguage() + "/" + user.getTheme());
        if (helpdoc == null) {
            String helpdocpath = "file://" + store.getBasePath(user.getPreferredLocale(), user.getTheme()) + "help.xml";
            try {
                DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                helpdoc = parser.parse(helpdocpath);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new WebMailException("Could not parse " + helpdocpath);
            }
            cache.put(user.getPreferredLocale().getLanguage() + "/" + user.getTheme(), helpdoc);
        }
        Node n = session.getModel().importNode(helpdoc.getDocumentElement(), true);
        session.getModel().getDocumentElement().appendChild(n);
        if (header.isContentSet("helptopic") && session instanceof WebMailSession) {
            ((WebMailSession) session).getUserModel().setStateVar("helptopic", header.getContent("helptopic"));
        }
        HTMLDocument retdoc = new XHTMLDocument(session.getModel(), store.getStylesheet("help.xsl", user.getPreferredLocale(), user.getTheme()));
        session.getModel().getDocumentElement().removeChild(n);
        if (header.isContentSet("helptopic") && session instanceof WebMailSession) {
            ((WebMailSession) session).getUserModel().removeAllStateVars("helptopic");
        }
        return retdoc;
    }

    public String provides() {
        return "help";
    }

    public String requires() {
        return "content bar";
    }
}
