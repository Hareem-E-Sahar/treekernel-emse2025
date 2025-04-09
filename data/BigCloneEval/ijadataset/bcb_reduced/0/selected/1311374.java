package net.sf.rmoffice.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import net.sf.rmoffice.core.RMSheet;
import net.sf.rmoffice.meta.MetaData;
import net.sf.rmoffice.meta.enums.StatEnum;
import net.sf.rmoffice.ui.models.LongRunningUIModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * 
 */
public class NpcPDFCreator extends AbstractPDFCreator {

    private static final Logger log = LoggerFactory.getLogger(NpcPDFCreator.class);

    private static final ResourceBundle RESOURCE = ResourceBundle.getBundle("conf.i18n.locale");

    public NpcPDFCreator(RMSheet sheet, MetaData data, JFrame parent, LongRunningUIModel longRunningModel) {
        super(sheet, data, parent, longRunningModel);
    }

    /** {@inheritDoc} */
    @Override
    public void internalCreate(OutputStream os, LongRunningUIModel longRunAdapter) throws Exception {
        Document document = createDocument();
        try {
            longRunAdapter.startProgress(2);
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            loadFonts();
            longRunAdapter.workDone(1, "pdf.page.shortpdf.title2");
            PdfContentByte canvas = writer.getDirectContent();
            createPage1(canvas);
            document.close();
            longRunAdapter.workDone(1, "message.exportpdf.successful");
        } finally {
            if (log.isInfoEnabled()) log.info("pdf created");
        }
    }

    private void createPage1(PdfContentByte canvas) throws BadElementException, MalformedURLException, IOException, DocumentException {
        float upperY = headerCustomTitle(canvas, RESOURCE.getString("pdf.page.shortpdf.title2"));
        float LINE_HEIGHT = 12;
        final float minCombatUpperY = 325;
        float combatUpperY = 0;
        float y = upperY - 2 * LINE_HEIGHT;
        y = page1RaceProfArmorDB(canvas, y);
        y = page1Resistance(canvas, y, false);
        combatUpperY = y;
        y = upperY - 2 * LINE_HEIGHT;
        y = page1Stats(canvas, y);
        y = page1Favorites(canvas, y, minCombatUpperY + LINE_HEIGHT, false);
        y = Math.min(y, combatUpperY);
        box(canvas, LEFT_X, upperY - LINE_HEIGHT, PAGE1_LEFTBOX_RIGHTX, y);
        box(canvas, PAGE1_RIGHTBOX_LEFTX, upperY - LINE_HEIGHT, RIGHT_X, y);
        y -= LINE_HEIGHT;
        box(canvas, LEFT_X, y, RIGHT_X, BOTTOM_Y);
        float lineHeight = 11;
        y = page6ConcussionHits(canvas, lineHeight, y);
        y = page6PowerPoints(canvas, lineHeight, y);
        boolean spaceLeft = y > 140;
        y = page6ExhaustionPointsAndMovement(canvas, lineHeight, y, spaceLeft);
        footer(canvas);
    }

    private float page1Stats(PdfContentByte canvas, final float initialY) {
        float LINE_HEIGHT = 14.5f;
        float y = initialY;
        float centerX = PAGE1_RIGHTBOX_LEFTX + (RIGHT_X - PAGE1_RIGHTBOX_LEFTX) / 2;
        float[] xVal = new float[] { PAGE1_RIGHTBOX_LEFTX + 10, 337, 365 };
        canvas.beginText();
        canvas.setFontAndSize(fontBold, 8);
        String[] totalBonus = StringUtils.split(RESOURCE.getString("pdf.page1.stat.bonus.total"));
        int totalBonusIdx = 0;
        if (totalBonus.length > 1) {
            canvas.showTextAligned(Element.ALIGN_CENTER, totalBonus[totalBonusIdx++], xVal[2], y, 0);
        }
        y -= 10;
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page1.stat.header"), xVal[0], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.stat.temp"), xVal[1], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, totalBonus[totalBonusIdx], xVal[2], y, 0);
        canvas.endText();
        y -= LINE_HEIGHT;
        int idx = 0;
        for (StatEnum stat : StatEnum.values()) {
            float xModi = idx < 5f ? 0 : (centerX - PAGE1_RIGHTBOX_LEFTX + 10);
            for (int col = 0; col < xVal.length; col++) {
                String str = null;
                switch(col) {
                    case 0:
                        canvas.beginText();
                        String statStr = stat.getFullI18N();
                        canvas.setFontAndSize(fontRegular, 8);
                        canvas.showTextAligned(Element.ALIGN_LEFT, statStr, xVal[col] + xModi, y, 0);
                        canvas.endText();
                        break;
                    case 1:
                        str = "" + sheet.getStatTemp(stat);
                        break;
                    case 2:
                        str = format(sheet.getStatBonusTotal(stat), false);
                        box(canvas, xVal[col] - 9 + xModi, y + 8, xVal[col] + 10 + xModi, y - 3);
                        break;
                    default:
                        str = "";
                }
                if (col > 0 && col < (xVal.length - 1)) {
                    showUserText(canvas, 8, xVal[col] + xModi, y, "___", Element.ALIGN_CENTER);
                }
                showUserText(canvas, 8, xVal[col] + xModi, y, str, Element.ALIGN_CENTER);
            }
            y -= LINE_HEIGHT;
            idx++;
            if (idx == 5) {
                y = y + 5 * LINE_HEIGHT;
            }
        }
        hline(canvas, PAGE1_RIGHTBOX_LEFTX, y, RIGHT_X);
        y -= LINE_HEIGHT;
        return y;
    }
}
