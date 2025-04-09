package logic;

import java.awt.geom.Line2D;
import java.awt.Point;
import java.util.ArrayList;

public class Wire extends Component implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Gate from, to;

    private int fromPinNumber, toPinNumber;

    public boolean isVisible;

    public static int numberOfWires = 0;

    public int uniqueWireID;

    private MultiLine line;

    public Wire(Gate from, int outputPinNumber, Gate to, int inputPinNumber) {
        this.from = from;
        this.to = to;
        this.fromPinNumber = outputPinNumber;
        this.toPinNumber = inputPinNumber;
        to.numberOfWiresConnectedToInputs++;
        uniqueWireID = numberOfWires++;
        line = calculateDefaultLine();
    }

    private MultiLine calculateDefaultLine() {
        java.util.ArrayList<Integer> xCoords = new ArrayList<Integer>();
        java.util.ArrayList<Integer> yCoords = new ArrayList<Integer>();
        int startX = from.getOutputPos(fromPinNumber - from.getNumInputs()).x;
        int startY = from.getOutputPos(fromPinNumber - from.getNumInputs()).y;
        int endX = to.getInputPos(toPinNumber).x;
        int endY = to.getInputPos(toPinNumber).y;
        if (startX >= endX) {
            int startTop = from.getYpos();
            int startBottom = startTop + from.getHeight();
            int endTop = to.getYpos();
            int endBottom = endTop + to.getHeight();
            int minPossY = Math.min(startTop, endTop) - 5;
            int maxPossY = Math.max(startBottom, endBottom) + 5;
            int optimalY;
            if (startBottom + 5 < endTop) {
                optimalY = (startBottom + endTop) / 2;
            } else if (endBottom + 5 < startTop) {
                optimalY = (endBottom + startTop) / 2;
            } else if (startY - minPossY + endY - minPossY < maxPossY - startY + maxPossY - endY) {
                optimalY = minPossY;
            } else {
                optimalY = maxPossY;
            }
            xCoords.add(startX);
            xCoords.add(startX + 10);
            xCoords.add(startX + 10);
            xCoords.add(endX - 10);
            xCoords.add(endX - 10);
            xCoords.add(endX);
            yCoords.add(startY);
            yCoords.add(startY);
            yCoords.add(optimalY);
            yCoords.add(optimalY);
            yCoords.add(endY);
            yCoords.add(endY);
        } else {
            int avgX = (startX + endX) / 2;
            xCoords.add(startX);
            xCoords.add(avgX);
            xCoords.add(avgX);
            xCoords.add(endX);
            yCoords.add(startY);
            yCoords.add(startY);
            yCoords.add(endY);
            yCoords.add(endY);
        }
        return new MultiLine(xCoords, yCoords);
    }

    public int getUniqueWireNumber() {
        return uniqueWireID;
    }

    public int getFromPinNumber() {
        return fromPinNumber;
    }

    public int getToPinNumber() {
        return toPinNumber;
    }

    public Gate getFrom() {
        return from;
    }

    public Gate getTo() {
        return to;
    }

    public void updateLine(MultiLine multi) {
        line = multi;
    }

    public boolean containsPoint(int x, int y) {
        return line.containsPoint(x, y);
    }

    public int getState() {
        return from.getValue(fromPinNumber);
    }

    public void dragTo(int x, int y) {
        line.dragTo(x, y);
        line.moveEnd(to.getInputPos(toPinNumber));
    }

    public void dragBy(int deltaX, int deltaY) {
        line.dragBy(deltaX, deltaY);
    }

    public void moveStart(Point p) {
        line.moveStart((int) p.getX(), (int) p.getY());
    }

    public void moveEnd(Point p) {
        line.moveEnd((int) p.getX(), (int) p.getY());
    }

    public boolean containedBy(java.awt.Rectangle r) {
        return line.containedBy(r);
    }

    public void moveStartOfWireTo(Gate g, int outputOfNewGate) {
        this.from = g;
        this.fromPinNumber = outputOfNewGate;
    }

    public void moveEndOfWireTo(Gate g, int inputOfNewGate) {
        this.to = g;
        this.toPinNumber = inputOfNewGate;
    }

    public Line2D.Double getLine(int i) {
        return line.getLine(i);
    }

    public int numberOfLines() {
        return line.numberOfLines();
    }
}
