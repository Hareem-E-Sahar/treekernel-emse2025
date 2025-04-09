package de.fu_berlin.inf.gmanda.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.joda.time.DateTime;
import de.fu_berlin.inf.gmanda.gui.manager.CommonService;
import de.fu_berlin.inf.gmanda.gui.preferences.ScrollOnShowProperty;
import de.fu_berlin.inf.gmanda.imports.GmaneFacade;
import de.fu_berlin.inf.gmanda.proxies.SearchStringProxy;
import de.fu_berlin.inf.gmanda.proxies.SelectionProxy;
import de.fu_berlin.inf.gmanda.qda.Code;
import de.fu_berlin.inf.gmanda.qda.CodedStringFactory;
import de.fu_berlin.inf.gmanda.qda.PrimaryDocument;
import de.fu_berlin.inf.gmanda.qda.Project;
import de.fu_berlin.inf.gmanda.util.StateChangeListener;
import de.fu_berlin.inf.gmanda.util.StringJoiner;
import de.fu_berlin.inf.gmanda.util.VariableProxyListener;
import de.fu_berlin.inf.gmanda.util.CStringUtils.StringConverter;
import de.fu_berlin.inf.gmanda.util.gui.HighlightSupport;

public class TextView extends JScrollPane {

    JTextPane pane = new JTextPane();

    HighlightSupport highlighter = new HighlightSupport(pane);

    protected Object toShow;

    protected String highlightQuery;

    Desktop desktop;

    boolean initialized = false;

