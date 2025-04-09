package jsbsim.initialization;

import jsbsim.FGFDMExec;
import jsbsim.FGJSBBase;
import jsbsim.FGUnitConverter;
import jsbsim.enums.EulerAngels;
import jsbsim.enums.TaControl;
import jsbsim.enums.TaState;

/** Models an aircraft control variables for purposes of trimming.
 */
public class FGTrimAnalysisControl extends FGJSBBase {

    private FGFDMExec fdmex;

    private FGInitialCondition fgic;

    private short eTaState;

    private short eTaControl;

    private String control_name;

    private double state_target = 0.0d, state_value = 0.0d, control_value = 0.0d, control_min, control_max, control_initial_value = 0.0d, control_step, control_tolerance, state_convert = 1.0d, control_convert = 1.0d;

    /**  Constructor for Trim Analysis Control class.
    @param fdmex FGFDMExec pointer
    @param IC pointer to initial conditions instance
    @param control a Control type (enum) */
    public FGTrimAnalysisControl(FGFDMExec fdex, FGInitialCondition ic, short ctrl) {
        super();
        double degtorad = FGUnitConverter.degtorad, radtodeg = FGUnitConverter.radtodeg;
        fdmex = fdex;
        fgic = ic;
        eTaState = TaState.taAll;
        eTaControl = ctrl;
        control_tolerance = FGTrimAxis.DEFAULT_TOLERANCE;
        switch(eTaControl) {
            case TaControl.taThrottle:
                control_min = 0;
                control_max = 1;
                control_step = 0.2;
                control_initial_value = 0.5;
                control_value = control_initial_value;
                control_name = "Throttle (cmd,norm)";
                break;
            case TaControl.taBeta:
                control_min = -30 * degtorad;
                control_max = 30 * degtorad;
                control_step = 1 * degtorad;
                control_convert = radtodeg;
                break;
            case TaControl.taAlpha:
                control_min = fdmex.GetAerodynamics().GetAlphaCLMin();
                control_max = fdmex.GetAerodynamics().GetAlphaCLMax();
                if (control_max <= control_min) {
                    control_max = 20 * degtorad;
                    control_min = -5 * degtorad;
                }
                control_step = 1 * degtorad;
                control_initial_value = (control_min + control_max) / 2;
                control_value = control_initial_value;
                control_convert = radtodeg;
                break;
            case TaControl.taPitchTrim:
                control_name = "Pitch Trim (cmd,norm)";
                control_min = -1;
                control_max = 1;
                control_step = 0.1;
                state_convert = radtodeg;
                break;
            case TaControl.taElevator:
                control_name = "Elevator (cmd,norm)";
                control_min = -1;
                control_max = 1;
                control_step = 0.1;
                state_convert = radtodeg;
                break;
            case TaControl.taRollTrim:
                control_name = "Roll Trim (cmd,norm)";
                control_min = -1;
                control_max = 1;
                control_step = 0.1;
                state_convert = radtodeg;
                break;
            case TaControl.taAileron:
                control_name = "Ailerons (cmd,norm)";
                control_min = -1;
                control_max = 1;
                control_step = 0.1;
                state_convert = radtodeg;
                break;
            case TaControl.taYawTrim:
                control_name = "Yaw Trim (cmd,norm)";
                control_min = -1;
                control_max = 1;
                control_step = 0.1;
                state_convert = radtodeg;
                break;
            case TaControl.taRudder:
                control_name = "Rudder (cmd,norm)";
                control_min = -1;
                control_max = 1;
                control_step = 0.1;
                state_convert = radtodeg;
                break;
            case TaControl.taAltAGL:
                control_name = "Altitude (ft)";
                control_min = 0;
                control_max = 30;
                control_step = 2;
                control_initial_value = fdmex.GetPropagate().GetDistanceAGL();
                control_value = control_initial_value;
                break;
            case TaControl.taPhi:
                control_name = "Phi (rad)";
                control_min = fdmex.GetPropagate().GetEuler(EulerAngels.ePhi) - 30 * degtorad;
                control_max = fdmex.GetPropagate().GetEuler(EulerAngels.ePhi) + 30 * degtorad;
                control_step = 1 * degtorad;
                state_convert = radtodeg;
                control_convert = radtodeg;
                break;
            case TaControl.taTheta:
                control_name = "Theta (rad)";
                control_min = fdmex.GetPropagate().GetEuler(EulerAngels.eTht) - 5 * degtorad;
                control_max = fdmex.GetPropagate().GetEuler(EulerAngels.eTht) + 5 * degtorad;
                control_step = 1 * degtorad;
                state_convert = radtodeg;
                break;
            case TaControl.taHeading:
                control_name = "Heading (rad)";
                control_min = fdmex.GetPropagate().GetEuler(EulerAngels.ePsi) - 30 * degtorad;
                control_max = fdmex.GetPropagate().GetEuler(EulerAngels.ePsi) + 30 * degtorad;
                control_step = 1 * degtorad;
                state_convert = radtodeg;
                break;
            case TaControl.taGamma:
                control_name = "Gamma (rad)";
                control_min = -80 * degtorad;
                control_max = 80 * degtorad;
                control_step = 1 * degtorad;
                control_convert = radtodeg;
                break;
        }
        Debug(0);
    }

