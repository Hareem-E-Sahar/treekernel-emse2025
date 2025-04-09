package abc.soot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.UnitBox;
import soot.jimple.GotoStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;

/**
 * @author sascha
 *
 *
 * This may be competely useless..is there a maximum on jump distance?
 */
public class FarJumpEliminator extends BodyTransformer {

    private static void debug(String message) {
        if (abc.main.Debug.v().farJumpEliminator) System.err.println("FJE*** " + message);
    }

    private static FarJumpEliminator instance = new FarJumpEliminator();

    public static void reset() {
        instance = new FarJumpEliminator();
    }

    public static FarJumpEliminator v() {
        return instance;
    }

    protected void internalTransform(Body body, String phaseName, Map options) {
        eliminateFarJumps(body.getMethod());
    }

    public static int FAR_JUMP = 3000;

    public static void eliminateFarJumps(SootMethod m) {
        Body b = m.getActiveBody();
        Chain units = m.getActiveBody().getUnits().getNonPatchingChain();
        if (units.size() < FAR_JUMP) return;
        Map stmtPositions = new HashMap(2 * units.size());
        {
            int stmtPos = 0;
            for (Iterator it = units.iterator(); it.hasNext(); stmtPos++) {
                Stmt s = (Stmt) it.next();
                stmtPositions.put(s, new Integer(stmtPos));
            }
        }
        int uncorrectedJumps;
        do {
            uncorrectedJumps = 0;
            Object[] unitArray = units.toArray();
            debug("Array size " + unitArray.length);
            int currentPos = 0;
            for (Iterator it = new ArrayList(units).iterator(); it.hasNext(); currentPos++) {
                Stmt s = (Stmt) it.next();
                if (!s.branches()) continue;
                List boxes = s.getUnitBoxes();
                for (Iterator itB = boxes.iterator(); itB.hasNext(); ) {
                    UnitBox box = (UnitBox) itB.next();
                    if (box.getUnit() instanceof Stmt) {
                        Stmt oldTarget = (Stmt) box.getUnit();
                        int targetPos = ((Integer) stmtPositions.get(oldTarget)).intValue();
                        int distance = Math.abs(currentPos - targetPos);
                        if (distance > FAR_JUMP) {
                            debug("changing far jump with distance " + distance + " in method " + m);
                            int newTarget = (currentPos + targetPos) / 2;
                            debug("New target " + newTarget);
                            Stmt ns = (Stmt) unitArray[newTarget];
                            GotoStmt gs2 = Jimple.v().newGotoStmt(oldTarget);
                            units.insertBefore(gs2, ns);
                            box.setUnit(gs2);
                            GotoStmt gs3 = Jimple.v().newGotoStmt(ns);
                            units.insertBefore(gs3, gs2);
                            stmtPositions.put(gs2, new Integer(newTarget));
                            stmtPositions.put(gs3, new Integer(newTarget));
                        }
                        if (distance > (FAR_JUMP * 2)) {
                            debug(" jump still too big");
                            uncorrectedJumps++;
                        }
                    }
                }
            }
        } while (uncorrectedJumps > 0);
    }

    public static void eliminateFarJumps() {
        for (Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for (Iterator mIt = cl.getSootClass().getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if (!m.hasActiveBody()) continue;
                eliminateFarJumps(m);
            }
        }
    }
}
