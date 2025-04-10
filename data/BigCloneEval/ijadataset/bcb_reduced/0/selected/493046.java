package homura.hde.main.animation;

import homura.hde.core.renderer.Camera;
import homura.hde.core.scene.Geometry;
import homura.hde.core.scene.Node;
import homura.hde.core.scene.batch.GeomBatch;
import homura.hde.core.util.geom.VertMap;
import homura.hde.main.core.scene.ConnectionPoint;
import homura.hde.util.export.InputCapsule;
import homura.hde.util.export.JMEExporter;
import homura.hde.util.export.JMEImporter;
import homura.hde.util.export.OutputCapsule;
import homura.hde.util.export.Savable;
import homura.hde.util.maths.Matrix4f;
import homura.hde.util.maths.Vector3f;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * SkinNode defines a scene node that contains skinned mesh data. A skinned mesh
 * is defined by a Geometry object representing the "skin" that is attached to a
 * skeleton (or a tree of Bones). The orientation, translation of these bones
 * define the position of the skin vertices. These bones can then be driven by
 * an animation system to provide the animation of the skin. SkinNode defines
 * for each vertex of the skin the bone that affects it and the weight
 * (BoneInfluence) of that affect. This allows multiple bones to share a single
 * vertex (although the total weight must add up to 1).
 * 
 * @author Joshua Slack
 * @author Mark Powell
 */
public class SkinNode extends Node implements Savable, BoneChangeListener {

    private static final long serialVersionUID = 1L;

    protected static Vector3f vertex = new Vector3f();

    protected static Vector3f normal = new Vector3f();

    protected boolean recalcBounds = true;

    protected boolean recalcNormals = true;

    protected boolean needsRefresh = true;

    protected Geometry skin = null;

    protected Bone skeleton = null;

    protected ArrayList<BoneInfluence>[][] cache = null;

    protected ArrayList<ConnectionPoint> connectionPoints;

    protected transient boolean newSkeletonAssigned = false;

    protected transient Matrix4f bindMatrix = new Matrix4f();

    /**
     * Empty Constructor to be used internally only.
     */
    public SkinNode() {
        setLastFrustumIntersection(Camera.INSIDE_FRUSTUM);
    }

    /**
     * Constructor creates a new SkinNode object with the supplied name.
     * 
     * @param name
     *            the name of this SkinNode
     */
    public SkinNode(String name) {
        super(name);
    }

    /**
     * getSkin returns the skin (Geometry) that the SkinNode is controlling.
     * 
     * @return the skin of this SkinNode
     */
    public Geometry getSkin() {
        return skin;
    }

    /**
     * setSkin sets the skin that the SkinNode will affect.
     * 
     * @param skin
     *            the skin that this SkinNode will affect.
     */
    public void setSkin(Geometry skin) {
        this.skin = skin;
        attachChild(skin);
    }

    /**
     * returns true if the bounding volume is recalculated on each update, false
     * otherwise. This is true by default.
     * 
     * @return true if bounding volumes will be calculated on each update, false
     *         otherwise.
     */
    public boolean isRecalcBounds() {
        return recalcBounds;
    }

    /**
     * sets whether the bounding volumes should be recalculated on each update
     * or not. True will recalculate the bounding volumes, while false will not.
     * This is true by default.
     * 
     * @param recalcBounds
     *            true to recalculate bounding volumes, false to not.
     */
    public void setRecalcBounds(boolean recalcBounds) {
        this.recalcBounds = recalcBounds;
    }

    /**
     * returns true if the normals are recalculated on each update, false
     * otherwise. This is true by default.
     * 
     * @return true if normals will be calculated on each update, false
     *         otherwise.
     */
    public boolean isRecalcNormals() {
        return recalcNormals;
    }

    /**
     * sets whether the normals should be recalculated on each update or not.
     * True will recalculate the normals, while false will not. This is true by
     * default.
     * 
     * @param recalcNormals
     *            true to recalculate normals, false to not.
     */
    public void setRecalcNormals(boolean recalcNormals) {
        this.recalcNormals = recalcNormals;
    }

