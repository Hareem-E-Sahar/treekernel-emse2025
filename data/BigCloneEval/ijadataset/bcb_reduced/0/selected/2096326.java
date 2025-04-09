package org.designerator.media.image.util;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import org.designerator.common.io.VideoIO;
import org.designerator.common.string.StringUtil;
import org.designerator.image.algo.util.ImageConversion;
import org.designerator.image.jpeg.ImageLoader;
import org.designerator.media.image.ImagePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.osgi.framework.Bundle;
import psd.base.PsdImage;
import psd.layer.PsdLayer;
import psd.layer.PsdSWTLayer;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGDecodeParam;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class IO {

    private static final int ERRORSIZEX = 160;

    private static final int ERRORSIZEY = 120;

    public static final String[] IMAGE_FORMATS = { "jpg", "jpeg", "tiff", "gif", "psd", "png", "ico", "bmp" };

    static ImageLoader loader;

    public static final String VIDEOLINK_EXT = ".videolink.xml";

    public static Image createErrorImage(final Display display, Image icon, String name, int width, int height) {
        if (icon == null) {
            icon = ImageHelper.createImage(null, "/icons/large/image-x-generic.gif");
        }
        Image errorImage = new Image(display, width, height);
        GC gc = new GC(errorImage);
        final Color b = new Color(display, 10, 10, 10);
        final Color w = new Color(display, 235, 235, 235);
        gc.setBackground(w);
        gc.setForeground(b);
        gc.fillRectangle(0, 0, width, height);
        Rectangle bounds = icon.getBounds();
        gc.drawImage(icon, (width - bounds.width) / 2, (height - bounds.height) / 2);
        if (!StringUtil.isEmpty(name)) {
            gc.setAntialias(SWT.ON);
            Point textExtent = gc.textExtent(name);
            int p = width - 2 - textExtent.x;
            if (p < 0) {
                name = StringUtil.getSubStringText(gc, name, gc.getFont(), width - 2);
                textExtent = gc.textExtent(name);
            }
            gc.setAlpha(200);
            gc.fillRectangle(0, height - textExtent.y - 6, width, textExtent.y + 6);
            gc.setAlpha(255);
            gc.drawText(name, (width - textExtent.x) / 2, height - textExtent.y - 4, true);
        }
        gc.dispose();
        b.dispose();
        w.dispose();
        return errorImage;
    }

    public static Image createImageFor(IPath gifFile) {
        Bundle bundle = ImagePlugin.getDefault().getBundle();
        URL url = FileLocator.find(bundle, gifFile, null);
        ImageDescriptor id = ImageDescriptor.createFromURL(url);
        return id.createImage();
    }

    public static Comparator<File> getFileAndDirComparator() {
        return new Comparator<File>() {

            @Override
            public int compare(File file1, File file2) {
                if (file1.isFile() && file2.isDirectory()) {
                    return 1;
                }
                if (file1.isDirectory() && file2.isFile()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private static String getFileName(String file) {
        if (file != null) {
            IPath p = new Path(file);
            String[] segments = p.segments();
            if (segments.length > 0) {
                return segments[segments.length - 1];
            }
        }
        return null;
    }

    public static String getImageFilePathOpen(Shell shell, String message, String currentDir) {
        FileDialog fileChooser = new FileDialog(shell, SWT.OPEN);
        fileChooser.setText(message);
        if (currentDir != null) {
            fileChooser.setFilterPath(currentDir);
        }
        fileChooser.setFilterExtensions(new String[] { "*.gif; *.jpg; *.png; *.ico; *.bmp; *.tif" });
        fileChooser.setFilterNames(new String[] { "SWT image" + " (gif, jpeg, png, ico, bmp,tif)" });
        String filename = fileChooser.open();
        if (filename != null) {
            return filename;
        }
        return null;
    }

    @SuppressWarnings("static-access")
    public static WindowsBitmap getImageinfo(Image image, boolean print) {
        if (SWT.getPlatform().equals("win32")) {
            org.eclipse.swt.internal.win32.BITMAP bitmap = new org.eclipse.swt.internal.win32.BITMAP();
            org.eclipse.swt.internal.win32.OS.GetObject(image.handle, bitmap.sizeof, bitmap);
            WindowsBitmap windows = new WindowsBitmap();
            windows.bmBitsPixel = bitmap.bmBitsPixel;
            windows.bmWidth = bitmap.bmWidth;
            windows.bmWidthBytes = bitmap.bmWidthBytes;
            windows.bmPlanes = bitmap.bmPlanes;
            windows.bmHeight = bitmap.bmHeight;
            if (print) {
                System.out.println("bmWidth:" + bitmap.bmWidth);
                System.out.println("bmWidthBytes:" + bitmap.bmWidthBytes);
                System.out.println("bmBitsPixel:" + bitmap.bmBitsPixel);
            }
            return windows;
        }
        return null;
    }

    public static FilenameFilter getImageNameFilter() {
        return new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (isValidImageFile(name)) {
                    return true;
                }
                return false;
            }
        };
    }

    public static Image getMediaIcon(String file) {
        if (file != null) {
            IPath p = new Path(file);
            if (isValidImageExtension(p.getFileExtension())) {
                return ImageHelper.createImage(null, "/icons/large/image-x-generic.gif");
            }
            if (isValidVideoFile(p.getFileExtension())) {
                return ImageHelper.createImage(null, "/icons/large/video-x-generic.gif");
            }
        }
        return null;
    }

    public static MediaFilterImpl getMediaNameFilter() {
        return new MediaFilterImpl();
    }

    public static String getSystemImageFilePathSave(Shell shell, String message, String currentDir) {
        FileDialog fileChooser = new FileDialog(shell, SWT.SAVE);
        fileChooser.setText(message);
        String filterPath = currentDir;
        fileChooser.setFilterPath(filterPath);
        String filename = fileChooser.open();
        if (filename != null) {
            return filename;
        }
        return null;
    }

    private static Image getValidImageFormat(Image sourceImage) {
        if (!isImage32or24bit(sourceImage)) {
            System.out.println("convertToArgbSWTImage");
            Image argbimage = ImageConversion.getPlatformSWTImage(sourceImage);
            if (!isImage32or24bit(argbimage)) {
                Image trueargbimage = ImageConversion.getPlatformSWT32Image(sourceImage);
                argbimage.dispose();
                sourceImage.dispose();
                return trueargbimage;
            }
            sourceImage.dispose();
            return argbimage;
        }
        return sourceImage;
    }

    public static IPath getWorkSpaceSavePath(Shell shell, String title, String message, IFile original) {
        SaveAsDialog dialog = new SaveAsDialog(shell);
        dialog.create();
        dialog.getShell().setText(title);
        dialog.setMessage(message);
        dialog.setOriginalFile(original);
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    public static byte[] gzipInput(String file) {
        try {
            FileInputStream fin = new FileInputStream(file);
            GZIPInputStream in = new GZIPInputStream(fin);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) break;
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        } catch (IOException e) {
            System.err.println(e);
        }
        return null;
    }

    public static void gzipOut(String[] files) {
        for (int i = 0; i < files.length; i++) {
            try {
                FileInputStream in = new FileInputStream(files[i]);
                FileOutputStream fout = new FileOutputStream(files[i]);
                GZIPOutputStream out = new GZIPOutputStream(fout);
                byte[] buffer = new byte[1024];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1) break;
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private static boolean isImage32or24bit(Image sourceImage) {
        boolean is32 = true;
        WindowsBitmap imageinfo = getImageinfo(sourceImage, false);
        if (imageinfo != null) {
            if (!(imageinfo.bmBitsPixel == 32 || imageinfo.bmBitsPixel == 24)) {
                is32 = false;
            }
        } else {
            ImageData imagedata = sourceImage.getImageData();
            if (!(imagedata.depth == 32 || imagedata.depth == 24)) {
                is32 = false;
            }
            imagedata = null;
        }
        return is32;
    }

    public static boolean isJpeg(IFile resource) {
        String fileExtension = resource.getFileExtension();
        if (fileExtension == null) {
            return false;
        }
        return fileExtension.toLowerCase().equals("jpg") || fileExtension.toLowerCase().equals("jpeg");
    }

    public static boolean isJpeg(String name) {
        if (name == null) {
            return false;
        }
        return name.toLowerCase().endsWith("jpg") || name.toLowerCase().endsWith("jpeg");
    }

    public static boolean isPsdImage(String image) {
        if (image == null) {
            return false;
        }
        image = image.toLowerCase();
        return image.endsWith(".psd");
    }

    public static boolean isValidFilePath(String thumbPath) {
        if (!StringUtil.isEmpty(thumbPath)) {
            File f = new File(thumbPath);
            return f.exists() && f.isFile();
        }
        return false;
    }

    public static boolean isValidImageExtension(String ext) {
        if (ext == null) {
            return false;
        }
        ext = ext.toLowerCase();
        for (int i = 0; i < IMAGE_FORMATS.length; i++) {
            if (ext.equals(IMAGE_FORMATS[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidImageFile(IFile newSelection) {
        if (newSelection == null) {
            return false;
        }
        String ext = newSelection.getFileExtension();
        if (ext == null) {
            return false;
        }
        String fileExtension = ext.toLowerCase();
        for (int i = 0; i < IMAGE_FORMATS.length; i++) {
            if (fileExtension.equals(IMAGE_FORMATS[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidImageFile(String image) {
        if (image == null) {
            return false;
        }
        image = image.toLowerCase();
        for (int i = 0; i < IMAGE_FORMATS.length; i++) {
            if (image.endsWith(IMAGE_FORMATS[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidMediaFile(File file) {
        return isValidVideoFile(file.getName()) || isValidImageFile(file.getName());
    }

    public static boolean isValidMediaFile(IFile file) {
        return isValidImageFile(file) || VideoIO.isFFmpegVideo(file.getFileExtension());
    }

    public static boolean isValidMediaFile(String file) {
        return isValidVideoFile(file) || isValidImageFile(file);
    }

    public static boolean isValidVideoFile(IFile file) {
        if (file == null) {
            return false;
        }
        String ext = file.getFileExtension();
        if (ext == null) {
            return false;
        }
        return VideoIO.isFFmpegVideo(ext);
    }

    public static boolean isValidVideoFile(String file) {
        return VideoIO.isFFmpegVideoName(file);
    }

    public static boolean isValidVideoOrLinkFile(String name) {
        return (IO.isValidVideoFile(name) || IO.isValidVideoLinkFile(name));
    }

    public static boolean isValidVideoLinkFile(String file) {
        if (file == null) {
            return false;
        }
        file = file.toLowerCase();
        return file.endsWith(VIDEOLINK_EXT);
    }

    public static BufferedImage loadBufImage(String string) {
        BufferedImage image = null;
        try {
            File file = new File(string);
            image = ImageIO.read(file);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static Image loadImage(InputStream file, Display display, boolean checkArgb) {
        Image sourceImage = null;
        try {
            sourceImage = new Image(display, file);
            if (sourceImage == null) {
                System.err.println("cannot load image");
                return null;
            }
            if (checkArgb) {
                sourceImage = getValidImageFormat(sourceImage);
            }
        } catch (org.eclipse.swt.SWTException e) {
            e.printStackTrace();
        } catch (java.lang.IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (sourceImage == null) {
            sourceImage = IO.createErrorImage(display, null, null, ERRORSIZEX, ERRORSIZEY);
        }
        return sourceImage;
    }

    public static Image loadImage(String file, final Display display, boolean checkArgb, boolean createErrorImage) {
        Image sourceImage = null;
        if (isPsdImage(file)) {
            FileInputStream stream;
            try {
                stream = new FileInputStream(file);
                PsdImage psdFile = new PsdImage(stream, true);
                stream.close();
                PsdLayer baseLayer = psdFile.getBaseLayer();
                if (baseLayer instanceof PsdSWTLayer) {
                    ImageData imageData = ((PsdSWTLayer) baseLayer).getImageData();
                    if (imageData != null) {
                        sourceImage = new Image(display, imageData);
                        return sourceImage;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.err.println("cannot load psd image");
            sourceImage = createErrorImage(display, getMediaIcon(file), getFileName(file), ERRORSIZEX, ERRORSIZEY);
            return sourceImage;
        }
        try {
            sourceImage = new Image(display, file);
            if (sourceImage == null && createErrorImage) {
                System.err.println("cannot load image");
                sourceImage = createErrorImage(display, getMediaIcon(file), getFileName(file), ERRORSIZEX, ERRORSIZEY);
                return sourceImage;
            }
            if (checkArgb) {
                if (sourceImage != null && file.endsWith(".gif")) {
                    sourceImage = ImageConversion.getPlatformSWTImageFromGif(sourceImage);
                } else {
                    sourceImage = getValidImageFormat(sourceImage);
                }
            }
        } catch (org.eclipse.swt.SWTException e) {
            System.err.println(file);
            if (sourceImage == null && createErrorImage) {
                sourceImage = createErrorImage(display, getMediaIcon(file), getFileName(file), ERRORSIZEX, ERRORSIZEY);
            }
            e.printStackTrace();
            return sourceImage;
        } catch (java.lang.IllegalArgumentException e) {
            System.err.println(file);
            e.printStackTrace();
            if (sourceImage == null && createErrorImage) {
                sourceImage = createErrorImage(display, getMediaIcon(file), getFileName(file), ERRORSIZEX, ERRORSIZEY);
            }
            return sourceImage;
        }
        return sourceImage;
    }

    public static BufferedImage loadJpgToBufImage(String filename) throws IOException {
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new FileInputStream(filename));
        BufferedImage inputImage = decoder.decodeAsBufferedImage();
        JPEGDecodeParam jpegDecodeParam = decoder.getJPEGDecodeParam();
        byte[][] markerData = jpegDecodeParam.getMarkerData(JPEGDecodeParam.APP1_MARKER);
        return inputImage;
    }

    public static Raster loadJpgToRaster(String filename) throws IOException {
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new FileInputStream(filename));
        Raster inputImage = decoder.decodeAsRaster();
        JPEGDecodeParam jpegDecodeParam = decoder.getJPEGDecodeParam();
        byte[][] markerData = jpegDecodeParam.getMarkerData(JPEGDecodeParam.APP1_MARKER);
        return inputImage;
    }

    /**
	 * Not tested enough // idea is to read and write jpg metadata
	 * @param display
	 * @param file
	 * @return
	 */
    public static Image loadSunJpgToSWT(Display display, String file) {
        BufferedImage bImage = null;
        Image swtImage = null;
        try {
            bImage = loadJpgToBufImage(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bImage != null) {
            swtImage = ImageConversion.getSWTImage(display, bImage);
        }
        bImage = null;
        return swtImage;
    }

    public static void makeDir(File f) throws IOException {
        if (f.exists()) {
            if (!f.isDirectory()) {
                final String message = "Cannot create Dir! Output isFile! " + f.getAbsolutePath();
                throw new IOException(message);
            }
        } else {
            if (!f.mkdirs()) {
                throw new IOException("Failed to create Dir: " + f.getAbsolutePath());
            }
        }
    }

    /**
	 * 
	 */
    public static void makeFile(File f) throws IOException {
        if (f.exists()) {
            if (f.isDirectory()) {
                final String message = "Cannot create File! Output_is Directory! " + f.getAbsolutePath();
                throw new IOException(message);
            }
        } else {
            if (!f.createNewFile()) {
                throw new IOException("Failed to create File: " + f.getAbsolutePath());
            }
        }
    }

    public static Object readObject(File f) throws IOException {
        Object object;
        FileInputStream bi = new FileInputStream(f);
        ObjectInputStream os = new ObjectInputStream(bi);
        try {
            object = os.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex.getMessage());
        }
        os.close();
        return object;
    }

    public static boolean saveAsJPEG(BufferedImage bufim, String filename, float quality) throws IOException {
        ImageWriter jpgWriter = null;
        Iterator it = ImageIO.getImageWritersByFormatName("jpeg");
        if (it.hasNext()) {
            jpgWriter = (ImageWriter) it.next();
        } else {
            throw new IOException("Can't find an ImageWriter for JPEG files.");
        }
        ImageWriteParam jpgParam = jpgWriter.getDefaultWriteParam();
        jpgParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgParam.setCompressionQuality(quality);
        jpgParam.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
        FileImageOutputStream imout = new FileImageOutputStream(new File(filename));
        jpgWriter.setOutput(imout);
        try {
            jpgWriter.write(null, new IIOImage(bufim, null, null), jpgParam);
            imout.close();
            return true;
        } catch (IOException e) {
            imout.close();
            e.printStackTrace();
        }
        imout.close();
        return false;
    }

    public static boolean saveAsSUNJPEG(byte[] pixel, int width, int height, String filename, float quality) {
        if (pixel.length == width * height * 4) {
            byte[] pixel2 = ImageConversion.byteArrayGBRAtoGBR(pixel);
            BufferedImage bimage = ImageConversion.bgrToBufferedImage(pixel2, width, height, null);
            try {
                IO.saveAsJPEG(bimage, filename, quality);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
	 * Saves the image data in this ImageLoader to a file with the specified
	 * name. The format parameter can have one of the following values:
	 * <dl>
	 * <dt><code>IMAGE_BMP</code></dt>
	 * <dd>Windows BMP file format, no compression</dd>
	 * <dt><code>IMAGE_BMP_RLE</code></dt>
	 * <dd>Windows BMP file format, RLE compression if appropriate</dd>
	 * <dt><code>IMAGE_GIF</code></dt>
	 * <dd>GIF file format</dd>
	 * <dt><code>IMAGE_ICO</code></dt>
	 * <dd>Windows ICO file format</dd>
	 * <dt><code>IMAGE_JPEG</code></dt>
	 * <dd>JPEG file format</dd>
	 * <dt><code>IMAGE_PNG</code></dt>
	 * <dd>PNG file format</dd>
	 * </dl>
	 */
    public static boolean saveImageSWT(ImageData data, String file, int format, int factor) {
        if (loader == null) {
            loader = new ImageLoader();
        }
        try {
            loader.data = new ImageData[] { data };
            loader.jpgFactor = factor;
            loader.save(file, format);
        } catch (org.eclipse.swt.SWTException e) {
            e.printStackTrace();
        } catch (SWTError e) {
            e.printStackTrace();
        } catch (java.lang.IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void saveJpgImage(Raster image, String file, JPEGEncodeParam params) {
        try {
            params.addMarkerData(JPEGDecodeParam.APP1_MARKER, "this is a marker".getBytes());
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(new FileOutputStream(file), params);
            encoder.encode(image);
        } catch (IOException e) {
        }
    }

    public static void saveScreenShot(String savepath, int format, int f) {
        final Display display = Display.getDefault();
        Rectangle rectangle = display.getBounds();
        Image image = new Image(display, rectangle.width, rectangle.height);
        GC gc = new GC(display);
        gc.copyArea(image, 0, 0);
        saveImageSWT(image.getImageData(), savepath, format, f);
        image.dispose();
        gc.dispose();
    }

    public static boolean writeBytes(byte[] data, File f) {
        FileOutputStream fout = null;
        BufferedOutputStream bufferOut = null;
        try {
            fout = new FileOutputStream(f);
            bufferOut = new BufferedOutputStream(fout);
            bufferOut.write(data);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            if (bufferOut != null) {
                try {
                    bufferOut.flush();
                    bufferOut.close();
                } catch (Exception e) {
                }
            }
        }
        return true;
    }

    public static void writeObject(Object object, File f) throws IOException {
        FileOutputStream bo = new FileOutputStream(f);
        ObjectOutputStream os = new ObjectOutputStream(bo);
        os.writeObject(object);
        os.close();
    }

    public IO() {
        super();
    }
}
