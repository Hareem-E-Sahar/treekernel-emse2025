package de.grogra.texgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

public class DiamondSquareTexture extends SyntheticTexture {

    int width = 256;

    int height = 256;

    float a0 = 1.0f;

    float a1 = 0.75f;

    float a2 = 0.5f;

    float a3 = 0.25f;

    long randomSeed = 1827564;

    boolean useColor = true;

    private BufferedImage img;

    private transient int imageStamp = -1;

    private Random rnd;

    public DiamondSquareTexture() {
    }

    @Override
    public BufferedImage getBufferedImage() {
        int s = getStamp();
        synchronized (this) {
            if (s != imageStamp) {
                createImage();
                imageStamp = s;
            }
        }
        return img;
    }

    private Color computeColor(float c) {
        float red = 0;
        float green = 0;
        float blue = 0;
        if (c < 0.5f) {
            red = c * 2;
        } else {
            red = (1.0f - c) * 2;
        }
        if (c >= 0.3f && c < 0.8f) {
            green = (c - 0.3f) * 2;
        } else if (c < 0.3f) {
            green = (0.3f - c) * 2;
        } else {
            green = (1.3f - c) * 2;
        }
        if (c >= 0.5f) {
            blue = (c - 0.5f) * 2;
        } else {
            blue = (0.5f - c) * 2;
        }
        if (red < 0) red = Math.abs(red);
        if (green < 0) green = Math.abs(green);
        if (blue < 0) blue = Math.abs(blue);
        if (red > 1) red -= (int) red;
        if (green > 1) green -= (int) green;
        if (blue > 1) blue -= (int) blue;
        if (useColor) return new Color(red, green, blue);
        c = 0.3f * red + 0.59f * green + 0.11f * blue;
        return new Color(c, c, c);
    }

    @Override
    public Dimension getPreferredIconSize(boolean small) {
        return small ? new Dimension(width * 32 / height, 32) : new Dimension(width, height);
    }

    /** 
	 * "helper function" to create an initial grid before the recursive function is called
	 */
    protected void createImage() {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        rnd = new Random(randomSeed);
        divideGrid(img.getGraphics(), 0, 0, width, height, a0, a1, a2, a3);
    }

    /** 
	 * This is the recursive function that implements the random midpoint
	 * displacement algorithm (diamond square). It will call itself until the grid pieces
	 * become smaller than one pixel.
	 */
    protected void divideGrid(Graphics g, float x, float y, float width, float height, float a0, float a1, float a2, float a3) {
        float edge1, edge2, edge3, edge4, middle;
        float newWidth = width / 2;
        float newHeight = height / 2;
        if (width > 2 || height > 2) {
            middle = (a0 + a1 + a2 + a3) / 4 + displace(width, height, newWidth + newHeight);
            edge1 = (a0 + a1) / 2;
            edge2 = (a1 + a2) / 2;
            edge3 = (a2 + a3) / 2;
            edge4 = (a3 + a1) / 2;
            if (middle < 0) {
                middle = 0;
            } else if (middle > 1.0f) {
                middle = 1.0f;
            }
            divideGrid(g, x, y, newWidth, newHeight, a0, edge1, middle, edge4);
            divideGrid(g, x + newWidth, y, newWidth, newHeight, edge1, a1, edge2, middle);
            divideGrid(g, x + newWidth, y + newHeight, newWidth, newHeight, middle, edge2, a2, edge3);
            divideGrid(g, x, y + newHeight, newWidth, newHeight, edge4, middle, edge3, a3);
        } else {
            g.setColor(computeColor((a0 + a1 + a2 + a3) / 4));
            g.drawRect((int) x, (int) y, 1, 1);
        }
    }

    /** 
	 * Randomly displaces color value for midpoint depending on size of grid piece.
	 */
    protected float displace(float width, float height, float num) {
        float max = num / (float) (width + height) * 3;
        return ((float) rnd.nextFloat() - 0.5f) * max;
    }

    public static final Type $TYPE;

    public static final Type.Field width$FIELD;

    public static final Type.Field height$FIELD;

    public static final Type.Field a0$FIELD;

    public static final Type.Field a1$FIELD;

    public static final Type.Field a2$FIELD;

    public static final Type.Field a3$FIELD;

    public static final Type.Field randomSeed$FIELD;

    public static final Type.Field useColor$FIELD;

    public static class Type extends SyntheticTexture.Type {

        public Type(Class c, de.grogra.persistence.SCOType supertype) {
            super(c, supertype);
        }

        public Type(DiamondSquareTexture representative, de.grogra.persistence.SCOType supertype) {
            super(representative, supertype);
        }

        Type(Class c) {
            super(c, SyntheticTexture.$TYPE);
        }

        private static final int SUPER_FIELD_COUNT = SyntheticTexture.Type.FIELD_COUNT;

        protected static final int FIELD_COUNT = SyntheticTexture.Type.FIELD_COUNT + 8;

        static Field _addManagedField(Type t, String name, int modifiers, de.grogra.reflect.Type type, de.grogra.reflect.Type componentType, int id) {
            return t.addManagedField(name, modifiers, type, componentType, id);
        }

        @Override
        protected void setBoolean(Object o, int id, boolean value) {
            switch(id) {
                case Type.SUPER_FIELD_COUNT + 7:
                    ((DiamondSquareTexture) o).useColor = (boolean) value;
                    return;
            }
            super.setBoolean(o, id, value);
        }

