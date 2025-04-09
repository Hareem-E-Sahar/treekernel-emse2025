package net.sf.opentranquera.pagespy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

/**
 * esto es lo que se pasa desde el filter (la pagina HTML) a los que estan escuchando.
 * @author Guille
 */
public class Page implements Serializable {

    private static final long serialVersionUID = -9036059553738132487L;

    private static Logger logger = Logger.getLogger(PageSpyFilter.LOG_CAT);

    private String id;

    private byte[] data;

    private Map properties;

    private boolean zipped = false;

    public Page(String id, byte[] data) {
        this.id = id;
        try {
            ZipOutputStream oZipOutputStream;
            ByteArrayOutputStream oByteArrayOutputStream;
            oByteArrayOutputStream = new ByteArrayOutputStream();
            oZipOutputStream = new ZipOutputStream(oByteArrayOutputStream);
            oZipOutputStream.putNextEntry(new ZipEntry(Page.class.getName()));
            oZipOutputStream.write(data);
            oZipOutputStream.finish();
            oZipOutputStream.flush();
            oZipOutputStream.closeEntry();
            oZipOutputStream.close();
            oByteArrayOutputStream.close();
            this.data = oByteArrayOutputStream.toByteArray();
            this.zipped = true;
        } catch (IOException e) {
            logger.error("Error while zipping the page", e);
            this.data = data;
            this.zipped = false;
        }
        this.properties = new HashMap();
    }

    /**
	 * @return Returns the data.
	 */
    public byte[] getData() {
        if (this.zipped) {
            try {
                ByteArrayInputStream oByteArrayInputStream;
                ZipInputStream oZipInputStream;
                byte abBuffer[];
                StringBuffer oStringBuffer;
                int nParcial;
                long lAcumulado;
                oByteArrayInputStream = new ByteArrayInputStream(this.data);
                oZipInputStream = new ZipInputStream(oByteArrayInputStream);
                if (oZipInputStream.getNextEntry() != null) {
                    abBuffer = new byte[10240];
                    oStringBuffer = new StringBuffer(102400);
                    nParcial = 0;
                    lAcumulado = 0;
                    while ((nParcial = oZipInputStream.read(abBuffer, 0, 10240)) != -1) {
                        oStringBuffer.append(new String(abBuffer, 0, nParcial));
                        lAcumulado += nParcial;
                    }
                    oZipInputStream.closeEntry();
                    oZipInputStream.close();
                    oByteArrayInputStream.close();
                    this.zipped = false;
                    return oStringBuffer.toString().getBytes();
                } else {
                    logger.error("Zip Entry not found");
                    return null;
                }
            } catch (IOException e) {
                logger.error("Error while unzipping the page", e);
                return null;
            }
        } else {
            return data;
        }
    }

    /**
	 * @return Returns the id.
	 */
    public String getId() {
        return id;
    }

    public String toString() {
        StringBuffer page = new StringBuffer(60);
        page.append("<PAGE>\n").append(new String(this.getData()));
        page.append("\n<PROPERTIES>\n");
        TreeSet ts = new TreeSet();
        ts.addAll(this.getProperties().keySet());
        Iterator it = ts.iterator();
        while (it.hasNext()) {
            String propName = (String) it.next();
            page.append(propName).append(": ").append(this.getProperties().get(propName)).append("<br/>");
        }
        page.append("\n</PROPERTIES>\n");
        page.append("\n</PAGE>\n");
        return page.toString();
    }

    /**
	 * @return Returns the properties.
	 */
    public Map getProperties() {
        return properties;
    }
}
