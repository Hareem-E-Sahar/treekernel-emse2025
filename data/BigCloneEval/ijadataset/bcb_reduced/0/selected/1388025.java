package pcode;

import jaguar.Jaguar;
import jaguar.JaguarImage;
import jaguar.JaguarPCode;
import jaguar.JaguarRectangle;
import jaguar.JaguarVM;
import java.io.File;
import java.util.ArrayList;

/**
 * @author peter
 *
 * <p>
 * <b>railroad:</b>
 * <pre>
 * ---- WAIT ----- number1 --------------------------------------------------------------|
 *            |                                                                        ^
 *            +--- number2 ------- TIMES EVERY -- number3 -- UNTIL -- condition expr --+
 *            | |              ^                                                       |    
 *            | +- UNLIMITED --+                                                       |    
 *            |                                                                        |    
 *            +--- rectangle ----------------------------------------------------------+  
 *                               |             |
 *                               +-- number4 --+
 * </pre>
 * <p>
 * The numbers can be expressions as long as they do not contain spaces.
 * <p>
 * number1 is the desired number of milliseconds to wait 
 * <p>
 * number2 is the maximum number of times the condition will be evaluated<br> 
 * number3 is the desired number of milliseconds to wait between evaluations<br>
 * as soon as the condition expr evaluates to true the statement is terminated.<br>
 * This format is a perfect alternative for the statements:
 * <pre>
 * FOR var1 FROM 1 BY 1 UNTIL number2/number3
 *   IF condition expr
 *     BREAK
 *   ENDIF
 *   WAIT number3
 * NEXT
 * </pre>
 * It saves a var1 and zillions of list/log lines and the related images with noninformation
 * to code:
 * <pre>
 * WAIT 100 TIMES EVERY 20 UNTIL LOOKUP(logo,screen)
 * </pre>
 * <p>
 * rectangle is the area that is waited for to change
 * <p>
 * number4 is the maximum number of milliseconds to wait for a change,
 * this is optional, the default time is one minute.
 * <p>
 * The status becomes OK when completed succesfully and NOK when the maximum has occurred.<br>
 * A simple WAIT n <b>never</b> affects status. 
 * <p>
 * The WAIT ... UNTIL and the WAIT rectangle are interrupt aware and restartable.
 * <p>
 * @see JaguarIF
 */
public class JaguarWAIT extends JaguarPCode {

    private JaguarImage biRef;

    private boolean unlimited;

    private int restartTimes;

    private int restartSlept;

    private boolean restarted;

    /**
	 * @param vm
	 * @param src
	 * @param line
	 * @param arg 
	 */
    public JaguarWAIT(JaguarVM vm, File src, String line, String arg) {
        super(vm, src, line, arg);
        biRef = null;
        unlimited = stringToken(0).equalsIgnoreCase("UNLIMITED");
        restarted = false;
    }

    protected void complete(ArrayList can, int i) {
        switch(i) {
            case 0:
                candidate(can, " UNLIMITED ~# ", 0);
                return;
            case 1:
                if (isNumToken(0)) {
                    candidate(can, " TIMES ~# ", 1);
                    return;
                }
                if (unlimited) {
                    candidate(can, " TIMES ", 1);
                    return;
                }
                return;
            case 2:
                if (isNumToken(0) || unlimited) {
                    candidate(can, " EVERY ", 2);
                    return;
                }
                return;
            case 4:
                if (isNumToken(0) || unlimited) {
                    candidate(can, " UNTIL ", 4);
                    return;
                }
                return;
        }
    }

    public int tokenType(int i) {
        if (i < 0) return JaguarPCode.OPCODE;
        if (!isToken(i)) return JaguarPCode.COMMENT;
        if (i == 0) {
            if (unlimited) return JaguarPCode.KEYWORD;
            if (!isNumToken(0)) return JaguarPCode.SYMBOL;
            return JaguarPCode.OPERAND;
        }
        if (" times every until ".indexOf(" " + lowerCaseToken(i) + " ") < 0) return JaguarPCode.OPERAND;
        return JaguarPCode.KEYWORD;
    }

