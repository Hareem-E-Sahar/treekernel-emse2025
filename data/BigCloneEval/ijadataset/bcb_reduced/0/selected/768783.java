package com.jspx.graphics.camera;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * ****************************************************************
 * 该JavaBean可以直接在其他Java应用程序中调用，实现屏幕的"拍照"
 * This JavaBean is used to snapshot the GUI in a
 * Java application! You can embeded
 * it in to your java application source code, and us
 * it to snapshot the right GUI of the application
 *
 * @author liluqun ([email]liluqun@263.net[/email])
 * @version 1.0
 *          <p/>
 * ***************************************************
 */
public class GuiCamera {

    private String fileName;

    private String defaultName = "GuiCamera";

    static int serialNum = 0;

    private String imageFormat;

    private String defaultImageFormat = "png";

    private Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * *************************************************************
     * 默认的文件前缀为GuiCamera，文件格式为PNG格式
     * The default construct will use the default
     * Image file surname "GuiCamera",
     * and default image format "png"
     * **************************************************************
     */
    public GuiCamera() {
        fileName = defaultName;
        imageFormat = defaultImageFormat;
    }

    /**
     * *************************************************************
     *
     * @param s      the surname of the snapshot file
     * @param format the format of the image file,
     *               it can be "jpg" or "png"
     *               本构造支持JPG和PNG文件的存储
     *               **************************************************************
     */
    public GuiCamera(String s, String format) {
        fileName = s;
        imageFormat = format;
    }

    /**
     * *************************************************************
     * 对屏幕进行拍照
     * snapShot the Gui once
     * **************************************************************
     */
    public void snapShot() {
        try {
            BufferedImage screenshot = (new Robot()).createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
            serialNum++;
            String name = fileName + String.valueOf(serialNum) + "." + imageFormat;
            File f = new File(name);
            System.out.print("Save File " + name);
            ImageIO.write(screenshot, imageFormat, f);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static void main(String[] args) {
        GuiCamera cam = new GuiCamera("d:\\Hello", "png");
        cam.snapShot();
    }
}
