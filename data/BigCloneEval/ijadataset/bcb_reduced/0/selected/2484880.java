package com.bluestone.action;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import com.bluestone.BaseAction;
import com.bluestone.context.IContext;
import com.bluestone.scripts.ActionScript;
import com.bluestone.scripts.Browser;
import com.bluestone.scripts.Proxy;
import com.bluestone.util.Util;

/**
 * @author <a href="mailto:bluesotne.master@gmail.com">daniel.q</a>
 *
 */
public class SetProxyAction extends BaseAction {

    public SetProxyAction(ActionScript action) {
        super(action);
    }

    public boolean execute(IContext context) {
        if (!super.execute(context)) {
            return false;
        }
        Proxy proxy = this.actionContext.getProjectContext().getProject().getProxy();
        if (proxy != null) {
            int httpport = proxy.getHttpport();
            String servername = proxy.getServername();
            String type = this.actionContext.getProjectContext().getProject().getBrowser().getType();
            ActionScript a = new ActionScript();
            a.setType(ActionFactory.OPENURLACTION);
            a.setPara("target", "about:blank");
            ActionFactory.createAction(a).execute(context);
            ActionScript b = new ActionScript();
            b.setType(ActionFactory.MAXPAGEACTION);
            ActionFactory.createAction(b).execute(context);
            if (Browser.IE6.equalsIgnoreCase(type) || Browser.IE7.equalsIgnoreCase(type)) {
                robot.delay(2000);
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_T);
                robot.delay(500);
                robot.keyRelease(KeyEvent.VK_T);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.delay(500);
                robot.keyPress(KeyEvent.VK_O);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_O);
                robot.delay(500);
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_SHIFT);
                robot.delay(500);
                for (int i = 0; i < 4; i++) {
                    robot.keyPress(KeyEvent.VK_RIGHT);
                    robot.delay(200);
                    robot.keyRelease(KeyEvent.VK_RIGHT);
                    robot.delay(200);
                }
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_L);
                robot.delay(500);
                robot.keyRelease(KeyEvent.VK_L);
                robot.keyRelease(KeyEvent.VK_ALT);
                for (int i = 0; i < 2; i++) {
                    robot.delay(200);
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.delay(200);
                    robot.keyRelease(KeyEvent.VK_TAB);
                }
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_SPACE);
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(200);
                ActionScript c = new ActionScript();
                c.setType(ActionFactory.INPUTKEYACTION);
                c.setPara("value", servername);
                ActionFactory.createAction(c).execute(context);
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(200);
                ActionScript d = new ActionScript();
                d.setType(ActionFactory.INPUTKEYACTION);
                d.setPara("value", "" + httpport);
                ActionFactory.createAction(d).execute(context);
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.delay(200);
                ActionScript e = new ActionScript();
                e.setType(ActionFactory.CLOSEPAGEACTION);
                ActionFactory.createAction(e).execute(context);
                ActionScript f = new ActionScript();
                f.setType(ActionFactory.CLOSEPAGEACTION);
                ActionFactory.createAction(f).execute(context);
            } else if (Browser.FIREFOX2.equalsIgnoreCase(type) || Browser.FIREFOX3.equalsIgnoreCase(type)) {
                robot.delay(2000);
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_T);
                robot.delay(500);
                robot.keyRelease(KeyEvent.VK_T);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.delay(500);
                robot.keyPress(KeyEvent.VK_O);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_O);
                Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                int l = -1;
                int c = -1;
                BufferedImage temp = robot.createScreenCapture(rect);
                BufferedImage temp1;
                for (int i = 0; i < 7; i++) {
                    robot.keyPress(KeyEvent.VK_RIGHT);
                    robot.delay(200);
                    robot.keyRelease(KeyEvent.VK_RIGHT);
                    robot.delay(200);
                    temp1 = robot.createScreenCapture(rect);
                    int x = compareImage(temp, temp1);
                    if (l == -1) {
                        l = x;
                        c = i;
                    }
                    if (x > l) {
                        l = x;
                        c = i;
                    }
                    temp = temp1;
                }
                for (int i = 0; i <= c; i++) {
                    robot.keyPress(KeyEvent.VK_RIGHT);
                    robot.delay(200);
                    robot.keyRelease(KeyEvent.VK_RIGHT);
                    robot.delay(200);
                }
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(200);
                for (int i = 0; i < 3; i++) {
                    robot.keyPress(KeyEvent.VK_LEFT);
                    robot.delay(200);
                    robot.keyRelease(KeyEvent.VK_LEFT);
                    robot.delay(200);
                }
                robot.keyPress(KeyEvent.VK_RIGHT);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_RIGHT);
                robot.delay(1000);
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(200);
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.delay(200);
                robot.delay(1000);
                for (int i = 0; i < 2; i++) {
                    robot.delay(200);
                    robot.keyPress(KeyEvent.VK_RIGHT);
                    robot.delay(200);
                    robot.keyRelease(KeyEvent.VK_RIGHT);
                }
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(200);
                ActionScript g = new ActionScript();
                g.setType(ActionFactory.INPUTKEYACTION);
                g.setPara("value", servername);
                ActionFactory.createAction(g).execute(context);
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(200);
                ActionScript h = new ActionScript();
                h.setType(ActionFactory.INPUTKEYACTION);
                h.setPara("value", "" + httpport);
                ActionFactory.createAction(h).execute(context);
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.delay(200);
                for (int i = 0; i < 3; i++) {
                    robot.delay(200);
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.delay(200);
                    robot.keyRelease(KeyEvent.VK_TAB);
                }
                robot.delay(200);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.delay(200);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.delay(200);
                ActionScript i = new ActionScript();
                i.setType(ActionFactory.CLOSEPAGEACTION);
                ActionFactory.createAction(i).execute(context);
            }
        }
        return true;
    }

    private int compareImage(BufferedImage image1, BufferedImage image2) {
        int x = 0;
        int startx = -1;
        int stopx = -1;
        for (int i = 0; i < image1.getHeight() - 1; i++) {
            for (int j = 0; j < image1.getWidth() - 1; j++) {
                int rgb1 = image1.getRGB(j, i);
                int rgb2 = image2.getRGB(j, i);
                if (rgb1 == rgb2) {
                    if (startx != -1) {
                        stopx = j;
                        break;
                    }
                    continue;
                } else {
                    startx = j;
                }
            }
        }
        return startx + (stopx - startx) / 2;
    }
}
