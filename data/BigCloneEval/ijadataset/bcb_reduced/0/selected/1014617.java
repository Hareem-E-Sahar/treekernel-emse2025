package org.juicyapps.juicyget.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.juicyapps.app.JuicyUtil;
import org.juicyapps.gui.tabwindow.TabWindowPane;
import org.juicyapps.juicyget.app.JuicyGet;

/**
 * @author hendrik@hhllcks.de
 */
public class JuicyGetTabWindowPane extends TabWindowPane {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8501301478183564854L;

    private JuicyGet juicyGet = null;

    private JButton cmdNewDownload = null;

    private JButton cmdOpenFTP = null;

    private JButton cmdShowDownloads = null;

    private JButton cmdOpenStorageFolder = null;

    private JButton cmdOpenPreferences = null;

    private JButton cmdExitJuicyGet = null;

    private JLabel lblInfo = new JLabel("Version 0.0.0.1");

    public JuicyGetTabWindowPane(JuicyGet jg) {
        juicyGet = jg;
        name = "JuicyGet";
        initLayout();
    }

    /**
	 * 
	 */
    private void initLayout() {
        int y = 150;
        int x = 200;
        Action newDownloadAction = new AbstractAction("New Download") {

            private static final long serialVersionUID = -337215485367444451L;

            @Override
            public void actionPerformed(ActionEvent e) {
                newDownload();
            }
        };
        Action downloadsAction = new AbstractAction("Show Downloads") {

            private static final long serialVersionUID = -1827559483386797438L;

            @Override
            public void actionPerformed(ActionEvent e) {
                showDownloads();
            }
        };
        Action openFtpAction = new AbstractAction("FTP") {

            private static final long serialVersionUID = 2874343949993322090L;

            @Override
            public void actionPerformed(ActionEvent e) {
                openFtp();
            }
        };
        Action openFolderAction = new AbstractAction("Storage Folder") {

            private static final long serialVersionUID = -2432191185182089772L;

            @Override
            public void actionPerformed(ActionEvent e) {
                openStorageFolder();
            }
        };
        Action preferencesAction = new AbstractAction("Preferences") {

            private static final long serialVersionUID = -8146262153816596622L;

            @Override
            public void actionPerformed(ActionEvent e) {
                openPreferences();
            }
        };
        Action exitAction = new AbstractAction("Exit") {

            private static final long serialVersionUID = -570131417936723980L;

            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };
        cmdNewDownload = new JButton(newDownloadAction);
        cmdNewDownload.setPreferredSize(new Dimension(x, y));
        cmdShowDownloads = new JButton(downloadsAction);
        cmdShowDownloads.setPreferredSize(new Dimension(x, y));
        cmdOpenFTP = new JButton(openFtpAction);
        cmdOpenFTP.setPreferredSize(new Dimension(x, y));
        cmdOpenStorageFolder = new JButton(openFolderAction);
        cmdOpenStorageFolder.setPreferredSize(new Dimension(x, y));
        cmdOpenPreferences = new JButton(preferencesAction);
        cmdOpenPreferences.setPreferredSize(new Dimension(x, y));
        cmdExitJuicyGet = new JButton(exitAction);
        cmdExitJuicyGet.setPreferredSize(new Dimension(x, y));
        setLayout(new GridBagLayout());
        JPanel buttonPane = new JPanel(new GridBagLayout());
        buttonPane.setBorder(BorderFactory.createTitledBorder("JuicyGet"));
        JuicyUtil.addComponent(buttonPane, cmdOpenFTP, 0, 1, 1, 1, 0, 0, new Insets(10, 10, 10, 10));
        JuicyUtil.addComponent(buttonPane, cmdNewDownload, 1, 1, 1, 1, 0, 0, new Insets(10, 10, 10, 10));
        JuicyUtil.addComponent(buttonPane, cmdShowDownloads, 2, 1, 1, 1, 0, 0, new Insets(10, 10, 10, 10));
        JuicyUtil.addComponent(buttonPane, cmdOpenStorageFolder, 0, 2, 1, 1, 0, 0, new Insets(10, 10, 10, 10));
        JuicyUtil.addComponent(buttonPane, cmdOpenPreferences, 1, 2, 1, 1, 0, 0, new Insets(10, 10, 10, 10));
        JuicyUtil.addComponent(buttonPane, cmdExitJuicyGet, 2, 2, 1, 1, 0, 0, new Insets(10, 10, 10, 10));
        JuicyUtil.addComponent(this, buttonPane, 0, 0, 1, 1, 0, 0, new Insets(10, 10, 10, 10));
        JuicyUtil.addComponent(this, lblInfo, 0, 1, 1, 1, 0, 0, new Insets(10, 10, 10, 10));
    }

    private void exit() {
        juicyGet.stop();
    }

    private void openPreferences() {
        notifyOpenPreferences();
    }

    private void openStorageFolder() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    desktop.open(new File(juicyGet.getStorageFolder()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openFtp() {
        juicyGet.openFTP();
    }

    private void showDownloads() {
        juicyGet.openDownloads();
    }

    private void newDownload() {
        juicyGet.newDownload();
    }
}
