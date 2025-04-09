package gov.nih.niaid.bcbb.nexplorer3.server.datamodels.layout;

import gov.nih.niaid.bcbb.nexplorer3.server.datamodels.CDAONodeViewImpl;
import gov.nih.niaid.bcbb.nexplorer3.server.interfaces.CDAONodeView;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.List;

public class LayoutDiagonal extends LayoutBase {

    int numLeaves;

    public void layoutImpl() throws Exception {
        numLeaves = leaves.length;
        double index = 0;
        for (CDAONodeView leaf : leaves) {
            index += getLayoutMult(leaf) / 2;
            leafPosition(leaf, index);
            index += getLayoutMult(leaf) / 2;
            index++;
        }
        branchPosition((CDAONodeView) tree.getRoot());
    }

    @Override
    public void drawLine(Graphics2D g, CDAONodeView p, CDAONodeView c) {
        g.draw(new Line2D.Float(p.getX(), p.getY(), c.getX(), c.getY()));
    }

    protected float branchPosition(CDAONodeView n) throws Exception {
        setAngle(n, 0);
        if (tree.isLeaf(n)) {
            return 0;
        }
        List<CDAONodeViewImpl> children = tree.getChildren(n);
        for (int i = 0; i < children.size(); i++) {
            CDAONodeView child = (CDAONodeView) children.get(i);
            branchPosition(child);
        }
        Collections.sort(children);
        CDAONodeView loChild = (CDAONodeView) Collections.min(children);
        CDAONodeView hiChild = (CDAONodeView) Collections.max(children);
        float stepSize = 1f / (numLeaves);
        float loLeaves = tree.getNumEnclosedLeaves(loChild);
        float hiLeaves = tree.getNumEnclosedLeaves(hiChild);
        float mLeaves = Math.max(loLeaves, hiLeaves);
        float loChildNewY = loChild.getY() + (mLeaves - loLeaves) * stepSize / 2;
        float hiChildNewY = hiChild.getY() - (mLeaves - hiLeaves) * stepSize / 2;
        float unscaledY = (loChildNewY + hiChildNewY) / 2;
        float unscaledX = calcXPosition(n);
        setPosition(n, unscaledX, unscaledY);
        return 0;
    }

    private void leafPosition(CDAONodeView n, double index) throws Exception {
        float yPos = (((float) (index + .5f) / (float) (numLeaves)));
        float xPos = 1;
        xPos = calcXPosition(n);
        setPosition(n, xPos, yPos);
    }

    float xPosForNumEnclosedLeaves(int numLeaves) {
        float asdf = (1 - (float) (numLeaves - 1) / (float) (leaves.length));
        return asdf;
    }
}
