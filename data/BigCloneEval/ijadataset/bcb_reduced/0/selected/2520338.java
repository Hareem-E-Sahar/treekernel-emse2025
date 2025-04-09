package twjcalc.gui.tuner;

import java.util.ArrayList;
import twjcalc.model.music.ET12;

/**
 * NoteDetector Detects and identifies notes being played into the microphone. When the note
 * changes, listeners are informed.
 * <p>
 * Sample Size: for low end of low D we need to sample 100 ms to be sure of detecting the notes
 * correctly. for High D we can use 50ms.
 * <p>
 * Sample Rate: 5000 seems adequate, can we reduce it? - not for high end of high D. For low D we
 * might use 2500.
 * <p>
 * High D: 50ms 5000Hz sampling.
 * <p>
 * Low D: 100ms 2500Hz sampling.
 * <p>
 * Maybe we can define this per 'octave' Low D first octave: 100ms, 1250Hz. Low D second octave and
 * high D first octave: 50ms 2500Hz. High D second octave 25ms 5000Hz. Jan 17, 2011
 */
public class NoteDetector {

    /**
	 * First peak above this ratio is assumed to be the target frequency
	 */
    private static final double PEAK_RATIO = 0.5;

    private CaptureThread captureThread;

    private final ArrayList listeners = new ArrayList();

    private float sampleRate = 5000;

    private int sampleSize = 800;

    private double peakRatio = PEAK_RATIO;

    private final double[] fft(final byte[] in) {
        final int n = in.length;
        final double out[] = new double[n / 2];
        final double twoPiOverLength = (2 * Math.PI) / in.length;
        for (int j = 0; j < out.length; j++) {
            double firstSummation = 0;
            double secondSummation = 0;
            for (int k = 0; k < in.length; k++) {
                final double dk = in[k];
                final double twoPInjk = twoPiOverLength * j * k;
                firstSummation += dk * Math.cos(twoPInjk);
                secondSummation += dk * Math.sin(twoPInjk);
            }
            out[j] = Math.pow(firstSummation, 2) + Math.pow(secondSummation, 2);
        }
        return out;
    }

    /**
	 * @param f
	 *            dataSet
	 * @return the integer index representing the first peak in the dataSet
	 */
    private final int findPeak(final double[] f) {
        double minF = Double.MAX_VALUE;
        double maxF = Double.MIN_VALUE;
        for (int i = 0; i < f.length; i++) {
            if (f[i] > maxF) {
                maxF = f[i];
            }
            if (f[i] < minF) {
                minF = f[i];
            }
        }
        final double spread = maxF - minF;
        final double threshold = minF + (spread * PEAK_RATIO);
        int start = -1;
        for (int i = 0; (-1 == start) && (i < f.length); i++) {
            if (f[i] > threshold) {
                start = i;
            }
        }
        int end = -1;
        if (start > 0) {
            for (int i = start; (-1 == end) && (i < f.length); i++) {
                if (f[i] < threshold) {
                    end = i - 1;
                }
            }
        }
        final int peak = (start + end) / 2;
        return peak;
    }

    /**
	 * Called by the capture thread, so package-private
	 * 
	 * @param rawData
	 */
    final void process(final byte[] rawData) {
        final double[] dData = fft(rawData);
        processDFT(dData);
    }

    public final void addListener(final NoteListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public final double getPeakRatio() {
        return peakRatio;
    }

    public void processDFT(final double[] dData) {
        String newNoteName = null;
        double dFrequency = 0;
        final int peak = findPeak(dData);
        if (peak > 0) {
            dFrequency = sampleRate * peak / (2 * dData.length);
            newNoteName = ET12.noteName(dFrequency);
        } else {
        }
        for (int i = 0; i < listeners.size(); i++) {
            final NoteListener l = (NoteListener) listeners.get(i);
            l.noteChanged(newNoteName, dFrequency, peak, dData);
        }
    }

    public final void removeListener(final NoteListener listener) {
        listeners.remove(listener);
    }

    public final void setPeakRatio(final double peakRatio) {
        this.peakRatio = peakRatio;
    }

    /**
	 * @param durationMs
	 *            number of milliseconds of data to capture before checking for a note.
	 * @param sampleRate
	 *            sampling rate of the hardware
	 */
    public final void startCapture(final int durationMs, final float sampleRate) {
        if (null != captureThread) {
            captureThread.stopCapture();
            captureThread = null;
        }
        sampleSize = (int) ((sampleRate * durationMs) / 1000);
        this.sampleRate = sampleRate;
        captureThread = new CaptureThread(this, sampleSize, sampleRate);
        captureThread.start();
    }

    /**
	 * Stop the capture.
	 */
    public final void stopCapture() {
        if (null != captureThread) {
            captureThread.stopCapture();
        }
        captureThread = null;
    }
}
