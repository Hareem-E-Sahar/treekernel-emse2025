package at.rc.tacos.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.util.comparator.CompoundComparator;
import at.rc.tacos.platform.iface.IFilterTypes;
import at.rc.tacos.platform.model.AbstractMessage;
import at.rc.tacos.platform.model.Location;
import at.rc.tacos.platform.model.QueryFilter;
import at.rc.tacos.platform.model.RosterEntry;
import at.rc.tacos.web.container.RosterEntryContainer;
import at.rc.tacos.web.container.RosterEntryContainerListContainer;
import at.rc.tacos.web.net.WebClient;
import at.rc.tacos.web.session.UserSession;

/**
 * Roster Controller.
 * @author Payer Martin
 * @version 1.0
 */
public class RosterController extends Controller {

    private static final String PARAM_CURRENT_DATE_NAME = "currentDate";

    private static final String PARAM_LOCATION_NAME = "locationId";

    private static final String PARAM_LOCATION_NO_VALUE = "noValue";

    private static final String MODEL_LOCATION_NAME = "location";

    private static final String MODEL_LOCATION_LIST_NAME = "locationList";

    private static final String MODEL_CALENDAR_DEFAULT_DATE_MILLISECONDS_NAME = "calendarDefaultDateMilliseconds";

    private static final String MODEL_CALENDAR_RANGE_START_NAME = "calendarRangeStart";

    private static final String MODEL_CALENDAR_RANGE_END_NAME = "calendarRangeEnd";

    private static final int MODEL_CALENDAR_RANGE_START_OFFSET = 10;

    private static final int MODEL_CALENDAR_RANGE_END_OFFSET = 1;

    private static final String PARAM_DATE_NAME = "date";

    private static final String MODEL_DATE_NAME = "date";

    private static final String MODEL_DATE_DAY_OF_YEAR_NAME = "dateDayOfYear";

    private static final String MODEL_DATE_MONTH_NAME = "dateMonth";

    private static final String MODEL_DATE_YEAR_NAME = "dateYear";

    private static final String MODEL_ROSTER_ENTRY_CONTAINER_LIST_CONTAINER = "rosterEntryContainerListContainer";

    private static final String PARAM_MESSAGE_CODE_NAME = "messageCode";

    private static final String MODEL_MESSAGE_CODE_NAME = "messageCode";

