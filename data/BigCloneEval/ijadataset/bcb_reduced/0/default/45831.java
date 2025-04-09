import computational.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;

/**
 * @author Massimo Bartoletti
 * @version 1.1
 */
public class CGAnimator extends JPanel implements ActionListener {

    private CGDemoModule demo;

    private CGAnimatorToolBar toolBar;

    private javax.swing.Timer timer;

    private int fps = 1;

    private int frameCounter;

    private CGAnimation animation;

    private CGHistoryDialog history;

    public CGAnimator(CGDemoModule demo) {
        this.demo = demo;
        setBackground(Color.white);
        setLayout(new BorderLayout());
        toolBar = new CGAnimatorToolBar(this);
        add(toolBar, BorderLayout.NORTH);
        toolBar.disableTools();
        int delay = (fps > 0) ? (1000 / fps) : 1000;
        timer = new javax.swing.Timer(delay, this);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);
        frameCounter = 0;
    }

    public void animate(CGCanvas canvas) {
        animation = createAnimation(canvas);
        if (animation != null) toolBar.enableTools(); else toolBar.disableTools();
        if (history != null) {
            history.dispose();
            history = null;
        }
        stop();
    }

    private CGAnimation createAnimation(CGCanvas canvas) {
        String key = getDemo().getResourceName() + ".animation";
        String animationName = getDemo().getString(key);
        try {
            Class animationClass = Class.forName(animationName);
            Constructor animationConstructor = animationClass.getConstructor(new Class[] { canvas.getClass() });
            Object[] args = new Object[] { canvas };
            CGAnimation animation = (CGAnimation) animationConstructor.newInstance(args);
            return animation;
        } catch (Exception ex) {
            getDemo().setStatus("Cannot create animation: " + ex);
            System.err.println("Cannot create animation: " + ex);
            return null;
        }
    }

    public void rewind() {
        if (animation != null) {
            frameCounter = 0;
            repaint();
        }
    }

    public void stepBack() {
        if (animation != null && frameCounter > 0) {
            --frameCounter;
            repaint();
        }
    }

    public void play() {
        if (animation != null) {
            if (frameCounter == animation.length() - 1) frameCounter = 0;
            toolBar.enableTool("StopTool");
            timer.start();
        }
    }

    public void pause() {
        timer.stop();
    }

    public void stop() {
        toolBar.disableTool("StopTool");
        timer.stop();
        CGTool play = toolBar.getTool("PlayTool");
        play.setSelected(false);
        frameCounter = 0;
        repaint();
    }

    public void stepForward() {
        if (animation != null && frameCounter < animation.length() - 1) {
            ++frameCounter;
            repaint();
        }
    }

    public void fastForward() {
        if (animation != null) {
            frameCounter = animation.length() - 1;
            repaint();
        }
    }

    public int getStep() {
        return frameCounter;
    }

    public void setStep(int step) {
        if (animation != null && step >= 0 && step < animation.length()) {
            frameCounter = step;
            repaint();
        }
    }

    public void showHistory() {
        if (animation != null) {
            if (history == null) {
                history = new CGHistoryDialog(this);
                history.highlightEvent(frameCounter);
            }
            history.show();
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (animation != null && frameCounter < animation.length() - 1) stepForward(); else {
            toolBar.disableTool("StopTool");
            CGTool play = toolBar.getTool("PlayTool");
            play.setSelected(false);
            timer.stop();
        }
    }

    public void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        if (animation != null) try {
            animation.render(frameCounter, gfx);
            getDemo().setStatus(animation.getCaption(frameCounter));
            if (history != null) history.highlightEvent(frameCounter);
        } catch (IndexOutOfBoundsException ex) {
        } catch (IllegalArgumentException ex) {
        }
    }

    public CGAnimation getAnimation() {
        return animation;
    }

    public CGDemoModule getDemo() {
        return demo;
    }

    public String getString(String key) {
        return getDemo().getString(key);
    }
}
