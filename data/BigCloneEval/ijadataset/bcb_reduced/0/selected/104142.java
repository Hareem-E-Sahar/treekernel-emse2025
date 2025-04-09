package org.ofbiz.content.openoffice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Random;
import java.sql.Timestamp;
import java.lang.Math;
import java.net.MalformedURLException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.entity.util.ByteWrapper;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * OpenOfficeServices Class
 */
public class OpenOfficeServices {

    public static final String module = OpenOfficeServices.class.getName();

    /**
     * Use OpenOffice to convert documents between types
     * This service requires that the "content.temp.dir" directory be set in the content.properties file.
     * This value should be operating system dependent with "\\" separators for Windows
     * and "/" for Linux/Unix.
     */
    public static Map convertDocumentByteWrapper(DispatchContext dctx, Map context) {
        Map results = ServiceUtil.returnSuccess();
        GenericDelegator delegator = dctx.getDelegator();
        XMultiComponentFactory xmulticomponentfactory = null;
        Timestamp ts = UtilDateTime.nowTimestamp();
        Random random = new Random(ts.getTime());
        String uniqueSeqNum = Integer.toString(Math.abs(random.nextInt()));
        String fileInName = "OOIN_" + uniqueSeqNum;
        String fileOutName = "OOOUT_" + uniqueSeqNum;
        File fileIn = null;
        File fileOut = null;
        ByteWrapper inByteWrapper = (ByteWrapper) context.get("inByteWrapper");
        String inputMimeType = (String) context.get("inputMimeType");
        String outputMimeType = (String) context.get("outputMimeType");
        String extName = OpenOfficeWorker.getExtensionFromMimeType(outputMimeType);
        fileOutName += "." + extName;
        String oooHost = (String) context.get("oooHost");
        String oooPort = (String) context.get("oooPort");
        try {
            xmulticomponentfactory = OpenOfficeWorker.getRemoteServer(oooHost, oooPort);
            byte[] inByteArray = inByteWrapper.getBytes();
            String tempDir = UtilProperties.getPropertyValue("content", "content.temp.dir");
            fileIn = new File(tempDir + fileInName);
            FileOutputStream fos = new FileOutputStream(fileIn);
            fos.write(inByteArray);
            fos.close();
            Debug.logInfo("fileIn:" + tempDir + fileInName, module);
            OpenOfficeWorker.convertOODocToFile(xmulticomponentfactory, tempDir + fileInName, tempDir + fileOutName, outputMimeType);
            fileOut = new File(tempDir + fileOutName);
            FileInputStream fis = new FileInputStream(fileOut);
            int c;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((c = fis.read()) > -1) {
                baos.write(c);
            }
            fis.close();
            results.put("outByteWrapper", new ByteWrapper(baos.toByteArray()));
            baos.close();
        } catch (MalformedURLException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.toString());
        } catch (FileNotFoundException e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        } catch (IOException e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        } catch (Exception e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        } finally {
            if (fileIn != null) fileIn.delete();
            if (fileOut != null) fileOut.delete();
        }
        return results;
    }

    /**
     * Use OpenOffice to convert documents between types
     */
    public static Map convertDocument(DispatchContext dctx, Map context) {
        XMultiComponentFactory xmulticomponentfactory = null;
        String stringUrl = "file:///" + context.get("filenameFrom");
        String stringConvertedFile = "file:///" + context.get("filenameTo");
        String filterName = "file:///" + context.get("filterName");
        String oooHost = (String) context.get("oooHost");
        String oooPort = (String) context.get("oooPort");
        try {
            xmulticomponentfactory = OpenOfficeWorker.getRemoteServer(oooHost, oooPort);
            OpenOfficeWorker.convertOODocToFile(xmulticomponentfactory, stringUrl, stringConvertedFile, filterName);
            Map results = ServiceUtil.returnSuccess();
            return results;
        } catch (IOException e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        } catch (Exception e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        }
    }

    /**
     * Use OpenOffice to convert documents between types
     */
    public static Map convertDocumentFileToFile(DispatchContext dctx, Map context) {
        XMultiComponentFactory xmulticomponentfactory = null;
        String stringUrl = (String) context.get("filenameFrom");
        String stringConvertedFile = (String) context.get("filenameTo");
        String inputMimeType = (String) context.get("inputMimeType");
        String outputMimeType = (String) context.get("outputMimeType");
        String oooHost = (String) context.get("oooHost");
        String oooPort = (String) context.get("oooPort");
        try {
            xmulticomponentfactory = OpenOfficeWorker.getRemoteServer(oooHost, oooPort);
            File inputFile = new File(stringUrl);
            long fileSize = inputFile.length();
            FileInputStream fis = new FileInputStream(inputFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream((int) fileSize);
            int c;
            while ((c = fis.read()) != -1) {
                baos.write(c);
            }
            OpenOfficeByteArrayInputStream oobais = new OpenOfficeByteArrayInputStream(baos.toByteArray());
            OpenOfficeByteArrayOutputStream oobaos = OpenOfficeWorker.convertOODocByteStreamToByteStream(xmulticomponentfactory, oobais, inputMimeType, outputMimeType);
            FileOutputStream fos = new FileOutputStream(stringConvertedFile);
            fos.write(oobaos.toByteArray());
            fos.close();
            fis.close();
            oobais.close();
            oobaos.close();
            Map results = ServiceUtil.returnSuccess();
            return results;
        } catch (IOException e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        } catch (Exception e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        }
    }

    /**
     * Use OpenOffice to convert documents between types
     */
    public static Map convertDocumentStreamToStream(DispatchContext dctx, Map context) {
        XMultiComponentFactory xmulticomponentfactory = null;
        String stringUrl = "file:///" + context.get("filenameFrom");
        String stringConvertedFile = "file:///" + context.get("filenameTo");
        String inputMimeType = (String) context.get("inputMimeType");
        String outputMimeType = (String) context.get("outputMimeType");
        String oooHost = (String) context.get("oooHost");
        String oooPort = (String) context.get("oooPort");
        try {
            xmulticomponentfactory = OpenOfficeWorker.getRemoteServer(oooHost, oooPort);
            File inputFile = new File(stringUrl);
            long fileSize = inputFile.length();
            FileInputStream fis = new FileInputStream(inputFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream((int) fileSize);
            int c;
            while ((c = fis.read()) != -1) {
                baos.write(c);
            }
            OpenOfficeByteArrayInputStream oobais = new OpenOfficeByteArrayInputStream(baos.toByteArray());
            OpenOfficeByteArrayOutputStream oobaos = OpenOfficeWorker.convertOODocByteStreamToByteStream(xmulticomponentfactory, oobais, inputMimeType, outputMimeType);
            FileOutputStream fos = new FileOutputStream(stringConvertedFile);
            fos.write(oobaos.toByteArray());
            fos.close();
            fis.close();
            oobais.close();
            oobaos.close();
            Map results = ServiceUtil.returnSuccess();
            return results;
        } catch (IOException e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        } catch (Exception e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        }
    }

    /**
     * Use OpenOffice to compare documents
     */
    public static Map compareDocuments(DispatchContext dctx, Map context) {
        XMultiComponentFactory xmulticomponentfactory = null;
        String stringUrl = "file:///" + context.get("filenameFrom");
        String stringOriginalFile = "file:///" + context.get("filenameOriginal");
        String stringOutFile = "file:///" + context.get("filenameOut");
        String oooHost = (String) context.get("oooHost");
        String oooPort = (String) context.get("oooPort");
        try {
            xmulticomponentfactory = OpenOfficeWorker.getRemoteServer(oooHost, oooPort);
        } catch (IOException e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        } catch (Exception e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError(e.toString());
        }
        try {
            XPropertySet xpropertysetMultiComponentFactory = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xmulticomponentfactory);
            Object objectDefaultContext = xpropertysetMultiComponentFactory.getPropertyValue("DefaultContext");
            XComponentContext xcomponentcontext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, objectDefaultContext);
            Object desktopObj = xmulticomponentfactory.createInstanceWithContext("com.sun.star.frame.Desktop", xcomponentcontext);
            XDesktop desktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktopObj);
            XComponentLoader xcomponentloader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, desktopObj);
            PropertyValue propertyvalue[] = new PropertyValue[1];
            propertyvalue[0] = new PropertyValue();
            propertyvalue[0].Name = "Hidden";
            propertyvalue[0].Value = new Boolean(true);
            Object objectDocumentToStore = xcomponentloader.loadComponentFromURL(stringUrl, "_blank", 0, propertyvalue);
            XStorable xstorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, objectDocumentToStore);
            propertyvalue = new PropertyValue[1];
            propertyvalue[0] = new PropertyValue();
            propertyvalue[0].Name = "URL";
            propertyvalue[0].Value = stringOriginalFile;
            XFrame frame = desktop.getCurrentFrame();
            Object dispatchHelperObj = xmulticomponentfactory.createInstanceWithContext("com.sun.star.frame.DispatchHelper", xcomponentcontext);
            XDispatchHelper dispatchHelper = (XDispatchHelper) UnoRuntime.queryInterface(XDispatchHelper.class, dispatchHelperObj);
            XDispatchProvider dispatchProvider = (XDispatchProvider) UnoRuntime.queryInterface(XDispatchProvider.class, frame);
            dispatchHelper.executeDispatch(dispatchProvider, ".uno:CompareDocuments", "", 0, propertyvalue);
            propertyvalue = new PropertyValue[1];
            propertyvalue[0] = new PropertyValue();
            propertyvalue[0].Name = "Overwrite";
            propertyvalue[0].Value = new Boolean(true);
            Debug.logInfo("stringOutFile: " + stringOutFile, module);
            xstorable.storeToURL(stringOutFile, propertyvalue);
            XComponent xcomponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xstorable);
            xcomponent.dispose();
            Map results = ServiceUtil.returnSuccess();
            return results;
        } catch (Exception e) {
            Debug.logError(e, "Error in OpenOffice operation: ", module);
            return ServiceUtil.returnError("Error converting document: " + e.toString());
        }
    }
}
