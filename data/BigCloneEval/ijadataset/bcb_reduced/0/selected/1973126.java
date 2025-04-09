package com.company.common;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import com.sun.image.codec.jpeg.*;
import java.io.*;

public class WnetWScreenRecorder extends Thread {

    private Dimension screenSize;

    private Rectangle rectangle;

    private Robot robot;

    private long i = 0;

    @SuppressWarnings("unused")
    private JPEGImageEncoder encoder;

    public WnetWScreenRecorder() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        rectangle = new Rectangle(screenSize);
        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        new WnetWScreenRecorder().start();
    }

    public void run() {
        FileOutputStream fos = null;
        while (true) {
            try {
                BufferedImage image = robot.createScreenCapture(rectangle);
                fos = new FileOutputStream("C:\\records\\" + i + ".gif");
                JPEGCodec.createJPEGEncoder(fos).encode(image);
                fos.close();
                i = i + 1;
                Thread.sleep(25);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
                try {
                    if (fos != null) fos.close();
                } catch (Exception e1) {
                }
            }
        }
    }
}
