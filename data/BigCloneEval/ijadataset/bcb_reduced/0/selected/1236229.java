package net.sourceforge.texture.boundary;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import net.sourceforge.texture.audio.AudioAnalyzerResults;
import net.sourceforge.texture.common.ConfigManager;
import net.sourceforge.texture.threads.SamplesMultiplexer;
import net.sourceforge.texture.threads.StreamsProcessor;
import utils.array.ByteArrayUtils;
import utils.stream.CustomPipedInputStream;
import com.imagero.uio.RandomAccessInput;
import com.imagero.uio.bio.BufferedRandomAccessIO;

public class WaveformCanvasesContainer extends JPanel implements ComponentListener {

    private static final long serialVersionUID = -3743633912704297905L;

    private final WaveformGraphsContainer waveformGraphsContainer;

    private WaveformCanvas[] waveformCanvases;

    private SamplesMultiplexer samplesMultiplexer;

    private RandomAccessInput randomAccessAudioStream;

    private PipedOutputStream[] waveformCanvasesSamplesSources;

    private CustomPipedInputStream[] waveformCanvasesSamplesSinks;

    private long randomAccessAudioStreamLength;

    private int optimalBufferSize;

    private int numWaveformCanvases;

    private int sampleSizeInByte;

    private int maxFramePosition;

    private int minSampleValue;

    private int maxSampleValue;

    private Method getSampleValueMethod;

    private int currentFramePosition;

    private int waveformCanvasHorizontalInsets;

    private int insetlessWaveformCanvasWidth;

    private int insetlessWaveformCanvasHeight;

    private int widthMinimumBinaryScaleFactor;

    private int widthCurrentBinaryScaleFactor;

    public WaveformCanvasesContainer(WaveformGraphsContainer waveformGraphsContainer) {
        this.waveformGraphsContainer = waveformGraphsContainer;
        this.currentFramePosition = 0;
        this.insetlessWaveformCanvasWidth = 0;
        this.setOpaque(false);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.addComponentListener(this);
    }

