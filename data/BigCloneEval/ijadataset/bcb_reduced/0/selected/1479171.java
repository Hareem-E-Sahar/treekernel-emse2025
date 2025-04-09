package tudresden.ocl20.pivot.modelinstancetype.ecore.internal.modelinstance;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.osgi.util.NLS;
import tudresden.ocl20.pivot.modelinstance.base.AbstractModelInstance;
import tudresden.ocl20.pivot.modelinstancetype.ecore.EcoreModelInstanceTypePlugin;
import tudresden.ocl20.pivot.modelinstancetype.ecore.internal.msg.EcoreModelInstanceTypeMessages;
import tudresden.ocl20.pivot.modelinstancetype.ecore.internal.provider.EcoreModelInstanceProvider;
import tudresden.ocl20.pivot.modelinstancetype.ecore.internal.util.EcoreModelInstanceTypeUtility;
import tudresden.ocl20.pivot.modelinstancetype.exception.AsTypeCastException;
import tudresden.ocl20.pivot.modelinstancetype.exception.CopyForAtPreException;
import tudresden.ocl20.pivot.modelinstancetype.exception.OperationAccessException;
import tudresden.ocl20.pivot.modelinstancetype.exception.OperationNotFoundException;
import tudresden.ocl20.pivot.modelinstancetype.exception.PropertyAccessException;
import tudresden.ocl20.pivot.modelinstancetype.exception.PropertyNotFoundException;
import tudresden.ocl20.pivot.modelinstancetype.exception.TypeNotFoundInModelException;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceBoolean;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceCollection;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceElement;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceEnumerationLiteral;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceInteger;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceObject;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstancePrimitiveType;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceReal;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceString;
import tudresden.ocl20.pivot.modelinstancetype.types.base.AbstractModelInstanceObject;
import tudresden.ocl20.pivot.pivotmodel.Operation;
import tudresden.ocl20.pivot.pivotmodel.Parameter;
import tudresden.ocl20.pivot.pivotmodel.Property;
import tudresden.ocl20.pivot.pivotmodel.Type;

/**
 * <p>
 * Implements the interface {@link IModelInstanceObject} for EMF Ecore
 * {@link EObject}s.
 * </p>
 * 
 * @author Claas Wilke
 */
public class EcoreModelInstanceObject extends AbstractModelInstanceObject implements IModelInstanceObject {

    /** The {@link Logger} for this class. */
    private static final Logger LOGGER = EcoreModelInstanceTypePlugin.getLogger(EcoreModelInstanceProvider.class);

    /** The {@link EObject} adapted by this {@link EcoreModelInstanceObject}. */
    private EObject myEObject;

    /**
	 * The Java {@link Class} this {@link AbstractModelInstanceObject} is casted
	 * to. This is required to access the right {@link Property}s and
	 * {@link Operation}s.
	 */
    private Class<?> myAdaptedType;

    /**
	 * The {@link EcoreModelInstanceFactory} of this
	 * {@link EcoreModelInstanceObject}. Required to adapt results of
	 * {@link Property} and {@link Operation} invocations.
	 */
    private EcoreModelInstanceFactory myFactory;

    /**
	 * <p>
	 * Creates a new {@link EcoreModelInstanceObject} for a given
	 * {@link EObject} and a given {@link Set} of {@link Type}s.
	 * </p>
	 * 
	 * @param object
	 *            The {@link EObject} that shall be adapted by this
	 *            {@link EcoreModelInstanceObject}.
	 * @param type
	 *            The {@link Type} the adapted {@link EObject} implements.
	 * @param originalType
	 *            The original {@link Type} the adapted {@link EObject}
	 *            implements (as after the object has been casted to another
	 *            {@link Type}.)
	 * @param factory
	 *            The {@link EcoreModelInstanceFactory} of this
	 *            {@link EcoreModelInstanceObject}. Required to adapt results of
	 *            {@link Property} and {@link Operation} invocations.
	 */
    protected EcoreModelInstanceObject(EObject eObject, Type type, Type originalType, EcoreModelInstanceFactory factory) {
        super(type, originalType);
        if (LOGGER.isDebugEnabled()) {
            String msg;
            msg = "EcoreModelInstanceObject(";
            msg += "eObject = " + eObject;
            msg += ", type = " + type;
            msg += ", originalType = " + originalType;
            msg += ", factory = " + factory;
            msg += ")";
            LOGGER.debug(msg);
        }
        this.myEObject = eObject;
        if (this.myEObject != null) {
            this.myAdaptedType = eObject.getClass();
        }
        this.myType = type;
        this.myFactory = factory;
        if (LOGGER.isDebugEnabled()) {
            String msg;
            msg = "EcoreModelInstanceObject(EObject, Type, IModelInstanceFactory) - exit";
            LOGGER.debug(msg);
        }
    }

