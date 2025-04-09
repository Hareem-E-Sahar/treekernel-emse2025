package com.mainatom.testutils;

import com.mainatom.ui.*;
import com.mainatom.utils.*;
import junit.framework.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

/**
 * Тестирование фреймов
 */
public class RobotFrame {

    private AFrame _frame;

    private TestCase _testCase;

    private int _numScreenShot;

    private boolean _modal;

    private RobotFrame _parent;

    public RobotFrame() {
    }

    public RobotFrame(AFrame frame) {
        _frame = frame;
    }

    public RobotFrame(AFrame frame, TestCase testCase) {
        _frame = frame;
        _testCase = testCase;
    }

    public AFrame getFrame() {
        return _frame;
    }

    public void setFrame(AFrame frame) {
        _frame = frame;
    }

    public TestCase getTestCase() {
        if (getParent() != null) {
            return getParent().getTestCase();
        }
        return _testCase;
    }

    public void setTestCase(TestCase testCase) {
        _testCase = testCase;
    }

    public RobotFrame getParent() {
        return _parent;
    }

    public void setParent(RobotFrame parent) {
        _parent = parent;
    }

    public void show(IRobotFrameRunner run) {
        if (isModal()) {
            getFrame().show("", "dialog", "");
        } else {
            getFrame().show("", "normal", "");
        }
        try {
            if (run != null) {
                run.exec(this);
            }
        } catch (Exception e) {
            throw new MfErrorWrap(e);
        } finally {
            getFrame().close();
        }
    }

    public void screenShot(File file, Insets inset) throws Exception {
        if (inset == null) {
            inset = new Insets(0, 0, 0, 0);
        }
        Robot robot = new Robot();
        robot.setAutoWaitForIdle(true);
        getFrame().refresh();
        robot.waitForIdle();
        Point xy = getFrame().getForm().getLocation();
        Dimension wh = getFrame().getForm().getSize();
        SwingUtilities.convertPointToScreen(xy, getFrame().getForm().getCtrl());
        if (getFrame().getForm().getWindow().getCtrl() instanceof Window) {
            xy = getFrame().getForm().getWindow().getLocation();
            wh = getFrame().getForm().getWindow().getSize();
        }
        Rectangle r = new Rectangle(xy.x - inset.left, xy.y - inset.top, wh.width + inset.right + inset.left, wh.height + inset.bottom + inset.right);
        if (r.width == 0) {
            r.width = 10;
        }
        if (r.height == 0) {
            r.height = 10;
        }
        BufferedImage image = robot.createScreenCapture(r);
        file.getParentFile().mkdirs();
        ImageIO.write(image, "png", file);
    }

    public File getScreenShotFile(String suffix) {
        File f = new File("temp/testScreenShoot/" + getTestCase().getClass().getSimpleName() + "/" + getTestCase().getName() + "_" + suffix + ".png");
        return f;
    }

    public int getNumScreenShot() {
        if (getParent() != null) {
            return getParent().getNumScreenShot();
        }
        return _numScreenShot;
    }

    public void nextNumScreenShot() {
        if (getParent() != null) {
            getParent().nextNumScreenShot();
        } else {
            _numScreenShot++;
        }
    }

    public void screenShot(Insets inset) throws Exception {
        nextNumScreenShot();
        File f = getScreenShotFile("" + getNumScreenShot());
        screenShot(f, inset);
    }

    public void screenShot() throws Exception {
        screenShot(null);
    }

    public void screenShot(int inset) throws Exception {
        screenShot(new Insets(inset, inset, inset, inset));
    }

    public void keypress(String keys) throws Exception {
        Robot r = new Robot();
        r.setAutoWaitForIdle(true);
        String[] ar = keys.split(" |\t|\n");
        for (String key : ar) {
            KeyStroke ks = UIService.getInst().shortcut(key);
            if (ks == null) {
                throw new Exception("Не правильная комбинация клавиш [" + key + "]");
            }
            r.waitForIdle();
            r.keyPress(ks.getKeyCode());
            r.delay(5);
            r.keyRelease(ks.getKeyCode());
            r.waitForIdle();
        }
    }

