package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.util.*;

/**
 * All WebObjects applications have exactly one <code>ERXBrowserFactory</code> 
 * instance. Its primary role is to manage {@link ERXBrowser} objects. 
 * It provides facility to parse <code>"user-agent"</code> HTTP header and to 
 * create an appropriate browser object. It also maintains the 
 * browser pool to store shared <code>ERXBrowser</code> objects. 
 * Since <code>ERXBrowser</code> object is immutable, it can be 
 * safely shared between sessions and <code>ERXBrowserFactory</code> 
 * tries to have only one instance of <code>ERXBrowser</code> for 
 * each kind of web browsers.
 * <p>
 * The primary method called by {@link ERXSession} and {@link ERXDirectAction} 
 * is {@link #browserMatchingRequest browserMatchingRequest} 
 * which takes a {@link com.webobjects.appserver.WORequest WORequest} 
 * as the parameter and returns a shared instance of browser object. 
 * You actually wouldn't have to call this function by yourself 
 * because <code>ERXSession</code> and <code>ERXDirectAction</code> 
 * provide {@link ERXSession#browser browser}</code> method 
 * that returns a browser object for the current request for you.
 * <p>
 * Note that <code>ERXSession</code> and <code>ERXDirectAction</code> 
 * call <code>ERXBrowserFactory</code>'s 
 * {@link #retainBrowser retailnBrowser} and {@link #releaseBrowser releaseBrowser}  
 * to put the browser object to the browser pool when it is 
 * created and to remove the browser object from the pool when 
 * it is no longer referred from sessions and direct actions. 
 * <code>ERXSession</code> and <code>ERXDirectAction</code> 
 * automatically handle this and you do not have to call these 
 * methods from your code.<br>
 * <p>
 * The current implementation of the parsers support variety of 
 * Web browsers in the market such as Internet Explorer (IE), 
 * OmniWeb, Netscape, iCab and Opera, versions between 2.0 and 7.0. <br>
 * <p>
 * To customize the parsers for <code>"user-agent"</code> HTTP header, 
 * subclass <code>ERXBrowserFactory</code> and override methods 
 * like {@link #parseBrowserName parseBrowserName}, 
 * {@link #parseVersion parseVersion}, 
 * {@link #parseMozillaVersion parseMozillaVersion} and 
 * {@link #parsePlatform parsePlatForm}. 
 * Then put the following statement into the application's 
 * constructor. 
 * <p>
 * <code>ERXBrowserFactory.{@link #setFactory 
 * setFactory(new SubClassOfERXBrowserFactory())};</code>
 * <p>
 * If you want to use your own subclass of <code>ERXBrowser</code>, 
 * put the follwoing statement into the application's constructor.
 * <p>
 * <code>ERXBrowserFactory.factory().{@link #setBrowserClassName 
 * setBrowserClassName("NameOfTheSubClassOfERXBrowser")}</code>
 *
 * <p>
 * <pre>
 * This implementation is tested with the following browsers (or "user-agent" strings)
 * Please ask the guy (tatsuyak@mac.com) for WOUnitTest test cases. 
 * 
 * Mac OS X 
 * ----------------------------------------------------------------------------------
 * iCab 2.8.1       Mozilla/4.5 (compatible; iCab 2.8.1; Macintosh; I; PPC)
 * IE 5.21          Mozilla/4.0 (compatible; MSIE 5.21; Mac_PowerPC)
 * Netscape 7.0b1   Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.0rc2) Gecko/20020512 Netscape/7.0b1
 * Netscape 6.2.3   Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
 * OmniWeb 4.1-v422 Mozilla/4.5 (compatible; OmniWeb/4.1-v422; Mac_PowerPC)
 * Safari 1.0b(v48) Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/48 (like Gecko) Safari/48
 * 
 * Windows 2000
 * ----------------------------------------------------------------------------------
 * IE 6.0           Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)
 * IE 5.5           Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)
 * Netscape 6.2.3   Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
 * Netscape 4.79    Mozilla/4.79 [en] (Windows NT 5.0; U)
 * Opera 6.04       Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.04  [en]
 *
 * </pre>
 */
