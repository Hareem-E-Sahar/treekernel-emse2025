package com.student.util.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;

/**
 * Excel����������,ʹ��POI��API����Excel (1)sheet��, a.����Ϊ��,
 * b.����ֻ��#colName#,��:�ͻ����:#CUST_NAME# c.����ֻ��%colName%,��:��Ʒ���:%PRO_NAME%
 * e.����ֻ����ͨ���ı�,��û�� #colName#��%colName% f.���Զ��� g.��������ɸѡ���� h.���Ը�Ԫ�����ù�ʽ
 * 
 */
@SuppressWarnings("deprecation")
public class ExcelExpUtil {

    private static final Pattern p1 = Pattern.compile("^.*#(.+)#.*$");

    private static final Pattern p2 = Pattern.compile("^.*%(.+)%.*$");

    private static final String STRNUM = "%STRNUM%";

    private static HSSFCellStyle cellStyle;

    private ExcelExpUtil() {
    }

    /**
	 * ����Excel ͨ������£�����Ҫ֪��������ɵ���ʱ�ļ���������WEB��������������ͻ��˺��ɾ�����ԣ�һ�㲻��Ҫ�����������
	 * 
	 * @param excelModule
	 *            ���ʱ����Ҫ�����
	 * @param templeteFile
	 *            ģ���ļ����·����
	 * @return ������ļ�
	 */
    public static File expExcel(ExcelModule excelModule, String templeteFile) throws Exception {
        if (StringUtil.isEmpty(templeteFile)) {
            return null;
        }
        File file = new File(templeteFile);
        String tmpFile = file.getName();
        return expExcel(excelModule, templeteFile, tmpFile);
    }

