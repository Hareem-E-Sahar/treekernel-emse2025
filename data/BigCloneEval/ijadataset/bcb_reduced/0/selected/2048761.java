package freeguide.plugins.ui.horizontal.manylabels;

import freeguide.common.base.IModuleConfigurationUI;
import freeguide.common.gui.LaunchBrowserOrError;
import freeguide.common.gui.SearchDialog;
import freeguide.common.lib.fgspecific.Application;
import freeguide.common.lib.fgspecific.TVChannelIconHelper;
import freeguide.common.lib.fgspecific.data.TVChannel;
import freeguide.common.lib.fgspecific.data.TVChannelsSet;
import freeguide.common.lib.fgspecific.data.TVData;
import freeguide.common.lib.fgspecific.data.TVIteratorProgrammes;
import freeguide.common.lib.fgspecific.data.TVProgramme;
import freeguide.common.lib.general.FileHelper;
import freeguide.common.lib.general.Time;
import freeguide.common.lib.general.TemplateParser;
import freeguide.common.plugininterfaces.BaseModule;
import freeguide.common.plugininterfaces.IModuleStorage;
import freeguide.common.plugininterfaces.IModuleStorage.Info;
import freeguide.common.plugininterfaces.IModuleViewer;
import freeguide.plugins.ui.horizontal.manylabels.templates.HandlerPersonalGuide;
import freeguide.plugins.ui.horizontal.manylabels.templates.HandlerProgrammeInfo;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

/**
 * Horizontal viewer plugin.
 */
public class HorizontalViewer extends BaseModule implements IModuleViewer {

    protected static final String REMINDER_MAIN = "reminder-alarm";

    /** DOCUMENT ME! */
    public static final int PIXELS_PADDING_FROM_LEFT = 100;

    /** Time formatter for 12 hour clock */
    public static final SimpleDateFormat timeFormat12Hour = new SimpleDateFormat("hh:mm aa");

    /** Time formatter for 24 hour clock */
    public static final SimpleDateFormat timeFormat24Hour = new SimpleDateFormat("HH:mm");

    /** How to format dates that go in filenames */
    public static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");

    /** Date formatter */
    public DateFormat comboBoxDateFormat = new SimpleDateFormat("d MMM yy");

    /** Date formatter */
    public DateFormat htmlDateFormat = new SimpleDateFormat("dd MMMM yyyy");

    /** Date formatter */
    public DateFormat weekdayFormat = new SimpleDateFormat("EEEE");

    /** Date formatter */
    public DateFormat shortWeekdayFormat = new SimpleDateFormat("EE");

    /** Config object. */
    protected HorizontalViewerConfig config = new HorizontalViewerConfig();

    /** UI object. */
    protected ViewerFrame panel;

    /** Current displayed date. */
    private long ourDate;

    public long todayMillis;

    /** Current displayed data. */
    public TVData currentData = new TVData();

    /**
     * A list of dates that have data worked out from the filenames in
     * the data directory.  (Members of this list are Longs.)
     */
    public LinkedList dateExistList = new LinkedList();

    /** Handlers for handle events from UI controls. */
    protected final HorizontalViewerHandlers handlers = new HorizontalViewerHandlers(this);

    protected JLabelProgramme currentProgrammeLabel;

    /** Menu item for searching through programmes */
    JMenuItem menuSearch = null;

    private boolean alreadyAskedForLoadData = false;

    /**
     * Flag to indicate search menu item had already been added.
     * Ideally, the menu item would be added when the Viewer is created, but
     * at this point, the main menu has not been created, so it cannot be
     * added.  Instead, add it once when the view is first opened.
     */
    protected boolean searchMenuAdded = false;

    /**
     * Defines the action when a mouse is used in the search window.
     * The current action is when a programme is clicked, the main viewer
     * goes to that programme.
     */
    protected MouseAdapter searchMouseAdapter = new MouseAdapter() {

        public void mouseClicked(MouseEvent e) {
            JList programmeList = (JList) (e.getSource());
            searchResultClicked(programmeList);
        }
    };

