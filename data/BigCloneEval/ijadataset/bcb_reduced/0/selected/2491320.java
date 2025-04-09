package au.vermilion.samplebank.GUI;

import au.vermilion.samplebank.SampleAudio;
import au.vermilion.samplebank.SystemSampleBank;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 */
public class SampleAudioPanel extends JPanel implements ChangeListener, MouseListener, MouseMotionListener, ActionListener {

    private final SampleAudio sample;

    private final SampleBankPanel gui;

    protected JSlider volSlider = null;

    protected JSlider rateSlider = null;

    protected JSlider panSlider = null;

    private BufferedImage sImage;

    private int startDisplayIndex = 0;

    private int endDisplayIndex = 0;

    @SuppressWarnings("LeakingThisInConstructor")
    public SampleAudioPanel(SampleAudio s, SampleBankPanel g) {
        sample = s;
        gui = g;
        setDoubleBuffered(false);
        addMouseListener(this);
        addMouseMotionListener(this);
        startDisplayIndex = 0;
        endDisplayIndex = sample.length;
    }

    @Override
    public void paintComponent(Graphics g) {
        final Graphics2D graphics = (Graphics2D) g;
        if (sImage == null) paintImage(graphics);
        graphics.drawImage(sImage, 0, 0, this);
        if (mouseClickStart >= 0 && mouseClickEnd >= 0) {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            graphics.setColor(Color.CYAN);
            if (mouseClickStart < mouseClickEnd) {
                graphics.fillRect(mouseClickStart, 1, mouseClickEnd - mouseClickStart, getHeight() - 2);
            } else {
                graphics.fillRect(mouseClickEnd, 1, mouseClickStart - mouseClickEnd, getHeight() - 2);
            }
        }
    }

