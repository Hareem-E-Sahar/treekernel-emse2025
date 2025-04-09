package net.sf.jyntax;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter.HighlightPainter;

/**
 * A rule for the {@link JyntaxHighlighter}.
 *
 * @author Clemens Renner (clemensrenner@users.sourceforge.net)
 * @version $Revision: 1.2 $
 */
public class HighlightRule {

    public static final Color DEFAULT_COLOR = new Color(146, 230, 116);

    private final HighlightPainter painter;

    private final Pattern pattern;

    private final Collection<String> tokensToPaintInPattern;

    /**
	 * Create a new rule with the given colour used for highlighting.
	 * 
	 * @throws PatternSyntaxException if the given pattern is not excepted by {@link Pattern}
	 */
    public HighlightRule(Color color, String pattern, String... tokensToPaintInPattern) throws PatternSyntaxException {
        this.painter = new DefaultHighlighter.DefaultHighlightPainter(color);
        this.pattern = Pattern.compile(pattern);
        this.tokensToPaintInPattern = Arrays.asList(tokensToPaintInPattern);
    }

    /**
	 * Create a new rule with the default colour {@link #DEFAULT_COLOR}.
	 * 
	 * @see #HighlightRule(Color, String, String...)
	 */
    public HighlightRule(String pattern, String... tokensToPaintInPattern) throws PatternSyntaxException {
        this(DEFAULT_COLOR, pattern, tokensToPaintInPattern);
    }

    /**
	 * Returns a collection of {@link HighlightElement}s, one for each
	 * occurrence of the tokens in the given document's text.
	 * 
	 * <p>
	 * The document is first matched against the pattern. All occurrences of the
	 * tokens within the pattern match are collected and returned in the form of
	 * {@link HighlightElement}s.
	 */
    public Collection<HighlightElement> findMatches(Document document) {
        try {
            Collection<HighlightElement> highlightElements = new LinkedList<HighlightElement>();
            String docText = "";
            docText = document.getText(0, document.getLength());
            Matcher patternMatcher = pattern.matcher(docText);
            while (patternMatcher.find()) {
                String patternMatchingText = patternMatcher.group();
                for (String token : tokensToPaintInPattern) {
                    Pattern tokenPattern = Pattern.compile(token);
                    Matcher tokenMatcher = tokenPattern.matcher(patternMatchingText);
                    while (tokenMatcher.find()) highlightElements.add(new HighlightElement(patternMatcher.start() + tokenMatcher.start(), patternMatcher.start() + tokenMatcher.end(), painter));
                }
            }
            return highlightElements;
        } catch (BadLocationException ex) {
            throw new IllegalArgumentException("Malformed document: " + document, ex);
        }
    }
}