    /** This function iterates through a call to the FGFDMExec::RunIC()
    function until the desired trimming condition falls inside a tolerance.*/
    public void Run() {
    }

    /** Sets the control value
    @param value
     */
    public final void SetControl(double value) {
        control_value = value;
    }

    /** Gets the control value
    @return value
     */
    public final double GetControl() {
        return control_value;
    }

    /** Return the control type
    @return TaControl
     */
    public final short GetControlType() {
        return eTaControl;
    }

    /** Gets the control name
    @return control name
     */
    public final String GetControlName() {
        return control_name;
    }

    /** Gets the control minimum value
    @return control min value
     */
    public final double GetControlMin() {
        return control_min;
    }

    /** Gets the control maximum value
    @return control nax value
     */
    public final double GetControlMax() {
        return control_max;
    }

    /** Set control step
    @param value of control step
     */
    public final void SetControlStep(double value) {
        control_step = value;
    }

    /** Get control step
    @return value of control step
     */
    public final double GetControlStep() {
        return control_step;
    }

    /** Set control initial value
    @param value of control initial value
     */
    public final void SetControlInitialValue(double value) {
        control_initial_value = value;
    }

    /** Get control step
    @return value of control initial value
     */
    public final double GetControlInitialValue() {
        return control_initial_value;
    }

    /** Set control value to minimum
     */
    public final void SetControlToMin() {
        control_value = control_min;
    }

    /** Set control value to maximum
     */
    public final void SetControlToMax() {
        control_value = control_max;
    }

    /** Set both control limits
    @param max control max
    @param min control min
     */
    public final void SetControlLimits(double min, double max) {
        control_min = min;
        control_max = max;
    }

    /** Set control tolerance
    @param ff value of control tolerance
     */
    public final void SetTolerance(double ff) {
        control_tolerance = ff;
    }

    /** Get control tolerance
    @return value of control tolerance
     */
    public final double GetTolerance() {
        return control_tolerance;
    }

