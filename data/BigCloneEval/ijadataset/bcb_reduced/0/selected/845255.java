package de.fhg.igd.logging;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import de.fhg.igd.util.URL;

/**
 * This is an abstract wrapper class for a logger instance of the underlying
 * logging mechanism. On configuration change requests, updates of output
 * destinations and log levels will be performed on all affected instances
 * through a bunch of (hidden) static methods, implicitly called by the
 * {@link LoggingConfiguration}.
 * <p>Specific operations like actually posting log messages or dealing
 * with {@link Forwarder} objects are defined here but yet need to be
 * implemented by subclasses for each specific underlying logging system.
 *
 * @author Matthias Pressfreund
 * @version "$Id: AbstractWrapper.java 1913 2007-08-08 02:41:53Z jpeters $"
 */
public abstract class AbstractWrapper implements Wrapper {

    /**
     * Storage for references of deployed {@link Forwarder} objects
     */
    private static Set forwarders_ = new HashSet();

    /**
     * The storage for all deployed target loggers
     */
    private static Set cache_ = new HashSet();

    /**
     * The reference counter
     */
    private int cnt_;

    /**
     * The name of the wrapped target logger
     */
    protected String name_;

    /**
     * The output destination name
     */
    protected String oname_;

    /**
     * The log level name
     */
    protected String lname_;

    /**
     * The log level
     */
    protected LogLevel level_;

    /**
     * Hidden construction.
     *
     * @param name The name of the wrapped logger
     *
     * @param ref A reference to the requesting instance
     */
    protected AbstractWrapper(String name, LoggerImpl ref) {
        ConfigurationParameters.BestMatch bo;
        ConfigurationParameters.BestMatch bl;
        LoggingConfiguration lc;
        Iterator i;
        cnt_ = 1;
        name_ = name;
        lc = LoggingConfiguration.atPresent();
        bo = lc.getOutput(name);
        bl = lc.getLogLevel(name);
        oname_ = bo.getName();
        lname_ = bl.getName();
        initTarget(ref);
        removeAllForwarders();
        for (i = ((Set) bo.getValue()).iterator(); i.hasNext(); ) {
            addForwarderFor((URL) i.next());
        }
        setLogLevel((LogLevel) bl.getValue());
    }

    /**
     * Initialize the implementation specific logger target.
     *
     * @param ref The <code>Logger</code> implementation that originally
     *   requested this wrapper
     */
    protected abstract void initTarget(LoggerImpl ref);

    /**
     * Request an <code>AbstractWrapper</code> for a given name.
     *
     * @param name The name of the requested wrapper
     * @param wclazz The {@link Wrapper} implementation class of the
     *   requested wrapper
     * @param ref A reference to the requesting instance
     *
     * @return The requested wrapper
     *
     * @throws LoggingException in case the requested wrapper could
     *   not be created
     */
    static AbstractWrapper requestFor(String name, Class wclazz, LoggerImpl ref) {
        AbstractWrapper wrapper;
        AbstractWrapper cached;
        Iterator i;
        if (name == null || wclazz == null || ref == null) {
            throw new LoggingException("Internal error");
        }
        wrapper = null;
        synchronized (cache_) {
            for (i = cache_.iterator(); i.hasNext(); ) {
                cached = (AbstractWrapper) i.next();
                if (wclazz.isInstance(cached) && cached.name_.equals(name)) {
                    wrapper = cached;
                    break;
                }
            }
            if (wrapper == null) {
                try {
                    wrapper = (AbstractWrapper) wclazz.getConstructor(new Class[] { String.class, LoggerImpl.class }).newInstance(new Object[] { name, ref });
                } catch (Throwable t) {
                    if (t instanceof InvocationTargetException) {
                        throw new LoggingException("Failed creating " + wclazz + ": " + ((InvocationTargetException) t).getTargetException().getMessage());
                    }
                    throw new LoggingException("Internal error while creating " + wclazz + ": " + t.getMessage());
                }
                cache_.add(wrapper);
            } else {
                wrapper.cnt_ += 1;
            }
        }
        return wrapper;
    }

