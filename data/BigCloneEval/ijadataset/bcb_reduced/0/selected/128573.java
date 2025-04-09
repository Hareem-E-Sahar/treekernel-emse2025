package edu.rice.cs.cunit.util;

import junit.framework.TestCase;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * File Operations.
 *
 * @author Mathias Ricken
 */
public class FileOps {

    /**
     * Makes a file equivalent to the given file f that is relative to base file b.  In other words, <code>new
     * File(b,makeRelativeTo(base,abs)).getCanonicalPath()</code> equals <code>f.getCanonicalPath()</code><p> In
     * Linux/Unix, if the file f is <code>/home/username/folder/file.java</code> and the file b is
     * <code>/home/username/folder/sublevel/file2.java</code>, then the resulting File path from this method would
     * be <code>../file.java</code> while its canoncial path would be <code>/home/username/folder/file.java</code>.</p>
     * In Windows, a file will be made absolute if it is on a different drive than the base.
     *
     * @param f The path that is to be made relative to the base file
     * @param b The file to make the next file relative to
     *
     * @return A new file whose path is relative to the base file while the value of <code>getCanonicalPath()</code>
     *         for the returned file is the same as the result of <code>getCanonicalPath()</code> for the given
     *         file.
     *
     * @throws IOException if an I/O error occurs; may happen since getCanonicalFile uses the file system
     */
    public static File makeRelativeTo(File f, File b) throws IOException {
        File base = b.getCanonicalFile();
        File abs = f.getCanonicalFile();
        if (!base.isDirectory()) {
            base = base.getParentFile();
        }
        File[] roots = File.listRoots();
        for (File r : roots) {
            if (abs.getAbsolutePath().startsWith(r.toString())) {
                if (!base.getAbsolutePath().startsWith(r.toString())) {
                    return abs;
                }
                break;
            }
        }
        String last = "";
        if (!abs.isDirectory()) {
            String tmp = abs.getPath();
            last = tmp.substring(tmp.lastIndexOf(File.separator) + 1);
            abs = abs.getParentFile();
        }
        String[] basParts = splitFile(base);
        String[] absParts = splitFile(abs);
        StringBuffer result = new StringBuffer();
        int diffIndex = -1;
        boolean different = false;
        for (int i = 0; i < basParts.length; i++) {
            if (!different && ((i >= absParts.length) || !basParts[i].equals(absParts[i]))) {
                different = true;
                diffIndex = i;
            }
            if (different) {
                result.append("..").append(File.separator);
            }
        }
        if (diffIndex < 0) {
            diffIndex = basParts.length;
        }
        for (int i = diffIndex; i < absParts.length; i++) {
            result.append(absParts[i]).append(File.separator);
        }
        result.append(last);
        return new File(result.toString());
    }

