package gnu.saw.graphics.screencapture;

import gnu.saw.graphics.image.SAWARGBPixelGrabber;
import gnu.saw.graphics.image.SAWImageIO;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;

public class SAWAWTScreenCaptureProvider {

    public static final int SAW_COLOR_QUALITY_LOW = 0;

    public static final int SAW_COLOR_QUALITY_MEDIUM = 1;

    public static final int SAW_COLOR_QUALITY_HIGH = 2;

    private volatile int colorQuality;

    private volatile boolean lowQualityScreenCaptureInitialized;

    private volatile boolean mediumQualityScreenCaptureInitialized;

    private volatile boolean highQualityScreenCaptureInitialized;

    private int i, lowRed, lowGreen, lowBlue;

    private int j, mediumRed, mediumGreen, mediumBlue;

    private int lowWidth, lowHeight;

    private int mediumWidth, mediumHeight;

    private int highWidth, highHeight;

    private int[] sectionPixelBufferInt;

    private int[] highQualityPixelBufferInt;

    private byte[] lowQualityPixelBufferByte;

    private short[] mediumQualityPixelBufferShort;

    private volatile Dimension currentScreenSize;

    private BufferedImage highQualityImage;

    private BufferedImage mediumQualityImage;

    private BufferedImage lowQualityImage;

    private volatile GraphicsDevice graphicsDevice;

    private volatile Robot screenCaptureRobot;

    private SAWImageIO imageIO = new SAWImageIO();

    public SAWAWTScreenCaptureProvider() {
        try {
            this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        } catch (Exception e) {
        }
    }

