package org.wtc.eclipse.draw2d.tester;

import abbot.tester.swt.ScrollBarTester;
import abbot.tester.swt.ScrollableTester;
import abbot.tester.swt.WidgetLocator;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.WidgetReference;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.util.ScreenCapture;
import junit.framework.TestCase;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;
import org.wtc.eclipse.draw2d.Draw2DActivator;
import org.wtc.eclipse.platform.util.ThreadUtil;
import java.text.MessageFormat;

/**
 * Legacy automated test support for low level UI "events" to be issued to Draw 2D
 * figures.
 * 
 * @since 3.8.0
 */
public class FigureTester {

    /**
     * Move the mouse to the figure with given parent canvas and click it once in the
     * center with mouse button 1.
     *
     * @since 3.8.0
     * @param  ui      - Driver for UI generated input
     * @param  parent  - The owner of the target label
     * @param  target  - The label figure to click
     */
    public void clickFigure(IUIContext ui, FigureCanvas parent, IFigure target) {
        clickFigure(ui, parent, target, 1);
    }

    /**
     * Move the mouse to the figure with given parent canvas and click it in the center
     * with mouse button 1.
     *
     * @since 3.8.0
     * @param  ui       - Driver for UI generated input
     * @param  locator  - With Window Tester 2.0, the widget locator is also the manager
     *                  of how a widget will be selected (for example, the ButtonLocator
     *                  will handle the button.click() and verify the widget has been
     *                  properly received the click event). The locator also handles the
     *                  waitForIdle() logic to make sure the UI is free to return from
     *                  this UI operation. For some UI operations (like launching Swing
     *                  dialogs), the wait for idle must be a loose interpretation (rather
     *                  then the classic wait for a syncExec ping to return). Use this
     *                  parameter to override the selection and/or waitForIdle logic or
     *                  provide <code>null</code> if the default selection & waitForIdle
     *                  logic is to be used.
     * @param  parent   - The owner of the target label
     * @param  target   - The label figure to click
     */
    public void clickFigure(IUIContext ui, FigureCanvas parent, IWidgetLocator locator, IFigure target) {
        clickFigure(ui, parent, locator, target, 1);
    }

    /**
     * Move the mouse to the figure with given parent canvas and click it once in the
     * center with mouse button 1.
     *
     * @since 3.8.0
     * @param  ui      - Driver for UI generated input
     * @param  parent  - The owner of the target label
     * @param  target  - The label figure to click
     * @param  count   - How many clicks?
     */
    private void clickFigure(IUIContext ui, FigureCanvas parent, IFigure target, int count) {
        clickFigure(ui, parent, null, target, count);
    }

