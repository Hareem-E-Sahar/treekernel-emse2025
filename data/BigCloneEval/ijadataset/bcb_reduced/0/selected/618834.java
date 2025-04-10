package net.bull.javamelody;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Partie du rapport pdf pour les threads sur le serveur.
 * @author Emeric Vernat
 */
class PdfThreadInformationsReport {

    private final List<ThreadInformations> threadInformationsList;

    private final Document document;

    private final DecimalFormat integerFormat = I18N.createIntegerFormat();

    private final boolean stackTraceEnabled;

    private final boolean cpuTimeEnabled;

    private final Font cellFont = PdfFonts.TABLE_CELL.getFont();

    private PdfPTable currentTable;

    private final PdfDocumentFactory pdfDocumentFactory;

    PdfThreadInformationsReport(List<ThreadInformations> threadInformationsList, boolean stackTraceEnabled, PdfDocumentFactory pdfDocumentFactory, Document document) {
        super();
        assert threadInformationsList != null;
        assert pdfDocumentFactory != null;
        assert document != null;
        this.threadInformationsList = threadInformationsList;
        this.pdfDocumentFactory = pdfDocumentFactory;
        this.document = document;
        this.stackTraceEnabled = stackTraceEnabled;
        this.cpuTimeEnabled = !threadInformationsList.isEmpty() && threadInformationsList.get(0).getCpuTimeMillis() != -1;
    }

    void toPdf() throws DocumentException, IOException {
        writeHeader();
        final PdfPCell defaultCell = getDefaultCell();
        boolean odd = false;
        for (final ThreadInformations threadInformations : threadInformationsList) {
            if (odd) {
                defaultCell.setGrayFill(0.97f);
            } else {
                defaultCell.setGrayFill(1);
            }
            odd = !odd;
            writeThreadInformations(threadInformations);
        }
        document.add(currentTable);
        final Paragraph tempsThreads = new Paragraph(I18N.getString("Temps_threads") + '\n', cellFont);
        tempsThreads.setAlignment(Element.ALIGN_RIGHT);
        document.add(tempsThreads);
    }

    void writeDeadlocks() throws DocumentException {
        final List<ThreadInformations> deadlockedThreads = new ArrayList<ThreadInformations>();
        for (final ThreadInformations thread : threadInformationsList) {
            if (thread.isDeadlocked()) {
                deadlockedThreads.add(thread);
            }
        }
        if (!deadlockedThreads.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append('\n');
            sb.append(I18N.getString("Threads_deadlocks"));
            String separator = " ";
            for (final ThreadInformations thread : deadlockedThreads) {
                sb.append(separator);
                sb.append(thread.getName());
                separator = ", ";
            }
            sb.append('\n');
            document.add(new Phrase(sb.toString(), PdfFonts.SEVERE_CELL.getFont()));
        }
    }

    private void writeHeader() throws DocumentException {
        final List<String> headers = createHeaders();
        final int[] relativeWidths = new int[headers.size()];
        Arrays.fill(relativeWidths, 0, headers.size(), 1);
        relativeWidths[0] = 3;
        relativeWidths[3] = 2;
        if (stackTraceEnabled) {
            relativeWidths[4] = 6;
        }
        currentTable = PdfDocumentFactory.createPdfPTable(headers, relativeWidths);
    }

    private List<String> createHeaders() {
        final List<String> headers = new ArrayList<String>();
        headers.add(getI18nString("Thread"));
        headers.add(getI18nString("Demon"));
        headers.add(getI18nString("Priorite"));
        headers.add(getI18nString("Etat"));
        if (stackTraceEnabled) {
            headers.add(getI18nString("Methode_executee"));
        }
        if (cpuTimeEnabled) {
            headers.add(getI18nString("Temps_cpu"));
            headers.add(getI18nString("Temps_user"));
        }
        return headers;
    }

    private void writeThreadInformations(ThreadInformations threadInformations) throws DocumentException, IOException {
        final PdfPCell defaultCell = getDefaultCell();
        defaultCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        addCell(threadInformations.getName());
        defaultCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (threadInformations.isDaemon()) {
            addCell(getI18nString("oui"));
        } else {
            addCell(getI18nString("non"));
        }
        defaultCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        addCell(integerFormat.format(threadInformations.getPriority()));
        defaultCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        final PdfPCell cell = new PdfPCell();
        final Paragraph paragraph = new Paragraph(getDefaultCell().getLeading() + cellFont.getSize());
        paragraph.add(new Chunk(getImage("bullets/" + HtmlThreadInformationsReport.getStateIcon(threadInformations)), 0, -1));
        paragraph.add(new Phrase(String.valueOf(threadInformations.getState()), cellFont));
        cell.addElement(paragraph);
        currentTable.addCell(cell);
        if (stackTraceEnabled) {
            addCell(threadInformations.getExecutedMethod());
        }
        if (cpuTimeEnabled) {
            defaultCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addCell(integerFormat.format(threadInformations.getCpuTimeMillis()));
            addCell(integerFormat.format(threadInformations.getUserTimeMillis()));
        }
    }

    private Image getImage(String resourceFileName) throws DocumentException, IOException {
        return pdfDocumentFactory.getSmallImage(resourceFileName);
    }

    private static String getI18nString(String key) {
        return I18N.getString(key);
    }

    private PdfPCell getDefaultCell() {
        return currentTable.getDefaultCell();
    }

    private void addCell(String string) {
        currentTable.addCell(new Phrase(string, cellFont));
    }
}
