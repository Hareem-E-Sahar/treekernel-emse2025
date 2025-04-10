package dk.apaq.vfs.impl.cifs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import dk.apaq.vfs.Directory;
import dk.apaq.vfs.FileSystem;
import dk.apaq.vfs.Node;
import dk.apaq.vfs.Path;

/**
 *
 * @author mzk
 */
public abstract class CifsNode implements Node {

    public CifsNode(CifsFileSystem filesystem, SmbFile file) {
        this.filesystem = filesystem;
        innerFile = file;
    }

    public CifsNode(CifsFileSystem filesystem, String url) throws MalformedURLException {
        this.filesystem = filesystem;
        innerFile = new SmbFile(url);
    }

    SmbFile innerFile;

    CifsFileSystem filesystem;

    protected void deleteRecursive(SmbFile file) throws IOException {
        if (!file.exists()) throw new IOException("The file does not exist.");
        if (!file.isDirectory()) throw new IOException("A file cannot be deleted recursively.");
        if (file.exists()) {
            SmbFile[] files = file.listFiles();
            for (SmbFile currentFile : files) {
                if (currentFile.isDirectory()) deleteRecursive(currentFile); else currentFile.delete();
            }
            file.delete();
        }
    }

    public void moveTo(Directory newParent) throws IOException {
        moveTo(newParent, innerFile.getName());
    }

    public void moveTo(Directory newParent, String newName) throws IOException {
        SmbFile newFile = new SmbFile(((CifsDirectory) newParent).innerFile, newName);
        innerFile.renameTo(newFile);
    }

    public void setName(String name) throws IOException {
        moveTo(getParent(), name);
    }

    public String getName() {
        String name = innerFile.getName();
        name = name.replaceAll("/", "");
        return name;
    }

    public boolean isRoot() {
        try {
            if (innerFile.getPath().equals(((CifsDirectory) filesystem.getRoot()).innerFile.getPath())) return true;
        } catch (FileNotFoundException ex) {
        }
        return false;
    }

    public Directory getParent() throws FileNotFoundException {
        try {
            if (isRoot()) return null;
            return new CifsDirectory(filesystem, innerFile.getParent());
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    public FileSystem getFileSystem() {
        return filesystem;
    }

    public boolean isDirectory() {
        try {
            return innerFile.isDirectory();
        } catch (SmbException ex) {
            Logger.getLogger(CifsNode.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean isFile() {
        try {
            return innerFile.isFile();
        } catch (SmbException ex) {
            Logger.getLogger(CifsNode.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean isHidden() {
        try {
            return innerFile.isHidden();
        } catch (SmbException ex) {
            Logger.getLogger(CifsNode.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public Date getLastModified() {
        return new Date(innerFile.getLastModified());
    }

    public void setLastModified(Date date) {
        try {
            innerFile.setLastModified(date.getTime());
        } catch (SmbException ex) {
            Logger.getLogger(CifsNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int compareTo(Node node) {
        return innerFile.getPath().compareTo(((CifsNode) node).innerFile.getPath());
    }

    public void delete() throws IOException {
        if (isRoot()) throw new IOException("Cannot delete root.");
        if (innerFile.isDirectory() && innerFile.list().length > 0) throw new IOException("Cannot delete directory because it is not empty.");
        innerFile.delete();
    }

    public URI toUri() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean equals(Node node) {
        return innerFile.getPath().equals(((CifsNode) node).innerFile.getPath());
    }

    public boolean equals(Object o) {
        if (o instanceof Node) return equals((Node) o);
        return super.equals(o);
    }

    public int compareTo(Object arg0) {
        return compareTo((Node) arg0);
    }

    public Path getPath() {
        return new Path();
    }

    public boolean canRead() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean canWrite() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getBaseName() {
        String name = innerFile.getName();
        if (name.contains(".")) return name.substring(0, name.lastIndexOf(".") - 1); else return name;
    }

    public String getSuffix() {
        String name = innerFile.getName();
        if (name.contains(".")) return name.substring(name.lastIndexOf(".") + 1); else return null;
    }
}
