package at.rc.tacos.web.controller;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import at.rc.tacos.platform.iface.IFilterTypes;
import at.rc.tacos.platform.model.AbstractMessage;
import at.rc.tacos.platform.model.Competence;
import at.rc.tacos.platform.model.Location;
import at.rc.tacos.platform.model.Login;
import at.rc.tacos.platform.model.QueryFilter;
import at.rc.tacos.platform.model.StaffMember;
import at.rc.tacos.web.net.WebClient;
import at.rc.tacos.web.session.UserSession;

/**
 * Edit Staff Member Controller
 * @author Payer Martin
 * @version 1.0
 */
public class EditStaffMemberController extends Controller {

    private static final String MODEL_TIMESTAMP_NAME = "timestamp";

    private static final String PARAM_STAFF_MEMBER_NAME = "staffMemberId";

    private static final String MODEL_STAFF_MEMBER_NAME = "staffMember";

    private static final String MODEL_STAFF_MEMBER_LIST_NAME = "staffMemberList";

    private static final String MODEL_LOGIN_NAME = "login";

    private static final String ACTION_NAME = "action";

    private static final String ACTION_UPDATE_STAFF_MEMBER = "updateStaffMember";

    private static final String PARAM_FIRSTNAME_NAME = "firstName";

    private static final String MODEL_FIRSTNAME_NAME = "firstName";

    private static final String PARAM_LASTNAME_NAME = "lastName";

    private static final String MODEL_LASTNAME_NAME = "lastName";

    private static final String PARAM_PHONE1_NAME = "phone1";

    private static final String MODEL_PHONE1_NAME = "phone1";

    private static final String PARAM_PHONE2_NAME = "phone2";

    private static final String MODEL_PHONE2_NAME = "phone2";

    private static final String MODEL_CALENDAR_DEFAULT_DATE_MILLISECONDS_NAME = "calendarDefaultDateMilliseconds";

    private static final String MODEL_CALENDAR_RANGE_START_NAME = "calendarRangeStart";

    private static final String MODEL_CALENDAR_RANGE_END_NAME = "calendarRangeEnd";

    private static final int MODEL_CALENDAR_MAX_AGE = 100;

    private static final String PARAM_BIRTHDATE_NAME = "birthDate";

    private static final String MODEL_BIRTHDATE_NAME = "birthDate";

    private static final String PARAM_SEX_NAME = "sex";

    private static final String PARAM_SEX_NO_VALUE = "noValue";

    private static final String MODEL_SEX_NAME = "sex";

    private static final String MODEL_PHOTO_PATH_NAME = "photo";

    private static final String PARAM_LOCATION_NAME = "locationId";

    private static final String PARAM_LOCATION_NO_VALUE = "noValue";

    private static final String MODEL_LOCATION_NAME = "location";

    private static final String MODEL_LOCATION_LIST_NAME = "locationList";

    private static final String PARAM_COMPETENCE_NAME = "competenceId";

    private static final String PARAM_COMPETENCE_NO_VALUE = "noValue";

    private static final String PARAM_COMPETENCE_HIDDEN_NAME = "competenceIds";

    private static final String MODEL_COMPETENCE_NAME = "competence";

    private static final String MODEL_COMPETENCE_HIDDEN_NAME = "competenceIds";

    private static final String MODEL_COMPETENCE_LIST_NAME = "competenceList";

    private static final String MODEL_COMPETENCE_TABLE_NAME = "competenceTable";

    private static final String PARAM_PASSWORD_NAME = "passwd";

    private static final String PARAM_REPEATED_PASSWORD_NAME = "repeatedPassword";

    private static final String PARAM_LOCK_USER_HIDDEN_NAME = "lockUserHidden";

    private static final String MODEL_LOCK_USER_NAME = "lockUser";

    private static final String PARAM_AUTHORIZATION_NAME = "authorization";

    private static final String PARAM_AUTHORIZATION_NO_VALUE = "noValue";

    private static final String MODEL_AUTHORIZATION_NAME = "authorization";

    private static final String MODEL_EDITED_COUNT_NAME = "editedCount";

