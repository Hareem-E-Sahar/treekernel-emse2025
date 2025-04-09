package ch.idsia.benchmark.mario.engine.level;

import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.tools.MarioAIOptions;
import ch.idsia.tools.RandomCreatureGenerator;
import ch.idsia.utils.ErrorCodes;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

/**
 * This class is simple to use. Just call <b>createLevel</b> method with params:
 * <ul>
 * MarioAIOptions args, that contains: ... TODO:TASK:[M]
 * <p/>
 * <li>length -- length of the level in cells. One cell is 16 pixels long</li>
 * <li>height -- height of the level in cells. One cell is 16 pixels long </li>
 * <li>seed -- use this param to make a globalRandom level.
 * On different machines with the same seed param there will be one level</li>
 * <li>levelDifficulty -- use this param to change difficult of the level.
 * On different machines with the same seed param there will be one level</li>
 * <li>levelType -- levelType of the level. One of Overground, Underground, Castle.</li>
 * </ul>
 *
 * @see #TYPE_OVERGROUND
 * @see #TYPE_UNDERGROUND
 * @see #TYPE_CASTLE
 */
public class LevelGenerator {

    public static final int TYPE_OVERGROUND = 0;

    public static final int TYPE_UNDERGROUND = 1;

    public static final int TYPE_CASTLE = 2;

    public static final int DEFAULT_FLOOR = -1;

    public static final int LevelLengthMinThreshold = 50;

    private static boolean isFlatLevel;

    private static int length;

    private static int height;

    private static Level level;

    private static Random globalRandom = new Random(0);

    private static Random ceilingRandom = new Random(0);

    private static RandomCreatureGenerator creaturesRandom = new RandomCreatureGenerator(0, "", 0);

    public static Random dxRnd = new Random(0);

    private static final int ODDS_STRAIGHT = 0;

    private static final int ODDS_HILL_STRAIGHT = 1;

    private static final int ODDS_TUBES = 2;

    private static final int ODDS_GAPS = 3;

    private static final int ODDS_CANNONS = 4;

    private static final int ODDS_DEAD_ENDS = 5;

    private static int[] odds = new int[6];

    private static int totalOdds;

    private static int levelDifficulty;

    private static int levelType;

    private static int levelSeed;

    private static boolean isLadder = false;

    private static final int ANY_HEIGHT = -1;

    private static final int INFINITE_FLOOR_HEIGHT = Integer.MAX_VALUE;

    static Level.objCounters counters = new Level.objCounters();

    private LevelGenerator() {
    }

    private static void loadLevel(String filePath) {
        try {
            if (filePath.equals("")) {
                System.err.println("[MarioAI ERROR] : level file path is empty; exiting...");
                System.exit(ErrorCodes.FILE_NAME_OR_LOAD_PROBLEM);
            }
            level = Level.load(new ObjectInputStream(new FileInputStream(filePath)));
        } catch (IOException e) {
            System.err.println("[MarioAI EXCEPTION] : failed while trying to loadAgent " + filePath);
            System.exit(ErrorCodes.FILE_NAME_OR_LOAD_PROBLEM);
        } catch (ClassNotFoundException e) {
            System.err.println("[MarioAI EXCEPTION] : class not found in " + filePath);
            System.exit(ErrorCodes.FILE_NAME_OR_LOAD_PROBLEM);
        }
    }

