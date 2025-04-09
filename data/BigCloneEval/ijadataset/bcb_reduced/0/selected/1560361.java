package org.stummi.swpb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.stummi.swpb.objects.APIObject;
import org.stummi.swpb.objects.LoginQuery;
import org.stummi.swpb.objects.SiteInfo;
import org.stummi.swpb.objects.SiteMatrix;
import org.stummi.swpb.objects.WikiList;
import org.stummi.swpb.objects.WikiTokens;
import org.stummi.swpb.querys.ListingQuery;
import org.stummi.swpb.querys.PropQuery;
import org.stummi.swpb.querys.getTokens;

/**
 * This is the mainclass for the WikipediaBot.
 * @author Stummi
 */
public class WikimediaBot extends DefaultHttpClient {

    private static String DEFAULT_SERVER = "de.wikipedia.org";

    private static String DEFAULT_APIFILE = "/w/api.php";

    private static ArrayList<NameValuePair> defaultparams = new ArrayList<NameValuePair>();

    private SiteMatrix siteMatrix;

    private URI apiurl;

    private int requestCounter;

    private String[] defaultSiteInfos = "general|namespaces|namespacealiases|statistics|dbrepllag|usergroups".split("\\|");

    private WikiTokens tokens;

    /**
	 * Default Constructor<br /> calls the main Constructor with the
	 * default-server (atm 'de.wikipedia.org') and the default-apifile (atm
	 * '/w/api.php')
	 *
	 * @throws URISyntaxException
	 *             if the server or apifile url invalid (that should never
	 *             happen)
	 * @see #WikipediaBot(String server)
	 * @see #WikipediaBot(String server, String apifile)
	 */
    public WikimediaBot() throws URISyntaxException {
        this(DEFAULT_SERVER, DEFAULT_APIFILE);
    }

    /**
	 * use this constructor, if you want to specify the wikimedia-project, you
	 * want to work on. (by default, it is "de.wikipedia.org")
	 *
	 * @param server
	 *            must be the hostname of the WikiMedia-project you want to work
	 *            with your bot. e.g. "de.wikipedia.org" or "en.wikiquote.org"
	 * @throws URISyntaxException
	 *             if the server or apifile url invalid. The server should only
	 *             be the hostname like "de.wikipedia.org"
	 * @see #WikipediaBot(String server, String apifile)
	 */
    public WikimediaBot(String server) throws URISyntaxException {
        this(server, DEFAULT_APIFILE);
    }

    /**
	 * use this constructor, if you want to specify the wikimedia-project and
	 * the apifile. By default, you should not need this constructor, because
	 * the apifile is always the default "/w/api.php"
	 *
	 * @param server
	 *            must be the hostname of the WikiMedia-project you want to work
	 *            with your bot. e.g. "de.wikipedia.org" or "en.wikiquote.org"
	 * @param apifile
	 *            the api-file, where the bot shall work. by default
	 *            "/w/api.php";
	 * @throws URISyntaxException
	 *             if the server or the api-file you specified is wrong
	 */
    public WikimediaBot(String server, String apifile) throws URISyntaxException {
        defaultparams.add(new BasicNameValuePair("format", "json"));
        apiurl = new URI("http://" + server + apifile);
    }

    /**
	 * this method gives you the complete sitematrix of the wikimedia-project.
	 * <br/> so you can get a list of all wikimedia-sites, which exists.
	 *
	 * @param forceNew
	 *            - if that true, the SiteMatrix will allways be loadet. if it
	 *            is false, the method will return a cached sitematrix, if it
	 *            still was loaded before
	 * @return the complete sitematrix of all wikimedia-projects
	 * @throws WikimediaApiException
	 */
    public SiteMatrix getSiteMatrix(boolean forceNew) throws WikimediaApiException {
        if (forceNew) this.siteMatrix = null;
        return getSiteMatrix();
    }

    /**
	 * gives you the sitematrix.
	 *
	 * @see #getSiteMatrix(boolean forceNew) here for more Informations
	 * @throws WikimediaApiException
	 */
    public SiteMatrix getSiteMatrix() throws WikimediaApiException {
        return siteMatrix == null ? siteMatrix = executeApi(SiteMatrix.class, "sitematrix") : siteMatrix;
    }

    /**
	 * @param name
	 *            - your username
	 * @param passwd
	 *            - your password
	 * @return a LoginQuery-object, from where you can get more informations of
	 *         a login
	 * @throws WikimediaApiException
	 */
    public LoginQuery login(String name, String passwd) throws WikimediaApiException {
        return login(name, passwd, true);
    }