    private static final String MODEL_ERRORS_NAME = "errors";

    private static final String ERRORS_FIRST_NAME_MISSING = "firstNameMissing";

    private static final String ERRORS_FIRST_NAME_MISSING_VALUE = "Vorname ist ein Pflichtfeld.";

    private static final String ERRORS_FIRST_NAME_TOO_LONG = "firstNameTooLong";

    private static final String ERRORS_FIRST_NAME_TOO_LONG_VALUE = "Vorname ist zu lang. Es sind maximal 30 Zeichen erlaubt.";

    private static final String ERRORS_LAST_NAME_MISSING = "lastNameMissing";

    private static final String ERRORS_LAST_NAME_MISSING_VALUE = "Nachname ist ein Pflichtfeld.";

    private static final String ERRORS_LAST_NAME_TOO_LONG = "lastNameTooLong";

    private static final String ERRORS_LAST_NAME_TOO_LONG_VALUE = "Nachname ist zu lang. Es sind maximal 30 Zeichen erlaubt.";

    private static final String ERRORS_PHONE1_TOO_LONG = "phone1TooLong";

    private static final String ERRORS_PHONE1_TOO_LONG_VALUE = "Telefon 1 ist zu lang. Es sind maximal 50 Zeichen erlaubt";

    private static final String ERRORS_PHONE2_TOO_LONG = "phone2TooLong";

    private static final String ERRORS_PHONE2_TOO_LONG_VALUE = "Telefon 2 ist zu lang. Es sind maximal 50 Zeichen erlaubt";

    private static final String ERRORS_BIRTHDATE = "birthdate";

    private static final String ERRORS_BIRTHDATE_VALUE = "Das Datumsformat von Geburtsdatum ist nicht korrekt.";

    private static final String ERRORS_BIRTHDATE_TOO_SMALL = "birthdateTooSmall";

    private static final String ERRORS_BIRTHDATE_TOO_SMALL_VALUE = "Der Wert von Geburtsdatum ist zu klein.";

    private static final String ERRORS_BIRTHDATE_TOO_BIG = "birthdateTooBig";

    private static final String ERRORS_BIRTHDATE_TOO_BIG_VALUE = "Der Wert von Geburtsdatum ist zu gro�.";

    private static final String ERRORS_SEX = "sex";

    private static final String ERRORS_SEX_VALUE = "Geschlecht ist ein Pflichtfeld.";

    private static final String ERRORS_PHOTO_TOO_BIG = "photoTooBig";

    private static final String ERRORS_PHOTO_TOO_BIG_VALUE = "Die angegebene Datei ist zu gro�.";

    private static final String ERRORS_PHOTO_WRONG_FORMAT = "photoWrongFormat";

    private static final String ERRORS_PHOTO_WRONG_FORMAT_VALUE = "Die angegebene Datei hat das falsche Format.";

    private static final String ERRORS_LOCATION = "location";

    private static final String ERRORS_LOCATION_VALUE = "Dienststelle ist ein Pflichtfeld.";

    private static final String ERRORS_COMPETENCES = "competences";

    private static final String ERRORS_COMPETENCES_VALUE = "Kompetenz Volont�r ist verpflichtend.";

    private static final String ERRORS_PASSWORD_MISSING = "passwordMissing";

    private static final String ERRORS_PASSWORD_MISSING_VALUE = "Passwort ist ein Pflichtfeld.";

    private static final String ERRORS_PASSWORD_TOO_LONG = "passwordTooLong";

    private static final String ERRORS_PASSWORD_TOO_LONG_VALUE = "Passwort ist zu lang. Es sind maximal 255 Zeichen erlaubt.";

    private static final String ERRORS_REPEATED_PASSWORD_MISSING = "repeatedPasswordMissing";

    private static final String ERRORS_REPEATED_PASSWORD_MISSING_VALUE = "Passwort wiederholen ist ein Pflichtfeld.";

    private static final String ERRORS_REPEATED_PASSWORD_TOO_LONG = "repeatedPasswordTooLong";

