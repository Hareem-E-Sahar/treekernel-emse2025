package core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import conf.GMailCfg;
import conf.Logger;

/**
 * this class is base class for connect to GMail,download Attachment from GMail
 * or upload Attachment to Gmail
 * 
 * @author Bob
 * @mail zhangbo.gm@gmail.com
 * @since 2007-04-29
 */
public class GMailConnect {

    private boolean isConnect = false;

    private String name = "gfiletest";

    private String pwd = "19811231";

    private HttpClient client = null;

    private HttpMethodBase lastHttpCon = null;

    private int statuCode = -1;

    private String base_href = "";

    private String at = "";

    private Session session = null;

    public GMailConnect() {
        HttpConnectionManager connManager = new MultiThreadedHttpConnectionManager();
        client = new HttpClient(connManager);
    }

    /**
	 * try to connect to GMail,so that can get base_href for the other thing to
	 * do
	 * 
	 * @return whether connect
	 */
    public boolean connect() {
        isConnect = false;
        if (step1() && step2() && step3()) isConnect = true;
        lastHttpCon.abort();
        return isConnect;
    }

    /**
	 * restun mails url with the subtitle,need connected result base_href
	 * 
	 * @param subject
	 * @return mails url,if have no result return null
	 */
    public String[] searchMailBySubject(String subject) {
        String[] result = null;
        String qs = subject;
        if (subject.length() > 23) qs = qs.substring(0, 23);
        if (!isConnect) {
            if (!connect()) return null;
        }
        String url = base_href + GMailCfg.QUERY_TOKEN + qs;
        GetMethod gm = new GetMethod(url);
        try {
            statuCode = client.executeMethod(gm);
        } catch (Exception ex) {
            Logger.log("serach mail fail,maybe search title have problem:");
            Logger.log("url-->" + url);
            ex.printStackTrace();
        }
        if (statuCode == HttpStatus.SC_OK) {
            try {
                String s = gm.getResponseBodyAsString();
                LinkedList<String> ll = new LinkedList<String>();
                String[] hrefs = parseHrefFromHtml(s);
                for (int i = 0; i < hrefs.length; i++) {
                    if (hrefs[i].indexOf(subject) > 0 && hrefs[i].indexOf("th=") > 0) {
                        String t = parseUrlFromHref(hrefs[i]);
                        ll.add(base_href + t);
                    }
                }
                if (ll.size() > 0) {
                    result = new String[ll.size()];
                    ll.toArray(result);
                }
            } catch (IOException e) {
                Logger.log("get serach mail response string fail");
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
	 * download attachment from gmail
	 * 
	 * @param localFilePath,where
	 *            you want to store attachment
	 * @param remoteUrl,attchment
	 *            belong to mail's url
	 * @param remoteFileName,attachment
	 *            file name
	 * @return wether download from gmail
	 */
    public boolean downLoadFile(String localFilePath, String remoteUrl, String remoteFileName) {
        boolean result = false;
        if (!isConnect) {
            if (!connect()) return false;
        }
        InputStream is = this.getAttachmentAsStream(remoteUrl, remoteFileName);
        if (is == null) {
            Logger.log("can't get attachment stream from remote url and remoteFileName");
            Logger.log("remoteUrl-->" + remoteUrl);
            Logger.log("remoteFileName-->" + remoteFileName);
            return false;
        }
        File f = new File(localFilePath);
        if (f.exists()) {
            Logger.log("local file system have this attachment,it will be delete for update");
            f.delete();
        }
        try {
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            byte[] bs = new byte[8192];
            int i = -1;
            while ((i = is.read(bs)) > -1) {
                fos.write(bs, 0, i);
            }
            fos.close();
            is.close();
            Logger.log("update over");
            result = true;
        } catch (Exception e) {
            Logger.log("local file system can't store attachment");
            e.printStackTrace();
        }
        return result;
    }

    /**
	 * upload local file to gmail
	 * 
	 * @param localFilePath,file
	 *            which want to upload
	 * @param subTitle,mail
	 *            title
	 * @return wether uploaded
	 */
    public boolean uploadFile(String localFilePath, String subject) {
        boolean result = false;
        if (session == null) {
            session = Session.getInstance(GMailCfg.GMAIL_SMTP_PROP, new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(name + "@gmail.com", pwd);
                }
            });
        }
        try {
            Message msg = new MimeMessage(session);
            msg.setHeader("X-Mailer", "smtpsend");
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(name + "@gmail.com", false));
            msg.setSubject(subject);
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.attachFile(localFilePath);
            MimeMultipart mmp = new MimeMultipart();
            mmp.addBodyPart(mbp);
            msg.setContent(mmp);
            Transport.send(msg);
        } catch (Exception e) {
            Logger.log("use smtp send mail fail!");
            e.printStackTrace();
        }
        return result;
    }

