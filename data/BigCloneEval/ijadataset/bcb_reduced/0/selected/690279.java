package org.ibex.graphics;

import java.util.Vector;

/** an abstract path; may contain splines and arcs */
public class Path {

    public static final float PX_PER_INCH = 72;

    public static final float INCHES_PER_CM = (float) 0.3937;

    public static final float INCHES_PER_MM = INCHES_PER_CM / 10;

    private static final int DEFAULT_PATHLEN = 1000;

    private static final float PI = (float) Math.PI;

    int numvertices = 0;

    float[] x = new float[DEFAULT_PATHLEN];

    float[] y = new float[DEFAULT_PATHLEN];

    byte[] type = new byte[DEFAULT_PATHLEN];

    float[] c1x = new float[DEFAULT_PATHLEN];

    float[] c1y = new float[DEFAULT_PATHLEN];

    float[] c2x = new float[DEFAULT_PATHLEN];

    float[] c2y = new float[DEFAULT_PATHLEN];

    boolean closed = false;

    static final byte TYPE_MOVETO = 0;

    static final byte TYPE_LINETO = 1;

    static final byte TYPE_ARCTO = 2;

    static final byte TYPE_CUBIC = 3;

    static final byte TYPE_QUADRADIC = 4;

    public static Path parse(String s) {
        return Tokenizer.parse(s);
    }

    public static class Tokenizer {

        String s;

        int i = 0;

        char lastCommand = 'M';

        public Tokenizer(String s) {
            this.s = s;
        }

        public static Path parse(String s) {
            if (s == null) return null;
            Tokenizer t = new Tokenizer(s);
            Path ret = new Path();
            char last_command = 'M';
            boolean first = true;
            while (t.hasMoreTokens()) {
                char command = t.parseCommand();
                if (first && command != 'M') throw new RuntimeException("the first command of a path must be 'M'");
                first = false;
                boolean relative = Character.toLowerCase(command) == command;
                command = Character.toLowerCase(command);
                ret.parseSingleCommandAndArguments(t, command, relative);
                last_command = command;
            }
            return ret;
        }

        private void consumeWhitespace() {
            while (i < s.length() && (Character.isWhitespace(s.charAt(i)))) i++;
            if (i < s.length() && s.charAt(i) == ',') i++;
            while (i < s.length() && (Character.isWhitespace(s.charAt(i)))) i++;
        }

        public boolean hasMoreTokens() {
            consumeWhitespace();
            return i < s.length();
        }

        public char parseCommand() {
            consumeWhitespace();
            char c = s.charAt(i);
            if (!Character.isLetter(c)) return lastCommand;
            i++;
            return lastCommand = c;
        }

        public float parseFloat() {
            consumeWhitespace();
            int start = i;
            float multiplier = 1;
            for (; i < s.length(); i++) {
                char c = s.charAt(i);
                if (Character.isWhitespace(c) || c == ',' || (c == '-' && i != start)) break;
                if (!((c >= '0' && c <= '9') || c == '.' || c == 'e' || c == 'E' || c == '-')) {
                    if (c == '%') {
                    } else if (s.regionMatches(i, "pt", 0, i + 2)) {
                    } else if (s.regionMatches(i, "em", 0, i + 2)) {
                    } else if (s.regionMatches(i, "pc", 0, i + 2)) {
                    } else if (s.regionMatches(i, "ex", 0, i + 2)) {
                    } else if (s.regionMatches(i, "mm", 0, i + 2)) {
                        i += 2;
                        multiplier = INCHES_PER_MM * PX_PER_INCH;
                        break;
                    } else if (s.regionMatches(i, "cm", 0, i + 2)) {
                        i += 2;
                        multiplier = INCHES_PER_CM * PX_PER_INCH;
                        break;
                    } else if (s.regionMatches(i, "in", 0, i + 2)) {
                        i += 2;
                        multiplier = PX_PER_INCH;
                        break;
                    } else if (s.regionMatches(i, "px", 0, i + 2)) {
                        i += 2;
                        break;
                    } else if (Character.isLetter(c)) break;
                    throw new RuntimeException("didn't expect character \"" + c + "\" in a numeric constant");
                }
            }
            if (start == i) throw new RuntimeException("FIXME");
            return Float.parseFloat(s.substring(start, i)) * multiplier;
        }
    }

