package com.kongur.star.venus.web.action.common.exprot.excel;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

/**
 * excel����������
 * 
 * @author wangzhaohui.ht
 * @created 2011-12-17 ����04:04:24
 * @version $Id: ExcelUtil.java 6516 2012-02-09 01:32:31Z qingming $
 */
public class ExcelUtil {

    /**
	 * 
	 * @param &lt;T&gt;��ݼ�����
	 * @param &lt;PV&gt;���������ͣ���ѡ����{@link FieldProperty} ��{@link String}
	 * @param title
	 *            sheetҳ����
	 * @param fieldMap
	 *            ������ӳ���ϵ��key�����excel����ʾ���������valueΪString���ͣ�
	 *            ��Ϊbean��ݼ����Ի���Map��ݼ���KEY�������{@link FieldProperty},�뿴
	 *            {@link FieldProperty}����˵��
	 * @param dataSet
	 *            ��ݼ�������<strong>Map</strong>��������<strong>javabean</strong>
	 * @return {@link HSSFWorkbook}
	 * @throws ExcelException
	 * <pre>
	 * 
	 * </pre>
	 */
    public static <T, PV> HSSFWorkbook createWorkBook(String title, Map<String, PV> fieldMap, Collection<T> dataSet) throws ExcelException {
        Map<String, FieldProperty> filedMap = convert(fieldMap);
        ExcelFileHandler<T> fileHandler = new ExcelFileHandler<T>(title, filedMap, dataSet);
        return fileHandler.createWorkBook();
    }

    /**
	 * �����ת��Ϊworkbook����
	 * 
	 * @param <T>��ݼ�����
	 * @param <PV>���������ͣ���ѡ����{@link FieldProperty} ��{@link String}
	 * @param title
	 *            sheetҳ����
	 * @param fieldMap
	 *            ������ӳ���ϵ��key�����excel����ʾ���������valueΪString���ͣ�
	 *            ��Ϊbean��ݼ����Ի���Map��ݼ���KEY�������{@link FieldProperty},�뿴
	 *            {@link FieldProperty}����˵��
	 * @param dataSet
	 *            ��ݼ�������<strong>Map</strong>��������<strong>javabean</strong>
	 * @param pageSize
	 *            ÿ��workbook�������
	 * @return {@link HSSFWorkbook}
	 * @throws ExcelException
	 */
    public static <T, PV> HSSFWorkbook[] createWorkbooks(String title, Map<String, PV> fieldMap, List<T> dataSet, int pageSize) throws ExcelException {
        if (dataSet == null) {
            return null;
        }
        int total = dataSet.size();
        int pageNum = total / pageSize + 1;
        int fromIndex = 0;
        int toIndex = pageSize;
        HSSFWorkbook[] hssfWorkbooks = new HSSFWorkbook[pageNum];
        for (int i = 0; i < pageNum; i++) {
            fromIndex = i * pageSize;
            toIndex = (i + 1) * pageSize;
            List<T> dataSetSub = dataSet.subList(fromIndex, toIndex);
            Map<String, FieldProperty> filedMap = convert(fieldMap);
            ExcelFileHandler<T> fileHandler = new ExcelFileHandler<T>(title, filedMap, dataSetSub);
            hssfWorkbooks[i] = fileHandler.createWorkBook();
        }
        return hssfWorkbooks;
    }

