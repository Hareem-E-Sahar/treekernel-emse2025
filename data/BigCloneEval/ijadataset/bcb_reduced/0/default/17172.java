import java.util.ArrayList;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.ObjectServer;
import com.db4o.query.Constraint;
import com.db4o.query.Query;
import java.util.Date;
import javax.xml.namespace.QName;
import java.net.*;
import java.io.*;

public abstract class NetworkUtils {

    public static String readLine(InputStream input) {
        String result = "";
        int c;
        try {
            while ((c = input.read()) != '\n') if (c == -1) return null;
            result += (char) c;
            while ((c = input.read()) != '\r') {
                if (c == -1) return null; else result += (char) c;
            }
            result += "\r";
        } catch (IOException x) {
            return null;
        }
        return result;
    }

    public static WeatherDatum getWeatherDotComInfo(SolarNodeSettings theSettings, String LocationIdentifier, WeatherDatum theWeather) {
        String inputLine = "";
        String[] finalString = new String[2];
        finalString[0] = "ok";
        finalString[1] = "";
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse("http://xoap.weather.com/weather/local/" + LocationIdentifier + "?cc=*&dayf=5&link=xoap&prod=xoap&par=" + theSettings.weatherDotComPartnerId + "&key=" + theSettings.weatherDotComLicenseKey);
            if (theSettings.debug) {
            }
            doc.getDocumentElement().normalize();
            NodeList locationList = doc.getElementsByTagName("loc");
            Node locationNode = locationList.item(0);
            if (locationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element locationElement = (Element) locationNode;
                NodeList sunriseList = locationElement.getElementsByTagName("sunr");
                Element sunriseElement = (Element) sunriseList.item(0);
                NodeList sunriseTextList = sunriseElement.getChildNodes();
                theWeather.sunrise = ((Node) sunriseTextList.item(0)).getNodeValue().toLowerCase().trim();
                NodeList sunsetList = locationElement.getElementsByTagName("suns");
                Element sunsetElement = (Element) sunsetList.item(0);
                NodeList sunsetTextList = sunsetElement.getChildNodes();
                theWeather.sunset = ((Node) sunsetTextList.item(0)).getNodeValue().toLowerCase().trim();
                NodeList latitudeList = locationElement.getElementsByTagName("lat");
                Element latitudeElement = (Element) latitudeList.item(0);
                NodeList latitudeTextList = latitudeElement.getChildNodes();
                double latitudeValue = Double.parseDouble(((Node) latitudeTextList.item(0)).getNodeValue().toLowerCase().trim());
                NodeList longitudeList = locationElement.getElementsByTagName("lat");
                Element longitudeElement = (Element) longitudeList.item(0);
                NodeList longitudeTextList = longitudeElement.getChildNodes();
                double longitudeValue = Double.parseDouble(((Node) longitudeTextList.item(0)).getNodeValue().toLowerCase().trim());
            }
            if (theSettings.debug) {
            }
            NodeList currentWeatherList = doc.getElementsByTagName("cc");
            if (theSettings.debug) {
            }
            Node currentWeatherNode = currentWeatherList.item(0);
            if (theSettings.debug) {
            }
            if (currentWeatherNode.getNodeType() == Node.ELEMENT_NODE) {
                Element settingsElement = (Element) currentWeatherNode;
                NodeList skyConditionsList = settingsElement.getElementsByTagName("t");
                Element skyConditionsElement = (Element) skyConditionsList.item(0);
                NodeList skyConditionsTextList = skyConditionsElement.getChildNodes();
                theWeather.skyConditions = ((Node) skyConditionsTextList.item(0)).getNodeValue().toLowerCase().trim();
                NodeList temperatureCelciusList = settingsElement.getElementsByTagName("tmp");
                Element temperatureCelciusElement = (Element) temperatureCelciusList.item(0);
                NodeList temperatureCelciusTextList = temperatureCelciusElement.getChildNodes();
                double temperatureFarhenheit = Double.parseDouble(((Node) temperatureCelciusTextList.item(0)).getNodeValue().toLowerCase().trim());
                theWeather.temperatureCelcius = ((5.0 / 9.0) * (temperatureFarhenheit - 32));
                NodeList humidityList = settingsElement.getElementsByTagName("hmid");
                Element humidityElement = (Element) humidityList.item(0);
                NodeList humidityTextList = humidityElement.getChildNodes();
                theWeather.humidity = Double.parseDouble(((Node) humidityTextList.item(0)).getNodeValue().toLowerCase().trim());
                NodeList visibilityList = settingsElement.getElementsByTagName("tmp");
                Element visibilityElement = (Element) visibilityList.item(0);
                NodeList visibilityTextList = visibilityElement.getChildNodes();
                theWeather.visibility = Double.parseDouble(((Node) visibilityTextList.item(0)).getNodeValue().toLowerCase().trim());
                NodeList dewpList = settingsElement.getElementsByTagName("tmp");
                Element dewpElement = (Element) dewpList.item(0);
                NodeList dewpTextList = dewpElement.getChildNodes();
                theWeather.dewPoint = Double.parseDouble(((Node) dewpTextList.item(0)).getNodeValue().toLowerCase().trim());
                NodeList barList = settingsElement.getElementsByTagName("bar");
                Element barElement = (Element) barList.item(0);
                NodeList barometricPressureList = barElement.getElementsByTagName("r");
                Element barometricPressureElement = (Element) barometricPressureList.item(0);
                NodeList barometricPressureTextList = barometricPressureElement.getChildNodes();
                theWeather.barometricPressure = Double.parseDouble(((Node) barometricPressureTextList.item(0)).getNodeValue().toLowerCase().trim());
                NodeList barometerDeltaList = barElement.getElementsByTagName("d");
                Element barometerDeltaElement = (Element) barometerDeltaList.item(0);
                NodeList barometerDeltaTextList = barometerDeltaElement.getChildNodes();
                theWeather.barometerDelta = ((Node) barometerDeltaTextList.item(0)).getNodeValue().toLowerCase().trim();
                NodeList uvList = settingsElement.getElementsByTagName("uv");
                Element uvElement = (Element) uvList.item(0);
                NodeList uvIndexList = uvElement.getElementsByTagName("i");
                Element uvIndexElement = (Element) uvIndexList.item(0);
                NodeList uvIndexTextList = uvIndexElement.getChildNodes();
                theWeather.uvIndex = Integer.parseInt(((Node) uvIndexTextList.item(0)).getNodeValue().toLowerCase().trim());
                if (theSettings.debug) {
                }
            }
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return theWeather;
    }

