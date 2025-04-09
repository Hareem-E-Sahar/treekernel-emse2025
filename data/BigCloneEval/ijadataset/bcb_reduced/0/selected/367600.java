package hambo.positioningapi.data;

import java.lang.Math;
import hambo.positioningapi.business.mpssdk.LocationResult;
import hambo.positioningapi.business.mpssdk.Coordinate;
import hambo.positioningapi.business.mpssdk.MPSException;
import hambo.positioningapi.business.ExternalException;
import hambo.positioningapi.business.InternalException;
import hambo.positioningapi.business.PositioningUtil;
import hambo.positioningapi.business.PositioningAPIException;
import hambo.svc.log.LogServiceManager;
import hambo.svc.log.Log;

/**
 * This class wraps information received from a positioning request on a given MSISDN
 * using the MPS protocol.
 * 
 */
public class MPSPositioningData extends PositioningDataAdapter {

    private int cellRadius;

    private int innerRadius;

    private int outerRadius;

    private int startAngle;

    private int stopAngle;

    private String x;

    private String y;

    private double easting;

    private double northing;

    private String zoneDesignator;

    private int zoneNumber;

    private int errorCode;

    private double radius;

    private String cellLatitude;

    private String cellLongitude;

    private double decimalLatitude;

    private double decimalLongitude;

    private final double conversionConstant = 111325;

    /**
     * 
     * Creates a MPSPositioningData object from the response information received in the LocationResult
     * object.
     * @param mpsResult the object containing the response information  
     * @exception throws ExternalException indicating a server side error
     * @exception throws InternalException if essential data is missing or malformed or if calculation of the coordinates fails
     */
    public MPSPositioningData(LocationResult mpsResult) throws ExternalException, InternalException {
        errorCode = mpsResult.getErrorCode();
        errorDescription = mpsResult.getErrorDescription();
        if (errorCode == 0) {
            cellRadius = mpsResult.getCellRadius();
            innerRadius = mpsResult.getInnerRadius();
            outerRadius = mpsResult.getOuterRadius();
            startAngle = mpsResult.getStartAngle();
            stopAngle = mpsResult.getStopAngle();
            time = mpsResult.getTime();
            msisdn = mpsResult.getPositionItem();
            try {
                Coordinate coord = mpsResult.getCoord();
                coordinateType = coord.getCoordinateType();
                if (coordinateType.equals("LL")) {
                    cellLongitude = coord.getLongitude();
                    cellLatitude = coord.getLatitude();
                    if ((startAngle != 0 && stopAngle != 360) && (startAngle != stopAngle)) {
                        try {
                            calculateCoordinates();
                        } catch (InternalException ie) {
                            LogServiceManager.getLog("positioningAPI, MPSPositioningData").println(Log.ERROR, ie.getErrorMessage());
                            throw new InternalException(ie.getErrorMessage());
                        }
                    } else {
                        longitude = cellLongitude;
                        latitude = cellLatitude;
                    }
                } else if (coordinateType.equals("XY")) {
                    x = Double.toString(coord.getXCoordinate());
                    y = Double.toString(coord.getYCoordinate());
                } else if (coordinateType.equals("UTM")) {
                    easting = coord.getEasting();
                    northing = coord.getNorthing();
                }
            } catch (MPSException mpse) {
                LogServiceManager.getLog("positioningAPI, MPSPositioningData").println(Log.ERROR, mpse.toString());
                throw new ExternalException("MPS", mpse.toString());
            }
        } else {
            LogServiceManager.getLog("positioningAPI, MPSPositioningData").println(Log.ERROR, "error when retrieving positioning data: " + errorDescription + "(" + errorCode + ")");
        }
    }

    /**
     * Gets the radius of the location area.
     * @return the radius of the location area
     */
    public int getRadius() {
        if ((startAngle == 0 && stopAngle == 360) || (startAngle == stopAngle)) return cellRadius;
        int start = startAngle;
        int stop = stopAngle;
        radius = (outerRadius + innerRadius) / 2;
        if (start > stop) stop += 360;
        double angle = (double) ((stop - start) / 2);
        double r = Math.tan(Math.toRadians(angle)) * radius;
        return (int) r;
    }

