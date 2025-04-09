package com.bluestone.action;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import javax.imageio.ImageIO;
import com.bluestone.BaseAction;
import com.bluestone.context.IContext;
import com.bluestone.scripts.ActionScript;
import com.bluestone.scripts.Attributes;
import com.bluestone.scripts.Browser;
import com.bluestone.util.Util;

/**
 * If the target is a coordinate(x,y),then save as image;
 * If the target is "saveAsFile",then save as file;
 * If the target is null,then save the full screen as image.
 * @author <a href="mailto:ming7655@gmail.com">Aaron</a>
 */
public class SaveAsAction extends BaseAction {

    public SaveAsAction(ActionScript action) {
        super(action);
    }

    private String saveAsFile = "saveAsFile";

    public boolean execute(IContext context) {
        if (!super.execute(context)) {
            return false;
        }
        String target = action.getPara("target");
        if (target != null) {
            if (target.indexOf(",") > 0) {
                saveAsImage(context);
            } else if (saveAsFile.equalsIgnoreCase(target)) {
                saveAsFile(context);
            }
        } else {
            saveFullScreenAsImage();
        }
        boolean isSaveSuccessed = isSaveSuccessed(action.getPara("value"));
        if (!isSaveSuccessed) {
            return false;
        }
        String delay = action.getPara("delay");
        if (delay != null && delay.length() > 0) {
            robot.delay(Util.getDelayTime(Integer.parseInt(delay)));
        } else {
            robot.delay(Util.getDelayTime(Util.SAVEASDELAY));
        }
        return true;
    }

    private boolean isSaveSuccessed(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        try {
            FileInputStream sb = new FileInputStream(file);
            int length = sb.available();
            if (length <= 0) {
                return false;
            }
        } catch (Exception e) {
            Util.error(e);
            return false;
        }
        return true;
    }

    /**
	 * Save as File
	 * 
	 */
    private void saveAsFile(IContext context) {
        robot.keyPress(KeyEvent.VK_ALT);
        robot.delay(Util.getDelayTime(Util.DELAY300));
        robot.keyPress(KeyEvent.VK_F);
        robot.delay(Util.getDelayTime(Util.DELAY300));
        robot.keyPress(KeyEvent.VK_A);
        robot.delay(Util.getDelayTime(Util.DELAY300));
        robot.keyRelease(KeyEvent.VK_A);
        robot.delay(Util.getDelayTime(Util.DELAY100));
        robot.keyRelease(KeyEvent.VK_F);
        robot.delay(Util.getDelayTime(Util.DELAY100));
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.delay(Util.getDelayTime(Util.DELAY500));
        saveToPath(context);
        robot.delay(Util.getDelayTime(Util.DELAY1000));
    }

    /**
	 * Save as Image
	 * 
	 */
    private void saveAsImage(IContext context) {
        String target = action.getPara("target");
        int index = target.indexOf("(");
        String temp = target.substring(0, index);
        int pos = target.indexOf(",");
        int endpos = target.indexOf(")");
        int x = Integer.valueOf(target.substring(index + 1, pos)).intValue();
        int y = Integer.valueOf(target.substring((pos + 1), endpos)).intValue();
        if (Attributes.IEXPLORE.equalsIgnoreCase(temp)) {
            y = y + this.actionContext.getProjectContext().getProject().getBarHeight();
        }
        robot.mouseMove(x, y);
        robot.mousePress(KeyEvent.BUTTON3_MASK);
        robot.delay(Util.getDelayTime(Util.DELAY300));
        robot.mouseRelease(KeyEvent.BUTTON3_MASK);
        String browserType = this.actionContext.getProjectContext().getProject().getBrowser().getType();
        if (Browser.FIREFOX2.equalsIgnoreCase(browserType)) {
            robot.delay(Util.getDelayTime(Util.DELAY1000));
            robot.keyPress(KeyEvent.VK_V);
            robot.delay(Util.getDelayTime(Util.DELAY500));
            robot.keyRelease(KeyEvent.VK_V);
            robot.delay(Util.getDelayTime(Util.DELAY1000));
        } else {
            robot.delay(Util.getDelayTime(Util.DELAY1000));
            robot.keyPress(KeyEvent.VK_S);
            robot.delay(Util.getDelayTime(Util.DELAY500));
            robot.keyRelease(KeyEvent.VK_S);
            robot.delay(Util.getDelayTime(Util.DELAY1000));
        }
        saveToPath(context);
    }

    private void saveFullScreenAsImage() {
        BufferedImage fullScreenImage = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        try {
            String value = action.getPara("value");
            checkSaveAsPath(value);
            ImageIO.write(fullScreenImage, "JPEG", new File(value));
        } catch (Exception e) {
            Util.error(e);
        }
    }

    private void saveToPath(IContext context) {
        String value = action.getPara("value");
        action.setPara("value", new File(value).getAbsolutePath());
        ActionScript a = new ActionScript();
        a.setType(ActionFactory.INPUTTEXTACTION);
        a.setPara("value", value);
        BaseAction inputAction = ActionFactory.createAction(a);
        checkSaveAsPath(value);
        if (inputAction.execute(context)) {
            ActionScript b = new ActionScript();
            b.setType(ActionFactory.INPUTKEYACTION);
            b.setPara("target", InputKeyAction.ENTERKEY);
            BaseAction enterAction = ActionFactory.createAction(b);
            if (checkPathIsExisted(value)) {
                enterAction.execute(context);
                ActionScript c = new ActionScript();
                c.setType(ActionFactory.INPUTKEYACTION);
                c.setPara("target", InputKeyAction.LEFTKEY);
                BaseAction leftAction = ActionFactory.createAction(c);
                leftAction.execute(context);
                robot.delay(Util.getDelayTime(Util.DELAY100));
                enterAction.execute(context);
            } else {
                enterAction.execute(context);
            }
        }
    }

    private boolean checkPathIsExisted(String path) {
        File file = new File(path);
        return file.exists();
    }

    private void checkSaveAsPath(String path) {
        String folder = path.substring(0, path.lastIndexOf("\\"));
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void main(String[] args) {
    }
}
