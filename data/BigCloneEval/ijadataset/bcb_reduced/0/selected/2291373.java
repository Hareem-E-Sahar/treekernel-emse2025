package j3dworkbench.j3dextensions;

import j3dworkbench.core.ImprovedNoise;
import j3dworkbench.event.AsyncWorker;
import j3dworkbench.event.MessageDispatcher;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Node;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureUnitState;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import com.sun.j3d.utils.scenegraph.io.SceneGraphIO;
import com.sun.j3d.utils.scenegraph.io.SceneGraphObjectReferenceControl;

public final class NoiseTextureAnimator extends ElapsedTimeBehavior implements ImageComponent2D.Updater, SceneGraphIO, AsyncWorker {

    private static final String PLEASE_CANCEL_OR_WAIT = "Please cancel or wait for previous operation to finish, and try again.";

    public static final int NOISE_TYPE_TURBULENCE = 0;

    public static final int NOISE_TYPE_FBM = 1;

    private static final float NOISE_ZOOM_NOMINAL = 17f;

    private boolean fireStencil = false;

    public static final int STENCIL_TYPE_NONE = 0;

    public static final int STENCIL_TYPE_DENSITY = 1;

    public static final int STENCIL_TYPE_ROUNDED = 2;

    private static final float Z_ZOOM_NOMINAL = 32.56f;

    private transient boolean decrement;

    private transient int image_dim = 64;

    public int column = 0, row = 0;

    private int noise_type = NOISE_TYPE_TURBULENCE;

    private int num_ticks = 1;

    private float redAlpha = 0, greenAlpha = 0, blueAlpha = 0;

    private int redMax = 255, greenMax = 192, blueMax = 0;

    private int redMin = 192, greenMin = 0, blueMin = 0;

    private float ridgedGain = 3f;

    public float getRidgedGain() {
        return ridgedGain;
    }

    public void setRidgedGain(float ridgedGain) {
        this.ridgedGain = ridgedGain;
    }

    private final int ridgedOctaves = 2;

    private float ridgedOffset = .77f;

    public float getRidgedOffset() {
        return ridgedOffset;
    }

    public void setRidgedOffset(float offset) {
        ridgedOffset = offset;
    }

    public static final int MIN = 0;

    public static final int MAX = 1;

    public static final int ALPHA = 2;

    private static final int VERSION = 2;

    private static final int VERSION_1 = 1;

    private static final long TIMESLICE = 50;

    private int stencilType = STENCIL_TYPE_NONE;

    private ImageComponent2D imageComp;

    private int textureUnitReference;

    private transient volatile int tickPointer;

    private transient volatile int progressPointer;

    private transient int value, red, green, blue, alpha, alphaPrime;

    private Tuple3d zooms = new Point3d(NOISE_ZOOM_NOMINAL, NOISE_ZOOM_NOMINAL, Z_ZOOM_NOMINAL);

    private transient byte[][][] colorNoise;

    private transient byte[][] stencilNoise;

    private transient volatile boolean working = false;

    private TextureUnitState textureUnit;

    private final Object mutex_noise = new Object();

    private final Object mutex_working = new Object();

    private boolean wasEnable;

    private volatile boolean cancelled;

    private double H = 2;

    private double lacun = 2;

    public double getLacun() {
        return lacun;
    }

    public void setLacun(double lacun) {
        this.lacun = lacun;
        initNoiseAsync();
    }

    public NoiseTextureAnimator() {
        super(25);
    }

    @Override
    public NoiseTextureAnimator cloneNode(boolean dup) {
        NoiseTextureAnimator nta = new NoiseTextureAnimator((int) this.getFPS());
        nta.duplicateNode(this, dup);
        return nta;
    }

    public NoiseTextureAnimator(int p_fps) {
        super(p_fps);
    }

