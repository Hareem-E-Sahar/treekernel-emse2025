package alto.sys;

/**
 * <p> A component that has configuration properties implements this
 * interface.  A component that is not specifically the root of a
 * system (host process main) implements the {@link Properties$Child}
 * interface.  </p>
 * 
 * <p> The component is expected to employ an instance of {@link
 * java.util.Properties} internally, and an inner class here provides
 * common {@link Properties$Tools} for implementing this interface in
 * this model. </p>
 * 
 * <p> The component properties are loaded and stored in the {@link
 * java.util.Properties} XML file format.  This schema is vaguely
 * similar to a feed format like RSS. </p>
 * 
 * <p> There are some extended "pull" for type methods here,
 * permitting the implementor and its users to pull specific types
 * from property values.  There are "data" types and "class" types
 * among them, where a "data" type has a string argument constructor
 * that accepts whatever it outputs from its {@link
 * java.lang.Object#toString()} method.  These strings must be XML
 * Text (PCDATA) compatible.  The data objects are cached within the
 * properties map itself.  The class types cache only the class
 * object, their instances (from a simple no arg constructor) are
 * maintained externally.  </p>
 * 
 * @author jdp
 * @since 1.5
 */
public interface Properties extends IO {

    /**
     * A component with a reference to the broader scope of its
     * systemic context implements this interface to return a
     * reference to its parent container.
     */
    public interface Child extends Properties {

        /**
         * @return Parent defined by corresponding setter method.
         */
        public Properties getPropertiesParent();

        /**
         * @param parent Container defining itself. 
         */
        public void setPropertiesParent(Properties parent);
    }

    /**
     * <p> Common semantics. </p>
     */
    public abstract static class Tools extends java.lang.Object {

        public static final java.lang.Class CLASS_STRING = java.lang.String.class;

        public static final java.lang.Class[] CTOR_ARGS_D = new java.lang.Class[] { CLASS_STRING };

        public static final void ImportPropertiesXML(java.util.Properties properties, IO.Source url) {
            if (null == properties || null == url) throw new Error.Argument("Null argument."); else {
                java.io.InputStream in = null;
                try {
                    in = url.openInputStream();
                    properties.loadFromXML(in);
                } catch (java.io.IOException exc) {
                } finally {
                    if (null != in) {
                        try {
                            in.close();
                        } catch (java.io.IOException ignore) {
                        }
                        in = null;
                    }
                }
            }
        }

        public static final void ImportPropertiesXML(java.util.Properties properties, java.io.InputStream in) throws java.io.IOException {
            if (null == properties || null == in) throw new Error.Argument("Null argument."); else properties.loadFromXML(in);
        }

        public static final void ExportPropertiesXML(java.util.Properties properties, java.lang.String comment_location, IO.Target url) {
            if (null == properties || null == url) throw new Error.Argument("Null argument."); else {
                java.io.OutputStream out = null;
                try {
                    out = url.openOutputStream();
                    properties.storeToXML(out, comment_location);
                } catch (java.io.IOException exc) {
                } finally {
                    if (null != out) {
                        try {
                            out.close();
                        } catch (java.io.IOException ignore) {
                        }
                        out = null;
                    }
                }
            }
        }

        public static final void ExportPropertiesXML(java.util.Properties properties, java.lang.String comment_location, java.io.OutputStream out) throws java.io.IOException {
            if (null == properties || null == out) throw new Error.Argument("Null argument."); else properties.storeToXML(out, comment_location);
        }

        public static final java.lang.Object GetProperty(java.util.Properties properties, java.lang.String name) {
            if (null == properties || null == name) throw new Error.Argument("Null argument."); else return properties.get(name);
        }

        public static final java.lang.Object GetPropertyData(java.util.Properties properties, java.lang.String name, java.lang.Class value_class) {
            if (null == properties || null == name || null == value_class) throw new Error.Argument("Null argument."); else {
                java.lang.Object value = properties.get(name);
                if (value instanceof java.lang.String) {
                    try {
                        java.lang.reflect.Constructor ctor = value_class.getConstructor(CTOR_ARGS_D);
                        java.lang.Object class_value = ctor.newInstance(new java.lang.Object[] { value });
                        return class_value;
                    } catch (java.lang.Throwable ignore) {
                        return value;
                    }
                } else return value;
            }
        }

