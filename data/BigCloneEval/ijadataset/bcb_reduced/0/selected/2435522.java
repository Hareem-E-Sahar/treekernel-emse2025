package net.sf.nxqd.gui;

import javax.swing.*;
import java.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import java.util.zip.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;
import java.net.*;
import net.sf.nxqd.gui.widgets.*;
import net.sf.nxqd.gui.widgets.queryviewer.*;
import net.sf.nxqd.gui.common.JNxqdUtils;
import net.sf.nxqd.event.*;
import net.sf.nxqd.NxqdManager;
import net.sf.nxqd.NxqdContainer;
import net.sf.nxqd.NxqdException;
import net.sf.nxqd.NxqdXMLValue;
import net.sf.nxqd.NxqdBlobValue;

public class JNxqdManager extends JPanel implements ActionListener, ListSelectionListener, NxqdManagerEventListener {

    private static Logger logger = Logger.getLogger(JNxqdManager.class.getName());

    private static final String BLOB_SUFFIX = ".blob";

    private String host, port;

    private JList containerList;

    private JToolBar toolBar;

    private NxqdGui parent;

    private JNxqdContainer container;

    private NxqdManager manager;

    private boolean connected;

    public JNxqdManager(NxqdGui parent) {
        super(new BorderLayout());
        setLayout(new BorderLayout());
        add(getCenterPanel(), BorderLayout.CENTER);
        this.parent = parent;
        connected = false;
    }

    public NxqdManager getManager() {
        return manager;
    }

