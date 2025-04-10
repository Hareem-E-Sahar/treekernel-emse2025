package org.joverseer.tools;

import java.util.ArrayList;
import org.joverseer.domain.Army;
import org.joverseer.domain.ArmyElement;
import org.joverseer.domain.ArmyElementType;
import org.joverseer.domain.ArmyEstimate;
import org.joverseer.domain.Character;
import org.joverseer.domain.ClimateEnum;
import org.joverseer.domain.FortificationSizeEnum;
import org.joverseer.domain.NationRelationsEnum;
import org.joverseer.domain.PopulationCenter;
import org.joverseer.domain.PopulationCenterSizeEnum;
import org.joverseer.metadata.domain.HexTerrainEnum;
import org.joverseer.tools.combatCalc.Combat;
import org.joverseer.tools.combatCalc.CombatArmy;
import org.joverseer.tools.combatCalc.CombatPopCenter;

public class CombatUtils {

    /**
	 * Old way of computing enHI
	 * Simply take into account number of troops per troop type, ignoring morale, training, weapons, etc
	 */
    public static int getNakedHeavyInfantryEquivalent2(Army a) {
        int nhi = 0;
        for (ArmyElement ae : a.getElements()) {
            double f = 0;
            if (ae.getArmyElementType().equals(ArmyElementType.HeavyCavalry)) {
                f = 1.6;
            } else if (ae.getArmyElementType().equals(ArmyElementType.LightCavalry)) {
                f = .8;
            } else if (ae.getArmyElementType().equals(ArmyElementType.HeavyInfantry)) {
                f = 1;
            } else if (ae.getArmyElementType().equals(ArmyElementType.LightInfantry)) {
                f = .5;
            } else if (ae.getArmyElementType().equals(ArmyElementType.Archers)) {
                f = .4;
            } else if (ae.getArmyElementType().equals(ArmyElementType.MenAtArms)) {
                f = .2;
            }
            nhi += new Double(ae.getNumber() * f).intValue();
        }
        return nhi;
    }

    /**
     * Heuristic method for computing enHI
     * Takes into account weapons, training, command total, morale, etc 
     */
    public static int getNakedHeavyInfantryEquivalent(Army a, Character c) {
        int nhi = 0;
        for (ArmyElement ae : a.getElements()) {
            double f = 0;
            if (ae.getArmyElementType().equals(ArmyElementType.HeavyCavalry)) {
                f = 1.6;
            } else if (ae.getArmyElementType().equals(ArmyElementType.LightCavalry)) {
                f = .8;
            } else if (ae.getArmyElementType().equals(ArmyElementType.HeavyInfantry)) {
                f = 1;
            } else if (ae.getArmyElementType().equals(ArmyElementType.LightInfantry)) {
                f = .5;
            } else if (ae.getArmyElementType().equals(ArmyElementType.Archers)) {
                f = .4;
            } else if (ae.getArmyElementType().equals(ArmyElementType.MenAtArms)) {
                f = .2;
            }
            int commandRank = c == null ? 30 : c.getCommandTotal();
            double wf = (180d + ae.getWeapons() + ae.getTraining()) / 400d / (200d / 400d);
            double cf = (a.getMorale() + commandRank + 100d) / 200d / (160d / 200d);
            double af = (100d + ae.getArmor()) / 100d;
            f = (f * wf * cf) + (2 * f * af);
            f /= 3;
            nhi += new Double(ae.getNumber() * f).intValue();
        }
        return nhi;
    }

    /**
     * Heuristic enHI calculation
     */
    public static int getNakedHeavyInfantryEquivalent(CombatArmy a) {
        int nhi = 0;
        for (ArmyElement ae : a.getElements()) {
            double f = 0;
            if (ae.getArmyElementType().equals(ArmyElementType.HeavyCavalry)) {
                f = 1.6;
            } else if (ae.getArmyElementType().equals(ArmyElementType.LightCavalry)) {
                f = .8;
            } else if (ae.getArmyElementType().equals(ArmyElementType.HeavyInfantry)) {
                f = 1;
            } else if (ae.getArmyElementType().equals(ArmyElementType.LightInfantry)) {
                f = .5;
            } else if (ae.getArmyElementType().equals(ArmyElementType.Archers)) {
                f = .4;
            } else if (ae.getArmyElementType().equals(ArmyElementType.MenAtArms)) {
                f = .2;
            }
            int commandRank = a.getCommandRank();
            double wf = (180d + ae.getWeapons() + ae.getTraining()) / 400d / (200d / 400d);
            double cf = (a.getMorale() + commandRank + 100d) / 200d / (160d / 200d);
            double af = (100d + ae.getArmor()) / 100d;
            f = (f * wf * cf) + (2 * f * af);
            f /= 3;
            nhi += new Double(ae.getNumber() * f).intValue();
        }
        return nhi;
    }

    /**
     * Computes eNHI based on the combat calculator
     */
    public static int getNakedHeavyInfantryEquivalent3(Army a) {
        return getNakedHeavyInfantryEquivalent3(new CombatArmy(a));
    }

