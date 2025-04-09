package jaxlib.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

/**
 * A globally shared cache for weakly referenced singleton objects.
 * <p>
 * This class usually is used to instantiate public classes providing some sort of factory pattern where
 * the factorie's class is unknown at compile time.
 * </p><p>
 * Instances are automatically removed from the cache when they or their class and classloader aren't 
 * referenced anymore. This class guarantees at any time to deliver the same object instance if the previous 
 * instance has not been freed by the garbage collector.
 * </p>
 *
 * @author  <a href="mailto:joerg.wassmer@web.de">J�rg Wa�mer</a>
 * @since   JaXLib 1.0
 * @version $Id: SingletonRegistry.java 1461 2005-11-20 20:38:37Z joerg_wassmer $
 */
public final class SingletonRegistry extends Object {

    private SingletonRegistry() {
        super();
    }

    private static final ClassRegistry<Class, Object> registry = new ClassRegistry<Class, Object>(true);

    /**
   * Returns a shared instance of the specified class.
   * <p>
   * The specified class must implement the public default constructor. The instance will be removed from
   * the cache once it isn't referenced anymore by any caller of this method. Thus callers should consider
   * to keep a reference to the returned object over time. Creation of new instances is expensive, while
   * retrieving existing ones is cheap.
   * </p><p>
   * A particular class may consider to call this method at time of its initalization with itself as argument,
   * keeping a static reference to the returned object. Such handled instances will stay in the cache until
   * the class gets unloaded. 
   * </p>
   *
   * @throws ExceptionInInitializerError
   *  if thrown during initalization of the specified class or of another class referenced by the specified
   *  one.
   * @throws Error
   *  if thrown by the constructor.
   * @throws SecurityException
   *  if thrown by the Java reflection mechanism or by the constructor itself. 
   * @throws IllegalArgumentException 
   *  if the specified class is primitive, abstract or not public.
   * @throws IllegalArgumentException 
   *  if the specified class is not implementing the public default constructor.
   *  The cause of this exception is an instance of {@link NoSuchMethodException}.
   * @throws IllegalArgumentException 
   *  if the Java reflection mechanism throws an {@link InstantiationException}, 
   *  {@link IllegalAccessException} or {@link InvocationTargetException}. The cause of the thrown
   *  {@code IllegalArgumentException} is an instance of the three specified types of exception.
   * @throws RuntimeException
   *  if thrown by the Java reflection mechanism or by the constructor itself. 
   * @throws NullPointerException
   *  if {@code type == null}.
   *
   * @see #registerSelf(Object)
   *
   * @since JaXLib 1.0
   */
    public static <T> T get(Class<T> type) {
        Object instance = registry.get((Class) type);
        if (instance == null) {
            if (type.isPrimitive()) throw new IllegalArgumentException("Class is primitive: " + type);
            final int modifiers = type.getModifiers();
            if (!Modifier.isPublic(modifiers)) throw new IllegalArgumentException("Class is not public: " + type);
            if (type.isInterface()) throw new IllegalArgumentException("Class is an interface: " + type);
            if (Modifier.isAbstract(modifiers)) throw new IllegalArgumentException("Class is abstract: " + type);
            try {
                Class.forName(type.getName());
                instance = registry.get((Class) type);
                if (instance != null) return (T) instance;
            } catch (ClassNotFoundException ex) {
            }
            Constructor ctor;
            try {
                ctor = type.getConstructor((Class[]) null);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("Class implements no public default constructor: " + type, ex);
            }
            try {
                instance = registry.putIfAbsentAndGet((Class) type, ctor.newInstance((Object[]) null));
            } catch (Error ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw ex;
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException("Exception invoking constructor: " + ctor, ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException("Constructor is not accessible: " + ctor, ex);
            } catch (InvocationTargetException ex) {
                throw new IllegalArgumentException("Exception thrown by constructor: " + ctor, ex);
            }
        }
        return (T) instance;
    }

    /**
   * Allows a class to register an instance of itself.
   * <p>
   * Classes registered through this method don't need to be public nor need to provide any special
   * constructor.
   * </p><p>
   * Classes calling this method usually want to keep a strong reference to the specified instance to 
   * prevent it from being garbage collected before the class gets unloaded.
   * </p>
   *
   * @return
   *  the instance registered after this call. This is identical to the specified object if no instance
   *  has been registered before or if the previously registered instance has been freed by the garbage
   *  collector.
   *
   * @throws NullPointerException
   *  if {@code instance == null}.
   * @throws SecurityException
   *  if the caller's class is not identical to the class of the specified object.
   * @throws SecurityException
   *  if the {@link StackTraces} class has not the permissions required to determine the caller class.
   *
   * @since JaXLib 1.0
   */
    public static <T> T registerSelf(T instance) {
        if (instance == null) throw new NullPointerException("instance");
        Class type = instance.getClass();
        Class callerClass = StackTraces.getCallerClass();
        if (callerClass != type) {
            throw new SecurityException("This method is allowed to be called by the class to be registered itself only:" + "\n  caller class = " + callerClass + "\n  object class = " + type);
        }
        return (T) registry.putIfAbsentAndGet((Class) type, instance);
    }
}
