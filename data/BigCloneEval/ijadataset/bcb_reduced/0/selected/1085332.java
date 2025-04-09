package csimage.demo;

import java.awt.Color;
import java.util.Random;

/**
 * @author johnwill To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RevealImage {

    private int[] colorMap;

    private RevealMatrix m;

    public RevealImage(String imagePath) {
        m = new RevealMatrix(500, 500, Color.RED, imagePath);
        m.loadImage(imagePath);
        storeColorIndex();
        randomize();
        m.setVisible(true);
    }

    public void randomize() {
        Random random = new Random();
        int newIndex = 0;
        int numPixels = m.getImageHeight() * m.getImageWidth();
        for (int i = 0; i < numPixels; ++i) {
            newIndex = random.nextInt(numPixels);
            swapColors(i, newIndex, false);
            swapInt(i, newIndex);
        }
    }

    private void storeColorIndex() {
        int size = m.getImageWidth() * m.getImageHeight();
        colorMap = new int[size];
        for (int i = 0; i < size; ++i) {
            colorMap[i] = i;
        }
    }

    public void swapColors(int cell1, int cell2, boolean doPaint) {
        Color temp = m.getColor(cell1);
        m.setColor(cell1, m.getColor(cell2), doPaint);
        m.setColor(cell2, temp, doPaint);
    }

    private void swapInt(int i, int j) {
        int temp = colorMap[i];
        colorMap[i] = colorMap[j];
        colorMap[j] = temp;
    }

    private int getSmallestRGB(int startCell) {
        int numPixels = m.getImageHeight() * m.getImageWidth();
        int indexOfSmallest = startCell;
        for (int i = startCell; i < numPixels; ++i) {
            if (isLessRGB(m.getColor(i), m.getColor(indexOfSmallest))) {
                indexOfSmallest = i;
            }
        }
        return indexOfSmallest;
    }

    private boolean isLessRGB(Color col1, Color col2) {
        if (col1.getRed() < col2.getRed()) return true; else if (col1.getRed() > col2.getRed()) return false; else {
            if (col1.getGreen() < col2.getGreen()) return true; else if (col1.getGreen() > col2.getGreen()) return false; else {
                if (col1.getBlue() < col2.getBlue()) return true; else return false;
            }
        }
    }

    public void selSortByColor() {
        int nextCell = 0;
        int numPixels = m.getImageHeight() * m.getImageWidth();
        for (int i = 0; i < numPixels; ++i) {
            nextCell = getSmallestRGB(i);
            if (nextCell != i) {
                swapColors(nextCell, i, true);
            }
        }
    }

    public void quickSortByColorShell() {
        int numPixels = m.getImageHeight() * m.getImageWidth();
        quickSortByColor(0, numPixels - 1);
    }

    public void quickSortByColor(int lower, int upper) {
        if (lower >= upper) {
            return;
        }
        int mid = partitionColor(lower, upper);
        quickSortByColor(lower, mid - 1);
        quickSortByColor(mid + 1, upper);
    }

    public int partitionColor(int lower, int upper) {
        Color target = m.getColor(lower);
        int mid = lower;
        for (int i = lower + 1; i <= upper; ++i) {
            if (isLessRGB(m.getColor(i), target)) {
                mid++;
                swapColors(mid, i, true);
            }
        }
        swapColors(mid, lower, true);
        return mid;
    }

    public static void delay(int msDelay) {
        long mark = System.currentTimeMillis();
        while (System.currentTimeMillis() < mark + msDelay) {
            ;
        }
    }

    public void recreateImageSelSort() {
        int nextCell = 0;
        for (int i = 0; i < colorMap.length; ++i) {
            nextCell = getSmallest(i);
            if (nextCell != i) {
                swapColors(nextCell, i, true);
                swapInt(nextCell, i);
            }
        }
    }

    private int getSmallest(int startCell) {
        int indexOfSmallest = startCell;
        for (int i = startCell; i < colorMap.length; ++i) {
            if (colorMap[i] < colorMap[indexOfSmallest]) {
                indexOfSmallest = i;
            }
        }
        return indexOfSmallest;
    }

    public void recreateImageInsSort() {
        for (int i = 1; i < colorMap.length; ++i) {
            int temp = colorMap[i];
            Color tempColor = m.getColor(i);
            int ins = i;
            while (ins > 0 && colorMap[ins - 1] >= temp) {
                colorMap[ins] = colorMap[ins - 1];
                m.setColor(ins, m.getColor(ins - 1), true);
                --ins;
            }
            colorMap[ins] = temp;
            m.setColor(ins, tempColor, true);
        }
    }

    public void recreateImageQuickSort() {
        quickSortImage(0, colorMap.length - 1);
    }

    public void quickSortImage(int lower, int upper) {
        if (lower >= upper) {
            return;
        }
        int mid = partition(lower, upper);
        quickSortImage(lower, mid - 1);
        quickSortImage(mid + 1, upper);
    }

    public int partition(int lower, int upper) {
        int target = colorMap[lower];
        int mid = lower;
        for (int i = lower + 1; i <= upper; ++i) {
            if (colorMap[i] < target) {
                mid++;
                swapInt(mid, i);
                swapColors(mid, i, true);
            }
        }
        swapInt(mid, lower);
        swapColors(mid, lower, true);
        return mid;
    }

    public void recreateImageMergeSort() {
        mergeSortImage(0, colorMap.length - 1);
    }

    public void mergeSortImage(int lower, int upper) {
        if (lower >= upper) {
            return;
        }
        int mid = (lower + upper) / 2;
        mergeSortImage(lower, mid);
        mergeSortImage(mid + 1, upper);
        merge(lower, mid, mid + 1, upper);
    }

    public void merge(int st1, int end1, int st2, int end2) {
        assert (end1 + 1 == end2);
        int[] tempColorMap = new int[end2 - st1 + 1];
        Color[] tempColor = new Color[end2 - st1 + 1];
        for (int i = st1; i <= end2; ++i) {
            tempColorMap[i - st1] = colorMap[i];
            tempColor[i - st1] = m.getColor(i);
        }
        int lo = st1;
        int hi = st2;
        int i = lo;
        while (lo <= end1 && hi <= end2) {
            if (tempColorMap[lo - st1] < tempColorMap[hi - st1]) {
                colorMap[i] = tempColorMap[lo - st1];
                m.setColor(i, tempColor[lo - st1], true);
                i++;
                lo++;
            } else {
                colorMap[i] = tempColorMap[hi - st1];
                m.setColor(i, tempColor[hi - st1], true);
                i++;
                hi++;
            }
        }
        while (lo <= end1) {
            colorMap[i] = tempColorMap[lo - st1];
            m.setColor(i, tempColor[lo - st1], true);
            i++;
            lo++;
        }
        while (hi <= end2) {
            colorMap[i] = tempColorMap[hi - st1];
            m.setColor(i, tempColor[hi - st1], true);
            i++;
            hi++;
        }
    }

    public static void main(String[] args) {
        System.out.println(new java.util.Date());
        RevealImage img = new RevealImage("csimage/pictures/junk1.jpg");
        img.recreateImageMergeSort();
        System.out.println(new java.util.Date());
    }
}
