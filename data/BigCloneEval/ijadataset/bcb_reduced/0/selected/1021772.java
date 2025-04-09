package org.jaytter.util;

/**
 * This class is responsible for opening a URL in the default browser.
 */
public class OpenDefaultBrowser {

    private String url;

    public OpenDefaultBrowser(String url) {
        this.url = url;
    }

    public void open() {
        if (!java.awt.Desktop.isDesktopSupported()) {
            System.err.println("Desktop is not supported (fatal)");
            return;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            System.err.println("Desktop doesn't support the browse action (fatal)");
            return;
        }
        try {
            java.net.URI uri = new java.net.URI(this.url);
            desktop.browse(uri);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }
}
