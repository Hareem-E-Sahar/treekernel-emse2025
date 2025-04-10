package org.openejb.alt.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Loads DTDs from disk so you don't have to hit the web to validate
 * configuration files.  This class will fail silently if the files aren't
 * available locally, and you'll end up hitting the web anyway.
 *
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision: 1.3 $
 */
public class DTDResolver implements EntityResolver {

    public static HashMap dtds = new HashMap();

    static {
        byte[] bytes = getDtd("ejb-jar_1_1.dtd");
        if (bytes != null) {
            dtds.put("ejb-jar.dtd", bytes);
            dtds.put("ejb-jar_1_1.dtd", bytes);
        }
    }

    public static byte[] getDtd(String dtdName) {
        try {
            URL dtd = new URL("resource:/openejb/dtds/" + dtdName);
            InputStream in = dtd.openStream();
            if (in == null) return null;
            byte[] buf = new byte[512];
            in = new BufferedInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int count;
            while ((count = in.read(buf)) > -1) out.write(buf, 0, count);
            in.close();
            out.close();
            return out.toByteArray();
        } catch (Throwable e) {
            return null;
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        int pos = systemId.lastIndexOf('/');
        if (pos != -1) {
            systemId = systemId.substring(pos + 1);
        }
        byte[] data = (byte[]) dtds.get(systemId);
        if (data != null) {
            return new InputSource(new ByteArrayInputStream(data));
        } else {
            return null;
        }
    }
}
