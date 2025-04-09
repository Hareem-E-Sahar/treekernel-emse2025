package com.bluestone.action;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import com.bluestone.BaseAction;
import com.bluestone.context.IContext;
import com.bluestone.scripts.ActionScript;
import com.bluestone.util.Util;

/**
 * @author <a href="mailto:bluesotne.master@gmail.com">daniel.q</a>
 * 
 */
public class CheckBrowserAction extends BaseAction {

    public CheckBrowserAction(ActionScript action) {
        super(action);
    }

    public boolean execute(IContext context) {
        if (!super.execute(context)) {
            return false;
        }
        String filepath = new File("html/blank.html").getAbsolutePath();
        ActionScript a = new ActionScript();
        a.setType(ActionFactory.OPENURLACTION);
        a.setPara("target", filepath);
        ActionFactory.createAction(a).execute(context);
        int height = countHeight(context);
        this.actionContext.getProjectContext().getProject().getBrowser().setBarHeight(height);
        Util.info("set browser bar height:" + height);
        BufferedImage image = robot.createScreenCapture(new Rectangle(5, 5, 10, 10));
        robot.delay(Util.getDelayTime(Util.DELAY300));
        ActionScript b = new ActionScript();
        b.setType(ActionFactory.CLOSEPAGEACTION);
        b.setPara("delay", "1000");
        ActionFactory.createAction(b).execute(context);
        height = countHeight(context);
        this.actionContext.getProjectContext().getProject().getBrowser().setTitlebarHeight(height);
        Util.info("set browser titlebar height:" + height);
        ActionScript c = new ActionScript();
        c.setType(ActionFactory.CLOSEPAGEACTION);
        c.setPara("delay", "1000");
        ActionFactory.createAction(c).execute(context);
        return true;
    }

    private int countHeight(IContext context) {
        ActionScript a = new ActionScript();
        a.setType(ActionFactory.MAXPAGEACTION);
        a.setPara("delay", "2000");
        ActionFactory.createAction(a).execute(context);
        BufferedImage image = robot.createScreenCapture(new Rectangle(0, 0, 10, 350));
        int count = 0;
        int height = 0;
        int temp = 0;
        for (int i = 0; i < image.getHeight(); i++) {
            int rgb = image.getRGB(5, i);
            if (rgb == temp) {
                count++;
            } else {
                temp = rgb;
                count = 0;
                height = i;
            }
            if (count == 100) {
                break;
            }
        }
        robot.delay(Util.getDelayTime(Util.DELAY300));
        return height;
    }
}
