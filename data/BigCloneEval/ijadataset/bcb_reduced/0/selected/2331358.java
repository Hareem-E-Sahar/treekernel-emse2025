package javax.management;

import java.lang.reflect.Method;
import java.security.AccessController;
import com.sun.jmx.mbeanserver.GetPropertyAction;
import com.sun.jmx.mbeanserver.Introspector;

/**
 * Describes an MBean attribute exposed for management.  Instances of
 * this class are immutable.  Subclasses may be mutable but this is
 * not recommended.
 *
 * @since 1.5
 */
public class MBeanAttributeInfo extends MBeanFeatureInfo implements Cloneable {

    private static final long serialVersionUID;

    static {
        long uid = 8644704819898565848L;
        try {
            GetPropertyAction act = new GetPropertyAction("jmx.serial.form");
            String form = AccessController.doPrivileged(act);
            if ("1.0".equals(form)) uid = 7043855487133450673L;
        } catch (Exception e) {
        }
        serialVersionUID = uid;
    }

    static final MBeanAttributeInfo[] NO_ATTRIBUTES = new MBeanAttributeInfo[0];

    /**
     * @serial The actual attribute type.
     */
    private final String attributeType;

    /**
     * @serial The attribute write right.
     */
    private final boolean isWrite;

    /**
     * @serial The attribute read right.
     */
    private final boolean isRead;

    /**
     * @serial Indicates if this method is a "is"
     */
    private final boolean is;

    /**
     * Constructs an <CODE>MBeanAttributeInfo</CODE> object.
     *
     * @param name The name of the attribute.
     * @param type The type or class name of the attribute.
     * @param description A human readable description of the attribute.
     * @param isReadable True if the attribute has a getter method, false otherwise.
     * @param isWritable True if the attribute has a setter method, false otherwise.
     * @param isIs True if this attribute has an "is" getter, false otherwise.
     *
     * @throws IllegalArgumentException if {@code isIs} is true but
     * {@code isReadable} is not, or if {@code isIs} is true and
     * {@code type} is not {@code boolean} or {@code java.lang.Boolean}.
     * (New code should always use {@code boolean} rather than
     * {@code java.lang.Boolean}.)
     */
    public MBeanAttributeInfo(String name, String type, String description, boolean isReadable, boolean isWritable, boolean isIs) {
        this(name, type, description, isReadable, isWritable, isIs, (Descriptor) null);
    }

    /**
     * Constructs an <CODE>MBeanAttributeInfo</CODE> object.
     *
     * @param name The name of the attribute.
     * @param type The type or class name of the attribute.
     * @param description A human readable description of the attribute.
     * @param isReadable True if the attribute has a getter method, false otherwise.
     * @param isWritable True if the attribute has a setter method, false otherwise.
     * @param isIs True if this attribute has an "is" getter, false otherwise.
     * @param descriptor The descriptor for the attribute.  This may be null
     * which is equivalent to an empty descriptor.
     *
     * @throws IllegalArgumentException if {@code isIs} is true but
     * {@code isReadable} is not, or if {@code isIs} is true and
     * {@code type} is not {@code boolean} or {@code java.lang.Boolean}.
     * (New code should always use {@code boolean} rather than
     * {@code java.lang.Boolean}.)
     *
     * @since 1.6
     */
    public MBeanAttributeInfo(String name, String type, String description, boolean isReadable, boolean isWritable, boolean isIs, Descriptor descriptor) {
        super(name, description, descriptor);
        this.attributeType = type;
        this.isRead = isReadable;
        this.isWrite = isWritable;
        if (isIs && !isReadable) {
            throw new IllegalArgumentException("Cannot have an \"is\" getter " + "for a non-readable attribute");
        }
        if (isIs && !type.equals("java.lang.Boolean") && !type.equals("boolean")) {
            throw new IllegalArgumentException("Cannot have an \"is\" getter " + "for a non-boolean attribute");
        }
        this.is = isIs;
    }