    /**
	 * ���������ֱ��ת��Ϊ����������
	 * 
	 * @param <T>��ݼ�����
	 * @param <PV>���������ͣ���ѡ����{@link FieldProperty} ��{@link String}
	 * @param title
	 *            sheetҳ����
	 * @param fieldMap
	 *            ������ӳ���ϵ��key�����excel����ʾ���������valueΪString���ͣ�
	 *            ��Ϊbean��ݼ����Ի���Map��ݼ���KEY�������{@link FieldProperty},�뿴
	 *            {@link FieldProperty}����˵��
	 * @param dataSet
	 *            ��ݼ�������<strong>Map</strong>��������<strong>javabean</strong>
	 * @param pageSize
	 *            ÿ��workbook�������
	 * @return
	 * @throws ExcelException
	 * @throws IOException
	 */
    public static <T, PV> InputStream[] createInputStreams(String title, Map<String, PV> fieldMap, List<T> dataSet, int pageSize) throws ExcelException, IOException {
        if (dataSet == null) {
            return null;
        }
        int total = dataSet.size();
        int pageNum = total / pageSize + 1;
        int fromIndex = 0;
        int toIndex = pageSize;
        HSSFWorkbook[] hssfWorkbooks = new HSSFWorkbook[pageNum];
        for (int i = 0; i < pageNum; i++) {
            fromIndex = i * pageSize;
            toIndex = (i + 1) * pageSize;
            if (toIndex > total) {
                toIndex = total - 1;
            }
            List<T> dataSetSub = dataSet.subList(fromIndex, toIndex);
            Map<String, FieldProperty> filedMap = convert(fieldMap);
            ExcelFileHandler<T> fileHandler = new ExcelFileHandler<T>(title, filedMap, dataSetSub);
            hssfWorkbooks[i] = fileHandler.createWorkBook();
        }
        return convertToInputStreams(hssfWorkbooks);
    }

    /**
	 * 
	 * @param <PV>
	 * @param fieldMap
	 * @return
	 * @throws ExcelException
	 */
    private static <PV> Map<String, FieldProperty> convert(Map<String, PV> fieldMap) throws ExcelException {
        Map<String, FieldProperty> filedMap = new LinkedHashMap<String, FieldProperty>();
        Set<Entry<String, PV>> enterSet = (fieldMap).entrySet();
        Iterator<Entry<String, PV>> entryIte = enterSet.iterator();
        Entry<String, PV> entry = null;
        FieldProperty filed = null;
        while (entryIte.hasNext()) {
            filed = new FieldProperty();
            entry = entryIte.next();
            if (entry.getValue() == null) {
                filedMap.put(entry.getKey(), filed);
            } else if (entry.getValue() instanceof String) {
                filed.setProperty((String) entry.getValue());
                filedMap.put(entry.getKey(), filed);
            } else if (entry.getValue() instanceof FieldProperty) {
                filedMap.put(entry.getKey(), (FieldProperty) entry.getValue());
            } else {
                throw new ExcelException("fieldMap��valueֻ��Ϊ" + String.class.getName() + "��" + FieldProperty.class.getName() + "����");
            }
        }
        return filedMap;
    }

