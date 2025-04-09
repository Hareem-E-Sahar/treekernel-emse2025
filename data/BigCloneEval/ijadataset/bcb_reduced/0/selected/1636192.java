package de.dNb.conversion.converters.util;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.ddb.pica.record.PicaField;
import de.ddb.pica.record.PicaRecord;
import de.ddb.pica.record.PicaSubfield;

/*******************************************************************************
 * Enthält nützliche Konstanten und Funktionen zur Datenkonversion.
 * 
 * 
 * @author Marcus Klein, German National Library
 * @since 01.02.2010
 ******************************************************************************/
public class ToPicaPlusUtil {

    private static final Log logger = LogFactory.getLog(ToPicaPlusUtil.class);

    /**
	 * ePaper
	 */
    public static final String ZSTITELID = "DNB_ZSTitelID";

    /**
	 * ePaper
	 */
    public static final String ERSTKATID = "Erstkat-ID";

    /**
	 * Einleitende Wendung: "In:" (ohne Blank!)
	 */
    public static final String INTRO_PHRASE_IN = "In:";

    /**
	 * Zeichen: Punkt
	 */
    public static final String CHAR_POINT = ".";

    /**
	 * Zeichen: Blank, Schrägstrich, Blank
	 */
    public static final String CHAR_BLANK_SLASH_BLANK = " / ";

    /**
	 * Zeichen: Schrägstrich
	 */
    public static final String CHAR_SLASH = "/";

    /**
	 * Zeichen: Punkt, Leerzeichen, Bindestrich, Leerzeichen
	 */
    public static final String CHAR_POINT_BLANK_HYPHEN_BLANK = ". - ";

    /**
	 * Zeichen: Leerzeichen
	 */
    public static final String CHAR_BLANK = " ";

    /**
	 * Zeichen: Leerzeichen, Semikolon, Blank
	 */
    public static final String CHAR_BLANK_SEMICOLON_BLANK = " ; ";

    /**
	 * Zeichen: Leerzeichen, Dopppelpunkt, Leerzeichen
	 */
    public static final String CHAR_BLANK_COLON_BLANK = " : ";

    /**
	 * Zeichen: Dopppelpunkt, Leerzeichen
	 */
    public static final String CHAR_COLON_BLANK = ": ";

    /**
	 * Zeichen: Dopppelpunkt
	 */
    public static final String CHAR_COLON = ":";

    /**
	 * Zeichen: Punkt, Leerzeichen
	 */
    public static final String CHAR_POINT_BLANK = ". ";

    /**
	 * Zeichen: Semikolon
	 */
    public static final String CHAR_SEMICOLON = ";";

    /**
	 * Zeichen: Semikolon, Leerzeichen
	 */
    public static final String CHAR_SEMICOLON_BLANK = "; ";

    /**
	 * Zeichen: Komma, Leerzeichen
	 */
    public static final String CHAR_COMMA_BLANK = ", ";

    /**
	 * Zeichen: Leerzeichen, Komma, Leerzeichen
	 */
    public static final String CHAR_BLANK_COMMA_BLANK = " , ";

    /**
	 * HashMap Person, Key: Vorname
	 */
    public static final String PERS_FORENAME = "forename";

    /**
	 * HashMap Person, Key: Namenspräfix
	 */
    public static final String PERS_PREFIX = "prefix";

    /**
	 * HashMap Person, Key: Nachname
	 */
    public static final String PERS_SURNAME = "surname";

    /**
	 * HashMap Person, Key: Persönlicher Name
	 */
    public static final String PERS_GIVEN_NAME = "givenName";

    /**
	 * HashMap Körperschaft, Key: Orte
	 */
    public static final String CORP_PLACES = "places";

    /**
	 * HashMap Körperschaft, Key: Name
	 */
    public static final String CORP_NAME = "name";

    /**
	 * HashMap Hochschulschriftenvermerk, Key: Name der Hochschule
	 */
    public static final String THESIS_UNIVERSITY_PLACE = "place";

    /**
	 * HashMap Hochschulschriftenvermerk, Key: Hochschulort
	 */
    public static final String THESIS_UNIVERSITY_NAME = "name";

    /**
	 * HashMap Hochschulschriftenvermerk, Key: Art der Hochschulschrift
	 */
    public static final String THESIS_TYPE = "type";

    /**
	 * HashMap Hochschulschriftenvermerk, Key: Name Prüfungsjahr
	 */
    public static final String THESIS_YEAR = "year";

    /**
	 * HashMap: leerer Key
	 */
    public static final String EMPTY_KEY = "empty";

    /**
	 * Datumsformat: dd.MM.yyyy
	 */
    public static final SimpleDateFormat DF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");

    /**
	 * Datumsformat: yyyy
	 */
    public static final SimpleDateFormat DF_YYYY = new SimpleDateFormat("yyyy");

    /**
	 * Datumsformat: yyyyMM
	 */
    public static final SimpleDateFormat DF_YYYYMM = new SimpleDateFormat("yyyyMM");

    /**
	 * Datumsformat: yyyyMMdd
	 */
    public static final SimpleDateFormat DF_YYYYMMDD = new SimpleDateFormat("yyyyMMdd");

    /**
	 * Datumsformat: yy.MM
	 */
    public static final SimpleDateFormat DF_YY_MM = new SimpleDateFormat("yy.MM");

