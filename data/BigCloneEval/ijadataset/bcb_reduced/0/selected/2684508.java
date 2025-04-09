package org.phymote.persistance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.phymote.sensor.ChartModificationSensor;
import org.phymote.sensor.data.VirtualData;
import org.phymote.sensor.datarow.ChartModificationDataRow;

public class SensorXML {

    private Document xmlDoc;

    private Element sensorElement;

    private Element dataElement;

    /**
	 * Creates a new (empty) XML-Object to save
	 */
    public SensorXML() {
        xmlDoc = new Document(new Element("VirtualDataSensor"));
        sensorElement = new Element("Sensor");
        dataElement = new Element("Data");
    }

    public SensorXML(File savefile) {
        try {
            xmlDoc = new SAXBuilder().build(savefile);
        } catch (JDOMException e) {
            System.err.println("Error reading settings file.");
        } catch (IOException e) {
            System.err.println("Error reading settings file.");
        }
        sensorElement = xmlDoc.getRootElement().getChild("Sensor");
        dataElement = xmlDoc.getRootElement().getChild("Data");
    }

    public SensorXML(ZipFile savefile) {
        try {
            xmlDoc = new SAXBuilder().build(savefile.getInputStream(savefile.getEntry("phymote.xml")));
            sensorElement = xmlDoc.getRootElement().getChild("Sensor");
            dataElement = xmlDoc.getRootElement().getChild("Data");
        } catch (ZipException e) {
            System.err.println("Error reading settings file.");
        } catch (JDOMException e) {
            System.err.println("Error reading settings file.");
        } catch (IOException e) {
            System.err.println("Error reading settings file.");
        }
    }

    public void setSensor(ChartModificationSensor sensor, String name) {
        try {
            xmlDoc.getRootElement().getChild("Sensor").detach();
        } catch (NullPointerException e) {
        }
        sensorElement = new Element("Sensor");
        sensorElement.setAttribute("name", name != null ? name : sensor.getName());
        for (int i = 0; i < sensor.getDimension(); i++) {
            Element dimensionElement = new Element("dimension").setAttribute("id", "" + i);
            dimensionElement.addContent(new Element("calibratezero").setText(sensor.getCalibrationZero(i) + ""));
            dimensionElement.addContent(new Element("calibratevalue").setText(sensor.getCalibrationValue(i) + ""));
            dimensionElement.addContent(new Element("calibraterepresentvalue").setText(sensor.getCalibrateRepresentValue(i) + ""));
            dimensionElement.addContent(new Element("unit").setText(sensor.getUnit(i)));
            dimensionElement.addContent(new Element("desc").setText(sensor.getDesc(i)));
            sensorElement.addContent(dimensionElement);
        }
        xmlDoc.getRootElement().addContent(sensorElement);
    }

    public void setData(ChartModificationDataRow dataRow) {
        try {
            xmlDoc.getRootElement().getChild("Data").detach();
        } catch (NullPointerException e) {
        }
        dataElement = getDataElement(dataRow.getDataCount(), dataRow);
        xmlDoc.getRootElement().addContent(dataElement);
    }

    private Element getDataElement(int length, ChartModificationDataRow dataRow) {
        if (length < 0) {
            return null;
        }
        int i = 0;
        try {
            Element result = new Element("Data");
            for (i = 0; i < length; i++) {
                Element valueElement = new Element("singleData");
                for (int dim = 0; dim < dataRow.getDimension(); dim++) {
                    valueElement.addContent(new Element("value").setAttribute("dim", "" + dim).setText("" + dataRow.getData(i).getValueDouble(dim)));
                }
                Element mtimeElement = new Element("mtime");
                mtimeElement.setText("" + dataRow.getData(i).getMTime());
                valueElement.addContent(mtimeElement);
                result.addContent(valueElement);
            }
            return result;
        } catch (OutOfMemoryError e) {
            System.err.println("Out of Memory. Savefile incomplete!\n");
            return getDataElement(i - 500, dataRow);
        }
    }

    @Override
    public String toString() {
        return new XMLOutputter().outputString(xmlDoc);
    }

    public void saveXML(File file) {
        try {
            new XMLOutputter().output(xmlDoc, new FileWriter(file));
        } catch (IOException e) {
            System.err.println("Error writing file.");
        } catch (OutOfMemoryError e) {
            System.err.println("Error writing file : Out of Memory.");
            file.delete();
            xmlDoc.removeContent();
        }
    }

    public void saveZippedXML(File file) {
        try {
            ZipOutputStream zipo = new ZipOutputStream(new FileOutputStream(file));
            zipo.putNextEntry(new ZipEntry("phymote.xml"));
            new XMLOutputter().output(xmlDoc, zipo);
            zipo.close();
        } catch (IOException e) {
            System.err.println("Error writing file.");
        } catch (OutOfMemoryError e) {
            System.err.println("Error writing file : Out of Memory.");
            file.delete();
            xmlDoc.removeContent();
        }
    }

    @SuppressWarnings("unchecked")
    public void makeSensor(ChartModificationSensor sensor) {
        sensor.setName(sensorElement.getAttributeValue("name"));
        List<Element> dimensions = sensorElement.getChildren("dimension");
        int[] calibrateZeros = new int[dimensions.size()];
        int[] calibrateValues = new int[dimensions.size()];
        float[] calibrateRepresentValues = new float[dimensions.size()];
        String[] units = new String[dimensions.size()];
        String[] desc = new String[dimensions.size()];
        for (Element dimensionElement : dimensions) {
            int id = Integer.parseInt(dimensionElement.getAttributeValue("id"));
            calibrateZeros[id] = Integer.parseInt(dimensionElement.getChildText("calibratezero"));
            calibrateValues[id] = Integer.parseInt(dimensionElement.getChildText("calibratevalue"));
            calibrateRepresentValues[id] = Float.parseFloat(dimensionElement.getChildText("calibraterepresentvalue"));
            units[id] = dimensionElement.getChildText("unit");
            desc[id] = dimensionElement.getChildText("desc");
        }
        sensor.setDesc(desc);
        sensor.setCalibration(calibrateZeros, calibrateValues, calibrateRepresentValues, units);
        ChartModificationDataRow dataRow = new ChartModificationDataRow(sensor);
        List<Element> data = dataElement.getChildren("singleData");
        for (Element singleData : data) {
            List<Element> dataValues = singleData.getChildren("value");
            double myData[] = new double[dataValues.size()];
            for (Element singleValue : dataValues) {
                myData[Integer.parseInt(singleValue.getAttributeValue("dim"))] = Double.parseDouble(singleValue.getText());
            }
            int mTime = Integer.parseInt(singleData.getChildText("mtime"));
            dataRow.addData(new VirtualData(dataRow, mTime, myData));
        }
        sensor.setDataRow(dataRow);
    }
}
