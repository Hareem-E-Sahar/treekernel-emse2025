package org.gudy.azureus2.pluginsimpl.local.utils.resourcedownloader;

import com.aelitis.azureus.core.util.Java15Utils;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.net.PasswordAuthentication;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;
import org.gudy.azureus2.core3.util.AETemporaryFileHandler;
import org.gudy.azureus2.core3.util.AEThread2;
import org.gudy.azureus2.core3.util.AddressUtils;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.core3.util.UrlUtils;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.security.*;
import org.gudy.azureus2.plugins.utils.resourcedownloader.*;
import com.aelitis.azureus.core.util.DeleteFileOnCloseInputStream;

public class ResourceDownloaderURLImpl extends ResourceDownloaderBaseImpl implements SEPasswordListener {

    private static final int BUFFER_SIZE = 32768;

    private static final int MAX_IN_MEM_READ_SIZE = 256 * 1024;

    protected URL original_url;

    protected boolean auth_supplied;

    protected String user_name;

    protected String password;

    protected InputStream input_stream;

    protected boolean cancel_download = false;

    protected boolean download_initiated;

    protected long size = -2;

    protected boolean force_no_proxy = false;

    private final String post_data;

    public ResourceDownloaderURLImpl(ResourceDownloaderBaseImpl _parent, URL _url) {
        this(_parent, _url, false, null, null);
    }

    public ResourceDownloaderURLImpl(ResourceDownloaderBaseImpl _parent, URL _url, String _user_name, String _password) {
        this(_parent, _url, true, _user_name, _password);
    }

    public ResourceDownloaderURLImpl(ResourceDownloaderBaseImpl _parent, URL _url, boolean _auth_supplied, String _user_name, String _password) {
        this(_parent, _url, null, _auth_supplied, _user_name, _password);
    }

    /**
	 * 
	 * @param _parent
	 * @param _url
	 * @param _data if null, GET will be used, otherwise POST will be used with
	 *              the data supplied
	 * @param _auth_supplied
	 * @param _user_name
	 * @param _password
	 */
    public ResourceDownloaderURLImpl(ResourceDownloaderBaseImpl _parent, URL _url, String _data, boolean _auth_supplied, String _user_name, String _password) {
        super(_parent);
        original_url = _url;
        post_data = _data;
        auth_supplied = _auth_supplied;
        user_name = _user_name;
        password = _password;
    }

    protected void setForceNoProxy(boolean force_no_proxy) {
        this.force_no_proxy = force_no_proxy;
    }

    protected URL getURL() {
        return (original_url);
    }

    public String getName() {
        return (original_url.toString());
    }

    public long getSize() throws ResourceDownloaderException {
        if (size == -2) {
            try {
                ResourceDownloaderURLImpl c = (ResourceDownloaderURLImpl) getClone(this);
                addReportListener(c);
                size = c.getSizeSupport();
                setProperties(c);
            } finally {
                if (size == -2) {
                    size = -1;
                }
            }
        }
        return (size);
    }

    protected void setSize(long l) {
        size = l;
    }

    public void setProperty(String name, Object value) throws ResourceDownloaderException {
        setPropertySupport(name, value);
    }

