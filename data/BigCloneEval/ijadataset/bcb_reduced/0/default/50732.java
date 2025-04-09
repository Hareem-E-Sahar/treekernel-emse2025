import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Test;

public class ImageTest {

    @Test
    public void testA() throws IOException {
        Process ps = Runtime.getRuntime().exec("WebCam/WebCam.exe");
        BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }
    }
}
