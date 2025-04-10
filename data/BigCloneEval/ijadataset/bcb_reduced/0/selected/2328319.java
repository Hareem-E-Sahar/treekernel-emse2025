package freeguide.plugins.ui.horizontal.manylabels;

import freeguide.common.lib.fgspecific.data.TVProgramme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * The form that displays the listings information. Now contains only the
 * GUI code with everything else moved out.
 *
 * @author Andy Balaam
 * @author Alex Buloichik (alex73 at zaval.org)
 */
public class ViewerFrame extends JPanel {

    /** The listener for when a scroll event happens */
    private AdjustmentListener comProgramScrollListener;

    /** Combobox containing the date we are viewing */
    public JComboBox comTheDate;

    /** The combobox showing the channel set we are using */
    public JComboBox comChannelSet;

    /** The panel showing the timeline */
    public TimePanel timePanel;

    /** The side panel showing programme details */
    protected JEditorPane detailsPanel;

    /** The panel containing the channel names */
    public JPanelChannel channelNamePanel;

    /** The scrollpane that contains the names of channels */
    public JScrollPane channelNameScrollPane;

    /** The panel containing the programmes */
    private JPanelProgramme programmesPanel;

    /** The Scrollpane showing programmes */
    public JScrollPane programmesScrollPane;

    /** The JEditorPane where the printedGuide is shown */
    public JEditorPane printedGuideArea;

    /** ToDo: DOCUMENT ME! */
    private JPanel topButtonsPanel;

    /** ToDo: DOCUMENT ME! */
    private JButton butPrint;

    /** ToDo: DOCUMENT ME! */
    private JButton butDownload;

    /**
     * The splitpane splitting the main panel from the printed guide
     * and programme details
     */
    public JSplitPane splitPaneMainDet;

    /** The splitpane splitting the printed guide from programme details */
    public JSplitPane splitPaneGuideDet;

    /** The splitpane splitting the channels from programmes */
    public JSplitPane splitPaneChanProg;

    /** ToDo: DOCUMENT ME! */
    private JButton butNextDay;

    /** ToDo: DOCUMENT ME! */
    private JButton butPreviousDay;

    /** ToDo: DOCUMENT ME! */
    private JButton butGoToNow;

    /** ToDo: DOCUMENT ME! */
    private JScrollPane printedGuideScrollPane;

    /** Constructor for the FreeGuideViewer object */
    HorizontalViewer parent;

    /**
     * Creates a new ViewerFrame object.
     *
     * @param parent DOCUMENT ME!
     */
    public ViewerFrame(HorizontalViewer parent) {
        this.parent = parent;
        initialize();
    }

    public void setHTMLFont(Font font) {
        printedGuideArea.setFont(font);
        detailsPanel.setFont(font);
    }

