import java.io.*;
import java.util.*;

/**
 * This program compiles all of the classes in the JAS tree appropriately.
 * <ul> directives::
 * <li> all
 * <li> core
 * <li> agent
 * <li> grammars
 * <li> classes
 * <li> clean
 * <li> docs
 * <li> jar
 * <li> help
 * </ul>
 *
 * <ul> verbosity options:
 * <li> noslash
 * <li> silent
 * <li> quiet
 * <li> verbose
 * <li> stderr
 * </ul>
 *
 * Usage:<br>
 * <pre>
 * java MakeJAS [verbosity] [directives] <directory>*
 * </pre>
 * <p>
 * 
 *
 * This class is designed to <b>ONLY</b> compile the JAS tree, and encodes
 * significant intelligence related to this task. 
 *
 * @author A. Spydell
 * @author <br> S. Stremler
 * @since 1.0
 */
public class MakeJAS implements Runnable {

    private boolean clean;

    private boolean grammar;

    private boolean classes;

    private boolean document;

    private boolean jar;

    private List list;

    private BufferedReader inOut;

    private BufferedReader inErr;

    private Thread errThread;

    private Thread outThread;

    private Object errLock;

    private Object outLock;

    private String message;

    private Spinner spinner;

    private static final File JAS_JAR = new File("jas.jar");

    private static final File JAS_ROOT = new File("..");

    private static final File JAS_SRC = new File(JAS_ROOT, "src");

    private static final File JAS_API = new File(JAS_SRC, "javax");

    private static final File JAS_SPI = new File(JAS_SRC, "org");

    private static final String JARLIST_NAME = ".jars";

    private static final String JAVADOC_NAME = ".jdoc";

    private static final String MANIFESTNAME = ".manifest";

    private static final String USER_DIR_KEY = "user.dir";

    private static final String ERR_START_MSG = "Invalid start directory";

    private static final String START_DIR = "jas" + File.separator + "bin";

    private static final String[] JAR_SUFFIXES = { ".class", ".config" };

    private int verbosity = OUTPUT_DEFAULT;

    private static final int OUTPUT_SILENT = 0;

    private static final int OUTPUT_QUIET = 1;

    private static final int OUTPUT_DEFAULT = 2;

    private static final int OUTPUT_VERBOSE = 4;

    private static final int OUTPUT_STDERR = 8;

    private MakeJAS(String[] args) throws Exception {
        String usrDir = System.getProperties().getProperty(USER_DIR_KEY);
        if (!usrDir.endsWith(START_DIR)) throw new Exception(ERR_START_MSG + ": " + usrDir);
        errLock = new Object();
        outLock = new Object();
        errThread = new Thread(this, "stderr");
        outThread = new Thread(this, "stdout");
        spinner = new Spinner();
        list = new ArrayList();
        Hashtable params = new Hashtable();
        for (int i = 0, n = args.length; i < n; i++) {
            if (isValidDirectory(args[i])) {
                list.add(new File(args[i]));
            } else {
                if (args[i].startsWith("-")) args[i] = args[i].substring(1);
                params.put(args[i], "");
            }
        }
        if (list.size() == 0) list.add(JAS_SRC);
        if (params.containsKey("help")) {
            System.err.println(usage());
            System.exit(1);
        }
        if (params.containsKey("api") || params.containsKey("spi")) {
            clean = true;
            classes = true;
            list.clear();
            if (params.containsKey("api")) list.add(JAS_API); else list.add(JAS_SPI);
            return;
        }
        if (params.containsKey("noslash")) spinner.showSlash(false);
        if (params.containsKey("all")) {
            clean = true;
            grammar = true;
            classes = true;
            document = true;
            jar = true;
            list.clear();
            list.add(JAS_SRC);
            return;
        }
        if (params.containsKey("docs")) document = true;
        if (params.containsKey("jar")) jar = true;
        if (params.containsKey("clean")) clean = true;
        if (params.containsKey("classes")) classes = true;
        if (params.containsKey("grammars")) grammar = true;
        if (params.containsKey("stderr")) {
            verbosity = OUTPUT_STDERR;
            spinner.showSlash(false);
        }
        if (params.containsKey("verbose")) {
            verbosity = OUTPUT_VERBOSE;
            spinner.showSlash(false);
        }
        if (params.containsKey("quiet")) {
            verbosity = OUTPUT_QUIET;
            spinner.showSlash(false);
        }
        if (params.containsKey("silent")) {
            verbosity = OUTPUT_SILENT;
            spinner.showSlash(false);
        }
        if (!clean && !grammar && !classes && !document && !jar) {
            clean = true;
            classes = true;
        }
    }

