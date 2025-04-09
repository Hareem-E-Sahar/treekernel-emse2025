package org.maze;

import java.util.Random;
import org.maze.individual.Individual;
import org.maze.utils.Vector;

/**
 *
 * @author tpasquie
 */
public class Maze {

    public static final byte EMPTY_FIELD = 0;

    public static final byte WALL_FIELD = 1;

    public static final byte STARTING_POSITION = 2;

    public static final byte MAZE_EXIT = 3;

    public static final byte SIZE = 32;

    private byte[] grid;

    private static Maze instance = null;

    private Random rand = new Random();

    private Maze() {
        grid = new byte[SIZE * SIZE];
    }

    public static Maze getInstance() {
        if (instance == null) instance = new Maze();
        return instance;
    }

    public int getSize() {
        return SIZE;
    }

    public byte[] getGrid() {
        return grid.clone();
    }

    public void randomGenerate() throws Exception {
        grid[0] = EMPTY_FIELD;
        this.fillWithWall();
        this.generateMainPath();
        for (int i = 0; i < 100; i++) {
            int x, y;
            do {
                x = rand.nextInt(SIZE);
                y = rand.nextInt(SIZE);
            } while (this.getGridValue(x, y) != EMPTY_FIELD);
            this.generateAdditionalPath(x, y);
        }
        grid[0] = STARTING_POSITION;
        grid[grid.length - 1] = MAZE_EXIT;
    }

    public void fillWithWall() throws Exception {
        for (int i = 0; i < grid.length; i++) {
            grid[i] = WALL_FIELD;
        }
    }

    public void generateMainPath() throws Exception {
        Vector v = new Vector();
        v.x = 0;
        v.y = 0;
        byte dir;
        int r = rand.nextInt(2);
        if (r == 0) {
            dir = Individual.EAST;
            v = this.makeCorridor(v.x, v.y, dir, 10);
        } else {
            dir = Individual.SOUTH;
            v = this.makeCorridor(v.x, v.y, dir, 10);
        }
        if (dir == Individual.EAST) {
            dir = Individual.SOUTH;
            v = this.makeCorridor(v.x, v.y, dir, 10);
        } else {
            dir = Individual.EAST;
            v = this.makeCorridor(v.x, v.y, dir, 10);
        }
        do {
            if (v.y == SIZE - 1) {
                dir = Individual.EAST;
                v = this.makeCorridor(v.x, v.y, dir, 10);
            } else if (v.x == SIZE - 1) {
                dir = Individual.SOUTH;
                v = this.makeCorridor(v.x, v.y, dir, 10);
            } else if (v.x == 0) {
                dir = Individual.SOUTH;
                v = this.makeCorridor(v.x, v.y, dir, 10);
                dir = Individual.EAST;
                v = this.makeCorridor(v.x, v.y, dir, 10);
            } else if (v.y == 0) {
                dir = Individual.EAST;
                v = this.makeCorridor(v.x, v.y, dir, 10);
                dir = Individual.SOUTH;
                v = this.makeCorridor(v.x, v.y, dir, 10);
            } else if (dir == Individual.EAST) {
                int y = v.y;
                r = rand.nextInt(2);
                if (r == 0 && this.checkNorth(v.x, v.y)) {
                    dir = Individual.NORTH;
                    v = this.makeCorridor(v.x, v.y, dir, 5);
                } else {
                    dir = Individual.SOUTH;
                    v = this.makeCorridor(v.x, v.y, dir, 10);
                }
            } else if (dir == Individual.SOUTH) {
                r = rand.nextInt(2);
                if (r == 0 && this.checkWest(v.x, v.y)) {
                    dir = Individual.WEST;
                    v = this.makeCorridor(v.x, v.y, dir, 5);
                } else {
                    dir = Individual.EAST;
                    v = this.makeCorridor(v.x, v.y, dir, 10);
                }
            } else if (dir == Individual.NORTH) {
                dir = Individual.EAST;
                v = this.makeCorridor(v.x, v.y, dir, 10);
            } else if (dir == Individual.WEST) {
                dir = Individual.SOUTH;
                v = this.makeCorridor(v.x, v.y, dir, 10);
            }
        } while (v.x != SIZE - 1 || v.y != SIZE - 1);
    }

