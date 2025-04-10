package ti.plato.ui.views.console;

import java.util.HashMap;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.part.IPageBookViewPage;
import ti.plato.ui.views.internal.console.ConsoleDocument;
import ti.plato.ui.views.internal.console.ConsoleHyperlinkPosition;
import ti.plato.ui.views.internal.console.ConsolePatternMatcher;

/**
 * An abstract text console that supports regular expression matching and
 * hyperlinks.
 * <p>
 * Pattern match listeners can be registered with a console programmatically
 * or via the <code>org.eclipse.ui.console.consolePatternMatchListeners</code>
 * extension point.
 * </p>
 * <p>
 * Clients may subclass this class. Subclasses must provide a document partitioner.
 * </p>
 * @since 3.1
 */
public abstract class TextConsole extends AbstractConsole {

    /**
     * The current width of the console. Used for fixed width consoles.
     * A value of <=0 means does not have a fixed width.
     */
    private int fConsoleWidth;

    /**
     * The current tab width
     */
    private int fTabWidth;

    /** 
	 * The font used by this console
	 */
    private Font fFont;

    /**
     * The Console's regular expression pattern matcher
     */
    private ConsolePatternMatcher fPatternMatcher;

    /**
     * The Console's document
     */
    private ConsoleDocument fDocument;

    /**
    * indication that the console's partitioner is not expecting more input
    */
    private boolean fPartitionerFinished = false;

    /**
     * Indication that the console's pattern matcher has finished.
     * (all matches have been found and all listeners notified)
     */
    private boolean fMatcherFinished = false;

    /**
     * indication that the console output complete property has been fired
     */
    private boolean fCompleteFired = false;

    /**
     * Map of client defined attributes
     */
    private HashMap fAttributes = new HashMap();

    private IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();

    protected void dispose() {
        super.dispose();
        fFont = null;
        synchronized (fAttributes) {
            fAttributes.clear();
        }
    }

    /**
     * Constructs a console with the given name, image descriptor, and lifecycle
     * 
     * @param name name to display for this console
     * @param consoleType console type identifier or <code>null</code>
     * @param imageDescriptor image to display for this console or <code>null</code>
     * @param autoLifecycle whether lifecycle methods should be called automatically
     *  when this console is added/removed from the console manager
     */
    public TextConsole(String name, String consoleType, ImageDescriptor imageDescriptor, boolean autoLifecycle) {
        super(name, consoleType, imageDescriptor, autoLifecycle);
        fDocument = new ConsoleDocument();
        fDocument.addPositionCategory(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
        fPatternMatcher = new ConsolePatternMatcher(this);
        fDocument.addDocumentListener(fPatternMatcher);
    }

    public IPageBookViewPage createPage(IConsoleView view) {
        return new TextConsolePage(this, view);
    }

    /**
	 * Returns this console's document.
     * <p>
     * Note that a console may or may not support direct manipulation of its document.
     * For example, an I/O console document and its partitions are produced from the
     * streams connected to it, and clients are not intended to modify the document's
     * contents.
     * </p>
	 * 
	 * @return this console's document
	 */
    public IDocument getDocument() {
        return fDocument;
    }

    /**
     * Returns the current width of this console. A value of zero of less 
     * indicates this console has no fixed width.
     * 
     * @return the current width of this console
     */
    public int getConsoleWidth() {
        return fConsoleWidth;
    }

    /**
     * Sets the width of this console in characters. Any value greater than zero
     * will cause this console to have a fixed width.
     * 
     * @param width the width to make this console. Values of 0 or less imply
     * the console does not have any fixed width.
     */
    public void setConsoleWidth(int width) {
        if (fConsoleWidth != width) {
            int old = fConsoleWidth;
            fConsoleWidth = width;
            firePropertyChange(this, IConsoleConstants.P_CONSOLE_WIDTH, new Integer(old), new Integer(fConsoleWidth));
        }
    }

    /**
	 * Sets the tab width used in this console.
	 * 
	 * @param newTabWidth the tab width 
	 */
    public void setTabWidth(final int newTabWidth) {
        if (fTabWidth != newTabWidth) {
            final int oldTabWidth = fTabWidth;
            fTabWidth = newTabWidth;
            ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {

                public void run() {
                    firePropertyChange(TextConsole.this, IConsoleConstants.P_TAB_SIZE, new Integer(oldTabWidth), new Integer(fTabWidth));
                }
            });
        }
    }

