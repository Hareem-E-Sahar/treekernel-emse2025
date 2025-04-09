import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import com.mtp.pounder.Player;

/**
 *
 * <p>Automatic class loader: </p>
 * <p>A class that loads automatically all jar libraries under lib folder and runs
 * the INGENIAS EDITOR</p>
 */
public class Runme {

    public Runme() {
    }

    ;

    private static final Class[] parameters = new Class[] { URL.class };

    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    public static void addFile(File f) throws IOException {
        addURL(f.toURL());
    }

    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { u });
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    public static void main(String args[]) throws Exception {
        java.io.File lib = new java.io.File("lib");
        File[] fs = lib.listFiles();
        for (int k = 0; k < fs.length; k++) {
            if (!fs[k].getName().toLowerCase().equals("ingeniaseditor.jar")) addFile(fs[k]); else {
                System.err.println(fs[k].getName());
            }
        }
        if (args.length > 0) {
            if (args[0].toLowerCase().equals("-t")) {
                File tfiles = new File("tutorial");
                File[] tutorials = tfiles.listFiles();
                File selected = (File) javax.swing.JOptionPane.showInputDialog(null, "Select one tutorial", "tutorials", javax.swing.JOptionPane.QUESTION_MESSAGE, null, tutorials, tutorials[0]);
                if (selected != null) {
                    Player player = new Player(selected.getPath());
                    player.play();
                } else {
                    System.exit(0);
                }
            } else {
                Class c = Class.forName("ingenias.editor.IDE");
                Method m = c.getMethod("main", new Class[] { String[].class });
                Object[] argl = new Object[args.length];
                System.arraycopy(args, 0, argl, 0, args.length);
                m.invoke(c, new Object[] { new String[0] });
            }
        } else {
            Class c = Class.forName("ingenias.editor.IDE");
            Method m = c.getMethod("main", new Class[] { String[].class });
            Object[] argl = new Object[args.length];
            System.arraycopy(args, 0, argl, 0, args.length);
            m.invoke(c, new Object[] { new String[0] });
        }
    }
}
