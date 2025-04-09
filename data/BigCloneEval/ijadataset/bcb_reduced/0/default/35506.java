import jeliot.io.*;

public class BinSearch {

    public static void main() {
        int[] taulu = new int[7];
        taulu[0] = 2;
        taulu[1] = 4;
        taulu[2] = 7;
        taulu[3] = 9;
        taulu[4] = 13;
        taulu[5] = 15;
        taulu[6] = 17;
        int etsi = 15;
        if (etsi(taulu, etsi)) {
            Output.println("L�ytyi");
        } else {
            Output.println("Ei l�ytynyt");
        }
    }

    public static boolean etsi(int[] taulu, int etsi) {
        return etsiBin(taulu, etsi, 0, taulu.length - 1);
    }

    public static boolean etsiBin(int[] taulu, int etsi, int alku, int loppu) {
        int keskus = (alku + loppu) / 2;
        if (taulu[keskus] == etsi) {
            return true;
        } else if (keskus == alku && keskus == loppu) {
            return false;
        }
        if (taulu[keskus] > etsi) {
            return etsiBin(taulu, etsi, alku, keskus);
        } else {
            return etsiBin(taulu, etsi, keskus, loppu);
        }
    }
}
