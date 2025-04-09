import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Test {

    /**
	 * Testfall 1<br>
	 * Ein rundes Geschenk in eine Quadratische-Box aus dem Lager packen
	 */
    public boolean Test1(String methodName) {
        BoxStorage storage = new BoxStorage();
        Box<HollowSquareShape> b1 = new Box<HollowSquareShape>(new HollowSquareShape(30.0, 15.0));
        Box<HollowCircleShape> b2 = new Box<HollowCircleShape>(new HollowCircleShape(30.0, 15.0));
        storage.add(b1);
        storage.add(b2);
        Roundabout gift = new Roundabout("Kreisel", new FullCircleShape(30.0, 15.0));
        return this.matchResultsToObject(methodName, b1, gift.verpacke(storage));
    }

    /**
	 * Testfall 2<br>
	 * Im Lager sind keine passenden Schachteln enthalten, es muss eine neue erzeugt werden
	 * um das Geschenk zu verpacken
	 */
    public boolean Test2(String methodName) {
        BoxStorage storage = new BoxStorage();
        Box<HollowRectangleShape> b1 = new Box<HollowRectangleShape>(new HollowRectangleShape(25.0, 66.0, 45.0));
        Box<HollowCircleShape> b2 = new Box<HollowCircleShape>(new HollowCircleShape(15.0, 33.0));
        Box b3;
        storage.add(b1);
        storage.add(b2);
        Laptop gift = new Laptop("Laptop", new FullRectangleShape(32.0, 45.0, 90.0));
        b3 = gift.verpacke(storage);
        return this.matchResultToBoolean(methodName, true, b3 != b1 && b3 != b2);
    }

    /**
	 * Testfall 3
	 * Ein Geschenk in mehrere Kisten verpacken
	 */
    public boolean Test3(String methodName) {
        BoxStorage storage = new BoxStorage();
        Box<HollowCircleShape> b1 = new Box<HollowCircleShape>(new HollowCircleShape(19.0, 54.0));
        Box<HollowCircleShape> b2 = new Box<HollowCircleShape>(new HollowCircleShape(20.0, 55.0));
        Box<HollowCircleShape> b3 = new Box<HollowCircleShape>(new HollowCircleShape(21.0, 56.0));
        storage.add(b1);
        storage.add(b2);
        storage.add(b3);
        Gift gift = new Ball("Ball", new FullCircleShape(19.0, 54.0));
        gift = gift.verpacke(storage);
        gift = gift.verpacke(storage);
        gift = gift.verpacke(storage);
        return this.matchResultsToObject(methodName, b3, gift);
    }

    /**
	 * Testfall 6
	 * Packe Geschenk mehrfach ein, jedoch muss die letzte Verpackung erst erzeugt werden
	 */
    public boolean Test6(String methodName) {
        BoxStorage storage = new BoxStorage();
        Box<HollowCircleShape> b1 = new Box<HollowCircleShape>(new HollowCircleShape(5.0, 5.0));
        Box<HollowSquareShape> b2 = new Box<HollowSquareShape>(new HollowSquareShape(6.0, 6.0));
        Box<HollowCircleShape> b3 = new Box<HollowCircleShape>(new HollowCircleShape(7.0, 7.0));
        Box b4;
        storage.add(b1);
        storage.add(b2);
        storage.add(b3);
        Ball gift = new Ball("Ball", new FullCircleShape(5.0, 5.0));
        b4 = gift.verpacke(storage).verpacke(storage).verpacke(storage);
        return this.matchResultToBoolean(methodName, true, b4 != b1 && b4 != b2 && b4 != b3);
    }

    /**
	 * Testfall 10
	 * Mehrere Geschenke in Schachteln packen und in den Geschenksack geben 
	 * danach volumen abfragen
	 */
    public boolean Test10(String methodName) {
        BoxStorage storage = new BoxStorage();
        GiftBag giftBag = new GiftBag();
        Box<HollowSquareShape> b1 = new Box<HollowSquareShape>(new HollowSquareShape(10.0, 10.0));
        Box<HollowSquareShape> b2 = new Box<HollowSquareShape>(new HollowSquareShape(10.0, 10.0));
        Box<HollowSquareShape> b3 = new Box<HollowSquareShape>(new HollowSquareShape(10.0, 10.0));
        storage.add(b1);
        storage.add(b2);
        storage.add(b3);
        ChessGame gift1 = new ChessGame("Schachspiel1", new FullSquareShape(10.0, 10.0));
        ChessGame gift2 = new ChessGame("Schachspiel2", new FullSquareShape(10.0, 10.0));
        ChessGame gift3 = new ChessGame("Schachspiel3", new FullSquareShape(10.0, 10.0));
        gift1.verpacke(storage);
        gift2.verpacke(storage);
        gift3.verpacke(storage);
        giftBag.addGift(b1, storage);
        giftBag.addGift(b2, storage);
        giftBag.addGift(b3, storage);
        return this.matchResultToDouble(methodName, 3993.0, giftBag.volumen());
    }

    /**
	 * Testfall 11
	 * Mehrere Geschenke in Schachteln packen und in den Geschenksack geben 
	 * danach volumen abfragen
	 */
    public boolean Test11(String methodName) {
        BoxStorage storage = new BoxStorage();
        GiftBag giftBag = new GiftBag();
        Box<HollowSquareShape> b1 = new Box<HollowSquareShape>(new HollowSquareShape(10.0, 10.0));
        Box<HollowSquareShape> b2 = new Box<HollowSquareShape>(new HollowSquareShape(10.0, 10.0));
        Box<HollowSquareShape> b3 = new Box<HollowSquareShape>(new HollowSquareShape(10.0, 10.0));
        storage.add(b1);
        storage.add(b2);
        storage.add(b3);
        ChessGame gift1 = new ChessGame("Schachspiel1", new FullSquareShape(10.0, 10.0));
        ChessGame gift2 = new ChessGame("Schachspiel2", new FullSquareShape(10.0, 10.0));
        ChessGame gift3 = new ChessGame("Schachspiel3", new FullSquareShape(10.0, 10.0));
        gift1.verpacke(storage).verpacke(storage).verpacke(storage);
        gift2.verpacke(storage);
        gift3.verpacke(storage);
        giftBag.addGift(b1, storage);
        giftBag.addGift(b2, storage);
        giftBag.addGift(b3, storage);
        return this.matchResultToString(methodName, "Schachspiel1\n" + "Schachspiel2\n" + "Schachspiel3\n", giftBag.geschenke());
    }

    /**
	 * Testfall 12
	 * HollowSquareShape verwenden wo HollowShape erwartet wird
	 */
    public boolean Test12(String methodName) {
        HollowShape shape = new HollowSquareShape(10.0, 10.0);
        return this.matchResultToDouble(methodName, 11.0 * 11.0 * 11.0, shape.getVolume());
    }

    /**
	 * Testfall 13
	 * HollowRectangleShape verwenden wo HollowShape erwartet wird
	 */
    public boolean Test13(String methodName) {
        HollowShape shape = new HollowRectangleShape(10.0, 12.0, 10.0);
        return this.matchResultToDouble(methodName, 11.0 * 13.0 * 11.0, shape.getVolume());
    }

    /**
	 * Testfall 14
	 * HollowCircleShape verwenden wo HollowShape erwartet wird
	 */
    public boolean Test14(String methodName) {
        HollowShape shape = new HollowCircleShape(10.0, 10.0);
        return this.matchResultToDouble(methodName, ((11.0 * 11.0 * Math.PI) / 4) * 11.0, shape.getVolume());
    }

    /**
	 * Testfall 15
	 * 2 Boxen erstellen und in box1  packing(Gift gift) mit box2 als Inputparameter aufrufen
	 * true wird returned da die box2 in box1 oasst ohne zu verrutschen
	 */
    public boolean Test15(String methodName) {
        HollowShape shape1 = new HollowSquareShape(12.0, 12.0);
        HollowShape shape2 = new HollowSquareShape(11.0, 11.0);
        Box<HollowShape> box1 = new Box<HollowShape>(shape1);
        Box<HollowShape> box2 = new Box<HollowShape>(shape2);
        return this.matchResultToBoolean(methodName, true, box1.packing(box2));
    }

    private boolean matchResultsToObject(String methodName, Object expectedResult, Object returnedResult) {
        if (expectedResult.equals(returnedResult)) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return false;
        }
    }

    /**
	 * Kontrolliert ob diese Funktion aufgerufen wird oder nicht
	 * @param methodName
	 * @param expectedResult true wenn aufruf erwartet, sonst false
	 * @param returnedResult
	 * @return
	 */
    private boolean matchExecution(String methodName, boolean expectedResult) {
        if (expectedResult == true) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED EXECUTION\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("UNEXPECTED EXECUTION\n");
            return false;
        }
    }

    private boolean matchResultToInteger(String methodName, int expectedResult, int returnedResult) {
        if (expectedResult == returnedResult) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return false;
        }
    }

    private boolean matchResultToBoolean(String methodName, boolean expectedResult, boolean returnedResult) {
        if (expectedResult == returnedResult) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return false;
        }
    }

    private boolean matchResultToDouble(String methodName, double expectedResult, double returnedResult) {
        if (expectedResult == returnedResult) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return false;
        }
    }

    /**
	 * 
	 * @param methodName Name des Testfalles
	 * @param expectedResult Erwarteter Wert
	 * @param returnedResult Zurueckgeliefertert Wert
	 * @return wenn expectedResult = returnedResult true; sonst false
	 */
    private boolean matchResultToString(String methodName, String expectedResult, String returnedResult) {
        if (expectedResult.equals(returnedResult)) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return false;
        }
    }

    public void run() {
        Method[] methods = this.getClass().getMethods();
        int countTestCases = 0;
        int countSuccessTestCases = 0;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().length() > 4) {
                if (methods[i].getName().substring(0, 4).equals("Test")) {
                    countTestCases++;
                    try {
                        if ((Boolean) (methods[i].invoke(this, methods[i].getName().substring(4)))) {
                            countSuccessTestCases++;
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("------------------------");
        System.out.println("Testcases: " + countTestCases);
        System.out.println("Success  : " + countSuccessTestCases);
        System.out.println("Failure  : " + (countTestCases - countSuccessTestCases));
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.run();
    }
}
