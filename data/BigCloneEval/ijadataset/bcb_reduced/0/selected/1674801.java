package padmig.examples;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import padmig.Migratable;
import padmig.MigrationException;
import padmig.Migratory;
import padmig.PadMig;
import padmig.Undock;

public class Fib implements Migratable, Serializable {

    boolean moved = false;

    @Migratory
    public int fib(int n) throws MalformedURLException, MigrationException {
        if (n == 0) {
            if (!moved) {
                PadMig.migrate(new URL("pp://localhost:7100/first"));
                moved = true;
            }
            return 1;
        } else if (n == 1) return 1; else return fib(n - 1) + fib(n - 2);
    }

    @Undock
    public void fibUndock(int n) throws MalformedURLException, MigrationException {
        int r = fib(n);
        System.out.println("fib = " + r);
    }

    public Serializable migratableMain(Serializable[] args) {
        if (args.length != 1) {
            System.out.println("Fib [integer]");
            return null;
        }
        try {
            fibUndock(Integer.parseInt((String) args[0]));
            System.out.println("Good bye!");
        } catch (NumberFormatException e) {
            System.out.println("args[0] is not an integer");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("migration failed");
            e.printStackTrace();
        }
        return null;
    }
}
