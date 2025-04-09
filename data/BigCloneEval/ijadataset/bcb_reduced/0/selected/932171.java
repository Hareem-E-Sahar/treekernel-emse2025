package penguin.dataset;

import java.awt.Point;
import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.Date;
import org.apache.xmlbeans.XmlOptions;
import org.penguinuri.penguin.*;
import penguin.Program;
import penguin.gui.LoadingBar;
import penguin.helpers.*;

/**
 * 
 * A site, this is the top most parent of the penguin.dataset package. It
 * contains all the child objects needed to represent an entire site.
 * 
 * @author Tim Dunstan
 * @author Laurence Twynam-Perkins
 * @version 2.2
 */
public class Site implements PenguinDataType {

    private LinkedList<Day> days = new LinkedList<Day>();

    private LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();

    private LinkedList<ImageEvent> events = new LinkedList<ImageEvent>();

    private HashMap<String, Template> templates = new HashMap<String, Template>();

    private String siteName = "", seasonName, xmlLocation = "";

    protected static final String[] defaultCounts = Program.Settings().getDefaultCountNames();

    protected static final ImageEvent[] defaultImageEvents = Program.Settings().getDefaultImagesEvents();

    protected static final EventValue[] defaultEventValues = Program.Settings().getDefaultEventValues();

    protected static final String[] defaultBookmarkTitles = Program.Settings().getDefaultBookmarkTitles();

    /** For use when checking if XML data set has been modified. */
    protected static int DATA_VERSION = 3;

    /**
	 * List of names of each count available for this site.
	 */
    private LinkedList<String> countNames = new LinkedList<String>();

    private Template mainTemplate = null;

    private boolean hasMainTemplate = false;

    private boolean isChanged = false;

    /**
	 * Instantiate a site object.
	 * 
	 * @param name
	 *            the Name of the site.
	 */
    public Site(String site, String season) {
        siteName = site;
        seasonName = season;
        xmlLocation = String.format("%s/%s/%s", Program.Settings().getDataFolder(), season, site);
    }

    /**
	 * Add a day to the current site.
	 * Insertion sorted.
	 * 
	 * @param day
	 *            the day to be added.
	 */
    public void addDay(Day day) {
        days.add(day);
        Collections.sort(days, new DayComparer());
    }

    public void addBookmark(Bookmark bookmark) {
        bookmarks.add(bookmark);
    }

    public Bookmark getBookmark(int id) {
        return bookmarks.get(id);
    }

    public LinkedList<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public LinkedList<ImageEvent> getEvents() {
        return events;
    }

    public HashMap<String, Template> getTemplates() {
        return templates;
    }

    /**
	 * Remove a day from the current set.
	 * 
	 * @param day
	 */
    public void removeDay(Day day) {
        days.remove(day);
    }

    /**
	 * Add an image with a specific date to the current day.
	 * 
	 * @param date The to which the object will be added.
	 * @param location The image's location as a String.
	 */
    public NestImage addImage(java.util.Date date, String location) {
        Calendar iCal = Calendar.getInstance();
        iCal.setTime(date);
        int year = iCal.get(Calendar.YEAR);
        int month = iCal.get(Calendar.MONTH);
        int day = iCal.get(Calendar.DAY_OF_MONTH);
        NestImage ni = new NestImage(date, location, defaultEventValues, countNames.toArray().length, System.getProperty("user.name"));
        Iterator<Day> dayIt = iterator();
        int index = 0;
        while (dayIt.hasNext()) {
            Day currentDay = dayIt.next();
            Calendar dCal = Calendar.getInstance();
            dCal.setTime(currentDay.getDate());
            int y = dCal.get(Calendar.YEAR);
            int m = dCal.get(Calendar.MONTH);
            int d = dCal.get(Calendar.DAY_OF_MONTH);
            if ((year == y && month == m && day == d)) {
                currentDay.addImage(ni);
                return ni;
            }
            index++;
        }
        Day newDay = new Day(date, this);
        newDay.addImage(ni);
        addDay(newDay);
        return ni;
    }

    /**
	 * Get a Day within a site by ID.
	 * 
	 * @param id
	 *            the ID of the day you wish to get.
	 * @return the day to which the ID belongs.
	 */
    public Day getDay(int id) {
        return this.days.get(id);
    }

    public LinkedList<Day> getDays() {
        return days;
    }

    /**
	 * Returns the number of days within the site.
	 * 
	 * @return
	 */
    public int numberOfDays() {
        return this.days.size();
    }

    /**
	 * Determine if the site is empty or not.
	 * 
	 * @return True if there are no days within the site.
	 */
    public boolean isEmpty() {
        return days.isEmpty();
    }

