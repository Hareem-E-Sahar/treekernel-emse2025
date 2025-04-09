public class Test {    private void actionIdea() {
        String url = URL_TALK_PAGE;
        if (Utilities.isDesktopSupported()) {
            Utilities.browseURL(url);
        } else {
            displayUrlMessage(GT._("You can submit bug reports or feature requests at the following URL:"), url);
        }
    }
}