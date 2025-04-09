package org.stellarium.ui.components;

import org.stellarium.ui.SglAccess;
import org.stellarium.vecmath.Vector2i;
import java.awt.event.MouseEvent;

/**
 * cursor Bar
 */
public class CursorBar extends StellariumComponent {

    public CursorBar(double _min, double _max, double _val) {
        minBar = (float) _min;
        maxBar = (float) _max;
        double tmpVal = _val;
        if (tmpVal < minBar && tmpVal > maxBar) tmpVal = (minBar + maxBar) / 2;
        setSize(100, 15);
        cursor.setSize(8, getSizeY());
        setValue(tmpVal);
        oldPos = cursor.getPos();
    }

    public void draw() {
        if (!visible) return;
        painter.drawSquareEdge(pos, size);
        SglAccess.glPushMatrix();
        SglAccess.glTranslatef(pos.i0, pos.i1, 0.f);
        scissor.push(pos, size);
        cursor.draw();
        scissor.pop();
        SglAccess.glPopMatrix();
        if (dragging) {
            String temp = "" + barVal;
            painter.print(pos.i0 + 2, pos.i1 + 2, temp);
        }
    }

    void setValue(double _barVal) {
        barVal = (float) _barVal;
        if (barVal < minBar) barVal = minBar;
        if (barVal > maxBar) barVal = maxBar;
        cursor.setPos((int) ((barVal - minBar) / (maxBar - minBar) * (size.i0 - cursor.getSizeX())), 0);
    }

    public void mouseReleased(MouseEvent e) {
        int bt = e.getButton();
        if (bt == MouseEvent.BUTTON1) {
            dragging = false;
        }
    }

    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (visible && isIn(x, y)) {
            if (cursor.isIn(x - pos.i0, y - pos.i1)) dragging = true;
            e.consume();
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (visible) {
            int x = e.getX() - pos.i0;
            int y = e.getY() - pos.i1;
            MouseEvent newEvent = new MouseEvent((java.awt.Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
            cursor.mouseMoved(newEvent);
            if (!dragging) return;
            if (x < 0) {
                x = 0;
            }
            if (x > size.i0 - cursor.getSizeX()) {
                x = size.i0 - cursor.getSizeX();
            }
            cursor.setPos(cursor.getPosx() + x - oldPos.i0, cursor.getPosy());
            if (cursor.getPosx() < 0) cursor.setPos(0, cursor.getPosy());
            if (cursor.getPosx() > size.i0 - cursor.getSizeX()) {
                cursor.setPos(size.i0 - cursor.getSizeX(), cursor.getPosy());
            }
            barVal = (float) cursor.getPosx() / (size.i0 - cursor.getSizeX()) * (maxBar - minBar);
            if (onChangeCallback != null) onChangeCallback.execute();
            oldPos.set(x, y);
        }
    }

    float getValue() {
        return barVal;
    }

    void setOnChangeCallback(StelCallback c) {
        onChangeCallback = c;
    }

    private boolean dragging;

    private Button cursor;

    private float minBar, maxBar, barVal;

    private StelCallback onChangeCallback;

    private Vector2i oldPos;
}
