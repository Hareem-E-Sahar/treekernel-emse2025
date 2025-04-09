package DE.FhG.IGD.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * This class implements an abstract {@link Resource Resource}.
 * Several methods are implemented with a default behaviour.
 * This behaviour is modelled on the <i>atomic</i> methods.
 * This class is thread-safe. However, on multiple concurrent
 * writes to this resource the process who finishes last wins.
 * The data being written last is inserted into the resource
 * while any previous changes are discarded.
 *
 * @author Volker Roth
 * @version "$Id: MemoryResource.java 117 2000-12-06 17:47:39Z vroth $"
 */
public class MemoryResource extends AbstractResource {

    /**
     * This map holds the resources of this instance.
     */
    private Map map_ = Collections.synchronizedMap(new TreeMap());

    /**
     * The target to which this instance's contents are
     * flushed.
     */
    private Object flushtarget_;

    /**
     * Creates an empty instance. Note that this constructor
     * does take (and therefor does not set) a target to
     * load and to flush data from.
     */
    public MemoryResource() {
    }

    /**
     * Creates an instance that flushes its contents to the
     * file with the given name. The file is accessed the
     * first time when {@link #flush flush} is called. If
     * the file access fails then the data stored in this
     * instance must be stored by other means such as
     * copying this resource to another. The contents are
     * compressed into a ZIP stream before writing them
     * to the file. If a file already exists with this name
     * then it is overwritten.
     *
     * @param name The name of the ZIP file to flush to.
     *   The ZIP file is re-written completely. Any data
     *   contained in a pre-existing ZIP file with that
     *   name is lost.
     */
    public MemoryResource(String flushtarget) {
        flushtarget_ = new File(flushtarget);
    }

    /**
     * Creates an instance that flushes its contents to the
     * given file. The file is accessed the
     * first time when {@link #flush flush} is called. If
     * the file access fails then the data stored in this
     * instance must be stored by other means such as
     * copying this resource to another. The contents are
     * compressed into a ZIP stream before writing them
     * to the file. If a file already exists with this name
     * then it is overwritten.
     *
     * @param name The name of the ZIP file to flush to.
     *   The ZIP file is re-written completely. Any data
     *   contained in a pre-existing ZIP file with that
     *   name is lost.
     */
    public MemoryResource(File flushtarget) {
        flushtarget_ = flushtarget;
    }

    /**
     * Creates an instance that flushes its contents to
     * the given resource. The resource is accessed the
     * first time when {@link #flush flush}is called.
     *
     * @param resource The resource the data is written
     *   to.
     */
    public MemoryResource(Resource flushtarget) {
        flushtarget_ = flushtarget;
    }

    /**
     * This method loads a MemoryResource from an input
     * stream that contains ZIP data.
     *
     * @param in The input stream to read from.
     */
    public void load(InputStream in) throws ZipException, IOException {
        ZipInputStream zip;
        ZipEntry ze;
        byte[] buffer;
        byte[] tmp;
        Outlet out;
        int l;
        if (in instanceof ZipInputStream) {
            zip = (ZipInputStream) in;
        } else {
            zip = new ZipInputStream(in);
        }
        map_.clear();
        buffer = new byte[BUFFER_SIZE];
        while ((ze = zip.getNextEntry()) != null) {
            if (ze.isDirectory()) {
                continue;
            }
            out = (Outlet) getOutputStream(ze.getName());
            if (out == null) {
                throw new IllegalStateException("Can't write to memory!");
            }
            while ((l = zip.read(buffer)) > 0) {
                out.write(buffer, 0, l);
            }
            tmp = out.toByteArray();
            out.close();
            zip.closeEntry();
            map_.put(ze.getName(), tmp);
        }
    }

    /**
     * Writes the contents of the memory delegate to the
     * given output stream. The output is piped through
     * a ZIP stream.
     *
     * @param out The output stream to which the contents
     *   of the memory delegate are written.
     * @exception IOException if guess what...
     */
    public void store(OutputStream out) throws IOException {
        int u;
        Iterator i;
        ZipEntry ze;
        Map.Entry entry;
        ZipOutputStream zip;
        u = 0;
        zip = new ZipOutputStream(out);
        synchronized (map_) {
            for (i = map_.entrySet().iterator(); i.hasNext(); ) {
                entry = (Map.Entry) i.next();
                ze = new ZipEntry((String) entry.getKey());
                zip.putNextEntry(ze);
                u++;
                zip.write((byte[]) entry.getValue());
                zip.closeEntry();
            }
        }
    }

