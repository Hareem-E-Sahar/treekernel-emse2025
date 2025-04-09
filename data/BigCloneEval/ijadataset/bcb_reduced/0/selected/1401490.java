package mobiledesktopserver;

import java.awt.Robot;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ScreenHandler {

    private static BufferedImage bufferedImage;

    private static BufferedInputStream imageBuffer;

    private static byte[] screenshot;

    public int deviceWidth = 100;

    public int deviceHeight = 100;

    public int zoomIn = 1;

    public int zoomOut = 1;

    public int viewPortX = 0;

    public int viewPortY = 0;

    public boolean isFitScreen = false;

    public String clientIP;

    public int screenWidth;

    public int screenHeight;

    int count = 1;

    boolean firstTimeGray = true;

    /** Creates a new instance of ScreenHandler */
    public ScreenHandler() {
    }

    public int getViewPortX() {
        return viewPortX;
    }

    public void setViewPortX(int x) {
        viewPortX = x;
    }

    public int getViewPortY() {
        return viewPortY;
    }

    public void setViewPortY(int y) {
        viewPortY = y;
    }

    public int getDeviceWidth() {
        return deviceWidth;
    }

    public void setDeviceWidth(int width) {
        deviceWidth = width;
    }

    public int getDeviceHeight() {
        return deviceHeight;
    }

    public void setDeviceHeight(int height) {
        deviceHeight = height;
    }

    public int getZoomIn() {
        return zoomIn;
    }

    public void setZoomIn(int zin) {
        zoomIn = zin;
    }

    public int getZoomOut() {
        return zoomOut;
    }

    public void setZoomOut(int zout) {
        zoomOut = zout;
    }

    public boolean getFitScreen() {
        return isFitScreen;
    }

    public void setFitScreen(boolean fit) {
        isFitScreen = fit;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int width) {
        screenWidth = width;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int height) {
        screenHeight = height;
    }

    public byte[] getAndSendScreenshot(Robot r, boolean colorImage) {
        r.delay(200);
        int imageColorType;
        if (colorImage) {
            count = 1;
            firstTimeGray = true;
            imageColorType = BufferedImage.TYPE_INT_RGB;
        } else {
            imageColorType = BufferedImage.TYPE_BYTE_GRAY;
            if (firstTimeGray) {
                firstTimeGray = false;
                System.out.println("Trying to create Grayscale image");
            } else {
                count++;
                System.out.println("Trying to divide the grayscale image");
            }
        }
        if (!isFitScreen) {
            imageColorType = BufferedImage.TYPE_INT_RGB;
            Image image = r.createScreenCapture(new Rectangle(viewPortX, viewPortY, deviceWidth + ((zoomIn * deviceWidth) - (zoomOut * deviceWidth)), deviceHeight + ((zoomIn * deviceHeight) - (zoomOut * deviceHeight))));
            bufferedImage = new BufferedImage(deviceWidth, deviceHeight, imageColorType);
            Graphics graphics = bufferedImage.getGraphics();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, deviceWidth, deviceHeight);
            graphics.drawImage(image, 0, 0, deviceWidth, deviceHeight, null);
            image = bufferedImage.getScaledInstance(deviceWidth, deviceHeight, imageColorType);
            bufferedImage = new BufferedImage(deviceWidth, deviceHeight, imageColorType);
            graphics = bufferedImage.getGraphics();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, deviceWidth, deviceHeight);
            graphics.drawImage(image, 0, 0, deviceWidth, deviceHeight, null);
        } else {
            float aspectRatio = 1;
            aspectRatio = (float) (screenWidth / screenHeight);
            int fitScreenWidth = deviceWidth;
            int fitScreenHeight = deviceHeight;
            if (deviceHeight > deviceWidth) {
                fitScreenWidth = deviceWidth;
                fitScreenHeight = (int) (deviceWidth / aspectRatio);
            }
            imageColorType = BufferedImage.TYPE_BYTE_GRAY;
            colorImage = false;
            Image image = r.createScreenCapture(new Rectangle(0, 0, screenWidth, screenHeight));
            bufferedImage = new BufferedImage(fitScreenWidth, fitScreenHeight, imageColorType);
            Graphics graphics = bufferedImage.getGraphics();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, deviceWidth, deviceHeight);
            graphics.drawImage(image, 0, 0, deviceWidth, (int) (fitScreenHeight / count), null);
            image = bufferedImage.getScaledInstance(screenWidth, (int) (screenHeight / count), imageColorType);
            bufferedImage = new BufferedImage(deviceWidth, (int) (fitScreenHeight), imageColorType);
            graphics = bufferedImage.getGraphics();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, deviceWidth, (int) (deviceHeight));
            graphics.drawImage(image, 0, 0, deviceWidth, (int) (fitScreenHeight / count), null);
        }
        File file = new File("mobiledesktopserver.jpg");
        try {
            file.createNewFile();
            FileOutputStream fileout = new FileOutputStream(file);
            ImageIO.write(bufferedImage, "jpg", fileout);
            fileout.flush();
            fileout.close();
            FileInputStream filein = new FileInputStream(file);
            imageBuffer = new BufferedInputStream(filein);
            screenshot = new byte[(int) file.length()];
            imageBuffer.read(screenshot, 0, screenshot.length);
        } catch (FileNotFoundException ex) {
            System.out.println("\nError related to file");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error in sending screenshot byte array");
            ex.printStackTrace();
        }
        if (file.length() < 15000) {
            return screenshot;
        } else {
            if (isFitScreen) {
                return getAndSendScreenshot(r, false);
            } else {
                if (colorImage) return getAndSendScreenshot(r, false); else return screenshot;
            }
        }
    }
}
