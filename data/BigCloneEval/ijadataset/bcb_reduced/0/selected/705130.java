package program;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.lang.Math;
import grammar.Evaluators.*;

public class Fight {

    Bot botLeft, botRight;

    private int lifeLeft, lifeRight;

    private int positionLeft, positionRight;

    private String statusLeft, statusRight;

    private int distance;

    private InputRule currentMoveLeft, currentMoveRight;

    private InputRule currentFightLeft, currentFightRight;

    private static final Random RANDOM = new Random();

    public Fight(Bot botLeft, Bot botRight) {
        this.botLeft = botLeft;
        this.lifeLeft = 100;
        this.botRight = botRight;
        this.lifeRight = 100;
        this.positionLeft = 10;
        this.positionRight = 500;
        this.statusLeft = "even";
        this.statusRight = "even";
        this.botLeft.setSpeed(calcSpeed(botLeft));
        this.botRight.setSpeed(calcSpeed(botRight));
    }

    public int getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(int positionLeft) {
        this.positionLeft = positionLeft;
    }

    public int getDistance() {
        return this.positionRight - this.positionLeft;
    }

    public int getPositionRight() {
        return positionRight;
    }

    public void setPositionRight(int positionRight) {
        this.positionRight = positionRight;
    }

    public int getLifeRight() {
        return (this.lifeRight > 0) ? this.lifeRight : 0;
    }

    public void setLifeRight(int value) {
        this.lifeRight = value;
    }

    public int getLifeLeft() {
        return (this.lifeLeft > 0) ? this.lifeLeft : 0;
    }

    public void setLifeLeft(int value) {
        this.lifeLeft = value;
    }

    public String getStatusLeft() {
        return statusLeft;
    }

    public void setStatusLeft(String statusLeft) {
        this.statusLeft = statusLeft;
    }

    public String getStatusRight() {
        return statusRight;
    }

    public void setStatusRight(String statusRight) {
        this.statusRight = statusRight;
    }

    public String getCurrentFightAction(boolean left) {
        return (left) ? this.currentFightLeft.getInputrule(true) : this.currentFightRight.getInputrule(true);
    }

    public String calculateStrengthDif(int hisLife, int opponentLife) {
        int dif = hisLife - opponentLife;
        if ((dif < 20) & (dif > 0)) {
            return "even";
        }
        if ((dif >= 20) & (dif < 40)) {
            return "stronger";
        }
        if (dif >= 40) {
            return "much stronger";
        }
        if ((dif > -20) & (dif < 0)) {
            return "even";
        }
        if ((dif <= -20) & (dif > -40)) {
            return "weaker";
        }
        if (dif <= -40) {
            return "much weaker";
        }
        return "even";
    }

    public boolean stillAlive() {
        return !(this.getLifeLeft() <= 0 || this.getLifeRight() <= 0);
    }

    public double calcSpeed(Bot b) {
        int punchPower = 5;
        int kickPower = 5;
        int punchReach = 5;
        int kickReach = 5;
        for (Characteristic c : b.getCharacteristics()) {
            if (c.getCharacterName().equalsIgnoreCase("punchPower")) {
                punchPower = c.getCharacterValue();
            }
            ;
            if (c.getCharacterName().equalsIgnoreCase("kickPower")) {
                kickPower = c.getCharacterValue();
            }
            ;
            if (c.getCharacterName().equalsIgnoreCase("punchReach")) {
                punchReach = c.getCharacterValue();
            }
            ;
            if (c.getCharacterName().equalsIgnoreCase("kickReach")) {
                kickReach = c.getCharacterValue();
            }
            ;
        }
        double weight = (punchPower + kickPower) / 2;
        double heigth = (punchReach + kickReach) / 2;
        double speed = Math.abs(0.5 * (heigth - weight));
        if (speed == 0) {
            speed = 1.0;
        }
        return speed;
    }

    private String getDistanceString() {
        return this.getDistance() < 20 ? "near" : "far";
    }

    public InputRule getAction(Bot b, String strengthdiff, boolean move) {
        List<InputRule> action = new LinkedList<InputRule>();
        List<String> operator = new LinkedList<String>();
        for (Rule r : b.getRules()) {
            for (Condition c : r.getConditionList()) {
                if ((c.getLeft().equalsIgnoreCase(strengthdiff)) || (c.getLeft().equalsIgnoreCase(getDistanceString()))) {
                    if ((c.getType().equalsIgnoreCase("and")) || (c.getType().equalsIgnoreCase("or"))) {
                        operator.add(c.getType());
                    }
                    if (move) {
                        action.add(r.getMoveRule());
                    } else {
                        action.add(r.getFightRule());
                    }
                }
            }
        }
        if (action.size() == 0) {
            return getAction(b, "always", move);
        }
        return action.get(RANDOM.nextInt(action.size()));
    }

    public void doAction() {
        this.setStatusLeft(calculateStrengthDif(this.lifeLeft, this.lifeRight));
        this.setStatusRight(calculateStrengthDif(this.lifeRight, this.lifeLeft));
        this.distance = this.positionRight - this.positionLeft;
        this.currentMoveLeft = getAction(botLeft, this.statusLeft, true);
        this.currentMoveRight = getAction(botRight, this.statusRight, true);
        this.currentFightLeft = getAction(botLeft, this.statusLeft, false);
        this.currentFightRight = getAction(botRight, this.statusRight, false);
        calculateLife(this.currentFightLeft, this.currentFightRight);
        calculateLife(this.currentFightRight, this.currentFightLeft);
        calculatePositions(this.currentMoveLeft, this.currentMoveRight);
    }

    public void calculateLife(InputRule irl, InputRule irr) {
        if (this.distance > 10) {
            return;
        }
        if ((!irl.getInputrule(true).contains("block")) && (irr.getInputrule(true).contains("kick"))) {
            this.lifeLeft -= 5 * this.botLeft.getSpeed();
        }
        if ((!irl.getInputrule(true).contains("block")) && (irr.getInputrule(true).contains("punch"))) {
            this.lifeLeft -= 3 * this.botLeft.getSpeed();
        }
        if ((irl.getInputrule(true).contains("block")) && (irr.getInputrule(true).contains("kick"))) {
            this.lifeLeft -= 3 * this.botLeft.getSpeed();
        }
        if ((irl.getInputrule(true).contains("block")) && (irr.getInputrule(true).contains("punch"))) {
            this.lifeLeft -= 1 * this.botLeft.getSpeed();
        }
        this.lifeRight -= 1;
        this.lifeLeft -= 1;
    }

    public void calculatePositions(InputRule irl, InputRule irr) {
        this.positionLeft = this.positionLeft + calcMove(irl.getInputrule(true));
        this.positionRight = this.positionRight - calcMove(irr.getInputrule(true));
    }

    public int calcMove(String m) {
        if (m.contains("run_towards")) {
            return 10;
        }
        if (m.contains("run_away")) {
            return -10;
        }
        if (m.contains("walk_towards")) {
            return 5;
        }
        if (m.contains("walk_away")) {
            return -5;
        }
        if (m.contains("crouch")) {
            return 2;
        }
        return 0;
    }
}
