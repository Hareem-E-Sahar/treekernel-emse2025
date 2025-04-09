import java.io.*;
import java.util.*;
import java.util.regex.*;

public class jwhich2 {

    static final boolean debug = false;

    static final String JAR_CMD = "jar";

    public static void main(String[] args) throws Exception {
        Map cmdargs = null;
        try {
            Pattern opt_with_params = Pattern.compile("^-j$");
            Pattern opt_no_params = Pattern.compile("^-[kxq]$");
            cmdargs = parse_cmd_args(args, opt_with_params, opt_no_params);
            if (debug) System.out.println("Parsed cmd args:" + cmdargs);
            List params = (List) cmdargs.get("#");
            if (params == null || params.size() < 2) throw new Exception("required argument missing");
            String jarcmd = (String) cmdargs.get("-j");
            Boolean no_recurs = (Boolean) cmdargs.get("-x");
            Boolean quite = (Boolean) cmdargs.get("-q");
            Boolean keep_searching = (Boolean) cmdargs.get("-k");
            jwhich2 jw = new jwhich2();
            if (jarcmd != null) jw.jar_cmd = jarcmd;
            if (quite != null) jw.verbose = false;
            if (no_recurs != null) jw.recursive = false;
            if (keep_searching != null) jw.keep_searching = true;
            if (debug) System.out.println("jw:" + jw);
            String dir = (String) params.get(0);
            String className = (String) params.get(1);
            className = className.replace('.', '/');
            jw.find(className, dir);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("args: " + Arrays.asList(args));
            System.out.println();
            usage();
            System.exit(1);
        }
    }

    static void usage() {
        System.out.println("Usage: java jwhich2 [-k] [-x] [-q] [-j <jar_cmd>] <dir> <classname>");
        System.out.println("\t-x do not search subfolders");
        System.out.println("\t-q quite");
        System.out.println("\t-j path to jar executable");
        System.out.println("\t-k keep searching for more after found a match");
        System.out.println();
    }

    static Map parse_cmd_args(String[] args, Pattern opt_with_params, Pattern opt_no_params) throws Exception {
        Map ret = new HashMap();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (opt_no_params.matcher(arg).matches()) {
                ret.put(arg, Boolean.TRUE);
            } else if (opt_with_params.matcher(arg).matches()) {
                if (i + 1 >= args.length) throw new Exception("option " + arg + " has no argumnet");
                ret.put(arg, args[++i]);
            } else {
                List params = (List) ret.get("#");
                if (params == null) {
                    params = new ArrayList();
                    ret.put("#", params);
                }
                params.add(arg);
            }
        }
        return ret;
    }

    static final FileFilter filter = new FileFilter() {

        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().endsWith(".jar");
        }
    };

    boolean verbose = true;

    boolean recursive = true;

    boolean keep_searching = false;

    boolean match_found = false;

    String jar_cmd = JAR_CMD;

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("verbose=").append(verbose).append(", recursive=").append(recursive).append(", keep_searching=").append(keep_searching).append(", match_found=").append(match_found).append(", jar_cmd=").append(jar_cmd);
        return buf.toString();
    }

    void find(String className, String dir) throws Exception {
        System.out.println("finding '" + className + "' in '" + dir + "'");
        System.out.println();
        if (!find(className, new File(dir))) {
            System.out.println();
            if (!match_found) System.out.println("No match found.");
        }
    }

    boolean find(String className, File dir) throws Exception {
        File[] subs = dir.listFiles(filter);
        if (subs == null) return false;
        for (int i = 0; i < subs.length; i++) {
            File f = subs[i];
            if (f.isDirectory()) {
                if (recursive && find(className, f)) {
                    if (!keep_searching) return true; else match_found = true;
                }
            } else {
                if (do_which(className, f)) {
                    if (!keep_searching) return true; else match_found = true;
                }
            }
        }
        return false;
    }

    boolean do_which(String className, File f) throws Exception {
        Runtime rt = Runtime.getRuntime();
        if (verbose) System.out.println(f);
        Process proc = rt.exec(JAR_CMD + " -tvf " + f.getAbsolutePath());
        InputStream proc_out = proc.getInputStream();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(proc_out));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.indexOf(className) != -1) {
                    System.out.println();
                    System.out.println("Match found: ");
                    System.out.println("\t" + line.trim());
                    System.out.println("\t" + f.getAbsolutePath());
                    System.out.println();
                    return true;
                }
            }
            return false;
        } finally {
            if (reader != null) reader.close();
        }
    }
}
