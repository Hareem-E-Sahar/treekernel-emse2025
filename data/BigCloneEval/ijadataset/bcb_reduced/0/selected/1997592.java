package regnumhelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Robot;
import java.awt.Rectangle;
import java.util.*;
import java.util.TimerTask;
import java.util.Timer;

/**
 *
 * @author Niels
 */
public class ScreenGrabber {

    Robot robot = null;

    Main main = null;

    private BufferedImage posImage = null;

    private BufferedImage healthManaImage = null;

    private BufferedImage targetNameImage = null;

    Timer screenGrabbingTimer = null;

    private int refreshRate = 1000;

    private Vector pictureChangeListener = new Vector();

    public BufferedImage getPosImage() {
        return posImage;
    }

    public BufferedImage getHealthManaImage() {
        return healthManaImage;
    }

    public BufferedImage getTargetNameImage() {
        return targetNameImage;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    public void addPictureChangedListener(PictureChangedListener l) {
        this.pictureChangeListener.add(l);
    }

    public void removePictureChangedListener(PictureChangedListener l) {
        this.pictureChangeListener.remove(l);
    }

    public synchronized void informPictureChangedListener(int imageType) {
        Iterator iter = pictureChangeListener.iterator();
        while (iter.hasNext()) {
            PictureChangedListener l = (PictureChangedListener) iter.next();
            l.pictureChanged(this, imageType);
        }
        iter = null;
    }

    public void startGrabbing() {
        if (screenGrabbingTimer != null) screenGrabbingTimer.cancel();
        screenGrabbingTimer = new Timer();
        TimerTask tt = new TimerTask() {

            public void run() {
                captureImage();
            }
        };
        screenGrabbingTimer = new Timer();
        screenGrabbingTimer.schedule(tt, 3000, refreshRate);
    }

    public void stopGrabbing() {
        if (screenGrabbingTimer != null) {
            screenGrabbingTimer.cancel();
        }
    }

    public ScreenGrabber(Main main) throws AWTException {
        robot = new Robot();
        this.main = main;
    }

    public void captureImage() {
        Rectangle posRect = new Rectangle(main.getSettings().getPosRectangle());
        Rectangle heathRect = main.getSettings().getHealthManaRectangle();
        Rectangle targetRect = new Rectangle(main.getSettings().getTargetNameRectangle());
        posRect.y -= TempSettings.getPosTextCalibrationOffeset();
        posRect.height = 10 + 2 * TempSettings.getPosTextCalibrationOffeset();
        targetRect.y -= TempSettings.getTargetTextCalibrationOffeset();
        targetRect.height = 10 + 2 * TempSettings.getTargetTextCalibrationOffeset();
        robot.delay(100);
        posImage = robot.createScreenCapture(posRect);
        informPictureChangedListener(PictureChangedListener.IMAGE_POSITION);
        if (main.getSettings().isCaptureManaHealthEnabled()) {
            robot.delay(100);
            healthManaImage = robot.createScreenCapture(heathRect);
            informPictureChangedListener(PictureChangedListener.IMAGE_MANA_HEALTH);
        }
        if (main.getSettings().isCaptureTargetEnabled()) {
            robot.delay(100);
            targetNameImage = robot.createScreenCapture(targetRect);
            informPictureChangedListener(PictureChangedListener.IMAGE_TARGET_NAME);
        }
        informPictureChangedListener(PictureChangedListener.IMAGE_ALL_UPDATED);
    }
}
