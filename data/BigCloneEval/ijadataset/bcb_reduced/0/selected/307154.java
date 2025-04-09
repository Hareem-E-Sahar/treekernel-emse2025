package spacedemo;

/**
 *
 * @author User
 */
public class Cannon {

    private int angle;

    private int minAngle;

    private int maxAngle;

    private int power;

    private Vec2D position;

    public Cannon(Vec2D position, int minAngle, int maxAngle, int power) {
        this.position = position;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.angle = (maxAngle + minAngle) / 2;
        this.power = power;
    }

    public Vec2D getPosition() {
        return position;
    }

    public Vec2D getVelocity() {
        return new Vec2D(Math.sin(angle * Math.PI / 180) * power, Math.cos(angle * Math.PI / 180) * power);
    }

    public void increaseAngle() {
        angle = Math.min(angle + 5, maxAngle);
    }

    public void decreaseAngle() {
        angle = Math.max(angle - 5, minAngle);
    }
}
