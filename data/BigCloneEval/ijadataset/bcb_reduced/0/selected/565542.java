package com.dukesoftware.antsim.ant;

import java.util.Random;
import com.dukesoftware.antsim.bag.Bag;
import com.dukesoftware.antsim.object.INextPosGenerator;
import com.dukesoftware.antsim.simulate.Const;
import com.dukesoftware.utils.data.TwoDimArrayCacheRegistory;

public class AntNextPosGenerator implements INextPosGenerator {

    public static final int RIGHT = 0;

    public static final int LEFT = 1;

    public static final int UP = 2;

    public static final int DOWN = 3;

    private int direction;

    private int nx, ny;

    private final Ant ant;

    private static Random random = new Random(System.currentTimeMillis());

    public AntNextPosGenerator(Ant ant) {
        this.ant = ant;
    }

    public void init(int x, int y) {
        nx = x;
        ny = y;
    }

    public int getDirection() {
        return direction;
    }

    private void updateDirection() {
        this.direction = random.nextInt(DOWN + 1);
    }

    private void updateNextPos(int x, int y) {
        if (direction == RIGHT) {
            nx = x + 1;
        } else if (direction == LEFT) {
            nx = x - 1;
        } else if (direction == UP) {
            ny = y - 1;
        } else if (direction == DOWN) {
            ny = y + 1;
        }
    }

    /**
	 * 
	 */
    public void calcNextPosition(int x, int y, TwoDimArrayCacheRegistory map) {
        while (true) {
            if (random.nextInt(5) == 0) {
                updateDirection();
            }
            updateNextPos(x, y);
            if (map.isInMap(nx, ny)) break;
        }
        if (!map.doesObjExist(Const.KEY_ANT, nx, ny)) {
            if (map.doesObjExist(Const.KEY_BAG, nx, ny)) {
                if (ant.isCarry()) {
                    Bag bag = ant.putBag();
                    bag.put(x, y);
                    map.putObj(Const.KEY_BAG, x, y, bag);
                } else {
                    Bag bag = (Bag) map.takeObj(Const.KEY_BAG, nx, ny);
                    bag.carry();
                    ant.takeBag(bag);
                }
            } else {
            }
            map.moveObj(Const.KEY_ANT, x, y, nx, ny);
            ant.set(nx, ny);
        } else {
        }
    }
}
