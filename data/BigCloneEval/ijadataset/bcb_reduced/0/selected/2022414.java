package net.ogi.maven.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

/**
 * @author Ognjen Bubalo
 * @version $Id$
 */
@SuppressWarnings("serial")
public class About extends JDialog {

    private final JPanel contentPanel = new JPanel();

    private JButton okButton;

    /**
	 * Create the dialog.
	 */
    public About() {
        setTitle("About Dependency checker");
        setBounds(100, 100, 510, 187);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        final About a = this;
        JLabel lblDependencyChecker = new JLabel("Dependency checker for Java applications");
        lblDependencyChecker.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel lblVersion = new JLabel("Version: 0.9");
        lblVersion.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel lblCreatedByOgnjen = new JLabel("Created by Ognjen Bubalo");
        lblCreatedByOgnjen.setFont(new Font("Arial", Font.PLAIN, 9));
        JLabel lblProjectInformation = new JLabel("Project information:");
        lblProjectInformation.setFont(new Font("Arial", Font.PLAIN, 11));
        String projectURL = "http://code.google.com/p/dependency-checker-for-java-applications/";
        JLabel lblNewLabel = new JLabel("<html><font color=\"#0000CF\"><u>" + projectURL + "</u></font></html>");
        lblNewLabel.setToolTipText("http://code.google.com/p/dependency-checker-for-java-applications/");
        lblNewLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                URI uri;
                try {
                    uri = new URI("http://code.google.com/p/dependency-checker-for-java-applications/");
                    open(uri);
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup().addContainerGap(206, Short.MAX_VALUE).addComponent(lblCreatedByOgnjen)).addGroup(gl_contentPanel.createSequentialGroup().addGap(24).addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addComponent(lblVersion).addComponent(lblDependencyChecker).addGroup(gl_contentPanel.createSequentialGroup().addComponent(lblProjectInformation).addPreferredGap(ComponentPlacement.RELATED).addComponent(lblNewLabel))).addContainerGap(51, Short.MAX_VALUE)));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPanel.createSequentialGroup().addContainerGap().addComponent(lblDependencyChecker).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblVersion).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE).addComponent(lblProjectInformation).addComponent(lblNewLabel)).addPreferredGap(ComponentPlacement.RELATED, 17, Short.MAX_VALUE).addComponent(lblCreatedByOgnjen)));
        contentPanel.setLayout(gl_contentPanel);
        {
            JPanel buttonPane = new JPanel();
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                okButton = new JButton("OK");
                okButton.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent arg0) {
                        a.dispose();
                    }
                });
                okButton.setActionCommand("OK");
                getRootPane().setDefaultButton(okButton);
            }
            GroupLayout gl_buttonPane = new GroupLayout(buttonPane);
            gl_buttonPane.setHorizontalGroup(gl_buttonPane.createParallelGroup(Alignment.LEADING).addGroup(gl_buttonPane.createSequentialGroup().addGap(211).addComponent(okButton).addContainerGap(236, Short.MAX_VALUE)));
            gl_buttonPane.setVerticalGroup(gl_buttonPane.createParallelGroup(Alignment.LEADING).addGroup(gl_buttonPane.createSequentialGroup().addComponent(okButton).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
            buttonPane.setLayout(gl_buttonPane);
        }
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
        }
    }
}
