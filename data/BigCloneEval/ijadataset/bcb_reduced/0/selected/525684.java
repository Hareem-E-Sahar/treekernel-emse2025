package com.mxgraph.canvas;

import java.awt.Color;
import java.util.Hashtable;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;

/**
 * An implementation of a canvas that uses HTML for painting.
 */
public class mxHtmlCanvas extends mxBasicCanvas {

    /**
	 * Holds the HTML document that represents the canvas.
	 */
    protected Document document;

    /**
	 * Constructs a new HTML canvas for the specified dimension and scale.
	 */
    public mxHtmlCanvas() {
        this(null);
    }

    /**
	 * Constructs a new HTML canvas for the specified bounds, scale and
	 * background color.
	 */
    public mxHtmlCanvas(Document document) {
        setDocument(document);
    }

    /**
	 * 
	 */
    public void appendHtmlElement(Element node) {
        if (document != null) {
            Node body = document.getDocumentElement().getFirstChild().getNextSibling();
            if (body != null) {
                body.appendChild(node);
            }
        }
    }

    /**
	 * 
	 */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
	 * Returns a reference to the document that represents the canvas.
	 * 
	 * @return Returns the document.
	 */
    public Document getDocument() {
        return document;
    }

    public Object drawVertex(int x, int y, int w, int h, Hashtable style) {
        int start = mxUtils.getInt(style, mxConstants.STYLE_STARTSIZE);
        x += translate.x;
        y += translate.y;
        if (start == 0) {
            drawShape(x, y, w, h, style);
        } else {
            start = (int) Math.round(start * scale);
            Hashtable cloned = new Hashtable(style);
            cloned.remove(mxConstants.STYLE_FILLCOLOR);
            cloned.remove(mxConstants.STYLE_ROUNDED);
            if (mxUtils.isTrue(style, mxConstants.STYLE_HORIZONTAL, true)) {
                drawShape(x, y, w, start, style);
                drawShape(x, y + start, w, h - start, cloned);
            } else {
                drawShape(x, y, start, h, style);
                drawShape(x + start, y, w - start, h, cloned);
            }
        }
        return null;
    }

    public Object drawEdge(List pts, Hashtable style) {
        pts = mxUtils.translatePoints(pts, translate.x, translate.y);
        drawLine(pts, style);
        return null;
    }

    public Object drawLabel(String label, int x, int y, int w, int h, Hashtable style, boolean isHtml) {
        if (drawLabels) {
            x += translate.x;
            y += translate.y;
            return drawText(label, x, y, w, h, style);
        }
        return null;
    }

