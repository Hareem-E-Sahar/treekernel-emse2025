public class Test {    public void actionOtherWikipedia() {
        String url = URL_OTHER_WIKIPEDIA;
        if (Utilities.isDesktopSupported()) {
            Utilities.browseURL(url);
        } else {
            displayUrlMessage(GT._("You can learn how to add other Wikipedia at the following URL:"), url);
        }
    }
}