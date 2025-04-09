package org.echarts.edt.sip.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * 
 * @author sg3235
 *
 */
public class SarPackager {

    /**
	 * 
	 * @author sg3235
	 *
	 */
    private class PathAndStream {

        private IPath path;

        private IStreamCreator streamCreator;

        /**
		 * @param path
		 * @param streamCreator
		 */
        public PathAndStream(IPath path, IStreamCreator streamCreator) {
            this.path = path;
            this.streamCreator = streamCreator;
        }

        /**
		 * Returns an InputStream for the file. 
		 * @return
		 * @throws IOException
		 * @throws CoreException
		 */
        public InputStream getStream() throws IOException, CoreException {
            return streamCreator.getStream(path);
        }

        public void complete() {
            streamCreator.complete(path);
        }
    }

    private HashMap entries = new HashMap();

    /**
	 * Constructor 
	 */
    public SarPackager() {
    }

    /**
	 * This method adds the file path and the stream to a map.
	 * @param entryPath    file path from the sar content root
	 * @param pathToLoad   file path from the project root
	 * @param streamMaker  
	 */
    public void addEntry(IPath entryPath, IPath pathToLoad, IStreamCreator streamMaker) {
        entries.put(entryPath, new PathAndStream(pathToLoad, streamMaker));
    }

    /**
	 * This method creates an InputStream for the files represented in the map
	 * and writes them into the SAR file.
	 * @param os
	 * @throws IOException
	 * @throws CoreException
	 */
    public boolean hasEntry(IPath entryPath) {
        return entries.containsKey(entryPath);
    }

    public int fileCount() {
        return entries.size();
    }

    public void write(ZipOutputStream os) throws IOException, CoreException {
        write(os, null);
    }

    public void write(ZipOutputStream os, ISarPackagingMonitor monitor) throws IOException, CoreException {
        Iterator it = entries.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            PathAndStream pns = (PathAndStream) entry.getValue();
            InputStream is = pns.getStream();
            if (monitor != null) monitor.savingEntry((IPath) entry.getKey());
            os.putNextEntry(new ZipEntry(((IPath) entry.getKey()).toPortableString()));
            System.out.println("Writing " + ((IPath) entry.getKey()).toPortableString());
            byte buffer[] = new byte[10240];
            while (true) {
                int nRead = is.read(buffer, 0, buffer.length);
                if (nRead <= 0) {
                    break;
                }
                os.write(buffer, 0, nRead);
            }
            is.close();
            pns.complete();
            if (monitor != null) monitor.entryCompleted((IPath) entry.getKey());
        }
    }

    /**
	 * Recursive method to find all of the files in the package/folder and adds 
	 * them to a map.
	 * @param container      project contents to package 
	 * @param streamMaker
	 * @param root           starting point marker
	 * @throws CoreException
	 */
    public void addProjectDirectory(IContainer container, IStreamCreator streamMaker, IPath root) throws CoreException {
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++) {
            IResource resource = members[i];
            if (resource instanceof IContainer) {
                if (root == null) {
                    System.out.println("Adding " + resource.getName());
                    addProjectDirectory((IContainer) resource, streamMaker, new Path(resource.getName()));
                } else {
                    System.out.println("Adding " + root + "/" + resource.getName());
                    addProjectDirectory((IContainer) resource, streamMaker, root.addTrailingSeparator().append(resource.getName()));
                }
            } else {
                if (root == null) {
                    System.out.println("Adding " + resource.getName());
                    addEntry(new Path(resource.getName()), resource.getProjectRelativePath(), streamMaker);
                } else {
                    System.out.println("Adding " + root + "/" + resource.getName());
                    addEntry(((IPath) root.clone()).addTrailingSeparator().append(resource.getName()), resource.getProjectRelativePath(), streamMaker);
                }
            }
        }
    }
}
