package edu.cmu.cs.bungee.client.viz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.CollationKey;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.SwingUtilities;
import edu.cmu.cs.bungee.client.query.ItemPredicate;
import edu.cmu.cs.bungee.client.query.Markup;
import edu.cmu.cs.bungee.client.query.Perspective;
import edu.cmu.cs.bungee.client.query.Query;
import edu.cmu.cs.bungee.client.viz.Summary.RankComponentHeights;
import edu.cmu.cs.bungee.javaExtensions.PerspectiveObserver;
import edu.cmu.cs.bungee.javaExtensions.Util;
import edu.cmu.cs.bungee.piccoloUtils.gui.APText;
import edu.cmu.cs.bungee.piccoloUtils.gui.Arrow;
import edu.cmu.cs.bungee.piccoloUtils.gui.LazyContainer;
import edu.cmu.cs.bungee.piccoloUtils.gui.LazyPNode;
import edu.cmu.cs.bungee.piccoloUtils.gui.LazyPPath;
import edu.cmu.cs.bungee.piccoloUtils.gui.MyInputEventHandler;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PInterpolatingActivity;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

final class PerspectiveViz extends LazyContainer implements FacetNode, PickFacetTextNotifier {

    private static final long serialVersionUID = 1L;

    Summary summary;

    Rank rank;

    PerspectiveViz parentPV;

    Perspective p;

    private SqueezablePNode labels;

    SqueezablePNode front;

    LazyPPath lightBeam = null;

    /**
	 * This will have isVisible==false unless we are the first child of the rank
	 */
    TextNfacets rankLabel;

    APText[] percentLabels;

    private LazyPNode percentLabelHotZone;

    private LazyPNode hotLine;

    private APText hotZonePopup;

    static final double PERCENT_LABEL_SCALE = 0.75;

    static final Color pvBG = new Color(0x1f2333);

    static final double epsilon = 1.0e-10;

    /**
	 * Our logical width, of which floor(leftEdge) - floor(leftEdge +
	 * visibleWidth) is visible. logicalWidth>=visibleWidth();
	 * 
	 * visibleWidth = w - epsilon bars are placed at floor(0 - logicalWidth)
	 */
    double logicalWidth = 0;

    /**
	 * offset into logicalWidth of the leftmost visible pixel. Rightmost visible
	 * pixel is leftEdge + w - epsilon 0<=leftEdge<logicalWidth-visibleWidth;
	 */
    double leftEdge = 0;

    boolean isZooming() {
        return logicalWidth > visibleWidth();
    }

    void resetLogicalBounds() {
        setLogicalBounds(0, visibleWidth());
    }

    void setLogicalBounds(double x, double w) {
        assert x >= 0 && x + visibleWidth() <= w : x + "/" + w + " " + visibleWidth();
        if (x != leftEdge || w != logicalWidth) {
            leftEdge = x;
            logicalWidth = w;
            revalidate();
        }
    }

    double visibleWidth() {
        return w - epsilon;
    }

    int labelHprojectionW;

    /**
	 * The facet of the bar at a given x coordinate. Only the endpoints are
	 * recorded. Noone should ask for coordinates in between the endpoints.
	 */
    private Perspective[] barXs;

    /**
	 * The facet of the label at a given x coordinate. All points are recorded.
	 */
    private Perspective[] labelXs;

    Hashtable<Perspective, Bar> barTable = new Hashtable<Perspective, Bar>();

    /**
	 * Remember previous layout parameters, and don't re-layout if they are the
	 * same.
	 */
    private RankComponentHeights prevComponentHeights;

    double prevH;

    MedianArrow medianArrow;

    PerspectiveViz(Perspective _p, Summary _summary) {
        p = _p;
        summary = _summary;
        parentPV = summary.lookupPV(p.getParent());
        setPickable(false);
        front = new SqueezablePNode();
        front.setPaint(pvBG);
        front.setHeight(1);
        addChild(front);
        labels = new SqueezablePNode();
        labels.setVisible(false);
        addChild(labels);
        rankLabel = new TextNfacets(art(), Bungee.summaryFG, true);
        rankLabel.setPickable(false);
        rankLabel.setWrapText(false);
        if (p.isOrdered()) {
            Color color = Markup.UNASSOCIATED_COLORS[1];
            medianArrow = new MedianArrow(color, 7, 0);
        }
        front.addInputEventListener(Bungee.facetClickHandler);
    }

    void delayedInit() {
        if (percentLabels == null) {
            percentLabels = new APText[3];
            boolean visible = rankLabel.getVisible();
            for (int i = 0; i < 3; i++) {
                percentLabels[i] = art().oneLineLabel();
                percentLabels[i].setTransparency(0);
                percentLabels[i].setVisible(visible);
                percentLabels[i].setPickable(false);
                percentLabels[i].setTextPaint(Bungee.PERCENT_LABEL_COLOR);
                percentLabels[i].setJustification(Component.RIGHT_ALIGNMENT);
                percentLabels[i].setConstrainWidthToTextWidth(false);
                front.addChild(percentLabels[i]);
            }
            percentLabels[0].setText(formatOddsRatio(Math.exp(-Rank.LOG_ODDS_RANGE)) + "+");
            percentLabels[1].setText(formatOddsRatio(Math.exp(0)));
            percentLabels[2].setText(formatOddsRatio(Math.exp(Rank.LOG_ODDS_RANGE)) + "+");
            percentLabelHotZone = new LazyPNode();
            percentLabelHotZone.setPickable(false);
            percentLabelHotZone.addInputEventListener(new HotZoneListener(this));
            hotLine = new LazyPNode();
            hotLine.setPaint(Bungee.PERCENT_LABEL_COLOR);
            hotLine.setVisible(false);
            hotLine.setPickable(false);
            hotZonePopup = art().oneLineLabel();
            hotZonePopup.setPaint(Color.black);
            hotZonePopup.setTextPaint(Bungee.helpColor);
            hotZonePopup.setVisible(false);
            hotZonePopup.setPickable(false);
            addChild(hotZonePopup);
        }
    }

    String getName() {
        return p.getName();
    }

    void updateData() {
        if (p.getTotalChildTotalCount() == 0) {
            query().removeRestrictionInternal(p);
            summary.synchronizeWithQuery();
        } else {
            assert rank.expectedPercentOn() >= 0;
            if (query().isQueryValid()) {
                for (Iterator<Bar> it = barTable.values().iterator(); it.hasNext(); ) {
                    Bar bar = it.next();
                    bar.updateData();
                }
            }
            if (medianArrow != null) {
                if (p.getOnCount() > 0 && art().getShowMedian()) {
                    front.addChild(medianArrow);
                    layoutMedianArrow();
                } else {
                    medianArrow.removeFromParent();
                }
            }
            drawLabels();
        }
    }

    void animateData(double zeroToOne) {
        for (Iterator<Bar> it = barTable.values().iterator(); it.hasNext(); ) {
            Bar bar = it.next();
            bar.animateData(zeroToOne);
        }
    }

    PActivity zoomer;

    void animatePanZoom(final double goalLeftEdge, final double goalLogicalWidth) {
        finishPanZoom();
        assert goalLeftEdge >= 0 && goalLeftEdge + visibleWidth() <= goalLogicalWidth : goalLeftEdge + "/" + goalLogicalWidth + " " + visibleWidth();
        zoomer = new PInterpolatingActivity(Bungee.rankAnimationMS, Bungee.rankAnimationStep) {

            final double startLeftEdge = leftEdge;

            final double startLogicalWidth = logicalWidth;

            @Override
            public void activityFinished() {
                zoomer = null;
                super.activityFinished();
            }

            @Override
            public void setRelativeTargetValue(float zeroToOne) {
                double newLeftEdge = Util.interpolate(startLeftEdge, goalLeftEdge, zeroToOne);
                double newLogicalWidth = Util.interpolate(startLogicalWidth, goalLogicalWidth, zeroToOne);
                try {
                    setLogicalBounds(newLeftEdge, newLogicalWidth);
                } catch (AssertionError e) {
                    Util.err("setRelativeTargetValue " + zeroToOne + " " + startLeftEdge + "/" + startLogicalWidth + " " + goalLeftEdge + "/" + goalLogicalWidth + " " + visibleWidth());
                    e.printStackTrace();
                }
            }
        };
        addActivity(zoomer);
    }

