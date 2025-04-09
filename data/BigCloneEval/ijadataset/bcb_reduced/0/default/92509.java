import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DoRuntime {

    public static void main(String args[]) throws Exception {
        test("javap.exe", "java.lang.String");
    }

    public static void test1(String cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        System.out.printf("Output of running is test1:");
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }

    public static void test(String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File("C:/Deloitte/Personel/Videos"));
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        pb.redirectErrorStream(true);
        processOutputStream(proc.getInputStream());
    }

    private static void processOutputStream(InputStream inputStream) throws Exception {
        BufferedInputStream inStream = new BufferedInputStream(inputStream);
        System.out.println("Available: " + inStream.available());
        while (inStream.available() > 0) {
            System.out.print((char) inStream.read());
        }
        inStream.close();
    }
}
