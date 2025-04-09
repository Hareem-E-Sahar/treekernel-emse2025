package com.degani.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.degani.AppMain;
import com.degani.recording.AudioRecorder;
import com.degani.recording.GraphicsRecorder;

public class RecordButton extends RoundedButton implements ActionListener {

    private GraphicsRecorder graphicsRecorder;

    private AudioRecorder audioRecorder;

    private String selectedFile;

    /**
	 *  Temp location for graphics data before storage
	 */
    private String graphicsTempFileName;

    /**
     *  Temp location for audio data before storage    
     */
    private String audioTempFileName;

    /**
     * Checksum for audio file
     */
    private CRC32 audioChecksum;

    static final int BUFFER_SIZE = 8192;

    private Color black;

    private Color red;

    public RecordButton() {
        red = new Color(255, 0, 0, 70);
        black = new Color(0, 0, 0, 70);
        addActionListener(this);
        setPreferredSize(new Dimension(32, 32));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int spacing = 5;
        if (graphicsRecorder != null && graphicsRecorder.isStarted()) {
            g.setColor(black);
            g.fillRect(spacing, spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
        } else {
            g.setColor(red);
            g.fillOval(spacing, spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (graphicsRecorder != null && graphicsRecorder.isStarted()) {
            try {
                graphicsRecorder.stop();
                graphicsRecorder = null;
                audioRecorder.finished();
                audioRecorder = null;
                ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(selectedFile), BUFFER_SIZE));
                File graphicsTempFile = new File(graphicsTempFileName);
                File audioTempFile = new File(audioTempFileName);
                BufferedInputStream graphicsStream = new BufferedInputStream(new FileInputStream(graphicsTempFile), BUFFER_SIZE);
                BufferedInputStream audioStream = new BufferedInputStream(new FileInputStream(audioTempFile), BUFFER_SIZE);
                int count;
                byte buf[] = new byte[BUFFER_SIZE];
                zout.setMethod(ZipOutputStream.DEFLATED);
                zout.putNextEntry(new ZipEntry("graphics"));
                while ((count = graphicsStream.read(buf, 0, BUFFER_SIZE)) != -1) {
                    zout.write(buf, 0, count);
                }
                graphicsStream.close();
                zout.setMethod(ZipOutputStream.STORED);
                ZipEntry audioEntry = new ZipEntry("audio");
                audioEntry.setSize(audioTempFile.length());
                audioEntry.setCrc(audioChecksum.getValue());
                zout.putNextEntry(audioEntry);
                while ((count = audioStream.read(buf, 0, BUFFER_SIZE)) != -1) {
                    zout.write(buf, 0, count);
                }
                audioStream.close();
                zout.close();
                graphicsTempFile.delete();
                audioTempFile.delete();
                selectedFile = null;
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(AppMain.getRootContentPane(), "Could not close recording session!", "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        } else {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showSaveDialog(AppMain.getRootContentPane());
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    selectedFile = chooser.getSelectedFile().getAbsolutePath();
                    String uid = UUID.randomUUID().toString();
                    graphicsTempFileName = String.format("%s.%s.graphics", selectedFile, uid);
                    graphicsRecorder = new GraphicsRecorder(new FileOutputStream(graphicsTempFileName));
                    audioTempFileName = String.format("%s.%s.audio", selectedFile, uid);
                    audioChecksum = new CRC32();
                    audioRecorder = new AudioRecorder(AppMain.getAudioInput(), new CheckedOutputStream(new FileOutputStream(audioTempFileName), audioChecksum));
                    graphicsRecorder.start();
                    audioRecorder.start();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(AppMain.getRootContentPane(), "Could not open file!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        repaint();
    }
}