    /**
	 * <p>
	 * Creates a new {@link EcoreModelInstanceObject} for a given
	 * {@link EObject} and a given {@link Set} of {@link Type}s.
	 * </p>
	 * 
	 * @param object
	 *            The {@link EObject} that shall be adapted by this
	 *            {@link EcoreModelInstanceObject}.
	 * @param clazz
	 *            The {@link Class} the adapted {@link EObject} shall be casted
	 *            to.
	 * @param type
	 *            The {@link Type} the adapted {@link EObject} implements.
	 * @param originalType
	 *            The original {@link Type} the adapted {@link EObject}
	 *            implements (as after the object has been casted to another
	 *            {@link Type}.)
	 * @param factory
	 *            The {@link EcoreModelInstanceFactory} of this
	 *            {@link EcoreModelInstanceObject}. Required to adapt results of
	 *            {@link Property} and {@link Operation} invocations.
	 */
    protected EcoreModelInstanceObject(EObject eObject, Class<?> clazz, Type type, Type originalType, EcoreModelInstanceFactory factory) {
        super(type, originalType);
        if (LOGGER.isDebugEnabled()) {
            String msg;
            msg = "EcoreModelInstanceObject(";
            msg += "eObject = " + eObject;
            msg += ", clazz = " + clazz;
            msg += ", type = " + type;
            msg += ", originalType = " + originalType;
            msg += ", factory = " + factory;
            msg += ")";
            LOGGER.debug(msg);
        }
        this.myEObject = eObject;
        this.myAdaptedType = clazz;
        this.myType = type;
        this.myFactory = factory;
        if (LOGGER.isDebugEnabled()) {
            String msg;
            msg = "EcoreModelInstanceObject(EObject, Class, Type, IModelInstanceFactory) - exit";
            LOGGER.debug(msg);
        }
    }

    public IModelInstanceElement asType(Type type) throws AsTypeCastException {
        if (type == null) {
            throw new IllegalArgumentException("Parameter 'type' must not be null.");
        }
        IModelInstanceElement result;
        if (this.getOriginalType().conformsTo(type)) {
            if (this.myEObject == null) {
                result = new EcoreModelInstanceObject(null, type, this.getOriginalType(), this.myFactory);
            } else {
                Class<?> typeClass;
                typeClass = EcoreModelInstanceTypeUtility.findClassOfType(this.myEObject.getClass(), type);
                if (typeClass == null) {
                    String msg;
                    msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_CannotCastTypeClassNotFound;
                    msg = NLS.bind(msg, this.getName(), type);
                    throw new AsTypeCastException(msg);
                }
                result = new EcoreModelInstanceObject(this.myEObject, typeClass, type, this.getOriginalType(), this.myFactory);
            }
        } else {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_CannotCastTypeClassNotFound;
            msg = NLS.bind(msg, this.getName(), type);
            throw new AsTypeCastException(msg);
        }
        return result;
    }

    /**
	 * <p>
	 * Performs a copy of the adapted element of this
	 * {@link IModelInstanceElement} that can be used to store it as a @pre
	 * value of a postcondition's expression. The method should copy the adapted
	 * object and all its references that are expected to change during the
	 * methods execution the postcondition is defined on.
	 * </p>
	 * 
	 * <p>
	 * For {@link EcoreModelInstanceObject}s this method tries to clone the
	 * adapted {@link Object} if the {@link Object} implements {@link Clonable}
	 * . Else the {@link Object} will be copied using an probably existing empty
	 * {@link Constructor} and a flat copy will be created (means all attributes
	 * and associations will lead to the same values and identities. <strong>If
	 * neither the <code>clone()</code> method nor the empty {@link Constructor}
	 * are provided, this operation will fail with an
	 * {@link CopyForAtPreException}.</strong>
	 * </p>
	 * 
	 * @return A copy of the adapted element.
	 * @throws CopyForAtPreException
	 *             Thrown, if an error during the copy process occurs.
	 */
    public IModelInstanceElement copyForAtPre() throws CopyForAtPreException {
        IModelInstanceElement result;
        result = null;
        if (this.myEObject instanceof Cloneable) {
            try {
                result = copyForAtPreWithClone();
            } catch (CopyForAtPreException e) {
            }
        }
        if (result == null) {
            copyForAtPreWithReflections();
        }
        return result;
    }

