package com.golden.gamedev.object.collision;

import com.golden.gamedev.object.CollisionManager;
import com.golden.gamedev.object.PlayField;
import com.golden.gamedev.object.Sprite;
import com.golden.gamedev.object.SpriteGroup;

/**
 * <p>
 * Subclass of <code>CollisionGroup</code> that calculates the precise
 * positions of the <code>Sprite</code>s at the moment of collision. It is
 * suitable for collisions that need the colliding objects to stop rather than
 * vanish.
 * </p>
 * 
 * <p>
 * For example:
 * <ul>
 * <li>Collisions between balls that need to stop or bounce precisely off each
 * other.</li>
 * <li>An object falling to the ground and stopping.</li>
 * <li>Objects that are replaced by something else (such as an explosion
 * effect).</li> &
 * </ul>
 * </p>
 * 
 * <p>
 * This class may not work as expected with concave sprites- such as L-shapes.
 * The position of the collision will be found accurately, but the direction may
 * not be as anticipated as it is based on the <code>CollisionRect</code>s
 * rather than pixel collisions or custom <code>CollisionShape</code>s
 * defined in subclasses of <code>PreciseCollisionGroup</code> If concave
 * sprites are necessary, it might be advisable to break them into groups of
 * smaller convex <code>Sprite</code>s.
 * </p>
 * 
 * @see PlayField#addCollisionGroup(SpriteGroup, SpriteGroup, CollisionManager)
 * 
 */
public abstract class PreciseCollisionGroup extends CollisionGroup {

    /*****************************************************************************
   * This is used to test for non-convergence in pixel perfect collision, or
   * when unusual <code>CollisionShape</code>s are used. The default value is
   * 0.000001.
   ****************************************************************************/
    protected static double ITERATIVE_BAILOUT = 0.000001;

    /*****************************************************************************
   * This is the distance that two objects must be within to be considered
   * adjacent. When a collision occurs, the <Sprite>s at their reverted
   * positions are guaranteed to be at least this close. This should be larger
   * than <code>ITERATIVE_BAILOUT</code>. The default value is 0.0001.
   ****************************************************************************/
    protected static double ADJACENCY_TOLERANCE = 0.0001;

    private CollisionShape shape3;

    private CollisionShape shape4;

    /*****************************************************************************
   * When set true, this <code>PreciseCollisionGroup</code> will send
   * debugging information to the console.
   ****************************************************************************/
    protected boolean log = false;

    /*****************************************************************************
   * Default constructor.
   ****************************************************************************/
    public PreciseCollisionGroup() {
    }