    void finishPanZoom() {
        if (zoomer != null) zoomer.terminate(0);
    }

    void validate(int _visibleWidth, boolean isShowRankLabels) {
        if (p.isPrefetched() || p.getParent() == null) {
            if (p.getTotalChildTotalCount() == 0) return;
            setPercentLabelVisible();
            front.setWidth(_visibleWidth);
            w = _visibleWidth;
            resetLogicalBounds();
            layoutLightBeam();
            rankLabel.setVisible(isShowRankLabels);
            if (!p.isPrefetched()) {
                queueDrawLetters();
            }
        } else {
            queuePrefetch();
        }
    }

    void revalidate() {
        if (visibleWidth() > 0) {
            assert logicalWidth > 0 : p + " " + leftEdge + "/" + logicalWidth;
            drawBars();
            drawLabels();
            if (medianArrow != null) {
                double logicalVisibleOffset = logicalWidth / 2 - leftEdge;
                if (logicalVisibleOffset >= 0 && logicalVisibleOffset < w) {
                    medianArrow.setOffset(logicalVisibleOffset, 1.0);
                } else medianArrow.removeFromParent();
                if (query().isQueryValid()) layoutMedianArrow();
            }
        }
    }

    void setFeatures() {
        facetPTexts.clear();
        if (medianArrow != null && p.guessOnCount() > 0) {
            if (art().getShowMedian()) front.addChild(medianArrow); else medianArrow.removeFromParent();
        }
        drawLabels();
        if (art().getShowZoomLetters()) {
            drawLetters();
        } else if (letters != null) {
            letters.removeFromParent();
            letters = null;
        }
    }

    void queuePrefetch() {
        queue(getDoValidate());
    }

    void queue(Runnable runnable) {
        Query q = query();
        q.unqueuePrefetch(runnable);
        q.queuePrefetch(p);
        q.queuePrefetch(runnable);
    }

    void queueDrawLetters() {
        queue(getDoDrawLetters());
    }

    void setPercentLabelVisible() {
        if (percentLabels != null) {
            boolean isShowRankLabels = this == rank.perspectives[0];
            percentLabels[0].setVisible(isShowRankLabels);
            percentLabels[1].setVisible(isShowRankLabels);
            percentLabels[2].setVisible(isShowRankLabels);
        }
    }

    private transient Runnable doValidate;

    Runnable getDoValidate() {
        if (doValidate == null) doValidate = new Runnable() {

            public void run() {
                rank.validateInternal();
            }
        };
        return doValidate;
    }

    private transient Runnable doDrawLetters;

    Runnable getDoDrawLetters() {
        if (doDrawLetters == null) doDrawLetters = new Runnable() {

            public void run() {
                drawLetters();
            }
        };
        return doDrawLetters;
    }

    void layoutMedianArrow() {
        if (p.isPrefetched()) {
            if (medianArrow.unconditionalMedian == null) {
                medianArrow.unconditionalMedian = p.getMedianPerspective(false);
            }
            double median = p.median(true);
            if (median >= 0.0) {
                int medianIndex = (int) median;
                double childFraction = median - medianIndex;
                Perspective medianChild = p.getNthChild(medianIndex);
                medianArrow.conditionalMedian = medianChild;
                Bar bar = barTable.get(medianChild);
                while (bar == null && medianIndex < p.nChildren() - 1) {
                    childFraction = 0.0;
                    bar = barTable.get(p.getNthChild(++medianIndex));
                }
                if (bar != null) {
                    int left = minBarPixelRaw(medianChild);
                    int right = maxBarPixelRaw(medianChild);
                    double x = Util.interpolate(left, right, (float) childFraction);
                    if (x < 0 || x > visibleWidth()) medianArrow.removeFromParent();
                    double length = x - medianArrow.getXOffset();
                    medianArrow.setLengthAndDirection((int) length);
                    medianArrow.updateColor(p.medianTestSignificant());
                    medianArrow.moveToFront();
                } else {
                    medianArrow.removeFromParent();
                }
            }
        }
    }

    void layoutPercentLabels() {
        if (isConnected()) {
            double frontH = summary.selectedFrontH();
            double yOffset = -PERCENT_LABEL_SCALE * art().lineH / 2.0 / frontH;
            double x = percentLabels[0].getXOffset();
            double scaleY = PERCENT_LABEL_SCALE / frontH;
            percentLabels[0].setTransform(Util.scaleNtranslate(PERCENT_LABEL_SCALE, scaleY, x, 1.0 + yOffset));
            percentLabels[1].setTransform(Util.scaleNtranslate(PERCENT_LABEL_SCALE, scaleY, x, 0.5 + yOffset));
            percentLabels[2].setTransform(Util.scaleNtranslate(PERCENT_LABEL_SCALE, scaleY, x, yOffset));
        }
    }

    void scaleMedianArrow() {
        if (medianArrow != null) {
            double scaleY = 1.0 / front.goalYscale;
            medianArrow.setTransform(Util.scaleNtranslate(1.0, scaleY, medianArrow.getXOffset(), 1.0));
        }
    }

    APText tempRatioLabel(double oddsRatio) {
        double frontH = summary.selectedFrontH();
        double offset = -art().lineH / 2.0 / frontH;
        double scaleY = PERCENT_LABEL_SCALE / frontH;
        double y = 1.0 - Rank.warp(oddsRatio);
        percentLabels[1].setTransform(Util.scaleNtranslate(PERCENT_LABEL_SCALE, scaleY, percentLabels[1].getXOffset(), y + offset));
        percentLabels[1].setText(formatOddsRatio(oddsRatio));
        percentLabels[1].setVisible(true);
        return percentLabels[1];
    }

    void hotZone(double y) {
        double effectiveY = 1.0 - y;
        double oddsRatio = Rank.unwarp(effectiveY);
        double frontH = summary.selectedFrontH();
        double offset = -art().lineH / 2.0 / frontH;
        double scaleY = PERCENT_LABEL_SCALE / frontH;
        percentLabels[1].setTransform(Util.scaleNtranslate(PERCENT_LABEL_SCALE, scaleY, percentLabels[1].getXOffset(), y + offset));
        percentLabels[1].setText(formatOddsRatio(oddsRatio));
        hotLine.setVisible(true);
        hotLine.moveToFront();
        hotLine.setScale(1 / frontH);
        hotLine.setBounds(0, (int) (y * frontH), (int) ((percentLabelHotZone.getWidth() + w) * frontH), 1);
        hotZonePopup.setVisible(true);
        String msg = (oddsRatio > 0.666666666) ? Math.round(oddsRatio) + " times as likely as others" : "1 / " + Math.round(1.0 / oddsRatio) + " as likely as others";
        hotZonePopup.setText(msg);
        hotZonePopup.moveToFront();
    }

    static String formatOddsRatio(double ratio) {
        if (ratio == Double.POSITIVE_INFINITY) return "* Infinity";
        int iRatio = (int) Math.round(ratio > 1.0 ? ratio : 1.0 / ratio);
        String result = iRatio == 1 ? "=" : (ratio > 1.0 ? "* " : "/ ") + iRatio;
        return result;
    }

    void loseHotZone() {
        double frontH = summary.selectedFrontH();
        double offset = -art().lineH / 2.0 / frontH;
        double scaleY = PERCENT_LABEL_SCALE / frontH;
        percentLabels[1].setTransform(Util.scaleNtranslate(PERCENT_LABEL_SCALE, scaleY, percentLabels[1].getXOffset(), 0.5 + offset));
        percentLabels[1].setText(formatOddsRatio(1.0));
        hotLine.setVisible(false);
        hotZonePopup.setVisible(false);
    }

