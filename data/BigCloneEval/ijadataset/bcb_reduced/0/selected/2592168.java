package edu.drexel.sd0910.ece01.aqmon.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;

/**
 * Utils class to determine what operating system is being used and other
 * platform-specified functions.
 * 
 * @author Kyle O'Connor
 * 
 */
public class OSUtils {

    private static final Logger log = Logger.getLogger(OSUtils.class);

    /**
	 * Utils class not be to instantiated.
	 */
    private OSUtils() {
    }

    /**
	 * Determines if the current platform is Windows.
	 * 
	 * @return true if Windows, false, otherwise.
	 */
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    /**
	 * Determines if the current platform is Mac.
	 * 
	 * @return true if Mac, false, otherwise.
	 */
    public static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("mac") >= 0);
    }

    /**
	 * Determines if the current platform is Linux or Unix.
	 * 
	 * @return true if Unix, false, otherwise.
	 */
    public static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }

    /**
	 * Get the platform's <code>String</code> to mark the end of each line.
	 * lineSeparator is a misnomer. It really should be lineTerminator.
	 * 
	 * @return the line separator
	 */
    public static String getLineTerminator() {
        return System.getProperty("line.separator");
    }

    /**
	 * Get the current working directory of the Java application as a
	 * <code>String</code>.
	 * 
	 * @return the current directory
	 */
    public static String getCurrentWorkingDirectory() {
        return System.getProperty("user.dir") + File.separator;
    }

    /**
	 * Utilizes the Java Desktop API to launch items using the default
	 * application for handling such items on this system. For more information
	 * see the {@link Desktop#browse(URI)} method.
	 * 
	 * @param strURI
	 *            the <code>URI</code> to a file or webpage as
	 *            <code>String</code>
	 */
    public static void browseToURI(final String strURI) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(strURI));
            } catch (IOException exp) {
                log.error(exp.toString());
            } catch (URISyntaxException exp) {
                log.error(exp.toString());
            }
        } else {
            log.fatal("Java Desktop API is not supported on this platform.");
        }
    }

    public static void main(String[] args) {
        if (isWindows()) {
            System.out.println("This is Windows");
        } else if (isMac()) {
            System.out.println("This is Mac");
        } else if (isUnix()) {
            System.out.println("This is Unix or Linux");
        } else {
            System.out.println("Your OS is not supported!");
        }
        System.out.println("Line Separator" + getLineTerminator() + "Test");
    }
}
