import java.io.*;
import java.awt.image.*;
import i5d.Image5D;
import i5d.cal.ChannelCalibration;
import i5d.cal.ChannelDisplayProperties;
import ij.*;
import ij.io.*;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

public class Save_Image5D implements PlugIn {

    /** Save the Image5D in TIFF format using a save file
    dialog. Returns false if the user selects cancel. */
    public void run(String arg) {
        if (IJ.versionLessThan("1.35c")) return;
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.error("No Image selected.");
            return;
        } else if (!(imp instanceof Image5D)) {
        }
        String name = imp.getTitle();
        SaveDialog sd = new SaveDialog("Save as Image5D", name, ".tif");
        name = sd.getFileName();
        if (name == null) return;
        String directory = sd.getDirectory();
        imp.startTiming();
        String path = directory + name;
        if (path == null) return;
        saveAsImage5D(imp, name, directory);
    }

    /** Save the Image5D as a multi-image TIFF using the specified name and directory. 
     * It should be possible to call this method directly without deviation via the run() 
     * method.*/
    public boolean saveAsImage5D(ImagePlus imp, String name, String directory) {
        if (imp == null) return false;
        String path = directory + name;
        FileInfo fi = imp.getFileInfo();
        Object info = imp.getProperty("Info");
        if (info != null && (info instanceof String)) fi.info = (String) info;
        if (fi.pixels == null && imp.getStack().isVirtual()) {
            IJ.error("Save As Tiff", "Virtual stacks not supported.");
            return false;
        }
        String description = (new FileSaver(imp)).getDescriptionString();
        fi.sliceLabels = imp.getImageStack().getSliceLabels();
        if (imp instanceof Image5D) {
            Image5D i5d = (Image5D) imp;
            LookUpTable lut = new LookUpTable(LookUpTable.createGrayscaleColorModel(false));
            fi.lutSize = lut.getMapSize();
            fi.reds = lut.getReds();
            fi.greens = lut.getGreens();
            fi.blues = lut.getBlues();
            description = description.substring(0, description.length() - 1);
            description = description + "Image5D=" + Image5D.VERSION + "\n";
            description = description + "\0";
            fi.description = description;
            int nChannelEntries = 7;
            int metadataSize = nChannelEntries * (i5d.getNChannels());
            int[] metaDataTypes = new int[metadataSize];
            byte[][] metaData = new byte[metadataSize][];
            i5d.storeCurrentChannelProperties();
            int metadataCounter = 0;
            try {
                for (int c = 1; c <= i5d.getNChannels(); ++c) {
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    DataOutputStream ds = new DataOutputStream(bs);
                    ChannelCalibration chCalibration = i5d.getChannelCalibration(c);
                    ChannelDisplayProperties chDispProps = i5d.getChannelDisplayProperties(c);
                    metaDataTypes[metadataCounter] = Open_Image5D.tagLUT;
                    ds.writeInt(1);
                    ds.writeInt(c);
                    ColorModel cm = chDispProps.getColorModel();
                    for (int v = 0; v <= 255; ++v) {
                        ds.writeByte(cm.getRed(v));
                    }
                    for (int v = 0; v <= 255; ++v) {
                        ds.writeByte(cm.getGreen(v));
                    }
                    for (int v = 0; v <= 255; ++v) {
                        ds.writeByte(cm.getBlue(v));
                    }
                    ds.flush();
                    metaData[metadataCounter] = bs.toByteArray();
                    metadataCounter++;
                    bs.reset();
                    metaDataTypes[metadataCounter] = Open_Image5D.tagCB;
                    ds.writeInt(1);
                    ds.writeInt(c);
                    ds.writeDouble(chDispProps.getMinValue());
                    ds.writeDouble(chDispProps.getMaxValue());
                    ds.flush();
                    metaData[metadataCounter] = bs.toByteArray();
                    metadataCounter++;
                    bs.reset();
                    metaDataTypes[metadataCounter] = Open_Image5D.tagTHR;
                    ds.writeInt(1);
                    ds.writeInt(c);
                    ds.writeDouble(chDispProps.getMinThreshold());
                    ds.writeDouble(chDispProps.getMaxThreshold());
                    ds.writeInt(chDispProps.getLutUpdateMode());
                    ds.flush();
                    metaData[metadataCounter] = bs.toByteArray();
                    metadataCounter++;
                    bs.reset();
                    metaDataTypes[metadataCounter] = Open_Image5D.tagGRA;
                    ds.writeInt(1);
                    ds.writeInt(c);
                    ds.writeBoolean(chDispProps.isDisplayedGray());
                    ds.flush();
                    metaData[metadataCounter] = bs.toByteArray();
                    metadataCounter++;
                    bs.reset();
                    metaDataTypes[metadataCounter] = Open_Image5D.tagOVL;
                    ds.writeInt(1);
                    ds.writeInt(c);
                    ds.writeBoolean(chDispProps.isDisplayedInOverlay());
                    ds.flush();
                    metaData[metadataCounter] = bs.toByteArray();
                    metadataCounter++;
                    bs.reset();
                    if (chCalibration.getLabel() != null && chCalibration.getLabel() != "") {
                        metaDataTypes[metadataCounter] = Open_Image5D.tagLBL;
                        ds.writeInt(1);
                        ds.writeInt(c);
                        ds.write(chCalibration.getLabel().getBytes());
                        ds.flush();
                        metaData[metadataCounter] = bs.toByteArray();
                        metadataCounter++;
                        bs.reset();
                    }
                    if (chCalibration.getFunction() != Calibration.NONE) {
                        metaDataTypes[metadataCounter] = Open_Image5D.tagCAL;
                        ds.writeInt(1);
                        ds.writeInt(c);
                        ds.writeInt(chCalibration.getFunction());
                        boolean coefficientsNull = (chCalibration.getCoefficients() == null);
                        if (!coefficientsNull) {
                            ds.writeInt(chCalibration.getCoefficients().length);
                            for (int n = 0; n < chCalibration.getCoefficients().length; n++) {
                                ds.writeDouble(chCalibration.getCoefficients()[n]);
                            }
                        } else {
                            ds.writeInt(0);
                        }
                        ds.writeBoolean(chCalibration.isZeroClip());
                        ds.write(chCalibration.getValueUnit().getBytes());
                        ds.flush();
                        metaData[metadataCounter] = bs.toByteArray();
                        metadataCounter++;
                        bs.reset();
                    }
                    bs.close();
                }
            } catch (IOException e) {
            }
            fi.metaDataTypes = new int[metadataCounter];
            fi.metaData = new byte[metadataCounter][];
            for (int n = 0; n < metadataCounter; n++) {
                fi.metaDataTypes[n] = metaDataTypes[n];
                fi.metaData[n] = metaData[n];
            }
        }
        try {
            TiffEncoder file = new TiffEncoder(fi);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
            file.write(out);
            out.close();
        } catch (IOException e) {
            showErrorMessage(e);
            return false;
        }
        updateImp(imp, fi, name, directory, FileInfo.TIFF);
        return true;
    }

    private void updateImp(ImagePlus imp, FileInfo fi, String name, String directory, int fileFormat) {
        imp.changes = false;
        if (name != null) {
            fi.fileFormat = fileFormat;
            fi.fileName = name;
            fi.directory = directory;
            if (fileFormat == FileInfo.TIFF) fi.offset = 768;
            fi.description = null;
            imp.setTitle(name);
            imp.setFileInfo(fi);
        }
    }

    void showErrorMessage(IOException e) {
        IJ.error("An error occured writing the file.\n \n" + e);
    }
}
