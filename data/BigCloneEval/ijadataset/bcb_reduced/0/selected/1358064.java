package com.kongur.star.venus.web.action.common.exprot;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.codehaus.jackson.map.deser.SettableBeanProperty.FieldProperty;
import com.kongur.star.venus.common.page.Paginable;
import com.kongur.star.venus.common.page.Pagination;
import com.kongur.star.venus.web.action.BaseAction;
import com.kongur.star.venus.web.action.common.exprot.excel.ExcelException;
import com.kongur.star.venus.web.action.common.exprot.excel.ExcelUtil;

/**
 * excel����������
 * 
 * @author wangzhaohui.ht
 * 
 * @param <E>
 */
public abstract class ExcelExport<E> extends BaseAction {

    private final Log logger = LogFactory.getLog(this.getClass());

    public void writeWithZip(HttpServletResponse response, Pagination<E> params, String title) throws IOException, ExcelException {
        wrapZip(params, title, response);
    }

    /**
	 * ����ѯ������EXCEL�ļ���ѹ����Ȼ��
	 * 
	 * @param pageSize
	 *            ÿ��EXCEL�ļ��ļ�¼��
	 * @param params
	 *            ��ѯ����
	 * @param fileName
	 *            �����ѹ���ļ���;
	 * @param response
	 *            {@link HttpServletResponse}
	 * @throws ExcelException
	 */
    public void wrapZip(Pagination<E> params, String fileName, HttpServletResponse response) throws ExcelException {
        setResponse(fileName, response);
        Paginable<E> iPageList = queryForPagin(params);
        flushBuffer(response);
        int total = iPageList == null ? 0 : iPageList.getTotalCount();
        ZipOutputStream zipout = null;
        OutputStream out = null;
        Paginable<E> list = null;
        try {
            out = response.getOutputStream();
            zipout = new ZipOutputStream(out);
            int pageNum = total / params.getPageSize() + 1;
            for (int i = 1; i <= pageNum; i++) {
                list = queryForPagin(params);
                flushBuffer(response);
                if (list != null && list.getData() != null && list.getData().size() > 0) {
                    String subName = fileName + "-" + i;
                    wrapExcel(fileName, response, zipout, out, list, subName);
                }
            }
            zipout.flush();
            out.flush();
            flushBuffer(response);
        } catch (IOException e) {
            throw new ExcelException("��д�ͻ������ʱ�����쳣", e);
        } finally {
            if (zipout != null) {
                try {
                    zipout.close();
                } catch (IOException e) {
                    logger.error("ѹ�����ر��쳣", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("response������ر��쳣", e);
                }
            }
            zipout = null;
            out = null;
        }
    }

    /**
	 * �����д�뵽EXCEL�������У������ļ�ѹ�����������
	 * 
	 * @param fileName
	 *            ѹ���ļ�����ƣ�
	 * @param response
	 *            {@link HttpServletResponse}
	 * @param zipout
	 *            {@link ZipOutputStream}
	 * @param out
	 *            {@link HttpServletResponse}�������
	 * @param list
	 *            ��ݼ���
	 * @param i
	 *            ѹ������ĵڼ����ļ��������ظ�
	 * @throws ExcelException
	 */
    private void wrapExcel(String fileName, HttpServletResponse response, ZipOutputStream zipout, OutputStream out, Paginable<E> list, String subName) throws ExcelException {
        flushBuffer(response);
        HSSFWorkbook workbook = ExcelUtil.createWorkBook(fileName, getFieldMap(), list.getData());
        flushBuffer(response);
        InputStream inputStream = null;
        BufferedInputStream origin = null;
        try {
            inputStream = ExcelUtil.convertToInputStream(workbook);
            flushBuffer(response);
            zipout.putNextEntry(new ZipEntry(subName + ".xls"));
            byte[] buf = new byte[2048];
            origin = new BufferedInputStream(inputStream, 2048);
            int len;
            int icount = 0;
            while ((len = origin.read(buf, 0, 2048)) != -1) {
                zipout.write(buf, 0, len);
                icount++;
                if (icount > 10) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        logger.error("���EXCEL˯��ʱ�쳣", e);
                    } finally {
                        icount = 0;
                        zipout.flush();
                        out.flush();
                        flushBuffer(response);
                    }
                }
            }
            zipout.flush();
            out.flush();
            flushBuffer(response);
        } catch (IOException e) {
            throw new ExcelException("���EXCEL�����������쳣", e);
        } finally {
            if (origin != null) {
                try {
                    origin.close();
                } catch (IOException e) {
                    logger.error("���ѹ����ʱ�������ر��쳣", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("���ѹ���������ر��쳣", e);
                }
            }
            origin = null;
            inputStream = null;
        }
    }

    /**
	 * ����HttpServletResponse����
	 * 
	 * @param fileName
	 *            �����ļ������
	 * @param response
	 *            HttpServletResponse
	 */
    protected void setResponse(String fileName, HttpServletResponse response) {
        response.reset();
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentType("application/x-msdownload;charset=UTF-8");
        if (fileName == null) {
            fileName = System.currentTimeMillis() + "";
        }
        try {
            fileName = new String(fileName.getBytes("gb2312"), "ISO8859-1") + ".zip";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        flushBuffer(response);
    }

    private void flushBuffer(HttpServletResponse response) {
        try {
            response.flushBuffer();
        } catch (IOException e) {
            logger.error("�����ļ��쳣", e);
        }
    }

    /**
	 * ��ҳ��ѯ���󷽷�
	 * 
	 * @param params
	 *            ��ѯ����
	 * @return
	 */
    public abstract Paginable<E> queryForPagin(Paginable<E> params);

    /**
	 * ������ӳ���ϵ��key�����excel����ʾ���������valueΪString���ͣ� ��Ϊbean��ݼ����Ի���Map��ݼ���KEY�������
	 * {@link FieldProperty},�뿴 {@link FieldProperty}����˵��
	 * 
	 * @return
	 */
    public abstract Map<String, Object> getFieldMap();
}
