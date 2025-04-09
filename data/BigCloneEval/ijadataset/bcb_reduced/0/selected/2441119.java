package org.base.apps.core.swing;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.base.apps.api.Application;
import org.base.apps.api.ApplicationData;
import org.base.apps.api.ApplicationInfo;
import org.base.apps.api.events.AppEvent;
import org.base.apps.api.events.AppEventListener;
import org.base.apps.beans.BeanContainer;
import org.base.apps.core.Env;
import org.base.apps.core.swing.BaseAction.Axn;
import org.base.apps.util.Util;
import org.base.apps.util.view.swing.BaseFrame;
import org.base.apps.util.view.swing.SwingUtil;

/**
 * A frame to hold multiple BASE applications, implemented as a singleton
 * (via {@link #getAppFrame()}).
 * 
 * @author Kevan Simpson
 */
public class AppFrame extends BaseFrame implements AppEventListener {

    private static final long serialVersionUID = -5662292458087974572L;

    private ActionMap mBaseActions;

    private AppTabs mAppTabs;

    /**
     * 
     */
    protected AppFrame() {
        this("BASE-Apps");
    }

    /**
     * @param title
     */
    protected AppFrame(String title) {
        super(title);
        init();
    }

    /** @see org.base.apps.api.events.AppEventListener#eventOccurred(org.base.apps.api.events.AppEvent) */
    @Override
    public void eventOccurred(AppEvent evt) {
        switch(evt.getType()) {
            case close:
                {
                    if (getSelectedApplication() == null) break;
                    getAppTabs().removeApplication(getSelectedApplication().getName());
                    break;
                }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <D, C extends Component> void addApplication(final Application<D, C> appl) {
        try {
            getAppTabs().addApplication(appl);
        } catch (Exception ex) {
            SwingUtil.displayErrorDialog(Util.format("An error occurred creating {0} application view: {1}", getTitle(), ex.getMessage()), ex, "Launch Failed!");
            return;
        }
        try {
            ApplicationData<D> data = appl.getData();
            if (data != null) {
                data.load();
                if (data.getBean() != null && appl.getView() instanceof BeanContainer) {
                    ((BeanContainer) appl.getView()).setBean(data.getBean());
                }
            } else {
                System.out.println("not loading null data");
            }
        } catch (Exception ex) {
            SwingUtil.displayErrorDialog(Util.format("An error occurred loading {0} application data: {1}", getTitle(), ex.getMessage()), ex, "Launch Failed!");
            return;
        }
        MenuManager.installAppMenu(appl.getApplicationInfo());
        Env.registerApplication(appl);
    }

    public <D, C extends Component> void removeApplication(final String appName) {
        try {
            getAppTabs().removeApplication(appName);
        } catch (Exception ex) {
            SwingUtil.displayErrorDialog(Util.format("An error occurred removing {0} application view: {1}", getTitle(), ex.getMessage()), ex, "Application Shutdown Failed!");
            return;
        }
        MenuManager.uninstallAppMenu(appName);
        Env.unregisterApplication(appName);
    }

    /** Closes the frame with a nifty graphical effect. */
    public void dissolve(boolean spin) {
        new Dissolver(spin).dissolveExit();
    }

    public ApplicationInfo getSelectedApplication() {
        return getAppTabs().getSelectedApplication();
    }

    /**
     * @return the baseActions
     */
    public ActionMap getBaseActions() {
        return mBaseActions;
    }

    /**
     * @param amap the baseActions to set
     */
    public void setBaseActions(ActionMap amap) {
        mBaseActions = amap;
        if (amap != null) {
            for (Object key : amap.keys()) {
                BaseAction axn = (BaseAction) amap.get(key);
                axn.setBean(this);
            }
        }
    }

    protected void init() {
        setBaseActions(BaseAction.newBaseActions());
        getContentPane().setLayout(new BorderLayout(5, 5));
        setAppTabs(new AppTabs(this));
        getContentPane().add(getAppTabs(), BorderLayout.CENTER);
        initMenuBar();
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        JButton btnSave = new JButton(getBaseActions().get(Axn.Save));
        pnl.add(btnSave);
        JButton btnExit = new JButton(getBaseActions().get(Axn.Exit));
        pnl.add(btnExit);
        getContentPane().add(pnl, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {

            /** @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent) */
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    AppFrame.this.getBaseActions().get(Axn.Exit).actionPerformed(new ActionEvent(AppFrame.this, -1, Axn.Exit.name()));
                } catch (Exception ex) {
                    SwingUtil.displayErrorDialog(Util.format("An error occurred saving {0} application data: {1}", getTitle(), ex.getMessage()), ex, "Save Failed!");
                    return;
                }
            }
        });
    }

