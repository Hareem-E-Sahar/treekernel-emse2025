import java.util.*;
import java.io.*;
import java.text.*;
import gnu.gettext.*;

public class Hello {

    public static void main(String[] args) {
        ResourceBundle catalog = ResourceBundle.getBundle("hello-java");
        System.out.println(GettextResource.gettext(catalog, "Hello, world!"));
        System.out.println(MessageFormat.format(GettextResource.gettext(catalog, "This program is running as process number {0}."), new Object[] { getPid() }));
    }

    private static String getPid() {
        try {
            String[] args = new String[] { "/bin/sh", "-c", "echo $PPID" };
            Process p = Runtime.getRuntime().exec(args);
            InputStream p_out = p.getInputStream();
            String s = (new BufferedReader(new InputStreamReader(p_out))).readLine();
            p.destroy();
            if (s != null) return s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "???";
    }
}
