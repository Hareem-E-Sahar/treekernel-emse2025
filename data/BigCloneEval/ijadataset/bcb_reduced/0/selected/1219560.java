package lrf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lrf.conv.BaseRenderer;
import lrf.docx.DOCX2EPUB;
import lrf.epub.EPUBMetaData;
import lrf.gui.EPUBViewer;
import lrf.merge.MergeEPUBAndTOC;
import lrf.merge.MergePDFAndTOC;
import lrf.objects.Book;
import lrf.objects.tags.Tag;
import lrf.pdf.PDF2SVG;
import lrf.pdf.PDFSerializer;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.xml.xmp.XmpWriter;

public class RecurseDirs {

    public static boolean xml, pdf, rtf, html, rdi, epub, noo, assvg;

    public static String catpar = null;

    public static ZipOutputStream zout = null;

    public static String mergedFile = null;

    public static boolean lrfSize = true;

    Hashtable<String, String> repl = new Hashtable<String, String>();

    public static void main(String args[]) {
        try {
            new RecurseDirs(args);
        } catch (Exception e) {
            e.printStackTrace();
            tryToCloseZip();
        }
    }

    private static void tryToCloseZip() {
        try {
            if (zout != null) {
                zout.close();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    String action = null;

    String argumentos[] = null;

    File root = null;

    public void UsageAndExit() {
        InputStream is;
        is = this.getClass().getResourceAsStream("/Usage.txt");
        int b;
        try {
            while ((b = is.read()) != -1) {
                System.out.print((char) b);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
	 * @param args
	 * @throws Exception
	 */
    public RecurseDirs(String args[]) throws Exception {
        argumentos = args;
        if (args.length == 0) UsageAndExit();
        action = args[0];
        boolean done = false;
        try {
            if (action.equalsIgnoreCase("convertLRF")) {
                done = convertActionParams(args);
            } else if (action.equalsIgnoreCase("convertPDF")) {
                done = convertPDFActionParams(args);
            } else if (action.equalsIgnoreCase("convertDOCX")) {
                done = convertDOCXActionParams(args);
            } else if (action.equalsIgnoreCase("view")) {
                done = viewActionParams(args);
            } else if (action.equalsIgnoreCase("updfmd")) {
                done = updfmdActionParams(args);
            } else if (action.equalsIgnoreCase("mergePDF")) {
                done = mergePDFActionParams(args);
            } else if (action.equalsIgnoreCase("mergeEPUB")) {
                done = mergeEPUBActionParams(args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!done) UsageAndExit();
    }

    private boolean viewActionParams(String[] args) throws Exception {
        boolean done;
        EPUBViewer.main(args);
        done = true;
        return done;
    }

    private boolean mergeEPUBActionParams(String[] args) throws Exception {
        String aut = null, tit = null, lang = "en";
        for (int i = 1; i < args.length; i++) {
            if (i == 1) {
                root = new File(args[i]);
                continue;
            }
            if (args[i].equalsIgnoreCase("-o")) {
                mergedFile = args[++i];
                continue;
            }
            if (args[i].equalsIgnoreCase("-a")) {
                aut = args[++i];
                continue;
            }
            if (args[i].equalsIgnoreCase("-t")) {
                tit = args[++i];
                continue;
            }
            if (args[i].equalsIgnoreCase("-l")) {
                lang = args[++i];
                continue;
            }
        }
        if (root == null || mergedFile == null || tit == null || aut == null) {
            UsageAndExit();
        }
        MergeEPUBAndTOC m = new MergeEPUBAndTOC(new File(mergedFile), tit, aut, lang);
        mergeEPUBAction(root, m);
        m.close();
        return true;
    }

    private boolean mergeEPUBAction(File d, MergeEPUBAndTOC m) {
        File list[] = d.listFiles();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory()) mergeEPUBAction(list[i], m); else if (list[i].getName().toLowerCase().endsWith(".epub")) try {
                System.out.print("Adding Book " + list[i].getName() + ": ");
                m.appendBook(list[i], null);
                System.out.println("OK.");
            } catch (Exception e) {
                System.out.print(" Error: ");
                e.printStackTrace(System.out);
                return false;
            }
        }
        return true;
    }

    private boolean mergePDFActionParams(String[] args) throws Exception {
        boolean done;
        for (int i = 1; i < args.length; i++) {
            if (i == 1) {
                root = new File(args[i]);
                continue;
            }
            if (args[i].equalsIgnoreCase("-o")) {
                mergedFile = args[++i];
                continue;
            }
        }
        if (root == null || mergedFile == null) {
            UsageAndExit();
        }
        MergePDFAndTOC.wBuild(root, mergedFile);
        done = true;
        return done;
    }

    private boolean updfmdActionParams(String[] args) throws Exception {
        boolean done;
        for (int i = 1; i < args.length; i++) {
            if (i == 1) {
                root = new File(args[i]);
                continue;
            }
        }
        if (root == null) {
            UsageAndExit();
        }
        updfmdAction(root);
        done = true;
        return done;
    }

    private boolean convertPDFActionParams(String[] args) {
        File dirOut = null;
        String lang = "en";
        for (int i = 1; i < args.length; i++) {
            if (i == 1) {
                root = new File(args[i]);
                continue;
            }
            if (args[i].equalsIgnoreCase("-d")) {
                dirOut = new File(args[++i]);
                if (dirOut.exists() && !dirOut.isDirectory()) {
                    throw new RuntimeException(args[i] + " is not a directory.");
                }
                if (!dirOut.exists()) dirOut.mkdirs();
                continue;
            }
            if (args[i].equalsIgnoreCase("-noo")) noo = true;
            if (args[i].equalsIgnoreCase("-svg")) assvg = true;
            if (args[i].equalsIgnoreCase("-l")) lang = args[++i];
            if (args[i].equalsIgnoreCase("-noe")) EPUBMetaData.doNotEmbedOTFFonts = true;
            if (args[i].equalsIgnoreCase("-nopb")) BaseRenderer.noPageBreakEmit = true;
        }
        if (assvg) {
            PDF2SVG.dirDest = dirOut;
            PDF2SVG.dirOrig = root;
            PDF2SVG.noo = noo;
            PDF2SVG.recurse(root, lang);
        } else {
            PDFSerializer.dirDest = dirOut;
            PDFSerializer.dirOrig = root;
            PDFSerializer.recurse(root, lang);
        }
        return true;
    }

    private boolean convertDOCXActionParams(String[] args) {
        File dirOut = null;
        String lang = "en";
        for (int i = 1; i < args.length; i++) {
            if (i == 1) {
                root = new File(args[i]);
                continue;
            }
            if (args[i].equalsIgnoreCase("-d")) {
                dirOut = new File(args[++i]);
                if (dirOut.exists() && !dirOut.isDirectory()) {
                    throw new RuntimeException(args[i] + " is not a directory.");
                }
                if (!dirOut.exists()) dirOut.mkdirs();
                continue;
            }
            if (args[i].equalsIgnoreCase("-noo")) noo = true;
            if (args[i].equalsIgnoreCase("-l")) lang = args[++i];
            if (args[i].equalsIgnoreCase("-noe")) EPUBMetaData.doNotEmbedOTFFonts = true;
            if (args[i].equalsIgnoreCase("-nopb")) BaseRenderer.noPageBreakEmit = true;
        }
        DOCX2EPUB conv = new DOCX2EPUB(root, dirOut == null ? root : dirOut);
        conv.recurse(root);
        return true;
    }

    private boolean convertActionParams(String[] args) throws FileNotFoundException, Exception, IOException {
        boolean done;
        File dirOut = null;
        for (int i = 1; i < args.length; i++) {
            if (i == 1) {
                root = new File(args[i]);
                continue;
            }
            if (args[i].equalsIgnoreCase("-rf")) {
                Tag.fontSizeSigma = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equalsIgnoreCase("-repl")) {
                repl.put(args[i + 1], args[i + 2]);
                i += 2;
                continue;
            }
            if (args[i].equalsIgnoreCase("-z")) {
                zout = new ZipOutputStream(new FileOutputStream(args[++i]));
                continue;
            }
            if (args[i].equalsIgnoreCase("-d")) {
                dirOut = new File(args[++i]);
                if (dirOut.exists() && !dirOut.isDirectory()) {
                    throw new RuntimeException(args[i] + " is not a directory.");
                }
                if (!dirOut.exists()) dirOut.mkdirs();
                continue;
            }
            if (args[i].equalsIgnoreCase("-xml")) xml = true;
            if (args[i].equalsIgnoreCase("-pdf")) pdf = true;
            if (args[i].equalsIgnoreCase("-rtf")) rtf = true;
            if (args[i].equalsIgnoreCase("-html")) {
                html = true;
            }
            if (args[i].equalsIgnoreCase("-epub")) epub = true;
            if (args[i].equalsIgnoreCase("-A4")) {
                lrfSize = false;
            }
            if (args[i].equalsIgnoreCase("-catpar")) {
                catpar = args[++i];
            }
            if (args[i].equalsIgnoreCase("-usep")) {
                EPUBMetaData.usep = true;
            }
            if (args[i].equalsIgnoreCase("-noo")) noo = true;
            if (args[i].equalsIgnoreCase("-nopb")) BaseRenderer.noPageBreakEmit = true;
            if (args[i].equalsIgnoreCase("-noe")) EPUBMetaData.doNotEmbedOTFFonts = true;
            if (args[i].equalsIgnoreCase("-cfont")) {
                BaseRenderer.embeddedFont = args[++i];
                BaseRenderer.ttcNumber = args[++i];
                BaseFont userbf = BaseFont.createFont(BaseRenderer.embeddedFont + "," + BaseRenderer.ttcNumber, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                Book.bfonts.put("user", userbf);
            }
        }
        if (root == null || (!xml && !pdf && !rtf && !html && !epub)) {
            UsageAndExit();
        }
        if (html && zout != null) {
            UsageAndExit();
        }
        if (dirOut == null) {
            dirOut = root;
        }
        convertAction(dirOut, root);
        if (zout != null) {
            zout.close();
        }
        done = true;
        return done;
    }

    private void dumpToPDF(File dirOut, String oname, String ff, Book book, Hashtable<String, String> repl) {
        String ret = null;
        try {
            System.out.print("Creating " + ff + ".pdf...");
            if (zout != null) {
                zout.putNextEntry(new ZipEntry(ff + ".pdf"));
                ret = book.getPDF(zout, lrfSize, repl);
            } else if (dirOut != null) {
                File nf = new File(dirOut, ff + ".pdf");
                nf.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(nf);
                ret = book.getPDF(fos, lrfSize, repl);
                fos.close();
            } else {
                FileOutputStream fos = new FileOutputStream(oname + ".pdf");
                ret = book.getPDF(fos, lrfSize, repl);
                fos.close();
            }
            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("PDF Error");
        }
    }

    private void dumpToRTF(File dirOut, String oname, String ff, Book book, Hashtable<String, String> repl) {
        try {
            System.out.print("Creating " + ff + ".rtf...");
            if (zout != null) {
                zout.putNextEntry(new ZipEntry(ff + ".rtf"));
                book.getRTF(zout, repl);
            } else if (dirOut != null) {
                File nf = new File(dirOut, ff + ".rtf");
                nf.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(nf);
                book.getRTF(fos, repl);
                fos.close();
            } else {
                FileOutputStream fos = new FileOutputStream(oname + ".rtf");
                book.getRTF(fos, repl);
                fos.close();
            }
            System.out.println("RTF Ok");
        } catch (Exception e) {
            System.out.println("RTF Error:");
            e.printStackTrace();
        }
    }

    private void dumpToHTML(File dirOut, String oname, String ff, Book book, Hashtable<String, String> repl) {
        try {
            System.out.print("Creating " + ff + ".html...");
            File images = new File(dirOut, ff);
            if (zout != null) {
                zout.putNextEntry(new ZipEntry(ff + ".html"));
                book.getHTML(zout, images, images.getName() + "/", repl);
            } else if (dirOut != null) {
                File nf = new File(dirOut, ff + ".html");
                nf.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(nf);
                book.getHTML(fos, images, images.getName() + "/", repl);
                fos.close();
            } else {
                FileOutputStream fos = new FileOutputStream(oname + ".html");
                book.getHTML(fos, images, images.getName() + "/", repl);
                fos.close();
            }
            System.out.println("HTML Ok");
        } catch (Exception e) {
            System.out.println("HTML Error:");
            e.printStackTrace();
        }
    }

    private void dumpToXML(File dirOut, String oname, String ff, Book book, Hashtable<String, String> repl) {
        try {
            System.out.print("Creating " + ff + ".xml...");
            if (zout != null) {
                zout.putNextEntry(new ZipEntry(ff + ".xml"));
                book.getXML(zout, repl);
            } else if (dirOut != null) {
                File nf = new File(dirOut, ff + ".xml");
                nf.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(nf);
                book.getXML(fos, repl);
                fos.close();
            } else {
                FileOutputStream fos = new FileOutputStream(oname + ".xml");
                book.getXML(fos, repl);
                fos.close();
            }
            System.out.println("XML Ok");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("XML Error");
        }
    }

    private void dumpToEpub(File dirOut, String oname, String ff, Book book, Hashtable<String, String> repl) {
        try {
            File outf;
            System.out.print("Creating " + ff + ".epub...");
            if (dirOut != null) {
                outf = new File(dirOut, ff + ".epub");
                outf.getParentFile().mkdirs();
            } else {
                outf = new File(oname + ".epub");
            }
            book.getEPUB(outf.getCanonicalPath(), repl, catpar);
            System.out.println("EPUB Ok");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EPUB Error");
        }
    }

    public void convertAction(File dirOut, File d) throws Exception {
        String[] files = d.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(d, files[i]);
            if (file.isDirectory()) {
                convertAction(dirOut, file);
            } else if (files[i].toLowerCase().endsWith(".lrf")) {
                String fi = files[i];
                String oname = d.getAbsolutePath() + File.separator + fi.substring(0, fi.length() - 4);
                String ff = oname.substring(root.getAbsolutePath().length() + 1);
                String aux = d.getAbsolutePath() + File.separator + fi;
                File auxFile = new File(aux);
                boolean genPDF = pdf, genHTML = html, genRTF = rtf, genXML = xml, genEPUB = epub;
                if (noo) {
                    if (dirOut == null) {
                        if (genPDF && new File(oname + ".pdf").exists()) genPDF = false;
                        if (genRTF && new File(oname + ".rtf").exists()) genRTF = false;
                        if (genXML && new File(oname + ".xml").exists()) genXML = false;
                        if (genEPUB && new File(oname + ".epub").exists()) genEPUB = false;
                        if (genHTML && new File(oname + ".html").exists()) genHTML = false;
                    } else {
                        if (genPDF && new File(dirOut, ff + ".pdf").exists()) genPDF = false;
                        if (genRTF && new File(dirOut, ff + ".rtf").exists()) genRTF = false;
                        if (genXML && new File(dirOut, ff + ".xml").exists()) genXML = false;
                        if (genEPUB && new File(dirOut, ff + ".epub").exists()) genEPUB = false;
                        if (genHTML && new File(dirOut, ff + ".html").exists()) genHTML = false;
                    }
                }
                Book book = null;
                if (genPDF || genHTML || genRTF || genXML || genEPUB) book = new Book(auxFile);
                if (genPDF) {
                    dumpToPDF(dirOut, oname, ff, book, repl);
                }
                if (genHTML) {
                    dumpToHTML(dirOut, oname, ff, book, repl);
                }
                if (genRTF) {
                    dumpToRTF(dirOut, oname, ff, book, repl);
                }
                if (genXML) {
                    dumpToXML(dirOut, oname, ff, book, repl);
                }
                if (genEPUB) {
                    dumpToEpub(dirOut, oname, ff, book, repl);
                }
                if ((pdf && !genPDF)) System.out.println("Skipping " + ff + ".pdf...");
                if ((html && !genHTML)) System.out.println("Skipping " + ff + ".html...");
                if ((xml && !genXML)) System.out.println("Skipping " + ff + ".xml...");
                if ((rtf && !genRTF)) System.out.println("Skipping " + ff + ".rtf...");
                if ((epub && !genEPUB)) System.out.println("Skipping " + ff + ".epub...");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void updfmdAction(File d) throws Exception {
        if (d == null) return;
        String[] files = d.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(d, files[i]);
            if (file.isDirectory()) {
                updfmdAction(file);
            } else if (files[i].toLowerCase().endsWith(".pdf")) {
                String fn = files[i];
                String auth = "", titl = fn.substring(0, fn.length() - 4);
                int pos;
                if ((pos = fn.indexOf("-")) > 0 && pos < fn.length() - 1) {
                    auth = fn.substring(0, pos).trim();
                    titl = fn.substring(pos + 1).trim();
                }
                try {
                    String absPath = d.getAbsolutePath() + "/" + fn;
                    System.out.println("Stampering " + absPath);
                    PdfReader reader = new PdfReader(absPath);
                    FileOutputStream fos = new FileOutputStream(absPath + "-new");
                    PdfStamper stamper = new PdfStamper(reader, fos);
                    HashMap<String, String> info = reader.getInfo();
                    info.put("Author", auth);
                    info.put("Title", titl);
                    String prod = info.get("Producer");
                    if (prod == null || prod.indexOf("LRFTools") < 0) info.put("Producer", prod + " and LRFTools");
                    stamper.setMoreInfo(info);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    XmpWriter xmp = new XmpWriter(baos, info);
                    xmp.close();
                    stamper.setXmpMetadata(baos.toByteArray());
                    stamper.close();
                    File old = new File(absPath);
                    File nue = new File(absPath + "-new");
                    old.delete();
                    nue.renameTo(old);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
