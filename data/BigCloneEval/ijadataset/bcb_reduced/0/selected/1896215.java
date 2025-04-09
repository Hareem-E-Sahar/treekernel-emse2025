package com.bluestone.action;

import java.awt.Rectangle;
import com.bluestone.BaseAction;
import com.bluestone.context.IContext;
import com.bluestone.scripts.ActionScript;
import com.bluestone.util.Util;

/**
 * @author <a href="mailto:bluesotne.master@gmail.com">daniel.q</a>
 * 
 */
public class CloseAllPagesAction extends BaseAction {

    public CloseAllPagesAction(ActionScript action) {
        super(action);
    }

    /**
	 * Close all of the browsers
	 */
    public boolean execute(IContext context) {
        if (!super.execute(context)) {
            return false;
        }
        while (true) {
            ActionScript a = new ActionScript();
            a.setType(ActionFactory.MAXPAGEACTION);
            ActionFactory.createAction(a).execute(context);
            java.awt.image.BufferedImage image = robot.createScreenCapture(new Rectangle(5, 5, 10, 10));
            robot.delay(Util.getDelayTime(Util.DELAY300));
            boolean flag = true;
            if (flag) {
                ActionScript b = new ActionScript();
                b.setType(ActionFactory.CLOSEPAGEACTION);
                ActionFactory.createAction(b).execute(context);
                robot.delay(1000);
            } else {
                ActionScript c = new ActionScript();
                c.setType(ActionFactory.MINPAGEACTION);
                ActionFactory.createAction(c).execute(context);
                break;
            }
        }
        return true;
    }
}
