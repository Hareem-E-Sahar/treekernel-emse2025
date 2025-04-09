package baus.gui.simulation;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Die Meldungsleiste enthält zusätzliche GUI-Elemente wie eine Liste von
 * Textmeldungen, eine Stoppuhr und Bedienelemente.
 * @author das BAUS! team
 */
@SuppressWarnings("serial")
public class MeldungsLeiste extends JPanel {

    private final JPanel westPanel = new JPanel(new GridLayout(3, 1));

    private final JButton hilfeButton;

    private final JButton auftragButton;

    private final JTextArea meldungen;

    /**
     * Erzeugt eine neue Meldungsleiste mit Uhr, Buttons und Meldungsausgabe.
     */
    public MeldungsLeiste() {
        setLayout(new BorderLayout(5, 5));
        hilfeButton = new JButton("Hilfe");
        hilfeButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        java.awt.Desktop.getDesktop().open(new File("resources/hilfe/index.html"));
                    } catch (final IOException ex) {
                        System.err.println("Die Hilfeseite von BAUS! konnte nicht gefunden werden");
                    }
                }
            }
        });
        westPanel.add(hilfeButton);
        auftragButton = new JButton("Auftrag");
        auftragButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    try {
                        java.awt.Desktop.getDesktop().open(new File("auftraege/test.pdf"));
                    } catch (final IOException ex) {
                        System.err.println("Der Auftrag konnte nicht angezeigt werden.");
                    }
                }
            }
        });
        westPanel.add(auftragButton);
        westPanel.add(new Uhr());
        add(westPanel, BorderLayout.WEST);
        meldungen = new JTextArea(4, 40);
        meldungen.setEditable(false);
        add(new JScrollPane(meldungen), BorderLayout.CENTER);
    }

    /**
     * Gibt eine weitere Meldung im Log aus.
     * @param meldung Die Meldung die ausgegeben werden soll
     */
    public final void addMeldung(final String meldung) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                meldungen.setText(meldungen.getText() + meldung);
            }
        });
    }
}
