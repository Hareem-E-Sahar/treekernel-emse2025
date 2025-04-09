package com.guanda.swidgex.widgets.internal;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import cn.com.windriver.weather.DaysWeather;

class JWeather extends JComponent {

    private static Cursor LEFT_CURSOR;

    private static Cursor RIGHT_CURSOR;

    static {
        Image leftImg = Toolkit.getDefaultToolkit().getImage(JWeather.class.getResource("/weather/left_cursor.png"));
        Image rightImg = Toolkit.getDefaultToolkit().getImage(JWeather.class.getResource("/weather/right_cursor.png"));
        LEFT_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(leftImg, new Point(0, 16), "Left Cursor");
        RIGHT_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(rightImg, new Point(0, 16), "Right Cursor");
    }

    private static final long serialVersionUID = 1L;

    private int curCurType = Cursor.DEFAULT_CURSOR;

    private DaysWeather today;

    private Image bgImg;

    private String error;

    public JWeather() {
        bgImg = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/weather/day_night.png"));
        setForeground(Color.yellow);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                myMouseClicked(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                curCurType = Cursor.DEFAULT_CURSOR;
                myMouseMoved(e);
            }

            public void mouseExited(MouseEvent e) {
                changeCurToDefault();
            }
        });
        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                myMouseMoved(e);
            }
        });
    }

    private void changeCurToDefault() {
        curCurType = Cursor.DEFAULT_CURSOR;
        this.setCursor(Cursor.getDefaultCursor());
    }

    private Rectangle getContentArea() {
        int cw = getWidth();
        int ch = getHeight();
        int bw = bgImg.getWidth(this);
        int bh = bgImg.getHeight(this);
        double ratio = (double) bw / bh;
        int nx, ny, nw, nh;
        if (cw * bh > bw * ch) {
            nh = ch;
            nw = (int) (ratio * nh);
            nx = (cw - nw) / 2;
            ny = 0;
        } else {
            nw = cw;
            nh = (int) (nw / ratio);
            nx = 0;
            ny = (ch - nh) / 2;
        }
        return new Rectangle(nx, ny, nw, nh);
    }

    private void myMouseMoved(MouseEvent e) {
        if (today != null) {
            Rectangle bounds = getContentArea();
            int x = e.getX();
            int y = e.getY();
            if (x > bounds.x && x < bounds.x + bounds.width / 3 && y > bounds.y && y < bounds.y + bounds.height) {
                if (curCurType != Cursor.W_RESIZE_CURSOR) {
                    if (today.getPrev() != null) {
                        this.setToolTipText(null);
                        curCurType = Cursor.W_RESIZE_CURSOR;
                        this.setCursor(LEFT_CURSOR);
                        this.setToolTipText(today.getPrev().getDate());
                    }
                } else {
                    if (today.getPrev() == null) {
                        this.setToolTipText(null);
                        curCurType = Cursor.DEFAULT_CURSOR;
                        this.setCursor(Cursor.getDefaultCursor());
                    }
                }
            } else if (x > bounds.x + bounds.width * 2 / 3 && x < bounds.x + bounds.width && y > bounds.y && y < bounds.y + bounds.height) {
                if (curCurType != Cursor.E_RESIZE_CURSOR) {
                    if (today.getNext() != null) {
                        this.setToolTipText(null);
                        curCurType = Cursor.E_RESIZE_CURSOR;
                        this.setCursor(RIGHT_CURSOR);
                        this.setToolTipText(today.getNext().getDate());
                    }
                } else {
                    if (today.getNext() == null) {
                        this.setToolTipText(null);
                        curCurType = Cursor.DEFAULT_CURSOR;
                        this.setCursor(Cursor.getDefaultCursor());
                    }
                }
            } else {
                if (curCurType != Cursor.DEFAULT_CURSOR) {
                    curCurType = Cursor.DEFAULT_CURSOR;
                    this.setCursor(Cursor.getDefaultCursor());
                    this.setToolTipText(null);
                }
            }
        }
    }

    private void myMouseClicked(MouseEvent e) {
        if (today != null) {
            int x = e.getX();
            int w = getWidth();
            if (x < w / 3) {
                if (today.getPrev() != null) {
                    this.setWeather(today.getPrev());
                    curCurType = Cursor.DEFAULT_CURSOR;
                    myMouseMoved(e);
                }
            } else if (x > 2 * w / 3) {
                if (today.getNext() != null) {
                    this.setWeather(today.getNext());
                    curCurType = Cursor.DEFAULT_CURSOR;
                    myMouseMoved(e);
                }
            }
        }
    }

    public void setWeather(DaysWeather today) {
        this.today = today;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int cw = getWidth();
        int ch = getHeight();
        Rectangle bounds = getContentArea();
        int bw = bgImg.getWidth(this);
        int bh = bgImg.getHeight(this);
        double ratio = (double) bw / bh;
        int nx, ny, nw, nh;
        nx = bounds.x;
        ny = bounds.y;
        nw = bounds.width;
        nh = bounds.height;
        g.drawImage(bgImg, nx, ny, nw, nh, this);
        if (today != null) {
            ratio = (double) nw / bw;
            Image sImg = today.getStartImage();
            int sw = (int) (sImg.getWidth(this) * ratio);
            int sh = (int) (sImg.getHeight(this) * ratio);
            int sx = nx + (nw / 2 - sw) / 2;
            int sy = ny + (nh - sh) / 2;
            g.drawImage(sImg, sx, sy, sw, sh, this);
            FontMetrics fm = g.getFontMetrics();
            String txt = today.getCity() + " " + today.getDate();
            int x = (cw - fm.stringWidth(txt)) / 2;
            int y = ny + fm.getHeight();
            g.drawString(txt, x, y);
            txt = today.getStatus();
            x = (cw - fm.stringWidth(txt)) / 2;
            y = (ch - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(txt, x, y);
            txt = today.getTemperature();
            x = (cw - fm.stringWidth(txt)) / 2;
            y += fm.getHeight();
            g.drawString(txt, x, y);
            txt = today.getDescription();
            x = (cw - fm.stringWidth(txt)) / 2;
            y += fm.getHeight();
            g.drawString(txt, x, y);
            Image tImg = today.getToImage();
            int tw = (int) (tImg.getWidth(this) * ratio);
            int th = (int) (tImg.getHeight(this) * ratio);
            int tx = nx + (nw / 2 - tw) / 2 + nw / 2;
            int ty = ny + (nh - th) / 2;
            g.drawImage(tImg, tx, ty, tw, th, this);
        } else if (error != null) {
            FontMetrics fm = g.getFontMetrics();
            int x = (cw - fm.stringWidth(error)) / 2;
            int y = bounds.y + fm.getHeight();
            g.drawString(error, x, y);
        }
    }

    public void setError(String error) {
        this.error = error;
        repaint();
    }
}
