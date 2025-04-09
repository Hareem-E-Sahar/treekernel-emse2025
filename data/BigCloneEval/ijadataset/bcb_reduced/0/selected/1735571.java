package com.jspx.io.zip;

import com.jspx.utils.FileUtil;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 压缩文件工具类。
 * 此类完成一般的新建、增加、导出、删除操作以及完成对压缩文件的内容的解析。
 */
public class Zipfile {

    private static final Log logger = LogFactory.getLog(Zipfile.class);

    private boolean isScaned = false;

    private boolean isChanged = false;

    private File selfFile;

    private static File workDirectory = new File(".");

    private static boolean haveSetWorkDirectory = false;

    private ArrayList entries = new ArrayList();

    private ArrayList entryNames = new ArrayList();

    private int count = 0;

    private long totalSize = 0;

    /**
     * 缺省构造方法，一般用于创建一个新的压缩文件，但是是一个临时文件。
     */
    public Zipfile() {
        try {
            selfFile = File.createTempFile("jzj", ".tmp", workDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造方法，如果指定的文件不存在则创建一个指定名称的新文件。
     *
     * @param fileName 文件名
     */
    public Zipfile(String fileName) {
        selfFile = new File(fileName);
        createNewFile();
    }

    /**
     * 构造方法，如果指定的文件不存在则创建一个指定名称的新文件。
     *
     * @param file 文件
     */
    public Zipfile(File file) {
        selfFile = file;
        createNewFile();
    }

    /**
     * 设置系统的工作目录，设置以后就不能修改，直到JVM退出。
     * 如果指定的目录不存在则为缺省的当前目录
     *
     * @param directoryName 目录名
     */
    public static void setWorkDirectory(String directoryName) {
        if (haveSetWorkDirectory) {
            return;
        }
        File tmp = new File(directoryName);
        if (tmp.isDirectory()) {
            workDirectory = tmp;
        }
    }

    /**
     * 设置系统的工作目录，设置以后就不能修改，直到JVM退出。
     * 如果指定的目录不存在则为缺省的当前目录
     *
     * @param directory 目录
     */
    public static void setWorkDirectory(File directory) {
        if (haveSetWorkDirectory) {
            return;
        }
        if (directory != null && directory.isDirectory()) {
            workDirectory = directory;
        }
    }

    /**
     * 向自己代表的压缩包中新增文件，如果该文件已经存在则增加失败。
     *
     * @param path     新增的文件所在的目录
     * @param fileName 新增的文件的文件名
     */
    public void addFile(File path, String fileName) {
        addFileToSelf(path, fileName, false);
    }

    /**
     * 向自己代表的压缩包中新增文件。
     *
     * @param path      新增的文件所在的目录
     * @param fileName  新增的文件的文件名
     * @param overWrite 是否覆盖已有的文件
     */
    public void addFile(File path, String fileName, boolean overWrite) {
        addFileToSelf(path, fileName, overWrite);
    }

    /**
     * 向自己代表的压缩包中新增文件。
     *
     * @param path      新增的文件所在的目录
     * @param fileName  新增的文件的文件名
     * @param overWrite 是否覆盖已有的文件
     */
    private void addFileToSelf(File path, String fileName, boolean overWrite) {
        BufferedInputStream bin;
        ZipOutputStream zout;
        ZipInputStream zin;
        File tmpzip = null;
        ZipEntry addedEntry = new ZipEntry(fileName);
        try {
            tmpzip = File.createTempFile("jzj", ".tmp", workDirectory);
            zin = new ZipInputStream(new FileInputStream(selfFile));
            zout = new ZipOutputStream(new FileOutputStream(tmpzip));
            ZipEntry entry;
            int len;
            byte[] b = new byte[4096];
            while ((entry = zin.getNextEntry()) != null) {
                if (!isSameEntry(entry, addedEntry)) {
                    zout.putNextEntry(new ZipEntry(entry.getName()));
                    while ((len = zin.read(b)) != -1) {
                        zout.write(b, 0, len);
                    }
                    zout.closeEntry();
                    zin.closeEntry();
                } else if (overWrite) {
                } else {
                    zout.close();
                    zin.close();
                    tmpzip.delete();
                    tmpzip = null;
                    throw new ZipException(ZipException.ENTRYEXIST);
                }
            }
            bin = new BufferedInputStream(new FileInputStream(new File(path, fileName)));
            zout.putNextEntry(addedEntry);
            while ((len = bin.read(b)) != -1) {
                zout.write(b, 0, len);
            }
            zout.closeEntry();
            zout.close();
            zin.close();
            String selfFileName = selfFile.getPath();
            selfFile.delete();
            tmpzip.renameTo(new File(selfFileName));
            selfFile = new File(selfFileName);
            isChanged = true;
            count++;
        } catch (Exception e) {
            if (tmpzip != null) {
                tmpzip.delete();
            }
            e.printStackTrace();
        }
    }

    /**
     * 向自己代表的压缩包中新增多个文件，如果已经存在则不会添加。
     *
     * @param path      新增的文件所在的目录
     * @param fileNames 新增的文件的文件名
     */
    public void addFiles(File path, String[] fileNames) {
        addFilesToSelf(path, fileNames, false);
    }

    /**
     * 向自己代表的压缩包中新增多个文件。
     *
     * @param path      新增的文件所在的目录
     * @param fileNames 新增的文件的文件名
     * @param overWrite 设置在已经存在该文件时是否进行覆盖
     */
    public void addFiles(File path, String[] fileNames, boolean overWrite) {
        addFilesToSelf(path, fileNames, overWrite);
    }

    /**
     * 向自己代表的压缩包中新增多个文件。
     *
     * @param path      新增的文件所在的目录
     * @param fileNames 新增的文件的文件名
     * @param overWrite 设置在已经存在该文件时是否进行覆盖
     */
    private void addFilesToSelf(File path, String[] fileNames, boolean overWrite) {
        BufferedInputStream bin;
        ZipOutputStream zout;
        ZipInputStream zin;
        File tmpzip = null;
        ZipEntry[] addedEntries = new ZipEntry[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            addedEntries[i] = new ZipEntry(fileNames[i]);
        }
        try {
            tmpzip = File.createTempFile("jzj", ".tmp", workDirectory);
            zin = new ZipInputStream(new FileInputStream(selfFile));
            zout = new ZipOutputStream(new FileOutputStream(tmpzip));
            ZipEntry entry;
            int len = 0;
            byte[] b = new byte[4096];
            while ((entry = zin.getNextEntry()) != null) {
                if (!isContainEntry(entry, addedEntries)) {
                    zout.putNextEntry(new ZipEntry(entry.getName()));
                    while ((len = zin.read(b)) != -1) {
                        zout.write(b, 0, len);
                    }
                    zout.closeEntry();
                    zin.closeEntry();
                } else if (!overWrite) {
                    zout.close();
                    zin.close();
                    tmpzip.delete();
                    tmpzip = null;
                    throw new ZipException(ZipException.ENTRYEXIST);
                }
            }
            for (int i = 0; i < addedEntries.length; i++) {
                bin = new BufferedInputStream(new FileInputStream(new File(path, fileNames[i])));
                zout.putNextEntry(addedEntries[i]);
                while ((len = bin.read(b)) != -1) {
                    zout.write(b, 0, len);
                }
                zout.closeEntry();
                count++;
            }
            zout.close();
            zin.close();
            String slefFileName = selfFile.getPath();
            logger.debug("rename to:" + slefFileName);
            selfFile.delete();
            tmpzip.renameTo(new File(slefFileName));
            selfFile = new File(slefFileName);
            isChanged = true;
        } catch (Exception e) {
            if (tmpzip != null) {
                tmpzip.delete();
            }
            e.printStackTrace();
        }
    }

    /**
     * 得到压缩包中的文件数。
     *
     * @return 压缩包中的文件数
     */
    public int size() {
        return count;
    }

    /**
     * 得到压缩包中的所有文件的大小的和。
     *
     * @return 压缩包中的所有文件的大小的和
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * 重新命名压缩包。
     *
     * @param fileName 期望的文件名
     */
    public void renameTo(String fileName) {
        if (fileName == null) {
            return;
        }
        File target = new File(fileName);
        if (target.exists()) {
            return;
        }
        selfFile.renameTo(target);
    }

    /**
     * 扫描压缩包取得全部需要的信息，如果已经扫描过而且自上次扫描后没有变化则不重新扫描。
     */
    public void scan() {
        if (selfFile == null) {
            return;
        }
        if (isChanged || !isScaned) {
            scanSelf();
        }
    }

    /**
     * 扫描压缩包取得全部需要的信息。
     */
    public void rescan() {
        if (selfFile == null) {
            return;
        }
        scanSelf();
    }

    /**
     * 判断自身是否包含指定的项目。
     *
     * @param entryName 项目名称
     * @return 存在返回true，否则返回false
     */
    public boolean isContainEntry(String entryName) {
        scan();
        return entryNames.contains(entryName);
    }

    /**
     * 扫描压缩包取得全部需要的信息。
     */
    @SuppressWarnings("unchecked")
    private void scanSelf() {
        entries.clear();
        count = 0;
        ZipFileRecord record;
        try {
            ZipFile file = new ZipFile(selfFile);
            Enumeration enu = file.entries();
            ZipEntry entry;
            while (enu.hasMoreElements()) {
                entry = (ZipEntry) enu.nextElement();
                if (entry.isDirectory() || entry.getName().equals("..\\")) {
                    continue;
                }
                record = new ZipFileRecord(entry);
                entries.add(record);
                totalSize = totalSize + record.getSize();
                entryNames.add(record.entry.getName());
                count++;
            }
            isScaned = true;
        } catch (IOException e) {
        }
    }

    /**
     * 将指定的项目导出为文件，如果导出目录中已经存在该文件则不会覆盖。
     *
     * @param entryName 需要导出的项目名称
     * @param fileName  导出后的文件名
     */
    public void extractEntryToFile(String entryName, String fileName) {
        extractEntry(entryName, fileName, false);
    }

    /**
     * 将指定的项目导出为文件。
     *
     * @param entryName 需要导出的项目名称
     * @param fileName  导出后的文件名
     * @param overWrite 文件已经存在时是否进行覆盖
     */
    public void extractEntryToFile(String entryName, String fileName, boolean overWrite) {
        extractEntry(entryName, fileName, overWrite);
    }

    /**
     * 将指定的项目导出为文件。
     *
     * @param entryName 需要导出的项目名称
     * @param fileName  导出后的文件名
     * @param overWrite 文件已经存在时是否进行覆盖
     */
    private void extractEntry(String entryName, String fileName, boolean overWrite) {
        if (FileUtil.isFileExist(fileName) && overWrite == false) {
            return;
        } else {
            try {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(selfFile));
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equals(entryName)) {
                        byte[] buf = new byte[4096];
                        BufferedInputStream bin = new BufferedInputStream(zin);
                        FileUtil.makeDirectory(fileName);
                        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(fileName));
                        while (bin.read(buf, 0, 1) != -1) {
                            bout.write(buf, 0, 1);
                        }
                        bout.close();
                        bin.close();
                    }
                    zin.closeEntry();
                }
                zin.close();
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * 将指定的多个项目分别导出为文件，在已经存在的情况下不进行覆盖。
     *
     * @param entryNames 需要导出的项目名称
     * @param fileNames  导出后的文件名
     */
    public void extractEntriesToFiles(String[] entryNames, String[] fileNames) {
        extractEntries(entryNames, fileNames, false);
    }

    /**
     * 将指定的多个项目分别导出为文件。
     *
     * @param entryNames 需要导出的项目名称
     * @param fileNames  导出后的文件名
     * @param overWrite  文件已经存在时是否进行覆盖
     */
    public void extractEntriesToFiles(String[] entryNames, String[] fileNames, boolean overWrite) {
        extractEntries(entryNames, fileNames, overWrite);
    }

    /**
     * 将指定的多个项目分别导出为文件。
     *
     * @param entryNames 需要导出的项目名称
     * @param fileNames  导出后的文件名
     * @param overWrite  文件已经存在时是否进行覆盖
     */
    private void extractEntries(String[] entryNames, String[] fileNames, boolean overWrite) {
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(selfFile));
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                int index = getContainEntry(entry.getName(), entryNames);
                if ((index != -1) && (overWrite || !FileUtil.isFileExist(fileNames[index]))) {
                    byte[] buf = new byte[4096];
                    BufferedInputStream bin = new BufferedInputStream(zin);
                    FileUtil.makeDirectory(fileNames[index]);
                    BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(fileNames[index]));
                    while (bin.read(buf, 0, 1) != -1) {
                        bout.write(buf, 0, 1);
                    }
                    bout.close();
                    bin.close();
                }
                zin.closeEntry();
            }
            zin.close();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * 从压缩包中删除指定的项目。
     *
     * @param entry 要删除的项目名
     */
    public void deleteEntry(String entry) {
        ZipOutputStream zout = null;
        ZipInputStream zin = null;
        File tmpzip = null;
        try {
            tmpzip = File.createTempFile("zip", ".tmp", new File("."));
            zin = new ZipInputStream(new FileInputStream(selfFile));
            zout = new ZipOutputStream(new FileOutputStream(tmpzip));
            ZipEntry ze;
            int len = 0;
            byte[] b = new byte[4096];
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.getName().equals(entry)) {
                    zin.closeEntry();
                    count--;
                    continue;
                }
                zout.putNextEntry(new ZipEntry(ze.getName()));
                logger.debug(ze.getName());
                while ((len = zin.read(b)) != -1) {
                    zout.write(b, 0, len);
                }
                zout.closeEntry();
                zin.closeEntry();
            }
            zout.close();
            zin.close();
            String slefFileName = selfFile.getPath();
            selfFile.delete();
            tmpzip.renameTo(new File(slefFileName));
            selfFile = new File(slefFileName);
            isChanged = true;
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * 从压缩包中删除指定的项目。
     *
     * @param entries 要删除的项目名
     */
    public void deleteEntries(String[] entries) {
        ZipOutputStream zout = null;
        ZipInputStream zin = null;
        File tmpzip = null;
        try {
            tmpzip = File.createTempFile("zip", ".tmp", new File("."));
            zin = new ZipInputStream(new FileInputStream(selfFile));
            zout = new ZipOutputStream(new FileOutputStream(tmpzip));
            ZipEntry ze;
            int len = 0;
            byte[] b = new byte[4096];
            while ((ze = zin.getNextEntry()) != null) {
                if (getContainEntry(ze.getName(), entries) != -1) {
                    zin.closeEntry();
                    count--;
                    continue;
                }
                zout.putNextEntry(new ZipEntry(ze.getName()));
                while ((len = zin.read(b)) != -1) {
                    zout.write(b, 0, len);
                }
                zout.closeEntry();
                zin.closeEntry();
            }
            zout.close();
            zin.close();
            String slefFileName = selfFile.getPath();
            selfFile.delete();
            tmpzip.renameTo(new File(slefFileName));
            selfFile = new File(slefFileName);
            isChanged = true;
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * 得到压缩包的文件名。
     *
     * @return 压缩包的文件名
     */
    public String getName() {
        return selfFile.getName();
    }

    /**
     * 得到压缩包的全文件名，即绝对路径名。
     *
     * @return 压缩包的全文件名
     */
    public String getFullName() {
        return selfFile.getAbsolutePath();
    }

    /**
     * 得到压缩包所在的目录的目录名。
     *
     * @return 压缩包所在的目录的目录名
     */
    public String getParentPath() {
        return selfFile.getParent();
    }

    /**
     * 得到压缩包中的所有项目的枚举。
     *
     * @return 压缩包中的所有项目的枚举
     */
    public Iterator entries() {
        return entries.iterator();
    }

    /**
     * 得到压缩包中的所有项目的项目名的数组。
     *
     * @return 压缩包中的所有项目的项目名的数组
     */
    public String[] getEntryNames() {
        String[] names = new String[this.entries.size()];
        Iterator entries = entries();
        int i = 0;
        while (entries.hasNext()) {
            names[i++] = (String) (((ZipFileRecord) entries.next()).get(0));
        }
        return names;
    }

    /**
     * 得到压缩包中的所有项目的项目的数组。
     *
     * @return 压缩包中的所有项目的项目的数组
     */
    public ZipFileRecord[] getEntries() {
        ZipFileRecord[] records = new ZipFileRecord[count];
        for (int i = 0; i < records.length; i++) {
            records[i] = (ZipFileRecord) entries.get(i);
        }
        return records;
    }

    /**
     * 判断两个项目是否相同。
     *
     * @param one 项目一
     * @param two 项目二
     * @return 如果两个项目的项目名相同则返回true，否则返回false
     */
    private boolean isSameEntry(ZipEntry one, ZipEntry two) {
        return one.getName().equals(two.getName());
    }

    /**
     * 判断在项目数组中是否存在指定的项目。
     *
     * @param one    指定的项目
     * @param others 项目数组
     * @return 存在返回true，否则返回false
     */
    private boolean isContainEntry(ZipEntry one, ZipEntry[] others) {
        for (ZipEntry other : others) {
            if (isSameEntry(one, other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 得到指定项目在项目数组中的索引。
     *
     * @param one    指定项目
     * @param others 项目数组
     * @return 指定项目在项目数组中的索引，不存在时返回-1
     */
    private int getContainEntry(String one, String[] others) {
        for (int i = 0; i < others.length; i++) {
            if (one.equals(others[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 如果自身代表的压缩包不存在则创建一个新文件。
     */
    private void createNewFile() {
        if (!selfFile.exists()) {
            try {
                selfFile.createNewFile();
            } catch (IOException e) {
            }
        }
    }
}
