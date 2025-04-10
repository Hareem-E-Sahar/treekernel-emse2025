package org.eclipse.gef.ui.rulers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.DefaultRangeModel;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Handle;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.internal.ui.rulers.GuideEditPart;
import org.eclipse.gef.internal.ui.rulers.RulerContextMenuProvider;
import org.eclipse.gef.internal.ui.rulers.RulerEditPart;
import org.eclipse.gef.internal.ui.rulers.RulerEditPartFactory;
import org.eclipse.gef.internal.ui.rulers.RulerRootEditPart;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;

/**
 * A RulerComposite is used to show rulers to the north and west of the control of a
 * given {@link #setGraphicalViewer(ScrollingGraphicalViewer) graphical viewer}.  The
 * rulers will be shown based on whether or not 
 * {@link org.eclipse.gef.rulers.RulerProvider#PROPERTY_HORIZONTAL_RULER horizontal ruler} 
 * and {@link org.eclipse.gef.rulers.RulerProvider#PROPERTY_VERTICAL_RULER vertical ruler}
 * properties are set on the given viewer, and the value of the 
 * {@link org.eclipse.gef.rulers.RulerProvider#PROPERTY_RULER_VISIBILITY visibility} 
 * property.
 * 
 * @author Pratik Shah
 * @since 3.0
 */
public class RulerComposite extends Composite {

    private EditDomain rulerEditDomain;

    private GraphicalViewer left, top;

    private FigureCanvas editor;

    private GraphicalViewer diagramViewer;

    private Font font;

    private Listener layoutListener;

    private PropertyChangeListener propertyListener;

    private boolean layingOut = false;

    private boolean isRulerVisible = true;

    private boolean needToLayout = false;

    private Runnable runnable = new Runnable() {

        public void run() {
            layout(false);
        }
    };

