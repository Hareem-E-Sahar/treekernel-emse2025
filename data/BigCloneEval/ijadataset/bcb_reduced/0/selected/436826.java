package xtrememp.player.audio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.spi.PropertiesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import xtrememp.player.dsp.DspAudioDataDispatcher;

/**
 *
 * @author Besmir Beqiri
 */
public class AudioPlayer implements Callable<Void>, LineListener {

    private final Logger logger = LoggerFactory.getLogger(AudioPlayer.class);

    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    protected final Lock lock = new ReentrantLock();

    protected final Condition pauseCondition = lock.newCondition();

    protected Object audioSource = null;

    protected DspAudioDataDispatcher dspAudioDataConsumer;

    protected AudioFileFormat audioFileFormat;

    protected AudioInputStream audioInputStream;

    protected SourceDataLine sourceDataLine;

    protected String mixerName;

    protected List<PlaybackListener> listeners;

    protected ExecutorService execService = Executors.newFixedThreadPool(2);

    protected Future<Void> future;

    protected long waitTimeForTermination = 2;

    protected Map properties;

    protected FloatControl gainControl;

    protected FloatControl panControl;

    protected BooleanControl muteControl;

    protected int bufferSize = DEFAULT_BUFFER_SIZE;

    public static final int INIT = 0;

    public static final int PLAY = 1;

    public static final int PAUSE = 2;

    public static final int SEEK = 3;

    public static final int STOP = 4;

    public static int state = AudioSystem.NOT_SPECIFIED;

    private Map emptyMap = new HashMap();

    public AudioPlayer() {
        dspAudioDataConsumer = new DspAudioDataDispatcher();
        execService.execute(dspAudioDataConsumer);
        listeners = new ArrayList<PlaybackListener>();
        reset();
    }

    public AudioPlayer(PlaybackListener listener) {
        this();
        addPlaybackListener(listener);
    }

