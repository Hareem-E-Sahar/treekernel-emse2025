package org.apache.velocity.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * This class provides  services to the application
 * developer, such as :
 * <ul>
 * <li> Simple Velocity Runtime engine initialization methods.
 * <li> Functions to apply the template engine to streams and strings
 *      to allow embedding and dynamic template generation.
 * <li> Methods to access Velocimacros directly.
 * </ul>
 *
 * <br><br>
 * While the most common way to use Velocity is via templates, as
 * Velocity is a general-purpose template engine, there are other
 * uses that Velocity is well suited for, such as processing dynamically
 * created templates, or processing content streams.
 *
 * <br><br>
 * The methods herein were developed to allow easy access to the Velocity
 * facilities without direct spelunking of the internals.  If there is
 * something you feel is necessary to add here, please, send a patch.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: Velocity.java 471381 2006-11-05 08:56:58Z wglass $
 */
public class Velocity implements RuntimeConstants {

    /**
     *  initialize the Velocity runtime engine, using the default
     *  properties of the Velocity distribution
     *
     * @throws Exception When an error during initialization occurs.
     */
    public static void init() throws Exception {
        RuntimeSingleton.init();
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the properties file passed in as the arg
     *
     *  @param propsFilename file containing properties to use to initialize
     *         the Velocity runtime
     * @throws Exception When an error during initialization occurs.
     */
    public static void init(String propsFilename) throws Exception {
        RuntimeSingleton.init(propsFilename);
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the passed in java.util.Properties object
     *
     *  @param p  Properties object containing initialization properties
     * @throws Exception When an error during initialization occurs.
     *
     */
    public static void init(Properties p) throws Exception {
        RuntimeSingleton.init(p);
    }

    /**
     * Set a Velocity Runtime property.
     *
     * @param key The property key.
     * @param value The property value.
     */
    public static void setProperty(String key, Object value) {
        RuntimeSingleton.setProperty(key, value);
    }

    /**
     * Add a Velocity Runtime property.
     *
     * @param key The property key.
     * @param value The property value.
     */
    public static void addProperty(String key, Object value) {
        RuntimeSingleton.addProperty(key, value);
    }

    /**
     * Clear a Velocity Runtime property.
     *
     * @param key of property to clear
     */
    public static void clearProperty(String key) {
        RuntimeSingleton.clearProperty(key);
    }

    /**
     * Set an entire configuration at once. This is
     * useful in cases where the parent application uses
     * the ExtendedProperties class and the velocity configuration
     * is a subset of the parent application's configuration.
     *
     * @param configuration A configuration object.
     *
     */
    public static void setExtendedProperties(ExtendedProperties configuration) {
        RuntimeSingleton.setConfiguration(configuration);
    }

    /**
     *  Get a Velocity Runtime property.
     *
     *  @param key property to retrieve
     *  @return property value or null if the property
     *        not currently set
     */
    public static Object getProperty(String key) {
        return RuntimeSingleton.getProperty(key);
    }

    /**
     *  renders the input string using the context into the output writer.
     *  To be used when a template is dynamically constructed, or want to use
     *  Velocity as a token replacer.
     *
     *  @param context context to use in rendering input string
     *  @param out  Writer in which to render the output
     *  @param logTag  string to be used as the template name for log
     *                 messages in case of error
     *  @param instring input string containing the VTL to be rendered
     *
     *  @return true if successful, false otherwise.  If false, see
     *             Velocity runtime log
     * @throws ParseErrorException The template could not be parsed.
     * @throws MethodInvocationException A method on a context object could not be invoked.
     * @throws ResourceNotFoundException A referenced resource could not be loaded.
     * @throws IOException While loading a reference, an I/O problem occured.
     */
    public static boolean evaluate(Context context, Writer out, String logTag, String instring) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
        return evaluate(context, out, logTag, new BufferedReader(new StringReader(instring)));
    }

    /**
     *  Renders the input stream using the context into the output writer.
     *  To be used when a template is dynamically constructed, or want to
     *  use Velocity as a token replacer.
     *
     *  @param context context to use in rendering input string
     *  @param writer  Writer in which to render the output
     *  @param logTag  string to be used as the template name for log messages
     *                 in case of error
     *  @param instream input stream containing the VTL to be rendered
     *
     *  @return true if successful, false otherwise.  If false, see
     *               Velocity runtime log
     *  @deprecated Use
     *  {@link #evaluate( Context context, Writer writer,
     *      String logTag, Reader reader ) }
     * @throws ParseErrorException The template could not be parsed.
     * @throws MethodInvocationException A method on a context object could not be invoked.
     * @throws ResourceNotFoundException A referenced resource could not be loaded.
     * @throws IOException While loading a reference, an I/O problem occured.
     */
    public static boolean evaluate(Context context, Writer writer, String logTag, InputStream instream) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
        BufferedReader br = null;
        String encoding = null;
        try {
            encoding = RuntimeSingleton.getString(INPUT_ENCODING, ENCODING_DEFAULT);
            br = new BufferedReader(new InputStreamReader(instream, encoding));
        } catch (UnsupportedEncodingException uce) {
            String msg = "Unsupported input encoding : " + encoding + " for template " + logTag;
            throw new ParseErrorException(msg);
        }
        return evaluate(context, writer, logTag, br);
    }

