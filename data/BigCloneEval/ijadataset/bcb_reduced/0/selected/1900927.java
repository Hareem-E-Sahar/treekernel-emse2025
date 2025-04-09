package jpatch.boundary.tools;

import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;
import java.awt.*;
import java.awt.event.*;
import javax.vecmath.*;

public final class RotoscopeTool extends JPatchTool {

    private static final int IDLE = 0;

    private static final int SLIDER = 1;

    private static final int MOVE = 2;

    private static final int SCALE_TOP_LEFT = 3;

    private static final int SCALE_TOP_RIGHT = 4;

    private static final int SCALE_BOTTOM_LEFT = 5;

    private static final int SCALE_BOTTOM_RIGHT = 6;

    private int iState = IDLE;

    private Rotoscope rotoscope;

    private int iWidth, iHeight, iLeftX, iRightX, iTopY, iBottomY, iCenterX, iCenterY;

    private int iMouseX;

    private int iMouseY;

    private float fFactor;

    RotoscopeTool() {
    }

    private void init(ViewDefinition viewDefinition) {
        rotoscope = MainFrame.getInstance().getModel().getRotoscope(viewDefinition.getView());
        if (rotoscope == null) return;
        float viewTranslateX = viewDefinition.getTranslateX();
        float viewTranslateY = viewDefinition.getTranslateY();
        float viewWidth = viewDefinition.getWidth();
        float viewHeight = viewDefinition.getHeight();
        float viewScale = viewDefinition.getScale() * viewWidth * 0.5f;
        float scale = rotoscope.getScale() * viewScale;
        float xPos = rotoscope.getXPosition() - rotoscope.getScale() * 0.5f * rotoscope.getPixelWidth();
        float yPos = rotoscope.getYPosition() + rotoscope.getScale() * 0.5f * rotoscope.getPixelHeight();
        iWidth = (int) (scale * rotoscope.getPixelWidth());
        iHeight = (int) (scale * rotoscope.getPixelHeight());
        iLeftX = (int) (viewWidth * 0.5f + (viewTranslateX + xPos) * viewScale);
        iRightX = (int) (iLeftX + iWidth);
        iTopY = (int) (viewHeight * 0.5f - (viewTranslateY + yPos) * viewScale);
        iBottomY = (int) (iTopY + iHeight);
        iCenterX = (iLeftX + iRightX) / 2;
        iCenterY = (iTopY + iBottomY) / 2;
    }

    public void paint(ViewDefinition viewDef) {
        Settings settings = Settings.getInstance();
        JPatchDrawable2 drawable = viewDef.getDrawable();
        init(viewDef);
        if (rotoscope == null) {
            drawable.setColor(settings.colors.selection);
            drawable.drawString("No rotoscope set!", (int) (viewDef.getWidth() / 2 - 50), (int) (viewDef.getHeight() / 2 - 20));
            return;
        }
        Color3f white = new Color3f(1, 1, 1);
        Color3f cSelection = settings.colors.selection;
        if (iState == MOVE) {
            drawable.setColor(white);
            drawable.drawRect(iLeftX, iTopY, iWidth, iHeight);
        } else {
            drawable.setColor(cSelection);
            drawable.drawRect(iLeftX, iTopY, iWidth, iHeight);
            if (iState == SCALE_TOP_LEFT) drawable.setColor(white); else drawable.setColor(cSelection);
            drawable.fillRect(iLeftX - 3, iTopY - 3, 7, 7);
            if (iState == SCALE_TOP_RIGHT) drawable.setColor(white); else drawable.setColor(cSelection);
            drawable.fillRect(iRightX - 3, iTopY - 3, 7, 7);
            if (iState == SCALE_BOTTOM_LEFT) drawable.setColor(white); else drawable.setColor(cSelection);
            drawable.fillRect(iLeftX - 3, iBottomY - 3, 7, 7);
            if (iState == SCALE_BOTTOM_RIGHT) drawable.setColor(white); else drawable.setColor(cSelection);
            drawable.fillRect(iRightX - 3, iBottomY - 3, 7, 7);
        }
        drawable.setColor(cSelection);
        drawable.drawString("Opacity:", iCenterX - 90, iBottomY + 17);
        drawable.drawRect(iCenterX - 40, iBottomY + 9, 127, 7);
        if (iState == SLIDER) {
            drawable.setColor(white);
        }
        drawable.fillRect(iCenterX - 40, iBottomY + 9, rotoscope.getOpacity() / 2, 7);
        drawable.drawLine(iCenterX - 41 + rotoscope.getOpacity() / 2, iBottomY + 5, iCenterX - 41 + rotoscope.getOpacity() / 2, iBottomY + 20);
        drawable.drawLine(iCenterX - 40 + rotoscope.getOpacity() / 2, iBottomY + 5, iCenterX - 40 + rotoscope.getOpacity() / 2, iBottomY + 20);
        drawable.drawLine(iCenterX - 39 + rotoscope.getOpacity() / 2, iBottomY + 5, iCenterX - 39 + rotoscope.getOpacity() / 2, iBottomY + 20);
    }

