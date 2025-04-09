package juploader.gui.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Singleton dający dostęp do systemowej przeglądarki WWW, o ile takowa jest
 * obsługiwana na danej platformie.
 *
 * @author Adam Pawelec
 */
public class Browser {

    /** Instancja singletonu. */
    private static final Browser instance = new Browser();

    /** Określa, czy akcja Browse jest dostępna. */
    private boolean browseSupported;

    /** Obiekt Desktop. */
    private final Desktop desktop;

    private Browser() {
        browseSupported = isBrowseSupported();
        if (browseSupported) {
            desktop = Desktop.getDesktop();
        } else {
            desktop = null;
        }
    }

    /**
     * Zwraca instancję singletonu.
     *
     * @return instancja
     */
    public static Browser getInstance() {
        return instance;
    }

    /**
     * Sprawdzenie czy akcja browse jest dostępna.
     *
     * @return true jeżeli tak
     */
    private boolean isBrowseSupported() {
        return (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE));
    }

    /**
     * Sprawdza, czy dany link można otworzyć w przeglądarce WWW.
     *
     * @param link link do sprawdzenia
     * @return true, jeżeli link nadaje się do otworzenia w przeglądarce
     */
    public boolean isLinkBrowseable(String link) {
        if (!browseSupported) {
            return false;
        } else {
            try {
                URI url = new URI(link);
                return (url.getScheme() == null ? false : (url.getScheme().equals("http") || (url.getScheme().equals("https")) || (url.getScheme().equals("ftp"))));
            } catch (URISyntaxException ex) {
                return false;
            }
        }
    }

    /**
     * Otwiera wskazany link w przeglądarce systemowej.
     *
     * @param link link to otwarcia
     * @throws IOException w przypadku gdy nie udało się otworzyć przeglądarki
     */
    public void openInBrowser(String link) throws IOException {
        if (isLinkBrowseable(link) && desktop != null) {
            desktop.browse(stringToURI(link));
        }
    }

    /** Zamienia napis na URI z pominięciem wyjątku. */
    private URI stringToURI(String link) {
        try {
            return new URI(link);
        } catch (URISyntaxException ex) {
            return null;
        }
    }
}
