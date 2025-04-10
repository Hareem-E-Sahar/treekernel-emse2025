package at.co.ait.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import at.co.ait.domain.integration.ILoadingGateway;
import at.co.ait.domain.services.DirectoryListingService;
import at.co.ait.utils.DOM;
import at.co.ait.web.common.UserPreferences;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(value = "/filebrowser/*")
public class FileBrowser {

    private static final Logger logger = LoggerFactory.getLogger(FileBrowser.class);

    @Autowired
    private DirectoryListingService directorylist;

    @Autowired
    private ILoadingGateway loading;

    @RequestMapping(value = "index", method = RequestMethod.GET)
    @ModelAttribute("fileList")
    public List<String> getMyFiles() {
        return null;
    }

    @RequestMapping(value = "ajaxTree", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public List<Map<String, Object>> getAjaxTree() {
        return directorylist.buildJSONMsgFromDir(null);
    }

    @RequestMapping(value = "sendData", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public List<Map<String, Object>> getLazyNode(@RequestParam String key) {
        return directorylist.buildJSONMsgFromDir(key);
    }

    @RequestMapping(value = "submitNodes", method = RequestMethod.POST)
    @ResponseBody
    public String submitNodes(@RequestParam(value = "selNodes", required = false) String nodes) {
        logger.info(nodes);
        return "submitted";
    }

    @RequestMapping(value = "sendNodes", method = RequestMethod.POST)
    @ResponseBody
    public String sendNodes(@RequestParam(value = "selNodes", required = true) List<String> keys, @RequestParam(value = "lang", required = false) String language) {
        logger.info("Received nodes to process: " + keys.size());
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("lang", language);
        UserPreferences prefs = (UserPreferences) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        for (String key : keys) {
            loading.loadfolder(directorylist.getFileByKey(Integer.valueOf(key)), prefs, options);
        }
        return "done";
    }

    private HashMap<String, String> marcLang2TesseractLang;

    {
        marcLang2TesseractLang = new HashMap<String, String>();
        marcLang2TesseractLang.put("bul", "bul");
        marcLang2TesseractLang.put("cat", "cat");
        marcLang2TesseractLang.put("cze", "ces");
        marcLang2TesseractLang.put("dan", "dan");
        marcLang2TesseractLang.put("ger", "deu");
        marcLang2TesseractLang.put("grc", "ell");
        marcLang2TesseractLang.put("gre", "ell");
        marcLang2TesseractLang.put("eng", "eng");
        marcLang2TesseractLang.put("fin", "fin");
        marcLang2TesseractLang.put("fre", "fra");
        marcLang2TesseractLang.put("hun", "hun");
        marcLang2TesseractLang.put("ind", "ind");
        marcLang2TesseractLang.put("ita", "ita");
        marcLang2TesseractLang.put("lav", "lav");
        marcLang2TesseractLang.put("lit", "lit");
        marcLang2TesseractLang.put("dut", "nld");
        marcLang2TesseractLang.put("nor", "nor");
        marcLang2TesseractLang.put("pol", "pol");
        marcLang2TesseractLang.put("por", "por");
        marcLang2TesseractLang.put("rum", "ron");
        marcLang2TesseractLang.put("rus", "rus");
        marcLang2TesseractLang.put("slo", "slk");
        marcLang2TesseractLang.put("slv", "slv");
        marcLang2TesseractLang.put("spa", "spa");
        marcLang2TesseractLang.put("srp", "srp");
        marcLang2TesseractLang.put("swe", "swe");
        marcLang2TesseractLang.put("tgl", "tgl");
        marcLang2TesseractLang.put("tur", "tur");
        marcLang2TesseractLang.put("ukr", "ukr");
        marcLang2TesseractLang.put("vie", "vie");
    }

    /**
	 * MARC doc {@link http://www.loc.gov/marc/bibliographic/ecbdlist.html}
	 * @param node
	 * @return
	 */
    @RequestMapping(value = "detectLanguage", method = RequestMethod.GET)
    @ResponseBody
    public String detectLanguage(@RequestParam(value = "node", required = true) String node) {
        File file = directorylist.getFileByKey(Integer.valueOf(node));
        XPathExpression getCtrlFld8;
        try {
            getCtrlFld8 = DOM.getXPath("//marc:controlfield[@tag='008']");
        } catch (XPathExpressionException e1) {
            logger.error("XPATH invalid", e1);
            return "ERROR - see log";
        }
        do {
            for (File inspect : file.listFiles()) {
                logger.debug(inspect.getAbsolutePath());
                try {
                    Document doc = null;
                    String fnlc = inspect.getName().toLowerCase();
                    if (fnlc.endsWith(".xml")) {
                        doc = DOM.parse(inspect);
                    } else if (fnlc.endsWith(".mrc")) {
                        doc = readMarcBinary(inspect);
                    }
                    if (doc != null) {
                        NodeList nl = (NodeList) getCtrlFld8.evaluate(doc, XPathConstants.NODESET);
                        logger.debug("008: " + nl.getLength());
                        for (int i = 0; i < nl.getLength(); ++i) {
                            Element e = (Element) nl.item(i);
                            String val = e.getTextContent();
                            if (val.length() > 37) {
                                String marcLangCode = val.substring(35, 38).trim();
                                String tessLang = marcLang2TesseractLang.get(marcLangCode);
                                return tessLang == null ? "" : tessLang;
                            }
                        }
                    }
                } catch (XPathExpressionException e) {
                    logger.error(file.getAbsolutePath() + " XPath eval error.", e);
                } catch (IOException e) {
                    logger.error(file.getAbsolutePath() + " read error.", e);
                }
            }
            file = file.getParentFile();
        } while (file != null);
        return "";
    }

    private Document readMarcBinary(File marcBinary) throws IOException {
        InputStream input = new FileInputStream(marcBinary);
        MarcReader reader = new MarcStreamReader(input);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        MarcWriter writer = new MarcXmlWriter(bout, "UTF-8");
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        writer.close();
        return DOM.parse(bout.toByteArray());
    }
}