    /**
     * addBoneInfluence defines how a vertex will be affected by a bone. This is
     * given with four values, the batch the vertex is found, the index to the
     * vertex in the batch, the index of the bone that has been or will be set
     * via setBones or addBone and the weight that this indexed bone affects the
     * vertex.
     * 
     * @param batch
     *            the batch that contains the vertex to be affected.
     * @param vert
     *            the index to the vertex.
     * @param bone
     *            the bone that affects the vertex.
     * @param weight
     *            the weight that the bone will affect the vertex.
     */
    public void addBoneInfluence(int batch, int vert, Bone bone, float weight) {
        if (weight == 0) return;
        if (cache == null) recreateCache();
        ArrayList<BoneInfluence> infs = cache[batch][vert];
        if (infs == null) {
            infs = new ArrayList<BoneInfluence>(1);
            cache[batch][vert] = infs;
        }
        BoneInfluence i = new BoneInfluence(bone, weight);
        i.boneId = bone.getName();
        if (!infs.contains(i)) infs.add(i);
    }

    public void setAnimation(BoneAnimation anim) {
        if (skeleton != null && skeleton.getAnimationController() != null) {
            skeleton.getAnimationController().setActiveAnimation(anim);
        }
    }

    public void setAnimation(int index) {
        if (skeleton != null && skeleton.getAnimationController() != null) {
            skeleton.getAnimationController().setActiveAnimation(index);
        }
    }

    public void setAnimation(String name) {
        if (skeleton != null && skeleton.getAnimationController() != null) {
            skeleton.getAnimationController().setActiveAnimation(name);
        }
    }

    public String getAnimationString() {
        if (skeleton != null && skeleton.getAnimationController() != null) {
            return skeleton.getAnimationController().getActiveAnimation().getName();
        }
        return null;
    }

    public void addBoneInfluence(int batch, int vert, String boneId, float weight) {
        if (weight == 0) return;
        if (cache == null) {
            recreateCache();
        }
        ArrayList<BoneInfluence> infs = cache[batch][vert];
        if (infs == null) {
            infs = new ArrayList<BoneInfluence>(1);
            cache[batch][vert] = infs;
        }
        BoneInfluence i = new BoneInfluence(null, weight);
        i.boneId = boneId;
        if (!infs.contains(i)) infs.add(i);
    }

    public ConnectionPoint addConnectionPoint(String name, Bone b) {
        ConnectionPoint cp = new ConnectionPoint(name, b);
        if (connectionPoints == null) {
            connectionPoints = new ArrayList<ConnectionPoint>();
        }
        connectionPoints.add(cp);
        this.attachChild(cp);
        return cp;
    }

    public ArrayList<ConnectionPoint> getConnectionPoints() {
        return connectionPoints;
    }

    /**
     * recreateCache initializes the cache of BoneInfluences for use by the skin
     * node.
     */
    @SuppressWarnings("unchecked")
    public void recreateCache() {
        cache = new ArrayList[skin.getBatchCount()][];
        for (int x = 0; x < cache.length; x++) {
            cache[x] = new ArrayList[skin.getBatch(x).getVertexCount()];
        }
    }

    /**
     * updateGeometricState overrides Spatials updateGeometric state to update
     * the skin mesh based on any changes the bones may have undergone. The
     * update is defined by the updateTime, only when that much time has passed
     * will the updateSkin method be called.
     * 
     * @param time
     *            the time that has passed between calls.
     * @param initiator
     *            true if this is the top level being called.
     */
    public void updateGeometricState(float time, boolean initiator) {
        if (newSkeletonAssigned) {
            assignSkeletonBoneInfluences();
        }
        if (skin != null && needsRefresh) {
            updateSkin();
            if (recalcBounds) {
                skin.updateModelBound();
            }
            needsRefresh = false;
        }
        super.updateGeometricState(time, initiator);
    }

    @Override
    public void updateWorldVectors() {
        if (getSkeleton() == null) {
            super.updateWorldVectors();
        } else {
            worldRotation.set(0, 0, 0, 1);
            worldTranslation.zero();
            worldScale.set(1, 1, 1);
            worldTranslation.zero();
        }
    }

