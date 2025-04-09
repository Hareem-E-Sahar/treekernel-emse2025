package com.uebbing.atleto.data;

import java.util.*;
import org.jdom.*;
import com.uebbing.atleto.util.MiscUtilities;

public class Diary {

    private Vector<Day> days = new Vector<Day>();

    private Vector<Equipment> equipmentList = new Vector<Equipment>();

    /**
    * Default Constructor
    */
    public Diary() {
    }

    /**
    * Populate the Diary object using a JDOM Element
    * @param diary element of an atleto data file
    */
    public void setContents(Element dom) {
        int numdays = 0;
        int count = 0;
        try {
            numdays = dom.getAttribute("numdays").getIntValue();
        } catch (DataConversionException de) {
            de.printStackTrace();
        }
        System.out.println("number of days: " + String.valueOf(numdays));
        List days = dom.getChildren("Day");
        Iterator iter = days.iterator();
        while (iter.hasNext()) {
            Day day = new Day();
            day.setContents((Element) iter.next());
            addDay(day);
            count++;
        }
        List surfaces = dom.getChildren("Surface");
        iter = surfaces.iterator();
        while (iter.hasNext()) {
            Element s = (Element) iter.next();
            WorkoutFactory.setValues(s.getAttribute("type").getValue(), "addSurface", s.getText());
        }
        List kows = dom.getChildren("KindOfWorkout");
        iter = kows.iterator();
        while (iter.hasNext()) {
            Element k = (Element) iter.next();
            WorkoutFactory.setValues(k.getAttribute("type").getValue(), "addKOW", k.getText());
        }
        List all_equip = dom.getChildren("Equipment");
        iter = all_equip.iterator();
        while (iter.hasNext()) {
            Element edom = (Element) iter.next();
            Equipment e = WorkoutFactory.buildEquipment(edom.getAttribute("type").getValue(), edom.getAttribute("wtype").getValue());
            if (e != null) {
                e.setContents(edom);
                equipmentList.add(e);
            }
        }
    }

    public List getEquipment() {
        return (List) equipmentList;
    }

    /**
    * Retrieve day by id
    * @return day object
    */
    public Day day(int id) {
        return dayByIndex(findDayIndex(id));
    }

    /**
    * Retrieve day by calendar date
    * @param day object
    */
    public Day day(GregorianCalendar cal) {
        return day(MiscUtilities.cal2Id(cal));
    }

    /**
    * Retrieve day by vector index
    * @param day object
    */
    public Day dayByIndex(int index) {
        if (index < 0 || index >= numDays()) return null;
        return (Day) days.get(index);
    }

    /**
    * Inserts a day object to the list. Note that the list is ordered chronologically.
    * If the day cannot be inserted in the list, it will be appended to the end.
    * @param Day object to add
    */
    public void addDay(Day day) {
        boolean inserted = false;
        for (int i = 0; i < numDays(); i++) {
            Day d = dayByIndex(i);
            if (day.getId() < d.getId()) {
                days.add(i, day);
                inserted = true;
                break;
            }
        }
        if (!inserted) days.add(day);
    }

    /**
    * This method determines the index of a specific day gieven by id.
    * It should be fairly fast as it uses a binary search.
    * @param id
    * @return index of this day
    */
    public int findDayIndex(int id) {
        Day d;
        int i, high, low;
        for (low = -1, high = numDays(); high - low > 1; ) {
            i = (high + low) / 2;
            d = dayByIndex(i);
            if (id <= d.getId()) high = i; else low = i;
        }
        d = dayByIndex(high);
        if (d == null) return -1; else if (id != d.getId()) return -1;
        return high;
    }

    /**
    * Returns the number of days in the list
    * @return number of days
    */
    public int numDays() {
        return days.size();
    }

    /**
    * Remove a specific day out of the list. All information about the day will
    * be deleted !
    * @param id of the day
    */
    public void removeDay(int id) {
        removeDayByIndex(findDayIndex(id));
    }

    /**
    * Remove a specific day out of the list. All information about the day will
    * be deleted !
    * @param calendar date of the day
    */
    public void removeDay(GregorianCalendar cal) {
        removeDayByIndex(findDayIndex(MiscUtilities.cal2Id(cal)));
    }

    /**
    * Remove a specific day out of the list. All information about the day will
    * be deleted !
    * @param vector index of the day
    */
    public void removeDayByIndex(int index) {
        days.remove(index);
    }

    public List subListOfDays(GregorianCalendar start, GregorianCalendar end) {
        int firstDay = findDayIndex(MiscUtilities.cal2Id(start));
        int lastDay = findDayIndex(MiscUtilities.cal2Id(end));
        while (firstDay < 0) {
            start.add(Calendar.DAY_OF_YEAR, 1);
            firstDay = findDayIndex(MiscUtilities.cal2Id(start));
            if (start.get(Calendar.DATE) == 31 && start.get(Calendar.MONTH) == Calendar.DECEMBER) break;
        }
        while (lastDay < 0) {
            end.add(Calendar.DAY_OF_YEAR, -1);
            lastDay = findDayIndex(MiscUtilities.cal2Id(end));
            if (end.get(Calendar.DATE) == 1 && end.get(Calendar.MONTH) == Calendar.JANUARY) break;
        }
        return days.subList(firstDay, lastDay + 1);
    }

    /**
    * Creates a string representation of this object formatted in xml.
    * @return xml representation of this Workout
    * @see #toXML( int tabs)
    */
    public String toString() {
        return toXML(0);
    }

    /**
    * Creates an xml representation of this object indented by tabs.
    * Must be implemented by derived class.
    * @param number of tabs to indent
    * @return xml representation of this workout
    * @see #toString()
    */
    public String toXML(int tabs) {
        StringBuffer sb = new StringBuffer();
        StringBuffer tsb = new StringBuffer();
        for (int i = 0; i < tabs; i++) tsb.append("  ");
        sb.append(tsb.toString() + "<Diary numdays=\"" + numDays() + "\">\n");
        sb.append(tsb.toString() + "  " + WorkoutFactory.valuesToXML("Run", "surfaceToXML"));
        sb.append(tsb.toString() + "  " + WorkoutFactory.valuesToXML("Run", "kowToXML"));
        sb.append(tsb.toString() + "  " + WorkoutFactory.valuesToXML("Cycle", "surfaceToXML"));
        sb.append(tsb.toString() + "  " + WorkoutFactory.valuesToXML("Cycle", "kowToXML"));
        for (int i = 0; i < equipmentList.size(); i++) sb.append(((Equipment) equipmentList.get(i)).toXML(tabs + 1));
        for (int i = 0; i < numDays(); i++) sb.append(((Day) days.get(i)).toXML(tabs + 1));
        sb.append(tsb.toString() + "</Diary>\n");
        return sb.toString();
    }
}
