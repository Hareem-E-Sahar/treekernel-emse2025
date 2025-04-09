package dendrarium.trees.svg;

import dendrarium.trees.disamb.ChildInfo;
import dendrarium.utils.StringUtils;
import dendrarium.utils.XMLUtils;
import java.util.List;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author tomek
 */
public class NodeTreeDrawer {

    Integer imageBottom;

    Integer treeBottom;

    Integer nodeY;

    Integer horizLineY;

    Integer rootY;

    Integer ruleY;

    public byte[] getSVGTree(String rule, List<ChildInfo> childList) {
        Document doc = getDocNodeTree(rule, childList);
        return XMLUtils.writePrettyByteArrayJDOM(doc);
    }

    public Document getDocNodeTree(String rule, List<ChildInfo> childList) {
        DocType docType = SVGJdom.getStandardDocType();
        Element svg = drawSVGNodeTree(rule, childList);
        Document doc = new Document(svg, docType);
        return doc;
    }

    private Element drawSVGNodeTree(String rule, List<ChildInfo> childList) {
        Integer imageWidth = 0;
        Element svg = SVGJdom.getStandardSVG();
        Integer bottomTableHeight = DrawConfig.getTextVertDistance() + DrawConfig.getTextFontSize() / 2;
        treeBottom = DrawConfig.getLevelSize() * 2;
        imageBottom = treeBottom + bottomTableHeight;
        nodeY = treeBottom - DrawConfig.getLevelSize();
        horizLineY = nodeY - 2 * DrawConfig.getTextFontSize();
        rootY = horizLineY - 3 * DrawConfig.getTextFontSize() / 2;
        ruleY = horizLineY - DrawConfig.getTextFontSize() / 2;
        int i = 0;
        int firstRight = 0;
        int lastLeft = 0;
        for (ChildInfo child : childList) {
            lastLeft = imageWidth;
            imageWidth = drawChild(child, imageWidth, svg);
            if (i == 0) {
                firstRight = imageWidth;
            }
            i++;
        }
        if (childList.size() > 1) {
            SVGJdom.drawHorizLine(firstRight / 2, (lastLeft + imageWidth) / 2, horizLineY, svg);
        }
        SVGJdom.drawVertLine(imageWidth / 2, horizLineY, rootY, svg);
        SVGJdom.drawText(rule, imageWidth / 2 + DrawConfig.getRuleFontSize(), ruleY, DrawConfig.getTextColor(), DrawConfig.getRuleFontSize().toString(), svg);
        svg.setAttribute("viewBox", "-2 -2 " + (imageWidth + 4) + " " + (imageBottom + 4));
        Integer realWidth = imageWidth / DrawConfig.getVariantVirtualToReal();
        Integer realHeight = imageBottom / DrawConfig.getVariantVirtualToReal();
        svg.setAttribute("width", realWidth.toString() + "cm");
        svg.setAttribute("height", realHeight.toString() + "cm");
        SVGJdom.drawHorizLine(0, imageWidth, treeBottom, svg);
        SVGJdom.drawHorizLine(0, imageWidth, imageBottom, svg);
        SVGJdom.drawVertLine(0, treeBottom, imageBottom, svg);
        return svg;
    }

    private Integer drawChild(ChildInfo child, Integer xLeft, Element svg) {
        String text = StringUtils.join(child.getSubSentence().toArray(), " ");
        SVGJdom.drawText(text, xLeft + DrawConfig.getTextFontWidth() / 2, treeBottom + DrawConfig.getTextVertDistance(), DrawConfig.getTextColor(), DrawConfig.getTextFontSize().toString(), svg);
        int tw = SVGJdom.textWidth(child.getLabel() + ((child.getAttr().size() > 0) ? (child.getAttrString() + "()") : ""));
        Integer xRight = xLeft + max(SVGJdom.textWidth(text), SVGJdom.rectangleWidth(tw));
        SVGJdom.drawVertLine(xRight, treeBottom, imageBottom, svg);
        Integer nodeX = (xLeft + xRight) / 2;
        SVGJdom.drawVertLine(nodeX, nodeY, horizLineY, svg);
        SVGJdom.drawHorizTriangle(xLeft, xRight, treeBottom, nodeY + DrawConfig.getPillowSize() * 3 / 2, DrawConfig.getVarTriangleStrokeColor(), DrawConfig.getVarTriangleColor(), svg);
        SVGJdom.drawTextColoredSVGNode(nodeX, nodeY, child.getLabel(), tw, child.getAttr(), child.getDiffAttr(), DrawConfig.getFillColor(), DrawConfig.getStrokeColor(), svg);
        return xRight;
    }

    int max(int x, int y) {
        return x > y ? x : y;
    }
}
