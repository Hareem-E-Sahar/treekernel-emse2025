package net.sf.rmoffice.pdf;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.rmoffice.RMOffice;
import net.sf.rmoffice.core.Equipment;
import net.sf.rmoffice.core.RMSheet;
import net.sf.rmoffice.core.Rank;
import net.sf.rmoffice.core.items.MagicalItem;
import net.sf.rmoffice.meta.ISkill;
import net.sf.rmoffice.meta.MetaData;
import net.sf.rmoffice.meta.Shield;
import net.sf.rmoffice.meta.enums.LengthUnit;
import net.sf.rmoffice.meta.enums.ResistanceEnum;
import net.sf.rmoffice.meta.enums.StatEnum;
import net.sf.rmoffice.ui.actions.DesktopAction;
import net.sf.rmoffice.ui.models.LongRunningUIModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;

/**
 * 
 */
public abstract class AbstractPDFCreator implements IPDFCreator {

    public static final int MAX_EQUIPMENT_LINES = 59;

    private static final Logger log = LoggerFactory.getLogger(AbstractPDFCreator.class);

    private static final ResourceBundle RESOURCE = ResourceBundle.getBundle("conf.i18n.locale");

    protected static final float UPPER_Y = 758.5f;

    protected static final int BOTTOM_Y = 50;

    protected static final int PAGE1_RIGHTBOX_LEFTX = 240;

    protected static final int PAGE1_LEFTBOX_RIGHTX = 225;

    protected static final float PAGE1_LEFTBOX_LINE_HEIGHT = 11f;

    protected static final int RIGHT_X = 555;

    protected static final int LEFT_X = 55;

    private final LongRunningUIModel longRunningModel;

    protected final RMSheet sheet;

    protected final MetaData data;

    protected BaseFont fontRegular;

    protected BaseFont fontBold;

    protected BaseFont fontUser;

    protected BaseFont fontUserPlain;

    protected BaseFont fontHeadline;

    protected BaseFont fontWidget;

    private final JFrame parent;

    public AbstractPDFCreator(RMSheet sheet, MetaData data, JFrame parent, LongRunningUIModel longRunningModel) {
        this.sheet = sheet;
        this.data = data;
        this.parent = parent;
        this.longRunningModel = longRunningModel;
    }

