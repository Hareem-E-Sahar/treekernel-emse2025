package de.uni_trier.st.nevada.layout.hierarchic;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public class RubberBandOptimizer implements LayoutOptimizer {

    private int fHOffset;

    public RubberBandOptimizer(final int pHOffset) {
        this.fHOffset = pHOffset;
    }

    public void optimizeLayout(final HGraph pGraph, final LOrder pOrder) {
        double lTMax, lTMin, lTGlobal, lTNew, lDeltaT;
        double lZNew, lZOld, lTthresh;
        double lFRub, lD;
        int lActRound, lMaxRound;
        lMaxRound = 5;
        lDeltaT = 0.05;
        lTMax = 1;
        lTMin = 0;
        lTthresh = lTMin + (lTMax - lTMin) / 100 * 5;
        final Map<HNode, Double> lTMap = new HashMap<HNode, Double>();
        final Map<HNode, Double> lIMap = new HashMap<HNode, Double>();
        for (int i = 1; i <= pOrder.countLayer(); ++i) {
            for (int j = 1; j <= pOrder.countLayerSize(i); ++j) {
                final HNode lCurNode = pOrder.getNode(i, j);
                lTMap.put(lCurNode, (lTMax + lTMin) / 2.0);
                lIMap.put(lCurNode, 0.0);
            }
        }
        lTGlobal = (lTMax + lTMin) / 2;
        lZNew = this.getZValue(pGraph);
        lZOld = Double.POSITIVE_INFINITY;
        lActRound = 0;
        while ((lActRound < lMaxRound) && ((lZNew < lZOld) || (lTGlobal >= lTthresh))) {
            lZOld = lZNew;
            for (int i = 1; i <= pOrder.countLayer(); ++i) {
                for (int j = 1; j <= pOrder.countLayerSize(i); ++j) {
                    final HNode lCurNode = pOrder.getNode(i, j);
                    lD = lFRub = this.getFRub(lCurNode, pGraph);
                    if (lFRub < 0) {
                        if (j > 1) {
                            final HNode lLeftNode = pOrder.getNode(i, j - 1);
                            double lLeftX;
                            if (lLeftNode instanceof DummyHNode) {
                                lLeftX = ((DummyHNode) lLeftNode).getLocation(pGraph).getX();
                            } else {
                                lLeftX = lLeftNode.getModel().getPosition(pGraph.getModel()).getX();
                            }
                            double lCurX;
                            if (lCurNode instanceof DummyHNode) {
                                lCurX = ((DummyHNode) lCurNode).getLocation(pGraph).getX();
                            } else {
                                lCurX = lCurNode.getModel().getPosition(pGraph.getModel()).getX();
                            }
                            final double lMax = lLeftX + lLeftNode.getModelDimension(pGraph.getModel()).getWidth() + this.fHOffset - lCurX;
                            if (lMax > lFRub) {
                                lD = lMax;
                            }
                        }
                    } else {
                        if (j < pOrder.countLayerSize(i)) {
                            final HNode lRightNode = pOrder.getNode(i, j + 1);
                            double lRightX;
                            if (lRightNode instanceof DummyHNode) {
                                lRightX = ((DummyHNode) lRightNode).getLocation(pGraph).getX();
                            } else {
                                lRightX = lRightNode.getModel().getPosition(pGraph.getModel()).getX();
                            }
                            double lCurX;
                            if (lCurNode instanceof DummyHNode) {
                                lCurX = ((DummyHNode) lCurNode).getLocation(pGraph).getX();
                            } else {
                                lCurX = lCurNode.getModel().getPosition(pGraph.getModel()).getX();
                            }
                            final double lMin = lRightX - lCurX - lCurNode.getModelDimension(pGraph.getModel()).getWidth() - this.fHOffset;
                            if (lMin < lFRub) {
                                lD = lMin;
                            }
                        }
                    }
                    if ((lD == 0) || (lD * lIMap.get(lCurNode).doubleValue() < 0)) {
                        lTNew = lTMap.get(lCurNode).doubleValue() - lDeltaT;
                        if (lTNew < lTMin) {
                            lTNew = lTMin;
                        }
                    } else {
                        lTNew = lTMap.get(lCurNode).doubleValue() + lDeltaT;
                        if (lTNew > lTMax) {
                            lTNew = lTMax;
                        }
                    }
                    lTGlobal += lTNew - lTMap.get(lCurNode) / pGraph.getHNodes().size();
                    if (lCurNode instanceof DummyHNode) {
                        final DummyHNode lCurNodeD = (DummyHNode) lCurNode;
                        lCurNodeD.setLocation(pGraph, new Point2D.Double(lCurNodeD.getLocation(pGraph).getX() + lD, lCurNodeD.getLocation(pGraph).getY()));
                    } else {
                        lCurNode.setLocation(pGraph.getModel(), new Point2D.Double(lCurNode.getModel().getPosition(pGraph.getModel()).getX() + lD, lCurNode.getModel().getPosition(pGraph.getModel()).getY()));
                    }
                    lIMap.put(lCurNode, new Double(lD));
                    lTMap.put(lCurNode, new Double(lTNew));
                }
            }
            lZNew = this.getZValue(pGraph);
            ++lActRound;
        }
    }

    private double getFRub(final HNode pCurNode, final HGraph pGraph) {
        double lF = 0.0;
        int degree = 0;
        double lArgNodeX;
        if (pCurNode instanceof DummyHNode) {
            lArgNodeX = ((DummyHNode) pCurNode).getLocation(pGraph).getX();
        } else {
            lArgNodeX = pCurNode.getModel().getPosition(pGraph.getModel()).getX();
        }
        for (final HNode lCurNode : pCurNode.getOutNodes(pGraph)) {
            double lCurNodeX;
            if (lCurNode instanceof DummyHNode) {
                lCurNodeX = ((DummyHNode) lCurNode).getLocation(pGraph).getX();
            } else {
                lCurNodeX = lCurNode.getModel().getPosition(pGraph.getModel()).getX();
            }
            lF += lCurNodeX - lArgNodeX;
            ++degree;
        }
        for (final HNode lCurNode : pCurNode.getInNodes(pGraph)) {
            double lCurNodeX;
            if (lCurNode instanceof DummyHNode) {
                lCurNodeX = ((DummyHNode) lCurNode).getLocation(pGraph).getX();
            } else {
                lCurNodeX = lCurNode.getModel().getPosition(pGraph.getModel()).getX();
            }
            lF += lCurNodeX - lArgNodeX;
            ++degree;
        }
        if (degree != 0) {
            return lF / degree;
        }
        return 0.0;
    }

    private double getZValue(final HGraph pGraph) {
        double lZ = 0;
        for (final HNode lCurNode : pGraph.getHNodes()) {
            double lCurNodeX;
            if (lCurNode instanceof DummyHNode) {
                lCurNodeX = ((DummyHNode) lCurNode).getLocation(pGraph).getX();
            } else {
                lCurNodeX = lCurNode.getModel().getPosition(pGraph.getModel()).getX();
            }
            for (final HNode lSuccNode : lCurNode.getOutNodes(pGraph)) {
                double lSuccNodeX;
                if (lSuccNode instanceof DummyHNode) {
                    lSuccNodeX = ((DummyHNode) lSuccNode).getLocation(pGraph).getX();
                } else {
                    lSuccNodeX = lSuccNode.getModel().getPosition(pGraph.getModel()).getX();
                }
                lZ += lSuccNodeX - lCurNodeX;
            }
            for (final HNode lPrevNode : lCurNode.getInNodes(pGraph)) {
                double lPrevNodeX;
                if (lPrevNode instanceof DummyHNode) {
                    lPrevNodeX = ((DummyHNode) lPrevNode).getLocation(pGraph).getX();
                } else {
                    lPrevNodeX = lPrevNode.getModel().getPosition(pGraph.getModel()).getX();
                }
                lZ += lPrevNodeX - lCurNodeX;
            }
        }
        return lZ;
    }
}