    protected long getSizeSupport() throws ResourceDownloaderException {
        try {
            String protocol = original_url.getProtocol().toLowerCase();
            if (protocol.equals("magnet") || protocol.equals("dht")) {
                return (-1);
            }
            reportActivity(this, "Getting size of " + original_url);
            try {
                URL url = new URL(original_url.toString().replaceAll(" ", "%20"));
                url = AddressUtils.adjustURL(url);
                try {
                    if (auth_supplied) {
                        SESecurityManager.setPasswordHandler(url, this);
                    }
                    for (int i = 0; i < 2; i++) {
                        try {
                            HttpURLConnection con;
                            if (url.getProtocol().equalsIgnoreCase("https")) {
                                HttpsURLConnection ssl_con = (HttpsURLConnection) openConnection(url);
                                ssl_con.setHostnameVerifier(new HostnameVerifier() {

                                    public boolean verify(String host, SSLSession session) {
                                        return (true);
                                    }
                                });
                                con = ssl_con;
                            } else {
                                con = (HttpURLConnection) openConnection(url);
                            }
                            con.setRequestMethod("HEAD");
                            con.setRequestProperty("User-Agent", Constants.AZUREUS_NAME + " " + Constants.AZUREUS_VERSION);
                            setRequestProperties(con, false);
                            con.connect();
                            int response = con.getResponseCode();
                            if ((response != HttpURLConnection.HTTP_ACCEPTED) && (response != HttpURLConnection.HTTP_OK)) {
                                setProperty("URL_HTTP_Response", new Long(response));
                                throw (new ResourceDownloaderException(this, "Error on connect for '" + trimForDisplay(url) + "': " + Integer.toString(response) + " " + con.getResponseMessage()));
                            }
                            getRequestProperties(con);
                            return (UrlUtils.getContentLength(con));
                        } catch (SSLException e) {
                            if (i == 0) {
                                if (SESecurityManager.installServerCertificates(url) != null) {
                                    continue;
                                }
                            }
                            throw (e);
                        } catch (IOException e) {
                            if (i == 0) {
                                URL retry_url = UrlUtils.getIPV4Fallback(url);
                                if (retry_url != null) {
                                    url = retry_url;
                                    continue;
                                }
                            }
                            throw (e);
                        }
                    }
                    throw (new ResourceDownloaderException(this, "Should never get here"));
                } finally {
                    if (auth_supplied) {
                        SESecurityManager.setPasswordHandler(url, null);
                    }
                }
            } catch (java.net.MalformedURLException e) {
                throw (new ResourceDownloaderException(this, "Exception while parsing URL '" + original_url + "':" + e.getMessage(), e));
            } catch (java.net.UnknownHostException e) {
                throw (new ResourceDownloaderException(this, "Exception while initializing download of '" + trimForDisplay(original_url) + "': Unknown Host '" + e.getMessage() + "'", e));
            } catch (java.io.IOException e) {
                throw (new ResourceDownloaderException(this, "I/O Exception while downloading '" + trimForDisplay(original_url) + "'", e));
            }
        } catch (Throwable e) {
            ResourceDownloaderException rde;
            if (e instanceof ResourceDownloaderException) {
                rde = (ResourceDownloaderException) e;
            } else {
                Debug.out(e);
                rde = new ResourceDownloaderException(this, "Unexpected error", e);
            }
            throw (rde);
        }
    }

    public ResourceDownloaderBaseImpl getClone(ResourceDownloaderBaseImpl parent) {
        ResourceDownloaderURLImpl c = new ResourceDownloaderURLImpl(parent, original_url, post_data, auth_supplied, user_name, password);
        c.setSize(size);
        c.setProperties(this);
        c.setForceNoProxy(force_no_proxy);
        return (c);
    }

    public void asyncDownload() {
        final Object parent_tls = TorrentUtils.getTLS();
        AEThread2 t = new AEThread2("ResourceDownloader:asyncDownload - " + trimForDisplay(original_url), true) {

            public void run() {
                Object child_tls = TorrentUtils.getTLS();
                TorrentUtils.setTLS(parent_tls);
                try {
                    download();
                } catch (ResourceDownloaderException e) {
                } finally {
                    TorrentUtils.setTLS(child_tls);
                }
            }
        };
        t.start();
    }

