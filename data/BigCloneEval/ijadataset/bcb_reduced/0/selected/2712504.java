package ch.ethz.dcg.spamato.base.common.util;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

/**
 * Handle a Set of Identifiers and TrustValues. The TrustValues can be increased
 * and decreased using default values or by specifying respective values. All
 * data can be persistently stored in and reloaded from a zip-file.
 * 
 * @author simon schlachter
 */
public class TrustedList {

    private HashMap<String, Integer> trustedListValues = new HashMap<String, Integer>();

    private static final String TRUSTED_VALUES_NAME = "values.txt";

    private HashMap<String, Long> trustedListTimestamps = new HashMap<String, Long>();

    private static final String TRUSTED_TIMES_NAME = "timestamps.txt";

    private int lowerTrustBound;

    private int upperTrustBound;

    private int defaultTrust;

    private int trustChangeStepSize;

    /**
	 * Create a new TrustedList using the provided constraints.
	 * 
	 * @param lowerTrustBound
	 *           the minimum any trust value can be reduced to.
	 * @param upperTrustBound
	 *           the maximum any trust value can be increased to.
	 * @param defaultTrustValue
	 *           the default trust value an unknown Identifier starts with.
	 * @param trustChangeStepSize
	 *           the default amount of how a trust value is increased/decreased if
	 *           nothing else is specified.
	 */
    public TrustedList(int lowerTrustBound, int upperTrustBound, int defaultTrustValue, int trustChangeStepSize) {
        this.lowerTrustBound = lowerTrustBound;
        this.upperTrustBound = upperTrustBound;
        this.defaultTrust = defaultTrustValue;
        this.trustChangeStepSize = trustChangeStepSize;
    }

