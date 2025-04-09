package barsuift.simLife.j3d.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import barsuift.simLife.Randomizer;
import barsuift.simLife.j3d.Axis;
import barsuift.simLife.j3d.Transform3DState;
import barsuift.simLife.j3d.util.TransformerHelper;

public class TreeLeavesOrganizer {

    public void organizeLeaves(List<TreeLeaf3DState> leavesStates, float branchLength) {
        int nbLeaves = leavesStates.size();
        float shift = branchLength / nbLeaves;
        for (int index = 0; index < nbLeaves; index++) {
            TreeLeaf3DState leaf3DState = leavesStates.get(index);
            Point3f leafAttachPoint = new Point3f(0, (index + (float) Math.random()) * shift, 0);
            Transform3D transform = TransformerHelper.getTranslationTransform3D(new Vector3f(leafAttachPoint));
            Transform3D rotationT3D = TransformerHelper.getRotationTransform3D(Randomizer.randomRotation(), Axis.X);
            transform.mul(rotationT3D);
            leaf3DState.setTransform(new Transform3DState(transform));
        }
    }

    protected Vector3f computeNewLeafTranslation(List<TreeLeaf3D> leaves3D, float branchLength) {
        float previousAttachPoint = 0;
        float saveAttachPoint1 = -1;
        float saveAttachPoint2 = -1;
        float distance;
        float maxDistance = -1;
        List<TreeLeaf3D> sortedLeaves3D = new ArrayList<TreeLeaf3D>(leaves3D);
        Collections.sort(sortedLeaves3D, new TreeLeaf3DComparator());
        for (TreeLeaf3D leaf3D : sortedLeaves3D) {
            float attachPoint = leaf3D.getPosition().getY();
            distance = attachPoint - previousAttachPoint;
            if (distance > maxDistance) {
                maxDistance = distance;
                saveAttachPoint1 = previousAttachPoint;
                saveAttachPoint2 = attachPoint;
            }
            previousAttachPoint = attachPoint;
        }
        distance = branchLength - previousAttachPoint;
        if (distance > maxDistance) {
            maxDistance = distance;
            saveAttachPoint1 = previousAttachPoint;
            saveAttachPoint2 = branchLength;
        }
        float middle = (saveAttachPoint1 + saveAttachPoint2) / 2;
        float random = Randomizer.random1() * maxDistance;
        float newY = middle + random;
        return new Vector3f(0, newY, 0);
    }

    public void placeNewLeaf(TreeLeaf3DState leaf3DState, TreeBranch3D branch3D) {
        Transform3D transform1 = new Transform3D();
        transform1.rotX(Randomizer.randomRotation());
        transform1.setTranslation(computeNewLeafTranslation(branch3D.getLeaves(), branch3D.getLength()));
        Transform3D transform = transform1;
        leaf3DState.setTransform(new Transform3DState(transform));
    }
}
