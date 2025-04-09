package grace.log;

import java.io.FileInputStream;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import gnu.regexp.RE;
import gnu.regexp.REException;
import grace.util.Properties;

/**
 * This implements a logging facility to allow the user to log
 * programmatic context and data to various log devices such as
 * System.out, a file, a GUI, etc.  Each entry in the log is called an
 * event.  An event captures various data items such as the time, an
 * event type, the program's position, a message, an object, or a Java
 * Exception.  The different event types handled by this logging
 * facility are:
 *
 * <ul>
 * <li>Errors</li>
 * <li>Warnings</li>
 * <li>Notices</li>
 * <li>Programmatic Traces</li>
 * <li>User defined</li>
 * </ul>
 *
 * <P><strong>Errors</strong> are problems that have been detected in
 * the code that are not accompanied by an exception, or exceptions
 * that have occurred and the program has interpreted the exception
 * and has translated it to a higher level error message, or
 * exceptions that have occurred that the programmer does not know how
 * to handle.  Errors should be situations where the program is
 * confident that if the problem is not solved, the program may not
 * continue to run.  The programmer may indicate an error message as
 * follows:
 *
 * <pre>
 *	if (dog == null) Log.error("Unexpected null dog");
 * </pre>
 * or,
 * <pre>
 *	try { dog.bark(); }
 *	catch (SickDogException e) { Log.error(e); }
 * </pre>
 *
 * <P>The programmer may also indicate an object as part of the
 * notification, as follows:
 *
 * <pre>
 *	dog.bark();
 *	if (!dog.isBarking()) Log.error("dog won't bark", dog);
 * </pre>
 *
 * <P><strong>Warnings</strong> are similar to errors but do not have
 * the same level of severity.  They indicate a problem in the system
 * but one that will not affect the immediate health of the program.
 *
 * <pre>
 * 	if (!dog.isBarking()) {
 *	    dog.bark();
 *	    Log.warning("dog wasn't barking but now he is");
 * </pre>
 *
 * <P><strong>Notices</strong> are events that the programmer would
 * like to be logged but that are not warnings or errors and are
 * merely for informational purposes only.
 *
 * <pre>
 * 	dog = new Dog();
 *	Log.notice("created new dog");
 * </pre>
 *
 * <P><strong>Traces</strong> are events used for debugging.  They can
 * be eliminated at run time by filtering.  Like all other events
 * types, the trace facility fills in the current function and line
 * number in the code so the caller should not provide this in his
 * message.
 *
 * <pre>
 *	Log.trace("current dog", dog);
 * </pre>
 *
 * <P><strong>User Defined</strong> are events in which the programmer
 * can specify his own event type.  There is no difference between the
 * programmer defined types and the defined ones (errors, warnings,
 * notices, traces).
 *
 * <pre>
 *	Log.log("statistics", "number-of-dogs=" + numDogs);
 * </pre>
 *
 * <h2>Handlers</h2>
 *
 * The Log class merely acts as a distributer of events to <a
 * href=Handler.html>Handlers</a>.  Handlers are objects that handle
 * and, presumably, output in some way one or more event types.  There
 * can be many Handlers installed in a running program.  For example,
 * one Handler can be saving every logged event to a file while
 * another Handler can be displaying a popup dialog only when an error
 * occurs.
 *
 * <P>Currently, there are only four handlers:
 *
 * 	<ul>
 *	<li><a href=StandardOutHandler.html>StandardOutHandler</a>
 *	<li><a href=FileHandler.html>FileHandler</a>
 *	<li><a href=JDBCHandler.html>JDBCHandler</a>
 *	<li><a href=ProxyHandler.html>ProxyHandler</a>
 *	</ul>
 * 
 * <P>To install a custom handler, one must derive from
 * grace.log.Handler and install an instance of the Handler using
 * the <code>addHandler(...)</code> function.
 *
 * <h3>User Object Data Handling</h3>
 *
 * The functions that accept an object as well as a message, will log
 * a message that is appropriate for that object.
 *
 * <h2>Control</h2>
 *
 * <P>Various system properties (settable on the command line using
 * the -D option) control the behavior of the logging functions:
 *
 * <pre>
 *	log = true|false
 *	log.format = <a href="../../grace/log/EventFormat.html" target="classFrame">event-format</a>
 * 	log.rc = filename to load instead of local .logrc file
 *	log.errors = true|false
 *	log.warnings = true|false
 *	log.notices = true|false
 *	log.traces = true|false
 *	log.time.format = short | medium | long | full | 24 | SimpleDateFormat
 *	log.time.relative = "clock | days | hours | minutes | seconds"
 *	log.time.zone = GMT etc.
 *	log.thread.format = thread-format
 *	log.message.format = message-format
 *	log.exception.format = message-format
 *	log.object.format = object-format
 *	log.handler.<i>name</i>.url =
 *	    file://abs | file:rel | rmi:///name | jdbc:protocol:name
 *	log.handler.<i>name</i>.class = FileHandler|StandardOutHandler|...
 *	log.handler.<i>name</i>.events = error|warnings|...
 *	log.handler.<i>name</i>.file = filename  (for FileHandler only)
 *	log.handler.<i>name</i>.maxsize = 12M | 2K | ... (for FileHandler only)
 *      log.function.exclude = "grace.log..* grace.util..*"
 *      log.function.include = "grace.log..* grace.util..*"
 *      log.event.include = "error warn.*"
 *      log.event.exclude = "trace.* notice.*"
 * </pre>
 *
 * <H2>Notes</H2>
 *
 * <ul>
 *
 * <li>The exclusion filters like <code>log.functions.exclude</code>
 * are applied against the results of the inclusion filters
 * like<code>log.functions.include</code>.
 *
 * </ul>
 * 
 * <h2>Wish List:</h2>
 * <ul>
 *
 * <li>consider documenting properties in XML.
 * <li>figure out how to display events as XML.
 *
 * <li>property of FileHandler to write zip files.
 *
 * <li>be able to reference the program position some number of levels
 * up.
 *
 * <li>separate verbose properties in addition to format strings.
 *
 * <li>functional interface to all properties.
 *
 * <li>fix bug where line number is 0 in jit compiled code.
 *
 * <li>write a NotImplementedException that puts fills in the class
 * name and function into the message.
 *
 * <li>ability to customize object format and recursion by class type.
 *
 * <li>JMSHandler.  publish logs to JMS topic.
 *
 * </ul>
 **/
