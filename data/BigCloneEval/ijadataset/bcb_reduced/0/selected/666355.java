package ao.util.io;

import ao.util.data.Arrs;
import ao.util.data.Colls;
import ao.util.data.iter.CloseableIterable;
import ao.util.misc.AoThrowables;
import ao.util.pass.DataSink;
import org.apache.log4j.Logger;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

/**
 * see http://www.asciitable.com/
 *
 * AoFiles as opposed to Files to avoid name clash with google guava.
 *
 *
 * @author aostrovsky
 */
public class AoFiles {

    private static Logger LOG = Logger.getLogger(AoFiles.class);

    private static final char[] escapeChars = "\\/:*?\"<>|".toCharArray();

    private static final char allowFrom = ' ';

    private static final char allowTill = '~';

    private static final String defaultEscape = "_";

    private AoFiles() {
    }

    public static void main(String[] args) {
        System.out.println(escapeName("/\\,:?\">><<|||@!#D"));
        System.out.println(name("hello/world/foo.bar"));
        System.out.println(path("a/b/c.bar"));
        System.out.println(path("x\\y\\z.bar"));
    }

    public static CloseableIterable<String> readLines(String file) {
        return readLines(new File(file));
    }

    private static CloseableIterable<String> readLines(File file) {
        if (file == null || !file.canRead()) {
            return null;
        }
        return Streams.readLines(AoFiles.in(file));
    }

    public static boolean delete(String fileOrDirectory) {
        return fileOrDirectory != null && delete(new File(fileOrDirectory));
    }

    public static boolean delete(File fileOrDirectory) {
        if (fileOrDirectory == null || !fileOrDirectory.exists()) {
            return false;
        }
        if (fileOrDirectory.isFile()) {
            return deleteFile(fileOrDirectory);
        }
        for (File sub : fileOrDirectory.listFiles()) {
            if (!delete(sub)) {
                return false;
            }
        }
        return true;
    }

