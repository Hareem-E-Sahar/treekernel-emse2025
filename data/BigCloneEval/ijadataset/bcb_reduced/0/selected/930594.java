package subsystem;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Component;
import observer.Observer;
import observer.Subject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.lowagie.text.Cell;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This is the interface class for the 'process' component for the program. It 
 * provides an interface for the world, and kind of encapsulates what a calendar
 * is.
 * 
 * @author Dana Burkart
 */
public class CakeCal implements Subject, Observer {

    public static final String NL = System.getProperty("line.separator");

    /**The new line seperator.  System-dependent*/
    public static final String TAB = "    ";

    /** preferred tab length*/
    public static final String UNTITLED = "New Calendar";

    /**"New Calendar"  Default name.*/
    public boolean modified;

    /**True if the calendar has been modified 
																				since it was last saved.*/
    private EventDatabase eventDatabase;

    private CalendarSettings settings;

    private ArrayList<Observer> observers;

    private int currentYear = getDate().year;

    private int currentMonth = getDate().month;

    private int currentDay = getDate().day;

    /**Main constructor.  Called unless there is an argument when Cake is invoked to open a file as a command line argument.
	 * 
	 */
    public CakeCal() {
        this.init();
    }

    /**
	 * Constructor for CakeCal.  Called when the title of a calendar has been provided when Cake in invoked.
	 * 
	 * @param args The arguments.  
	 */
    public CakeCal(String args[]) {
        this.init();
        if (args.length == 1) {
            this.loadCal(args[0], false);
            settings.setName(new String(args[0]));
        }
    }

    /**Called by the constructors to do common tasks.
	 * 
	 */
    private void init() {
        settings = new CalendarSettings();
        eventDatabase = new EventDatabase(this);
        settings.setName(UNTITLED);
        settings.setDateCreated(CakeCal.getDate());
        modified = false;
        observers = new ArrayList<Observer>();
        eventDatabase.attachObserver(this);
    }

    /**Return the settings used by this calendar.
	 * 
	 * @return The settings class used by this calendar.
	 */
    public CalendarSettings getSettings() {
        return settings;
    }

    /**Set the settings used by this calendar.
	 * 
	 * @param newSettings The new settings to be used for this calendar.
	 */
    public void setSettings(CalendarSettings newSettings) {
        settings = newSettings;
        this.updateData();
    }

