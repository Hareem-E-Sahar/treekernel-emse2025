package views.background;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import data.Context;
import data.Data_Interface;
import data.MultiContext;
import views.background.ghost.GhostDropEvent;
import views.background.ghost.GhostDropManagerDemo;
import views.background.ghost.GhostDroppable;
import views.widgets.chart.tests.ChartWidget;
import views.widgets.geographic.ColorItem;
import views.widgets.geographic.EllipseItem;
import views.widgets.geographic.GeographicWidget;
import views.widgets.geographic.LegendItem;

public class MainPanel extends JPanel implements GhostDroppable {

    private static final long serialVersionUID = -6591360056042542999L;

    private static final float VERTICAL_BORDER = 1.0f / 30;

    private static final float HORIZONTAL_BORDER = 1.0f / 40;

    private static final float VERTICAL_GAP = 1.0f / 32;

    private static final float HORIZONTAL_GAP = 1.0f / 44;

    private static final int LEFTBAR_SPACE = 150;

    private static final int LEFTBAR_WIDTH = 110;

    private static final int LEFTBAR_CAPA = 6;

    protected static MainPanel me;

    protected ArrayList<EmptyFrame> frames = new ArrayList<EmptyFrame>();

    public JPanel framesPanel;

    protected RightSideBar rightSideBar;

    protected int nbDisplayedFrames = 4;

    /**
	 * Constructor
	 */
    public MainPanel() {
        super(new GridBagLayout());
        this.setPreferredSize(new Dimension(1000, 700));
        this.setBorder(new LineBorder(Color.black, 1));
        MainWindow.setChartListener(new GhostDropManagerDemo(this));
        MainWindow.getChartListener().addTarget(this);
        MainWindow.setWidgetListener(new GhostDropManagerDemo(this));
        GridBagConstraints framesConstraints = new GridBagConstraints();
        framesConstraints.fill = GridBagConstraints.BOTH;
        framesConstraints.weightx = 1;
        framesConstraints.weighty = 1;
        framesPanel = new JPanel(null);
        framesPanel.setPreferredSize(new Dimension((int) this.getPreferredSize().getWidth() - LEFTBAR_SPACE, (int) this.getPreferredSize().getHeight()));
        framesPanel.setOpaque(false);
        System.out.println("framesPanel : " + framesPanel.getPreferredSize());
        for (int i = 0; i < nbDisplayedFrames; i++) {
            frames.add(new EmptyFrame());
            frames.get(i).setBackground(new Color(0, 200, 250));
            frames.get(i).setVisible(true);
            frames.get(i).setOpaque(true);
            placeFrame(frames.get(i), i);
            framesPanel.add(frames.get(i));
            System.out.println("frame " + i + " : " + frames.get(i).getBounds());
        }
        IdentityHashMap<LegendItem, MultiContext> legendItems = new IdentityHashMap<LegendItem, MultiContext>();
        MultiContext mc1 = new MultiContext();
        mc1.addContext(Data_Interface.getInstance().getContexts(Data_Interface.getInstance().getContextTypes().get(1)).get(9));
        MultiContext mc2 = new MultiContext();
        mc2.addContext(Data_Interface.getInstance().getContexts(Data_Interface.getInstance().getContextTypes().get(2)).get(113));
        legendItems.put(new ColorItem(Color.orange, mc2.shortString()), mc2);
        frames.get(0).setContent(new GeographicWidget(frames.get(0), legendItems));
        frames.get(1).setContent(new ChartWidget(frames.get(1), false));
        frames.get(2).setContent(new ChartWidget(frames.get(2), false));
        for (EmptyFrame ef : frames) {
            System.out.println("\t Frame " + frames.indexOf(ef) + " : " + ef.getWidget());
        }
        GridBagConstraints leftBarConstraints = new GridBagConstraints();
        leftBarConstraints.fill = GridBagConstraints.BOTH;
        leftBarConstraints.weightx = 0.13;
        leftBarConstraints.weighty = 1;
        leftBarConstraints.insets = new Insets(10, 10, 10, 10);
        rightSideBar = new RightSideBar();
        this.add(framesPanel, framesConstraints);
        this.add(rightSideBar, leftBarConstraints);
        me = this;
    }

