import java.util.Random;
import java.util.Stack;

public class Cars {

    private Stack<Integer> unorderedZone = new Stack();

    private Stack<Integer> sideZone = new Stack();

    public Cars(int n) {
        int[] cars = randomiseCars(n);
        printCars(cars);
        for (int c : cars) unorderedZone.push(c);
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        new Cars(10).sort();
    }

    void sort() {
        int i = 0;
        while (!unorderedZone.empty()) {
            int c = side();
            if (c == i && ++i > 0) {
                forward();
                while (!sideZone.empty()) {
                    back();
                }
            }
        }
        System.out.println();
    }

    private int side() {
        int c = unorderedZone.pop();
        sideZone.push(c);
        return c;
    }

    private void back() {
        int c = sideZone.pop();
        unorderedZone.push(c);
    }

    private void forward() {
        int c = sideZone.pop();
        System.out.print(c + " ");
    }

    void printCars(int[] cars) {
        for (int c : cars) System.out.print(c + " ");
        System.out.println();
    }

    private int[] randomiseCars(int n) {
        int[] cars = new int[n];
        for (int i = 0; i < n; i++) cars[i] = i;
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            int j = i + r.nextInt(n - i);
            int tmp = cars[i];
            cars[i] = cars[j];
            cars[j] = tmp;
        }
        return cars;
    }
}
