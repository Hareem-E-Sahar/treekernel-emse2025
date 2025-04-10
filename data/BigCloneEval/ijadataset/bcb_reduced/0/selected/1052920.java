package de.iritgo.aktera.webservices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.transport.Transport;
import org.xmlpull.v1.XmlPullParserException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpClientTransport extends Transport {

    private HttpClient httpClient;

    public HttpClientTransport(String url) {
        super(url);
    }

    public HttpClientTransport() {
        super();
    }

    @Override
    public void call(String soapAction, SoapEnvelope envelope) throws IOException, XmlPullParserException {
        if (soapAction == null) {
            soapAction = "\"\"";
        }
        byte[] requestData = createRequestData(envelope);
        requestDump = debug ? new String(requestData) : null;
        responseDump = null;
        HttpPost method = new HttpPost(url);
        method.addHeader("User-Agent", "kSOAP/2.0-Excilys");
        method.addHeader("SOAPAction", "soapAction");
        method.addHeader("Content-Type", "text/xml");
        HttpEntity entity = new ByteArrayEntity(requestData);
        method.setEntity(entity);
        HttpResponse response = httpClient.execute(method);
        InputStream inputStream = response.getEntity().getContent();
        if (debug) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            while (true) {
                int rd = inputStream.read(buf, 0, 256);
                if (rd == -1) {
                    break;
                }
                bos.write(buf, 0, rd);
            }
            bos.flush();
            buf = bos.toByteArray();
            responseDump = new String(buf);
            inputStream.close();
            inputStream = new ByteArrayInputStream(buf);
        }
        parseResponse(envelope, inputStream);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
