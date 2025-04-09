package com.compomics.icelogo.gui.graph;

import com.compomics.icelogo.core.aaindex.AAIndexParameterMatrix;
import com.compomics.icelogo.core.aaindex.AAIndexParameterResult;
import com.compomics.icelogo.core.data.AminoAcidCounter;
import com.compomics.icelogo.core.data.MainInformationFeeder;
import com.compomics.icelogo.core.data.MatrixAminoAcidStatistics;
import com.compomics.icelogo.core.enumeration.AminoAcidEnum;
import com.compomics.icelogo.core.enumeration.ExperimentTypeEnum;
import com.compomics.icelogo.core.enumeration.ObservableEnum;
import com.compomics.icelogo.core.interfaces.AminoAcidStatistics;
import com.compomics.icelogo.core.interfaces.MatrixDataModel;
import com.compomics.icelogo.gui.interfaces.Savable;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import javax.swing.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * This class creates a SVG document. It paint a graph with the values (mean, reference mean, confidence interval) for a specific Aa parameter matrix
 */
public class AAIndexComponent extends JSVGCanvas implements Observer, Savable {

    public int iLogoWidth;

    public int iLogoHeigth;

    /**
     * The number of elements
     */
    public int iLogoElements;

    /**
     * The start position
     */
    public int iStartPosition;

    /**
     * The width of on element (This is the (logo width - 100)/ logo elements)
     */
    public double iElementWidth;

    /**
     * The SVG document
     */
    public SVGDocument doc;

    /**
     * The matrix data model
     */
    public MatrixDataModel iDataModel;

    /**
     * The standard deviation used for the creation of this graph
     */
    public double iStandardDeviation;

    /**
     * The information feeder
     */
    public MainInformationFeeder iInformationFeeder;

    /**
     * The Aa parameter matrix for this graph
     */
    private AAIndexParameterMatrix iAaParameterMatrix;

    /**
     * A vector with the calculated results for the aa parameter matrix
     */
    private Vector<AAIndexParameterResult> iAaParameterResults = new Vector<AAIndexParameterResult>();

    /**
     * The maximum value that can be displayed on the graph
     */
    private double iMax = -99999999999.0;

    /**
     * The minimum value that can be displayed on the graph
     */
    private double iMin = 99999999999.0;

    /**
     * A boolean that indicates if a sliding window must be used
     */
    private boolean iUseSlidingWindow;

    /**
     * Int with the size of the sliding window
     */
    private int iSlidingWindowSize;

    /**
     * indicates if this panel is updating
     */
    private boolean iUpdating;

    /**
     * boolean that indicates that we work with two sets
     */
    private boolean lUseTwoSets = false;

    /**
     * The Constructor
     *
     * @param aDataModel MatrixDataModel.
     */
    public AAIndexComponent(MatrixDataModel aDataModel) {
        this.iLogoElements = aDataModel.getNumberOfPositions();
        this.iInformationFeeder = MainInformationFeeder.getInstance();
        iInformationFeeder.addObserver(this);
        this.iDataModel = aDataModel;
        this.makeSVG();
    }

    /**
     * This methods gives an SVG document.
     *
     * @return An SVG document
     */
    public SVGDocument getSVG() {
        return doc;
    }

    /**
     * This method get all the necessary information from the MainInformationFeeder Singleton.
     */
    public void getInfo() {
        this.iStartPosition = iInformationFeeder.getStartPosition();
        this.iStandardDeviation = iInformationFeeder.getZscore();
        this.iAaParameterMatrix = iInformationFeeder.getSelectedAaParameterMatrix();
        this.iSlidingWindowSize = iInformationFeeder.getSlidingWindowSize();
        this.iUseSlidingWindow = iInformationFeeder.isSlidingWindowInAaParameter();
        this.iLogoHeigth = iInformationFeeder.getGraphableHeight();
        this.iLogoWidth = iInformationFeeder.getGraphableWidth();
    }