    public String getName() {
        return name_;
    }

    public LogLevel getLogLevel() {
        return level_;
    }

    /**
     * After adding the first output destination for the given name, update
     * all wrappers whose best output names match the given name.
     *
     * @param name The name to initialize the output destinations for
     * @param output The new output destination
     */
    static void initOutput(String name, URL output) {
        ConfigurationParameters.BestMatch bo;
        AbstractWrapper wrapper;
        Iterator i;
        synchronized (cache_) {
            for (i = cache_.iterator(); i.hasNext(); ) {
                wrapper = (AbstractWrapper) i.next();
                bo = LoggingConfiguration.atPresent().getOutput(wrapper.name_);
                if (bo.getName().equals(name)) {
                    wrapper.oname_ = name;
                    wrapper.removeAllForwarders();
                    wrapper.addForwarderFor(output);
                }
            }
        }
    }

    /**
     * Add a new output destination for the given name and update all
     * wrappers whose current output names match the given name.
     *
     * @param name The name to add the new output destination for
     * @param output The new output destination
     */
    static void addOutput(String name, URL output) {
        AbstractWrapper wrapper;
        Iterator i;
        synchronized (cache_) {
            for (i = cache_.iterator(); i.hasNext(); ) {
                wrapper = (AbstractWrapper) i.next();
                if (wrapper.oname_.equals(name)) {
                    wrapper.addForwarderFor(output);
                }
            }
        }
    }

    /**
     * Remove an output destination for the given name and update all
     * wrappers whose current output name match the given name.
     *
     * @param name The name to remove the output destination for
     * @param output The output destination to remove
     */
    static void removeOutput(String name, URL output) {
        AbstractWrapper wrapper;
        Iterator i;
        synchronized (cache_) {
            for (i = cache_.iterator(); i.hasNext(); ) {
                wrapper = (AbstractWrapper) i.next();
                if (wrapper.oname_.equals(name)) {
                    wrapper.removeForwarderFor(output);
                }
            }
        }
    }

    /**
     * After deleting all output destinations of the given name, update all
     * wrappers whose output name matches the given name and replace their
     * output destinations by the best matching substitutes.
     *
     * @param name The name to delete the output destinations for
     */
    static void deleteOutput(String name) {
        ConfigurationParameters.BestMatch bo;
        AbstractWrapper wrapper;
        Iterator i;
        Iterator j;
        synchronized (cache_) {
            for (i = cache_.iterator(); i.hasNext(); ) {
                wrapper = (AbstractWrapper) i.next();
                if (wrapper.oname_.equals(name)) {
                    bo = LoggingConfiguration.atPresent().getOutput(name);
                    wrapper.oname_ = bo.getName();
                    wrapper.removeAllForwarders();
                    for (j = ((Set) bo.getValue()).iterator(); j.hasNext(); ) {
                        wrapper.addForwarderFor((URL) j.next());
                    }
                }
            }
        }
    }

    /**
     * Add a new {@link Forwarder} for the given <code>URL</code> to the
     * wrapped logger.
     *
     * @param url The new output destination to be added to the wrapped logger
     */
    protected abstract void addForwarderFor(URL url);

    /**
     * Remove a {@link Forwarder} for the given <code>URL</code> from the
     * wrapped logger.
     *
     * @param url The output destination to be removed from the wrapped logger
     */
    protected abstract void removeForwarderFor(URL url);

    /**
     * Remove all {@link Forwarder} and similar objects (e.g.
     * {@link java.util.logging.Handler} instances for the <i>Sun</i>
     * logging system) for the given <code>URL</code>.
     */
    protected abstract void removeAllForwarders();

    /**
     * Add a log level definition for the given name and update all
     * wrappers whose best log level names match the given name.
     *
     * @param name The name to add the log level for
     * @param level The log level for the given name
     */
    static void addLogLevel(String name, LogLevel level) {
        ConfigurationParameters.BestMatch bl;
        AbstractWrapper wrapper;
        Iterator i;
        synchronized (cache_) {
            for (i = cache_.iterator(); i.hasNext(); ) {
                wrapper = (AbstractWrapper) i.next();
                bl = LoggingConfiguration.atPresent().getLogLevel(wrapper.name_);
                if (bl.getName().equals(name)) {
                    wrapper.setLogLevel(level);
                }
            }
        }
    }

