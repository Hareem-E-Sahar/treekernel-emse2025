package ncsu.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.ImageIcon;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.net.SocketAppender;
import com.sun.xacml.ctx.Result;

/**
 * @author eemartin
 */
public class Util {

    public static final String MUTANT_DIR = "mutants";

    public static final String REQ_DIR = "requests";

    public static String tablesDir(String outputDir) {
        return outputDir + "/tables";
    }

    public static String policiesDir(String outputDir) {
        return outputDir + "/policies";
    }

    public static final String SUBJ_STATS_TAB = "/subject-stats.txt";

    public static final String EQUIV_MUTANTS_TAB = "/equiv-mutants.txt";

    public static final String MRGRV_EQUIV_MUTANTS_TAB = "/mrgrv-equiv-mutants.txt";

    public static final String NUM_MUTANTS_TAB = "/num-mutants.txt";

    public static final String PERF_MUTANTS_TAB = "/perf-mutants.txt";

    public static final String COVERAGE = "/coverage.txt";

    public static final String REQ_REDUCE = "/req-reduce.txt";

    public static final String FAULT_REDUCE = "/fault-reduce.txt";

    public static final String FAULT_REDUCE_OP = "/fault-reduce-op.txt";

    public static final String PERF_MUT_TEST = "/perf-mut-test.txt";

    public static final String IMAGE_DIR = "/polver/gui/icons/";

    public static final String SEP = System.getProperty("line.separator");

    public static final String LOG_FILE = "myssLog.txt";

    public static final Level LOG_LEVEL = Level.INFO;

    public static final String LOG_LAYOUT = "%d %-5p [%t] %-17c{2} (%13F:%L) %3x - %m%n";

    private static boolean alreadySetup = false;

    private static NumberFormat formatter;

    private Util() {
    }

    public static synchronized void setupLogger() {
        Util.setupLogger(false, LOG_LEVEL);
    }

    public static synchronized void setupLogger(Level level) {
        Util.setupLogger(false, level);
    }

    public static synchronized void setupLogger(boolean guiVisible) {
        Util.setupLogger(guiVisible, LOG_LEVEL);
    }

    /**
	 * Sets up log4j with default parameters
	 */
    public static synchronized void setupLogger(boolean guiVisible, Level level) {
        if (!alreadySetup) {
            try {
                alreadySetup = true;
                Logger root = Logger.getRootLogger();
                root.setLevel(level);
                if (guiVisible) {
                    SocketAppender eventViewer = new SocketAppender("localhost", 4445);
                    root.addAppender(eventViewer);
                    eventViewer.setName("EventViewerAppender");
                }
                RollingFileAppender fileAppender = new RollingFileAppender(new PatternLayout(LOG_LAYOUT), LOG_FILE, true);
                fileAppender.setName("FileAppender");
                fileAppender.setMaxBackupIndex(1);
                fileAppender.setMaxFileSize("1MB");
                ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN));
                consoleAppender.setName("ConsoleAppender");
                root.addAppender(fileAppender);
                root.addAppender(consoleAppender);
            } catch (Exception e) {
                System.err.println("Error configuring logger.");
                e.printStackTrace();
            }
        }
    }

    public static ImageIcon getIcon(String name, String descrip) {
        URL url = Util.class.getResource(IMAGE_DIR + name);
        if (url != null) {
            return new ImageIcon(url, descrip);
        }
        return null;
    }

    public static String getDecisionString(int decision) {
        switch(decision) {
            case Result.DECISION_PERMIT:
                return "Permit";
            case Result.DECISION_DENY:
                return "Deny";
            case Result.DECISION_INDETERMINATE:
                return "Indeterminate";
            case Result.DECISION_NOT_APPLICABLE:
                return "Not_Applicable";
        }
        return "Unknown";
    }

    public static void cleanDir(File f, Logger logger) {
        if (!f.exists()) {
            logger.error("Can not clean. File does not exist = " + f);
            return;
        }
        logger.debug("Deleting contents of " + f);
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                cleanDir(files[i], logger);
            }
            if (!files[i].delete()) {
                logger.error("Could not delete file " + files[i].getPath());
            }
        }
    }

    public static void texifyTablesDir(Logger logger, File tablesDir) {
        texify(tablesDir, logger);
    }

    public static void texify(File f, Logger logger) {
        if (!f.exists()) {
            logger.error("File does not exist " + f);
        }
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                texify(files[i], logger);
            } else {
                LaTeXiFier.writeTableFromFile(files[i]);
            }
        }
    }

    public static File createFile(String f, Logger logger) {
        return createFile(new File(f), logger);
    }

    public static File createFile(File f, Logger logger) {
        if (f == null) {
            return f;
        }
        if (f.exists()) {
            return f;
        }
        if (!f.getParentFile().exists()) {
            if (!f.getParentFile().mkdirs()) {
                logger.error("Could not create directory " + f.getParentFile());
            }
        }
        try {
            if (!f.createNewFile()) {
                logger.error("Could not create file " + f);
            }
        } catch (IOException e) {
            logger.error("Could not create file " + f, e);
        }
        return f;
    }

    public static NumberFormat getNumberFormat() {
        if (formatter == null) {
            formatter = NumberFormat.getInstance();
            if (formatter instanceof DecimalFormat) {
                DecimalFormat dformatter = (DecimalFormat) formatter;
                dformatter.setMaximumFractionDigits(2);
            }
        }
        return formatter;
    }
}
