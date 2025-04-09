public class Test {    protected PdfViewer(Window frame, String title, boolean modal, File fileToView, XMLNotePreferences prefs) throws IOException {
        super(frame, title);
        super.setModal(true);
        _fileToView = fileToView;
        _tmpFileToView = File.createTempFile("HOD", ".pdf");
        _tmpFileToView.deleteOnExit();
        copyFile(_fileToView, _tmpFileToView);
        _pageFormat = PrinterJob.getPrinterJob().defaultPage();
        _tr = new DefaultXMLNoteTranslator();
        _toolbar = new JXMLNoteToolBar(this, null);
        _toolbar.setButtonSize(24);
        _toolbar.removeAllSections();
        _toolbar.addSection("file", _tr._("Save PDF document"));
        _toolbar.add("file", JXMLNoteToolBar.ACTION_SAVE, _tr._("save PDF document"), this);
        _toolbar.addSection("navigating", _tr._("Navigate the PDF document"));
        _toolbar.add("navigating", JXMLNoteToolBar.ACTION_FIRST, _tr._("First page"), this);
        _toolbar.add("navigating", JXMLNoteToolBar.ACTION_PREVIOUS, _tr._("Previous page"), this);
        _toolbar.add("navigating", JXMLNoteToolBar.ACTION_NEXT, _tr._("Next page"), this);
        _toolbar.add("navigating", JXMLNoteToolBar.ACTION_LAST, _tr._("Last page"), this);
        _toolbar.addSection("zooming", _tr._("Zooming"));
        _toolbar.add("zooming", JXMLNoteToolBar.ACTION_ZOOM_FIT_WIDTH, _tr._("Fit Width"), this);
        _toolbar.add("zooming", JXMLNoteToolBar.ACTION_ZOOM_FIT_HEIGHT, _tr._("Fit Height"), this);
        _toolbar.add("zooming", JXMLNoteToolBar.ACTION_ZOOM_100, _tr._("100%"), this);
        _toolbar.add("zooming", JXMLNoteToolBar.ACTION_ZOOM_LESS, _tr._("10% less"), this);
        _toolbar.add("zooming", JXMLNoteToolBar.ACTION_ZOOM_MORE, _tr._("10% more"), this);
        _toolbar.addSection("printing", _tr._("Printing"));
        _toolbar.add("printing", JXMLNoteToolBar.ACTION_PRINT, _tr._("Print this document"), this);
        _toolbar.add("printing", JXMLNoteToolBar.ACTION_PRINT_PREFS, _tr._("Printer preferences"), this);
        _toolbar.initToolBar();
        _bar = new JMenuBar();
        JMenu window = JRecentlyUsedMenu.makeMenu(_tr._("_Window"));
        JXMLNoteIcon icn = new JXMLNoteIcon(JXMLNoteToolBar.ACTION_CLOSE);
        JMenuItem close = JRecentlyUsedMenu.makeMenuItem(_tr._("_Close"), icn, JXMLNoteToolBar.ACTION_CLOSE, this);
        _bar.add(window);
        window.add(close);
        JPanel mb = new JPanel(new BorderLayout());
        mb.add(_bar, BorderLayout.NORTH);
        JPanel view = new JPanel(new BorderLayout());
        view.add(_toolbar, BorderLayout.NORTH);
        _pdfpanel = new PdfPagePanel(_pageFormat);
        view.add(_pdfpanel, BorderLayout.CENTER);
        mb.add(view, BorderLayout.CENTER);
        StatusBar sbar = new StatusBar();
        _zoomLabel = new JLabel();
        _pageNrLabel = new JLabel();
        _pagesLabel = new JLabel();
        sbar.add(_zoomLabel);
        sbar.add(new JLabel("|"));
        sbar.add(_pageNrLabel);
        sbar.add(new JLabel("/"));
        sbar.add(_pagesLabel);
        mb.add(sbar, BorderLayout.SOUTH);
        this.add(mb);
        _raf = new RandomAccessFile(_tmpFileToView, "r");
        _channel = _raf.getChannel();
        ByteBuffer buf = _channel.map(FileChannel.MapMode.READ_ONLY, 0, _channel.size());
        _pdffile = new PDFFile(buf);
        _closed = false;
        _pageNr = 1;
        _pageNrLabel.setText(Integer.toString(_pageNr));
        _pagesLabel.setText(Integer.toString(_pdffile.getNumPages()));
        _preferences = prefs;
        applyPreferences();
        this.addWindowListener(new WindowListener() {

            public void windowActivated(WindowEvent arg0) {
            }

            public void windowClosed(WindowEvent arg0) {
            }

            public void windowDeactivated(WindowEvent arg0) {
            }

            public void windowDeiconified(WindowEvent arg0) {
            }

            public void windowIconified(WindowEvent arg0) {
            }

            public void windowOpened(WindowEvent arg0) {
            }

            public void windowClosing(WindowEvent arg0) {
                storePrefs();
                try {
                    closePdf();
                } catch (IOException e) {
                    DefaultXMLNoteErrorHandler.exception(e);
                }
            }
        });
    }
}