    @Override
    public void duplicateNode(Node original, boolean forceDuplicate) {
        super.duplicateNode(original, forceDuplicate);
        NoiseTextureAnimator nta = (NoiseTextureAnimator) original;
        redAlpha = nta.redAlpha;
        redMax = nta.redMax;
        redMin = nta.redMin;
        blueAlpha = nta.blueAlpha;
        blueMax = nta.blueMax;
        blueMin = nta.blueMin;
        greenAlpha = nta.greenAlpha;
        greenMax = nta.greenMax;
        greenMin = nta.greenMin;
        this.stencilType = nta.stencilType;
        this.fireStencil = nta.fireStencil;
        this.num_ticks = nta.num_ticks;
        this.image_dim = nta.image_dim;
        this.zooms.set(nta.getZooms());
        this.colorNoise = new byte[num_ticks][image_dim][image_dim];
        System.arraycopy(nta.colorNoise, 0, this.colorNoise, 0, nta.colorNoise.length);
        this.stencilNoise = new byte[image_dim][image_dim];
        System.arraycopy(nta.stencilNoise, 0, this.stencilNoise, 0, nta.stencilNoise.length);
        textureUnit = (TextureUnitState) nta.getTextureUnit().cloneNodeComponent(forceDuplicate);
        textureUnit.setCapability(TextureUnitState.ALLOW_STATE_WRITE);
        textureUnit.setTexture(initTexture());
        imageComp.updateData(NoiseTextureAnimator.this, 0, 0, image_dim, image_dim);
    }

    public void createSceneGraphObjectReferences(SceneGraphObjectReferenceControl ref) {
        textureUnitReference = ref.addReference(textureUnit);
    }

