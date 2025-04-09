package com.intel.gpe.installer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import javax.swing.UIManager;
import com.intel.gpe.installer.panels.InstallPanel;

public class Installer {

    Properties orig;

    Properties newprop;

    String resourcedir;

    List<InstallPanel> panels = new ArrayList<InstallPanel>();

    public Installer() throws Exception {
        orig = new Properties();
        FileInputStream in = new FileInputStream(getProperty("properties.orig"));
        orig.load(in);
        in.close();
        newprop = new Properties();
        Enumeration e = orig.propertyNames();
        while (e.hasMoreElements()) {
            String propname = (String) e.nextElement();
            newprop.setProperty(propname, orig.getProperty(propname));
        }
        resourcedir = getProperty("resource.dir");
    }

    public String getProperty(String key) {
        String res = orig.getProperty(key);
        if (res == null) res = System.getProperty(key);
        return subst(res);
    }

    private String subst(String str) {
        if (str == null) return "";
        int idx1 = 0;
        while ((idx1 = str.indexOf("${")) >= 0) {
            int idx2 = str.indexOf('}', idx1);
            String propname = str.substring(idx1 + 2, idx2);
            String replacement = propname.startsWith("env.") ? subst(System.getenv().get(propname.substring("env.".length()))) : getProperty(propname);
            str = str.substring(0, idx1) + replacement + str.substring(idx2 + 1);
        }
        return str;
    }

    public void populateProperty(String key, String value) {
        newprop.setProperty(key, value);
    }

    public void onFinish() throws Exception {
        File f = new File(getProperty("properties.file"));
        f.delete();
        FileOutputStream out = new FileOutputStream(f);
        newprop.store(out, null);
        out.close();
    }

    public URL getHTMLResource(String name) {
        try {
            return new URL("file:///" + resourcedir + java.io.File.separator + name + ".html");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getModified(String name) {
        for (InstallPanel p : panels) {
            String val = p.getModified(name);
            if (val != null) return val;
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Installer installer = new Installer();
            String panelsStr = System.getProperty("panels");
            List<InstallPanel> panels = new ArrayList<InstallPanel>();
            installer.panels = panels;
            if (panelsStr != null) {
                StringTokenizer tk = new StringTokenizer(panelsStr, ", \t\n\r");
                while (tk.hasMoreTokens()) {
                    String clazzStr = tk.nextToken();
                    Class clazz = Class.forName(clazzStr);
                    Constructor constr = clazz.getConstructor(Installer.class);
                    InstallPanel p = (InstallPanel) constr.newInstance(installer);
                    panels.add(p);
                }
            } else {
                System.err.println("No panels loaded");
                System.exit(0);
            }
            InstallerFrame frame = new InstallerFrame(installer, panels);
            frame.setSize(1000, 700);
            frame.setLocation(10, 10);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
