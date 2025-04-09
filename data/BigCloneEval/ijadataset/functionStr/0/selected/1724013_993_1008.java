public class Test {    private void actionMostDabLinks() {
        EnumWikipedia wikipedia = getWikipedia();
        if (wikipedia == null) {
            return;
        }
        WPCConfiguration configuration = wikipedia.getConfiguration();
        if ((configuration.getMostDisambiguationLinks() == null) || (configuration.getMostDisambiguationLinks().isEmpty())) {
            String url = URL_OTHER_WIKIPEDIA;
            displayUrlMessage(GT._("There's no known list of pages containing many disambiguation links for this Wikipedia.\n" + "You can learn how to configure WikiCleaner at the following URL:"), url);
            if (Utilities.isDesktopSupported()) {
                Utilities.browseURL(url);
            }
            return;
        }
        new PageListWorker(wikipedia, this, null, configuration.getMostDisambiguationLinks(), PageListWorker.Mode.CATEGORY_MEMBERS_ARTICLES, false, GT._("Pages with many disambiguation links")).start();
    }
}