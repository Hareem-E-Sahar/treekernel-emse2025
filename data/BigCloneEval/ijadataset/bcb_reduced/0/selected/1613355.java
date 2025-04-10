package co.edu.unal.ungrid.image;

import co.edu.unal.ungrid.core.Dimension2D;
import co.edu.unal.ungrid.core.MemoryHelper;
import co.edu.unal.ungrid.image.util.TransposeHelper;

public class DoublePlane extends AbstractPlane {

    public static final long serialVersionUID = 1L;

    public DoublePlane() {
    }

    public DoublePlane(final Dimension2D<Integer> dim) {
        assert dim != null;
        setSize(dim);
        createArray();
    }

    public DoublePlane(final Dimension2D<Integer> dim, final double clear) {
        assert dim != null;
        setSize(dim);
        createArray();
        initArray(clear);
    }

    public DoublePlane(final Dimension2D<Integer> dim, double[] fa) {
        assert dim != null;
        assert dim.getWidth().intValue() > 0;
        assert dim.getHeight().intValue() > 0;
        assert fa != null;
        assert dim.getWidth().intValue() * dim.getHeight().intValue() == fa.length;
        setSize(dim);
        m_data = fa;
    }

    public DoublePlane(final Dimension2D<Integer> dim, int[] na, int offset) {
        assert dim != null;
        assert dim.getWidth().intValue() > 0;
        assert dim.getHeight().intValue() > 0;
        assert na != null;
        assert dim.getWidth().intValue() * dim.getHeight().intValue() - offset <= na.length;
        setSize(dim);
        createArray();
        fromArray(na, offset);
    }

    public DoublePlane(final Dimension2D<Integer> dim, float[] fa, int offset) {
        assert dim != null;
        assert dim.getWidth().intValue() > 0;
        assert dim.getHeight().intValue() > 0;
        assert fa != null;
        assert dim.getWidth().intValue() * dim.getHeight().intValue() - offset <= fa.length;
        setSize(dim);
        createArray();
        fromArray(fa, offset);
    }

    public DoublePlane(final Dimension2D<Integer> dim, double[] fa, int offset) {
        assert dim != null;
        assert dim.getWidth().intValue() > 0;
        assert dim.getHeight().intValue() > 0;
        assert fa != null;
        assert dim.getWidth().intValue() * dim.getHeight().intValue() - offset <= fa.length;
        setSize(dim);
        createArray();
        fromArray(fa, offset);
    }

    public DoublePlane(DoublePlane plane) {
        assert plane != null;
        setSize(plane.getSize());
        createArray();
        System.arraycopy(plane.m_data, 0, m_data, 0, m_data.length);
    }

    protected void createArray() {
        assert 0 < getWidth();
        assert 0 < getHeight();
        m_data = MemoryHelper.createDouble1D(getWidth() * getHeight());
    }

    protected void createArrayIf() {
        if (m_data == null) {
            createArray();
        }
    }

    protected void initArray() {
        initArray(0.0);
    }

    protected void initArray(final double value) {
        assert m_data != null;
        for (int i = 0; i < m_data.length; i++) {
            m_data[i] = value;
        }
        reset();
    }

    public void clear(double value) {
        createArrayIf();
        initArray(value);
    }

    public void set(int idx, double v) {
        assert 0 <= idx && idx < m_data.length;
        m_data[idx] = v;
        reset();
    }

    public double get(int idx) {
        assert m_data != null;
        assert 0 <= idx && idx < m_data.length;
        return m_data[idx];
    }

    public void set(int x, int y, double v) {
        assert m_data != null;
        assert 0 <= x && x < getWidth();
        assert 0 <= y && y < getHeight();
        m_data[x + y * getWidth()] = v;
        reset();
    }

    public double get(int x, int y) {
        assert m_data != null;
        assert 0 <= x && x < getWidth();
        assert 0 <= y && y < getHeight();
        return m_data[x + y * getWidth()];
    }

    public double[] doubleArray() {
        createArrayIf();
        return m_data;
    }

    public byte[] byteArray() {
        createArrayIf();
        byte[] ba = MemoryHelper.createByte1D(m_data.length);
        if (ba != null) {
            for (int i = 0; i < ba.length; i++) {
                ba[i] = (byte) (m_data[i] * GRAY_LEVEL_MAX);
            }
        }
        return ba;
    }

    public float[] floatArray() {
        createArrayIf();
        float[] fa = MemoryHelper.createFloat1D(m_data.length);
        if (fa != null) {
            for (int i = 0; i < fa.length; i++) {
                fa[i] = (float) m_data[i];
            }
        }
        return fa;
    }

    public int[] intArray() {
        createArrayIf();
        int[] ia = MemoryHelper.createInt1D(m_data.length);
        if (ia != null) {
            for (int i = 0; i < ia.length; i++) {
                ia[i] = (int) (m_data[i] * GRAY_LEVEL_MAX);
            }
        }
        return ia;
    }

