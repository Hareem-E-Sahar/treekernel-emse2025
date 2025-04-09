package com.silenistudios.silenus.raw;

import java.io.Serializable;
import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.xml.XMLUtility;

/**
 * A transformation matrix is a 3x3 matrix containing scaling, shearing and translation components, like this:
 * [ a b tx ]
 * [ c d ty ]
 * [ 0 0 1  ]
 * This is the typical definition of an affine transformation.
 * @author Karel
 *
 */
public class TransformationMatrix implements Serializable {

    private static final long serialVersionUID = -8782592959755072441L;

    double[][] fMatrix = new double[2][2];

    double fTranslateX;

    double fTranslateY;

    public TransformationMatrix() {
        fMatrix[0][0] = 1.0;
        fMatrix[0][1] = 0.0;
        fMatrix[1][0] = 0.0;
        fMatrix[1][1] = 1.0;
        fTranslateX = 0.0;
        fTranslateY = 0.0;
    }

    public TransformationMatrix(XMLUtility XMLUtility, Node matrix) throws ParseException {
        setTranslateX(XMLUtility.getDoubleAttribute(matrix, "tx", 0.0));
        setTranslateY(XMLUtility.getDoubleAttribute(matrix, "ty", 0.0));
        setMatrixElement(0, 0, XMLUtility.getDoubleAttribute(matrix, "a", 1.0));
        setMatrixElement(0, 1, XMLUtility.getDoubleAttribute(matrix, "b", 0.0));
        setMatrixElement(1, 0, XMLUtility.getDoubleAttribute(matrix, "c", 0.0));
        setMatrixElement(1, 1, XMLUtility.getDoubleAttribute(matrix, "d", 1.0));
    }

    public TransformationMatrix(double[][] matrix, double translateX, double translateY) {
        fMatrix[0][0] = matrix[0][0];
        fMatrix[0][1] = matrix[0][1];
        fMatrix[1][0] = matrix[1][0];
        fMatrix[1][1] = matrix[1][1];
        fTranslateX = translateX;
        fTranslateY = translateY;
    }

    public TransformationMatrix(double translateX, double translateY, double scaleX, double scaleY, double rotation) {
        fTranslateX = translateX;
        fTranslateY = translateY;
        fMatrix[0][0] = Math.cos(rotation) * scaleX;
        fMatrix[0][1] = -Math.sin(rotation) * scaleY;
        fMatrix[1][0] = Math.sin(rotation) * scaleX;
        fMatrix[1][1] = Math.cos(rotation) * scaleY;
    }

    public double computeX(double x, double y) {
        return getScaleX() * Math.cos(-getRotation()) * x - getScaleY() * Math.sin(-getRotation()) * y + fTranslateX;
    }

    public double computeY(double x, double y) {
        return getScaleX() * Math.sin(-getRotation()) * x + getScaleY() * Math.cos(-getRotation()) * y + fTranslateY;
    }

    public double[][] getMatrix() {
        return fMatrix;
    }

    public double getTranslateX() {
        return fTranslateX;
    }

    public double getTranslateY() {
        return fTranslateY;
    }

    public double getScaleX() {
        return Math.sqrt(fMatrix[0][0] * fMatrix[0][0] + fMatrix[0][1] * fMatrix[0][1]);
    }

    public double getScaleY() {
        return Math.sqrt(fMatrix[1][0] * fMatrix[1][0] + fMatrix[1][1] * fMatrix[1][1]);
    }

    private double det() {
        return fMatrix[0][0] * fMatrix[1][1] - fMatrix[0][1] * fMatrix[1][0];
    }

    private double sign(double x) {
        return (Math.abs(x) < 0.0000001) ? 1 : x / Math.abs(x);
    }

    public double getRotation() {
        return Math.atan2(fMatrix[1][0], fMatrix[0][0]);
    }

    public void setMatrixElement(int x, int y, double value) {
        fMatrix[x][y] = value;
    }

    public void setTranslateX(double tx) {
        fTranslateX = tx;
    }

    public void setTranslateY(double ty) {
        fTranslateY = ty;
    }

    public static TransformationMatrix compose(TransformationMatrix B, TransformationMatrix A) {
        TransformationMatrix C = new TransformationMatrix();
        C.fMatrix[0][0] = B.fMatrix[0][0] * A.fMatrix[0][0] + B.fMatrix[0][1] * A.fMatrix[1][0];
        C.fMatrix[0][1] = B.fMatrix[0][0] * A.fMatrix[0][1] + B.fMatrix[0][1] * A.fMatrix[1][1];
        C.fMatrix[1][0] = B.fMatrix[1][0] * A.fMatrix[0][0] + B.fMatrix[1][1] * A.fMatrix[1][0];
        C.fMatrix[1][1] = B.fMatrix[1][0] * A.fMatrix[0][1] + B.fMatrix[1][1] * A.fMatrix[1][1];
        C.fTranslateX = B.fMatrix[0][0] * A.fTranslateX + B.fMatrix[0][1] * A.fTranslateY + B.fTranslateX;
        C.fTranslateY = B.fMatrix[1][0] * A.fTranslateX + B.fMatrix[1][1] * A.fTranslateY + B.fTranslateY;
        return C;
    }

    @Override
    public String toString() {
        return "[" + fMatrix[0][0] + " " + fMatrix[0][1] + " ; " + fMatrix[1][0] + " " + fMatrix[1][1] + "] + [" + fTranslateX + " " + fTranslateY + "]";
    }

    public boolean isFlipped() {
        double r = Math.atan2(fMatrix[1][0], fMatrix[0][0]) - Math.atan2(fMatrix[1][1], fMatrix[0][1]);
        if (r < Math.PI) r += 2 * Math.PI;
        if (r > Math.PI) r -= 2 * Math.PI;
        return r > 0.0;
    }

    @Override
    public TransformationMatrix clone() {
        return new TransformationMatrix(getMatrix(), getTranslateX(), getTranslateY());
    }
}
