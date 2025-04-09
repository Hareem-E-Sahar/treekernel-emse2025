import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static java.lang.Character.toLowerCase;

/**
 * Created by IntelliJ IDEA.
 * User: Алексей
 * Date: 13.07.11
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public class AniPlayer {

    private JPanel AniPlayerWindow;

    private JFileChooser fc;

    private CBrowseWindow BrowseWindow;

    public AniPlayer() {
        AniPlayerWindow.setFocusable(true);
        AniPlayerWindow.setPreferredSize(new Dimension(640, 480));
        BrowseWindow = new CBrowseWindow();
        BrowseWindow.setLocation(0, 0);
        BrowseWindow.setPreferredSize(new Dimension(640, 480));
        AniPlayerWindow.add(BrowseWindow);
        fc = new JFileChooser();
        AniPlayerWindow.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if (toLowerCase(e.getKeyChar()) == 'o') {
                    int returnVal = fc.showOpenDialog(AniPlayerWindow);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        BufferedImage img = null;
                        try {
                            img = ImageIO.read(fc.getSelectedFile());
                        } catch (IOException ie) {
                        }
                        BrowseWindow.setImg(img);
                        AniPlayerWindow.repaint();
                    }
                }
            }
        });
        AniPlayerWindow.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                BrowseWindow.setLocation(0, 0);
                BrowseWindow.setSize(AniPlayerWindow.getSize());
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("AniPlayer");
        frame.setContentPane(new AniPlayer().AniPlayerWindow);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
