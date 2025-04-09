package com.android.ide.common.layout;

import static com.android.ide.common.layout.LayoutConstants.ANDROID_URI;
import static com.android.ide.common.layout.LayoutConstants.ATTR_LAYOUT_HEIGHT;
import static com.android.ide.common.layout.LayoutConstants.ATTR_LAYOUT_WIDTH;
import static com.android.ide.common.layout.LayoutConstants.ATTR_ORIENTATION;
import static com.android.ide.common.layout.LayoutConstants.VALUE_FILL_PARENT;
import static com.android.ide.common.layout.LayoutConstants.VALUE_HORIZONTAL;
import static com.android.ide.common.layout.LayoutConstants.VALUE_VERTICAL;
import com.android.ide.common.api.DrawingStyle;
import com.android.ide.common.api.DropFeedback;
import com.android.ide.common.api.IDragElement;
import com.android.ide.common.api.IFeedbackPainter;
import com.android.ide.common.api.IGraphics;
import com.android.ide.common.api.IMenuCallback;
import com.android.ide.common.api.INode;
import com.android.ide.common.api.INodeHandler;
import com.android.ide.common.api.IViewMetadata;
import com.android.ide.common.api.IViewMetadata.FillPreference;
import com.android.ide.common.api.IViewRule;
import com.android.ide.common.api.InsertType;
import com.android.ide.common.api.MenuAction;
import com.android.ide.common.api.Point;
import com.android.ide.common.api.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An {@link IViewRule} for android.widget.LinearLayout and all its derived
 * classes.
 */
public class LinearLayoutRule extends BaseLayoutRule {

    /**
     * Add an explicit Orientation toggle to the context menu.
     */
    @Override
    public List<MenuAction> getContextMenu(final INode selectedNode) {
        if (supportsOrientation()) {
            String curr_orient = selectedNode.getStringAttr(ANDROID_URI, ATTR_ORIENTATION);
            if (curr_orient == null || curr_orient.length() == 0) {
                curr_orient = VALUE_HORIZONTAL;
            }
            IMenuCallback onChange = new IMenuCallback() {

                public void action(MenuAction action, final String valueId, Boolean newValue) {
                    String actionId = action.getId();
                    final INode node = selectedNode;
                    if (actionId.equals("_orientation")) {
                        node.editXml("Change LinearLayout " + ATTR_ORIENTATION, new INodeHandler() {

                            public void handle(INode n) {
                                node.setAttribute(ANDROID_URI, ATTR_ORIENTATION, valueId);
                            }
                        });
                    }
                }
            };
            return concatenate(super.getContextMenu(selectedNode), new MenuAction.Choices("_orientation", "Orientation", mapify("horizontal", "Horizontal", "vertical", "Vertical"), curr_orient, onChange));
        } else {
            return super.getContextMenu(selectedNode);
        }
    }

    /**
     * Returns true if the given node represents a vertical linear layout.
     * @param node the node to check layout orientation for
     * @return true if the layout is in vertical mode, otherwise false
     */
    protected boolean isVertical(INode node) {
        return VALUE_VERTICAL.equals(node.getStringAttr(ANDROID_URI, ATTR_ORIENTATION));
    }

    /**
     * Returns true if this LinearLayout supports switching orientation.
     *
     * @return true if this layout supports orientations
     */
    protected boolean supportsOrientation() {
        return true;
    }

    @Override
    public DropFeedback onDropEnter(final INode targetNode, final IDragElement[] elements) {
        if (elements.length == 0) {
            return null;
        }
        Rect bn = targetNode.getBounds();
        if (!bn.isValid()) {
            return null;
        }
        boolean isVertical = isVertical(targetNode);
        List<MatchPos> indexes = new ArrayList<MatchPos>();
        int last = isVertical ? bn.y : bn.x;
        int pos = 0;
        boolean lastDragged = false;
        int selfPos = -1;
        for (INode it : targetNode.getChildren()) {
            Rect bc = it.getBounds();
            if (bc.isValid()) {
                boolean isDragged = false;
                for (IDragElement element : elements) {
                    if (bc.equals(element.getBounds())) {
                        isDragged = true;
                    }
                }
                if (isDragged) {
                    int v = isVertical ? bc.y + (bc.h / 2) : bc.x + (bc.w / 2);
                    selfPos = pos;
                    indexes.add(new MatchPos(v, pos++));
                } else if (lastDragged) {
                    pos++;
                } else {
                    int v = isVertical ? bc.y : bc.x;
                    v = (last + v) / 2;
                    indexes.add(new MatchPos(v, pos++));
                }
                last = isVertical ? (bc.y + bc.h) : (bc.x + bc.w);
                lastDragged = isDragged;
            } else {
                pos++;
            }
        }
        if (!lastDragged) {
            int v = last + 1;
            indexes.add(new MatchPos(v, pos));
        }
        int posCount = targetNode.getChildren().length + 1;
        return new DropFeedback(new LinearDropData(indexes, posCount, isVertical, selfPos), new IFeedbackPainter() {

            public void paint(IGraphics gc, INode node, DropFeedback feedback) {
                drawFeedback(gc, node, elements, feedback);
            }
        });
    }

