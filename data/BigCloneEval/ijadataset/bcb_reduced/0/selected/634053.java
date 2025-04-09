package saf.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import saf.structure.Attribute;
import saf.structure.FightAction;
import saf.structure.FightActionType;
import saf.structure.Fighter;
import saf.structure.Logical;
import saf.structure.LogicalClause;
import saf.structure.LogicalSimple;
import saf.structure.MoveAction;
import saf.structure.MoveActionType;
import saf.structure.Rule;

public class FighterState extends Subject {

    private int health;

    private double fighterX, step;

    private boolean actionPerform;

    private Fighter fighter;

    private StartingPosition startingPosition;

    private Rule currentRule;

    private MoveActionType currentMoveActionType;

    private FightActionType currentFightActionType;

    public FighterState(Fighter fighter, StartingPosition startingPosition) {
        switch(startingPosition) {
            case left:
                this.fighterX = 20;
                break;
            case right:
                this.fighterX = 80;
                break;
        }
        this.currentMoveActionType = MoveActionType.stand;
        this.fighter = fighter;
        this.startingPosition = startingPosition;
        this.health = 100;
        this.step = 0;
    }

    public void think(FighterState otherFighterState) {
        if (step <= 0) {
            setCurrentRule(otherFighterState);
            setCurrentMoveAction();
            setCurrentFightAction();
            step = 100;
        }
    }

    public void perform(FighterState otherFighterState) {
        doMoveAction(otherFighterState);
        doFightAction(otherFighterState);
        nextStep();
    }

    private double getSpeed() {
        int punchPower = fighter.getAttributePower(Attribute.punchPower);
        int kickPower = fighter.getAttributePower(Attribute.kickPower);
        int punchReach = fighter.getAttributePower(Attribute.punchReach);
        int kickReach = fighter.getAttributePower(Attribute.kickReach);
        double weight = (punchPower + kickPower) / 2;
        double height = (punchReach + kickReach) / 2;
        return (0.5 * (height - weight) + 5);
    }

    public double getFighterX() {
        return fighterX;
    }

    public void moveFighterX(double move) {
        setFighterX(getFighterX() + move);
    }

    public void setFighterX(double fighterX) {
        if (fighterX < 10) fighterX = 10;
        if (fighterX > 90) fighterX = 90;
        this.fighterX = fighterX;
        this.notifyObservers();
    }

    public int getHealth() {
        return health;
    }

    public void doDamage(int damage, FightActionType fightAction) {
        this.health = this.getHealth() - damageDone(fightAction, damage);
        this.notifyObservers();
    }

    public boolean fighterLost() {
        return this.health <= 0;
    }

    public Fighter getFighter() {
        return fighter;
    }

    public StartingPosition getStartingPosition() {
        return startingPosition;
    }

    public MoveActionType getCurrentMoveActionType() {
        return currentMoveActionType;
    }

    public FightActionType getCurrentFightActionType() {
        return currentFightActionType;
    }

    public boolean isActionPerform() {
        return actionPerform;
    }

    public void setActionPerform(boolean actionPerformed) {
        this.actionPerform = actionPerformed;
        this.notifyObservers();
    }

    public void nextStep() {
        step -= getSpeed();
    }

    private void setCurrentRule(FighterState otherFighterState) {
        Random randomGenerator = new Random();
        List<Rule> possible = getPossibleRules(otherFighterState);
        this.currentRule = possible.get(randomGenerator.nextInt(possible.size()));
    }

    public void setCurrentMoveAction() {
        Random randomGenerator = new Random();
        List<MoveAction> moveActions = currentRule.getMoveActions();
        currentMoveActionType = moveActions.get(randomGenerator.nextInt(moveActions.size())).getMoveActionType();
    }

    public void setCurrentFightAction() {
        Random randomGenerator = new Random();
        List<FightAction> fightActions = currentRule.getFightActions();
        currentFightActionType = fightActions.get(randomGenerator.nextInt(fightActions.size())).getFightActionType();
    }

    private List<Rule> getPossibleRules(FighterState otherFighterState) {
        List<Rule> possibleRules = new ArrayList<Rule>();
        for (Rule rule : fighter.getRules()) {
            if (isRulePossible(rule.getLogical(), otherFighterState)) possibleRules.add(rule);
        }
        return possibleRules;
    }

