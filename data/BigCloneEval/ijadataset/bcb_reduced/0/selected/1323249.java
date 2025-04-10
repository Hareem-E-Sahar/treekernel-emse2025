package org.jnet.viewer;

import org.jnet.util.Logger;
import org.jnet.g3d.*;
import org.jnet.modelset.ModelSet;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.BitSet;
import javax.vecmath.Point3i;

class RepaintManager implements Serializable {

    static final long serialVersionUID = 1L;

    Viewer viewer;

    FrameRenderer frameRenderer;

    RepaintManager(Viewer viewer) {
        this.viewer = viewer;
        frameRenderer = new FrameRenderer(viewer);
    }

    int currentModelIndex = 0;

    FrameRenderer getFrameRenderer() {
        return frameRenderer;
    }

    /**
   * sets replacement of viewer
   * @param viewer
   */
    public void setViewer(Viewer viewer) {
        this.viewer = viewer;
        frameRenderer.setViewer(viewer);
    }

    void setCurrentModelIndex(int modelIndex) {
        setCurrentModelIndex(modelIndex, true);
    }

    void setCurrentModelIndex(int modelIndex, boolean clearBackgroundModel) {
        int formerModelIndex = currentModelIndex;
        ModelSet modelSet = viewer.getModelSet();
        int modelCount = (modelSet == null ? 0 : modelSet.getModelCount());
        if (modelCount == 1) currentModelIndex = modelIndex = 0; else if (modelIndex < 0 || modelIndex >= modelCount) modelIndex = -1;
        String ids = null;
        boolean isSameSource = false;
        if (currentModelIndex != modelIndex) {
            if (modelCount > 0) {
                boolean fromDataFrame = viewer.isJnetDataFrame(currentModelIndex);
                if (fromDataFrame) viewer.setJnetDataFrame(null, -1, currentModelIndex);
                if (currentModelIndex != -1) viewer.saveModelOrientation();
                if (fromDataFrame || viewer.isJnetDataFrame(modelIndex)) {
                    ids = viewer.getJnetFrameType(modelIndex) + viewer.getJnetFrameType(currentModelIndex);
                    isSameSource = (viewer.getJnetDataSourceFrame(modelIndex) == viewer.getJnetDataSourceFrame(currentModelIndex));
                }
            }
            currentModelIndex = modelIndex;
            if (ids != null) {
                viewer.restoreModelOrientation(modelIndex);
                if (isSameSource && ids.indexOf("quaternion") >= 0 && ids.indexOf("ramachandran") < 0) viewer.restoreModelRotation(formerModelIndex);
            }
        }
        viewer.setTrajectory(currentModelIndex);
        if (currentModelIndex == -1 && clearBackgroundModel) setBackgroundModelIndex(-1);
        viewer.setTainted(true);
        setFrameRangeVisible();
        if (modelSet != null) {
            setStatusFrameChanged();
            if (!viewer.getSelectAllModels()) viewer.setSelectionSubset(viewer.getModelNodeBitSet(currentModelIndex, false));
        }
    }

    private void setStatusFrameChanged() {
        viewer.setStatusFrameChanged(animationOn ? -2 - currentModelIndex : currentModelIndex);
    }

    int backgroundModelIndex = -1;

    void setBackgroundModelIndex(int modelIndex) {
        ModelSet modelSet = viewer.getModelSet();
        if (modelSet == null || modelIndex < 0 || modelIndex >= modelSet.getModelCount()) modelIndex = -1;
        backgroundModelIndex = modelIndex;
        if (modelIndex >= 0) viewer.setTrajectory(modelIndex);
        viewer.setTainted(true);
        setFrameRangeVisible();
    }

    private BitSet bsVisibleFrames = new BitSet();

    BitSet getVisibleFramesBitSet() {
        return bsVisibleFrames;
    }

    private void setFrameRangeVisible() {
        bsVisibleFrames.clear();
        if (backgroundModelIndex >= 0) bsVisibleFrames.set(backgroundModelIndex);
        if (currentModelIndex >= 0) {
            bsVisibleFrames.set(currentModelIndex);
            return;
        }
        if (frameStep == 0) return;
        int nDisplayed = 0;
        int frameDisplayed = 0;
        for (int i = firstModelIndex; i != lastModelIndex; i += frameStep) if (!viewer.isJnetDataFrame(i)) {
            bsVisibleFrames.set(i);
            nDisplayed++;
            frameDisplayed = i;
        }
        if (firstModelIndex == lastModelIndex || !viewer.isJnetDataFrame(lastModelIndex) || nDisplayed == 0) {
            bsVisibleFrames.set(lastModelIndex);
            if (nDisplayed == 0) firstModelIndex = lastModelIndex;
            nDisplayed = 0;
        }
        if (nDisplayed == 1 && currentModelIndex < 0) setCurrentModelIndex(frameDisplayed);
    }

