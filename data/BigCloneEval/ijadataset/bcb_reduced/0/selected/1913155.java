package com.coyousoft.wangyu.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sysolar.util.file.FileIO;
import com.coyousoft.wangyu.entity.CoreUrl;
import com.coyousoft.wangyu.entity.Domain;
import com.coyousoft.wangyu.entity.UserUrl;
import com.coyousoft.wangyu.entity.UserUrlCat;
import com.coyousoft.wangyu.util.FileUtil;
import com.coyousoft.wangyu.util.StringUtil;
import com.coyousoft.wangyu.util.UrlUtil;

public final class UserUrlHelper {

    private static final Log log = LogFactory.getLog(UserUrlHelper.class);

    public static final Pattern pUnvalidUrl;

    public static final Pattern pLocalProtocol;

    public static final Pattern pNetProtocol;

    public static final Pattern pSite;

    public static final Pattern pIpDomain;

    public static final Pattern pNo3wTopDomain;

    public static final Pattern pNo3wCnTopDomain;

    public static final Pattern pTopDomain;

    public static final Pattern pCnTopDomain;

    static {
        pUnvalidUrl = Pattern.compile("((javascript)|(place)):.*", Pattern.CASE_INSENSITIVE);
        pLocalProtocol = Pattern.compile("((file:///)|([a-z]:\\\\)|(\\\\)).*", Pattern.CASE_INSENSITIVE);
        pNetProtocol = Pattern.compile("(\\w+):/+([\\u4e00-\\u9fa5\\w+\\-\\.]+)([:/]?.*)", Pattern.CASE_INSENSITIVE);
        pIpDomain = Pattern.compile("[\\d\\.]+");
        pSite = Pattern.compile("(\\w+)://[\\u4e00-\\u9fa5\\w+\\-\\.]+", Pattern.CASE_INSENSITIVE);
        pNo3wTopDomain = Pattern.compile("[\\u4e00-\\u9fa5\\w+\\-]+\\.[a-z]+");
        String s = "([\\u4e00-\\u9fa5\\w+\\-]+)\\.((com)|(net)|(org)|(gov))\\.cn\\z";
        pNo3wCnTopDomain = Pattern.compile(s);
        s = "([\\u4e00-\\u9fa5\\w+\\-]+)\\.((com)|(net)|(org)|(gov))\\.((cn)|(hk)|(tw))\\z";
        pCnTopDomain = Pattern.compile(s);
        pTopDomain = Pattern.compile("\\.?([\\w+\\-]+\\.[a-z]+)\\z");
    }

    public static UserUrl check(UserUrl userUrl) {
        return check(userUrl, true, true);
    }