    public static ArrayList getNodes(SolarNodeSettings theSettings, String mode) {
        String inputLine = "";
        String[] finalString = new String[2];
        finalString[0] = "ok";
        finalString[1] = "";
        ArrayList nodeSet = new ArrayList();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse("http://www.solarnetwork.net/getNodes3.php");
            if (theSettings.debug) {
            }
            doc.getDocumentElement().normalize();
            if (theSettings.debug) {
                System.out.println("Root element of the doc should be nodeInfo: " + doc.getDocumentElement().getNodeName());
            }
            NodeList nodesList = doc.getElementsByTagName("nodes");
            if (theSettings.debug) {
                System.out.println("nodesList getLength:" + nodesList.getLength() + "\n");
            }
            Node nodesNode = nodesList.item(0);
            if (theSettings.debug) {
            }
            if (nodesNode.getNodeType() == Node.ELEMENT_NODE) {
                Element nodesElement = (Element) nodesNode;
                NodeList solarNodeList = nodesElement.getElementsByTagName("node");
                if (theSettings.debug) {
                    System.out.println("solarNodeList getLength:" + solarNodeList.getLength() + "\n");
                }
                int i = 0;
                for (i = 0; i < solarNodeList.getLength(); i++) {
                    SolarNode thisNode = new SolarNode();
                    Element thisNodeElement = (Element) solarNodeList.item(i);
                    NodeList thisNodeIdList = thisNodeElement.getElementsByTagName("id");
                    Element thisNodeIdElement = (Element) thisNodeIdList.item(0);
                    NodeList thisNodeIdTextList = thisNodeIdElement.getChildNodes();
                    thisNode.id = Integer.parseInt(((Node) thisNodeIdTextList.item(0)).getNodeValue().toLowerCase().trim());
                    NodeList thisNodeWcIdentifierList = thisNodeElement.getElementsByTagName("wcIdentifier");
                    Element thisNodeWcIdentifierElement = (Element) thisNodeWcIdentifierList.item(0);
                    NodeList thisNodeWcIdentifierTextList = thisNodeWcIdentifierElement.getChildNodes();
                    thisNode.weatherDotComLocationIdentifier = ((Node) thisNodeWcIdentifierTextList.item(0)).getNodeValue().toLowerCase().trim();
                    nodeSet.add(thisNode);
                }
                if (theSettings.debug) {
                }
            }
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return nodeSet;
    }

    public static String[] getWeatherData(SolarNodeSettings theSettings) {
        String inputLine = "";
        String[] finalString = new String[2];
        finalString[0] = "ok";
        finalString[1] = "";
        Socket socket = null;
        InputStream input = null;
        try {
            URL theUrl = new URL("http://www.webservicex.net/globalweather.asmx/GetWeather?CityName=melbourne&CountryName=australia");
            URLConnection yc = theUrl.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            if (theSettings.debug) {
            }
            while ((((inputLine = in.readLine()) != null)) & (finalString[1].length() < 600)) {
                finalString[1] += inputLine + "\n";
            }
        } catch (Exception e) {
            if (theSettings.debug) {
                System.out.print("error in URL read:" + e.getMessage() + "\n");
            }
        }
        if (theSettings.debug) {
            System.out.println(" finalString[0]:" + finalString[0] + "\n");
            System.out.println(" finalString[1]:" + finalString[1] + "\n");
        }
        return finalString;
    }