public class Log implements Distributer {

    public static final String rcsid = "$Id: Log.java,v 1.1 2005/12/15 01:45:08 fbergmann Exp $";

    /**
     * Can be passed to log(type, ...) or to enableEvent(type)
     * functions.  Passing this to log(type, ...) is the same as
     * calling error(...);
     **/
    public static final String ERROR = "error";

    /**
     * Can be passed to log(type, ...) or to enableEvent(type)
     * functions.  Passing this to log(type, ...) is the same as
     * calling warning(...);
     **/
    public static final String WARNING = "warning";

    /**
     * Can be passed to log(type, ...) or to enableEvent(type)
     * functions.  Passing this to log(type, ...) is the same as
     * calling notice(...);
     **/
    public static final String NOTICE = "notice";

    /**
     * Can be passed to log(type, ...) or to enableEvent(type)
     * functions.  Passing this to log(type, ...) is the same as
     * calling trace(...);
     **/
    public static final String TRACE = "trace";

    private static final String handlerPropertyPrefix = "log.handler.";

    private static final String defaultHandlerPackage = "grace.log";

    private static final String defaultHandlerClassname = "grace.log.StandardOutputHandler";

    private static final String standardOutName = "out";

    private static final SimpleDateFormat internalDateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss");

    private static final String rawVirtualMachineId = new java.rmi.dgc.VMID().toString();

    private static Properties properties = new Properties();

    private static String hostname = "localhost";

    private static int numInstantiatedDistributers = 0;

    private String name = properties.get("log.name", (++numInstantiatedDistributers == 1) ? virtualMachineId() : virtualMachineId() + numInstantiatedDistributers);

    private boolean enablesInitialized = false;

    private boolean errorsEnabled = true;

    private boolean warningsEnabled = true;

    private boolean noticesEnabled = true;

    private boolean tracesEnabled = true;

    private static boolean internalLogEnabled = false;

    private Hashtable allHandlers = new Hashtable();

    private Hashtable eventHandlers = new Hashtable();

    private Vector functionNamesToInclude = new Vector();

    private Vector functionNamesToExclude = new Vector();

    private Vector eventTypesToInclude = new Vector();

    private Vector eventTypesToExclude = new Vector();

    private EventFormat defaultObjectFormat = new EventFormat("%(%j)o");

    private boolean initialized = false;

    static {
        try {
            properties.loadSystem();
            String localFile = properties.get("log.rc", ".logrc");
            properties.loadHomeFile(".logrc");
            properties.loadFile(localFile);
            properties.loadSystem();
            properties.integrateWithSystemProperties();
        } catch (Exception e) {
            Log.internal("warning: can't load properties");
        }
        internalLogEnabled = properties.get("log.internal", "false").equals("true");
        try {
            hostname = java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostname = "localhost";
        }
        internal("initialized statics");
    }

    /**
     * This singleton allows the user to access the log through the
     * static methods.  
     **/
    private static Log instance = new Log();

    /**
     * Used to staticly access the singleton log instance.  Usually,
     * the static log functions are sufficient for most users but if
     * the user really wants access to an instance, this is it.
     **/
    public static Log getInstance() {
        return instance;
    }

