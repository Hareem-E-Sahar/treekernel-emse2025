package org.deved.antlride.internal.ui.views.railroad;

import java.util.HashMap;
import java.util.Map;
import org.deved.antlride.core.model.ElementKind;
import org.deved.antlride.core.model.IAlternative;
import org.deved.antlride.core.model.IBlock;
import org.deved.antlride.core.model.IModelElement;
import org.deved.antlride.core.model.IStatement;
import org.deved.antlride.core.model.IStatement.EBNF;
import org.deved.antlride.core.model.ast.AAbstractModelElementVisitor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class AntlrRailRoadVisitor extends AAbstractModelElementVisitor {

    private int fColumn;

    private int fRow;

    private int fLevel = -2;

    private StringBuilder buffer = new StringBuilder();

    private Map<IStatement, Point> fPositions = new HashMap<IStatement, Point>();

    private AntlrRailRoadMatrix<Object> fMatrix = new AntlrRailRoadMatrix<Object>();

    private static final int INITIAL_X = 50;

    private static final int X_DISTANCE = 10;

    private static final int Y_DISTANCE = 20;

    @Override
    public void accept(IModelElement node) {
        buffer.append("\n");
        buffer.append(node.getElementName());
        buffer.append(":\n");
        super.accept(node);
    }

    public String getRepresentation() {
        return buffer.toString();
    }

    public Figure getDiagram() {
        adjustsLocation();
        DrawDiagram draw = new DrawDiagram();
        fMatrix.iterate(draw);
        return draw.root;
    }

    private void adjustsLocation() {
        System.out.println(getRepresentation());
        System.out.println(fMatrix.toString());
        fMatrix.iterate(new AntlrRailRoadMatrixIterator() {

            int x_offset = 0;

            int[] widths = calculateMaxWidth();

            public void visit(Object element, int row, int column) {
                AntlrRailRoadNode node = (AntlrRailRoadNode) element;
                Rectangle location = node.location;
                location.x = (column + 1) * INITIAL_X + x_offset;
                location.y = (row + 1) * AntlrRailRoadNode.defaultHeight() + (row + 1) * Y_DISTANCE;
                x_offset += Math.max(location.width, widths[column]) + X_DISTANCE;
            }

            public void visitNull(int row, int column) {
                x_offset += widths[column] + X_DISTANCE;
            }

            public void visitRow(int row) {
                x_offset = 0;
            }
        });
    }

    private int[] calculateMaxWidth() {
        final int[] widths = new int[fMatrix.columnCount()];
        AntlrRailRoadMatrixIterator iterator = new AntlrRailRoadMatrixIterator() {

            public void visitNull(int row, int column) {
            }

            public void visitRow(int row) {
            }

            public void visit(Object element, int row, int column) {
                AntlrRailRoadNode node = (AntlrRailRoadNode) element;
                if (node.location.width > widths[column]) {
                    widths[column] = node.location.width;
                }
            }
        };
        fMatrix.iterate(iterator);
        return widths;
    }

    @Override
    public boolean visitBlock(IBlock node) {
        fLevel += 2;
        while (fColumn < fMatrix.columnCount()) {
            if (fMatrix.containsElementAtColumn(fRow, fColumn)) {
                moveX(1);
            } else {
                break;
            }
        }
        remember(node, fColumn, fRow);
        appendNode("block", fRow, fColumn);
        return true;
    }

    @Override
    public void endvisitBlock(IBlock node) {
        for (IStatement statement : node) {
            unremember(statement);
        }
        Point point = unremember(node);
        fRow = point.y;
        appendNode("/block", fRow, fColumn);
        EBNF ebnf = node.getEbnfOperator();
        if (ebnf != EBNF.NONE) {
        }
        fLevel -= 2;
    }

    @Override
    public boolean visitAlternative(IAlternative node) {
        if (node.getNumber() > 0) {
            IBlock block = node.getParent().getAdapter(IBlock.class);
            fColumn = position(block.get(node.getNumber() - 1)).x;
            while (fRow < fMatrix.rowCount()) {
                boolean free = !fMatrix.containsElementAtRow(fRow, fColumn);
                if (free) {
                    break;
                }
                moveY(1);
            }
        }
        remember(node, fColumn, fRow);
        fLevel += 2;
        appendNode("alternative", fRow, fColumn);
        fLevel += 2;
        if (isEmptyAlternative(node)) {
            visitStatement(node);
        }
        return true;
    }

    @Override
    public void endvisitAlternative(IAlternative node) {
        moveY(1);
        fLevel -= 2;
        appendNode("/alternative", fRow, fColumn);
        fLevel -= 2;
    }

    public void visitStatement(IStatement node) {
        boolean alternative = node.getElementKind() == ElementKind.ALTERNATIVE;
        String nodeName = alternative ? "" : node.getElementName();
        while (fMatrix.contains(fRow, fColumn)) {
            moveX(1);
        }
        appendNode("statement", nodeName, fRow, fColumn, true);
        AntlrRailRoadNode rNode = new AntlrRailRoadNode(nodeName, fRow, fColumn);
        fMatrix.set(rNode, fRow, fColumn);
        moveX(1);
    }

    private void appendNode(String node, int row, int column) {
        appendNode(node, node, row, column, false);
    }

    private void appendNode(String node, String name, int row, int column, boolean closeTag) {
        String endTag = closeTag ? "/>" : ">";
        String attrName = name.equals(node) ? "" : " name=\"" + name + "\"";
        String NODE = "<%1$s%2$s row=\"%3$s\" column=\"%4$s\"%5$s\n";
        buffer.append(getIndent(fLevel));
        buffer.append(String.format(NODE, node, attrName, row, column, endTag));
    }

    public void endvisitStatement(IStatement node) {
    }

    @Override
    protected void in(IModelElement element) {
        if (isStatement(element)) visitStatement((IStatement) element);
    }

    @Override
    public boolean visitRewriteBlock(IBlock node) {
        return false;
    }

    @Override
    public boolean visitRewriteAlternative(IAlternative node) {
        return false;
    }

    @Override
    protected void out(IModelElement element) {
        if (isStatement(element)) endvisitStatement((IStatement) element);
    }

    private boolean isStatement(IModelElement element) {
        if (element instanceof IStatement) {
            ElementKind kind = element.getElementKind();
            switch(kind) {
                case BLOCK:
                case ALTERNATIVE:
                case STATEMENT_ACTION:
                case TARGET_ACTION:
                case VARIABLE:
                case ASSIGN_OPERATOR:
                    return false;
            }
            return true;
        }
        return false;
    }

    private boolean isEmptyAlternative(IAlternative alternative) {
        if (alternative.size() == 0) return true;
        if (alternative.size() == 1) {
            IStatement statement = alternative.get(0);
            return statement.getElementKind() == ElementKind.TARGET_ACTION || statement.getElementKind() == ElementKind.STATEMENT_ACTION;
        }
        return false;
    }

    private String getIndent(int level) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < level; i++) buffer.append(" ");
        return buffer.toString();
    }

    private Point position(IStatement statement) {
        return fPositions.get(statement);
    }

    private void remember(IStatement statement, int x, int y) {
        fPositions.put(statement, new Point(x, y));
    }

    private Point unremember(IStatement statement) {
        return fPositions.remove(statement);
    }

    private void moveX(int offset) {
        fColumn += offset;
    }

    private void moveY(int offset) {
        fRow += offset;
    }

    private class DrawDiagram implements AntlrRailRoadMatrixIterator {

        private Figure root;

        private AntlrRailRoadNode lastNode;

        public DrawDiagram() {
            root = new Figure();
            root.setLayoutManager(new XYLayout());
        }

        public void visitNull(int row, int column) {
        }

        public void visitRow(int row) {
            lastNode = null;
        }

        public void visit(Object element, int row, int column) {
            AntlrRailRoadNode node = (AntlrRailRoadNode) element;
            column--;
            if (column >= 0) {
                AntlrRailRoadNode prev = (AntlrRailRoadNode) fMatrix.get(row, column);
                while (prev == null && row >= 0) {
                    row--;
                    prev = (AntlrRailRoadNode) fMatrix.get(row, column);
                }
                if (prev != null) {
                    Polyline polyline = new Polyline();
                    polyline.setLineWidth(2);
                    if (prev.row == node.row) {
                        double y = prev.location.y + (prev.location.height / 2);
                        double x1 = prev.location.x + prev.location.width;
                        double x2 = node.location.x;
                        polyline.addPoint(new Point(x1, y));
                        polyline.addPoint(new Point(x2, y));
                    } else {
                        double y1 = prev.location.y + (prev.location.height / 2);
                        double y2 = node.location.y + (node.location.height / 2);
                        double prevEnd = prev.location.x + prev.location.width;
                        double nextStart = node.location.x;
                        double nextEnd = nextStart + node.location.width;
                        double x1 = prevEnd + (nextStart - prevEnd) / 2;
                        x1 += x1 * 7.5 / 100;
                        double x2 = node.location.x;
                        polyline.addPoint(new Point(x1, y1));
                        polyline.addPoint(new Point(x1, y2));
                        polyline.addPoint(new Point(x2, y2));
                    }
                    root.add(polyline);
                }
            }
            root.add(node, node.location);
        }
    }
}