    public static String[] getHttpData(SolarNodeSettings theSettings) {
        String inputLine = "";
        String[] finalString = new String[2];
        finalString[0] = "ok";
        finalString[1] = "";
        Socket socket = null;
        InputStream input = null;
        try {
            if (theSettings.debug) {
                System.err.println("about to try source: " + theSettings.socketDataSource);
                System.err.println("about to try port: " + theSettings.socketDataPort);
            }
            try {
                System.err.println("before socket \n");
                socket = new Socket(theSettings.socketDataSource, theSettings.socketDataPort);
                System.err.println("after socket \n");
                System.err.println("about to try port: " + theSettings.socketDataPort);
                System.err.println("before getinputstream \n");
                input = socket.getInputStream();
                System.err.println("after getinputstream \n");
                System.err.println("about to try port: " + theSettings.socketDataPort);
            } catch (UnknownHostException e) {
                finalString[0] = "error";
                finalString[1] = "Unknown host: " + theSettings.socketDataSource;
                if (theSettings.debug) {
                    System.err.println("Unknown host: " + theSettings.socketDataSource);
                }
            } catch (IOException e) {
                finalString[0] = "error";
                finalString[1] = "Error opening socket:" + theSettings.socketDataPort;
                if (theSettings.debug) {
                    System.err.println("Error opening socket:" + theSettings.socketDataPort);
                }
            }
            while (((inputLine = readLine(input)) != null) & (finalString[1].length() < 600)) {
                finalString[1] += inputLine + "\n";
            }
        } catch (Exception e) {
            if (theSettings.debug) {
                System.out.print("error in URL read:" + e.getMessage() + "\n");
            }
        }
        if (theSettings.debug) {
            System.out.println(" finalString[0]:" + finalString[0] + "\n");
            System.out.println(" finalString[1]:" + finalString[1] + "\n");
        }
        return finalString;
    }

    public static Call setupPowerCall(SolarNodeSettings theSettings, Call call) {
        char cf = 10;
        char lf = 13;
        try {
            Service service1 = new Service();
            if (theSettings.debug) {
                System.out.println("init service" + cf + lf);
            }
            if (theSettings.debug) {
                System.out.println("created call" + cf + lf);
            }
            call.setTargetEndpointAddress(new java.net.URL(theSettings.webServiceEndpoint));
            if (theSettings.debug) {
                System.out.println("set target" + cf + lf);
            }
            call.setOperationName(theSettings.webServiceMethod);
            if (theSettings.debug) {
                System.out.println("set operation to:" + theSettings.webServiceMethod + cf + lf);
            }
        } catch (Exception e) {
            if (theSettings.debug) {
                System.out.print("error setupPowerCall:" + e.getMessage());
            }
        }
        return call;
    }

    public static Call setupGetNodesCall(SolarNodeSettings theSettings, Call call) {
        char cf = 10;
        char lf = 13;
        try {
            Service service1 = new Service();
            if (theSettings.debug) {
                System.out.println("init service" + cf + lf);
            }
            if (theSettings.debug) {
                System.out.println("created call" + cf + lf);
            }
            call.setTargetEndpointAddress(new java.net.URL("http://www.solarnetwork.net/getNodes2.php"));
            if (theSettings.debug) {
                System.out.println("set target" + cf + lf);
            }
            call.setOperationName("getSolarNodes");
            if (theSettings.debug) {
                System.out.println("set operation to:" + theSettings.webServiceMethod + cf + lf);
            }
        } catch (Exception e) {
            if (theSettings.debug) {
                System.out.print("error setupPowerCall:" + e.getMessage());
            }
        }
        return call;
    }

    public static Call setupConsumptionCall(SolarNodeSettings theSettings, Call call) {
        char cf = 10;
        char lf = 13;
        try {
            Service service1 = new Service();
            if (theSettings.debug) {
            }
            if (theSettings.debug) {
            }
            call.setTargetEndpointAddress(new java.net.URL(theSettings.consumptionWebServiceEndpoint));
            if (theSettings.debug) {
            }
            call.setOperationName(theSettings.consumptionWebServiceMethod);
            if (theSettings.debug) {
            }
        } catch (Exception e) {
            if (theSettings.debug) {
                System.out.print("error setupPowerCall:" + e.getMessage());
            }
        }
        return call;
    }

    public static Call setupWeatherCall(SolarNodeSettings theSettings, Call call) {
        char cf = 10;
        char lf = 13;
        try {
            Service service1 = new Service();
            if (theSettings.debug) {
                System.out.println("init service" + cf + lf);
            }
            if (theSettings.debug) {
                System.out.println("created call" + cf + lf);
            }
            call.setTargetEndpointAddress(new java.net.URL(theSettings.weatherWebServiceEndpoint));
            if (theSettings.debug) {
                System.out.println("set target" + cf + lf);
            }
            call.setOperationName(theSettings.weatherWebServiceMethod);
            if (theSettings.debug) {
                System.out.println("set operation to:" + theSettings.weatherWebServiceMethod + cf + lf);
            }
        } catch (Exception e) {
            if (theSettings.debug) {
                System.out.print("error setupWeatherCall:" + e.getMessage());
            }
        }
        return call;
    }