    /**
     *  Renders the input reader using the context into the output writer.
     *  To be used when a template is dynamically constructed, or want to
     *  use Velocity as a token replacer.
     *
     *  @param context context to use in rendering input string
     *  @param writer  Writer in which to render the output
     *  @param logTag  string to be used as the template name for log messages
     *                 in case of error
     *  @param reader Reader containing the VTL to be rendered
     *
     *  @return true if successful, false otherwise.  If false, see
     *               Velocity runtime log
     * @throws ParseErrorException The template could not be parsed.
     * @throws MethodInvocationException A method on a context object could not be invoked.
     * @throws ResourceNotFoundException A referenced resource could not be loaded.
     * @throws IOException While loading a reference, an I/O problem occured.
     *
     *  @since Velocity v1.1
     */
    public static boolean evaluate(Context context, Writer writer, String logTag, Reader reader) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
        SimpleNode nodeTree = null;
        try {
            nodeTree = RuntimeSingleton.parse(reader, logTag);
        } catch (ParseException pex) {
            throw new ParseErrorException(pex);
        } catch (TemplateInitException pex) {
            throw new ParseErrorException(pex);
        }
        if (nodeTree != null) {
            InternalContextAdapterImpl ica = new InternalContextAdapterImpl(context);
            ica.pushCurrentTemplateName(logTag);
            try {
                try {
                    nodeTree.init(ica, RuntimeSingleton.getRuntimeServices());
                } catch (TemplateInitException pex) {
                    throw new ParseErrorException(pex);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    getLog().error("Velocity.evaluate() : init exception for tag = " + logTag, e);
                }
                nodeTree.render(ica, writer);
            } finally {
                ica.popCurrentTemplateName();
            }
            return true;
        }
        return false;
    }

    /**
     *  Invokes a currently registered Velocimacro with the parms provided
     *  and places the rendered stream into the writer.
     *
     *  Note : currently only accepts args to the VM if they are in the context.
     *
     *  @param vmName name of Velocimacro to call
     *  @param logTag string to be used for template name in case of error
     *  @param params args used to invoke Velocimacro. In context key format :
     *                  eg  "foo","bar" (rather than "$foo","$bar")
     *  @param context Context object containing data/objects used for rendering.
     *  @param writer  Writer for output stream
     *  @return true if Velocimacro exists and successfully invoked, false otherwise.
     */
    public static boolean invokeVelocimacro(String vmName, String logTag, String params[], Context context, Writer writer) {
        if (vmName == null || params == null || context == null || writer == null || logTag == null) {
            getLog().error("Velocity.invokeVelocimacro() : invalid parameter");
            return false;
        }
        if (!RuntimeSingleton.isVelocimacro(vmName, logTag)) {
            getLog().error("Velocity.invokeVelocimacro() : VM '" + vmName + "' not registered.");
            return false;
        }
        StringBuffer construct = new StringBuffer("#");
        construct.append(vmName);
        construct.append("(");
        for (int i = 0; i < params.length; i++) {
            construct.append(" $");
            construct.append(params[i]);
        }
        construct.append(" )");
        try {
            return evaluate(context, writer, logTag, construct.toString());
        } catch (ParseErrorException pee) {
            throw pee;
        } catch (MethodInvocationException mie) {
            throw mie;
        } catch (ResourceNotFoundException rnfe) {
            throw rnfe;
        } catch (IOException ioe) {
            getLog().error("Velocity.invokeVelocimacro() failed", ioe);
        } catch (RuntimeException re) {
            throw re;
        }
        return false;
    }

    /**
     *  Merges a template and puts the rendered stream into the writer.
     *  The default encoding that Velocity uses to read template files is defined in
     *  the property input.encoding and defaults to ISO-8859-1.
     *
     *  @param templateName name of template to be used in merge
     *  @param context  filled context to be used in merge
     *  @param  writer  writer to write template into
     *
     *  @return true if successful, false otherwise.  Errors
     *           logged to velocity log.
     *  @deprecated Use
     *  {@link #mergeTemplate( String templateName, String encoding,
     *                Context context, Writer writer )}
     * @throws ParseErrorException The template could not be parsed.
     * @throws MethodInvocationException A method on a context object could not be invoked.
     * @throws ResourceNotFoundException A referenced resource could not be loaded.
     * @throws Exception Any other exception.
     */
    public static boolean mergeTemplate(String templateName, Context context, Writer writer) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        return mergeTemplate(templateName, RuntimeSingleton.getString(INPUT_ENCODING, ENCODING_DEFAULT), context, writer);
    }

    /**
     *  merges a template and puts the rendered stream into the writer
     *
     *  @param templateName name of template to be used in merge
     *  @param encoding encoding used in template
     *  @param context  filled context to be used in merge
     *  @param  writer  writer to write template into
     *
     *  @return true if successful, false otherwise.  Errors
     *           logged to velocity log
     *
     * @throws ParseErrorException The template could not be parsed.
     * @throws MethodInvocationException A method on a context object could not be invoked.
     * @throws ResourceNotFoundException A referenced resource could not be loaded.
     * @throws Exception Any other exception.
     *
     * @since Velocity v1.1
     */
    public static boolean mergeTemplate(String templateName, String encoding, Context context, Writer writer) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        Template template = RuntimeSingleton.getTemplate(templateName, encoding);
        if (template == null) {
            getLog().error("Velocity.mergeTemplate() was unable to load template '" + templateName + "'");
            return false;
        } else {
            template.merge(context, writer);
            return true;
        }
    }

    /**
     *  Returns a <code>Template</code> from the Velocity
     *  resource management system.
     *
     * @param name The file name of the desired template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public static Template getTemplate(String name) throws ResourceNotFoundException, ParseErrorException, Exception {
        return RuntimeSingleton.getTemplate(name);
    }

    /**
     *  Returns a <code>Template</code> from the Velocity
     *  resource management system.
     *
     * @param name The file name of the desired template.
     * @param encoding The character encoding to use for the template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     *
     *  @since Velocity v1.1
     */
    public static Template getTemplate(String name, String encoding) throws ResourceNotFoundException, ParseErrorException, Exception {
        return RuntimeSingleton.getTemplate(name, encoding);
    }

    /**
     * <p>Determines whether a resource is accessable via the
     * currently configured resource loaders.  {@link
     * org.apache.velocity.runtime.resource.Resource} is the generic
     * description of templates, static content, etc.</p>
     *
     * <p>Note that the current implementation will <b>not</b> change
     * the state of the system in any real way - so this cannot be
     * used to pre-load the resource cache, as the previous
     * implementation did as a side-effect.</p>
     *
     * @param resourceName The name of the resource to search for.
     * @return Whether the resource was located.
     */
    public static boolean resourceExists(String resourceName) {
        return (RuntimeSingleton.getLoaderNameForResource(resourceName) != null);
    }

    /**
     * Returns a convenient Log instance that wraps the current LogChute.
     * Use this to log error messages. It has the usual methods.
     *
     * @return A convenience Log instance that wraps the current LogChute.
     */
    public static Log getLog() {
        return RuntimeSingleton.getLog();
    }

    /**
     * @deprecated Use getLog() and call warn() on it.
     * @see Log#warn(Object)
     * @param message The message to log.
     */
    public static void warn(Object message) {
        getLog().warn(message);
    }

    /**
     * @deprecated Use getLog() and call info() on it.
     * @see Log#info(Object)
     * @param message The message to log.
     */
    public static void info(Object message) {
        getLog().info(message);
    }

    /**
     * @deprecated Use getLog() and call error() on it.
     * @see Log#error(Object)
     * @param message The message to log.
     */
    public static void error(Object message) {
        getLog().error(message);
    }

    /**
     * @deprecated Use getLog() and call debug() on it.
     * @see Log#debug(Object)
     * @param message The message to log.
     */
    public static void debug(Object message) {
        getLog().debug(message);
    }

    /**
     *  <p>
     *  Set the an ApplicationAttribue, which is an Object
     *  set by the application which is accessable from
     *  any component of the system that gets a RuntimeServices.
     *  This allows communication between the application
     *  environment and custom pluggable components of the
     *  Velocity engine, such as loaders and loggers.
     *  </p>
     *
     *  <p>
     *  Note that there is no enfocement or rules for the key
     *  used - it is up to the application developer.  However, to
     *  help make the intermixing of components possible, using
     *  the target Class name (e.g.  com.foo.bar ) as the key
     *   might help avoid collision.
     *  </p>
     *
     *  @param key object 'name' under which the object is stored
     *  @param value object to store under this key
     */
    public static void setApplicationAttribute(Object key, Object value) {
        RuntimeSingleton.getRuntimeInstance().setApplicationAttribute(key, value);
    }

    /**
     * @param resourceName Name of the Template to check.
     * @return True if the template exists.
     * @see #resourceExists(String)
     * @deprecated Use resourceExists(String) instead.
     */
    public static boolean templateExists(String resourceName) {
        return resourceExists(resourceName);
    }
}
