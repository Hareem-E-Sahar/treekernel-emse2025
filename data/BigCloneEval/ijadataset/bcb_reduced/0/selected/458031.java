package edu.asu.commons.foraging.jcal3d.core;

import java.util.ListIterator;
import java.util.Vector;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.misc.Error;
import edu.asu.commons.foraging.jcal3d.misc.Quaternion;

public class CoreTrack {

    protected int coreBoneId = -1;

    protected Vector<CoreKeyframe> keyframes = new Vector<CoreKeyframe>();

    public CoreTrack() {
    }

    public boolean addCoreKeyframe(CoreKeyframe coreKeyframe) {
        keyframes.add(coreKeyframe);
        int index = keyframes.size() - 1;
        while (index > 0 && keyframes.get(index).getTime() < keyframes.get(index - 1).getTime()) {
            CoreKeyframe temp = keyframes.get(index);
            keyframes.setElementAt(keyframes.get(index - 1), index);
            keyframes.setElementAt(temp, index - 1);
            --index;
        }
        return true;
    }

    public int getCoreBoneId() {
        return coreBoneId;
    }

    public Vector3D getTranslation(float time) {
        ListIterator<CoreKeyframe> iteratorCoreKeyframeAfter;
        iteratorCoreKeyframeAfter = getUpperBound(time);
        if (!iteratorCoreKeyframeAfter.hasNext()) {
            return keyframes.lastElement().getTranslation();
        }
        if (!iteratorCoreKeyframeAfter.hasPrevious()) {
            return keyframes.firstElement().getTranslation();
        }
        CoreKeyframe coreKeyframeAfter = iteratorCoreKeyframeAfter.next();
        iteratorCoreKeyframeAfter.previous();
        CoreKeyframe coreKeyframeBefore = iteratorCoreKeyframeAfter.previous();
        float blendFactor;
        blendFactor = (time - coreKeyframeBefore.getTime()) / (coreKeyframeAfter.getTime() - coreKeyframeBefore.getTime());
        Vector3D translation = coreKeyframeBefore.getTranslation();
        translation = translation.blend(blendFactor, coreKeyframeAfter.getTranslation());
        return translation;
    }

    public Quaternion getRotation(float time) {
        ListIterator<CoreKeyframe> iteratorCoreKeyframeAfter;
        iteratorCoreKeyframeAfter = getUpperBound(time);
        if (!iteratorCoreKeyframeAfter.hasNext()) {
            return keyframes.lastElement().getRotation();
        }
        if (!iteratorCoreKeyframeAfter.hasPrevious()) {
            return keyframes.firstElement().getRotation();
        }
        CoreKeyframe coreKeyframeAfter = iteratorCoreKeyframeAfter.next();
        iteratorCoreKeyframeAfter.previous();
        CoreKeyframe coreKeyframeBefore = iteratorCoreKeyframeAfter.previous();
        float blendFactor;
        blendFactor = (time - coreKeyframeBefore.getTime()) / (coreKeyframeAfter.getTime() - coreKeyframeBefore.getTime());
        Quaternion rotation = coreKeyframeBefore.getRotation();
        rotation = rotation.blend(blendFactor, coreKeyframeAfter.getRotation());
        return rotation;
    }

    public boolean setCoreBoneId(int coreBoneId) {
        if (coreBoneId < 0) {
            Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
            return false;
        }
        this.coreBoneId = coreBoneId;
        return true;
    }

    public int getCoreKeyframeCount() {
        return keyframes.size();
    }

    public CoreKeyframe getCoreKeyframe(int index) {
        return keyframes.get(index);
    }

    public void scale(float factor) {
        for (int keyframeId = 0; keyframeId < keyframes.size(); keyframeId++) {
            Vector3D translation = keyframes.get(keyframeId).getTranslation();
            translation = (Vector3D) translation.multiply(factor);
            keyframes.get(keyframeId).setTranslation(translation);
        }
    }

    private ListIterator<CoreKeyframe> getUpperBound(float time) {
        int lowerBound = 0;
        int upperBound = keyframes.size() - 1;
        while (lowerBound < upperBound - 1) {
            int middle = (lowerBound + upperBound) / 2;
            if (time >= keyframes.get(middle).getTime()) {
                lowerBound = middle;
            } else {
                upperBound = middle;
            }
        }
        return keyframes.listIterator(upperBound);
    }
}
