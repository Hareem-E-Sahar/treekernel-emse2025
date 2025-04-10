package logicsim;

import java.util.Properties;
import java.io.FileInputStream;
import javax.swing.JOptionPane;
import java.net.*;
import javax.swing.JApplet;

/**
 *
 * @author atetzl
 */
public class I18N {

    public static Properties prop = null;

    public I18N() {
        this(null);
    }

    /** Creates a new instance of I18N */
    public I18N(JApplet applet) {
        if (prop != null) return;
        String lang = "en";
        try {
            Properties userProperties = new Properties();
            if (applet != null) {
                URL url = new URL(applet.getCodeBase() + "logicsim.cfg");
                userProperties.load(url.openStream());
            } else {
                userProperties.load(new FileInputStream("logicsim.cfg"));
            }
            if (userProperties.containsKey("language")) lang = userProperties.getProperty("language");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        prop = new Properties();
        try {
            if (applet != null) {
                URL url = new URL(applet.getCodeBase() + "languages/" + lang + ".txt");
                prop.load(url.openStream());
            } else {
                prop.load(new FileInputStream("languages/" + lang + ".txt"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                if (applet != null) {
                    URL url = new URL(applet.getCodeBase() + "languages/en.txt");
                    prop.load(url.openStream());
                } else {
                    prop.load(new FileInputStream("languages/en.txt"));
                }
            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(null, "Language file languages/en.txt not found.\nPlease run the program from its directory.");
                System.exit(5);
            }
        }
    }

    public static String getString(String key) {
        return prop.getProperty(key);
    }
}
