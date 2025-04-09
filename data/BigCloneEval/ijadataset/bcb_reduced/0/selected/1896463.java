package org.xith3d.render.lwjgl;

import java.io.File;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import org.jagatoo.logging.ProfileTimer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.openmali.types.twodee.Rect2i;
import org.xith3d.picking.PickRequest;
import org.xith3d.render.BackgroundRenderPass;
import org.xith3d.render.CanvasPeer;
import org.xith3d.render.Clipper;
import org.xith3d.render.ClipperInfo;
import org.xith3d.render.OpenGLCapabilities;
import org.xith3d.render.OpenGLStatesCache;
import org.xith3d.render.OpenGlExtensions;
import org.xith3d.render.RenderOptions;
import org.xith3d.render.RenderPass;
import org.xith3d.render.RenderPassConfig;
import org.xith3d.render.RenderPeer;
import org.xith3d.render.ScissorRect;
import org.xith3d.render.StateUnitPeerRegistry;
import org.xith3d.render.preprocessing.RenderAtom;
import org.xith3d.render.preprocessing.RenderBin;
import org.xith3d.render.preprocessing.RenderBinProvider;
import org.xith3d.scenegraph.RenderingAttributes;
import org.xith3d.scenegraph.Transform3D;
import org.xith3d.scenegraph.View;
import org.xith3d.scenegraph._SG_PrivilegedAccess;
import org.xith3d.scenegraph.View.CameraMode;
import org.xith3d.utility.logging.X3DLog;
import org.xith3d.utility.screenshots.ScreenshotCreator;

/**
 * RenderPeer implementation base for LWJGL.
 * 
 * @author David Yazel
 * @author Marvin Froehlich (aka Qudus)
 */
class RenderPeerImpl extends RenderPeer {

    private final FloatBuffer gluPickMatrixBuffer = BufferUtils.createFloatBuffer(16);

    private final DoubleBuffer planeBuffer = BufferUtils.createDoubleBuffer(4);

    private ScissorRect lastScissorBox = null;

    private int lastClipperId = -1;

    private int activeClipperId = -1;

    private RenderOptions effectiveRenderOptions = new RenderOptions();

    private final RenderTargetPeer renderTargetPeer = new RenderTargetPeer(this);

    private final ShadowRenderPeer shadowPeer = new ShadowRenderPeer(this);

    private ScreenshotCreator scheduledShotCreator = null;

    private ScreenshotCreator shotCreator = null;

    public RenderPeerImpl(CanvasPeerImplBase canvasPeer, StateUnitPeerRegistry shaderRegistry, OpenGLStatesCache statesCache, RenderOptions renderOptions) {
        super(canvasPeer, shaderRegistry, statesCache, renderOptions);
    }

    public RenderPeerImpl(CanvasPeerImplBase canvasPeer, StateUnitPeerRegistry shaderRegistry, OpenGLStatesCache statesCache) {
        this(canvasPeer, shaderRegistry, statesCache, new RenderOptions());
    }

    protected static void setGCRequested() {
        setGCRequested(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCanvasPeer(CanvasPeer canvasPeer) {
        super.setCanvasPeer(canvasPeer);
    }

    public CanvasPeerImplBase getCanvasPeerBase() {
        return ((CanvasPeerImplBase) getCanvasPeer());
    }

    private final FloatBuffer defaultLightColorArray = BufferUtils.createFloatBuffer(4);

    {
        defaultLightColorArray.put(0.2f).put(0.2f).put(0.2f).put(1.0f).flip();
    }

    public final void setDefaultLightColor(float r, float g, float b, float a) {
        defaultLightColorArray.put(r).put(g).put(b).put(a).flip();
    }

    public final float[] getClearColor() {
        return (clearColor);
    }

    @SuppressWarnings("unused")
    private final void drawFloor(OpenGLStatesCache statesCache) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_CULL_FACE);
        statesCache.cullFaceEnabled = false;
        GL11.glDisable(GL11.GL_LIGHTING);
        statesCache.lightingEnabled = false;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        statesCache.texture2DEnabled[statesCache.currentServerTextureUnit] = false;
        GL11.glDisable(GL12.GL_TEXTURE_3D);
        statesCache.texture3DEnabled[statesCache.currentServerTextureUnit] = false;
        GL11.glDisable(GL11.GL_BLEND);
        statesCache.blendingEnabled = false;
        GL11.glDepthMask(false);
        GL11.glColor3f(0.2f, 0.2f, 0.7f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(-200.0f, 0f, -200.0f);
        GL11.glVertex3f(-200.0f, 0f, +200.0f);
        GL11.glVertex3f(+200.0f, 0f, +200.0f);
        GL11.glVertex3f(+200.0f, 0f, -200.0f);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glDepthMask(statesCache.depthWriteMask);
    }

    private final void finishScissors(OpenGLStatesCache statesCache) {
        {
            if (!statesCache.enabled || statesCache.scissorTestEnabled) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                statesCache.scissorTestEnabled = false;
            }
        }
        lastScissorBox = null;
    }

