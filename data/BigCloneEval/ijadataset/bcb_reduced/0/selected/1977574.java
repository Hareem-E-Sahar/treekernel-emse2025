package de.nomule.mediaproviders;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.nomule.applogic.MediaProvider;
import de.nomule.applogic.NoMuleRuntime;
import de.nomule.applogic.SearchResult;
import de.nomule.applogic.Settings;
import de.nomule.common.HTTP;

public class MySpaceMediaProvider extends MediaProvider {

    @Override
    public boolean MatchURLPattern(String strLink) {
        return strLink.matches("http\\:\\/\\/vids.myspace.com\\/.+");
    }

    @Override
    public String getMediaURL(String strLink) {
        try {
            String res = de.nomule.mediaproviders.KeepVid.getAnswer(strLink, "aa");
            if (NoMuleRuntime.DEBUG) System.out.println(res);
            String regexp = "http\\:\\/\\/[^\"]+\\.flv";
            Pattern p = Pattern.compile(regexp);
            Matcher m = p.matcher(res);
            m.find();
            String strRetUrl = res.substring(m.start(), m.end());
            if (NoMuleRuntime.DEBUG) System.out.println(strRetUrl);
            return strRetUrl;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getName() {
        return "MySpace";
    }

    @Override
    public String[] getProvidedContent() {
        String[] ret = { "Music" };
        return ret;
    }

    public boolean providesSearch() {
        return true;
    }

    public LinkedList<SearchResult> search(String strRequest) {
        LinkedList<SearchResult> ret = new LinkedList<SearchResult>();
        try {
            String strSearchUrl = "http://vids.myspace.com/index.cfm?fuseaction=vids.search&SearchBoxID=SplashHeader&q=" + URLEncoder.encode(strRequest, "UTF-8") + "&t=vid";
            String res = HTTP.get(strSearchUrl);
            if (NoMuleRuntime.DEBUG) System.out.println(res);
            String regexp = "<a href=\"\\/index.cfm\\?fuseaction\\=vids\\.individual\\&videoid\\=[0-9]+\">[^<]+<\\/a>";
            Pattern p = Pattern.compile(regexp);
            Matcher m = p.matcher(res);
            String result = "";
            while (m.find()) {
                result = res.substring(m.start(), m.end());
                if (NoMuleRuntime.DEBUG) System.out.println("Searchresult:" + result);
                String urlRegExp = "\\/index.cfm\\?fuseaction\\=vids\\.individual\\&videoid\\=[0-9]+";
                String titleRegExp = "\\>[^\\<]+\\<";
                Pattern pUrl = Pattern.compile(urlRegExp);
                Pattern pTitle = Pattern.compile(titleRegExp);
                Matcher mUrl = pUrl.matcher(result);
                Matcher mTitle = pTitle.matcher(result);
                if (mUrl.find() && mTitle.find()) {
                    String strUrl = "http://vids.myspace.com" + result.substring(mUrl.start(), mUrl.end());
                    String strTitle = result.substring(mTitle.start() + 1, mTitle.end() - 1) + " at MySpace";
                    SearchResult s = new SearchResult(strTitle, strUrl);
                    ret.add(s);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public String getTestUrl() {
        return "http://vids.myspace.com/index.cfm?fuseaction=vids.individual&videoid=19607454";
    }
}
