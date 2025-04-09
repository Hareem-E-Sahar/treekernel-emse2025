package ch.tarnet.pigment.model;

import java.awt.Color;

public class Pigment {

    public static final Pigment black = new Pigment(0f, 0f, 0f);

    float r, g, b;

    public Pigment() {
        this.r = 0f;
        this.g = 0f;
        this.b = 0f;
    }

    public Pigment(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Pigment(int r, int g, int b) {
        this.r = r / 255f;
        this.g = g / 255f;
        this.b = b / 255f;
    }

    public Pigment(Color color) {
        this.r = color.getRed() / 255f;
        this.g = color.getGreen() / 255f;
        this.b = color.getBlue() / 255f;
    }

    public Pigment(Pigment pigment) {
        this.r = pigment.r;
        this.g = pigment.g;
        this.b = pigment.b;
    }

    /**
	 * @param i
	 */
    public Pigment(int i) {
        this.r = (i >> 16 & 0xff) / 255f;
        this.g = (i >> 8 & 0xff) / 255f;
        this.b = (i & 0xff) / 255f;
    }

    public Pigment mult(float f) {
        this.r *= f;
        this.g *= f;
        this.b *= f;
        return this;
    }

    public Pigment mult(Pigment pigment) {
        this.r *= pigment.r;
        this.g *= pigment.g;
        this.b *= pigment.b;
        return this;
    }

    public Pigment add(Pigment pigment) {
        this.r += pigment.r;
        this.g += pigment.g;
        this.b += pigment.b;
        return this;
    }

    public float getRed() {
        return r;
    }

    public float getGreen() {
        return g;
    }

    public float getBlue() {
        return b;
    }

    public Pigment set(Color color) {
        this.r = color.getRed() / 255f;
        this.g = color.getGreen() / 255f;
        this.b = color.getBlue() / 255f;
        return this;
    }

    public Pigment set(Pigment pigment) {
        this.r = pigment.r;
        this.g = pigment.g;
        this.b = pigment.b;
        return this;
    }

    public Pigment sub(Pigment pigment) {
        this.r -= pigment.r;
        this.g -= pigment.g;
        this.b -= pigment.b;
        return this;
    }

    public Color toColor() {
        return new Color(r, g, b);
    }

    public String toString() {
        return String.format("rgb(%.2f, %.2f, %.2f)", r, g, b);
    }

    public String toHexString() {
        return String.format("#%02X%02X%02X", (int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    public String toiRGBString() {
        return String.format("rgb(%d, %d, %d)", (int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    public String toHSLString() {
        float h, s, l;
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        float delta = max - min;
        l = (max + min) / 2;
        if (delta == 0) {
            h = 0;
            s = 0;
        } else {
            if (l < 0.5f) s = delta / (max + min); else s = delta / (2 - max - min);
            float deltaRed = (((max - r) / 6f) + (delta / 2f)) / delta;
            float deltaGreen = (((max - g) / 6f) + (delta / 2f)) / delta;
            float deltaBlue = (((max - b) / 6f) + (delta / 2f)) / delta;
            if (r == max) h = deltaBlue - deltaGreen; else if (g == max) h = (1f / 3f) + deltaRed - deltaBlue; else if (b == max) h = (2f / 3f) + deltaGreen - deltaRed; else throw new RuntimeException("hum hum, no color are the max. Impossible!");
            if (h < 0f) h += 1f;
            if (h > 1f) h -= 1f;
        }
        return String.format("hsl(%.2f, %.2f, %.2f)", h, s, l);
    }

    public float[] getHLS() {
        float h, s, l;
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        float delta = max - min;
        l = (max + min) / 2;
        if (delta == 0) {
            h = 0;
            s = 0;
        } else {
            if (l < 0.5f) s = delta / (max + min); else s = delta / (2 - max - min);
            float deltaRed = (((max - r) / 6f) + (delta / 2f)) / delta;
            float deltaGreen = (((max - g) / 6f) + (delta / 2f)) / delta;
            float deltaBlue = (((max - b) / 6f) + (delta / 2f)) / delta;
            if (r == max) h = deltaBlue - deltaGreen; else if (g == max) h = (1f / 3f) + deltaRed - deltaBlue; else if (b == max) h = (2f / 3f) + deltaGreen - deltaRed; else throw new RuntimeException("hum hum, no color are the max. Impossible!");
            if (h < 0f) h += 1f;
            if (h > 1f) h -= 1f;
        }
        return new float[] { h, l, s };
    }

    /**
	 * @return
	 */
    public int[] getIntRGB() {
        return new int[] { (int) (r * 255), (int) (g * 255), (int) (b * 255) };
    }
}
