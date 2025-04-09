package org.alastairmailer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import org.apache.lucene.document.Document;
import org.apache.pdfbox.searchengine.lucene.LucenePDFDocument;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;

public class PDFHandler extends FiletypeHandler {

    public static final ImageIcon PDF_ICON_SMALL = new ImageIcon(LuceneSidePanelPlugin.getResourceURL("images/pdf-icon-small.png"));

    public static final ImageIcon PDF_ICON_LARGE = new ImageIcon(LuceneSidePanelPlugin.getResourceURL("images/pdf-icon-large.png"));

    public static final ImageIcon UP_ICON = new ImageIcon(LuceneSidePanelPlugin.getResourceURL("images/up.png"));

    public static final ImageIcon DOWN_ICON = new ImageIcon(LuceneSidePanelPlugin.getResourceURL("images/down.png"));

    public static final ImageIcon PAGEUP_ICON = new ImageIcon(LuceneSidePanelPlugin.getResourceURL("images/page-up.png"));

    public static final ImageIcon PAGEDOWN_ICON = new ImageIcon(LuceneSidePanelPlugin.getResourceURL("images/page-down.png"));

    public static final ImageIcon ZOOMIN_ICON = new ImageIcon(LuceneSidePanelPlugin.getResourceURL("images/zoom-in.png"));

    public static final ImageIcon ZOOMOUT_ICON = new ImageIcon(LuceneSidePanelPlugin.getResourceURL("images/zoom-out.png"));

