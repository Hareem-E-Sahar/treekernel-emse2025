public class Test {    private void actionErrorWhiteList() {
        Object selected = listAllErrors.getSelectedItem();
        if ((selected instanceof CheckError) && (Utilities.isDesktopSupported())) {
            CheckError error = (CheckError) selected;
            if (error.getAlgorithm().getWhiteListPageName() != null) {
                Utilities.browseURL(getWikipedia(), error.getAlgorithm().getWhiteListPageName(), true);
            } else {
                DecimalFormat format = new DecimalFormat("000");
                Utilities.displayInformationMessage(getParentComponent(), GT._("There''s no white list defined for this error type.\n" + "If you want to define a white list you need to add :\n" + "  {0} = <page name> END\n" + "to the translation page ({1}) on \"{2}\"", new Object[] { "error_" + format.format(error.getErrorNumber()) + "_whitelistpage_" + getWikipedia().getSettings().getCodeCheckWiki(), getWikipedia().getCWConfiguration().getTranslationPage(), getWikipedia().toString() }));
            }
        }
    }
}