package uebung09.ml.aufgabe04;

import java.util.Vector;

public class VectorTest {

    private Vector<Integer> vec;

    public VectorTest() {
        vec = new Vector<Integer>();
    }

    public void clear() {
        vec = new Vector<Integer>();
    }

    public void generateTree(int nodes) {
        int e;
        for (int i = 0; i < nodes; i++) {
            e = (int) (Math.random() * Integer.MAX_VALUE);
            if (vec.size() == 0) vec.add(e); else add(0, vec.size() - 1, e);
        }
    }

    public void add(int lower, int upper, int content) {
        int index = (lower + upper) / 2;
        if (content > vec.get(index)) {
            if (index + 1 >= vec.size() || vec.get(index + 1) > content) {
                vec.add(index + 1, content);
            } else {
                add(index + 1, upper, content);
            }
        } else {
            if (index == 0 || (vec.get(index - 1) < content)) {
                vec.add(index, content);
            } else {
                add(lower, index - 1, content);
            }
        }
    }

    public void print() {
        System.out.print("{ ");
        for (int i = 0; i < vec.size(); i++) {
            System.out.print(vec.get(i));
            if (i != vec.size() - 1) System.out.print(", ");
        }
        System.out.println("}");
    }

    public static void main(String[] args) {
        System.out.println("VECTOR based TEST");
        System.out.println("Please be patient, the following operations may take some time...");
        final int TESTRUNS = 100;
        final int BEGINSIZE = 10000;
        final int VARYSIZE = 10;
        VectorTest vt = new VectorTest();
        double avgTime = 0;
        long startTime;
        for (int i = 0; i < TESTRUNS; i++) {
            vt.clear();
            startTime = System.currentTimeMillis();
            vt.generateTree(BEGINSIZE + i * VARYSIZE);
            avgTime = ((avgTime * i) + (System.currentTimeMillis() - startTime)) / (i + 1);
        }
        System.out.println("Test successful, result is as follows:");
        System.out.println("Average time for generation is: " + avgTime + "ms");
    }
}
