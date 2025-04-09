package writer2latex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.text.Collator;
import org.w3c.dom.Element;
import writer2latex.api.Converter;
import writer2latex.api.ConverterFactory;
import writer2latex.api.ConverterResult;
import writer2latex.api.OutputFile;
import writer2latex.util.Misc;
import writer2latex.util.Config;
import writer2latex.util.L10n;
import writer2latex.office.*;
import writer2latex.xhtml.XhtmlDocument;

/**
 * <p>Commandline utility to convert an OpenOffice.org Writer XML file into XHTML/LaTeX/BibTeX</p>
 * <p>The utility is invoked with the following command line:</p>
 * <pre>java -jar writer2latex.jar [options] source [target]</pre>
 * <p>Where the available options are
 * <ul>
 * <li><code>-latex</code>, <code>-bibtex</code>, <code>-xhtml</code>,
       <code>-xhtml+mathml</code>, <code>-xhtml+mathml+xsl</code>
 * <li><code>-recurse</code>
 * <li><code>-config[=]filename</code>
 * <li><code>-template[=]filename</code>
 * <li><code>-option[=]value</code>
 * </ul>
 * <p>where <code>option</code> can be any simple option known to Writer2LaTeX
 * (see documentation for the configuration file).</p>
 */
public class Writer2XHTML {

    private static String VERSION = "0.5";

    private static String DATE = "16/10/2007";

    private String sTargetMIME = MIMETypes.XHTML;

    private boolean bRecurse = false;

    private String sConfigFileName = null;

    private String sTemplateFileName = null;

    private Hashtable options = new Hashtable();

    private String sSource = null;

    private String sTarget = null;

    @SuppressWarnings("unused")
    private String sOutputFormat = null;

    private String sOutPathName = null;

    private String sOutFileName = null;

    private String sOutFileExtension = null;

    private byte[] templateBytes = null;

    private XhtmlDocument template = null;

    private Config config;

    L10n l10n;

    boolean bBatch = false;

    String sDefaultLang = null;

    String sDefaultCountry = null;