    /**
	 * Gets the Singleton corresponding to the world panel
	 * @return the only WorldPanel, creates it if it doesn't exist
	 */
    public static MainPanel getInstance() {
        if (me == null) me = new MainPanel();
        return me;
    }

    /**
	 * Redefines the drawing method
	 * @param g The component's Graphics
	 */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Paint oldPaint = g2d.getPaint();
        GradientPaint gradient = new GradientPaint(0, 0, Color.BLACK, 0, (int) (getHeight() * 0.7), Color.WHITE, false);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        placeFrames();
        g2d.setPaint(oldPaint);
    }

    /**
	 * Places the frame at this index in the panel
	 * 
	 * @param frame	The frame that needs to be placed
	 * @param index	The frame index in <i>frames</i>
	 */
    protected void placeFrame(EmptyFrame frame, int index) {
        int width = (int) framesPanel.getPreferredSize().getWidth();
        int height = (int) framesPanel.getPreferredSize().getHeight();
        float frameWidthMultiplier;
        float frameHeightMultiplier;
        int sizeReport = (nbDisplayedFrames + 1) / 2;
        switch(nbDisplayedFrames) {
            case 0:
                System.out.println("Empty frames set");
                break;
            case 1:
                frame.setBounds((int) (width * HORIZONTAL_BORDER), (int) (height * VERTICAL_BORDER), (int) (width * (1 - 2 * HORIZONTAL_BORDER)), (int) (height * (1 - 2 * VERTICAL_BORDER)));
                break;
            case 2:
                frame.setBounds((int) (width * (HORIZONTAL_BORDER * (1 - (index % sizeReport)) + (1 + HORIZONTAL_GAP) / sizeReport * (index % sizeReport))), (int) (height * (VERTICAL_BORDER * (1 - (index / sizeReport)) + (1 + VERTICAL_GAP) / 2 * (index / sizeReport))), (int) (width * (1 - 2 * HORIZONTAL_BORDER)), (int) (height * (1 - 2 * VERTICAL_BORDER - VERTICAL_GAP) / 2));
                break;
            case 3:
            case 4:
                frame.setBounds((int) (width * (HORIZONTAL_BORDER * (1 - (index % sizeReport)) + (1 + HORIZONTAL_GAP) / sizeReport * (index % sizeReport))), (int) (height * (VERTICAL_BORDER * (1 - (index / sizeReport)) + (1 + VERTICAL_GAP) / sizeReport * (index / sizeReport))), (int) (width * (1 - 2 * HORIZONTAL_BORDER - HORIZONTAL_GAP) / 2), (int) (height * (1 - 2 * VERTICAL_BORDER - VERTICAL_GAP) / 2));
                break;
            case 5:
            case 6:
                frameWidthMultiplier = (1 - 2 * HORIZONTAL_BORDER - HORIZONTAL_GAP) / 2;
                frameHeightMultiplier = (1 - 2 * VERTICAL_BORDER - 2 * VERTICAL_GAP) / 3;
                frame.setBounds((int) (width * (HORIZONTAL_BORDER + (index % 2) * (HORIZONTAL_GAP + frameWidthMultiplier))), (int) (height * (VERTICAL_BORDER + (index / 2) * (VERTICAL_GAP + frameHeightMultiplier))), (int) (width * frameWidthMultiplier), (int) (height * frameHeightMultiplier));
                break;
            default:
                break;
        }
    }

    protected void placeFrames() {
        for (EmptyFrame frame : frames) {
            placeFrame(frame, frames.indexOf(frame));
        }
    }

    public Rectangle getDropZoneBounds() {
        return this.getBounds();
    }

    public void ghostDropped(GhostDropEvent e) {
    }
}
