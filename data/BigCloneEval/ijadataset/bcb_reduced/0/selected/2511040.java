package javax.help.plaf.basic;

import javax.help.*;
import javax.help.search.*;
import javax.help.plaf.HelpNavigatorUI;
import javax.help.event.HelpModelListener;
import javax.help.event.HelpModelEvent;
import javax.help.search.SearchListener;
import javax.help.search.SearchEvent;
import java.util.EventObject;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.MalformedURLException;
import javax.help.DefaultHelpModel.DefaultHighlight;
import javax.help.Map.ID;

public class BasicSearchNavigatorUI extends HelpNavigatorUI implements HelpModelListener, SearchListener, PropertyChangeListener, TreeSelectionListener, ComponentListener {

    protected JHelpSearchNavigator searchnav;

    protected JScrollPane sp;

    protected JTree tree;

    protected DefaultMutableTreeNode topNode;

    protected JTextField searchparams;

    protected boolean displayOptions;

    protected SearchEngine helpsearch;

    protected SearchQuery searchquery;

    protected DefaultMutableTreeNode lastTOCnode;

    private HelpSet newHelpSet;

    public static ComponentUI createUI(JComponent x) {
        return new BasicSearchNavigatorUI((JHelpSearchNavigator) x);
    }

    public BasicSearchNavigatorUI(JHelpSearchNavigator b) {
        ImageIcon icon = getImageIcon(b.getNavigatorView());
        if (icon != null) {
            setIcon(icon);
        } else {
            setIcon(UIManager.getIcon("SearchNav.icon"));
        }
    }

    ActionListener searchAction = new SearchActionListener();

    private Cursor paramCursor;

    private Cursor treeCursor;

    private Cursor waitCursor = null;

    class SearchActionListener implements ActionListener {

        public synchronized void actionPerformed(ActionEvent e) {
            HelpModel helpmodel = searchnav.getModel();
            try {
                if (paramCursor == null) {
                    paramCursor = searchparams.getCursor();
                }
                if (treeCursor == null) {
                    treeCursor = tree.getCursor();
                }
                if (waitCursor == null) {
                    waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                }
                searchparams.setCursor(waitCursor);
                tree.setCursor(waitCursor);
                if (helpsearch == null) {
                    debug("helpsearch is null");
                    helpsearch = searchnav.getSearchEngine();
                    searchquery = helpsearch.createQuery();
                    searchquery.addSearchListener(BasicSearchNavigatorUI.this);
                }
                debug("click on search w/: " + searchparams.getText());
                if (searchquery.isActive()) {
                    searchquery.stop();
                }
                searchquery.start(searchparams.getText(), searchnav.getLocale());
            } catch (Exception e2) {
                searchparams.setCursor(paramCursor);
                tree.setCursor(treeCursor);
                e2.printStackTrace();
                searchnav.getToolkit().beep();
                searchnav.getToolkit().beep();
            }
        }
    }

    public void installUI(JComponent c) {
        searchnav = (JHelpSearchNavigator) c;
        HelpModel helpmodel = searchnav.getModel();
        searchnav.setLayout(new BorderLayout());
        searchnav.addPropertyChangeListener(this);
        searchnav.addComponentListener(this);
        if (helpmodel != null) {
            helpmodel.addHelpModelListener(this);
        }
        JLabel search = new JLabel(HelpUtilities.getString(HelpUtilities.getLocale(c), "search.findLabel"));
        searchparams = new JTextField("", 20);
        search.setLabelFor(searchparams);
        searchparams.addActionListener(searchAction);
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.add(search);
        box.add(searchparams);
        searchnav.add("North", box);
        topNode = new DefaultMutableTreeNode();
        lastTOCnode = null;
        tree = new JTree(topNode);
        TreeSelectionModel tsm = tree.getSelectionModel();
        tsm.addTreeSelectionListener(this);
        tree.setShowsRootHandles(false);
        tree.setRootVisible(false);
        sp = new JScrollPane();
        sp.getViewport().add(tree);
        searchnav.add("Center", sp);
        reloadData();
    }

