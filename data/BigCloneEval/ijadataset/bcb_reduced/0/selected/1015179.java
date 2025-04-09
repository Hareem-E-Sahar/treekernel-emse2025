package orbe.hex;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class HXGeom {

    public static HXPoint offsetHex(HXPoint hex, int dir) throws HexDirUnknownError {
        int i = hex.i;
        int j = hex.j;
        switch(dir) {
            case 5:
                i--;
                break;
            case 2:
                i++;
                break;
            case 0:
                if (j % 2 == 0) i--;
                j--;
                break;
            case 1:
                if (j % 2 != 0) i++;
                j--;
                break;
            case 4:
                if (j % 2 == 0) i--;
                j++;
                break;
            case 3:
                if (j % 2 != 0) i++;
                j++;
                break;
            default:
                throw new HexDirUnknownError(dir);
        }
        hex.i = i;
        hex.j = j;
        return hex;
    }

    public static Set<HXPoint> getHexAround(HXPoint centre, int rayon) {
        if (rayon < 0) {
            throw new IllegalArgumentException("Rayon doit �tre >= 0");
        } else if (rayon == 0) {
            return Collections.singleton(centre);
        } else {
            HashSet<HXPoint> hexs = new HashSet<HXPoint>();
            HX3D centre3d = to3D(centre);
            for (int dx = -rayon; dx <= rayon; dx++) {
                int ymin = dx >= 0 ? -rayon : -rayon - dx;
                int ymax = dx >= 0 ? rayon - dx : rayon;
                for (int dy = ymin; dy <= ymax; dy++) {
                    int dz = dx + dy;
                    HXPoint p = to2D(centre3d.x + dx, centre3d.y + dy, centre3d.z + dz);
                    hexs.add(p);
                }
            }
            return hexs;
        }
    }

    public static HX3D to3D(int i, int j) {
        int x;
        int y;
        int z;
        if (j % 2 == 0) {
            x = j;
            y = (2 * i - j) / 2;
            z = 2 * i - y;
        } else {
            x = j;
            y = ((2 * i + 1) - j) / 2;
            z = (2 * i + 1) - y;
        }
        return new HX3D(x, y, z);
    }

    public static HX3D to3D(HXPoint p) {
        return to3D(p.i, p.j);
    }

    public static HXPoint to2D(int x, int y, int z) {
        int j = x;
        int i = (y + z) / 2;
        return new HXPoint(i, j);
    }

    public static HXPoint to2D(HX3D p) {
        return to2D(p.x, p.y, p.z);
    }

    public static int distance(HXPoint a, HXPoint b) {
        return distance(to3D(a), to3D(b));
    }

    private static int distance(HX3D a, HX3D b) {
        int maxx = Math.abs(a.x - b.x);
        int maxy = Math.abs(a.y - b.y);
        int maxz = Math.abs(a.z - b.z);
        return Math.max(maxx, Math.max(maxy, maxz));
    }

    public static void line(HXPoint a, HXPoint b, boolean includea, boolean includeb, HXPointIterator iterator) {
        HX3D pa = to3D(a);
        HX3D pb = to3D(b);
        int xa = pa.x;
        int ya = pa.y;
        int xb = pb.x;
        int yb = pb.y;
        int dx = Math.abs(xa - xb);
        int sx = xb > xa ? 1 : -1;
        int dy = Math.abs(ya - yb);
        int sy = yb > ya ? 1 : -1;
        boolean inversion = false;
        if (dy > dx) {
            inversion = true;
            int t = xa;
            xa = ya;
            ya = t;
            t = xb;
            xb = yb;
            yb = t;
            t = sx;
            sx = sy;
            sy = t;
            t = dx;
            dx = dy;
            dy = t;
        }
        int d = 2 * dy - dx;
        HX3D diff = new HX3D(xb - xa, yb - ya, (xb + yb) - (xa + ya));
        Sextant sextant = getSextant(diff);
        boolean start = true;
        while (xa != xb || ya != yb) {
            int cx = xa;
            int cy = ya;
            if (inversion) {
                int t = cx;
                cx = cy;
                cy = t;
            }
            if (start) {
                if (includea) {
                    HXPoint hex = to2D(cx, cy, cx + cy);
                    iterator.forHX(hex);
                }
                start = false;
            } else {
                HXPoint hex = to2D(cx, cy, cx + cy);
                iterator.forHX(hex);
            }
            int za = xa + ya;
            int nxa = xa;
            int nya = ya;
            boolean diagonale = false;
            if (d >= 0) {
                nya += sy;
                d -= 2 * dx;
                diagonale = true;
            }
            nxa += sx;
            d += 2 * dy;
            int nza = nxa + nya;
            int dz = Math.abs(nza - za);
            if (dz >= 2) {
                HXPoint hex = to2D(xa, ya, xa + ya);
                if (diagonale) {
                    offsetHex(hex, sextant.d1);
                } else {
                    offsetHex(hex, sextant.d2);
                }
                HX3D intermediaire = to3D(hex);
                int ix = intermediaire.x;
                int iy = intermediaire.y;
                if (inversion) {
                    int t = ix;
                    ix = iy;
                    iy = t;
                }
                hex = to2D(ix, iy, ix + iy);
                iterator.forHX(hex);
            }
            xa = nxa;
            ya = nya;
        }
        if (includeb) {
            if (inversion) {
                int t = xb;
                xb = yb;
                yb = t;
            }
            HXPoint hex = to2D(xb, yb, xb + yb);
            iterator.forHX(hex);
        }
    }

    /**
	 * Calcul du sextant d'un point par rapport � l'origine
	 */
    private static Sextant getSextant(HX3D p) {
        int x = p.x;
        int y = p.y;
        int z = p.z;
        if (x > 0) {
            if (y > 0) {
                if (z > 0) {
                    if (x < y) {
                        return new Sextant(2, 3);
                    } else {
                        return new Sextant(3, 2);
                    }
                } else {
                    throw new RuntimeException("Cas impossible : x>0, y>0, z<0");
                }
            } else {
                if (z > 0) {
                    if (-y > z) {
                        return new Sextant(4, 3);
                    } else {
                        return new Sextant(3, 4);
                    }
                } else {
                    if (-x > z) {
                        return new Sextant(5, 4);
                    } else {
                        return new Sextant(4, 5);
                    }
                }
            }
        } else {
            if (y > 0) {
                if (z > 0) {
                    if (-x > z) {
                        return new Sextant(1, 2);
                    } else {
                        return new Sextant(2, 1);
                    }
                } else {
                    if (-y > z) {
                        return new Sextant(0, 1);
                    } else {
                        return new Sextant(1, 0);
                    }
                }
            } else {
                if (z > 0) {
                    throw new RuntimeException("Cas impossible : x<0, y<0, z>0");
                } else {
                    if (x < y) {
                        return new Sextant(0, 5);
                    } else {
                        return new Sextant(5, 0);
                    }
                }
            }
        }
    }

    private static class Sextant {

        public String toString() {
            return (new StringBuilder("(")).append(d1).append(",").append(d2).append(")").toString();
        }

        public int d1;

        public int d2;

        public Sextant(int d1, int d2) {
            this.d1 = d1;
            this.d2 = d2;
        }
    }

    private static class Portee {

        public String toString() {
            return (new StringBuilder()).append(h1).append(" --> ").append(h2).toString();
        }

        public HXPoint h1;

        public HXPoint h2;

        public Portee(HXPoint h1, HXPoint h2) {
            this.h1 = h1;
            if (h1 == h2) this.h2 = new HXPoint(h2); else this.h2 = h2;
        }
    }

    public static void paint(HXPoint startPoint, HXPaintControler paintControler) {
        Stack<Portee> stack = new Stack<Portee>();
        Portee p = new Portee(startPoint, startPoint);
        stack.push(p);
        while (!stack.isEmpty()) {
            p = stack.pop();
            paintExtend(p, paintControler);
            paintFill(p, paintControler);
            Portee np = null;
            HXPoint h = new HXPoint(p.h1);
            offsetHex(h, 0);
            if (paintControler.canFill(h)) np = new Portee(h, h);
            int i1 = p.h1.i;
            int i2 = p.h2.i;
            int j = p.h1.j;
            for (int i = i1; i <= i2; i++) {
                h = new HXPoint(i, j);
                offsetHex(h, 1);
                if (paintControler.canFill(h)) {
                    if (np != null) np.h2 = h; else np = new Portee(h, h);
                } else if (np != null) {
                    stack.push(np);
                    np = null;
                }
            }
            if (np != null) stack.push(np);
            np = null;
            h = new HXPoint(p.h1);
            offsetHex(h, 4);
            if (paintControler.canFill(h)) np = new Portee(h, h);
            i1 = p.h1.i;
            i2 = p.h2.i;
            j = p.h1.j;
            for (int i = i1; i <= i2; i++) {
                h = new HXPoint(i, j);
                offsetHex(h, 3);
                if (paintControler.canFill(h)) {
                    if (np != null) np.h2 = h; else np = new Portee(h, h);
                } else if (np != null) {
                    stack.push(np);
                    np = null;
                }
            }
            if (np != null) stack.push(np);
        }
    }

    private static void paintExtend(Portee portee, HXPaintControler paintControler) {
        HXPoint h = new HXPoint(portee.h1);
        do {
            h = new HXPoint(h.i - 1, h.j);
        } while (paintControler.canFill(h));
        h = new HXPoint(h.i + 1, h.j);
        portee.h1 = h;
        h = new HXPoint(portee.h2);
        do {
            h = new HXPoint(h.i + 1, h.j);
        } while (paintControler.canFill(h));
        h = new HXPoint(h.i - 1, h.j);
        portee.h2 = h;
    }

    private static void paintFill(Portee portee, HXPaintControler paintControler) {
        int i1 = portee.h1.i;
        int i2 = portee.h2.i;
        int j = portee.h1.j;
        for (int i = i1; i <= i2; i++) {
            paintControler.fill(new HXPoint(i, j));
        }
    }

    /**
	 * Parcours de a � b en suivant les contours des hex
	 * 
	 * @param a
	 *            D�but du parcours
	 * @param b
	 *            Fin du parcours
	 * @param includea
	 *            Traiter le d�but ?
	 * @param includeb
	 *            Traiter la fin ?
	 * @param iterator
	 *            Traitement � effectuer
	 */
    public static void edge(HXCorner a, HXCorner b, boolean includea, boolean includeb, HXCornerIterator iterator) {
        HX3DCorner pa = to3DCorner(a);
        HX3DCorner pb = to3DCorner(b);
        int xa = pa.x;
        int ya = pa.y;
        int za = pa.z;
        int xb = pb.x;
        int yb = pb.y;
        int zb = pb.z;
        int[] x = new int[3];
        int[] y = new int[3];
        int[] z = new int[3];
        int[] distance = new int[3];
        if (includea) {
            iterator.forCorner(a);
        }
        while ((xa != xb) || (ya != yb)) {
            boolean pair = (ya - xa) % 3 == 0;
            if (pair) {
                x[0] = xa - 1;
                y[0] = ya;
                z[0] = za - 1;
                x[1] = xa + 1;
                y[1] = ya - 1;
                z[1] = za;
                x[2] = xa;
                y[2] = ya + 1;
                z[2] = za + 1;
            } else {
                x[0] = xa;
                y[0] = ya - 1;
                z[0] = za - 1;
                x[1] = xa + 1;
                y[1] = ya;
                z[1] = za + 1;
                x[2] = xa - 1;
                y[2] = ya + 1;
                z[2] = za;
            }
            for (int i = 0; i < 3; i++) {
                distance[i] = distanceCorner(x[i], y[i], z[i], xb, yb, zb);
            }
            int min = Integer.MAX_VALUE;
            int index = -1;
            for (int i = 0; i < 3; i++) {
                if (distance[i] < min) {
                    min = distance[i];
                    index = i;
                }
            }
            xa = x[index];
            ya = y[index];
            za = z[index];
            boolean ok = true;
            if (xa == xb && ya == yb) {
                ok = includeb;
            }
            if (ok) {
                iterator.forCorner(to2DCorner(xa, ya, za, 0));
            }
        }
    }

    /**
	 * Convertir un coin xyz en un coin (i,j,n)
	 * 
	 * @return Coin converti
	 * @param x,
	 *            y, z Coordonn�es normalis�es du coin
	 * @param _012
	 *            Type de coin d�sir�. Valeur comprise entre 0, 1 et 2. Si coin
	 *            pair, les valeurs de coin prises sont 0, 2, 4. Si coin impair,
	 *            les valeurs de coin prises sont 1, 3, 5.
	 */
    public static HXCorner to2DCorner(int cx, int cy, int cz, int _012) {
        int test = (cx - cy) % 3;
        int x, y, z;
        int corner;
        if (test == 0) {
            switch(_012) {
                case 0:
                    x = (cx + 2 * cy) / 3;
                    y = (cx - cy) / 3;
                    corner = 0;
                    break;
                case 1:
                    x = (cx + 2 * cy) / 3;
                    y = (cx - cy) / 3 - 1;
                    corner = 2;
                    break;
                case 2:
                    x = (cx + 2 * cy) / 3 - 1;
                    y = (cx - cy) / 3;
                    corner = 4;
                    break;
                default:
                    throw new IllegalArgumentException("_012 must be 0, 1 or 2");
            }
        } else {
            switch(_012) {
                case 0:
                    x = (cx + 2 * cy + 1) / 3;
                    y = (cx - cy - 2) / 3;
                    corner = 1;
                    break;
                case 1:
                    x = (cx + 2 * cy - 2) / 3;
                    y = (cx - cy - 2) / 3;
                    corner = 3;
                    break;
                case 2:
                    x = (cx + 2 * cy - 2) / 3;
                    y = (cx - cy + 1) / 3;
                    corner = 5;
                    break;
                default:
                    throw new IllegalArgumentException("_012 must be 0, 1 or 2");
            }
        }
        z = x + y;
        HXPoint hex = to2D(x, y, z);
        return new HXCorner(hex, corner);
    }

    /**
	 * Distance entre deux coins
	 */
    public static int distanceCorner(int xa, int ya, int za, int xb, int yb, int zb) {
        return Math.max(Math.abs(xa - xb), Math.max(Math.abs(ya - yb), Math.abs(za - zb)));
    }

    public static HX3DCorner to3DCorner(HXCorner c) {
        HX3D h = to3D(c.hex);
        int x;
        int y;
        int z;
        switch(c.corner) {
            case 0:
                x = h.y + h.z;
                y = h.x - h.y;
                break;
            case 1:
                x = h.y + h.z + 1;
                y = h.x - h.y - 1;
                break;
            case 2:
                x = h.y + h.z + 2;
                y = h.x - h.y - 1;
                break;
            case 3:
                x = h.y + h.z + 2;
                y = h.x - h.y;
                break;
            case 4:
                x = h.y + h.z + 1;
                y = h.x - h.y + 1;
                break;
            case 5:
                x = h.y + h.z;
                y = h.x - h.y + 1;
                break;
            default:
                throw new HexDirUnknownError(c.corner);
        }
        z = x + y;
        return new HX3DCorner(x, y, z);
    }
}
