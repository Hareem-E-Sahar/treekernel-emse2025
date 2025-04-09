import java.io.File;
import java.io.FileFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClearMSXMLLog {

    private static Logger logger = Logger.getLogger("ClearMSXMLLog");

    private static Level level = Level.OFF;

    private static class MSXMLLogDirFilter implements FileFilter {

        public boolean accept(File me) {
            if (!me.isDirectory()) return false;
            if (!me.getName().matches("[0-9a-f]+")) return false;
            String[] files = me.list();
            if (files.length != 1) return false;
            if (!files[0].equals("msxml4-KB927978-enu.log")) return false;
            return true;
        }
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        logger.setLevel(level);
        File c = new File("c:");
        logger.info(c.getAbsolutePath());
        File[] logDirs = c.listFiles(new MSXMLLogDirFilter());
        for (File f : logDirs) {
            logger.info(f.getAbsolutePath());
            deleteDir(f);
        }
    }

    private static void deleteDir(File dir) {
        if (!dir.isDirectory()) {
            dir.delete();
            return;
        }
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) deleteDir(f); else f.delete();
        }
        dir.delete();
    }
}
