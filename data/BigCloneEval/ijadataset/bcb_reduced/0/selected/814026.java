package de.nomule.outputhandler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.farng.mp3.*;
import org.farng.mp3.id3.*;
import de.nomule.applogic.NoMuleRuntime;
import de.nomule.applogic.OutputprofileHandler;
import de.nomule.applogic.Settings;
import de.nomule.common.HTTP;

public class Mp3TaggingHandler implements OutputprofileHandler {

    private Hashtable<String, String> hstValues;

    private Mp3NddParser nddFile = null;

    private String strOutputfile = "";

    private String getAuthor() {
        if (nddFile != null && nddFile.hasAuthor()) return nddFile.getAuthor();
        String s = "";
        StringTokenizer t = new StringTokenizer(strOutputfile, "/");
        while (t.hasMoreTokens()) s = t.nextToken();
        t = new StringTokenizer(s, "-");
        if (t.hasMoreTokens()) s = t.nextToken();
        return s.replace('_', ' ').trim();
    }

    private String getTitle() {
        if (nddFile != null && nddFile.hasTitle()) return nddFile.getTitle();
        String s = "";
        StringTokenizer t = new StringTokenizer(strOutputfile, "/");
        while (t.hasMoreTokens()) s = t.nextToken();
        t = new StringTokenizer(s, "-");
        if (t.hasMoreTokens()) s = t.nextToken();
        if (t.hasMoreTokens()) s = t.nextToken();
        t = new StringTokenizer(s, ".");
        if (t.hasMoreTokens()) s = t.nextToken();
        return s.replace('_', ' ').trim();
    }

    private String getAlbum() {
        String strAlbum = "";
        if (nddFile != null && nddFile.hasAlbum()) strAlbum = nddFile.getAlbum(); else {
            String title = getTitle();
            String url = "";
            try {
                url = "http://musicbrainz.org/ws/1/track/?type=xml&title=" + URLEncoder.encode(title, "UTF-8") + "&artist=" + URLEncoder.encode(getAuthor(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String ret = HTTP.get(url);
            String regexp = "<title>[^<]*</title>";
            Pattern p = Pattern.compile(regexp);
            Matcher m = p.matcher(ret);
            while (m.find()) {
                String s = ret.substring(m.start() + 7, m.end() - 8);
                if (!s.toUpperCase().equals(title.toUpperCase())) {
                    strAlbum = s;
                    break;
                }
            }
        }
        try {
            String strImgUrl = "";
            if (nddFile != null && nddFile.hasAlbumImage()) strImgUrl = nddFile.getAlbumImage(); else {
                String url = "http://albumart.org/index.php?srchkey=" + URLEncoder.encode(strAlbum, "UTF-8") + "&itempage=1&newsearch=1&searchindex=Music";
                String ret = HTTP.get(url);
                String regexp = "\"http:\\/\\/[^\"\']*amazon[^\"\']*\\.jpg\"";
                Pattern p = Pattern.compile(regexp);
                Matcher m = p.matcher(ret);
                m.find();
                strImgUrl = ret.substring(m.start() + 1, m.end() - 1);
            }
            HTTP.download(strImgUrl, strOutputfile + ".jpg", new javax.swing.JLabel());
        } catch (IOException e) {
            System.out.println("Hallo");
        } catch (IllegalStateException e) {
            System.out.println("Couldnt find an Album image");
        }
        return strAlbum;
    }

    private String getLyrics() {
        if (nddFile != null && nddFile.hasLyrics()) {
            String strLyricsUrl = nddFile.getLyrics();
            return HTTP.get(strLyricsUrl);
        }
        String strAuthor = getAuthor();
        String strTitle = getTitle();
        String strLyricsUrl = "";
        try {
            strLyricsUrl = "http://lyricwiki.org/api.php?artist=" + URLEncoder.encode(strAuthor, "UTF-8") + "&song=" + URLEncoder.encode(strTitle, "UTF-8") + "&fmt=text";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String strLyrics = HTTP.get(strLyricsUrl);
        String strNl = "";
        strNl += ((char) 13);
        strNl += ((char) 10);
        return strLyrics.replace(strNl, "\n");
    }

    public void run() {
        if (NoMuleRuntime.DEBUG) System.out.println("Mp3TaggingHandler - Running!!");
        strOutputfile = hstValues.get("$O");
        String strNddFile = hstValues.get("$D");
        if (strNddFile != null && (strNddFile.length() > 0)) nddFile = new Mp3NddParser(strNddFile);
        try {
            MP3File file = new MP3File(strOutputfile);
            ID3v2_3 tag = new ID3v2_3();
            String strAuthor = getAuthor();
            tag.setAuthorComposer(strAuthor);
            tag.setLeadArtist(strAuthor);
            String strTitle = getTitle();
            tag.setSongTitle(strTitle);
            String strAlbum = getAlbum();
            tag.setAlbumTitle(strAlbum);
            FrameBodyUSLT u = new FrameBodyUSLT((byte) 0, "deu", "", getLyrics());
            ID3v2_3Frame f2 = new ID3v2_3Frame();
            f2.setBody(u);
            tag.setFrame(f2);
            file.setID3v2Tag(tag);
            file.save();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            System.out.println("Oh neyyy - A TagException called e");
        } catch (ClassCastException e) {
        }
        String strFileDelete = strOutputfile.replace(".mp3", ".original.mp3");
        System.out.println(strFileDelete);
        File f = new File(strFileDelete);
        f.deleteOnExit();
    }

    public void setValues(Hashtable<String, String> t) {
        hstValues = t;
    }
}
