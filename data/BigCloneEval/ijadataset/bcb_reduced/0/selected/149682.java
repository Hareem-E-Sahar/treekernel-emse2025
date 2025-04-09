package mindtct;

/***********************************************************************
      LIBRARY: LFS - NIST Latent Fingerprint System

      FILE:    UTIL.C
      AUTHOR:  Michael D. Garris
      DATE:    03/16/1999

      Contains general support routines required by the NIST
      Latent Fingerprint System (LFS).

***********************************************************************
               ROUTINES:
                        maxv()
                        minv()
                        minmaxs()
                        distance()
                        squared_distance()
                        in_int_list()
                        remove_from_int_list()
                        find_incr_position_dbl()
                        angle2line()
                        line2direction()
                        closest_dir_dist()
***********************************************************************/
public class Util {

    /*************************************************************************
	**************************************************************************
	#cat: maxv - Determines the maximum value in the given list of integers.
	#cat:        NOTE, the list is assumed to be NOT empty!

	   Input:
	      list - non-empty list of integers to be searched
	      num  - number of integers in the list
	   Return Code:
	      Maximum - maximum value in the list
	**************************************************************************/
    public static int maxv(final int[] list, final int num) {
        int i;
        int maxval;
        maxval = list[0];
        for (i = 1; i < num; i++) {
            if (list[i] > maxval) maxval = list[i];
        }
        return (maxval);
    }

    /*************************************************************************
	**************************************************************************
	#cat: minv - Determines the minimum value in the given list of integers.
	#cat:        NOTE, the list is assumed to be NOT empty!

	   Input:
	      list - non-empty list of integers to be searched
	      num  - number of integers in the list
	   Return Code:
	      Minimum - minimum value in the list
	**************************************************************************/
    public static int minv(final int[] list, final int num) {
        int i;
        int minval;
        minval = list[0];
        for (i = 1; i < num; i++) {
            if (list[i] < minval) minval = list[i];
        }
        return (minval);
    }

    public static enum MinMaxType {

        MINIMA, MAXIMA
    }

    ;

    public static class MinMaxResult {

        public final int minmax_alloc, minmax_num;

        public final int[] minmax_val, minmax_i;

        public final MinMaxType[] minmax_type;

        public MinMaxResult(int minmax_alloc, int minmax_num, int[] minmax_i, MinMaxType[] minMaxTypes, int[] minmax_val) {
            super();
            this.minmax_alloc = minmax_alloc;
            this.minmax_i = minmax_i;
            this.minmax_num = minmax_num;
            this.minmax_type = minMaxTypes;
            this.minmax_val = minmax_val;
        }

        public static final MinMaxResult NOT_ENOUGH_INPUT_ITEMS = new MinMaxResult(0, 0, new int[0], new MinMaxType[0], new int[0]);
    }