    /**
     * This method calculates the AaParameterResult for the different positions.
     * The results are stored in the iAaParameterResults vector.
     */
    public void calculate() {
        iMax = -99999999999.0;
        iMin = 99999999999.0;
        iAaParameterResults.removeAllElements();
        for (int p = 0; p < iDataModel.getNumberOfPositions(); p++) {
            AminoAcidStatistics lExperimentalMatrix = iDataModel.getExperimentalAminoAcidStatistics(p, ExperimentTypeEnum.EXPERIMENT);
            AminoAcidStatistics lTwoExperimentalMatrix = iDataModel.getExperimentalAminoAcidStatistics(p, ExperimentTypeEnum.EXPERIMENT_TWO);
            if (lTwoExperimentalMatrix != null) {
                lUseTwoSets = true;
            }
            AminoAcidStatistics lReferenceMatrix = iDataModel.getReferenceAminoAcidStatistics(p);
            int lPosSetSize = (int) lExperimentalMatrix.getStatistics(AminoAcidEnum.ALA).getN();
            int lTwoPosSetSize = 0;
            if (lUseTwoSets) {
                lTwoPosSetSize = (int) lTwoExperimentalMatrix.getStatistics(AminoAcidEnum.ALA).getN();
            }
            int lRefSetSize = (int) lReferenceMatrix.getStatistics(AminoAcidEnum.ALA).getN();
            int lN = 0;
            if (lPosSetSize > lRefSetSize) {
                lN = lRefSetSize;
            } else {
                lN = lPosSetSize;
            }
            if (lUseTwoSets && lN > lTwoPosSetSize) {
                lN = lTwoPosSetSize;
            }
            double lPositionMean = 0.0;
            double lTwoPositionMean = 0.0;
            double lReferenceMean = 0.0;
            for (AminoAcidEnum aa : AminoAcidEnum.values()) {
                StatisticalSummary lPosStat = lExperimentalMatrix.getStatistics(aa);
                lPositionMean = lPositionMean + lPosStat.getMean() * (Double) iAaParameterMatrix.getValueForAminoAcid(aa);
                if (lUseTwoSets) {
                    StatisticalSummary lTwoPosStat = lTwoExperimentalMatrix.getStatistics(aa);
                    lTwoPositionMean = lTwoPositionMean + lTwoPosStat.getMean() * (Double) iAaParameterMatrix.getValueForAminoAcid(aa);
                }
            }
            DescriptiveStatistics lReferenceMeans = new DescriptiveStatistics();
            if (lReferenceMatrix.getDimension() == 1) {
                for (int i = 0; i < 100; i++) {
                    AminoAcidEnum[] lAas = lReferenceMatrix.getRandomPeptide(lN);
                    double lSum = 0.0;
                    for (int j = 0; j < lAas.length; j++) {
                        lSum = lSum + (Double) iAaParameterMatrix.getValueForAminoAcid(lAas[j]);
                    }
                    lReferenceMeans.addValue(lSum / (double) lAas.length);
                }
            } else {
                MatrixAminoAcidStatistics lReferenceMatrixStatistics = (MatrixAminoAcidStatistics) lReferenceMatrix;
                for (int i = 0; i < lReferenceMatrixStatistics.getDimension(); i++) {
                    AminoAcidCounter lCounter = lReferenceMatrixStatistics.getAminoAcidCounter(i);
                    double lSum = 0.0;
                    for (AminoAcidEnum aa : AminoAcidEnum.values()) {
                        for (int j = 0; j < lCounter.getCount(aa); j++) {
                            lSum = lSum + (Double) iAaParameterMatrix.getValueForAminoAcid(aa);
                        }
                    }
                    lReferenceMeans.addValue(lSum / lCounter.getTotalCount());
                }
            }
            lReferenceMean = lReferenceMeans.getMean();
            double lStandardDeviation = lReferenceMeans.getStandardDeviation();
            if (lPositionMean > iMax) {
                iMax = lPositionMean;
            }
            if (lUseTwoSets && lTwoPositionMean > iMax) {
                iMax = lTwoPositionMean;
            }
            if (lReferenceMean > iMax) {
                iMax = lReferenceMean;
            }
            if (lPositionMean < iMin) {
                iMin = lPositionMean;
            }
            if (lUseTwoSets && lTwoPositionMean < iMin) {
                iMin = lTwoPositionMean;
            }
            if (lReferenceMean < iMin) {
                iMin = lReferenceMean;
            }
            AAIndexParameterResult lResult = new AAIndexParameterResult(lPositionMean, lReferenceMean, lStandardDeviation, p);
            if (lUseTwoSets) {
                lResult.setCalulatedMeanSetTwo(lTwoPositionMean);
            }
            if (lResult.getUpperConfidenceLimit() > iMax) {
                iMax = lResult.getUpperConfidenceLimit();
            }
            if (lResult.getLowerConfidenceLimit() < iMin) {
                iMin = lResult.getLowerConfidenceLimit();
            }
            iAaParameterResults.add(lResult);
        }
        double lDiff = iMax - iMin;
        iMax = Math.round((iMax + lDiff / 10.0) * 100.0) / 100.0;
        iMin = Math.round((iMin - lDiff / 10.0) * 100.0) / 100.0;
        if (iUseSlidingWindow) {
            useSlidingWindow();
        }
    }