    AnimationThread animationThread;

    boolean inMotion = false;

    void setInMotion(boolean inMotion) {
        this.inMotion = inMotion;
    }

    int holdRepaint = 0;

    boolean repaintPending;

    void pushHoldRepaint() {
        ++holdRepaint;
    }

    void popHoldRepaint() {
        --holdRepaint;
        if (holdRepaint <= 0) {
            holdRepaint = 0;
            repaintPending = true;
            viewer.repaint();
        }
    }

    boolean refresh() {
        if (repaintPending) return false;
        repaintPending = true;
        if (holdRepaint == 0) {
            viewer.repaint();
        }
        return true;
    }

    synchronized void requestRepaintAndWait() {
        viewer.repaint();
        try {
            wait();
        } catch (InterruptedException e) {
        }
    }

    synchronized void repaintDone() {
        repaintPending = false;
        notify();
    }

    void render(Graphics3D g3d, ModelSet modelSet) {
        frameRenderer.render(g3d, modelSet);
        Rectangle band = viewer.getRubberBandSelection();
        Rectangle navigator = viewer.getNavigatorBand();
        if (band != null && g3d.setColix(viewer.getColixRubberband())) {
            if (viewer.getRubberDrawType() == JnetConstants.DRAW_CIRCLE) g3d.drawCircleCentered(viewer.getColixRubberband(), (int) Math.hypot(band.width, band.height) * 2, band.x, band.y, 0, false); else g3d.drawUnfilledRect(band.x, band.y, 0, 0, band.width, band.height);
        }
        if (navigator != null && g3d.setColix(Graphics3D.BLACK) && viewer.getFreezing() && navigator.x != Integer.MAX_VALUE) {
            g3d.drawUnfilledRect(navigator.x, navigator.y, 0, 0, navigator.width, navigator.height);
            g3d.drawUnfilledRect(navigator.x - 1, navigator.y - 1, 0, 0, navigator.width + 2, navigator.height + 2);
        }
    }

    String generateOutput(String type, Graphics3D g3d, ModelSet modelSet, String fileName) {
        return frameRenderer.generateOutput(type, g3d, modelSet, fileName);
    }

    /****************************************************************
   * Animation support
   ****************************************************************/
    int firstModelIndex;

    int lastModelIndex;

    int frameStep;

    void initializePointers(int frameStep) {
        firstModelIndex = 0;
        int modelCount = viewer.getModelCount();
        lastModelIndex = (frameStep == 0 ? 0 : modelCount) - 1;
        this.frameStep = frameStep;
        viewer.setFrameVariables(firstModelIndex, lastModelIndex);
    }

    void clear() {
        clearAnimation();
        frameRenderer.clear();
    }

    void clearAnimation() {
        setAnimationOn(false);
        setCurrentModelIndex(0);
        setAnimationDirection(1);
        setAnimationFps(10);
        setAnimationReplayMode(0, 0, 0);
        initializePointers(0);
    }

    Hashtable getAnimationInfo() {
        Hashtable info = new Hashtable();
        info.put("firstModelIndex", new Integer(firstModelIndex));
        info.put("lastModelIndex", new Integer(lastModelIndex));
        info.put("animationDirection", new Integer(animationDirection));
        info.put("currentDirection", new Integer(currentDirection));
        info.put("displayModelIndex", new Integer(currentModelIndex));
        info.put("displayModelNumber", viewer.getModelNumberDotted(currentModelIndex));
        info.put("displayModelName", (currentModelIndex >= 0 ? viewer.getModelName(currentModelIndex) : ""));
        info.put("animationFps", new Integer(animationFps));
        info.put("animationReplayMode", getAnimationModeName());
        info.put("firstFrameDelay", new Float(firstFrameDelay));
        info.put("lastFrameDelay", new Float(lastFrameDelay));
        info.put("animationOn", Boolean.valueOf(animationOn));
        info.put("animationPaused", Boolean.valueOf(animationPaused));
        return info;
    }

