package blueprint4j.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalLookAndFeel;
import blueprint4j.db.Entity;
import blueprint4j.gui.db.DataPanelEntity;
import blueprint4j.utils.BindException;
import blueprint4j.utils.Log;
import blueprint4j.utils.Settings;
import blueprint4j.utils.SettingsStorePropertiesFile;

public class Gui {

    private static HashMap cached_icons = new HashMap();

    public static final String ICON_BASE = "24x24/";

    private static Color row_normal_color = null;

    private static Color row_highlighter_color = new Color(243, 243, 254);

    private static Color row_selected_color = new Color(204, 204, 255);

    private static Color row_normal_color_forground = null;

    private static Color row_selected_color_forground = new Color(204, 204, 255);

    private static Font button_font = new Font("Lucida Sans Unicode", Font.PLAIN, 12);

    private static void init() {
        row_normal_color = UIManager.getColor("Table.background");
        int color = row_normal_color.getRed() + row_normal_color.getGreen() + row_normal_color.getBlue();
        row_highlighter_color = row_normal_color;
        row_selected_color = UIManager.getColor("Table.selectionBackground");
        row_normal_color_forground = UIManager.getColor("Table.foreground");
        row_selected_color_forground = UIManager.getColor("Table.selectionForeground");
        if (color / 3 > 128) {
            while (Math.abs(color - (row_highlighter_color.getRed() + row_highlighter_color.getGreen() + row_highlighter_color.getBlue())) < 64) {
                row_highlighter_color = row_highlighter_color.darker();
            }
        } else {
            while (Math.abs(color - (row_highlighter_color.getRed() + row_highlighter_color.getGreen() + row_highlighter_color.getBlue())) < 64) {
                row_highlighter_color = row_highlighter_color.brighter();
            }
        }
    }

    public static Color getRowSelectedColor() {
        if (row_normal_color == null) {
            init();
        }
        return row_selected_color;
    }

    public static Color getRowSelectedColorForground() {
        if (row_normal_color == null) {
            init();
        }
        return row_selected_color_forground;
    }

    public static Color getRowHighlighterColor() {
        if (row_normal_color == null) {
            init();
        }
        return row_highlighter_color;
    }

    public static Color getRowNormalColor() {
        if (row_normal_color == null) {
            init();
        }
        return row_normal_color;
    }

    public static Color getRowNormalColorForground() {
        if (row_normal_color == null) {
            init();
        }
        return row_normal_color_forground;
    }

    public static void captureScreen(Component component, File file) throws IOException, AWTException {
        int width = component.getWidth();
        int height = component.getHeight();
        int x = component.getX();
        int y = component.getY();
        for (component = component.getParent(); component != null; component = component.getParent()) {
            x += component.getX();
            y += component.getY();
        }
        BufferedImage screencapture = new Robot().createScreenCapture(new Rectangle(x, y, width, height));
        ImageIO.write(screencapture, "jpg", file);
    }

    public static Image loadImageFromResource(String resource_name) throws IOException {
        ImageIcon ii = loadIconsFromResource(resource_name);
        return ii != null ? ii.getImage() : null;
    }

    public static ImageIcon loadIconsFromResource(String resource_name) throws IOException {
        return loadIconsFromResource(new String[] { resource_name })[0];
    }

