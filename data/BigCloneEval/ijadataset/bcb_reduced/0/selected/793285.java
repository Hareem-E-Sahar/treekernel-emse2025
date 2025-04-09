package org.sopera.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.sopware.toolsuite.admintool.exceptions.AdminToolException;
import org.sopware.toolsuite.admintool.exceptions.ErrorCodeEnum;

/**
 * This utility class is used to zip/unzip operation over file system.
 *
 * @author atelesh
 */
public class ZipUtils {

    private static final String ZIP_PATH_SEPARATOR = "/";

    private static final int BUFFER = 1024;

    /**
	 * Unzips content of ZIP file to the specified directory.
	 *
	 * @param zipFile
	 *            File, the ZIP file to be unzipped
	 * @param directory
	 *            File, the directory where content of ZIP file will be unzipped
	 * @throws AdminToolException
	 */
    public static void unZipAllToDirectory(File zipFile, File directory) throws AdminToolException {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            BufferedOutputStream out = null;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    File outFile = new File(directory, entry.getName());
                    if (!outFile.getParentFile().exists()) {
                        outFile.getParentFile().mkdirs();
                    }
                    int count;
                    byte[] buff = new byte[BUFFER];
                    try {
                        out = new BufferedOutputStream(new FileOutputStream(outFile), BUFFER);
                        while ((count = zis.read(buff, 0, BUFFER)) != -1) {
                            out.write(buff, 0, count);
                        }
                        out.flush();
                    } catch (IOException e) {
                        throw new AdminToolException(ErrorCodeEnum.FILE_OUTPUT, new Object[] { outFile.getPath() });
                    } finally {
                        if (null != out) {
                            try {
                                out.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new AdminToolException(ErrorCodeEnum.FILE_LOAD, new Object[] { zipFile.getPath() });
        } finally {
            if (null != zis) {
                try {
                    zis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
	 * Zips all files from the specified directory into ZIP file.
	 *
	 * @param zipFile
	 *            File, the ZIP file to be unzipped
	 * @param directory
	 *            File, the directory where content of ZIP file will be unzipped
	 * @param includePassedDirectory
	 *            boolean, whether specified directory should be included into
	 *            result ZIP file or it content only
	 * @throws AdminToolException
	 */
    public static File zipAllFromDirectory(File zipFile, File directory, boolean includePassedDirectory) throws AdminToolException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            addFolderToZip((includePassedDirectory) ? "" : null, directory, zos);
            zos.flush();
        } catch (IOException e) {
            throw new AdminToolException(ErrorCodeEnum.FILE_OUTPUT, new Object[] { zipFile.getPath() });
        } finally {
            if (null != zos) {
                try {
                    zos.close();
                } catch (IOException e) {
                }
            }
        }
        return zipFile;
    }

    /**
	 * Write the content of the file in a new ZipEntry, named path + file name,
	 * of the zip stream. The result is that the file will be in the path folder
	 * in the generated archive.
	 *
	 * @param zipPath
	 *            String, the relative path with the root archive.
	 * @param srcFile
	 *            File, the file to add
	 * @param zos
	 *            ZipOutputStram, the stream to use to write the given file.
	 * @throws IOException
	 */
    private static void addToZip(String zipPath, File srcFile, ZipOutputStream zos) throws IOException {
        if (srcFile.isDirectory()) {
            addFolderToZip(zipPath, srcFile, zos);
        } else {
            byte[] buff = new byte[BUFFER];
            int len;
            FileInputStream in = null;
            try {
                in = new FileInputStream(srcFile);
                zos.putNextEntry(new ZipEntry(buildZipPathPrefix(zipPath) + srcFile.getName()));
                while ((len = in.read(buff)) > 0) {
                    zos.write(buff, 0, len);
                }
                zos.closeEntry();
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
	 * add the folder to the zip stream.
	 *
	 * @param zipPath
	 *            String, the relative path with the root archive. (if 'null'
	 *            folder itself will not include into zip)
	 * @param srcFolder
	 *            File, the file to add
	 * @param zos
	 *            ZipOutputStream, the stream to use to write the given file.
	 * @throws IOException
	 */
    private static void addFolderToZip(String zipPath, File srcFolder, ZipOutputStream zos) throws IOException {
        final String zipFolderPath = (null == zipPath) ? "" : buildZipPathPrefix(zipPath) + srcFolder.getName();
        for (File file : srcFolder.listFiles()) {
            addToZip(zipFolderPath, file, zos);
        }
    }

    private static String buildZipPathPrefix(String zipPath) {
        return (null == zipPath || "".equals(zipPath)) ? "" : zipPath + ZIP_PATH_SEPARATOR;
    }
}
