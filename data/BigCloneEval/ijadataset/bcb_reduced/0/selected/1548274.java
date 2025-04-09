package ps.client.plugin.eq2.gui.dialog.chat;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URLEncoder;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import ps.client.gui.util.textpanel.LinePart;

public class CharacterLink extends LinePart {

    public static final Font FONT = new Font("Arial", Font.PLAIN, 12);

    static ImageIcon httpIcon;

    Color mouseOverForgroundColor = Color.YELLOW;

    Color forgroundColor = Color.WHITE;

    long lastClicked = 0;

    JLabel label;

    public CharacterLink(String linkText) {
        this(linkText, 0, 0, 0);
    }

    public CharacterLink(String linkText, int leftGap, int rightGap, int vOffset) {
        super(new JLabel(linkText), leftGap, rightGap, vOffset);
        this.leftGap = leftGap;
        this.rightGap = rightGap;
        this.vOffset = vOffset;
        label = (JLabel) comp;
        allowLineBreak = false;
        label.setFont(FONT);
        label.setForeground(forgroundColor);
        label.setIconTextGap(1);
        label.setVerticalAlignment(JLabel.TOP);
        label.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                long delay = System.currentTimeMillis() - lastClicked;
                if (e.getButton() == MouseEvent.BUTTON1 && delay > 1000) {
                    e.consume();
                    lastClicked = System.currentTimeMillis();
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                desktop.browse(new URI("http://everquest2.com/Valor/" + URLEncoder.encode(label.getText(), "UTF-8") + "/"));
                            } catch (Exception ex) {
                                System.err.println(ex.getMessage());
                            }
                        }
                    }
                }
            }

            public void mouseEntered(MouseEvent e) {
                forgroundColor = label.getForeground();
                label.setForeground(mouseOverForgroundColor);
            }

            public void mouseExited(MouseEvent e) {
                label.setForeground(forgroundColor);
            }
        });
    }

    public JLabel getLabel() {
        return label;
    }
}