    /**
     * Change the log level definition for the given name and update all
     * wrappers whose current log level name match the given name.
     *
     * @param name The name to change the log level for
     * @param level The new log level of the given name
     */
    static void changeLogLevel(String name, LogLevel level) {
        AbstractWrapper wrapper;
        Iterator i;
        synchronized (cache_) {
            for (i = cache_.iterator(); i.hasNext(); ) {
                wrapper = (AbstractWrapper) i.next();
                if (wrapper.lname_.equals(name)) {
                    wrapper.setLogLevel(level);
                }
            }
        }
    }

    /**
     * Remove the log level definition for the given name and replace it by
     * the best matching substitute.
     *
     * @param name The name to add the log level for
     */
    static void removeLogLevel(String name) {
        ConfigurationParameters.BestMatch bl;
        AbstractWrapper wrapper;
        LogLevel level;
        Iterator i;
        synchronized (cache_) {
            for (i = cache_.iterator(); i.hasNext(); ) {
                wrapper = (AbstractWrapper) i.next();
                if (wrapper.lname_.equals(name)) {
                    bl = LoggingConfiguration.atPresent().getLogLevel(name);
                    wrapper.lname_ = bl.getName();
                    level = (LogLevel) bl.getValue();
                    wrapper.setLogLevel(level);
                }
            }
        }
    }

    /**
     * Set the log level of the wrapped logger.
     *
     * @param level The new log level
     */
    protected abstract void setLogLevel(LogLevel level);

    public synchronized void dismiss() {
        if (cnt_ < 1 || --cnt_ > 0) {
            return;
        }
        removeAllForwarders();
        cache_.remove(this);
    }

    /**
     * This method allows subclasses to access the protected
     * {@link Chronometer#instanceFor} to request <code>Chronometer</code>
     * objects.
     *
     * @param url The <code>URL</code> to get the <code>Chronometer</code>
     *   instance for
     *
     * @return The requested <code>Chronometer</code>
     */
    protected static Chronometer chronometerFor(URL url) {
        return Chronometer.instanceFor(url);
    }

    /**
     * Register a <code>Forwarder</code> to be updated in case of buffer
     * configuration changes.
     */
    protected static void addForwarder(Forwarder forwarder) {
        forwarders_.add(forwarder);
    }

    /**
     * Deregister a <code>Forwarder</code> from buffer configuration change
     * updates.
     */
    protected static void removeForwarder(Forwarder forwarder) {
        forwarders_.remove(forwarder);
    }

    /**
     * Change the buffer size of affected {@link Forwarder} instances (that
     * have been registered before).
     *
     * @param output The output destination of affected
     *   <code>Forwarder</code> instances
     * @param buffersize The new buffer size
     *
     * @see AbstractWrapper#addForwarder
     */
    static void changeBuffer(URL output, Integer buffersize) {
        Forwarder forwarder;
        Iterator i;
        synchronized (forwarders_) {
            for (i = forwarders_.iterator(); i.hasNext(); ) {
                forwarder = (Forwarder) i.next();
                if (forwarder.getOutputDestination().equals(output) && !forwarder.getBufferSize().equals(buffersize)) {
                    forwarder.updateBufferSize(buffersize);
                }
            }
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AbstractWrapper) {
            AbstractWrapper wrapper = (AbstractWrapper) obj;
            return (name_.equals(wrapper.name_) && oname_.equals(wrapper.oname_) && lname_.equals(wrapper.lname_) && level_.equals(wrapper.level_));
        }
        return false;
    }

    public int hashCode() {
        return (name_.hashCode() + oname_.hashCode() + lname_.hashCode() + level_.hashCode() + 63);
    }

    public String toString() {
        return ("[name='" + name_ + "',oname='" + oname_ + "',lname='" + lname_ + "',level=" + level_ + "]");
    }
}