    public void fromArray(byte[] ba, int offset) {
        assert ba != null;
        assert ba.length - offset >= numPixels() : ba.length + " != " + numPixels();
        createArrayIf();
        for (int i = 0, src = offset; i < m_data.length; i++, src++) {
            m_data[i] = (double) (ba[src] & 0xFF) / GRAY_LEVEL_MAX;
        }
        reset();
    }

    public void copyArray(byte[] ba, int offset) {
        fromArray(ba);
    }

    public void fromArray(int[] ia, int offset) {
        assert ia != null;
        assert ia.length - offset >= numPixels() : ia.length + " != " + numPixels();
        createArrayIf();
        for (int i = 0, src = offset; i < m_data.length; i++, src++) {
            m_data[i] = (double) ia[src] / GRAY_LEVEL_MAX;
        }
        reset();
    }

    public void copyArray(int[] ia, int offset) {
        fromArray(ia, offset);
    }

    public void fromArray(float[] fa, int offset) {
        assert fa != null;
        assert fa.length - offset >= numPixels() : fa.length + " != " + numPixels();
        createArrayIf();
        for (int i = 0, src = offset; i < m_data.length; i++, src++) {
            m_data[i] = fa[src];
        }
        reset();
    }

    public void copyArray(float[] ia, int offset) {
        fromArray(ia, offset);
    }

    public void fromArray(double[] fa, int offset) {
        assert fa != null;
        assert fa.length - offset >= numPixels() : (fa.length - offset) + " < " + numPixels();
        if (offset == 0 && fa.length == numPixels()) {
            m_data = fa;
        } else {
            createArrayIf();
            for (int i = 0, src = offset; i < m_data.length; i++, src++) {
                m_data[i] = fa[src];
            }
        }
        reset();
    }

    public void copyArray(double[] fa, int offset) {
        assert fa != null;
        assert fa.length - offset >= numPixels() : (fa.length - offset) + " < " + numPixels();
        createArrayIf();
        System.arraycopy(fa, offset, m_data, 0, m_data.length);
        reset();
    }

    public void setTile(int x, int y, double[] tile, int tileWidth, int tileHeight) {
        if (x < 0 || x >= getWidth()) throw new IllegalArgumentException("x=" + x);
        if (y < 0 || y >= getHeight()) throw new IllegalArgumentException("y=" + y);
        if (tile == null) throw new IllegalArgumentException("tile=" + tile);
        int iw = getWidth();
        int ih = getHeight();
        double xEnd = x + tileWidth;
        double yEnd = y + tileHeight;
        if (xEnd > iw) xEnd = iw;
        if (yEnd > ih) yEnd = ih;
        for (int yy = y; yy < yEnd; yy++) {
            int ypos = yy * iw;
            int p = (yy - y) * tileWidth;
            for (int xx = x; xx < xEnd; xx++, p++) {
                m_data[xx + ypos] = tile[p];
            }
        }
        reset();
    }

