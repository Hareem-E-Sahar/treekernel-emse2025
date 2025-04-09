package net.sf.fileexchange.ui;

import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;
import static net.sf.fileexchange.ui.EditPortDialog.showEditPortDialog;
import static net.sf.fileexchange.ui.Util.createLabeledPanel;
import static net.sf.fileexchange.ui.Util.createTablePanel;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Desktop.Action;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumnModel;
import net.sf.fileexchange.api.AddressSources;
import net.sf.fileexchange.api.ApplicationConstants;
import net.sf.fileexchange.api.Model;
import net.sf.fileexchange.api.AddressSources.Address;
import net.sf.fileexchange.api.AddressSources.FailedToGetValueOfAddress;
import net.sf.fileexchange.util.http.Server.PortListener;

public class MainFrame {

    private static final Logger LOG = Logger.getLogger(MainFrame.class.getCanonicalName());

    private static final int BYTES_PER_KB = 1000;

    public static JFrame create(Model model) {
        JFrame frame = new JFrame(ApplicationConstants.NAME_AND_VERSION);
        try {
            frame.setIconImage(ImageIO.read(ApplicationConstants.ICON_URL));
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame.setResizable(isResizeAllowed());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new MainFrameCloseListener(frame, model));
        GroupLayout layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);
        JSplitPane splitPane = createSplitPlane(model);
        Component serverPanel = createServerPanel(frame, model);
        ParallelGroup horizontalGroup = layout.createParallelGroup();
        horizontalGroup.addComponent(serverPanel);
        horizontalGroup.addComponent(splitPane);
        layout.setHorizontalGroup(horizontalGroup);
        Group verticalGroup = layout.createSequentialGroup();
        verticalGroup.addComponent(serverPanel);
        verticalGroup.addComponent(splitPane);
        layout.setVerticalGroup(verticalGroup);
        frame.setPreferredSize(new Dimension(900, 550));
        frame.pack();
        return frame;
    }

    private static boolean isResizeAllowed() {
        String resizableProperty = System.getProperty("resizable");
        if (resizableProperty == null) return true; else return Boolean.valueOf(resizableProperty);
    }

    private static final class MainFrameCloseListener extends WindowAdapter {

        private final JFrame frame;

        private final Model model;

        private MainFrameCloseListener(JFrame frame, Model model) {
            this.frame = frame;
            this.model = model;
        }

        @Override
        public void windowClosing(WindowEvent arg0) {
            WaitForServerDialog dialog = new WaitForServerDialog(frame, model);
            dialog.showIfNessary();
            try {
                model.close();
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Exception occured while closing", e);
            }
        }
    }

    private static Component createServerPanel(final JFrame frame, final Model model) {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        JLabel urlLabel = new JLabel("URL:");
        final JLabel urlStart = new JLabel("http://");
        final AddressSources addressSources = model.getAddressSources();
        final JComboBox urlHostComboBox = new JComboBox(createAddressListComboBoxModel(addressSources));
        urlHostComboBox.setEditable(true);
        urlHostComboBox.setSelectedItem(model.getAddress());
        urlHostComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                final Object selected = urlHostComboBox.getSelectedItem();
                final String newValue;
                if (selected instanceof Address) {
                    Address address = (Address) selected;
                    try {
                        newValue = address.getValue();
                    } catch (FailedToGetValueOfAddress e) {
                        urlHostComboBox.setSelectedItem(model.getAddress());
                        final String message = e.getMessage();
                        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else if (selected == RefreshAddressListDummyObject.INSTANCE) {
                    newValue = model.getAddress();
                    urlHostComboBox.setSelectedItem(newValue);
                    urlHostComboBox.setModel(createAddressListComboBoxModel(addressSources));
                } else if (selected == EditAddressListDummyObject.INSTANCE) {
                    newValue = model.getAddress();
                    urlHostComboBox.setSelectedItem(newValue);
                    JDialog dialog = EditAddressListDialog.create(frame, model.getAddressSources());
                    dialog.setVisible(true);
                    urlHostComboBox.setModel(createAddressListComboBoxModel(addressSources));
                } else {
                    newValue = selected.toString();
                }
                model.setAddress(newValue);
                urlHostComboBox.setSelectedItem(newValue);
            }
        });
        final JLabel urlPortLabel = new JLabel(":?");
        final JButton visitUrlButton = new JButton("Visit URL");
        urlPortLabel.addHierarchyListener(new PortURLUpdater(urlPortLabel, model));
        visitUrlButton.setEnabled(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE));
        visitUrlButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                URI uri;
                try {
                    uri = model.getURI();
                } catch (URISyntaxException e1) {
                    final String message = String.format("URL is invalid.");
                    JOptionPane.showConfirmDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (IOException e) {
                    final String message = String.format("Failed to browse %s", uri);
                    JOptionPane.showConfirmDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            ;
        });
        JButton copyUrlButton = new JButton("Copy URL");
        copyUrlButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                URL url;
                try {
                    url = model.getURL();
                } catch (MalformedURLException e) {
                    final String message = e.getMessage();
                    JOptionPane.showConfirmDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                StringSelection selection = new StringSelection(url.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }

            ;
        });
        JButton editPortButton = new JButton("Edit Port");
        editPortButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showEditPortDialog(frame, model);
            }
        });
        JLabel commandsLabel = new JLabel("Commands:");
        JButton startButton = StartButton.create(model);
        JButton stopButton = StopButton.create(model);
        JLabel limitsLabel = new JLabel("Limits:");
        JCheckBox deliverLimitCheckBox = new JCheckBox("Limit deliver speed to", model.isDeliverSpeedLimitEnabled());
        deliverLimitCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                model.setDeliverSpeedLimitEnabled(!model.isDeliverSpeedLimitEnabled());
            }
        });
        final Integer currentDeliverLimit = Integer.valueOf(model.getDeliverSpeedLimitLimit() / BYTES_PER_KB);
        final JSpinner deliverLimitSpinner = new JSpinner(new SpinnerNumberModel(currentDeliverLimit, Integer.valueOf(0), null, Integer.valueOf(5)));
        deliverLimitSpinner.getModel().addChangeListener(new javax.swing.event.ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                final int kB = (Integer) deliverLimitSpinner.getValue();
                model.setDeliverSpeedLimit(kB * BYTES_PER_KB);
            }
        });
        final JLabel lastdeliverLimitLabel = new JLabel(" kB/s");
        JCheckBox receiveLimitCheckBox = new JCheckBox("Limit receive speed to", model.isReceiveSpeedLimitEnabled());
        receiveLimitCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                model.setReceiveSpeedLimitEnabled(!model.isReceiveSpeedLimitEnabled());
            }
        });
        final Integer currentreceiveLimit = Integer.valueOf(model.getReceiveSpeedLimitLimit() / BYTES_PER_KB);
        final JSpinner receiveLimitSpinner = new JSpinner(new SpinnerNumberModel(currentreceiveLimit, Integer.valueOf(0), null, Integer.valueOf(50)));
        receiveLimitSpinner.getModel().addChangeListener(new javax.swing.event.ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                final int kB = (Integer) receiveLimitSpinner.getValue();
                model.setReceiveSpeedLimit(kB * BYTES_PER_KB);
            }
        });
        final JLabel lastReceiveLimitLabel = new JLabel(" kB/s");
        Group horizontalLeftSideGroup = layout.createParallelGroup(Alignment.TRAILING);
        horizontalLeftSideGroup.addComponent(urlLabel);
        horizontalLeftSideGroup.addComponent(commandsLabel);
        horizontalLeftSideGroup.addComponent(limitsLabel);
        SequentialGroup horizontalURLGroup = layout.createSequentialGroup();
        horizontalURLGroup.addComponent(urlStart);
        horizontalURLGroup.addComponent(urlHostComboBox, 50, 300, 300);
        horizontalURLGroup.addComponent(urlPortLabel);
        horizontalURLGroup.addPreferredGap(ComponentPlacement.RELATED);
        horizontalURLGroup.addComponent(editPortButton);
        horizontalURLGroup.addPreferredGap(ComponentPlacement.RELATED);
        horizontalURLGroup.addComponent(visitUrlButton);
        horizontalURLGroup.addPreferredGap(ComponentPlacement.RELATED);
        horizontalURLGroup.addComponent(copyUrlButton);
        SequentialGroup horizontalCommandsGroup = layout.createSequentialGroup();
        horizontalCommandsGroup.addComponent(startButton);
        horizontalCommandsGroup.addPreferredGap(ComponentPlacement.RELATED);
        horizontalCommandsGroup.addComponent(stopButton);
        SequentialGroup horizontalLimitGroup = layout.createSequentialGroup();
        horizontalLimitGroup.addComponent(deliverLimitCheckBox);
        horizontalLimitGroup.addComponent(deliverLimitSpinner, GroupLayout.DEFAULT_SIZE, 60, GroupLayout.PREFERRED_SIZE);
        horizontalLimitGroup.addComponent(lastdeliverLimitLabel);
        horizontalLimitGroup.addPreferredGap(ComponentPlacement.UNRELATED);
        horizontalLimitGroup.addComponent(receiveLimitCheckBox);
        horizontalLimitGroup.addComponent(receiveLimitSpinner, GroupLayout.DEFAULT_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        horizontalLimitGroup.addComponent(lastReceiveLimitLabel);
        Group horizontalRightSideGroup = layout.createParallelGroup(Alignment.LEADING);
        horizontalRightSideGroup.addGroup(horizontalURLGroup);
        horizontalRightSideGroup.addGroup(horizontalCommandsGroup);
        horizontalRightSideGroup.addGroup(horizontalLimitGroup);
        SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.addGroup(horizontalLeftSideGroup);
        horizontalGroup.addPreferredGap(ComponentPlacement.RELATED);
        horizontalGroup.addGroup(horizontalRightSideGroup);
        Group verticalURLGroup = layout.createBaselineGroup(true, false);
        verticalURLGroup.addComponent(urlLabel);
        verticalURLGroup.addComponent(urlStart);
        verticalURLGroup.addComponent(urlHostComboBox);
        verticalURLGroup.addComponent(urlPortLabel);
        verticalURLGroup.addComponent(editPortButton);
        verticalURLGroup.addComponent(visitUrlButton);
        verticalURLGroup.addComponent(copyUrlButton);
        Group verticalCommandsGroup = layout.createBaselineGroup(true, false);
        verticalCommandsGroup.addComponent(commandsLabel);
        verticalCommandsGroup.addComponent(startButton);
        verticalCommandsGroup.addComponent(stopButton);
        Group verticalLimitsGroup = layout.createBaselineGroup(true, false);
        verticalLimitsGroup.addComponent(limitsLabel);
        verticalLimitsGroup.addComponent(deliverLimitCheckBox);
        verticalLimitsGroup.addComponent(deliverLimitSpinner);
        verticalLimitsGroup.addComponent(lastdeliverLimitLabel);
        verticalLimitsGroup.addComponent(receiveLimitCheckBox);
        verticalLimitsGroup.addComponent(receiveLimitSpinner);
        verticalLimitsGroup.addComponent(lastReceiveLimitLabel);
        SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addGroup(verticalURLGroup);
        verticalGroup.addPreferredGap(ComponentPlacement.RELATED);
        verticalGroup.addGroup(verticalCommandsGroup);
        verticalGroup.addPreferredGap(ComponentPlacement.RELATED);
        verticalGroup.addGroup(verticalLimitsGroup);
        layout.setVerticalGroup(verticalGroup);
        layout.setHorizontalGroup(horizontalGroup);
        return createLabeledPanel("Server:", panel);
    }

    private static JSplitPane createSplitPlane(Model model) {
        final Component left = createTreePanel(model);
        final Component right = createRightSide(model);
        final JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(1);
        return splitPane;
    }

    private static JSplitPane createRightSide(Model model) {
        final Component top = createFilesUploadedByOthersTable(model);
        final Component bottom = TransfersPanel.create(model);
        final JSplitPane splitPane = new JSplitPane(VERTICAL_SPLIT, top, bottom);
        splitPane.setResizeWeight(0.5);
        return splitPane;
    }

    private static Component createTreePanel(Model model) {
        final JPanel treePanel;
        final JLabel label;
        final JComboBox comboBox;
        synchronized (model.getResourceTreeLock()) {
            treePanel = TreeComponentFactory.createTreePanel(model.getResourceTreeOwner(), model);
            label = new JLabel("Server File Structure Type:");
            comboBox = TreeComponentComboBox.createForOwner(model.getResourceTreeOwner(), model);
        }
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        final SequentialGroup horizontalHeaderGroup = layout.createSequentialGroup();
        horizontalHeaderGroup.addComponent(label);
        horizontalHeaderGroup.addComponent(comboBox);
        final Group verticalHeaderGroup = layout.createBaselineGroup(true, false);
        verticalHeaderGroup.addComponent(label);
        verticalHeaderGroup.addComponent(comboBox);
        final Group horizontalGroup = layout.createParallelGroup();
        horizontalGroup.addGroup(horizontalHeaderGroup);
        horizontalGroup.addComponent(treePanel);
        final SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addGroup(verticalHeaderGroup);
        verticalGroup.addComponent(treePanel);
        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);
        return new JScrollPane(panel);
    }

    private static Component createFilesUploadedByOthersTable(Model model) {
        final JTable table = new JTable(new FilesUploadedByOthersTableModel(model));
        table.setFillsViewportHeight(true);
        JPopupMenu popupMenu = FilesUploadedByOthersPopupMenu.create(model, table);
        table.setComponentPopupMenu(popupMenu);
        final TableColumnModel columnModel = table.getColumnModel();
        columnModel.removeColumn(columnModel.getColumn(0));
        columnModel.addColumn(FilesUploadedbyOthersColumns.createNameTableColumn());
        columnModel.addColumn(FilesUploadedbyOthersColumns.createSizeTableColumn());
        Component buttons = createFilesUploadedByOthersButtons(model, table, table);
        Component tablePanel = createTablePanel(table, buttons);
        return createLabeledPanel("Files uploaded by others:", tablePanel);
    }

    private static Component createFilesUploadedByOthersButtons(final Model model, JTable table, Component dialogOwner) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("Delete");
        deleteButton.addHierarchyListener(new SelectedRowsEnabler(deleteButton, table.getSelectionModel()));
        deleteButton.addActionListener(new DeleteUploadedFileActionListener(model, table));
        panel.add(deleteButton);
        JButton saveAsButton = new JButton("Save As");
        saveAsButton.addHierarchyListener(new SaveAsEnabler(saveAsButton, table, model.getFilesUploadedByOthers()));
        saveAsButton.addActionListener(new SaveAsActionListenter(model, table));
        panel.add(saveAsButton);
        JButton saveInButton = new JButton("Save In");
        saveInButton.addHierarchyListener(new SelectedRowsEnabler(saveInButton, table.getSelectionModel()));
        saveInButton.addActionListener(new SaveInActionListenter(model, table));
        panel.add(saveInButton);
        return panel;
    }

    private static class PortURLUpdater extends AbstractComponentUpdater<JLabel> {

        private final PortListener portListener;

        private final Model model;

        public PortURLUpdater(JLabel label, Model model) {
            super(label);
            this.model = model;
            this.portListener = new PortListener() {

                @Override
                public void portChanged() {
                    updateComponent();
                }
            };
        }

        @Override
        void registerListener() {
            model.getServer().registerPortListener(portListener);
        }

        @Override
        void unregisterListener() {
            model.getServer().unregisterPortListener(portListener);
        }

        @Override
        protected void updateComponent() {
            component.setText(":" + model.getServer().getPort());
        }
    }

    private static ComboBoxModel createAddressListComboBoxModel(AddressSources addressSources) {
        final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(addressSources.getAddressArray());
        comboBoxModel.addElement(RefreshAddressListDummyObject.INSTANCE);
        comboBoxModel.addElement(EditAddressListDummyObject.INSTANCE);
        return comboBoxModel;
    }

    private static final class RefreshAddressListDummyObject {

        static final RefreshAddressListDummyObject INSTANCE = new RefreshAddressListDummyObject();

        /**
		 * Should only be used for the construction of {@link #INSTANCE}.
		 */
        private RefreshAddressListDummyObject() {
        }

        @Override
        public String toString() {
            return "Refresh List";
        }
    }

    private static final class EditAddressListDummyObject {

        static final EditAddressListDummyObject INSTANCE = new EditAddressListDummyObject();

        /**
		 * Should only be used for the construction of {@link #INSTANCE}.
		 */
        private EditAddressListDummyObject() {
        }

        @Override
        public String toString() {
            return "Edit List";
        }
    }
}