    public void mousePressed(MouseEvent mouseEvent) {
        ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
        init(viewDef);
        if (rotoscope == null) return;
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            iMouseX = mouseEvent.getX();
            iMouseY = mouseEvent.getY();
            float dx = iMouseX - iCenterX;
            float dy = iMouseY - iCenterY;
            float size = (float) Math.sqrt(dx * dx + dy * dy);
            fFactor = rotoscope.getScale() / size;
            int opacitySliderPos = iCenterX - 40 + rotoscope.getOpacity() / 2;
            if (iMouseY > iBottomY + 3 && iMouseY < iBottomY + 22 && iMouseX > opacitySliderPos - 4 && iMouseX < opacitySliderPos + 4) {
                iState = SLIDER;
                ((Component) mouseEvent.getSource()).addMouseMotionListener(this);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
            } else if (iMouseX > iLeftX - 5 && iMouseX < iLeftX + 5 && iMouseY > iTopY - 5 && iMouseY < iTopY + 5) {
                iState = SCALE_TOP_LEFT;
                ((Component) mouseEvent.getSource()).addMouseMotionListener(this);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
            } else if (iMouseX > iRightX - 5 && iMouseX < iRightX + 5 && iMouseY > iTopY - 5 && iMouseY < iTopY + 5) {
                iState = SCALE_TOP_RIGHT;
                ((Component) mouseEvent.getSource()).addMouseMotionListener(this);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
            } else if (iMouseX > iLeftX - 5 && iMouseX < iLeftX + 5 && iMouseY > iBottomY - 5 && iMouseY < iBottomY + 5) {
                iState = SCALE_BOTTOM_LEFT;
                ((Component) mouseEvent.getSource()).addMouseMotionListener(this);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
            } else if (iMouseX > iRightX - 5 && iMouseX < iRightX + 5 && iMouseY > iBottomY - 5 && iMouseY < iBottomY + 5) {
                iState = SCALE_BOTTOM_RIGHT;
                ((Component) mouseEvent.getSource()).addMouseMotionListener(this);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
            } else if (iMouseX >= iLeftX && iMouseX <= iRightX && iMouseY >= iTopY && iMouseY <= iBottomY) {
                iState = MOVE;
                ((Component) mouseEvent.getSource()).addMouseMotionListener(this);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
            }
        }
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            ((Component) mouseEvent.getSource()).removeMouseMotionListener(this);
            iState = IDLE;
            MainFrame.getInstance().getJPatchScreen().update_all();
        }
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
        init(viewDef);
        if (rotoscope == null) return;
        int deltaX = mouseEvent.getX() - iMouseX;
        int deltaY = mouseEvent.getY() - iMouseY;
        iMouseX = mouseEvent.getX();
        iMouseY = mouseEvent.getY();
        switch(iState) {
            case SLIDER:
                int opacity = (iMouseX - iCenterX + 40) * 2;
                if (opacity < 0) opacity = 0;
                if (opacity > 255) opacity = 255;
                rotoscope.setOpacity(opacity);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
                break;
            case MOVE:
                float scale = viewDef.getScale() * viewDef.getWidth() * 0.5f;
                float x = rotoscope.getXPosition() + 1f / scale * deltaX;
                float y = rotoscope.getYPosition() - 1f / scale * deltaY;
                rotoscope.setPosition(x, y);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
                break;
            case SCALE_TOP_LEFT:
            case SCALE_TOP_RIGHT:
            case SCALE_BOTTOM_LEFT:
            case SCALE_BOTTOM_RIGHT:
                float dx = iMouseX - iCenterX;
                float dy = iMouseY - iCenterY;
                float size = (float) Math.sqrt(dx * dx + dy * dy);
                rotoscope.setScale(size * fFactor);
                MainFrame.getInstance().getJPatchScreen().single_update((Component) mouseEvent.getSource());
                break;
        }
    }
}