    /*************************************************************************
	**************************************************************************
	#cat: minmaxs - Takes a list of integers and identifies points of relative
	#cat:           minima and maxima.  The midpoint of flat plateaus and valleys
	#cat:           are selected when they are detected.

	   Input:
	      items     - list of integers to be analyzed
	      num       - number of items in the list
	   Output:
	      ominmax_val   - value of the item at each minima or maxima
	      ominmax_type  - identifies a minima as '-1' and maxima as '1'
	      ominmax_i     - index of item's position in list
	      ominmax_alloc - number of allocated minima and/or maxima
	      ominmax_num   - number of detected minima and/or maxima
	   Return Code:
	      Zero     - successful completion
	      Negative - system error
	**************************************************************************/
    public static MinMaxResult minmaxs(final int[] items, final int num) {
        if (num < 3) {
            return (MinMaxResult.NOT_ENOUGH_INPUT_ITEMS);
        }
        final int minmax_alloc = num - 2;
        final int[] minmax_val = new int[minmax_alloc];
        MinMaxType[] minmax_type = new MinMaxType[minmax_alloc];
        int[] minmax_i = new int[minmax_alloc];
        int minmax_num = 0;
        int i = 0;
        int diff = items[1] - items[0];
        int state;
        if (diff > 0) state = 1; else if (diff < 0) state = -1; else state = 0;
        int start = 0;
        i++;
        while (i < num - 1) {
            diff = items[i + 1] - items[i];
            if (diff > 0) {
                if (state == 1) {
                    start = i;
                } else if (state == -1) {
                    int loc = (start + i) / 2;
                    minmax_val[minmax_num] = items[loc];
                    minmax_type[minmax_num] = MinMaxType.MINIMA;
                    minmax_i[minmax_num++] = loc;
                    state = 1;
                    start = i;
                } else {
                    if (i - start > 1) {
                        int loc = (start + i) / 2;
                        minmax_val[minmax_num] = items[loc];
                        minmax_type[minmax_num] = MinMaxType.MINIMA;
                        minmax_i[minmax_num++] = loc;
                        state = 1;
                        start = i;
                    } else {
                        state = 1;
                        start = i;
                    }
                }
            } else if (diff < 0) {
                if (state == -1) {
                    start = i;
                } else if (state == 1) {
                    int loc = (start + i) / 2;
                    minmax_val[minmax_num] = items[loc];
                    minmax_type[minmax_num] = MinMaxType.MAXIMA;
                    minmax_i[minmax_num++] = loc;
                    state = -1;
                    start = i;
                } else {
                    if (i - start > 1) {
                        int loc = (start + i) / 2;
                        minmax_val[minmax_num] = items[loc];
                        minmax_type[minmax_num] = MinMaxType.MAXIMA;
                        minmax_i[minmax_num++] = loc;
                        state = -1;
                        start = i;
                    } else {
                        state = -1;
                        start = i;
                    }
                }
            }
            i++;
        }
        return new MinMaxResult(minmax_alloc, minmax_num, minmax_i, minmax_type, minmax_val);
    }

    /*************************************************************************
	**************************************************************************
	#cat: distance - Takes two coordinate points and computes the
	#cat:            Euclidean distance between the two points.

	   Input:
	      x1  - x-coord of first point
	      y1  - y-coord of first point
	      x2  - x-coord of second point
	      y2  - y-coord of second point
	   Return Code:
	      Distance - computed Euclidean distance
	**************************************************************************/
    public static double distance(final int x1, final int y1, final int x2, final int y2) {
        double dx, dy, dist;
        dx = (double) (x1 - x2);
        dy = (double) (y1 - y2);
        dist = (dx * dx) + (dy * dy);
        dist = Math.sqrt(dist);
        return (dist);
    }

    /*************************************************************************
	**************************************************************************
	#cat: squared_distance - Takes two coordinate points and computes the
	#cat:                    squared distance between the two points.

	   Input:
	      x1  - x-coord of first point
	      y1  - y-coord of first point
	      x2  - x-coord of second point
	      y2  - y-coord of second point
	   Return Code:
	      Distance - computed squared distance
	**************************************************************************/
    public static double squared_distance(final int x1, final int y1, final int x2, final int y2) {
        double dx, dy, dist;
        dx = (double) (x1 - x2);
        dy = (double) (y1 - y2);
        dist = (dx * dx) + (dy * dy);
        return (dist);
    }

    /*************************************************************************
	**************************************************************************
	#cat: in_int_list - Determines if a specified value is store in a list of
	#cat:               integers and returns its location if found.

	   Input:
	      item    - value to search for in list
	      list    - list of integers to be searched
	      len     - number of integers in search list
	   Return Code:
	      Zero or greater - first location found equal to search value
	      Negative        - search value not found in the list of integers
	**************************************************************************/
    public static int in_int_list(final int item, final int[] list, final int len) {
        int i;
        for (i = 0; i < len; i++) {
            if (list[i] == item) return (i);
        }
        return (-1);
    }