    /**
	 * Returns the tab width used in this console.
	 * 
	 * @return tab width used in this console
	 */
    public int getTabWidth() {
        return fTabWidth;
    }

    /**
	 * Returns the font used by this console. Must be called in the UI thread.
	 * 
	 * @return font used by this console
	 */
    public Font getFont() {
        if (fFont == null) {
            fFont = getDefaultFont();
        }
        return fFont;
    }

    /**
     * Returns the default text font.
     * 
     * @return the default text font
     */
    private Font getDefaultFont() {
        return JFaceResources.getFont(JFaceResources.TEXT_FONT);
    }

    /**
	 * Sets the font used by this console. Specify <code>null</code> to use
	 * the default text font.
	 * 
	 * @param newFont font, or <code>null</code> to indicate the default font
	 */
    public void setFont(Font newFont) {
        getFont();
        if (newFont == null) {
            newFont = getDefaultFont();
        }
        if (!fFont.equals(newFont)) {
            Font old = fFont;
            fFont = newFont;
            firePropertyChange(this, IConsoleConstants.P_FONT, old, fFont);
        }
    }

    /**
     * Clears the console.
     * <p>
     * Since a console may or may not support direct manipulation
     * of its document's contents, this method should be called to clear a text console's
     * document. The default implementation sets this console's document content
     * to the empty string directly. Subclasses should override as required.
     * </p>
     */
    public void clearConsole() {
        IDocument document = getDocument();
        if (document != null) {
            document.set("");
        }
    }

    /**
     * Returns the console's document partitioner.
     * @return The console's document partitioner
     */
    protected abstract IConsoleDocumentPartitioner getPartitioner();

    /**
     * Returns all hyperlinks in this console.
     * 
     * @return all hyperlinks in this console
     */
    public IHyperlink[] getHyperlinks() {
        try {
            Position[] positions = getDocument().getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
            IHyperlink[] hyperlinks = new IHyperlink[positions.length];
            for (int i = 0; i < positions.length; i++) {
                ConsoleHyperlinkPosition position = (ConsoleHyperlinkPosition) positions[i];
                hyperlinks[i] = position.getHyperLink();
            }
            return hyperlinks;
        } catch (BadPositionCategoryException e) {
            return new IHyperlink[0];
        }
    }

    /**
     * Returns the hyperlink at the given offset of <code>null</code> if none.
     * 
     * @param offset offset for which a hyperlink is requested
     * @return the hyperlink at the given offset of <code>null</code> if none
     */
    public IHyperlink getHyperlink(int offset) {
        try {
            IDocument document = getDocument();
            if (document != null) {
                Position[] positions = document.getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
                Position position = findPosition(offset, positions);
                if (position instanceof ConsoleHyperlinkPosition) {
                    return ((ConsoleHyperlinkPosition) position).getHyperLink();
                }
            }
        } catch (BadPositionCategoryException e) {
        }
        return null;
    }

    /**
	 * Binary search for the position at a given offset.
	 *
	 * @param offset the offset whose position should be found
	 * @return the position containing the offset, or <code>null</code>
	 */
    private Position findPosition(int offset, Position[] positions) {
        if (positions.length == 0) return null;
        int left = 0;
        int right = positions.length - 1;
        int mid = 0;
        Position position = null;
        while (left < right) {
            mid = (left + right) / 2;
            position = positions[mid];
            if (offset < position.getOffset()) {
                if (left == mid) right = left; else right = mid - 1;
            } else if (offset > (position.getOffset() + position.getLength() - 1)) {
                if (right == mid) left = right; else left = mid + 1;
            } else {
                left = right = mid;
            }
        }
        position = positions[left];
        if (offset >= position.getOffset() && (offset < (position.getOffset() + position.getLength()))) {
            return position;
        }
        return null;
    }

    /**
     * Adds the given pattern match listener to this console. The listener will
     * be connected and receive match notifications. Has no effect if an identical
     * listener has already been added.
     * 
     * @param listener the listener to add
     */
    public void addPatternMatchListener(IPatternMatchListener listener) {
        fPatternMatcher.addPatternMatchListener(listener);
    }

