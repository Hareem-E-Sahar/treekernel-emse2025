package net.sf.warpcore.cms.servlets;

import java.io.*;
import java.util.*;
import java.net.URLEncoder;
import net.sf.wedgetarian.util.*;
import net.sf.warpcore.cms.value.*;
import net.sf.warpcore.cms.entity.property.PropertyDomain;
import net.sf.warpcore.domperignon.common.*;
import net.sf.warpcore.domperignon.api.*;

public class SimpleDeployer {

    public void deploy(String docRoot, String uri, char[] data) throws IOException {
        write(docRoot, uri, new CharArrayReader(data));
    }

    public void deploy(String docRoot, String uri, InputStream in) throws IOException {
        write(docRoot, uri, in);
    }

    public void deploy(String docRoot, String uri, Reader in) throws IOException {
        write(docRoot, uri, in);
    }

    public void deploy(String docRoot, ContentVO contentVO, String path) throws IOException {
        String encoding = contentVO.getEncoding();
        if ((encoding != null) && !encoding.equalsIgnoreCase("data") && !encoding.equalsIgnoreCase("binary")) {
            deploy(docRoot, path, contentVO.getReader());
        } else {
            deploy(docRoot, path, contentVO.getContentData());
        }
    }

    private void write(String docRoot, String uri, Reader in) throws IOException {
        File f = new File(docRoot, uri.replace('/', File.separatorChar));
        File dir = f.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileWriter fw = new FileWriter(f);
        int c;
        while ((c = in.read()) >= 0) {
            fw.write(c);
        }
        fw.close();
    }

    private void write(String docRoot, String uri, InputStream in) throws IOException {
        File f = new File(docRoot, uri.replace('/', File.separatorChar));
        File dir = f.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        OutputStream out = new FileOutputStream(f);
        int c;
        while ((c = in.read()) >= 0) {
            out.write(c);
        }
        out.close();
    }

    public void copy(String src, String dest) throws IOException {
        syncFile(new File(src), new File(dest));
    }

    protected void syncFile(File src, File dest) throws IOException {
        if (src.exists()) {
            if (!dest.exists()) {
                copyFile(src, dest);
            } else {
                if (src.isDirectory()) {
                    if (dest.isDirectory()) {
                        int i, j;
                        File[] srcChildren = src.listFiles();
                        File[] destChildren = dest.listFiles();
                        for (i = 0; i < srcChildren.length; i++) {
                            File srcChild = srcChildren[i];
                            File destChild = new File(dest, srcChild.getName());
                            syncFile(srcChild, destChild);
                        }
                        for (j = 0; j < destChildren.length; j++) {
                            File destChild = destChildren[j];
                            File srcChild = null;
                            boolean found = false;
                            for (i = 0; i < srcChildren.length; i++) {
                                srcChild = srcChildren[i];
                                if (srcChild.getName().equals(destChild.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                removeFile(destChild);
                            }
                        }
                    } else {
                        removeFile(dest);
                        copyFile(src, dest);
                    }
                } else if (dest.isDirectory() || (src.lastModified() > dest.lastModified())) {
                    removeFile(dest);
                    copyFile(src, dest);
                }
            }
        }
    }

    protected void copyFile(File src, File dest) throws IOException {
        if (src.exists()) {
            if (src.isDirectory()) {
                dest.mkdir();
                File children[] = src.listFiles();
                for (int i = 0; i < children.length; i++) {
                    File srcChild = children[i];
                    File destChild = new File(dest, srcChild.getName());
                    copyFile(srcChild, destChild);
                }
            } else {
                dest.createNewFile();
                OutputStream out = new FileOutputStream(dest);
                InputStream in = new FileInputStream(src);
                int c;
                while ((c = in.read()) >= 0) {
                    out.write(c);
                }
                in.close();
                out.close();
            }
        }
    }

    protected void removeFile(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                File children[] = file.listFiles();
                for (int i = 0; i < children.length; i++) {
                    File child = children[i];
                    if (!(child.getName().equals(".") || child.getName().equals(".."))) {
                        removeFile(child);
                    }
                }
            }
            file.delete();
        }
    }
}
