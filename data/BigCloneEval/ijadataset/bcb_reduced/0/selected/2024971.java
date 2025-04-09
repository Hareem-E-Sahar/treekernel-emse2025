package cn;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import com.google.gdata.client.GoogleService;
import com.google.gdata.data.Entry;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.util.common.html.HtmlToText;

public class sta {

    public static FileWriter fw;

    public static String s_forum = "C:\\_2_Jigsaw\\wwwroot\\forum\\2\\";

    public static String s = "", s3 = "";

    public static int k = 1, j = 1, j_max = 5;

    public static String set_google_cookie(String s) {
        String sid = s.substring(s.indexOf("SID="));
        sid = sid.substring(0, sid.indexOf(";") + 1);
        String lsid = s.substring(s.lastIndexOf("LSID="));
        lsid = lsid.substring(0, lsid.indexOf(";") + 1);
        return sid + " " + lsid;
    }

    public static String get_ssl_header(String s) throws Exception {
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket("www.google.com", 443);
        PrintStream outStream = new PrintStream(sslsocket.getOutputStream());
        outStream.println(s);
        outStream.flush();
        DataInputStream inStream = new DataInputStream(sslsocket.getInputStream());
        StringBuffer stb = new StringBuffer();
        int ch = ' ';
        for (ch = inStream.read(); ch > 0; ch = inStream.read()) {
            stb.append((char) ch);
            if (stb.indexOf("\r\n\r\n") > -1) break;
        }
        inStream.close();
        outStream.close();
        sslsocket.close();
        return stb.toString();
    }

    public static String get_ssl_page(String s) throws Exception {
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket("www.google.com", 443);
        PrintStream outStream = new PrintStream(sslsocket.getOutputStream());
        outStream.println(s);
        outStream.flush();
        DataInputStream inStream = new DataInputStream(sslsocket.getInputStream());
        StringBuffer stb = new StringBuffer();
        int ch = ' ';
        for (ch = inStream.read(); ch > 0; ch = inStream.read()) {
            stb.append((char) ch);
            if (stb.indexOf("</html>") > -1 || stb.indexOf("</HTML>") > -1) break;
        }
        inStream.close();
        outStream.close();
        sslsocket.close();
        return stb.toString();
    }

    public static String get_blogger_ssl_page(String s) throws Exception {
        System.out.println(s);
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket("www.blogger.com", 443);
        PrintStream outStream = new PrintStream(sslsocket.getOutputStream());
        outStream.println(s);
        outStream.flush();
        DataInputStream inStream = new DataInputStream(sslsocket.getInputStream());
        StringBuffer stb = new StringBuffer();
        int ch = ' ';
        for (ch = inStream.read(); ch > 0; ch = inStream.read()) {
            stb.append((char) ch);
            if (stb.indexOf("</html>") > -1 || stb.indexOf("</HTML>") > -1) break;
        }
        inStream.close();
        outStream.close();
        sslsocket.close();
        return stb.toString();
    }

    public static String rep(String line, String old_s, String new_s) {
        int index = 0;
        while ((index = line.indexOf(old_s, index)) >= 0) {
            line = line.substring(0, index) + new_s + line.substring(index + old_s.length());
            index += new_s.length();
        }
        return line;
    }

    public static String rt(String s) {
        int i = s.indexOf("<");
        int j = s.indexOf(">");
        while (i >= 0 && j > i) {
            s = s.substring(0, i) + s.substring(j + 1);
            i = s.indexOf("<");
            j = s.indexOf(">");
        }
        return s;
    }

    public static byte[] hs2b(String hex) {
        java.util.Vector<Object> res = new java.util.Vector<Object>();
        String part;
        int pos = 0;
        final int len = 2;
        while (pos < hex.length()) {
            part = hex.substring(pos, pos + len);
            pos += len;
            int byteVal = Integer.parseInt(part, 16);
            res.add(new Byte((byte) byteVal));
        }
        if (res.size() > 0) {
            byte[] b = new byte[res.size()];
            for (int i = 0; i < res.size(); i++) {
                Byte a = (Byte) res.elementAt(i);
                b[i] = a.byteValue();
            }
            return b;
        } else {
            return null;
        }
    }