    /**
     * normalizeWeights insures that all vertex BoneInfluences equal 1. The total
     * BoneInfluence on a single vertex should be 1 otherwise the position of the
     * vertex will be multiplied.
     */
    public void normalizeWeights() {
        if (cache == null) return;
        for (int batch = cache.length; --batch >= 0; ) {
            normalizeWeights(batch);
        }
    }

    public int getInfluenceCount(int batch) {
        if (cache == null) return 0;
        int rVal = 0;
        for (int vert = cache[batch].length; --vert >= 0; ) {
            ArrayList<BoneInfluence> infs = cache[batch][vert];
            if (infs != null) rVal += infs.size();
        }
        return rVal;
    }

    public void normalizeWeights(int batch) {
        if (cache == null) return;
        for (int vert = cache[batch].length; --vert >= 0; ) {
            ArrayList<BoneInfluence> infs = cache[batch][vert];
            if (infs == null) continue;
            float total = 0;
            for (int x = infs.size(); --x >= 0; ) {
                BoneInfluence influence = infs.get(x);
                total += influence.weight;
            }
            for (int x = infs.size(); --x >= 0; ) {
                BoneInfluence influence = infs.get(x);
                influence.weight /= total;
            }
        }
    }

    public void setSkeleton(Bone b) {
        if (skeleton != null) skeleton.removeBoneListener(this);
        skeleton = b;
        skeleton.addBoneListener(this);
        newSkeletonAssigned = true;
    }

    public Bone getSkeleton() {
        return skeleton;
    }

    public void assignSkeletonBoneInfluences() {
        if (skeleton != null) {
            for (int i = 0; i < cache.length; i++) {
                for (int j = 0; j < cache[i].length; j++) {
                    if (cache[i][j] != null) {
                        for (int k = 0; k < cache[i][j].size(); k++) {
                            cache[i][j].get(k).assignBone(skeleton);
                        }
                    }
                }
            }
        }
        regenInfluenceOffsets();
        normalizeWeights();
        newSkeletonAssigned = false;
    }

    /**
     * regenInfluenceOffsets calculate the offset of a particular vertex from a
     * bone. This allows the bone's rotation to position the vertex in world
     * space. This should only be called a single time during initialization.
     */
    public void regenInfluenceOffsets() {
        if (cache == null) return;
        Vector3f vertex = new Vector3f();
        Vector3f normal = new Vector3f();
        FloatBuffer verts, norms;
        for (int batch = cache.length; --batch >= 0; ) {
            GeomBatch tb = skin.getBatch(batch);
            verts = tb.getVertexBuffer();
            norms = tb.getNormalBuffer();
            verts.clear();
            norms.clear();
            for (int vert = 0, max = cache[batch].length; vert < max; vert++) {
                ArrayList<BoneInfluence> infs = cache[batch][vert];
                vertex.set(verts.get(), verts.get(), verts.get());
                normal.set(norms.get(), norms.get(), norms.get());
                if (infs == null) continue;
                bindMatrix.mult(vertex, vertex);
                if (recalcNormals) {
                    bindMatrix.rotateVect(normal);
                }
                for (int x = infs.size(); --x >= 0; ) {
                    BoneInfluence infl = infs.get(x);
                    if (infl.bone != null) {
                        infl.vOffset = new Vector3f(vertex);
                        infl.bone.bindMatrix.inverseTranslateVect(infl.vOffset);
                        infl.bone.bindMatrix.inverseRotateVect(infl.vOffset);
                        if (recalcNormals) {
                            infl.nOffset = new Vector3f(normal);
                            infl.bone.bindMatrix.inverseRotateVect(infl.nOffset);
                        }
                    }
                }
            }
        }
    }