    public JComponent getCenterPanel() {
        toolBar = new JToolBar("NxqdManager");
        JButton b;
        toolBar.add(b = NxqdGui.makeButton("/toolbarButtonGraphics/general/Refresh16.gif", "refresh", "refresh container list", "refresh"));
        b.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        toolBar.add(b = NxqdGui.makeButton(JNxqdUtils.QUERY_ICON, "query", "query using XQuery or XPath", "query"));
        b.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                query();
            }
        });
        toolBar.add(b = NxqdGui.makeButton(JNxqdUtils.CREATE_CON_ICON, "create", "create container", "create"));
        b.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                create();
            }
        });
        toolBar.add(b = NxqdGui.makeButton("/toolbarButtonGraphics/general/Delete16.gif", "delete", "delete container", "delete"));
        b.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        toolBar.add(b = NxqdGui.makeButton("/toolbarButtonGraphics/general/SaveAs16.gif", "rename", "rename container", "rename"));
        b.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rename();
            }
        });
        toolBar.add(b = NxqdGui.makeButton("/toolbarButtonGraphics/general/Import16.gif", "import", "import database", "import"));
        b.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                importDB();
            }
        });
        toolBar.add(b = NxqdGui.makeButton("/toolbarButtonGraphics/general/Export16.gif", "export", "export database", "export"));
        b.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                exportDB();
            }
        });
        setToolBarState(toolBar, false);
        containerList = new JList(new DefaultListModel());
        containerList.addListSelectionListener(this);
        JLabelListCellRenderer renderer = new JLabelListCellRenderer();
        containerList.setCellRenderer(renderer);
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(toolBar, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(containerList);
        containerPanel.add(scrollPane, BorderLayout.CENTER);
        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, containerPanel, container = new JNxqdContainer());
        return center;
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            container.setContainer(manager, containerList.getSelectedValues());
        }
    }

    public boolean isConnected() {
        if (manager != null) {
            return manager.isConnected();
        } else {
            return false;
        }
    }

    public void connect(String host, String port) throws NxqdException {
        logger.info("connecting");
        this.host = host;
        this.port = port;
        manager = new NxqdManager(host, port);
        manager.connect();
        manager.addNxqdManagerEventListener(this);
        refresh();
        setToolBarState(toolBar, true);
        container.setManager(manager);
    }

    public void disconnect() throws NxqdException {
        logger.info("disconnecting");
        DefaultListModel listm = (DefaultListModel) containerList.getModel();
        listm.removeAllElements();
        setToolBarState(toolBar, false);
        container.disconnect();
        manager.disconnect();
    }

    public static void setToolBarState(JToolBar tb, boolean enabled) {
        Component[] components = tb.getComponents();
        Component curr;
        for (int i = 0; i < tb.getComponentCount(); i++) {
            curr = tb.getComponentAtIndex(i);
            if ((curr instanceof JButton)) {
                ((JButton) curr).setEnabled(enabled);
            }
        }
    }

    public static void setToolBarState(JToolBar tb, boolean enabled, String[] text) {
        Component[] components = tb.getComponents();
        Component curr;
        for (int i = 0; i < tb.getComponentCount(); i++) {
            curr = tb.getComponentAtIndex(i);
            if ((curr instanceof JButton)) {
                for (int j = 0; j < text.length; j++) {
                    if (((JButton) curr).getActionCommand().equals(text[j])) {
                        ((JButton) curr).setEnabled(enabled);
                        break;
                    }
                }
            }
        }
    }

    class RefreshThread implements Runnable {

        private JComponent parent;

        public RefreshThread(JComponent parent) {
            this.parent = parent;
        }

        public void run() {
            try {
                JNxqdManager.setToolBarState(toolBar, false);
                containerList.clearSelection();
                DefaultListModel listm = (DefaultListModel) containerList.getModel();
                ImageIcon icon = new ImageIcon(NxqdGui.getResource(getClass(), JNxqdUtils.CON_ICON));
                java.util.List containers = manager.listContainers();
                Collections.sort(containers);
                JLabel current;
                for (int i = listm.getSize() - 1; i >= 0; i--) {
                    current = (JLabel) listm.get(i);
                    if (!containers.contains(current.getText())) {
                        listm.removeElementAt(i);
                    } else {
                        containers.remove(current.getText());
                    }
                }
                for (int i = 0; i < containers.size(); i++) {
                    listm.add(i, new JLabel(containers.get(i).toString(), icon, SwingConstants.LEFT));
                }
                setToolBarState(toolBar, true);
            } catch (Throwable t) {
                setToolBarState(toolBar, false);
                JOptionPane.showMessageDialog(parent, "Error refreshing view (" + t.getMessage() + ")");
                logger.severe(t.getMessage());
            }
        }
    }

    public void refresh() {
        new Thread(new RefreshThread(this)).start();
    }

    public void rename() {
        try {
            String oldName = ((JLabel) containerList.getSelectedValue()).getText();
            String newContainerName = JOptionPane.showInputDialog("Enter new name for new container " + oldName);
            if (newContainerName != null) {
                manager.renameContainer(oldName, newContainerName);
                refresh();
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this, "Error renaming container (" + t.getMessage() + ")");
            logger.severe(t.getMessage());
        }
    }

    public void create() {
        try {
            String containerName = JOptionPane.showInputDialog("Enter name for new container");
            if (containerName != null) {
                manager.createContainer(containerName);
                refresh();
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this, "Error creating container (" + t.getMessage() + ")");
            logger.severe(t.getMessage());
        }
    }

    public void delete() {
        try {
            Object[] containers = containerList.getSelectedValues();
            containerList.clearSelection();
            container.setContainer(manager, new Object[0]);
            String containerName;
            for (int i = 0; i < containers.length; i++) {
                containerName = ((JLabel) containers[i]).getText();
                manager.deleteContainer(containerName);
            }
            refresh();
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this, "Error deleting container (" + t.getMessage() + ")");
            logger.severe(t.getMessage());
        }
    }

    class ImportThread implements Runnable {

        private JComponent parent;

        public ImportThread(JComponent parent) {
            this.parent = parent;
        }

        public void run() {
            manager.removeNxqdManagerEventListener((JNxqdManager) parent);
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                chooser.setDialogTitle("Import from zip");
                int returnVal = chooser.showOpenDialog(parent);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    ZipInputStream in = new ZipInputStream(new ProgressMonitorInputStream(parent, "Importing from " + chooser.getSelectedFile().getCanonicalPath(), new FileInputStream(file)));
                    ZipEntry entry;
                    while ((entry = in.getNextEntry()) != null) {
                        unzipContainer(entry, in, true);
                    }
                    in.close();
                }
                refresh();
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(parent, "Error adding document (" + t.getMessage() + ")");
                logger.severe(t.getMessage());
            } finally {
                manager.addNxqdManagerEventListener((JNxqdManager) parent);
            }
        }

        public void unzipContainer(ZipEntry entry, ZipInputStream in, boolean overwrite) throws IOException, NxqdException {
            NxqdContainer container;
            if (entry.isDirectory()) {
                String containerName = entry.getName();
                containerName = containerName.substring(0, containerName.length() - 1);
                if (!manager.containerExists(containerName)) {
                    container = manager.createContainer(containerName);
                }
            } else {
                String containerName = entry.getName().substring(0, entry.getName().indexOf('/'));
                String documentId = entry.getName().substring(entry.getName().indexOf('/') + 1, entry.getName().length());
                if (!manager.containerExists(containerName)) {
                    container = manager.createContainer(containerName);
                } else {
                    container = manager.getContainer(containerName);
                }
                ByteArrayOutputStream documentBytes = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    documentBytes.write(buf, 0, len);
                }
                if (documentId.endsWith(BLOB_SUFFIX)) {
                    container.putBlob(documentId.substring(0, documentId.length() - BLOB_SUFFIX.length()), new NxqdBlobValue(documentBytes.toByteArray()));
                } else {
                    if (!entry.isDirectory()) {
                        if (container.documentExists(documentId)) {
                            if (overwrite) {
                                container.deleteDocument(documentId);
                            } else {
                                throw new NxqdException("Document already exists");
                            }
                        }
                        container.putDocument(documentId, new NxqdXMLValue(documentBytes.toString("UTF-8")));
                    }
                }
            }
        }
    }

    public void importDB() {
        new Thread(new ImportThread(this)).start();
    }

    class ExportThread implements Runnable {

        private JComponent parent;

        public ExportThread(JComponent parent) {
            this.parent = parent;
        }

        public void run() {
            Object[] container = containerList.getSelectedValues();
            if (container.length == 0) {
                JOptionPane.showMessageDialog(parent, "no containers are selected", "error", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                chooser.setDialogTitle("Export to zip");
                int returnVal = chooser.showOpenDialog(parent);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (file.exists()) {
                        if (JOptionPane.showConfirmDialog(parent, "File exists - overwrite?", "confirm", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                            return;
                        }
                    }
                    ProgressMonitor progressMonitor = new ProgressMonitor(parent, "Exporting selected containers", "Preparing " + chooser.getSelectedFile().getCanonicalPath(), 0, container.length);
                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
                    String containerName;
                    for (int i = 0; i < container.length; i++) {
                        containerName = ((JLabel) container[i]).getText();
                        progressMonitor.setNote("Exporting " + containerName);
                        progressMonitor.setProgress(i);
                        zipContainer(containerName, out);
                    }
                    out.close();
                    progressMonitor.close();
                }
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(parent, "Error adding document (" + t.getMessage() + ")");
                logger.severe(t.getMessage());
            }
        }

        private void zipContainer(String containerName, ZipOutputStream out) throws NxqdException, IOException {
            NxqdContainer container = manager.getContainer(containerName);
            java.util.List contents = container.listDocuments();
            if (contents.size() == 0) {
                out.putNextEntry(new ZipEntry(containerName + '/'));
                out.closeEntry();
            } else {
                String documentId;
                byte[] buf;
                for (int i = 0; i < contents.size(); i++) {
                    documentId = contents.get(i).toString();
                    out.putNextEntry(new ZipEntry(containerName + '/' + documentId));
                    String document = container.getDocument(documentId).asString();
                    buf = document.getBytes();
                    out.write(buf, 0, buf.length);
                    out.closeEntry();
                }
            }
            contents = container.listBlobs();
            if (contents.size() == 0) {
                out.putNextEntry(new ZipEntry(containerName + '/'));
                out.closeEntry();
            } else {
                String documentId;
                byte[] buf;
                for (int i = 0; i < contents.size(); i++) {
                    documentId = contents.get(i).toString();
                    out.putNextEntry(new ZipEntry(containerName + '/' + documentId + BLOB_SUFFIX));
                    buf = container.getBlob(documentId).getBlob();
                    out.write(buf, 0, buf.length);
                    out.closeEntry();
                }
            }
        }
    }

    public void exportDB() {
        new Thread(new ExportThread(this)).start();
    }

    public void query() {
        QueryViewer queryViewer = new QueryViewer(this);
        if (containerList.getSelectedValue() != null) {
            queryViewer.setQuery("(collection(\"" + ((JLabel) containerList.getSelectedValue()).getText() + "\"))//*");
        }
    }

    public JNxqdContainer getJNxqdContainer() {
        return container;
    }

    public void handleContainerRenamed(java.lang.String oldName, java.lang.String newName) {
        handleContainerDeleted(oldName);
        handleContainerCreated(newName);
    }

    public void handleContainerDeleted(java.lang.String name) {
        JLabel current;
        DefaultListModel listm = (DefaultListModel) containerList.getModel();
        for (int i = listm.getSize() - 1; i >= 0; i--) {
            current = (JLabel) listm.get(i);
            if (current.getText().equals(name)) {
                listm.removeElementAt(i);
                return;
            }
        }
    }

    public void handleContainerCreated(java.lang.String name) {
        handleContainerDeleted(name);
        ImageIcon icon = new ImageIcon(NxqdGui.getResource(getClass(), JNxqdUtils.CON_ICON));
        DefaultListModel listm = (DefaultListModel) containerList.getModel();
        listm.add(0, new JLabel(name, icon, SwingConstants.LEFT));
    }

    public void handleEvent(NxqdEvent event) {
    }
}
