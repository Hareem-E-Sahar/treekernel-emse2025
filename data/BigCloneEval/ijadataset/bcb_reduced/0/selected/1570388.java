package saf.game.state;

import saf.game.GameConstant;
import saf.structure.Bot;
import saf.structure.intelligence.BehaviorIntelligence;

public class BotState implements GameConstant {

    private static final int CONST_DISTANCEFROMWALL = 50;

    private static final int CONST_HITPOINTS = 100;

    public BotState(Bot bot, String side) {
        this.bot = bot;
        this.side = side;
        this.hitpoints = CONST_HITPOINTS;
        this.winner = false;
        this.speed = calculateSpeed(bot);
        this.distanceFromWall = CONST_DISTANCEFROMWALL;
        this.lastFightAction = "";
        this.lastMoveAction = "stand";
    }

    private Bot bot;

    private String side;

    private int hitpoints;

    private boolean winner;

    private double speed;

    private int distanceFromWall;

    private double credits;

    private String lastFightAction;

    private String lastMoveAction;

    public int getHitpoints() {
        return hitpoints;
    }

    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public void updateHitpoints(double attackPower) {
        this.hitpoints = (int) (this.hitpoints - attackPower);
    }

    public boolean isJumping() {
        return lastMoveAction.equals(MOVE_ACTION_JUMP);
    }

    public Bot getBot() {
        return bot;
    }

    public String getSide() {
        return side;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean value) {
        this.winner = value;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public double getCredits() {
        return this.credits;
    }

    public void updateCredits() {
        this.credits += this.speed;
    }

    public void updateCredits(double dAmount) {
        this.credits += dAmount;
    }

    public int getDistanceFromWall() {
        return distanceFromWall;
    }

    public void setDistanceFromWall(int distanceFromWall) {
        this.distanceFromWall = distanceFromWall;
    }

    public void updateDistanceFromWall(int dAmount) {
        this.distanceFromWall += dAmount;
    }

    public String getLastFightAction() {
        return lastFightAction;
    }

    public void setLastFightAction(String lastFightAction) {
        this.lastFightAction = lastFightAction;
    }

    private double calculateSpeed(Bot bot) {
        int punchPower = BehaviorIntelligence.getCharacteristic(bot, CHARACTERISTIC_PUNCHPOWER);
        int kickPower = BehaviorIntelligence.getCharacteristic(bot, CHARACTERISTIC_KICKPOWER);
        int punchReach = BehaviorIntelligence.getCharacteristic(bot, CHARACTERISTIC_PUNCHREACH);
        int kickReach = BehaviorIntelligence.getCharacteristic(bot, CHARACTERISTIC_KICKREACH);
        double weight = (punchPower + kickPower) / 2;
        double height = (punchReach + kickReach) / 2;
        return (0.5 * (height - weight)) + 5;
    }

    public String getLastMoveAction() {
        return lastMoveAction;
    }

    public void setLastMoveAction(String lastMoveAction) {
        this.lastMoveAction = lastMoveAction;
    }
}
