package org.xBaseJ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xBaseJ.fields.DateField;

public class Util extends Object {

    static Log logr = LogFactory.getLog(Util.class);

    static boolean log4JConfigured = false;

    static java.util.Properties props = new java.util.Properties();

    static File propFile = null;

    static InputStream propIS = null;

    static long lastUpdate = -1;

    static boolean recheckProperties;

    private static String servletContextPath = null;

    private static boolean x86Architecture = ((System.getProperty("os.arch").indexOf("86") == 0) && (System.getProperty("os.arch").compareTo("vax") != 0));

    static {
        try {
            String test = getxBaseJProperty("checkPropertyFileForChanges");
            if (test.length() > 0) recheckProperties = (test.compareToIgnoreCase("true") == 0 || test.compareToIgnoreCase("yes") == 0);
        } catch (Exception e) {
            logr.error(e.getMessage(), e);
        } finally {
            logr.debug("recheckProperties is " + recheckProperties);
        }
    }

    public static long x86(long in) {
        if (x86Architecture) return in;
        boolean negative = false;
        long is;
        long first, second, third, fourth, fifth, sixth, seventh, eigth, isnt, save;
        save = in;
        if (in < 0) {
            negative = true;
        }
        first = in >>> 56;
        if (negative) first = (byte) in & 0x7f;
        if (negative == true) first |= 0x80;
        isnt = first << 56;
        save = in - isnt;
        second = save >>> 48;
        isnt = second << 48;
        save = save - isnt;
        third = save >>> 40;
        isnt = third << 40;
        save = save - isnt;
        fourth = save >>> 32;
        isnt = fourth << 32;
        save = save - isnt;
        fifth = save >>> 24;
        isnt = fifth << 24;
        save = save - isnt;
        sixth = save >>> 16;
        isnt = sixth << 16;
        save = save - isnt;
        seventh = save >>> 8;
        isnt = seventh << 8;
        save = save - isnt;
        eigth = save;
        is = (eigth << 56) + (seventh << 48) + (sixth << 40) + (fifth << 32) + (fourth << 24) + (third << 16) + (second << 8) + first;
        return is;
    }

    public static int x86(int in) {
        if (x86Architecture) {
            return in;
        }
        boolean negative = false;
        int is;
        int first, second, third, fourth, save;
        save = in;
        if (in < 0) {
            negative = true;
            in &= 0x7fffffff;
        }
        first = in >>> 24;
        if (negative == true) first |= 0x80;
        in = save & 0x00ff0000;
        second = in >>> 16;
        in = save & 0x0000ff00;
        third = in >>> 8;
        fourth = save & 0x000000ff;
        is = (fourth << 24) + (third << 16) + (second << 8) + first;
        return is;
    }

    public static short x86(short in) {
        if (x86Architecture) return in;
        short is, save = in;
        boolean negative = false;
        int first, second;
        if (in < 0) {
            negative = true;
            in &= 0x7fff;
        }
        first = in >>> 8;
        if (negative == true) first |= 0x80;
        second = save & 0x00ff;
        is = (short) ((second << 8) + first);
        return is;
    }

    public static double doubleDate(DateField d) {
        return doubleDate(d.get());
    }

    public static double doubleDate(String s) {
        int i;
        if (s.trim().length() == 0) return 1e100;
        int year = Integer.parseInt(s.substring(0, 4));
        if (year == 0) return 1e100;
        int days[] = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        int month = Integer.parseInt(s.substring(4, 6));
        int day = Integer.parseInt(s.substring(6, 8));
        int daydif = 2378497;
        if ((year / 4) == 0) days[2] = 29;
        if (year > 1799) {
            daydif += day - 1;
            for (i = 2; i <= month; i++) daydif += days[i - 1];
            daydif += (year - 1800) * 365;
            daydif += ((year - 1800) / 4);
            daydif -= ((year - 1800) % 100);
            if (year > 1999) daydif++;
        } else {
            daydif -= (days[month] - day + 1);
            for (i = 11; i >= month; i--) daydif -= days[i + 1];
            daydif -= (1799 - year) * 365;
            daydif -= (1799 - year) / 4;
        }
        Integer retInt = new Integer(daydif);
        return retInt.doubleValue();
    }