    /**
	 * ��EXCEL������תΪ������
	 * 
	 * @param workbook
	 *            ������
	 * @return �����
	 * @throws IOException
	 */
    public static InputStream convertToInputStream(HSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
	 * ��EXCEL������תΪ������
	 * 
	 * @param workbook
	 *            ������
	 * @return �����
	 * @throws IOException
	 */
    public static InputStream[] convertToInputStreams(HSSFWorkbook[] workbooks) throws IOException {
        if (workbooks == null) {
            return null;
        }
        int length = workbooks.length;
        InputStream[] inputStreams = new ByteArrayInputStream[length];
        for (int i = 0; i < length; i++) {
            inputStreams[i] = convertToInputStream(workbooks[i]);
        }
        return inputStreams;
    }

    /**
	 * �����������ʹ��ZIPѹ�����������
	 * 
	 * @param inputStreams
	 *            ����������
	 * @param out
	 *            �����
	 * @throws IOException
	 */
    public static void zipToOut(InputStream[] inputStreams, OutputStream out) throws IOException {
        zipToOut(inputStreams, out, "");
    }

    /**
	 * �����������ʹ��ZIPѹ�����������
	 * 
	 * @param inputStreams
	 *            ����������
	 * @param out
	 *            �����
	 * @param fileNamePrex
	 *            �ļ���ǰ׺
	 * @throws IOException
	 */
    public static void zipToOut(InputStream[] inputStreams, OutputStream out, String fileNamePrex) throws IOException {
        ZipOutputStream zipout = new ZipOutputStream(out);
        for (int i = 0; i < inputStreams.length; i++) {
            zipout.putNextEntry(new ZipEntry(fileNamePrex + (i + 1) + ".xls"));
            byte[] buf = new byte[2048];
            BufferedInputStream origin = new BufferedInputStream(inputStreams[i], 2048);
            int len;
            while ((len = origin.read(buf, 0, 2048)) != -1) {
                zipout.write(buf, 0, len);
            }
            zipout.flush();
            origin.close();
            inputStreams[i].close();
        }
        zipout.flush();
        zipout.close();
    }

    /**
	 * excel�ļ�������
	 * 
	 * @author jijingbang.ht
	 * @created 2011-12-16 ����07:53:24
	 * @version $Id: ExcelUtil.java 6516 2012-02-09 01:32:31Z qingming $
	 * @param <T>
	 */
    private static class ExcelFileHandler<T> {

        private final String title;

        private final Collection<T> dataSet;

        private final Map<String, FieldProperty> fieldMap;

        private final HSSFWorkbook workbook = new HSSFWorkbook();

        private HSSFSheet sheet = null;

        private CellStyle doubleCellStyle = null;

        private CellStyle dateCellStyle = null;

        public ExcelFileHandler(String title, Map<String, FieldProperty> filedMap, Collection<T> dataSet) {
            this.title = title;
            this.dataSet = dataSet;
            this.fieldMap = filedMap;
            doubleCellStyle = workbook.createCellStyle();
            doubleCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));
            dateCellStyle = workbook.createCellStyle();
            HSSFDataFormat format = workbook.createDataFormat();
            dateCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd HH:mm:ss"));
        }

        /**
		 * ����excel������
		 * 
		 * @return
		 * @throws ExcelException
		 */
        public HSSFWorkbook createWorkBook() throws ExcelException {
            sheet = null;
            if (title != null) {
                sheet = workbook.createSheet(title);
            } else {
                sheet = workbook.createSheet();
            }
            sheet.setDefaultColumnWidth(20);
            addField();
            if (dataSet != null && dataSet.size() > 0) {
                addDataSet();
            }
            return workbook;
        }

        /**
		 * ����ֶ�
		 */
        private void addField() {
            Set<Entry<String, FieldProperty>> enterSet = (fieldMap).entrySet();
            Iterator<Entry<String, FieldProperty>> entryIte = enterSet.iterator();
            Entry<String, FieldProperty> entry = null;
            CellStyle style = null;
            style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            style.setFont(font);
            Row row = sheet.createRow((short) 0);
            int colnum = 0;
            Cell cell = null;
            while (entryIte.hasNext()) {
                entry = entryIte.next();
                cell = row.createCell(colnum);
                cell.setCellValue(entry.getKey());
                if (fieldMap != null) {
                    addComment(row, cell, entry.getValue());
                }
                cell.setCellStyle(style);
                colnum++;
            }
        }

        /**
		 * ���ע��
		 * 
		 * @param row
		 * @param cell
		 * @param fieldProperty
		 */
        private void addComment(Row row, Cell cell, FieldProperty fieldProperty) {
            CreationHelper factory = workbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();
            int borderWidth = 0;
            ClientAnchor anchor = null;
            Comment comment = null;
            String commetText = fieldProperty.getFieldComment();
            if (commetText != null) {
                borderWidth = (int) Math.sqrt(commetText.length() / 4) + 1;
                anchor = factory.createClientAnchor();
                anchor.setCol1(cell.getColumnIndex());
                anchor.setCol2(cell.getColumnIndex() + borderWidth);
                anchor.setRow1(row.getRowNum());
                anchor.setRow2(row.getRowNum() + (borderWidth < 2 ? 2 : borderWidth));
                comment = drawing.createCellComment(anchor);
                comment.setString(factory.createRichTextString(commetText));
                cell.setCellComment(comment);
            }
        }

