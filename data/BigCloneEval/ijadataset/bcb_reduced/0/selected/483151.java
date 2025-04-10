package ui;

import core.ChannelSubscription;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelSubscriptionIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.ChannelBuilder;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JOptionPane;

/**
 * @author  jp
 * 
 * Allow user to post to current channel, if writeable
 */
public class PostPanel extends javax.swing.JPanel implements SubscriptionSelectionListenerIF {

    private ChannelSubscriptionIF channelSubscription;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JTextArea txtData;

    private javax.swing.JTextField txtTitle;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JButton txtPost;

    /** Creates new form PostPanel */
    public PostPanel() {
        initComponents();
        this.setEnable(this, false);
    }

    public void subscriptionSelected(ChannelSubscriptionIF channelSubscription) {
        this.channelSubscription = channelSubscription;
        if (channelSubscription == null) {
            this.setEnable(this, false);
            return;
        }
        ChannelIF channel = channelSubscription.getChannel();
        if (channel instanceof Channel) {
            this.setEnable(this, true);
        } else {
            this.setEnable(this, false);
        }
    }

    void setEnable(Container c, boolean enable) {
        Component[] components = c.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
            if (components[i] instanceof Container) {
                setEnable((Container) components[i], enable);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jScrollPane1 = new javax.swing.JScrollPane();
        txtData = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtTitle = new javax.swing.JTextField();
        txtPost = new javax.swing.JButton();
        setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(txtData);
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jPanel1.setLayout(new java.awt.BorderLayout());
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Title");
        jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);
        txtTitle.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jPanel1.add(txtTitle, java.awt.BorderLayout.CENTER);
        txtPost.setText("Post");
        txtPost.setToolTipText("Post the current item to the channel");
        txtPost.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPostActionPerformed(evt);
            }
        });
        jPanel1.add(txtPost, java.awt.BorderLayout.EAST);
        add(jPanel1, java.awt.BorderLayout.NORTH);
    }

    private void txtPostActionPerformed(java.awt.event.ActionEvent evt) {
        ChannelBuilder builder = new ChannelBuilder();
        ChannelIF channelIF = this.channelSubscription.getChannel();
        ItemIF itemIF = builder.makeItem(this.txtTitle.getText(), this.txtData.getText(), channelIF.getLocation());
        channelIF.addItem(itemIF);
        if (this.channelSubscription instanceof ChannelSubscription) {
            ChannelSubscription cs = (ChannelSubscription) this.channelSubscription;
            try {
                cs.store();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error storing channel: " + e.getMessage(), "Channel Update Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        this.txtData.setText("");
        this.txtTitle.setText("");
    }
}