    /** Creates a concrete vector path transformed through the given matrix. */
    public Raster realize(Affine a) {
        Raster ret = new Raster();
        int NUMSTEPS = 5;
        ret.numvertices = 1;
        ret.x[0] = (int) Math.round(a.multiply_px(x[0], y[0]));
        ret.y[0] = (int) Math.round(a.multiply_py(x[0], y[0]));
        for (int i = 1; i < numvertices; i++) {
            if (type[i] == TYPE_LINETO) {
                float rx = x[i];
                float ry = y[i];
                ret.x[ret.numvertices] = (int) Math.round(a.multiply_px(rx, ry));
                ret.y[ret.numvertices] = (int) Math.round(a.multiply_py(rx, ry));
                ret.edges[ret.numedges++] = ret.numvertices - 1;
                ret.numvertices++;
            } else if (type[i] == TYPE_MOVETO) {
                float rx = x[i];
                float ry = y[i];
                ret.x[ret.numvertices] = (int) Math.round(a.multiply_px(rx, ry));
                ret.y[ret.numvertices] = (int) Math.round(a.multiply_py(rx, ry));
                ret.numvertices++;
            } else if (type[i] == TYPE_ARCTO) {
                float rx = c1x[i];
                float ry = c1y[i];
                float phi = c2x[i];
                float fa = ((int) c2y[i]) >> 1;
                float fs = ((int) c2y[i]) & 1;
                float x1 = x[i];
                float y1 = y[i];
                float x2 = x[i + 1];
                float y2 = y[i + 1];
                float x1_ = (float) Math.cos(phi) * (x1 - x2) / 2 + (float) Math.sin(phi) * (y1 - y2) / 2;
                float y1_ = -1 * (float) Math.sin(phi) * (x1 - x2) / 2 + (float) Math.cos(phi) * (y1 - y2) / 2;
                float tmp = (float) Math.sqrt((rx * rx * ry * ry - rx * rx * y1_ * y1_ - ry * ry * x1_ * x1_) / (rx * rx * y1_ * y1_ + ry * ry * x1_ * x1_));
                float cx_ = (fa == fs ? -1 : 1) * tmp * (rx * y1_ / ry);
                float cy_ = (fa == fs ? -1 : 1) * -1 * tmp * (ry * x1_ / rx);
                float cx = (float) Math.cos(phi) * cx_ - (float) Math.sin(phi) * cy_ + (x1 + x2) / 2;
                float cy = (float) Math.sin(phi) * cx_ + (float) Math.cos(phi) * cy_ + (y1 + y2) / 2;
                float ux = 1, uy = 0, vx = (x1_ - cx_) / rx, vy = (y1_ - cy_) / ry;
                float det = ux * vy - uy * vx;
                float theta1 = (det < 0 ? -1 : 1) * (float) Math.acos((ux * vx + uy * vy) / ((float) Math.sqrt(ux * ux + uy * uy) * (float) Math.sqrt(vx * vx + vy * vy)));
                ux = (x1_ - cx_) / rx;
                uy = (y1_ - cy_) / ry;
                vx = (-1 * x1_ - cx_) / rx;
                vy = (-1 * y1_ - cy_) / ry;
                det = ux * vy - uy * vx;
                float dtheta = (det < 0 ? -1 : 1) * (float) Math.acos((ux * vx + uy * vy) / ((float) Math.sqrt(ux * ux + uy * uy) * (float) Math.sqrt(vx * vx + vy * vy)));
                dtheta = dtheta % (float) (2 * Math.PI);
                if (fs == 0 && dtheta > 0) theta1 -= 2 * PI;
                if (fs == 1 && dtheta < 0) theta1 += 2 * PI;
                if (fa == 1 && dtheta < 0) dtheta = 2 * PI + dtheta; else if (fa == 1 && dtheta > 0) dtheta = -1 * (2 * PI - dtheta);
                float theta = theta1;
                for (int j = 0; j < NUMSTEPS; j++) {
                    float rasterx = rx * (float) Math.cos(theta) * (float) Math.cos(phi) - ry * (float) Math.sin(theta) * (float) Math.sin(phi) + cx;
                    float rastery = rx * (float) Math.cos(theta) * (float) Math.sin(phi) + ry * (float) Math.cos(phi) * (float) Math.sin(theta) + cy;
                    ret.x[ret.numvertices] = (int) Math.round(a.multiply_px(rasterx, rastery));
                    ret.y[ret.numvertices] = (int) Math.round(a.multiply_py(rasterx, rastery));
                    ret.edges[ret.numedges++] = ret.numvertices - 1;
                    ret.numvertices++;
                    theta += dtheta / NUMSTEPS;
                }
            } else if (type[i] == TYPE_CUBIC) {
                float ax = x[i + 1] - 3 * c2x[i] + 3 * c1x[i] - x[i];
                float bx = 3 * c2x[i] - 6 * c1x[i] + 3 * x[i];
                float cx = 3 * c1x[i] - 3 * x[i];
                float dx = x[i];
                float ay = y[i + 1] - 3 * c2y[i] + 3 * c1y[i] - y[i];
                float by = 3 * c2y[i] - 6 * c1y[i] + 3 * y[i];
                float cy = 3 * c1y[i] - 3 * y[i];
                float dy = y[i];
                for (float t = 0; t < 1; t += 1 / (float) NUMSTEPS) {
                    float rx = ax * t * t * t + bx * t * t + cx * t + dx;
                    float ry = ay * t * t * t + by * t * t + cy * t + dy;
                    ret.x[ret.numvertices] = (int) Math.round(a.multiply_px(rx, ry));
                    ret.y[ret.numvertices] = (int) Math.round(a.multiply_py(rx, ry));
                    ret.edges[ret.numedges++] = ret.numvertices - 1;
                    ret.numvertices++;
                }
            } else if (type[i] == TYPE_QUADRADIC) {
                float bx = x[i + 1] - 2 * c1x[i] + x[i];
                float cx = 2 * c1x[i] - 2 * x[i];
                float dx = x[i];
                float by = y[i + 1] - 2 * c1y[i] + y[i];
                float cy = 2 * c1y[i] - 2 * y[i];
                float dy = y[i];
                for (float t = 0; t < 1; t += 1 / (float) NUMSTEPS) {
                    float rx = bx * t * t + cx * t + dx;
                    float ry = by * t * t + cy * t + dy;
                    ret.x[ret.numvertices] = (int) Math.round(a.multiply_px(rx, ry));
                    ret.y[ret.numvertices] = (int) Math.round(a.multiply_py(rx, ry));
                    ret.edges[ret.numedges++] = ret.numvertices - 1;
                    ret.numvertices++;
                }
            }
        }
        if (ret.numedges > 0) ret.sort(0, ret.numedges - 1, false);
        return ret;
    }

