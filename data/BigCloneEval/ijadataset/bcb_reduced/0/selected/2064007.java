package at.ac.tuwien.law.yaplaf.plugin.input.pdfreader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.log4j.Logger;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import at.ac.tuwien.law.yaplaf.entities.Paper;
import at.ac.tuwien.law.yaplaf.exceptions.MissingPropertyException;
import at.ac.tuwien.law.yaplaf.interfaces.Input;

/**
 * 
 * Zum Lesen von Zip-Dateien, die Papers als PDF enthalten
 * 
 * @author TU Wien, Daten- & Informatikrecht, YAPLAF, Markus Huber
 * 
 * 
 * 
 */
public class PdfReader implements Input {

    private static Logger logger = Logger.getLogger(PdfReader.class);

    private static ResourceBundle messages = ResourceBundle.getBundle("at.ac.tuwien.law.yaplaf.plugin.input.pdfreader.messages");

    Properties props = null;

    /**
	 * Startet den Filereader, vorher m�ssen Properties �bergeben werden
	 * 
	 * @throws MissingPropertyException
	 * @return LinkedList<Paper>
	 */
    public List<Paper> execute() throws MissingPropertyException {
        LinkedList<Paper> papers = new LinkedList<Paper>();
        try {
            String filename = props.getProperty("filename");
            if (filename == null) throw new MissingPropertyException();
            ZipFile zipFile = new ZipFile(filename);
            Enumeration entries = zipFile.entries();
            File f = new File(filename);
            String folderName = f.getParent() + File.separator + f.getName().substring(0, f.getName().lastIndexOf("."));
            File folder = new File(folderName);
            if (!folder.exists()) folder.mkdirs();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                InputStream zin = zipFile.getInputStream(zipEntry);
                String name = zipEntry.getName();
                int idx;
                if ((idx = name.lastIndexOf("/")) != -1) {
                    name = name.substring(idx + 1);
                }
                if (!name.equals("config.xml")) {
                    File file = new File(folderName + File.separator + name);
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    Vector<String> words = new Vector<String>();
                    for (int i = 0, last = 0; ; ) {
                        i = name.indexOf('_', last);
                        if (i != -1) {
                            words.addElement(name.substring(last, i));
                            last = i + 1;
                        } else {
                            break;
                        }
                    }
                    int readBytes = 0;
                    byte[] datos = new byte[(int) 8 * 1024];
                    while ((readBytes = zin.read(datos, 0, datos.length)) > 0) {
                        out.write(datos, 0, readBytes);
                    }
                    zin.close();
                    out.close();
                    PDFTextStripper pts = new PDFTextStripper();
                    String paperText = pts.getText(PDDocument.load(file));
                    Paper paper = new Paper();
                    paper.setMatriculationNumber((String) words.get(0));
                    paper.setNachname((String) words.get(1));
                    paper.setVorname((String) words.get(2));
                    paper.setPaperText(paperText);
                    papers.add(paper);
                }
            }
            logger.info("Datei erfolgreich eingelesen!");
            return papers;
        } catch (Throwable t) {
            logger.info("Datei konnte nicht ge�ffnet werden!");
            logger.info(t);
            t.printStackTrace();
            return null;
        }
    }

    /**
	 * @return Properties
	 */
    public Properties getProperties() {
        logger.warn(messages.getString("getProperties"));
        return props;
    }

    /**
	 * @return Map<String, String>
	 */
    public Map<String, String> getPropertiesDescription() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("filename", "Zip-Datei, die Papers als PDF im Format matrikelnr_nachname_vorname.pdf enth�lt");
        return map;
    }

    /**
	 * @param props
	 *            setzt die Eigenschaften, die ueber getPropertiesDescription()
	 *            abgefragt werden koennen.
	 */
    public void setProperties(Properties props) throws MissingPropertyException {
        this.props = props;
    }
}
