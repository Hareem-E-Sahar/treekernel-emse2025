package net.sf.filePiper.processors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import net.sf.filePiper.model.ExecutionPhase;
import net.sf.filePiper.model.FileProcessor;
import net.sf.filePiper.model.FileProcessorEnvironment;
import net.sf.filePiper.model.InputFileInfo;
import net.sf.filePiper.model.StatusHolder;
import net.sf.sfac.gui.editor.ObjectEditor;
import net.sf.sfac.gui.editor.cmp.ReadOnlyObjectEditor;
import net.sf.sfac.setting.Settings;

/**
 * Processor zipping all input streams in one output zip.
 * 
 * @author BEROL
 */
public class ZipProcessor implements FileProcessor {

    Logger log = Logger.getLogger(ZipProcessor.class);

    private ZipOutputStream zipStream;

    private InputFileInfo zipInfo;

    private StatusHolder holder = new StatusHolder() {

        @Override
        protected String getRunningMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append("Zipping ");
            appendCount(getInputFileCount(), "file", sb);
            sb.append(" (");
            appendCount(getByteCount(), "byte", sb);
            sb.append(")...");
            return sb.toString();
        }

        @Override
        protected String getDoneMessage() {
            StringBuilder sb = new StringBuilder();
            appendCount(getByteCount(), "byte", sb);
            sb.append(" of ");
            appendCount(getInputFileCount(), "file", sb);
            sb.append(" zipped");
            if (getInputFileCount() > 1) {
                sb.append(" (");
                appendCount(getByteCount() / getInputFileCount(), "byte", sb);
                sb.append(" per file)");
            }
            sb.append(".");
            return sb.toString();
        }
    };

    public String getProcessorName() {
        return "Zip";
    }

    public void init(Settings sett) {
    }

    public int getOutputCardinality(int inputCardinality) {
        return ONE;
    }

    public void process(InputStream is, InputFileInfo info, FileProcessorEnvironment env) throws IOException {
        ZipOutputStream zip = getZipOutputStream(info, env);
        zip.putNextEntry(new ZipEntry(info.getProposedRelativePath()));
        holder.inputFileStarted();
        int i;
        while (((i = is.read()) >= 0) && env.shouldContinue()) {
            zip.write(i);
            holder.bytesProcessed(1);
        }
        zip.closeEntry();
    }

    private ZipOutputStream getZipOutputStream(InputFileInfo info, FileProcessorEnvironment env) throws IOException {
        if (zipStream == null) {
            String oldExt = info.getProposedExtension();
            info.setProposedExtension("zip");
            OutputStream os = env.getOutputStream(info);
            zipStream = new ZipOutputStream(os);
            info.setProposedExtension(oldExt);
            zipInfo = info;
        } else {
            zipInfo.mergeInfo(info);
        }
        return zipStream;
    }

    public void startBatch(FileProcessorEnvironment env) throws IOException {
        holder.reset(ExecutionPhase.STARTING);
        zipStream = null;
        zipInfo = null;
    }

    public void endBatch(FileProcessorEnvironment env) throws IOException {
        zipStream.close();
        holder.setCurrentPhase(env.getCurrentPhase());
        zipStream = null;
        zipInfo = null;
    }

    public String getStatusMessage() {
        return holder.getStatusMessage();
    }

    public ObjectEditor getEditor() {
        return new ReadOnlyObjectEditor("Zip all the input files to one single output");
    }
}