    protected void parseSingleCommandAndArguments(Tokenizer t, char command, boolean relative) {
        if (numvertices == 0 && command != 'm') throw new RuntimeException("first command MUST be an 'm'");
        if (numvertices > x.length - 2) {
            float[] new_x = new float[x.length * 2];
            System.arraycopy(x, 0, new_x, 0, x.length);
            x = new_x;
            float[] new_y = new float[y.length * 2];
            System.arraycopy(y, 0, new_y, 0, y.length);
            y = new_y;
        }
        switch(command) {
            case 'z':
                {
                    int where;
                    type[numvertices - 1] = TYPE_LINETO;
                    for (where = numvertices - 1; where > 0; where--) if (type[where - 1] == TYPE_MOVETO) break;
                    x[numvertices] = x[where];
                    y[numvertices] = y[where];
                    numvertices++;
                    closed = true;
                    break;
                }
            case 'm':
                {
                    if (numvertices > 0) type[numvertices - 1] = TYPE_MOVETO;
                    x[numvertices] = t.parseFloat() + (relative ? x[numvertices - 1] : 0);
                    y[numvertices] = t.parseFloat() + (relative ? y[numvertices - 1] : 0);
                    numvertices++;
                    break;
                }
            case 'l':
            case 'h':
            case 'v':
                {
                    type[numvertices - 1] = TYPE_LINETO;
                    float first = t.parseFloat(), second;
                    if (command == 'h') {
                        second = relative ? 0 : y[numvertices - 1];
                    } else if (command == 'v') {
                        second = first;
                        first = relative ? 0 : x[numvertices - 1];
                    } else {
                        second = t.parseFloat();
                    }
                    x[numvertices] = first + (relative ? x[numvertices - 1] : 0);
                    y[numvertices] = second + (relative ? y[numvertices - 1] : 0);
                    numvertices++;
                    break;
                }
            case 'a':
                {
                    type[numvertices - 1] = TYPE_ARCTO;
                    c1x[numvertices - 1] = t.parseFloat() + (relative ? x[numvertices - 1] : 0);
                    c1y[numvertices - 1] = t.parseFloat() + (relative ? y[numvertices - 1] : 0);
                    c2x[numvertices - 1] = (t.parseFloat() / 360) * 2 * PI;
                    c2y[numvertices - 1] = (((int) t.parseFloat()) << 1) | (int) t.parseFloat();
                    x[numvertices] = t.parseFloat() + (relative ? x[numvertices - 1] : 0);
                    y[numvertices] = t.parseFloat() + (relative ? y[numvertices - 1] : 0);
                    numvertices++;
                    break;
                }
            case 's':
            case 'c':
                {
                    type[numvertices - 1] = TYPE_CUBIC;
                    if (command == 'c') {
                        c1x[numvertices - 1] = t.parseFloat() + (relative ? x[numvertices - 1] : 0);
                        c1y[numvertices - 1] = t.parseFloat() + (relative ? y[numvertices - 1] : 0);
                    } else if (numvertices > 1 && type[numvertices - 2] == TYPE_CUBIC) {
                        c1x[numvertices - 1] = 2 * x[numvertices - 1] - c2x[numvertices - 2];
                        c1y[numvertices - 1] = 2 * y[numvertices - 1] - c2y[numvertices - 2];
                    } else {
                        c1x[numvertices - 1] = x[numvertices - 1];
                        c1y[numvertices - 1] = y[numvertices - 1];
                    }
                    c2x[numvertices - 1] = t.parseFloat() + (relative ? x[numvertices - 1] : 0);
                    c2y[numvertices - 1] = t.parseFloat() + (relative ? y[numvertices - 1] : 0);
                    x[numvertices] = t.parseFloat() + (relative ? x[numvertices - 1] : 0);
                    y[numvertices] = t.parseFloat() + (relative ? y[numvertices - 1] : 0);
                    numvertices++;
                    break;
                }
            case 't':
            case 'q':
                {
                    type[numvertices - 1] = TYPE_QUADRADIC;
                    if (command == 'q') {
                        c1x[numvertices - 1] = t.parseFloat() + (relative ? x[numvertices - 1] : 0);
                        c1y[numvertices - 1] = t.parseFloat() + (relative ? y[numvertices - 1] : 0);
                    } else if (numvertices > 1 && type[numvertices - 2] == TYPE_QUADRADIC) {
                        c1x[numvertices - 1] = 2 * x[numvertices - 1] - c1x[numvertices - 2];
                        c1y[numvertices - 1] = 2 * y[numvertices - 1] - c1y[numvertices - 2];
                    } else {
                        c1x[numvertices - 1] = x[numvertices - 1];
                        c1y[numvertices - 1] = y[numvertices - 1];
                    }
                    x[numvertices] = t.parseFloat() + (relative ? x[numvertices - 1] : 0);
                    y[numvertices] = t.parseFloat() + (relative ? y[numvertices - 1] : 0);
                    numvertices++;
                    break;
                }
            default:
        }
    }