    public void paintImage(Graphics2D srcG) {
        final int width = getWidth() - 1;
        final int height = getHeight() - 1;
        sImage = srcG.getDeviceConfiguration().createCompatibleImage(width + 1, height + 1, Transparency.OPAQUE);
        final Graphics2D graphics = sImage.createGraphics();
        final int offset = height / 2;
        final int ampli = 3 - offset;
        final int NUMLINES = width * 3;
        final float ALPHA = 0.2f;
        final double sScale = (endDisplayIndex - startDisplayIndex) / (double) NUMLINES;
        final double pScale = width / (double) NUMLINES;
        graphics.setColor(new Color(0, 0, 0));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(255, 0, 0));
        graphics.drawRect(0, 0, width, offset);
        graphics.drawRect(0, 0, width, height);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHA));
        graphics.setColor(new Color(0, 255, 0));
        drawChannel(NUMLINES, sScale, pScale, ampli, offset, graphics, sample.sampleData[0]);
        if (sample.numChannels == 2) {
            graphics.setColor(new Color(0, 0, 255));
            drawChannel(NUMLINES, sScale, pScale, ampli, offset, graphics, sample.sampleData[1]);
        }
        graphics.dispose();
    }

    private void drawChannel(final int NUMLINES, final double sScale, final double pScale, final int ampli, final int offset, final Graphics2D graphics, float[] channel) {
        for (int x = 0; x < NUMLINES; x++) {
            int si = startDisplayIndex + (int) (x * sScale);
            int ei = startDisplayIndex + (int) ((x + 1) * sScale);
            float maxy = 0.0f;
            float miny = 0.0f;
            int step = (ei - si) / 11;
            if (step < 1) {
                step = 1;
            }
            if (ei == si) {
                ei++;
            }
            for (int c = si; c < ei; c += step) {
                if (c < 0) {
                    c = 0;
                }
                if (c >= endDisplayIndex) {
                    break;
                }
                maxy = Math.max(maxy, channel[c]);
                miny = Math.min(miny, channel[c]);
            }
            int px = (int) (x * pScale);
            int pmaxy = (int) (maxy * ampli + offset);
            int pminy = (int) (miny * ampli + offset);
            graphics.drawLine(px, pmaxy, px, pminy);
            if (x % 10 == 0) {
                try {
                    Thread.sleep(0);
                } catch (Exception ex) {
                }
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == volSlider) {
            sample.volume = volSlider.getValue();
        } else if (e.getSource() == rateSlider) {
            sample.pitch = rateSlider.getValue();
        } else if (e.getSource() == panSlider) {
            sample.panning = panSlider.getValue();
        }
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        mouseClickStart = -1;
        mouseClickEnd = -1;
        sImage = null;
    }

    private static final long serialVersionUID = -1L;

    private int mouseClickStart = -1;

    private int mouseClickEnd = -1;

    private boolean mouseDown = false;

    @Override
    public void mousePressed(MouseEvent me) {
        int oldPos = mouseClickEnd;
        mouseClickEnd = -1;
        repaint(oldPos, 0, 1, getHeight());
        oldPos = mouseClickStart;
        mouseClickStart = me.getX();
        repaint(oldPos, 0, 1, getHeight());
        repaint(mouseClickStart, 0, 1, getHeight());
        mouseDown = true;
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        mouseClickEnd = me.getX();
        if (mouseClickEnd < 0) mouseClickEnd = 0;
        if (mouseClickEnd > getWidth()) mouseClickEnd = getWidth();
        mouseDown = false;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        if (mouseDown) {
            int oldPos = mouseClickEnd;
            mouseClickEnd = me.getX();
            if (mouseClickEnd < 0) mouseClickEnd = 0;
            if (mouseClickEnd > getWidth()) mouseClickEnd = getWidth();
            repaint(oldPos, 0, 1, getHeight());
            repaint(mouseClickEnd, 0, 1, getHeight());
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("ZOOMIN")) {
            if (mouseClickStart >= 0 && mouseClickEnd >= 0 && mouseClickStart != mouseClickEnd) {
                if (mouseClickStart > mouseClickEnd) {
                    int t = mouseClickStart;
                    mouseClickStart = mouseClickEnd;
                    mouseClickEnd = t;
                }
                float sScale = (endDisplayIndex - startDisplayIndex) / (float) getWidth();
                endDisplayIndex = startDisplayIndex + (int) (mouseClickEnd * sScale);
                startDisplayIndex = startDisplayIndex + (int) (mouseClickStart * sScale);
                refresh();
            } else {
                int mid = (startDisplayIndex + endDisplayIndex) / 2;
                int dist = mid - startDisplayIndex;
                dist = (int) (dist * 0.9f);
                startDisplayIndex = mid - dist;
                endDisplayIndex = mid + dist;
                refresh();
            }
        } else if (ae.getActionCommand().equals("ZOOMOUT")) {
            int mid = (startDisplayIndex + endDisplayIndex) / 2;
            int dist = mid - startDisplayIndex;
            dist = (int) (dist * 1.8f);
            startDisplayIndex = mid - dist;
            endDisplayIndex = mid + dist;
            refresh();
        } else if (ae.getActionCommand().equals("CUT")) {
            if (mouseClickStart >= 0 && mouseClickEnd >= 0 && mouseClickStart != mouseClickEnd) {
                if (mouseClickStart > mouseClickEnd) {
                    int t = mouseClickStart;
                    mouseClickStart = mouseClickEnd;
                    mouseClickEnd = t;
                }
                float sScale = (endDisplayIndex - startDisplayIndex) / (float) getWidth();
                int endCut = startDisplayIndex + (int) (mouseClickEnd * sScale);
                int startCut = startDisplayIndex + (int) (mouseClickStart * sScale);
                if (startCut < startDisplayIndex) startCut = startDisplayIndex;
                if (endCut > endDisplayIndex) endCut = endDisplayIndex;
                int newLen = sample.length - (endCut - startCut);
                float[][] newSampleData = new float[sample.numChannels][newLen];
                for (int y = 0; y < sample.numChannels; y++) {
                    for (int x = 0; x < startCut; x++) {
                        newSampleData[y][x] = sample.sampleData[y][x];
                    }
                    for (int x = endCut; x < sample.length; x++) {
                        newSampleData[y][startCut + x - endCut] = sample.sampleData[y][x];
                    }
                }
                sample.sampleData = newSampleData;
                sample.length = newLen;
                gui.sampleUpdated(sample);
                refresh();
            }
        }
    }

    private void refresh() {
        if (startDisplayIndex < 0) {
            startDisplayIndex = 0;
        }
        if (endDisplayIndex > sample.length) {
            endDisplayIndex = sample.length;
        }
        sImage = null;
        mouseClickStart = -1;
        repaint();
    }
}