    /**
     * Set up the entire static bit of the GUI
     */
    private void initialize() {
        java.awt.GridBagConstraints gridBagConstraints;
        topButtonsPanel = new javax.swing.JPanel();
        butGoToNow = new javax.swing.JButton();
        butPreviousDay = new javax.swing.JButton();
        comTheDate = new javax.swing.JComboBox();
        comChannelSet = new javax.swing.JComboBox();
        butNextDay = new javax.swing.JButton();
        splitPaneMainDet = new javax.swing.JSplitPane();
        printedGuideScrollPane = new FocusJScrollPane();
        printedGuideArea = new JEditorPane();
        printedGuideArea.setEditable(false);
        printedGuideArea.setContentType("text/html");
        printedGuideArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        detailsPanel = new JEditorPane();
        detailsPanel.setEditable(false);
        detailsPanel.setContentType("text/html");
        detailsPanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        splitPaneChanProg = new javax.swing.JSplitPane();
        splitPaneGuideDet = new javax.swing.JSplitPane();
        channelNameScrollPane = new FocusJScrollPane();
        channelNamePanel = new JPanelChannel(parent);
        programmesScrollPane = new FocusJScrollPane();
        programmesPanel = new JPanelProgramme(parent);
        timePanel = new TimePanel(parent);
        butPrint = new javax.swing.JButton();
        butDownload = new javax.swing.JButton();
        setLayout(new java.awt.GridBagLayout());
        topButtonsPanel.setLayout(new java.awt.GridBagLayout());
        butGoToNow.setFont(new java.awt.Font("Dialog", 0, 10));
        butGoToNow.setText(parent.getLocalizer().getString("go_to_now"));
        butGoToNow.setMnemonic(KeyEvent.VK_N);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        topButtonsPanel.add(butGoToNow, gridBagConstraints);
        butPreviousDay.setText(parent.getLocalizer().getString("minus"));
        butPreviousDay.setMnemonic(KeyEvent.VK_MINUS);
        butPreviousDay.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPreviousDayActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        topButtonsPanel.add(butPreviousDay, gridBagConstraints);
        comTheDate.setEditable(false);
        comTheDate.setFont(new java.awt.Font("Dialog", 0, 10));
        comTheDate.setMinimumSize(new java.awt.Dimension(120, 25));
        comTheDate.setPreferredSize(new java.awt.Dimension(120, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        topButtonsPanel.add(comTheDate, gridBagConstraints);
        butNextDay.setText(parent.getLocalizer().getString("plus"));
        butNextDay.setMnemonic(KeyEvent.VK_EQUALS);
        butNextDay.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNextDayActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        topButtonsPanel.add(butNextDay, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        add(topButtonsPanel, gridBagConstraints);
        comChannelSet.setEditable(false);
        comChannelSet.setFont(new java.awt.Font("Dialog", 0, 10));
        comChannelSet.setMinimumSize(new java.awt.Dimension(170, 25));
        comChannelSet.setPreferredSize(new java.awt.Dimension(140, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(comChannelSet, gridBagConstraints);
        butDownload.setFont(new java.awt.Font("Dialog", 0, 10));
        butDownload.setText(parent.getLocalizer().getString("HorizontalViewer.Download"));
        butDownload.setMnemonic(KeyEvent.VK_D);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(butDownload, gridBagConstraints);
        splitPaneMainDet.setOneTouchExpandable(true);
        splitPaneMainDet.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPaneGuideDet.setOneTouchExpandable(true);
        splitPaneGuideDet.setOrientation(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
        printedGuideScrollPane.setViewportView(printedGuideArea);
        splitPaneMainDet.setRightComponent(splitPaneGuideDet);
        splitPaneGuideDet.setLeftComponent(printedGuideScrollPane);
        FocusJScrollPane detailsScrollPane = new FocusJScrollPane();
        detailsScrollPane.setViewportView(detailsPanel);
        splitPaneGuideDet.setRightComponent(detailsScrollPane);
        channelNameScrollPane.setBorder(null);
        channelNameScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        channelNameScrollPane.setMinimumSize(new java.awt.Dimension(10, 10));
        channelNameScrollPane.setPreferredSize(new java.awt.Dimension(10, 10));
        Color bg = new java.awt.Color(245, 245, 255);
        channelNamePanel.setBackground(bg);
        JPanel tmpJPanel = new JPanel();
        tmpJPanel.setPreferredSize(new java.awt.Dimension(24, 24));
        tmpJPanel.setBackground(bg);
        channelNameScrollPane.setColumnHeaderView(tmpJPanel);
        channelNameScrollPane.setViewportView(channelNamePanel);
        splitPaneChanProg.setLeftComponent(channelNameScrollPane);
        programmesScrollPane.setBorder(null);
        programmesScrollPane.setColumnHeaderView(timePanel);
        programmesPanel.setBackground(bg);
        programmesScrollPane.setViewportView(programmesPanel);
        timePanel.setPreferredSize(new java.awt.Dimension(24, 24));
        timePanel.setLayout(null);
        timePanel.setBackground(bg);
        splitPaneChanProg.setRightComponent(programmesScrollPane);
        splitPaneChanProg.setFocusable(false);
        splitPaneChanProg.addFocusListener(new BorderChanger(splitPaneChanProg));
        splitPaneMainDet.setLeftComponent(splitPaneChanProg);
        splitPaneMainDet.addFocusListener(new BorderChanger(splitPaneMainDet));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(splitPaneMainDet, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        butPrint.setFont(new java.awt.Font("Dialog", 0, 10));
        butPrint.setText(parent.getLocalizer().getString("HorizontalViewer.Print"));
        butPrint.setMnemonic(KeyEvent.VK_P);
        butPrint.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrintActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(butPrint, gridBagConstraints);
        programmesScrollPane.getVerticalScrollBar().addAdjustmentListener(new java.awt.event.AdjustmentListener() {

            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                programmesScrollPaneVerAdjust(evt);
            }
        });
        boolean alignTextToLeftOfScreen = true;
        if (alignTextToLeftOfScreen) {
            comProgramScrollListener = new AdjustmentListener() {

                public void adjustmentValueChanged(AdjustmentEvent e) {
                    programScrolled(e);
                }
            };
            programmesScrollPane.getHorizontalScrollBar().addAdjustmentListener(comProgramScrollListener);
        }
    }

    /**
     * Event handler for when the "Print" button is pressed
     *
     * @param evt The event object
     */
    public void butPrintActionPerformed(java.awt.event.ActionEvent evt) {
        parent.printHTML();
    }

    /**
     * Event handler for when the "Next" button is clicked
     *
     * @param evt The event object
     */
    public void butNextDayActionPerformed(java.awt.event.ActionEvent evt) {
        parent.goToNextDay();
    }

    /**
     * Event handler for when the "Previous" button is clicked
     *
     * @param evt The event object
     */
    public void butPreviousDayActionPerformed(java.awt.event.ActionEvent evt) {
        parent.goToPrevDay();
    }

    /**
     * The event procedure for the vertical scrollpane listener - just
     * calls the scrollChannelNames method.
     *
     * @param evt The event object
     */
    public void programmesScrollPaneVerAdjust(java.awt.event.AdjustmentEvent evt) {
        scrollChannelNames();
    }

    /**
     * Scrolls the channel names to the same y-position as the main
     * panel.
     */
    public void scrollChannelNames() {
        channelNameScrollPane.getVerticalScrollBar().setValue(programmesScrollPane.getVerticalScrollBar().getValue());
    }

    /**
     * ToDo: DOCUMENT ME!
     *
     * @param reference ToDo: DOCUMENT ME!
     */
    void scrollToReference(String reference) {
        getPrintedGuideArea().scrollToReference(reference);
    }

    /**
     * When a scoll event happens, repaint the main panel, to allow
     * the text to be adjusted to be visible even if the programme starts off
     * to the left.
     *
     * @param e DOCUMENT ME!
     */
    private void programScrolled(AdjustmentEvent e) {
        programmesPanel.repaint();
    }

    /**
     * DOCUMENT_ME!
     *
     * @param programme DOCUMENT_ME!
     */
    public void scrollTo(final TVProgramme programme) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(programme.getStart());
        scrollToPadded(cal, programme.getChannel().getID());
    }

    private void scrollToPadded(final Calendar showTime, final String channelID) {
        getProgrammesScrollPane().getHorizontalScrollBar().setValue(getTimePanel().getScrollValue(showTime) - HorizontalViewer.PIXELS_PADDING_FROM_LEFT);
        scrollToChannel(channelID);
    }

    /**
     * DOCUMENT_ME!
     *
     * @param showTime DOCUMENT_ME!
     * @param channelID DOCUMENT ME!
     */
    public void scrollTo(final Calendar showTime, final String channelID) {
        getProgrammesScrollPane().getHorizontalScrollBar().setValue(getTimePanel().getScrollValue(showTime));
        scrollToChannel(channelID);
    }

    private void scrollToChannel(final String channelID) {
        getProgrammesScrollPane().getVerticalScrollBar().setValue(getChannelNamePanel().getScrollValue(channelID));
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JComboBox getComboDate() {
        return comTheDate;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JComboBox getComboChannelsSet() {
        return comChannelSet;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JPanelProgramme getProgrammesPanel() {
        return programmesPanel;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JScrollPane getProgrammesScrollPane() {
        return programmesScrollPane;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JEditorPane getPrintedGuideArea() {
        return printedGuideArea;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JPanelChannel getChannelNamePanel() {
        return channelNamePanel;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public TimePanel getTimePanel() {
        return timePanel;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JEditorPane getDetailsPanel() {
        return detailsPanel;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JButton getButtonGoToNow() {
        return butGoToNow;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JButton getButtonDownload() {
        return butDownload;
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JButton getDefaultButton() {
        return butGoToNow;
    }

    static class BorderChanger implements FocusListener {

        /** ToDo: DOCUMENT ME! */
        static final Border focusedBorder = new LineBorder(Color.black, 2);

        /** ToDo: DOCUMENT ME! */
        static final Border unfocusedBorder = new EmptyBorder(2, 2, 2, 2);

        /** ToDo: DOCUMENT ME! */
        JComponent borderChangee;

        /**
         * Creates a new BorderChanger object.
         *
         * @param borderChangee DOCUMENT ME!
         */
        public BorderChanger(JComponent borderChangee) {
            this.borderChangee = borderChangee;
        }

        /**
         * DOCUMENT_ME!
         *
         * @param e DOCUMENT_ME!
         */
        public void focusGained(FocusEvent e) {
            borderChangee.setBorder(focusedBorder);
        }

        /**
         * DOCUMENT_ME!
         *
         * @param e DOCUMENT_ME!
         */
        public void focusLost(FocusEvent e) {
            borderChangee.setBorder(unfocusedBorder);
        }
    }

    static class FocusJScrollPane extends JScrollPane {

        /**
         * Creates a new FocusJScrollPane object. ToDo: DOCUMENT ME!
         */
        FocusJScrollPane() {
            super();
            this.addFocusListener(new BorderChanger(this));
        }

        /**
         * DOCUMENT_ME!
         *
         * @param view DOCUMENT_ME!
         */
        public void setViewportView(Component view) {
            super.setViewportView(view);
            view.addFocusListener(new BorderChanger(this));
        }
    }
}
