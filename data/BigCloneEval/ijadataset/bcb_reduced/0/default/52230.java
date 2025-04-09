import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.io.StringReader;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;

public class LinearModel {

    List dependentList;

    List regressorList;

    Document dom;

    private String xmlInputString = "";

    private static String testString = "<analysis_input><analysis_model> <analysis_name>LINEAR_MODEL</analysis_name></analysis_model><data><variable><variable_name>var_x</variable_name><variable_type>INDENPEDENT</variable_type><data_type>QUANTITATIVE</data_type><data_value>68.0, 49.0, 60.0, 68.0, 97.0, 82.0, 59.0, 50.0, 73.0, 39.0, 71.0, 95.0, 61.0, 72.0, 87.0, 40.0, 66.0, 58.0, 58.0, </data_value></variable><variable><variable_name>var_y</variable_name><variable_type>DENPEDENT</variable_type> <data_type>QUANTITATIVE</data_type><data_value>75.0, 63.0, 57.0, 88.0, 88.0, 79.0, 82.0, 73.0, 90.0, 62.0, 70.0, 96.0, 76.0, 75.0, 85.0, 40.0, 74.0, 70.0, 75.0, 72.0, </data_value></variable></data></analysis_input>";

    /************************* Constructor *******************************/
    public LinearModel(String xmlInputString) {
        this.xmlInputString = testString;
        regressorList = new ArrayList();
        dependentList = new ArrayList();
    }

    public String generateRStatements() {
        parseXmlFile();
        parseDocument();
        return printRStatements();
    }

    private void parseXmlFile() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource input = new InputSource(new StringReader(xmlInputString));
            dom = db.parse(input);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseDocument() {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("variable");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                Variable var = getVariable(el);
                if (var.variable_type.equalsIgnoreCase("INDENPEDENT")) {
                    regressorList.add(var);
                } else if (var.variable_type.equalsIgnoreCase("DENPEDENT")) {
                    dependentList.add(var);
                }
            }
        }
    }

    /**
	 * I take an employee element and read the values in, create
	 * an Regressor object and return it
	 * @param empEl
	 * @return
	 */
    private Variable getVariable(Element empEl) {
        String analysis_name = getTextValue(empEl, "analysis_name");
        String variable_name = getTextValue(empEl, "variable_name");
        String variable_type = getTextValue(empEl, "variable_type");
        String data_type = getTextValue(empEl, "data_type");
        String data_value = getTextValue(empEl, "data_value");
        Variable variable = new Variable(variable_name, variable_type, data_value);
        return variable;
    }

    /**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is name I will return John
	 * @param ele
	 * @param tagName
	 * @return
	 */
    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }
        return textVal;
    }

    /**
	 * Calls getTextValue and returns a int value
	 * @param ele
	 * @param tagName
	 * @return
	 */
    private int getIntValue(Element ele, String tagName) {
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    /**
	 * Iterate through the list and print the
	 * content to console
	 */
    private void printData() {
        StringBuffer sb = new StringBuffer();
        Iterator it = regressorList.iterator();
        while (it.hasNext()) {
            System.out.println("printData regressor: " + it.next().toString());
        }
        it = dependentList.iterator();
        while (it.hasNext()) {
            System.out.println("printData depedent: " + it.next().toString());
        }
    }

    private String printRStatements() {
        StringBuffer sb = new StringBuffer();
        StringBuffer sbRegressorList = new StringBuffer();
        Iterator it = regressorList.iterator();
        Variable r = null;
        Variable d = null;
        while (it.hasNext()) {
            r = (Variable) (it.next());
            sb.append(r.variable_name);
            sb.append(" = c(");
            sb.append(r.data_value);
            sb.append("); ");
            sbRegressorList.append(r.variable_name + " + ");
        }
        sb.append("\n");
        it = dependentList.iterator();
        while (it.hasNext()) {
            d = (Variable) (it.next());
            sb.append(d.variable_name);
            sb.append(" = c(");
            sb.append(d.data_value);
            sb.append("); ");
        }
        sb.append("\n");
        String regressorList = sbRegressorList.toString();
        regressorList = regressorList.substring(0, regressorList.length() - 3);
        sb.append("m = lm(" + d.variable_name + " ~ " + regressorList + ")");
        System.out.println("\n\nR Statements = \n" + sb.toString());
        return sb.toString();
    }

    /*************************** Main test code ***********************************/
    public static void main(String[] args) {
        System.out.println(testString);
        LinearModel test = new LinearModel(new String(testString));
        String rStatements = test.generateRStatements();
        System.out.println(rStatements);
    }

    /********************* Private classes declarations ********************/
    private class Variable {

        String variable_name;

        String variable_type;

        String data_value;

        Variable(String variable_name, String variable_type, String data_value) {
            this.variable_name = variable_name;
            this.variable_type = variable_type;
            this.data_value = data_value;
        }
    }

    private class Dependent extends Variable {

        Dependent(String variable_name, String variable_type, String data_value) {
            super(variable_name, variable_type, data_value);
        }
    }

    private class Regressor extends Variable {

        Regressor(String variable_name, String variable_type, String data_value) {
            super(variable_name, variable_type, data_value);
        }
    }
}
