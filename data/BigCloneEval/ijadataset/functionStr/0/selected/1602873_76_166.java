public class Test {    @SuppressWarnings("unchecked")
    public static String[] recursiveGetDBusType(Type c, boolean basic, int level) throws DBusException {
        if (out.length <= level) {
            StringBuffer[] newout = new StringBuffer[out.length];
            System.arraycopy(out, 0, newout, 0, out.length);
            out = newout;
        }
        if (null == out[level]) out[level] = new StringBuffer(); else out[level].delete(0, out[level].length());
        if (basic && !(c instanceof Class)) throw new DBusException(c + _(" is not a basic type"));
        if (c instanceof TypeVariable) out[level].append((char) Message.ArgumentType.VARIANT); else if (c instanceof GenericArrayType) {
            out[level].append((char) Message.ArgumentType.ARRAY);
            String[] s = recursiveGetDBusType(((GenericArrayType) c).getGenericComponentType(), false, level + 1);
            if (s.length != 1) throw new DBusException(_("Multi-valued array types not permitted"));
            out[level].append(s[0]);
        } else if ((c instanceof Class && DBusSerializable.class.isAssignableFrom((Class<? extends Object>) c)) || (c instanceof ParameterizedType && DBusSerializable.class.isAssignableFrom((Class<? extends Object>) ((ParameterizedType) c).getRawType()))) {
            Type[] newtypes = null;
            if (c instanceof Class) {
                for (Method m : ((Class<? extends Object>) c).getDeclaredMethods()) if (m.getName().equals("deserialize")) newtypes = m.getGenericParameterTypes();
            } else for (Method m : ((Class<? extends Object>) ((ParameterizedType) c).getRawType()).getDeclaredMethods()) if (m.getName().equals("deserialize")) newtypes = m.getGenericParameterTypes();
            if (null == newtypes) throw new DBusException(_("Serializable classes must implement a deserialize method"));
            String[] sigs = new String[newtypes.length];
            for (int j = 0; j < sigs.length; j++) {
                String[] ss = recursiveGetDBusType(newtypes[j], false, level + 1);
                if (1 != ss.length) throw new DBusException(_("Serializable classes must serialize to native DBus types"));
                sigs[j] = ss[0];
            }
            return sigs;
        } else if (c instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) c;
            if (p.getRawType().equals(Map.class)) {
                out[level].append("a{");
                Type[] t = p.getActualTypeArguments();
                try {
                    String[] s = recursiveGetDBusType(t[0], true, level + 1);
                    if (s.length != 1) throw new DBusException(_("Multi-valued array types not permitted"));
                    out[level].append(s[0]);
                    s = recursiveGetDBusType(t[1], false, level + 1);
                    if (s.length != 1) throw new DBusException(_("Multi-valued array types not permitted"));
                    out[level].append(s[0]);
                } catch (ArrayIndexOutOfBoundsException AIOOBe) {
                    if (AbstractConnection.EXCEPTION_DEBUG && Debug.debug) Debug.print(Debug.ERR, AIOOBe);
                    throw new DBusException(_("Map must have 2 parameters"));
                }
                out[level].append('}');
            } else if (List.class.isAssignableFrom((Class<? extends Object>) p.getRawType())) {
                for (Type t : p.getActualTypeArguments()) {
                    if (Type.class.equals(t)) out[level].append((char) Message.ArgumentType.SIGNATURE); else {
                        String[] s = recursiveGetDBusType(t, false, level + 1);
                        if (s.length != 1) throw new DBusException(_("Multi-valued array types not permitted"));
                        out[level].append((char) Message.ArgumentType.ARRAY);
                        out[level].append(s[0]);
                    }
                }
            } else if (p.getRawType().equals(Variant.class)) {
                out[level].append((char) Message.ArgumentType.VARIANT);
            } else if (DBusInterface.class.isAssignableFrom((Class<? extends Object>) p.getRawType())) {
                out[level].append((char) Message.ArgumentType.OBJECT_PATH);
            } else if (Tuple.class.isAssignableFrom((Class<? extends Object>) p.getRawType())) {
                Type[] ts = p.getActualTypeArguments();
                Vector<String> vs = new Vector<String>();
                for (Type t : ts) for (String s : recursiveGetDBusType(t, false, level + 1)) vs.add(s);
                return vs.toArray(new String[0]);
            } else throw new DBusException(_("Exporting non-exportable parameterized type ") + c);
        } else if (c.equals(Byte.class)) out[level].append((char) Message.ArgumentType.BYTE); else if (c.equals(Byte.TYPE)) out[level].append((char) Message.ArgumentType.BYTE); else if (c.equals(Boolean.class)) out[level].append((char) Message.ArgumentType.BOOLEAN); else if (c.equals(Boolean.TYPE)) out[level].append((char) Message.ArgumentType.BOOLEAN); else if (c.equals(Short.class)) out[level].append((char) Message.ArgumentType.INT16); else if (c.equals(Short.TYPE)) out[level].append((char) Message.ArgumentType.INT16); else if (c.equals(UInt16.class)) out[level].append((char) Message.ArgumentType.UINT16); else if (c.equals(Integer.class)) out[level].append((char) Message.ArgumentType.INT32); else if (c.equals(Integer.TYPE)) out[level].append((char) Message.ArgumentType.INT32); else if (c.equals(UInt32.class)) out[level].append((char) Message.ArgumentType.UINT32); else if (c.equals(Long.class)) out[level].append((char) Message.ArgumentType.INT64); else if (c.equals(Long.TYPE)) out[level].append((char) Message.ArgumentType.INT64); else if (c.equals(UInt64.class)) out[level].append((char) Message.ArgumentType.UINT64); else if (c.equals(Double.class)) out[level].append((char) Message.ArgumentType.DOUBLE); else if (c.equals(Double.TYPE)) out[level].append((char) Message.ArgumentType.DOUBLE); else if (c.equals(Float.class) && AbstractConnection.FLOAT_SUPPORT) out[level].append((char) Message.ArgumentType.FLOAT); else if (c.equals(Float.class)) out[level].append((char) Message.ArgumentType.DOUBLE); else if (c.equals(Float.TYPE) && AbstractConnection.FLOAT_SUPPORT) out[level].append((char) Message.ArgumentType.FLOAT); else if (c.equals(Float.TYPE)) out[level].append((char) Message.ArgumentType.DOUBLE); else if (c.equals(String.class)) out[level].append((char) Message.ArgumentType.STRING); else if (c.equals(Variant.class)) out[level].append((char) Message.ArgumentType.VARIANT); else if (c instanceof Class && DBusInterface.class.isAssignableFrom((Class<? extends Object>) c)) out[level].append((char) Message.ArgumentType.OBJECT_PATH); else if (c instanceof Class && Path.class.equals((Class<? extends Object>) c)) out[level].append((char) Message.ArgumentType.OBJECT_PATH); else if (c instanceof Class && ObjectPath.class.equals((Class<? extends Object>) c)) out[level].append((char) Message.ArgumentType.OBJECT_PATH); else if (c instanceof Class && ((Class<? extends Object>) c).isArray()) {
            if (Type.class.equals(((Class<? extends Object>) c).getComponentType())) out[level].append((char) Message.ArgumentType.SIGNATURE); else {
                out[level].append((char) Message.ArgumentType.ARRAY);
                String[] s = recursiveGetDBusType(((Class<? extends Object>) c).getComponentType(), false, level + 1);
                if (s.length != 1) throw new DBusException(_("Multi-valued array types not permitted"));
                out[level].append(s[0]);
            }
        } else if (c instanceof Class && Struct.class.isAssignableFrom((Class<? extends Object>) c)) {
            out[level].append((char) Message.ArgumentType.STRUCT1);
            Type[] ts = Container.getTypeCache(c);
            if (null == ts) {
                Field[] fs = ((Class<? extends Object>) c).getDeclaredFields();
                ts = new Type[fs.length];
                for (Field f : fs) {
                    Position p = f.getAnnotation(Position.class);
                    if (null == p) continue;
                    ts[p.value()] = f.getGenericType();
                }
                Container.putTypeCache(c, ts);
            }
            for (Type t : ts) if (t != null) for (String s : recursiveGetDBusType(t, false, level + 1)) out[level].append(s);
            out[level].append(')');
        } else {
            throw new DBusException(_("Exporting non-exportable type ") + c);
        }
        if (Debug.debug) Debug.print(Debug.VERBOSE, "Converted Java type: " + c + " to D-Bus Type: " + out[level]);
        return new String[] { out[level].toString() };
    }
}