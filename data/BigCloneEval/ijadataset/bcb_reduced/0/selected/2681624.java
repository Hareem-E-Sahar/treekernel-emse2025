package net.hanjava.widget;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Customizable Notifier API
 * 
 * @author accent
 */
public abstract class AbstractNotifier {

    /** Window�� close�ϴ� �� Animation�� ���̴�. Animation�� ������ ��� dispose�� �� �� ���� �ִ� */
    public interface Animation {

        /** animation ��ü�� �����ϸ鼭 ������ Thread�� ���ϴ°�? */
        boolean hasOwnThread();

        boolean isRunning();

        /**
		 * @param src
		 *            ����� �׸��� �ִ� component
		 * @param dest
		 *            ���������� �������� component
		 */
        void doAnimation(VoidPanel src, JComponent dest);
    }

    private Point lastOrigin = null;

    private List<NotifierWindow> activeWindows = new Vector<NotifierWindow>();

    public void doNotify() {
        Point origin = nextOrigin();
        Dimension size = getRequiredSize();
        Icon back = null;
        Rectangle rect = new Rectangle(origin, size);
        try {
            back = snapshot(rect);
        } catch (AWTException e) {
            URL url = null;
            try {
                url = new URL("http://www.thinkfree.com/images/common/logo.gif");
            } catch (MalformedURLException e1) {
            }
            back = new ImageIcon(url);
        }
        Animation anim = createAnimation();
        if (anim == null) {
            anim = new SlideUpAnimation();
        }
        NotifierWindow win = new NotifierWindow(this, back, anim);
        win.setBounds(rect);
        JComponent slidingComp = createSlidingComponent();
        win.init(slidingComp);
        win.setVisible(true);
        win.animate();
    }

    /** SlidingContainer�� Size�� ��ȯ */
    protected abstract Dimension getRequiredSize();

    /**
	 * SlidingContainer�� ���� Component�� ����� ��ȯ�Ѵ�. ��ȯ�� Component��
	 * getRequiredSize()�� ���ŭ �ڵ����� resize�ȴ�.
	 */
    protected abstract JComponent createSlidingComponent();

    /** null�� ��ȯ�ϸ� �⺻ Animation(SlideUp)�� ���ȴ� */
    protected abstract Animation createAnimation();

    void addActiveWindow(NotifierWindow win) {
        activeWindows.add(win);
        lastOrigin = win.getLocation();
    }

    void removeActiveWindow(NotifierWindow win) {
        activeWindows.remove(win);
        if (activeWindows.size() <= 0) {
            lastOrigin = null;
        }
    }

    private Point nextOrigin() {
        Dimension rSize = getRequiredSize();
        if (lastOrigin == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle rect = ge.getMaximumWindowBounds();
            int endX = rect.x + rect.width - rSize.width;
            int endY = rect.y + rect.height;
            lastOrigin = new Point(endX, endY);
        }
        return new Point(lastOrigin.x, lastOrigin.y - rSize.height);
    }

    private Icon snapshot(Rectangle rect) throws AWTException {
        Robot r = new Robot();
        BufferedImage img = r.createScreenCapture(rect);
        Icon icon = new ImageIcon(img);
        return icon;
    }
}
