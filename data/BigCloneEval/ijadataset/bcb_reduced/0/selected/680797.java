package org.retro.gis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This object is designed to get information through the normal search
 * engines or wikis, currently supported for en.wiki and google.com
 * 
 * @author bigbinc
 * 
 * based on Googler.java(javabot) : copyright : Ricky Clarkson / Justin Lee
 */
public class URLConnectLib {

    private Socket _socket = null;

    private String _host = null;

    private int _port = 80;

    private String _query = null;

    private String _refer = null;

    private String _passResults = null;

    private String _finalResults = null;

    private boolean redirectPassedFlag = false;

    private List _listTags = new ArrayList();

    URLConnectLib(String _h, int p) {
        _host = _h;
        _port = p;
    }

    /**
     * Make the HTTP socket connection with the host
     */
    public void makeConnection() {
        try {
            _socket = new Socket(_host, _port);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public List getList() {
        return _listTags;
    }

    public void setQuery(String q, String refer) {
        _query = q;
        _refer = refer;
    }

    /**
     * Find all instances of strong or other html tags and replace with the text.
     * 
     * This method uses regex grouping.
     * 
     * @param mv
     * @return
     */
    protected static String replaceTagAnchor(String mv, List l, String _type) {
        String page = mv;
        String nonGrp = null;
        String grping = null;
        if (_type.equalsIgnoreCase("with-title")) {
            nonGrp = "<a.*?>.*?</a>";
            grping = "(<a.*?>(.*?)</a>)";
        } else {
            nonGrp = "<a.*?href=.*?>.*?</a>";
            grping = "(<a.*?href=.*?>(.*?)</a>)";
        }
        Pattern p = Pattern.compile(grping);
        Matcher m = p.matcher(page);
        while (m.find()) {
            int n = m.groupCount();
            String val = null;
            String full = null;
            if (n >= 2) {
                full = m.group(1);
                val = m.group(2);
            }
            if (val != null) {
                l.add(val.trim());
                page = page.replaceAll(full, val);
            }
        }
        return page;
    }

    protected static String getFirstTag(String tg, String mv) {
        int s = mv.indexOf("<" + tg + ">");
        int e = mv.indexOf("</" + tg + ">");
        return mv.substring(s + tg.length() + 2, e);
    }

    protected static String getFirstTagRegex(String tg, String mv) {
        String page = mv;
        Pattern p = Pattern.compile("(<" + tg + ">(.*?)</" + tg + ">)", Pattern.DOTALL);
        Matcher m = p.matcher(page);
        while (m.find()) {
            int n = m.groupCount();
            String val = null;
            String full = null;
            if (n >= 2) {
                full = m.group(1);
                val = m.group(2);
            }
            if (val != null) return val;
        }
        return null;
    }

    protected static String replaceTags(String tg, String mv, List l) {
        String page = mv;
        String nonGrp = "<" + tg + ">.*?</" + tg + ">";
        Pattern p = Pattern.compile("(<" + tg + ">(.*?)</" + tg + ">)");
        Matcher m = p.matcher(page);
        while (m.find()) {
            int n = m.groupCount();
            String val = null;
            String full = null;
            if (n >= 2) {
                full = m.group(1);
                val = m.group(2);
            }
            if (val != null) {
                l.add(val.trim());
                page = page.replaceAll(full, val);
            }
        }
        return page;
    }

    private void extractContentInformation(String _passResults) {
        Pattern p = Pattern.compile("start content", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(_passResults);
        boolean b = false;
        int _end = -1;
        while (m.find()) {
            _end = m.end();
            b = true;
        }
        if (_end > 0) {
            String major = _passResults.substring(_end, _passResults.length());
            major = major.replaceAll("\n", "");
            major = major.replaceAll("\t", "");
            major = major.replaceAll("\r\n", "");
            major = major.replaceAll("\f", "");
            major = replaceTags("strong", major, _listTags);
            major = replaceTags("em", major, _listTags);
            major = replaceTags("i", major, _listTags);
            major = replaceTags("b", major, _listTags);
            major = replaceTagAnchor(major, _listTags, "none");
            major = replaceTagAnchor(major, _listTags, "with-title");
            String data = getFirstTagRegex("p", major);
            _passResults = data.trim();
            _passResults = _passResults.replaceAll("\"", "'").trim();
            _finalResults = _passResults;
        }
    }

    public String getResults() {
        return _finalResults;
    }

    private void extractLocation(String _loc, int _end) {
        String get_u = _loc.substring(_end, _loc.length()).trim();
        if (get_u.length() > 0) {
            System.out.print(".");
            System.out.flush();
            Pattern _p = Pattern.compile("^http://", Pattern.CASE_INSENSITIVE);
            Matcher _m = _p.matcher(get_u);
            boolean _b = false;
            int _iend = -1;
            while (_m.find()) {
                _b = true;
                _iend = _m.end();
            }
            if (_iend > 0) {
                String pars02 = get_u.substring(_iend, get_u.length());
                _p = Pattern.compile("/");
                _m = _p.matcher(pars02.trim());
                String[] tok = _p.split(pars02.trim());
                String hst = tok[0].trim();
                int _s = -1;
                while (_m.find()) {
                    _s = _m.start();
                    if (_s > 0) break;
                }
                String rest = pars02.substring(_s, pars02.length()).trim();
                _host = hst;
                StringBuffer _buf = new StringBuffer();
                _buf.append("GET " + rest);
                _buf.append(" HTTP/1.1\r\n");
                _buf.append("Accept: text/html\r\n");
                _buf.append("Referer: " + _refer + "\r\n");
                _buf.append("Accept-Language: en-us\r\n");
                _buf.append("User-Agent: Mozilla/4.0 (compatible; ");
                _buf.append("MSIE 6.0; Windows NT 5.1; ");
                _buf.append("Avant Browser [avantbrowser.com]; ");
                _buf.append(".NET CLR 1.1.4322)\r\n");
                _buf.append("Host: " + _host + "\r\n" + "Connection: close\r\n\r\n");
                String httpPost = _buf.toString();
                System.out.print(".");
                System.out.flush();
                try {
                    Thread.sleep(100);
                    closeConnection();
                    _socket = new Socket(_host, _port);
                    if (_socket == null) throw new RuntimeException("Invalid Host Connection"); else System.out.print("..");
                    _socket.setSoTimeout(2 * 60 * 1000);
                    PrintWriter writer = new PrintWriter(_socket.getOutputStream(), true);
                    writer.print(httpPost);
                    writer.flush();
                    StringBuffer resultBuffer = new StringBuffer();
                    String line = null;
                    BufferedReader bufferedReader = null;
                    bufferedReader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
                    do {
                        try {
                            line = bufferedReader.readLine();
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                        if (line != null) resultBuffer.append(line + "\r\n");
                    } while (line != null);
                    try {
                        _socket.close();
                        _socket = null;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    String result = resultBuffer.toString();
                    _passResults = result.trim();
                    redirectPassedFlag = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw new RuntimeException("Invalid URL");
            }
        }
    }

    private void checkRedirectMessage(String v) {
        Pattern p = Pattern.compile("\\r\\n");
        Matcher m = p.matcher(v);
        String[] _lines = p.split(v);
        int err_state = 0;
        for (int i = 0; i < _lines.length; i++) {
            String _process = _lines[i].trim();
            if (_process != null) {
                if (_process.length() > 0) {
                    String _reg = "HTTP/\\d";
                    String _regLoc = "Location\\W";
                    Pattern in_p = Pattern.compile(_reg, Pattern.CASE_INSENSITIVE);
                    Matcher in_m = in_p.matcher(_process);
                    boolean _b = false;
                    int last_end = -1;
                    while (in_m.find()) {
                        _b = true;
                    }
                    if (_b) {
                        err_state++;
                        Pattern z = Pattern.compile("30\\d");
                        Matcher n = z.matcher(_process);
                        boolean x = false;
                        while (n.find()) {
                            last_end = n.end();
                            x = true;
                        }
                        if (x) {
                            err_state++;
                        }
                    }
                    if (err_state >= 2) {
                        in_p = Pattern.compile(_regLoc, Pattern.CASE_INSENSITIVE);
                        in_m = in_p.matcher(_process);
                        _b = false;
                        while (in_m.find()) {
                            _b = true;
                            last_end = in_m.end();
                        }
                        if (_b) {
                            extractLocation(_process.trim(), last_end);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void performSearch() throws IOException {
        if (_socket == null) throw new IOException("Socket connection is invalid");
        _socket.setSoTimeout(3 * 60 * 1000);
        StringBuffer _buf = new StringBuffer();
        _buf.append("GET /wiki/Special:Search?search=");
        _buf.append(URLEncoder.encode(_query, "UTF-8") + "&go=Go ");
        _buf.append(" HTTP/1.1\r\n");
        _buf.append("Accept: text/html\r\n");
        _buf.append("Referer: " + _refer + "\r\n");
        _buf.append("Accept-Language: en-us\r\n");
        _buf.append("User-Agent: Mozilla/4.0 (compatible; ");
        _buf.append("MSIE 6.0; Windows NT 5.1; ");
        _buf.append("Avant Browser [avantbrowser.com]; ");
        _buf.append(".NET CLR 1.1.4322)\r\n");
        _buf.append("Host: " + _host + "\r\n" + "Connection: close\r\n\r\n");
        String httpPost = _buf.toString();
        System.out.print(".");
        try {
            PrintWriter writer = new PrintWriter(_socket.getOutputStream(), true);
            writer.print(httpPost);
            writer.flush();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        StringBuffer resultBuffer = new StringBuffer();
        String line = null;
        do {
            try {
                line = bufferedReader.readLine();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            if (line != null) resultBuffer.append(line + "\r\n");
        } while (line != null);
        try {
            _socket.close();
            _socket = null;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        String result = resultBuffer.toString();
        _passResults = result.trim();
        checkRedirectMessage(_passResults);
        if (redirectPassedFlag) {
            extractContentInformation(_passResults);
        } else {
            System.out.println(" ERR : Redirect Extract Failed : ");
        }
    }

    public void closeConnection() {
        if (_socket != null) {
            try {
                _socket.close();
                _socket = null;
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    public void finalize() throws Throwable {
        closeConnection();
    }
}
