package org.dellroad.jc;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import org.dellroad.jc.cgen.Util;

/**
 * Singleton class used as the starting point for object file generation.
 * The JC virtual machine will invoke {@link #generateObject generateObject()}
 * on the singleton instance when it needs to create a new ELF object file.
 *
 * <p>
 * The object file is actually generated by whatever {@link ObjectGenerator}
 * is specified by the <code>jc.object.generator</code> system property.
 * By default, a
 * {@link org.dellroad.jc.cgen.JCObjectGenerator JCObjectGenerator} is used.
 */
public final class Generate {

    private static Generate instance;

    private static final String objectDir = SearchpathFinder.splitPath(System.getProperty("jc.object.path", "."))[0];

    /**
	 * The system-dependent character used to separate directory
	 * names in a path name.
	 */
    public static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);

    /**
	 * The system-dependent character used to separate individual
	 * paths in a search path.
	 */
    public static final char PATH_SEPARATOR = System.getProperty("path.separator").charAt(0);

    private final ObjectGenerator generator;

    private Generate() {
        try {
            this.generator = (ObjectGenerator) Class.forName(System.getProperty("jc.object.generator")).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * Retrieve the singleton instance of this class.
	 */
    public static synchronized Generate v() {
        if (instance == null) instance = new Generate();
        return instance;
    }

    /**
	 * Generate the ELF object file for the named class. The object
	 * file is written into the first directory in the search path
	 * specified by the <code>jc.object.path</code> system property.
	 *
	 * @param className	name of class (with slashes not dots).
	 * @param loader	class loader used to acquire the class file
	 * @see #objectFile objectFile()
	 */
    public void generateObject(String className, ClassLoader loader) throws Exception {
        generator.generateObject(className, new JCFinder(loader), objectFile(className));
    }

    /**
	 * Return the file that contains the ELF object for the class.
	 * This will be a filename under the first directory in the search
	 * path specified by the <code>jc.object.path</code> system property.
	 */
    public static File objectFile(String className) {
        return objectFile(className, new File(objectDir));
    }

    /**
	 * Return the file that contains the ELF object for the class,
	 * given that <code>dir</code> is the root of the object directory
	 * hierarchy.
	 *
	 * <p>
	 * This method does not depend on the <code>jc.object.path</code>
	 * and therefore may be used when running under other Java VM's.
	 */
    public static File objectFile(String className, File dir) {
        return new File(dir, encode(className, true).replace('/', FILE_SEPARATOR) + ".o");
    }

    /**
	 * Return the "JC hash" of some bytes. This is defined to be
	 * the last 8 bytes of the MD5 of the input, interpreted as
	 * a big-endian Java long value.
	 */
    public static long hash(InputStream s) {
        try {
            DigestInputStream dis = new DigestInputStream(s, MessageDigest.getInstance("MD5"));
            byte[] buf = new byte[100];
            while (dis.read(buf, 0, buf.length) != -1) ;
            byte[] hash = dis.getMessageDigest().digest();
            return ((long) (hash[hash.length - 8] & 0xff) << 56) | ((long) (hash[hash.length - 7] & 0xff) << 48) | ((long) (hash[hash.length - 6] & 0xff) << 40) | ((long) (hash[hash.length - 5] & 0xff) << 32) | ((long) (hash[hash.length - 4] & 0xff) << 24) | ((long) (hash[hash.length - 3] & 0xff) << 16) | ((long) (hash[hash.length - 2] & 0xff) << 8) | ((long) (hash[hash.length - 1] & 0xff));
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
	 * Equivalent to <code>encode(name, false)</code>.
	 */
    public static String encode(String name) {
        return encode(name, false);
    }

    /**
	 * Encode a name so that it uses only "safe" 7-bit characters
	 * so that it's suitable for use as a file name. First the string
	 * is UTF-8 encoded, then slashes become underscores and all other
	 * non alphanumeric characters (UTF-8 bytes really) become two
	 * underscores followed by two hex digits.
	 *
	 * @param name Class name
	 * @param ignoreSlashes whether to pass slash characters through
	 *	unencoded or convert them to underscores.
	 */
    public static String encode(String name, boolean ignoreSlashes) {
        StringBuffer b = new StringBuffer(name.length() + 15);
        byte[] utf = Util.utf8Encode(name);
        for (int i = 0; i < utf.length; i++) {
            int ch = utf[i] & 0xff;
            if (ch == '/' && ignoreSlashes) {
                b.append((char) ch);
                continue;
            }
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                b.append((char) ch);
                continue;
            }
            if (ch == '.') {
                b.append('_');
                continue;
            }
            b.append("__");
            b.append(Integer.toHexString(ch >> 4));
            b.append(Integer.toHexString(ch & 0x0f));
        }
        return b.toString();
    }

    /**
	 * Reverse of {@link #encode}.
	 *
	 * @throws IllegalArgumentException
	 *	if <code>s</code> is not validly encoded.
	 */
    public static String decode(String s) {
        byte[] buf = new byte[s.length()];
        int blen = 0;
        for (int i = 0; i < s.length(); ) {
            char ch = s.charAt(i++);
            int val;
            int j;
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '/') {
                buf[blen++] = (byte) ch;
                continue;
            }
            if (ch != '_') throw new IllegalArgumentException(s);
            int n1, n2;
            if (i + 2 < s.length() && s.charAt(i) == '_' && (n1 = Character.digit(s.charAt(i + 1), 16)) != -1 && (n2 = Character.digit(s.charAt(i + 2), 16)) != -1) {
                i += 3;
                buf[blen++] = (byte) ((n1 << 4) | n2);
                continue;
            }
            buf[blen++] = (byte) '/';
        }
        try {
            return new String(buf, 0, blen, "UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }
}
