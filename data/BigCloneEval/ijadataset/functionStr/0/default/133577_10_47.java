public class Test {    ResultHelperTests() throws Exception {
        Node node;
        String string;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File("tests" + File.separator + "test.xml"));
        Log.log("instantiating ResultHelper for test.xml");
        ResultHelper rh = new ResultHelper(doc);
        string = rh.getDataAttribute("name");
        if (string.compareTo("testXML") != 0) Log.error("name returned " + string);
        string = rh.getDataAttribute("type");
        if (string.compareTo("text/plain") != 0) Log.error("content-type returned " + string);
        string = rh.getDataAttribute("source");
        if (string.compareTo("test.xml") != 0) Log.error("source returned " + string);
        string = rh.getDataAttribute("author");
        if (string.compareTo("cts") != 0) Log.error("author returned " + string);
        string = rh.getEmbeddedData();
        if (string.compareTo("Some useless data") != 0) Log.error("getEmbeddedData returned \"" + string + "\"");
        string = rh.toString();
        if (string.indexOf("Some useless data") == -1) Log.error("toString returned: " + string);
        doc = rh.getDocument();
        string = doc.getNodeName();
        if (string.compareTo("#document") != 0) Log.error("getDocument returned a node with name " + string);
        node = doc.getFirstChild();
        string = node.getNodeName();
        if (string.compareTo("ColanderResult") != 0) Log.error("getDocument.firstChild returned " + string);
        Log.log("instantiating ResultHelper with internal data");
        ResultHelper rh2 = new ResultHelper("Some useless data", "testXML", "text/plain", "test.xml", "cts");
        string = rh2.getDataAttribute("name");
        if (string.compareTo("testXML") != 0) Log.error("name returned " + string);
        string = rh2.getDataAttribute("type");
        if (string.compareTo("text/plain") != 0) Log.error("content-type returned " + string);
        string = rh2.getDataAttribute("source");
        if (string.compareTo("test.xml") != 0) Log.error("source returned " + string);
        string = rh2.getEmbeddedData();
        if (string.compareTo("Some useless data") != 0) Log.error("getEmbeddedData returned " + string);
    }
}