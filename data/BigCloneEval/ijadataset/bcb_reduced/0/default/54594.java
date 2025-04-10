import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.db4o.Db4o;
import com.db4o.ObjectServer;

public class SolarNodeSettings {

    public long repeatCount;

    public long consumptionRepeatInterval;

    public long weatherRepeatInterval;

    public String millisecondsBetweenDataSweeps;

    public String twoDigitHourForDataSweepEvent;

    public String twoDigitMinutesForDataSweepEvent;

    public int modbusReceiveTimeout;

    public int samplesPerSnapshot;

    public boolean debug;

    public boolean generationLoggerActive = false;

    public boolean consumptionLoggerActive = false;

    public boolean weatherLoggerActive = false;

    public int nodeId;

    public int consumptionANodeId;

    public int consumptionBNodeId;

    public int consumptionAHexId;

    public int consumptionBHexId;

    public int weatherReportingNodeId;

    public String localDataFile;

    public String localConsumptionDataFile;

    public String chargeControllerInterface;

    public String socketDataSource;

    public int socketDataPort;

    public String chargeController;

    public String mx60Address;

    public int dataReadOuterWindowInMilliseconds;

    public int dataReadInnerWindowInMilliseconds;

    public int consumptionReadIntervalInMilliseconds;

    public int pvSystemVoltage;

    public String webServiceEndpoint;

    public String consumptionWebServiceEndpoint;

    public String webServiceMethod;

    public String consumptionWebServiceMethod;

    public int systemStorageCapacity;

    public int dropBatteryCapacityLimit;

    public String chargeControllerSerialPort;

    public String batteryComputerSerialPort;

    public String sourceSwitchSerialPort;

    public String consumptionReceiverSerialPort;

    public String consumptionMonitor;

    public String weatherWebServiceEndpoint;

    public String weatherWebServiceMethod;

    public String weatherDotComPartnerId;

    public String weatherDotComLicenseKey;

    public ObjectServer localGenerationDbServer;

    public ObjectServer localConsumptionDbServer;

    public String localDb4oServer;

    public String snFtpUser;

    public String snFtpPassword;

