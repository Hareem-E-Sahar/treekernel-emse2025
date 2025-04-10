package org.eclipse.emf.ecore.resource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

/**
 * A converter to normalize a URI or to produce an input or output stream for a URI.
 * <p>
 * A resource set provides {@link ResourceSet#getURIConverter one} of these
 * for use by it's {@link ResourceSet#getResources resources}
 * when they are {@link Resource#save(java.util.Map) serialized} and {@link Resource#load(java.util.Map) deserialized}.
 * A resource set also uses this directly when it {@link ResourceSet#getResource looks up} a resource:
 * a resource is considered a match if {@link Resource#getURI it's URI}, 
 * and the URI being looked up, 
 * {@link #normalize normalize} to {@link URI#equals(Object) equal} URIs.
 * Clients must extend the default {@link org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl implementation},
 * since methods can and will be added to this API.
 * </p>
 * @see ResourceSet#getURIConverter()
 * @see URIHandler
 * @see ContentHandler
 */
public interface URIConverter {

    /**
   * An option used to pass the calling URIConverter to the {@link URIHandler}s.
   * @since 2.4
   */
    String OPTION_URI_CONVERTER = "URI_CONVERTER";

    /**
   * An option to pass a {@link Map Map&lt;Object, Object>} to any of the URI converter's methods 
   * in order to yield results in addition to the returned value of the method.
   * @since 2.4
   */
    String OPTION_RESPONSE = "RESPONSE";

    /**
   * A property of the {@link #OPTION_RESPONSE response option} 
   * used to yield the {@link #ATTRIBUTE_TIME_STAMP time stamp} associated
   * with the creation of an {@link #createInputStream(URI, Map) input} or an {@link #createOutputStream(URI, Map) output} stream.
   * This is typically used by resource {@link Resource#load(Map) load} and {@link Resource#save(Map) save} 
   * in order to set the {@link Resource#getTimeStamp()}.
   * @since 2.4
   */
    String RESPONSE_TIME_STAMP_PROPERTY = "TIME_STAMP";

    /**
   * Returns the normalized form of the URI.
   * <p>
   * This may, in theory, do absolutely anything.
   * Default behaviour includes 
   * applying URI {@link URIConverter#getURIMap mapping},
   * assuming <code>"file:"</code> protocol 
   * for a {@link URI#isRelative relative} URI with a {@link URI#hasRelativePath relative path}:
   *<pre>
   *  ./WhateverDirectory/Whatever.file 
   *    -> 
   *  file:./WhateverDirectory/Whatever.file
   *</pre>
   * and assuming <code>"platform:/resource"</code> protocol 
   * for a relative URI with an {@link URI#hasAbsolutePath absolute path}:
   *<pre>
   *  /WhateverRelocatableProject/Whatever.file 
   *    -> 
   *  platform:/resource/WhateverRelocatableProject/Whatever.file
   *</pre>
   * </p>
   * <p>
   * It is important to emphasize that normalization can result it loss of information.
   * The normalized URI should generally be used only for comparison and for access to input or output streams.
   * </p>
   * @param uri the URI to normalize.
   * @return the normalized form.
   * @see org.eclipse.emf.ecore.plugin.EcorePlugin#getPlatformResourceMap
   */
    URI normalize(URI uri);

    /**
   * Returns the map used for remapping a logical URI to a physical URI when {@link #normalize normalizing}.
   * <p>
   * An implementation will typically also delegate to the {@link URIConverter#URI_MAP global} map,
   * so registrations made in this map are <em>local</em> to this URI converter,
   * i.e., they augment or override those of the global map.
   * </p>
   * <p>
   * The map generally specifies instance to instance mapping,
   * except for the case that both the key URI and the value URI end with "/", 
   * which specifies a folder to folder mapping.
   * A folder mapping will remap any URI that has the key as its {@link URI#replacePrefix prefix}, 
   * e.g., if the map contains:
   *<pre>
   *  http://www.example.com/ -> platform:/resource/example/
   *</pre>
   * then the URI
   *<pre>
   *  http://www.example.com/a/b/c.d
   *</pre>
   * will map to 
   *<pre>
   *  platform:/resource/example/a/b/c.d
   *</pre>
   * A matching instance mapping is considered first.
   * If there isn't one, the folder mappings are considered starting with the {@link URI#segmentCount() longest} prefix. 
   * </p>
   * @see #normalize(URI)
   * @see #URI_MAP
   * @return the map used for remapping a logical URI to a physical URI.
   */
    Map<URI, URI> getURIMap();