    private boolean isRulePossible(Logical logical, FighterState otherFighterState) {
        if (logical instanceof LogicalClause) return ((LogicalClause) logical).computeClause(isRulePossible(((LogicalClause) logical).getLeftHandSide(), otherFighterState), isRulePossible(((LogicalClause) logical).getLeftHandSide(), otherFighterState));
        switch(((LogicalSimple) logical).getCondition()) {
            case always:
                return true;
            case near:
                if (Math.abs(fighterX - otherFighterState.getFighterX()) <= Common.FAR_NEAR_THRESHOLD) return true;
                break;
            case far:
                if (Math.abs(fighterX - otherFighterState.getFighterX()) > Common.FAR_NEAR_THRESHOLD) return true;
                break;
            case much_stronger:
                if ((health - otherFighterState.getHealth()) > Common.MUCH_STRONGER_WEAKER_THRESHOLD) return true;
                break;
            case stronger:
                if (health > otherFighterState.getHealth()) return true;
                break;
            case even:
                if (health == otherFighterState.getHealth()) return true;
                break;
            case weaker:
                if (health < otherFighterState.getHealth()) return true;
                break;
            case much_weaker:
                if ((otherFighterState.getHealth() - health) > Common.MUCH_STRONGER_WEAKER_THRESHOLD) return true;
                break;
        }
        return false;
    }

    private void doMoveAction(FighterState otherFighterState) {
        switch(currentMoveActionType) {
            case walk_towards:
                if (Math.abs(fighterX - otherFighterState.getFighterX()) > 0) {
                    if (startingPosition == StartingPosition.left) {
                        moveFighterX(1);
                        break;
                    } else {
                        moveFighterX(-1);
                    }
                }
                break;
            case walk_away:
                if (Math.abs(fighterX - otherFighterState.getFighterX()) >= 0) {
                    if (startingPosition == StartingPosition.left) {
                        moveFighterX(-1);
                        break;
                    } else {
                        moveFighterX(1);
                    }
                }
                break;
            case run_towards:
                if (Math.abs(fighterX - otherFighterState.getFighterX()) > 1) {
                    if (getStartingPosition() == StartingPosition.left) {
                        moveFighterX(2);
                        break;
                    } else {
                        moveFighterX(-2);
                    }
                }
                break;
            case run_away:
                if (Math.abs(fighterX - otherFighterState.getFighterX()) >= 1) {
                    if (getStartingPosition() == StartingPosition.left) {
                        moveFighterX(-2);
                        break;
                    } else {
                        moveFighterX(2);
                    }
                }
                break;
        }
    }

    private void doFightAction(FighterState otherFighterState) {
        switch(currentFightActionType) {
            case punch_low:
                if (actionPerform && (Math.abs(fighterX - otherFighterState.getFighterX()) <= fighter.getAttributePower(Attribute.punchReach))) otherFighterState.doDamage(fighter.getAttributePower(Attribute.punchPower), FightActionType.punch_low);
                break;
            case punch_high:
                if (actionPerform && (Math.abs(fighterX - otherFighterState.getFighterX()) <= fighter.getAttributePower(Attribute.punchReach))) otherFighterState.doDamage(fighter.getAttributePower(Attribute.punchPower), FightActionType.punch_high);
                break;
            case kick_low:
                if (actionPerform && (Math.abs(fighterX - otherFighterState.getFighterX()) <= fighter.getAttributePower(Attribute.kickReach))) otherFighterState.doDamage(fighter.getAttributePower(Attribute.kickPower), FightActionType.kick_low);
                break;
            case kick_high:
                if (actionPerform && (Math.abs(fighterX - otherFighterState.getFighterX()) <= fighter.getAttributePower(Attribute.kickReach))) otherFighterState.doDamage(fighter.getAttributePower(Attribute.kickPower), FightActionType.kick_high);
                break;
        }
        setActionPerform(step > 50 && step < 70);
    }

    private int damageDone(FightActionType fightActionType, int damage) {
        switch(fightActionType) {
            case kick_low:
            case punch_low:
                if (actionPerform && currentMoveActionType == MoveActionType.jump) return 0;
                if (actionPerform && currentFightActionType == FightActionType.block_low) return Math.round(damage / 5);
                break;
            case kick_high:
            case punch_high:
                if (actionPerform && currentMoveActionType == MoveActionType.crouch) return 0;
                if (actionPerform && currentFightActionType == FightActionType.block_high) return Math.round(damage / 5);
                break;
        }
        return damage;
    }
}
