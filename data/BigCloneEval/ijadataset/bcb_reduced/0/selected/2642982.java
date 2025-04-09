package url;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import util.Timer;

/**
 *
 * This code downlaoded from
 * https://code.google.com/p/nicenet/
 *
 *
 * This is a class to make life easier by having a URL class that has built into it
 * the handling of errors beyonds those addressed in java.net URL/URI combo.
 * <p>An address in NiceURL
 * has all the parts of an address in java.net.URL except for the fragment (# and everything after it - anchors). This is because that
 * part of the URL is not essential to finding its content, and we want resources to be unique by location, not
 * the anchoring "sub-locations" provided by the fragment information.</p>
 *
 * <p>
 * NiceURL's main difference from URL is the ability to validate addresses a little better than either URL/URI.
 * While it still, like URL, relies on having absolute sources specified in the constructor, and
 * throws the same exceptions for appropriate reasons, it has some additional functionality.
 *
 * <ul>
 * <li> Static methods for retrieving content and producing encoded URL strings.
 * <li> Source address validation, including getting additional information.
 * <li> Scraping for links in the page
 * <li> Parsing HTML links
 * </ul>
 *
 * <p>
 * It does <i>not</i> extend java.net.URL, which is final.
 *
 * <h3>Version</h3>
 * <ul>
 * <li>2.1 - Cosmetic changes; lots of variables because private with getter and setter methods
 * <li>2.0 - Reads from InputStream byte by byte into a StringBuffer
 * <li>1.7 - uses a timing thread to cap http queries.
 * <li>1.6 - some more features, including a way to toggle (as a static variable) what this optimizes - http requests or memory - by
 * specifying whether to save the content of a page oncce we get it, or more on wiht our lives. <i>Note: this issue, I hope,
 * will be addressed by the CleverURL class, which has not been finished yet</i>
 * <li>1.5 - part of the Drivers package, has additional static methods to help retrieve online content.
 * In its validation process, NiceURL has grown quite a bit more sophisticated.
 * <li>1.0 - part of the Drivers.Retriever package, only does some basic string operations on
 * the source string
 * </ul>
 *
 *
 * @author KTK
 */
public class NiceURL {

    /**
     * Time limit on HTTP request.
     */
    private static int TIMER_LIMIT = 30000;

    /**
     * Getter for HTTP request time limit
     */
    public static int getTimerLimit() {
        return TIMER_LIMIT;
    }

    /**
     * Setter for HTTP request time limit
     */
    public static void setTimerLimit(int limit) {
        TIMER_LIMIT = limit;
    }

    private static String[] GOOD_EXT = new String[] { "htm", "html", "htmls", "php", "php4", "php5", "phtm", "phtml", "sht", "shtm", "shtml", "stm", "stml", "xhtm", "xhtml", "xhtml", "txt", "asp" };

    /**
     * A list of exensions that contain legal, non-directory content. We assume,
     * which is mostly reasonable, that any address that has this extension
     * is not a directory. (We do not assume the converse of that, though)
     */
    public static String[] getGoodExtensions() {
        return GOOD_EXT;
    }

    public static void addGoodExtension(String ext) {
        String[] nGE = copyStrArr(GOOD_EXT, 1);
        nGE[GOOD_EXT.length] = ext;
        GOOD_EXT = nGE;
    }

    public static void clearGoodExtension() {
        GOOD_EXT = new String[0];
    }

    private static String[] BAD_EXT = new String[] { "pdf", "wmv", "doc", "docx", "swf", "jpg", "jpeg", "mp4", "mp3", "avi" };

    public static String[] getBadExtensions() {
        return BAD_EXT;
    }

    /**
     * A list of extensions to give up on. The package in questions only reads online content,
     * not the .pdf files and other documents. These are extensions we simply "give up" on
     * when we see them.
     */
    public static void addBadExtension(String ext) {
        String[] nBE = copyStrArr(BAD_EXT, 1);
        nBE[BAD_EXT.length] = ext;
        BAD_EXT = nBE;
    }

