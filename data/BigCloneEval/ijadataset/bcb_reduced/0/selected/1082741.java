package saf;

import java.util.List;
import config.settings;
import checker.SafInterface;
import checker.SafVisitor;

public class Personality implements SafInterface {

    private List<Strength> strenghts;

    private float speed;

    public Personality(List<Strength> strenghts) {
        this.strenghts = strenghts;
        int punchPower = 5;
        int kickPower = 5;
        int punchReach = 5;
        int kickReach = 5;
        for (Strength strength : strenghts) {
            if (strength.getCharacteristic().equals(settings.PUNCH_POWER)) punchPower = strength.getValue();
            if (strength.getCharacteristic().equals(settings.KICK_POWER)) kickPower = strength.getValue();
            if (strength.getCharacteristic().equals(settings.PUNCH_REACH)) punchReach = strength.getValue();
            if (strength.getCharacteristic().equals(settings.KICK_REACH)) kickReach = strength.getValue();
        }
        int weight = (punchPower + kickPower) / 2;
        int height = (punchReach + kickReach) / 2;
        this.speed = (height - weight) / 2;
    }

    public List<Strength> getStrengths() {
        return this.strenghts;
    }

    public boolean hasStrength(String s) {
        for (Strength strength : strenghts) {
            if (strength.getCharacteristic().equals(s)) return true;
        }
        return false;
    }

    public Strength getStrength(String s) {
        for (Strength strenght : strenghts) {
            if (strenght.getCharacteristic().equals(s)) return strenght;
        }
        return new Strength("", 0);
    }

    public float getSpeed() {
        return this.speed;
    }

    @Override
    public void accept(SafVisitor visitor) {
        for (Strength s : strenghts) visitor.visit(s);
        visitor.visit(this);
    }
}
