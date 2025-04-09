package org.ais.convert.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

/**
 * Utilities class - simple set of useful static functions which are used by
 * different classes from different packages. Mostly GUI utilities.
 */
public class GUIUtils {

    /**
	 * Puts component to be centralized relative to the parent
	 */
    public static void setCentalizedLocationRelativeMe(Component parent, Component me) {
        if (parent == null) return;
        Point parentLoc = parent.getLocation();
        int x = 0, y = 0;
        int parentX = parentLoc.x;
        int parentY = parentLoc.y;
        int parentW = parent.getSize().width;
        int parentH = parent.getSize().height;
        int w = me.getSize().width;
        int h = me.getSize().height;
        if (w < parentW) x = parentX + (parentW - w) / 2; else x = parentX - (w - parentW) / 2;
        if (h < parentH) y = parentY + (parentH - h) / 2; else y = parentY - (h - parentH) / 2;
        me.setLocation(x, y);
    }

    /**
	 * Puts the component into the center of the screen
	 */
    public static void setCentalizedLocation(Component me) {
        int x = 0, y = 0;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int screenW = dim.width;
        int screenH = dim.height;
        int w = me.getSize().width;
        int h = me.getSize().height;
        if (w < screenW) x = (screenW - w) / 2; else x = 0;
        if (h < screenH) y = (screenH - h) / 2; else y = 0;
        me.setLocation(x, y);
    }

    /**
	 * Returns the main parent frame for the component
	 */
    public static Frame getParentFrame(Component c) {
        while (c.getParent() != null) {
            c = c.getParent();
        }
        return (Frame) c;
    }

    /**
	 * Returns the nearest Window for the component
	 */
    public static Window getNearestWindow(Component c) {
        while (c != null && !(c instanceof Window)) {
            c = c.getParent();
        }
        return (Window) c;
    }

    /**
	 * Returns the nearest frame for the component
	 */
    public static Frame getNearestFrame(Component c) {
        while (c != null && !(c instanceof Frame)) {
            c = c.getParent();
        }
        return (Frame) c;
    }

    /**
	 * Returns the nearest Dialog for the component
	 */
    public static Dialog getNearestDialog(Component c) {
        while (c != null && !(c instanceof Dialog)) {
            c = c.getParent();
        }
        return (Dialog) c;
    }

    /**
	 * Prints all parents of the component (for debugging)
	 */
    public static String printParents(Component c) {
        StringBuffer sb = new StringBuffer("");
        while (c.getParent() != null) {
            sb.append(c.getName());
            sb.append("/");
            c = c.getParent();
        }
        sb.append(c.getName());
        return sb.toString();
    }

    /**
	 * Dumps (prints out) current memory state (VM total/free) to stdout
	 */
    public static void sample() {
        System.out.println("-----------------Sampling------------------");
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        System.out.println("Total memory:" + total);
        System.out.println("Free memory:" + free);
        System.out.println("Percentage available:" + (int) (((double) free / (double) total) * (double) 100) + "%");
    }

    private static Color fromRGB(String tag) {
        Color color = null;
        try {
            String rgb = tag.substring(1);
            int r = Integer.parseInt(rgb.substring(0, 2), 16);
            int g = Integer.parseInt(rgb.substring(2, 4), 16);
            int b = Integer.parseInt(rgb.substring(4, 6), 16);
            return new Color(r, g, b);
        } catch (Exception e) {
        }
        return color;
    }

    public static final Font deriveFont(Font sample, String family, String style, String sizeStr) throws Exception {
        int size;
        if (sizeStr == null) {
            size = sample.getSize();
        } else {
            size = Integer.parseInt(sizeStr);
        }
        int fs;
        if (style == null) {
            fs = sample.getStyle();
        } else if (style.equals("bold")) {
            fs = Font.BOLD;
        } else if (style.equals("italic")) {
            fs = Font.ITALIC;
        } else if (style.equals("bold_italic")) {
            fs = (Font.ITALIC | Font.BOLD);
        } else if (style.equals("plane")) {
            fs = Font.PLAIN;
        } else {
            throw new Exception("Font style:" + style + " does not exist");
        }
        String ff = null;
        if (family == null) {
            ff = sample.getName();
        } else {
            ff = family;
        }
        return new Font(ff, fs, size);
    }

    public static void main(String[] args) {
        java.util.Properties props = new java.util.Properties();
        props.put("aaa", "bbb");
        props.put("bbb", "bbb");
        props.put("c", "bbb");
        props.put("Properties.h1", "h1value");
        props.put("Properties.h2", "h2value");
        java.util.Properties servletSpecificProps = new java.util.Properties();
        String fullClassName = servletSpecificProps.getClass().getName();
        String justName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String thisServletPrefix = justName + ".";
        int indFrom = thisServletPrefix.length();
        for (java.util.Enumeration en = props.keys(); en.hasMoreElements(); ) {
            String aKey = (String) en.nextElement();
            if (!aKey.startsWith(thisServletPrefix)) continue;
            Object aValue = props.get(aKey);
            String newKey = aKey.substring(indFrom);
            servletSpecificProps.put(newKey, aValue);
        }
        System.out.println(servletSpecificProps);
    }
}
