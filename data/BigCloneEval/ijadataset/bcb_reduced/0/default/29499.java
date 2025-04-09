import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Pack {

    private static String root = "D:\\workspaces";

    private static File dir;

    private static ZipOutputStream out;

    private static byte[] b = new byte[1024 * 1024];

    private static List<String> ignoreList = new ArrayList<String>();

    static {
        ignoreList.add("work");
        ignoreList.add("CVS");
        ignoreList.add("META-INF");
        ignoreList.add("bak");
        ignoreList.add("temp");
        ignoreList.add("bin");
        ignoreList.add("classes");
    }

    public static void main(String[] args) throws Exception {
        if (!root.endsWith("/") || root.endsWith("\\")) root += "\\";
        dir = new File(root);
        out = new ZipOutputStream(new FileOutputStream("D:\\workspaces.zip"));
        list(dir);
        out.close();
    }

    private static void list(File dir) throws Exception {
        String prefix = dir.getCanonicalPath();
        if (prefix.length() > root.length()) {
            prefix = prefix.substring(root.length());
        } else {
            prefix = "";
        }
        for (String name : dir.list()) {
            if (!name.startsWith(".") && !name.endsWith(".jar") && !ignoreList.contains(name)) {
                File file = new File(dir, name);
                if (file.isDirectory()) {
                    System.out.println(file);
                    list(file);
                } else {
                    out.putNextEntry(new ZipEntry(prefix + "\\" + name));
                    FileInputStream in = new FileInputStream(file);
                    int l;
                    while ((l = in.read(b)) != -1) {
                        if (l != 0) out.write(b, 0, l);
                    }
                    in.close();
                    out.closeEntry();
                }
            }
        }
    }
}
