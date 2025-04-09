public class Test {    private void actionHelpRequestedOn() {
        EnumWikipedia wikipedia = getWikipedia();
        if (wikipedia == null) {
            return;
        }
        WPCConfiguration configuration = wikipedia.getConfiguration();
        if (configuration.getTemplatesForHelpRequested() == null) {
            String url = URL_OTHER_WIKIPEDIA;
            displayUrlMessage(GT._("There's no known template for requesting help for the Wikipedia.\n" + "You can learn how to configure WikiCleaner at the following URL:"), url);
            if (Utilities.isDesktopSupported()) {
                Utilities.browseURL(url);
            }
        } else {
            List<String> pageNames = new ArrayList<String>();
            for (Page template : configuration.getTemplatesForHelpRequested()) {
                pageNames.add(template.getTitle());
            }
            new PageListWorker(wikipedia, this, null, pageNames, PageListWorker.Mode.EMBEDDED_IN, false, GT._("Help requested on...")).start();
        }
    }
}