    /** a vector path */
    public static class Raster {

        int[] x = new int[DEFAULT_PATHLEN];

        int[] y = new int[DEFAULT_PATHLEN];

        int numvertices = 0;

        /**
         *  A list of the vertices on this path which *start* an *edge* (rather than a moveto), sorted by increasing y.
         *  example: x[edges[1]],y[edges[1]] - x[edges[i]+1],y[edges[i]+1] is the second-topmost edge
         *  note that if x[i],y[i] - x[i+1],y[i+1] is a MOVETO, then no element in edges will be equal to i
         */
        int[] edges = new int[DEFAULT_PATHLEN];

        int numedges = 0;

        /** translate a rasterized path */
        public void translate(int dx, int dy) {
            for (int i = 0; i < numvertices; i++) {
                x[i] += dx;
                y[i] += dy;
            }
        }

        /** simple quicksort, from http://sourceforge.net/snippet/detail.php?type=snippet&id=100240 */
        int sort(int left, int right, boolean partition) {
            if (partition) {
                int i, j, middle;
                middle = (left + right) / 2;
                int s = edges[right];
                edges[right] = edges[middle];
                edges[middle] = s;
                for (i = left - 1, j = right; ; ) {
                    while (y[edges[++i]] < y[edges[right]]) ;
                    while (j > left && y[edges[--j]] > y[edges[right]]) ;
                    if (i >= j) break;
                    s = edges[i];
                    edges[i] = edges[j];
                    edges[j] = s;
                }
                s = edges[right];
                edges[right] = edges[i];
                edges[i] = s;
                return i;
            } else {
                if (left >= right) return 0;
                int p = sort(left, right, true);
                sort(left, p - 1, false);
                sort(p + 1, right, false);
                return 0;
            }
        }

