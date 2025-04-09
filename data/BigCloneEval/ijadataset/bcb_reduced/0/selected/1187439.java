package net.metasimian.spelunk.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.metasimian.spelunk.statistics.Statistics;

public class ZipReport extends AbstractReport implements Report {

    ZipOutputStream zip;

    ZipEntry entry;

    public ZipReport(OutputStream os) {
        super();
        zip = new ZipOutputStream(os);
        out = new PrintWriter(zip);
    }

    public void log(String eventId, String name, String value) {
        out.println(name + ";" + value);
        out.flush();
    }

    public void dump(String eventId, String message) {
        createEntry(eventId);
        out.write(message);
        out.flush();
    }

    private void createEntry(String id) {
        try {
            if (entry != null) {
                if (id.equals(entry.getName())) return; else zip.closeEntry();
            }
            Statistics.eventCount++;
            entry = new ZipEntry(id);
            zip.putNextEntry(entry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeReport() throws Exception {
        if (entry != null) zip.closeEntry();
        zip.flush();
        zip.close();
    }
}
