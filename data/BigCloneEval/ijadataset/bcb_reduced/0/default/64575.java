import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagConstant;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.*;

public class AutoTag {

    public static String getAuthor(String strFilename) {
        String[] parts = strFilename.split("-");
        return parts[0].replace('_', ' ');
    }

    public static String getTrackname(String strFilename) {
        String[] parts = strFilename.split("-");
        return parts[1].replace('_', ' ');
    }

    public static String getFileInformation(String strFullname) {
        File f = new File(strFullname);
        String ret = f.getName();
        return ret.substring(0, ret.lastIndexOf('.'));
    }

    public static String getLyrics(String author, String track) {
        String url = "http://lyricwiki.org/api.php?artist=" + author.replace(' ', '_') + "&song=" + track.replace(' ', '_') + "&fmt=text";
        String ret = HTTP.get(url);
        return ret;
    }

    public static void getAlbumImage(String album, String author, String sFilename) throws UnsupportedEncodingException {
        String url = "http://images.google.com/images?q=" + album.replace(' ', '+') + "+" + author.replace(' ', '+');
        String ret = HTTP.get(url);
        String regexp = "\"http:\\/\\/[^\"\']*\\.jpg\"";
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(ret);
        m.find();
        String imgUrl = ret.substring(m.start() + 1, m.end() - 1);
        System.out.println(imgUrl);
        HTTP.download(imgUrl, sFilename);
    }

    public static String getAlbumName(String author, String track) {
        String url = "http://musicbrainz.org/ws/1/track/?type=xml&title=" + track.replace(' ', '+') + "&artist=" + author.replace(' ', '+');
        String ret = HTTP.get(url);
        System.out.println(url);
        String regexp = "<title>[^<]*</title>";
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(ret);
        while (m.find()) {
            String s = ret.substring(m.start() + 7, m.end() - 8);
            if (!s.toUpperCase().equals(track.toUpperCase())) return s;
        }
        return "";
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        if (args.length < 1) System.exit(0);
        String sFile = args[0];
        String s = getFileInformation(sFile);
        String sAuthor = getAuthor(s);
        String sTrack = getTrackname(s);
        String sAlbum = getAlbumName(sAuthor, sTrack);
        getAlbumImage(sAlbum, sAuthor, sFile + ".jpg");
        String sNl = "";
        sNl += ((char) 13);
        sNl += ((char) 10);
        String sLyrics = getLyrics(sAuthor, sTrack).replace("\n", sNl);
        try {
            MP3File f = new MP3File(sFile);
            ID3v2_3 i = new ID3v2_3();
            if (!sAlbum.equals("")) {
                System.out.println("Setting Album Title:" + sAlbum);
                i.setAlbumTitle(sAlbum);
            }
            System.out.println("Setting Album Composer:" + sAuthor);
            i.setAuthorComposer(sAuthor);
            System.out.println("Setting Lead Artist:" + sAuthor);
            i.setLeadArtist(sAuthor);
            System.out.println("Setting Song Title:" + sTrack);
            i.setSongTitle(sTrack);
            if (!sLyrics.equals("")) {
                FrameBodyUSLT u = new FrameBodyUSLT((byte) 0, "deu", "", sLyrics);
                ID3v2_3Frame f2 = new ID3v2_3Frame();
                f2.setBody(u);
                i.setFrame(f2);
            }
            f.setID3v2Tag(i);
            f.save();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
