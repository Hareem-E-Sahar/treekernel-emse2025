package Script.UAT.Lib;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageUtility {

    private String fileName;

    private String defaultName = "GuiCamera";

    static int serialNum = 0;

    private String imageFormat;

    private String defaultImageFormat = "jpg";

    public ImageUtility() {
        fileName = defaultName;
        imageFormat = defaultImageFormat;
    }

    public ImageUtility(String s, String format) {
        fileName = s;
        imageFormat = format;
    }

    /****************************************************************
     * Method        : SaveScreenToJPG
     * Author        : Wan Yu
     * Description   : Save screen print to jpg file.
     * Time          :  2007/04/24
     * -------------------------------------------------------------
     * Input         :
     *                String fileFullName : the full name (include path) for the jpg file.
     * ----------------------------------------------------------------
     * Output        :    
     *                Void
     * _______________________________________________________________
     * History       :
     *                 2007/04/24 init           
     ****************************************************************/
    public static void SaveScreenToJPG(String fileFullName) {
        try {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            BufferedImage screenshot = (new Robot()).createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
            String name = fileFullName;
            File f = new File(name);
            System.out.print("Save File " + name);
            ImageIO.write(screenshot, "jpg", f);
            System.out.print("..Finished!\n");
        } catch (Exception ex) {
            throw new RuntimeException("Can not save screen to file.");
        }
    }

    public static void SaveScreenToJPG(String fileName, String filePath) {
        try {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            BufferedImage screenshot = (new Robot()).createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
            if (!filePath.endsWith("\\")) filePath = filePath + "\\";
            String name = filePath + fileName;
            File f = new File(name);
            System.out.print("Save File " + name);
            ImageIO.write(screenshot, "jpg", f);
            System.out.print("..Finished!\n");
        } catch (Exception ex) {
            throw new RuntimeException("Can not save screen to file.");
        }
    }
}