    public void addPlaybackListener(PlaybackListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePlaybackListener(PlaybackListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public List<PlaybackListener> getPlaybackListeners() {
        return listeners;
    }

    protected void notifyEvent(Playback state) {
        notifyEvent(state, emptyMap);
    }

    protected void notifyEvent(Playback state, Map properties) {
        for (PlaybackListener listener : listeners) {
            PlaybackEventLauncher launcher = new PlaybackEventLauncher(this, state, getPosition(), properties, listener);
            launcher.start();
        }
        logger.info("{}", state);
    }

    protected void reset() {
        closeStream();
        if (sourceDataLine != null) {
            sourceDataLine.flush();
            sourceDataLine.close();
            sourceDataLine = null;
        }
        audioFileFormat = null;
        gainControl = null;
        panControl = null;
        muteControl = null;
        future = null;
        emptyMap.clear();
    }

    /**
     * Open file to play.
     * @param file
     * @throws PlayerException
     */
    public void open(File file) throws PlayerException {
        if (file != null) {
            audioSource = file;
            init();
        }
    }

    /**
     * Open URL to play.
     * @param url
     * @throws PlayerException 
     */
    public void open(URL url) throws PlayerException {
        if (url != null) {
            audioSource = url;
            init();
        }
    }

    protected void init() throws PlayerException {
        int oldState = state;
        state = AudioSystem.NOT_SPECIFIED;
        if (oldState == PAUSE) {
            lock.lock();
            try {
                pauseCondition.signal();
            } finally {
                lock.unlock();
            }
        }
        if (future != null && !future.isDone()) {
            awaitTermination();
        }
        lock.lock();
        try {
            reset();
            notifyEvent(Playback.BUFFERING);
            initAudioInputStream();
            initSourceDataLine();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inits AudioInputStream and AudioFileFormat from the data source.
     * @throws PlayerException
     */
    protected void initAudioInputStream() throws PlayerException {
        try {
            if (audioSource instanceof File) {
                initAudioInputStream((File) audioSource);
            } else if (audioSource instanceof URL) {
                initAudioInputStream((URL) audioSource);
            }
            logger.info("Data source: {}", audioSource);
            if (audioFileFormat instanceof TAudioFileFormat) {
                properties = ((TAudioFileFormat) audioFileFormat).properties();
                properties = deepCopy(properties);
            } else {
                properties = new HashMap();
            }
            if (audioFileFormat.getByteLength() > 0) {
                properties.put("audio.length.bytes", new Integer(audioFileFormat.getByteLength()));
            }
            if (audioFileFormat.getFrameLength() > 0) {
                properties.put("audio.length.frames", new Integer(audioFileFormat.getFrameLength()));
            }
            if (audioFileFormat.getType() != null) {
                properties.put("audio.type", audioFileFormat.getType().toString());
            }
            AudioFormat audioFormat = audioFileFormat.getFormat();
            if (audioFormat.getFrameRate() > 0) {
                properties.put("audio.framerate.fps", new Float(audioFormat.getFrameRate()));
            }
            if (audioFormat.getFrameSize() > 0) {
                properties.put("audio.framesize.bytes", new Integer(audioFormat.getFrameSize()));
            }
            if (audioFormat.getSampleRate() > 0) {
                properties.put("audio.samplerate.hz", new Float(audioFormat.getSampleRate()));
            }
            if (audioFormat.getSampleSizeInBits() > 0) {
                properties.put("audio.samplesize.bits", new Integer(audioFormat.getSampleSizeInBits()));
            }
            if (audioFormat.getChannels() > 0) {
                properties.put("audio.channels", new Integer(audioFormat.getChannels()));
            }
            if (audioFormat instanceof TAudioFormat) {
                Map addproperties = ((TAudioFormat) audioFormat).properties();
                properties.putAll(addproperties);
            }
        } catch (UnsupportedAudioFileException ex) {
            throw new PlayerException(ex);
        } catch (IOException ex) {
            throw new PlayerException(ex);
        }
    }

    /**
     * Inits Audio ressources from file.
     * @param file
     * @throws javax.sound.sampled.UnsupportedAudioFileException
     * @throws java.io.IOException
     */
    protected void initAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        audioInputStream = AudioSystem.getAudioInputStream(file);
        audioFileFormat = AudioSystem.getAudioFileFormat(file);
    }

    /**
     * Inits Audio ressources from URL.
     * @param url
     * @throws javax.sound.sampled.UnsupportedAudioFileException
     * @throws java.io.IOException
     */
    protected void initAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        audioInputStream = AudioSystem.getAudioInputStream(url);
        audioFileFormat = AudioSystem.getAudioFileFormat(url);
    }

    /**
     * Inits Audio ressources from AudioSystem.
     */
    protected void initSourceDataLine() throws PlayerException {
        if (sourceDataLine == null) {
            try {
                logger.info("Create Source Data Line");
                AudioFormat sourceAudioFormat = audioInputStream.getFormat();
                logger.info("Source format: {}", sourceAudioFormat);
                int nSampleSizeInBits = sourceAudioFormat.getSampleSizeInBits();
                if (nSampleSizeInBits <= 0) {
                    nSampleSizeInBits = 16;
                }
                if ((sourceAudioFormat.getEncoding() == AudioFormat.Encoding.ULAW) || (sourceAudioFormat.getEncoding() == AudioFormat.Encoding.ALAW)) {
                    nSampleSizeInBits = 16;
                }
                if (nSampleSizeInBits != 8) {
                    nSampleSizeInBits = 16;
                }
                AudioFormat targetAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceAudioFormat.getSampleRate(), nSampleSizeInBits, sourceAudioFormat.getChannels(), sourceAudioFormat.getChannels() * (nSampleSizeInBits / 8), sourceAudioFormat.getSampleRate(), false);
                logger.info("Target format: {}", targetAudioFormat);
                audioInputStream = AudioSystem.getAudioInputStream(targetAudioFormat, audioInputStream);
                AudioFormat audioFormat = audioInputStream.getFormat();
                DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
                if (!AudioSystem.isLineSupported(lineInfo)) {
                    throw new PlayerException(lineInfo + " is not supported");
                }
                if (mixerName == null) {
                    mixerName = getMixers().get(0);
                }
                Mixer mixer = getMixer(mixerName);
                if (mixer != null) {
                    logger.info("Mixer: " + mixer.getMixerInfo().toString());
                    sourceDataLine = (SourceDataLine) mixer.getLine(lineInfo);
                } else {
                    sourceDataLine = (SourceDataLine) AudioSystem.getLine(lineInfo);
                    mixerName = null;
                }
                sourceDataLine.addLineListener(this);
                logger.info("Line Info: " + sourceDataLine.getLineInfo().toString());
                logger.info("Line AudioFormat: " + sourceDataLine.getFormat().toString());
                if (bufferSize <= 0) {
                    bufferSize = sourceDataLine.getBufferSize();
                }
                sourceDataLine.open(audioFormat, bufferSize);
                logger.info("Line: BufferSize = {}", sourceDataLine.getBufferSize());
                for (Control c : sourceDataLine.getControls()) {
                    logger.info("Controls: {}", c);
                }
                if (sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    gainControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
                }
                if (sourceDataLine.isControlSupported(FloatControl.Type.PAN)) {
                    panControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.PAN);
                }
                if (sourceDataLine.isControlSupported(BooleanControl.Type.MUTE)) {
                    muteControl = (BooleanControl) sourceDataLine.getControl(BooleanControl.Type.MUTE);
                }
                state = INIT;
                notifyEvent(Playback.OPENED);
            } catch (LineUnavailableException ex) {
                throw new PlayerException(ex);
            }
        }
    }

