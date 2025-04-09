package ca.cbc.sportwire.dochandler;

import java.io.File;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Vector;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.UnknownServiceException;
import java.net.MalformedURLException;
import java.net.ConnectException;
import org.jdom.Document;
import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import ca.cbc.sportwire.WireFeederProperties;
import ca.cbc.sportwire.QueueElement;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.log4j.Category;

/**
 * <p><code>JDOMToFile</code> is used by the DocQueue to process
 * JDOM items on the queue.  This handler assumes the doc is a JDOM
 * Document, then writes the XML to a file
 *
 * <p>Created: Wed Apr  3 16:05:01 EST 2002</p>
 *
 * @author <a href="mailto:garym@canada.com">Gary Lawrence Murphy</a>
 * @version $Id: JDOMToFile.java,v 1.16 2008-12-17 23:00:06 garym Exp $
 */
public class JDOMToFile implements DocHandler, WireFeederProperties {

    public class XPathNotFoundException extends Exception {
    }

    /**
	 * Set up a reporting category in Log4J
	 */
    static Category cat = Category.getInstance(JDOMToFile.class.getName());

    private ExtendedProperties config;

    /**
	 * Get the value of config.
	 * @return ExtendedProperties value of config.
	 */
    public ExtendedProperties getConfig() {
        return config;
    }

    /**
	 * Set the value of config.
	 * @param v  Value to assign to config.
	 */
    public void setConfig(ExtendedProperties v) {
        this.config = v;
    }

    /**
	 * <code>JDOMToFile</code> constructor stores the config so
	 * we can get at connection details later
	 *
	 * @param config an <code>ExtendedProperties</code> value
	 */
    public JDOMToFile(ExtendedProperties config) {
        cat.debug("creating new JDOMToFile Document Handler");
        setConfig(config);
    }

    private String getSaxClass() {
        return getConfig().getString(SAX_CLASS_PROPERTY, DEFAULT_SAX_DRIVER_CLASS);
    }

    public static String getXPathProperty(Document dom, String xpath) {
        List nodes = null;
        StringBuilder result = new StringBuilder("");
        cat.debug("Searching xpath=" + xpath);
        try {
            nodes = new JDOMXPath(xpath).selectNodes(dom);
            if (nodes.size() == 0) {
                String msg = "Property XPath " + xpath + " not found!";
                cat.error(msg);
            }
            try {
                for (Iterator a = nodes.iterator(); a.hasNext(); ) {
                    Object node = a.next();
                    if (node instanceof Attribute) {
                        result.append(((Attribute) node).getValue());
                    } else if (node instanceof Element) {
                        result.append(((Element) node).getText());
                    } else {
                        result.append(node.toString());
                    }
                    if (a.hasNext()) {
                        cat.error("XPath returns multiple nodes: " + xpath);
                    }
                }
            } catch (NoSuchMethodError e) {
                cat.error("XPath returns invalid node: " + xpath);
            }
        } catch (JaxenException j) {
            cat.error("XPath error on " + xpath + ": " + j.getClass().getName() + ": " + j.getMessage());
            j.printStackTrace();
        }
        cat.debug("Found xpath=>" + result.toString());
        return (result.length() > 0) ? result.toString() : null;
    }

    public static String datetimeToPath(String iso_date) {
        if (iso_date == null) return null;
        cat.debug("Path from " + iso_date);
        Date d = null;
        SimpleDateFormat target = null;
        try {
            List ds = new LinkedList();
            ds.add("yyyyMMdd'T'HHmmssZ");
            ds.add("yyyyMMdd'T'HHmmss z");
            ds.add("yyyyMMdd'T'HHmmss");
            Iterator i = ds.iterator();
            SimpleDateFormat inp;
            while ((d == null) && i.hasNext()) {
                try {
                    inp = new SimpleDateFormat((String) i.next());
                    d = inp.parse(iso_date);
                    cat.debug("Using date format " + inp);
                } catch (ParseException e) {
                }
            }
            assert (d != null) : "invalid date: " + iso_date;
            cat.debug("SportsML date-time: " + iso_date);
            StringBuilder fmt = new StringBuilder("yyyy").append(File.separator).append("MM").append(File.separator).append("dd");
            target = new SimpleDateFormat(fmt.toString());
        } catch (Exception e) {
            cat.error("Failed to parse " + iso_date + ": " + e.getClass().getName() + ": " + e.getMessage());
        }
        return (target != null) ? target.format(d) : null;
    }

