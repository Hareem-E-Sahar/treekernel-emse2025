import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test {

    /**
	 * Testfall 1
	 * 300GB auf 500GB Festplatte belegen
	 * Es wird true erwartet
	 */
    public boolean Test1(String methodName) {
        HardDisc hardDisc = new HardDisc(50000);
        return this.matchResultToBoolean(methodName, true, hardDisc.use(30000));
    }

    /**
	 * Testfall 10
	 * 300 GB auf 120GB Festplatte belegen
	 * Es wird false erwartet
	 */
    public boolean Test10(String methodName) {
        HardDisc hardDisc = new HardDisc(12000);
        return this.matchResultToBoolean(methodName, false, hardDisc.use(30000));
    }

    /**
	 * Testfall 20
	 * -300 GB auf 500GB Festplatte
	 * Es wird false erwartet
	 */
    public boolean Test20(String methodName) {
        HardDisc hardDisc = new HardDisc(50000);
        return this.matchResultToBoolean(methodName, false, hardDisc.use(-30000));
    }

    /**
	 * Testfall 30
	 * Ausgeben des freien Speichers auf 500GB Festplatte
	 * Es wird 20000 erwartet
	 */
    public boolean Test30(String methodName) {
        HardDisc hardDisc = new HardDisc(50000);
        hardDisc.use(20000);
        hardDisc.use(10000);
        return this.matchResultToInteger(methodName, 20000, hardDisc.available());
    }

    /**
	 * Testfall 40
	 * Ausgeben des freien Speichers auf 500GB Festplatte
	 * nach Beanspruchung von Platz
	 * Es wird 20000 erwartet
	 */
    public boolean Test40(String methodName) {
        HardDisc hardDisc = new HardDisc(50000);
        hardDisc.use(20000);
        hardDisc.use(10000);
        hardDisc.use(30000);
        return this.matchResultToInteger(methodName, 20000, hardDisc.available());
    }

    /**
	 * Testfall 50
	 * Es wird versucht ein Medium im DVDlaufwerk zu beschriebe, obweohl keines eingelegt ist.
	 * --> beschreiben nicht moeglich es wird false zuruerckgeliefert
	 */
    public boolean Test50(String methodName) {
        DiscDrive dd1 = new DVDDrive();
        return this.matchResultToBoolean(methodName, false, dd1.use(40));
    }

    /**
	 * Testfall 51
	 * Es wird versucht ein Medium im DVDlaufwerk laden, das nicht gelesen werden kann.
	 * --> aufruf von load liefert false
	 */
    public boolean Test51(String methodName) {
        Disc disc1 = new BlueRay();
        DiscDrive dd1 = new DVDDrive();
        return this.matchResultToBoolean(methodName, false, dd1.load(disc1));
    }

    /**
	 * Testfall 52
	 * Es wird versucht ein Medium(eine DVD) im zu DVDlaufwerk laden.
	 * --> aufruf von load liefert true, da DVD ein geeignetes speichermedium ist
	 */
    public boolean Test52(String methodName) {
        Disc disc1 = new DVD();
        DiscDrive dd1 = new DVDDrive();
        return this.matchResultToBoolean(methodName, true, dd1.load(disc1));
    }

    /**
	 * Testfall 53
	 * Es wird mittels load(disc1) die disc1 in das DVD Laufwerk gelegt.
	 * mit use(40) wird die disc mit 40 kb bschrieben
	 * aufruf von available() liefertden vorhandenen speicher des mediums zurueck, das sich im laufwerk befindet
	 * 
	 */
    public boolean Test53(String methodName) {
        Disc disc1 = new DVD();
        DiscDrive dd1 = new DVDDrive();
        dd1.load(disc1);
        dd1.use(40);
        return this.matchResultToInteger(methodName, 4699960, dd1.available());
    }

    /**
	 * Testfall 54
	 * Es wird versucht ein Medium(eine CDRW) im BlueRaylaufwerk zu laden und dann mit use(50)zu beschreiben.
	 * --> aufruf von use(50) liefert false, da Blueraylaufwerke cdrw nicht verwenden koennen.
	 */
    public boolean Test54(String methodName) {
        Disc disc1 = new CDRW();
        DiscDrive dd1 = new BlueRayDrive();
        dd1.load(disc1);
        return this.matchResultToBoolean(methodName, false, dd1.use(50));
    }

    /**
	 * Testfall 55
	 * Zuerst wird disc1 im BlueRaylaufwerk mit load() geladen, danach soll im selben laufwerk 
	 * disc 2 geladen werden obwohl die vorherige noch nicht mit unload ausgeworfen wurde.
	 * --> aufruf von load(disc2) liefert false, da im Laufwerk schon eine disc ist.
	 */
    public boolean Test55(String methodName) {
        Disc disc1 = new BlueRay();
        Disc disc2 = new BlueRay();
        DiscDrive dd1 = new BlueRayDrive();
        dd1.load(disc1);
        return this.matchResultToBoolean(methodName, false, dd1.load(disc2));
    }

    /**
	 * Testfall 56
	 * Zuerst wird disc1 im BlueRaylaufwerk mit load() geladen, danach soll im selben laufwerk 
	 * disc 2 geladen werden, aber erst nachdem disc1 mittels unload ausgeweofen wurde
	 * --> aufruf von load(disc2) liefert true, da im Laufwerk bei load(disc2) keine disc geladen ist.
	 */
    public boolean Test56(String methodName) {
        Disc disc1 = new BlueRay();
        Disc disc2 = new BlueRay();
        DiscDrive dd1 = new BlueRayDrive();
        dd1.load(disc1);
        dd1.unload();
        return this.matchResultToBoolean(methodName, true, dd1.load(disc2));
    }

    /**
	 * Testfall 57
	 * Zuerst wird disc1 im DVDlaufwerk mit load() geladen und etwas von groessee 65 daraufgeschrieben, 
	 * danach soll im selben laufwerk 
	 * disc 2 geladen werden, obwohl sich disc 1 noch im laufwerk befindet.
	 * danach soll der speicher von disc 2 abgefregt werden, was nicht geht da sie sich ja nicht in einem laufwerk befindet
	 * --> aufruf von dd1.available() leifert den unbelegten speicher des sich darin befindlichen mediums 
	 * -> in unserem fall disc 1 (700000 - 65) -> es wird 699935 returned 
	 */
    public boolean Test57(String methodName) {
        Disc disc1 = new CDRW();
        Disc disc2 = new DVD();
        DiscDrive dd1 = new DVDDrive();
        dd1.load(disc1);
        dd1.use(65);
        dd1.load(disc2);
        return this.matchResultToInteger(methodName, 699935, dd1.available());
    }

    /**
	 * Testfall 58
	 * Es soll eine CDRW im BlueRaydrive geladen und etwas darauf gespeichert werden.
	 * Beider Operationen werden nicht ausgefuehrt da cdrw mit blueraydrive nicht kompatibel ist.
	 * Der Aufruf von dd1.available liefert -1, da sich kein kompatibles medium im laufwerk befindet
	 * -1 wird bei uns im Fehlerfall geliefert(ersichtlich in blueraydrive und dvddrive)
	 */
    public boolean Test58(String methodName) {
        Disc disc1 = new CDRW();
        DiscDrive dd1 = new BlueRayDrive();
        dd1.load(disc1);
        dd1.use(65);
        return this.matchResultToInteger(methodName, -1, dd1.available());
    }

    /**
	 * Testfall 59
	 * Zuerst wird versucht eine cdrw im bluerayaufwerk zu laden und zu beschreiben, was wegen 
	 * der nicht gegebenen kompatibilitaet nicht geht.
	 * danach wird ohne dd1.unload() auszufuehren im blueraylaufwerk disc 2 geladen und beschrieben
	 * das funktioniert weil disc 1 wegen kompatibilitaetsproblemen nicht geladen wurde.
	 * die eingelegte disc 2 wird beschrieben und der freie speicher ausgegeben
	 * --> 4700000 - 100 - 5045 = 4694855 wird returned
	 */
    public boolean Test59(String methodName) {
        Disc disc1 = new CDRW();
        Disc disc2 = new DVD();
        DiscDrive dd1 = new BlueRayDrive();
        dd1.load(disc1);
        dd1.use(65);
        dd1.load(disc2);
        dd1.use(100);
        dd1.use(5045);
        return this.matchResultToInteger(methodName, 4694855, dd1.available());
    }

    /**
	 * Testfall 200
	 * Es wird versucht ein neues USB Laufwerk an einen USBPort zu stecken
	 *  Es wird true erwartet
	 */
    public boolean Test200(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        return this.matchResultToBoolean(methodName, true, usbPort.plugIn(usbDrive));
    }

    /**
	 * Testfall 210
	 * Zwei USB Laufwerke an den selben USBPort stecken
	 *  Es wird false erwartet
	 */
    public boolean Test210(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive1 = new USBDrive();
        USBDrive usbDrive2 = new USBDrive();
        usbPort.plugIn(usbDrive1);
        return this.matchResultToBoolean(methodName, false, usbPort.plugIn(usbDrive2));
    }

    /**
	 * Testfall 220
	 * Zwei USB Laufwerke an den selben USBPort stecken, Laufwerk dazwischen
	 * abstecken!
	 *  Es wird true erwartet
	 */
    public boolean Test220(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive1 = new USBDrive();
        USBDrive usbDrive2 = new USBDrive();
        usbPort.plugIn(usbDrive1);
        usbPort.plugOut();
        return this.matchResultToBoolean(methodName, true, usbPort.plugIn(usbDrive2));
    }

    /**
	 * Testfall 230
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine DVD einlegen
	 *  Es wird true erwartet
	 */
    public boolean Test230(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        DVD dvd = new DVD();
        usbPort.plugIn(usbDrive);
        return this.matchResultToBoolean(methodName, true, usbDrive.load(dvd));
    }

    /**
	 * Testfall 240
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine CDRW einlegen
	 *  Es wird true erwartet
	 */
    public boolean Test240(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        CDRW cd = new CDRW();
        usbPort.plugIn(usbDrive);
        return this.matchResultToBoolean(methodName, true, usbDrive.load(cd));
    }

    /**
	 * Testfall 250
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine BlueRay einlegen
	 *  Es wird false erwartet
	 */
    public boolean Test250(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        BlueRay blueray = new BlueRay();
        usbPort.plugIn(usbDrive);
        return this.matchResultToBoolean(methodName, false, usbDrive.load(blueray));
    }

    /**
	 * Testfall 260
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine CDRW einlegen
	 * Auslesen des freien Speicherplatz
	 *  Es wird 700000 erwartet
	 */
    public boolean Test260(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        CDRW cd = new CDRW();
        usbPort.plugIn(usbDrive);
        usbDrive.load(cd);
        return this.matchResultToInteger(methodName, 700000, usbPort.available());
    }

    /**
	 * Testfall 270
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine CDRW einlegen
	 * Auslesen des freien Speicherplatz nach belegen von 400MB
	 *  Es wird 300000 erwartet
	 */
    public boolean Test270(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        CDRW cd = new CDRW();
        usbPort.plugIn(usbDrive);
        usbDrive.load(cd);
        usbPort.use(400000);
        return this.matchResultToInteger(methodName, 300000, usbPort.available());
    }

    /**
	 * Testfall 280
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine CDRW einlegen
	 * Auslesen des freien Speicherplatz nach belegen von 2x 400MB
	 *  Es wird 300000 erwartet
	 */
    public boolean Test280(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        CDRW cd = new CDRW();
        usbPort.plugIn(usbDrive);
        usbDrive.load(cd);
        usbPort.use(400000);
        usbPort.use(400000);
        return this.matchResultToInteger(methodName, 300000, usbPort.available());
    }

    /**
	 * Testfall 290
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine DVD einlegen
	 * Auslesen des freien Speicherplatz nach belegen von 2x 400MB
	 *  Es wird 3900000 erwartet
	 */
    public boolean Test290(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        DVD dvd = new DVD();
        usbPort.plugIn(usbDrive);
        usbDrive.load(dvd);
        usbPort.use(400000);
        usbPort.use(400000);
        return this.matchResultToInteger(methodName, 3900000, usbPort.available());
    }

    /**
	 * Testfall 300
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine DVD einlegen
	 * Belegen von 2x 400MB, -40KB
	 *  Es wird false erwartet
	 */
    public boolean Test300(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        DVD dvd = new DVD();
        usbPort.plugIn(usbDrive);
        usbDrive.load(dvd);
        usbPort.use(400000);
        usbPort.use(400000);
        return this.matchResultToBoolean(methodName, false, usbPort.use(-40));
    }

    /**
	 * Testfall 310
	 * Ein USBLaufwerk an einem USBPort anstecken und in das Laufwerk eine Blueray einlegen
	 * Auslesen des freien Speicherplatz nach belegen von 2x 400MB
	 *  Es wird false erwartet
	 */
    public boolean Test310(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        BlueRay blueray = new BlueRay();
        usbPort.plugIn(usbDrive);
        usbDrive.load(blueray);
        return this.matchResultToBoolean(methodName, false, usbPort.use(300));
    }

    /**
	 * Testfall 320
	 * Laden einer Disc obwohl das Laufwerk noch nicht angesteckt ist
	 *  Es wird false erwartet
	 */
    public boolean Test320(String methodName) {
        USBDrive usbDrive = new USBDrive();
        Disc blueray = new BlueRay();
        return this.matchResultToBoolean(methodName, false, usbDrive.load(blueray));
    }

    /**
	 * Testfall 330
	 * Laden einer Disc obwohl das Laufwerk noch nicht angesteckt ist
	 *  Es wird false erwartet
	 */
    public boolean Test330(String methodName) {
        USBDrive usbDrive = new USBDrive();
        Disc dvd = new DVD();
        return this.matchResultToBoolean(methodName, false, usbDrive.load(dvd));
    }

    /**
	 * Testfall 340
	 * Laden eines USBSticks an einen USBPort
	 *  Es wird true erwartet
	 */
    public boolean Test340(String methodName) {
        USBPort usbPort = new USBPort();
        USB usbStick = new USBStick();
        return this.matchResultToBoolean(methodName, true, usbPort.plugIn(usbStick));
    }

    /**
	 * Testfall 350
	 * Laden eines USBSticks an einen USBPort, auslesen seiner Kapazitaet
	 *  Es wird 25000000 erwartet
	 */
    public boolean Test350(String methodName) {
        USBPort usbPort = new USBPort();
        USB usbStick = new USBStick();
        usbPort.plugIn(usbStick);
        return this.matchResultToInteger(methodName, 25000000, usbPort.available());
    }

    public boolean Test355(String methodName) {
        USBPort usbPort = new USBPort();
        USBDrive usbDrive = new USBDrive();
        DVD dvd = new DVD();
        usbPort.plugIn(usbDrive);
        usbDrive.load(dvd);
        usbPort.plugOut();
        usbPort.plugIn(usbDrive);
        return this.matchResultToBoolean(methodName, true, usbDrive.use(1));
    }

    /**
	 * Testfall 360
	 * Laden eines USBSticks an einen USBPort, Speicherplatz belegen und
	 * freien Speicherplatz ausgeben
	 *  Es wird 20000000 erwartet
	 */
    public boolean Test360(String methodName) {
        USBPort usbPort = new USBPort();
        USB usbStick = new USBStick();
        usbPort.plugIn(usbStick);
        usbPort.use(5000000);
        return this.matchResultToInteger(methodName, 20000000, usbPort.available());
    }

    /**
	 * Testfall 370
	 * USBSticks: Speicherplatz belegen und
	 * freien Speicherplatz ausgeben ohne anstecken
	 *  Es wird -1 erwartet
	 */
    public boolean Test370(String methodName) {
        USB usbStick = new USBStick();
        usbStick.use(5000000);
        return this.matchResultToInteger(methodName, -1, usbStick.available());
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

    /**
	 * Kontrolliert ob diese Funktion aufgerufen wird oder nicht
	 * @param methodName
	 * @param expectedResult true wenn aufruf erwartet, sonst false
	 * @param returnedResult
	 * 
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
