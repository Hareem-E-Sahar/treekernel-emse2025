package net.sourceforge.regexview.views;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * An imlementation of <code>LineStyleListener</code> to paint regular
 * expression text matches.
 * 
 * @author Reto Christen
 */
public class RegExLineStyler implements LineStyleListener {

    /** The default foreground color for text matches. */
    public static final Color DEFAULT_MATCH_FOREGROUND_COLOR;

    /** The default background color for text matches. */
    public static final Color DEFAULT_MATCH_BACKGROUND_COLOR;

    /** The default foreground color for text non-matches. */
    public static final Color DEFAULT_NON_MATCH_FOREGROUND_COLOR;

    /** The default background color for text non-matches. */
    public static final Color DEFAULT_NON_MATCH_BACKGROUND_COLOR;

    static {
        Display display = Display.getDefault();
        DEFAULT_MATCH_FOREGROUND_COLOR = createColor(new RGB(255, 0, 0));
        DEFAULT_MATCH_BACKGROUND_COLOR = createColor(new RGB(255, 255, 255));
        DEFAULT_NON_MATCH_FOREGROUND_COLOR = createColor(new RGB(192, 192, 192));
        DEFAULT_NON_MATCH_BACKGROUND_COLOR = createColor(new RGB(255, 255, 255));
    }

    /** The current styles. */
    private final Vector fStyles = new Vector();

    /** The current foreground color for text matches. */
    private Color matchForegroundColor = DEFAULT_MATCH_FOREGROUND_COLOR;

    /** The current background color for text matches. */
    private Color matchBackgroundColor = DEFAULT_MATCH_BACKGROUND_COLOR;

    /** The current foreground color for text matches. */
    private Color nonMatchForegroundColor = DEFAULT_NON_MATCH_FOREGROUND_COLOR;

    /** The current background color for text matches. */
    private Color nonMatchBackgroundColor = DEFAULT_NON_MATCH_BACKGROUND_COLOR;

    /**
     * Constructor.
     */
    public RegExLineStyler() {
        super();
    }

    /**
     * Constructor taking two <code>RGB</code> objects.
     * 
     * @param matchForegroundColor
     *            The <code>RGB</code> value for the match foreground color to
     *            set.
     * @param matchBackgroundColor
     *            The <code>RGB</code> value for the match background color to
     *            set.
     */
    public RegExLineStyler(final RGB matchForegroundColor, final RGB matchBackgroundColor) {
        setMatchForegroundColor(matchForegroundColor);
        setMatchBackgroundColor(matchBackgroundColor);
    }

    /**
     * Constructor taking two <code>Color</code> objects.
     * 
     * @param matchForegroundColor
     *            The <code>Color</code> value for the match foreground color
     *            to set.
     * @param matchBackgroundColor
     *            The <code>Color</code> value for the match background color
     *            to set.
     */
    public RegExLineStyler(final Color matchForegroundColor, final Color matchBackgroundColor) {
        setMatchForegroundColor(matchForegroundColor);
        setMatchBackgroundColor(matchBackgroundColor);
    }

    /**
     * Creates a <code>Color</code> object from the given <code>RGB</code>
     * object.
     * 
     * @param rgb
     *            A <code>RGB</code> object.
     * @return A <code>Color</code> object.
     */
    private static final Color createColor(final RGB rgb) {
        Display display = Display.getDefault();
        return new Color(display, rgb);
    }

    /**
     * Sets the background color for text matches.
     * 
     * @param rgb
     *            The RGB value for the color to set.
     */
    public void setMatchBackgroundColor(final RGB rgb) {
        this.matchBackgroundColor = createColor(rgb);
    }

    /**
     * Sets the background color for text matches.
     * 
     * @param matchBackgroundColor
     *            The matchBackgroundColor to set.
     */
    public void setMatchBackgroundColor(final Color matchBackgroundColor) {
        this.matchBackgroundColor = matchBackgroundColor;
    }

