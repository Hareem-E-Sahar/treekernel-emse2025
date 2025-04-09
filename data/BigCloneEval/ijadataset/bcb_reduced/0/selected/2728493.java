package saadadb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Contains some methods managing working directories
 * @author laurent
 * @version $Id: WorkDirectory.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public class WorkDirectory {

    /**
	 * Check that baseDirectory is a writable directory. 
	 * Attempt to create it if it does not exist
	 * @param baseDirectory
	 * @throws Exception :Rise if baseDirectory cannot be used as working directory
	 */
    public static final void validWorkingDirectory(String baseDirectory) throws Exception {
        File f = new File(baseDirectory);
        if (f.exists()) {
            if (!f.isDirectory()) {
                throw new Exception(baseDirectory + " is not a directory");
            } else if (!f.canWrite()) {
                throw new Exception("Cannot write in directory " + baseDirectory);
            }
            return;
        } else {
            if (!f.mkdir()) {
                throw new Exception("Cannot create  directory " + baseDirectory);
            } else if (!f.canWrite()) {
                throw new Exception("Cannot write in directory " + baseDirectory);
            }
            return;
        }
    }

    /**
	 * Check that baseDirectory is a writable directory. 
	 * @param baseDirectory
	 * @return  true if the directory can be used as working directory
	 * @throws Exception if something goes wrong with @link File 
	 */
    public static final boolean isWorkingDirectoryValid(String baseDirectory) throws Exception {
        File f = new File(baseDirectory);
        if (f.exists()) {
            if (!f.isDirectory()) {
                return false;
            } else if (!f.canWrite()) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
	 * Delete recursively the content of the file f
	 * @param f file (or directory) to delete
	 * @throws IOException
	 */
    public static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) delete(c);
        }
        if (!f.delete()) throw new FileNotFoundException("Failed to delete file: " + f);
    }

    /**
	 * Remove recursively the content of the directory f
	 * @param f
	 * @throws IOException
	 */
    public static void emptyDirectory(File f) throws IOException {
        if (Messenger.debug_mode) Messenger.printMsg(Messenger.DEBUG, "empty directory " + f.getAbsolutePath());
        if (f.isDirectory()) {
            for (File c : f.listFiles()) delete(c);
        }
    }

    /**
	 * Remove recursively the content of the directory f except files named except
	 * @param f
	 * @param except 
	 * @throws IOException
	 */
    public static void emptyDirectory(File f, String except) throws IOException {
        if (Messenger.debug_mode) Messenger.printMsg(Messenger.DEBUG, "empty directory " + f.getAbsolutePath());
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                if (except != null && !c.getName().equals(except)) {
                    delete(c);
                }
            }
        }
    }
}