    /**
	 * @param name
	 *            - your username
	 * @param passwd
	 *            - your password
	 * @return a LoginQuery-object, from where you can get more informations of
	 *         a login
	 * @throws WikimediaApiException
	 *             if throwErrorOnFail is true and the login failed. Or if
	 *             occures some other error in the query
	 */
    public LoginQuery login(String name, String passwd, boolean throwErrorOnFail) throws WikimediaApiException {
        LoginQuery login = executeApi(LoginQuery.class, "login", null, new BasicNameValuePair[] { new BasicNameValuePair("lgname", name), new BasicNameValuePair("lgpassword", passwd) });
        if (throwErrorOnFail && !login.isSuccessfull()) throw new WikimediaApiLoginException(login.getResult());
        tokens = null;
        return login;
    }

    public WikiTokens getTokens() throws WikimediaApiException {
        if (tokens == null) {
            cacheTokens("edit");
        }
        return tokens;
    }

    /**
	 * logout from the api. The Session-Token will truncated and all cookies
	 * gets deleted
	 *
	 * @throws WikimediaApiException
	 */
    public void logout() throws WikimediaApiException {
        executeApi(null, "logout");
    }

    /**
	 * This method simply combines all given strings with a pipe an returns the
	 * new String
	 *
	 * @param strings
	 *            a list or an array of strings
	 * @return a String, which is used on the api to list more values in a
	 *         parameter. (e.g. "bar|moep|foo")
	 */
    public static String buildStringList(Object... strings) {
        String ret = "";
        for (Object o : strings) {
            ret += (!ret.isEmpty() ? "|" : "") + o.toString();
        }
        return ret;
    }

    public static String buildStringList(String... strings) {
        return buildStringList((Object[]) strings);
    }

    public static String buildStringList(Integer... strings) {
        return buildStringList((Object[]) strings);
    }

    /**
	 * @return a SiteInfo object, where you can get some information and
	 *         statistics of this wikimedia-page
	 * @throws WikimediaApiException
	 */
    public SiteInfo getSiteInfo() throws WikimediaApiException {
        return getSiteInfo(defaultSiteInfos);
    }

    /**
	 * @param infos
	 *            - an array of strings, which specify the types of infos, you
	 *            want.<br/> possible values: general, namespaces,
	 *            namespacealiases, specialpagealiases, interwikimap, dbrepllag,
	 *            statistics, usergroups
	 * @return a SiteInfo-Object
	 * @throws WikimediaApiException
	 */
    public SiteInfo getSiteInfo(String... infos) throws WikimediaApiException {
        return getSiteInfo(infos, true, null);
    }

    public void cacheTokens(String... tokens) throws WikimediaApiException {
        this.tokens = executePropQuery(WikiTokens.class, new getTokens("edit"), "Main Page");
    }

