package com.openclub.ui.objects.widget;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.text.JTextComponent;
import com.openclub.objects.Note;

public class NoteDisplay extends JWindow implements MouseListener, MouseMotionListener, WindowFocusListener, ComponentListener, Runnable {

    private static final int MARGIN = 0;

    private static final int BLUR = 3;

    private static final int NOTE_CLEAR = 0;

    private static final int NOTE_BLURED = 1;

    private Point firstPoint = null;

    private Point lastPoint = null;

    private boolean dragged = false;

    private Color backgroundColor = null;

    private Color borderColor = null;

    private Color backAlpha = null;

    private static Color defaultBackground = new Color(251, 236, 93);

    private Image wholeScreenCapture = null;

    private Image shadow = null;

    private JPanel contentPane = new JPanel() {

        public void paintComponent(Graphics g) {
            System.err.println("repaint " + NoteDisplay.this.note);
            new Throwable().printStackTrace();
            g.drawImage(shadow, 0, 0, NoteDisplay.this);
        }
    };

    private JTextComponent content = new JTextArea();

    private JTextComponent fromTextField = new JTextField();

    private JTextComponent toTextField = new JTextField();

    public NoteDisplay(Frame frame) {
        super(frame);
        setBackground(defaultBackground);
        setSize(200, 200);
        contentPane.addMouseMotionListener(this);
        contentPane.addMouseListener(this);
        content.addMouseMotionListener(this);
        content.addMouseListener(this);
        toTextField.addMouseMotionListener(this);
        toTextField.addMouseListener(this);
        fromTextField.addMouseMotionListener(this);
        fromTextField.addMouseListener(this);
        fromTextField.setOpaque(false);
        toTextField.setOpaque(false);
        content.setOpaque(false);
        contentPane.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        toTextField.setBorder(BorderFactory.createLineBorder(borderColor));
        fromTextField.setBorder(BorderFactory.createLineBorder(borderColor));
        JPanel subPanel = new JPanel(new GridLayout());
        subPanel.setOpaque(false);
        subPanel.add(fromTextField);
        subPanel.add(toTextField);
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.add(content, BorderLayout.CENTER);
        contentPane.add(subPanel, BorderLayout.NORTH);
        super.setContentPane(contentPane);
        addComponentListener(this);
        addWindowFocusListener(this);
        new Thread(this).start();
    }

    public void setVisible(boolean b) {
        if (b) {
            captureBackground(true);
        }
        super.setVisible(b);
        if (b) {
            try {
                createShadowPicture(NOTE_BLURED);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    private Note note = new Note();

    public void setNote(Note note) {
        this.note = note;
        int pox = (Integer) note.get(Note.POSX);
        int poy = (Integer) note.get(Note.POSY);
        setLocation(pox, poy);
        fromTextField.setText((String) note.get(Note.FROM));
        toTextField.setText((String) note.get(Note.TO));
        content.setText((String) note.get(Note.TEXT));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        firstPoint = e.getPoint();
        beginDragging();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        firstPoint = null;
        lastPoint = null;
        endDragging();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        lastPoint = e.getPoint();
        if (firstPoint != null && lastPoint != null) {
            if (isVisible()) {
                Point start = this.getLocationOnScreen();
                this.setLocation(start.x + lastPoint.x - firstPoint.x, start.y + lastPoint.y - firstPoint.y);
                note.set(Note.POSX, this.getX());
                note.set(Note.POSY, this.getY());
                try {
                    createShadowPicture(NOTE_CLEAR);
                } catch (AWTException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void beginDragging() {
        if (dragged == false) {
            dragged = true;
            captureBackground(false);
        }
    }

    private void endDragging() {
        dragged = false;
        try {
            createShadowPicture(NOTE_BLURED);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public void componentShown(ComponentEvent evt) {
    }

    public void componentResized(ComponentEvent evt) {
        repaint();
    }

    public void componentMoved(ComponentEvent evt) {
        repaint();
    }

    public void componentHidden(ComponentEvent evt) {
    }

    public void windowGainedFocus(WindowEvent evt) {
    }

    public void windowLostFocus(WindowEvent evt) {
        wholeScreenCapture = null;
    }

    private static ConvolveOp op = null;

    static {
        op = getBlurOp(BLUR);
    }

    private void createShadowPicture(int type) throws AWTException {
        if (shadow == null) {
            shadow = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g = (Graphics2D) shadow.getGraphics();
        Rectangle windowRect = getBounds();
        g.drawImage(wholeScreenCapture, -windowRect.x, -windowRect.y, NoteDisplay.this);
        g.setColor(backAlpha);
        g.fillRect(MARGIN - 1, MARGIN - 1, windowRect.width - 2 * (MARGIN - 1), windowRect.height - 2 * (MARGIN - 1));
        if (type == NOTE_BLURED) {
            g.drawImage((BufferedImage) shadow, op, 0, 0);
        }
    }

    private static ConvolveOp getBlurOp(int size) {
        float[] data = new float[size * size];
        float value = 1 / (float) (size * size);
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
        }
        return new ConvolveOp(new Kernel(size, size, data));
    }

    public void freeCapture() {
        wholeScreenCapture = null;
    }

    public void captureBackground(boolean force) {
        System.err.println("captureBackground: " + new Throwable().getStackTrace()[1]);
        super.setVisible(false);
        if (force) {
            capture();
            super.setVisible(true);
        } else {
            captureRequested = true;
        }
    }

    public void capture() {
        try {
            GraphicsConfiguration conf = getGraphicsConfiguration();
            GraphicsDevice device = conf.getDevice();
            wholeScreenCapture = null;
            wholeScreenCapture = new Robot(device).createScreenCapture(conf.getBounds());
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private long lastupdate = 0;

    private boolean captureRequested = true;

    public void run() {
        try {
            while (true) {
                Thread.sleep(50);
                long now = new Date().getTime();
                if (captureRequested) {
                    try {
                        if ((now - lastupdate) > 100) {
                            capture();
                            lastupdate = now;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    NoteDisplay.super.setVisible(true);
                    captureRequested = false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Color getBackground() {
        return backgroundColor;
    }

    public void setBackground(Color background) {
        backgroundColor = background;
        borderColor = background.darker();
        backAlpha = new Color((backgroundColor.getRGB() & 0x00FFFFFF) + (153 << 24), true);
        super.setBackground(background);
    }
}
