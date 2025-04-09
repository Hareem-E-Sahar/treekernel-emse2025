package code_package;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author n1ghtstalker
 */
public class web_search {

    public void google_search(String input) throws URISyntaxException {
        try {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            URI uri;
            uri = new URI("http://www.google.gr/search?hl=el&q=" + input.replace(' ', '+') + "&btnG=%CE%91%CE%BD%CE%B1%CE%B6%CE%AE%CF%84%CE%B7%CF%83%CE%B7&meta=");
            desktop.browse(uri);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void isohunt_search(String input) throws URISyntaxException {
        try {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            URI uri;
            uri = new URI("http://isohunt.com/torrents/?ihq=" + input.replace(' ', '+'));
            desktop.browse(uri);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void youtube_search(String input) throws URISyntaxException {
        try {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            URI uri;
            uri = new URI("http://www.youtube.com/results?search_query=" + input.replace(' ', '+') + "&search_type=&aq=f");
            desktop.browse(uri);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void imdb_search(String input) throws URISyntaxException {
        try {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            URI uri;
            uri = new URI("http://www.imdb.com/find?s=all&q=" + input.replace(' ', '+') + "&x=0&y=0");
            desktop.browse(uri);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
