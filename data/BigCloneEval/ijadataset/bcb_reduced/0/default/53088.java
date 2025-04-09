import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class MessageDialog {

    public static void showMessageDialog(String title, Object message, int messageType, int optionType) {
        JDialog messageDialog = new JOptionPane(message, messageType, optionType).createDialog(title);
        messageDialog.setIconImage(TrayIcon.getApplicationIconImage());
        messageDialog.setLocationRelativeTo(null);
        messageDialog.setVisible(true);
        messageDialog.dispose();
    }

    /**
	 * Creates a "label" written in HTML, which may contain clickable links.
	 * 
	 * @param htmlMessage The message in HTML.
	 * 
	 * @return A "label" which can be passed in to a message box.
	 */
    public static JEditorPane createURLLabel(String htmlMessage) {
        Font font = UIManager.getFont("Label.font");
        String rgb = Integer.toHexString(new JPanel().getBackground().getRGB());
        rgb = rgb.substring(2, rgb.length());
        String bodyRule = "body { background: #" + rgb + "; font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }";
        JEditorPane jEditorPane = new JEditorPane(new HTMLEditorKit().getContentType(), htmlMessage);
        ((HTMLDocument) jEditorPane.getDocument()).getStyleSheet().addRule(bodyRule);
        jEditorPane.setEditable(false);
        jEditorPane.setBorder(null);
        jEditorPane.setOpaque(false);
        jEditorPane.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    HyperlinkEvent.EventType eventType = hyperlinkEvent.getEventType();
                    if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
                        } catch (URISyntaxException uriSyntaxException) {
                            showMessageDialog(null, new MessageFormat(Messages.getString("MessageDialog.6")).format(new Object[] { hyperlinkEvent.getURL() }), JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
                        } catch (IOException ioException) {
                            showMessageDialog(null, Messages.getString("MessageDialog.8"), JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
                        }
                    }
                } else {
                    showMessageDialog(null, Messages.getString("MessageDialog.7"), JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
                }
            }
        });
        return jEditorPane;
    }
}
