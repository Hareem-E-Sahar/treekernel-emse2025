package com.lowagie.toolbox.plugins;

import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.IntegerArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import com.lowagie.toolbox.swing.PdfInformationPanel;

/**
 * This tool lets you split a PDF in two separate PDF files.
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Split extends AbstractTool {

    static {
        addVersion("$Id: Split.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
	 * Constructs an Split object.
	 */
    public Split() {
        FileArgument f = new FileArgument(this, "srcfile", "The file you want to split", false, new PdfFilter());
        f.setLabel(new PdfInformationPanel());
        arguments.add(f);
        arguments.add(new FileArgument(this, "destfile1", "The file to which the first part of the original PDF has to be written", true, new PdfFilter()));
        arguments.add(new FileArgument(this, "destfile2", "The file to which the second part of the original PDF has to be written", true, new PdfFilter()));
        arguments.add(new IntegerArgument(this, "pagenumber", "The pagenumber where you want to split"));
    }

    /**
	 * @see com.lowagie.toolbox.AbstractTool#createFrame()
	 */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Split", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Split OPENED ===");
    }

    /**
	 * @see com.lowagie.toolbox.AbstractTool#execute()
	 */
    public void execute() {
        try {
            if (getValue("srcfile") == null) throw new InstantiationException("You need to choose a sourcefile");
            File src = (File) getValue("srcfile");
            if (getValue("destfile1") == null) throw new InstantiationException("You need to choose a destination file for the first part of the PDF");
            File file1 = (File) getValue("destfile1");
            if (getValue("destfile2") == null) throw new InstantiationException("You need to choose a destination file for the second part of the PDF");
            File file2 = (File) getValue("destfile2");
            int pagenumber = Integer.parseInt((String) getValue("pagenumber"));
            PdfReader reader = new PdfReader(src.getAbsolutePath());
            int n = reader.getNumberOfPages();
            System.out.println("There are " + n + " pages in the original file.");
            if (pagenumber < 2 || pagenumber > n) {
                throw new DocumentException("You can't split this document at page " + pagenumber + "; there is no such page.");
            }
            Document document1 = new Document(reader.getPageSizeWithRotation(1));
            Document document2 = new Document(reader.getPageSizeWithRotation(pagenumber));
            PdfWriter writer1 = PdfWriter.getInstance(document1, new FileOutputStream(file1));
            PdfWriter writer2 = PdfWriter.getInstance(document2, new FileOutputStream(file2));
            document1.open();
            PdfContentByte cb1 = writer1.getDirectContent();
            document2.open();
            PdfContentByte cb2 = writer2.getDirectContent();
            PdfImportedPage page;
            int rotation;
            int i = 0;
            while (i < pagenumber - 1) {
                i++;
                document1.setPageSize(reader.getPageSizeWithRotation(i));
                document1.newPage();
                page = writer1.getImportedPage(reader, i);
                rotation = reader.getPageRotation(i);
                if (rotation == 90 || rotation == 270) {
                    cb1.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
                } else {
                    cb1.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                }
            }
            while (i < n) {
                i++;
                document2.setPageSize(reader.getPageSizeWithRotation(i));
                document2.newPage();
                page = writer2.getImportedPage(reader, i);
                rotation = reader.getPageRotation(i);
                if (rotation == 90 || rotation == 270) {
                    cb2.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
                } else {
                    cb2.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                }
            }
            document1.close();
            document2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     * @param arg StringArgument
     */
    public void valueHasChanged(AbstractArgument arg) {
        if (internalFrame == null) {
            return;
        }
    }

    /**
     * Split a PDF in two separate PDF files.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Split tool = new Split();
        if (args.length < 4) {
            System.err.println(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     *
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     * @throws InstantiationException
     * @return File
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue("destfile1");
    }
}