    /**
	 * Draws the shape specified with the STYLE_SHAPE key in the given style.
	 * 
	 * @param x X-coordinate of the shape.
	 * @param y Y-coordinate of the shape.
	 * @param w Width of the shape.
	 * @param h Height of the shape.
	 * @param style Style of the the shape.
	 */
    public Element drawShape(int x, int y, int w, int h, Hashtable style) {
        String fillColor = mxUtils.getString(style, mxConstants.STYLE_FILLCOLOR);
        String gradientColor = mxUtils.getString(style, mxConstants.STYLE_GRADIENTCOLOR);
        String strokeColor = mxUtils.getString(style, mxConstants.STYLE_STROKECOLOR);
        float strokeWidth = (float) (mxUtils.getFloat(style, mxConstants.STYLE_STROKEWIDTH, 1) * scale);
        String shape = mxUtils.getString(style, mxConstants.STYLE_SHAPE);
        Element elem = document.createElement("div");
        if (shape.equals(mxConstants.SHAPE_LINE)) {
            String direction = mxUtils.getString(style, mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
            if (direction.equals(mxConstants.DIRECTION_EAST) || direction.equals(mxConstants.DIRECTION_WEST)) {
                y = Math.round(y + h / 2);
                h = 1;
            } else {
                x = Math.round(y + w / 2);
                w = 1;
            }
        }
        if (mxUtils.isTrue(style, mxConstants.STYLE_SHADOW, false) && fillColor != null) {
            Element shadow = (Element) elem.cloneNode(true);
            String s = "overflow:hidden;position:absolute;" + "left:" + String.valueOf(x + mxConstants.SHADOW_OFFSETX) + "px;" + "top:" + String.valueOf(y + mxConstants.SHADOW_OFFSETY) + "px;" + "width:" + String.valueOf(w) + "px;" + "height:" + String.valueOf(h) + "px;background:" + mxConstants.W3C_SHADOWCOLOR + ";border-style:solid;border-color:" + mxConstants.W3C_SHADOWCOLOR + ";border-width:" + String.valueOf(Math.round(strokeWidth)) + ";";
            shadow.setAttribute("style", s);
            appendHtmlElement(shadow);
        }
        if (shape.equals(mxConstants.SHAPE_IMAGE)) {
            String img = getImageForStyle(style);
            if (img != null) {
                elem = document.createElement("img");
                elem.setAttribute("border", "0");
                elem.setAttribute("src", img);
            }
        }
        String s = "overflow:hidden;position:absolute;" + "left:" + String.valueOf(x) + "px;" + "top:" + String.valueOf(y) + "px;" + "width:" + String.valueOf(w) + "px;" + "height:" + String.valueOf(h) + "px;background:" + fillColor + ";" + ";border-style:solid;border-color:" + strokeColor + ";border-width:" + String.valueOf(Math.round(strokeWidth)) + ";";
        elem.setAttribute("style", s);
        appendHtmlElement(elem);
        return elem;
    }

    /**
	 * Draws the given lines as segments between all points of the given list
	 * of mxPoints.
	 * 
	 * @param pts List of points that define the line.
	 * @param style Style to be used for painting the line.
	 */
    public void drawLine(List pts, Hashtable style) {
        String strokeColor = mxUtils.getString(style, mxConstants.STYLE_STROKECOLOR);
        int strokeWidth = (int) (mxUtils.getInt(style, mxConstants.STYLE_STROKEWIDTH, 1) * scale);
        if (strokeColor != null && strokeWidth > 0) {
            mxPoint last = (mxPoint) pts.get(0);
            for (int i = 1; i < pts.size(); i++) {
                mxPoint pt = (mxPoint) pts.get(i);
                drawSegment((int) last.getX(), (int) last.getY(), (int) pt.getX(), (int) pt.getY(), strokeColor, strokeWidth);
                last = pt;
            }
        }
    }

    /**
	 * Draws the specified segment of a line.
	 * 
	 * @param x0 X-coordinate of the start point.
	 * @param y0 Y-coordinate of the start point.
	 * @param x1 X-coordinate of the end point.
	 * @param y1 Y-coordinate of the end point.
	 * @param strokeColor Color of the stroke to be painted.
	 * @param strokeWidth Width of the stroke to be painted.
	 */
    protected void drawSegment(int x0, int y0, int x1, int y1, String strokeColor, int strokeWidth) {
        int tmpX = Math.min(x0, x1);
        int tmpY = Math.min(y0, y1);
        int width = Math.max(x0, x1) - tmpX;
        int height = Math.max(y0, y1) - tmpY;
        x0 = tmpX;
        y0 = tmpY;
        if (width == 0 || height == 0) {
            String s = "overflow:hidden;position:absolute;" + "left:" + String.valueOf(x0) + "px;" + "top:" + String.valueOf(y0) + "px;" + "width:" + String.valueOf(width) + "px;" + "height:" + String.valueOf(height) + "px;" + "border-color:" + strokeColor + ";" + "border-style:solid;" + "border-width:1 1 0 0px;";
            Element elem = document.createElement("div");
            elem.setAttribute("style", s);
            appendHtmlElement(elem);
        } else {
            int x = x0 + (x1 - x0) / 2;
            drawSegment(x0, y0, x, y0, strokeColor, strokeWidth);
            drawSegment(x, y0, x, y1, strokeColor, strokeWidth);
            drawSegment(x, y1, x1, y1, strokeColor, strokeWidth);
        }
    }

    /**
	 * Draws the specified text either using drawHtmlString or using drawString.
	 * 
	 * @param text Text to be painted.
	 * @param x X-coordinate of the text.
	 * @param y Y-coordinate of the text.
	 * @param w Width of the text.
	 * @param h Height of the text.
	 * @param style Style to be used for painting the text.
	 */
    public Element drawText(String text, int x, int y, int w, int h, Hashtable style) {
        Element table = mxUtils.createTable(document, text, x, y, w, h, scale, style);
        appendHtmlElement(table);
        return table;
    }
}