    String getState(StringBuffer sfunc) {
        int modelCount = viewer.getModelCount();
        if (modelCount < 2) return "";
        StringBuffer commands = new StringBuffer();
        if (sfunc != null) {
            sfunc.append("  _setFrameState;\n");
            commands.append("function _setFrameState();\n");
        }
        commands.append("# frame state;\n");
        commands.append("# modelCount ").append(modelCount).append(";\n# first ").append(viewer.getModelNumberDotted(0)).append(";\n# last ").append(viewer.getModelNumberDotted(modelCount - 1)).append(";\n");
        if (backgroundModelIndex >= 0) StateManager.appendCmd(commands, "set backgroundModel " + viewer.getModelNumberDotted(backgroundModelIndex));
        StateManager.appendCmd(commands, "frame RANGE " + viewer.getModelNumberDotted(firstModelIndex) + " " + viewer.getModelNumberDotted(lastModelIndex));
        StateManager.appendCmd(commands, "animation DIRECTION " + (animationDirection == 1 ? "+1" : "-1"));
        StateManager.appendCmd(commands, "animation FPS " + animationFps);
        StateManager.appendCmd(commands, "animation MODE " + getAnimationModeName() + " " + firstFrameDelay + " " + lastFrameDelay);
        StateManager.appendCmd(commands, "frame " + viewer.getModelNumberDotted(currentModelIndex));
        StateManager.appendCmd(commands, "animation " + (!animationOn ? "OFF" : currentDirection == 1 ? "PLAY" : "PLAYREV"));
        if (animationOn && animationPaused) StateManager.appendCmd(commands, "animation PAUSE");
        if (sfunc != null) commands.append("end function;\n\n");
        return commands.toString();
    }

    int animationDirection = 1;

    int currentDirection = 1;

    void setAnimationDirection(int animationDirection) {
        this.animationDirection = animationDirection;
    }

    int animationFps;

    void setAnimationFps(int animationFps) {
        this.animationFps = animationFps;
    }

    static final int ANIMATION_ONCE = 0;

    static final int ANIMATION_LOOP = 1;

    static final int ANIMATION_PALINDROME = 2;

    int animationReplayMode = 0;

    float firstFrameDelay, lastFrameDelay;

    int firstFrameDelayMs, lastFrameDelayMs;

    void setAnimationReplayMode(int animationReplayMode, float firstFrameDelay, float lastFrameDelay) {
        this.firstFrameDelay = firstFrameDelay > 0 ? firstFrameDelay : 0;
        firstFrameDelayMs = (int) (this.firstFrameDelay * 1000);
        this.lastFrameDelay = lastFrameDelay > 0 ? lastFrameDelay : 0;
        lastFrameDelayMs = (int) (this.lastFrameDelay * 1000);
        if (animationReplayMode >= ANIMATION_ONCE && animationReplayMode <= ANIMATION_PALINDROME) this.animationReplayMode = animationReplayMode; else Logger.error("invalid animationReplayMode:" + animationReplayMode);
    }

    void setAnimationRange(int framePointer, int framePointer2) {
        int modelCount = viewer.getModelCount();
        if (framePointer < 0) framePointer = 0;
        if (framePointer2 < 0) framePointer2 = modelCount;
        if (framePointer >= modelCount) framePointer = modelCount - 1;
        if (framePointer2 >= modelCount) framePointer2 = modelCount - 1;
        firstModelIndex = framePointer;
        lastModelIndex = framePointer2;
        frameStep = (framePointer2 < framePointer ? -1 : 1);
        rewindAnimation();
    }

    boolean animationOn = false;

    private void animationOn(boolean TF) {
        animationOn = TF;
        viewer.setBooleanProperty("_animating", TF);
    }

    boolean animationPaused = false;

    void setAnimationOn(boolean animationOn) {
        if (!animationOn || !viewer.haveModelSet()) {
            setAnimationOff(false);
            return;
        }
        viewer.refresh(3, "Viewer:setAnimationOn");
        setAnimationRange(-1, -1);
        resumeAnimation();
    }

    void setAnimationOff(boolean isPaused) {
        if (animationThread != null) {
            animationThread.interrupt();
            animationThread = null;
        }
        animationPaused = isPaused;
        viewer.refresh(3, "Viewer:setAnimationOff");
        animationOn(false);
        setStatusFrameChanged();
    }

    void pauseAnimation() {
        setAnimationOff(true);
    }