    /**
     * Returns the background color for text matches.
     * 
     * @return The matchBackgroundColor.
     */
    public Color getMatchBackgroundColor() {
        return matchBackgroundColor;
    }

    /**
     * Sets the foreground color for text matches.
     * 
     * @param rgb
     *            The RGB value for the color to set.
     */
    public void setMatchForegroundColor(final RGB rgb) {
        this.matchBackgroundColor = createColor(rgb);
    }

    /**
     * Sets the foreground color for text matches.
     * 
     * @param matchForegroundColor
     *            The matchForegroundColor to set.
     */
    public void setMatchForegroundColor(final Color matchForegroundColor) {
        this.matchForegroundColor = matchForegroundColor;
    }

    /**
     * Returns the foreground color for text matches.
     * 
     * @return The matchForegroundColor.
     */
    public Color getMatchForegroundColor() {
        return matchForegroundColor;
    }

    /**
     * Sets the background color for text non-matches.
     * 
     * @param rgb
     *            The RGB value for the color to set.
     */
    public void setNonMatchBackgroundColor(final RGB rgb) {
        this.nonMatchBackgroundColor = createColor(rgb);
    }

    /**
     * Sets the background color for text non-matches.
     * 
     * @param nonMatchBackgroundColor
     *            The nonMatchBackgroundColor to set.
     */
    public void setNonMatchBackgroundColor(final Color nonMatchBackgroundColor) {
        this.nonMatchBackgroundColor = nonMatchBackgroundColor;
    }

    /**
     * Returns the background color for text non-matches.
     * 
     * @return The nonMatchBackgroundColor.
     */
    public Color getNonMatchBackgroundColor() {
        return nonMatchBackgroundColor;
    }

    /**
     * Sets the foreground color for text non-matches.
     * 
     * @param rgb
     *            The RGB value for the color to set.
     */
    public void setNonMatchForegroundColor(final RGB rgb) {
        this.nonMatchForegroundColor = createColor(rgb);
    }

    /**
     * Sets the foreground color for text non-matches.
     * 
     * @param nonMatchForegroundColor
     *            The nonMatchForegroundColor to set.
     */
    public void setNonMatchForegroundColor(final Color nonMatchForegroundColor) {
        this.nonMatchForegroundColor = nonMatchForegroundColor;
    }

    /**
     * Returns the foreground color for text non-matches.
     * 
     * @return The nonMatchForegroundColor.
     */
    public Color getNonMatchForegroundColor() {
        return nonMatchForegroundColor;
    }

    /**
     * Resets the styles.
     */
    public void reset() {
        fStyles.clear();
    }

    /**
     * Performs the regular expression on the text and creates the style ranges.
     * 
     * @param exp
     *            The regular expression.
     * @param text
     *            The text to apply the regular expression on.
     */
    public void performRegEx(final String exp, final String text) {
        Pattern pattern = Pattern.compile(exp);
        Matcher matcher = pattern.matcher(text);
        fStyles.clear();
        int start = 0;
        int end = 0;
        while (matcher.find()) {
            end = matcher.start();
            fStyles.addElement(new StyleRange(start, end - start, nonMatchForegroundColor, nonMatchBackgroundColor));
            start = matcher.start();
            end = matcher.end();
            fStyles.addElement(new StyleRange(start, end - start, matchForegroundColor, matchBackgroundColor));
            start = end;
        }
        end = text.length();
        fStyles.addElement(new StyleRange(start, end - start, nonMatchForegroundColor, nonMatchBackgroundColor));
    }

    /**
     * This method is called when a line is about to be drawn in order to get
     * the line's style information.
     * 
     * @param event
     * @see org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt.custom.LineStyleEvent)
     */
    public void lineGetStyle(final LineStyleEvent event) {
        event.styles = new StyleRange[fStyles.size()];
        fStyles.copyInto(event.styles);
    }
}
