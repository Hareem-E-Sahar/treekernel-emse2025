package net.sourceforge.olympos.oaw.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.openarchitectureware.workflow.WorkflowContext;
import org.openarchitectureware.workflow.issues.Issues;
import org.openarchitectureware.workflow.lib.AbstractWorkflowComponent;
import org.openarchitectureware.workflow.monitor.ProgressMonitor;

public class AddToZip extends AbstractWorkflowComponent {

    protected String sourceFile = null;

    protected String targetFile = null;

    protected String fileToAdd = null;

    protected String newEntry = null;

    public void setSourceFile(final String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setTargetFile(final String targetFile) {
        this.targetFile = targetFile;
    }

    public void setNewEntry(final String newEntry) {
        this.newEntry = newEntry;
    }

    public void setFileToAdd(final String fileToAdd) {
        this.fileToAdd = fileToAdd;
    }

    @Override
    public void checkConfiguration(Issues issues) {
        check(issues);
    }

    private boolean check(Issues issues) {
        boolean result = true;
        if (sourceFile != null) {
            if (!isValidFile(sourceFile)) {
                issues.addError("SourceFile " + sourceFile + " is not accessable");
                result = false;
            }
        } else {
            issues.addError("No SourceFile given");
            result = false;
        }
        if (targetFile == null) {
            issues.addError("No TargetFile given");
            result = false;
        }
        if (fileToAdd != null) {
            if (!isValidFile(fileToAdd)) {
                issues.addError("FileToAdd " + fileToAdd + " is not accessable");
                result = false;
            }
        } else {
            issues.addError("No FileToAdd given");
            result = false;
        }
        if (newEntry == null) {
            issues.addError("No NewEntry given");
        }
        return result;
    }

    private boolean isValidFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.canRead();
    }

    @Override
    public void invoke(WorkflowContext context, ProgressMonitor monitor, Issues issues) {
        if (!check(issues)) {
            return;
        }
        try {
            byte[] buf = new byte[0xFFFF];
            ZipInputStream zin = new ZipInputStream(new FileInputStream(sourceFile));
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFile));
            ZipEntry entry = zin.getNextEntry();
            while (entry != null) {
                out.putNextEntry(new ZipEntry(entry.getName()));
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                entry = zin.getNextEntry();
            }
            zin.close();
            File file = new File(fileToAdd);
            InputStream in = new FileInputStream(file);
            out.putNextEntry(new ZipEntry(newEntry));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
            out.close();
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();
            try {
                sw.close();
            } catch (IOException e1) {
            }
            issues.addError("Exception occured!\n" + sw.toString());
        }
    }
}
