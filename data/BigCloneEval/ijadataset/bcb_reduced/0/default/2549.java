import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class main {

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static void main(String[] argv) {
        String prefix = "c:/kjames/";
        if (false) {
            System.err.println("Usage:   java ExampleDomShowNodes <XmlFile> <TagName>");
            System.err.println("Example: java ExampleDomShowNodes MyXmlFile.xml Button");
            System.exit(1);
        }
        try {
            PrintStream p;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("Schlachter1951_rev3a.xml"));
            NodeList booknodes = document.getElementsByTagName("BIBLEBOOK");
            NodeList chapters;
            Element chapter;
            Element vers;
            NodeList verses;
            NamedNodeMap attributes;
            String index = Integer.toString(booknodes.getLength());
            Integer i, c = 0;
            FileOutputStream out;
            deleteDirectory(new File(prefix));
            new File(prefix).mkdir();
            File chapterfile;
            for (i = 0; booknodes.item(i) != null; i++) {
                attributes = booknodes.item(i).getAttributes();
                chapter = (Element) booknodes.item(i);
                chapters = chapter.getElementsByTagName("CHAPTER");
                index = index + "," + attributes.getNamedItem("bname").getNodeValue() + "," + attributes.getNamedItem("bnumber").getNodeValue() + "," + chapters.getLength();
                new File(prefix + attributes.getNamedItem("bnumber").getNodeValue()).mkdir();
                for (c = 0; chapters.item(c) != null; c++) {
                    vers = (Element) chapters.item(c);
                    verses = vers.getElementsByTagName("VERS");
                    index = index + "," + verses.getLength();
                    chapterfile = new File(prefix + attributes.getNamedItem("bnumber").getNodeValue() + "/" + Integer.toString(c + 1) + ".dat");
                    chapterfile.createNewFile();
                    chapterfile = null;
                    out = new FileOutputStream(prefix + attributes.getNamedItem("bnumber").getNodeValue() + "/" + Integer.toString(c + 1) + ".dat");
                    p = new PrintStream(out);
                    for (int k = 0; verses.item(k) != null; k++) p.println(verses.item(k).getTextContent());
                    p.close();
                    p = null;
                    out = null;
                }
                chapters = null;
            }
            File indexfile = new File(prefix + "index.dat");
            indexfile.createNewFile();
            out = new FileOutputStream(prefix + "index.dat");
            p = new PrintStream(out);
            p.print(index);
            p.close();
            System.out.print(index);
        } catch (SAXParseException spe) {
            System.out.println("\n** Parsing error, line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.out.println("   " + spe.getMessage());
            Exception e = (spe.getException() != null) ? spe.getException() : spe;
            e.printStackTrace();
        } catch (SAXException sxe) {
            Exception e = (sxe.getException() != null) ? sxe.getException() : sxe;
            e.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