    /**
     * This method flushes the contents of this Resource
     * to the flush target.
     */
    public void flush() throws IOException {
        OutputStream out;
        if (flushtarget_ == null) {
            return;
        }
        Iterator i;
        if (flushtarget_ instanceof File) {
            out = new FileOutputStream((File) flushtarget_);
            try {
                store(out);
            } finally {
                out.close();
            }
            return;
        }
        if (flushtarget_ instanceof Resource) {
            store((Resource) flushtarget_);
            return;
        }
        throw new IllegalStateException("Flashtarget is of an unknown class!");
    }

    /**
     *
     * @return The input stream or <code>null</code> if the
     *   requested resource cannot be found.
     * @param name The name identifying the data of which
     *   an input stream is requested.
     */
    public InputStream getInputStream(String name) throws IOException {
        byte[] b;
        name = canonicalName(name);
        if (name.length() == 0) {
            throw new IllegalArgumentException("Name has zero length!");
        }
        b = (byte[]) map_.get(name);
        if (b == null) {
            return null;
        }
        return new ByteArrayInputStream(b);
    }

    /**
     *
     * @return The output stream.
     * @param name The name identifying the data of which
     *   an output stream is requested.
     * @exception IOException if guess what...
     * @exception UnsupportedOperationException if this
     *   operation is not supported by this resource.
     */
    public OutputStream getOutputStream(String name) throws IOException, UnsupportedOperationException {
        return new Outlet(canonicalName(name));
    }

    /**
     *
     * @return The output stream.
     * @param name The name identifying the data of which
     *   an output stream is requested.
     * @param append If <code>true</code> then the data
     *   is appended.
     * @exception IOException if guess what...
     * @exception UnsupportedOperationException if this
     *   operation is not supported by this resource.
     */
    public OutputStream getOutputStream(String name, boolean append) throws IOException, UnsupportedOperationException {
        name = canonicalName(name);
        if (append == false) {
            return new Outlet(name);
        }
        byte[] b;
        Outlet outlet;
        b = (byte[]) map_.get(name);
        if (b == null) {
            return new Outlet(name);
        }
        outlet = new Outlet(name, (int) (1.2f * b.length));
        outlet.write(b);
        return outlet;
    }

    /**
     *
     * @param name The name of the resource that should be
     *   deleted.
     * @exception IOException if guess what...
     * @exception UnsupportedOperationException if this
     *   operation is not supported by this resource.
     */
    public void delete(String name) throws IOException, UnsupportedOperationException {
        map_.remove(canonicalName(name));
    }

    /**
     * Deletes all contents of this Resource.
     *
     * @exception IOException if guess what...
     * @exception UnsupportedOperationException if this
     *   operation is not supported by this resource.
     */
    public void deleteAll() throws IOException, UnsupportedOperationException {
        map_.clear();
    }

    /**
     * Returns a list of names of the resource items in
     * existance.
     *
     * @return The resource names.
     */
    public List list() throws IOException {
        List list;
        synchronized (map_) {
            list = new ArrayList(map_.size());
            list.addAll(map_.keySet());
        }
        return list;
    }

    /**
     * This method lists all resource items in the &quot;
     * directory&quot; with the given name. That is, all
     * resource items starting with the prefix name plus
     * one more component separated from the name by a slash.
     *
     * @param name The directory to list.
     * @return The resource names.
     */
    public List list(String name) throws IOException {
        String s;
        Iterator i;
        ArrayList list;
        name = canonicalName(name) + "/";
        list = new ArrayList(32);
        synchronized (map_) {
            for (i = map_.keySet().iterator(); i.hasNext(); ) {
                s = (String) i.next();
                if (s.startsWith(name)) {
                    list.add(s);
                }
            }
        }
        list.trimToSize();
        return list;
    }

