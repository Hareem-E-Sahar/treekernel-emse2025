package com.mindtree.techworks.insight.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import com.mindtree.techworks.insight.Controller;
import com.mindtree.techworks.insight.InsightConstants;
import com.mindtree.techworks.insight.eventsearch.SearchCriteria;
import com.mindtree.techworks.insight.gui.action.FindAction;
import com.mindtree.techworks.insight.gui.widgets.StatusBar;
import com.mindtree.techworks.insight.pagination.IPage;
import com.mindtree.techworks.insight.spi.LogEvent;

/**
*
* The <code>EventDetailsPresentation</code> class is a Presentation implementation
* that displays details of one LoggingEvent at a time. Uses a JEditorPane to display
* the results.   
*
* @author  Regunath B
* @version 1.0, 04/10/25
* @see     com.mindtree.techworks.insight.gui.Presentation
* @see     org.apache.log4j.spi.LoggingEvent
* @see     com.mindtree.techworks.insight.gui.EventListPresentation
*/
public class EventDetailsPresentation extends JPanel implements Presentation {

    /**
	 * Used for object serialization
	 */
    private static final long serialVersionUID = -3210768545540400816L;

    /**
	 * Useful constants for the preferred size of this Presentation 
	 */
    private static final int WIDTH = 900;

    private static final int HEIGHT = 300;

    /**
	 * Useful constant that identifies the class name of the EventListPresentation
	 * that this Presentation is interested in for widget change notifications.
	 */
    private static final String EVENT_LIST_PRESENTATION = EventListPresentation.class.getName();

    /**
	 * The Controller instance for this Presentation
	 */
    private Controller controller;

    /**
	 * The LogEvent that is rendered by this Presentation
	 */
    private LogEvent event;

    /**
	 * The MessageFormat instance used for formatting the event details display
	 */
    private static final MessageFormat FORMATTER = new MessageFormat("<b>" + InsightConstants.getLiteral("NAMESPACE") + ":</b> <code>{0}</code>" + "<br><b>" + InsightConstants.getLiteral("PRIORITY_LABEL") + ":</b> <code>{1}</code>" + "<br><b>" + InsightConstants.getLiteral("THREAD_NAME_LABEL") + ":</b> <code>{2}</code>" + "<br><b>" + InsightConstants.getLiteral("LOGGER_NAME") + ":</b> <code>{3}</code>" + "<br><b>" + InsightConstants.getLiteral("MESSAGE_LABEL") + ":</b>" + "<pre>{4}</pre>" + "<b>" + InsightConstants.getLiteral("THROWABLE") + ":</b>" + "<pre>{5}</pre>");

    /**
	 * The JEditorPane instance used to render the the event details
	 */
    private JTextPane eventDetails;

