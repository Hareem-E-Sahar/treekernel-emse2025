package lrf.epub;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lrf.Utils;
import org.w3c.tidy.Tidy;

public abstract class EPUBMetaData {

    public String language = null;

    public static final String ffam = "Linux Libertine O";

    public static boolean doNotEmbedOTFFonts = false;

    public static boolean usep = false;

    public static String[][] otfDefs = { { "fonts/LinLibertine_Re-2.8.14.otf", "Linux Libertine O", "" }, { "fonts/LinLibertine_It-2.8.2.otf", "Linux Libertine O", "font-style:italic;" }, { "fonts/LinLibertine_Bd-2.8.1.otf", "Linux Libertine O", "font-weight:bold;" }, { "fonts/LinLibertine_BI-2.8.0.otf", "Linux Libertine O", "font-style:italic; font-weight:bold;" } };

    private XMLNode rootNavMap;

    /**
	 * Listado de elementos del manifiesto
	 */
    Vector<XMLNode> mftItems = new Vector<XMLNode>();

    /**
	 * LIstado ordenado de documentos xhtml que conmponen el epub
	 */
    protected Vector<XMLNode> spineItems = new Vector<XMLNode>();

    int tocItemCounter = 1;

    int mftItemCounter = 1;

    int spineItemCounter = 1;

    String epubFilename;

    ZipOutputStream zos = null;

    File tmpOutF = null;

    ZipOutputStream tmpzos = null;

    public abstract String getTitle();

    public abstract String getCreator();

    public abstract String getPublisher();

    public abstract Vector<String> getSubject();

    public abstract String getRights();

    public abstract String getIdentifier();

    public final String getLanguage() {
        return language;
    }

    /**
	 * Nodo raiz del TOC
	 * @return raiz de la tabla de contenidos
	 */
    public XMLNode getNavMap() {
        return rootNavMap;
    }

    public Vector<XMLNode> getManifestItems() {
        return mftItems;
    }

    public Vector<XMLNode> getSpineOrder() {
        return spineItems;
    }

    protected EPUBMetaData(String lang) {
        language = lang;
        rootNavMap = new XMLNode("navMap", null, null, false);
    }

    protected EPUBMetaData(String epfn, String lang) throws Exception {
        language = lang;
        rootNavMap = new XMLNode("navMap", null, null, false);
        init(epfn);
    }

    public void init(String epfn) throws FileNotFoundException, IOException {
        tmpOutF = File.createTempFile("lrftools", "-tmp");
        tmpzos = new ZipOutputStream(new FileOutputStream(tmpOutF));
        epubFilename = epfn;
    }