    /**
     * Returns a list of resources items; the names of the
     * resource items satisfies the given filter. If the
     * filter is <code>null</code> then all names are
     * accepted. The filter is passed the complete name
     * of the resource item.<p>
     *
     * This implementation list the complete Resource and then
     * filters the names such that the filter may not block
     * access to the resource.
     *
     * @param filter The {@link ResourceFilter ResourceFilter}
     *   that is asked to accept resource names.
     */
    public List list(ResourceFilter filter) throws IOException {
        List list;
        String s;
        Iterator i;
        ArrayList al;
        list = list();
        if (filter == null) {
            return list;
        }
        al = new ArrayList(list.size());
        for (i = list.iterator(); i.hasNext(); ) {
            s = (String) i.next();
            if (filter.accept(s)) {
                al.add(s);
            }
        }
        al.trimToSize();
        return al;
    }

    /**
     * If resource item <code>new</code> already exists then it is
     * replaced by <code>old</code>. The rename operation is <i>
     * atomic</i>.
     *
     * @param source The resource item to be renamed.
     * @param destination The new name.
     * @exception IOException if the source does not exist or
     *   any other I/O error occurs.
     */
    public void rename(String source, String destination) throws IOException, UnsupportedOperationException {
        byte[] b;
        source = canonicalName(source);
        destination = canonicalName(destination);
        synchronized (map_) {
            b = (byte[]) map_.remove(source);
            if (b == null) {
                throw new IOException("Resource does not exist: " + source);
            }
            map_.put(destination, b);
        }
    }

    /**
     * Returns a subview of the given resource. The name space
     * of the new resource is rooted at the given name. Items
     * must be addressed relative to the new name space. The
     * new resource contains all items of this resource that
     * start with the given name.
     *
     * @param name The root of the name space of the new
     *   resource.
     * @return The new resource.
     */
    public Resource subview(String name) throws IOException {
        name = canonicalName(name);
        if (name.length() == 0) {
            return this;
        }
        return new SubviewResource(this, name);
    }

    /**
     * This method returns <code>true</code> if a resource
     * item with the given name exists.
     *
     * @param name The name of the resource.
     * @return <code>true</code> iff the resource with the
     *   given name exists.
     */
    public boolean exists(String name) {
        return map_.containsKey(canonicalName(name));
    }

    /**
     * Returns the length of the resource item with the
     * given name.
     *
     * @return The length of the resource item with the
     *   given name.
     * @exception FileNotFoundException if no such resource
     *   exists.
     */
    public long length(String name) throws IOException {
        byte[] b;
        name = canonicalName(name);
        synchronized (map_) {
            if (!map_.containsKey(name)) {
                throw new FileNotFoundException(name + " does not exist!");
            }
            b = (byte[]) map_.get(name);
        }
        return b.length;
    }

    /**
     * This class is required for transforming the output
     * stream to which data is written into a byte array
     * that is stored in the memory delegate. Method {@link
     * #getOutputStream getOutputStream} returns an instance
     * of this class. Upon closing the outlet, it transfers
     * its internal byte array buffer into the {@link map_
     * map} that keeps the association between resource
     * names and data.<p>
     *
     * Consequently, ther caller needs to properly close
     * this stream in order for the written data to become
     * visible. Otherwise, all changes are lost.
     *
     * @author Volker Roth
     */
    public class Outlet extends ByteArrayOutputStream {

        /**
         * The name of the entry or <code>null</code>
         * if the outlet is already closed.
         */
        protected String name_;

        /**
         * Creates a new instance of an outlet. The data
         * written to the outlet is transferred back into
         * the map when it is closed.
         *
         * @param name The name of the resource.
         */
        private Outlet(String name) {
            if (name == null) {
                throw new NullPointerException("Name");
            }
            name = canonicalName(name);
            if (name.length() == 0) {
                throw new IllegalArgumentException("Name has zero length!");
            }
            name_ = name;
        }

        /**
         * Creates a new instance of an outlet. The data
         * written to the outlet is transferred back into
         * the map when it is closed. The initial capacity
         * is set to the given one.
         *
         * @param name The name of the resource.
         */
        public Outlet(String name, int capacity) {
            super(capacity);
            if (name == null) {
                throw new NullPointerException("Name");
            }
            name = canonicalName(name);
            if (name.length() == 0) {
                throw new IllegalArgumentException("Name has zero length!");
            }
            name_ = name;
        }

        /**
         * Closes the outlet and puts the bytes back into
         * the map.
         */
        public synchronized void close() {
            if (name_ != null) {
                map_.put(name_, toByteArray());
            }
            name_ = null;
        }

        /**
         * Try to close in the case of garbage collection.
         */
        protected void finalize() {
            synchronized (this) {
                close();
            }
        }
    }
}
