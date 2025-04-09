package com.bluestone.action;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.bluestone.BaseAction;
import com.bluestone.context.IContext;
import com.bluestone.scripts.ActionScript;
import com.bluestone.util.Util;

/**
 * According the rectangle to intercept an image.
 * @author <a href="mailto:bluesotne.master@gmail.com">daniel.q</a>
 *
 */
public class ScreenshotAction extends BaseAction {

    public ScreenshotAction(ActionScript action) {
        super(action);
    }

    public boolean execute(IContext context) {
        if (!super.execute(context)) {
            return false;
        }
        String target = action.getPara("target");
        Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        if (target != null && target.length() > 0) {
            String[] values = target.split(",");
            if (values.length == 4) {
                int x = Integer.parseInt(values[0]);
                int y = Integer.parseInt(values[1]);
                int w = Integer.parseInt(values[2]);
                int h = Integer.parseInt(values[3]);
                rect = new Rectangle(x, y, w, h);
            }
        }
        BufferedImage fullScreenImage = robot.createScreenCapture(rect);
        try {
            String value = action.getPara("value");
            checkPath(value);
            ImageIO.write(fullScreenImage, "JPEG", new File(value));
        } catch (Exception e) {
            Util.error(e);
        }
        return true;
    }

    private void checkPath(String path) {
        String folder = path.substring(0, path.lastIndexOf("\\"));
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
