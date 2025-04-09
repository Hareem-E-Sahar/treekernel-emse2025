package cz.cvut.phone.pp.dto;

import cz.cvut.phone.pp.constants.LanguageConstants;
import cz.cvut.phone.pp.constants.XMLConstants;
import cz.cvut.phone.pp.exception.WrongDataException;
import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import jxl.Cell;
import jxl.CellType;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * Most important class of PhoneParser. Takes care about recognizing the files
 * and calling the right classes to parse them.
 * @author Jiří Havelka
 */
public class Reader {

    /**
     * Postfix of csv files.
     */
    public static String CSVPOSTFIX = ".csv";

    /**
     * Postfix of xls files.
     */
    public static String XLSPOSTFIX = ".xls";

    /**
     * Postfix of zip archives.
     */
    public static String ZIPPOSTFIX = ".zip";

    /**
     * Prefix of temporary files.
     */
    public static String TMPPREFIX = "tmp";

    /**
     * Name of parsed file.
     */
    private String fileName;

    /**
     * Mime-type of parsed file.
     */
    private String mime;

    /**
     * Data of parsed file.
     */
    private byte[] data;

    /**
     * Buffered reader for data to be parsed.
     */
    private BufferedReader br;

    /**
     * Result of parsing.
     */
    private PhoneParserResultDTO result;

    /**
     * Logger initialization.
     */
    private Logger log = Logger.getLogger(LanguageConstants.LOGGER_NAME);