    public static void clearBadExtension() {
        BAD_EXT = new String[0];
    }

    /**
     * List of encoding pairs; described below.
     */
    private static String[] ENCODING = new String[] { " ", "20" };

    /**
     * Getter for the list of url encoding pairs, s.t. even-indexed (2n) elements are characters forbidden
     * in the HTTP protocol, and the adjacent odd-indexed elements (2n+1) are the corresponding
     * hex encodings. Used to replace every occurence of an illegal
     * character at 2n with a string formed by concatenating a "%" to the front of the (2n+1)
     * encoding.
     */
    public static String[] getURLEncodings() {
        return ENCODING;
    }

    /**
     * Adds an encoding pair
     * @param HTTP: string that could appear in HTTP address, but wihtou the % sign (eg, HTTP space would be "20" here)
     * @param translation: what this would be interpresed as (eg, for the above=20, this would be=" ")
     */
    public static void addURLEncodings(String HTTP, String translation) {
        String[] nENC = copyStrArr(ENCODING, 2);
        nENC[ENCODING.length] = translation;
        nENC[ENCODING.length + 1] = HTTP;
        ENCODING = nENC;
    }

    /**
     * Delected all HTTP address encoding pairs.
     */
    public static void clearURLEncodings() {
        ENCODING = new String[0];
    }

    /**
     * Helper for setters for the string arrays above.
     * @param orig
     * @param add
     * @return
     */
    private static String[] copyStrArr(String[] orig, int add) {
        String[] n = new String[orig.length + add];
        for (int i = 0; i < orig.length; i++) {
            n[i] = orig[i];
        }
        return n;
    }

    /**
     * This is what determines whether or not to save the contents, or make repeated http requests.
     */
    private boolean careful = true;

    /**
     * Returns list of links found on a page;
     * doesn't return anything if getLinkList
     */
    private ArrayList<String[]> addressList;

    private static Timer timer;

    /**
     * Link "context:" what is before the link (from  the previous period), in the link text,
     * and after the link (until the next period)
     */
    private HashSet<String[]> linkContext;

    /**
     * Underlying URL object
     */
    private URL url;

    /**
     * Actual contents of hte page
     */
    private StringBuilder contents;

    /**
     * Contains flags;
     * [0]: true iff this address points someplace valid (this would be false if it points
     * to a document with an illegal extension)
     * [1]: true iff this is a directory
     */
    private boolean[] flags = new boolean[] { false, false };

    private boolean verbose;

    public static void prep() {
        if (timer == null) {
            timer = new Timer(TIMER_LIMIT);
            timer.start();
        }
    }

    public static void prep(int limit) {
        if (limit == -1) {
            timer = null;
        } else {
            timer = new Timer(limit);
            timer.start();
        }
    }

    public NiceURL(String source) throws MalformedURLException {
        this(source, true, true);
    }

    /**
     *
     * @param source String address of the file or directory we would like to access
     * @param toughValidate flag to say whether to do the validation which requires content retrieval
     * @throws java.net.MalformedURLException Exception happens when we cannot form a URL object; a frequent example
     *          is passing a relative url, or a mailto:/javascript:. This allows this non-URL-child to behave a little
     *          like the (final) java.net.URL class.
     */
    public NiceURL(String source, boolean deepValidate, boolean verbose) throws MalformedURLException {
        prep();
        this.verbose = verbose;
        linkContext = new HashSet<String[]>();
        source = encodeURLSource(source);
        URL tester = new URL(source);
        flags[0] = true;
        String path = tester.getPath();
        String host = tester.getHost();
        String query = tester.getQuery();
        String protocol = tester.getProtocol();
        int port = tester.getPort();
        if (path.equals("")) {
            source = protocol + "://" + host;
            if (port != -1) {
                source += ":" + port;
            }
            source += "/" + tester.getFile();
            url = new URL(source);
            flags[1] = true;
            return;
        }
        if (path.charAt(path.length() - 1) == '/') {
            url = new URL(makeDirectory(protocol, host, port, noDirPath(path), query));
            return;
        }
        int i;
        for (i = path.length() - 1; i >= 0 && path.charAt(i) != '.'; i--) {
        }
        String extension = path.substring(i + 1, path.length());
        for (String disallowed : BAD_EXT) {
            if (extension.equals(disallowed)) {
                flags[0] = false;
                break;
            }
        }
        if (!flags[0]) {
            return;
        }
        flags[1] = true;
        for (String allowed : GOOD_EXT) {
            if (extension.equals(allowed)) {
                flags[1] = false;
                break;
            }
        }
        if (deepValidate) {
            deepValidate(tester, false);
        } else {
            url = tester;
        }
    }

