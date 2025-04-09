package org.vous.facelib.filters;

import java.util.Random;
import org.vous.facelib.bitmap.Bitmap;

public class JitterFilter implements IFilter {

    private int mLevel;

    private Random mRand;

    public JitterFilter(int level) {
        mLevel = level;
        mRand = new Random();
    }

    @Override
    public Bitmap apply(Bitmap source) {
        Bitmap dest = source.clone();
        int w = dest.getWidth();
        int h = dest.getHeight();
        for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) {
            int px = dest.getPixel(x, y);
            int l = mRand.nextInt(mLevel + 1);
            int nx = x + l;
            if (nx >= w) nx = w - 1;
            int ny = y + l;
            if (ny >= h) ny = h - 1;
            dest.setPixel(nx, ny, px);
        }
        return dest;
    }

    public int[] relocate(int x, int y, int[] pixels, int level) {
        return null;
    }
}