    /**
 * Constructor
 * 
 * @param parent	a widget which will be the parent of the new instance (cannot be null)
 * @param style		the style of widget to construct
 * @see	Composite#Composite(org.eclipse.swt.widgets.Composite, int)
 */
    public RulerComposite(Composite parent, int style) {
        super(parent, style);
        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                disposeResources();
            }
        });
    }

    private static Rectangle calculateEditorTrim(Canvas canvas) {
        Rectangle bounds = canvas.getBounds();
        Rectangle clientArea = canvas.getClientArea();
        Rectangle result = new Rectangle(0, 0, bounds.width - clientArea.width, bounds.height - clientArea.height);
        if (result.width != 0 || result.height != 0) {
            Rectangle trim = canvas.computeTrim(0, 0, 0, 0);
            result.x = result.height == 0 ? 0 : trim.x;
            result.y = result.width == 0 ? 0 : trim.y;
        }
        return result;
    }

    private static Rectangle calculateRulerTrim(Canvas canvas) {
        if ("carbon".equals(SWT.getPlatform())) {
            Rectangle trim = canvas.computeTrim(0, 0, 0, 0);
            trim.width = 0 - trim.x * 2;
            trim.height = 0 - trim.y * 2;
            return trim;
        }
        return new Rectangle(0, 0, 0, 0);
    }

    private GraphicalViewer createRulerContainer(int orientation) {
        ScrollingGraphicalViewer viewer = new RulerViewer();
        final boolean isHorizontal = orientation == PositionConstants.NORTH || orientation == PositionConstants.SOUTH;
        viewer.setRootEditPart(new RulerRootEditPart(isHorizontal));
        viewer.setEditPartFactory(new RulerEditPartFactory(diagramViewer));
        viewer.createControl(this);
        ((GraphicalEditPart) viewer.getRootEditPart()).getFigure().setBorder(new RulerBorder(isHorizontal));
        viewer.setProperty(GraphicalViewer.class.toString(), diagramViewer);
        FigureCanvas canvas = (FigureCanvas) viewer.getControl();
        canvas.setScrollBarVisibility(FigureCanvas.NEVER);
        if (font == null) {
            FontData[] data = canvas.getFont().getFontData();
            for (int i = 0; i < data.length; i++) {
                data[i].setHeight(data[i].getHeight() - 1);
            }
            font = new Font(Display.getCurrent(), data);
        }
        canvas.setFont(font);
        if (isHorizontal) {
            canvas.getViewport().setHorizontalRangeModel(editor.getViewport().getHorizontalRangeModel());
        } else {
            canvas.getViewport().setVerticalRangeModel(editor.getViewport().getVerticalRangeModel());
        }
        if (rulerEditDomain == null) {
            rulerEditDomain = new EditDomain();
            rulerEditDomain.setCommandStack(diagramViewer.getEditDomain().getCommandStack());
        }
        rulerEditDomain.addViewer(viewer);
        return viewer;
    }

    private void disposeResources() {
        if (diagramViewer != null) diagramViewer.removePropertyChangeListener(propertyListener);
        if (font != null) font.dispose();
    }

    private void disposeRulerViewer(GraphicalViewer viewer) {
        if (viewer == null) return;
        RangeModel rModel = new DefaultRangeModel();
        Viewport port = ((FigureCanvas) viewer.getControl()).getViewport();
        port.setHorizontalRangeModel(rModel);
        port.setVerticalRangeModel(rModel);
        rulerEditDomain.removeViewer(viewer);
        viewer.getControl().dispose();
    }

    private void doLayout() {
        if (left == null && top == null) {
            Rectangle area = getClientArea();
            if (!editor.getBounds().equals(area)) editor.setBounds(area);
            return;
        }
        int leftWidth = 0, topHeight = 0;
        Rectangle leftTrim = null, topTrim = null;
        if (left != null) {
            leftTrim = calculateRulerTrim((Canvas) left.getControl());
            leftWidth = left.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).x + leftTrim.width;
        }
        if (top != null) {
            topTrim = calculateRulerTrim((Canvas) top.getControl());
            topHeight = top.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y + topTrim.height;
        }
        Rectangle editorSize = getClientArea();
        editorSize.x = leftWidth;
        editorSize.y = topHeight;
        editorSize.width -= leftWidth;
        editorSize.height -= topHeight;
        editor.setBounds(editorSize);
        Rectangle trim = calculateEditorTrim(editor);
        if (left != null) {
            left.getControl().setBounds(0, topHeight - trim.x + leftTrim.x - 1, leftWidth, editorSize.height - trim.height + leftTrim.height + 1);
        }
        if (top != null) {
            top.getControl().setBounds(leftWidth - trim.y + topTrim.y - 1, 0, editorSize.width - trim.width + topTrim.width + 1, topHeight);
        }
    }

    private GraphicalViewer getRulerContainer(int orientation) {
        GraphicalViewer result = null;
        switch(orientation) {
            case PositionConstants.NORTH:
                result = top;
                break;
            case PositionConstants.WEST:
                result = left;
        }
        return result;
    }

    /**
 * @see org.eclipse.swt.widgets.Composite#layout(boolean)
 */
    public void layout(boolean change) {
        if (!layingOut && !isDisposed()) {
            checkWidget();
            if (change || needToLayout) {
                needToLayout = false;
                layingOut = true;
                doLayout();
                layingOut = false;
            }
        }
    }

    /**
 * Creates rulers for the given graphical viewer.
 * <p>
 * The primaryViewer or its Control cannot be <code>null</code>.  The primaryViewer's
 * Control should be a FigureCanvas and a child of this Composite.  This method should
 * only be invoked once.
 * <p>
 * To create ruler(s), simply add the RulerProvider(s) (with the right key: 
 * RulerProvider.PROPERTY_HORIZONTAL_RULER or RulerProvider.PROPERTY_VERTICAL_RULER) 
 * as a property on the given viewer.  It can be done after this method is invoked.
 * RulerProvider.PROPERTY_RULER_VISIBILITY can be used to show/hide the rulers.
 * 
 * @param	primaryViewer	The graphical viewer for which the rulers have to be created
 */
    public void setGraphicalViewer(ScrollingGraphicalViewer primaryViewer) {
        Assert.isNotNull(primaryViewer);
        Assert.isNotNull(primaryViewer.getControl());
        Assert.isTrue(diagramViewer == null);
        diagramViewer = primaryViewer;
        editor = (FigureCanvas) diagramViewer.getControl();
        layoutListener = new Listener() {

            public void handleEvent(Event event) {
                layout(true);
            }
        };
        addListener(SWT.Resize, layoutListener);
        editor.getHorizontalBar().addListener(SWT.Show, layoutListener);
        editor.getHorizontalBar().addListener(SWT.Hide, layoutListener);
        editor.getVerticalBar().addListener(SWT.Show, layoutListener);
        editor.getVerticalBar().addListener(SWT.Hide, layoutListener);
        propertyListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                String property = evt.getPropertyName();
                if (RulerProvider.PROPERTY_HORIZONTAL_RULER.equals(property)) {
                    setRuler((RulerProvider) diagramViewer.getProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER), PositionConstants.NORTH);
                } else if (RulerProvider.PROPERTY_VERTICAL_RULER.equals(property)) {
                    setRuler((RulerProvider) diagramViewer.getProperty(RulerProvider.PROPERTY_VERTICAL_RULER), PositionConstants.WEST);
                } else if (RulerProvider.PROPERTY_RULER_VISIBILITY.equals(property)) setRulerVisibility(((Boolean) diagramViewer.getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY)).booleanValue());
            }
        };
        diagramViewer.addPropertyChangeListener(propertyListener);
        Boolean rulerVisibility = (Boolean) diagramViewer.getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
        if (rulerVisibility != null) setRulerVisibility(rulerVisibility.booleanValue());
        setRuler((RulerProvider) diagramViewer.getProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER), PositionConstants.NORTH);
        setRuler((RulerProvider) diagramViewer.getProperty(RulerProvider.PROPERTY_VERTICAL_RULER), PositionConstants.WEST);
    }

    private void setRuler(RulerProvider provider, int orientation) {
        Object ruler = null;
        if (isRulerVisible && provider != null) ruler = provider.getRuler();
        if (ruler == null) {
            setRulerContainer(null, orientation);
            layout(true);
            return;
        }
        GraphicalViewer container = getRulerContainer(orientation);
        if (container == null) {
            container = createRulerContainer(orientation);
            setRulerContainer(container, orientation);
        }
        if (container.getContents() != ruler) {
            container.setContents(ruler);
            needToLayout = true;
            Display.getCurrent().asyncExec(runnable);
        }
    }

    private void setRulerContainer(GraphicalViewer container, int orientation) {
        if (orientation == PositionConstants.NORTH) {
            if (top == container) return;
            disposeRulerViewer(top);
            top = container;
        } else if (orientation == PositionConstants.WEST) {
            if (left == container) return;
            disposeRulerViewer(left);
            left = container;
        }
    }

    private void setRulerVisibility(boolean isVisible) {
        if (isRulerVisible != isVisible) {
            isRulerVisible = isVisible;
            if (diagramViewer != null) {
                setRuler((RulerProvider) diagramViewer.getProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER), PositionConstants.NORTH);
                setRuler((RulerProvider) diagramViewer.getProperty(RulerProvider.PROPERTY_VERTICAL_RULER), PositionConstants.WEST);
            }
        }
    }

    private static class RulerBorder extends AbstractBorder {

        private static final Insets H_INSETS = new Insets(0, 1, 0, 0);

        private static final Insets V_INSETS = new Insets(1, 0, 0, 0);

        private boolean horizontal;

        /**
	 * Constructor
	 * 
	 * @param isHorizontal	whether or not the ruler being bordered is horizontal or not
	 */
        public RulerBorder(boolean isHorizontal) {
            horizontal = isHorizontal;
        }

        /**
	 * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
	 */
        public Insets getInsets(IFigure figure) {
            return horizontal ? H_INSETS : V_INSETS;
        }

        /**
	 * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure, org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
	 */
        public void paint(IFigure figure, Graphics graphics, Insets insets) {
            graphics.setForegroundColor(ColorConstants.buttonDarker);
            if (horizontal) {
                graphics.drawLine(figure.getBounds().getTopLeft(), figure.getBounds().getBottomLeft().translate(new org.eclipse.draw2d.geometry.Point(0, -4)));
            } else {
                graphics.drawLine(figure.getBounds().getTopLeft(), figure.getBounds().getTopRight().translate(new org.eclipse.draw2d.geometry.Point(-4, 0)));
            }
        }
    }

    /**
 * Custom graphical viewer intended to be used for rulers.
 * 
 * @author Pratik Shah
 * @since 3.0
 */
    private static class RulerViewer extends ScrollingGraphicalViewer {

        /**
	 * Constructor
	 */
        public RulerViewer() {
            super();
            init();
        }

        /**
	 * @see org.eclipse.gef.EditPartViewer#appendSelection(org.eclipse.gef.EditPart)
	 */
        public void appendSelection(EditPart editpart) {
            if (editpart instanceof RootEditPart) editpart = ((RootEditPart) editpart).getContents();
            setFocus(editpart);
            super.appendSelection(editpart);
        }

        /**
	 * @see org.eclipse.gef.GraphicalViewer#findHandleAt(org.eclipse.draw2d.geometry.Point)
	 */
        public Handle findHandleAt(org.eclipse.draw2d.geometry.Point p) {
            final GraphicalEditPart gep = (GraphicalEditPart) findObjectAtExcluding(p, new ArrayList());
            if (gep == null || !(gep instanceof GuideEditPart)) return null;
            return new Handle() {

                public DragTracker getDragTracker() {
                    return ((GuideEditPart) gep).getDragTracker(null);
                }

                public org.eclipse.draw2d.geometry.Point getAccessibleLocation() {
                    return null;
                }
            };
        }

        /**
	 * @see org.eclipse.gef.ui.parts.AbstractEditPartViewer#init()
	 */
        protected void init() {
            setContextMenu(new RulerContextMenuProvider(this));
            setKeyHandler(new RulerKeyHandler(this));
        }

        /**
	 * Requests to reveal a ruler are ignored since that causes undesired scrolling to
	 * the origin of the ruler
	 * 
	 * @see org.eclipse.gef.EditPartViewer#reveal(org.eclipse.gef.EditPart)
	 */
        public void reveal(EditPart part) {
            if (part != getContents()) super.reveal(part);
        }

        /**
	 * @see org.eclipse.gef.EditPartViewer#setContents(org.eclipse.gef.EditPart)
	 */
        public void setContents(EditPart editpart) {
            super.setContents(editpart);
            setFocus(getContents());
        }

        /**
	 * Custom KeyHandler intended to be used with a RulerViewer
	 * 
	 * @author Pratik Shah
	 * @since 3.0
	 */
        protected static class RulerKeyHandler extends GraphicalViewerKeyHandler {

            /**
		 * Constructor
		 * 
		 * @param viewer	The viewer for which this handler processes keyboard input
		 */
            public RulerKeyHandler(GraphicalViewer viewer) {
                super(viewer);
            }

            /**
		 * @see org.eclipse.gef.KeyHandler#keyPressed(org.eclipse.swt.events.KeyEvent)
		 */
            public boolean keyPressed(KeyEvent event) {
                if (event.keyCode == SWT.DEL) {
                    if (getFocusEditPart() instanceof GuideEditPart) {
                        RulerEditPart parent = (RulerEditPart) getFocusEditPart().getParent();
                        getViewer().getEditDomain().getCommandStack().execute(parent.getRulerProvider().getDeleteGuideCommand(getFocusEditPart().getModel()));
                        event.doit = false;
                        return true;
                    }
                    return false;
                } else if (((event.stateMask & SWT.ALT) != 0) && (event.keyCode == SWT.ARROW_UP)) {
                    EditPart parent = getFocusEditPart().getParent();
                    if (parent instanceof RulerEditPart) navigateTo(getFocusEditPart().getParent(), event);
                    return true;
                }
                return super.keyPressed(event);
            }
        }
    }
}
