package com.loribel.commons.swing.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JDialog;
import com.loribel.commons.util.FTools;

/**
 * Tools.
 *
 * @author Gregory Borelli
 */
public final class GB_ScreenShotTools {

    public static int HEIGHT_TITLE_WINDOWS_2000 = 23;

    public static int HEIGHT_TITLE_WINDOWS_XP = 30;

    public static int HEIGHT_WINDOWS_TITLE = HEIGHT_TITLE_WINDOWS_XP;

    public static int HEIGHT_BORDER_WINDOWS = 5;

    public static int WIDTH_BORDER_WINDOWS_WEST = 6;

    public static int WIDTH_BORDER_WINDOWS_EAST = 5;

    public static void createScreenShot(Window a_frame, File a_file, boolean a_useBorder) throws Exception {
        if (a_frame == null) {
            return;
        }
        a_frame.toFront();
        a_frame.repaint();
        Thread.sleep(400);
        Dimension d = a_frame.getSize();
        Rectangle r = null;
        if (a_useBorder) {
            r = new Rectangle(a_frame.getX(), a_frame.getY(), d.width, d.height);
        } else {
            r = new Rectangle(a_frame.getX() + WIDTH_BORDER_WINDOWS_WEST, a_frame.getY() + HEIGHT_WINDOWS_TITLE, d.width - WIDTH_BORDER_WINDOWS_WEST - WIDTH_BORDER_WINDOWS_EAST, d.height - HEIGHT_WINDOWS_TITLE - HEIGHT_BORDER_WINDOWS);
        }
        createScreenShot(a_file, r);
    }

    public static void createScreenShot(File a_file) throws Exception {
        Dimension l_dim = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle r = new Rectangle(0, 0, l_dim.width, l_dim.height);
        createScreenShot(a_file, r);
    }

    public static void createScreenShot(File a_file, Rectangle a_rectangle) throws Exception {
        Robot l_robot = new Robot();
        BufferedImage l_img = l_robot.createScreenCapture(a_rectangle);
        a_file.getParentFile().mkdirs();
        String l_extension = FTools.getExtension(a_file);
        ImageIO.write(l_img, l_extension, a_file);
    }

    /**
     * Build a Dialog with JOptionPane.
     */
    public static void createScreenShotOptionDialog(Component a_parentComponent, Object a_message, String a_title, int a_optionType, int a_messageType, Icon a_icon, Object[] a_options, Object a_initialValue, File a_file) throws Exception {
        JDialog l_dialog = GB_DialogTools.buildOptionDialog(a_parentComponent, a_message, a_title, a_optionType, a_messageType, a_icon, a_options, a_initialValue);
        l_dialog.setModal(false);
        l_dialog.show();
        createScreenShot(l_dialog, a_file, true);
        l_dialog.dispose();
    }

    /**
     * Build a Dialog with JOptionPane.
     */
    public static void createScreenShotOptionDialog(Component a_parentComponent, Object a_message, String a_title, int a_optionType, int a_messageType, File a_file) throws Exception {
        JDialog l_dialog = GB_DialogTools.buildOptionDialog(a_parentComponent, a_message, a_title, a_optionType, a_messageType);
        l_dialog.setModal(false);
        l_dialog.show();
        createScreenShot(l_dialog, a_file, true);
    }
}
