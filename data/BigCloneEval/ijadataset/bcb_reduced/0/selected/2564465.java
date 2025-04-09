package com.sderhy;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import tools.*;

public class Diaporama extends Frame implements WindowListener, MouseListener, ActionListener, ComponentListener, Runnable {

    int screenH = Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3;

    int screenW = Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 3;

    Thread runner;

    Vector vimages;

    Image img;

    int w, h;

    int destw, desth;

    int xLeft, yBottom;

    int currentImage = 0;

    int numImages;

    private int increment = 1;

    int pause = 1000;

    int x = 0;

    int y = 0;

    Image offImage;

    private Graphics go;

    private boolean isRunning;

    Button Stop, Play, Pause, Reverse, Slower, Faster;

    public Diaporama(Vector vimages) {
        super("Diaporama");
        this.setBackground(Color.black);
        this.setSize(screenW, screenH);
        this.vimages = vimages;
        isRunning = false;
        numImages = vimages.size();
        this.addMouseListener(this);
        this.addWindowListener(this);
        Panel p = new Panel();
        Stop = new Button("Stop");
        Stop.setBackground(Color.lightGray);
        Stop.addActionListener(this);
        Play = new Button("Play");
        Play.setBackground(Color.lightGray);
        Play.addActionListener(this);
        Pause = new Button("Pause");
        Pause.setBackground(Color.lightGray);
        Pause.addActionListener(this);
        Reverse = new Button("Reverse");
        Reverse.setBackground(Color.lightGray);
        Reverse.addActionListener(this);
        Slower = new Button("Slower");
        Slower.setBackground(Color.lightGray);
        Slower.addActionListener(this);
        Faster = new Button("Faster");
        Faster.setBackground(Color.lightGray);
        Faster.addActionListener(this);
        p.add(Stop);
        p.add(Play);
        p.add(Pause);
        p.add(Reverse);
        p.add(Slower);
        p.add(Faster);
        add("South", p);
        this.addComponentListener(this);
    }

    public void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
            isRunning = true;
        }
    }

    public void stop() {
        runner.stop();
        isRunning = false;
        runner = null;
    }

    public void update(Graphics g) {
        if (offImage == null) offImage = this.createImage(getSize().width, getSize().height);
        go = offImage.getGraphics();
        go.fillRect(0, 0, getSize().width, getSize().height);
        go.drawImage(img, x, y, xLeft, yBottom, 0, 0, w, h, this);
        paint(g);
        go.dispose();
        Tools.gc();
    }

    public void show() {
        Winager.add(this);
        super.show();
    }

    public void hide() {
        Winager.remove(this);
        super.hide();
    }

    public void dispose() {
        stop();
        vimages = null;
        Tools.gc();
        super.dispose();
    }

    public void paint(Graphics g) {
        g.drawImage(offImage, 0, 0, getSize().width, getSize().height, this);
    }

    public void centerImage(int largeur, int hauteur) {
        int width = getSize().width;
        int height = getSize().height;
        if (w > width && (w > h)) {
            destw = width * 95 / 100;
            desth = (h * width * 95) / (w * 100);
        } else if (h > height && (h >= w)) {
            desth = height * 90 / 100;
            destw = w * height * 90 / (h * 100);
        } else {
            desth = h;
            destw = w;
        }
        x = (width - destw) / 2;
        y = (height - desth) / 2;
        xLeft = (width + destw) / 2;
        yBottom = (height + desth) / 2;
    }

    public void run() {
        while (true) {
            PixObject po = (PixObject) vimages.elementAt(currentImage);
            img = po.image;
            w = po.w;
            h = po.h;
            centerImage(w, h);
            repaint();
            Tools.pause(pause);
            currentImage += increment;
            if (currentImage < 0) currentImage += numImages;
            currentImage %= numImages;
        }
    }

    private void suspend() {
        if (runner == null) return;
        if (!isRunning) return;
        runner.suspend();
        isRunning = false;
    }

    private void resume() {
        if (runner == null) return;
        if (isRunning) return;
        runner.resume();
        isRunning = true;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == Stop) suspend();
        if (e.getSource() == Play) resume();
        if (e.getSource() == Reverse) increment = -increment;
        if (e.getSource() == Pause) suspend();
        if (e.getSource() == Slower) pause += pause * 30 / 100;
        if (e.getSource() == Faster) {
            pause -= pause * 30 / 100;
            if (pause < 100) pause = 100;
        }
    }

    public void mousePressed(MouseEvent e) {
        Tools.debug(this, "  mousePressed");
        if (isRunning) suspend(); else resume();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        ;
    }

    public void mouseExited(MouseEvent e) {
        ;
    }

    public void mouseReleased(MouseEvent e) {
        ;
    }

    public void windowClosing(WindowEvent e) {
        this.dispose();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        offImage = null;
        Tools.gc();
    }

    ;

    public void componentMoved(ComponentEvent e) {
    }

    ;

    public void componentShown(ComponentEvent e) {
    }

    ;

    public void componentHidden(ComponentEvent e) {
        if (isRunning) suspend();
    }

    ;
}
