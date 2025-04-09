package net.tileeditor.general;

import net.tileeditor.SimpleObject;
import net.tileeditor.source.ObjectWrapperFactory;
import net.tileeditor.source.SimpleObjectWrapper;
import net.engine.graphics.Sprite;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public abstract class SimpleObjectViewPanel extends BaseEditorPanel {

    protected SimpleObject hoveringObject;

    protected ArrayList<SimpleObject> simpleObjects;

    protected SimpleObjectWrapper simpleObjectWrapper;

    protected int celWidth;

    protected int celHeight;

    public SimpleObjectViewPanel() {
        super(false);
        this.simpleObjectWrapper = null;
        simpleObjects = new ArrayList<SimpleObject>();
        hoveringObject = null;
    }

    public void setSimpleObjectWrapper(SimpleObjectWrapper simpleObjectWrapper) {
        this.simpleObjectWrapper = simpleObjectWrapper;
    }

    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        pickHoveringObject(e);
    }

    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        pickHoveringObject(e);
    }

    protected void pickHoveringObject(MouseEvent e) {
        hoveringObject = pick(e.getX(), e.getY());
    }

    public void setCelWidthAndHeight(int celWidth, int celHeight) {
        this.celWidth = celWidth;
        this.celHeight = celHeight;
    }

    public void preRender() {
        super.preRender();
        renderObjects();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public abstract Color getBorderColour(int i, boolean hovering);

    public void renderObjects() {
        clearActiveObjects(true);
        CountAndDimension countAndDimension = calculateGrid();
        int x = 0;
        int y = 0;
        for (int i = 0; i < simpleObjects.size(); i++) {
            SimpleObject simpleObject = simpleObjects.get(i);
            int xPos = (int) ((x * (celWidth + 10)) - ((countAndDimension.width) / 2) - 5);
            int yPos = (int) ((y * (celHeight + 10)) - ((countAndDimension.height) / 2) - 5);
            simpleObjectWrapper.render(this, simpleObject.value, xPos, yPos, celWidth, celHeight, 0);
            Color colour = getBorderColour(i, hoveringObject == simpleObject);
            renderBorder(xPos, yPos, colour);
            x++;
            if (x >= countAndDimension.xCount) {
                x = 0;
                y++;
            }
        }
    }

    public CountAndDimension calculateGrid() {
        int xCount;
        int yCount;
        if (simpleObjects.size() == 1) {
            xCount = 1;
            yCount = 1;
        } else {
            double area = celWidth * celHeight * simpleObjects.size();
            double sqrtArea = (int) Math.sqrt(area);
            xCount = (int) Math.ceil(sqrtArea / (double) celWidth);
            yCount = (int) Math.ceil((double) simpleObjects.size() / (double) xCount);
        }
        int width;
        if (xCount == 1) {
            width = 0;
        } else {
            width = (xCount - 2) * (celWidth + 10) + celWidth;
        }
        int height;
        if (yCount == 1) {
            height = 0;
        } else {
            height = (yCount - 2) * (celHeight + 10) + celHeight;
        }
        return new CountAndDimension(xCount, yCount, width, height);
    }

    protected Rectangle calculateRect(int xPos, int yPos) {
        int halfWidth = (celWidth + 1) / 2;
        int halfHeight = (celHeight + 1) / 2;
        int x = (int) (xPos - camera.getPosition().x);
        int y = (int) (yPos - camera.getPosition().y);
        return new Rectangle(x - halfWidth - 1, y - halfHeight - 1, celWidth + 1, celHeight + 1);
    }

    protected void renderBorder(int xPos, int yPos, Color color) {
        Rectangle rectangle = calculateRect(xPos, yPos);
        backBuffer.setColor(color);
        backBuffer.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    protected void renderBorder(Sprite sprite) {
        Point topLeft = getTopLeftScreenPosition(sprite);
        Point bottomRight = getBottomRightScreenPosition(sprite);
        int x1 = topLeft.x - 1;
        int y1 = topLeft.y - 1;
        backBuffer.drawRect(x1, y1, bottomRight.x - x1, bottomRight.y - y1);
    }

    public SimpleObject pick(int x, int y) {
        CountAndDimension countAndDimension = calculateGrid();
        int xIndex = 0;
        int yIndex = 0;
        for (int i = 0; i < simpleObjects.size(); i++) {
            SimpleObject object = simpleObjects.get(i);
            int xPos = (int) ((xIndex * (celWidth + 10)) - (countAndDimension.width / 2) - 5);
            int yPos = (int) ((yIndex * (celHeight + 10)) - (countAndDimension.height / 2) - 5);
            Rectangle rectangle = calculateRect(xPos, yPos);
            if (((x > rectangle.x) && (x < rectangle.x + rectangle.width)) && ((y > rectangle.y) && (y < rectangle.y + rectangle.height))) {
                return object;
            }
            xIndex++;
            if (xIndex >= countAndDimension.xCount) {
                xIndex = 0;
                yIndex++;
            }
        }
        return null;
    }

    public void defaultValues(Class tileClass) {
        SimpleObjectWrapper wrapper = ObjectWrapperFactory.getInstance().getObjectWrapper(tileClass);
        setSimpleObjectWrapper(wrapper);
    }

    public void clearSimpleObjects() {
        simpleObjects = new ArrayList<SimpleObject>();
    }

    public void addSimpleObject(SimpleObject object) {
        simpleObjects.add(object);
    }

    public ArrayList<SimpleObject> getSimpleObjects() {
        return simpleObjects;
    }
}