    /**
     * 校验、初始化用户网址(UserUrl)对象。当网址无效时，返回 null 。
     *      1. 域名转换为小写、域名后面的path路径保持大小写不变；
     *      2. 删除链接最后的尾巴斜线 /
     *      3. 本地域名，domainId 设为 1、coreUrlId 也设为1；
     *      4. 非HTTP、HTTPS的外网域名，domainId 设为 2，coreUrlId 走正常逻辑；
     *      5. 设定相关的 Domain 对象、顶级 Domain 对象、CoreUrl 对象；
     * @param userUrl 
     * @param isCoreUrlNeeded 是否需要创建 CoreUrl 对象
     * @param isDomainNeeded 是否需要创建 Domain 对象
     * @return 用户网址(UserUrl)对象，或者 null - 当网址无效时。
     */
    public static UserUrl check(UserUrl userUrl, boolean isCoreUrlNeeded, boolean isDomainNeeded) {
        String urlHref = userUrl.getUrlHref().trim();
        String urlName = userUrl.getUrlName();
        StringBuilder buffer = new StringBuilder(256);
        if (pUnvalidUrl.matcher(urlHref).matches() || urlHref.length() > 255) {
            log.error("无效链接：" + userUrl);
            return null;
        }
        urlHref = urlHref.replace('\\', '/');
        if (null != urlName) {
            urlName = StringUtil.dealJsChat(urlName);
            userUrl.setUrlName(StringUtil.asciiTrim(urlName, 60));
        }
        if (null != userUrl.getUrlMemo()) {
            userUrl.setUrlMemo(StringUtil.dealJsChat(userUrl.getUrlMemo()));
        }
        while (urlHref.charAt(urlHref.length() - 1) == '/') {
            urlHref = urlHref.substring(0, urlHref.length() - 1);
        }
        userUrl.setUrlHref(urlHref);
        if (pLocalProtocol.matcher(urlHref).matches()) {
            userUrl.setDomainId(Domain.NATIVE_DOMAIN_ID).setCoreUrlId(CoreUrl.NATIVE_CORE_URL_ID);
            return userUrl;
        }
        if (!pNetProtocol.matcher(urlHref).matches()) {
            urlHref = "http://" + urlHref;
        }
        Matcher m = pNetProtocol.matcher(urlHref);
        String protocol, domain, path;
        if (m.matches()) {
            protocol = m.group(1).toLowerCase();
            domain = m.group(2).toLowerCase();
            path = m.group(3);
        } else {
            log.error("无效网络协议: " + userUrl);
            return null;
        }
        if (!protocol.matches("http[s]?")) {
            userUrl.setDomainId(Domain.UNHTTP_DOMAIN_ID);
            userUrl.setCoreUrl(new CoreUrl().setCoreUrlHref(urlHref).setCoreUrlHrefType(CoreUrl.HREF_TYPE_SITE));
            return userUrl;
        }
        String d = domain;
        if (d.matches("[\\u4e00-\\u9fa5\\w\\-]+") || isInnerIP(domain)) {
            return userUrl.setDomainId(Domain.NATIVE_DOMAIN_ID).setCoreUrlId(CoreUrl.NATIVE_CORE_URL_ID);
        }
        path = path.replaceAll("/+", "/");
        urlHref = buffer.append(protocol).append("://").append(domain).append(path).toString();
        userUrl.setUrlHref(urlHref);
        if (!isCoreUrlNeeded) {
            return userUrl;
        }
        buffer.delete(0, buffer.length());
        buffer.append(protocol).append("://").append(add3w4topDomain(domain)).append(path);
        CoreUrl coreUrl = new CoreUrl().setCoreUrlHref(buffer.toString());
        if (pSite.matcher(coreUrl.getCoreUrlHref()).matches()) {
            coreUrl.setCoreUrlHrefType(CoreUrl.HREF_TYPE_SITE);
        } else {
            coreUrl.setCoreUrlHrefType(CoreUrl.HREF_TYPE_PAGE);
        }
        userUrl.setCoreUrl(coreUrl);
        if (!isDomainNeeded) {
            return userUrl;
        }
        String topDomain = null;
        if ((m = pCnTopDomain.matcher(domain)).find() && !m.group(1).equals("www")) {
            topDomain = m.group();
        } else if ((m = pTopDomain.matcher(domain)).find()) {
            topDomain = m.group(1);
        } else {
            topDomain = domain;
        }
        domain = add3w4topDomain(domain);
        topDomain = add3w4topDomain(topDomain);
        buffer.delete(0, buffer.length());
        buffer.append(protocol).append("://").append(domain);
        Domain domainEntity = new Domain().setDomainHref(buffer.toString());
        domainEntity.setDomainType(Domain.TYPE_HTTP_WWW);
        userUrl.setDomain(domainEntity);
        if (pIpDomain.matcher(domain).matches()) {
            domainEntity.setDomainRank(1);
            return userUrl;
        }
        String tempDomain = domain, tempTopDomain = topDomain;
        if (tempDomain.startsWith("www.")) {
            tempDomain = tempDomain.substring(4, tempDomain.length());
        }
        if (tempTopDomain.startsWith("www.")) {
            tempTopDomain = tempTopDomain.substring(4, tempTopDomain.length());
        }
        domainEntity.setDomainRank(StringUtil.count(tempDomain, '.') - StringUtil.count(tempTopDomain, '.') + 1);
        domainEntity.setDomainId(userUrl.getDomainId());
        buffer.delete(0, buffer.length());
        buffer.append(protocol).append("://").append(topDomain);
        Domain topDomainEntity;
        if (!domain.equals(topDomain)) {
            topDomainEntity = new Domain().setDomainHref(buffer.toString());
            topDomainEntity.setDomainType(Domain.TYPE_HTTP_WWW).setDomainRank(1);
            domainEntity.setTopDomain(topDomainEntity);
        }
        return userUrl;
    }

    /**
     * 为未加 www 的顶级域名前加 www。
     * 
     * @param domain
     * @return 
     */
    public static String add3w4topDomain(String domain) {
        Matcher m = pNo3wCnTopDomain.matcher(domain);
        if (pNo3wTopDomain.matcher(domain).matches() || (m.matches() && !m.group(1).equals("www"))) {
            domain = "www." + domain;
        }
        return domain;
    }

