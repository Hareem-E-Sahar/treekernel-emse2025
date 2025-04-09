package JSX;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

class XMLDeserialize {

    /** --------------------------------------------------------------------------
	* debug constants
	* ---------------
	**/
    private static final boolean VERSION_DEBUG = false;

    private static final boolean DEBUG_CUSTOM = false;

    private static final boolean DEBUG = false;

    private static final boolean TESTCIRC = false;

    private static final boolean ALIASDEBUG = false;

    private static final boolean INTERNAL_DEBUG = false;

    private static final boolean DEBUG_CLASS = false;

    private static final boolean FINAL_DEBUG = false;

    static final boolean GETFIELD_DEBUG = false;

    static final boolean READRESOLVE_DEBUG = false;

    static final boolean IBM_DEBUG = false;

    static final boolean KELLY_DEBUG = false;

    private static final boolean SUPER_DEBUG = false;

    private static final boolean NESTED_DEBUG = false;

    private static final boolean NESTEDREADOBJECT_DEBUG = false;

    private static final boolean ADDATTR_NULL_DEBUG = false;

    private static final boolean KELLYSUPER_DEBUG = false;

    private static final boolean NOV_DEBUG = false;

    private static final boolean CFG_DEBUG = false;

    private static final boolean MIKE_DEBUG = false;

    /** --------------------------------------------------------------------------
	* Deserialization fields
	* ----------------------
	* NB: there are others scattered through the code, proximate to their use.
	**/
    private ParserXML p;

    /** --------------------------------------------------------------------------
	* alias fields
	* ------------
	* Consider: extract as an object.  It has state, init, and API.
	* Consider: change name from 'aliasHash' to 'aliasTable'.
	**/
    private int aliasSerialNumber;

    private Hashtable aliasHash = new Hashtable();

    /**--------------------------------------------------------------------------
		* main
		* ----
		* Version information only; for test code, see JSXTest.java
		**/
    public static void main(String args[]) {
        System.err.println(XMLSerialize.VERSION);
    }

    /**--------------------------------------------------------------------------
		* XMLDeserialize(in) - core constructor
		* --------------
		* Core constructor (others call it)
		* init alias *only* here, or confuses re-enterant code (internal ser)
		* ALT: we could have an explicit null, instead of with/without CoNfig.
		**/
    XMLDeserialize(Reader in) {
        reset();
        p = new ParserXML(in);
    }

    private Config cfg = new Config();

    XMLDeserialize(Reader in, Config cfg) {
        this.cfg = cfg;
        reset();
        p = new ParserXML(in);
    }

    /** XMLDeserialize()
		* --------------
		* Default to System.in
		**/
    XMLDeserialize() {
        this(new InputStreamReader(System.in));
    }

    GetFieldImpl currentGetField = new GetFieldImpl();

    static final class GetFieldImpl extends java.io.ObjectInputStream.GetField {

        HashMap objMap = new HashMap();

        public String toString() {
            return "GetFieldImpl state: " + objMap;
        }

        GetFieldImpl() {
        }

        void putObj(String name, Object value) {
            if (GETFIELD_DEBUG) System.err.println("putObj(" + name + ", " + value + ");");
            objMap.put(name, value);
        }

        public ObjectStreamClass getObjectStreamClass() {
            return null;
        }

        public boolean defaulted(String name) throws IOException, IllegalArgumentException {
            return !objMap.containsKey(name);
        }

        public boolean get(String name, boolean defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return ParseUtilities.parseBoolean((String) objMap.get(name)); else return defvalue;
        }

        public char get(String name, char defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return ((String) objMap.get(name)).charAt(0); else return defvalue;
        }

        public byte get(String name, byte defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return Byte.parseByte((String) objMap.get(name)); else return defvalue;
        }

        public short get(String name, short defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return Short.parseShort((String) objMap.get(name)); else return defvalue;
        }

        public int get(String name, int defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return Integer.parseInt((String) objMap.get(name)); else return defvalue;
        }

        public long get(String name, long defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return Long.parseLong((String) objMap.get(name)); else return defvalue;
        }

        public float get(String name, float defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return ParseUtilities.parseFloat((String) objMap.get(name)); else return defvalue;
        }

        public double get(String name, double defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return ParseUtilities.parseDouble((String) objMap.get(name)); else return defvalue;
        }

        public Object get(String name, Object defvalue) throws IOException, IllegalArgumentException {
            if (objMap.containsKey(name)) return objMap.get(name); else return defvalue;
        }
    }

    int invocationCount = 0;

    /** deserialize()
		* -----------
		* Entry point.
		* NB: invocationCount distinguisges between first time callers, and
		* calls from internal serialization.  This allows both old and new
		* API's to work.
		**/
    String jsxVersion;

    boolean superVersion = false;

    Object deserialize() throws ParserXML.ExceptionXML, ClassNotFoundException, IOException {
        if (MIKE_DEBUG) System.err.println("LOGGING: entering *deserialize* of the Mike Special");
        if (INTERNAL_DEBUG) System.err.println("RETRIEVE optional data object: " + objStoreIndex);
        if (invocationCount == 0) {
            reset();
            invocationCount++;
            ParserXML.Tag tag = p.readTag();
            if (tag == null) throw new EOFException("No XML found in input");
            while (tag.isPI) {
                if (VERSION_DEBUG) System.err.println("start PI: " + tag);
                ParserXML.Attr attr;
                while (!(attr = p.readAttr()).isEndPI) {
                    if (attr.isEnd) throw new IOException("PI should end with '?>', not '/>'");
                    if (attr.name.equals("version")) {
                        if (VERSION_DEBUG) System.err.println("\tversion: " + attr.value);
                        if (tag.name.equals(XMLSerialize.JSX_HEADER_TARGET)) {
                            jsxVersion = attr.value;
                            if (jsxVersion.equals("1")) {
                                superVersion = false;
                            } else if (jsxVersion.equals("2")) {
                                superVersion = true;
                            } else {
                                throw new IOException("Unknown jsx version=\"" + jsxVersion + "\".  You need to upgrade to read this later version");
                            }
                        }
                    } else if (attr.name.equals("encoding")) {
                        if (VERSION_DEBUG) System.err.println("\tencoding: " + attr.value);
                    } else {
                        if (VERSION_DEBUG) System.err.println("\tunknown attr: " + attr + " in PI: " + tag);
                    }
                }
                if (VERSION_DEBUG) System.err.println("end PI: ?>");
                tag = p.readTag();
            }
            if (!tag.start) throw new ParserXML.ExceptionXML("Expected start tag, got \"</" + tag.name + ">\"");
            Object o = createObject(tag, null);
            if (TESTCIRC) System.err.println("Count was: " + (aliasSerialNumber - XMLSerialize.ALIAS_INIT));
            if (ALIASDEBUG) System.err.println(aliasHash);
            invocationCount--;
            return o;
        } else {
            if (superVersion) {
                defROSubstitute();
                ParserXML.Tag tag = p.readTag();
                if (NOV_DEBUG) System.err.println("readObject: just read a " + tag);
                return createObject(tag, null);
            } else return getObj();
        }
    }