        /** finds the x value at which the line intercepts the line y=_y */
        private int intercept(int i, float _y, boolean includeTop, boolean includeBottom) {
            if (includeTop ? (_y < Math.min(y[i], y[i + 1])) : (_y <= Math.min(y[i], y[i + 1]))) return Integer.MIN_VALUE;
            if (includeBottom ? (_y > Math.max(y[i], y[i + 1])) : (_y >= Math.max(y[i], y[i + 1]))) return Integer.MIN_VALUE;
            return (int) Math.round((((float) (x[i + 1] - x[i])) / ((float) (y[i + 1] - y[i]))) * ((float) (_y - y[i])) + x[i]);
        }

        /** fill the interior of the path */
        public void fill(PixelBuffer buf, Paint paint) {
            if (numedges == 0) return;
            int y0 = y[edges[0]], y1 = y0;
            boolean useEvenOdd = false;
            for (int index = 1; index < numedges; index++) {
                int count = 0;
                y0 = y1;
                y1 = y[edges[index]];
                if (y0 == y1) continue;
                int x0 = Integer.MIN_VALUE;
                int leftSegment = -1;
                while (true) {
                    int x1 = Integer.MAX_VALUE;
                    int rightSegment = Integer.MAX_VALUE;
                    for (int i = 0; i < numedges; i++) {
                        if (y[edges[i]] == y[edges[i] + 1]) continue;
                        int i0 = intercept(edges[i], y0, true, false);
                        int i1 = intercept(edges[i], y1, false, true);
                        if (i0 == Integer.MIN_VALUE || i1 == Integer.MIN_VALUE) continue;
                        int midpoint = i0 + i1;
                        if (midpoint < x0) continue;
                        if (midpoint == x0 && i <= leftSegment) continue;
                        if (midpoint > x1) continue;
                        if (midpoint == x1 && i >= rightSegment) continue;
                        rightSegment = i;
                        x1 = midpoint;
                    }
                    if (leftSegment == rightSegment || rightSegment == Integer.MAX_VALUE) break;
                    if (leftSegment != -1) if ((useEvenOdd && count % 2 != 0) || (!useEvenOdd && count != 0)) paint.fillTrapezoid(intercept(edges[leftSegment], y0, true, true), intercept(edges[rightSegment], y0, true, true), y0, intercept(edges[leftSegment], y1, true, true), intercept(edges[rightSegment], y1, true, true), y1, buf);
                    if (useEvenOdd) count++; else count += (y[edges[rightSegment]] < y[edges[rightSegment] + 1]) ? -1 : 1;
                    leftSegment = rightSegment;
                    x0 = x1;
                }
            }
        }