    /**
	 * ����Excel
	 * 
	 * @param excelModule
	 *            ���ʱ����Ҫ�����
	 * @param templeteFile
	 *            ģ���ļ����·����
	 * @param tmpFile
	 *            �����ļ����·����
	 * @return ������ļ�
	 */
    public static File expExcel(ExcelModule excelModule, String templeteFile, String tmpFile) throws Exception {
        FileInputStream fis = new FileInputStream(templeteFile);
        HSSFWorkbook wb = new HSSFWorkbook(fis);
        cellStyle = wb.createCellStyle();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            HSSFSheet sheet = wb.getSheetAt(i);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                continue;
            }
            if (StringUtil.isNotEmpty(excelModule.getSheetName(i))) {
                wb.setSheetName(i, excelModule.getSheetName(i));
            }
            int strNumRow = getRowNum(sheet);
            setOnceValue(wb, sheet, i, excelModule, strNumRow);
            setMultiValue(wb, sheet, i, excelModule, strNumRow);
        }
        return createResultFile(wb, fis, tmpFile);
    }

    /**
	 * 
	 * getExcelInputStream(�@��excel ݔ����
	 * 
	 * @param excelModule
	 * @return
	 * @throws Exception
	 *@return InputStream
	 * @exception
	 * @since 1.0
	 */
    public static InputStream getExcelInputStream(ExcelModule excelModule, String templeteFile) throws Exception {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(templeteFile);
        ByteArrayInputStream bas = null;
        HSSFWorkbook wb = new HSSFWorkbook(fis);
        cellStyle = wb.createCellStyle();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            HSSFSheet sheet = wb.getSheetAt(i);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                continue;
            }
            if (StringUtil.isNotEmpty(excelModule.getSheetName(i))) {
                wb.setSheetName(i, excelModule.getSheetName(i));
            }
            int strNumRow = getRowNum(sheet);
            setOnceValue(wb, sheet, i, excelModule, strNumRow);
            setMultiValue(wb, sheet, i, excelModule, strNumRow);
        }
        wb.write(bao);
        byte[] ba = bao.toByteArray();
        bas = new ByteArrayInputStream(ba);
        bao.close();
        fis.close();
        return bas;
    }

    private static void setMultiValue(HSSFWorkbook wb, HSSFSheet sheet, int sheetIndex, ExcelModule excelData, int strNumRow) {
        List<Map<String, String>> list = excelData.getMultData(sheetIndex);
        if (strNumRow == -1) {
            return;
        }
        copyRow(wb, sheet, list, strNumRow);
        setMultiValue(sheet, list, strNumRow);
    }

    private static void setMultiValue(HSSFSheet sheet, List<Map<String, String>> list, int strNumRow) {
        if (list == null || list.size() <= 0) {
            return;
        }
        for (int i = strNumRow; i <= (strNumRow + list.size() - 1); i++) {
            HSSFRow row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            for (short j = 0; j <= row.getLastCellNum(); j++) {
                HSSFCell cell = row.getCell(j);
                setAutoLine(cell);
                if (cell == null) {
                    continue;
                }
                String cellValue = cell.getStringCellValue();
                if (STRNUM.equals(cellValue)) {
                    cell.setCellValue("" + (i - strNumRow + 1));
                } else {
                    setMultiValue(list.get(i - strNumRow), cell);
                }
            }
        }
    }

    private static void setMultiValue(Map<String, String> data, HSSFCell cell) {
        if (cell == null || cell.getCellType() != HSSFCell.CELL_TYPE_STRING) {
            return;
        }
        String oldValue = cell.getStringCellValue();
        if (StringUtil.isEmpty(oldValue) || oldValue == null || oldValue.length() <= 0) {
            return;
        }
        Matcher m = p2.matcher(oldValue);
        if (m.find()) {
            String colName = m.group(1);
            String target = data.get(colName.toUpperCase());
            if (target == null) target = "";
            String newValue = oldValue.replace("%" + colName + "%", (colName == null || colName.length() <= 0) ? "" : target);
            cell.setCellValue(newValue);
        }
    }

    private static void copyRow(HSSFWorkbook wb, HSSFSheet sheet, List<Map<String, String>> list, int strNumRow) {
        int endRow = sheet.getLastRowNum();
        if (list == null) {
            HSSFRow hssfRow = sheet.getRow(strNumRow);
            if (hssfRow == null) {
                return;
            }
            int first = hssfRow.getFirstCellNum();
            int last = hssfRow.getLastCellNum();
            for (int i = first; i <= last; i++) {
                HSSFCell cell = hssfRow.getCell((short) i);
                if (cell == null) {
                    continue;
                }
                cell.setCellValue("");
            }
            return;
        }
        if (strNumRow != endRow && list.size() > 1) sheet.shiftRows(strNumRow + 1, endRow, list.size() - 1);
        HSSFRow templeteRow = sheet.getRow(strNumRow);
        for (int i = strNumRow + 1; i <= (strNumRow + list.size() - 1); i++) {
            HSSFRow row = sheet.getRow(i);
            row = ((row == null) ? (sheet.createRow(i)) : row);
            row.setHeight(templeteRow.getHeight());
            for (short j = 0; j <= templeteRow.getLastCellNum(); j++) {
                HSSFCell templeteCell = templeteRow.getCell(j);
                HSSFCell cell = row.createCell(j);
                String value = null;
                HSSFCellStyle style = wb.createCellStyle();
                if (templeteCell != null) {
                    value = templeteCell.getStringCellValue();
                    style = templeteCell.getCellStyle();
                }
                cell.setCellValue(value);
                cell.setCellStyle(style);
            }
        }
        dealCellMergedRegion(sheet, strNumRow, list.size());
    }

    /**
	 * ���� �ϲ���Ԫ�� ������
	 */
    private static void dealCellMergedRegion(HSSFSheet sheet, int strNumRow, int size) {
        int startRow = strNumRow + 1;
        int endRow = strNumRow + size - 1;
        int sum = sheet.getNumMergedRegions();
        for (int i = 0; i < sum; i++) {
            Region range = sheet.getMergedRegionAt(i);
            if (range == null) {
                continue;
            }
            int firstRow = range.getRowFrom();
            int lastRow = range.getRowTo();
            if (firstRow == strNumRow && lastRow == strNumRow) {
                short firstCol = range.getColumnFrom();
                short lastCol = range.getColumnTo();
                for (int j = startRow; j <= endRow; j++) {
                    Region region = new Region(j, firstCol, j, lastCol);
                    sheet.addMergedRegion(region);
                }
            }
        }
    }

    /**
	 * �ҵ�%STRNUM%���б� ���û���ҵ�,�򷵻�-1:
	 */
    private static int getRowNum(HSSFSheet sheet) {
        int startRow = sheet.getFirstRowNum();
        int endRow = sheet.getLastRowNum();
        for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            short startCol = row.getFirstCellNum();
            short endCol = row.getLastCellNum();
            for (short colIndex = startCol; colIndex <= endCol; colIndex++) {
                HSSFCell cell = row.getCell(colIndex);
                if (cell == null || cell.getCellType() != HSSFCell.CELL_TYPE_STRING || StringUtil.isEmpty(cell.getStringCellValue())) {
                    continue;
                } else {
                    Matcher m = p2.matcher(cell.getStringCellValue());
                    if (m.find()) {
                        return rowIndex;
                    }
                }
            }
        }
        return -1;
    }

    /**
	 * ����Ψһֵ
	 */
    private static void setOnceValue(HSSFWorkbook wb, HSSFSheet sheet, int sheetIndex, ExcelModule excelData, int strNumRow) throws Exception {
        Map<String, String> data = excelData.getOnceData(sheetIndex);
        if (data == null || data.size() <= 0) {
            return;
        }
        int startRow = sheet.getFirstRowNum();
        int endRow = sheet.getLastRowNum();
        for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            short startCol = row.getFirstCellNum();
            short endCol = row.getLastCellNum();
            for (short colIndex = startCol; colIndex <= endCol; colIndex++) {
                HSSFCell cell = row.getCell(colIndex);
                if (cell == null) {
                    continue;
                } else {
                    setOnceValue(data, cell);
                }
            }
        }
    }

    private static void setOnceValue(Map<String, String> data, HSSFCell cell) {
        if (cell == null || cell.getCellType() != HSSFCell.CELL_TYPE_STRING) {
            return;
        }
        setAutoLine(cell);
        String oldValue = cell.getStringCellValue();
        if (oldValue == null || oldValue.length() <= 0) {
            return;
        }
        String str = oldValue.replaceAll("\n", " ").replace("\r", " ");
        Matcher m = p1.matcher(str);
        if (m.find()) {
            String colName = m.group(1);
            String newValue = oldValue.replace("#" + colName + "#", (colName == null || colName.length() <= 0) ? "" : data.get(colName.toUpperCase()));
            cell.setCellValue(newValue);
        }
    }

    /**
	 * �������ļ�
	 */
    private static File createResultFile(HSSFWorkbook wb, FileInputStream fis, String tmpFile) throws Exception {
        String fileName = generateFileName(tmpFile);
        FileOutputStream fos = new FileOutputStream(fileName);
        wb.write(fos);
        fos.close();
        fis.close();
        return new File(fileName);
    }

    private static String generateFileName(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return null;
        }
        String dateString = DateUtil.formate(new Date());
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName + dateString;
        }
        return fileName.substring(0, index) + dateString + fileName.substring(index);
    }

    public static void main(String[] args) {
        System.out.println(generateFileName("src.xls"));
    }

    /**
	 * ���õ�Ԫ���Զ�����
	 * 
	 * @param cell
	 */
    private static void setAutoLine(HSSFCell cell) {
        if (cell == null) return;
        try {
            HSSFCellStyle style = cell.getCellStyle();
            if (style == null) style = cellStyle;
            style.setWrapText(true);
            cell.setCellStyle(style);
        } catch (Exception e) {
        }
    }
}
