package cn.myapps.util.pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import cn.myapps.constans.Environment;
import cn.myapps.util.StringUtil;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class ObpmPdfDocument {

    BaseFont bfChinese;

    Document document;

    PdfWriter writer;

    PdfPTable table;

    String watermark;

    Font font;

    public ObpmPdfDocument(String webFileName) {
        this(webFileName, "");
    }

    public ObpmPdfDocument(String webFileName, String watermark) {
        try {
            bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            font = new Font(bfChinese, 12f);
            document = new Document(PageSize.A4);
            writer = PdfWriter.getInstance(document, new FileOutputStream(getFileRealPath(webFileName)));
            this.watermark = watermark;
            if (!StringUtil.isBlank(this.watermark)) {
                writer.setPageEvent(new PageNumbersWatermark(this.bfChinese, this.watermark));
            }
            document.open();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        document.close();
    }

    /**
	 * 插入新一页
	 * 
	 */
    public void addPage() {
        document.newPage();
    }

    /**
	 * 插入标题
	 * 
	 * @param title
	 */
    public void addTitle(String title) {
        try {
            Font titleFont = new Font(bfChinese, 14f, Font.BOLD);
            document.add(new Paragraph(title, titleFont));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
	 * 插入表格开始
	 */
    public void addTableStart(int numColumns) {
        table = new PdfPTable(numColumns);
        table.setWidthPercentage(100f);
    }

    /**
	 * 插入数组行
	 * 
	 * @param array
	 */
    public void addArrayRow(String[] array) {
        for (int i = 0; i < array.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(array[i], font));
            cell.setBorder(0);
            table.addCell(cell);
        }
    }

    /**
	 * 插入列
	 * 
	 * @param text
	 */
    public void addCell(String text) {
        addCell(text, 0);
    }

    public void addCell(String text, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (border > 0) {
            cell.setBorder(PdfPCell.BOX);
        } else {
            cell.setBorder(PdfPCell.NO_BORDER);
        }
        table.addCell(cell);
    }

    /**
	 * 插入表格结束
	 */
    public void addTableEnd() {
        try {
            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
	 * 多个PDF合并
	 * @throws IOException 
	 * @throws DocumentException 
	 * */
    public void addPDF(String source, String title) throws IOException, DocumentException {
        int lastString = source.indexOf("_");
        String temPath = null;
        if (lastString != -1) {
            temPath = source.substring(1, lastString);
        }
        String sourcePath = getFileRealPath(temPath).replaceAll("\\\\", "/");
        PdfReader reader = new PdfReader(sourcePath);
        PdfContentByte cb = writer.getDirectContent();
        int pageOfCurrentReaderPDF = 0;
        int pages = reader.getNumberOfPages();
        for (int i = 0; i < pages; i++) {
            document.newPage();
            pageOfCurrentReaderPDF++;
            PdfImportedPage page = writer.getImportedPage(reader, pageOfCurrentReaderPDF);
            cb.addTemplate(page, 0, 0);
        }
    }

    /**
	 * 插入图片
	 */
    public void addImage(String webFileName) {
        try {
            if (StringUtil.isBlank(webFileName)) {
                return;
            }
            int lastIndex = webFileName.indexOf("_");
            if (lastIndex != -1) {
                webFileName = webFileName.substring(0, lastIndex);
            }
            Image image = Image.getInstance(getFileRealPath(webFileName));
            toFitPage(image);
            document.add(image);
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toFitPage(Image image) {
        float pageWidth = document.getPageSize().getWidth() - 90;
        float pageHight = document.getPageSize().getHeight() - 90;
        if (image.getWidth() > pageWidth || image.getHeight() > pageHight) {
            image.scaleToFit(pageWidth, pageHight);
        }
    }

    /**
	 * 插入图片
	 */
    public void addImageRow(String label, String webFileName) {
        try {
            if (StringUtil.isBlank(webFileName)) {
                return;
            }
            int lastIndex = webFileName.indexOf("_");
            if (lastIndex != -1) {
                webFileName = webFileName.substring(0, lastIndex);
            }
            Image image = Image.getInstance(webFileName);
            table.addCell(new Phrase(label, new Font(bfChinese)));
            toFitPage(image);
            table.addCell(image);
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * 获取文件真实路径名称
	 * 
	 * @param webFileName
	 * @return
	 */
    public String getFileRealPath(String webFileName) {
        Environment env = Environment.getInstance();
        String realfilename = env.getRealPath(webFileName);
        return realfilename;
    }

    /**
	 * 插入文本行
	 * 
	 * @param label
	 * @param value
	 */
    public void addTextRow(String label, String value) {
        PdfPCell cell0 = new PdfPCell(new Phrase(label, font));
        PdfPCell cell1 = new PdfPCell(new Phrase(value, font));
        cell0.setBorder(0);
        cell1.setBorder(0);
        table.addCell(cell0);
        table.addCell(cell1);
    }
}