    /**
   * The global static URI map.
   * Registrations made in this instance will (typically) be available
   * for {@link URIConverter#normalize use} by any URI converter.
   * It is populated by URI mappings registered via
   * {@link org.eclipse.emf.ecore.plugin.EcorePlugin.Implementation#startup() plugin registration}.
   * @see #normalize(URI)
   */
    Map<URI, URI> URI_MAP = org.eclipse.emf.ecore.resource.impl.URIMappingRegistryImpl.INSTANCE.map();

    /**
   * Returns the list of {@link URIHandler}s.
   * @return the list of {@link URIHandler}s.
   * @since 2.4
   */
    EList<URIHandler> getURIHandlers();

    /**
   * Returns the first URI handler in the {@link #getURIHandler(URI) list} of URI handlers which {@link URIHandler#canHandle(URI) can handle} the given URI.
   * @param uri the URI for which to find a handler.
   * @return the first URI handler in the list of URI handlers which can handle the given URI.
   * @throws RuntimeException if no matching handler is found.
   * @since 2.4 
   */
    URIHandler getURIHandler(URI uri);

    /**
   * Returns the list of {@link ContentHandler}s.
   * @return the list of {@link ContentHandler}s.
   * @since 2.4
   */
    EList<ContentHandler> getContentHandlers();

    /**
   * Creates an input stream for the URI and returns it;
   * it has the same effect as calling {@link #createInputStream(URI, Map) createInputStream(uri, null)}.
   * @param uri the URI for which to create the input stream.
   * @return an open input stream.
   * @exception IOException if there is a problem obtaining an open input stream.
   * @see #createInputStream(URI, Map)
   */
    InputStream createInputStream(URI uri) throws IOException;

    /**
   * Creates an input stream for the URI and returns it.
   * <p>
   * It {@link #normalize normalizes} the URI and uses that as the basis for further processing.
   * Special requirements, such as an Eclipse file refresh, 
   * are handled by the {@link org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl default implementation}.
   * </p>
   * @param uri the URI for which to create the input stream.
   * @param options a map of options to influence the kind of stream that is returned; unrecognized options are ignored and <code>null</code> is permitted.
   * @return an open input stream.
   * @exception IOException if there is a problem obtaining an open input stream.
   * @since 2.4
   */
    InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException;

    /**
   * An interface that is optionally implemented by the input streams returned from 
   * {@link URIConverter#createInputStream(URI)} and {@link URIConverter#createInputStream(URI, Map)}.
   * @see ReadableInputStream
   */
    interface Readable {

        /**
     * Returns a reader that provides access to the same underlying data as the input stream itself.
     * @return a reader that provides access to the same underlying data as the input stream itself.
     */
        Reader asReader();

        /**
     * Returns the encoding used to convert the reader's characters to bytes.
     * @return the encoding used to convert the reader's characters to bytes.
     */
        String getEncoding();
    }

    /**
   * A wrapper around a reader that implements an input stream but can be unwrapped to access the reader directly.
   */
    class ReadableInputStream extends InputStream implements Readable {

        private static final Pattern XML_HEADER = Pattern.compile("<\\?xml\\s+(?:version\\s*=\\s*\"[^\"]*\"\\s+)encoding\\s*=\\s*\"\\s*([^\\s\"]*)\"\\s*\\?>");

        public static String getEncoding(String xmlString) {
            Matcher matcher = XML_HEADER.matcher(xmlString);
            return matcher.lookingAt() ? matcher.group(1) : null;
        }

