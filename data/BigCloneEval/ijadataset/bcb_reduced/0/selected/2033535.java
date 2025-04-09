package enlishem;

import java.util.ArrayList;
import java.util.Iterator;

public class Quadtree {

    private Quadtree[] children;

    private Quadtree parent;

    private int x1, x2, y1, y2;

    private ArrayList entities;

    public Quadtree(Tile[][] tiles, int x1, int x2, int y1, int y2, Quadtree parent) {
        this.parent = parent;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        createTree(tiles, x1, x2, y1, y2);
    }

    public Quadtree(Tile[][] tiles) {
        if (createTree(tiles, 0, tiles.length, 0, tiles.length)) entities = new ArrayList();
    }

    public boolean createTree(Tile[][] tiles, int x1, int x2, int y1, int y2) {
        if (x2 - x1 == 1) return true;
        children = new Quadtree[4];
        int midx = x1 + (x2 - x1) / 2, midy = y1 + (y2 - y1) / 2;
        children[0] = new Quadtree(tiles, x1, midx, y1, midy, this);
        children[1] = new Quadtree(tiles, midx, x2, y1, midy, this);
        children[2] = new Quadtree(tiles, midx, x2, midy, y2, this);
        children[3] = new Quadtree(tiles, x1, midx, midy, y2, this);
        return false;
    }

    public Quadtree getParent() {
        return parent;
    }

    public Quadtree[] getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return children == null;
    }

    public int getSize() {
        return x2 - x1;
    }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    public Iterator getEntities() {
        return entities.iterator();
    }
}