    public static InputStream getInputStreamForResource(String resource) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (is == null) {
            System.out.println("Exception: ???  Mostly File not found,   returning: 'null'");
            return null;
        }
        return is;
    }

    public static ImageIcon[] loadIconsFromResource(String[] resource_name) throws IOException {
        if (!cached_icons.containsKey(resource_name)) {
            ImageIcon[] loaded = new ImageIcon[resource_name.length];
            int width = 0, height = 0;
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            for (int t = 0; t < resource_name.length; t++) {
                URL url = Thread.currentThread().getContextClassLoader().getResource(resource_name[t]);
                if (url == null) {
                    throw new IOException("Can not find resource [" + resource_name[t] + "]");
                }
                loaded[t] = new ImageIcon(toolkit.createImage(url));
                if (loaded[t].getIconWidth() > width) {
                    width = loaded[t].getIconWidth();
                }
                if (loaded[t].getIconHeight() > height) {
                    height = loaded[t].getIconHeight();
                }
            }
            BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = buffered.createGraphics();
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, width, height);
            ImageIcon[] images = new ImageIcon[loaded.length];
            for (int t = 0; t < loaded.length; t++) {
                g.drawImage(loaded[t].getImage(), 0, 0, null);
                BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                frame.createGraphics().drawImage(buffered.getSubimage(0, 0, width, height), 0, 0, null);
                images[t] = new ImageIcon(frame);
            }
            cached_icons.put(resource_name, images);
        }
        return (ImageIcon[]) cached_icons.get(resource_name);
    }

    public static void addEnter4FocusShift(Component component) {
        HashSet key_set = new HashSet();
        key_set.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        key_set.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        key_set.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK));
        component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, key_set);
        key_set = new HashSet();
        key_set.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK));
        key_set.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK));
        key_set.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, key_set);
    }

    public static Image rotateLeft(Image source) {
        if (source != null) {
            BufferedImage buffer = new BufferedImage(source.getHeight(null), source.getWidth(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) buffer.getGraphics();
            AffineTransform trans = AffineTransform.getRotateInstance(-Math.PI / 2);
            g.setTransform(trans);
            g.drawImage(source, -source.getWidth(null), 0, null);
            g.dispose();
            return buffer;
        }
        return null;
    }

    public static Image rotateRight(Image source) {
        if (source != null) {
            BufferedImage buffer = new BufferedImage(source.getHeight(null), source.getWidth(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) buffer.getGraphics();
            AffineTransform trans = AffineTransform.getRotateInstance(Math.PI / 2);
            g.setTransform(trans);
            g.drawImage(source, 0, -source.getHeight(null), null);
            g.dispose();
            return buffer;
        }
        return null;
    }

    public static Image flipVertically(Image source) {
        if (source != null) {
            BufferedImage buffer = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffer.getGraphics();
            g.drawImage(source, 0, source.getHeight(null) - 1, source.getWidth(null) - 1, 0, 0, 0, source.getWidth(null) - 1, source.getHeight(null) - 1, null);
            g.dispose();
            return buffer;
        }
        return null;
    }

    public static Image flipHorizontally(Image source) {
        if (source != null) {
            BufferedImage buffer = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffer.getGraphics();
            g.drawImage(source, source.getWidth(null) - 1, 0, 0, source.getHeight(null) - 1, 0, 0, source.getWidth(null) - 1, source.getHeight(null) - 1, null);
            g.dispose();
            return buffer;
        }
        return null;
    }

    public static JLabel buildLabel(String name) {
        return new JLabel(name) {

            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g);
            }
        };
    }

    public static class WindowsButton extends JPanel implements MouseListener {

        private ActionListener action_listener = null;

        private JLabel iconlabel = new JLabel(), label = null;

        private String card_title;

        private Border empty_border = BorderFactory.createEmptyBorder(2, 2, 2, 2);

        private Border over_border = BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK), BorderFactory.createMatteBorder(1, 1, 0, 0, new Color(236, 233, 216))), BorderFactory.createEmptyBorder(1, 1, 1, 1));

        private Border pressed_border = BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(236, 233, 216)), BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK)), BorderFactory.createEmptyBorder(1, 1, 1, 1));

        public WindowsButton(String card_title, String title, String icon, ActionListener action_listener) {
            super(new BorderLayout());
            setOpaque(false);
            this.card_title = card_title;
            this.action_listener = action_listener;
            JPanel iconpanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            iconpanel.setOpaque(false);
            add(iconpanel, BorderLayout.CENTER);
            try {
                iconlabel.setIcon(Gui.loadIconsFromResource(new String[] { icon })[0]);
                iconlabel.setOpaque(false);
                iconpanel.add(iconlabel);
                iconlabel.setBorder(empty_border);
            } catch (Exception e) {
                Log.debug.out(e);
            }
            label = new JLabel(title);
            label.setOpaque(false);
            label.setFont(button_font);
            label.setForeground(Color.WHITE);
            JPanel label_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            label_panel.setOpaque(false);
            label_panel.add(label);
            add(label_panel, BorderLayout.SOUTH);
            this.addMouseListener(this);
        }

        public void setTitle(String title) {
            label.setText(title);
        }

        public void mouseClicked(MouseEvent e) {
            action_listener.actionPerformed(new ActionEvent(this, 0, ""));
        }

        public void mouseEntered(MouseEvent e) {
            iconlabel.setBorder(over_border);
        }

        public void mouseExited(MouseEvent e) {
            iconlabel.setBorder(empty_border);
        }

        public void mousePressed(MouseEvent e) {
            iconlabel.setBorder(pressed_border);
        }

        public void mouseReleased(MouseEvent e) {
            if (iconlabel.getBorder() == pressed_border) {
                iconlabel.setBorder(over_border);
            }
        }
    }

    public static JFrame getParentFrame(Component component) {
        for (component = component.getParent(); component != null; component = component.getParent()) {
            if (component instanceof JFrame) return (JFrame) component;
        }
        return null;
    }

    public static JDialog getParentDialog(Component component) {
        for (component = component.getParent(); component != null; component = component.getParent()) {
            if (component instanceof JDialog) return (JDialog) component;
        }
        return null;
    }

    public static Window getParentWindow(Component component) {
        for (component = component.getParent(); component != null; component = component.getParent()) {
            if (component instanceof Window) return (Window) component;
        }
        return null;
    }

    /**
     * Creates an image from the source, and paints it on a new image
     **/
    public static Image captureImage(Component source) {
        Image image = source.createImage(source.getWidth(), source.getHeight());
        source.paint(image.getGraphics());
        return image;
    }

    public static void saveImagePNG(BufferedImage image, File destination) throws IOException {
        javax.imageio.ImageIO.write(image, "PNG", destination);
    }

    public static boolean showEntityEditFrame(String title, Entity entity, String buttonName) throws BindException, IOException {
        return showEntityEditFrame(new Binder(), title, entity, buttonName);
    }

    public static boolean showEntityEditFrame(final Binder binder, String title, Entity entity, String buttonName) throws BindException, IOException {
        final JDialog jd = new JDialog((JFrame) null, title);
        final List<Boolean> result = new ArrayList<Boolean>();
        DataPanelEntity ep = new DataPanelEntity(title, entity);
        ep.setBinderBindable(binder, entity);
        JButton ok = new JButton(buttonName);
        JButton cancel = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(cancel);
        buttons.add(ok);
        result.clear();
        result.add(false);
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    binder.intoBindings();
                } catch (BindException e1) {
                    Log.debug.out(e);
                }
                result.clear();
                result.add(true);
                jd.setVisible(false);
            }
        });
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jd.setVisible(false);
            }
        });
        binder.intoControls();
        JPanel editPanel = new JPanel(new BorderLayout());
        editPanel.add(ep, BorderLayout.CENTER);
        editPanel.add(buttons, BorderLayout.SOUTH);
        jd.getContentPane().add(editPanel);
        jd.setModal(true);
        jd.pack();
        jd.setLocationRelativeTo(null);
        jd.setVisible(true);
        return result.get(0);
    }

    private static JDialog dialog = new JDialog();

    private static long endTime = 0;

    public static void startSplashScreen(Image image, int seconds) {
        endTime = System.currentTimeMillis() + seconds * 1000;
        dialog.setModal(false);
        dialog.setResizable(false);
        dialog.setUndecorated(true);
        JLabel label = new JLabel(new ImageIcon(image));
        dialog.getContentPane().add(label);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static void endSplashScreen() {
        long wait = System.currentTimeMillis() - endTime;
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        dialog.setVisible(false);
    }
}