    @Override
    public void layoutChildren() {
        if (logicalWidth > 0) {
            if (!rank.componentHeights.equals(prevComponentHeights)) {
                prevComponentHeights = rank.componentHeights;
                prevH = h;
                double foldH = rank.foldH();
                front.layout(foldH, rank.frontH());
                scaleMedianArrow();
                double yScale = rank.labelsH();
                if (yScale > 0) yScale /= summary.selectedLabelH();
                labels.layout(foldH + rank.frontH(), yScale);
                layoutLetters();
                layoutRankLabel(false);
                layoutPercentLabels();
                if (!areLabelsInited()) drawLabels();
                layoutLightBeam();
                PerspectiveViz[] children = getChildPVs();
                if (children != null) {
                    for (int i = 0; i < children.length; i++) {
                        children[i].layoutLightBeam();
                    }
                }
            }
        }
    }

    void layoutLetters() {
        if (letters != null) {
            double foldH = rank.foldH();
            letters.setOffset(0, foldH);
            letters.setHeight(foldH);
            letters.setY(-foldH);
            letters.setVisible(labels.getVisible());
        }
    }

    void layoutRankLabel(boolean contentChanged) {
        if (rankLabel.getVisible() && rank.frontH() > 0) {
            double xScale = 1.0;
            double yScale = 1.0 / rank.frontH();
            double labelH = rank.frontH() + rank.labelsH();
            double lineH = art().lineH;
            if (labelH < lineH) {
                xScale = labelH / lineH;
                yScale *= xScale;
                labelH = lineH;
            } else labelH = lineH * ((int) (labelH / lineH));
            boolean boundsChanged = rankLabel.setBounds(0, 0, Math.floor(rank.rankLabelWdith() / xScale), labelH);
            rankLabel.setTransform(Util.scaleNtranslate(xScale, yScale, rankLabel.getXOffset(), 0.0));
            if (contentChanged || boundsChanged) {
                rankLabel.setHeight(labelH);
                rankLabel.setWrapOnWordBoundaries(labelH > lineH);
                rankLabel.layoutBestFit();
                rank.hackRankLabelNotifier();
            }
        }
    }

    /**
	 * [top, bottom, topLeft, topRight, bottomLeft, bottomRight]
	 */
    private float[] prevLightBeamCoords = null;

    /**
	 * Draw the light beam shining from parentPV to this pv.
	 * 
	 * The shape has to change shape during animation, so is called in
	 * layoutChildren rather than draw.
	 */
    void layoutLightBeam() {
        if (parentPV != null) {
            Bar parentFrontRect = parentPV.lookupBar(p);
            if (parentFrontRect != null) {
                PBounds gBottomRect = front.getGlobalBounds();
                if (gBottomRect.getWidth() > 0) {
                    float[] newCoords = new float[6];
                    Rectangle2D lBottomRect = rank.globalToLocal(gBottomRect);
                    newCoords[1] = (float) (lBottomRect.getY());
                    newCoords[4] = (float) lBottomRect.getX();
                    newCoords[5] = newCoords[4] + (float) lBottomRect.getWidth();
                    PBounds gTopRect = parentFrontRect.getGlobalBounds();
                    Rectangle2D lTopRect = rank.globalToLocal(gTopRect);
                    newCoords[0] = (float) (lTopRect.getY() + lTopRect.getHeight());
                    newCoords[2] = (float) lTopRect.getX();
                    newCoords[3] = newCoords[2] + (float) lTopRect.getWidth();
                    if (!Arrays.equals(newCoords, prevLightBeamCoords)) {
                        prevLightBeamCoords = newCoords;
                        if (lightBeam == null) {
                            lightBeam = new LazyPPath();
                            lightBeam.setPickable(false);
                            lightBeam.setStroke(null);
                            updateLightBeamTransparency();
                            rank.addChild(lightBeam);
                        } else {
                            lightBeam.reset();
                        }
                        float[] Xs = { newCoords[4], newCoords[2], newCoords[3], newCoords[5], newCoords[4] };
                        float[] Ys = { newCoords[1], newCoords[0], newCoords[0], newCoords[1], newCoords[1] };
                        lightBeam.setPathToPolyline(Xs, Ys);
                    }
                }
            } else if (lightBeam != null) {
                lightBeam.removeFromParent();
                lightBeam = null;
            }
        }
    }

    PerspectiveViz[] getChildPVs() {
        return summary.getChildPVs(this);
    }

    static final boolean VERIFY_BAR_TABLES = true;

    boolean verifyBarTables() {
        if (VERIFY_BAR_TABLES) {
            Perspective prevFacet = barXs[0];
            assert prevFacet != null : printBarXs();
            boolean lastEmpty = false;
            for (int i = 1; i < barXs.length; i++) {
                if (barXs[i] == null) lastEmpty = true; else if (barXs[i] == prevFacet) lastEmpty = false; else {
                    assert lastEmpty == false : i + printBarXs();
                    prevFacet = barXs[i];
                }
            }
            assert lastEmpty == false : printBarXs();
        }
        return true;
    }

    String printBarXs() {
        Util.print(p + " has " + barTable.size() + " bars over " + barXs.length + " pixels.");
        Util.printDeep(barXs);
        ItemPredicate prevFacet = barXs[0];
        int facetStart = 0;
        for (int i = 1; i < barXs.length; i++) {
            Perspective facet = barXs[i];
            if (facet != null) {
                if (facet == prevFacet) i++; else if (facetStart != i - 1) Util.print("\nunterminated:");
                Util.print(prevFacet + ": " + facet.getTotalCount() + " " + facet.cumCountExclusive() + "-" + facet.cumCountInclusive() + " [" + facetStart + ", " + (i - 1) + "]");
                prevFacet = facet;
                facetStart = i;
            }
        }
        return "";
    }

    private void setTextSize() {
        int projectionW = (int) (1.1 * art().lineH);
        if (projectionW != labelHprojectionW) {
            labelHprojectionW = projectionW;
            if (percentLabels != null) {
                double percentLabelW = art().getStringWidth("/ 100+");
                double percentLabelScaledW = percentLabelW * PERCENT_LABEL_SCALE;
                double x = Math.round(-percentLabelScaledW);
                for (int i = 0; i < 3; i++) {
                    percentLabels[i].setWidth(percentLabelW);
                    percentLabels[i].setXoffset(x);
                }
                percentLabelScaledW = Math.round(percentLabelScaledW * 0.67);
                percentLabelHotZone.setBounds(-percentLabelScaledW, 0, percentLabelScaledW, 1);
                hotZonePopup.setOffset(percentLabelHotZone.getX(), summary.selectedFoldH() - 1.5 * hotZonePopup.getHeight());
            }
        }
    }

    double percentLabelW() {
        if (percentLabels == null) return 0; else return percentLabels[0].getWidth() * percentLabels[0].getScale();
    }

    private void drawBars() {
        assert p.getTotalChildTotalCount() > 0 : p;
        front.removeAllChildren();
        front.addChild(rankLabel);
        setTextSize();
        if (percentLabels != null) {
            for (int i = 0; i < 3; i++) {
                percentLabels[i].setFont(art().font);
                front.addChild(percentLabels[i]);
            }
            front.addChild(percentLabelHotZone);
            front.addChild(hotLine);
        }
        if (medianArrow != null && art().getShowMedian()) {
            front.addChild(medianArrow);
        }
        computeBars();
    }

