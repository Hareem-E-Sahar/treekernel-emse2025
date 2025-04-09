import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;

public class Control_http_Shoutcast {

    private BufferedReader bw = null;

    private InputStream readGenresStream = null;

    private InputStream readStream = null;

    private String text = "";

    private Vector<Vector<String>> addGenre = new Vector<Vector<String>>(0, 1);

    private Vector<String> tmpGenres = new Vector<String>(0, 1);

    private Vector<String[]> streams = new Vector<String[]>(0, 1);

    private String subpage = "";

    private Boolean stopSearching = false;

    private int numberOfStreams = 1;

    public Control_http_Shoutcast() {
    }

    /**
	 * This method look for the stream address + port in a .pls or .m3u file and
	 * return the first one found. If it found a stream than it returns it. else
	 * it returns an empty string
	 * 
	 * @param streamURL
	 * @return
	 */
    public String getfirstStreamFromURL(String streamURL) {
        String url = "";
        String tmp = "";
        Boolean breakLook = false;
        try {
            URL stream = new URL(streamURL);
            readStream = stream.openStream();
            bw = new BufferedReader(new InputStreamReader(readStream));
            while (!breakLook && (tmp = bw.readLine()) != null) {
                if (tmp.contains("File")) {
                    int startAddress = tmp.indexOf("=");
                    url = tmp.substring(startAddress + 1);
                    breakLook = true;
                } else if (tmp.contains("http://")) {
                    url = tmp;
                    breakLook = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (readStream != null) {
                try {
                    readStream.close();
                } catch (IOException e) {
                }
            }
        }
        return url;
    }

    /**
	 * Get all Genres from the website www.shoutcast.com save it into a String
	 * Vector in this class
	 */
    public void getGenresFromWebsite() {
        try {
            URL shoutcast = new URL("http://classic.shoutcast.com");
            readGenresStream = shoutcast.openStream();
            bw = new BufferedReader(new InputStreamReader(readGenresStream));
            while (!stopSearching && (text = bw.readLine()) != null) {
                if (text.trim().startsWith("<td class=\"SearchBox\"")) {
                    while (!stopSearching && (text = bw.readLine()) != null) {
                        if (text.contains("<form action=")) {
                            Scanner f = new Scanner(text).useDelimiter("\\s*\"\\s*");
                            f.next();
                            subpage = f.next();
                            f.close();
                        }
                        if (text.contains("<OPTION VALUE=")) {
                            Scanner f = new Scanner(text).useDelimiter("\\s*\"\\s*");
                            f.next();
                            f.next();
                            String tmp = f.next();
                            if (!tmp.startsWith("> - ")) {
                                if (tmpGenres.capacity() > 0) addGenre.add(tmpGenres);
                                tmpGenres = new Vector<String>(0, 1);
                                tmpGenres.add(tmp.substring(1));
                            } else {
                                tmpGenres.add(tmp.substring(4));
                            }
                            f.close();
                        }
                        if (text.contains("</SELECT>")) {
                            stopSearching = true;
                        }
                    }
                }
            }
            if (tmpGenres.capacity() > 0) {
                addGenre.add(tmpGenres);
            }
            if (addGenre.get(0).get(0).equals("lass=")) {
                addGenre.remove(0);
                addGenre.trimToSize();
            }
            tmpGenres = new Vector<String>(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopSearching = false;
            if (readGenresStream != null) {
                try {
                    readGenresStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
	 * Browse the list of stream on shoutcast.com in the given genre and save it
	 * into an Array of Strings into an vector called streaminfo. streaminfo
	 * contains following information: streaminfo[0] = Name streaminfo[1] =
	 * Website streaminfo[2] = Genre streaminfo[3] = now Playing streaminfo[4] =
	 * Listeners/MaxListeners streaminfo[5] = Bitrate streaminfo[6] = Format
	 * streaminfo[7] = Link
	 * 
	 * @param genre
	 */
    public void getStreamsPerGenre(String genre) {
        streams.removeAllElements();
        streams.trimToSize();
        try {
            URL shoutcast = new URL("http://classic.shoutcast.com" + subpage + "?sgenre=" + genre + "&numresult=100");
            readGenresStream = shoutcast.openStream();
            bw = new BufferedReader(new InputStreamReader(readGenresStream));
            String[] streamInfo = new String[8];
            while (!stopSearching && (text = bw.readLine()) != null) {
                if (text.contains("<b>" + numberOfStreams + "</b>")) {
                    if (numberOfStreams > 1) {
                        streams.add(streamInfo);
                        streamInfo = new String[8];
                    }
                    int x = streamInfo.length;
                    for (int fx = 0; fx < x; fx++) {
                        streamInfo[fx] = "";
                    }
                    numberOfStreams++;
                }
                try {
                    if (text.contains("/sbin/shoutcast-playlist")) {
                        Scanner f = new Scanner(text).useDelimiter("\\s*href=\"\\s*");
                        f.next();
                        String tmpA = f.next();
                        f = new Scanner(tmpA).useDelimiter("\\s*\"\\s*");
                        streamInfo[7] = f.next();
                        f.close();
                    }
                    if (text.contains("<b>[")) {
                        Scanner f = new Scanner(text).useDelimiter("\\s*<b>\\s*");
                        f.next();
                        String tmpA = f.next();
                        f = new Scanner(tmpA).useDelimiter("\\s*]\\s*");
                        streamInfo[2] = f.next().substring(1);
                        f.close();
                    }
                    if (text.contains("_scurl\"")) {
                        Scanner f = new Scanner(text).useDelimiter("\\s*href=\"\\s*");
                        f.next();
                        String tmpA = f.next();
                        f = new Scanner(tmpA).useDelimiter("\\s*\"\\s*");
                        streamInfo[1] = f.next();
                        f.close();
                        f = new Scanner(text).useDelimiter("\\s*<a\\s*");
                        f.next();
                        tmpA = f.next();
                        f = new Scanner(tmpA).useDelimiter("\\s*\">\\s*");
                        f.next();
                        tmpA = f.next();
                        f = new Scanner(tmpA).useDelimiter("\\s*</a\\s*");
                        streamInfo[0] = f.next();
                        f.close();
                    }
                    if (text.contains("Now Playing")) {
                        Scanner f = new Scanner(text).useDelimiter("\\s*</font>\\s*");
                        f.next();
                        streamInfo[3] = f.next();
                        f.close();
                        bw.readLine();
                        text = bw.readLine();
                        f = new Scanner(text).useDelimiter("\\s*font\\s*");
                        f.next();
                        String tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*\">\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*<\\s*");
                        streamInfo[4] = f.next();
                        bw.readLine();
                        text = bw.readLine();
                        f = new Scanner(text).useDelimiter("\\s*font\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*\">\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*<\\s*");
                        streamInfo[5] = f.next();
                        f.close();
                        bw.readLine();
                        bw.readLine();
                        bw.readLine();
                        bw.readLine();
                        text = bw.readLine();
                        f = new Scanner(text).useDelimiter("\\s*font\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*\">\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*<\\s*");
                        streamInfo[6] = f.next();
                        f.close();
                    }
                    if (text.trim().equals("</font></td>")) {
                        bw.readLine();
                        text = bw.readLine();
                        Scanner f = new Scanner(text).useDelimiter("\\s*font\\s*");
                        f.next();
                        String tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*\">\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*<\\s*");
                        streamInfo[4] = f.next();
                        bw.readLine();
                        text = bw.readLine();
                        f = new Scanner(text).useDelimiter("\\s*font\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*\">\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*<\\s*");
                        streamInfo[5] = f.next().trim();
                        f.close();
                        bw.readLine();
                        bw.readLine();
                        bw.readLine();
                        bw.readLine();
                        text = bw.readLine();
                        f = new Scanner(text).useDelimiter("\\s*font\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*\">\\s*");
                        f.next();
                        tmpB = f.next();
                        f = new Scanner(tmpB).useDelimiter("\\s*<\\s*");
                        streamInfo[6] = f.next();
                        f.close();
                    }
                } catch (NoSuchElementException f) {
                    System.out.println("Cant find everything the the html");
                }
            }
        } catch (Exception e) {
            System.out.println("HHHIIIIIIIIERRR");
            if (e.getMessage().startsWith("stream is closed")) {
                stopSearching = true;
            } else e.printStackTrace();
        } finally {
            stopSearching = false;
            numberOfStreams = 1;
            if (readGenresStream != null) {
                try {
                    readGenresStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
	 * This Method return an Vector of Strings witch contains all genres roots
	 * from site
	 * 
	 * @return
	 */
    public Vector<Vector<String>> getAllGenres() {
        return addGenre;
    }

    /**
	 * This Method return an Vector of Strings witch contains all streams from a
	 * specific genre
	 * 
	 * @return
	 */
    public Vector<String[]> getStreams() {
        return streams;
    }

    public String getBaseAddress() {
        String shoutcast = "http://classic.shoutcast.com";
        return shoutcast;
    }
}
