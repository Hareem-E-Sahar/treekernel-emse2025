package regnumhelper.mule;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Handles building an inventory from the screenshot.
 * @author Michael Speth
 * @version 1.0
 */
public class MuleInventory {

    /**
     * Handles all the parsing.
     **/
    private MuleOCR muleOCR;

    public Vector itemsV;

    private Robot robot;

    /**
     * Used for finding the middle of the tab on the y-axis.
     **/
    private static final int TAB_Y_OFFSET = 10;

    /**
     * Used for finding the middle of the tab for the x-axis.
     **/
    private static final int TAB_X_OFFSET = 18;

    /**
     * Used for finding the next tab.
     **/
    private static final int TAB_X_NXT_OFFSET = 38;

    /**
     * The X Offset for the inventory box.
     **/
    private static final int INV_BOX_X_OFFSET = 20;

    /**
     * The y offset for the inventory box.
     **/
    private static final int INV_BOX_Y_OFFSET = 16;

    /**
     * The pixel distance between text in the inventory.
     **/
    private static final int ITEM_OFFSET = 18;

    /**
     * The pixel distance between the mouse cursor and the item window.
     **/
    private static final int ITEM_Y_OFFSET = 30;

    /**
     * The width of the item window.
     **/
    private static final int ITEM_WIDTH = 250;

    /**
     * The height of the item window.
     **/
    private static final int ITEM_HEIGHT = 270;

    public MuleInventory() {
        muleOCR = new MuleOCR();
        itemsV = new Vector();
        try {
            robot = new Robot();
            robot.setAutoDelay(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all the data from the series of images and fills the internel data structure with item information.
     * @param rootDir the destination for the screen caps.
     **/
    public void getData(int x1, int y1, int x2, int y2, String rootDir) {
        Vector imagesV = new Vector();
        itemsV.removeAllElements();
        Rectangle rect = new Rectangle(x1 + INV_BOX_X_OFFSET, y1 + INV_BOX_Y_OFFSET, x2, y2);
        for (int i = 0; i < 5; i++) {
            selectInventoryTab(x1, y1, i);
            try {
                imagesV.add(robot.createScreenCapture(rect));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < imagesV.size(); i++) {
            Vector v = muleOCR.getInventoryItems((BufferedImage) imagesV.elementAt(i), i + 1);
            selectInventoryTab(x1, y1, i);
            for (int j = 0; j < v.size(); j++) {
                robot.mouseMove(x1 + x2 - 5, y1 + INV_BOX_Y_OFFSET + 5 + ITEM_OFFSET * (j + 1));
                robot.delay(700);
                getFullItemDescription(x1 + x2, y1 + INV_BOX_Y_OFFSET + 5 + ITEM_OFFSET * (j + 1) + ITEM_Y_OFFSET, i + 1, j + 1, rootDir);
            }
            itemsV.addAll(v);
        }
    }

    /**
     * Takes a screenshot of the item.
     * @param startx the x position to take the screenshot.
     * @param starty the y position to take the screenshot.
     * @param tab the tab this item is from.
     * @param item the number of this item within the tab.
     * @param rootDir the destination directory for the screen caps.
     **/
    private void getFullItemDescription(int startX, int startY, int tab, int item, String rootDir) {
        try {
            Rectangle rect = new Rectangle(startX, startY, ITEM_WIDTH, ITEM_HEIGHT);
            BufferedImage bi = robot.createScreenCapture(rect);
            BufferedImage bi2 = bi.getSubimage(38, 0, ITEM_WIDTH - 38, 36);
            String white = muleOCR.getWhiteText(bi2);
            String yellow = muleOCR.getYellowText(bi2);
            String name;
            System.out.println("w = " + white);
            System.out.println("y = " + yellow);
            if (white == null || white.equals("")) {
                name = yellow;
            } else {
                name = white;
            }
            StringTokenizer stok = new StringTokenizer(name, "\n", false);
            if (stok.hasMoreTokens()) {
                name = stok.nextToken();
            }
            javax.imageio.ImageIO.write(bi, "png", new File(rootDir + File.separator + Item.getFileName(name)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Selects the tab in the inventory.
     * @param tab the tab to be selected.
     **/
    private void selectInventoryTab(int x1, int y1, int tab) {
        try {
            robot.mouseMove(x1 + TAB_X_OFFSET + TAB_X_NXT_OFFSET * tab, y1 + TAB_Y_OFFSET);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        } catch (Exception e) {
        }
    }
}
