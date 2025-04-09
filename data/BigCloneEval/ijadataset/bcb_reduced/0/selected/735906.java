package com.mephi.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.JFrame;

/**
 * This class extends javax.swing.JFrame 
 * @author mephisto
 * @since 2011-04-11
 */
public class MFrame extends JFrame {

    /**
	 * 
	 * The default constructor which creates frame positioned in the center
	 * with height equals a half of the screen height and width equals  
	 * a half of the screen width
	 */
    public MFrame() {
        this(0, 0);
    }

    /**
	 * 
	 * @param additionalWidth to add the width of the frame
	 * @param additionalHeight to add the height of the frame
	 */
    public MFrame(int additionalWidth, int additionalHeight) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_WIDTH = screenSize.width;
        SCREEN_HEIGHT = screenSize.height;
        this.additionalWidth = additionalWidth;
        this.additionalHeight = additionalHeight;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameWidth = (SCREEN_WIDTH + additionalWidth) / 2;
        frameHeight = (SCREEN_HEIGHT + additionalHeight) / 2;
        this.setPreferredSize(new Dimension(frameWidth, frameHeight));
        this.setSize(this.getPreferredSize());
        this.pack();
        this.setBackground(Color.LIGHT_GRAY);
        this.setVisible(true);
        setFrame();
    }

    /**
	 * 
	 * @param frameWidth the frameWidth to set
	 * @param frameHeight the frameHeight to set
	 */
    public void setFrame(int frameWidth, int frameHeight) {
        this.setSize(frameWidth, frameHeight);
        this.setLocation((SCREEN_WIDTH - frameWidth) / 2, (SCREEN_HEIGHT - frameHeight) / 2);
    }

    /**
	 * 
	 * @return the left corner of the frame
	 */
    public Point getLeftCorner() {
        return new Point(this.getLocation().x - frameWidth / 4, this.getLocation().y - frameHeight / 4);
    }

    /**
	 * 
	 * The method adds a component with given guidelines 
	 * @param c component which is added
	 * @param guidelines to define constraints
	 * @param x coordinate x of the area
	 * @param y coordinate y of the area
	 * @param width to set the width of area
	 * @param height to set the width of area
	 */
    public void addToContainer(Component c, GridBagConstraints guidelines, int x, int y, int width, int height) {
        guidelines.gridx = x;
        guidelines.gridy = y;
        guidelines.gridwidth = width;
        guidelines.gridheight = height;
        this.getContentPane().add(c, guidelines);
    }

    private void setFrame() {
        this.setSize((SCREEN_WIDTH + additionalWidth) / 2, (SCREEN_HEIGHT + additionalHeight) / 2);
        this.setLocation((SCREEN_WIDTH - additionalWidth) / 4, (SCREEN_HEIGHT - additionalHeight) / 4);
    }

    /**
	 * The width of the current screen resolution 
	 */
    static int SCREEN_WIDTH;

    /**
	 * The height of the current screen resolution 
	 */
    static int SCREEN_HEIGHT;

    private static final long serialVersionUID = 763518223511526558L;

    private int additionalWidth;

    private int additionalHeight;

    private int frameWidth;

    private int frameHeight;
}