    /**
	 * Gets an iterator for the days within the site.
	 * 
	 * @return
	 */
    public Iterator<Day> iterator() {
        return days.iterator();
    }

    /**
	 * Returns a template by Unique ID String.
	 * 
	 * @param name
	 *            the name of the template.
	 * @return
	 */
    public Template getTemplate(String name) {
        Template t = templates.get(name);
        if (t == null && (name != null && !name.equals("") && !name.equals("null"))) {
            Debug.print("[Site] getTemplate: Oh no! template isn't in the hash table..");
            t = new Template(xmlLocation, name);
            Debug.print("[Site] getTemplate: " + xmlLocation + " - " + name);
            addTemplate(t);
        }
        return t;
    }

    /**
	 * Adds a new blank template to the current Site object.
	 * @return
	 */
    public Template addTemplate() {
        Template t = new Template(xmlLocation);
        templates.put(t.toString(), t);
        return t;
    }

    /**
	 * Add a new template to the current sites list.
	 * 
	 * @param template
	 */
    public void addTemplate(Template template) {
        templates.put(template.toString(), template);
    }

    /**
	 * Returns the name of the season.
	 * @return
	 */
    public String getSeason() {
        return this.seasonName;
    }

    /**
	 * Returns the name of the site.
	 * @return
	 */
    public String getSite() {
        return siteName;
    }

    @Override
    public String toString() {
        return siteName;
    }

    /**
	 * Save the current site to its XML file.
	 * 
	 * @return true if successful.
	 */
    public boolean save() {
        return save(xmlLocation);
    }

