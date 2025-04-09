package saf.fight.animation;

import saf.ast.*;
import saf.ast.enumType.Characteristics;
import saf.fight.action.FightAct;
import saf.fight.action.MoveAct;
import saf.fight.action.SideEnum;
import java.awt.*;

public class Status {

    private Bot fighter;

    private double fighterSpeed;

    private int punchPower, kickPower, punchReach, kickReach;

    private MoveAct safMoveActConfig;

    private FightAct safFightActConfig;

    private final int dValue = 5;

    public Status(Bot b, Point p, String fileName, SideEnum s) {
        fighter = b;
        calculateFighterSpeed();
        safMoveActConfig = new MoveAct(p, s);
        safFightActConfig = new FightAct(fileName);
    }

    public FightAct getFightStatus() {
        return safFightActConfig;
    }

    public MoveAct getMoveStatus() {
        return safMoveActConfig;
    }

    public double getSpeed() {
        return fighterSpeed;
    }

    public int getPunchPower() {
        return punchPower;
    }

    public int getKickPower() {
        return kickPower;
    }

    public int getPunchReach() {
        return punchReach;
    }

    public int getKickReach() {
        return kickReach;
    }

    private void calculateFighterSpeed() {
        int height = 0, weight = 0;
        preConfigPersonality();
        weight = (punchPower + kickPower) / 2;
        height = (punchReach + kickReach) / 2;
        fighterSpeed = Math.abs(0.5 * (height - weight));
    }

    private void preConfigPersonality() {
        String name;
        boolean isExist;
        for (Characteristics c : Characteristics.values()) {
            isExist = false;
            for (Personality p : fighter.getPersonality()) {
                name = p.getCharacterName();
                if (name.equalsIgnoreCase(c.toString())) {
                    setCharValue(c, p.getCharacterValue());
                    isExist = true;
                    break;
                }
            }
            if (!isExist) setCharValue(c, dValue);
        }
    }

    private void setCharValue(Characteristics chara, int v) {
        switch(chara) {
            case punchPower:
                punchPower = v;
                break;
            case kickPower:
                kickPower = v;
                break;
            case punchReach:
                punchReach = v;
                break;
            case kickReach:
                kickReach = v;
                break;
            default:
                break;
        }
    }
}
