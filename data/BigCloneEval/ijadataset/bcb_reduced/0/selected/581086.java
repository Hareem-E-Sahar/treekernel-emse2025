package saf;

import saf.attacks.*;
import saf.moves.*;
import safreader.nodes.*;

public class Bot {

    private Bot opponent;

    private String botName = "";

    private Fighter fighter;

    private BotMove currentmove = null;

    private BotAttack currentattack = null;

    private BotTactic tactic = null;

    private boolean jump = false, crouch = false, stand = false;

    private boolean punchLow = false, punchHigh = false, kickLow = false, kickHigh = false, blockLow = false, blockHigh = false;

    private int health = 100;

    private int position;

    private int awayPositions;

    private int nearPositions;

    private int punchReach, kickReach, kickPower, punchPower, speed;

    private Logger logger;

    private final int WEAKER = 4;

    public Bot(Fighter f, int position) {
        fighter = f;
        this.position = position;
        setStrengths();
        speed = this.calculateSpeed();
        botName = f.getName();
        logger = new Logger(botName);
    }

    private void setStrengths() {
        BotPersonality bp = new BotPersonality(fighter.getPersonality());
        punchReach = bp.getPunchReach();
        kickReach = bp.getKickReach();
        kickPower = bp.getKickPower();
        punchPower = bp.getPunchPower();
    }

    public void establishTactic() {
        if (currentmove == null || currentmove.isCompleted()) {
            currentmove = getBotMove();
        }
        currentattack = getBotAttack();
    }

    public void doTactic() {
        logger.log("Move is " + currentmove);
        resetMoves();
        currentmove.doMove();
        logger.log("Attacking with " + currentattack);
        resetAttacks();
        currentattack.doAttack();
        logger.log("Health is: " + health);
    }

    public void damageOpponent(int damage) {
        opponent.damage(damage);
    }

    private BotMove getBotMove() {
        if (tactic == null) {
            tactic = getTactic();
        }
        return tactic.getBotMove();
    }

    private BotAttack getBotAttack() {
        if (tactic == null) {
            tactic = getTactic();
        }
        return tactic.getBotAttack();
    }

    private BotTactic getTactic() {
        return new BotTactic(fighter, this);
    }

    private int calculateSpeed() {
        int weight = (punchPower + kickPower) / 2;
        int height = (punchReach + kickReach) / 2;
        return (int) Math.round(Math.abs(0.5 * (height - weight)));
    }

    public void addOpponent(Bot opponent) {
        this.opponent = opponent;
        awayPositions = Math.max(opponent.getPunchReach(), opponent.getKickReach());
        nearPositions = Math.min(getPunchReach(), getKickReach());
    }

    public int getPosition() {
        return position;
    }

    public int getHealth() {
        return health;
    }

    public void setPosition(int newPosition) {
        position = newPosition;
    }

    public int getOpponentPosition() {
        return opponent.getPosition();
    }

    public boolean isDefeated() {
        if (health > 0) {
            return false;
        } else {
            return true;
        }
    }

    public int getSpeed() {
        return speed;
    }

    public void damage(int damage) {
        health = health - damage;
    }

    public void setPunchLow(Boolean val) {
        punchLow = val;
    }

    public boolean getPunchLow() {
        return punchLow;
    }

    public void setPunchHigh(Boolean val) {
        punchHigh = val;
    }

    public boolean getPunchHigh() {
        return punchHigh;
    }

    public void setKickHigh(Boolean val) {
        kickHigh = val;
    }

    public boolean getKickHigh() {
        return kickHigh;
    }

    public void setKickLow(Boolean val) {
        kickLow = val;
    }

    public boolean getKickLow() {
        return kickLow;
    }

    public void setBlockLow(Boolean val) {
        blockLow = val;
    }

    public void setBlockHigh(Boolean val) {
        blockHigh = val;
    }

    public int opponentDistance() {
        return Math.abs(getPosition() - getOpponentPosition());
    }

    public boolean isOpponentFar() {
        if (opponentDistance() > awayPositions) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOpponentNear() {
        if (opponentDistance() <= nearPositions) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOpponentWeaker() {
        return getStrengthDifference() > 0;
    }

    public boolean isOpponentMuchWeaker() {
        return getStrengthDifference() > WEAKER;
    }

    public boolean isOpponentStronger() {
        if (getStrengthDifference() < 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOpponentMuchStronger() {
        if (getStrengthDifference() < -4) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOpponentEven() {
        if (getStrengthDifference() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getStrengthDifference() {
        int strengthDif = getTotalStrength() - opponent.getTotalStrength();
        return strengthDif;
    }

    public int getTotalStrength() {
        int totalStrength;
        totalStrength = punchReach + kickReach + kickPower + punchPower;
        return totalStrength;
    }

    public void moveEast(int positionsToMove) {
        int newPosition = position + positionsToMove;
        if (!Arena.isPositionInArena(newPosition)) {
            position = Arena.getEastmostPosition();
        } else {
            position = newPosition;
        }
    }

    public void moveWest(int positionsToMove) {
        int newPosition = position - positionsToMove;
        if (!Arena.isPositionInArena(newPosition)) {
            position = Arena.getWestmostPosition();
        } else {
            position = newPosition;
        }
    }

    public boolean isAtArenaMargin() {
        if (position == Arena.getWestmostPosition() || position == Arena.getEastmostPosition()) {
            return true;
        } else {
            return false;
        }
    }

    public void resetAttacks() {
        punchLow = false;
        punchHigh = false;
        kickLow = false;
        kickHigh = false;
        blockLow = false;
        blockHigh = false;
    }

    public void resetMoves() {
        jump = false;
        crouch = false;
        stand = false;
    }

    public void setJump(boolean j) {
        jump = j;
    }

    public void setCrouch(boolean c) {
        crouch = c;
    }

    public void setStand(boolean s) {
        stand = s;
    }

    public boolean getJump() {
        return jump;
    }

    public boolean getCrouch() {
        return crouch;
    }

    public boolean getStand() {
        return stand;
    }

    public int getPunchPower() {
        return punchPower;
    }

    public int getPunchReach() {
        return punchReach;
    }

    public int getKickPower() {
        return kickPower;
    }

    public int getKickReach() {
        return kickReach;
    }

    public boolean getBlockLow() {
        return blockLow;
    }

    public boolean getBlockHigh() {
        return blockHigh;
    }

    public boolean opponentBlockHigh() {
        return opponent.getBlockHigh();
    }

    public boolean opponentBlockLow() {
        return opponent.getBlockLow();
    }

    public void log(String msg) {
        logger.log(msg);
    }

    public int getNearPositions() {
        return nearPositions;
    }

    public int getAwayPositions() {
        return awayPositions;
    }

    public boolean isOpponentWithinKickReach() {
        return opponentDistance() <= kickReach;
    }

    public boolean isOpponentWithinPunchReach() {
        return opponentDistance() <= punchReach;
    }

    public String getBotName() {
        return botName;
    }
}