    public Writer2XHTML() {
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String getDate() {
        return DATE;
    }

    public void setsOutputFormat(String of) {
        sOutputFormat = of;
    }

    public void setsOutFileExtension(String ofe) {
        sOutFileExtension = ofe;
    }

    public void setTargetMime(String tm) {
        sTargetMIME = tm;
    }

    public void setsSource(String source) {
        sSource = source;
    }

    public void setsOutPathName(String opn) {
        sOutPathName = opn;
    }

    public void setsOutFileName(String ofn) {
        sOutFileName = ofn;
    }

    public void doConversion() {
        String sOutputFormat = "xhtml";
        System.out.println();
        System.out.println("This is Writer2" + sOutputFormat + ", Version " + ConverterFactory.getVersion() + " (" + ConverterFactory.getDate() + ")");
        System.out.println();
        System.out.println("Starting conversion...");
        config = new Config();
        if (sConfigFileName != null) {
            File f = new File(sConfigFileName);
            if (!f.exists()) {
                System.err.println("The configuration file '" + sConfigFileName + "' does not exist.");
            } else {
                try {
                    config.read(new FileInputStream(sConfigFileName));
                } catch (Throwable t) {
                    System.err.println("I had trouble reading the configuration file " + sConfigFileName);
                    t.printStackTrace();
                }
            }
        }
        String sFullOutFileName = sTarget != null ? sTarget : Misc.removeExtension(sSource);
        if (sFullOutFileName.endsWith(File.separator)) {
            sOutPathName = sFullOutFileName;
            sOutFileName = (new File(sSource)).getName();
        } else {
            File f = new File(sFullOutFileName);
            sOutPathName = f.getParent();
            if (sOutPathName == null) {
                sOutPathName = "";
            } else {
                sOutPathName += File.separator;
            }
            sOutFileName = f.getName();
        }
        sOutFileName = Misc.removeExtension(sOutFileName);
        Enumeration keys = options.keys();
        while (keys.hasMoreElements()) {
            String sKey = (String) keys.nextElement();
            String sValue = (String) options.get(sKey);
            config.setOption(sKey, sValue);
        }
        l10n = new L10n();
        sDefaultLang = System.getProperty("user.language");
        sDefaultCountry = System.getProperty("user.country");
        l10n.setLocale(sDefaultLang, sDefaultCountry);
        File f = new File(sSource);
        if (!f.exists()) {
            System.out.println("I'm sorry, I can't find " + sSource);
            System.exit(1);
        }
        if (!f.canRead()) {
            System.out.println("I'm sorry, I can't read " + sSource);
            System.exit(1);
        }
        if (sTemplateFileName != null) {
            try {
                templateBytes = Misc.inputStreamToByteArray(new FileInputStream(sTemplateFileName));
                template = new XhtmlDocument("Template", XhtmlDocument.XHTML_MATHML);
                template.read(new ByteArrayInputStream(templateBytes));
            } catch (Throwable t) {
                t.printStackTrace();
                template = null;
            }
        }
        if (f.isDirectory()) {
            convertDirectory(f, sOutPathName);
        } else if (f.isFile()) {
            convertFile(f, sOutPathName);
        }
        System.out.println("Done!");
    }

    private String convertFile(File file, String sOutPathName) {
        String sFileName = file.getName();
        String sLocalOutFileName = bBatch ? Misc.removeExtension(sFileName) : sOutFileName;
        System.out.println("Converting " + sFileName);
        ConverterResult dataOut = null;
        ConverterFactory factory = new ConverterFactory();
        factory.setConfig(config);
        Converter converter = factory.createConverter(sTargetMIME);
        if (converter != null) {
            try {
                if (templateBytes != null) {
                    converter.readTemplate(new ByteArrayInputStream(templateBytes));
                }
                dataOut = converter.convert(new FileInputStream(file), sLocalOutFileName);
            } catch (Exception e) {
                System.out.println(" --> Conversion failed");
                e.printStackTrace();
            }
        }
        File dir = new File(sOutPathName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Enumeration docEnum = dataOut.getDocumentEnumeration();
        while (docEnum.hasMoreElements()) {
            OutputFile docOut = (OutputFile) docEnum.nextElement();
            String fileName = sOutPathName + docOut.getFileName();
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                docOut.write(fos);
                fos.flush();
                fos.close();
            } catch (Exception writeExcept) {
                System.out.println("\nThere was an error writing out file <" + fileName + ">");
                writeExcept.printStackTrace();
            }
        }
        return sLocalOutFileName + sOutFileExtension;
    }

    private void convertDirectory(File dir, String sOutPathName) {
        bBatch = true;
        XhtmlDocument htmlDoc = new XhtmlDocument("index", XhtmlDocument.XHTML11);
        htmlDoc.setEncoding(config.xhtmlEncoding());
        htmlDoc.setNoDoctype(config.xhtmlNoDoctype());
        if (templateBytes != null) {
            htmlDoc.readFromTemplate(template);
        } else {
            htmlDoc.createHeaderFooter();
        }
        org.w3c.dom.Document htmlDOM = htmlDoc.getContentDOM();
        htmlDoc.getTitleNode().appendChild(htmlDOM.createTextNode(dir.getName()));
        Element meta = htmlDOM.createElement("meta");
        meta.setAttribute("http-equiv", "Content-Type");
        meta.setAttribute("content", "text/html; charset=" + htmlDoc.getEncoding().toLowerCase());
        htmlDoc.getHeadNode().appendChild(meta);
        if (config.xhtmlCustomStylesheet().length() > 0) {
            Element htmlStyle = htmlDOM.createElement("link");
            htmlStyle.setAttribute("rel", "stylesheet");
            htmlStyle.setAttribute("type", "text/css");
            htmlStyle.setAttribute("media", "all");
            htmlStyle.setAttribute("href", config.xhtmlCustomStylesheet());
            htmlDoc.getHeadNode().appendChild(htmlStyle);
        }
        Element header = htmlDoc.getHeaderNode();
        if (config.getXhtmlUplink().length() > 0) {
            Element a = htmlDOM.createElement("a");
            a.setAttribute("href", config.getXhtmlUplink());
            a.appendChild(htmlDOM.createTextNode(l10n.get(L10n.UP)));
            header.appendChild(htmlDOM.createTextNode("["));
            header.appendChild(a);
            header.appendChild(htmlDOM.createTextNode("] "));
        } else {
            header.appendChild(htmlDOM.createTextNode("[" + l10n.get(L10n.UP) + "]"));
        }
        header.appendChild(htmlDOM.createElement("hr"));
        Element footer = htmlDoc.getFooterNode();
        footer.appendChild(htmlDOM.createElement("hr"));
        if (config.getXhtmlUplink().length() > 0) {
            Element a = htmlDOM.createElement("a");
            a.setAttribute("href", config.getXhtmlUplink());
            a.appendChild(htmlDOM.createTextNode(l10n.get(L10n.UP)));
            footer.appendChild(htmlDOM.createTextNode("["));
            footer.appendChild(a);
            footer.appendChild(htmlDOM.createTextNode("] "));
        } else {
            footer.appendChild(htmlDOM.createTextNode("[" + l10n.get(L10n.UP) + "]"));
        }
        if (sOutPathName.length() > 0) {
            Element h1 = htmlDOM.createElement("h1");
            htmlDoc.getContentNode().appendChild(h1);
            String sSeparator = File.separator;
            if (sSeparator.equals("\\")) {
                sSeparator = "\\\\";
            }
            String sHeading = sOutPathName.substring(0, sOutPathName.length() - 1).replaceAll(sSeparator, " - ");
            h1.appendChild(htmlDOM.createTextNode(sHeading));
        }
        File[] contents = dir.listFiles();
        int nLen = contents.length;
        Collator collator = Collator.getInstance(new Locale(sDefaultLang, sDefaultCountry));
        for (int i = 0; i < nLen; i++) {
            for (int j = i + 1; j < nLen; j++) {
                File entryi = contents[i];
                File entryj = contents[j];
                if (collator.compare(entryi.getName(), entryj.getName()) > 0) {
                    contents[i] = entryj;
                    contents[j] = entryi;
                }
            }
        }
        if (bRecurse) {
            boolean bUseIcon = config.getXhtmlDirectoryIcon().length() > 0;
            for (int i = 0; i < nLen; i++) {
                if (contents[i].isDirectory()) {
                    config.setOption("xhtml_uplink", "../index.html");
                    convertDirectory(contents[i], sOutPathName + contents[i].getName() + File.separator);
                    Element p = htmlDOM.createElement("p");
                    htmlDoc.getContentNode().appendChild(p);
                    if (bUseIcon) {
                        Element img = htmlDOM.createElement("img");
                        p.appendChild(img);
                        img.setAttribute("src", config.getXhtmlDirectoryIcon());
                        img.setAttribute("alt", "Directory icon");
                        p.appendChild(htmlDOM.createTextNode(" "));
                    }
                    Element a = htmlDOM.createElement("a");
                    p.appendChild(a);
                    a.setAttribute("href", Misc.makeHref(contents[i].getName() + "/index.html"));
                    a.appendChild(htmlDOM.createTextNode(contents[i].getName()));
                }
            }
        }
        boolean bUseIcon = config.getXhtmlDocumentIcon().length() > 0;
        for (int i = 0; i < nLen; i++) {
            if (contents[i].isFile()) {
                config.setOption("xhtml_uplink", "index.html");
                String sLinkFile = convertFile(contents[i], sOutPathName);
                if (sLinkFile != null) {
                    Element p = htmlDOM.createElement("p");
                    htmlDoc.getContentNode().appendChild(p);
                    if (bUseIcon) {
                        Element img = htmlDOM.createElement("img");
                        p.appendChild(img);
                        img.setAttribute("src", config.getXhtmlDocumentIcon());
                        img.setAttribute("alt", "Document icon");
                        p.appendChild(htmlDOM.createTextNode(" "));
                    }
                    Element a = htmlDOM.createElement("a");
                    p.appendChild(a);
                    a.setAttribute("href", Misc.makeHref(sLinkFile));
                    a.appendChild(htmlDOM.createTextNode(Misc.removeExtension(sLinkFile)));
                }
            }
        }
        File outdir = new File(sOutPathName);
        if (!outdir.exists()) {
            outdir.mkdirs();
        }
        String sFileName = sOutPathName + htmlDoc.getFileName();
        try {
            FileOutputStream fos = new FileOutputStream(sFileName);
            htmlDoc.write(fos);
            fos.flush();
            fos.close();
        } catch (Exception writeExcept) {
            System.out.println("\nThere was an error writing out file <" + sFileName + ">");
            writeExcept.printStackTrace();
        }
    }
}
