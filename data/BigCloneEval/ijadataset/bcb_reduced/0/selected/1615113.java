package org.levex.games.fallingsand;

import java.util.Random;
import org.lwjgl.Sys;
import org.newdawn.slick.SlickException;

/**
 *
 * @author Levex
 */
public class Generator {

    public static boolean initDone = false;

    public static int W = 0, H = 0;

    public static volatile char[][] map = null;

    public static int char_x = 0;

    public static int char_y = 0;

    public static void init(int w, int h) {
        W = w;
        H = h;
        map = new char[W][H];
        initDone = true;
    }

    private static void Disc(int centerx, int centery, int diameter, char material) {
        int r = diameter / 2;
        int rr = r * r;
        for (int x = centerx - r; x <= centerx + r; ++x) {
            int dx = x - centerx;
            int dy = (int) Math.round(Math.sqrt(rr - dx * dx));
            for (int y = centery - dy; y <= centery + dy; ++y) {
                if (x >= W - 4) continue;
                if (x <= 0) continue;
                if (y >= H - 4) continue;
                if (y <= 0) continue;
                map[x][y] = material;
            }
        }
    }

    private static void setPixel(int x, int y, char m) {
        if (x >= W) return;
        if (x <= 0) return;
        if (y >= H) return;
        if (y <= 0) return;
        map[x][y] = m;
    }