    /**
	 * Save the current site to an XML file.
	 * 
	 * @param location
	 *            the location of the sites XML file.
	 * @return true if successful.
	 */
    private boolean save(String location) {
        try {
            Debug.print(getClass(), "save(): Saving metadata to XML.");
            java.io.File file = new java.io.File(location + "/" + "PenguinData.xml");
            LoadingBar lb = new LoadingBar(days.size() + 2);
            lb.setVisible(true);
            lb.setTitle("Setting up files and folders");
            if (!file.exists()) {
                java.io.File dir = new java.io.File(location);
                dir.mkdirs();
                file.createNewFile();
            }
            SiteDocument s = null;
            SiteDocument.Site site = null;
            try {
                s = SiteDocument.Factory.parse(file);
                site = s.getSite();
            } catch (Exception ex) {
                Debug.print("save(): Site data didn't exist");
                s = SiteDocument.Factory.newInstance();
                site = s.addNewSite();
                site.setSeason(String.format("%s/%s", seasonName, siteName));
                for (int i = 0; i < defaultCounts.length; i++) {
                    Category cat = site.addNewCategory();
                    cat.setTitle(defaultCounts[i]);
                    cat.setId(i);
                }
                for (int i = 0; i < defaultImageEvents.length; i++) {
                    EventType e = site.addNewEvent();
                    e.setInvalid(defaultImageEvents[i].isInvalid());
                    e.setName(defaultImageEvents[i].getName());
                }
                site.setVersion(DATA_VERSION);
                XmlOptions ops = new XmlOptions();
                ops.setSavePrettyPrint().setSavePrettyPrintIndent(4);
                Debug.print(getClass(), "save(): Calling XMLBeans save.");
                s.save(file, ops);
            }
            Category[] cats = site.getCategoryArray();
            if (site.getVersion() < 2 && cats.length == defaultCounts.length) {
                int totalCats = cats.length;
                for (int i = 0; i < totalCats; i++) {
                    site.removeCategory(0);
                }
                for (int i = 0; i < defaultCounts.length; i++) {
                    Category cat = site.addNewCategory();
                    cat.setTitle(defaultCounts[i]);
                    cat.setId(i);
                }
            }
            lb.increment();
            Debug.print(getClass(), "save(): Days to XML");
            Iterator<Day> d = days.iterator();
            int currentDayID = 0;
            while (d.hasNext()) {
                Debug.print("save(): Day (" + currentDayID + ")");
                Day vday = d.next();
                DateNode[] xmlDays = site.getDayArray();
                DateNode currentDay = null;
                for (int i = 0; i < xmlDays.length; i++) {
                    Date xDate = Conversions.getDateFormat().parse(xmlDays[i].getDatestamp());
                    if (xDate.equals(vday.getDate())) {
                        if (currentDay == null) currentDay = xmlDays[i]; else site.getDomNode().removeChild(xmlDays[i].getDomNode());
                    }
                }
                if (currentDay == null) {
                    Debug.print("      -- Day didn't exist in the xml.");
                    currentDay = site.addNewDay();
                    currentDay.setDatestamp(Conversions.getDateFormat().format(vday.getDate()));
                }
                Iterator<NestImage> images = vday.iterator();
                int currentImageID = 0;
                while (images.hasNext()) {
                    Debug.print("      -- image (" + currentImageID + ")");
                    NestImage image = images.next();
                    TimeNode[] xmlTime = currentDay.getTimeArray();
                    TimeNode currentTime = null;
                    for (int i = 0; i < xmlTime.length; i++) {
                        Date xd = Conversions.getTimeFormat().parse(xmlTime[i].getTimestamp());
                        Date id = image.getTimeDateTaken();
                        if (xd.equals(id)) {
                            currentTime = xmlTime[i];
                            break;
                        }
                    }
                    Debug.print("      -- checked other images.");
                    if (currentTime == null) {
                        Debug.print("      -- current time was null");
                        currentTime = currentDay.addNewTime();
                        currentTime.setTimestamp(Conversions.getTimeFormat().format(image.getTimeDateTaken()));
                    }
                    Debug.print("      -- currentTime has value");
                    if (image.getTemplate() != null) {
                        Debug.print("      -- setting template..");
                        Template t = image.getTemplate();
                        currentTime.setTemplate(t.toString());
                        Debug.print("      -- template set");
                    }
                    File f = new File(image.imageSrc);
                    currentTime.setImg(f.getName());
                    Debug.print("      -- set Image file.");
                    int currentDataID = 0;
                    Debug.print(this.getClass(), String.format("Saving Event Data for %s", currentTime.getTimestamp()));
                    for (int i = 0; i < image.eventValues.length; i++) {
                        Event e;
                        if (currentTime.getEventArray().length < i + 1) {
                            currentTime.addNewEvent();
                        }
                        e = currentTime.getEventArray()[i];
                        e.setValue(image.eventValues[i].getValue());
                        e.setId(i);
                    }
                    Iterator<NestData> nestDataIt = image.nests.iterator();
                    Debug.print(this.getClass(), String.format("Saving NestData for %s", currentTime.getTimestamp()));
                    while (nestDataIt.hasNext()) {
                        NestData data = nestDataIt.next();
                        MetaDataHolder[] xmlMeta = currentTime.getMetadataArray();
                        MetaDataHolder currentMeta = null;
                        if (currentDataID < xmlMeta.length) {
                            currentMeta = xmlMeta[currentDataID];
                        } else {
                            currentMeta = currentTime.addNewMetadata();
                            currentMeta.setNodeID(currentDataID);
                        }
                        currentMeta.setAdults(data.getAdults());
                        currentMeta.setChicks(data.getChicks());
                        currentDataID++;
                    }
                    Debug.print(this.getClass(), String.format("Saving Count Data for %s", currentTime.getTimestamp()));
                    if (currentTime.getCountsArray().length < this.countNames.size()) {
                        int start = currentTime.getCountsArray().length;
                        int end = this.countNames.size();
                        Debug.print("Counts are different Sizes, in XML: " + start + " in Array: " + end);
                        for (int i = start; end != start && i < end; i++) {
                            CountType c = currentTime.addNewCounts();
                        }
                    }
                    Debug.print(this.getClass(), String.format("Saving Count Names for %s", currentTime.getTimestamp()));
                    for (int i = 0; i < this.countNames.size(); i++) {
                        Debug.print("-- Count Names" + countNames.get(i).toString());
                        Iterator<Point> dataIt = image.getCount(i).iterator();
                        CountType currentCount = currentTime.getCountsArray(i);
                        if (currentCount == null) {
                            currentCount = currentTime.addNewCounts();
                        } else {
                            XYValueNode[] chicks = currentCount.getNodeArray();
                            for (int j = 0; j < chicks.length; j++) {
                                currentCount.removeNode(0);
                            }
                        }
                        currentCount.setCounted(image.isCountCounted(i));
                        currentCount.setCatid(i);
                        while (dataIt.hasNext()) {
                            Point p = dataIt.next();
                            XYValueNode newPoint = currentTime.getCountsArray(i).addNewNode();
                            newPoint.setX((int) p.getX());
                            newPoint.setY((int) p.getY());
                        }
                    }
                    currentTime.setComment(image.getComment());
                    if (image.isChanged()) currentTime.setUsername(System.getProperty("user.name"));
                    currentImageID++;
                    image.setChanged(false);
                }
                lb.increment();
                currentDayID++;
            }
            Debug.print(getClass(), "save(): Fixing bookmarks.");
            int maxBookmarks = site.getBookmarkArray().length;
            for (int i = 0; i < maxBookmarks; i++) {
                site.removeBookmark(0);
            }
            for (int i = 0; i < bookmarks.size(); i++) {
                org.penguinuri.penguin.Bookmark bmark = site.addNewBookmark();
                Bookmark b = bookmarks.get(i);
                bmark.setTitle(b.getTitle());
                bmark.setDate(b.getDateString());
                bmark.setTime(b.getTimeString());
            }
            int maxEventTypes = site.getEventArray().length;
            for (int i = 0; i < maxEventTypes; i++) {
                site.removeEvent(0);
            }
            for (int i = 0; i < events.size(); i++) {
                EventType event = site.addNewEvent();
                ImageEvent iEvent = events.get(i);
                event.setId(i);
                event.setName(iEvent.getName());
                event.setInvalid(iEvent.isInvalid());
            }
            site.setVersion(DATA_VERSION);
            XmlOptions ops = new XmlOptions();
            ops.setSavePrettyPrint().setSavePrettyPrintIndent(4);
            Debug.print(getClass(), "save(): Calling XMLBeans save.");
            s.save(file, ops);
            Debug.print(getClass(), "save(): Finished saving metadata to XML.");
            Debug.print(getClass(), "save(): Saving templates.");
            lb.setTitle("Saving templates");
            Iterator<Template> ti = templates.values().iterator();
            while (ti.hasNext()) {
                ti.next().save();
            }
            lb.increment();
            Debug.print("[site] save(): Templates saved.");
        } catch (Exception ex) {
            Debug.print(ex);
            return false;
        }
        return true;
    }

