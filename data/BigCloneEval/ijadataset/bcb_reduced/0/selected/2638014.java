package owl2prefuse.graph;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import owl2prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.layout.Layout;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 * Set label positions. Labels are assumed to be DecoratorItem instances,
 * decorating their respective edges. The layout simply gets the bounds
 * of the decorated edge and assigns the label coordinates to 30% of the center
 * of those bounds. This ensures that labels are not placed on top of each other 
 * in the case of bidirectional nodes.
 * <p/>
 * Project OWL2Prefuse <br/>
 * EdgeLabelLayout.java created 3 januari 2007, 14:41
 * <p/>
 * Copyright &copy 2006 Jethro Borsje
 * 
 * @author <a href="mailto:info@jborsje.nl">Jethro Borsje</a>
 * @version $$Revision:$$, $$Date:$$
 */
class EdgeLabelLayout extends Layout {

    /**
     * Creates a new instance of EdgeLabelLayout.
     * @param p_vis A reference to the visualization processed by this Action.
     */
    public EdgeLabelLayout(Visualization p_vis) {
        super(Constants.EDGE_DECORATORS);
        m_vis = p_vis;
    }

    /**
     * This method is an abstract method from Action which is overloaded here. It 
     * places the labels in the center of the VisualItem in the data group.
     * @param frac The fraction of this Action's duration that has elapsed.
     */
    public void run(double frac) {
        Iterator iter = m_vis.items(m_group);
        while (iter.hasNext()) {
            DecoratorItem decorator = (DecoratorItem) iter.next();
            VisualItem decoratedItem = decorator.getDecoratedItem();
            Rectangle2D bounds = decoratedItem.getBounds();
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            double x2 = 0;
            double y2 = 0;
            if (decoratedItem instanceof EdgeItem) {
                VisualItem dest = ((EdgeItem) decoratedItem).getTargetItem();
                x2 = dest.getX();
                y2 = dest.getY();
                x = (x + x2) / 2;
                y = (y + y2) / 2;
            }
            setX(decorator, null, x);
            setY(decorator, null, y);
        }
    }
}
