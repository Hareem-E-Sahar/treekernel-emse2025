package repast.simphony.visualization.visualization3D;

import repast.simphony.space.graph.Network;
import repast.simphony.space.projection.Projection;
import repast.simphony.ui.Imageable;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualization.*;
import repast.simphony.visualization.decorator.ProjectionDecorator3D;
import repast.simphony.visualization.visualization2D.IDisplayLayer2D;
import repast.simphony.visualization.visualization3D.style.EdgeStyle3D;
import repast.simphony.visualization.visualization3D.style.Style3D;
import repast.simphony.visualization.visualization3D.style.ValueLayerStyle3D;
import javax.media.j3d.Behavior;
import javax.media.j3d.Canvas3D;
import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for 3D displays.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2006/01/06 22:53:54 $
 */
public abstract class AbstractDisplay3D extends AbstractDisplay implements WindowListener {

    static class ImageablePanel extends JPanel implements Imageable {

        public ImageablePanel(LayoutManager layout, boolean isDoubleBuffered) {
            super(layout, isDoubleBuffered);
        }

        public ImageablePanel(LayoutManager layout) {
            super(layout);
        }

        public ImageablePanel(boolean isDoubleBuffered) {
            super(isDoubleBuffered);
        }

        public ImageablePanel() {
        }

