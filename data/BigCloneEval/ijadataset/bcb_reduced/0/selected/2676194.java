package org.waveprotocol.wave.client.scroll;

/**
 * An immutable, one-dimensional region.
 *
 * @author hearnden@google.com (David Hearnden)
 */
public final class Extent {

    private final double start;

    private final double end;

    private Extent(double start, double end) {
        this.start = start;
        this.end = end;
    }

    public static Extent of(double start, double end) {
        return new Extent(start, end);
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public double getSize() {
        return end - start;
    }

    public Extent scale(double s) {
        double mid = (end + start) / 2;
        double half = s * (end - start) / 2;
        return Extent.of(mid - half, mid + half);
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }
}
