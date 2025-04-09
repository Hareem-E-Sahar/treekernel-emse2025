package com.memoire.bu;

import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import javax.swing.UIManager;

/**
 * A set of basic image filters, all derived from RGBImageFilter.
 */
public class BuFilters {

    public static final LevelFilter LEVEL128 = new LevelFilter(128);

    public static final BlackAndWhiteFilter BW = new BlackAndWhiteFilter();

    public static final GreyFilter GREY = new GreyFilter();

    public static final RedFilter RED = new RedFilter();

    public static final BrighterFilter BRIGHTER = new BrighterFilter();

    public static final DarkerFilter DARKER = new DarkerFilter();

    public static final AlphaFilter ALPHA = new AlphaFilter();

    public static final AntialiasFilter ANTIALIAS = new AntialiasFilter();

    public static final EmbossFilter EMBOSS = new EmbossFilter();

    private static final EnabledFilter ENABLED = new EnabledFilter();

    private static final PressedFilter PRESSED = new PressedFilter();

    private static final RolloverFilter ROLLOVER = new RolloverFilter();

    private static final DisabledFilter DISABLED = new DisabledFilter();

    public static final ImageFilter getEnabled() {
        ImageFilter r = ENABLED;
        Object o = UIManager.get("Theme.enabledIconFilter");
        if (o instanceof ImageFilter) r = (ImageFilter) o;
        return r;
    }

    public static final ImageFilter getPressed() {
        ImageFilter r = PRESSED;
        Object o = UIManager.get("Theme.pressedIconFilter");
        if (o instanceof ImageFilter) r = (ImageFilter) o;
        return r;
    }

    public static final ImageFilter getSelected() {
        ImageFilter r = PRESSED;
        Object o = UIManager.get("Theme.selectedIconFilter");
        if (o instanceof ImageFilter) r = (ImageFilter) o;
        return r;
    }

    public static final ImageFilter getRollover() {
        ImageFilter r = ROLLOVER;
        Object o = UIManager.get("Theme.rolloverIconFilter");
        if (o instanceof ImageFilter) r = (ImageFilter) o;
        return r;
    }

    public static final ImageFilter getDisabled() {
        ImageFilter r = DISABLED;
        Object o = UIManager.get("Theme.disabledIconFilter");
        if (o instanceof ImageFilter) r = (ImageFilter) o;
        return r;
    }

    public static class LevelFilter extends RGBImageFilter {

        protected int level_;

        public LevelFilter(int _level) {
            level_ = _level;
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            int h = (299 * r + 587 * g + 114 * b) / 1000;
            return h < level_ ? 0xFF000000 : 0xFFFFFFFF;
        }
    }

    static class BlackAndWhiteFilter extends RGBImageFilter {

        protected BlackAndWhiteFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int rgb = _rgb;
            int r = (rgb & 0x00ff0000) >> 16;
            int g = (rgb & 0x0000ff00) >> 8;
            int b = (rgb & 0x000000ff);
            int h = (299 * r + 587 * g + 114 * b) / 1000;
            rgb &= 0xFF000000;
            if (h <= 64) ; else if (h >= 224) rgb |= 0x00FFFFFF; else if ((h <= 96) && ((_x % 2 != 0) || (_y % 2 != 0))) ; else if ((h <= 160) && ((_x + _y) % 2 == 0)) ; else if ((_x % 2 == 0) && (_y % 2 == 0)) ; else rgb |= 0x00FFFFFF;
            return rgb;
        }

