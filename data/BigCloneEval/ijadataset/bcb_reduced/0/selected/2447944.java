package com.quikj.application.utilities.postinstall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * 
 * @author amit
 */
public class ConfigParams {

    private ArrayList list = new ArrayList();

    /** Holds value of property errorMessage. */
    private String errorMessage = "";

    /** Creates a new instance of ConfigParams */
    public ConfigParams() {
    }

    public String displayEnteredValues() {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = list.iterator();
        while (iter.hasNext() == true) {
            ConfigElement e = (ConfigElement) iter.next();
            int d = e.getDisplay();
            if (d != ConfigElement.DISPLAY_NONE) {
                buffer.append(e.getParamName() + " : ");
                if (d == ConfigElement.DISPLAY_ALL) {
                    buffer.append(e.getParamValue());
                } else {
                    int len = e.getParamValue().length();
                    for (int i = 0; i < len; i++) {
                        buffer.append('*');
                    }
                }
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }

    public ConfigElement find(String name) {
        Iterator iter = list.iterator();
        while (iter.hasNext() == true) {
            ConfigElement element = (ConfigElement) iter.next();
            if (element.getParamName().equals(name) == true) {
                return element;
            }
        }
        return null;
    }

    private int findIndex(String name) {
        Iterator iter = list.iterator();
        int i = 0;
        while (iter.hasNext() == true) {
            ConfigElement element = (ConfigElement) iter.next();
            if (element.getParamName().equals(name) == true) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
	 * Getter for property errorMessage.
	 * 
	 * @return Value of property errorMessage.
	 * 
	 */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
	 * Getter for property list.
	 * 
	 * @return Value of property list.
	 * 
	 */
    public java.util.ArrayList getList() {
        return list;
    }

    private void list(File base, String suffix, ArrayList list) throws FileNotFoundException, IOException {
        File[] listing = base.listFiles();
        for (int i = 0; i < listing.length; i++) {
            if ((listing[i].getName().equals(".") == true) || (listing[i].getName().equals("..") == true)) {
                continue;
            }
            if (listing[i].isDirectory() == true) {
                list(listing[i], suffix, list);
                continue;
            }
            if (listing[i].getName().endsWith(suffix) == true) {
                String orig_abs = listing[i].getAbsolutePath();
                String config_abs = orig_abs.substring(0, (orig_abs.length() - suffix.length()));
                list.add(config_abs);
                if ((new File(config_abs)).exists() == true) {
                    FileUtils.copy(config_abs, config_abs + ".old");
                }
                FileUtils.copy(orig_abs, config_abs);
            }
        }
    }

    public void put(ConfigElement element) {
        int index = findIndex(element.getParamName());
        if (index == -1) {
            list.add(element);
        } else {
            list.set(index, element);
        }
    }

    public void remove(String name) {
        ConfigElement e = find(name);
        if (e != null) {
            list.remove(e);
        }
    }

    public boolean replace(String base, String suffix, ScreenPrinterInterface out) {
        try {
            File top = new File(base);
            if (top.isDirectory() == false) {
                errorMessage = "The top-level folder " + base + " is not a folder";
                return false;
            }
            ArrayList file_list = new ArrayList();
            list(top, suffix, file_list);
            Iterator iter = list.iterator();
            while (iter.hasNext() == true) {
                ConfigElement e = (ConfigElement) iter.next();
                String replace_with = e.getReplacePattern();
                if (replace_with == null) {
                    continue;
                }
                ReplaceString rep = new ReplaceString();
                rep.setStringToReplace(replace_with);
                rep.setStringNewValue(e.getParamValue());
                Iterator it = file_list.iterator();
                while (it.hasNext() == true) {
                    rep.addFile((String) it.next());
                }
                rep.replace();
                String error = rep.getErrorMessage();
                if (error.length() > 0) {
                    out.println("Error replacing values for " + e.getParamName() + ": " + error);
                    continue;
                }
                out.println("Replaced value for " + e.getParamName() + " in " + rep.getNumReplaced() + " places");
            }
            return true;
        } catch (Exception ex) {
            errorMessage = "IO error occured while configuring system files";
            return false;
        }
    }

    public boolean save(String folder, String filename) {
        Properties properties = new Properties();
        Iterator iter = list.iterator();
        while (iter.hasNext() == true) {
            ConfigElement e = (ConfigElement) iter.next();
            int d = e.getDisplay();
            properties.setProperty(e.getParamName(), e.getParamValue());
        }
        try {
            File file = new File(folder, filename);
            FileOutputStream fos = new FileOutputStream(file);
            properties.store(fos, "Ace Operator Install Parameters");
            fos.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
}