    public static String b2hs(byte[] buf) {
        StringBuffer sbuff = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            int b = buf[i];
            if (b < 0) b = b & 0xFF;
            if (b < 16) sbuff.append("0");
            sbuff.append(Integer.toHexString(b).toUpperCase());
        }
        return sbuff.toString();
    }

    public static void wtf(String str, String furl) {
        try {
            FileOutputStream fos = new FileOutputStream(furl);
            Writer out = new OutputStreamWriter(fos);
            out.write(str);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void wtft(String str, String furl) {
        try {
            FileOutputStream fos = new FileOutputStream(furl, true);
            Writer out = new OutputStreamWriter(fos);
            out.write(str + "\r\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void wtf_ru(String str, String furl, String enc) {
        try {
            FileOutputStream fos = new FileOutputStream(furl);
            Writer out = new OutputStreamWriter(fos, enc);
            out.write(str);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void wtf_rut(String str, String furl) {
        try {
            FileOutputStream fos = new FileOutputStream(furl, true);
            Writer out = new OutputStreamWriter(fos, "CP1251");
            out.write(str + "\r\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String rfu2(String url, String enc) throws Exception {
        String s = "", str = "";
        URL u = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream(), enc));
        while ((str = in.readLine()) != null) {
            s = s + str + "\r\n";
        }
        in.close();
        return s;
    }

    public static String rff(String s) throws Exception {
        return rfu2("file:///" + System.getProperties().getProperty("user.dir") + "/" + s, "UTF8");
    }

    public static String rffw(String s) throws Exception {
        return rfu("file:///" + System.getProperties().getProperty("user.dir") + "/" + s);
    }

    public static String rfu(String url) {
        StringBuffer s = new StringBuffer();
        try {
            URL u = new URL(url);
            InputStream in = u.openConnection().getInputStream();
            for (int ch = in.read(); ch > 0; ch = in.read()) {
                s.append((char) ch);
            }
            in.close();
        } catch (IOException e) {
            return e.toString();
        }
        return s.toString();
    }

    public static void tokenize(String s) {
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            System.out.println(st.nextToken());
        }
    }

    public static String rfu_win(String url) throws Exception {
        String s = "", str = "";
        try {
            URL u = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream(), "CP1251"));
            while ((str = in.readLine()) != null) {
                s = s + str;
            }
            in.close();
        } catch (IOException e) {
        }
        return s;
    }

    public static String rfu_utf(String url) throws Exception {
        String s = "", str = "";
        try {
            URL u = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream(), "UTF-8"));
            while ((str = in.readLine()) != null) {
                s = s + str;
            }
            in.close();
        } catch (IOException e) {
        }
        return s;
    }

    public static void wtf_utf(String str, String furl) {
        try {
            FileOutputStream fos = new FileOutputStream(furl);
            Writer out = new OutputStreamWriter(fos, "UTF-8");
            out.write(str);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCookie(String host, String page) throws Exception {
        String c = "GET " + page + "  HTTP/1.0\r\n" + "Host: " + host + "\r\n" + "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.8.0.3) Gecko/20060426 Firefox/1.5.0.3\r\n" + "Accept: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5\r\n" + "Accept-Language: en-us,en;q=0.5\r\n" + "Accept-Encoding: \r\n" + "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\r\n" + "Keep-Alive: 300\r\n" + "Connection: keep-alive\r\n" + "Content-Type: application/x-www-form-urlencoded\r\n\r\n";
        Socket js = new Socket(host, 80);
        PrintStream outStream = new PrintStream(js.getOutputStream());
        outStream.println(c);
        outStream.flush();
        DataInputStream inStream = new DataInputStream(js.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
        String sb = "", s = br.readLine();
        while ((s = br.readLine()) != null) {
            sb = sb + s + "\r\n";
            if (s.indexOf("Cookie:") > -1) c = s.substring(s.indexOf("Cookie:"));
            if (s.indexOf("</HTML>") > -1) break;
        }
        inStream.close();
        outStream.close();
        js.close();
        return c;
    }

    public static void list() {
        for (int i = 11; i < 23; i++) for (int j = 11; j < 44; j++) wtft("k" + i + "-k-" + j + " " + "k" + i, "list.txt");
        for (int i = 983; i < 999; i++) for (int j = 101; j < 134; j++) wtft("k" + i + "-" + j + " " + "k" + i, "list.txt");
    }

    public static void bin(String blog, String s) {
        try {
            URL postUrl = new URL("http://" + blog + ".blogspot.com/feeds/posts/default");
            Entry myEntry = new Entry();
            myEntry.setContent(new HtmlTextConstruct(s));
            GoogleService myService = new GoogleService("blogger", "");
            myService.setUserCredentials(blog.substring(0, 3) + "@quicklydone.com", "tverskoy");
            myService.insert(postUrl, myEntry);
            System.out.println(blog + " OK");
        } catch (Exception e) {
            System.err.println(HtmlToText.htmlToPlainText(e.toString()));
            try {
                Thread.sleep(11000);
            } catch (Exception ee) {
                System.out.println("sleeping 11 sec...");
            }
        }
    }

    public static String rsp(String s) {
        Pattern pattern = Pattern.compile("\\s+");
        Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll(" ");
    }

    public static void zi(String s, String z, int i) throws Exception {
        byte[] buffer = s.getBytes("UTF-8");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(z + ".zip"));
        out.putNextEntry(new ZipEntry(i + ".txt"));
        out.write(buffer);
        out.closeEntry();
        out.close();
        System.out.println(i + ".txt -> " + z + ".zip");
    }

    public static String gre(String ss) throws Exception {
        String host = ss.substring(ss.indexOf("//") + 2);
        host = host.substring(0, host.indexOf("/"));
        Socket js = new Socket(host, 80);
        PrintStream outStream = new PrintStream(js.getOutputStream());
        BufferedInputStream bis = new BufferedInputStream(js.getInputStream());
        outStream.println(ss);
        outStream.flush();
        int b;
        StringBuffer sb = new StringBuffer();
        try {
            while ((b = bis.read()) != -1) {
                sb.append((char) b);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        bis.close();
        outStream.close();
        js.close();
        return sb.toString();
    }

    public static String zir(String zips) throws Exception {
        String s = "", ss = "", sz = "";
        int n = 0;
        File f = new File(zips);
        for (int i = 0; i < f.list().length; i++) {
            s = f.list()[i];
            s = s.substring(s.indexOf("-") + 1, s.indexOf("."));
            n = n + Integer.parseInt(s);
        }
        int j = (int) (n * Math.random());
        n = 0;
        for (int i = 0; i < f.list().length; i++) {
            s = f.list()[i];
            sz = s;
            s = s.substring(s.indexOf("-") + 1, s.indexOf("."));
            n = n + Integer.parseInt(s);
            if (j < n) {
                break;
            }
        }
        s = "";
        int i = Integer.parseInt(sz.substring(sz.indexOf("-") + 1, sz.indexOf(".")));
        int k = (int) (i * Math.random()) + 1;
        ZipFile zipFile = new ZipFile(new File(zips + "/" + sz));
        ZipEntry entry = zipFile.getEntry(k + ".txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), "UTF-8"));
        while ((ss = br.readLine()) != null) {
            s = s + ss;
        }
        br.close();
        zipFile.close();
        return s;
    }
}
