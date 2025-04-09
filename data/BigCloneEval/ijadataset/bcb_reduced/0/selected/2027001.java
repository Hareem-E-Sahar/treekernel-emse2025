package org.rt.author;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import java.io.File;

/**
 * The main class of the application.
 */
public class CredentialAuthorApp extends SingleFrameApplication {

    private static File profileDir;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        show(new CredentialAuthorView(this, profileDir));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of CredentialAuthorApp
     */
    public static CredentialAuthorApp getApplication() {
        return Application.getInstance(CredentialAuthorApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) throws Exception {
        char[] pass = { 'p' };
        String userName = "p";
        System.out.println("Entering key gen mode");
        File dirr = new File("/usr/lib/rt-credential/");
        for (int i = 0; i < 200; ++i) {
            System.out.print(i + " ");
            KeyGenerator.generateKeyPair(userName, userName, pass, i, dirr);
        }
        System.exit(0);
        String dir = null;
        profileDir = null;
        if (args.length == 2) {
            dir = args[1];
            if (dir.startsWith("\"")) {
                profileDir = new File(dir.substring(1, dir.length() - 1));
            } else {
                profileDir = new File(dir);
            }
            if (!(profileDir.exists() && profileDir.canRead() && profileDir.canWrite())) {
                System.err.println("Could not find profiles directory: " + dir + " does not exist or does not have proper permissions.");
            }
        }
        launch(CredentialAuthorApp.class, args);
    }
}