        public String toString() {
            return "BuFilters.BW";
        }
    }

    static class GreyFilter extends RGBImageFilter {

        protected GreyFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            int h = (299 * r + 587 * g + 114 * b) / 1000;
            r = g = b = h;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.GREY";
        }
    }

    static class RedFilter extends RGBImageFilter {

        protected RedFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            r = 255;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.RED";
        }
    }

    static class BrighterFilter extends RGBImageFilter {

        protected BrighterFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            r += 128;
            g += 128;
            b += 128;
            if (r > 255) r = 255;
            if (r < 0) r = 0;
            if (g > 255) g = 255;
            if (g < 0) g = 0;
            if (b > 255) b = 255;
            if (b < 0) b = 0;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.BRIGHTER";
        }
    }

    static class DarkerFilter extends RGBImageFilter {

        protected DarkerFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            r -= 128;
            g -= 128;
            b -= 128;
            if (r > 255) r = 255;
            if (r < 0) r = 0;
            if (g > 255) g = 255;
            if (g < 0) g = 0;
            if (b > 255) b = 255;
            if (b < 0) b = 0;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.DARKER";
        }
    }

    static class AlphaFilter extends RGBImageFilter {

        protected AlphaFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            a = (a + 256) % 256;
            a >>= 2;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.ALPHA";
        }
    }

    static class AntialiasFilter extends RGBImageFilter {

        protected AntialiasFilter() {
        }

        private int a1_, r1_, g1_, b1_;

        private int a2_, r2_, g2_, b2_;

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            a = (a + 256) % 256;
            a1_ = a;
            r1_ = r;
            g1_ = g;
            b1_ = b;
            if (_x > 0) {
                int N = ((a1_ < 255) ? 2 : 3);
                a = (N * a1_ + a2_) / (N + 1);
                r = (N * a1_ * r1_ + a2_ * r2_) / (N * a1_ + a2_);
                g = (N * a1_ * g1_ + a2_ * g2_) / (N * a1_ + a2_);
                b = (N * a1_ * b1_ + a2_ * b2_) / (N * a1_ + a2_);
                if (r > 255) r = 255;
                if (r < 0) r = 0;
                if (g > 255) g = 255;
                if (g < 0) g = 0;
                if (b > 255) b = 255;
                if (b < 0) b = 0;
            }
            a2_ = Math.max(a1_, 1);
            r2_ = r1_;
            g2_ = g1_;
            b2_ = b1_;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.ANTIALIAS";
        }
    }

    static class EmbossFilter extends RGBImageFilter {

        protected EmbossFilter() {
        }

        private int a1_, a2_, a3_;

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            int h = (299 * r + 587 * g + 114 * b) / 1000;
            a = (a + 256) % 256;
            h = (224 + h) / 2;
            a3_ = a2_;
            a2_ = a1_;
            a1_ = a;
            if (_x == 0) a2_ = a3_ = 0;
            if (_x == 1) a3_ = 0;
            a = 255;
            if ((a3_ != 255) && (a2_ != 255) && (a1_ == 255)) h = 128; else if ((a3_ != 255) && (a2_ == 255) && (a1_ == 255)) h = 255; else if ((a3_ == 255) && (a2_ == 255) && (a1_ != 255)) h = 128; else if ((a3_ == 255) && (a2_ != 255) && (a1_ != 255)) h = 255; else a = 0;
            r = h;
            g = h;
            b = h;
            if (r > 255) r = 255;
            if (r < 0) r = 0;
            if (g > 255) g = 255;
            if (g < 0) g = 0;
            if (b > 255) b = 255;
            if (b < 0) b = 0;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.EMBOSS";
        }
    }

    static class EnabledFilter extends RGBImageFilter {

        protected EnabledFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            return _rgb;
        }

        public String toString() {
            return "BuFilters.ENABLED";
        }
    }

    static class RolloverFilter extends RGBImageFilter {

        protected RolloverFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            r += 64;
            g += 64;
            b += 64;
            if (r > 255) r = 255;
            if (r < 0) r = 0;
            if (g > 255) g = 255;
            if (g < 0) g = 0;
            if (b > 255) b = 255;
            if (b < 0) b = 0;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.ROLLOVER";
        }
    }

    static class PressedFilter extends RGBImageFilter {

        protected PressedFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            r -= 64;
            g -= 64;
            b -= 64;
            if (r > 255) r = 255;
            if (r < 0) r = 0;
            if (g > 255) g = 255;
            if (g < 0) g = 0;
            if (b > 255) b = 255;
            if (b < 0) b = 0;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.PRESSED";
        }
    }

    static class DisabledFilter extends RGBImageFilter {

        protected DisabledFilter() {
        }

        public int filterRGB(int _x, int _y, int _rgb) {
            int a = (_rgb & 0xff000000) >> 24;
            int r = (_rgb & 0x00ff0000) >> 16;
            int g = (_rgb & 0x0000ff00) >> 8;
            int b = (_rgb & 0x000000ff);
            int h = (299 * r + 587 * g + 114 * b) / 1000;
            r = h;
            g = h;
            b = h;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public String toString() {
            return "BuFilters.DISABLED";
        }
    }
}
