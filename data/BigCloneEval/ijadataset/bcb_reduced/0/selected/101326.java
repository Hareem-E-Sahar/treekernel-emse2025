package algorithm.sort;

public class MergeSort {

    public static void main(String[] args) {
        int[] a = { 7, 6, 5, 4, 8, 3, 7, 2, 8, 9 };
        int[] temp = new int[a.length];
        int esq = 0;
        int dir = a.length - 1;
        mergeSort(a, temp, esq, dir);
    }

    public static void mergeSort(int[] x, int[] xTemp, int esq, int dir) {
        if (esq < dir) {
            int medio = (esq + dir) / 2;
            mergeSort(x, xTemp, esq, medio);
            mergeSort(x, xTemp, medio + 1, dir);
            join(x, xTemp, esq, medio + 1, dir);
        }
    }

    public static void join(int[] a, int[] temp, int esq, int dir, int fim) {
        int finIzq = dir - 1;
        int posAux = esq;
        int numElementos = fim - esq + 1;
        while (esq <= finIzq && dir <= fim) if (a[esq] < a[dir]) temp[posAux++] = a[esq++]; else temp[posAux++] = a[dir++];
        while (esq <= finIzq) temp[posAux++] = a[esq++];
        while (dir <= fim) temp[posAux++] = a[dir++];
        for (int i = 0; i < numElementos; i++, fim--) a[fim] = temp[fim];
    }
}
