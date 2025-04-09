package com.csam.stubs.actions;

import com.csam.stubs.Action;
import java.lang.reflect.Constructor;

/**
 * This action specifies that aexception is to be thrown by a stubbed method.
 *
 * @author Nathan Crause
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ThrowAction implements Action {

    private Class exceptionClass;

    private String message;

    /**
     * Constructs a new <code>ThrowAction</code> which is expected to throw
     * an exception of the specified type with the specified detail message.
     *
     * @param exceptionClass the type of exception to throw
     * @param message the exception's detail message
     * @since 1.0.0
     */
    public ThrowAction(Class exceptionClass, String message) {
        this.exceptionClass = exceptionClass;
        this.message = message;
    }

    /**
     * Throws the exception specified to the instantiation time.
     *
     * @throws java.lang.Throwable the exception specified.
     * @since 1.0.0
     */
    public void throwException() throws Throwable {
        Constructor c = exceptionClass.getConstructor(String.class);
        throw (Throwable) c.newInstance(message);
    }

    @Override
    public String toString() {
        return "Throw exception '" + exceptionClass.getName() + "' with message '" + message + "'";
    }
}
