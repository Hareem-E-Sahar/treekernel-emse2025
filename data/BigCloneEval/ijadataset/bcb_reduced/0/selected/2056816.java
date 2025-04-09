package org.nakedobjects.nof.reflect.java.reflect;

import org.nakedobjects.noa.NakedObjectRuntimeException;
import org.nakedobjects.noa.adapter.Naked;
import org.nakedobjects.noa.adapter.NakedCollection;
import org.nakedobjects.noa.adapter.NakedReference;
import org.nakedobjects.noa.annotations.Annotation;
import org.nakedobjects.noa.annotations.When;
import org.nakedobjects.noa.exceptions.DisabledDeclarativelyException;
import org.nakedobjects.noa.reflect.Consent;
import org.nakedobjects.noa.reflect.NakedObjectAction;
import org.nakedobjects.noa.reflect.SpecObjectPair;
import org.nakedobjects.noa.spec.NakedObjectSpecification;
import org.nakedobjects.nof.core.context.NakedObjectsContext;
import org.nakedobjects.nof.core.persist.TransactionException;
import org.nakedobjects.nof.core.reflect.Allow;
import org.nakedobjects.nof.core.reflect.Veto;
import org.nakedobjects.nof.core.util.DebugString;
import org.nakedobjects.nof.reflect.java.annotations.AnnotationFactorySet;
import org.nakedobjects.nof.reflect.peer.ActionParamPeer;
import org.nakedobjects.nof.reflect.peer.MemberIdentifier;
import org.nakedobjects.nof.reflect.peer.ReflectionException;
import org.nakedobjects.nof.reflect.peer.ReflectiveActionException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class JavaAction extends JavaMember implements ExtensionHolderMutableActionPeer {

    private static final Logger LOG = Logger.getLogger(JavaAction.class);

    private static final Map wrapperClasses = new HashMap();

    static {
        wrapperClasses.put(boolean.class, Boolean.class);
        wrapperClasses.put(byte.class, Byte.class);
        wrapperClasses.put(short.class, Short.class);
        wrapperClasses.put(int.class, Integer.class);
        wrapperClasses.put(long.class, Long.class);
        wrapperClasses.put(float.class, Float.class);
        wrapperClasses.put(double.class, Double.class);
    }

    private final Method actionMethod;

    private final Method validMethod;

    private final MemberHelper descriptionsMethod;

    private final MemberHelper namesMethod;

    private final MemberHelper optionalParametersMethod;

    private final MemberHelper parameterOptions;

    private final MemberHelper defaultParametersMethod;

    public JavaAction(final MemberIdentifier identifier, final NakedObjectAction.Type type, final NakedObjectAction.Target target, final NakedObjectSpecification onType, final NakedObjectSpecification[] parameterSpecs, final Method actionMethod, final NakedObjectSpecification returnType, final ParameterMethods parameterMethods, final DescriptiveMethods descriptiveMethods, final GeneralControlMethods controlMethods, final ActionFlags actionFlags, final MemberSessionMethods sessionMethods, final AnnotationFactorySet annotationFactorySet) {
        super(identifier, descriptiveMethods, controlMethods, actionFlags.isProtected(), actionFlags.isHidden(), sessionMethods);
        this.type = type;
        this.actionMethod = remember(actionMethod);
        this.target = target;
        this.onType = onType;
        this.returnType = returnType;
        this.isInstanceMethod = !Modifier.isStatic(actionMethod.getModifiers());
        this.validMethod = remember(controlMethods.getValidMethod1());
        this.namesMethod = parameterMethods.getNamesMethod();
        this.descriptionsMethod = remember(parameterMethods.getDescriptionsMethod());
        this.optionalParametersMethod = remember(parameterMethods.getOptionalMethod());
        this.defaultParametersMethod = remember(parameterMethods.getDefaultsMethod());
        this.parameterOptions = remember(parameterMethods.getOptionsMethod());
        this.canWrap = parameterMethods.canWrap();
        this.maxLengths = parameterMethods.getMaxLengths();
        this.typicalLengths = parameterMethods.getTypicalLengths();
        this.noLines = parameterMethods.getNoLines();
        this.paramCount = actionMethod.getParameterTypes().length;
        this.parameterSpecs = new NakedObjectSpecification[paramCount];
        this.parameters = new JavaActionParam[paramCount];
        for (int i = 0; i < this.parameterSpecs.length; i++) {
            this.parameterSpecs[i] = parameterSpecs[i];
            final JavaActionParam javaActionParam = new JavaActionParam(parameterSpecs[i]);
            final Annotation[] annotations = annotationFactorySet.processParams(actionMethod, i);
            AnnotationUtil.addAnnotations(javaActionParam, annotations);
            this.parameters[i] = javaActionParam;
        }
    }

    private final NakedObjectAction.Target target;

    private final NakedObjectAction.Type type;

    private final boolean isInstanceMethod;

    private final NakedObjectSpecification onType;

    private final NakedObjectSpecification returnType;

    public NakedObjectSpecification getOnType() {
        return onType;
    }

    public NakedObjectSpecification getReturnType() {
        return returnType;
    }

    public NakedObjectAction.Target getTarget() {
        return target;
    }

    public NakedObjectAction.Type getType() {
        return type;
    }

    public boolean isOnInstance() {
        return isInstanceMethod;
    }

    public Naked execute(final NakedReference inObject, final Naked[] parameters) throws ReflectiveActionException {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }
        try {
            Object[] executionParameters = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                executionParameters[i] = domainObject(parameters[i]);
            }
            Object object = domainObject(inObject);
            Object result = actionMethod.invoke(object, executionParameters);
            LOG.debug(" action result " + result);
            if (result == null) {
                return null;
            }
            return NakedObjectsContext.getObjectLoader().getAdapter(new SpecObjectPair(getReturnType(), result));
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof TransactionException) {
                throw new ReflectiveActionException("TransactionException thrown while executing " + actionMethod + " " + e.getTargetException().getMessage(), e.getTargetException());
            } else {
                invocationException("Exception executing " + actionMethod, e);
                return null;
            }
        } catch (IllegalAccessException e) {
            throw new ReflectiveActionException("Illegal access of " + actionMethod, e);
        }
    }

    private final int paramCount;

    private final JavaActionParam[] parameters;

    private final NakedObjectSpecification[] parameterSpecs;

    private final int[] maxLengths;

    private final int[] typicalLengths;

    private final int[] noLines;

    private final boolean[] canWrap;

    public int getParameterCount() {
        return paramCount;
    }

    public ActionParamPeer[] getParameters() {
        return parameters;
    }

    public String[] getParameterNames() {
        try {
            String[] labels = (String[]) namesMethod.execute(null);
            return labels;
        } catch (IllegalArgumentException e) {
            throw new ReflectionException("Failed to execute associated method for action " + getIdentifier() + ": " + namesMethod, e);
        }
    }

    public NakedObjectSpecification[] getParameterTypes() {
        return parameterSpecs;
    }

    public String[] getParameterDescriptions() {
        try {
            String[] descriptions = (String[]) descriptionsMethod.execute(null);
            return descriptions;
        } catch (IllegalArgumentException e) {
            throw new ReflectionException("Failed to execute associated method for action " + getIdentifier() + ": " + namesMethod, e);
        }
    }

    public boolean[] getOptionalParameters() {
        return (boolean[]) optionalParametersMethod.execute(null);
    }

    public Object[] getParameterDefaults(NakedReference target) {
        return (Object[]) defaultParametersMethod.execute(domainObject(target));
    }

    public Object[][] getParameterOptions(NakedReference target) {
        Object[] options = (Object[]) parameterOptions.execute(domainObject(target));
        if (options == null) {
            return new Object[getParameterCount()][];
        }
        Object[][] array = new Object[options.length][];
        for (int i = 0; i < options.length; i++) {
            Object option = options[i];
            if (option == null) {
                continue;
            } else if (option.getClass().isArray()) {
                Class arrayType = option.getClass().getComponentType();
                if (arrayType.isPrimitive()) {
                    if (arrayType == char.class) {
                        array[i] = convertCharToCharaterArray(options[i]);
                    } else {
                        array[i] = convertPrimitiveToObjectArray(arrayType, options[i]);
                    }
                } else {
                    array[i] = (Object[]) option;
                }
            } else {
                NakedCollection adapter = NakedObjectsContext.getObjectLoader().createAdapterForCollection(option, getParameterTypes()[i]);
                Enumeration e = adapter.elements();
                Object[] optionArray = new Object[adapter.size()];
                int j = 0;
                while (e.hasMoreElements()) {
                    optionArray[j++] = ((Naked) e.nextElement()).getObject();
                }
                array[i] = optionArray;
            }
        }
        return array;
    }

    private Object[] convertCharToCharaterArray(Object originalArray) {
        char[] original = (char[]) originalArray;
        int len = original.length;
        Character[] converted = new Character[len];
        for (int i = 0; i < converted.length; i++) {
            converted[i] = new Character(original[i]);
        }
        return converted;
    }

    private Object[] convertPrimitiveToObjectArray(Class arrayType, Object originalArray) {
        Object[] convertedArray;
        try {
            Class wrapperClass = (Class) wrapperClasses.get(arrayType);
            Constructor constructor = wrapperClass.getConstructor(new Class[] { String.class });
            int len = Array.getLength(originalArray);
            convertedArray = (Object[]) Array.newInstance(wrapperClass, len);
            for (int i = 0; i < len; i++) {
                convertedArray[i] = constructor.newInstance(new Object[] { Array.get(originalArray, i).toString() });
            }
        } catch (NoSuchMethodException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (InstantiationException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new NakedObjectRuntimeException(e);
        }
        return convertedArray;
    }

    public int[] getParameterMaxLengths() {
        return maxLengths;
    }

    public int[] getParameterTypicalLengths() {
        return typicalLengths;
    }

    public int[] getParameterNoLines() {
        return noLines;
    }

    public boolean[] canParametersWrap() {
        return canWrap;
    }

    public Consent isUsable(final NakedReference target) {
        if (target == null) {
            return Allow.DEFAULT;
        }
        boolean isPersistent = target.getResolveState().isPersistent();
        if (isProtected == When.UNTIL_PERSISTED && !isPersistent) {
            return new Veto("Action disabled until object persisted");
        }
        if (isProtected == When.ONCE_PERSISTED && isPersistent) {
            return new Veto("Action disabled once object persisted");
        }
        return executeConsent(usableMethod, target, false);
    }

    public Consent isUsableDeclaratively() {
        if (isProtected == When.ALWAYS) {
            return new Veto(new DisabledDeclarativelyException(getName(), "Action permanently disabled"));
        }
        return super.isUsableDeclaratively();
    }

    public Consent isParameterSetValidImperatively(final NakedReference object, final Naked[] parameters) {
        return executeConsent(validMethod, object, parameters, false);
    }

    protected Consent executeConsent(final Method method, final NakedReference target, final Naked[] parameters, boolean authorizationCheck) {
        if (method == null) {
            return Allow.DEFAULT;
        }
        Naked[] parameterSet = parameters == null ? new Naked[0] : parameters;
        if (parameterSet.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameterSet.length);
        }
        Object[] longParams;
        if (method.getName().startsWith("default")) {
            longParams = new Object[0];
        } else {
            longParams = new Object[parameterSet.length];
            for (int i = 0; i < longParams.length; i++) {
                longParams[i] = parameterSet[i] == null ? null : parameterSet[i].getObject();
            }
        }
        return executeConsent(method, target, longParams, authorizationCheck);
    }

    public String toString() {
        StringBuffer parameters = new StringBuffer();
        Class[] types = actionMethod.getParameterTypes();
        if (types.length == 0) {
            parameters.append("none");
        }
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                parameters.append("/");
            }
            parameters.append(types[i]);
        }
        return "JavaAction [method=" + actionMethod.getName() + ",type=" + type.getName() + ",parameters=" + parameters + "]";
    }

    public void debugData(final DebugString debug) {
        debug.appendln("Action", actionMethod);
        if (!(namesMethod instanceof NoMemberHelper)) {
            debug.appendln("Labels", namesMethod);
        }
        if (!(optionalParametersMethod instanceof NoMemberHelper)) {
            debug.appendln("Optional", optionalParametersMethod);
        }
        if (!(defaultParametersMethod instanceof NoMemberHelper)) {
            debug.appendln("Defaults", defaultParametersMethod);
        }
        if (!(parameterOptions instanceof NoMemberHelper)) {
            debug.appendln("Choices", parameterOptions);
        }
        if (validMethod != null) {
            debug.appendln("Valid", validMethod);
        }
        super.debugData(debug);
    }
}
