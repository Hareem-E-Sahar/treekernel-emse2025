package kfschmidt.data4d.io;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
import java.util.zip.*;
import kfschmidt.qvii.Task;
import kfschmidt.data4d.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.*;
import org.w3c.dom.*;
import javax.swing.text.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import java.awt.*;
import java.awt.geom.*;
import kfschmidt.geom3d.*;

public class QVIIIO {

    Task mTask;

    public QVIIIO() {
    }

    public QVIIIO(Task t) {
        mTask = t;
    }

    public void saveQVIIVolume(VolumeTimeSeries vol, File outfile) throws Exception {
        ZipOutputStream zout = openZipArchive(outfile);
        String volxml = getStringForVol(vol);
        writeXMLToZipArchive("qvii_volume.xml", volxml, zout);
        streamDataToZip(vol, zout);
        zout.close();
        System.out.println("QVIIVolume saved.");
    }

    public VolumeTimeSeries loadQVIIVolume(File qv2file) throws Exception {
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(qv2file), 5000000));
        ZipEntry ze = null;
        Vector entry_names = new Vector();
        Vector entry_data = new Vector();
        String xml_data = null;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.getName().toLowerCase().endsWith(".xml")) {
                xml_data = getByteOutputStreamForZipEntry(zin).toString();
            }
            zin.closeEntry();
        }
        zin.close();
        zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(qv2file), 5000000));
        String datafile = getDataFileName(xml_data);
        VolumeTimeSeries retvol = getVolForString(xml_data);
        while ((ze = zin.getNextEntry()) != null) {
            System.out.println("testing enry: " + ze.getName() + " against: " + datafile);
            if (ze.getName().equals(datafile)) {
                loadDataFromZip(zin, retvol);
            }
            zin.closeEntry();
        }
        zin.close();
        System.out.println("loadQVIIVolume(" + qv2file + ") finished, returning: " + retvol);
        if (mTask != null && mTask.getComplete()) return null;
        return retvol;
    }

    public void streamDataToZip(VolumeTimeSeries vol, ZipOutputStream zout) throws Exception {
        ZipEntry entry = new ZipEntry(vol.getId() + ".dat");
        zout.putNextEntry(entry);
        if (vol instanceof FloatVolumeTimeSeries) {
            streamData(((FloatVolumeTimeSeries) vol).getData(), zout);
        } else if (vol instanceof RGBVolumeTimeSeries) {
            streamData(((RGBVolumeTimeSeries) vol).getARGBData(), zout);
        } else if (vol instanceof BinaryVolumeTimeSeries) {
            streamData(((BinaryVolumeTimeSeries) vol).getBinaryData(), zout);
        }
    }

    private ZipOutputStream openZipArchive(File file) throws Exception {
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file), 5000000));
        zout.setLevel(0);
        return zout;
    }

    private void writeXMLToZipArchive(String filename, String xml, ZipOutputStream zout) throws Exception {
        ZipEntry entry = new ZipEntry(filename);
        zout.putNextEntry(entry);
        zout.write(xml.getBytes());
    }

    public void loadDataFromZip(ZipInputStream zin, VolumeTimeSeries vol) throws Exception {
        System.out.println("loadDataFromZip()");
        if (vol instanceof FloatVolumeTimeSeries) {
            loadFloatDataFromZipEntry(zin, ((FloatVolumeTimeSeries) vol).getData());
        } else if (vol instanceof RGBVolumeTimeSeries) {
            loadRGBDataFromZipEntry(zin, ((RGBVolumeTimeSeries) vol).getAData(), ((RGBVolumeTimeSeries) vol).getRData(), ((RGBVolumeTimeSeries) vol).getGData(), ((RGBVolumeTimeSeries) vol).getBData());
        } else if (vol instanceof BinaryVolumeTimeSeries) {
            loadBoolDataFromZipEntry(zin, ((BinaryVolumeTimeSeries) vol).getBinaryData());
        }
    }

    public String getDataFileName(String xml) throws Exception {
        if (xml == null) throw new Exception("Null volume xml");
        VolumeTimeSeries retvol = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(bais);
        NodeList list = doc.getElementsByTagName("QVIIVOLUMETIMESERIES");
        if (list.getLength() < 1) throw new Exception("QVIIVOLUMETIMESERIES element not found");
        Element rootel = (Element) list.item(0);
        list = rootel.getElementsByTagName("VOLUME");
        if (list.getLength() < 1) throw new Exception("VOLUME element not found");
        Element volel = (Element) list.item(0);
        return volel.getAttribute("datafile");
    }

    public VolumeTimeSeries getVolForString(String xml) throws Exception {
        if (xml == null) throw new Exception("Null volume xml");
        VolumeTimeSeries retvol = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(bais);
        NodeList list = doc.getElementsByTagName("QVIIVOLUMETIMESERIES");
        if (list.getLength() < 1) throw new Exception("QVIIVOLUMETIMESERIES element not found");
        Element rootel = (Element) list.item(0);
        list = rootel.getElementsByTagName("VOLUME");
        if (list.getLength() < 1) throw new Exception("VOLUME element not found");
        Element volel = (Element) list.item(0);
        String typestr = volel.getAttribute("type");
        String idstr = volel.getAttribute("id");
        String descstr = volel.getAttribute("desc");
        String scalexstr = volel.getAttribute("scalex");
        String scaleystr = volel.getAttribute("scaley");
        String scalezstr = volel.getAttribute("scalez");
        String rotxstr = volel.getAttribute("rotx");
        String rotystr = volel.getAttribute("roty");
        String rotzstr = volel.getAttribute("rotz");
        String transxstr = volel.getAttribute("transx");
        String transystr = volel.getAttribute("transy");
        String transzstr = volel.getAttribute("transz");
        String fovxstr = volel.getAttribute("fovx");
        String fovystr = volel.getAttribute("fovy");
        String fovzstr = volel.getAttribute("fovz");
        String thkstr = volel.getAttribute("thk");
        String widthstr = volel.getAttribute("width");
        String heightstr = volel.getAttribute("height");
        String slicesstr = volel.getAttribute("slices");
        String spacestr = volel.getAttribute("space");
        String repsstr = volel.getAttribute("reps");
        String trstr = volel.getAttribute("trs");
        double scalex = Double.parseDouble(scalexstr);
        double scaley = Double.parseDouble(scaleystr);
        double scalez = Double.parseDouble(scalezstr);
        double rotx = Double.parseDouble(rotxstr);
        double roty = Double.parseDouble(rotystr);
        double rotz = Double.parseDouble(rotzstr);
        double transx = Double.parseDouble(transxstr);
        double transy = Double.parseDouble(transystr);
        double transz = Double.parseDouble(transzstr);
        double fovx = Double.parseDouble(fovxstr);
        double fovy = Double.parseDouble(fovystr);
        double fovz = Double.parseDouble(fovzstr);
        double thk = Double.parseDouble(thkstr);
        double space = Double.parseDouble(spacestr);
        int width = Integer.parseInt(widthstr);
        int height = Integer.parseInt(heightstr);
        int slices = Integer.parseInt(slicesstr);
        int reps = Integer.parseInt(repsstr);
        double[] trs = VolumeTimeSeries.parseTRString(trstr, reps);
        if (typestr.equals("RGB")) {
            retvol = new RGBVolumeTimeSeries(idstr, height, width, slices, reps, trs, fovx, fovy, thk, space);
        } else if (typestr.equals("FLOAT")) {
            retvol = new FloatVolumeTimeSeries(idstr, height, width, slices, reps, trs, fovx, fovy, thk, space);
        } else if (typestr.equals("BOOL")) {
            retvol = new BinaryVolumeTimeSeries(idstr, height, width, slices, reps, trs, fovx, fovy, thk, space);
        }
        if (retvol != null && descstr != null) retvol.setDescription(descstr);
        return retvol;
    }

    public String getStringForVol(VolumeTimeSeries vol) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000000);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        DOMImplementation di = builder.getDOMImplementation();
        Document doc = di.createDocument("", "QVIIVOLUMETIMESERIES", null);
        Element rootElement = doc.getDocumentElement();
        rootElement.appendChild(getElementForVolume(vol, doc));
        StreamResult res_stream = new StreamResult(baos);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer serializer = transformerFactory.newTransformer();
        serializer.transform(new DOMSource(doc), res_stream);
        return baos.toString();
    }

    protected Element getElementForVolume(VolumeTimeSeries vol, Document doc) throws Exception {
        Element volel = doc.createElement("VOLUME");
        volel.setAttribute("id", vol.getId());
        String type = "nulltype";
        if (vol.getType() == VolumeTimeSeries.RGB) {
            type = "RGB";
        } else if (vol.getType() == VolumeTimeSeries.FLOAT) {
            type = "FLOAT";
        } else if (vol.getType() == VolumeTimeSeries.BOOL) {
            type = "BOOL";
        }
        volel.setAttribute("type", type);
        volel.setAttribute("scalex", "" + vol.getScaleX());
        volel.setAttribute("scaley", "" + vol.getScaleY());
        volel.setAttribute("scalez", "" + vol.getScaleZ());
        volel.setAttribute("rotx", "" + vol.getRotX());
        volel.setAttribute("roty", "" + vol.getRotY());
        volel.setAttribute("rotz", "" + vol.getRotZ());
        volel.setAttribute("transx", "" + vol.getTransX());
        volel.setAttribute("transy", "" + vol.getTransY());
        volel.setAttribute("transz", "" + vol.getTransZ());
        volel.setAttribute("desc", vol.getDescription());
        volel.setAttribute("fovx", "" + vol.getFOVX());
        volel.setAttribute("fovy", "" + vol.getFOVY());
        volel.setAttribute("fovz", "" + vol.getFOVZ());
        volel.setAttribute("thk", "" + vol.getThk());
        volel.setAttribute("width", "" + vol.getWidth());
        volel.setAttribute("height", "" + vol.getHeight());
        volel.setAttribute("slices", "" + vol.getSlices());
        volel.setAttribute("space", "" + vol.getSpace());
        volel.setAttribute("reps", "" + vol.getReps());
        volel.setAttribute("trs", "" + vol.getTRString());
        volel.setAttribute("datafile", vol.getId() + ".dat");
        return volel;
    }

    public void streamData(double[][][][] data, ZipOutputStream zout) throws Exception {
        DataOutputStream daos = new DataOutputStream(zout);
        for (int rep = 0; rep < data.length; rep++) {
            if (mTask != null && mTask.getComplete()) return;
            if (mTask != null) mTask.setPctComplete((int) (100f * (float) rep / (float) data.length));
            for (int slice = 0; slice < data[0].length; slice++) {
                for (int y = 0; y < data[0][0][0].length; y++) {
                    for (int x = 0; x < data[0][0].length; x++) {
                        daos.writeFloat((float) data[rep][slice][x][y]);
                    }
                }
            }
        }
        daos.flush();
        daos.close();
    }

    public void streamData(int[][][][] data, ZipOutputStream zout) throws Exception {
        DataOutputStream daos = new DataOutputStream(zout);
        for (int rep = 0; rep < data.length; rep++) {
            if (mTask != null && mTask.getComplete()) return;
            if (mTask != null) mTask.setPctComplete((int) (100f * (float) rep / (float) data.length));
            for (int slice = 0; slice < data[0].length; slice++) {
                for (int y = 0; y < data[0][0][0].length; y++) {
                    for (int x = 0; x < data[0][0].length; x++) {
                        daos.writeInt(data[rep][slice][x][y]);
                    }
                }
            }
        }
        daos.flush();
        daos.close();
    }

    public void streamData(boolean[][][][] data, ZipOutputStream zout) throws Exception {
        DataOutputStream daos = new DataOutputStream(zout);
        int counter = 0;
        int workingint = 0;
        for (int rep = 0; rep < data.length; rep++) {
            if (mTask != null && mTask.getComplete()) return;
            if (mTask != null) mTask.setPctComplete((int) (100f * (float) rep / (float) data.length));
            for (int slice = 0; slice < data[0].length; slice++) {
                for (int y = 0; y < data[0][0][0].length; y++) {
                    for (int x = 0; x < data[0][0].length; x++) {
                        if (data[rep][slice][x][y]) {
                            workingint = workingint | (0x01 << (31 - counter));
                        }
                        counter++;
                        if (counter == 32) {
                            counter = 0;
                            daos.writeInt(workingint);
                            workingint = 0;
                        }
                    }
                }
            }
        }
        daos.flush();
        daos.close();
    }

    /**
     *  TODO: change to local implementation of uuencode/decode
     */
    public String uuEncodeByteArray(byte[] bindata) throws Exception {
        BASE64Encoder encoder = new BASE64Encoder();
        String base64 = encoder.encode(bindata);
        return base64;
    }

    /**
     *  TODO: change to local implementation of uuencode/decode
     */
    public byte[] uuDecodeString(String uudata) throws Exception {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(uudata);
    }

    private void loadBoolDataFromZipEntry(ZipInputStream zin, boolean[][][][] data) throws Exception {
        DataInputStream dis = new DataInputStream(zin);
        int workingint = dis.readInt();
        int counter = 0;
        int mask = 0;
        int test = 0;
        for (int rep = 0; rep < data.length; rep++) {
            if (mTask != null && mTask.getComplete()) return;
            if (mTask != null) mTask.setPctComplete((int) (100f * (float) rep / (float) data.length));
            for (int slice = 0; slice < data[0].length; slice++) {
                for (int y = 0; y < data[0][0][0].length; y++) {
                    for (int x = 0; x < data[0][0].length; x++) {
                        if (counter == 32) {
                            counter = 0;
                            workingint = dis.readInt();
                        }
                        mask = (0x01 << (31 - counter));
                        test = workingint & mask;
                        test = test >>> (31 - counter);
                        if (test == 1) data[rep][slice][x][y] = true;
                        counter++;
                    }
                }
            }
        }
    }

    private void loadRGBDataFromZipEntry(ZipInputStream zin, byte[][][][] adata, byte[][][][] rdata, byte[][][][] gdata, byte[][][][] bdata) throws Exception {
        DataInputStream dis = new DataInputStream(zin);
        int workingint = 0;
        for (int rep = 0; rep < adata.length; rep++) {
            if (mTask != null && mTask.getComplete()) return;
            if (mTask != null) mTask.setPctComplete((int) (100f * (float) rep / (float) adata.length));
            for (int slice = 0; slice < adata[0].length; slice++) {
                for (int y = 0; y < adata[0][0][0].length; y++) {
                    for (int x = 0; x < adata[0][0].length; x++) {
                        workingint = dis.readInt();
                        adata[rep][slice][x][y] = (byte) (0xff & (workingint >>> 24));
                        rdata[rep][slice][x][y] = (byte) (0xff & (workingint >>> 16));
                        gdata[rep][slice][x][y] = (byte) (0xff & (workingint >>> 8));
                        bdata[rep][slice][x][y] = (byte) (0xff & (workingint));
                    }
                }
            }
        }
    }

    private void loadFloatDataFromZipEntry(ZipInputStream zin, double[][][][] data) throws Exception {
        DataInputStream dis = new DataInputStream(zin);
        for (int rep = 0; rep < data.length; rep++) {
            if (mTask != null && mTask.getComplete()) return;
            if (mTask != null) mTask.setPctComplete((int) (100f * (float) rep / (float) data.length));
            for (int slice = 0; slice < data[0].length; slice++) {
                for (int y = 0; y < data[0][0][0].length; y++) {
                    for (int x = 0; x < data[0][0].length; x++) {
                        data[rep][slice][x][y] = dis.readFloat();
                    }
                }
            }
        }
    }

    private ByteArrayOutputStream getByteOutputStreamForZipEntry(ZipInputStream zin) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100000);
        byte data[] = new byte[2048];
        int count = 0;
        while ((count = zin.read(data)) != -1) {
            baos.write(data, 0, count);
        }
        return baos;
    }
}
