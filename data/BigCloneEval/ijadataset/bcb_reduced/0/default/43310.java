import java.io.File;
import java.io.FileNotFoundException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.util.*;

public class CSVHandler {

    public CSVHandler(String filename) {
        initDB(filename);
    }

    public Set<Address> doHandle(AddressRequest request) {
        Set<Address> resultAddresses = new HashSet<Address>();
        for (int i = 0; i < AddressBook.numRun; i++) {
            resultAddresses.addAll(search(request.getFirstname(), request.getLastname()));
        }
        return resultAddresses;
    }

    private NodeList listOfAddresses;

    private void initDB(String filename) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(filename));
            doc.getDocumentElement().normalize();
            listOfAddresses = doc.getElementsByTagName("address");
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

    /***
	 * Sequentially search for a person identified by given 
	 * first name and last name.
	 * TODO: implement more efficient algorithm for searching.
	 * @param firstname -- first name of the person
	 * @param lastname -- last name of the person
	 * @return the address of the person.
	 */
    private Set<Address> search(String firstname, String lastname) {
        Address newAddress = new Address();
        newAddress.setFirstname(firstname);
        newAddress.setLastname(lastname);
        Set<Address> resultAddresses = new HashSet<Address>();
        for (int index = 0; index < listOfAddresses.getLength(); index++) {
            Node currentXMLNode = listOfAddresses.item(index);
            if (currentXMLNode.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) currentXMLNode;
                NodeList firstNameList = currentElement.getElementsByTagName("firstname");
                Element firstNameElement = (Element) firstNameList.item(0);
                NodeList textFNList = firstNameElement.getChildNodes();
                String currentFirstName = ((Node) textFNList.item(0)).getNodeValue().trim();
                if (!firstname.equals(currentFirstName)) continue;
                NodeList lastNameList = currentElement.getElementsByTagName("lastname");
                Element lastNameElement = (Element) lastNameList.item(0);
                NodeList textLNList = lastNameElement.getChildNodes();
                String currentLastName = ((Node) textLNList.item(0)).getNodeValue().trim();
                if (!lastname.equals(currentLastName)) continue;
                NodeList streetList = currentElement.getElementsByTagName("street");
                Element streetElement = (Element) streetList.item(0);
                NodeList textStreetList = streetElement.getChildNodes();
                newAddress.setStreet(((Node) textStreetList.item(0)).getNodeValue().trim());
                NodeList cityList = currentElement.getElementsByTagName("city");
                Element cityElement = (Element) cityList.item(0);
                NodeList textCityList = cityElement.getChildNodes();
                newAddress.setCity(((Node) textCityList.item(0)).getNodeValue().trim());
                NodeList stateList = currentElement.getElementsByTagName("state");
                Element stateElement = (Element) stateList.item(0);
                NodeList textStateList = stateElement.getChildNodes();
                newAddress.setState(((Node) textStateList.item(0)).getNodeValue().trim());
                NodeList zipList = currentElement.getElementsByTagName("zipcode");
                Element zipElement = (Element) zipList.item(0);
                NodeList textZipList = zipElement.getChildNodes();
                newAddress.setZipcode(((Node) textZipList.item(0)).getNodeValue().trim());
                resultAddresses.add(newAddress);
            }
        }
        return resultAddresses;
    }
}