    /**
     * Move the mouse to the figure with given parent canvas and click it once in the
     * center with mouse button 1.
     *
     * @since 3.8.0
     * @param  ui       - Driver for UI generated input
     * @param  parent   - The owner of the target label
     * @param  locator  - With Window Tester 2.0, the widget locator is also the manager
     *                  of how a widget will be selected (for example, the ButtonLocator
     *                  will handle the button.click() and verify the widget has been
     *                  properly received the click event). The locator also handles the
     *                  waitForIdle() logic to make sure the UI is free to return from
     *                  this UI operation. For some UI operations (like launching Swing
     *                  dialogs), the wait for idle must be a loose interpretation (rather
     *                  then the classic wait for a syncExec ping to return). Use this
     *                  parameter to override the selection and/or waitForIdle logic or
     *                  provide <code>null</code> if the default selection & waitForIdle
     *                  logic is to be used.
     * @param  target   - The label figure to click
     * @param  count    - How many clicks?
     */
    private void clickFigure(IUIContext ui, FigureCanvas parent, IWidgetLocator locator, IFigure target, int count) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(parent);
        TestCase.assertNotNull(target);
        Rectangle figureBounds = toFigureCanvas(target);
        org.eclipse.draw2d.geometry.Rectangle bounds = new org.eclipse.draw2d.geometry.Rectangle(figureBounds.x, figureBounds.y, figureBounds.width, figureBounds.height);
        clickFigureAtPoint(ui, parent, locator, bounds, count);
    }

    /**
     * move the mouse to the given click point and click it once using mouse button 1.
     *
     * @since 3.8.0
     * @param  ui          - Driver for UI generated input
     * @param  parent      - The owner of the target Point
     * @param  clickPoint  - The location relative to the parent's bounds to click
     * @param  count       - The number of times to click
     */
    private void clickFigureAtPoint(IUIContext ui, FigureCanvas parent, org.eclipse.draw2d.geometry.Rectangle bounds, int count) {
        clickFigureAtPoint(ui, parent, null, bounds, count);
    }

    /**
     * move the mouse to the given click point and click it once using mouse button 1.
     *
     * @since 3.8.0
     * @param  ui          - Driver for UI generated input
     * @param  parent      - The owner of the target Point
     * @param  locator     - With Window Tester 2.0, the widget locator is also the
     *                     manager of how a widget will be selected (for example, the
     *                     ButtonLocator will handle the button.click() and verify the
     *                     widget has been properly received the click event). The locator
     *                     also handles the waitForIdle() logic to make sure the UI is
     *                     free to return from this UI operation. For some UI operations
     *                     (like launching Swing dialogs), the wait for idle must be a
     *                     loose interpretation (rather then the classic wait for a
     *                     syncExec ping to return). Use this parameter to override the
     *                     selection and/or waitForIdle logic or provide <code>null</code>
     *                     if the default selection & waitForIdle logic is to be used.
     * @param  clickPoint  - The location relative to the parent's bounds to click
     * @param  count       - The number of times to click
     */
    private void clickFigureAtPoint(IUIContext ui, FigureCanvas parent, IWidgetLocator locator, org.eclipse.draw2d.geometry.Rectangle bounds, int count) {
        TestCase.assertTrue((count > 0) && (count < 3));
        Point p = scrollForClick(parent, bounds);
        IWidgetLocator canvasLocator = null;
        if (locator != null) {
            try {
                IWidgetLocator widgetRef = ui.find(locator);
                TestCase.assertTrue(widgetRef instanceof WidgetReference);
                Object obj = ((WidgetReference) widgetRef).getWidget();
                TestCase.assertNotNull(obj);
                TestCase.assertTrue("EXPECTED THE GIVEN LOCATOR TO FIND A FigureCanvas BUT IT FOUND A " + obj.getClass().getName(), obj instanceof FigureCanvas);
                canvasLocator = locator;
            } catch (WidgetSearchException e) {
                Draw2DActivator.logException(e);
                TestCase.fail(e.getLocalizedMessage());
            }
        } else {
            canvasLocator = new WidgetReference(parent);
        }
        try {
            ui.click(count, new XYLocator(canvasLocator, p.x, p.y));
        } catch (WidgetSearchException ex) {
            Draw2DActivator.logException(ex);
            TestCase.fail(ex.getLocalizedMessage());
        }
    }

    /**
     * Move the mouse to the label-typed figure with given parent canvas and click it once
     * in the middle of the text with mouse button 1.
     *
     * @since 3.8.0
     * @param  ui      - Driver for UI generated input
     * @param  parent  - The owner of the target label
     * @param  target  - The label figure to click
     */
    public void clickLabel(IUIContext ui, FigureCanvas parent, Label target) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(parent);
        TestCase.assertNotNull(target);
        org.eclipse.draw2d.geometry.Rectangle bounds = target.getTextBounds();
        clickFigureAtPoint(ui, parent, bounds, 1);
    }

    /**
     * Move the mouse to the polyline figure with given parent canvas and click it once
     * with mouse button 1.
     *
     * @since 3.8.0
     * @param  ui      - Driver for UI generated input
     * @param  parent  - The owner of the polyline
     * @param  target  - The polyline figure to click
     */
    public void clickPolyline(IUIContext ui, FigureCanvas parent, Polyline target) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(parent);
        TestCase.assertNotNull(target);
        org.eclipse.draw2d.geometry.Point start = target.getStart();
        org.eclipse.draw2d.geometry.Rectangle bounds = new org.eclipse.draw2d.geometry.Rectangle(start.x, start.y, 1, 1);
        clickFigureAtPoint(ui, parent, bounds, 1);
    }

    /**
     * Move the mouse to the figure with given parent canvas and click it once with mouse
     * button 3.
     *
     * @since 3.8.0
     * @param  ui        - Driver for UI generated input
     * @param  parent    - The owner of the target label
     * @param  target    - The label figure to click
     * @param  menuText  - Context menu text to execute
     */
    public void contextClickFigure(IUIContext ui, FigureCanvas parent, IFigure target, String menuText) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(parent);
        TestCase.assertNotNull(target);
        TestCase.assertNotNull(menuText);
        org.eclipse.draw2d.geometry.Rectangle bounds = target.getBounds();
        org.eclipse.draw2d.geometry.Point center = bounds.getCenter();
        contextClickPoint(ui, parent, center, menuText);
    }

    /**
     * Move the mouse to the label-typed figure with given parent canvas and click it once
     * with mouse button 3.
     *
     * @since 3.8.0
     * @param  ui        - Driver for UI generated input
     * @param  parent    - The owner of the target label
     * @param  target    - The label figure to click
     * @param  menuText  - Context menu text to execute
     */
    public void contextClickLabel(IUIContext ui, FigureCanvas parent, Label target, String menuText) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(parent);
        TestCase.assertNotNull(target);
        TestCase.assertNotNull(menuText);
        org.eclipse.draw2d.geometry.Rectangle bounds = target.getTextBounds();
        org.eclipse.draw2d.geometry.Point center = bounds.getCenter();
        contextClickPoint(ui, parent, center, menuText);
    }

    /**
     * performs a context menu click at the given 'clickPoint' and invokes the context
     * menu item that matches the given 'menuText'
     *
     * @since 3.8.0
     * @param  ui          - Driver for UI generated input
     * @param  parent      - The figure canvas on which the context click will occur
     * @param  clickPoint  - The click point on the figure canvas (does not consider the
     *                     viewport view location)
     * @param  menuText    - The menu item to invoke
     */
    private void contextClickPoint(IUIContext ui, FigureCanvas parent, org.eclipse.draw2d.geometry.Point clickPoint, String menuText) {
        Point p = scrollForClick(parent, clickPoint);
        try {
            ui.contextClick(new XYLocator(new WidgetReference(parent), p.x, p.y), menuText);
        } catch (WidgetSearchException wse) {
            Draw2DActivator.logException(wse);
            TestCase.fail(wse.getLocalizedMessage());
        }
    }

    /**
     * Move the mouse to the polyline figure associated with the given parent canvas and
     * click it once with mouse button 3.
     *
     * @since 3.8.0
     * @param  ui        - Driver for UI generated input
     * @param  parent    - The owner of the target polyline
     * @param  target    - The polyline to click
     * @param  menuText  - the context menu item to invoke
     */
    public void contextClickPolyline(IUIContext ui, FigureCanvas parent, Polyline target, String menuText) {
        TestCase.assertNotNull(ui);
        TestCase.assertNotNull(parent);
        TestCase.assertNotNull(target);
        TestCase.assertNotNull(menuText);
        contextClickPoint(ui, parent, target.getStart(), menuText);
    }

    /**
     * Move the mouse to the figure with given parent canvas and click it twice in the
     * center with mouse button 1.
     *
     * @since 3.8.0
     * @param  ui      - Driver for UI generated input
     * @param  parent  - The owner of the target label
     * @param  target  - The label figure to click
     */
    public void doubleClickFigure(IUIContext ui, FigureCanvas parent, IFigure target) {
        clickFigure(ui, parent, target, 2);
    }

    /**
     * Move the mouse to the figure with given parent canvas and click it twice in the
     * center with mouse button 1.
     *
     * @since 3.8.0
     * @param  ui       - Driver for UI generated input
     * @param  locator  - With Window Tester 2.0, the widget locator is also the manager
     *                  of how a widget will be selected (for example, the ButtonLocator
     *                  will handle the button.click() and verify the widget has been
     *                  properly received the click event). The locator also handles the
     *                  waitForIdle() logic to make sure the UI is free to return from
     *                  this UI operation. For some UI operations (like launching Swing
     *                  dialogs), the wait for idle must be a loose interpretation (rather
     *                  then the classic wait for a syncExec ping to return). Use this
     *                  parameter to override the selection and/or waitForIdle logic or
     *                  provide <code>null</code> if the default selection & waitForIdle
     *                  logic is to be used.
     * @param  parent   - The owner of the target label
     * @param  target   - The label figure to click
     */
    public void doubleClickFigure(IUIContext ui, FigureCanvas parent, IWidgetLocator locator, IFigure target) {
        clickFigure(ui, parent, locator, target, 2);
    }

    /**
     * Find the screen-relative location of the given figure owned by the given figure
     * canvas.
     *
     * @param   parent  - The owner of the given figure
     * @param   figure  - The figure to find the screen-relative bounds
     * @return  Rectangle - Screen relative bounds of
     */
    public Rectangle getBounds(final FigureCanvas parent, IFigure figure) {
        TestCase.assertNotNull(parent);
        TestCase.assertNotNull(figure);
        Rectangle figureBounds = toFigureCanvas(figure);
        int scrolledOffsetY = getScrolledOffsetY(parent);
        int scrolledOffsetX = getScrolledOffsetX(parent);
        Rectangle adjustedFigureBounds = new Rectangle(figureBounds.x + scrolledOffsetX, figureBounds.y - scrolledOffsetY, figureBounds.width, figureBounds.height);
        final Rectangle[] canvasBounds = new Rectangle[1];
        Runnable r = new Runnable() {

            public void run() {
                canvasBounds[0] = WidgetLocator.getBounds(parent, false);
            }
        };
        ThreadUtil.ensureRunOnUIThread(r);
        Rectangle resultingBounds = new Rectangle(canvasBounds[0].x + adjustedFigureBounds.x, canvasBounds[0].y + adjustedFigureBounds.y, adjustedFigureBounds.width, adjustedFigureBounds.height);
        return resultingBounds;
    }

    /**
     * Get the number of pixels that the given figure canvas has been scrolled
     * horizontally.
     */
    public int getScrolledOffsetX(FigureCanvas canvas) {
        TestCase.assertNotNull(canvas);
        ScrollableTester scrollableTester = new ScrollableTester();
        ScrollBar scrollBar = scrollableTester.getHorizontalBar(canvas);
        ScrollBarTester scrollBarTester = new ScrollBarTester();
        return scrollBarTester.getSelection(scrollBar);
    }

    /**
     * Get the number of pixels that the given figure canvas has been scrolled vertically.
     */
    public int getScrolledOffsetY(FigureCanvas canvas) {
        TestCase.assertNotNull(canvas);
        ScrollableTester scrollableTester = new ScrollableTester();
        scrollableTester.getClientArea(canvas);
        ScrollBar scrollBar = scrollableTester.getVerticalBar(canvas);
        ScrollBarTester scrollBarTester = new ScrollBarTester();
        return scrollBarTester.getSelection(scrollBar);
    }

    /**
     * scrollToVisible - Scroll the viewport to place the given point center of the
     * viewport (or as far as the viewport will allow if the given point can't be scrolled
     * to the center).
     *
     * @return  Point - The point to click on the parent figure canvas *after* the
     *          viewport has been scrolled. The provided click point should not be used
     *          after calling this method
     */
    public Point scrollForClick(FigureCanvas parent, org.eclipse.draw2d.geometry.Point clickPoint) {
        org.eclipse.draw2d.geometry.Rectangle bounds = new org.eclipse.draw2d.geometry.Rectangle(clickPoint.x, clickPoint.y, 1, 1);
        return scrollForClick(parent, bounds);
    }

    /**
     * scrollForClick - Scroll the viewport to place the given point center of the
     * viewport (or as far as the viewport will allow if the given point can't be scrolled
     * to the center).
     *
     * @return  Point - The point to click on the parent figure canvas *after* the
     *          viewport has been scrolled. The provided click point should not be used
     *          after calling this method
     */
    public Point scrollForClick(FigureCanvas parent, IFigure figure) {
        TestCase.assertNotNull(parent);
        TestCase.assertNotNull(figure);
        Rectangle r1 = toFigureCanvas(figure);
        org.eclipse.draw2d.geometry.Rectangle r2 = new org.eclipse.draw2d.geometry.Rectangle(r1.x, r1.y, r1.width, r1.height);
        return scrollForClick(parent, r2);
    }

    /**
     * scrollForClick - Scroll the viewport to place the given point center of the
     * viewport (or as far as the viewport will allow if the given point can't be scrolled
     * to the center).
     *
     * @return  Point - The point to click on the parent figure canvas *after* the
     *          viewport has been scrolled. The provided click point should not be used
     *          after calling this method
     */
    public Point scrollForClick(final FigureCanvas parent, final org.eclipse.draw2d.geometry.Rectangle bounds) {
        final org.eclipse.draw2d.geometry.Point clickPoint = bounds.getCenter();
        final Point[] p = new Point[1];
        Runnable runner = new Runnable() {

            public void run() {
                int boundsCenterX = bounds.x + (bounds.width / 2);
                int boundsCenterY = bounds.y + (bounds.height / 2);
                Viewport viewport = parent.getViewport();
                Dimension viewportSize = viewport.getSize().getCopy();
                org.eclipse.draw2d.geometry.Point viewportLoc = viewport.getViewLocation().getCopy();
                int viewPortPoint1X = viewportLoc.x;
                int viewPortPoint1Y = viewportLoc.y;
                int viewPortPoint2X = viewportLoc.x + viewportSize.width;
                int viewPortPoint3Y = viewportLoc.y + viewportSize.height;
                int adjustedPortPoint1Y = viewPortPoint1Y + 25;
                int adjustedPortPoint3Y = viewPortPoint3Y - 25;
                if ((boundsCenterX < viewPortPoint1X) || (boundsCenterX > viewPortPoint2X) || (boundsCenterY < adjustedPortPoint1Y) || (boundsCenterY > adjustedPortPoint3Y)) {
                    Dimension contentsSize = viewport.getContents().getSize().getCopy();
                    int dx = 0;
                    int diffX = boundsCenterX - (viewPortPoint1X + (viewportSize.width / 2));
                    if (boundsCenterX < viewPortPoint1X) {
                        dx = -1 * Math.min(viewPortPoint1X, Math.abs(diffX));
                    } else if (boundsCenterX > viewPortPoint2X) {
                        dx = Math.min(contentsSize.width - viewPortPoint2X, Math.abs(diffX));
                    }
                    int dy = 0;
                    int diffY = boundsCenterY - (adjustedPortPoint1Y + (viewportSize.height / 2));
                    if (boundsCenterY < adjustedPortPoint1Y) {
                        dy = -1 * Math.min(viewPortPoint1Y, Math.abs(diffY));
                    } else if (boundsCenterY > adjustedPortPoint3Y) {
                        dy = Math.min(contentsSize.height - viewPortPoint3Y, Math.abs(diffY));
                    }
                    if ((dx == 0) && (dy == 0) && ((contentsSize.height - viewPortPoint3Y) != 0)) {
                        ScreenCapture.createScreenCapture("FigureTester.scrollForClick");
                        String message = MessageFormat.format("Click point ({0}, {1}) was not in viewport, should have to scroll but deltas were ({2}, {3})", new Object[] { boundsCenterX, boundsCenterY, dx, dy });
                        TestCase.assertTrue(message, (dx != 0) || (dy != 0));
                    }
                    viewportLoc.translate(dx, dy);
                    viewport.setViewLocation(viewportLoc);
                    TestCase.assertEquals("viewport was not scrolled the expected amount", viewportLoc, viewport.getViewLocation());
                }
                p[0] = new Point(clickPoint.x - viewportLoc.x, clickPoint.y - viewportLoc.y);
            }
        };
        ThreadUtil.ensureRunOnUIThread(runner);
        TestCase.assertNotNull(p[0]);
        return p[0];
    }

    /**
     * Calculate the canvas-relative bounds of the given figure.
     *
     * @param   figure  - Figure to find the bounds of
     * @return  Rectangle - The bounds of the given figure relative to the figure canvas
     *          that owns it
     */
    public Rectangle toFigureCanvas(IFigure figure) {
        TestCase.assertNotNull(figure);
        org.eclipse.draw2d.geometry.Rectangle draw2dBounds = figure.getBounds();
        Rectangle rectangleBounds = new Rectangle(draw2dBounds.x, draw2dBounds.y, draw2dBounds.width, draw2dBounds.height);
        IFigure parent = figure.getParent();
        if ((draw2dBounds.x == 0) && (draw2dBounds.y == 0) && (parent != null)) {
            Rectangle parentBounds = toFigureCanvas(parent);
            rectangleBounds.x = rectangleBounds.x + parentBounds.x;
            rectangleBounds.y = rectangleBounds.y + parentBounds.y;
        }
        return rectangleBounds;
    }

    /**
     * Calculate the canvas-relative bounds of the given label.
     *
     * @param   label  - Label to find the text bounds of
     * @return  Rectangle - The bounds of the given label relative to the figure canvas
     *          that owns it
     */
    public Rectangle toFigureCanvas(Label label) {
        TestCase.assertNotNull(label);
        org.eclipse.draw2d.geometry.Rectangle draw2dBounds = label.getTextBounds();
        Rectangle rectangleBounds = new Rectangle(draw2dBounds.x, draw2dBounds.y, draw2dBounds.width, draw2dBounds.height);
        return rectangleBounds;
    }
}