    private final void startScissors(OpenGLStatesCache statesCache, ScissorRect scissorBox) {
        if ((scissorBox == null) && ((lastScissorBox != null) || (GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)))) {
            finishScissors(statesCache);
            return;
        }
        if (scissorBox != null) {
            final boolean ok;
            if (!scissorBox.equals(lastScissorBox)) {
                scissorBox.clamp(getCanvasPeerBase().getCurrentViewport());
                ok = scissorBox.check(getCanvasPeerBase().getCurrentViewport());
                if (ok) GL11.glScissor(scissorBox.getX(), scissorBox.getY(), scissorBox.getWidth(), scissorBox.getHeight());
            } else ok = true;
            if (ok && ((lastScissorBox == null) || (!statesCache.enabled || !statesCache.scissorTestEnabled))) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                statesCache.scissorTestEnabled = true;
            }
            lastScissorBox = scissorBox;
        }
    }

    public static final int translateClipPlaneIndex(int index) {
        switch(index) {
            case 0:
                return (GL11.GL_CLIP_PLANE0);
            case 1:
                return (GL11.GL_CLIP_PLANE1);
            case 2:
                return (GL11.GL_CLIP_PLANE2);
            case 3:
                return (GL11.GL_CLIP_PLANE3);
            case 4:
                return (GL11.GL_CLIP_PLANE4);
            case 5:
                return (GL11.GL_CLIP_PLANE5);
            default:
                throw new Error("unknown clip plane " + index);
        }
    }

    private final void finishClipper(OpenGLStatesCache statesCache) {
        for (int i = 0; i < 6; i++) {
            if (!statesCache.enabled || statesCache.clipPlaneEnabled[i]) {
                GL11.glDisable(translateClipPlaneIndex(i));
                statesCache.clipPlaneEnabled[i] = false;
            }
        }
        activeClipperId = -1;
    }

    private final void startClipper(OpenGLStatesCache statesCache, ClipperInfo info, View view) {
        final Clipper clipper = (info == null) ? null : info.getClipper();
        if ((clipper != null) && (clipper.isEnabled())) {
            if (clipper.getId() == activeClipperId) return;
            activeClipperId = clipper.getId();
            if (lastClipperId != clipper.getId()) {
                GL11.glPushMatrix();
                GL11.glLoadMatrix(_SG_PrivilegedAccess.getFloatBuffer(view.getModelViewTransform(false), false));
                if (!clipper.isWorldCoordinateSystemUsed()) {
                    final Transform3D modelView = info.getModelView();
                    if (modelView != null) {
                        GL11.glMultMatrix(_SG_PrivilegedAccess.getFloatBuffer(modelView, true));
                    }
                }
            }
            for (int i = 0; i < 6; i++) {
                final int glI = translateClipPlaneIndex(i);
                if (clipper.isPlaneEnabled(i)) {
                    if (lastClipperId != clipper.getId()) {
                        planeBuffer.clear();
                        planeBuffer.put(clipper.getPlane(i).getA());
                        planeBuffer.put(clipper.getPlane(i).getB());
                        planeBuffer.put(clipper.getPlane(i).getC());
                        planeBuffer.put(clipper.getPlane(i).getD());
                        planeBuffer.rewind();
                        GL11.glClipPlane(glI, planeBuffer);
                    }
                    if (!statesCache.enabled || !statesCache.clipPlaneEnabled[i]) {
                        GL11.glEnable(glI);
                        statesCache.clipPlaneEnabled[i] = true;
                    }
                } else {
                    if (!statesCache.enabled || statesCache.clipPlaneEnabled[i]) {
                        GL11.glDisable(glI);
                        statesCache.clipPlaneEnabled[i] = false;
                    }
                }
            }
            if (lastClipperId != clipper.getId()) {
                GL11.glPopMatrix();
                lastClipperId = clipper.getId();
            }
        } else {
            finishClipper(statesCache);
        }
    }

    private final int drawBin(OpenGLStatesCache statesCache, OpenGLCapabilities glCaps, RenderOptions options, boolean isScissorEnabled, boolean isClipperEnabled, RenderBin bin, View view, long frameId, int nameOffset, long nanoTime, long nanoStep, RenderMode renderMode) {
        final CanvasPeer canvasPeer = getCanvasPeer();
        int triangles = 0;
        final int n = bin.size();
        for (int i = 0; i < n; i++) {
            RenderAtom<?> atom = bin.getAtom(i);
            try {
                if (renderMode == RenderMode.PICKING) GL11.glPushName(nameOffset + i);
                if (isScissorEnabled) startScissors(statesCache, atom.getScissorRect());
                if (isClipperEnabled) startClipper(statesCache, atom.getClipper(), view);
                triangles += this.renderAtom(atom, null, canvasPeer, glCaps, statesCache, view, options, nanoTime, nanoStep, renderMode, frameId);
            } catch (Throwable e) {
                e.printStackTrace();
                System.exit(0);
            } finally {
                if (renderMode == RenderMode.PICKING) {
                    GL11.glPopName();
                }
            }
        }
        if (isClipperEnabled) finishClipper(statesCache);
        return (triangles);
    }

    private final void setColorMask(int colorMask, OpenGLStatesCache statesCache) {
        if (statesCache.enabled && colorMask == statesCache.colorWriteMask) return;
        GL11.glColorMask((colorMask & 1) != 0, (colorMask & 2) != 0, (colorMask & 4) != 0, (colorMask & 8) != 0);
        statesCache.colorWriteMask = colorMask;
    }

    /**
     * sets up the projection and model matrices and initializes the screen
     *
     * @param view
     */
    private final void renderStart(OpenGLStatesCache statesCache, int atomsCount, PickRequest pickRequest) {
        super.renderStart(pickRequest);
        getCanvasPeerBase().beforeRenderStart();
        if (pickRequest == null) {
            selectBuffer = null;
            GL11.glRenderMode(GL11.GL_RENDER);
            GL11.glClearDepth(1.0f);
            if ((shotCreator != null) && (shotCreator.getFormat() == ScreenshotCreator.Format.RGBA)) GL11.glClearColor(clearColor[0], clearColor[1], clearColor[2], 1f); else GL11.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
            lastScissorBox = null;
            if (!statesCache.enabled || statesCache.scissorTestEnabled) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                statesCache.scissorTestEnabled = false;
            }
            if (!disableClearBuffer) {
                if (fullOverpaint) GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT); else GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
            }
        } else {
            GL11.glRenderMode(GL11.GL_RENDER);
            GL11.glClearDepth(1.0f);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
            selectBuffer = BufferUtils.createIntBuffer(atomsCount * 4);
            GL11.glSelectBuffer(selectBuffer);
            GL11.glRenderMode(GL11.GL_SELECT);
            GL11.glInitNames();
        }
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, defaultLightColorArray);
        if (OpenGlExtensions.GL_EXT_separate_specular_color) GL11.glLightModeli(GL12.GL_LIGHT_MODEL_COLOR_CONTROL, GL12.GL_SEPARATE_SPECULAR_COLOR);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
    }

    /**
     * Our own functionally equivalent implementation of Mesa-style gluPickMatrix.
     * We need this because of at least on some installations of Linux glu.gluPickMatrix(...) fails 
     * with unknown error (Unexpected signal 11) and JMV exits.
     */
    private final void gluPickMatrix(float x, float y, float width, float height, int[] viewport) {
        final float sx, sy;
        final float tx, ty;
        sx = viewport[2] / width;
        sy = viewport[3] / height;
        tx = (viewport[2] + 2.0f * (viewport[0] - x)) / width;
        ty = (viewport[3] + 2.0f * (viewport[1] - y)) / height;
        gluPickMatrixBuffer.clear();
        gluPickMatrixBuffer.put(sx).put(0.0f).put(0.0f).put(0.0f);
        gluPickMatrixBuffer.put(0.0f).put(sy).put(0.0f).put(0.0f);
        gluPickMatrixBuffer.put(0.0f).put(0.0f).put(1.0f).put(0.0f);
        gluPickMatrixBuffer.put(tx).put(ty).put(0.0f).put(1.0f);
        gluPickMatrixBuffer.flip();
        GL11.glMultMatrix(gluPickMatrixBuffer);
    }

    /**
     * Initializes the OpenGL view
     */
    private final void renderStartView(View view, PickRequest pickRequest) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        if (pickRequest != null) {
            final Rect2i currentViewport = getCanvasPeerBase().getCurrentViewport();
            final int y = getCanvasPeer().getHeight() - currentViewport.getTop() - currentViewport.getHeight();
            final int[] viewport = new int[] { currentViewport.getLeft(), y, currentViewport.getWidth(), currentViewport.getHeight() };
            gluPickMatrix(pickRequest.getMouseX(), viewport[3] - pickRequest.getMouseY(), 1, 1, viewport);
        }
        GL11.glMultMatrix(_SG_PrivilegedAccess.getFloatBuffer(view.getProjection(), true));
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    /**
     * Sets the current GL matrix to the view adjusting it depending
     * on the mode.
     * @param view
     * @param mode VIEW_NORMAL will use the standard view, VIEW_FIXED_POSITION
     *             will use only the rotational component of the standard view 
     *             (the position is left as the identity), VIEW_FIXED sets the view
     *             to the identity matrix
     */
    private final void setGLModelViewMatrix(View view, View.CameraMode mode) {
        if (mode == null) mode = CameraMode.VIEW_NORMAL;
        GL11.glLoadMatrix(_SG_PrivilegedAccess.getFloatBuffer(view.getModelViewTransform(mode, true), true));
    }

    /**
     * Render the frame using the definition provided in the atomsCollector object.
     * 
     * @throws Throwable
     */
    private final int renderMain(OpenGLCapabilities glCaps, OpenGLStatesCache statesCache, RenderOptions options, boolean isScissorEnabled, boolean isClipperEnabled, View view, RenderPass renderPass, long frameId, int nameOffset, long nanoTime, long nanoStep, RenderMode renderMode) throws Throwable {
        X3DLog.debug("Rendering ", 2, " bins");
        int triangles = 0;
        ProfileTimer.startProfile(X3DLog.LOG_CHANNEL, "CanvasPeerImpl::Drawing Main Scene");
        final RenderBinProvider binProvider = renderPass.getRenderBinProvider();
        if (binProvider.getOpaqueBin().size() > 0) {
            if (!statesCache.enabled || !statesCache.depthTestEnabled) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                statesCache.depthTestEnabled = true;
            }
            triangles += drawBin(statesCache, glCaps, options, isScissorEnabled, isClipperEnabled, binProvider.getOpaqueBin(), view, frameId, nameOffset, nanoTime, nanoStep, renderMode);
            nameOffset += binProvider.getOpaqueBin().size();
        }
        if (binProvider.getTransparentBin().size() > 0) {
            triangles += drawBin(statesCache, glCaps, options, isScissorEnabled, isClipperEnabled, binProvider.getTransparentBin(), view, frameId, nameOffset, nanoTime, nanoStep, renderMode);
            nameOffset += binProvider.getTransparentBin().size();
        }
        ProfileTimer.endProfile();
        if (!statesCache.enabled || !statesCache.depthTestEnabled) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            statesCache.depthTestEnabled = true;
        }
        if ((renderMode != RenderMode.PICKING) && ((shotCreator == null) || (shotCreator.getFormat() == ScreenshotCreator.Format.RGB))) {
            triangles += shadowPeer.drawShadows(view, renderPass.getShadowCasterLight(), binProvider.getShadowsBin(), frameId);
        }
        getCanvasPeer().addTriangles(triangles);
        return (nameOffset);
    }

    /**
     * Stop picking mode and convert select buffer to PickRenderResult[] in addition to the default renderDone() action
     */
    private Object renderDone(List<RenderPass> renderPasses, PickRequest pickRequest, OpenGLStatesCache statesCache, long frameId) {
        if (!statesCache.enabled || !statesCache.depthWriteMask) {
            GL11.glDepthMask(true);
            statesCache.depthWriteMask = true;
        }
        if (!statesCache.enabled || statesCache.colorWriteMask != RenderingAttributes.COLOR_WRITE_MASK_ALL) {
            GL11.glColorMask(true, true, true, true);
            statesCache.colorWriteMask = RenderingAttributes.COLOR_WRITE_MASK_ALL;
        }
        super.renderDone(frameId);
        if (pickRequest != null) {
            GL11.glFlush();
            return (convertSelectBuffer(GL11.glRenderMode(GL11.GL_RENDER), renderPasses, pickRequest.getPickAll()));
        }
        return (null);
    }

    public final int renderRenderPass(Object glObj, OpenGLCapabilities glCaps, OpenGLStatesCache statesCache, List<RenderPass> renderPasses, RenderPass renderPass, final int rpIndex, boolean isRenderTargetMode, RenderMode renderMode, View view, boolean layeredMode, long frameId, long nanoTime, long nanoStep, PickRequest pickRequest, int nameOffset) throws Throwable {
        final RenderPassConfig passConfig = renderPass.getConfig();
        if (pickRequest == null) {
            renderPass.getRenderCallbackNotifier().notifyBeforeRenderPassIsRendered(renderPass, getCanvasPeer().getType(), glObj);
        }
        if ((passConfig == null) || (passConfig.getColorMask() == -1)) setColorMask(colorMask, statesCache); else setColorMask(passConfig.getColorMask(), statesCache);
        _SG_PrivilegedAccess.set(view, true, passConfig);
        setGLModelViewMatrix(view, (passConfig == null) ? null : passConfig.getCameraMode());
        if (renderPass.isEnabled()) {
            if (passConfig != null) {
                if (passConfig.getRenderOptions() != null) effectiveRenderOptions.loadOptions(passConfig.getRenderOptions()); else effectiveRenderOptions.loadOptions(this.getRenderOptions());
                if (isRenderTargetMode && (renderMode != RenderMode.SHADOW_MAP_GENERATION)) getCanvasPeerBase().updateViewport(passConfig.getViewport());
            } else {
                effectiveRenderOptions.loadOptions(this.getRenderOptions());
                if (isRenderTargetMode && (renderMode != RenderMode.SHADOW_MAP_GENERATION)) getCanvasPeerBase().updateViewport(null);
            }
            statesCache.enabled = effectiveRenderOptions.isGLStatesCacheEnabled();
            view.getFrustum((getCanvasPeerBase().getCurrentViewport() == null) ? getCanvasPeer().getCanvas3D() : getCanvasPeerBase().getCurrentViewport());
            if (isRenderTargetMode && (renderPass.getRenderTarget() != null)) {
                renderPass.getRenderCallbackNotifier().notifyBeforeRenderTargetIsActivated(renderPass, renderPass.getRenderTarget(), getCanvasPeer().getType(), glObj);
                renderTargetPeer.setupRenderTarget(glCaps, statesCache, getCanvasPeer(), renderPass.getRenderTarget());
                renderPass.getRenderCallbackNotifier().notifyAfterRenderTargetIsActivated(renderPass, renderPass.getRenderTarget(), getCanvasPeer().getType(), glObj);
            }
            renderStartView(view, pickRequest);
            if (isRenderTargetMode && (renderPass.getRenderTarget() != null) && renderPass.getRenderTarget().isBackgroundRenderingEnabled()) {
                for (int i = 0; i < renderPasses.size(); i++) {
                    final RenderPass bgPass = renderPasses.get(i);
                    if (bgPass instanceof BackgroundRenderPass) {
                        nameOffset += renderRenderPass(glObj, glCaps, statesCache, renderPasses, bgPass, i, false, renderMode, view, layeredMode, frameId, nanoTime, nanoStep, pickRequest, nameOffset);
                    }
                }
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                setGLModelViewMatrix(view, passConfig.getCameraMode());
                view.getFrustum(getCanvasPeerBase().getCurrentViewport());
                renderStartView(view, pickRequest);
            }
            if ((layeredMode && !renderPass.isUnlayeredModeForced()) || (!layeredMode && renderPass.isLayeredModeForced())) {
                if (rpIndex > 0) GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            }
            if (pickRequest == null) {
                renderPass.getRenderCallbackNotifier().notifyAfterRenderPassIsSetUp(renderPass, getCanvasPeer().getType(), glObj);
            }
            nameOffset += renderMain(glCaps, statesCache, effectiveRenderOptions, renderPass.isScissorEnabled(), renderPass.isClipperEnabled(), view, renderPass, frameId, nameOffset, nanoTime, nanoStep, renderMode);
            if (isRenderTargetMode && (renderPass.getRenderTarget() != null)) {
                renderPass.getRenderCallbackNotifier().notifyBeforeRenderTargetIsDeactivated(renderPass, renderPass.getRenderTarget(), getCanvasPeer().getType(), glObj);
                renderTargetPeer.finishRenderTarget(renderPass.getRenderTarget());
                renderPass.getRenderCallbackNotifier().notifyAfterRenderTargetIsDeactivated(renderPass, renderPass.getRenderTarget(), getCanvasPeer().getType(), glObj);
            }
        } else if (pickRequest != null) {
            nameOffset += renderPass.getRenderBinProvider().getAtomsCount();
        }
        if ((renderMode == RenderMode.SHADOW_MAP_GENERATION) || ((passConfig != null) && (passConfig.getRenderOptions() != null)) || ((rpIndex + 1 < renderPasses.size()) && (renderPasses.get(rpIndex + 1).getConfig() != null) && (renderPasses.get(rpIndex + 1).getConfig().getRenderOptions() != null))) {
            super.renderDone(frameId);
            super.resetStateUnitStateArrays();
        }
        _SG_PrivilegedAccess.set(view, false, (RenderPassConfig) null);
        if (pickRequest == null) {
            renderPass.getRenderCallbackNotifier().notifyAfterRenderPassCompleted(renderPass, getCanvasPeer().getType(), glObj);
        }
        return (nameOffset);
    }

    /**
     * Does the actual rendering. OpenGL context threading issues must be handled/solved earlier.
     */
    @Override
    public final Object render(Object glObj, View view, List<RenderPass> renderPasses, boolean layeredMode, long frameId, long nanoTime, long nanoStep, PickRequest pickRequest) {
        if (view == null) {
            return (null);
        }
        lastClipperId = -1;
        shotCreator = scheduledShotCreator;
        scheduledShotCreator = null;
        final OpenGLCapabilities glCaps = getCanvasPeer().getOpenGLCapabilities();
        final OpenGLStatesCache statesCache = getStatesCache();
        statesCache.enabled = getRenderOptions().isGLStatesCacheEnabled();
        Object result = null;
        ShapeAtomPeer.reset();
        X3DLog.debug("Starting to render the frame");
        try {
            if ((renderPasses != null) && (renderPasses.size() > 0)) {
                RenderBinProvider firstBinProvider = renderPasses.get(0).getRenderBinProvider();
                renderStart(statesCache, firstBinProvider.getAtomsCount(), pickRequest);
            } else {
                renderStart(statesCache, 0, pickRequest);
            }
            int nameOffset = 0;
            for (int i = 0; i < renderPasses.size(); i++) {
                final RenderPass renderPass = renderPasses.get(i);
                if (renderPass.isEnabled()) {
                    if ((shotCreator == null) || (shotCreator.getFormat() == ScreenshotCreator.Format.RGB)) {
                        nameOffset += shadowPeer.initShadows(view, renderPass.getShadowCasterLight(), renderPass.getRenderBinProvider().getShadowsBin(), frameId);
                    }
                    nameOffset += renderRenderPass(glObj, glCaps, statesCache, renderPasses, renderPass, i, true, (pickRequest == null) ? RenderMode.NORMAL : RenderMode.PICKING, view, layeredMode, frameId, nanoTime, nanoStep, pickRequest, nameOffset);
                }
            }
            result = renderDone(renderPasses, pickRequest, statesCache, frameId);
            if (shotCreator != null) {
                GL11.glReadPixels(0, 0, getCanvasPeer().getWidth(), getCanvasPeer().getHeight(), shotCreator.getFormat().getIntGL(), GL11.GL_UNSIGNED_BYTE, shotCreator.getBuffer());
                shotCreator.createScreenshot();
                shotCreator = null;
            }
        } catch (Throwable terr) {
            X3DLog.print(terr);
            terr.printStackTrace();
            if (terr instanceof RuntimeException) {
                throw (RuntimeException) terr;
            }
            if (terr instanceof Error) {
                throw (Error) terr;
            }
            throw new Error(terr);
        }
        checkGCRequested();
        X3DLog.debug("Done rendering the frame");
        return (result);
    }

    public void clearViewport(float r, float g, float b, float a) {
        GL11.glClearColor(r, g, b, a);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
    }

    public void clearViewport() {
        clearViewport(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Takes a screenshot of the current rendering
     * 
     * @param file the file to save the screenshot to
     * @param alpha with alpha channel?
     */
    @Override
    public final void takeScreenshot(File file, boolean alpha) {
        ScreenshotCreator.Format format;
        if (alpha) format = ScreenshotCreator.Format.RGBA; else format = ScreenshotCreator.Format.RGB;
        this.scheduledShotCreator = new ScreenshotCreator(getCanvasPeer().getWidth(), getCanvasPeer().getHeight(), format, file);
    }
}
