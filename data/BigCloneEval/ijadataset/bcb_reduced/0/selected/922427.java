package JavaTron;

import java.util.*;

/**
 * Represents all the song information for a song from an the AudioTron.
 *
 * @author Taylor Gautier
 * @author additions: Joe Culbreth
 * @version $Revision: 1.3 $
 */
public class AudioTronSong implements Timer.Alarm, AudioTron.Playable {

    public String title = null;

    public String artist = null;

    public String album = null;

    public String genre = null;

    public String source = null;

    public String sourceLocation = null;

    public int index;

    public int total = 0;

    public int pos = 0;

    public long estimatedPos = 0;

    private long delta = 0;

    private long next = 0;

    private final String UNKNOWN = "Unknown";

    private boolean playing = false;

    public static final AudioTronSong NULL_SONG = new AudioTronSong();

    static AudioTronState at;

    /**
   * Default constructor for song -- assigns a negative index
   */
    public AudioTronSong() {
        this(-1);
    }

    /**
   * Constructor that takes an index
   *
   * @param index_ the index of the song (in the play queue)
   */
    public AudioTronSong(int index_) {
        index = index_;
        title = UNKNOWN;
    }

    public void showAll() {
        System.out.println(title);
        System.out.println(artist);
        System.out.println(album);
        System.out.println(genre);
        System.out.println(source);
        System.out.println(sourceLocation);
        System.out.println(index);
        System.out.println(total);
    }

    public String[] getSongInfo() {
        String[] info = new String[8];
        info[0] = "Title:" + title;
        info[1] = "Artist:" + artist;
        info[2] = "album:" + album;
        info[3] = "genre:" + genre;
        info[4] = "source:" + source;
        info[5] = "ID:" + sourceLocation;
        info[6] = "index:" + index;
        info[7] = "total:" + total;
        return (info);
    }

    /**
   * Copy contents of other song to this song
   */
    public void update(AudioTronSong song) {
        title = song.title;
        artist = song.artist;
        album = song.album;
        genre = song.genre;
        source = song.source;
        sourceLocation = song.sourceLocation;
        index = song.index;
        if (song.total > 0 && song.total != total) {
            total = song.total;
        }
        if (song.pos > 0 && song.pos != pos) {
            pos = song.pos;
        }
    }

    /**
   * Determine if these two songs match.  They match if:
   * 1) This song's title is UNKNOWN
   * 2) The two song titles are the same
   * 3) The two song titles are similar
   *
   * @param song the song to check match against
   *
   * @return boolean true if they match, false otherwise
   */
    public boolean checkMatch(AudioTronSong song) {
        if (title == UNKNOWN) {
            return true;
        }
        if (title.equals(song.title)) {
            return true;
        }
        int index = title.indexOf(" -");
        if (index > 0) {
            if (song.title.length() > index && title.substring(0, index).equals(song.title.substring(0, index))) {
                return true;
            }
        }
        return false;
    }

    /**
   * Determine whether this song should be updated by the song that
   * is passed in (whether the passed in song has more info)
   *
   * @param s the song to determine if it is newer than this song
   *
   * @return true if the passed song is newer, false if not
   */
    public boolean shouldUpdate(AudioTronSong s) {
        if (title == UNKNOWN && s.title != UNKNOWN) {
            return true;
        }
        if (artist == null && s.artist != null) {
            return true;
        }
        if (album == null && s.album != null) {
            return true;
        }
        if (total == 0 && s.total > 0) {
            return true;
        }
        if (index != s.index) {
            return true;
        }
        if (!title.equals(s.title)) {
            return true;
        }
        return false;
    }

    public static void appendIndex(StringBuffer str, int index) {
        if (index >= 0) {
            if (index < 10) {
                str.append(0);
            }
            str.append(index);
        }
    }

    public static String time(int seconds) {
        StringBuffer b = new StringBuffer();
        b.append(new Integer(seconds / 60).toString());
        b.append(":");
        if (seconds % 60 < 10) {
            b.append('0');
        }
        b.append(new Integer(seconds % 60).toString());
        return b.toString();
    }

    /**
   * Generates a ListBox friendly String
   */
    public String toString() {
        return getIndexedLongFormAdapter().toString();
    }

    public Object instance() {
        return this;
    }

    private ShortFormAdapter shortFormAdapter;

    private LongFormAdapter longFormAdapter;

    private IndexedLongFormAdapter indexedLongFormAdapter;

    private TitleAdapter titleAdapter;

