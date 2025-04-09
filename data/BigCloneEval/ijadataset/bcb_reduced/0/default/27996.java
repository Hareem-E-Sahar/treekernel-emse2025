import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Executor {

    static final int STANDARD = 0;

    static final int ERROR = 1;

    public static StringBuffer exec(String[] cmd, int type, boolean outputSkip) throws Exception {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd);
        if (outputSkip == false) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        BufferedReader out;
        if (type == STANDARD) {
            out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        } else {
            out = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        }
        String line = null;
        while ((line = out.readLine()) != null) {
            result.append(line);
        }
        return result;
    }
}
