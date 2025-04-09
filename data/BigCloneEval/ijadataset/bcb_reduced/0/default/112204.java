import java.io.*;
import java.util.regex.*;

public class Killer {

    public static boolean kill(File prey, FileFilter filter) throws IOException {
        if (prey.isFile() && filter.accept(prey)) {
            return prey.delete();
        } else if (prey.isDirectory()) {
            File[] children = prey.listFiles();
            if (children != null) for (File subPrey : children) {
                if (!kill(subPrey, filter)) {
                    java.util.logging.Logger.global.info("Killing spree stops on " + subPrey.getCanonicalPath());
                    return false;
                }
            }
            if (filter.accept(prey)) {
                return prey.delete();
            }
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        final Pattern p = Pattern.compile(args[1]);
        final FilenameFilter fileMatcher = new FilenameFilter() {

            public boolean accept(File dir, String fileName) {
                return p.matcher(fileName).matches();
            }
        };
        kill(new File(args[0]), new FileFilter() {

            public boolean accept(File filtered) {
                if (filtered.isFile()) {
                    return fileMatcher.accept(filtered.getParentFile(), filtered.getName());
                } else if (filtered.isDirectory()) {
                    return filtered.listFiles() == null;
                }
                return false;
            }
        });
    }
}
