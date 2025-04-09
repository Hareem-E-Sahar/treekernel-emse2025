package de.grogra.imp3d.shading;

import java.util.Random;

public class Carpenter extends SyntheticTexture {

    float offset = 0.5f;

    float color = 0.5f;

    float noise = 1.0f;

    private static final Random rnd = new Random();

    @Override
    protected void calculateImageData() {
        float color = this.color;
        setPixel(0, 0, color);
        int x0 = 0;
        int y0 = 0;
        int x1 = x0 + width;
        int y1 = y0 + height;
        carpenter(x0, y0, x1, y1);
    }

    void carpenter(int x0, int y0, int x1, int y1) {
        int w = x1 - x0;
        int h = y1 - y0;
        while (w >= 2 || h >= 2) {
            for (int x = x0; x < x1; x += w) {
                for (int y = y0; y < y1; y += h) {
                    diamondStep(x, y, x + w, y + h);
                }
            }
            for (int x = x0; x < x1; x += w) {
                for (int y = y0; y < y1; y += h) {
                    squareStep(x - w / 2, y, x + w / 2, y + h);
                    squareStep(x, y - h / 2, x + w, y + h / 2);
                }
            }
            w /= 2;
            h /= 2;
        }
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                setPixel(x, y, getPixel(x, y) + offset);
            }
        }
    }

    void diamondStep(int x0, int y0, int x1, int y1) {
        int w = x1 - x0;
        int h = y1 - y0;
        if (w < 2 || h < 2) return;
        int x = (x0 + x1) / 2;
        int y = (y0 + y1) / 2;
        float c00 = getPixel(x0, y0);
        float c01 = getPixel(x1, y0);
        float c10 = getPixel(x0, y1);
        float c11 = getPixel(x1, y1);
        float c = (c00 + c01 + c10 + c11) / 4;
        c += noise * (rnd.nextFloat() - 0.5);
        setPixel(x, y, c);
    }

    void squareStep(int x0, int y0, int x1, int y1) {
        int x = (x0 + x1) / 2;
        int y = (y0 + y1) / 2;
        float cx0 = getPixel(x0, y);
        float cx1 = getPixel(x1, y);
        float cy0 = getPixel(x, y0);
        float cy1 = getPixel(x, y1);
        float c = (cx0 + cx1 + cy0 + cy1) / 4;
        c += noise * (rnd.nextFloat() - 0.5);
        setPixel(x, y, c);
    }

    public static final NType $TYPE;

    public static final NType.Field offset$FIELD;

    public static final NType.Field color$FIELD;

    public static final NType.Field noise$FIELD;

    private static final class _Field extends NType.Field {

        private final int id;

        _Field(String name, int modifiers, de.grogra.reflect.Type type, de.grogra.reflect.Type componentType, int id) {
            super(Carpenter.$TYPE, name, modifiers, type, componentType);
            this.id = id;
        }

        @Override
        public void setFloat(Object o, float value) {
            switch(id) {
                case 0:
                    ((Carpenter) o).offset = (float) value;
                    return;
                case 1:
                    ((Carpenter) o).color = (float) value;
                    return;
                case 2:
                    ((Carpenter) o).noise = (float) value;
                    return;
            }
            super.setFloat(o, value);
        }

        @Override
        public float getFloat(Object o) {
            switch(id) {
                case 0:
                    return ((Carpenter) o).getOffset();
                case 1:
                    return ((Carpenter) o).getColor();
                case 2:
                    return ((Carpenter) o).getNoise();
            }
            return super.getFloat(o);
        }
    }

    static {
        $TYPE = new NType(new Carpenter());
        $TYPE.addManagedField(offset$FIELD = new _Field("offset", 0 | _Field.SCO, de.grogra.reflect.Type.FLOAT, null, 0));
        $TYPE.addManagedField(color$FIELD = new _Field("color", 0 | _Field.SCO, de.grogra.reflect.Type.FLOAT, null, 1));
        $TYPE.addManagedField(noise$FIELD = new _Field("noise", 0 | _Field.SCO, de.grogra.reflect.Type.FLOAT, null, 2));
        $TYPE.validate();
    }

    @Override
    protected NType getNTypeImpl() {
        return $TYPE;
    }

    @Override
    protected de.grogra.graph.impl.Node newInstance() {
        return new Carpenter();
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float value) {
        this.offset = (float) value;
    }

    public float getColor() {
        return color;
    }

    public void setColor(float value) {
        this.color = (float) value;
    }

    public float getNoise() {
        return noise;
    }

    public void setNoise(float value) {
        this.noise = (float) value;
    }
}
