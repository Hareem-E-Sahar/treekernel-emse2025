package xtrememp.tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.flac.FlacInfoReader;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.tag.flac.FlacTag;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.metadata.StreamInfo;
import xtrememp.util.Utilities;

/**
 * This class gives information (audio format and comments) about Flac input or URL.
 * 
 * @author Besmir Beqiri
 */
public class FlacInfo implements TagInfo {

    protected String type = null;

    protected int channels = 0;

    protected int bitspersample = 0;

    protected int samplerate = 0;

    protected int bitrate = 0;

    protected long size = -1;

    protected int duration = -1;

    protected String location = null;

    protected String track = null;

    protected String year = null;

    protected String genre = null;

    protected String title = null;

    protected String artist = null;

    protected String album = null;

    protected String comment = null;

    protected StreamInfo info = null;

    /**
     * Load and parse Flac info from File.
     *
     * @param input
     * @throws IOException
     */
    @Override
    public void load(File input) throws IOException, UnsupportedAudioFileException {
        AudioFileFormat aff = AudioSystem.getAudioFileFormat(input);
        type = aff.getType().toString();
        if (!type.equalsIgnoreCase("flac")) {
            throw new UnsupportedAudioFileException("Not Flac audio format");
        }
        size = input.length();
        location = input.getPath();
        FileInputStream is = new FileInputStream(input);
        FLACDecoder decoder = new FLACDecoder(is);
        decoder.readMetadata();
        info = decoder.getStreamInfo();
        FlacTag flacTag = null;
        GenericAudioHeader gah = null;
        try {
            AudioFile flacFile = AudioFileIO.read(input);
            flacTag = (FlacTag) flacFile.getTag();
            FlacInfoReader fir = new FlacInfoReader();
            gah = fir.read(new RandomAccessFile(input, "r"));
            if (gah != null) {
                type = gah.getEncodingType();
                channels = gah.getChannelNumber();
                samplerate = gah.getSampleRateAsNumber();
                bitrate = (int) gah.getBitRateAsNumber();
                duration = gah.getTrackLength();
            }
            if (flacTag != null) {
                title = flacTag.getFirstTitle();
                artist = flacTag.getFirstArtist();
                album = flacTag.getFirstAlbum();
                year = flacTag.getFirstYear();
                genre = flacTag.getFirstGenre();
                track = flacTag.getFirstTrack();
                comment = flacTag.getFirstComment();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Load and parse Flac info from an URL.
     *
     * @param input
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    @Override
    public void load(URL input) throws IOException, UnsupportedAudioFileException {
        location = input.toString();
        FLACDecoder decoder = new FLACDecoder(input.openStream());
        decoder.readMetadata();
        info = decoder.getStreamInfo();
        AudioFileFormat aff = AudioSystem.getAudioFileFormat(input);
        loadInfo(aff);
    }

    /**
     * Load and parse Flac info from an input stream.
     *
     * @param input
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    @Override
    public void load(InputStream input) throws IOException, UnsupportedAudioFileException {
        FLACDecoder decoder = new FLACDecoder(input);
        decoder.readMetadata();
        info = decoder.getStreamInfo();
        AudioFileFormat aff = AudioSystem.getAudioFileFormat(input);
        loadInfo(aff);
    }

    /**
     * Load Flac info from an AudioFileFormat.
     *
     * @param aff the audio file format
     * @throws javax.sound.sampled.UnsupportedAudioFileException
     */
    protected void loadInfo(AudioFileFormat aff) throws UnsupportedAudioFileException {
        type = aff.getType().toString();
        if (!type.equalsIgnoreCase("flac")) {
            throw new UnsupportedAudioFileException("Not Flac audio format");
        }
        channels = info.getChannels();
        samplerate = info.getSampleRate();
        bitspersample = info.getBitsPerSample();
        duration = Math.round(info.getTotalSamples() / info.getSampleRate());
    }

    public long getSize() {
        return size;
    }

    public String getLocation() {
        return location;
    }

    public int getBitsPerSample() {
        return bitspersample;
    }

    @Override
    public String getCodecDetails() {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><b>Encoding Type: </b>");
        sb.append(getEncodingType().toUpperCase());
        sb.append("<br><b>Sampling rate: </b>");
        sb.append(getSampleRate() + " Hz");
        sb.append("<br><b>Bitrate: </b>");
        sb.append(getBitRate() + " Kbps");
        sb.append("<br><b>Channels: </b>");
        sb.append(getChannels());
        if (size != -1) {
            sb.append("<br><b>Size: </b>");
            sb.append(Utilities.byteCountToDisplaySize(size));
        }
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public int getChannels() {
        return channels;
    }

    @Override
    public int getSampleRate() {
        return samplerate;
    }

    @Override
    public int getBitRate() {
        return bitrate;
    }

    @Override
    public int getTrackLength() {
        return duration;
    }

    @Override
    public String getTitle() {
        return (title == null) ? null : title.trim();
    }

    @Override
    public String getArtist() {
        return (artist == null) ? null : artist.trim();
    }

    @Override
    public String getAlbum() {
        return (album == null) ? null : album.trim();
    }

    @Override
    public String getTrack() {
        return (track == null) ? null : track.trim();
    }

    @Override
    public String getGenre() {
        return (genre == null) ? null : genre.trim();
    }

    @Override
    public String getComment() {
        return (comment == null) ? null : comment.trim();
    }

    @Override
    public String getYear() {
        return (year == null) ? null : year.trim();
    }

    @Override
    public String getEncodingType() {
        return (type == null) ? null : type.trim();
    }
}