        /**
     * @since 2.4
     */
        public static String getEncoding(Reader xmlReader) {
            try {
                xmlReader.mark(100);
                char[] buffer = new char[100];
                int length = xmlReader.read(buffer);
                if (length > -1) {
                    Matcher matcher = XML_HEADER.matcher(new String(buffer, 0, length));
                    return matcher.lookingAt() ? matcher.group(1) : null;
                } else {
                    return null;
                }
            } catch (IOException exception) {
                return null;
            } finally {
                try {
                    xmlReader.reset();
                } catch (IOException exception) {
                }
            }
        }

        protected String encoding;

        protected Reader reader;

        protected Buffer buffer;

        public ReadableInputStream(Reader reader, String encoding) {
            super();
            this.reader = reader;
            this.encoding = encoding;
        }

        /**
     * @since 2.4
     */
        public ReadableInputStream(Reader xmlReader) {
            super();
            this.reader = xmlReader.markSupported() ? xmlReader : new BufferedReader(xmlReader);
            this.encoding = getEncoding(this.reader);
        }

        public ReadableInputStream(String string, String encoding) {
            this(new StringReader(string), encoding);
        }

        public ReadableInputStream(String xmlString) {
            this(new StringReader(xmlString), getEncoding(xmlString));
        }

        @Override
        public int read() throws IOException {
            if (buffer == null) {
                buffer = new Buffer(100);
            }
            return buffer.read();
        }

        public Reader asReader() {
            return reader;
        }

        public String getEncoding() {
            return encoding;
        }

        @Override
        public void close() throws IOException {
            super.close();
            reader.close();
        }

        @Override
        public synchronized void reset() throws IOException {
            super.reset();
            reader.reset();
        }

        protected class Buffer extends ByteArrayOutputStream {

            protected int index;

            protected char[] characters;

            protected OutputStreamWriter writer;

            public Buffer(int size) throws IOException {
                super(size);
                characters = new char[size];
                writer = new OutputStreamWriter(this, encoding);
            }

            public int read() throws IOException {
                if (index < count) {
                    return buf[index++];
                } else {
                    index = 0;
                    reset();
                    int readCount = reader.read(characters);
                    if (readCount < 0) {
                        return -1;
                    } else {
                        writer.write(characters, 0, readCount);
                        writer.flush();
                        return buf[index++];
                    }
                }
            }
        }
    }

    /**
   * Creates an output stream for the URI and returns it;
   * it has the same effect as calling {@link #createOutputStream(URI, Map) createOutputStream(uri, null)}.
   * @return an open output stream.
   * @exception IOException if there is a problem obtaining an open output stream.
   * @see #createOutputStream(URI, Map)
   */
    OutputStream createOutputStream(URI uri) throws IOException;

    /**
   * Creates an output stream for the URI and returns it.
   * <p>
   * It {@link #normalize normalizes} the URI and uses that as the basis for further processing.
   * Special requirements, such as an Eclipse file refresh, 
   * are handled by the {@link org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl default implementation}.
   * </p>
   * @param uri the URI for which to create the output stream.
   * @param options a map of options to influence the kind of stream that is returned; unrecognized options are ignored and <code>null</code> is permitted.
   * @return an open output stream.
   * @exception IOException if there is a problem obtaining an open output stream.
   * @since 2.4
   */
    OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException;

    /**
   * An interface that is optionally implemented by the output streams returned from 
   * {@link URIConverter#createOutputStream(URI)} and {@link URIConverter#createOutputStream(URI, Map)}.
   * @see WriteableOutputStream
   */
    interface Writeable {

        /**
     * Returns a writer that provides access to the same underlying data as the input stream itself.
     * @return a writer that provides access to the same underlying data as the input stream itself.
     */
        Writer asWriter();

        /**
     * Returns the encoding used to convert the writer's bytes to characters.
     * @return the encoding used to convert the writer's bytes to characters.
     */
        String getEncoding();
    }