    public NiceURL(URL readyUrl, boolean care) {
        prep();
        url = readyUrl;
        careful = care;
    }

    public void addContext(String pre, String text, String post) {
        linkContext.add(new String[] { pre, text, post });
    }

    /**
     * This function takes the guessed input URL and tries to understand it.
     * This involves making multiple calls to figure out if it is a directory.
     * Knowing whether something is a directory is essential, but not obvious,
     * because of the magic of dynamic URLs.<Br>
     * Now, a helpful rule of thumb
     * is that a directory can be accessed with or without the slash at the end;
     * but a file can only be read, at least using this library, when there is
     * no slash at the end. Thus, we first try to pretend it IS a directory
     * and append a slash. Then, if the file cannot be read, we know it is
     * not a directory.
     * @param tester
     * @throws java.net.MalformedURLException
     */
    protected void deepValidate(URL tester, boolean pack) throws MalformedURLException {
        String path = tester.getPath();
        String host = tester.getHost();
        String query = tester.getQuery();
        String protocol = tester.getProtocol();
        int port = tester.getPort();
        url = tester;
        if (flags[1]) {
            tester = new URL(makeDirectory(protocol, host, port, path, query));
            contents = getURLContents(tester, pack, verbose);
        }
        if (contents == null || !flags[1]) {
            flags[1] = false;
            contents = getURLContents(url, pack, verbose);
            if (contents == null) {
                if (verbose) System.err.println("Failed to read " + tester);
                flags[0] = false;
            } else {
            }
        } else {
            flags[1] = true;
            url = tester;
        }
        if (careful) {
            contents = null;
        }
    }

    protected String makeDirectory(URL u) {
        if (u == null) {
            return null;
        }
        String path = u.getPath();
        String host = u.getHost();
        String query = u.getQuery();
        String protocol = u.getProtocol();
        int port = u.getPort();
        return makeDirectory(protocol, host, port, path, query);
    }

    protected String makeDirectory(String protocol, String host, int port, String path, String query) {
        path = noDirPath(path, true);
        host = noDirPath(host, true);
        String source = protocol + "://" + host;
        if (port != -1) {
            source += ":" + port;
        }
        if (path.length() > 0) {
            source += "/" + path;
        }
        source += "/";
        if (query != null) {
            source += "?" + query;
        }
        return source;
    }

    protected String noPath(String protocol, String host, int port) {
        host = noDirPath(host);
        String source = protocol + "://" + host;
        if (port != -1) {
            source += ":" + port;
        }
        return source;
    }

    protected String noDirPath(String path) {
        return noDirPath(path, false);
    }

    protected String noDirPath(String path, boolean stripfront) {
        while (path != null && path.length() > 0 && path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }
        while (path != null && path.length() > 0 && path.charAt(0) == '/' && (stripfront || (path.length() > 1 && path.charAt(1) == '/'))) {
            path = path.substring(1, path.length());
        }
        if (verbose) System.out.println("Stripped path: " + stripfront + "  " + path);
        return path;
    }