    private static void Circle(int xo, int yo, int diameter, char material) {
        int radius = diameter / 2;
        setPixel(xo, yo + radius, material);
        setPixel(xo, yo - radius, material);
        setPixel(xo + radius, yo, material);
        setPixel(xo - radius, yo, material);
        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;
        while (x < y) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;
            setPixel(xo + x, yo + y, material);
            setPixel(xo - x, yo + y, material);
            setPixel(xo + x, yo - y, material);
            setPixel(xo - x, yo - y, material);
            setPixel(xo + y, yo + x, material);
            setPixel(xo - y, yo + x, material);
            setPixel(xo + y, yo - x, material);
            setPixel(xo - y, yo - x, material);
        }
    }

    public static void addCharacter(char mat, int x, int y) {
        char_x = x;
        char_y = y;
        map[x][y - 1] = mat;
        map[x][y - 2] = mat;
        map[x][y - 3] = mat;
        map[x][y - 4] = mat;
        map[x][y - 5] = mat;
        map[x + 1][y - 3] = mat;
        map[x + 2][y - 3] = mat;
        map[x - 1][y - 3] = mat;
        map[x - 2][y - 3] = mat;
        map[x + 1][y + 1] = mat;
        map[x - 1][y + 1] = mat;
    }

    private static void removeCharacter() {
        char mat = 0;
        map[char_x][char_y - 1] = mat;
        map[char_x][char_y - 2] = mat;
        map[char_x][char_y - 3] = mat;
        map[char_x][char_y - 4] = mat;
        map[char_x][char_y - 5] = mat;
        map[char_x + 1][char_y - 3] = mat;
        map[char_x + 2][char_y - 3] = mat;
        map[char_x - 1][char_y - 3] = mat;
        map[char_x - 2][char_y - 3] = mat;
        map[char_x + 1][char_y + 1] = mat;
        map[char_x - 1][char_y + 1] = mat;
    }

    private static boolean checkCharacterY(int y) {
        char mat = 0;
        if (y < 0) {
            if (map[char_x - 1][char_y + (y - 4)] != mat) return false;
            if (map[char_x - 2][char_y + (y - 4)] != mat) return false;
            if (map[char_x + 1][char_y + (y - 4)] != mat) return false;
            if (map[char_x + 2][char_y + (y - 4)] != mat) return false;
            if (map[char_x][char_y + (y - 6)] != mat) return false;
        } else {
            if (map[char_x - 2][char_y + (y - 3)] != mat) return false;
            if (map[char_x][char_y + (y + 1)] != mat) return false;
            if (map[char_x - 1][char_y + (y + 2)] != mat) return false;
            if (map[char_x + 1][char_y + (y + 2)] != mat) return false;
            if (map[char_x + 2][char_y + (y - 3)] != mat) return false;
        }
        return true;
    }

    private static boolean checkCharacterX(int x) {
        char mat = 0;
        if (map[char_x + x][char_y - 1] != mat) return false;
        if (map[char_x + x][char_y - 2] != mat) return false;
        if (map[char_x + x][char_y - 4] != mat) return false;
        if (map[char_x + x][char_y - 5] != mat) return false;
        if (x < 0) {
            if (map[char_x - (x + 2)][char_y - 3] != mat) return false;
        } else {
            if (map[char_x + (x + 2)][char_y - 3] != mat) return false;
        }
        return true;
    }

    public static void moveCharacterX(int i) {
        if (char_x + i >= W) return;
        if (char_x + i <= 0) return;
        if (checkCharacterX(i)) {
            removeCharacter();
            addCharacter(Properties.ids.get("char"), char_x + i, char_y);
        }
    }

    public static void moveCharacterY(int i) {
        if (char_y + i >= H) return;
        if (char_y + i <= 0) return;
        if (checkCharacterY(i)) {
            removeCharacter();
            addCharacter(Properties.ids.get("char"), char_x, char_y + i);
        }
    }

    private static int[] isThereNearby(char m, int x, int y) {
        return isThereNearby(m, x, y, 20);
    }

    private static int[] isThereNearby(char m, int x, int y, int radius) {
        for (int _x = x - radius / 2; _x < x + radius / 2; _x++) {
            for (int _y = y - radius / 2; _y < y + radius / 2; _y++) {
                if (map[_x][_y] == m) {
                    Inventory.add(m, 1);
                    return new int[] { _x, _y };
                }
            }
        }
        return null;
    }

    public static void mine(int x, int y) {
        int[] p = null;
        if ((p = isThereNearby(Properties.ids.get("stone"), x, y)) != null) {
            int x0 = p[0];
            int y0 = p[1];
            map[x0][y0] = 0;
        }
    }

    private static Random gRND = new Random();

    public static void useUtility(char m) {
        map[char_x][char_y - 7] = m;
        if (m == Properties.ids.get("fire")) {
            Canvas.energy[char_x][char_y - 7] = (char) (100 + gRND.nextInt(100));
        }
    }

    public static void generate(int seed) throws SlickException {
        if (!initDone) throw new SlickException("Use init() first.");
        Random rnd = new Random(seed);
        int rocks = 11;
        for (int i = 0; i < rocks; i++) {
            int x = 46 + rnd.nextInt(W - 46);
            int y = 46 + rnd.nextInt(H - 46);
            Disc(x, y, 8, Properties.ids.get("stone"));
        }
        int trees = 10;
        int tree_height = 50;
        int tree_width = 50;
        int branches_height = 50;
        int branches_width = 50;
        for (int i = 0; i < trees; i++) {
            int y = tree_height + branches_height + rnd.nextInt(H - tree_height - branches_height);
            int x = tree_width + branches_width + rnd.nextInt(W - tree_width - branches_width);
            int tree_top = y - tree_height;
            for (int a = y - tree_height; a < y; a++) {
                map[x][a] = Properties.ids.get("wood");
                map[x - 1][a] = Properties.ids.get("wood");
                map[x + 1][a] = Properties.ids.get("wood");
            }
            Disc(x, y - (tree_height + 16), 32, Properties.ids.get("plant"));
        }
        int lakes = 1;
        for (int i = 0; i < lakes; i++) {
            int x = 46 + rnd.nextInt(W - 46);
            int y = 46 + rnd.nextInt(H - 46);
            Circle(x, y, 64, Properties.ids.get("stone"));
            Circle(x + 1, y, 64, Properties.ids.get("stone"));
            Disc(x, y, 60, Properties.ids.get("water"));
        }
        int house_width = 50;
        int house_height = 50;
        int x = 4 + rnd.nextInt(46);
        int y = 4 + rnd.nextInt(H - 4);
        for (int i = x; i < x + house_width; i++) {
            for (int a = y; a < y + house_height; a++) {
                map[i][a] = Properties.ids.get("house");
            }
        }
    }
}
