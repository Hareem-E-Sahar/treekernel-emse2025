package algorithms;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author leeing
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("E:" + StrictMath.E);
        System.out.println("Pi:" + StrictMath.PI);
        try {
            System.out.println(Runtime.getRuntime().exec("cmd /c dir ").getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(new File(".").getAbsoluteFile());
    }
}
