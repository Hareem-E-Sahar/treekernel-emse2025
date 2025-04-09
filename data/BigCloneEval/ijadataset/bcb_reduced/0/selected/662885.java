package com.googlecode.climb.game;

import java.util.LinkedList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import com.googlecode.climb.R;
import com.googlecode.saga.Sprite;
import com.googlecode.saga.Vector2;

/**
 * 
 */
final class Spot {

    private static final String LOG_TAG = "Spot";

    private static final int SPOT_DIAMETER = 16;

    private static final int SPOT_RADIUS = Spot.SPOT_DIAMETER / 2;

    private static final int START_YPOS = PlatformSequence.LOWEST_PLATFORM_YPOS + 1;

    private static final int START_XPOS = 50;

    private static final int NOACC = 0;

    private static final int LEFTACC = 1;

    private static final int RIGHTACC = 2;

    /**
     * Spot's tale consists of two balls, which follow him.
     */
    private int tale1XPosition;

    private int tale1YPosition;

    private int tale2XPosition;

    private int tale2YPosition;

    private final Paint tale_paint = new Paint();

    {
        this.tale_paint.setStyle(Style.FILL);
        this.tale_paint.setARGB(75, 255, 0, 50);
    }

    private int accelerateDirection;

    private final Vector2 position;

    private int xSpeed;

    private int ySpeed;

    private boolean isLanded;

    private boolean allowJump;

    private boolean highFall;

    private boolean veryHighFall;

    private final Sprite spotSprite;

    private final SpotAnimation spotAnimation;

    private final PlatformLayer platformLayer;

    private final LinkedList<SpotEventListener> eventListeners = new LinkedList<SpotEventListener>();

    private int lastTouchedPlatform;

    private int highestTouchedPlatform;

    private final ClimbKeyEngine keyEngine;

    Spot(PlatformLayer platformLayer, Game engine) {
        this.platformLayer = platformLayer;
        this.keyEngine = engine;
        this.position = platformLayer.newVector(START_XPOS, START_YPOS);
        final Bitmap spotBitmap = engine.getBitmap(R.drawable.spot);
        this.spotSprite = new Sprite(spotBitmap, Spot.SPOT_DIAMETER, Spot.SPOT_DIAMETER);
        this.spotAnimation = new SpotAnimation(this.spotSprite);
        this.tale1XPosition = this.position.getVirtualScreenX();
        this.tale1YPosition = this.position.getVirtualScreenY();
        this.tale2XPosition = this.position.getVirtualScreenX();
        this.tale2YPosition = this.position.getVirtualScreenY();
        this.isLanded = true;
        this.allowJump = true;
    }

    final void doDraw(Canvas canvas) {
        this.spotAnimation.animate();
        final int spotX = this.position.getVirtualScreenX() - Spot.SPOT_RADIUS;
        final int spotY = this.position.getVirtualScreenY() - Spot.SPOT_DIAMETER + 2;
        final int tale1X = this.tale1XPosition + Spot.SPOT_RADIUS;
        final int tale1Y = this.tale1YPosition + Spot.SPOT_RADIUS;
        final int tale2X = this.tale2XPosition + Spot.SPOT_RADIUS;
        final int tale2Y = this.tale2YPosition + Spot.SPOT_RADIUS;
        final int tale15X = (tale1X + tale2X) / 2;
        final int tale15Y = (tale1Y + tale2Y) / 2;
        canvas.drawCircle(tale2X, tale2Y, Spot.SPOT_RADIUS - 5, this.tale_paint);
        canvas.drawCircle(tale15X, tale15Y, Spot.SPOT_RADIUS - 4, this.tale_paint);
        canvas.drawCircle(tale1X, tale1Y, Spot.SPOT_RADIUS - 3, this.tale_paint);
        this.spotSprite.setPosition(spotX, spotY);
        this.spotSprite.doDraw(canvas);
        this.tale2XPosition = this.tale1XPosition;
        this.tale2YPosition = this.tale1YPosition;
        this.tale1XPosition = spotX;
        this.tale1YPosition = spotY;
    }

    final void doUpdate() {
        checkKeys();
        applySpeed();
        applyGravity();
        platformCollisionDetection();
        applyAcceleration();
        sideCollisionDetection();
    }

    private final void checkKeys() {
        if (this.keyEngine.isJumpPressed()) {
            jump();
        }
        if (this.keyEngine.isMoveLeftPressed()) {
            this.accelerateDirection = Spot.LEFTACC;
        } else if (this.keyEngine.isMoveRightPressed()) {
            this.accelerateDirection = Spot.RIGHTACC;
        } else {
            this.accelerateDirection = Spot.NOACC;
        }
    }

    final void jump() {
        if (!this.allowJump) {
            return;
        }
        this.isLanded = false;
        this.allowJump = false;
        final int speed = Math.abs(this.xSpeed);
        if (speed >= 130) {
            this.ySpeed = 19;
            this.spotAnimation.startHighJump();
        } else if (speed >= 80) {
            this.ySpeed = 16;
            this.spotAnimation.startHighJump();
        } else if (speed >= 1) {
            this.ySpeed = 11;
            this.spotAnimation.startHighJump();
        } else {
            this.ySpeed = 10;
            this.spotAnimation.startHighJump();
        }
        callJumpListeners(this.lastTouchedPlatform);
    }

