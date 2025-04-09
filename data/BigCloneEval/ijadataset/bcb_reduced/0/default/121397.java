import java.io.File;

public class FileSystemTools {

    public static File createTmpDir(File parentDir, String dirNamePrefix) {
        File dir;
        int i = 0;
        boolean dirOK = false;
        do {
            String postfix = String.format("%02d", i);
            String path = parentDir.getPath() + File.separatorChar + dirNamePrefix + "-" + postfix;
            dir = new File(path);
            if (dir.exists()) {
                i++;
            } else {
                dirOK = true;
            }
        } while (!dirOK);
        dir.mkdir();
        return dir;
    }

    public static boolean deleteDir(File dir) {
        if (dir.exists()) {
            File[] contents = dir.listFiles();
            for (int i = 0; i < contents.length; i++) {
                File f = contents[i];
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    f.delete();
                }
            }
        }
        return dir.delete();
    }
}
