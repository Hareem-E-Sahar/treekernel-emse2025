public class Test {    protected static void setLinkHandler(JEditorPane ep) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                ep.addHyperlinkListener(new HyperlinkListener() {

                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            try {
                                desktop.browse(e.getURL().toURI());
                            } catch (URISyntaxException use) {
                                use.printStackTrace();
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }
                    }
                });
                return;
            }
        } catch (NoClassDefFoundError e) {
        }
        ((HTMLEditorKit) ep.getEditorKit()).setLinkCursor(null);
    }
}