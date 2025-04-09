package jalview.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import jalview.annotation.*;
import jalview.seq.SequenceI;
import jalview.gui.schemes.*;

public class DrawableFeatureSet extends DrawableSeqFeature implements Drawable, FeatureSetI {

    Color colour;

    boolean joinFeatures = true;

    FeatureSetI fset;

    boolean selected = false;

    boolean visible = true;

    boolean fullDraw = true;

    FeatureTypes ftypes = new FeatureTypes();

    public DrawableFeatureSet() {
        this(Color.gray);
        initTypes();
    }

    public DrawableFeatureSet(Color colour) {
        this.fset = new FeatureSet();
        this.colour = colour;
        initTypes();
    }

    public DrawableFeatureSet(FeatureSetI fset, Color colour) {
        this.fset = fset;
        this.colour = colour;
        initTypes();
    }

    public DrawableFeatureSet(FeatureSetI fset) {
        this.fset = fset;
        this.colour = Color.gray;
        initTypes();
    }

    public void initTypes() {
        for (int i = 0; i < size(); i++) {
            SeqFeatureI sf = getFeatureAt(i);
            String type = sf.getType();
            if (!(sf instanceof Drawable)) {
                DrawableSeqFeature dsf = new DrawableSeqFeature(sf);
                setFeatureAt(i, dsf);
            } else {
                setFeatureAt(i, sf);
            }
            if (ftypes.getType(type) == null) {
                FeatureProperty fp = new FeatureProperty(type, FeatureProperty.SINGLE);
                ftypes.addType(fp);
            }
        }
    }

    public void draw(Graphics g, int panelx, int panely, long startbase, long endbase, float xscale, int yspace, FeatureProperty fp) {
        for (int i = 0; i < size(); i++) {
            SeqFeatureI sf = getFeatureAt(i);
            if (i < size() - 1) {
                long intstart = getFeatureAt(i).getEnd();
                long intend = getFeatureAt(i + 1).getStart();
                long intmid = (intend + intstart) / 2;
                if (fp.getStyle() == FeatureProperty.GENE) {
                    if (intstart < endbase && intend > startbase) {
                        g.setColor(Color.gray);
                        g.drawLine(panelx + (int) ((intstart - startbase + 1) / xscale), panely - yspace / 2, panelx + (int) ((intend - startbase) / xscale), panely - yspace / 2);
                    }
                }
            }
            if (fullDraw) {
                ((Drawable) getFeatureAt(i)).draw(g, panelx, panely, startbase, endbase, xscale, yspace, fp);
            } else {
                _draw(g, panelx, panely, startbase, endbase, xscale, yspace, fp);
            }
        }
    }

    public void _draw(Graphics g, int panelx, int panely, long startbase, long endbase, float xscale, int yspace, FeatureProperty fp) {
        long tmpxst = startbase;
        long tmpxen = endbase;
        long start = getStart();
        long end = getEnd();
        if (start < endbase && end > startbase) {
            if (start > startbase) {
                tmpxst = start;
            }
            if (end < endbase) {
                tmpxen = end;
            }
            int tmpxstart = panelx + (int) ((tmpxst - startbase) / xscale);
            int tmpxend = panelx + (int) ((tmpxen - startbase + 1) / xscale);
            int xsize = (int) (tmpxend - tmpxstart);
            if (xsize < minsize) {
                xsize = minsize;
            }
            g.setColor(fp.getColour());
            g.fillRect((int) tmpxstart, panely - (yspace - 1), xsize, (yspace - 2));
            g.setColor(Color.black);
            g.drawRect((int) tmpxstart, panely - (yspace - 1), xsize, yspace - 2);
        }
    }

    public void setColour(java.awt.Color newColour) {
        for (int i = 0; i < size(); i++) {
            ((Drawable) getFeatureAt(i)).setColour(newColour);
        }
        this.colour = newColour;
    }

    public Color getColour() {
        return colour;
    }

