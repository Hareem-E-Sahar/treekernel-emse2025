public class Test {    public void actionOtherLanguage() {
        String url = URL_OTHER_LANGUAGE;
        if (Utilities.isDesktopSupported()) {
            Utilities.browseURL(url);
        } else {
            displayUrlMessage(GT._("You can learn how to add other languages at the following URL:"), url);
        }
    }
}