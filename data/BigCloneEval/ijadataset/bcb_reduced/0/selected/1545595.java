package net.villonanny;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.villonanny.type.ResourceType;
import net.villonanny.type.ResourceTypeMap;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.slf4j.helpers.MessageFormatter;

public class Util {

    private static final Logger log = Logger.getLogger(Util.class);

    public static final String USERAGENT_DEFAULT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)";

    private static String ERROR_MESSAGE_BUNDLE_NAME = "Messages";

    private static boolean utf8;

    public static final long MILLI_SECOND = 1000;

    public static final long MILLI_MINUTE = 60 * MILLI_SECOND;

    public static final long MILLI_HOUR = 60 * MILLI_MINUTE;

    private Translator translator;

    private String serverHost;

    private int serverPort;

    private List<String> loginPostNames;

    private List<String> loginPostValues;

    private String loginPassword = null;

    private long serverTimeMillisDelta = 0;

    private String baseUrl;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    ;

    private HierarchicalConfiguration serverConfiguration;

    private HttpClient client;

    private String lastVisitPage = "";

    public static final String P_FLAGS = "(?s)(?i)(?u)";

    private static int screenx = 1280;

    private static int screeny = 1024;

    public boolean writePostLog = false;

    static {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            screenx = screenSize.width;
            screeny = screenSize.height;
        } catch (Throwable e) {
            log.debug("Can't get screen size, using default");
        }
    }

    public Util(HierarchicalConfiguration serverConfig) {
        this(serverConfig, true);
    }

    public Util(HierarchicalConfiguration serverConfig, boolean setLanguage) {
        this.serverConfiguration = serverConfig;
        this.client = getHttpClient();
        if (setLanguage) {
            String languageCode = serverConfig.getString("/@language");
            this.translator = new Translator(languageCode);
            if (!ConfigManager.hasLanguage(languageCode)) {
                String message = Util.getLocalMessage("msg.noLanguageConfig", this.getClass());
                throw new FatalException(MessageFormatter.format(message, languageCode, ConfigManager.CONFIGDIR));
            }
        } else {
            this.translator = new Translator();
        }
    }

    public static void log(String message, Exception e) {
        log.error(message + ": " + e.getMessage());
        if (log.isDebugEnabled()) {
            log.error("Stack Trace", e);
        }
    }

    public static String format(Date date) {
        return VilloNanny.formatter.format(date);
    }

    public static Date getIncrediblyLongTime() {
        return new Date(System.currentTimeMillis() + 31536000000L);
    }

    public String httpGetPage(String urlString) throws ConversationException {
        return httpGetPage(urlString, false);
    }

    public String httpGetPage(String urlString, boolean quick) throws ConversationException {
        log.debug("Getting " + urlString + " (with login check) ...");
        if (urlString == null) {
            log.warn("Nothing to get");
            return "";
        }
        String page = httpGetPageNoLogin(urlString, quick);
        if (isLoginPage(page)) {
            String s = "Login page returned; performing login for " + urlString;
            EventLog.log(s);
            page = loginWithPage(page, urlString, quick);
            if (isLoginPage(page)) {
                throw new FatalException("Can't login");
            }
            page = httpGetPageNoLogin(urlString, quick);
        }
        log.debug("Got (with login check) " + urlString);
        return page;
    }

    public String httpGetPageNoLogin(String urlString, boolean quick) throws ConversationException {
        Console.getInstance().checkFlags();
        GetMethod get = new GetMethod(urlString);
        addHeaders(get);
        try {
            log.debug("Getting " + urlString + " ...");
            client.executeMethod(get);
            lastVisitPage = urlString;
            String page = get.getResponseBodyAsString();
            Pattern p = Pattern.compile(" src=\"([^\"\\?]*)[^\"]*\"");
            Matcher m = p.matcher(page);
            while (m.find()) {
                String src = m.group(1);
                getIfNotCachedAndDrop(urlString, src);
            }
            return page;
        } catch (java.net.ConnectException e) {
            throw new ConversationException("Connection to \"" + urlString + "\" failed (check network/proxy setup).", e);
        } catch (IOException e) {
            throw new ConversationException("Can't read page " + urlString, e);
        } finally {
            get.releaseConnection();
            shortestPause(quick);
            log.debug("Got " + urlString);
        }
    }

    /**
	 * Force login
	 * @throws ConversationException
	 */
    public void login(boolean sharp) throws ConversationException {
        int counter = 2;
        while (true) {
            try {
                String loginUrlString = serverConfiguration.getString("/loginUrl");
                String page = httpGetPage(loginUrlString);
                break;
            } catch (ConversationException e) {
                EventLog.log("Login failed: " + e.getMessage());
                log.error(e);
                if (counter-- > 0) {
                    EventLog.log("Retrying...");
                    Util.shortestPause(sharp);
                } else {
                    log.error("Exiting " + serverConfiguration.getString("/@desc"));
                    throw e;
                }
            }
        }
    }

    /**
	 * 
	 * @loginForm the html page containing the login form
	 * @urlString the url that returned the login form
	 */
    private String loginWithPage(String loginForm, String urlString, boolean quick) throws ConversationException {
        Pattern p;
        Matcher m;
        fillLoginParameters(loginForm, urlString);
        String loginPostString = baseUrl + "dorf1.php";
        Calendar localTime = new GregorianCalendar();
        localTime.set(Calendar.YEAR, 1970);
        localTime.set(Calendar.MONTH, Calendar.JANUARY);
        localTime.set(Calendar.DAY_OF_MONTH, 1);
        String pageAfterLogin = httpPostPage(loginPostString, loginPostNames, loginPostValues, quick);
        CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
        Cookie[] logoncookies = cookiespec.match(serverHost, serverPort, "/", false, client.getState().getCookies());
        if (logoncookies.length == 0) {
            this.loginPassword = null;
            throw new ConversationException("Authentication failed (no cookies)");
        }
        log.info("Authentication ok");
        p = Pattern.compile("(?s)id=\"tp1\"[^>]*>(.*?)</span>");
        m = p.matcher(pageAfterLogin);
        if (m.find()) {
            String serverTimeString = m.group(1);
            Date serverDate;
            try {
                serverDate = timeFormat.parse(serverTimeString);
            } catch (ParseException e) {
                throw new ConversationException("Can't parse server time: " + serverTimeString);
            }
            this.serverTimeMillisDelta = localTime.getTimeInMillis() - serverDate.getTime();
        } else {
            throw new ConversationException("Can't find server time");
        }
        return pageAfterLogin;
    }

    private void fillLoginParameters(String loginForm, String urlString) throws ConversationException {
        URL loginUrl;
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        try {
            loginUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new FatalException(String.format("loginUrl for server \"%s\" is invalid", serverConfiguration.getString("/@desc")), e);
        }
        baseUrl = urlString.substring(0, urlString.indexOf("/", "http://".length()) + 1);
        serverHost = loginUrl.getHost();
        serverPort = loginUrl.getPort();
        if (serverPort == -1) {
            serverPort = loginUrl.getDefaultPort();
            if (serverPort == -1) {
                serverPort = 80;
            }
        }
        loginPostNames = new ArrayList<String>();
        loginPostValues = new ArrayList<String>();
        String userNameField;
        Pattern p = Pattern.compile("<input.*?type=\"text\".*?name=\"(.*?)\"");
        Matcher m = p.matcher(loginForm);
        if (m.find()) {
            userNameField = m.group(1);
        } else {
            throw new ConversationException("Can't find username input field");
        }
        if (m.find()) {
            log.warn("Too many username input fields; ignoring...");
        }
        loginPostNames.add(userNameField);
        String user = serverConfiguration.getString("/user");
        loginPostValues.add(user);
        String pwdField;
        p = Pattern.compile("<input.*?type=\"password\".*?name=\"(.*?)\"");
        m = p.matcher(loginForm);
        if (m.find()) {
            pwdField = m.group(1);
        } else {
            throw new ConversationException("Can't find password input field");
        }
        if (m.find()) {
            log.warn("Too many password input fields; ignoring...");
        }
        loginPostNames.add(pwdField);
        String pwd = serverConfiguration.getString("/password");
        if (pwd == null) {
            pwd = this.loginPassword;
            if (pwd == null) {
                EventLog.log("Waiting for password input");
                pwd = inputLine("Type the password for " + user + " on " + serverConfiguration.getString("/@desc") + ": ");
                this.loginPassword = pwd;
            }
        }
        loginPostValues.add(pwd);
        addHiddenPostFields(loginForm, "<form method=\"post\" name=\"snd\" action=\"dorf1.php\">", loginPostNames, loginPostValues);
        addButtonCoordinates("s1", 80, 20, loginPostNames, loginPostValues);
        try {
            int pos = loginPostNames.indexOf("w");
            loginPostValues.set(pos, screenx + ":" + screeny);
        } catch (Exception e) {
            log.warn("Can't find login parameter 'w' (ignoring)");
        }
    }

    public static void addButtonCoordinates(String prefix, int x, int y, List<String> names, List<String> values) {
        int vx = (int) (Math.random() * x);
        int vy = (int) (Math.random() * y);
        names.add(prefix + ".x");
        values.add(Integer.toString(vx));
        names.add(prefix + ".y");
        values.add(Integer.toString(vy));
    }

    /**
	 * @return the start position of the form
	 */
    public static int addHiddenPostFields(String page, String startFromPattern, List<String> names, List<String> values) throws ConversationException {
        Pattern p;
        Matcher m;
        p = Pattern.compile(startFromPattern);
        m = p.matcher(page);
        if (!m.find()) {
            throw new ConversationException("Can't find start of form with pattern \"" + startFromPattern + "\"");
        }
        int startPos = m.start();
        p = Pattern.compile("</form>");
        m = p.matcher(page);
        m.region(startPos, page.length());
        int endPos = page.length();
        if (m.find()) {
            endPos = m.end();
        }
        p = Pattern.compile("<input(?=[^>]*type=\"hidden\")[^>]*name=\"(.*?)\"[^>]*value=\"(.*?)\"");
        m = p.matcher(page);
        m.region(startPos, endPos);
        while (m.find()) {
            String name = m.group(1);
            String value = m.group(2);
            names.add(name);
            values.add(value);
        }
        p = Pattern.compile("<input(?=[^>]*type=\"hidden\")[^>]*value=\"(.*?)\"[^>]*name=\"(.*?)\"");
        m = p.matcher(page);
        m.region(startPos, endPos);
        while (m.find()) {
            String name = m.group(1);
            String value = m.group(2);
            names.add(name);
            values.add(value);
        }
        return startPos;
    }

    public boolean isLoginPage(String page) {
        Pattern p;
        Matcher m;
        p = Pattern.compile("(?s)(?i)<input[^>]+id\\s*=\\s*\"btn_login\"");
        m = p.matcher(page);
        return m.find();
    }

    public void addHeaders(HttpMethod m) {
        m.addRequestHeader("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        m.addRequestHeader("Accept-Language", "en,it;q=0.5");
        m.addRequestHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        m.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        if (!lastVisitPage.equals("")) {
            m.addRequestHeader("Referer", lastVisitPage);
        }
    }

    public String httpPostPage(String url, List<String> postNames, List<String> postValues, boolean quick) throws ConversationException {
        return httpPostPage(url, postNames, postValues, quick, false);
    }

    public String httpPostPage(String url, List<String> postNames, List<String> postValues, boolean quick, boolean doLog) throws ConversationException {
        PostMethod httpPost = new PostMethod(url);
        addHeaders(httpPost);
        NameValuePair[] postData = new NameValuePair[postNames.size()];
        for (int i = 0; i < postData.length; i++) {
            postData[i] = new NameValuePair(postNames.get(i), postValues.get(i));
        }
        httpPost.setRequestBody(postData);
        String page;
        try {
            client.executeMethod(httpPost);
            if (doLog) {
                return "";
            }
            lastVisitPage = url;
            page = httpPost.getResponseBodyAsString();
            if (doLog) {
                return "";
            }
        } catch (IOException e) {
            throw new ConversationException("Can't read page " + url, e);
        } finally {
            httpPost.releaseConnection();
        }
        int statuscode = httpPost.getStatusCode();
        if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) || (statuscode == HttpStatus.SC_MOVED_PERMANENTLY) || (statuscode == HttpStatus.SC_SEE_OTHER) || (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
            Header header = httpPost.getResponseHeader("location");
            if (header != null) {
                String newuri = header.getValue();
                if ((newuri == null) || (newuri.equals(""))) {
                    newuri = "/";
                }
                log.debug("Redirect target: " + newuri);
                page = httpGetPageNoLogin(newuri, quick);
            } else {
                throw new ConversationException("Invalid redirect (location=null)");
            }
        }
        log.debug("Posted " + url);
        return page;
    }

    public void httpQuickPostPage(PostMethod httpPost) throws ConversationException {
        try {
            client.executeMethod(httpPost);
        } catch (IOException e) {
            throw new ConversationException("Can't submit page", e);
        } finally {
            httpPost.releaseConnection();
        }
    }

    private void getIfNotCachedAndDrop(String pageUrl, String src) {
        try {
            String relative = src;
            if (src.startsWith("http://")) {
                int pos = relative.indexOf("/", "http://".length());
                relative = relative.substring(pos);
            } else {
                int pos = pageUrl.indexOf("/", "http://".length());
                if (pos == -1) {
                    src = pageUrl + "/" + src;
                } else {
                    src = pageUrl.substring(0, pos + 1) + src;
                }
            }
            String cachePath = ConfigManager.getString("/imageCache/@path", "imageCache");
            File file = new File(cachePath, relative);
            if (!file.canRead()) {
                log.debug("Caching resource : " + src);
                GetMethod getObj = new GetMethod(src);
                try {
                    addHeaders(getObj);
                    client.executeMethod(getObj);
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (Exception e) {
                    log.warn("Error while caching resource " + src + " (ignored)", e);
                } finally {
                    getObj.releaseConnection();
                }
            }
        } catch (Exception e) {
            log.warn("Error while checking cache for resource " + src + " (ignored):" + e.getMessage());
        }
    }

    public static void sleep(long milli) {
        if (milli < 0) {
            log.debug("Not sleeping: negative value " + milli);
            return;
        }
        Date awake = new Date(System.currentTimeMillis() + milli);
        String s = String.format("Sleeping %s minutes until %s ...", milli / MILLI_MINUTE, format(awake));
        try {
            Thread.sleep(milli);
        } catch (InterruptedException e) {
            log.debug("Sleep interrputed");
        }
    }

    public void dayPause(long minimumPauseMillis, boolean sharp) {
        log.debug("Day pause");
        long minMillis = ConfigManager.getInt("dayExtraPauseMinutes/@min", 5) * MILLI_MINUTE;
        long maxMillis = ConfigManager.getInt("dayExtraPauseMinutes/@max", 10) * MILLI_MINUTE;
        doPause(minimumPauseMillis, minMillis, maxMillis, sharp);
    }

    public void nightPause(long minimumPauseMillis, boolean sharp) {
        log.debug("Night pause");
        long minMillis = ConfigManager.getInt("nightExtraPauseMinutes/@min", 60) * MILLI_MINUTE;
        long maxMillis = ConfigManager.getInt("nightExtraPauseMinutes/@max", 90) * MILLI_MINUTE;
        doPause(minimumPauseMillis, minMillis, maxMillis, sharp);
    }

    private void doPause(long neededMillis, long minAddMillis, long maxAddMillis, boolean sharp) {
        if (Console.getInstance().isQuitting()) {
            return;
        }
        if (sharp) {
            sleep(neededMillis);
        } else {
            long millis = neededMillis + (long) (Math.random() * (maxAddMillis - minAddMillis) + minAddMillis);
            sleep(millis);
        }
    }

    public static void shortestPause(boolean quick) {
        if ("true".equalsIgnoreCase(System.getProperty("QUICK"))) {
            log.warn("Forcing quick pause");
            quick = true;
        }
        double millis = Math.random();
        if (Console.getInstance().isQuitting()) {
            quick = true;
        }
        if (quick) {
            millis = (millis * 500.0) + 100;
        } else {
            millis = (millis * 500.0) + 100;
        }
        try {
            log.debug("Sleeping " + (long) millis + (quick ? " (quick)" : ""));
            Thread.sleep((long) millis);
        } catch (InterruptedException e) {
            log.debug("Sleep interrputed");
        }
        log.debug("Resuming after pausing " + (long) millis + (quick ? " (quick)" : ""));
    }

    public static String getFullUrl(String currentPageUrlString, String newUrlEnd) {
        return currentPageUrlString.substring(0, currentPageUrlString.lastIndexOf("/") + 1) + newUrlEnd;
    }

    /**
	 * Convert "HH:mm:ss" into seconds
	 * @param timeString
	 * @return
	 */
    public static int timeToSeconds(String timeString) {
        int value = 0;
        String[] parts = timeString.trim().split(":");
        for (int i = 0; i < parts.length; i++) {
            String elem = parts[i];
            int elemVal = Integer.parseInt(elem);
            value = value * 60 + elemVal;
        }
        return value;
    }

    /**
	 * Transforms "HH:mm:ss" into date
	 * @param timeNeeded
	 * @return
	 */
    public Date getCompletionTime(String timeNeeded) {
        int seconds = timeToSeconds(timeNeeded);
        Calendar time = new GregorianCalendar();
        time.add(Calendar.SECOND, seconds);
        return time.getTime();
    }

    public Date calcWhenAvailable(ResourceTypeMap production, ResourceTypeMap availableResources, ResourceTypeMap neededResources) {
        float hoursNeeded = 0;
        for (ResourceType res : ResourceType.values()) {
            if (res == ResourceType.FOOD) {
                continue;
            }
            int missing = neededResources.get(res) - availableResources.get(res);
            float time = missing / (float) production.get(res);
            if (time > hoursNeeded) {
                hoursNeeded = time;
            }
        }
        int seconds = (int) (hoursNeeded * 3600);
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    public boolean isNightTime() {
        try {
            final String FORMAT1 = "yyyy MMM dd";
            String timeFrom = ConfigManager.getString("nightTime/@from", "01:00");
            String timeTo = ConfigManager.getString("nightTime/@to", "06:00");
            SimpleDateFormat format1 = new SimpleDateFormat(FORMAT1);
            String nowDateString = format1.format(new Date());
            SimpleDateFormat format2 = new SimpleDateFormat(FORMAT1 + " HH:mm");
            Date dateFrom = format2.parse(nowDateString + " " + timeFrom);
            Date dateTo = format2.parse(nowDateString + " " + timeTo);
            if (dateFrom.after(dateTo)) {
                dateFrom = new Date(dateFrom.getTime() - 1000L * 3600 * 24);
            }
            Date now = new Date();
            return (now.after(dateFrom) && now.before(dateTo));
        } catch (ParseException e) {
            throw new FatalException("Invalid time format", e);
        }
    }

    /**
	 * Simulate a user that is away doing something else
	 * @param minimumPauseMillis 
	 */
    public void userPause(long minimumPauseMillis, boolean sharp) {
        try {
            if (isNightTime()) {
                nightPause(minimumPauseMillis, sharp);
            } else {
                dayPause(minimumPauseMillis, sharp);
            }
        } catch (Exception e) {
            log.error("Unexpected exception caught (ignored)", e);
            Util.sleep(5000);
        }
    }

    /**
	 * Convert milliseconds into "HH:mm:ss" 
	 * @param milliPause
	 * @return
	 */
    public String milliToTimeString(long milliPause) {
        long hours = milliPause / 3600000;
        long min = (milliPause - hours * 3600000) / 60000;
        long sec = (milliPause % 60000) / 1000;
        return hours + ":" + min + ":" + sec;
    }

    public static String inputLine(String prompt) {
        return inputLine(prompt, null);
    }

    public static String inputLine(String prompt, String defaultValue) {
        System.out.println(prompt);
        BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            String result = readIn.readLine();
            if (defaultValue != null && (result == null || result.trim().length() == 0)) {
                System.out.println(defaultValue);
                return defaultValue;
            }
            return result;
        } catch (IOException e) {
            throw new FatalException("Can't read user input", e);
        }
    }

    public Date serverTimeToLocalTime(Date serverTime) {
        return new Date(serverTime.getTime() + serverTimeMillisDelta);
    }

    public Translator getTranslator() {
        return translator;
    }

    public static void saveTestPattern(String desc, Pattern pattern, String page) {
        String outputDir = ConfigManager.getString("patternDebug/@path", "logs/patterns");
        PatternDebugger patternDebugger = new PatternDebugger(desc, pattern, page);
        patternDebugger.toFile(outputDir);
    }

    /**
	 * Returns a localised message, loaded from a message bundle.
	 * @param key the key to the message
	 * @param caller the class of the caller, needed to retrieve the bundle file from the same package of the caller
	 * @return
	 */
    public static String getLocalMessage(String key, Class caller) {
        try {
            ResourceBundle bundle = getResourceBundle(ERROR_MESSAGE_BUNDLE_NAME, caller);
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
	 * Ottiene un bundle caricando il file che si trova nello stesso package della classe indicata
	 * @param bundleName nomeBundle
	 * @param callerClass chiamante
	 * @return ResourceBundle
	 * @throws MissingResourceException bundle non esiste
	 */
    public static ResourceBundle getResourceBundle(String bundleName, Class caller) throws MissingResourceException {
        StringBuffer fullBundleName = new StringBuffer(caller.getPackage().getName()).append(".").append(bundleName);
        return ResourceBundle.getBundle(fullBundleName.toString());
    }

    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
        client.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, (int) (MILLI_SECOND * 30));
        client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, (int) (MILLI_SECOND * 30));
        client.getParams().setParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, true);
        String userAgent = ConfigManager.getString("userAgent", USERAGENT_DEFAULT);
        client.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
        if (ConfigManager.getBoolean("proxy/@enabled", false)) {
            String host = ConfigManager.getString("proxy/hostName");
            int port = ConfigManager.getInt("proxy/hostPort");
            client.getHostConfiguration().setProxy(host, port);
            String user = ConfigManager.getString("proxy/proxyUser");
            String pwd = ConfigManager.getString("proxy/proxyPassword");
            if (user != null) {
                Credentials credentials = null;
                String ntHost = ConfigManager.getString("proxy/NTHost");
                String ntDomain = ConfigManager.getString("proxy/NTDomain");
                if ((ntHost != null) && (ntDomain != null)) {
                    credentials = new NTCredentials(user, pwd, ntHost, ntDomain);
                } else {
                    credentials = new UsernamePasswordCredentials(user, pwd);
                }
                AuthScope authScope = new AuthScope(host, port);
                client.getState().setProxyCredentials(authScope, credentials);
            }
        }
        return client;
    }

    /**
	 * Check if there is a "-utf8" command line argument
	 * @param args
	 * @return
	 */
    public static void setUtf8(String[] args) {
        utf8 = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].trim().toLowerCase().equals("-utf8")) {
                utf8 = true;
                break;
            }
        }
        log.debug("utf8 is " + utf8);
    }

    public static String startTimeString(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String[] split = args[i].trim().split("=", 2);
            if (split[0].toLowerCase().equals("-starttime") && split.length > 1) {
                return split[1];
            }
        }
        return null;
    }

    public static String getEncodingString() {
        return utf8 ? "UTF-8" : "ISO-8859-1";
    }
}
