package pelletQuest.entities;

import pelletQuest.map.GameMap;
import pelletQuest.resources.*;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.util.Log;
import java.util.ArrayList;

public class Maxim extends Entity {

    public static boolean canStop = false;

    private static Maxim me = null;

    public static Maxim getMaxim() {
        return me;
    }

    public static final String NAME = "player";

    public String getName() {
        return NAME;
    }

    private int pellets, nextLevel, lastLevel;

    private int level;

    private int health, maxHealth;

    private int state = 0;

    private int invincibleTimer = 0;

    private boolean unrestrictedMovement = false;

    private GameMap lastMap;

    private String transition = "";

    private int transitionTimer = 0;

    private int maxTransitionTimer = 1;

    private boolean[] direction;

    private boolean[] command;

    private ArrayList<String> inventory = new ArrayList<String>();

    private ArrayList<String> inventoryGraphics = new ArrayList<String>();

    private String spriteset;

    private Vector2f drawFrom;

    private String[] walkableTerrains;

    private int renderFrame, frameClock, frameClockMax;

    private int moveTimer, maxMoveTimer;

    private float speed;

    private int speedTimer;

    public Maxim(GameMap map, String[] walkableTerrains, int xStart, int yStart, String playerGraphics) {
        super(map, new Rectangle(xStart * 16, yStart * 16, 15, 15));
        this.walkableTerrains = walkableTerrains;
        this.spriteset = playerGraphics;
        drawFrom = new Vector2f(0, -6);
        pellets = 0;
        lastLevel = 0;
        nextLevel = 100;
        level = 1;
        health = maxHealth = 3;
        direction = new boolean[4];
        command = new boolean[4];
        canLeaveZone = true;
        canLeaveMap = true;
        frameClockMax = 75;
        frameClock = 0;
        renderFrame = 0;
        maxMoveTimer = 13;
        moveTimer = 0;
        speed = 1;
        speedTimer = -1;
        lastMap = map;
        me = this;
    }

    public void update(int delta) {
        transitionTimer -= delta;
        if (transitionTimer <= 0) {
            moveTimer -= delta;
            invincibleTimer -= delta;
            super.update(delta);
            if (speedTimer > 0) {
                speedTimer -= delta;
                if (speedTimer <= 0) {
                    speed = 1;
                }
            }
            frameClock -= delta;
            if (frameClock < 0) {
                frameClock = (int) (frameClockMax / speed);
                renderFrame++;
                if (renderFrame >= GraphicsManager.getSpriteset(spriteset).getLengthOfSequence(lastDirection)) {
                    renderFrame = 1;
                }
                if (renderFrame >= GraphicsManager.getSpriteset(spriteset).getLengthOfSequence(lastDirection)) {
                    renderFrame = 0;
                }
            }
            if (moveTimer < 0) {
                if ((box.getX() % 16 == 0 && box.getY() % 16 == 0) || (moveDirection.x + moveDirection.y == 0)) {
                    if (unrestrictedMovement && getCollisions(new Vector2f(0, 0)).size() == 0) {
                        unrestrictedMovement = false;
                    }
                    if (canStop) {
                        if (direction[0] && canMove(new Vector2f(0, -1))) {
                            moveDirection = new Vector2f(0, -1);
                        } else if (direction[1] && canMove(new Vector2f(-1, 0))) {
                            moveDirection = new Vector2f(-1, 0);
                        } else if (direction[2] && canMove(new Vector2f(0, 1))) {
                            moveDirection = new Vector2f(0, 1);
                        } else if (direction[3] && canMove(new Vector2f(1, 0))) {
                            moveDirection = new Vector2f(1, 0);
                        } else {
                            moveDirection = new Vector2f(0, 0);
                        }
                    } else {
                        if (command[0] && canMove(new Vector2f(0, -1))) {
                            moveDirection = new Vector2f(0, -1);
                            command = new boolean[4];
                            command[0] = true;
                        } else if (command[1] && canMove(new Vector2f(-1, 0))) {
                            moveDirection = new Vector2f(-1, 0);
                            command = new boolean[4];
                            command[1] = true;
                        } else if (command[2] && canMove(new Vector2f(0, 1))) {
                            moveDirection = new Vector2f(0, 1);
                            command = new boolean[4];
                            command[2] = true;
                        } else if (command[3] && canMove(new Vector2f(1, 0))) {
                            moveDirection = new Vector2f(1, 0);
                            command = new boolean[4];
                            command[3] = true;
                        }
                    }
                    if (moveDirection.x + moveDirection.y != 0) {
                    }
                }
            }
            while (moveTimer < 0) {
                if (moveDirection.x + moveDirection.y != 0) {
                    if (tryMove(moveDirection)) {
                        lastDirection = moveDirection;
                    } else {
                        moveDirection = new Vector2f(0, 0);
                    }
                } else {
                    tryMove(moveDirection);
                }
                moveTimer += (maxMoveTimer / speed);
            }
        }
    }