    /*************************************************************************
	**************************************************************************
	#cat: remove_from_int_list - Takes a position index into an integer list and
	#cat:                removes the value from the list, collapsing the resulting
	#cat:                list.

	   Input:
	      index      - position of value to be removed from list
	      list       - input list of integers
	      num        - number of integers in the list
	   Output:
	      list       - list with specified integer removed
	      num        - decremented number of integers in list
	   Return Code:
	      Zero      - successful completion
	      Negative  - system error
	**************************************************************************/
    public static void remove_from_int_list(final int index, int[] list, final int num) {
        int fr, to;
        if ((index < 0) && (index >= num)) {
            throw new ArrayIndexOutOfBoundsException(String.format("index=%d, num=%d, list_address=%s", index, num, list));
        }
        for (to = index, fr = index + 1; fr < num; to++, fr++) list[to] = list[fr];
    }

    /*************************************************************************
	**************************************************************************
	#cat: ind_incr_position_dbl - Takes a double value and a list of doubles and
	#cat:               determines where in the list the double may be inserted,
	#cat:               preserving the increasing sorted order of the list.

	   Input:
	      val  - value to be inserted into the list
	      list - list of double in increasing sorted order
	      num  - number of values in the list
	   Return Code:
	      Zero or Positive - insertion position in the list
	**************************************************************************/
    public static int find_incr_position_dbl(final double val, double[] list, final int num) {
        int i;
        for (i = 0; i < num; i++) {
            if (val < list[i]) return (i);
        }
        return (i);
    }

    /*************************************************************************
	**************************************************************************
	#cat: angle2line - Takes two coordinate points and computes the angle
	#cat:            to the line formed by the two points.

	   Input:
	      fx         - x-coord of first point
	      fy         - y-coord of first point
	      tx         - x-coord of second point
	      ty         - y-coord of second point
	   Return Code:
	      Angle - angle to the specified line
	**************************************************************************/
    public static double angle2line(final int fx, final int fy, final int tx, final int ty) {
        double dx, dy, theta;
        dy = (double) (fy - ty);
        dx = (double) (tx - fx);
        if ((Math.abs(dx) < Lfs.MIN_SLOPE_DELTA) && (Math.abs(dy) < Lfs.MIN_SLOPE_DELTA)) theta = 0.0; else {
            theta = Math.atan2(dy, dx);
            throw new UnsupportedOperationException();
        }
        return (theta);
    }

    /*************************************************************************
	**************************************************************************
	#cat: line2direction - Takes two coordinate points and computes the
	#cat:            directon (on a full circle) in which the first points
	#cat:            to the second.

	   Input:
	      fx         - x-coord of first point (pointing from)
	      fy         - y-coord of first point (pointing from)
	      tx         - x-coord of second point (pointing to)
	      ty         - y-coord of second point (pointing to)
	      ndirs      - number of IMAP directions (in semicircle)
	   Return Code:
	      Direction  - determined direction on a "full" circle
	**************************************************************************/
    public static int line2direction(final int fx, final int fy, final int tx, final int ty, final int ndirs) {
        double theta, pi_factor;
        int idir, full_ndirs;
        final double pi2 = Math.PI * 2.0;
        theta = angle2line(ty, tx, fy, fx);
        theta += pi2;
        theta = theta % pi2;
        full_ndirs = ndirs << 1;
        pi_factor = (double) full_ndirs / pi2;
        theta *= pi_factor;
        idir = (int) Math.round(theta);
        idir %= full_ndirs;
        return (idir);
    }

    /*************************************************************************
	**************************************************************************
	#cat: closest_dir_dist - Takes to integer IMAP directions and determines the
	#cat:                    closest distance between them accounting for
	#cat:                    wrap-around either at the beginning or ending of
	#cat:                    the range of directions.

	   Input:
	      dir1  - integer value of the first direction
	      dir2  - integer value of the second direction
	      ndirs - the number of possible directions
	   Return Code:
	      Non-negative - distance between the 2 directions
	**************************************************************************/
    public static int closest_dir_dist(final int dir1, final int dir2, final int ndirs) {
        int d1, d2, dist;
        dist = Lfs.INVALID_DIR;
        if ((dir1 >= 0) && (dir2 >= 0)) {
            d1 = Math.abs(dir2 - dir1);
            d2 = ndirs - d1;
            dist = Math.min(d1, d2);
        }
        return (dist);
    }
}