    public void getSettingsFromFile() {
        try {
            System.out.println("IN getSettingsFromFile: " + "\n");
            Date dateNow = new Date();
            SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
            StringBuilder nowYYYYMMDD = new StringBuilder(dateformatYYYYMMDD.format(dateNow));
            System.out.println("today: '" + nowYYYYMMDD + "'");
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File("ini/solarNodeSettings.xml"));
            doc.getDocumentElement().normalize();
            System.out.println("Root element of the doc should be solarNode: " + doc.getDocumentElement().getNodeName());
            NodeList settingsList = doc.getElementsByTagName("settings");
            Node settingsNode = settingsList.item(0);
            if (settingsNode.getNodeType() == Node.ELEMENT_NODE) {
                Element settingsElement = (Element) settingsNode;
                NodeList debugList = settingsElement.getElementsByTagName("debug");
                Element debugElement = (Element) debugList.item(0);
                NodeList debugTextList = debugElement.getChildNodes();
                String debugText = ((Node) debugTextList.item(0)).getNodeValue().toLowerCase().trim();
                if (debugText.equals("true")) {
                    this.debug = true;
                }
                NodeList generationLoggerActiveList = settingsElement.getElementsByTagName("generationLoggerActive");
                Element generationLoggerActiveElement = (Element) generationLoggerActiveList.item(0);
                NodeList generationLoggerActiveTextList = generationLoggerActiveElement.getChildNodes();
                String generationLoggerActiveText = ((Node) generationLoggerActiveTextList.item(0)).getNodeValue().toLowerCase().trim();
                if (generationLoggerActiveText.equals("true")) {
                    this.generationLoggerActive = true;
                    System.out.println("generationLoggerActive true : " + this.generationLoggerActive + "\n");
                } else {
                    System.out.println("generationLoggerActive false : " + this.generationLoggerActive + "\n");
                }
                NodeList consumptionLoggerActiveList = settingsElement.getElementsByTagName("consumptionLoggerActive");
                Element consumptionLoggerActiveElement = (Element) consumptionLoggerActiveList.item(0);
                NodeList consumptionLoggerActiveTextList = consumptionLoggerActiveElement.getChildNodes();
                String consumptionLoggerActiveText = ((Node) consumptionLoggerActiveTextList.item(0)).getNodeValue().toLowerCase().trim();
                if (consumptionLoggerActiveText.equals("true")) {
                    this.consumptionLoggerActive = true;
                    System.out.println("consumptionLoggerActive true : " + this.consumptionLoggerActive + "\n");
                } else {
                    System.out.println("consumptionLoggerActive false : " + this.consumptionLoggerActive + "\n");
                }
                NodeList weatherLoggerActiveList = settingsElement.getElementsByTagName("weatherLoggerActive");
                Element weatherLoggerActiveElement = (Element) weatherLoggerActiveList.item(0);
                NodeList weatherLoggerActiveTextList = weatherLoggerActiveElement.getChildNodes();
                String weatherLoggerActiveText = ((Node) weatherLoggerActiveTextList.item(0)).getNodeValue().toLowerCase().trim();
                if (weatherLoggerActiveText.equals("true")) {
                    this.weatherLoggerActive = true;
                    System.out.println("weatherLoggerActive true : " + this.weatherLoggerActive + "\n");
                } else {
                    System.out.println("weatherLoggerActive false : " + this.weatherLoggerActive + "\n");
                }
                NodeList repeatCountList = settingsElement.getElementsByTagName("repeatCount");
                Element repeatCountElement = (Element) repeatCountList.item(0);
                NodeList repeatCountTextList = repeatCountElement.getChildNodes();
                this.repeatCount = Long.parseLong(((Node) repeatCountTextList.item(0)).getNodeValue().trim());
                System.out.println("repeatCount : " + this.repeatCount + "\n");
                NodeList consumptionRepeatIntervalList = settingsElement.getElementsByTagName("consumptionRepeatInterval");
                Element consumptionRepeatIntervalElement = (Element) consumptionRepeatIntervalList.item(0);
                NodeList consumptionRepeatIntervalTextList = consumptionRepeatIntervalElement.getChildNodes();
                this.consumptionRepeatInterval = Long.parseLong(((Node) consumptionRepeatIntervalTextList.item(0)).getNodeValue().trim());
                System.out.println("consumptionRepeatInterval : " + this.consumptionRepeatInterval + "\n");
                NodeList weatherRepeatIntervalList = settingsElement.getElementsByTagName("weatherRepeatInterval");
                Element weatherRepeatIntervalElement = (Element) weatherRepeatIntervalList.item(0);
                NodeList weatherRepeatIntervalTextList = weatherRepeatIntervalElement.getChildNodes();
                this.weatherRepeatInterval = Long.parseLong(((Node) weatherRepeatIntervalTextList.item(0)).getNodeValue().trim());
                System.out.println("weatherRepeatInterval : " + this.weatherRepeatInterval + "\n");
                NodeList chargeControllerInterfaceList = settingsElement.getElementsByTagName("chargeControllerInterface");
                Element chargeControllerInterfaceElement = (Element) chargeControllerInterfaceList.item(0);
                NodeList chargeControllerInterfaceTextList = chargeControllerInterfaceElement.getChildNodes();
                this.chargeControllerInterface = ((Node) chargeControllerInterfaceTextList.item(0)).getNodeValue().trim();
                System.out.println("chargeControllerInterface : " + this.chargeControllerInterface + "\n");
                NodeList localDb4oServerList = settingsElement.getElementsByTagName("localDb4oServer");
                Element localDb4oServerElement = (Element) localDb4oServerList.item(0);
                NodeList localDb4oServerTextList = localDb4oServerElement.getChildNodes();
                this.localDb4oServer = ((Node) localDb4oServerTextList.item(0)).getNodeValue().trim();
                System.out.println("localDb4oServer : " + this.localDb4oServer + "\n");
                NodeList snFtpUserList = settingsElement.getElementsByTagName("snFtpUser");
                Element snFtpUserElement = (Element) snFtpUserList.item(0);
                NodeList snFtpUserTextList = snFtpUserElement.getChildNodes();
                this.snFtpUser = ((Node) snFtpUserTextList.item(0)).getNodeValue().trim();
                System.out.println("snFtpUser : " + this.snFtpUser + "\n");
                NodeList snFtpPasswordList = settingsElement.getElementsByTagName("snFtpPassword");
                Element snFtpPasswordElement = (Element) snFtpPasswordList.item(0);
                NodeList snFtpPasswordTextList = snFtpPasswordElement.getChildNodes();
                this.snFtpPassword = ((Node) snFtpPasswordTextList.item(0)).getNodeValue().trim();
                System.out.println("snFtpPassword : " + this.snFtpPassword + "\n");
                NodeList socketDataSourceList = settingsElement.getElementsByTagName("socketDataSource");
                Element socketDataSourceElement = (Element) socketDataSourceList.item(0);
                NodeList socketDataSourceTextList = socketDataSourceElement.getChildNodes();
                this.socketDataSource = ((Node) socketDataSourceTextList.item(0)).getNodeValue().trim();
                System.out.println("socketDataSource : " + this.socketDataSource + "\n");
                NodeList socketDataPortList = settingsElement.getElementsByTagName("socketDataPort");
                Element socketDataPortElement = (Element) socketDataPortList.item(0);
                NodeList socketDataPortTextList = socketDataPortElement.getChildNodes();
                this.socketDataPort = Integer.parseInt(((Node) socketDataPortTextList.item(0)).getNodeValue().trim());
                System.out.println("socketDataPort : " + this.socketDataPort + "\n");
                NodeList mx60AddressList = settingsElement.getElementsByTagName("mx60Address");
                Element mx60AddressElement = (Element) mx60AddressList.item(0);
                NodeList mx60AddressTextList = mx60AddressElement.getChildNodes();
                this.mx60Address = ((Node) mx60AddressTextList.item(0)).getNodeValue().trim();
                System.out.println("mx60Address : " + this.mx60Address + "\n");
                NodeList millisecondsBetweenDataSweepsList = settingsElement.getElementsByTagName("millisecondsBetweenDataSweeps");
                Element millisecondsBetweenDataSweepsElement = (Element) millisecondsBetweenDataSweepsList.item(0);
                NodeList millisecondsBetweenDataSweepsTextList = millisecondsBetweenDataSweepsElement.getChildNodes();
                this.millisecondsBetweenDataSweeps = ((Node) millisecondsBetweenDataSweepsTextList.item(0)).getNodeValue().trim();
                System.out.println("millisecondsBetweenDataSweeps : " + this.millisecondsBetweenDataSweeps + "\n");
                NodeList twoDigitHourForDataSweepEventList = settingsElement.getElementsByTagName("twoDigitHourForDataSweepEvent");
                Element twoDigitHourForDataSweepEventElement = (Element) twoDigitHourForDataSweepEventList.item(0);
                NodeList twoDigitHourForDataSweepEventTextList = twoDigitHourForDataSweepEventElement.getChildNodes();
                this.twoDigitHourForDataSweepEvent = ((Node) twoDigitHourForDataSweepEventTextList.item(0)).getNodeValue().trim();
                System.out.println("twoDigitHourForDataSweepEvent : " + this.twoDigitHourForDataSweepEvent + "\n");
                NodeList twoDigitMinutesForDataSweepEventList = settingsElement.getElementsByTagName("twoDigitMinutesForDataSweepEvent");
                Element twoDigitMinutesForDataSweepEventElement = (Element) twoDigitMinutesForDataSweepEventList.item(0);
                NodeList twoDigitMinutesForDataSweepEventTextList = twoDigitMinutesForDataSweepEventElement.getChildNodes();
                this.twoDigitMinutesForDataSweepEvent = ((Node) twoDigitMinutesForDataSweepEventTextList.item(0)).getNodeValue().trim();
                System.out.println("twoDigitMinutesForDataSweepEvent : " + this.twoDigitMinutesForDataSweepEvent + "\n");
                NodeList modbusReceiveTimeoutList = settingsElement.getElementsByTagName("modbusReceiveTimeout");
                Element modbusReceiveTimeoutElement = (Element) modbusReceiveTimeoutList.item(0);
                NodeList modbusReceiveTimeoutTextList = modbusReceiveTimeoutElement.getChildNodes();
                this.modbusReceiveTimeout = Integer.parseInt(((Node) modbusReceiveTimeoutTextList.item(0)).getNodeValue().trim());
                NodeList samplesPerSnapshotList = settingsElement.getElementsByTagName("samplesPerSnapshot");
                Element samplesPerSnapshotElement = (Element) samplesPerSnapshotList.item(0);
                NodeList samplesPerSnapshotTextList = samplesPerSnapshotElement.getChildNodes();
                this.samplesPerSnapshot = Integer.parseInt(((Node) samplesPerSnapshotTextList.item(0)).getNodeValue().trim());
                NodeList nodeIdList = settingsElement.getElementsByTagName("nodeId");
                Element nodeIdElement = (Element) nodeIdList.item(0);
                NodeList nodeIdTextList = nodeIdElement.getChildNodes();
                this.nodeId = Integer.parseInt(((Node) nodeIdTextList.item(0)).getNodeValue().trim());
                NodeList consumptionANodeIdList = settingsElement.getElementsByTagName("consumptionANodeId");
                Element consumptionANodeIdElement = (Element) consumptionANodeIdList.item(0);
                NodeList consumptionANodeIdTextList = consumptionANodeIdElement.getChildNodes();
                this.consumptionANodeId = Integer.parseInt(((Node) consumptionANodeIdTextList.item(0)).getNodeValue().trim());
                System.out.println("consumptionANodeId : " + this.consumptionANodeId + "\n");
                NodeList consumptionBNodeIdList = settingsElement.getElementsByTagName("consumptionBNodeId");
                Element consumptionBNodeIdElement = (Element) consumptionBNodeIdList.item(0);
                NodeList consumptionBNodeIdTextList = consumptionBNodeIdElement.getChildNodes();
                this.consumptionBNodeId = Integer.parseInt(((Node) consumptionBNodeIdTextList.item(0)).getNodeValue().trim());
                System.out.println("consumptionBNodeId : " + this.consumptionBNodeId + "\n");
                NodeList consumptionAHexIdList = settingsElement.getElementsByTagName("consumptionAHexId");
                Element consumptionAHexIdElement = (Element) consumptionAHexIdList.item(0);
                NodeList consumptionAHexIdTextList = consumptionAHexIdElement.getChildNodes();
                this.consumptionAHexId = Integer.parseInt(((Node) consumptionAHexIdTextList.item(0)).getNodeValue().trim());
                System.out.println("consumptionAHexId : " + this.consumptionAHexId + "\n");
                NodeList consumptionBHexIdList = settingsElement.getElementsByTagName("consumptionBHexId");
                Element consumptionBHexIdElement = (Element) consumptionBHexIdList.item(0);
                NodeList consumptionBHexIdTextList = consumptionBHexIdElement.getChildNodes();
                this.consumptionBHexId = Integer.parseInt(((Node) consumptionBHexIdTextList.item(0)).getNodeValue().trim());
                System.out.println("consumptionBHexId : " + this.consumptionBHexId + "\n");
                NodeList weatherDotComPartnerIdList = settingsElement.getElementsByTagName("weatherDotComPartnerId");
                Element weatherDotComPartnerIdElement = (Element) weatherDotComPartnerIdList.item(0);
                NodeList weatherDotComPartnerIdTextList = weatherDotComPartnerIdElement.getChildNodes();
                this.weatherDotComPartnerId = ((Node) weatherDotComPartnerIdTextList.item(0)).getNodeValue().trim();
                System.out.println("weatherDotComPartnerId : " + this.weatherDotComPartnerId + "\n");
                NodeList weatherDotComLicenseKeyList = settingsElement.getElementsByTagName("weatherDotComLicenseKey");
                Element weatherDotComLicenseKeyElement = (Element) weatherDotComLicenseKeyList.item(0);
                NodeList weatherDotComLicenseKeyTextList = weatherDotComLicenseKeyElement.getChildNodes();
                this.weatherDotComLicenseKey = ((Node) weatherDotComLicenseKeyTextList.item(0)).getNodeValue().trim();
                System.out.println("weatherDotComLicenseKey : " + this.weatherDotComLicenseKey + "\n");
                NodeList weatherReportingNodeIdList = settingsElement.getElementsByTagName("weatherReportingNodeId");
                Element weatherReportingNodeIdElement = (Element) weatherReportingNodeIdList.item(0);
                NodeList weatherReportingNodeIdTextList = weatherReportingNodeIdElement.getChildNodes();
                this.weatherReportingNodeId = Integer.parseInt(((Node) weatherReportingNodeIdTextList.item(0)).getNodeValue().trim());
                System.out.println("weatherReportingNodeId : " + this.weatherReportingNodeId + "\n");
                NodeList localDataFileList = settingsElement.getElementsByTagName("localDataFile");
                Element localDataFileElement = (Element) localDataFileList.item(0);
                NodeList localDataFileTextList = localDataFileElement.getChildNodes();
                this.localDataFile = ((Node) localDataFileTextList.item(0)).getNodeValue().trim();
                System.out.println("localDataFile : " + this.localDataFile + "\n");
                NodeList localConsumptionDataFileList = settingsElement.getElementsByTagName("localConsumptionDataFile");
                Element localConsumptionDataFileElement = (Element) localConsumptionDataFileList.item(0);
                NodeList localConsumptionDataFileTextList = localConsumptionDataFileElement.getChildNodes();
                this.localConsumptionDataFile = ((Node) localConsumptionDataFileTextList.item(0)).getNodeValue().trim();
                System.out.println("localConsumptionDataFile : " + this.localConsumptionDataFile + "\n");
                NodeList chargeControllerList = settingsElement.getElementsByTagName("chargeController");
                Element chargeControllerElement = (Element) chargeControllerList.item(0);
                NodeList chargeControllerTextList = chargeControllerElement.getChildNodes();
                this.chargeController = ((Node) chargeControllerTextList.item(0)).getNodeValue().trim();
                NodeList dataReadOuterWindowInMillisecondsList = settingsElement.getElementsByTagName("dataReadOuterWindowInMilliseconds");
                Element dataReadOuterWindowInMillisecondsElement = (Element) dataReadOuterWindowInMillisecondsList.item(0);
                NodeList dataReadOuterWindowInMillisecondsTextList = dataReadOuterWindowInMillisecondsElement.getChildNodes();
                this.dataReadOuterWindowInMilliseconds = Integer.parseInt(((Node) dataReadOuterWindowInMillisecondsTextList.item(0)).getNodeValue().trim());
                NodeList dataReadInnerWindowInMillisecondsList = settingsElement.getElementsByTagName("dataReadInnerWindowInMilliseconds");
                Element dataReadInnerWindowInMillisecondsElement = (Element) dataReadInnerWindowInMillisecondsList.item(0);
                NodeList dataReadInnerWindowInMillisecondsTextList = dataReadInnerWindowInMillisecondsElement.getChildNodes();
                this.dataReadInnerWindowInMilliseconds = Integer.parseInt(((Node) dataReadInnerWindowInMillisecondsTextList.item(0)).getNodeValue().trim());
                NodeList consumptionReadIntervalInMillisecondsList = settingsElement.getElementsByTagName("consumptionReadIntervalInMilliseconds");
                Element consumptionReadIntervalInMillisecondsElement = (Element) consumptionReadIntervalInMillisecondsList.item(0);
                NodeList consumptionReadIntervalInMillisecondsTextList = consumptionReadIntervalInMillisecondsElement.getChildNodes();
                this.consumptionReadIntervalInMilliseconds = Integer.parseInt(((Node) consumptionReadIntervalInMillisecondsTextList.item(0)).getNodeValue().trim());
                System.out.println("consumptionReadIntervalInMilliseconds : " + this.consumptionReadIntervalInMilliseconds + "\n");
                NodeList pvSystemVoltageList = settingsElement.getElementsByTagName("pvSystemVoltage");
                Element pvSystemVoltageElement = (Element) pvSystemVoltageList.item(0);
                NodeList pvSystemVoltageTextList = pvSystemVoltageElement.getChildNodes();
                this.pvSystemVoltage = Integer.parseInt(((Node) pvSystemVoltageTextList.item(0)).getNodeValue().trim());
                NodeList webServiceEndpointList = settingsElement.getElementsByTagName("webServiceEndpoint");
                Element webServiceEndpointElement = (Element) webServiceEndpointList.item(0);
                NodeList webServiceEndpointTextList = webServiceEndpointElement.getChildNodes();
                this.webServiceEndpoint = ((Node) webServiceEndpointTextList.item(0)).getNodeValue().trim();
                NodeList consumptionWebServiceEndpointList = settingsElement.getElementsByTagName("consumptionWebServiceEndpoint");
                Element consumptionWebServiceEndpointElement = (Element) consumptionWebServiceEndpointList.item(0);
                NodeList consumptionWebServiceEndpointTextList = consumptionWebServiceEndpointElement.getChildNodes();
                this.consumptionWebServiceEndpoint = ((Node) consumptionWebServiceEndpointTextList.item(0)).getNodeValue().trim();
                System.out.println("consumptionWebServiceEndpoint : " + this.consumptionWebServiceEndpoint + "\n");
                NodeList webServiceMethodList = settingsElement.getElementsByTagName("webServiceMethod");
                Element webServiceMethodElement = (Element) webServiceMethodList.item(0);
                NodeList webServiceMethodTextList = webServiceMethodElement.getChildNodes();
                this.webServiceMethod = ((Node) webServiceMethodTextList.item(0)).getNodeValue().trim();
                NodeList consumptionWebServiceMethodList = settingsElement.getElementsByTagName("consumptionWebServiceMethod");
                Element consumptionWebServiceMethodElement = (Element) consumptionWebServiceMethodList.item(0);
                NodeList consumptionWebServiceMethodTextList = consumptionWebServiceMethodElement.getChildNodes();
                this.consumptionWebServiceMethod = ((Node) consumptionWebServiceMethodTextList.item(0)).getNodeValue().trim();
                System.out.println("consumptionWebServiceMethod : " + this.consumptionWebServiceMethod + "\n");
                NodeList weatherWebServiceEndpointList = settingsElement.getElementsByTagName("weatherWebServiceEndpoint");
                Element weatherWebServiceEndpointElement = (Element) weatherWebServiceEndpointList.item(0);
                NodeList weatherWebServiceEndpointTextList = weatherWebServiceEndpointElement.getChildNodes();
                this.weatherWebServiceEndpoint = ((Node) weatherWebServiceEndpointTextList.item(0)).getNodeValue().trim();
                NodeList weatherWebServiceMethodList = settingsElement.getElementsByTagName("weatherWebServiceMethod");
                Element weatherWebServiceMethodElement = (Element) weatherWebServiceMethodList.item(0);
                NodeList weatherWebServiceMethodTextList = weatherWebServiceMethodElement.getChildNodes();
                this.weatherWebServiceMethod = ((Node) weatherWebServiceMethodTextList.item(0)).getNodeValue().trim();
                NodeList systemStorageCapacityList = settingsElement.getElementsByTagName("systemStorageCapacity");
                Element systemStorageCapacityElement = (Element) systemStorageCapacityList.item(0);
                NodeList systemStorageCapacityTextList = systemStorageCapacityElement.getChildNodes();
                this.systemStorageCapacity = Integer.parseInt(((Node) systemStorageCapacityTextList.item(0)).getNodeValue().trim());
                NodeList dropBatteryCapacityLimitList = settingsElement.getElementsByTagName("dropBatteryCapacityLimit");
                Element dropBatteryCapacityLimitElement = (Element) dropBatteryCapacityLimitList.item(0);
                NodeList dropBatteryCapacityLimitTextList = dropBatteryCapacityLimitElement.getChildNodes();
                this.dropBatteryCapacityLimit = Integer.parseInt(((Node) dropBatteryCapacityLimitTextList.item(0)).getNodeValue().trim());
                NodeList chargeControllerSerialPortList = settingsElement.getElementsByTagName("chargeControllerSerialPort");
                Element chargeControllerSerialPortElement = (Element) chargeControllerSerialPortList.item(0);
                NodeList chargeControllerSerialPortTextList = chargeControllerSerialPortElement.getChildNodes();
                this.chargeControllerSerialPort = ((Node) chargeControllerSerialPortTextList.item(0)).getNodeValue().trim();
                System.out.println("chargeControllerSerialPort : " + this.chargeControllerSerialPort + "\n");
                NodeList batteryComputerSerialPortList = settingsElement.getElementsByTagName("batteryComputerSerialPort");
                Element batteryComputerSerialPortElement = (Element) batteryComputerSerialPortList.item(0);
                NodeList batteryComputerSerialPortTextList = batteryComputerSerialPortElement.getChildNodes();
                this.batteryComputerSerialPort = ((Node) batteryComputerSerialPortTextList.item(0)).getNodeValue().trim();
                NodeList sourceSwitchSerialPortList = settingsElement.getElementsByTagName("sourceSwitchSerialPort");
                Element sourceSwitchSerialPortElement = (Element) sourceSwitchSerialPortList.item(0);
                NodeList sourceSwitchSerialPortTextList = sourceSwitchSerialPortElement.getChildNodes();
                this.sourceSwitchSerialPort = ((Node) sourceSwitchSerialPortTextList.item(0)).getNodeValue().trim();
                NodeList consumptionReceiverSerialPortList = settingsElement.getElementsByTagName("consumptionReceiverSerialPort");
                Element consumptionReceiverSerialPortElement = (Element) consumptionReceiverSerialPortList.item(0);
                NodeList consumptionReceiverSerialPortTextList = consumptionReceiverSerialPortElement.getChildNodes();
                this.consumptionReceiverSerialPort = ((Node) consumptionReceiverSerialPortTextList.item(0)).getNodeValue().trim();
                System.out.println("consumptionReceiverSerialPort : " + this.consumptionReceiverSerialPort + "\n");
                NodeList consumptionMonitorList = settingsElement.getElementsByTagName("consumptionMonitor");
                Element consumptionMonitorElement = (Element) consumptionMonitorList.item(0);
                NodeList consumptionMonitorTextList = consumptionMonitorElement.getChildNodes();
                this.consumptionMonitor = ((Node) consumptionMonitorTextList.item(0)).getNodeValue().trim();
                System.out.println("consumptionMonitor : " + this.consumptionMonitor + "\n");
            } else {
                System.out.println("not an element Node " + "\n");
            }
            System.out.println("done with settings" + "\n");
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public int startGenerationDb() {
        int success = 0;
        try {
            this.localGenerationDbServer = Db4o.openServer(this.localDataFile, 5000);
            this.localGenerationDbServer.grantAccess("solar", "solar");
        } catch (Exception e) {
            System.out.println("error starting localGenerationDbServer " + e.toString() + "\n");
        }
        return success;
    }

    public int startConsumptionDb() {
        int success = 0;
        try {
            this.localConsumptionDbServer = Db4o.openServer(this.localConsumptionDataFile, 5001);
            this.localConsumptionDbServer.grantAccess("solar", "solar");
        } catch (Exception e) {
            System.out.println("error starting localConsumptionDbServer " + e.toString() + "\n");
        }
        return success;
    }
}
