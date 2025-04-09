package foucault.model;

import foucault.utils.PropPersistentField;

public class Zone {

    @PropPersistentField()
    public double radiusStart;

    @PropPersistentField()
    public double radiusEnd;

    @PropPersistentField()
    public double radius;

    @PropPersistentField()
    public boolean used = true;

    public Zone() {
    }

    public Zone(double radiusStart, double radiusEnd) {
        this.radiusStart = radiusStart;
        this.radiusEnd = radiusEnd;
        this.radius = (radiusStart + radiusEnd) / 2;
    }
}
