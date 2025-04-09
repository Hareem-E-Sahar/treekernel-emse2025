package targetanalyzer.core.analysis.image;

import java.io.FileInputStream;
import java.io.RandomAccessFile;
import targetanalyzer.core.analysis.image.metadata.ImageInfo;

/**
 * Calculates the radius in pixels for hits on a target image by taking the resolution of the scanned image and the caliber of the used ammunition in account.
 * @author  Ruediger Gad
 */
public class PixelRadiusCalculator {

    static final double CM_PER_INCH = 2.54f;

    /**
	 * @uml.property  name="imageInfo"
	 * @uml.associationEnd  
	 */
    private ImageInfo imageInfo = new ImageInfo();

    private double anomalyCorrectionFactor = 0.95;

    private int dpi;

    public PixelRadiusCalculator(FileInputStream in) throws Exception {
        this.imageInfo.setInput(in);
        this.init();
    }

    public PixelRadiusCalculator(RandomAccessFile in) throws Exception {
        this.imageInfo.setInput(in);
        this.init();
    }

    public int getPixelRadiusFromMillimeter(double mmCaliber) {
        return (int) (((mmCaliber * this.anomalyCorrectionFactor) / 10) / CM_PER_INCH / 2 * this.dpi);
    }

    private void init() throws Exception {
        if (!this.imageInfo.check()) {
            throw new Exception("Error getting metadata from file!");
        }
        int heightDPI = this.imageInfo.getPhysicalHeightDpi();
        int widthDPI = this.imageInfo.getPhysicalWidthDpi();
        if (heightDPI <= 0 || widthDPI <= 0) {
            throw new Exception("Error retrieving dpi values from" + " file! heightDPI = " + heightDPI + "   widthDPI = " + widthDPI);
        }
        if (heightDPI == widthDPI) {
            this.dpi = heightDPI;
        } else {
            this.dpi = (heightDPI + widthDPI) / 2;
        }
    }
}