    public void drawMaxim(Graphics g, int x, int y) {
        int frame = renderFrame;
        if (moveDirection.x == 0 && moveDirection.y == 0) {
            frame = 0;
        }
        GraphicsManager.getSpriteset(spriteset).render(g, (int) (x + box.getX() + drawFrom.x), (int) (y + box.getY() + drawFrom.y), lastDirection, 0, frame);
    }

    public void render(Graphics g, int x, int y) {
        if ((transition.startsWith("dropDown") && transitionTimer > 0) == false) {
            if (invincibleTimer > 0) {
                if (state == 2) {
                    float percentage = 1;
                    if (invincibleTimer < 1001) {
                        percentage = invincibleTimer / 1000f;
                    }
                    int phase = 0;
                    if (percentage < 0.3) {
                        phase = 1;
                    }
                    renderSparkles(percentage, x - 4, y - 6);
                    renderSparkles(percentage, x + 4, y - 6);
                    GraphicsManager.getSpriteFromSequence("levelupShadow", phase).draw(x + box.getX() + drawFrom.x, y + box.getY() + drawFrom.y + 6);
                    drawMaxim(g, x, y);
                    GraphicsManager.getSpriteFromSequence("levelupHalo", phase).draw(x + box.getX() + drawFrom.x, y + box.getY() + drawFrom.y + 6);
                    renderSparkles(percentage, x, y - 2);
                } else if (state == 3) {
                    int yAdjust = -Math.abs((invincibleTimer / 50) % 11 - 5) + 5;
                    if (invincibleTimer > 800 || (invincibleTimer / 10) % 2 == 0) {
                        renderSparkles(0.36f + (float) ((invincibleTimer / 50) % 10) / 100, x - 4, y - 4 + yAdjust);
                        renderSparkles(0.36f + (float) ((invincibleTimer / 50) % 10) / 100, x + 4, y - 4 + yAdjust);
                    }
                    renderShadow(x, y);
                    drawMaxim(g, x, y);
                    if (invincibleTimer > 800 || (invincibleTimer / 10) % 2 == 0) {
                        renderSparkles(0.36f + (float) ((invincibleTimer / 50) % 10) / 100, x, y - 2 + yAdjust);
                    }
                } else if (state != 1 || (invincibleTimer / 10) % 2 == 0) {
                    renderShadow(x, y);
                    drawMaxim(g, x, y);
                }
            } else {
                renderShadow(x, y);
                drawMaxim(g, x, y);
            }
        }
        if (pelletQuest.main.PelletQuestGame.debug) {
            g.drawRect(box.getX() + x, box.getY() + y, box.getWidth(), box.getHeight());
        }
    }

    private void renderShadow(int x, int y) {
        GraphicsManager.getSprite("shadow").draw(x + box.getX() + drawFrom.x, y + box.getY() + drawFrom.y + 6);
    }

    private void renderSparkles(float percentage, int x, int y) {
        int phase = 0;
        if (percentage < 0.4) {
            phase = 1;
        } else if (percentage < 0.3) {
            phase = 2;
        }
        if (percentage < 0.2) {
            phase = 3;
        }
        GraphicsManager.getSpriteFromSequence("sparkles", phase).draw(x + box.getX() + drawFrom.x, y + box.getY() + drawFrom.y + (10 * percentage));
    }

    public void renderDropDown(Graphics g, int x, int y, double height, float percentage) {
        double falling = 0;
        if (percentage < 0.7) {
            percentage = percentage * (10f / 7f);
            falling = Math.pow((Math.sqrt(height) * percentage), 2) - height;
        } else if (percentage < 0.9) {
            percentage = (float) (percentage - 0.7) * (10f / 1f);
            falling = Math.pow((Math.sqrt(height / 17) * (percentage - 1f)), 2) - (height / 17);
        }
        getCurrentMap().render(g, x, y, false);
        GraphicsManager.getSprite("shadow").draw(x + box.getX() + drawFrom.x, y + box.getY() + drawFrom.y + 6);
        drawMaxim(g, x, y + (int) falling);
    }

