package APJP.HTTPS;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import APJP.HTTP11.HTTPMessageHeader;
import APJP.HTTP11.HTTPRequestMessage;
import APJP.HTTP11.HTTPResponseMessage;
import APJP.HTTP11.HTTPSRequest;

public class HTTPSServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static Logger logger;

    private static String APJP_KEY;

    private static String[] APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_KEY;

    private static String[] APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_VALUE;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        logger = Logger.getLogger(HTTPSServlet.class.getName());
        APJP_KEY = System.getProperty("APJP_KEY", "");
        APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_KEY = new String[5];
        APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_VALUE = new String[5];
        for (int i = 0; i < APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_KEY.length; i = i + 1) {
            APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_KEY[i] = System.getProperty("APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_" + (i + 1) + "_KEY", "");
            APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_VALUE[i] = System.getProperty("APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_" + (i + 1) + "_VALUE", "");
        }
    }

    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            httpServletResponse.setStatus(200);
            for (int i = 0; i < APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_KEY.length; i = i + 1) {
                if (APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_KEY[i].equalsIgnoreCase("") == false) {
                    httpServletResponse.addHeader(APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_KEY[i], APJP_REMOTE_HTTPS_SERVER_RESPONSE_PROPERTY_VALUE[i]);
                }
            }
            SecretKeySpec secretKeySpec = new SecretKeySpec(APJP_KEY.getBytes(), "ARCFOUR");
            Cipher inputStreamCipher = Cipher.getInstance("ARCFOUR");
            inputStreamCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            CipherInputStream httpRequestInputStream = new CipherInputStream(httpServletRequest.getInputStream(), inputStreamCipher);
            Cipher outputStreamCipher = Cipher.getInstance("ARCFOUR");
            outputStreamCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            CipherOutputStream httpResponseOutputStream = new CipherOutputStream(httpServletResponse.getOutputStream(), outputStreamCipher);
            HTTPRequestMessage httpRequestMessage1 = new HTTPRequestMessage(httpRequestInputStream);
            httpRequestMessage1.read();
            HTTPSRequest httpsRequest1 = new HTTPSRequest(httpRequestMessage1);
            httpsRequest1.open();
            try {
                HTTPResponseMessage httpResponseMessage1 = httpsRequest1.getHTTPResponseMessage();
                HTTPMessageHeader[] httpResponseMessage1Headers1 = httpResponseMessage1.getHTTPMessageHeaders();
                HTTPMessageHeader httpResponseMessage1Header1 = httpResponseMessage1Headers1[0];
                String httpResponseMessage1Header1Key1 = httpResponseMessage1Header1.getKey();
                String httpResponseMessage1Header1Value1 = httpResponseMessage1Header1.getValue();
                httpResponseOutputStream.write((httpResponseMessage1Header1Value1 + "\r\n").getBytes());
                for (int i = 1; i < httpResponseMessage1Headers1.length; i = i + 1) {
                    httpResponseMessage1Header1 = httpResponseMessage1Headers1[i];
                    httpResponseMessage1Header1Key1 = httpResponseMessage1Header1.getKey();
                    httpResponseMessage1Header1Value1 = httpResponseMessage1Header1.getValue();
                    httpResponseOutputStream.write((httpResponseMessage1Header1Key1 + ": " + httpResponseMessage1Header1Value1 + "\r\n").getBytes());
                }
                httpResponseOutputStream.write(("\r\n").getBytes());
                httpResponseMessage1.read(httpResponseOutputStream);
            } catch (Exception e) {
                throw e;
            } finally {
                try {
                    httpsRequest1.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "EXCEPTION", e);
            httpServletResponse.setStatus(500);
        }
    }
}
