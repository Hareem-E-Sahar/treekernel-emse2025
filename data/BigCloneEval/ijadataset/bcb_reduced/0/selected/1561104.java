package galacticthrone.screen.text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javagame.core.io.video.texture.Texture;
import javax.media.opengl.GL;
import org.apache.log4j.Logger;

/**
 * 
 *
 * @author Jaco van der Westhuizen
 */
public class BitmapFont implements Font {

    private static final Logger logger = Logger.getLogger(BitmapFont.class);

    private final Texture tex;

    private final float sizeX;

    private final float sizeY;

    private final float aspect;

    public BitmapFont(String filename) {
        this.tex = new BitmapFontTexture(filename);
        this.sizeX = 1f / 16f;
        this.sizeY = 1f / 16f;
        aspect = (float) tex.getWidth() / (float) tex.getHeight();
        logger.info("Loaded font '" + filename + "'.");
    }

    public BitmapFont(GL gl, String filename) {
        this.tex = new BitmapFontTexture(gl, filename);
        this.sizeX = 1f / 16f;
        this.sizeY = 1f / 16f;
        aspect = (float) tex.getWidth() / (float) tex.getHeight();
        logger.info("Loaded font '" + filename + "'.");
    }

    /**
	 * Writes text to an OpenGL enabled canvas.

	 * @param gl The current OpenGL context.
	 * @param text The text to write.
	 * @param color The colour of the writing.
	 * @param posX The x-coordinate at the left, right or centre, depending on the alignment.
	 * @param posY The y-coordinate at the bottom.
	 * @param size The vertical size of the writing.
	 * @param align The text's alignment.  Can be <code>Font.ALIGN_LEFT</code>/<code>RIGHT</code>/<code>CENTER</code>.
	 * @param canvasWidth The width of the canvas, in pixels.
	 * @param canvasHeight The height of the canvas, in pixels.
	 */
    @Override
    public void write(GL gl, String[] lines, Color color, float size, byte align, float minX, float maxX, float minY, float maxY) {
        final float charHeight = size;
        final float charWidth = size * aspect;
        tex.activate(gl);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        switch(align) {
            case Font.ALIGN_CENTER:
                {
                    final float center = (minX + maxX) / 2;
                    float top = maxY;
                    for (String line : lines) {
                        float left = center - (line.length() * charWidth) / 2;
                        float bottom = top - charHeight;
                        for (byte b : line.getBytes()) {
                            float right = left + charWidth;
                            float x0 = (b & 15) * sizeX;
                            float y1 = ((b & 240) >> 4) * sizeY;
                            float x1 = x0 + sizeX;
                            float y0 = y1 + sizeY;
                            gl.glTexCoord2f(x0, y0);
                            gl.glVertex2f(left, bottom);
                            gl.glTexCoord2f(x0, y1);
                            gl.glVertex2f(left, top);
                            gl.glTexCoord2f(x1, y1);
                            gl.glVertex2f(right, top);
                            gl.glTexCoord2f(x1, y0);
                            gl.glVertex2f(right, bottom);
                            left += charWidth;
                        }
                        top -= charHeight;
                    }
                    break;
                }
            case Font.ALIGN_RIGHT:
                {
                    final float lright = maxX;
                    float top = maxY;
                    for (String line : lines) {
                        float left = lright - (line.length() * charWidth);
                        float bottom = top - charHeight;
                        for (byte b : line.getBytes()) {
                            float right = left + charWidth;
                            float x0 = (b & 15) * sizeX;
                            float y1 = ((b & 240) >> 4) * sizeY;
                            float x1 = x0 + sizeX;
                            float y0 = y1 + sizeY;
                            gl.glTexCoord2f(x0, y0);
                            gl.glVertex2f(left, bottom);
                            gl.glTexCoord2f(x0, y1);
                            gl.glVertex2f(left, top);
                            gl.glTexCoord2f(x1, y1);
                            gl.glVertex2f(right, top);
                            gl.glTexCoord2f(x1, y0);
                            gl.glVertex2f(right, bottom);
                            left += charWidth;
                        }
                        top -= charHeight;
                    }
                    break;
                }
            default:
                {
                    final float lleft = minX;
                    float top = maxY;
                    for (String line : lines) {
                        float left = lleft;
                        float bottom = top - charHeight;
                        for (byte b : line.getBytes()) {
                            float right = left + charWidth;
                            float x0 = (b & 15) * sizeX;
                            float y1 = ((b & 240) >> 4) * sizeY;
                            float x1 = x0 + sizeX;
                            float y0 = y1 + sizeY;
                            gl.glTexCoord2f(x0, y0);
                            gl.glVertex2f(left, bottom);
                            gl.glTexCoord2f(x0, y1);
                            gl.glVertex2f(left, top);
                            gl.glTexCoord2f(x1, y1);
                            gl.glVertex2f(right, top);
                            gl.glTexCoord2f(x1, y0);
                            gl.glVertex2f(right, bottom);
                            left += charWidth;
                        }
                        top -= charHeight;
                    }
                    break;
                }
        }
        gl.glEnd();
    }

    @Override
    public float calcHeight(String[] text, float size) {
        return size * text.length;
    }

    @Override
    public float calcWidth(String[] text, float size) {
        int maxLen = 0;
        for (String line : text) {
            final int len = line.length();
            if (len > maxLen) maxLen = len;
        }
        return size * aspect * maxLen;
    }

    @Override
    public float calcWidth(String line, float size) {
        return size * aspect * line.length();
    }

    @Override
    public String[] makeLines(String text, float size, float maxWidth) {
        final String[] paras = text.split("\r{0,1}\n{1}");
        final List<String> lines = new ArrayList<String>(paras.length * 2);
        final float maxLineLen = maxWidth / (size * aspect);
        for (String para : paras) {
            final String[] words = para.split("[ \t]");
            StringBuilder line = new StringBuilder();
            int len = 0;
            for (String word : words) {
                if ((len + 1 + word.length() > maxLineLen) && (len > 0)) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                    len = 0;
                }
                if (len > 0) {
                    line.append(" ");
                    len++;
                }
                line.append(word);
                len += word.length();
            }
            if (len > 0) {
                lines.add(line.toString());
            }
        }
        return (String[]) lines.toArray();
    }

    @Override
    public void write(GL gl, String text, Color color, float size, byte align, float minX, float maxX, float minY, float maxY) {
        write(gl, new String[] { text }, color, size, align, minX, maxX, minY, maxY);
    }

    /**
     * @param gl
     */
    public void unload(GL gl) {
        this.tex.unload(gl);
        logger.info("Unloaded a bitmap font.");
    }
}
