package com.jeecms.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * ��������zipѹ����
 * 
 * @author liufang
 * 
 */
public class Zipper {

    private static final Logger log = LoggerFactory.getLogger(Zipper.class);

    /**
	 * ����ѹ����
	 * 
	 */
    public static void zip(OutputStream out, List<FileEntry> fileEntrys) {
        new Zipper(out, fileEntrys);
    }

    /**
	 * ����Zipper����
	 * 
	 * @param out
	 *            �����
	 * @param filter
	 *            �ļ����ˣ������˿���Ϊnull��
	 * @param srcFilename
	 *            Դ�ļ�������ж��Դ�ļ������Դ�ļ���Ŀ¼����ô������Ŀ¼��������
	 */
    protected Zipper(OutputStream out, List<FileEntry> fileEntrys) {
        Assert.notEmpty(fileEntrys);
        long begin = System.currentTimeMillis();
        log.debug("��ʼ����ѹ����");
        try {
            try {
                zipOut = new ZipOutputStream(out);
                for (FileEntry fe : fileEntrys) {
                    zip(fe.getFile(), fe.getFilter(), fe.getZipEntry(), fe.getPrefix());
                }
            } finally {
                zipOut.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("����ѹ����ʱ������IO�쳣��", e);
        }
        long end = System.currentTimeMillis();
        log.info("����ѹ����ɹ�����ʱ��{}ms��", end - begin);
    }

    /**
	 * ѹ���ļ�
	 * 
	 * @param srcFile
	 *            Դ�ļ�
	 * @param pentry
	 *            ��ZipEntry
	 * @throws IOException
	 */
    private void zip(File srcFile, FilenameFilter filter, ZipEntry pentry, String prefix) throws IOException {
        ZipEntry entry;
        if (srcFile.isDirectory()) {
            if (pentry == null) {
                entry = new ZipEntry(srcFile.getName());
            } else {
                entry = new ZipEntry(pentry.getName() + "/" + srcFile.getName());
            }
            File[] files = srcFile.listFiles(filter);
            for (File f : files) {
                zip(f, filter, entry, prefix);
            }
        } else {
            if (pentry == null) {
                entry = new ZipEntry(prefix + srcFile.getName());
            } else {
                entry = new ZipEntry(pentry.getName() + "/" + prefix + srcFile.getName());
            }
            FileInputStream in;
            try {
                log.debug("��ȡ�ļ���{}", srcFile.getAbsolutePath());
                in = new FileInputStream(srcFile);
                try {
                    zipOut.putNextEntry(entry);
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        zipOut.write(buf, 0, len);
                    }
                    zipOut.closeEntry();
                } finally {
                    in.close();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("����ѹ����ʱ��Դ�ļ������ڣ�" + srcFile.getAbsolutePath(), e);
            }
        }
    }

    private byte[] buf = new byte[1024];

    private ZipOutputStream zipOut;

    public static class FileEntry {

        private FilenameFilter filter;

        private String parent;

        private File file;

        private String prefix;

        public FileEntry(String parent, String prefix, File file, FilenameFilter filter) {
            this.parent = parent;
            this.prefix = prefix;
            this.file = file;
            this.filter = filter;
        }

        public FileEntry(String parent, File file) {
            this.parent = parent;
            this.file = file;
        }

        public FileEntry(String parent, String prefix, File file) {
            this(parent, prefix, file, null);
        }

        public ZipEntry getZipEntry() {
            if (StringUtils.isBlank(parent)) {
                return null;
            } else {
                return new ZipEntry(parent);
            }
        }

        public FilenameFilter getFilter() {
            return filter;
        }

        public void setFilter(FilenameFilter filter) {
            this.filter = filter;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getPrefix() {
            if (prefix == null) {
                return "";
            } else {
                return prefix;
            }
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }
}
