package com.paracel.tt.io;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class TipFile {

    private String file_name = "unknown";

    public int num_peaks;

    public IntrinsicPeak peak_list[];

    private long bytesRead;

    private long fileLength;

    private JProgressBar progressBar;

    boolean sleepNeeded;

    /** Total intrinsic signal peak list. */
    private IntrinsicPeak totalSignalPeaks[];

    private int maxX = 0;

    int shiftX;

    public class IntrinsicPeak implements Cloneable {

        public char base;

        public int type;

        public int length;

        public int x[];

        public int y[];

        public IntrinsicPeak() {
        }

        public IntrinsicPeak(char b, int typ, int beg, int sig[]) {
            base = b;
            type = typ;
            length = sig.length;
            x = new int[length];
            y = new int[length];
            for (int i = 0; i < length; i++) {
                x[i] = i + beg;
            }
            System.arraycopy(sig, 0, y, 0, length);
        }

        public IntrinsicPeak(IntrinsicPeak target) {
            this(target.base, target.type, target.x[0], target.y);
        }

        public Object clone() {
            return new IntrinsicPeak(this);
        }

        public void dispose() {
            x = null;
            y = null;
        }

        public int getBase() {
            return base;
        }

        public int getType() {
            return type;
        }

        public int getLength() {
            return length;
        }

        public int[] getXValues() {
            return x;
        }

        public int[] getYValues() {
            return y;
        }
    }

    public TipFile(String name, JFrame frame) throws FileNotFoundException, IOException {
        JDialog progressWindow = null;
        if (frame != null) {
            progressWindow = new JDialog(frame, "Loading tip file");
            progressWindow.setSize(300, 50);
            UIManager.put("ProgressBar.selectionBackground", Color.black);
            UIManager.put("ProgressBar.selectionForeground", Color.white);
            UIManager.put("ProgressBar.foreground", new Color(8, 32, 128));
            progressBar = new JProgressBar();
            progressBar.setMinimum(0);
            progressBar.setStringPainted(false);
            progressBar.setSize(300, 20);
            progressWindow.getContentPane().add(progressBar, BorderLayout.CENTER);
            int x1 = (int) frame.getLocation().getX();
            int y1 = (int) frame.getLocation().getY();
            int w1 = frame.getWidth();
            int h1 = frame.getHeight();
            int w2 = progressWindow.getWidth();
            int h2 = progressWindow.getHeight();
            int x2 = x1 + (w1 - w2) / 2;
            int y2 = y1 + (h1 - h2) / 2;
            progressWindow.setLocation(x2, y2);
            progressWindow.setResizable(false);
            progressWindow.setVisible(true);
            String osName = System.getProperty("os.name");
            if (osName != null) {
                osName = osName.toLowerCase().trim();
                if (osName.indexOf("windows") >= 0) {
                    sleepNeeded = true;
                } else {
                    sleepNeeded = false;
                }
            }
        }
        RandomAccessFile f = new RandomAccessFile(name, "r");
        ArrayList peaks = new ArrayList(3000);
        int num_lines = 0;
        char b = 'N';
        int typ = 0, beg = 0, len = 0, ipos = 0, num = 0, sig[];
        int counter = 0;
        bytesRead = 0;
        fileLength = f.length();
        if (progressBar != null) {
            progressBar.setMaximum((int) fileLength);
        }
        int percentageComplete = 0;
        String s;
        String line = f.readLine();
        if (line == null || !line.startsWith(">")) {
            throw new IOException(name + " does not seem to be a valid .tip file");
        } else {
            bytesRead += line.length();
            line = line.trim();
            num_lines++;
        }
        int i = 0;
        line = f.readLine();
        bytesRead += line.length();
        if (line != null) {
            line = line.trim();
        }
        while (line != null) {
            StringTokenizer st = new StringTokenizer(line);
            if (i % 2 == 0) {
                if (st.hasMoreTokens()) {
                    b = st.nextToken().charAt(0);
                }
                try {
                    if (st.hasMoreTokens()) {
                        ipos = Integer.parseInt(st.nextToken());
                    }
                    if (st.hasMoreTokens()) {
                        num = Integer.parseInt(st.nextToken());
                        len = Math.max(0, 2 * num - 1);
                    }
                    beg = ipos - num + 1;
                    if (st.hasMoreTokens()) {
                        st.nextToken();
                    }
                    if (st.hasMoreTokens()) {
                        st.nextToken();
                    }
                    if (st.hasMoreTokens()) {
                        st.nextToken();
                    }
                    if (st.hasMoreTokens()) {
                        typ = Integer.parseInt(st.nextToken());
                    }
                } catch (Exception e) {
                }
            } else {
                sig = new int[len];
                for (int j = num - 1; j >= 0; j--) {
                    try {
                        if (st.hasMoreTokens()) {
                            sig[j] = Integer.parseInt(st.nextToken());
                            sig[len - 1 - j] = sig[j];
                        }
                    } catch (Exception e) {
                    }
                }
                peaks.add(new IntrinsicPeak(b, typ, beg, sig));
                if (maxX < (beg + sig.length - 1)) {
                    maxX = beg + sig.length - 1;
                }
            }
            i++;
            line = f.readLine();
            if (line != null) {
                if (progressBar != null) {
                    bytesRead += line.length();
                    if (bytesRead * 100 / fileLength >= counter) {
                        setProgressValue(bytesRead);
                        counter += 10;
                    }
                }
                line = line.trim();
            }
        }
        f.close();
        num_peaks = peaks.size();
        peak_list = new IntrinsicPeak[num_peaks];
        for (i = 0; i < num_peaks; i++) {
            peak_list[i] = (IntrinsicPeak) (peaks.get(i));
        }
        peaks = null;
        IntrinsicPeak firstPeak = peak_list[0];
        shiftX = (firstPeak.x[0] < 0) ? -firstPeak.x[0] : 0;
        int maxLen = maxX + 1 + shiftX;
        int[] ASignals = new int[maxLen];
        int[] CSignals = new int[maxLen];
        int[] GSignals = new int[maxLen];
        int[] TSignals = new int[maxLen];
        IntrinsicPeak peak;
        int x;
        for (i = 0; i < peak_list.length; i++) {
            peak = peak_list[i];
            for (int j = 0; j < peak.x.length; j++) {
                x = peak.x[j] + shiftX;
                if (x < 0) {
                    continue;
                }
                if (x >= maxLen) {
                    break;
                }
                switch(peak.base) {
                    case 'A':
                        ASignals[x] += peak.y[j];
                        break;
                    case 'C':
                        CSignals[x] += peak.y[j];
                        break;
                    case 'G':
                        GSignals[x] += peak.y[j];
                        break;
                    case 'T':
                        TSignals[x] += peak.y[j];
                        break;
                }
            }
        }
        IntrinsicPeak[] totalSPeaks = new IntrinsicPeak[peak_list.length];
        IntrinsicPeak temp;
        int index = 0, numOfPeaksFound = 0;
        int AFromIndex = 0, CFromIndex = 0, GFromIndex = 0, TFromIndex = 0;
        while (true) {
            numOfPeaksFound = 0;
            temp = findPeak('A', ASignals, AFromIndex);
            if (temp != null) {
                totalSPeaks[index++] = temp;
                AFromIndex = temp.x[temp.x.length - 1] + shiftX;
                numOfPeaksFound++;
            } else {
                AFromIndex = ASignals.length;
            }
            temp = findPeak('C', CSignals, CFromIndex);
            if (temp != null) {
                totalSPeaks[index++] = temp;
                CFromIndex = temp.x[temp.x.length - 1] + shiftX;
                numOfPeaksFound++;
            } else {
                CFromIndex = CSignals.length;
            }
            temp = findPeak('G', GSignals, GFromIndex);
            if (temp != null) {
                totalSPeaks[index++] = temp;
                GFromIndex = temp.x[temp.x.length - 1] + shiftX;
                numOfPeaksFound++;
            } else {
                GFromIndex = GSignals.length;
            }
            temp = findPeak('T', TSignals, TFromIndex);
            if (temp != null) {
                totalSPeaks[index++] = temp;
                TFromIndex = temp.x[temp.x.length - 1] + shiftX;
                numOfPeaksFound++;
            } else {
                TFromIndex = TSignals.length;
            }
            if (numOfPeaksFound == 0) {
                break;
            }
            if (index >= totalSPeaks.length) {
                break;
            }
        }
        totalSignalPeaks = new IntrinsicPeak[index];
        System.arraycopy(totalSPeaks, 0, totalSignalPeaks, 0, index);
        totalSPeaks = null;
        if (frame != null) {
            setProgressValue(fileLength);
            progressWindow.setVisible(false);
            progressWindow.dispose();
            progressWindow = null;
            System.gc();
        }
    }

    protected IntrinsicPeak findPeak(char base, int[] signals, int fromIndex) {
        if (fromIndex < 0 || signals == null || fromIndex >= signals.length - 1) {
            return null;
        }
        int start = -1, end = -1;
        if (signals[fromIndex] > 0) {
            start = fromIndex;
        } else {
            for (int i = fromIndex; i < signals.length - 1; i++) {
                if (signals[i] == 0 && signals[i + 1] > 0) {
                    start = i;
                    break;
                }
            }
        }
        if (start == -1) {
            return null;
        }
        for (int i = start + 1; i < signals.length - 1; i++) {
            if (signals[i] > 0 && signals[i + 1] == 0) {
                end = i + 1;
                break;
            } else {
                if (i - start + 1 >= 100) {
                    end = i;
                    break;
                }
            }
        }
        if (end == -1 && signals[signals.length - 1] > 0) {
            end = signals.length - 1;
        }
        if (end < start) {
            return null;
        }
        int[] sig = new int[end - start + 1];
        System.arraycopy(signals, start, sig, 0, sig.length);
        return (new IntrinsicPeak(base, 0, start - shiftX, sig));
    }

    protected void sleep(long ms) {
        try {
            Thread.currentThread().sleep(ms);
        } catch (Exception e) {
            System.err.println("TipFile--Constructor" + e.toString());
            e.printStackTrace(System.err);
        }
    }

    protected void setProgressValue(final long value) {
        if (progressBar == null) {
            return;
        }
        if (sleepNeeded) {
            sleep(1);
        }
        progressBar.setValue((int) value);
    }

    /** Attempts to release the resources. */
    public void releaseResources() {
        for (int i = 0; i < peak_list.length; i++) {
            if (peak_list[i] != null) {
                peak_list[i].dispose();
                peak_list[i] = null;
            }
        }
        peak_list = null;
        for (int i = 0; i < totalSignalPeaks.length; i++) {
            if (totalSignalPeaks[i] != null) {
                totalSignalPeaks[i].dispose();
                totalSignalPeaks[i] = null;
            }
        }
        totalSignalPeaks = null;
    }

    public String getName() {
        return file_name;
    }

    public int getNumPeaks() {
        return num_peaks;
    }

    public IntrinsicPeak[] getPeaks() {
        return peak_list;
    }

    public IntrinsicPeak[] getTotalSignalPeaks() {
        return totalSignalPeaks;
    }

    public int getNumTotalSignalPeaks() {
        return totalSignalPeaks.length;
    }

    public static void main(String[] args) {
        TipFile t;
        int i, num_peaks;
        char b;
        int typ, beg, len;
        int sig[];
        if (args.length != 1) {
            System.out.println("Usage: java TipFile <filename>");
            System.exit(0);
        }
        try {
            t = new TipFile(args[0], null);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
        System.out.println("Input file is: " + args[0]);
        System.out.println("Number of peaks is " + t.getNumPeaks());
        System.out.println("Number of total intrinsic signal peaks is " + t.getNumTotalSignalPeaks());
    }
}