    public void setSelected(boolean state) {
        for (int i = 0; i < size(); i++) {
            ((Drawable) getFeatureAt(i)).setSelected(state);
        }
        this.selected = state;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setVisible(boolean state) {
        for (int i = 0; i < size(); i++) {
            ((Drawable) getFeatureAt(i)).setVisible(state);
        }
        this.visible = state;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getYindex() {
        return ((Drawable) getFeatureAt(0)).getYindex();
    }

    public void setYindex(int index) {
        for (int i = 0; i < size(); i++) {
            ((Drawable) getFeatureAt(i)).setYindex(index);
        }
    }

    public int size() {
        return fset.size();
    }

    public void addFeature(SeqFeatureI feature) {
        if (feature instanceof Drawable) {
            fset.addFeature(feature);
            feature.setRefFeature(this);
        } else {
            DrawableSeqFeature dsf = new DrawableSeqFeature(feature, getColour());
            fset.addFeature(dsf);
            dsf.setRefFeature(this);
        }
    }

    public void deleteFeature(SeqFeatureI feature) {
        fset.deleteFeature(feature);
    }

    public SeqFeatureI deleteFeatureAt(int i) {
        return fset.deleteFeatureAt(i);
    }

    public SeqFeatureI getFeatureAt(int i) {
        return fset.getFeatureAt(i);
    }

    public void setFeatureAt(int i, SeqFeatureI sf) {
        fset.setFeatureAt(i, sf);
        sf.setRefFeature(this);
    }

    public Vector getFeatures() {
        return fset.getFeatures();
    }

    public Vector getFeatures(long start, long end) {
        return fset.getFeatures(start, end);
    }

    public void group() {
        fset.group();
        for (int i = 0; i < size(); i++) {
            if (!(getFeatureAt(i) instanceof DrawableFeatureSet)) {
                DrawableFeatureSet dsf = new DrawableFeatureSet((FeatureSet) getFeatureAt(i));
                setFeatureAt(i, dsf);
            }
        }
    }

    public void clump() {
        fset.clump();
        for (int i = 0; i < size(); i++) {
            if (!(getFeatureAt(i) instanceof DrawableFeatureSet)) {
                DrawableFeatureSet dsf = new DrawableFeatureSet((FeatureSet) getFeatureAt(i));
                setFeatureAt(i, dsf);
            }
        }
    }

    public void expand(FeatureSetI newfset) {
        fset.expand(newfset);
        for (int i = 0; i < size(); i++) {
            if (!(getFeatureAt(i) instanceof DrawableFeatureSet) && (getFeatureAt(i) instanceof FeatureSetI)) {
                DrawableFeatureSet dsf = new DrawableFeatureSet((FeatureSetI) getFeatureAt(i));
                setFeatureAt(i, dsf);
            }
        }
    }

    public void makeSetFeatures() {
        fset.makeSetFeatures();
        for (int i = 0; i < size(); i++) {
            if (!(getFeatureAt(i) instanceof DrawableFeatureSet)) {
                DrawableFeatureSet dsf = new DrawableFeatureSet((FeatureSet) getFeatureAt(i));
                FeatureProperty fp = ftypes.getType(dsf.getType());
                fp.setStyle(FeatureProperty.GENE);
                setFeatureAt(i, dsf);
            }
        }
    }

    public void setStart(long start) {
    }

    public long getStart() {
        return fset.getStart();
    }

    public void setEnd(long end) {
    }

    public long getEnd() {
        return fset.getEnd();
    }

    public void setStrand(int strand) {
    }

    public int getStrand() {
        return fset.getStrand();
    }

    public void setName(String name) {
        fset.setName(name);
    }

    public String getName() {
        return fset.getName();
    }

    public double getScore() {
        return fset.getScore();
    }

    public void setScore(double score) {
        fset.setScore(score);
    }

    public void setType(String type) {
        fset.setType(type);
    }

    public String getType() {
        return fset.getType();
    }

    public SeqFeatureI getRefFeature() {
        return fset.getRefFeature();
    }

    public void setRefFeature(SeqFeatureI f) {
        fset.setRefFeature(f);
    }

    public SequenceI getSequenceI() {
        return fset.getSequenceI();
    }

    public void setSequenceI(SequenceI seq) {
        fset.setSequenceI(seq);
    }

    public FeatureSetI getProducedFeatures() {
        return fset.getProducedFeatures();
    }

    public void setProducedFeatures(FeatureSetI fset) {
        fset.setProducedFeatures(fset);
    }

    public boolean overlaps(SeqFeatureI se) {
        if (getStart() <= se.getEnd() && getEnd() >= se.getStart()) {
            return true;
        }
        return false;
    }

    public OverlapI findOverlap(SeqFeatureI feature) {
        return null;
    }
}
