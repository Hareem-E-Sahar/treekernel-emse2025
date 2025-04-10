package com.safi.asterisk.actionstep.impl;

import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import com.safi.asterisk.Call;
import com.safi.asterisk.actionstep.ActionstepPackage;
import com.safi.asterisk.actionstep.BackgroundDetect;
import com.safi.core.actionstep.ActionStepException;
import com.safi.core.actionstep.ActionStepFactory;
import com.safi.core.actionstep.Output;
import com.safi.core.actionstep.OutputType;
import com.safi.core.actionstep.impl.ActionStepImpl;
import com.safi.core.call.CallConsumer1;
import com.safi.core.call.CallPackage;
import com.safi.core.call.SafiCall;
import com.safi.core.saflet.SafletContext;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Background Detect</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link com.safi.asterisk.actionstep.impl.BackgroundDetectImpl#getCall1 <em>Call1</em>}</li>
 *   <li>{@link com.safi.asterisk.actionstep.impl.BackgroundDetectImpl#getFilename <em>Filename</em>}</li>
 *   <li>{@link com.safi.asterisk.actionstep.impl.BackgroundDetectImpl#getSilence <em>Silence</em>}</li>
 *   <li>{@link com.safi.asterisk.actionstep.impl.BackgroundDetectImpl#getMinTime <em>Min Time</em>}</li>
 *   <li>{@link com.safi.asterisk.actionstep.impl.BackgroundDetectImpl#getMaxTime <em>Max Time</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BackgroundDetectImpl extends AsteriskActionStepImpl implements BackgroundDetect {

    /**
	 * The cached value of the '{@link #getCall1() <em>Call1</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getCall1()
	 * @generated
	 * @ordered
	 */
    protected SafiCall call1;

    /**
	 * The default value of the '{@link #getFilename() <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFilename()
	 * @generated
	 * @ordered
	 */
    protected static final String FILENAME_EDEFAULT = null;

    /**
	 * The cached value of the '{@link #getFilename() <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFilename()
	 * @generated
	 * @ordered
	 */
    protected String filename = FILENAME_EDEFAULT;

    /**
	 * The default value of the '{@link #getSilence() <em>Silence</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getSilence()
	 * @generated
	 * @ordered
	 */
    protected static final int SILENCE_EDEFAULT = 1000;

    /**
	 * The cached value of the '{@link #getSilence() <em>Silence</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getSilence()
	 * @generated
	 * @ordered
	 */
    protected int silence = SILENCE_EDEFAULT;

    /**
	 * The default value of the '{@link #getMinTime() <em>Min Time</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getMinTime()
	 * @generated
	 * @ordered
	 */
    protected static final int MIN_TIME_EDEFAULT = 100;

    /**
	 * The cached value of the '{@link #getMinTime() <em>Min Time</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getMinTime()
	 * @generated
	 * @ordered
	 */
    protected int minTime = MIN_TIME_EDEFAULT;

    /**
	 * The default value of the '{@link #getMaxTime() <em>Max Time</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getMaxTime()
	 * @generated
	 * @ordered
	 */
    protected static final int MAX_TIME_EDEFAULT = 0;

    /**
	 * The cached value of the '{@link #getMaxTime() <em>Max Time</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getMaxTime()
	 * @generated
	 * @ordered
	 */
    protected int maxTime = MAX_TIME_EDEFAULT;

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    protected BackgroundDetectImpl() {
        super();
    }

    @Override
    public void beginProcessing(SafletContext context) throws ActionStepException {
        super.beginProcessing(context);
        Exception exception = null;
        boolean speechDetected = false;
        if (call1 == null) {
            handleException(context, new ActionStepException("No current call found"));
            return;
        } else if (!(call1 instanceof Call)) {
            handleException(context, new ActionStepException("Call isn't isn't an Asterisk call: " + call1.getClass().getName()));
            return;
        }
        if (((Call) call1).getChannel() == null) {
            handleException(context, new ActionStepException("No channel found in current context"));
            return;
        }
        {
            AgiChannel channel = ((Call) call1).getChannel();
            try {
                StringBuffer appCmd = new StringBuffer(filename);
                appCmd.append('|').append(silence).append('|').append(minTime);
                if (maxTime > 0) appCmd.append('|').append(maxTime);
                int result = channel.exec("BackgroundDetect", appCmd.toString());
                if (debugLog.isLoggable(Level.FINEST)) {
                    debug("BackgroundDetect returned " + result);
                }
                if (result == -2) {
                    exception = new ActionStepException("Application BackgroundDetect not found");
                } else if (result == -1) {
                    context.addException(new ActionStepException("Channel was hung up"));
                    return;
                }
                String talk = channel.getVariable("TALK_DETECTED");
                if (debugLog.isLoggable(Level.FINEST)) {
                    debug("TALK_DETECTED var was " + talk);
                }
                if (talk != null) {
                    try {
                        int speechDuration = Integer.parseInt(talk);
                        if (speechDuration > 0) speechDetected = true;
                        if (debugLog.isLoggable(Level.FINEST)) {
                            debug("Speech duration was " + speechDuration);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            } catch (Exception e) {
                exception = e;
            }
        }
        if (exception != null) {
            handleException(context, exception);
            return;
        }
        handleSuccess(context, speechDetected ? 2 : 1);
    }

    @Override
    public void createDefaultOutputs() {
        super.createDefaultOutputs();
        Output o = ActionStepFactory.eINSTANCE.createOutput();
        o.setOutputType(OutputType.CHOICE);
        o.setName("speech");
        getOutputs().add(o);
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    protected EClass eStaticClass() {
        return ActionstepPackage.Literals.BACKGROUND_DETECT;
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public SafiCall getCall1() {
        if (call1 != null && call1.eIsProxy()) {
            InternalEObject oldCall1 = (InternalEObject) call1;
            call1 = (SafiCall) eResolveProxy(oldCall1);
            if (call1 != oldCall1) {
                if (eNotificationRequired()) eNotify(new ENotificationImpl(this, Notification.RESOLVE, ActionstepPackage.BACKGROUND_DETECT__CALL1, oldCall1, call1));
            }
        }
        return call1;
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public SafiCall basicGetCall1() {
        return call1;
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public void setCall1(SafiCall newCall1) {
        SafiCall oldCall1 = call1;
        call1 = newCall1;
        if (eNotificationRequired()) eNotify(new ENotificationImpl(this, Notification.SET, ActionstepPackage.BACKGROUND_DETECT__CALL1, oldCall1, call1));
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public String getFilename() {
        return filename;
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public void setFilename(String newFilename) {
        String oldFilename = filename;
        filename = newFilename;
        if (eNotificationRequired()) eNotify(new ENotificationImpl(this, Notification.SET, ActionstepPackage.BACKGROUND_DETECT__FILENAME, oldFilename, filename));
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public int getSilence() {
        return silence;
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public void setSilence(int newSilence) {
        int oldSilence = silence;
        silence = newSilence;
        if (eNotificationRequired()) eNotify(new ENotificationImpl(this, Notification.SET, ActionstepPackage.BACKGROUND_DETECT__SILENCE, oldSilence, silence));
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public int getMinTime() {
        return minTime;
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public void setMinTime(int newMinTime) {
        int oldMinTime = minTime;
        minTime = newMinTime;
        if (eNotificationRequired()) eNotify(new ENotificationImpl(this, Notification.SET, ActionstepPackage.BACKGROUND_DETECT__MIN_TIME, oldMinTime, minTime));
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public int getMaxTime() {
        return maxTime;
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public void setMaxTime(int newMaxTime) {
        int oldMaxTime = maxTime;
        maxTime = newMaxTime;
        if (eNotificationRequired()) eNotify(new ENotificationImpl(this, Notification.SET, ActionstepPackage.BACKGROUND_DETECT__MAX_TIME, oldMaxTime, maxTime));
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch(featureID) {
            case ActionstepPackage.BACKGROUND_DETECT__CALL1:
                if (resolve) return getCall1();
                return basicGetCall1();
            case ActionstepPackage.BACKGROUND_DETECT__FILENAME:
                return getFilename();
            case ActionstepPackage.BACKGROUND_DETECT__SILENCE:
                return getSilence();
            case ActionstepPackage.BACKGROUND_DETECT__MIN_TIME:
                return getMinTime();
            case ActionstepPackage.BACKGROUND_DETECT__MAX_TIME:
                return getMaxTime();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch(featureID) {
            case ActionstepPackage.BACKGROUND_DETECT__CALL1:
                setCall1((SafiCall) newValue);
                return;
            case ActionstepPackage.BACKGROUND_DETECT__FILENAME:
                setFilename((String) newValue);
                return;
            case ActionstepPackage.BACKGROUND_DETECT__SILENCE:
                setSilence((Integer) newValue);
                return;
            case ActionstepPackage.BACKGROUND_DETECT__MIN_TIME:
                setMinTime((Integer) newValue);
                return;
            case ActionstepPackage.BACKGROUND_DETECT__MAX_TIME:
                setMaxTime((Integer) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public void eUnset(int featureID) {
        switch(featureID) {
            case ActionstepPackage.BACKGROUND_DETECT__CALL1:
                setCall1((SafiCall) null);
                return;
            case ActionstepPackage.BACKGROUND_DETECT__FILENAME:
                setFilename(FILENAME_EDEFAULT);
                return;
            case ActionstepPackage.BACKGROUND_DETECT__SILENCE:
                setSilence(SILENCE_EDEFAULT);
                return;
            case ActionstepPackage.BACKGROUND_DETECT__MIN_TIME:
                setMinTime(MIN_TIME_EDEFAULT);
                return;
            case ActionstepPackage.BACKGROUND_DETECT__MAX_TIME:
                setMaxTime(MAX_TIME_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public boolean eIsSet(int featureID) {
        switch(featureID) {
            case ActionstepPackage.BACKGROUND_DETECT__CALL1:
                return call1 != null;
            case ActionstepPackage.BACKGROUND_DETECT__FILENAME:
                return FILENAME_EDEFAULT == null ? filename != null : !FILENAME_EDEFAULT.equals(filename);
            case ActionstepPackage.BACKGROUND_DETECT__SILENCE:
                return silence != SILENCE_EDEFAULT;
            case ActionstepPackage.BACKGROUND_DETECT__MIN_TIME:
                return minTime != MIN_TIME_EDEFAULT;
            case ActionstepPackage.BACKGROUND_DETECT__MAX_TIME:
                return maxTime != MAX_TIME_EDEFAULT;
        }
        return super.eIsSet(featureID);
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
        if (baseClass == CallConsumer1.class) {
            switch(derivedFeatureID) {
                case ActionstepPackage.BACKGROUND_DETECT__CALL1:
                    return CallPackage.CALL_CONSUMER1__CALL1;
                default:
                    return -1;
            }
        }
        return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
        if (baseClass == CallConsumer1.class) {
            switch(baseFeatureID) {
                case CallPackage.CALL_CONSUMER1__CALL1:
                    return ActionstepPackage.BACKGROUND_DETECT__CALL1;
                default:
                    return -1;
            }
        }
        return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
    }

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();
        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (filename: ");
        result.append(filename);
        result.append(", silence: ");
        result.append(silence);
        result.append(", minTime: ");
        result.append(minTime);
        result.append(", maxTime: ");
        result.append(maxTime);
        result.append(')');
        return result.toString();
    }
}
