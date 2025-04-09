package org.waveprotocol.wave.client.paging;

/**
 * Region implementation.
 *
 */
public final class RegionImpl implements Region {

    private double start;

    private double end;

    private RegionImpl(double start, double end) {
        set(start, end);
    }

    public static RegionImpl at(double start, double end) {
        return new RegionImpl(start, end);
    }

    public static RegionImpl at(Region content) {
        return new RegionImpl(content.getStart(), content.getEnd());
    }

    @Override
    public double getStart() {
        return start;
    }

    @Override
    public double getEnd() {
        return end;
    }

    public double getSize() {
        return end - start;
    }

    public void moveStart(double distance) {
        start += distance;
    }

    public void moveEnd(double distance) {
        end += distance;
    }

    public RegionImpl moveBy(double distance) {
        moveStart(distance);
        moveEnd(distance);
        return this;
    }

    public RegionImpl set(double start, double end) {
        if (end < start) {
            throw new IllegalArgumentException("start: " + start + ", end: " + end);
        }
        this.start = start;
        this.end = end;
        return this;
    }

    public RegionImpl scale(double scale) {
        double oldHeight = end - start;
        double mid = (start + end) / 2;
        double newHeight = oldHeight * scale;
        this.start = mid - newHeight / 2;
        this.end = mid + newHeight / 2;
        return this;
    }

    public RegionImpl set(Region region) {
        return set(region.getStart(), region.getEnd());
    }

    @Override
    public String toString() {
        return "{start: " + start + "; end: " + end + "}";
    }
}