        @Override
        protected boolean getBoolean(Object o, int id) {
            switch(id) {
                case Type.SUPER_FIELD_COUNT + 7:
                    return ((DiamondSquareTexture) o).isUseColor();
            }
            return super.getBoolean(o, id);
        }

        @Override
        protected void setInt(Object o, int id, int value) {
            switch(id) {
                case Type.SUPER_FIELD_COUNT + 0:
                    ((DiamondSquareTexture) o).width = (int) value;
                    return;
                case Type.SUPER_FIELD_COUNT + 1:
                    ((DiamondSquareTexture) o).height = (int) value;
                    return;
            }
            super.setInt(o, id, value);
        }

        @Override
        protected int getInt(Object o, int id) {
            switch(id) {
                case Type.SUPER_FIELD_COUNT + 0:
                    return ((DiamondSquareTexture) o).getWidth();
                case Type.SUPER_FIELD_COUNT + 1:
                    return ((DiamondSquareTexture) o).getHeight();
            }
            return super.getInt(o, id);
        }

        @Override
        protected void setLong(Object o, int id, long value) {
            switch(id) {
                case Type.SUPER_FIELD_COUNT + 6:
                    ((DiamondSquareTexture) o).randomSeed = (long) value;
                    return;
            }
            super.setLong(o, id, value);
        }

        @Override
        protected long getLong(Object o, int id) {
            switch(id) {
                case Type.SUPER_FIELD_COUNT + 6:
                    return ((DiamondSquareTexture) o).getRandomSeed();
            }
            return super.getLong(o, id);
        }

        @Override
        protected void setFloat(Object o, int id, float value) {
            switch(id) {
                case Type.SUPER_FIELD_COUNT + 2:
                    ((DiamondSquareTexture) o).a0 = (float) value;
                    return;
                case Type.SUPER_FIELD_COUNT + 3:
                    ((DiamondSquareTexture) o).a1 = (float) value;
                    return;
                case Type.SUPER_FIELD_COUNT + 4:
                    ((DiamondSquareTexture) o).a2 = (float) value;
                    return;
                case Type.SUPER_FIELD_COUNT + 5:
                    ((DiamondSquareTexture) o).a3 = (float) value;
                    return;
            }
            super.setFloat(o, id, value);
        }

        @Override
        protected float getFloat(Object o, int id) {
            switch(id) {
                case Type.SUPER_FIELD_COUNT + 2:
                    return ((DiamondSquareTexture) o).getA0();
                case Type.SUPER_FIELD_COUNT + 3:
                    return ((DiamondSquareTexture) o).getA1();
                case Type.SUPER_FIELD_COUNT + 4:
                    return ((DiamondSquareTexture) o).getA2();
                case Type.SUPER_FIELD_COUNT + 5:
                    return ((DiamondSquareTexture) o).getA3();
            }
            return super.getFloat(o, id);
        }

        @Override
        public Object newInstance() {
            return new DiamondSquareTexture();
        }
    }

    public de.grogra.persistence.ManageableType getManageableType() {
        return $TYPE;
    }

    static {
        $TYPE = new Type(DiamondSquareTexture.class);
        width$FIELD = Type._addManagedField($TYPE, "width", 0 | Type.Field.SCO, de.grogra.reflect.Type.INT, null, Type.SUPER_FIELD_COUNT + 0);
        height$FIELD = Type._addManagedField($TYPE, "height", 0 | Type.Field.SCO, de.grogra.reflect.Type.INT, null, Type.SUPER_FIELD_COUNT + 1);
        a0$FIELD = Type._addManagedField($TYPE, "a0", 0 | Type.Field.SCO, de.grogra.reflect.Type.FLOAT, null, Type.SUPER_FIELD_COUNT + 2);
        a1$FIELD = Type._addManagedField($TYPE, "a1", 0 | Type.Field.SCO, de.grogra.reflect.Type.FLOAT, null, Type.SUPER_FIELD_COUNT + 3);
        a2$FIELD = Type._addManagedField($TYPE, "a2", 0 | Type.Field.SCO, de.grogra.reflect.Type.FLOAT, null, Type.SUPER_FIELD_COUNT + 4);
        a3$FIELD = Type._addManagedField($TYPE, "a3", 0 | Type.Field.SCO, de.grogra.reflect.Type.FLOAT, null, Type.SUPER_FIELD_COUNT + 5);
        randomSeed$FIELD = Type._addManagedField($TYPE, "randomSeed", 0 | Type.Field.SCO, de.grogra.reflect.Type.LONG, null, Type.SUPER_FIELD_COUNT + 6);
        useColor$FIELD = Type._addManagedField($TYPE, "useColor", 0 | Type.Field.SCO, de.grogra.reflect.Type.BOOLEAN, null, Type.SUPER_FIELD_COUNT + 7);
        $TYPE.validate();
    }

    public boolean isUseColor() {
        return useColor;
    }

    public void setUseColor(boolean value) {
        this.useColor = (boolean) value;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int value) {
        this.width = (int) value;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int value) {
        this.height = (int) value;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long value) {
        this.randomSeed = (long) value;
    }

    public float getA0() {
        return a0;
    }

    public void setA0(float value) {
        this.a0 = (float) value;
    }

    public float getA1() {
        return a1;
    }

    public void setA1(float value) {
        this.a1 = (float) value;
    }

    public float getA2() {
        return a2;
    }

    public void setA2(float value) {
        this.a2 = (float) value;
    }

    public float getA3() {
        return a3;
    }

    public void setA3(float value) {
        this.a3 = (float) value;
    }
}