    /**
     * Removes the given pattern match listener from this console. The listener will be
     * disconnected and will no longer receive match notifications. Has no effect
     * if the listener was not previously added.
     * 
     * @param listener the pattern match listener to remove
     */
    public void removePatternMatchListener(IPatternMatchListener listener) {
        fPatternMatcher.removePatternMatchListener(listener);
    }

    /**
     * Job scheduling rule that prevent the job from running if the console's PatternMatcher
     * is active.
     */
    private class MatcherSchedulingRule implements ISchedulingRule {

        public boolean contains(ISchedulingRule rule) {
            return rule == this;
        }

        public boolean isConflicting(ISchedulingRule rule) {
            if (contains(rule)) {
                return true;
            }
            if (rule != this && rule instanceof MatcherSchedulingRule) {
                return (((MatcherSchedulingRule) rule).getConsole() == TextConsole.this);
            }
            return false;
        }

        public TextConsole getConsole() {
            return TextConsole.this;
        }
    }

    /**
     * Returns a scheduling rule which can be used to prevent jobs from running
     * while this console's pattern matcher is active.
     * 
     * @return a scheduling rule which can be used to prevent jobs from running
     * while this console's pattern matcher is active
     */
    public ISchedulingRule getSchedulingRule() {
        return new MatcherSchedulingRule();
    }

    /**
     * This console's partitioner should call this method when it is not expecting any new data
     * to be appended to the document. 
     */
    public void partitionerFinished() {
        fPatternMatcher.forceFinalMatching();
        fPartitionerFinished = true;
        checkFinished();
    }

    /**
     * Called by this console's pattern matcher when matching is complete.
     * <p>
     * Clients should not call this method.
     * <p>
     */
    public void matcherFinished() {
        fMatcherFinished = true;
        fDocument.removeDocumentListener(fPatternMatcher);
        checkFinished();
    }

    /**
     * Fires the console output complete property change event.
     */
    private synchronized void checkFinished() {
        if (!fCompleteFired && fPartitionerFinished && fMatcherFinished) {
            fCompleteFired = true;
            firePropertyChange(this, IConsoleConstants.P_CONSOLE_OUTPUT_COMPLETE, null, null);
        }
    }

    /**
     * Adds a hyperlink to this console.
     * 
     * @param hyperlink the hyperlink to add
     * @param offset the offset in the console document at which the hyperlink should be added
     * @param length the length of the text which should be hyperlinked
     * @throws BadLocationException if the specified location is not valid.
     */
    public void addHyperlink(IHyperlink hyperlink, int offset, int length) throws BadLocationException {
        IDocument document = getDocument();
        ConsoleHyperlinkPosition hyperlinkPosition = new ConsoleHyperlinkPosition(hyperlink, offset, length);
        try {
            document.addPosition(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY, hyperlinkPosition);
            fConsoleManager.refresh(this);
        } catch (BadPositionCategoryException e) {
            ConsolePlugin.log(e);
        }
    }

    /**
     * Returns the region assocaited with the given hyperlink.
     * 
     * @param link hyperlink
     * @return the region associated witht the hyperlink or null if the hyperlink is not found.
     */
    public IRegion getRegion(IHyperlink link) {
        try {
            IDocument doc = getDocument();
            if (doc != null) {
                Position[] positions = doc.getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
                for (int i = 0; i < positions.length; i++) {
                    ConsoleHyperlinkPosition position = (ConsoleHyperlinkPosition) positions[i];
                    if (position.getHyperLink().equals(link)) {
                        return new Region(position.getOffset(), position.getLength());
                    }
                }
            }
        } catch (BadPositionCategoryException e) {
        }
        return null;
    }

    /**
     * Returns the attribue associated with the specified key.
     * 
     * @param key attribute key
     * @return the attribue associated with the specified key
     */
    public Object getAttribute(String key) {
        synchronized (fAttributes) {
            return fAttributes.get(key);
        }
    }

    /**
     * Sets an attribute value. Intended for client data.
     * 
     * @param key attribute key
     * @param value attribute value
     */
    public void setAttribute(String key, Object value) {
        synchronized (fAttributes) {
            fAttributes.put(key, value);
        }
    }
}
