package com.gampire.pc.view.listener;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Timer;

public abstract class PCIIMouseDragAdapter extends MouseAdapter {

    protected static final int SPEED_DECREASE_PERCENTAGE_PER_SECOND = 95;

    protected static final int TIMER_DELAY_IN_MILLISECONDS = 100;

    protected static final double NEGLIGIBLE_SPEED = 30.0;

    protected final JComponent component;

    protected final Cursor originalCursor;

    private final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    protected final Timer scrollTimer;

    protected long timeAtLastActionPerfomedInMilliseconds = 0;

    private boolean mouseDragged = false;

    private int yMousePressed = 0;

    private int yMouseDragged = 0;

    private int yPreviousMouseDragged = 0;

    private int yPreviousScroll = 0;

    private long timeAtMouseDragged = 0;

    private long timeAtPreviousMouseDragged = 0;

    private double lastMeasuredDragSpeed = 0.0;

    protected double currentScrollSpeed = 0.0;

    public PCIIMouseDragAdapter(JComponent comp) {
        this.component = comp;
        this.originalCursor = comp.getCursor();
        scrollTimer = new javax.swing.Timer(TIMER_DELAY_IN_MILLISECONDS, new ActionListener() {

            protected double moveDistance = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTimeInMilliseconds = System.currentTimeMillis();
                long elapsedTimeInMilliseconds = currentTimeInMilliseconds - timeAtLastActionPerfomedInMilliseconds;
                timeAtLastActionPerfomedInMilliseconds = currentTimeInMilliseconds;
                moveDistance += (currentScrollSpeed * elapsedTimeInMilliseconds / 1000.0);
                int height = (int) moveDistance;
                if (height != 0) {
                    moveDistance -= height;
                    scrollUp(height);
                }
                double speedDecreasePercentage = SPEED_DECREASE_PERCENTAGE_PER_SECOND * elapsedTimeInMilliseconds / 1000.0;
                currentScrollSpeed = currentScrollSpeed * (100.0 - speedDecreasePercentage) * 1.e-2;
                if (Math.abs(currentScrollSpeed) < NEGLIGIBLE_SPEED) {
                    currentScrollSpeed = 0.0;
                    scrollTimer.stop();
                }
            }
        });
    }

    abstract void mouseReleasedButNoDrag(MouseEvent e);

    protected void scrollUp(int height) {
        if (height == 0) return;
        JViewport viewport = (JViewport) component.getParent();
        Point viewPosition = viewport.getViewPosition();
        viewPosition.translate(0, -height);
        component.scrollRectToVisible(new Rectangle(viewPosition, viewport.getSize()));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        component.setCursor(handCursor);
        yMousePressed = e.getPoint().y;
        yPreviousScroll = yMousePressed;
        yMouseDragged = yMousePressed;
        yPreviousMouseDragged = yMousePressed;
        currentScrollSpeed = 0.0;
        scrollTimer.stop();
        mouseDragged = false;
        timeAtPreviousMouseDragged = System.currentTimeMillis();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        component.setCursor(originalCursor);
        scrollTimer.stop();
        yMouseDragged = e.getPoint().y;
        currentScrollSpeed = lastMeasuredDragSpeed;
        if (mouseDragged) {
            if (Math.abs(lastMeasuredDragSpeed) > 0.2) {
                timeAtLastActionPerfomedInMilliseconds = System.currentTimeMillis();
                scrollTimer.start();
            }
            mouseDragged = false;
        } else {
            mouseReleasedButNoDrag(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        component.setCursor(handCursor);
        scrollTimer.stop();
        yMouseDragged = e.getPoint().y;
        timeAtMouseDragged = System.currentTimeMillis();
        int meanStopY = (yMouseDragged + yPreviousMouseDragged) / 2;
        scrollUp(meanStopY - yPreviousScroll);
        if (timeAtMouseDragged != timeAtPreviousMouseDragged) {
            double timeIntervalInSeconds = (timeAtMouseDragged - timeAtPreviousMouseDragged) / 1000.0;
            double distanceScrolled = (double) (meanStopY - yPreviousScroll);
            lastMeasuredDragSpeed = distanceScrolled / timeIntervalInSeconds;
        }
        yPreviousMouseDragged = yMouseDragged;
        timeAtPreviousMouseDragged = timeAtMouseDragged;
        yPreviousScroll = meanStopY;
        mouseDragged = true;
    }
}
