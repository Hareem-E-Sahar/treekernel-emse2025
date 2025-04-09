package jbotrace.base;

import jbotrace.base.dataServer.*;

/**
 * Holds the dynamic data of a car. 
 */
public class CarState {

    private static final double AIRDENSITYG_L = 1.2;

    DataServer dataServer;

    int dataTypeVelocityId;

    CarDefinition carDefinition;

    DriverCommand driverCommand;

    double direction;

    Vector2d position;

    double fuelL;

    double brake;

    double steeringAngle;

    double throttle;

    int gear;

    double velocityM_s;

    double wheelTorqueNm;

    double wheelForceN;

    double gripForceN;

    double rollingResistanceN;

    double airDragN;

    double nettoForceN;

    double accelerationM_ss;

    double freeRollingWheelAngularVelocity_s;

    double wheelAngularVelocity_s;

    double longitudinalSlip;

    double engineTorqueNm;

    double engineRPM_s;

    double weightOnFrontWheelsN;

    double weightOnRearWheelsN;

    double maxForceToGroundN;

    /**
	 *  Initialize the state of the car.
	 */
    public CarState(CarDefinition carDefinition) {
        this.carDefinition = carDefinition;
    }

    /**
	 * A very primitive friction function for the moment (similar to rars).
	 * @param slip
	 * @return double
	 */
    private double calcGripForceN(double slip, double maxForceToGroundN) {
        return maxForceToGroundN * slip / (0.08 + slip);
    }

    /**
	 * 
	 * @param timestepS
	 */
    public void calculateMovement(double timestepS) {
        weightOnFrontWheelsN = carDefinition.getEmptyMassKg() * 9.81 / 2;
        weightOnRearWheelsN = carDefinition.getEmptyMassKg() * 9.81 / 2;
        maxForceToGroundN = weightOnRearWheelsN * 1.1;
        engineTorqueNm = carDefinition.getEngineMaxTorqueNm() * getThrottle();
        wheelTorqueNm = engineTorqueNm * carDefinition.getGearRatio(getGear()) * carDefinition.getDifferentialRatio();
        wheelForceN = wheelTorqueNm / (carDefinition.getDiameterRearWheelM() / 2);
        freeRollingWheelAngularVelocity_s = velocityM_s / carDefinition.diameterRearWheelM * 2;
        wheelAngularVelocity_s = freeRollingWheelAngularVelocity_s + 0.5;
        engineRPM_s = wheelAngularVelocity_s * carDefinition.getGearRatio(getGear()) * carDefinition.getDifferentialRatio();
        if (wheelForceN > maxForceToGroundN - 1) {
            longitudinalSlip = 8;
        } else {
            double abweichung = 2;
            double left = 0;
            double right = 100;
            longitudinalSlip = 0.4;
            int turns = 0;
            do {
                double gripForceX = calcGripForceN(longitudinalSlip, maxForceToGroundN);
                abweichung = gripForceX / wheelForceN;
                if (abweichung < 1) {
                    left = longitudinalSlip;
                    longitudinalSlip = (longitudinalSlip + right) / 2;
                } else if (abweichung > 1) {
                    right = longitudinalSlip;
                    longitudinalSlip = (longitudinalSlip + left) / 2;
                }
                turns++;
            } while (Math.abs(abweichung - 1) > 0.01);
        }
        gripForceN = calcGripForceN(longitudinalSlip, maxForceToGroundN);
        rollingResistanceN = 0.7 * velocityM_s;
        airDragN = (carDefinition.getCoefficientOfDrag() * carDefinition.getFrontalAreaMm() * AIRDENSITYG_L * velocityM_s * velocityM_s) * 0.5;
        nettoForceN = gripForceN - rollingResistanceN - airDragN;
        accelerationM_ss = nettoForceN / getMass();
        velocityM_s = velocityM_s + timestepS * accelerationM_ss;
        position.add(direction, velocityM_s * timestepS);
        if (dataServer != null) dataServer.dataUpdate(dataTypeVelocityId, new Double(velocityM_s));
    }

