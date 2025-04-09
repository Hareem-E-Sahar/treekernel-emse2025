package GUI;

import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.StringTokenizer;
import java.lang.*;
import java.io.*;

public class FlamePanel extends Panel implements Runnable {

    public boolean handleEvent(Event event) {
        if (event.target == this) switch(event.id) {
            case 501:
                burn = 40;
                return true;
            case 503:
                return true;
            case 502:
                if (!tw_isLink || event.y < YSIZE) return true;
                stop();
                return true;
        }
        return true;
    }

    int rand(int n) {
        return Math.abs(rd.nextInt() % n);
    }

    int colorit(int c) {
        return pal[c & 0xff];
    }

    int coloroff(int c) {
        return (c & 0xff0000) >> 16;
    }

    void processline(int i) {
        int offset = XSIZE;
        int poffset = offset - XSIZE;
        if (i <= 1 || i >= XE) {
            for (int j = 1; j < YSIZE; j++) {
                off[i + poffset] = 0xff000000;
                poffset += XSIZE;
            }
        } else {
            for (int j = 1; j < YSIZE; j++) {
                int c = coloroff(off[i + offset]);
                if (c < 6) off[i + poffset] = 0xff000000; else off[(i - (rand(3) - 1)) + poffset] = colorit(c - rand(6));
                poffset += XSIZE;
                offset += XSIZE;
            }
        }
    }

    void smoothBottom() {
        for (int i = 2; i < XE - 1; i++) {
            int total = 0;
            for (int j = -1; j <= 1; j++) total += coloroff(off[BOTTOM + i + j]);
            off[BOTTOM + i] = colorit(total / 3);
        }
    }

    void splashRandom() {
        int d = rand(15);
        int max = XE - 1;
        for (int i = 0; i < d; i++) {
            int c = rand(max);
            int col = coloroff(off[BOTTOM + 1 + c]);
            if ((col &= 0x7) < 0) col = 0;
            off[BOTTOM + 1 + c] = colorit(col);
        }
    }

    void splash() {
        int max = XE - 1;
        for (int i = 0; i < 25; i++) {
            int d = rand(4) + 2;
            int c = rand(max - d);
            for (int j = 0; j < d; j++) {
                off[BOTTOM + 1 + c] = 0xff000000;
                c++;
            }
        }
    }

    public synchronized void process() {
        int mid = 1 + (XE - 1) / 2;
        for (int i = rand(8); i > 0; i--) {
            int c = rand(XE - 1 - 15 - 15) + 15;
            for (int j = 0; j < 10; j++) off[BOTTOM + c + j] = colorit(180 + rand(76));
        }
        if (burn > 0) {
            splash();
            burn--;
        }
        splashRandom();
        smoothBottom();
        for (int i = 1; i < mid; i++) processline(i);
        for (int i = XE; i >= mid; i--) processline(i);
    }

    void makepal() {
        for (int i = 0; i < 256; i++) pal[i] = 0xff000000;
        for (int i = 1; i < 110; i++) pal[i] = 0xff000000 | i << 16 | (i * 3) / 5 << 8 | i / 5;
        for (int i = 110; i < 256; i++) {
            int c = pal[i - 1] & 0xff00ffff;
            if ((c & 0xff00) < 60928) c += 256;
            if ((c & 0xff00) < 60928) c += 256;
            if ((c & 0xff) < 255) c++;
            pal[i] = 0xff000000 | c | i << 16;
        }
    }

    public static void setMessage(String R) {
        Message = R;
    }