    void drawFeedback(IGraphics gc, INode node, IDragElement[] elements, DropFeedback feedback) {
        Rect b = node.getBounds();
        if (!b.isValid()) {
            return;
        }
        gc.useStyle(DrawingStyle.DROP_RECIPIENT);
        gc.drawRect(b);
        gc.useStyle(DrawingStyle.DROP_ZONE);
        LinearDropData data = (LinearDropData) feedback.userData;
        boolean isVertical = data.isVertical();
        int selfPos = data.getSelfPos();
        for (MatchPos it : data.getIndexes()) {
            int i = it.getDistance();
            int pos = it.getPosition();
            if (pos != selfPos) {
                if (isVertical) {
                    gc.drawLine(b.x, i, b.x + b.w, i);
                } else {
                    gc.drawLine(i, b.y, i, b.y + b.h);
                }
            }
        }
        Integer currX = data.getCurrX();
        Integer currY = data.getCurrY();
        if (currX != null && currY != null) {
            gc.useStyle(DrawingStyle.DROP_ZONE_ACTIVE);
            int x = currX;
            int y = currY;
            Rect be = elements[0].getBounds();
            if (data.getInsertPos() != selfPos || selfPos == -1) {
                gc.useStyle(DrawingStyle.DROP_PREVIEW);
                if (data.getWidth() != null) {
                    int width = data.getWidth();
                    int fromX = x - width / 2;
                    int toX = x + width / 2;
                    gc.drawLine(fromX, y, toX, y);
                } else if (data.getHeight() != null) {
                    int height = data.getHeight();
                    int fromY = y - height / 2;
                    int toY = y + height / 2;
                    gc.drawLine(x, fromY, x, toY);
                }
            }
            if (be.isValid()) {
                boolean isLast = data.isLastPosition();
                int offsetX;
                int offsetY;
                if (isVertical) {
                    offsetX = b.x - be.x;
                    offsetY = currY - be.y - (isLast ? 0 : (be.h / 2));
                } else {
                    offsetX = currX - be.x - (isLast ? 0 : (be.w / 2));
                    offsetY = b.y - be.y;
                }
                gc.useStyle(DrawingStyle.DROP_PREVIEW);
                for (IDragElement element : elements) {
                    Rect bounds = element.getBounds();
                    if (bounds.isValid() && (bounds.w > b.w || bounds.h > b.h) && node.getChildren().length == 0) {
                        final int px, py, pw, ph;
                        if (bounds.w > b.w) {
                            px = b.x;
                            pw = b.w;
                        } else {
                            px = bounds.x + offsetX;
                            pw = bounds.w;
                        }
                        if (bounds.h > b.h) {
                            py = b.y;
                            ph = b.h;
                        } else {
                            py = bounds.y + offsetY;
                            ph = bounds.h;
                        }
                        Rect within = new Rect(px, py, pw, ph);
                        gc.drawRect(within);
                    } else {
                        drawElement(gc, element, offsetX, offsetY);
                    }
                }
            }
        }
    }

    @Override
    public DropFeedback onDropMove(INode targetNode, IDragElement[] elements, DropFeedback feedback, Point p) {
        Rect b = targetNode.getBounds();
        if (!b.isValid()) {
            return feedback;
        }
        LinearDropData data = (LinearDropData) feedback.userData;
        boolean isVertical = data.isVertical();
        int bestDist = Integer.MAX_VALUE;
        int bestIndex = Integer.MIN_VALUE;
        Integer bestPos = null;
        for (MatchPos index : data.getIndexes()) {
            int i = index.getDistance();
            int pos = index.getPosition();
            int dist = (isVertical ? p.y : p.x) - i;
            if (dist < 0) dist = -dist;
            if (dist < bestDist) {
                bestDist = dist;
                bestIndex = i;
                bestPos = pos;
                if (bestDist <= 0) break;
            }
        }
        if (bestIndex != Integer.MIN_VALUE) {
            Integer oldX = data.getCurrX();
            Integer oldY = data.getCurrY();
            if (isVertical) {
                data.setCurrX(b.x + b.w / 2);
                data.setCurrY(bestIndex);
                data.setWidth(b.w);
                data.setHeight(null);
            } else {
                data.setCurrX(bestIndex);
                data.setCurrY(b.y + b.h / 2);
                data.setWidth(null);
                data.setHeight(b.h);
            }
            data.setInsertPos(bestPos);
            feedback.requestPaint = !equals(oldX, data.getCurrX()) || !equals(oldY, data.getCurrY());
        }
        return feedback;
    }

