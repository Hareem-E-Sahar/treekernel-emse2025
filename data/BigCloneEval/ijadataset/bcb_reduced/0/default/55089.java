import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Calendar;
import java.awt.*;
import java.text.*;

public class NMEA implements Runnable {

    private NMEAdata NMEAd;

    static final int NLine = 20;

    static final int GOTPOS = 21;

    static final int GOTHEADING = 22;

    static final int GOTPOSHEADING = 23;

    static final int GOTPOSTIME = 24;

    static final int GOTTIME = 25;

    int lindex = 0;

    int nline = 0;

    static int tok = 0;

    String inputLine[] = new String[NLine];

    Thread dispThread = new Thread(new NMEAdisplay(this), "NMEA display");

    public NMEA(ImageGMap IGM, Label nl) {
        NMEAd = new NMEAdata(IGM, nl);
    }

    public void run() {
        Thread thisThread = Thread.currentThread();
        Thread deadThread;
        System.out.println("  NMEAPos start: ");
        NMEAd.NMEAinit();
        if (dispThread != null) {
            CAux.perr("Dispthread start", 3);
            dispThread.start();
        }
        Garmincomm.serialOpen(Garmincomm.NMEA_comm);
        CAux.perr("NMEA opened", 3);
        while (GPS.NMEAThread == thisThread) {
            String il = "";
            try {
                il = (Garmincomm.nmeaIn).readLine();
                Stat.tline++;
            } catch (IOException ioe) {
                System.out.println("IOException NMEA: " + ioe);
            }
            synchronized (this) {
                inputLine[lindex] = il;
                if (lindex > NLine) {
                    lindex = 0;
                }
                if (nline < NLine) {
                    nline++;
                    notify();
                } else {
                    String nullStr;
                    int ns;
                    Stat.missed++;
                }
                lindex++;
                if (lindex >= NLine) {
                    lindex = 0;
                }
            }
        }
        deadThread = dispThread;
        dispThread = null;
        if (deadThread != null) {
            deadThread.interrupt();
        }
    }

    void display() {
        boolean gotTime = false;
        boolean gotPos = false;
        boolean gotHeading = false;
        String dline;
        long oldtime = 0, thistime;
        while (dispThread != null) {
            synchronized (this) {
                int ol;
                while (nline < 1) {
                    try {
                        CAux.perr("NMEA wait", 2);
                        wait();
                    } catch (InterruptedException ie) {
                        CAux.perr("NMEA wake up", 2);
                    }
                }
                ol = lindex - nline;
                if (ol < 0) {
                    ol = ol + NLine;
                }
                tok++;
                dline = inputLine[ol];
                nline--;
            }
            thistime = System.currentTimeMillis();
            if ((thistime - oldtime) / 1000 > GMenv.NMEAInterval) {
                int lres;
                if (CAux.tLevel > 2) {
                    CAux.perr("NMEA dT=" + thistime + "-" + oldtime + " =" + (thistime - oldtime) / 1000 + " I=" + GMenv.NMEAInterval, 1);
                }
                lres = NMEAd.processLine(dline, true, gotTime || (GMenv.NMEAInterval == 0), !gotPos);
                if (lres == GOTPOS || lres == GOTPOSTIME || lres == GOTPOSHEADING) {
                    gotPos = true;
                }
                if (lres == GOTHEADING || lres == GOTPOSHEADING) {
                    gotHeading = true;
                }
                if (lres == GOTPOSTIME || lres == GOTTIME) {
                    gotTime = true;
                }
                if (gotPos && gotTime && gotHeading) {
                    oldtime = thistime;
                    gotHeading = false;
                    gotPos = false;
                    gotTime = false;
                }
            }
        }
    }
}

class NMEAdisplay implements Runnable {

    NMEA NMEAproducer;

    NMEAdisplay(NMEA np) {
        NMEAproducer = np;
    }

    public void run() {
        CAux.perr("nmea DispThread init", 3);
        NMEAproducer.display();
    }
}
