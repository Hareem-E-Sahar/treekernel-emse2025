package net.datao.selector.impl;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import net.datao.jung.layout.MyCircleLayout;
import net.datao.jung.layout.MyFRLayout;
import net.datao.jung.layout.MyFRLayout2;
import net.datao.jung.ontologiesItems.Edge;
import net.datao.jung.ontologiesItems.Vertex;
import net.datao.jung.ontologiesItems.ClassGraph;
import net.datao.selector.LayoutSelectionDispatcher;
import net.datao.selector.LayoutSelectionListener;
import net.datao.selector.LayoutSelector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lolive
 * Date: 3 janv. 2008
 * Time: 12:15:54
 * To change this template use File | Settings | File Templates.
 */
public class LayoutSelectorImpl implements LayoutSelector {

    protected Layout selectedLayout;

    protected List<? extends Layout> repository;

    protected List<Class> availableLayout = new ArrayList<Class>();

    protected Map<Class, Layout> typedLayout = new HashMap<Class, Layout>();

    protected LayoutSelectionDispatcher layoutSelectionDispatcher;

    public static Layout EMPTYLAYOUT = new MyFRLayout(new DirectedSparseGraph());

    public LayoutSelectorImpl(Graph g) {
        availableLayout.add(MyCircleLayout.class);
        availableLayout.add(MyFRLayout.class);
        availableLayout.add(MyFRLayout2.class);
        layoutSelectionDispatcher = new LayoutSelectionDispatcherImpl();
        selectedLayout = new MyCircleLayout(g);
        typedLayout.put(selectedLayout.getClass(), selectedLayout);
    }

    public LayoutSelectorImpl(Graph g, Class layoutClass) {
        availableLayout.add(MyCircleLayout.class);
        availableLayout.add(MyFRLayout.class);
        availableLayout.add(MyFRLayout2.class);
        if (!availableLayout.contains(layoutClass)) availableLayout.add(layoutClass);
        layoutSelectionDispatcher = new LayoutSelectionDispatcherImpl();
        Layout defaultLayout = null;
        try {
            defaultLayout = (Layout) layoutClass.getConstructor(Graph.class).newInstance(g);
        } catch (Exception ex) {
            ex.printStackTrace();
            defaultLayout = new MyCircleLayout(g);
        }
        selectedLayout = defaultLayout;
        typedLayout.put(selectedLayout.getClass(), selectedLayout);
    }

    public Layout<Vertex, Edge> getSelectedLayout() {
        return selectedLayout;
    }

    public List<Class> getLayoutsAsList() {
        return availableLayout;
    }

    public void setSelectedLayoutType(Class layoutClass) {
        if (!Layout.class.isAssignableFrom(layoutClass)) throw new RuntimeException("Selected layout is of type " + layoutClass.getName() + ". It is not a Layout. Exiting...");
        if (layoutClass == getSelectedLayout().getClass()) return;
        Graph selectedGraph = getSelectedLayout().getGraph();
        if (!typedLayout.containsKey(layoutClass)) if (layoutClass.equals(MyCircleLayout.class)) typedLayout.put(layoutClass, new MyCircleLayout(selectedGraph)); else if (layoutClass.equals(MyFRLayout.class)) typedLayout.put(layoutClass, new MyFRLayout(selectedGraph)); else if (layoutClass.equals(MyFRLayout2.class)) typedLayout.put(layoutClass, new MyFRLayout2(selectedGraph)); else throw new RuntimeException("Layout of class " + layoutClass.getName() + " is unavailable. Exiting...");
        selectedLayout = typedLayout.get(layoutClass);
        if (selectedGraph != selectedLayout.getGraph()) selectedLayout.setGraph(selectedGraph);
        fireLayoutSelectionEvent(new LayoutSelectionEvent(this));
    }

    public void addLayoutSelectionListener(LayoutSelectionListener l) {
        layoutSelectionDispatcher.addLayoutSelectionListener(l);
    }

    public void removeLayoutSelectionListener(LayoutSelectionListener l) {
        layoutSelectionDispatcher.removeLayoutSelectionListener(l);
    }

    public void fireLayoutSelectionEvent(LayoutSelectionEvent e) {
        layoutSelectionDispatcher.fireLayoutSelectionEvent(e);
    }
}
