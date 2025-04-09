package net.tileeditor.source;

import net.engine.Camera;
import net.engine.GamePanel;
import net.engine.picture.ColorUtil;
import net.engine.parser.TextParser;
import net.engine.parser.IntegerPointer;
import net.engine.file.xml.XMLTag;
import net.engine.file.xml.XMLBody;
import net.tileeditor.SimpleObject;
import net.tileeditor.DefaultSimpleObject;
import net.tileeditor.SimpleObjects;
import java.awt.*;

public class ColourWrapper extends SimpleObjectWrapper {

    public ColourWrapper() {
        this.defaultValue = new DefaultSimpleObject(new Color(0, 0, 0, 0));
    }

    public Class getTileClass() {
        return Color.class;
    }

    public SimpleObject getClearValue() {
        return defaultValue;
    }

    public void render(GamePanel gamePanel, Object object, int x, int y, int width, int height, int layer) {
        Color color = (Color) object;
        if (color.getAlpha() == 255) {
            Graphics2D buffer = gamePanel.getBackBuffer();
            buffer.setColor(color);
            Camera camera = gamePanel.getCamera();
            int halfWidth = (width + 1) / 2;
            int halfHeight = (height + 1) / 2;
            x -= camera.getPosition().x;
            y -= camera.getPosition().y;
            int left = x - halfWidth;
            int top = y - halfHeight;
            buffer.fillRect(left, top, width, height);
        }
    }

    public SimpleObject fromXML(XMLBody body, SimpleObjects simpleObjects) {
        return new SimpleObject(body, ColorUtil.fromXML(body), simpleObjects);
    }

    public void toXML(XMLBody body, Object value) {
        ColorUtil.toXML(body, (Color) value);
    }
}