    /**
     * updateSkin positions the vertices of the skin based on the bones and the
     * BoneInfluences those bones have on the vertices. Each vertex is placed into
     * world space for rendering.
     */
    public synchronized void updateSkin() {
        if (cache == null || skin == null) return;
        FloatBuffer verts, norms;
        for (int batch = cache.length; --batch >= 0; ) {
            GeomBatch tb = skin.getBatch(batch);
            verts = tb.getVertexBuffer();
            norms = tb.getNormalBuffer();
            verts.clear();
            if (recalcNormals) norms.clear();
            tb.setHasDirtyVertices(true);
            for (int vert = 0, max = cache[batch].length; vert < max; vert++) {
                ArrayList<BoneInfluence> infs = cache[batch][vert];
                if (infs == null) continue;
                vertex.zero();
                if (recalcNormals) normal.zero();
                for (int x = infs.size(); --x >= 0; ) {
                    BoneInfluence inf = infs.get(x);
                    if (inf.bone != null) {
                        inf.bone.applyBone(inf, vertex, normal);
                    }
                }
                vertex.multLocal(worldScale);
                if (verts.remaining() > 2) verts.put(vertex.x).put(vertex.y).put(vertex.z);
                if (recalcNormals && norms.remaining() > 2) {
                    norms.put(normal.x).put(normal.y).put(normal.z);
                }
            }
        }
    }

    public ArrayList<BoneInfluence>[][] getCache() {
        return cache;
    }

    public void setCache(ArrayList<BoneInfluence>[][] cache) {
        this.cache = cache;
    }

    public void setBindMatrix(Matrix4f mat) {
        bindMatrix = mat;
    }

    public void batchChange(Geometry geometry, int index1, int index2) {
        if (geometry == skin) {
            ArrayList<BoneInfluence>[] temp1 = cache[index1];
            ArrayList<BoneInfluence>[] temp2 = cache[index2];
            cache[index1] = temp2;
            cache[index2] = temp1;
        }
    }

    public void write(JMEExporter e) throws IOException {
        revertToBind();
        super.write(e);
        OutputCapsule cap = e.getCapsule(this);
        cap.write(recalcBounds, "recalcBounds", true);
        cap.write(recalcNormals, "recalcNormals", true);
        cap.write(skin, "skin", null);
        cap.write(skeleton, "skeleton", null);
        cap.writeSavableArrayListArray2D(cache, "cache", null);
        cap.writeSavableArrayList(connectionPoints, "connectionPoints", null);
    }

    @SuppressWarnings("unchecked")
    public void read(JMEImporter e) throws IOException {
        super.read(e);
        InputCapsule cap = e.getCapsule(this);
        recalcBounds = cap.readBoolean("recalcBounds", true);
        recalcNormals = cap.readBoolean("recalcNormals", true);
        skin = (Geometry) cap.readSavable("skin", null);
        skeleton = (Bone) cap.readSavable("skeleton", null);
        cache = cap.readSavableArrayListArray2D("cache", null);
        connectionPoints = cap.readSavableArrayList("connectionPoints", null);
        regenInfluenceOffsets();
        skin.updateModelBound();
        updateWorldBound();
        if (skeleton != null) skeleton.addBoneListener(this);
    }

    public void revertToBind() {
        if (skeleton != null) skeleton.getRootSkeleton().revertToBind();
        updateSkin();
        bindMatrix.loadIdentity();
    }

    public void boneChanged(BoneChangeEvent e) {
        needsRefresh = true;
    }

    public void remapInfluences(VertMap[] mappings) {
        for (int x = 0; x < mappings.length; x++) {
            remapInfluences(mappings[x], x);
        }
    }

    @SuppressWarnings("unchecked")
    public void remapInfluences(VertMap mappings, int batchIndex) {
        ArrayList<BoneInfluence>[] infls = cache[batchIndex];
        ArrayList<BoneInfluence>[] newInfls = new ArrayList[skin.getBatch(batchIndex).getVertexCount()];
        cache[batchIndex] = newInfls;
        for (int x = 0; x < infls.length; x++) {
            for (int y = 0; y < infls[x].size(); y++) {
                BoneInfluence bi = infls[x].get(y);
                if (bi.bone != null) addBoneInfluence(batchIndex, mappings.getNewIndex(x), bi.bone, bi.weight); else addBoneInfluence(batchIndex, mappings.getNewIndex(x), bi.boneId, bi.weight);
            }
        }
        normalizeWeights(batchIndex);
    }

    @SuppressWarnings("unchecked")
    public void removeBatch(int batchIndex) {
        ArrayList<BoneInfluence>[][] newCache = new ArrayList[skin.getBatchCount()][];
        for (int x = 0; x < cache.length - 1; x++) {
            if (x < batchIndex) newCache[x] = cache[x]; else newCache[x] = cache[x + 1];
        }
        cache = newCache;
    }
}
