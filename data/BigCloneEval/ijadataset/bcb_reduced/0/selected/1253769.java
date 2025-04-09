package net.sf.dvstar.transmission.dialogs;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.net.URISyntaxException;
import javax.swing.event.HyperlinkEvent;
import net.sf.dvstar.transmission.*;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import javax.swing.event.HyperlinkListener;
import net.sf.dvstar.transmission.protocol.TransmissionManager;
import net.sf.dvstar.transmission.spmeter.SpeedMonitorPanel;
import net.sf.dvstar.transmission.spmeter.SpeedMonitorProviderImpl;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.openide.util.Exceptions;

public class TransmissionAboutBox extends javax.swing.JDialog implements HyperlinkListener {

    private Image titleImage;

    private ResourceMap globalResourceMap;

    public TransmissionAboutBox(TransmissionManager parent) {
        super(parent.getFrame());
        globalResourceMap = Application.getInstance(net.sf.dvstar.transmission.TransmissionApp.class).getContext().getResourceMap(TransmissionView.class);
        titleImage = globalResourceMap.getImageIcon("MainFrame.icon").getImage();
        this.setIconImage(globalResourceMap.getImageIcon("MainFrame.icon").getImage());
        setIconImage(titleImage);
        initComponents();
        epInfo.addHyperlinkListener(this);
        epThanks.addHyperlinkListener(this);
        SpeedMonitorPanel monitor = new SpeedMonitorPanel(new SpeedMonitorProviderImpl());
        monitor.sleepAmount = 100;
        monitor.start();
        plSystem.add(monitor, BorderLayout.CENTER);
        getRootPane().setDefaultButton(closeButton);
        taLicense.setCaretPosition(0);
        epThanks.setCaretPosition(0);
    }

    @Action
    public void closeAboutBox() {
        dispose();
    }

