import genj.gedcom.Gedcom;
import genj.report.Report;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * GenJ - Report to run an external program
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportExec extends Report {

    /**
   * Main method
   */
    public void start(Gedcom gedcom) {
        String cmd = getValueFromUser("executables", translate("WhichExecutable"), new String[0]);
        if (cmd == null || cmd.length() == 0) return;
        BufferedReader in = null;
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                println(line);
            }
        } catch (IOException ioe) {
            println(translate("Error", ioe.getMessage()));
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
            }
            ;
        }
    }
}
