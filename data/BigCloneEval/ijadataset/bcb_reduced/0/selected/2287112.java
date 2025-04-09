package net.sourceforge.ondex.ovtk2.ui.popup;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.QuerySetParser;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins.AccessionPlugin;
import net.sourceforge.ondex.ovtk2.ui.popup.custom.CustomPopupItem;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;
import net.sourceforge.ondex.transformer.relationcollapser.ClusterCollapser;
import net.sourceforge.ondex.validator.htmlaccessionlink.Condition;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Menu shown on right click on nodes.
 * 
 * @author taubertj
 * 
 */
public class VertexMenu extends JPopupMenu implements VertexMenuListener<ONDEXConcept, ONDEXRelation>, MenuPointListener {

    private static final long serialVersionUID = 7519988778944416506L;

    private DialogConceptItem conceptInfo = null;

    private OVTK2Viewer viewer = null;

    private static final boolean DEBUG = false;

    public static VertexMenu INSTANCE = null;

    /**
	 * Constructor for a given OVTK2Viewer.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
    public VertexMenu(OVTK2Viewer viewer) {
        this.viewer = viewer;
        INSTANCE = this;
    }

    protected Set<ONDEXConcept> getMultipleNodes(OVTK2Viewer viewer, ONDEXConcept vertex) {
        Set<ONDEXConcept> pickedNodes = viewer.getPickedNodes();
        if (!pickedNodes.contains(vertex)) pickedNodes = Collections.singleton(vertex);
        return pickedNodes;
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
                if (!acc.isAmbiguous() && vertex.getElementOf().getId().contains(acc.getElementOf().getId())) {
                    try {
                        new AccessionPlugin(viewer.getONDEXJUNGGraph());
                    } catch (InvalidPluginArgumentException e) {
                        ErrorDialog.show(e);
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
                                            ErrorDialog.show(ioe);
                                        }
                                    } else {
                                        JOptionPane.showInputDialog(desktop, "Hyperlinks not supported by OS.");
                                    }
                                }
                            });
                            this.add(item);
                        } catch (URISyntaxException e1) {
                            ErrorDialog.show(e1);
                        }
                    }
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setVertexAndView(ONDEXConcept vertex, VisualizationViewer<ONDEXConcept, ONDEXRelation> visComp) {
        removeAll();
        addLink(vertex);
        JMenu change = new JMenu(Config.language.getProperty("Viewer.VertexMenu.ChangeBy"));
        JMenu link = new JMenu(Config.language.getProperty("Viewer.VertexMenu.LinkBy"));
        JMenu hide = new JMenu(Config.language.getProperty("Viewer.VertexMenu.HideBy"));
        JMenu show = new JMenu(Config.language.getProperty("Viewer.VertexMenu.ShowBy"));
        Set<ONDEXConcept> nodes = getMultipleNodes(viewer, vertex);
        ArrayList<String> entries = new ArrayList<String>();
        Enumeration<?> enu = Config.config.propertyNames();
        while (enu.hasMoreElements()) {
            String name = (String) enu.nextElement();
            if (name.startsWith("Viewer.VertexMenu.")) {
                entries.add(name);
            }
        }
        try {
            Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
        } catch (FileNotFoundException fnfe) {
            ErrorDialog.show(fnfe);
        }
        String[] ordered = entries.toArray(new String[entries.size()]);
        Arrays.sort(ordered);
        try {
            for (String name : ordered) {
                String clazz = Config.config.getProperty(name);
                int index = clazz.lastIndexOf(".");
                name = clazz.substring(index + 1, clazz.length());
                if (DEBUG) System.err.println("class mapping: " + name + " ->  " + clazz);
                EntityMenuItem<ONDEXConcept> item = OVTK2PluginLoader.getInstance().loadPopupItem(name);
                item.init(viewer, nodes);
                if (item.accepts()) {
                    switch(item.getCategory()) {
                        case HIDE:
                            hide.add(item.getItem());
                            break;
                        case SHOW:
                            show.add(item.getItem());
                            break;
                        case LINK:
                            link.add(item.getItem());
                            break;
                        case CHANGE:
                            change.add(item.getItem());
                            break;
                    }
                }
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        }
        if (change.getSubElements().length > 0) add(change);
        if (link.getSubElements().length > 0) add(link);
        if (hide.getSubElements().length > 0) add(hide);
        if (show.getSubElements().length > 0) add(show);
        addSeparator();
        conceptInfo = new DialogConceptItem();
        conceptInfo.init(viewer, nodes);
        add(conceptInfo.getItem());
        addSeparator();
        if (Boolean.parseBoolean(Config.config.getProperty("PopupEditor.Enable"))) {
            CustomPopupItem menuItem = new CustomPopupItem();
            menuItem.init(viewer, nodes);
            if (menuItem.accepts()) {
                add(menuItem.getItem());
                addSeparator();
            }
        }
        boolean selectedConcepts = (viewer.getPickedNodes().size() != 0 || vertex != null) ? true : false;
        boolean selectedRelations = viewer.getPickedEdges().size() == 0 ? false : true;
        Set<String> layoutAndCenter = new HashSet<String>();
        boolean empty = true;
        JMenu querry = new JMenu("Query");
        Map<String, List<String>> options = new HashMap<String, List<String>>();
        List<String> menuCommands = new ArrayList<String>();
        try {
            Class<?> clsInterp = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.scripting.sparql.SPARQLInterpreter");
            Object instanceInterp = clsInterp.getMethod("getCurrentInstance", new Class<?>[0]).invoke(clsInterp, new Object[0]);
            if (instanceInterp != null) {
                boolean success = (Boolean) clsInterp.getMethod("configure", new Class<?>[0]).invoke(instanceInterp, new Object[0]);
                if (!success) return;
                QuerySetParser qs = (QuerySetParser) clsInterp.getMethod("getQuerySetParser", new Class<?>[0]).invoke(instanceInterp, new Object[0]);
                File file = qs.getQuerySetLocation();
                BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath() + File.separator + "interactive.sqs"));
                String line;
                while ((line = br.readLine()) != null) {
                    boolean doLayout = false;
                    if (line.startsWith("#COMMAND_LIST")) {
                        if (line.contains("*")) {
                            doLayout = true;
                        }
                        do {
                            line = br.readLine();
                        } while (line.trim().length() == 0);
                        menuCommands.add(line.trim());
                        if (doLayout) {
                            layoutAndCenter.add(line.trim());
                        }
                    }
                }
                br.close();
                for (String s : menuCommands) {
                    options.put(s, qs.getCommandList(s));
                }
                Map<String, List<String>> edgeOptions = new HashMap<String, List<String>>();
                Map<String, List<String>> nodeOptions = new HashMap<String, List<String>>();
                for (Entry<String, List<String>> ent : options.entrySet()) {
                    boolean isEdge = false;
                    boolean isNode = false;
                    for (String s : ent.getValue()) {
                        if (EntityURIMenuItem.PRIMARY_URI.matcher(s).find()) {
                            isNode = true;
                        } else if (EntityURIMenuItem.FROM_URI.matcher(s).find()) {
                            isEdge = true;
                        } else if (EntityURIMenuItem.TO_URI.matcher(s).find()) {
                            isEdge = true;
                        } else if (EntityURIMenuItem.TYPE.matcher(s).find()) {
                            isEdge = true;
                        }
                    }
                    if (isEdge) {
                        edgeOptions.put(ent.getKey(), ent.getValue());
                    } else if (isNode) {
                        nodeOptions.put(ent.getKey(), ent.getValue());
                    }
                }
                if (selectedConcepts) {
                    for (Entry<String, List<String>> ent : nodeOptions.entrySet()) {
                        EntityURIMenuItem vm = EntityURIMenuItem.getMenuItem(viewer, vertex, null, ent.getKey(), ent.getValue(), Collections.EMPTY_LIST, layoutAndCenter);
                        if (vm != null) {
                            empty = false;
                            querry.add(vm);
                        }
                    }
                }
                if (selectedConcepts && selectedRelations) {
                    querry.addSeparator();
                }
                if (selectedRelations) {
                    for (Entry<String, List<String>> ent : edgeOptions.entrySet()) {
                        EntityURIMenuItem vm = EntityURIMenuItem.getMenuItem(viewer, vertex, null, ent.getKey(), Collections.EMPTY_LIST, ent.getValue(), layoutAndCenter);
                        if (vm != null) {
                            empty = false;
                            querry.add(vm);
                        }
                    }
                }
                if (selectedConcepts) {
                    querry.addSeparator();
                    EntityURIMenuItem vm = VertexURLResolverMenuItem.getMenuItem(viewer, vertex, null, "Resolve URL");
                    querry.add(vm);
                }
                if (!empty) {
                    add(querry);
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        if (viewer.getPickedNodes().size() > 1) {
            JMenuItem merge = new JMenuItem("merge selection");
            merge.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int option = JOptionPane.showInternalConfirmDialog(viewer, "This will merge the selected concepts into one.\n" + "This step cannot be undone.\n" + "Do you want to continue?", "Merging concepts", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (option == JOptionPane.NO_OPTION) return;
                    ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
                    ClusterCollapser collapser = new ClusterCollapser(true, true, null);
                    HashSet<ONDEXConcept> toMerge = new HashSet<ONDEXConcept>(viewer.getPickedNodes());
                    graph.setVisibility(toMerge, false);
                    try {
                        ONDEXConcept merged = collapser.collapseConceptCluster(graph, toMerge);
                        graph.setVisibility(merged, true);
                        Set<ONDEXConcept> neighbourNodes = new HashSet<ONDEXConcept>();
                        for (ONDEXRelation r : graph.getRelationsOfConcept(merged)) {
                            graph.setVisibility(r, true);
                            if (r.getFromConcept().equals(merged)) neighbourNodes.add(r.getToConcept()); else neighbourNodes.add(r.getFromConcept());
                        }
                        LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), merged, neighbourNodes);
                        viewer.center();
                    } catch (InconsistencyException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            addSeparator();
            add(merge);
        }
    }

    @Override
    public void setPoint(Point point) {
        conceptInfo.setPoint(point);
    }
}
