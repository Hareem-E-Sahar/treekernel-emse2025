package userInterface;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import reprojection.Rectangle;

/**
 * The Image Panel. Contains the picture and the transparent view of the Google earth rendering windows.
 * @author Clement Oliva, Sebastien Thouement.
 *
 */
public class ImagePanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 3675940545140775604L;

    static final int selectionColor = 0xffff0000;

    private BufferedImage snapshot = null;

    private BufferedImage inputImage = null;

    private int[] selection = null;

    protected int selectionSize = 0;

    public BufferedImage getImg() {
        return inputImage;
    }

    private MainWindow mainWindow = null;

    private Thread imageThread = null;

    private boolean running = false;

    private boolean paused = false;

    protected ImagePanel_mouseAdapter imagePanelMouseAdapter;

    int mni = 0;

    int mnj = 0;

    int pixels[] = null;

    int originpixels[] = null;

    boolean firstPointSelected;

    reprojection.Point firstSelectedPoint;

    boolean secondPointSelected;

    reprojection.Point secondSelectedPoint;

    Rectangle subImageRectangle;

    /**
	 * 
	 */
    public ImagePanel() {
        super();
        this.subImageRectangle = new reprojection.Rectangle();
        this.firstSelectedPoint = new reprojection.Point();
        this.secondSelectedPoint = new reprojection.Point();
        this.firstPointSelected = false;
        this.secondPointSelected = false;
        try {
            init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * 
	 * @throws Exception
	 */
    private void init() throws Exception {
        this.addMouseListener(imagePanelMouseAdapter = new ImagePanel_mouseAdapter(this));
    }

    @Override
    public void run() {
        try {
            while (isRunning() && !isPaused()) {
                loadLeftImage();
                try {
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * 
	 * @param s
	 */
    public void setImage(String s) {
        ImageIcon imc = new ImageIcon(s);
        imageToInts(imc);
        System.out.println("image " + s + " loaded");
        repaint();
    }

    /**
	 * 
	 * @param imc
	 */
    public void imageToInts(ImageIcon imc) {
        if (imc == null) {
            return;
        }
        imc.getImageLoadStatus();
        Image im = imc.getImage();
        mni = imc.getIconWidth();
        mnj = imc.getIconHeight();
        originpixels = new int[mni * mnj];
        pixels = new int[mni * mnj];
        PixelGrabber pr = new PixelGrabber(im, 0, 0, mni, mnj, originpixels, 0, mni);
        try {
            pr.grabPixels();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setPixelAlpha(100);
        inputImage = new BufferedImage(mni, mnj, BufferedImage.TYPE_3BYTE_BGR);
        for (int i = 0; i < mni; ++i) {
            for (int j = 0; j < mnj; ++j) {
                inputImage.setRGB(i, j, originpixels[i + j * mni]);
            }
        }
    }

    /**
	 * Create a screen shot of the Google Earth rendering window, and load it to the panel.
	 */
    public void loadLeftImage() {
        try {
            Robot robot = new Robot();
            snapshot = robot.createScreenCapture(mainWindow.getGEPanelSize());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        repaint();
    }

    /**
	 * 
	 */
    public void start() {
        imageThread = new Thread(this);
        setRunning(true);
        imageThread.start();
    }

    public void paint(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        super.paint(g);
        if (snapshot != null) {
            g.drawImage(snapshot, 0, 0, getWidth(), getHeight(), this);
        }
        if (pixels != null) {
            g.drawImage(getImageFromInts(pixels), 0, 0, getWidth(), getHeight(), this);
        }
        this.paintSelection(this.getWidth(), this.getHeight());
        if (selection != null) {
            MemoryImageSource mr = new MemoryImageSource(this.getWidth(), this.getHeight(), selection, 0, this.getWidth());
            g.drawImage(Toolkit.getDefaultToolkit().createImage(mr), 0, 0, getWidth(), getHeight(), this);
        }
    }

    public Image getImageFromInts(int pixels[]) {
        MemoryImageSource mr = new MemoryImageSource(mni, mnj, pixels, 0, mni);
        Image limage = Toolkit.getDefaultToolkit().createImage(mr);
        return limage;
    }

    public void attachWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    public void setPixelAlpha(int alpha) {
        if (pixels == null) {
            return;
        }
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = originpixels[i] & ((alpha << 24) + 0x00FFFFFF);
        }
        repaint();
    }

    public ImagePanel_mouseAdapter getImagePanelMouseAdapter() {
        return imagePanelMouseAdapter;
    }

    /**
	 * 
	 */
    public void computeReprojectionMatrix() {
        if (inputImage != null) {
            mainWindow.computeReprojectionMatrix(inputImage, subImageRectangle);
        }
    }

    public BufferedImage getImage() {
        return snapshot;
    }

    public Rectangle getSubImageRectangle() {
        return subImageRectangle;
    }

    public boolean isFirstPointSelected() {
        return firstPointSelected;
    }

    public boolean isSecondPointSelected() {
        return secondPointSelected;
    }

    /**
	 * 
	 * @param x
	 * @param y
	 */
    public void mouseClick(int x, int y) {
        int xOnImage, yOnImage;
        xOnImage = mni * x / this.getWidth();
        yOnImage = mnj * y / this.getHeight();
        if (!((this.firstPointSelected == true) && (this.secondPointSelected == false))) {
            this.firstSelectedPoint.setPoint(xOnImage, yOnImage);
            this.firstPointSelected = true;
            this.secondPointSelected = false;
            System.out.println("First point selected : (" + x + ',' + y + ')');
            firstSelectedPoint.display("First point :");
        } else {
            if (xOnImage != firstSelectedPoint.getX() && yOnImage != firstSelectedPoint.getY()) {
                this.secondSelectedPoint.setPoint(xOnImage, yOnImage);
                this.secondPointSelected = true;
                this.subImageRectangle.setRect(this.firstSelectedPoint, this.secondSelectedPoint);
                System.out.println("Second point selected : (" + x + ',' + y + ')');
                secondSelectedPoint.display("Second point :");
                subImageRectangle.display("Subimage :");
            }
        }
        this.repaint();
    }

    /**
	 * 
	 */
    public void unselectPointOrRectangle() {
        this.firstPointSelected = false;
        this.secondPointSelected = false;
        this.repaint();
    }

    /**
	 * 
	 * @param width
	 * @param height
	 */
    private void paintSelection(int width, int height) {
        if (selection == null || width * height > selectionSize) {
            selectionSize = width * height;
            selection = new int[selectionSize];
        }
        int max = width * height;
        for (int i = 0; i < max; ++i) {
            selection[i] = 0;
        }
        if (this.secondPointSelected == true) {
            int xl, xr, yt, yb;
            xl = this.subImageRectangle.getX() * width / mni;
            yt = this.subImageRectangle.getY() * height / mnj;
            xr = xl + this.subImageRectangle.getWidth() * width / mni;
            yb = yt + this.subImageRectangle.getHeight() * height / mnj;
            for (int i = xl; i < xr; ++i) {
                selection[yt * width + i] = selectionColor;
                selection[yb * width + i] = selectionColor;
            }
            for (int j = yt; j < yb; ++j) {
                selection[j * width + xl] = selectionColor;
                selection[j * width + xr] = selectionColor;
            }
        } else if (this.firstPointSelected == true) {
            int x = this.firstSelectedPoint.getX() * width / mni;
            int y = this.firstSelectedPoint.getY() * height / mnj;
            for (int i = 0; i < width; ++i) {
                selection[y * width + i] = selectionColor;
            }
            for (int j = 0; j < height; ++j) {
                selection[j * width + x] = selectionColor;
            }
        }
    }
}
