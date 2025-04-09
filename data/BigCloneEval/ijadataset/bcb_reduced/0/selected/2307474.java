package com.ctext.ite.gui.main;

import com.apple.eawt.Application;
import com.ctext.ite.utils.Logger;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.ctext.ite.javaversion.JavaVersion;
import com.ctext.ite.javaversion.VersionChecker;
import com.ctext.ite.utils.OSOperations;
import com.ctext.ite.utils.StringHandler;
import org.omegat.gui.main.ITELinker;
import org.omegat.util.gui.Colouriser;

/**
 * The main class used to launch the ITE.
 * @author W. Fourie, M. Schlemmer
 */
public class Main {

    private static MainFrame frame;

    /** Entry point */
    public static void main(String[] args) {
        Logger.logger = new Logger();
        Logger.logger.log(Level.INFO, "Program Started");
        if (VersionChecker.compareVersions(VersionChecker.getVersionValues(System.getProperty("java.version")), new JavaVersion(1, 6, 0, 12))) {
            Logger.logger.log(Level.INFO, "Correct JRE version");
        } else {
            JOptionPane.showMessageDialog(null, StringHandler.getString("DM_JRE"), StringHandler.getString("DT_JRE"), JOptionPane.ERROR_MESSAGE);
            Logger.logger.log(Level.WARNING, "Outdated JRE version");
        }
        registerFont();
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null) {
            Graphics2D g = splash.createGraphics();
            splash.update();
        }
        Logger.logger.log(Level.INFO, "OS Type: " + OSOperations.osType);
        try {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        UIManager.getInstalledLookAndFeels();
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        if (OSOperations.osType.startsWith("Mac")) {
                            System.setProperty("apple.laf.useScreenMenuBar", "true");
                            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Autshumato ITE");
                        }
                    } catch (java.lang.Exception ex) {
                        Logger.logger.log(Level.WARNING, "Unbale to setLookAndFeel", ex);
                    }
                    frame = new MainFrame();
                    frame.setTitle("Autshumato ITE");
                    frame.setLocation(0, 0);
                    frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
                    frame.setVisible(true);
                    MainHandler.mainHandler = new MainHandler(frame);
                    ITELinker.linker = new ITELinker(MainHandler.mainHandler.icon);
                    Colouriser.colours = new Colouriser();
                    frame.setIconImage(MainHandler.mainHandler.icon);
                    if (OSOperations.osType.startsWith("Mac")) {
                        com.apple.eawt.Application.getApplication().setDockIconImage(MainHandler.mainHandler.icon2);
                        Application macApp = Application.getApplication();
                        MacHandler mh = new MacHandler(frame);
                        macApp.addApplicationListener(mh);
                        macApp.setEnabledPreferencesMenu(true);
                    }
                    frame.runITE();
                }
            });
        } catch (java.lang.Exception ex) {
            Logger.logger.log(Level.SEVERE, "Abnormal application termination", ex);
            MainFrame p = null;
            String err = StringHandler.getString("DM_AE") + ex.getMessage();
            if (frame != null) p = frame;
            JOptionPane.showMessageDialog(p, err, StringHandler.getString("DT_AE"), JOptionPane.ERROR_MESSAGE);
            try {
                if (frame != null) frame.dispose();
            } catch (java.lang.Exception ex2) {
                Logger.logger.log(Level.WARNING, "Exception Closing MainFrame", ex2);
                System.exit(0);
            }
        }
    }

    private static void registerFont() {
        boolean found = false;
        String[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals("DejaVu Sans")) found = true;
        }
        if (found) {
            Logger.logger.log(Level.INFO, "Font already installed");
        } else {
            try {
                java.io.InputStream fontStream = Main.class.getResourceAsStream("/com/ctext/ite/gui/resources/DejaVuSans.ttf");
                java.awt.Font myFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontStream);
                fontStream.close();
                if (java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(myFont)) Logger.logger.log(Level.INFO, "Font Registered"); else Logger.logger.log(Level.INFO, "Font Not Registered");
            } catch (Exception ex) {
                Logger.logger.log(Level.WARNING, "Error Loading Font", ex);
            }
        }
    }
}
