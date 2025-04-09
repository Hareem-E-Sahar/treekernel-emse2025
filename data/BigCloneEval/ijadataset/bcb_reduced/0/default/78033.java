public class ProductTriplet {

    private long countTriplets2(int minx, int maxx, int miny, int maxy, int minz, int maxz) {
        long count = 0;
        if (maxy - miny < maxx - minx) {
            int temp = miny;
            miny = minx;
            minx = temp;
            temp = maxy;
            maxy = maxx;
            maxx = temp;
        }
        for (int x = minx; x <= maxx; x++) {
            int minAllowedY = (minz + x - 1) / x;
            int maxAllowedY = maxz / x;
            if (maxAllowedY < miny) break;
            if (minAllowedY > maxy) continue;
            minAllowedY = minAllowedY > miny ? minAllowedY : miny;
            maxAllowedY = maxAllowedY < maxy ? maxAllowedY : maxy;
            count += maxAllowedY - minAllowedY + 1;
        }
        return count;
    }

    public long countTriplets(int minx, int maxx, int miny, int maxy, int minz, int maxz) {
        if (minx > maxx || miny > maxy || (long) minx * miny > maxz || (long) maxx * maxy < minz) return 0;
        if ((long) minx * miny >= minz && (long) maxx * maxy <= maxz) return (long) (maxx - minx + 1) * (maxy - miny + 1);
        if ((maxx - minx < 10000) && (maxy - miny < 10000)) return countTriplets2(minx, maxx, miny, maxy, minz, maxz);
        long count = 0;
        int midx = (minx + maxx) / 2;
        int midy = (miny + maxy) / 2;
        count += countTriplets(minx, midx, miny, midy, minz, maxz) + countTriplets(midx + 1, maxx, miny, midy, minz, maxz) + countTriplets(minx, midx, midy + 1, maxy, minz, maxz) + countTriplets(midx + 1, maxx, midy + 1, maxy, minz, maxz);
        return count;
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        System.out.println(new ProductTriplet().countTriplets(3, 999999999, 3, 999999999, 3, 999999999));
    }
}
