public class Test {    @Override
    protected Void doInBackground() throws Exception {
        int pageOffset = 0;
        Document document = null;
        PdfCopy writer = null;
        List<Object> master = new ArrayList<Object>();
        final Collection<? extends File> fl = getInputFiles();
        final MainFrame parent = getMainFrameInstance();
        final String outFile = getOutputFile();
        for (final Iterator<? extends File> fi = ((null == fl) || (fl.size() <= 0)) ? null : fl.iterator(); (fi != null) && fi.hasNext() && (!isCancelled()); ) {
            final File f = fi.next();
            if (null == f) continue;
            publish(f);
            _logger.info("processing " + f + " start");
            final long pStart = System.currentTimeMillis();
            try {
                final PdfReader reader = new PdfReader(f.getAbsolutePath());
                reader.consolidateNamedDestinations();
                final int n = reader.getNumberOfPages();
                final List<?> bookmarks = SimpleBookmark.getBookmark(reader);
                if (bookmarks != null) {
                    if (pageOffset > 0) SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                    master.addAll(bookmarks);
                }
                pageOffset += n;
                if (null == document) {
                    document = new Document(reader.getPageSizeWithRotation(1));
                    writer = new PdfCopy(document, new FileOutputStream(outFile));
                    document.open();
                    _logger.info("Opened output=" + outFile);
                }
                for (int i = 1; i <= n; i++) {
                    final PdfImportedPage page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }
                final PRAcroForm form = reader.getAcroForm();
                if (form != null) writer.copyAcroForm(reader);
            } catch (Exception e) {
                final long pEnd = System.currentTimeMillis(), pDuration = pEnd - pStart;
                _logger.error(e.getClass().getName() + " while handle input=" + f + " after " + pDuration + " msec.: " + e.getMessage(), e);
                BaseOptionPane.showMessageDialog(parent, e);
                break;
            }
            final long pEnd = System.currentTimeMillis(), pDuration = pEnd - pStart;
            _logger.info("processing " + f + " processed " + f.length() + " bytes in " + pDuration + " msec.");
        }
        try {
            if (!master.isEmpty()) writer.setOutlines(master);
            if (document != null) document.close();
            _logger.info("Closing output=" + outFile);
        } catch (Exception e) {
            _logger.error(e.getClass().getName() + " while finalize output to " + outFile + ": " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(parent, e);
        }
        return null;
    }
}