    public int build() throws Exception {
        int buildStatus = 0;
        errThread.start();
        outThread.start();
        File accumulator[][] = new File[list.size()][];
        File start_dir = null;
        messageInform("\n" + configuration() + "\n");
        for (int i = 0; i < list.size(); i++) {
            start_dir = (File) list.get(i);
            if (clean) {
                messageStart("Deleting (*.class) files....");
                clean(find(start_dir, ".class"));
                messageStop("done!");
            }
            if (grammar) {
                messageStart("Performing javacc....");
                int status = javacc(find(start_dir, ".jj"));
                messageStop("done!");
                if (status > 0) {
                    messageInform("\nJavaCC FAILED " + status + " times.");
                    buildStatus++;
                }
            }
            if (classes) {
                messageStart("Performing javac....");
                int status = javac(find(start_dir, ".java"));
                messageStop("done!");
                if (status > 0) {
                    messageInform("\njavac FAILED " + status + " times.");
                    buildStatus++;
                }
            }
            if (document) accumulator[i] = find(start_dir, ".java");
        }
        if (document) {
            messageStart("Creating javadoc....");
            int status = javadoc(accumulator);
            if (status >= 0) {
                messageStop("done!");
                buildStatus++;
            } else {
                messageStop("failed!");
            }
        }
        if (jar) {
            messageStart("Creating jar file....");
            int status = makejar();
            if (status >= 0) {
                messageStop("done!");
                buildStatus++;
            } else {
                messageStop("failed!");
            }
        }
        System.err.flush();
        System.out.flush();
        if (inErr != null) inErr.close();
        if (inOut != null) inOut.close();
        errThread.interrupt();
        outThread.interrupt();
        return buildStatus;
    }

    public String configuration() {
        return "Build options: " + (clean ? "clean " : "") + (grammar ? "grammar " : "") + (classes ? "classes " : "") + (document ? "document " : "") + (jar ? "jar" : "");
    }

    private static final String HELP_MSG = "\nUsage:\n" + "   java MakeJAS [verbosity] [directives] [directory]*\n\n" + "   Verbosity Options:\n" + "      noslash - suppresses spinner output.\n" + "      silent  - silent mode, outputs nothing.\n" + "      quiet   - quiet mode, outputs almost nothing.\n" + "      verbose - verbose mode, outputs everything.\n" + "      stderr  - standard error mode, outputs only errors.\n\n" + "   Directive Options:\n" + "      all     - makes classes and documents.\n" + "      clean   - deletes all class files.\n" + "      grammar - generates the grammar files.\n" + "      classes - recompile the *.java files.\n" + "      agent   - clean and recompile the agent subtree.\n" + "      core    - clean and recompile the core subtree.\n" + "      docs    - makes the javadoc for TIIERA.\n" + "      jar     - make a jar file.\n" + "      help    - prints this message.\n";

    public static final String usage() {
        return HELP_MSG;
    }