    public InputStream download() throws ResourceDownloaderException {
        try {
            reportActivity(this, getLogIndent() + "Downloading: " + trimForDisplay(original_url));
            try {
                this_mon.enter();
                if (download_initiated) {
                    throw (new ResourceDownloaderException(this, "Download already initiated"));
                }
                download_initiated = true;
            } finally {
                this_mon.exit();
            }
            try {
                URL url = new URL(original_url.toString().replaceAll(" ", "%20"));
                String protocol = url.getProtocol().toLowerCase();
                if (url.getPort() == -1 && !(protocol.equals("magnet") || protocol.equals("dht"))) {
                    int target_port;
                    if (protocol.equals("http")) {
                        target_port = 80;
                    } else {
                        target_port = 443;
                    }
                    try {
                        String str = original_url.toString().replaceAll(" ", "%20");
                        int pos = str.indexOf("://");
                        pos = str.indexOf("/", pos + 4);
                        if (pos == -1) {
                            url = new URL(str + ":" + target_port + "/");
                        } else {
                            url = new URL(str.substring(0, pos) + ":" + target_port + str.substring(pos));
                        }
                    } catch (Throwable e) {
                        Debug.printStackTrace(e);
                    }
                }
                url = AddressUtils.adjustURL(url);
                try {
                    if (auth_supplied) {
                        SESecurityManager.setPasswordHandler(url, this);
                    }
                    boolean use_compression = true;
                    boolean follow_redirect = true;
                    redirect_label: for (int redirect_loop = 0; redirect_loop < 2 && follow_redirect; redirect_loop++) {
                        follow_redirect = false;
                        for (int connect_loop = 0; connect_loop < 2; connect_loop++) {
                            File temp_file = null;
                            try {
                                HttpURLConnection con;
                                if (url.getProtocol().equalsIgnoreCase("https")) {
                                    HttpsURLConnection ssl_con = (HttpsURLConnection) openConnection(url);
                                    ssl_con.setHostnameVerifier(new HostnameVerifier() {

                                        public boolean verify(String host, SSLSession session) {
                                            return (true);
                                        }
                                    });
                                    con = ssl_con;
                                } else {
                                    con = (HttpURLConnection) openConnection(url);
                                }
                                con.setRequestProperty("User-Agent", Constants.AZUREUS_NAME + " " + Constants.AZUREUS_VERSION);
                                con.setRequestProperty("Connection", "close");
                                if (use_compression) {
                                    con.addRequestProperty("Accept-Encoding", "gzip");
                                }
                                setRequestProperties(con, use_compression);
                                if (post_data != null) {
                                    con.setDoOutput(true);
                                    con.setRequestMethod("POST");
                                    OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                                    wr.write(post_data);
                                    wr.flush();
                                }
                                con.connect();
                                int response = con.getResponseCode();
                                if (response == HttpURLConnection.HTTP_MOVED_TEMP || response == HttpURLConnection.HTTP_MOVED_PERM) {
                                    String move_to = con.getHeaderField("location");
                                    if (move_to != null && url.getProtocol().equalsIgnoreCase("http")) {
                                        try {
                                            URL move_to_url = new URL(URLDecoder.decode(move_to, "UTF-8"));
                                            if (move_to_url.getProtocol().equalsIgnoreCase("https")) {
                                                url = move_to_url;
                                                follow_redirect = true;
                                                continue redirect_label;
                                            }
                                        } catch (Throwable e) {
                                        }
                                    }
                                }
                                if (response != HttpURLConnection.HTTP_ACCEPTED && response != HttpURLConnection.HTTP_OK) {
                                    setProperty("URL_HTTP_Response", new Long(response));
                                    throw (new ResourceDownloaderException(this, "Error on connect for '" + trimForDisplay(url) + "': " + Integer.toString(response) + " " + con.getResponseMessage()));
                                }
                                getRequestProperties(con);
                                boolean compressed = false;
                                try {
                                    this_mon.enter();
                                    input_stream = con.getInputStream();
                                    String encoding = con.getHeaderField("content-encoding");
                                    if (encoding != null) {
                                        if (encoding.equalsIgnoreCase("gzip")) {
                                            compressed = true;
                                            input_stream = new GZIPInputStream(input_stream);
                                        } else if (encoding.equalsIgnoreCase("deflate")) {
                                            compressed = true;
                                            input_stream = new InflaterInputStream(input_stream);
                                        }
                                    }
                                } finally {
                                    this_mon.exit();
                                }
                                ByteArrayOutputStream baos = null;
                                FileOutputStream fos = null;
                                try {
                                    byte[] buf = new byte[BUFFER_SIZE];
                                    long total_read = 0;
                                    long size = compressed ? -1 : UrlUtils.getContentLength(con);
                                    baos = size > 0 ? new ByteArrayOutputStream(size > MAX_IN_MEM_READ_SIZE ? MAX_IN_MEM_READ_SIZE : (int) size) : new ByteArrayOutputStream();
                                    while (!cancel_download) {
                                        int read = input_stream.read(buf);
                                        if (read > 0) {
                                            if (total_read > MAX_IN_MEM_READ_SIZE) {
                                                if (fos == null) {
                                                    temp_file = AETemporaryFileHandler.createTempFile();
                                                    fos = new FileOutputStream(temp_file);
                                                    fos.write(baos.toByteArray());
                                                    baos = null;
                                                }
                                                fos.write(buf, 0, read);
                                            } else {
                                                baos.write(buf, 0, read);
                                            }
                                            total_read += read;
                                            informAmountComplete(total_read);
                                            if (size > 0) {
                                                informPercentDone((int) ((100 * total_read) / size));
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                    if (size > 0 && total_read != size) {
                                        if (total_read > size) {
                                            Debug.outNoStack("Inconsistent stream length for '" + trimForDisplay(original_url) + "': expected = " + size + ", actual = " + total_read);
                                        } else {
                                            throw (new IOException("Premature end of stream"));
                                        }
                                    }
                                } finally {
                                    if (fos != null) {
                                        fos.close();
                                    }
                                    input_stream.close();
                                }
                                InputStream res;
                                if (temp_file != null) {
                                    res = new DeleteFileOnCloseInputStream(temp_file);
                                    temp_file = null;
                                } else {
                                    res = new ByteArrayInputStream(baos.toByteArray());
                                }
                                boolean handed_over = false;
                                try {
                                    if (informComplete(res)) {
                                        handed_over = true;
                                        return (res);
                                    }
                                } finally {
                                    if (!handed_over) {
                                        res.close();
                                    }
                                }
                                throw (new ResourceDownloaderException(this, "Contents downloaded but rejected: '" + trimForDisplay(original_url) + "'"));
                            } catch (SSLException e) {
                                if (connect_loop == 0) {
                                    if (SESecurityManager.installServerCertificates(url) != null) {
                                        continue;
                                    }
                                }
                                throw (e);
                            } catch (ZipException e) {
                                if (connect_loop == 0) {
                                    use_compression = false;
                                    continue;
                                }
                            } catch (IOException e) {
                                if (connect_loop == 0) {
                                    String msg = e.getMessage();
                                    if (msg != null) {
                                        msg = msg.toLowerCase(MessageText.LOCALE_ENGLISH);
                                        if (msg.indexOf("gzip") != -1) {
                                            use_compression = false;
                                            continue;
                                        }
                                    }
                                    URL retry_url = UrlUtils.getIPV4Fallback(url);
                                    if (retry_url != null) {
                                        url = retry_url;
                                        continue;
                                    }
                                }
                                throw (e);
                            } finally {
                                if (temp_file != null) {
                                    temp_file.delete();
                                }
                            }
                        }
                    }
                    throw (new ResourceDownloaderException(this, "Should never get here"));
                } finally {
                    if (auth_supplied) {
                        SESecurityManager.setPasswordHandler(url, null);
                    }
                }
            } catch (java.net.MalformedURLException e) {
                throw (new ResourceDownloaderException(this, "Exception while parsing URL '" + trimForDisplay(original_url) + "':" + e.getMessage(), e));
            } catch (java.net.UnknownHostException e) {
                throw (new ResourceDownloaderException(this, "Exception while initializing download of '" + trimForDisplay(original_url) + "': Unknown Host '" + e.getMessage() + "'", e));
            } catch (java.io.IOException e) {
                throw (new ResourceDownloaderException(this, "I/O Exception while downloading '" + trimForDisplay(original_url) + "'", e));
            }
        } catch (Throwable e) {
            ResourceDownloaderException rde;
            if (e instanceof ResourceDownloaderException) {
                rde = (ResourceDownloaderException) e;
            } else {
                Debug.out(e);
                rde = new ResourceDownloaderException(this, "Unexpected error", e);
            }
            informFailed(rde);
            throw (rde);
        }
    }

    public void cancel() {
        setCancelled();
        cancel_download = true;
        try {
            this_mon.enter();
            if (input_stream != null) {
                try {
                    input_stream.close();
                } catch (Throwable e) {
                }
            }
        } finally {
            this_mon.exit();
        }
        informFailed(new ResourceDownloaderCancelledException(this));
    }

    protected void setRequestProperties(HttpURLConnection con, boolean use_compression) {
        Map properties = getLCKeyProperties();
        Iterator it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith("url_") && value instanceof String) {
                key = key.substring(4);
                if (key.equals("accept-encoding") && !use_compression) {
                } else {
                    con.setRequestProperty(key, (String) value);
                }
            }
        }
    }

    protected void getRequestProperties(HttpURLConnection con) {
        try {
            setProperty(ResourceDownloader.PR_STRING_CONTENT_TYPE, con.getContentType());
            setProperty("URL_URL", con.getURL());
            Map headers = con.getHeaderFields();
            Iterator it = headers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                Object val = entry.getValue();
                if (key != null) {
                    setProperty("URL_" + key, val);
                }
            }
            setPropertiesSet();
        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }
    }

    public PasswordAuthentication getAuthentication(String realm, URL tracker) {
        if (user_name == null || password == null) {
            String user_info = tracker.getUserInfo();
            if (user_info == null) {
                return (null);
            }
            String user_bit = user_info;
            String pw_bit = "";
            int pos = user_info.indexOf(':');
            if (pos != -1) {
                user_bit = user_info.substring(0, pos);
                pw_bit = user_info.substring(pos + 1);
            }
            return (new PasswordAuthentication(user_bit, pw_bit.toCharArray()));
        }
        return (new PasswordAuthentication(user_name, password.toCharArray()));
    }

    public void setAuthenticationOutcome(String realm, URL tracker, boolean success) {
    }

    public void clearPasswords() {
    }

    private URLConnection openConnection(URL url) throws IOException {
        if (this.force_no_proxy) {
            return Java15Utils.openConnectionForceNoProxy(url);
        } else {
            return url.openConnection();
        }
    }

    protected String trimForDisplay(URL url) {
        String str = url.toString();
        int pos = str.indexOf('?');
        if (pos != -1) {
            str = str.substring(0, pos);
        }
        return (str);
    }
}
