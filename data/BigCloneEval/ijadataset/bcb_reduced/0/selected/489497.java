package org.jal3d;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import cat.arestorm.math.MutableQuaternion;
import cat.arestorm.math.MutableVector3D;
import cat.arestorm.math.Vector3D;

public final class CoreTrack {

    private int coreBoneId;

    private final Vector<CoreKeyframe> keyframes;

    public CoreTrack(final int boneId, final Collection<CoreKeyframe> keyframes) {
        coreBoneId = boneId;
        this.keyframes = new Vector<CoreKeyframe>(keyframes);
    }

    public CoreTrack() {
        coreBoneId = -1;
        keyframes = new Vector<CoreKeyframe>();
    }

    /**
	 * Returns a specified state.
	 *
	 * This function returns the state (translation and rotation of the core bone)
	 * for the specified time and duration.
	 *
	 * @param time The time in seconds at which the state should be returned.
	 * @param translation A reference to the translation reference that will be
	 *                    filled with the specified state.
	 * @param rotation A reference to the rotation reference that will be filled
	 *                 with the specified state.
	 *
	 * @return One of the following values:<ul>
	 *         <li><strong>true</strong> if successful</li>
 	 *         <li><strong>false</strong> if an error happened</li>
 	 *         </ul>
	 */
    public boolean getState(float time, final MutableVector3D translation, final MutableQuaternion rotation) {
        int intCoreKeyframeBefore;
        int intCoreKeyframeAfter;
        intCoreKeyframeAfter = getUpperBound(time);
        if (intCoreKeyframeAfter == keyframes.size()) {
            --intCoreKeyframeAfter;
            rotation.copy(keyframes.get(intCoreKeyframeAfter).getRotation());
            translation.copy(keyframes.get(intCoreKeyframeAfter).getTranslation());
            return true;
        }
        if (intCoreKeyframeAfter == 0) {
            rotation.copy(keyframes.get(intCoreKeyframeAfter).getRotation());
            translation.copy(keyframes.get(intCoreKeyframeAfter).getTranslation());
            return true;
        }
        intCoreKeyframeBefore = intCoreKeyframeAfter - 1;
        CoreKeyframe coreKeyframeBefore = keyframes.get(intCoreKeyframeBefore);
        CoreKeyframe coreKeyframeAfter = keyframes.get(intCoreKeyframeAfter);
        float blendFactor;
        blendFactor = (time - coreKeyframeBefore.getTime()) / (coreKeyframeAfter.getTime() - coreKeyframeBefore.getTime());
        translation.copy(coreKeyframeBefore.getTranslation());
        translation.blendSelf(blendFactor, coreKeyframeAfter.getTranslation());
        rotation.copy(coreKeyframeBefore.getRotation());
        rotation.blendSelf(blendFactor, coreKeyframeAfter.getRotation());
        return true;
    }

    /** 
	 * Returns the ID of the core bone.
	 *
	 * This function returns the ID of the core bone to which the core track
	 * instance is attached to.
	 *
	 * @return One of the following values:<ul>
	 *         <li>the <strong>ID</strong> of the core bone<li>
	 *         <li><strong>-1</strong> if an error happened<li>
	 *         </ul>
	 */
    public int getCoreBoneId() {
        return coreBoneId;
    }

    /**
	 * Sets the ID of the core bone.
	 *
	 * This function sets the ID of the core bone to which the core track instance
	 * is attached to.
	 *
	 * @param coreBoneId The ID of the bone to which the core track instance should
	 *                   be attached to.
 	 *
 	 * @return One of the following values:<ul>
	 *         <li><strong>true</strong> if successful</li>
 	 *         <li><strong>false</strong> if an error happened</li>
 	 *         </ul>
 	 * @deprecated
	 */
    @Deprecated
    boolean setCoreBoneId(int coreBoneId) {
        if (coreBoneId < 0) {
            return false;
        }
        this.coreBoneId = coreBoneId;
        return true;
    }

    public int getCoreKeyframeCount() {
        return keyframes.size();
    }

    public CoreKeyframe getCoreKeyframe(int idx) {
        return keyframes.get(idx);
    }

    /**
	 * Adds a core keyframe.
	 *
	 * This function adds a core keyframe to the core track instance.
	 *
	 * @param coreKeyframe A pointer to the core keyframe that should be added.
	 *
	 * @return One of the following values:<ul>
	 *         <li><strong>true</strong> if successful</li>
 	 *         <li><strong>false</strong> if an error happened</li>
 	 *         </ul>
 	 * @deprecated
	 */
    @Deprecated
    boolean addCoreKeyframe(CoreKeyframe coreKeyframe) {
        keyframes.add(coreKeyframe);
        int idx = keyframes.size() - 1;
        while (idx > 0 && keyframes.get(idx).getTime() < keyframes.get(idx - 1).getTime()) {
            Collections.swap(keyframes, idx, idx - 1);
            idx--;
        }
        return true;
    }

    @Deprecated
    void removeCoreKeyFrame(int i) {
        keyframes.remove(i);
    }

    /**
	 * Scale the core track.
	 *
	 * This function rescale all the data that are in the core track instance.
	 *
	 * @param factor A float with the scale factor
	 */
    @SuppressWarnings("deprecation")
    public void scale(float factor) {
        for (CoreKeyframe coreKeyframe : keyframes) {
            Vector3D translation = coreKeyframe.getTranslation();
            coreKeyframe.setTranslation(translation.mult(factor));
        }
    }

    private int getUpperBound(float time) {
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
        return upperBound;
    }
}
