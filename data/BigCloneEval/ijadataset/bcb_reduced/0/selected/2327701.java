package at.laborg.briss.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DesktopHelper {

    public static void openFileWithDesktopApp(File cropDestinationFile) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(cropDestinationFile);
        }
    }

    public static void openDonationLink(String uri) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            URI donationURI;
            try {
                donationURI = new URI(uri);
                desktop.browse(donationURI);
            } catch (URISyntaxException e) {
            }
        }
    }
}