    /**
	 * delete mail like this subtitle
	 * 
	 * @param subject
	 * @return
	 */
    public boolean deleteMail(String subject) {
        boolean result = false;
        if (!isConnect) {
            if (!connect()) return result;
        }
        String[] urls = searchMailBySubject(subject);
        if (urls != null) {
            NameValuePair[] datas = new NameValuePair[urls.length + 3];
            for (int i = 0; i < urls.length; i++) {
                String t = urls[i].substring(urls[i].indexOf("th=") + 3);
                datas[i] = new NameValuePair("t", t);
            }
            datas[urls.length] = new NameValuePair("tact", "tr");
            datas[urls.length + 1] = new NameValuePair("bact", "");
            datas[urls.length + 2] = new NameValuePair("nvp_tbu_go", "start");
            PostMethod post = new PostMethod(base_href + "?" + at);
            post.setRequestBody(datas);
            try {
                statuCode = client.executeMethod(post);
            } catch (Exception e) {
                Logger.log("delete mail fail");
                e.printStackTrace();
            }
            if (statuCode == HttpStatus.SC_OK) result = true;
        } else {
            Logger.log("not find the mail match this title");
            Logger.log("title-->" + subject);
            result = true;
        }
        return result;
    }

    /**
	 * 
	 * @param htmlBody
	 * @return
	 */
    private String[] parseHrefFromHtml(String htmlBody) {
        String[] result = null;
        Pattern pt = Pattern.compile(GMailCfg.REGEX_FIND_HREF);
        Matcher mt = pt.matcher(htmlBody);
        LinkedList<String> ll = new LinkedList<String>();
        while (mt.find()) {
            ll.add(mt.group());
        }
        if (ll.size() > 0) {
            result = new String[ll.size()];
            ll.toArray(result);
        }
        return result;
    }

    /**
	 * 
	 * @param href
	 * @return
	 */
    private String parseUrlFromHref(String href) {
        String result = null;
        Pattern pt = Pattern.compile(GMailCfg.REGEX_FIND_URL_IN_HREF);
        Matcher mt = pt.matcher(href);
        if (mt.find()) result = mt.group(4);
        return result;
    }

    /**
	 * open a stream from gmail attachment
	 * 
	 * @param mailUrl,mail
	 *            url
	 * @param fileName,attachment
	 *            file
	 * @return
	 */
    private InputStream getAttachmentAsStream(String mailUrl, String fileName) {
        GetMethod get = new GetMethod(mailUrl);
        try {
            statuCode = client.executeMethod(get);
            if (statuCode == HttpStatus.SC_OK) {
                String s = get.getResponseBodyAsString();
                int a = s.indexOf(fileName);
                s = s.substring(a + fileName.length());
                String[] hrefs = parseHrefFromHtml(s);
                for (int i = 0; i < hrefs.length; i++) {
                    if (hrefs[i].indexOf(GMailCfg.ATTACHMENT_DOWNLOAD_FLAG) > 0) {
                        String url = GMailCfg.GMAIL_BASE + parseUrlFromHref(hrefs[i]);
                        GetMethod gt = new GetMethod(url);
                        int b = client.executeMethod(gt);
                        if (b == HttpStatus.SC_OK) return gt.getResponseBodyAsStream();
                    }
                }
            }
        } catch (Exception e) {
            Logger.log("open remote attachment stream fail");
            e.printStackTrace();
        }
        return null;
    }