    private void calculateCoordinates() throws InternalException {
        double innerRad = (double) innerRadius;
        double outerRad = (double) outerRadius;
        double startAng = (double) startAngle;
        double stopAng = (double) stopAngle;
        double dlat = 0;
        double dlong = 0;
        double angle;
        if (stopAngle < startAngle) {
            angle = (startAngle + (stopAngle + 360)) / 2;
            if (angle > 360) angle -= 360;
        } else angle = (startAng + stopAng) / 2;
        radius = (outerRad + innerRad) / 2;
        double v = 0;
        double deltaX = 0;
        double deltaY = 0;
        try {
            dlat = PositioningUtil.convertLLToDecimal(cellLatitude);
            dlong = PositioningUtil.convertLLToDecimal(cellLongitude);
        } catch (PositioningAPIException nfe) {
            LogServiceManager.getLog("positioningAPI, MPSPositioningData").println(Log.ERROR, "Error when parsing coordinates to double", nfe);
            throw new InternalException("MPSPositioningData: Unable to convert coordinates to decimal values, " + nfe.getErrorMessage());
        }
        if (cellLatitude.charAt(0) == 'S') dlat = -dlat;
        if (cellLongitude.charAt(0) == 'W') dlong = -dlong;
        if (angle == 0 || angle == 360) {
            deltaX = 0;
            deltaY = radius;
        } else if (angle == 90) {
            deltaX = radius;
            deltaY = 0;
        } else if (angle == 180) {
            deltaX = 0;
            deltaY = -radius;
        } else if (angle == 270) {
            deltaX = -radius;
            deltaY = 0;
        } else {
            if ((angle > 0) && (angle < 90)) {
                v = angle;
                deltaX = (Math.sin(Math.toRadians(v)) * radius);
                deltaY = (Math.sin(Math.toRadians(90 - v)) * radius);
            } else if ((angle > 90) && (angle < 180)) {
                v = angle - 90;
                deltaY = -(Math.sin(Math.toRadians(v)) * radius);
                deltaX = (Math.sin(Math.toRadians(90 - v)) * radius);
            } else if ((angle > 180) && (angle < 270)) {
                v = angle - 180;
                deltaX = -(Math.sin(Math.toRadians(v)) * radius);
                deltaY = -(Math.sin(Math.toRadians(90 - v)) * radius);
            } else {
                v = angle - 270;
                deltaY = (Math.sin(Math.toRadians(v)) * radius);
                deltaX = -(Math.sin(Math.toRadians(90 - v)) * radius);
            }
        }
        decimalLatitude = dlat + (deltaY / conversionConstant);
        decimalLongitude = dlong + (deltaX / (Math.cos(Math.toRadians(decimalLatitude)) * conversionConstant));
        latitude = PositioningUtil.convertDecLatitudeToLL(decimalLatitude);
        longitude = PositioningUtil.convertDecLongitudeToLL(decimalLongitude);
    }

    /**
     * Gets the easting value, this is for the UTM geodetic system.
     * Note that this system is not supported by the mapping subsystem.
     * @return the easting value
     */
    public double getEasting() {
        return this.easting;
    }

    /**
     * Gets the northing value, this is for the UTM geodetic system.
     * Note that this system is not supported by the mapping subsystem.
     * @return the northing value
     */
    public double getNorthing() {
        return this.northing;
    }

    /**
     * Gets the zone number, this is for the UTM geodetic system.
     * Note that this system is not supported by the mapping subsystem.
     * @return the zone number
     */
    public int getZoneNumber() {
        return this.zoneNumber;
    }

    /**
     * Gets the zone designator, this is for the UTM geodetic system.
     * Note that this system is not supported by the mapping subsystem.
     * @return the zone designator
     */
    public String getZoneDesignator() {
        return this.zoneDesignator;
    }

    /**
     * Gets the x-coordinate of the mobile station, used with the RT90 geodetic system.
     * Note that this system is not supported by the mapping subsystem.
     * @return the x-coordinate
     */
    public String getXCoordinate() {
        return this.x;
    }

    /**
     * Gets the y-coordinate of the mobile station, used with the RT90 geodetic system.
     * Note that this system is not supported by the mapping subsystem.
     * @return the y-coordinate
     */
    public String getYCoordinate() {
        return this.y;
    }

    /**
     * Gets the inner radius.
     * This method never needs to be used as long as the default geodetic- and coordinate system
     * (WGS84, LL) is used. If you for some strange reason would like to use other systems
     * you will have to calculate the coordinates of the terminal yourself as the MPS protocol do not return coordinates of the terminal, 
     * using the inner radius, outer radius, start angle and stop angle as well as the coordinates for the base station.
     * It is however strongly recommended that you use the default systems, since they are international
     * standards and since they are supported by the mapping subsystem.
     * @return inner radius
     */
    public int getInnerRadius() {
        return this.innerRadius;
    }

    /**
     * Gets the outer radius.
     * This method never needs to be used as long as the default geodetic- and coordinate system
     * (WGS84, LL) is used. If you for some strange reason would like to use other systems
     * you will have to calculate the coordinates of the terminal yourself as the MPS protocol do not return coordinates of the terminal, 
     * using the inner radius, outer radius, start angle and stop angle as well as the coordinates for the base station.
     * It is however strongly recommended that you use the default systems, since they are international
     * standards and since they are supported by the mapping subsystem.
     * @return outer radius
     */
    public int getOuterRadius() {
        return this.outerRadius;
    }

    /**
     * Gets the start angle.
     * This method never needs to be used as long as the default geodetic- and coordinate system
     * (WGS84, LL) is used. If you for some strange reason would like to use other systems
     * you will have to calculate the coordinates of the terminal yourself as the MPS protocol do not return coordinates of the terminal, 
     * using the inner radius, outer radius, start angle and stop angle as well as the coordinates for the base station.
     * It is however strongly recommended that you use the default systems, since they are international
     * standards and since they are supported by the mapping subsystem.
     * @return start angle
     */
    public int getStartAngle() {
        return this.startAngle;
    }

    /**
     * Gets the stop angle.
     * This method never needs to be used as long as the default geodetic- and coordinate system
     * (WGS84, LL) is used. If you for some strange reason would like to use other systems
     * you will have to calculate the coordinates of the terminal yourself as the MPS protocol do not return coordinates of the terminal, 
     * using the inner radius, outer radius, start angle and stop angle as well as the coordinates for the base station.
     * It is however strongly recommended that you use the default systems, since they are international
     * standards and since they are supported by the mapping subsystem.
     * @return stop angle
     */
    public int getStopAngle() {
        return this.stopAngle;
    }
}
