package org.gbif.portal.harvest.workflow.activity.occurrence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.portal.dao.CatalogueNumberDAO;
import org.gbif.portal.dao.CellCountryDAO;
import org.gbif.portal.dao.CollectionCodeDAO;
import org.gbif.portal.dao.CountryDAO;
import org.gbif.portal.dao.DataResourceDAO;
import org.gbif.portal.dao.IdentifierRecordDAO;
import org.gbif.portal.dao.ImageRecordDAO;
import org.gbif.portal.dao.InstitutionCodeDAO;
import org.gbif.portal.dao.LinkRecordDAO;
import org.gbif.portal.dao.OccurrenceRecordDAO;
import org.gbif.portal.dao.TaxonNameDAO;
import org.gbif.portal.dao.TypificationRecordDAO;
import org.gbif.portal.harvest.taxonomy.ScientificNameParser;
import org.gbif.portal.model.CellCountry;
import org.gbif.portal.model.Country;
import org.gbif.portal.model.DataResource;
import org.gbif.portal.model.ImageRecord;
import org.gbif.portal.model.LinkRecord;
import org.gbif.portal.model.OccurrenceRecord;
import org.gbif.portal.model.RawOccurrenceRecord;
import org.gbif.portal.model.TaxonName;
import org.gbif.portal.model.TypificationRecord;
import org.gbif.portal.util.db.OccurrenceRecordUtils;
import org.gbif.portal.util.geospatial.CellIdUtils;
import org.gbif.portal.util.geospatial.UnableToGenerateCellIdException;
import org.gbif.portal.util.log.GbifLogMessage;
import org.gbif.portal.util.log.GbifLogUtils;
import org.gbif.portal.util.log.LogEvent;
import org.gbif.portal.util.mapping.CodeMapping;
import org.gbif.portal.util.workflow.Activity;
import org.gbif.portal.util.workflow.BaseActivity;
import org.gbif.portal.util.workflow.ProcessContext;

/**
 * An activity that will create an OccurrenceRecord with the values found in the 
 * context and synchronise it with the DB
 * @author trobertson
 */
public class OccurrenceRecordSynchroniserActivity extends BaseActivity implements Activity {

    /**
	 * Unknown basis of record
	 */
    public static int UNKNOWN_BASIS_OF_RECORD = 0;

    /**
	 * Context Keys
	 */
    protected String contextKeyRawOccurrenceRecord;

    protected String contextKeyTaxonConceptId;

    protected String contextKeyTaxonNameId;

    protected String contextKeyLogGroup;

    protected GbifLogUtils gbifLogUtils;

    protected ScientificNameParser scientificNameParser;

    protected String contextKeySkipLinkRecord = "skipLinkRecord";

    protected String contextKeySkipTypificationRecord = "skipTypificationRecord";

    protected String contextKeySkipImageRecord = "skipImageRecord";

    protected String contextKeyCount = "recordProcessedCount";

    protected String contextKeyAddedCount = "recordAddedCount";

    protected String contextKeyUpdatedCount = "recordUpdatedCount";

    /** 
	 * The min value that will be synced with OR  
	 */
    protected int minRecordDepthInCentimetres = 0;

    /** 
	 * The max value that will be synced with OR - tied to the data type for occurrence_record.depth_centimetres 
	 */
    protected int maxRecordDepthInCentimetres = 16777215;

    /** 
	 * The highest depth value recognised as valid 
	 */
    protected int outOfRangeDepth = 10000;

    /** 
	 * The lowest altitude value recognised as valid 
	 */
    protected int outOfRangeMinAltitude = -100;

    /** 
	 * The highest altitude value recognised as valid 
	 */
    protected int outOfRangeMaxAltitude = 10000;

    /** 
	 * The max value that will be synced with OR - tied to the data type for occurrence_record.altitude_metres 
	 */
    protected int maxToRecordAltitudeInMetres = 32767;

    /** 
	 * The lowest value that will be synced with OR  
	 */
    protected int minToRecordAltitudeInMetres = -32768;

    /**
	 * Maps the basis of record strings to standard codes
	 */
    protected CodeMapping basisOfRecordMapping = null;

    /**
	 * Maps the image type strings to standard codes
	 */
    protected CodeMapping imageTypeMapping = null;

    /**
	 * The ISO country mapping
	 */
    protected Map<String, String> isoCountryCodeMap = new HashMap<String, String>();

    /**
	 * The pattern for the html link catalogue number
	 */
    protected Pattern pattern = Pattern.compile("<a href=\"(.+)\">(.+)</a>");

    /**
	 * Pattern for removing measurement denominations 
	 */
    protected Pattern measureMarkerPattern = Pattern.compile(".*[a-zA-Z].*");

    /**
	 * Pattern for recognising measurements in feet
	 */
    protected Pattern feetMarkerPattern = Pattern.compile(".*ft.*|.*FT.*|.*'.*");

    /**
	 * Pattern for recognising measurements in inches
	 */
    protected Pattern inchesMarkerPattern = Pattern.compile(".*in.*|.*\".*");

    /**
	 * Pattern for recognising a range value
	 */
    protected Pattern sepMarkerPattern = Pattern.compile("\\d-.*");

    /**
	 * Pattern for recognising an angular measurement i.e degrees, minutes, seconds
	 * as opposed to decimal degrees
	 */
    protected Pattern coordinateMarkerPattern = Pattern.compile(".*'.*|.*\".*|.*[EWNSewns].*");

    /**
	 * DAOs
	 */
    protected OccurrenceRecordDAO occurrenceRecordDAO;

    protected InstitutionCodeDAO institutionCodeDAO;

    protected CollectionCodeDAO collectionCodeDAO;

    protected CatalogueNumberDAO catalogueNumberDAO;

    protected DataResourceDAO dataResourceDAO;

    protected CellCountryDAO cellCountryDAO;

    protected CountryDAO countryDAO;

    protected ImageRecordDAO imageRecordDAO;

    protected LinkRecordDAO linkRecordDAO;

    protected IdentifierRecordDAO identifierRecordDAO;

    protected TypificationRecordDAO typificationRecordDAO;

    protected TaxonNameDAO taxonNameDAO;