    private static boolean equals(Integer i1, Integer i2) {
        if (i1 == i2) {
            return true;
        } else if (i1 != null) {
            return i1.equals(i2);
        } else {
            return i2.equals(i1);
        }
    }

    @Override
    public void onDropLeave(INode targetNode, IDragElement[] elements, DropFeedback feedback) {
    }

    @Override
    public void onDropped(final INode targetNode, final IDragElement[] elements, final DropFeedback feedback, final Point p) {
        LinearDropData data = (LinearDropData) feedback.userData;
        final int initialInsertPos = data.getInsertPos();
        final Map<String, Pair<String, String>> idMap = getDropIdMap(targetNode, elements, feedback.isCopy || !feedback.sameCanvas);
        targetNode.editXml("Add elements to LinearLayout", new INodeHandler() {

            public void handle(INode node) {
                int insertPos = initialInsertPos;
                for (IDragElement element : elements) {
                    String fqcn = element.getFqcn();
                    INode newChild = targetNode.insertChildAt(fqcn, insertPos);
                    if (insertPos >= 0) {
                        insertPos++;
                    }
                    addAttributes(newChild, element, idMap, DEFAULT_ATTR_FILTER);
                    addInnerElements(newChild, element, idMap);
                }
            }
        });
    }

    @Override
    public void onChildInserted(INode node, INode parent, InsertType insertType) {
        String fqcn = node.getFqcn();
        IViewMetadata metadata = mRulesEngine.getMetadata(fqcn);
        if (metadata != null) {
            boolean vertical = isVertical(parent);
            FillPreference fill = metadata.getFillPreference();
            if (fill.fillHorizontally(vertical)) {
                node.setAttribute(ANDROID_URI, ATTR_LAYOUT_WIDTH, VALUE_FILL_PARENT);
            }
            if (fill.fillVertically(vertical)) {
                node.setAttribute(ANDROID_URI, ATTR_LAYOUT_HEIGHT, VALUE_FILL_PARENT);
            }
        }
    }

    /** A possible match position */
    private class MatchPos {

        /** The pixel distance */
        private int mDistance;

        /** The position among siblings */
        private int mPosition;

        public MatchPos(int distance, int position) {
            this.mDistance = distance;
            this.mPosition = position;
        }

        @Override
        public String toString() {
            return "MatchPos [distance=" + mDistance + ", position=" + mPosition + "]";
        }

        private int getDistance() {
            return mDistance;
        }

        private int getPosition() {
            return mPosition;
        }
    }

    private class LinearDropData {

        /** Vertical layout? */
        private final boolean mVertical;

        /** Insert points (pixels + index) */
        private final List<MatchPos> mIndexes;

        /** Number of insert positions in the target node */
        private final int mNumPositions;

        /** Current marker X position */
        private Integer mCurrX;

        /** Current marker Y position */
        private Integer mCurrY;

        /** Position of the dragged element in this layout (or
            -1 if the dragged element is from elsewhere) */
        private final int mSelfPos;

        /** Current drop insert index (-1 for "at the end") */
        private int mInsertPos = -1;

        /** width of match line if it's a horizontal one */
        private Integer mWidth;

        /** height of match line if it's a vertical one */
        private Integer mHeight;

        public LinearDropData(List<MatchPos> indexes, int numPositions, boolean isVertical, int selfPos) {
            this.mIndexes = indexes;
            this.mNumPositions = numPositions;
            this.mVertical = isVertical;
            this.mSelfPos = selfPos;
        }

        @Override
        public String toString() {
            return "LinearDropData [currX=" + mCurrX + ", currY=" + mCurrY + ", height=" + mHeight + ", indexes=" + mIndexes + ", insertPos=" + mInsertPos + ", isVertical=" + mVertical + ", selfPos=" + mSelfPos + ", width=" + mWidth + "]";
        }

        private boolean isVertical() {
            return mVertical;
        }

        private void setCurrX(Integer currX) {
            this.mCurrX = currX;
        }

        private Integer getCurrX() {
            return mCurrX;
        }

        private void setCurrY(Integer currY) {
            this.mCurrY = currY;
        }

        private Integer getCurrY() {
            return mCurrY;
        }

        private int getSelfPos() {
            return mSelfPos;
        }

        private void setInsertPos(int insertPos) {
            this.mInsertPos = insertPos;
        }

        private int getInsertPos() {
            return mInsertPos;
        }

        private List<MatchPos> getIndexes() {
            return mIndexes;
        }

        private void setWidth(Integer width) {
            this.mWidth = width;
        }

        private Integer getWidth() {
            return mWidth;
        }

        private void setHeight(Integer height) {
            this.mHeight = height;
        }

        private Integer getHeight() {
            return mHeight;
        }

        /**
         * Returns true if we are inserting into the last position
         *
         * @return true if we are inserting into the last position
         */
        public boolean isLastPosition() {
            return mInsertPos == mNumPositions - 1;
        }
    }
}
