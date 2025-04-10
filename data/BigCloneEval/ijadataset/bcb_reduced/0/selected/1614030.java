package net.sourceforge.poi.hssf.dev;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;
import net.sourceforge.poi.poifs.filesystem.POIFSFileSystem;
import net.sourceforge.poi.hssf.record.*;
import net.sourceforge.poi.hssf.model.*;
import net.sourceforge.poi.hssf.usermodel.*;
import net.sourceforge.poi.hssf.util.*;

/**
 * File for HSSF testing/examples
 *
 * THIS IS NOT THE MAIN HSSF FILE!!  This is a util for testing functionality.
 * It does contain sample API usage that may be educational to regular API users.
 *
 * @see #main
 * @author Andrew Oliver (andycoliver@excite.com)
 */
public class HSSF {

    private String filename = null;

    private InputStream stream = null;

    private Record[] records = null;

    protected HSSFWorkbook hssfworkbook = null;

    /**
     * Constructor HSSF - creates an HSSFStream from an InputStream.  The HSSFStream
     * reads in the records allowing modification.
     *
     *
     * @param filename
     *
     * @exception IOException
     *
     */
    public HSSF(String filename) throws IOException {
        this.filename = filename;
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
        hssfworkbook = new HSSFWorkbook(fs);
    }

    /**
     * Constructor HSSF - given a filename this outputs a sample sheet with just
     * a set of rows/cells.
     *
     *
     * @param filename
     * @param write
     *
     * @exception IOException
     *
     */
    public HSSF(String filename, boolean write) throws IOException {
        short rownum = 0;
        FileOutputStream out = new FileOutputStream(filename);
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        HSSFRow r = null;
        HSSFCell c = null;
        HSSFCellStyle cs = wb.createCellStyle();
        HSSFCellStyle cs2 = wb.createCellStyle();
        HSSFCellStyle cs3 = wb.createCellStyle();
        HSSFFont f = wb.createFont();
        HSSFFont f2 = wb.createFont();
        f.setFontHeightInPoints((short) 12);
        f.setColor((short) 0xA);
        f.setBoldweight(f.BOLDWEIGHT_BOLD);
        f2.setFontHeightInPoints((short) 10);
        f2.setColor((short) 0xf);
        f2.setBoldweight(f2.BOLDWEIGHT_BOLD);
        cs.setFont(f);
        cs.setDataFormat(HSSFDataFormat.getFormat("($#,##0_);[Red]($#,##0)"));
        cs2.setBorderBottom(cs2.BORDER_THIN);
        cs2.setFillPattern((short) 1);
        cs2.setFillForegroundColor((short) 0xA);
        cs2.setFont(f2);
        wb.setSheetName(0, "HSSF Test");
        for (rownum = (short) 0; rownum < 300; rownum++) {
            r = s.createRow(rownum);
            if ((rownum % 2) == 0) {
                r.setHeight((short) 0x249);
            }
            for (short cellnum = (short) 0; cellnum < 50; cellnum += 2) {
                c = r.createCell(cellnum, HSSFCell.CELL_TYPE_NUMERIC);
                c.setCellValue(rownum * 10000 + cellnum + (((double) rownum / 1000) + ((double) cellnum / 10000)));
                if ((rownum % 2) == 0) {
                    c.setCellStyle(cs);
                }
                c = r.createCell((short) (cellnum + 1), HSSFCell.CELL_TYPE_STRING);
                c.setCellValue("TEST");
                s.setColumnWidth((short) (cellnum + 1), (short) ((50 * 8) / ((double) 1 / 20)));
                if ((rownum % 2) == 0) {
                    c.setCellStyle(cs2);
                }
            }
        }
        rownum++;
        rownum++;
        r = s.createRow(rownum);
        cs3.setBorderBottom(cs3.BORDER_THICK);
        for (short cellnum = (short) 0; cellnum < 50; cellnum++) {
            c = r.createCell(cellnum, HSSFCell.CELL_TYPE_BLANK);
            c.setCellStyle(cs3);
        }
        s.addMergedRegion(new Region((short) 0, (short) 0, (short) 3, (short) 3));
        s.addMergedRegion(new Region((short) 100, (short) 100, (short) 110, (short) 110));
        s = wb.createSheet();
        wb.setSheetName(1, "DeletedSheet");
        wb.removeSheetAt(1);
        wb.write(out);
        out.close();
    }

    /**
     * Constructor HSSF - takes in file - attempts to read it then reconstruct it
     *
     *
     * @param infile
     * @param outfile
     * @param write
     *
     * @exception IOException
     *
     */
    public HSSF(String infile, String outfile, boolean write) throws IOException {
        this.filename = filename;
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
        hssfworkbook = new HSSFWorkbook(fs);
    }

    /**
     * Method main
     *
     * Given 1 argument takes that as the filename, inputs it and dumps the
     * cell values/types out to sys.out
     *
     * given 2 arguments where the second argument is the word "write" and the
     * first is the filename - writes out a sample (test) spreadsheet (see
     * public HSSF(String filename, boolean write)).
     *
     * given 2 arguments where the first is an input filename and the second
     * an output filename (not write), attempts to fully read in the
     * spreadsheet and fully write it out.
     *
     * given 3 arguments where the first is an input filename and the second an
     * output filename (not write) and the third is "modify1", attempts to read in the
     * spreadsheet, deletes rows 0-24, 74-99.  Changes cell at row 39, col 3 to
     * "MODIFIED CELL" then writes it out.  Hence this is "modify test 1".  If you
     * take the output from the write test, you'll have a valid scenario.
     *
     * @param args
     *
     */
    public static void main(String[] args) {
        if (args.length < 2) {
        } else if (args.length == 2) {
            if (args[1].toLowerCase().equals("write")) {
                System.out.println("Write mode");
                try {
                    long time = System.currentTimeMillis();
                    HSSF hssf = new HSSF(args[0], true);
                    System.out.println("" + (System.currentTimeMillis() - time) + " ms generation time");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("readwrite test");
                try {
                    HSSF hssf = new HSSF(args[0]);
                    HSSFWorkbook wb = hssf.hssfworkbook;
                    FileOutputStream stream = new FileOutputStream(args[1]);
                    wb.write(stream);
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if ((args.length == 3) && args[2].toLowerCase().equals("modify1")) {
            try {
                HSSF hssf = new HSSF(args[0]);
                HSSFWorkbook wb = hssf.hssfworkbook;
                FileOutputStream stream = new FileOutputStream(args[1]);
                HSSFSheet sheet = wb.getSheetAt(0);
                for (int k = 0; k < 25; k++) {
                    HSSFRow row = sheet.getRow(k);
                    sheet.removeRow(row);
                }
                for (int k = 74; k < 100; k++) {
                    HSSFRow row = sheet.getRow(k);
                    sheet.removeRow(row);
                }
                HSSFRow row = sheet.getRow(39);
                HSSFCell cell = row.getCell((short) 3);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                cell.setCellValue("MODIFIED CELL!!!!!");
                wb.write(stream);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
