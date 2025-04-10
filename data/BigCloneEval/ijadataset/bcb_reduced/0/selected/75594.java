package ch.unibe.inkml;

import org.w3c.dom.Element;

/**
 * @author emanuel
 *
 */
public class InkIdentityMapping extends InkMapping {

    /**
	 * @param ink
	 */
    public InkIdentityMapping(InkInk ink) {
        super(ink);
    }

    @Override
    public Type getType() {
        return InkMapping.Type.IDENTITY;
    }

    @Override
    public void buildFromXMLNode(Element node) throws InkMLComplianceException {
        super.buildFromXMLNode(node);
    }

    @Override
    protected void exportToInkMLHook(Element mappingNode) {
    }

    protected void saveBinds(Element mappingNode) {
    }

    @Override
    public boolean isInvertible() {
        return true;
    }

    @Override
    public void transform(double[][] sourcePoints, double[][] points, InkTraceFormat sourceFormat, InkTraceFormat targetFormat) throws InkMLComplianceException {
        int[] targetIndices = new int[sourceFormat.getChannelCount()];
        int i = 0;
        for (InkChannel c : sourceFormat) {
            targetIndices[i++] = targetFormat.indexOf(c.getName());
        }
        for (i = 0; i < sourcePoints.length; i++) {
            for (int c = 0; c < targetIndices.length; c++) {
                points[i][targetIndices[c]] = sourcePoints[i][c];
            }
        }
    }

    @Override
    public void backTransform(double[][] sourcePoints, double[][] points, InkTraceFormat canvasFormat, InkTraceFormat sourceFormat) throws InkMLComplianceException {
        int[] targetIndices = new int[sourceFormat.getChannelCount()];
        int i = 0;
        for (InkChannel c : sourceFormat) {
            targetIndices[i++] = canvasFormat.indexOf(c.getName());
        }
        for (i = 0; i < sourcePoints.length; i++) {
            for (int c = 0; c < targetIndices.length; c++) {
                sourcePoints[i][c] = points[i][targetIndices[c]];
            }
        }
    }
}
