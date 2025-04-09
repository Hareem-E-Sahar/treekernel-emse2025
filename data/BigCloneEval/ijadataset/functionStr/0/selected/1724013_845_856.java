public class Test {    private void actionHelp() {
        EnumWikipedia wikipedia = getWikipedia();
        String url = EnumWikipedia.EN.getConfiguration().getHelpURL();
        if ((wikipedia != null) && (wikipedia.getConfiguration().getHelpURL() != null)) {
            url = wikipedia.getConfiguration().getHelpURL();
        }
        if (Utilities.isDesktopSupported()) {
            Utilities.browseURL(url);
        } else {
            displayUrlMessage(GT._("You can read the help on Wikipedia Cleaner at the following URL:"), url);
        }
    }
}