    /**
   * A wrapper around a writer that implements an output stream but can be unwrapped to access the writer directly.
   */
    static class WriteableOutputStream extends OutputStream implements Writeable {

        protected String encoding;

        protected Writer writer;

        protected Buffer buffer;

        public WriteableOutputStream(Writer writer, String encoding) {
            super();
            this.writer = writer;
            this.encoding = encoding;
        }

        @Override
        public void write(int b) throws IOException {
            if (buffer == null) {
                buffer = new Buffer(100);
            }
            buffer.write(b);
        }

        public Writer asWriter() {
            return writer;
        }

        public String getEncoding() {
            return encoding;
        }

        @Override
        public void close() throws IOException {
            super.close();
            writer.close();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            buffer.flush();
            writer.flush();
        }

        protected class Buffer extends ByteArrayInputStream {

            protected int index;

            protected char[] characters;

            protected InputStreamReader reader;

            public Buffer(int size) throws IOException {
                super(new byte[size], 0, 0);
                characters = new char[size];
                reader = new InputStreamReader(this, encoding);
            }

            public void write(int b) throws IOException {
                if (count < buf.length) {
                    buf[count++] = (byte) b;
                } else {
                    int readCount = reader.read(characters);
                    if (readCount > 0) {
                        writer.write(characters, 0, readCount);
                    }
                    count = 0;
                    index = 0;
                    pos = 0;
                    write(b);
                }
            }

            public void flush() throws IOException {
                int readCount = reader.read(characters);
                if (readCount > 0) {
                    writer.write(characters, 0, readCount);
                }
                count = 0;
                index = 0;
                pos = 0;
            }
        }
    }

    /**
   * An interface to be implemented by encryption service providers.
   * @since 2.2.0
   */
    interface Cipher {

        /**
     * Encrypts the specified output stream.
     * @param outputStream
     * @return an encrypted output stream
     */
        OutputStream encrypt(OutputStream outputStream) throws Exception;

        /**
     * This method is invoked after the encrypted output stream is used
     * allowing the Cipher implementation to do any maintenance work required,
     * such as flushing an internal cache.
     * @param outputStream the encrypted stream returned by {@link #encrypt(OutputStream)}.
     */
        void finish(OutputStream outputStream) throws Exception;

        /**
     * Decrypts the specified input stream.
     * @param inputStream
     * @return a decrypted input stream
     */
        InputStream decrypt(InputStream inputStream) throws Exception;

        /**
     * This method is invoked after the decrypted input stream is used
     * allowing the Cipher implementation to do any maintenance work required,
     * such as flushing internal cache.
     * @param inputStream the stream returned by {@link #decrypt(InputStream)}.
     */
        void finish(InputStream inputStream) throws Exception;
    }

    /**
   * Deletes the contents of the given URI. 
   * @param uri the URI to consider.
   * @param options options to influence how the contents are deleted, or <code>null</code> if there are no options.
   * @throws IOException if there is a problem deleting the contents.
   * @since 2.4
   */
    void delete(URI uri, Map<?, ?> options) throws IOException;

    /**
   * Returns a map from String properties to their corresponding values representing a description the given URI's contents.
   * See the {@link ContentHandler#contentDescription(URI, InputStream, Map, Map) content handler} for more details.
   * @param uri the URI to consider.
   * @param options options to influence how the content description is determined, or <code>null</code> if there are no options.
   * @return a map from String properties to their corresponding values representing a description the given URI's contents.
   * @throws IOException if there is a problem accessing the contents.
   * @see ContentHandler#contentDescription(URI, InputStream, Map, Map)
   * @since 2.4
   */
    Map<String, ?> contentDescription(URI uri, Map<?, ?> options) throws IOException;

    /**
   * Returns whether the given URI has contents.
   * If the URI {@link #exists(URI, Map) exists}
   * it will be possible to {@link #createOutputStream(URI, Map) create} an input stream.
   * @param uri the URI to consider.
   * @param options options to influence how the existence determined, or <code>null</code> if there are no options.
   * @return whether the given URI has contents.
   * @since 2.4
   */
    boolean exists(URI uri, Map<?, ?> options);