    public void resetGraphicsDevice() {
        try {
            setGraphicsDevice(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
        } catch (Exception e) {
        }
    }

    public void setGraphicsDevice(GraphicsDevice graphicsDevice) {
        if (!this.graphicsDevice.getIDstring().equals(graphicsDevice.getIDstring())) {
            this.graphicsDevice = graphicsDevice;
            initializeScreenCapture();
        }
    }

    public synchronized void setColorQuality(int colorQuality) {
        this.colorQuality = colorQuality;
    }

    public Dimension getCurrentScreenSize() {
        try {
            DisplayMode mode = graphicsDevice.getDisplayMode();
            return new Dimension(mode.getWidth(), mode.getHeight());
        } catch (Exception e) {
        }
        return null;
    }

    private boolean initializeLowQualityScreenCapture(GraphicsDevice device) {
        reset();
        try {
            if (screenCaptureRobot == null) {
                screenCaptureRobot = new Robot(device);
                screenCaptureRobot.setAutoDelay(0);
                screenCaptureRobot.setAutoWaitForIdle(false);
            }
            if (changedLowSettings()) {
                refreshLowSettings();
            }
            lowQualityScreenCaptureInitialized = true;
            return true;
        } catch (Exception e) {
            lowQualityScreenCaptureInitialized = false;
            return false;
        }
    }

    private boolean initializeMediumQualityScreenCapture(GraphicsDevice device) {
        reset();
        try {
            if (screenCaptureRobot == null) {
                screenCaptureRobot = new Robot(device);
                screenCaptureRobot.setAutoDelay(0);
                screenCaptureRobot.setAutoWaitForIdle(false);
            }
            if (changedMediumSettings()) {
                refreshMediumSettings();
            }
            mediumQualityScreenCaptureInitialized = true;
            return true;
        } catch (Exception e) {
            mediumQualityScreenCaptureInitialized = false;
            return false;
        }
    }

    private boolean initializeHighQualityScreenCapture(GraphicsDevice device) {
        reset();
        try {
            if (screenCaptureRobot == null) {
                screenCaptureRobot = new Robot(device);
                screenCaptureRobot.setAutoDelay(0);
                screenCaptureRobot.setAutoWaitForIdle(false);
            }
            if (changedHighSettings()) {
                refreshHighSettings();
            }
            highQualityScreenCaptureInitialized = true;
            return true;
        } catch (Exception e) {
            highQualityScreenCaptureInitialized = false;
            return false;
        }
    }

    public synchronized boolean initializeScreenCapture() {
        if (colorQuality == SAW_COLOR_QUALITY_HIGH) {
            return initializeHighQualityScreenCapture(graphicsDevice);
        } else if (colorQuality == SAW_COLOR_QUALITY_MEDIUM) {
            return initializeMediumQualityScreenCapture(graphicsDevice);
        } else {
            return initializeLowQualityScreenCapture(graphicsDevice);
        }
    }

    public synchronized boolean initializeScreenCapture(GraphicsDevice device) {
        if (colorQuality == SAW_COLOR_QUALITY_HIGH) {
            return initializeHighQualityScreenCapture(device);
        } else if (colorQuality == SAW_COLOR_QUALITY_MEDIUM) {
            return initializeMediumQualityScreenCapture(device);
        } else {
            return initializeLowQualityScreenCapture(device);
        }
    }

    private boolean isLowQualityScreenCaptureInitialized() {
        return lowQualityScreenCaptureInitialized;
    }

    private boolean isMediumQualityScreenCaptureInitialized() {
        return mediumQualityScreenCaptureInitialized;
    }

    private boolean isHighQualityScreenCaptureInitialized() {
        return highQualityScreenCaptureInitialized;
    }

    public synchronized boolean isScreenCaptureInitialized() {
        if (colorQuality == SAW_COLOR_QUALITY_HIGH) {
            return isHighQualityScreenCaptureInitialized();
        } else if (colorQuality == SAW_COLOR_QUALITY_MEDIUM) {
            return isMediumQualityScreenCaptureInitialized();
        } else {
            return isLowQualityScreenCaptureInitialized();
        }
    }

    public synchronized void reset() {
        disposeLowQualityScreenCaptureResources();
        disposeMediumQualityScreenCaptureResources();
        disposeHighQualityScreenCaptureResources();
        screenCaptureRobot = null;
    }

    public synchronized void dispose() {
        disposeLowQualityScreenCaptureResources();
        disposeMediumQualityScreenCaptureResources();
        disposeHighQualityScreenCaptureResources();
        sectionPixelBufferInt = null;
        screenCaptureRobot = null;
        try {
            this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        } catch (Exception e) {
        }
    }

    private void disposeLowQualityScreenCaptureResources() {
        if (lowQualityImage != null) {
            lowQualityImage.flush();
            lowQualityImage = null;
        }
        lowQualityPixelBufferByte = null;
        lowWidth = 0;
        lowHeight = 0;
        lowQualityScreenCaptureInitialized = false;
    }

    private void disposeMediumQualityScreenCaptureResources() {
        if (mediumQualityImage != null) {
            mediumQualityImage.flush();
            mediumQualityImage = null;
        }
        mediumQualityPixelBufferShort = null;
        mediumWidth = 0;
        mediumHeight = 0;
        mediumQualityScreenCaptureInitialized = false;
    }

    private void disposeHighQualityScreenCaptureResources() {
        if (highQualityImage != null) {
            highQualityImage.flush();
            highQualityImage = null;
        }
        sectionPixelBufferInt = null;
        highQualityPixelBufferInt = null;
        highWidth = 0;
        highHeight = 0;
        highQualityScreenCaptureInitialized = false;
    }

    private boolean changedLowSettings() {
        currentScreenSize = getCurrentScreenSize();
        if (currentScreenSize == null) {
            return true;
        }
        return (currentScreenSize.width != lowWidth || currentScreenSize.height != lowHeight);
    }

    private boolean changedMediumSettings() {
        currentScreenSize = getCurrentScreenSize();
        if (currentScreenSize == null) {
            return true;
        }
        return (currentScreenSize.width != mediumWidth || currentScreenSize.height != mediumHeight);
    }

    private boolean changedHighSettings() {
        currentScreenSize = getCurrentScreenSize();
        if (currentScreenSize == null) {
            return true;
        }
        return (currentScreenSize.width != highWidth || currentScreenSize.height != highHeight);
    }

    private void refreshLowSettings() {
        currentScreenSize = getCurrentScreenSize();
        if (currentScreenSize == null) {
            return;
        }
        lowWidth = currentScreenSize.width;
        lowHeight = currentScreenSize.height;
        if (lowQualityImage != null) {
            lowQualityImage.flush();
            lowQualityImage = null;
        }
        lowQualityImage = imageIO.newImage(lowWidth, lowHeight, BufferedImage.TYPE_BYTE_INDEXED);
    }

    private void refreshMediumSettings() {
        currentScreenSize = getCurrentScreenSize();
        if (currentScreenSize == null) {
            return;
        }
        mediumWidth = currentScreenSize.width;
        mediumHeight = currentScreenSize.height;
        if (mediumQualityImage != null) {
            mediumQualityImage.flush();
            mediumQualityImage = null;
        }
        mediumQualityImage = imageIO.newImage(mediumWidth, mediumHeight, BufferedImage.TYPE_USHORT_555_RGB);
    }

    private void refreshHighSettings() {
        currentScreenSize = getCurrentScreenSize();
        if (currentScreenSize == null) {
            return;
        }
        highWidth = currentScreenSize.width;
        highHeight = currentScreenSize.height;
        if (highQualityImage != null) {
            highQualityImage.flush();
            highQualityImage = null;
        }
        highQualityImage = imageIO.newImage(highWidth, highHeight, BufferedImage.TYPE_INT_RGB);
    }

    private BufferedImage createLowQualityScreenCapture(boolean drawPointer) {
        if (changedLowSettings()) {
            refreshLowSettings();
        }
        BufferedImage screenCapture = screenCaptureRobot.createScreenCapture(new Rectangle(0, 0, lowWidth, lowHeight));
        int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
        if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength) {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels(sectionPixelBufferInt);
        } else {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels();
        }
        lowQualityPixelBufferByte = ((DataBufferByte) lowQualityImage.getRaster().getDataBuffer()).getData();
        for (i = 0; i < pixelDataLength; i++) {
            lowRed = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) + 26) / 51) * 36);
            lowGreen = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) + 26) / 51) * 6);
            lowBlue = ((((sectionPixelBufferInt[i]) & 0xFF) + 26) / 51);
            lowQualityPixelBufferByte[i] = (byte) (lowRed + lowGreen + lowBlue);
        }
        screenCapture.flush();
        screenCapture = null;
        lowQualityPixelBufferByte = null;
        if (changedLowSettings()) {
            refreshLowSettings();
            return null;
        } else {
            if (drawPointer) {
                drawPointer(lowQualityImage);
            }
            return lowQualityImage;
        }
    }

    private BufferedImage createLowQualityScreenCapture(boolean drawPointer, Rectangle area) {
        if (changedLowSettings()) {
            refreshLowSettings();
        }
        Rectangle trueArea = new Rectangle(area.x, area.y, Math.min(area.width, lowWidth - area.x), Math.min(area.height, lowHeight - area.y));
        BufferedImage screenCapture = screenCaptureRobot.createScreenCapture(trueArea);
        int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
        if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength) {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels(sectionPixelBufferInt);
        } else {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels();
        }
        lowQualityPixelBufferByte = ((DataBufferByte) lowQualityImage.getRaster().getDataBuffer()).getData();
        int startOffset = trueArea.x + lowQualityImage.getWidth() * trueArea.y;
        int currentWidth = 0;
        int currentHeight = 0;
        for (i = 0; i < pixelDataLength; i++, currentWidth++) {
            if (currentWidth == trueArea.getWidth()) {
                currentWidth = 0;
                currentHeight += lowQualityImage.getWidth();
            }
            lowRed = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) + 26) / 51) * 36);
            lowGreen = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) + 26) / 51) * 6);
            lowBlue = ((((sectionPixelBufferInt[i]) & 0xFF) + 26) / 51);
            lowQualityPixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (lowRed + lowGreen + lowBlue);
        }
        screenCapture.flush();
        screenCapture = null;
        lowQualityPixelBufferByte = null;
        if (changedLowSettings()) {
            refreshLowSettings();
            return null;
        } else {
            if (drawPointer) {
                drawPointer(lowQualityImage, trueArea);
            }
            return lowQualityImage;
        }
    }

    private BufferedImage createMediumQualityScreenCapture(boolean drawPointer) {
        if (changedMediumSettings()) {
            refreshMediumSettings();
        }
        BufferedImage screenCapture = screenCaptureRobot.createScreenCapture(new Rectangle(0, 0, mediumWidth, mediumHeight));
        int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
        if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength) {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels(sectionPixelBufferInt);
        } else {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels();
        }
        mediumQualityPixelBufferShort = ((DataBufferUShort) mediumQualityImage.getRaster().getDataBuffer()).getData();
        for (j = 0; j < pixelDataLength; j++) {
            mediumRed = ((((sectionPixelBufferInt[j] >> 16) & 0xFF) >>> 3) << 10);
            mediumGreen = ((((sectionPixelBufferInt[j] >> 8) & 0xFF) >>> 3) << 5);
            mediumBlue = ((((sectionPixelBufferInt[j]) & 0xFF) >>> 3));
            mediumQualityPixelBufferShort[j] = (short) (mediumRed | mediumGreen | mediumBlue);
        }
        screenCapture.flush();
        screenCapture = null;
        mediumQualityPixelBufferShort = null;
        if (changedMediumSettings()) {
            refreshMediumSettings();
            return null;
        } else {
            if (drawPointer) {
                drawPointer(mediumQualityImage);
            }
            return mediumQualityImage;
        }
    }

    private BufferedImage createMediumQualityScreenCapture(boolean drawPointer, Rectangle area) {
        if (changedMediumSettings()) {
            refreshMediumSettings();
        }
        Rectangle trueArea = new Rectangle(area.x, area.y, Math.min(area.width, mediumWidth - area.x), Math.min(area.height, mediumHeight - area.y));
        BufferedImage screenCapture = screenCaptureRobot.createScreenCapture(trueArea);
        int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
        if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength) {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels(sectionPixelBufferInt);
        } else {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels();
        }
        mediumQualityPixelBufferShort = ((DataBufferUShort) mediumQualityImage.getRaster().getDataBuffer()).getData();
        int startOffset = trueArea.x + mediumQualityImage.getWidth() * trueArea.y;
        int currentWidth = 0;
        int currentHeight = 0;
        for (j = 0; j < pixelDataLength; j++, currentWidth++) {
            if (currentWidth == trueArea.getWidth()) {
                currentWidth = 0;
                currentHeight += mediumQualityImage.getWidth();
            }
            mediumRed = ((((sectionPixelBufferInt[j] >> 16) & 0xFF) >>> 3) << 10);
            mediumGreen = ((((sectionPixelBufferInt[j] >> 8) & 0xFF) >>> 3) << 5);
            mediumBlue = ((((sectionPixelBufferInt[j]) & 0xFF) >>> 3));
            mediumQualityPixelBufferShort[startOffset + currentWidth + currentHeight] = (short) (mediumRed | mediumGreen | mediumBlue);
        }
        screenCapture.flush();
        screenCapture = null;
        mediumQualityPixelBufferShort = null;
        if (changedMediumSettings()) {
            refreshMediumSettings();
            return null;
        } else {
            if (drawPointer) {
                drawPointer(mediumQualityImage, trueArea);
            }
            return mediumQualityImage;
        }
    }

    private BufferedImage createHighQualityScreenCapture(boolean drawPointer) {
        if (changedHighSettings()) {
            refreshHighSettings();
        }
        BufferedImage screenCapture = screenCaptureRobot.createScreenCapture(new Rectangle(0, 0, highWidth, highHeight));
        highQualityPixelBufferInt = ((DataBufferInt) highQualityImage.getRaster().getDataBuffer()).getData();
        highQualityPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels(highQualityPixelBufferInt);
        screenCapture.flush();
        screenCapture = null;
        highQualityPixelBufferInt = null;
        if (changedHighSettings()) {
            refreshHighSettings();
            return null;
        } else {
            if (drawPointer) {
                drawPointer(highQualityImage);
            }
            return highQualityImage;
        }
    }

    private BufferedImage createHighQualityScreenCapture(boolean drawPointer, Rectangle area) {
        if (changedHighSettings()) {
            refreshHighSettings();
        }
        Rectangle trueArea = new Rectangle(area.x, area.y, Math.min(area.width, highWidth - area.x), Math.min(area.height, highHeight - area.y));
        BufferedImage screenCapture = screenCaptureRobot.createScreenCapture(trueArea);
        if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= (screenCapture.getWidth() * screenCapture.getHeight())) {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels(sectionPixelBufferInt);
        } else {
            sectionPixelBufferInt = new SAWARGBPixelGrabber(screenCapture).getPixels();
        }
        highQualityImage.getRaster().setDataElements(trueArea.x, trueArea.y, trueArea.width, trueArea.height, sectionPixelBufferInt);
        screenCapture.flush();
        screenCapture = null;
        highQualityPixelBufferInt = null;
        if (changedHighSettings()) {
            refreshHighSettings();
            return null;
        } else {
            if (drawPointer) {
                drawPointer(highQualityImage, trueArea);
            }
            return highQualityImage;
        }
    }

    private Rectangle getDeviceBounds(GraphicsDevice graphicsDevice) {
        return graphicsDevice.getDefaultConfiguration().getBounds();
    }

    private void drawPointer(BufferedImage image) {
        PointerInfo info = MouseInfo.getPointerInfo();
        if (info == null) {
            return;
        }
        GraphicsDevice infoDevice = info.getDevice();
        Point pointerLocation = info.getLocation();
        Rectangle bounds = new Rectangle();
        if (infoDevice == null) {
            return;
        } else {
            if (!infoDevice.getIDstring().equals(graphicsDevice.getIDstring())) {
                return;
            }
            bounds = getDeviceBounds(infoDevice);
        }
        pointerLocation.translate(bounds.x * -1, bounds.y * -1);
        int x, y, m, n;
        try {
            x = pointerLocation.x;
            y = pointerLocation.y;
            if (x + 2 < image.getWidth()) {
                image.setRGB(x + 2, y, (image.getRGB(x + 2, y) ^ 0x00FFFFFF));
            }
            if (x - 2 >= 0) {
                image.setRGB(x - 2, y, (image.getRGB(x - 2, y) ^ 0x00FFFFFF));
            }
            if (y + 2 < image.getHeight()) {
                image.setRGB(x, y + 2, (image.getRGB(x, y + 2) ^ 0x00FFFFFF));
            }
            if (y - 2 >= 0) {
                image.setRGB(x, y - 2, (image.getRGB(x, y - 2) ^ 0x00FFFFFF));
            }
            if (x + 2 < image.getWidth() && y + 1 < image.getHeight()) {
                image.setRGB(x + 2, y + 1, (image.getRGB(x + 2, y + 1) ^ 0x00FFFFFF));
            }
            if (x + 2 < image.getWidth() && y - 1 >= 0) {
                image.setRGB(x + 2, y - 1, (image.getRGB(x + 2, y - 1) ^ 0x00FFFFFF));
            }
            if (x - 2 >= 0 && y + 1 < image.getHeight()) {
                image.setRGB(x - 2, y + 1, (image.getRGB(x - 2, y + 1) ^ 0x00FFFFFF));
            }
            if (x - 2 >= 0 && y - 1 >= 0) {
                image.setRGB(x - 2, y - 1, (image.getRGB(x - 2, y - 1) ^ 0x00FFFFFF));
            }
            if (x + 1 < image.getWidth() && y + 2 < image.getHeight()) {
                image.setRGB(x + 1, y + 2, (image.getRGB(x + 1, y + 2) ^ 0x00FFFFFF));
            }
            if (x + 1 < image.getWidth() && y - 2 >= 0) {
                image.setRGB(x + 1, y - 2, (image.getRGB(x + 1, y - 2) ^ 0x00FFFFFF));
            }
            if (x - 1 >= 0 && y + 2 < image.getHeight()) {
                image.setRGB(x - 1, y + 2, (image.getRGB(x - 1, y + 2) ^ 0x00FFFFFF));
            }
            if (x - 1 >= 0 && y - 2 >= 0) {
                image.setRGB(x - 1, y - 2, (image.getRGB(x - 1, y - 2) ^ 0x00FFFFFF));
            }
            n = 9;
            x = pointerLocation.x - 2;
            y = pointerLocation.y + 2;
            if (x >= 0 && y < image.getHeight()) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            if (y < image.getHeight()) {
                for (m = 0; (m < n) && (x - m >= 0); m++) {
                    image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
                }
            }
            if (x >= 0) {
                for (m = 0; (m < n) && (y + m < image.getHeight()); m++) {
                    image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x + 2;
            y = pointerLocation.y + 2;
            if (x < image.getWidth() && y < image.getHeight()) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            if (y < image.getHeight()) {
                for (m = 0; (m < n) && (x + m < image.getWidth()); m++) {
                    image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
                }
            }
            if (x < image.getWidth()) {
                for (m = 0; (m < n) && (y + m < image.getHeight()); m++) {
                    image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x + 2;
            y = pointerLocation.y - 2;
            if (x < image.getWidth() && y >= 0) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            if (y >= 0) {
                for (m = 0; (m < n) && (x + m < image.getWidth()); m++) {
                    image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
                }
            }
            if (x < image.getWidth()) {
                for (m = 0; (m < n) && (y - m >= 0); m++) {
                    image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x - 2;
            y = pointerLocation.y - 2;
            if (x >= 0 && y >= 0) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            if (y >= 0) {
                for (m = 0; (m < n) && (x - m >= 0); m++) {
                    image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
                }
            }
            if (x >= 0) {
                for (m = 0; (m < n) && (y - m >= 0); m++) {
                    image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
                }
            }
            n = 8;
            x = pointerLocation.x - 3;
            y = pointerLocation.y + 3;
            if (x >= 0 && y < image.getHeight()) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            if (y < image.getHeight()) {
                for (m = 0; (m < n) && (x - m >= 0); m++) {
                    image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
                }
            }
            if (x >= 0) {
                for (m = 0; (m < n) && (y + m < image.getHeight()); m++) {
                    image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x + 3;
            y = pointerLocation.y + 3;
            if (x < image.getWidth() && y < image.getHeight()) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            if (y < image.getHeight()) {
                for (m = 0; (m < n) && (x + m < image.getWidth()); m++) {
                    image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
                }
            }
            if (x < image.getWidth()) {
                for (m = 0; (m < n) && (y + m < image.getHeight()); m++) {
                    image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x + 3;
            y = pointerLocation.y - 3;
            if (x < image.getWidth() && y >= 0) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            if (y >= 0) {
                for (m = 0; (m < n) && (x + m < image.getWidth()); m++) {
                    image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
                }
            }
            if (x < image.getWidth()) {
                for (m = 0; (m < n) && (y - m >= 0); m++) {
                    image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x - 3;
            y = pointerLocation.y - 3;
            if (x >= 0 && y >= 0) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            if (y >= 0) {
                for (m = 0; (m < n) && (x - m >= 0); m++) {
                    image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
                }
            }
            if (x >= 0) {
                for (m = 0; (m < n) && (y - m >= 0); m++) {
                    image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
                }
            }
        } catch (Exception e) {
        }
    }

    private void drawPointer(BufferedImage image, Rectangle area) {
        PointerInfo info = MouseInfo.getPointerInfo();
        if (info == null) {
            return;
        }
        GraphicsDevice infoDevice = info.getDevice();
        Point pointerLocation = info.getLocation();
        Rectangle bounds = new Rectangle();
        if (infoDevice == null) {
            return;
        } else {
            if (!infoDevice.getIDstring().equals(graphicsDevice.getIDstring())) {
                return;
            }
            bounds = getDeviceBounds(infoDevice);
        }
        pointerLocation.translate(bounds.x * -1, bounds.y * -1);
        int x, y, m, n;
        try {
            x = pointerLocation.x;
            y = pointerLocation.y;
            if (area.contains(x + 2, y)) {
                image.setRGB(x + 2, y, (image.getRGB(x + 2, y) ^ 0x00FFFFFF));
            }
            if (area.contains(x - 2, y)) {
                image.setRGB(x - 2, y, (image.getRGB(x - 2, y) ^ 0x00FFFFFF));
            }
            if (area.contains(x, y + 2)) {
                image.setRGB(x, y + 2, (image.getRGB(x, y + 2) ^ 0x00FFFFFF));
            }
            if (area.contains(x, y - 2)) {
                image.setRGB(x, y - 2, (image.getRGB(x, y - 2) ^ 0x00FFFFFF));
            }
            if (area.contains(x + 2, y + 1)) {
                image.setRGB(x + 2, y + 1, (image.getRGB(x + 2, y + 1) ^ 0x00FFFFFF));
            }
            if (area.contains(x + 2, y - 1)) {
                image.setRGB(x + 2, y - 1, (image.getRGB(x + 2, y - 1) ^ 0x00FFFFFF));
            }
            if (area.contains(x - 2, y + 1)) {
                image.setRGB(x - 2, y + 1, (image.getRGB(x - 2, y + 1) ^ 0x00FFFFFF));
            }
            if (area.contains(x - 2, y - 1)) {
                image.setRGB(x - 2, y - 1, (image.getRGB(x - 2, y - 1) ^ 0x00FFFFFF));
            }
            if (area.contains(x + 1, y + 2)) {
                image.setRGB(x + 1, y + 2, (image.getRGB(x + 1, y + 2) ^ 0x00FFFFFF));
            }
            if (area.contains(x + 1, y - 2)) {
                image.setRGB(x + 1, y - 2, (image.getRGB(x + 1, y - 2) ^ 0x00FFFFFF));
            }
            if (area.contains(x - 1, y + 2)) {
                image.setRGB(x - 1, y + 2, (image.getRGB(x - 1, y + 2) ^ 0x00FFFFFF));
            }
            if (area.contains(x - 1, y - 2)) {
                image.setRGB(x - 1, y - 2, (image.getRGB(x - 1, y - 2) ^ 0x00FFFFFF));
            }
            n = 9;
            x = pointerLocation.x - 2;
            y = pointerLocation.y + 2;
            if (area.contains(x, y)) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x - m, y)) {
                    image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
                }
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x, y + m)) {
                    image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x + 2;
            y = pointerLocation.y + 2;
            if (area.contains(x, y)) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x + m, y)) {
                    image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
                }
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x, y + m)) {
                    image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x + 2;
            y = pointerLocation.y - 2;
            if (area.contains(x, y)) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x + m, y)) {
                    image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
                }
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x, y - m)) {
                    image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x - 2;
            y = pointerLocation.y - 2;
            if (area.contains(x, y)) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x - m, y)) {
                    image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
                }
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x, y - m)) {
                    image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
                }
            }
            n = 8;
            x = pointerLocation.x - 3;
            y = pointerLocation.y + 3;
            if (area.contains(x, y)) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x - m, y)) {
                    image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
                }
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x, y + m)) {
                    image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x + 3;
            y = pointerLocation.y + 3;
            if (area.contains(x, y)) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x + m, y)) {
                    image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
                }
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x, y + m)) {
                    image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x + 3;
            y = pointerLocation.y - 3;
            if (area.contains(x, y)) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x + m, y)) {
                    image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
                }
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x, y - m)) {
                    image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
                }
            }
            x = pointerLocation.x - 3;
            y = pointerLocation.y - 3;
            if (area.contains(x, y)) {
                image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x - m, y)) {
                    image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
                }
            }
            for (m = 0; (m < n); m++) {
                if (area.contains(x, y - m)) {
                    image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
                }
            }
        } catch (Exception e) {
        }
    }

    public BufferedImage createScreenCapture() {
        return createScreenCapture(false);
    }

    public BufferedImage createScreenCapture(Rectangle area) {
        return createScreenCapture(false, area);
    }

    public synchronized BufferedImage createScreenCapture(boolean drawPointer) {
        if (!isScreenCaptureInitialized()) {
            initializeScreenCapture();
        }
        if (colorQuality == SAW_COLOR_QUALITY_HIGH) {
            return createHighQualityScreenCapture(drawPointer);
        } else if (colorQuality == SAW_COLOR_QUALITY_MEDIUM) {
            return createMediumQualityScreenCapture(drawPointer);
        } else {
            return createLowQualityScreenCapture(drawPointer);
        }
    }

    public synchronized BufferedImage createScreenCapture(boolean drawPointer, Rectangle area) {
        if (!isScreenCaptureInitialized()) {
            initializeScreenCapture();
        }
        if (colorQuality == SAW_COLOR_QUALITY_HIGH) {
            return createHighQualityScreenCapture(drawPointer, area);
        } else if (colorQuality == SAW_COLOR_QUALITY_MEDIUM) {
            return createMediumQualityScreenCapture(drawPointer, area);
        } else {
            return createLowQualityScreenCapture(drawPointer, area);
        }
    }
}
