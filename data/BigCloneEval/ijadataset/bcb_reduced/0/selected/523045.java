package ca.uhn.hl7v2.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.ModelClassFactory;

/**
 * <p>
 * Provides common functionality needed by implementers of the Segment
 * interface.
 * </p>
 * <p>
 * Implementing classes should define all the fields for the segment they
 * represent in their constructor. The add() method is useful for this purpose.
 * </p>
 * <p>
 * For example the constructor for an MSA segment might contain the following
 * code:<br>
 * <code>this.add(new ID(), true, 2, null);<br>
 * this.add(new ST(), true, 20, null);<br>...</code>
 * </p>
 * 
 * @author Bryan Tripp (bryan_tripp@sourceforge.net)
 */
public abstract class AbstractSegment extends AbstractStructure implements Segment {

    private static final long serialVersionUID = -6686329916234746948L;

    private List<List<Type>> fields;

    private List<Class<? extends Type>> types;

    private List<Boolean> required;

    private List<Integer> length;

    private List<Object> args;

    private List<Integer> maxReps;

    private List<String> names;

    /**
	 * Calls the abstract init() method to create the fields in this segment.
	 * 
	 * @param parent
	 *            parent group
	 * @param factory
	 *            all implementors need a model class factory to find datatype
	 *            classes, so we include it as an arg here to emphasize that
	 *            fact ... AbstractSegment doesn't actually use it though
	 */
    public AbstractSegment(Group parent, ModelClassFactory factory) {
        super(parent);
        this.fields = new ArrayList<List<Type>>();
        this.types = new ArrayList<Class<? extends Type>>();
        this.required = new ArrayList<Boolean>();
        this.length = new ArrayList<Integer>();
        this.args = new ArrayList<Object>();
        this.maxReps = new ArrayList<Integer>();
        this.names = new ArrayList<String>();
    }

    /**
	 * Returns an array of Field objects at the specified location in the
	 * segment. In the case of non-repeating fields the array will be of length
	 * one. Fields are numbered from 1.
	 */
    public Type[] getField(int number) throws HL7Exception {
        List<Type> retVal = getFieldAsList(number);
        return retVal.toArray(new Type[retVal.size()]);
    }

    /**
	 * Returns an array of a specific type class
	 */
    protected <T extends Type> T[] getTypedField(int number, T[] array) {
        List<Type> retVal;
        try {
            retVal = getFieldAsList(number);
            @SuppressWarnings("unchecked") List<T> cast = (List<T>) (List<?>) retVal;
            return cast.toArray(array);
        } catch (ClassCastException cce) {
            log.error("Unexpected problem obtaining field value.  This is a bug.", cce);
            throw new RuntimeException(cce);
        } catch (HL7Exception he) {
            log.error("Unexpected problem obtaining field value.  This is a bug.", he);
            throw new RuntimeException(he);
        }
    }

    protected int getReps(int number) {
        try {
            return getFieldAsList(number).size();
        } catch (HL7Exception he) {
            log.error("Unexpected problem obtaining field value.  This is a bug.", he);
            throw new RuntimeException(he);
        }
    }

