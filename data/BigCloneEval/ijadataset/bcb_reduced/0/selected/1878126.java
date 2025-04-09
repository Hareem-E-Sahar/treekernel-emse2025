package Utilitaires;

import java.awt.Desktop;
import java.io.File;

public class OpenWithDefaultViewer {

    /**
	 * Ouvre un fichier avec le programme par defaut du systeme.
	 * @param toOpen -String
	 * 
	 */
    public static void open(String toOpen) {
        if (toOpen == null) throw new NullPointerException();
        if (!Desktop.isDesktopSupported()) return;
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(new File(toOpen));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