    /**
     * Uses the ENCODING list to validate an http string.
     * @param in String representation of a URL
     * @return String representation of a URL which has all the "illegal" characters replaced with corresponding encodings
     */
    public static String encodeURLSource(String in) {
        String out = "";
        boolean doEncode;
        for (int i = 0; i < in.length(); i++) {
            doEncode = false;
            for (int j = 0; j < ENCODING.length && !doEncode; j += 2) {
                if (in.charAt(i) == ENCODING[j].charAt(0)) {
                    out += "%" + ENCODING[j + 1];
                    doEncode = true;
                }
            }
            if (!doEncode) {
                out += in.charAt(i);
            }
        }
        return out;
    }

    /**
     *
     * Retrieves, and returns in String format, the contents of the website corresponding to the specified
     * NiceURL instance.
     * @param u NiceURL to read from1
     * @return
     */
    public static StringBuilder getURLContents(NiceURL u) {
        if (u == null || !u.isNice()) {
            return null;
        }
        return getURLContents(u.url);
    }

    /**
     *
     * Retrieves, and returns in String format, the contents of the website corresponding to the specified
     * URL instance.
     * @param u URL to read from
     * @return
     */
    public static StringBuilder getURLContents(URL u) {
        return getURLContents(u, false, false);
    }

    public static StringBuilder getURLContents(URL u, boolean pack, boolean verbose) {
        if (u == null) {
            return null;
        }
        try {
            long time = System.currentTimeMillis();
            URLConnection uconnect = u.openConnection();
            uconnect.setReadTimeout(30000);
            String header = uconnect.getContentType();
            if (header == null) {
                return null;
            }
            int end = (header.length() >= 4) ? 4 : header.length();
            header = header.substring(0, end);
            header = header.toLowerCase();
            if (!header.equals("text")) {
                return null;
            }
            header = uconnect.getContentEncoding();
            long old = (time - uconnect.getLastModified());
            InputStream stream = (InputStream) uconnect.getContent();
            StringBuilder output = new StringBuilder();
            int letter = stream.read();
            if (timer != null) {
                timer.begin();
            }
            while (letter != -1) {
                if (timer != null && !timer.ok()) {
                    timer.reset();
                    if (verbose) System.out.println("Inputstream timeout - WARNING: this _should_ only happen when we have a non-text site.");
                    return null;
                }
                if (pack) {
                    if (letter == '\\' || letter == '\'' || letter == '\"') {
                        output.append('\\');
                    }
                    output.append((char) letter);
                } else {
                    output.append((char) letter);
                }
                letter = stream.read();
            }
            if (timer != null) {
                timer.reset();
            }
            stream.close();
            return output;
        } catch (Exception ex) {
            System.err.println("getURLContents(" + u + ")\t " + ex);
            return null;
        }
    }

    /**
     * Prints out the validated address (<i>not</i> the contents!)
     * @return String representation of the NiceURL
     */
    @Override
    public String toString() {
        if (!flags[0]) {
            return null;
        } else {
            return url.toString();
        }
    }

    /**
     * Does this NiceURL point to a valid online resource?
     * @return true iff so
     */
    public boolean isNice() {
        return flags[0];
    }

    /**
     * Returns (when possible) the contents of the online resource this NiceURL refers to.
     * <i>Please Note:</i> This is an accessor method; once a website has been read once during the life of this instance,
     * it is not read again.
     * @return String of the contents
     */
    public StringBuilder getContents() {
        if (!flags[0]) {
            return null;
        }
        if (careful) {
            return getURLContents(this);
        }
        if (contents == null) {
            contents = getURLContents(this);
        }
        return contents;
    }

    public String getAddress() {
        return url.toExternalForm();
    }

    public URL getURL() {
        return url;
    }

    /**
     * Getter for directory flag
     * @return true iff address points to a directory
     */
    public boolean isDir() {
        return flags[1];
    }

