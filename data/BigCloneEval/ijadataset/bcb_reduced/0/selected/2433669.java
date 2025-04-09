package de.nomule.mediaproviders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import de.nomule.applogic.MediaProvider;
import de.nomule.applogic.NoMuleRuntime;
import de.nomule.applogic.SearchResult;
import de.nomule.applogic.Settings;

public class YouPornMediaProvider extends MediaProvider {

    public boolean MatchURLPattern(String strLink) {
        return strLink.matches("http:\\/\\/www\\.youporn\\.com\\/watch\\/.*");
    }

    public String getMediaURL(String strLink) {
        if (NoMuleRuntime.DEBUG) System.out.println(strLink);
        GetMethod get = new GetMethod(strLink);
        HttpClient h = new HttpClient();
        try {
            h.executeMethod(get);
            get.releaseConnection();
            get = new GetMethod(strLink + "?user_choice=Enter");
            String[] sPaths = strLink.split("\\/watch\\/[0-9]+\\/{0,1}");
            String strPath = strLink.substring(sPaths[0].length());
            if (sPaths.length > 1) strPath = strPath.substring(0, strPath.length() - sPaths[1].length());
            System.out.println(strPath);
            Date d = new Date((new Date()).getTime() + (1 * 24 * 3600 * 1000));
            h.getState().addCookie(new Cookie(".youporn.com", "age_check", "1", strPath, d, false));
            h.executeMethod(get);
            BufferedReader in = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
            String s = "";
            String res = "";
            while ((s = in.readLine()) != null) {
                res += s;
            }
            get.releaseConnection();
            String regexp = "http:\\/\\/download\\.youporn\\.com\\/[^\"]+\\.flv";
            Pattern p = Pattern.compile(regexp);
            Matcher m = p.matcher(res);
            m.find();
            return res.substring(m.start(), m.end());
        } catch (HttpException e) {
            JOptionPane.showMessageDialog(null, "Internal HTTP Error", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Internal HTTP Error");
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Internal I/O Error", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Internal I/O Error.");
            e.printStackTrace();
        }
        return "";
    }

    public String getName() {
        return "YouPorn";
    }

    public boolean providesSearch() {
        return true;
    }

    public LinkedList<SearchResult> search(String strRequest) {
        LinkedList<SearchResult> ret = new LinkedList<SearchResult>();
        HttpClient h = new HttpClient();
        try {
            String strRequestUrl = "http://www.youporn.com/search";
            if (strRequest.toLowerCase().contains("straight!")) {
                strRequestUrl += "?type=straight";
                strRequest = strRequest.replaceAll("straight!", "");
            }
            if (strRequest.toLowerCase().contains("gay!")) {
                strRequestUrl += "?type=gay";
                strRequest = strRequest.replaceAll("gay!", "");
            }
            if (strRequest.toLowerCase().contains("cocks!")) {
                strRequestUrl += "?type=cocks";
                strRequest = strRequest.replaceAll("cocks!", "");
            }
            if (!strRequestUrl.endsWith("search")) strRequestUrl += "&"; else strRequestUrl += "?";
            strRequestUrl += "query=" + URLEncoder.encode(strRequest, "UTF-8");
            if (NoMuleRuntime.DEBUG) System.out.println(strRequestUrl);
            GetMethod get = new GetMethod(strRequestUrl);
            Date d = new Date((new Date()).getTime() + (1 * 24 * 3600 * 1000));
            h.getState().addCookie(new Cookie(".youporn.com", "age_check", "1", "/", d, false));
            h.executeMethod(get);
            BufferedReader in = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
            String s = "";
            String res = "";
            while ((s = in.readLine()) != null) {
                res += s;
            }
            get.releaseConnection();
            if (NoMuleRuntime.DEBUG) System.out.println(res);
            String regexp = "\\<a href\\=\"\\/watch\\/[^\"]+\"\\>[^\\<]+";
            Pattern p = Pattern.compile(regexp);
            Matcher m = p.matcher(res);
            while (m.find()) {
                int startPos = m.start() + "<a href=\"".length();
                String strUrl = "http://www.youporn.com";
                int pos = 0;
                for (pos = startPos; pos < m.end() && (res.charAt(pos) != '\"'); pos++) {
                    strUrl += res.charAt(pos);
                }
                String strTitle = res.substring(pos + 2, m.end());
                if (strTitle.trim().length() > 0) ret.add(new SearchResult(strTitle + " at YouPorn", strUrl));
            }
            return ret;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getProvidedContent() {
        String[] s = { "Porn" };
        return s;
    }

    @Override
    public String getTestUrl() {
        return "http://www.youporn.com/watch/172690";
    }
}
