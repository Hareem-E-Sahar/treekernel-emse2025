package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.Action;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.rvsnoop.Application;
import org.rvsnoop.actions.ExportToFile;
import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.Version;

/**
 * Export the current ledger selction to a ‘snoop record bundle’.
 * <p>
 * A record bundle is just a zip file containing one entry per record, the
 * records use the RvSnoop byte stream format (i.e. the native message format
 * and a bit of additional metadata).
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision: 393 $, $Date: 2008-06-02 10:22:38 -0400 (Mon, 02 Jun 2008) $
 * @since 1.6
 */
public final class ExportToRecordBundle extends ExportToFile {

    static class FileFilter extends javax.swing.filechooser.FileFilter {

        FileFilter() {
            super();
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            final String lower = f.getName().toLowerCase(Locale.ENGLISH);
            return lower.endsWith(".srb") || lower.endsWith(".rbz");
        }

        @Override
        public String getDescription() {
            return "Record Bundles";
        }
    }

    static final long serialVersionUID = 9036056933732494645L;

    public static final String COMMAND = "exportToRecordBundle";

    private static String NAME = "Record Bundle";

    private static String TOOLTIP = "Export the current ledger selction to a record bundle";

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private final DataOutput dataBuffer = new DataOutputStream(buffer);

    private ZipOutputStream zip;

    public ExportToRecordBundle(Application application) {
        super(application, COMMAND, NAME, new FileFilter());
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
    }

    @Override
    protected void writeFooter() throws IOException {
        zip.close();
    }

    @Override
    protected void writeHeader(int numberOfRecords) throws IOException {
        zip = new ZipOutputStream(stream);
        zip.setComment(Version.getAsStringWithName() + " Record Bundle");
    }

    @Override
    protected void writeRecord(Record record, int index) throws IOException {
        buffer.reset();
        RecordSelection.write(record, dataBuffer);
        final byte[] bytes = buffer.toByteArray();
        final ZipEntry entry = new ZipEntry(Integer.toString(index));
        entry.setSize(bytes.length);
        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
    }
}
