package net.xmlrpc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import libomv.utils.Helpers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * XMLRPCClient allows to call remote XMLRPC method.
 *
 * <p>
 * The following table shows how XML-RPC types are mapped to java call
 * parameters/response values.
 * </p>
 *
 * <p>
 * <table border="2" align="center" cellpadding="5">
 * <thead>
 * <tr>
 * <th>XML-RPC Type</th>
 * <th>Call Parameters</th>
 * <th>Call Response</th>
 * </tr>
 * </thead>
 *
 * <tbody>
 * <td>int, i4</td>
 * <td>byte<br />
 * Byte<br />
 * short<br />
 * Short<br />
 * int<br />
 * Integer</td>
 * <td>int<br />
 * Integer</td>
 * </tr>
 * <tr>
 * <td>i8</td>
 * <td>long<br />
 * Long</td>
 * <td>long<br />
 * Long</td>
 * </tr>
 * <tr>
 * <td>double</td>
 * <td>float<br />
 * Float<br />
 * double<br />
 * Double</td>
 * <td>double<br />
 * Double</td>
 * </tr>
 * <tr>
 * <td>string</td>
 * <td>String</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>boolean</td>
 * <td>boolean<br />
 * Boolean</td>
 * <td>boolean<br />
 * Boolean</td>
 * </tr>
 * <tr>
 * <td>dateTime.iso8601</td>
 * <td>java.util.Date<br />
 * java.util.Calendar</td>
 * <td>java.util.Date</td>
 * </tr>
 * <tr>
 * <td>base64</td>
 * <td>byte[]</td>
 * <td>byte[]</td>
 * </tr>
 * <tr>
 * <td>array</td>
 * <td>java.util.List&lt;Object&gt;<br />
 * Object[]</td>
 * <td>Object[]</td>
 * </tr>
 * <tr>
 * <td>struct</td>
 * <td>java.util.Map&lt;String, Object&gt;</td>
 * <td>java.util.Map&lt;String, Object&gt;</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * You can also pass as a parameter any object implementing XMLRPCSerializable
 * interface. In this case your object overrides getSerializable() telling how
 * to serialize to XMLRPC protocol
 * </p>
 */
public class XMLRPCClient extends XMLRPCCommon {

    private HttpClient client;

    private HttpPost postMethod;

    private HttpParams httpParams;

    /**
	 * XMLRPCClient constructor. Creates new instance based on server URI
	 *
	 * @param XMLRPC
	 *            server URI
	 */
    public XMLRPCClient(URI uri) throws XmlPullParserException {
        postMethod = new HttpPost(uri);
        postMethod.addHeader("Content-Type", "text/xml");
        httpParams = postMethod.getParams();
        HttpProtocolParams.setUseExpectContinue(httpParams, false);
        client = new DefaultHttpClient();
    }

    /**
	 * Convenience constructor. Creates new instance based on server String
	 * address
	 *
	 * @param XMLRPC
	 *            server address
	 */
    public XMLRPCClient(String url) throws XmlPullParserException {
        this(URI.create(url));
    }

    /**
	 * Convenience XMLRPCClient constructor. Creates new instance based on
	 * server URL
	 *
	 * @param XMLRPC
	 *            server URL
	 */
    public XMLRPCClient(URL url) throws XmlPullParserException {
        this(URI.create(url.toExternalForm()));
    }

    /**
	 * Sets basic authentication on web request using plain credentials
	 *
	 * @param username
	 *            The plain text username
	 * @param password
	 *            The plain text password
	 */
    public void setBasicAuthentication(String username, String password) {
        ((DefaultHttpClient) client).getCredentialsProvider().setCredentials(new AuthScope(postMethod.getURI().getHost(), postMethod.getURI().getPort(), AuthScope.ANY_REALM), new UsernamePasswordCredentials(username, password));
    }

    /**
	 * Registers a new scheme for this client. Useful to provide a scheme with
	 * custom security provider such as for certificate verification for the
	 * HTTPS scheme
	 *
	 * @param scheme
	 *            The scheme to add to the connection manager for this
	 *            connection
	 */
    public Scheme register(Scheme scheme) {
        return client.getConnectionManager().getSchemeRegistry().register(scheme);
    }

    public void cancel() {
        postMethod.abort();
    }