    @Override
    public Map<String, Object> handleRequest(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws Exception {
        final Map<String, Object> params = new HashMap<String, Object>();
        final UserSession userSession = (UserSession) request.getSession().getAttribute("userSession");
        final WebClient connection = userSession.getConnection();
        params.put(PARAM_CURRENT_DATE_NAME, new Date());
        final String paramLocationId = request.getParameter(PARAM_LOCATION_NAME);
        int locationId = 0;
        Location location = userSession.getDefaultFormValues().getDefaultLocation();
        if (paramLocationId != null && !paramLocationId.equals("")) {
            if (paramLocationId.equalsIgnoreCase(PARAM_LOCATION_NO_VALUE)) {
                location = null;
            } else {
                locationId = Integer.parseInt(paramLocationId);
            }
        }
        final List<AbstractMessage> locationList = connection.sendListingRequest(Location.ID, null);
        if (!Location.ID.equalsIgnoreCase(connection.getContentType())) {
            throw new IllegalArgumentException("Error: Error at connection to Tacos server occoured.");
        }
        params.put(MODEL_LOCATION_LIST_NAME, locationList);
        for (final Iterator<AbstractMessage> itLoactionList = locationList.iterator(); itLoactionList.hasNext(); ) {
            final Location l = (Location) itLoactionList.next();
            if (l.getId() == locationId) {
                location = l;
            }
        }
        params.put(MODEL_LOCATION_NAME, location);
        Date date = userSession.getDefaultFormValues().getDefaultDate();
        if (date == null) {
            date = new Date();
        }
        final Calendar calendar = Calendar.getInstance();
        final int rangeStart = calendar.get(Calendar.YEAR) - MODEL_CALENDAR_RANGE_START_OFFSET;
        final int rangeEnd = calendar.get(Calendar.YEAR) + MODEL_CALENDAR_RANGE_END_OFFSET;
        params.put(MODEL_CALENDAR_DEFAULT_DATE_MILLISECONDS_NAME, date.getTime());
        params.put(MODEL_CALENDAR_RANGE_START_NAME, rangeStart);
        params.put(MODEL_CALENDAR_RANGE_END_NAME, rangeEnd);
        final Calendar rangeStartCalendar = Calendar.getInstance();
        rangeStartCalendar.set(Calendar.YEAR, rangeStartCalendar.get(Calendar.YEAR) - MODEL_CALENDAR_RANGE_START_OFFSET);
        final Calendar rangeEndCalendar = Calendar.getInstance();
        rangeEndCalendar.set(Calendar.YEAR, rangeEndCalendar.get(Calendar.YEAR) + MODEL_CALENDAR_RANGE_END_OFFSET);
        final String paramDate = request.getParameter(PARAM_DATE_NAME);
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        final SimpleDateFormat formatDateForServer = new SimpleDateFormat("dd-MM-yyyy");
        Date dateTemp = null;
        if (paramDate != null) {
            try {
                dateTemp = df.parse(paramDate);
            } catch (ParseException e) {
            }
            if (dateTemp != null && dateTemp.getTime() < rangeStartCalendar.getTimeInMillis() || dateTemp.getTime() > rangeEndCalendar.getTimeInMillis()) {
            } else {
                date = dateTemp;
            }
        }
        params.put(MODEL_DATE_NAME, date);
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        params.put(MODEL_DATE_DAY_OF_YEAR_NAME, c.get(Calendar.DAY_OF_YEAR));
        params.put(MODEL_DATE_MONTH_NAME, c.get(Calendar.MONTH));
        params.put(MODEL_DATE_YEAR_NAME, c.get(Calendar.YEAR));
        final String dateForServerString = formatDateForServer.format(date);
        final QueryFilter rosterFilter = new QueryFilter();
        rosterFilter.add(IFilterTypes.DATE_FILTER, dateForServerString);
        if (location != null) {
            rosterFilter.add(IFilterTypes.ROSTER_LOCATION_FILTER, Integer.toString(location.getId()));
        }
        final List<AbstractMessage> rosterEntryList = connection.sendListingRequest(RosterEntry.ID, rosterFilter);
        final List<RosterEntryContainer> rosterEntryContainerList = new ArrayList<RosterEntryContainer>();
        if (!RosterEntry.ID.equalsIgnoreCase(connection.getContentType())) {
            throw new IllegalArgumentException("Error: Error at connection to Tacos server occoured.");
        }
        for (Iterator<AbstractMessage> itRosterEntryList = rosterEntryList.iterator(); itRosterEntryList.hasNext(); ) {
            final RosterEntry rosterEntry = (RosterEntry) itRosterEntryList.next();
            final RosterEntryContainer rosterEntryContainer = new RosterEntryContainer();
            rosterEntryContainer.setRosterEntry(rosterEntry);
            final Calendar deadlineCalendar = Calendar.getInstance();
            deadlineCalendar.setTime(rosterEntryContainer.getPlannedStartOfWork());
            deadlineCalendar.set(Calendar.HOUR, deadlineCalendar.get(Calendar.HOUR) - RosterEntryContainer.EDIT_ROSTER_ENTRY_DEADLINE_HOURS);
            rosterEntryContainer.setDeadline(deadlineCalendar.getTime());
            final Calendar registerStartCalendar = Calendar.getInstance();
            registerStartCalendar.setTime(rosterEntryContainer.getPlannedStartOfWork());
            registerStartCalendar.set(Calendar.HOUR, registerStartCalendar.get(Calendar.HOUR) - RosterEntryContainer.REGISTER_ROSTER_ENTRY_DEADLINE_HOURS);
            rosterEntryContainer.setRegisterStart(registerStartCalendar.getTime());
            rosterEntryContainerList.add(rosterEntryContainer);
        }
        final RosterEntryContainerListContainer container = new RosterEntryContainerListContainer(rosterEntryContainerList);
        final Comparator<Location> locationComparator = new PropertyComparator("locationName", true, true);
        final Comparator sortComp = new CompoundComparator(new Comparator[] { new PropertyComparator("plannedStartOfWork", true, true), new PropertyComparator("rosterEntry.staffMember.lastName", true, true), new PropertyComparator("rosterEntry.staffMember.firstName", true, true) });
        container.groupRosterEntriesBy(locationComparator);
        container.sortRosterEntries(sortComp);
        params.put(MODEL_ROSTER_ENTRY_CONTAINER_LIST_CONTAINER, container);
        if (request.getParameter(PARAM_MESSAGE_CODE_NAME) != null && !request.getParameter(PARAM_MESSAGE_CODE_NAME).equals("")) {
            params.put(MODEL_MESSAGE_CODE_NAME, request.getParameter(PARAM_MESSAGE_CODE_NAME));
        }
        userSession.getDefaultFormValues().setDefaultLocation(location);
        userSession.getDefaultFormValues().setDefaultDate(date);
        return params;
    }
}