    private final ImageComponent2D initImageComponent2D() {
        BufferedImage bi = new BufferedImage(image_dim, image_dim, BufferedImage.TYPE_INT_ARGB);
        imageComp = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bi, true, true);
        imageComp.setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
        imageComp.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);
        return imageComp;
    }

    public final int getWorkLoad() {
        synchronized (mutex_noise) {
            return num_ticks;
        }
    }

    public int getStencilType() {
        return stencilType;
    }

    private final Texture initTexture() {
        Texture texture = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, image_dim, image_dim);
        texture.setCapability(Texture.ALLOW_ENABLE_WRITE);
        texture.setEnable(true);
        texture.setMagFilter(Texture2D.NICEST);
        texture.setMinFilter(Texture2D.NICEST);
        texture.setAnisotropicFilterMode(Texture.ANISOTROPIC_SINGLE_VALUE);
        texture.setCapability(Texture.ALLOW_LOD_RANGE_WRITE);
        texture.setCapability(Texture.ALLOW_ENABLE_WRITE);
        texture.setBoundaryModeS(Texture2D.WRAP);
        texture.setBoundaryModeT(Texture2D.WRAP);
        texture.setImage(0, imageComp = initImageComponent2D());
        return texture;
    }

    public final TextureUnitState getTextureUnit() {
        if (textureUnit != null) return textureUnit;
        textureUnit = new TextureUnitState();
        textureUnit.setCapability(TextureUnitState.ALLOW_STATE_WRITE);
        textureUnit.setTexture(initTexture());
        initNoiseAsync();
        return textureUnit;
    }

    @Override
    public void setEnable(boolean state) {
        if (state && !isInit()) {
            try {
                Thread.sleep(TIMESLICE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isInit()) {
                throw new IllegalStateException("Not initialized");
            }
        }
        if (state && colorNoise == null) {
            initNoiseAsync();
        }
        super.setEnable(state);
        if (state) {
            return;
        }
        try {
            Thread.sleep(TIMESLICE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Tuple3d getZooms() {
        return zooms;
    }

    private final void initColorNoise() {
        switch(noise_type) {
            case NOISE_TYPE_TURBULENCE:
                initTurbulence();
                break;
            case NOISE_TYPE_FBM:
                initFBM();
                break;
        }
    }

    private final int getNumOctaves() {
        int value = (int) Math.ceil((Math.log(image_dim) / Math.log(2.0)));
        return value;
    }

    private final void initDensityStencil() {
        float center = (image_dim + 1) / 2;
        float center2 = center * center;
        float center4 = center2 * center2;
        float center6 = center4 * center2;
        float columnDistance = 0;
        float rowDistance = 0;
        float distance2 = 0;
        int y = 0;
        int opacity = 0;
        for (int x = 0; x < stencilNoise.length; x++) {
            for (y = 0; y < stencilNoise[0].length; y++) {
                columnDistance = (x - center) * (x - center);
                rowDistance = (y - center) * (y - center);
                distance2 = rowDistance + columnDistance;
                if (distance2 > center2) {
                    opacity = 0;
                } else {
                    float distance4 = distance2 * distance2;
                    float distance6 = distance4 * distance2;
                    opacity = (int) (255 * (1 - (4 / 9) * (distance6 / center6) + (17 / 9) * (distance4 / center4) - (22 / 9) * (distance2 / center2)));
                }
                stencilNoise[x][y] = (byte) Math.min(255, opacity);
            }
        }
    }

    private final void initStencil() {
        if (stencilNoise == null) {
            stencilNoise = new byte[image_dim][image_dim];
        }
        switch(stencilType) {
            case STENCIL_TYPE_NONE:
                break;
            case STENCIL_TYPE_DENSITY:
                initDensityStencil();
                break;
            case STENCIL_TYPE_ROUNDED:
                initBorderStencil();
                break;
        }
    }

    private final void initRoundedStencil() {
        int y = 0;
        float noise = 0;
        float dist = 0;
        float value1 = 0;
        int opacity = 0;
        final float image_radius = (image_dim + 1) / 2;
        float weight = 0;
        final float distance2RandomRatio = .75f;
        final float stencilZoom = 50f;
        final Point2f center = new Point2f(image_radius, image_radius);
        final Point2f point = new Point2f();
        float max = (float) Math.sqrt((image_radius * image_radius) * 2);
        for (int x = 0; x < stencilNoise.length; x++) {
            for (y = 0; y < stencilNoise[0].length; y++) {
                noise = (float) ImprovedNoise.fBm(0, x / stencilZoom, y / stencilZoom, 2, 4);
                point.set(x, y);
                dist = center.distance(point);
                if (dist < image_radius - 10) {
                    stencilNoise[x][y] = (byte) 255;
                    continue;
                }
                value1 = (1 - dist / max) * 2f;
                weight = distance2RandomRatio * value1 + (1 - distance2RandomRatio) * noise;
                opacity = (int) (value1 * Math.min(255, (255 * weight)));
                stencilNoise[x][y] = (byte) Math.min(255, opacity);
            }
        }
    }

    /**
	 * Will vary from 0 - 10
	 */
    private final void initBorderStencil() {
        int y = 0;
        int border = 10;
        int delta = 0;
        int deltaPrime = 0;
        int coeff = 5;
        for (int x = 0; x < stencilNoise.length; x++) {
            for (y = 0; y < stencilNoise[0].length; y++) {
                delta = 255;
                deltaPrime = 255;
                if (x < border) {
                    delta = border - x - 1;
                    delta = Math.abs(delta - (border - 1));
                } else if (x > (stencilNoise.length - 1) - border) {
                    delta = (stencilNoise.length - 1) - x;
                }
                if (y < border) {
                    deltaPrime = border - y - 1;
                    deltaPrime = Math.abs(deltaPrime - (border - 1));
                } else if (y > (stencilNoise.length - 1) - border) {
                    deltaPrime = (stencilNoise.length - 1) - y;
                }
                if (delta == 255 && deltaPrime == 255) {
                    stencilNoise[x][y] = (byte) 255;
                } else {
                    delta = Math.min(delta, deltaPrime);
                    stencilNoise[x][y] = (byte) Math.min(255, delta * delta * coeff);
                }
            }
        }
    }

    private final void initFBM() {
        int x = 0;
        int y = 0;
        int octaves = getNumOctaves();
        for (int z = 0; z < num_ticks; z++) {
            progressPointer = z;
            for (x = 0; x < colorNoise[0].length; x++) {
                if (cancelled) {
                    return;
                }
                for (y = 0; y < colorNoise[0].length; y++) {
                    colorNoise[z][x][y] = (byte) Math.min(255, Math.round(255 * ImprovedNoise.fBm2(z / zooms.z, x / zooms.x, y / zooms.y, H, lacun, octaves)));
                }
            }
        }
    }

    private final void initTurbulence() {
        int x = 0;
        int y = 0;
        final int octaves = getNumOctaves();
        for (int z = 0; z < num_ticks; z++) {
            progressPointer = z;
            for (x = 0; x < colorNoise[0].length; x++) {
                if (cancelled) {
                    return;
                }
                for (y = 0; y < colorNoise[0].length; y++) {
                    colorNoise[z][x][y] = (byte) Math.min(255, Math.round(255d * ImprovedNoise.turbulence(z / zooms.z, x / zooms.x, y / zooms.y, octaves)));
                }
            }
        }
    }

    public boolean isWorking() {
        synchronized (mutex_working) {
            return working;
        }
    }

    public boolean isInit() {
        synchronized (mutex_noise) {
            return colorNoise != null;
        }
    }

    @Override
    protected void doWork() {
        if (tickPointer == num_ticks - 1) decrement = true; else if (tickPointer == 0) decrement = false;
        try {
            imageComp.updateData(this, 0, 0, image_dim, image_dim);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (decrement) --tickPointer; else ++tickPointer;
    }

    public void readSceneGraphObject(DataInput in) throws IOException {
        int version = in.readUnsignedByte();
        textureUnitReference = in.readInt();
        setFPS(in.readInt());
        num_ticks = in.readInt();
        redMin = in.readInt();
        redMax = in.readInt();
        greenMin = in.readInt();
        greenMax = in.readInt();
        blueMin = in.readInt();
        blueMax = in.readInt();
        redAlpha = in.readFloat();
        greenAlpha = in.readFloat();
        blueAlpha = in.readFloat();
        noise_type = in.readByte();
        stencilType = in.readByte();
        fireStencil = in.readBoolean();
        image_dim = in.readInt();
        if (colorNoise == null) {
            colorNoise = new byte[num_ticks][image_dim][image_dim];
        }
        if (stencilNoise == null) {
            stencilNoise = new byte[image_dim][image_dim];
        }
        for (int i = 0; i < colorNoise.length; i++) {
            for (int j = 0; j < colorNoise[i].length; j++) {
                in.readFully(colorNoise[i][j]);
            }
        }
        for (int i = 0; i < image_dim; i++) {
            in.readFully(stencilNoise[i]);
        }
        if (fireStencil) {
            ridgedGain = in.readFloat();
            ridgedOffset = in.readFloat();
        }
        if (version > VERSION_1) {
            H = in.readFloat();
            lacun = in.readFloat();
        }
    }

    private synchronized void initNoiseAsync() {
        if (colorNoise == null) {
            synchronized (mutex_noise) {
                colorNoise = new byte[num_ticks][image_dim][image_dim];
            }
        }
        if (stencilNoise == null) {
            synchronized (mutex_noise) {
                stencilNoise = new byte[image_dim][image_dim];
            }
        }
        if (wasEnable) {
            setEnable(true);
            wasEnable = false;
        }
        synchronized (mutex_working) {
            working = true;
            progressPointer = 0;
        }
        MessageDispatcher.getInstance().notifyAsyncWorkInProgress(this);
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(TIMESLICE);
                    initColorNoise();
                    initStencil();
                    updateImage();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    synchronized (mutex_working) {
                        cancelled = false;
                        working = false;
                    }
                }
            }
        }).start();
    }

    private void updateImage() {
        if (textureUnit == null) {
            getTextureUnit();
            return;
        }
        if (!isInit() && !working) {
            initNoiseAsync();
        }
        imageComp.updateData(NoiseTextureAnimator.this, 0, 0, image_dim, image_dim);
    }

    public void restoreSceneGraphObjectReferences(SceneGraphObjectReferenceControl ref) {
        textureUnit = (TextureUnitState) ref.resolveReference(textureUnitReference);
        if (textureUnit == null) {
            throw new IllegalStateException();
        }
        textureUnit.setTexture(initTexture());
        updateImage();
    }

    public boolean saveChildren() {
        return false;
    }

    public void setNoiseType(final int type) {
        noise_type = type;
        initNoiseAsync();
    }

    public void setStencilType(final int b) {
        stencilType = b;
        initStencil();
        updateImage();
    }

    public void setZooms(final Tuple3f p_zooms) {
        zooms.set(p_zooms);
        initNoiseAsync();
    }

    public final void updateData(ImageComponent2D ic2d, int arg1, int arg2, int arg3, int arg4) {
        int height = 0;
        int delta = 0;
        int alphaBeta = 255;
        for (column = 0; column < image_dim; column++) {
            for (row = 0; row < image_dim; row++) {
                value = colorNoise[tickPointer][column][row] & 0xFF;
                if (fireStencil) {
                    height = (int) Math.min(image_dim * ImprovedNoise.ridged(tickPointer / zooms.z, column / zooms.x, 0, ridgedOctaves, ridgedOffset, ridgedGain), image_dim - 1);
                    if (row > height) {
                        ic2d.getImage().setRGB(column, row, 0);
                        continue;
                    }
                    delta = height - row;
                    alphaBeta = Math.min(255, delta * delta);
                }
                if (stencilType > STENCIL_TYPE_NONE) {
                    alphaPrime = stencilNoise[column][row] & 0xFF;
                } else {
                    alphaPrime = 255;
                }
                alphaPrime = Math.min(alphaPrime, alphaBeta);
                red = Math.max(Math.min(value, redMax), redMin);
                green = Math.max(Math.min(value, greenMax), greenMin);
                blue = Math.max(Math.min(value, blueMax), blueMin);
                alpha = (int) Math.max(0, alphaPrime - (redAlpha * red + greenAlpha * green + blueAlpha * blue));
                ic2d.getImage().setRGB(column, row, (alpha << 24) + (red << 16) + (green << 8) + blue);
            }
        }
    }

    public void writeSceneGraphObject(DataOutput out) throws IOException {
        out.write(VERSION);
        out.writeInt(textureUnitReference);
        out.writeInt((int) getFPS());
        out.writeInt(num_ticks);
        out.writeInt(redMin);
        out.writeInt(redMax);
        out.writeInt(greenMin);
        out.writeInt(greenMax);
        out.writeInt(blueMin);
        out.writeInt(blueMax);
        out.writeFloat(redAlpha);
        out.writeFloat(greenAlpha);
        out.writeFloat(blueAlpha);
        out.writeByte(noise_type);
        out.writeByte(stencilType);
        out.writeBoolean(fireStencil);
        out.writeInt(image_dim);
        for (int i = 0; i < colorNoise.length; i++) {
            for (int j = 0; j < colorNoise[i].length; j++) {
                out.write(colorNoise[i][j]);
            }
        }
        for (int i = 0; i < image_dim; i++) {
            out.write(stencilNoise[i]);
        }
        if (fireStencil) {
            out.writeFloat(ridgedGain);
            out.writeFloat(ridgedOffset);
        }
        if (VERSION > VERSION_1) {
            out.writeFloat((float) H);
            out.writeFloat((float) lacun);
        }
    }

    public int getImageDim() {
        return image_dim;
    }

    public int getFrames() {
        return num_ticks;
    }

    /**
	 * Reset the image size, and therefore, the texture and image as well
	 * 
	 * @param p_dim
	 */
    public void setImageDim(final int p_dim) {
        if (isWorking()) {
            MessageDispatcher.getInstance().notifyInfo(PLEASE_CANCEL_OR_WAIT);
            return;
        }
        if (getEnable()) {
            wasEnable = true;
            setEnable(false);
        }
        image_dim = p_dim;
        textureUnit.setTexture(initTexture());
        colorNoise = null;
        stencilNoise = null;
        initNoiseAsync();
    }

    public void setFrames(final int p_frames) {
        if (isWorking()) {
            MessageDispatcher.getInstance().notifyInfo(PLEASE_CANCEL_OR_WAIT);
            return;
        }
        if (getEnable()) {
            if (p_frames > 1) {
                wasEnable = true;
            }
            setEnable(false);
        }
        colorNoise = null;
        num_ticks = p_frames;
        tickPointer = 0;
        initNoiseAsync();
    }

    public int getNoiseType() {
        return noise_type;
    }

    public int getProgress() {
        return progressPointer + 1;
    }

    public boolean isFireEffect() {
        return fireStencil;
    }

    public void setFireEffect(boolean fireStencil) {
        this.fireStencil = fireStencil;
        updateImage();
    }

    public Color getColors(int which) {
        switch(which) {
            case MIN:
                return new Color(redMin, greenMin, blueMin);
            case MAX:
                return new Color(redMax, greenMax, blueMax);
            case ALPHA:
                return new Color(Math.round(redAlpha * 255f), Math.round(greenAlpha * 255f), Math.round(blueAlpha * 255f));
        }
        throw new IllegalArgumentException("Bad color type: " + which);
    }

    public void setColors(Color color, int which) {
        switch(which) {
            case MIN:
                redMin = color.getRed();
                blueMin = color.getBlue();
                greenMin = color.getGreen();
                break;
            case MAX:
                redMax = color.getRed();
                blueMax = color.getBlue();
                greenMax = color.getGreen();
                break;
            case ALPHA:
                redAlpha = color.getRed() / 255f;
                blueAlpha = color.getBlue() / 255f;
                greenAlpha = color.getGreen() / 255f;
        }
        updateImage();
    }

    public String getTaskName() {
        return " Recalculating noise data...";
    }

    public void cancel() {
        cancelled = true;
    }

    public double getHValue() {
        return H;
    }

    public void setHValue(double h) {
        H = h;
        initNoiseAsync();
    }
}