    public DoublePlane getTile(int x, int y, int w, int h) {
        if (x < 0 || x >= getWidth()) throw new IllegalArgumentException("x=" + x);
        if (y < 0 || y >= getHeight()) throw new IllegalArgumentException("y=" + y);
        int iw = getWidth();
        int ih = getHeight();
        int xx = x + w;
        int yy = y + h;
        if (xx > iw) xx = iw;
        if (yy > ih) yy = ih;
        w = xx - x;
        h = yy - y;
        double[] fa = new double[w * h];
        for (int ay = 0, io = y * iw + x; ay < h; ay++, io += iw) {
            System.arraycopy(m_data, io, fa, ay * w, w);
        }
        return new DoublePlane(new Dimension2D<Integer>(w, h), fa);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        int h = getHeight();
        int w = getWidth();
        for (int y = 0, i = 0; y < h; y++) {
            sb.append("[ ");
            if (m_data != null) {
                for (int x = 0; x < w; x++, i++) {
                    sb.append(String.format("%12.3f", m_data[i]) + " ");
                }
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    @Override
    protected void computeMeanValue() {
        int[] na = intArray();
        if (na != null) {
            double fMean = 0.0;
            double nPix = 0;
            for (int i = 0; i < na.length; i++) {
                if (na[i] != RgbImage.OUTLIER) {
                    fMean += na[i];
                    nPix++;
                }
            }
            setMeanValue(fMean / nPix);
        }
    }

    public void trimCorners() {
        if (isTrimmed()) return;
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            System.out.println("IntImage::trimCorners(): w=" + w + " h=" + h);
        }
        int x;
        for (int y = 0; y < h; y++) {
            int ypos = y * w;
            for (x = 0; x < w; x++) {
                int pos = x + ypos;
                if (m_data[pos] == BLACK_CORNER || m_data[pos] > WHITE_CORNER) {
                    m_data[pos] = OUTLIER;
                } else {
                    break;
                }
            }
            if (x == 0) {
                break;
            }
        }
        for (int y = h - 1; y >= 0; y--) {
            int ypos = y * w;
            for (x = 0; x < w; x++) {
                int pos = x + ypos;
                if (m_data[pos] == BLACK_CORNER || m_data[pos] > WHITE_CORNER) {
                    m_data[pos] = OUTLIER;
                } else {
                    break;
                }
            }
            if (x == 0) {
                break;
            }
        }
        for (int y = 0; y < h; y++) {
            int ypos = y * w;
            for (x = w - 1; x >= 0; x--) {
                int pos = x + ypos;
                if (m_data[pos] == BLACK_CORNER || m_data[pos] > WHITE_CORNER) {
                    m_data[pos] = OUTLIER;
                } else {
                    break;
                }
            }
            if (x == w - 1) {
                break;
            }
        }
        for (int y = h - 1; y >= 0; y--) {
            int ypos = y * w;
            for (x = w - 1; x >= 0; x--) {
                int pos = x + ypos;
                if (m_data[pos] == BLACK_CORNER || m_data[pos] > WHITE_CORNER) {
                    m_data[pos] = OUTLIER;
                } else {
                    break;
                }
            }
            if (x == w - 1) {
                break;
            }
        }
        setTrimmed(true);
        reset();
    }

    public void addNoise(double noise) {
        int nPoints = (int) (m_data.length * noise);
        for (int i = 0; i < nPoints; i++) {
            int idx = (int) (m_data.length * Math.random());
            m_data[idx] = 1.0 - m_data[idx];
        }
    }

    public void binarize() {
        assert m_data != null;
        if (m_data != null) {
            for (int p = 0; p < m_data.length; p++) {
                m_data[p] = (m_data[p] < 0.5 ? BLACK : WHITE);
            }
        }
    }

    public void goGray() {
        assert m_data != null;
    }

    public DoublePlane getSubSampledVersion(int subsampling) {
        Dimension2D<Integer> inSize = getSize();
        Dimension2D<Integer> outSize = inSize.clone();
        for (int i = 0; i < outSize.getDims(); i++) {
            outSize.set(i, Math.max(1, outSize.get(i).intValue() / subsampling));
        }
        if (outSize.getWidth().intValue() < MIN_MR_SIZE && outSize.getHeight().intValue() < MIN_MR_SIZE) {
            return null;
        }
        DoublePlane plane = new DoublePlane(outSize);
        double[] data = plane.doubleArray();
        int oh = plane.getHeight();
        int ow = plane.getWidth();
        int iw = inSize.getWidth().intValue();
        int scanline = (subsampling - 1) * iw;
        boolean bIsOddWidth = (iw % 2 > 0);
        for (int y = 0, i = 0, j = 0; y < oh; y++) {
            for (int x = 0; x < ow; x++, j += subsampling, i++) {
                data[i] = m_data[j];
            }
            if (bIsOddWidth) j += 1;
            j += scanline;
        }
        return plane;
    }

    private void center(final double[] srcData, final Dimension2D<Integer> src, final double[] dstData, final Dimension2D<Integer> dst) {
        int sh = src.getHeight().intValue();
        int sw = src.getWidth().intValue();
        int dw = dst.getWidth().intValue();
        int dh = dst.getHeight().intValue();
        int dstX = (dw - sw) / 2;
        int dstY = (dh - sh) / 2;
        int dstStart = (dstY * dw) + dstX;
        for (int y = 0; y < sh; y++) {
            System.arraycopy(srcData, y * sw, dstData, dstStart + (y * dw), sw);
        }
    }

    @Override
    public void center(final AbstractPlane plane) {
        assert plane != null;
        center(plane.doubleArray(), plane.getSize(), doubleArray(), getSize());
    }

    @Override
    public DoublePlane xGradient() {
        DoublePlane plane = new DoublePlane(getSize());
        double[] src = doubleArray();
        double[] dst = plane.doubleArray();
        int w = getWidth();
        int h = getHeight();
        for (int y = 0; y < h; y++) {
            int row = y * w;
            int end = row + w - 1;
            for (int x = row; x < end; x++) {
                dst[x] = src[x + 1] - src[x];
            }
        }
        return plane;
    }

    @Override
    public DoublePlane yGradient() {
        DoublePlane plane = new DoublePlane(getSize());
        double[] src = doubleArray();
        double[] dst = plane.doubleArray();
        int w = getWidth();
        int h = getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h - 1; y++) {
                int zu = x + y * w;
                int zb = zu + w;
                dst[zu] = src[zb] - src[zu] + 0.5;
            }
        }
        return plane;
    }