    /**
     * Constructor for this class
     * @param controller the Controller for this Presentation
     */
    public EventDetailsPresentation(Controller controller) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(InsightConstants.getLiteral("DETAILS")));
        this.eventDetails = new JTextPane();
        this.eventDetails.setEditable(false);
        this.eventDetails.setContentType("text/html");
        this.eventDetails.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent event) {
                if (getEvent() != null && event.getKeyCode() == KeyEvent.VK_F && event.isControlDown()) {
                    FindAction.getInstance(getController().getInsight()).showSearchTextFrame();
                }
            }
        });
        add(new JScrollPane(eventDetails), BorderLayout.CENTER);
        this.setPreferredSize(getIdealPreferredSize());
        this.controller = controller;
        this.controller.registerPresentation(this);
        this.controller.registerWidgetChangeListener(controller.getPresentation(EVENT_LIST_PRESENTATION), this);
    }

    /**
	 * Presentation interface method implementation. Returns the fully qualified class name
	 * of this Presentation
	 * @see com.mindtree.techworks.insight.gui.Presentation#getUID()
	 */
    public String getUID() {
        return this.getClass().getName();
    }

    /**
	 * Presentation interface method implementation. Renders the display with the
	 * specified data. Type-casts the Object data as a org.apache.log4j.spi.LoggingEvent
	 * and renders its details. 
	 * @see com.mindtree.techworks.insight.gui.Presentation#notifyWidgetStateChange(com.mindtree.techworks.insight.gui.Presentation, int, java.lang.Object)
	 */
    public void notifyWidgetStateChange(Presentation presentation, int identifier, Object data) {
        this.event = (LogEvent) data;
        final Object[] args = { event.getNamespace().getNamespaceAsString(), event.getLevel(), escape(event.getThreadName()), escape(event.getLoggerName()), escape(event.getMessage().toString()), escape(getThrowableInfoAsString(event.getThrowableStrRepresentation())) };
        eventDetails.setText(FORMATTER.format(args));
        eventDetails.setCaretPosition(0);
    }

    /**
	 * Presentation interface method implementation. Returns this Presentation.
	 * @see com.mindtree.techworks.insight.gui.Presentation#getViewComponent()
	 */
    public JComponent getViewComponent() {
        return this;
    }

    /**
	 * Presentation interface method implementation. Returns false.
	 * @see com.mindtree.techworks.insight.gui.Presentation#doesProcessRealTimeUpdates()
	 */
    public boolean doesProcessRealTimeUpdates() {
        return false;
    }

    /**
	 * Presentation interface method implementation. Does nothing. 
	 * @see com.mindtree.techworks.insight.gui.Presentation#processRealTimeUpdate(com.mindtree.techworks.insight.spi.LogEvent)
	 */
    public void processRealTimeUpdate(LogEvent logEvent) {
    }

    /** 
	 * Interface method implementation
	 * @see com.mindtree.techworks.insight.gui.Presentation#resetWidgets()
	 */
    public void resetWidgets() {
        this.event = null;
        eventDetails.setText("");
    }

    /**
     * Highlights text in the event details display that match the specified search text specified limited to
     * the specified log event attributes
     * @param searchText the text to search for
     * @param searchType the search type. See SearchCriteria for type definitions
     * @return true if atleast one macth is found, false otherwise
     * @see com.mindtree.techworks.insight.eventsearch.SearchCriteria
     */
    public boolean highlightText(String searchText, int searchType) {
        int firstIndex = -1;
        try {
            Document document = eventDetails.getDocument();
            String sourceText = document.getText(0, document.getLength()).toUpperCase();
            LinkedList matchAttributeBoundaryList = new LinkedList();
            constructMatchAttributeBoundaryList(sourceText, matchAttributeBoundaryList, searchType);
            Highlighter highlighter = eventDetails.getHighlighter();
            highlighter.removeAllHighlights();
            Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(UIManager.getDefaults().getColor(InsightConstants.TEXT_HIGHLIGHT));
            Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE | Pattern.CANON_EQ | Pattern.UNICODE_CASE);
            Matcher matcher = pattern.matcher(sourceText);
            while (matcher.find()) {
                int searchIndex = matcher.start();
                Iterator iterator = matchAttributeBoundaryList.iterator();
                while (iterator.hasNext()) {
                    AttributeBoundary ab = (AttributeBoundary) iterator.next();
                    if (ab.containsEquals(searchType, searchIndex)) {
                        highlighter.addHighlight(searchIndex, matcher.end(), highlightPainter);
                    }
                }
                if (firstIndex == -1 && searchIndex > -1) {
                    firstIndex = searchIndex;
                }
            }
            if (firstIndex > -1) {
                eventDetails.setCaretPosition(firstIndex);
            } else {
                StatusBar.getInstance().setDisplayText(0, InsightConstants.getLiteral("ERROR_FIND_FAILURE"), false);
            }
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        return ((firstIndex > -1));
    }

    /**
	 * Interface method implementation
	 * @see Presentation#displayPage(IPage, long)
	 */
    public void displayPage(IPage page, long eventSequenceNumber) {
        if (eventSequenceNumber < 0) {
            resetWidgets();
        }
    }

    /**
	 * @return Returns the controller.
	 */
    public Controller getController() {
        return controller;
    }

    /**
	 * @return Returns the event.
	 */
    public LogEvent getEvent() {
        return event;
    }

    /**
	 * Presentation Interface method implementation
	 * @see com.mindtree.techworks.insight.gui.Presentation#setScrollLock(boolean)
	 */
    public void setScrollLock(boolean status) {
    }

    /**
	 * Private helper method that returned a string that escapes the special
	 * characters in the specified string to a HTML compatible form 
	 * @param aStr the string whose characters need to be escaped for HTML rendering
	 * @return the escaped string
	 */
    private String escape(String aStr) {
        if (aStr == null) {
            return null;
        }
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < aStr.length(); i++) {
            char c = aStr.charAt(i);
            switch(c) {
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '\"':
                    buf.append("&quot;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                default:
                    buf.append(c);
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * Private helper method that converts the specified throwable info String[]
     * into a string with each String in the array as a new line
     * @param trace String[] of throwable information
     * @return single String that is a concatenated form of the throwable information
     */
    private String getThrowableInfoAsString(String[] trace) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < trace.length; i++) {
            buffer.append(trace[i]).append("\n");
        }
        return buffer.toString();
    }

    /**
	 * Helper method that returns the best size between this component's preferred size and that ideal 
	 * for the display size
	 * @return Dimension most appropriate for this Presentation
	 */
    private Dimension getIdealPreferredSize() {
        Dimension dimension = null;
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        dimension = new Dimension((int) Math.min(screenDim.getWidth() - 50, WIDTH), (int) Math.min(screenDim.getHeight() / 3, HEIGHT));
        return dimension;
    }

    /**
	 * Helper method that constructs the list of AttributeBoundary instances based
	 * on the specified search type
	 * @param sourceText the text of the event details document object in uppercase
	 * @param matchAttributeBoundaryList list to populate the AttributeBoundary instances
	 * @param searchType the searchType as defined in SearchCriteria
	 * @see SearchCriteria
	 */
    private void constructMatchAttributeBoundaryList(String sourceText, LinkedList matchAttributeBoundaryList, int searchType) {
        if (this.event == null) {
            return;
        }
        int attributeTextStartIndex = 0;
        int attributeIndex = sourceText.indexOf(this.event.getNamespace().getNamespaceAsString().toUpperCase(), attributeTextStartIndex);
        int attributeLength = this.event.getNamespace().getNamespaceAsString().length();
        if ((searchType & SearchCriteria.NAMESPACE_SEARCH) == SearchCriteria.NAMESPACE_SEARCH) {
            matchAttributeBoundaryList.add(new AttributeBoundary(SearchCriteria.NAMESPACE_SEARCH, attributeTextStartIndex, attributeIndex + attributeLength));
        }
        attributeTextStartIndex = attributeIndex + attributeLength;
        attributeIndex = sourceText.indexOf(this.event.getLevel().toString().toUpperCase(), attributeTextStartIndex);
        attributeLength = this.event.getLevel().toString().length();
        if ((searchType & SearchCriteria.PRIORITY_SEARCH) == SearchCriteria.PRIORITY_SEARCH) {
            matchAttributeBoundaryList.add(new AttributeBoundary(SearchCriteria.PRIORITY_SEARCH, attributeTextStartIndex, attributeIndex + attributeLength));
        }
        attributeTextStartIndex = attributeIndex + attributeLength;
        attributeIndex = sourceText.indexOf(this.event.getThreadName().toUpperCase(), attributeTextStartIndex);
        attributeLength = this.event.getThreadName().length();
        if ((searchType & SearchCriteria.THREAD_SEARCH) == SearchCriteria.THREAD_SEARCH) {
            matchAttributeBoundaryList.add(new AttributeBoundary(SearchCriteria.THREAD_SEARCH, attributeTextStartIndex, attributeIndex + attributeLength));
        }
        attributeTextStartIndex = attributeIndex + attributeLength;
        attributeIndex = sourceText.indexOf(this.event.getLoggerName().toUpperCase(), attributeTextStartIndex);
        attributeLength = this.event.getLoggerName().length();
        if ((searchType & SearchCriteria.CATEGORY_SEARCH) == SearchCriteria.CATEGORY_SEARCH) {
            matchAttributeBoundaryList.add(new AttributeBoundary(SearchCriteria.CATEGORY_SEARCH, attributeTextStartIndex, attributeIndex + attributeLength));
        }
        attributeTextStartIndex = attributeIndex + attributeLength;
        attributeIndex = sourceText.indexOf(this.event.getMessage().toString().toUpperCase().replaceAll("\r", ""), attributeTextStartIndex);
        attributeLength = this.event.getMessage().toString().length();
        if (((searchType & SearchCriteria.MESSAGE_SEARCH) == SearchCriteria.MESSAGE_SEARCH) || ((searchType & SearchCriteria.EXCEPTION_CLASS_NAME_SEARCH) == SearchCriteria.EXCEPTION_CLASS_NAME_SEARCH)) {
            matchAttributeBoundaryList.add(new AttributeBoundary(SearchCriteria.MESSAGE_SEARCH, attributeTextStartIndex, attributeIndex + attributeLength));
            matchAttributeBoundaryList.add(new AttributeBoundary(SearchCriteria.EXCEPTION_CLASS_NAME_SEARCH, attributeTextStartIndex, attributeIndex + attributeLength));
        }
        attributeTextStartIndex = attributeIndex + attributeLength;
        attributeIndex = sourceText.indexOf(getThrowableInfoAsString(this.event.getThrowableStrRepresentation()).toUpperCase(), attributeTextStartIndex);
        attributeLength = getThrowableInfoAsString(this.event.getThrowableStrRepresentation()).length();
        if (((searchType & SearchCriteria.THROWABLE_SEARCH) == SearchCriteria.THROWABLE_SEARCH) || ((searchType & SearchCriteria.EXCEPTION_CLASS_NAME_SEARCH) == SearchCriteria.EXCEPTION_CLASS_NAME_SEARCH)) {
            matchAttributeBoundaryList.add(new AttributeBoundary(SearchCriteria.THROWABLE_SEARCH, attributeTextStartIndex, attributeIndex + attributeLength));
            matchAttributeBoundaryList.add(new AttributeBoundary(SearchCriteria.EXCEPTION_CLASS_NAME_SEARCH, attributeTextStartIndex, attributeIndex + attributeLength));
        }
    }

    /**
	 * Helper class that contains the boundary details of a LogEvent 
	 * attribute such as start and end indices in the details display text 
	 */
    private class AttributeBoundary {

        /**
		 * Attribute type as defined in SearchCriteria
		 */
        private int attributeType;

        /**
		 * The start Caret position in the display for the LogEvent field/attribute
		 */
        private int startIndex = -1;

        /**
		 * The end Caret position in the display for the LogEvent field/attribute
		 */
        private int endIndex = -1;

        /**
		 * Constructor for this class
		 * @param attributeType valid attribute type as defined in SearchCriteria
		 * @param startIndex the start Caret position
		 * @param endIndex the end Caret position
		 * @see SearchCriteria
		 */
        public AttributeBoundary(int attributeType, int startIndex, int endIndex) {
            this.attributeType = attributeType;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        /**
		 * Determines if the specified attributeType matches this AttributeBoundary type and index lies within it
		 * @param attributeType the valid attribute type as defined in SearchCriteria
		 * @param index the index to be verified for containment within this AttributeBoundary coordinates
		 * @return true if the type matches and the index is contained in this AttributeBoundary
		 */
        public boolean containsEquals(int attributeType, int index) {
            return (((attributeType & this.attributeType) == this.attributeType) && index >= startIndex && index <= endIndex);
        }
    }
}
