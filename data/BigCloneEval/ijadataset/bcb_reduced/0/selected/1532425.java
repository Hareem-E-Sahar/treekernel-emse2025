package net.sourceforge.htmlunit.corejs.javascript;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class implements the Array native object.
 * @author Norris Boyd
 * @author Mike McCabe
 */
public class NativeArray extends IdScriptableObject {

    static final long serialVersionUID = 7331366857676127338L;

    private static final Object ARRAY_TAG = "Array";

    private static final Integer NEGATIVE_ONE = Integer.valueOf(-1);

    static void init(Scriptable scope, boolean sealed) {
        NativeArray obj = new NativeArray(0);
        obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
    }

    static int getMaximumInitialCapacity() {
        return maximumInitialCapacity;
    }

    static void setMaximumInitialCapacity(int maximumInitialCapacity) {
        NativeArray.maximumInitialCapacity = maximumInitialCapacity;
    }

    public NativeArray(long lengthArg) {
        denseOnly = lengthArg <= maximumInitialCapacity;
        if (denseOnly) {
            int intLength = (int) lengthArg;
            if (intLength < DEFAULT_INITIAL_CAPACITY) intLength = DEFAULT_INITIAL_CAPACITY;
            dense = new Object[intLength];
            Arrays.fill(dense, Scriptable.NOT_FOUND);
        }
        length = lengthArg;
    }

    public NativeArray(Object[] array) {
        denseOnly = true;
        dense = array;
        length = array.length;
    }

    @Override
    public String getClassName() {
        return "Array";
    }

    private static final int Id_length = 1, MAX_INSTANCE_ID = 1;

    @Override
    protected int getMaxInstanceId() {
        return MAX_INSTANCE_ID;
    }

    @Override
    protected int findInstanceIdInfo(String s) {
        if (s.equals("length")) {
            return instanceIdInfo(DONTENUM | PERMANENT, Id_length);
        }
        return super.findInstanceIdInfo(s);
    }

    @Override
    protected String getInstanceIdName(int id) {
        if (id == Id_length) {
            return "length";
        }
        return super.getInstanceIdName(id);
    }

    @Override
    protected Object getInstanceIdValue(int id) {
        if (id == Id_length) {
            return ScriptRuntime.wrapNumber(length);
        }
        return super.getInstanceIdValue(id);
    }

    @Override
    protected void setInstanceIdValue(int id, Object value) {
        if (id == Id_length) {
            setLength(value);
            return;
        }
        super.setInstanceIdValue(id, value);
    }

