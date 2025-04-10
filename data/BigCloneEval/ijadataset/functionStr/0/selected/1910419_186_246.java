public class Test {    private void run(Reader xmlIn, OutputStream out) throws IOException, SAXException {
        Document dom = null;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            f.setCoalescing(true);
            f.setIgnoringComments(true);
            f.setValidating(false);
            DocumentBuilder b = f.newDocumentBuilder();
            dom = b.parse(new InputSource(xmlIn));
        } catch (ParserConfigurationException err) {
            throw new IOException(err);
        }
        Element root = dom.getDocumentElement();
        if (root == null) throw new SAXException("Not root in document");
        Attr att = root.getAttributeNode("label");
        if (att == null) root.setAttribute("label", "Wikipedia");
        Menu menu = parseMenu(root);
        menu.id = "menuWikipedia";
        ZipOutputStream zout = new ZipOutputStream(out);
        String content = ResourceUtils.getContent(XUL4Wikipedia.class, "chrome.manifest");
        addEntry(zout, "chrome.manifest", content);
        content = ResourceUtils.getContent(XUL4Wikipedia.class, "install.rdf");
        addEntry(zout, "install.rdf", content);
        content = ResourceUtils.getContent(XUL4Wikipedia.class, "library.js");
        addDir(zout, "chrome/");
        addDir(zout, "chrome/content/");
        addDir(zout, "chrome/skin/");
        String signal = "/*INSERT_CMD_HERE*/";
        int n = content.indexOf(signal);
        if (n == -1) throw new RuntimeException("where is " + signal + " ??");
        ZipEntry entry = new ZipEntry("chrome/content/library.js");
        zout.putNextEntry(entry);
        PrintWriter pout = new PrintWriter(zout);
        pout.write(content.substring(0, n));
        menu.toJS(pout);
        pout.write(content.substring(n + signal.length()));
        pout.flush();
        zout.closeEntry();
        entry = new ZipEntry("chrome/content/menu.xul");
        zout.putNextEntry(entry);
        pout = new PrintWriter(zout);
        pout.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pout.println("<overlay id=\"xul4wikipedia\" xmlns=\"" + XUL.NS + "\">");
        pout.println("<script src=\"library.js\"/>");
        pout.println("<popup id=\"contentAreaContextMenu\">");
        pout.println("<menuseparator/>");
        menu.toXUL(pout);
        pout.println("</popup>");
        pout.println("</overlay>");
        pout.flush();
        zout.closeEntry();
        InputStream png = XUL4Wikipedia.class.getResourceAsStream("32px-Wikipedia-logo.png");
        if (png == null) throw new IOException("Cannot get icon");
        entry = new ZipEntry("chrome/skin/wikipedia.png");
        zout.putNextEntry(entry);
        IOUtils.copyTo(png, zout);
        zout.closeEntry();
        zout.finish();
        zout.flush();
    }
}