    public static Call setupGetWeatherCall(SolarNodeSettings theSettings, Call call) {
        char cf = 10;
        char lf = 13;
        try {
            Service service1 = new Service();
            if (theSettings.debug) {
                System.out.println("init service" + cf + lf);
            }
            if (theSettings.debug) {
                System.out.println("created call" + cf + lf);
            }
            call.setTargetEndpointAddress(new java.net.URL("http://www.webservicex.net/globalweather.asmx"));
            call.setUseSOAPAction(true);
            call.setSOAPActionURI("http://www.webserviceX.NET/GetCitiesByCountry");
            if (theSettings.debug) {
                System.out.println("set target to  http://www.webservicex.net/globalweather.asmx \n");
            }
            call.setOperation(new QName("http://www.webserviceX.NET"), "GetCitiesByCountry");
            if (theSettings.debug) {
                System.out.println("set operation to: http://www.webserviceX.NET/GetWeather" + "\n");
            }
        } catch (Exception e) {
            if (theSettings.debug) {
                System.out.print("error setupWeatherCall:" + e.getMessage());
            }
        }
        return call;
    }

    public static int makePowerCall(SolarNodeSettings theSettings, PowerDatum thePower) {
        char cf = 10;
        char lf = 13;
        String ret = "";
        int registeredPowerDatumId = 0;
        try {
            Service service1 = new Service();
            Call call = (Call) service1.createCall();
            if (theSettings.debug) {
                System.out.println("in makePowerCall to " + theSettings.webServiceEndpoint + cf + lf);
            }
            try {
                if (theSettings.debug) {
                    System.out.println("before setup power call " + theSettings.webServiceEndpoint + cf + lf);
                }
                call = NetworkUtils.setupPowerCall(theSettings, call);
                if (theSettings.debug) {
                    System.out.println("after setup power call " + theSettings.webServiceEndpoint + cf + lf);
                }
                call.addParameter("nodeId", XMLType.XSD_INT, ParameterMode.IN);
                call.addParameter("pvVolts", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("pvAmps", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("batteryVolts", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("dcAmps", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("ampHoursToday", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("kiloWattHoursToday", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("whenLogged", XMLType.XSD_DATETIME, ParameterMode.IN);
                call.addParameter("errorNote", XMLType.XSD_STRING, ParameterMode.IN);
                call.setReturnType(XMLType.XSD_STRING);
                if (theSettings.debug) {
                    System.out.println("set params" + cf + lf);
                }
                Object[] powerStrings = { thePower.nodeId, Double.toString(thePower.pvVolts), Double.toString(thePower.pvAmps), Double.toString(thePower.batteryVolts), Double.toString(thePower.dcAmps), Double.toString(thePower.ampHoursToday), Double.toString(thePower.kiloWattHoursToday), thePower.whenLogged, thePower.errorNote };
                if (theSettings.debug) {
                    System.out.println("created data strings" + cf + lf);
                    System.out.println("about to make call nodeId:" + thePower.nodeId + cf);
                    System.out.println("about to make call pvVolts:" + thePower.pvVolts + cf);
                    System.out.println("about to make call pvAmps:" + thePower.pvAmps + cf);
                    System.out.println("about to make call batteryVolts:" + thePower.batteryVolts + cf);
                    System.out.println("about to make call dcAmps:" + thePower.dcAmps + cf);
                    System.out.println("about to make call ampHoursToday:" + thePower.ampHoursToday + cf);
                    System.out.println("about to make call kiloWattHoursToday:" + thePower.kiloWattHoursToday + cf);
                    System.out.println("about to make call notes:" + thePower.errorNote + cf);
                    System.out.println("about to make call whenLogged:" + thePower.whenLogged.toString() + cf);
                }
                ret = (String) call.invoke(powerStrings);
                if (theSettings.debug) {
                    System.out.println("ret return value as string:" + ret + "\n");
                }
                if (theSettings.debug) {
                    System.out.println("after call:" + ret);
                }
            } catch (Exception e) {
                registeredPowerDatumId = -1;
                if (theSettings.debug) {
                    System.out.print("error makeVoltageCall:" + e.getMessage());
                }
            }
            try {
                registeredPowerDatumId = Integer.parseInt(ret);
            } catch (Exception e2) {
                registeredPowerDatumId = -2;
                if (theSettings.debug) {
                    System.out.print("we got no return value from the webservice call:" + e2.getMessage());
                }
            }
        } catch (Exception e) {
            registeredPowerDatumId = -3;
            if (theSettings.debug) {
                System.out.print("error setupPowerCall:" + e.getMessage());
            }
        }
        return registeredPowerDatumId;
    }

    public static String getNodesCall(SolarNodeSettings theSettings, int nodeId) {
        char cf = 10;
        char lf = 13;
        Object[] ret;
        int registeredPowerDatumId = 0;
        try {
            Service service1 = new Service();
            Call call = (Call) service1.createCall();
            if (theSettings.debug) {
                System.out.println("in makePowerCall to " + theSettings.webServiceEndpoint + cf + lf);
            }
            try {
                if (theSettings.debug) {
                    System.out.println("before setup power call " + theSettings.webServiceEndpoint + cf + lf);
                }
                call = NetworkUtils.setupGetNodesCall(theSettings, call);
                if (theSettings.debug) {
                    System.out.println("after setup power call " + theSettings.webServiceEndpoint + cf + lf);
                }
                call.addParameter("nodeId", XMLType.XSD_INT, ParameterMode.IN);
                call.setReturnType(XMLType.XSD_ANY);
                if (theSettings.debug) {
                    System.out.println("set params" + cf + lf);
                }
                Object[] powerStrings = { nodeId };
                if (theSettings.debug) {
                    System.out.println("created data strings" + cf + lf);
                }
                ret = (Object[]) call.invoke(powerStrings);
                if (theSettings.debug) {
                    System.out.println("ret return value as string:" + ret + "\n");
                }
                if (theSettings.debug) {
                    System.out.println("after call:" + ret);
                }
            } catch (Exception e) {
                registeredPowerDatumId = -1;
                if (theSettings.debug) {
                    System.out.print("error makeVoltageCall:" + e.getMessage());
                }
            }
            try {
            } catch (Exception e2) {
                registeredPowerDatumId = -2;
                if (theSettings.debug) {
                    System.out.print("we got no return value from the webservice call:" + e2.getMessage());
                }
            }
        } catch (Exception e) {
            registeredPowerDatumId = -3;
            if (theSettings.debug) {
                System.out.print("error setupPowerCall:" + e.getMessage());
            }
        }
        return "ok";
    }

    public static int makeWeatherCall(SolarNodeSettings theSettings, WeatherDatum theWeather) {
        String ret = "";
        int registeredWeatherDatumId = 0;
        int i;
        try {
            Service service1 = new Service();
            if (theSettings.debug) {
                System.out.println("in makeWeatherCall to " + theSettings.weatherWebServiceEndpoint + "\n");
            }
            try {
                if (theSettings.debug) {
                    System.out.println("before makeWeatherCall " + theSettings.weatherWebServiceEndpoint + "\n");
                }
                Call call = (Call) service1.createCall();
                call = NetworkUtils.setupWeatherCall(theSettings, call);
                if (theSettings.debug) {
                    System.out.println("after setupWeatherCall " + theSettings.weatherWebServiceEndpoint + "\n");
                    System.out.println("theWeather.nodeId " + theWeather.nodeId + "\n");
                    System.out.println("theWeather.reportingNodeId " + theWeather.reportingNodeId + "\n");
                    System.out.println("theWeather.sourceId " + theWeather.sourceId + "\n");
                    System.out.println("theWeather.skyConditions:" + theWeather.skyConditions + "\n");
                    System.out.println("theWeather.temperatureCelcius:" + theWeather.temperatureCelcius + "\n");
                    System.out.println("theWeather.humidity:" + theWeather.humidity + "\n");
                    System.out.println("theWeather.visibility:" + theWeather.visibility + "\n");
                    System.out.println("theWeather.barometricPressure:" + theWeather.barometricPressure + "\n");
                    System.out.println("theWeather.barometerDelta:" + theWeather.barometerDelta + "\n");
                    System.out.println("theWeather.uvIndex:" + theWeather.uvIndex + "\n");
                    System.out.println("theWeather.whenLogged:" + theWeather.whenLogged + "\n");
                    System.out.println("theWeather.errorNote:" + theWeather.errorNote + "\n");
                }
                call.addParameter("nodeId", XMLType.XSD_INT, ParameterMode.IN);
                call.addParameter("reportingNodeId", XMLType.XSD_INT, ParameterMode.IN);
                call.addParameter("sourceId", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("skyConditions", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("temperatureCelcius", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("visibility", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("humidity", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("barometricPressure", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("barometerDelta", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("uvIndex", XMLType.XSD_INT, ParameterMode.IN);
                call.addParameter("sunrise", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("sunset", XMLType.XSD_STRING, ParameterMode.IN);
                call.addParameter("whenLogged", XMLType.XSD_DATETIME, ParameterMode.IN);
                call.addParameter("errorNote", XMLType.XSD_STRING, ParameterMode.IN);
                call.setReturnType(XMLType.XSD_STRING);
                if (theSettings.debug) {
                    System.out.println("set params" + "\n");
                }
                if (theSettings.debug) {
                }
                Object[] powerStrings = { theWeather.nodeId, theWeather.reportingNodeId, theWeather.sourceId, theWeather.skyConditions, Double.toString(theWeather.temperatureCelcius), Double.toString(theWeather.visibility), Double.toString(theWeather.humidity), Double.toString(theWeather.barometricPressure), theWeather.barometerDelta, Integer.toString(theWeather.uvIndex), theWeather.sunrise, theWeather.sunset, theWeather.whenLogged, theWeather.errorNote };
                if (theSettings.debug) {
                    System.out.println("created data strings" + "\n");
                    System.out.println("about to make call nodeId:" + theWeather.nodeId + "\n");
                    System.out.println("about to make call whenLogged:" + theWeather.whenLogged.toString() + "\n");
                    System.out.println("about to make call sunrise:" + theWeather.sunrise + "\n");
                    System.out.println("about to make call sunset:" + theWeather.sunset + "\n");
                    System.out.println("about to make call errorNote:" + theWeather.errorNote + "\n");
                }
                ret = (String) call.invoke(powerStrings);
                if (theSettings.debug) {
                    System.out.println("ret return value as string:" + ret + "\n");
                }
                if (theSettings.debug) {
                    System.out.println("after call:" + ret);
                }
            } catch (Exception e) {
                registeredWeatherDatumId = -1;
                if (theSettings.debug) {
                    System.out.print("error makeWeatherCall:" + e.getMessage());
                }
            }
            try {
                registeredWeatherDatumId = Integer.parseInt(ret);
            } catch (Exception e2) {
                registeredWeatherDatumId = -2;
                if (theSettings.debug) {
                    System.out.print("we got no return value from the makeWeatherCall webservice call:" + e2.getMessage());
                }
            }
        } catch (Exception e) {
            registeredWeatherDatumId = -3;
            if (theSettings.debug) {
                System.out.print("error makeWeatherCall:" + e.getMessage());
            }
        }
        return registeredWeatherDatumId;
    }

    public static WeatherDatum getWeatherCall(SolarNodeSettings theSettings, SolarNode theNode, WeatherDatum theWeather) {
        String ret = "";
        int registeredWeatherDatumId = 0;
        int i;
        try {
            Service service1 = new Service();
            if (theSettings.debug) {
                System.out.println("in getWeatherCall to  http://www.webserviceX.NET/GetWeather \n");
            }
            try {
                if (theSettings.debug) {
                    System.out.println("before getWeatherCall http://www.webserviceX.NET/GetWeather \n");
                }
                Call call = (Call) service1.createCall();
                call = NetworkUtils.setupGetWeatherCall(theSettings, call);
                if (theSettings.debug) {
                    System.out.println("after getWeatherCall http://www.webserviceX.NET/GetWeather \n");
                }
                call.addParameter(new QName("CountryName"), XMLType.XSD_STRING, ParameterMode.IN);
                call.setReturnType(XMLType.XSD_STRING);
                if (theSettings.debug) {
                    System.out.println("set params" + "\n");
                    System.out.println("getOperationName:" + call.getOperationName().toString() + "\n");
                    System.out.println("getSOAPActionURI:" + call.getSOAPActionURI() + "\n");
                }
                if (theSettings.debug) {
                }
                Object[] powerStrings = { "Australia" };
                if (theSettings.debug) {
                    System.out.println("created data strings" + "\n");
                    System.out.println("about to make call theNode.city:" + theNode.city + "\n");
                    System.out.println("about to make call theNode.country:" + theNode.country + "\n");
                }
                ret = (String) call.invoke(powerStrings);
                if (theSettings.debug) {
                    System.out.println("ret return value as string:" + ret + "\n");
                }
                if (theSettings.debug) {
                    System.out.println("after call:" + ret);
                }
            } catch (Exception e) {
                registeredWeatherDatumId = -1;
                if (theSettings.debug) {
                    System.out.print("error -1 getWeatherCall:" + e.getMessage() + "\n");
                }
            }
            try {
            } catch (Exception e2) {
                registeredWeatherDatumId = -2;
                if (theSettings.debug) {
                    System.out.print("error -2 we got no return value from the getWeatherCall webservice call:" + e2.getMessage());
                }
            }
        } catch (Exception e) {
            registeredWeatherDatumId = -3;
            if (theSettings.debug) {
                System.out.print("error -3 getWeatherCall:" + e.getMessage());
            }
        }
        return theWeather;
    }

    public static int makeConsumptionCall(SolarNodeSettings theSettings, ConsumptionDatum thePower) {
        char cf = 10;
        char lf = 13;
        String ret = "";
        int registeredPowerDatumId = 0;
        int i;
        try {
            Service service1 = new Service();
            if (theSettings.debug) {
            }
            try {
                if (theSettings.debug) {
                }
                for (i = 0; i < thePower.samples.size(); i++) {
                    Call call = (Call) service1.createCall();
                    call = NetworkUtils.setupConsumptionCall(theSettings, call);
                    if (theSettings.debug) {
                    }
                    call.addParameter("nodeId", XMLType.XSD_INT, ParameterMode.IN);
                    call.addParameter("amps", XMLType.XSD_STRING, ParameterMode.IN);
                    call.addParameter("whenLogged", XMLType.XSD_DATETIME, ParameterMode.IN);
                    call.addParameter("errorNote", XMLType.XSD_STRING, ParameterMode.IN);
                    call.setReturnType(XMLType.XSD_STRING);
                    if (theSettings.debug) {
                    }
                    if (theSettings.debug) {
                    }
                    Object[] powerStrings = { Integer.toString((Integer) ((ArrayList) thePower.samples.get(i)).get(0)), Double.toString((Double) ((ArrayList) thePower.samples.get(i)).get(2)), (Date) ((ArrayList) thePower.samples.get(i)).get(1), thePower.errorNote };
                    if (theSettings.debug) {
                        System.out.println("about to make call nodeId:" + Integer.toString((Integer) ((ArrayList) thePower.samples.get(i)).get(0)) + cf);
                        System.out.println("about to make call amps data:" + Double.toString((Double) ((ArrayList) thePower.samples.get(i)).get(2)) + cf);
                        System.out.println("about to make call whenLogged data:" + (Date) ((ArrayList) thePower.samples.get(i)).get(1) + cf);
                        System.out.println("about to make call errorNote:" + thePower.errorNote + "\n");
                    }
                    ret = (String) call.invoke(powerStrings);
                    if (theSettings.debug) {
                        System.out.println("return id from solarnetwork:" + ret + "\n");
                    }
                }
                if (theSettings.debug) {
                }
            } catch (Exception e) {
                registeredPowerDatumId = -1;
                if (theSettings.debug) {
                    System.out.print("error makeConsumptionCall:" + e.getMessage());
                }
            }
            try {
                registeredPowerDatumId = Integer.parseInt(ret);
            } catch (Exception e2) {
                registeredPowerDatumId = -2;
                if (theSettings.debug) {
                    System.out.print("we got no return value from the webservice call:" + e2.getMessage());
                }
            }
        } catch (Exception e) {
            registeredPowerDatumId = -3;
            if (theSettings.debug) {
                System.out.print("error setupPowerCall:" + e.getMessage());
            }
        }
        return registeredPowerDatumId;
    }

    public static int addLocalPowerDatum(PowerDatum thePower, SolarNodeSettings theSettings) {
        char cf = 10;
        char lf = 13;
        if (theSettings.debug) {
        }
        try {
            ObjectContainer db = Db4o.openClient(theSettings.localDb4oServer, 5000, "solar", "solar");
            try {
                if (theSettings.debug) {
                }
                thePower.id = getRandomNegativeId();
                if (theSettings.debug) {
                    System.out.println("about to store random thePower.id:" + thePower.id + "\n");
                    System.out.println("about to store random thePower.pvVolts:" + thePower.pvVolts + "\n");
                    System.out.println("about to store random thePower.pvAmps:" + thePower.pvAmps + "\n");
                    System.out.println("about to store random thePower.batteryVolts:" + thePower.batteryVolts + "\n");
                    System.out.println("about to store random thePower.dcAmps:" + thePower.dcAmps + "\n");
                }
                db.set(thePower);
                if (theSettings.debug) {
                }
            } catch (Exception dbEx1) {
                if (theSettings.debug) {
                    System.out.println("dbEx1: " + dbEx1.getMessage() + "\n");
                }
            } finally {
                db.close();
                if (theSettings.debug) {
                    System.out.println("just closed the db4o database" + "\n");
                }
            }
        } catch (Exception dbEx2) {
            if (theSettings.debug) {
                System.out.println("catch for open db: " + dbEx2.getMessage() + "\n");
            }
        }
        return thePower.id;
    }

    public static int addLocalConsumptionDatum(ConsumptionDatum thePower, SolarNodeSettings theSettings) {
        char cf = 10;
        char lf = 13;
        if (theSettings.debug) {
            System.out.println("about to open db40 file:" + theSettings.localConsumptionDataFile + "\n");
        }
        try {
            ObjectContainer db = Db4o.openClient(theSettings.localDb4oServer, 5001, "solar", "solar");
            try {
                if (theSettings.debug) {
                }
                thePower.id = getRandomNegativeId();
                if (theSettings.debug) {
                    System.out.println("about to store random thePower.id:" + thePower.id + "\n");
                    System.out.println("about to store random thePower.samples:" + thePower.samples.size() + "\n");
                }
                db.set(thePower);
                if (theSettings.debug) {
                    System.out.println("after write to db40:" + thePower.id + cf + lf);
                }
            } catch (Exception dbEx1) {
                if (theSettings.debug) {
                    System.out.println("dbEx1: " + dbEx1.getMessage() + "\n");
                }
            } finally {
                db.close();
                if (theSettings.debug) {
                }
            }
        } catch (Exception dbEx2) {
            if (theSettings.debug) {
                System.out.println("catch for open db: " + dbEx2.getMessage() + "\n");
            }
        }
        return thePower.id;
    }

    public static PowerDatum getLocalPowerDatum(int negativeId, SolarNodeSettings theSettings) {
        char cf = 10;
        char lf = 13;
        PowerDatum foundPowerDatum = new PowerDatum();
        if (theSettings.debug) {
            System.out.println("about to open db40 file:" + theSettings.localDataFile + "\n");
        }
        try {
            ObjectContainer db = Db4o.openClient(theSettings.localDb4oServer, 5000, "solar", "solar");
            try {
                PowerDatum testPower = new PowerDatum(negativeId);
                ObjectSet result = db.get(testPower);
                if (result.size() == 1) {
                    while (result.hasNext()) {
                        foundPowerDatum = (PowerDatum) result.next();
                    }
                } else {
                    if (theSettings.debug) {
                        System.out.println("not a single entry for negative value, count: " + result.size() + "\n");
                    }
                }
            } catch (Exception dbEx1) {
                if (theSettings.debug) {
                    System.out.println("error in finding negative powerDatumId: " + dbEx1.getMessage() + "\n");
                }
            } finally {
                db.close();
            }
        } catch (Exception e) {
            if (theSettings.debug) {
                System.out.println("error in opening db: " + e.getMessage() + "\n");
            }
        }
        return foundPowerDatum;
    }

    public static int updateLocalPowerDatum(PowerDatum thePower, int registeredPowerDatumId, SolarNodeSettings theSettings) {
        int updateState = -1;
        System.out.println("about to open db4o for update: " + "\n");
        try {
            ObjectContainer db = Db4o.openClient(theSettings.localDb4oServer, 5000, "solar", "solar");
            if (theSettings.debug) {
                System.out.println("listing all PowerDatum: " + "\n");
                ObjectSet testResult = db.get(PowerDatum.class);
                if (testResult.size() > 0) {
                } else {
                    System.out.println("no objects in file: " + theSettings.localDataFile + "\n");
                }
            }
            try {
                if (theSettings.debug) {
                    System.out.println("search id: " + thePower.id + "\n");
                    System.out.println("search volts: " + thePower.pvVolts + "\n");
                    System.out.println("search amps: " + thePower.pvAmps + "\n");
                }
                ObjectSet result = db.get(thePower);
                if (result.size() == 1) {
                    while (result.hasNext()) {
                        PowerDatum foundPowerDatum = (PowerDatum) result.next();
                        System.out.println("found it id: " + foundPowerDatum.id + "\n");
                        System.out.println("found it volts: " + foundPowerDatum.pvVolts + "\n");
                        System.out.println("found it amps: " + foundPowerDatum.pvAmps + "\n");
                        System.out.println("found it batteryVolts: " + foundPowerDatum.batteryVolts + "\n");
                        System.out.println("found it dcAmps: " + foundPowerDatum.dcAmps + "\n");
                        foundPowerDatum.id = registeredPowerDatumId;
                        db.set(foundPowerDatum);
                        System.out.println("now updated it : " + foundPowerDatum.id + "\n");
                    }
                } else {
                    if (theSettings.debug) {
                        System.out.println("not a single entry for negative value, count: " + result.size() + "\n");
                    }
                }
            } catch (Exception dbEx1) {
                if (theSettings.debug) {
                    System.out.println("error in update objectset: " + dbEx1.getMessage() + "\n");
                }
            } finally {
                db.close();
            }
        } catch (Exception dbEx2) {
            if (theSettings.debug) {
                System.out.println("catch for open db: " + dbEx2.getMessage() + "\n");
            }
        }
        return updateState;
    }

    public static int updateLocalConsumptionDatum(ConsumptionDatum thePower, int registeredPowerDatumId, SolarNodeSettings theSettings) {
        int updateState = -1;
        try {
            ObjectContainer db = Db4o.openClient(theSettings.localDb4oServer, 5001, "solar", "solar");
            if (theSettings.debug) {
                ObjectSet testResult = db.get(ConsumptionDatum.class);
                if (testResult.size() > 0) {
                } else {
                    System.out.println("no objects in file: " + theSettings.localConsumptionDataFile + "\n");
                }
            }
            try {
                if (theSettings.debug) {
                }
                ObjectSet result = db.get(thePower);
                if (result.size() == 1) {
                    while (result.hasNext()) {
                        ConsumptionDatum foundConsumptionDatum = (ConsumptionDatum) result.next();
                        foundConsumptionDatum.id = registeredPowerDatumId;
                        db.set(foundConsumptionDatum);
                    }
                } else {
                    if (theSettings.debug) {
                        System.out.println("not a single entry for negative value, count: " + result.size() + "\n");
                    }
                }
            } catch (Exception dbEx1) {
                if (theSettings.debug) {
                    System.out.println("error in update objectset: " + dbEx1.getMessage() + "\n");
                }
            } finally {
                db.close();
            }
        } catch (Exception dbEx2) {
            if (theSettings.debug) {
                System.out.println("catch for open db: " + dbEx2.getMessage() + "\n");
            }
        }
        return updateState;
    }

    public static int getRandomNegativeId() {
        Random rand = new Random();
        int i = 2 + (rand.nextInt()) * 10000000;
        i = Math.abs(i);
        return (-i);
    }

    public static ArrayList getNegativeValues(SolarNodeSettings theSettings) {
        ArrayList negativeIds = new ArrayList();
        int unRegisteredPowerDataCount;
        try {
            ObjectContainer db = Db4o.openClient(theSettings.localDb4oServer, 5000, "solar", "solar");
            try {
                Query q = db.query();
                q.constrain(PowerDatum.class);
                Constraint constr = q.descend("id").constrain(new Integer(0)).smaller();
                ObjectSet result = q.execute();
                if (result.isEmpty()) {
                    System.out.println("NOTHING TO DISPLAY");
                } else {
                    unRegisteredPowerDataCount = result.size();
                    if (theSettings.debug) {
                    }
                    int thisPowerDatumId;
                    while (result.hasNext()) {
                        PowerDatum thePower = (PowerDatum) (result.next());
                        negativeIds.add(thePower.id);
                        if (theSettings.debug) {
                        }
                    }
                    if (theSettings.debug) {
                    }
                }
            } catch (Exception ex) {
                if (theSettings.debug) {
                    System.out.println("was not able to open or create the local file" + ex.toString() + "\n");
                }
            } finally {
                db.close();
            }
        } catch (Exception ex) {
            if (theSettings.debug) {
                System.out.println("error opening db4o file:" + ex.toString() + "\n");
            }
        }
        return negativeIds;
    }
}