    /**
	 * <code>SportsMLToFilename</code>: Extract a configurable
	 * attribute from some element to form the filename produced by
	 * the dochandler. The method will use the
	 * <tt>xml.filename.xpath</tt> property for the xpath that should
	 * return one and only one match.  This match can be an element or
	 * an attribute.
	 *
	 * @param dom a <code>Document</code> value
	 * @param config a <code>ExtendedProperties</code> value
	 * @return a <code>String</code> value
	 * @exception FileNotFoundException if an error occurs
	 * @see WireFeederProperties
	 */
    public static String SportsMLToFilename(Document dom, ExtendedProperties config) throws FileNotFoundException {
        StringBuilder fullpath = new StringBuilder(config.getString(XML_PATH_PROPERTY, DEFAULT_XML_PATH));
        String pathxp = config.getString(FILENAME_XPATH_PROPERTY, DEFAULT_FILENAME_XPATH);
        String datexp = config.getString(DATETIME_XPATH_PROPERTY, DEFAULT_DATETIME_XPATH);
        try {
            String pathid = getXPathProperty(dom, pathxp);
            if (pathid == null) {
                String msg = "XPath returns null: " + pathxp;
                cat.error(msg);
                throw new FileNotFoundException(msg);
            } else cat.debug("found path-id: " + pathid);
            String datepath = datetimeToPath(getXPathProperty(dom, datexp));
            if (datepath == null) {
                cat.error("date-time XPath returns null: " + datexp + "; using errors/");
                datepath = "errors";
            } else cat.debug("found date-path: " + datepath);
            StringBuilder relpath = new StringBuilder(datepath);
            if (!pathid.startsWith(File.separator)) {
                relpath.append(File.separator);
            }
            relpath.append(pathid);
            if (!(fullpath.toString().endsWith(File.separator) || relpath.toString().startsWith(File.separator))) {
                fullpath.append(File.separator);
            }
            if (!relpath.toString().endsWith(".xml")) relpath.append(".xml");
            fullpath.append(relpath.toString().replace(' ', '_'));
        } catch (Exception j) {
            cat.error(j.getClass().getName() + ": " + j.getMessage());
            j.printStackTrace();
            throw new FileNotFoundException(j.getMessage());
        }
        return fullpath.toString();
    }

    /**
	 * <code>isVolatile</code>: documents that match the system property
	 * expression are considered volatile and can thus be updated by new
	 * versions with the same FILENAME_XPATH_PROPERTY.
	 *
	 * NOTE: this method is currently a simple string match but should be
	 * implemented as a java.util.regex pattern or array of patterns after
	 * the Sportwire project is upgraded from the old ORO classes.
	 */
    protected boolean isVolatile(String filename) {
        String regex = getConfig().getString(VOLATILE_PATH_REGEX_PROPERTY, DEFAULT_VOLATILE_PATH_REGEX);
        return filename.contains(regex);
    }

    /**
	 * <code>prepareFile</code>: ensure the directory exists before
	 * proceeding; will throw an exception if the file exists unless it
	 * is classed as 'volatile'
	 *
	 * @param filename a <code>String</code> value
	 * @return a <code>File</code> value
	 */
    protected File prepareFile(String filename) throws IOException {
        File newfile = new File(filename);
        if (newfile.exists() && !isVolatile(filename)) {
            throw new IOException("Duplicate entry for " + filename + " rejected");
        }
        newfile.getParentFile().mkdirs();
        return newfile;
    }

    private static List deflist = null;

    private static List postlist = null;

    private static HashMap urlmap = null;

    /**
	 * <code>notifyPush</code>: Tell a list of external CGI of the incoming
	 * document by posting the filename to each address.  If the transaction
	 * fails, the process is repeated once, and false returned if it still
	 * fails.
	 */
    protected boolean notifyPush(String filename) {
        return notifyPush(filename, getConfig());
    }

    protected static boolean notifyPush(String filename, ExtendedProperties config) {
        boolean result = true;
        if (!config.getBoolean(NOTIFY_ENABLE_PROPERTY, NOTIFY_ENABLE_DEFAULT)) {
            cat.debug("Post Notification disabled");
            return result;
        }
        String path = config.getString(XML_PATH_PROPERTY, DEFAULT_XML_PATH);
        filename = filename.substring(path.length());
        if (postlist == null) {
            cat.debug("Post: initializing NOTIFY postlist");
            if (deflist == null) {
                cat.debug("Post: initializing default NOTIFY targets");
                deflist = new Vector();
                StringTokenizer st = new StringTokenizer(NOTIFY_POSTLIST_DEFAULT);
                while (st.hasMoreTokens()) {
                    deflist.add(st.nextToken());
                }
            }
            postlist = config.getVector(NOTIFY_POSTLIST_PROPERTY, (Vector) deflist);
            urlmap = new HashMap();
        }
        cat.debug("Post Notify on " + filename);
        for (Iterator i = postlist.iterator(); i.hasNext(); ) {
            String post = (String) i.next();
            cat.debug("Post to " + post);
            try {
                URL target = (URL) urlmap.get(post);
                if (target == null) {
                    target = new URL(post);
                    synchronized (urlmap) {
                        urlmap.put(post, target);
                    }
                }
                postNotice(target, filename);
            } catch (MalformedURLException e) {
                cat.error("Post URL error on " + post);
                result = false;
            }
        }
        return result;
    }