        /**
		 * �����ݼ�
		 * 
		 * @param <T>
		 * @param sheet
		 * @param fieldMap
		 * @param dataSet
		 * @throws ExcelException
		 */
        @SuppressWarnings("unchecked")
        private void addDataSet() throws ExcelException {
            int rownum = 1;
            for (T rowdata : dataSet) {
                Row row = sheet.createRow(rownum);
                if (rowdata instanceof Map) {
                    addMapDataToRow(row, (Map<Object, Object>) rowdata);
                } else {
                    addBeanDataToRow(row, rowdata);
                }
                rownum++;
            }
        }

        /**
		 * ���Map���
		 * 
		 * @param row
		 * @param rowdata
		 */
        private void addMapDataToRow(Row row, Map<Object, Object> rowdata) {
            Set<Entry<String, FieldProperty>> enterSet = (fieldMap).entrySet();
            Iterator<Entry<String, FieldProperty>> entryIte = enterSet.iterator();
            Entry<String, FieldProperty> entry = null;
            int column = 0;
            Cell cell = null;
            Object value = null;
            while (entryIte.hasNext()) {
                cell = row.createCell(column);
                entry = entryIte.next();
                value = rowdata.get(entry.getValue().getProperty());
                addCellValue(cell, value, entry.getValue());
                column++;
            }
        }

        /**
		 * ���Bean���
		 * 
		 * @param row
		 * @param bean
		 * @throws ExcelException
		 */
        private void addBeanDataToRow(Row row, Object bean) throws ExcelException {
            Set<Entry<String, FieldProperty>> enterSet = (fieldMap).entrySet();
            Iterator<Entry<String, FieldProperty>> entryIte = enterSet.iterator();
            Entry<String, FieldProperty> entry = null;
            int column = 0;
            Cell cell = null;
            String methodName = null;
            Object value = null;
            Method method = null;
            while (entryIte.hasNext()) {
                cell = row.createCell(column);
                entry = entryIte.next();
                try {
                    methodName = entry.getValue().getProperty();
                    methodName = "get" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
                    method = bean.getClass().getDeclaredMethod(methodName);
                    value = method.invoke(bean);
                } catch (NoSuchMethodException e) {
                    throw new ExcelException(e);
                } catch (Exception e) {
                    throw new ExcelException(e);
                }
                addCellValue(cell, value, entry.getValue());
                column++;
            }
        }

        /**
		 * ��ӵ�Ԫ������
		 * 
		 * @param cell
		 *            �������ĵ�Ԫ��
		 * @param context
		 *            ����
		 * @param filed
		 *            ������
		 */
        private void addCellValue(Cell cell, Object context, FieldProperty filed) {
            if (context == null) {
                cell.setCellValue("");
            } else if (filed.getType() != 0) {
                if (filed.getType() == FieldProperty.INT) {
                    cell.setCellValue(Integer.valueOf(context.toString()));
                } else if (filed.getType() == FieldProperty.LONG) {
                    cell.setCellValue(Long.valueOf(context.toString()));
                } else if (filed.getType() == FieldProperty.DOUBLE) {
                    cell.setCellStyle(doubleCellStyle);
                    cell.setCellValue(Double.valueOf(context.toString()));
                } else if (filed.getType() == FieldProperty.DATE) {
                    cell.setCellStyle(dateCellStyle);
                    cell.setCellValue((Date) context);
                } else {
                    cell.setCellValue(context.toString());
                }
            } else {
                if (context instanceof Integer || context instanceof Short) {
                    cell.setCellValue(Integer.valueOf(context.toString()));
                } else if (context instanceof Long) {
                    cell.setCellValue(Long.valueOf(context.toString()));
                } else if (context instanceof Date) {
                    cell.setCellStyle(dateCellStyle);
                    cell.setCellValue((Date) context);
                } else if (context instanceof Boolean) {
                    cell.setCellValue((Boolean) context);
                } else if (context instanceof Double) {
                    cell.setCellStyle(dateCellStyle);
                    cell.setCellValue(Double.valueOf(context.toString()));
                } else {
                    cell.setCellValue(context.toString());
                }
            }
        }
    }
}
