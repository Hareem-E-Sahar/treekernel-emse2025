package com.indigen.victor.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.cocoon.environment.Session;
import org.w3c.dom.Node;
import com.indigen.victor.actions.VictorAction;
import com.indigen.victor.core.LogEnabled;

/**
 * A victor exporter allows to deliver to a specific data system. Developers should consider
 * subclassing {@link com.indigen.victor.export.StatefulExporter} that provides additional 
 * services
 */
public abstract class Exporter extends LogEnabled {

    /**
	 * The action that created the exporter
	 */
    protected VictorAction action;

    /**
	 * The unique identifier for the exporter
	 */
    protected String exporterId;

    /**
	 * The base url to access the resulting export
	 */
    protected String baseUrl;

    /**
	 * Definition of unix systems for some specific operations
	 */
    static HashSet unixOSes = new HashSet();

    static {
        unixOSes.add("AIX");
        unixOSes.add("Digital Unix");
        unixOSes.add("FreeBSD");
        unixOSes.add("HP UX");
        unixOSes.add("Irix");
        unixOSes.add("Linux");
        unixOSes.add("Solaris");
    }

    /**
	 * Exporter constructor
	 * @param action the action that creates the object
	 */
    public Exporter(VictorAction action) {
        this.action = action;
        exporterId = "" + action.getRandom();
        baseUrl = action.getUrlParameter("baseurl");
        action.getActionLogger().debug("Created Exporter id " + exporterId);
    }

    /**
	 * Assign a Victor action to this exporter
	 * @param action
	 */
    protected void setAction(VictorAction action) {
        this.action = action;
    }

    /**
	 * Get the object's Victor action
	 * @return
	 */
    public VictorAction getAction() {
        return action;
    }

    /**
	 * Get the default path to access export repository
	 * @return
	 */
    protected String getDefaultPath() {
        return "${root}/var/exports/" + getExporterName() + "/${area}/${oid}";
    }

    /**
	 * Get the path to access export repository. The export path can be defined in the 
	 * application configuration property file.
	 * @return
	 */
    protected String getBasePath() {
        Properties config = action.getConfig();
        String path = config.getProperty("exporter." + getExporterName() + ".path.area." + action.getArea());
        if (path == null) path = config.getProperty("exporter." + getExporterName() + ".path");
        if (path == null) path = config.getProperty("exporter.path", getDefaultPath());
        path = path.replaceAll("\\$\\{root\\}", action.getRootDirectory().getAbsolutePath());
        path = path.replaceAll("\\$\\{area\\}", action.getArea());
        path = path.replaceAll("\\$\\{oid\\}", action.getOid());
        return path;
    }

    /**
	 * Get the maximum number of resulting instances for this exporter
	 * @return
	 */
    protected int getMaxInstances() {
        Properties config = action.getConfig();
        String maxStr = config.getProperty("exporter." + getExporterName() + ".maxcount.area." + action.getArea());
        if (maxStr == null) maxStr = config.getProperty("exporter." + getExporterName() + ".maxcount");
        if (maxStr == null) maxStr = config.getProperty("exporter.maxcount", "-1");
        try {
            return Integer.parseInt(maxStr);
        } catch (NumberFormatException e) {
            getLogger().error("Invalid max count in configuration");
            return -1;
        }
    }

    /**
	 * Check whether the maximum number of exports have been reached
	 * @param exportOut
	 * @return
	 */
    protected boolean checkMaxExportsCount(Map exportOut) {
        int maxCount = getMaxInstances();
        if (maxCount >= 0) {
            int count = getInstances().size();
            if (count >= maxCount) {
                exportOut.put("succeed", "false");
                exportOut.put("reasonkey", "export.error.maxcountreached");
                return false;
            }
        }
        return true;
    }

    /**
	 * Check whether an export is currently in progress
	 * @param exportOut
	 * @return
	 */
    protected boolean checkExportInProgress(Map exportOut) {
        if (getCurrentExporter(action) == null) return true; else {
            exportOut.put("reasonkey", "export.error.exportinprogress");
            return false;
        }
    }