    /**Load a calendar from the specified document
	 * 
	 * @param document The document to load from
	 * @param merge True if we are merging calendars, false otherwise
	 * 
	 * @return True if the loading was successful, false otherwise.
	 */
    private boolean loadCal(Document document, boolean merge) {
        try {
            document.getDocumentElement().normalize();
            NodeList settingsNodes = document.getElementsByTagName("settings");
            if (!merge) {
                for (int i = 0; i < settingsNodes.getLength(); i++) {
                    CalendarSettings temp = new CalendarSettings();
                    Element element = (Element) settingsNodes.item(i);
                    Element nameE = (Element) element.getElementsByTagName("name").item(0);
                    NodeList name = nameE.getChildNodes();
                    temp.setName(name.item(0).getNodeValue());
                    Element datecreatedE = (Element) element.getElementsByTagName("datecreated").item(0);
                    NodeList datecreated = datecreatedE.getChildNodes();
                    temp.setDateCreated(SimpleDate.parse(datecreated.item(0).getNodeValue()));
                    Element ownerE = (Element) element.getElementsByTagName("owner").item(0);
                    NodeList owner = ownerE.getChildNodes();
                    if (owner.getLength() > 0) temp.setOwner(owner.item(0).getNodeValue()); else temp.setOwner("");
                    settings = temp;
                }
            }
            NodeList eventNodes = document.getElementsByTagName("event");
            for (int i = 0; i < eventNodes.getLength(); i++) {
                String tempTitle;
                String tempDesc;
                String tempRecu;
                String tempLoc;
                Period tempPd;
                Element element = (Element) eventNodes.item(i);
                Element titleE = (Element) element.getElementsByTagName("title").item(0);
                NodeList title = titleE.getChildNodes();
                tempTitle = title.item(0).getNodeValue();
                Element descE = (Element) element.getElementsByTagName("desc").item(0);
                NodeList desc = descE.getChildNodes();
                if (desc.getLength() > 0) tempDesc = desc.item(0).getNodeValue(); else tempDesc = "";
                Element recuE = (Element) element.getElementsByTagName("recu").item(0);
                NodeList recu = recuE.getChildNodes();
                if (recu.getLength() > 0) tempRecu = recu.item(0).getNodeValue(); else tempRecu = "0";
                Element locationE = (Element) element.getElementsByTagName("location").item(0);
                NodeList location = locationE.getChildNodes();
                if (location.getLength() > 0) tempLoc = location.item(0).getNodeValue(); else tempLoc = "";
                Element periodE = (Element) element.getElementsByTagName("period").item(0);
                NodeList period = periodE.getChildNodes();
                tempPd = Period.parse(period.item(0).getNodeValue());
                eventDatabase.loadEvent(new Event(tempPd, tempTitle, tempDesc, tempRecu, tempLoc));
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            return false;
        }
        return true;
    }

    /**Load a calendar from a specified input stream
	 * 
	 * @param is The input stream to load from
	 * @return True if succesful, false otherwise.
	 */
    public boolean loadCal(InputStream is) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(is);
            return loadCal(document, false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
	 * Loads a calendar into this CakeCal.
	 * 
	 * @param filename name of the file to load
	 * @param merge true if we are merging calendars, false otherwise.
	 * 
	 * @return true if successful, false otherwise
	 */
    public boolean loadCal(String filename, boolean merge) {
        try {
            File file = new File(filename);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            return loadCal(document, merge);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**Read an .ics calendar file from the given filename
	 * 
	 * @param filename The filename to open
	 * @return true if succesful, false otherwise
	 */
    public boolean loadICS(String filename) {
        File f = new File(filename);
        try {
            return loadICS(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**Read an .ics calendar file
	 * 
	 * @param fin The input stream to read the file from
	 * @return true if successful, false otherwise.
	 */
    @SuppressWarnings("unchecked")
    private boolean loadICS(InputStream fin) {
        try {
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar calendar = builder.build(fin);
            for (Iterator i = calendar.getComponents().iterator(); i.hasNext(); ) {
                Component component = (Component) i.next();
                if (component.getName().equals("VEVENT")) {
                    String start = component.getProperty("DTSTART").getValue();
                    String end = component.getProperty("DTEND").getValue();
                    int sYMD = 0;
                    int eYMD = 0;
                    try {
                        sYMD = Integer.parseInt(start.substring(0, 8));
                        eYMD = Integer.parseInt(end.substring(0, 8));
                    } catch (NumberFormatException e) {
                        System.err.println("Malformed iCalendar file");
                    }
                    SimpleDate startDate = new SimpleDate();
                    startDate.day = sYMD % 100;
                    startDate.year = sYMD / 10000;
                    startDate.month = (sYMD / 100) % 100;
                    SimpleDate endDate = new SimpleDate();
                    endDate.day = eYMD % 100;
                    endDate.year = eYMD / 10000;
                    endDate.month = (eYMD / 100) % 100;
                    SimpleTime startTime = new SimpleTime(0, 0);
                    SimpleTime endTime = new SimpleTime(24, 00);
                    if (start.indexOf("T") != -1) {
                        String sTime = start.substring(start.indexOf("T"));
                        String eTime = end.substring(end.indexOf("T"));
                        try {
                            startTime.hour = Integer.parseInt(sTime.substring(1, 3));
                            startTime.minutes = Integer.parseInt(sTime.substring(3, 5));
                            endTime.hour = Integer.parseInt(eTime.substring(1, 3));
                            endTime.minutes = Integer.parseInt(eTime.substring(3, 5));
                        } catch (NumberFormatException e) {
                            System.err.println("Malformed ICS file");
                        }
                    }
                    Period tempPd = new Period(new SimpleDateTime(startDate, startTime), new SimpleDateTime(endDate, endTime));
                    String tmpSummary = component.getProperty("SUMMARY") == null ? "" : component.getProperty("SUMMARY").getValue();
                    String tmpDescription = component.getProperty("DESCRIPTION") == null ? "" : component.getProperty("DESCRIPTION").getValue();
                    String tmpLocation = component.getProperty("LOCATION") == null ? "" : component.getProperty("LOCATION").getValue();
                    eventDatabase.loadEvent(new Event(tempPd, tmpSummary, tmpDescription, "0", tmpLocation));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
	 * Saves a calendar to a file.
	 * 
	 * @param filename name of the file to save to
	 * @return whether or not the save was successful
	 */
    public boolean saveCal(String filename) throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + NL;
        xml += "<calendar>" + NL + settings.toXML() + eventDatabase.toXML() + "</calendar>";
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            filename = filename.concat(".cml");
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write(xml);
        out.close();
        modified = false;
        return true;
    }

    /**
	 * Returns the day of a week a date occurs on. Does this in O(c), which helps
	 * the calendar render speedily.
	 * 
	 * @param d the date to get the weekday of
	 * @return the day of the week
	 */
    public static int getDayOfWeek(SimpleDate d) {
        return Cakeday.getDayOfWeek(d);
    }

    /**
	 * Returns an array of months for a given year.
	 * 
	 * @param year which year to get months for
	 * @return array of months for the year 
	 */
    public static int[] getMonths(int year) {
        return Cakeday.getMonths(year);
    }

    /**
	 * Returns a SimpleDate object containing the current date.
	 * 
	 * @return SimpleDate of the current date.
	 */
    public static SimpleDate getDate() {
        String df = "yyyy.MM.dd";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(df);
        return SimpleDate.parse(sdf.format(cal.getTime()));
    }

    /**
	 * Returns a list of events within a certain period.
	 * 
	 * @param p the period to use
	 * @return an ArrayList containing the events within that period
	 */
    public ArrayList<Event> getEvents(Period p) {
        return eventDatabase.getEvents(p);
    }

    /**
	 * Adds an event to the event database. 
	 * 
	 * @param e the event to add to the database
	 */
    public void addEvent(Event e) {
        modified = true;
        eventDatabase.addEvent(e);
    }

    /**
	 * Updates an event in the EventDatabase, and returns the event updated,
	 * (with has the updated UID)
	 * 
	 * @param e event to update
	 */
    public void updateEvent(Event e) {
        eventDatabase.updateEvent(e);
    }

    /**
	 * Deletes an equivalent event from the event database.
	 * 
	 * @param e event to delete
	 * @return whether or not it was successful
	 */
    public boolean deleteEvent(Event e) {
        return eventDatabase.deleteEvent(e);
    }

    /**defined by subject
	 * 
	 * @param o
	 */
    public void attachObserver(Observer o) {
        observers.add(o);
    }

    /**Defined by subject
	 * 
	 */
    public void notifyObservers() {
        Iterator<Observer> i = observers.iterator();
        while (i.hasNext()) {
            Observer o = i.next();
            o.updateData();
        }
    }

    /**defined by observer
	 * 
	 */
    public void updateData() {
        this.modified = true;
        this.notifyObservers();
    }

    public void setCurrentYear(int currentYear) {
        this.currentYear = currentYear;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentMonth(int currentMonth) {
        this.currentMonth = currentMonth;
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    /**Export this calendar as a PDF file.  This is just essentially a flat-file dump,
	 * in this format:
	 * 
	 * +--------------------------------------------------+
	 * | Date       Time    Description                   |
	 * | <date>    <time>   <desc> @ <location>           |
	 * |           <time2>  <desc> @ <location>           |
	 * | <date2>   <time>   <desc> @ <location>           |
	 * +--------------------------------------------------+
	 * 
	 * etc...
	 * 
	 * @param p The period to get the events for that the PDF is generated from
	 * @param filename The filename that the user would like to save as.
	 */
    public void exportAsPDF(Period p, String filename, Rectangle pageSize) {
        ArrayList<Event> events = new ArrayList<Event>();
        ArrayList<Period> thePeriods = Period.splitIntoDays(p);
        for (Period day : thePeriods) {
            events.addAll(eventDatabase.getEvents(day));
        }
        try {
            com.lowagie.text.Document document = new com.lowagie.text.Document(pageSize, 0, 0, 20, 20);
            @SuppressWarnings("unused") PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
            Cell dateCell = new Cell("Date");
            dateCell.setHeader(true);
            dateCell.setBorder(0001);
            Cell timeCell = new Cell("Time");
            timeCell.setHeader(true);
            timeCell.setBorder(0001);
            Cell event = new Cell("Event");
            event.setHeader(true);
            event.setBorder(0001);
            Table theTable = new Table(3, events.size() + 1);
            theTable.setBorderColor(Color.WHITE);
            theTable.setWidths(new int[] { 10, 20, 70 });
            theTable.addCell(dateCell);
            theTable.addCell(timeCell);
            theTable.addCell(event);
            theTable.endHeaders();
            int month = -1;
            int day = -1;
            for (Event e : events) {
                Period pd = e.getPeriod();
                String date = pd.start.date.month + "/" + pd.start.date.day;
                if (month == pd.start.date.month && day == pd.start.date.day) {
                    date = "";
                }
                month = pd.start.date.month;
                day = pd.start.date.day;
                String time = pd.start.time.toString() + "-\n" + pd.end.time.toString();
                String desc = e.getDescription();
                String loc = e.getLocation();
                String descAndLoc;
                boolean atSymbolReq = true;
                boolean colonReq = true;
                if (loc.isEmpty()) {
                    atSymbolReq = false;
                }
                if (desc.isEmpty()) {
                    colonReq = false;
                }
                if (atSymbolReq) {
                    descAndLoc = e.getTitle() + ": " + desc + " @ " + loc;
                } else if (colonReq) {
                    descAndLoc = e.getTitle() + ": " + desc;
                } else {
                    descAndLoc = e.getTitle() + desc;
                }
                Cell tempDate = new Cell(date);
                tempDate.setBorder(0000);
                Cell tempTime = new Cell(time);
                tempTime.setBorder(0000);
                Cell tempEvent = new Cell(descAndLoc);
                tempEvent.setBorder(0000);
                theTable.addCell(tempDate);
                theTable.addCell(tempTime);
                theTable.addCell(tempEvent);
            }
            document.add(theTable);
            document.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (DocumentException e1) {
            e1.printStackTrace();
        }
    }

    /**Export the calendar as a PDF, in MonthView
	 * 
	 * @param p The period containing the months to export
	 * @param filename The name to save the PDF as
	 * @param pageSize
	 */
    public void exportAsPDFMonth(Period p, String filename, Rectangle pageSize) {
        ArrayList<Period> thePeriods = Period.splitIntoMonths(p);
        try {
            com.lowagie.text.Document document = new com.lowagie.text.Document(pageSize, 0, 0, 20, 20);
            @SuppressWarnings("unused") PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
            for (Period month : thePeriods) {
                Cell monthCell = new Cell(Cakeday.MONTHS[month.start.date.month - 1]);
                monthCell.setColspan(7);
                int currentMonthOffset = CakeCal.getDayOfWeek(SimpleDate.parse(month.start.date.year + "." + month.start.date.month + "." + 1));
                int[] months = CakeCal.getMonths(month.start.date.year);
                Table theTable = new Table(7, 7);
                theTable.setBorderColor(Color.LIGHT_GRAY);
                theTable.setWidths(new int[] { 10, 10, 10, 10, 10, 10, 10 });
                theTable.addCell(monthCell);
                theTable.endHeaders();
                for (int x = 0; x < 7 * 6; x++) {
                    Cell temp = new Cell();
                    if (x < currentMonthOffset || x > months[month.start.date.month - 1]) {
                        temp.addElement(new Paragraph(""));
                    } else {
                        Phrase date = new Phrase(((x - currentMonthOffset) + 1) + "", FontFactory.getFont(FontFactory.COURIER, 12, Font.NORMAL, Color.BLUE));
                        Paragraph addToCell = new Paragraph(date);
                        ArrayList<Event> events = new ArrayList<Event>();
                        ArrayList<Period> days = Period.splitIntoDays(month);
                        for (Period theDay : days) {
                            events.addAll(eventDatabase.getEvents(theDay));
                            for (Event eventToadd : events) {
                                addToCell.add(new Paragraph("\n   " + eventToadd.getTitle()));
                            }
                            events.clear();
                        }
                        temp.addElement(addToCell);
                    }
                    theTable.addCell(temp);
                }
                document.add(theTable);
            }
            document.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (DocumentException e1) {
            e1.printStackTrace();
        }
    }
}
