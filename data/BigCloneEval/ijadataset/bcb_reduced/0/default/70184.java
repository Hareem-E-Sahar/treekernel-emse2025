import java.io.File;
import java.util.HashMap;

/**
 * <p>This class generates the java docs for all the files in the given path
 * and saves the html files in the given destination directory.</p>
 * <p>Copyright � 2008 Shaz Solutions. All Rights Reserved.</p>
 * <p>Company: Shaz Solutions</p>
 * @author Shahzad Masud
 * @created June 04, 2008
 * @version 1.0
 */
public class JavaDocGenerator {

    private HashMap map = null;

    private String destDir = null;

    private String srcDir = null;

    private String options = " -quiet -splitindex -use -version -author ";

    public JavaDocGenerator(String srcDir, String destDir, String options) {
        this.destDir = destDir;
        this.srcDir = srcDir;
        if (options != null) {
            this.options = options;
        }
    }

    public void run() throws Exception {
        if (destDir == null || srcDir == null) {
            throw new Exception("Source and destination directories can not be null.");
        }
        System.out.println("Looking for java files ...");
        String fileNames = getFiles();
        StringBuffer command = new StringBuffer("javadoc -d ");
        command.append(destDir);
        command.append(options);
        command.append(fileNames);
        System.out.println("Command : \n" + command.toString());
        Process p = Runtime.getRuntime().exec(command.toString());
        java.io.BufferedInputStream bis = new java.io.BufferedInputStream(p.getInputStream());
        System.out.println("Process Output : ");
        byte b = 0;
        while ((b = (byte) bis.read()) != -1) {
            System.out.print((char) b);
        }
        int output = p.waitFor();
        System.out.println("Stopped with code : " + output);
    }

    /**
   * This method returns package paths separated by space appended by \*.java
   * in one string so that it can be used to generate java doc of all the files.
   * @return String having file list separated by space.
   * @throws Exception In case the <code>srcDir</code> does not exist.
   */
    private String getFiles() throws Exception {
        map = new HashMap();
        File srcPath = new File(srcDir);
        if (srcPath.exists() == false) {
            throw new Exception("Source Path does not exist.");
        }
        getPaths(srcPath);
        StringBuffer str = new StringBuffer(" ");
        java.util.Set keys = map.keySet();
        String[] toBePrinted = (String[]) keys.toArray(new String[0]);
        for (int i = 0; i < toBePrinted.length; i++) {
            str.append(toBePrinted);
            str.append("\\*.java ");
        }
        return str.toString();
    }

    private void getPaths(File path) {
        if (path.isFile()) {
            map.put(path.getParent(), "Found");
        }
        if (path.isDirectory()) {
            File[] children = path.listFiles();
            for (int i = 0; i < children.length; i++) {
                getPaths(children[i]);
            }
        }
    }

    /**
   * Main method to run this class.
   * @param args Command line arguments.
   * @throws Exception in case of any errors.
   */
    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 1) {
            System.err.println("Usage : java JavaDocGenerator \"Source Dir\" \"Destination Dir\"");
            System.exit(1);
        }
        String options = " -quiet -splitindex -use -version -author ";
        if (args.length > 2) {
            options = args[2];
        }
        JavaDocGenerator obj = new JavaDocGenerator(args[0], args[1], options);
        obj.run();
        return;
    }
}
