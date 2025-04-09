package net.mjrz.fm.ui.help;

import java.awt.Desktop;
import java.net.URI;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.MiscUtils;
import org.apache.log4j.Logger;

public class HelpUtil {

    public static String HELP_URL = "http://www.ifreebudget/dist/help_redirect.php?key=";

    private static Logger logger = Logger.getLogger(HelpUtil.class.getName());

    public static void loadHelpPage(String key) {
        try {
            java.awt.Desktop d = Desktop.getDesktop();
            if (Desktop.isDesktopSupported()) {
                if (key == null || key.length() == 0) {
                    d.browse(new URI(UIDefaults.PRODUCT_DOCUMENTATION_URL));
                } else {
                    String url = HELP_URL + key;
                    d.browse(new URI(url));
                }
            }
        } catch (Exception e) {
            logger.error(MiscUtils.stackTrace2String(e));
        }
    }
}