    /**
	 * Load the specific Site into memory, given that season and site have been variables have been declared. 
	 * @return
	 * @throws Exception
	 */
    public boolean load() throws Exception {
        return load(xmlLocation + "/" + "PenguinData.xml", true);
    }

    public boolean load(boolean loadbar) throws Exception {
        return load(xmlLocation + "/" + "PenguinData.xml", loadbar);
    }

    /**
	 * Load a site from XML.
	 * 
	 * @param location
	 *            the location of the XML to load the site.
	 * @return
	 */
    private boolean load(String location, boolean loadbar) throws Exception {
        countNames.clear();
        events.clear();
        bookmarks.clear();
        days.clear();
        templates.clear();
        File f = new java.io.File(location);
        SiteDocument s = org.penguinuri.penguin.SiteDocument.Factory.parse(f);
        SiteDocument.Site sd = s.getSite();
        Category[] cat = sd.getCategoryArray();
        for (int i = 0; i < cat.length; i++) {
            if (sd.getVersion() < 2 && cat.length == defaultCounts.length) {
                countNames.add(defaultCounts[i]);
            } else countNames.add(cat[i].getTitle());
        }
        EventType[] ev = sd.getEventArray();
        if (ev.length > 0) {
            for (int i = 0; i < ev.length; i++) {
                ImageEvent event = new ImageEvent(ev[i].getName(), ev[i].getInvalid());
                events.add(event);
            }
        } else {
            for (int i = 0; i < defaultImageEvents.length; i++) {
                events.add(defaultImageEvents[i]);
            }
        }
        DateNode[] dn = sd.getDayArray();
        LoadingBar lb = new LoadingBar(dn.length);
        lb.setVisible(loadbar);
        try {
            File templateFolder = new java.io.File(xmlLocation + "//templates");
            File[] temps = templateFolder.listFiles();
            for (int i = 0; i < temps.length; i++) {
                if (temps[i].isFile()) {
                    getTemplate(temps[i].getName());
                }
            }
        } catch (Exception ex) {
            Debug.print(ex);
        }
        try {
            String previousDate = "";
            for (int i = 0; i < dn.length; i++) {
                Day d = new Day(Conversions.getDateFormat().parse(dn[i].getDatestamp()), this);
                if (previousDate.equals(dn[i].getDatestamp())) {
                    sd.getDomNode().removeChild(dn[i].getDomNode());
                    lb.increment();
                    continue;
                }
                previousDate = dn[i].getDatestamp();
                addDay(d);
                TimeNode[] nestImages = dn[i].getTimeArray();
                for (int x = 0; x < nestImages.length; x++) {
                    java.util.Date date = Conversions.getTimeFormat().parse(nestImages[x].getTimestamp());
                    TimeNode image = nestImages[x];
                    EventValue[] eValues = new EventValue[events.size()];
                    Event[] xValues = image.getEventArray();
                    for (int j = 0; j < events.size(); j++) {
                        if (j < xValues.length) {
                            eValues[j] = new EventValue(j, xValues[j].getValue());
                        } else {
                            eValues[j] = new EventValue(j, false);
                        }
                    }
                    if (sd.getVersion() < 3) {
                        eValues[0].setValue(!image.getValid());
                    }
                    NestImage ni = new NestImage(date, String.format("%s/%s", f.getParent(), image.getImg()), getTemplate(image.getTemplate()), eValues, cat.length, image.getUsername());
                    MetaDataHolder[] data = image.getMetadataArray();
                    for (int y = 0; y < data.length; y++) {
                        ni.addDataToNest(y, data[y].getAdults(), data[y].getChicks());
                    }
                    CountType[] count = image.getCountsArray();
                    for (int j = 0; j < count.length; j++) {
                        XYValueNode[] penguin = count[j].getNodeArray();
                        for (int k = 0; k < penguin.length; k++) {
                            XYValueNode current = penguin[k];
                            ni.addPoint(j, new Point(current.getX(), current.getY()));
                        }
                        ni.setCountAsCounted(j, count[j].getCounted());
                    }
                    d.addImage(ni);
                    ni.setComment(image.getComment());
                    ni.setChanged(false);
                }
                lb.increment();
            }
            if (sd.getBookmarkArray().length <= 0) {
                for (int i = 0; i < defaultBookmarkTitles.length; i++) {
                    org.penguinuri.penguin.Bookmark bmark = sd.addNewBookmark();
                    bmark.setTitle(defaultBookmarkTitles[i]);
                    bmark.setDate("");
                    bmark.setTime("");
                }
            }
            org.penguinuri.penguin.Bookmark[] BArray = sd.getBookmarkArray();
            for (int i = 0; i < BArray.length; i++) {
                org.penguinuri.penguin.Bookmark bmark = BArray[i];
                int day = bmark.getDayIndex(), time = bmark.getTimeIndex();
                String sDay = bmark.getDate(), sTime = bmark.getTime();
                if (((sDay == null || sDay.equals("")) || (sTime == null || sTime.equals(""))) && (day < 0 || time < 0)) {
                    addBookmark(new Bookmark(bmark.getTitle()));
                } else {
                    if ((sDay != null && !sDay.equals("")) && (sTime != null && !sTime.equals(""))) {
                        addBookmark(new Bookmark(this, bmark.getTitle(), sDay, sTime));
                    } else {
                        String imageTime = Conversions.getDateFormat().format(this.getDay(day).getImage(time).getTimeDateTaken());
                        String dayDate = Conversions.getDateFormat().format(this.getDay(day).getDate());
                        addBookmark(new Bookmark(bmark.getTitle(), dayDate, imageTime));
                    }
                }
            }
        } catch (Exception ex) {
            lb.setVisible(false);
            throw ex;
        }
        isChanged = false;
        return true;
    }

