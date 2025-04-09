package nl.headspring.photoz.client;

import nl.headspring.photoz.client.events.FolderChangedBusEvent;
import nl.headspring.photoz.common.Configuration;
import nl.headspring.photoz.common.ImageService;
import nl.headspring.photoz.common.eventbus.BusEvent;
import nl.headspring.photoz.common.eventbus.EventBus;
import nl.headspring.photoz.common.eventbus.EventClass;
import nl.headspring.photoz.imagecollection.Annotation;
import nl.headspring.photoz.imagecollection.fs.Folder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import static nl.headspring.photoz.client.AnnotationTree.AnnotationView.VIEW_BY_LOCATION;
import static nl.headspring.photoz.client.AnnotationTree.AnnotationView.VIEW_BY_MARK;
import static nl.headspring.photoz.client.AnnotationTree.AnnotationView.VIEW_BY_TAG;
import static nl.headspring.photoz.client.AnnotationTree.AnnotationView.VIEW_BY_TIME;

/**
 * Class AnnotationTree.
 *
 * @author Eelco Sommer
 * @since Sep 20, 2010
 */
public class AnnotationTree extends JPanel implements TreeSelectionListener {

    public static final EventClass DATE_GROUP_CHANGED_EVENT_CLASS = new EventClass("date.group.changed");

    private static final Log LOG = LogFactory.getLog(AnnotationTree.class);

    enum AnnotationView {

        VIEW_BY_LOCATION, VIEW_BY_TIME, VIEW_BY_TAG, VIEW_BY_MARK
    }

    private final EventBus eventBus;

    private final ImageService imageService;

    private final Configuration configuration;

    private final JTree tree;

    private AnnotationView annotationView = VIEW_BY_LOCATION;

    public AnnotationTree(EventBus eventBus, ImageService imageService, Configuration configuration) {
        super(new BorderLayout());
        this.eventBus = eventBus;
        this.imageService = imageService;
        this.configuration = configuration;
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton(new AbstractAction("Locatie") {

            public void actionPerformed(ActionEvent e) {
                annotationView = VIEW_BY_LOCATION;
                updateTree();
            }
        }));
        buttonPanel.add(new JButton(new AbstractAction("Tijd") {

            public void actionPerformed(ActionEvent e) {
                annotationView = VIEW_BY_TIME;
                updateTree();
            }
        }));
        buttonPanel.add(new JButton(new AbstractAction("Tag") {

            public void actionPerformed(ActionEvent e) {
                annotationView = VIEW_BY_TAG;
                updateTree();
            }
        }));
        buttonPanel.add(new JButton(new AbstractAction("Gemarkeerd") {

            public void actionPerformed(ActionEvent e) {
                annotationView = VIEW_BY_MARK;
                updateTree();
            }
        }));
        add(buttonPanel, BorderLayout.NORTH);
        tree = new JTree();
        tree.setRootVisible(false);
        tree.setModel(createFolderModel());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private TreeModel createFolderModel() {
        List<String> folders = new ArrayList<String>();
        for (Folder folder : imageService.getFolders()) {
            folders.add(folder.getAbsolutePath());
        }
        Collections.sort(folders);
        String commonRoot = getCommonRoot(folders, "");
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Locatie");
        for (String folder : folders) {
            top.add(new DefaultMutableTreeNode(new FolderNode(new Folder(folder), folder.substring(commonRoot.length()))));
        }
        return new DefaultTreeModel(top);
    }

    private String getCommonRoot(List<String> folders, String upRoot) {
        if (folders == null || folders.size() == 0) {
            return upRoot;
        }
        final String firstEntry = folders.get(0);
        int p = firstEntry.indexOf(File.separator);
        if (p >= 0 && hasCommonRoot(folders, firstEntry.substring(0, p + 1))) {
            folders = stripCommonRoot(folders, firstEntry.substring(0, p + 1));
            return upRoot + getCommonRoot(folders, firstEntry.substring(0, p + 1));
        } else {
            return upRoot;
        }
    }

    private List<String> stripCommonRoot(List<String> folders, String commonRoot) {
        List<String> stripped = new ArrayList<String>(folders.size());
        for (String folder : folders) {
            stripped.add(folder.substring(commonRoot.length()));
        }
        return stripped;
    }

    private boolean hasCommonRoot(List<String> folders, String commonRoot) {
        for (String folder : folders) {
            if (!folder.startsWith(commonRoot)) {
                return false;
            }
        }
        return true;
    }

    private TreeModel createTimeModel() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Tijd");
        final List<String> dates = imageService.getDateGroup();
        Collections.sort(dates);
        for (String dateKey : dates) {
            top.add(new DefaultMutableTreeNode(new TimeNode(dateKey)));
        }
        return new DefaultTreeModel(top);
    }

    private TreeModel createTagModel() {
        return new DefaultTreeModel(new DefaultMutableTreeNode("Tag"));
    }

    private TreeModel createMarkModel() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Tijd");
        for (Annotation annotation : imageService.getMarked()) {
            top.add(new DefaultMutableTreeNode(annotation.getName()));
        }
        return new DefaultTreeModel(top);
    }

    private void updateTree() {
        switch(annotationView) {
            case VIEW_BY_LOCATION:
                tree.setModel(createFolderModel());
                break;
            case VIEW_BY_TIME:
                tree.setModel(createTimeModel());
                break;
            case VIEW_BY_TAG:
                tree.setModel(createTagModel());
                break;
            case VIEW_BY_MARK:
                tree.setModel(createMarkModel());
                break;
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;
        Object nodeObject = node.getUserObject();
        if (node.isLeaf()) {
            if (nodeObject instanceof FolderNode) {
                FolderNode folderNode = (FolderNode) nodeObject;
                eventBus.publish(new FolderChangedBusEvent(folderNode.getFolder()));
            } else if (nodeObject instanceof TimeNode) {
                TimeNode timeNode = (TimeNode) nodeObject;
                LOG.debug(timeNode);
                eventBus.publish(new TimeGroupChangedEvent(timeNode.getDateKey()));
            }
        }
    }

    class FolderNode {

        private final Folder folder;

        private final String shortCut;

        FolderNode(Folder folder, String shortCut) {
            this.folder = folder;
            this.shortCut = shortCut;
        }

        public Folder getFolder() {
            return folder;
        }

        public String getShortCut() {
            return shortCut;
        }

        public String toString() {
            return shortCut;
        }
    }

    class TimeNode {

        private final String dateKey;

        TimeNode(String dateKey) {
            this.dateKey = dateKey;
        }

        public String toString() {
            try {
                DateFormat dfIn = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dfIn.parse(dateKey);
                DateFormat dfOut = new SimpleDateFormat("d MMMM yyyy", new Locale("NL", "nl"));
                return dfOut.format(date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public String getDateKey() {
            return dateKey;
        }
    }

    class TimeGroupChangedEvent implements BusEvent {

        private final String dateKey;

        TimeGroupChangedEvent(String dateKey) {
            this.dateKey = dateKey;
        }

        public EventClass getEventClass() {
            return AnnotationTree.DATE_GROUP_CHANGED_EVENT_CLASS;
        }

        public String getDateKey() {
            return dateKey;
        }
    }
}
