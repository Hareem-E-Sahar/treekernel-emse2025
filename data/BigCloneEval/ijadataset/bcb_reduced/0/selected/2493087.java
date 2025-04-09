package tudresden.ocl20.pivot.modelinstancetype.java.internal.modelinstance;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;
import tudresden.ocl20.pivot.model.IModel;
import tudresden.ocl20.pivot.model.ModelAccessException;
import tudresden.ocl20.pivot.modelinstance.IModelInstance;
import tudresden.ocl20.pivot.modelinstance.base.AbstractModelInstance;
import tudresden.ocl20.pivot.modelinstancetype.exception.OperationAccessException;
import tudresden.ocl20.pivot.modelinstancetype.exception.OperationNotFoundException;
import tudresden.ocl20.pivot.modelinstancetype.exception.PropertyAccessException;
import tudresden.ocl20.pivot.modelinstancetype.exception.PropertyNotFoundException;
import tudresden.ocl20.pivot.modelinstancetype.exception.TypeNotFoundInModelException;
import tudresden.ocl20.pivot.modelinstancetype.java.JavaModelInstanceTypePlugin;
import tudresden.ocl20.pivot.modelinstancetype.java.internal.msg.JavaModelInstanceTypeMessages;
import tudresden.ocl20.pivot.modelinstancetype.java.internal.util.JavaModelInstanceTypeUtility;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceBoolean;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceCollection;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceElement;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceEnumerationLiteral;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceInteger;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceObject;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstancePrimitiveType;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceReal;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceString;
import tudresden.ocl20.pivot.pivotmodel.EnumerationLiteral;
import tudresden.ocl20.pivot.pivotmodel.Operation;
import tudresden.ocl20.pivot.pivotmodel.Parameter;
import tudresden.ocl20.pivot.pivotmodel.Property;
import tudresden.ocl20.pivot.pivotmodel.Type;

/**
 * <p>
 * Represents instances of {@link IModel}s represented by Java classes.
 * </p>
 * 
 * @author Claas Wilke
 */
public class JavaModelInstance extends AbstractModelInstance {

    /** The {@link Logger} for this class. */
    private static final Logger LOGGER = JavaModelInstanceTypePlugin.getLogger(JavaModelInstance.class);

    /**
	 * A counter used to generated default names for empty
	 * {@link JavaModelInstance}s.
	 */
    private static int nameCounter = 0;

    /**
	 * The {@link ClassLoader}s of this {@link JavaModelInstance}. Required to
	 * find {@link EnumerationLiteral}s, to get static {@link Property}s and to
	 * invoke static {@link Operation}s.
	 */
    private Set<ClassLoader> myClassLoaders = new HashSet<ClassLoader>();