    /**
     * 从用户上传的本地收藏夹文件里解析出网址分类、每个分类下包含的网址。其中未分类的网址将被放在
     *  catName 为 null 的 UserUrlCat 对象里。
     * 
     * @param bookmarkFile 用户上传的本地收藏夹文件
     * @return UserUrlCat（用户网址分类）对象列表
     * @throws Exception
     */
    public static List<UserUrlCat> parseBookmark(File bookmarkFile) throws Exception {
        List<UserUrlCat> userUrlCatList = new ArrayList<UserUrlCat>(10);
        UserUrlCat userUrlCat, defaultUserUrlCat = new UserUrlCat();
        defaultUserUrlCat.setCatName("收藏夹");
        defaultUserUrlCat.setCatCreateType(UserUrlCat.CREATE_TYPE_USER);
        String src = FileIO.readAsString(bookmarkFile, FileUtil.getCharset(bookmarkFile));
        src = formatBookmark(src);
        List<Object[]> tempCatList = new ArrayList<Object[]>(10);
        Pattern p = Pattern.compile("<(h\\d)[^>]*>([^<]*)</\\1>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(src);
        while (m.find()) {
            tempCatList.add(new Object[] { m.group(2), m.start(), m.end() });
        }
        String noCatUrls = null;
        if (tempCatList.size() == 0) {
            noCatUrls = src;
        } else {
            noCatUrls = src.substring(0, (Integer) tempCatList.get(0)[1]);
        }
        if (null != noCatUrls && !noCatUrls.trim().equals("")) {
            parseUserUrl(noCatUrls, defaultUserUrlCat);
        }
        String catUrls = null, catName;
        for (int i = 0; i < tempCatList.size(); i++) {
            if (i == tempCatList.size() - 1) {
                catUrls = src.substring((Integer) tempCatList.get(i)[2]);
            } else {
                catUrls = src.substring((Integer) tempCatList.get(i)[2], (Integer) tempCatList.get(i + 1)[1]);
            }
            catName = tempCatList.get(i)[0].toString().trim();
            if (catName.equals("")) {
                parseUserUrl(catUrls, defaultUserUrlCat);
            } else {
                userUrlCat = new UserUrlCat().setCatName(catName);
                userUrlCat.setCatCreateType(UserUrlCat.CREATE_TYPE_USER);
                parseUserUrl(catUrls, userUrlCat);
                if (userUrlCat.getUserUrlList() != null && userUrlCat.getUserUrlList().size() > 0) {
                    userUrlCatList.add(userUrlCat);
                }
            }
        }
        if (null != defaultUserUrlCat.getUserUrlList()) {
            userUrlCatList.add(defaultUserUrlCat);
        }
        return userUrlCatList;
    }

    /**
     * 从字串里解析网址链接和标题。
     * 
     * @param src
     * @param userUrlList
     * @return
     */
    private static UserUrlCat parseUserUrl(String src, UserUrlCat userUrlCat) {
        Pattern p = Pattern.compile("<a (\\w+=\"[^\"]*\"\\s*)+>([^<]*)</a>", Pattern.CASE_INSENSITIVE);
        Pattern pHref = Pattern.compile("href=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(src);
        while (m.find()) {
            Matcher mHref = pHref.matcher(m.group());
            if (mHref.find()) {
                userUrlCat.add(new UserUrl().setUrlName(m.group(2)).setUrlHref(mHref.group(1)));
            }
        }
        return userUrlCat;
    }

    /**
     * 格式化用户上传的收藏夹字串，以方便使用正则表达式进行解析。
     * 
     * @param input
     * @return
     */
    private static String formatBookmark(String input) {
        Pattern p;
        Matcher m;
        StringBuilder buffer = new StringBuilder(1024);
        input = input.replaceAll("\\s+", " ");
        p = Pattern.compile("\\\\[\"\']{1}");
        m = p.matcher(input);
        int offset = 0;
        buffer.append(input);
        while (m.find()) {
            buffer.delete(m.start() - offset, m.end() - offset);
            offset += (m.end() - m.start());
        }
        input = buffer.toString();
        input = input.replace('\'', '"');
        return input.replaceAll("\\s*=\\s*", "=");
    }

    public static boolean isInnerIP(String domain) {
        if (!UrlUtil.isIp(domain)) {
            return false;
        }
        boolean isInnerIp = false;
        long ipNum = getIpNum(domain);
        long aBegin = getIpNum("10.0.0.0");
        long aEnd = getIpNum("10.255.255.255");
        long bBegin = getIpNum("172.16.0.0");
        long bEnd = getIpNum("172.31.255.255");
        long cBegin = getIpNum("192.168.0.0");
        long cEnd = getIpNum("192.168.255.255");
        isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || domain.equals("127.0.0.1");
        return isInnerIp;
    }

    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);
        long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
        return ipNum;
    }

    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    public static void main(String[] args) throws Exception {
        UserUrl userUrl = new UserUrl().setUrlHref("http://218.93.60.206/domain.html");
        check(userUrl, true, true);
    }
}
