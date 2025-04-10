package ag.ion.noa.graphic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import ag.ion.noa.NOAException;
import com.sun.star.text.HoriOrientation;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.text.VertOrientation;

/**
 * Information to be used for graphic operations.
 * 
 * @author Markus Kr�ger
 * @version $Revision: 10398 $
 */
public class GraphicInfo {

    public static final double PIXEL_RATIO = 26.45;

    private File tmpFile = null;

    private String url = null;

    private int width = 0;

    private int height = 0;

    private short verticalAlignment = VertOrientation.TOP;

    private short horizontalAlignment = HoriOrientation.CENTER;

    private TextContentAnchorType anchor = null;

    /**
	 * Constructs new GraphicInfo. TODO this is just a workaround to support
	 * streams
	 * 
	 * @param imageInputStream
	 *            the input stream of the image (this will create a temporary
	 *            file) Also closes the stream.
	 * @param width
	 *            the width in pixel
	 * @param widthPixel
	 *            if the width is described as pixels, otherwise it is assumed
	 *            that the width is in thenth millimeter
	 * @param height
	 *            the height in pixel
	 * @param heightPixel
	 *            if the height is described as pixels, otherwise it is assumed
	 *            that the height is in thenth millimeter
	 * @param verticalAlignment
	 *            the vertical alignment. Possible values are described in
	 *            {@link VertOrientation}
	 * @param horizontalAlignment
	 *            the horizontal alignment. Possible values are described in
	 *            {@link HoriOrientation}
	 * @param anchor
	 *            the anchor type of the image. Possible values are described in
	 *            {@link TextContentAnchorType}
	 * 
	 * @throws NOAException
	 *             if creation by input stream fails
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public GraphicInfo(InputStream imageInputStream, int width, boolean widthPixel, int height, boolean heightPixel, short verticalAlignment, short horizontalAlignment, TextContentAnchorType anchor) throws NOAException {
        if (imageInputStream == null) throw new IllegalArgumentException();
        try {
            tmpFile = File.createTempFile("NOATempImage", ".noa.tmp");
            tmpFile.deleteOnExit();
            FileOutputStream streamOut = new FileOutputStream(tmpFile);
            int c;
            while ((c = imageInputStream.read()) != -1) streamOut.write(c);
            imageInputStream.close();
            streamOut.close();
        } catch (Throwable throwable) {
            throw new NOAException(throwable);
        }
        String url = tmpFile.getAbsolutePath();
        init(url, width, widthPixel, height, heightPixel, verticalAlignment, horizontalAlignment, anchor);
    }

    /**
	 * Constructs new GraphicInfo.
	 * 
	 * @param url
	 *            the url of the image in the file system
	 * @param width
	 *            the width in pixel
	 * @param widthPixel
	 *            if the width is described as pixels, otherwise it is assumed
	 *            that the width is in thenth millimeter
	 * @param height
	 *            the height in pixel
	 * @param heightPixel
	 *            if the height is described as pixels, otherwise it is assumed
	 *            that the height is in thenth millimeter
	 * @param verticalAlignment
	 *            the vertical alignment. Possible values are described in
	 *            {@link VertOrientation}
	 * @param horizontalAlignment
	 *            the horizontal alignment. Possible values are described in
	 *            {@link HoriOrientation}
	 * @param anchor
	 *            the anchor type of the image. Possible values are described in
	 *            {@link TextContentAnchorType}
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public GraphicInfo(String url, int width, boolean widthPixel, int height, boolean heightPixel, short verticalAlignment, short horizontalAlignment, TextContentAnchorType anchor) {
        init(url, width, widthPixel, height, heightPixel, verticalAlignment, horizontalAlignment, anchor);
    }

    /**
	 * Returns the url of the image.
	 * 
	 * @return the url of the image
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public String getUrl() {
        return url;
    }

    /**
	 * Returns the width of the image in tenth millimeter.
	 * 
	 * @return the width of the image in tenth millimeter
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public int getWidth() {
        return width;
    }

    /**
	 * Returns the height of the image in tenth millimeter.
	 * 
	 * @return the height of the image in tenth millimeter
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public int getHeight() {
        return height;
    }

    /**
	 * Returns the vertical alignment of the image. Possible values are
	 * described in {@link VertOrientation}
	 * 
	 * @return the vertical alignment of the image
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public short getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
	 * Returns the horizontal alignment of the image. Possible values are
	 * described in {@link HoriOrientation}
	 * 
	 * @return the horizontal alignment of the image
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public short getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
	 * Returns the anchor of the image, or null. Possible values are described
	 * in {@link TextContentAnchorType}
	 * 
	 * @return the anchor of the image, or null
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public TextContentAnchorType getAnchor() {
        return anchor;
    }

    /**
	 * Cleans up the graphic info (i.e. deletes temporary file is it was used).
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    public void cleanUp() {
        if (tmpFile != null && tmpFile.exists()) tmpFile.delete();
    }

    /**
	 * Initializes the graphic info
	 * 
	 * @param url
	 *            the url of the image in the file system
	 * @param width
	 *            the width in pixel
	 * @param widthPixel
	 *            if the width is described as pixels, otherwise it is assumed
	 *            that the width is in thenth millimeter
	 * @param height
	 *            the height in pixel
	 * @param heightPixel
	 *            if the height is described as pixels, otherwise it is assumed
	 *            that the height is in thenth millimeter
	 * @param verticalAlignment
	 *            the vertical alignment. Possible values are described in
	 *            {@link VertOrientation}
	 * @param horizontalAlignment
	 *            the horizontal alignment. Possible values are described in
	 *            {@link HoriOrientation}
	 * @param anchor
	 *            the anchor type of the image. Possible values are described in
	 *            {@link TextContentAnchorType}
	 * 
	 * @author Markus Kr�ger
	 * @date 09.07.2007
	 */
    private void init(String url, int width, boolean widthPixel, int height, boolean heightPixel, short verticalAlignment, short horizontalAlignment, TextContentAnchorType anchor) {
        if (url == null || width < 1 || height < 1) throw new IllegalArgumentException();
        this.url = new File(url).toURI().toString();
        if (widthPixel) width = (int) Math.round(Math.floor(width * PIXEL_RATIO));
        this.width = width;
        if (heightPixel) height = (int) Math.round(Math.floor(height * PIXEL_RATIO));
        this.height = height;
        this.verticalAlignment = verticalAlignment;
        this.horizontalAlignment = horizontalAlignment;
        this.anchor = anchor;
    }
}
