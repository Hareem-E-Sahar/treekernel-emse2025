import ch.rolandschaer.ascrblr.util.ServiceException;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXPanel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.swing.AbstractButton;
import javax.swing.border.*;
import ImageFileChooser.*;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.swingx.graphics.GraphicsUtilities;
import org.jdesktop.swingx.image.GaussianBlurFilter;

/**
 *
 * @author Dark_Wolf
 */
public class AvatarPanel extends JPanel {

    JPanel centerPanel;

    Avatar avatar;

    TrackPanel trackPanel;

    AlbumPanel albumPanel;

    ImagePanel imagePanel;

    JButton redownloadButton;

    JButton changePictureButton;

    JButton closeButton;

    JFileChooser fc;

    AlbumDownloader albumDownloader = null;

    private BufferedImage blurBuffer;

    private BufferedImage backBuffer;

    private float alpha = 0.0f;

    Thread infoloader;

    Component parent;

    /** Creates a new instance of AvatarPanel
     * @param avatar
     * The avatar to be drawn
     * @param parent 
     * The parent component
     */
    public AvatarPanel(final Avatar avatar, Component parent) {
        super(new GridBagLayout());
        setOpaque(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.parent = parent;
        this.avatar = avatar;
        centerPanel = new JPanel(new FlowLayout());
        centerPanel.setOpaque(false);
        albumPanel = new AlbumPanel(new GridBagLayout());
        albumPanel.setAlpha(0.0f);
        trackPanel = new TrackPanel();
        trackPanel.setOpaque(false);
        imagePanel = new ImagePanel(avatar.getRawImage(), parent.getWidth() - 68, parent.getHeight() - 68);
        createButtons();
        addComponents();
        centerPanel.add(albumPanel);
        add(centerPanel, new GridBagConstraints());
        infoloader = new Thread(new InfoLoaderThread());
        infoloader.setPriority(Thread.NORM_PRIORITY);
        infoloader.start();
    }

    private void addComponents() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.2;
        c.weighty = 0.1;
        c.gridwidth = 5;
        c.gridheight = 3;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        albumPanel.add(trackPanel, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 4;
        c.gridy = 0;
        c.weightx = 0.2;
        c.weighty = 0.5;
        c.gridwidth = 4;
        c.gridheight = 3;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        albumPanel.add(imagePanel, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 5;
        c.gridy = 0;
        c.weightx = 0.1;
        c.weighty = 0.1;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        albumPanel.add(closeButton);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 3;
        c.gridy = 4;
        c.weightx = 0.1;
        c.weighty = 0.2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        albumPanel.add(redownloadButton, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        c.gridy = 4;
        c.weightx = 0.1;
        c.weighty = 0.2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        albumPanel.add(changePictureButton, c);
    }

    private void createButtons() {
        redownloadButton = new DarkButton("Redownload image", 30, 120);
        redownloadButton.setBorderPainted(false);
        redownloadButton.setBackground(new Color(0, 0, 0, 0));
        redownloadButton.setFocusable(false);
        redownloadButton.setOpaque(false);
        redownloadButton.setFont(new Font("Dialog", Font.PLAIN, 10));
        redownloadButton.setForeground(Color.WHITE);
        redownloadButton.setContentAreaFilled(false);
        redownloadButton.setVerticalTextPosition(AbstractButton.CENTER);
        redownloadButton.setHorizontalTextPosition(AbstractButton.CENTER);
        redownloadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                avatar.forceDownloadImage();
            }
        });
        changePictureButton = new DarkButton("Set local image", 30, 120);
        changePictureButton.setFont(new Font("Dialog", Font.PLAIN, 10));
        changePictureButton.setContentAreaFilled(false);
        changePictureButton.setVerticalTextPosition(AbstractButton.CENTER);
        changePictureButton.setHorizontalTextPosition(AbstractButton.CENTER);
        changePictureButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (fc == null) {
                    fc = new JFileChooser();
                    fc.addChoosableFileFilter(new ImageFilter());
                    fc.setAcceptAllFileFilterUsed(false);
                    fc.setFileView(new ImageFileView());
                    fc.setAccessory(new ImagePreview(fc));
                }
                int returnVal = fc.showOpenDialog(albumPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    avatar.setImage(file);
                }
                fc.setSelectedFile(null);
            }
        });
        closeButton = new JButton(new CloseIcon(16, 16));
        closeButton.setBorderPainted(false);
        closeButton.setBackground(new Color(0, 0, 0, 0));
        closeButton.setFocusable(false);
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setForeground(Color.WHITE);
        closeButton.setPressedIcon(new CloseIcon(15, 15));
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ((AlbumApplet) parent).removeAvatarFrame();
            }
        });
    }

    private void createBlur() {
        blurBuffer = GraphicsUtilities.createCompatibleImage(parent.getWidth(), parent.getHeight());
        Graphics2D g2 = blurBuffer.createGraphics();
        parent.paint(g2);
        System.out.println(parent.getClass().getName());
        g2.dispose();
        backBuffer = blurBuffer;
        blurBuffer = GraphicsUtilities.createThumbnailFast(blurBuffer, parent.getWidth() / 2);
        blurBuffer = new GaussianBlurFilter(5).filter(blurBuffer, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isVisible() && blurBuffer != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(backBuffer, 0, 0, null);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.drawImage(blurBuffer, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    public void fadeIn() {
        createBlur();
        setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Animator animator = PropertySetter.createAnimator(400, albumPanel, "alpha", 1.0f);
                animator.setAcceleration(0.2f);
                animator.setDeceleration(0.3f);
                animator.addTarget(new PropertySetter(AvatarPanel.this, "alpha", 1.0f));
                animator.start();
            }
        });
    }

    public void fadeOut() {
        createBlur();
        setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Animator animator = PropertySetter.createAnimator(400, albumPanel, "alpha", 0.0f);
                animator.setAcceleration(0.2f);
                animator.setDeceleration(0.3f);
                animator.addTarget(new PropertySetter(AvatarPanel.this, "alpha", 0.0f));
                animator.start();
            }
        });
    }

    private class TrackPanel extends JXPanel {

        ArrayList<String> tracks;

        Font avatarFont;

        int textSize = 12;

        public TrackPanel() {
            avatarFont = new Font("Dialog", Font.PLAIN, textSize);
            tracks = new ArrayList<String>();
        }

        public void loadText() {
            if (albumDownloader != null) {
                tracks = albumDownloader.getTracksForAlbum();
            }
            if (tracks.size() < 1) {
                tracks = SqliteInterface.getInstance().getTracksForAlbum(avatar.getAlbum());
            }
            repaint();
            SwingUtilities.getAncestorOfClass(AlbumPanel.class, this).validate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            int tempSize = (int) (getHeight() / (tracks.size() * 1.5));
            if (tempSize != textSize && tempSize < 12) {
                textSize = tempSize;
                avatarFont = new Font("Dialog", Font.PLAIN, textSize);
            }
            super.paintComponent(g);
            int count = 1;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setFont(avatarFont);
            for (String string : tracks) {
                g2.drawString(string, 5, count * textSize * 1.5f);
                count++;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getMaximumWidth(), (int) ((tracks.size() + 1) * textSize * 1.5));
        }

        private int getMaximumWidth() {
            int width = 0;
            FontMetrics metrics = this.getGraphics().getFontMetrics();
            for (String string : tracks) {
                int tempwidth = metrics.stringWidth(string);
                if (tempwidth > width) {
                    width = tempwidth;
                }
            }
            return width;
        }
    }

    private class ButtonIcon implements Icon {

        int height;

        int width;

        public ButtonIcon(int height, int width) {
            this.height = height;
            this.width = width;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D button = new RoundRectangle2D.Double(x, y, width, height, height / 2, height / 2);
            g2.setColor(new Color(0x000000));
            Paint paint = g2.getPaint();
            g2.setPaint(new LinearGradientPaint(x + width / 2, y + height / 2, x + width / 2, y + height, new float[] { 0, 1 }, new Color[] { new Color(0, 0, 0, 80), new Color(255, 255, 255, 100) }, MultipleGradientPaint.CycleMethod.NO_CYCLE));
            g2.fill(button);
            g2.setPaint(paint);
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    private class AlbumPanel extends JXPanel {

        public AlbumPanel() {
            setOpaque(false);
            int bpad = 10;
            setBorder(new EmptyBorder(bpad, bpad, bpad, bpad));
        }

        public AlbumPanel(LayoutManager2 layout) {
            super(layout);
            setOpaque(false);
            int bpad = 10;
            setBorder(new EmptyBorder(bpad, bpad, bpad, bpad));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int x = 0;
            int y = 0;
            int w = getWidth();
            int h = getHeight();
            int arc = 30;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 220));
            g2.fillRoundRect(x, y, w, h, arc, arc);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(40, 40, 40, 220));
            g2.drawRoundRect(x, y, w, h, arc, arc);
            g2.dispose();
        }
    }

    private class InfoLoaderThread implements Runnable {

        public void run() {
            try {
                albumDownloader = new AlbumDownloader(avatar.getAlbum(), avatar.getArtist());
            } catch (ServiceException ex) {
                System.out.println("Album not found on Last.fm");
            } finally {
                trackPanel.loadText();
            }
        }
    }
}
