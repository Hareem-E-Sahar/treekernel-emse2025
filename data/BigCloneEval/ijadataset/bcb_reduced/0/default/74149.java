import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GoodWindowsExec {

    public static String[] getCommands(final String xsdFilename, final String packageName, final String genDirName, final String javaHomePath) {
        final String osName = System.getProperty("os.name");
        final String[] cmd = new String[8];
        if (osName.equals("Windows NT")) {
            cmd[0] = "cmd.exe";
            cmd[1] = "/C";
            cmd[2] = "set SCHEMA=" + xsdFilename;
            cmd[3] = "set PKG=" + packageName;
            cmd[4] = "set DIR=" + genDirName;
            cmd[5] = "set JAVA_HOME=" + javaHomePath;
            cmd[6] = "PATH=%JAVA_HOME%\\bin;%PATH%";
            cmd[7] = "xjc -p %PKG% %SCHEMA% -d %DIR% -target 2.0 -verbose";
        } else if (osName.equals("Windows 95")) {
            cmd[0] = "command.com";
            cmd[1] = "/C";
            cmd[2] = "set SCHEMA=" + xsdFilename;
            cmd[3] = "set PKG=" + packageName;
            cmd[4] = "set DIR=" + genDirName;
            cmd[5] = "set JAVA_HOME=" + javaHomePath;
            cmd[6] = "PATH=%JAVA_HOME%\\bin;%PATH%";
            cmd[7] = "xjc -p %PKG% %SCHEMA% -d %DIR% -target 2.0 -verbose";
        } else {
            cmd[0] = "cmd.exe";
            cmd[1] = "/C";
            cmd[2] = "set SCHEMA=" + xsdFilename;
            cmd[3] = "set PKG=" + packageName;
            cmd[4] = "set DIR=" + genDirName;
            cmd[5] = "set JAVA_HOME=" + javaHomePath;
            cmd[6] = "PATH=%JAVA_HOME%\\bin;%PATH%";
            cmd[7] = "xjc -p %PKG% %SCHEMA% -d %DIR% -target 2.0 -verbose";
        }
        return cmd;
    }

    public static void main(final String args[]) {
        try {
            final String osName = System.getProperty("os.name");
            final String[] cmd = getCommands("enumtypes.xsd", "com.enumtypes", "src\\gen", "C:\\java\\jdk\\1.6.0_10");
            final Runtime rt = Runtime.getRuntime();
            for (int i = 0; i < cmd.length; i++) {
                System.out.println("Execing: " + cmd[i]);
            }
            final Process proc = rt.exec(cmd);
            final StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
            final StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            final int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }
}

class StreamGobbler extends Thread {

    InputStream is;

    String type;

    StreamGobbler(final InputStream is, final String type) {
        this.is = is;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            final InputStreamReader isr = new InputStreamReader(is);
            final BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(type + ">" + line);
            }
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
