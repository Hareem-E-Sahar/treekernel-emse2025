import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class info {

    public static void main(String argv[]) {
        new info().judge();
    }

    private void judge() {
        Runtime myrun = Runtime.getRuntime();
        String[] cmdstr = { "./info.o" };
        try {
            Process proc = myrun.exec(cmdstr);
            proc.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