    private int makejar() {
        int status = -1;
        if (JAS_JAR.exists()) {
            messageInform("Warning: " + JAS_JAR.getName() + " will be replaced.");
        }
        List jarlist = list;
        if (jarlist.size() == 1 && jarlist.get(0).equals(JAS_SRC)) {
            jarlist = new ArrayList();
            jarlist.add(JAS_API);
            jarlist.add(JAS_SPI);
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < jarlist.size(); i++) {
            File start_dir = (File) jarlist.get(i);
            for (int k = 0; k < JAR_SUFFIXES.length; k++) {
                File classesList[] = find(start_dir, JAR_SUFFIXES[k]);
                String root_prefix = "";
                for (int j = 0; j < classesList.length; j++) {
                    spinner.spin();
                    try {
                        root_prefix = start_dir.getCanonicalFile().getParent();
                        String s = classesList[j].getCanonicalPath();
                        if (!s.startsWith(root_prefix)) {
                            messageError("Root Match Failure!");
                            messageError("Expected '" + s + "' ");
                            messageError("to start wth '" + root_prefix + "'");
                        } else {
                            buffer.append(" -C ");
                            buffer.append(root_prefix);
                            buffer.append(" ");
                            buffer.append(s.substring(root_prefix.length() + 1));
                        }
                    } catch (Exception e) {
                        messageError("Caught exception: " + e);
                    }
                }
            }
        }
        try {
            String options = "cvf";
            String targets = JAS_JAR.getName();
            if ((new File(MANIFESTNAME)).exists()) {
                targets = targets + " " + MANIFESTNAME;
                options = options + "m";
            }
            String cmd = "jar " + options + " " + targets + buffer;
            status = execute(cmd).waitFor();
        } catch (Exception e) {
            messageError("Exec failed: " + e);
        }
        return status;
    }

    private void clean(File[] cls_files) {
        for (int i = 0; i < cls_files.length; i++) delete(cls_files[i]);
    }

    private void jccClean(File[] jj_files) {
        for (int i = 0; i < jj_files.length; i++) {
            File dir = jj_files[i].getParentFile();
            File[] fnames = dir.listFiles();
            for (int j = 0; j < fnames.length; j++) {
                String fname = fnames[j].getName();
                if (fname.equals("ASCII_CharStream.java") || fname.equals("ParseException.java") || fname.endsWith("Parser.java") || fname.endsWith("Constants.java") || fname.endsWith("TokenManager.java") || fname.equals("Token.java") || fname.equals("TokenMgrError.java")) {
                    delete(fnames[j]);
                }
            }
        }
    }

    private void delete(File file) {
        messagePrint("delete " + file);
        file.delete();
    }

    private int javacc(File[] jj_files) throws IOException, InterruptedException {
        String javacc = "java COM.sun.labs.javacc.Main -OUTPUT_DIRECTORY:";
        String jj_file = null, jj_dir = null, jj_cmd = null, jj_msg = null;
        int status = 0;
        for (int i = 0; i < jj_files.length; i++) {
            jj_file = jj_files[i].toString();
            jj_dir = jj_file.substring(0, jj_file.lastIndexOf(File.separator));
            jj_cmd = javacc + jj_dir + " " + jj_file;
            messagePrint("javacc -OUTPUT_DIRECTORY:" + jj_dir + " " + jj_file);
            status += execute(jj_cmd).waitFor();
        }
        return status;
    }

    private int javac(File[] jc_files) throws IOException, InterruptedException {
        File java_file = null, jcls_file = null;
        String jvc_cmd = null, jvc_fl = null, cls_fl = null;
        String jar_list = null, jar_args = null;
        int status = 0;
        for (int i = 0; i < jc_files.length; i++) {
            jvc_fl = jc_files[i].toString();
            cls_fl = jvc_fl.substring(0, jvc_fl.lastIndexOf(".")) + ".class";
            java_file = jc_files[i];
            jcls_file = new File(cls_fl);
            jar_list = getClasspath(java_file.getParentFile());
            jar_args = jar_list == null ? "" : "-classpath " + jar_list;
            if (!jcls_file.exists() || jcls_file.lastModified() < java_file.lastModified()) {
                jvc_cmd = "javac -nowarn " + jar_args + " " + jvc_fl;
                status += execute(jvc_cmd).waitFor();
            }
            spinner.spin();
        }
        return status;
    }

