package tico.editor.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import javax.swing.*;
import tico.components.resources.SoundFilter;
import tico.configuration.TLanguage;
import tico.editor.TEditor;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import javax.sound.sampled.*;
import java.awt.font.*;
import java.text.*;

public class TRecordSound extends JPanel implements ActionListener {

    File CurrentDirectory = null;

    public static File text = null;

    final int bufSize = 16384;

    FormatControls formatControls = new FormatControls();

    Capture capture = new Capture();

    Playback playback = new Playback();

    AudioInputStream audioInputStream;

    SamplingGraph samplingGraph;

    JButton playB, captB, pausB, loadB;

    JButton auB, aiffB, waveB;

    JTextField textField;

    String fileName = "untitled";

    String errStr;

    double duration, seconds;

    File file;

    Vector lines = new Vector();

    String extension;

    public TRecordSound() {
        setLayout(new BorderLayout());
        EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
        SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.LOWERED);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        JPanel p2 = new JPanel();
        p2.setBorder(sbb);
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
        playB = addButton(TLanguage.getString("TRecordSound.PLAY"), buttonsPanel, false);
        captB = addButton(TLanguage.getString("TRecordSound.RECORD"), buttonsPanel, true);
        pausB = addButton(TLanguage.getString("TRecordSound.PAUSE"), buttonsPanel, false);
        p2.add(buttonsPanel);
        JPanel samplingPanel = new JPanel(new BorderLayout());
        eb = new EmptyBorder(10, 20, 20, 20);
        samplingPanel.setBorder(new CompoundBorder(eb, sbb));
        samplingPanel.add(samplingGraph = new SamplingGraph());
        p2.add(samplingPanel);
        JPanel savePanel = new JPanel();
        savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.Y_AXIS));
        JPanel saveBpanel = new JPanel();
        waveB = addButton(TLanguage.getString("TRecordSound.SAVE"), saveBpanel, false);
        savePanel.add(saveBpanel);
        p2.add(savePanel);
        p1.add(p2);
        add(p1);
    }

    public void open() {
    }

    public void close() {
        if (playback.thread != null) {
            playB.doClick(0);
        }
        if (capture.thread != null) {
            captB.doClick(0);
        }
    }

    private JButton addButton(String name, JPanel p, boolean state) {
        JButton b = new JButton(name);
        b.addActionListener(this);
        b.setEnabled(state);
        p.add(b);
        return b;
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj.equals(waveB)) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(TLanguage.getString("TRecordSound.TITLE"));
            chooser.setCurrentDirectory(CurrentDirectory);
            chooser.addChoosableFileFilter(new SoundFilter());
            chooser.setAcceptAllFileFilterUsed(false);
            int returnVal = chooser.showSaveDialog(getParent());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                CurrentDirectory = chooser.getCurrentDirectory();
                saveToFile(chooser.getName(chooser.getSelectedFile()), AudioFileFormat.Type.WAVE);
            } else {
                text = null;
            }
        } else if (obj.equals(playB)) {
            if (playB.getText().startsWith(TLanguage.getString("TRecordSound.PLAY"))) {
                playback.start();
                samplingGraph.start();
                captB.setEnabled(false);
                pausB.setEnabled(true);
                playB.setText(TLanguage.getString("TRecordSound.STOP"));
            } else {
                playback.stop();
                samplingGraph.stop();
                captB.setEnabled(true);
                pausB.setEnabled(false);
                playB.setText(TLanguage.getString("TRecordSound.PLAY"));
            }
        } else if (obj.equals(captB)) {
            if (captB.getText().startsWith(TLanguage.getString("TRecordSound.RECORD"))) {
                file = null;
                capture.start();
                fileName = "untitled";
                samplingGraph.start();
                playB.setEnabled(false);
                pausB.setEnabled(true);
                waveB.setEnabled(false);
                captB.setText(TLanguage.getString("TRecordSound.STOP"));
            } else {
                lines.removeAllElements();
                capture.stop();
                samplingGraph.stop();
                playB.setEnabled(true);
                pausB.setEnabled(false);
                waveB.setEnabled(true);
                captB.setText(TLanguage.getString("TRecordSound.RECORD"));
            }
        } else if (obj.equals(pausB)) {
            if (pausB.getText().startsWith(TLanguage.getString("TRecordSound.PAUSE"))) {
                if (capture.thread != null) {
                    capture.line.stop();
                } else {
                    if (playback.thread != null) {
                        playback.line.stop();
                    }
                }
                pausB.setText(TLanguage.getString("TRecordSound.RESUME"));
            } else {
                if (capture.thread != null) {
                    capture.line.start();
                } else {
                    if (playback.thread != null) {
                        playback.line.start();
                    }
                }
                pausB.setText(TLanguage.getString("TRecordSound.PAUSE"));
            }
        }
    }

    public void createAudioInputStream(File file, boolean updateComponents) {
        if (file != null && file.isFile()) {
            try {
                this.file = file;
                errStr = null;
                audioInputStream = AudioSystem.getAudioInputStream(file);
                playB.setEnabled(true);
                fileName = file.getName();
                long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
                duration = milliseconds / 1000.0;
                waveB.setEnabled(true);
                if (updateComponents) {
                    formatControls.setFormat(audioInputStream.getFormat());
                    samplingGraph.createWaveForm(null);
                }
            } catch (Exception ex) {
                reportStatus(ex.toString());
            }
        } else {
            reportStatus("Audio file required.");
        }
    }

    public static String getExtension(String s) {
        String ext = null;
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public void saveToFile(String name, AudioFileFormat.Type fileType) {
        if (audioInputStream == null) {
            reportStatus("No loaded audio to save");
            return;
        } else if (file != null) {
            createAudioInputStream(file, false);
        }
        try {
            audioInputStream.reset();
        } catch (Exception e) {
            reportStatus("Unable to reset stream " + e);
            return;
        }
        extension = getExtension(name);
        if (extension == null) {
            name = name + ".wav";
        }
        File file = new File(fileName = CurrentDirectory + File.separator + name);
        text = file;
        try {
            if (AudioSystem.write(audioInputStream, fileType, file) == -1) {
                throw new IOException("Problems writing to file");
            }
        } catch (Exception ex) {
            reportStatus(ex.toString());
        }
        samplingGraph.repaint();
    }

    private void reportStatus(String msg) {
        if ((errStr = msg) != null) {
            System.out.println(errStr);
            samplingGraph.repaint();
        }
    }

    /**
	     * Write data to the OutputChannel.
	     */
    public class Playback implements Runnable {

        SourceDataLine line;

        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Playback");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        private void shutDown(String message) {
            if ((errStr = message) != null) {
                System.err.println(errStr);
                samplingGraph.repaint();
            }
            if (thread != null) {
                thread = null;
                samplingGraph.stop();
                captB.setEnabled(true);
                pausB.setEnabled(false);
                playB.setText("Play");
            }
        }

        public void run() {
            if (file != null) {
                createAudioInputStream(file, false);
            }
            if (audioInputStream == null) {
                shutDown("No loaded audio to play back");
                return;
            }
            try {
                audioInputStream.reset();
            } catch (Exception e) {
                shutDown("Unable to reset the stream\n" + e);
                return;
            }
            AudioFormat format = formatControls.getFormat();
            AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);
            if (playbackInputStream == null) {
                shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
                return;
            }
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);
            } catch (LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            }
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead = 0;
            line.start();
            while (thread != null) {
                try {
                    if ((numBytesRead = playbackInputStream.read(data)) == -1) {
                        break;
                    }
                    int numBytesRemaining = numBytesRead;
                    while (numBytesRemaining > 0) {
                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                    }
                } catch (Exception e) {
                    shutDown("Error during playback: " + e);
                    break;
                }
            }
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
            shutDown(null);
        }
    }

    /**
	     * Reads data from the input channel and writes to the output stream
	     */
    class Capture implements Runnable {

        TargetDataLine line;

        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Capture");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        private void shutDown(String message) {
            if ((errStr = message) != null && thread != null) {
                thread = null;
                samplingGraph.stop();
                playB.setEnabled(true);
                pausB.setEnabled(false);
                waveB.setEnabled(true);
                captB.setText("Record");
                System.err.println(errStr);
                samplingGraph.repaint();
            }
        }

        public void run() {
            duration = 0;
            audioInputStream = null;
            AudioFormat format = formatControls.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            } catch (SecurityException ex) {
                shutDown(ex.toString());
                return;
            } catch (Exception ex) {
                shutDown(ex.toString());
                return;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;
            line.start();
            while (thread != null) {
                if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
                out.write(data, 0, numBytesRead);
            }
            line.stop();
            line.close();
            line = null;
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            byte audioBytes[] = out.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
            long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
            duration = milliseconds / 1000.0;
            try {
                audioInputStream.reset();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            samplingGraph.createWaveForm(audioBytes);
        }
    }

    /**
	     * Controls for the AudioFormat.
	     */
    class FormatControls extends JPanel {

        Vector groups = new Vector();

        JToggleButton linrB, ulawB, alawB, rate8B, rate11B, rate16B, rate22B, rate44B;

        JToggleButton size8B, size16B, signB, unsignB, litB, bigB, monoB, sterB;

        public FormatControls() {
            setLayout(new GridLayout(0, 1));
            EmptyBorder eb = new EmptyBorder(0, 0, 0, 5);
            BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
            CompoundBorder cb = new CompoundBorder(eb, bb);
            setBorder(new CompoundBorder(cb, new EmptyBorder(8, 5, 5, 5)));
            JPanel p1 = new JPanel();
            ButtonGroup encodingGroup = new ButtonGroup();
            linrB = addToggleButton(p1, encodingGroup, "linear", true);
            ulawB = addToggleButton(p1, encodingGroup, "ulaw", false);
            alawB = addToggleButton(p1, encodingGroup, "alaw", false);
            add(p1);
            groups.addElement(encodingGroup);
            JPanel p2 = new JPanel();
            JPanel p2b = new JPanel();
            ButtonGroup sampleRateGroup = new ButtonGroup();
            rate8B = addToggleButton(p2, sampleRateGroup, "8000", false);
            rate11B = addToggleButton(p2, sampleRateGroup, "11025", false);
            rate16B = addToggleButton(p2b, sampleRateGroup, "16000", false);
            rate22B = addToggleButton(p2b, sampleRateGroup, "22050", false);
            rate44B = addToggleButton(p2b, sampleRateGroup, "44100", true);
            add(p2);
            add(p2b);
            groups.addElement(sampleRateGroup);
            JPanel p3 = new JPanel();
            ButtonGroup sampleSizeInBitsGroup = new ButtonGroup();
            size8B = addToggleButton(p3, sampleSizeInBitsGroup, "8", false);
            size16B = addToggleButton(p3, sampleSizeInBitsGroup, "16", true);
            add(p3);
            groups.addElement(sampleSizeInBitsGroup);
            JPanel p4 = new JPanel();
            ButtonGroup signGroup = new ButtonGroup();
            signB = addToggleButton(p4, signGroup, "signed", true);
            unsignB = addToggleButton(p4, signGroup, "unsigned", false);
            add(p4);
            groups.addElement(signGroup);
            JPanel p5 = new JPanel();
            ButtonGroup endianGroup = new ButtonGroup();
            litB = addToggleButton(p5, endianGroup, "little endian", false);
            bigB = addToggleButton(p5, endianGroup, "big endian", true);
            add(p5);
            groups.addElement(endianGroup);
            JPanel p6 = new JPanel();
            ButtonGroup channelsGroup = new ButtonGroup();
            monoB = addToggleButton(p6, channelsGroup, "mono", false);
            sterB = addToggleButton(p6, channelsGroup, "stereo", true);
            add(p6);
            groups.addElement(channelsGroup);
        }

        private JToggleButton addToggleButton(JPanel p, ButtonGroup g, String name, boolean state) {
            JToggleButton b = new JToggleButton(name, state);
            p.add(b);
            g.add(b);
            return b;
        }

        public AudioFormat getFormat() {
            Vector v = new Vector(groups.size());
            for (int i = 0; i < groups.size(); i++) {
                ButtonGroup g = (ButtonGroup) groups.get(i);
                for (Enumeration e = g.getElements(); e.hasMoreElements(); ) {
                    AbstractButton b = (AbstractButton) e.nextElement();
                    if (b.isSelected()) {
                        v.add(b.getText());
                        break;
                    }
                }
            }
            AudioFormat.Encoding encoding = AudioFormat.Encoding.ULAW;
            String encString = (String) v.get(0);
            float rate = Float.valueOf((String) v.get(1)).floatValue();
            int sampleSize = Integer.valueOf((String) v.get(2)).intValue();
            String signedString = (String) v.get(3);
            boolean bigEndian = ((String) v.get(4)).startsWith("big");
            int channels = ((String) v.get(5)).equals("mono") ? 1 : 2;
            if (encString.equals("linear")) {
                if (signedString.equals("signed")) {
                    encoding = AudioFormat.Encoding.PCM_SIGNED;
                } else {
                    encoding = AudioFormat.Encoding.PCM_UNSIGNED;
                }
            } else if (encString.equals("alaw")) {
                encoding = AudioFormat.Encoding.ALAW;
            }
            return new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
        }

        public void setFormat(AudioFormat format) {
            AudioFormat.Encoding type = format.getEncoding();
            if (type == AudioFormat.Encoding.ULAW) {
                ulawB.doClick();
            } else if (type == AudioFormat.Encoding.ALAW) {
                alawB.doClick();
            } else if (type == AudioFormat.Encoding.PCM_SIGNED) {
                linrB.doClick();
                signB.doClick();
            } else if (type == AudioFormat.Encoding.PCM_UNSIGNED) {
                linrB.doClick();
                unsignB.doClick();
            }
            float rate = format.getFrameRate();
            if (rate == 8000) {
                rate8B.doClick();
            } else if (rate == 11025) {
                rate11B.doClick();
            } else if (rate == 16000) {
                rate16B.doClick();
            } else if (rate == 22050) {
                rate22B.doClick();
            } else if (rate == 44100) {
                rate44B.doClick();
            }
            switch(format.getSampleSizeInBits()) {
                case 8:
                    size8B.doClick();
                    break;
                case 16:
                    size16B.doClick();
                    break;
            }
            if (format.isBigEndian()) {
                bigB.doClick();
            } else {
                litB.doClick();
            }
            if (format.getChannels() == 1) {
                monoB.doClick();
            } else {
                sterB.doClick();
            }
        }
    }

    /**
	     * Render a WaveForm.
	     */
    class SamplingGraph extends JPanel implements Runnable {

        private Thread thread;

        private Font font10 = new Font("serif", Font.PLAIN, 10);

        private Font font12 = new Font("serif", Font.PLAIN, 12);

        Color jfcBlue = new Color(204, 204, 255);

        Color pink = new Color(255, 175, 175);

        public SamplingGraph() {
            setBackground(new Color(20, 20, 20));
        }

        public void createWaveForm(byte[] audioBytes) {
            lines.removeAllElements();
            AudioFormat format = audioInputStream.getFormat();
            if (audioBytes == null) {
                try {
                    audioBytes = new byte[(int) (audioInputStream.getFrameLength() * format.getFrameSize())];
                    audioInputStream.read(audioBytes);
                } catch (Exception ex) {
                    reportStatus(ex.toString());
                    return;
                }
            }
            Dimension d = getSize();
            int w = d.width;
            int h = d.height - 15;
            int[] audioData = null;
            if (format.getSampleSizeInBits() == 16) {
                int nlengthInSamples = audioBytes.length / 2;
                audioData = new int[nlengthInSamples];
                if (format.isBigEndian()) {
                    for (int i = 0; i < nlengthInSamples; i++) {
                        int MSB = (int) audioBytes[2 * i];
                        int LSB = (int) audioBytes[2 * i + 1];
                        audioData[i] = MSB << 8 | (255 & LSB);
                    }
                } else {
                    for (int i = 0; i < nlengthInSamples; i++) {
                        int LSB = (int) audioBytes[2 * i];
                        int MSB = (int) audioBytes[2 * i + 1];
                        audioData[i] = MSB << 8 | (255 & LSB);
                    }
                }
            } else if (format.getSampleSizeInBits() == 8) {
                int nlengthInSamples = audioBytes.length;
                audioData = new int[nlengthInSamples];
                if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                    for (int i = 0; i < audioBytes.length; i++) {
                        audioData[i] = audioBytes[i];
                    }
                } else {
                    for (int i = 0; i < audioBytes.length; i++) {
                        audioData[i] = audioBytes[i] - 128;
                    }
                }
            }
            int frames_per_pixel = audioBytes.length / format.getFrameSize() / w;
            byte my_byte = 0;
            double y_last = 0;
            int numChannels = format.getChannels();
            for (double x = 0; x < w && audioData != null; x++) {
                int idx = (int) (frames_per_pixel * numChannels * x);
                if (format.getSampleSizeInBits() == 8) {
                    my_byte = (byte) audioData[idx];
                } else {
                    my_byte = (byte) (128 * audioData[idx] / 32768);
                }
                double y_new = (double) (h * (128 - my_byte) / 256);
                lines.add(new Line2D.Double(x, y_last, x, y_new));
                y_last = y_new;
            }
            repaint();
        }

        public void paint(Graphics g) {
            Dimension d = getSize();
            int w = d.width;
            int h = d.height;
            int INFOPAD = 15;
            Graphics2D g2 = (Graphics2D) g;
            g2.setBackground(getBackground());
            g2.clearRect(0, 0, w, h);
            g2.setColor(Color.white);
            g2.fillRect(0, h - INFOPAD, w, INFOPAD);
            if (errStr != null) {
                g2.setColor(jfcBlue);
                g2.setFont(new Font("serif", Font.BOLD, 18));
                g2.drawString("ERROR", 5, 20);
                AttributedString as = new AttributedString(errStr);
                as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
                AttributedCharacterIterator aci = as.getIterator();
                FontRenderContext frc = g2.getFontRenderContext();
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
                float x = 5, y = 25;
                lbm.setPosition(0);
                while (lbm.getPosition() < errStr.length()) {
                    TextLayout tl = lbm.nextLayout(w - x - 5);
                    if (!tl.isLeftToRight()) {
                        x = w - tl.getAdvance();
                    }
                    tl.draw(g2, x, y += tl.getAscent());
                    y += tl.getDescent() + tl.getLeading();
                }
            } else if (capture.thread != null) {
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("Length: " + String.valueOf(seconds), 3, h - 4);
            } else {
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("File: " + fileName + "  Length: " + String.valueOf(duration) + "  Position: " + String.valueOf(seconds), 3, h - 4);
                if (audioInputStream != null) {
                    g2.setColor(jfcBlue);
                    for (int i = 1; i < lines.size(); i++) {
                        g2.draw((Line2D) lines.get(i));
                    }
                    if (seconds != 0) {
                        double loc = seconds / duration * w;
                        g2.setColor(pink);
                        g2.setStroke(new BasicStroke(3));
                        g2.draw(new Line2D.Double(loc, 0, loc, h - INFOPAD - 2));
                    }
                }
            }
        }

        public void start() {
            thread = new Thread(this);
            thread.setName("SamplingGraph");
            thread.start();
            seconds = 0;
        }

        public void stop() {
            if (thread != null) {
                thread.interrupt();
            }
            thread = null;
        }

        public void run() {
            seconds = 0;
            while (thread != null) {
                if ((playback.line != null) && (playback.line.isOpen())) {
                    long milliseconds = (long) (playback.line.getMicrosecondPosition() / 1000);
                    seconds = milliseconds / 1000.0;
                } else if ((capture.line != null) && (capture.line.isActive())) {
                    long milliseconds = (long) (capture.line.getMicrosecondPosition() / 1000);
                    seconds = milliseconds / 1000.0;
                }
                try {
                    thread.sleep(100);
                } catch (Exception e) {
                    break;
                }
                repaint();
                while ((capture.line != null && !capture.line.isActive()) || (playback.line != null && !playback.line.isOpen())) {
                    try {
                        thread.sleep(10);
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            seconds = 0;
            repaint();
        }
    }

    public static void main(String[] args) {
        TRecordSound capturePlayback = new TRecordSound();
        capturePlayback.open();
        JFrame f = new JFrame("Capture/Playback");
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.getContentPane().add("Center", capturePlayback);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 720;
        int h = 340;
        f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        f.setSize(w, h);
        f.setVisible(true);
    }
}