    private static final String ERRORS_REPEATED_PASSWORD_TOO_LONG_VALUE = "Passwort wiederholen ist zu lang. Es sind maximal 255 Zeichen erlaubt.";

    private static final String ERRORS_PASSWORDS_NOT_EQUAL = "passwordsNotEqual";

    private static final String ERRORS_PASSWORDS_NOT_EQUAL_VALUE = "Die zwei eingegebenen Passw�rter stimmen nicht �berein.";

    private static final String ERRORS_AUTHORIZATION = "authorization";

    private static final String ERRORS_AUTHORIZATION_VALUE = "Authorisierung ist ein Pflichtfeld.";

    @Override
    public Map<String, Object> handleRequest(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws Exception {
        final ResourceBundle fileUpload = ResourceBundle.getBundle(Dispatcher.FILEUPLOAD_BUNDLE_PATH);
        final Map<String, Object> params = new HashMap<String, Object>();
        final UserSession userSession = (UserSession) request.getSession().getAttribute("userSession");
        final WebClient connection = userSession.getConnection();
        final String authorization = userSession.getLoginInformation().getAuthorization();
        if (!authorization.equals(Login.AUTH_ADMIN)) {
            throw new IllegalArgumentException("Error: User has no permission for functionality.");
        }
        String paramStaffMemberId = null;
        String paramAction = null;
        String paramFirstName = null;
        String paramLastName = null;
        String paramPhone1 = null;
        String paramPhone2 = null;
        String paramBirthdate = null;
        String paramSex = null;
        FileItem photo = null;
        String paramLocationId = null;
        String paramCompetenceId = null;
        String paramCompetenceHidden = null;
        String paramPassword = null;
        String paramRepeatedPassword = null;
        String paramLockUserHidden = null;
        String paramStaffMemberAuthorization = null;
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            final DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(Integer.parseInt(fileUpload.getString("memory.maxsize")));
            factory.setRepository(new File(fileUpload.getString("tmp.dir")));
            final ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(Long.parseLong(fileUpload.getString("request.maxsize")));
            final List<FileItem> items = upload.parseRequest(request);
            final Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                final FileItem item = (FileItem) iter.next();
                if (!item.isFormField()) {
                    if (item.getSize() > 0) {
                        photo = item;
                        params.put(MODEL_PHOTO_PATH_NAME, item.getName());
                    }
                } else {
                    if (item.getFieldName().equals(ACTION_NAME)) {
                        paramAction = item.getString();
                    } else if (item.getFieldName().equals(PARAM_FIRSTNAME_NAME)) {
                        paramFirstName = item.getString();
                    } else if (item.getFieldName().equals(PARAM_LASTNAME_NAME)) {
                        paramLastName = item.getString();
                    } else if (item.getFieldName().equals(PARAM_PHONE1_NAME)) {
                        paramPhone1 = item.getString();
                    } else if (item.getFieldName().equals(PARAM_PHONE2_NAME)) {
                        paramPhone2 = item.getString();
                    } else if (item.getFieldName().equals(PARAM_BIRTHDATE_NAME)) {
                        paramBirthdate = item.getString();
                    } else if (item.getFieldName().equals(PARAM_SEX_NAME)) {
                        paramSex = item.getString();
                    } else if (item.getFieldName().equals(PARAM_LOCATION_NAME)) {
                        paramLocationId = item.getString();
                    } else if (item.getFieldName().equals(PARAM_COMPETENCE_NAME)) {
                        paramCompetenceId = item.getString();
                    } else if (item.getFieldName().equals(PARAM_COMPETENCE_HIDDEN_NAME)) {
                        paramCompetenceHidden = item.getString();
                    } else if (item.getFieldName().equals(PARAM_PASSWORD_NAME)) {
                        paramPassword = item.getString();
                    } else if (item.getFieldName().equals(PARAM_REPEATED_PASSWORD_NAME)) {
                        paramRepeatedPassword = item.getString();
                    } else if (item.getFieldName().equals(PARAM_LOCK_USER_HIDDEN_NAME)) {
                        paramLockUserHidden = item.getString();
                    } else if (item.getFieldName().equals(PARAM_AUTHORIZATION_NAME)) {
                        paramStaffMemberAuthorization = item.getString();
                    } else if (item.getFieldName().equals(PARAM_STAFF_MEMBER_NAME)) {
                        paramStaffMemberId = item.getString();
                    }
                }
            }
        }
        params.put(MODEL_TIMESTAMP_NAME, new Date().getTime());
        if (request.getParameter(PARAM_STAFF_MEMBER_NAME) != null) {
            paramStaffMemberId = request.getParameter(PARAM_STAFF_MEMBER_NAME);
        }
        int staffMemberId = 0;
        StaffMember staffMember = null;
        if (paramStaffMemberId != null && !paramStaffMemberId.equals("")) {
            staffMemberId = Integer.parseInt(paramStaffMemberId);
        }
        final QueryFilter lockedStaffMembersFilter = new QueryFilter();
        lockedStaffMembersFilter.add(IFilterTypes.STAFF_MEMBER_LOCKED_UNLOCKED_FILTER, "true");
        final List<AbstractMessage> staffMemberList = connection.sendListingRequest(StaffMember.ID, lockedStaffMembersFilter);
        if (!StaffMember.ID.equalsIgnoreCase(connection.getContentType())) {
            throw new IllegalArgumentException("Error: Error at connection to Tacos server occoured.");
        }
        params.put(MODEL_STAFF_MEMBER_LIST_NAME, staffMemberList);
        for (final Iterator<AbstractMessage> itStaffMemberList = staffMemberList.iterator(); itStaffMemberList.hasNext(); ) {
            final StaffMember sm = (StaffMember) itStaffMemberList.next();
            if (sm.getStaffMemberId() == staffMemberId) {
                staffMember = sm;
            }
        }
        if (staffMember == null) {
            if (staffMemberList.size() > 0) {
                staffMember = (StaffMember) staffMemberList.get(0);
            } else {
                throw new IllegalArgumentException("Error: Location has an illegal state.");
            }
        }
        params.put(MODEL_STAFF_MEMBER_NAME, staffMember);
        final QueryFilter loginUsernameF = new QueryFilter();
        loginUsernameF.add(IFilterTypes.USERNAME_FILTER, staffMember.getUserName());
        final List<AbstractMessage> loginList = connection.sendListingRequest(Login.ID, loginUsernameF);
        if (!Login.ID.equalsIgnoreCase(connection.getContentType())) {
            throw new IllegalArgumentException("Error: Error at connection to Tacos server occoured.");
        }
        final Login login = (Login) loginList.get(0);
        params.put(MODEL_LOGIN_NAME, login);
        if (staffMember != null && login != null) {
            final String defaultFirstName = staffMember.getFirstName();
            String firstName = null;
            firstName = paramFirstName;
            if (firstName != null) {
                params.put(MODEL_FIRSTNAME_NAME, firstName);
            } else {
                params.put(MODEL_FIRSTNAME_NAME, defaultFirstName);
            }
            final String defaultLastName = staffMember.getLastName();
            String lastName = null;
            lastName = paramLastName;
            if (lastName != null) {
                params.put(MODEL_LASTNAME_NAME, lastName);
            } else {
                params.put(MODEL_LASTNAME_NAME, defaultLastName);
            }
            final String defaultPhone1 = staffMember.getPhone1();
            String phone1 = null;
            phone1 = paramPhone1;
            if (phone1 != null) {
                params.put(MODEL_PHONE1_NAME, phone1);
            } else params.put(MODEL_PHONE1_NAME, defaultPhone1);
            final String defaultPhone2 = staffMember.getPhone2();
            String phone2 = null;
            phone2 = paramPhone2;
            if (phone2 != null) {
                params.put(MODEL_PHONE2_NAME, phone2);
            } else params.put(MODEL_PHONE2_NAME, defaultPhone2);
            final Calendar calendar = Calendar.getInstance();
            final int rangeStart = calendar.get(Calendar.YEAR) - MODEL_CALENDAR_MAX_AGE;
            final int rangeEnd = calendar.get(Calendar.YEAR);
            params.put(MODEL_CALENDAR_DEFAULT_DATE_MILLISECONDS_NAME, calendar.getTimeInMillis());
            params.put(MODEL_CALENDAR_RANGE_START_NAME, rangeStart);
            params.put(MODEL_CALENDAR_RANGE_END_NAME, rangeEnd);
            String birthdateString = null;
            String defaultBirthdateString;
            if (staffMember.getBirthday() != null) {
                defaultBirthdateString = staffMember.getBirthday().replaceAll("-", ".");
            } else {
                defaultBirthdateString = null;
            }
            if (paramBirthdate != null) {
                birthdateString = paramBirthdate;
            }
            if (birthdateString != null) {
                params.put(MODEL_BIRTHDATE_NAME, birthdateString);
            } else {
                params.put(MODEL_BIRTHDATE_NAME, defaultBirthdateString);
            }
            String defaultSex = null;
            if (staffMember.isMale()) {
                defaultSex = StaffMember.STAFF_MALE;
            } else {
                defaultSex = StaffMember.STAFF_FEMALE;
            }
            String sex = null;
            if (paramSex != null && !paramSex.equals("") && !paramSex.equals(PARAM_SEX_NO_VALUE)) {
                if (paramSex.equals(StaffMember.STAFF_MALE)) {
                    sex = paramSex;
                } else if (paramSex.equals(StaffMember.STAFF_FEMALE)) {
                    sex = paramSex;
                }
            }
            if (sex != null || (paramSex != null && paramSex.equals(PARAM_SEX_NO_VALUE))) {
                params.put(MODEL_SEX_NAME, sex);
            } else {
                params.put(MODEL_SEX_NAME, defaultSex);
            }
            int locationId = 0;
            final Location defaultLocation = staffMember.getPrimaryLocation();
            Location location = null;
            if (paramLocationId != null && !paramLocationId.equals("") && !paramLocationId.equals(PARAM_LOCATION_NO_VALUE)) {
                locationId = Integer.parseInt(paramLocationId);
            }
            final List<AbstractMessage> locationList = connection.sendListingRequest(Location.ID, null);
            if (!Location.ID.equalsIgnoreCase(connection.getContentType())) {
                throw new IllegalArgumentException("Error: Error at connection to Tacos server occoured.");
            }
            for (final Iterator<AbstractMessage> itLoactionList = locationList.iterator(); itLoactionList.hasNext(); ) {
                final Location l = (Location) itLoactionList.next();
                if (l.getId() == locationId) {
                    location = l;
                }
            }
            params.put(MODEL_LOCATION_LIST_NAME, locationList);
            if (location != null || (paramLocationId != null && paramLocationId.equals(PARAM_LOCATION_NO_VALUE))) {
                params.put(MODEL_LOCATION_NAME, location);
            } else {
                params.put(MODEL_LOCATION_NAME, defaultLocation);
            }
            int competenceId = 0;
            final Competence defaultCompetence = null;
            Competence competence = null;
            Competence volunteerCompetence = null;
            if (paramCompetenceId != null && !paramCompetenceId.equals("") && !paramCompetenceId.equals(PARAM_COMPETENCE_NO_VALUE)) {
                competenceId = Integer.parseInt(paramCompetenceId);
            }
            final List<AbstractMessage> competenceList = connection.sendListingRequest(Competence.ID, null);
            if (!Competence.ID.equalsIgnoreCase(connection.getContentType())) {
                throw new IllegalArgumentException("Error: Error at connection to Tacos server occoured.");
            }
            for (final Iterator<AbstractMessage> itCompetenceL = competenceList.iterator(); itCompetenceL.hasNext(); ) {
                final Competence c = (Competence) itCompetenceL.next();
                if (c.getId() == competenceId) {
                    competence = c;
                }
                if (c.getCompetenceName().equals(Competence.COMPETENCE_NAME_VOLUNTEER)) {
                    volunteerCompetence = c;
                }
            }
            params.put(MODEL_COMPETENCE_LIST_NAME, competenceList);
            if (competence != null || (paramCompetenceId != null && paramCompetenceId.equals(PARAM_COMPETENCE_NO_VALUE))) {
                params.put(MODEL_COMPETENCE_NAME, competence);
            } else {
                params.put(MODEL_COMPETENCE_NAME, defaultCompetence);
            }
            final List<Competence> competenceTable = new ArrayList<Competence>();
            final List<Competence> defaultCompetenceTable = staffMember.getCompetenceList();
            String competenceHidden = null;
            String defaultCompetenceHidden = null;
            for (final Iterator<Competence> itCompTable = defaultCompetenceTable.iterator(); itCompTable.hasNext(); ) {
                final Competence co = (Competence) itCompTable.next();
                if (defaultCompetenceHidden == null) {
                    defaultCompetenceHidden = Integer.toString(co.getId());
                } else {
                    defaultCompetenceHidden = defaultCompetenceHidden + "," + Integer.toString(co.getId());
                }
            }
            if (paramCompetenceHidden != null && !paramCompetenceHidden.equals("")) {
                final String[] paramCompetenceTableArray = paramCompetenceHidden.split(",");
                for (int i = 0; i < paramCompetenceTableArray.length; i++) {
                    for (final Iterator<AbstractMessage> itCompetence = competenceList.iterator(); itCompetence.hasNext(); ) {
                        final Competence co = (Competence) itCompetence.next();
                        if (co.getId() == Integer.parseInt(paramCompetenceTableArray[i])) {
                            competenceTable.add(co);
                            if (competenceHidden == null) {
                                competenceHidden = Integer.toString(co.getId());
                            } else {
                                competenceHidden = competenceHidden + "," + Integer.toString(co.getId());
                            }
                        }
                    }
                }
            }
            if (competenceHidden != null) {
                params.put(MODEL_COMPETENCE_HIDDEN_NAME, competenceHidden);
            } else {
                params.put(MODEL_COMPETENCE_HIDDEN_NAME, defaultCompetenceHidden);
            }
            if (competenceTable.size() > 0) {
                params.put(MODEL_COMPETENCE_TABLE_NAME, competenceTable);
            } else {
                params.put(MODEL_COMPETENCE_TABLE_NAME, defaultCompetenceTable);
            }
            String password = null;
            if (paramPassword != null) {
                password = paramPassword;
            }
            String repeatedPassword = null;
            if (paramRepeatedPassword != null) {
                repeatedPassword = paramRepeatedPassword;
            }
            boolean defaultLockUser = login.isIslocked();
            boolean lockUser = false;
            if (paramLockUserHidden != null) {
                if (paramLockUserHidden.equalsIgnoreCase("true")) {
                    lockUser = true;
                } else {
                    lockUser = false;
                }
                params.put(MODEL_LOCK_USER_NAME, lockUser);
            } else {
                params.put(MODEL_LOCK_USER_NAME, defaultLockUser);
            }
            final String defaultStaffMemberAuthorization = login.getAuthorization();
            String staffMemberAuthorization = null;
            if (paramStaffMemberAuthorization != null && !paramStaffMemberAuthorization.equals("") && !paramStaffMemberAuthorization.equals(PARAM_AUTHORIZATION_NO_VALUE)) {
                if (paramStaffMemberAuthorization.equals(Login.AUTH_USER)) {
                    staffMemberAuthorization = paramStaffMemberAuthorization;
                } else if (paramStaffMemberAuthorization.equals(Login.AUTH_ADMIN)) {
                    staffMemberAuthorization = paramStaffMemberAuthorization;
                }
            }
            if (staffMemberAuthorization != null || (paramStaffMemberAuthorization != null && paramStaffMemberAuthorization.equals(PARAM_AUTHORIZATION_NO_VALUE))) {
                params.put(MODEL_AUTHORIZATION_NAME, staffMemberAuthorization);
            } else {
                params.put(MODEL_AUTHORIZATION_NAME, defaultStaffMemberAuthorization);
            }
            final String action = paramAction;
            final Map<String, String> errors = new HashMap<String, String>();
            boolean valid = true;
            if (action != null && action.equals(ACTION_UPDATE_STAFF_MEMBER)) {
                if (staffMember == null) {
                    throw new IllegalArgumentException("Staff Member must not be null.");
                }
                if (login == null) {
                    throw new IllegalArgumentException("Login must not be null.");
                }
                if (firstName == null || firstName.trim().equals("")) {
                    valid = false;
                    errors.put(ERRORS_FIRST_NAME_MISSING, ERRORS_FIRST_NAME_MISSING_VALUE);
                } else if (firstName.length() > 30) {
                    valid = false;
                    errors.put(ERRORS_FIRST_NAME_TOO_LONG, ERRORS_FIRST_NAME_TOO_LONG_VALUE);
                }
                if (lastName == null || lastName.trim().equals("")) {
                    valid = false;
                    errors.put(ERRORS_LAST_NAME_MISSING, ERRORS_LAST_NAME_MISSING_VALUE);
                } else if (lastName.length() > 30) {
                    valid = false;
                    errors.put(ERRORS_LAST_NAME_TOO_LONG, ERRORS_LAST_NAME_TOO_LONG_VALUE);
                }
                if (phone1.length() > 50) {
                    valid = false;
                    errors.put(ERRORS_PHONE1_TOO_LONG, ERRORS_PHONE1_TOO_LONG_VALUE);
                }
                if (phone2.length() > 50) {
                    valid = false;
                    errors.put(ERRORS_PHONE2_TOO_LONG, ERRORS_PHONE2_TOO_LONG_VALUE);
                }
                final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                Date birthdate = null;
                final Calendar rangeStartCalendar = Calendar.getInstance();
                rangeStartCalendar.set(Calendar.YEAR, rangeStartCalendar.get(Calendar.YEAR) - MODEL_CALENDAR_MAX_AGE);
                final Calendar rangeEndCalendar = Calendar.getInstance();
                rangeEndCalendar.set(Calendar.YEAR, rangeEndCalendar.get(Calendar.YEAR));
                if (birthdateString != null && !birthdateString.trim().equals("")) {
                    try {
                        birthdate = df.parse(birthdateString);
                    } catch (ParseException e) {
                        valid = false;
                        errors.put(ERRORS_BIRTHDATE, ERRORS_BIRTHDATE_VALUE);
                    }
                    if (birthdate != null) {
                        if (birthdate.getTime() < rangeStartCalendar.getTimeInMillis()) {
                            valid = false;
                            errors.put(ERRORS_BIRTHDATE_TOO_SMALL, ERRORS_BIRTHDATE_TOO_SMALL_VALUE);
                        } else if (birthdate.getTime() > rangeEndCalendar.getTimeInMillis()) {
                            valid = false;
                            errors.put(ERRORS_BIRTHDATE_TOO_BIG, ERRORS_BIRTHDATE_TOO_BIG_VALUE);
                        }
                    }
                }
                if (sex == null) {
                    valid = false;
                    errors.put(ERRORS_SEX, ERRORS_SEX_VALUE);
                }
                if (photo != null) {
                    final String contentType = photo.getContentType();
                    final String fileName = photo.getName();
                    long sizeInBytes = photo.getSize();
                    if (sizeInBytes > Long.parseLong(fileUpload.getString("editStaffMember.photo.maxsize"))) {
                        valid = false;
                        errors.put(ERRORS_PHOTO_TOO_BIG, ERRORS_PHOTO_TOO_BIG_VALUE);
                    } else if (!Pattern.matches(fileUpload.getString("editStaffMember.photo.contentType"), contentType)) {
                        valid = false;
                        errors.put(ERRORS_PHOTO_WRONG_FORMAT, ERRORS_PHOTO_WRONG_FORMAT_VALUE);
                    }
                }
                if (location == null) {
                    valid = false;
                    errors.put(ERRORS_LOCATION, ERRORS_LOCATION_VALUE);
                }
                boolean volunteerFound = false;
                for (final Iterator<Competence> itCT = competenceTable.iterator(); itCT.hasNext(); ) {
                    final Competence co = itCT.next();
                    if (co.getCompetenceName().equals(Competence.COMPETENCE_NAME_VOLUNTEER)) {
                        volunteerFound = true;
                    }
                }
                if (!volunteerFound) {
                    valid = false;
                    errors.put(ERRORS_COMPETENCES, ERRORS_COMPETENCES_VALUE);
                }
                if (password != null && !password.trim().equals("") && repeatedPassword != null && !repeatedPassword.trim().equals("")) {
                    if (password == null || password.trim().equals("")) {
                        valid = false;
                        errors.put(ERRORS_PASSWORD_MISSING, ERRORS_PASSWORD_MISSING_VALUE);
                    } else if (password.length() > 255) {
                        valid = false;
                        errors.put(ERRORS_PASSWORD_TOO_LONG, ERRORS_PASSWORD_TOO_LONG_VALUE);
                    }
                    if (repeatedPassword == null || repeatedPassword.trim().equals("")) {
                        valid = false;
                        errors.put(ERRORS_REPEATED_PASSWORD_MISSING, ERRORS_REPEATED_PASSWORD_MISSING_VALUE);
                    } else if (repeatedPassword.length() > 255) {
                        valid = false;
                        errors.put(ERRORS_REPEATED_PASSWORD_TOO_LONG, ERRORS_REPEATED_PASSWORD_TOO_LONG_VALUE);
                    }
                    if (!password.equals(repeatedPassword)) {
                        valid = false;
                        errors.put(ERRORS_PASSWORDS_NOT_EQUAL, ERRORS_PASSWORDS_NOT_EQUAL_VALUE);
                    }
                }
                if (staffMemberAuthorization == null) {
                    valid = false;
                    errors.put(ERRORS_AUTHORIZATION, ERRORS_AUTHORIZATION_VALUE);
                }
                if (valid) {
                    if (password != null && !password.trim().equals("")) {
                        login.setPassword(password);
                    }
                    login.setIslocked(lockUser);
                    login.setAuthorization(staffMemberAuthorization);
                    staffMember.setFirstName(firstName);
                    staffMember.setLastName(lastName);
                    if (phone1 != null) staffMember.setPhone1(phone1);
                    if (phone2 != null) staffMember.setPhone2(phone2);
                    if (birthdate != null) {
                        final SimpleDateFormat dfServer = new SimpleDateFormat("dd-MM-yyyy");
                        staffMember.setBirthday(dfServer.format(birthdate));
                    }
                    if (sex.equals(StaffMember.STAFF_MALE)) {
                        staffMember.setMale(true);
                    } else if (sex.equals(StaffMember.STAFF_FEMALE)) {
                        staffMember.setMale(false);
                    }
                    if (photo != null) {
                        final File uploadedFile = new File(fileUpload.getString("editStaffMember.photo.absolute.dir") + "/" + staffMember.getStaffMemberId() + ".jpg");
                        photo.write(uploadedFile);
                    }
                    staffMember.setPrimaryLocation(location);
                    staffMember.setCompetenceList(competenceTable);
                    login.setUserInformation(staffMember);
                    connection.sendUpdateRequest(Login.ID, login);
                    if (!connection.getContentType().equalsIgnoreCase(Login.ID)) {
                        throw new IllegalArgumentException("Error: Error at connection to Tacos server occoured.");
                    }
                    connection.sendUpdateRequest(StaffMember.ID, staffMember);
                    if (!connection.getContentType().equalsIgnoreCase(StaffMember.ID)) {
                        throw new IllegalArgumentException("Error: Error at connection to Tacos server occoured.");
                    }
                    userSession.getDefaultFormValues().setDefaultLocation(location);
                    params.put(MODEL_EDITED_COUNT_NAME, 1);
                }
            }
            params.put(MODEL_ERRORS_NAME, errors);
        }
        userSession.getDefaultFormValues().setDefaultStaffMember(staffMember);
        return params;
    }
}
