public class Application {

    public static void main() {
        int[] array = new int[] { 2, 4, 7, 9, 13, 15, 17 };
        int value = 15;
        if (task(array, value)) {
            System.out.println("True");
        } else {
            System.out.println("False");
        }
    }

    public static boolean task(int[] array, int value) {
        return startTask(array, value, 0, array.length - 1);
    }

    public static boolean startTask(int[] array, int value, int v1, int v2) {
        int aux = (v1 + v2) / 2;
        if (array[aux] == value) {
            return true;
        } else if (aux == v1) {
            return false;
        }
        if (array[aux] > value) {
            return startTask(array, value, v1, aux);
        } else {
            return startTask(array, value, aux, v2);
        }
    }
}
