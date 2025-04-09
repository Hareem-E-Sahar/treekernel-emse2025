package jsbsim.initialization;

import jsbsim.FGFDMExec;
import jsbsim.FGJSBBase;
import jsbsim.FGUnitConverter;
import jsbsim.enums.Control;
import jsbsim.enums.State;
import jsbsim.enums.EulerAngels;
import jsbsim.enums.Positions;
import jsbsim.models.FGLGear;

/** Models an aircraft axis for purposes of trimming.
 */
public class FGTrimAxis extends FGJSBBase {

    private FGFDMExec fdmex;

    private FGInitialCondition fgic;

    private short eState;

    private short eControl;

    private double state_target = 0;

    private double state_value = 0;

    private double control_value = 0.0d;

    private double control_min;

    private double control_max;

    private double tolerance;

    private double solver_eps;

    private double state_convert = 1.0d;

    private double control_convert = 1.0d;

    private int max_iterations = 10;

    private int its_to_stable_value = 0;

    private int total_stability_iterations = 0;

    private int total_iterations = 0;

    public static final double DEFAULT_TOLERANCE = 0.001;

    private static final String StateNames[] = { "all", "udot", "vdot", "wdot", "qdot", "pdot", "rdot", "hmgt", "nlf" };

    private static final String ControlNames[] = { "Throttle", "Sideslip", "Angle of Attack", "Elevator", "Ailerons", "Rudder", "Altitude AGL", "Pitch Angle", "Roll Angle", "Flight Path Angle", "Pitch Trim", "Roll Trim", "Yaw Trim", "Heading" };