    public void close() throws IOException {
        ZipEntry ze;
        ze = new ZipEntry("mimetype");
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(20);
        ze.setCrc(0x2cab616f);
        zos = new ZipOutputStream(new FileOutputStream(epubFilename));
        zos.putNextEntry(ze);
        zos.write("application/epub+zip".getBytes());
        ze = new ZipEntry("META-INF/container.xml");
        zos.putNextEntry(ze);
        String container = "<?xml version=\"1.0\"?>\n<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n  <rootfiles>\n    <rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n  </rootfiles>\n</container>";
        zos.write(container.getBytes());
        Content c = new Content(this);
        addMemoryContent("OEBPS/content.opf", c.dump(), 5);
        TOC t = new TOC(this);
        addMemoryContent("OEBPS/toc.ncx", t.dump(), 5);
        tmpzos.close();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(tmpOutF));
        for (ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
            zos.putNextEntry(ze);
            Utils.writeTo(zis, zos);
        }
        zis.close();
        zos.close();
        tmpOutF.delete();
    }

    private void nextZipEntry(String url, int level) throws IOException {
        if (!url.startsWith("META-INF/") && !url.startsWith("OEBPS/")) url = "OEBPS/" + url;
        ZipEntry ze = new ZipEntry(url);
        ze.setMethod(ZipEntry.DEFLATED);
        tmpzos.putNextEntry(ze);
    }

    private void writeInputStream(InputStream is) throws IOException {
        Utils.writeTo(is, tmpzos);
    }

    /**
	 * A�ade una cadena al zip epub
	 * 
	 * @param url
	 * 		URL de la cadena dentro del archivo zip. Se le antepone OEBPS/ si es
	 * 		necesario
	 * @param content
	 * 		Contenido del archivo
	 * @param level
	 * 		Nivel de compresion
	 * @throws IOException
	 */
    public void addMemoryContent(String url, String content, int level) throws IOException {
        nextZipEntry(url, level);
        tmpzos.write(content.getBytes());
    }

    /**
	 * A�ade un archivo al zip epub
	 * 
	 * @param url
	 * 		URL del archivo dentro del zip. Se le antepone OEPBS/ si es
	 * 		necesario
	 * @param f
	 * 		Fichero a a�adir
	 * @param level
	 * 		Nivel de compresion
	 * @throws IOException
	 */
    public void addFile(String url, File f, int level) throws IOException {
        nextZipEntry(url, level);
        FileInputStream fis = new FileInputStream(f);
        writeInputStream(fis);
        fis.close();
    }

    /**
	 * A�ade el contenido de un byte[] al archivo zip epub
	 * 
	 * @param url
	 * 		URL dentro del archivo ZIP. Sele antepone OEBPS/ si es necesario
	 * @param buf
	 * 		contenido del archivo
	 * @param level
	 * 		Nivel de compresi�n
	 * @throws IOException
	 */
    public void addBA(String url, byte buf[], int level) throws IOException {
        nextZipEntry(url, level);
        tmpzos.write(buf);
    }

    public void addBAOS(String url, ByteArrayOutputStream baos, int level) throws IOException {
        nextZipEntry(url, level);
        baos.writeTo(tmpzos);
    }

    public void addIS(String url, InputStream is, int level) throws IOException {
        nextZipEntry(url, level);
        writeInputStream(is);
    }

    public void addStoredResource(String name, int level) throws IOException {
        nextZipEntry(name, level);
        InputStream is = ClassLoader.getSystemResourceAsStream(name);
        writeInputStream(is);
    }

    private int tocDepth = 1;

    /**
	 * Crea un nuevo nodo de navegacion. Todos los NavPoint deben ser creados
	 * mediante este procedimiento.
	 * 
	 * @param name
	 * 		Nombre del nodo
	 * @param url
	 * 		URL dentro del archivo zip epub con el que conecta. No debe contener
	 * 		OEBPS/.
	 * @param padre
	 * 		Nombre del navPoint padre. rootNavMap es el nodo raiz.
	 * @return Nuevo nodo creado.
	 */
    public XMLNode createNavPoint(String name, String url, XMLNode padre) {
        XMLNode ret = new XMLNode("navPoint", null, padre, false);
        ret.addAtr("id", "navp-" + tocItemCounter);
        ret.addAtr("playOrder", "" + tocItemCounter);
        tocItemCounter++;
        XMLNode lbl = new XMLNode("navLabel", null, ret, false);
        new XMLNode("text", name.trim(), lbl, true);
        XMLNode cnt = new XMLNode("content", null, ret, false);
        cnt.addAtr("src", url);
        tocDepth = -1;
        depth(rootNavMap);
        return ret;
    }

    private void depth(XMLNode k) {
        if (k == null) return;
        if (k.childs.size() > 0) {
            for (int i = 0; i < k.childs.size(); i++) {
                XMLNode hijo = k.childs.elementAt(i);
                hijo.padre = k;
                depth(hijo);
            }
        } else {
            int depth = -2;
            while (k.padre != null) {
                depth++;
                k = k.padre;
            }
            if (tocDepth < depth) tocDepth = depth;
        }
    }

    public static String createRandomIdentifier() {
        String ret = "";
        Math.random();
        ret += Integer.toHexString(0x10000000 + (int) (Math.random() * 0xffffffff));
        ret += "-";
        ret += Integer.toHexString(0x1000 + (int) (Math.random() * 0xffff));
        ret += "-";
        ret += Integer.toHexString(0x1000 + (int) (Math.random() * 0xffff));
        ret += "-";
        ret += Integer.toHexString(0x1000 + (int) (Math.random() * 0xffff));
        ret += "-";
        ret += Integer.toHexString(0x10000000 + (int) (Math.random() * 0xffffffff));
        ret += Integer.toHexString(0x1000 + (int) (Math.random() * 0xffff));
        return ret;
    }

    /**
	 * A�ade un nodo al manifest del epub. Si spinned==true se a�ade al apartado
	 * spinned del ncx.
	 * 
	 * @param url
	 * 		URL dentro del archivo
	 * @param fnl
	 * @param spinned
	 * @throws IOException
	 */
    protected void mftAddNodoAndSpine(String url, boolean spinned) throws IOException {
        mftAddNodoAndSpineWithMT(url, spinned, Content.getMime(url.substring(1 + url.lastIndexOf("."))));
    }

    protected void mftAddNodoAndSpineWithMT(String url, boolean spinned, String mty) throws IOException {
        XMLNode nodo = mftAddNodoWithMT(url, mty);
        if (spinned) {
            spineItems.add(nodo);
        }
    }

    protected XMLNode mftAddNodoWithMT(String url, String mty) {
        XMLNode nodo;
        url = url.replace('\\', '/');
        nodo = new XMLNode("item", null, null, false);
        nodo.addAtr("id", "item" + (mftItemCounter++));
        nodo.addAtr("href", url);
        nodo.addAtr("media-type", mty);
        mftItems.add(nodo);
        return nodo;
    }

    public int getTOCDepth() {
        return tocDepth;
    }

    public void processFile(File f, String epubUrl) throws FileNotFoundException, IOException, Exception {
        InputStream is = new FileInputStream(f);
        processFile(is, epubUrl);
        is.close();
    }

    public void processFile(String content, String epubUrl) throws IOException, Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(content.getBytes());
        processFile(baos, epubUrl);
    }

    public void processFile(ByteArrayOutputStream bos, String epubUrl) throws FileNotFoundException, IOException, Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        processFile(bis, epubUrl);
    }

    private String procUseP(String contenido) {
        contenido = contenido.replace("<div", "<p");
        contenido = contenido.replace("</div", "</p");
        return contenido;
    }

    public void processFile(InputStream is, String epubUrl) throws IOException, FileNotFoundException, Exception {
        epubUrl = Utils.toUnhandText(epubUrl);
        String fnl = epubUrl.toLowerCase();
        if (fnl.endsWith(".html")) {
            String contenido = htmlToXhtml(epubUrl, is);
            String xurl = epubUrl.substring(0, epubUrl.length() - 5) + ".xhtml";
            mftAddNodoAndSpine(xurl, true);
            if (usep) {
                contenido = procUseP(contenido);
            }
            addMemoryContent(xurl, contenido, 5);
        } else if (fnl.endsWith(".jpg") || fnl.endsWith(".jpeg")) {
            mftAddNodoAndSpine(epubUrl, false);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".png")) {
            mftAddNodoAndSpine(epubUrl, false);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".bmp")) {
            mftAddNodoAndSpine(epubUrl, false);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".gif")) {
            mftAddNodoAndSpine(epubUrl, false);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".svg")) {
            mftAddNodoAndSpine(epubUrl, false);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".xhtml")) {
            mftAddNodoAndSpine(epubUrl, true);
            if (usep) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Utils.writeTo(is, baos);
                String con = baos.toString();
                con = procUseP(con);
                addMemoryContent(epubUrl, con, 5);
            } else {
                addIS(epubUrl, is, 5);
            }
        } else if (fnl.endsWith(".xml")) {
            mftAddNodoAndSpine(epubUrl, true);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".xpgt")) {
            mftAddNodoAndSpine(epubUrl, true);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".css")) {
            mftAddNodoAndSpine(epubUrl, false);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".ttf")) {
            mftAddNodoAndSpine(epubUrl, false);
            addIS(epubUrl, is, 5);
        } else if (fnl.endsWith(".otf")) {
            mftAddNodoAndSpine(epubUrl, false);
            addIS(epubUrl, is, 5);
        }
    }

    protected String htmlToXhtml(String fn, InputStream is) throws IOException, Exception {
        String contenido = null;
        Tidy tidy = new Tidy();
        tidy.setXmlOut(true);
        tidy.setXHTML(true);
        tidy.setFixBackslash(true);
        tidy.setOnlyErrors(false);
        tidy.setQuiet(true);
        tidy.setMakeClean(true);
        ByteArrayOutputStream baoAux = new ByteArrayOutputStream();
        PrintWriter nullPW = new PrintWriter(baoAux);
        tidy.setErrout(nullPW);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        BufferedInputStream bis = new BufferedInputStream(is);
        tidy.parse(bis, bos);
        contenido = new String(baos.toByteArray());
        contenido = contenido.replaceAll("src=\"file:", "src=\"");
        if (contenido == null || contenido.length() == 0) {
            nullPW.flush();
            System.out.println(fn + ":" + baoAux.toString());
            throw new Exception("Tidy error");
        }
        bis.close();
        bos.close();
        return contenido;
    }

    public void buildCSS(String epubUrl, Hashtable<String, String> estilos, boolean putLineHeight) throws FileNotFoundException, IOException, Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bos);
        int i;
        if (!doNotEmbedOTFFonts) {
            for (i = 0; i < otfDefs.length; i++) {
                pw.println("@font-face { font-family: \"" + otfDefs[i][1] + "\"; " + otfDefs[i][2] + " src: url(" + otfDefs[i][0] + "); }");
            }
            for (i = 0; i < otfDefs.length; i++) {
                processFile(getClass().getResourceAsStream("/" + otfDefs[i][0]), otfDefs[i][0]);
            }
        }
        String tosort[] = new String[estilos.size()];
        i = 0;
        Hashtable<String, String> reverse = new Hashtable<String, String>();
        for (Enumeration<String> keys = estilos.keys(); keys.hasMoreElements(); ) {
            String k = keys.nextElement();
            String v = estilos.get(k);
            if (putLineHeight && k.contains("text-indent") && !k.contains("line-height")) {
                k += ";line-height:0.9em";
            }
            reverse.put(v, k);
            tosort[i++] = v;
        }
        Arrays.sort(tosort);
        for (i = 0; i < tosort.length; i++) {
            String na = tosort[i];
            String st = reverse.get(na);
            pw.println("." + na + " { " + st.replace('\'', '\"') + " } ");
        }
        pw.flush();
        processFile(bos, epubUrl);
    }
}
