package xjava;

/**
 *
 * @author torandi
 */
public class SearchSort {

    public static void Sortera(int data[]) {
        for (int i = 1; i < data.length; i++) {
            int pos = i;
            int tmp = data[i];
            while (pos > 0 && data[pos - 1] > tmp) {
                data[pos] = data[pos - 1];
                pos--;
            }
            data[pos] = tmp;
        }
    }

    public static int binSearch(int data[], int find) {
        int min = 0;
        int max = data.length - 1;
        int pos = -1;
        while (min <= max && pos == -1) {
            int mitt = (min + max) / 2;
            if (find > data[mitt]) {
                min = mitt + 1;
            } else if (find < data[mitt]) {
                max = mitt - 1;
            } else {
                pos = mitt;
            }
        }
        return pos;
    }

    public static int linSearch(int data[], int find) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == find) {
                return i;
            }
        }
        return -1;
    }

    public static int binSS(int data[], int find) {
        Sortera(data);
        return binSearch(data, find);
    }
}
