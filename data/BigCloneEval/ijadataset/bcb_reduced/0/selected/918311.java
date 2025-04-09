package net.sourceforge.transumanza.exception;

import java.lang.reflect.Constructor;
import net.sourceforge.transumanza.base.Component;

/**
 * @author <a href="mailto:giokarka@users.sourceforge.net">Giorgio Carchedi</a>
 */
public class LoaderExceptionWrapper {

    public static LoaderException wrapInto(Class wrapperExceptionClass, Component component, String message, Exception internal) {
        if (wrapperExceptionClass.isAssignableFrom(internal.getClass())) {
            return (LoaderException) internal;
        } else {
            try {
                Constructor con = wrapperExceptionClass.getConstructor(new Class[] { Component.class, String.class, Throwable.class });
                LoaderException wrapperException = (LoaderException) con.newInstance(new Object[] { component, message, internal });
                wrapperException.fillInStackTrace();
                return wrapperException;
            } catch (Exception e) {
                throw new RuntimeException("Error creating exception of type " + wrapperExceptionClass + ". Failed to invoke the public constructor (Component, String, Exception)", e);
            }
        }
    }

    /**
     * Wrap a generic exception passed as argument into a WriterException if is
     * not an instance of WriterException
     * 
     * @param component The component where the exception occurred
     * @param root The exception to wrap
     * @return A WriterException wrapping root exception or the root exception
     *         if is an instance of WriterException
     */
    public static WriterException intoWriterException(Component component, String message, Exception internal) {
        return (WriterException) wrapInto(WriterException.class, component, message, internal);
    }
}
