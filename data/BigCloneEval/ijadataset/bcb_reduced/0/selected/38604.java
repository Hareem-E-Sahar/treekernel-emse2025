package creiter.gpxTcxWelder.data;

import com.google.common.collect.ImmutableList;
import creiter.gpxTcxWelder.data.xml.tcx.ActivityLapT;
import creiter.gpxTcxWelder.data.xml.tcx.ActivityListT;
import creiter.gpxTcxWelder.data.xml.tcx.ActivityT;
import creiter.gpxTcxWelder.data.xml.tcx.PositionT;
import creiter.gpxTcxWelder.data.xml.tcx.TrackT;
import creiter.gpxTcxWelder.data.xml.tcx.TrackpointT;
import creiter.gpxTcxWelder.data.xml.tcx.TrainingCenterDatabaseT;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author christian
 */
public class TcxFile extends File {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private TrainingCenterDatabaseT trainingCenterDatabase;

    private long minTrackpointInterval, maxTrackpointInterval, avgTrackpointInterval;

    private int trackpointCount;

    private int trackCount;

    private List<TrackpointT> trackpoints;

    public TcxFile(URI uri) throws JAXBException {
        super(uri);
        init();
    }

    public TcxFile(File parent, String child) throws JAXBException {
        super(parent, child);
        init();
    }

    public TcxFile(String parent, String child) throws JAXBException {
        super(parent, child);
        init();
    }

    public TcxFile(String pathname) throws JAXBException {
        super(pathname);
        init();
    }

    public TcxFile(File file) throws JAXBException {
        super(file.getPath());
        init();
    }

    private void init() throws JAXBException {
        generateTrainingCenterDatabase();
        calculateIntervals();
    }

    private void generateTrainingCenterDatabase() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance("creiter.gpxTcxWelder.data.xml.tcx");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        JAXBElement element = (JAXBElement) unmarshaller.unmarshal((File) this);
        trainingCenterDatabase = (TrainingCenterDatabaseT) element.getValue();
    }

    public TrainingCenterDatabaseT getTrainingCenterDatabase() {
        return trainingCenterDatabase;
    }

    private void calculateIntervals() {
        trackpoints = new ArrayList<TrackpointT>();
        ActivityListT activityList = trainingCenterDatabase.getActivities();
        Long lastTimeEpochUtc = null;
        boolean maxNotCalculated = true;
        boolean minNotCalculated = true;
        if (activityList != null) {
            for (ActivityT activity : activityList.getActivity()) {
                for (ActivityLapT lap : activity.getLap()) {
                    for (TrackT track : lap.getTrack()) {
                        trackCount++;
                        for (TrackpointT trackpoint : track.getTrackpoint()) {
                            trackpointCount++;
                            trackpoints.add(trackpoint);
                            Long thisTimestamp = trackpoint.getTime().normalize().toGregorianCalendar().getTimeInMillis();
                            if (lastTimeEpochUtc != null) {
                                Long diff = thisTimestamp - lastTimeEpochUtc;
                                avgTrackpointInterval = (avgTrackpointInterval + diff) / 2;
                                if (maxNotCalculated || diff > maxTrackpointInterval) {
                                    maxTrackpointInterval = diff;
                                    maxNotCalculated = false;
                                }
                                if (minNotCalculated || diff < minTrackpointInterval) {
                                    minTrackpointInterval = diff;
                                    minNotCalculated = false;
                                }
                            }
                            lastTimeEpochUtc = thisTimestamp;
                        }
                    }
                }
            }
        } else {
            trackpointCount = 0;
            minTrackpointInterval = 0L;
            maxTrackpointInterval = 0L;
            avgTrackpointInterval = 0L;
        }
    }

    /**
     *
     * @return Average intervall between track points (Millisecons!)
     */
    public Long getAverageTrackpointInterval() {
        return avgTrackpointInterval;
    }

    /**
     *
     * @return Maximum intervall between track points (Millisecons!)
     */
    public Long getMaximumTrackpointInterval() {
        return maxTrackpointInterval;
    }

    /**
     *
     * @return Minimum intervall between track points (Millisecons!)
     */
    public Long getMinimumTrackpointInterval() {
        return minTrackpointInterval;
    }

    public Integer getTrackpointCount() {
        return trackpointCount;
    }

    public Integer getTrackCount() {
        return trackCount;
    }

    /**
     * Returns an unmodifieable List of all Trackpoints in this TCX file
     *
     * @return unmodifieable List of all Trackpoints in this TCX file
     */
    public List<TrackpointT> getTrackpoints() {
        return ImmutableList.copyOf(trackpoints);
    }

    public WeldingResultInfo supplementGeoPostions(GpxFileSet gpxFileSet, long maximumTimeOffset) {
        int totalPos = 0;
        int posFound = 0;
        int posNotFound = 0;
        int minTimeDifference = Integer.MAX_VALUE;
        int maxTimeDifference = 0;
        logger.debug("Starting to supplementGeoPostions");
        for (TrackpointT trackpoint : trackpoints) {
            if (trackpoint.getPosition() == null) {
                XMLGregorianCalendar trackpointCal = trackpoint.getTime().normalize();
                DateTime trackpointDate = new DateTime(trackpointCal.getYear(), trackpointCal.getMonth(), trackpointCal.getDay(), trackpointCal.getHour(), trackpointCal.getMinute(), trackpointCal.getSecond(), trackpointCal.getMillisecond(), DateTimeZone.UTC);
                logger.debug("Looking for GeoPos for " + trackpointCal.toXMLFormat());
                GeoPosition matchingPosition = gpxFileSet.getGeoPosition(trackpointDate, maximumTimeOffset);
                totalPos++;
                if (matchingPosition != null) {
                    PositionT positionT = new PositionT();
                    positionT.setLatitudeDegrees(matchingPosition.getLatitude().doubleValue());
                    positionT.setLongitudeDegrees(matchingPosition.getLongitude().doubleValue());
                    if (matchingPosition.getElevation() != null) {
                        trackpoint.setAltitudeMeters(matchingPosition.getElevation().doubleValue());
                    }
                    trackpoint.setPosition(positionT);
                    logger.debug("Found geoposition for trackpoint at date " + trackpointCal.toXMLFormat());
                    posFound++;
                } else {
                    logger.debug("No matching geoposition for trackpoint at date " + trackpointCal.toXMLFormat());
                    posNotFound++;
                }
            }
        }
        logger.debug("Done with supplementGeoPostions");
        return new WeldingResultInfo(totalPos, posFound, posNotFound, maxTimeDifference, minTimeDifference);
    }

    public void writeToFile(File file) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(TrainingCenterDatabaseT.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd");
        marshaller.marshal(new JAXBElement(new QName("http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2", "TrainingCenterDatabase"), TrainingCenterDatabaseT.class, trainingCenterDatabase), new FileOutputStream(file));
    }
}