    public Object getObject() {
        return this.myEObject;
    }

    public IModelInstanceElement getProperty(Property property) throws PropertyAccessException, PropertyNotFoundException {
        if (property == null) {
            throw new IllegalArgumentException("Parameter 'property' must not be null.");
        }
        IModelInstanceElement result;
        if (this.myEObject == null) {
            result = this.myFactory.createModelInstanceElement(null, property.getType());
        } else {
            EStructuralFeature sf = this.myEObject.eClass().getEStructuralFeature(property.getName());
            if (sf == null) {
                String msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_PropertyNotFoundInModelInstanceElement;
                msg = NLS.bind(msg, property, this.myEObject);
                throw new PropertyAccessException(msg);
            }
            Object adapteeResult = this.myEObject.eGet(sf);
            result = AbstractModelInstance.adaptInvocationResult(adapteeResult, property.getType(), this.myFactory);
        }
        return result;
    }

    public IModelInstanceElement invokeOperation(Operation operation, List<IModelInstanceElement> args) throws OperationNotFoundException, OperationAccessException {
        if (operation == null) {
            throw new IllegalArgumentException("Parameter 'operation' must not be null.");
        } else if (args == null) {
            throw new IllegalArgumentException("Parameter 'args' must not be null.");
        }
        IModelInstanceElement result;
        if (this.myEObject == null) {
            result = this.myFactory.createModelInstanceElement(null, operation.getType());
        } else {
            Method operationMethod;
            int argSize;
            Class<?>[] argumentTypes;
            Object[] argumentValues;
            operationMethod = this.findMethodOfAdaptedObject(operation);
            argumentTypes = operationMethod.getParameterTypes();
            argumentValues = new Object[args.size()];
            argSize = Math.min(args.size(), operation.getSignatureParameter().size());
            for (int index = 0; index < argSize; index++) {
                argumentValues[index] = this.createAdaptedElement(args.get(index), argumentTypes[index]);
            }
            try {
                Object adapteeResult;
                operationMethod.setAccessible(true);
                adapteeResult = operationMethod.invoke(this.myEObject, argumentValues);
                if (adapteeResult instanceof EObject) {
                    try {
                        Type adapteeType = this.myFactory.getTypeUtility().findTypeOfEObjectInModel((EObject) adapteeResult);
                        result = AbstractModelInstance.adaptInvocationResult(adapteeResult, adapteeType, this.myFactory);
                    } catch (TypeNotFoundInModelException e) {
                        throw new OperationAccessException("Result of invocation of Operation '" + operation.getName() + "' could not be adapted to any model type.", e);
                    }
                } else {
                    result = AbstractModelInstance.adaptInvocationResult(adapteeResult, operation.getType(), this.myFactory);
                }
            } catch (IllegalArgumentException e) {
                String msg;
                msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_OperationAccessFailed;
                msg = NLS.bind(msg, operation, e.getMessage());
                throw new OperationAccessException(msg, e);
            } catch (IllegalAccessException e) {
                String msg;
                msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_OperationAccessFailed;
                msg = NLS.bind(msg, operation, e.getMessage());
                throw new OperationAccessException(msg, e);
            } catch (InvocationTargetException e) {
                String msg;
                msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_OperationAccessFailed;
                msg = NLS.bind(msg, operation, e.getMessage());
                throw new OperationAccessException(msg, e);
            }
        }
        return result;
    }

    @Override
    public boolean isKindOf(Type type) {
        return this.getOriginalType().conformsTo(type);
    }

    public String toString() {
        StringBuffer result;
        result = new StringBuffer();
        result.append(this.getClass().getSimpleName());
        result.append("[type=");
        result.append(this.getType().getName());
        result.append(",originalType=");
        result.append(this.getOriginalType().getName());
        result.append(",");
        result.append(this.myEObject.toString());
        result.append("]");
        return result.toString();
    }