    /**
     * The only constructor
     * @param fileName Name of input file.
     * @param mime Type of input file.
     * @param data File data in form of byte array.
     * @throws java.lang.Exception
     */
    public Reader(String fileName, String mime, byte[] data) throws Exception {
        this.fileName = fileName;
        this.mime = mime;
        this.data = data;
        this.result = new PhoneParserResultDTO();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        if (fileName.endsWith(CSVPOSTFIX)) {
            this.br = new BufferedReader(new InputStreamReader(in));
            parse(fileName);
            br.close();
        } else {
            if (fileName.endsWith(XLSPOSTFIX)) {
                Workbook wb = Workbook.getWorkbook(in);
                for (int sh = 0; sh < wb.getNumberOfSheets(); sh++) {
                    File newCsv = File.createTempFile(TMPPREFIX, CSVPOSTFIX);
                    FileWriter out = new FileWriter(newCsv);
                    try {
                        xlsToCsv(wb, out, sh);
                    } catch (Exception e) {
                        result.addWrongDataFile(fileName + "/sheet_" + sh);
                    }
                    out.close();
                    this.br = new BufferedReader(new FileReader(newCsv));
                    parse(fileName + "/sheet_" + sh);
                    br.close();
                    newCsv.delete();
                }
            } else {
                if (fileName.endsWith(ZIPPOSTFIX)) {
                    File f = File.createTempFile(TMPPREFIX, ZIPPOSTFIX);
                    FileOutputStream out = new FileOutputStream(f);
                    ZipFile zf;
                    ZipInputStream zin;
                    try {
                        byte[] buffer = new byte[1024];
                        int r = 0;
                        while ((r = in.read(buffer)) > -1) {
                            out.write(buffer, 0, r);
                        }
                        zf = new ZipFile(f.getAbsoluteFile());
                        ZipEntry ze;
                        zin = new ZipInputStream(new ByteArrayInputStream(data));
                        for (Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
                            ze = ((ZipEntry) entries.nextElement());
                            zin.getNextEntry();
                            ByteArrayOutputStream newByte = new ByteArrayOutputStream();
                            buffer = new byte[1024];
                            r = 0;
                            while ((r = zin.read(buffer)) > -1) {
                                newByte.write(buffer, 0, r);
                            }
                            String newFileName = fileName + "/" + ze.toString();
                            Reader insideZip = new Reader(newFileName, mime, newByte.toByteArray());
                            result.mergeWith(insideZip.result);
                            newByte.close();
                        }
                        zin.close();
                        zf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            out.close();
                            f.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    File f = new File(fileName);
                    if (f.isDirectory()) {
                        File[] files = f.listFiles();
                        byte[] buffer = new byte[1024];
                        ByteArrayOutputStream newByte;
                        FileInputStream newIn;
                        for (File ff : files) {
                            newIn = new FileInputStream(ff);
                            newByte = new ByteArrayOutputStream();
                            int r = 0;
                            while ((r = newIn.read(buffer)) > -1) {
                                newByte.write(buffer, 0, r);
                            }
                            Reader insideDir = new Reader(ff.getName(), mime, newByte.toByteArray());
                            newByte.close();
                            newIn.close();
                            result.mergeWith(insideDir.result);
                        }
                    } else {
                        result.addWrongFile(fileName);
                    }
                    in.close();
                }
            }
        }
    }

    /**
     * Method deciding which parsing class to call. And calling it
     * @param name Name of file which is read.
     * @throws java.lang.Exception
     */
    private void parse(String name) throws Exception {
        try {
            String line;
            line = br.readLine();
            File templateFolder = new File("templates");
            File[] templates = templateFolder.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    if (name.endsWith(".xml")) {
                        return true;
                    }
                    return false;
                }
            });
            SAXBuilder builder = new SAXBuilder();
            boolean templateFound = false;
            if (templates.length == 0) log.debug("NO TEMPLATES! Put them to: " + templateFolder.getAbsolutePath()); else for (int i = 0; i < templates.length; i++) {
                Document doc = builder.build(templates[i]);
                if (line.startsWith(doc.getRootElement().getAttributeValue(XMLConstants.FIRST_LINE_BEGIN_XML))) {
                    templateFound = true;
                    log.debug("TEMPLATE: " + templates[i].getName());
                    new Parse(br, result, doc);
                    break;
                }
            }
            if (!templateFound) {
                log.debug("NO template suits!");
                throw new WrongDataException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("READER 241");
            result.addWrongDataFile(name);
        }
    }

    /**
     * Method converting all lists of .xls file into .csv file
     * @param wb Input - .xls workbook.
     * @param out Output - FileWriter to .csv file.
     * @throws java.lang.Exception
     */
    private void xlsToCsv(Workbook wb, FileWriter out) throws Exception {
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            processSheet(wb.getSheet(i), out);
        }
    }

    /**
     * Method converting one list of .xls file into .csv file
     * @param wb Input - .xls workbook.
     * @param out Output - FileWriter to .csv file.
     * @param sheetNr Number of sheet we want to convert.
     * @throws java.lang.Exception
     */
    private void xlsToCsv(Workbook wb, FileWriter out, int sheetNr) throws Exception {
        if ((wb.getSheets().length) > sheetNr) {
            processSheet(wb.getSheet(sheetNr), out);
        } else {
            throw new WrongDataException();
        }
    }

    /**
     * Method converting .xls sheet into .csv file
     * @param sh Sheet of .xls workbook.
     * @param out Output - FileWriter to .csv file.
     * @throws java.lang.Exception
     */
    private void processSheet(Sheet sh, FileWriter out) throws Exception {
        int rows = sh.getRows();
        int cols = sh.getColumns();
        Cell[] lineparts = new Cell[cols];
        String line = "";
        double number;
        for (int i = 0; i < rows; i++) {
            lineparts = sh.getRow(i);
            line = lineparts[0].getContents();
            for (int j = 1; j < lineparts.length; j++) {
                if (lineparts[j].getType() == CellType.NUMBER) {
                    number = ((NumberCell) lineparts[j]).getValue();
                    if (number == (int) number) {
                        line += ";" + (int) number;
                    } else {
                        line += ";" + number;
                    }
                } else {
                    line += ";" + lineparts[j].getContents();
                }
            }
            line += "\n";
            out.write(line);
        }
    }

    /**
     * Getter returning parameter result;
     * @return Result of parsing.
     */
    public PhoneParserResultDTO getResult() {
        return result;
    }
}
