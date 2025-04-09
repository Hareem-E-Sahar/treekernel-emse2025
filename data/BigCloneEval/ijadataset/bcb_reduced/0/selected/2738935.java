package org.lindenb.xsd;

import java.lang.reflect.Constructor;
import java.util.regex.Pattern;

/**
 * XSDType
 * @author lindenb
 *
 */
public abstract class XSD<T> extends org.lindenb.sw.vocabulary.XSD {

    public abstract static class Restriction<X> extends XSD<X> {

        private XSD<X> delegate;

        protected Restriction(XSD<X> delegate) {
            super(delegate.getJavaClass());
            this.delegate = delegate;
        }

        @Override
        public String getLabel() {
            return getRestricted().getLabel();
        }

        @Override
        protected String getLocalName() {
            return getRestricted().getLocalName();
        }

        protected XSD<X> getRestricted() {
            return this.delegate;
        }
    }

    public abstract static class StringRestriction extends Restriction<String> {

        protected StringRestriction(XSD<String> delegate) {
            super(delegate);
        }
    }

    public static class LengthRestriction extends Restriction<String> {

        private int length;

        public LengthRestriction(XSD<String> delegate, int length) {
            super(delegate);
            this.length = length;
        }

        @Override
        public boolean validate(String value) {
            if (!getRestricted().validate(value)) return false;
            return value.length() == this.length;
        }

        @Override
        public String parse(String lexical) {
            String s = super.parse(lexical);
            if (s.length() != this.length) throw new IllegalArgumentException("length of argument is different from " + this.length);
            return s;
        }
    }

    /**
	 * AsString
	 * @author pierre
	 *
	 */
    public static class AsString extends XSD<java.lang.String> {

        AsString() {
            super(java.lang.String.class);
        }

        @Override
        protected java.lang.String getLocalName() {
            return "string";
        }

        @Override
        public java.lang.String getLabel() {
            return "A String";
        }

        @Override
        public boolean validate(java.lang.String value) {
            return value != null;
        }

        @Override
        public java.lang.String parse(java.lang.String value) {
            return value;
        }

        @Override
        public java.lang.String unparse(java.lang.String value) {
            return value;
        }
    }

    /**
	 * AsNormalizedString
	 * @author pierre
	 * The normalizedString data type also contains characters, but the XML processor will remove line feeds, carriage returns, and tab characters.
	 * Whitespace is normalized, but not collapsed. Thus, it is still possible for multiple consecutive whitespace characters to exist.
	 */
    public static class AsNormalizedString extends XSD.AsString {

        @Override
        protected java.lang.String getLocalName() {
            return "normalizedString";
        }

        @Override
        public java.lang.String getLabel() {
            return "A Normalized String";
        }

        private java.lang.String normalize(java.lang.String value) {
            return value.replaceAll("[\n\r\t]", " ");
        }

        @Override
        public boolean validate(java.lang.String value) {
            return value != null && value.equals(normalize(value));
        }

        @Override
        public java.lang.String parse(java.lang.String value) {
            return normalize(value);
        }

        @Override
        public java.lang.String unparse(java.lang.String value) {
            return value;
        }
    }

    /**
	 * AsToken
	 * @author pierre
	 * TThe token data type also contains characters, but the XML processor will remove line feeds, carriage returns, tabs, leading and trailing spaces, and multiple spaces.
	 */
    public static class AsToken extends XSD.AsString {

        @Override
        protected java.lang.String getLocalName() {
            return "token";
        }

        @Override
        public java.lang.String getLabel() {
            return "A Tokenized String";
        }

        private java.lang.String tokenize(java.lang.String value) {
            return value.trim().replaceAll("[ \n\r\t]+", " ");
        }

        @Override
        public boolean validate(java.lang.String value) {
            return value != null && value.equals(tokenize(value));
        }

        @Override
        public java.lang.String parse(java.lang.String value) {
            return tokenize(value);
        }

        @Override
        public java.lang.String unparse(java.lang.String value) {
            return value;
        }
    }

    /**
		 * AsURI
		 *
		 */
    public static class AsURI extends XSD<java.net.URI> {

        AsURI() {
            super(java.net.URI.class);
        }

        @Override
        public java.lang.String getLocalName() {
            return "anyURI";
        }

        @Override
        public java.lang.String getLabel() {
            return "A Uniform Resource Identifier Reference (URI)";
        }

        @Override
        protected java.lang.String mayTrim(java.lang.String s) {
            return s == null ? null : s.trim();
        }
    }

    /**
		 * AsURI
		 *
		 */
    public static class AsURL extends XSD<java.net.URL> {

        AsURL() {
            super(java.net.URL.class);
        }

        @Override
        public java.lang.String getLocalName() {
            return "url";
        }

        @Override
        public java.lang.String getLabel() {
            return "A URL";
        }

        @Override
        protected java.lang.String mayTrim(java.lang.String s) {
            return s == null ? null : s.trim();
        }
    }

    public static class AsBoolean extends XSD<java.lang.Boolean> {

        AsBoolean() {
            super(java.lang.Boolean.class);
        }

        @Override
        public java.lang.String getLocalName() {
            return "boolean";
        }

        @Override
        public java.lang.String getLabel() {
            return "A Boolean";
        }

