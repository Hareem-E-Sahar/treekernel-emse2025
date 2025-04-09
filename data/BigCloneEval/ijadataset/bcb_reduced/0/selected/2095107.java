package javbot.command;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javbot.GlobalClass;
import javbot.util.Jpeg;

public class CmdScreenshot extends Command {

    private static final Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

    private int quality;

    private int scaling;

    private final int mode;

    private final int MODE_INVALID = 0;

    private final int MODE_NORMAL = 1;

    private final int MODE_QUALITY_AND_SCALE = 2;

    public CmdScreenshot() throws AWTException {
        super();
        mode = MODE_NORMAL;
    }

    public CmdScreenshot(int quality, int scaling) throws AWTException {
        super();
        if (quality == 100 && scaling == 100) {
            mode = MODE_NORMAL;
        } else if (0 < quality && quality <= 100 && 0 < scaling && scaling <= 1000) {
            mode = MODE_QUALITY_AND_SCALE;
            this.quality = quality;
            this.scaling = scaling;
        } else {
            mode = MODE_INVALID;
        }
    }

    public void execute() {
        try {
            if (mode == MODE_INVALID) return;
            String fileName = "shot_" + formatter.format(Calendar.getInstance().getTime()) + ".jpg";
            BufferedImage image = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            File file = new File(fileName);
            if (mode == MODE_QUALITY_AND_SCALE) {
                File fileTemp = new File(fileName + ".tmp");
                ImageIO.write(image, "jpg", fileTemp);
                Jpeg.processImageWithQualityAndScaling(fileTemp, file, quality, scaling);
                fileTemp.delete();
            } else if (mode == MODE_NORMAL) {
                ImageIO.write(image, "jpg", file);
            }
            System.out.println("Saved screen shot (" + image.getWidth() + " x " + image.getHeight() + " pixels) to file \"" + fileName + "\".");
            GlobalClass.files.add(new File(fileName));
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        switch(mode) {
            case MODE_NORMAL:
                return "Screenshot";
            case MODE_QUALITY_AND_SCALE:
                return "Screenshot (Quality " + quality + "%, Scaling " + scaling + "%)";
        }
        return null;
    }
}
