package kuasar.plugin.netcreator.gui.network;

import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import kuasar.plugin.Intercom.GUI;
import kuasar.plugin.utils.*;

/**
 *
 * @author Jesus Navalon i Pastor <jnavalon at redhermes dot net>
 */
public class pn_SaveNetwork extends kuasar.plugin.classMod.AbstractPanel {

    private JPanel current;

    private int estimatedNodes = -1;

    private String netpath = null;

    private ArrayList<Object[]> ips = null;

    private InetAddress gw = null;

    private ArrayList<InetAddress> dns = null;

    private th_Save save = null;

    /** Creates new form pn_SaveNetwork */
    public pn_SaveNetwork(JPanel current, ArrayList<Object[]> ips, InetAddress gw, ArrayList<InetAddress> dns, String netpath) {
        pn_SaveNetwork(current, ips, gw, dns, netpath, -1);
    }

    public pn_SaveNetwork(JPanel current, ArrayList<Object[]> ips, InetAddress gw, ArrayList<InetAddress> dns, String netpath, int estimatedNodes) {
        pn_SaveNetwork(current, ips, gw, dns, netpath, estimatedNodes);
    }

    private void pn_SaveNetwork(JPanel current, ArrayList<Object[]> ips, InetAddress gw, ArrayList<InetAddress> dns, String netpath, int estimatedNodes) {
        this.current = current;
        this.estimatedNodes = estimatedNodes;
        this.ips = ips;
        this.netpath = netpath;
        this.gw = gw;
        this.dns = dns;
        initComponents();
        pb_progress.setVisible(false);
        lbl_status.setVisible(false);
        pn_Summary.setVisible(false);
        epn_Summary.setContentType("text/rtf");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        bt_Continue = new javax.swing.JButton();
        bt_Cancel = new javax.swing.JButton();
        pn_Container = new javax.swing.JPanel();
        lbl_Info = new javax.swing.JLabel();
        lbl_status = new javax.swing.JLabel();
        pb_progress = new javax.swing.JProgressBar();
        pn_Summary = new javax.swing.JPanel();
        lbl_Summary = new javax.swing.JLabel();
        scp_Summary = new javax.swing.JScrollPane();
        epn_Summary = new javax.swing.JEditorPane();
        bt_Print = new javax.swing.JButton();
        bt_Save = new javax.swing.JButton();
        setOpaque(false);
        bt_Continue.setBackground(new java.awt.Color(0, 0, 0));
        bt_Continue.setForeground(new java.awt.Color(204, 204, 204));
        bt_Continue.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kuasar/plugin/netcreator/icons/save.png")));
        bt_Continue.setText("Save");
        bt_Continue.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_ContinueActionPerformed(evt);
            }
        });
        bt_Cancel.setBackground(new java.awt.Color(0, 0, 0));
        bt_Cancel.setForeground(new java.awt.Color(204, 204, 204));
        bt_Cancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kuasar/plugin/netcreator/icons/cancel.png")));
        bt_Cancel.setText("Back");
        bt_Cancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_CancelActionPerformed(evt);
            }
        });
        pn_Container.setOpaque(false);
        lbl_Info.setForeground(new java.awt.Color(204, 204, 204));
        lbl_Info.setText("<html><body>Congrats, all tests was successfull! <p> Press <i>Continue</i> to set up the network!</body></html> ");
        lbl_status.setForeground(new java.awt.Color(204, 204, 204));
        pb_progress.setValue(50);
        pn_Summary.setOpaque(false);
        lbl_Summary.setForeground(new java.awt.Color(204, 204, 204));
        lbl_Summary.setText("Summary:");
        scp_Summary.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scp_Summary.setViewportView(epn_Summary);
        bt_Print.setForeground(new java.awt.Color(204, 204, 204));
        bt_Print.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kuasar/plugin/netcreator/icons/print.png")));
        bt_Print.setText("Print");
        bt_Print.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        bt_Print.setBorderPainted(false);
        bt_Print.setContentAreaFilled(false);
        bt_Print.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bt_Print.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_PrintActionPerformed(evt);
            }
        });
        bt_Save.setForeground(new java.awt.Color(204, 204, 204));
        bt_Save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kuasar/plugin/netcreator/icons/save.png")));
        bt_Save.setText("Save");
        bt_Save.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        bt_Save.setBorderPainted(false);
        bt_Save.setContentAreaFilled(false);
        bt_Save.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bt_Save.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_SaveActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout pn_SummaryLayout = new javax.swing.GroupLayout(pn_Summary);
        pn_Summary.setLayout(pn_SummaryLayout);
        pn_SummaryLayout.setHorizontalGroup(pn_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(pn_SummaryLayout.createSequentialGroup().addContainerGap().addGroup(pn_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(scp_Summary, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE).addGroup(pn_SummaryLayout.createSequentialGroup().addComponent(lbl_Summary).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 196, Short.MAX_VALUE).addComponent(bt_Print).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(bt_Save))).addContainerGap()));
        pn_SummaryLayout.setVerticalGroup(pn_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(pn_SummaryLayout.createSequentialGroup().addContainerGap().addGroup(pn_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(lbl_Summary).addComponent(bt_Save).addComponent(bt_Print)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(scp_Summary, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)));
        javax.swing.GroupLayout pn_ContainerLayout = new javax.swing.GroupLayout(pn_Container);
        pn_Container.setLayout(pn_ContainerLayout);
        pn_ContainerLayout.setHorizontalGroup(pn_ContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(pn_ContainerLayout.createSequentialGroup().addContainerGap().addGroup(pn_ContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(pn_ContainerLayout.createSequentialGroup().addGroup(pn_ContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(lbl_Info, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE).addComponent(pb_progress, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)).addGap(12, 12, 12)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pn_ContainerLayout.createSequentialGroup().addGroup(pn_ContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(pn_Summary, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(lbl_status, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)).addContainerGap()))));
        pn_ContainerLayout.setVerticalGroup(pn_ContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(pn_ContainerLayout.createSequentialGroup().addContainerGap().addComponent(lbl_Info, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(pb_progress, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(13, 13, 13).addComponent(lbl_status, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(pn_Summary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(229, Short.MAX_VALUE).addComponent(bt_Cancel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(bt_Continue).addContainerGap()).addComponent(pn_Container, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(pn_Container, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(bt_Cancel).addComponent(bt_Continue)).addContainerGap()));
    }

    private void bt_CancelActionPerformed(java.awt.event.ActionEvent evt) {
        if (save != null) if (save.isAlive()) save.cleanStop();
        JPanel parent = (JPanel) this.getParent();
        current.setBounds(this.getBounds());
        parent.removeAll();
        parent.add(current);
        parent.updateUI();
    }

    private void bt_ContinueActionPerformed(java.awt.event.ActionEvent evt) {
        if (estimatedNodes == -1) {
            pb_progress.setIndeterminate(true);
        } else {
            pb_progress.setIndeterminate(false);
            pb_progress.setMinimum(0);
            pb_progress.setMaximum(estimatedNodes);
            pb_progress.setValue(0);
        }
        lbl_Info.setVisible(false);
        pb_progress.setVisible(true);
        lbl_status.setVisible(true);
        pn_Summary.setVisible(true);
        lbl_status.setText("Starting...");
        bt_Continue.setEnabled(false);
        save();
    }

    private void bt_PrintActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            epn_Summary.print();
        } catch (PrinterException ex) {
            pn_Info.Load((JPanel) this.getParent(), this, "Error", "Some was wrong to print<p> Message Exception:<br>" + ex.getLocalizedMessage(), pn_Info.ICON_ERROR);
        }
    }

    private void bt_SaveActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogType(JFileChooser.SAVE_DIALOG);
        jfc.setMultiSelectionEnabled(false);
        if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        StyledDocument doc = (StyledDocument) epn_Summary.getDocument();
        RTFEditorKit kit = new RTFEditorKit();
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(jfc.getSelectedFile().getAbsoluteFile()));
            kit.write(bos, doc, doc.getStartPosition().getOffset(), doc.getLength());
            GUI.launchInfo("Network save correctly ( " + jfc.getSelectedFile().getAbsoluteFile() + " )");
        } catch (FileNotFoundException ex) {
            pn_Info.Load((JPanel) this.getParent(), this, "Error", "Path or File wasn't found to write<p>" + ex.getLocalizedMessage(), pn_Info.ICON_ERROR);
        } catch (IOException ex) {
            pn_Info.Load((JPanel) this.getParent(), this, "Error", "File is read-only or write-protected. Check it and try again<p>" + ex.getLocalizedMessage(), pn_Info.ICON_ERROR);
        } catch (BadLocationException ex) {
            pn_Info.Load((JPanel) this.getParent(), this, "Error", "Data couldn't be saved<p>" + ex.getLocalizedMessage(), pn_Info.ICON_ERROR);
        } finally {
            try {
                if (bos != null) bos.close();
            } catch (IOException ex) {
                pn_Info.Load((JPanel) this.getParent(), this, "Error", "File couldn't be closed!<br>Maybe file won't be accessible while kuasar is opened<p>" + ex.getLocalizedMessage(), pn_Info.ICON_ERROR);
            }
        }
    }

    private javax.swing.JButton bt_Cancel;

    private javax.swing.JButton bt_Continue;

    private javax.swing.JButton bt_Print;

    private javax.swing.JButton bt_Save;

    private javax.swing.JEditorPane epn_Summary;

    private javax.swing.JLabel lbl_Info;

    private javax.swing.JLabel lbl_Summary;

    private javax.swing.JLabel lbl_status;

    private javax.swing.JProgressBar pb_progress;

    private javax.swing.JPanel pn_Container;

    private javax.swing.JPanel pn_Summary;

    private javax.swing.JScrollPane scp_Summary;

    private void save() {
        save = new th_Save(ips, gw, dns, netpath, pb_progress, lbl_status, epn_Summary);
        save.start();
    }
}
