package hu.csq.dyneta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author Tamás Cséri
 */
public class ExperimentResultProcessorFile implements ExperimentResultProcessor {

    private String fileName;

    private boolean toZIP;

    private PrintWriter outfile;

    private File tempFile;

    private String rowHeader = "";

    private Logger logger = Logger.getLogger(ExperimentResultProcessorFile.class.getName());

    public ExperimentResultProcessorFile(String fileName, boolean toZIP) {
        this.fileName = fileName;
        this.toZIP = toZIP;
        logger.debug("Opening output file: " + fileName);
        try {
            tempFile = new File(fileName + ".tmp");
            outfile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
        } catch (IOException ex) {
            logger.error("Cannot open output file.", ex);
            throw new RuntimeException(ex);
        }
    }

    public void processHeaderInformation(List<String> statnames) {
        StringBuilder sb = new StringBuilder();
        sb.append("CreatorName");
        sb.append(';');
        sb.append("CreatorParams");
        sb.append(';');
        sb.append("EventName");
        sb.append(';');
        sb.append("EventParams");
        sb.append(';');
        sb.append("EventStep");
        sb.append(';');
        for (String statname : statnames) {
            sb.append(statname);
            sb.append(';');
        }
        outfile.println(sb.substring(0, sb.length() - 1));
    }

    public void processNextExperimentRun(String ncName, String ncParam, String neName, String neParam) {
        StringBuilder sb = new StringBuilder();
        sb.append(ncName);
        sb.append(';');
        sb.append(ncParam);
        sb.append(';');
        sb.append(neName);
        sb.append(';');
        sb.append(neParam);
        sb.append(';');
        rowHeader = sb.toString();
    }

    public void processResultsAfterStep(long step, List<double[]> statvalues) {
        outfile.print(rowHeader);
        outfile.print(step);
        outfile.print(';');
        for (double[] statpart : statvalues) {
            for (int statsi = 0; statsi < statpart.length; statsi++) {
                outfile.print(statpart[statsi]);
                outfile.print(';');
            }
        }
        outfile.println();
    }

    public void close() {
        logger.debug("Closing output file.");
        outfile.close();
        tempFile.renameTo(new File(fileName));
        if (toZIP) {
            logger.debug("ZIPping output file.");
            try {
                ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(fileName + ".zip"));
                zipout.setLevel(9);
                String outfilezipname = fileName.substring(fileName.lastIndexOf(System.getProperty("file.separator")) + 1);
                zipout.putNextEntry(new ZipEntry(outfilezipname));
                FileInputStream fis = new FileInputStream(fileName);
                byte[] buffer = new byte[65536];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zipout.write(buffer, 0, len);
                }
                zipout.close();
                fis.close();
                logger.debug("ZIPping output file ok.");
                logger.debug("Removing " + fileName);
                (new File(fileName)).delete();
            } catch (IOException ex) {
                logger.debug("Error when zipping file", ex);
            }
        }
    }
}
