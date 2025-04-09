package simulation;

/**
 * @author leon
 * 
 */
public class Sun {

    private static int MINUTES_OF_YEAR = 60 * 24 * 365;

    private static int MINUTES_OF_DAY = 60 * 24;

    private static int SUN_RADIUS = 695500;

    private static int MAJOR_AXIS_LENGTH = 152500000;

    private static double SUN_SURFACE_INTENSITY = 1e21 / (4 * Math.PI * Math.pow(SUN_RADIUS, 2));

    private static double ORBIT_DEGREE_MINUTE = 360.0 / 365.0 / 24.0 / 60.0;

    private static double SPIN_DEGREE_MINUTE = 360.0 / 24.0 / 60.0;

    private static int MINUTE_VERNAL_EQUINOX = 115200;

    private double longitude;

    private double latRadiation;

    private int minuteOfYear = MINUTE_VERNAL_EQUINOX;

    private int minuteOfDay = 0;

    private double minorAxisLength;

    private double tiltOfEarth;

    private double equinoxAngle;

    private double orbitX;

    private double orbitY;

    private double eccentricity;

    /**
	 * @param lattitude
	 * @param longitude
	 */
    public Sun(double longitude, double eccentricity, double tiltOfEarth) {
        this.longitude = longitude;
        this.tiltOfEarth = tiltOfEarth;
        this.eccentricity = eccentricity;
        reconfigure();
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
        reconfigure();
    }

    public void setTiltOfEarth(double tiltOfEarth) {
        this.tiltOfEarth = tiltOfEarth;
        reconfigure();
    }

    public double getOrbitX() {
        return orbitX;
    }

    public double getOrbitY() {
        return orbitY;
    }

    public double getLatitude() {
        return latRadiation;
    }

    private void reconfigure() {
        this.minorAxisLength = Math.sqrt(Math.pow(MAJOR_AXIS_LENGTH, 4) * Math.pow(eccentricity, 2) - Math.pow(MAJOR_AXIS_LENGTH, 2));
        orbitX = MAJOR_AXIS_LENGTH * Math.cos((MINUTE_VERNAL_EQUINOX) * ORBIT_DEGREE_MINUTE);
        orbitY = minorAxisLength * Math.cos((MINUTE_VERNAL_EQUINOX) * ORBIT_DEGREE_MINUTE);
        equinoxAngle = Math.atan(orbitY / orbitX);
    }

    /**
	 * @return
	 */
    public double getLongitude() {
        return longitude;
    }

    /**
	 * @param degree
	 */
    public void moveSun(double degree) {
        double tempLon = longitude + degree;
        if (tempLon > 180) {
            longitude = (-180 + (tempLon % 180));
        } else {
            longitude = tempLon;
        }
        minuteOfYear = (minuteOfYear + 1) % MINUTES_OF_YEAR;
        minuteOfDay = (minuteOfDay + 1) % MINUTES_OF_DAY;
    }

    public void reset() {
        longitude = 0;
    }

    public float cRadiationFactor(float tL, float bL, float lL, float rL) {
        float result;
        float aveLat = (tL + bL) / 2;
        float aveLon = (lL + rL) / 2;
        orbitX = MAJOR_AXIS_LENGTH * Math.cos(Math.toRadians((minuteOfYear - MINUTE_VERNAL_EQUINOX) * ORBIT_DEGREE_MINUTE));
        orbitY = minorAxisLength * Math.cos(Math.toRadians((minuteOfYear - MINUTE_VERNAL_EQUINOX) * ORBIT_DEGREE_MINUTE));
        latRadiation = Math.abs(aveLat + tiltOfEarth * Math.cos(Math.toRadians(SPIN_DEGREE_MINUTE * minuteOfDay + Math.atan(orbitY / orbitX) - equinoxAngle + aveLon)));
        double lonRadiation = Math.cos(Math.toRadians(aveLon - this.longitude));
        double distance = Math.sqrt(orbitX * orbitX / 2 + orbitY * orbitY / 2);
        if (lonRadiation < 0) {
            return 0;
        } else {
            result = (float) (latRadiation * lonRadiation * (SUN_SURFACE_INTENSITY / Math.pow(distance / SUN_RADIUS, 2) * Math.pow(10, 10)));
            return result;
        }
    }
}
