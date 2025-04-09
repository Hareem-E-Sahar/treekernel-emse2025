package org.juddi.client;

import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.juddi.datatype.RegistryObject;
import org.juddi.datatype.request.Admin;
import org.juddi.datatype.request.AuthInfo;
import org.juddi.datatype.request.Inquiry;
import org.juddi.datatype.request.Publish;
import org.juddi.datatype.request.SecurityPolicy;
import org.juddi.datatype.response.AuthToken;
import org.juddi.error.RegistryException;
import org.juddi.handler.HandlerMaker;
import org.juddi.handler.IHandler;
import org.juddi.registry.Registry;
import org.juddi.transport.Transport;
import org.juddi.transport.TransportFactory;
import org.juddi.util.Config;
import org.juddi.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a vesion 2.0 UDDI registry and implements all
 * services as specified in the UDDI version 2.0 specification.
 *
 * @author Steve Viens (sviens@users.sourceforge.net)
 */
public class RegistryProxy extends Registry {

    private static Log log = LogFactory.getLog(RegistryProxy.class);

    private static HandlerMaker maker = HandlerMaker.getInstance();

    private static Transport transport = TransportFactory.getTransport();

    /**
   * Default constructor
   */
    public RegistryProxy() {
    }

    /**
   * "Used to request an authentication token from an Operator Site.
   *  Authentication tokens are required to use all other APIs defined
   *  in the publishers API.  This server serves as the program's
   *  equivalent of a login request."
   *
   * @exception org.juddi.error.RegistryException
   */
    public AuthToken get_authToken() throws RegistryException {
        String userID = Config.getStringProperty("juddi.userID", null);
        if ((userID == null) || (userID.trim().length() == 0)) throw new RegistryException("A juddi.userID property " + "value was not found in the juddi.properties " + "file: juddi.userID = " + userID);
        String password = Config.getStringProperty("juddi.password", null);
        if (password == null) throw new RegistryException("A juddi.password property " + "value was not found in the juddi.properties " + "file: juddi.password = " + password);
        return this.get_authToken(userID, password);
    }

    /**
   * 
   */
    public AuthToken get_authToken(String userID, String password) throws RegistryException {
        if ((userID == null) || (userID.trim().length() == 0)) throw new RegistryException("An invalid userID " + "value was specified: userID = " + userID);
        if (password == null) throw new RegistryException("An invalid password " + "value was specified: password = " + password);
        return super.get_authToken(userID, password);
    }

    /**
   *
   */
    public RegistryObject execute(RegistryObject uddiRequest) throws RegistryException {
        URL endPointURL = null;
        if (uddiRequest instanceof Inquiry) endPointURL = Config.getInquiryURL(); else if (uddiRequest instanceof Publish || uddiRequest instanceof SecurityPolicy) endPointURL = Config.getPublishURL(); else if (uddiRequest instanceof Admin) endPointURL = Config.getAdminURL(); else throw new RegistryException("Unsupported Request: The " + "request '" + uddiRequest.getClass().getName() + "' is an " + "invalid or unknown request type.");
        Document document = XMLUtils.createDocument();
        Element temp = document.createElement("temp");
        String requestName = uddiRequest.getClass().getName();
        IHandler requestHandler = maker.lookup(requestName);
        requestHandler.marshal(uddiRequest, temp);
        Element request = (Element) temp.getFirstChild();
        request.setAttribute("generic", Config.getStringProperty("juddi.clientGeneric", Registry.UDDI_V2_GENERIC));
        request.setAttribute("xmlns", Config.getStringProperty("juddi.clientXMLNS", Registry.UDDI_V2_NAMESPACE));
        Element response = transport.send(request, endPointURL);
        String responseName = response.getLocalName();
        if (responseName == null) {
            throw new RegistryException("Unsupported response " + "from registry. A value was not present.");
        }
        IHandler handler = maker.lookup(responseName.toLowerCase());
        if (handler == null) {
            throw new RegistryException("Unsupported response " + "from registry. Response type '" + responseName + "' is unknown.");
        }
        RegistryObject uddiResponse = handler.unmarshal(response);
        if (uddiResponse instanceof RegistryException) throw ((RegistryException) uddiResponse);
        return uddiResponse;
    }

    /***************************************************************************/
    public static void main(String[] args) throws RegistryException {
        RegistryProxy proxy = new RegistryProxy();
        AuthToken authToken = proxy.get_authToken("sviens", "password");
        AuthInfo authInfo = authToken.getAuthInfo();
        System.out.println("AuthToken: " + authInfo.getValue());
    }
}