    /**
	 * @see org.gbif.portal.util.workflow.Activity#execute(org.gbif.portal.util.workflow.ProcessContext)
	 */
    @SuppressWarnings("unchecked")
    public ProcessContext execute(ProcessContext context) throws Exception {
        RawOccurrenceRecord ror = (RawOccurrenceRecord) context.get(getContextKeyRawOccurrenceRecord(), RawOccurrenceRecord.class, true);
        if ((Long) context.get(getContextKeyTaxonConceptId(), Long.class, false) == null) {
            logger.warn("Skipping raw occurrence [ID: " + ror.getId() + "] as no taxon concept id found for it.");
            return context;
        }
        OccurrenceRecord or = occurrenceRecordDAO.getById(ror.getId());
        OccurrenceRecord clonedOr = null;
        boolean create = true;
        if (or != null && or.getId() > 0) {
            create = false;
            clonedOr = (OccurrenceRecord) BeanUtils.cloneBean(or);
            logger.debug("Updating OccurrenceRecord[" + or.getId() + "]");
            resetIssues(or);
        } else {
            logger.debug("Creating new OccurrenceRecord");
            or = new OccurrenceRecord();
            or.setId(ror.getId());
        }
        or.setDataProviderId(ror.getDataProviderId());
        or.setDataResourceId(ror.getDataResourceId());
        String institutionCode = StringUtils.trimToEmpty(ror.getInstitutionCode());
        String collectionCode = StringUtils.trimToEmpty(ror.getCollectionCode());
        long institutionCodeId = institutionCodeDAO.createIfNotExists(institutionCode);
        long collectionCodeId = collectionCodeDAO.createIfNotExists(collectionCode);
        or.setInstitutionCodeId(institutionCodeId);
        or.setCollectionCodeId(collectionCodeId);
        String catalogueNumber = StringUtils.trimToEmpty(ror.getCatalogueNumber());
        String linkForCatalogueNumber = null;
        if (catalogueNumber == null) {
            or.setOtherIssueBits(OccurrenceRecordUtils.OTHER_MISSING_CATALOGUE_NUMBER);
        } else {
            linkForCatalogueNumber = validateCatalogueNumber(catalogueNumber, or);
        }
        or.setTaxonConceptId((Long) context.get(getContextKeyTaxonConceptId(), Long.class, false));
        or.setTaxonNameId((Long) context.get(getContextKeyTaxonNameId(), Long.class, false));
        String bor = ror.getBasisOfRecord();
        if (bor != null) {
            or.setBasisOfRecord(basisOfRecordMapping.mapToCode(bor));
        } else {
            DataResource dr = dataResourceDAO.getById(or.getDataResourceId());
            if (dr.getBasisOfRecord() != 0) {
                or.setBasisOfRecord(dr.getBasisOfRecord());
            }
        }
        if (or.getBasisOfRecord() == UNKNOWN_BASIS_OF_RECORD) {
            or.setOtherIssueBits(OccurrenceRecordUtils.OTHER_MISSING_BASIS_OF_RECORD);
        }
        or.setIsoCountryCode(getIsoCode(context, ror.getCountry()));
        setLatLong(context, ror, or);
        setAltitudeInMetres(context, ror, or);
        setDepthInCentimetres(context, ror, or);
        setYearMonthDay(context, ror, or);
        or.setModified(new Date());
        if (create) {
            occurrenceRecordDAO.create(or);
        } else {
            occurrenceRecordDAO.update(or);
        }
        incrementCount(context, contextKeyCount);
        if (clonedOr == null) {
            incrementCount(context, contextKeyAddedCount);
        } else if (!or.equals(clonedOr)) {
            incrementCount(context, contextKeyUpdatedCount);
        }
        synchroniseImages(context, ror, or);
        synchroniseLinks(context, ror, or, linkForCatalogueNumber);
        synchroniseTypifications(context, ror, or);
        logIssues(context, ror, or);
        return context;
    }

    /**
	 * Reset the issues for this record.
	 * 
	 * @param or
	 */
    private void resetIssues(OccurrenceRecord or) {
        or.setGeospatialIssue(OccurrenceRecordUtils.NO_ISSUES);
        or.setTaxonomicIssue(OccurrenceRecordUtils.NO_ISSUES);
        or.setOtherIssue(OccurrenceRecordUtils.NO_ISSUES);
    }

