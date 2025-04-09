package org.jmgl.nb.php;

import org.jmgl.php.messdetector.Settings;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jmgl.php.messdetector.BatchInterface;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * provides access to settings and batch for phpMD
 *
 * @version $Id$
 * @author Jens Radtke <nb@fin-sn.de>
 */
public final class MDManager {

    private MDManager() {
    }

    /**
   * test if valid settings saved
   *
   * @return
   */
    public static boolean isConfiguredAndEnabled() {
        return true;
    }

    /**
   * get stored settings or defaults
   *
   * @return
   */
    protected static Settings getSettings() {
        final Settings settings = Settings.getDefaultSettings("");
        settings.setPath(NbPreferences.forModule(MDManager.class).get("php.md.path", settings.getPath()));
        settings.setRules(NbPreferences.forModule(MDManager.class).get("php.md.rules", settings.getRules()));
        settings.setPriority(NbPreferences.forModule(MDManager.class).getInt("php.md.priority", settings.getPriority()));
        try {
            settings.setInclude(Pattern.compile(NbPreferences.forModule(MDManager.class).get("php.md.include", settings.getInclude().toString()), Pattern.CASE_INSENSITIVE));
        } catch (PatternSyntaxException exception) {
            Exceptions.printStackTrace(exception);
            settings.setInclude(null);
        }
        try {
            settings.setExclude(Pattern.compile(NbPreferences.forModule(MDManager.class).get("php.md.exclude", settings.getExclude().toString()), Pattern.CASE_INSENSITIVE));
        } catch (PatternSyntaxException exception) {
            Exceptions.printStackTrace(exception);
            settings.setExclude(null);
        }
        return settings;
    }

    /**
   * get stored settings or defaults
   *
   * @return
   */
    protected static void saveSettings(final Settings settings) {
        NbPreferences.forModule(MDManager.class).put("php.md.path", settings.getPath());
        NbPreferences.forModule(MDManager.class).put("php.md.rules", settings.getRules());
        NbPreferences.forModule(MDManager.class).putInt("php.md.priority", settings.getPriority());
        if (settings.hasInclude()) {
            NbPreferences.forModule(MDManager.class).put("php.md.include", settings.getInclude().pattern());
        } else {
            NbPreferences.forModule(MDManager.class).put("php.md.include", "");
        }
        if (settings.hasExclude()) {
            NbPreferences.forModule(MDManager.class).put("php.md.exclude", settings.getExclude().pattern());
        } else {
            NbPreferences.forModule(MDManager.class).put("php.md.exclude", "");
        }
    }

    /**
   * tries to get the phpmd batch interface from stored settings
   *
   * @return Messdetector with stored settings
   */
    public static BatchInterface getScanner() {
        return new BatchInterface(getSettings());
    }

    /**
   * tests if a given file is a valid mdbatch file, uses version parameter
   *
   * @param filename
   *
   * @return
   */
    public static boolean hasValidConfiguration(final Settings settings) {
        final File batchfile = new File(settings.getPath());
        if (!batchfile.exists() || !batchfile.isFile()) {
            return false;
        }
        try {
            final StringBuilder batchOutput = new StringBuilder();
            String line;
            final Process batchProcess = Runtime.getRuntime().exec(settings.getPath() + " --version", null, batchfile.getParentFile());
            final BufferedReader input = new BufferedReader(new InputStreamReader(batchProcess.getInputStream()));
            while ((line = input.readLine()) != null) {
                batchOutput.append(line);
            }
            input.close();
            return batchOutput.toString().trim().startsWith("PHPMD ");
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }
}
