public class Test {        public void actionPerformed(ActionEvent ae) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop d = Desktop.getDesktop();
                    d.browse(new URI(MasteryCards.GPL_URL));
                } catch (Exception e) {
                    Methods.displaySimpleError(framer, "Can't access website; check internet connection?\nPlease visit '" + MasteryCards.GPL_URL + "' to view GPL");
                }
            } else {
                Methods.displaySimpleAlert(framer, "Please visit '" + MasteryCards.GPL_URL + "' to view GPL");
            }
        }
}