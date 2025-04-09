package com.migniot.streamy.encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1Tag;
import org.blinkenlights.jid3.v2.ID3V2Tag;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import com.migniot.streamy.core.Category;
import com.migniot.streamy.core.CheckListener;
import com.migniot.streamy.core.Metadata;

/**
 * The activator class controls the plug-in life cycle
 */
public class EncoderPlugin extends AbstractUIPlugin {

    /**
	 * The logger.
	 */
    private static Logger LOGGER = Logger.getLogger(EncoderPlugin.class);

    public static final String PLUGIN_ID = "com.migniot.streamy.Encoder";

    private static EncoderPlugin plugin;

    /**
	 * The guessed {@link FFMpegConfiguration}.
	 */
    private FFMpegConfiguration configuration;

    /**
	 * The constructor
	 */
    public EncoderPlugin() {
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        this.configuration = null;
    }

    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
    public static EncoderPlugin getDefault() {
        return plugin;
    }

    /**
	 * Perform encoding checks.
	 * 
	 * @param listener
	 *            The listener
	 */
    public void check(CheckListener listener) {
        ProcessBuilder builder = new ProcessBuilder(getFFMpeg(), "-version");
        Process process = null;
        try {
            process = builder.start();
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
        } catch (IOException ioe) {
            listener.failed(Category.ENCODER, "Error running ffmpeg", ioe);
            return;
        }
        try {
            process.waitFor();
        } catch (InterruptedException ie) {
            listener.failed(Category.ENCODER, "Error waiting for ffmpeg termination", ie);
            return;
        }
        listener.checked(Category.ENCODER, "Success running ffmpeg");
        builder = new ProcessBuilder(getFFMpeg(), "-formats");
        try {
            process = builder.start();
            Thread.sleep(100);
            process.getOutputStream().close();
            process.getErrorStream().close();
        } catch (IOException ioe) {
            listener.failed(Category.ENCODER, "Error running ffmpeg", ioe);
            return;
        } catch (InterruptedException e) {
            listener.failed(Category.ENCODER, "Error running ffmpeg", e);
            return;
        }
        InputStream standard = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(standard));
        StringBuilder ffmpegFormats = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                ffmpegFormats.append(line).append("\n");
            }
        } catch (IOException ioe) {
            listener.failed(Category.ENCODER, "Error reading ffmpeg formats list", ioe);
            return;
        }
        try {
            process.waitFor();
        } catch (InterruptedException ie) {
            listener.failed(Category.ENCODER, "Error waiting for ffmpeg termination", ie);
            return;
        }
        configuration = new FFMpegConfiguration();
        configuration.guessConfiguration(ffmpegFormats.toString());
        if (configuration.getFlvcodec() == null) {
            listener.failed(Category.ENCODER, "FFMpeg formats list misses FLV", null);
            return;
        }
        if (configuration.getMp3codec() == null) {
            listener.failed(Category.ENCODER, "FFMpeg formats list misses MP3", null);
            return;
        }
        listener.checked(Category.ENCODER, "Success hinting ffmpeg audio formats");
        if (configuration.getMpegcodec() == null) {
            listener.failed(Category.ENCODER, "FFMpeg formats list misses MPEG", null);
            return;
        }
        listener.checked(Category.ENCODER, "Success hinting ffmpeg video formats");
    }

    /**
	 * Try to find or download ffmpeg and return the executable path.
	 * 
	 * @return The ffmpeg executable path or the constant "ffmpeg"
	 *         {@link String}
	 */
    public String getFFMpeg() {
        if (SystemUtils.IS_OS_WINDOWS) {
            File ffmpeg = new File(getStateLocation().toFile(), "ffmpeg.exe");
            if (ffmpeg.exists()) {
                return ffmpeg.getAbsolutePath();
            }
        }
        return "ffmpeg";
    }

    /**
	 * Return true if FFMpeg download is needed
	 * 
	 * @return True if FFMpeg download is needed
	 */
    public boolean needFFMpegDownload() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return !new File(getStateLocation().toFile(), "ffmpeg.exe").exists();
        }
        return false;
    }

    /**
	 * Try to download ffmpeg-0.5.zip, windows only.
	 * 
	 * @param monitor
	 *            The monitor
	 * @throws InvocationTargetException
	 *             When download fails
	 * @throws InterruptedException
	 *             When download is interrupted
	 */
    public void downloadFFMpeg(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            new FFMpegDownloader(new File(getStateLocation().toFile(), "ffmpeg.exe")).run(monitor);
        } catch (InvocationTargetException exception) {
            LOGGER.error("Can't download ffmpeg.zip from zourceforge", exception);
            throw exception;
        } catch (InterruptedException exception) {
            LOGGER.error("FFMpeg download from zourceforge interrupted", exception);
            throw exception;
        }
    }

    /**
	 * Convert an audio file.
	 * 
	 * @param source
	 *            The source
	 * @param destination
	 *            The destination
	 * @throws FFMpegException
	 *             Upon failure
	 */
    public void convertAudio(File source, File destination) throws FFMpegException {
        convertMedia(EncoderPreferenceInitializer.AUDIO_OPTIONS, source, destination);
    }

    /**
	 * Convert an audio file.
	 * 
	 * @param prefix
	 *            The options prefix, audio or video
	 * @param source
	 *            The source
	 * @param destination
	 *            The destination
	 * @throws FFMpegException
	 *             Upon failure
	 * @see EncoderPreferenceInitializer#AUDIO_OPTIONS
	 *      EncoderPreferenceInitializer#VIDEO_OPTIONS
	 */
    public void convertMedia(String prefix, File source, File destination) throws FFMpegException {
        IPreferenceStore store = EncoderPlugin.getDefault().getPreferenceStore();
        int n = store.getInt(prefix + ".count");
        String[] command = new String[n];
        for (int i = 0; i < n; i++) {
            String part = store.getString(prefix + "." + i);
            if ("<ffmpeg>".equals(part)) {
                part = EncoderPlugin.getDefault().getFFMpeg();
            } else if ("<source>".equals(part)) {
                part = source.getAbsolutePath();
            } else if ("<destination>".equals(part)) {
                part = destination.getAbsolutePath();
            }
            command[i] = part;
        }
        convertMedia(command);
    }

    /**
	 * Convert a video file.
	 * 
	 * @param source
	 *            The source
	 * @param destination
	 *            The destination
	 * @throws FFMpegException
	 *             Upon failure
	 */
    public void convertVideo(File source, File destination) throws FFMpegException {
        convertMedia(EncoderPreferenceInitializer.VIDEO_OPTIONS, source, destination);
    }

    /**
	 * Perform a media conversion.
	 * 
	 * @param command
	 *            The conversion command
	 * @throws FFMpegException
	 *             Upon failure
	 */
    private void convertMedia(String[] command) throws FFMpegException {
        LOGGER.info("Conversion command = " + Arrays.asList(command));
        ProcessBuilder builder = new ProcessBuilder(command);
        Process ffmpeg = null;
        try {
            ffmpeg = builder.start();
            Thread.sleep(100);
        } catch (IOException e) {
            throw new FFMpegException("Can't start FFMpeg", e);
        } catch (InterruptedException e) {
            throw new FFMpegException("Can't start FFMpeg", e);
        }
        try {
            ffmpeg.getOutputStream().close();
            ffmpeg.getInputStream().close();
            ffmpeg.getErrorStream().close();
        } catch (IOException e) {
            throw new FFMpegException("Can't start FFMpeg without output or error stream", e);
        }
        int exitStatus = -1;
        try {
            exitStatus = ffmpeg.waitFor();
        } catch (InterruptedException e) {
            throw new FFMpegException("FFMpeg conversion interrupted", e);
        }
        if (exitStatus != 0) {
            throw new FFMpegException(new StringBuilder("FFMpeg conversion failed with status = [").append(exitStatus).append("] using command = ").append(Arrays.asList(command)).toString());
        }
    }

    /**
	 * Add or complete ID3 tags.
	 * 
	 * @param file
	 *            The mp3 file
	 * @param metadata
	 *            The metadata
	 */
    public void tagMp3(File file, Metadata metadata) {
        MediaFile mediaFile = new MP3File(file);
        try {
            for (ID3Tag tag : mediaFile.getTags()) {
                if (tag instanceof ID3V1Tag) {
                    ID3V1Tag v1Tag = (ID3V1Tag) tag;
                    if (metadata.containsKey(Metadata.AUTHOR)) {
                        v1Tag.setArtist(metadata.get(Metadata.AUTHOR));
                    }
                    if (metadata.containsKey(Metadata.ALBUM)) {
                        v1Tag.setAlbum(metadata.get(Metadata.ALBUM));
                    }
                    if (metadata.containsKey(Metadata.TITLE)) {
                        v1Tag.setTitle(metadata.get(Metadata.TITLE));
                    }
                    mediaFile.setID3Tag(v1Tag);
                } else if (tag instanceof ID3V2Tag) {
                    ID3V2Tag v2Tag = (ID3V2Tag) tag;
                    if (metadata.containsKey(Metadata.AUTHOR)) {
                        v2Tag.setArtist(metadata.get(Metadata.AUTHOR));
                    }
                    if (metadata.containsKey(Metadata.ALBUM)) {
                        v2Tag.setAlbum(metadata.get(Metadata.ALBUM));
                    }
                    if (metadata.containsKey(Metadata.TITLE)) {
                        v2Tag.setTitle(metadata.get(Metadata.TITLE));
                    }
                    mediaFile.setID3Tag(v2Tag);
                }
            }
        } catch (ID3Exception e) {
            LOGGER.error(MessageFormat.format("Error reading ID3 tags from file = [{0}]", file.getAbsolutePath()), e);
        }
    }

    /**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