    /**
     * A simplification so that we can use .equals on this object with a string
     * address. This is done by makign a new NiceURL instance. So that means
     * we are doing all this validation stuff over again. Be aware.
     * @param address
     * @return
     */
    public boolean equals(String address) {
        try {
            NiceURL test = new NiceURL(address);
            return this.equals(test);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * @param other
     * @return <code>true</code> iff the URL-containign fields of the two objects are themselves equal
     */
    @Override
    public boolean equals(Object other) {
        if (other.getClass() != this.getClass()) {
            return false;
        }
        return (this.getURL()).equals(((NiceURL) other).getURL());
    }

    /**
     * Hashes over the URL-ontaining field
     * @return hashcode
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.url != null ? this.url.hashCode() : 0);
        return hash;
    }

    /**
     * Can change the contents quickly.
     * @param contents New content
     * @return wasblank Return <code>true</code> iff the existing contents field was blank. So <code>false</code> means
     * that we overwrote something - not the jurisdiction of this method, but should be dealth with appropriately!
     */
    protected boolean setContents(StringBuilder contents) {
        boolean out = true;
        if (this.contents != null) {
            out = false;
        }
        this.contents = contents;
        return out;
    }

    /**
     * Check what we are saving - memory or http requests
     * @return careful Returns a descriptive string for one ofthe two things we could be optimizing over
     */
    public String saveToString() {
        return careful ? "memory" : "number of http requests";
    }

    /**
     * Check what we are saving - memory or number of http requests
     * @return careful Returns <code>true</code> iff we try to save memory
     */
    public boolean save() {
        return careful;
    }

    /**
     * Set to static mode to save memory, and purge the current contents
     */
    public void saveMemory() {
        contents = null;
        careful = true;
    }

    /**
     * Set to static mode to save http requests
     */
    public void saveRequests() {
        careful = false;
    }

    /**
     * Uses several reqular expressions to (1) find all formulations of <code>&lt;a href=</code> and
     * (2) find, if it is specified a formulation of <code>&lt;base href=</code>. This happens
     * by first finding a list of all tags, then iterating over them and running them through
     * each of the other two expression automata. Once we find <code>&lt;a href=</code>, we
     * have a helper function that figures out the actual link text. Then, we end up with a list of
     * links, and a base URL (if it is not directly specified, we take the current CleverURL's location
     * as telling us the base). Then we make everything absolute, and save it in the <code>address_list</code>
     * field, which is accessible by other functions.
     * <br>
     * <strong>Note: now that I think about it, this REALLY should be in NiceURL/CleverURL itself.....</strong>
     * @see Retriever getURLfromLink()
     * @param nu
     * @return
     */
    public ArrayList<String[]> getLinkList() {
        if (!isNice()) {
            return null;
        }
        StringBuilder tempcont = getContents();
        if (tempcont == null) {
            return null;
        }
        long times = System.currentTimeMillis();
        Pattern regexLink = Pattern.compile("([^.<>]*)<\\s*[Aa]\\s+[^>]*[Hh][Rr][Ee][Ff]\\s*=([^>]*)>([^<>]*)<\\s*/\\s*[Aa]\\s*>([^.<>]*)");
        Pattern regexBase = Pattern.compile("<\\s*[Bb][Aa][Ss][Ee]\\s+[^>]*[Hh][Rr][Ee][Ff]\\s*=\\s*[\"']?([^\"' >]+)[\"' >]");
        Matcher matcher;
        Matcher baseMatcher;
        matcher = regexLink.matcher(tempcont);
        ArrayList<String[]> temp_list = new ArrayList<String[]>();
        int lastend = -1;
        int streak = 0;
        int current = -1;
        while (matcher.find()) {
            String linkText = matcher.group(3);
            String linkAddr = getURLfromLink(matcher.group(2));
            String linkPre = matcher.group(1);
            String linkPost = matcher.group(4);
            if (matcher.start() == lastend) {
                linkPre = (temp_list.get(current))[2] + (temp_list.get(current))[1] + (temp_list.get(current))[3];
                streak++;
                for (int i = 0; i < streak; i++) {
                    String[] rewrite = temp_list.get(current - i);
                    rewrite[3] += linkText + linkPost;
                    temp_list.set(current - i, rewrite);
                }
            } else if (streak > 0) {
                String[] rewrite = temp_list.get(current);
                rewrite[3] += linkText + linkPost;
                temp_list.set(current, rewrite);
                streak = 0;
            } else {
                streak = 0;
            }
            current++;
            lastend = matcher.end();
            temp_list.add(new String[] { linkAddr, linkText, linkPre, linkPost });
        }
        String base = "none";
        baseMatcher = regexBase.matcher(tempcont);
        while (baseMatcher.find()) {
            base = getURLfromLink(baseMatcher.group(1));
        }
        if (base.equals("none")) {
            base = getAddress();
        }
        URL baseURL = url;
        try {
            int delim = 0;
            for (delim = base.length() - 1; delim >= 0; delim--) {
                if (base.charAt(delim) == '/') {
                    break;
                }
            }
            base = base.substring(0, delim);
            baseURL = new URL(base);
        } catch (Exception e) {
        }
        URL tester;
        String url2;
        addressList = new ArrayList<String[]>();
        times = System.currentTimeMillis() - times;
        System.out.println("  It took " + times + " millis to get link list from " + tempcont.length() + " characters");
        times = System.currentTimeMillis();
        for (String[] urlInfo : temp_list) {
            if (urlInfo[0].length() >= 10 && urlInfo[0].substring(0, 10).equals("javascript")) {
                continue;
            }
            if (urlInfo[0].length() == 0) {
                continue;
            }
            if (urlInfo[0].charAt(0) == '#') {
                continue;
            }
            if (urlInfo[0].length() > 0 && urlInfo[0].charAt(0) == '/') {
                url2 = noPath(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort()) + noDirPath(urlInfo[0]);
            } else {
                url2 = urlInfo[0];
            }
            try {
                tester = new URL(url2);
                addressList.add(new String[] { tester.toString(), urlInfo[1], urlInfo[2], urlInfo[3] });
            } catch (MalformedURLException e1) {
                try {
                    while (url2.length() > 0 && url2.charAt(0) == '/') {
                        url2 = url2.substring(1);
                    }
                    tester = new URL(noDirPath(baseURL.toExternalForm()) + "/" + url2);
                    addressList.add(new String[] { tester.toString(), urlInfo[1], urlInfo[2], urlInfo[3] });
                } catch (MalformedURLException e2) {
                }
            }
        }
        times = System.currentTimeMillis() - times;
        if (verbose) System.out.println("It took " + times + " millis to populate link list from " + addressList.size() + " links in the list");
        return addressList;
    }

    /**
     * This is a helper method for the link getter method. IT would be useless if that
     * function were overwritten - so it is final. It basically takes a string
     * that follows some formulation of <code>&lt;a href=</code> and parses the link
     * out of it. It does so very carefully, assuming nothing about quotation marks.
     * <br>
     * Either the link begins with a single quote, a double quote, or some character. If
     * it is some characrer, then it reads until it sees a space, and terminated, declaring
     * the string up to that space the link. If it is a quote, then is parses by watching out for that
     * quote again. It is able to deal with escaped quotes.
     * <br>
     * One might think that this sounds very much like a very simple grammar. Indeed, one would be gloriously correct.
     * You CANNOT do what this function does in a regular expression. Anyone who says otherwise
     * has clearly not noticed how much leeway coders get with a-href's.
     * @param link
     * @return
     */
    public static final String getURLfromLink(String link) {
        String out;
        if (link.charAt(0) == '"' || link.charAt(0) == '\'') {
            char ending = link.charAt(0);
            boolean escape = false;
            out = "";
            for (int i = 1; i < link.length(); i++) {
                if (link.charAt(i) == '\\') {
                    escape = true;
                } else if (!escape && link.charAt(i) == ending || link.charAt(i) == '\n') {
                    break;
                } else {
                    escape = false;
                }
                out += link.charAt(i);
            }
        } else {
            out = "";
            for (int i = 0; i < link.length() && link.charAt(i) != ' ' && link.charAt(i) != '\t' && link.charAt(i) != '\n'; i++) {
                out += link.charAt(i);
            }
        }
        return out;
    }
}
