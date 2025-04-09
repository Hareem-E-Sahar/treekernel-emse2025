public class Test {    private void actionErrorDetail() {
        Object selected = listAllErrors.getSelectedItem();
        if ((selected instanceof CheckError) && (Utilities.isDesktopSupported())) {
            CheckError error = (CheckError) selected;
            if (error.getAlgorithm().getLink() != null) {
                Utilities.browseURL(getWikipedia(), error.getAlgorithm().getLink(), true);
            } else {
                DecimalFormat format = new DecimalFormat("000");
                Utilities.displayInformationMessage(getParentComponent(), GT._("There''s no page defined for this error type.\n" + "If you want to define a page you need to add :\n" + "  {0} = <page name> END\n" + "to the translation page ({1}) on \"{2}\"", new Object[] { "error_" + format.format(error.getErrorNumber()) + "_link_" + getWikipedia().getSettings().getCodeCheckWiki(), getWikipedia().getCWConfiguration().getTranslationPage(), getWikipedia().toString() }));
            }
        }
    }
}