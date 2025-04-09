package com.hypermine.ultrasonic.components;

import java.util.*;

/**
 * @author wschwitzer
 * @author $Author: wschwitzer $
 * @version $Rev: 147 $
 * @levd.rating GREEN Rev: 139
 */
public class Events implements Iterable<Event> {

    private static final Event[] EMPTY_EVENTS = {};

    private final List<Event> list = new ArrayList<Event>();

    public synchronized void add(Event event) {
        int index = insertIndexOf(event.getPosition());
        list.add(index, event);
    }

    public synchronized void remove(Event event) {
        int index = firstIndexOf(event.getPosition());
        while (index < list.size() && list.get(index).getPosition() == event.getPosition()) {
            if (list.get(index) == event) {
                list.remove(index);
                break;
            }
            index++;
        }
    }

    public synchronized Event[] get(int beginPosition, int endPosition) {
        if (beginPosition >= endPosition) {
            return EMPTY_EVENTS;
        }
        int beginIndex = firstIndexOf(beginPosition);
        int endIndex = beginIndex;
        while (endIndex < list.size() && list.get(endIndex).getPosition() < endPosition) {
            endIndex++;
        }
        if (beginIndex == endIndex) {
            return EMPTY_EVENTS;
        }
        int count = endIndex - beginIndex;
        Event[] result = new Event[count];
        for (int i = 0; i < count; i++) {
            result[i] = list.get(beginIndex + i);
        }
        return result;
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    public synchronized Event getFirst() {
        return list.get(0);
    }

    public synchronized Event getLast() {
        return list.get(list.size() - 1);
    }

    private int insertIndexOf(int position) {
        return firstIndexOf(position + 1);
    }

    private int firstIndexOf(int position) {
        int lowIndex = 0;
        int highIndex = list.size() - 1;
        while (lowIndex <= highIndex) {
            int pivotIndex = (lowIndex + highIndex) / 2;
            Event pivotElement = list.get(pivotIndex);
            int compare = pivotElement.getPosition() - position;
            if (compare < 0) {
                lowIndex = pivotIndex + 1;
            } else {
                highIndex = pivotIndex - 1;
            }
        }
        return lowIndex;
    }

    @Override
    public Iterator<Event> iterator() {
        return list.iterator();
    }
}
