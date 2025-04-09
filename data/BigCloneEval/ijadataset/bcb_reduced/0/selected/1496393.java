package seventhsense.gui.credits;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.JLabel;
import seventhsense.system.Version;
import java.awt.Insets;
import javax.swing.SwingConstants;

/**
 * Panel for showing developer credits and donate button
 * 
 * @author Parallan, Drag-On
 *
 */
public class CreditsView extends JPanel {

    /**
	 * Default serial version
	 */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(CreditsView.class.getName());

    /**
	 * Constructs the Credits panel
	 */
    public CreditsView() {
        super();
        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
        setLayout(gridBagLayout);
        final JEditorPane editorPaneCredits = new JEditorPane();
        final GridBagConstraints gbc_editorPaneCredits = new GridBagConstraints();
        gbc_editorPaneCredits.insets = new Insets(0, 0, 5, 0);
        gbc_editorPaneCredits.fill = GridBagConstraints.BOTH;
        gbc_editorPaneCredits.gridx = 0;
        gbc_editorPaneCredits.gridy = 0;
        add(editorPaneCredits, gbc_editorPaneCredits);
        editorPaneCredits.setEditable(false);
        editorPaneCredits.setOpaque(false);
        try {
            editorPaneCredits.setPage(CreditsView.class.getResource("/seventhsense/resources/credits.html"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
        editorPaneCredits.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(final HyperlinkEvent event) {
                onCreditsHyperlinkUpdate(event);
            }
        });
        final JLabel labelVersion = new JLabel(Version.getVersion().toString());
        labelVersion.setHorizontalAlignment(SwingConstants.TRAILING);
        final GridBagConstraints gbc_labelVersion = new GridBagConstraints();
        gbc_labelVersion.fill = GridBagConstraints.BOTH;
        gbc_labelVersion.gridx = 0;
        gbc_labelVersion.gridy = 1;
        add(labelVersion, gbc_labelVersion);
    }

    /**
	 * Event.
	 * 
	 * @param event event
	 */
    private void onCreditsHyperlinkUpdate(final HyperlinkEvent event) {
        if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
            if ("http".equalsIgnoreCase(event.getURL().getProtocol()) || "https".equalsIgnoreCase(event.getURL().getProtocol())) {
                if (Desktop.isDesktopSupported()) {
                    final Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(event.getURL().toURI());
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, e.toString(), e);
                        } catch (URISyntaxException e) {
                            LOGGER.log(Level.SEVERE, e.toString(), e);
                        }
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "Desktop not supported");
                }
            } else {
                LOGGER.log(Level.SEVERE, "Unsupported url type!");
            }
        }
    }
}
