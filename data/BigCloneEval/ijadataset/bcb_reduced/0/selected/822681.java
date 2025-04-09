package j3dworkbench.gui;

import j3dworkbench.core.J3DWorkbenchConstants;
import j3dworkbench.core.Universe;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;

/**
 * @author Ken Mc Neill
 */
public class Canvas3DExtension extends Canvas3D {

    private static final int SAMPLE_FRAMES = 50;

    private static final String FPS = "fps: ";

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final Font FONT = new Font("SansSerif", Font.BOLD, 16);

    static final GraphicsConfigTemplate3D graphicsconfigtemplate3d = new GraphicsConfigTemplate3D();

    public static final GraphicsConfiguration graphicscconfig;

    static {
        graphicsconfigtemplate3d.setDoubleBuffer(GraphicsConfigTemplate.REQUIRED);
        graphicsconfigtemplate3d.setSceneAntialiasing(GraphicsConfigTemplate.PREFERRED);
        graphicscconfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getBestConfiguration(graphicsconfigtemplate3d);
    }

    private static final boolean fpsCounter = Boolean.valueOf(System.getProperty("j3dwb.showfps"));

    private long timestampPostRender;

    private static Map<String, Object> J3D_PROPS = null;

    static final double PPI_TO_METERS = 0.0254d;

    private static final int SCREEN_DPI_DEFAULT = 96;

    private static final long FPNS_50 = J3DWorkbenchConstants.NANOS_PER_SECOND / 50;

    private static final long FLUX_TOLERANCE = J3DWorkbenchConstants.NANOS_PER_MILLI * 20;

    private static final long BUFFER_FLUX_TOLERANCE = J3DWorkbenchConstants.NANOS_PER_MILLI * 5;

    private long deltaNanos = FPNS_50;

    private long deltaNanosLast = FPNS_50;

    private long deltaNanosAvg = FPNS_50;

    private long timestampPreRender;

    private int frame;

    private long fps;

    private long lastDeltaSwap = 10000000L;

    private long deltaBufferSwap = lastDeltaSwap;

    private long bufferSwapAvg = lastDeltaSwap;

    @SuppressWarnings("unchecked")
    public Canvas3DExtension(GraphicsConfiguration gc) {
        super(gc);
        if (J3D_PROPS == null) {
            J3D_PROPS = queryProperties();
            printProperties();
        }
    }

    public static Canvas3DExtension makeCanvas() {
        Canvas3DExtension canvas = new Canvas3DExtension(graphicscconfig);
        canvas.setScreenPPI(SCREEN_DPI_DEFAULT);
        return canvas;
    }

    public static void printProperties() {
        final Iterator<String> iter = J3D_PROPS.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Logger.getLogger(Universe.J3DWB).fine(key + " = " + J3D_PROPS.get(key));
        }
    }

    public static Map<String, Object> getProperties() {
        return J3D_PROPS;
    }

    /**
	 * Was a workaround for D3D bug--unsure if still necessary
	 * 
	 * @Override(non-Javadoc)
	 * 
	 * @see java.awt.Component#setSize(int, int)
	 */
    public void setSize(int width, int height) {
        width = Math.max(width, 1);
        height = Math.max(height, 1);
        super.setSize(width, height);
    }

    /**
	 * * Was a workaround for D3D bug--unsure if still necessary
	 * 
	 * @see Component#setBounds(int, int, int, int)
	 */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        width = Math.max(width, 1);
        height = Math.max(height, 1);
        super.setBounds(x, y, width, height);
    }

    private void drawFPS() {
        if (frame % SAMPLE_FRAMES == 0) {
            fps = getLastFPSAvg();
        }
        getGraphics2D().setFont(FONT);
        getGraphics2D().drawString(String.valueOf(FPS + fps), 10, 20);
        getGraphics2D().flush(true);
    }

    @Override
    public final void postRender() {
        timestampPostRender = System.nanoTime();
        deltaNanosLast = deltaNanos;
        deltaNanos = timestampPostRender - timestampPreRender;
        if (Math.abs(deltaNanos - deltaNanosLast) < FLUX_TOLERANCE) {
            deltaNanosAvg = (deltaNanos + deltaNanosLast) / 2;
        }
        if (fpsCounter) {
            drawFPS();
        }
        postRender2();
    }

    public void postRender2() {
    }

    @Override
    public final void preRender() {
        frame++;
        timestampPreRender = System.nanoTime();
        if (frame < 2) {
            return;
        }
        lastDeltaSwap = deltaBufferSwap;
        deltaBufferSwap = timestampPreRender - timestampPostRender;
        if (Math.abs(deltaBufferSwap - lastDeltaSwap) < BUFFER_FLUX_TOLERANCE) {
            bufferSwapAvg = (lastDeltaSwap + deltaBufferSwap) / 2;
        }
    }

    public long getLastFPSAvg() {
        return J3DWorkbenchConstants.NANOS_PER_SECOND / getLastFrameDurationAvg();
    }

    public long getLastFrameDurationAvg() {
        return deltaNanosAvg + bufferSwapAvg;
    }

    public int getScreenPPI() {
        final double temp = getScreen3D().getPhysicalScreenWidth() / getScreen3D().getSize().width;
        return (int) (PPI_TO_METERS / temp);
    }

    public void setScreenPPI(int ppi) {
        final double temp = PPI_TO_METERS / ppi;
        getScreen3D().setPhysicalScreenWidth(temp * getScreen3D().getSize().width);
        getScreen3D().setPhysicalScreenHeight(temp * getScreen3D().getSize().height);
    }
}
