package org.red5.screen.webstart.tgui;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.red5.screen.webstart.BlankArea;
import org.red5.screen.webstart.ScreenShareRTMPT;

public class VirtualScreen {

    public static VirtualScreen instance = null;

    public boolean showWarning = true;

    public boolean doUpdateBounds = true;

    public VirtualScreen() throws Exception {
        instance = this;
        ScreenShareRTMPT.instance.tFieldScreenZoom = new JLabel();
        ScreenShareRTMPT.instance.tFieldScreenZoom.setBounds(10, 120, 200, 20);
        ScreenShareRTMPT.instance.tFieldScreenZoom.setText("Select your screen Area:");
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.tFieldScreenZoom);
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        VirtualScreenBean.screenratio = screenSize.getHeight() / screenSize.getWidth();
        VirtualScreenBean.screenWidthMax = Double.valueOf(screenSize.getWidth()).intValue();
        VirtualScreenBean.screenHeightMax = Double.valueOf(screenSize.getHeight()).intValue();
        VirtualScreenBean.vScreenHeight = Long.valueOf(Math.round(VirtualScreenBean.vScreenWidth * VirtualScreenBean.screenratio)).intValue();
        int width = VirtualScreenBean.vScreenWidth;
        int height = Long.valueOf(Math.round(width * VirtualScreenBean.screenratio)).intValue();
        ScreenShareRTMPT.instance.vScreenIconLeft = new JLabel();
        ScreenShareRTMPT.instance.vScreenIconLeft.setBounds(14, 162 + (height / 2), 32, 16);
        ScreenShareRTMPT.instance.vScreenIconRight = new JLabel();
        ScreenShareRTMPT.instance.vScreenIconRight.setBounds(30 + width - 16, 162 + (height / 2), 32, 16);
        ScreenShareRTMPT.instance.vScreenIconUp = new JLabel();
        ScreenShareRTMPT.instance.vScreenIconUp.setBounds(30 + (width / 2) - 8, 162 - 8, 16, 32);
        ScreenShareRTMPT.instance.vScreenIconDown = new JLabel();
        ScreenShareRTMPT.instance.vScreenIconDown.setBounds(30 + (width / 2) - 8, 162 + height - 8, 16, 32);
        Image im_left = ImageIO.read(ScreenShareRTMPT.class.getResource("/1leftarrow.png"));
        ImageIcon iIcon1 = new ImageIcon(im_left);
        Image im_right = ImageIO.read(ScreenShareRTMPT.class.getResource("/1rightarrow.png"));
        ImageIcon iIcon2 = new ImageIcon(im_right);
        Image im_up = ImageIO.read(ScreenShareRTMPT.class.getResource("/1uparrow.png"));
        ImageIcon iIcon3 = new ImageIcon(im_up);
        Image im_down = ImageIO.read(ScreenShareRTMPT.class.getResource("/1downarrow.png"));
        ImageIcon iIcon4 = new ImageIcon(im_down);
        JLabel jLab1 = new JLabel(iIcon1);
        jLab1.setBounds(0, 0, 16, 16);
        JLabel jLab2 = new JLabel(iIcon2);
        jLab2.setBounds(16, 0, 16, 16);
        ScreenShareRTMPT.instance.vScreenIconLeft.add(jLab1);
        ScreenShareRTMPT.instance.vScreenIconLeft.add(jLab2);
        ScreenShareRTMPT.instance.vScreenIconLeft.setToolTipText("Change width");
        VirtualScreenXMouseListener xLeftMouseListener = new VirtualScreenXMouseListener();
        ScreenShareRTMPT.instance.vScreenIconLeft.addMouseListener(xLeftMouseListener);
        ScreenShareRTMPT.instance.vScreenIconLeft.addMouseMotionListener(xLeftMouseListener);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.vScreenIconLeft);
        JLabel jLab3 = new JLabel(iIcon1);
        jLab3.setBounds(0, 0, 16, 16);
        JLabel jLab4 = new JLabel(iIcon2);
        jLab4.setBounds(16, 0, 16, 16);
        ScreenShareRTMPT.instance.vScreenIconRight.add(jLab3);
        ScreenShareRTMPT.instance.vScreenIconRight.add(jLab4);
        ScreenShareRTMPT.instance.vScreenIconRight.setToolTipText("Change width");
        VirtualScreenWidthMouseListener widthMouseListener = new VirtualScreenWidthMouseListener();
        ScreenShareRTMPT.instance.vScreenIconRight.addMouseListener(widthMouseListener);
        ScreenShareRTMPT.instance.vScreenIconRight.addMouseMotionListener(widthMouseListener);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.vScreenIconRight);
        JLabel jLab5 = new JLabel(iIcon3);
        jLab5.setBounds(0, 0, 16, 16);
        JLabel jLab6 = new JLabel(iIcon4);
        jLab6.setBounds(0, 16, 16, 16);
        ScreenShareRTMPT.instance.vScreenIconUp.add(jLab5);
        ScreenShareRTMPT.instance.vScreenIconUp.add(jLab6);
        ScreenShareRTMPT.instance.vScreenIconUp.setToolTipText("Change height");
        VirtualScreenYMouseListener yMouseListener = new VirtualScreenYMouseListener();
        ScreenShareRTMPT.instance.vScreenIconUp.addMouseListener(yMouseListener);
        ScreenShareRTMPT.instance.vScreenIconUp.addMouseMotionListener(yMouseListener);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.vScreenIconUp);
        JLabel jLab7 = new JLabel(iIcon3);
        jLab7.setBounds(0, 0, 16, 16);
        JLabel jLab8 = new JLabel(iIcon4);
        jLab8.setBounds(0, 16, 16, 16);
        ScreenShareRTMPT.instance.vScreenIconDown.add(jLab7);
        ScreenShareRTMPT.instance.vScreenIconDown.add(jLab8);
        ScreenShareRTMPT.instance.vScreenIconDown.setToolTipText("Change height");
        VirtualScreenHeightMouseListener heightMouseListener = new VirtualScreenHeightMouseListener();
        ScreenShareRTMPT.instance.vScreenIconDown.addMouseListener(heightMouseListener);
        ScreenShareRTMPT.instance.vScreenIconDown.addMouseMotionListener(heightMouseListener);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.vScreenIconDown);
        ScreenShareRTMPT.instance.virtualScreen = new BlankArea(new Color(255, 255, 255, 100));
        ScreenShareRTMPT.instance.virtualScreen.setOpaque(true);
        ScreenShareRTMPT.instance.virtualScreen.setHorizontalAlignment(SwingConstants.LEFT);
        ScreenShareRTMPT.instance.virtualScreen.setVerticalAlignment(SwingConstants.TOP);
        ScreenShareRTMPT.instance.virtualScreen.setText(VirtualScreenBean.screenWidthMax + ":" + VirtualScreenBean.screenHeightMax);
        ScreenShareRTMPT.instance.virtualScreen.setBounds(30, 170, VirtualScreenBean.vScreenWidth, VirtualScreenBean.vScreenHeight);
        VirtualScreenMouseListener vListener = new VirtualScreenMouseListener();
        ScreenShareRTMPT.instance.virtualScreen.addMouseListener(vListener);
        ScreenShareRTMPT.instance.virtualScreen.addMouseMotionListener(vListener);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.virtualScreen);
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage imageScreen = robot.createScreenCapture(screenRectangle);
        Image img = imageScreen.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        System.out.println("img" + img);
        ImageIcon image = new ImageIcon(img);
        ScreenShareRTMPT.instance.blankArea = new JLabel(image);
        ScreenShareRTMPT.instance.blankArea.setBounds(30, 170, width, height);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.blankArea);
        VirtualScreenBean.vScreenSpinnerX = 0;
        ScreenShareRTMPT.instance.vscreenXLabel = new JLabel();
        ScreenShareRTMPT.instance.vscreenXLabel.setText("SharingScreen X:");
        ScreenShareRTMPT.instance.vscreenXLabel.setBounds(250, 170, 150, 24);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.vscreenXLabel);
        ScreenShareRTMPT.instance.jVScreenXSpin = new JSpinner(new SpinnerNumberModel(VirtualScreenBean.vScreenSpinnerX, 0, VirtualScreenBean.screenWidthMax, 1));
        ScreenShareRTMPT.instance.jVScreenXSpin.setBounds(400, 170, 60, 24);
        ScreenShareRTMPT.instance.jVScreenXSpin.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                calcNewValueXSpin();
            }
        });
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.jVScreenXSpin);
        VirtualScreenBean.vScreenSpinnerY = 0;
        ScreenShareRTMPT.instance.vscreenYLabel = new JLabel();
        ScreenShareRTMPT.instance.vscreenYLabel.setText("SharingScreen Y:");
        ScreenShareRTMPT.instance.vscreenYLabel.setBounds(250, 200, 150, 24);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.vscreenYLabel);
        ScreenShareRTMPT.instance.jVScreenYSpin = new JSpinner(new SpinnerNumberModel(VirtualScreenBean.vScreenSpinnerY, 0, VirtualScreenBean.screenHeightMax, 1));
        ScreenShareRTMPT.instance.jVScreenYSpin.setBounds(400, 200, 60, 24);
        ScreenShareRTMPT.instance.jVScreenYSpin.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                calcNewValueYSpin();
            }
        });
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.jVScreenYSpin);
        VirtualScreenBean.vScreenSpinnerWidth = VirtualScreenBean.screenWidthMax;
        ScreenShareRTMPT.instance.vscreenWidthLabel = new JLabel();
        ScreenShareRTMPT.instance.vscreenWidthLabel.setText("SharingScreen Width:");
        ScreenShareRTMPT.instance.vscreenWidthLabel.setBounds(250, 240, 150, 24);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.vscreenWidthLabel);
        ScreenShareRTMPT.instance.jVScreenWidthSpin = new JSpinner(new SpinnerNumberModel(VirtualScreenBean.vScreenSpinnerWidth, 0, VirtualScreenBean.screenWidthMax, 1));
        ScreenShareRTMPT.instance.jVScreenWidthSpin.setBounds(400, 240, 60, 24);
        ScreenShareRTMPT.instance.jVScreenWidthSpin.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                calcNewValueWidthSpin();
            }
        });
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.jVScreenWidthSpin);
        VirtualScreenBean.vScreenSpinnerHeight = VirtualScreenBean.screenHeightMax;
        ScreenShareRTMPT.instance.vscreenHeightLabel = new JLabel();
        ScreenShareRTMPT.instance.vscreenHeightLabel.setText("SharingScreen Height:");
        ScreenShareRTMPT.instance.vscreenHeightLabel.setBounds(250, 270, 150, 24);
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.vscreenHeightLabel);
        ScreenShareRTMPT.instance.jVScreenHeightSpin = new JSpinner(new SpinnerNumberModel(VirtualScreenBean.vScreenSpinnerHeight, 0, VirtualScreenBean.screenHeightMax, 1));
        ScreenShareRTMPT.instance.jVScreenHeightSpin.setBounds(400, 270, 60, 24);
        ScreenShareRTMPT.instance.jVScreenHeightSpin.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                calcNewValueHeightSpin();
            }
        });
        ScreenShareRTMPT.instance.t.add(ScreenShareRTMPT.instance.jVScreenHeightSpin);
    }

    void calcNewValueXSpin() {
        if (this.doUpdateBounds) {
            int newX = Integer.valueOf(ScreenShareRTMPT.instance.jVScreenXSpin.getValue().toString()).intValue();
            if (VirtualScreenBean.vScreenSpinnerWidth + newX > VirtualScreenBean.screenWidthMax) {
                newX = VirtualScreenBean.screenWidthMax - VirtualScreenBean.vScreenSpinnerWidth;
                ScreenShareRTMPT.instance.jVScreenXSpin.setValue(newX);
                if (this.showWarning) ScreenShareRTMPT.instance.showBandwidthWarning("Reduce the width of the SharingScreen before you try to move it left");
            } else {
                VirtualScreenBean.vScreenSpinnerX = newX;
                updateVScreenBounds();
            }
        } else {
            VirtualScreenBean.vScreenSpinnerX = Integer.valueOf(ScreenShareRTMPT.instance.jVScreenXSpin.getValue().toString()).intValue();
        }
    }

    void calcNewValueYSpin() {
        if (this.doUpdateBounds) {
            int newY = Integer.valueOf(ScreenShareRTMPT.instance.jVScreenYSpin.getValue().toString()).intValue();
            if (VirtualScreenBean.vScreenSpinnerHeight + newY > VirtualScreenBean.screenHeightMax) {
                newY = VirtualScreenBean.screenHeightMax - VirtualScreenBean.vScreenSpinnerHeight;
                ScreenShareRTMPT.instance.jVScreenYSpin.setValue(newY);
                if (this.showWarning) ScreenShareRTMPT.instance.showBandwidthWarning("Reduce the height of the SharingScreen before you try to move it bottom");
            } else {
                VirtualScreenBean.vScreenSpinnerY = newY;
                updateVScreenBounds();
            }
        } else {
            VirtualScreenBean.vScreenSpinnerY = Integer.valueOf(ScreenShareRTMPT.instance.jVScreenYSpin.getValue().toString()).intValue();
        }
    }

    void calcNewValueWidthSpin() {
        if (this.doUpdateBounds) {
            int newWidth = Integer.valueOf(ScreenShareRTMPT.instance.jVScreenWidthSpin.getValue().toString()).intValue();
            if (VirtualScreenBean.vScreenSpinnerX + newWidth > VirtualScreenBean.screenWidthMax) {
                newWidth = VirtualScreenBean.screenWidthMax - VirtualScreenBean.vScreenSpinnerX;
                ScreenShareRTMPT.instance.jVScreenWidthSpin.setValue(newWidth);
                if (this.showWarning) ScreenShareRTMPT.instance.showBandwidthWarning("Reduce the x of the SharingScreen before you try to make it wider");
            } else {
                VirtualScreenBean.vScreenSpinnerWidth = newWidth;
                updateVScreenBounds();
            }
        } else {
            VirtualScreenBean.vScreenSpinnerWidth = Integer.valueOf(ScreenShareRTMPT.instance.jVScreenWidthSpin.getValue().toString()).intValue();
        }
    }

    void calcNewValueHeightSpin() {
        if (this.doUpdateBounds) {
            int newHeight = Integer.valueOf(ScreenShareRTMPT.instance.jVScreenHeightSpin.getValue().toString()).intValue();
            if (VirtualScreenBean.vScreenSpinnerY + newHeight > VirtualScreenBean.screenHeightMax) {
                newHeight = VirtualScreenBean.screenHeightMax - VirtualScreenBean.vScreenSpinnerY;
                ScreenShareRTMPT.instance.jVScreenHeightSpin.setValue(newHeight);
                if (this.showWarning) ScreenShareRTMPT.instance.showBandwidthWarning("Reduce the y of the SharingScreen before you try to make it higher");
            } else {
                VirtualScreenBean.vScreenSpinnerHeight = newHeight;
                updateVScreenBounds();
            }
        } else {
            VirtualScreenBean.vScreenSpinnerHeight = Integer.valueOf(ScreenShareRTMPT.instance.jVScreenHeightSpin.getValue().toString()).intValue();
        }
    }

    /**
	 * update the bounds of the vScreen
	 * by useing the vars from the Spinners
	 *
	 */
    void updateVScreenBounds() {
        double newvScreenWidth = VirtualScreenBean.vScreenSpinnerWidth * (new Double(VirtualScreenBean.vScreenWidth) / new Double(VirtualScreenBean.screenWidthMax));
        double newvScreenX = VirtualScreenBean.vScreenSpinnerX * (new Double(VirtualScreenBean.vScreenWidth) / new Double(VirtualScreenBean.screenWidthMax));
        double newvScreenHeight = VirtualScreenBean.vScreenSpinnerHeight * (new Double(VirtualScreenBean.vScreenHeight) / new Double(VirtualScreenBean.screenHeightMax));
        double newvScreenY = VirtualScreenBean.vScreenSpinnerY * (new Double(VirtualScreenBean.vScreenHeight) / new Double(VirtualScreenBean.screenHeightMax));
        ScreenShareRTMPT.instance.vScreenIconLeft.setLocation(Long.valueOf(Math.round(newvScreenX)).intValue() + 30 - 16, Long.valueOf(Math.round(newvScreenY)).intValue() + 162 + (Long.valueOf(Math.round(newvScreenHeight)).intValue() / 2));
        ScreenShareRTMPT.instance.vScreenIconRight.setLocation(Long.valueOf(Math.round(newvScreenX)).intValue() + 30 + Long.valueOf(Math.round(newvScreenWidth)).intValue() - 16, Long.valueOf(Math.round(newvScreenY)).intValue() + 162 + ((Long.valueOf(Math.round(newvScreenHeight)).intValue()) / 2));
        ScreenShareRTMPT.instance.vScreenIconUp.setLocation(Long.valueOf(Math.round(newvScreenX)).intValue() + 30 + (Long.valueOf(Math.round(newvScreenWidth)).intValue() / 2) - 8, Long.valueOf(Math.round(newvScreenY)).intValue() + 162 - 8);
        ScreenShareRTMPT.instance.vScreenIconDown.setLocation(Long.valueOf(Math.round(newvScreenX)).intValue() + 30 + (Long.valueOf(Math.round(newvScreenWidth)).intValue() / 2) - 8, Long.valueOf(Math.round(newvScreenY)).intValue() + 162 - 8 + (Long.valueOf(Math.round(newvScreenHeight)).intValue()));
        ScreenShareRTMPT.instance.virtualScreen.setBounds(30 + Long.valueOf(Math.round(newvScreenX)).intValue(), 170 + Long.valueOf(Math.round(newvScreenY)).intValue(), Long.valueOf(Math.round(newvScreenWidth)).intValue(), Long.valueOf(Math.round(newvScreenHeight)).intValue());
        ScreenShareRTMPT.instance.virtualScreen.setText(VirtualScreenBean.vScreenSpinnerWidth + ":" + VirtualScreenBean.vScreenSpinnerHeight);
    }
}