    public void browse(final URL url) {
        commonService.run(new Runnable() {

            public void run() {
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
        }, "Error opening URL");
    }

    ScrollOnShowProperty scrollOnShow;

    CommonService commonService;

    GmaneFacade gmane;

    public TextView(ScrollOnShowProperty scrollOnShow, SelectionProxy selectionProxy, SearchStringProxy search, GmaneFacade gmane, CommonService service) {
        super();
        this.scrollOnShow = scrollOnShow;
        this.gmane = gmane;
        this.commonService = service;
        setBorder(BorderFactory.createEmptyBorder());
        pane.setEditable(false);
        pane.setPreferredSize(new Dimension(400, 200));
        pane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        pane.setFont(new Font("Courier New", 0, 12));
        pane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent arg0) {
                if (arg0.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    browse(arg0.getURL());
                }
            }
        });
        setViewportView(pane);
        invalidate();
        final StateChangeListener<PrimaryDocument> pdListener = new StateChangeListener<PrimaryDocument>() {

            public void stateChangedNotification(PrimaryDocument t) {
                update();
            }
        };
        selectionProxy.add(new VariableProxyListener<Object>() {

            public void setVariable(Object show) {
                if (toShow != null && toShow instanceof PrimaryDocument) ((PrimaryDocument) toShow).getTextChangeNotifier().remove(pdListener);
                toShow = show;
                if (toShow != null && toShow instanceof PrimaryDocument) ((PrimaryDocument) toShow).getTextChangeNotifier().add(pdListener);
                update();
            }
        });
        this.toShow = selectionProxy.getVariable();
        search.addAndNotify(new VariableProxyListener<String>() {

            public void setVariable(String highlightQuery) {
                TextView.this.highlightQuery = highlightQuery;
                updateHighlight();
            }
        });
    }

    public static String toHTMLBody(String html) {
        html = html.replace(">", "&gt;");
        html = html.replace("<", "&lt;");
        html = toBoldAndItalic(html);
        html = html.replaceAll("\n", "<br>");
        html = html.replaceAll("\r", "");
        html = html.replaceAll("\f", "");
        html = html.replaceAll("<br>([ \t]*&gt;[ \t]*&gt;[ \t]*&gt;[ \t]*&gt;[ \t]*&gt;[ \t]*&gt;.*?)(?=<br>)", "<br><span style=\"color:#880088;\">$1</span>");
        html = html.replaceAll("<br>([ \t]*&gt;[ \t]*&gt;[ \t]*&gt;[ \t]*&gt;[ \t]*&gt;.*?)(?=<br>)", "<br><span style=\"color:#ff7700;\">$1</span>");
        html = html.replaceAll("<br>([ \t]*&gt;[ \t]*&gt;[ \t]*&gt;[ \t]*&gt;.*?)(?=<br>)", "<br><span style=\"color:#888800;\">$1</span>");
        html = html.replaceAll("<br>([ \t]*&gt;[ \t]*&gt;[ \t]*&gt;.*?)(?=<br>)", "<br><span style=\"color:#00bb00;\">$1</span>");
        html = html.replaceAll("<br>([ \t]*&gt;[ \t]*&gt;.*?)(?=<br>)", "<br><span style=\"color:#000090;\">$1</span>");
        html = html.replaceAll("<br>([ \t]*&gt;.*?)(?=<br>)", "<br><span style=\"color:#ee0000;\">$1</span>");
        html = toHyperLink(html);
        html = html.replaceAll("([^a-zA-Z0-9._+-/?=~!@#$%&:])(www[.][a-zA-Z0-9._+-/?=~!@#$%&:]+[^]. >\t])([] ,<\t])", "$1<a href=\"http://$2\">$2</a>$3");
        html = html.replaceAll("([ :,&lt;>\t])([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+[.][a-zA-Z]{2,4})([] ,&gt;<\t])", "$1<a href=\"mailto:$2\">$2</a>$3");
        html = html.replaceAll("<br>\\s*<br>(\\s*<br>)+", "<br><br>");
        html = html.replaceAll("  ", "&nbsp;&nbsp;");
        html = html.replace("<br>", "\n<br>");
        html = html.replaceFirst("^(\\s*<br>)*", "    ");
        html = html.replaceFirst("(<br>\\s*)*$", "");
        return html;
    }

    public static String embedIntoHTMLBody(String html) {
        return "<html><body style=\"font-family: monospace; font-size: 12pt;\">\n" + html + "<br><br></body></html>";
    }

    public static String toHTML(String html) {
        return embedIntoHTMLBody(toHTMLBody(html));
    }

    public static String toHyperLink(String html) {
        html = html.replaceAll("(https{0,1}://[a-zA-Z0-9._+-/?=~!@#$%&:]+[^]. )<\\t])", "<a href=\"$1\">$1</a>");
        return html;
    }

    public static String toBoldAndItalic(String html) {
        html = html.replaceAll("([\\s>*])_([^\\s_]+( [^\\s_]+?){0,4}?)_(?=[\\s<*])", "$1_<u>$2</u>_");
        html = html.replaceAll("([\\s>_])\\*([^\\s*]+( [^\\s*]+?){0,4}?)\\*(?=[\\s<_])", "$1*<b>$2</b>*");
        return html;
    }

    public void updateHighlight() {
        highlighter.removeHighlights();
        if (toShow == null || !(toShow instanceof PrimaryDocument || toShow instanceof Project)) {
            return;
        } else {
            final PrimaryDocument pd = (PrimaryDocument) toShow;
            List<String> quoteList = new LinkedList<String>();
            String code = pd.getCodeAsString();
            if (code != null) {
                for (Code c : CodedStringFactory.parse(pd.getCodeAsString()).getAllDeep("quote")) {
                    quoteList.add(StringUtils.strip(c.getValue(), ",. \r\n\f\t'\""));
                }
            }
            StrTokenizer st = new StrTokenizer(highlightQuery, StrMatcher.splitMatcher(), StrMatcher.quoteMatcher());
            quoteList.addAll(Arrays.asList(st.getTokenArray()));
            StringJoiner sb = new StringJoiner("|");
            for (String s : quoteList) {
                if (s == null) continue;
                s = s.trim();
                if (s.length() == 0) continue;
                s = s.replaceAll("\\p{Punct}", " ");
                String searchPattern = de.fu_berlin.inf.gmanda.util.CStringUtils.join(Arrays.asList(s.split("\\s+")), "\\s+", new StringConverter<String>() {

                    public String toString(String t) {
                        return Pattern.quote(t);
                    }
                });
                sb.append("(?i)(" + searchPattern + ")");
            }
            String searchPattern = sb.toString();
            if (searchPattern.length() > 0) {
                highlighter.findAllMatches(Pattern.compile(searchPattern));
            }
        }
    }

    public void update() {
        if (toShow == null || !(toShow instanceof PrimaryDocument || toShow instanceof Project)) {
            setEnabled(false);
            pane.setText("");
        } else {
            setEnabled(true);
            if (toShow instanceof PrimaryDocument) {
                final PrimaryDocument pd = (PrimaryDocument) toShow;
                pane.setContentType("text/html");
                pane.setFont(new Font("Courier", 0, 10));
                pd.getMetaData().put("lastseen", new DateTime().toString());
                pane.setText("<html><body>loading...</body></html>");
                commonService.run(new Runnable() {

                    public void run() {
                        String text = pd.getText(gmane);
                        String html = text;
                        if (!"text/html".equals(pd.getMetaData("Content-type"))) {
                            html = toHTML(text);
                        } else {
                            html = embedIntoHTMLBody(text);
                        }
                        pane.setText(html);
                        updateHighlight();
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                invalidate();
                                resetScrollPosition();
                            }
                        });
                    }
                }, "Error Fetching Text from Gmane");
            } else if (toShow instanceof Project) {
                pane.setText("Number of primary documents: " + ((Project) toShow).getPrimaryDocuments());
            }
        }
    }

    public void resetScrollPosition() {
        if (scrollOnShow.getValue()) {
            Rectangle rect = null;
            try {
                Document d = pane.getDocument();
                String content = d.getText(0, d.getLength()).toLowerCase();
                int index = -1;
                if (highlightQuery != null) {
                    index = content.indexOf(highlightQuery);
                }
                if (index != -1) {
                    rect = pane.modelToView(index);
                }
            } catch (BadLocationException e) {
            }
            if (rect != null) {
                rect.y = Math.max(0, rect.y - 50);
                rect.height = rect.height + 50;
                pane.scrollRectToVisible(rect);
                return;
            }
        }
        getVerticalScrollBar().setValue(0);
    }
}
