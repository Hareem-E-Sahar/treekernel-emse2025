package org.alicebot.server.core;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.alicebot.server.core.util.DeveloperErrorException;
import org.alicebot.server.core.util.Trace;

/**
 *  <p>
 *  Maintains in-memory predicate values for userids.
 *  Every public set and get method checks the size of the cache,
 *  and saves out part of it if it has exceeded a configurable limit.
 *  </p>
 *  <p>
 *  This currently has the defect that it doesn't choose intelligently
 *  which userids' predicates to cache (it should do this for the ones
 *  who have not been heard from the longest).  The HashMap that contains
 *  the predicates (keyed by userid) makes no guarantees about order. :-(
 *  </p>
 *
 *  @author Noel Bush
 *  @since  4.1.4'
 */
public class PredicateMaster {

    /** Maximum length of indexed predicates. */
    private static final int MAX_INDEX = 5;

    /** Contains information about some predicates set at load time. */
    private static final HashMap predicatesInfo = new HashMap();

    /** Holds cached predicates, keyed by userid. */
    private static final Map cache = Collections.synchronizedMap(new HashMap());

    /** The maximum size of the cache. */
    private static final int cacheMax = Globals.predicateValueCacheMax();

    /** The preferred minimum value for the cache (starts at half of {@link #cacheMax}, may be adjusted). */
    private static int cacheMin = Math.max(cacheMax / 2, 1);

    /** A counter for tracking the number of predicate value cache operations. */
    protected static int cacheSize = 0;

    /** The listeners to the PredicateMaster. */
    private static HashSet listeners = new HashSet();

    /** Private instance of self. */
    private static final PredicateMaster myself = new PredicateMaster();

    /** Whether the PredicateMaster is available. */
    private static boolean available = true;

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /**
     *  Private constructor.
     */
    private PredicateMaster() {
    }

    /**
     *  Prohibits cloning this class.
     */
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     *  Sets a predicate <code>value</code> against a
     *  predicate <code>name</code> for a given userid,
     *  and returns either the <code>name</code> or the <code>value</code>,
     *  depending on the predicate type.
     *
     *  @param name     the predicate name
     *  @param value    the predicate value
     *  @param userid   the userid
     *
     *  @return the <code>name</code> or the <code>value</code>, depending on the predicate type
     */
    public static synchronized String set(String name, String value, String userid) {
        waitUntilAvailable();
        Map userPredicates = predicatesFor(userid);
        userPredicates.put(name, value);
        cacheSize++;
        checkCache();
        return nameOrValue(name, value);
    }

    /**
     *  Sets a <code>value</code> against an indexed
     *  predicate <code>name</code> for a given <code>userid</code>,
     *  and returns either the <code>name</code> or the <code>value</code>,
     *  depending on the predicate type.
     *
     *  @param name     the predicate name
     *  @param index    the index
     *  @param value    the predicate value
     *  @param userid   the userid
     *
     *  @return the <code>name</code> or the <code>value</code>, depending on the predicate type
     */
    public static synchronized String set(String name, int index, String value, String userid) {
        waitUntilAvailable();
        Map userPredicates = predicatesFor(userid);
        Vector values = getLoadOrCreateValueVector(name, userPredicates, userid);
        ;
        try {
            values.add(index - 1, value);
        } catch (IndexOutOfBoundsException e) {
            pushOnto(values, value);
        }
        cacheSize++;
        checkCache();
        return nameOrValue(name, value);
    }

    /**
     *  Pushes a <code>value</code> onto an indexed
     *  predicate <code>name</code> for a given <code>userid</code>,
     *  and returns either the <code>name</code> or the <code>value</code>,
     *  depending on the predicate type.
     *
     *  @param name     the predicate name
     *  @param value    the predicate value
     *  @param userid   the userid
     *
     *  @return the <code>name</code> or the <code>value</code>, depending on the predicate type
     */
    public static synchronized String push(String name, String value, String userid) {
        waitUntilAvailable();
        Map userPredicates = predicatesFor(userid);
        Vector values = getLoadOrCreateValueVector(name, userPredicates, userid);
        pushOnto(values, value);
        cacheSize++;
        checkCache();
        return nameOrValue(name, value);
    }