    public boolean bump(Entity other) {
        getHurt(other.getDamage());
        if (!(other.canMoveThroughWhenInvincible() && invincibleTimer > 0) && !(other.canMoveOffWhenInvincible && unrestrictedMovement)) {
            if (other.isSolid()) {
                moveDirection = new Vector2f(0, 0);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void getBumped(Entity other) {
        getHurt(other.getDamage());
    }

    public void getHurt(int damage) {
        if (damage > 0 && invincibleTimer < 0) {
            health -= damage;
            unrestrictedMovement = true;
            AudioManager.self.playSound("ouch");
            invincibleTimer = 2000;
            state = 1;
        }
    }

    public boolean teleport(String transition, int transitionTime, Vector2f targetPos, String targetName, Vector2f finalDirection) {
        GameMap originalMap = map;
        if (super.teleport(transition, transitionTime, targetPos, targetName, finalDirection)) {
            this.transition = transition;
            this.maxTransitionTimer = this.transitionTimer = transitionTime;
            lastMap = originalMap;
            lastMap.leave();
            if (finalDirection != null) {
                moveDirection = new Vector2f(0, 0);
                direction = new boolean[4];
                command = new boolean[4];
                if (finalDirection.x > 0) {
                    keyPressed(205);
                } else if (finalDirection.x < 0) {
                    keyPressed(203);
                } else if (finalDirection.y > 0) {
                    keyPressed(208);
                } else if (finalDirection.y < 0) {
                    keyPressed(200);
                }
            }
            map.enter();
            return true;
        } else {
            return false;
        }
    }

    public String[] getInventory() {
        String[] result = new String[inventory.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = inventory.get(i);
        }
        return result;
    }

    public boolean hasItem(String item) {
        for (String i : inventory) {
            if (i.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public String[] getInventoryGraphics() {
        String[] result = new String[inventoryGraphics.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = inventoryGraphics.get(i);
        }
        return result;
    }

    public void removeItem(String item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).equals(item)) {
                inventory.remove(i);
                inventoryGraphics.remove(i);
                return;
            }
        }
        Log.warn(" Could not find item to remove \"" + item + "\"!!");
    }

    public void addItem(String name, String sprite) {
        inventory.add(name);
        inventoryGraphics.add(sprite);
    }

    public void transition(String type, int time) {
        transition = type;
        transitionTimer = maxTransitionTimer = time;
    }

    public void makeInvincible(int duration) {
        if (state != 2) {
            state = 3;
        }
        invincibleTimer = duration;
    }

    public void speedUp(float newSpeed, int duration) {
        speed = newSpeed;
        speedTimer = duration;
    }

    public void fullHeal() {
        health = maxHealth;
    }

    public void addPellet() {
        pellets++;
        checkForLevelup();
    }

    private void checkForLevelup() {
        if (pellets - lastLevel >= nextLevel) {
            level++;
            lastLevel += nextLevel;
            nextLevel = fib(level + 1) * 100;
            Log.info("Level-up! Pellets needed for next level: " + nextLevel);
            fullHeal();
            invincibleTimer = 1000;
            state = 2;
            AudioManager.self.playSound("levelup");
        }
    }

    public void keyPressed(int key) {
        if (key == 200 || key == 17) {
            direction = new boolean[4];
            direction[0] = true;
            command = new boolean[4];
            command[0] = true;
        } else if (key == 203 || key == 30) {
            direction = new boolean[4];
            direction[1] = true;
            command = new boolean[4];
            command[1] = true;
        } else if (key == 208 || key == 31) {
            direction = new boolean[4];
            direction[2] = true;
            command = new boolean[4];
            command[2] = true;
        } else if (key == 205 || key == 32) {
            direction = new boolean[4];
            direction[3] = true;
            command = new boolean[4];
            command[3] = true;
        }
    }

    public void keyReleased(int key) {
        if (key == 200 || key == 17) {
            direction[0] = false;
        } else if (key == 203 || key == 30) {
            direction[1] = false;
        } else if (key == 208 || key == 31) {
            direction[2] = false;
        } else if (key == 205 || key == 32) {
            direction[3] = false;
        }
    }

    public Vector2f getKeyDirection() {
        if (direction[0] && canMove(new Vector2f(0, -1))) {
            return new Vector2f(0, -1);
        } else if (direction[1] && canMove(new Vector2f(-1, 0))) {
            return new Vector2f(-1, 0);
        } else if (direction[2] && canMove(new Vector2f(0, 1))) {
            return new Vector2f(0, 1);
        } else if (direction[3] && canMove(new Vector2f(1, 0))) {
            return new Vector2f(1, 0);
        } else {
            return new Vector2f(0, 0);
        }
    }

    public int getLayer() {
        return 2;
    }

    public boolean isSolid() {
        return true;
    }

    public GameMap getCurrentMap() {
        return map;
    }

    public int getPellets() {
        return pellets;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean flashRed() {
        return invincibleTimer > 1980 && state == 1;
    }

    public boolean isInTransition() {
        return transitionTimer > 0;
    }

    public GameMap getLastMap() {
        return lastMap;
    }

    public String getTransition() {
        return transition;
    }

    public int getTransitionTime() {
        return transitionTimer;
    }

    public float getTransitionPercent() {
        return (float) (maxTransitionTimer - transitionTimer) / maxTransitionTimer;
    }

    public float getLevelupPercent() {
        return (float) (pellets - lastLevel) / nextLevel;
    }

    public int getLevel() {
        return level;
    }

    public int getPelletsNeeded() {
        return lastLevel + nextLevel;
    }

    public String[] getPassableTerrains() {
        return walkableTerrains;
    }

    public void setSpriteset(String newSpriteset) {
        spriteset = newSpriteset;
    }

    private static int fib(int x) {
        if (x < 3) {
            return 1;
        } else {
            return fib(x - 1) + fib(x - 2);
        }
    }
}
