import java.io.InputStreamReader;
import java.io.BufferedReader;

class Test {

    BufferedReader bufin;

    Test2 test2;

    public static void main(String[] args) {
        Test t = new Test();
        t.startLoop();
    }

    public Test() {
        bufin = new BufferedReader(new InputStreamReader(System.in));
        test2 = new Test2();
    }

    private void startLoop() {
        int state = 0;
        int numA = 1;
        int numB = 1;
        while (true) {
            switch(state) {
                case 0:
                    System.out.println("Please type first number:");
                    try {
                        numA = getNextNumber();
                        state = 1;
                    } catch (NumberFormatException nfe) {
                        System.out.println("That is not a valid integer!");
                    }
                    break;
                case 1:
                    System.out.println("Please type second number:");
                    try {
                        numB = getNextNumber();
                        state = 0;
                        int gcd = getGCD(numA, numB);
                        test2.x = gcd;
                        System.out.println("GCD is " + test2.x);
                    } catch (NumberFormatException nfe) {
                        System.out.println("That is not a valid integer!");
                    }
                    break;
            }
        }
    }

    private int getNextNumber() {
        String instr = "";
        try {
            instr = bufin.readLine();
        } catch (java.io.IOException e) {
            System.out.println("IO error!");
            System.exit(1);
        }
        if (instr == null) System.exit(0);
        Integer theint;
        theint = new Integer(instr);
        return theint.intValue();
    }

    private int getGCD(int a, int b) {
        while (b != 0) {
            System.out.println("a=" + a + "  b=" + b);
            int temp = b;
            b = a % b;
            a = temp;
        }
        System.out.println("a=" + a + "  b=" + b);
        return a;
    }
}
