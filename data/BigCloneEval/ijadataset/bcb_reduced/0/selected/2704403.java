package net.sourceforge.entrainer.gui;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JWindow;
import net.sourceforge.entrainer.gui.animation.AnimationRegister;
import net.sourceforge.entrainer.gui.animation.EntrainerAnimation;
import net.sourceforge.entrainer.guitools.GuiUtil;
import net.sourceforge.entrainer.mediator.EntrainerMediator;
import net.sourceforge.entrainer.mediator.MediatorConstants;
import net.sourceforge.entrainer.mediator.ReceiverAdapter;
import net.sourceforge.entrainer.mediator.ReceiverChangeEvent;
import net.sourceforge.entrainer.sound.tools.FrequencyToHalfTimeCycle;
import net.sourceforge.entrainer.util.Utils;
import net.sourceforge.entrainer.xml.Settings;

/**
 * This class draws itself to cover the entire screen. It provides the canvas
 * upon which animations are drawn.
 * 
 * @author burton
 */
public class AnimationWindow extends JWindow {

    private static final long serialVersionUID = 1L;

    private BufferedImage background;

    private BufferedImage offscreenImage;

    private boolean isStarted = false;

    private boolean isRunning = false;

    private EntrainerAnimation entrainerAnimation;

    private int yOffset;

    private Image customImage = null;

    private FrequencyToHalfTimeCycle calculator = new FrequencyToHalfTimeCycle();

    public AnimationWindow() {
        super();
        initEntrainerAnimation();
        setYOffset();
        initGui();
        initMediator();
    }

    public void setVisible(boolean b) {
        if (!b) {
            getEntrainerAnimation().clearAnimation();
        } else {
            initBackground();
        }
        super.setVisible(b);
    }

    /**
	 * Overridden. Not invoked directly; called via <code>repaint();</code>
	 */
    public void paint(Graphics g) {
        BufferedImage offscreenImage = getCompatibleImage();
        Graphics2D offScreen = offscreenImage.createGraphics();
        if (!getEntrainerAnimation().useBackgroundColour()) {
            if (getCustomImage() == null) {
                offScreen.drawImage(background, 0, 0, null);
            } else {
                offScreen.drawImage(getCustomImage(), 0, 0, null);
            }
        } else {
            offScreen.drawImage(getEntrainerAnimation().getCustomImage(), 0, 0, null);
        }
        getEntrainerAnimation().animate(offScreen);
        g.drawImage(offscreenImage, 0, 0, this);
    }

    EntrainerAnimation getEntrainerAnimation() {
        return entrainerAnimation;
    }

    void setEntrainerAnimation(EntrainerAnimation entrainerAnimation) {
        if (this.entrainerAnimation != null) {
            this.entrainerAnimation.clearAnimation();
        }
        this.entrainerAnimation = entrainerAnimation;
        if (null != entrainerAnimation && entrainerAnimation.useBackgroundColour()) {
            setBackground(entrainerAnimation.getBackgroundColour());
        }
    }

    private void initBackground() {
        initDesktopBackground();
    }

    private void initDesktopBackground() {
        Entrainer frame = Entrainer.getInstance();
        frame.toBack();
        frame.setExtendedState(Frame.ICONIFIED);
        while (frame.getExtendedState() != Frame.ICONIFIED) {
        }
        Utils.snooze(500);
        initBackground(getScreenSize());
        if (!getEntrainerAnimation().isHideEntrainerFrame()) {
            frame.setExtendedState(Frame.NORMAL);
            frame.toFront();
        }
    }

    private BufferedImage getCompatibleImage() {
        if (offscreenImage == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
            offscreenImage = gc.createCompatibleImage(getScreenSize().width, getScreenSize().height);
        }
        return offscreenImage;
    }

    private void initEntrainerAnimation() {
        initEntrainerAnimation(Settings.getInstance().getAnimationProgram());
        initAnimationBackground(Settings.getInstance().getAnimationBackground());
    }

    private void initEntrainerAnimation(String stringRep) {
        List<EntrainerAnimation> animations = AnimationRegister.getEntrainerAnimations();
        if (null == stringRep && !animations.isEmpty()) {
            setEntrainerAnimation(animations.get(0));
        } else if (null == getEntrainerAnimation()) {
            setEntrainerAnimation(AnimationRegister.getEntrainerAnimation(stringRep));
        } else if (!stringRep.equals(getEntrainerAnimation().toString())) {
            setEntrainerAnimation(AnimationRegister.getEntrainerAnimation(stringRep));
        }
    }

    private void initGui() {
        Dimension size = getScreenSize();
        setSize(size);
        setLocation(new Point(0, getYOffset()));
        initBackground(size);
    }

    private Dimension getScreenSize() {
        return GuiUtil.getWorkingVirtualScreenSize();
    }

    private void initBackground(Dimension size) {
        try {
            Robot robot = new Robot();
            background = robot.createScreenCapture(new Rectangle(new Point(0, getYOffset()), size));
        } catch (AWTException e) {
            GuiUtil.handleProblem(e);
        }
    }

    private void initMediator() {
        EntrainerMediator.getInstance().addReceiver(new ReceiverAdapter(this) {

            @Override
            protected void processReceiverChangeEvent(ReceiverChangeEvent e) {
                MediatorConstants parm = e.getParm();
                switch(parm) {
                    case ANIMATION_BACKGROUND:
                        initAnimationBackground(e.getStringValue());
                        break;
                    case ANIMATION_PROGRAM:
                        initEntrainerAnimation(e.getStringValue());
                        break;
                    case ENTRAINMENT_FREQUENCY:
                        setEntrainmentFrequency(e.getDoubleValue());
                        break;
                    case START_ENTRAINMENT:
                        if (!e.getBooleanValue()) {
                            getEntrainerAnimation().clearAnimation();
                        } else {
                            setStarted(e.getBooleanValue());
                            checkStart();
                            startAnimationThread();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void initAnimationBackground(String animationBackground) {
        if (animationBackground == null || animationBackground.trim().length() == 0) {
            setCustomImage(null);
        } else {
            setCustomImage(GuiUtil.getImage(animationBackground));
        }
    }

    private void checkStart() {
        if (!isRunning && isStarted()) {
            isRunning = true;
        }
    }

    private void startAnimationThread() {
        Thread t = new Thread("Animation Window Animation Thread") {

            public void run() {
                setPriority(Thread.MAX_PRIORITY);
                while (isStarted()) {
                    long l = getMillis() > 5000 ? 5000 : getMillis();
                    Utils.snooze(l, getNanos());
                    repaint();
                }
                isRunning = false;
            }
        };
        t.start();
    }

    private void setEntrainmentFrequency(double entFreq) {
        calculator.setFrequency(entFreq);
    }

    private long getMillis() {
        return calculator.getMillis();
    }

    private int getNanos() {
        return calculator.getNanos();
    }

    private boolean isStarted() {
        return isStarted;
    }

    private void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    private int getYOffset() {
        return yOffset;
    }

    private void setYOffset() {
        if (isMac()) {
            setYOffset(22);
        } else {
            setYOffset(0);
        }
    }

    private boolean isMac() {
        return System.getProperty("os.name").contains("Mac");
    }

    private void setYOffset(int offset) {
        yOffset = offset;
    }

    private Image getCustomImage() {
        return customImage;
    }

    private void setCustomImage(Image backgroundImage) {
        if (backgroundImage != null) {
            backgroundImage = GuiUtil.scaleImage(backgroundImage, getScreenSize());
        }
        this.customImage = backgroundImage;
    }
}
