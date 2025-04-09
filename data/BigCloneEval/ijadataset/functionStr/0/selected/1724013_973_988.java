public class Test {    private void actionCurrentDabList() {
        EnumWikipedia wikipedia = getWikipedia();
        if (wikipedia == null) {
            return;
        }
        WPCConfiguration configuration = wikipedia.getConfiguration();
        if ((configuration.getCurrentDisambiguationList() == null) || (configuration.getCurrentDisambiguationList().isEmpty())) {
            String url = URL_OTHER_WIKIPEDIA;
            displayUrlMessage(GT._("There's no known list of disambiguation pages for this Wikipedia.\n" + "You can learn how to configure WikiCleaner at the following URL:"), url);
            if (Utilities.isDesktopSupported()) {
                Utilities.browseURL(url);
            }
            return;
        }
        new PageListWorker(wikipedia, this, null, configuration.getCurrentDisambiguationList(), PageListWorker.Mode.INTERNAL_LINKS, false, GT._("Current disambiguation list")).start();
    }
}