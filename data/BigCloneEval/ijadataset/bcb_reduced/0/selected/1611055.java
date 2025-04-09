package globali;

import java.awt.Desktop;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sascha_p
 */
public class jcApriFileEsterno {

    public static void apri(java.io.File dst) {
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + dst);
            } catch (IOException ex) {
                Logger.getLogger(jcApriFileEsterno.class.getName()).log(Level.SEVERE, null, ex);
                jcFunzioni.erroreSQL(ex.toString());
            }
        } else {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(dst);
                } catch (IOException ex) {
                    Logger.getLogger(jcApriFileEsterno.class.getName()).log(Level.SEVERE, null, ex);
                    jcFunzioni.erroreSQL(ex.toString());
                }
            } else {
                jcFunzioni.erroreSQL("Impossibile lanciare il file !");
            }
        }
    }
}
