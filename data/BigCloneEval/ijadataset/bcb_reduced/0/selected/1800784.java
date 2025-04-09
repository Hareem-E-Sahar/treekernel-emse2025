package org.pushingpixels.lafwidget.preview;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JViewport;
import org.pushingpixels.lafwidget.LafWidgetUtilities;

/**
 * Default implementation of the component preview painter. The component
 * preview is a scaled-down (as necessary) thumbnail of the relevant component.
 * 
 * @author Kirill Grouchnikov
 */
public class DefaultPreviewPainter extends PreviewPainter {

    public boolean hasPreview(Container parent, Component component, int componentIndex) {
        return (component != null);
    }

    public void previewComponent(Container parent, Component component, int componentIndex, Graphics g, int x, int y, int w, int h) {
        if (component == null) return;
        int compWidth = component.getWidth();
        int compHeight = component.getHeight();
        if ((compWidth > 0) && (compHeight > 0)) {
            BufferedImage tempCanvas = new BufferedImage(compWidth, compHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics tempCanvasGraphics = tempCanvas.getGraphics();
            component.paint(tempCanvasGraphics);
            double coef = Math.min((double) w / (double) compWidth, (double) h / (double) compHeight);
            if (coef < 1.0) {
                int sdWidth = (int) (coef * compWidth);
                int sdHeight = (int) (coef * compHeight);
                int dx = x + (w - sdWidth) / 2;
                int dy = y + (h - sdHeight) / 2;
                g.drawImage(LafWidgetUtilities.createThumbnail(tempCanvas, sdWidth), dx, dy, null);
            } else {
                g.drawImage(tempCanvas, x, y, null);
            }
        }
    }

    public boolean hasPreviewWindow(Container parent, Component component, int componentIndex) {
        return true;
    }

    public Dimension getPreviewWindowDimension(Container parent, Component component, int componentIndex) {
        Dimension superResult = super.getPreviewWindowDimension(parent, component, componentIndex);
        if (parent instanceof JViewport) {
            Rectangle viewportRect = ((JViewport) parent).getViewRect();
            int width = Math.min(viewportRect.width / 3, superResult.width);
            int height = Math.min(viewportRect.height / 3, superResult.height);
            return new Dimension(width, height);
        }
        return superResult;
    }
}