    /**
     *  Gets the predicate <code>value</code> associated
     *  with a <code>name</code> for a given <code>userid</code>.
     *
     *  @param name     the predicate name
     *  @param userid   the userid
     *
     *  @return the <code>value</code> associated with the given <code>name</code>,
     *          for the given <code>userid</code>
     */
    public static synchronized String get(String name, String userid) {
        waitUntilAvailable();
        String value;
        Map userPredicates = predicatesFor(userid);
        Object valueObject = userPredicates.get(name);
        if (valueObject == null) {
            try {
                value = ActiveMultiplexor.getInstance().loadPredicate(name, userid);
            } catch (NoSuchPredicateException e0) {
                value = bestAvailableDefault(name);
            }
            if (value != null) {
                userPredicates.put(name, value);
            } else {
                throw new DeveloperErrorException("Null string found in user predicates cache!");
            }
        } else if (valueObject instanceof String) {
            value = (String) valueObject;
        } else if (valueObject instanceof Vector) {
            Vector values = (Vector) valueObject;
            value = (String) values.firstElement();
        } else {
            throw new DeveloperErrorException("Something other than a String or a Vector found in user predicates cache!");
        }
        checkCache();
        return value;
    }

    /**
     *  Gets the predicate <code>value</code> associated
     *  with a <code>name</code> at a given <code>index</code>
     *  for a given <code>userid</code>.
     *
     *  @param name     the predicate name
     *  @param index    the index
     *  @param userid   the userid
     *
     *  @return the <code>value</code> associated with the given <code>name</code>
     *          at the given <code>index</code>, for the given <code>userid</code>
     */
    public static synchronized String get(String name, int index, String userid) {
        waitUntilAvailable();
        Map userPredicates = predicatesFor(userid);
        String value = null;
        Vector values = null;
        try {
            values = getValueVector(name, userPredicates);
        } catch (NoSuchPredicateException e0) {
            try {
                values = loadValueVector(name, userPredicates, userid);
            } catch (NoSuchPredicateException e1) {
                value = bestAvailableDefault(name);
                userPredicates.put(name, value);
            }
        }
        if (values != null) {
            try {
                value = (String) values.get(index - 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                value = bestAvailableDefault(name);
            }
        }
        checkCache();
        return value;
    }

    /**
     *  Waits until {@link #available} is <code>true</code>
     *  (or the thread is interrupted).
     */
    private static void waitUntilAvailable() {
        while (!available) {
            try {
                myself.wait(5000);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     *  Pushes a value onto the front of a list, and pops off
     *  any values at the end of the list so that the list size
     *  is no more than {@link #MAX_INDEX}.
     *
     *  @param values   the list onto which to push
     *  @param value    the value to push onto the list
     */
    private static synchronized void pushOnto(Vector values, Object value) {
        values.add(0, value);
        while (values.size() > MAX_INDEX) {
            values.remove(values.size() - 1);
        }
    }

    /**
     *  Returns, from the cache, a Vector of values assigned to a <code>name</code>
     *  for a predicate for a <code>userid</code>.  If the <code>name</code>
     *  exists in a predicate for the <code>userid</code> but it is not
     *  indexed, it is converted into an indexed value.  If it does not
     *  exist at all, a <code>NoSuchPredicateException</code> is thrown.
     *
     *  @param name             the name of the predicate
     *  @param userPredicates   the existing map of predicates
     *
     *  @return a list of values assigned to a <code>name</code>
     *          for a predicate for a <code>userid</code>
     *
     *  @throws NoSuchPredicateException if no values are assigned to the <code>name</code>
     */
    private static synchronized Vector getValueVector(String name, Map userPredicates) throws NoSuchPredicateException {
        Vector values;
        if (userPredicates.size() > 0 && userPredicates.containsKey(name)) {
            Object valueObject = userPredicates.get(name);
            if (valueObject instanceof String) {
                if (valueObject != null) {
                    values = createValueVector(name, userPredicates);
                    values.add((String) valueObject);
                } else {
                    throw new DeveloperErrorException("Null String found as value in predicates!");
                }
            } else if (valueObject instanceof Vector) {
                if (valueObject != null) {
                    values = (Vector) valueObject;
                } else {
                    throw new DeveloperErrorException("Null Vector found as value in predicates!");
                }
            } else {
                throw new DeveloperErrorException("Something other than a String or Vector found in predicates!");
            }
        } else {
            throw new NoSuchPredicateException(name);
        }
        return values;
    }

    /**
     *  Tries to load a predicate with <code>name</code> for <code>userid</code>
     *  from the ActiveMultiplexor into the <code>userPredicates</code>.
     *  If successful, tries to get the value list
     *  for name.  If unsuccessful, throws a NoSuchPredicateException.
     *
     *  @param name             the predicate <code>name</code>
     *  @param userPredicates   the user predicates (must not be null!)
     *  @param userid           the userid
     *
     *  @return a Vector of values assigned to a <code>name</code>
     *          for a predicate for a <code>userid</code>
     *
     *  @throws NoSuchPredicateException if no values are assigned to the <code>name</code>
     *  @throws NullPointerException if <code>userPredicates</code> is null
     */
    private static synchronized Vector loadValueVector(String name, Map userPredicates, String userid) throws NoSuchPredicateException, NullPointerException {
        if (userPredicates == null) {
            throw new NullPointerException("Cannot call loadValueVector with null userPredicates!");
        }
        int index = 1;
        String value;
        try {
            value = ActiveMultiplexor.getInstance().loadPredicate(name + '.' + index, userid);
        } catch (NoSuchPredicateException e0) {
            throw new NoSuchPredicateException(name);
        }
        Vector values = createValueVector(name, userPredicates);
        values.add(value);
        try {
            while (index <= MAX_INDEX) {
                index++;
                values.add(ActiveMultiplexor.getInstance().loadPredicate(name + '.' + index, userid));
            }
        } catch (NoSuchPredicateException e1) {
        }
        return values;
    }

    /**
     *  Creates a value list for the given predicate <code>name</code>
     *  and given <code>userid</code>.
     *
     *  @param name             the predicate name
     *  @param userPredicates   the predicates map to which to add the list
     *
     *  @return the new list
     */
    private static synchronized Vector createValueVector(String name, Map userPredicates) {
        Vector values = new Vector();
        userPredicates.put(name, values);
        return values;
    }

    /**
     *  Returns a value list one way or another: first tries to
     *  get it from the cache, then tries to load it from the
     *  ActiveMultiplexor; finally creates a new one if the preceding failed.
     *
     *  @param name             the predicate <code>name</code>
     *  @param userPredicates   the user predicates map
     *  @param userid           the userid
     *
     *  @return a value list in <code>userPredicates</code> for <code>name</code> for <code>userid</code>
     */
    private static synchronized Vector getLoadOrCreateValueVector(String name, Map userPredicates, String userid) {
        Vector values;
        try {
            values = getValueVector(name, userPredicates);
        } catch (NoSuchPredicateException e0) {
            try {
                values = loadValueVector(name, userPredicates, userid);
            } catch (NoSuchPredicateException e1) {
                values = createValueVector(name, userPredicates);
            }
        }
        return values;
    }

    /**
     *  Returns the best available default predicate <code>value</code>
     *  for a predicate <code>name</code>
     *
     *  @param name the predicate name
     */
    private static String bestAvailableDefault(String name) {
        if (predicatesInfo.containsKey(name)) {
            String value = ((PredicateInfo) predicatesInfo.get(name)).defaultValue;
            if (value != null) {
                return value;
            }
        }
        return Globals.getPredicateEmptyDefault();
    }

    /**
     *  Returns the name or value of a predicate, depending
     *  on whether or not it is &quot;return-name-when-set&quot;.
     *
     *  @param name     the predicate name
     *  @param value    the predicate valud
     */
    private static String nameOrValue(String name, String value) {
        if (predicatesInfo.containsKey(name)) {
            if (((PredicateInfo) predicatesInfo.get(name)).returnNameWhenSet) {
                return name;
            }
        }
        return value;
    }

    /**
     *  Returns the map of predicates for a userid if it is cached,
     *  or a new map if it is not cached.
     *
     *  @param userid
     */
    private static synchronized Map predicatesFor(String userid) {
        Map userPredicates;
        if (!cache.containsKey(userid)) {
            userPredicates = Collections.synchronizedMap(new HashMap());
            cache.put(userid, userPredicates);
        } else {
            userPredicates = (Map) cache.get(userid);
            if (userPredicates == null) {
                throw new DeveloperErrorException("userPredicates is null.");
            }
        }
        return userPredicates;
    }

    /**
     *  Registers some information about a predicate in advance.
     *  Not required; just used when it is necessary to specify a
     *  default value for a predicate and/or specify its type
     *  as return-name-when-set.
     *
     *  @param name                 the name of the predicate
     *  @param defaultValue         the default value (if any) for the predicate
     *  @param returnNameWhenSet    whether the predicate should return its name when set
     *  @param botid                the bot id for whom to register the predicate this way (not yet used)
     */
    public static void registerPredicate(String name, String defaultValue, boolean returnNameWhenSet, String botid) {
        available = false;
        PredicateInfo predicate = new PredicateInfo();
        predicate.name = name;
        predicate.defaultValue = defaultValue;
        predicate.returnNameWhenSet = returnNameWhenSet;
        predicatesInfo.put(name, predicate);
        available = true;
    }

    /**
     *  Attempts to dump a given number of predicate values
     *  from the cache, starting with the oldest userid first.
     *
     *  @param dumpCount    the number of values to try to dump
     *
     *  @return the number that were actually dumped
     */
    private static synchronized int save(int dumpCount) {
        available = false;
        if (!cache.isEmpty()) {
            Iterator userids = cache.keySet().iterator();
            while (userids.hasNext()) {
                String userid = (String) userids.next();
                Map userPredicates = (Map) cache.get(userid);
                Iterator predicates = userPredicates.keySet().iterator();
                while (predicates.hasNext()) {
                    String name = (String) predicates.next();
                    Object valueObject = userPredicates.get(name);
                    if (valueObject instanceof String) {
                        String value = (String) userPredicates.get(name);
                        if (!value.equals(bestAvailableDefault(name))) {
                            ActiveMultiplexor.getInstance().savePredicate(name, value, userid);
                        }
                    } else if (valueObject instanceof Vector) {
                        Vector values = (Vector) userPredicates.get(name);
                        int valueCount = values.size();
                        for (int index = 1; index <= valueCount; index++) {
                            String value = (String) values.get(index - 1);
                            if (!value.equals(bestAvailableDefault(name))) {
                                ActiveMultiplexor.getInstance().savePredicate(name + '.' + index, value, userid);
                            }
                        }
                    } else {
                        throw new DeveloperErrorException("Something other than a String or Vector found in predicates!");
                    }
                }
                cacheSize -= userPredicates.size();
                predicates.remove();
            }
        }
        Iterator listenerIterator = listeners.iterator();
        while (listenerIterator.hasNext()) {
            PredicateMasterListener listener = (PredicateMasterListener) listenerIterator.next();
            listener.updateUserids(Collections.unmodifiableSet(cache.keySet()));
        }
        available = true;
        return cacheSize;
    }

    /**
     *  Dumps the entire cache.
     */
    static synchronized void saveAll() {
        available = false;
        Trace.userinfo("Saving all cached predicates (userids: " + cache.size() + ").");
        save(cache.size());
        Trace.userinfo("Finished saving cached predicates.");
        available = true;
    }

    /**
     *  Checks the predicate cache, and saves out predicates
     *  if necessary.
     */
    private static void checkCache() {
        if (cacheSize >= cacheMax) {
            int resultSize = save((cacheSize - cacheMin));
            if (resultSize < cacheMin) {
                cacheMin = (resultSize + cacheMin) / 2;
            }
        }
    }

    /**
     *  Registers a PredicateMasterListener to receive events.
     *
     *  @param listener     the listener
     */
    public static void registerListener(PredicateMasterListener listener) {
        listeners.add(listener);
    }
}
