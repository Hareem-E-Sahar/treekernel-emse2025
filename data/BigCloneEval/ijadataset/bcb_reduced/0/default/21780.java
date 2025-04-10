import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import org.w3c.dom.Document;
import org.xhtmlrenderer.event.DefaultDocumentListener;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Opens a frame and displays, for a selected font, the glyphs for a range of Unicode code points. Can be used to
 * identify which glyphs are supported by a font. Can export to PDF. Requires core-renderer and iText on classpath.
 *
 * @author Patrick Wright
 */
public class FontGlyphTableRender {

    private static final int TO_SWING = 1;

    private static final int TO_PDF = 2;

    private static final String OUTPUT_ENTITIES = "entities";

    private static final String OUTPUT_CODEPOINTS = "codepoints";

    private static final int ENT_PER_PAGE = 399;

    private int curFrom;

    private String outputType = OUTPUT_CODEPOINTS;

    private Font currentFont;

    private JFrame frame;

    private XHTMLPanel xpanel;

    private JTextField fontPathTF;

    private JTextField familyNameField;

    private JButton prevBtn;

    private JButton nextBtn;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    new FontGlyphTableRender().run();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void run() throws Exception {
        frame = new JFrame("Flying Saucer: Show Font Glyphs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel optionsPanel = new JPanel(new BorderLayout());
        fontPathTF = new JTextField();
        fontPathTF.setColumns(40);
        familyNameField = new JTextField();
        familyNameField.setEnabled(true);
        familyNameField.setEditable(false);
        familyNameField.setColumns(40);
        JPanel top1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top1.add(new JLabel("Enter font path: "));
        top1.add(fontPathTF);
        JButton chooseFontFileBtn = new JButton("...");
        chooseFontFileBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String filename = File.separator + "tmp";
                String famPath = fontPathTF.getText();
                if (currentFont != null && famPath.length() > 0) {
                    filename = new File(famPath).getParent();
                }
                JFileChooser fc = new JFileChooser(new File(filename));
                fc.showOpenDialog(frame);
                File selFile = fc.getSelectedFile();
                Font font = null;
                String msg = "";
                try {
                    font = loadFont(selFile.getPath());
                } catch (IOException e1) {
                    msg = e1.getMessage();
                }
                if (font == null) {
                    JOptionPane.showMessageDialog(frame, "Can't load file--is it a valid Font file? " + msg);
                } else {
                    fontPathTF.setText(selFile.getPath());
                    familyNameField.setText(font.getFamily());
                }
            }
        });
        top1.add(chooseFontFileBtn);
        ActionListener outputSelection = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                outputType = e.getActionCommand();
                enableButtons();
                if (currentFont != null) {
                    deferredChangePage(curFrom);
                }
            }
        };
        JRadioButton jrbCodePoint = new JRadioButton("Codepoints");
        jrbCodePoint.setActionCommand(OUTPUT_CODEPOINTS);
        jrbCodePoint.addActionListener(outputSelection);
        jrbCodePoint.setSelected(true);
        JRadioButton jrbEntities = new JRadioButton("Entities");
        jrbEntities.setActionCommand(OUTPUT_ENTITIES);
        jrbEntities.addActionListener(outputSelection);
        ButtonGroup bg = new ButtonGroup();
        bg.add(jrbCodePoint);
        bg.add(jrbEntities);
        top1.add(jrbCodePoint);
        top1.add(jrbEntities);
        JPanel top2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top2.add(new JLabel("Family: "));
        top2.add(familyNameField);
        JPanel top = new JPanel(new BorderLayout());
        top.add(top1, BorderLayout.NORTH);
        top.add(top2, BorderLayout.CENTER);
        JPanel mid = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prevBtn = new JButton("Prev");
        nextBtn = new JButton("Next");
        JButton pdfBtn = new JButton("PDF");
        JButton renderBtn = new JButton("Render");
        prevBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom - ENT_PER_PAGE);
            }
        });
        nextBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom + ENT_PER_PAGE);
            }
        });
        renderBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom);
            }
        });
        pdfBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                resolveCurrentFont();
                if (currentFont == null) {
                    JOptionPane.showMessageDialog(frame, "Need a valid font file path");
                    fontPathTF.requestFocus();
                    return;
                }
                deferredLoadAndRender(curFrom, TO_PDF);
            }
        });
        mid.add(prevBtn);
        mid.add(nextBtn);
        mid.add(renderBtn);
        mid.add(pdfBtn);
        optionsPanel.add(top, BorderLayout.NORTH);
        optionsPanel.add(mid, BorderLayout.CENTER);
        fontPathTF.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom);
            }
        });
        xpanel = new XHTMLPanel();
        xpanel.addDocumentListener(new DefaultDocumentListener() {

            public void documentLoaded() {
                frame.setCursor(Cursor.getDefaultCursor());
            }
        });
        resetMouseListeners();
        FSScrollPane scroll = new FSScrollPane(xpanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel cont = new JPanel(new BorderLayout());
        cont.add(optionsPanel, BorderLayout.NORTH);
        cont.add(scroll, BorderLayout.CENTER);
        frame.getContentPane().add(cont);
        frame.pack();
        frame.setSize(1024, 730);
        enableButtons();
        frame.setVisible(true);
    }

    private void resetMouseListeners() {
        List l = xpanel.getMouseTrackingListeners();
        for (Iterator i = l.iterator(); i.hasNext(); ) {
            FSMouseListener listener = (FSMouseListener) i.next();
            if (listener instanceof LinkListener) {
                xpanel.removeMouseTrackingListener(listener);
            }
        }
    }

    private void enableButtons() {
        prevBtn.setEnabled(outputType.equals(OUTPUT_CODEPOINTS) && curFrom > 0);
        nextBtn.setEnabled(outputType.equals(OUTPUT_CODEPOINTS) && Math.pow(2, 16) - curFrom != 0);
    }

    private Font loadFont(String fontPath) throws IOException {
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath).toURL().openStream());
            return font.deriveFont(Font.PLAIN, 12);
        } catch (FontFormatException e) {
            System.err.println(fontPath + " INVALID FONT FORMAT " + e.getMessage());
            return null;
        }
    }

    private void deferredChangePage(final int startAt) {
        deferredLoadAndRender(startAt, TO_SWING);
    }

    private void deferredLoadAndRender(final int startAt, final int renderTo) {
        resolveCurrentFont();
        if (currentFont == null) {
            JOptionPane.showMessageDialog(frame, "Can't load font--check font file path.");
            fontPathTF.requestFocus();
            return;
        }
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(new Runnable() {

            public void run() {
                final Document doc = loadDocument(startAt);
                if (renderTo == TO_SWING) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            try {
                                curFrom = startAt;
                                xpanel.setDocument(doc, null, new XhtmlNamespaceHandler());
                                xpanel.getSharedContext().getCss().getCascadedStyle(null, false);
                            } catch (Throwable e) {
                                JOptionPane.showMessageDialog(frame, "Can't load document (table of glyphs). Err: " + e.getMessage());
                            }
                            enableButtons();
                        }
                    });
                } else {
                    frame.setCursor(Cursor.getDefaultCursor());
                    renderPDF(doc);
                }
            }
        }).start();
    }

    private Document loadDocument(int startAt) {
        curFrom = startAt;
        String page;
        if (outputType.equals(OUTPUT_CODEPOINTS)) {
            Table table = buildGlyphTable(startAt, startAt + ENT_PER_PAGE);
            page = new Page().toHtml(table.toHtml(getFontFamily(), curFrom), getFontFamily());
        } else {
            page = parseEnt(new Page().toHtml("", getFontFamily()));
        }
        InputSource is = new InputSource(new BufferedReader(new StringReader(page)));
        return XMLResource.load(is).getDocument();
    }

    private Table buildGlyphTable(int from, int to) {
        Table table = new Table(16);
        for (int j = from; j <= to; j++) {
            if (isLegalInXml(j)) {
                if (currentFont.canDisplay((char) j)) {
                    table.addColumn("&amp;#" + j + ";");
                    table.addGlyph("&#" + j + ";");
                } else {
                    table.addColumn("&amp;#" + j + ";");
                    table.addGlyph("&nbsp;");
                }
            } else {
                table.addColumn("&amp;#" + j + ";");
                table.addGlyph("!");
            }
        }
        return table;
    }

    private void renderPDF(Document doc) {
        String msgToUser = "";
        File f;
        try {
            f = File.createTempFile("flying-saucer-glyph-test", ".pdf");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Can't create temp file for PDF output, err: " + e.getMessage());
            return;
        }
        final ITextRenderer renderer = new ITextRenderer();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            renderer.setDocument(doc, null, new XhtmlNamespaceHandler());
            ITextFontResolver resolver = renderer.getFontResolver();
            resolver.addFont(fontPathTF.getText(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.layout();
            renderer.createPDF(bos);
            msgToUser = "Rendered PDF: " + f.getCanonicalPath();
        } catch (FileNotFoundException e) {
            msgToUser = "Can't create PDF, err: " + e.getMessage();
        } catch (DocumentException e) {
            msgToUser = "Can't create PDF, err: " + e.getMessage();
        } catch (IOException e) {
            msgToUser = "Can't create PDF, err: " + e.getMessage();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
        if (msgToUser.length() != 0) {
            final String finalMsg = msgToUser;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(frame, finalMsg);
                }
            });
        }
    }

    private void resolveCurrentFont() {
        String path = fontPathTF.getText();
        if (path.length() == 0) {
            currentFont = null;
            return;
        }
        try {
            currentFont = loadFont(path);
            if (currentFont != null) {
                familyNameField.setText(currentFont.getFamily());
            }
        } catch (IOException e) {
            e.printStackTrace();
            currentFont = null;
        }
    }

    private String parseEnt(String html) {
        try {
            final Table table = new Table(15);
            XMLReader parser = XMLReaderFactory.createXMLReader();
            InputSource is = new InputSource(new BufferedReader(new StringReader(html)));
            try {
                parser.setFeature("http://xml.org/sax/features/validation", true);
            } catch (SAXException e) {
                System.err.println("Cannot activate validation.");
            }
            parser.setEntityResolver(FSEntityResolver.instance());
            DefaultHandler2 dh2 = new DefaultHandler2() {

                boolean isEnt;

                public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
                    super.externalEntityDecl(name, publicId, systemId);
                    isEnt = systemId.endsWith(".ent");
                }

                public void internalEntityDecl(String name, String value) throws SAXException {
                    super.internalEntityDecl(name, value);
                    if (isEnt) {
                        if (name.startsWith("%")) return;
                        int codePoint = (int) value.charAt(0);
                        table.addColumn("&amp;" + name + ";");
                        table.addColumn("&amp;#" + codePoint + ";");
                        table.addGlyph("&#" + codePoint + ";");
                    }
                }
            };
            parser.setProperty("http://xml.org/sax/properties/declaration-handler", dh2);
            parser.parse(is);
            return new Page().toHtml(table.toHtml(getFontFamily(), 0), getFontFamily());
        } catch (SAXException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean isLegalInXml(int uccp) {
        return ((uccp == 0x9) || (uccp == 0xA) || (uccp == 0xD) || ((uccp >= 0x20) && (uccp <= 0xD7FF)) || ((uccp >= 0xE000) && (uccp <= 0xFFFD)) || ((uccp >= 0x10000) && (uccp <= 0x10FFFF))) && uccp != 0x0DDD;
    }

    private String getFontFamily() {
        return currentFont.getFamily();
    }

    private static class Page {

        public String toHtml(String bodyContent, String fontFamily) {
            StringBuffer sb = new StringBuffer();
            sb.append(getHeadDecl(getStyleDecl(fontFamily)));
            sb.append("<body>\n");
            sb.append(bodyContent);
            sb.append("</body>\n");
            sb.append("</html>\n");
            return sb.toString();
        }

        private String getHeadDecl(String style) {
            return "<?xml version='1.0' encoding='utf-8'?>\n" + "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>\n" + "<html xmlns='http://www.w3.org/1999/xhtml'>\n" + "<head>" + "<title>Full Entity Chart</title>\n" + style + "</head>\n";
        }

        private String getStyleDecl(String fontFamily) {
            String css = "* {font-size: 8pt; font-family: \"" + fontFamily + "\" } " + "table {table-layout: fixed; width: 100%; border-collapse: collapse; border: 1px solid black;} " + "col {} " + ".glyph {width: 1.35em; border-right-width: 2px;} " + "td {border: 1px solid black; }" + "td .glyph {}";
            return "<style type=\"text/css\">\n" + css + "\n</style>\n";
        }
    }

    private static class Table {

        private int colCnt;

        private List cols = new ArrayList();

        private List headerLines = new ArrayList();

        public Table(int colCnt) {
            this.colCnt = colCnt;
        }

        public String toHtml(String fontFamily, int curFrom) {
            StringBuffer sb = new StringBuffer();
            for (Iterator it = headerLines.iterator(); it.hasNext(); ) {
                String line = (String) it.next();
                sb.append("<p>").append(line).append("</p>\n");
            }
            sb.append("<p>Table of Unicode Characters</p>\n");
            sb.append("<p>Using font: ").append(fontFamily).append(", Unicode code points starting with ").append(curFrom).append("</p>\n");
            sb.append("<p>Empty cell means no glyph available; ! means codepoint not allowed in XML, per spec.</p>\n");
            sb.append("<table>\n");
            for (int i = 0; i < colCnt; i++) {
                sb.append("<col class=\"").append(((Col) cols.get(i)).cssClass).append("\"/>\n");
            }
            int cnt = 0;
            sb.append("<tr>\n");
            for (Iterator it = cols.iterator(); it.hasNext(); ) {
                Col col = (Col) it.next();
                sb.append("<td class=\"").append(((Col) cols.get(cnt)).cssClass).append("\">").append(col.content).append("</td>");
                if (++cnt % colCnt == 0 && it.hasNext()) {
                    sb.append("\n</tr>\n");
                    sb.append("<tr>\n");
                }
            }
            sb.append("\n</tr>\n");
            sb.append("</table>\n");
            return sb.toString();
        }

        public void addColumn(String content) {
            cols.add(new Col("", content));
        }

        public void addGlyph(String content) {
            cols.add(new Col("glyph", content));
        }

        public void addHeaderLine(String text) {
            headerLines.add(text);
        }

        static class Col {

            private final String cssClass;

            private final String content;

            public Col(String cssClass, String content) {
                this.cssClass = cssClass;
                this.content = content;
            }
        }
    }
}