    void defROSubstitute() throws IOException {
        try {
            if (NOV_DEBUG) System.err.println("entering defROSubstitute:");
            if (defROCalledStack.get() == false) {
                if (NOV_DEBUG) System.err.println("\tfor first time");
                defROCalledStack.set(true);
                defaultReadObject_ver2();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("unexpected default fields - that's OK in itself," + "the problem is: " + e);
            e.printStackTrace();
        }
    }

    void reset() {
        if (invocationCount != 0) throw new Error("Deserialize: Attempt to reset alias table mid-way.");
        initAlias();
    }

    void close() {
        p.close();
    }

    /** =========================================================================
		* alias nascent-object
		* --------------------
		* NB: init, fields and API: extract into a class.
		* Should move this into a separate, pluggable class, so that
		* (1). the implementation can be changed easily
		* (2). a co-developer can work on it in isolation
		* (3). it can be recompiled separately - faster recompilation
		* (4). it can be reused elsewhere
		* (5). a different implementation can be plugged in (including a subclass)
		*
		* All are aspects of "divide and conquer" - the danger is to divide 
		* overzealously, in an inappropriate way, which counterproductively
		* increases complexity.  However, when a division is clear, it really
		* should be done.
		* The reluctance to do it is that the change may introduce bugs.  It is
		* work, and uncertainty, and fear of opening a can of worms - better to
		* let sleeping dogs lie.
		* And yet, because it has been used as it were a separate object, it
		* should not be too hard.  And if there are ripples in the force, it is
		* good to find them and tame them, rather than fear them.
		*
		* It would also be good to have an "alias" API - javadocs could produce
		* this automatically.  Makes it easier to work with.  Looks more
		* professional (to employers, licensees), and more welcoming to 
		* co-developers.   Reduces barriers to contribution.  Increases chances
		* of patches being offered.
		**/
    private void initAlias() {
        aliasSerialNumber = XMLSerialize.ALIAS_INIT;
        aliasHash.clear();
    }

    /**--------------------------------------------------------------------------
		* getAlias
		* --------
		* Much of this is duplicated code from addAttr().  This should be
		*	rationalized and refactored at some point.
		**/
    boolean aliasNameMissing;

    private NamedObject getAlias(ParserXML.Attr[] addAttrTmp, String flag, Object parent) throws IOException {
        aliasNameMissing = false;
        ParserXML.Attr attr = addAttrTmp[0];
        String fieldName = null;
        String aliasName = null;
        if (flag.equals(XMLSerialize.ALIAS_TAG_TOKEN)) {
            if (!attr.isEnd) {
                do {
                    if (attr.name.equals(XMLSerialize.NAME_TOKEN)) {
                        if (fieldName != null) throw new ParserXML.ExceptionXML("just one name field");
                        fieldName = attr.value;
                    } else if (attr.name.equals(XMLSerialize.ALIAS_ATTR_TOKEN)) {
                        if (aliasName != null) throw new ParserXML.ExceptionXML("just one alias name");
                        aliasName = attr.value;
                    } else throw new ParserXML.ExceptionXML("unknown attribute: " + attr.name);
                } while (!(attr = p.readAttr()).isEnd);
            }
            if (!attr.emptyTag) {
                ParserXML.Tag closeTag = p.readTag();
                if (!closeTag.name.equals(XMLSerialize.ALIAS_TAG_TOKEN)) throw new ParserXML.ExceptionXML(XMLSerialize.ALIAS_TAG_TOKEN, closeTag.name);
            }
            Object me = aliasHash.get(aliasName);
            if (me == null) {
                throw new ParserXML.ExceptionXML("alias target '" + aliasName + "' not found");
            }
            if (parent != null) {
                if (fieldName == null) aliasNameMissing = true; else {
                    currentGetField.putObj(fieldName, me);
                    Field f = getAllField(parent.getClass(), fieldName);
                    setFinal(f, parent, me);
                }
            }
            return new NamedObject(fieldName, me, !aliasNameMissing);
        } else return null;
    }

    private static void setFinal(Field f, Object parent, Object value) {
        if (f == null) return;
        try {
            if (FINAL_DEBUG) {
                System.err.println("setFinal");
                System.err.println(f);
                System.err.println(parent);
                System.err.println(value);
            }
            if (Modifier.isFinal(f.getModifiers())) {
                ObjectStreamField osf = new ObjectStreamField(f.getName(), f.getType());
                Field osfField = ObjectStreamField.class.getDeclaredField("field");
                osfField.setAccessible(true);
                osfField.set(osf, f);
                if (FINAL_DEBUG) {
                    System.err.println(osf);
                    System.err.println("about to set...");
                }
                if (f.getType().isPrimitive()) {
                    MagicClass.setPrimitiveFieldValues(parent, osf, value);
                    if (FINAL_DEBUG) System.err.println("...set primitive final field!");
                } else {
                    MagicClass.setObjectFieldValue(parent, osf, f.getType(), value);
                    if (FINAL_DEBUG) System.err.println("...set object!");
                }
            } else {
                f.set(parent, value);
            }
            if (FINAL_DEBUG) System.err.println("finished setFinal");
        } catch (IllegalAccessException e) {
        } catch (NoSuchFieldException e) {
        }
    }

    /** ------------------------------------------------------------------------
		* putAlias
		* --------
		* Change to return the alias - this should be a "back-compatible
		* refactoring" for callers (tho all return statements need to be changed),
		* and it allows it to be stored, and used to later update it (for
		* readResolve()).
		**/
    private String putAlias(Object o) throws ParserXML.ExceptionXML {
        if (o == null) return null;
        String alias;
        if (cfg.aliasID) {
            alias = p.getAlias().value;
        } else {
            ++aliasSerialNumber;
            alias = aliasSerialNumber + "";
            try {
                p.getAlias();
            } catch (ParserXML.ExceptionXML e) {
            }
        }
        aliasHash.put(alias, o);
        return alias;
    }

    /** ------------------------------------------------------------------------
		* updateAlias
		* -----------
		* for readResolve() - easy to implement, because Hashtable already
		* allows for updates
		**/
    private String updateAlias(Object o, String alias) throws IOException {
        if (o == null) return null;
        if (aliasHash.put(alias, o) == null) throw new IOException("update called, but no existing alias");
        return alias;
    }

    private ObjIn oisSubclass;

    static final Class[] OIS_ARGS = { ObjectInputStream.class };

    Object[] readObjectArglist;

    /**--------------------------------------------------------------------------
		* setArg()
		* -------
		* Consider: putting this in constructor; or caller in same package or same
		* class.
		**/
    void setArg(Object[] argList) {
        readObjectArglist = argList;
        oisSubclass = (ObjIn) argList[0];
    }

    Vector primStore = null;

    int primStoreIndex = -1;

    /**--------------------------------------------------------------------------
		* getPrimString()
		* ---------------
		* Called by readInt(), readFloat(), etc to get the String that was
		* stored by addAttr
		**/
    String getPrimString() throws IOException {
        if (superVersion) {
            defROSubstitute();
            return deserializePrimOptData();
        } else {
            return (String) primStore.get(primStoreIndex++);
        }
    }

    private String deserializePrimOptData() throws IOException {
        ParserXML.Tag tag = p.readTag();
        checkOpenTag(XMLSerialize.OPT_PRIM_DATA_TOKEN, tag);
        ParserXML.Attr attr;
        String value = null;
        while (!(attr = p.readAttr()).isEnd) {
            if (attr.name.equals(XMLSerialize.VALUE_TOKEN)) value = attr.value;
        }
        if (value == null) throw new ParserXML.ExceptionXML("Expected: " + XMLSerialize.VALUE_TOKEN);
        if (!attr.emptyTag) checkCloseTag(XMLSerialize.OPT_PRIM_DATA_TOKEN, p.readTag());
        return value;
    }

    Vector objStore = new Vector();

    int objStoreIndex;

    /**--------------------------------------------------------------------------
		* getObj()
		* --------
		* Called by readObject(), when it is a from a customization (not the
		* top-level call).  The Vector is populated by createObject.
		**/
    Object getObj() {
        return objStore.get(objStoreIndex++);
    }

    /**--------------------------------------------------------------------------
		* NamedObject - message object (to return more than one thing)
		* -----------
		* Defn: Wraps an "object" and its (field)"name".
		* Used by: createObject. null, alias, array etc routines return it.
		*
		* Future: We return a message object.  Object creation is inefficient.
		* Reuse a static object - a global variable with cleaner semantics (usage).
		* That is, set global/static object fields, and return it.  Problem is that
		* not restricted to this use - others could still access it as global -
		* unless the orginator of it was in a separate class.  An advantage of
		* small modules.
		**/
    class NamedObject {

        String name;

        Object object;

        boolean named;

        NamedObject(String name, Object object, boolean named) {
            this.name = name;
            this.object = object;
            this.named = named;
        }
    }

    private Object createObject(ParserXML.Tag tag, Object parent) throws IOException, ClassNotFoundException {
        ParserXML.Attr[] addAttrTmp = addName();
        return createObject(tag, addAttrTmp, parent);
    }

    private Object createObject(ParserXML.Tag tag, ParserXML.Attr[] addAttrTmp, Object parent) throws IOException, ClassNotFoundException {
        String elementName = tag.name;
        if (KELLY_DEBUG) System.err.println("adding " + elementName);
        Object me = parent;
        boolean nameMissing = false;
        NamedObject namedObject;
        if (elementName.equals(XMLSerialize.NULL_TOKEN)) {
            namedObject = deserializeNull(addAttrTmp, parent);
            me = namedObject.object;
            nameMissing = !namedObject.named;
            if (nameMissing) {
                if (parent != null) {
                    if (parent.getClass() != Vector.class && parent.getClass() != Hashtable.class && !parent.getClass().isArray()) {
                        objStore.add(me);
                    }
                }
            }
            return me;
        } else if (elementName.startsWith(XMLSerialize.ARRAY_TOKEN)) {
            namedObject = createArray(addAttrTmp, elementName, parent);
            me = namedObject.object;
            nameMissing = !namedObject.named;
            if (nameMissing) {
                if (parent != null) {
                    if (parent != null && parent.getClass() != Hashtable.class && parent.getClass() != Vector.class && !parent.getClass().isArray()) {
                        objStore.add(me);
                    }
                }
            }
            return me;
        } else if ((namedObject = getAlias(addAttrTmp, elementName, parent)) != null) {
            me = namedObject.object;
            nameMissing = !namedObject.named;
            if (nameMissing) {
                if (parent != null) {
                    if (parent.getClass() != Vector.class && parent.getClass() != Hashtable.class && !parent.getClass().isArray()) {
                        objStore.add(me);
                    }
                }
            }
            return me;
        } else if (elementName.equals(XMLSerialize.BINARY_DATA_TOKEN)) {
            namedObject = deserializeBinary(addAttrTmp, tag, parent);
            nameMissing = !namedObject.named;
            me = namedObject.object;
            if (parent != null) {
                if (nameMissing) {
                    if (parent.getClass() != Vector.class && parent.getClass() != Hashtable.class && !parent.getClass().isArray()) {
                        objStore.add(me);
                    }
                }
            }
            return me;
        } else if (elementName.equals("java.lang.String")) {
            namedObject = deserializeWrapper(addAttrTmp, tag, parent);
            nameMissing = !namedObject.named;
            me = namedObject.object;
            if (parent != null) {
                if (nameMissing) {
                    if (parent.getClass() != Vector.class && parent.getClass() != Hashtable.class && !parent.getClass().isArray()) {
                        objStore.add(me);
                    }
                }
            }
            return me;
        } else if (elementName.equals("java.lang.Class")) {
            if (DEBUG_CLASS) System.err.println("Got a Class tag");
            namedObject = deserializeClass(addAttrTmp, tag, parent);
            nameMissing = !namedObject.named;
            me = namedObject.object;
            if (parent != null) {
                if (nameMissing) {
                    if (parent.getClass() != Vector.class && parent.getClass() != Hashtable.class && !parent.getClass().isArray()) {
                        objStore.add(me);
                    }
                }
            }
            if (DEBUG_CLASS) {
                System.err.println(">>>>start>>>>>");
                System.err.println(me);
                try {
                    System.err.println(((Class) me).newInstance());
                    new JSX.ObjOut(System.err).writeObject(((Class) me).newInstance());
                } catch (InstantiationException e) {
                    System.err.println(e);
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    System.err.println("Should never get here - it means that we couldn't create a Hashtable with the no-arg constructor");
                    e.printStackTrace();
                }
                System.err.println("<<<<end<<<<");
            }
            return me;
        } else if (elementName.equals("java.util.Vector")) {
            Class clazz = Class.forName(ParseUtilities.descapeDollar(elementName));
            me = null;
            try {
                me = clazz.newInstance();
            } catch (InstantiationException e) {
                if (DEBUG) {
                    System.err.println("Should never get here - it means that we couldn't create a Hashtable with the no-arg constructor");
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (DEBUG) {
                    System.err.println("Should never get here - it means that we couldn't create a Hashtable with the no-arg constructor");
                    e.printStackTrace();
                }
            }
            ParserXML.Attr name = null;
            name = addPrim(addAttrTmp, me, false);
            nameMissing = name.nameMissing;
            if (parent != null) {
                if (nameMissing) {
                    if (parent.getClass() != Vector.class && parent.getClass() != Hashtable.class && !parent.getClass().isArray()) {
                        objStore.add(me);
                    }
                } else {
                    currentGetField.putObj(name.value, me);
                    Field f = getAllField(parent.getClass(), name.value);
                    setFinal(f, parent, me);
                }
            }
            putAlias(me);
            if (!name.emptyTag) {
                addVectorElements((Vector) me);
            }
            return me;
        } else if (elementName.equals("java.util.Hashtable")) {
            Class clazz = Class.forName(ParseUtilities.descapeDollar(elementName));
            me = null;
            try {
                me = clazz.newInstance();
            } catch (InstantiationException e) {
                if (DEBUG) {
                    System.err.println("Should never get here - it means that we couldn't create a Hashtable with the no-arg constructor");
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (DEBUG) {
                    System.err.println("Should never get here - it means that we couldn't create a Hashtable with the no-arg constructor");
                    e.printStackTrace();
                }
            }
            ParserXML.Attr name = null;
            name = addPrim(addAttrTmp, me, false);
            nameMissing = name.nameMissing;
            if (parent != null) {
                if (nameMissing) {
                    if (parent.getClass() != Vector.class && parent.getClass() != Hashtable.class && !parent.getClass().isArray()) {
                        objStore.add(me);
                    }
                } else {
                    currentGetField.putObj(name.value, me);
                    Field f = getAllField(parent.getClass(), name.value);
                    setFinal(f, parent, me);
                }
            }
            putAlias(me);
            if (!name.emptyTag) {
                addHashtableElements((Hashtable) me);
            }
            return me;
        } else {
            ParserXML.Attr name = addAttrTmp[1];
            boolean subclass = false;
            if (name != null && name.value.equals(XMLSerialize.SUPER_TOKEN)) {
                subclass = true;
            }
            String alias = null;
            Class meClassFrame;
            if (!subclass) {
                Class clazz = null;
                String escapedName = ParseUtilities.descapeDollar(elementName);
                ObjectStreamClass osc = getOsc(escapedName);
                clazz = oisSubclass.resolveClass(osc);
                if (clazz == null) throw new ClassNotFoundException("Could not load " + escapedName);
                me = null;
                try {
                    if (Externalizable.class.isAssignableFrom(clazz)) {
                        try {
                            Constructor cons = clazz.getConstructor(new Class[0]);
                            me = cons.newInstance(new Object[0]);
                        } catch (NoSuchMethodException e) {
                            throw new InvalidClassException("Missing public no-arg constructor for class " + clazz.getName());
                        } catch (InstantiationException e) {
                            throw new InvalidClassException("An interface or abstract class " + clazz.getName());
                        }
                    } else {
                        me = MagicClass.newInstance(clazz);
                        if (me == null) throw new ClassNotFoundException("MagicClass.newInstance() failed to create a " + clazz + ", from the name " + escapedName);
                    }
                } catch (InvocationTargetException e) {
                    Throwable thrown = e.getTargetException();
                    if (thrown instanceof ClassNotFoundException) throw (ClassNotFoundException) thrown; else if (thrown instanceof IOException) throw (IOException) thrown; else if (thrown instanceof RuntimeException) throw (RuntimeException) thrown; else {
                        System.err.println("\nWrapper Exception:");
                        e.printStackTrace();
                        System.err.println("\nException wrapped up:");
                        thrown.printStackTrace();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (IBM_DEBUG) System.err.println("IBM: me is " + (me == null ? "null" : "not null"));
                if (KELLY_DEBUG) {
                    if (me.getClass() == Class.forName("java.awt.Container")) {
                        System.err.println("Got a java.awt.Container");
                    }
                }
                if (DEBUG) System.err.println("Made class \"" + elementName + "\"");
                alias = putAlias(me);
                meClassFrame = clazz;
                if (superVersion) {
                    lookahead.set(tag, addAttrTmp);
                    f(me);
                }
            } else {
                meClassFrame = Class.forName(ParseUtilities.descapeDollar(elementName));
            }
            if (!superVersion) {
                Vector localStackPrimStore = primStore;
                int localStackPrimStoreIndex = primStoreIndex;
                primStore = new Vector();
                primStoreIndex = 0;
                name = addPrim(addAttrTmp, me, false);
                nameMissing = name.nameMissing;
                if (DEBUG && name != null) System.err.println("Name: " + name);
                if (DEBUG) System.err.println("nameMissing: " + nameMissing);
                if (INTERNAL_DEBUG) {
                    if (primStore != null) {
                        for (Enumeration keys = primStore.elements(); keys.hasMoreElements(); ) {
                            Object a = keys.nextElement();
                            System.err.println("In primStore: " + a + " -- " + a.getClass() + " -- " + a.hashCode());
                        }
                    }
                }
                Vector localStackObjStore = objStore;
                objStore = new Vector();
                int localStackObjStoreIndex = objStoreIndex;
                objStoreIndex = 0;
                Object localMeStore = meStore;
                meStore = me;
                Class localMeClassFrameStore = meClassFrameStore;
                meClassFrameStore = meClassFrame;
                boolean localEmptyTagStore = emptyTagStore;
                emptyTagStore = name.emptyTag;
                boolean localDefaultCalledStore = defaultCalledStore;
                defaultCalledStore = false;
                Method m;
                if (me instanceof Externalizable) {
                    ((Externalizable) me).readExternal(oisSubclass);
                } else {
                    Vector classes = new Vector();
                    Class[] superClasses = XMLSerialize.getReversedSuperClasses(me.getClass());
                    if (SUPER_DEBUG) System.err.println(me.getClass().getName() + " " + (!nameMissing ? name.value : "<unnamed>") + ": superClasses.length = " + superClasses.length);
                    for (int j = 0; j < superClasses.length; j++) {
                        Class c = superClasses[j];
                        if ((m = XMLSerialize.getDeclaredMethod(c, "readObject", OIS_ARGS, Modifier.PRIVATE, Modifier.STATIC)) != null) {
                            if (INTERNAL_DEBUG) System.err.print("\t--JSX deserialize--");
                            try {
                                m.invoke(me, readObjectArglist);
                                if (INTERNAL_DEBUG) System.err.println("\tinvoke " + m + " on " + me + " of " + me.getClass());
                            } catch (InvocationTargetException e) {
                                if (DEBUG) {
                                    System.err.println("JSX InvocationTargetException:");
                                    System.err.println("Object which is a: " + me.getClass());
                                    e.printStackTrace();
                                }
                                Throwable t = e.getTargetException();
                                if (t instanceof ClassNotFoundException) throw (ClassNotFoundException) t; else if (t instanceof IOException) throw (IOException) t; else if (t instanceof RuntimeException) throw (RuntimeException) t; else if (t instanceof Error) throw (Error) t; else throw new Error("interal error");
                            } catch (IllegalAccessException e) {
                                defaultReadObject();
                            }
                        } else {
                            defaultReadObject();
                        }
                    }
                    if (superClasses.length == 0) defaultReadObject();
                }
                if (!subclass) {
                    if (NOV_DEBUG) System.err.println("readResolve is called!!!!!!!!!!!");
                    me = readResolve(me, alias);
                }
                defaultCalledStore = localDefaultCalledStore;
                meStore = localMeStore;
                meClassFrameStore = localMeClassFrameStore;
                emptyTagStore = localEmptyTagStore;
                objStoreIndex = localStackObjStoreIndex;
                objStore = localStackObjStore;
                primStoreIndex = localStackPrimStoreIndex;
                primStore = localStackPrimStore;
            }
            if (superVersion) {
                if (!subclass) {
                    if (NOV_DEBUG) System.err.println("readResolve is called!!!!!!!!!!!");
                    me = readResolve(me, alias);
                }
            }
            if (!subclass) {
                if (parent != null) {
                    if (nameMissing) {
                        if (parent.getClass() != Vector.class && parent.getClass() != Hashtable.class && !parent.getClass().isArray()) {
                            objStore.add(me);
                            if (INTERNAL_DEBUG) System.err.println("custom obj[" + objStoreIndex + "]," + objStore);
                        }
                    } else {
                        if (DEBUG_CUSTOM) {
                            System.err.println("getting field '" + name.value + "'");
                        }
                        currentGetField.putObj(name.value, me);
                        Field f = getAllField(parent.getClass(), name.value);
                        setFinal(f, parent, me);
                    }
                }
            }
            return me;
        }
    }

    /** static to ensure self-contained.  Separate out, so easy to reuse, as there
	* are two points that need Class.forName(), and need to send it with this
	* instead.
	**/
    private static ObjectStreamClass getOsc(String escapedName) throws ClassNotFoundException, IOException {
        ObjectStreamClass osc = null;
        try {
            osc = (ObjectStreamClass) MagicClass.newInstance(ObjectStreamClass.class);
            Field oscNameField = ObjectStreamClass.class.getDeclaredField("name");
            oscNameField.setAccessible(true);
            setFinal(oscNameField, osc, escapedName);
        } catch (InvocationTargetException e) {
            Throwable thrown = e.getTargetException();
            if (thrown instanceof ClassNotFoundException) throw (ClassNotFoundException) thrown; else if (thrown instanceof IOException) throw (IOException) thrown; else {
                System.err.println("\nWrapper Exception:");
                e.printStackTrace();
                System.err.println("\nException wrapped up:");
                thrown.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return osc;
    }

    void f(Object me) throws IOException, ParserXML.ExceptionXML, ClassNotFoundException {
        String openTagName = lookahead.tag.name;
        ParserXML.Attr[] addAttrTmp = { lookahead.attr, lookahead.name };
        addPrim(addAttrTmp, me, false);
        lookahead.set(p.readTag(), addName());
        if (lookahead.tag.start && lookahead.name.value.equals(XMLSerialize.SUPER_TOKEN)) nestedSubclass(me); else throw new ParserXML.ExceptionXML("subclass openTag", lookahead.tag.name);
        checkCloseTag(openTagName, lookahead.tag);
    }

    private void eatClose(String expectedName) throws IOException, ParserXML.ExceptionXML {
        checkCloseTag(expectedName, lookahead.tag);
        advance();
    }

    private void eatOpen(String tagName, String objName) throws IOException, ParserXML.ExceptionXML {
        if (tagName != null) checkOpenTag(tagName, lookahead.tag);
        if (objName != null) checkObjName(objName, lookahead.name);
        advance();
    }

    private void checkCloseTag(String expectedName, ParserXML.Tag tag) throws ParserXML.ExceptionXML {
        if (tag.start) throw new ParserXML.ExceptionXML("Wanted a close tag, got an open tag", "</" + expectedName + ">", "<" + tag.name + ">"); else if (!tag.name.equals(expectedName)) throw new ParserXML.ExceptionXML("</" + expectedName + ">", "</" + tag.name + ">");
    }

    private void checkOpenTag(String expectedName, ParserXML.Tag tag) throws ParserXML.ExceptionXML {
        if (!tag.start) throw new ParserXML.ExceptionXML("Wanted an open tag, got a close tag", "</" + expectedName + ">", "<" + tag.name + ">"); else if (!tag.name.equals(expectedName)) throw new ParserXML.ExceptionXML("</" + expectedName + ">", "</" + tag.name + ">");
    }

    private void checkObjName(String expectedName, ParserXML.Attr name) throws ParserXML.ExceptionXML {
        if (name == null) throw new ParserXML.ExceptionXML("Expected a name, but none found"); else if (!name.value.equals(expectedName)) throw new ParserXML.ExceptionXML("</" + expectedName + ">", "</" + name.value + ">");
    }

    /** ---------------------------------------------------------------------------
	* nestedSubclass
	* --------------
	*	F -> C
	*	C -> F*[O][C]
	*	O -> F*

	*		C -> r[C]		//C - nestedSubclass()
	*		r -> (d|s)	//r - generalReadObject(me);
	*		d -> defRO[O]	//d - defaultReadObject()
	*		defRO -> F*
	*		s -> [defRO][O]	//s - readObject().
	* 	O -> (p|q)*			//this mixes the levels - we need to read defRO and O
	*		p -> [defRO]
	*			I'm very lost here - defer it.  This is a hard bit...

	* NOTATION: uppercase non-terminals are elements, which contain their RHS
  *   FIRST(C) = { <C> }
			Therefore, the very first token is always an open tag, followed by:
  *   FIRST(C) = { FIRST(r),FIRST(n),</C> }
  *   FIRST(r) = { <F>,<O>,e }
  *   FIRST(n) = { <C>,e }

	* C() {	//FIRST(C) = {<C>};
			eatOpen(<C>);
				r();
				if (<C>) C();		//n()
			eatClose(</C>);
		}

	* Lookahead interaction with other routines:
	*   (1). addAttrTmp - is this argument a kind of lookahead?
	*   (2). tag and addAttrTmp - these are used as if they were "lookahead".
	* However, where are these symbols eaten?  If checking them did advance
	* the input stream, then maybe we wouldn't need the explicit readTag()?

	* "Refactoring is crafting" it to be beautiful, and really understanding it,
	* and bring light, wisdom and clarity to it.

	**/
    private void nestedSubclass(Object me) throws IOException, ClassNotFoundException {
        String frameName = lookahead.tag.name;
        generalReadObject(me);
        if (lookahead.tag.start && lookahead.name.value.equals(XMLSerialize.SUPER_TOKEN)) {
            nestedSubclass(me);
        }
        eatClose(frameName);
    }

    final Lookahead lookahead = new Lookahead();

    static class Lookahead {

        ParserXML.Tag tag;

        ParserXML.Attr attr;

        ParserXML.Attr name;

        void set(ParserXML.Tag tag, ParserXML.Attr attr, ParserXML.Attr name) {
            this.tag = tag;
            this.attr = attr;
            this.name = name;
        }

        void set(ParserXML.Tag tag, ParserXML.Attr[] addAttrTmp) {
            this.tag = tag;
            if (addAttrTmp != null) {
                this.attr = addAttrTmp[0];
                this.name = addAttrTmp[1];
            }
        }
    }

    static class Stack {

        Vector v = new Vector();

        void push(Object o) {
            v.add(o);
        }

        void set(Object o) {
            v.set(v.size() - 1, o);
        }

        Object get() {
            return v.get(v.size() - 1);
        }

        Object pop() {
            return v.remove(v.size() - 1);
        }
    }

    static class TagStack {

        Stack s = new Stack();

        void push(ParserXML.Tag tag) {
            System.err.println("push(" + tag + ")");
            s.push(tag);
        }

        void set(ParserXML.Tag tag) {
            System.err.println("\tset(" + tag + ")");
            s.set(tag);
        }

        ParserXML.Tag get() {
            return (ParserXML.Tag) s.get();
        }

        ParserXML.Tag pop() {
            System.err.println("pop = " + get());
            return (ParserXML.Tag) s.pop();
        }
    }

    static class BooleanStack {

        Stack s = new Stack();

        void push(boolean bool) {
            s.push(bool ? Boolean.TRUE : Boolean.FALSE);
        }

        void set(boolean bool) {
            s.set(bool ? Boolean.TRUE : Boolean.FALSE);
        }

        boolean get() {
            return ((Boolean) s.get()).booleanValue();
        }

        boolean pop() {
            return ((Boolean) s.pop()).booleanValue();
        }
    }

    TagStack defROStack = new TagStack();

    BooleanStack defROCalledStack = new BooleanStack();

    void generalReadObject(Object me) throws IOException, ClassNotFoundException {
        Class meClassFrame = Class.forName(ParseUtilities.descapeDollar(lookahead.tag.name));
        ParserXML.Attr[] addAttrTmp = { lookahead.attr, lookahead.name };
        defaultReadObject_attrs_in = addAttrTmp;
        defaultReadObject_me = me;
        defROStack.push(null);
        defROCalledStack.push(false);
        Method readObjectMethod = XMLSerialize.getDeclaredMethod(meClassFrame, "readObject", OIS_ARGS, Modifier.PRIVATE, Modifier.STATIC);
        if (readObjectMethod == null) {
            d();
        } else {
            System.err.println(" ----- s() is called");
            s(me, readObjectMethod);
        }
        defROStack.pop();
        defROCalledStack.pop();
    }

    /**
	* This is d():
	* d     -> defRO[O]
	* O     -> e				//assume empty (could be F*)
	* 
	* defRO -> F*
	*
	**/
    void d() throws ParserXML.ExceptionXML, IOException, ClassNotFoundException {
        defaultReadObject_ver2_from_d();
        if (lookahead.tag.start && lookahead.tag.name.equals(XMLSerialize.OPT_DATA_TOKEN)) {
            eatOpen(XMLSerialize.OPT_DATA_TOKEN, null);
            eatClose(XMLSerialize.OPT_DATA_TOKEN);
        }
    }

    /**
	* This is s():
	* s     -> defRO[O]
	**/
    void s(Object me, Method readObjectMethod) throws ParserXML.ExceptionXML, IOException, ClassNotFoundException {
        invokeReadObject(me, readObjectMethod);
        if (!defROCalledStack.get()) {
            System.err.println("calling defRO from s()");
            defaultReadObject_ver2_from_s();
        }
        ParserXML.Tag earlierOpt = defROStack.get();
        if (earlierOpt.start && earlierOpt.name.equals(XMLSerialize.OPT_DATA_TOKEN)) {
            advance();
            if (!earlierOpt.name.equals(lookahead.tag.name)) {
                System.err.println("earlier: " + earlierOpt);
                System.err.println("lookahead: " + lookahead.tag);
                System.exit(0);
            }
            System.err.println("reading <opt> in s()");
            eatClose(XMLSerialize.OPT_DATA_TOKEN);
        }
    }

    void invokeReadObject(Object me, Method readObjectMethod) throws ClassNotFoundException, IOException {
        if (NOV_DEBUG) System.err.println("using correct class frame now (I believe)");
        if (NESTEDREADOBJECT_DEBUG) System.err.print("\t--JSX deserialize--");
        try {
            readObjectMethod.invoke(me, readObjectArglist);
            if (NESTEDREADOBJECT_DEBUG) System.err.println("\tinvoke " + readObjectMethod + " on " + me + " of " + me.getClass());
        } catch (InvocationTargetException e) {
            if (DEBUG) {
                System.err.println("JSX InvocationTargetException:");
                System.err.println("Object which is a: " + me.getClass());
                e.printStackTrace();
            }
            Throwable t = e.getTargetException();
            if (t instanceof ClassNotFoundException) throw (ClassNotFoundException) t; else if (t instanceof IOException) throw (IOException) t; else if (t instanceof RuntimeException) throw (RuntimeException) t; else if (t instanceof Error) throw (Error) t; else throw new Error("internal error");
        } catch (IllegalAccessException e) {
            System.err.println("IllegalAccessExcetion - please report this error");
            e.printStackTrace();
        }
    }

    /** ------------------------------------------------------------------------
 	* defaultReadObject
	* -----------------
	* For superVersion, will be called directly from object's readObject()...
	* Switch, according to which version, for back-compatibility.
	**/
    void defaultReadObject() throws IOException, ClassNotFoundException {
        if (superVersion) {
            ;
            defaultReadObject_ver2();
        } else {
            defaultReadObject_ver1();
        }
    }

    /**
	* defaultReadObject_ver2_from_d()
	* -------------------------------
	* Only called from d().
	*
	**/
    void defaultReadObject_ver2_from_d() throws IOException, ClassNotFoundException {
        defROCalledStack.set(true);
        Object me = defaultReadObject_me;
        addPrim(new ParserXML.Attr[] { lookahead.attr, lookahead.name }, me, false);
        advance();
        while (openF(me)) {
            createObject(lookahead.tag, new ParserXML.Attr[] { lookahead.attr, lookahead.name }, me);
            advance();
        }
    }

    /**
	* defaultReadObject_ver2_from_s()
	* ------------------------
	* Only called from s().
	**/
    void defaultReadObject_ver2_from_s() throws IOException, ClassNotFoundException {
        defROCalledStack.set(true);
        Object me = defaultReadObject_me;
        addPrim(defaultReadObject_attrs_in, me, false);
        advance();
        while (openF(me)) {
            createObject(lookahead.tag, new ParserXML.Attr[] { lookahead.attr, lookahead.name }, me);
            advance();
        }
        if (lookahead.tag.start && lookahead.tag.name.equals(XMLSerialize.OPT_DATA_TOKEN)) {
            addPrim(new ParserXML.Attr[] { lookahead.attr, lookahead.name }, me, false);
        }
        defaultReadObject_attrs_out = new ParserXML.Attr[] { lookahead.attr, lookahead.name };
        defROStack.set(lookahead.tag);
    }

    Object defaultReadObject_me;

    ParserXML.Attr[] defaultReadObject_attrs_in;

    ParserXML.Attr[] defaultReadObject_attrs_out;

    void defaultReadObject_ver2() throws IOException, ClassNotFoundException {
        defROCalledStack.set(true);
        Object me = defaultReadObject_me;
        addPrim(defaultReadObject_attrs_in, me, false);
        advance();
        while (openF(me)) {
            createObject(lookahead.tag, new ParserXML.Attr[] { lookahead.attr, lookahead.name }, me);
            advance();
        }
        if (lookahead.tag.start && lookahead.tag.name.equals(XMLSerialize.OPT_DATA_TOKEN)) {
            addPrim(new ParserXML.Attr[] { lookahead.attr, lookahead.name }, me, false);
        }
        defaultReadObject_attrs_out = new ParserXML.Attr[] { lookahead.attr, lookahead.name };
        defROStack.set(lookahead.tag);
    }

    /** -------------------------------------------------------------------------
	* read the next token (tag) into lookahead.  Should be done for every match
	*
	*/
    void advance() throws IOException {
        ParserXML.Tag tag = p.readTag();
        if (tag.start) lookahead.set(tag, addName()); else lookahead.set(tag, null, null);
    }

    /** Check lookahead to see if it is an <F> or not.
	* This is complex: <F> is not an actual tag, but can only be discerned by
	* it *not* being any of the other possible tags.  That is, it is by
	* default.
	**/
    boolean openF(Object me) throws IOException, ClassNotFoundException {
        if (lookahead.tag == null) {
            throw new EOFException("Premature EOF");
        } else if (lookahead.tag.start && lookahead.tag.name.equals(XMLSerialize.OPT_DATA_TOKEN)) {
            return false;
        } else if (lookahead.tag.start && lookahead.name != null && lookahead.name.value.equals(XMLSerialize.SUPER_TOKEN)) {
            return false;
        } else if (lookahead.tag.start) {
            return true;
        } else if (!lookahead.tag.start) {
            return false;
        } else {
            throw new Error("Internal error: Please send the stacktrace and XML " + "input file to bren@mail.csse.monash.edu.au to report this problem");
        }
    }

    boolean defaultCalledStore;

    Object meStore;

    Class meClassFrameStore;

    boolean emptyTagStore;

    void defaultReadObject_ver1() throws IOException, ClassNotFoundException {
        if (!defaultCalledStore) {
            defaultCalledStore = true;
            if (!emptyTagStore) {
                Object me = meStore;
                Class meClassFrame = meClassFrameStore;
                ParserXML.Tag tag;
                while (((tag = p.readTag()) != null) && (tag.start)) {
                    createObject(tag, me);
                }
                if (tag == null) throw new EOFException("Premature EOF"); else {
                    String className = meClassFrame.getName();
                    String rightTag = ParseUtilities.escapeDollar(className);
                    String closeTag = tag.name;
                    if (cfg != null && cfg.refactor != null) closeTag = cfg.refactor.mapClassname(closeTag);
                    if (!closeTag.equals(rightTag)) throw new ParserXML.ExceptionXML("</" + rightTag + ">", "</" + closeTag + ">");
                }
            }
        }
    }

    /** ---------------------------------------------------------------------------
	* readResolve
	* -----------
	* Reflectively invoke the present objects's readResolve().
	* Return value can be: an object or an exception.  We are using an 
	* Exception to flag the result, instead of trying to encode it in the
	* return value itself (by wrapping it, for example - which is sort of how
	* Exceptions work, anyway.
	* QUESTION: If we checked for it separately, we can choose whether to cache
	* or not...
	* "parameterless" = no-arg
	**/
    Object readResolve(Object me, String alias) throws ObjectStreamException {
        if (READRESOLVE_DEBUG) System.err.println("entered readResolve, with " + me + " and alias=" + alias);
        try {
            Method readResolveMethod = null;
            try {
                readResolveMethod = me.getClass().getDeclaredMethod("readResolve", new Class[] {});
            } catch (NoSuchMethodException e) {
                if (READRESOLVE_DEBUG) System.err.println("No readResolve() found - checking super");
                Class clazz = me.getClass();
                while (clazz != Object.class) {
                    try {
                        clazz = clazz.getSuperclass();
                        readResolveMethod = clazz.getDeclaredMethod("readResolve", new Class[] {});
                        break;
                    } catch (NoSuchMethodException continueLoop) {
                    }
                }
                if (readResolveMethod == null) return me;
                if (!checkSuperMethodAccess(readResolveMethod, me.getClass())) return me;
            }
            readResolveMethod.setAccessible(true);
            Object newme = readResolveMethod.invoke(me, new Object[] {});
            updateAlias(newme, alias);
            if (READRESOLVE_DEBUG) System.err.println("readResolve returned " + newme);
            return newme;
        } catch (InvocationTargetException e) {
            System.err.println("JSX InvocationTargetException:");
            try {
                throw (ObjectStreamException) e.getTargetException();
            } catch (ClassCastException f) {
                throw new Error("readResolve() of " + me.getClass() + " threw an '" + e.getTargetException() + "'. It should only be of type ObjectStreamException");
            }
        } catch (Exception e) {
            System.err.println("not an InvocationTargetException");
            e.printStackTrace();
        }
        return null;
    }

    private static boolean checkSuperMethodAccess(Method scMethod, Class ofClass) {
        if (scMethod == null) {
            return false;
        }
        int supermods = scMethod.getModifiers();
        if (Modifier.isPublic(supermods) || Modifier.isProtected(supermods)) {
            return true;
        } else if (Modifier.isPrivate(supermods)) {
            return false;
        } else {
            return isSameClassPackage(scMethod.getDeclaringClass(), ofClass);
        }
    }

    private static boolean isSameClassPackage(Class cl1, Class cl2) {
        if (cl1.getClassLoader() != cl2.getClassLoader()) {
            return false;
        } else {
            String clName1 = cl1.getName();
            String clName2 = cl2.getName();
            int idx1 = clName1.lastIndexOf('.');
            int idx2 = clName2.lastIndexOf('.');
            if (idx1 == -1 || idx2 == -1) {
                return idx1 == idx2;
            } else {
                return clName1.regionMatches(false, 0, clName2, 0, idx1 - 1);
            }
        }
    }

    private NamedObject deserializeNull(ParserXML.Attr[] addAttrTmp, Object parent) throws ClassNotFoundException, NotActiveException, IOException {
        String name = null;
        Object me = null;
        boolean named = false;
        ParserXML.Attr firstAttr = addPrim(addAttrTmp, me, false);
        if (!firstAttr.nameMissing) {
            named = true;
            name = firstAttr.value;
        }
        if (!firstAttr.emptyTag) {
            ParserXML.Tag tag = p.readTag();
            if (tag == null) throw new EOFException("Premature EOF"); else if (tag.start) throw new ParserXML.ExceptionXML("</" + XMLSerialize.NULL_TOKEN + ">", "<" + tag.name + ">"); else if (!tag.name.equals(XMLSerialize.NULL_TOKEN)) {
                throw new ParserXML.ExceptionXML("</" + XMLSerialize.NULL_TOKEN + ">", "</" + tag.name + ">");
            }
        }
        if (parent != null) {
            if (named) {
                if (name == null) {
                    throw new IOException("name: No " + XMLSerialize.NAME_TOKEN + " attribute found for " + parent.getClass());
                }
                currentGetField.putObj(name, me);
                Field f = getAllField(parent.getClass(), name);
                setFinal(f, parent, me);
                return new NamedObject(name, me, named);
            } else {
                return new NamedObject(name, me, named);
            }
        } else {
            return new NamedObject(name, me, named);
        }
    }

    /**--------------------------------------------------------------------------t
		* getAllField()
		* -----------
		* Named in accord with the JSX serialization method "getAllFields()".
		* Returns private and superclass fields.

		* If NoSuchFieldException, we call NoSuchFieldHander... and re-try just
		* *once* again:
		*	If NoSuchFieldException,
		*	  if handler: get the new field and try once more.
		*	    if another NoSuchFieldException, then fail noisily.
		*	  else (ie no-handler): fail silently.
		*	Thus, adding the handler is switches on debug exceptions (ie non-silent)
		*
		**/
    private Field getAllField(Class c, String fieldName) throws SecurityException {
        Field field = null;
        try {
            field = attemptGetAllField(c, fieldName);
        } catch (NoSuchFieldException firstE) {
            if (cfg.noSuchFieldHandler != null) {
                if (CFG_DEBUG) System.err.println("cfg = " + cfg);
                String mappedFieldName = null;
                try {
                    mappedFieldName = cfg.noSuchFieldHandler.noSuchField(c, fieldName);
                } catch (NoSuchFieldException secondE) {
                    throw new IllegalArgumentException(secondE + "");
                }
                try {
                    field = attemptGetAllField(c, mappedFieldName);
                } catch (NoSuchFieldException secondE) {
                    throw new IllegalArgumentException("Mapped '" + fieldName + "'" + " to '" + mappedFieldName + "'" + ", but neither found in " + c + ": " + secondE);
                }
            }
        }
        return field;
    }

    /**--------------------------------------------------------------------------t
		* attemptGetAllField()
		* --------------------
		* Named in accord with the JSX serialization method "getAllFields()".
		* returns private and superclass fields.
		* Called by "getAllFields" above.
		*
		* DANGER: it could find a field from a superclass that customises its
		* own serialization.  This shouldn't happen. It really should check for
		* the readObject() method, and if present, disregard that declared class.
		*
		* We need to parse any class-qualified fields, and we can get these
		* directly from the named class - some unexpected efficiency.
		*
		* NOTE: should not consider any fields that are static and/or transient...
		*
		*/
    private Field attemptGetAllField(Class c, String fieldName) throws NoSuchFieldException, SecurityException {
        if (fieldName == null) throw new NoSuchFieldException("field name had value 'null'");
        Field f = null;
        if ((f = getShadowedField(fieldName)) != null) {
            if ((f.getModifiers() & XMLSerialize.NOT_SERIALIZED) == 0) {
                f.setAccessible(true);
                return f;
            } else throw new NoSuchFieldException(f + ": this field evolved to non-serializeable, it seems.");
        }
        Class initc = c;
        do {
            try {
                f = c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            } catch (Exception e) {
                System.err.println(e + ": No such field '" + fieldName + "' in " + c + " (superclass of " + initc);
            }
        } while (f == null && (c = c.getSuperclass()) != Object.class);
        if (f == null) throw new NoSuchFieldException("JSX can't find the field " + ((fieldName == null) ? "<null>" : "'" + fieldName + "'") + ".  Not in " + initc + ", or private, or in superclasses\n" + "Could it be that the class lacks a readObject() method?");
        if ((f.getModifiers() & XMLSerialize.NOT_SERIALIZED) == 0) {
            f.setAccessible(true);
            return f;
        } else throw new NoSuchFieldException(f + ": this field evolved to non-serializeable, it seems.");
    }

    static Field getShadowedField(String s) throws NoSuchFieldException {
        int dotpos = s.lastIndexOf('.');
        if (dotpos == -1) return null;
        String classQual = s.substring(0, dotpos);
        String fieldName = s.substring(dotpos + 1);
        Class clazz = null;
        try {
            clazz = Class.forName(ParseUtilities.descapeDollar(classQual));
        } catch (ClassNotFoundException e) {
            System.err.println("The class-qualification of this shadowed field was not found: " + s);
            e.printStackTrace();
        }
        return clazz.getDeclaredField(fieldName);
    }

    private NamedObject createArray(ParserXML.Attr[] addAttrTmp, String tagName, Object parent) throws ClassNotFoundException, IOException {
        int length = -1;
        String name = null;
        boolean named = false;
        boolean emptyTag = false;
        ParserXML.Attr firstAttr = addPrim(addAttrTmp, null, true);
        length = firstAttr.length;
        if (!firstAttr.nameMissing) {
            named = true;
            name = firstAttr.value;
        }
        emptyTag = firstAttr.emptyTag;
        int dim = 0;
        int index = 0;
        String tok = XMLSerialize.ARRAY_TOKEN;
        int skip = tok.length();
        while (tagName.startsWith(tok, index)) {
            dim++;
            index += skip;
        }
        if (DEBUG) System.err.println("dim= " + dim);
        String type = tagName.substring(index);
        if (DEBUG) System.err.println("type= " + type);
        Object me = null;
        int[] dimsArray = new int[dim];
        dimsArray[0] = length;
        Class comp;
        if (type.equals("int")) comp = int.class; else if (type.equals("double")) comp = double.class; else if (type.equals("boolean")) comp = boolean.class; else if (type.equals("char")) comp = char.class; else if (type.equals("byte")) comp = byte.class; else if (type.equals("short")) comp = short.class; else if (type.equals("long")) comp = long.class; else if (type.equals("float")) comp = float.class; else {
            if (MIKE_DEBUG) System.err.println("LOGGING: array object component, within the Mike Special");
            ObjectStreamClass osc = getOsc(ParseUtilities.descapeDollar(type));
            comp = oisSubclass.resolveClass(osc);
        }
        me = Array.newInstance(comp, dimsArray);
        putAlias(me);
        if (DEBUG) System.err.println("type= " + me.getClass().getName());
        if (DEBUG) System.err.println("len= " + Array.getLength(me));
        ParserXML.Tag tag;
        ParserXML.Attr secondAttr = null;
        if (dim == 1 && me.getClass().getComponentType().isPrimitive()) {
            addAttrTmp = addName();
            secondAttr = addPrim(addAttrTmp, me, true);
            length = -1;
            if (!secondAttr.nameMissing) {
                if (named) throw new IOException("two names: " + "'" + firstAttr.value + "' and '" + secondAttr.value + "' " + "in " + parent.getClass());
                named = true;
                name = secondAttr.value;
            }
            emptyTag = secondAttr.emptyTag;
            if (DEBUG) System.err.println("after setting primitive array, saw: " + secondAttr);
        } else {
            ParserXML.Attr end = p.readAttr();
            if (DEBUG) System.err.println("after starting reference array, saw: " + end);
            for (int i = 0; i < length; i++) {
                Object obj;
                tag = p.readTag();
                if (tag == null) throw new EOFException("Premature EOF");
                if (!tag.start) throw new IOException("Expected " + length + " elements in array; read only" + i);
                if (tag.name.equals("java.lang.String") || tag.name.equals("java.lang.Integer") || tag.name.equals("java.lang.Double") || tag.name.equals("java.lang.Boolean") || tag.name.equals("java.lang.Character") || tag.name.equals("java.lang.Byte") || tag.name.equals("java.lang.Short") || tag.name.equals("java.lang.Long") || tag.name.equals("java.lang.Float")) obj = deserializeWrapper(tag, null).object; else obj = createObject(tag, null);
                if (DEBUG) System.err.println("Adding {" + obj + "} to Array");
                Array.set(me, i, obj);
            }
        }
        if (!emptyTag) {
            tag = p.readTag();
            if (tag == null) throw new EOFException("Premature EOF");
            if (tag.start) throw new IOException("End of array");
            if (!tag.name.equals(tagName)) throw new ParserXML.ExceptionXML("</" + tagName + ">", "</" + tag.name + ">");
        }
        if (parent != null) {
            if (named) {
                if (name == null) {
                    throw new IOException("name: No " + XMLSerialize.NAME_TOKEN + " attribute found for " + parent.getClass());
                }
                currentGetField.putObj(name, me);
                Field f = getAllField(parent.getClass(), name);
                setFinal(f, parent, me);
                return new NamedObject(name, me, named);
            } else {
                return new NamedObject(name, me, named);
            }
        } else {
            return new NamedObject(name, me, named);
        }
    }

    /**--------------------------------------------------------------------------t
		* addAttr (map to primitives and String)
		* -------
		* Handles: byte, char, short, int, long, float, double and String.
		* As fields, arrays and custom data.
		* NOTE: String can never appear as custom data (can only be written as an
		* object).
		*
		* Consider: BIZARRE: Could do something odd for arrays: take the object
		* only as indicating the exact type of the array, and in here, when read
		* the length, actually create it.  And yet it makes sense in a way.
		*
		* or, return a proper "attrMsg", which includes the length as info.  This
		* is wha should be returned, anyway - not this hacked use of Attr.
		**/
    private ParserXML.Attr addAttr(Object o, boolean isArray) throws IOException, ClassNotFoundException, ParserXML.ExceptionXML {
        ParserXML.Attr[] addAttrTmp = addName();
        return addPrim(addAttrTmp, o, isArray);
    }

    /**--------------------------------------------------------------------------t
		* addName
		* -------
		* Functionality factored out of addAttr, for the sake of super protocol.
		*
		**/
    private ParserXML.Attr[] addName() throws IOException, ParserXML.ExceptionXML {
        ParserXML.Attr attr;
        ParserXML.Attr name = null;
        if (!(attr = p.readAttr()).isEnd) {
            if (DEBUG) System.err.println(attr);
            if (attr.name.equals(XMLSerialize.NAME_TOKEN)) {
                if (name != null) throw new ParserXML.ExceptionXML("just one name field");
                name = attr;
            }
        }
        return new ParserXML.Attr[] { attr, name };
    }

    private ParserXML.Attr addPrim(ParserXML.Attr[] addAttrTmp, Object o, boolean isArray) throws IOException, ClassNotFoundException, ParserXML.ExceptionXML {
        int length = -1;
        if (ADDATTR_NULL_DEBUG) System.err.println("addAttrTmp = " + addAttrTmp);
        ParserXML.Attr attr = addAttrTmp[0];
        ParserXML.Attr name = addAttrTmp[1];
        if (!attr.isEnd) {
            do {
                if (name == attr) continue;
                if (isArray) {
                    if (attr.name.equals(XMLSerialize.LENGTH_TOKEN)) {
                        if (INTERNAL_DEBUG) {
                            System.err.println("ARRAY LENGTH: " + attr);
                        }
                        length = Integer.parseInt(attr.value);
                        break;
                    }
                    if (INTERNAL_DEBUG) {
                        System.err.println("ARRAY PRIMITIVE ATTRIBUTES: " + attr);
                    }
                    String indexStr = attr.name.substring(XMLSerialize.ARRAY_PRIMITIVE_INDEX_TOKEN.length());
                    int index = Integer.parseInt(indexStr);
                    if (DEBUG) System.err.println(attr);
                    Object value = getValue(o.getClass().getComponentType(), attr);
                    Array.set(o, index, value);
                } else {
                    if (attr.name.endsWith(XMLSerialize.ALIAS_STRING_TOKEN)) {
                        if (INTERNAL_DEBUG) {
                            System.err.println("STRING ALIAS: " + attr);
                        }
                        String fieldName = attr.name.substring(0, attr.name.indexOf(XMLSerialize.ALIAS_STRING_TOKEN));
                        Object strObj = aliasHash.get(attr.value);
                        currentGetField.putObj(fieldName, strObj);
                        Field f = getAllField(o.getClass(), fieldName);
                        if (f != null) {
                            setFinal(f, o, strObj);
                        }
                    } else {
                        if (attr.name.startsWith(XMLSerialize.INTERNAL_PRIMITIVE_TOKEN)) {
                            if (INTERNAL_DEBUG) {
                                System.err.println("INTERNAL: " + attr);
                            }
                            String primString = attr.name.substring(XMLSerialize.INTERNAL_PRIMITIVE_TOKEN.length());
                            if (DEBUG) System.err.println(attr);
                            primStore.add(attr.value);
                        } else {
                            if (INTERNAL_DEBUG) {
                                System.err.println("ordinary: " + attr);
                            }
                            if (IBM_DEBUG) System.err.println("IBM: currentGetField = " + currentGetField);
                            if (IBM_DEBUG) System.err.println("IBM: attr = " + attr);
                            currentGetField.putObj(attr.name, attr.value);
                            if (IBM_DEBUG) System.err.println("IBM: o is " + (o == null ? "null" : "not null"));
                            Field f = getAllField(o.getClass(), attr.name);
                            if (f != null) {
                                Class fc = f.getType();
                                if (DEBUG) System.err.println("Made Field \"" + fc.getName() + "\"");
                                setFinal(f, o, getValue(fc, attr));
                            }
                        }
                    }
                }
            } while (!(attr = p.readAttr()).isEnd);
        }
        attr.length = length;
        if (name == null) {
            attr.nameMissing = true;
            return attr;
        }
        name.isEnd = attr.isEnd;
        name.emptyTag = attr.emptyTag;
        name.length = attr.length;
        return name;
    }

    /**--------------------------------------------------------------------------
		* getValue
		* ------------------
		* In: the class of the primitive; the attr (name-value pair)
		* Out: * object representing the primitive
		* Class may come from the "TYPE" field, or from array component type.
		* Used by:
		*		deserializeWrapper
		*		addAttr - note: an intermediate wrapper object is created redundantly
		*
		* It may be more efficient to not reuse this code.
		* when used for an attribute, it creates an Object needlessly;
		* however, such an object is required for Hashtable
		**/
    private Object getValue(Class fc, ParserXML.Attr attr) throws IOException {
        if (DEBUG) System.err.println("getting value of " + fc);
        if (fc.isPrimitive()) {
            if (fc == int.class) return new Integer(attr.value); else if (fc == double.class) return new Double(ParseUtilities.parseDouble(attr.value)); else if (fc == boolean.class) {
                return new Boolean(ParseUtilities.parseBoolean(attr.value));
            } else if (fc == byte.class) return new Byte(attr.value); else if (fc == char.class) {
                String a = ParseUtilities.decodeXML(attr.value);
                if (a.length() != 1) throw new IllegalArgumentException("Character data must be exactly one character long; instead we got: '" + a + "'");
                return new Character(a.charAt(0));
            } else if (fc == short.class) return new Short(attr.value); else if (fc == long.class) return new Long(attr.value); else if (fc == float.class) return new Float(ParseUtilities.parseFloat(attr.value)); else throw new ParserXML.ExceptionXML("Unimplemented primitive: \"" + fc.getName() + "\"");
        } else {
            if (fc == String.class) {
                String s = ParseUtilities.decodeXML(attr.value);
                putAlias(s);
                return s;
            } else throw new ParserXML.ExceptionXML("'" + attr.name + "' is not primitive or String");
        }
    }

    /**--------------------------------------------------------------------------
		* addVectorElements
		* -----------------
		* Note: this should now be possible automatically, since we can read
		* private fields (and any internal serialization)
		* Note: Vector object already created when we get here.
		**/
    private void addVectorElements(Vector v) throws ClassNotFoundException, IOException {
        ParserXML.Tag tag;
        while (true) {
            Object obj;
            tag = p.readTag();
            if (tag == null) throw new EOFException("Premature EOF");
            if (!tag.start) return;
            if (tag.name.equals("java.lang.String") || tag.name.equals("java.lang.Integer") || tag.name.equals("java.lang.Double") || tag.name.equals("java.lang.Boolean") || tag.name.equals("java.lang.Character") || tag.name.equals("java.lang.Byte") || tag.name.equals("java.lang.Short") || tag.name.equals("java.lang.Long") || tag.name.equals("java.lang.Float")) obj = deserializeWrapper(tag, null).object; else obj = createObject(tag, null);
            if (DEBUG) System.err.println("Adding {" + obj + "} to Vector");
            v.add(obj);
        }
    }

    /**--------------------------------------------------------------------------
		* addHashtableElements
		* --------------------
		* An container object.
		* Handles just tags (no attrs)
		**/
    private void addHashtableElements(Hashtable h) throws ClassNotFoundException, IOException {
        ParserXML.Tag tag;
        while (true) {
            Object[] objs = { null, null };
            for (int i = 0; i < objs.length; i++) {
                tag = p.readTag();
                if (tag == null) throw new EOFException("Premature EOF");
                if (!tag.start) if (i == 0) return; else throw new IOException("incomplete Hashtable entry");
                if (tag.name.equals("java.lang.String") || tag.name.equals("java.lang.Integer") || tag.name.equals("java.lang.Double") || tag.name.equals("java.lang.Boolean") || tag.name.equals("java.lang.Character") || tag.name.equals("java.lang.Byte") || tag.name.equals("java.lang.Short") || tag.name.equals("java.lang.Long") || tag.name.equals("java.lang.Float")) objs[i] = deserializeWrapper(tag, null).object; else objs[i] = createObject(tag, null);
            }
            if (DEBUG) System.err.println("Putting {" + objs[0] + ", " + objs[1] + "}");
            h.put(objs[0], objs[1]);
        }
    }

    /**--------------------------------------------------------------------------
		* deserializeClass
		* ----------------
		* Handles the attrs.
		* Need to refactor the test for wrapper type
		* Reads the whole thing in, including <x></x> or <x/>
		**/
    private NamedObject deserializeClass(ParserXML.Attr[] addAttrTmp, ParserXML.Tag startTag, Object cheatParent) throws ClassNotFoundException, IOException {
        ParserXML.Attr attr = addAttrTmp[0];
        ParserXML.Attr end, name = null;
        ParserXML.Tag tag;
        if (DEBUG) System.err.println("deserializeClass: " + attr);
        if (attr.name.equals(XMLSerialize.NAME_TOKEN)) {
            name = attr;
            attr = p.readAttr();
        }
        if (!attr.name.equals(XMLSerialize.CLASSNAME_TOKEN)) throw new ParserXML.ExceptionXML(XMLSerialize.CLASSNAME_TOKEN + " expected");
        if (!(end = p.readAttr()).isEnd) throw new ParserXML.ExceptionXML("\">\" or \"/>\" expected");
        if (!end.emptyTag) {
            tag = p.readTag();
            if (!tag.name.equals(startTag.name) || tag.start) throw new ParserXML.ExceptionXML("</" + tag.name + "> expected");
        }
        if (DEBUG) System.err.println("String read in: " + attr.value);
        String className = ParseUtilities.descapeDollar(attr.value);
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            if (className.equals("int")) clazz = int.class; else if (className.equals("double")) clazz = double.class; else if (className.equals("boolean")) clazz = boolean.class; else if (className.equals("byte")) clazz = byte.class; else if (className.equals("char")) clazz = char.class; else if (className.equals("short")) clazz = short.class; else if (className.equals("long")) clazz = long.class; else if (className.equals("float")) clazz = float.class; else if (className.equals("void")) clazz = void.class; else throw new ClassNotFoundException("class '" + className + "' not found");
        }
        putAlias(clazz);
        if (name != null) {
            currentGetField.putObj(name.value, clazz);
            Field f = getAllField(cheatParent.getClass(), name.value);
            setFinal(f, cheatParent, clazz);
            return new NamedObject(name.value, clazz, true);
        }
        return new NamedObject(null, clazz, false);
    }

    /**--------------------------------------------------------------------------
		* deserializeBinary
		* ----------------
		* Handles the attrs.
		* Need to refactor the test for wrapper type
		* Reads the whole thing in, including <x></x> or <x/>
		**/
    private NamedObject deserializeBinary(ParserXML.Attr[] addAttrTmp, ParserXML.Tag startTag, Object cheatParent) throws ClassNotFoundException, IOException {
        ParserXML.Attr attr = addAttrTmp[0];
        ParserXML.Attr end, name = null;
        ParserXML.Tag tag;
        if (DEBUG) System.err.println("deserializeBinary: " + attr);
        if (attr.name.equals(XMLSerialize.NAME_TOKEN)) {
            name = attr;
            attr = p.readAttr();
        }
        if (!attr.name.equals(XMLSerialize.VALUE_TOKEN)) throw new ParserXML.ExceptionXML(XMLSerialize.VALUE_TOKEN + " expected");
        if (!(end = p.readAttr()).isEnd) throw new ParserXML.ExceptionXML("\">\" or \"/>\" expected");
        if (!end.emptyTag) {
            tag = p.readTag();
            if (!tag.name.equals(startTag.name) || tag.start) throw new ParserXML.ExceptionXML("</" + tag.name + "> expected");
        }
        if (DEBUG) System.err.println("Binary data read in: " + attr.value);
        byte[] binaryData = ParseUtilities.decodeHex(attr.value);
        putAlias(binaryData);
        if (name != null) {
            currentGetField.putObj(name.value, binaryData);
            Field f = getAllField(cheatParent.getClass(), name.value);
            setFinal(f, cheatParent, binaryData);
            return new NamedObject(name.value, binaryData, true);
        }
        return new NamedObject(null, binaryData, false);
    }

    /**--------------------------------------------------------------------------
		* deserializeWrapper
		* ------------------
		* Handles the attrs.
		* Need to refactor the test for wrapper type
		* Reads the whole thing in, including <x></x> or <x/>
		**/
    private NamedObject deserializeWrapper(ParserXML.Tag startTag, Object cheatParent) throws ClassNotFoundException, IOException {
        return deserializeWrapper(addName(), startTag, cheatParent);
    }

    private NamedObject deserializeWrapper(ParserXML.Attr[] addAttrTmp, ParserXML.Tag startTag, Object cheatParent) throws ClassNotFoundException, IOException {
        ParserXML.Attr attr = addAttrTmp[0];
        ParserXML.Attr end, name = null;
        ParserXML.Tag tag;
        if (DEBUG) System.err.println("deserializeWrapper: " + attr);
        if (attr.name.equals(XMLSerialize.NAME_TOKEN)) {
            name = attr;
            attr = p.readAttr();
        }
        if (!attr.name.equals(XMLSerialize.VALUE_TOKEN)) throw new ParserXML.ExceptionXML(XMLSerialize.VALUE_TOKEN + " expected");
        if (!(end = p.readAttr()).isEnd) throw new ParserXML.ExceptionXML("\">\" or \"/>\" expected");
        if (!end.emptyTag) {
            tag = p.readTag();
            if (!tag.name.equals(startTag.name) || tag.start) throw new ParserXML.ExceptionXML("</" + tag.name + "> expected");
        }
        if (DEBUG) System.err.println("String read in: " + attr.value);
        if (startTag.name.equals("java.lang.String")) {
            String s = ParseUtilities.decodeXML((String) attr.value);
            putAlias(s);
            if (name != null) {
                currentGetField.putObj(name.value, s);
                Field f = getAllField(cheatParent.getClass(), name.value);
                setFinal(f, cheatParent, s);
                return new NamedObject(name.value, s, true);
            }
            return new NamedObject(null, s, false);
        }
        Class c = Class.forName(ParseUtilities.descapeDollar(startTag.name));
        try {
            Field f = c.getDeclaredField("TYPE");
            Object o = getValue((Class) f.get(c), attr);
            putAlias(o);
            return new NamedObject(null, o, false);
        } catch (NoSuchFieldException e) {
            System.err.println("'" + c + "' is not a wrapper class: " + e);
        } catch (IllegalAccessException e) {
            System.err.println("'" + c + "' is not a wrapper class: " + e);
        }
        throw new IOException("Shouldn't get here");
    }

    /**--------------------------------------------------------------------------
		* Test class
		* ----------
		* Predates ObjIn and ObjOut!
		**/
    public static class Test {

        public static void main(String args[]) throws Exception {
            XMLDeserialize d = new XMLDeserialize();
            XMLSerialize s = new XMLSerialize();
            Object o;
            while ((o = d.deserialize()) != null) {
                s.serialize(o);
            }
        }
    }
}
