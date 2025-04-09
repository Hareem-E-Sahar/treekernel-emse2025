package tilex.sim;

import java.util.Random;

/**
 * @author Alexei
 */
public class SimField {

    private static Random rnd = new Random();

    /**
	 * Ширина поля.
	 */
    private final int width;

    /**
	 * Высота поля.
	 */
    private final int height;

    /**
	 * Кол-во видов ресурсов на поле.
	 */
    private final int resourceTypes;

    /**
	 * Матрица организмов.
	 */
    private SimCreature[] creatures = null;

    /**
	 * Матрицы с ресурсами всех видов.
	 * Первый индекс - вид ресурса, второй - матрица данного вида ресурсов.
	 */
    private int[][] resources = null;

    /**
	 * Способ обработки "краевого эффекта".
	 * 0 - поле замкнуто по краям
	 * 1 - поле замкнуто в тороид
	 */
    private int edgeEffectType;

    /**
	 * Смещения в матрице creatures до 8 окружающих клетку соседей и
	 * аналогичные смещения по координатам для обработки краевых эффектов.
	 */
    private int[] neighborOffsets = null;

    private int[][] neighborXYOffsets = null;

    SimField(int width, int height, int resourceTypes) {
        this.width = width;
        this.height = height;
        this.resourceTypes = resourceTypes;
        creatures = new SimCreature[width * height];
        resources = new int[resourceTypes][width * height];
        for (int i = 0; i < resourceTypes; i++) {
            for (int j = 0; j < width * height; j++) {
                resources[i][j] = 40 + rnd.nextInt(30);
            }
        }
        neighborOffsets = new int[] { -1, width + 1, -width, width - 1, 1, width, -width + 1, -width - 1 };
        neighborXYOffsets = new int[][] { { -1, 0 }, { 1, 1 }, { 0, -1 }, { -1, 1 }, { 1, 0 }, { 0, 1 }, { 1, -1 }, { -1, -1 } };
        setEdgeEffectType(0);
    }

    private void shuffleNeighborOffsets() {
        for (int i = 0; i < 8; i++) {
            int j = i + rnd.nextInt(8 - i);
            int t = neighborOffsets[i];
            neighborOffsets[i] = neighborOffsets[j];
            neighborOffsets[j] = t;
            t = neighborXYOffsets[i][0];
            neighborXYOffsets[i][0] = neighborXYOffsets[j][0];
            neighborXYOffsets[j][0] = t;
            t = neighborXYOffsets[i][1];
            neighborXYOffsets[i][1] = neighborXYOffsets[j][1];
            neighborXYOffsets[j][1] = t;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getResourceTypes() {
        return resourceTypes;
    }

    public void setEdgeEffectType(int edgeEffectType) {
        this.edgeEffectType = edgeEffectType;
    }

    public int getEdgeEffectType() {
        return edgeEffectType;
    }

    void randomize(int n) {
        Random rnd = new Random();
        for (int r = 0; r < resourceTypes; r++) {
            for (int i = 0; i < width * height; i++) {
                resources[r][i] = rnd.nextInt(n);
            }
        }
    }

    int getResource(int type, int x, int y) {
        if (resources != null) return resources[type][y * width + x]; else return 0;
    }

    public SimCreature getCreature(int x, int y) {
        if (creatures != null) return creatures[y * width + x]; else return null;
    }

    /**
	 * Добавляет на поле в указанные координаты новый организм, созданный по шаблону.
	 * 
	 * @param x
	 *            координата x
	 * @param y
	 *            координата y
	 * @param sample
	 *            шаблон
	 */
    public void addCreature(int x, int y, SimCreatureSample sample) {
        if (creatures != null) {
            creatures[y * width + x] = new SimCreature(sample, false, resourceTypes);
        }
    }

    /**
	 * Выполняет один шаг симуляции на поле, основываясь на текущем состоянии.
	 */
    public void simulate() {
        synchronized (this) {
            if (creatures != null) {
                int i = width * height - 1;
                for (int y = height - 1; y >= 0; y--) {
                    for (int x = width - 1; x >= 0; x--, i--) {
                        if (creatures[i] != null) {
                            int childs = simulateCreature(x, y, i);
                            if (childs > 0) {
                                shuffleNeighborOffsets();
                                if (y > 0 && y < height - 1 && x > 0 && x < width - 1) {
                                    int o = rnd.nextInt(8);
                                    for (int n = 0; n < 8; n++) {
                                        int j = i + neighborOffsets[o++ % 8];
                                        if (creatures[j] == null) {
                                            creatures[j] = new SimCreature(creatures[i], true, resourceTypes);
                                        } else {
                                            resources[creatures[i].getResourceIn()][i] += creatures[i].getStartMass();
                                        }
                                        childs--;
                                        if (childs == 0) {
                                            break;
                                        }
                                    }
                                } else {
                                    int o = rnd.nextInt(8);
                                    for (int n = 0; n < 8; n++) {
                                        int nx = x + neighborXYOffsets[o % 8][0];
                                        int ny = y + neighborXYOffsets[o++ % 8][1];
                                        switch(edgeEffectType) {
                                            case 0:
                                                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                                                    continue;
                                                }
                                                break;
                                            case 1:
                                                if (nx < 0) {
                                                    nx = width - 1;
                                                } else if (nx >= width) {
                                                    nx = 0;
                                                }
                                                if (ny < 0) {
                                                    ny = height - 1;
                                                } else if (ny >= height) {
                                                    ny = 0;
                                                }
                                                break;
                                        }
                                        int j = ny * width + nx;
                                        if (creatures[j] == null) {
                                            creatures[j] = new SimCreature(creatures[i], true, resourceTypes);
                                        } else {
                                            resources[creatures[i].getResourceIn()][i] += creatures[i].getStartMass();
                                        }
                                        childs--;
                                        if (childs == 0) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
	 * Выполняет симуляцию для одного организма в указанных координатах на поле.
	 * 
	 * @param x
	 *            координата x
	 * @param y
	 *            координата y
	 * @param i
	 *            смещение в массиве creatures для текущего организма (для уменьшения кол-ва вычислений)
	 * @return кол-во детей, которое появилось у данного организма на текущем шаге
	 */
    private int simulateCreature(int x, int y, int i) {
        SimCreature c = creatures[i];
        int ch = c.simulate(resources[c.resourceIn][i]);
        if (c.getAge() > c.getMaxAge()) {
            resources[c.resourceOut][i] += c.getMass();
            creatures[i] = null;
        } else {
            resources[c.resourceIn][i] -= Math.min(c.getResourceEat(), resources[c.resourceIn][i]);
        }
        return ch;
    }
}