    /** Set theta value on ground for trim
    the aircraft center of rotation is no longer the cg once the gear
    contact the ground so the altitude needs to be changed when pitch
    and roll angle are adjusted.  Instead of attempting to calculate the
    new center of rotation, pick a gear unit as a reference and use its
    location vector to calculate the new height change. i.e. new altitude =
    earth z component of that vector (which is in body axes )
    @param ff
     */
    public void SetThetaOnGround(double ff) {
        int center = -1, i = 0, ref = -1;
        while ((ref < 0) && (i < fdmex.GetGroundReactions().GetNumGearUnits())) {
            if (fdmex.GetGroundReactions().GetGearUnit(i).GetWOW()) {
                if (Math.abs(fdmex.GetGroundReactions().GetGearUnit(i).GetBodyLocation(2)) > 0.01) {
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
        System.out.println("");
        System.out.println("SetThetaOnGround ref gear: " + ref);
        if (ref >= 0) {
            double sp = fdmex.GetPropagate().GetSinEuler(EulerAngels.ePhi);
            double cp = fdmex.GetPropagate().GetCosEuler(EulerAngels.ePhi);
            double lx = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(1);
            double ly = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(2);
            double lz = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(3);
            double hagl = -1 * lx * Math.sin(ff) + ly * sp * Math.cos(ff) + lz * cp * Math.cos(ff);
            fgic.SetAltitudeAGLFtIC(hagl);
            System.out.println("SetThetaOnGround new alt: " + hagl);
        }
        fgic.SetThetaRadIC(ff);
        System.out.println("SetThetaOnGround new theta: " + ff);
    }

    /** Set phi value on ground for trim
    @param ff
     */
    public void SetPhiOnGround(double ff) {
        int i = 0, ref = -1;
        while ((ref < 0) && (i < fdmex.GetGroundReactions().GetNumGearUnits())) {
            if ((fdmex.GetGroundReactions().GetGearUnit(i).GetWOW()) && (Math.abs(fdmex.GetGroundReactions().GetGearUnit(i).GetBodyLocation(2)) > 0.01)) {
                ref = i;
            }
            i++;
        }
        if (ref >= 0) {
            double st = fdmex.GetPropagate().GetSinEuler(EulerAngels.eTht);
            double ct = fdmex.GetPropagate().GetCosEuler(EulerAngels.eTht);
            double lx = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(1);
            double ly = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(2);
            double lz = fdmex.GetGroundReactions().GetGearUnit(ref).GetBodyLocation(3);
            double hagl = -1 * lx * st + ly * Math.sin(ff) * ct + lz * Math.cos(ff) * ct;
            fgic.SetAltitudeAGLFtIC(hagl);
        }
        fgic.SetPhiRadIC(ff);
    }

    /** Set target state value for trim
    @param target
     */
    public final void SetStateTarget(double target) {
        state_target = target;
    }

    /** Get target state value for trim
    @return state target
    //  */
    public final double GetStateTarget() {
        return state_target;
    }

    /** Calculate steady state thetas value on ground
    @return true if successful
     */
    public boolean initTheta() {
        int i, N;
        int iForward = 0;
        int iAft = 1;
        double zAft, zForward, zDiff, theta;
        double xAft, xForward, xDiff;
        boolean level;
        double saveAlt;
        saveAlt = fgic.GetAltitudeAGLFtIC();
        fgic.SetAltitudeAGLFtIC(100);
        N = fdmex.GetGroundReactions().GetNumGearUnits();
        for (i = 0; i < N; i++) {
            if (fdmex.GetGroundReactions().GetGearUnit(i).GetBodyLocation(1) > 0) {
                iForward = i;
                break;
            }
        }
        for (i = 0; i < N; i++) {
            if (fdmex.GetGroundReactions().GetGearUnit(i).GetBodyLocation(1) < 0) {
                iAft = i;
                break;
            }
        }
        xAft = fdmex.GetGroundReactions().GetGearUnit(iAft).GetBodyLocation(1);
        xForward = fdmex.GetGroundReactions().GetGearUnit(iForward).GetBodyLocation(1);
        xDiff = xForward - xAft;
        zAft = fdmex.GetGroundReactions().GetGearUnit(iAft).GetLocalGear(3);
        zForward = fdmex.GetGroundReactions().GetGearUnit(iForward).GetLocalGear(3);
        zDiff = zForward - zAft;
        level = false;
        theta = fgic.GetThetaDegIC();
        while (!level && (i < 100)) {
            theta += FGUnitConverter.radtodeg * Math.atan(zDiff / xDiff);
            fgic.SetThetaDegIC(theta);
            fdmex.RunIC();
            zAft = fdmex.GetGroundReactions().GetGearUnit(iAft).GetLocalGear(3);
            zForward = fdmex.GetGroundReactions().GetGearUnit(iForward).GetLocalGear(3);
            zDiff = zForward - zAft;
            if (Math.abs(zDiff) < 0.1) {
                level = true;
            }
            i++;
        }
        if (debug_lvl > 0) {
            System.out.println("    Initial Theta: " + fdmex.GetPropagate().GetEuler(EulerAngels.eTht) * FGUnitConverter.radtodeg);
            System.out.println("    Used gear unit " + iAft + " as aft and " + iForward + " as forward");
        }
        control_min = (theta + 5) * FGUnitConverter.degtorad;
        control_max = (theta - 5) * FGUnitConverter.degtorad;
        fgic.SetAltitudeAGLFtIC(saveAlt);
        return (i < 100);
    }

    public void setThrottlesPct() {
        double tMin, tMax;
        for (int i = 0; i < fdmex.GetPropulsion().GetNumEngines(); i++) {
            tMin = fdmex.GetPropulsion().GetEngine(i).GetThrottleMin();
            tMax = fdmex.GetPropulsion().GetEngine(i).GetThrottleMax();
            fdmex.GetFCS().SetThrottleCmd(i, tMin + control_value * (tMax - tMin));
            fdmex.RunIC();
            fdmex.GetPropulsion().GetSteadyState();
        }
    }

    public double getState() {
        switch(eTaState) {
            case TaState.taUdot:
                state_value = fdmex.GetPropagate().GetUVWdot(1) - state_target;
                break;
            case TaState.taVdot:
                state_value = fdmex.GetPropagate().GetUVWdot(2) - state_target;
                break;
            case TaState.taWdot:
                state_value = fdmex.GetPropagate().GetUVWdot(3) - state_target;
                break;
            case TaState.taPdot:
                state_value = fdmex.GetPropagate().GetPQRdot(1) - state_target;
                break;
            case TaState.taQdot:
                state_value = fdmex.GetPropagate().GetPQRdot(2) - state_target;
                break;
            case TaState.taRdot:
                state_value = fdmex.GetPropagate().GetPQRdot(3) - state_target;
                break;
            case TaState.taHmgt:
                state_value = computeHmgt() - state_target;
                break;
            case TaState.taNlf:
                state_value = fdmex.GetAircraft().GetNlf() - state_target;
                break;
            case TaState.taAll:
                break;
        }
        return state_value;
    }

    public void getControl() {
        switch(eTaControl) {
            case TaControl.taThrottle:
                control_value = fdmex.GetFCS().GetThrottleCmd(0);
                break;
            case TaControl.taBeta:
                control_value = fdmex.GetAuxiliary().Getbeta();
                break;
            case TaControl.taAlpha:
                control_value = fdmex.GetAuxiliary().Getalpha();
                break;
            case TaControl.taPitchTrim:
                control_value = fdmex.GetFCS().GetPitchTrimCmd();
                break;
            case TaControl.taElevator:
                control_value = fdmex.GetFCS().GetDeCmd();
                break;
            case TaControl.taRollTrim:
            case TaControl.taAileron:
                control_value = fdmex.GetFCS().GetDaCmd();
                break;
            case TaControl.taYawTrim:
            case TaControl.taRudder:
                control_value = fdmex.GetFCS().GetDrCmd();
                break;
            case TaControl.taAltAGL:
                control_value = fdmex.GetPropagate().GetDistanceAGL();
                break;
            case TaControl.taTheta:
                control_value = fdmex.GetPropagate().GetEuler(EulerAngels.eTht);
                break;
            case TaControl.taPhi:
                control_value = fdmex.GetPropagate().GetEuler(EulerAngels.ePhi);
                break;
            case TaControl.taGamma:
                control_value = fdmex.GetAuxiliary().GetGamma();
                break;
            case TaControl.taHeading:
                control_value = fdmex.GetPropagate().GetEuler(EulerAngels.ePsi);
                break;
        }
    }

    public void setControl() {
        switch(eTaControl) {
            case TaControl.taThrottle:
                setThrottlesPct();
                break;
            case TaControl.taBeta:
                fgic.SetBetaRadIC(control_value);
                break;
            case TaControl.taAlpha:
                fgic.SetAlphaRadIC(control_value);
                break;
            case TaControl.taPitchTrim:
                fdmex.GetFCS().SetPitchTrimCmd(control_value);
                break;
            case TaControl.taElevator:
                fdmex.GetFCS().SetDeCmd(control_value);
                break;
            case TaControl.taRollTrim:
            case TaControl.taAileron:
                fdmex.GetFCS().SetDaCmd(control_value);
                break;
            case TaControl.taYawTrim:
            case TaControl.taRudder:
                fdmex.GetFCS().SetDrCmd(control_value);
                break;
            case TaControl.taAltAGL:
                fgic.SetAltitudeAGLFtIC(control_value);
                break;
            case TaControl.taTheta:
                fgic.SetThetaRadIC(control_value);
                break;
            case TaControl.taPhi:
                fgic.SetPhiRadIC(control_value);
                break;
            case TaControl.taGamma:
                fgic.SetFlightPathAngleRadIC(control_value);
                break;
            case TaControl.taHeading:
                fgic.SetPsiRadIC(control_value);
                break;
        }
    }

    private double computeHmgt() {
        double diff = fdmex.GetPropagate().GetEuler(EulerAngels.ePsi) - fdmex.GetAuxiliary().GetGroundTrack();
        if (diff < -FGUnitConverter.M_PI) {
            return (diff + 2 * FGUnitConverter.M_PI);
        }
        if (diff > FGUnitConverter.M_PI) {
            return (diff - 2 * FGUnitConverter.M_PI);
        }
        return diff;
    }

    @Override
    public void Debug(int from) {
    }
}
