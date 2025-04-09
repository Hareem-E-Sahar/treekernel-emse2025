package com.mobiwebinc.compconn.screen;

import com.mobiwebinc.compconn.communication.SystemController;
import com.mobiwebinc.compconn.configuration.ServerConfiguration;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author suraj
 */
public class ScreenPackager implements Runnable {

    private static final int BOOSTER = 5;

    SystemController[] systems;

    private static ScreenPackager screenPackager = null;

    private BufferedImage[] screens;

    int connectedTo;

    boolean running = false;

    BlockingQueue<Integer> requests;

    Rectangle[] screenRects;

    private int[] currentImageNos;

    private ScreenPackager() {
        screenRects = ServerConfiguration.getInstance().getCleanedScreenRects();
        systems = SystemController.getInstances();
        requests = new LinkedBlockingQueue<Integer>();
        currentImageNos = new int[systems.length];
        screens = new BufferedImage[systems.length];
    }

    public static ScreenPackager getInstance() {
        if (screenPackager == null) {
            screenPackager = new ScreenPackager();
        }
        return screenPackager;
    }

    public void run() {
        running = true;
        while (connectedTo > 0) {
            try {
                captureScreen(requests.take());
            } catch (InterruptedException ex) {
                Logger.getLogger(ScreenPackager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        running = false;
    }

    public synchronized void disconnected() {
        ServerConfiguration.getInstance().reloadScreens();
        connectedTo--;
        if (connectedTo == 0) {
            screenPackager = null;
        }
    }

    public synchronized void connected() {
        ServerConfiguration.getInstance().reloadScreens();
        connectedTo++;
        if (!running) {
            new Thread(this).start();
        }
    }

    public void stopIt() {
        connectedTo = 0;
    }

    /**
     * @return the screen
     */
    public synchronized BufferedImage getScreen(int monitor) {
        return systems[monitor].createScreenCapture(screenRects[monitor]);
    }

    /**
     * @param screen the screen to set
     */
    public synchronized void setScreen(int monitor, BufferedImage screen) {
        this.screens[monitor] = screen;
    }

    /**
     * @return the currentImageNo
     */
    public int getCurrentImageNo(int monitor) {
        requests.offer(monitor);
        requests.offer(monitor);
        return currentImageNos[monitor];
    }

    private void captureScreen(int monitor) {
        setScreen(monitor, systems[monitor].createScreenCapture(screenRects[monitor]));
        currentImageNos[monitor] = (currentImageNos[monitor] + 1) % Integer.MAX_VALUE;
        System.out.println("current " + currentImageNos[monitor]);
    }
}
