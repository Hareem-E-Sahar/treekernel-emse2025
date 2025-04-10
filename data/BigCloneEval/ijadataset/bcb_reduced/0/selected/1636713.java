package net.mjrz.fm.onlinebanking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import net.mjrz.fm.entity.beans.ONLBDetails;
import net.mjrz.fm.utils.MiscUtils;
import org.apache.log4j.Logger;

/**
 * @author Mjrz contact@mjrz.net
 *
 */
public class MessageProcessor {

    private static Logger logger = Logger.getLogger(MessageProcessor.class.getName());

    public static String getOfxResponse(ONLBDetails details, OfxRequest request) {
        HttpsURLConnection conn = null;
        StringBuilder response = new StringBuilder();
        BufferedReader reader = null;
        try {
            String host = details.getUrl();
            String data = request.getRequestString();
            URL url = new URL(host);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("REQUEST_METHOD", "POST");
            conn.setRequestProperty("Content-Type", "application/x-ofx");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while (true) {
                int s = reader.read();
                if (s == -1) break;
                response.append((char) s);
            }
            conn.disconnect();
        } catch (Exception e) {
            logger.error(MiscUtils.stackTrace2String(e));
        } finally {
            if (conn != null) conn.disconnect();
            if (reader != null) try {
                reader.close();
            } catch (Exception e) {
                logger.error(MiscUtils.stackTrace2String(e));
            }
        }
        return response.toString();
    }
}