    int maybeDrawBar(Perspective facet, double divisor, boolean forceDraw) {
        assert facet.getParent() == p;
        int barW = 0;
        if (visibleWidth() > 0 && facet.getTotalCount() > 0 && (!forceDraw || lookupBar(facet) == null)) {
            assert p.getTotalChildTotalCount() >= facet.getTotalCount() : facet + " " + facet.getTotalCount() + "/" + p.getTotalChildTotalCount() + " " + query().isQueryValid();
            double minX = Math.max(leftEdge, facet.cumCountExclusive() * divisor) - leftEdge;
            double maxX = Math.min(visibleWidth(), facet.cumCountInclusive() * divisor - leftEdge);
            assert minX >= 0 && maxX < w && maxX >= minX : "shouldn't try to draw this bar " + facet + " " + minX + "-" + maxX + " " + leftEdge + " " + w + "/" + logicalWidth;
            int iMaxX = (int) maxX;
            int iMinX = (int) minX;
            assert facet.cumCountInclusive() <= p.getTotalChildTotalCount() : facet + " " + facet.cumCountInclusive() + "/" + p.getTotalChildTotalCount();
            assert iMaxX < logicalWidth : minX + " " + maxX + " " + logicalWidth;
            assert iMinX >= 0 : facet + " " + facet.cumCountInclusive() + " " + facet.getTotalCount();
            if (barXs[iMinX] != null && barXs[iMinX] != facet) {
                iMinX += 1;
                assert iMinX >= iMaxX || barXs[iMinX] == null || barXs[iMinX] == facet : this + " " + facet + " " + printBarXs() + " " + minX + "-" + maxX;
            }
            if (barXs[iMaxX] != null && barXs[iMaxX] != facet) {
                iMaxX -= 1;
                assert ((int) minX) >= iMaxX || barXs[iMaxX] == null || barXs[iMaxX] == facet : this + " " + facet + " " + printBarXs();
            }
            if (iMinX > iMaxX && forceDraw) {
                if (iMinX > 1 && (barXs[iMinX - 2] == null || barXs[iMinX - 2] == barXs[iMinX - 1])) {
                    barXs[iMinX - 2] = barXs[iMinX - 1];
                    iMinX -= 1;
                    iMaxX = iMinX;
                } else if (iMaxX < barXs.length - 2 && (barXs[iMaxX + 2] == null || barXs[iMaxX + 2] == barXs[iMaxX + 1])) {
                    barXs[iMaxX + 2] = barXs[iMaxX + 1];
                    iMaxX += 1;
                    iMinX = iMaxX;
                } else if (forceDraw) {
                    iMinX = (iMinX > 1) ? iMinX - 1 : iMaxX + 1;
                    iMaxX = iMinX;
                    Perspective oldFacet = barXs[iMinX];
                    Bar oldBar = barTable.get(oldFacet);
                    assert oldBar != null : p + " " + facet + " " + oldFacet + " " + iMinX + "-" + iMaxX + " " + visibleWidth() + printBarXs();
                    Bar.release(oldBar);
                    barTable.remove(oldFacet);
                    barXs[iMinX] = facet;
                }
            }
            if (iMinX <= iMaxX) {
                barW = iMaxX - iMinX + 1;
                Bar bar = Bar.getBar(this, iMinX, barW, facet);
                front.addChild(bar);
                if (query().isQueryValid()) {
                    double expectedPercentOn = rank.expectedPercentOn();
                    if (expectedPercentOn >= 0 && facet.getOnCount() >= 0) {
                        bar.updateData();
                        if (true || !forceDraw) bar.animateData(1.0);
                    }
                }
                barTable.put(facet, bar);
                assert validateBarX(facet, iMinX);
                assert validateBarX(facet, iMaxX);
            }
        }
        return barW;
    }

    double barWidthRatio() {
        return logicalWidth / p.getTotalChildTotalCount();
    }

    Bar lookupBar(Perspective facet) {
        Bar result = barTable.get(facet);
        return result;
    }

    int nBars() {
        return barTable.size();
    }

    double[] pValues() {
        double[] result = new double[nBars()];
        int i = 0;
        for (Iterator<Perspective> it = barTable.keySet().iterator(); it.hasNext(); i++) {
            ItemPredicate facet = it.next();
            result[i] = facet.pValue();
        }
        return result;
    }

    private boolean validateBarX(Perspective datum, int xCoord) {
        double divisor = barWidthRatio();
        double maxX = datum.cumCountInclusive() * divisor;
        double minX = maxX - datum.getTotalCount() * divisor;
        int iMaxX = (int) (maxX - leftEdge);
        int iMinX = (int) (minX - leftEdge);
        assert xCoord >= iMinX && xCoord <= iMaxX : xCoord + " [" + iMinX + ", " + iMaxX + "]";
        return true;
    }

    private FacetPText mouseNameLabel;

    void updateSelection(Set<Perspective> facets) {
        updateLightBeamTransparency();
        drawMouseLabel();
        for (Iterator<Perspective> it = facets.iterator(); it.hasNext(); ) {
            Perspective facet = it.next();
            Bar bar = lookupBar(facet);
            if (bar != null) {
                bar.updateSelection();
            }
        }
        if (labels != null && labels.getVisible()) {
            for (int i = 0; i < labels.getChildrenCount(); i++) {
                FacetPText label = (FacetPText) labels.getChild(i);
                Perspective labelFacet = label.getFacet();
                if (labelFacet != null && facets.contains(labelFacet)) label.setColor();
            }
        }
    }

    void updateLightBeamTransparency() {
        if (lightBeam != null) {
            lightBeam.setPaint(p.isRestriction(true) ? Markup.INCLUDED_COLORS[0] : Color.white);
            if (art().highlightedFacets.contains(p)) lightBeam.setTransparency(0.2f); else lightBeam.setTransparency(0.15f);
        }
    }

    /**
	 * y coordinate for numeric labels; depends on maxCount.
	 */
    double numW;

    double nameW;

    private void drawLabels() {
        assert SwingUtilities.isEventDispatchThread();
        labels.removeAllChildren();
        if (labels.getVisible() && barTable.size() > 0 && query().isQueryValid()) {
            if (p.isPrefetched()) {
                int maxCount = rank.maxCount();
                numW = Math.round(art().numWidth(maxCount) + 10.0);
                nameW = Math.floor(summary.selectedLabelH() * 1.4 - numW - 2 * art().lineH);
                computeLabels();
                mouseNameLabel = new FacetPText(null, 0.0, -1.0);
                mouseNameLabel.setVisible(false);
                mouseNameLabel.setPickable(false);
                labels.addChild(mouseNameLabel);
            } else {
                queuePrefetch();
            }
            drawMouseLabel();
        }
    }

    private boolean areLabelsInited() {
        return labels.getChildrenCount() > 0;
    }

    private int prevMidX;

    private void drawMouseLabel() {
        if (areLabelsInited() && labels.getVisible()) {
            Perspective mousedFacet = null;
            for (Iterator<Perspective> it = art().highlightedFacets.iterator(); it.hasNext() && mousedFacet == null; ) {
                Perspective facet = it.next();
                if (facet.getParent() == p && isPerspectiveVisible(facet)) {
                    mousedFacet = facet;
                }
            }
            drawMouseLabelInternal(mousedFacet);
        }
    }

    private void drawMouseLabelInternal(Perspective v) {
        boolean state = v != null;
        int midX = state ? midLabelPixel(v, barWidthRatio()) : prevMidX;
        int iVisibleWidth = (int) visibleWidth();
        if (midX >= 0 && midX <= iVisibleWidth) {
            prevMidX = midX;
            int minX = Util.constrain(midX - labelHprojectionW, 0, iVisibleWidth);
            int maxX = Util.constrain(midX + labelHprojectionW, 0, iVisibleWidth);
            for (int i = minX; i <= maxX; i++) {
                FacetText label = findLabel(labelXs[i]);
                if (label != null) {
                    int midLabel = midLabelPixel(labelXs[i], barWidthRatio());
                    if (Math.abs(midLabel - midX) <= labelHprojectionW) {
                        if (labelXs[i] == v) {
                            label.setVisible(true);
                            mouseNameLabel.setVisible(false);
                            mouseNameLabel.setPickable(false);
                            return;
                        } else label.setVisible(v == null);
                    }
                }
            }
            if (state) {
                maybeDrawBar(v, barWidthRatio(), true);
                assert verifyBarTables();
                mouseNameLabel.setFacet(v);
                mouseNameLabel.setPTextOffset(midX, 0.0);
                labels.moveAheadOf(front);
            } else {
                front.moveAheadOf(labels);
            }
            mouseNameLabel.setVisible(state);
            mouseNameLabel.setPickable(state);
        }
    }