    /**
	 * <p>
	 * Creates and initializes a new {@link JavaModelInstance}.
	 * </p>
	 * 
	 * @param providerClass
	 *            The provider class used to get all type instances of this
	 *            model instance.
	 * @param model
	 *            The {@link IModel} of this {@link IModelInstance}.s
	 * @throws TypeNotFoundInModelException
	 *             Thrown if a model instance Object, cannot be adapted to a
	 *             {@link Type} in the {@link IModel}.
	 */
    @SuppressWarnings("unchecked")
    public JavaModelInstance(Class<?> providerClass, IModel model) throws ModelAccessException, TypeNotFoundInModelException {
        if (LOGGER.isDebugEnabled()) {
            String msg;
            msg = "JavaModelInstance(";
            msg += "providerClass = " + providerClass;
            msg += ", model = " + model;
            msg += ")";
            LOGGER.debug(msg);
        }
        this.myModel = model;
        this.myName = providerClass.getCanonicalName();
        this.myClassLoaders.add(providerClass.getClassLoader());
        this.myModelInstanceFactory = new JavaModelInstanceFactory(this.myModel);
        try {
            Method providerMethod;
            List<Object> providedObjects;
            providerMethod = providerClass.getDeclaredMethod("getModelObjects", new Class[0]);
            providedObjects = (List<Object>) providerMethod.invoke(null, new Object[0]);
            this.addObjects(providedObjects);
            this.initializeTypeMapping();
        } catch (NoSuchMethodException e) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_ProviderMethodNotFound;
            msg = NLS.bind(msg, providerClass);
            LOGGER.error(msg);
            throw new ModelAccessException(msg, e);
        } catch (IllegalArgumentException e) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_ProviderMethodInvocationError;
            msg = NLS.bind(msg, providerClass);
            LOGGER.error(msg);
            throw new ModelAccessException(msg, e);
        } catch (IllegalAccessException e) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_ProviderMethodInvocationError;
            msg = NLS.bind(msg, providerClass);
            LOGGER.error(msg);
            throw new ModelAccessException(msg, e);
        } catch (InvocationTargetException e) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_ProviderMethodInvocationError;
            msg = NLS.bind(msg, providerClass);
            LOGGER.error(msg);
            throw new ModelAccessException(msg, e);
        } catch (NoClassDefFoundError e) {
            String msg;
            msg = "Your model instance class " + providerClass + " could not be opened. Maybe it contains some unavailable imports or compile errors.";
            LOGGER.error(msg);
            throw new ModelAccessException(msg, e);
        }
        if (LOGGER.isDebugEnabled()) {
            String msg;
            msg = "JavaModelInstance(Class<?>, IModel) - exit";
            LOGGER.debug(msg);
        }
    }

    /**
	 * <p>
	 * Creates and initializes a new, empty {@link JavaModelInstance}.
	 * </p>
	 * 
	 * <p>
	 * <strong>Please note that an empty {@link JavaModelInstance} cannot be
	 * used to find static {@link Property}s and static {@link Operation}s until
	 * at least one {@link Object} has been added whose {@link ClassLoader}
	 * knows the {@link Class} of the static {@link Property} or
	 * {@link Operation}!</strong>
	 * </p>
	 * 
	 * @param model
	 *            The {@link IModel} of this {@link IModelInstance}.s
	 */
    public JavaModelInstance(IModel model) {
        if (LOGGER.isDebugEnabled()) {
            String msg;
            msg = "JavaModelInstance(";
            msg += "model = " + model;
            msg += ")";
            LOGGER.debug(msg);
        }
        this.myModel = model;
        this.myName = this.getClass().getSimpleName() + nameCounter;
        nameCounter++;
        this.myModelInstanceFactory = new JavaModelInstanceFactory(this.myModel);
        if (LOGGER.isDebugEnabled()) {
            String msg;
            msg = "JavaModelInstance(IModel) - exit";
            LOGGER.debug(msg);
        }
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
	 * @param classLoaders
	 *            A {@link Set} of {@link ClassLoader}s that can probably be
	 *            used to load required {@link Class}es for the adaptation.
	 * @return The created or adapted value ({@link Object}).
	 */
    @SuppressWarnings("unchecked")
    protected static Object createAdaptedElement(IModelInstanceElement modelInstanceElement, Class<?> typeClass, Set<ClassLoader> classLoaders) {
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
            result = createAdaptedEnumerationLiteral((IModelInstanceEnumerationLiteral) modelInstanceElement, typeClass, classLoaders);
        } else if (modelInstanceElement instanceof IModelInstanceCollection) {
            if (typeClass.isArray()) {
                result = createAdaptedArray(modelInstanceElement, typeClass, classLoaders);
            } else if (Collection.class.isAssignableFrom(typeClass)) {
                result = createAdaptedCollection((IModelInstanceCollection<IModelInstanceElement>) modelInstanceElement, typeClass, classLoaders);
            } else {
                String msg;
                msg = JavaModelInstanceTypeMessages.JavaModelInstance_CannotRecreateCollection;
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
	 * @param classLoaders
	 *            A {@link Set} of {@link ClassLoader}s that can probably be
	 *            used to load required {@link Class}es for the adaptation.
	 * @return The converted {@link Object}.
	 */
    @SuppressWarnings("unchecked")
    private static Object createAdaptedArray(IModelInstanceElement modelInstanceElement, Class<?> type, Set<ClassLoader> classLoaders) {
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
                    throw new IllegalArgumentException(JavaModelInstanceTypeMessages.JavaModelInstance_CannotRecreateArray);
                }
            } else {
                Object[] array;
                array = (Object[]) Array.newInstance(componentType, adaptedCollection.size());
                index = 0;
                for (IModelInstanceElement anElement : adaptedCollection) {
                    array[index] = createAdaptedElement(anElement, componentType, classLoaders);
                }
                result = array;
            }
        } else {
            throw new IllegalArgumentException(JavaModelInstanceTypeMessages.JavaModelInstance_CannotRecreateArray);
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
	 * @param classLoaders
	 *            A {@link Set} of {@link ClassLoader}s that can probably be
	 *            used to load required {@link Class}es for the adaptation.
	 * @return The converted {@link Collection}.
	 */
    @SuppressWarnings("unchecked")
    private static Collection<?> createAdaptedCollection(IModelInstanceCollection<IModelInstanceElement> modelInstanceCollection, Class<?> clazzType, Set<ClassLoader> classLoaders) {
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
                if (TreeSet.class.isAssignableFrom(clazzType)) {
                    result = new TreeSet<Object>();
                } else if (LinkedHashSet.class.isAssignableFrom(clazzType)) {
                    result = new LinkedHashSet<Object>();
                } else if (HashSet.class.isAssignableFrom(clazzType)) {
                    result = new HashSet<Object>();
                } else if (Stack.class.isAssignableFrom(clazzType)) {
                    result = new Stack<Object>();
                } else if (Vector.class.isAssignableFrom(clazzType)) {
                    result = new Vector<Object>();
                } else if (LinkedList.class.isAssignableFrom(clazzType)) {
                    result = new LinkedList<Object>();
                } else {
                    result = new ArrayList<Object>();
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
                    result.add(createAdaptedElement(anElement, elementClassType, classLoaders));
                }
            } else {
                String msg;
                msg = JavaModelInstanceTypeMessages.JavaModelInstance_CannotRecreateCollection;
                throw new IllegalArgumentException(msg);
            }
        } else {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_CannotRecreateCollection;
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
	 * @param classLoaders
	 *            A {@link Set} of {@link ClassLoader}s that can probably be
	 *            used to load required {@link Class}es for the adaptation.
	 * @return The converted {@link Object}.
	 */
    private static Object createAdaptedEnumerationLiteral(IModelInstanceEnumerationLiteral modelInstanceEnumerationLiteral, Class<?> typeClass, Set<ClassLoader> classLoaders) {
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
                msg = JavaModelInstanceTypeMessages.JavaModelInstance_EnumerationLiteralNotFound;
                msg = NLS.bind(modelInstanceEnumerationLiteral.getLiteral().getQualifiedName(), "The enumeration literal could not be adapted to any constant of the given Enum class.");
                throw new IllegalArgumentException(msg);
            }
        } else {
            List<String> enumerationQualifiedName;
            String enumClassName;
            enumerationQualifiedName = modelInstanceEnumerationLiteral.getLiteral().getQualifiedNameList();
            enumerationQualifiedName.remove(enumerationQualifiedName.size() - 1);
            enumClassName = JavaModelInstanceTypeUtility.toCanonicalName(enumerationQualifiedName);
            try {
                Class<?> enumClass;
                enumClass = loadJavaClass(enumClassName, classLoaders);
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
                        msg = JavaModelInstanceTypeMessages.JavaModelInstance_EnumerationLiteralNotFound;
                        msg = NLS.bind(modelInstanceEnumerationLiteral.getLiteral().getQualifiedName(), "The enumeration literal could not be adapted to any constant of the given Enum class.");
                        throw new IllegalArgumentException(msg);
                    }
                } else {
                    String msg;
                    msg = JavaModelInstanceTypeMessages.JavaModelInstance_EnumerationLiteralNotFound;
                    msg = NLS.bind(modelInstanceEnumerationLiteral.getLiteral().getQualifiedName(), "The found class " + enumClass + " is not an Enum.");
                    throw new IllegalArgumentException(msg);
                }
            } catch (ClassNotFoundException e) {
                String msg;
                msg = JavaModelInstanceTypeMessages.JavaModelInstance_EnumerationLiteralNotFound;
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
    private static Object createAdaptedIntegerValue(IModelInstanceInteger modelInstanceInteger, Class<?> type) {
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
    private static Object createAdaptedRealValue(IModelInstanceReal modelInstanceReal, Class<?> type) {
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
    private static Object createAdaptedStringValue(IModelInstanceString modelInstanceString, Class<?> type) {
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
	 * A helper method that tries to load a {@link Class} for a given canonical
	 * name using all {@link ClassLoader}s of this {@link JavaModelInstance}.
	 * </p>
	 * 
	 * @param canonicalName
	 *            The canonical name of the {@link Class} that shall be loaded.
	 * @param classLoaders
	 *            A {@link Set} of {@link ClassLoader}s that can probably be
	 *            used to load required {@link Class}es for the adaptation.
	 * @return
	 * @throws ClassNotFoundException
	 *             Thrown, if the {@link Class} cannot be found by any
	 *             {@link ClassLoader} of this {@link JavaModelInstance}.
	 */
    private static Class<?> loadJavaClass(String canonicalName, Set<ClassLoader> classLoaders) throws ClassNotFoundException {
        Class<?> result;
        result = null;
        for (ClassLoader aClassLoader : classLoaders) {
            try {
                if (aClassLoader != null) {
                    result = aClassLoader.loadClass(canonicalName);
                    break;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        if (result == null) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_ClassNotFound;
            msg = NLS.bind(msg, canonicalName);
            throw new ClassNotFoundException(msg);
        }
        return result;
    }

    public IModelInstanceElement addModelInstanceElement(Object object) throws TypeNotFoundInModelException {
        if (object == null) {
            throw new IllegalArgumentException("Parameter 'object' must not be null.");
        }
        IModelInstanceElement result;
        result = this.addObject(object);
        if (result instanceof IModelInstanceObject) {
            this.addModelInstanceObjectToCache((IModelInstanceObject) result);
        }
        this.myClassLoaders.add(object.getClass().getClassLoader());
        return result;
    }

    public IModelInstanceElement getStaticProperty(Property property) throws PropertyAccessException, PropertyNotFoundException {
        if (property == null) {
            throw new IllegalArgumentException("Parameter 'property' must not be null.");
        }
        IModelInstanceElement result;
        if (this.myClassLoaders.size() > 0) {
            Class<?> propertyClass;
            String propertyClassCanonicalName;
            try {
                propertyClassCanonicalName = JavaModelInstanceTypeUtility.toCanonicalName(property.getOwner().getQualifiedNameList());
                propertyClass = loadJavaClass(propertyClassCanonicalName, this.myClassLoaders);
                Field propertyField;
                try {
                    propertyField = propertyClass.getDeclaredField(property.getName());
                    Object propertyValue;
                    propertyValue = null;
                    propertyField.setAccessible(true);
                    try {
                        propertyValue = propertyField.get(null);
                        result = adaptInvocationResult(propertyValue, property.getType(), (JavaModelInstanceFactory) this.myModelInstanceFactory);
                    } catch (IllegalArgumentException e) {
                        String msg;
                        msg = JavaModelInstanceTypeMessages.JavaModelInstance_PropertyAccessFailed;
                        msg = NLS.bind(msg, property, e.getMessage());
                        throw new PropertyAccessException(msg, e);
                    } catch (IllegalAccessException e) {
                        String msg;
                        msg = JavaModelInstanceTypeMessages.JavaModelInstance_PropertyAccessFailed;
                        msg = NLS.bind(msg, property, e.getMessage());
                        throw new PropertyAccessException(msg, e);
                    }
                } catch (NoSuchFieldException e) {
                    String msg;
                    msg = JavaModelInstanceTypeMessages.JavaModelInstance_StaticPropertyNotFound;
                    msg = NLS.bind(msg, property, "The field was not found in the adapted class.");
                    throw new PropertyNotFoundException(msg);
                }
            } catch (ClassNotFoundException e2) {
                String msg;
                msg = JavaModelInstanceTypeMessages.JavaModelInstance_StaticPropertyNotFound;
                msg = NLS.bind(msg, property, e2.getMessage());
                throw new PropertyNotFoundException(msg, e2);
            }
        } else {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_StaticPropertyNotFound;
            msg = NLS.bind(msg, property, "The ClassLoader of the JavaModelInstance was null.");
            throw new PropertyNotFoundException(msg);
        }
        return result;
    }

    public IModelInstanceElement invokeStaticOperation(Operation operation, List<IModelInstanceElement> args) throws OperationNotFoundException, OperationAccessException {
        if (operation == null) {
            throw new IllegalArgumentException("Parameter 'operation' must not be null.");
        } else if (args == null) {
            throw new IllegalArgumentException("Parameter 'args' must not be null.");
        }
        IModelInstanceElement result;
        Method operationMethod;
        int argSize;
        Class<?>[] argumentTypes;
        Object[] argumentValues;
        operationMethod = this.findStaticMethod(operation);
        argumentTypes = operationMethod.getParameterTypes();
        argumentValues = new Object[args.size()];
        argSize = Math.min(args.size(), operation.getSignatureParameter().size());
        for (int index = 0; index < argSize; index++) {
            argumentValues[index] = createAdaptedElement(args.get(index), argumentTypes[index], this.myClassLoaders);
            index++;
        }
        try {
            Object adapteeResult;
            operationMethod.setAccessible(true);
            adapteeResult = operationMethod.invoke(null, argumentValues);
            result = JavaModelInstance.adaptInvocationResult(adapteeResult, operation.getType(), (JavaModelInstanceFactory) this.myModelInstanceFactory);
        } catch (IllegalArgumentException e) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_OperationAccessFailed;
            msg = NLS.bind(msg, operation, e.getMessage());
            throw new OperationAccessException(msg, e);
        } catch (IllegalAccessException e) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_OperationAccessFailed;
            msg = NLS.bind(msg, operation, e.getMessage());
            throw new OperationAccessException(msg, e);
        } catch (InvocationTargetException e) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_OperationAccessFailed;
            msg = NLS.bind(msg, operation, e.getMessage());
            throw new OperationAccessException(msg, e);
        }
        return result;
    }

    /**
	 * <p>
	 * Adds a {@link List} of given {@link Object}s to the objects {@link List}.
	 * </p>
	 * 
	 * @param objects
	 *            A {@link List} of {@link Object}s which shall be added to the
	 *            objects {@link List}.
	 * @throws TypeNotFoundInModelException
	 *             Thrown if a given Object, cannot be adapted to a {@link Type}
	 *             in the {@link IModel}.
	 */
    private void addObjects(List<Object> objects) throws TypeNotFoundInModelException {
        for (Object anObject : objects) {
            this.addObject(anObject);
        }
    }

    /**
	 * <p>
	 * Adds a given {@link Object} to the {@link List} of
	 * {@link IModelInstanceElement}s.
	 * </p>
	 * 
	 * @param anObject
	 *            The {@link Object} which shall be added to the {@link List} of
	 *            {@link IModelInstanceElement}s.
	 * @return The added {@link IModelInstanceElement}.
	 * @throws TypeNotFoundInModelException
	 *             Thrown if a given Object, cannot be adapted to a {@link Type}
	 *             in the {@link IModel}.
	 */
    private IModelInstanceElement addObject(Object anObject) throws TypeNotFoundInModelException {
        IModelInstanceElement result;
        result = this.myModelInstanceFactory.createModelInstanceElement(anObject);
        if (result == null) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_ObjectDoesNoMatchToModel;
            msg = NLS.bind(msg, anObject.getClass(), this.myModel.getDisplayName());
            LOGGER.error(msg);
            throw new TypeNotFoundInModelException(msg);
        }
        if (result instanceof IModelInstanceObject) {
            this.myModelInstanceObjects.add((IModelInstanceObject) result);
        }
        return result;
    }

    /**
	 * <p>
	 * A helper {@link Method} used to find a static method of this
	 * {@link JavaModelInstance} that conforms to a given {@link Operation}.
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
    private Method findStaticMethod(Operation operation) throws OperationNotFoundException {
        Method result;
        String methodSourceClassCanonicalName;
        Class<?> methodSourceClass;
        methodSourceClassCanonicalName = JavaModelInstanceTypeUtility.toCanonicalName(operation.getOwner().getQualifiedNameList());
        try {
            methodSourceClass = loadJavaClass(methodSourceClassCanonicalName, this.myClassLoaders);
            result = null;
            for (Method aMethod : methodSourceClass.getDeclaredMethods()) {
                boolean nameIsEqual;
                boolean resultTypeIsConform;
                boolean argumentSizeIsEqual;
                nameIsEqual = aMethod.getName().equals(operation.getName());
                resultTypeIsConform = JavaModelInstanceTypeUtility.conformsTypeToType(aMethod.getGenericReturnType(), operation.getType());
                argumentSizeIsEqual = aMethod.getParameterTypes().length == operation.getSignatureParameter().size();
                if (nameIsEqual && resultTypeIsConform && argumentSizeIsEqual) {
                    java.lang.reflect.Type[] javaTypes;
                    List<Parameter> pivotModelParamters;
                    boolean matches;
                    javaTypes = aMethod.getGenericParameterTypes();
                    pivotModelParamters = operation.getSignatureParameter();
                    matches = true;
                    for (int index = 0; index < operation.getSignatureParameter().size(); index++) {
                        if (!JavaModelInstanceTypeUtility.conformsTypeToType(javaTypes[index], pivotModelParamters.get(index).getType())) {
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
            if (result == null) {
                String msg;
                msg = JavaModelInstanceTypeMessages.JavaModelInstance_StaticOperationNotFound;
                msg = NLS.bind(msg, operation, "Given Operation does not exist in implementation.");
                throw new OperationNotFoundException(msg);
            }
        } catch (ClassNotFoundException e) {
            String msg;
            msg = JavaModelInstanceTypeMessages.JavaModelInstance_StaticOperationNotFound;
            msg = NLS.bind(msg, operation, e.getMessage());
            throw new OperationNotFoundException(msg, e);
        }
        return result;
    }
}
