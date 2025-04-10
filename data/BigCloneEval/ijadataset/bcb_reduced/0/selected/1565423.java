package edu.tufts.vue.layout;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import tufts.vue.*;
import tufts.vue.ds.DataAction;
import tufts.vue.ds.XmlDataSource;
import edu.tufts.vue.metadata.MetadataList;
import edu.tufts.vue.metadata.VueMetadataElement;
import edu.tufts.vue.dataset.*;

public class ListRandomLayout extends Layout {

    public static String DEFAULT_METADATA_LABEL = "default";

    public static final double FACTOR = 1.5;

    public static final int MAX_COLLISION_CHECK = 20;

    /** Creates a new instance of ListRandomLayout */
    public ListRandomLayout() {
    }

    public LWMap createMap(Dataset ds, String mapName) throws Exception {
        Map<String, LWNode> nodeMap = new HashMap<String, LWNode>();
        Map<String, Integer> repeatMap = new HashMap<String, Integer>();
        LWMap map = new LWMap(mapName);
        XmlDataSource datasource = new XmlDataSource(mapName, ds.getFileName());
        Properties props = new Properties();
        props.put("displayName", mapName);
        props.put("name", mapName);
        props.put("address", ds.getFileName());
        datasource.setConfiguration(props);
        VUE.getContentDock().setVisible(true);
        VUE.getContentPanel().showDatasetsTab();
        DataSetViewer.getDataSetList().addOrdered(datasource);
        VUE.getContentPanel().getDSBrowser().getDataSetViewer().setActiveDataSource(datasource);
        DataSourceViewer.saveDataSourceViewer();
        List<LWComponent> nodes = DataAction.makeRowNodes(datasource.getSchema());
        for (LWComponent component : nodes) {
            map.add(component);
        }
        LayoutAction.random.act(new LWSelection(nodes));
        return map;
    }

    /** It takes a map and scatters the nodes randomly
     * @param map the map for layout
     */
    public void layout(LWMap map) {
        Iterator<LWNode> nodeIterator = map.getChildIterator();
        while (nodeIterator.hasNext()) {
            LWNode node = nodeIterator.next();
            node.setLocation(MAP_SIZE * Math.random(), MAP_SIZE * Math.random());
        }
    }

    public void layout(java.util.List<LWNode> nodeList) {
        for (LWNode node : nodeList) {
            node.setLocation(MAP_SIZE * Math.random(), MAP_SIZE * Math.random());
        }
    }

    public void layout(LWSelection selection) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double xAdd = X_COL_SIZE;
        double yAdd = Y_COL_SIZE;
        int total = 0;
        Iterator<LWComponent> i = selection.iterator();
        while (i.hasNext()) {
            LWComponent c = i.next();
            if (c instanceof LWNode) {
                LWNode node = (LWNode) c;
                minX = node.getLocation().getX() < minX ? node.getLocation().getX() : minX;
                minY = node.getLocation().getY() < minY ? node.getLocation().getY() : minY;
                maxX = node.getLocation().getX() > maxX ? node.getLocation().getX() : maxX;
                maxY = node.getLocation().getY() > maxY ? node.getLocation().getY() : maxY;
                xAdd = xAdd > node.getWidth() ? xAdd : node.getWidth();
                yAdd = yAdd > node.getHeight() ? yAdd : node.getHeight();
                total++;
            }
        }
        double xSize = Math.sqrt(total) * (xAdd + X_SPACING) * FACTOR;
        double ySize = Math.sqrt(total) * (yAdd + Y_SPACING) * FACTOR;
        i = selection.iterator();
        while (i.hasNext()) {
            LWComponent c = i.next();
            if (c instanceof LWNode) {
                LWNode node = (LWNode) c;
                double centerX = (minX + maxX) / 2;
                double centerY = (minY + maxY) / 2;
                double x = centerX + xSize * (Math.random() - 0.5);
                double y = centerY + ySize * (Math.random() - 0.5);
                int col_count = 0;
                boolean flag = true;
                while (flag && col_count < MAX_COLLISION_CHECK) {
                    LWComponent overlapComponent;
                    if (VUE.getActiveViewer() != null) {
                        if ((VUE.getActiveViewer().pickNode((float) x, (float) y) != null) || (VUE.getActiveViewer().pickNode((float) x + node.getWidth(), (float) y + node.getHeight()) != null) || (VUE.getActiveViewer().pickNode((float) x, (float) y + node.getHeight()) != null) || (VUE.getActiveViewer().pickNode((float) x + node.getWidth(), (float) y) != null)) {
                            x = centerX + xSize * (Math.random() - 0.5);
                            y = centerY + ySize * (Math.random() - 0.5);
                        } else {
                            flag = false;
                        }
                    }
                    col_count++;
                }
                node.setLocation(x, y);
            }
        }
    }
}
