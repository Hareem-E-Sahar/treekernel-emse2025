package DE.FhG.IGD.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * This class provides utility methods for {@link Resource
 * Resources}. For instance, methods for zipping Resources
 * and for unzipping to a Resource.
 *
 * @author Volker Roth
 * @version "$Id: Resources.java 117 2000-12-06 17:47:39Z vroth $"
 */
public class Resources extends Object {

    public static final int BUFFER_SIZE = 1024;

    /**
     * No one instantiates this.
     */
    private Resources() {
    }

    /**
     * Zips the contents of the given resource to the given
     * output stream.
     *
     * @return The number of entries written to the
     *   ZIP archive.
     * @param resource The Resource to be zipped.
     * @param out The output stream to write the
     *   the zipped data to. This may already be
     *   a ZipOutputStream.
     */
    public static int zip(Resource resource, OutputStream out) throws IOException {
        List l;
        l = resource.list();
        return zip(resource, l, out);
    }

    /**
     * Zips the contents of the given resource to the given
     * output stream. Only those Resource items are included
     * whose names are passed in the given list.
     *
     * @return The number of entries written to the
     *   ZIP archive.
     * @param resource The Resource to be zipped.
     * @param names The names of the Resource items
     *   to include.
     * @param out The output stream to write the
     *   the zipped data to. This may already be
     *   a ZipOutputStream.
     */
    public static int zip(Resource resource, List names, OutputStream out) throws IOException {
        ZipOutputStream zip;
        InputStream in;
        Iterator i;
        String s;
        byte[] buf;
        int n;
        int u;
        if (out instanceof ZipOutputStream) {
            zip = (ZipOutputStream) out;
        } else {
            zip = new ZipOutputStream(out);
        }
        buf = new byte[BUFFER_SIZE];
        for (u = 0, i = names.iterator(); i.hasNext(); ) {
            s = (String) i.next();
            in = resource.getInputStream(s);
            if (in == null) {
                continue;
            }
            zip.putNextEntry(new ZipEntry(s));
            u++;
            while ((n = in.read(buf)) > 0) {
                zip.write(buf, 0, n);
            }
            in.close();
            zip.closeEntry();
        }
        if (u > 0) {
            zip.close();
        }
        return u;
    }

    /**
     * Unzips the zipped data in the given input stream and
     * writes the resulting files to the given Resource.
     *
     * @param in The input stream that must hold zipped
     *   data. It may already be a ZipInputStream.
     * @param resource The Resource to which the resulting
     *   files are written.
     */
    public static void unzip(InputStream in, Resource resource) throws Exception {
        unzip(in, resource, 0);
    }

    /**
     * Unzips the zipped data in the given input stream and
     * writes the resulting files to the given Resource. At
     * most the given number of bytes are unzipped. If the
     * unzipped data is more than the given number of bytes
     * then an exception is thrown after unzipping the given
     * number of bytes. If 0 is passed then the limit is set
     * to infinity.
     *
     * @param in The input stream that must hold zipped
     *   data. It may already be a ZipInputStream.
     * @param resource The Resource to which the resulting
     *   files are written.
     * @param max The maximum number of bytes to unzip from
     *   the given input stream.
     */
    public static long unzip(InputStream in, Resource resource, long max) throws IOException {
        ZipInputStream zip;
        OutputStream out;
        ZipEntry entry;
        String s;
        byte[] buf;
        long sum;
        int n;
        sum = 0;
        if (in instanceof ZipInputStream) {
            zip = (ZipInputStream) in;
        } else {
            zip = new ZipInputStream(in);
        }
        buf = new byte[BUFFER_SIZE];
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            s = entry.getName();
            out = resource.getOutputStream(s);
            while ((n = zip.read(buf)) > 0) {
                out.write(buf, 0, n);
                sum += n;
                if (max > 0 && sum > max) {
                    out.close();
                    zip.closeEntry();
                    zip.close();
                    throw new ZipException("Archive contains more than " + max + " bytes!");
                }
            }
            out.close();
            zip.closeEntry();
        }
        zip.close();
        return sum;
    }

    /**
     * This method removes leading and trailing slashes from
     * the given name.
     *
     * @param name The name.
     * @return The name without leading and trailing slashes.
     * @exception ResourceException if the name contains '..'
     *   or '//'.
     */
    public static String canonicalName(String name) throws ResourceException {
        int m;
        int n;
        if (name.indexOf("..") >= 0 || name.indexOf("//") >= 0) {
            throw new ResourceException("Resource name contains '..' or '//'");
        }
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }
}
