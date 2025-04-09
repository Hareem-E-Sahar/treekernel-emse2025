import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import net.laubenberger.bogatyr.helper.HelperEnvironment;
import net.laubenberger.bogatyr.helper.HelperIO;
import net.laubenberger.bogatyr.helper.HelperString;
import net.laubenberger.bogatyr.helper.launcher.LauncherFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class to parse Java files and check if JUnit tests are available.
 * To find such classes and methods, it must be marked with $JUnit$.
 *
 * @author Stefan Laubenberger
 * @version 20100813
 */
public class CheckJUnit {

    private static final Logger log = LoggerFactory.getLogger(CheckJUnit.class);

    private static final String MARKER = "$JUnit$";

    private static final String EXTENSION_CSV = ".csv";

    private static final String EXTENSION_JAVA = ".java";

    private static final String QUALIFIER_PUBLIC = "public";

    private static final String QUALIFIER_PROTECTED = "protected";

    private static final String QUALIFIER_CLASS = "class";

    private static final String QUALIFIER_INTERFACE = "interface";

    /**
	 * @param args
	 */
    public static void main(final String[] args) {
        list(false);
    }

    private static void list(final boolean isTested) {
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new FileFilterCsv());
        if (isTested) {
            fc.setSelectedFile(new File(HelperEnvironment.getUserHomeDirectory(), "JUnitTested" + EXTENSION_CSV));
        } else {
            fc.setSelectedFile(new File(HelperEnvironment.getUserHomeDirectory(), "JUnitUntested" + EXTENSION_CSV));
        }
        if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(null)) {
            final File output = fc.getSelectedFile();
            output.delete();
            try {
                final java.io.FileFilter filter = new java.io.FileFilter() {

                    @Override
                    public boolean accept(final File file) {
                        return HelperString.endsWith(file.getName(), EXTENSION_JAVA) && !HelperString.contains(file.getName(), "svn");
                    }
                };
                final Collection<File> listJava = HelperIO.getFiles(HelperEnvironment.getUserDirectory(), filter);
                HelperIO.writeLine(output, HelperString.concatenate(HelperString.SEMICOLON, "Class", "Method/Variable"));
                for (final File file : listJava) {
                    if (file.getAbsolutePath().contains("/main/java/")) {
                        final Scanner scanner = new Scanner(file);
                        while (scanner.hasNextLine()) {
                            final String line = scanner.nextLine();
                            if (isTested) {
                                if (null != line && line.contains(MARKER)) {
                                    HelperIO.writeLine(output, HelperString.concatenate(HelperString.SEMICOLON, file.getAbsolutePath(), line));
                                }
                            } else {
                                if (null != line && !line.contains(MARKER) && (line.contains(QUALIFIER_PUBLIC) || line.contains(QUALIFIER_PROTECTED)) && !line.contains(QUALIFIER_CLASS) && !line.contains(QUALIFIER_INTERFACE) && !line.contains(HelperString.SEMICOLON)) {
                                    HelperIO.writeLine(output, HelperString.concatenate(HelperString.SEMICOLON, file.getAbsolutePath(), line));
                                }
                            }
                        }
                        scanner.close();
                    }
                }
            } catch (IOException ex) {
                log.error("Could not process files", ex);
            }
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Open file with the default application?", "Open file", JOptionPane.YES_NO_OPTION)) {
                try {
                    LauncherFile.open(output);
                } catch (IOException ex) {
                    log.error("Could not open output file", ex);
                    System.exit(10);
                }
            }
        }
    }

    static class FileFilterCsv extends FileFilter {

        @Override
        public boolean accept(final File file) {
            return HelperString.endsWith(file.getName(), EXTENSION_CSV);
        }

        @Override
        public String getDescription() {
            return "*.csv";
        }
    }
}