        public static final java.lang.Boolean GetPropertyDataBoolean(java.util.Properties properties, java.lang.String name) {
            java.lang.Object value = GetProperty(properties, name);
            if (value instanceof java.lang.Boolean) {
                return (java.lang.Boolean) value;
            } else if (value instanceof java.lang.String) {
                java.lang.String value_string = (java.lang.String) value;
                java.lang.Boolean value_boolean;
                if ("true".equalsIgnoreCase(value_string)) value_boolean = java.lang.Boolean.TRUE; else value_boolean = java.lang.Boolean.FALSE;
                properties.put(name, value_boolean);
                return value_boolean;
            } else return null;
        }

        public static final java.lang.Integer GetPropertyDataInteger(java.util.Properties properties, java.lang.String name) {
            java.lang.Object value = GetProperty(properties, name);
            if (value instanceof java.lang.Integer) {
                return (java.lang.Integer) value;
            } else if (value instanceof java.lang.String) {
                java.lang.String value_string = (java.lang.String) value;
                java.lang.Integer value_integer = new java.lang.Integer(value_string);
                properties.put(name, value_integer);
                return value_integer;
            } else return null;
        }

        public static final java.lang.Float GetPropertyDataFloat(java.util.Properties properties, java.lang.String name) {
            java.lang.Object value = GetProperty(properties, name);
            if (value instanceof java.lang.Float) {
                return (java.lang.Float) value;
            } else if (value instanceof java.lang.String) {
                java.lang.String value_string = (java.lang.String) value;
                java.lang.Float value_float = new java.lang.Float(value_string);
                properties.put(name, value_float);
                return value_float;
            } else return null;
        }

        public static final java.lang.Class GetPropertyClass(java.util.Properties properties, java.lang.String name) {
            java.lang.Object value = GetProperty(properties, name);
            if (value instanceof java.lang.Class) {
                return (java.lang.Class) value;
            } else if (value instanceof java.lang.String) {
                java.lang.String value_string = (java.lang.String) value;
                try {
                    return java.lang.Class.forName(value_string);
                } catch (java.lang.ClassNotFoundException exc) {
                    return null;
                }
            } else return null;
        }

        public static final java.lang.Object GetPropertyClassInstance(java.util.Properties properties, java.lang.String name) {
            java.lang.Class value_class = GetPropertyClass(properties, name);
            if (null != value_class) return ClassInstance(value_class); else return null;
        }

        public static final java.lang.Object ClassInstance(java.lang.Class value_class) {
            if (null != value_class) {
                try {
                    java.lang.Object value_object = value_class.newInstance();
                    return value_object;
                } catch (java.lang.InstantiationException exc) {
                    throw new Error.Argument(value_class.getName(), exc);
                } catch (java.lang.IllegalAccessException exc) {
                    throw new Error.State(value_class.getName(), exc);
                }
            } else return null;
        }

        public static final java.net.URL GetPropertyDataURL(java.util.Properties properties, java.lang.String name) {
            java.lang.Object value = GetProperty(properties, name);
            if (value instanceof java.net.URL) {
                return (java.net.URL) value;
            } else if (value instanceof java.lang.String) {
                java.lang.String value_string = (java.lang.String) value;
                try {
                    java.net.URL value_url = new java.net.URL(value_string);
                    properties.put(name, value_url);
                    return value_url;
                } catch (java.net.MalformedURLException exc) {
                    throw new Error.Argument(value_string, exc);
                }
            } else return null;
        }

        public static final java.net.InetAddress GetPropertyInetAddress(java.util.Properties properties, java.lang.String name) {
            java.lang.Object value = GetProperty(properties, name);
            if (value instanceof java.net.InetAddress) {
                return (java.net.InetAddress) value;
            } else if (value instanceof java.lang.String) {
                java.lang.String value_string = (java.lang.String) value;
                try {
                    int filter = value_string.indexOf('/');
                    if (0 < filter) value_string = value_string.substring(0, filter);
                    java.net.InetAddress value_host = java.net.InetAddress.getByName(value_string);
                    return value_host;
                } catch (java.net.UnknownHostException exc) {
                    throw new Error.Argument(value_string, exc);
                }
            } else return null;
        }

        public static final void SetProperty(java.util.Properties properties, java.lang.String name, java.lang.Object value) {
            if (null == properties || null == name) throw new Error.Argument("Null argument."); else if (null == value) properties.remove(name); else {
                properties.put(name, value);
            }
        }

        public static final void RemoveProperty(java.util.Properties properties, java.lang.String name) {
            if (null == properties || null == name) throw new Error.Argument("Null argument."); else {
                properties.remove(name);
            }
        }
    }

