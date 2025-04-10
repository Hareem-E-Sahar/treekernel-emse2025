package com.lowagie.toolbox.plugins;

import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.OptionArgument;
import com.lowagie.toolbox.arguments.filters.ImageFilter;
import com.lowagie.toolbox.arguments.filters.PdfFilter;

/**
 * Converts a Tiff file to a PDF file.
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Tiff2Pdf extends AbstractTool {

    static {
        addVersion("$Id: Tiff2Pdf.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
	 * Constructs a Tiff2Pdf object.
	 */
    public Tiff2Pdf() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, "srcfile", "The file you want to convert", false, new ImageFilter(false, false, false, false, false, true)));
        arguments.add(new FileArgument(this, "destfile", "The file to which the converted TIFF has to be written", true, new PdfFilter()));
        OptionArgument oa = new OptionArgument(this, "pagesize", "Pagesize");
        oa.addOption("A4", "A4");
        oa.addOption("Letter", "LETTER");
        oa.addOption("Original format", "ORIGINAL");
        arguments.add(oa);
    }

    /**
	 * @see com.lowagie.toolbox.AbstractTool#createFrame()
	 */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Tiff2Pdf", true, false, true);
        internalFrame.setSize(550, 250);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Tiff2Pdf OPENED ===");
    }

    /**
	 * @see com.lowagie.toolbox.AbstractTool#execute()
	 */
    public void execute() {
        try {
            if (getValue("srcfile") == null) throw new InstantiationException("You need to choose a sourcefile");
            File tiff_file = (File) getValue("srcfile");
            if (getValue("destfile") == null) throw new InstantiationException("You need to choose a destination file");
            File pdf_file = (File) getValue("destfile");
            RandomAccessFileOrArray ra = new RandomAccessFileOrArray(tiff_file.getAbsolutePath());
            int comps = TiffImage.getNumberOfPages(ra);
            boolean adjustSize = false;
            Document document = new Document(PageSize.A4);
            float width = PageSize.A4.getWidth() - 40;
            float height = PageSize.A4.getHeight() - 120;
            if ("ORIGINAL".equals(getValue("pagesize"))) {
                Image img = TiffImage.getTiffImage(ra, 1);
                if (img.getDpiX() > 0 && img.getDpiY() > 0) {
                    img.scalePercent(7200f / img.getDpiX(), 7200f / img.getDpiY());
                }
                document.setPageSize(new Rectangle(img.getScaledWidth(), img.getScaledHeight()));
                adjustSize = true;
            } else if ("LETTER".equals(getValue("pagesize"))) {
                document.setPageSize(PageSize.LETTER);
                width = PageSize.LETTER.getWidth() - 40;
                height = PageSize.LETTER.getHeight() - 120;
            }
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdf_file));
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            for (int c = 0; c < comps; ++c) {
                Image img = TiffImage.getTiffImage(ra, c + 1);
                if (img != null) {
                    if (img.getDpiX() > 0 && img.getDpiY() > 0) {
                        img.scalePercent(7200f / img.getDpiX(), 7200f / img.getDpiY());
                    }
                    if (adjustSize) {
                        document.setPageSize(new Rectangle(img.getScaledWidth(), img.getScaledHeight()));
                        document.newPage();
                        img.setAbsolutePosition(0, 0);
                    } else {
                        if (img.getScaledWidth() > width || img.getScaledHeight() > height) {
                            if (img.getDpiX() > 0 && img.getDpiY() > 0) {
                                float adjx = width / img.getScaledWidth();
                                float adjy = height / img.getScaledHeight();
                                float adj = Math.min(adjx, adjy);
                                img.scalePercent(7200f / img.getDpiX() * adj, 7200f / img.getDpiY() * adj);
                            } else img.scaleToFit(width, height);
                        }
                        img.setAbsolutePosition(20, 20);
                        document.newPage();
                        document.add(new Paragraph(tiff_file + " - page " + (c + 1)));
                    }
                    cb.addImage(img);
                    System.out.println("Finished page " + (c + 1));
                }
            }
            ra.close();
            document.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            System.err.println(e.getMessage());
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
     * Converts a tiff file to PDF.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Tiff2Pdf tool = new Tiff2Pdf();
        if (args.length < 2) {
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
        return (File) getValue("destfile");
    }
}