    public Vector makeCorridor(int x, int y, byte direction, int max_length) throws Exception {
        Vector v = new Vector();
        int r;
        v.x = x;
        v.y = y;
        if (direction == Individual.EAST) {
            if (x + max_length > SIZE - 1) {
                max_length = SIZE - x - 1;
            }
        } else if (direction == Individual.SOUTH) {
            if (y + max_length > SIZE - 1) {
                max_length = SIZE - y - 1;
            }
        } else if (direction == Individual.WEST) {
            if (x - max_length < 0) {
                max_length = x - 1;
            }
        } else if (direction == Individual.NORTH) {
            if (y - max_length < 0) {
                max_length = y - 1;
            }
        }
        if (direction == Individual.EAST) {
            int count = 0;
            if (max_length > 2) r = 2 + rand.nextInt(max_length - 2); else r = max_length;
            do {
                v.x += 1;
                this.writeCase(EMPTY_FIELD, v.x, v.y);
                count++;
            } while (count < r);
        }
        if (direction == Individual.WEST) {
            int count = 0;
            if (max_length > 2) r = 2 + rand.nextInt(max_length - 2); else r = max_length;
            do {
                v.x -= 1;
                this.writeCase(EMPTY_FIELD, v.x, v.y);
                count++;
            } while (count < r);
        }
        if (direction == Individual.NORTH) {
            int count = 0;
            if (max_length > 2) r = 2 + rand.nextInt(max_length - 2); else r = max_length;
            do {
                v.y -= 1;
                this.writeCase(EMPTY_FIELD, v.x, v.y);
                count++;
            } while (count < r);
        }
        if (direction == Individual.SOUTH) {
            int count = 0;
            if (max_length > 2) r = 2 + rand.nextInt(max_length - 2); else r = max_length;
            do {
                v.y += 1;
                this.writeCase(EMPTY_FIELD, v.x, v.y);
                count++;
            } while (count < r);
        }
        if (v.y == SIZE - 2) {
            v.y += 1;
            this.writeCase(EMPTY_FIELD, v.x, v.y);
        } else if (v.x == SIZE - 2) {
            v.x += 1;
            this.writeCase(EMPTY_FIELD, v.x, v.y);
        } else if (v.y == 1) {
            v.y -= 1;
            this.writeCase(EMPTY_FIELD, v.x, v.y);
        } else if (v.x == 1) {
            v.x -= 1;
            this.writeCase(EMPTY_FIELD, v.x, v.y);
        }
        return v;
    }

    public boolean checkWest(int x, int y) {
        if (x == 0) return false;
        for (int i = x - 1; i >= 0; i--) {
            if (this.getGridValue(i, y) == EMPTY_FIELD) return false;
        }
        return true;
    }

    public boolean checkNorth(int x, int y) {
        if (y == 0) return false;
        for (int i = y - 1; i >= 0; i--) {
            if (this.getGridValue(y, i) == EMPTY_FIELD) return false;
        }
        return true;
    }

    public void generateAdditionalPath(int vx, int vy) throws Exception {
        Vector v = new Vector();
        v.x = vx;
        v.y = vy;
        int count = 0;
        int fields = 0;
        do {
            count++;
            int r = rand.nextInt(4);
            if (r == 0) {
                if (v.x + 1 >= 0 && v.x + 1 < SIZE && this.checkSurrounding(v.x + 1, v.y)) {
                    v.x += 1;
                    this.writeCase(EMPTY_FIELD, v.x, v.y);
                    count = 0;
                }
            }
            if (r == 1) {
                if (v.x - 1 >= 0 && v.x - 1 < SIZE && this.checkSurrounding(v.x - 1, v.y)) {
                    v.x -= 1;
                    this.writeCase(EMPTY_FIELD, v.x, v.y);
                    count = 0;
                }
            }
            if (r == 2) {
                if (v.y + 1 >= 0 && v.y + 1 < SIZE && this.checkSurrounding(v.x, v.y + 1)) {
                    v.y += 1;
                    this.writeCase(EMPTY_FIELD, v.x, v.y);
                    count = 0;
                }
            }
            if (r == 3) {
                if (v.y - 1 >= 0 && v.y - 1 < SIZE && this.checkSurrounding(v.x, v.y - 1)) {
                    v.y -= 1;
                    this.writeCase(EMPTY_FIELD, v.x, v.y);
                    count = 0;
                }
            }
        } while (count < 20);
    }

    public void writeCase(byte type, int x, int y) throws Exception {
        if (x < 0 || x >= SIZE) throw new Exception("X, out of grid : " + x + "-" + y);
        if (y < 0 || y >= SIZE) throw new Exception("Y, out of grid : " + x + "-" + y);
        grid[x + y * SIZE] = type;
    }

    public int getAvailablePath(int x, int y) {
        int v = 0;
        if (this.getGridValue(x - 1, y) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x + 1, y) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x, y + 1) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x, y - 1) == EMPTY_FIELD) {
            v++;
        }
        return v;
    }

    public boolean checkSurrounding(int x, int y) {
        int v = 0;
        if (this.getGridValue(x - 1, y) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x + 1, y) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x - 1, y + 1) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x + 1, y + 1) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x - 1, y - 1) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x + 1, y - 1) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x, y + 1) == EMPTY_FIELD) {
            v++;
        }
        if (this.getGridValue(x, y - 1) == EMPTY_FIELD) {
            v++;
        }
        if (v > 2) return false; else return true;
    }

    public byte getGridValue(int x, int y) {
        if (x < 0 || x >= SIZE) return WALL_FIELD;
        if (y < 0 || y >= SIZE) return WALL_FIELD;
        return grid[x + y * SIZE];
    }
}