    /**
   * Return an adapter object which will return a
   * formatted string that shows the song in a short form.
   */
    public Object getShortFormAdapter() {
        if (shortFormAdapter == null) {
            synchronized (this) {
                if (shortFormAdapter == null) {
                    shortFormAdapter = new ShortFormAdapter();
                }
            }
        }
        return shortFormAdapter;
    }

    /**
   * Return an adapter object which will return a
   * formatted string that shows the song in a short form.
   */
    public Object getLongFormAdapter() {
        if (longFormAdapter == null) {
            synchronized (this) {
                if (longFormAdapter == null) {
                    longFormAdapter = new LongFormAdapter();
                }
            }
        }
        return longFormAdapter;
    }

    /**
   * Return an adapter object which will return a
   * formatted string that shows the song
   */
    public Object getIndexedLongFormAdapter() {
        if (indexedLongFormAdapter == null) {
            synchronized (this) {
                if (indexedLongFormAdapter == null) {
                    indexedLongFormAdapter = new IndexedLongFormAdapter();
                }
            }
        }
        return indexedLongFormAdapter;
    }

    /**
   * Return an adapter object which will return a
   * formatted string that shows the song
   */
    public Object getTitleAdapter() {
        if (titleAdapter == null) {
            synchronized (this) {
                if (titleAdapter == null) {
                    titleAdapter = new TitleAdapter();
                }
            }
        }
        return titleAdapter;
    }

    protected class TitleAdapter extends BasicAdapter {

        /**
     * Return just the title
     */
        public String toString() {
            return title;
        }
    }

    protected class ShortFormAdapter extends BasicAdapter {

        /**
     * Return a condensed string representing the song
     */
        public String toString() {
            StringBuffer str = new StringBuffer();
            if (index >= 0) {
                appendIndex(str, index);
                str.append(". ");
            }
            str.append(time((int) (estimatedPos / 1000)));
            str.append('/');
            str.append(time(total));
            return str.toString();
        }
    }

    protected class IndexedLongFormAdapter extends LongFormAdapter {

        protected String internalToString(StringBuffer str) {
            if (index >= 0) {
                appendIndex(str, index);
                str.append(". ");
            }
            return super.internalToString(str);
        }
    }

    protected class LongFormAdapter extends BasicAdapter {

        /**
     * Return a long string that shows most of all the fields
     * for the song
     */
        public String toString() {
            StringBuffer str = new StringBuffer();
            return internalToString(str);
        }

        protected String internalToString(StringBuffer str) {
            str.append(title);
            if (artist != null) {
                str.append(" - ");
                str.append(artist);
            }
            if (album != null) {
                str.append(" - ");
                str.append(album);
            }
            if (genre != null) {
                str.append(" - ");
                str.append(genre);
            }
            return str.toString();
        }
    }

    protected void setPosition(int newPos) {
        pos = newPos;
        long newDelta;
        if (pos > 0) {
            newDelta = pos * 1000 - (estimatedPos + 500);
            if (Math.abs(newDelta) > 4000) {
                estimatedPos = pos * 1000;
                delta = 0;
            } else {
                newDelta /= 10;
                delta = (delta + newDelta) / 2;
            }
        }
        firePositionUpdated();
    }

    protected void setEstimatedPosition(long newPos) {
        estimatedPos = newPos;
        firePositionUpdated();
    }

    protected void firePositionUpdated() {
        if (at != null) {
            AudioTronListener listener;
            for (Enumeration e = at.listeners.elements(); e.hasMoreElements(); ) {
                listener = (AudioTronListener) e.nextElement();
                listener.songPositionUpdated(this);
            }
        }
    }

    protected synchronized void setPlaying(boolean p) {
        if (p != playing) {
            if (playing) {
                pos = 0;
                estimatedPos = 0;
                delta = 0;
            }
            if (p) {
                addAlarm();
            }
            playing = p;
        }
    }

    private void addAlarm() {
        next = System.currentTimeMillis() + 1000 - delta;
        Timer.addAlarm(this);
    }

    public void tick() {
        if (estimatedPos + 1000 <= total * 1000 && playing && at.state != ATState.PAUSED) {
            setEstimatedPosition(estimatedPos + 1000);
        }
        if (playing) {
            addAlarm();
        }
    }

    public long getAlarm() {
        return next;
    }

    protected class BasicAdapter implements Adapter {

        public Object getSource() {
            return instance();
        }
    }

    public boolean isPlayable() {
        return (true);
    }

    public String getType() {
        return (new String("Title"));
    }

    public String getFile() {
        return (title);
    }
}
