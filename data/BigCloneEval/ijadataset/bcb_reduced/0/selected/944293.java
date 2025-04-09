package edu.stanford.genetics.treeview.plugin.karyoview;

import edu.stanford.genetics.treeview.LogBuffer;

/**
* represents the loci in just one chromosome...
*/
abstract class Chromosome {

    public static final int LINEAR = 1;

    public static final int CIRCULAR = 1;

    public abstract ChromosomeLocus getLeftEnd();

    public abstract ChromosomeLocus getRightEnd();

    public boolean isEmpty() {
        if (getLeftEnd() == getRightEnd() && getRightEnd() == null) {
            return true;
        } else {
            return false;
        }
    }

    public abstract int getType();

    public abstract double getMaxPosition();

    public abstract double getMaxPosition(int arm);

    public abstract ChromosomeLocus getClosestLocus(int arm, double position);

    public abstract ChromosomeLocus getLocus(int arm, int index);

    public abstract void insertLocus(ChromosomeLocus locus);

    /**
  * this internal routine is used to insert a locus into an array, maintaining the property that a
  * locus with minimal position is at index 0, and that there is a non-decreasing position as the
  * indexes increase. The array may include null values.
  *
  * @return the index inserted into or -1 on failure to insert.
  */
    protected int insertLocusIntoArray(ChromosomeLocus[] array, ChromosomeLocus locus) {
        for (int point = 0; point < array.length; point++) {
            if (array[point] == null) {
                array[point] = locus;
                return point;
            }
            if (array[point].getPosition() > locus.getPosition()) {
                for (int j = array.length - 1; j > point; j--) {
                    array[j] = array[j - 1];
                }
                array[point] = locus;
                return point;
            }
        }
        System.out.println(" array " + array);
        LogBuffer.println("Error in Genome.insertLocusIntoArray(): we weren't about to fit locus " + locus + " into data structure on account of not allocating enough space");
        return -1;
    }

    /**
  * just bisect and recurse. Bottoms out when min == max....
  */
    protected ChromosomeLocus getLocusRecursive(double position, ChromosomeLocus[] array, int min, int max) {
        if (min == max) {
            return array[min];
        }
        int midL = (max + min) / 2;
        int midR = midL + 1;
        if (array[midL].getPosition() > position) {
            return getLocusRecursive(position, array, min, midL);
        }
        if (array[midR].getPosition() < position) {
            return getLocusRecursive(position, array, midR, max);
        }
        double distL = Math.abs(array[midL].getPosition() - position);
        double distR = Math.abs(array[midR].getPosition() - position);
        if (distL > distR) {
            return array[midR];
        } else {
            return array[midL];
        }
    }
}
