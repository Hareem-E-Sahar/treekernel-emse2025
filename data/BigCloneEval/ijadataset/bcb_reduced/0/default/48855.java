import java.awt.Frame;
import de.fuhrmeister.browserchooser.SplashLoader;
import de.fuhrmeister.util.ImageManager;

/**
 * This class is the entry point only for packed JARs used
 * on Mac OS X. It is a "workaround wrapper".
 * On Mac OS X the name of the class that contains the main method,
 * will be the application name in the menu bar.
 * 
 *  
 *	@date		24.11.2009
 *	@author		Marcus Fuhrmeister
 *  @version
 */
public class BrowserChooser {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        Frame splashFrame = null;
        splashFrame = SplashLoader.splash(ImageManager.getInstance().getImage(ImageManager.SPLASH));
        try {
            Class.forName("de.fuhrmeister.browserchooser.Loader").getMethod("main", new Class[] { String[].class }).invoke(null, new Object[] { args });
        } catch (final Exception ex) {
            ex.printStackTrace();
            System.exit(10);
        }
        if (splashFrame != null) {
            splashFrame.dispose();
        }
    }
}
