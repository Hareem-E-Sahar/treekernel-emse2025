package oojmerge;

import java.io.*;
import java.util.zip.*;
import oojmerge.barcoder.*;

/**
 * ImageToMerge
 * Copyright &copy; medge 2010
 * Version 1.0
 * @author medge
 */
public class ImageToMerge {

    private String imageName;

    private File file;

    private String barcodeValue;

    private String barcodeType;

    private int width;

    private int height;

    private boolean includeCaption;

    private boolean checkDigit;

    public ImageToMerge(String imageName, File file) {
        this.imageName = imageName;
        this.file = file;
    }

    public ImageToMerge(String imageName, String barcodeValue, String barcodeType, int width, int height, boolean includeCaption, boolean checkDigit) {
        this.imageName = imageName;
        this.barcodeValue = barcodeValue;
        this.barcodeType = barcodeType;
        this.width = width;
        this.height = height;
        this.includeCaption = includeCaption;
        this.checkDigit = checkDigit;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getBarcodeValue() {
        return barcodeValue;
    }

    public void setBarcodeValue(String barcodeValue) {
        this.barcodeValue = barcodeValue;
    }

    public String getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(String barcodeType) {
        this.barcodeType = barcodeType;
    }

    public boolean isBarcode() {
        return file == null;
    }

    private void addFile(InputStream i, OutputStream o) throws IOException {
        int r = 0;
        byte[] b = new byte[1024];
        do {
            r = i.read(b);
            if (r > 0) o.write(b, 0, r);
        } while (r > 0);
    }

    public void addToODF(ZipOutputStream zo) throws IOException, ZipException, BarcodeException {
        ZipEntry ze = new ZipEntry("Pictures/" + getImageName());
        zo.putNextEntry(ze);
        if (isBarcode()) {
            Barcoder barcode = Barcoder.buildBarcoder(barcodeType);
            barcode.setValue(barcodeValue);
            barcode.setImageWidth(width);
            barcode.setImageHeight(height);
            barcode.setIncludeCaption(includeCaption);
            barcode.setCheckDigit(checkDigit);
            BarcoderImage bcimage = new BarcoderImage(barcode);
            bcimage.sendGifTo(zo);
        } else {
            FileInputStream fis = new FileInputStream(file);
            addFile(fis, zo);
        }
    }
}