        /** stroke the outline of the path */
        public void stroke(PixelBuffer buf, int width, int color) {
            stroke(buf, width, color, null, 0, 0);
        }

        public void stroke(PixelBuffer buf, int width, int color, String dashArray, int dashOffset, float segLength) {
            if (dashArray == null) {
                for (int i = 0; i < numedges; i++) buf.drawLine((int) x[edges[i]], (int) y[edges[i]], (int) x[edges[i] + 1], (int) y[edges[i] + 1], width, color, false);
                return;
            }
            float ratio = 1;
            if (segLength > 0) {
                float actualLength = 0;
                for (int i = 0; i < numvertices; i++) {
                    if (x[i] == x[i + 1] && y[i] == y[i + 1]) continue;
                    if (x[i + 1] == x[i + 2] && y[i + 1] == y[i + 2]) continue;
                    int x1 = x[i];
                    int x2 = x[i + 1];
                    int y1 = y[i];
                    int y2 = y[i + 1];
                    actualLength += java.lang.Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
                }
                ratio = actualLength / segLength;
            }
            Tokenizer pt = new Tokenizer(dashArray);
            Vector v = new Vector();
            while (pt.hasMoreTokens()) v.addElement(new Float(pt.parseFloat()));
            float[] dashes = new float[v.size() % 2 == 0 ? v.size() : 2 * v.size()];
            for (int i = 0; i < dashes.length; i++) dashes[i] = ((Float) v.elementAt(i % v.size())).floatValue();
            int dashpos = dashOffset;
            boolean on = dashpos % 2 == 0;
            for (int i = 0; i < numvertices; i++) {
                if (x[i] == x[i + 1] && y[i] == y[i + 1]) continue;
                if (x[i + 1] == x[i + 2] && y[i + 1] == y[i + 2]) continue;
                int x1 = (int) x[i];
                int x2 = (int) x[i + 1];
                int y1 = (int) y[i];
                int y2 = (int) y[i + 1];
                float segmentLength = (float) java.lang.Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
                int _x1 = x1, _y1 = y1;
                float pos = 0;
                do {
                    pos = Math.min(segmentLength, pos + dashes[dashpos] * ratio);
                    if (pos != segmentLength) dashpos = (dashpos + 1) % dashes.length;
                    int _x2 = (int) ((x2 * pos + x1 * (segmentLength - pos)) / segmentLength);
                    int _y2 = (int) ((y2 * pos + y1 * (segmentLength - pos)) / segmentLength);
                    if (on) buf.drawLine(_x1, _y1, _x2, _y2, width, color, false);
                    on = !on;
                    _x1 = _x2;
                    _y1 = _y2;
                } while (pos < segmentLength);
            }
        }

        public int boundingBoxWidth() {
            int ret = 0;
            for (int i = 0; i < numvertices; i++) ret = Math.max(ret, x[i]);
            return ret;
        }

        public int boundingBoxHeight() {
            int ret = 0;
            for (int i = 0; i < numvertices; i++) ret = Math.max(ret, y[i]);
            return ret;
        }
    }
}
