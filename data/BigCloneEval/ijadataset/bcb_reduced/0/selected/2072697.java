package persistencia;

/**
 * Implementa el método Quick Sort para ordenar un archivo
 * representado por un DirectAccessFile. Seguimos el patrón Strategy.
 * 
 * @author Ing. Valerio Frittelli.
 * @version Abril de 2010.
 */
public class QuickSort implements DirectAccessFileSorter {

    private DirectAccessFile rf;

    /**
     * Crea un ordenador para un DirectAccessFile, asociado al algoritmo
     * QuickSort. 
     */
    public QuickSort(DirectAccessFile rf) {
        this.rf = rf;
    }

    /**
     * Implementación del método QuickSort para ordenar un
     * archivo representado por un DirectAccessFile. El método no hace nada
     * si no hay un archivo asociado al ordenador, o si ese archivo no tiene
     * una clase base asociada.
     */
    public void sort() {
        if (rf == null || rf.getBaseClassName() == null || rf.getMode().equals("r")) return;
        long n = rf.count();
        quick(0, n - 1);
    }

    private void quick(long izq, long der) {
        long i = izq, j = der;
        long c = (izq + der) / 2;
        Grabable rc = rf.get(c);
        do {
            Grabable ri = rf.get(i);
            while (ri.compareTo(rc) < 0 && i < der) {
                i++;
                ri = rf.get(i);
            }
            Grabable rj = rf.get(j);
            while (rc.compareTo(rj) < 0 && j > izq) {
                j--;
                rj = rf.get(j);
            }
            if (i <= j) {
                rf.set(j, ri);
                rf.set(i, rj);
                i++;
                j--;
            }
        } while (i <= j);
        if (izq < j) quick(izq, j);
        if (i < der) quick(i, der);
    }
}