    public void prepare(AudioAnalyzerResults audioAnalyzerResults) throws IOException {
        this.randomAccessAudioStream = audioAnalyzerResults.getRandomAccessStream(this);
        this.randomAccessAudioStreamLength = randomAccessAudioStreamLength;
        this.optimalBufferSize = optimalBufferSize;
        this.numWaveformCanvases = numWaveformCanvases;
        this.sampleSizeInByte = sampleSizeInByte;
        this.maxFramePosition = maxFramePosition;
        this.minSampleValue = 0;
        this.maxSampleValue = 0;
        String getSampleValueMethodName = "get";
        if (audioAnalyzerResults.getFormat().getEncoding().equals(Encoding.PCM_SIGNED)) {
            this.minSampleValue = -(int) Math.scalb(1, this.sampleSizeInByte * 8 - 1);
            this.maxSampleValue = -this.minSampleValue - 1;
            getSampleValueMethodName += "Signed";
        } else if (audioAnalyzerResults.getFormat().getEncoding().equals(Encoding.PCM_UNSIGNED)) {
            this.minSampleValue = 0;
            this.maxSampleValue = (int) Math.scalb(1, this.sampleSizeInByte * 8) - 1;
            getSampleValueMethodName += "Unsigned";
        }
        switch(this.sampleSizeInByte) {
            case 1:
                getSampleValueMethodName += "Byte";
                break;
            case 2:
                getSampleValueMethodName += "Short";
                break;
            case 3:
                getSampleValueMethodName += "24BitInt";
                break;
            case 4:
                getSampleValueMethodName += "Int";
                break;
        }
        try {
            this.getSampleValueMethod = ByteArrayUtils.class.getMethod(getSampleValueMethodName, new Class[] { byte[].class });
            this.waveformCanvases = new WaveformCanvas[this.numWaveformCanvases];
            for (int i = 0; i < this.numWaveformCanvases; i++) {
                WaveformCanvas waveformCanvas = new WaveformCanvas(i, this);
                waveformCanvas.setMinimumSize(new Dimension(ConfigManager.getWaveformGraphLevelPanelWidth(), ConfigManager.getWaveformGraphHeight()));
                waveformCanvas.setPreferredSize(waveformCanvas.getMinimumSize());
                waveformCanvas.setMaximumSize(new Dimension(Integer.MAX_VALUE, ConfigManager.getWaveformGraphHeight()));
                this.waveformCanvases[i] = waveformCanvas;
                this.add(this.waveformCanvases[i]);
                if (i < this.numWaveformCanvases - 1) {
                    this.add(Box.createVerticalStrut(ConfigManager.getWaveformGraphVerticalSpacing()));
                } else {
                    this.add(Box.createVerticalGlue());
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        this.stopRefreshingWaveformGraphs();
        this.removeAll();
    }

    public void setBinaryScaleFactor(int binaryScaleFactor) {
        this.widthCurrentBinaryScaleFactor = binaryScaleFactor;
        this.restartRefreshingWaveformGraphs();
    }

    public void refreshPosition(int position) {
        this.currentFramePosition = position;
        for (int i = 0; i < this.numWaveformCanvases; i++) {
            this.waveformCanvases[i].refreshPosition(position);
        }
    }

    private void startRefreshingWaveformGraphs() {
        this.waveformCanvasesSamplesSources = new PipedOutputStream[this.numWaveformCanvases];
        this.waveformCanvasesSamplesSinks = new CustomPipedInputStream[this.numWaveformCanvases];
        for (int i = 0; i < this.numWaveformCanvases; i++) {
            this.waveformCanvasesSamplesSources[i] = new PipedOutputStream();
            try {
                this.waveformCanvasesSamplesSinks[i] = new CustomPipedInputStream(this.optimalBufferSize / this.numWaveformCanvases);
                this.waveformCanvasesSamplesSources[i].connect(waveformCanvasesSamplesSinks[i]);
                this.waveformCanvases[i].startRefreshingWaveformGraph(this.waveformCanvasesSamplesSinks[i], (this.randomAccessAudioStreamLength / this.numWaveformCanvases), this.optimalBufferSize / this.numWaveformCanvases, this.getSampleValueMethod, this.sampleSizeInByte, this.maxFramePosition, this.minSampleValue, this.maxSampleValue, this.insetlessWaveformCanvasWidth, this.insetlessWaveformCanvasHeight, this.widthCurrentBinaryScaleFactor);
            } catch (IOException exception) {
                assert false;
            }
        }
        this.samplesMultiplexer = new SamplesMultiplexer(this.randomAccessAudioStream, this.waveformCanvasesSamplesSources, this.randomAccessAudioStreamLength, this.optimalBufferSize, this.sampleSizeInByte, this);
        this.samplesMultiplexer.start();
    }

    private void stopRefreshingWaveformGraphs() {
        if (this.samplesMultiplexer != null) {
            this.samplesMultiplexer.stopStreaming();
            this.samplesMultiplexer = null;
            for (int i = 0; i < this.numWaveformCanvases; i++) {
                this.waveformCanvases[i].stopRefreshingWaveformGraph();
            }
            this.waveformCanvasesSamplesSources = null;
            this.waveformCanvasesSamplesSinks = null;
        }
    }

    private void restartRefreshingWaveformGraphs() {
        this.stopRefreshingWaveformGraphs();
        this.startRefreshingWaveformGraphs();
    }

    private void calculateWidthMinimumBinaryScaleFactor() {
        int bestFittedWaveformCanvasWidth = this.maxFramePosition + 1;
        this.widthMinimumBinaryScaleFactor = 0;
        while (bestFittedWaveformCanvasWidth > this.insetlessWaveformCanvasWidth) {
            if (bestFittedWaveformCanvasWidth % 2 == 0) {
                bestFittedWaveformCanvasWidth /= 2;
            } else {
                bestFittedWaveformCanvasWidth = (bestFittedWaveformCanvasWidth + 1) / 2;
            }
            this.widthMinimumBinaryScaleFactor--;
        }
    }

    public void notifyIOException(IOException e) {
        this.waveformGraphsContainer.notifyIOException(e);
    }

    public void notifyThreadIOException(StreamsProcessor streamsProcessor, IOException e) {
        this.notifyIOException(e);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (this.insetlessWaveformCanvasWidth == 0) {
            Insets borderInsets = this.waveformCanvases[0].getInsets();
            this.waveformCanvasHorizontalInsets = borderInsets.left + borderInsets.right;
            this.insetlessWaveformCanvasHeight = ConfigManager.getWaveformGraphHeight() - borderInsets.top - borderInsets.bottom;
        }
        int previousInsetlessWaveformCanvasWidth = this.insetlessWaveformCanvasWidth;
        this.insetlessWaveformCanvasWidth = this.getWidth() - this.waveformCanvasHorizontalInsets;
        if (previousInsetlessWaveformCanvasWidth == 0) {
            this.calculateWidthMinimumBinaryScaleFactor();
            this.setBinaryScaleFactor(this.widthMinimumBinaryScaleFactor);
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }
}
