package org.jwebsocket.plugins.rpc;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 * Abstract RPCCallable class.
 * Add a method to get an instance of the rpcCallable class created.
 * @author Quentin Ambard
 */
public abstract class AbstractRPCCallable {

    private static Logger mLog = Logging.getLogger(AbstractRPCCallable.class);

    /**
	 * Return an instance of the RpcCallableClass which extends this AbstractRPCCallable class using the default constructor. 
	 * (call the method getInstanceOfRpcCallableClass(null, null))
	 * @return instance of the RPCCallable class
	 */
    public RPCCallable getInstanceOfRpcCallableClass() {
        return getInstanceOfRpcCallableClass(null, null);
    }

    /**
	 * return an instance of the RpcCallableClass which extends this AbstractRPCCallable class.
	 * Usually called with a WebSocketConnector as parameter (or null if no parameters)
	 * @param aListOfParameter
	 * @param aListOfClass
	 * @return instance of the RPCCallable class
	 */
    public RPCCallable getInstanceOfRpcCallableClass(Object[] aListOfParameter, Class[] aListOfClass) {
        RPCCallable lNewInstance = null;
        Class lClass = this.getClass();
        try {
            if (aListOfClass == null) {
                lNewInstance = (RPCCallable) lClass.getConstructor().newInstance();
            } else {
                lNewInstance = (RPCCallable) lClass.getConstructor(aListOfClass).newInstance(aListOfParameter);
            }
        } catch (Exception e) {
            mLog.error("Can't build an instance of the RPCCallable class" + lClass.getName() + ". " + "classes: " + aListOfClass + " - parameters: " + aListOfParameter + "." + e.getMessage());
            return null;
        }
        return lNewInstance;
    }
}