    private String getClasspath(File dir) {
        if (dir == null) return null;
        File jarfile = new File(dir, JARLIST_NAME);
        if (!jarfile.exists()) return null;
        StringBuffer buf = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(jarfile));
            buf.append(getLibPathname(dir, reader.readLine()));
            String line = reader.readLine();
            while (line != null) {
                buf.append(File.pathSeparatorChar);
                buf.append(getLibPathname(dir, line));
                line = reader.readLine();
            }
        } catch (IOException iox) {
            return null;
        }
        String returnString = buf.toString().trim() + File.pathSeparator + System.getProperties().getProperty("java.class.path");
        if (returnString.indexOf(" ") > -1) {
            messageError("Spaces are not allowed in CLASSPATHs: " + returnString);
            throw new RuntimeException("SPACE found in CLASSPATH.");
        }
        return returnString;
    }

    private String getLibPathname(File dir, String name) throws IOException {
        File file = new File(dir, name);
        if (!file.exists()) {
            messageError("Can't find " + file);
        }
        return file.getCanonicalPath();
    }

    private int javadoc(File[][] list) {
        int status = -1;
        try {
            File f = new File(JAVADOC_NAME);
            if (!(f.exists() && f.isFile())) {
                messageError("Unable to find file: " + JAVADOC_NAME);
                messageError("Javadoc creation aborting.");
                return -1;
            }
            String line = "";
            StringBuffer options = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while (line != null) {
                if (!(line.startsWith("#") || line.length() == 0)) {
                    options.append(" ");
                    options.append(line.trim());
                }
                line = reader.readLine();
            }
            reader.close();
            String rootPath = JAS_SRC.getCanonicalFile().toString();
            int len = rootPath.length() + File.separator.length();
            Set set = new TreeSet();
            for (int i = 0; i < list.length; i++) {
                for (int j = 0; j < list[i].length; j++) {
                    String s = list[i][j].getCanonicalFile().getParent();
                    if (s.startsWith(rootPath)) {
                        set.add(s.substring(len).replace(File.separatorChar, '.'));
                    }
                    spinner.spin();
                }
            }
            StringBuffer packages = new StringBuffer();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                packages.append(iterator.next());
                packages.append(" ");
                spinner.spin();
            }
            status = execute("javadoc " + options + " " + packages).waitFor();
        } catch (Exception e) {
            messageError("\nJavadoc creation failed. Aborting.\n" + e);
        }
        return status;
    }

    private static final boolean isValidDirectory(String dir_name) {
        File dir = new File(dir_name);
        if (!dir.exists()) return false;
        if (!dir.isDirectory()) return false;
        return true;
    }

    private File[] find(File dir, String type) {
        ArrayList resultList = new ArrayList();
        find(dir, type, resultList, new HashSet());
        return (File[]) resultList.toArray(new File[] {});
    }

    private void find(File dir, String type, ArrayList list, HashSet seen) {
        File[] files = dir.listFiles();
        ArrayList drs = new ArrayList();
        String filename = "";
        spinner.spin();
        for (int i = 0; i < files.length; i++) {
            try {
                filename = files[i].getCanonicalPath();
            } catch (IOException fserror) {
                messageError("IOError! " + fserror);
                filename = "$IOEXCEPTION$";
                seen.add(filename);
            }
            if (!seen.contains(filename)) {
                if (files[i].isDirectory()) {
                    drs.add(files[i]);
                } else {
                    if (files[i].toString().endsWith(type)) list.add(files[i]);
                }
            }
            seen.add(filename);
        }
        File[] dirs = (File[]) drs.toArray(new File[] {});
        for (int i = 0; i < dirs.length; i++) {
            find(dirs[i], type, list, seen);
        }
    }

    private Process execute(String cmd) throws IOException, InterruptedException {
        messagePrint(cmd);
        Process proc = Runtime.getRuntime().exec(cmd);
        inOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        inErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        synchronized (errLock) {
            errLock.notify();
        }
        synchronized (outLock) {
            outLock.notify();
        }
        return proc;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (Thread.currentThread().getName().equals("stdout")) readOutAction(); else readErrAction();
                } catch (IOException ioerr) {
                    messageError("IOException: " + ioerr);
                }
            }
        } catch (InterruptedException interr) {
        }
    }

    private void readErrAction() throws IOException, InterruptedException {
        synchronized (errLock) {
            errLock.wait();
        }
        String line = null;
        while ((line = inErr.readLine()) != null) {
            messageError(line);
            spinner.spin();
        }
    }

    private void readOutAction() throws IOException, InterruptedException {
        synchronized (outLock) {
            outLock.wait();
        }
        String line = null;
        while ((line = inOut.readLine()) != null) {
            if (verbosity == OUTPUT_VERBOSE) {
                messagePrint(line);
            }
            spinner.spin();
        }
    }

    private final void eraseMessage() {
        if (message != null) {
            for (int i = 0; i < message.length(); i++) System.out.print(spinner.BS);
            System.out.print(" ");
            System.out.flush();
            System.out.print(spinner.BS);
        }
        System.out.flush();
    }

    private final void reprintMessage() {
        if (message != null) System.out.print(message);
        System.out.flush();
    }

    private synchronized void messageStart(String msg) {
        if (msg == null) return;
        if (verbosity == OUTPUT_SILENT) return;
        if (verbosity == OUTPUT_QUIET) return;
        if (verbosity == OUTPUT_STDERR) return;
        if (message != null) System.out.println();
        message = msg;
        System.out.print(message);
        System.out.flush();
    }

    private synchronized void messageError(String msg) {
        if (msg == null) return;
        if (verbosity == OUTPUT_SILENT) return;
        if (verbosity == OUTPUT_QUIET) return;
        eraseMessage();
        System.err.print(msg);
        if (message != null) {
            for (int i = 0; i < message.length(); i++) System.err.print(" ");
        }
        System.err.println();
        reprintMessage();
    }

    private synchronized void messagePrint(String msg) {
        if (msg == null) return;
        if (verbosity == OUTPUT_SILENT) return;
        if (verbosity == OUTPUT_QUIET) return;
        if (verbosity == OUTPUT_STDERR) return;
        if (verbosity == OUTPUT_DEFAULT) return;
        eraseMessage();
        System.out.println(msg);
        reprintMessage();
    }

    private synchronized void messageInform(String msg) {
        if (msg == null) return;
        if (verbosity == OUTPUT_SILENT) return;
        if (verbosity == OUTPUT_STDERR) return;
        eraseMessage();
        System.out.println(msg);
        reprintMessage();
    }

    private synchronized void messageStop(String msg) {
        if (msg == null) return;
        if (verbosity == OUTPUT_SILENT) return;
        if (verbosity == OUTPUT_QUIET) return;
        if (verbosity == OUTPUT_STDERR) return;
        spinner.unspin();
        System.out.println(msg);
        System.out.flush();
        message = null;
    }

    public static void main(String[] args) throws Exception {
        try {
            MakeJAS builder = new MakeJAS(args);
            int result = builder.build();
            if (result > 0) System.exit(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private class Spinner {

        private boolean showSlash = true;

        private int slashIndex = 0;

        private char SLASHES[] = { '-', '\\', '|', '/' };

        private String BS = "";

        public void showSlash(boolean b) {
            showSlash = b;
        }

        public synchronized void spin() {
            if (!showSlash) return;
            System.out.print(BS + SLASHES[slashIndex++]);
            System.out.flush();
            slashIndex %= SLASHES.length;
            Thread.yield();
        }

        public synchronized void unspin() {
            if (!showSlash) return;
            System.out.flush();
            System.out.print(BS);
            System.out.print(" ");
            System.out.print(BS);
            System.out.flush();
            Thread.yield();
        }
    }
}