    FacetText findLabel(Perspective facet) {
        if (facet != null) {
            for (int i = 0; i < labels.getChildrenCount(); i++) {
                FacetText child = (FacetText) labels.getChild(i);
                if (child.facet == facet) return child;
            }
        }
        return null;
    }

    private int maybeDrawLabel(Perspective v) {
        int nPixelsOccluded = 0;
        if (v.getTotalCount() > 0) {
            int iVisibleWidth = (int) visibleWidth();
            int midX = midLabelPixel(v, barWidthRatio());
            assert midX >= 0 && midX < iVisibleWidth : v + " " + midX + " " + iVisibleWidth;
            if (labelXs[midX] == null || labelXs[midX] == v) {
                maybeDrawBar(v, barWidthRatio(), true);
                assert verifyBarTables();
                if (lookupBar(v) != null) {
                    FacetPText label = getFacetPText(v, 0.0, midX);
                    labels.addChild(label);
                    int minX = Util.constrain(midX - labelHprojectionW, 0, iVisibleWidth);
                    int maxX = Util.constrain(midX + labelHprojectionW, 0, iVisibleWidth);
                    for (int i = minX; i <= maxX; i++) {
                        if (labelXs[i] == null) nPixelsOccluded++;
                        labelXs[i] = v;
                    }
                }
            }
        }
        return nPixelsOccluded;
    }

    boolean isConnected() {
        return rank.isConnected();
    }

    void connectToPerspective() {
        rank.connect();
    }

    void setConnected(boolean connected) {
        if (rankLabel.getVisible()) {
            float transparency = connected ? 1 : 0;
            for (int i = 0; i < 3; i++) {
                percentLabels[i].animateToTransparency(transparency, Bungee.dataAnimationMS);
            }
            percentLabelHotZone.setPickable(connected);
            rankLabel.setPickable(connected);
            resetLogicalBounds();
        }
    }

    void highlightFacet(Perspective facet, int modifiers, PInputEvent e) {
        if (art().getIsEditing() && e.isRightMouseButton()) {
            art().setClickDesc("Set selected for edit");
        } else if (art().getIsEditing() && e.isMiddleMouseButton()) {
            art().setClickDesc("Open edit menu");
        } else if (isConnected() || facet == null) {
            art().setClickDesc(facet != null ? facet.facetDoc(modifiers) : null);
        } else {
            highlight(facet, modifiers);
        }
        art().highlightFacet(facet, modifiers);
    }

    public boolean pick(FacetText node, int modifiers) {
        return pickFacet(node.getFacet(), modifiers);
    }

    boolean pickFacet(Perspective facet, int modifiers) {
        boolean handle = isHandlePickFacetText(facet, modifiers);
        if (handle) {
            art().printUserAction(Bungee.RANK_LABEL, facet, modifiers);
            if (!isConnected()) {
                connectToPerspective();
            } else {
                if (art().arrowFocus != null && art().arrowFocus.getParent() == p) facet = art().arrowFocus; else if (p.nRestrictions() > 0) facet = p.allRestrictions().first(); else facet = p.getNthChild(0);
                summary.togglePerspectiveList(facet);
            }
        }
        return handle;
    }

    public boolean highlight(FacetText node, boolean state, int modifiers) {
        Perspective facet = state ? node.getFacet() : null;
        return highlight(facet, modifiers);
    }

    boolean isHandlePickFacetText(Perspective facet, int modifiers) {
        return facet != null && (!Util.isAnyShiftKeyDown(modifiers) && facet == p || !isConnected());
    }

    boolean highlight(Perspective facet, int modifiers) {
        boolean handle = isHandlePickFacetText(facet, modifiers);
        if (handle) {
            art().highlightFacet(facet, modifiers);
            Markup doc = Query.emptyMarkup();
            if (!isConnected()) {
                doc.add("open category ");
                doc.add(p);
            } else if (art().getShowTagLists()) {
                if (summary.perspectiveList == null || summary.perspectiveList.isHidden()) {
                    doc.add("List all ");
                    doc.add(p);
                    doc.add(" tags");
                } else doc.add("Hide the list of tags");
            } else handle = false;
            if (handle) art().setClickDesc(doc);
        }
        return handle;
    }

    double frontBottomOffset() {
        return front.getYOffset() + Math.min(Math.round(front.getFullBounds().getHeight()), rank.getHeight());
    }

    void hidePvTransients() {
        summary.mayHideTransients();
    }

    Hashtable<Perspective, FacetPText> facetPTexts = new Hashtable<Perspective, FacetPText>();

    FacetPText getFacetPText(Perspective _facet, double _y, double x) {
        FacetPText label = null;
        if (_facet != null) label = facetPTexts.get(_facet);
        if (label == null || label.numW != numW || label.nameW != nameW) {
            label = new FacetPText(_facet, _y, x);
            if (_facet != null) facetPTexts.put(_facet, label);
        } else {
            label.setPTextOffset(x, _y);
            ((APText) label).setText(label.art.facetLabel(_facet, numW, nameW, false, true, label.showCheckBox, true, label));
            label.setColor();
        }
        return label;
    }

    final class FacetPText extends FacetText {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        void setFacet(Perspective _facet) {
            facet = _facet;
            assert facet.getParent() != null;
            ((APText) this).setText(art.facetLabel(facet, numW, nameW, false, showChildIndicator, showCheckBox, true, this));
        }

        void setPTextOffset(double x, double y) {
            setColor();
            double offset = getWidth() / 1.4;
            x = Math.round(x - offset + (y + art.lineH) / 1.4 - 0.85 * art.lineH);
            double _y = Math.round(y / 1.4 + offset);
            setOffset(x, _y);
        }

        FacetPText(Perspective _facet, double _y, double x) {
            super(summary.art, PerspectiveViz.this.numW, PerspectiveViz.this.nameW);
            setRotation(-Math.PI / 4.0);
            showCheckBox = art.getShowCheckboxes();
            showChildIndicator = true;
            setUnderline(true);
            if (_facet != null) {
                setFacet(_facet);
                setPTextOffset(x, _y);
            }
        }

        @Override
        public boolean pick(int modifiers, PInputEvent e) {
            if (isConnected()) {
                super.pick(modifiers, e);
            } else connectToPerspective();
            return true;
        }

        @Override
        void printUserAction(int modifiers) {
            art.printUserAction(Bungee.BAR_LABEL, facet, modifiers);
        }

        @Override
        public boolean highlight(boolean state, int modifiers, PInputEvent e) {
            Point2D mouseCoords = e.getPositionRelativeTo(this);
            double x = mouseCoords.getX();
            double _y = mouseCoords.getY();
            modifiers = getModifiersInternal(modifiers, x);
            boolean isHighlighted = art.highlightedFacets.contains(facet);
            if (state) {
                if (!isHighlighted) {
                    if (x >= -5.0 && x <= w + 5 && _y >= -5.0 && _y <= getHeight() + 5.0) {
                        highlightInternal(state, modifiers);
                    }
                }
            } else if (isHighlighted) {
                highlightInternal(state, modifiers);
            }
            return true;
        }
    }

    /**
	 * Only called by replayOps
	 */
    void clickBar(Perspective facet, int modifiers) {
        maybeDrawBar(facet, barWidthRatio(), true);
        Bar bar = lookupBar(facet);
        assert bar != null : facet + " " + facet.getTotalCount() + " " + p + " " + p.getTotalChildTotalCount() + " " + visibleWidth() + " " + logicalWidth;
        bar.pick(modifiers, null);
    }

    @Override
    public String toString() {
        return "<PerspectiveViz " + p + ">";
    }

    void restrict() {
        drawBars();
        drawLabels();
    }

