package entity;

import physics.Vec2;

public class Cannon {

    private Vec2 position;

    private int angle;

    private int maxAngle;

    private int minAngle;

    private int power;

    public Cannon(Vec2 position, int minAngle, int maxAngle, int power) {
        this.position = position;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.angle = (maxAngle + minAngle) / 2;
        this.power = power;
    }

    public Vec2 getPosition() {
        return position;
    }

    public Vec2 getVelocity() {
        return new Vec2(Math.sin(angle * Math.PI / 180) * power, Math.cos(angle * Math.PI / 180) * power);
    }

    public void increaseAngle() {
        angle = Math.min(angle + 5, maxAngle);
    }

    public void decreaseAngle() {
        angle = Math.max(angle - 5, minAngle);
    }

    public void shoot() {
        EntityManager.addSpacePod(position, getVelocity());
    }
}
