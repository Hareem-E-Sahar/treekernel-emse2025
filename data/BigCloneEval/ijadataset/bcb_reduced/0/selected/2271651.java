package com.greentea.relaxation.algorithms.genetic;

import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Random;

public class PermutationGenerator {

    private final int from;

    private final int to;

    private Hashtable<Integer, Integer> memory = new Hashtable<Integer, Integer>();

    private int key;

    Random rand;

    public PermutationGenerator(int from, int to, int key) {
        this.from = from;
        this.to = to;
        this.key = key;
        reset();
    }

    public void reset() {
        rand = new Random(key);
    }

    public void reset(int key) {
        rand = new Random(key);
    }

    public int next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int value = from + rand.nextInt(to - from);
        while (memory.containsKey(value)) {
            ++value;
            if (value == to) {
                value = from;
            }
        }
        memory.put(value, value);
        return value;
    }

    public boolean hasNext() {
        if (memory.size() >= to - from) {
            return false;
        }
        return true;
    }
}
