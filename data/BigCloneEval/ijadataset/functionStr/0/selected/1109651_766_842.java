public class Test {    public static void main(final String args[]) {
        final ArgumentList alm = new ArgumentList("AutoPlotUI");
        alm.addOptionalPositionArgument(0, "URL", null, "initial URL to load");
        alm.addOptionalPositionArgument(1, "bookmarks", null, "bookmarks to load");
        alm.addOptionalSwitchArgument("port", "p", "port", "-1", "enable scripting via this port");
        alm.addBooleanSwitchArgument("scriptPanel", "s", "scriptPanel", "enable script panel");
        alm.addBooleanSwitchArgument("logConsole", "l", "logConsole", "enable log console");
        alm.addBooleanSwitchArgument("nativeLAF", "n", "nativeLAF", "use the system look and feel");
        alm.process(args);
        System.err.println("welcome to autoplot");
        Logger.getLogger("ap").info("welcome to autoplot ");
        final ApplicationModel model = new ApplicationModel();
        final String initialURL;
        final String bookmarks;
        if (alm.getValue("URL") != null) {
            initialURL = alm.getValue("URL");
            Logger.getLogger("ap").info("setting initial URL to >>>" + initialURL + "<<<");
            bookmarks = alm.getValue("bookmarks");
        } else {
            initialURL = null;
            bookmarks = null;
        }
        if (alm.getBooleanValue("scriptPanel")) {
            model.options.setScriptVisible(true);
        }
        if (alm.getBooleanValue("logConsole")) {
            model.options.setLogConsoleVisible(true);
        }
        if (alm.getBooleanValue("nativeLAF")) {
            try {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                final AutoPlotUI app = new AutoPlotUI(model);
                if (!alm.getValue("port").equals("-1")) {
                    int iport = Integer.parseInt(alm.getValue("port"));
                    app.setupServer(iport, model);
                }
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                    public void uncaughtException(Thread t, Throwable e) {
                        Logger.getLogger("virbo.autoplot").severe("runtime exception: " + e);
                        app.setStatus("caught exception: " + e.toString());
                        model.application.getExceptionHandler().handleUncaught(e);
                    }
                });
                app.setVisible(true);
                if (initialURL != null) {
                    app.dataSetSelector.setValue(initialURL);
                    app.dataSetSelector.maybePlot();
                }
                if (bookmarks != null) {
                    Runnable run = new Runnable() {

                        public void run() {
                            try {
                                final URL url = new URL(bookmarks);
                                Document doc = AutoplotUtil.readDoc(url.openStream());
                                List<Bookmark> book = Bookmark.parseBookmarks(doc);
                                model.setBookmarks(book);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                model.getCanvas().getApplication().getExceptionHandler().handle(ex);
                            }
                        }
                    };
                    new Thread(run, "LoadBookmarksThread").start();
                }
                app.setStatus("ready");
            }
        });
    }
}