    public FacetNode startDrag(PNode ignore, Point2D local) {
        assert Util.ignore(ignore);
        dragStartOffset = local.getX();
        return this;
    }

    private double dragStartOffset;

    private Letters letters;

    public void drag(Point2D ignore, PDimension delta) {
        assert Util.ignore(ignore);
        double vertical = delta.getHeight();
        double horizontal = delta.getWidth();
        if (Math.abs(vertical) > Math.abs(horizontal)) horizontal = 0; else vertical = 0;
        double deltaZoom = Math.pow(2, -vertical / 20.0);
        double newLogicalWidth = logicalWidth * deltaZoom;
        double newLeftEdge = 0;
        if (newLogicalWidth < visibleWidth()) {
            newLogicalWidth = visibleWidth();
        } else {
            deltaZoom = newLogicalWidth / logicalWidth;
            double pan = -horizontal;
            newLeftEdge = Util.constrain(leftEdge + pan + (leftEdge + dragStartOffset) * (deltaZoom - 1), 0, newLogicalWidth - visibleWidth());
            assert newLeftEdge >= 0 && newLeftEdge + visibleWidth() <= newLogicalWidth : newLeftEdge + "/" + newLogicalWidth + " " + visibleWidth();
        }
        setLogicalBounds(newLeftEdge, newLogicalWidth);
    }

    final class MedianArrow extends Arrow {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        final MedianArrowHandler medianArrowHandler = new MedianArrowHandler();

        ItemPredicate unconditionalMedian;

        ItemPredicate conditionalMedian;

        int significant = 0;

        int highlighted = 1;

        MedianArrow(Paint color, int size, int length) {
            super(color, size, length);
            setPickable(false);
            addInputEventListener(medianArrowHandler);
        }

        void updateColor(int _significant) {
            significant = _significant;
            redraw();
        }

        void redraw() {
            Color color = Bungee.significanceColor(significant, highlighted);
            setStrokePaint(color);
        }

        void highlight(boolean state) {
            highlighted = state ? 1 : 0;
            redraw();
        }

        final class MedianArrowHandler extends MyInputEventHandler {

            MedianArrowHandler() {
                super(PPath.class);
            }

            @Override
            public boolean exit(PNode node) {
                art().showMedianArrowDesc(null);
                highlight(false);
                return true;
            }

            @Override
            public boolean enter(PNode ignore) {
                art().showMedianArrowDesc(p);
                highlight(true);
                return true;
            }
        }
    }

    public Bungee art() {
        return summary.art;
    }

    Query query() {
        return summary.art.query;
    }

    LazyPNode anchorForPopup(Perspective facet) {
        LazyPNode bar = null;
        if (facet == p) {
            bar = rankLabel;
        } else if (p.isPrefetched() && isVisible(facet)) {
            if (areLabelsInited()) drawMouseLabelInternal(facet); else if (barTable.size() > 0) maybeDrawBar(facet, barWidthRatio(), true);
            bar = lookupBar(facet);
        }
        return bar;
    }

    void prepareAnimation() {
        if (medianArrow != null) {
            medianArrow.setVisible(false);
        }
    }

    void animate(float zeroToOne) {
        if (zeroToOne == 1) {
            if (medianArrow != null) {
                medianArrow.setVisible(true);
            }
        }
    }

    /**
	 * We want to draw bars greedily by totalCount. However there may be 100,000
	 * children, of which only 100 or so might be drawn. barXs keeps the child
	 * with the highest totalCount that maps into each pixel.
	 * 
	 */
    private void computeBars() {
        Bar.release(barTable.values());
        barTable.clear();
        int iVisibleWidth = (int) visibleWidth();
        assert iVisibleWidth <= logicalWidth : iVisibleWidth + "/" + logicalWidth;
        if (iVisibleWidth > 0) {
            barXs = new Perspective[iVisibleWidth + 1];
            double divisor = barWidthRatio();
            for (Iterator<Perspective> it = slowVisibleChildIterator(); it.hasNext(); ) {
                Perspective child = it.next();
                int totalCount = child.getTotalCount();
                if (totalCount > 0) {
                    int iMaxX = maxBarPixel(child, divisor);
                    int iMinX = minBarPixel(child, divisor);
                    if (iMaxX >= 0 && iMinX <= iVisibleWidth) {
                        assert iMaxX >= 0 && iMinX <= iVisibleWidth && iMaxX >= iMinX && child.cumCountInclusive() <= p.getTotalChildTotalCount() : printBar(child, iMinX, iMaxX, "bar");
                        if (totalCount > itemTotalCount(iMinX)) barXs[iMinX] = child;
                        if (iMaxX > iMinX) {
                            barXs[iMinX + 1] = child;
                            barXs[iMaxX] = child;
                            if (iMaxX > iMinX + 1) barXs[iMaxX - 1] = child;
                        }
                    }
                }
            }
            assert verifyBarTables();
            drawComputedBars();
            drawLetters();
        }
    }

    void drawLetters() {
        if (art().getShowZoomLetters() && barXs != null) {
            if (letters == null) {
                letters = new Letters();
                layoutLetters();
                addChild(letters);
            } else {
                letters.redraw();
            }
        }
    }

    private void drawComputedBars() {
        int iVisibleWidth = (int) visibleWidth();
        double divisor = barWidthRatio();
        for (int x = 0; x <= iVisibleWidth; ) {
            Perspective toDraw = barXs[x];
            assert toDraw != null : x + "/" + barXs.length + " " + Util.valueOfDeep(barXs);
            int pixels = maybeDrawBar(toDraw, divisor, false);
            assert pixels > 0;
            x += pixels;
        }
    }

    private void computeLabels() {
        int iVisibleWidth = (int) visibleWidth();
        labelXs = new Perspective[iVisibleWidth + 1];
        double divisor = barWidthRatio();
        Perspective[] restrictions = p.allRestrictions().toArray(new Perspective[0]);
        int restrictionBonus = query().getTotalCount();
        boolean isQueryRestricted = query().isRestricted();
        for (Iterator<Perspective> it = visibleChildIterator(); it.hasNext(); ) {
            Perspective child = it.next();
            int totalCount = child.getTotalCount();
            if (totalCount > 0) {
                int iMaxX = maxBarPixel(child, divisor);
                int iMinX = minBarPixel(child, divisor);
                assert iMaxX >= 0 && iMinX <= iVisibleWidth : printBar(child, iMinX, iMaxX, "label");
                int iMidX = (iMaxX + iMinX) / 2;
                if (priorityCount(child, restrictions, isQueryRestricted, restrictionBonus) > itemOnCount(iMidX, restrictions, isQueryRestricted, restrictionBonus)) labelXs[iMidX] = child;
            }
        }
        drawComputedLabel(null, divisor, restrictions, isQueryRestricted, restrictionBonus);
    }

    /**
	 * March along pixels, finding the child Perspectives to draw. At each
	 * pixel, you draw the child with the highest count at that pixel, which was
	 * computed above, unless another child with a higher count has a label that
	 * would occlude it, unless IT would be occluded. So you get a recusive
	 * test, where a conflict on the rightmost label can propagate all the way
	 * back to the left. At each call, you know there are no conflicts with
	 * leftCandidate from the left. You look for a conflict on the right (or
	 * failing that, the next non-conflict on the right) and recurse on that to
	 * get the next labeled Perspective to the right. You draw leftCandidate iff
	 * it doesn't conflict with that next label.
	 */
    Perspective drawComputedLabel(Perspective leftCandidate, double divisor, Perspective[] restrictions, boolean isQueryRestricted, int restrictionBonus) {
        assert query().isQueryValid();
        Perspective result = null;
        int x1 = -1;
        int x0 = -1;
        int threshold = -1;
        if (leftCandidate != null) {
            x0 = midLabelPixel(leftCandidate, divisor);
            threshold = priorityCount(leftCandidate, restrictions, isQueryRestricted, restrictionBonus);
            x1 = x0 + labelHprojectionW;
        }
        int iVisibleWidth = (int) visibleWidth();
        for (int x = x0 + 1; x < iVisibleWidth && result == null; x++) {
            if (x > x1) threshold = -1;
            Perspective rightCandidate = labelXs[x];
            if (rightCandidate != null && priorityCount(rightCandidate, restrictions, isQueryRestricted, restrictionBonus) > threshold) {
                Perspective nextDrawn = drawComputedLabel(rightCandidate, divisor, restrictions, isQueryRestricted, restrictionBonus);
                if (nextDrawn != null && midLabelPixel(nextDrawn, divisor) <= midLabelPixel(rightCandidate, divisor) + labelHprojectionW) {
                    result = nextDrawn;
                } else {
                    result = rightCandidate;
                    maybeDrawLabel(result);
                }
            }
        }
        return result;
    }