    private void initComponents() {
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel appTitleLabel = new javax.swing.JLabel();
        javax.swing.JLabel versionLabel = new javax.swing.JLabel();
        javax.swing.JLabel appVersionLabel = new javax.swing.JLabel();
        javax.swing.JLabel vendorLabel = new javax.swing.JLabel();
        javax.swing.JLabel appVendorLabel = new javax.swing.JLabel();
        javax.swing.JLabel homepageLabel = new javax.swing.JLabel();
        javax.swing.JLabel imageLabel = new javax.swing.JLabel();
        tfURL = new javax.swing.JTextField();
        epInfo = new javax.swing.JEditorPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lbGPLv3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taLicense = new javax.swing.JTextArea();
        plSystem = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        epThanks = new javax.swing.JEditorPane();
        jPanel4 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(net.sf.dvstar.transmission.TransmissionApp.class).getContext().getResourceMap(TransmissionAboutBox.class);
        setTitle(resourceMap.getString("title"));
        setIconImage(null);
        setModal(true);
        setName("aboutBox");
        setResizable(false);
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(517, 283));
        jTabbedPane1.setName("jTabbedPane1");
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(517, 312));
        jPanel1.setBackground(resourceMap.getColor("jPanel1.background"));
        jPanel1.setMinimumSize(new java.awt.Dimension(512, 256));
        jPanel1.setName("jPanel1");
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(483, 256));
        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD, appTitleLabel.getFont().getSize() + 4));
        appTitleLabel.setText(resourceMap.getString("Application.title"));
        appTitleLabel.setName("appTitleLabel");
        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        versionLabel.setText(resourceMap.getString("versionLabel.text"));
        versionLabel.setName("versionLabel");
        appVersionLabel.setText(resourceMap.getString("Application.version"));
        appVersionLabel.setName("appVersionLabel");
        vendorLabel.setFont(vendorLabel.getFont().deriveFont(vendorLabel.getFont().getStyle() | java.awt.Font.BOLD));
        vendorLabel.setText(resourceMap.getString("vendorLabel.text"));
        vendorLabel.setName("vendorLabel");
        appVendorLabel.setText(resourceMap.getString("Application.vendor"));
        appVendorLabel.setName("appVendorLabel");
        homepageLabel.setFont(homepageLabel.getFont().deriveFont(homepageLabel.getFont().getStyle() | java.awt.Font.BOLD));
        homepageLabel.setText(resourceMap.getString("homepageLabel.text"));
        homepageLabel.setName("homepageLabel");
        imageLabel.setIcon(resourceMap.getIcon("imageLabel.icon"));
        imageLabel.setName("imageLabel");
        tfURL.setEditable(false);
        tfURL.setFont(resourceMap.getFont("tfURL.font"));
        tfURL.setForeground(resourceMap.getColor("tfURL.foreground"));
        tfURL.setText(resourceMap.getString("Application.homepage"));
        tfURL.setToolTipText(resourceMap.getString("tfURL.toolTipText"));
        tfURL.setBorder(null);
        tfURL.setName("tfURL");
        tfURL.setOpaque(false);
        tfURL.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tfURLMouseClicked(evt);
            }
        });
        epInfo.setContentType(resourceMap.getString("epInfo.contentType"));
        epInfo.setEditable(false);
        epInfo.setFont(resourceMap.getFont("epInfo.font"));
        epInfo.setText(resourceMap.getString("epInfo.text"));
        epInfo.setName("epInfo");
        epInfo.setOpaque(false);
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(appTitleLabel).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(vendorLabel).addComponent(homepageLabel).addComponent(versionLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(tfURL, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE).addComponent(appVendorLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE).addComponent(appVersionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))).addComponent(epInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(imageLabel).addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup().addComponent(appTitleLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(epInfo).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(appVersionLabel).addComponent(versionLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(vendorLabel).addComponent(appVendorLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(homepageLabel).addComponent(tfURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))).addContainerGap()));
        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1);
        jPanel2.setName("jPanel2");
        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel5.setName("jPanel5");
        jPanel5.setPreferredSize(new java.awt.Dimension(512, 51));
        lbGPLv3.setIcon(resourceMap.getIcon("lbGPLv3.icon"));
        lbGPLv3.setName("lbGPLv3");
        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel5Layout.createSequentialGroup().addComponent(lbGPLv3).addContainerGap(483, Short.MAX_VALUE)));
        jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel5Layout.createSequentialGroup().addComponent(lbGPLv3).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel2.add(jPanel5, java.awt.BorderLayout.NORTH);
        jScrollPane1.setName("jScrollPane1");
        taLicense.setColumns(20);
        taLicense.setFont(resourceMap.getFont("taLicense.font"));
        taLicense.setRows(5);
        taLicense.setText(resourceMap.getString("taLicense.text"));
        taLicense.setName("taLicense");
        jScrollPane1.setViewportView(taLicense);
        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2);
        plSystem.setName("plSystem");
        plSystem.setLayout(new java.awt.BorderLayout());
        jPanel7.setName("jPanel7");
        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 610, Short.MAX_VALUE));
        jPanel7Layout.setVerticalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100, Short.MAX_VALUE));
        plSystem.add(jPanel7, java.awt.BorderLayout.SOUTH);
        jTabbedPane1.addTab(resourceMap.getString("plSystem.TabConstraints.tabTitle"), plSystem);
        jPanel6.setName("jPanel6");
        jScrollPane2.setBorder(null);
        jScrollPane2.setName("jScrollPane2");
        epThanks.setBorder(null);
        epThanks.setContentType(resourceMap.getString("epThanks.contentType"));
        epThanks.setEditable(false);
        epThanks.setText(resourceMap.getString("epThanks.text"));
        epThanks.setName("epThanks");
        epThanks.setOpaque(false);
        jScrollPane2.setViewportView(epThanks);
        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel6Layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE).addContainerGap()));
        jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel6Layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE).addContainerGap()));
        jTabbedPane1.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6);
        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        jPanel4.setMaximumSize(new java.awt.Dimension(488, 46));
        jPanel4.setName("jPanel4");
        jPanel4.setPreferredSize(new java.awt.Dimension(488, 46));
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(net.sf.dvstar.transmission.TransmissionApp.class).getContext().getActionMap(TransmissionAboutBox.class, this);
        closeButton.setAction(actionMap.get("closeAboutBox"));
        closeButton.setName("closeButton");
        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup().addContainerGap(534, Short.MAX_VALUE).addComponent(closeButton).addContainerGap()));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(closeButton).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        getContentPane().add(jPanel4, java.awt.BorderLayout.SOUTH);
        pack();
    }

    private void tfURLMouseClicked(java.awt.event.MouseEvent evt) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URL(tfURL.getText()).toURI());
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private javax.swing.JButton closeButton;

    private javax.swing.JEditorPane epInfo;

    private javax.swing.JEditorPane epThanks;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JPanel jPanel6;

    private javax.swing.JPanel jPanel7;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JTabbedPane jTabbedPane1;

    private javax.swing.JLabel lbGPLv3;

    private javax.swing.JPanel plSystem;

    private javax.swing.JTextArea taLicense;

    private javax.swing.JTextField tfURL;

    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
        } else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
        } else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(event.getURL().toURI());
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
