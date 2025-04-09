public class Test {    private void actionOptionsSystem() {
        if (Utilities.isDesktopSupported()) {
            EnumWikipedia wikipedia = getWikipedia();
            Utilities.browseURL(wikipedia, wikipedia.getConfigurationPage(), true);
        } else {
            displayUrlMessage(GT._("You can learn how to configure WikiCleaner at the following URL:"), URL_OTHER_WIKIPEDIA);
        }
    }
}