    public Icon getIcon() {
        return PDFHandler.PDF_ICON_SMALL;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".pdf");
    }

    @Override
    protected Document getDocument(File f) throws IOException {
        Logger pdfBox = Logger.getLogger("org.apache.pdfbox.util.PDFStreamEngine");
        pdfBox.setLevel(Level.WARNING);
        Document doc = LucenePDFDocument.getDocument(f);
        return doc;
    }

    public String getUID(File f) {
        return f.getAbsolutePath() + File.separator + f.lastModified();
    }

    @Override
    public void cache(File f, String[] terms) {
        super.cache(f, terms);
        cacher.requestCache(f, 1, terms);
    }

    @Override
    public void decache(File f) {
        super.decache(f);
        cacher.decache(f);
    }

    public PDFHandler() {
        super();
        PdfDecoder.init(true);
        contextPanel = buildPanel();
        cacher = new PDFImageCacher();
    }

    private JPanel contextPanel;

    public JPanel getButtonBar() {
        JPanel buttonBar = new JPanel();
        buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.LINE_AXIS));
        buttonBar.add(Box.createHorizontalGlue());
        upButton = new JButton(UP_ICON);
        upButton.setEnabled(false);
        upButton.setToolTipText("Previous match");
        buttonBar.add(upButton);
        downButton = new JButton(DOWN_ICON);
        downButton.setEnabled(false);
        downButton.setToolTipText("Next match");
        buttonBar.add(downButton);
        buttonBar.add(Box.createHorizontalStrut(20 + 10));
        pageUpButton = new JButton(PAGEUP_ICON);
        pageUpButton.setEnabled(false);
        pageUpButton.setToolTipText("Previous page");
        buttonBar.add(pageUpButton);
        pageDownButton = new JButton(PAGEDOWN_ICON);
        pageDownButton.setEnabled(false);
        pageDownButton.setToolTipText("Next page");
        buttonBar.add(pageDownButton);
        buttonBar.add(Box.createHorizontalStrut(20 + 10));
        openPDFButton = new JButton(PDF_ICON_LARGE);
        openPDFButton.setToolTipText("Open PDF in external viewer");
        if (Desktop.isDesktopSupported() == false) {
            openPDFButton.setEnabled(false);
        }
        buttonBar.add(openPDFButton);
        buttonBar.add(Box.createHorizontalGlue());
        setListeners();
        return buttonBar;
    }

    private JPanel buildPanel() {
        JPanel p = new JPanel();
        GridBagLayout gBagPDF = new GridBagLayout();
        GridBagConstraints cPDF = new GridBagConstraints();
        p.setLayout(gBagPDF);
        cPDF.gridx = 0;
        cPDF.gridy = 0;
        cPDF.weightx = 0.9;
        cPDF.weighty = 0.5;
        cPDF.fill = GridBagConstraints.BOTH;
        scroll = new JScrollPane();
        scroll.setViewportView(pdfLabel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getHorizontalScrollBar().setUnitIncrement(10);
        p.add(scroll, cPDF);
        return p;
    }

    private void setListeners() {
        pageDownButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                currentDoc.showNextPage();
            }
        });
        pageUpButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                currentDoc.showPreviousPage();
            }
        });
        openPDFButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingWorker<Object, Object> openExternal = new SwingWorker<Object, Object>() {

                    @Override
                    protected Object doInBackground() throws Exception {
                        Desktop.getDesktop().open(currentFile);
                        return null;
                    }

                    @Override
                    protected void done() {
                        super.done();
                        contextPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                };
                contextPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                openExternal.execute();
            }
        });
        downButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                currentDoc.showNextMatch();
            }
        });
        upButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                currentDoc.showPreviousMatch();
            }
        });
    }

    private JScrollPane scroll;

    private void centreRectangle(Rectangle h) {
        JViewport view = scroll.getViewport();
        int centreX = (int) Math.round(h.getCenterX() - view.getViewRect().width / 2.0);
        int centreY = (int) Math.round(h.getCenterY() - view.getViewRect().height / 2.0);
        view.setViewPosition(new Point(centreX, centreY));
    }

    @Override
    public Component getContextComponent() {
        return contextPanel;
    }

    private JLabel pdfLabel = new JLabel();

    private float zoom = 1.5f;

    private JButton pageUpButton;

    private JButton pageDownButton;

    private PDFImageCacher cacher;

    private File currentFile;

    private String[] search;

    private JButton openPDFButton;

    private JButton upButton;

    private JButton downButton;

    class PDFDocument extends SwingWorker<List<PageRectangle>, PDFHighlightImage> {

        private File loadFile;

        private PDFImageCacher imageCache;

        private PdfPageData metadata;

        private Rectangle selectedMatch;

        private int currentPage = -10;

        private int currentMatch = -1;

        public PDFDocument(File f, PDFImageCacher cache, String[] searchTerms) throws PdfException {
            super();
            loadFile = f;
            imageCache = cache;
            PdfDecoder d = new PdfDecoder(false);
            d.openPdfFile(loadFile.toString());
            metadata = d.getPdfPageData();
            for (int i = 1; i <= metadata.getPageCount(); i++) {
                imageCache.requestCache(loadFile, i, searchTerms);
            }
        }

        public void load() {
            pdfLabel = new JLabel("Loading...", JLabel.CENTER);
            pdfLabel.setBackground(Color.white);
            scroll.getViewport().setBackground(Color.white);
            pagePanel.add(pdfLabel);
            scroll.setViewportView(pdfLabel);
            contextPanel.revalidate();
            execute();
        }

        private PDFHighlightImage firstMatchImage = null;

        @Override
        protected List<PageRectangle> doInBackground() throws Exception {
            boolean firstMatchFound = false;
            List<PageRectangle> allHighlights = new Vector<PageRectangle>();
            for (int i = 1; i <= metadata.getPageCount(); i++) {
                PDFHighlightImage im = imageCache.getImage(currentFile, i);
                for (Rectangle r : im.highlights) {
                    allHighlights.add(new PageRectangle(r, i));
                }
                if (allHighlights.size() != 0 && !firstMatchFound) {
                    firstMatchFound = true;
                    firstMatchImage = im;
                }
                publish(im);
            }
            return allHighlights;
        }

        private List<PDFHighlightImage> pages = new Vector<PDFHighlightImage>();

        private Map<Rectangle, Integer> matchPages = new HashMap<Rectangle, Integer>();

        private List<Rectangle> matches = new Vector<Rectangle>();

        @Override
        protected void process(List<PDFHighlightImage> chunks) {
            for (PDFHighlightImage page : chunks) {
                pages.add(page);
                for (Rectangle r : page.highlights) {
                    matches.add(r);
                    matchPages.put(r, page.pageNum);
                }
                if (page == firstMatchImage) {
                    selectedMatch = firstMatchImage.highlights.get(0);
                    currentMatch = 0;
                    currentPage = firstMatchImage.pageNum;
                    showPage(currentPage);
                    centreRectangle(selectedMatch);
                } else {
                    updateButtons();
                }
            }
        }

        private final Color selectColor = new Color(255, 255, 0, 125);

        private final Color otherColor = new Color(0, 0, 255, 125);

        private JPanel pagePanel = new JPanel();

        public void showPage(int n) {
            if (n > 0 && n <= pages.size()) {
                currentPage = n;
                pagePanel.removeAll();
                pagePanel.setBackground(Color.DARK_GRAY);
                scroll.getViewport().setBackground(Color.DARK_GRAY);
                BufferedImage im = pages.get(n - 1).pdfImage;
                Graphics2D g = im.createGraphics();
                g.scale(zoom, zoom);
                pdfLabel = new JLabel(new ImageIcon(pages.get(n - 1).pdfImage));
                pagePanel.add(pdfLabel);
                pdfLabel.setLayout(null);
                for (Rectangle r : pages.get(n - 1).highlights) {
                    JLabel highlight = new JLabel();
                    highlight.setBackground(r == selectedMatch ? selectColor : otherColor);
                    pdfLabel.add(highlight);
                    highlight.setBounds(r);
                    highlight.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    highlight.setOpaque(true);
                    final Rectangle rRef = r;
                    final int rRefPage = n;
                    highlight.addMouseListener(new MouseListener() {

                        public void mouseClicked(MouseEvent e) {
                            selectedMatch = rRef;
                            currentMatch = matches.indexOf(rRef);
                            updateButtons();
                            Point oldView = scroll.getViewport().getViewPosition();
                            showPage(rRefPage);
                            scroll.getViewport().setViewPosition(oldView);
                        }

                        public void mouseEntered(MouseEvent e) {
                        }

                        public void mouseExited(MouseEvent e) {
                        }

                        public void mousePressed(MouseEvent e) {
                        }

                        public void mouseReleased(MouseEvent e) {
                        }
                    });
                }
                scroll.setViewportView(pagePanel);
                contextPanel.revalidate();
            }
            updateButtons();
        }

        private void updateButtons() {
            pageDownButton.setEnabled(currentPage < pages.size());
            pageUpButton.setEnabled(currentPage > 1);
            downButton.setEnabled(currentMatch < matches.size());
            upButton.setEnabled(currentMatch > 1);
        }

        public void showNextPage() {
            showPage(currentPage + 1);
        }

        public void showPreviousPage() {
            showPage(currentPage - 1);
        }

        public void showMatch(int n) {
            if (n >= 0 && n < matches.size()) {
                selectedMatch = matches.get(n);
                currentMatch = n;
                int selectedMatchPage = matchPages.get(selectedMatch);
                showPage(selectedMatchPage);
                centreRectangle(selectedMatch);
            }
            downButton.setEnabled(n < matches.size() - 1);
            upButton.setEnabled(n > 0);
        }

        public void showNextMatch() {
            showMatch(currentMatch + 1);
        }

        public void showPreviousMatch() {
            showMatch(currentMatch - 1);
        }
    }

    private PDFDocument currentDoc;

    public void showContext(File f, String searchQ) {
        try {
            search = searchQ.split(" ");
            if (currentFile != null) {
                cacher.decacheExceptFirst(currentFile);
            }
            currentFile = f;
            currentDoc = new PDFDocument(currentFile, cacher, search);
            currentDoc.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
    }
}

class PageRectangle {

    public final Rectangle r;

    public final int page;

    public PageRectangle(Rectangle r, int page) {
        this.r = r;
        this.page = page;
    }
}
