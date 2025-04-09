package org.gzigzag.vob;

import java.util.*;
import java.awt.*;

/** A class for anchoring buoys to a region on the sea floor.
 *  <blockquote>
 *  buoy (n) : bright-colored; a float attached by rope to the seabed to
 *             mark channels in a harbor or underwater hazards
 *             <i>(WordNet (r) 1.7)</i>
 *  </blockquote>
 */
public class BuoyPlacer {

    public static final String rcsid = "$Id: BuoyPlacer.java,v 1.13 2001/09/21 10:54:07 tjl Exp $";

    public static boolean dbg = false;

    private static void p(String s) {
        if (dbg) pa(s);
    }

    private static void pa(String s) {
        System.err.println(s);
    }

    public interface Buoy {

        int getPrefWidth();

        int getPrefHeight();

        int getMinWidth();

        int getMinHeight();

        /** If false, scale any way you like; if true, retain aspect
	 * ratio.
	 */
        boolean constAspectScalable();

        void put(VobScene into, int x, int y, int w, int h);
    }

    private Rectangle sea;

    private TreeSet placeables;

    private int count = 0;

    private static final class Placeable implements Comparable {

        public Buoy buoy;

        public int[] pos;

        public float importance;

        public int compareTo(Object o) {
            if (o == null) return 1;
            Placeable p = (Placeable) o;
            if (p == this) return 0;
            if (importance > p.importance) return 1;
            if (importance < p.importance) return -1;
            throw new RuntimeException("Two Placeables with same importance!");
        }

        public Placeable(Buoy b, int[] p, float i) {
            this.buoy = b;
            this.pos = p;
            this.importance = i;
        }
    }

    public BuoyPlacer(Rectangle sea) {
        this.sea = sea;
        placeables = new TreeSet();
    }

    public void add(Buoy b, int x, int y, float importance) {
        add(b, new int[] { x, y }, importance);
    }

    public void add(Buoy b, int[] pos, float importance) {
        float epsilon = 0.001f;
        placeables.add(new Placeable(b, pos, importance + count * epsilon));
        count++;
    }

    public void place(VobScene seafloor) {
        Placeable[] pls = (Placeable[]) placeables.toArray(new Placeable[] {});
        SortedSet impord = new TreeSet();
        SortedMap rng = new TreeMap();
        rng.put(new Integer(sea.y), new Integer(sea.y));
        rng.put(new Integer(sea.y + sea.height - 1), new Integer(sea.y + sea.height - 1));
        float sctop = 1000000000000000.0f, scbot = 0;
        for (int i = 0; i < pls.length; i++) {
            int y = (int) pls[i].pos[1];
            if (scbot < y) scbot = y;
            if (sctop > y) sctop = y;
            impord.add(pls[i]);
        }
        for (Iterator i = impord.iterator(); i.hasNext() && rng.size() != 0; ) {
            Placeable p = (Placeable) i.next();
            int yc = (int) p.pos[1];
            int h = p.buoy.getPrefHeight();
            int w = p.buoy.getPrefWidth();
            int ubound = -1, lbound = -1;
            SortedMap mp = rng.headMap(new Integer(yc));
            if (mp.size() == 0) continue;
            lbound = ((Integer) mp.get(mp.lastKey())).intValue();
            if (lbound > yc) {
                yc = lbound + h / 2;
            }
            mp = rng.tailMap(new Integer(yc));
            if (mp.size() == 0) continue;
            ubound = ((Integer) mp.firstKey()).intValue();
            if (ubound - lbound + 1 > h) {
                ubound = yc + (h - 1) / 2;
                lbound = ubound + 1 - h;
            }
            if ((ubound - lbound + 1 < h || sea.width < w) && p.buoy.constAspectScalable()) {
                int rw = sea.width, rh = ubound - lbound + 1;
                if (rw > rh * w / h) rw = rh * w / h; else rh = rw * h / w;
                w = rw;
                h = rh;
            }
            rng.put(new Integer(lbound), new Integer(ubound));
            int x = sea.x, y = lbound;
            p.buoy.put(seafloor, x, y, w, h);
        }
    }
}
