public class Test {    private void actionCheckWiki() {
        EnumWikipedia wikipedia = getWikipedia();
        if (wikipedia == null) {
            return;
        }
        if (!wikipedia.getCWConfiguration().isProjectAvailable()) {
            String url = URL_OTHER_WIKIPEDIA;
            displayUrlMessage(GT._("There's no known Check Wikipedia Project for this Wikipedia.\n" + "You can learn how to configure WikiCleaner at the following URL:"), url);
            if (Utilities.isDesktopSupported()) {
                Utilities.browseURL(url);
            }
            return;
        }
        Controller.runCheckWikiProject(getWikipedia());
    }
}