package metadata;

import net.jxta.document.Element;
import net.jxta.share.metadata.*;

/**
 * An implementation of the description metadata scheme.  The only metadata
 * stored is a plain english description of the content.  The format of a
 * description metadata element is as follows:<br><br>
 * <p/>
 * <code>&lt;metadata><br>
 * &nbsp;&nbsp;&lt;scheme><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name>description&lt;/name><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;location><i>location of resource used to parse
 * and query the scheme</i>&lt;/location><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;content-type>text/plain&lt;/content-type><br>
 * &nbsp;&nbsp;&lt;/scheme><br>
 * &nbsp;&nbsp;<i>plain english description</i><br>
 * &lt;/metadata></code>
 * <br><br>
 * For an example of this class in use, see <a href="../../../../../ref/src/net/jxta/test/ShareDemo.java">net.jxta.test.ShareDemo</a> and <a href="../../../../../ref/src/net/jxta/test/SearchDemo.java">net.jxta.test.SearchDemo</a>.
 *
 * @see ContentMetadataFactory
 * @see Keywords
 */
public class ConcreteElement extends ContentMetadata {

    /**
     * The content type used by the description metadata scheme.
     */
    public static final String DESCRIPTION_CONTENT_TYPE = "text/plain";

    private String value;

    /**
     * Create a MetadataQuery instance that can be used to query objects of
     * this class.
     *
     * @param query the string to search for
     */
    public static MetadataQuery newQuery(String parameter, String query) {
        return new ElementQuery(parameter, query);
    }

    /**
     * A simple MetadataQuery implementation that can be used to search a
     * Description metadata object for a string.
     */
    public static class ElementQuery implements MetadataQuery {

        private String queryString;

        private String parameter;

        /**@param queryString the string to search for.  It is assumed that
	 * this is not null, if it is, <code>queryMetadata()</code> will throw
	 * a NullPointerException.
	 */
        public ElementQuery(String parameter, String queryString) {
            this.parameter = parameter;
            this.queryString = queryString;
        }

        /**Test if a ContentMetadata object matches this query.
	 *@param metadata the Description object to query.
	 *@return <code>Integer.MAX_VALUE</code> if the query string was found 
	 * in <code>metadata</code>'s value, <code>Integer.MIN_VALUE</code> if
	 * not.
	 *@exception IllegalArgumentException if <code>metadata</code> is not
	 * an instance of the Description class.
	 */
        public int queryMetadata(ContentMetadata metadata) throws IllegalArgumentException {
            String desc;
            try {
                desc = metadata.getValue();
            } catch (ClassCastException cce) {
                throw new IllegalArgumentException("metadata is not of the ConcreteElement class");
            }
            if (desc.indexOf(queryString) != -1) {
                return Integer.MAX_VALUE;
            }
            return Integer.MIN_VALUE;
        }
    }

    /**
     * Return the ContentMetadataConstructor used to create instances of this
     * class.
     */
    public static ContentMetadataConstructor getConstructor() {
        return new ContentMetadataConstructor() {

            public ContentMetadata newInstance(Element metadataEl) throws IllegalArgumentException {
                return new Description(metadataEl);
            }
        };
    }

    /**
     * Create a new Description object with a given string as the metadata.
     *
     * @throws NullPointerException if <code>description</code> is null.
     */
    public ConcreteElement(String elementName, String value) {
        name = elementName;
        content_type = DESCRIPTION_CONTENT_TYPE;
        if (value == null) {
            value = "";
        }
        this.value = value;
    }

    /**
     * Create a Description instance from a <code>&lt;metadata></code> element.
     *
     * @throws IllegalArgumentException if <code>el<code> was formatted
     *                                  improperly.
     * @throws NullPointerException     if <code>el</code> is null.
     */
    public ConcreteElement(Element el) throws IllegalArgumentException {
        init(el);
        name = "ConcreteElement";
        content_type = DESCRIPTION_CONTENT_TYPE;
        value = (String) el.getValue();
        if (value == null) {
            throw new IllegalArgumentException("metadata element's value is null");
        }
    }

    /**
     * A function for generating safe copies of this ContentMetadata object.
     *
     * @return a safe copy of this ContentMetadata object
     */
    public Object clone() throws CloneNotSupportedException {
        ConcreteElement result = new ConcreteElement(name, value);
        result.location = location;
        return result;
    }

    public String getValue() {
        return value;
    }

    /**
     * Return the text of the description
     */
    public String getTag() {
        return name;
    }
}