    public SiteInfo getSiteInfo(String[] infos, boolean showalldbs, Boolean localesonly) throws WikimediaApiException {
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("siprop", buildStringList((Object[]) infos)));
        if (localesonly != null) list.add(new BasicNameValuePair("sifilteriw", localesonly ? "locale" : "!locale"));
        if (showalldbs) {
            list.add(new BasicNameValuePair("sishowalldb", ""));
        }
        return executeMetaQuery(SiteInfo.class, "siteinfo", list);
    }

    public <T extends WikiList<?>> T executeListQuery(Class<T> returnClass, ListingQuery... querys) throws WikimediaApiException {
        return executeListQuery(returnClass, null, querys);
    }

    public <T extends WikiList<?>> T executeListQuery(Class<T> returnClass, ArrayList<NameValuePair> pars, ListingQuery... querys) throws WikimediaApiException {
        String listparams = "";
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        for (ListingQuery q : querys) {
            listparams = buildStringList(listparams, q.getType());
            params.addAll(q.getQualifiedParams(false));
        }
        params.add(new BasicNameValuePair("list", listparams));
        if (pars != null) params.addAll(pars);
        T retObject = executeApi(returnClass, "query", params);
        retObject.setCallBackOptions(this, returnClass, pars, querys);
        return retObject;
    }

    public <T extends WikiList<?>> T executePropQuery(Class<T> returnClass, PropQuery query) throws WikimediaApiException {
        return executePropQuery(returnClass, query, "$1");
    }

    public <T extends WikiList<?>> T executePropQuery(Class<T> returnClass, PropQuery query, Integer... siteIDs) throws WikimediaApiException {
        return executePropQuery(returnClass, new PropQuery[] { query }, siteIDs);
    }

    public <T extends WikiList<?>> T executePropQuery(Class<T> returnClass, PropQuery query, String... sites) throws WikimediaApiException {
        return executePropQuery(returnClass, new PropQuery[] { query }, sites);
    }

    public <T extends WikiList<?>> T executePropQuery(Class<T> returnClass, PropQuery[] querys, Integer... siteIDs) throws WikimediaApiException {
        String pageids = buildStringList(siteIDs);
        ArrayList<NameValuePair> cbParams = new ArrayList<NameValuePair>();
        cbParams.add(new BasicNameValuePair("pageids", pageids));
        return executePropQuery(returnClass, querys, cbParams);
    }

    public <T extends WikiList<?>> T executePropQuery(Class<T> returnClass, PropQuery[] querys, String... sites) throws WikimediaApiException {
        String pageids = buildStringList(sites);
        ArrayList<NameValuePair> cbParams = new ArrayList<NameValuePair>();
        cbParams.add(new BasicNameValuePair("titles", pageids));
        return executePropQuery(returnClass, querys, cbParams);
    }

    public <T extends WikiList<?>> T executePropQuery(Class<T> returnClass, PropQuery[] querys, ArrayList<NameValuePair> listingParams) throws WikimediaApiException {
        String propparams = "";
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        for (PropQuery q : querys) {
            propparams = buildStringList(propparams, q.getType());
            params.addAll(q.getQualifiedParams());
        }
        params.addAll(listingParams);
        params.add(new BasicNameValuePair("prop", propparams));
        T retObject = executeApi(returnClass, "query", params);
        return retObject;
    }

    public <T extends APIObject> T executeMetaQuery(Class<T> returnclass, String metaType, NameValuePair... params) throws WikimediaApiException {
        return executeMetaQuery(returnclass, metaType, new ArrayList<NameValuePair>(Arrays.asList(params)));
    }

    public <T extends APIObject> T executeMetaQuery(Class<T> returnclass, String metaType, List<NameValuePair> params) throws WikimediaApiException {
        params.add(new BasicNameValuePair("meta", metaType));
        return executeApi(returnclass, "query", params);
    }

    private <T extends APIObject> T executeApi(Class<T> returnclass, String action) throws WikimediaApiException {
        return executeApi(returnclass, new BasicNameValuePair("action", action));
    }

    private <T extends APIObject> T executeApi(Class<T> returnclass, NameValuePair... getParams) throws WikimediaApiException {
        return executeApi(returnclass, Arrays.asList(getParams), null);
    }

    private <T extends APIObject> T executeApi(Class<T> returnclass, String action, List<NameValuePair> getParams) throws WikimediaApiException {
        getParams.add(new BasicNameValuePair("action", action));
        return executeApi(returnclass, getParams, null);
    }

    private <T extends APIObject> T executeApi(Class<T> returnclass, String action, NameValuePair[] getParams, NameValuePair[] postParams) throws WikimediaApiException {
        List<NameValuePair> getList = (getParams == null ? new ArrayList<NameValuePair>() : Arrays.asList(getParams));
        getList.add(new BasicNameValuePair("action", action));
        return executeApi(returnclass, getList, Arrays.asList(postParams));
    }

    private <T extends APIObject> T executeApi(Class<T> returnclass, List<NameValuePair> getParams, List<NameValuePair> postParams) throws WikimediaApiException {
        ArrayList<NameValuePair> gets = new ArrayList<NameValuePair>(defaultparams);
        if (getParams != null) gets.addAll(getParams);
        gets.add(new BasicNameValuePair("requestid", "" + requestCounter++));
        String getParamString = URLEncodedUtils.format(gets, "utf-8");
        HttpUriRequest req = postParams == null ? new HttpGet(apiurl + "?" + getParamString) : new HttpPost(apiurl + "?" + getParamString);
        if (postParams != null) try {
            ((HttpPost) req).setEntity(new UrlEncodedFormEntity(postParams));
        } catch (UnsupportedEncodingException e) {
            throw new WikimediaApiException("IOError", e);
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(execute(req).getEntity().getContent()));
            String line = in.readLine();
            T obj = returnclass.getConstructor(String.class).newInstance(line);
            if (obj.has("error")) {
                JSONObject err = (JSONObject) obj.get("error");
                throw new WikimediaApiException(err.get("code") + ": " + err.get("info"));
            }
            return obj;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }
}
