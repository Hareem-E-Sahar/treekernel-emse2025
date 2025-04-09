package com.exedosoft.plat.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import com.exedosoft.plat.ExedoException;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOBO;
import com.exedosoft.plat.bo.DODataSource;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.ui.DODownLoadFile;
import com.exedosoft.plat.util.id.UUIDHex;

/**
 * 如果要用static 关键字一定要考虑好线同步问题
 * 
 * @author anolesoft
 * 
 */
public class ZipUtil {

    private static Log log = LogFactory.getLog(ZipUtil.class);

    public static List unzip(ZipFile aZipFile) {
        String randomStr = UUIDHex.getInstance().generate();
        List unZipFiles = new ArrayList();
        try {
            for (Enumeration enu = aZipFile.getEntries(); enu.hasMoreElements(); ) {
                ZipEntry ze = (ZipEntry) enu.nextElement();
                InputStream is = aZipFile.getInputStream(ze);
                StringBuilder sb = new StringBuilder(DOGlobals.getInstance().UPLOAD_TEMP).append(randomStr).append(File.separator);
                File dirFile = new File(sb.toString());
                dirFile.mkdir();
                sb.append(ze.getName());
                File aFile = new File(sb.toString());
                aFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(aFile);
                byte[] by = new byte[1024];
                int c;
                while ((c = is.read(by)) != -1) {
                    fos.write(by, 0, c);
                }
                unZipFiles.add(sb.toString());
                fos.close();
                is.close();
            }
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return unZipFiles;
    }

    /**
	 * 写压缩文件
	 */
    public static String writeZip(String paneModelUid, DOBO bo, DOService aService, String allSelects) throws IOException {
        String deptCode = "a000000";
        BOInstance aUser = DOGlobals.getInstance().getSessoinContext().getUser();
        if (aUser != null) {
            deptCode = aUser.getValue("deptcode");
        }
        StringBuilder zipFilePath = new StringBuilder(DOGlobals.WORK_DIR).append(File.separator).append(deptCode);
        File aWkDir = new File(zipFilePath.toString());
        if (!aWkDir.exists()) {
            aWkDir.mkdir();
        }
        zipFilePath.append(File.separator).append("batch.zip");
        File aFile = new File(zipFilePath.toString());
        aFile.createNewFile();
        OutputStream os = new FileOutputStream(aFile);
        ZipOutputStream zos = new ZipOutputStream(os);
        String[] arraySelect = allSelects.split(",");
        for (int i = 0; i < arraySelect.length; i++) {
            String aSelect = arraySelect[i];
            if (aSelect == null || aSelect.trim().equals("")) {
                continue;
            }
            BOInstance aInstance = bo.refreshContext(aSelect);
            if (aService != null) {
                try {
                    aService.invokeAll();
                } catch (ExedoException e) {
                    e.printStackTrace();
                }
            }
            if (aInstance != null) {
                String id_applyid = aInstance.getValue("id_applyid");
                ZipEntry ze = new ZipEntry(id_applyid + ".xml");
                zos.putNextEntry(ze);
                zos.write(DODownLoadFile.outHtmlCode(paneModelUid).getBytes("utf-8"));
                ze.clone();
            }
        }
        zos.close();
        return zipFilePath.toString();
    }

    public static void testProduce() {
        DODataSource ds = DODataSource.getDataSourceByL10n("计量院系统数据库");
        System.out.println(ds);
        Connection con = null;
        CallableStatement pstmt = null;
        try {
            con = ds.getConnection();
            System.out.println(con.getCatalog());
            pstmt = con.prepareCall("SubmitChildProject(?,?)", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1, "1");
            pstmt.registerOutParameter(2, Types.VARCHAR);
            pstmt.execute();
            String outp = pstmt.getString(2);
            System.out.println("outpoutpoutpoutpoutpoutpoutpoutpoutp:::::" + outp);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                ds.ifCloseConnection(con);
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File aFile = new File(DOGlobals.UPLOAD_TEMP + StringUtil.getCurrentDayStr());
        aFile.mkdir();
    }
}
