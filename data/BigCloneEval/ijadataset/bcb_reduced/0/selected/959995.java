package com.iver.utiles.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import com.iver.utiles.MathExtension;

/**
 * This class has static methods that return items that their beginning text value matches with a text pattern. <br>
 * It's necessary that items of the parameter array (Vector) were sort ordered according to their text value. <br>
 * Supports Vectors with and without repeated items.
 * 
 * There are four methods, that are a modification of a binary search algorithm of search, getting a rank of items:
 * <ul>
 * <li>Considering case sensitive in the search.</li>
 * <li>Ignoring case sensitive in the search.</li>
 * <li>Considering case sensitive in the search and an object which implements the Comparable interface</li>
 * <li>Ignoring case sensitive in the search, but yes an objecth which implements the Comparable interface</li>
 * </ul>
 * 
 * @author Pablo Piqueras Bartolom� (pablo.piqueras@iver.es)
 */
public class BinarySearchUsingFirstCharacters {

    /**
	 * This method should be used when is wanted to distinguish small letters from capital letters during the search.
	 *   
	 * It's necessary that all items of the array implement the {@link Comparable} interface.<br>
	 * And it's also necessary that the value returned by the <i>toString()</i> method of each item (supposing 
	 *   they inherit from Object) would be the expected value user saw (that would be used to compare the items).
	 *   
	 * @param text java.lang.String
	 * @param sortOrderedItems java.util.Vector
	 */
    public static synchronized List<Object> doSearchConsideringCaseSensitive(String text, Vector<Object> sortOrderedItems) {
        int currentIteration = 0;
        int size = sortOrderedItems.size();
        int maxNumberOfIterations = (int) MathExtension.log2(size);
        int lowIndex = 0;
        int highIndex = sortOrderedItems.size() - 1;
        int mIndx;
        while ((lowIndex <= highIndex) && (currentIteration <= maxNumberOfIterations)) {
            mIndx = (lowIndex + highIndex) / 2;
            if (sortOrderedItems.get(mIndx).toString().startsWith(text)) {
                lowIndex = highIndex = mIndx;
                highIndex++;
                while ((highIndex < size) && (sortOrderedItems.get(highIndex).toString().startsWith(text))) {
                    highIndex++;
                }
                while (((lowIndex - 1) > -1) && (sortOrderedItems.get((lowIndex - 1)).toString().startsWith(text))) {
                    lowIndex--;
                }
                List<Object> list = new Vector<Object>(sortOrderedItems.subList(lowIndex, highIndex));
                lowIndex--;
                while ((lowIndex > -1) && (sortOrderedItems.get((lowIndex)).toString().toLowerCase().startsWith(text.toLowerCase()))) {
                    if (sortOrderedItems.get(lowIndex).toString().startsWith(text)) {
                        list.add(0, sortOrderedItems.get(lowIndex));
                    }
                    lowIndex--;
                }
                while ((highIndex < size) && (sortOrderedItems.get(highIndex).toString().toLowerCase().startsWith(text.toLowerCase()))) {
                    if (sortOrderedItems.get(highIndex).toString().startsWith(text)) {
                        list.add(list.size(), sortOrderedItems.get(highIndex));
                    }
                    highIndex++;
                }
                return list;
            } else {
                if (sortOrderedItems.get(mIndx).toString().compareTo(text) > 0) {
                    highIndex = mIndx - 1;
                } else {
                    lowIndex = mIndx + 1;
                }
            }
            currentIteration++;
        }
        return null;
    }

