public class Test {        public void actionPerformed(ActionEvent ae) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop d = Desktop.getDesktop();
                    d.browse(new URI(Init.SITE_URL));
                } catch (Exception e) {
                    Methods.displaySimpleError(framer, "Can't access website; check internet connection?\nPlease visit '" + Init.GPL_URL + "' to view GPL");
                }
            } else {
                Methods.displaySimpleAlert(framer, "Please visit '" + Init.SITE_URL + "' to check latest version");
            }
        }
}