    /**
     * Изготовление 3-х снимков: в текущем размере, при уменьшении на resize и при увеличении на resize
     *
     * @param resize
     */
    public void screenShotResize(int resize) throws Exception {
        screenShot();
        AWindow w = getFrame().getForm().getWindow();
        Dimension sz = w.getSize();
        sz.width = sz.width - resize;
        sz.height = sz.height - resize;
        w.setSize(sz);
        File f = getScreenShotFile("" + getNumScreenShot() + "_small");
        screenShot(f, null);
        sz.width = sz.width + resize + resize;
        sz.height = sz.height + resize + resize;
        w.setSize(sz);
        f = getScreenShotFile("" + getNumScreenShot() + "_big");
        screenShot(f, null);
        sz.width = sz.width - resize;
        sz.height = sz.height - resize;
        w.setSize(sz);
    }

    public void textpress(String text) throws Exception {
        Robot r = new Robot();
        r.setAutoWaitForIdle(true);
        KeyStroke ks;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char cu = Character.toUpperCase(c);
            if (!(cu >= '0' && cu <= '9' || cu >= 'A' && cu <= 'Z' || cu == '.')) {
                throw new Exception("Не правильный символ при вводе текста [" + c + "]");
            }
            ks = UIService.getInst().getShortcutFactory().getKeyStroke("" + cu);
            r.waitForIdle();
            if (c == cu && cu >= 'A') {
                r.keyPress(KeyEvent.VK_SHIFT);
            }
            r.keyPress(ks.getKeyCode());
            r.delay(5);
            r.keyRelease(ks.getKeyCode());
            if (c == cu && cu >= 'A') {
                r.keyRelease(KeyEvent.VK_SHIFT);
            }
            r.waitForIdle();
        }
    }

    public void mousemove(int x, int y, JComponent relative) throws Exception {
        Robot r = new Robot();
        r.setAutoWaitForIdle(true);
        Point p = new Point(x, y);
        SwingUtilities.convertPointToScreen(p, relative);
        r.mouseMove(p.x, p.y);
        r.waitForIdle();
        r.delay(200);
    }

    public void mousemove(int x, int y, AControl relative) throws Exception {
        mousemove(x, y, relative.getCtrl());
    }

    public void mousepress(int x, int y, JComponent relative, String button) throws Exception {
        int b = InputEvent.BUTTON1_MASK;
        if (button.equals("right") || button.equals("r")) {
            b = InputEvent.BUTTON3_MASK;
        }
        Robot r = new Robot();
        r.setAutoWaitForIdle(true);
        Point p = new Point(x, y);
        SwingUtilities.convertPointToScreen(p, relative);
        r.mouseMove(p.x, p.y);
        r.waitForIdle();
        r.mousePress(b);
        r.delay(5);
        r.mouseRelease(b);
        r.waitForIdle();
        r.delay(200);
    }

    public void mousepress(int x, int y) throws Exception {
        mousepress(x, y, getFrame().getCtrl(), "left");
    }

    public void mousepress(int x, int y, String button) throws Exception {
        mousepress(x, y, getFrame().getCtrl(), button);
    }

    public void mousepress(int x, int y, AControl ctrl, String button) throws Exception {
        mousepress(x, y, ctrl.getCtrl(), button);
    }

    /**
     * Возвращает верхнюю форму на экране
     *
     * @return
     */
    public AFrame getTopScreenFrame() {
        return UIService.getInst().getTopScreenFrame();
    }

    /**
     * Создает RobotFrame для фрейма верхнего уровня
     *
     * @return
     */
    public RobotFrame getTopRobotFrame() throws Exception {
        Robot robot = new Robot();
        robot.setAutoWaitForIdle(true);
        getFrame().refresh();
        robot.waitForIdle();
        AFrame f = getTopScreenFrame();
        if (f == null) {
            throw new MfError("Не найден frame верхнего уровня");
        }
        if (f == getFrame()) {
            throw new MfError("Текущее верхнее окно принадлежит активному RobotFrame");
        }
        RobotFrame r = new RobotFrame(f);
        r.setParent(this);
        getFrame().refresh();
        f.refresh();
        robot.waitForIdle();
        return r;
    }

    public boolean isModal() {
        return _modal;
    }

    /**
     * Установить модальный режим. В модальном режиме первый фрейм показывается в модальном
     * режиме (в диалог) остальные проскакивают
     *
     * @param modal
     */
    public void setModal(boolean modal) {
        if (modal) {
            String s = System.getProperty("batch.test");
            if ("true".equals(s)) {
                return;
            }
            System.setProperty("batch.test", "true");
        }
        _modal = modal;
    }
}