    private String printBar(Perspective child, int iMinX, int iMaxX, String what) {
        Util.print("Adding " + what + " for " + child + ": totalCount=" + child.getTotalCount() + " range=" + child.cumCountExclusive() + "-" + child.cumCountInclusive() + "/" + p.getTotalChildTotalCount() + " iMinX-iMaxX=" + iMinX + "-" + iMaxX + " left/logical=" + leftEdge + "/" + logicalWidth);
        return "";
    }

    private int itemTotalCount(int x) {
        if (barXs[x] == null) return 0; else return barXs[x].getTotalCount();
    }

    /**
	 * @param child
	 * @return give filters priority over other children
	 */
    private int priorityCount(Perspective child, Perspective[] restrictions, boolean isQueryRestricted, int restrictionBonus) {
        int result = child.getOnCount(isQueryRestricted);
        if (Util.isMember(restrictions, child)) result += restrictionBonus;
        return result;
    }

    private int itemOnCount(int x, Perspective[] restrictions, boolean isQueryRestricted, int restrictionBonus) {
        if (labelXs[x] == null) return -1; else return priorityCount(labelXs[x], restrictions, isQueryRestricted, restrictionBonus);
    }

    int maxBarPixel(Perspective child) {
        return maxBarPixel(child, barWidthRatio());
    }

    /**
	 * @param child
	 * @param divisor
	 * @return x offset relative to leftEdge. return w-1 if offset would be off
	 *         the screen.
	 */
    int maxBarPixel(Perspective child, double divisor) {
        double dPixel = child.cumCountInclusive() * divisor - leftEdge;
        return (int) Math.min(visibleWidth(), dPixel);
    }

    int minBarPixelRaw(Perspective child) {
        return (int) (child.cumCountExclusive() * barWidthRatio() - leftEdge);
    }

    int maxBarPixelRaw(Perspective child) {
        return (int) (child.cumCountInclusive() * barWidthRatio() - leftEdge);
    }

    /**
	 * @param child
	 * @param divisor
	 * @return x offset relative to left edge. return 0 if offset would be
	 *         negative.
	 */
    int minBarPixel(Perspective child, double divisor) {
        double dPixel = child.cumCountExclusive() * divisor - leftEdge;
        return Math.max(0, (int) dPixel);
    }

    /**
	 * @param child
	 * @param divisor
	 * @return x offset of middle of label relative to leftEdge
	 */
    private int midLabelPixel(Perspective child, double divisor) {
        return (minBarPixel(child, divisor) + maxBarPixel(child, divisor)) / 2;
    }

    boolean isVisible(Perspective child) {
        double divisor = barWidthRatio();
        double minPixel = (child.cumCountExclusive() + 0.5) * divisor - leftEdge;
        double maxPixel = (child.cumCountInclusive() - 0.5) * divisor - leftEdge;
        return minPixel < visibleWidth() && maxPixel > 0;
    }

    private class Letters extends LazyPNode implements FacetNode, PickFacetTextNotifier, PerspectiveObserver {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        Letters() {
            addInputEventListener(Bungee.facetClickHandler);
            redraw();
        }

        public boolean isAlphabetic() {
            return p.isAlphabetic() && art().getShowZoomLetters();
        }

        public void redraw() {
            CollationKey prefix = prefix();
            if (prefix != null) {
                int iVisibleWidth = (int) visibleWidth();
                setWidth(PerspectiveViz.this.getWidth());
                if (p.isAlphabetic()) {
                    if (barTable.size() == 1) {
                        removeAllChildren();
                        return;
                    }
                    double divisor = barWidthRatio();
                    int[] counts = new int[iVisibleWidth + 1];
                    char[] letterXs = new char[iVisibleWidth + 1];
                    Perspective firstWithLetter = firstVisiblePerspective();
                    Perspective lastVisiblePerspective = lastVisiblePerspective();
                    for (Iterator<Entry<CollationKey, Perspective[]>> it = p.letterOffsetsIterator(prefix); it.hasNext(); ) {
                        Entry<CollationKey, Perspective[]> entry = it.next();
                        CollationKey letter = entry.getKey();
                        Perspective lastWithLetter = entry.getValue()[1];
                        if (lastWithLetter.compareTo(firstWithLetter) >= 0 && firstWithLetter.compareTo(lastVisiblePerspective) <= 0) {
                            int iMidX = (minBarPixel(firstWithLetter, divisor) + maxBarPixel(lastWithLetter, divisor)) / 2;
                            assert iMidX >= 0 : p + " " + prefix + " " + letter + " " + lastWithLetter + " " + minBarPixel(firstWithLetter, divisor) + "-" + maxBarPixel(lastWithLetter, divisor);
                            letterXs[iMidX] = letter.getSourceString().toUpperCase().charAt(0);
                            counts[iMidX] = (firstWithLetter == lastWithLetter) ? firstWithLetter.getTotalCount() : lastWithLetter.cumCountExclusive() - firstWithLetter.cumCountExclusive();
                            firstWithLetter = lastWithLetter.nextSibling();
                            assert firstWithLetter != null || !it.hasNext() : this + " " + lastWithLetter + " " + lastWithLetter.whichChild() + "/" + p.nChildren() + " '" + prefix.getSourceString() + "'\n" + p.letterOffsetsString(prefix);
                        }
                    }
                    removeAllChildren();
                    drawComputedLetter(-1, letterXs, counts, prefix.getSourceString());
                }
            }
        }

        private int drawComputedLetter(int leftCandidateX, char[] letterXs, int[] counts, String prefix) {
            int result = -1;
            int x1 = -1;
            int x0 = -1;
            int threshold = 0;
            if (leftCandidateX >= 0) {
                x0 = leftCandidateX;
                threshold = counts[leftCandidateX];
                x1 = x0 + labelHprojectionW;
            }
            int iVisibleWidth = (int) visibleWidth();
            for (int x = x0 + 1; x < iVisibleWidth && result < 0; x++) {
                if (x > x1) threshold = 0;
                char rightCandidate = letterXs[x];
                if (rightCandidate > 0 && counts[x] > threshold) {
                    int nextDrawnX = drawComputedLetter(x, letterXs, counts, prefix);
                    if (nextDrawnX > 0 && nextDrawnX <= x + labelHprojectionW) {
                        result = nextDrawnX;
                    } else {
                        result = x;
                        maybeDrawLetter(prefix.toLowerCase() + letterXs[result], x);
                    }
                }
            }
            return result;
        }

        private Map<String, FacetPText> letterPTextCache = new Hashtable<String, FacetPText>();

        private void maybeDrawLetter(String s, int midX) {
            int iVisibleWidth = (int) visibleWidth();
            assert midX >= 0 : s;
            assert midX < iVisibleWidth : s + " " + midX + " " + iVisibleWidth;
            FacetPText label = letterPTextCache.get(s);
            if (label == null || label.getFont() != art().font) {
                label = getFacetPText(null, 0.0, midX);
                label.setPermanentTextPaint(Bungee.summaryFG.darker());
                label.setPaint(Color.black);
                label.pickFacetTextNotifier = this;
                label.setText(s);
                letterPTextCache.put(s, label);
            }
            assert label.getParent() == null;
            label.setPTextOffset(midX + label.getHeight() / 2 + label.getWidth() * 1.4, -labelHprojectionW - label.getWidth());
            addChild(label);
        }

