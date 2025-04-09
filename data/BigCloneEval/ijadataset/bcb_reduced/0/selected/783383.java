package org.ai.pattern;

/**
 *
 * @author yannart
 */
public class UmbralFinder {

    public static final int ISODATA_ALGORITHM = 1;

    public UmbralFinder() {
    }

    public static int getUmbral(long[] valores, int algoritmo) {
        switch(algoritmo) {
            case ISODATA_ALGORITHM:
                return findIsodataUmbral(127, valores, 0, valores.length);
        }
        return -1;
    }

    private static int findIsodataUmbral(int umbral, long[] valores, int inicio, int fin) {
        if (inicio == fin) return inicio;
        int promedio1 = getPromedios(inicio, umbral, valores);
        int promedio2 = getPromedios(umbral, fin, valores);
        if (promedio1 < 0 || promedio2 < 0) {
            if (promedio1 < 0) inicio = umbral;
            if (promedio2 < 0) fin = umbral;
            umbral = (inicio + fin) / 2;
            return findIsodataUmbral(umbral, valores, inicio, fin);
        } else {
            int newumbral = (promedio1 + promedio2) / 2;
            if (newumbral == umbral) {
                return newumbral;
            } else {
                return findIsodataUmbral(newumbral, valores, inicio, fin);
            }
        }
    }

    private static int getPromedios(int inicio, int fin, long[] valores) {
        long suma = 0;
        long numpixeles = 0;
        long backsuma;
        for (int i = inicio; i < fin; i++) {
            if (suma < 0) System.out.println("ERROR");
            backsuma = suma;
            suma += valores[i] * i;
            numpixeles += valores[i];
            if (backsuma > suma) {
                System.out.println("ERROR");
                System.out.println("Valor =" + valores[i]);
                System.out.println("i =" + i);
                System.out.println("backsuma =" + backsuma);
                System.out.println("suma =" + suma);
                System.out.println("num =" + valores[i]);
            }
        }
        if (numpixeles != 0) {
            return (int) (suma / numpixeles);
        } else {
            return -1;
        }
    }
}