    /**
	 * Returns the names of the various counts.
	 * @return
	 */
    public LinkedList<String> getCountNames() {
        return countNames;
    }

    /**
	 * Print a debug of the site to the Console.
	 */
    public void debugPrint() {
        Debug.print("Site of " + siteName);
        Iterator<Day> dayIt = iterator();
        int i = 0;
        while (dayIt.hasNext()) {
            Day d = dayIt.next();
            Debug.print("   Day: " + i);
            Iterator<NestImage> imageIt = d.iterator();
            int imgCount = 0;
            while (imageIt.hasNext()) {
                NestImage img = imageIt.next();
                Debug.print("    Image: " + img.getTimeDateTaken() + " - " + imgCount);
                imgCount++;
            }
            i++;
        }
    }

    public void setChanged() {
        isChanged = true;
    }

    public boolean hasChanged() {
        boolean hasChanged = false;
        Iterator<Day> d = this.days.iterator();
        while (d.hasNext() && !hasChanged && !isChanged) {
            Iterator<NestImage> n = d.next().iterator();
            while (n.hasNext() && !hasChanged) {
                hasChanged = hasChanged || n.next().isChanged();
            }
        }
        return hasChanged || isChanged;
    }

    public Template getMainTemplate() {
        if (!hasMainTemplate) {
            Iterator<Day> dayIt = days.iterator();
            while (dayIt.hasNext()) {
                Iterator<NestImage> nestIt = dayIt.next().iterator();
                while (nestIt.hasNext()) {
                    Template t = nestIt.next().getTemplate();
                    if (t != null) {
                        mainTemplate = t;
                        return t;
                    }
                }
            }
            hasMainTemplate = true;
        }
        return mainTemplate;
    }
}
