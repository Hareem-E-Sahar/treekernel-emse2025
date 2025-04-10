package com.frinika.tools;

import java.io.IOException;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.server.AudioClient;
import com.frinika.project.FrinikaAudioSystem;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.FrinikaSequencer;

public class BufferedPlayback implements Runnable {

    private ProjectContainer project;

    private JFrame frame;

    MyMidiRenderer midiRenderer;

    volatile boolean active = false;

    Thread thread = null;

    Thread playthread = null;

    AudioBuffer buffer;

    AudioProcess outputprocess;

    byte[] bytebuffer;

    int pipeSize = (int) (10 * 44100 * 2 * 1);

    int pipeWaitSize = (int) (10 * 44100 * 2 * 1);

    byte[] circularbuffer = new byte[pipeSize];

    int circularbuffer_read_pos = 0;

    int circularbuffer_write_pos = 0;

    int circularbuffer_avail = 0;

    public BufferedPlayback(JFrame frame, ProjectContainer project) {
        this.frame = frame;
        this.project = project;
        buffer = project.getAudioServer().createAudioBuffer("BufferPlayback");
        outputprocess = project.getOutputProcess();
        bytebuffer = new byte[buffer.getSampleCount() * 2 * 2];
    }

    public void start() {
        if (thread != null) {
            stop();
        }
        active = true;
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        String[] options = { "STOP" };
        JOptionPane.showOptionDialog(frame, "To stop playback press STOP", "Buffered Playback, ", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        stop();
    }

    public void stop() {
        if (!active) return;
        active = false;
        if (thread.isAlive()) try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread = null;
    }

    public void run() {
        project.getAudioServer().stealAudioServer(BufferedPlayback.this, client);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        long startTick = project.getSequencer().getTickPosition();
        midiRenderer = new MyMidiRenderer(project.getMixer(), project.getSequencer(), startTick, Integer.MAX_VALUE, project.getAudioServer().getSampleRate());
        FrinikaSequencer sequencer = project.getSequencer();
        sequencer.setRealtime(false);
        sequencer.start();
        byte[] buffer = new byte[256];
        try {
            while (active) {
                int ret = midiRenderer.read(buffer);
                boolean repeat = true;
                int i = 0;
                while (true) {
                    synchronized (circularbuffer) {
                        while (i < buffer.length) {
                            if (circularbuffer_avail == pipeSize) break;
                            circularbuffer[circularbuffer_write_pos] = buffer[i];
                            circularbuffer_write_pos = (circularbuffer_write_pos + 1) % pipeSize;
                            circularbuffer_avail++;
                            i++;
                        }
                    }
                    if (i != buffer.length) try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } else break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sequencer.stop();
        sequencer.setRealtime(true);
        project.getMixer().getMainBus().setOutputProcess(project.getOutputProcess());
        project.getAudioServer().returnAudioServer(BufferedPlayback.this);
    }

    AudioClient client = new AudioClient() {

        boolean startProcessing = false;

        public void read(byte[] byteBuffer) {
            synchronized (circularbuffer) {
                if (!startProcessing) {
                    if (circularbuffer_avail < pipeWaitSize) {
                        Arrays.fill(byteBuffer, (byte) 0);
                        return;
                    } else startProcessing = true;
                }
                if (circularbuffer_avail < byteBuffer.length) {
                    System.out.println("Buffer underrun : " + circularbuffer_avail + " < " + byteBuffer.length);
                    startProcessing = false;
                    Arrays.fill(byteBuffer, (byte) 0);
                    return;
                }
                for (int i = 0; i < byteBuffer.length; i++) {
                    byteBuffer[i] = circularbuffer[circularbuffer_read_pos];
                    circularbuffer_read_pos = (circularbuffer_read_pos + 1) % pipeSize;
                    circularbuffer_avail--;
                }
            }
            return;
        }

        public void work(int bufSize) {
            read(bytebuffer);
            float[] left = buffer.getChannel(0);
            float[] right = buffer.getChannel(1);
            for (int n = 0; n < buffer.getSampleCount() * 2; n++) {
                float sample = ((short) ((0xff & bytebuffer[(n * 2) + 1]) + ((0xff & bytebuffer[(n * 2) + 0]) * 256)) / 32768f);
                if (n % 2 == 0) left[n / 2] = sample; else right[n / 2] = sample;
            }
            outputprocess.processAudio(buffer);
        }

        public void setEnabled(boolean b) {
        }
    };
}