    /**
     * Computes eNHI based on the combat calculator
     * lossOptimismFactor can be -1, 0 or 1. -1 assumes minimum losses within the range, 0 assumes avg losses, 1 assumes maximum losses
     */
    public static int getNakedHeavyInfantryEquivalent3(ArmyEstimate a, int lossOptimismFactor) {
        return getNakedHeavyInfantryEquivalent3(new CombatArmy(a, lossOptimismFactor));
    }

    /**
     * Computes eNHI based on the combat calculator
     */
    public static int getNakedHeavyInfantryEquivalent3(CombatArmy ca) {
        int nhi1 = getNakedHeavyInfantryEquivalent(ca) * 4 / 3;
        int nhi2 = getNakedHeavyInfantryEquivalent(ca) / 4 * 3;
        while (true) {
            int nhi = (nhi1 + nhi2) / 2;
            ca.setLosses(0);
            CombatArmy ca2 = new CombatArmy();
            ArrayList<ArmyElement> aes = new ArrayList<ArmyElement>();
            ArmyElement ae = new ArmyElement(ArmyElementType.HeavyInfantry, nhi);
            ae.setTraining(10);
            ae.setWeapons(10);
            ae.setArmor(0);
            aes.add(ae);
            ca2.setElements(aes);
            ca2.setMorale(30);
            ca2.setCommandRank(30);
            Combat combat = new Combat();
            combat.setMaxRounds(20);
            combat.addToSide(0, ca);
            combat.addToSide(1, ca2);
            combat.setClimate(ClimateEnum.Mild);
            combat.setTerrain(HexTerrainEnum.plains);
            combat.runArmyBattle();
            if ((ca.getLosses() > 98 && ca2.getLosses() > 98) || Math.abs(ca2.getLosses() - ca.getLosses()) < 1) {
                return nhi;
            } else if (nhi1 - nhi2 < 2) {
                return nhi;
            } else if (ca.getLosses() > ca2.getLosses()) {
                nhi1 = nhi;
            } else {
                nhi2 = nhi;
            }
        }
    }

    public static CombatArmy findCombatArmyRequiredToCapturePopCenter(PopulationCenterSizeEnum pcSize, FortificationSizeEnum fort, int loyalty, ClimateEnum climate, HexTerrainEnum terrain) {
        if (pcSize.getCode() == 0) return null;
        CombatPopCenter pc = new CombatPopCenter();
        pc.setSize(pcSize);
        pc.setFort(fort);
        pc.setLoyalty(loyalty);
        int nhi1 = 1;
        int nhi2 = 30000;
        while (true) {
            pc.setCaptured(false);
            int nhi = (nhi1 + nhi2) / 2;
            CombatArmy ca2 = new CombatArmy();
            ArrayList<ArmyElement> aes = new ArrayList<ArmyElement>();
            ArmyElement ae = new ArmyElement(ArmyElementType.HeavyInfantry, nhi);
            ae.setTraining(10);
            ae.setWeapons(10);
            ae.setArmor(0);
            aes.add(ae);
            ca2.setElements(aes);
            ca2.setMorale(30);
            ca2.setCommandRank(30);
            Combat combat = new Combat();
            combat.setMaxRounds(20);
            combat.addToSide(0, ca2);
            combat.setSide2Pc(pc);
            combat.setAttackPopCenter(true);
            combat.setClimate(climate);
            combat.setTerrain(terrain);
            combat.autoSetRelationsToHated();
            combat.runWholeCombat();
            int pcDefense = combat.computePopCenterStrength(pc);
            int diff = pc.getStrengthOfAttackingArmies() - pcDefense;
            if (Math.abs(diff) < 50) {
                return ca2;
            } else if (diff < 0) {
                nhi1 = nhi;
            } else {
                nhi2 = nhi;
            }
        }
    }

    public static int canCapturePopCenter(Army a, PopulationCenterSizeEnum pcSize, FortificationSizeEnum fort) {
        if (pcSize.getCode() == 0) return -1;
        PopulationCenter pc = new PopulationCenter();
        pc.setSize(pcSize);
        pc.setFortification(fort);
        int l1 = 100;
        int l2 = 0;
        int lastCaptureLoyalty = -1;
        do {
            int l = (l1 + l2) / 2;
            CombatArmy ca = new CombatArmy(a);
            CombatPopCenter cpc = new CombatPopCenter(pc);
            cpc.setLoyalty(l);
            Combat combat = new Combat();
            combat.addToSide(0, ca);
            combat.setSide2Pc(cpc);
            combat.getSide1Relations()[0][10] = NationRelationsEnum.Hated;
            combat.setMaxRounds(10);
            combat.setClimate(ClimateEnum.Mild);
            combat.setTerrain(HexTerrainEnum.plains);
            combat.runPcBattle(0, 0);
            if (cpc.isCaptured()) {
                lastCaptureLoyalty = l;
                if (l2 < l) {
                    l2 = l;
                } else {
                    l2++;
                    if (l1 < l2) break;
                }
            } else {
                if (l1 > l) {
                    l1 = l;
                } else {
                    l1--;
                    if (l1 < l2) break;
                }
            }
        } while (true);
        return lastCaptureLoyalty;
    }
}