    public void flipVertical() {
        int w = getWidth();
        int h = getHeight();
        double[] fa = MemoryHelper.createDouble1D(w * h);
        if (fa != null) {
            for (int y = 0, src = 0, dst = w * (h - 1); y < h; y++, src += w, dst -= w) {
                System.arraycopy(m_data, src, fa, dst, w);
            }
        }
        m_data = fa;
    }

    public void flipHorizontal() {
        int w = getWidth();
        int h = getHeight();
        double[] fa = MemoryHelper.createDouble1D(w);
        if (fa != null) {
            for (int y = 0, src = 0; y < h; y++, src += w) {
                System.arraycopy(m_data, src, fa, 0, w);
                for (int x = 0, dst = src + w - 1; x < w; x++, dst--) {
                    m_data[dst] = fa[x];
                }
            }
        }
    }

    @Override
    public void transpose() {
        if (m_data != null) {
            int w = getWidth();
            int h = getHeight();
            if (w == h) {
                for (int y = 0, za = 0; y < h; y++) {
                    for (int x = 0, zb = y; x < w; x++, za++, zb += w) {
                        if (x > y) {
                            double d = m_data[za];
                            m_data[za] = m_data[zb];
                            m_data[zb] = d;
                        }
                    }
                }
            } else {
                m_data = TransposeHelper.transpose(m_data, w, h);
                setWidth(h);
                setHeight(w);
            }
        }
    }

    @Override
    public DoublePlane getTranspose() {
        DoublePlane plane = null;
        if (m_data != null) {
            int w = getWidth();
            int h = getHeight();
            plane = new DoublePlane(new Dimension2D<Integer>(h, w), TransposeHelper.transpose(m_data, w, h));
        }
        return plane;
    }

    @Override
    public void fillFromImage(final AbstractImage<AbstractPlane> ai, final ViewPointConversion tp) {
        assert ai != null;
        assert tp != null;
        if (m_data != null) {
            switch(tp) {
                case CoronalToSagittal:
                    {
                        int w = getWidth();
                        int h = getHeight();
                        assert w == ai.getDepth();
                        assert h == ai.getHeight();
                        double[][][] ba = ai.doubleArray3D();
                        int pos = ai.getWidth() - getIndex() - 1;
                        for (int y = 0, i = 0; y < h; y++) {
                            for (int x = 0; x < w; x++, i++) {
                                m_data[i] = ba[x][y][pos];
                            }
                        }
                    }
                    break;
                case SagittalToCoronal:
                    {
                        int w = getWidth();
                        int h = getHeight();
                        assert w == ai.getDepth();
                        assert h == ai.getHeight();
                        double[][][] ba = ai.doubleArray3D();
                        int pos = getIndex();
                        for (int y = 0, i = 0; y < h; y++) {
                            for (int x = w - 1; x >= 0; x--, i++) {
                                m_data[i] = ba[x][y][pos];
                            }
                        }
                    }
                    break;
                case CoronalToAxial:
                    {
                        int w = getWidth();
                        int h = getHeight();
                        assert w == ai.getWidth();
                        assert h == ai.getDepth();
                        double[][][] ba = ai.doubleArray3D();
                        int zLast = h - 1;
                        int idx = getIndex();
                        for (int y = 0, z = zLast, dst = 0; y < h; y++, z--, dst += w) {
                            System.arraycopy(ba[z][idx], 0, m_data, dst, w);
                        }
                    }
                    break;
                case AxialToCoronal:
                    {
                        int w = getWidth();
                        int h = getHeight();
                        assert w == ai.getWidth();
                        assert h == ai.getDepth();
                        double[][][] ba = ai.doubleArray3D();
                        int idx = ai.getHeight() - getIndex() - 1;
                        for (int y = 0, z = 0, dst = 0; y < h; y++, z++, dst += w) {
                            System.arraycopy(ba[z][idx], 0, m_data, dst, w);
                        }
                    }
                    break;
                case SagittalToAxial:
                    {
                    }
                    break;
            }
        }
    }

    @Override
    public void fillFromImage(final float[][][] fa, final ViewPointConversion tp) {
        assert fa != null;
        assert tp != null;
        System.err.println("Not implemented");
    }

    public int dataLength() {
        return (m_data != null ? m_data.length : 0);
    }

    public static void main(String[] args) {
        DoublePlane plane = new DoublePlane(new Dimension2D<Integer>(5, 10));
        System.out.println(plane);
    }

    private double[] m_data;

    public static final double OUTLIER = -1.0;

    public static final double BLACK = 0.0;

    public static final double WHITE = 1.0;

    public static final double BLACK_CORNER = BLACK;

    public static final double WHITE_CORNER = 220.0 / GRAY_LEVEL_MAX;
}
