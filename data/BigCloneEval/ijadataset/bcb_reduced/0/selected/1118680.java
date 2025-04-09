package cross.commands.workflow;

import cross.io.FileTools;
import cross.io.misc.DefaultConfigurableFileFilter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * If configured to do so, zips all elements of a given <code>IWorkflow</code>
 * matching the given <code>FileFilter</code>. Marks directories and files which
 * are unmatched for deletion on exit of the virtual machine if configured.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Data
@Slf4j
public class WorkflowZipper {

    private IWorkflow workflow = null;

    private FileFilter fileFilter = new DefaultConfigurableFileFilter();

    private boolean zipWorkflow = true;

    private boolean deleteOnExit = false;

    private HashSet<String> zipEntries = new HashSet<String>();

    private FileTools fileTools;

    private void addZipEntry(final int bufsize, final ZipOutputStream zos, final byte[] input_buffer, final File file) throws IOException {
        log.debug("Adding zip entry for file {}", file);
        if (file.exists() && file.isFile()) {
            final ZipEntry zip_entry = new ZipEntry(file.getName());
            if (this.zipEntries.contains(file.getName())) {
                log.info("Skipping duplicate zip entry {}", file.getName());
                return;
            } else {
                this.zipEntries.add(file.getName());
            }
            zos.putNextEntry(zip_entry);
            final FileInputStream in = new FileInputStream(file);
            final BufferedInputStream source = new BufferedInputStream(in, bufsize);
            int len = 0;
            while ((len = source.read(input_buffer, 0, bufsize)) != -1) {
                zos.write(input_buffer, 0, len);
            }
            zos.flush();
            source.close();
            zos.closeEntry();
        } else {
            log.warn("Skipping nonexistant file or directory {}", file);
        }
    }

    /**
     * Saves the currently assigned workflow elements, matching currently
     * assigned FileFilter to File. Marks all files for deletion on exit.
     * 
     * @param f
     * @return
     */
    public boolean save(final File f) {
        if (this.zipWorkflow) {
            this.zipEntries.clear();
            final int bufsize = 1024;
            final File zipFile = f;
            ZipOutputStream zos;
            try {
                final FileOutputStream fos = new FileOutputStream(zipFile);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                log.info("Created zip output stream");
                final byte[] input_buffer = new byte[bufsize];
                final Iterator<IWorkflowResult> iter = this.workflow.getResults();
                File basedir = fileTools.prependDefaultDirsWithPrefix("", null, this.workflow.getStartupDate());
                log.info("marked basedir for deletion on exit: {}", basedir);
                if (this.deleteOnExit) {
                    basedir.deleteOnExit();
                }
                log.info("setting basedir to parent file: {}", basedir.getParentFile());
                basedir = basedir.getParentFile();
                while (iter.hasNext()) {
                    final IWorkflowResult iwr = iter.next();
                    if (iwr instanceof IWorkflowFileResult) {
                        final IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                        final File file = iwfr.getFile();
                        log.info("Retrieving file result {}", file);
                        final File parent = file.getParentFile();
                        log.info("Retrieving parent of file result {}", parent);
                        if (parent.getAbsolutePath().startsWith(basedir.getAbsolutePath()) && !parent.getAbsolutePath().equals(basedir.getAbsolutePath())) {
                            log.info("Marking file and parent for deletion");
                            if (this.deleteOnExit) {
                                parent.deleteOnExit();
                                file.deleteOnExit();
                            }
                        }
                        if (file.getAbsolutePath().startsWith(basedir.getAbsolutePath())) {
                            log.info("Marking file for deletion");
                            if (this.deleteOnExit) {
                                file.deleteOnExit();
                            }
                        }
                        if ((this.fileFilter != null) && !this.fileFilter.accept(file)) {
                            continue;
                        } else {
                            log.info("Adding zip entry!");
                            addZipEntry(bufsize, zos, input_buffer, file);
                        }
                    }
                }
                try {
                    zos.flush();
                    zos.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        } else {
            log.debug("Configured to not zip Workflow results!");
            return false;
        }
    }

    public boolean save(final File parentDir, final String filename) {
        return save(new File(parentDir, filename));
    }
}
