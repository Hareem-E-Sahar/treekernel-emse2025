package nl.uva.saf.simulation;

import java.awt.Dimension;
import java.util.HashMap;
import nl.uva.saf.fdl.CharacteristicExtractor;
import nl.uva.saf.fdl.NameExtractor;
import nl.uva.saf.fdl.ast.ITreeNode;
import nl.uva.saf.fdl.types.CharacteristicType;
import nl.uva.saf.fdl.types.FightActionType;
import nl.uva.saf.fdl.types.MoveActionType;

public class FighterBot {

    private HashMap<CharacteristicType, Integer> characteristics;

    protected final int default_value = 5;

    private volatile FightActionType fightAction = FightActionType.unknown;

    private final ITreeNode fighterNode;

    private volatile int health;

    private volatile MoveActionType moveAction = MoveActionType.unknown;

    private String name;

    private int nextTurn;

    private volatile Vector2d origin;

    private volatile Vector2d position;

    private Vector2d spawnPosition;

    private int speed;

    public FighterBot(ITreeNode fighter) {
        fighterNode = fighter;
        nextTurn = 0;
        position = new Vector2d();
        spawnPosition = new Vector2d();
        origin = new Vector2d(100, 150);
        health = 100;
        if (fighterNode != null) {
            name = NameExtractor.getName(fighterNode);
            characteristics = CharacteristicExtractor.getCharacteristics(fighterNode);
        } else {
            name = "";
            characteristics = new HashMap<CharacteristicType, Integer>();
        }
        calculateSpeed();
    }

    private void calculateSpeed() {
        int punchPower = getAttribute(CharacteristicType.punchPower);
        int kickPower = getAttribute(CharacteristicType.kickPower);
        int punchReach = getAttribute(CharacteristicType.punchReach);
        int kickReach = getAttribute(CharacteristicType.kickReach);
        double weight = (punchPower + kickPower) / 2;
        double height = (punchReach + kickReach) / 2;
        speed = (int) Math.abs(0.5 * (height - weight));
    }

    public void containInDimension(Dimension dimension) {
        if (position.x < 0) {
            position.x = 0;
        } else if (position.x > dimension.width - 1) {
            position.x = dimension.width - 1;
        }
        if (position.y < 0) {
            position.y = 0;
        } else if (position.y > dimension.height - 1) {
            position.y = dimension.height - 1;
        }
    }

    /**
	 * Deducts an amount of hitpoints from the bot. A bots health cannot get
	 * lower than 0;
	 * 
	 * @param hitpoints
	 *            The amount of hitpoints to deduct. If this value is zero or
	 *            lower, no health will be deducted.
	 */
    public void deductHealth(int hitpoints) {
        if (hitpoints > 0) {
            health -= hitpoints;
        }
        if (health < 0) {
            health = 0;
        }
    }

    public int getAttribute(CharacteristicType name) {
        Integer value = characteristics.get(name);
        return value == null ? default_value : value;
    }

    public FightActionType getFightAction() {
        return fightAction;
    }

    public ITreeNode getFighterNode() {
        return fighterNode;
    }

    public int getHealth() {
        return health;
    }

    public MoveActionType getMoveAction() {
        return moveAction;
    }

    public String getName() {
        return name;
    }

    public int getNextTurn() {
        return nextTurn;
    }

    public Vector2d getOrigin() {
        return origin;
    }

    public Vector2d getPosition() {
        return position;
    }

    public Vector2d getSpawnPosition() {
        return spawnPosition;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isCrouching() {
        return moveAction == MoveActionType.crouch;
    }

    public boolean isJumping() {
        return moveAction == MoveActionType.jump;
    }

    public void resetHealth() {
        health = 100;
    }

    public void setAttribute(CharacteristicType name, int value) {
        characteristics.put(name, value);
        calculateSpeed();
    }

    public void setFightAction(FightActionType fightAction) {
        this.fightAction = fightAction;
    }

    public void setMoveAction(MoveActionType moveAction) {
        this.moveAction = moveAction;
    }

    public void setNextTurn(int turns) {
        if (turns >= 0) {
            this.nextTurn = turns;
        }
    }

    public void setOrigin(Vector2d origin) {
        this.origin = origin;
    }

    public void setPosition(Vector2d position) {
        this.position = position;
    }

    public void setSpawnPosition(Vector2d spawnPosition) {
        this.spawnPosition = spawnPosition;
    }
}
