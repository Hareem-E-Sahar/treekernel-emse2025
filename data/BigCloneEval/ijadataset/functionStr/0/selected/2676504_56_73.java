public class Test {            public void run() {
                if (!initialized) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop d = Desktop.getDesktop();
                        if (d.isSupported(Desktop.Action.BROWSE)) {
                            TextView.this.desktop = d;
                        }
                    }
                    initialized = true;
                }
                if (desktop != null) {
                    try {
                        desktop.browse(url.toURI());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
}