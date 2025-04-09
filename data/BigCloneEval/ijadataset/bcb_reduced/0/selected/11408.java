package com.sun.kvem.midp.pim;

import java.util.Vector;

/**
 * Class KeySortUtility contains static method "store" only.
 * @see store 
 *
 */
class KeySortUtility {

    /**
     * Support method. Maintains a list, valueList, where each element in
     * the list has a corresponding key (of type Long) stored in keyList.
     * This method inserts "value" into "valueList" with the following
     * postconditions:
     * <ul>
     * <li> keyList.size() == valueList.size()
     * <li> keyList[i] corresponds to valueList[i] (0 <= i < keyList.size())
     * <li> keyList is in ascending order of keys
     * <li> keyList contains key
     * <li> valueList contains value
     * <li> key corresponds to value
     * </ul>
     * The following are preconditions:
     * <ul>
     * <li> keyList.size() == valueList.size()
     * <li> keyList[i] corresponds to valueList[i] (0 <= i < keyList.size())
     * <li> keyList is in ascending order of keys
     * </ul>
     * In order to maintain the arrays correctly, the objects passed as keyList
     * and valueList should <i>only</i> be modified by this method.
     * <p>Preconditions are not verified by this method.
     * @param keyList array or property names
     * @param valueList array of property values
     * @param key identifier for this list
     * @param value target object to store the list
     */
    static void store(Vector keyList, Vector valueList, long key, Object value) {
        int lowerBound = 0;
        int upperBound = keyList.size();
        while (lowerBound != upperBound) {
            int index = lowerBound + (upperBound - lowerBound) / 2;
            long indexKey = ((Long) keyList.elementAt(index)).longValue();
            if (indexKey > key) {
                if (index == upperBound) {
                    upperBound--;
                } else {
                    upperBound = index;
                }
            } else {
                if (index == lowerBound) {
                    lowerBound++;
                } else {
                    lowerBound = index;
                }
            }
        }
        keyList.insertElementAt(new Long(key), lowerBound);
        valueList.insertElementAt(value, lowerBound);
    }
}
