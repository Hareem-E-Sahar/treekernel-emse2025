package org.ensembl.draw.tracks;

import org.ensembl.draw.glyph.*;
import org.ensembl.draw.renderer.*;
import org.ensembl.draw.*;
import org.ensembl.draw.data.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;
import java.text.*;

public class Ruler extends Track {

    GlyphSet glyphset;

    String dasType, configName = "Ruler";

    transient JFCRenderer renderer;

    transient SelectionPanel selPanel;

    public Ruler() {
    }

    double bestUnit(int range) {
        double pwr = Math.log(range / 10) / Math.log(10);
        int roundpwr = (int) Math.round(pwr);
        int closestPOT = (int) Math.pow(10, roundpwr);
        if (range < 10) return 1;
        double[] candidates = { 5, 2.5, 2, 1, .5, .25, .2 };
        int bestCandIndex = 0;
        int minDeviation = 1000;
        for (int i = 0; i < candidates.length; i++) {
            double thisCand = candidates[i] * closestPOT;
            int numberOfTicks = (int) (range / thisCand);
            int deviation = (int) Math.abs(10 - numberOfTicks);
            if (numberOfTicks < 11 && deviation < minDeviation) {
                minDeviation = deviation;
                bestCandIndex = i;
            }
        }
        return (candidates[bestCandIndex] * closestPOT);
    }

    int trailingZeros(int unit) {
        int ans = 0;
        while (unit % 10 == 0) {
            ans++;
            unit = unit / 10;
        }
        return ans;
    }

    boolean dataLoaded = false;

    void paintThread() {
        Ruler.this.buffImage = new BufferedImage(width, plannedSize().height, BufferedImage.TYPE_INT_RGB);
        Graphics buffGraf = buffImage.getGraphics();
        buffGraf.setColor(new Color(250, 252, 216));
        buffGraf.fillRect(0, 0, width, plannedSize().height);
        init();
        glyphset.transform(pane.transform);
        dataLoaded = true;
        renderer.render(buffGraf);
    }

    public void setSelectionRect(int x1, int y1, int x2, int y2) {
        selRectX1 = x1;
        selRectY1 = y1;
        selRectX2 = x2;
        selRectY2 = y2;
    }

    int selRectX1 = -1, selRectY1, selRectX2, selRectY2;

    public void paint(final Graphics g) {
        paintThread();
        g.drawImage(buffImage, left, top, null);
        g.setColor(Color.red);
        if (selRectX1 != -1) {
            g.drawRect(selRectX1 < selRectX2 ? selRectX1 : selRectX2, selRectY1 < selRectY2 ? selRectY1 : selRectY2, selRectX1 < selRectX2 ? selRectX2 - selRectX1 : selRectX1 - selRectX2, selRectY1 < selRectY2 ? selRectY2 - selRectY1 : selRectY1 - selRectY2);
        }
    }

    public void init() {
        glyphset = new GlyphSet();
        renderer = new JFCRenderer(glyphset.glyphs());
        glyphset.init_glyphset(null, cf, -13);
        Color featureColor = Color.black;
        String subdivs = "on";
        String abbrev = "on";
        Font labelFont = new Font("SansSerif", Font.PLAIN, 9);
        Panel panel = new Panel();
        RectGlyph rect = new RectGlyph();
        rect.x = 0;
        rect.y = 4;
        rect.height = 0;
        rect.width = (int) (cf.getRange().getEndPosition() - cf.getRange().getStartPosition());
        rect.color = featureColor;
        rect.absolutey = true;
        glyphset.push(rect);
        int bpErrorPerPixel = (int) (0.5 / pane.transform.scalex);
        int start = (int) cf.getRange().getStartPosition();
        int stop = (int) cf.getRange().getEndPosition();
        int range = (int) (cf.getRange().getEndPosition() - cf.getRange().getStartPosition());
        double best = bestUnit(range);
        int startTick = (int) (Math.ceil(start / (double) best) * best);
        int ave = start + range / 2;
        String scalestr;
        int scale;
        int scaledp;
        if (ave > 1000000) {
            scale = 1000000;
            scalestr = "Mb";
            scaledp = 6;
        } else if (ave > 1000) {
            scale = 1000;
            scalestr = "Kb";
            scaledp = 3;
        } else {
            scale = 1;
            scalestr = "bp";
            scaledp = 0;
        }
        DecimalFormat decForm = new DecimalFormat();
        int trailZeros = trailingZeros((int) best);
        decForm.setMinimumFractionDigits(scaledp - trailZeros);
        decForm.setMaximumFractionDigits(scaledp - trailZeros);
        int preStartTick = (int) (Math.floor(start / (double) best) * best);
        if (preStartTick < start && start - preStartTick < bpErrorPerPixel) {
            rect = new RectGlyph();
            rect.x = (int) (start - cf.getRange().getStartPosition());
            rect.y = 4;
            rect.width = 0;
            rect.height = 2;
            rect.color = featureColor;
            rect.absolutey = true;
            glyphset.push(rect);
            if (subdivs.equals("on")) {
                String text = "" + decForm.format((double) start / scale) + " " + scalestr;
                TextGlyph tglyph = new TextGlyph();
                tglyph.x = (int) (start - cf.getRange().getStartPosition());
                tglyph.y = 8;
                tglyph.height = 8;
                tglyph.font = labelFont;
                tglyph.color = featureColor;
                tglyph.text = text;
                tglyph.absolutey = true;
                glyphset.push(tglyph);
            }
        }
        for (int marker = startTick; marker < stop; marker += best) {
            rect = new RectGlyph();
            rect.x = (int) (marker - cf.getRange().getStartPosition());
            rect.y = 4;
            rect.width = 0;
            rect.height = 2;
            rect.color = featureColor;
            rect.absolutey = true;
            glyphset.push(rect);
            if (subdivs.equals("on")) {
                String text = "" + decForm.format((double) marker / scale) + " " + scalestr;
                if (!(marker + best < stop)) {
                    int labelWidth = panel.getFontMetrics(labelFont).stringWidth(text);
                    if (marker + labelWidth > stop) continue;
                }
                TextGlyph tglyph = new TextGlyph();
                tglyph.x = (int) (marker - cf.getRange().getStartPosition());
                tglyph.y = 8;
                tglyph.height = 8;
                tglyph.font = labelFont;
                tglyph.color = featureColor;
                tglyph.text = text;
                tglyph.absolutey = true;
                glyphset.push(tglyph);
            }
        }
    }

    public Dimension plannedSize() {
        return new Dimension(500, 22);
    }

    static transient BufferedImage referenceBuffImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

    public int getLabelLength() {
        FontMetrics fmetr = referenceBuffImage.getGraphics().getFontMetrics(new Font("SansSerif", Font.PLAIN, 11));
        return fmetr.stringWidth(label);
    }
}
