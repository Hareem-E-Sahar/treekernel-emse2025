package org.juicyapps.juicynews.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.juicyapps.app.JuicyFolderComparator;
import org.juicyapps.gui.tabwindow.TabWindowPane;
import org.juicyapps.juicynews.app.JuicyNews;
import org.juicyapps.juicynews.rss.RssItem;
import org.juicyapps.persistence.pojo.JuicyFolder;
import org.juicyapps.persistence.pojo.Rssfeed;

public class NewsPane extends TabWindowPane implements TreeSelectionListener, MouseListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = -979508550221205749L;

    private JuicyNews juicyNews = null;

    private JSplitPane contentPane = null;

    private JTree treeFolders = null;

    private DefaultTreeModel treeFoldersModel = null;

    private DefaultMutableTreeNode rootNode = null;

    private JList listNews = null;

    private DefaultListModel listRssModel = new DefaultListModel();

    private DefaultMutableTreeNode activeNode;

    private HashMap<JuicyFolder, DefaultMutableTreeNode> mapFolders = new HashMap<JuicyFolder, DefaultMutableTreeNode>();

    private Rssfeed activeFeed = null;

    private JToolBar toolbar = new JToolBar();

    private JuicyFolder activeFolder;

    private JScrollPane scrollPane;

    private JButton cmdAddFeed = null;

    private JButton cmdEditFeed = null;

    private JButton cmdDeleteFeed = null;

    private JButton cmdAddFolder = null;

    private JButton cmdRenameFolder = null;

    private JButton cmdDeleteFolder = null;

    private JButton cmdClose = null;

    public NewsPane(String applicationName, String newName, JuicyNews juicyNews) {
        this.juicyNews = juicyNews;
        this.name = newName;
        this.appName = applicationName;
        setLayout(new BorderLayout());
        addToolbar();
        initLayout();
    }

    public void initLayout() {
        NewsTreeCellRenderer cellRenderer = new NewsTreeCellRenderer();
        contentPane = new JSplitPane();
        rootNode = new DefaultMutableTreeNode("RSS Feeds");
        treeFoldersModel = new DefaultTreeModel(rootNode);
        treeFolders = new JTree(treeFoldersModel);
        treeFolders.addTreeSelectionListener(this);
        treeFolders.setCellRenderer(cellRenderer);
        treeFolders.setMinimumSize(new Dimension(150, 0));
        listNews = new JList(listRssModel);
        listNews.setCellRenderer(new RssItemListCellRenderer());
        listNews.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listNews.addMouseListener(this);
        listNews.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent lse) {
                if (lse.getValueIsAdjusting() == false) {
                    for (int x = 0; x < listRssModel.size(); x++) {
                        listRssModel.setElementAt(listRssModel.getElementAt(x), x);
                    }
                }
            }
        });
        scrollPane = new JScrollPane(listNews);
        contentPane.setLeftComponent(treeFolders);
        contentPane.setRightComponent(scrollPane);
        treeFolders.expandRow(0);
        add(contentPane, BorderLayout.CENTER);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                fillTree();
            }
        });
    }

    private void fillTree() {
        rootNode.removeAllChildren();
        List<Rssfeed> feeds = juicyNews.getRssFeedsWithoutFolder();
        List<JuicyFolder> listFolder = juicyNews.getRssFolders();
        Collections.sort(listFolder, new JuicyFolderComparator());
        Iterator<JuicyFolder> folderIterator = listFolder.iterator();
        while (folderIterator.hasNext()) {
            JuicyFolder folder = folderIterator.next();
            if (folder.getJfType().equals("r")) {
                DefaultMutableTreeNode newFolderNode = new DefaultMutableTreeNode(folder);
                rootNode.add(newFolderNode);
                mapFolders.put(folder, newFolderNode);
            }
        }
        fillFolders();
        Iterator<Rssfeed> feedIterator = feeds.iterator();
        while (feedIterator.hasNext()) {
            Rssfeed feed = feedIterator.next();
            DefaultMutableTreeNode newFeedNode = new DefaultMutableTreeNode(feed);
            rootNode.add(newFeedNode);
        }
        treeFoldersModel.reload();
    }

    private void fillFolders() {
        Set<Entry<JuicyFolder, DefaultMutableTreeNode>> setFolders = mapFolders.entrySet();
        Iterator<Entry<JuicyFolder, DefaultMutableTreeNode>> folderIterator = setFolders.iterator();
        while (folderIterator.hasNext()) {
            Entry<JuicyFolder, DefaultMutableTreeNode> entry = folderIterator.next();
            Iterator<Rssfeed> feedIterator = entry.getKey().getRssFeeds().iterator();
            while (feedIterator.hasNext()) {
                Rssfeed feed = feedIterator.next();
                DefaultMutableTreeNode feedNode = new DefaultMutableTreeNode(feed);
                entry.getValue().add(feedNode);
            }
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        if (path != null) {
            if (path.getLastPathComponent() != null) {
                activeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (activeNode.getUserObject().getClass() == Rssfeed.class) {
                    cmdAddFeed.setEnabled(true);
                    cmdEditFeed.setEnabled(true);
                    cmdDeleteFeed.setEnabled(true);
                    cmdAddFolder.setEnabled(true);
                    cmdRenameFolder.setEnabled(false);
                    cmdDeleteFolder.setEnabled(false);
                    activeFeed = (Rssfeed) activeNode.getUserObject();
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            displayFeed(activeFeed);
                        }
                    });
                } else if (activeNode.getUserObject().getClass() == JuicyFolder.class) {
                    cmdAddFeed.setEnabled(true);
                    cmdEditFeed.setEnabled(false);
                    cmdDeleteFeed.setEnabled(false);
                    cmdAddFolder.setEnabled(true);
                    cmdRenameFolder.setEnabled(true);
                    cmdDeleteFolder.setEnabled(true);
                    activeFolder = (JuicyFolder) activeNode.getUserObject();
                    displayFeed(null);
                }
            }
        }
    }

    public void displayFeed(Rssfeed feed) {
        if (feed != null) {
            listRssModel.removeAllElements();
            List<RssItem> oldEntries = juicyNews.getOldEntries(feed);
            List<RssItem> newEntries = juicyNews.getNewEntries(feed);
            if (newEntries != null) {
                Iterator<RssItem> newsIterator = newEntries.iterator();
                while (newsIterator.hasNext()) {
                    RssItem news = newsIterator.next();
                    listRssModel.addElement(news);
                }
            } else {
                listRssModel.removeAllElements();
            }
        }
    }

    private void addFeed() {
        Rssfeed feed = juicyNews.addFeed();
        if (feed != null) {
            DefaultMutableTreeNode newFeedNode = new DefaultMutableTreeNode(feed);
            rootNode.add(newFeedNode);
            treeFoldersModel.reload();
        }
    }

    private void editFeed() {
        juicyNews.editFeed(activeFeed);
        treeFoldersModel.nodeChanged(activeNode);
    }

    private void deleteFeed() {
        juicyNews.deleteFeed(activeFeed);
        fillTree();
    }

    private void addFolder() {
        juicyNews.addFolder();
        fillTree();
    }

    private void renameFolder() {
        juicyNews.renameFolder(activeFolder);
        treeFoldersModel.nodeChanged(activeNode);
    }

    private void deleteFolder() {
        juicyNews.deleteFolder(activeFolder);
        rootNode.remove(activeNode);
        fillTree();
    }

    public void addToolbar() {
        Action addFeedAction = new AbstractAction() {

            private static final long serialVersionUID = -4893072667888820544L;

            {
                putValue(Action.LARGE_ICON_KEY, new ImageIcon("img" + File.separator + "close48.png"));
                putValue(Action.SHORT_DESCRIPTION, "Add Feed");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                addFeed();
            }
        };
        Action editFeedAction = new AbstractAction() {

            private static final long serialVersionUID = -4893072667888820544L;

            {
                putValue(Action.LARGE_ICON_KEY, new ImageIcon("img" + File.separator + "close48.png"));
                putValue(Action.SHORT_DESCRIPTION, "Edit Feed");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                editFeed();
            }
        };
        Action deleteFeedAction = new AbstractAction() {

            private static final long serialVersionUID = -4893072667888820544L;

            {
                putValue(Action.LARGE_ICON_KEY, new ImageIcon("img" + File.separator + "close48.png"));
                putValue(Action.SHORT_DESCRIPTION, "Delete Feed");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteFeed();
            }
        };
        Action addFolderAction = new AbstractAction() {

            private static final long serialVersionUID = -4893072667888820544L;

            {
                putValue(Action.LARGE_ICON_KEY, new ImageIcon("img" + File.separator + "newFolder48.png"));
                putValue(Action.SHORT_DESCRIPTION, "Add Folder");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                addFolder();
            }
        };
        Action renameFolderAction = new AbstractAction() {

            private static final long serialVersionUID = -4893072667888820544L;

            {
                putValue(Action.LARGE_ICON_KEY, new ImageIcon("img" + File.separator + "renameFolder48.png"));
                putValue(Action.SHORT_DESCRIPTION, "Rename Folder");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                renameFolder();
            }
        };
        Action deleteFolderAction = new AbstractAction() {

            private static final long serialVersionUID = -4893072667888820544L;

            {
                putValue(Action.LARGE_ICON_KEY, new ImageIcon("img" + File.separator + "deleteFolder48.png"));
                putValue(Action.SHORT_DESCRIPTION, "Delete Folder");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteFolder();
            }
        };
        Action closeAction = new AbstractAction() {

            private static final long serialVersionUID = -4893072667888820544L;

            {
                putValue(Action.LARGE_ICON_KEY, new ImageIcon("img" + File.separator + "close48.png"));
                putValue(Action.SHORT_DESCRIPTION, "Close News Center");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                notifyClose();
            }
        };
        cmdAddFeed = new JButton(addFeedAction);
        cmdEditFeed = new JButton(editFeedAction);
        cmdDeleteFeed = new JButton(deleteFeedAction);
        cmdAddFolder = new JButton(addFolderAction);
        cmdRenameFolder = new JButton(renameFolderAction);
        cmdDeleteFolder = new JButton(deleteFolderAction);
        cmdClose = new JButton(closeAction);
        cmdEditFeed.setEnabled(false);
        cmdDeleteFeed.setEnabled(false);
        cmdRenameFolder.setEnabled(false);
        cmdDeleteFolder.setEnabled(false);
        toolbar.add(cmdAddFeed);
        toolbar.add(cmdEditFeed);
        toolbar.add(cmdDeleteFeed);
        toolbar.add(cmdAddFolder);
        toolbar.add(cmdRenameFolder);
        toolbar.add(cmdDeleteFolder);
        toolbar.add(cmdClose);
        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            RssItem item = (RssItem) listNews.getSelectedValue();
            Desktop desktop;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI(item.getUrl()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }
}