    /**
     * normlizes the string to remove XML characters.
     *
     * @param inString String to be normalized
     * @return String normalized String
     */
    public static String normalize(String inString) {
        int i;
        StringBuffer sb = new StringBuffer();
        for (i = 0; i < inString.length(); i++) switch(inString.charAt(i)) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case '\'':
                sb.append("&apos;");
                break;
            default:
                if (inString.charAt(i) < ' ') sb.append(inString.charAt(i) + ''); else sb.append(inString.charAt(i));
        }
        return new String(sb);
    }

    public static String getxBaseJProperty(String inString) throws IOException {
        synchronized (props) {
            if (propFile == null && propIS == null) {
                propIS = getPropertiesFile();
                if (propIS != null) {
                    lastUpdate = propFile.lastModified();
                    logr.debug("loading properties");
                    props.load(propIS);
                }
            } else if (propFile != null && recheckProperties == true) {
                if (lastUpdate < propFile.lastModified()) {
                    props = new java.util.Properties();
                    propIS = new FileInputStream(propFile);
                    lastUpdate = propFile.lastModified();
                    logr.debug("loading properties, because modified");
                    props.load(propIS);
                }
            }
            String rets = props.getProperty(inString);
            if (rets != null) rets = rets.trim(); else rets = "";
            return rets;
        }
    }

    public static void setxBaseJProperty(String inName, String inValue) throws IOException {
        synchronized (props) {
            props.put(inName, inValue);
        }
    }

    /**
     * returns true if org.xBaseJ.property dontTrimFields is "true" or "yes"
     * @return boolean false or true
     */
    public static boolean dontTrimFields() {
        String prop;
        try {
            prop = getxBaseJProperty("trimFields");
        } catch (IOException e) {
            return false;
        }
        if (prop.toLowerCase().compareTo("yes") == 0) return true;
        if (prop.toLowerCase().compareTo("true") == 0) return true;
        return false;
    }

    /**
     * returns true if org.xBaseJ.property fieldFilled is "true" or "yes"
     * @return boolean false or true
     */
    public static boolean fieldFilledWithSpaces() {
        String prop;
        try {
            prop = getxBaseJProperty("fieldFilledWithSpaces");
        } catch (IOException e) {
            return false;
        }
        if (prop.toLowerCase().compareTo("yes") == 0) return true;
        if (prop.toLowerCase().compareTo("true") == 0) return true;
        return false;
    }

    /**
     * static class method to build the properties file input stream see
     * org.xBaseJ.properties file
     * <ul>search for org.xBaseJ.properties file
     * <li>as specified by system property xBase.properties
     * <li>local directory
     * <li>user.home directory
     * <li>java.home directory
     * <li>classpath environment variable
     * </ul>
     *
     * @return InputStream org.xBaseJ.properties file
     * @throws xBaseJException
     *             io error most likely
     */
    private static InputStream getPropertiesFile() {
        String xBaseJPropertiesFileName = "org.xBaseJ.properties";
        String _fileName = System.getProperty(xBaseJPropertiesFileName);
        if (_fileName != null) xBaseJPropertiesFileName = _fileName;
        File f3, f2, f1 = new File(xBaseJPropertiesFileName);
        if (f1.exists()) try {
            propFile = f1;
            logr.debug("properties file loaded from " + f1.getAbsolutePath());
            return new FileInputStream(f1);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            return null;
        } else {
            xBaseJPropertiesFileName = System.getProperty("user.home") + "/org.xBaseJ.properties";
            f2 = new File(xBaseJPropertiesFileName);
            if (f2.exists()) try {
                propFile = f2;
                logr.debug("properties file loaded from " + f2.getAbsolutePath());
                return new FileInputStream(f2);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
                return null;
            } else {
                xBaseJPropertiesFileName = System.getProperty("java.home") + "/org.xBaseJ.properties";
                f3 = new File(xBaseJPropertiesFileName);
                if (f3.exists()) {
                    try {
                        propFile = f3;
                        logr.debug("properties file loaded from " + f3.getAbsolutePath());
                        return new FileInputStream(f3);
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                        return null;
                    }
                } else {
                    InputStream is = (new Util()).getClass().getResourceAsStream("/org.xBaseJ.properties");
                    if (is == null) {
                        if (servletContextPath == null) is = ClassLoader.getSystemClassLoader().getResourceAsStream("org.xBaseJ.properties"); else {
                            try {
                                is = new FileInputStream(servletContextPath + "/org.xBaseJ.properties");
                                logr.debug("properties file loaded from servlet context path " + servletContextPath + "/org.xBaseJ.properties");
                                propFile = new File(servletContextPath + "/org.xBaseJ.properties");
                            } catch (IOException ioe) {
                                logr.debug("Searched for org.xBaseJ.properties as " + servletContextPath + "/org.xBaseJ.properties");
                            }
                        }
                    }
                    if (is != null) {
                        logr.debug("properties file loaded from classpath");
                        return is;
                    } else {
                        logr.debug("Searched for org.xBaseJ.properties as " + f1.getAbsolutePath());
                        logr.debug("Searched for org.xBaseJ.properties as " + f2.getAbsolutePath());
                        logr.debug("Searched for org.xBaseJ.properties as " + f3.getAbsolutePath());
                        logr.debug("Searched for org.xBaseJ.properties in classpath environment variable");
                        return null;
                    }
                }
            }
        }
    }

    /**
     * use this if you need to reset the property file which is usually left
     * open
     *
     *
     */
    public static void closexBaseJProperty() {
        synchronized (props) {
            propFile = null;
            if (propIS != null) {
                try {
                    propIS.close();
                } catch (IOException e) {
                    logr.fatal(e.getMessage(), e);
                }
                propIS = null;
            }
            props = new java.util.Properties();
        }
    }

    public static void copyFile(String inputFile, String outputFile) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        FileOutputStream fos = new FileOutputStream(outputFile);
        for (int b = fis.read(); b != -1; b = fis.read()) fos.write(b);
        fos.close();
        fis.close();
    }

    /**
	 * @return boolean true if property says use shared locks
	 */
    public static boolean useSharedLocks() {
        String usl;
        try {
            usl = getxBaseJProperty("useSharedLocks").toLowerCase();
        } catch (IOException e) {
            logr.error(e.getMessage(), e);
            usl = "false";
        }
        return (usl.compareTo("true") == 0);
    }
}
