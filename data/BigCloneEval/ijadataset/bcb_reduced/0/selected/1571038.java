package com.d_project.xprint.core;

import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XText
 * @author Kazuhiko Arase
 */
public class XText extends XNode {

    public XText() {
        super(NodeNames.TEXT_NODE);
    }

    public Dimension2D getContentSize(XNodeContext context) {
        double rectWidth = getBounds().getWidth();
        double width = 0;
        double height = 0;
        TextLayoutItem[] layoutItem = createTextLayoutItems();
        double lineGap = getLineGap();
        for (int i = 0; i < layoutItem.length; i++) {
            if (!layoutItem[i].empty) {
                width = Math.max(width, layoutItem[i].layout.getAdvance());
            }
            if (i > 0) {
                height += lineGap;
            }
            height += layoutItem[i].layout.getAscent();
            height += layoutItem[i].layout.getDescent();
        }
        if (rectWidth > 0) {
            width = Math.max(0, rectWidth - getDoubleAttribute(AttributeKeys.PADDING_LEFT) - getDoubleAttribute(AttributeKeys.PADDING_RIGHT));
        }
        return new Size2D(width, height);
    }

    private double getLineGap() {
        if (getAttribute(AttributeKeys.LINE_GAP) != null) {
            return getDoubleAttribute(AttributeKeys.LINE_GAP);
        } else {
            return getDoubleAttribute(AttributeKeys.FONT_SIZE, true) / 4;
        }
    }

    public void paint(XNodeContext context, IXGraphics g) {
        Rectangle2D rect = getBounds();
        TextLayoutItem[] layoutItem = createTextLayoutItems();
        double lineGap = getLineGap();
        double th = 0;
        for (int i = 0; i < layoutItem.length; i++) {
            if (i > 0) {
                th += lineGap;
            }
            th += layoutItem[i].layout.getAscent();
            th += layoutItem[i].layout.getDescent();
        }
        double y = getContentY(th);
        for (int i = 0; i < layoutItem.length; i++) {
            if (i > 0) {
                y += lineGap;
            }
            y += layoutItem[i].layout.getAscent();
            if (!layoutItem[i].empty) {
                if (AttributeValues.JUSTIFIED.equals(getAttribute(AttributeKeys.CONTENT_ALIGN))) {
                    double x = getDoubleAttribute(AttributeKeys.PADDING_LEFT);
                    double width = Math.max(0, rect.getWidth() - getDoubleAttribute(AttributeKeys.PADDING_LEFT) - getDoubleAttribute(AttributeKeys.PADDING_RIGHT));
                    g.drawText(layoutItem[i].layout.getJustifiedLayout((float) width), x, y);
                } else {
                    double x = getContentX(layoutItem[i].layout.getAdvance());
                    g.drawText(layoutItem[i].layout, x, y);
                }
            }
            y += layoutItem[i].layout.getDescent();
        }
    }

    private String[] getLines() {
        String text = getText();
        List list = new ArrayList();
        Pattern pat = Pattern.compile("\\r?\\n");
        Matcher mat = pat.matcher(text);
        int start = 0;
        while (mat.find(start)) {
            list.add(text.substring(start, mat.start()));
            start = mat.end();
        }
        list.add(text.substring(start));
        if (list.size() == 0) {
            return new String[] { "" };
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    private TextLayoutItem[] createTextLayoutItems() {
        double rectWidth = getBounds().getWidth();
        FontRenderContext frc = new FontRenderContext(null, true, true);
        List layoutList = new ArrayList();
        String[] lines = getLines();
        for (int i = 0; i < lines.length; i++) {
            String text = lines[i];
            if (text.length() > 0) {
                if (rectWidth > 0) {
                    double width = Math.max(0, rectWidth - getDoubleAttribute(AttributeKeys.PADDING_LEFT) - getDoubleAttribute(AttributeKeys.PADDING_RIGHT));
                    AttributedString string = new AttributedString(text);
                    string.addAttribute(TextAttribute.FONT, getFont());
                    LineBreakMeasurer lbm = new LineBreakMeasurer(string.getIterator(), frc);
                    while (lbm.getPosition() < text.length()) {
                        TextLayout layout = lbm.nextLayout((float) width);
                        layoutList.add(new TextLayoutItem(layout, false));
                    }
                } else {
                    layoutList.add(new TextLayoutItem(new TextLayout(text, getFont(), frc), false));
                }
            } else {
                layoutList.add(new TextLayoutItem(new TextLayout(" ", getFont(), frc), true));
            }
        }
        return (TextLayoutItem[]) layoutList.toArray(new TextLayoutItem[layoutList.size()]);
    }

    private static class TextLayoutItem {

        public final TextLayout layout;

        public final boolean empty;

        public TextLayoutItem(TextLayout layout, boolean empty) {
            this.layout = layout;
            this.empty = empty;
        }
    }
}