    @Override
    protected void fillConstructorProperties(IdFunctionObject ctor) {
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_join, "join", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_reverse, "reverse", 0);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_sort, "sort", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_push, "push", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_pop, "pop", 0);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_shift, "shift", 0);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_unshift, "unshift", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_splice, "splice", 2);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_concat, "concat", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_slice, "slice", 2);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_indexOf, "indexOf", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_lastIndexOf, "lastIndexOf", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_every, "every", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_filter, "filter", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_forEach, "forEach", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_map, "map", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_some, "some", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_reduce, "reduce", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_reduceRight, "reduceRight", 1);
        addIdFunctionProperty(ctor, ARRAY_TAG, ConstructorId_isArray, "isArray", 1);
        super.fillConstructorProperties(ctor);
    }

    @Override
    protected void initPrototypeId(int id) {
        String s;
        int arity;
        switch(id) {
            case Id_constructor:
                arity = 1;
                s = "constructor";
                break;
            case Id_toString:
                arity = 0;
                s = "toString";
                break;
            case Id_toLocaleString:
                arity = 0;
                s = "toLocaleString";
                break;
            case Id_toSource:
                arity = 0;
                s = "toSource";
                break;
            case Id_join:
                arity = 1;
                s = "join";
                break;
            case Id_reverse:
                arity = 0;
                s = "reverse";
                break;
            case Id_sort:
                arity = 1;
                s = "sort";
                break;
            case Id_push:
                arity = 1;
                s = "push";
                break;
            case Id_pop:
                arity = 0;
                s = "pop";
                break;
            case Id_shift:
                arity = 0;
                s = "shift";
                break;
            case Id_unshift:
                arity = 1;
                s = "unshift";
                break;
            case Id_splice:
                arity = 2;
                s = "splice";
                break;
            case Id_concat:
                arity = 1;
                s = "concat";
                break;
            case Id_slice:
                arity = 2;
                s = "slice";
                break;
            case Id_indexOf:
                arity = 1;
                s = "indexOf";
                break;
            case Id_lastIndexOf:
                arity = 1;
                s = "lastIndexOf";
                break;
            case Id_every:
                arity = 1;
                s = "every";
                break;
            case Id_filter:
                arity = 1;
                s = "filter";
                break;
            case Id_forEach:
                arity = 1;
                s = "forEach";
                break;
            case Id_map:
                arity = 1;
                s = "map";
                break;
            case Id_some:
                arity = 1;
                s = "some";
                break;
            case Id_reduce:
                arity = 1;
                s = "reduce";
                break;
            case Id_reduceRight:
                arity = 1;
                s = "reduceRight";
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(id));
        }
        initPrototypeMethod(ARRAY_TAG, id, s, arity);
    }

    @Override
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (!f.hasTag(ARRAY_TAG)) {
            return super.execIdCall(f, cx, scope, thisObj, args);
        }
        int id = f.methodId();
        again: for (; ; ) {
            switch(id) {
                case ConstructorId_join:
                case ConstructorId_reverse:
                case ConstructorId_sort:
                case ConstructorId_push:
                case ConstructorId_pop:
                case ConstructorId_shift:
                case ConstructorId_unshift:
                case ConstructorId_splice:
                case ConstructorId_concat:
                case ConstructorId_slice:
                case ConstructorId_indexOf:
                case ConstructorId_lastIndexOf:
                case ConstructorId_every:
                case ConstructorId_filter:
                case ConstructorId_forEach:
                case ConstructorId_map:
                case ConstructorId_some:
                case ConstructorId_reduce:
                case ConstructorId_reduceRight:
                    {
                        if (args.length > 0) {
                            thisObj = ScriptRuntime.toObject(scope, args[0]);
                            Object[] newArgs = new Object[args.length - 1];
                            for (int i = 0; i < newArgs.length; i++) newArgs[i] = args[i + 1];
                            args = newArgs;
                        }
                        id = -id;
                        continue again;
                    }
                case ConstructorId_isArray:
                    return args.length > 0 && (args[0] instanceof NativeArray);
                case Id_constructor:
                    {
                        boolean inNewExpr = (thisObj == null);
                        if (!inNewExpr) {
                            return f.construct(cx, scope, args);
                        }
                        return jsConstructor(cx, scope, args);
                    }
                case Id_toString:
                    return toStringHelper(cx, scope, thisObj, cx.hasFeature(Context.FEATURE_TO_STRING_AS_SOURCE), false);
                case Id_toLocaleString:
                    return toStringHelper(cx, scope, thisObj, false, true);
                case Id_toSource:
                    return toStringHelper(cx, scope, thisObj, true, false);
                case Id_join:
                    return js_join(cx, thisObj, args);
                case Id_reverse:
                    return js_reverse(cx, thisObj, args);
                case Id_sort:
                    return js_sort(cx, scope, thisObj, args);
                case Id_push:
                    return js_push(cx, thisObj, args);
                case Id_pop:
                    return js_pop(cx, thisObj, args);
                case Id_shift:
                    return js_shift(cx, thisObj, args);
                case Id_unshift:
                    return js_unshift(cx, thisObj, args);
                case Id_splice:
                    return js_splice(cx, scope, thisObj, args);
                case Id_concat:
                    return js_concat(cx, scope, thisObj, args);
                case Id_slice:
                    return js_slice(cx, thisObj, args);
                case Id_indexOf:
                    return indexOfHelper(cx, thisObj, args, false);
                case Id_lastIndexOf:
                    return indexOfHelper(cx, thisObj, args, true);
                case Id_every:
                case Id_filter:
                case Id_forEach:
                case Id_map:
                case Id_some:
                    return iterativeMethod(cx, id, scope, thisObj, args);
                case Id_reduce:
                case Id_reduceRight:
                    return reduceMethod(cx, id, scope, thisObj, args);
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    @Override
    public Object get(int index, Scriptable start) {
        if (!denseOnly && isGetterOrSetter(null, index, false)) return super.get(index, start);
        if (dense != null && 0 <= index && index < dense.length) return dense[index];
        return super.get(index, start);
    }

    @Override
    public boolean has(int index, Scriptable start) {
        if (!denseOnly && isGetterOrSetter(null, index, false)) return super.has(index, start);
        if (dense != null && 0 <= index && index < dense.length) return dense[index] != NOT_FOUND;
        return super.has(index, start);
    }

    private static long toArrayIndex(String id) {
        double d = ScriptRuntime.toNumber(id);
        if (d == d) {
            long index = ScriptRuntime.toUint32(d);
            if (index == d && index != 4294967295L) {
                if (Long.toString(index).equals(id)) {
                    return index;
                }
            }
        }
        return -1;
    }

    @Override
    public void put(String id, Scriptable start, Object value) {
        super.put(id, start, value);
        if (start == this) {
            long index = toArrayIndex(id);
            if (index >= length) {
                length = index + 1;
                denseOnly = false;
            }
        }
    }

    private boolean ensureCapacity(int capacity) {
        if (capacity > dense.length) {
            if (capacity > MAX_PRE_GROW_SIZE) {
                denseOnly = false;
                return false;
            }
            capacity = Math.max(capacity, (int) (dense.length * GROW_FACTOR));
            Object[] newDense = new Object[capacity];
            System.arraycopy(dense, 0, newDense, 0, dense.length);
            Arrays.fill(newDense, dense.length, newDense.length, Scriptable.NOT_FOUND);
            dense = newDense;
        }
        return true;
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        if (start == this && !isSealed() && dense != null && 0 <= index && (denseOnly || !isGetterOrSetter(null, index, true))) {
            if (index < dense.length) {
                dense[index] = value;
                if (this.length <= index) this.length = (long) index + 1;
                return;
            } else if (denseOnly && index < dense.length * GROW_FACTOR && ensureCapacity(index + 1)) {
                dense[index] = value;
                this.length = (long) index + 1;
                return;
            } else {
                denseOnly = false;
            }
        }
        super.put(index, start, value);
        if (start == this) {
            if (this.length <= index) {
                this.length = (long) index + 1;
            }
        }
    }

    @Override
    public void delete(int index) {
        if (dense != null && 0 <= index && index < dense.length && !isSealed() && (denseOnly || !isGetterOrSetter(null, index, true))) {
            dense[index] = NOT_FOUND;
        } else {
            super.delete(index);
        }
    }

    @Override
    public Object[] getIds() {
        Object[] superIds = super.getIds();
        if (dense == null) {
            return superIds;
        }
        int N = dense.length;
        long currentLength = length;
        if (N > currentLength) {
            N = (int) currentLength;
        }
        if (N == 0) {
            return superIds;
        }
        int superLength = superIds.length;
        Object[] ids = new Object[N + superLength];
        int presentCount = 0;
        for (int i = 0; i != N; ++i) {
            if (dense[i] != NOT_FOUND) {
                ids[presentCount] = Integer.valueOf(i);
                ++presentCount;
            }
        }
        if (presentCount != N) {
            Object[] tmp = new Object[presentCount + superLength];
            System.arraycopy(ids, 0, tmp, 0, presentCount);
            ids = tmp;
        }
        System.arraycopy(superIds, 0, ids, presentCount, superLength);
        return ids;
    }

    @Override
    public Object[] getAllIds() {
        Set<Object> allIds = new LinkedHashSet<Object>(Arrays.asList(this.getIds()));
        allIds.addAll(Arrays.asList(super.getAllIds()));
        return allIds.toArray();
    }

    public Integer[] getIndexIds() {
        Object[] ids = getIds();
        java.util.List<Integer> indices = new java.util.ArrayList<Integer>(ids.length);
        for (Object id : ids) {
            int int32Id = ScriptRuntime.toInt32(id);
            if (int32Id >= 0 && ScriptRuntime.toString(int32Id).equals(ScriptRuntime.toString(id))) {
                indices.add(int32Id);
            }
        }
        return indices.toArray(new Integer[indices.size()]);
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        if (hint == ScriptRuntime.NumberClass) {
            Context cx = Context.getContext();
            if (cx.getLanguageVersion() == Context.VERSION_1_2) return Long.valueOf(length);
        }
        return super.getDefaultValue(hint);
    }

    private ScriptableObject defaultIndexPropertyDescriptor(Object value) {
        Scriptable scope = getParentScope();
        if (scope == null) scope = this;
        ScriptableObject desc = new NativeObject();
        ScriptRuntime.setObjectProtoAndParent(desc, scope);
        desc.defineProperty("value", value, EMPTY);
        desc.defineProperty("writable", true, EMPTY);
        desc.defineProperty("enumerable", true, EMPTY);
        desc.defineProperty("configurable", true, EMPTY);
        return desc;
    }

    @Override
    protected ScriptableObject getOwnPropertyDescriptor(Context cx, Object id) {
        if (dense != null) {
            int index = toIndex(id);
            if (0 <= index && index < length) {
                Object value = dense[index];
                return defaultIndexPropertyDescriptor(value);
            }
        }
        return super.getOwnPropertyDescriptor(cx, id);
    }

    @Override
    public void defineOwnProperty(Context cx, Object id, ScriptableObject desc) {
        if (dense != null) {
            Object[] values = dense;
            dense = null;
            denseOnly = false;
            for (int i = 0; i < values.length; i++) {
                if (values[i] != NOT_FOUND) {
                    put(i, this, values[i]);
                }
            }
        }
        int index = toIndex(id);
        if (index >= length) {
            length = index + 1;
        }
        super.defineOwnProperty(cx, id, desc);
    }

    private int toIndex(Object id) {
        if (id instanceof String) {
            return (int) toArrayIndex((String) id);
        } else if (id instanceof Number) {
            return ((Number) id).intValue();
        } else {
            return -1;
        }
    }

    /**
     * See ECMA 15.4.1,2
     */
    private static Object jsConstructor(Context cx, Scriptable scope, Object[] args) {
        if (args.length == 0) return new NativeArray(0);
        if (cx.getLanguageVersion() == Context.VERSION_1_2) {
            return new NativeArray(args);
        } else {
            Object arg0 = args[0];
            if (args.length > 1 || !(arg0 instanceof Number)) {
                return new NativeArray(args);
            } else {
                long len = ScriptRuntime.toUint32(arg0);
                if (len != ((Number) arg0).doubleValue()) {
                    String msg = ScriptRuntime.getMessage0("msg.arraylength.bad");
                    throw ScriptRuntime.constructError("RangeError", msg);
                }
                return new NativeArray(len);
            }
        }
    }

    public long getLength() {
        return length;
    }

    /** @deprecated Use {@link #getLength()} instead. */
    public long jsGet_length() {
        return getLength();
    }

    /**
     * Change the value of the internal flag that determines whether all
     * storage is handed by a dense backing array rather than an associative
     * store.
     * @param denseOnly new value for denseOnly flag
     * @throws IllegalArgumentException if an attempt is made to enable
     *   denseOnly after it was disabled; NativeArray code is not written
     *   to handle switching back to a dense representation
     */
    void setDenseOnly(boolean denseOnly) {
        if (denseOnly && !this.denseOnly) throw new IllegalArgumentException();
        this.denseOnly = denseOnly;
    }

    private void setLength(Object val) {
        double d = ScriptRuntime.toNumber(val);
        long longVal = ScriptRuntime.toUint32(d);
        if (longVal != d) {
            String msg = ScriptRuntime.getMessage0("msg.arraylength.bad");
            throw ScriptRuntime.constructError("RangeError", msg);
        }
        if (denseOnly) {
            if (longVal < length) {
                Arrays.fill(dense, (int) longVal, dense.length, NOT_FOUND);
                length = longVal;
                return;
            } else if (longVal < MAX_PRE_GROW_SIZE && longVal < (length * GROW_FACTOR) && ensureCapacity((int) longVal)) {
                length = longVal;
                return;
            } else {
                denseOnly = false;
            }
        }
        if (longVal < length) {
            if (length - longVal > 0x1000) {
                Object[] e = getIds();
                for (int i = 0; i < e.length; i++) {
                    Object id = e[i];
                    if (id instanceof String) {
                        String strId = (String) id;
                        long index = toArrayIndex(strId);
                        if (index >= longVal) delete(strId);
                    } else {
                        int index = ((Integer) id).intValue();
                        if (index >= longVal) delete(index);
                    }
                }
            } else {
                for (long i = longVal; i < length; i++) {
                    deleteElem(this, i);
                }
            }
        }
        length = longVal;
    }

    static long getLengthProperty(Context cx, Scriptable obj) {
        if (obj instanceof NativeString) {
            return ((NativeString) obj).getLength();
        } else if (obj instanceof NativeArray) {
            return ((NativeArray) obj).getLength();
        }
        return ScriptRuntime.toUint32(ScriptRuntime.getObjectProp(obj, "length", cx));
    }

    private static Object setLengthProperty(Context cx, Scriptable target, long length) {
        return ScriptRuntime.setObjectProp(target, "length", ScriptRuntime.wrapNumber(length), cx);
    }

    private static void deleteElem(Scriptable target, long index) {
        int i = (int) index;
        if (i == index) {
            target.delete(i);
        } else {
            target.delete(Long.toString(index));
        }
    }

    private static Object getElem(Context cx, Scriptable target, long index) {
        if (index > Integer.MAX_VALUE) {
            String id = Long.toString(index);
            return ScriptRuntime.getObjectProp(target, id, cx);
        } else {
            return ScriptRuntime.getObjectIndex(target, (int) index, cx);
        }
    }

    private static Object getRawElem(Scriptable target, long index) {
        if (index > Integer.MAX_VALUE) {
            return ScriptableObject.getProperty(target, Long.toString(index));
        } else {
            return ScriptableObject.getProperty(target, (int) index);
        }
    }

    private static void setElem(Context cx, Scriptable target, long index, Object value) {
        if (index > Integer.MAX_VALUE) {
            String id = Long.toString(index);
            ScriptRuntime.setObjectProp(target, id, value, cx);
        } else {
            ScriptRuntime.setObjectIndex(target, (int) index, value, cx);
        }
    }

    private static String toStringHelper(Context cx, Scriptable scope, Scriptable thisObj, boolean toSource, boolean toLocale) {
        long length = getLengthProperty(cx, thisObj);
        StringBuilder result = new StringBuilder(256);
        String separator;
        if (toSource) {
            result.append('[');
            separator = ", ";
        } else {
            separator = ",";
        }
        boolean haslast = false;
        long i = 0;
        boolean toplevel, iterating;
        if (cx.iterating == null) {
            toplevel = true;
            iterating = false;
            cx.iterating = new ObjToIntMap(31);
        } else {
            toplevel = false;
            iterating = cx.iterating.has(thisObj);
        }
        try {
            if (!iterating) {
                cx.iterating.put(thisObj, 0);
                for (i = 0; i < length; i++) {
                    if (i > 0) result.append(separator);
                    Object elem = getElem(cx, thisObj, i);
                    if (elem == null || elem == Undefined.instance) {
                        haslast = false;
                        continue;
                    }
                    haslast = true;
                    if (toSource) {
                        result.append(ScriptRuntime.uneval(cx, scope, elem));
                    } else if (elem instanceof String) {
                        String s = (String) elem;
                        if (toSource) {
                            result.append('\"');
                            result.append(ScriptRuntime.escapeString(s));
                            result.append('\"');
                        } else {
                            result.append(s);
                        }
                    } else {
                        if (toLocale) {
                            Callable fun;
                            Scriptable funThis;
                            fun = ScriptRuntime.getPropFunctionAndThis(elem, "toLocaleString", cx);
                            funThis = ScriptRuntime.lastStoredScriptable(cx);
                            elem = fun.call(cx, scope, funThis, ScriptRuntime.emptyArgs);
                        }
                        result.append(ScriptRuntime.toString(elem));
                    }
                }
            }
        } finally {
            if (toplevel) {
                cx.iterating = null;
            }
        }
        if (toSource) {
            if (!haslast && i > 0) result.append(", ]"); else result.append(']');
        }
        return result.toString();
    }

    /**
     * See ECMA 15.4.4.3
     */
    private static String js_join(Context cx, Scriptable thisObj, Object[] args) {
        long llength = getLengthProperty(cx, thisObj);
        int length = (int) llength;
        if (llength != length) {
            throw Context.reportRuntimeError1("msg.arraylength.too.big", String.valueOf(llength));
        }
        String separator = (args.length < 1 || args[0] == Undefined.instance) ? "," : ScriptRuntime.toString(args[0]);
        if (thisObj instanceof NativeArray) {
            NativeArray na = (NativeArray) thisObj;
            if (na.denseOnly) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    if (i != 0) {
                        sb.append(separator);
                    }
                    if (i < na.dense.length) {
                        Object temp = na.dense[i];
                        if (temp != null && temp != Undefined.instance && temp != Scriptable.NOT_FOUND) {
                            sb.append(ScriptRuntime.toString(temp));
                        }
                    }
                }
                return sb.toString();
            }
        }
        if (length == 0) {
            return "";
        }
        String[] buf = new String[length];
        int total_size = 0;
        for (int i = 0; i != length; i++) {
            Object temp = getElem(cx, thisObj, i);
            if (temp != null && temp != Undefined.instance) {
                String str = ScriptRuntime.toString(temp);
                total_size += str.length();
                buf[i] = str;
            }
        }
        total_size += (length - 1) * separator.length();
        StringBuilder sb = new StringBuilder(total_size);
        for (int i = 0; i != length; i++) {
            if (i != 0) {
                sb.append(separator);
            }
            String str = buf[i];
            if (str != null) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    /**
     * See ECMA 15.4.4.4
     */
    private static Scriptable js_reverse(Context cx, Scriptable thisObj, Object[] args) {
        if (thisObj instanceof NativeArray) {
            NativeArray na = (NativeArray) thisObj;
            if (na.denseOnly) {
                for (int i = 0, j = ((int) na.length) - 1; i < j; i++, j--) {
                    Object temp = na.dense[i];
                    na.dense[i] = na.dense[j];
                    na.dense[j] = temp;
                }
                return thisObj;
            }
        }
        long len = getLengthProperty(cx, thisObj);
        long half = len / 2;
        for (long i = 0; i < half; i++) {
            long j = len - i - 1;
            Object temp1 = getElem(cx, thisObj, i);
            Object temp2 = getElem(cx, thisObj, j);
            setElem(cx, thisObj, i, temp2);
            setElem(cx, thisObj, j, temp1);
        }
        return thisObj;
    }

    /**
     * See ECMA 15.4.4.5
     */
    private static Scriptable js_sort(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
        final Comparator<Object> comparator;
        if (args.length > 0 && Undefined.instance != args[0]) {
            final Callable jsCompareFunction = ScriptRuntime.getValueFunctionAndThis(args[0], cx);
            final Scriptable funThis = ScriptRuntime.lastStoredScriptable(cx);
            final Object[] cmpBuf = new Object[2];
            comparator = new Comparator<Object>() {

                public int compare(final Object x, final Object y) {
                    if (x == y) {
                        return 0;
                    } else if (y == Undefined.instance || y == Scriptable.NOT_FOUND) {
                        return -1;
                    } else if (x == Undefined.instance || x == Scriptable.NOT_FOUND) {
                        return 1;
                    }
                    cmpBuf[0] = x;
                    cmpBuf[1] = y;
                    Object ret = jsCompareFunction.call(cx, scope, funThis, cmpBuf);
                    final double d = ScriptRuntime.toNumber(ret);
                    if (d < 0) {
                        return -1;
                    } else if (d > 0) {
                        return +1;
                    }
                    return 0;
                }
            };
        } else {
            comparator = new Comparator<Object>() {

                public int compare(final Object x, final Object y) {
                    if (x == y) return 0; else if (y == Undefined.instance || y == Scriptable.NOT_FOUND) {
                        return -1;
                    } else if (x == Undefined.instance || x == Scriptable.NOT_FOUND) {
                        return 1;
                    }
                    final String a = ScriptRuntime.toString(x);
                    final String b = ScriptRuntime.toString(y);
                    return a.compareTo(b);
                }
            };
        }
        final int length = (int) getLengthProperty(cx, thisObj);
        final Object[] working = new Object[length];
        for (int i = 0; i != length; ++i) {
            working[i] = getElem(cx, thisObj, i);
        }
        Arrays.sort(working, comparator);
        for (int i = 0; i < length; ++i) {
            setElem(cx, thisObj, i, working[i]);
        }
        return thisObj;
    }

    /**
     * Non-ECMA methods.
     */
    private static Object js_push(Context cx, Scriptable thisObj, Object[] args) {
        if (thisObj instanceof NativeArray) {
            NativeArray na = (NativeArray) thisObj;
            if (na.denseOnly && na.ensureCapacity((int) na.length + args.length)) {
                for (int i = 0; i < args.length; i++) {
                    na.dense[(int) na.length++] = args[i];
                }
                return ScriptRuntime.wrapNumber(na.length);
            }
        }
        long length = getLengthProperty(cx, thisObj);
        for (int i = 0; i < args.length; i++) {
            setElem(cx, thisObj, length + i, args[i]);
        }
        length += args.length;
        Object lengthObj = setLengthProperty(cx, thisObj, length);
        if (cx.getLanguageVersion() == Context.VERSION_1_2) return args.length == 0 ? Undefined.instance : args[args.length - 1]; else return lengthObj;
    }

    private static Object js_pop(Context cx, Scriptable thisObj, Object[] args) {
        Object result;
        if (thisObj instanceof NativeArray) {
            NativeArray na = (NativeArray) thisObj;
            if (na.denseOnly && na.length > 0) {
                na.length--;
                result = na.dense[(int) na.length];
                na.dense[(int) na.length] = NOT_FOUND;
                return result;
            }
        }
        long length = getLengthProperty(cx, thisObj);
        if (length > 0) {
            length--;
            result = getElem(cx, thisObj, length);
        } else {
            result = Undefined.instance;
        }
        setLengthProperty(cx, thisObj, length);
        return result;
    }

    private static Object js_shift(Context cx, Scriptable thisObj, Object[] args) {
        if (thisObj instanceof NativeArray) {
            NativeArray na = (NativeArray) thisObj;
            if (na.denseOnly && na.length > 0) {
                na.length--;
                Object result = na.dense[0];
                System.arraycopy(na.dense, 1, na.dense, 0, (int) na.length);
                na.dense[(int) na.length] = NOT_FOUND;
                return result;
            }
        }
        Object result;
        long length = getLengthProperty(cx, thisObj);
        if (length > 0) {
            long i = 0;
            length--;
            result = getElem(cx, thisObj, i);
            if (length > 0) {
                for (i = 1; i <= length; i++) {
                    Object temp = getElem(cx, thisObj, i);
                    setElem(cx, thisObj, i - 1, temp);
                }
            }
        } else {
            result = Undefined.instance;
        }
        setLengthProperty(cx, thisObj, length);
        return result;
    }

    private static Object js_unshift(Context cx, Scriptable thisObj, Object[] args) {
        if (thisObj instanceof NativeArray) {
            NativeArray na = (NativeArray) thisObj;
            if (na.denseOnly && na.ensureCapacity((int) na.length + args.length)) {
                System.arraycopy(na.dense, 0, na.dense, args.length, (int) na.length);
                for (int i = 0; i < args.length; i++) {
                    na.dense[i] = args[i];
                }
                na.length += args.length;
                return ScriptRuntime.wrapNumber(na.length);
            }
        }
        long length = getLengthProperty(cx, thisObj);
        int argc = args.length;
        if (args.length > 0) {
            if (length > 0) {
                for (long last = length - 1; last >= 0; last--) {
                    Object temp = getElem(cx, thisObj, last);
                    setElem(cx, thisObj, last + argc, temp);
                }
            }
            for (int i = 0; i < args.length; i++) {
                setElem(cx, thisObj, i, args[i]);
            }
            length += args.length;
            return setLengthProperty(cx, thisObj, length);
        }
        return ScriptRuntime.wrapNumber(length);
    }

    private static Object js_splice(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        NativeArray na = null;
        boolean denseMode = false;
        if (thisObj instanceof NativeArray) {
            na = (NativeArray) thisObj;
            denseMode = na.denseOnly;
        }
        scope = getTopLevelScope(scope);
        int argc = args.length;
        if (argc == 0) return ScriptRuntime.newObject(cx, scope, "Array", null);
        long length = getLengthProperty(cx, thisObj);
        long begin = toSliceIndex(ScriptRuntime.toInteger(args[0]), length);
        argc--;
        long count;
        if (args.length == 1) {
            count = length - begin;
        } else {
            double dcount = ScriptRuntime.toInteger(args[1]);
            if (dcount < 0) {
                count = 0;
            } else if (dcount > (length - begin)) {
                count = length - begin;
            } else {
                count = (long) dcount;
            }
            argc--;
        }
        long end = begin + count;
        Object result;
        if (count != 0) {
            if (count == 1 && (cx.getLanguageVersion() == Context.VERSION_1_2)) {
                result = getElem(cx, thisObj, begin);
            } else {
                if (denseMode) {
                    int intLen = (int) (end - begin);
                    Object[] copy = new Object[intLen];
                    System.arraycopy(na.dense, (int) begin, copy, 0, intLen);
                    result = cx.newArray(scope, copy);
                } else {
                    Scriptable resultArray = ScriptRuntime.newObject(cx, scope, "Array", null);
                    for (long last = begin; last != end; last++) {
                        Object temp = getElem(cx, thisObj, last);
                        setElem(cx, resultArray, last - begin, temp);
                    }
                    result = resultArray;
                }
            }
        } else {
            if (cx.getLanguageVersion() == Context.VERSION_1_2) {
                result = Undefined.instance;
            } else {
                result = ScriptRuntime.newObject(cx, scope, "Array", null);
            }
        }
        long delta = argc - count;
        if (denseMode && length + delta < Integer.MAX_VALUE && na.ensureCapacity((int) (length + delta))) {
            System.arraycopy(na.dense, (int) end, na.dense, (int) (begin + argc), (int) (length - end));
            if (argc > 0) {
                System.arraycopy(args, 2, na.dense, (int) begin, argc);
            }
            if (delta < 0) {
                Arrays.fill(na.dense, (int) (length + delta), (int) length, NOT_FOUND);
            }
            na.length = length + delta;
            return result;
        }
        if (delta > 0) {
            for (long last = length - 1; last >= end; last--) {
                Object temp = getElem(cx, thisObj, last);
                setElem(cx, thisObj, last + delta, temp);
            }
        } else if (delta < 0) {
            for (long last = end; last < length; last++) {
                Object temp = getElem(cx, thisObj, last);
                setElem(cx, thisObj, last + delta, temp);
            }
        }
        int argoffset = args.length - argc;
        for (int i = 0; i < argc; i++) {
            setElem(cx, thisObj, begin + i, args[i + argoffset]);
        }
        setLengthProperty(cx, thisObj, length + delta);
        return result;
    }

    private static Scriptable js_concat(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        scope = getTopLevelScope(scope);
        Function ctor = ScriptRuntime.getExistingCtor(cx, scope, "Array");
        Scriptable result = ctor.construct(cx, scope, ScriptRuntime.emptyArgs);
        if (thisObj instanceof NativeArray && result instanceof NativeArray) {
            NativeArray denseThis = (NativeArray) thisObj;
            NativeArray denseResult = (NativeArray) result;
            if (denseThis.denseOnly && denseResult.denseOnly) {
                boolean canUseDense = true;
                int length = (int) denseThis.length;
                for (int i = 0; i < args.length && canUseDense; i++) {
                    if (args[i] instanceof NativeArray) {
                        final NativeArray arg = (NativeArray) args[i];
                        canUseDense = arg.denseOnly;
                        length += arg.length;
                    } else {
                        length++;
                    }
                }
                if (canUseDense && denseResult.ensureCapacity(length)) {
                    System.arraycopy(denseThis.dense, 0, denseResult.dense, 0, (int) denseThis.length);
                    int cursor = (int) denseThis.length;
                    for (int i = 0; i < args.length && canUseDense; i++) {
                        if (args[i] instanceof NativeArray) {
                            NativeArray arg = (NativeArray) args[i];
                            System.arraycopy(arg.dense, 0, denseResult.dense, cursor, (int) arg.length);
                            cursor += (int) arg.length;
                        } else {
                            denseResult.dense[cursor++] = args[i];
                        }
                    }
                    denseResult.length = length;
                    return result;
                }
            }
        }
        long length;
        long slot = 0;
        if (ScriptRuntime.instanceOf(thisObj, ctor, cx)) {
            length = getLengthProperty(cx, thisObj);
            for (slot = 0; slot < length; slot++) {
                Object temp = getElem(cx, thisObj, slot);
                setElem(cx, result, slot, temp);
            }
        } else {
            setElem(cx, result, slot++, thisObj);
        }
        for (int i = 0; i < args.length; i++) {
            if (ScriptRuntime.instanceOf(args[i], ctor, cx)) {
                Scriptable arg = (Scriptable) args[i];
                length = getLengthProperty(cx, arg);
                for (long j = 0; j < length; j++, slot++) {
                    Object temp = getElem(cx, arg, j);
                    setElem(cx, result, slot, temp);
                }
            } else {
                setElem(cx, result, slot++, args[i]);
            }
        }
        return result;
    }

    private Scriptable js_slice(Context cx, Scriptable thisObj, Object[] args) {
        Scriptable scope = getTopLevelScope(this);
        Scriptable result = ScriptRuntime.newObject(cx, scope, "Array", null);
        long length = getLengthProperty(cx, thisObj);
        long begin, end;
        if (args.length == 0) {
            begin = 0;
            end = length;
        } else {
            begin = toSliceIndex(ScriptRuntime.toInteger(args[0]), length);
            if (args.length == 1) {
                end = length;
            } else {
                end = toSliceIndex(ScriptRuntime.toInteger(args[1]), length);
            }
        }
        for (long slot = begin; slot < end; slot++) {
            Object temp = getElem(cx, thisObj, slot);
            setElem(cx, result, slot - begin, temp);
        }
        return result;
    }

    private static long toSliceIndex(double value, long length) {
        long result;
        if (value < 0.0) {
            if (value + length < 0.0) {
                result = 0;
            } else {
                result = (long) (value + length);
            }
        } else if (value > length) {
            result = length;
        } else {
            result = (long) value;
        }
        return result;
    }

    /**
     * Implements the methods "indexOf" and "lastIndexOf".
     */
    private Object indexOfHelper(Context cx, Scriptable thisObj, Object[] args, boolean isLast) {
        Object compareTo = args.length > 0 ? args[0] : Undefined.instance;
        long length = getLengthProperty(cx, thisObj);
        long start;
        if (isLast) {
            if (args.length < 2) {
                start = length - 1;
            } else {
                start = ScriptRuntime.toInt32(ScriptRuntime.toNumber(args[1]));
                if (start >= length) start = length - 1; else if (start < 0) start += length;
            }
        } else {
            if (args.length < 2) {
                start = 0;
            } else {
                start = ScriptRuntime.toInt32(ScriptRuntime.toNumber(args[1]));
                if (start < 0) {
                    start += length;
                    if (start < 0) start = 0;
                }
            }
        }
        if (thisObj instanceof NativeArray) {
            NativeArray na = (NativeArray) thisObj;
            if (na.denseOnly) {
                if (isLast) {
                    for (int i = (int) start; i >= 0; i--) {
                        if (na.dense[i] != Scriptable.NOT_FOUND && ScriptRuntime.shallowEq(na.dense[i], compareTo)) {
                            return Long.valueOf(i);
                        }
                    }
                } else {
                    for (int i = (int) start; i < length; i++) {
                        if (na.dense[i] != Scriptable.NOT_FOUND && ScriptRuntime.shallowEq(na.dense[i], compareTo)) {
                            return Long.valueOf(i);
                        }
                    }
                }
                return NEGATIVE_ONE;
            }
        }
        if (isLast) {
            for (long i = start; i >= 0; i--) {
                if (ScriptRuntime.shallowEq(getElem(cx, thisObj, i), compareTo)) {
                    return new Long(i);
                }
            }
        } else {
            for (long i = start; i < length; i++) {
                if (ScriptRuntime.shallowEq(getElem(cx, thisObj, i), compareTo)) {
                    return new Long(i);
                }
            }
        }
        return NEGATIVE_ONE;
    }

    /**
     * Implements the methods "every", "filter", "forEach", "map", and "some".
     */
    private Object iterativeMethod(Context cx, int id, Scriptable scope, Scriptable thisObj, Object[] args) {
        Object callbackArg = args.length > 0 ? args[0] : Undefined.instance;
        if (callbackArg == null || !(callbackArg instanceof Function)) {
            throw ScriptRuntime.notFunctionError(callbackArg);
        }
        Function f = (Function) callbackArg;
        Scriptable parent = ScriptableObject.getTopLevelScope(f);
        Scriptable thisArg;
        if (args.length < 2 || args[1] == null || args[1] == Undefined.instance) {
            thisArg = parent;
        } else {
            thisArg = ScriptRuntime.toObject(cx, scope, args[1]);
        }
        long length = getLengthProperty(cx, thisObj);
        int resultLength = id == Id_map ? (int) length : 0;
        Scriptable array = cx.newArray(scope, resultLength);
        long j = 0;
        for (long i = 0; i < length; i++) {
            Object[] innerArgs = new Object[3];
            Object elem = getRawElem(thisObj, i);
            if (elem == Scriptable.NOT_FOUND) {
                continue;
            }
            innerArgs[0] = elem;
            innerArgs[1] = Long.valueOf(i);
            innerArgs[2] = thisObj;
            Object result = f.call(cx, parent, thisArg, innerArgs);
            switch(id) {
                case Id_every:
                    if (!ScriptRuntime.toBoolean(result)) return Boolean.FALSE;
                    break;
                case Id_filter:
                    if (ScriptRuntime.toBoolean(result)) setElem(cx, array, j++, innerArgs[0]);
                    break;
                case Id_forEach:
                    break;
                case Id_map:
                    setElem(cx, array, i, result);
                    break;
                case Id_some:
                    if (ScriptRuntime.toBoolean(result)) return Boolean.TRUE;
                    break;
            }
        }
        switch(id) {
            case Id_every:
                return Boolean.TRUE;
            case Id_filter:
            case Id_map:
                return array;
            case Id_some:
                return Boolean.FALSE;
            case Id_forEach:
            default:
                return Undefined.instance;
        }
    }

    /**
     * Implements the methods "reduce" and "reduceRight".
     */
    private Object reduceMethod(Context cx, int id, Scriptable scope, Scriptable thisObj, Object[] args) {
        Object callbackArg = args.length > 0 ? args[0] : Undefined.instance;
        if (callbackArg == null || !(callbackArg instanceof Function)) {
            throw ScriptRuntime.notFunctionError(callbackArg);
        }
        Function f = (Function) callbackArg;
        Scriptable parent = ScriptableObject.getTopLevelScope(f);
        long length = getLengthProperty(cx, thisObj);
        long offset = id == Id_reduceRight ? length - 1 : 0;
        Object value = args.length > 1 ? args[1] : Scriptable.NOT_FOUND;
        for (long i = 0; i < length; i++) {
            Object elem = getRawElem(thisObj, Math.abs(i - offset));
            if (elem == Scriptable.NOT_FOUND) {
                continue;
            }
            if (value == Scriptable.NOT_FOUND) {
                value = elem;
            } else {
                Object[] innerArgs = new Object[4];
                innerArgs[0] = value;
                innerArgs[1] = elem;
                innerArgs[2] = new Long(i);
                innerArgs[3] = thisObj;
                value = f.call(cx, parent, parent, innerArgs);
            }
        }
        if (value == Scriptable.NOT_FOUND) {
            throw ScriptRuntime.typeError0("msg.empty.array.reduce");
        }
        return value;
    }

    @Override
    protected int findPrototypeId(String s) {
        int id;
        L0: {
            id = 0;
            String X = null;
            int c;
            L: switch(s.length()) {
                case 3:
                    c = s.charAt(0);
                    if (c == 'm') {
                        if (s.charAt(2) == 'p' && s.charAt(1) == 'a') {
                            id = Id_map;
                            break L0;
                        }
                    } else if (c == 'p') {
                        if (s.charAt(2) == 'p' && s.charAt(1) == 'o') {
                            id = Id_pop;
                            break L0;
                        }
                    }
                    break L;
                case 4:
                    switch(s.charAt(2)) {
                        case 'i':
                            X = "join";
                            id = Id_join;
                            break L;
                        case 'm':
                            X = "some";
                            id = Id_some;
                            break L;
                        case 'r':
                            X = "sort";
                            id = Id_sort;
                            break L;
                        case 's':
                            X = "push";
                            id = Id_push;
                            break L;
                    }
                    break L;
                case 5:
                    c = s.charAt(1);
                    if (c == 'h') {
                        X = "shift";
                        id = Id_shift;
                    } else if (c == 'l') {
                        X = "slice";
                        id = Id_slice;
                    } else if (c == 'v') {
                        X = "every";
                        id = Id_every;
                    }
                    break L;
                case 6:
                    c = s.charAt(0);
                    if (c == 'c') {
                        X = "concat";
                        id = Id_concat;
                    } else if (c == 'f') {
                        X = "filter";
                        id = Id_filter;
                    } else if (c == 's') {
                        X = "splice";
                        id = Id_splice;
                    } else if (c == 'r') {
                        X = "reduce";
                        id = Id_reduce;
                    }
                    break L;
                case 7:
                    switch(s.charAt(0)) {
                        case 'f':
                            X = "forEach";
                            id = Id_forEach;
                            break L;
                        case 'i':
                            X = "indexOf";
                            id = Id_indexOf;
                            break L;
                        case 'r':
                            X = "reverse";
                            id = Id_reverse;
                            break L;
                        case 'u':
                            X = "unshift";
                            id = Id_unshift;
                            break L;
                    }
                    break L;
                case 8:
                    c = s.charAt(3);
                    if (c == 'o') {
                        X = "toSource";
                        id = Id_toSource;
                    } else if (c == 't') {
                        X = "toString";
                        id = Id_toString;
                    }
                    break L;
                case 11:
                    c = s.charAt(0);
                    if (c == 'c') {
                        X = "constructor";
                        id = Id_constructor;
                    } else if (c == 'l') {
                        X = "lastIndexOf";
                        id = Id_lastIndexOf;
                    } else if (c == 'r') {
                        X = "reduceRight";
                        id = Id_reduceRight;
                    }
                    break L;
                case 14:
                    X = "toLocaleString";
                    id = Id_toLocaleString;
                    break L;
            }
            if (X != null && X != s && !X.equals(s)) id = 0;
        }
        return id;
    }

    private static final int Id_constructor = 1, Id_toString = 2, Id_toLocaleString = 3, Id_toSource = 4, Id_join = 5, Id_reverse = 6, Id_sort = 7, Id_push = 8, Id_pop = 9, Id_shift = 10, Id_unshift = 11, Id_splice = 12, Id_concat = 13, Id_slice = 14, Id_indexOf = 15, Id_lastIndexOf = 16, Id_every = 17, Id_filter = 18, Id_forEach = 19, Id_map = 20, Id_some = 21, Id_reduce = 22, Id_reduceRight = 23, MAX_PROTOTYPE_ID = 23;

    private static final int ConstructorId_join = -Id_join, ConstructorId_reverse = -Id_reverse, ConstructorId_sort = -Id_sort, ConstructorId_push = -Id_push, ConstructorId_pop = -Id_pop, ConstructorId_shift = -Id_shift, ConstructorId_unshift = -Id_unshift, ConstructorId_splice = -Id_splice, ConstructorId_concat = -Id_concat, ConstructorId_slice = -Id_slice, ConstructorId_indexOf = -Id_indexOf, ConstructorId_lastIndexOf = -Id_lastIndexOf, ConstructorId_every = -Id_every, ConstructorId_filter = -Id_filter, ConstructorId_forEach = -Id_forEach, ConstructorId_map = -Id_map, ConstructorId_some = -Id_some, ConstructorId_reduce = -Id_reduce, ConstructorId_reduceRight = -Id_reduceRight, ConstructorId_isArray = -24;

    /**
     * Internal representation of the JavaScript array's length property.
     */
    private long length;

    /**
     * Fast storage for dense arrays. Sparse arrays will use the superclass's
     * hashtable storage scheme.
     */
    private Object[] dense;

    /**
     * True if all numeric properties are stored in <code>dense</code>.
     */
    private boolean denseOnly;

    /**
     * The maximum size of <code>dense</code> that will be allocated initially.
     */
    private static int maximumInitialCapacity = 10000;

    /**
     * The default capacity for <code>dense</code>.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    /**
     * The factor to grow <code>dense</code> by.
     */
    private static final double GROW_FACTOR = 1.5;

    private static final int MAX_PRE_GROW_SIZE = (int) (Integer.MAX_VALUE / GROW_FACTOR);
}
