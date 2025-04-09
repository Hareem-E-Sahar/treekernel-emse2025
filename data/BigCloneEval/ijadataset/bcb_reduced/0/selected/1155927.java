package org.xmlhammer.gui.output;

import java.awt.Color;
import java.awt.LayoutManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.Highlight;
import org.apache.log4j.Logger;

/**
 * A search-able Panel.
 * 
 * @version $Revision$, $Date$
 * @author Edwin Dankert <edankert@gmail.com>
 */
public abstract class SearchablePanel extends JPanel {

    public SearchablePanel() {
        super();
    }

    public SearchablePanel(boolean arg0) {
        super(arg0);
    }

    public SearchablePanel(LayoutManager arg0, boolean arg1) {
        super(arg0, arg1);
    }

    public SearchablePanel(LayoutManager arg0) {
        super(arg0);
    }

    public abstract boolean search(String search, boolean matchCase, boolean forward);

    protected static boolean search(JTextComponent text, String search, boolean matchCase, boolean forward) {
        Pattern pattern = null;
        String regularSearch = "\\Q" + prepareNonRegularExpression(search) + "\\E";
        int start = -1;
        int end = -1;
        if (!matchCase) {
            pattern = Pattern.compile(regularSearch, Pattern.CASE_INSENSITIVE);
        } else {
            pattern = Pattern.compile(regularSearch);
        }
        try {
            int caret = text.getCaretPosition();
            if (getHiglight(text) != null) {
                caret = Math.max(caret, Math.max(getHighlightStart(text), getHighlightEnd(text)));
            }
            Matcher matcher = pattern.matcher(text.getText(0, text.getDocument().getLength()));
            if (forward) {
                boolean match = matcher.find(caret);
                if (!match) {
                    match = matcher.find(0);
                }
                if (match) {
                    start = matcher.start();
                    end = matcher.end();
                }
            } else {
                caret = text.getCaretPosition();
                if (getHiglight(text) != null) {
                    caret = Math.min(caret, Math.min(getHighlightStart(text), getHighlightEnd(text)));
                }
                while (matcher.find()) {
                    if (matcher.start() < caret) {
                        start = matcher.start();
                        end = matcher.end();
                    } else {
                        break;
                    }
                }
                if (start == -1) {
                    boolean match = matcher.find(caret);
                    while (match) {
                        start = matcher.start();
                        end = matcher.end();
                        match = matcher.find();
                    }
                }
            }
            if (end != -1) {
                matcher.find(start);
                select(text, start, end);
            }
            text.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return end != -1;
    }

    private static String prepareNonRegularExpression(String regexp) {
        StringBuffer result = new StringBuffer(regexp);
        if (result.length() > 0) {
            int index = result.indexOf("\\E");
            while (index != -1) {
                result.replace(index, index + 2, "\\E\\\\E\\Q");
                index = result.indexOf("\\E", index + 7);
            }
            if (result.charAt(result.length() - 1) == '\\') {
                result.append("E\\\\\\Q");
            }
        }
        return result.toString();
    }

    private static void select(JTextComponent text, int start, int end) {
        try {
            if (start > 0) {
                text.getHighlighter().removeAllHighlights();
                text.getHighlighter().addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(brighter(text.getSelectionColor())));
            }
        } catch (BadLocationException e) {
            Logger.getLogger(SearchablePanel.class).debug(e);
        }
        text.setCaretPosition(Math.max(0, end));
    }

    private static int getHighlightStart(JTextComponent text) {
        Highlight highlight = getHiglight(text);
        if (highlight != null) {
            return highlight.getStartOffset();
        }
        return -1;
    }

    private static int getHighlightEnd(JTextComponent text) {
        Highlight highlight = getHiglight(text);
        if (highlight != null) {
            return highlight.getEndOffset();
        }
        return -1;
    }

    private static Highlight getHiglight(JTextComponent text) {
        Highlight[] highlights = text.getHighlighter().getHighlights();
        if (highlights.length > 0) {
            return highlights[0];
        }
        return null;
    }

    private static Color brighter(Color color) {
        return new Color(brighten(color.getRed()), brighten(color.getGreen()), brighten(color.getBlue()));
    }

    private static int brighten(int color) {
        int newColor = color * (100 + 10) / 100;
        if (newColor >= 0xFF) {
            newColor = 0xFF;
        } else if (newColor == color) {
            newColor++;
        }
        return newColor;
    }
}