        @Override
        public java.lang.Boolean parse(java.lang.String s) {
            if (s != null) {
                if (s.equalsIgnoreCase("true") || s.equals("1")) return java.lang.Boolean.TRUE; else if (s.equalsIgnoreCase("false") || s.equals("0")) return java.lang.Boolean.FALSE;
            }
            throw new NumberFormatException("bad boolean " + s);
        }

        @Override
        public boolean validate(java.lang.String value) {
            if (value == null) return false;
            try {
                parse(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public abstract static class AsNumber<T extends java.lang.Number> extends XSD<T> {

        protected AsNumber(Class<T> clazz) {
            super(clazz);
        }

        @Override
        protected java.lang.String mayTrim(java.lang.String s) {
            return s == null ? null : s.trim();
        }
    }

    /**
	 * AsShort
	 */
    public static class AsShort extends AsNumber<java.lang.Short> {

        AsShort() {
            super(java.lang.Short.class);
        }

        @Override
        protected java.lang.String getLocalName() {
            return "short";
        }

        @Override
        public java.lang.String getLabel() {
            return "A Short";
        }
    }

    /**
	 * AsInt
	 *
	 */
    public static class AsInteger extends AsNumber<java.lang.Integer> {

        AsInteger() {
            super(java.lang.Integer.class);
        }

        @Override
        protected java.lang.String getLocalName() {
            return "integer";
        }

        @Override
        public java.lang.String getLabel() {
            return "An Integer";
        }
    }

    /**
	 * AsLong
	 *
	 */
    public static class AsLong extends AsNumber<java.lang.Long> {

        AsLong() {
            super(java.lang.Long.class);
        }

        @Override
        protected java.lang.String getLocalName() {
            return "long";
        }

        @Override
        public java.lang.String getLabel() {
            return "A Long";
        }
    }

    /**
	 * AsFloat
	 * 
	 */
    public static class AsFloat extends AsNumber<java.lang.Float> {

        AsFloat() {
            super(java.lang.Float.class);
        }

        @Override
        protected java.lang.String getLocalName() {
            return "float";
        }

        @Override
        public java.lang.String getLabel() {
            return "A float";
        }
    }

    /**
	 * 
	 * AsDouble
	 *
	 */
    public static class AsDouble extends AsNumber<java.lang.Double> {

        AsDouble() {
            super(java.lang.Double.class);
        }

        @Override
        protected java.lang.String getLocalName() {
            return "double";
        }

        @Override
        public java.lang.String getLabel() {
            return "A double";
        }
    }

    public static final AsBoolean Boolean = new AsBoolean();

    public static final AsString String = new AsString();

    public static final AsShort Short = new AsShort();

    public static final AsInteger Integer = new AsInteger();

    public static final AsLong Long = new AsLong();

    public static final AsFloat Float = new AsFloat();

    public static final AsDouble Double = new AsDouble();

    public static final AsURI AnyURI = new AsURI();

    public static final AsURL AnyURL = new AsURL();

    public static final AsNormalizedString NormalizedString = new AsNormalizedString();

    public static final AsToken Token = new AsToken();

    private static XSD<?> TYPES[] = new XSD[] { Boolean, String, Short, Integer, Long, Float, Double, AnyURI, AnyURL, NormalizedString, Token };

    /** java class fior this xsd:type */
    private Class<T> clazz;

    /** constructor */
    protected XSD(Class<T> clazz) {
        this.clazz = clazz;
    }

    /** return a regex pattern or null for this xsd:type */
    public Pattern getPattern() {
        return null;
    }

    public abstract java.lang.String getLabel();

    protected abstract java.lang.String getLocalName();

    protected java.lang.String mayTrim(java.lang.String s) {
        return s;
    }

    public boolean validate(java.lang.String value) {
        if (value == null) return false;
        try {
            Constructor<T> cst = getJavaClass().getConstructor(String.class);
            cst.newInstance(mayTrim(value));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !(obj instanceof XSD)) return false;
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getJavaClass().hashCode();
    }

    /** returns the java class associated to this xsd:type */
    public final Class<T> getJavaClass() {
        return this.clazz;
    }

    /** parses a string to a &lt;T&gt; object.
	 * @throws an IllegalArgumentException if string cannot be casted */
    public T parse(java.lang.String lexical) {
        if (lexical == null) throw new NullPointerException("cannot parse null");
        try {
            Constructor<T> cst = getJavaClass().getConstructor(String.class);
            return cst.newInstance(mayTrim(lexical));
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** transform a T object to a string */
    public java.lang.String unparse(T s) {
        return s.toString();
    }

    public java.lang.String getNamespaceURI() {
        return XSD.NS + getLocalName();
    }

    @Override
    public java.lang.String toString() {
        return getNamespaceURI();
    }

    /** returns all XSD predefined types */
    public static XSD<?>[] getTypes() {
        return TYPES;
    }

    /** find a types by its XSD uri */
    public static XSD<?> findTypeByURI(String uri) {
        for (XSD<?> xsdt : getTypes()) {
            if (xsdt.getNamespaceURI().equals(uri)) return xsdt;
        }
        return null;
    }

    /** find a types by its XSD class */
    public static XSD<?> findTypeByClass(Class<?> c) {
        for (XSD<?> xsdt : getTypes()) {
            if (xsdt.getJavaClass().equals(c)) return xsdt;
        }
        return null;
    }
}
