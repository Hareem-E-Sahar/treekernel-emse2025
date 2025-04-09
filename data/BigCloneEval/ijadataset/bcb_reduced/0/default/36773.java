import java.io.*;
import java.util.HashMap;
import org.w3c.dom.*;
import javax.xml.parsers.*;

class ApplyTransformRes extends HTTPResponse {

    public ApplyTransformRes() throws Exception {
        super();
    }

    public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap headers) throws Exception {
        outStream.write(doTransform(urlData, headers));
    }

    private byte[] doTransform(HTTPurl urlData, HashMap headers) throws Exception {
        String xml = urlData.getParameter("xml");
        String xsl = urlData.getParameter("xsl");
        String themeDir = store.getProperty("path.theme");
        String userAgent = (String) headers.get("User-Agent");
        if (userAgent != null && userAgent.length() > 0) {
            String[] agents = store.getAgentMappingList();
            for (int x = 0; x < agents.length; x++) {
                if (userAgent.indexOf(agents[x]) > -1) {
                    themeDir = store.getThemeForAgent(agents[x]);
                }
            }
        }
        String httpDir = store.getProperty("path.httproot");
        xml = httpDir + File.separator + "themes" + File.separator + themeDir + File.separator + "xml" + File.separator + xml + ".xml";
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        File xmlSource = new File(xml);
        if (xmlSource.exists() == false) throw new Exception("Source XML file (" + xml + ") not found");
        Document doc = docBuilder.parse(xmlSource);
        XSL transformer = new XSL(doc, xsl + ".xsl", urlData, headers);
        transformer.addCookie("backURL", urlData.getReqString());
        return transformer.doTransform();
    }
}
