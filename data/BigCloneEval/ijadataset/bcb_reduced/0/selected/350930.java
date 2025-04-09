package com.gever.sysman.log.util;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.gever.exception.*;
import com.gever.jdbc.sqlhelper.*;
import com.gever.util.log.Log;

/**
 * <p>Title:Log�ļ����� </p>
 * <p>Description:Log�ļ����� </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: GEVER</p>
 * @author Hu.Walker
 * @version 1.0
 */
public class RsToFileServlet extends HttpServlet {

    private String dbData = "gdp";

    Log log = Log.getInstance(RsToFileServlet.class);

    public RsToFileServlet() {
    }

    private String getDbData() {
        return this.dbData;
    }

    private String downFileName = new String("");

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    private String nullToString(Object value) {
        String strRet = (String) value;
        if (null == strRet) {
            return strRet = "";
        }
        return strRet;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        final String CONTENT_TYPE = "application/octet-stream; charset=ISO-8859-1";
        final String CONTENT_TYPE_TEXT = "text/html; charset=GB2312";
        log.showLog("----servlet--start---");
        String type = new String();
        String sqlwhere = new String();
        type = nullToString(request.getParameter("type"));
        if (!nullToString(request.getParameter("fileName")).equals("")) this.setDownFileName(request.getParameter("fileName"));
        String strRealPath = this.getServletContext().getRealPath("/");
        String strPath = new String();
        if (!type.equals("")) {
            sqlwhere = request.getParameter("sqlwhere");
            if (sqlwhere != null) sqlwhere = new String(replace(sqlwhere, "|", "%").getBytes("ISO8859-1"), "GB2312");
            sqlwhere = nullToString(sqlwhere);
            String strsql = getSql(type, sqlwhere);
            try {
                this.createDirtory(strRealPath);
                RsToFileUtil rs = new RsToFileUtil();
                strPath = rs.sqlRsToFile(strsql, strRealPath, type);
                java.io.File file = new java.io.File(strPath);
                java.io.FileInputStream fin = new java.io.FileInputStream(file);
                long len = file.length();
                Long lng = new Long(len);
                String strFile2 = new String(getDownFileName().getBytes(), "ISO-8859-1");
                response.addHeader("Content-Disposition", "attachment; filename=" + strFile2);
                response.addHeader("Content-Length", lng.toString());
                response.setContentType(CONTENT_TYPE);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                int b;
                while ((b = fin.read()) != -1) {
                    outStream.write(b);
                }
                out.write(outStream.toByteArray());
                out.flush();
                out.close();
                outStream.close();
                fin.close();
            } catch (DefaultException e) {
                response.setContentType(CONTENT_TYPE_TEXT);
                out.print("");
                out.print("Sorry, file not found!");
                out.print(" close ");
                e.printStackTrace(System.out);
            }
        } else {
            response.setContentType(CONTENT_TYPE_TEXT);
            out.print("");
            out.print("Sorry, file not found!");
            out.print(" close ");
        }
    }

    /**
     * ����Ŀ¼
     * @param strDirName Ŀ¼��
     */
    private void createDirtory(String strDirName) {
        File strDir = new File(strDirName);
        if (!strDir.isDirectory()) {
            strDir.mkdir();
        }
        strDir = null;
    }

    public String getSql(String type, String sqlwhere) {
        String strsql = new String();
        StringBuffer strBuf = new StringBuffer();
        if (type.equals("syslog")) {
            strsql = "select otime ����ʱ��,username ������,module ģ��,ipAddress �ͻ���IP��ַ,action ��������,memo ��ע  from T_SYSTEM_LOG where 1=1 " + sqlwhere;
        }
        return strsql;
    }

    /**
     * �ַ��滻����
     * Wengnb Add 2003-09-09
     * @param strSource String:�ַ�
     * @param strFrom   String:Դ�ִ�
     * @param strTo     String:�滻���ִ�
     * @return          String:�����滻����ִ����
     */
    public static String replace(String strSource, String strFrom, String strTo) {
        if (strSource == null) return "";
        String strDest = "";
        int intFromLen = strFrom.length();
        int intPos = 0;
        while ((intPos = strSource.indexOf(strFrom)) != -1) {
            strDest = strDest + strSource.substring(0, intPos);
            strDest = strDest + strTo;
            strSource = strSource.substring(intPos + intFromLen);
        }
        strDest = strDest + strSource;
        return strDest;
    }

    /**
     * Wengnb Add 2003-09-09
     * ��"'"�滻��Ϊ"''"
     * @param strSource String:�ַ�
     * @return          String:�����滻����ִ����
     */
    public static String replaceText(String strSource) {
        return replace(strSource, "'", "''");
    }

    /**
     * ������ʷ��Ϣ��¼
     * @param strsql �����Ӿ�
     * @param realPath �ļ����ص�·��
     * @param type ����
     * @return �ɹ����
     * @throws GeneralException
     */
    public synchronized String sqlRsToFile(String strsql, String realPath, String strType) throws DefaultException {
        SQLHelper helper = new DefaultSQLHelper(this.getDbData());
        Connection conn = helper.getConnection();
        Statement st = null;
        ResultSet rs = null;
        java.sql.ResultSetMetaData rsMeta = null;
        StringBuffer sBufSql = new StringBuffer();
        boolean bIsInBox = true;
        String strSubWhere = new String();
        String strFldName;
        String strValue;
        int iRet = 0;
        realPath = realPath + "uploadfiles" + File.separator;
        File dirName = new File(realPath);
        if (!dirName.exists()) {
            dirName.mkdirs();
        }
        String filePath;
        filePath = realPath + strType + ".txt";
        log.showLog("----realpath=" + filePath);
        StringBuffer sBufTxt = new StringBuffer();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(strsql);
            rsMeta = rs.getMetaData();
            int cols = 0;
            cols = rsMeta.getColumnCount();
            sBufTxt.setLength(0);
            for (int idx = 1; idx <= cols; idx++) {
                strFldName = (rsMeta.getColumnName(idx));
                sBufTxt.append(strFldName);
                if (idx < cols) sBufTxt.append("\t");
            }
            sBufTxt.append("\n\r");
            while (rs.next()) {
                for (int idx = 1; idx <= cols; idx++) {
                    strFldName = (rsMeta.getColumnName(idx));
                    sBufTxt.append(rs.getString(strFldName));
                    if (idx < cols) sBufTxt.append("\t");
                }
                sBufTxt.append("\n\r");
                iRet++;
            }
            log.showLog("-------sql rows---" + iRet + filePath);
            rs.close();
            st.close();
            log.showLog("-------filePath---" + iRet + filePath);
            File myFilePath = new File(filePath);
            if (!myFilePath.exists()) myFilePath.createNewFile();
            FileWriter resultFile = new FileWriter(myFilePath);
            PrintWriter myFile = new PrintWriter(resultFile);
            myFile.println(sBufTxt.toString());
            resultFile.close();
            myFile.close();
        } catch (FileNotFoundException fnfe) {
            log.showLog("�ļ�δ�ҵ�..****..");
        } catch (IOException e) {
            log.showLog("д���ļ�����" + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            throw new DefaultException(e);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (java.sql.SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }
        return filePath;
    }

    public String getDownFileName() {
        return downFileName;
    }

    public void setDownFileName(String downFileName) {
        this.downFileName = downFileName;
    }

    public void setDbData(String dbData) {
        this.dbData = dbData;
    }
}