    /**
     * Returns true if the file f is contained in the directory dir or its subdirectories.
     * @param f the file
     * @param dir the directory
     * @return true if file is contained in directory or one of its subdirectories
     */
    public static boolean isContainedIn(File f, File dir) {
        try {
            return isContainedInCanonical(f.getCanonicalFile(), dir.getCanonicalFile());
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Returns true if the file f is contained in the directory dir or its subdirectories, or if
     * the file *is* the directory.
     * Both the file and the directory must be canonical already.
     * @param f the canonical file
     * @param dir the canonical directory
     * @return true if file is contained in directory or one of its subdirectories or the file *is* the directory
     */
    public static boolean isContainedInCanonical(File f, File dir) {
        if ((dir == null) || (f == null)) {
            return false;
        }
        if (f.equals(dir)) {
            return true;
        }
        File parent = f.getParentFile();
        return isContainedInCanonical(parent, dir);
    }

    /**
     * Splits a file into an array of strings representing each parent folder of the given file.  The file whose
     * path is <code>/home/username/txt.txt</code> in linux would be split into the string array:
     * {&quot;&quot;,&quot;home&quot;,&quot;username&quot;,&quot;txt.txt&quot;}. Delimeters are excluded.
     *
     * @param fileToSplit the file to split into its directories.
     * @return array of path element names
     */
    public static String[] splitFile(File fileToSplit) {
        String path = fileToSplit.getPath();
        ArrayList<String> list = new ArrayList<String>();
        while (!path.equals("")) {
            int idx = path.indexOf(File.separator);
            if (idx < 0) {
                list.add(path);
                path = "";
            } else {
                list.add(path.substring(0, idx));
                path = path.substring(idx + 1);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Splits a string with a list of paths, separated by pathSeparator, into an array of paths.
     * This will correctly split a path even if it contains a drive letter, but only if
     * File.pathSeparator ('/' or '\\') follows the drive letter.
     * Examples (assuming the File.separatorChar is '\\'):
     * splitPaths("C:\\foo:C:\\bar", ':', true) will be split into {"C:\\foo", "C:\\bar"}.
     * splitPaths("C:foo:C:\\bar", ':', true) will be split into {"C", "foo", "C:\\bar"}.
     *
     * @param pathString the string with the list of paths, separated by pathSeparator
     * @param pathSeparator the character separating the paths (should be either ':' or ';')
     * @return array of paths
     */
    public static String[] splitPaths(String pathString, char pathSeparator) {
        return splitPaths(pathString, pathSeparator, true);
    }

    /**
     * Splits a string with a list of paths, separated by pathSeparator, into an array of paths.
     * If winDriveLetters is true, this will correctly split a path even if it contains
     * a drive letter, but only if File.pathSeparator ('/' or '\\') follows the drive letter.
     * Examples (assuming the File.separatorChar is '\\'):
     * splitPaths("C:\\foo:C:\\bar", ':', true) will be split into {"C:\\foo", "C:\\bar"}.
     * splitPaths("C:foo:C:\\bar", ':', true) will be split into {"C", "foo", "C:\\bar"}.
     *
     * @param pathString the string with the list of paths, separated by pathSeparator
     * @param pathSeparator the character separating the paths (should be either ':' or ';')
     * @param winDriveLetters treat "C:" followed by separatorChar as drive letter
     * @return array of paths
     */
    public static String[] splitPaths(String pathString, char pathSeparator, boolean winDriveLetters) {
        boolean checkDriveLetters = (pathSeparator == ':') && winDriveLetters;
        ArrayList<String> list = new ArrayList<String>();
        int idx = 0;
        while ((idx = pathString.indexOf(pathSeparator, idx)) >= 0) {
            String path = pathString.substring(0, idx);
            if (checkDriveLetters && ((path.length() == 1) && (pathString.charAt(idx + 1) == File.separatorChar))) {
                ++idx;
                continue;
            }
            list.add(path);
            pathString = pathString.substring(idx + 1);
            idx = 0;
        }
        list.add(pathString);
        return list.toArray(new String[list.size()]);
    }

    /**
     * Unequivocally exits a program, if necessary by using Runtime.halt and not executing ShutdownHooks.
     * @param status program's exit status
     */
    public static void exit(final int status) {
        Thread terminator = new Thread(new Runnable() {

            public void run() {
                System.exit(status);
            }
        }, "Attempt System.exit");
        terminator.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        Runtime.getRuntime().halt(status);
    }

    /**
     * Create a new temporary directory. The directory will be deleted on exit, if empty. (To delete it recursively on
     * exit, use deleteDirectoryOnExit.)
     *
     * @param name Non-unique portion of the name of the directory to create.
     *
     * @return File representing the directory that was created.
     * @throws IOException
     */
    public static File createTempDirectory(final String name) throws IOException {
        return createTempDirectory(name, null);
    }

    /**
     * Create a new temporary directory. The directory will be deleted on exit, if it only contains temp files and temp
     * directories created after it.  (To delete it on exit regardless of contents, call deleteDirectoryOnExit after
     * constructing the file tree rooted at this directory.  Note that createTempDirectory(..) is not much more helpful
     * than mkdir() in this context (other than generating a new temp file name) because cleanup is a manual process.)
     *
     * @param name   Non-unique portion of the name of the directory to create.
     * @param parent Parent directory to contain the new directory
     *
     * @return File representing the directory that was created.
     * @throws IOException
     */
    public static File createTempDirectory(final String name, final File parent) throws IOException {
        File file = File.createTempFile(name, "", parent);
        file.delete();
        file.mkdir();
        file.deleteOnExit();
        return file;
    }

    /**
     * Delete the given directory including any files and directories it contains.
     *
     * @param dir File object representing directory to delete. If, for some reason, this file object is not a
     *            directory, it will still be deleted.
     *
     * @return true if there were no problems in deleting. If it returns false, something failed and the directory
     *         contents likely at least partially still exist.
     */
    public static boolean deleteDirectory(final File dir) {
        if (!dir.isDirectory()) {
            boolean res;
            res = dir.delete();
            if (!res) {
                System.err.println("Could not delete " + dir);
            }
            return res;
        }
        boolean ret = true;
        File[] childFiles = dir.listFiles();
        if (childFiles != null) {
            for (File f : childFiles) {
                ret = ret && deleteDirectory(f);
            }
        }
        ret = ret && dir.delete();
        return ret;
    }

    /**
     * Instructs Java to recursively delete the given directory and its contents when the JVM exits.
     *
     * @param dir File object representing directory to delete. If, for some reason, this file object is not a
     *            directory, it will still be deleted.
     */
    public static void deleteDirectoryOnExit(final File dir) {
        dir.deleteOnExit();
        if (dir.isDirectory()) {
            File[] childFiles = dir.listFiles();
            if (childFiles != null) {
                for (File f : childFiles) {
                    deleteDirectoryOnExit(f);
                }
            }
        }
    }

    /**
     * Copy the the file or directory src to dst.
     * @param src source file
     * @param dst destination
     * @throws IOException
     */
    public static void copyFile(final File src, final File dst) throws IOException {
        if (src.isFile()) {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } else if (src.isDirectory()) {
            dst.mkdirs();
            for (File f : src.listFiles()) {
                copyFile(f, new File(dst, f.getName()));
            }
        }
    }

    /**
     * Enumerate the files in src.
     * @param src source file
     * @return list of files in src
     * @throws IOException
     */
    public static Set<File> enumFiles(final File src) throws IOException {
        Set<File> l = new HashSet<File>();
        if (src.isFile()) {
            l.add(src);
        } else if (src.isDirectory()) {
            for (File f : src.listFiles()) {
                Set<File> recur = enumFiles(f);
                l.addAll(recur);
            }
        }
        return l;
    }

    /** Convert all path entries in a path string to absolute paths. The delimiter in the path string is the
     *  "path.separator" property.  Empty entries are equivalent to "." and will thus are converted to the
     *  "user.dir" (working directory).
     *  Example:
     *    ".:drjava::/home/foo/junit.jar" with "user.dir" set to "/home/foo/bar" will be converted to
     *    "/home/foo/bar:/home/foo/bar/drjava:/home/foo/bar:/home/foo/junit.jar".
     *
     *  @param path path string with entries to convert
     *  @return path string with all entries as absolute paths
     */
    public static String convertToAbsolutePathEntries(String path) {
        String pathSep = System.getProperty("path.separator");
        path += pathSep + "x";
        String[] pathEntries = path.split(pathSep);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathEntries.length - 1; ++i) {
            File f = new File(pathEntries[i]);
            sb.append(f.getAbsolutePath());
            sb.append(pathSep);
        }
        String reconstructedPath = sb.toString();
        if (reconstructedPath.length() != 0) {
            reconstructedPath = reconstructedPath.substring(0, reconstructedPath.length() - 1);
        }
        return reconstructedPath;
    }

    /**
     * Unpack all files in the jar file to the specified directory.
     *
     * @param inJar  the input Jar
     * @param outDir the output directory
     * @return number of files unpacked
     * @throws IOException
     */
    public static long unpackJar(File inJar, File outDir) throws IOException {
        JarFile jf = null;
        long count = 0;
        try {
            jf = new JarFile(inJar);
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                if (!e.isDirectory()) {
                    InputStream is = null;
                    try {
                        is = jf.getInputStream(e);
                        FileOutputStream fos = null;
                        try {
                            String name = e.getName().replace('/', File.separatorChar);
                            int pos = name.lastIndexOf(File.separatorChar);
                            if (pos >= 0) {
                                String dir = name.substring(0, pos);
                                (new File(outDir, dir)).mkdirs();
                            }
                            fos = new FileOutputStream(new File(outDir, name));
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while (-1 != (bytesRead = is.read(buffer))) {
                                fos.write(buffer, 0, bytesRead);
                            }
                            ++count;
                        } finally {
                            if (fos != null) {
                                fos.close();
                                fos = null;
                            }
                        }
                    } finally {
                        if (is != null) {
                            is.close();
                            is = null;
                        }
                    }
                }
            }
        } finally {
            if (jf != null) {
                jf.close();
                jf = null;
            }
        }
        return count;
    }

    /**
     * Pack all files in the directory into the specified Jar.
     *
     * @param inDir  the input directory
     * @param outJar the output Jar
     * @return number of files packed
     * @throws IOException
     */
    public static long packJar(File inDir, File outJar) throws IOException {
        FileOutputStream fos = new FileOutputStream(outJar);
        JarOutputStream jos = new JarOutputStream(fos);
        long count = 0;
        Set<File> files = enumFiles(inDir);
        for (File f : files) {
            File fRel = makeRelativeTo(f, inDir);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                jos.putNextEntry(new ZipEntry(fRel.getPath().replace(File.separatorChar, '/')));
                byte[] buffer = new byte[1024];
                int bytesRead;
                while (-1 != (bytesRead = fis.read(buffer))) {
                    jos.write(buffer, 0, bytesRead);
                }
                ++count;
            } finally {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            }
        }
        jos.close();
        fos.close();
        return count;
    }

    public static class FileOpsTest extends TestCase {

        public void testSplitPaths() {
            String[] p;
            final String fs = File.separator;
            p = splitPaths("C:" + fs + "foo:C:" + fs + "bar::C:" + fs + "foo" + fs + "bar:", ':', true);
            assertEquals(5, p.length);
            assertEquals("C:" + fs + "foo", p[0]);
            assertEquals("C:" + fs + "bar", p[1]);
            assertEquals("", p[2]);
            assertEquals("C:" + fs + "foo" + fs + "bar", p[3]);
            assertEquals("", p[4]);
            p = splitPaths("C:foo:C:bar::C:foo" + fs + "bar:", ':', true);
            assertEquals(8, p.length);
            assertEquals("C", p[0]);
            assertEquals("foo", p[1]);
            assertEquals("C", p[2]);
            assertEquals("bar", p[3]);
            assertEquals("", p[4]);
            assertEquals("C", p[5]);
            assertEquals("foo" + fs + "bar", p[6]);
            assertEquals("", p[7]);
            p = splitPaths("", ':', true);
            assertEquals(1, p.length);
            assertEquals("", p[0]);
            p = splitPaths("C:" + fs + "foo;C:" + fs + "bar;;C:" + fs + "foo" + fs + "bar;", ';', true);
            assertEquals(5, p.length);
            assertEquals("C:" + fs + "foo", p[0]);
            assertEquals("C:" + fs + "bar", p[1]);
            assertEquals("", p[2]);
            assertEquals("C:" + fs + "foo" + fs + "bar", p[3]);
            assertEquals("", p[4]);
            p = splitPaths("C:foo;C:bar;;C:foo" + fs + "bar;", ';', true);
            assertEquals(5, p.length);
            assertEquals("C:foo", p[0]);
            assertEquals("C:bar", p[1]);
            assertEquals("", p[2]);
            assertEquals("C:foo" + fs + "bar", p[3]);
            assertEquals("", p[4]);
            p = splitPaths("", ':', true);
            assertEquals(1, p.length);
            assertEquals("", p[0]);
            p = splitPaths("C;foo;C;bar;;C;foo/bar;", ';', true);
            assertEquals(8, p.length);
            assertEquals("C", p[0]);
            assertEquals("foo", p[1]);
            assertEquals("C", p[2]);
            assertEquals("bar", p[3]);
            assertEquals("", p[4]);
            assertEquals("C", p[5]);
            assertEquals("foo/bar", p[6]);
            assertEquals("", p[7]);
            p = splitPaths("C:\\foo:C:\\bar::C:\\foo\\bar:", ':', false);
            assertEquals(8, p.length);
            assertEquals("C", p[0]);
            assertEquals("\\foo", p[1]);
            assertEquals("C", p[2]);
            assertEquals("\\bar", p[3]);
            assertEquals("", p[4]);
            assertEquals("C", p[5]);
            assertEquals("\\foo\\bar", p[6]);
            assertEquals("", p[7]);
            p = splitPaths("C:foo:C:bar::C:foo\\bar:", ':', false);
            assertEquals(8, p.length);
            assertEquals("C", p[0]);
            assertEquals("foo", p[1]);
            assertEquals("C", p[2]);
            assertEquals("bar", p[3]);
            assertEquals("", p[4]);
            assertEquals("C", p[5]);
            assertEquals("foo\\bar", p[6]);
            assertEquals("", p[7]);
            p = splitPaths("", ':', true);
            assertEquals(1, p.length);
            assertEquals("", p[0]);
            p = splitPaths("C:/foo:C:/bar::C:/foo/bar:", ':', false);
            assertEquals(8, p.length);
            assertEquals("C", p[0]);
            assertEquals("/foo", p[1]);
            assertEquals("C", p[2]);
            assertEquals("/bar", p[3]);
            assertEquals("", p[4]);
            assertEquals("C", p[5]);
            assertEquals("/foo/bar", p[6]);
            assertEquals("", p[7]);
            p = splitPaths("C:foo:C:bar::C:foo/bar:", ':', false);
            assertEquals(8, p.length);
            assertEquals("C", p[0]);
            assertEquals("foo", p[1]);
            assertEquals("C", p[2]);
            assertEquals("bar", p[3]);
            assertEquals("", p[4]);
            assertEquals("C", p[5]);
            assertEquals("foo/bar", p[6]);
            assertEquals("", p[7]);
            p = splitPaths("C:\\foo;C:\\bar;;C:\\foo\\bar;", ';', false);
            assertEquals(5, p.length);
            assertEquals("C:\\foo", p[0]);
            assertEquals("C:\\bar", p[1]);
            assertEquals("", p[2]);
            assertEquals("C:\\foo\\bar", p[3]);
            assertEquals("", p[4]);
            p = splitPaths("C:foo;C:bar;;C:foo\\bar;", ';', false);
            assertEquals(5, p.length);
            assertEquals("C:foo", p[0]);
            assertEquals("C:bar", p[1]);
            assertEquals("", p[2]);
            assertEquals("C:foo\\bar", p[3]);
            assertEquals("", p[4]);
            p = splitPaths("", ':', true);
            assertEquals(1, p.length);
            assertEquals("", p[0]);
            p = splitPaths("C:/foo;C:/bar;;C:/foo/bar;", ';', false);
            assertEquals(5, p.length);
            assertEquals("C:/foo", p[0]);
            assertEquals("C:/bar", p[1]);
            assertEquals("", p[2]);
            assertEquals("C:/foo/bar", p[3]);
            assertEquals("", p[4]);
            p = splitPaths("C;foo;C;bar;;C;foo/bar;", ';', false);
            assertEquals(8, p.length);
            assertEquals("C", p[0]);
            assertEquals("foo", p[1]);
            assertEquals("C", p[2]);
            assertEquals("bar", p[3]);
            assertEquals("", p[4]);
            assertEquals("C", p[5]);
            assertEquals("foo/bar", p[6]);
            assertEquals("", p[7]);
        }
    }
}
