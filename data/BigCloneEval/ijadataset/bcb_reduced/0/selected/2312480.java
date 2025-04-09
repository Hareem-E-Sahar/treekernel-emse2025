package org.openfibs.board.vector2d;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

public class FontResolver {

    static final int FONT_CACHE_SIZE = 20;

    public static class FontCacheKey {

        String testLabel;

        String fontName;

        Rectangle2D boundingRect;

        int fontStyle;

        FontRenderContext renderContext;

        private final int hashCode;

        public FontCacheKey(String testLabel, String fontName, int fontStyle, Rectangle2D boundingRect, FontRenderContext renderContext) {
            this.testLabel = testLabel;
            this.fontName = fontName;
            this.boundingRect = boundingRect;
            this.renderContext = renderContext;
            this.fontStyle = fontStyle;
            hashCode = testLabel.hashCode() + fontName.hashCode() + renderContext.hashCode() + fontStyle + (int) boundingRect.getWidth() + (int) boundingRect.getHeight();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof FontCacheKey) {
                FontCacheKey that = (FontCacheKey) other;
                return this.testLabel.equals(that.testLabel) && this.fontName.equals(that.fontName) && this.renderContext.equals(that.renderContext) && this.fontStyle == that.fontStyle && this.boundingRect.getWidth() == that.boundingRect.getWidth() && this.boundingRect.getHeight() == that.boundingRect.getHeight();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "[" + "testLabel" + "='" + testLabel + "'" + "fontName" + "='" + fontName + "'" + "boundingRect" + "=" + boundingRect + "renderContext" + "=" + renderContext + "fontStyle" + "=" + fontStyle + "]";
        }
    }

    static final LRUCache<FontCacheKey, Font> cache = new LRUCache<FontCacheKey, Font>(FONT_CACHE_SIZE);

    public static final Font resolveFont(String testLabel, String fontName, int fontStyle, Rectangle2D boundingRect, FontRenderContext renderContext) {
        final FontCacheKey key = new FontCacheKey(testLabel, fontName, fontStyle, boundingRect, renderContext);
        Font font = (cache.get(key));
        if (font == null) {
            font = calculateBoundedFont(testLabel, fontName, fontStyle, boundingRect, renderContext);
            cache.put(key, font);
        }
        return font;
    }

    protected static Font calculateBoundedFont(String testLabel, String fontName, int fontStyle, Rectangle2D boundingRect, FontRenderContext renderContext) {
        Font currentFont = null;
        int currentFontSize = 1;
        int tooSmall = currentFontSize;
        int tooBig = Integer.MAX_VALUE;
        while (tooBig == Integer.MAX_VALUE) {
            final Font font = new Font(fontName, fontStyle, currentFontSize);
            if (fitsIntoBound(boundingRect, renderContext, testLabel, font)) {
                tooSmall = currentFontSize;
                currentFontSize *= 2;
            } else {
                tooBig = currentFontSize;
            }
        }
        boolean searching = true;
        while (searching) {
            currentFontSize = (tooSmall + tooBig) / 2;
            final Font font = new Font(fontName, fontStyle, currentFontSize);
            if (fitsIntoBound(boundingRect, renderContext, testLabel, font)) {
                currentFont = font;
                if (tooSmall == currentFontSize) {
                    searching = false;
                } else {
                    tooSmall = currentFontSize;
                }
            } else {
                tooBig = currentFontSize;
            }
        }
        ;
        return currentFont;
    }

    private static final boolean fitsIntoBound(Rectangle2D boundingRect, FontRenderContext frc, String testString, Font font) {
        final Rectangle2D lbounds = new TextLayout(testString, font, frc).getBounds();
        return boundingRect.getHeight() >= lbounds.getHeight() && boundingRect.getWidth() >= lbounds.getWidth();
    }
}