    private final void applySpeed() {
        int xSpeed_normalized = this.xSpeed / 10;
        if (this.xSpeed > 0) {
            if (xSpeed_normalized > 10) {
                xSpeed_normalized = 10;
            } else {
                xSpeed_normalized += 1;
            }
        } else if (this.xSpeed < 0) {
            if (xSpeed_normalized < -10) {
                xSpeed_normalized = -10;
            } else {
                xSpeed_normalized -= 1;
            }
        }
        this.position.add(xSpeed_normalized, this.ySpeed);
    }

    final Vector2 getPosition() {
        return this.position;
    }

    private final int getXSpeed() {
        return this.xSpeed;
    }

    final int getYSpeed() {
        return this.ySpeed;
    }

    private final void landSpot(int platform) {
        if (this.veryHighFall) {
            this.highFall = false;
            this.veryHighFall = false;
            this.spotAnimation.startVeryHighLand();
        } else if (this.highFall) {
            this.highFall = false;
            this.spotAnimation.startHighLand();
        }
        this.isLanded = true;
        this.allowJump = true;
        this.ySpeed = 0;
        this.highestTouchedPlatform = Math.max(this.highestTouchedPlatform, platform);
        final int previousPlatform = this.lastTouchedPlatform;
        this.lastTouchedPlatform = platform;
        callLandListeners(platform, previousPlatform);
    }

    final void setYSpeed(int speed) {
        this.ySpeed = speed;
    }

    private final void applyAcceleration() {
        switch(this.accelerateDirection) {
            case NOACC:
                int speed = this.xSpeed;
                if (speed == 0) {
                    return;
                }
                if (speed > 0) {
                    if (speed > 100) {
                        speed = 100;
                    }
                    if (speed > 5) {
                        this.xSpeed -= 5;
                    } else {
                        this.xSpeed = 0;
                    }
                } else if (speed < 0) {
                    if (speed < -100) {
                        speed = -100;
                    }
                    if (speed < 5) {
                        this.xSpeed += 5;
                    } else {
                        this.xSpeed = 0;
                    }
                }
                break;
            case Spot.LEFTACC:
                if (this.xSpeed > 0) {
                    this.xSpeed = this.xSpeed / 2 - 1;
                } else {
                    this.xSpeed -= 13;
                }
                if (this.xSpeed < -200) {
                    this.xSpeed = -200;
                }
                break;
            case Spot.RIGHTACC:
                if (this.xSpeed < 0) {
                    this.xSpeed = this.xSpeed / 2 + 1;
                } else {
                    this.xSpeed += 13;
                }
                if (this.xSpeed > 200) {
                    this.xSpeed = 200;
                }
                break;
        }
    }

    private final void sideCollisionDetection() {
        int xSpeed_normalized = this.xSpeed / 10;
        final int xPos = this.position.getLayerX();
        if (xSpeed_normalized > 10) {
            xSpeed_normalized = 10;
        } else if (xSpeed_normalized < -10) {
            xSpeed_normalized = -10;
        }
        if (xSpeed_normalized < 0) {
            if (xSpeed_normalized + xPos - 8 < 17) {
                this.position.setLayerX(17 + 6);
                this.xSpeed = (-1) * this.xSpeed;
                if (xSpeed_normalized <= -10) {
                    this.spotAnimation.startBounce();
                } else {
                    this.spotAnimation.startHighJump();
                }
                this.accelerateDirection = Spot.RIGHTACC;
                callCollideListeners();
            }
        } else if (xSpeed_normalized > 0) {
            if (xSpeed_normalized + xPos + 8 > 159) {
                this.position.setLayerX(159 - 6);
                this.xSpeed = (-1) * this.xSpeed;
                if (xSpeed_normalized >= 10) {
                    this.spotAnimation.startBounce();
                } else {
                    this.spotAnimation.startHighJump();
                }
                this.accelerateDirection = Spot.LEFTACC;
                callCollideListeners();
            }
        }
    }

    private final void applyGravity() {
        if (!this.isLanded) {
            this.ySpeed -= 2;
            if (this.ySpeed <= -14) {
                this.veryHighFall = true;
                this.spotAnimation.startHighFall();
            } else if (this.ySpeed <= -8) {
                this.highFall = true;
                this.spotAnimation.startFall();
            }
        }
    }

    private final void platformCollisionDetection() {
        final int touchedPlatform = this.platformLayer.checkCollision(this);
        if ((this.ySpeed <= 0) && (touchedPlatform != -1)) {
            this.landSpot(touchedPlatform);
        } else if ((this.ySpeed == 0) && (touchedPlatform == -1)) {
            if (this.isLanded) {
                this.allowJump = true;
            }
            this.isLanded = false;
        } else {
            this.allowJump = false;
        }
    }

    final int getLastTouchedPlatform() {
        return this.lastTouchedPlatform;
    }

    final int getHighestTouchedPlatform() {
        return this.highestTouchedPlatform;
    }

    final void addSpotEventListener(SpotEventListener listener) {
        this.eventListeners.add(listener);
    }

    private final void callJumpListeners(int platform) {
        final int size = this.eventListeners.size();
        for (int i = 0; i < size; i++) {
            this.eventListeners.get(i).onSpotJumped(platform);
        }
    }

    private final void callLandListeners(int platform, int previousPlatform) {
        final int size = this.eventListeners.size();
        for (int i = 0; i < size; i++) {
            this.eventListeners.get(i).onSpotLanded(platform, previousPlatform);
        }
    }

    private final void callCollideListeners() {
        final int size = this.eventListeners.size();
        for (int i = 0; i < size; i++) {
            this.eventListeners.get(i).onSpotCollidedWall();
        }
    }
}