    protected KeyAdapter searchKeyAdapter = new KeyAdapter() {

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                JList programmeList = (JList) (e.getSource());
                searchResultClicked(programmeList);
            }
        }
    };

    private void searchResultClicked(JList programmeList) {
        if (panel != null) {
            TVProgramme programme = (TVProgramme) (programmeList.getSelectedValue());
            goToDate(programme.getStart());
            JLabelProgramme label = panel.getProgrammesPanel().getLabelForProgramme(programme);
            if (label != null) {
                panel.getProgrammesPanel().requestFocus();
                panel.scrollTo(programme);
                label.getActionMap().get("click").actionPerformed(new ActionEvent(label, 0, "click"));
            } else {
                JOptionPane.showMessageDialog(Application.getInstance().getCurrentFrame(), Application.getInstance().getLocalizedMessage("this_channel_is_not_visible"), Application.getInstance().getLocalizedMessage("channel_not_visible"), JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    /**
     * Get config object.
     *
     * @return config object
     */
    public Object getConfig() {
        return config;
    }

    public void setConfig(HorizontalViewerConfig config) {
        this.config = config;
        redraw();
    }

    /**
     * Set locale handler.
     *
     * @throws Exception
     */
    public void reloadResourceBundle() throws Exception {
        super.reloadResourceBundle();
        comboBoxDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        htmlDateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        weekdayFormat = new SimpleDateFormat("EEEE");
        shortWeekdayFormat = new SimpleDateFormat("EE");
    }

    /**
     * Get UI panel.
     *
     * @return UI panel
     */
    public JPanel getPanel() {
        if (panel == null) {
            if (this.menuSearch == null) {
                this.menuSearch = new JMenuItem();
            }
            panel = new ViewerFrame(this);
        }
        return panel;
    }

    /**
     * Methods to handle the current date
     */
    public void setDate(long newDate) {
        ourDate = newDate;
        Calendar cal = Calendar.getInstance(Application.getInstance().getTimeZone(), Locale.ENGLISH);
        cal.setTimeInMillis(newDate);
        cal.add(Calendar.DATE, 1);
        todayMillis = cal.getTimeInMillis() - ourDate;
    }

    public long getDate() {
        return ourDate;
    }

    /**
     * Start viewer.
     */
    public void open() {
        panel.splitPaneChanProg.setDividerLocation(config.positionSplitPaneVertical);
        panel.splitPaneMainDet.setDividerLocation(config.positionSplitPaneHorizontalTop);
        panel.splitPaneGuideDet.setDividerLocation(config.positionSplitPaneHorizontalBottom);
        setDate(System.currentTimeMillis());
        prepareChannelsSetList();
        onDataChanged();
        panel.getButtonGoToNow().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                goToNow();
            }
        });
        panel.getButtonDownload().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                Application.getInstance().doStartGrabbers();
            }
        });
        panel.getPrintedGuideArea().addHyperlinkListener(new ViewerFramePersonalGuideListener(this));
        panel.getProgrammesScrollPane().validate();
        scrollToNow();
        if (!searchMenuAdded) {
            menuSearch.setText(Application.getInstance().getLocalizedMessage("search"));
            menuSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
            Application.getInstance().getMainMenu().getTools().insert(menuSearch, 0);
            menuSearch.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    new SearchDialog(Application.getInstance().getCurrentFrame(), searchMouseAdapter, searchKeyAdapter);
                }
            });
            searchMenuAdded = true;
        }
        menuSearch.setVisible(true);
    }

    /**
     * Close viewer.
     */
    public void close() {
        saveConfigNow();
        menuSearch.setVisible(false);
        panel = null;
    }

    /**
     * Save config object immediatly.
     */
    protected void saveConfigNow() {
        config.positionSplitPaneVertical = panel.splitPaneChanProg.getDividerLocation();
        config.positionSplitPaneHorizontalTop = panel.splitPaneMainDet.getDividerLocation();
        config.positionSplitPaneHorizontalBottom = panel.splitPaneGuideDet.getDividerLocation();
        super.saveConfigNow();
    }

    /**
     * Redraw screen data.
     */
    public void redraw() {
        panel.getProgrammesScrollPane().getVerticalScrollBar().setUnitIncrement(config.sizeChannelHeight);
        panel.getProgrammesScrollPane().getHorizontalScrollBar().setUnitIncrement(config.sizeProgrammeHour / 6);
        panel.getTimePanel().repaint();
        drawProgrammes();
        updateProgrammeInfo(null);
        updatePersonalizedGuide();
    }

    /**
     * DOCUMENT_ME!
     */
    public void redrawCurrentProgramme() {
        if (currentProgrammeLabel != null) {
            currentProgrammeLabel.setupColors(getDate());
            currentProgrammeLabel.repaint();
        }
        updatePersonalizedGuide();
    }

    /**
     * Scroll to now on the time line, and update the screen if this
     * involves changing the day.
     */
    public void goToNow() {
        goToDate(System.currentTimeMillis());
        scrollToNow();
    }

    private void scrollToNow() {
        long now = System.currentTimeMillis();
        panel.getProgrammesScrollPane().getHorizontalScrollBar().setValue(panel.getTimePanel().getScrollValue(now) - PIXELS_PADDING_FROM_LEFT);
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public Info getDisplayedInfo() {
        final IModuleStorage.Info info = new IModuleStorage.Info();
        info.channelsList = getChannelsSetByName(config.currentChannelSetName);
        info.minDate = getDate();
        info.maxDate = getDate() + todayMillis;
        return info;
    }

    /**
     * Load data for current date and selected channels set.
     */
    protected void loadData() {
        synchronized (this) {
            try {
                currentData = Application.getInstance().getDataStorage().get(getDisplayedInfo());
            } catch (Exception ex) {
                Application.getInstance().getLogger().log(Level.WARNING, "Error reading TV data", ex);
            }
            if (currentData.getChannelsCount() == 0) {
                askForLoadData();
            }
        }
    }

    protected void askForLoadData() {
        if (!alreadyAskedForLoadData) {
            alreadyAskedForLoadData = true;
            int r = JOptionPane.showConfirmDialog(Application.getInstance().getCurrentFrame(), Application.getInstance().getLocalizedMessage("there_are_missing_listings_for_today"), Application.getInstance().getLocalizedMessage("download_listings_q"), JOptionPane.YES_NO_OPTION);
            if (r == 0) {
                Application.getInstance().doStartGrabbers();
            }
        }
    }

    /**
     * Draw all the programmes and channels on screen.
     */
    protected synchronized void drawProgrammes() {
        currentProgrammeLabel = null;
        JLabelProgramme.setupLabel(this);
        DateFormat timeFormat = getCurrentDateFormat();
        final Font font = new Font(config.fontName, config.fontStyle, config.fontSize);
        panel.setHTMLFont(font);
        final TVChannelsSet currentChannelSet = (TVChannelsSet) getChannelsSetByName(config.currentChannelSetName).clone();
        Iterator it = currentChannelSet.getChannels().iterator();
        while (it.hasNext()) {
            TVChannelsSet.Channel listCh = (TVChannelsSet.Channel) it.next();
            if (!currentData.containsChannel(listCh.getChannelID())) {
                it.remove();
            }
        }
        panel.getProgrammesPanel().init(getDate(), font, currentChannelSet.getChannels().size(), timeFormat);
        final List channels = new ArrayList();
        for (it = currentChannelSet.getChannels().iterator(); it.hasNext(); ) {
            TVChannelsSet.Channel listCh = (TVChannelsSet.Channel) it.next();
            TVChannel curChan = currentData.get(listCh.getChannelID());
            channels.add(curChan);
        }
        panel.getChannelNamePanel().setFont(font);
        panel.getChannelNamePanel().setChanels((TVChannel[]) channels.toArray(new TVChannel[channels.size()]));
        panel.getChannelNamePanel().setPreferredSize(new Dimension(panel.getChannelNamePanel().getMaxChannelWidth(), (currentChannelSet.getChannels().size() * config.sizeChannelHeight) + 50));
        Dimension tmp = new Dimension((int) (config.sizeProgrammeHour * todayMillis / Time.HOUR), currentChannelSet.getChannels().size() * config.sizeChannelHeight);
        panel.getProgrammesPanel().setPreferredSize(tmp);
        panel.getProgrammesPanel().setMinimumSize(tmp);
        panel.getProgrammesPanel().setMaximumSize(tmp);
        tmp = new Dimension((int) (config.sizeProgrammeHour * todayMillis / Time.HOUR), panel.getTimePanel().getPreferredSize().height);
        panel.getTimePanel().setPreferredSize(tmp);
        panel.getTimePanel().setMinimumSize(tmp);
        panel.getTimePanel().setMaximumSize(tmp);
        panel.getTimePanel().setTimes(getDate(), getDate() + todayMillis);
        currentData.iterate(new TVIteratorProgrammes() {

            protected void onChannel(TVChannel channel) {
            }

            public void onProgramme(TVProgramme programme) {
                int row = currentChannelSet.getChannelIndex(getCurrentChannel().getID());
                if (row != -1) {
                    panel.getProgrammesPanel().addProgramme(programme, row);
                }
            }
        });
        panel.getProgrammesPanel().sort();
        panel.getTimePanel().revalidate();
        panel.getTimePanel().repaint();
        panel.getProgrammesPanel().revalidate();
        panel.getProgrammesPanel().repaint();
        panel.getChannelNamePanel().revalidate();
        panel.getChannelNamePanel().repaint();
    }

    /**
     * Change the date combo to the given date. Will trigger an event
     * causing a repaint of all the programmes.
     *
     * @param newDate DOCUMENT ME!
     */
    private void goToDate(long newDate) {
        boolean moved = false;
        JComboBox cmbDate = panel.getComboDate();
        int lastResortIndex = 0;
        ListIterator it = dateExistList.listIterator(0);
        while (it.hasNext()) {
            long itemdt = ((Long) (it.next())).longValue();
            if (newDate >= itemdt) {
                lastResortIndex = it.previousIndex();
                Calendar itemCal = Calendar.getInstance(Application.getInstance().getTimeZone(), Locale.ENGLISH);
                itemCal.setTimeInMillis(itemdt);
                itemCal.add(Calendar.DATE, 1);
                if (newDate < (itemCal.getTimeInMillis())) {
                    cmbDate.setSelectedIndex(lastResortIndex);
                    moved = true;
                    break;
                }
            }
        }
        if (!moved) {
            Calendar now = Calendar.getInstance(Application.getInstance().getTimeZone(), Locale.ENGLISH);
            now.add(Calendar.DATE, -1);
            if (newDate >= (now.getTimeInMillis())) {
                it = dateExistList.listIterator(0);
                long prevdt = 0;
                while (it.hasNext()) {
                    long itemdt = ((Long) (it.next())).longValue();
                    if ((prevdt < newDate) && (newDate < itemdt)) {
                        addDateExistItem(cmbDate, newDate, it.previousIndex());
                        moved = true;
                        break;
                    }
                    prevdt = itemdt;
                }
                if (!moved) {
                    addDateExistItem(cmbDate, newDate, it.nextIndex());
                }
            } else {
                long lastresortlong = ((Long) (dateExistList.get(lastResortIndex))).longValue();
                if ((cmbDate.getSelectedIndex() == lastResortIndex) && (newDate > lastresortlong)) {
                    ++lastResortIndex;
                }
                cmbDate.setSelectedIndex(lastResortIndex);
            }
        }
    }

    private void addDateExistItem(JComboBox cmbDate, long newDate, int idx) {
        Calendar cal = Calendar.getInstance(Application.getInstance().getTimeZone(), Locale.ENGLISH);
        cal.setTimeInMillis(newDate);
        config.dayStartTime.adjustCalendar(cal);
        Date date = cal.getTime();
        String thisDate = shortWeekdayFormat.format(date) + " " + comboBoxDateFormat.format(date);
        dateExistList.add(idx, new Long(cal.getTimeInMillis()));
        cmbDate.insertItemAt(thisDate, idx);
        cmbDate.setSelectedIndex(idx);
    }

    /**
     * Change the date
     */
    private void changeDay(int offset) {
        Calendar cal = Calendar.getInstance(Application.getInstance().getTimeZone(), Locale.ENGLISH);
        cal.setTimeInMillis(getDate());
        cal.add(Calendar.DATE, offset);
        goToDate(cal.getTimeInMillis());
    }

    /**
     * Move forward in time one day.
     */
    public void goToNextDay() {
        changeDay(1);
    }

    /**
     * Move backward in time one day.
     */
    public void goToPrevDay() {
        changeDay(-1);
    }

    /**
     * DOCUMENT_ME!
     */
    public void onChannelsSetsChanged() {
        prepareChannelsSetList();
        loadData();
        redraw();
    }

    /**
     * DOCUMENT_ME!
     */
    public void onDataChanged() {
        prepareDateList();
        loadData();
        goToDate(getDate());
        redraw();
    }

    protected void prepareChannelsSetList() {
        panel.getComboChannelsSet().removeItemListener(handlers.comboChannelsSetItemListener);
        int itemCount = panel.getComboChannelsSet().getItemCount();
        for (int i = 0; i < itemCount; i++) {
            panel.getComboChannelsSet().removeItemAt(0);
        }
        panel.getComboChannelsSet().insertItemAt(getLocalizer().getString("all_channels"), 0);
        for (int i = 0; i < Application.getInstance().getChannelsSetsList().size(); i++) {
            TVChannelsSet cs = (TVChannelsSet) Application.getInstance().getChannelsSetsList().get(i);
            panel.getComboChannelsSet().addItem(cs.getName());
        }
        panel.getComboChannelsSet().addItem(getLocalizer().getString("edit_channels_sets"));
        TVChannelsSet cs = getChannelsSetByName(config.currentChannelSetName);
        if (cs == null) {
            config.currentChannelSetName = null;
            panel.getComboChannelsSet().setSelectedIndex(0);
        } else {
            panel.getComboChannelsSet().setSelectedIndex(Application.getInstance().getChannelsSetsList().indexOf(cs) + 1);
        }
        panel.getComboChannelsSet().addItemListener(handlers.comboChannelsSetItemListener);
    }

    protected TVChannelsSet getChannelsSetByName(final String channelsSetName) {
        if (channelsSetName == null) {
            return Application.getInstance().getDataStorage().getInfo().channelsList;
        } else {
            for (int i = 0; i < Application.getInstance().getChannelsSetsList().size(); i++) {
                TVChannelsSet cs = (TVChannelsSet) Application.getInstance().getChannelsSetsList().get(i);
                if (channelsSetName.equals(cs.getName())) {
                    return cs;
                }
            }
            return null;
        }
    }

    protected void prepareDateList() {
        IModuleStorage.Info info = Application.getInstance().getDataStorage().getInfo();
        Calendar cal = Calendar.getInstance(Application.getInstance().getTimeZone(), Locale.ENGLISH);
        cal.setTimeInMillis(info.minDate);
        config.dayStartTime.adjustCalendar(cal);
        if (cal.getTimeInMillis() > info.minDate) {
            cal.add(Calendar.DATE, -1);
        }
        dateExistList.clear();
        for (; cal.getTimeInMillis() <= info.maxDate; cal.add(Calendar.DATE, 1)) {
            dateExistList.add(new Long(cal.getTimeInMillis()));
        }
        JComboBox cmbDate = panel.getComboDate();
        cmbDate.removeItemListener(handlers.comboDateItemListener);
        ((DefaultComboBoxModel) cmbDate.getModel()).removeAllElements();
        comboBoxDateFormat.setTimeZone(Application.getInstance().getTimeZone());
        ListIterator it = dateExistList.listIterator();
        while (it.hasNext()) {
            long dt = ((Long) (it.next())).longValue();
            Date date = new Date(dt);
            cmbDate.addItem(shortWeekdayFormat.format(date) + " " + comboBoxDateFormat.format(date));
        }
        cmbDate.addItemListener(handlers.comboDateItemListener);
    }

    /**
     * DOCUMENT_ME!
     *
     * @param parentDialog DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public IModuleConfigurationUI getConfigurationUI(JDialog parentDialog) {
        return new ConfigureUIController(this, parentDialog);
    }

    /**
     * DOCUMENT_ME!
     *
     * @return DOCUMENT_ME!
     */
    public JButton getDefaultButton() {
        return panel.getDefaultButton();
    }

    /**
     * DOCUMENT_ME!
     *
     * @param channel DOCUMENT_ME!
     */
    public void changeIconActionPerformed(final TVChannel channel) {
        JFileChooser chooser = new JFileChooser(config.lastIconDir);
        chooser.setFileFilter(new FileFilter() {

            private Pattern images = null;

            public boolean accept(File f) {
                if (images == null) {
                    images = Pattern.compile("\\.(?i)(?:jpe?g|gif|png|JPG)$");
                }
                return f.isDirectory() || images.matcher(f.getName()).find();
            }

            public String getDescription() {
                return getLocalizer().getString("images_gif_jpeg_png");
            }
        });
        int returnVal = chooser.showOpenDialog(panel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            config.lastIconDir = chooser.getCurrentDirectory().toString();
            try {
                FileHelper.copy(chooser.getSelectedFile(), new File(TVChannelIconHelper.getIconFileName(channel)));
                redraw();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Event handler for the channel -> reset icon menu entry
     *
     * @param channel
     */
    public void resetIconActionPerformed(final TVChannel channel) {
        new File(TVChannelIconHelper.getIconFileName(channel)).delete();
        redraw();
    }

    /**
     * Print personalized guide.
     */
    public void printHTML() {
        File f = new File(Application.getInstance().getWorkingDirectory() + "/guide.html");
        try {
            BufferedWriter buffy = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
            TemplateParser parser = new TemplateParser("resources/plugins/ui/horizontal/manylabels/templates/TemplatePersonalGuidePrint.html");
            parser.process(new HandlerPersonalGuide(getLocalizer(), currentData, new Date(getDate()), htmlDateFormat, weekdayFormat, getCurrentDateFormat(), true), buffy);
            buffy.close();
            LaunchBrowserOrError.browseLocalFileOrError(f);
        } catch (Exception ex) {
            Application.getInstance().getLogger().log(Level.WARNING, "Error write HTML guide", ex);
        }
    }

    /**
     * Refresh personalized guide.
     */
    protected void updatePersonalizedGuide() {
        StringWriter str = new StringWriter();
        try {
            TemplateParser parser = new TemplateParser("resources/plugins/ui/horizontal/manylabels/templates/TemplatePersonalGuide.html");
            parser.process(new HandlerPersonalGuide(getLocalizer(), currentData, new Date(getDate()), htmlDateFormat, weekdayFormat, getCurrentDateFormat(), false), str);
        } catch (Exception ex) {
            Application.getInstance().getLogger().log(Level.SEVERE, "Error construct personalized HTML guide for screen", ex);
        }
        panel.getPrintedGuideArea().setText(str.toString());
        panel.getPrintedGuideArea().setCaretPosition(0);
    }

    /**
     * Refresh programme detail information.
     *
     * @param programme
     */
    protected void updateProgrammeInfo(final TVProgramme programme) {
        try {
            final TemplateParser parser = new TemplateParser("resources/plugins/ui/horizontal/manylabels/templates/TemplateProgrammeInfo.html");
            StringWriter out = new StringWriter();
            parser.process(new HandlerProgrammeInfo(getLocalizer(), programme, getCurrentDateFormat()), out);
            panel.getDetailsPanel().setText(out.getBuffer().toString());
            panel.getDetailsPanel().setCaretPosition(0);
        } catch (Exception ex) {
            Application.getInstance().getLogger().log(Level.SEVERE, "Error construct programme info HTML for screen", ex);
        }
    }

    protected DateFormat getCurrentDateFormat() {
        final DateFormat result = config.display24time ? HorizontalViewer.timeFormat24Hour : HorizontalViewer.timeFormat12Hour;
        result.setTimeZone(Application.getInstance().getTimeZone());
        return result;
    }
}
