package perun.isle.trigger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import perun.common.exception.InvalidDataException;
import perun.common.exception.TriggerNotFoundException;
import perun.common.log.Log;

/**
 * Implements registry of event queues and triggers.
 */
public class EventTriggerHandler extends UnicastRemoteObject implements TriggerRegistryRI {

    /**
	 * Internal structure to store Triggers
	 */
    private final Map<Integer, Trigger> triggers;

    /**
	 * Stores event queues.
	 */
    private final Map<String, Triggable> queues;

    /**
	 * Counter for unique id allocation.
	 */
    private int triggerID;

    /**
	 * singleton
	 */
    private static EventTriggerHandler instance = getInstance();

    /**
	 * Gets instance of handler.
	 */
    public static EventTriggerHandler getInstance() {
        try {
            if (instance == null) instance = new EventTriggerHandler();
        } catch (RemoteException re) {
            Log.exception(Log.ERROR, "failed to init trigger registry", re);
        }
        return instance;
    }

    /**
	 * Creates a new instance of EventTriggerHandler
	 * @throws RemoteException
	 */
    private EventTriggerHandler() throws RemoteException {
        queues = new HashMap<String, Triggable>();
        triggers = new HashMap<Integer, Trigger>();
    }

    /**
	 * Adds new queue to the system.
	 * @param Type queue type
	 * @param q new queue
	 */
    public synchronized void registerQueue(String Type, Triggable q) {
        Log.event(Log.INFO, "Registering new trigger event queue, type: " + Type);
        queues.put(Type, q);
    }

    public synchronized int addTrigger(String triggerClass) throws RemoteException {
        return addTrigger(triggerClass, null);
    }

    /**
	 * Adds trigger to the system.
	 * @param triggerClass specifies the trigger
	 * @param data configuration data for trigger
	 * @return identification of new trigger
	 */
    public synchronized int addTrigger(String className, String data) {
        int id = triggerID++;
        try {
            Constructor triggerConstructor = Class.forName(className).getConstructor(new Class[] { int.class });
            Trigger newTrigger = (Trigger) triggerConstructor.newInstance(new Object[] { id });
            Log.event(Log.INFO, "Registering new trigger type: " + newTrigger.getType());
            triggers.put(id, newTrigger);
            if (data != null) newTrigger.setProperties(data);
            Triggable queue = queues.get(newTrigger.getType());
            queue.RegisterTrigger(id, newTrigger);
            return id;
        } catch (ClassNotFoundException cnfnde) {
            Log.exception(Log.ERROR, "Triggger class not found", cnfnde);
        } catch (NoSuchMethodException nmtde) {
            Log.exception(Log.ERROR, "Trigger class has not requied constructor", nmtde);
        } catch (InstantiationException ie) {
            Log.exception(Log.ERROR, ie);
        } catch (IllegalAccessException ilglacce) {
            Log.exception(Log.ERROR, "Illegal access to constructor", ilglacce);
        } catch (RemoteException re) {
            Log.exception(Log.ERROR, re);
        } catch (InvocationTargetException inve) {
            Log.exception(Log.ERROR, "Cannot invocate target constructor", inve);
        } catch (InvalidDataException invdatae) {
            Log.exception(Log.ERROR, invdatae);
        }
        return -1;
    }

    public synchronized void removeTrigger(int id) throws TriggerNotFoundException, RemoteException {
        Trigger tr = triggers.remove(id);
        queues.get(tr.getType()).UnregisterTrigger(id);
    }

    public synchronized void removeAllTriggers() throws RemoteException {
        triggers.clear();
    }

    public synchronized TriggerRI getTrigger(int id) throws RemoteException, TriggerNotFoundException {
        TriggerRI trigger = (TriggerRI) triggers.get(id);
        if (trigger == null) throw new TriggerNotFoundException(id);
        return trigger;
    }

    public synchronized Integer[] getAllTriggers() throws RemoteException {
        Set<Integer> trigs = triggers.keySet();
        return trigs.toArray(new Integer[trigs.size()]);
    }

    /**
	 * Called when new event occurs.
	 * @param type type of event
	 * @param param parameter for triggers
	 * @return returns value from queue
	 */
    public synchronized int event(String type, Object param) {
        return queues.get(type).ProcessTriggers(param);
    }
}
