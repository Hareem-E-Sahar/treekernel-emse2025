package uk.co.caprica.vlcj.radio.service.icecast;

import org.simpleframework.xml.Element;
import uk.co.caprica.vlcj.radio.model.DirectoryEntry;

/**
 * Implementation of a directory entry.
 */
public class IcecastDirectoryEntry implements DirectoryEntry {

    /**
   * Name of the station.
   */
    @Element(name = "server_name")
    private String name;

    /**
   * Streaming URL for the audio.
   */
    @Element(name = "listen_url")
    private String url;

    /**
   * The (media) type of the server.
   */
    @Element(name = "server_type")
    private String type;

    /**
   * Bit-rate of the stream.
   */
    @Element(name = "bitrate")
    private String bitRate;

    /**
   * Number of audio channels in the stream.
   */
    @Element
    private int channels;

    /**
   * Sample rate of the stream.
   */
    @Element(name = "samplerate")
    private int sampleRate;

    /**
   * Genre.
   */
    @Element
    private String genre;

    /**
   * Currently playing item.
   */
    @Element(required = false, name = "current_song")
    private String nowPlaying;

    @Override
    public String getDirectory() {
        return "Icecast";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getBitRate() {
        return bitRate;
    }

    @Override
    public int getChannels() {
        return channels;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public String getGenre() {
        return genre;
    }

    @Override
    public String getNowPlaying() {
        return nowPlaying;
    }

    @Override
    public int compareTo(DirectoryEntry o) {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(300);
        sb.append(getClass().getSimpleName()).append('[');
        sb.append("name=").append(name).append(',');
        sb.append("url=").append(url).append(',');
        sb.append("type=").append(type).append(',');
        sb.append("bitRate=").append(bitRate).append(',');
        sb.append("channels=").append(channels).append(',');
        sb.append("sampleRate=").append(sampleRate).append(',');
        sb.append("genre=").append(genre).append(',');
        sb.append("nowPlaying=").append(nowPlaying).append(']');
        return sb.toString();
    }
}