    public Object callEx(String method, Object[] params) throws XMLRPCException {
        Object object = null;
        try {
            String body = methodCall(method, params);
            HttpEntity entity = new StringEntity(body);
            postMethod.setEntity(entity);
            HttpResponse response = client.execute(postMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new XMLRPCException("HTTP status code: " + statusCode + " != " + HttpStatus.SC_OK);
            }
            entity = response.getEntity();
            if (entity != null) {
                InputStream inStream = entity.getContent();
                try {
                    XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
                    pullParser.setInput(new BufferedInputStream(inStream), Helpers.UTF8_ENCODING);
                    pullParser.nextTag();
                    pullParser.require(XmlPullParser.START_TAG, null, Tag.METHOD_RESPONSE);
                    pullParser.nextTag();
                    String tag = pullParser.getName();
                    if (tag.equals(Tag.PARAMS)) {
                        pullParser.nextTag();
                        pullParser.require(XmlPullParser.START_TAG, null, Tag.PARAM);
                        pullParser.nextTag();
                        object = iXMLRPCSerializer.deserialize(pullParser);
                        postMethod.abort();
                    } else if (tag.equals(Tag.FAULT)) {
                        pullParser.nextTag();
                        object = iXMLRPCSerializer.deserialize(pullParser);
                        postMethod.abort();
                        if (object instanceof Map) {
                            @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) object;
                            String faultString = (String) map.get(Tag.FAULT_STRING);
                            int faultCode = (Integer) map.get(Tag.FAULT_CODE);
                            throw new XMLRPCFault(faultString, faultCode);
                        }
                        throw new XMLRPCException("Bad <fault> format in XMLRPC response");
                    } else {
                        postMethod.abort();
                        throw new XMLRPCException("Bad tag <" + tag + "> in XMLRPC response - neither <params> nor <fault>");
                    }
                } catch (IOException ex) {
                    throw ex;
                } catch (RuntimeException ex) {
                    postMethod.abort();
                    throw ex;
                } finally {
                    inStream.close();
                }
            }
        } catch (XMLRPCException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new XMLRPCException(e);
        }
        return object;
    }

    private String methodCall(String method, Object[] params) throws IllegalArgumentException, IllegalStateException, IOException {
        StringWriter bodyWriter = new StringWriter();
        serializer.setOutput(bodyWriter);
        serializer.startDocument(null, null);
        serializer.startTag(null, Tag.METHOD_CALL);
        serializer.startTag(null, Tag.METHOD_NAME).text(method).endTag(null, Tag.METHOD_NAME);
        serializeParams(params);
        serializer.endTag(null, Tag.METHOD_CALL);
        serializer.endDocument();
        return bodyWriter.toString();
    }

    /**
	 * Convenience method call with no parameters
	 *
	 * @param method
	 *            name of method to call
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method) throws XMLRPCException {
        return callEx(method, null);
    }

    /**
	 * Convenience method call with one parameter
	 *
	 * @param method
	 *            name of method to call
	 * @param p0
	 *            method's parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method, Object p0) throws XMLRPCException {
        Object[] params = { p0 };
        return callEx(method, params);
    }

    /**
	 * Convenience method call with two parameters
	 *
	 * @param method
	 *            name of method to call
	 * @param p0
	 *            method's 1st parameter
	 * @param p1
	 *            method's 2nd parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method, Object p0, Object p1) throws XMLRPCException {
        Object[] params = { p0, p1 };
        return callEx(method, params);
    }

    /**
	 * Convenience method call with three parameters
	 *
	 * @param method
	 *            name of method to call
	 * @param p0
	 *            method's 1st parameter
	 * @param p1
	 *            method's 2nd parameter
	 * @param p2
	 *            method's 3rd parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method, Object p0, Object p1, Object p2) throws XMLRPCException {
        Object[] params = { p0, p1, p2 };
        return callEx(method, params);
    }

    /**
	 * Convenience method call with four parameters
	 *
	 * @param method
	 *            name of method to call
	 * @param p0
	 *            method's 1st parameter
	 * @param p1
	 *            method's 2nd parameter
	 * @param p2
	 *            method's 3rd parameter
	 * @param p3
	 *            method's 4th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method, Object p0, Object p1, Object p2, Object p3) throws XMLRPCException {
        Object[] params = { p0, p1, p2, p3 };
        return callEx(method, params);
    }

    /**
	 * Convenience method call with five parameters
	 *
	 * @param method
	 *            name of method to call
	 * @param p0
	 *            method's 1st parameter
	 * @param p1
	 *            method's 2nd parameter
	 * @param p2
	 *            method's 3rd parameter
	 * @param p3
	 *            method's 4th parameter
	 * @param p4
	 *            method's 5th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method, Object p0, Object p1, Object p2, Object p3, Object p4) throws XMLRPCException {
        Object[] params = { p0, p1, p2, p3, p4 };
        return callEx(method, params);
    }

    /**
	 * Convenience method call with six parameters
	 *
	 * @param method
	 *            name of method to call
	 * @param p0
	 *            method's 1st parameter
	 * @param p1
	 *            method's 2nd parameter
	 * @param p2
	 *            method's 3rd parameter
	 * @param p3
	 *            method's 4th parameter
	 * @param p4
	 *            method's 5th parameter
	 * @param p5
	 *            method's 6th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) throws XMLRPCException {
        Object[] params = { p0, p1, p2, p3, p4, p5 };
        return callEx(method, params);
    }

    /**
	 * Convenience method call with seven parameters
	 *
	 * @param method
	 *            name of method to call
	 * @param p0
	 *            method's 1st parameter
	 * @param p1
	 *            method's 2nd parameter
	 * @param p2
	 *            method's 3rd parameter
	 * @param p3
	 *            method's 4th parameter
	 * @param p4
	 *            method's 5th parameter
	 * @param p5
	 *            method's 6th parameter
	 * @param p6
	 *            method's 7th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) throws XMLRPCException {
        Object[] params = { p0, p1, p2, p3, p4, p5, p6 };
        return callEx(method, params);
    }

    /**
	 * Convenience method call with eight parameters
	 *
	 * @param method
	 *            name of method to call
	 * @param p0
	 *            method's 1st parameter
	 * @param p1
	 *            method's 2nd parameter
	 * @param p2
	 *            method's 3rd parameter
	 * @param p3
	 *            method's 4th parameter
	 * @param p4
	 *            method's 5th parameter
	 * @param p5
	 *            method's 6th parameter
	 * @param p6
	 *            method's 7th parameter
	 * @param p7
	 *            method's 8th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
    public Object call(String method, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) throws XMLRPCException {
        Object[] params = { p0, p1, p2, p3, p4, p5, p6, p7 };
        return callEx(method, params);
    }
}
