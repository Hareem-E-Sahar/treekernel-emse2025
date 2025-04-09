package net.sourceforge.ondex.ovtk2lite.popup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins.AccessionPlugin;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.ui.popup.VertexMenuListener;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ChangeNodeColorItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ChangeNodeShapeItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ChangeNodeSizeItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.HideNodeConceptClassItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.HideNodeDataSourceItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.HideNodeEvidenceTypeItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.HideNodeItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.HideNodeLabelItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.HideNodeSameTagItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.HideNodeTagItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.HideOtherNodesItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ShowNodeLabelItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ShowNodeNeighbourhoodConceptClassItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ShowNodeNeighbourhoodItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ShowNodeNeighbourhoodRelationTypeItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ShowNodeRelationsVisibleItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ShowNodeSameTagItem;
import net.sourceforge.ondex.ovtk2.ui.popup.items.ShowNodeTagItem;
import net.sourceforge.ondex.validator.htmlaccessionlink.Condition;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Menu shown on right click on nodes.
 * 
 * @author taubertj
 * 
 */
public class VertexMenu extends JPopupMenu implements VertexMenuListener<ONDEXConcept, ONDEXRelation> {

    OVTK2PropertiesAggregator aggregator;

    JMenu change, hide, show;

    /**
	 * generated
	 */
    private static final long serialVersionUID = -1024568069913408029L;

    public VertexMenu(OVTK2PropertiesAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @Override
    public void setVertexAndView(ONDEXConcept vertex, VisualizationViewer<ONDEXConcept, ONDEXRelation> visComp) {
        removeAll();
        addLink(vertex);
        change = new JMenu(Config.language.getProperty("Viewer.VertexMenu.ChangeBy"));
        hide = new JMenu(Config.language.getProperty("Viewer.VertexMenu.HideBy"));
        show = new JMenu(Config.language.getProperty("Viewer.VertexMenu.ShowBy"));
        PickedState<ONDEXConcept> pickedNodes = aggregator.getVisualizationViewer().getPickedVertexState();
        if (!pickedNodes.isPicked(vertex)) pickedNodes.pick(vertex, true);
        Set<ONDEXConcept> set = pickedNodes.getPicked();
        addItem(new ChangeNodeColorItem(), set);
        addItem(new ChangeNodeShapeItem(), set);
        addItem(new ChangeNodeSizeItem(), set);
        addItem(new HideNodeConceptClassItem(), set);
        addItem(new HideNodeDataSourceItem(), set);
        addItem(new HideNodeEvidenceTypeItem(), set);
        addItem(new HideNodeItem(), set);
        addItem(new HideNodeLabelItem(), set);
        addItem(new HideNodeSameTagItem(), set);
        addItem(new HideNodeTagItem(), set);
        addItem(new HideOtherNodesItem(), set);
        addItem(new ShowNodeLabelItem(), set);
        addItem(new ShowNodeNeighbourhoodConceptClassItem(), set);
        addItem(new ShowNodeNeighbourhoodItem(), set);
        addItem(new ShowNodeNeighbourhoodRelationTypeItem(), set);
        addItem(new ShowNodeRelationsVisibleItem(), set);
        addItem(new ShowNodeSameTagItem(), set);
        addItem(new ShowNodeTagItem(), set);
        if (change.getSubElements().length > 0) add(change);
        if (hide.getSubElements().length > 0) add(hide);
        if (show.getSubElements().length > 0) add(show);
    }

    /**
	 * Adds an EntityMenuItem to the correct menu if it accepts graph selection
	 * 
	 * @param item
	 * @param set
	 */
    private void addItem(EntityMenuItem<ONDEXConcept> item, Set<ONDEXConcept> set) {
        item.init(aggregator, set);
        if (item.accepts()) {
            switch(item.getCategory()) {
                case HIDE:
                    hide.add(item.getItem());
                    break;
                case SHOW:
                    show.add(item.getItem());
                    break;
                case CHANGE:
                    change.add(item.getItem());
                    break;
            }
        }
    }

    /**
	 * Constructs a click able menu entry for the relevant accession link, which
	 * is non-ambiguous and of same data source, only the first one found is
	 * taken.
	 * 
	 * @param vertex
	 */
    private void addLink(ONDEXConcept vertex) {
        Set<ConceptAccession> accs = vertex.getConceptAccessions();
        if (accs.size() > 0) {
            for (ConceptAccession acc : accs) {
                if (!acc.isAmbiguous() && acc.getElementOf().equals(vertex.getElementOf())) {
                    try {
                        new AccessionPlugin(aggregator.getONDEXJUNGGraph());
                    } catch (InvalidPluginArgumentException e) {
                        JOptionPane.showMessageDialog((Component) aggregator, e.getMessage());
                    }
                    String url = AccessionPlugin.cvToURL.get(acc.getElementOf().getId());
                    if (AccessionPlugin.mapper != null) {
                        Condition cond = new Condition(acc.getElementOf().getId(), vertex.getElementOf().getId());
                        String prefix = (String) AccessionPlugin.mapper.validate(cond);
                        if (prefix != null && prefix.length() > 0) {
                            url = prefix;
                        }
                    }
                    if (url != null) {
                        try {
                            final URI uri = new URI(url + "" + acc.getAccession());
                            JMenuItem item = new JMenuItem(acc.getElementOf().getId() + ": " + acc.getAccession());
                            item.setForeground(Color.BLUE);
                            item.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Desktop desktop = null;
                                    if (Desktop.isDesktopSupported()) {
                                        desktop = Desktop.getDesktop();
                                        try {
                                            desktop.browse(uri);
                                        } catch (IOException ioe) {
                                            JOptionPane.showMessageDialog((Component) aggregator, ioe.getMessage());
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog((Component) aggregator, "Hyperlinks not supported by OS.");
                                    }
                                }
                            });
                            this.add(item);
                        } catch (URISyntaxException e1) {
                            JOptionPane.showMessageDialog((Component) aggregator, e1.getMessage());
                        }
                    }
                    break;
                }
            }
        }
    }
}
