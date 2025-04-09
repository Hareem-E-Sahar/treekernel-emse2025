public class Test {            public void run() {
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
}