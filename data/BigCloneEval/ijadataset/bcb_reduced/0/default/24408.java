import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class J2ME {

    private static String jarPath = "C:\\Documents and Settings\\Administrator\\桌面\\androidChargeControlServer.jar";

    private static String charsetName = "utf-8";

    private static ProcessBuilder builder = new ProcessBuilder("cmd", "/c", "jad -ff -space -nonlb *.class");

    private static Pattern pattern = Pattern.compile("\\\\u([0-9A-F]{1,4})");

    public static void main(String[] args) throws Exception {
        if (args.length > 0) jarPath = args[0];
        if (args.length > 1) charsetName = args[1];
        File directory = extractJar();
        checkDirectory(directory);
        File src = new File(directory, "src");
        src.mkdir();
        File res = new File(directory, "res");
        res.mkdir();
        sortDirectory(directory, src, res);
    }

    private static File extractJar() throws Exception {
        File jar = new File(jarPath);
        ZipFile zip = new ZipFile(jar);
        Enumeration<ZipEntry> zipEntry = (Enumeration<ZipEntry>) zip.entries();
        File directory = new File(jar.getParent(), jar.getName().replace(".jar", "_") + System.currentTimeMillis());
        directory.mkdir();
        while (zipEntry.hasMoreElements()) {
            ZipEntry entry = zipEntry.nextElement();
            File file = new File(directory, entry.getName());
            if (entry.isDirectory()) {
                file.mkdir();
            } else {
                File parent = file.getParentFile();
                if (!parent.exists()) parent.mkdirs();
                BufferedInputStream in = new BufferedInputStream(zip.getInputStream(entry));
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                byte[] b = new byte[1024000];
                for (int i = in.read(b); i > -1; i = in.read(b)) {
                    if (i != 0) out.write(b, 0, i);
                }
                in.close();
                out.close();
            }
        }
        zip.close();
        return directory;
    }

    private static void checkDirectory(File directory) throws Exception {
        boolean alreadyDecompile = false;
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                checkDirectory(file);
            } else if (!alreadyDecompile && file.getName().endsWith(".class")) {
                decompileClass(directory);
                alreadyDecompile = true;
            }
        }
    }

    private static void decompileClass(File directory) throws Exception {
        builder.directory(directory);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        InputStream in = process.getInputStream();
        byte[] b = new byte[1024];
        while (in.read(b) != -1) ;
        in.close();
        for (File file : directory.listFiles()) {
            String name = file.getName();
            if (name.endsWith(".jad")) {
                file.renameTo(new File(file.getParent(), file.getName().replace(".jad", ".java")));
            } else if (name.endsWith(".class")) {
                file.delete();
            }
        }
    }

    private static void sortDirectory(File directory, File src, File res) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                String name = file.getName();
                if (!name.endsWith("src") && !name.endsWith("res")) sortDirectory(file, src, res);
            } else if (file.getName().endsWith(".java")) {
                File move = new File(src, file.getPath().replace(src.getParent(), ""));
                File dir = move.getParentFile();
                if (!dir.exists()) dir.mkdirs();
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                PrintWriter out = new PrintWriter(move, charsetName);
                byte[] b = new byte[1024 * 1024];
                int i;
                while ((i = in.read(b)) != -1) {
                    if (i > 0) out.write(decodeUnicode(new String(b, 0, i)));
                }
                in.close();
                file.delete();
                out.close();
            } else {
                File move = null;
                if (file.getName().endsWith("MANIFEST.MF")) {
                    move = new File(src.getParent(), new File(jarPath).getName().replace(".jar", ".jad"));
                } else {
                    move = new File(res, file.getPath().replace(src.getParent(), ""));
                }
                File dir = move.getParentFile();
                if (!dir.exists()) dir.mkdirs();
                file.renameTo(move);
            }
        }
        if (directory.compareTo(src.getParentFile()) != 0) directory.delete();
    }

    public static String decodeUnicode(String content) {
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = null;
        boolean isMatches = false;
        while (matcher.find()) {
            if (!isMatches) {
                sb = new StringBuffer();
                isMatches = true;
            }
            char c = (char) Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(sb, String.valueOf(c));
        }
        if (isMatches) {
            matcher.appendTail(sb);
            content = sb.toString();
        }
        return content;
    }
}
