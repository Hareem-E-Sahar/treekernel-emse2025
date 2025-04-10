package neembuuuploader.uploaders;

import java.util.logging.Level;
import neembuuuploader.accounts.HotFileAccount;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import java.io.File;
import javax.swing.JOptionPane;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 */
public class HotFile extends AbstractUploader implements UploaderAccountNecessary {

    HotFileAccount hotFileAccount = (HotFileAccount) AccountsManager.getAccount("HotFile.com");

    HttpGet httpget;

    HttpPost httppost;

    String strResponse, link;

    String start;

    String manageURL;

    public HotFile(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "HotFile.com";
        if (hotFileAccount.loginsuccessful) {
            host = hotFileAccount.username + " | HotFile.com";
        }
    }

    @Override
    public void run() {
        if (hotFileAccount.loginsuccessful) {
            host = hotFileAccount.username + " | HotFile.com";
        } else {
            host = "HotFile.com";
            uploadFailed();
            return;
        }
        uploadLogin();
    }

    private void uploadLogin() {
        try {
            status = UploadStatus.INITIALISING;
            if (file.length() > 419430400) {
                JOptionPane.showMessageDialog(neembuuuploader.NeembuuUploader.getInstance(), "<html><b>" + getClass().getSimpleName() + "</b> " + TranslationProvider.get("neembuuuploader.uploaders.maxfilesize") + ": <b>400MB</b></html>", getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
                uploadFailed();
                return;
            }
            status = UploadStatus.GETTINGCOOKIE;
            HttpParams params = new BasicHttpParams();
            params.setParameter("http.useragent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            DefaultHttpClient httpclient = new DefaultHttpClient(params);
            httpget = new HttpGet("http://hotfile.com/?cookiecheck=1");
            httpget.setHeader("Referer", "http://www.hotfile.com/");
            httpget.setHeader("Cache-Control", "max-age=0");
            httpget.setHeader("Origin", "http://www.hotfile.com/");
            httpget.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            httpclient = new DefaultHttpClient(params);
            httpclient.getCookieStore().addCookie(HotFileAccount.getHfcookie());
            HttpResponse httpresponse = httpclient.execute(httpget);
            strResponse = EntityUtils.toString(httpresponse.getEntity());
            start = "<form action=\"";
            link = strResponse.substring(strResponse.indexOf(start + "http://") + start.length());
            link = link.substring(0, link.indexOf("\""));
            NULogger.getLogger().info(link);
            httppost = new HttpPost(link);
            httppost.setHeader("Referer", "http://www.hotfile.com/");
            httppost.setHeader("Cache-Control", "max-age=0");
            httppost.setHeader("Origin", "http://www.hotfile.com/");
            httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            requestEntity.addPart("uploads[]", new MonitoredFileBody(file, uploadProgress));
            requestEntity.addPart("iagree", new StringBody("on"));
            requestEntity.addPart("", new StringBody("Upload"));
            httppost.setEntity(requestEntity);
            status = UploadStatus.UPLOADING;
            httpresponse = httpclient.execute(httppost);
            manageURL = httpresponse.getHeaders("Location")[0].getValue();
            NULogger.getLogger().log(Level.INFO, "HotFile Manage URL{0}", manageURL);
            NULogger.getLogger().info("Getting links from Manage URL");
            status = UploadStatus.GETTINGLINK;
            httpget = new HttpGet(manageURL);
            httpclient = new DefaultHttpClient(params);
            httpresponse = httpclient.execute(httpget);
            strResponse = EntityUtils.toString(httpresponse.getEntity());
            start = "<input type=\"text\" name=\"url\" id=\"url\" class=\"textfield\" value=\"";
            downURL = strResponse.substring(strResponse.indexOf(start) + start.length());
            downURL = downURL.substring(0, downURL.indexOf("\""));
            start = "<input type=\"text\" name=\"delete\" id=\"delete\" class=\"textfield\" value=\"";
            delURL = strResponse.substring(strResponse.indexOf(start) + start.length());
            delURL = delURL.substring(0, delURL.indexOf("\""));
            NULogger.getLogger().log(Level.INFO, "Download Link: {0}", downURL);
            NULogger.getLogger().log(Level.INFO, "Delete link: {0}", delURL);
            uploadFinished();
        } catch (Exception ex) {
            ex.printStackTrace();
            NULogger.getLogger().severe(ex.toString());
            uploadFailed();
        }
    }
}