    /**
     * This method will do the calculations if a sliding window is needed.
     * It will get the AaParameterResults from the iAaParameterResults Vector.
     * It will store the results also in the iAaParameterResults Vector.
     */
    public void useSlidingWindow() {
        Vector<AAIndexParameterResult> lTemp = new Vector<AAIndexParameterResult>();
        int lSlidingWindowStart = 0;
        if (iSlidingWindowSize % 2 == 0) {
            lSlidingWindowStart = iSlidingWindowSize / 2;
        } else {
            lSlidingWindowStart = (iSlidingWindowSize + 1) / 2;
        }
        for (int i = 0; i < iAaParameterResults.size(); i++) {
            double lTempCalMean = 0.0;
            double lTempRefMean = 0.0;
            double lTempSD = 0.0;
            double lTwoTempCalMean = 0.0;
            double lTwoTempRefMean = 0.0;
            double lTwoTempSD = 0.0;
            int lNumberOfSummedResults = 0;
            for (int j = 1; j <= iSlidingWindowSize; j++) {
                int lResultPosition = i - (lSlidingWindowStart - j);
                if (lResultPosition >= 0 && lResultPosition <= iAaParameterResults.size() - 1) {
                    AAIndexParameterResult lResult = iAaParameterResults.get(lResultPosition);
                    lTempCalMean = lTempCalMean + lResult.getCalulatedMean();
                    lTempRefMean = lTempRefMean + lResult.getNegativeSetMean();
                    lTempSD = lTempSD + lResult.getStandardDeviation();
                    if (lUseTwoSets) {
                        lTwoTempCalMean = lTwoTempCalMean + lResult.getCalulatedMeanSetTwo();
                        lTwoTempRefMean = lTwoTempRefMean + lResult.getNegativeSetMean();
                        lTwoTempSD = lTwoTempSD + lResult.getStandardDeviation();
                    }
                    lNumberOfSummedResults = lNumberOfSummedResults + 1;
                }
            }
            AAIndexParameterResult lResult = new AAIndexParameterResult(lTempCalMean / (double) lNumberOfSummedResults, lTempRefMean / (double) lNumberOfSummedResults, lTempSD / (double) lNumberOfSummedResults, i);
            if (lUseTwoSets) {
                lResult.setCalulatedMeanSetTwo(lTwoTempCalMean / (double) lNumberOfSummedResults);
            }
            lTemp.add(lResult);
        }
        iAaParameterResults = lTemp;
    }