        /**
		 * @return longest common prefix of the names of p's children that would
		 *         be displayed if bars were infinitesimally thin, or null if
		 *         some names haven't been cached yet. I.e. consider children
		 *         whose bar would be too small to draw, since letterOffsets
		 *         consider them. Otherwise zooming to a prefix might zoom to a
		 *         longer prefix because only one extension of it has visible
		 *         bars.
		 */
        private CollationKey prefix() {
            Perspective first = firstVisiblePerspective();
            for (Perspective prev = first.previousSibling(); prev != null && isVisible(prev); prev = prev.previousSibling()) {
                first = prev;
            }
            Perspective last = lastVisiblePerspective();
            for (Perspective next = last.nextSibling(); next != null && isVisible(next); next = next.nextSibling()) {
                last = next;
            }
            String name1 = first.getName(this, null);
            String name2 = last.getName(this, null);
            CollationKey result = (name1 != null && name2 != null) ? Util.toCollationKey(Util.commonPrefix(name1, name2)) : null;
            return result;
        }

        boolean keyPress(char suffix) {
            finishPanZoom();
            CollationKey prefix = prefix();
            if (prefix != null && p.isAlphabetic()) {
                String prefixString = prefix.getSourceString();
                if (suffix == '\b') {
                    if (prefixString.length() > 0) {
                        while (prefixString.length() > 0 && p.getLetterOffsets(prefix = Util.toCollationKey(prefixString = prefixString.substring(0, prefixString.length() - 1))).size() == 1) {
                        }
                    } else if (!isZooming()) {
                        art().setTip(p.getName() + " is fully zoomed out already");
                        return false;
                    }
                } else prefix = Util.toCollationKey(prefixString + suffix);
                return zoom(prefix);
            } else return false;
        }

        public boolean pick(FacetText node, int modifiers) {
            return zoom(Util.toCollationKey(node.getText()));
        }

        boolean zoom(final CollationKey s) {
            if (s.getSourceString().length() == 0) {
                animatePanZoom(0, visibleWidth());
            } else {
                Perspective rightFacet = p.lastWithPrefix(s);
                if (rightFacet != null) {
                    Perspective leftFacet = p.firstWithPrefix(s);
                    assert leftFacet != null : p + " " + s;
                    double newLogicalWidth = visibleWidth() * p.getTotalChildTotalCount() / (rightFacet.cumCountInclusive() - leftFacet.cumCountExclusive());
                    double newLeftEdge = leftFacet.cumCountExclusive() * (newLogicalWidth / p.getTotalChildTotalCount());
                    animatePanZoom(newLeftEdge, newLogicalWidth);
                } else {
                    art().setTip("No " + p.getName() + " tags start with '" + s.getSourceString().toUpperCase() + "'");
                }
            }
            return true;
        }

        public boolean highlight(FacetText node, boolean state, int modifiers) {
            String prefix = node.getText();
            int newPrefixLength = prefix.length() - 1;
            char suffix = prefix.charAt(newPrefixLength);
            String msg = null;
            if (state) {
                msg = "zoom into tags starting with '" + prefix + "', as will typing '" + suffix + "'";
                if (newPrefixLength > 0) {
                    String unzoomPrefix;
                    if (newPrefixLength == 1) unzoomPrefix = "any letter."; else unzoomPrefix = "'" + prefix.substring(0, newPrefixLength - 1) + "'.";
                    msg += ";  backspace zooms out to tags starting with " + unzoomPrefix;
                }
            }
            art().setClickDesc(msg);
            return true;
        }

        public boolean highlight(boolean state, int modifiers, PInputEvent e) {
            art().setClickDesc(state ? "Start dragging up/down to zoom; left/right to pan" : null);
            return true;
        }

        public Bungee art() {
            return PerspectiveViz.this.art();
        }

        public Perspective getFacet() {
            return p;
        }

        public void mayHideTransients(PNode node) {
        }

        public boolean pick(int modifiers, PInputEvent e) {
            return false;
        }

        public FacetNode startDrag(PNode node, Point2D positionRelativeTo) {
            return PerspectiveViz.this.startDrag(node, positionRelativeTo);
        }

        public void drag(Point2D position, PDimension delta) {
            assert false;
        }

        @Override
        public void setVisible(boolean state) {
            super.setVisible(state);
            setPickable(state);
            setChildrenPickable(state);
        }

        @Override
        public String toString() {
            return "<Letters " + p + ">";
        }
    }

    Perspective firstVisiblePerspective() {
        assert barXs[0] != null : verifyBarTables();
        return barXs[0];
    }

    Perspective lastVisiblePerspective() {
        int iVisibleWidth = (int) visibleWidth();
        assert barXs[iVisibleWidth] != null : verifyBarTables();
        return barXs[iVisibleWidth];
    }

    /**
	 * This depends on barXs already being computed, so don't call this from
	 * computeBars
	 * 
	 * @return Iterator over children whose bars would fall within the visible
	 *         portion of the logicalWidth.
	 */
    Iterator<Perspective> visibleChildIterator() {
        return p.getChildIterator(barXs[0], barXs[(int) visibleWidth()]);
    }

    boolean isPerspectiveVisible(Perspective facet) {
        double divisor = barWidthRatio();
        int minCount = (int) Math.ceil(leftEdge / divisor + epsilon);
        int maxCount = (int) ((leftEdge + visibleWidth()) / divisor - epsilon);
        return facet.cumCountInclusive() >= minCount && facet.cumCountExclusive() <= maxCount;
    }

    Iterator<Perspective> slowVisibleChildIterator() {
        double divisor = barWidthRatio();
        int minCount = (int) Math.ceil(leftEdge / divisor + epsilon);
        int maxCount = (int) ((leftEdge + visibleWidth()) / divisor - epsilon);
        return p.cumCountChildIterator(minCount, maxCount);
    }

    public Perspective getFacet() {
        return p;
    }

    public boolean highlight(boolean state, int modifiers, PInputEvent e) {
        return false;
    }

    public void mayHideTransients(PNode node) {
    }

    public boolean pick(int modifiers, PInputEvent e) {
        return false;
    }

    boolean keyPress(char key) {
        if (letters != null) return letters.keyPress(key); else if (!art().getShowZoomLetters()) art().setTip("Zooming is disabled in beginner mode"); else if (!p.isAlphabetic()) art().setTip("Zooming is disabled because " + p.getName() + " tags are not in alphabetical order"); else assert false;
        return false;
    }
}

final class HotZoneListener extends PBasicInputEventHandler {

    PerspectiveViz pv;

    HotZoneListener(PerspectiveViz _pv) {
        pv = _pv;
    }

    @Override
    public void mouseEntered(PInputEvent e) {
        double y = e.getPositionRelativeTo(e.getPickedNode()).getY();
        pv.hotZone(y);
        e.setHandled(true);
    }

    @Override
    public void mouseExited(PInputEvent e) {
        pv.loseHotZone();
        e.setHandled(true);
    }

    @Override
    public void mouseMoved(PInputEvent e) {
        double y = e.getPositionRelativeTo(e.getPickedNode()).getY();
        pv.hotZone(y);
        e.setHandled(true);
    }
}

final class SqueezablePNode extends LazyPNode {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    double goalYscale = -1;

    private double goalY = -1;

    SqueezablePNode() {
        setPickable(false);
    }

    void setVisible() {
        setVisible(goalYscale > 0);
    }

    void layout(double y, double yScale) {
        if (yScale != goalYscale || y != goalY) {
            goalYscale = yScale;
            goalY = y;
            boolean isVisible = (yScale > 0);
            setVisible(isVisible);
            if (isVisible) {
                setTransform(Util.scaleNtranslate(1.0, yScale, 0.0, y));
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setChildrenPickable(visible);
    }
}
