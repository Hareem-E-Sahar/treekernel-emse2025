package alx.library;

import javax.swing.JPanel;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.html.HTMLEditorKit;
import alx.library.actions.CloseDialogAction;

public class HtmlDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(HtmlDialog.class.getName());

    private static final int DEFAULT_HEIGHT = 600;

    private static final int DEFAULT_WIDTH = 600;

    private JPanel jContentPane = null;

    private JScrollPane jScrollPane = null;

    private JEditorPane jTextPane = null;

    private String sourceFile;

    private boolean storePosition = false;

    public HtmlDialog(Frame owner, String title, String sourceFile, boolean storePosition) {
        this(owner, title, sourceFile, storePosition, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public HtmlDialog(Frame owner, String title, String sourceFile, int width, int height) {
        this(owner, title, sourceFile, false, width, height);
    }

    public HtmlDialog(Frame owner, String title, String _sourceFile, boolean _storePosition, int width, int height) {
        super(owner);
        this.sourceFile = _sourceFile;
        this.storePosition = _storePosition;
        initialize(title, width, height);
    }

    /**
	 * This method initializes this
	 * @param height 
	 * @param width 
	 * 
	 * @return void
	 */
    private void initialize(String title, int width, int height) {
        this.setSize(width, height);
        this.setTitle(title);
        this.setName(title);
        this.setContentPane(getJContentPane());
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        if (storePosition) GUIUtils.restorePosition(this);
        setupActions();
    }

    /**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextPane());
        }
        return jScrollPane;
    }

    /**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
    private JEditorPane getJTextPane() {
        if (jTextPane == null) {
            jTextPane = new JEditorPane();
            jTextPane.setEditable(false);
            jTextPane.setEditorKit(new HTMLEditorKit());
            loadTextIntoTextPane();
            jTextPane.addHyperlinkListener(new HyperlinkListener() {

                @Override
                public void hyperlinkUpdate(HyperlinkEvent evt) {
                    if (evt.getEventType() == EventType.ACTIVATED) {
                        try {
                            if (Desktop.isDesktopSupported()) {
                                Desktop desktop = Desktop.getDesktop();
                                if (evt.getURL() != null) {
                                    desktop.browse(new URI(evt.getURL().toString()));
                                } else {
                                }
                            }
                        } catch (Exception e) {
                            log.log(Level.WARNING, "Error", e);
                        }
                    }
                }
            });
        }
        return jTextPane;
    }

    private void loadTextIntoTextPane() {
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            URL u = cl.getResource(sourceFile);
            if (u != null) {
                jTextPane.read(u.openStream(), null);
            } else {
                String msg = "Resource " + sourceFile + " not found";
                log.warning(msg);
                jTextPane.setText(msg);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error", e);
        }
    }

    private void setupActions() {
        jContentPane.getActionMap().put("CLOSE", new CloseDialogAction(this));
        jContentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CLOSE");
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    @Override
    public void dispose() {
        if (storePosition) GUIUtils.savePosition(this);
        super.dispose();
    }
}