    void tw_begin() {
        for (int i = 0; i < 30; i++) tw_color[i] = pal[170 + i] + 80;
        tw_isLink = true;
        tw_link = "http://www.6sense.com";
        String fts = "TimesRoman";
        Image twImg = createImage(XSIZE, 50);
        Graphics twGrp;
        twGrp = twImg.getGraphics();
        twGrp.setFont(new Font(fts, 1, 40));
        FontMetrics fm = twGrp.getFontMetrics();
        String words = (Message == null) ? "Flames+Nishanth+Menon" : Message;
        StringTokenizer st = new StringTokenizer(words, "+");
        tw_max = st.countTokens();
        tw_words = new int[tw_max][1000];
        tw_count = 0;
        while (st.hasMoreTokens()) {
            clearOff();
            String str = st.nextToken();
            twGrp.setColor(Color.black);
            twGrp.fillRect(0, 0, XSIZE, 50);
            twGrp.setColor(Color.white);
            twGrp.drawString(str, (XSIZE - fm.stringWidth(str)) / 2, 40);
            PixelGrabber pg = new PixelGrabber(twImg, 0, 0, XSIZE, 50, off, 0, XSIZE);
            boolean grabbed = true;
            try {
                pg.grabPixels();
            } catch (InterruptedException _ex) {
                grabbed = false;
            }
            if (grabbed) {
                int i = 1;
                for (int keep = 0; keep < XSIZE * 50 && i < 999; ) {
                    int cnt;
                    for (cnt = 0; keep < XSIZE * 50 && (off[keep++] & 0xffffff) == 0; cnt++) ;
                    tw_words[tw_count][i++] = cnt + 1;
                    for (cnt = 1; keep < XSIZE * 50 && (off[keep++] & 0xffffff) != 0; cnt++) ;
                    tw_words[tw_count][i++] = cnt;
                }
                tw_words[tw_count][0] = i - 1;
                tw_count++;
            }
        }
        tw_max = tw_count;
        twGrp = null;
        twImg.flush();
        twImg = null;
        tw_count = 0;
        tw_timer = (System.currentTimeMillis() + 4000L) - 1000L;
    }

    void tw_process() {
        long ctime = System.currentTimeMillis();
        if (ctime <= tw_timer) {
            if (ctime < tw_timer - 1000L) {
                int i = 1;
                int offset = vWORD_OFFSET;
                for (int max = tw_words[tw_count][0]; i < max; ) {
                    offset += tw_words[tw_count][i++];
                    for (int count = tw_words[tw_count][i++]; --count >= 0; ) off[offset++] = tw_color[rand(30)];
                }
            }
        } else {
            if (++tw_count >= tw_max) tw_count = 0;
            tw_timer = System.currentTimeMillis() + 4000L;
        }
    }

    public void paint(Graphics g) {
        g.setFont(new Font("Helvetica", 1, 9));
        g.setColor(new Color(120, 120, 50));
        g.drawString("                                      ", XSIZE / 2 - 90, (YSIZE + gYSIZE) - 1);
        art = createImage(new MemoryImageSource(XSIZE, YSIZE, off, 0, XSIZE));
        g.drawImage(art, 0, 0, null);
        art.flush();
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void init() {
        XSIZE = size().width;
        YSIZE = size().height - 11;
        XE = XSIZE - 1;
        gXSIZE = XSIZE;
        gYSIZE = 11;
        BOTTOM = XSIZE * (YSIZE - 1);
        vWORD_OFFSET = XSIZE * (YSIZE / 2 - 25);
        setLayout(null);
        resize(XSIZE, YSIZE + gYSIZE);
        setBackground(new Color(0, 20, 40));
        off = new int[XSIZE * YSIZE];
    }

    public void start() {
        init();
        makepal();
        motor = new Thread(this);
        motor.setPriority(1);
        motor.start();
    }

    public void stop() {
        motor.stop();
    }

    void clearOff() {
        for (int i = XSIZE * YSIZE - 1; i >= 0; i--) off[i] = 0xff000000;
    }

    public void run() {
        tw_begin();
        clearOff();
        do {
            long cc = System.currentTimeMillis() + 40L;
            process();
            tw_process();
            repaint();
            try {
                Thread.sleep(1L);
            } catch (InterruptedException _ex) {
            }
            while (System.currentTimeMillis() < cc) ;
        } while (true);
    }

    public FlamePanel() {
        super();
        XE = 249;
        pal = new int[256];
        rd = new Random();
        tw_color = new int[30];
    }

    static final int XS = 1;

    static final int YS = 1;

    static final int XB = 15;

    static final int SMOOTH = 1;

    static final int decay = 6;

    int XE;

    int XSIZE;

    int gXSIZE;

    int YSIZE;

    int gYSIZE;

    int BOTTOM;

    int vWORD_OFFSET;

    static final int MAXPAL = 256;

    int pal[];

    Random rd;

    Image art;

    Thread motor;

    int off[];

    int count;

    int burn;

    static final long TW_DLAY = 4000L;

    static final long TW_FADE = 1000L;

    static final int TW_LEN = 1000;

    static final int TW_MAXCOLOR = 30;

    static final int YLEN = 50;

    long tw_timer;

    int tw_words[][];

    int tw_max;

    int tw_count;

    int tw_color[];

    boolean tw_isLink;

    String tw_link;

    private static String Message;

    public static void main(String[] args) throws Exception {
    }
}