    public void uninstallUI(JComponent c) {
        HelpModel helpmodel = searchnav.getModel();
        searchnav.removeComponentListener(this);
        searchnav.removePropertyChangeListener(this);
        TreeSelectionModel tsm = tree.getSelectionModel();
        tsm.removeTreeSelectionListener(this);
        searchnav.setLayout(null);
        searchnav.removeAll();
        if (helpmodel != null) {
            helpmodel.removeHelpModelListener(this);
        }
        searchnav = null;
    }

    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(200, 100);
    }

    public Dimension getMinimumSize(JComponent c) {
        if (sp != null) {
            return ((ScrollPaneLayout) sp.getLayout()).minimumLayoutSize(sp);
        } else {
            return new Dimension(100, 100);
        }
    }

    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    /**
     * Indicates that there is new search data to use.
     */
    private void reloadData() {
        helpsearch = null;
        setCellRenderer(searchnav.getNavigatorView(), tree);
        HelpModel model = searchnav.getModel();
        if (model != null) {
            addSubHelpSets(model.getHelpSet());
        }
    }

    /**
     * Reloads data from new model, creates new search engine to search in new model if model
     * contains view with the same name
     **/
    private void reloadData(HelpModel model) {
        debug("reloadData using new model");
        helpsearch = null;
        SearchView view = null;
        newHelpSet = model.getHelpSet();
        SearchView oldView = (SearchView) searchnav.getNavigatorView();
        String oldName = oldView.getName();
        NavigatorView[] navViews = newHelpSet.getNavigatorViews();
        for (int i = 0; i < navViews.length; i++) {
            if ((navViews[i].getName()).equals(oldName)) {
                NavigatorView tempView = navViews[i];
                if (tempView instanceof SearchView) {
                    view = (SearchView) tempView;
                    break;
                }
            }
        }
        if (view == null) return;
        topNode.removeAllChildren();
        searchnav.setSearchEngine(new MergingSearchEngine(view));
        setCellRenderer(view, tree);
        addSubHelpSets(newHelpSet);
    }

    /** Adds subhelpsets
     *
     * @param hs The HelpSet which subhelpsets will be added
     */
    protected void addSubHelpSets(HelpSet hs) {
        for (Enumeration e = hs.getHelpSets(); e.hasMoreElements(); ) {
            HelpSet ehs = (HelpSet) e.nextElement();
            NavigatorView[] views = ehs.getNavigatorViews();
            for (int i = 0; i < views.length; i++) {
                if (searchnav.canMerge(views[i])) searchnav.merge(views[i]);
            }
            addSubHelpSets(ehs);
        }
    }

    /**
     * Merges in the navigational data from another NavigatorView. 
     */
    public void merge(NavigatorView view) {
        debug("merging " + view);
        String text = searchparams.getText();
        if (text != null && text.length() != 0) {
            searchAction.actionPerformed(new ActionEvent(searchparams, ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    /**
     * Removes the navigational data from another NavigatorView. 
     */
    public void remove(NavigatorView view) {
        debug("removing " + view);
        if (searchparams.getText() != null) {
            searchAction.actionPerformed(new ActionEvent(searchparams, ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    /**
     * Setd the desired cell renderer on this tree.  This is exposed for redefinition
     * by subclases.
     */
    protected void setCellRenderer(NavigatorView view, JTree tree) {
        if (view == null) {
            return;
        }
        Map map = view.getHelpSet().getCombinedMap();
        tree.setCellRenderer(new BasicSearchCellRenderer(map));
    }

    /**
     * Processes an idChanged event. Search is different from all other
     * navigators in that you while search tree is synchronized 
     * the highlighting doesn't occur unless selected from the search
     * navigator.
     */
    public void idChanged(HelpModelEvent e) {
        ID id = e.getID();
        URL url = e.getURL();
        HelpModel helpModel = searchnav.getModel();
        debug("idChanged(" + e + ")");
        if (e.getSource() != helpModel) {
            debug("Internal inconsistency!");
            debug("  " + e.getSource() + " != " + helpModel);
            throw new Error("Internal error");
        }
        TreePath s = tree.getSelectionPath();
        if (s != null) {
            Object o = s.getLastPathComponent();
            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode tn = (DefaultMutableTreeNode) o;
                SearchTOCItem item = (SearchTOCItem) tn.getUserObject();
                if (item != null) {
                    ID nId = item.getID();
                    if (nId != null && nId.equals(id)) {
                        return;
                    }
                }
            }
        }
        DefaultMutableTreeNode node = findIDorURL(topNode, id, url);
        if (node == null) {
            debug("node didn't exist");
            tree.clearSelection();
            return;
        }
        TreePath path = new TreePath(node.getPath());
        tree.expandPath(path);
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    protected JHelpNavigator getHelpNavigator() {
        return searchnav;
    }

    /**
     * A value has changed.  This is used as a TreeSelectionListener.
     */
    public void valueChanged(TreeSelectionEvent e) {
        JHelpNavigator navigator = getHelpNavigator();
        HelpModel helpmodel = navigator.getModel();
        debug("ValueChanged: " + e);
        debug("  model: " + helpmodel);
        TreeItem[] items = null;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            items = new TreeItem[paths.length];
            for (int i = 0; i < paths.length; i++) {
                if (paths[i] != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
                    items[i] = (TreeItem) node.getUserObject();
                }
            }
        }
        navigator.setSelectedItems(items);
        if (items != null && items.length == 1) {
            SearchTOCItem item = (SearchTOCItem) items[0];
            if (item != null) {
                if (item.getID() != null) {
                    try {
                        helpmodel.setCurrentID(item.getID(), item.getName(), navigator);
                    } catch (InvalidHelpSetContextException ex) {
                        System.err.println("BadID: " + item.getID());
                        return;
                    }
                } else if (item.getURL() != null) {
                    helpmodel.setCurrentURL(item.getURL(), item.getName(), navigator);
                } else {
                    return;
                }
                if (helpmodel instanceof TextHelpModel) {
                    DefaultHighlight h[] = new DefaultHighlight[item.hitCount()];
                    int i = 0;
                    Enumeration enum1 = item.getSearchHits();
                    while (enum1.hasMoreElements()) {
                        SearchHit info = (SearchHit) enum1.nextElement();
                        h[i] = new DefaultHighlight(info.getBegin(), info.getEnd());
                        i++;
                    }
                    ((TextHelpModel) helpmodel).setHighlights(h);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        debug(this + " " + "propertyChange: " + event.getSource() + " " + event.getPropertyName());
        if (event.getSource() == searchnav) {
            String changeName = event.getPropertyName();
            if (changeName.equals("helpModel")) {
                reloadData((HelpModel) event.getNewValue());
            } else if (changeName.equals("font")) {
                debug("Font change");
                Font newFont = (Font) event.getNewValue();
                searchparams.setFont(newFont);
                RepaintManager.currentManager(searchparams).markCompletelyDirty(searchparams);
                tree.setFont(newFont);
                RepaintManager.currentManager(tree).markCompletelyDirty(tree);
            }
        }
    }

    /**
     * Invoked when the component's size changes.
     */
    public void componentResized(ComponentEvent e) {
    }

    /**
     * Invoked when the component's position changes.
     */
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * Invoked when the component has been made visible.
     */
    public void componentShown(ComponentEvent e) {
        searchparams.selectAll();
        searchparams.requestFocus();
    }

    /**
     * Invoked when the component has been made invisible.
     */
    public void componentHidden(ComponentEvent e) {
    }

    private DefaultMutableTreeNode findIDorURL(DefaultMutableTreeNode node, ID id, URL url) {
        SearchTOCItem item = (SearchTOCItem) node.getUserObject();
        if (item != null) {
            ID testID = item.getID();
            if (testID != null && id != null && testID.equals(id)) {
                return node;
            } else {
                URL testURL = item.getURL();
                if (testURL != null && url != null && url.sameFile(testURL)) {
                    return node;
                }
            }
        }
        int size = node.getChildCount();
        for (int i = 0; i < size; i++) {
            DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) node.getChildAt(i);
            DefaultMutableTreeNode test = findIDorURL(tmp, id, url);
            if (test != null) {
                return test;
            }
        }
        return null;
    }

    private void reorder(Vector nodes) {
        debug("reorder nodes");
        topNode.removeAllChildren();
        DefaultMutableTreeNode[] array = new DefaultMutableTreeNode[nodes.size()];
        nodes.copyInto(array);
        quickSort(array, 0, array.length - 1);
        for (int i = 0; i < array.length; i++) {
            topNode.add((DefaultMutableTreeNode) array[i]);
        }
        ((DefaultTreeModel) tree.getModel()).reload();
        tree.invalidate();
        tree.repaint();
    }

    /** This is a version of C.A.R Hoare's Quick Sort
    * algorithm.  This will handle arrays that are already
    * sorted, and arrays with duplicate keys.<BR>
    *
    * If you think of a one dimensional array as going from
    * the lowest index on the left to the highest index on the right
    * then the parameters to this function are lowest index or
    * left and highest index or right.  The first time you call
    * this function it will be with the parameters 0, a.length - 1.
    *
    * @param a       a DefaultMutableTreeNode array
    * @param lo0     left boundary of array partition
    * @param hi0     right boundary of array partition
    */
    void quickSort(DefaultMutableTreeNode a[], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        int mid;
        if (hi0 > lo0) {
            mid = (lo0 + hi0) / 2;
            while (lo <= hi) {
                while ((lo < hi0) && (compare(a[lo], a[mid]) > 0)) ++lo;
                while ((hi > lo0) && (compare(a[hi], a[mid]) < 0)) --hi;
                if (lo <= hi) {
                    swap(a, lo, hi);
                    ++lo;
                    --hi;
                }
            }
            if (lo0 < hi) quickSort(a, lo0, hi);
            if (lo < hi0) quickSort(a, lo, hi0);
        }
    }

    private void swap(DefaultMutableTreeNode a[], int i, int j) {
        DefaultMutableTreeNode T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;
    }

    private int compare(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2) {
        SearchTOCItem item1, item2;
        double confidence1, confidence2;
        int hits1, hits2;
        item1 = (SearchTOCItem) node1.getUserObject();
        confidence1 = item1.getConfidence();
        hits1 = item1.hitCount();
        item2 = (SearchTOCItem) node2.getUserObject();
        confidence2 = item2.getConfidence();
        hits2 = item2.hitCount();
        if (confidence1 > confidence2) {
            return -1;
        } else if (confidence1 < confidence2) {
            return 1;
        } else {
            if (hits1 < hits2) {
                return -1;
            } else if (hits1 > hits2) {
                return 1;
            }
        }
        return 0;
    }

    public synchronized void itemsFound(SearchEvent e) {
        SwingUtilities.invokeLater(new SearchItemsFound(e));
    }

    class SearchItemsFound implements Runnable {

        SearchEvent e;

        SearchItemsFound(SearchEvent e) {
            this.e = e;
        }

        public void run() {
            SearchTOCItem tocitem;
            Vector nodes = new Vector();
            Enumeration children = topNode.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
                nodes.addElement(node);
            }
            debug("items found");
            HelpModel helpmodel = searchnav.getModel();
            HelpSet hs = helpmodel.getHelpSet();
            debug("hs:" + hs.toString());
            Map map = hs.getCombinedMap();
            Enumeration itemEnum = e.getSearchItems();
            while (itemEnum.hasMoreElements()) {
                SearchItem item = (SearchItem) itemEnum.nextElement();
                debug("  item: " + item);
                URL url;
                try {
                    url = new URL(item.getBase(), item.getFilename());
                } catch (MalformedURLException me) {
                    System.err.println("Failed to create URL from " + item.getBase() + "|" + item.getFilename());
                    continue;
                }
                boolean foundNode = false;
                DefaultMutableTreeNode node = null;
                Enumeration nodesEnum = nodes.elements();
                while (nodesEnum.hasMoreElements()) {
                    node = (DefaultMutableTreeNode) nodesEnum.nextElement();
                    tocitem = (SearchTOCItem) node.getUserObject();
                    URL testURL = tocitem.getURL();
                    if (testURL != null && url != null && url.sameFile(testURL)) {
                        tocitem = (SearchTOCItem) node.getUserObject();
                        tocitem.addSearchHit(new SearchHit(item.getConfidence(), item.getBegin(), item.getEnd()));
                        foundNode = true;
                        break;
                    }
                }
                if (!foundNode) {
                    tocitem = new SearchTOCItem(item);
                    node = new DefaultMutableTreeNode(tocitem);
                    nodes.addElement(node);
                }
            }
            reorder(nodes);
            ((DefaultTreeModel) tree.getModel()).reload();
        }
    }

    public synchronized void searchStarted(SearchEvent e) {
        debug("search Started");
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                TreeSelectionModel tsm = tree.getSelectionModel();
                tsm.clearSelection();
                topNode.removeAllChildren();
                ((DefaultTreeModel) tree.getModel()).reload();
                tree.invalidate();
                tree.repaint();
            }
        });
    }

    public synchronized void searchFinished(SearchEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                TreeSelectionModel tsm = tree.getSelectionModel();
                if (lastTOCnode == null && topNode.getChildCount() > 0) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) topNode.getFirstChild();
                    if (node != null) {
                        tsm.clearSelection();
                        tsm.setSelectionPath(new TreePath(node.getPath()));
                    }
                } else {
                    searchnav.getToolkit().beep();
                }
                searchparams.setCursor(paramCursor);
                tree.setCursor(treeCursor);
            }
        });
        return;
    }

    /**
     * For printf debugging.
     */
    private static boolean debug = false;

    private static void debug(String str) {
        if (debug) {
            System.out.println("BasicSearchNavigatorUI: " + str);
        }
    }
}
