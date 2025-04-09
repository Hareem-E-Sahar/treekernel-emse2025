package org.dyndns.fichtner.purgeannotationrefs.optimizer;

import static org.dyndns.fichtner.purgeannotationrefs.Util.isClass;
import static org.dyndns.fichtner.purgeannotationrefs.Util.isZip;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Optimizer to optimize class objects inside zip files (recursively).
 * 
 * @author Peter Fichtner
 */
public class ZipOptimizer {

    private static final int BUFFER_SIZE = 4096;

    private final ClassOptimizer classOptimizer;

    /**
	 * Creates a new ZipOptimizer doing delegation calls to the passed
	 * ClassOptimizer.
	 * 
	 * @param classOptimizer ClassOptimizer to use
	 */
    public ZipOptimizer(final ClassOptimizer classOptimizer) {
        this.classOptimizer = classOptimizer;
    }

    /**
	 * Optimizes the classes inside the passed zip stream and write the result
	 * to the passed output stream.
	 * 
	 * @param input stream to read from
	 * @param output stream to write to
	 * @throws IOException IO error
	 */
    public void optimize(final ZipInputStream input, final ZipOutputStream output) throws IOException {
        ZipEntry entry;
        final Set<String> names = new HashSet<String>();
        while ((entry = input.getNextEntry()) != null) {
            final String name = entry.getName();
            if (names.add(name)) {
                processZipEntry(input, output, entry);
            }
        }
    }

    /**
	 * Process the passed ZipEntry.
	 * 
	 * @param input stream to read from
	 * @param output stream to write to
	 * @param entry the entry to process
	 * @throws IOException IO error
	 */
    protected void processZipEntry(final ZipInputStream input, final ZipOutputStream output, final ZipEntry entry) throws IOException {
        output.putNextEntry(cloneZipEntry(entry));
        if (!entry.isDirectory()) {
            final String name = entry.getName();
            if (isClass(name)) {
                processClass(name, input, output);
            } else if (isZip(name)) {
                processNestedZip(name, input, output);
            } else {
                processResource(name, input, output);
            }
        }
        output.closeEntry();
        input.closeEntry();
    }

    /**
	 * Process the passed nested zip file.
	 * 
	 * @param name of the nested zip file
	 * @param input stream to read from
	 * @param output stream to write to
	 * 
	 * @throws IOException IO error
	 */
    protected void processNestedZip(String name, final ZipInputStream input, final ZipOutputStream output) throws IOException {
        optimize(input, output);
    }

    /**
	 * Process the passed class.
	 * 
	 * @param name of the passed class
	 * @param input stream to read from
	 * @param output stream to write to
	 * @throws IOException IO error
	 */
    protected void processClass(String name, final ZipInputStream input, final ZipOutputStream output) throws IOException {
        this.classOptimizer.optimize(input, output);
    }

    /**
	 * Process the passed resource (non-class, non-zip).
	 * 
	 * @param name name of the resource
	 * @param input stream to read from
	 * @param output stream to write to
	 * @throws IOException IO error
	 */
    protected void processResource(String name, final ZipInputStream input, final ZipOutputStream output) throws IOException {
        int count;
        final byte[] buffer = new byte[BUFFER_SIZE];
        while ((count = input.read(buffer, 0, buffer.length)) != -1) {
            output.write(buffer, 0, count);
        }
    }

    private static ZipEntry cloneZipEntry(final ZipEntry entry) {
        final ZipEntry result = new ZipEntry(entry.getName());
        result.setExtra(entry.getExtra());
        result.setTime(entry.getTime());
        result.setMethod(entry.getMethod());
        result.setComment(entry.getComment());
        if (entry.getCrc() != -1) {
            result.setCrc(entry.getCrc());
        }
        if (entry.getSize() != -1) {
            result.setSize(entry.getSize());
        }
        return result;
    }
}