    private static boolean deleteFile(File file) {
        if (!file.delete()) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            return file.delete();
        }
        return true;
    }

    public static File resource(String rootPath) {
        String asRoot = (rootPath.startsWith("/") ? rootPath : "/" + rootPath);
        return resource(Void.class, asRoot);
    }

    public static File resource(Class<?> relativeTo, String path) {
        URL url = relativeTo.getResource(path);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }

    public static String nameWithoutExtension(File file) {
        return nameWithoutExtension(file.toString());
    }

    public static String nameWithoutExtension(String filePath) {
        String name = name(filePath);
        int dotIndex = name.lastIndexOf('.');
        return dotIndex == -1 ? name : name.substring(0, dotIndex);
    }

    public static String name(String filePath) {
        return new File(filePath).getName();
    }

    public static String path(String ofFile) {
        int lastSlash = lastDirectorySeparator(ofFile);
        if (lastSlash == -1) {
            return "";
        } else {
            return ofFile.substring(0, lastSlash);
        }
    }

    private static int lastDirectorySeparator(String ofPath) {
        int lastDir = ofPath.lastIndexOf("/");
        return (lastDir == -1) ? ofPath.lastIndexOf("\\") : lastDir;
    }

    public static String escapePath(String filePath) {
        return escapePath(filePath, defaultEscape);
    }

    public static File escapePath(File filePath) {
        return escapePath(filePath, defaultEscape);
    }

    public static String escapePath(String filePath, String escape) {
        return escapePath(new File(filePath), escape).toString();
    }

    public static File escapePath(File filePath, String escape) {
        LinkedList<String> parts = new LinkedList<String>();
        for (File cursor = filePath; cursor != null; cursor = cursor.getParentFile()) {
            parts.addFirst(escapeName(filePath.getName(), escape));
        }
        return new File(Colls.join(parts, escape));
    }

    public static String escapeName(String fileName) {
        return escapeName(fileName, defaultEscape);
    }

    public static String escapeName(String fileName, String escape) {
        StringBuilder str = new StringBuilder();
        for (char c : fileName.toCharArray()) {
            if (c < allowFrom || c > allowTill || Arrs.indexOf(escapeChars, c) != -1) {
                str.append(escape);
            } else {
                str.append(c);
            }
        }
        return str.toString();
    }

    public static boolean copy(String in, String out) {
        return copy(new File(in), new File(out));
    }

    public static boolean copy(File in, String out) {
        return copy(in, new File(out));
    }

    public static boolean copy(String in, File out) {
        return copy(new File(in), out);
    }

    /**
	 * Compy contents of file file overwriting another.
	 * 
	 * Taken from
	 * 	http://www.rgagnon.com/javadetails/java-0064.html
	 * 
	 * In Java 7 there is a native mechanism for doing file copies,
	 * 	but I can't use that for now.
     * 
     * @param in source of copy
     * @param out destination of copy
     * @return true if and only if the file was successfully copied
     */
    public static boolean copy(File in, File out) {
        try {
            doCopy(in, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void doCopy(File in, File out) throws Exception {
        InputStream fis = new BufferedInputStream(new FileInputStream(in));
        Dirs.pathTo(out);
        OutputStream fos = new BufferedOutputStream(new FileOutputStream(out));
        Streams.copyAndClose(fis, fos);
    }

    public static void read(String inputFile, DataSink<BufferedReader> processor) {
        read(new File(inputFile), processor);
    }

    public static BufferedReader read(String inputFile) {
        return read(new File(inputFile));
    }

    public static void read(File inputFile, DataSink<BufferedReader> processor) {
        BufferedReader in = read(inputFile);
        try {
            processor.process(in);
        } catch (Throwable t) {
            throw AoThrowables.wrap(t);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LOG.error("Unable to close file: " + inputFile, e);
            }
        }
    }

    public static BufferedReader read(File inputFile) {
        return new BufferedReader(new InputStreamReader(in(inputFile)));
    }

    public static void write(String outFile, DataSink<PrintWriter> processor) {
        write(outFile, false, processor);
    }

    public static PrintWriter write(String outFile) {
        return write(new File(outFile));
    }

    public static void write(File outFile, DataSink<PrintWriter> processor) {
        write(outFile, false, processor);
    }

    public static PrintWriter write(File outFile) {
        return write(outFile, false);
    }

    public static void write(String outFile, boolean append, DataSink<PrintWriter> processor) {
        write(new File(outFile), append, processor);
    }

    public static PrintWriter write(String outFile, boolean append) {
        return write(new File(outFile), append);
    }

    public static void write(File outFile, boolean append, DataSink<PrintWriter> processor) {
        PrintWriter out = write(outFile, append);
        try {
            processor.process(out);
        } catch (Exception e) {
            throw AoThrowables.wrap(e);
        } finally {
            out.close();
            if (out.checkError()) {
                LOG.warn("Issue while writing to: " + outFile);
            }
        }
    }

    public static PrintWriter write(File outFile, boolean append) {
        return new PrintWriter(new OutputStreamWriter(out(outFile, append)));
    }

    public static void in(String inputFile, DataSink<InputStream> processor) {
        in(new File(inputFile), processor);
    }

    public static InputStream in(String inputFile) {
        return in(new File(inputFile));
    }

    public static void in(File inputFile, DataSink<InputStream> processor) {
        InputStream in = in(inputFile);
        try {
            processor.process(in);
        } catch (Throwable t) {
            throw AoThrowables.wrap(t);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LOG.error("Unable to close file: " + inputFile, e);
            }
        }
    }

    public static InputStream in(File inputFile) {
        try {
            return new BufferedInputStream(new FileInputStream(inputFile));
        } catch (FileNotFoundException e) {
            throw AoThrowables.wrap(e);
        }
    }

    public static void out(String outFile, DataSink<OutputStream> processor) {
        out(outFile, false, processor);
    }

    public static OutputStream out(String outFile) {
        return out(new File(outFile));
    }

    public static void out(File outFile, DataSink<OutputStream> processor) {
        out(outFile, false, processor);
    }

    public static OutputStream out(File outFile) {
        return out(outFile, false);
    }

    public static void out(String outFile, boolean append, DataSink<OutputStream> processor) {
        out(new File(outFile), append, processor);
    }

    public static OutputStream out(String outFile, boolean append) {
        return out(new File(outFile), append);
    }

    public static void out(File outFile, boolean append, DataSink<OutputStream> processor) {
        OutputStream out = out(outFile, append);
        try {
            processor.process(out);
        } catch (Exception e) {
            throw AoThrowables.wrap(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                LOG.warn("Can't close file: " + outFile, e);
            }
        }
    }

    public static OutputStream out(File outFile, boolean append) {
        try {
            return new BufferedOutputStream(new FileOutputStream(new File(Dirs.pathTo(outFile), outFile.getName()), append));
        } catch (FileNotFoundException e) {
            throw AoThrowables.wrap(e);
        }
    }

    public static FilterOutputStream newOuputFilter(Class<? extends FilterOutputStream> filter, OutputStream delegate) {
        try {
            return filter.getConstructor(OutputStream.class).newInstance(delegate);
        } catch (Exception e) {
            throw AoThrowables.wrap(e);
        }
    }

    public static FilterInputStream newInputFilter(Class<? extends FilterInputStream> filter, InputStream delegate) {
        try {
            return filter.getConstructor(InputStream.class).newInstance(delegate);
        } catch (Exception e) {
            throw AoThrowables.wrap(e);
        }
    }
}