    private List<Type> getFieldAsList(int number) throws HL7Exception {
        ensureEnoughFields(number);
        if (number < 1 || number > fields.size()) {
            throw new HL7Exception("Can't retrieve field " + number + " from segment " + this.getClass().getName() + " - there are only " + fields.size() + " fields.", HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        return fields.get(number - 1);
    }

    /**
	 * Returns a specific repetition of field at the specified index. If there
	 * exist fewer repetitions than are required, the number of repetitions can
	 * be increased by specifying the lowest repetition that does not yet exist.
	 * For example if there are two repetitions but three are needed, the third
	 * can be created and accessed using the following code: <br>
	 * <code>Type t = getField(x, 3);</code>
	 * 
	 * @param number
	 *            the field number (starting at 1)
	 * @param rep
	 *            the repetition number (starting at 0)
	 * @throws HL7Exception
	 *             if field index is out of range, if the specified repetition
	 *             is greater than the maximum allowed, or if the specified
	 *             repetition is more than 1 greater than the existing # of
	 *             repetitions.
	 */
    public Type getField(int number, int rep) throws HL7Exception {
        ensureEnoughFields(number);
        if (number < 1 || number > fields.size()) {
            throw new HL7Exception("Can't get field " + number + " in segment " + getName() + " - there are currently only " + fields.size() + " reps.", HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        List<Type> arr = fields.get(number - 1);
        if (rep > arr.size()) throw new HL7Exception("Can't get repetition " + rep + " from field " + number + " - there are currently only " + arr.size() + " reps.", HL7Exception.APPLICATION_INTERNAL_ERROR);
        if (rep == arr.size()) {
            Type newType = createNewType(number);
            arr.add(newType);
        }
        return arr.get(rep);
    }

    /**
	 * Returns a specific repetition of field with concrete type at the specified index
	 */
    protected <T extends Type> T getTypedField(int number, int rep) {
        try {
            @SuppressWarnings("unchecked") T retVal = (T) getField(number, rep);
            return retVal;
        } catch (ClassCastException cce) {
            log.error("Unexpected problem obtaining field value.  This is a bug.", cce);
            throw new RuntimeException(cce);
        } catch (HL7Exception he) {
            log.error("Unexpected problem obtaining field value.  This is a bug.", he);
            throw new RuntimeException(he);
        }
    }

    /**
	 * <p>
	 * Attempts to create an instance of a field type without using reflection.
	 * </p>
	 * <p>
	 * Note that the default implementation just returns <code>null</code>, and
	 * it is not neccesary to override this method to provide any particular
	 * behaviour. When a new field instance is needed within a segment, this
	 * method is tried first, and if it returns <code>null</code>, reflection is
	 * used instead. Implementations of this method is auto-generated by the
	 * source generator module.
	 * </p>
	 * 
	 * @return Returns a newly instantiated type, or <code>null</code> if not
	 *         possible
	 * @param field
	 *            Field number - Note that this is zero indexed!
	 */
    protected Type createNewTypeWithoutReflection(int field) {
        return null;
    }

    /**
	 * Creates a new instance of the Type at the given field number in this
	 * segment.
	 */
    private Type createNewType(int field) throws HL7Exception {
        Type retVal = createNewTypeWithoutReflection(field - 1);
        if (retVal != null) {
            return retVal;
        }
        int number = field - 1;
        Class<? extends Type> c = this.types.get(number);
        Type newType = null;
        try {
            Object[] args = getArgs(number);
            Class<?>[] argClasses = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Message) {
                    argClasses[i] = Message.class;
                } else {
                    argClasses[i] = args[i].getClass();
                }
            }
            newType = c.getConstructor(argClasses).newInstance(args);
        } catch (IllegalAccessException iae) {
            throw new HL7Exception("Can't access class " + c.getName() + " (" + iae.getClass().getName() + "): " + iae.getMessage(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        } catch (InstantiationException ie) {
            throw new HL7Exception("Can't instantiate class " + c.getName() + " (" + ie.getClass().getName() + "): " + ie.getMessage(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        } catch (InvocationTargetException ite) {
            throw new HL7Exception("Can't instantiate class " + c.getName() + " (" + ite.getClass().getName() + "): " + ite.getMessage(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        } catch (NoSuchMethodException nme) {
            throw new HL7Exception("Can't instantiate class " + c.getName() + " (" + nme.getClass().getName() + "): " + nme.getMessage(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        return newType;
    }

    private Object[] getArgs(int fieldNum) {
        Object[] result = null;
        Object o = this.args.get(fieldNum);
        if (o != null && o instanceof Object[]) {
            result = (Object[]) o;
        } else {
            result = new Object[] { getMessage() };
        }
        return result;
    }

    /**
	 * Returns true if the given field is required in this segment - fields are
	 * numbered from 1.
	 * 
	 * @throws HL7Exception
	 *             if field index is out of range.
	 */
    public boolean isRequired(int number) throws HL7Exception {
        if (number < 1 || number > required.size()) {
            throw new HL7Exception("Can't retrieve optionality of field " + number + " from segment " + this.getClass().getName() + " - there are only " + fields.size() + " fields.", HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        try {
            return required.get(number - 1);
        } catch (Exception e) {
            throw new HL7Exception("Can't retrieve optionality of field " + number + ": " + e.getMessage(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
    }

    /**
	 * Returns the maximum length of the field at the given index, in characters
	 * - fields are numbered from 1.
	 * 
	 * @throws HL7Exception
	 *             if field index is out of range.
	 */
    public int getLength(int number) throws HL7Exception {
        if (number < 1 || number > length.size()) {
            throw new HL7Exception("Can't retrieve max length of field " + number + " from segment " + this.getClass().getName() + " - there are only " + fields.size() + " fields.", HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        try {
            return length.get(number - 1);
        } catch (Exception e) {
            throw new HL7Exception("Can't retrieve max length of field " + number + ": " + e.getMessage(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
    }

    /**
	 * Returns the number of repetitions of this field that are allowed.
	 * 
	 * @throws HL7Exception
	 *             if field index is out of range.
	 */
    public int getMaxCardinality(int number) throws HL7Exception {
        if (number < 1 || number > length.size()) {
            throw new HL7Exception("Can't retrieve cardinality of field " + number + " from segment " + this.getClass().getName() + " - there are only " + fields.size() + " fields.", HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        try {
            return maxReps.get(number - 1);
        } catch (Exception e) {
            throw new HL7Exception("Can't retrieve max repetitions of field " + number + ": " + e.getMessage(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
    }

    /**
	 * @deprecated Use {@link #add(Class, boolean, int, int, Object[], String)}
	 */
    protected void add(Class<? extends Type> c, boolean required, int maxReps, int length, Object[] constructorArgs) throws HL7Exception {
        add(c, required, maxReps, length, constructorArgs, null);
    }

    /**
	 * Adds a field to the segment. The field is initially empty (zero
	 * repetitions). The field number is sequential depending on previous add()
	 * calls. Implementing classes should use the add() method in their
	 * constructor in order to define fields in their segment.
	 * 
	 * @param c
	 *            the class of the data for this field - this should inherit
	 *            from Type
	 * @param required
	 *            whether a value for this field is required in order for the
	 *            segment to be valid
	 * @param maxReps
	 *            the maximum number of repetitions - 0 implies that there is no
	 *            limit
	 * @param length
	 *            the maximum length of each repetition of the field (in
	 *            characters)
	 * @param constructorArgs
	 *            an array of objects that will be used as constructor arguments
	 *            if new instances of this class are created (use null for
	 *            zero-arg constructor)
	 * @param name
	 *            the name of the field
	 * @throws HL7Exception
	 *             if the given class does not inherit from Type or if it can
	 *             not be instantiated.
	 */
    protected void add(Class<? extends Type> c, boolean required, int maxReps, int length, Object[] constructorArgs, String name) throws HL7Exception {
        List<Type> arr = new ArrayList<Type>();
        this.types.add(c);
        this.fields.add(arr);
        this.required.add(required);
        this.length.add(length);
        this.args.add(constructorArgs);
        this.maxReps.add(maxReps);
        this.names.add(name);
    }

    /**
	 * Called from getField(...) methods. If a field has been requested that
	 * doesn't exist (eg getField(15) when only 10 fields in segment) adds
	 * Varies fields to the end of the segment up to the required number.
	 */
    private void ensureEnoughFields(int fieldRequested) {
        int fieldsToAdd = fieldRequested - this.numFields();
        if (fieldsToAdd < 0) {
            fieldsToAdd = 0;
        }
        try {
            for (int i = 0; i < fieldsToAdd; i++) {
                this.add(Varies.class, false, 0, 65536, null);
            }
        } catch (HL7Exception e) {
            log.error("Can't create additional generic fields to handle request for field " + fieldRequested, e);
        }
    }

    public static void main(String[] args) {
    }

    /**
	 * Returns the number of fields defined by this segment (repeating fields
	 * are not counted multiple times).
	 */
    public int numFields() {
        return this.fields.size();
    }

    /**
	 * Returns the class name (excluding package).
	 * 
	 * @see Structure#getName()
	 */
    public String getName() {
        String fullName = this.getClass().getName();
        return fullName.substring(fullName.lastIndexOf('.') + 1, fullName.length());
    }

    /**
	 * {@inheritDoc}
	 */
    public String[] getNames() {
        return names.toArray(new String[names.size()]);
    }

    /**
	 * {@inheritDoc }
	 * 
	 * <p>
	 * <b>Note that this method will not currently work to parse an MSH segment
	 * if the encoding characters are not already set. This limitation should be
	 * resulved in a future version</b>
	 * </p>
	 */
    public void parse(String string) throws HL7Exception {
        EncodingCharacters encodingCharacters = EncodingCharacters.getInstance(getMessage());
        clear();
        getMessage().getParser().parse(this, string, encodingCharacters);
    }

    /**
	 * {@inheritDoc }
	 */
    public String encode() throws HL7Exception {
        return getMessage().getParser().doEncode(this, EncodingCharacters.getInstance(getMessage()));
    }

    /**
	 * Removes a repetition of a given field by name. For example, if a PID
	 * segment contains 10 repititions a "Patient Identifier List" field and
	 * "Patient Identifier List" is supplied with an index of 2, then this call
	 * would remove the 3rd repetition.
	 * 
	 * @return The removed structure
	 * @throws HL7Exception
	 *             if the named Structure is not part of this Group.
	 */
    protected Type removeRepetition(int fieldNum, int index) throws HL7Exception {
        if (fieldNum < 1 || fieldNum > fields.size()) {
            throw new HL7Exception("The field " + fieldNum + " does not exist in the segment " + this.getClass().getName(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        String name = names.get(fieldNum - 1);
        List<Type> list = fields.get(fieldNum - 1);
        if (list.size() == 0) {
            throw new HL7Exception("Invalid index: " + index + ", structure " + name + " has no repetitions", HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        if (list.size() <= index) {
            throw new HL7Exception("Invalid index: " + index + ", structure " + name + " must be between 0 and " + (list.size() - 1), HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        return list.remove(index);
    }

    /**
	 * Inserts a repetition of a given Field into repetitions of that field by
	 * name.
	 * 
	 * @return The newly created and inserted field
	 * @throws HL7Exception
	 *             if the named Structure is not part of this Group.
	 */
    protected Type insertRepetition(int fieldNum, int index) throws HL7Exception {
        if (fieldNum < 1 || fieldNum > fields.size()) {
            throw new HL7Exception("The field " + fieldNum + " does not exist in the segment " + this.getClass().getName(), HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        List<Type> list = fields.get(fieldNum - 1);
        Type newType = createNewType(fieldNum);
        list.add(index, newType);
        return newType;
    }

    /**
	 * Clears all data from this segment
	 */
    public void clear() {
        for (List<Type> next : fields) {
            next.clear();
        }
    }
}