    protected void initMenuBar() {
        setJMenuBar(new JMenuBar());
        JMenu base = new JMenu("BASE");
        base.add(new JMenuItem(getBaseActions().get(Axn.Save)));
        base.add(new JSeparator());
        base.add(new JMenuItem(getBaseActions().get(Axn.Save)));
        getJMenuBar().add(base);
    }

    /**
     * @return the appTabs
     */
    protected AppTabs getAppTabs() {
        return mAppTabs;
    }

    /**
     * @param appTabs the appTabs to set
     */
    protected void setAppTabs(AppTabs appTabs) {
        mAppTabs = appTabs;
    }

    private static AppFrame mSingletonFrame = null;

    public static AppFrame getAppFrame() {
        if (mSingletonFrame == null) {
            synchronized (AppFrame.class) {
                mSingletonFrame = new AppFrame("BASE-Apps");
            }
        }
        return mSingletonFrame;
    }

    class Dissolver extends JComponent implements Runnable {

        private static final long serialVersionUID = 5109905277303255130L;

        private Window mFullScreen;

        private int mCount;

        private BufferedImage mFrameBuffer, mScreenBuffer;

        private boolean mSpin = false;

        public Dissolver(boolean spin) {
            mSpin = spin;
        }

        public void dissolveExit() {
            try {
                Robot robot = new Robot();
                Rectangle frameRect = AppFrame.this.getBounds();
                mFrameBuffer = robot.createScreenCapture(frameRect);
                AppFrame.this.setVisible(false);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle screenRect = new Rectangle(0, 0, screenSize.width, screenSize.height);
                mScreenBuffer = robot.createScreenCapture(screenRect);
                mFullScreen = new Window(new JFrame("Exiting BASE... Goodbye!"));
                mFullScreen.setSize(screenSize);
                mFullScreen.add(this);
                this.setSize(screenSize);
                mFullScreen.setVisible(true);
                new Thread(this).start();
            } catch (AWTException awt) {
                System.err.println("Exiting BASE: " + awt.getMessage());
                System.exit(0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /** @see javax.swing.JComponent#paint(java.awt.Graphics) */
        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g.drawImage(mScreenBuffer, -mFullScreen.getX(), -mFullScreen.getY(), null);
            if (mSpin) {
                AffineTransform oldTrans = g2.getTransform();
                g2.translate(AppFrame.this.getX(), AppFrame.this.getY());
                g2.translate(-((mCount + 1) * (AppFrame.this.getX() + AppFrame.this.getWidth()) / 10), 0);
                float scale = 1f / ((float) mCount + 1);
                g2.scale(scale, scale);
                g2.rotate(((float) mCount) / 3.14 / 1.3, AppFrame.this.getWidth() / 2, AppFrame.this.getHeight() / 2);
                g2.drawImage(mFrameBuffer, 0, 0, null);
                g2.setTransform(oldTrans);
            } else {
                Composite oldComp = g2.getComposite();
                Composite fade = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - ((float) mCount) / 20f);
                g2.setComposite(fade);
                g2.drawImage(mFrameBuffer, AppFrame.this.getX(), AppFrame.this.getY(), null);
                g2.setComposite(oldComp);
            }
        }

        /** @see java.lang.Runnable#run() */
        public void run() {
            try {
                mCount = 0;
                for (int i = 0; i < 20; i++) {
                    mCount = i;
                    mFullScreen.repaint();
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
            }
            System.exit(0);
        }
    }
}
