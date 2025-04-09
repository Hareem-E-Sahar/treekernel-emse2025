import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;

public class Execute {

    public String Execute(String app, String directory, String arguments) {
        Process process = null;
        int AppPid = 0;
        String processpid = "";
        String LaunchHelper = System.getProperty("java.io.tmpdir") + "AppEmbed\\" + "AppLauncher.exe";
        System.err.println("App Loader is at: " + LaunchHelper);
        directory = "\"" + directory + "\"";
        app = "\"" + app + " " + Main.ARGUMENTS + "\"";
        System.err.println("Attempting to send following command to LaunchHelper: " + app + " " + directory);
        try {
            String[] Program = { LaunchHelper, app, directory };
            process = Runtime.getRuntime().exec(Program);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String out;
            while ((out = input.readLine()) != null) {
                processpid = out;
            }
            input.close();
        } catch (Exception ex) {
        }
        return processpid;
    }

    public static String Findexe(String dir) {
        File myDir = new File(dir);
        FilenameFilter select = new FileListFilter("", "exe");
        File[] contents = myDir.listFiles(select);
        if (contents != null) {
            System.err.println("Exes found: " + contents.length);
            if (contents.length == 1) {
                for (File file : contents) {
                    return file.toString();
                }
            } else {
                return dir + "\\" + dir.substring(dir.lastIndexOf('\\') + 1) + ".exe";
            }
        } else {
            return "";
        }
        return "";
    }

    static class FileListFilter implements FilenameFilter {

        private String name;

        private String extension;

        public FileListFilter(String name, String extension) {
            this.name = name;
            this.extension = extension;
        }

        public boolean accept(File directory, String filename) {
            boolean fileOK = true;
            if (name != null) {
                fileOK &= filename.startsWith(name);
            }
            if (extension != null) {
                fileOK &= filename.endsWith('.' + extension);
            }
            return fileOK;
        }
    }
}