    /** {@inheritDoc} */
    @Override
    public final void doCreate(final String outputFilePath) {
        Thread t = new Thread("generator thread") {

            @Override
            public void run() {
                FileOutputStream outputFileStream = null;
                try {
                    outputFileStream = new FileOutputStream(outputFilePath);
                    internalCreate(outputFileStream, longRunningModel);
                    new DesktopAction(new File(outputFilePath)).actionPerformed(new ActionEvent(parent, 0, "open.pdf"));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    Writer sOut = new StringWriter();
                    e.printStackTrace(new PrintWriter(sOut));
                    JOptionPane.showMessageDialog(parent, RESOURCE.getString("error.exportpdf") + "\n\n" + e.getClass().getName() + " " + sOut.toString());
                } finally {
                    if (outputFileStream != null) {
                        try {
                            outputFileStream.close();
                        } catch (IOException e) {
                            log.error("Could not close file", e);
                        }
                    }
                    longRunningModel.done();
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /**
	 * Implementor must handle the {@link LongRunningUIModel#startProgress(int)} and {@link LongRunningUIModel#workDone(int, String)}.
	 * Finishing the progress adapter is not necessary.
	 * 
	 * @param outputFileStream the file output stream, not {@code null}
	 * @param longRunAdapter
	 * @throws Exception
	 */
    protected abstract void internalCreate(OutputStream outputFileStream, LongRunningUIModel longRunAdapter) throws Exception;

    /**
	 * Loads the fonts.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
    protected void loadFonts() throws DocumentException, IOException {
        if (log.isDebugEnabled()) log.debug("loading fonts");
        fontHeadline = BaseFont.createFont("fonts/TrajanPro-Regular.otf", BaseFont.WINANSI, BaseFont.EMBEDDED, true);
        fontHeadline.setSubset(true);
        fontRegular = BaseFont.createFont("fonts/ACaslonPro-Regular.otf", BaseFont.WINANSI, BaseFont.EMBEDDED, true);
        fontRegular.setSubset(true);
        fontBold = BaseFont.createFont("fonts/ACaslonPro-Bold.otf", BaseFont.WINANSI, BaseFont.EMBEDDED, true);
        fontBold.setSubset(true);
        fontUser = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.WINANSI, false);
        fontUserPlain = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        fontWidget = BaseFont.createFont(BaseFont.ZAPFDINGBATS, BaseFont.WINANSI, false);
    }

    /**
	 * Draws the footer with copyright text.
	 * 
	 * @param canvas
	 */
    protected void footer(PdfContentByte canvas) {
        canvas.beginText();
        canvas.setFontAndSize(fontRegular, 6);
        canvas.showTextAligned(Element.ALIGN_RIGHT, RESOURCE.getString("pdf.footer"), 555, 42, 0);
        canvas.showTextAligned(Element.ALIGN_RIGHT, RESOURCE.getString("pdf.copyright"), 555, 34, 0);
        canvas.endText();
    }

    protected void line(PdfContentByte canvas, float x1, float y1, float x2, float y2) {
        canvas.moveTo(x1, y1);
        canvas.lineTo(x2, y2);
        canvas.stroke();
    }

    /**
	 * vertial line
	 * 
	 * @param canvas
	 * @param x
	 * @param y1
	 * @param y2
	 */
    protected void vline(PdfContentByte canvas, float x, float y1, float y2) {
        line(canvas, x, y1, x, y2);
    }

    /**
	 * horizontal line
	 * 
	 * @param canvas
	 * @param x1
	 * @param y
	 * @param x2
	 */
    protected void hline(PdfContentByte canvas, float x1, float y, float x2) {
        line(canvas, x1, y, x2, y);
    }

    protected void box(PdfContentByte canvas, float upperLeftX, float upperLeftY, float bottomRightX, float bottomRightY) {
        canvas.moveTo(upperLeftX, upperLeftY);
        canvas.lineTo(bottomRightX, upperLeftY);
        canvas.lineTo(bottomRightX, bottomRightY);
        canvas.lineTo(upperLeftX, bottomRightY);
        canvas.closePathStroke();
    }

    public static String format(int number, boolean hideZero) {
        if (number > 0) {
            return "+" + number;
        } else if (hideZero && number == 0) {
            return "";
        }
        return "" + number;
    }

    protected float header2(PdfContentByte canvas, int page) throws BadElementException, MalformedURLException, IOException, DocumentException {
        return headerCustomTitle(canvas, RESOURCE.getString("pdf.page" + page + ".title2"));
    }

    protected float headerCustomTitle(PdfContentByte canvas, String title) throws BadElementException, MalformedURLException, IOException, DocumentException {
        URL imageUrl = getClass().getResource("/images/rmlogo.png");
        Image logo = Image.getInstance(imageUrl);
        logo.setAbsolutePosition(365f, 792f);
        logo.scaleToFit(170, 120);
        canvas.addImage(logo, false);
        box(canvas, LEFT_X, 780, RIGHT_X, 767);
        canvas.beginText();
        canvas.setFontAndSize(fontHeadline, 14);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page.title"), 92, 818, 0);
        canvas.showTextAligned(Element.ALIGN_LEFT, title, 92, 802, 0);
        canvas.setFontAndSize(fontHeadline, 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page2.character"), 60, 771, 0);
        float x1 = fontHeadline.getWidthPoint(RESOURCE.getString("pdf.page2.character"), 8) + 65;
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("ui.basic.level") + ":", 300, 771, 0);
        float x2 = fontHeadline.getWidthPoint(RESOURCE.getString("ui.basic.level") + ":", 8) + 305;
        canvas.endText();
        showUserText(canvas, 8, x1, 771, sheet.getCharacterName());
        showUserText(canvas, 8, x2, 771, "" + sheet.getLevel());
        return 771;
    }

    protected void showUserText(PdfContentByte canvas, int fontSize, float x, float y, String text) {
        showUserText(canvas, fontSize, x, y, text, Element.ALIGN_LEFT);
    }

    protected void showUserText(PdfContentByte canvas, int fontSize, float x, float y, String text, int alignment) {
        if (text != null) {
            canvas.beginText();
            canvas.setFontAndSize(fontUser, fontSize);
            canvas.showTextAligned(alignment, text, x, y, 0);
            canvas.endText();
        }
    }

    protected void labeledUserText(PdfContentByte canvas, String label, String text, float x, float y, float x2, BaseFont labelFont, int fontSize) {
        float x1 = labelFont.getWidthPoint(label, fontSize) + x + 4;
        canvas.beginText();
        canvas.setFontAndSize(labelFont, fontSize);
        canvas.showTextAligned(Element.ALIGN_LEFT, label, x, y, 0);
        if (x2 > x1) {
            float widthUnderScore = labelFont.getWidthPoint("_", fontSize);
            float length = x2 - x1;
            String line = StringUtils.repeat("_", (int) Math.floor(length / widthUnderScore));
            canvas.showTextAligned(Element.ALIGN_RIGHT, line, x2 - 4, y, 0);
        }
        canvas.endText();
        showUserText(canvas, fontSize, x1, y, text);
    }

    protected Image loadImage(String fullPathOfImage) {
        String imageStr = null;
        try {
            imageStr = fullPathOfImage + ".jpg";
            URL imageUrl = getClass().getResource(imageStr);
            return Image.getInstance(imageUrl);
        } catch (Exception e) {
            log.error("Could not find the rune image " + imageStr);
        }
        return null;
    }

    /**
	 * Collects and sorts all favorite skills.
	 * @param favSkills
	 * @param favSpelllists
	 * @param favsWepaons
	 */
    protected void prepareFavorites(List<List<Object>> favSkills, List<List<Object>> favSpelllists, List<List<Object>> favsWepaons) {
        for (Rank rank : sheet.getSkillRanks()) {
            if (Boolean.TRUE.equals(rank.getFavorite())) {
                ArrayList<Object> line = new ArrayList<Object>();
                ISkill skill = sheet.getSkill(rank.getId());
                if (skill != null) {
                    line.add(skill);
                    line.add("" + rank.getRank());
                    line.add(format(sheet.getSkillTotalBonus(skill), false));
                    if (sheet.getSkillcategory(skill).getRankType().isWeapon()) {
                        String[] split = StringUtils.split(skill.getDescription(), ',');
                        if (split != null) {
                            if (split.length > 0) {
                                line.add(StringUtils.trimToEmpty(split[0]));
                            }
                            if (split.length > 1) {
                                line.add(StringUtils.trimToEmpty(split[1]));
                            }
                        }
                        while (line.size() < 5) {
                            line.add("");
                        }
                        favsWepaons.add(line);
                    } else if (skill.isSpelllist()) {
                        favSpelllists.add(line);
                    } else {
                        favSkills.add(line);
                    }
                    List<String> items = new ArrayList<String>();
                    List<MagicalItem> magicalitems = sheet.getMagicalitems();
                    for (int i = 0; i < magicalitems.size(); i++) {
                        MagicalItem item = magicalitems.get(i);
                        if (Boolean.TRUE.equals(item.getFavorite()) && item.hasSkillModifier(skill)) {
                            items.add(item.asOneLine(i + 1));
                        }
                    }
                    line.add(items);
                } else {
                    if (log.isWarnEnabled()) log.warn("Could not load skill with ID " + rank.getId());
                }
            }
        }
        Comparator<List<Object>> comparator = new Comparator<List<Object>>() {

            @Override
            public int compare(List<Object> l1, List<Object> l2) {
                ISkill sk1 = (ISkill) l1.get(0);
                ISkill sk2 = (ISkill) l2.get(0);
                if (!sk1.isSpelllist() && sk2.isSpelllist()) {
                    return -1;
                }
                if (sk1.isSpelllist() && !sk2.isSpelllist()) {
                    return 1;
                }
                return sk1.getName().compareTo(sk2.getName());
            }
        };
        Collections.sort(favSkills, comparator);
        Collections.sort(favSpelllists, comparator);
        Collections.sort(favsWepaons, comparator);
    }

    protected float page1Favorites(PdfContentByte canvas, float y, final float maxBottomY, boolean withEquipment) {
        List<List<Object>> favSkills = new ArrayList<List<Object>>();
        List<List<Object>> favSpelllists = new ArrayList<List<Object>>();
        List<List<Object>> favsWepaons = new ArrayList<List<Object>>();
        prepareFavorites(favSkills, favSpelllists, favsWepaons);
        float centerX = PAGE1_RIGHTBOX_LEFTX + (RIGHT_X - PAGE1_RIGHTBOX_LEFTX) / 2;
        float[] xVal = new float[] { PAGE1_RIGHTBOX_LEFTX + 4, centerX - 42, centerX - 17, centerX + 6, RIGHT_X - 42, RIGHT_X - 15 };
        float lineHeight = 10.5f;
        canvas.beginText();
        canvas.setFontAndSize(fontHeadline, 8);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.skill.favs"), centerX, y, 0);
        y -= lineHeight;
        canvas.setFontAndSize(fontBold, 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("common.skill"), xVal[0], y, 0);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("common.skill"), xVal[3], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.ranks"), xVal[1], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.ranks"), xVal[4], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.bonus"), xVal[2], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.bonus"), xVal[5], y, 0);
        canvas.endText();
        y -= lineHeight;
        float yL = y;
        float yR = y;
        for (int k = 0; k < 2; k++) {
            List<List<Object>> favs = (k == 0) ? favSkills : favSpelllists;
            int firstRightIndex = getIndexOfHalfSpace(favs, lineHeight, centerX - PAGE1_RIGHTBOX_LEFTX, canvas);
            if (k == 1 && favs.size() > 0) {
                canvas.beginText();
                canvas.setFontAndSize(fontHeadline, 8);
                canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.spell.favs"), centerX, y, 0);
                canvas.endText();
                y -= lineHeight;
                yL = y;
                yR = y;
            }
            for (int idx = 0; y > maxBottomY && idx < favs.size(); idx++) {
                int c = 0;
                float y0 = yL;
                if (idx >= firstRightIndex) {
                    c = 3;
                    y0 = yR;
                }
                List<Object> line = favs.get(idx);
                for (int col = 0; col < 3; col++) {
                    String text = (col == 0) ? ((ISkill) line.get(col)).getName() : (String) line.get(col);
                    int align = (col == 0) ? Element.ALIGN_LEFT : Element.ALIGN_CENTER;
                    showUserText(canvas, 7, xVal[col + c], y0, text, align);
                    if (col == 0) {
                        labeledUserText(canvas, "", "", xVal[col + c] - 3, y0, xVal[col + c + 1] - 20, fontRegular, 7);
                    } else {
                        canvas.beginText();
                        canvas.setFontAndSize(fontRegular, 7);
                        canvas.showTextAligned(align, "_____", xVal[col + c], y0, 0);
                        canvas.endText();
                    }
                }
                @SuppressWarnings("unchecked") List<String> list = (List<String>) line.get(3);
                for (String itemLine : list) {
                    y0 = showMagicalItems(canvas, itemLine, y0, xVal[c], xVal[c + 2], false);
                }
                y0 -= lineHeight;
                if (idx >= firstRightIndex) {
                    yR = y0;
                } else {
                    yL = y0;
                }
                y = Math.min(yL, yR);
            }
        }
        if (favsWepaons.size() > 0 && y > maxBottomY) {
            hline(canvas, PAGE1_RIGHTBOX_LEFTX, y, RIGHT_X);
            y -= lineHeight;
            xVal = new float[] { 247, 360, 388, 414, 490 };
            canvas.beginText();
            canvas.setFontAndSize(fontHeadline, 8);
            canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.attack.favs"), centerX, y, 0);
            canvas.endText();
            y -= lineHeight;
            canvas.beginText();
            canvas.setFontAndSize(fontBold, 8);
            canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page1.attack.descr"), xVal[0], y, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.ranks"), xVal[1], y, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.bonus"), xVal[2], y, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.fumble"), xVal[3], y, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.modifications"), xVal[4], y, 0);
            canvas.endText();
            y -= lineHeight;
            for (int idx = 0; idx < favsWepaons.size() && y > maxBottomY; idx++) {
                List<Object> weaponSkillArr = favsWepaons.get(idx);
                for (int col = 0; col < xVal.length; col++) {
                    String str = "";
                    if (col == 0) {
                        str = ((ISkill) weaponSkillArr.get(col)).getName();
                    } else if (col < weaponSkillArr.size()) {
                        str = (String) weaponSkillArr.get(col);
                    }
                    int align = (col == 0) ? Element.ALIGN_LEFT : Element.ALIGN_CENTER;
                    showUserText(canvas, (col < 4 ? 7 : 6), xVal[col], y, str, align);
                    if (col == 0) {
                        labeledUserText(canvas, "", "", xVal[col] - 3, y, xVal[col + 1] - 15, fontRegular, 7);
                    } else {
                        canvas.beginText();
                        canvas.setFontAndSize(fontRegular, 7);
                        if (col + 1 == xVal.length) {
                            canvas.showTextAligned(Element.ALIGN_RIGHT, "__________________________________", RIGHT_X - 4, y, 0);
                        } else {
                            canvas.showTextAligned(align, "______", xVal[col], y, 0);
                        }
                        canvas.endText();
                    }
                }
                @SuppressWarnings("unchecked") List<String> items = (List<String>) weaponSkillArr.get(5);
                for (String item : items) {
                    y = showMagicalItems(canvas, item, y, xVal[0], RIGHT_X - 4, false);
                }
                y -= lineHeight;
            }
        }
        if (withEquipment) {
            List<Equipment> equipFavs = new ArrayList<Equipment>();
            for (Equipment eq : sheet.getEquipments()) {
                if (eq.isFavorite()) {
                    equipFavs.add(eq);
                }
            }
            if (equipFavs.size() > 0 && y > maxBottomY) {
                hline(canvas, PAGE1_RIGHTBOX_LEFTX, y, RIGHT_X);
                y -= lineHeight;
                canvas.beginText();
                canvas.setFontAndSize(fontHeadline, 8);
                canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.equipment.favs"), centerX, y, 0);
                canvas.endText();
                y -= lineHeight;
                canvas.beginText();
                canvas.setFontAndSize(fontBold, 8);
                canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("common.equipment.itemdesc"), 247, y, 0);
                canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.equipment.location"), 485, y, 0);
                canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("common.equipment.weight"), 530, y, 0);
                canvas.endText();
                y -= lineHeight;
                for (int i = 0; i < equipFavs.size() && y > maxBottomY; i++) {
                    Equipment eq = equipFavs.get(i);
                    labeledUserText(canvas, "", eq.getDescription(), 244, y, 460, fontRegular, 7);
                    showUserText(canvas, 7, 485, y, eq.getPlace(), Element.ALIGN_CENTER);
                    showUserText(canvas, 7, 530, y, sheet.getWeightUnit().getFormattedString(eq.getWeight()), Element.ALIGN_CENTER);
                    canvas.beginText();
                    canvas.setFontAndSize(fontRegular, 7);
                    canvas.showTextAligned(Element.ALIGN_CENTER, "___________", 485, y, 0);
                    canvas.showTextAligned(Element.ALIGN_CENTER, "__________", 530, y, 0);
                    canvas.endText();
                    y -= lineHeight;
                }
            }
            if (y > (maxBottomY + lineHeight)) {
                hline(canvas, PAGE1_RIGHTBOX_LEFTX, y, RIGHT_X);
                y -= lineHeight;
            }
        }
        return y;
    }

    protected float page6ExhaustionPointsAndMovement(PdfContentByte canvas, final float lineHeight, final float initialY, boolean withBottomLine) {
        canvas.beginText();
        canvas.setFontAndSize(fontBold, 8);
        float y = initialY - lineHeight;
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page6.exhaustionpoints"), LEFT_X + 5, y, 0);
        int exh = sheet.getExhaustionPoints();
        int[] points = new int[6];
        String[] modifier = { "", "-5", "-15", "-30", "-60", "-100" };
        points[0] = (int) Math.round(exh * 0.25);
        points[1] = (int) Math.round(exh * 0.5) - points[0];
        points[2] = (int) Math.round(exh * 0.75) - points[1] - points[0];
        points[3] = (int) Math.round(exh * 0.9) - points[2] - points[1] - points[0];
        points[4] = (int) Math.round(exh * 0.99) - points[3] - points[2] - points[1] - points[0];
        points[5] = exh - points[4] - points[3] - points[2] - points[1] - points[0];
        for (int i = 0; i < points.length; i++) {
            y -= lineHeight;
            canvas.setFontAndSize(fontRegular, 8);
            canvas.showTextAligned(Element.ALIGN_LEFT, modifier[i], LEFT_X + 5, y, 0);
            String str = getWidgetBoxes(points[i]);
            canvas.setFontAndSize(fontWidget, 8);
            canvas.showTextAligned(Element.ALIGN_LEFT, str, LEFT_X + 25, y, 0);
        }
        canvas.setFontAndSize(fontBold, 8);
        y = initialY - lineHeight;
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page6.movement"), RIGHT_X - 100, y, 0);
        y -= lineHeight;
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page6.movement.title.pace"), RIGHT_X - 195, y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page6.movement.title.moverate"), RIGHT_X - 105, y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page6.movement.title.exhpts"), RIGHT_X - 65, y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page6.movement.title.maneuver"), RIGHT_X - 20, y, 0);
        canvas.setFontAndSize(fontRegular, 8);
        int baseRateCM = sheet.getBaseMovement();
        String[] data = { "walk", "fastwalk", "run", "sprint", "fastsprint", "dash" };
        float[] modifierMovement = { 1, 1.5f, 2, 3, 4, 5 };
        for (int i = 0; i < data.length; i++) {
            y -= lineHeight;
            canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page6.movement." + data[i]), RIGHT_X - 195, y, 0);
            int mvAsCustUnit = LengthUnit.CM.convertTo(Math.round(modifierMovement[i] * baseRateCM), sheet.getLengthUnit());
            canvas.showTextAligned(Element.ALIGN_CENTER, sheet.getLengthUnit().getFormattedString(mvAsCustUnit), RIGHT_X - 105, y, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page6.movement." + data[i] + ".exh"), RIGHT_X - 65, y, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page6.movement." + data[i] + ".maneuver"), RIGHT_X - 20, y, 0);
        }
        canvas.endText();
        y -= 10;
        if (withBottomLine) {
            hline(canvas, LEFT_X, y + 4, RIGHT_X);
        }
        vline(canvas, RIGHT_X - 200, initialY + 2, y + 4);
        return y;
    }

    protected float page6PowerPoints(PdfContentByte canvas, final float lineHeight, float y) {
        canvas.beginText();
        canvas.setFontAndSize(fontBold, 8);
        y -= lineHeight;
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page6.powerpoints"), LEFT_X + 5, y, 0);
        int pp = sheet.getPowerPoints();
        int[] points = new int[4];
        String[] modifier = { "", "-10", "-20", "-30" };
        points[0] = (int) Math.round(pp * 0.25);
        points[1] = (int) Math.round(pp * 0.5) - points[0];
        points[2] = (int) Math.round(pp * 0.75) - points[1] - points[0];
        points[3] = pp - points[2] - points[1] - points[0];
        for (int i = 0; i < points.length; i++) {
            y -= lineHeight;
            canvas.setFontAndSize(fontRegular, 8);
            canvas.showTextAligned(Element.ALIGN_LEFT, modifier[i], LEFT_X + 5, y, 0);
            String str = getWidgetBoxes(points[i]);
            canvas.setFontAndSize(fontWidget, 8);
            canvas.showTextAligned(Element.ALIGN_LEFT, str, LEFT_X + 25, y, 0);
        }
        canvas.endText();
        hline(canvas, LEFT_X, y - 6, RIGHT_X);
        y -= 8;
        return y;
    }

    protected float page6ConcussionHits(PdfContentByte canvas, final float lineHeight, float y) {
        canvas.beginText();
        canvas.setFontAndSize(fontBold, 8);
        y -= lineHeight;
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page6.concussionhits"), LEFT_X + 5, y, 0);
        int hits = sheet.getHitPoints();
        int[] points = new int[4];
        String[] modifier = { "", "-10", "-20", "-30" };
        points[0] = (int) Math.round(hits * 0.25);
        points[1] = (int) Math.round(hits * 0.5) - points[0];
        points[2] = (int) Math.round(hits * 0.75) - points[1] - points[0];
        points[3] = hits - points[2] - points[1] - points[0];
        for (int i = 0; i < points.length; i++) {
            y -= lineHeight;
            canvas.setFontAndSize(fontRegular, 8);
            canvas.showTextAligned(Element.ALIGN_LEFT, modifier[i], LEFT_X + 5, y, 0);
            String str = getWidgetBoxes(points[i]);
            canvas.setFontAndSize(fontWidget, 8);
            canvas.showTextAligned(Element.ALIGN_LEFT, str, LEFT_X + 25, y, 0);
        }
        y -= lineHeight;
        canvas.setFontAndSize(fontRegular, 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page6.unconscious") + ":", LEFT_X + 5, y, 0);
        y -= lineHeight;
        int statTemp = sheet.getStatTemp(StatEnum.CONSTITUTION);
        points = new int[2];
        points[0] = (int) Math.ceil(statTemp * 0.5);
        points[1] = statTemp - points[0];
        String str = getWidgetBoxes(points[0]);
        canvas.setFontAndSize(fontWidget, 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, str, LEFT_X + 25, y, 0);
        y -= lineHeight;
        str = getWidgetBoxes(points[1]);
        canvas.setFontAndSize(fontWidget, 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, str, LEFT_X + 25, y, 0);
        float centerX = LEFT_X + ((RIGHT_X - LEFT_X) / 2);
        y -= lineHeight;
        canvas.setFontAndSize(fontRegular, 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page6.stunrounds") + ":", LEFT_X + 5, y, 0);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page6.bleedingperround") + ":", centerX + 5, y, 0);
        y -= lineHeight;
        str = getWidgetBoxes(20);
        canvas.setFontAndSize(fontWidget, 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, str, LEFT_X + 25, y, 0);
        canvas.showTextAligned(Element.ALIGN_LEFT, str, centerX + 25, y, 0);
        canvas.endText();
        hline(canvas, LEFT_X, y - 6, RIGHT_X);
        y -= 8;
        return y;
    }

    private String getWidgetBoxes(int numbs) {
        StringBuilder sb = new StringBuilder();
        for (int j = 1; j <= numbs; j++) {
            sb.append("o");
            if (j % 5 == 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
	 * 
	 * @param canvas the pdf canvas
	 * @param initialY upper y
	 * @return the new y
	 * @throws IOException 
	 */
    protected float page1RaceProfArmorDB(PdfContentByte canvas, final float initialY) throws IOException {
        float y = initialY;
        float x = LEFT_X + 5;
        String raceCulture = sheet.getRace().getName();
        if (StringUtils.trimToNull(sheet.getCulture().getName()) != null) {
            raceCulture += " (" + sheet.getCulture().getName() + ")";
        }
        labeledUserText(canvas, RESOURCE.getString("ui.basic.race") + ":", raceCulture, x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("ui.basic.profession") + ":", sheet.getProfession().getName(), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        if (!StringUtils.isBlank(sheet.getApprenticeShip())) {
            labeledUserText(canvas, RESOURCE.getString("rolemaster.trainingpacks") + ":", "", x, y, 0, fontRegular, 8);
            y -= PAGE1_LEFTBOX_LINE_HEIGHT;
            labeledUserText(canvas, "", sheet.getApprenticeShip(), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
            y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        }
        StringBuilder magicRealm = new StringBuilder();
        for (StatEnum stat : sheet.getMagicRealm()) {
            if (magicRealm.length() > 0) {
                magicRealm.append(" / ");
            }
            magicRealm.append(RESOURCE.getString("MagicRealm." + stat.name()));
        }
        labeledUserText(canvas, RESOURCE.getString("rolemaster.realm") + ":", magicRealm.toString(), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        hline(canvas, LEFT_X, y + (PAGE1_LEFTBOX_LINE_HEIGHT / 4), PAGE1_LEFTBOX_RIGHTX);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.armor") + ":", "" + sheet.getArmor(), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        canvas.beginText();
        canvas.setFontAndSize(fontUser, 6);
        float x0 = x + 5 + fontRegular.getWidthPoint(RESOURCE.getString("rolemaster.armor") + ": xyz", 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("armor." + sheet.getArmor()), x0, y, 0);
        canvas.endText();
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.penalty.weight") + ":", "" + sheet.getWeightPenalty(), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        int mv = LengthUnit.CM.convertTo(sheet.getBaseMovement(), sheet.getLengthUnit());
        String bmv = sheet.getLengthUnit().getFormattedString(mv);
        labeledUserText(canvas, RESOURCE.getString("rolemaster.basemovementrate") + ":", bmv + RESOURCE.getString("rolemaster.basemovementrate.perrnd"), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.penalty.moveingmaneuver") + ":", format(sheet.getArmorManeuverModi(), false), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.penalty.missile") + ":", format(sheet.getArmorRangeModi(), false), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        hline(canvas, LEFT_X, y + (PAGE1_LEFTBOX_LINE_HEIGHT / 4), PAGE1_LEFTBOX_RIGHTX);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.quickness.bonus") + ":", format(sheet.getReactionBonus(), false), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.penalty.quickness.armor") + ":", "" + sheet.getArmorReactionModi(), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        Shield shield = sheet.getShield();
        StringBuilder sb = new StringBuilder();
        if (shield != null && (shield.getCloseBonus() > 0 || shield.getRangeBonus() > 0)) {
            sb.append(shield.getName());
            sb.append(" (+" + shield.getCloseBonus());
            if (shield.getCloseBonus() != shield.getRangeBonus()) {
                sb.append("/+" + shield.getRangeBonus());
            }
            sb.append(")");
        }
        labeledUserText(canvas, RESOURCE.getString("rolemaster.db.shield") + ":", sb.toString(), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.db.magic") + ":", "", x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.db.special") + ":", "", x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        labeledUserText(canvas, RESOURCE.getString("rolemaster.db.total") + ":", format(sheet.getDefensiveBonus(), false), x, y, PAGE1_LEFTBOX_RIGHTX, fontRegular, 8);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        hline(canvas, LEFT_X, y + (PAGE1_LEFTBOX_LINE_HEIGHT / 4), PAGE1_LEFTBOX_RIGHTX);
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        return y;
    }

    /**
	 * 
	 * @param canvas the pdf canvas 
	 * @param initialY the upper y
	 * @param withBottomLine whether to draw a line at bottom
	 * @throws IOException 
	 * @return the new y
	 */
    protected float page1Resistance(PdfContentByte canvas, final float initialY, boolean withBottomLine) throws IOException {
        String line = "_____";
        float lineWidth = fontRegular.getWidthPoint(line, 8);
        float[] xVal = new float[] { LEFT_X + 4, 130, 157, PAGE1_LEFTBOX_RIGHTX - 6 - (lineWidth / 2) };
        float y = initialY;
        float center = LEFT_X + ((PAGE1_LEFTBOX_RIGHTX - LEFT_X) / 2);
        canvas.beginText();
        canvas.setFontAndSize(fontHeadline, 8);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.resistance.headline"), center, y, 0);
        canvas.endText();
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        canvas.beginText();
        canvas.setFontAndSize(fontBold, 8);
        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("pdf.page1.resistance.type"), xVal[0], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.resistance.race"), xVal[1], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.resistance.stat"), xVal[2], y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, RESOURCE.getString("pdf.page1.resistance.total"), xVal[3], y, 0);
        canvas.endText();
        y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        for (ResistanceEnum res : ResistanceEnum.values()) {
            for (int col = 0; col < xVal.length; col++) {
                String str = null;
                switch(col) {
                    case 0:
                        canvas.beginText();
                        canvas.setFontAndSize(fontRegular, 8);
                        canvas.showTextAligned(Element.ALIGN_LEFT, RESOURCE.getString("ResistanceEnum." + res.name()), xVal[0], y, 0);
                        canvas.showTextAligned(Element.ALIGN_CENTER, line, xVal[1], y, 0);
                        canvas.showTextAligned(Element.ALIGN_CENTER, line, xVal[2], y, 0);
                        String stat = "(3x " + RESOURCE.getString("StatEnum." + res.getStat().name() + ".short") + ")";
                        canvas.showTextAligned(Element.ALIGN_LEFT, stat, xVal[2] + 10, y, 0);
                        canvas.showTextAligned(Element.ALIGN_RIGHT, line, PAGE1_LEFTBOX_RIGHTX - 6, y, 0);
                        canvas.endText();
                        break;
                    case 1:
                        str = format(sheet.getRace().getResistenceBonus(res), false);
                        break;
                    case 2:
                        str = format(sheet.getResistenceStatBonus(res), false);
                        break;
                    case 3:
                        str = format(sheet.getResistenceBonusTotal(res), false);
                        break;
                    default:
                        str = "";
                }
                if (col > 0) {
                    showUserText(canvas, 8, xVal[col], y, str, Element.ALIGN_CENTER);
                }
            }
            List<MagicalItem> items = sheet.getMagicalitems();
            for (int i = 0; i < items.size(); i++) {
                MagicalItem item = items.get(i);
                if (Boolean.TRUE.equals(item.getFavorite()) && item.hasResistanceModifier(res)) {
                    y = showMagicalItems(canvas, item.asOneLine(i + 1), y, LEFT_X + 6, PAGE1_LEFTBOX_RIGHTX - 6, false);
                }
            }
            y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        }
        for (String lineStr : sheet.getRace().getAdditionalResistenceLines()) {
            showUserText(canvas, 8, 60.0f, y, lineStr);
            y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        }
        if (withBottomLine) {
            hline(canvas, LEFT_X, y + (PAGE1_LEFTBOX_LINE_HEIGHT / 4), PAGE1_LEFTBOX_RIGHTX);
            y -= PAGE1_LEFTBOX_LINE_HEIGHT;
        }
        return y;
    }

    protected float showMagicalItems(PdfContentByte canvas, String text, float y, float leftX, float urx, boolean simulate) {
        ColumnText ct = new ColumnText(canvas);
        Phrase phrase = new Phrase(text, new Font(fontUser, 6));
        ct.setSimpleColumn(phrase, leftX, y, urx, BOTTOM_Y + 5, 7, Element.ALIGN_LEFT);
        try {
            ct.go(simulate);
            if (log.isDebugEnabled()) log.debug("description item lines written: " + ct.getLinesWritten());
            y -= (7 * ct.getLinesWritten() + 1);
        } catch (DocumentException e) {
            log.error(e.getMessage(), e);
        }
        return y;
    }

    /**
	 * Returns the index of the first element of the right column 
	 * 
	 */
    private int getIndexOfHalfSpace(List<List<Object>> skillList, float lineHeightSkill, float width, PdfContentByte canvas) {
        float leftColSpace = 0;
        float rightColSpace = 0;
        int idxR = skillList.size();
        int idxL = -1;
        if (skillList.size() > 0) {
            while (idxL < skillList.size() && (idxL + 1) < idxR) {
                leftColSpace += lineHeightSkill;
                @SuppressWarnings("unchecked") List<String> items = (List<String>) skillList.get(++idxL).get(3);
                for (String line : items) {
                    float yDiff = showMagicalItems(canvas, line, 0, 0, width, true);
                    leftColSpace += Math.abs(yDiff);
                }
                while ((idxL + 1) < idxR && leftColSpace > rightColSpace) {
                    rightColSpace += lineHeightSkill;
                    @SuppressWarnings("unchecked") List<String> itemsR = (List<String>) skillList.get(--idxR).get(3);
                    for (String line : itemsR) {
                        float yDiff = showMagicalItems(canvas, line, 0, 0, width, true);
                        rightColSpace += Math.abs(yDiff);
                    }
                }
            }
        }
        return idxR;
    }

    /**
	 * 
	 * @return the new document instance
	 */
    protected Document createDocument() {
        Document document = new Document(PageSize.A4);
        document.addCreationDate();
        document.addCreator(RMOffice.getProgramString());
        if (sheet.getCharacterName() != null) {
            document.addTitle(sheet.getCharacterName());
        }
        return document;
    }
}
