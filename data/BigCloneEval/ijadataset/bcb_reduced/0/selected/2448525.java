package view.help;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import loading.LoadingFiles;
import view.abstractClass.AbstractJDialog;
import controller.Controller;

@SuppressWarnings("serial")
public class About extends AbstractJDialog implements ActionListener {

    private JLabel labelAuthors, labelDescription, labelName, labelImage, labelSite;

    private JPanel panelImage, panelText, panelAuthors, panelDescription, panelName, panelButtons, panelClose, panelCredit, panelLicence, panelSite;

    private JPanel container = new JPanel();

    private JButton buttonClose, buttonCredits, buttonLicence;

    private Controller controller;

    public About(Controller controller) {
        super();
        this.controller = controller;
        this.setSize(350, 300);
        this.setLocationRelativeTo(null);
        this.setTitle("À propos");
        this.initComponents();
        this.setContentPane(container);
        this.changeLookAndFeel(controller.getModel().getModelPrefProperties().getPref()[5]);
        this.pack();
        this.setVisible(true);
    }

    public void initComponents() {
        labelAuthors = new JLabel("Copyright (c) 2010, 2011 Jérémy Chevrier");
        labelDescription = new JLabel("Un logiciel de suivi de cohortes");
        labelName = new JLabel("JCohorte  0.3.3");
        labelImage = new JLabel();
        labelImage.setIcon(new ImageIcon(LoadingFiles.getAbout()));
        labelName.setFont(new Font("Ubuntu", Font.BOLD, 30));
        labelDescription.setFont(new Font("Ubuntu", Font.BOLD, 16));
        labelSite = new JLabel("<html><u><font color='blue'>Site de JCohorte</font></u></html>");
        labelSite.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                labelSite.setBackground(Color.BLUE);
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URI("http://sourceforge.net/projects/jcohorte/"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        labelSite.setToolTipText("http://sourceforge.net/projects/jcohorte/");
        buttonClose = new JButton("Fermer");
        buttonLicence = new JButton("Licence");
        buttonCredits = new JButton("Crédits");
        buttonClose.addActionListener(this);
        buttonLicence.addActionListener(this);
        buttonCredits.addActionListener(this);
        panelText = new JPanel();
        panelImage = new JPanel();
        panelAuthors = new JPanel();
        panelDescription = new JPanel();
        panelName = new JPanel();
        panelButtons = new JPanel();
        panelClose = new JPanel();
        panelCredit = new JPanel();
        panelLicence = new JPanel();
        panelSite = new JPanel();
        panelName.add(labelName);
        panelDescription.add(labelDescription);
        panelAuthors.add(labelAuthors);
        panelSite.add(labelSite);
        panelText.setLayout(new BoxLayout(panelText, BoxLayout.PAGE_AXIS));
        panelText.add(panelName);
        panelText.add(panelDescription);
        panelText.add(panelAuthors);
        panelText.add(panelSite);
        panelClose.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelClose.add(buttonClose);
        panelCredit.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelCredit.add(buttonCredits);
        panelLicence.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelLicence.add(buttonLicence);
        panelButtons.setLayout(new GridLayout(1, 3));
        panelButtons.add(panelCredit);
        panelButtons.add(panelLicence);
        panelButtons.add(panelClose);
        panelImage.add(labelImage);
        container.setLayout(new BorderLayout());
        container.add(panelImage, BorderLayout.NORTH);
        container.add(panelText, BorderLayout.CENTER);
        container.add(panelButtons, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Object object = arg0.getSource();
        if (object == buttonClose) this.dispose();
        if (object == buttonCredits) new Credits(controller);
        if (object == buttonLicence) new Licence(controller);
    }
}