    /**
	 * <code>postNotice</code>: posts the filename to a CGI notifier
	 *
	 * @param action an <code>URL</code> value
	 * @param filename a <code>String</code> latest file
	 */
    protected static void postNotice(final URL action, final String filename) {
        (new Thread() {

            public void run() {
                int xtries = 1;
                while (xtries-- > 0) {
                    try {
                        URLConnection ws = action.openConnection();
                        ws.setDoOutput(true);
                        PrintWriter out = new PrintWriter(ws.getOutputStream());
                        out.print("filename=" + URLEncoder.encode(filename));
                        out.close();
                        cat.debug("Post Notify sent on " + filename);
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(ws.getInputStream()));
                            String inline;
                            if ((inline = in.readLine()) != null) {
                                cat.debug("Post ACK " + inline);
                            }
                            in.close();
                            return;
                        } catch (IOException e) {
                            cat.debug(filename + ": " + e.getClass().getName() + ": " + e.getMessage());
                            if (e.getMessage().matches(".*40[05] for URL.*")) {
                                cat.error("Post Notify error on " + filename + "; post rejected by remote.");
                                return;
                            } else if (e.getMessage().matches(".*500 for URL.*")) {
                                cat.warn("Post Server failed on " + filename + "; post ignored");
                            } else throw e;
                        }
                    } catch (ConnectException e) {
                        cat.error("Post ConnectionException " + action.toString() + ": " + e.getMessage());
                        return;
                    } catch (Exception e) {
                        cat.error("Post Error " + action.toString() + ": " + e.getClass().getName() + ": " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                }
                cat.error("Post abort on " + filename);
            }
        }).start();
    }

    /**
	 * <code>storeXML</code>: Creates a text rendition of the JDOM
	 * object and writes it to the specified filename.
	 *
	 * @param dom a <code>Document</code> value
	 * @param filename a <code>String</code> value
	 * @exception IOException if an error occurs
	 */
    public void storeXML(Document dom, String filename) throws IOException {
        XMLOutputter xml = new XMLOutputter(Format.getPrettyFormat());
        File xmlfile = prepareFile(filename);
        try {
            cat.debug("Writing " + filename + " in UTF-8");
            Writer fo = new OutputStreamWriter(new FileOutputStream(xmlfile), "UTF-8");
            xml.output(dom, fo);
            fo.close();
        } catch (UnsupportedEncodingException e) {
            cat.error(e.getClass().getName() + ": " + e.getMessage());
            throw e;
        }
    }

    /**
	 * <code>processDocument</code>: handler for the DocQueue, this
	 * method takes the QueueElement payload, parses out a filename
	 * and writes the XML out to that path.  Once the file is written,
	 * a notice is posted to the log and an optional HTTP notice is
	 * posted to a remote CGI where the path can be used to provide
	 * real-time data-push services.
	 *
	 * @param d an <code>QueueElement</code> containing a JDOM payload
	 * @return a <code>null</code> value signifying the end of processing
	 */
    public Object processDocument(Object d) {
        if (!(d instanceof QueueElement)) {
            cat.error("Invalid document object rejected.");
            return null;
        }
        QueueElement doc = (QueueElement) d;
        try {
            Document dom = (Document) doc.getValue();
            assert (dom != null);
            DocType dtd = dom.getDocType();
            if (dtd == null) {
                cat.warn("assuming Sportsml DTD in " + doc.getTag());
                dtd = new DocType(getConfig().getString(SML_ROOT_PROPERTY, SML_ROOT_DEFAULT), getConfig().getString(SML_PUBLIC_PROPERTY, SML_PUBLIC_DEFAULT), getConfig().getString(SML_DTD_PROPERTY, SML_DTD_DEFAULT));
            }
            dom.setDocType(new DocType(dtd.getElementName()));
            cat.debug("doc " + doc.getTag() + " DTD: " + dtd.getElementName() + " SystemID: " + dtd.getSystemID());
            String filename = SportsMLToFilename(dom, getConfig());
            cat.debug("FILENAME: " + filename);
            storeXML(dom, filename);
            cat.info("XML: " + filename);
            if (!notifyPush(filename)) {
                cat.warn("Notification failed on " + filename);
            }
        } catch (FileNotFoundException e) {
            cat.error("FILE ERROR - skipped " + doc.getTag() + ": " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            cat.error("FILE STORE ERROR - skipped " + doc.getTag() + ": " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            cat.error("XML ERROR - skipped " + doc.getTag() + ": " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }
}