    /**
	 * This method should be used when is wanted not to distinguish small letters from capital letters during the search.
	 *   
	 * It's necessary that all items of the array implement the {@link Comparable} interface.<br>
	 * And it's also necessary that the value returned by the <i>toString()</i> method of each item (supposing 
	 *   they inherit from Object) would be the expected value user saw (that would be used to compare the items).
	 *
	 * In this particular situation, it's supposed that the vector is sort ordered according the default algorithm of Java; this has the problem that
	 *   it doesn't consideres the special characters and the orthographic rules of languages that aren't English, and then, for a particular
	 *   <i>text</i> search, an incorrect result could be obtained. The solution decided for this problem has been to modify the algorithm, for seach
	 *   into two ranks.
	 *
	 * @param text java.lang.String
	 * @param sortOrderedItems java.util.Vector
	 */
    public static synchronized List<Object> doSearchIgnoringCaseSensitive(String text, Vector<Object> sortOrderedItems) {
        int currentIteration = 0;
        int size = sortOrderedItems.size();
        int maxNumberOfIterations = (int) MathExtension.log2(size);
        int lowIndex = 0;
        int highIndex = sortOrderedItems.size() - 1;
        int mIndx;
        List<Object> list = null;
        List<Object> list2 = null;
        while ((lowIndex <= highIndex) && (currentIteration <= maxNumberOfIterations)) {
            mIndx = (lowIndex + highIndex) / 2;
            if (sortOrderedItems.get(mIndx).toString().toLowerCase().startsWith(text.toLowerCase())) {
                lowIndex = highIndex = mIndx;
                highIndex++;
                while ((highIndex < size) && (sortOrderedItems.get(highIndex).toString().toLowerCase().startsWith(text.toLowerCase()))) {
                    highIndex++;
                }
                while (((lowIndex - 1) > -1) && (sortOrderedItems.get((lowIndex - 1)).toString().toLowerCase().startsWith(text.toLowerCase()))) {
                    lowIndex--;
                }
                list = Arrays.asList((sortOrderedItems.subList(lowIndex, highIndex)).toArray());
                break;
            } else {
                if (sortOrderedItems.get(mIndx).toString().compareTo(text) > 0) {
                    highIndex = mIndx - 1;
                } else {
                    lowIndex = mIndx + 1;
                }
            }
            currentIteration++;
        }
        currentIteration = 0;
        lowIndex = 0;
        highIndex = sortOrderedItems.size() - 1;
        while ((lowIndex <= highIndex) && (currentIteration <= maxNumberOfIterations)) {
            mIndx = (lowIndex + highIndex) / 2;
            if (sortOrderedItems.get(mIndx).toString().toLowerCase().startsWith(text.toLowerCase())) {
                lowIndex = highIndex = mIndx;
                highIndex++;
                while ((highIndex < size) && (sortOrderedItems.get(highIndex).toString().toLowerCase().startsWith(text.toLowerCase()))) {
                    highIndex++;
                }
                while (((lowIndex - 1) > -1) && (sortOrderedItems.get((lowIndex - 1)).toString().toLowerCase().startsWith(text.toLowerCase()))) {
                    lowIndex--;
                }
                list2 = Arrays.asList((sortOrderedItems.subList(lowIndex, highIndex)).toArray());
                if (list == null) return list2; else {
                    if (list2 == null) return null;
                    Object obj;
                    int j;
                    list = new ArrayList<Object>(list.subList(0, list.size()));
                    for (int i = 0; i < list2.size(); i++) {
                        obj = list2.get(i);
                        if (!list.contains(obj)) {
                            for (j = 0; j < list.size(); j++) {
                                if (list.get(j).toString().compareTo(obj.toString()) > 0) break;
                            }
                            list.add(j, obj);
                        }
                    }
                    size = list.size();
                    if (size == 0) {
                        j = 0;
                    } else {
                        j = sortOrderedItems.indexOf(list.get(size - 1));
                    }
                    j++;
                    if (j < sortOrderedItems.size()) {
                        do {
                            obj = sortOrderedItems.get(j);
                            if (obj.toString().toLowerCase().startsWith(text.toLowerCase())) {
                                list.add(size, obj);
                            }
                            j++;
                        } while (j < sortOrderedItems.size());
                    }
                    return list;
                }
            } else {
                if (sortOrderedItems.get(mIndx).toString().toLowerCase().compareTo(text.toLowerCase()) > 0) {
                    highIndex = mIndx - 1;
                } else {
                    lowIndex = mIndx + 1;
                }
            }
            currentIteration++;
        }
        return null;
    }

    /**
	 * This method should be used when is wanted distinguish small letters from capital letters during the search, and the comparation of items
	 *   done according an algorithm we define.
	 *   
	 * And it's also necessary that the value returned by the <i>toString()</i> method of each item (supposing 
	 *   they inherit from Object) would be the expected value user saw (that would be used to compare the items).
	 *   
	 * @param text java.lang.String
	 * @param sortOrderedItems java.util.Vector
	 * @param comp An Comparator object which implements the <i><b>compareTo()</b><i>  method.
	 */
    public static synchronized List<Object> doSearchConsideringCaseSensitive(String text, Vector<Object> sortOrderedItems, Comparator<Object> comp) {
        List<Object> results_list = doSearchIgnoringCaseSensitive(text, sortOrderedItems, comp);
        if (results_list == null) return null;
        List<Object> results = new ArrayList<Object>();
        for (int i = 0; i < (results_list.size()); i++) {
            if (results_list.get(i).toString().startsWith(text)) {
                results.add(results_list.get(i));
            }
        }
        return results;
    }

    /**
	 * This method should be used when is wanted not to distinguish small letters from capital letters during the search, and the comparation of items
	 *   done according an algorithm we define.
	 *   
	 * And it's also necessary that the value returned by the <i>toString()</i> method of each item (supposing 
	 *   they inherit from Object) would be the expected value user saw (that would be used to compare the items).
	 *
	 * @param text java.lang.String
	 * @param sortOrderedItems java.util.Vector
	 * @param comp An Comparator object which implements the <i><b>compareTo()</b><i>  method.
	 */
    public static synchronized List<Object> doSearchIgnoringCaseSensitive(String text, Vector<Object> sortOrderedItems, Comparator<Object> comp) {
        int currentIteration = 0;
        int size = sortOrderedItems.size();
        int maxNumberOfIterations = (int) MathExtension.log2(size);
        int lowIndex = 0;
        int highIndex = sortOrderedItems.size() - 1;
        int mIndx;
        while ((lowIndex <= highIndex) && (currentIteration <= maxNumberOfIterations)) {
            mIndx = (lowIndex + highIndex) / 2;
            if (sortOrderedItems.get(mIndx).toString().toLowerCase().startsWith(text.toLowerCase())) {
                lowIndex = highIndex = mIndx;
                highIndex++;
                while ((highIndex < size) && (sortOrderedItems.get(highIndex).toString().toLowerCase().startsWith(text.toLowerCase()))) {
                    highIndex++;
                }
                while (((lowIndex - 1) > -1) && (sortOrderedItems.get((lowIndex - 1)).toString().toLowerCase().startsWith(text.toLowerCase()))) {
                    lowIndex--;
                }
                return Arrays.asList((sortOrderedItems.subList(lowIndex, highIndex)).toArray());
            } else {
                if (comp.compare(sortOrderedItems.get(mIndx).toString().toLowerCase(), text.toLowerCase()) > 0) {
                    highIndex = mIndx - 1;
                } else {
                    lowIndex = mIndx + 1;
                }
            }
            currentIteration++;
        }
        return null;
    }
}
