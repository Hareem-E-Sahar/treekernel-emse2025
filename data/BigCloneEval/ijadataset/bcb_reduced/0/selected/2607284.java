package org.kabeja.batik.tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.util.Base64EncoderStream;
import org.kabeja.xml.SAXFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class ImageBase64Encoder extends XMLFilterImpl implements SAXFilter {

    public static final String ELEMENT_IMAGE = "image";

    public static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";

    public static final String XLINK_ATTRIBUTE_HREF = "href";

    protected Map properties = new HashMap();

    public void setProperties(Map properties) {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (ELEMENT_IMAGE.equals(localName)) {
            boolean resolved = false;
            AttributesImpl attr = new AttributesImpl(atts);
            for (int i = 0; i < attr.getLength(); i++) {
                if (XLINK_ATTRIBUTE_HREF.equals(attr.getLocalName(i))) {
                    String file = attr.getValue(i);
                    String base = encodeBase64(file);
                    if (base != null) {
                        attr.setValue(i, base);
                        resolved = true;
                    }
                }
            }
            if (resolved) {
                super.startElement(uri, localName, qName, attr);
            }
        } else {
            super.startElement(uri, localName, qName, atts);
        }
    }

    protected String encodeBase64(String file) {
        try {
            URL url = new URL(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(32000);
            bos.write("data:;base64,".getBytes());
            Base64EncoderStream base = new Base64EncoderStream(bos);
            BufferedInputStream in = new BufferedInputStream(url.openStream());
            int l = -1;
            byte[] b = new byte[1024];
            while ((l = in.read(b)) > -1) {
                base.write(b, 0, l);
            }
            in.close();
            base.flush();
            base.close();
            return new String(bos.toByteArray());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    public Map getProperties() {
        return this.properties;
    }
}
