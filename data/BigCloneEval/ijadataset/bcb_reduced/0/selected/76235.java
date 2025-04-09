package org.bitbrushers.jobextractor.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bitbrushers.jobextractor.to.JobOffer;

public class ExcelWriter {

    private OutputStream oStream;

    private HSSFWorkbook workbook;

    private HSSFSheet jobSheet;

    private int jobLastRow;

    private static final String VALUES_HEADER = "Value";

    private static final String CITY_HEADER = "City";

    private static final String STATE_HEADER = "State";

    private static final String DATE_HEADER = "Date posted";

    private static final String TITLE_HEADER = "Position";

    private static final String DESCRIPTION_HEADER = "Description";

    private static final String COMPANY_HEADER = "Company";

    private static final String EMAIL_HEADER = "Email";

    private static final String CODE_HEADER = "Code";

    public ExcelWriter(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            this.workbook = new HSSFWorkbook();
            this.jobSheet = this.workbook.createSheet("Jobs");
            this.jobLastRow = -1;
            this.createJobHeader();
        } else {
            try {
                this.workbook = new HSSFWorkbook(new FileInputStream(filename));
            } catch (IOException e) {
                System.out.println("WARNING: \"" + filename + "\" is invalid. " + "If it exists, try removing it and start again.");
                System.exit(1);
            }
            this.jobSheet = this.workbook.getSheet("Jobs");
            this.jobLastRow = this.jobSheet.getLastRowNum();
        }
        try {
            this.oStream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println("WARNING: \"" + filename + "\" cannot be opened." + " Maybe it is already opened by another program.");
            System.exit(1);
        }
    }

    private void createJobHeader() {
        jobLastRow++;
        HSSFRow row = this.jobSheet.createRow(jobLastRow);
        HSSFCell cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.VALUES_HEADER));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.CITY_HEADER));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.STATE_HEADER));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.DATE_HEADER));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.TITLE_HEADER));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.DESCRIPTION_HEADER));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.COMPANY_HEADER));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.EMAIL_HEADER));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(ExcelWriter.CODE_HEADER));
    }

    private HSSFCell getNextCell(HSSFRow row) {
        int lastCell = row.getLastCellNum();
        HSSFCell cell = row.createCell((short) (lastCell + 1));
        return cell;
    }

    public boolean writeJobOffer(JobOffer jo) {
        HSSFRow row = null;
        int rowIndex = 0;
        if (this.jobLastRow >= 1) {
            rowIndex = this.getInsertionPoint(jo);
            if (rowIndex == -1) {
                return false;
            }
            this.jobSheet.createRow(++jobLastRow);
            this.jobSheet.shiftRows(rowIndex, jobLastRow - 1, 1);
            row = this.jobSheet.getRow(rowIndex);
        } else {
            row = this.jobSheet.createRow(++jobLastRow);
        }
        HSSFCell cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(jo.getValues()));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(jo.getCity()));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(jo.getState()));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(jo.getDate()));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(jo.getTitle()));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(jo.getDescription()));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(jo.getCompany()));
        cell = getNextCell(row);
        cell.setCellValue(new HSSFRichTextString(jo.getEmail()));
        cell = getNextCell(row);
        cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
        cell.setCellValue(Integer.parseInt(jo.getCode().trim()));
        return true;
    }

    private int getInsertionPoint(JobOffer jo) {
        HSSFRow row = null;
        int low = 1;
        int high = this.jobLastRow;
        int mid = 0;
        int sheetCodeValue = 0;
        int newCodeValue = Integer.parseInt(jo.getCode().trim());
        while (low <= high) {
            mid = (low + high) / 2;
            row = this.jobSheet.getRow(mid);
            sheetCodeValue = (int) row.getCell((short) 8).getNumericCellValue();
            if (newCodeValue < sheetCodeValue) {
                high = mid - 1;
            } else if (newCodeValue > sheetCodeValue) {
                low = mid + 1;
            } else {
                return -1;
            }
        }
        if (newCodeValue < sheetCodeValue) {
            return mid;
        }
        if (newCodeValue > sheetCodeValue) {
            return mid + 1;
        }
        return mid;
    }

    public void close() {
        try {
            this.workbook.write(this.oStream);
            this.oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
