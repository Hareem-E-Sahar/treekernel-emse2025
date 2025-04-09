package org.one.stone.soup.wiki.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.*;
import org.one.stone.soup.wiki.file.manager.Resource;
import org.one.stone.soup.wiki.file.manager.ResourceFolder;
import org.one.stone.soup.wiki.file.manager.ResourceStore;

/**
	* The Zipper class is a helper class that overlays an easy to use set
	* of methods on the java.util.zip classes. It allows for multiple
	* entries to the zip file, each specified as a local file name.
	* A static method is also given that will build a zip file of
	* a single local file.
	*
*/
public class WikiZipper {

    private ZipOutputStream zOut;

    private OutputStream oStream;

    public WikiZipper(OutputStream oStream) throws IOException {
        this.oStream = oStream;
        zOut = new ZipOutputStream(oStream);
        zOut.setLevel(9);
        zOut.setMethod(Deflater.DEFLATED);
    }

    public void addEntry(String zipEntryName, InputStream iStream) throws IOException {
        this.addEntry(zipEntryName, iStream, true);
    }

    public void addEntry(String zipEntryName, InputStream iStream, boolean compress) throws IOException {
        zipEntryName = zipEntryName.replace('\\', '/');
        if (iStream == null) {
            if (zipEntryName.length() < 1 || zipEntryName.charAt(zipEntryName.length() - 2) != '/') {
                zipEntryName = zipEntryName + "/";
            }
            ZipEntry zEntry = new ZipEntry(zipEntryName);
            try {
                zOut.putNextEntry(zEntry);
            } catch (Exception e) {
            }
            return;
        }
        ZipEntry zEntry = new ZipEntry(zipEntryName);
        if (compress == true) zEntry.setMethod(ZipEntry.DEFLATED); else zEntry.setMethod(ZipEntry.STORED);
        zOut.putNextEntry(zEntry);
        byte[] data = new byte[10000];
        int inByte = iStream.read(data);
        while (inByte != -1) {
            zOut.write(data, 0, inByte);
            inByte = iStream.read(data);
        }
        zOut.closeEntry();
        iStream.close();
    }

    public void close() throws IOException {
        zOut.close();
        oStream.close();
    }

    public static void zip(InputStream in, OutputStream out, String zipEntryName) throws IOException {
        ZipOutputStream zOut = new ZipOutputStream(out);
        zOut.setLevel(9);
        zOut.setMethod(Deflater.DEFLATED);
        ZipEntry zEntry = new ZipEntry(zipEntryName);
        zOut.putNextEntry(zEntry);
        byte[] data = new byte[10000];
        int inByte = in.read(data);
        while (inByte != -1) {
            zOut.write(data, 0, inByte);
            inByte = in.read(data);
        }
        zOut.closeEntry();
        zOut.close();
        in.close();
    }

    public void zipAll(ResourceStore resourceStore, ResourceFolder folder) throws IOException {
        _zipAll(resourceStore, folder, folder);
        close();
    }

    public void _zipAll(ResourceStore resourceStore, ResourceFolder root, ResourceFolder folder) throws IOException {
        ResourceFolder[] folderList = resourceStore.listResourceFolders(folder);
        for (int loop = 0; loop < folderList.length; loop++) {
            _zipAll(resourceStore, root, folderList[loop]);
        }
        Resource[] list = resourceStore.listResources(folder);
        for (int loop = 0; loop < list.length; loop++) {
            String entryName = list[loop].getPath() + "/" + list[loop].getName();
            if (entryName.equals(root.getPath() + "/" + root.getName() + ".wiki.zip")) {
                continue;
            }
            if (entryName.charAt(0) == '/') {
                entryName = entryName.substring(1);
            }
            addEntry(entryName, resourceStore.getInputStream(list[loop]));
        }
    }
}