    public static Level createLevel(MarioAIOptions args) {
        levelType = args.getLevelType();
        try {
            levelSeed = args.getLevelRandSeed() + levelType;
        } catch (Exception e) {
            loadLevel(args.getParameterValue("-ls"));
            return level;
        }
        length = args.getLevelLength();
        height = args.getLevelHeight();
        if (height < 15) {
            System.err.println("[Mario AI WARNING] : Level height changed to minimal allowed value 15");
            height = 15;
        }
        isFlatLevel = args.isFlatLevel();
        counters.reset(args);
        levelDifficulty = args.getLevelDifficulty();
        odds[ODDS_STRAIGHT] = 20;
        odds[ODDS_HILL_STRAIGHT] = 1;
        odds[ODDS_TUBES] = 2 + 1 * levelDifficulty;
        odds[ODDS_GAPS] = 3 * levelDifficulty;
        odds[ODDS_CANNONS] = -10 + 5 * levelDifficulty;
        odds[ODDS_DEAD_ENDS] = 2 + 2 * levelDifficulty;
        if (levelType != LevelGenerator.TYPE_OVERGROUND) odds[ODDS_HILL_STRAIGHT] = 0;
        totalOdds = 0;
        for (int i = 0; i < odds.length; i++) {
            if (odds[i] < 0) odds[i] = 0;
            totalOdds += odds[i];
            odds[i] = totalOdds - odds[i];
        }
        if (totalOdds <= 0) {
            System.err.println("[Mario AI SURPRISE] : UNEXPECTED level will be generated");
            totalOdds = 1;
        }
        level = new Level(length, height);
        globalRandom.setSeed(levelSeed);
        creaturesRandom.setSeed(levelSeed, args.getEnemies(), levelDifficulty);
        ceilingRandom.setSeed(levelSeed);
        dxRnd.setSeed(levelSeed);
        isLadder = args.isLevelLadder();
        int currentLength = 0;
        int floor = DEFAULT_FLOOR;
        if (isFlatLevel) floor = height - 1 - globalRandom.nextInt(4);
        currentLength += buildStraight(0, level.length, true, floor, INFINITE_FLOOR_HEIGHT);
        while (currentLength < level.length - 10) {
            currentLength += buildZone(currentLength, level.length - currentLength, ANY_HEIGHT, floor, INFINITE_FLOOR_HEIGHT);
        }
        if (!isFlatLevel) floor = height - 1 - globalRandom.nextInt(4);
        level.xExit = args.getExitX();
        level.yExit = args.getExitY();
        if (level.xExit == 0) level.xExit = level.length - 1;
        if (level.yExit == 0) level.yExit = floor - 1;
        level.randomSeed = levelSeed;
        level.type = levelType;
        level.difficulty = levelDifficulty;
        for (int x = currentLength; x < level.length; x++) {
            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                }
            }
        }
        fixWalls();
        setPrincess(level.xExit, level.yExit);
        level.counters = counters;
        return level;
    }

    private static void setPrincess(int x, int y) {
        level.setSpriteTemplate(x, y, new SpriteTemplate(Sprite.KIND_PRINCESS));
        level.setBlock(x, y, (byte) (15 + 15 * 16));
    }

    private static int buildZone(int x, int maxLength, int maxHeight, int floor, int floorHeight) {
        int t = globalRandom.nextInt(totalOdds);
        int type = 0;
        for (int i = 0; i < odds.length; i++) {
            if (odds[i] <= t) {
                type = i;
            }
        }
        int length = 0;
        switch(type) {
            case ODDS_STRAIGHT:
                length = buildStraight(x, maxLength, false, floor, floorHeight);
                break;
            case ODDS_HILL_STRAIGHT:
                if (floor == DEFAULT_FLOOR && counters.hillStraightCount < counters.totalHillStraight) {
                    counters.hillStraightCount++;
                    length = buildHill(x, true, maxLength, floor, false);
                } else length = 0;
                break;
            case ODDS_TUBES:
                if (counters.tubesCount < counters.totalTubes) length = buildTubes(x, maxLength, maxHeight, floor, floorHeight); else length = 0;
                break;
            case ODDS_GAPS:
                if ((floor > 2 || floor == ANY_HEIGHT) && (counters.gapsCount < counters.totalGaps)) {
                    counters.gapsCount++;
                    length = buildGap(x, maxLength, maxHeight, floor, floorHeight);
                } else length = 0;
                break;
            case ODDS_CANNONS:
                if (counters.cannonsCount < counters.totalCannons) length = buildCannons(x, maxLength, maxHeight, floor, floorHeight); else length = 0;
                break;
            case ODDS_DEAD_ENDS:
                {
                    if (floor == DEFAULT_FLOOR && counters.deadEndsCount < counters.totalDeadEnds) {
                        counters.deadEndsCount++;
                        length = buildDeadEnds(x, maxLength);
                    }
                }
        }
        int crCount = 0;
        for (int yy = level.height; yy > 0; yy--) if (level.getBlock(x, yy) == 0 && creaturesRandom.nextInt(levelDifficulty + 1) + 1 > (levelDifficulty + 1) / 2 && crCount < levelDifficulty + 1 && level.getSpriteTemplate(x, yy) == null) {
            addEnemy(x, yy);
            ++crCount;
        }
        if (levelType > 0) buildCeiling(x, length);
        return length;
    }

    private static void buildCeiling(int x0, int length) {
        int maxCeilingHeight = 3;
        int ceilingLength = length;
        if (ceilingLength < 2) return;
        int len = 0;
        while (len < ceilingLength) {
            int sectionLength = ceilingRandom.nextInt(2) + 2;
            if (sectionLength > ceilingLength) sectionLength = ceilingLength;
            int height = ceilingRandom.nextInt(maxCeilingHeight) + 1;
            for (int i = 0; i < sectionLength; i++) {
                for (int j = 0; j < height; j++) level.setBlock(x0 + len + i, j, (byte) (1 + 9 * 16));
            }
            len += sectionLength;
        }
    }

    private static void addEnemy(int x, int y) {
        if (!creaturesRandom.canAdd()) return;
        int dx = (int) dxRnd.nextGaussian();
        int creatureKind = creaturesRandom.nextCreature();
        if (creatureKind != Sprite.KIND_UNDEF) {
            if (level.setSpriteTemplate(x + dx, y, new SpriteTemplate(creatureKind))) ++counters.creatures; else creaturesRandom.increaseLastCreature();
        }
    }

    private static int buildDeadEnds(int x0, int maxLength) {
        int floor = height - 2 - globalRandom.nextInt(2);
        int length = 0;
        int preDeadEndLength = 7 + globalRandom.nextInt(10);
        int rHeight = floor - 1;
        int separatorY = 3 + globalRandom.nextInt(rHeight - 7);
        length += buildStraight(x0, preDeadEndLength, true, floor, INFINITE_FLOOR_HEIGHT);
        if (globalRandom.nextInt(3) == 0 && isLadder) {
            int ladderX = x0 + globalRandom.nextInt(length - 1) + 1;
            if (ladderX > x0 + length) ladderX = x0 + length;
            buildLadder(ladderX, floor, floor - separatorY);
        } else buildBlocks(x0, x0 + preDeadEndLength, floor, true, 0, 0, true, true);
        globalRandom.nextInt();
        int k = globalRandom.nextInt(5);
        boolean direction = globalRandom.nextInt(k + 1) != 1;
        int separatorHeight = 2 + globalRandom.nextInt(2);
        int nx = x0 + length;
        int depth = globalRandom.nextInt(levelDifficulty + 1) + 2 * (1 + levelDifficulty);
        if (depth + length > maxLength) {
            while (depth + length > maxLength - 1) {
                depth--;
            }
        }
        int tLength = 0;
        int bSpace = floor - (separatorY + separatorHeight);
        if (bSpace < 4) {
            while (bSpace < 4) {
                separatorY -= 1;
                bSpace = floor - (separatorY + separatorHeight);
            }
        }
        int wallWidth = 2 + globalRandom.nextInt(3);
        while (tLength < depth) {
            tLength += buildZone(nx + tLength, depth - tLength, separatorY - 1, separatorY, separatorHeight);
        }
        tLength = 0;
        while (tLength < depth) {
            tLength += buildZone(nx + tLength, depth - tLength, bSpace, floor, INFINITE_FLOOR_HEIGHT);
        }
        boolean wallFromBlocks = false;
        for (int x = nx; x < nx + depth; x++) {
            for (int y = 0; y < height; y++) {
                if (x - nx >= depth - wallWidth) {
                    if (direction) {
                        if (y <= separatorY) {
                            if (wallFromBlocks) level.setBlock(x, y, (byte) (0 + 1 * 16)); else level.setBlock(x, y, (byte) (1 + 9 * 16));
                        }
                    } else {
                        if (y >= separatorY) {
                            if (wallFromBlocks) level.setBlock(x, y, (byte) (0 + 1 * 16)); else level.setBlock(x, y, (byte) (1 + 9 * 16));
                        }
                    }
                }
            }
        }
        return length + tLength;
    }

    private static void buildLadder(int x0, int floor, int maxHeight) {
        int ladderHeight = globalRandom.nextInt(height);
        if (ladderHeight > maxHeight && maxHeight != ANY_HEIGHT) {
            ladderHeight = maxHeight;
        }
        if (ladderHeight < 4) return;
        for (int y = floor, i = 0; i < ladderHeight - 1; y--, i++) level.setBlock(x0, y - 1, (byte) (13 + 3 * 16));
        level.setBlock(x0, floor - ladderHeight, (byte) (13 + 5 * 16));
    }

    private static int buildGap(int xo, int maxLength, int maxHeight, int vfloor, int floorHeight) {
        int gs = globalRandom.nextInt(5) + 2;
        int gl = globalRandom.nextInt(levelDifficulty) + levelDifficulty > 7 ? 10 : 3;
        int length = gs * 2 + gl;
        if (length > maxLength) length = maxLength;
        boolean hasStairs = globalRandom.nextInt(3) == 0;
        if (isFlatLevel || (maxHeight <= 5 && maxHeight != ANY_HEIGHT)) {
            hasStairs = false;
        }
        int floor = vfloor;
        if (vfloor == DEFAULT_FLOOR && !isFlatLevel) {
            floor = height - 1 - globalRandom.nextInt(4);
        } else {
            floor++;
            if (floor > 1) {
                floor -= 1;
            }
        }
        if (floorHeight == INFINITE_FLOOR_HEIGHT) {
            floorHeight = height - floor;
        }
        for (int x = xo; x < xo + length; x++) {
            if (x < xo + gs || x > xo + length - gs - 1) {
                for (int y = 0; y < height; y++) {
                    if (y >= floor && y <= floor + floorHeight) level.setBlock(x, y, (byte) (1 + 9 * 16)); else if (hasStairs) {
                        if (x < xo + gs) {
                            if (y >= floor - (x - xo) + 1 && y <= floor + floorHeight) level.setBlock(x, y, (byte) (9 + 0 * 16));
                        } else if (y >= floor - ((xo + length) - x) + 2 && y <= floor + floorHeight) level.setBlock(x, y, (byte) (9 + 0 * 16));
                    }
                }
            }
        }
        if (gl > 8) {
            buildHill(xo + gs + globalRandom.nextInt(Math.abs((gl - 4)) / 2 + 1), false, 3, floor, true);
        }
        return length;
    }

    private static int buildCannons(int xo, int maxLength, int maxHeight, int vfloor, int floorHeight) {
        int maxCannonHeight = 0;
        int length = globalRandom.nextInt(10) + 2;
        if (length > maxLength) length = maxLength;
        int floor = vfloor;
        if (vfloor == DEFAULT_FLOOR) {
            floor = height - 1 - globalRandom.nextInt(4);
        } else {
            globalRandom.nextInt();
        }
        if (floorHeight == INFINITE_FLOOR_HEIGHT) {
            floorHeight = height - floor;
        }
        int oldXCannon = -1;
        int xCannon = xo + 1 + globalRandom.nextInt(4);
        for (int x = xo; x < xo + length; x++) {
            if (x > xCannon) {
                xCannon += 2 + globalRandom.nextInt(4);
                counters.cannonsCount++;
            }
            if (xCannon == xo + length - 1) {
                xCannon += 10;
            }
            int cannonHeight = floor - globalRandom.nextInt(3) - 1;
            if (maxHeight != ANY_HEIGHT) {
                if (floor - cannonHeight >= maxHeight) {
                    if (maxHeight > 4) {
                        maxHeight = 4;
                    }
                    while (floor - cannonHeight > maxHeight) {
                        cannonHeight++;
                    }
                }
                if (cannonHeight > maxCannonHeight) maxCannonHeight = cannonHeight;
            }
            for (int y = 0; y < height; y++) {
                if (y >= floor && y <= floor + floorHeight) {
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                } else if (counters.cannonsCount <= counters.totalCannons) {
                    if (x == xCannon && y >= cannonHeight && y <= floor) {
                        if (y == cannonHeight) {
                            if (oldXCannon != -1 && creaturesRandom.nextInt(35) > levelDifficulty + 1) {
                            }
                            oldXCannon = x;
                            level.setBlock(x, y, (byte) (14 + 0 * 16));
                        } else if (y == cannonHeight + 1) {
                            level.setBlock(x, y, (byte) (14 + 1 * 16));
                        } else {
                            level.setBlock(x, y, (byte) (14 + 2 * 16));
                        }
                    }
                }
            }
        }
        if (globalRandom.nextBoolean()) buildBlocks(xo, xo + length, floor - maxCannonHeight - 2, false, 0, 0, false, false);
        return length;
    }

    private static int buildHill(int x0, boolean withStraight, int maxLength, int vfloor, boolean isInGap) {
        int length = globalRandom.nextInt(10) + 10;
        if (length > maxLength) {
            length = maxLength;
        }
        int floor = vfloor;
        if (vfloor == DEFAULT_FLOOR) {
            floor = height - 1 - globalRandom.nextInt(4);
        }
        if (withStraight) {
            for (int x = x0; x < x0 + length; x++) {
                for (int y = 0; y < height; y++) {
                    if (y >= floor) {
                        level.setBlock(x, y, (byte) (1 + 9 * 16));
                    }
                }
            }
        }
        boolean canBuild = true;
        int top = floor;
        if (isInGap) floor = level.height;
        while (canBuild) {
            top -= isFlatLevel ? 0 : (globalRandom.nextInt(2) + 2);
            if (top < 0) canBuild = false; else {
                int l = globalRandom.nextInt(length / 2) + 1;
                int xx0 = globalRandom.nextInt(l + 1) + x0;
                if (globalRandom.nextInt(4) == 0) {
                    decorate(xx0 - 1, xx0 + l + 1, top);
                    canBuild = false;
                }
                for (int x = xx0; x < xx0 + l; x++) {
                    for (int y = top; y < floor; y++) {
                        int xx = 5;
                        if (x == xx0) xx = 4;
                        if (x == xx0 + l - 1) xx = 6;
                        int yy = 9;
                        if (y == top) yy = 8;
                        if (level.getBlock(x, y) == 0) {
                            level.setBlock(x, y, (byte) (xx + yy * 16));
                        } else {
                            if (level.getBlock(x, y) == (byte) (4 + 8 * 16)) level.setBlock(x, y, (byte) (4 + 11 * 16));
                            if (level.getBlock(x, y) == (byte) (6 + 8 * 16)) level.setBlock(x, y, (byte) (6 + 11 * 16));
                        }
                    }
                }
                addEnemy(xx0, top - 1);
            }
        }
        return length;
    }

    private static int buildTubes(int xo, int maxLength, int maxHeight, int vfloor, int floorHeight) {
        int maxTubeHeight = 0;
        int length = globalRandom.nextInt(10) + 5;
        if (length > maxLength) length = maxLength;
        int floor = vfloor;
        if (vfloor == DEFAULT_FLOOR) {
            floor = height - 1 - globalRandom.nextInt(4);
        } else {
            globalRandom.nextInt();
        }
        int xTube = xo + 1 + globalRandom.nextInt(4);
        int tubeHeight = floor - globalRandom.nextInt(3) - 1;
        if (maxHeight != ANY_HEIGHT) {
            if (floor - tubeHeight > maxHeight) {
                if (maxHeight > 4) {
                    maxHeight = 4;
                }
                while (floor - tubeHeight > maxHeight) {
                    tubeHeight++;
                }
            }
        }
        if (floorHeight == INFINITE_FLOOR_HEIGHT) {
            floorHeight = height - floor;
        }
        int oldXTube = -1;
        for (int x = xo; x < xo + length; x++) {
            if (x > xTube + 1) {
                xTube += 3 + globalRandom.nextInt(4);
                tubeHeight = floor - globalRandom.nextInt(2) - 2;
                if (maxHeight != ANY_HEIGHT) {
                    while (floor - tubeHeight > maxHeight - 1) {
                        tubeHeight++;
                    }
                }
                if (tubeHeight > maxTubeHeight) maxTubeHeight = tubeHeight;
            }
            if (xTube >= xo + length - 2) {
                xTube += 10;
            }
            if (x == xTube && globalRandom.nextInt(11) < levelDifficulty + 1 && creaturesRandom.isCreatureEnabled("f")) {
                level.setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Sprite.KIND_ENEMY_FLOWER));
                ++counters.creatures;
            }
            for (int y = 0; y < floor + floorHeight; y++) {
                if (y >= floor && y <= floor + floorHeight) level.setBlock(x, y, (byte) (1 + 9 * 16)); else {
                    if ((x == xTube || x == xTube + 1) && y >= tubeHeight) {
                        int xPic = 10 + x - xTube;
                        if (y == tubeHeight) {
                            level.setBlock(x, y, (byte) (xPic + 0 * 16));
                            if (x == xTube) {
                                if (oldXTube != -1 && creaturesRandom.nextInt(35) > levelDifficulty + 1) {
                                }
                                oldXTube = x;
                                counters.tubesCount++;
                            }
                        } else {
                            level.setBlock(x, y, (byte) (xPic + 1 * 16));
                        }
                    }
                }
            }
        }
        if (globalRandom.nextBoolean()) buildBlocks(xo, xo + length, floor - maxTubeHeight - 2, false, 0, 0, false, false);
        return length;
    }

    private static int buildStraight(int xo, int maxLength, boolean safe, int vfloor, int floorHeight) {
        int length;
        if (floorHeight != INFINITE_FLOOR_HEIGHT) {
            length = maxLength;
        } else {
            length = globalRandom.nextInt(8) + 2;
            if (safe) length = 10 + globalRandom.nextInt(5);
            if (length > maxLength) length = maxLength;
        }
        int floor = vfloor;
        if (vfloor == DEFAULT_FLOOR) {
            floor = height - 1 - globalRandom.nextInt(4);
        } else {
            globalRandom.nextInt();
        }
        int y1 = height;
        if (floorHeight != INFINITE_FLOOR_HEIGHT) {
            y1 = floor + floorHeight;
        }
        for (int x = xo; x < xo + length; x++) for (int y = floor; y < y1; y++) if (y >= floor) level.setBlock(x, y, (byte) (1 + 9 * 16));
        if (!safe) {
            if (length > 5) {
                decorate(xo, xo + length, floor);
            }
        }
        return length;
    }

    private static boolean canBuildBlocks(int x0, int floor, boolean isHB) {
        if ((counters.blocksCount >= counters.totalBlocks && !isHB)) {
            return false;
        }
        boolean res = true;
        return res;
    }

    private static boolean buildBlocks(int x0, int x1, int floor, boolean pHB, int pS, int pE, boolean onlyHB, boolean isDistance) {
        boolean result = false;
        if (counters.blocksCount > counters.totalBlocks) {
            return false;
        }
        int s = pS;
        int e = pE;
        boolean hb = pHB;
        if (onlyHB) hb = onlyHB;
        --floor;
        while (floor > 0) {
            if ((x1 - 1 - e) - (x0 + 1 + s) > 0) {
                for (int x = x0 + s; x < x1 - e; x++) {
                    if (hb && counters.totalHiddenBlocks != 0) {
                        boolean isBlock = globalRandom.nextInt(2) == 1;
                        if (isBlock && canBuildBlocks(x, floor - 4, true)) {
                            level.setBlock(x, floor - 4, (byte) (1));
                            counters.hiddenBlocksCount++;
                            ++counters.coinsCount;
                        }
                    } else {
                        boolean canDeco = false;
                        if (x != x0 + 1 && x != x1 - 2 && globalRandom.nextInt(3) == 0) {
                            if (canBuildBlocks(x, floor - 4, false)) {
                                counters.blocksCount++;
                                int rnd = globalRandom.nextInt(6);
                                if (rnd >= 0 && rnd < 2) {
                                    if (level.getBlock(x, floor) == 0) level.setBlock(x, floor, (byte) (4 + 2 + 1 * 16));
                                } else if (rnd >= 2 && rnd < 4) {
                                    if (level.getBlock(x, floor) == 0) {
                                        level.setBlock(x, floor, (byte) (4 + 1 + 1 * 16));
                                        ++counters.coinsCount;
                                    }
                                } else if (rnd >= 4 && rnd < 6) {
                                    int coinsNumber = globalRandom.nextInt(9) + 1;
                                    level.setBlock(x, floor, (byte) (4 + 3 + 1 * 16));
                                    level.setBlockData(x, floor, (byte) -coinsNumber);
                                    counters.coinsCount += coinsNumber;
                                }
                                canDeco = true;
                            }
                        } else if (globalRandom.nextInt(4) == 0) {
                            if (canBuildBlocks(x, floor - 4, false)) {
                                counters.blocksCount++;
                                if (globalRandom.nextInt(4) == 0) {
                                    if (level.getBlock(x, floor) == 0) level.setBlock(x, floor, (byte) (2 + 1 * 16));
                                } else {
                                    if (level.getBlock(x, floor) == 0) {
                                        level.setBlock(x, floor, (byte) (1 + 1 * 16));
                                        ++counters.coinsCount;
                                    }
                                }
                                canDeco = true;
                            }
                        } else if (globalRandom.nextInt(2) == 1 && canBuildBlocks(x, floor - 4, false)) {
                            if (level.getBlock(x, floor) == 0) {
                                counters.blocksCount++;
                                level.setBlock(x, floor, (byte) (0 + 1 * 16));
                                canDeco = true;
                            }
                        }
                        if (canDeco) {
                            buildCoins(x0, x1, floor, s, e);
                        }
                    }
                }
                if (onlyHB) {
                    hb = true;
                } else {
                    hb = globalRandom.nextInt(4) == 0;
                }
            }
            int delta = isDistance ? 4 : globalRandom.nextInt(6) + 3;
            if (delta > 4) result = true;
            floor -= delta;
            s = globalRandom.nextInt(4);
            e = globalRandom.nextInt(4);
        }
        globalRandom.nextBoolean();
        return result;
    }

    private static void buildCoins(int x0, int x1, int floor, int s, int e) {
        if (floor - 2 < 0) return;
        if ((x1 - 1 - e) - (x0 + 1 + s) > 1) {
            for (int x = x0 + 1 + s; x < x1 - 1 - e; x++) {
                if (counters.coinsCount >= counters.totalCoins) {
                    break;
                }
                if (level.getBlock(x, floor - 2) == 0) {
                    counters.coinsCount++;
                    level.setBlock(x, floor - 2, (byte) (2 + 2 * 16));
                }
            }
        }
    }

    private static void decorate(int x0, int x1, int floor) {
        if (floor < 1) return;
        int s = globalRandom.nextInt(4);
        int e = globalRandom.nextInt(4);
        boolean hb = ((globalRandom.nextInt(levelDifficulty + 1) % (levelDifficulty + 1))) > 0.5;
        if (floor - 2 > 0 && !hb) {
            buildCoins(x0, x1, floor, s, e);
        }
        boolean buildLadder = buildBlocks(x0, x1, floor, hb, s, e, false, false);
        if (buildLadder && isLadder && globalRandom.nextInt(3) == 0) buildLadder(globalRandom.nextBoolean() ? x0 : x1, floor, ANY_HEIGHT);
    }

    private static void fixWalls() {
        boolean[][] blockMap = new boolean[length + 1][height + 1];
        for (int x = 0; x < length + 1; x++) {
            for (int y = 0; y < height + 1; y++) {
                int blocks = 0;
                for (int xx = x - 1; xx < x + 1; xx++) {
                    for (int yy = y - 1; yy < y + 1; yy++) {
                        if (level.getBlockCapped(xx, yy) == (byte) (1 + 9 * 16)) blocks++;
                    }
                }
                blockMap[x][y] = blocks == 4;
            }
        }
        blockify(level, blockMap, length + 1, height + 1);
    }

    private static void blockify(Level level, boolean[][] blocks, int width, int height) {
        int to = 0;
        if (levelType == LevelGenerator.TYPE_CASTLE) to = 4 * 2; else if (levelType == LevelGenerator.TYPE_UNDERGROUND) to = 4 * 3;
        boolean[][] b = new boolean[2][2];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int xx = x; xx <= x + 1; xx++) {
                    for (int yy = y; yy <= y + 1; yy++) {
                        int _xx = xx;
                        int _yy = yy;
                        if (_xx < 0) _xx = 0;
                        if (_yy < 0) _yy = 0;
                        if (_xx > width - 1) _xx = width - 1;
                        if (_yy > height - 1) _yy = height - 1;
                        b[xx - x][yy - y] = blocks[_xx][_yy];
                    }
                }
                if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
                    if (b[0][0] == b[0][1]) {
                        if (b[0][0]) {
                            level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                        } else {
                        }
                    } else {
                        if (b[0][0]) {
                            level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                        }
                    }
                } else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
                    if (b[0][0]) {
                        level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                    } else {
                        level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                    }
                } else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
                    level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                } else if (b[0][0] == b[1][0]) {
                    if (b[0][0]) {
                        if (b[0][1]) {
                            level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                        }
                    } else {
                        if (b[0][1]) {
                            level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                        }
                    }
                } else if (b[0][1] == b[1][1]) {
                    if (b[0][1]) {
                        if (b[0][0]) {
                            level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                        }
                    } else {
                        if (b[0][0]) {
                            level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                        }
                    }
                } else {
                    level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
                }
            }
        }
    }
}