        public BufferedImage getImage() {
            Component comp = getComponent(0);
            Rectangle rect = new Rectangle(comp.getLocationOnScreen(), comp.getSize());
            try {
                Robot robot = new Robot(this.getGraphicsConfiguration().getDevice());
                return robot.createScreenCapture(rect);
            } catch (AWTException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    static class DummyValueDisplayLayer implements ValueDisplayLayer3D {

        public void applyUpdates() {
        }

        public void setStyle(ValueLayerStyle3D style) {
        }

        public void update() {
        }

        public void addDataLayer(ValueLayer layer) {
        }

        public void init(Behavior masterBehavior) {
        }
    }

    protected DisplayData<?> initData;

    protected Map<Class, AbstractDisplayLayer3D> displayMap = new HashMap<Class, AbstractDisplayLayer3D>();

    protected Map<Network, AbstractDisplayLayer3D> networkMap = new HashMap<Network, AbstractDisplayLayer3D>();

    protected ValueDisplayLayer3D valueLayer;

    protected ValueLayerStyle3D valueLayerStyle;

    protected Layout layout;

    protected boolean iconified = false;

    protected JPanel panel;

    private boolean addWindowListener = true;

    private LayoutUpdater layoutUpdater;

    protected Map<String, ProjectionDecorator3D> decoratorMap;

    public AbstractDisplay3D(DisplayData<?> data, Layout layout) {
        this.initData = data;
        this.layout = layout;
        layoutUpdater = new UpdateLayoutUpdater(layout);
        decoratorMap = new HashMap<String, ProjectionDecorator3D>();
    }

    public void init() {
        if (initData.getProjectionCount() > 0) {
            for (Object obj : initData.objects()) {
                addObject(obj);
            }
            for (Projection proj : initData.getProjections()) {
                proj.addProjectionListener(this);
            }
        }
        for (ValueLayer layer : initData.getValueLayers()) {
            if (valueLayer == null) {
                valueLayer = createValueLayerDisplayLayer(null);
            }
            valueLayer.addDataLayer(layer);
        }
    }

    public void registerStyle(Class clazz, Style3D style) {
        AbstractDisplayLayer3D layer = displayMap.get(clazz);
        if (layer == null) {
            layer = createDisplayLayer(style);
            displayMap.put(clazz, layer);
        } else {
            layer.setStyle(style);
        }
    }

    public Iterable<Class> getRegisteredAgents() {
        return displayMap.keySet();
    }

    public void registerDecorator(ProjectionDecorator3D decorator) {
        decoratorMap.put(decorator.getProjection().getName(), decorator);
    }

    public void registerNetworkStyle(Network network, EdgeStyle3D style) {
        AbstractDisplayLayer3D layer = networkMap.get(network);
        if (layer == null) {
            layer = createEdgeLayer(style, network);
            networkMap.put(network, layer);
        } else {
            layer.setStyle(style);
        }
    }

    public void registerValueLayerStyle(ValueLayerStyle3D style) {
        if (valueLayer == null || valueLayer instanceof DummyValueDisplayLayer) {
            valueLayer = createValueLayerDisplayLayer(style);
        } else {
            valueLayer.setStyle(style);
        }
        valueLayerStyle = style;
    }

    public void createPanel() {
        panel = new ImageablePanel();
        panel.setMinimumSize(new Dimension(10, 10));
        panel.addHierarchyListener(new HierarchyListener() {

            public void hierarchyChanged(HierarchyEvent e) {
                if (e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED && addWindowListener) {
                    if (panel != null) {
                        Window window = SwingUtilities.getWindowAncestor(panel);
                        if (window != null) {
                            window.addWindowListener(AbstractDisplay3D.this);
                            addWindowListener = false;
                        }
                    }
                }
            }
        });
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
        layoutUpdater.setLayout(layout);
    }

    public void setLayoutFrequency(IDisplay.LayoutFrequency frequency, int interval) {
        if (frequency == LayoutFrequency.AT_UPDATE) {
            layoutUpdater = new UpdateLayoutUpdater(layout);
        } else if (frequency == LayoutFrequency.AT_INTERVAL) {
            layoutUpdater = new IntervalLayoutUpdater(layout, interval);
        } else if (frequency == LayoutFrequency.ON_NEW) {
            layoutUpdater = new AddedRemovedLayoutUpdater(layout);
        } else if (frequency == LayoutFrequency.ON_MOVE) {
            layoutUpdater = new MovedLayoutUpdater(layout);
        }
    }

    public synchronized void update() {
        if (getCanvas() != null && !getCanvas().isDisplayable()) return;
        layoutUpdater.update();
        for (IDisplayLayer layer : displayMap.values()) {
            layer.update(layoutUpdater);
        }
        for (IDisplayLayer layer : networkMap.values()) {
            layer.update(layoutUpdater);
        }
        valueLayer.update();
    }

    protected abstract AbstractDisplayLayer3D createDisplayLayer(Style3D style);

    protected abstract AbstractDisplayLayer3D createEdgeLayer(EdgeStyle3D style, Network network);

    protected abstract ValueDisplayLayer3D createValueLayerDisplayLayer(ValueLayerStyle3D style);

    protected void addObject(Object obj) {
        AbstractDisplayLayer3D layer = findLayer(obj);
        if (layer != null) {
            layer.addObject(obj);
            layoutUpdater.addTriggerCondition(LayoutUpdater.Condition.ADDED);
        }
    }

    protected AbstractDisplayLayer3D findLayer(Object obj) {
        Class<? extends Object> objClass = obj.getClass();
        AbstractDisplayLayer3D layer = displayMap.get(objClass);
        if (layer == null) {
            for (Class<?> clazz : displayMap.keySet()) {
                if (clazz.isAssignableFrom(objClass)) {
                    layer = displayMap.get(clazz);
                    break;
                }
            }
        }
        return layer;
    }

    @Override
    protected void moveObject(Object o) {
        layoutUpdater.addTriggerCondition(LayoutUpdater.Condition.MOVED);
    }

    protected void removeObject(Object obj) {
        IDisplayLayer layer = findLayer(obj);
        if (layer != null) {
            layer.removeObject(obj);
            layoutUpdater.addTriggerCondition(LayoutUpdater.Condition.REMOVED);
        }
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
        iconified = false;
    }

    public void windowIconified(WindowEvent e) {
        iconified = true;
    }

    public void windowOpened(WindowEvent e) {
    }

    /**
   * Get the data used to initialize this display.
   *
   * @return the data used to initialize this display.
   */
    public DisplayData<?> getInitData() {
        return initData;
    }

    public abstract Canvas3D getCanvas();
}
