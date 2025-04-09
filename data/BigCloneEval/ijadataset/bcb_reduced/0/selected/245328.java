package org.jpedal.objects;

import org.jpedal.utils.LogWriter;

/**
 * holds the current text state
 */
public class TextState implements Cloneable {

    /** current text mode is horizontal or vertical @deprecated*/
    public boolean isHorizontalWritingMode = true;

    /**orientation of text using contstants from PdfData*/
    public int writingMode = 0;

    /**last Tm value*/
    private float[][] TmAtStart = new float[3][3];

    /**matrix operations for calculating start of text*/
    public float[][] Tm = new float[3][3];

    private String font_ID = "";

    /**name of font being used*/
    private String font_family_name = "";

    /**leading setin text*/
    private float TL = 0;

    /**gap between chars set by Tc command*/
    private float character_spacing = 0;

    /**current Tfs value*/
    private float Tfs = 0;

    /** text rise set in stream*/
    private float text_rise = 0;

    /**text height - see also Tfs*/
    private float th = 1;

    /**matrix with trm*/
    private float[][] Trm;

    /**gap inserted with spaces - set by Tw*/
    private float word_spacing;

    /**font size as whole number*/
    private int current_font_size = 0;

    /**
	 * set Trm values
	 */
    public TextState() {
        Tm[0][0] = 1;
        Tm[0][1] = 0;
        Tm[0][2] = 0;
        Tm[1][0] = 0;
        Tm[1][1] = 1;
        Tm[1][2] = 0;
        Tm[2][0] = 0;
        Tm[2][1] = 0;
        Tm[2][2] = 1;
    }

    /**
	 * get Tm at start of line
	 */
    public float[][] getTMAtLineStart() {
        return TmAtStart;
    }

    /**
	 * set Tm at start of line
	 */
    public void setTMAtLineStart() {
        TmAtStart[0][0] = Tm[0][0];
        TmAtStart[0][1] = Tm[0][1];
        TmAtStart[0][2] = Tm[0][2];
        TmAtStart[1][0] = Tm[1][0];
        TmAtStart[1][1] = Tm[1][1];
        TmAtStart[1][2] = Tm[1][2];
        TmAtStart[2][0] = Tm[2][0];
        TmAtStart[2][1] = Tm[2][1];
        TmAtStart[2][2] = Tm[2][2];
    }

    /**
	 * set Horizontal Scaling
	 */
    public final void setHorizontalScaling(float th) {
        this.th = th;
    }

    /**
	 * get font id
	 */
    public final String getFontID() {
        return font_ID;
    }

    /**
	 * get Text rise
	 */
    public final float getTextRise() {
        return text_rise;
    }

    /**
	 * get character spacing
	 */
    public final float getCharacterSpacing() {
        return character_spacing;
    }

    /**
	 * get word spacing
	 */
    public final float getWordSpacing() {
        return word_spacing;
    }

    /**
	 * set font tfs
	 */
    public final void setLeading(float TL) {
        this.TL = TL;
    }

    /**
	 * get font tfs
	 */
    public final float getTfs() {
        return Tfs;
    }

    /**
	 * get Horizontal Scaling
	 */
    public final float getHorizontalScaling() {
        return th;
    }

    /**
	 * get font name
	 */
    public final String getFontName() {
        return font_family_name;
    }

    /**
	 * set Text rise
	 */
    public final void setTextRise(float text_rise) {
        this.text_rise = text_rise;
    }

    /**
	 * get current font size
	 */
    public final int getCurrentFontSize() {
        int value = current_font_size;
        if (value == 0) value = (int) this.Tfs;
        return value;
    }

    /**
	 * set current font size
	 */
    public final void setCurrentFontSize(int value) {
        this.current_font_size = value;
    }

    /**
	 * get Trm for plotting text
	 */
    public final float[][] getTrm() {
        return Trm;
    }

    /**
	 * get font tfs
	 */
    public final float getLeading() {
        return TL;
    }

    /**
	 * get Trm for plotting text
	 */
    public final void setTrm(float[][] Trm) {
        this.Trm = Trm;
    }

    /**
	 * clone object
	 */
    public final Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch (Exception e) {
            LogWriter.writeLog("Unable to clone " + e);
        }
        return o;
    }

    /**
	 * set word spacing
	 */
    public final void setWordSpacing(float word_spacing) {
        this.word_spacing = word_spacing;
    }

    /**
	 * set font name
	 */
    public final void setFont(String font_family_name, String font_ID) {
        this.font_family_name = font_family_name;
        this.font_ID = font_ID;
    }

    /**
	 * set character spacing
	 */
    public final void setCharacterSpacing(float character_spacing) {
        this.character_spacing = character_spacing;
    }

    /**
	 * set font tfs to default
	 */
    public final void resetTm() {
        Tm[0][0] = 1;
        Tm[0][1] = 0;
        Tm[0][2] = 0;
        Tm[1][0] = 0;
        Tm[1][1] = 1;
        Tm[1][2] = 0;
        Tm[2][0] = 0;
        Tm[2][1] = 0;
        Tm[2][2] = 1;
    }

    /**
	 * set font tfs
	 */
    public final void setFontTfs(float Tfs) {
        this.Tfs = Tfs;
    }
}
