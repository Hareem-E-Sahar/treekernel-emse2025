package padmig.examples;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import padmig.DontMigrate;
import padmig.Migratable;
import padmig.MigrationException;
import padmig.Migratory;
import padmig.PadMig;
import padmig.Undock;

public class Tests implements Migratable, Serializable {

    public Serializable migratableMain(Serializable[] args) {
        try {
            int x = fSyncUndock(23);
            System.out.println("result = " + x);
            fAsyncUndock();
            System.out.println("bye bye at the origin");
        } catch (Exception e) {
            System.out.println("migration failed");
            e.printStackTrace();
        }
        return null;
    }

    @Undock
    public void fAsyncUndock() throws MalformedURLException, MigrationException {
        @DontMigrate @SuppressWarnings("unused") double someUnnecessaryLocalVariable = 1;
        @DontMigrate @SuppressWarnings("unused") Object someNotSerializableLocalVariable;
        {
            @SuppressWarnings("unused") int i = 0;
            @SuppressWarnings("unused") int[] array = new int[] { 1, 2, 3 };
            @DontMigrate @SuppressWarnings("unused") List<Map<Integer, ?>> l = new LinkedList<Map<Integer, ?>>();
        }
        {
            @SuppressWarnings("unused") Integer i = 0;
            @SuppressWarnings("unused") int[][] array = new int[][] { { 1, 2, 3 }, { 5, 6, 7 }, { 7, 8, 9 } };
            @DontMigrate List<Float> l = new LinkedList<Float>();
            l.add(5.0f);
        }
        PadMig.migrate(new URL("pp://localhost:7100/first"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
        }
        System.out.println("bye bye at the remote side");
    }

    @Undock
    public int fSyncUndock(int n) throws MalformedURLException, MigrationException {
        int r = f(4);
        System.out.println("4th fibonacci number: " + r);
        int s = g(g(n)) + g(g(g(n)));
        System.out.println("s = " + s);
        double t = h(g(n)) + h((int) ((long) h(g(n))));
        System.out.println("t = " + t);
        label: switch(f(1)) {
            case 1:
                System.out.println("falling through into default case");
            default:
                {
                    break label;
                }
            case 2:
                System.out.println("destination is " + getDest());
        }
        return r;
    }

    @Migratory
    public int f(int n) throws MalformedURLException, MigrationException {
        if (n == 0) {
            PadMig.migrate(getDest());
            return 1;
        } else if (n == 1) {
            return 1;
        } else {
            return f(n - 1) + f(n - 2);
        }
    }

    public Fib getFib() {
        return new Fib();
    }

    public int s(int r) {
        return r;
    }

    @Migratory
    @SuppressWarnings("unused")
    public int g(int n) throws MalformedURLException, MigrationException {
        lx: {
            System.out.println("inside lx-labelled block");
            {
                System.out.println("inside unlabelled block");
                URL dest = null;
                if (n == (int) 0L) dest = new URL("pp://localhost:7100/first"); else ly: for (int i = 0; i < 3; i++) {
                    if (i < 1) {
                        lz: while (dest == null) {
                            dest = getDest();
                            if (i == 15) {
                                System.out.println("before continue");
                                continue ly;
                            }
                            System.out.println("last statement of while-loop");
                        }
                    }
                    lz: {
                        if (i > 5) {
                            System.out.println("before break");
                            break lz;
                        }
                        System.out.println("last statement of lz-labelled block");
                    }
                    PadMig.migrate(dest);
                    System.out.println("last statement of for-loop");
                }
            }
            System.out.println("after unlabelled block");
        }
        System.out.println("after lx-labelled block");
        return 42 * n;
    }

    @Migratory
    public double h(int n) throws MigrationException {
        l1: try {
            System.out.println("inside outer try");
            l2: try {
                System.out.println("inside inner try");
                n = fSyncUndock(n);
                if (n == 1) {
                    System.out.println("breaking the outer try");
                    break l1;
                } else if (n == 2) {
                    System.out.println("breaking the inner try");
                    break l2;
                }
                System.out.println("no break so far");
            } catch (Exception e) {
                System.out.println("inner error" + e.getMessage());
            } finally {
                System.out.println("done with inner try");
            }
            System.out.println("after inner try");
        } catch (RuntimeException e) {
            System.out.println("outer error" + e.getMessage());
            throw e;
        } finally {
            System.out.println("done with outer try");
        }
        System.out.println("after outer try");
        return 0.1 * n;
    }

    @Migratory
    public void i() throws MalformedURLException, MigrationException {
        PadMig.migrate(new URL("pp://localhost:7100/first"));
    }

    @Migratory
    public void i(String p1, int p2, Integer p3) throws MalformedURLException, MigrationException {
        PadMig.migrate(new URL("pp://localhost:7100/first"));
    }

    @Migratory
    public URL getDest() throws MalformedURLException, MigrationException {
        for (int i = (int) 0.1, j = 23; i < 3 && j != 0; i++, j--) {
            System.out.println("hi, no. " + i);
        }
        lxx: for (int x = 42; x % 2 == 0; x--) {
            while (x > 23) {
                if (x % 2 == 1) {
                    continue lxx;
                }
            }
        }
        for (; ; ) {
            System.err.println("hello");
            break;
        }
        lyy: try {
            f(1);
        } catch (Exception e) {
            System.err.println("outer exception handling");
        } finally {
            System.out.println("hicks");
        }
        @DontMigrate List<Integer> l = new LinkedList<Integer>();
        for (Integer i : l) {
            System.out.println(i);
        }
        int[] array = new int[] { 1, 2, 3 };
        for (int i : array) {
            System.out.println(i);
        }
        while (--array[1] > 0) {
            i();
            System.out.println("hi again");
        }
        while (array[0]-- > 0) ;
        do {
            i();
            System.out.println("hi again and again");
        } while (--array[2] > 0);
        return new URL("pp://localhost:7200/second");
    }
}
