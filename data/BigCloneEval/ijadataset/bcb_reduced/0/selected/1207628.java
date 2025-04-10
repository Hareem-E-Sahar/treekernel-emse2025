package org.tscribble.bitleech.plugins.http.net.impl.httpcomponents;

import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.tscribble.bitleech.core.download.auth.AuthManager;
import org.tscribble.bitleech.core.download.auth.IAuthProvider;
import org.tscribble.bitleech.core.download.protocol.AbstractProtocolClient;
import org.tscribble.bitleech.plugins.http.auth.HTTPAuthProvider;
import org.tscribble.bitleech.plugins.http.auth.HTTPAuthType;
import org.tscribble.bitleech.plugins.http.auth.HTTPAuthentication;

/**
 * @author triston
 * 
 *	Created on Aug 09, 2007
 */
public class Components_HTTPClient extends AbstractProtocolClient {

    /**
	 * Logger for this class
	 */
    protected final Logger log = Logger.getLogger("Components_HTTPClient");

    private DefaultHttpClient cl;

    private HttpHead head;

    private HttpGet get;

    private HttpEntity ent;

    private HTTPAuthentication auth;

    private HTTPAuthProvider ap;

    public Components_HTTPClient() {
    }

    public void getDownloadInfo(String _url) throws Exception {
        cl = new DefaultHttpClient();
        InfoAuthPromter hp = new InfoAuthPromter();
        cl.setCredentialsProvider(hp);
        head = new HttpHead(_url);
        head.setHeader("User-Agent", "test");
        head.setHeader("Accept", "*/*");
        head.setHeader("Range", "bytes=0-");
        HttpResponse resp = cl.execute(head);
        ent = resp.getEntity();
        int code = resp.getStatusLine().getStatusCode();
        if (code == 401) {
            throw new Exception("HTTP Auth Failed");
        }
        AuthManager.putAuth(getSite(), auth);
        setURL(head.getURI().toString());
        setSize(ent.getContentLength());
        setRangeEnd(getSize() - 1);
        setResumable(code == 206);
    }

    @Override
    public void setAuthProvider(IAuthProvider ap) {
        this.ap = (HTTPAuthProvider) ap;
    }

    @Override
    public IAuthProvider getAuthProvider() {
        return ap;
    }

    public void initGet() throws Exception {
        cl = new DefaultHttpClient();
        GetAuthPromter hp = new GetAuthPromter();
        cl.setCredentialsProvider(hp);
        get = new HttpGet(getURL());
        get.setHeader("User-Agent", "test");
        get.setHeader("Accept", "*/*");
        get.setHeader("Range", "bytes=" + getPosition() + "-" + getRangeEnd());
        HttpResponse resp = cl.execute(get);
        ent = resp.getEntity();
        setInputStream(ent.getContent());
    }

    public void disconnect() {
        try {
            if (head != null) {
                head.abort();
            }
            if (get != null) {
                get.abort();
                ent.consumeContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            get = null;
            head = null;
        }
    }

    private class InfoAuthPromter extends BasicCredentialsProvider {

        public Credentials getCredentials(AuthScope scope) {
            Credentials cred = null;
            ap.setRealm(scope.getRealm());
            ap.setSite(getSite());
            log.debug("getCredentials(AuthScope) - Scheme: " + scope.getScheme());
            try {
                ap.setAuthType(HTTPAuthType.BASIC);
                auth = (HTTPAuthentication) ap.promptAuthentication();
                cred = new UsernamePasswordCredentials(auth.getUser(), auth.getPassword());
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
            return cred;
        }
    }

    private class GetAuthPromter extends BasicCredentialsProvider {

        public Credentials getCredentials(AuthScope scope) {
            Credentials cred = null;
            auth = (HTTPAuthentication) AuthManager.getAuth(getSite());
            log.debug("getCredentials(AuthScope) - Scheme: " + scope.getScheme());
            try {
                cred = new UsernamePasswordCredentials(auth.getUser(), auth.getPassword());
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
            return cred;
        }
    }
}