    /**
	 * @return double
	 */
    private double getMass() {
        return carDefinition.getEmptyMassKg() + getFuelL();
    }

    /**
	 * @return double
	 */
    public double getBrake() {
        return brake;
    }

    /**
	 * @return double
	 */
    public double getDirection() {
        return direction;
    }

    /**
	 * @return DriverCommand
	 */
    public DriverCommand getDriverCommand() {
        return driverCommand;
    }

    /**
	 * @return Vector
	 */
    public Vector2d getPosition() {
        return position;
    }

    /**
	 * @return double
	 */
    public double getSteeringAngle() {
        return steeringAngle;
    }

    /**
	 * @return double
	 */
    public double getThrottle() {
        return throttle;
    }

    /**
	 * @return double
	 */
    public double getVelocityM_s() {
        return velocityM_s;
    }

    double getWheelDirection(int wheel) {
        if (wheel < 2) return getDirection() + getSteeringAngle(); else return getDirection();
    }

    /**
	 * Sets the brake.
	 * @param brake The brake to set
	 */
    public void setBrake(double brake) {
        if (brake > 1) brake = 1; else if (brake < 0) brake = 0;
        this.brake = brake;
    }

    /** Sets the direction of the car. */
    void setDirection(double direction) {
        while (direction < 0) direction += Math.PI * 2;
        while (direction >= Math.PI * 2) direction -= Math.PI * 2;
        this.direction = direction;
    }

    /**
	 * Sets the driverCommand.
	 * @param driverCommand The driverCommand to set
	 */
    public void setDriverCommand(DriverCommand driverCommand) {
        this.driverCommand = driverCommand;
        setThrottle(driverCommand.getThrottle());
        setSteeringAngle(driverCommand.getSteeringAngle());
        setBrake(driverCommand.getBrake());
        setGear(driverCommand.getGear());
    }

    /**
	 * Sets the position.
	 * @param position The position to set
	 */
    public void setPosition(Vector2d position) {
        this.position = position;
    }

    /**
	 * Sets the steeringAngle.
	 * @param steeringAngle The steeringAngle to set
	 */
    public void setSteeringAngle(double steeringAngle) {
        while (steeringAngle < 0) steeringAngle += Math.PI * 2;
        while (steeringAngle >= Math.PI * 2) steeringAngle -= Math.PI * 2;
        this.steeringAngle = steeringAngle;
    }

    /**
	 * Sets the throttle.
	 * @param throttle The throttle to set
	 */
    public void setThrottle(double throttle) {
        if (throttle > 1) throttle = 1; else if (throttle < 0) throttle = 0;
        this.throttle = throttle;
    }

    /**
	 * @return double
	 */
    public int getGear() {
        return gear;
    }

    /**
	 * Sets the gear.
	 * @param gear The gear to set
	 */
    public void setGear(int gear) {
        this.gear = gear;
    }

    /**
	 * @return double
	 */
    public double getFuelL() {
        return fuelL;
    }

    /**
	 * @return double
	 */
    public double getAccelerationM_ss() {
        return accelerationM_ss;
    }

    /**
	 * @return double
	 */
    public double getAirDragN() {
        return airDragN;
    }

    /**
	 * @return double
	 */
    public double getNettoForceN() {
        return nettoForceN;
    }

    /**
	 * @return double
	 */
    public double getRollingResistanceN() {
        return rollingResistanceN;
    }

    /**
	 * @return double
	 */
    public double getWheelForceN() {
        return wheelForceN;
    }

    /**
	 * @return double
	 */
    public double getWheelTorqueNm() {
        return wheelTorqueNm;
    }

    /**
	 * Sets the dataServer.
	 * @param dataServer The dataServer to set
	 */
    public void setDataServer(DataServer dataServer) {
        if (this.dataServer != null) this.dataServer.removeDataType(dataTypeVelocityId);
        this.dataServer = dataServer;
        DataType dataTypeVelocity = new DataType("jbotrace.car.velocity", "velocity", "Vc", "m/s", new Double(0));
        dataTypeVelocityId = dataServer.addDataSource(dataTypeVelocity);
    }
}