    /**
	 * Replace placeholder in the given string. A placeholder is under the form 
	 * <code>${xxxx}</code>
	 * @param str the original string
	 * @param placeholder the placeholder to be replaced, without the ${} 
	 * @param replacement the string to put in place of the placeholder
	 * @return the new string
	 */
    public static String replacePlaceholders(String str, String placeholder, String replacement) {
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("(\\$\\{" + placeholder + "\\})");
        int lastIndex = 0;
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            sb.append(str.substring(lastIndex, m.start()));
            sb.append(replacement);
            lastIndex = m.end();
        }
        sb.append(str.substring(lastIndex));
        return sb.toString();
    }

    /**
     * Replace standard placeholders in a string. See 
     * {@link #replaceStandardPlaceholders(VictorAction, String)}
     * @param str
     * @return
     */
    public String replaceStandardPlaceholders(String str) {
        return replaceStandardPlaceholders(action, str);
    }

    /**
     * Replace standard placeholder in a string. Standard placeholders are:<ul>
     * <li>oid: the project identifier</li>
     * <li>area: the authorization area</li>
     * <li>root: the victor root directory</li>
     * <li>base: the project files repository</li>
     * </ul>
     * @param action
     * @param str
     * @return
     */
    public static String replaceStandardPlaceholders(VictorAction action, String str) {
        str = replacePlaceholders(str, "oid", action.getOid());
        str = replacePlaceholders(str, "area", action.getArea());
        str = replacePlaceholders(str, "root", action.getRootDirectory().getAbsolutePath());
        str = replacePlaceholders(str, "base", action.getBaseDirectory().getAbsolutePath());
        return str;
    }

    /**
     * Must be overridden to return the short name of the exporter. The name should
     * not contain spaces of special characters
     * @return
     */
    public abstract String getExporterName();

    /**
	 * Must be overridden to return the name of the exporter that will be read by the user
	 * @return
	 */
    public abstract String getExporterTitle();

    /**
	 * Must be overridden to return the list of existing exports for this exporter type
	 * @return
	 */
    public abstract List getInstances();

    /**
	 * Must overridden to carry out the actual export.
	 * @param exportOut the map containing parameters for this export
	 */
    public abstract void export(Map exportOut);

    /**
	 * Hold available exporter class names
	 */
    static Map exporterClasses = null;

    /**
	 * Load the exporter class in the JVM
	 * @param action
	 */
    protected static void loadClasses(VictorAction action) {
        Properties config = action.getConfig();
        String exportClassesStr = config.getProperty("exporter.classes", "");
        String[] exportClasses = exportClassesStr.split(",");
        exporterClasses = new Hashtable();
        for (int i = 0; i < exportClasses.length; i++) {
            String exportClassStr = exportClasses[i];
            if (exportClassesStr.length() == 0) continue;
            try {
                ClassLoader classLoader = Exporter.class.getClassLoader();
                Class exportClass = Class.forName(exportClassStr, true, classLoader);
                Constructor constructor = exportClass.getConstructor(new Class[] { VictorAction.class });
                Exporter exporter = (Exporter) constructor.newInstance(new Object[] { action });
                exporterClasses.put(exporter.getExporterName(), exporter);
            } catch (ClassNotFoundException e) {
                action.getActionLogger().error("Exporter.loadClasses(): Exporter class not found " + exportClassStr);
            } catch (NoSuchMethodException e) {
                action.getActionLogger().error("Exporter.loadClasses(): No such method on " + exportClassStr);
            } catch (InvocationTargetException e) {
                action.getActionLogger().error("Exporter.loadClasses(): Invocation exception " + exportClassStr, e);
            } catch (IllegalAccessException e) {
                action.getActionLogger().error("Exporter.loadClasses(): Illegal access exception " + exportClassStr, e);
            } catch (InstantiationException e) {
                action.getActionLogger().error("Exporter.loadClasses(): Instantiation exception " + exportClassStr, e);
            }
        }
    }

    /**
	 * Instantiate a new exporter of the given type
	 * @param action
	 * @param exporterName
	 * @return
	 */
    public static Exporter getExporter(VictorAction action, String exporterName) {
        if (exporterClasses == null) loadClasses(action);
        Exporter proto = (Exporter) exporterClasses.get(exporterName);
        if (proto == null) {
            action.getActionLogger().warn("Exporter.getExporter(): no such exporter " + exporterName);
            return null;
        }
        try {
            Constructor constructor = proto.getClass().getConstructor(new Class[] { VictorAction.class });
            Exporter exporter = (Exporter) constructor.newInstance(new Object[] { action });
            return exporter;
        } catch (NoSuchMethodException e) {
            action.getActionLogger().error("Exporter.getExporter(): No such method on " + proto.getClass().getName());
        } catch (InvocationTargetException e) {
            action.getActionLogger().error("Exporter.getExporter(): Invocation exception " + proto.getClass().getName(), e);
        } catch (IllegalAccessException e) {
            action.getActionLogger().error("Exporter.getExporter(): Illegal access exception " + proto.getClass().getName(), e);
        } catch (InstantiationException e) {
            action.getActionLogger().error("Exporter.getExporter(): Instantiation exception " + proto.getClass().getName(), e);
        }
        return null;
    }

    /**
	 * Return the list of available exporters
	 * @param action
	 * @return
	 */
    public static List getExporterNames(VictorAction action) {
        if (exporterClasses == null) loadClasses(action);
        List names = new Vector(exporterClasses.keySet());
        Collections.sort(names);
        return names;
    }

    /**
	 * Put the current exporter object into servlet session
	 *
	 */
    public void toSession() {
        Session session = action.getSession();
        session.setAttribute("exportsession", this);
    }

    /**
	 * Get the current exporter from servlet session with the current id
	 * @param action
	 * @param exporterId
	 * @return
	 */
    public static Exporter getCurrentExporter(VictorAction action, String exporterId) {
        Exporter exporter = getCurrentExporter(action);
        if (exporter == null) {
            action.getActionLogger().warn("getCurrentExporter(): no current exporter");
            return null;
        }
        if (!exporter.getExporterId().equals(exporterId)) {
            action.getActionLogger().warn("getCurrentExporter(): expected id " + exporter.getExporterId() + " got " + exporterId);
            return null;
        }
        return exporter;
    }

    /**
	 * Get the current exporter from servlet session
	 * @param action
	 * @return
	 */
    public static Exporter getCurrentExporter(VictorAction action) {
        Session session = action.getSession();
        Exporter exporter = (Exporter) session.getAttribute("exportsession");
        if (exporter != null) {
            exporter.setAction(action);
        }
        return exporter;
    }

    /**
	 * Get the exporter instance identifier
	 * @return
	 */
    public String getExporterId() {
        return exporterId;
    }

    /**
	 * Start an export operation
	 * @param action
	 * @param exporterName
	 * @return
	 */
    public static Map startExport(VictorAction action, String exporterName) {
        Map exportOut = new Hashtable();
        exportOut.put("state", "exporting");
        Exporter exporter = getExporter(action, exporterName);
        if (exporter == null) {
            exportOut.put("state", "failed");
            exportOut.put("reasonkey", "export.error.nosuchexporter");
            return exportOut;
        }
        exportOut.put("exportid", exporter.getExporterId());
        exporter.toSession();
        exporter.export(exportOut);
        return exportOut;
    }

    /**
	 * Remove an existing export
	 * @param action
	 * @param exporterName
	 * @param exportName
	 */
    public static void removeExport(VictorAction action, String exporterName, String exportName) {
        Exporter exporter = getExporter(action, exporterName);
        if (exporter == null) {
            action.getActionLogger().error("removeExport(" + exporterName + "," + exportName + "): no exporter for that name");
            return;
        }
        exporter.removeExport(exportName);
    }

    /**
	 * Step to the next export configuration page
	 * @param exportOut
	 * @param data
	 */
    public void stepExport(Map exportOut, Node data) {
        exportOut.put("state", "failed");
        exportOut.put("nextpage", "nostepdefined");
    }

    /**
	 * Abort the current export
	 * @param exportOut
	 */
    public void abortExport(Map exportOut) {
        exportOut.put("exportid", exporterId);
        exportOut.put("state", "aborted");
        exportOut.put("nextpage", "exportaborted");
    }

    /**
	 * Must be overriden to implement actual export removal
	 * @param exportName
	 */
    public void removeExport(String exportName) {
    }

    /**
	 * Get the next URL to be called in the export configuration
	 * @param exportOut
	 * @param nextPage
	 * @return
	 */
    public Map getNextPageSrc(Map exportOut, String nextPage) {
        Map np = new Hashtable();
        np.put("src", nextPage + ".jx");
        if (nextPage.endsWith(".xul")) {
            np.put("type", "jxxul");
        } else if (nextPage.endsWith("*.html")) {
            np.put("type", "jxhtml");
            np.put("serializer", "html");
        } else if (nextPage.endsWith("*.xml")) {
            np.put("type", "jxxml");
            np.put("serializer", "xml");
        } else {
            np.put("src", nextPage);
            np.put("type", "read");
        }
        return np;
    }

    /**
	 * Utility to copy recursively a directory
	 * @param from
	 * @param baseTo
	 * @param name
	 * @return
	 */
    public boolean copy(File from, File baseTo, String name) {
        if (!from.exists()) {
            action.getActionLogger().error("copy(" + from.getAbsolutePath() + "," + baseTo.getAbsolutePath() + ") from directory does not exist");
            return false;
        }
        if (name == null) name = from.getName();
        File to = new File(baseTo, name);
        if (from.isDirectory()) {
            if (to.exists() == false && to.mkdirs() == false) {
                action.getActionLogger().error("copy(" + from.getAbsolutePath() + "," + baseTo.getAbsolutePath() + ") cannot create directory: " + to.getAbsolutePath());
                return false;
            } else {
                File[] files = from.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].getAbsolutePath().endsWith(".jx")) {
                        if (copy(files[i], to, null) == false) return false;
                    }
                }
            }
        } else {
            if (copyFile(from, to) == false) return false;
        }
        return true;
    }

    /**
	 * Utility to copy a file
	 * @param from
	 * @param to
	 * @return
	 */
    public boolean copyFile(File from, File to) {
        try {
            FileOutputStream fos = new FileOutputStream(to);
            FileInputStream fis = new FileInputStream(from);
            byte[] buf = new byte[1024];
            while (true) {
                int n = fis.read(buf);
                if (n <= 0) {
                    break;
                }
                fos.write(buf, 0, n);
            }
            fos.flush();
            fos.close();
            fis.close();
            return true;
        } catch (Exception e) {
            action.getActionLogger().error("copyFile(" + from.getAbsolutePath() + "," + to.getAbsolutePath() + ") error: " + e);
            return false;
        }
    }

    /**
     * Recursively remove the given directory
     * @param dir
     */
    public static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) deleteDirectory(files[i]); else files[i].delete();
        }
        dir.delete();
    }

    /**
     * If the system permits it (unix systems), set the writable flag on the given file path
     * @param path
     */
    protected void setWritable(String path) {
        Properties sysProps = System.getProperties();
        String os = sysProps.getProperty("os.name");
        if (!unixOSes.contains(os)) {
            getLogger().warn("setWriteable(): cannot set file permissions on non-unix system. OS=" + os);
            return;
        }
        try {
            Process process = Runtime.getRuntime().exec("/bin/chmod -R a+rw " + path);
            int status = process.waitFor();
            if (status != 0) {
                getLogger().error("Changing permissions on " + path + " returned status: " + status);
            }
        } catch (IOException e) {
            getLogger().error("Failed changing permissions on " + path + ": " + e);
        } catch (InterruptedException e) {
            getLogger().error("Interruption while changing permissions on " + path + ": " + e);
        }
    }
}
