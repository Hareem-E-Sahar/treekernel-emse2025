package Game.sprites;

import java.lang.reflect.Constructor;
import graphics.*;

/**
    A PowerUp class is a Sprite that the player can pick up.
*/
public abstract class PowerUp extends Sprite {

    public PowerUp(Animation anim) {
        super(anim);
    }

    public Object clone() {
        Constructor constructor = getClass().getConstructors()[0];
        try {
            return constructor.newInstance(new Object[] { (Animation) anim.clone() });
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
        A Star PowerUp. Gives the player points.
    */
    public static class Star extends PowerUp {

        public Star(Animation anim) {
            super(anim);
        }
    }

    /**
        A Music PowerUp. Changes the game music.
    */
    public static class ColorSwitch extends PowerUp {

        private boolean bOn;

        private boolean active;

        private boolean touched;

        private final long waitTime = 3000;

        private long timeWaited;

        private int affectedHeight, affectedWidth, originOffsetX, originOffsetY;

        public ColorSwitch(Animation anim) {
            super(anim);
            bOn = true;
            active = true;
            touched = false;
            timeWaited = 0;
            affectedHeight = 9;
            affectedWidth = 7;
            originOffsetX = (affectedWidth + 1) / 2;
            originOffsetY = (affectedHeight + 1) / 2;
        }

        public int getOriginOffsetX() {
            return originOffsetX;
        }

        public int getOriginOffsetY() {
            return originOffsetY;
        }

        public int getAffectedHeight() {
            return affectedHeight;
        }

        public int getAffectedWidth() {
            return affectedWidth;
        }

        public boolean isOn() {
            return bOn;
        }

        public void touched() {
            if (!touched) {
                touched = true;
                active = false;
                bOn = !bOn;
                if (bOn) System.out.println("Is on"); else System.out.println("Is off");
            }
        }

        @Override
        public void update(long elapsedTime) {
            super.update(elapsedTime);
            if (touched) {
                if (timeWaited > waitTime) {
                    touched = false;
                    active = true;
                    timeWaited = 0;
                } else timeWaited += elapsedTime;
            }
        }

        public boolean isActive() {
            return active;
        }
    }

    /**
        A Goal PowerUp. Advances to the next map.
    */
    public static class Goal extends PowerUp {

        public Goal(Animation anim) {
            super(anim);
        }
    }
}
