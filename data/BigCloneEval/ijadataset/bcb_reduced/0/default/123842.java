public class Odometer {

    public double x = 10;

    public double y = 10;

    public double orientation = 0;

    private double distance_wheels = 13.4;

    private double distance_left_degree;

    private double distance_right_degree;

    private static double leftWheelDiameter = 4.5;

    private static double rightWheelDiameter = 4.5;

    private static double radius_right = rightWheelDiameter / 2;

    private static double radius_left = leftWheelDiameter / 2;

    private static Odometer INSTANCE;

    public double prevLeftTacho = 0;

    public double prevRightTacho = 0;

    public double sL = 0;

    public double sR = 0;

    private long prevTime = 0;

    private Odometer() {
        this.distance_left_degree = Math.PI * radius_left * 2 / 360;
        this.distance_right_degree = Math.PI * radius_right * 2 / 360;
    }

    public static Odometer getInstance() {
        if (INSTANCE == null) INSTANCE = new Odometer();
        return INSTANCE;
    }

    public void nextTacho(double leftTacho, double rightTacho) {
        double distance_left = distance_left_degree * (leftTacho - prevLeftTacho);
        double distance_right = distance_right_degree * (rightTacho - prevRightTacho);
        this.orientation = this.orientation + (distance_right - distance_left) / this.distance_wheels;
        double distanceTraveled = (distance_left + distance_right) / 2;
        distanceTraveled *= 0.917;
        this.x = this.x + (distanceTraveled) * Math.cos(orientation);
        this.y = this.y + (distanceTraveled) * Math.sin(orientation);
        prevLeftTacho = leftTacho;
        prevRightTacho = rightTacho;
    }
}