    /**
	 * Log issues with this record.
	 * 
	 * @param context
	 * @param ror
	 * @param or
	 */
    private void logIssues(ProcessContext context, RawOccurrenceRecord ror, OccurrenceRecord or) {
        String bor = ror.getBasisOfRecord();
        if (or.getBasisOfRecord() == UNKNOWN_BASIS_OF_RECORD) {
            GbifLogMessage message = null;
            if (bor != null) message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_BASISOFRECORDPARSEISSUE, bor + ": basis of record not recognised "); else message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_BASISOFRECORDPARSEISSUE, "No basis of record provided");
            message.setCountOnly(true);
            logger.warn(message);
        }
        if (or.getIsoCountryCode() == null && ror.getCountry() != null) {
            GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_COUNTRYNAMEPARSEISSUE, ror.getCountry() + ": country name not matched");
            message.setCountOnly(true);
            logger.warn(message);
        }
        if (or.getGeospatialIssue() != OccurrenceRecordUtils.NO_ISSUES) {
            StringBuffer messageText = new StringBuffer();
            if (or.getIsoCountryCode() != null) {
                messageText.append(or.getIsoCountryCode());
                messageText.append(" ");
            }
            messageText.append("(Lat: ");
            messageText.append(ror.getLatitude());
            messageText.append(", Lon: ");
            messageText.append(ror.getLongitude());
            messageText.append("): ");
            messageText.append(OccurrenceRecordUtils.formatGeospatialIssue(or.getGeospatialIssue()));
            GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, messageText.toString());
            message.setOccurrenceId(or.getId());
            message.setCount(1);
            logger.error(message);
        }
    }

    /**
	 * Checks to see if there is a URL in the catalogue number and handles it appropriately
	 * @param catalogueNumber To check
	 * @return valid catalogue number
	 */
    private String validateCatalogueNumber(String catalogueNumber, OccurrenceRecord or) {
        String link = null;
        if (catalogueNumber != null) {
            Matcher m = pattern.matcher(catalogueNumber);
            if (m.matches()) {
                logger.debug("Catalogue number contains html link: " + catalogueNumber);
                catalogueNumber = m.group(2);
                link = m.group(1);
            }
            long catalogueNumberId = catalogueNumberDAO.createIfNotExists(catalogueNumber);
            or.setCatalogueNumberId(catalogueNumberId);
        }
        return link;
    }

    /**
	 * Set the altitude on this occurrence record
	 * 
	 * @param context
	 * @param ror
	 * @param or
	 */
    public void setAltitudeInMetres(ProcessContext context, RawOccurrenceRecord ror, OccurrenceRecord or) {
        String minAltitude = ror.getMinAltitude();
        String maxAltitude = ror.getMaxAltitude();
        String altitudePrecisionAsString = ror.getAltitudePrecision();
        Float minAltitudeAsFloat = null;
        Float maxAltitudeAsFloat = null;
        Integer gi = or.getGeospatialIssue();
        minAltitudeAsFloat = getAltitudeMeasurement(context, minAltitude, "min altitude", gi);
        maxAltitudeAsFloat = getAltitudeMeasurement(context, maxAltitude, "max altitude", gi);
        if (altitudePrecisionAsString != null) {
            altitudePrecisionAsString = removeMeasurementMarkers(altitudePrecisionAsString);
            Float altitudePrecision = getAltitudeMeasurement(context, altitudePrecisionAsString, "altitude precision", gi);
            try {
                if (altitudePrecision != null && minAltitudeAsFloat != null && maxAltitudeAsFloat != null) {
                    minAltitudeAsFloat -= altitudePrecision;
                    maxAltitudeAsFloat += altitudePrecision;
                }
            } catch (NumberFormatException e) {
                GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, "invalid or unparsable altitude precision");
                message.setCountOnly(true);
                logger.warn(message);
            }
        }
        or.setGeospatialIssue(gi);
        Integer altitudeAsInt = null;
        if (maxAltitudeAsFloat != null && minAltitudeAsFloat != null) {
            if ((maxAltitudeAsFloat + minAltitudeAsFloat) == 0) {
                altitudeAsInt = 0;
                or.setAltitudeInMetres(altitudeAsInt);
            } else if (maxAltitudeAsFloat == 0 && minAltitudeAsFloat > 0) {
                altitudeAsInt = Math.round(minAltitudeAsFloat);
                if (altitudeAsInt <= maxToRecordAltitudeInMetres && altitudeAsInt >= minToRecordAltitudeInMetres) {
                    or.setAltitudeInMetres(altitudeAsInt);
                }
            } else {
                Float altitude = (maxAltitudeAsFloat + minAltitudeAsFloat) / 2;
                altitudeAsInt = Math.round(altitude);
                if (altitudeAsInt <= maxToRecordAltitudeInMetres && altitudeAsInt >= minToRecordAltitudeInMetres) {
                    or.setAltitudeInMetres(altitudeAsInt);
                }
            }
        } else if (minAltitudeAsFloat != null) {
            altitudeAsInt = Math.round(minAltitudeAsFloat);
            if (altitudeAsInt <= maxToRecordAltitudeInMetres && altitudeAsInt >= minToRecordAltitudeInMetres) {
                or.setAltitudeInMetres(altitudeAsInt);
            }
        } else if (maxAltitudeAsFloat != null) {
            altitudeAsInt = Math.round(maxAltitudeAsFloat);
            if (altitudeAsInt <= maxToRecordAltitudeInMetres && altitudeAsInt >= minToRecordAltitudeInMetres) {
                or.setAltitudeInMetres(altitudeAsInt);
            }
        }
        if (altitudeAsInt != null && altitudeAsInt == 0) {
            GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, "number of records marker with altitude 0");
            message.setCountOnly(true);
            logger.warn(message);
        }
        if (altitudeAsInt != null && (altitudeAsInt > outOfRangeMaxAltitude || altitudeAsInt < outOfRangeMinAltitude)) {
            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_ALTITUDE_OUT_OF_RANGE);
            GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, "number of records with out of range altitude (>" + outOfRangeMaxAltitude + " or <" + outOfRangeMinAltitude + ")");
            message.setCountOnly(true);
            logger.warn(message);
        }
        if (altitudeAsInt != null && (altitudeAsInt == -9999 || altitudeAsInt == 9999)) {
            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_ERRONOUS_ALTITUDE);
            GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, "number of records with altitude -9999");
            message.setCountOnly(true);
            logger.warn(message);
        }
        if (minAltitudeAsFloat != null && maxAltitudeAsFloat != null && minAltitudeAsFloat > maxAltitudeAsFloat && maxAltitudeAsFloat != 0) {
            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_MIN_MAX_ALTITUDE_REVERSED);
            GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, "number of records with min and max altitude transposed");
            message.setCountOnly(true);
            logger.warn(message);
        }
    }

    /**
	 * Parses an altitude measurement.
	 * 
	 * @param context
	 * @param altitude
	 * @param fieldName
	 * @param geospatialIssues
	 * @return
	 */
    private Float getAltitudeMeasurement(ProcessContext context, String altitude, String fieldName, Integer geospatialIssues) {
        Float altitudeAsFloat = null;
        try {
            if (altitude != null) {
                Matcher altitudeMatcher = measureMarkerPattern.matcher(altitude);
                boolean containsNonnumeric = altitudeMatcher.matches();
                if (containsNonnumeric) {
                    geospatialIssues &= (OccurrenceRecordUtils.GEOSPATIAL_MASK ^ OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_ALTITUDE_NON_NUMERIC);
                    boolean isInFeet = feetMarkerPattern.matcher(altitude).matches();
                    boolean isInInches = inchesMarkerPattern.matcher(altitude).matches();
                    GbifLogMessage nonNumericMessage = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, fieldName + " contains non-numeric characters");
                    nonNumericMessage.setCountOnly(true);
                    logger.warn(nonNumericMessage);
                    if (sepMarkerPattern.matcher(altitude).matches()) {
                        try {
                            String min = altitude.substring(0, altitude.indexOf('-')).trim();
                            min = removeMeasurementMarkers(min);
                            String max = altitude.substring(altitude.indexOf('-') + 1).trim();
                            max = removeMeasurementMarkers(max);
                            GbifLogMessage rangeMessage = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, fieldName + " contains range supplied in single field");
                            rangeMessage.setCountOnly(true);
                            logger.warn(rangeMessage);
                            Float minFloat = Float.parseFloat(min);
                            Float maxFloat = Float.parseFloat(max);
                            if (minFloat != 0 && maxFloat != 0 && (maxFloat - minFloat) != 0) {
                                altitudeAsFloat = (maxFloat + minFloat) / 2;
                            }
                        } catch (NumberFormatException e) {
                        }
                    } else {
                        altitude = removeMeasurementMarkers(altitude);
                        altitudeAsFloat = Float.parseFloat(altitude);
                    }
                    if (altitudeAsFloat != null) {
                        if (isInFeet || isInInches) {
                            geospatialIssues &= (OccurrenceRecordUtils.GEOSPATIAL_MASK ^ OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_ALTITUDE_IN_FEET);
                        }
                        if (isInFeet) {
                            altitudeAsFloat = convertFeetToMetres(altitudeAsFloat);
                        } else if (isInInches) {
                            altitudeAsFloat = convertInchesToMetres(altitudeAsFloat);
                        }
                    }
                } else {
                    altitudeAsFloat = Float.parseFloat(altitude);
                }
            }
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
        }
        return altitudeAsFloat;
    }

    /**
	 * Remove "m" etc.
	 * 
	 * @param s
	 * @return
	 */
    public static String removeMeasurementMarkers(String s) {
        if (s == null) return null;
        s = s.replaceAll("[a-zA-Z\" \"\"]", "");
        return s;
    }

    /**
	 * Sets the Lat Long data
	 * @param ror Source
	 * @param or Target
	 */
    private void setLatLong(ProcessContext context, RawOccurrenceRecord ror, OccurrenceRecord or) {
        Float lat = null;
        Float lon = null;
        lat = getCoordinate(context, ror.getLatitude(), "latitude");
        lon = getCoordinate(context, ror.getLongitude(), "longitude");
        if (lon != null && lat != null) {
            if (lon == 0.0 && lat == 0.0) {
                logger.debug("Ignoring 0,0 Lat/Long");
                or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_ZERO_COORDINATES);
                lon = null;
                lat = null;
            } else if (lon < -180.0 || lon > 180.0 || lat < -90.0 || lat > 90.0) {
                if (lon >= -90.0 && lon <= 90.0 && lat >= -180.0 && lat <= 180.0) {
                    or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_INVERTED_COORDINATES);
                }
                logger.debug("Ignoring out-of-range coordinates");
                or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_COORDINATES_OUT_OF_RANGE);
                lon = null;
                lat = null;
            }
        }
        if (lat == null && lon != null) {
            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_COORDINATES_OUT_OF_RANGE);
            lon = null;
        } else if (lon == null && lat != null) {
            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_COORDINATES_OUT_OF_RANGE);
            lat = null;
        }
        or.setLatitude(lat);
        or.setLongitude(lon);
        try {
            int cellId = CellIdUtils.toCellId(lat, lon);
            int centiCellId = CellIdUtils.toCentiCellId(lat, lon);
            or.setCellId(cellId);
            or.setCentiCellId(centiCellId);
            or.setMod360CellId(cellId % 360);
        } catch (UnableToGenerateCellIdException e) {
            logger.debug(e.getMessage());
            or.setCellId(null);
            or.setMod360CellId(null);
            or.setCentiCellId(null);
        }
        if (or.getCellId() != null) {
            List<CellCountry> cellCountries = cellCountryDAO.getByCellId(or.getCellId());
            if (or.getIsoCountryCode() == null) {
                if (cellCountries.size() == 1) {
                    CellCountry cellCountry = cellCountries.get(0);
                    or.setIsoCountryCode(cellCountry.getIsoCountryCode());
                    or.setOtherIssueBits(OccurrenceRecordUtils.OTHER_COUNTRY_INFERRED_FROM_COORDINATES);
                }
            } else {
                boolean intersection = false;
                for (CellCountry cellCountry : cellCountries) {
                    if (cellCountry.getIsoCountryCode().equals(or.getIsoCountryCode())) {
                        intersection = true;
                        break;
                    }
                }
                if (!intersection) {
                    Country country = countryDAO.getByIsoCountryCode(or.getIsoCountryCode());
                    if (country != null) {
                        if (coordinatesWithinCountryBox(country, lat, lon)) {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_COUNTRY_COORDINATE_MISMATCH);
                        } else if (coordinatesWithinCountryBox(country, lat, -lon)) {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_NEGATED_LONGITUDE);
                        } else if (coordinatesWithinCountryBox(country, -lat, lon)) {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_NEGATED_LATITUDE);
                        } else if (coordinatesWithinCountryBox(country, -lat, -lon)) {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_NEGATED_LONGITUDE | OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_NEGATED_LATITUDE);
                        } else if (coordinatesWithinCountryBox(country, lon, lat)) {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_INVERTED_COORDINATES | OccurrenceRecordUtils.GEOSPATIAL_COUNTRY_COORDINATE_MISMATCH);
                        } else if (coordinatesWithinCountryBox(country, lon, -lat)) {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_INVERTED_COORDINATES | OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_NEGATED_LONGITUDE);
                        } else if (coordinatesWithinCountryBox(country, -lon, lat)) {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_INVERTED_COORDINATES | OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_NEGATED_LATITUDE);
                        } else if (coordinatesWithinCountryBox(country, -lon, -lat)) {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_INVERTED_COORDINATES | OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_NEGATED_LONGITUDE | OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_NEGATED_LATITUDE);
                        } else {
                            or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_COUNTRY_COORDINATE_MISMATCH);
                        }
                    } else {
                        or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_COUNTRY_COORDINATE_MISMATCH);
                    }
                }
            }
        }
    }

    /**
	 * Parse coordinate value logging problems if they exist.
	 * 
	 * @param context
	 * @param coordinateAsString
	 * @param fieldName
	 * @return
	 */
    private Float getCoordinate(ProcessContext context, String coordinateAsString, String fieldName) {
        if (coordinateAsString == null) return null;
        boolean validFormat = true;
        Float coordinate = null;
        try {
            if (coordinateAsString.contains(",")) {
                coordinateAsString = coordinateAsString.replaceAll(",", ".");
                validFormat = false;
            }
            coordinate = Float.parseFloat(coordinateAsString);
        } catch (NumberFormatException e) {
            validFormat = false;
        }
        if (coordinateMarkerPattern.matcher(coordinateAsString).matches()) {
            GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, fieldName + " given in degrees/minutes/seconds instead of decimal");
            message.setCountOnly(true);
            logger.warn(message);
        }
        if (!validFormat) {
            GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, "supplied" + fieldName + " of invalid format");
            message.setCountOnly(true);
            logger.warn(message);
        }
        return coordinate;
    }

    /**
	 * Sets the depth data
	 * @param ror Source
	 * @param or Target
	 */
    public void setDepthInCentimetres(ProcessContext context, RawOccurrenceRecord ror, OccurrenceRecord or) {
        Float depth = null;
        Integer gi = or.getGeospatialIssue();
        Float precision = getDepthMeasurement(context, ror.getDepthPrecision(), "depth precision", gi);
        Float min = getDepthMeasurement(context, ror.getMinDepth(), "min depth", gi);
        Float max = getDepthMeasurement(context, ror.getMaxDepth(), "max depth", gi);
        if (min != null || max != null) {
            if (min == null) min = max; else if (max == null) max = min;
            if (precision != null) {
                min -= precision;
                max += precision;
            }
            if (max + min != 0) {
                depth = (max + min) / 2;
            } else {
                depth = 0f;
            }
            if (depth != null) {
                int depthInCentimetres = Math.round(depth * 100);
                if (depthInCentimetres <= maxRecordDepthInCentimetres && depthInCentimetres >= minRecordDepthInCentimetres) {
                    or.setDepthInCentimetres(Math.round(depth * 100));
                }
            }
            if ((min != null || max != null) && min > max) {
                or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_MIN_MAX_DEPTH_REVERSED);
                GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, "min and max depth transposed");
                message.setCountOnly(true);
                logger.warn(message);
            }
            if (depth != null && depth > outOfRangeDepth) {
                or.setGeospatialIssueBits(OccurrenceRecordUtils.GEOSPATIAL_DEPTH_OUT_OF_RANGE);
                GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, "number of records with out of range depth (>" + outOfRangeDepth + ")");
                message.setCountOnly(true);
                logger.warn(message);
            }
        }
    }

    /**
	 * Synchronise image records
	 * @param ror
	 * @param or
	 */
    private void synchroniseImages(ProcessContext context, RawOccurrenceRecord ror, OccurrenceRecord or) throws Exception {
        logger.debug("Starting synchroniseImages");
        if (context.containsKey(contextKeySkipImageRecord) && (Boolean) context.get(contextKeySkipImageRecord, Boolean.class, false)) {
            logger.debug("Skipping image syncing");
            return;
        }
        List<ImageRecord> images = imageRecordDAO.findByOccurrenceId(ror.getId());
        logger.debug("Synchronising " + images.size() + " images");
        for (ImageRecord image : images) {
            if (image.getImageType() == OccurrenceRecordUtils.IMAGETYPE_UNKNOWN) {
                int imageType = imageTypeMapping.mapToCode(image.getRawImageType());
                image.setImageType(imageType);
            }
            image.setTaxonConceptId(or.getTaxonConceptId());
            imageRecordDAO.updateOrCreate(image);
        }
        logger.debug("Finished synchronising " + images.size() + " images");
    }

    /**
	 * Synchronise link records 
	 * @param ror
	 * @param or
	 * @param urlFromCatalogueNumber If this is not null, then it makes sure that this link exists
	 */
    private void synchroniseLinks(ProcessContext context, RawOccurrenceRecord ror, OccurrenceRecord or, String urlFromCatalogueNumber) throws Exception {
        List<LinkRecord> links = linkRecordDAO.findByOccurrenceId(ror.getId());
        boolean linkForCatalogueNumberRequired = false;
        if (urlFromCatalogueNumber != null) {
            linkForCatalogueNumberRequired = true;
        }
        if (!linkForCatalogueNumberRequired && context.containsKey(contextKeySkipLinkRecord) && (Boolean) context.get(contextKeySkipLinkRecord, Boolean.class, false)) {
            logger.debug("Skipping catalogue number syncing");
            return;
        }
        for (LinkRecord link : links) {
            link.setTaxonConceptId(or.getTaxonConceptId());
            link.setLinkType(OccurrenceRecordUtils.LINKTYPE_OCCURRENCEPAGE);
            linkRecordDAO.updateOrCreate(link);
            if (linkForCatalogueNumberRequired && link.getUrl().equals(urlFromCatalogueNumber)) {
                logger.debug("The link for the Catalogue number exists");
                linkForCatalogueNumberRequired = false;
            }
        }
        if (linkForCatalogueNumberRequired) {
            logger.debug("Creating new link for the URL in the catalogue number");
            LinkRecord link = new LinkRecord();
            link.setOccurrenceId(or.getId());
            link.setTaxonConceptId(or.getTaxonConceptId());
            link.setLinkType(OccurrenceRecordUtils.LINKTYPE_OCCURRENCEPAGE);
            link.setUrl(urlFromCatalogueNumber);
            link.setDataResourceId(ror.getDataResourceId());
            link.setDescription("Extracted from catalogue number");
            linkRecordDAO.create(link);
        }
    }

    /**
	 * Synchronise typification records 
	 * @param ror
	 * @param or
	 */
    private void synchroniseTypifications(ProcessContext context, RawOccurrenceRecord ror, OccurrenceRecord or) throws Exception {
        if (context.containsKey(contextKeySkipTypificationRecord) && (Boolean) context.get(contextKeySkipTypificationRecord, Boolean.class, false)) {
            logger.debug("Skipping typification syncing");
            return;
        }
        List<TypificationRecord> typifications = typificationRecordDAO.findByOccurrenceId(ror.getId());
        for (TypificationRecord typification : typifications) {
            typification.setTaxonNameId(getTypeNameId(context, or, typification));
            typificationRecordDAO.updateOrCreate(typification);
        }
    }

    /**
	 * Determine the id for the TaxonName corresponding to the ScientificName in the 
	 * typification record.  Look for an existing matching name, using the name from the
	 * occurrence record if it matches.  Create a TaxonName if necessary.
	 *   
	 * @param context
	 * @param or
	 * @param typification
	 * @return id
	 */
    private long getTypeNameId(ProcessContext context, OccurrenceRecord or, TypificationRecord typification) {
        long id = or.getTaxonNameId();
        if (typification.getScientificName() != null) {
            List<TaxonName> classification = new ArrayList<TaxonName>();
            int parseResult = scientificNameParser.parse(context, typification.getScientificName(), classification);
            if (parseResult == ScientificNameParser.PARSED) {
                TaxonName name = classification.get(0);
                TaxonName orName = taxonNameDAO.getById(or.getTaxonNameId());
                if (!name.getCanonical().equals(orName.getCanonical())) {
                    TaxonName existingName = taxonNameDAO.getUnique(name.getCanonical(), name.getAuthor(), name.getRank());
                    if (existingName == null) {
                        taxonNameDAO.create(name);
                        id = name.getId();
                    } else {
                        id = existingName.getId();
                    }
                }
            } else {
                GbifLogMessage message = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_SCIENTIFICNAMEPARSEISSUE, "Could not parse typified name: [" + typification.getScientificName() + "]");
                message.setCountOnly(true);
                logger.error(message);
            }
        }
        return id;
    }

    /**
	 * Retrieve a depth measurement, reporting any faults in the data.
	 * 
	 * Handles ranges and converts feet to metres.
	 * 
	 * @param s String to parse
	 * @return Float value or null
	 */
    private Float getDepthMeasurement(ProcessContext context, String depth, String fieldName, Integer geospatialIssues) {
        Float depthAsFloat = null;
        try {
            if (depth != null) {
                Matcher depthMatcher = measureMarkerPattern.matcher(depth);
                boolean containsNonnumeric = depthMatcher.matches();
                if (containsNonnumeric) {
                    geospatialIssues &= (OccurrenceRecordUtils.GEOSPATIAL_MASK ^ OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_DEPTH_NON_NUMERIC);
                    boolean isInFeet = feetMarkerPattern.matcher(depth).matches();
                    boolean isInInches = inchesMarkerPattern.matcher(depth).matches();
                    GbifLogMessage nonNumericMessage = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, fieldName + " contains non-numeric characters");
                    nonNumericMessage.setCountOnly(true);
                    logger.warn(nonNumericMessage);
                    if (sepMarkerPattern.matcher(depth).matches()) {
                        try {
                            String min = depth.substring(0, depth.indexOf('-')).trim();
                            min = removeMeasurementMarkers(min);
                            String max = depth.substring(depth.indexOf('-') + 1).trim();
                            max = removeMeasurementMarkers(max);
                            GbifLogMessage rangeMessage = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_GEOSPATIALISSUE, fieldName + " contains range supplied in single field");
                            rangeMessage.setCountOnly(true);
                            logger.warn(rangeMessage);
                            Float minFloat = Float.parseFloat(min);
                            Float maxFloat = Float.parseFloat(max);
                            if (minFloat != 0 && maxFloat != 0 && (maxFloat - minFloat) != 0) {
                                depthAsFloat = (maxFloat + minFloat) / 2;
                            }
                        } catch (NumberFormatException e) {
                        }
                    } else {
                        depth = removeMeasurementMarkers(depth);
                        depthAsFloat = Float.parseFloat(depth);
                    }
                    if (depthAsFloat != null) {
                        if (isInFeet || isInInches) {
                            geospatialIssues &= (OccurrenceRecordUtils.GEOSPATIAL_MASK ^ OccurrenceRecordUtils.GEOSPATIAL_PRESUMED_DEPTH_IN_FEET);
                        }
                        if (isInFeet) {
                            depthAsFloat = convertFeetToMetres(depthAsFloat);
                        } else if (isInInches) {
                            depthAsFloat = convertInchesToMetres(depthAsFloat);
                        }
                    }
                } else {
                    depthAsFloat = Float.parseFloat(depth);
                }
            }
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
        }
        return depthAsFloat;
    }

    /**
	 * Checks whether coordinates fall within bounding box for country
	 * @param country
	 * @param latitude
	 * @param longitude
	 * @return true if coordinates in box
	 */
    private boolean coordinatesWithinCountryBox(Country country, Float latitude, Float longitude) {
        return (latitude >= country.getMinLatitude() && latitude <= country.getMaxLatitude() && longitude >= country.getMinLongitude() && longitude <= country.getMaxLongitude());
    }

    /**
	 * Sets the Year Month and Day data
	 * @param ror Source
	 * @param or Target
	 */
    private void setYearMonthDay(ProcessContext context, RawOccurrenceRecord ror, OccurrenceRecord or) {
        Integer year = null;
        Integer month = null;
        Integer day = null;
        Date date = null;
        boolean invalidDate = false;
        try {
            if (ror.getYear() != null) year = new Integer(ror.getYear());
            if (year != null && year < 0) or.setOtherIssueBits(OccurrenceRecordUtils.OTHER_INVALID_DATE);
        } catch (NumberFormatException e) {
            invalidDate = true;
        }
        try {
            if (ror.getMonth() != null) month = new Integer(ror.getMonth());
            if (month != null && (month < 1 || month > 12)) or.setOtherIssueBits(OccurrenceRecordUtils.OTHER_INVALID_DATE);
        } catch (NumberFormatException e) {
            invalidDate = true;
        }
        try {
            if (ror.getDay() != null) day = new Integer(ror.getDay());
            if (day != null && day < 0) or.setOtherIssueBits(OccurrenceRecordUtils.OTHER_INVALID_DATE);
        } catch (NumberFormatException e) {
            invalidDate = true;
        }
        if (year != null && month != null && day != null) {
            try {
                if (year > 0) {
                    if (year < 100) {
                        Calendar calendar = Calendar.getInstance();
                        int currentYear = calendar.get(Calendar.YEAR);
                        if (year > currentYear % 100) {
                            year += ((currentYear / 100) - 1) * 100;
                        } else {
                            year += (currentYear / 100) * 100;
                        }
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month - 1, day, 12, 0, 0);
                    date = new Date(calendar.getTimeInMillis());
                }
            } catch (Exception e) {
                logger.debug("Invalid date");
                invalidDate = true;
                or.setOtherIssueBits(OccurrenceRecordUtils.OTHER_INVALID_DATE);
            }
        }
        if (invalidDate) {
            GbifLogMessage rangeMessage = gbifLogUtils.createGbifLogMessage(context, LogEvent.EXTRACT_TEMPORALISSUE, "Invalid or unparsable date");
            rangeMessage.setCountOnly(true);
            logger.warn(rangeMessage);
        }
        if (year != null && (year > 0 && year < 2100)) or.setYear(year);
        if (month != null && (month > 0 && month < 13)) or.setMonth(month);
        or.setOccurrenceDate(date);
    }

    /**
	 * Attempts to make an ISO country code of the country supplied
	 * @param countryString Which may be a name or a code already
	 * @return The ISO country code
	 */
    protected String getIsoCode(ProcessContext context, String countryString) {
        String country = StringUtils.trimToNull(countryString);
        if (country != null) {
            if (getIsoCountryCodeMap().containsValue(country.toUpperCase())) {
                logger.debug(country.toUpperCase() + " appears to be an ISO country code already");
                return country.toUpperCase();
            } else if (getIsoCountryCodeMap().containsKey(country.toUpperCase())) {
                logger.debug(country + " maps to ISO code: " + getIsoCountryCodeMap().get(country.toUpperCase()));
                return getIsoCountryCodeMap().get(country.toUpperCase());
            } else {
                logger.error("No ISO country code for: " + country.toUpperCase());
            }
        }
        return null;
    }

    boolean isInFeet(String measurement) {
        return feetMarkerPattern.matcher(measurement).matches();
    }

    boolean isInInches(String measurement) {
        return inchesMarkerPattern.matcher(measurement).matches();
    }

    float convertInchesToMetres(float inches) {
        return inches * 0.0254f;
    }

    float convertFeetToMetres(float feet) {
        return feet * 0.3048f;
    }

    /**
	 * Increment a count within the context.
	 * @param context
	 * @param contextKey
	 */
    private void incrementCount(ProcessContext context, String contextKey) {
        Integer count = (Integer) context.get(contextKey);
        if (count == null) {
            count = new Integer(1);
        } else {
            count = count + 1;
        }
        context.put(contextKey, count);
    }

    /**
	 * @return the contextKeyRawOccurrenceRecord
	 */
    public String getContextKeyRawOccurrenceRecord() {
        return contextKeyRawOccurrenceRecord;
    }

    /**
	 * @param contextKeyRawOccurrenceRecord the contextKeyRawOccurrenceRecord to set
	 */
    public void setContextKeyRawOccurrenceRecord(String contextKeyRawOccurrenceRecord) {
        this.contextKeyRawOccurrenceRecord = contextKeyRawOccurrenceRecord;
    }

    /**
	 * @return the occurrenceRecordDAO
	 */
    public OccurrenceRecordDAO getOccurrenceRecordDAO() {
        return occurrenceRecordDAO;
    }

    /**
	 * @param occurrenceRecordDAO the occurrenceRecordDAO to set
	 */
    public void setOccurrenceRecordDAO(OccurrenceRecordDAO occurrenceRecordDAO) {
        this.occurrenceRecordDAO = occurrenceRecordDAO;
    }

    /**
	 * @return the contextKeyTaxonConceptId
	 */
    public String getContextKeyTaxonConceptId() {
        return contextKeyTaxonConceptId;
    }

    /**
	 * @param contextKeyTaxonConceptId the contextKeyTaxonConceptId to set
	 */
    public void setContextKeyTaxonConceptId(String contextKeyTaxonConceptId) {
        this.contextKeyTaxonConceptId = contextKeyTaxonConceptId;
    }

    /**
	 * @return the contextKeyTaxonNameId
	 */
    public String getContextKeyTaxonNameId() {
        return contextKeyTaxonNameId;
    }

    /**
	 * @param contextKeyTaxonNameId the contextKeyTaxonNameId to set
	 */
    public void setContextKeyTaxonNameId(String contextKeyTaxonNameId) {
        this.contextKeyTaxonNameId = contextKeyTaxonNameId;
    }

    /**
	 * @return the contextKeyLogGroup
	 */
    public String getContextKeyLogGroup() {
        return contextKeyLogGroup;
    }

    /**
	 * @param contextKeyLogGroup the contextKeyLogGroup to set
	 */
    public void setContextKeyLogGroup(String contextKeyLogGroup) {
        this.contextKeyLogGroup = contextKeyLogGroup;
    }

    /**
	 * @return the gbifLogUtils
	 */
    public GbifLogUtils getGbifLogUtils() {
        return gbifLogUtils;
    }

    /**
	 * @param gbifLogUtils the gbifLogUtils to set
	 */
    public void setGbifLogUtils(GbifLogUtils gbifLogUtils) {
        this.gbifLogUtils = gbifLogUtils;
    }

    /**
	 * @return the isoCountryCodeMap
	 */
    public Map<String, String> getIsoCountryCodeMap() {
        return isoCountryCodeMap;
    }

    /**
	 * @param isoCountryCodeMap the isoCountryCodeMap to set
	 */
    public void setIsoCountryCodeMap(Map<String, String> isoCountryCodeMap) {
        this.isoCountryCodeMap = isoCountryCodeMap;
    }

    /**
	 * @return the dataResourceDAO
	 */
    public DataResourceDAO getDataResourceDAO() {
        return dataResourceDAO;
    }

    /**
	 * @param dataResourceDAO the dataResourceDAO to set
	 */
    public void setDataResourceDAO(DataResourceDAO dataResourceDAO) {
        this.dataResourceDAO = dataResourceDAO;
    }

    /**
	 * @return the basisOfRecordMapping
	 */
    public CodeMapping getBasisOfRecordMapping() {
        return basisOfRecordMapping;
    }

    /**
	 * @param basisOfRecordMapping the basisOfRecordMapping to set
	 */
    public void setBasisOfRecordMapping(CodeMapping basisOfRecordMapping) {
        this.basisOfRecordMapping = basisOfRecordMapping;
    }

    /**
	 * @return the imageTypeMapping
	 */
    public CodeMapping getImageTypeMapping() {
        return imageTypeMapping;
    }

    /**
	 * @param imageTypeMapping the imageTypeMapping to set
	 */
    public void setImageTypeMapping(CodeMapping imageTypeMapping) {
        this.imageTypeMapping = imageTypeMapping;
    }

    /**
	 * @return the cellCountryDAO
	 */
    public CellCountryDAO getCellCountryDAO() {
        return cellCountryDAO;
    }

    /**
	 * @param cellCountryDAO the cellCountryDAO to set
	 */
    public void setCellCountryDAO(CellCountryDAO cellCountryDAO) {
        this.cellCountryDAO = cellCountryDAO;
    }

    /**
	 * @return the countryDAO
	 */
    public CountryDAO getCountryDAO() {
        return countryDAO;
    }

    /**
	 * @param countryDAO the countryDAO to set
	 */
    public void setCountryDAO(CountryDAO countryDAO) {
        this.countryDAO = countryDAO;
    }

    /**
	 * @return the identifierRecordDAO
	 */
    public IdentifierRecordDAO getIdentifierRecordDAO() {
        return identifierRecordDAO;
    }

    /**
	 * @param identifierRecordDAO the identifierRecordDAO to set
	 */
    public void setIdentifierRecordDAO(IdentifierRecordDAO identifierRecordDAO) {
        this.identifierRecordDAO = identifierRecordDAO;
    }

    /**
	 * @return the imageRecordDAO
	 */
    public ImageRecordDAO getImageRecordDAO() {
        return imageRecordDAO;
    }

    /**
	 * @param imageRecordDAO the imageRecordDAO to set
	 */
    public void setImageRecordDAO(ImageRecordDAO imageRecordDAO) {
        this.imageRecordDAO = imageRecordDAO;
    }

    /**
	 * @return the linkRecordDAO
	 */
    public LinkRecordDAO getLinkRecordDAO() {
        return linkRecordDAO;
    }

    /**
	 * @param linkRecordDAO the linkRecordDAO to set
	 */
    public void setLinkRecordDAO(LinkRecordDAO linkRecordDAO) {
        this.linkRecordDAO = linkRecordDAO;
    }

    /**
	 * @return the typificationRecordDAO
	 */
    public TypificationRecordDAO getTypificationRecordDAO() {
        return typificationRecordDAO;
    }

    /**
	 * @param typificationRecordDAO the typificationRecordDAO to set
	 */
    public void setTypificationRecordDAO(TypificationRecordDAO typificationRecordDAO) {
        this.typificationRecordDAO = typificationRecordDAO;
    }

    /**
	 * @return the taxonNameDAO
	 */
    public TaxonNameDAO getTaxonNameDAO() {
        return taxonNameDAO;
    }

    /**
	 * @param taxonNameDAO the taxonNameDAO to set
	 */
    public void setTaxonNameDAO(TaxonNameDAO taxonNameDAO) {
        this.taxonNameDAO = taxonNameDAO;
    }

    /**
	 * @return the scientificNameParser
	 */
    public ScientificNameParser getScientificNameParser() {
        return scientificNameParser;
    }

    /**
	 * @param scientificNameParser the scientificNameParser to set
	 */
    public void setScientificNameParser(ScientificNameParser scientificNameParser) {
        this.scientificNameParser = scientificNameParser;
    }

    /**
	 * @return Returns the catalogueNumberDAO.
	 */
    public CatalogueNumberDAO getCatalogueNumberDAO() {
        return catalogueNumberDAO;
    }

    /**
	 * @param catalogueNumberDAO The catalogueNumberDAO to set.
	 */
    public void setCatalogueNumberDAO(CatalogueNumberDAO catalogueNumberDAO) {
        this.catalogueNumberDAO = catalogueNumberDAO;
    }

    /**
	 * @return Returns the collectionCodeDAO.
	 */
    public CollectionCodeDAO getCollectionCodeDAO() {
        return collectionCodeDAO;
    }

    /**
	 * @param collectionCodeDAO The collectionCodeDAO to set.
	 */
    public void setCollectionCodeDAO(CollectionCodeDAO collectionCodeDAO) {
        this.collectionCodeDAO = collectionCodeDAO;
    }

    /**
	 * @return Returns the institutionCodeDAO.
	 */
    public InstitutionCodeDAO getInstitutionCodeDAO() {
        return institutionCodeDAO;
    }

    /**
	 * @param institutionCodeDAO The institutionCodeDAO to set.
	 */
    public void setInstitutionCodeDAO(InstitutionCodeDAO institutionCodeDAO) {
        this.institutionCodeDAO = institutionCodeDAO;
    }

    /**
   * @param unknown_basis_of_record the uNKNOWN_BASIS_OF_RECORD to set
   */
    public static void setUNKNOWN_BASIS_OF_RECORD(int unknown_basis_of_record) {
        UNKNOWN_BASIS_OF_RECORD = unknown_basis_of_record;
    }

    /**
   * @param contextKeySkipLinkRecord the contextKeySkipLinkRecord to set
   */
    public void setContextKeySkipLinkRecord(String contextKeySkipLinkRecord) {
        this.contextKeySkipLinkRecord = contextKeySkipLinkRecord;
    }

    /**
   * @param contextKeySkipTypificationRecord the contextKeySkipTypificationRecord to set
   */
    public void setContextKeySkipTypificationRecord(String contextKeySkipTypificationRecord) {
        this.contextKeySkipTypificationRecord = contextKeySkipTypificationRecord;
    }

    /**
   * @param contextKeySkipImageRecord the contextKeySkipImageRecord to set
   */
    public void setContextKeySkipImageRecord(String contextKeySkipImageRecord) {
        this.contextKeySkipImageRecord = contextKeySkipImageRecord;
    }

    /**
   * @param maxRecordDepthInCentimetres the maxRecordDepthInCentimetres to set
   */
    public void setMaxRecordDepthInCentimetres(int maxRecordDepthInCentimetres) {
        this.maxRecordDepthInCentimetres = maxRecordDepthInCentimetres;
    }

    /**
   * @param maxRecordAltitudeInMetres the maxRecordAltitudeInMetres to set
   */
    public void setMaxRecordAltitudeInMetres(int maxRecordAltitudeInMetres) {
        this.maxToRecordAltitudeInMetres = maxRecordAltitudeInMetres;
    }

    /**
   * @param pattern the pattern to set
   */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
   * @param measureMarkerPattern the measureMarkerPattern to set
   */
    public void setMeasureMarkerPattern(Pattern measureMarkerPattern) {
        this.measureMarkerPattern = measureMarkerPattern;
    }

    /**
   * @param outOfRangeDepth the outOfRangeDepth to set
   */
    public void setOutOfRangeDepth(int outOfRangeDepth) {
        this.outOfRangeDepth = outOfRangeDepth;
    }

    /**
   * @param outOfRangeMinAltitude the outOfRangeMinAltitude to set
   */
    public void setOutOfRangeMinAltitude(int outOfRangeMinAltitude) {
        this.outOfRangeMinAltitude = outOfRangeMinAltitude;
    }

    /**
   * @param outOfRangeMaxAltitude the outOfRangeMaxAltitude to set
   */
    public void setOutOfRangeMaxAltitude(int outOfRangeMaxAltitude) {
        this.outOfRangeMaxAltitude = outOfRangeMaxAltitude;
    }

    /**
   * @param contextKeyAddedCount the contextKeyAddedCount to set
   */
    public void setContextKeyAddedCount(String contextKeyAddedCount) {
        this.contextKeyAddedCount = contextKeyAddedCount;
    }

    /**
   * @param contextKeyUpdatedCount the contextKeyUpdatedCount to set
   */
    public void setContextKeyUpdatedCount(String contextKeyUpdatedCount) {
        this.contextKeyUpdatedCount = contextKeyUpdatedCount;
    }

    /**
   * @param minRecordDepthInCentimetres the minRecordDepthInCentimetres to set
   */
    public void setMinRecordDepthInCentimetres(int minRecordDepthInCentimetres) {
        this.minRecordDepthInCentimetres = minRecordDepthInCentimetres;
    }

    /**
   * @param maxToRecordAltitudeInMetres the maxToRecordAltitudeInMetres to set
   */
    public void setMaxToRecordAltitudeInMetres(int maxToRecordAltitudeInMetres) {
        this.maxToRecordAltitudeInMetres = maxToRecordAltitudeInMetres;
    }

    /**
   * @param minToRecordAltitudeInMetres the minToRecordAltitudeInMetres to set
   */
    public void setMinToRecordAltitudeInMetres(int minToRecordAltitudeInMetres) {
        this.minToRecordAltitudeInMetres = minToRecordAltitudeInMetres;
    }

    /**
   * @param feetMarkerPattern the feetMarkerPattern to set
   */
    public void setFeetMarkerPattern(Pattern feetMarkerPattern) {
        this.feetMarkerPattern = feetMarkerPattern;
    }

    /**
   * @param inchesMarkerPattern the inchesMarkerPattern to set
   */
    public void setInchesMarkerPattern(Pattern inchesMarkerPattern) {
        this.inchesMarkerPattern = inchesMarkerPattern;
    }

    /**
   * @param sepMarkerPattern the sepMarkerPattern to set
   */
    public void setSepMarkerPattern(Pattern sepMarkerPattern) {
        this.sepMarkerPattern = sepMarkerPattern;
    }

    /**
   * @param coordinateMarkerPattern the coordinateMarkerPattern to set
   */
    public void setCoordinateMarkerPattern(Pattern coordinateMarkerPattern) {
        this.coordinateMarkerPattern = coordinateMarkerPattern;
    }
}
