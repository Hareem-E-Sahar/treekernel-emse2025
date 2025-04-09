package egovframework.com.utl.sim.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class EgovFileCmprs {

    static final int COMPRESSION_LEVEL = 8;

    static final int BUFFER_SIZE = 64 * 1024;

    static final char FILE_SEPARATOR = File.separatorChar;

    /**
	 * 파일(디렉토리)을 압축하는 기능
	 * @param String source 원본파일명(디렉토리명)
	 * @param String target 압축파일명
	 * @return boolean result 압축성공여부 True / False
	 * @exception Exception
	*/
    public static boolean cmprsFile(String source, String target) throws Exception {
        boolean result = false;
        int cnt = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream finput = null;
        FileOutputStream foutput = null;
        ZipOutputStream zoutput = null;
        String source1 = source.replace('\\', FILE_SEPARATOR).replace('/', FILE_SEPARATOR);
        String target1 = target.replace('\\', FILE_SEPARATOR).replace('/', FILE_SEPARATOR);
        File srcFile = new File(source1);
        if (srcFile.exists()) {
            if (srcFile.isFile()) {
                String target2 = EgovFileTool.createNewFile(target1);
                File tarFile = new File(target2);
                foutput = null;
                zoutput = null;
                ZipEntry zentry = null;
                try {
                    foutput = new FileOutputStream(tarFile);
                    zoutput = new ZipOutputStream((OutputStream) foutput);
                    finput = new FileInputStream(srcFile);
                    zentry = new ZipEntry(srcFile.getName());
                    zoutput.putNextEntry(zentry);
                    zoutput.setLevel(COMPRESSION_LEVEL);
                    cnt = 0;
                    while ((cnt = finput.read(buffer)) != -1) {
                        zoutput.write(buffer, 0, cnt);
                    }
                    zoutput.closeEntry();
                    result = true;
                } catch (Exception e) {
                    tarFile.delete();
                    throw e;
                } finally {
                    if (finput != null) finput.close();
                    if (zoutput != null) zoutput.close();
                    if (foutput != null) foutput.close();
                }
            } else if (srcFile.isDirectory()) {
                String target2 = EgovFileTool.createNewFile(target1);
                File tarFile = new File(target2);
                ZipEntry zentry = null;
                try {
                    foutput = new FileOutputStream(tarFile);
                    zoutput = new ZipOutputStream((OutputStream) foutput);
                    File[] fileArr = srcFile.listFiles();
                    ArrayList list = EgovFileTool.getSubFilesByAll(fileArr);
                    for (int i = 0; i < list.size(); i++) {
                        File sfile = new File((String) list.get(i));
                        finput = new FileInputStream(sfile);
                        zentry = new ZipEntry(sfile.getAbsolutePath().replace('\\', '/').replaceAll(srcFile.getAbsolutePath().replace('\\', '/'), ""));
                        zoutput.putNextEntry(zentry);
                        zoutput.setLevel(COMPRESSION_LEVEL);
                        cnt = 0;
                        while ((cnt = finput.read(buffer)) != -1) {
                            zoutput.write(buffer, 0, cnt);
                        }
                        finput.close();
                        result = true;
                    }
                    zoutput.closeEntry();
                } catch (Exception e) {
                    tarFile.delete();
                    throw e;
                } finally {
                    if (finput != null) finput.close();
                    if (zoutput != null) zoutput.close();
                    if (foutput != null) foutput.close();
                }
            }
        }
        return result;
    }

    /**
	 * 파일(디렉토리)을 압축해제하는 기능
	 * @param String source 압축파일명
	 * @param String target 압출이 풀릴 디렉토리
	 * @return boolean result 압축해제성공여부 True / False
	 * @exception Exception
	*/
    public static boolean decmprsFile(String source, String target) throws Exception {
        boolean result = false;
        int cnt = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream finput = null;
        FileOutputStream foutput = null;
        ZipInputStream zinput = null;
        String source1 = source.replace('\\', FILE_SEPARATOR).replace('/', FILE_SEPARATOR);
        String target1 = target.replace('\\', FILE_SEPARATOR).replace('/', FILE_SEPARATOR);
        File srcFile = new File(source1);
        if (srcFile.exists() && srcFile.isFile()) {
            String target2 = EgovFileTool.createNewDirectory(target1);
            File tarFile = new File(target2);
            finput = new FileInputStream(srcFile);
            zinput = new ZipInputStream((InputStream) finput);
            foutput = null;
            ZipEntry entry;
            try {
                while ((entry = zinput.getNextEntry()) != null) {
                    String filename = entry.getName();
                    String tempFile = EgovFileTool.createNewFile(tarFile.getAbsolutePath() + FILE_SEPARATOR + filename);
                    File efile = new File(tempFile);
                    foutput = new FileOutputStream(efile);
                    while ((cnt = zinput.read(buffer)) != -1) {
                        if (foutput != null) foutput.write(buffer, 0, cnt);
                    }
                }
                result = true;
            } catch (Exception e) {
                throw e;
            } finally {
                if (finput != null) finput.close();
                if (zinput != null) zinput.close();
                if (foutput != null) foutput.close();
            }
        }
        return result;
    }
}