public class ERXBrowserFactory {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXBrowserFactory.class);

    private static final String _DEFAULT_BROWSER_CLASS_NAME = "er.extensions.ERXBasicBrowser";

    private static ERXBrowserFactory _factory = new ERXBrowserFactory();

    public static ERXBrowserFactory factory() {
        return _factory;
    }

    public static void setFactory(ERXBrowserFactory newFactory) {
        _factory = newFactory;
    }

    private String _browserClassName;

    /**
     * Returns the name of the {@link ERXBrowser} subclass. 
     * The default value is <code>"er.extensions.ERXBasicBrowser"</code>.
     * 
     * @return	the name of the ERXBrowser subclass; default to 
     *          <code>"er.extensions.ERXBasicBrowser"</code>
     * @see	#setBrowserClassName
     */
    public String browserClassName() {
        return _browserClassName;
    }

    /**
     * Sets the name of the {@link ERXBrowser} subclass.
     * 
     * @param name	the name of the ERXBrowser subclass; ignored if null
     * @see		#browserClassName
     * @see		#createBrowser
     */
    public void setBrowserClassName(String name) {
        if (name != null && name.length() > 0) _browserClassName = name;
    }

    public ERXBrowserFactory() {
        setBrowserClassName(_DEFAULT_BROWSER_CLASS_NAME);
    }

    /** 
     * Gets a shared browser object for given request. 
     * Parses <code>"user-agent"</code> string in the request and gets 
     * the appropiate browser object. 
     * <p>
     * This is the primary method to call from application logics, and 
     * once you get a browser object, you are responsible to call 
     * {@link #retainBrowser retainBrowser} to keep the browser 
     * object in the browser pool. 
     * <p>
     * You are also required to call {@link #releaseBrowser releaseBrowser} 
     * to release the browser from the pool when it is no longer needed. 
     * 
     * @param request 	WORequest
     * @return 		a shared browser object
     */
    public ERXBrowser browserMatchingRequest(WORequest request) {
        if (request == null) throw new IllegalArgumentException("Request can't be null.");
        String ua = (String) request.headerForKey("user-agent");
        if (ua == null) {
            return getBrowserInstance(ERXBrowser.UNKNOWN_BROWSER, ERXBrowser.UNKNOWN_VERSION, ERXBrowser.UNKNOWN_VERSION, ERXBrowser.UNKNOWN_PLATFORM, null);
        } else {
            String browserName = parseBrowserName(ua);
            String version = parseVersion(ua);
            String mozillaVersion = parseMozillaVersion(ua);
            String platform = parsePlatform(ua);
            NSDictionary userInfo = new NSDictionary(parseCPU(ua), "cpu");
            return getBrowserInstance(browserName, version, mozillaVersion, platform, userInfo);
        }
    }

    /** 
     * Gets a shared browser object from browser pool. If such browser 
     * object does not exist, this method will create one by using 
     * {@link #createBrowser createBrowser} method.
     * 
     * @param browserName string
     * @param version string
     * @param platform string
     * @param userInfo dictionary
     * @return a shared browser object
     */
    public synchronized ERXBrowser getBrowserInstance(String browserName, String version, String mozillaVersion, String platform, NSDictionary userInfo) {
        String key = _computeKey(browserName, version, mozillaVersion, platform, userInfo);
        ERXBrowser browser = (ERXBrowser) _browserPool().objectForKey(key);
        if (browser == null) browser = createBrowser(browserName, version, mozillaVersion, platform, userInfo);
        return browser;
    }

    /** 
     * Creates a new browser object for given parameters. Override this 
     * method if you need to provide your own subclass of {@link ERXBrowser}. 
     * If you override it, your implementation should not call <code>super</code>.
     * <p>
     * Alternatively, use {@link #setBrowserClassName} and {@link #browserClassName}.
     *
     * @param browserName string
     * @param version string
     * @param mozillaVersion string
     * @param platform string
     * @param userInfo dictionary
     * @return new browser object that is a concrete subclass of <code>ERXBrowser</code>
     * @see	#setBrowserClassName
     * @see	#browserClassName
     */
    public synchronized ERXBrowser createBrowser(String browserName, String version, String mozillaVersion, String platform, NSDictionary userInfo) {
        ERXBrowser browser = null;
        try {
            browser = _createBrowserWithClassName(browserClassName(), browserName, version, mozillaVersion, platform, userInfo);
        } catch (Exception ex) {
            log.error("Unable to create a browser for class name: " + browserClassName() + " with exception: " + ex.getMessage() + ".  Will use default classes." + " Please ensure that the fully-qualified name for the class is specified" + " if it is in a different package.");
        }
        if (browser == null) {
            try {
                browser = _createBrowserWithClassName(_DEFAULT_BROWSER_CLASS_NAME, browserName, version, mozillaVersion, platform, userInfo);
            } catch (Exception ex) {
                log.error("Unable to create even a default browser for class name: " + _DEFAULT_BROWSER_CLASS_NAME + " with exception: " + ex.getMessage() + "  Will instanciate a browser with regular" + " new " + _DEFAULT_BROWSER_CLASS_NAME + "(...) statement.");
                browser = new ERXBasicBrowser(browserName, version, mozillaVersion, platform, userInfo);
            }
        }
        return browser;
    }

    private ERXBrowser _createBrowserWithClassName(String className, String browserName, String version, String mozillaVersion, String platform, NSDictionary userInfo) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Class browserClass = Class.forName(className);
        Class[] paramArray = new Class[] { String.class, String.class, String.class, String.class, NSDictionary.class };
        java.lang.reflect.Constructor constructor = browserClass.getConstructor(paramArray);
        return (ERXBrowser) constructor.newInstance(new Object[] { browserName, version, mozillaVersion, platform, userInfo });
    }

    public synchronized void retainBrowser(ERXBrowser browser) {
        String key = _computeKey(browser);
        _browserPool().setObjectForKey(browser, key);
        _incrementReferenceCounterForKey(key);
    }

    public synchronized void releaseBrowser(ERXBrowser browser) {
        String key = _computeKey(browser);
        ERXMutableInteger count = _decrementReferenceCounterForKey(key);
        if (count == null) {
            _browserPool().removeObjectForKey(key);
        } else if (count.intValue() <= 0) {
            _browserPool().removeObjectForKey(key);
            _referenceCounters().removeObjectForKey(key);
        }
    }

    public String parseBrowserName(String userAgent) {
        String browserString = _browserString(userAgent);
        String browser = ERXBrowser.UNKNOWN_BROWSER;
        if (browserString.indexOf("MSIE") > -1) browser = ERXBrowser.IE; else if (browserString.indexOf("Safari") > -1) browser = ERXBrowser.SAFARI; else if (browserString.indexOf("OmniWeb") > -1) browser = ERXBrowser.OMNIWEB; else if (browserString.indexOf("iCab") > -1) browser = ERXBrowser.ICAB; else if (browserString.indexOf("Opera") > -1) browser = ERXBrowser.OPERA; else if (browserString.indexOf("Netscape") > -1) browser = ERXBrowser.NETSCAPE; else if (browserString.indexOf("Mozilla") > -1) browser = ERXBrowser.NETSCAPE;
        return browser;
    }

    public String parseVersion(String userAgent) {
        String browserString = _browserString(userAgent);
        int startpos;
        String version = ERXBrowser.UNKNOWN_VERSION;
        final String netscape6 = "Netscape6";
        startpos = browserString.indexOf(netscape6);
        if (startpos > -1) browserString = browserString.substring(startpos + netscape6.length());
        startpos = ERXStringUtilities.indexOfNumericInString(browserString);
        if (startpos > -1) {
            StringTokenizer st = new StringTokenizer(browserString.substring(startpos), " ;");
            if (st.hasMoreTokens()) version = st.nextToken();
        }
        return version;
    }

    public String parseMozillaVersion(String userAgent) {
        final String mozilla = "Mozilla/";
        String mozillaVersion = ERXBrowser.UNKNOWN_VERSION;
        int startpos = userAgent.indexOf(mozilla);
        if (startpos > -1) {
            StringTokenizer st = new StringTokenizer(userAgent.substring(startpos + mozilla.length()), " ;");
            if (st.hasMoreTokens()) mozillaVersion = st.nextToken();
        }
        return mozillaVersion;
    }

    public String parsePlatform(String userAgent) {
        String platform = ERXBrowser.UNKNOWN_PLATFORM;
        if (userAgent.indexOf("Win") > -1) platform = ERXBrowser.WINDOWS; else if (userAgent.indexOf("Mac") > -1) platform = ERXBrowser.MACOS; else if (userAgent.indexOf("Linux") > -1) platform = ERXBrowser.LINUX;
        return platform;
    }

    public String parseCPU(String userAgent) {
        String cpu = ERXBrowser.UNKNOWN_CPU;
        if (userAgent.indexOf("PowerPC") > -1) cpu = ERXBrowser.POWER_PC; else if (userAgent.indexOf("PPC") > -1) cpu = ERXBrowser.POWER_PC;
        return cpu;
    }

    private String _browserString(String userAgent) {
        String browserString = userAgent;
        int startpos;
        final String safari = "Safari";
        startpos = browserString.indexOf(safari);
        if (startpos > -1) browserString = browserString.substring(startpos);
        final String opera = "Opera";
        startpos = browserString.indexOf(opera);
        if (startpos > -1) browserString = browserString.substring(startpos);
        final String compatible = "compatible;";
        startpos = browserString.indexOf(compatible);
        if (startpos > -1) browserString = browserString.substring(startpos + compatible.length());
        final String netscape = "Netscape";
        startpos = browserString.indexOf(netscape);
        if (startpos > -1) browserString = browserString.substring(startpos);
        return browserString;
    }

    private NSMutableDictionary _browserPool;

    private NSMutableDictionary _browserPool() {
        if (_browserPool == null) _browserPool = new NSMutableDictionary();
        return _browserPool;
    }

    private NSMutableDictionary _referenceCounters;

    private NSMutableDictionary _referenceCounters() {
        if (_referenceCounters == null) _referenceCounters = new NSMutableDictionary();
        return _referenceCounters;
    }

    private ERXMutableInteger _incrementReferenceCounterForKey(String key) {
        ERXMutableInteger count = (ERXMutableInteger) _referenceCounters().objectForKey(key);
        if (count != null) count.increment(); else {
            count = new ERXMutableInteger(1);
            _referenceCounters().setObjectForKey(count, key);
        }
        if (log.isDebugEnabled()) log.debug("_incrementReferenceCounterForKey() - count = " + count + ", key = " + key);
        return count;
    }

    private ERXMutableInteger _decrementReferenceCounterForKey(String key) {
        ERXMutableInteger count = (ERXMutableInteger) _referenceCounters().objectForKey(key);
        if (count != null) count.decrement();
        if (log.isDebugEnabled()) log.debug("_decrementReferenceCounterForKey() - count = " + count + ", key = " + key);
        return count;
    }

    private String _computeKey(ERXBrowser browser) {
        return browser.browserName() + "." + browser.version() + "." + browser.mozillaVersion() + "." + browser.platform() + "." + browser.userInfo();
    }

    private String _computeKey(String browserName, String version, String mozillaVersion, String platform, NSDictionary userInfo) {
        return browserName + "." + version + "." + mozillaVersion + "." + platform + "." + userInfo;
    }
}
