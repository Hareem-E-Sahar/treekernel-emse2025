package doors.ui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jdom.Document;
import org.jdom.Element;
import doors.XmlParseException;
import doors.util.Util;

public class UiUtil {

    public static final int INVOKE_JAVA = 0;

    public static final int INVOKE_SHELL = 1;

    private static int cascadeX = 20;

    private static int cascadeY = 50;

    private static int cascadeWidth = 25;

    private static int cascadeCount = 0;

    private static int cascadeMax = 10;

    /**
	 * Loads the XML from the URL, resolves $(URLPATH), returns an Element
	 * representing the root Part.
	 */
    public static Element loadPartXml(java.net.URL xmlUrl) throws IOException, XmlParseException {
        String urlStr = xmlUrl.toExternalForm();
        String urlPath = urlStr.substring(0, urlStr.lastIndexOf("/"));
        urlPath.replaceAll("%", "&#x25;");
        String xmlStr = Util.readUrl(xmlUrl);
        xmlStr = xmlStr.replaceAll("\\$\\(URLPATH\\)", urlPath);
        Document doc = doors.util.XmlUtil.stringToDocument(xmlStr);
        return doc.getRootElement();
    }

    /**
	 * launchClient
	 */
    public static JFrame launchClient(int partId, String deviceName) throws java.io.IOException {
        String ior = Variables.controller.getDeviceIor(deviceName);
        JFrame frame = null;
        if (ior.equals("")) {
            doors.util.Swing.errorMessage("Couldn't get IOR for " + deviceName, "Error Launching Client");
            return null;
        }
        int invokationType = INVOKE_JAVA;
        if (invokationType == INVOKE_SHELL) {
            String command = "/bin/sh /home/adam/work/doors/bin/midifileplayer-client.sh " + ior;
            Runtime.getRuntime().exec(command);
        } else if (invokationType == INVOKE_JAVA) {
            org.omg.CORBA.Object obj = Variables.orb.string_to_object(ior);
            doors.Device deviceObj = doors.DeviceHelper.narrow(obj);
            String className = "doors.midifileplayer.client.MidiFilePlayerClient";
            IDeviceClient client = null;
            try {
                Class deviceClass = Class.forName(className);
                Constructor constructor = deviceClass.getConstructor(new Class[] {});
                client = (IDeviceClient) constructor.newInstance(new Object[] {});
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.toString());
            }
            frame = new JFrame(deviceName);
            JPanel panel = client.getPanel(partId, deviceObj);
            frame.setContentPane(panel);
            frame.setVisible(true);
            frame.setSize(panel.getPreferredSize());
            frame.setLocation(cascadeX + cascadeWidth * cascadeCount, cascadeY + cascadeWidth * cascadeCount);
            cascadeCount++;
            if (cascadeCount >= cascadeMax) cascadeCount = 0;
        } else {
            doors.util.Swing.errorMessage("Cannot invoke " + deviceName + " client - unknown invokationType in properties file", "Error launching " + deviceName);
        }
        return frame;
    }
}
