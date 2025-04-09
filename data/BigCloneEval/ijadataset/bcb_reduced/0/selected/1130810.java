package l1jDevils.common;

import java.util.Random;

public class StaticUtil {

    private static Random _rnd = new Random();

    public static Random getRnd() {
        return _rnd;
    }

    public static int getRndInt(int range) {
        int result = 0;
        if (range > 0) {
            result = _rnd.nextInt(range);
        }
        return result;
    }

    public static int getRndInt(int beginRange, int endRange) {
        int result = 0;
        if (endRange > beginRange && beginRange > 0) {
            result = beginRange + _rnd.nextInt(endRange - beginRange);
        }
        return result;
    }

    public static long getSysTime() {
        return System.currentTimeMillis();
    }
}
