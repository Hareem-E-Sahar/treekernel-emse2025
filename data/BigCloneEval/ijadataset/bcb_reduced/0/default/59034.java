import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Test {

    public static void main(String... args) throws Exception {
        System.out.println(args);
        Object o = null;
        story(o);
        String fileName = "12121212.wmv";
        System.out.println(fileName.substring(0, fileName.indexOf('.')) + ".mp4");
    }

    private static void story(Object... o) {
        if (o != null) System.out.println("O noes!" + o.length);
    }

    public static void testFFMPEG() {
        try {
            Process execProcess = Runtime.getRuntime().exec("javac.exe");
            BufferedInputStream inStream = new BufferedInputStream(execProcess.getErrorStream());
            while (inStream.available() != 0) {
                System.out.print((char) inStream.read());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("C:/Deloitte/ffmpeg/bin/ffmpeg.exe -i \"C:/Deloitte/Personel/Videos/Clean Install Ubuntu Linux on a PC.mp4\"");
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        System.out.printf("Output of running is:");
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }
}