    /**
     * <p> The convention here is that this string be an absolute path
     * expression that can be employed relative to a web based
     * application storage service.  Its file extension suffix is
     * "xml", subject to being a valid reference. </p>
     * 
     * @return String defined by "set properties location" or a class
     * defined constant for the properties comment string. 
     */
    public java.lang.String getPropertiesLocation();

    /**
     * Possible No Op: A class that has an absolute constant value may
     * ignore this call.
     * 
     * @param location A location string is an absolute or relative
     * URI (by our convention here) that is written into the
     * properties comments in the "export properties xml" method.
     */
    public void setPropertiesLocation(java.lang.String location);

    /**
     * @param url Input source for {@link java.util.Properties} XML
     * format.
     */
    public void importPropertiesXML(IO.Source url);

    /**
     * @param in An input stream in {@link java.util.Properties} XML
     * format.
     * @exception java.io.IOException Error reading properties.
     */
    public void importPropertiesXML(java.io.InputStream in) throws java.io.IOException;

    /**
     * @param url Output target for {@link java.util.Properties} XML
     * format.
     */
    public void exportPropertiesXML(IO.Target url);

    /**
     * @param out An output stream for {@link java.util.Properties}
     * XML format.
     * @exception java.io.IOException Error writing properties.
     */
    public void exportPropertiesXML(java.io.OutputStream out) throws java.io.IOException;

    /**
     * @param name A property name string (dot style).
     * @return The currently mapped value.
     */
    public java.lang.Object getProperty(java.lang.String name);

    /**
     * <p> A data class like {@link java.lang.Integer} has a public
     * constructor with a string argument which is used here.  The
     * {@link java.lang.Object#toString()} method of a data class
     * returns the same strings that it accepts as input to its data
     * constructor.  </p>
     * 
     * @param name Property name
     * @param dclass A data class must accept the all of the strings
     * returned from its {@link java.lang.Object#toString()} as
     * arguments to its constructor.  These strings must be XML Text
     * (PCDATA) compatible.
     * @return Pull and persist an instance of the argument class from
     * a string returned by {@link #getProperty(java.lang.String)}.
     * Or, null if that object is not a string.   
     */
    public java.lang.Object getPropertyData(java.lang.String name, java.lang.Class dclass);

    /**
     * @param name Property name
     * @return Pull and persist a boolean from a string returned by
     * {@link #getProperty(java.lang.String)}.  Or, null if that
     * object is not a string.
     */
    public java.lang.Boolean getPropertyDataBoolean(java.lang.String name);

    /**
     * @param name Property name
     * @return Pull and persist an integer from a string returned by
     * {@link #getProperty(java.lang.String)}.  Or, null if that
     * object is not a string.
     */
    public java.lang.Integer getPropertyDataInteger(java.lang.String name);

    /**
     * @param name Property name
     * @return Pull and persist a float from a string returned by
     * {@link #getProperty(java.lang.String)}.  Or, null if that
     * object is not a string.
     */
    public java.lang.Float getPropertyDataFloat(java.lang.String name);

    /**
     * <p> An instance of a class name string value may be constructed
     * using a simple, no argument constructor.  </p>
     * 
     * @param name Property name
     * @return Pull a class from a string returned by {@link
     * #getProperty(java.lang.String)}.  Or, null if that object is
     * not a string.
     * 
     * @see getPropertyClassInstance(java.lang.String)
     */
    public java.lang.Class getPropertyClass(java.lang.String name);

    /**
     * <p> An instance of a class name string value is constructed
     * using a simple, no argument constructor.  </p>
     * 
     * @param name Property name
     * @return Instantiate a class returned by {@link
     * #getPropertyClass(java.lang.String)}.  Or, null.
     * 
     * @see #getPropertyClass(java.lang.String)
     */
    public java.lang.Object getPropertyClassInstance(java.lang.String name);

    /**
     * @param name Property name
     * @return Pull and persist a url from a string returned by
     * {@link #getProperty(java.lang.String)}.  Or, null if that
     * object is not a string.
     */
    public java.net.URL getPropertyDataURL(java.lang.String name);

    /**
     * @param name Property name
     * @return Pull a host address from a string returned by {@link
     * #getProperty(java.lang.String)}.  Or, null if that object is
     * not a string.
     */
    public java.net.InetAddress getPropertyInetAddress(java.lang.String name);

    /**
     * @param name A property name string in dotted notation, employed
     * varbatim
     * @param value Any value to be mapped, it's "to string" method
     * will be employed for its export value.
     */
    public void setProperty(java.lang.String name, java.lang.Object value);

    /**
     * @param name A property name string in dotted notation, employed verbatim
     */
    public void removeProperty(java.lang.String name);
}
