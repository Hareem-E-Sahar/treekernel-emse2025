package org.gvsig.gui.beans.comboboxconfigurablelookup.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.gvsig.gui.beans.comboboxconfigurablelookup.ILookUp;
import org.gvsig.gui.beans.comboboxconfigurablelookup.StringComparator;

/**
 * <p>Agent that looks up items of an locale-rules alphabetically sort ordered <code>Vector</code> that
 *  start with a text. Those items will be returned as a {@link List List}.</p>
 * 
 * <p>Supports two versions: with or without considering case sensitive.</p>
 * 
 * @version 07/02/2008
 * @author Pablo Piqueras Bartolom� (pablo.piqueras@iver.es) 
 */
public class StartsWithLookUpAgent implements ILookUp {

    /**
	 * <p>Creates a new instance of the class <code>StartsWithLookUpAgent</code>.</p>
	 */
    public StartsWithLookUpAgent() {
    }

    public synchronized List<Object> doLookUpConsideringCaseSensitive(String text, Vector<Object> sortOrderedItems, StringComparator comp) {
        if (text == null) return null;
        List<Object> results_list = doLookUpIgnoringCaseSensitive(text, sortOrderedItems, comp);
        if (results_list == null) return null;
        List<Object> results = new ArrayList<Object>();
        for (int i = 0; i < (results_list.size()); i++) {
            if (results_list.get(i).toString().startsWith(text)) {
                results.add(results_list.get(i));
            }
        }
        return results;
    }

    public synchronized List<Object> doLookUpIgnoringCaseSensitive(String text, Vector<Object> sortOrderedItems, StringComparator comp) {
        if (text == null) return null;
        int currentIteration = 0;
        int size = sortOrderedItems.size();
        int maxNumberOfIterations = (int) (Math.log(size) / Math.log(2));
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