    /**
	 * Datumsformat: yy.MM.dd
	 */
    public static final SimpleDateFormat DF_YY_MM_DD = new SimpleDateFormat("yy.MM.dd");

    /**
	 * Datumsformat: yy.MM.dd MMMM yyyy
	 */
    public static final SimpleDateFormat DF_YY_MM_DD_MMMM_YYYY = new SimpleDateFormat("yy.MM.dd MMMM yyyy");

    /**
	 * Datumsformat: yyyy-MM-dd
	 */
    public static final SimpleDateFormat DF_YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");

    /**
	 * <p>
	 * Mapt eine List auf ein wiederholbares Pica-Unterfeld.
	 * </p>
	 * 
	 * @param list
	 *            Pica-Unterfelder als List
	 * @param picaField
	 *            Pica-Feld
	 * @param subfield
	 *            Wiederholbares Pica-Unterfeld
	 * @author t_kleinm, German National Library
	 */
    public static void picaListToPicaSubfield(List<String> list, PicaField picaField, String subfield) {
        if (logger.isDebugEnabled()) {
            logger.debug("picaListToPicaSubfield() - start");
        }
        if (list != null && list.size() > 0 && picaField != null && !isNullOrEmpty(subfield)) {
            for (int i = 0; i < list.size(); i++) {
                String value = list.get(i);
                if (!isNullOrEmpty(value)) {
                    picaField.addNewSubfield(subfield, value);
                }
            }
        } else if (logger.isDebugEnabled()) {
            if (list == null) {
                logger.debug("picaMapToPicaField() - list= null");
            }
            if (picaField == null) {
                logger.debug("picaMapToPicaField() - picaField= null");
            }
            if (subfield == null) {
                logger.debug("picaMapToPicaField() - subfield= null");
            } else {
                logger.debug("picaMapToPicaField() - subfield.length()= " + subfield.length());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("picaListToPicaSubfield() - end");
        }
    }

    /**
	 * <p>
	 * Mapt ein HashMap auf ein Pica-Feld.
	 * </p>
	 * 
	 * @param hashmap
	 *            Pica-Feld als HashMap
	 * @param picaField
	 *            Pica-Feld
	 * @param subfields
	 *            Pica-Unterfelder als String-Array in der Reihenfolge, in
	 *            welcher sie erzeugt werden sollen
	 * @author t_kleinm, German National Library
	 */
    public static void picaMapToPicaField(HashMap<String, String> hashmap, PicaField picaField, String[] subfields) {
        if (logger.isDebugEnabled()) {
            logger.debug("picaMapToPicaField() - start");
        }
        if (hashmap != null && hashmap.size() > 0 && picaField != null && subfields != null && subfields.length > 0) {
            for (String subfield : subfields) {
                if (logger.isDebugEnabled()) {
                    logger.debug("picaMapToPicaField() - subfield " + subfield + CHAR_COLON_BLANK + hashmap.get(subfield));
                }
                if (!isNullOrEmpty(hashmap.get(subfield))) picaField.addNewSubfield(subfield, hashmap.get(subfield));
            }
        } else if (logger.isDebugEnabled()) {
            if (hashmap == null) logger.debug("picaMapToPicaField() - hashmap= null");
            if (picaField == null) logger.debug("picaMapToPicaField() - picaField= null");
            if (subfields == null) logger.debug("picaMapToPicaField() - subfields= null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("picaMapToPicaField() - end");
        }
    }

    /**
	 * <p>
	 * Mapt ein Pica-HashMap auf ein Pica-Feld mit wiederholbaren Unterfelder.
	 * </p>
	 * 
	 * @param hashMap
	 *            Pica-Feld als HashMap
	 * @param picaField
	 *            Pica-Feld
	 * @param subfields
	 *            Pica-Unterfelder als String-Array in der Reihenfolge, in
	 *            welcher sie erzeugt werden sollen
	 * @author t_kleinm, German National Library
	 */
    public static void picaMapToPicaFieldRepeatable(HashMap<String, List<String>> hashMap, PicaField picaField, String[] subfields) {
        if (logger.isDebugEnabled()) {
            logger.debug("picaMapToPicaFieldRepeatable() - start");
        }
        if (hashMap != null && hashMap.size() > 0 && picaField != null && subfields != null && subfields.length > 0) {
            for (String subfield : subfields) {
                if (logger.isDebugEnabled()) {
                    logger.debug("picaMapToPicaFieldRepeatable() - subfield= " + subfield);
                }
                picaListToPicaSubfield(hashMap.get(subfield), picaField, subfield);
            }
        }
        if (logger.isDebugEnabled()) {
            if (hashMap == null) {
                logger.debug("picaMapToPicaFieldRepeatable() - hashMap= null");
            } else {
                logger.debug("picaMapToPicaFieldRepeatable() - hashMap.size()= " + hashMap.size());
            }
            if (picaField == null) {
                logger.debug("picaMapToPicaFieldRepeatable() - picaField= null");
            }
            if (subfields == null) {
                logger.debug("picaMapToPicaFieldRepeatable() - subfields= null");
            } else {
                logger.debug("picaMapToPicaFieldRepeatable() - subfields.length=" + subfields.length);
            }
            logger.debug("picaMapToPicaFieldRepeatable() - end");
        }
    }

    /**
	 * <p>
	 * Ein Pica-Feld enthält ein nicht wiederholbares Unterfeld, welches
	 * überschrieben werden soll.
	 * </p>
	 * 
	 * @param field
	 *            Pica-Feld, dessen Unterfeld einen neuen Wert erhalten soll
	 * @param subfield
	 *            Unterfeld, welches einen neuen Wert erhalten soll
	 * @param newValue
	 *            Der neue Wert für das Unterfeld
	 * @param delimiter
	 *            Abgrenzungszeichen (Delimiter), fall das Unterfeld bereits
	 *            einen Wert besitzt
	 * @author kleinm, German National Library
	 */
    @SuppressWarnings("unchecked")
    public static void updatePicaSubfield(PicaField field, String subfield, String newValue, String delimiter) {
        if (logger.isDebugEnabled()) {
            logger.debug("updatePicaSubfield() - start");
            logger.debug("updatePicaSubfield() - before update: field= " + field);
        }
        if (field != null && subfield != null && newValue != null && newValue.length() > 0) {
            if (field.hasSubfield(subfield)) {
                List<PicaSubfield> picaSubfields = field.selectSubfields(subfield);
                if (picaSubfields != null && picaSubfields.size() > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("updatePicaSubfield() - picaSubfields= " + picaSubfields);
                    }
                    PicaSubfield picaSubfield = picaSubfields.get(0);
                    if (delimiter == null) delimiter = CHAR_BLANK;
                    String content = field.selectContent(subfield);
                    if (logger.isDebugEnabled()) {
                        logger.debug("updatePicaSubfield() - field.selectContent(\"" + subfield + "\")= " + content);
                    }
                    if (content != null && content.length() > 0) {
                        content += delimiter + newValue;
                    } else {
                        content = newValue;
                    }
                    PicaSubfield newPicaSubfield = new PicaSubfield(subfield);
                    newPicaSubfield.setContent(content);
                    field.overwriteSubfield(picaSubfield, newPicaSubfield);
                    if (logger.isDebugEnabled()) {
                        logger.debug("updatePicaSubfield() - subfield $" + subfield + " successfully updated");
                        logger.debug("updatePicaSubfield() - after update: field= " + field);
                    }
                } else {
                    logger.error("updatePicaSubfield() - can not access subfields");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("updatePicaSubfield() - creating new subfield $" + subfield);
                }
                field.addNewSubfield(subfield, newValue);
                if (logger.isDebugEnabled()) {
                    logger.debug("updatePicaSubfield() - field= " + field);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("updatePicaSubfield() - end");
        }
    }

    /**
	 * @param str
	 *            Enthält der übergebene String einen Wert?
	 * @return true oder false
	 * @author t_kleinm, German National Library
	 */
    public static boolean isNullOrEmpty(String str) {
        if (str == null) return true; else if (str.trim().length() == 0) return true;
        return false;
    }

    /**
	 * @see #isNullOrEmpty(String)
	 * @author kleinm, German National Library
	 * @param str
	 *            Enthält der übergebene String einen Wert?
	 * @return true oder false
	 * @deprecated
	 */
    @Deprecated
    public static boolean isEmpty(String str) {
        return isNullOrEmpty(str);
    }

    /**
	 * <p>
	 * "Säubert" Text, d.h. löscht überflüssige Whitespaces.
	 * </p>
	 * <p>
	 * Diese Funktion wird nicht mehr benötigt, da Zeilenumbrüche (\n) und
	 * Wagenrückläufe (\r) in Pica-Subfeldern ab pica-tools-1.2.8 bereits über
	 * <code>addNewSubfield()</code> entfernt werden. Weitere Whitespaces
	 * innerhalb eines Strings sollen nicht mehr entfernt werden. Nur noch das
	 * Entfernen von Leerzeichen am Anfang und Ende ist erlaubt.
	 * </p>
	 * 
	 * @param text
	 *            Text, der "gesäubert" werden soll
	 * @return Früher: "gesäuberte" Text
	 * @deprecated
	 * @author Marcus Klein
	 */
    @Deprecated
    public static String clean(String text) {
        if (logger.isDebugEnabled()) {
            logger.debug("clean() - start");
        }
        if (text != null && text.length() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("clean() - length before cleaning: " + text.length());
            }
            text = text.replaceAll("\n", CHAR_BLANK).replaceAll("\t", CHAR_BLANK).replaceAll("\r", CHAR_BLANK).replaceAll("\f", CHAR_BLANK).replaceAll("\\s+", CHAR_BLANK).replaceAll(" +", CHAR_BLANK).trim();
            if (logger.isDebugEnabled()) {
                logger.debug("clean() - length after cleaning: " + text.length());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("clean() - text= " + text);
            logger.debug("clean() - end");
        }
        return text;
    }

    /**
	 * <p>
	 * Ein Pica-Feld enthält ein nicht wiederholbares Unterfeld, welches neu
	 * erzeugt oder überschrieben werden soll.
	 * </p>
	 * 
	 * @param picaHashMap
	 *            Pica-Feld als HashMap
	 * @param key
	 *            Pica-Unterfeld
	 * @param newValue
	 *            neuer Inhalt des Pica-Unterfeldes
	 * @param delimiter
	 *            Abgrenzungszeichen (Delimiter), falls das Pica-Unterfeld
	 *            bereits einen Wert hat
	 * @author t_kleinm, German National Library
	 */
    public static void updatePicaMap(HashMap<String, String> picaHashMap, String key, String newValue, String delimiter) {
        if (logger.isDebugEnabled()) {
            logger.debug("createOrUpdatePicaSubField() - start");
        }
        String oldValue = null;
        if (picaHashMap != null & !picaHashMap.isEmpty() && newValue != null && newValue.length() > 0) {
            oldValue = picaHashMap.get(key);
            if (oldValue == null || oldValue.length() == 0) {
                picaHashMap.put(key, newValue);
            } else {
                if (delimiter == null) delimiter = CHAR_BLANK;
                newValue = oldValue + delimiter + newValue;
                picaHashMap.put(key, newValue);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("createOrUpdatePicaSubField() - delimiter= " + delimiter);
            logger.debug("createOrUpdatePicaSubField() - oldValue= " + oldValue);
            logger.debug("createOrUpdatePicaSubField() - newValue= " + newValue);
            logger.debug("createOrUpdatePicaSubField() - end");
        }
    }

    /**
	 * @param file
	 *            Dateiname
	 * @return Dateiinhalt als Byte-Array oder null
	 */
    public static byte[] fileToByteArray(File file) {
        if (logger.isDebugEnabled()) {
            logger.debug("fileToByteArray() - start");
        }
        byte buffer[] = null;
        try {
            FileInputStream in = new FileInputStream(file);
            int fl = (int) file.length();
            buffer = new byte[fl];
            @SuppressWarnings("unused") int len = in.read(buffer, 0, fl);
            if (logger.isDebugEnabled()) {
                logger.debug("fileToByteArray() - file '" + file.getName() + "' transformed to byte array");
            }
        } catch (Exception e) {
            logger.error("Error: Can not transform file '" + file.getName() + "' to byte array", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("fileToByteArray() - end");
        }
        return buffer;
    }

    /**
	 * <p>
	 * Den übergebenen Wert einer HashMap hinzufügen oder einen bereits
	 * vorhandenen Wert ergänzen.
	 * </p>
	 * 
	 * @param hashMap
	 *            Die HashMap, welche den Wert enthält.
	 * @param key
	 *            Der Schlüssel, welcher den Wert adressiert.
	 * @param value
	 *            Der neue Wert.
	 * @param delimiter
	 *            Das Begrenzungszeichen, welches zwischen altem und neuem Wert
	 *            steht.
	 * @author kleinm, German National Library
	 */
    public static void addValueToHashMap(HashMap<String, String> hashMap, String key, String value, String delimiter) {
        if (logger.isDebugEnabled()) {
            logger.debug("addValueToHashMap() - start");
        }
        if (value != null && value.length() > 0) {
            if (hashMap != null) {
                if (hashMap.size() == 0) {
                    if (key != null) hashMap.put(key, value);
                } else {
                    if (delimiter == null) delimiter = new String();
                    if (hashMap.get(key) != null) {
                        hashMap.put(key, (hashMap.get(key) + delimiter + value).trim());
                    } else {
                        hashMap.put(key, (value.toString()).trim());
                    }
                }
            } else {
                logger.error("addValueToHashMap() - hashMap= null");
            }
        } else {
            logger.warn("addValueToHashMap() - not adding value to hashMap " + "because value has no content");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("addValueToHashMap() - end");
        }
    }

    /**
	 * <p>
	 * Wird ein Pica-Feld für Personennamen (028A/B/C) übergeben, werden die
	 * notwendigen Pica-Unterfelder aus der HashMap <code>personName</code>
	 * erzeugt, sofern Unterfeld $9 (VK-Nr.) nicht vorh. ist. Wird die
	 * Verfasserangabe (<code>authorStatement</code>) übergeben, wird diese
	 * ebenfalls mit den Daten aus der HashMap <code>personName</code> und dem
	 * String <code>authorStatementByRole</code> ergänzt.
	 * </p>
	 * 
	 * @param field028
	 *            Pica-Feld
	 * @param personName
	 *            HashMap mit Pesonendaten. Erwartet werden als Keys
	 *            <code>forename</code> (Vorname), <code>prefix</code>
	 *            (Namenspräfix), <code>surname</code> (Nachname) oder
	 *            <code>givenName</code> (persönlicher Name)
	 * @param authorStatement
	 *            Zu ergänzende Verfasserangabe als HashMap. Keys sind die
	 *            einleitenden Wendungen der Verfasserangabe.
	 * @param authorStatementByRole
	 *            Einleitende Wendung für den Eintrag in der Verfasserangabe
	 * @author kleinm, German National Library
	 */
    public static void convertPerson(PicaField field028, HashMap<String, String> personName, HashMap<String, String> authorStatement, String authorStatementByRole) {
        if (logger.isDebugEnabled()) {
            logger.debug("convertPerson() - start");
            logger.debug("convertPerson() - field028.hasSubfield('9')= " + field028.hasSubfield("9"));
        }
        String forename = personName.get(PERS_FORENAME);
        String prefix = personName.get(PERS_PREFIX);
        String surname = personName.get(PERS_SURNAME);
        String givenName = personName.get(PERS_GIVEN_NAME);
        if (logger.isDebugEnabled()) {
            logger.debug("convertPerson() - forename= " + forename);
            logger.debug("convertPerson() - prefix= " + prefix);
            logger.debug("convertPerson() - surname= " + surname);
            logger.debug("convertPerson() - givenName= " + givenName);
        }
        StringBuffer buffer = new StringBuffer();
        if (!isNullOrEmpty(givenName)) {
            givenName = givenName.trim();
            if (field028 != null && !field028.hasSubfield("9")) {
                field028.addNewSubfield("5", givenName);
            }
            buffer.append(givenName);
        } else if (!isNullOrEmpty(forename) || !isNullOrEmpty(surname)) {
            if (!isNullOrEmpty(forename)) {
                forename = forename.trim();
            }
            if (!isNullOrEmpty(prefix)) {
                prefix = prefix.trim();
            }
            if (!isNullOrEmpty(surname)) {
                surname = surname.trim();
            } else if (logger.isDebugEnabled()) {
                logger.error("convertPerson() - missing surname");
            }
            if (!isNullOrEmpty(forename)) {
                if (field028 != null && !field028.hasSubfield("9")) {
                    field028.addNewSubfield("d", forename);
                }
                buffer.append(forename);
            }
            if (!isNullOrEmpty(prefix)) {
                if (field028 != null && !field028.hasSubfield("9")) {
                    field028.addNewSubfield("c", prefix);
                }
                if (!isNullOrEmpty(forename)) buffer.append(CHAR_BLANK);
                buffer.append(prefix);
            }
            if (!isNullOrEmpty(surname)) {
                if (field028 != null && !field028.hasSubfield("9")) {
                    field028.addNewSubfield("a", surname);
                }
                if (!isNullOrEmpty(forename) || !isNullOrEmpty(prefix)) buffer.append(CHAR_BLANK);
                buffer.append(surname);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convertPerson() - authorStatementByRole= " + authorStatementByRole);
            logger.debug("convertPerson() - buffer= " + buffer);
        }
        if (authorStatement != null && buffer.length() > 0) {
            if (!isNullOrEmpty(authorStatementByRole)) {
                addValueToHashMap(authorStatement, authorStatementByRole, buffer.toString(), CHAR_BLANK_SEMICOLON_BLANK);
            } else {
                addValueToHashMap(authorStatement, EMPTY_KEY, buffer.toString(), CHAR_BLANK_SEMICOLON_BLANK);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("convertPerson() - authorStatement.size()= " + authorStatement.size());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convertPerson() - authorStatement= " + authorStatement);
            logger.debug("convertPerson() - end");
        }
    }

    /**
	 * <p>
	 * Prüft, ob in einem Pica-Feld Unterfelder vorhanden sind.
	 * 
	 * @param field
	 *            Pica-Feld
	 * @return true oder false
	 * @author kleinm, German National Library
	 */
    public static boolean hasSubfields(PicaField field) {
        if (logger.isDebugEnabled()) {
            logger.debug("hasSubfields() - start");
            logger.debug("hasSubfields() - field.name= " + field.getName());
        }
        boolean ret = false;
        if (field != null && field.getSubfields().size() > 0) {
            ret = true;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hasSubfields() - ret= " + ret);
            logger.debug("hasSubfields() - end");
        }
        return ret;
    }

    /**
	 * <p>
	 * Ein Pica-Feld enthält ein nicht wiederholbares Unterfeld, welches
	 * überschrieben werden soll.
	 * </p>
	 * <p>
	 * Anwendungsbeispiel: replacePicaSubfield(picaRecord.selectField("021A"),
	 * "a", "Frohe Ostern!");
	 * <p>
	 * 
	 * @param field
	 *            Pica-Feld, dessen Unterfeld einen neuen Wert erhalten soll
	 * @param subfield
	 *            Unterfeld, welches einen neuen Wert erhalten soll
	 * @param newValue
	 *            Der neue Wert für das Unterfeld
	 * @author kleinm, German National Library
	 */
    @SuppressWarnings("unchecked")
    public static void replacePicaSubfield(PicaField field, String subfield, String newValue) {
        if (!isNullOrEmpty(newValue) && field != null && field.hasSubfield(subfield)) {
            List<PicaSubfield> picaSubfields = field.selectSubfields(subfield);
            if (picaSubfields != null && picaSubfields.size() > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("replacePicaSubfield() - picaSubfields= " + picaSubfields);
                }
                PicaSubfield picaSubfield = picaSubfields.get(0);
                PicaSubfield newPicaSubfield = new PicaSubfield(subfield);
                newPicaSubfield.setContent(newValue);
                field.overwriteSubfield(picaSubfield, newPicaSubfield);
            }
        }
    }

    /**
	 * <p>
	 * Wird ein Pica-Feld für Körperschaften (029F...) übergeben, werden die
	 * notwendigen Pica-Unterfelder aus der HashMap <code>corporation</code>
	 * erzeugt, sofern Unterfeld $6 (VK-Nr.) nicht vorh. ist. Wird die
	 * Verfasserangabe (<code>authorStatement</code>) übergeben, wird diese
	 * ebenfalls mit den Daten aus der HashMap <code>corporation</code> und dem
	 * String <code>authorStatementByRole</code> ergänzt.
	 * </p>
	 * 
	 * @param field
	 *            Pica-Feld für Körperschaften (029F...)
	 * @param corporation
	 *            HashMap mit Körperschaftsdaten. Erwartet werden als Keys
	 *            <code>name, places</code>)
	 * @param authorStatement
	 *            Zu ergänzende Verfasserangabe als HashMap. Keys sind die
	 *            einleitenden Wendungen der Verfasserangabe.
	 * @param authorStatementByRole
	 *            Einleitende Wendung für den Eintrag in der Verfasserangabe
	 * @author kleinm, German National Library
	 */
    public static void convertCorporation(PicaField field, HashMap<String, String> corporation, HashMap<String, String> authorStatement, String authorStatementByRole) {
        if (logger.isDebugEnabled()) {
            logger.debug("convertCorporation() - start");
            if (field != null) {
                logger.debug("convertCorporation() - field.hasSubfield('6')= " + field.hasSubfield("6"));
            }
        }
        String name = null;
        String places = null;
        if (corporation != null && corporation.size() > 0) {
            if (corporation.get(CORP_NAME) != null) name = corporation.get(CORP_NAME).trim();
            if (corporation.get(CORP_PLACES) != null) places = corporation.get(CORP_PLACES).trim();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convertCorporation() - name= " + name);
            logger.debug("convertCorporation() - places= " + places);
        }
        StringBuffer buffer = new StringBuffer();
        if (!isNullOrEmpty(name)) {
            if (field != null && !field.hasSubfield("6")) {
                field.addNewSubfield("a", name);
            }
            buffer.append(name);
            if (!isNullOrEmpty(places)) {
                if (field != null && !field.hasSubfield("6")) {
                    field.addNewSubfield("c", places);
                }
                if (!isNullOrEmpty(name)) buffer.append(CHAR_BLANK);
                buffer.append(places.replace(CHAR_SEMICOLON_BLANK, CHAR_COMMA_BLANK));
            }
        } else if (logger.isDebugEnabled()) {
            logger.error("convertCorporation() - missing name of corporation");
        }
        if (authorStatement != null && buffer.length() > 0) {
            if (!isNullOrEmpty(authorStatementByRole)) {
                addValueToHashMap(authorStatement, authorStatementByRole, buffer.toString(), CHAR_BLANK_SEMICOLON_BLANK);
            } else {
                addValueToHashMap(authorStatement, EMPTY_KEY, buffer.toString(), CHAR_BLANK_SEMICOLON_BLANK);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("convertCorporation() - authorStatement.size()= " + authorStatement.size());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convertCorporation() - authorStatement= " + authorStatement);
            logger.debug("convertCorporation() - end");
        }
    }

    /**
	 * <p>
	 * Eine neue Fußnote wird in Pica-Feld 037A erzeugt. Existiert das Pica-Feld
	 * noch nicht, wird es neu angelegt und dem Pica-Record hinzugefügt.
	 * </p>
	 * 
	 * @param footnote
	 *            Fußnote
	 * @param record
	 *            Pica-Record
	 * @author t_kleinm, German National Library
	 */
    public static void newFootnote(String footnote, PicaRecord record) {
        PicaField field037A = record.selectField("037A");
        if (field037A == null) {
            field037A = record.addNewField("037A");
            field037A.addNewSubfield("a", footnote);
        } else {
            updatePicaSubfield(field037A, "a", footnote, CHAR_POINT_BLANK_HYPHEN_BLANK);
        }
    }

    /**
	 * <p>
	 * Erzeugt den Hochschulschriftenvermerk.
	 * </p>
	 * 
	 * @param universityPlace
	 *            Hochschulort
	 * @param universityName
	 *            Name der Hochschule
	 * @param thesisType
	 *            Art der Hochschulschrift
	 * @param thesisYear
	 *            Prüfungsjahr (auch Datumsangabe)
	 * @return Hochschulschriftenvermerk
	 * @author kleinm, German National Library
	 */
    public static String getThesisStatement(String universityPlace, String universityName, String thesisType, String thesisYear) {
        if (logger.isDebugEnabled()) {
            logger.debug("getThesisStatement() - start");
        }
        StringBuffer buffer = new StringBuffer();
        if (!isNullOrEmpty(universityPlace)) {
            universityPlace = universityPlace.trim();
            universityPlace = universityPlace.replace(CHAR_SEMICOLON_BLANK, CHAR_COMMA_BLANK);
            if (!isNullOrEmpty(universityPlace)) buffer.append(universityPlace);
        }
        if (!isNullOrEmpty(universityName)) {
            universityName = universityName.trim();
            if (buffer.length() > 0) buffer.append(CHAR_COMMA_BLANK);
            buffer.append(universityName);
        }
        if (!isNullOrEmpty(thesisType)) {
            thesisType = thesisType.trim();
            if (buffer.length() > 0) buffer.append(CHAR_COMMA_BLANK);
            buffer.append(thesisType);
        }
        if (!isNullOrEmpty(thesisYear)) {
            thesisYear = thesisYear.trim();
            thesisYear = findPattern(thesisYear, ".*(\\d\\d\\d\\d).*");
            if (buffer.length() > 0) buffer.append(CHAR_COMMA_BLANK);
            buffer.append(thesisYear);
        }
        String ret = buffer.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("getThesisStatement() - ret= " + ret);
            logger.debug("getThesisStatement() - end");
        }
        return ret;
    }

    /**
	 * <p>
	 * Sucht in einer Zeichenkette mit einem regulären Ausdruck (z.B.
	 * Jahresangabe: .*(\\d\\d\\d\\d).*).
	 * </p>
	 * 
	 * @param str
	 *            zu durchsuchende Zeichenkette
	 * @param regEx
	 *            regulärer Ausdruck
	 * @return Treffer oder null
	 * @author t_kleinm, German National Library
	 */
    public static String findPattern(String str, String regEx) {
        if (logger.isDebugEnabled()) {
            logger.debug("findPattern() - start");
            logger.debug("findPattern() - str= " + str);
            logger.debug("findPattern() - regEx= " + regEx);
        }
        String hit = null;
        if (str != null && regEx != null) {
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str);
            if (m.matches()) hit = m.group(1);
            if (logger.isDebugEnabled()) {
                logger.debug("findPattern() - hit= " + hit);
                logger.debug("findPattern() - end");
            }
        }
        return hit;
    }

    /**
	 * <p>
	 * Fügt einem Pica-Record ein Pica-Feld hinzu, aber nur, wenn es auch
	 * Unterfelder besitzt.
	 * </p>
	 * 
	 * @param picaField
	 *            Pica-Feld
	 * @param record
	 *            Pica-Record
	 * @author t_kleinm, German National Library
	 */
    public static void addPicaField(PicaField picaField, PicaRecord record) {
        if (record != null && picaField != null && hasSubfields(picaField)) {
            record.addField(picaField);
        }
    }

    /**
	 * <p>
	 * Ausgehend vom aktuellen Datum wird abhängig von der übergebenen
	 * Monatsangabe ein zukünftiges Datum berechnet und zurückgegeben.
	 * </p>
	 * 
	 * @param months
	 *            Anzahl der Monate
	 * @return zukünftiges Datum
	 * @author t_kleinm, German National Library
	 */
    public static Date getFutureDate(int months) {
        if (logger.isDebugEnabled()) {
            logger.debug("getFutureDate() - start");
        }
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, months);
        Date future = calendar.getTime();
        if (logger.isDebugEnabled()) {
            logger.debug("getFutureDate() - now= " + now);
            logger.debug("getFutureDate() - future= " + future);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getFutureDate() - end");
        }
        return future;
    }

    /**
	 * <p>
	 * Wandelt eine Datumsangabe von String in Date um, sofern es gültig ist.
	 * </p>
	 * 
	 * @param date
	 *            Datum als String
	 * @param dateFormat
	 *            Datumsformat
	 * @return Datum als Date oder null
	 * @author t_kleinm, German National Library
	 */
    public static Date getDateFromString(String date, SimpleDateFormat dateFormat) {
        if (logger.isDebugEnabled()) {
            logger.debug("getDateFromString() - start");
            logger.debug("getDateFromString() - date= " + date);
            logger.debug("getDateFromString() - dateFormat.toPattern()= " + dateFormat.toPattern());
        }
        Date parsedDate = null;
        if (!isNullOrEmpty(date) && dateFormat != null) {
            dateFormat.setLenient(false);
            try {
                parsedDate = dateFormat.parse(date);
                if (logger.isDebugEnabled()) {
                    logger.debug("getDateFromString() - " + date + " is valid");
                }
            } catch (ParseException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getDateFromString() - " + date + " is unvalid");
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getDateFromString() - parsedDate= " + parsedDate);
            logger.debug("getDateFromString() - end");
        }
        return parsedDate;
    }

    /**
	 * <p>
	 * Prüft, ob eine Datumsangabe einem bestimmten Datumsformat entspricht.
	 * </p>
	 * 
	 * @param date
	 *            zu prüfendes Datum
	 * @param dateFormat
	 *            Datumsformat
	 * @return true oder false
	 */
    public static boolean isValidDate(String date, SimpleDateFormat dateFormat) {
        if (logger.isDebugEnabled()) {
            logger.debug("isValidDate() - start");
            logger.debug("isValidDate() - date= " + date);
            logger.debug("isValidDate() - dateFormat.toPattern()= " + dateFormat.toPattern());
        }
        boolean isValidDate = false;
        Date parsedDate = null;
        if (!isNullOrEmpty(date) && dateFormat != null) {
            dateFormat.setLenient(false);
            try {
                parsedDate = dateFormat.parse(date);
                isValidDate = true;
                if (logger.isDebugEnabled()) {
                    logger.debug("isValidDate() - " + date + " is valid");
                }
            } catch (ParseException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("isValidDate() - " + date + " is unvalid");
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("isValidDate() - parsedDate= " + parsedDate);
            logger.debug("isValidDate() - isValidDate= " + isValidDate);
            logger.debug("isValidDate() - end");
        }
        return isValidDate;
    }

    /**
	 * <p>
	 * Prüft, ob es sich bei einer Jahresangabe um ein korrektes
	 * Erscheinungsjahr handelt.
	 * </p>
	 * 
	 * @param year
	 *            zu prüfendes Jahr (vierstellig)
	 * 
	 * @return true oder false
	 */
    public static boolean isValidYearOfPublication(String year) {
        if (logger.isDebugEnabled()) {
            logger.debug("isValidYearOfPublication() - start");
            logger.debug("isValidYearOfPublication() - year= " + year);
        }
        boolean isValidYear = false;
        if (!isNullOrEmpty(year) && year.length() == 4) {
            SimpleDateFormat df_yyyy = new SimpleDateFormat("yyyy");
            if (isValidDate(year, df_yyyy)) {
                Pattern p = Pattern.compile("\\d\\d\\d\\d");
                Matcher m = p.matcher(year);
                if (m.matches()) {
                    isValidYear = true;
                } else if (logger.isDebugEnabled()) {
                    logger.debug("isValidYearOfPublication() - 4 digits are necessary");
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("isValidYearOfPublication() - isValidYear= " + isValidYear);
            logger.debug("isValidYearOfPublication() - end");
        }
        return isValidYear;
    }

    /**
	 * <p>
	 * Die HashMap enthält die Verfasserangabe. Keys sind die einleitende
	 * Wendungen der Verfasserangabe.
	 * </p>
	 * 
	 * @param hashMap
	 *            Die HashMap, welche die Verfasserangabe enthält.
	 * @return Verfasserangabe als String
	 * @author kleinm, German National Library
	 */
    public static String getAuthorStatement(HashMap<String, String> hashMap) {
        if (logger.isDebugEnabled()) {
            logger.debug("getAuthorStatement() - start");
        }
        StringBuffer authorStatement = null;
        if (hashMap != null && hashMap.size() > 0) {
            authorStatement = new StringBuffer();
            for (Map.Entry<String, String> e : hashMap.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();
                if (logger.isDebugEnabled()) {
                    logger.debug("getAuthorStatement() - hashMap: key= " + key + ", value=" + value);
                }
                if (!isNullOrEmpty(value)) {
                    if (authorStatement.length() > 0) {
                        authorStatement.append(CHAR_POINT_BLANK);
                    }
                    if (!key.equals(EMPTY_KEY)) {
                        authorStatement.append(key);
                        authorStatement.append(CHAR_BLANK);
                    }
                    authorStatement.append(value);
                }
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("getAuthorStatement() - hashMap= null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getAuthorStatement() - authorStatement= " + authorStatement);
            logger.debug("getAuthorStatement() - end");
        }
        return authorStatement.toString();
    }

    /**
	 * <p>
	 * Durchsucht den Text nach Wörtern, welche nur ein Zeichen lang sind. Diese
	 * werden durch einen Abkürzungspunkt ergänzt.
	 * </p>
	 * 
	 * @param text
	 *            zu durchsuchender Text
	 * @return Text mit Abkürzungspunkten
	 * @author t_kleinm, German National Library
	 */
    public static String addAbbreviationPoints(String text) {
        if (logger.isDebugEnabled()) {
            logger.debug("addAbbreviationPoints() - start");
            logger.debug("updatePicaSubfield() - before adding: text= " + text);
        }
        if (!isNullOrEmpty(text)) {
            StringBuffer buffer = new StringBuffer();
            buffer = new StringBuffer();
            String[] textArr = text.split(CHAR_BLANK);
            for (int i = 0; i < textArr.length; i++) {
                buffer.append(textArr[i].trim());
                if (textArr[i].length() == 1) {
                    buffer.append(CHAR_POINT);
                }
                buffer.append(CHAR_BLANK);
            }
            text = buffer.toString().trim();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("updatePicaSubfield() - after adding: text= " + text);
            logger.debug("addAbbreviationPoints() - end");
        }
        return text;
    }

    /**
	 * <p>
	 * Verarbeitet DNB-Sachgruppen (DDC-Notationen) für Pica-Feld 045E. Ist die
	 * Haupt-Sachgruppe ($e) wird der Wert in das Unterfeld $f überführt.
	 * Enthält die Sachgruppe ein Semikolon (z.B. 800;B), gelangt der Wert nach
	 * dem Semikolon ebenfalls in das Unterfeld $f.
	 * </p>
	 * 
	 * @param ddcNotation
	 *            DDC-Sachgruppe
	 * @param field045E
	 *            Pica-Feld 045E
	 */
    public static void convertDnbSubjectCategory(String ddcNotation, PicaField field045E) {
        if (logger.isDebugEnabled()) {
            logger.debug("convertDnbSubjectCategory() - start");
            logger.debug("convertDnbSubjectCategory() - ddcNotation= " + ddcNotation);
        }
        if (field045E != null && ddcNotation != null && ddcNotation.length() > 0) {
            if (ddcNotation.contains(";")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("convertDnbSubjectCategory() - semicolon found");
                }
                int lastIndex = ddcNotation.lastIndexOf(";");
                if (logger.isDebugEnabled()) {
                    logger.debug("convertDnbSubjectCategory() - lastIndex= " + lastIndex);
                }
                String field045E$e = ddcNotation.substring(0, lastIndex);
                String field045E$f = ddcNotation.substring(lastIndex + 1, ddcNotation.length());
                if (logger.isDebugEnabled()) {
                    logger.debug("convertDnbSubjectCategory() - field045E$e= " + field045E$e);
                    logger.debug("convertDnbSubjectCategory() - field045E$f= " + field045E$f);
                }
                if (!ToPicaPlusUtil.isNullOrEmpty(field045E$e)) {
                    if (!field045E.hasSubfield("e")) {
                        field045E.addNewSubfield("e", field045E$e);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("convertDnbSubjectCategory() - " + "subfield $e is already existing");
                        }
                        field045E.addNewSubfield("f", field045E$e);
                    }
                }
                if (!ToPicaPlusUtil.isNullOrEmpty(field045E$f)) {
                    field045E.addNewSubfield("f", field045E$f);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("convertDnbSubjectCategory() - no semicolon found");
                }
                if (!field045E.hasSubfield("e")) {
                    field045E.addNewSubfield("e", ddcNotation);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("convertDnbSubjectCategory() - " + "subfield $e is already existing");
                    }
                    field045E.addNewSubfield("f", ddcNotation);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convertDnbSubjectCategory() - end");
        }
    }
}
