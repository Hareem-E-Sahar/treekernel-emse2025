package com.secheresse.superImageResizer.conversion;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ConversionParameters {

    public static final int PARAMETERS_OK = 0;

    public static final int PARAMETERS_WARNING = 1;

    public static final int PARAMETERS_ERROR = 2;

    public static final int TOP_LEFT = 0;

    public static final int TOP = 1;

    public static final int TOP_RIGHT = 2;

    public static final int LEFT = 3;

    public static final int CENTER = 4;

    public static final int RIGHT = 5;

    public static final int BOTTOM_LEFT = 6;

    public static final int BOTTOM = 7;

    public static final int BOTTOM_RIGHT = 8;

    private String errors;

    private String warnings;

    private String thumbnailsDirectory;

    private File tempDir;

    public File getTempDir() {
        return tempDir;
    }

    public void setTempDir(File tempDir) {
        if (!tempDir.getAbsolutePath().endsWith(File.separator)) this.tempDir = new File(tempDir.getAbsolutePath() + File.separator); else this.tempDir = tempDir;
    }

    private File srcDir;

    private File destDir;

    private File zipFile;

    private boolean copyOtherFiles;

    private boolean processSubdirectories;

    private boolean thumbnail;

    private boolean zip;

    private int quality;

    private boolean resize;

    private Dimension dimension;

    private boolean keepAspectRatio;

    private boolean crop;

    private Color fillColor;

    private boolean border;

    private int borderWidth;

    private Color borderColor;

    private boolean rotate;

    private boolean watermark;

    private File watermarkFile;

    private int watermarkPosition;

    private Image watermarkImage;

    private Dimension watermarkMargins;

    private int thumbnailQuality;

    private Dimension thumbnailDimension;

    private boolean thumbnailKeepAspectRatio;

    private boolean thumbnailCrop;

    private Color thumbnailFillColor;

    private boolean thumbnailBorder;

    private int thumbnailBorderWidth;

    private Color thumbnailBorderColor;

    private boolean thumbnailRotate;

    private boolean thumbnailWatermark;

    private File thumbnailWatermarkFile;

    private int thumbnailWatermarkPosition;

    private Image thumbnailWatermarkImage;

    private Dimension thumbnailWatermarkMargins;

    public ConversionParameters() {
        super();
        loadConfig();
    }

    public int check() {
        errors = "";
        warnings = "";
        if (!srcDir.isDirectory()) {
            errors += "- Source folder does not exist:\n\t" + srcDir + "\n";
        } else if (!srcDir.canRead()) {
            errors += "- You don't have access to the source folder:\n\t" + srcDir + "\n";
        }
        if (!isZip()) {
            if (!destDir.isDirectory()) {
                if (!destDir.getParentFile().isDirectory()) {
                    errors += "- Destination folder is not valid:\n\t" + destDir + "\n";
                } else if (destDir.getParentFile().canWrite()) {
                    warnings = "- Destination folder does not exist!\nWould you like to create it?";
                } else {
                    errors += "- You don't have access to the destination folder:\n\t" + destDir + "\n";
                }
            }
        }
        if (srcDir.equals(destDir)) {
            warnings += "- You are about to perfom in place conversion! Changes cannot be undone!!\n";
        } else if (processSubdirectories) {
            if (destDir.getAbsolutePath().startsWith(srcDir.getAbsolutePath()) && destDir.getAbsolutePath().charAt(srcDir.getAbsolutePath().length()) == File.separatorChar) {
                errors += "- If processing subfolders destination cannot be a source folder's child!\n";
            }
        }
        checkSize();
        checkBorder();
        checkWatermark();
        if (thumbnail) {
            checkThumbnailSize();
            checkThumbnailBorder();
            checkThumbnailWatermark();
        }
        if (!errors.equals("")) {
            return PARAMETERS_ERROR;
        } else if (!warnings.equals("")) {
            return PARAMETERS_WARNING;
        } else {
            return PARAMETERS_OK;
        }
    }

    private void checkSize() {
        if (resize && (dimension.width <= 0 || dimension.height <= 0)) {
            errors += "- Image size is not valid!\n";
        }
    }

    private void checkBorder() {
        if (border && borderWidth <= 0) {
            errors += "- Image border width is not valid!\n";
        }
    }

    private void checkWatermark() {
        if (watermark) {
            if (watermarkFile == null) {
                errors += "- Watermark image is not set!\n";
            } else if (watermarkMargins.width < 0 || watermarkMargins.height < 0) {
                errors += "- Watermark margins are not valid!\n";
            }
            if (!checkWatermarkImage()) {
                errors += "- Cannot open watermark image!\n";
                ;
            }
        }
    }

    public boolean checkWatermarkImage() {
        if (watermarkFile != null) {
            Image p = Toolkit.getDefaultToolkit().createImage(watermarkFile.getAbsolutePath());
            Container c = new Container();
            MediaTracker m = new MediaTracker(c);
            m.addImage(p, 0);
            try {
                m.waitForID(0);
            } catch (InterruptedException ex) {
                return false;
            }
            if (p.getWidth(null) > 0 && p.getHeight(null) > 0) {
                watermarkImage = p;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void checkThumbnailSize() {
        if (thumbnailDimension.width <= 0 || thumbnailDimension.height <= 0) {
            errors += "- Thumbnails size is not valid!\n";
        }
    }

    private void checkThumbnailBorder() {
        if (thumbnailBorder && thumbnailBorderWidth <= 0) {
            errors += "- Thumbnail border width is not valid!\n";
        }
    }

    private void checkThumbnailWatermark() {
        if (thumbnailWatermark) {
            if (thumbnailWatermarkFile == null) {
                errors += "- Thumbnail watermark image is not set!\n";
            }
            if (watermarkMargins.width < 0 || watermarkMargins.height < 0) {
                errors += "- Thumbnail watermark margins are not valid!\n";
            }
            if (!checkThumbnailWatermarkImage()) {
                errors += "- Cannot open thumbnail wtermark image!\n";
                ;
            }
        }
    }

    private boolean checkThumbnailWatermarkImage() {
        if (thumbnailWatermarkFile != null) {
            Image p = Toolkit.getDefaultToolkit().createImage(thumbnailWatermarkFile.getAbsolutePath());
            Container c = new Container();
            MediaTracker m = new MediaTracker(c);
            m.addImage(p, 0);
            try {
                m.waitForID(0);
            } catch (InterruptedException ex) {
                return false;
            }
            if (p.getWidth(null) > 0 && p.getHeight(null) > 0) {
                thumbnailWatermarkImage = p;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getErrors() {
        return errors;
    }

    public String getWarnings() {
        return warnings;
    }

    public String colorToString(Color color) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    public Color stringToColor(String color) {
        String[] s = color.split(",");
        int r = Integer.parseInt(s[0]);
        int g = Integer.parseInt(s[1]);
        int b = Integer.parseInt(s[2]);
        return new Color(r, g, b);
    }

    public String dimensionToString(Dimension dimension) {
        return (int) dimension.getWidth() + "x" + (int) dimension.getHeight();
    }

    public Dimension stringToDimension(String dimension) {
        String[] s = dimension.split("x");
        int w = Integer.parseInt(s[0]);
        int h = Integer.parseInt(s[1]);
        return new Dimension(w, h);
    }

    public String booleanToString(boolean b) {
        if (b) {
            return "yes";
        } else {
            return "no";
        }
    }

    public boolean stringToBoolean(String b) {
        return b.equals("yes");
    }

    public void loadConfig() {
        Properties p = new Properties();
        File configFile = new File(System.getProperty("user.home") + File.separator + ".superImageResizer.properties");
        if (configFile.isFile() && configFile.canRead()) {
            try {
                p.load(new FileInputStream(configFile));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                p.load(this.getClass().getClassLoader().getResourceAsStream("default.properties"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        srcDir = new File(System.getProperty("user.home"));
        destDir = new File(System.getProperty("user.home"));
        zipFile = new File(System.getProperty("user.home") + File.separator + "images.zip");
        dimension = stringToDimension(p.getProperty("dimension"));
        thumbnailDimension = stringToDimension(p.getProperty("thumbnailDimension"));
        quality = Integer.parseInt(p.getProperty("quality"));
        thumbnailQuality = Integer.parseInt(p.getProperty("thumbnailQuality"));
        rotate = stringToBoolean(p.getProperty("rotate"));
        resize = stringToBoolean(p.getProperty("resize"));
        thumbnail = stringToBoolean(p.getProperty("thumbnail"));
        zip = stringToBoolean(p.getProperty("zip"));
        watermark = stringToBoolean(p.getProperty("watermark"));
        watermarkPosition = Integer.parseInt(p.getProperty("watermarkPosition"));
        watermarkMargins = stringToDimension(p.getProperty("watermarkMargins"));
        keepAspectRatio = stringToBoolean(p.getProperty("keepAspectRatio"));
        thumbnailCrop = stringToBoolean(p.getProperty("thumbnailCrop"));
        thumbnailKeepAspectRatio = stringToBoolean(p.getProperty("thumbnailKeepAspectRatio"));
        crop = stringToBoolean(p.getProperty("crop"));
        fillColor = stringToColor(p.getProperty("fillColor"));
        border = stringToBoolean(p.getProperty("border"));
        borderColor = stringToColor(p.getProperty("borderColor"));
        thumbnailBorder = stringToBoolean(p.getProperty("thumbnailBorder"));
        thumbnailBorderColor = stringToColor(p.getProperty("thumbnailBorderColor"));
        thumbnailBorderWidth = Integer.parseInt(p.getProperty("thumbnailBorderWidth"));
        borderWidth = Integer.parseInt(p.getProperty("borderWidth"));
        thumbnailFillColor = stringToColor(p.getProperty("thumbnailFillColor"));
        copyOtherFiles = stringToBoolean(p.getProperty("copyOtherFiles"));
        processSubdirectories = stringToBoolean(p.getProperty("processSubdirectories"));
        thumbnailsDirectory = "thumbnails";
        thumbnailWatermarkPosition = Integer.parseInt(p.getProperty("thumbnailWatermarkPosition"));
        thumbnailWatermarkMargins = stringToDimension(p.getProperty("thumbnailWatermarkMargins"));
    }

    public void save() {
        File configFile = new File(System.getProperty("user.home") + File.separator + ".superImageResizer.properties");
        if (configFile.canWrite() || (!configFile.exists() && configFile.getParentFile().canWrite())) {
            Properties p = new Properties();
            p.setProperty("dimension", dimensionToString(dimension));
            p.setProperty("thumbnailDimension", dimensionToString(thumbnailDimension));
            p.setProperty("quality", Integer.toString(quality));
            p.setProperty("thumbnailQuality", Integer.toString(thumbnailQuality));
            p.setProperty("rotate", booleanToString(rotate));
            p.setProperty("resize", booleanToString(resize));
            p.setProperty("thumbnail", booleanToString(thumbnail));
            p.setProperty("zip", booleanToString(zip));
            p.setProperty("watermark", booleanToString(watermark));
            p.setProperty("watermarkPosition", Integer.toString(watermarkPosition));
            p.setProperty("watermarkMargins", dimensionToString(watermarkMargins));
            p.setProperty("keepAspectRatio", booleanToString(keepAspectRatio));
            p.setProperty("thumbnailCrop", booleanToString(thumbnailCrop));
            p.setProperty("thumbnailKeepAspectRatio", booleanToString(thumbnailKeepAspectRatio));
            p.setProperty("crop", booleanToString(crop));
            p.setProperty("fillColor", colorToString(fillColor));
            p.setProperty("border", booleanToString(border));
            p.setProperty("borderColor", colorToString(borderColor));
            p.setProperty("thumbnailBorder", booleanToString(thumbnailBorder));
            p.setProperty("thumbnailBorderColor", colorToString(thumbnailBorderColor));
            p.setProperty("thumbnailBorderWidth", Integer.toString(thumbnailBorderWidth));
            p.setProperty("borderWidth", Integer.toString(borderWidth));
            p.setProperty("thumbnailFillColor", colorToString(thumbnailFillColor));
            p.setProperty("copyOtherFiles", booleanToString(copyOtherFiles));
            p.setProperty("processSubdirectories", booleanToString(processSubdirectories));
            p.setProperty("thumbnailsDirectory", thumbnailsDirectory);
            p.setProperty("thumbnailWatermarkPosition", Integer.toString(thumbnailWatermarkPosition));
            p.setProperty("thumbnailWatermarkMargins", dimensionToString(thumbnailWatermarkMargins));
            try {
                p.store(new FileOutputStream(configFile), "Super Image Resizer configuration");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
        }
    }

    private static ZipOutputStream zos;

    public void createZip() {
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void closeZip() {
        try {
            zos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void zip(File file, File dest) {
        try {
            String entryPath = dest.getAbsolutePath().substring(tempDir.getAbsolutePath().length() + 1).replace(File.separatorChar, '/');
            int bytesRead;
            byte[] buffer = new byte[1024];
            CRC32 crc = new CRC32();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            crc.reset();
            while ((bytesRead = bis.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
            bis.close();
            bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(entryPath);
            entry.setMethod(ZipEntry.STORED);
            entry.setCompressedSize(file.length());
            entry.setSize(file.length());
            entry.setCrc(crc.getValue());
            zos.putNextEntry(entry);
            while ((bytesRead = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
            bis.close();
        } catch (ZipException ex) {
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void zip(File file) {
        zip(file, file);
    }

    public String getThumbnailsDirectory() {
        return thumbnailsDirectory;
    }

    public void setThumbnailsDirectory(String thumbnailsDirectory) {
        this.thumbnailsDirectory = thumbnailsDirectory;
    }

    public File getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public File getDestDir() {
        return destDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public File getZipFile() {
        return zipFile;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public boolean isCopyOtherFiles() {
        return copyOtherFiles;
    }

    public void setCopyOtherFiles(boolean copyOtherFiles) {
        this.copyOtherFiles = copyOtherFiles;
    }

    public boolean isProcessSubdirectories() {
        return processSubdirectories;
    }

    public void setProcessSubdirectories(boolean processSubdirectories) {
        this.processSubdirectories = processSubdirectories;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isZip() {
        return zip;
    }

    public void setZip(boolean zip) {
        this.zip = zip;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean isResize() {
        return resize;
    }

    public void setResize(boolean resize) {
        this.resize = resize;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public boolean isKeepAspectRatio() {
        return keepAspectRatio;
    }

    public void setKeepAspectRatio(boolean keepAspectRatio) {
        this.keepAspectRatio = keepAspectRatio;
    }

    public boolean isCrop() {
        return crop;
    }

    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public boolean isBorder() {
        return border;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    public int getBorderThickness() {
        return borderWidth;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderWidth = borderThickness;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public boolean isWatermark() {
        return watermark;
    }

    public void setWatermark(boolean watermark) {
        this.watermark = watermark;
    }

    public File getWatermarkFile() {
        return watermarkFile;
    }

    public void setWatermarkFile(File watermarkFile) {
        this.watermarkFile = watermarkFile;
    }

    public Image getWatermarkImage() {
        return watermarkImage;
    }

    public int getWatermarkPosition() {
        return watermarkPosition;
    }

    public void setWatermarkPosition(int watermarkPosition) {
        this.watermarkPosition = watermarkPosition;
    }

    public Dimension getWatermarkMargins() {
        return watermarkMargins;
    }

    public void setWatermarkMargins(Dimension watermarkMargins) {
        this.watermarkMargins = watermarkMargins;
    }

    public int getThumbnailQuality() {
        return thumbnailQuality;
    }

    public void setThumbnailQuality(int thumbnailQuality) {
        this.thumbnailQuality = thumbnailQuality;
    }

    public Dimension getThumbnailDimension() {
        return thumbnailDimension;
    }

    public void setThumbnailDimension(Dimension thumbnailDimension) {
        this.thumbnailDimension = thumbnailDimension;
    }

    public boolean isThumbnailKeepAspectRatio() {
        return thumbnailKeepAspectRatio;
    }

    public void setThumbnailKeepAspectRatio(boolean thumbnailKeepAspectRatio) {
        this.thumbnailKeepAspectRatio = thumbnailKeepAspectRatio;
    }

    public boolean isThumbnailCrop() {
        return thumbnailCrop;
    }

    public void setThumbnailCrop(boolean thumbnailCrop) {
        this.thumbnailCrop = thumbnailCrop;
    }

    public Color getThumbnailFillColor() {
        return thumbnailFillColor;
    }

    public void setThumbnailFillColor(Color thumbnailFillColor) {
        this.thumbnailFillColor = thumbnailFillColor;
    }

    public boolean isThumbnailBorder() {
        return thumbnailBorder;
    }

    public void setThumbnailBorder(boolean thumbnailBorder) {
        this.thumbnailBorder = thumbnailBorder;
    }

    public int getThumbnailBorderThickness() {
        return thumbnailBorderWidth;
    }

    public void setThumbnailBorderThickness(int thumbnailBorderThickness) {
        this.thumbnailBorderWidth = thumbnailBorderThickness;
    }

    public Color getThumbnailBorderColor() {
        return thumbnailBorderColor;
    }

    public void setThumbnailBorderColor(Color thumbnailBorderColor) {
        this.thumbnailBorderColor = thumbnailBorderColor;
    }

    public boolean isThumbnailRotate() {
        return thumbnailRotate;
    }

    public void setThumbnailRotate(boolean thumbnailRotate) {
        this.thumbnailRotate = thumbnailRotate;
    }

    public boolean isThumbnailWatermark() {
        return thumbnailWatermark;
    }

    public void setThumbnailWatermark(boolean thumbnailWatermark) {
        this.thumbnailWatermark = thumbnailWatermark;
    }

    public File getThumbnailWatermarkFile() {
        return thumbnailWatermarkFile;
    }

    public void setThumbnailWatermarkFile(File thumbnailWatermarkFile) {
        if (thumbnailWatermarkFile != null) {
            Image p = Toolkit.getDefaultToolkit().createImage(thumbnailWatermarkFile.getAbsolutePath());
            Container c = new Container();
            MediaTracker m = new MediaTracker(c);
            m.addImage(p, 0);
            try {
                m.waitForID(0);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            if (p.getWidth(null) > 0 && p.getHeight(null) > 0) {
                this.watermarkFile = thumbnailWatermarkFile;
                thumbnailWatermarkImage = p;
            }
        }
    }

    public Image getThumbnailWatermarkImage() {
        return thumbnailWatermarkImage;
    }

    public int getThumbnailWatermarkPosition() {
        return thumbnailWatermarkPosition;
    }

    public void setThumbnailWatermarkPosition(int thumbnailWatermarkPosition) {
        this.thumbnailWatermarkPosition = thumbnailWatermarkPosition;
    }

    public Dimension getThumbnailWatermarkMargins() {
        return thumbnailWatermarkMargins;
    }

    public void setThumbnailWatermarkMargins(Dimension thumbnailWatermarkMargins) {
        this.thumbnailWatermarkMargins = thumbnailWatermarkMargins;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }
}
