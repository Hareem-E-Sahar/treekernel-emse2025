package edu.cmu.cs.euklas.scanners;

import java.io.File;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import edu.cmu.cs.euklas.Activator;
import edu.cmu.cs.euklas.dataobjects.Image;

/**
 * This scanner scans an HTML document for the
 * used local images.
 * 
 * @author Christian Doerner
 */
public class ImageScanner extends AbstractScanner {

    private static final String imagePattern = "<img.+src\\s*=\\s*['|\"].+\\.(png|gif|jpg|tiff|bmp)['|\"]";

    private static final String innerImagePattern = "src\\s*=\\s*['|\"].+\\.(png|gif|jpg|tiff|bmp)";

    private IResource resource;

    /**
	 * Standard constructor
	 * 
	 * @param targetCode The code to scan
	 * @param resource The resource from which the targetCode was taken
	 */
    public ImageScanner(String targetCode, IResource resource) {
        super(targetCode);
        this.resource = resource;
    }

    /**
	 * This method scans for <img> tags in 
	 * the given target code. 
	 * 
	 * @return A list of unavailable image files
	 */
    public LinkedList<Image> scanForMissingImages() {
        LinkedList<Image> undefinedImages = new LinkedList<Image>();
        Pattern p = Pattern.compile(imagePattern);
        Matcher m = p.matcher(targetCode.toLowerCase());
        while (m.find()) {
            Pattern p1 = Pattern.compile(innerImagePattern);
            Matcher m1 = p1.matcher(m.group().toLowerCase());
            if (m1.find()) {
                String[] src = null;
                if (m1.group().contains("\"")) src = m1.group().split("\""); else src = m1.group().split("'");
                if (!isImageAvailable(src[1])) {
                    undefinedImages.add(new Image(src[1], m.start(), m.end()));
                    Activator.getDefault().logDebuggingData("[ImageScanner]: Added '" + src[1] + "' to the list of undefined images.");
                }
            }
        }
        return undefinedImages;
    }

    /**
	 * This method checks, if a passed image file exists or not
	 * 
	 * @param src The image that should be checked
	 * @return If the image file exists
	 */
    private boolean isImageAvailable(String src) {
        IPath path = resource.getLocation().removeLastSegments(1);
        File available = new File(path.toOSString() + File.separator + src);
        return available.exists();
    }
}
