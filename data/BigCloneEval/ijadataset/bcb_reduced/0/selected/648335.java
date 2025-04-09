package andere_dingen;

public class MinDouble {

    public MinDouble() {
        double a = 10;
        while (a != 0) {
            System.out.println(a);
            a = a - 0.1;
        }
    }

    public static void main(String[] args) {
        new MinDouble();
    }
}
