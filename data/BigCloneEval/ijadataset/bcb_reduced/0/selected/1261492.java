package org.stumeikle.NeuroCoSA.Debug;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import javax.accessibility.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;

/**
 * A generic NeuroCoSADEBUG demo module
 *
 * @version 1.21 03/25/05
 * @author Jeff Dinkins
 */
public class DemoModule extends JApplet {

    private int PREFERRED_WIDTH = 680;

    private int PREFERRED_HEIGHT = 600;

    Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), new EmptyBorder(5, 5, 5, 5));

    public static Dimension HGAP2 = new Dimension(2, 1);

    public static Dimension VGAP2 = new Dimension(1, 2);

    public static Dimension HGAP5 = new Dimension(5, 1);

    public static Dimension VGAP5 = new Dimension(1, 5);

    public static Dimension HGAP10 = new Dimension(10, 1);

    public static Dimension VGAP10 = new Dimension(1, 10);

    public static Dimension HGAP15 = new Dimension(15, 1);

    public static Dimension VGAP15 = new Dimension(1, 15);

    public static Dimension HGAP20 = new Dimension(20, 1);

    public static Dimension VGAP20 = new Dimension(1, 20);

    public static Dimension HGAP25 = new Dimension(25, 1);

    public static Dimension VGAP25 = new Dimension(1, 25);

    public static Dimension HGAP30 = new Dimension(30, 1);

    public static Dimension VGAP30 = new Dimension(1, 30);

    private NeuroCoSADEBUG swingset = null;

    private JPanel panel = null;

    private String resourceName = null;

    private String iconPath = null;

    private String sourceCode = null;

    private ResourceBundle bundle = null;

    public DemoModule(NeuroCoSADEBUG swingset) {
        this(swingset, null, null);
    }

    public DemoModule(NeuroCoSADEBUG swingset, String resourceName, String iconPath) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        this.resourceName = resourceName;
        this.iconPath = iconPath;
        this.swingset = swingset;
        loadSourceCode();
    }

    public String getResourceName() {
        return resourceName;
    }

    public JPanel getDemoPanel() {
        return panel;
    }

    public NeuroCoSADEBUG getNeuroCoSADEBUG() {
        return swingset;
    }

    public String getString(String key) {
        String value = "nada";
        if (bundle == null) {
            if (getNeuroCoSADEBUG() != null) {
                bundle = getNeuroCoSADEBUG().getResourceBundle();
            } else {
                bundle = ResourceBundle.getBundle("resources.swingset");
            }
        }
        try {
            value = bundle.getString(key);
        } catch (MissingResourceException e) {
            System.out.println("java.util.MissingResourceException: Couldn't find value for: " + key);
        }
        return value;
    }

    public char getMnemonic(String key) {
        return (getString(key)).charAt(0);
    }

    public ImageIcon createImageIcon(String filename, String description) {
        if (getNeuroCoSADEBUG() != null) {
            return getNeuroCoSADEBUG().createImageIcon(filename, description);
        } else {
            String path = "/NeuroCoSA.Debug/resources/images/" + filename;
            return new ImageIcon(getClass().getResource(path), description);
        }
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void loadSourceCode() {
        if (getResourceName() != null) {
            String filename = getResourceName() + ".java";
            sourceCode = new String("<html><body bgcolor=\"#ffffff\"><pre>");
            InputStream is;
            InputStreamReader isr;
            CodeViewer cv = new CodeViewer();
            URL url;
            try {
                url = getClass().getResource(filename);
                is = url.openStream();
                isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine();
                while (line != null) {
                    sourceCode += cv.syntaxHighlight(line) + " \n ";
                    line = reader.readLine();
                }
                sourceCode += new String("</pre></body></html>");
            } catch (Exception ex) {
                sourceCode = "Could not load file: " + filename;
            }
        }
    }

    public String getName() {
        return getString(getResourceName() + ".name");
    }

    ;

    public Icon getIcon() {
        return createImageIcon(iconPath, getResourceName() + ".name");
    }

    ;

    public String getToolTip() {
        return getString(getResourceName() + ".tooltip");
    }

    ;

    public void mainImpl() {
        JFrame frame = new JFrame(getName());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(getDemoPanel(), BorderLayout.CENTER);
        getDemoPanel().setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        frame.pack();
        frame.show();
    }

    public JPanel createHorizontalPanel(boolean threeD) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setAlignmentX(LEFT_ALIGNMENT);
        if (threeD) {
            p.setBorder(loweredBorder);
        }
        return p;
    }

    public JPanel createVerticalPanel(boolean threeD) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setAlignmentX(LEFT_ALIGNMENT);
        if (threeD) {
            p.setBorder(loweredBorder);
        }
        return p;
    }

    public static void main(String[] args) {
        DemoModule demo = new DemoModule(null);
        demo.mainImpl();
    }

    public void init() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(getDemoPanel(), BorderLayout.CENTER);
    }

    void updateDragEnabled(boolean dragEnabled) {
    }

    public void notifyActivated() {
    }
}
