package org.mooym.incident;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * This class represents an occurring error to the user, so he can take actions himself.
 * 
 * @author roesslerj
 * @since 0.2
 */
public class ErrorDialog extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Point VIEWPORT_START = new Point(0, 0);

    private boolean isDetailsVisible = true;

    private static final FeedbackManager feedback = FeedbackManagerFactory.getInstance(ErrorDialog.class);

    /** Creates a new ErrorDialog. */
    public ErrorDialog(Component mainFrame, PermanentIncident incident) {
        jTextAreaDetails.setText(incident.toXML());
        jTextFieldTopic.setText(incident.toString());
        initComponents();
        initBehaviour(mainFrame);
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jLabelText.setText("<html>Mooym encountered a problem that will result in loss of some functionality. Since it was not handled appropriately, it is most propably a bug.<br /> [Show only if no autoupdate: Autoupdate disabled. It is recommended that you download and install the latest version of this program to solve this problem.] [Show only if autosubmitBug: Mooym will try to submit the bug if an Internet connection is established. If this does not work for some reasons or if you want to do it manually (e.g. to get direct feedback), please use the following information:] [Show only if no autosubmitBug: Automatically submitting bugs is disabled. To get assistance and feedback, please report the problem at <a href=\"http://mooym.uservoice.com\">http://mooym.uservoice.com</a>.] <br /><br />  Sorry for the inconvenience.</html>");
        jLabelText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButtonDetails.setText("Details");
        jButtonOK.setText("OK");
        jTextAreaDetails.setColumns(20);
        jTextAreaDetails.setEditable(false);
        jTextAreaDetails.setRows(5);
        jScrollPaneDetails.setViewportView(jTextAreaDetails);
        jLabelTopic.setText("Topic:");
        javax.swing.GroupLayout jPanelDetailsLayout = new javax.swing.GroupLayout(jPanelDetails);
        jPanelDetails.setLayout(jPanelDetailsLayout);
        jPanelDetailsLayout.setHorizontalGroup(jPanelDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 485, Short.MAX_VALUE).addGroup(jPanelDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanelDetailsLayout.createSequentialGroup().addContainerGap().addGroup(jPanelDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPaneDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE).addGroup(jPanelDetailsLayout.createSequentialGroup().addComponent(jLabelTopic, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jTextFieldTopic, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))).addContainerGap())));
        jPanelDetailsLayout.setVerticalGroup(jPanelDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 134, Short.MAX_VALUE).addGroup(jPanelDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanelDetailsLayout.createSequentialGroup().addContainerGap().addGroup(jPanelDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelTopic)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPaneDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE).addContainerGap())));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabelText, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jButtonDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jButtonOK, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanelDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabelText, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jButtonOK).addComponent(jButtonDetails)).addContainerGap(145, Short.MAX_VALUE)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGap(161, 161, 161).addComponent(jPanelDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGap(0, 0, 0))));
    }

    protected final javax.swing.JButton jButtonDetails = new javax.swing.JButton();

    protected final javax.swing.JButton jButtonOK = new javax.swing.JButton();

    protected final javax.swing.JLabel jLabelText = new javax.swing.JLabel();

    protected final javax.swing.JLabel jLabelTopic = new javax.swing.JLabel();

    protected final javax.swing.JPanel jPanelDetails = new javax.swing.JPanel();

    protected final javax.swing.JScrollPane jScrollPaneDetails = new javax.swing.JScrollPane();

    protected final javax.swing.JTextArea jTextAreaDetails = new javax.swing.JTextArea();

    protected final javax.swing.JTextField jTextFieldTopic = new javax.swing.JTextField();

    /**
   * Init the behavior of the dialog. 
   */
    private void initBehaviour(Component mainFrame) {
        setTitle("An error occurred.");
        jButtonOK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        jButtonDetails.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDetailsVisible();
            }
        });
        jLabelText.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                try {
                    open(new URI("http://mooym.uservoice.com"));
                } catch (URISyntaxException exc) {
                    feedback.registerBug("The given static URI contained a syntax error!", exc);
                }
            }
        });
        pack();
        toggleDetailsVisible();
        SwingUtilities.updateComponentTreeUI(this);
        setResizable(true);
        setLocationRelativeTo(null);
        setVisible(true);
        jScrollPaneDetails.getViewport().setViewPosition(VIEWPORT_START);
    }

    /**
   * Toggles the visibility of the details part.
   */
    private void toggleDetailsVisible() {
        isDetailsVisible = !isDetailsVisible;
        jPanelDetails.setVisible(isDetailsVisible);
        if (isDetailsVisible) {
            setSize(getWidth(), (int) (getHeight() + jPanelDetails.getPreferredSize().getHeight()));
        } else {
            if (jPanelDetails.getHeight() != 0) {
                Dimension preferredSize = new Dimension();
                preferredSize.setSize(jPanelDetails.getPreferredSize().getWidth(), jPanelDetails.getHeight());
                jPanelDetails.setPreferredSize(preferredSize);
                setSize(getWidth(), getHeight() - jPanelDetails.getHeight());
            }
        }
    }

    /**
   * Open the given URI in the native web browser.
   * 
   * @param uri The URI to open.
   */
    private void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException exc) {
                feedback.registerBug("IOException occurred opening a link in the browser.", exc);
            }
        } else {
            feedback.registerBug("Desktop is not supported, cannot open browser to show link!");
        }
    }
}
