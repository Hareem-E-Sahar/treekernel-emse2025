package swogl;

import javax.vecmath.Point3f;

/**
 * A simple BoundingBox
 */
public class BoundingBox {

    /**
     * The minimum x value of this BoundingBox
     */
    private float minX = Float.POSITIVE_INFINITY;

    /**
     * The minimum y value of this BoundingBox
     */
    private float minY = Float.POSITIVE_INFINITY;

    /**
     * The minimum z value of this BoundingBox
     */
    private float minZ = Float.POSITIVE_INFINITY;

    /**
     * The maximum x value of this BoundingBox
     */
    private float maxX = Float.NEGATIVE_INFINITY;

    /**
     * The maximum y value of this BoundingBox
     */
    private float maxY = Float.NEGATIVE_INFINITY;

    /**
     * The maximum z value of this BoundingBox
     */
    private float maxZ = Float.NEGATIVE_INFINITY;

    /**
     * Creates a new BoundingBox
     */
    public BoundingBox() {
    }

    /**
     * Creates a new BoundingBox with the given size
     * 
     * @param minX The minimum x value of this BoundingBox
     * @param minY The minimum y value of this BoundingBox
     * @param minZ The minimum z value of this BoundingBox
     * @param maxX The maximum x value of this BoundingBox
     * @param maxY The maximum y value of this BoundingBox
     * @param maxZ The maximum z value of this BoundingBox
     */
    public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * Creates a new BoundingBox as a copy of the given one
     * 
     * @param other The BoundingBox that serves as input for this
     */
    public BoundingBox(BoundingBox other) {
        combine(other);
    }

    /**
     * Returns the minimum x value of this BoundingBox
     * 
     * @return The minimum x value of this BoundingBox
     */
    public float getMinX() {
        return minX;
    }

    /**
     * Set the minimum x value of this BoundingBox
     * 
     * @param minX The minimum x value of this BoundingBox
     */
    public void setMinX(float minX) {
        this.minX = minX;
    }

    /**
     * Returns the minimum y value of this BoundingBox
     * 
     * @return The minimum y value of this BoundingBox
     */
    public float getMinY() {
        return minY;
    }

    /**
     * Set the minimum y value of this BoundingBox
     * 
     * @param minY The minimum y value of this BoundingBox
     */
    public void setMinY(float minY) {
        this.minY = minY;
    }

    /**
     * Returns the minimum z value of this BoundingBox
     * 
     * @return The minimum z value of this BoundingBox
     */
    public float getMinZ() {
        return minZ;
    }

    /**
     * Set the minimum z value of this BoundingBox
     * 
     * @param minZ The minimum z value of this BoundingBox
     */
    public void setMinZ(float minZ) {
        this.minZ = minZ;
    }

    /**
     * Returns the maximum x value of this BoundingBox
     * 
     * @return The maximum x value of this BoundingBox
     */
    public float getMaxX() {
        return maxX;
    }

    /**
     * Set the maximum x value of this BoundingBox
     * 
     * @param maxX The maximum x value of this BoundingBox
     */
    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    /**
     * Returns the maximum y value of this BoundingBox
     * 
     * @return The maximum y value of this BoundingBox
     */
    public float getMaxY() {
        return maxY;
    }

    /**
     * Set the maximum y value of this BoundingBox
     * 
     * @param maxY The maximum y value of this BoundingBox
     */
    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    /**
     * Returns the maximum z value of this BoundingBox
     * 
     * @return The maximum z value of this BoundingBox
     */
    public float getMaxZ() {
        return maxZ;
    }

    /**
     * Set the maximum z value of this BoundingBox
     * 
     * @param maxZ The maximum z value of this BoundingBox
     */
    public void setMaxZ(float maxZ) {
        this.maxZ = maxZ;
    }

    /**
     * Returns the minimum point of this BoundingBox
     * 
     * @return The minimum point of this BoundingBox
     */
    public Point3f getMin() {
        return new Point3f(minX, minY, minZ);
    }

    /**
     * Set the minimum point of this BoundinBox
     * 
     * @param min The minimum point of this BoundinBox
     */
    public void setMin(Point3f min) {
        minX = min.x;
        minY = min.y;
        minZ = min.z;
    }

    /**
     * Returns the maximum point of this BoundingBox
     * 
     * @return The maximum point of this BoundingBox
     */
    public Point3f getMax() {
        return new Point3f(maxX, maxY, maxZ);
    }

    /**
     * Set the maximum point of this BoundinBox
     * 
     * @param max The maximum point of this BoundinBox
     */
    public void setMax(Point3f max) {
        maxX = max.x;
        maxY = max.y;
        maxZ = max.z;
    }

    /**
     * Returns the size of this BoundingBox in x direction
     *  
     * @return The size of this BoundingBox in x direction
     */
    public float getSizeX() {
        return maxX - minX;
    }

    /**
     * Returns the size of this BoundingBox in y direction
     *  
     * @return The size of this BoundingBox in y direction
     */
    public float getSizeY() {
        return maxY - minY;
    }

    /**
     * Returns the size of this BoundingBox in z direction
     *  
     * @return The size of this BoundingBox in z direction
     */
    public float getSizeZ() {
        return maxZ - minZ;
    }

    /**
     * Returns the center of this BoundingBox
     * 
     * @return The center of this BoundingBox
     */
    public Point3f getCenter() {
        float x = (minX + maxX) / 2;
        float y = (minY + maxY) / 2;
        float z = (minZ + maxZ) / 2;
        return new Point3f(x, y, z);
    }

    /**
     * Combines this BoundingBox with the given point
     * 
     * @param point The point this BoundingBox should be combined with
     */
    public void combine(Point3f point) {
        combine(point.x, point.y, point.z);
    }

    /**
     * Combines this BoundingBox with another BoundingBox
     * 
     * @param other The BoundingBox this BoundingBox should be combined with
     */
    public void combine(BoundingBox other) {
        combine(other.getMin());
        combine(other.getMax());
    }

    /**
     * Combines this BoundingBox with the given point
     * 
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @param z The z coordinate of the point
     */
    public void combine(float x, float y, float z) {
        minX = Math.min(x, minX);
        minY = Math.min(y, minY);
        minZ = Math.min(z, minZ);
        maxX = Math.max(x, maxX);
        maxY = Math.max(y, maxY);
        maxZ = Math.max(z, maxZ);
    }

    /**
     * Returns a String representation of this BoundingBox
     * 
     * @return A String representation of this BoundingBox
     */
    @Override
    public String toString() {
        return "BoundingBox[(" + minX + "," + minY + "," + minZ + ")-(" + maxX + "," + maxY + "," + maxZ + ")]";
    }
}