    protected void execute() {
        Jaguar.setFocusable(true);
        if (unlimited || isNumToken(0)) {
            if (stringToken(1).equalsIgnoreCase("TIMES") && stringToken(2).equalsIgnoreCase("EVERY") && isNumToken(3) && stringToken(4).equalsIgnoreCase("UNTIL")) {
                int times = unlimited ? 1 : intToken(0);
                int interval = intToken(3);
                int slept = 0;
                if (restarted) {
                    times = restartTimes;
                    slept = restartSlept;
                    restarted = false;
                }
                String expr = vm.macros(stringToken(5));
                boolean log = Jaguar.isLog();
                boolean list = Jaguar.isList();
                vm.setStatus(JaguarVM.OK);
                while (expr.equals("0") || expr.equals("")) {
                    Jaguar.setLog(false);
                    Jaguar.setList(false);
                    vm.setStatus(JaguarVM.NOK);
                    Jaguar.sleep(interval);
                    ++slept;
                    if (!unlimited) --times;
                    if (times < 1) break;
                    if (vm.isUserInterrupt()) {
                        vm.setPc(vm.getPc() - 1);
                        Jaguar.setList(list);
                        Jaguar.setLog(log);
                        restartTimes = times;
                        restartSlept = slept;
                        restarted = true;
                        return;
                    }
                    expr = vm.macros(stringToken(5));
                    vm.setStatus(JaguarVM.OK);
                }
                Jaguar.setList(list);
                Jaguar.setLog(log);
                if (Jaguar.isLog()) vm.logLine(times < 1 ? Jaguar.LOGRED : Jaguar.LOGBLUE, Jaguar.LOGWHITE, Jaguar.LOGPROPORTIONAL, "slept " + slept + " X " + interval + "ms for " + stringToken(5) + " to become true", null, null);
                return;
            }
            Jaguar.sleep(intToken(0));
            return;
        }
        vm.setStatus(JaguarVM.OK);
        String name = lowerCaseToken(0);
        JaguarRectangle r = vm.getRect(name);
        if (r == null) {
            vm.setError(lowerCaseToken(0) + " undefined");
            vm.setStatus(JaguarVM.NOK);
            return;
        }
        vm.minimize();
        if (biRef == null) {
            biRef = Jaguar.createJaguarImage(r);
            if (Jaguar.isLog()) vm.logLine(Jaguar.LOGBLUE, Jaguar.LOGWHITE, Jaguar.LOGPROPORTIONAL, lowerCaseToken(0) + " prefetched", biRef.getImage(), null);
        }
        int t = 20;
        int maxt = 60000;
        if (isNumToken(1)) maxt = intToken(1);
        Jaguar.sleep(t);
        Jaguar.getRobby().waitForIdle();
        Jaguar.getRobby().delay(20);
        JaguarImage biTst = Jaguar.createJaguarImage(r);
        int tott = t;
        if (restarted) {
            tott = restartTimes;
            t = restartSlept;
            restarted = false;
        }
        while (biRef.isSameAs(biTst)) {
            if (tott > maxt) {
                Jaguar.addLine("timeout waiting for " + name + " to change");
                vm.setStatus(JaguarVM.NOK);
                if (Jaguar.isLog()) {
                    JaguarRectangle rr = new JaguarRectangle(r, false);
                    Jaguar.fetch(rr);
                    vm.logLine(Jaguar.LOGRED, Jaguar.LOGWHITE, Jaguar.LOGPROPORTIONAL, "timeout waiting for " + name + " to change", null, rr);
                }
                return;
            }
            if (t < 500) t += t;
            Jaguar.sleep(t);
            if (vm.isUserInterrupt()) {
                vm.setPc(vm.getPc() - 1);
                restartTimes = tott;
                restartSlept = t;
                restarted = true;
                return;
            }
            biTst = Jaguar.createJaguarImage(r);
            tott += t;
        }
        if (Jaguar.isLog()) vm.logLine(Jaguar.LOGBLUE, Jaguar.LOGWHITE, Jaguar.LOGPROPORTIONAL, name + " changed", biTst.getImage(), null);
    }

    /**
	 * <p>
	 * This one prepares the buffered image that's gonna be used 
	 * on the next "WAIT rectangle number2" statement, because the current 
	 * statement may change this image on screen this is done before the
	 * current statement is executed to get the desired effect on the
	 * actual wait statement.
	 * @see #execute()
	 */
    public void prepare() {
        if (unlimited) return;
        vm.setStatus(JaguarVM.OK);
        String name = lowerCaseToken(0);
        JaguarRectangle r = vm.getRect(name);
        if (r == null) {
            biRef = null;
            return;
        }
        vm.minimize();
        Jaguar.getRobby().waitForIdle();
        Jaguar.getRobby().delay(20);
        biRef = new JaguarImage(Jaguar.getRobby().createScreenCapture(r));
        if (Jaguar.isLog()) vm.logLine(Jaguar.LOGBLUE, Jaguar.LOGWHITE, Jaguar.LOGPROPORTIONAL, lowerCaseToken(0) + " prefetched", biRef.getImage(), null);
    }
}