    /**
	 * Tries to load the list from a given file
	 */
    public void load(InputStream inStream) throws IOException {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        ZipInputStream zipIn = new ZipInputStream(inStream);
        synchronized (this.trustedListValues) {
            reset();
            Properties loadHelperVals = new Properties();
            Properties loadHelperTimestmps = new Properties();
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                if (zipEntry.getName().equals(TRUSTED_TIMES_NAME)) {
                    loadHelperTimestmps.load(zipIn);
                } else if (zipEntry.getName().equals(TRUSTED_VALUES_NAME)) {
                    loadHelperVals.load(zipIn);
                }
            }
            for (Object oIdentifier : loadHelperVals.keySet()) {
                String curIdentifier = (String) oIdentifier;
                Integer curTrustValue = new Integer(loadHelperVals.getProperty(curIdentifier));
                Long timestmp;
                try {
                    timestmp = new Long(dateFormat.parse(loadHelperTimestmps.getProperty(curIdentifier)).getTime());
                } catch (Exception e) {
                    timestmp = new Long(System.currentTimeMillis());
                }
                this.trustedListValues.put(curIdentifier, curTrustValue);
                if (timestmp != null) this.trustedListTimestamps.put(curIdentifier, timestmp);
            }
        }
    }

    /**
	 * Saves the list to disk
	 */
    public void save(OutputStream outStream) throws IOException {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        ZipOutputStream zipOut = new ZipOutputStream(outStream);
        synchronized (this.trustedListValues) {
            Properties saveHelperVals = new Properties();
            Properties saveHelperTimestmps = new Properties();
            for (String curKey : trustedListValues.keySet()) {
                String trustVal = String.valueOf(getTrustValue(curKey));
                String timeStmp = dateFormat.format(new Date(getTimeStamp(curKey)));
                saveHelperVals.setProperty(curKey, trustVal);
                saveHelperTimestmps.setProperty(curKey, timeStmp);
            }
            zipOut.putNextEntry(new ZipEntry(TRUSTED_VALUES_NAME));
            saveHelperVals.store(zipOut, "TrustedList. Please do not edit.");
            zipOut.closeEntry();
            zipOut.putNextEntry(new ZipEntry(TRUSTED_TIMES_NAME));
            saveHelperTimestmps.store(zipOut, "TrustedList Timestamps. Please do not edit.");
            zipOut.closeEntry();
            zipOut.finish();
        }
    }

    /**
	 * checks if the identifier is whitelisted. Being whitelisted means to have
	 * the maximum trust value.
	 * 
	 * @param identifier
	 *           the identifier to check
	 * @return <code>true</code> if the identifier has the maximum available trust
	 * value, <code>false</code> in all other cases.
	 */
    public boolean isWhitelisted(String identifier) {
        return getTrustValue(identifier) == this.upperTrustBound;
    }

    /**
	 * Checks if the identifier is blacklisted. Being blacklisted means to have
	 * the minimum trust value.
	 * 
	 * @param identifier
	 *           the identifier to check
	 * @return <code>true</code> if the identifier has the minimum availble trust
	 * value, <code>false</code> in all other cases.
	 */
    public boolean isBlacklisted(String identifier) {
        return getTrustValue(identifier) == this.lowerTrustBound;
    }

    /**
	 * Increases the Trust Value for an identifier by adding the specified value.
	 * The trust value can not be higher than the maximum allowed trust value.
	 * 
	 * @param identifier
	 *           the identifier to increase its trust value.
	 * @param howMuch
	 *           how much to increase the trust value.
	 * @return the new trust value of <code>identifier</code>
	 */
    public int increaseTrust(String identifier, int howMuch) {
        return addTrust(identifier, howMuch);
    }

    /**
	 * Increase the trust value of the specified identifier by the default
	 * increase value. Behaves exactly like <code>increaseTrust(String, int)</code>
	 * called with the default increase value.
	 * 
	 * @see #increaseTrust(String, int)
	 */
    public int increaseTrust(String identifier) {
        return increaseTrust(identifier, this.trustChangeStepSize);
    }

    /**
	 * Same as <code>increaseTrust(Vector, int)</code> but using the default
	 * increase value instead of a specified one.
	 * 
	 * @see #increaseTrust(Vector,int)
	 */
    public void increaseTrust(Vector<String> identifierStrings) {
        increaseTrust(identifierStrings, this.trustChangeStepSize);
    }

    /**
	 * Same as <code>increaseTrust(String)</code> but updates a vector of
	 * identifiers.
	 * 
	 * @see #increaseTrust(String, int)
	 */
    public void increaseTrust(Vector<String> identifierStrings, int howMuch) {
        for (String id : identifierStrings) {
            increaseTrust(id, howMuch);
        }
    }

    /**
	 * Decrease the trust value of the identifier by the default reduce-value.
	 * This method behaves exactly like <code>decreaseTrust(String,int)</code>
	 * called with the default reduce-value.
	 * 
	 * @see #decreaseTrust(String,int)
	 */
    public int decreaseTrust(String identifier) {
        return decreaseTrust(identifier, this.trustChangeStepSize);
    }

    /**
	 * Decrease the trust value of the identifier by the specified value. The
	 * method assures to not lower the trust value below the minimum allowed trust
	 * value.
	 * 
	 * @param identifier
	 *           the identifier to reduce the trust value for.
	 * @param howMuch
	 *           how much to reduce the trust value.
	 * @return the new trust value of <code>identifier</code>
	 */
    public int decreaseTrust(String identifier, int howMuch) {
        return addTrust(identifier, (-1) * howMuch);
    }

    /**
	 * Same as <code>decreaseTrust(Vector,int)</code> but by using a default value
	 * to decrease the trust values.
	 * 
	 * @see #decreaseTrust(Vector, int)
	 */
    public void decreaseTrust(Vector<String> identifierStrings) {
        decreaseTrust(identifierStrings, this.trustChangeStepSize);
    }

    /**
	 * Same as <code>decreaseTrust(String,int)</code> but by decreasing the trust
	 * values for a Vector of identifier instead of only one.
	 * 
	 * @see #decreaseTrust(String,int)
	 */
    public void decreaseTrust(Vector<String> identifierStrings, int howMuch) {
        for (String curIdentifier : identifierStrings) {
            decreaseTrust(curIdentifier, howMuch);
        }
    }

    /**
	 * Add the value specified to the trust value of the identifier while
	 * respecting upper and lower bounds for the trust value.
	 * 
	 * @param identifier
	 *           the identifier to adapt the trust value for.
	 * @param valueToAdd
	 *           how much to add to the trust value. this may be <0.
	 * @return the new trust value of <code>identifier</code>
	 */
    protected int addTrust(String identifier, int valueToAdd) {
        synchronized (this.trustedListValues) {
            int trustSoFar = getTrustValue(identifier);
            trustSoFar += valueToAdd;
            return setTrustValue(identifier, trustSoFar);
        }
    }

    /**
	 * Get a cleaned Vector containing only identifiers that are not whitelisted.
	 * The provided Vector is untouched.
	 */
    public Vector<String> removeWhitelistedOnes(Vector<String> identifierStrings) {
        Vector<String> tmp = new Vector<String>();
        for (String curIdentifier : identifierStrings) {
            if (!isWhitelisted(curIdentifier)) tmp.add(curIdentifier);
        }
        return tmp;
    }

    /**
	 * Get a cleaned Vector containing only identifiers that are not blacklisted.
	 * The provided Vector is untouched.
	 */
    public Vector<String> removeBlacklistedOnes(Vector<String> identifierStrings) {
        Vector<String> tmp = new Vector<String>();
        for (String curIdentifier : identifierStrings) {
            if (!isBlacklisted(curIdentifier)) tmp.add(curIdentifier);
        }
        return tmp;
    }

    /**
	 * Remove all data stored for the identifier.
	 * 
	 * @param identifier
	 *           the identifier to remove all data for.
	 */
    public void remove(String identifier) {
        synchronized (this.trustedListValues) {
            this.trustedListValues.remove(identifier);
            this.trustedListTimestamps.remove(identifier);
        }
    }

    /**
	 * Remove all data stored for a Vector of identifiers. All data for all
	 * identifiers in <code>identifierStrings</code> is deleted.
	 * 
	 * @param identifierStrings
	 *           a vector containing identifiers whose data should be deleted.
	 */
    public void remove(Vector<String> identifierStrings) {
        for (String curIdentifier : identifierStrings) {
            remove(curIdentifier);
        }
    }

    /**
	 * Get all Identifier-TrustValue Pairs. Identifiers are used as key in the
	 * resulting HashMap.
	 * 
	 * @return the internally used HashMap to store the TrustValues for all
	 * identifiers. Changing this data results in a data change in this
	 * TrustedList.
	 */
    public HashMap<String, Integer> getTrustedListValues() {
        return trustedListValues;
    }

    /**
	 * Set a new List of TrustValues. Existing TrustValues dropped and replaced by
	 * these ones.
	 * 
	 * @param map
	 *           the new trust values.
	 */
    public void setTrustedListValues(HashMap<String, Integer> map) {
        reset();
        synchronized (this.trustedListValues) {
            this.reset();
            this.trustedListValues.putAll(map);
        }
    }

    /**
	 * Delete all stored data.
	 */
    public void reset() {
        synchronized (this.trustedListValues) {
            this.trustedListValues.clear();
            this.trustedListTimestamps.clear();
        }
    }

    /**
	 * Delete data concering identifiers which have not been updated for at least
	 * the specified amount of milliseconds.
	 */
    public void loseOldEntries(long maxAgeMillis) {
        long oldestTimeMillis = System.currentTimeMillis() - maxAgeMillis;
        synchronized (this.trustedListValues) {
            Vector<String> toRemove = new Vector<String>();
            for (String curIdentifier : trustedListValues.keySet()) {
                long timeStmp = getTimeStamp(curIdentifier);
                if (getTimeStamp(curIdentifier) < oldestTimeMillis) {
                    toRemove.add(curIdentifier);
                }
            }
            for (String remove : toRemove) {
                this.trustedListValues.remove(remove);
                this.trustedListTimestamps.remove(remove);
            }
        }
    }

    /**
	 * Gets the accumulated Trust Values of the <code>topX</code> most trusted
	 * identifiers in <code>identifierStrings</code>.
	 */
    public int getTrustValueSum(Vector<String> identifierStrings, int topX) {
        int sum = 0;
        Collection<String> ids2SumUp;
        if (topX >= identifierStrings.size()) {
            ids2SumUp = identifierStrings;
        } else {
            ids2SumUp = getTopXOf(identifierStrings, topX);
        }
        for (String curIdentifier : ids2SumUp) {
            sum += getTrustValue(curIdentifier);
        }
        return sum;
    }

    /**
	 * Get the <code>topX</code> most trusted identifiers in identifierStrings.
	 */
    private Collection<String> getTopXOf(Vector<String> identifierStrings, int topX) {
        if (identifierStrings.size() == 0) return identifierStrings;
        LinkedList<String> list = new LinkedList<String>();
        Iterator<String> iter = identifierStrings.iterator();
        String curID = (String) iter.next();
        int minValue = getTrustValue(curID);
        int maxValue = minValue;
        int tempValue;
        String searchID;
        int searchIndex;
        list.addFirst(curID);
        while (iter.hasNext()) {
            curID = (String) iter.next();
            tempValue = getTrustValue(curID);
            if (tempValue < minValue) {
                list.addFirst(curID);
                minValue = tempValue;
            } else if (tempValue > maxValue) {
                list.addLast(curID);
                maxValue = tempValue;
            } else {
                searchIndex = 0;
                searchID = (String) list.get(searchIndex);
                while (tempValue > getTrustValue(searchID)) {
                    searchID = (String) list.get(++searchIndex);
                }
                list.add(searchIndex, curID);
            }
            if (list.size() > topX) {
                list.removeFirst();
                minValue = getTrustValue((String) list.getFirst());
            }
        }
        return list;
    }

    /**
	 * Get the stored trust value of <code>identifier</code> or the default trust
	 * value if no data is stored for this identifier.
	 */
    public int getTrustValue(String identifier) {
        int trustValue;
        try {
            trustValue = ((Integer) this.trustedListValues.get(identifier)).intValue();
        } catch (NullPointerException e) {
            trustValue = this.defaultTrust;
        }
        return trustValue;
    }

    /**
	 * Get the time of the last change of <code>identifier</code>'s trust value or
	 * the current time if no time for the last change is stored.
	 */
    protected long getTimeStamp(String identifier) {
        long timeStamp;
        try {
            timeStamp = ((Long) this.trustedListTimestamps.get(identifier)).longValue();
        } catch (NullPointerException e) {
            timeStamp = System.currentTimeMillis();
        }
        return timeStamp;
    }

    /**
	 * Set the trust value for <code>identifier</code>. This method assures to
	 * keep the trust value in the allowed intervall between lowerbound and
	 * upperbound.
	 * 
	 * @return the effectively stored trust value.
	 */
    public int setTrustValue(String identifier, int trust) {
        trust = Math.min(trust, this.upperTrustBound);
        trust = Math.max(trust, this.lowerTrustBound);
        synchronized (this.trustedListValues) {
            this.trustedListValues.put(identifier, new Integer(trust));
            this.trustedListTimestamps.put(identifier, new Long(System.currentTimeMillis()));
        }
        return trust;
    }
}