    /**  Constructor for Trim Axis class.
    @param fdmex FGFDMExec pointer
    @param IC pointer to initial conditions instance
    @param state a State type (enum)
    @param control a Control type (enum) */
    public FGTrimAxis(FGFDMExec fdex, FGInitialCondition ic, short st, short ctrl) {
        super();
        fdmex = fdex;
        fgic = ic;
        eState = st;
        eControl = ctrl;
        switch(eState) {
            case State.tUdot:
            case State.tVdot:
            case State.tWdot:
                tolerance = DEFAULT_TOLERANCE;
                break;
            case State.tQdot:
            case State.tPdot:
            case State.tRdot:
                tolerance = DEFAULT_TOLERANCE / 10;
                break;
            case State.tHmgt:
                tolerance = 0.01;
                break;
            case State.tNlf:
                state_target = 1.0;
                tolerance = 1E-5;
                break;
            case State.tAll:
                break;
        }
        solver_eps = tolerance;
        switch(eControl) {
            case Control.tThrottle:
                control_min = 0;
                control_max = 1;
                control_value = 0.5;
                break;
            case Control.tBeta:
                control_min = -30 * FGUnitConverter.degtorad;
                control_max = 30 * FGUnitConverter.degtorad;
                control_convert = FGUnitConverter.radtodeg;
                break;
            case Control.tAlpha:
                control_min = fdmex.GetAerodynamics().GetAlphaCLMin();
                control_max = fdmex.GetAerodynamics().GetAlphaCLMax();
                if (control_max <= control_min) {
                    control_max = 20 * FGUnitConverter.degtorad;
                    control_min = -5 * FGUnitConverter.degtorad;
                }
                control_value = (control_min + control_max) / 2;
                control_convert = FGUnitConverter.radtodeg;
                solver_eps = tolerance / 100;
                break;
            case Control.tPitchTrim:
            case Control.tElevator:
            case Control.tRollTrim:
            case Control.tAileron:
            case Control.tYawTrim:
            case Control.tRudder:
                control_min = -1;
                control_max = 1;
                state_convert = FGUnitConverter.radtodeg;
                solver_eps = tolerance / 100;
                break;
            case Control.tAltAGL:
                control_min = 0;
                control_max = 30;
                control_value = fdmex.GetPropagate().GetDistanceAGL();
                solver_eps = tolerance / 100;
                break;
            case Control.tTheta:
                control_min = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.eTht) - 5 * FGUnitConverter.degtorad;
                control_max = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.eTht) + 5 * FGUnitConverter.degtorad;
                state_convert = FGUnitConverter.radtodeg;
                break;
            case Control.tPhi:
                control_min = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.ePhi) - 30 * FGUnitConverter.degtorad;
                control_max = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.ePhi) + 30 * FGUnitConverter.degtorad;
                state_convert = FGUnitConverter.radtodeg;
                control_convert = FGUnitConverter.radtodeg;
                break;
            case Control.tGamma:
                solver_eps = tolerance / 100;
                control_min = -80 * FGUnitConverter.degtorad;
                control_max = 80 * FGUnitConverter.degtorad;
                control_convert = FGUnitConverter.radtodeg;
                break;
            case Control.tHeading:
                control_min = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.ePsi) - 30 * FGUnitConverter.degtorad;
                control_max = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.ePsi) + 30 * FGUnitConverter.degtorad;
                state_convert = FGUnitConverter.radtodeg;
                break;
        }
        Debug(0);
    }

    /** This function iterates through a call to the FGFDMExec::RunIC()
    function until the desired trimming condition falls inside a tolerance.*/
    public void Run() {
        double last_state_value;
        int i;
        setControl();
        i = 0;
        boolean stable = false;
        while (!stable) {
            i++;
            last_state_value = state_value;
            fdmex.RunIC();
            getState();
            if (i > 1) {
                if ((Math.abs(last_state_value - state_value) < tolerance) || (i >= 100)) {
                    stable = true;
                }
            }
        }
        its_to_stable_value = i;
        total_stability_iterations += its_to_stable_value;
        total_iterations++;
    }

    public double GetState() {
        getState();
        return state_value;
    }

    public final void SetControl(double value) {
        control_value = value;
    }

    public final double GetControl() {
        return control_value;
    }

    public final short GetStateType() {
        return eState;
    }

    public final short GetControlType() {
        return eControl;
    }

    public final String GetStateName() {
        return StateNames[eState];
    }

    public final String GetControlName() {
        return ControlNames[eControl];
    }

    public final double GetControlMin() {
        return control_min;
    }

    public final double GetControlMax() {
        return control_max;
    }

    public final void SetControlToMin() {
        control_value = control_min;
    }

    public final void SetControlToMax() {
        control_value = control_max;
    }

    public final void SetControlLimits(double min, double max) {
        control_min = min;
        control_max = max;
    }

    public final void SetTolerance(double ff) {
        tolerance = ff;
    }

    public final double GetTolerance() {
        return tolerance;
    }

    public final double GetSolverEps() {
        return solver_eps;
    }

    public final void SetSolverEps(double ff) {
        solver_eps = ff;
    }

    public final int GetIterationLimit() {
        return max_iterations;
    }

    public final void SetIterationLimit(int ii) {
        max_iterations = ii;
    }

    public final int GetStability() {
        return its_to_stable_value;
    }

    public final int GetRunCount() {
        return total_stability_iterations;
    }

    public double GetAvgStability() {
        if (total_iterations > 0) {
            return (double) total_stability_iterations / total_iterations;
        }
        return 0;
    }

    /**
    the aircraft center of rotation is no longer the cg once the gear
    contact the ground so the altitude needs to be changed when pitch
    and roll angle are adjusted.  Instead of attempting to calculate the
    new center of rotation, pick a gear unit as a reference and use its
    location vector to calculate the new height change. i.e. new altitude =
    earth z component of that vector (which is in body axes )
     */
    public void SetThetaOnGround(double ff) {
        int center = -1, i = 0, ref = -1, gearcount = fdmex.GetGroundReactions().GetNumGearUnits();
        while (ref < 0 && i < gearcount) {
            if (fdmex.GetGroundReactions().GetGearUnit(i).GetWOW()) {
                if (Math.abs(fdmex.GetGroundReactions().GetGearUnit(i).GetBodyLocation(Positions.eY)) > 0.01) {
                    ref = i;
                } else {
                    center = i;
                }
            }
            i++;
        }
        if ((ref < 0) && (center >= 0)) {
            ref = center;
        }
        if (ref >= 0) {
            double sp = fdmex.GetPropagate().GetSinEuler(EulerAngels.ePhi);
            double cp = fdmex.GetPropagate().GetCosEuler(EulerAngels.ePhi);
            double lx = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(Positions.eX);
            double ly = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(Positions.eY);
            double lz = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(Positions.eZ);
            double hagl = -1 * lx * Math.sin(ff) + ly * sp * Math.cos(ff) + lz * cp * Math.cos(ff);
            fgic.SetAltitudeAGLFtIC(hagl);
        }
        fgic.SetThetaRadIC(ff);
    }

    public void SetPhiOnGround(double ff) {
        int i = 0, ref = -1, gearcount = fdmex.GetGroundReactions().GetNumGearUnits();
        while (ref < 0 && i < gearcount) {
            if ((fdmex.GetGroundReactions().GetGearUnit(i).GetWOW()) && (Math.abs(fdmex.GetGroundReactions().GetGearUnit(i).GetBodyLocation(Positions.eY)) > 0.01)) {
                ref = i;
            }
            i++;
        }
        if (ref >= 0) {
            double st = fdmex.GetPropagate().GetSinEuler(EulerAngels.eTht);
            double ct = fdmex.GetPropagate().GetCosEuler(EulerAngels.eTht);
            double lx = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(Positions.eX);
            double ly = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(Positions.eY);
            double lz = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(Positions.eZ);
            double hagl = -1 * lx * st + ly * Math.sin(ff) * ct + lz * Math.cos(ff) * ct;
            fgic.SetAltitudeAGLFtIC(hagl);
        }
        fgic.SetPhiRadIC(ff);
    }

    public final void SetStateTarget(double target) {
        state_target = target;
    }

    public final double GetStateTarget() {
        return state_target;
    }

    public boolean initTheta() {
        int i = -1, N = fdmex.GetGroundReactions().GetNumGearUnits();
        int iForward = 0;
        int iAft = 1;
        double zAft, zForward, zDiff, theta;
        double xAft, xForward, xDiff;
        boolean level;
        double saveAlt = fgic.GetAltitudeAGLFtIC();
        fgic.SetAltitudeAGLFtIC(100);
        FGLGear gear;
        while (++i < N) {
            gear = fdmex.GetGroundReactions().GetGearUnit(i);
            if (0 == iForward && gear.GetBodyLocation(Positions.eX) > 0) {
                iForward = i;
                continue;
            }
            if (1 == iAft && gear.GetBodyLocation(Positions.eX) < 0) {
                iAft = i;
                continue;
            }
        }
        xAft = fdmex.GetGroundReactions().GetGearUnit(iAft).GetBodyLocation(Positions.eX);
        xForward = fdmex.GetGroundReactions().GetGearUnit(iForward).GetBodyLocation(Positions.eX);
        xDiff = xForward - xAft;
        zAft = fdmex.GetGroundReactions().GetGearUnit(iAft).GetLocalGear(Positions.eZ);
        zForward = fdmex.GetGroundReactions().GetGearUnit(iForward).GetLocalGear(Positions.eZ);
        zDiff = zForward - zAft;
        level = false;
        theta = fgic.GetThetaDegIC();
        while (!level && (i < 100)) {
            theta += FGUnitConverter.radtodeg * Math.atan(zDiff / xDiff);
            fgic.SetThetaDegIC(theta);
            fdmex.RunIC();
            zAft = fdmex.GetGroundReactions().GetGearUnit(iAft).GetLocalGear(Positions.eZ);
            zForward = fdmex.GetGroundReactions().GetGearUnit(iForward).GetLocalGear(Positions.eZ);
            zDiff = zForward - zAft;
            if (Math.abs(zDiff) < 0.1) {
                level = true;
            }
            i++;
        }
        if (debug_lvl > 0) {
            double start_th = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.eTht) * FGUnitConverter.radtodeg;
            System.out.println("    Initial Theta: " + start_th);
            System.out.println("    Used gear unit " + iAft + " as aft and " + iForward + " as forward");
        }
        control_min = (theta + 5) * FGUnitConverter.degtorad;
        control_max = (theta - 5) * FGUnitConverter.degtorad;
        fgic.SetAltitudeAGLFtIC(saveAlt);
        return (i < 100);
    }

    public void AxisReport() {
        System.out.println(String.format("  %20s: %6.2f %5s: %9.2e Tolerance: %3.0e", GetControlName(), GetControl() * control_convert, GetStateName(), GetState() + state_target, GetTolerance()));
        System.out.println(Math.abs(GetState() + state_target) < Math.abs(GetTolerance()) ? "  Passed" : "  Failed");
    }

    public boolean InTolerance() {
        getState();
        return (Math.abs(state_value) <= tolerance);
    }

    public void setThrottlesPct() {
        double tMin, tMax;
        int engine_count = fdmex.GetPropulsion().GetNumEngines();
        while (--engine_count > -1) {
            tMin = fdmex.GetPropulsion().GetEngine(engine_count).GetThrottleMin();
            tMax = fdmex.GetPropulsion().GetEngine(engine_count).GetThrottleMax();
            fdmex.GetFCS().SetThrottleCmd(engine_count, tMin + control_value * (tMax - tMin));
            fdmex.RunIC();
            fdmex.GetPropulsion().GetSteadyState();
        }
    }

    public void getState() {
        switch(eState) {
            case State.tUdot:
                state_value = fdmex.GetPropagate().GetUVWdot(Positions.eX) - state_target;
                break;
            case State.tVdot:
                state_value = fdmex.GetPropagate().GetUVWdot(Positions.eY) - state_target;
                break;
            case State.tWdot:
                state_value = fdmex.GetPropagate().GetUVWdot(Positions.eZ) - state_target;
                break;
            case State.tQdot:
                state_value = fdmex.GetPropagate().GetPQRdot(Positions.eY) - state_target;
                break;
            case State.tPdot:
                state_value = fdmex.GetPropagate().GetPQRdot(Positions.eX) - state_target;
                break;
            case State.tRdot:
                state_value = fdmex.GetPropagate().GetPQRdot(Positions.eZ) - state_target;
                break;
            case State.tHmgt:
                state_value = computeHmgt() - state_target;
                break;
            case State.tNlf:
                state_value = fdmex.GetAircraft().GetNlf() - state_target;
                break;
            case State.tAll:
                break;
        }
    }

    public void getControl() {
        switch(eControl) {
            case Control.tThrottle:
                control_value = fdmex.GetFCS().GetThrottleCmd(0);
                break;
            case Control.tBeta:
                control_value = fdmex.GetAuxiliary().Getbeta();
                break;
            case Control.tAlpha:
                control_value = fdmex.GetAuxiliary().Getalpha();
                break;
            case Control.tPitchTrim:
                control_value = fdmex.GetFCS().GetPitchTrimCmd();
                break;
            case Control.tElevator:
                control_value = fdmex.GetFCS().GetDeCmd();
                break;
            case Control.tRollTrim:
            case Control.tAileron:
                control_value = fdmex.GetFCS().GetDaCmd();
                break;
            case Control.tYawTrim:
            case Control.tRudder:
                control_value = fdmex.GetFCS().GetDrCmd();
                break;
            case Control.tAltAGL:
                control_value = fdmex.GetPropagate().GetDistanceAGL();
                break;
            case Control.tTheta:
                control_value = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.eTht);
                break;
            case Control.tPhi:
                control_value = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.ePhi);
                break;
            case Control.tGamma:
                control_value = fdmex.GetAuxiliary().GetGamma();
                break;
            case Control.tHeading:
                control_value = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.ePsi);
                break;
        }
    }

    public void setControl() {
        switch(eControl) {
            case Control.tThrottle:
                setThrottlesPct();
                break;
            case Control.tBeta:
                fgic.SetBetaRadIC(control_value);
                break;
            case Control.tAlpha:
                fgic.SetAlphaRadIC(control_value);
                break;
            case Control.tPitchTrim:
                fdmex.GetFCS().SetPitchTrimCmd(control_value);
                break;
            case Control.tElevator:
                fdmex.GetFCS().SetDeCmd(control_value);
                break;
            case Control.tRollTrim:
            case Control.tAileron:
                fdmex.GetFCS().SetDaCmd(control_value);
                break;
            case Control.tYawTrim:
            case Control.tRudder:
                fdmex.GetFCS().SetDrCmd(control_value);
                break;
            case Control.tAltAGL:
                fgic.SetAltitudeAGLFtIC(control_value);
                break;
            case Control.tTheta:
                fgic.SetThetaRadIC(control_value);
                break;
            case Control.tPhi:
                fgic.SetPhiRadIC(control_value);
                break;
            case Control.tGamma:
                fgic.SetFlightPathAngleRadIC(control_value);
                break;
            case Control.tHeading:
                fgic.SetPsiRadIC(control_value);
                break;
        }
    }

    private double computeHmgt() {
        double diff = fdmex.GetPropagate().GetEuler().GetEntry(EulerAngels.ePsi) - fdmex.GetAuxiliary().GetGroundTrack();
        if (diff < -FGUnitConverter.M_PI) {
            return (diff + 2 * FGUnitConverter.M_PI);
        } else if (diff > FGUnitConverter.M_PI) {
            return (diff - 2 * FGUnitConverter.M_PI);
        } else {
            return diff;
        }
    }

    @Override
    public void Debug(int from) {
        if (debug_lvl <= 0) {
            return;
        }
        if ((debug_lvl & 2) > 0) {
            switch(from) {
                case 0:
                    System.out.println("Instantiated: FGTrimAxis");
                    break;
                case 1:
                    System.out.println("Destroyed:    FGTrimAxis");
                    break;
            }
        }
        if ((debug_lvl & 64) > 0) {
            if (from == 0) {
                System.out.println("$Id$");
            }
        }
    }
}