    /**
     * Logs the given message marked with the given event type.  event
     * type is assumed to be some user defined string that indicates
     * the nature of the given message.  For example, this could be
     * "error", "warnings", "stats", etc.
     *
     * @param eventType user defined classification of log message
     * @param message free form user message to log
     **/
    public static void log(String eventType, String message) {
        if (instance.eventTypeEnabled(eventType)) {
            Event event = new Event(eventType);
            event.setMessage(message);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * Logs the given object marked with the given eventType.  EventType
     * is assumed to be some user defined string that indicates the
     * nature of the given message.  For example, this could be
     * "error", "warnings", "stats", etc.
     *
     * @param eventType user defined classification of log message
     * @param object object to log
     **/
    public static void log(String eventType, Object object) {
        if (instance.eventTypeEnabled(eventType)) {
            Event event = new Event(eventType);
            event.setObject(object);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * Logs the given message and object marked with the given
     * eventType.  EventType is assumed to be some user defined string
     * that indicates the nature of the given message.  For example,
     * this could be "error", "warnings", "stats", etc.
     *
     * @param eventType user defined classification of log message
     * @param message free form user message to log
     * @param object object to log
     **/
    public static void log(String eventType, String message, Object object) {
        if (instance.eventTypeEnabled(eventType)) {
            Event event = new Event(eventType);
            event.setMessage(message);
            event.setObject(object);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This logs the indicated error message text to the configured
     * log device.  This is equivalent to calling the log function
     * with "error" as the event type.
     *
     * <p>Error messages should indicate a grave condition in which the
     * programmer can not anticipate the integrity of the running
     * system.
     *
     * @param message to be logged to the log device
     **/
    public static void error(String message) {
        if (instance.eventTypeEnabled(ERROR)) {
            Event event = new Event(ERROR);
            event.setMessage(message);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This logs the error message and then logs the given object.
     * The format and extent that this object is logged is dependent
     * on the particular implementation of the log.  This is
     * equivalent to calling the log function with "error" as the
     * event type.
     *
     * <p>Error messages should indicate a grave condition in which the
     * programmer can not anticipate the integrity of the running
     * system.
     *
     * @param message to be logged to the log device
     *
     * @param object to be logged to log device in implementation
     * specific manner.
     **/
    public static void error(String message, Object object) {
        if (instance.eventTypeEnabled(ERROR)) {
            Event event = new Event(ERROR);
            event.setMessage(message);
            event.setObject(object);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This function simply logs the given exception to the log
     * device.  The format and that extent the exception is logged is
     * dependent on the particular implementation of the log.  This is
     * equivalent to calling the log function with "error" as the
     * event type.
     *
     * <p>Error messages should indicate a grave condition in which the
     * programmer can not anticipate the integrity of the running
     * system.
     *
     * @param object to be logged to log device in implementation
     * specific manner.
     **/
    public static void error(Exception exception) {
        if (instance.eventTypeEnabled(ERROR)) {
            Event event = new Event(ERROR);
            event.setObject(exception);
            event.setPosition(new StackTrace(exception));
            instance.distribute(event);
        }
    }

    /**
     * This logs the given message along with the given exception.
     * The format and that extent the exception is logged is dependent
     * on the particular implementation of the log.  This is
     * equivalent to calling the log function with "error" as the
     * event type.
     *
     * <p>Error messages should indicate a grave condition in which the
     * programmer can not anticipate the integrity of the running
     * system.
     **/
    public static void error(String message, Exception exception) {
        if (instance.eventTypeEnabled(ERROR)) {
            Event event = new Event(ERROR);
            event.setMessage(message);
            event.setObject(exception);
            event.setPosition(new StackTrace(exception));
            instance.distribute(event);
        }
    }

    /**
     * This logs the indicated warning message text to the configured
     * log device.  This is equivalent to calling the log function
     * with "warning" as the event type.
     *
     * <p>Warning messages should indicate a problem in the system but
     * one which the programmer expects will <strong>not</strong>
     * affect the immediate integrity of the running system.
     *
     * @param message to log
     **/
    public static void warning(String message) {
        if (instance.eventTypeEnabled(WARNING)) {
            Event event = new Event(WARNING);
            event.setMessage(message);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This logs the indicated warning message text and object to the
     * configured log device.  The format of the object in the log is
     * specific to the particular log implmentation.  This is
     * equivalent to calling the log function with "warning" as the
     * event type.
     *
     * <p>Warning messages should indicate a problem in the system but
     * one which the programmer expects will <strong>not</strong>
     * affect the immediate integrity of the running system.
     *
     * @param message to log
     **/
    public static void warning(String message, Object object) {
        if (instance.eventTypeEnabled(WARNING)) {
            Event event = new Event(WARNING);
            event.setMessage(message);
            event.setObject(object);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This logs the indicated warning message text and give exception
     * to the configured log device.  The format of the object in the
     * log is specific to the particular log implmentation.  This is
     * equivalent to calling the log function with "warning" as the
     * event type.
     *
     * <p>Warning messages should indicate a problem in the system but
     * one which the programmer expects will <strong>not</strong>
     * affect the immediate integrity of the running system.
     *
     * @param message to be logged
     * @param exception to be logged
     **/
    public static void warning(String message, Exception exception) {
        if (instance.eventTypeEnabled(WARNING)) {
            Event event = new Event(WARNING);
            event.setMessage(message);
            event.setObject(exception);
            event.setPosition(new StackTrace(exception));
            instance.distribute(event);
        }
    }

    /**
     * This logs the indicated notice message text to the configured
     * log device.  This is equivalent to calling the log function
     * with "warning" as the event type.
     *
     * <p>Notices messages should be used to communicate situations
     * that have developed that are part of the normal operation of
     * the system.  This is equivalent to calling the log function
     * with "notice" as the event type.
     *
     * @param message to log
     **/
    public static void notice(String message) {
        if (instance.eventTypeEnabled(NOTICE)) {
            Event event = new Event(NOTICE);
            event.setMessage(message);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This logs the indicated notice message text and the given
     * object to the configured log device.  The object is written in
     * an implmentation specific to the particular log device.  This
     * is equivalent to calling the log function with "notice" as the
     * event type.
     *
     * <p>Notices messages should be used to communicate situations
     * that have developed that are part of the normal operation of
     * the system.
     *
     * @param message to log
     * @param object to log in implementation specific format
     **/
    public static void notice(String message, Object object) {
        if (instance.eventTypeEnabled(NOTICE)) {
            Event event = new Event(NOTICE);
            event.setMessage(message);
            event.setObject(object);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This function makes an entry in the log device that indicates
     * the programtic position of the caller.  Typically this will
     * include the filename and line number and could also include the
     * class name and function name.  This is equivalent to calling
     * the log function with "trace" as the event type.
     **/
    public static void trace() {
        if (instance.eventTypeEnabled(TRACE)) {
            Event event = new Event(TRACE);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This function writes the given message in the log device and
     * makes an entry in the log device that indicates the programtic
     * position of the caller.  Typically this will include the
     * filename and line number and could also include the class name
     * and function name.  This is equivalent to calling the log
     * function with "trace" as the event type.
     *
     * @param message to log 
     **/
    public static void trace(String message) {
        if (instance.eventTypeEnabled(TRACE)) {
            Event event = new Event(TRACE);
            event.setMessage(message);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This function writes the given object in the log device and
     * makes an entry in the log device that indicates the programtic
     * position of the caller.  Typically this will include the
     * filename and line number and could also include the class name
     * and function name.  Both the object and the trace message are
     * specific to the log implementation.  This is equivalent to
     * calling the log function with "trace" as the event type.
     *
     * @param object to log in implementation specific manner
     **/
    public static void trace(Object object) {
        if (instance.eventTypeEnabled(TRACE)) {
            Event event = new Event(TRACE);
            event.setObject(object);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This function writes the given message and object in the log
     * device and makes an entry in the log device that indicates the
     * programtic position of the caller.  Typically this will include
     * the filename and line number and could also include the class
     * name and function name.  Both the object and the trace message
     * are specific to the log implementation.  This is equivalent to
     * calling the log function with "trace" as the event type.
     *
     * @param message to log
     * @param object to log in implementation specific manner
     **/
    public static void trace(String message, Object object) {
        if (instance.eventTypeEnabled(TRACE)) {
            Event event = new Event(TRACE);
            event.setMessage(message);
            event.setObject(object);
            event.setPosition(new StackTrace(1));
            instance.distribute(event);
        }
    }

    /**
     * This function is a very simple way to output a log message
     * without using any of the log dispatching or handling.  This is
     * mainly used by the internal log system to avoid any kind of
     * recursion problems when the internal log system needs to log
     * its own messages.  All exceptions are printed to the standard
     **/
    public static synchronized void internal(String message) {
        if (internalLogEnabled) {
            System.out.print(internalDateFormat.format(new java.util.Date()));
            StackTrace position = new StackTrace(1);
            System.out.print(": ");
            System.out.print(position.getShortClassname());
            System.out.print('.');
            System.out.print(position.getFunction());
            System.out.print(':');
            System.out.print(position.getLineNumber());
            System.out.print(": internal: ");
            System.out.println(message);
        }
    }

    /**
     * This function is a very simple way to output a log exceptions
     * without using any of the log dispatching or handling.  This is
     * mainly used by the internal log system to avoid any kind of
     * recursion problems when the internal log system needs to log
     * its own exceptions.  All exceptions are printed to the standard
     * out.
     **/
    public static synchronized void internal(Exception e) {
        System.out.print(internalDateFormat.format(new java.util.Date()));
        StackTrace position = new StackTrace(1);
        System.out.print(": ");
        System.out.print(position.getShortClassname());
        System.out.print('.');
        System.out.print(position.getFunction());
        System.out.print(':');
        System.out.print(position.getLineNumber());
        System.out.print(": internal: ");
        e.printStackTrace();
        System.out.println();
    }

    /**
     * Logs are constructed internally and usually should not be
     * constructed by the normal user.  The best way to use the log
     * system is to call one of the static functions.
     **/
    public Log() {
        try {
            initialize();
        } catch (Exception e) {
            Log.internal(e);
        }
    }

    /**
     * This returns the name of this log/distributer.  This name
     * defaults to the virtual machine id as determined by
     * java.rmi.dgc.VMID, with all of the colons removed and a
     * sequence number added to the end.  This can be used to uniquely
     * identify any log/distributer on the net.
     *
     * <p>The property log.name can be used to override the use of the
     * VMID and use the user supplied name as a prefix with the
     * sequence number.
     **/
    public String getName() {
        return name;
    }

    /**
     * This overrides the default use of the VMID and sets the name of
     * this log.
     **/
    public void setName(String name) {
        this.name = replaceBadPunctuation(name);
    }

    /**
     * Returns the virtual machine id as determined by
     * java.rmi.dgc.VMID but with all of the colons replaced by dots.
     **/
    protected String virtualMachineId() {
        return replaceBadPunctuation(rawVirtualMachineId);
    }

    /**
     * This returns a copy of the given string with all puntuation
     * that is offensive to rmi URLs replaced by dots.
     **/
    protected static String replaceBadPunctuation(String source) {
        return source.replace(':', '.');
    }

    /**
     * This function calls the handler.handle(Event) function.  This
     * catches the case when remote handlers are called and the
     * event.object is not serializable.  In this case, the object is
     * formatted in a default style (Java) and sent off as a String.
     *
     * @param handler to call the handle function for the event
     * @param event to pass to the handle function
     **/
    protected void dispatch(Handler handler, Event event) {
        try {
            handler.handle(event);
        } catch (java.rmi.MarshalException e) {
            if (e.detail instanceof java.io.NotSerializableException && event.object != null) {
                try {
                    event.object = defaultObjectFormat.format(event);
                    handler.handle(event);
                } catch (Exception ee) {
                    Log.internal("bad connection to handler; removing: " + ee.getMessage());
                    removeHandler(handler);
                }
            }
        } catch (Exception e) {
            Log.internal(e);
            Log.internal("bad connection to handler; removing...");
            removeHandler(handler);
        }
    }

    /**
     * This removes the given handler from all of the handlers -
     * allHandlers and eventHandlers.
     **/
    protected void removeHandler(Handler toRemove) {
        Log.internal("removing handler " + toRemove);
        removeObjectFrom(allHandlers, toRemove);
        removeObjectFrom(eventHandlers, toRemove);
    }

    /**
     * This removes an object from a hashtable. The bloody hastable
     * can not remove an object from itself - only a key.
     **/
    protected void removeObjectFrom(Hashtable table, Object toRemove) {
        Enumeration keys = table.keys();
        Enumeration elements = table.elements();
        while (keys.hasMoreElements() && elements.hasMoreElements()) {
            Object key = keys.nextElement();
            Object object = elements.nextElement();
            if (object.equals(toRemove)) {
                table.remove(key);
            }
        }
    }

    /**
     * This is the heart of the logging.  It takes an Event and calls
     * each of the appropriate filtered Handlers and then calls each
     * one of the Handlers that handles all events.
     *
     * @param event to log
     **/
    public void distribute(Event event) {
        if (eventIsInteresting(event)) {
            event.setHostname(hostname);
            event.setVirtualMachineName(name);
            Hashtable handlers = (Hashtable) eventHandlers.get(event.type);
            if (handlers != null) {
                Enumeration iter = handlers.elements();
                while (iter.hasMoreElements()) {
                    dispatch((Handler) iter.nextElement(), event);
                }
            }
            Enumeration iter = allHandlers.elements();
            while (iter.hasMoreElements()) {
                dispatch((Handler) iter.nextElement(), event);
            }
        }
    }

    /**
     * Indicates that the given event should be included in the log
     * based on whether the function from which the event was
     * generated matches the filters.  This uses the
     * functionNamesToInclude/Exclude filters setup in the init
     * function.
     **/
    protected boolean eventIsInteresting(Event event) {
        String functionText = event.position.getClassname() + '.' + event.position.getFunction();
        return eventFieldIsInteresting(functionText, functionNamesToInclude, functionNamesToExclude);
    }

    /**
     * This applies the filters against the given text.  First the
     * inclusion filters are applied and then the exclusion filters
     * applied against the results of the inclusion filter.  Note, an
     * empty includeFilter means include everything.  An empty
     * excludeFilter means exclude nothing.
     *
     * @param text to include/exclude using filters
     * @param includeFilter Vector<RE> matches against which will return true
     * @param excludeFilter Vector<RE> matches against which will return false
     *
     * @return whether includeFilter matched and excludeFilter didn't match
     **/
    protected boolean eventFieldIsInteresting(String text, Vector includeFilter, Vector excludeFilter) {
        boolean interesting = false;
        for (int i = 0; i < includeFilter.size(); ++i) {
            RE expression = (RE) includeFilter.elementAt(i);
            if (expression.isMatch(text)) {
                interesting = true;
                break;
            }
        }
        if (interesting) {
            for (int i = 0; i < excludeFilter.size(); ++i) {
                RE expression = (RE) excludeFilter.elementAt(i);
                if (expression.isMatch(text)) {
                    interesting = false;
                    break;
                }
            }
        }
        return interesting;
    }

    /**
     * Enables or disables the logging of errors.  Can be
     * called at any point during the execution of the program.
     **/
    public static void enableErrors(boolean enabled) {
        instance.enableEventType(ERROR, enabled);
    }

    public static boolean errorsEnabled() {
        return instance.errorsEnabled;
    }

    /**
     * Enables or disables the logging of warnings.  Can be
     * called at any point during the execution of the program.
     **/
    public static void enableWarnings(boolean enabled) {
        instance.enableEventType(WARNING, enabled);
    }

    public static boolean warningsEnabled() {
        return instance.warningsEnabled;
    }

    /**
     * Enables or disables the logging of notices.  Can be
     * called at any point during the execution of the program.
     **/
    public static void enableNotices(boolean enabled) {
        instance.enableEventType(NOTICE, enabled);
    }

    public static boolean noticesEnabled() {
        return instance.noticesEnabled;
    }

    /**
     * Enables or disables the logging of traces.  Can be
     * called at any point during the execution of the program.
     **/
    public static void enableTraces(boolean enabled) {
        instance.enableEventType(TRACE, enabled);
    }

    public static boolean tracesEnabled() {
        return instance.tracesEnabled;
    }

    public void enableEventType(String eventType, boolean enabled) {
        try {
            RE expression = new RE(eventType);
            if (eventTypesToInclude.contains(expression)) eventTypesToInclude.removeElement(expression);
            if (eventTypesToExclude.contains(expression)) eventTypesToExclude.removeElement(expression);
            if (enabled) eventTypesToInclude.addElement(expression); else eventTypesToExclude.addElement(expression);
        } catch (REException e) {
            e.printStackTrace();
        }
    }

    /**
     * Indicates that the given event type should be included in the
     * log.  This uses the eventTypesToInclude/Exclude filters setup
     * in the init function.
     **/
    public boolean eventTypeEnabled(String type) {
        return eventFieldIsInteresting(type, eventTypesToInclude, eventTypesToExclude);
    }

    /**
     * Add the given handler into this log such that all subsequent
     * messages will be dispatched to this and all previously
     * installed Handlers.
     **/
    public void addHandler(Handler handler) {
        Log.internal("");
        allHandlers.put(handler, handler);
    }

    /**
     * Add the given handler into this log such that all subsequent
     * messages of the given event will be dispatched to this and all
     * previously installed Handlers of the given event.
     **/
    public void addHandler(Handler handler, String event) {
        Log.internal("");
        Hashtable handlersForEvent = (Hashtable) eventHandlers.get(event);
        if (handlersForEvent == null) {
            handlersForEvent = new Hashtable();
            eventHandlers.put(event, handlersForEvent);
        }
        handlersForEvent.put(handler, handler);
    }

    /**
     * This adds a handler for each event given in the space separated
     * list of events.
     **/
    protected void addToEventHandlers(Handler handler, String handlerName, String events) {
        if (events.equals("")) {
            allHandlers.put(handlerName, handler);
        } else {
            StringTokenizer tokenizer = new StringTokenizer(events);
            while (tokenizer.hasMoreTokens()) {
                String event = tokenizer.nextToken();
                Hashtable handlersForEvent = (Hashtable) eventHandlers.get(event);
                if (handlersForEvent == null) {
                    handlersForEvent = new Hashtable();
                    eventHandlers.put(event, handlersForEvent);
                }
                handlersForEvent.put(handlerName, handler);
            }
        }
    }

    /**
     * Given the name of a handler, this looks up the properties to
     * instantiate and install the correct handler.
     **/
    protected void addHandler(String name) {
        Log.internal("name=" + name);
        if (!allHandlers.contains(name)) {
            String prefix = handlerPropertyPrefix + name;
            Class defaultHandlerClass = FileHandler.class;
            String url = properties.get(prefix + ".url");
            if (url != null) {
                if (url.startsWith("jdbc:")) {
                    addLocalHandler(name, JDBCHandler.class);
                } else if (url.startsWith("file:")) {
                    if (url.endsWith("-")) {
                        addLocalHandler(name, StandardOutHandler.class);
                    } else addLocalHandler(name, FileHandler.class);
                } else if (url.startsWith("rmi:")) {
                    addRemoteObject(name, url);
                }
            } else {
                addLocalHandler(name, FileHandler.class);
            }
        }
    }

    protected void addLocalHandler(String name, Class defaultHandlerClass) {
        String handlerClassName = properties.get(handlerPropertyPrefix + name + ".class");
        Class handlerClass = null;
        try {
            handlerClass = Class.forName(handlerClassName);
        } catch (Exception e) {
            try {
                handlerClass = Class.forName(defaultHandlerPackage + handlerClassName);
            } catch (Exception ee) {
                handlerClass = defaultHandlerClass;
            }
        }
        try {
            Class types[] = { String.class, String.class };
            Constructor constructor = handlerClass.getConstructor(types);
            Object params[] = { handlerPropertyPrefix, name };
            Handler handler = (Handler) constructor.newInstance(params);
            lookupFilterAndAddHandler(handler, name);
            checkAndBindHandler(name, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This checks to see if the Handler needs to be bound in the RMI
     * registry as a server. The bind is optional so that the normal
     * local logging handlers won't hang the program when the program
     * completes normally.
     *
     * <p>property log.handler.<i>name</i>.server true|false false
     * installs the named handler in the RMI registry with the key
     * "log.handler.name.<i>vmid<i>".
     *
     * @param name name of handler to bind
     * @param handler to bind
     **/
    protected void checkAndBindHandler(String name, Handler handler) {
        String fullPrefix = handlerPropertyPrefix + name + "." + this.getName();
        String server = properties.get(fullPrefix + ".server", "false");
        if (server.equals("true")) {
            try {
                java.rmi.Naming.rebind(fullPrefix, java.rmi.server.UnicastRemoteObject.exportObject(handler));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This binds the Distributer in the RMI registry if the property
     * is set.  This is useful if the Distributer will be used as a
     * remote centralised server.
     *
     * property log.distributer.<i>vmid</i>
     **/
    protected void checkAndBindDistributer() {
        if (properties.get("log.server", "false").equals("true")) {
            String bindName = "log.distributer." + this.getName();
            try {
                internal("trying to bind distributer under name '" + bindName + "'");
                java.rmi.Naming.rebind(bindName, java.rmi.server.UnicastRemoteObject.exportObject(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This is called when a handler is specified as remote using the
     * log.handler property.  This queries the remote object and
     * installs the remote object or the ProxyHandler depending on
     * what type the remote object is.
     **/
    protected void addRemoteObject(String name, String url) {
        try {
            Object remote = java.rmi.Naming.lookup(url);
            if (remote != null) {
                if (remote instanceof Handler) {
                    lookupFilterAndAddHandler((Handler) remote, name);
                } else if (remote instanceof Distributer) {
                    Distributer distributer = (Distributer) remote;
                    try {
                        distributer.getName();
                        lookupFilterAndAddHandler(new ProxyHandler(distributer), name);
                    } catch (Exception e) {
                    }
                }
            } else {
                internal("null handler");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This takes the given handler and handler name and looks up a
     * list of events to which this handler should respond and then
     * installs the handler.
     **/
    protected void lookupFilterAndAddHandler(Handler handler, String name) {
        String events = properties.get(handlerPropertyPrefix + name + ".events", "");
        addToEventHandlers(handler, name, events);
    }

    /**
     * Takes a space separated list of regular expressions and creates
     * RE objects and adds them to the given vector of expressions.
     **/
    private void setupFilters(String filters, Vector expressions) {
        java.util.StringTokenizer tokens = new java.util.StringTokenizer(filters, " ");
        while (tokens.hasMoreTokens()) {
            String filter = tokens.nextToken();
            try {
                RE expression = new RE(filter);
                expressions.addElement(expression);
            } catch (Exception e) {
                System.out.println("Log warning: bad filter '" + filter + "'");
            }
        }
    }

    /**
     * Sets the given space separated list of regular expressions that
     * will act as filters to include functions in the logging output.
     * When distributing a log event, each space separated expression
     * in the filter string is matched against the concatenation of
     * the full classname and the function name (without the
     * signature).  If any one expression matches, the event is
     * distributed.
     *
     * <p>Calling this function overrides any values specified
     * in the property "log.function.include".
     *
     * <p>Note, the include list is processed before the exclude list.
     * This means any event included by this filter can be
     * subsequently excluded by the exclude list.
     *
     * <p>Some useful expressions are:
     * <ul>
     *
     * <li>"grace\.MyClass\..*" logs all events produced by the
     * class grace.MyClass.
     *
     * <li>"grace\.*" will log all events produced by all classes and
     * functions in the grace package.
     *
     * <li>"grace\.MyClass\.myFunc" logs all events produced by
     * grace.MyClass.myFunc().
     *
     * </ul>
     *
     * @param filters space separated list of regular expressions
     **/
    public void setFunctionNamesToInclude(String filters) {
        properties.put("log.function.include", filters);
        initializeFilters();
    }

    /**
     * Sets the given space separated list of regular expressions that
     * will act as filters to exclude functions in the logging output.
     * When distributing a log event, each space separated expression
     * in the filter string is matched against the concatenation of
     * the full classname and the function name (without the
     * signature).  If any one expression matches, the event is
     * not distributed.
     *
     * <p>Calling this function overrides any values specified
     * in the property "log.function.exclude".
     *
     * <p>Note, the exclude list is processed after the include list.
     * This means that this filter has the utlitimate decision of
     * whether the event is distributed.
     *
     * <p>Some useful expressions are:
     * <ul>
     *
     * <li>"grace\.MyClass\..*" eliminates all events produced by the
     * class grace.MyClass.
     *
     * <li>"grace\.*" eliminates all events produced by all classes and
     * functions in the grace package.
     *
     * <li>"grace\.MyClass\.myFunc" eliminates all events produced by
     * grace.MyClass.myFunc().
     *
     * </ul>
     *
     * @param filters space separated list of regular expressions
     **/
    public void setFunctionNamesToExclude(String filters) {
        properties.put("log.function.exclude", filters);
        initializeFilters();
    }

    /**
     * Sets the given space separated list of regular expressions that
     * will act as filters to include event types in the logging
     * output.  When distributing a log event, each space separated
     * expression in the filter string is matched against the event
     * type (error, warning, notice, etc). If any one expression
     * matches, the event is distributed.
     *
     * <p>Calling this function overrides any values specified
     * in the property "log.event.include".
     *
     * <p>Note, the include list is processed before the exclude list.
     * This means any event included by this filter can be
     * subsequently excluded by the exclude list.
     *
     * <p>Some useful expressions are:
     * <ul>
     *
     * <li>".*" includes all events types.
     *
     * <li>"error warning" includes all error and warning events.
     *
     * <li>"stat.*" includes all events starting with 'stat'.
     *
     * </ul>
     *
     * @param filters space separated list of regular expressions
     **/
    public void setEventTypesToInclude(String filters) {
        properties.put("log.event.include", filters);
        initializeFilters();
    }

    /**
     * Sets the given space separated list of regular expressions that
     * will act as filters to exclude event types in the logging
     * output.  When distributing a log event, each space separated
     * expression in the filter string is matched against the event
     * type If any one expression matches, the event is not
     * distributed.
     *
     * <p>Calling this function overrides any values specified
     * in the property "log.event.exclude".
     *
     * <p>Note, the exclude list is processed after the include list.
     * This means that this filter has the utlitimate decision of
     * whether the event is distributed.
     *
     * @param filters space separated list of regular expressions
     **/
    public void setEventTypesToExclude(String filters) {
        properties.put("log.event.exclude", filters);
        initializeFilters();
    }

    /**
     * Returns the properties list that is statically maintained by
     * this class.
     **/
    public Properties getProperties() {
        return properties;
    }

    /**
     * Loads the given set of extraProperties without affecting the
     * System properties or the original properties maintained by this
     * Log. It loads the given extraProperties into a new, temporary
     * set of internally properties, loads the given extraProperties,
     * reinitializes all of the filters with the revised set of
     * properties, then sets the properties back to the original set.
     *
     * @param extraProperties to merge into the current set
     **/
    public void loadProperties(java.util.Properties extraProperties) {
        Properties original = properties;
        properties = new Properties(original);
        properties.load(extraProperties);
        initializeFilters();
        properties = original;
    }

    /**
     * This loads the properties and sets up the filters for including
     * and excluding functions and event types.  Each time it is
     * called, it clears the functionNamesToInclude,
     * functionNamesToExclude, eventTypesToInclude, and
     * eventTypesToExclude.
     **/
    public void initializeFilters() {
        internal("initializing filters");
        String allEnabled = properties.get("log", "true");
        String defaultInclude = allEnabled.equals("true") ? ".*" : "";
        String defaultExclude = "";
        functionNamesToInclude.removeAllElements();
        functionNamesToExclude.removeAllElements();
        eventTypesToInclude.removeAllElements();
        eventTypesToExclude.removeAllElements();
        setupFilters(properties.get(new String[] { "log.function.include", "log.functions.include", "log.include.functions" }, ".*"), functionNamesToInclude);
        internal("function include filter '" + functionNamesToInclude + "'");
        setupFilters(properties.get(new String[] { "log.function.exclude", "log.functions.exclude", "log.exclude.functions" }, ""), functionNamesToExclude);
        internal("function exclude filter '" + functionNamesToExclude + "'");
        setupFilters(properties.get(new String[] { "log.event.include", "log.events.include", "log.events" }, defaultInclude), eventTypesToInclude);
        internal("event include filter '" + functionNamesToExclude + "'");
        setupFilters(properties.get(new String[] { "log.event.exclude", "log.events.exclude" }, defaultExclude), eventTypesToExclude);
        internal("event exclude filter '" + functionNamesToExclude + "'");
        String enabled = properties.get(new String[] { "log.errors", "log.error" }, null);
        if (enabled != null) {
            enableEventType(ERROR, enabled.equals("true"));
        }
        enabled = properties.get(new String[] { "log.warnings", "log.warning" }, null);
        if (enabled != null) {
            enableEventType(WARNING, enabled.equals("true"));
        }
        enabled = properties.get(new String[] { "log.notices", "log.notice" }, null);
        if (enabled != null) {
            enableEventType(NOTICE, enabled.equals("true"));
        }
        enabled = properties.get(new String[] { "log.traces", "log.trace" }, null);
        if (enabled != null) {
            enableEventType(TRACE, enabled.equals("true"));
        }
    }

    /**
     * Initializes all of the elements of a Log/Distributer object.
     * This means setting up handlers, of if none, at least one
     * standard out handler, setting up the enabled function and event
     * type filters, and binding this distributer object in the local
     * rmi register, if requested.
     **/
    protected void initialize() {
        internal("initializing");
        try {
            if (!allHandlers.containsKey(standardOutName)) {
                if (properties.get(handlerPropertyPrefix + standardOutName, "true").equals("true")) {
                    addToEventHandlers(new StandardOutHandler(handlerPropertyPrefix, standardOutName), standardOutName, "");
                }
            }
        } catch (Exception e) {
            internal(e);
        }
        try {
            Vector names = properties.names('^' + handlerPropertyPrefix + ".*");
            for (int i = 0; i < names.size(); ++i) {
                String key = (String) names.elementAt(i);
                int begin = handlerPropertyPrefix.length();
                int end = key.indexOf(".", begin);
                String name = null;
                if (end == -1) {
                    if (key.length() > begin) name = key.substring(begin, key.length());
                } else name = key.substring(begin, end);
                if (!allHandlers.containsKey(name)) addHandler(key.substring(begin, end));
            }
        } catch (Exception e) {
            internal(e);
        }
        initializeFilters();
        checkAndBindDistributer();
        internal("done initializing");
    }

    /**
     * This main acts as a little test program to test many of the
     * features of the Log.  It tests some multi-threaded behavior as
     * well as many of the functions.  Unfortunately, since most of
     * the functions output text in user configurable way, this test
     * doesn't do anything more than print out a bunch of text and let
     * the user see if it looks correct.
     **/
    public static void main(String[] args) {
        int numThreads = 1;
        if (args.length > 0) {
            numThreads = Integer.parseInt(args[0]);
        }
        new grace.log.test.GeneralTest(numThreads).run();
    }
}
