package org.gudy.azureus2.core3.util.protocol.bc;

import java.io.IOException;
import java.net.*;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.UrlUtils;

public class Handler extends URLStreamHandler {

    public URLConnection openConnection(URL u) {
        URL magnet_url;
        try {
            String str = UrlUtils.parseTextForMagnets(u.toExternalForm());
            if (str == null) {
                Debug.out("Failed to transform bc url '" + u + "'");
                return (null);
            } else {
                magnet_url = new URL(str);
            }
        } catch (Throwable e) {
            Debug.out("Failed to transform bc url '" + u + "'", e);
            return (null);
        }
        try {
            return (magnet_url.openConnection());
        } catch (MalformedURLException e) {
            Debug.printStackTrace(e);
            return (null);
        } catch (IOException e) {
            Debug.printStackTrace(e);
            return (null);
        }
    }
}
