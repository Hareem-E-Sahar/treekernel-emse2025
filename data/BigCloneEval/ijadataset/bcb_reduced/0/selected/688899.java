package registry;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pucgenie
 */
public class SetACL {

    private static final String SETACLEXE = System.getProperty("user.dir") + File.separator + "dismGUI-tools" + File.separator + System.getProperty("os.arch") + File.separator + "setacl.exe";

    static {
        System.out.println("Klasse geladen: registry.SetACL");
        System.out.println(" " + SETACLEXE);
        System.out.println(" SetACL.exe exists: " + new File(SETACLEXE).exists());
    }

    /**
     * Sets Read/Write permission to the keys in
     * HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Component Based Servicing\Packages
     */
    public static boolean unlockPackages(String target) {
        boolean success = false;
        try {
            Process prc;
            prc = Runtime.getRuntime().exec((SETACLEXE + "'-on HKLM\\" + (target == null ? "" : (target + "\\")) + "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Component Based Servicing\\Packages'-ot reg'-actn setowner'-ownr n:S-1-1-0;s:y'-rec yes").split("'"));
            int c;
            BufferedInputStream bis = new BufferedInputStream(prc.getInputStream());
            prc.getOutputStream().write('n');
            success = prc.waitFor() == 0;
            while ((c = bis.read()) != -1) {
                System.out.print((char) c);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(SetACL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SetACL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }
}
