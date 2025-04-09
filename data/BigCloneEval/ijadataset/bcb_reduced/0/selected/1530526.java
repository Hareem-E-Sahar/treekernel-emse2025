package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.browser.OS;
import org.eclipse.swt.internal.xhtml.CSSStyle;
import org.eclipse.swt.internal.xhtml.Element;
import org.eclipse.swt.internal.xhtml.document;
import org.eclipse.swt.widgets.*;

/**
 * Instances of this class represent a selectable user interface object
 * that represent a page in a notebook widget.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.CLOSE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */
public class CTabItem extends Item {

    CTabFolder parent;

    int x, y, width, height = 0;

    Control control;

    String toolTipText;

    String shortenedText;

    int shortenedTextWidth;

    Font font;

    Image disabledImage;

    Rectangle closeRect = new Rectangle(0, 0, 0, 0);

    int closeImageState = CTabFolder.NONE;

    boolean showClose = false;

    boolean showing = false;

    static final int TOP_MARGIN = 2;

    static final int BOTTOM_MARGIN = 2;

    static final int LEFT_MARGIN = 4;

    static final int RIGHT_MARGIN = 4;

    static final int INTERNAL_SPACING = 4;

    static final int FLAGS = SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC;

    static final String ELLIPSIS = "...";

    Element seperatorLine;

    Element imageHandle;

    Element textHandle;

    Element closeBtn;

    Element[] elements = new Element[0];

    /**
 * Constructs a new instance of this class given its parent
 * (which must be a <code>CTabFolder</code>) and a style value
 * describing its behavior and appearance. The item is added
 * to the end of the items maintained by its parent.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p>
 *
 * @param parent a CTabFolder which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#getStyle()
 */
    public CTabItem(CTabFolder parent, int style) {
        this(parent, style, parent.getItemCount());
    }

    /**
 * Constructs a new instance of this class given its parent
 * (which must be a <code>CTabFolder</code>), a style value
 * describing its behavior and appearance, and the index
 * at which to place it in the items maintained by its parent.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p>
 *
 * @param parent a CTabFolder which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 * @param index the zero-relative index to store the receiver in its parent
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the parent (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#getStyle()
 */
    public CTabItem(CTabFolder parent, int style, int index) {
        super(parent, checkStyle(style));
        showClose = (style & SWT.CLOSE) != 0;
        parent.createItem(this, index);
    }

    static int checkStyle(int style) {
        return SWT.NONE;
    }

    boolean useEllipses() {
        return parent.simple;
    }

    String shortenText(String text, int width) {
        return useEllipses() ? shortenText(text, width, ELLIPSIS) : shortenText(text, width, "");
    }

    private Point textExtent(String text, int flags) {
        return OS.getStringStyledSize(text, "ctabitem-text", null);
    }

    String shortenText(String text, int width, String ellipses) {
        if (textExtent(text, FLAGS).x <= width) return text;
        int ellipseWidth = textExtent(ellipses, FLAGS).x;
        int length = text.length();
        int end = length - 1;
        while (end > 0) {
            text = text.substring(0, end);
            int l = textExtent(text, FLAGS).x;
            if (l + ellipseWidth <= width) {
                return text + ellipses;
            }
            end--;
        }
        return text.substring(0, 1);
    }

    public void dispose() {
        if (isDisposed()) return;
        if (closeBtn != null) {
            OS.destroyHandle(closeBtn);
            closeBtn = null;
        }
        if (textHandle != null) {
            OS.destroyHandle(textHandle);
            textHandle = null;
        }
        if (imageHandle != null) {
            OS.destroyHandle(imageHandle);
            imageHandle = null;
        }
        if (elements != null) {
            for (int i = 0; i < elements.length; i++) {
                Element el = elements[i];
                if (el != null) {
                    OS.destroyHandle(el);
                    elements[i] = null;
                }
            }
            elements = null;
        }
        parent.destroyItem(this);
        super.dispose();
        parent = null;
        control = null;
        toolTipText = null;
        shortenedText = null;
        font = null;
    }

