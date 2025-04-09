package gov.usda.gdpc.axis2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;

/**
 *
 * @author  terryc
 */
public final class GDPCAxis2Utils {

    /**
     * GDPCAxis2Utils Constructor.
     */
    private GDPCAxis2Utils() {
    }

    public static OMElement getFirstChildWithName(OMElement parent, String local) {
        Iterator itr = parent.getChildrenWithName(new QName(GDPCAxis2Constants.GDPC_NAMESPACE, local));
        if (itr.hasNext()) {
            return (OMElement) itr.next();
        } else {
            return null;
        }
    }

    public static OMElement createAttachment(Object obj, String local, OMFactory factory, OMNamespace namespace) {
        OMElement attachment = null;
        ZipOutputStream zos = null;
        ByteArrayOutputStream byteStream = null;
        ObjectOutputStream objectOutStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            zos = new ZipOutputStream(byteStream);
            ZipEntry entry = new ZipEntry(local);
            zos.putNextEntry(entry);
            objectOutStream = new ObjectOutputStream(zos);
            objectOutStream.writeObject(obj);
            zos.closeEntry();
            attachment = factory.createOMElement(local, namespace);
            DataHandler dh = new DataHandler(new ByteArrayDataSource(byteStream.toByteArray()));
            OMText text = factory.createOMText(dh, true);
            attachment.addChild(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attachment;
    }

    public static OMElement createAttachments(Object[] objects, String[] locals, OMFactory factory, OMNamespace namespace) {
        if (objects.length != locals.length) {
            throw new IllegalArgumentException("GDPCAxis2Utils: createAttachments: should be same number of objects and locals.");
        }
        OMElement attachment = null;
        ZipOutputStream zos = null;
        ByteArrayOutputStream byteStream = null;
        ObjectOutputStream objectOutStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            zos = new ZipOutputStream(byteStream);
            for (int i = 0; i < objects.length; i++) {
                ZipEntry entry = new ZipEntry(locals[i]);
                zos.putNextEntry(entry);
                objectOutStream = new ObjectOutputStream(zos);
                objectOutStream.writeObject(objects[i]);
                zos.closeEntry();
            }
            attachment = factory.createOMElement("attachment", namespace);
            DataHandler dh = new DataHandler(new ByteArrayDataSource(byteStream.toByteArray()));
            OMText text = factory.createOMText(dh, true);
            attachment.addChild(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attachment;
    }

    public static Object getAttachment(OMElement element, String local) {
        OMElement resultElement = getFirstChildWithName(element, local);
        if (resultElement == null) {
            return null;
        }
        OMText binaryNode = (OMText) resultElement.getFirstOMChild();
        DataHandler actualDH = (DataHandler) binaryNode.getDataHandler();
        Object obj = null;
        try {
            ByteArrayInputStream input = (ByteArrayInputStream) actualDH.getInputStream();
            ZipInputStream zis = new ZipInputStream(input);
            ZipEntry entry = zis.getNextEntry();
            ObjectInputStream ois = new ObjectInputStream(zis);
            obj = ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static List getAttachments(OMElement element) {
        OMElement resultElement = getFirstChildWithName(element, "attachment");
        if (resultElement == null) {
            return null;
        }
        OMText binaryNode = (OMText) resultElement.getFirstOMChild();
        DataHandler actualDH = (DataHandler) binaryNode.getDataHandler();
        List result = new ArrayList();
        try {
            ByteArrayInputStream input = (ByteArrayInputStream) actualDH.getInputStream();
            ZipInputStream zis = new ZipInputStream(input);
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                ObjectInputStream ois = new ObjectInputStream(zis);
                Object obj = ois.readObject();
                result.add(obj);
                entry = zis.getNextEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void printOMElements(OMElement element, String tabSpaces) {
        if (tabSpaces == null) {
            tabSpaces = "";
        }
        printOMElement(element, tabSpaces);
        Iterator itr = element.getChildElements();
        while (itr.hasNext()) {
            Object obj = itr.next();
            printOMElements((OMElement) obj, tabSpaces + "   ");
        }
    }

    public static void printOMElement(OMElement element, String tabSpaces) {
        if (tabSpaces == null) {
            tabSpaces = "";
        }
        System.out.println(tabSpaces + "element class: " + element.getClass().getName());
        System.out.println(tabSpaces + "element toString: " + element.toString());
        System.out.println(tabSpaces + "element getText: " + element.getText());
    }
}