    /**
   * The time stamp {@link #getAttributes(URI, Map) attribute} representing the last time the contents of a URI were modified.
   * The value is represented as Long that encodes the number of milliseconds 
   * since the epoch 00:00:00 GMT, January 1, 1970.
   * @since 2.4
   */
    String ATTRIBUTE_TIME_STAMP = "timeStamp";

    /**
   * A {@link #ATTRIBUTE_TIME_STAMP} value that indicates no time stamp is available.
   * @since 2.4
   */
    long NULL_TIME_STAMP = -1;

    /**
   * The length {@link #getAttributes(URI, Map) attribute} representing the number of bytes in the contents of a URI.
   * It is represented as a Long value.
   * @since 2.4
   */
    String ATTRIBUTE_LENGTH = "length";

    /**
   * The read only {@link #getAttributes(URI, Map) attribute} representing whether the contents of a URI can be modified.
   * It is represented as a Boolean value.
   * If the URI's contents {@link #exists(URI, Map) exist} and it is read only, 
   * it will not be possible to {@link #createOutputStream(URI, Map) create} an output stream.
   * @since 2.4
   */
    String ATTRIBUTE_READ_ONLY = "readOnly";

    /**
   * The execute {@link #getAttributes(URI, Map) attribute} representing whether the contents of a URI can be executed.
   * It is represented as a Boolean value.
   * @since 2.4
   */
    String ATTRIBUTE_EXECUTABLE = "executable";

    /**
   * The archive {@link #getAttributes(URI, Map) attribute} representing whether the contents of a URI are archived.
   * It is represented as a Boolean value.
   * @since 2.4
   */
    String ATTRIBUTE_ARCHIVE = "archive";

    /**
   * The hidden {@link #getAttributes(URI, Map) attribute} representing whether the URI is visible.
   * It is represented as a Boolean value.
   * @since 2.4
   */
    String ATTRIBUTE_HIDDEN = "hidden";

    /**
   * The directory {@link #getAttributes(URI, Map) attribute} representing whether the URI represents a directory rather than a file.
   * It is represented as a Boolean value.
   * @since 2.4
   */
    String ATTRIBUTE_DIRECTORY = "directory";

    /**
   * An option passed to a {@link Set Set<String>} to {@link #getAttributes(URI, Map)} to indicate the specific attributes to be fetched.
   */
    String OPTION_REQUESTED_ATTRIBUTES = "requestedAttributes";

    /**
   * Returns a map from String attributes to their corresponding values representing information about various aspects of the URI's state.
   * The {@link #OPTION_REQUESTED_ATTRIBUTES requested attributes option} can be used to specify which properties to fetch;
   * without that option, all supported attributes will be fetched.
   * If the URI doesn't not support any particular attribute, an entry for that attribute will not be appear in the result.
   * @param uri the URI to consider.
   * @param options options to influence how the attributes are determined, or <code>null</code> if there are no options.
   * @return a map from String attributes to their corresponding values representing information about various aspects of the URI's state.
   */
    Map<String, ?> getAttributes(URI uri, Map<?, ?> options);

    /**
   * Updates the map from String attributes to their corresponding values representing information about various aspects of the URI's state.
   * Unsupported or unchangeable attributes are ignored.
   * @param uri the URI to consider.
   * @param attributes the new values for the attributes.
   * @param options options to influence how the attributes are updated, or <code>null</code> if there are no options.
   * @throws IOException if there is a problem updating the attributes.
   */
    void setAttributes(URI uri, Map<String, ?> attributes, Map<?, ?> options) throws IOException;

    /**
   * The global static URI converter instance.
   * It's generally not a good idea to modify any aspect of this instance.
   * Instead, use a resource set's {@link ResourceSet#getURIConverter() local} instance.
   * @since 2.4
   */
    URIConverter INSTANCE = new ExtensibleURIConverterImpl();
}
