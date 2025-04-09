package com.qbrowser.localstore;

import com.jidesoft.swing.FolderChooser;
import com.qbrowser.QBrowserV2;
import com.qbrowser.icons.QBrowserIconsFactory;
import com.qbrowser.util.QBrowserUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author takemura
 */
public class LocalStoreConfigPanel {

    HashMap config_frames = new HashMap();

    static ResourceBundle resources = QBrowserV2.resources;

    static FolderChooser _folderChooser;

    static File _currentFolder = null;

    static List<String> _recentList = new ArrayList<String>();

    static HashMap parameters = new HashMap();

    QBrowserV2 vqb2;

    public void showCreateLSConfigPanel(QBrowserV2 qb2, LocalStoreManager lsm) {
        vqb2 = qb2;
        JFrame configFrame = new JFrame();
        Dimension d = new Dimension();
        configFrame.setPreferredSize(d);
        configFrame.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png").getImage());
        configFrame.setTitle(resources.getString("qkey.msg.msg297"));
        configFrame.setBackground(Color.white);
        configFrame.getContentPane().setLayout(new BorderLayout());
        JLabel expl = new JLabel(resources.getString("qkey.msg.msg298"));
        if (_folderChooser == null) {
            _folderChooser = new FolderChooser();
            _folderChooser.setAvailableButtons(_folderChooser.getAvailableButtons() & ~FolderChooser.BUTTON_DELETE);
            _folderChooser.setNavigationFieldVisible(true);
        }
        JPanel con_panel = new JPanel();
        GridBagLayout gbag = new GridBagLayout();
        con_panel.setLayout(gbag);
        GridBagConstraints vcs = new GridBagConstraints();
        JTextField local_store_name = new JTextField(20);
        int prefl = 30;
        JTextField local_store_dir_path = new JTextField(prefl);
        d.setSize(300 + (prefl * 10), 200);
        JCheckBox local_store_valid = new JCheckBox();
        local_store_valid.setSelected(true);
        HashMap ps = new HashMap();
        ps.put("local_store_name", local_store_name);
        ps.put("local_store_dir_path", local_store_dir_path);
        ps.put("local_store_valid", local_store_valid);
        String panelid = String.valueOf(System.nanoTime());
        parameters.put(panelid, ps);
        int countY = 0;
        QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resources.getString("qkey.msg.msg287") + "  ", local_store_name, countY++);
        QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resources.getString("qkey.msg.msg288") + "  ", local_store_dir_path, countY++);
        JButton folderc = (JButton) createFolderChooseButton(local_store_dir_path);
        vcs.gridx = 2;
        vcs.gridy = countY - 1;
        vcs.weightx = 1.0;
        vcs.weighty = 1.0;
        vcs.anchor = GridBagConstraints.WEST;
        gbag.setConstraints(folderc, vcs);
        con_panel.add(folderc);
        QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resources.getString("qkey.msg.msg289") + "  ", local_store_valid, countY++);
        configFrame.getContentPane().add(BorderLayout.NORTH, expl);
        configFrame.getContentPane().add(BorderLayout.CENTER, con_panel);
        JButton okbutton1 = new JButton("          OK          ");
        okbutton1.addActionListener(new CreateOKListener(panelid, lsm));
        JButton cancelbutton = new JButton("             " + resources.getString("qkey.msg.msg035") + "          ");
        cancelbutton.addActionListener(new ConfigCancelListener(panelid));
        JPanel pbuttonpanel = new JPanel();
        pbuttonpanel.setLayout(new BorderLayout());
        pbuttonpanel.add(BorderLayout.WEST, okbutton1);
        pbuttonpanel.add(BorderLayout.CENTER, cancelbutton);
        JPanel temppanel = new JPanel();
        temppanel.setLayout(new BorderLayout());
        temppanel.add(BorderLayout.SOUTH, pbuttonpanel);
        configFrame.getContentPane().add(BorderLayout.SOUTH, temppanel);
        configFrame.pack();
        config_frames.put(panelid, configFrame);
        configFrame.setLocationRelativeTo(qb2);
        configFrame.setVisible(true);
    }

    public void showConfigPanel(LocalStoreProperty lsp, LocalStoreManager lsm, QBrowserV2 qb2) {
        vqb2 = qb2;
        if (lsp == null) {
            return;
        }
        String destname = lsp.getDestName();
        if (config_frames.containsKey(destname)) {
            JFrame configFrame = (JFrame) config_frames.get(destname);
            configFrame.setLocationRelativeTo(qb2);
            configFrame.setVisible(true);
        } else {
            JFrame configFrame = new JFrame();
            Dimension d = new Dimension();
            configFrame.setPreferredSize(d);
            configFrame.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png").getImage());
            configFrame.setTitle(resources.getString("qkey.msg.msg286") + " " + destname);
            configFrame.setBackground(Color.white);
            configFrame.getContentPane().setLayout(new BorderLayout());
            JLabel expl = new JLabel(resources.getString("qkey.msg.msg290"));
            if (_folderChooser == null) {
                _folderChooser = new FolderChooser();
                _folderChooser.setAvailableButtons(_folderChooser.getAvailableButtons() & ~FolderChooser.BUTTON_DELETE);
                _folderChooser.setNavigationFieldVisible(true);
            }
            JPanel con_panel = new JPanel();
            GridBagLayout gbag = new GridBagLayout();
            con_panel.setLayout(gbag);
            GridBagConstraints vcs = new GridBagConstraints();
            JTextField local_store_name = new JTextField(20);
            local_store_name.setText(destname);
            int prefl = lsp.getReal_file_directory().length();
            if (prefl > 50) prefl = 50;
            if (prefl < 20) prefl = 20;
            JTextField local_store_dir_path = new JTextField(prefl);
            d.setSize(300 + (prefl * 10), 200);
            local_store_dir_path.setText(lsp.getReal_file_directory());
            JCheckBox local_store_valid = new JCheckBox();
            if (lsp.isValid()) {
                local_store_valid.setSelected(true);
            }
            JCheckBox auto_migration = new JCheckBox();
            auto_migration.setSelected(true);
            HashMap ps = new HashMap();
            ps.put("local_store_name", local_store_name);
            ps.put("local_store_dir_path", local_store_dir_path);
            ps.put("local_store_valid", local_store_valid);
            ps.put("auto_migration", auto_migration);
            parameters.put(destname, ps);
            int countY = 0;
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resources.getString("qkey.msg.msg287") + "  ", local_store_name, countY++);
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resources.getString("qkey.msg.msg288") + "  ", local_store_dir_path, countY++);
            JButton folderc = (JButton) createFolderChooseButton(local_store_dir_path);
            vcs.gridx = 2;
            vcs.gridy = countY - 1;
            vcs.weightx = 1.0;
            vcs.weighty = 1.0;
            vcs.anchor = GridBagConstraints.WEST;
            gbag.setConstraints(folderc, vcs);
            con_panel.add(folderc);
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resources.getString("qkey.msg.msg289") + "  ", local_store_valid, countY++);
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resources.getString("qkey.msg.msg296") + "  ", auto_migration, countY++);
            configFrame.getContentPane().add(BorderLayout.NORTH, expl);
            configFrame.getContentPane().add(BorderLayout.CENTER, con_panel);
            JButton okbutton1 = new JButton("          " + resources.getString("qkey.msg.msg306") + "          ");
            okbutton1.addActionListener(new ConfigOKListener(destname, lsm));
            JButton cancelbutton = new JButton("         " + resources.getString("qkey.msg.msg035") + "             ");
            cancelbutton.addActionListener(new ConfigCancelListener(destname));
            JPanel pbuttonpanel = new JPanel();
            pbuttonpanel.setLayout(new BorderLayout());
            pbuttonpanel.add(BorderLayout.WEST, okbutton1);
            pbuttonpanel.add(BorderLayout.CENTER, cancelbutton);
            JPanel temppanel = new JPanel();
            temppanel.setLayout(new BorderLayout());
            temppanel.add(BorderLayout.SOUTH, pbuttonpanel);
            configFrame.getContentPane().add(BorderLayout.SOUTH, temppanel);
            configFrame.pack();
            config_frames.put(destname, configFrame);
            configFrame.setLocationRelativeTo(qb2);
            configFrame.setVisible(true);
        }
    }

    AbstractButton createFolderChooseButton(final JTextField dirpath) {
        final JButton button = new JButton("...");
        button.setMnemonic('B');
        button.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (dirpath.getText().length() > 0) {
                    _currentFolder = _folderChooser.getFileSystemView().createFileObject(dirpath.getText());
                }
                _folderChooser.setCurrentDirectory(_currentFolder);
                _folderChooser.setRecentList(_recentList);
                _folderChooser.setFileHidingEnabled(true);
                int result = _folderChooser.showOpenDialog(button.getTopLevelAncestor());
                if (result == FolderChooser.APPROVE_OPTION) {
                    _currentFolder = _folderChooser.getSelectedFile();
                    if (_recentList.contains(_currentFolder.toString())) {
                        _recentList.remove(_currentFolder.toString());
                    }
                    _recentList.add(0, _currentFolder.toString());
                    File selectedFile = _folderChooser.getSelectedFile();
                    if (selectedFile != null) {
                        dirpath.setText(selectedFile.toString());
                    } else {
                        dirpath.setText("");
                    }
                }
            }
        });
        button.setRequestFocusEnabled(false);
        button.setFocusable(false);
        return button;
    }

    class ConfigOKListener implements ActionListener {

        String local_dest_name;

        LocalStoreManager lsm;

        public ConfigOKListener(String dest_name, LocalStoreManager vlsm) {
            local_dest_name = dest_name;
            lsm = vlsm;
        }

        public void actionPerformed(ActionEvent e) {
            JFrame frame = (JFrame) config_frames.get(local_dest_name);
            HashMap ps = (HashMap) parameters.get(local_dest_name);
            JTextField local_store_name = (JTextField) ps.get("local_store_name");
            if (local_store_name.getText().length() == 0) {
                QBrowserUtil.popupErrorMessageDialog(new Exception(resources.getString("qkey.msg.msg295")), frame.getRootPane());
                return;
            }
            JTextField local_store_dir_path = (JTextField) ps.get("local_store_dir_path");
            File ifile = new File(local_store_dir_path.getText());
            if (ifile.exists() && ifile.isDirectory()) {
            } else {
                QBrowserUtil.popupErrorMessageDialog(new Exception(resources.getString("qkey.msg.msg294")), frame.getRootPane());
                return;
            }
            JCheckBox local_store_valid = (JCheckBox) ps.get("local_store_valid");
            JCheckBox auto_migration = (JCheckBox) ps.get("auto_migration");
            LocalStoreProperty lsp = lsm.getLocalStoreProperty(local_dest_name);
            File from = new File(lsp.getReal_file_directory());
            if (!ifile.getAbsolutePath().equals(from.getAbsolutePath()) && auto_migration.getSelectedObjects() != null) {
                File[] fromfiles = from.listFiles();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < fromfiles.length; i++) {
                    if (fromfiles[i].isFile() && fromfiles[i].getName().endsWith("Message.zip")) {
                        try {
                            QBrowserUtil.copy(fromfiles[i], new File(ifile.getAbsolutePath() + File.separator + fromfiles[i].getName()));
                        } catch (Throwable thex) {
                            sb.append(thex.getMessage()).append("\n");
                        }
                    }
                }
                if (sb.length() != 0) {
                    QBrowserUtil.popupErrorMessageDialog(new Exception(sb.toString()), frame.getRootPane());
                }
            }
            if (!lsp.getDestName().equals(local_store_name.getText())) {
                vqb2.removeNamedTabbedPane(lsp.getDestNameWithSuffix());
                lsm.removeLocalStoreProperty(lsp);
            }
            lsp.setDestName(local_store_name.getText());
            lsp.setReal_file_directory(ifile.getAbsolutePath());
            if (local_store_valid.getSelectedObjects() != null) {
                lsp.setValid(true);
            } else {
                lsp.setValid(false);
            }
            try {
                lsm.updateAndSaveLocalStoreProperty(lsp);
            } catch (Exception lspe) {
                QBrowserUtil.popupErrorMessageDialog(lspe, frame.getRootPane());
            }
            frame.setVisible(false);
            if (lsp.isValid()) {
                try {
                    vqb2.collectDestination();
                } catch (Exception iie) {
                    iie.printStackTrace();
                }
            }
            vqb2.setMainDestComboBox(lsp.getDestNameWithSuffix());
            vqb2.initLocalStoreManager();
            vqb2.refreshLocalStoreMsgTableWithFileReloading(lsp.getDestNameWithSuffix());
            vqb2.initTreePane();
        }
    }

    class ConfigCancelListener implements ActionListener {

        String local_dest_name;

        public ConfigCancelListener(String dest_name) {
            local_dest_name = dest_name;
        }

        public void actionPerformed(ActionEvent e) {
            JFrame frame = (JFrame) config_frames.get(local_dest_name);
            frame.setVisible(false);
        }
    }

    class CreateOKListener implements ActionListener {

        String panelid;

        LocalStoreManager lsm;

        public CreateOKListener(String panelidv, LocalStoreManager vlsm) {
            panelid = panelidv;
            lsm = vlsm;
        }

        public void actionPerformed(ActionEvent e) {
            JFrame frame = (JFrame) config_frames.get(panelid);
            HashMap ps = (HashMap) parameters.get(panelid);
            frame.setVisible(false);
            JTextField local_store_name = (JTextField) ps.get("local_store_name");
            if (local_store_name.getText().length() == 0) {
                QBrowserUtil.popupErrorMessageDialog(new Exception(resources.getString("qkey.msg.msg295")), frame.getRootPane());
                return;
            }
            JTextField local_store_dir_path = (JTextField) ps.get("local_store_dir_path");
            File ifile = new File(local_store_dir_path.getText());
            if (ifile.exists() && ifile.isDirectory()) {
            } else {
                QBrowserUtil.popupErrorMessageDialog(new Exception(resources.getString("qkey.msg.msg294")), frame.getRootPane());
                return;
            }
            JCheckBox local_store_valid = (JCheckBox) ps.get("local_store_valid");
            LocalStoreProperty lsp = new LocalStoreProperty();
            lsp.setDestName(local_store_name.getText());
            lsp.setReal_file_directory(ifile.getAbsolutePath());
            if (local_store_valid.getSelectedObjects() != null) {
                lsp.setValid(true);
            } else {
                lsp.setValid(false);
            }
            try {
                lsm.addNewLocalStoreProperty(lsp);
            } catch (Exception lspe) {
                QBrowserUtil.popupErrorMessageDialog(lspe, frame.getRootPane());
                return;
            }
            try {
                if (lsp.isValid()) {
                    QBrowserV2.destinationNamesForDisplayQueue = new ArrayList();
                    QBrowserV2.destinationNamesForDisplayTopic = new ArrayList();
                    vqb2.collectDestination();
                }
                vqb2.setMainDestComboBox(lsp.getDestNameWithSuffix());
                vqb2.initLocalStoreManager();
                vqb2.initTreePane();
            } catch (Exception colex) {
                colex.printStackTrace();
            }
        }
    }
}