    /**
     * This method "paints" the logo in a SVG document.
     */
    public void makeSVG() {
        if (!iUpdating) {
            iUpdating = true;
            this.getInfo();
            this.calculate();
            iElementWidth = (iLogoWidth - 100) / iLogoElements;
            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
            String svgNS = "http://www.w3.org/2000/svg";
            doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
            Element svgRoot = doc.getDocumentElement();
            svgRoot.setAttributeNS(null, "width", String.valueOf(iLogoWidth - 50));
            svgRoot.setAttributeNS(null, "height", String.valueOf(iLogoHeigth));
            Element yAxis = doc.createElementNS(svgNS, "rect");
            yAxis.setAttributeNS(null, "x", "49");
            yAxis.setAttributeNS(null, "y", "50");
            yAxis.setAttributeNS(null, "width", "1");
            yAxis.setAttributeNS(null, "height", String.valueOf(iLogoHeigth - 80));
            yAxis.setAttributeNS(null, "style", "fill:black");
            Element xAxis1 = doc.createElementNS(svgNS, "rect");
            xAxis1.setAttributeNS(null, "x", "49");
            xAxis1.setAttributeNS(null, "y", String.valueOf(iLogoHeigth - 50));
            xAxis1.setAttributeNS(null, "width", String.valueOf(iElementWidth * iLogoElements));
            xAxis1.setAttributeNS(null, "height", "1");
            xAxis1.setAttributeNS(null, "style", "fill:black");
            Element xAxis2 = doc.createElementNS(svgNS, "rect");
            xAxis2.setAttributeNS(null, "x", "49");
            xAxis2.setAttributeNS(null, "y", String.valueOf(iLogoHeigth - 30));
            xAxis2.setAttributeNS(null, "width", String.valueOf(iElementWidth * iLogoElements));
            xAxis2.setAttributeNS(null, "height", "1");
            xAxis2.setAttributeNS(null, "style", "fill:black");
            Element top = doc.createElementNS(svgNS, "path");
            top.setAttributeNS(null, "d", "M  44.5,54 L 49.5,50 L 54.5,54");
            top.setAttributeNS(null, "style", "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1");
            Element markerLine1 = doc.createElementNS(svgNS, "path");
            markerLine1.setAttributeNS(null, "d", "M  49,70 L 44,70 L 44,70");
            markerLine1.setAttributeNS(null, "style", "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1");
            Element markerLine2 = doc.createElementNS(svgNS, "path");
            markerLine2.setAttributeNS(null, "d", "M  49," + String.valueOf(70 + (iLogoHeigth - 120) / 2) + " L 44," + String.valueOf(70 + (iLogoHeigth - 120) / 2) + "");
            markerLine2.setAttributeNS(null, "style", "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1");
            Element marker1 = doc.createElementNS(svgNS, "text");
            marker1.setAttributeNS(null, "x", "20");
            marker1.setAttributeNS(null, "y", "70");
            marker1.setAttributeNS(null, "style", "font-size:14px;fill:black;font-family:Arial");
            marker1.setAttributeNS(null, "text-anchor", "middle");
            Text markerText1 = doc.createTextNode(String.valueOf(iMax));
            marker1.appendChild(markerText1);
            svgRoot.appendChild(marker1);
            Element marker3 = doc.createElementNS(svgNS, "text");
            marker3.setAttributeNS(null, "x", "20");
            marker3.setAttributeNS(null, "y", String.valueOf(70 + (iLogoHeigth - 70 - 50) / 2));
            marker3.setAttributeNS(null, "style", "font-size:14px;fill:black;font-family:Arial");
            marker3.setAttributeNS(null, "text-anchor", "middle");
            Text markerText3 = doc.createTextNode(String.valueOf(Math.round((iMax - (iMax - iMin) / 2) * 100.0) / 100.0));
            marker3.appendChild(markerText3);
            svgRoot.appendChild(marker3);
            int startNumber = iStartPosition;
            int elementCount = 0;
            for (int s = startNumber; s < startNumber + iLogoElements; s++) {
                Element number = doc.createElementNS(svgNS, "text");
                number.setAttributeNS(null, "x", String.valueOf(50 + elementCount * iElementWidth + iElementWidth / 2));
                number.setAttributeNS(null, "y", String.valueOf(iLogoHeigth - 35));
                number.setAttributeNS(null, "style", "font-size:14px;fill:black;font-family:Arial");
                number.setAttributeNS(null, "text-anchor", "middle");
                Text numberText = doc.createTextNode(String.valueOf(s));
                number.appendChild(numberText);
                svgRoot.appendChild(number);
                elementCount = elementCount + 1;
                Element line = doc.createElementNS(svgNS, "path");
                line.setAttributeNS(null, "d", "M  " + String.valueOf(49 + elementCount * iElementWidth) + "," + String.valueOf(iLogoHeigth - 30) + " L " + String.valueOf(49 + elementCount * iElementWidth) + "," + String.valueOf(iLogoHeigth - 50));
                line.setAttributeNS(null, "style", "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1");
                svgRoot.appendChild(line);
            }
            svgRoot.appendChild(yAxis);
            svgRoot.appendChild(xAxis1);
            svgRoot.appendChild(xAxis2);
            svgRoot.appendChild(top);
            svgRoot.appendChild(markerLine1);
            svgRoot.appendChild(markerLine2);
            if (iUseSlidingWindow) {
                Element titleAxis2 = doc.createElementNS(svgNS, "text");
                titleAxis2.setAttributeNS(null, "x", String.valueOf((iLogoHeigth / -2) + (iLogoHeigth / -4)));
                titleAxis2.setAttributeNS(null, "y", "35");
                titleAxis2.setAttributeNS(null, "transform", "matrix(0,-1,1,0,0,0)");
                titleAxis2.setAttributeNS(null, "style", "font-size:14px;fill:black;font-family:Arial");
                titleAxis2.setAttributeNS(null, "text-anchor", "middle");
                Text data2 = doc.createTextNode("Sliding window size: " + iSlidingWindowSize);
                titleAxis2.appendChild(data2);
                svgRoot.appendChild(titleAxis2);
            }
            Element titleAxis2 = doc.createElementNS(svgNS, "text");
            titleAxis2.setAttributeNS(null, "x", String.valueOf(iLogoWidth / 2));
            titleAxis2.setAttributeNS(null, "y", "25");
            titleAxis2.setAttributeNS(null, "style", "font-size:14px;fill:black;font-family:Arial");
            titleAxis2.setAttributeNS(null, "text-anchor", "middle");
            Text data2 = doc.createTextNode(iAaParameterMatrix.getTitle());
            titleAxis2.appendChild(data2);
            svgRoot.appendChild(titleAxis2);
            double lMaxVerticalHeight = (double) iLogoHeigth - 100.0;
            double lMaxDiff = iMax - iMin;
            String lConfidenceIntervalString = "";
            String lPositionString = "";
            String lReferenceString = "";
            for (int p = 0; p < iAaParameterResults.size(); p++) {
                double elementStartX = 50.0 + p * iElementWidth + iElementWidth / 2;
                double lDiff = iAaParameterResults.get(p).getUpperConfidenceLimit() - iMin;
                double lDiffPerc = lDiff / lMaxDiff;
                double elementStartY = (double) iLogoHeigth - 50.0 - (lMaxVerticalHeight * lDiffPerc);
                if (p == 0) {
                    lConfidenceIntervalString = "M  " + String.valueOf(elementStartX) + "," + elementStartY;
                } else {
                    lConfidenceIntervalString = lConfidenceIntervalString + " L " + String.valueOf(elementStartX) + "," + elementStartY;
                }
            }
            for (int p = 0; p < iAaParameterResults.size(); p++) {
                double elementStartX = 50.0 + (iAaParameterResults.size() - 1 - p) * iElementWidth + iElementWidth / 2;
                double lDiff = iAaParameterResults.get(iAaParameterResults.size() - 1 - p).getLowerConfidenceLimit() - iMin;
                double lDiffPerc = lDiff / lMaxDiff;
                double elementStartY = (double) iLogoHeigth - 50.0 - (lMaxVerticalHeight * lDiffPerc);
                lConfidenceIntervalString = lConfidenceIntervalString + " L " + String.valueOf(elementStartX) + "," + elementStartY;
            }
            lConfidenceIntervalString = lConfidenceIntervalString + " z ";
            Element lConfidence = doc.createElementNS(svgNS, "path");
            lConfidence.setAttributeNS(null, "d", lConfidenceIntervalString);
            lConfidence.setAttributeNS(null, "style", "fill:#ed00b2;fill-rule:evenodd;stroke:#000000;stroke-width:0.9;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;fill-opacity:1;opacity:0.27;stroke-miterlimit:4;stroke-dasharray:none");
            svgRoot.appendChild(lConfidence);
            for (int p = 0; p < iAaParameterResults.size(); p++) {
                double elementStartX = 50.0 + p * iElementWidth + iElementWidth / 2;
                double lDiff = iAaParameterResults.get(p).getCalulatedMean() - iMin;
                double lDiffPerc = lDiff / lMaxDiff;
                double elementStartY = (double) iLogoHeigth - 50.0 - (lMaxVerticalHeight * lDiffPerc);
                if (p == 0) {
                    lPositionString = "M  " + String.valueOf(elementStartX) + "," + elementStartY;
                } else {
                    lPositionString = lPositionString + " L " + String.valueOf(elementStartX) + "," + elementStartY;
                }
            }
            Element lPositionMeans = doc.createElementNS(svgNS, "path");
            lPositionMeans.setAttributeNS(null, "d", lPositionString);
            lPositionMeans.setAttributeNS(null, "style", "fill:none;fill-rule:evenodd;stroke:#00ff00;stroke-width:3;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:none");
            svgRoot.appendChild(lPositionMeans);
            if (lUseTwoSets) {
                for (int p = 0; p < iAaParameterResults.size(); p++) {
                    double elementStartX = 50.0 + p * iElementWidth + iElementWidth / 2;
                    double lDiff = iAaParameterResults.get(p).getCalulatedMeanSetTwo() - iMin;
                    double lDiffPerc = lDiff / lMaxDiff;
                    double elementStartY = (double) iLogoHeigth - 50.0 - (lMaxVerticalHeight * lDiffPerc);
                    if (p == 0) {
                        lPositionString = "M  " + String.valueOf(elementStartX) + "," + elementStartY;
                    } else {
                        lPositionString = lPositionString + " L " + String.valueOf(elementStartX) + "," + elementStartY;
                    }
                }
                Element lTwoPositionMeans = doc.createElementNS(svgNS, "path");
                lTwoPositionMeans.setAttributeNS(null, "d", lPositionString);
                lTwoPositionMeans.setAttributeNS(null, "style", "fill:none;fill-rule:evenodd;stroke:#0000FF;stroke-width:3;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:none");
                svgRoot.appendChild(lTwoPositionMeans);
            }
            for (int p = 0; p < iAaParameterResults.size(); p++) {
                double elementStartX = 50.0 + p * iElementWidth + iElementWidth / 2;
                double lDiff = iAaParameterResults.get(p).getNegativeSetMean() - iMin;
                double lDiffPerc = lDiff / lMaxDiff;
                double elementStartY = (double) iLogoHeigth - 50.0 - (lMaxVerticalHeight * lDiffPerc);
                if (p == 0) {
                    lReferenceString = "M  " + String.valueOf(elementStartX) + "," + elementStartY;
                } else {
                    lReferenceString = lReferenceString + " L " + String.valueOf(elementStartX) + "," + elementStartY;
                }
            }
            Element lReferenceMeans = doc.createElementNS(svgNS, "path");
            lReferenceMeans.setAttributeNS(null, "d", lReferenceString);
            lReferenceMeans.setAttributeNS(null, "style", "fill:none;fill-rule:evenodd;stroke:#ff0000;stroke-width:3;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:none");
            svgRoot.appendChild(lReferenceMeans);
            this.setSVGDocument(doc);
            iUpdating = false;
        }
    }

    /**
     * An update is performed when something is changed in the observed object (MainInformationFeeder)
     *
     * @param o   The observed object
     * @param arg An argument
     */
    public void update(Observable o, Object arg) {
        if (arg != null && (arg.equals(ObservableEnum.NOTIFY_AA_PARAMETER) || arg.equals(ObservableEnum.NOTIFY_STATISTICAL) || arg.equals(ObservableEnum.NOTIFY_GRAPHABLE_FRAME_SIZE) || arg.equals(ObservableEnum.NOTIFY_START_POSITION))) {
            this.makeSVG();
        }
    }

    public boolean isSvg() {
        return true;
    }

    public boolean isChart() {
        return false;
    }

    public JPanel getContentPanel() {
        return null;
    }

    public String getTitle() {
        return "aaParameter";
    }

    public String getDescription() {
        return "Graph with the aa parameter";
    }

    /**
     * Gives a boolean that indicates if the saveble is text.
     *
     * @return
     */
    public boolean isText() {
        return false;
    }

    public String getText() {
        return null;
    }
}