    /**
   * Performs collision check between Sprite <code>s1</code> and Sprite
   * <code>s2</code>, and returns true if the sprites (<code>shape1</code>,
   * <code>shape2</code>) collided.
   * <p>
   * 
   * The revert positions are set precisely by this method.
   * 
   * @param s1
   *        sprite from group 1
   * @param s2
   *        sprite from group 2
   * @param shape1
   *        bounding box of sprite 1
   * @param shape2
   *        bounding box of sprite 2
   * @return <code>true</code> if the sprites is collided one another.
   */
    public boolean isCollide(Sprite s1, Sprite s2, CollisionShape shape1, CollisionShape shape2) {
        if ((this.pixelPerfectCollision && CollisionManager.isPixelCollide(s1.getX(), s1.getY(), s1.getImage(), s2.getX(), s2.getY(), s2.getImage())) || (!this.pixelPerfectCollision && shape1.intersects(shape2))) {
            this.sprite1 = s1;
            this.sprite2 = s2;
            this.collisionSide = 0;
            double speedX1 = s1.getX() - s1.getOldX(), speedY1 = s1.getY() - s1.getOldY(), speedX2 = s2.getX() - s2.getOldX(), speedY2 = s2.getY() - s2.getOldY();
            double x1 = shape1.getX() - speedX1, y1 = shape1.getY() - speedY1, x2 = shape2.getX() - speedX2, y2 = shape2.getY() - speedY2;
            double w1 = shape1.getWidth(), h1 = shape1.getHeight(), w2 = shape2.getWidth(), h2 = shape2.getHeight();
            if (this.log) {
                System.out.print("Collision (" + s1.getX() + "," + s1.getY() + "),(" + x1 + "," + y1 + ")-->");
            }
            if (this.checkCollisionHelper(s1, s2, x1, y1, x2, y2, true)) {
                if (this.log) {
                    System.out.print("Overlap->");
                }
                this.collisionSide = 0;
                Sprite spriteToMove, otherSprite;
                if (speedX1 == 0 && speedY1 == 0 && speedX2 == 0 && speedY2 == 0) {
                    if (this.log) {
                        System.out.println("Both stationary");
                    }
                    return false;
                } else {
                    double s1cx = shape1.getX() + shape1.getWidth() / 2;
                    double s1cy = shape1.getY() + shape1.getHeight() / 2;
                    double s2cx = shape2.getX() + shape2.getWidth() / 2;
                    double s2cy = shape2.getY() + shape2.getHeight() / 2;
                    if (Math.pow(speedX1, 2) + Math.pow(speedY1, 2) > Math.pow(speedX2, 2) + Math.pow(speedY2, 2)) {
                        spriteToMove = s1;
                        otherSprite = s2;
                    } else {
                        spriteToMove = s2;
                        otherSprite = s1;
                    }
                    if (this.log) {
                        System.out.print(spriteToMove + "-->");
                    }
                    double distXLeft = s1cx - s2cx + w1 / 2 + w2 / 2;
                    double distXRight = s2cx - s1cx + w1 / 2 + w2 / 2;
                    double distYUp = s1cy - s2cy + h1 / 2 + h2 / 2;
                    double distYDown = s2cy - s1cy + h1 / 2 + h2 / 2;
                    double minDist = Math.min(Math.min(distXLeft, distXRight), Math.min(distYUp, distYDown));
                    if (spriteToMove == s1) {
                        this.collisionX2 = s2.getX();
                        this.collisionY2 = s2.getY();
                        if (minDist == distXLeft) {
                            this.collisionX1 = s1.getX() - distXLeft;
                            this.collisionY1 = s1.getY();
                            this.collisionSide = CollisionGroup.RIGHT_LEFT_COLLISION;
                        } else if (minDist == distXRight) {
                            this.collisionX1 = s1.getX() + distXRight;
                            this.collisionY1 = s1.getY();
                            this.collisionSide = CollisionGroup.LEFT_RIGHT_COLLISION;
                        } else if (minDist == distYUp) {
                            this.collisionX1 = s1.getX();
                            this.collisionY1 = s1.getY() - distYUp;
                            this.collisionSide = CollisionGroup.BOTTOM_TOP_COLLISION;
                        } else {
                            this.collisionX1 = s1.getX();
                            this.collisionY1 = s1.getY() + distYDown;
                            this.collisionSide = CollisionGroup.TOP_BOTTOM_COLLISION;
                        }
                        if (this.log) {
                            System.out.println("Corrected");
                        }
                        return true;
                    } else {
                        this.collisionX1 = s1.getX();
                        this.collisionY1 = s1.getY();
                        if (minDist == distXLeft) {
                            this.collisionX2 = s2.getX() - distXLeft;
                            this.collisionY2 = s2.getY();
                            this.collisionSide = CollisionGroup.LEFT_RIGHT_COLLISION;
                        } else if (minDist == distXRight) {
                            this.collisionX2 = s2.getX() + distXRight;
                            this.collisionY2 = s2.getY();
                            this.collisionSide = CollisionGroup.RIGHT_LEFT_COLLISION;
                        } else if (minDist == distYUp) {
                            this.collisionX2 = s2.getX();
                            this.collisionY2 = s2.getY() - distYUp;
                            this.collisionSide = CollisionGroup.TOP_BOTTOM_COLLISION;
                        } else {
                            this.collisionX2 = s2.getX();
                            this.collisionY2 = s2.getY() + distYDown;
                            this.collisionSide = CollisionGroup.BOTTOM_TOP_COLLISION;
                        }
                        if (this.log) {
                            System.out.println("Corrected");
                        }
                        return true;
                    }
                }
            } else {
                double tHoriz = 999999.0, tVert = 999999.0;
                int xCollision = -1, yCollision = -1;
                if (speedX1 > speedX2) {
                    if (this.log) {
                        System.out.print("dx1>dx2-->");
                    }
                    tHoriz = (x2 - x1 - w1) / (speedX1 - speedX2);
                    xCollision = CollisionGroup.RIGHT_LEFT_COLLISION;
                } else if (speedX2 > speedX1) {
                    if (this.log) {
                        System.out.print("dx1<dx2-->");
                    }
                    tHoriz = (x1 - x2 - w2) / (speedX2 - speedX1);
                    xCollision = CollisionGroup.LEFT_RIGHT_COLLISION;
                }
                if (speedY1 > speedY2) {
                    if (this.log) {
                        System.out.print("dy1>dy2-->");
                    }
                    tVert = (y2 - y1 - h1) / (speedY1 - speedY2);
                    yCollision = CollisionGroup.BOTTOM_TOP_COLLISION;
                } else if (speedY2 > speedY1) {
                    if (this.log) {
                        System.out.print("dy1<dy2-->");
                    }
                    tVert = (y1 - y2 - h2) / (speedY2 - speedY1);
                    yCollision = CollisionGroup.TOP_BOTTOM_COLLISION;
                }
                double finalT;
                if (tHoriz <= tVert) {
                    if (this.log) {
                        System.out.print("X " + tHoriz + "-->");
                    }
                    this.collisionSide = xCollision;
                    if (this.checkAdjacencyHelper(s1, s2, x1 + tHoriz * speedX1, y1 + tHoriz * speedY1, x2 + tHoriz * speedX2, y2 + tHoriz * speedY2, speedX1, speedY1, speedX2, speedY2, false)) {
                        if (this.log) {
                            System.out.print("X " + tHoriz + "-->");
                        }
                        finalT = tHoriz;
                    } else {
                        if (this.log) {
                            System.out.print("Y " + tVert + "-->");
                        }
                        this.collisionSide = yCollision;
                        finalT = tVert;
                    }
                } else {
                    if (this.log) {
                        System.out.print("Y " + tVert + "-->");
                    }
                    this.collisionSide = yCollision;
                    if (this.checkAdjacencyHelper(s1, s2, x1 + tVert * speedX1, y1 + tVert * speedY1, x2 + tVert * speedX2, y2 + tVert * speedY2, speedX1, speedY1, speedX2, speedY2, false)) {
                        if (this.log) {
                            System.out.print("Y " + tVert + "-->");
                        }
                        finalT = tVert;
                    } else {
                        if (this.log) {
                            System.out.print("X " + tHoriz + "-->");
                        }
                        this.collisionSide = xCollision;
                        finalT = tHoriz;
                    }
                }
                this.collisionX1 = x1 + finalT * speedX1;
                this.collisionY1 = y1 + finalT * speedY1;
                this.collisionX2 = x2 + finalT * speedX2;
                this.collisionY2 = y2 + finalT * speedY2;
                if (this.checkCollisionHelper(s1, s2, this.collisionX1, this.collisionY1, this.collisionX2, this.collisionY2, true)) {
                    if (this.log) {
                        System.out.print("Iterate (1)-->");
                    }
                    if (this.iterativeMethod(s1, s2, 0.0, finalT, x1, y1, x2, y2, speedX1, speedY1, speedX2, speedY2)) {
                        this.collisionX1 = this.collisionX1 - x1 + s1.getOldX();
                        this.collisionY1 = this.collisionY1 - y1 + s1.getOldY();
                        this.collisionX2 = this.collisionX2 - x2 + s2.getOldX();
                        this.collisionY2 = this.collisionY2 - y2 + s2.getOldY();
                        if (this.log) {
                            System.out.println("true: " + this.collisionSide + " (" + this.collisionX1 + "," + this.collisionY1 + ")");
                        }
                        return true;
                    } else {
                        if (this.log) {
                            System.out.println("false");
                        }
                        return false;
                    }
                } else if (this.checkAdjacencyHelper(s1, s2, this.collisionX1, this.collisionY1, this.collisionX2, this.collisionY2, speedX1, speedY1, speedX2, speedY2, true)) {
                    this.collisionX1 = this.collisionX1 - x1 + s1.getOldX();
                    this.collisionY1 = this.collisionY1 - y1 + s1.getOldY();
                    this.collisionX2 = this.collisionX2 - x2 + s2.getOldX();
                    this.collisionY2 = this.collisionY2 - y2 + s2.getOldY();
                    if (this.log) {
                        System.out.println("true: " + this.collisionSide + " (" + this.collisionX1 + "," + this.collisionY1 + ")");
                    }
                    return true;
                } else {
                    if (this.log) {
                        System.out.print("Iterate (2)-->");
                    }
                    if (this.iterativeMethod(s1, s2, finalT, 1.0, x1, y1, x2, y2, speedX1, speedY1, speedX2, speedY2)) {
                        this.collisionX1 = this.collisionX1 - x1 + s1.getOldX();
                        this.collisionY1 = this.collisionY1 - y1 + s1.getOldY();
                        this.collisionX2 = this.collisionX2 - x2 + s2.getOldX();
                        this.collisionY2 = this.collisionY2 - y2 + s2.getOldY();
                        if (this.log) {
                            System.out.println("true: " + this.collisionSide + " (" + this.collisionX1 + "," + this.collisionY1 + ")");
                        }
                        return true;
                    } else {
                        if (this.log) {
                            System.out.println("false");
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    protected boolean checkCollisionHelper(Sprite s1, Sprite s2, double x1, double y1, double x2, double y2, boolean includePixelPerfect) {
        if (includePixelPerfect && this.pixelPerfectCollision) {
            return CollisionManager.isPixelCollide(x1, y1, s1.getImage(), x2, y2, s2.getImage());
        } else {
            this.shape3 = this.getCollisionShape1(s1);
            this.shape3.setLocation(x1, y1);
            this.shape4 = this.getCollisionShape2(s2);
            this.shape4.setLocation(x2, y2);
            return this.shape3.intersects(this.shape4);
        }
    }

    protected boolean checkAdjacencyHelper(Sprite s1, Sprite s2, double x1, double y1, double x2, double y2, double dx1, double dy1, double dx2, double dy2, boolean includePixelPerfect) {
        double dx = 0, dy = 0;
        if (dx1 - dx2 < 0) {
            dx = -PreciseCollisionGroup.ADJACENCY_TOLERANCE;
        } else if (dx1 - dx2 > 0) {
            dx = PreciseCollisionGroup.ADJACENCY_TOLERANCE;
        }
        if (dy1 - dy2 < 0) {
            dy = -PreciseCollisionGroup.ADJACENCY_TOLERANCE;
        } else if (dy1 - dy2 > 0) {
            dy = PreciseCollisionGroup.ADJACENCY_TOLERANCE;
        }
        if (includePixelPerfect && this.pixelPerfectCollision) {
            return CollisionManager.isPixelCollide(x1 + dx, y1 + dy, s1.getImage(), x2, y2, s2.getImage());
        } else {
            this.shape3 = this.getCollisionShape1(s1);
            this.shape3.setLocation(x1 + dx, y1 + dy);
            this.shape4 = this.getCollisionShape2(s2);
            this.shape4.setLocation(x2, y2);
            return this.shape3.intersects(this.shape4);
        }
    }

    protected boolean iterativeMethod(Sprite s1, Sprite s2, double lowerT, double higherT, double oldX1, double oldY1, double oldX2, double oldY2, double speedX1, double speedY1, double speedX2, double speedY2) {
        double workingT = (lowerT + higherT) / 2;
        double curX1, curY1, curX2, curY2;
        double maxSpeed = Math.max(Math.max(Math.abs(speedX1), Math.abs(speedY1)), Math.max(Math.abs(speedX2), Math.abs(speedY2)));
        while (true) {
            curX1 = oldX1 + workingT * speedX1;
            curY1 = oldY1 + workingT * speedY1;
            curX2 = oldX2 + workingT * speedX2;
            curY2 = oldY2 + workingT * speedY2;
            if (this.checkCollisionHelper(s1, s2, curX1, curY1, curX2, curY2, true)) {
                higherT = workingT;
                workingT = (workingT + lowerT) / 2;
                if ((higherT - lowerT) * maxSpeed < PreciseCollisionGroup.ITERATIVE_BAILOUT) {
                    System.err.println("Iterative failure-- too close");
                    break;
                }
            } else if (this.checkAdjacencyHelper(s1, s2, curX1, curY1, curX2, curY2, speedX1, speedY1, speedX2, speedY2, true)) {
                this.collisionX1 = Math.abs(curX1 - oldX1) > 2 * PreciseCollisionGroup.ADJACENCY_TOLERANCE ? curX1 : oldX1;
                this.collisionY1 = Math.abs(curY1 - oldY1) > 2 * PreciseCollisionGroup.ADJACENCY_TOLERANCE ? curY1 : oldY1;
                this.collisionX2 = Math.abs(curX2 - oldX2) > 2 * PreciseCollisionGroup.ADJACENCY_TOLERANCE ? curX2 : oldX2;
                this.collisionY2 = Math.abs(curY2 - oldY2) > 2 * PreciseCollisionGroup.ADJACENCY_TOLERANCE ? curY2 : oldY2;
                return true;
            } else {
                lowerT = workingT;
                workingT = (workingT + higherT) / 2;
                if ((higherT - lowerT) * maxSpeed < PreciseCollisionGroup.ITERATIVE_BAILOUT) {
                    break;
                }
            }
        }
        return false;
    }
}
