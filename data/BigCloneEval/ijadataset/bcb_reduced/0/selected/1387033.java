package com.patientis.framework.controls.forms;

import java.awt.image.BufferedImage;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.AlphaComposite;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.lang.ref.WeakReference;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import com.patientis.framework.image.GaussianBlurFilter;
import com.patientis.framework.controls.ISMemory;
import com.patientis.framework.controls.ISScrollPane;
import com.patientis.model.common.BaseModel;
import com.patientis.model.common.ModelReference;
import com.patientis.framework.controls.menus.ISMenuBuilder;
import com.patientis.framework.locale.FormatUtil;
import com.patientis.framework.locale.ImageUtil;
import com.patientis.framework.locale.SystemUtil;
import com.patientis.framework.logging.Log;
import com.patientis.framework.scripting.ISMediator;
import com.patientis.framework.utility.FileSystemUtil;
import com.patientis.framework.utility.SwingUtil;

/**
 * ISFrame extends JFrame and implements IModelControlled to bind to the container model
 * 
 * <br/>Design Patterns: <a href="/functionality/rm/1000055.html">Form Panel Hierarchy</a>
 * <br/>
 */
public class ISFrame extends JFrame implements IModelControlled {

    private static final long serialVersionUID = 1L;

    /**
	 * Weak reference to the container model
	 */
    private WeakReference<IContainer> containerRef = null;

    /**
	 * @throws HeadlessException
	 */
    public ISFrame() throws HeadlessException {
        super();
        init();
    }

    /**
	 * @param gc
	 */
    public ISFrame(GraphicsConfiguration gc) {
        super(gc);
        init();
    }

    /**
	 * @param title
	 * @param gc
	 */
    public ISFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        init();
    }

    /**
	 * @param title
	 * @throws HeadlessException
	 */
    public ISFrame(String title) throws HeadlessException {
        super(title);
        init();
    }

    /**
	 * Memory management
	 */
    private void init() {
        ISMemory.getInstance().add(this);
        if (FormatUtil.hasDefaultFont()) setFont(FormatUtil.getDefaultFont());
    }

    /**
**
	 * @see com.patientis.framework.controls.forms.IModelControlled#setModel(com.patientis.framework.controls.forms.IContainer)
	 */
    public void setModel(IContainer containerModel) throws Exception {
        if (containerModel == null) {
            throw new NullPointerException();
        }
        if (containerModel.getImagePath() != null) {
            setIconImage(ImageUtil.getImage(containerModel.getImagePath()));
        }
        this.containerRef = new WeakReference<IContainer>(containerModel);
        initialize();
    }

    /**
	 * Bind component
	 * 
	 * @throws Exception
	 */
    private void initialize() throws Exception {
        this.getContentPane().setLayout(new BorderLayout());
        if (containerRef.get() != null) {
            containerRef.get().addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (String.valueOf(ModelReference.BASE).equals(evt.getPropertyName())) {
                        applyModel();
                    }
                }
            });
            applyModel();
        }
    }

    /**
	 * Apply model changes
	 */
    private void applyModel() {
        if (containerRef != null) {
            this.setLocation(containerRef.get().getLocation(SwingUtil.getScreenSize(this)));
            if (containerRef.get().getColorBackground() != null) {
                this.setBackground(containerRef.get().getColorBackground());
            }
            this.setPreferredSize(containerRef.get().getPreferredSize());
            this.setMinimumSize(containerRef.get().getMinimumSize());
            this.setSize(containerRef.get().getSize());
            this.setTitle(containerRef.get().getPanelTitle());
            this.setVisible(containerRef.get().isVisible());
            if (containerRef.get().isMaximize()) {
                try {
                    this.setExtendedState(Frame.MAXIMIZED_BOTH);
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    if (getWidth() < (.95 * dim.getWidth()) || getHeight() < (.95 * dim.getHeight())) {
                        if (dim.getWidth() > 1150) {
                            dim.setSize(1150, dim.getHeight());
                        }
                        if (dim.getHeight() > 750) {
                            dim.setSize(dim.getWidth(), 750);
                        }
                        setSize((int) (.95 * dim.getWidth()), (int) (.90 * dim.getHeight()));
                        setLocation(25, 25);
                    }
                } catch (Exception ex) {
                    Log.exception(ex);
                }
            }
        }
    }

    /**
**
	 * @see com.patientis.framework.controls.forms.IModelControlled#addContainer(java.awt.Container, com.patientis.framework.controls.forms.IContainer)
	 */
    public void addContainer(Container container, IContainer containerModel, ISMediator mediator) {
        this.getContentPane().add(container, containerModel.getLayoutConstraint());
    }

    /**
	 * Add child containers to the frame
	 * 
	 * @param childContainers
	 * @throws Exception
	 */
    public void addContainers(List<IContainer> childContainers, ISMediator mediator, BaseModel model) throws Exception {
        for (IContainer cm : childContainers) {
            if (cm.isMenu()) {
                ISMenuBuilder.buildMenu(this, cm.getMenus(), mediator);
            } else if (cm.isScrollpane()) {
                this.getContentPane().add(new ISScrollPane(cm.getInstance(mediator, model)), cm.getLayoutConstraint());
            } else {
                Component component = cm.getInstance(mediator, model);
                if (component != null) {
                    this.getContentPane().add(component, cm.getLayoutConstraint());
                }
            }
        }
    }

    /**
	 * @see java.lang.Object#finalize()
	 */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ISMemory.getInstance().remove(this);
    }

    /**
	 * Blurred buffer
	 */
    private BufferedImage blurBuffer;

    /**
	 * Backing buffer
	 */
    private BufferedImage backBuffer;

    /**
	 * Alpha value
	 */
    private float alpha = 1f;

    /**
	 * Blur if needed
	 * 
	 * @see java.awt.Container#paint(java.awt.Graphics)
	 */
    @Override
    public void paint(Graphics g) {
        if (isVisible() && blurBuffer != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(backBuffer, 0, 0, null);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.drawImage(blurBuffer, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
            super.paint(g2);
        } else {
            super.paint(g);
        }
    }

    /**
	 * Create the blur
	 */
    public void createBlur() throws Exception {
        blurBuffer = new java.awt.Robot().createScreenCapture(getBounds());
        Graphics2D g2 = blurBuffer.createGraphics();
        paint(g2);
        g2.dispose();
        backBuffer = blurBuffer;
        new GaussianBlurFilter(5).filter(backBuffer, blurBuffer);
    }

    /**
	 * Clear blur
	 */
    public void clearBlur() {
        backBuffer = null;
        blurBuffer = null;
    }

    /**
	 * 
	 * @return
	 */
    public IContainer getContainer() {
        if (containerRef == null) {
            return null;
        } else {
            return containerRef.get();
        }
    }
}
