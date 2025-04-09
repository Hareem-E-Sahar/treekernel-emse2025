package com.googlecode.jsonrpc4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Default implementation of the {@link ExceptionResolver}
 * interface that attemps to re-throw the same exception
 * that was thrown by the server.  This always returns
 * a {@link Throwable}.
 *
 */
public class DefaultExceptionResolver implements ExceptionResolver {

    private static final Logger LOGGER = Logger.getLogger(DefaultExceptionResolver.class.getName());

    public static final DefaultExceptionResolver INSTANCE = new DefaultExceptionResolver();

    /**
	 * {@inheritDoc}
	 */
    public Throwable resolveException(ObjectNode response) {
        ObjectNode errorObject = ObjectNode.class.cast(response.get("error"));
        if (!errorObject.has("data") || errorObject.get("data").isNull() || !errorObject.get("data").isObject()) {
            return createJsonRpcClientException(errorObject);
        }
        ObjectNode dataObject = ObjectNode.class.cast(errorObject.get("data"));
        if (!dataObject.has("exceptionTypeName") || dataObject.get("exceptionTypeName") == null || dataObject.get("exceptionTypeName").isNull() || !dataObject.get("exceptionTypeName").isTextual()) {
            return createJsonRpcClientException(errorObject);
        }
        String exceptionTypeName = dataObject.get("exceptionTypeName").getValueAsText();
        String message = dataObject.has("message") && dataObject.get("message").isTextual() ? dataObject.get("message").getValueAsText() : null;
        Throwable ret = null;
        try {
            ret = createThrowable(exceptionTypeName, message);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to create throwable", e);
        }
        if (ret == null) {
            ret = createJsonRpcClientException(errorObject);
        }
        return ret;
    }

    /**
	 * Creates a {@link JsonRpcClientException} from the given
	 * {@link ObjectNode}.
	 * @param errorObject the error object
	 * @return the exception
	 */
    private JsonRpcClientException createJsonRpcClientException(ObjectNode errorObject) {
        return new JsonRpcClientException(errorObject.get("code").getValueAsInt(), errorObject.get("message").getValueAsText(), errorObject.get("data"));
    }

    /**
	 * Attempts to create an {@link Throwable} of the given type
	 * with the given message.  For this method to create a
	 * {@link Throwable} it must have either a default (no-args)
	 * constructor or a  constructor that takes a {@code String}
	 * as the message name.  null is returned if a {@link Throwable}
	 * can't be created.
	 * @param typeName the java type name (class name)
	 * @param message the message
	 * @return the throwable
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 */
    private Throwable createThrowable(String typeName, String message) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(typeName);
        } catch (Exception e) {
            LOGGER.warning("Unable to load Throwable class " + typeName);
            return null;
        }
        if (!Throwable.class.isAssignableFrom(clazz)) {
            LOGGER.warning("Type does not inherit from Throwable" + clazz.getName());
            return null;
        }
        @SuppressWarnings("unchecked") Class<? extends Throwable> tClazz = (Class<? extends Throwable>) clazz;
        Constructor<? extends Throwable> defaultCtr = null;
        Constructor<? extends Throwable> messageCtr = null;
        try {
            defaultCtr = tClazz.getConstructor();
        } catch (Throwable t) {
        }
        try {
            messageCtr = tClazz.getConstructor(String.class);
        } catch (Throwable t) {
        }
        if (message != null && messageCtr != null) {
            return messageCtr.newInstance(message);
        } else if (message != null && defaultCtr != null) {
            LOGGER.warning("Unable to invoke message constructor for " + clazz.getName());
            return defaultCtr.newInstance();
        } else if (message == null && defaultCtr != null) {
            return defaultCtr.newInstance();
        } else if (message == null && messageCtr != null) {
            LOGGER.warning("Passing null message to message constructor for " + clazz.getName());
            return messageCtr.newInstance((String) null);
        } else {
            LOGGER.warning("Unable to find a suitable constructor for " + clazz.getName());
            return null;
        }
    }
}
