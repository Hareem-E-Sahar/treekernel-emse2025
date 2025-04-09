import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HttpMessage;

public class Util {

    public static String getFileData(String file) {
        int count = 0;
        byte[] bytes = new byte[1024];
        StringBuffer buffer = new StringBuffer();
        try {
            if (new java.io.File(file).exists()) {
                java.io.FileInputStream in = new java.io.FileInputStream(file);
                while ((count = in.read(bytes)) > 0) {
                    buffer.append(new String(bytes, 0, count));
                }
                in.close();
            }
        } catch (Exception me) {
            me.printStackTrace();
        }
        return buffer.toString();
    }

    public static Hashtable getHashdata(String file) {
        Hashtable hash = new Hashtable();
        String data = Util.getFileData(file);
        StringTokenizer tokens = new StringTokenizer(data, "\r\n");
        while (tokens.hasMoreTokens()) {
            String token = (String) tokens.nextToken();
            String key = token.substring(0, token.indexOf("="));
            String value = "";
            try {
                value = token.substring(token.indexOf("=") + 1);
            } catch (Exception me) {
            }
            hash.put(key, value);
        }
        return hash;
    }

    public static String getRequestParasAsString(Hashtable hash, boolean encoding) {
        StringBuffer sb = new StringBuffer();
        Enumeration keys = hash.keys();
        boolean first = true;
        while (keys.hasMoreElements()) {
            if (!first) sb.append('&');
            first = false;
            String key = (String) keys.nextElement();
            String value = (String) hash.get(key);
            try {
                if (encoding) {
                    key = URLEncoder.encode(key, "UTF-8");
                    value = URLEncoder.encode(value, "UTF-8");
                }
                sb.append(key + "=" + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String getCookieAsString(Hashtable hash) {
        StringBuffer sb = new StringBuffer();
        Enumeration keys = hash.keys();
        boolean first = true;
        while (keys.hasMoreElements()) {
            if (!first) sb.append("; ");
            first = false;
            String key = (String) keys.nextElement();
            String value = (String) hash.get(key);
            try {
                sb.append(key + "=" + value);
            } catch (IllegalArgumentException e) {
            }
        }
        return sb.toString();
    }

    public static void saveHttpMessage(HttpMessage msg, String outputfolder, String file) {
        String responseHeaderFile = outputfolder + file + "_response.txt";
        String responseBodyFile = outputfolder + file + "_response.html";
        String requestHeaderFile = outputfolder + file + "_request.txt";
        String requestPostFile = outputfolder + file + "_request_post.txt";
        String responseHeader = msg.getResponseHeader().toString();
        createFile(responseHeaderFile, responseHeader.getBytes());
        String responseBody = msg.getResponseBody().toString();
        if (responseBody != null && responseBody.length() > 0) {
            createFile(responseBodyFile, responseBody.getBytes());
        }
        String requestHeader = msg.getRequestHeader().toString();
        createFile(requestHeaderFile, requestHeader.getBytes());
        if (msg.getRequestHeader().getMethod().equalsIgnoreCase("post")) {
            Pattern pSeparator = Pattern.compile("([^=&]+)[=]([^=&]*)");
            String name = null;
            String value = null;
            StringBuffer buffer = new StringBuffer();
            Matcher matcher = pSeparator.matcher(msg.getRequestBody().toString());
            while (matcher.find()) {
                try {
                    name = URLDecoder.decode(matcher.group(1), "8859_1");
                    value = URLDecoder.decode(matcher.group(2), "8859_1");
                    buffer.append(name).append("=").append(value).append("\r\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            createFile(requestPostFile, buffer.toString().getBytes());
        }
    }

    public static void createFile(String file, byte[] bytes) {
        try {
            if (bytes == null || bytes.length == 0) {
                return;
            }
            java.io.FileOutputStream out = new java.io.FileOutputStream(file);
            out.write(bytes);
            out.flush();
            out.close();
        } catch (Exception me) {
            me.printStackTrace();
        }
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static List getListdata(String file) {
        Vector list = new Vector();
        String data = Util.getFileData(file);
        StringTokenizer tokens = new StringTokenizer(data, "\r\n");
        while (tokens.hasMoreTokens()) {
            list.add(tokens.nextToken());
        }
        return list;
    }
}
