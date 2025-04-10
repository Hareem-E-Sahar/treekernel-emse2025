public class Opdracht3_5 {

    public Opdracht3_5() {
        System.out.println("Opdracht 3.5");
        double ans = halveringsmethode();
        System.out.println("Antwoord = " + ans);
    }

    public static double halveringsmethode() {
        double onderGrens = Main.vraagDouble("Wat is de onderGrens?\n (double) (1)");
        double bovenGrens = Main.vraagDouble("Wat is de bovenGrens?\n (double) (2)");
        int welkeBerekening = Main.vraagInt("Welke Berekening?\n-1) x-1.2\n1) Math.pow(x, 5) - (3 * Math.pow(x, 2)) - 2 = 0\n2) Math.pow(Math.E,x) - (3*x) = 0\n");
        double ans = 0;
        double x = (onderGrens + bovenGrens) / 2;
        double aanpassing = 0.0001;
        int keer = 0;
        while ((bovenGrens - onderGrens) >= aanpassing) {
            System.out.println("---------------------------------");
            keer += 1;
            x = (onderGrens + bovenGrens) / 2;
            ans = berekening(x, welkeBerekening);
            System.out.println("Antwoord van de Berekening: " + ans);
            if (ans == 0) {
                onderGrens = x;
                bovenGrens = x;
                return x;
            }
            if (ans < 0) {
                onderGrens = x;
                System.out.println("onderGrens Omhoog");
            } else {
                bovenGrens = x;
                System.out.println("bovenGrens Omlaag");
            }
        }
        System.out.println("x: " + x);
        System.out.println("Aantal Keer:" + keer);
        return x;
    }

    public static double berekening(double x, int welke) {
        if (welke == 1) {
            System.out.println("Math.pow(x, 5) - (3 * Math.pow(x, 2)) - 2;");
            return Math.pow(x, 5) - (3 * Math.pow(x, 2)) - 2;
        }
        if (welke == 2) {
            System.out.println("Math.pow(Math.E, x) - (3*x);");
            return Math.pow(Math.E, x) - (3 * x);
        }
        if (welke == -1) {
            System.out.println("x-1.2");
            return x - 1.2;
        }
        return 0.0;
    }

    public static void main(String[] args) {
        new Opdracht3_5();
    }
}