    /**
	 * <p>
	 * A helper method that tries to copy the adapted object using the method
	 * {@link Object#clone()}.
	 * </p>
	 * 
	 * @return A copy of the adapted {@link EObject} of this
	 *         {@link EcoreModelInstanceObject}.
	 * @throws CopyForAtPreException
	 *             Thrown, if the adapted {@link EObject} cannot be copied via
	 *             clone method.
	 */
    private IModelInstanceElement copyForAtPreWithClone() throws CopyForAtPreException {
        IModelInstanceElement result;
        Method cloneMethod;
        try {
            EObject adaptedResult;
            cloneMethod = this.myEObject.getClass().getMethod("clone");
            cloneMethod.setAccessible(true);
            adaptedResult = (EObject) cloneMethod.invoke(this.myEObject);
            result = new EcoreModelInstanceObject(adaptedResult, this.myAdaptedType, this.myType, this.getOriginalType(), this.myFactory);
        } catch (SecurityException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (NoSuchMethodException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (IllegalArgumentException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (IllegalAccessException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (InvocationTargetException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        }
        return result;
    }

    /**
	 * <p>
	 * A helper method that tries to copy the adapted {@link Object} of this
	 * {@link EcoreModelInstanceObject} with an empty {@link Constructor} based
	 * on reflections. The copied {@link Object} will be a flat copy of this
	 * {@link Object}. Thus, the fields will all have the same value and id.
	 * </p>
	 * 
	 * @return A copy of the adapted {@link Object} of this
	 *         {@link EcoreModelInstanceObject}.
	 * @throws CopyForAtPreException
	 *             Thrown, if the adapted {@link EObject} cannot be copied using
	 *             an empty {@link Constructor}.
	 */
    private IModelInstanceObject copyForAtPreWithReflections() throws CopyForAtPreException {
        IModelInstanceObject result;
        Class<?> adapteeClass;
        adapteeClass = this.myEObject.getClass();
        try {
            EObject copiedAdaptedObject;
            Constructor<?> emptyConstructor;
            emptyConstructor = adapteeClass.getConstructor(new Class[0]);
            copiedAdaptedObject = (EObject) emptyConstructor.newInstance(new Object[0]);
            while (adapteeClass != null) {
                for (Field field : adapteeClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (!(Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))) {
                        field.set(copiedAdaptedObject, field.get(this.myEObject));
                    }
                }
                adapteeClass = adapteeClass.getSuperclass();
            }
            result = new EcoreModelInstanceObject(copiedAdaptedObject, this.myAdaptedType, this.myType, this.getOriginalType(), this.myFactory);
        } catch (SecurityException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (NoSuchMethodException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (IllegalArgumentException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (InstantiationException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (IllegalAccessException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        } catch (InvocationTargetException e) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_CannotCopyForAtPre;
            msg = NLS.bind(msg, this.getName(), e.getMessage());
            throw new CopyForAtPreException(msg, e);
        }
        return result;
    }

    /**
	 * <p>
	 * Returns or creates the element that is adapted by a given
	 * {@link IModelInstanceElement}. E.g., if the given
	 * {@link IModelInstanceElement} is an {@link IModelInstanceObject}, the
	 * adapted {@link Object} is simply returned. For
	 * {@link IModelInstancePrimitiveType}, a newly created primitive is
	 * returned.
	 * </p>
	 * 
	 * @param modelInstanceElement
	 *            The {@link IModelInstanceElement} those adapted {@link Object}
	 *            shall be returned or created.
	 * @param typeClass
	 *            The {@link Class} the recreated element should be an instance
	 *            of. This could be required for
	 *            {@link IModelInstancePrimitiveType}s or for
	 *            {@link IModelInstanceCollection}s.
	 * @return The created or adapted value ({@link Object}).
	 */
    @SuppressWarnings("unchecked")
    private Object createAdaptedElement(IModelInstanceElement modelInstanceElement, Class<?> typeClass) {
        Object result;
        if (modelInstanceElement == null) {
            result = null;
        } else if (modelInstanceElement instanceof IModelInstancePrimitiveType) {
            if (modelInstanceElement instanceof IModelInstanceBoolean) {
                result = ((IModelInstanceBoolean) modelInstanceElement).getBoolean();
            } else if (modelInstanceElement instanceof IModelInstanceInteger) {
                result = createAdaptedIntegerValue((IModelInstanceInteger) modelInstanceElement, typeClass);
            } else if (modelInstanceElement instanceof IModelInstanceReal) {
                result = createAdaptedRealValue((IModelInstanceReal) modelInstanceElement, typeClass);
            } else if (modelInstanceElement instanceof IModelInstanceString) {
                result = createAdaptedStringValue((IModelInstanceString) modelInstanceElement, typeClass);
            } else {
                result = null;
            }
        } else if (modelInstanceElement instanceof IModelInstanceEnumerationLiteral) {
            result = createAdaptedEnumerationLiteral((IModelInstanceEnumerationLiteral) modelInstanceElement, typeClass);
        } else if (modelInstanceElement instanceof IModelInstanceCollection) {
            if (typeClass.isArray()) {
                result = createAdaptedArray(modelInstanceElement, typeClass);
            } else if (Collection.class.isAssignableFrom(typeClass)) {
                result = createAdaptedCollection((IModelInstanceCollection<IModelInstanceElement>) modelInstanceElement, typeClass);
            } else {
                String msg;
                msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_CannotRecreateCollection;
                throw new IllegalArgumentException(msg);
            }
        } else if (modelInstanceElement instanceof IModelInstanceObject) {
            result = ((IModelInstanceObject) modelInstanceElement).getObject();
        } else {
            result = null;
        }
        return result;
    }

    /**
	 * <p>
	 * A helper method the converts a given {@link IModelInstanceElement} into
	 * an Array value of a given {@link Class}.
	 * </p>
	 * 
	 * @param modelInstanceElement
	 *            The {@link IModelInstanceElement} that shall be converted.
	 * @param type
	 *            The {@link Class} to that the given
	 *            {@link IModelInstanceElement} shall be converted.
	 * @return The converted {@link Object}.
	 */
    @SuppressWarnings("unchecked")
    private Object createAdaptedArray(IModelInstanceElement modelInstanceElement, Class<?> type) {
        Object result;
        if (modelInstanceElement instanceof IModelInstanceCollection) {
            IModelInstanceCollection<IModelInstanceElement> modelInstanceCollection;
            Collection<IModelInstanceElement> adaptedCollection;
            Class<?> componentType;
            int index;
            componentType = type.getComponentType();
            modelInstanceCollection = (IModelInstanceCollection<IModelInstanceElement>) modelInstanceElement;
            adaptedCollection = modelInstanceCollection.getCollection();
            if (componentType.isPrimitive()) {
                if (boolean.class.isAssignableFrom(componentType)) {
                    boolean[] array;
                    array = new boolean[adaptedCollection.size()];
                    index = 0;
                    for (IModelInstanceElement anElement : adaptedCollection) {
                        array[index] = ((IModelInstanceBoolean) anElement).getBoolean().booleanValue();
                    }
                    result = array;
                } else if (byte.class.isAssignableFrom(componentType)) {
                    byte[] array;
                    array = new byte[adaptedCollection.size()];
                    index = 0;
                    for (IModelInstanceElement anElement : adaptedCollection) {
                        array[index] = ((IModelInstanceInteger) anElement).getLong().byteValue();
                    }
                    result = array;
                } else if (char.class.isAssignableFrom(componentType)) {
                    char[] array;
                    array = new char[adaptedCollection.size()];
                    index = 0;
                    for (IModelInstanceElement anElement : adaptedCollection) {
                        array[index] = ((IModelInstanceString) anElement).getString().charAt(0);
                    }
                    result = array;
                } else if (double.class.isAssignableFrom(componentType)) {
                    double[] array;
                    array = new double[adaptedCollection.size()];
                    index = 0;
                    for (IModelInstanceElement anElement : adaptedCollection) {
                        array[index] = ((IModelInstanceReal) anElement).getDouble().doubleValue();
                    }
                    result = array;
                } else if (float.class.isAssignableFrom(componentType)) {
                    float[] array;
                    array = new float[adaptedCollection.size()];
                    index = 0;
                    for (IModelInstanceElement anElement : adaptedCollection) {
                        array[index] = ((IModelInstanceReal) anElement).getDouble().floatValue();
                    }
                    result = array;
                } else if (int.class.isAssignableFrom(componentType)) {
                    int[] array;
                    array = new int[adaptedCollection.size()];
                    index = 0;
                    for (IModelInstanceElement anElement : adaptedCollection) {
                        array[index] = ((IModelInstanceInteger) anElement).getLong().intValue();
                    }
                    result = array;
                } else if (long.class.isAssignableFrom(componentType)) {
                    long[] array;
                    array = new long[adaptedCollection.size()];
                    index = 0;
                    for (IModelInstanceElement anElement : adaptedCollection) {
                        array[index] = ((IModelInstanceInteger) anElement).getLong().longValue();
                    }
                    result = array;
                } else if (short.class.isAssignableFrom(componentType)) {
                    short[] array;
                    array = new short[adaptedCollection.size()];
                    index = 0;
                    for (IModelInstanceElement anElement : adaptedCollection) {
                        array[index] = ((IModelInstanceInteger) anElement).getLong().shortValue();
                    }
                    result = array;
                } else {
                    throw new IllegalArgumentException(EcoreModelInstanceTypeMessages.EcoreModelInstance_CannotRecreateArray);
                }
            } else {
                Object[] array;
                array = (Object[]) Array.newInstance(componentType, adaptedCollection.size());
                index = 0;
                for (IModelInstanceElement anElement : adaptedCollection) {
                    array[index] = createAdaptedElement(anElement, componentType);
                }
                result = array;
            }
        } else {
            throw new IllegalArgumentException(EcoreModelInstanceTypeMessages.EcoreModelInstance_CannotRecreateArray);
        }
        return result;
    }

    /**
	 * <p>
	 * A helper method that converts a given {@link IModelInstanceElement} into
	 * an {@link Collection} of a given {@link Class} type.
	 * </p>
	 * 
	 * @param modelInstanceCollection
	 *            The {@link IModelInstanceCollection} that shall be converted.
	 * @param type
	 *            The {@link Collection} {@link Class} to that the given
	 *            {@link IModelInstanceElement} shall be converted.
	 * @return The converted {@link Collection}.
	 */
    @SuppressWarnings("unchecked")
    private Collection<?> createAdaptedCollection(IModelInstanceCollection<IModelInstanceElement> modelInstanceCollection, Class<?> clazzType) {
        Collection<Object> result;
        if (Collection.class.isAssignableFrom(clazzType)) {
            try {
                Constructor<?> collectionConstructor;
                collectionConstructor = clazzType.getConstructor(new Class[0]);
                result = (Collection<Object>) collectionConstructor.newInstance(new Object[0]);
            } catch (SecurityException e) {
                result = null;
            } catch (NoSuchMethodException e) {
                result = null;
            } catch (IllegalArgumentException e) {
                result = null;
            } catch (InstantiationException e) {
                result = null;
            } catch (IllegalAccessException e) {
                result = null;
            } catch (InvocationTargetException e) {
                result = null;
            }
            if (result == null) {
                if (UniqueEList.class.isAssignableFrom(clazzType)) {
                    result = new UniqueEList<Object>();
                } else if (List.class.isAssignableFrom(clazzType)) {
                    result = new BasicEList<Object>();
                } else if (Set.class.isAssignableFrom(clazzType)) {
                    result = new HashSet<Object>();
                }
            }
            if (result != null) {
                Class<?> elementClassType;
                if (clazzType.getTypeParameters().length == 1 && clazzType.getTypeParameters()[0].getBounds().length == 1 && clazzType.getTypeParameters()[0].getBounds()[0] instanceof Class) {
                    elementClassType = (Class<?>) clazzType.getTypeParameters()[0].getBounds()[0];
                } else {
                    elementClassType = Object.class;
                }
                for (IModelInstanceElement anElement : modelInstanceCollection.getCollection()) {
                    result.add(createAdaptedElement(anElement, elementClassType));
                }
            } else {
                String msg;
                msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_CannotRecreateCollection;
                throw new IllegalArgumentException(msg);
            }
        } else {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_CannotRecreateCollection;
            throw new IllegalArgumentException(msg);
        }
        return result;
    }

    /**
	 * <p>
	 * A helper method the converts a given
	 * {@link IModelInstanceEnumerationLiteral} into an {@link Object} value of
	 * a given {@link Class}. If the given {@link Class} does not represents an
	 * {@link Enum}, a {@link IllegalArgumentException} is thrown.
	 * </p>
	 * 
	 * @param modelInstanceEnumerationLiteral
	 *            The {@link IModelInstanceEnumerationLiteral} that shall be
	 *            converted.
	 * @param type
	 *            The {@link Class} to that the given
	 *            {@link IModelInstanceEnumerationLiteral} shall be converted.
	 * @return The converted {@link Object}.
	 */
    private Object createAdaptedEnumerationLiteral(IModelInstanceEnumerationLiteral modelInstanceEnumerationLiteral, Class<?> typeClass) {
        Object result;
        if (typeClass.isEnum()) {
            result = null;
            for (Object anEnumConstant : typeClass.getEnumConstants()) {
                if (anEnumConstant.toString().equals(modelInstanceEnumerationLiteral.getLiteral().getName())) {
                    result = anEnumConstant;
                    break;
                }
            }
            if (result == null) {
                String msg;
                msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_EnumerationLiteralNotFound;
                msg = NLS.bind(modelInstanceEnumerationLiteral.getLiteral().getQualifiedName(), "The enumeration literal could not be adapted to any constant of the given Enum class.");
                throw new IllegalArgumentException(msg);
            }
        } else {
            List<String> enumerationQualifiedName;
            String enumClassName;
            enumerationQualifiedName = modelInstanceEnumerationLiteral.getLiteral().getQualifiedNameList();
            enumerationQualifiedName.remove(enumerationQualifiedName.size() - 1);
            enumClassName = EcoreModelInstanceTypeUtility.toCanonicalName(enumerationQualifiedName);
            try {
                Class<?> enumClass;
                enumClass = this.loadJavaClass(enumClassName);
                if (enumClass.isEnum()) {
                    result = null;
                    for (Object anEnumConstant : enumClass.getEnumConstants()) {
                        if (anEnumConstant.toString().equals(modelInstanceEnumerationLiteral.getLiteral().getName())) {
                            result = anEnumConstant;
                            break;
                        }
                    }
                    if (result == null) {
                        String msg;
                        msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_EnumerationLiteralNotFound;
                        msg = NLS.bind(modelInstanceEnumerationLiteral.getLiteral().getQualifiedName(), "The enumeration literal could not be adapted to any constant of the given Enum class.");
                        throw new IllegalArgumentException(msg);
                    }
                } else {
                    String msg;
                    msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_EnumerationLiteralNotFound;
                    msg = NLS.bind(modelInstanceEnumerationLiteral.getLiteral().getQualifiedName(), "The found class " + enumClass + " is not an Enum.");
                    throw new IllegalArgumentException(msg);
                }
            } catch (ClassNotFoundException e) {
                String msg;
                msg = EcoreModelInstanceTypeMessages.EcoreModelInstance_EnumerationLiteralNotFound;
                msg = NLS.bind(modelInstanceEnumerationLiteral.getLiteral().getQualifiedName(), e.getMessage());
                throw new IllegalArgumentException(msg, e);
            }
        }
        return result;
    }

    /**
	 * <p>
	 * A helper method the converts a given {@link IModelInstanceInteger} into
	 * an Integer value of a given {@link Class}. If the given {@link Class}
	 * represents an unknown integer {@link Class}, a {@link Long} is returned.
	 * </p>
	 * 
	 * @param modelInstanceInteger
	 *            The {@link IModelInstanceElement} that shall be converted.
	 * @param type
	 *            The {@link Class} to that the given
	 *            {@link IModelInstanceElement} shall be converted.
	 * @return The converted {@link Object}.
	 */
    private Object createAdaptedIntegerValue(IModelInstanceInteger modelInstanceInteger, Class<?> type) {
        Object result;
        if (type.equals(BigDecimal.class)) {
            result = new BigDecimal(modelInstanceInteger.getLong());
        } else if (type.equals(BigInteger.class)) {
            result = BigInteger.valueOf(modelInstanceInteger.getLong());
        } else if (type.equals(byte.class) || type.equals(Byte.class)) {
            result = modelInstanceInteger.getLong().byteValue();
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            result = modelInstanceInteger.getLong().intValue();
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            result = modelInstanceInteger.getLong();
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            result = modelInstanceInteger.getLong().shortValue();
        } else {
            result = modelInstanceInteger.getLong();
        }
        return result;
    }

    /**
	 * <p>
	 * A helper method the converts a given {@link IModelInstanceReal} into a
	 * Real value of a given {@link Class}. If the given {@link Class}
	 * represents an unknown real {@link Class}, a {@link Number} is returned.
	 * </p>
	 * 
	 * @param modelInstanceReal
	 *            The {@link IModelInstanceReal} that shall be converted.
	 * @param type
	 *            The {@link Class} to that the given {@link IModelInstanceReal}
	 *            shall be converted.
	 * @return The converted {@link Object}.
	 */
    private Object createAdaptedRealValue(IModelInstanceReal modelInstanceReal, Class<?> type) {
        Object result;
        if (type.equals(double.class) || type.equals(BigInteger.class)) {
            result = modelInstanceReal.getDouble();
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            result = modelInstanceReal.getDouble().floatValue();
        } else {
            result = modelInstanceReal.getDouble();
        }
        return result;
    }

    /**
	 * <p>
	 * A helper method the converts a given {@link IModelInstanceString} into a
	 * String value of a given {@link Class}. If the given {@link Class}
	 * represents an unknown String {@link Class}, a {@link String} is returned.
	 * </p>
	 * 
	 * @param modelInstanceString
	 *            The {@link IModelInstanceString} that shall be converted.
	 * @param type
	 *            The {@link Class} to that the given
	 *            {@link IModelInstanceString} shall be converted.
	 * @return The converted {@link Object}.
	 */
    private Object createAdaptedStringValue(IModelInstanceString modelInstanceString, Class<?> type) {
        Object result;
        String stringValue;
        stringValue = modelInstanceString.getString();
        if (type.equals(char.class) || type.equals(BigInteger.class)) {
            if (stringValue.length() > 0) {
                result = stringValue.toCharArray()[0];
            } else {
                result = null;
            }
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            if (stringValue.length() > 0) {
                result = new Character(stringValue.toCharArray()[0]);
            } else {
                result = null;
            }
        } else {
            result = stringValue;
        }
        return result;
    }

    /**
	 * <p>
	 * A helper {@link Method} used to find a {@link Method} of the adapted
	 * {@link Object} of this {@link EcoreModelInstanceObject} that conforms to
	 * a given {@link Operation}.
	 * </p>
	 * 
	 * @param operation
	 *            The {@link Operation} for that a {@link Method} shall be
	 *            found.
	 * @return The found {@link Method}.
	 * @throws OperationNotFoundException
	 *             If no matching {@link Method} for the given {@link Operation}
	 *             can be found.
	 */
    private Method findMethodOfAdaptedObject(Operation operation) throws OperationNotFoundException {
        Method result;
        Class<?> methodSourceClass;
        result = null;
        methodSourceClass = this.myAdaptedType;
        while (methodSourceClass != null && result == null) {
            for (Method aMethod : methodSourceClass.getDeclaredMethods()) {
                boolean nameIsEqual;
                boolean resultTypeIsConform;
                boolean argumentSizeIsEqual;
                nameIsEqual = aMethod.getName().equals(operation.getName());
                resultTypeIsConform = EcoreModelInstanceTypeUtility.conformsTypeToType(aMethod.getGenericReturnType(), operation.getType());
                argumentSizeIsEqual = aMethod.getParameterTypes().length == operation.getSignatureParameter().size();
                if (nameIsEqual && resultTypeIsConform && argumentSizeIsEqual) {
                    java.lang.reflect.Type[] javaTypes;
                    List<Parameter> pivotModelParamters;
                    boolean matches;
                    javaTypes = aMethod.getGenericParameterTypes();
                    pivotModelParamters = operation.getSignatureParameter();
                    matches = true;
                    for (int index = 0; index < operation.getSignatureParameter().size(); index++) {
                        if (!EcoreModelInstanceTypeUtility.conformsTypeToType(javaTypes[index], pivotModelParamters.get(index).getType())) {
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        result = aMethod;
                        break;
                    }
                }
            }
            methodSourceClass = methodSourceClass.getSuperclass();
        }
        if (result == null) {
            String msg;
            msg = EcoreModelInstanceTypeMessages.EcoreModelInstanceObject_OperationNotFound;
            msg = NLS.bind(msg, operation, this.myEObject.getClass());
            throw new OperationNotFoundException(msg);
        }
        return result;
    }

    /**
	 * <p>
	 * A helper method that tries to load a {@link Class} for a given canonical
	 * name using all {@link ClassLoader}s of this {@link JavaModelInstance}.
	 * </p>
	 * 
	 * @param canonicalName
	 *            The canonical name of the {@link Class} that shall be loaded.
	 * @return
	 * @throws ClassNotFoundException
	 *             Thrown, if the {@link Class} cannot be found by any
	 *             {@link ClassLoader} of this {@link JavaModelInstance}.
	 */
    private Class<?> loadJavaClass(String canonicalName) throws ClassNotFoundException {
        Class<?> result;
        result = null;
        result = this.myAdaptedType.getClassLoader().loadClass(canonicalName);
        return result;
    }
}
