package org.ozoneDB.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.ozoneDB.core.PropertyConfigurable;
import org.ozoneDB.core.PropertyInfo;

/**
 * Factory that creates streams that read/write to other streams via a Zip
 * stream.
 *
 * @author <a href="mailto:leoATmekenkampD0Tcom">Leo Mekenkamp (mind the anti sp@m)</a>
 * @version $Id: ZipStreamFactory.java,v 1.3 2004/12/12 20:29:47 leomekenkamp Exp $
 */
public class ZipStreamFactory implements StreamFactory, PropertyConfigurable {

    public static final PropertyInfo LEVEL = new PropertyInfo(".level", Integer.TYPE, "compression level in range [0-9], see java.util.zip.ZipOutputStream.setLevel(int)", new String[] { "1", "4" }, "1");

    public static final PropertyInfo METHOD = new PropertyInfo(".method", Integer.TYPE, "method used for compression, see java.util.zip.ZipOutputStream.setMethod(int)", new String[] { "8" }, "8");

    public static final PropertyInfo ENTRYNAME = new PropertyInfo(".entryName", String.class, "name for the ZipEntry in the zip file", new String[] { "entry", "FooBar", "R_Daneel_Olivaw" }, "O");

    private String prefix;

    private int method;

    private int level;

    private String entryName;

    /**
     * As prescribed by the <code>PropertyConfigurable</code> interface.
     */
    public ZipStreamFactory(Properties properties, String prefix) {
        this.prefix = prefix;
        method = Integer.parseInt(properties.getProperty(prefix + METHOD.getKey(), METHOD.getDefaultValue()));
        level = Integer.parseInt(properties.getProperty(prefix + LEVEL, LEVEL.getDefaultValue()));
        entryName = properties.getProperty(prefix + ENTRYNAME, ENTRYNAME.getDefaultValue());
    }

    public InputStream createInputStream(InputStream in) throws IOException {
        ZipInputStream result = new ZipInputStream(in);
        result.getNextEntry();
        return result;
    }

    public OutputStream createOutputStream(OutputStream out) throws IOException {
        ZipOutputStream result = new ZipOutputStream(out);
        result.putNextEntry(new ZipEntry(entryName));
        return result;
    }

    public Collection getPropertyInfos() {
        Collection result = new LinkedList();
        result.add(LEVEL);
        result.add(METHOD);
        result.add(ENTRYNAME);
        return result;
    }

    public String getPrefix() {
        return prefix;
    }
}
