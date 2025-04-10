package net.sourceforge.processdash.i18n;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/** Loads translations from a user-contributed zipfile, and merges them with
 * translations that are already present in the dashboard source tree.
 */
public class AddTranslations extends MatchingTask {

    private static final String BROKEN_SUFFIX = "!broken";

    private boolean verbose = false;

    private File dir;

    public void setDir(File d) {
        dir = d;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void execute() throws BuildException {
        DirectoryScanner ds = getDirectoryScanner(dir);
        String[] srcFiles = ds.getIncludedFiles();
        for (int i = 0; i < srcFiles.length; i++) {
            try {
                addTranslationsFromFile(new File(dir, srcFiles[i]));
            } catch (IOException ioe) {
                if (verbose) ioe.printStackTrace(System.out);
                System.out.println("'" + srcFiles[i] + "' does not appear to " + "be a valid translations zipfile - skipping.");
            }
        }
    }

    private void addTranslationsFromFile(File file) throws IOException {
        if (verbose) System.out.println("Looking for translations in " + file);
        ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
        File jFreeFile = new File(dir, ("lib/jfreechart" + localeId(file.getName()) + ".zip"));
        Map jFreeRes = loadJFreeResources(jFreeFile);
        boolean jFreeModified = false;
        ZipEntry entry;
        while ((entry = zipIn.getNextEntry()) != null) {
            if (entry.isDirectory()) continue;
            String filename = entry.getName();
            if (filename.indexOf("jrc-editor") != -1) {
                mergePropertiesFile(zipIn, "l10n-tool/src/" + terminalName(filename));
            } else if (filename.indexOf("jfree") != -1) {
                if (mergeJFreeProperties(jFreeRes, zipIn, filename)) jFreeModified = true;
            } else if (filename.startsWith("Templates") && filename.endsWith(".properties")) {
                mergePropertiesFile(zipIn, filename);
            } else if (!filename.equals("ref.zip") && !filename.equals("save-tags.txt")) {
                System.out.println("Warning - unrecognized file '" + filename + "'");
            }
        }
        if (jFreeModified) saveJFreeResources(jFreeFile, jFreeRes);
        zipIn.close();
    }

    private String localeId(String filename) {
        int underlinePos = filename.indexOf('_');
        int dotPos = filename.indexOf('.', underlinePos);
        return filename.substring(underlinePos, dotPos);
    }

    private String terminalName(String entryName) {
        int slashPos = entryName.lastIndexOf('/');
        if (slashPos == -1) slashPos = entryName.lastIndexOf('\\');
        if (slashPos == -1) return entryName; else return entryName.substring(slashPos + 1);
    }

    private Map loadJFreeResources(File jFreeFile) throws IOException {
        Map result = new HashMap();
        if (jFreeFile.exists()) {
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(jFreeFile));
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String filename = entry.getName();
                if (filename.toLowerCase().endsWith(".properties")) {
                    Properties p = new SortedProperties();
                    p.load(zipIn);
                    if (!p.isEmpty()) result.put(filename, p);
                }
            }
            zipIn.close();
        }
        return result;
    }

    private boolean mergeJFreeProperties(Map jFreeRes, ZipInputStream zipIn, String filename) throws IOException {
        if (JFREE_EXISTING_LANGUAGES.contains(localeId(filename).substring(1))) return false;
        filename = maybeRenameJFreeFile(filename);
        Properties incoming = new Properties();
        incoming.load(zipIn);
        Properties original = (Properties) jFreeRes.get(filename);
        if (original == null) original = new Properties();
        Properties merged = new SortedProperties();
        merged.putAll(original);
        merged.putAll(incoming);
        if (original.equals(merged)) {
            if (verbose) System.out.println("    No new properties in '" + filename + "'");
            return false;
        } else {
            if (verbose) System.out.print("    ");
            System.out.println("Updating '" + filename + "'");
            jFreeRes.put(filename, merged);
            return true;
        }
    }

    private String maybeRenameJFreeFile(String filename) {
        for (int i = 0; i < JFREE_FILE_RENAMES.length; i++) {
            String oldPrefix = JFREE_FILE_RENAMES[i][0];
            String newPrefix = JFREE_FILE_RENAMES[i][1];
            if (filename.startsWith(oldPrefix)) {
                return newPrefix + filename.substring(oldPrefix.length());
            }
        }
        return filename;
    }

    private static final String[][] JFREE_FILE_RENAMES = { { "org/jfree/chart/ui/LocalizationBundle", "org/jfree/chart/editor/LocalizationBundle" } };

    private void saveJFreeResources(File jFreeFile, Map jFreeRes) throws IOException {
        if (jFreeRes.isEmpty()) return;
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(jFreeFile));
        for (Iterator i = jFreeRes.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            String filename = (String) e.getKey();
            Properties p = (Properties) e.getValue();
            zipOut.putNextEntry(new ZipEntry(filename));
            p.store(zipOut, PROP_FILE_HEADER);
            zipOut.closeEntry();
        }
        zipOut.close();
    }

    private void mergePropertiesFile(ZipInputStream zipIn, String filename) throws IOException {
        Properties incoming = new Properties();
        incoming.load(zipIn);
        File destFile = new File(dir, filename);
        Properties original = new Properties();
        if (destFile.exists()) original.load(new FileInputStream(destFile));
        Properties merged = new SortedProperties();
        merged.putAll(original);
        for (Iterator i = incoming.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            if (key.endsWith(BROKEN_SUFFIX)) {
                String intendedKey = key.substring(0, key.length() - BROKEN_SUFFIX.length());
                if (original.containsKey(intendedKey)) continue;
            }
            merged.put(key, value);
        }
        if (original.equals(merged)) {
            if (verbose) System.out.println("    No new properties in '" + filename + "'");
            return;
        }
        if (verbose) System.out.print("    ");
        System.out.println("Updating '" + filename + "'");
        FileOutputStream out = new FileOutputStream(destFile);
        merged.store(out, PROP_FILE_HEADER);
        out.close();
    }

    private static final String PROP_FILE_HEADER = "Process Dashboard Resource Bundle";

    private static final Set<String> JFREE_EXISTING_LANGUAGES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("de", "es", "fr", "it", "nl", "pl", "pt", "ru", "zh")));

    private class SortedProperties extends Properties {

        public synchronized Enumeration keys() {
            TreeSet keys = new TreeSet();
            for (Enumeration e = super.keys(); e.hasMoreElements(); ) keys.add(e.nextElement());
            return Collections.enumeration(keys);
        }
    }
}