    void drawClose(GC gc) {
        if (closeBtn != null) {
            closeBtn.style.display = parent.showClose ? "" : "none";
        }
        if (closeRect.width == 0 || closeRect.height == 0) return;
        int indent = Math.max(1, (CTabFolder.BUTTON_SIZE - 9) / 2);
        int x = closeRect.x + indent;
        int y = closeRect.y + indent;
        y += parent.onBottom ? -1 : 1;
        if (closeBtn == null) {
            closeBtn = document.createElement("DIV");
            parent.handle.appendChild(closeBtn);
            closeBtn.title = "Close";
        }
        closeBtn.style.left = x + "px";
        closeBtn.style.top = y + "px";
        switch(closeImageState) {
            case CTabFolder.NORMAL:
                {
                    closeBtn.className = "ctabitem-close";
                    break;
                }
            case CTabFolder.HOT:
                {
                    closeBtn.className = "ctabitem-close-hover";
                    break;
                }
            case CTabFolder.SELECTED:
                {
                    closeBtn.className = "ctabitem-close";
                    break;
                }
            case CTabFolder.NONE:
                {
                    closeBtn.className = "ctabitem-none";
                    break;
                }
        }
    }

    void drawSelected(GC gc) {
        if (textHandle != null) {
            textHandle.style.display = "";
        }
        if (imageHandle != null) {
            imageHandle.style.display = "";
        }
        if (seperatorLine != null) {
            seperatorLine.style.display = "";
        }
        if (closeBtn != null) {
            closeBtn.style.display = "";
        }
        Point size = parent.getSize();
        int rightEdge = Math.min(x + width, parent.getRightItemEdge());
        int xx = parent.borderLeft;
        int yy = parent.onBottom ? size.y - parent.borderBottom - parent.tabHeight - parent.highlight_header : parent.borderTop + parent.tabHeight + 1;
        int ww = size.x - parent.borderLeft - parent.borderRight;
        int hh = parent.highlight_header - 1;
        int[] shape = new int[] { xx, yy, xx + ww, yy, xx + ww, yy + hh, xx, yy + hh };
        if (parent.selectionGradientColors != null && !parent.selectionGradientVertical) {
            parent.drawBackground(gc, shape, true);
        } else {
        }
        if (elements[0] == null) {
            Element el = document.createElement("DIV");
            el.className = "ctabfolder-border-line";
            parent.handle.appendChild(el);
            elements[0] = el;
        }
        CSSStyle s = elements[0].style;
        s.left = (x + 2) + "px";
        s.top = (parent.onBottom ? y + height - 1 : y) + "px";
        s.width = (width + 1 - 2 - (!OS.isIE ? 2 : 0)) + "px";
        s.backgroundColor = CTabFolder.borderColor.getCSSHandle();
        if (elements[1] == null) {
            Element el = document.createElement("DIV");
            el.className = "ctabfolder-border-pixel";
            parent.handle.appendChild(el);
            elements[1] = el;
        }
        s = elements[1].style;
        s.left = x + 1 + "px";
        s.top = (parent.onBottom ? y + height - 2 : y + 1) + "px";
        s.width = (width + 1 - 2 - (!OS.isIE ? 2 : 0)) + "px";
        s.backgroundColor = parent.selectionBackground.getCSSHandle();
        if (elements[2] == null) {
            Element el = document.createElement("DIV");
            el.className = "ctabfolder-border-pixel";
            parent.handle.appendChild(el);
            elements[2] = el;
        }
        s = elements[2].style;
        s.left = x + "px";
        s.top = (parent.onBottom ? y - 1 : y + 2) + "px";
        s.width = (width + 1 - (!OS.isIE ? 2 : 0)) + "px";
        s.height = (height - 1) + "px";
        s.backgroundColor = parent.selectionBackground.getCSSHandle();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null) {
                elements[i].style.display = "";
            }
        }
        if (seperatorLine != null) {
            seperatorLine.style.display = "none";
        }
        int xDraw = x + LEFT_MARGIN;
        if (parent.single && (parent.showClose || showClose)) xDraw += CTabFolder.BUTTON_SIZE;
        Image image = getImage();
        if (image != null) {
            Rectangle imageBounds = image.getBounds();
            int maxImageWidth = rightEdge - xDraw - RIGHT_MARGIN;
            if (!parent.single && closeRect.width > 0) maxImageWidth -= closeRect.width + INTERNAL_SPACING;
            if (imageBounds.width < maxImageWidth) {
                int imageX = xDraw;
                int imageY = y + (height - imageBounds.height) / 2;
                imageY += parent.onBottom ? -1 : 1;
                drawImage(image, imageX, imageY, imageBounds.width, imageBounds.height);
                xDraw += imageBounds.width + INTERNAL_SPACING;
            }
        }
        int textWidth = rightEdge - xDraw - RIGHT_MARGIN;
        if (!parent.single && closeRect.width > 0) textWidth -= closeRect.width + INTERNAL_SPACING;
        if (textWidth > 0) {
            if (shortenedText == null || shortenedTextWidth != textWidth) {
                shortenedText = shortenText(getText(), textWidth);
                shortenedTextWidth = textWidth;
            }
            Point extent = textExtent(shortenedText, FLAGS);
            int textY = y + (height - extent.y) / 2;
            textY += parent.onBottom ? -1 : 1;
            drawText(shortenedText, xDraw, textY, FLAGS);
        }
        if (parent.showClose || showClose) drawClose(gc);
    }

    void drawHighlight(GC gc, int rightEdge) {
        if (parent.simple || this.parent.onBottom) return;
        if (parent.selectionHighlightGradientBegin == null) return;
        Color[] gradients = parent.selectionHighlightGradientColorsCache;
        if (gradients == null) return;
        int gradientsSize = gradients.length;
        if (gradientsSize == 0) return;
        gc.setForeground(gradients[0]);
        gc.drawLine(CTabFolder.TOP_LEFT_CORNER_HILITE[0] + x + 1, 1 + y, rightEdge - parent.curveIndent, 1 + y);
        int[] leftHighlightCurve = CTabFolder.TOP_LEFT_CORNER_HILITE;
        int d = parent.tabHeight - parent.topCurveHighlightEnd.length / 2;
        int lastX = 0;
        int lastY = 0;
        int lastColorIndex = 0;
        for (int i = 0; i < leftHighlightCurve.length / 2; i++) {
            int rawX = leftHighlightCurve[i * 2];
            int rawY = leftHighlightCurve[i * 2 + 1];
            lastX = rawX + x;
            lastY = rawY + y;
            lastColorIndex = rawY - 1;
            gc.setForeground(gradients[lastColorIndex]);
            gc.drawPoint(lastX, lastY);
        }
        for (int i = lastColorIndex; i < gradientsSize; i++) {
            gc.setForeground(gradients[i]);
            gc.drawPoint(lastX, 1 + lastY++);
        }
        int rightEdgeOffset = rightEdge - parent.curveIndent;
        for (int i = 0; i < parent.topCurveHighlightStart.length / 2; i++) {
            int rawX = parent.topCurveHighlightStart[i * 2];
            int rawY = parent.topCurveHighlightStart[i * 2 + 1];
            lastX = rawX + rightEdgeOffset;
            lastY = rawY + y;
            lastColorIndex = rawY - 1;
            if (lastColorIndex >= gradientsSize) break;
            gc.setForeground(gradients[lastColorIndex]);
            gc.drawPoint(lastX, lastY);
        }
        for (int i = lastColorIndex; i < lastColorIndex + d; i++) {
            if (i >= gradientsSize) break;
            gc.setForeground(gradients[i]);
            gc.drawPoint(1 + lastX++, 1 + lastY++);
        }
        for (int i = 0; i < parent.topCurveHighlightEnd.length / 2; i++) {
            int rawX = parent.topCurveHighlightEnd[i * 2];
            int rawY = parent.topCurveHighlightEnd[i * 2 + 1];
            lastX = rawX + rightEdgeOffset;
            lastY = rawY + y;
            lastColorIndex = rawY - 1;
            if (lastColorIndex >= gradientsSize) break;
            gc.setForeground(gradients[lastColorIndex]);
            gc.drawPoint(lastX, lastY);
        }
    }

    void drawRightUnselectedBorder(GC gc) {
        if (this.parent.simple) {
            if (seperatorLine == null) {
                Element el = document.createElement("DIV");
                el.className = "ctabfolder-border-vline";
                this.parent.handle.appendChild(el);
                seperatorLine = el;
            }
            CSSStyle s = seperatorLine.style;
            s.left = (x + width - 1) + "px";
            s.top = y + "px";
            s.height = height + "px";
        } else {
        }
    }

    void drawBorder(GC gc, int[] shape) {
        gc.setForeground(CTabFolder.borderColor);
        gc.drawPolyline(shape);
    }

    void drawLeftUnselectedBorder(GC gc) {
        if (this.parent.simple) {
            if (seperatorLine == null) {
                Element el = document.createElement("DIV");
                el.className = "ctabfolder-border-vline";
                this.parent.handle.appendChild(el);
                seperatorLine = el;
            }
            CSSStyle s = seperatorLine.style;
            s.left = x + "px";
            s.top = y + "px";
            s.height = height + "px";
        } else {
        }
    }

    void drawUnselected(GC gc) {
        String displayStr = showing ? "" : "none";
        if (textHandle != null) {
            textHandle.style.display = displayStr;
        }
        if (imageHandle != null) {
            imageHandle.style.display = displayStr;
        }
        if (seperatorLine != null) {
            seperatorLine.style.display = displayStr;
        }
        if (closeBtn != null) {
            closeBtn.style.display = displayStr;
        }
        if (!showing) return;
        int index = parent.indexOf(this);
        if (index > 0 && index < parent.selectedIndex) drawLeftUnselectedBorder(gc);
        if (index > parent.selectedIndex) drawRightUnselectedBorder(gc);
        if (seperatorLine != null) {
            seperatorLine.style.display = "";
            if (index == 0 && index < parent.selectedIndex) {
                seperatorLine.style.display = "none";
            }
        }
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null) {
                elements[i].style.display = "none";
            }
        }
        int xDraw = x + LEFT_MARGIN;
        Image image = getImage();
        if (image != null && parent.showUnselectedImage) {
            Rectangle imageBounds = image.getBounds();
            int maxImageWidth = x + width - xDraw - RIGHT_MARGIN;
            if (parent.showUnselectedClose && (parent.showClose || showClose)) {
                maxImageWidth -= closeRect.width + INTERNAL_SPACING;
            }
            if (imageBounds.width < maxImageWidth) {
                int imageX = xDraw;
                int imageHeight = imageBounds.height;
                int imageY = y + (height - imageHeight) / 2;
                imageY += parent.onBottom ? -1 : 1;
                int imageWidth = imageBounds.width * imageHeight / imageBounds.height;
                drawImage(image, imageX, imageY, imageWidth, imageHeight);
                xDraw += imageWidth + INTERNAL_SPACING;
            }
        } else {
            if (imageHandle != null) {
                handle.style.display = "none";
            }
        }
        int textWidth = x + width - xDraw - RIGHT_MARGIN;
        if (parent.showUnselectedClose && (parent.showClose || showClose)) {
            textWidth -= closeRect.width + INTERNAL_SPACING;
        }
        if (textWidth > 0) {
            if (shortenedText == null || shortenedTextWidth != textWidth) {
                shortenedText = shortenText(getText(), textWidth);
                shortenedTextWidth = textWidth;
            }
            Point extent = textExtent(shortenedText, FLAGS);
            int textY = y + (height - extent.y) / 2;
            textY += parent.onBottom ? -1 : 1;
            drawText(shortenedText, xDraw, textY, FLAGS);
        }
        if (parent.showUnselectedClose && (parent.showClose || showClose)) drawClose(gc);
    }

    private void drawImage(Image image, int imageX, int imageY, int imageWidth, int imageHeight) {
        if (imageHandle == null) {
            imageHandle = document.createElement("DIV");
            parent.handle.appendChild(imageHandle);
            imageHandle.className = "ctabitem-image";
        }
        CSSStyle handleStyle = imageHandle.style;
        handleStyle.left = imageX + "px";
        handleStyle.top = imageY + "px";
        handleStyle.width = imageWidth + "px";
        handleStyle.height = imageHeight + "px";
        if (OS.isIENeedPNGFix && image.url != null && image.url.toLowerCase().endsWith(".png") && handleStyle.filter != null) {
            handleStyle.backgroundImage = "";
            handleStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\"" + image.url + "\", sizingMethod=\"image\")";
        } else {
            if (OS.isIENeedPNGFix && handleStyle.filter != null) handleStyle.filter = "";
            handleStyle.backgroundImage = "url(\"" + image.url + "\")";
        }
    }

    private void drawText(String string, int lineX, int lineY, int flags) {
        if (textHandle == null) {
            Element textEl = document.createElement("DIV");
            textEl.className = "ctabitem-text";
            textEl.style.position = "absolute";
            parent.handle.appendChild(textEl);
            textHandle = textEl;
            OS.setTextSelection(textEl, false);
        }
        if (textHandle.childNodes.length == 0 || textHandle.childNodes[0].nodeValue != string) {
            OS.clearChildren(textHandle);
            textHandle.appendChild(document.createTextNode(string));
        }
        textHandle.style.left = lineX + "px";
        textHandle.style.top = lineY + "px";
    }

    /**
 * Returns a rectangle describing the receiver's size and location
 * relative to its parent.
 *
 * @return the receiver's bounding column rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
    public Rectangle getBounds() {
        int w = width;
        if (!parent.simple && !parent.single && parent.indexOf(this) == parent.selectedIndex) w += parent.curveWidth - parent.curveIndent;
        return new Rectangle(x, y, w, height);
    }

    /**
* Gets the control that is displayed in the content area of the tab item.
*
* @return the control
*
* @exception SWTException <ul>
*    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
*    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
* </ul>
*/
    public Control getControl() {
        checkWidget();
        return control;
    }

    /**
 * Get the image displayed in the tab if the tab is disabled.
 * 
 * @return the disabled image or null
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @deprecated the disabled image is not used
 */
    public Image getDisabledImage() {
        checkWidget();
        return disabledImage;
    }

    /**
 * Returns the font that the receiver will use to paint textual information.
 *
 * @return the receiver's font
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 *  @since 3.0
 */
    public Font getFont() {
        checkWidget();
        if (font != null) return font;
        return parent.getFont();
    }

    /**
 * Returns the receiver's parent, which must be a <code>CTabFolder</code>.
 *
 * @return the receiver's parent
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
    public CTabFolder getParent() {
        return parent;
    }

    /**
 * Returns the receiver's tool tip text, or null if it has
 * not been set.
 *
 * @return the receiver's tool tip text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
    public String getToolTipText() {
        checkWidget();
        if (toolTipText == null && shortenedText != null) {
            String text = getText();
            if (!shortenedText.equals(text)) return text;
        }
        return toolTipText;
    }

    /**
* Returns <code>true</code> if the item will be rendered in the visible area of the CTabFolder. Returns false otherwise.
* 
*  @return <code>true</code> if the item will be rendered in the visible area of the CTabFolder. Returns false otherwise.
* 
*  @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
* @since 3.0
*/
    public boolean isShowing() {
        checkWidget();
        return showing;
    }

    void onPaint(GC gc, boolean isSelected) {
        if (width == 0 || height == 0) return;
        if (isSelected) {
            drawSelected(gc);
        } else {
            drawUnselected(gc);
        }
    }

    int preferredHeight(GC gc) {
        Image image = getImage();
        int h = (image == null) ? 0 : image.getBounds().height;
        String text = getText();
        if (font == null) {
            h = Math.max(h, gc.textExtent(text, FLAGS).y);
        } else {
            Font gcFont = gc.getFont();
            gc.setFont(font);
            h = Math.max(h, gc.textExtent(text, FLAGS).y);
            gc.setFont(gcFont);
        }
        return h + TOP_MARGIN + BOTTOM_MARGIN;
    }

    int preferredWidth(GC gc, boolean isSelected, boolean minimum) {
        if (isDisposed()) return 0;
        int w = 0;
        Image image = getImage();
        if (image != null && (isSelected || parent.showUnselectedImage)) {
            w += image.getBounds().width;
        }
        String text = null;
        if (minimum) {
            int minChars = parent.minChars;
            text = minChars == 0 ? null : getText();
            if (text != null && text.length() > minChars) {
                if (useEllipses()) {
                    int end = minChars < ELLIPSIS.length() + 1 ? minChars : minChars - ELLIPSIS.length();
                    text = text.substring(0, end);
                    if (minChars > ELLIPSIS.length() + 1) text += ELLIPSIS;
                } else {
                    int end = minChars;
                    text = text.substring(0, end);
                }
            }
        } else {
            text = getText();
        }
        if (text != null) {
            if (w > 0) w += INTERNAL_SPACING;
            if (font == null) {
                w += gc.textExtent(text, FLAGS).x;
            } else {
                Font gcFont = gc.getFont();
                gc.setFont(font);
                w += gc.textExtent(text, FLAGS).x;
                gc.setFont(gcFont);
            }
        }
        if (parent.showClose || showClose) {
            if (isSelected || parent.showUnselectedClose) {
                if (w > 0) w += INTERNAL_SPACING;
                w += CTabFolder.BUTTON_SIZE;
            }
        }
        return w + LEFT_MARGIN + RIGHT_MARGIN;
    }

    /**
 * Sets the control that is used to fill the client area of
 * the tab folder when the user selects the tab item.
 *
 * @param control the new control (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li> 
 *    <li>ERROR_INVALID_PARENT - if the control is not in the same widget tree</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
    public void setControl(Control control) {
        checkWidget();
        if (control != null) {
            if (control.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            if (control.getParent() != parent) SWT.error(SWT.ERROR_INVALID_PARENT);
        }
        if (this.control != null && !this.control.isDisposed()) {
            this.control.setVisible(false);
        }
        this.control = control;
        if (this.control != null) {
            int index = parent.indexOf(this);
            if (index == parent.getSelectionIndex()) {
                this.control.setBounds(parent.getClientArea());
                this.control.setVisible(true);
            } else {
                this.control.setVisible(false);
            }
        }
    }

    /**
 * Sets the image that is displayed if the tab item is disabled.
 * Null will clear the image.
 * 
 * @param image the image to be displayed when the item is disabled or null
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @deprecated This image is not used
 */
    public void setDisabledImage(Image image) {
        checkWidget();
        if (image != null && image.isDisposed()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.disabledImage = image;
    }

    /**
 * Sets the font that the receiver will use to paint textual information
 * for this item to the font specified by the argument, or to the default font
 * for that kind of control if the argument is null.
 *
 * @param font the new font (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @since 3.0
 */
    public void setFont(Font font) {
        checkWidget();
        if (font != null && font.isDisposed()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (font == null && this.font == null) return;
        if (font != null && font.equals(this.font)) return;
        this.font = font;
        if (!parent.updateTabHeight(false)) {
            parent.updateItems();
            parent.redrawTabs();
        }
    }

    public void setImage(Image image) {
        checkWidget();
        if (image != null && image.isDisposed()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        Image oldImage = getImage();
        if (image == null && oldImage == null) return;
        if (image != null && image.equals(oldImage)) return;
        super.setImage(image);
        if (!parent.updateTabHeight(false)) {
            if (oldImage != null && image != null) {
                Rectangle oldBounds = oldImage.getBounds();
                Rectangle bounds = image.getBounds();
                if (bounds.width == oldBounds.width && bounds.height == oldBounds.height) {
                    if (showing) {
                        boolean selected = parent.indexOf(this) == parent.selectedIndex;
                        if (selected || parent.showUnselectedImage) {
                            int imageX = x + LEFT_MARGIN, maxImageWidth;
                            if (selected) {
                                if (parent.single && (parent.showClose || showClose)) imageX += CTabFolder.BUTTON_SIZE;
                                int rightEdge = Math.min(x + width, parent.getRightItemEdge());
                                maxImageWidth = rightEdge - imageX - RIGHT_MARGIN;
                                if (!parent.single && closeRect.width > 0) maxImageWidth -= closeRect.width + INTERNAL_SPACING;
                            } else {
                                maxImageWidth = x + width - imageX - RIGHT_MARGIN;
                                if (parent.showUnselectedClose && (parent.showClose || showClose)) {
                                    maxImageWidth -= closeRect.width + INTERNAL_SPACING;
                                }
                            }
                            if (bounds.width < maxImageWidth) {
                                int imageY = y + (height - bounds.height) / 2 + (parent.onBottom ? -1 : 1);
                                parent.redraw(imageX, imageY, bounds.width, bounds.height, false);
                            }
                        }
                    }
                    return;
                }
            }
            parent.updateItems();
            parent.redrawTabs();
        }
    }

    public void setText(String string) {
        checkWidget();
        if (string == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (string.equals(getText())) return;
        super.setText(string);
        shortenedText = null;
        shortenedTextWidth = 0;
        if (!parent.updateTabHeight(false)) {
            parent.updateItems();
            parent.redrawTabs();
        }
    }

    /**
 * Sets the receiver's tool tip text to the argument, which
 * may be null indicating that no tool tip text should be shown.
 *
 * @param string the new tool tip text (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
    public void setToolTipText(String string) {
        checkWidget();
        toolTipText = string;
    }
}
