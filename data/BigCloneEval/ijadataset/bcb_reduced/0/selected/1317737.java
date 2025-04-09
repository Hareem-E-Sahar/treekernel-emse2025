package net.sf.genedator.writers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSDataFileWriter implements DataWriter {

    private String fileName;

    private XSSFWorkbook workbook;

    private CreationHelper creationHelper;

    private XSSFSheet sheet;

    private static final String XML_ENCODING = "UTF-8";

    private String sheetRef;

    @Override
    public void saveData(List<String[]> data) {
        init();
        save(data);
    }

    @Override
    public void saveDataWithHeaders(List<String[]> data, String[] headersNames) {
        init();
        Row row = sheet.createRow(0);
        for (int i = 0; i < headersNames.length; i++) {
            row.createCell(i).setCellValue(headersNames[i]);
        }
        save(data);
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private void init() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("new sheet");
        sheetRef = sheet.getPackagePart().getPartName().getName();
        FileOutputStream os;
        try {
            os = new FileOutputStream(fileName + "_");
            workbook.write(os);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save(List<String[]> data) {
        File tmp = null;
        try {
            tmp = File.createTempFile("sheet", ".xml");
            Writer fw = new OutputStreamWriter(new FileOutputStream(tmp), XML_ENCODING);
            generate(fw, data);
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(fileName);
            substitute(new File(fileName + "_"), tmp, sheetRef.substring(1), out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generate(Writer out, List<String[]> data) throws IOException {
        SpreadsheetWriter sw = new SpreadsheetWriter(out);
        sw.beginSheet();
        int counter = 0;
        for (String[] array : data) {
            sw.insertRow(counter++);
            for (int i = 0; i < array.length; i++) {
                sw.createCell(i, array[i]);
            }
            sw.endRow();
        }
        sw.endSheet();
    }

    private static void substitute(File zipfile, File tmpfile, String entry, OutputStream out) throws IOException {
        ZipFile zip = new ZipFile(zipfile);
        ZipOutputStream zos = new ZipOutputStream(out);
        @SuppressWarnings("unchecked") Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
        while (en.hasMoreElements()) {
            ZipEntry ze = en.nextElement();
            if (!ze.getName().equals(entry)) {
                zos.putNextEntry(new ZipEntry(ze.getName()));
                InputStream is = zip.getInputStream(ze);
                copyStream(is, zos);
                is.close();
            }
        }
        zos.putNextEntry(new ZipEntry(entry));
        InputStream is = new FileInputStream(tmpfile);
        copyStream(is, zos);
        is.close();
        zos.close();
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] chunk = new byte[1024];
        int count;
        while ((count = in.read(chunk)) >= 0) {
            out.write(chunk, 0, count);
        }
    }
}