    /**
     * Set SourceDataLine buffer size. It affects audio latency
     * (the delay between SourceDataLine.write(data) and real sound).
     * @param size if equal to -1 (AudioSystem.NOT_SPECIFIED)
     * means maximum buffer size available.
     */
    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            this.bufferSize = AudioSystem.NOT_SPECIFIED;
        } else {
            this.bufferSize = bufferSize;
        }
    }

    /**
     * Return SourceDataLine buffer size.
     * @return -1 (AudioSystem.NOT_SPECIFIED) for maximum buffer size.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Deep copy of a Map.
     * @param src
     * @return map
     */
    protected Map deepCopy(Map src) {
        HashMap map = new HashMap();
        if (src != null) {
            Iterator it = src.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object value = src.get(key);
                map.put(key, value);
            }
        }
        return map;
    }

    public List<String> getMixers() {
        List<String> mixers = new ArrayList<String>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        if (mixerInfos != null) {
            for (int i = 0; i < mixerInfos.length; i++) {
                Line.Info lineInfo = new Line.Info(SourceDataLine.class);
                Mixer _mixer = AudioSystem.getMixer(mixerInfos[i]);
                if (_mixer.isLineSupported(lineInfo)) {
                    mixers.add(mixerInfos[i].getName());
                }
            }
        }
        return mixers;
    }

    public Mixer getMixer(String name) {
        Mixer _mixer = null;
        if (name != null) {
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            if (mixerInfos != null) {
                for (int i = 0; i < mixerInfos.length; i++) {
                    if (mixerInfos[i].getName().equals(name)) {
                        _mixer = AudioSystem.getMixer(mixerInfos[i]);
                        break;
                    }
                }
            }
        }
        return _mixer;
    }

    public String getMixerName() {
        return mixerName;
    }

    public void setMixerName(String name) {
        mixerName = name;
    }

    public long getDuration() {
        long duration = AudioSystem.NOT_SPECIFIED;
        if (properties.containsKey("duration")) {
            duration = ((Long) properties.get("duration")).longValue();
        } else {
            duration = getTimeLengthEstimation(properties);
        }
        return duration;
    }

    public int getByteLength() {
        int bytesLength = AudioSystem.NOT_SPECIFIED;
        if (properties != null) {
            if (properties.containsKey("audio.length.bytes")) {
                bytesLength = ((Integer) properties.get("audio.length.bytes")).intValue();
            }
        }
        return bytesLength;
    }

    public int getPositionByte() {
        int positionByte = AudioSystem.NOT_SPECIFIED;
        if (properties != null) {
            if (properties.containsKey("mp3.position.byte")) {
                positionByte = ((Integer) properties.get("mp3.position.byte")).intValue();
                return positionByte;
            }
            if (properties.containsKey("ogg.position.byte")) {
                positionByte = ((Integer) properties.get("ogg.position.byte")).intValue();
                return positionByte;
            }
        }
        return positionByte;
    }

    protected long getTimeLengthEstimation(Map properties) {
        long milliseconds = AudioSystem.NOT_SPECIFIED;
        int byteslength = AudioSystem.NOT_SPECIFIED;
        if (properties != null) {
            if (properties.containsKey("audio.length.bytes")) {
                byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
            }
            if (properties.containsKey("duration")) {
                milliseconds = (int) (((Long) properties.get("duration")).longValue()) / 1000;
            } else {
                int bitspersample = AudioSystem.NOT_SPECIFIED;
                int channels = AudioSystem.NOT_SPECIFIED;
                float samplerate = AudioSystem.NOT_SPECIFIED;
                int framesize = AudioSystem.NOT_SPECIFIED;
                if (properties.containsKey("audio.samplesize.bits")) {
                    bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue();
                }
                if (properties.containsKey("audio.channels")) {
                    channels = ((Integer) properties.get("audio.channels")).intValue();
                }
                if (properties.containsKey("audio.samplerate.hz")) {
                    samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue();
                }
                if (properties.containsKey("audio.framesize.bytes")) {
                    framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue();
                }
                if (bitspersample > 0) {
                    milliseconds = (long) (1000.0f * byteslength / (samplerate * channels * (bitspersample / 8)));
                } else {
                    milliseconds = (long) (1000.0f * byteslength / (samplerate * framesize));
                }
            }
        }
        return milliseconds * 1000;
    }

    /**
     * Sets Gain value.
     * @param gain a value bitween -1.0 and +1.0
     * @throws PlayerException
     */
    public void setGain(float gain) throws PlayerException {
        if (gainControl != null) {
            double minGain = gainControl.getMinimum();
            double maxGain = gainControl.getMaximum();
            double ampGain = ((10.0f / 20.0f) * maxGain) - minGain;
            double cste = Math.log(10.0) / 20;
            double value = minGain + (1 / cste) * Math.log(1 + (Math.exp(cste * ampGain) - 1) * gain);
            gainControl.setValue((float) value);
            logger.info("{}", gainControl.toString());
        } else {
            throw new PlayerException("Gain control not supported");
        }
    }

    public float getGain() {
        float gain = 0.0f;
        if (gainControl != null) {
            gain = gainControl.getValue();
        }
        return gain;
    }

    /**
     * Sets Pan value.
     * @param pan a value bitween -1.0 and +1.0
     * @throws PlayerException
     */
    public void setPan(float pan) throws PlayerException {
        if (panControl != null) {
            panControl.setValue(pan);
            logger.info("{}", panControl.toString());
        } else {
            throw new PlayerException("Pan control not supported");
        }
    }

    public float getPan() {
        float pan = 0.0f;
        if (panControl != null) {
            pan = panControl.getValue();
        }
        return pan;
    }

    /**
     * Sets Mute value.
     * @param mute a boolean value
     * @throws PlayerException
     */
    public void setMuted(boolean mute) throws PlayerException {
        if (muteControl != null) {
            muteControl.setValue(mute);
            logger.info("{}", muteControl.toString());
        } else {
            throw new PlayerException("Mute control not supported");
        }
    }

    public boolean isMuted() {
        boolean muted = false;
        if (muteControl != null) {
            muted = muteControl.getValue();
        }
        return muted;
    }

    public long getPosition() {
        long pos = 0;
        if (sourceDataLine != null) {
            pos = sourceDataLine.getMicrosecondPosition();
        }
        return pos;
    }

    public int getState() {
        return state;
    }

    public DspAudioDataDispatcher getDspAudioDataConsumer() {
        return dspAudioDataConsumer;
    }

    @Override
    public Void call() throws PlayerException {
        logger.info("Decoding thread started");
        int nBytesRead = 0;
        int audioDataLength = dspAudioDataConsumer.getSampleSize() * 4;
        byte[] audioBytes = new byte[audioDataLength];
        lock.lock();
        try {
            while ((nBytesRead != -1) && (state != STOP) && (state != SEEK) && (state != AudioSystem.NOT_SPECIFIED)) {
                try {
                    if (state == PLAY) {
                        int toRead = audioDataLength;
                        int totalRead = 0;
                        while (toRead > 0 && (nBytesRead = audioInputStream.read(audioBytes, totalRead, toRead)) != -1) {
                            totalRead += nBytesRead;
                            toRead -= nBytesRead;
                        }
                        if (totalRead > 0) {
                            byte[] audioData = Arrays.copyOf(audioBytes, audioDataLength);
                            sourceDataLine.write(audioData, 0, totalRead);
                            dspAudioDataConsumer.storeAudioData(audioData, totalRead);
                            for (PlaybackListener pl : listeners) {
                                PlaybackEvent pe = new PlaybackEvent(this, Playback.PLAYING, sourceDataLine.getMicrosecondPosition(), emptyMap);
                                if (audioInputStream instanceof PropertiesContainer) {
                                    pe.setProperties(((PropertiesContainer) audioInputStream).properties());
                                }
                                pl.playbackProgress(pe);
                            }
                        }
                    } else {
                        pauseCondition.await();
                    }
                } catch (Exception ex) {
                    logger.error("Decoder Exception: ", ex);
                    state = STOP;
                    notifyEvent(Playback.STOPPED);
                    throw new PlayerException(ex);
                }
            }
            if (sourceDataLine != null) {
                sourceDataLine.drain();
                sourceDataLine.stop();
                sourceDataLine.close();
                sourceDataLine = null;
            }
            closeStream();
            if (nBytesRead == -1) {
                notifyEvent(Playback.EOM);
            }
        } finally {
            lock.unlock();
        }
        notifyEvent(Playback.STOPPED);
        logger.info("Decoding thread completed");
        return null;
    }

    private void awaitTermination() {
        try {
            future.get();
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (ExecutionException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void update(LineEvent event) {
        logger.info("SourceDataLine: {}", event.getType());
    }

    public void play() throws PlayerException {
        lock.lock();
        try {
            switch(state) {
                case STOP:
                    initAudioInputStream();
                    initSourceDataLine();
                case INIT:
                    if (sourceDataLine != null) {
                        sourceDataLine.start();
                        state = PLAY;
                        future = execService.submit(this);
                        notifyEvent(Playback.PLAYING);
                    }
                    break;
                case PAUSE:
                    if (sourceDataLine != null) {
                        sourceDataLine.start();
                        state = PLAY;
                        pauseCondition.signal();
                        notifyEvent(Playback.PLAYING);
                    }
                    break;
            }
        } finally {
            lock.unlock();
        }
    }

    public void pause() {
        if (sourceDataLine != null) {
            if (state == PLAY) {
                state = PAUSE;
                sourceDataLine.stop();
                notifyEvent(Playback.PAUSED);
            }
        }
    }

    public void stop() {
        if (state != STOP) {
            int oldState = state;
            state = STOP;
            lock.lock();
            try {
                if (oldState == INIT) {
                    if (sourceDataLine != null) {
                        sourceDataLine.flush();
                        sourceDataLine.close();
                        sourceDataLine = null;
                    }
                    closeStream();
                    notifyEvent(Playback.STOPPED);
                } else if (oldState == PAUSE) {
                    pauseCondition.signal();
                }
            } finally {
                lock.unlock();
            }
            if (future != null && !future.isDone()) {
                awaitTermination();
            }
        }
    }

    public long seek(long bytes) throws PlayerException {
        long totalSkipped = 0;
        if (audioSource instanceof File) {
            int bytesLength = getByteLength();
            if ((bytesLength <= 0) || (bytes >= bytesLength)) {
                notifyEvent(Playback.EOM);
                return totalSkipped;
            }
            logger.info("Bytes to skip: {}", bytes);
            int oldState = state;
            state = SEEK;
            if (oldState == PAUSE) {
                lock.lock();
                try {
                    pauseCondition.signal();
                } finally {
                    lock.unlock();
                }
            }
            if (future != null && !future.isDone()) {
                awaitTermination();
            }
            long skipped = 0;
            lock.lock();
            try {
                reset();
                notifyEvent(Playback.SEEKING);
                initAudioInputStream();
                if (audioInputStream != null) {
                    String type = (String) properties.get("audio.type");
                    if (type.equalsIgnoreCase("ogg")) {
                        initSourceDataLine();
                        totalSkipped = audioInputStream.skip(bytes);
                        if (totalSkipped == -1) {
                            logger.info("Skipped: {}/{}", totalSkipped, bytes);
                            if (totalSkipped == -1) {
                                throw new PlayerException("Seek not supported");
                            }
                        }
                        logger.info("Total skipped bytes: {}", totalSkipped);
                    } else {
                        while (totalSkipped < bytes) {
                            skipped = audioInputStream.skip(bytes - totalSkipped);
                            if (skipped == 0) {
                                break;
                            }
                            totalSkipped = totalSkipped + skipped;
                            logger.info("Skipped: {}/{}", totalSkipped, bytes);
                            if (totalSkipped == -1) {
                                throw new PlayerException("Seek not supported");
                            }
                        }
                        logger.info("Total skipped bytes: {}", totalSkipped);
                        initSourceDataLine();
                    }
                }
            } catch (IOException ex) {
                throw new PlayerException(ex);
            } finally {
                lock.unlock();
            }
            if (oldState == PLAY) {
                play();
            }
        }
        return totalSkipped;
    }

    protected void closeStream() {
        if (audioInputStream != null) {
            try {
                audioInputStream.close();
                audioInputStream = null;
                logger.info("Stream closed");
            } catch (IOException ex) {
                logger.error("Cannot close stream", ex);
            }
        }
    }
}
