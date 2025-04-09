package com.iver.utiles.vectorUtilities;

import java.util.Comparator;
import java.util.Vector;
import com.iver.utiles.MathExtension;

/**
 * New functionality to work with vectors (of elements).
 * 
 * @author Pablo Piqueras Bartolom� (pablo.piqueras@iver.es)
 */
public class VectorUtilities {

    /**
	 * Adds an item in alphabetical order.
	 * 
	 * @param v java.util.Vector in alphabetical order.
	 * @param obj java.lang.Object
	 */
    public static synchronized void addAlphabeticallyOrdered(Vector<Object> v, Object obj) {
        int size = v.size();
        int currentIteration = 0;
        int index, aux_index;
        int lowIndex = 0;
        int highIndex = size - 1;
        int maxNumberOfIterations = (int) MathExtension.log2(size);
        if (size == 0) {
            v.add(obj);
            return;
        }
        while ((lowIndex <= highIndex) && (currentIteration <= maxNumberOfIterations)) {
            index = (lowIndex + highIndex) / 2;
            if (v.get(index).toString().compareTo(obj.toString()) == 0) {
                v.add(index, obj);
                return;
            }
            if (v.get(index).toString().compareTo(obj.toString()) < 0) {
                aux_index = index + 1;
                if ((aux_index) >= size) {
                    v.add(v.size(), obj);
                    return;
                }
                if (v.get(aux_index).toString().compareTo(obj.toString()) > 0) {
                    v.add(aux_index, obj);
                    return;
                }
                lowIndex = aux_index;
            } else {
                if (v.get(index).toString().compareTo(obj.toString()) > 0) {
                    aux_index = index - 1;
                    if ((aux_index) < 0) {
                        v.add(0, obj);
                        return;
                    }
                    if (v.get(aux_index).toString().compareTo(obj.toString()) < 0) {
                        v.add(index, obj);
                        return;
                    }
                    highIndex = aux_index;
                }
            }
            currentIteration++;
        }
    }

    /**
	 * Adds an item in alphabetical order using a comparator for compare the objects. The vector must be alhabetically ordered.
	 *
	 * @param v java.util.Vector in alphabetical order.
	 * @param obj java.lang.Object
	 * @param comp java.util.Comparator
	 */
    public static synchronized void addAlphabeticallyOrdered(Vector<Object> v, Object obj, Comparator<Object> comp) {
        int size = v.size();
        int currentIteration = 0;
        int index, aux_index;
        int lowIndex = 0;
        int highIndex = size - 1;
        int maxNumberOfIterations = (int) MathExtension.log2(size);
        if (size == 0) {
            v.add(obj);
            return;
        }
        while ((lowIndex <= highIndex) && (currentIteration <= maxNumberOfIterations)) {
            index = (lowIndex + highIndex) / 2;
            if (comp.compare(v.get(index), obj) == 0) {
                v.add(index, obj);
                return;
            }
            if (comp.compare(v.get(index), obj) < 0) {
                aux_index = index + 1;
                if ((aux_index) >= size) {
                    v.add(v.size(), obj);
                    return;
                }
                if (comp.compare(v.get(aux_index), obj) > 0) {
                    v.add(aux_index, obj);
                    return;
                }
                lowIndex = aux_index;
            } else {
                if (comp.compare(v.get(index), obj) > 0) {
                    aux_index = index - 1;
                    if ((aux_index) < 0) {
                        v.add(0, obj);
                        return;
                    }
                    if (comp.compare(v.get(aux_index), obj) < 0) {
                        v.add(index, obj);
                        return;
                    }
                    highIndex = aux_index;
                }
            }
            currentIteration++;
        }
    }
}