    void reverseAnimation() {
        currentDirection = -currentDirection;
        if (!animationOn) resumeAnimation();
    }

    int intAnimThread = 0;

    void resumeAnimation() {
        if (currentModelIndex < 0) setAnimationRange(firstModelIndex, lastModelIndex);
        if (viewer.getModelCount() <= 1) {
            animationOn(false);
            return;
        }
        animationOn(true);
        animationPaused = false;
        if (animationThread == null) {
            intAnimThread++;
            animationThread = new AnimationThread(firstModelIndex, lastModelIndex, intAnimThread);
            animationThread.start();
        }
    }

    boolean setAnimationNext() {
        return setAnimationRelative(animationDirection);
    }

    void setAnimationLast() {
        setCurrentModelIndex(animationDirection > 0 ? lastModelIndex : firstModelIndex);
    }

    void rewindAnimation() {
        setCurrentModelIndex(animationDirection > 0 ? firstModelIndex : lastModelIndex);
        currentDirection = 1;
        viewer.setFrameVariables(firstModelIndex, lastModelIndex);
    }

    boolean setAnimationPrevious() {
        return setAnimationRelative(-animationDirection);
    }

    boolean setAnimationRelative(int direction) {
        int frameStep = this.frameStep * direction * currentDirection;
        int modelIndexNext = currentModelIndex + frameStep;
        boolean isDone = (modelIndexNext > firstModelIndex && modelIndexNext > lastModelIndex || modelIndexNext < firstModelIndex && modelIndexNext < lastModelIndex);
        if (isDone) {
            switch(animationReplayMode) {
                case ANIMATION_ONCE:
                    return false;
                case ANIMATION_LOOP:
                    modelIndexNext = (animationDirection == currentDirection ? firstModelIndex : lastModelIndex);
                    break;
                case ANIMATION_PALINDROME:
                    currentDirection = -currentDirection;
                    modelIndexNext -= 2 * frameStep;
            }
        }
        int nModels = viewer.getModelCount();
        if (modelIndexNext < 0 || modelIndexNext >= nModels) return false;
        setCurrentModelIndex(modelIndexNext);
        return true;
    }

    String getAnimationModeName() {
        switch(animationReplayMode) {
            case ANIMATION_LOOP:
                return "LOOP";
            case ANIMATION_PALINDROME:
                return "PALINDROME";
            default:
                return "ONCE";
        }
    }

    class AnimationThread extends Thread implements Runnable {

        final int framePointer;

        final int framePointer2;

        int intThread;

        AnimationThread(int framePointer, int framePointer2, int intAnimThread) {
            this.framePointer = framePointer;
            this.framePointer2 = framePointer2;
            this.setName("AnimationThread");
            intThread = intAnimThread;
        }

        public void run() {
            long timeBegin = System.currentTimeMillis();
            int targetTime = 0;
            int sleepTime;
            if (Logger.debugging) Logger.debug("animation thread " + intThread + " running");
            requestRepaintAndWait();
            try {
                sleepTime = targetTime - (int) (System.currentTimeMillis() - timeBegin);
                if (sleepTime > 0) Thread.sleep(sleepTime);
                boolean isFirst = true;
                while (!isInterrupted() && animationOn) {
                    if (currentModelIndex == framePointer) {
                        targetTime += firstFrameDelayMs;
                        sleepTime = targetTime - (int) (System.currentTimeMillis() - timeBegin);
                        if (sleepTime > 0) Thread.sleep(sleepTime);
                    }
                    if (currentModelIndex == framePointer2) {
                        targetTime += lastFrameDelayMs;
                        sleepTime = targetTime - (int) (System.currentTimeMillis() - timeBegin);
                        if (sleepTime > 0) Thread.sleep(sleepTime);
                    }
                    if (!isFirst && !repaintPending && !setAnimationNext()) {
                        Logger.debug("animation thread " + intThread + " exiting");
                        setAnimationOff(false);
                        return;
                    }
                    isFirst = false;
                    targetTime += (1000 / animationFps);
                    sleepTime = targetTime - (int) (System.currentTimeMillis() - timeBegin);
                    refresh();
                    sleepTime = targetTime - (int) (System.currentTimeMillis() - timeBegin);
                    if (sleepTime > 0) Thread.sleep(sleepTime);
                }
            } catch (InterruptedException ie) {
                Logger.debug("animation thread interrupted!");
                setAnimationOn(false);
            }
        }
    }
}