    /**
     * <p>This constructor takes the name of a simple attribute, and Method
     * objects for reading and writing the attribute.  The {@link Descriptor}
     * of the constructed object will include fields contributed by any
     * annotations on the {@code Method} objects that contain the
     * {@link DescriptorKey} meta-annotation.
     *
     * @param name The programmatic name of the attribute.
     * @param description A human readable description of the attribute.
     * @param getter The method used for reading the attribute value.
     *          May be null if the property is write-only.
     * @param setter The method used for writing the attribute value.
     *          May be null if the attribute is read-only.
     * @exception IntrospectionException There is a consistency
     * problem in the definition of this attribute.
     */
    public MBeanAttributeInfo(String name, String description, Method getter, Method setter) throws IntrospectionException {
        this(name, attributeType(getter, setter), description, (getter != null), (setter != null), isIs(getter), ImmutableDescriptor.union(Introspector.descriptorForElement(getter), Introspector.descriptorForElement(setter)));
    }

    /**
     * <p>Returns a shallow clone of this instance.
     * The clone is obtained by simply calling <tt>super.clone()</tt>,
     * thus calling the default native shallow cloning mechanism
     * implemented by <tt>Object.clone()</tt>.
     * No deeper cloning of any internal field is made.</p>
     *
     * <p>Since this class is immutable, cloning is chiefly of
     * interest to subclasses.</p>
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns the class name of the attribute.
     *
     * @return the class name.
     */
    public String getType() {
        return attributeType;
    }

    /**
     * Whether the value of the attribute can be read.
     *
     * @return True if the attribute can be read, false otherwise.
     */
    public boolean isReadable() {
        return isRead;
    }

    /**
     * Whether new values can be written to the attribute.
     *
     * @return True if the attribute can be written to, false otherwise.
     */
    public boolean isWritable() {
        return isWrite;
    }

    /**
     * Indicates if this attribute has an "is" getter.
     *
     * @return true if this attribute has an "is" getter.
     */
    public boolean isIs() {
        return is;
    }

    public String toString() {
        String access;
        if (isReadable()) {
            if (isWritable()) access = "read/write"; else access = "read-only";
        } else if (isWritable()) access = "write-only"; else access = "no-access";
        return getClass().getName() + "[" + "description=" + getDescription() + ", " + "name=" + getName() + ", " + "type=" + getType() + ", " + access + ", " + (isIs() ? "isIs, " : "") + "descriptor=" + getDescriptor() + "]";
    }

    /**
     * Compare this MBeanAttributeInfo to another.
     *
     * @param o the object to compare to.
     *
     * @return true if and only if <code>o</code> is an MBeanAttributeInfo such
     * that its {@link #getName()}, {@link #getType()}, {@link
     * #getDescription()}, {@link #isReadable()}, {@link
     * #isWritable()}, and {@link #isIs()} values are equal (not
     * necessarily identical) to those of this MBeanAttributeInfo.
     */
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MBeanAttributeInfo)) return false;
        MBeanAttributeInfo p = (MBeanAttributeInfo) o;
        return (p.getName().equals(getName()) && p.getType().equals(getType()) && p.getDescription().equals(getDescription()) && p.getDescriptor().equals(getDescriptor()) && p.isReadable() == isReadable() && p.isWritable() == isWritable() && p.isIs() == isIs());
    }

    public int hashCode() {
        return getName().hashCode() ^ getType().hashCode();
    }

    private static boolean isIs(Method getter) {
        return (getter != null && getter.getName().startsWith("is") && (getter.getReturnType().equals(Boolean.TYPE) || getter.getReturnType().equals(Boolean.class)));
    }

    /**
     * Finds the type of the attribute.
     */
    private static String attributeType(Method getter, Method setter) throws IntrospectionException {
        Class type = null;
        if (getter != null) {
            if (getter.getParameterTypes().length != 0) {
                throw new IntrospectionException("bad getter arg count");
            }
            type = getter.getReturnType();
            if (type == Void.TYPE) {
                throw new IntrospectionException("getter " + getter.getName() + " returns void");
            }
        }
        if (setter != null) {
            Class params[] = setter.getParameterTypes();
            if (params.length != 1) {
                throw new IntrospectionException("bad setter arg count");
            }
            if (type == null) type = params[0]; else if (type != params[0]) {
                throw new IntrospectionException("type mismatch between " + "getter and setter");
            }
        }
        if (type == null) {
            throw new IntrospectionException("getter and setter cannot " + "both be null");
        }
        return type.getName();
    }
}