    private boolean step1() {
        boolean result = false;
        PostMethod post = new PostMethod("https://www.google.com/accounts/ServiceLoginAuth");
        NameValuePair[] data = { new NameValuePair("service", "mail"), new NameValuePair("Email", name), new NameValuePair("Passwd", pwd), new NameValuePair("null", "Sign in"), new NameValuePair("continue", "https://gmail.google.com/gmail") };
        post.setRequestBody(data);
        try {
            statuCode = client.executeMethod(post);
        } catch (Exception ex) {
            Logger.log("step 1 fail");
            ex.printStackTrace();
            statuCode = -1;
            result = false;
        }
        if (statuCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            step1_1(post);
        } else {
            Logger.log("step 1 fail");
            result = false;
        }
        if (statuCode == HttpStatus.SC_OK) {
            result = true;
        } else {
            Logger.log("step 1 fail");
            result = false;
        }
        post.abort();
        return result;
    }

    private void step1_1(HttpMethod post) {
        GetMethod get = null;
        try {
            String loc = post.getResponseHeader("Location").getValue();
            get = new GetMethod(loc);
            statuCode = client.executeMethod(get);
            setLastHttpCon(get);
        } catch (Exception e) {
            Logger.log("step 1_1 fail");
            if (get != null) get.abort();
            e.printStackTrace();
            statuCode = -1;
        }
    }

    private void setLastHttpCon(HttpMethodBase method) {
        if (lastHttpCon != null) lastHttpCon.abort();
        lastHttpCon = method;
    }

    private boolean step2() {
        GetMethod get = null;
        try {
            String html = lastHttpCon.getResponseBodyAsString();
            int a = html.indexOf("location.replace(\"") + 18;
            int b = html.indexOf("\"", a + 1);
            String url = html.substring(a, b);
            url = url.replaceAll("u003d", "=");
            int x = url.indexOf("\\");
            while (x != -1) {
                url = url.substring(0, x) + url.substring(x + 1);
                x = url.indexOf("\\");
            }
            get = new GetMethod(url);
            statuCode = client.executeMethod(get);
            setLastHttpCon(get);
            return true;
        } catch (Exception ex) {
            Logger.log("step 2 fail");
            if (get != null) get.abort();
            ex.printStackTrace();
            statuCode = -1;
            return false;
        }
    }

    private boolean step3() {
        GetMethod get = null;
        try {
            String s = lastHttpCon.getResponseBodyAsString();
            int a = s.indexOf("top.location=\"");
            int b = s.indexOf("\";", a + 1);
            String temp = s.substring(a + 14, b);
            temp = temp.replaceAll("x26", "&");
            int x = temp.indexOf("\\");
            while (x != -1) {
                temp = temp.substring(0, x) + temp.substring(x + 1);
                x = temp.indexOf("\\");
            }
            String location = "http://mail.google.com/mail/" + temp;
            get = new GetMethod(location);
            statuCode = client.executeMethod(get);
            String s1 = get.getResponseBodyAsString();
            int a1 = s1.indexOf("<base href=\"");
            int b1 = s1.indexOf("\">", a1 + 1);
            base_href = s1.substring(a1 + 12, b1);
            a1 = s1.indexOf("<form action=\"");
            a1 = s1.indexOf("<form action=\"?", a1 + 10);
            b1 = s1.indexOf("\" name", a1 + 1);
            at = s1.substring(a1 + 15, b1);
            get.abort();
            return true;
        } catch (Exception e) {
            Logger.log("step 3 fail");
            if (get != null) get.abort();
            e.printStackTrace();
            statuCode = -1;
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPsw() {
        return pwd;
    }

    public void setPsw(String psw) {
        this.pwd = psw;
    }

    public boolean isConnect() {
        return isConnect;
    }
}
