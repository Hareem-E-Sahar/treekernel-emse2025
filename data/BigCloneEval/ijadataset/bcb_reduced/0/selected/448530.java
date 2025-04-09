package lc.animation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

public class Animation implements Externalizable {

    private String name;

    private float duration;

    private ArrayList<Animation> children = new ArrayList<Animation>();

    private float[] timestamps;

    private Keyframe[] keyframes;

    private Keyframe[] tangentIn;

    private Keyframe[] tangentOut;

    public Animation() {
    }

    public Animation(String name, float duration, float[] timestamps, Keyframe[] keyframes, Keyframe[] tangentIn, Keyframe[] tangentOut) {
        this.name = name.toLowerCase();
        this.duration = duration;
        this.timestamps = timestamps;
        this.keyframes = keyframes;
        this.tangentIn = tangentIn;
        this.tangentOut = tangentOut;
    }

    public String getName() {
        return name;
    }

    public float getDuration() {
        return duration;
    }

    public ArrayList<Animation> getChildren() {
        return children;
    }

    public float[] getTimestamps() {
        return timestamps;
    }

    public Keyframe[] getSamples() {
        return keyframes;
    }

    public Keyframe[] getTangentsIn() {
        return tangentIn;
    }

    public Keyframe[] getTangentsOut() {
        return tangentOut;
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            int version = in.readInt();
            if (version != VERSION) throw new IOException("Version not recognized in Animation.readExternal()");
            name = in.readUTF();
            duration = in.readFloat();
            byte b = in.readByte();
            if ((b & 1) != 0) {
                int sz = in.readInt();
                timestamps = new float[sz];
                for (int i = 0; i != sz; ++i) {
                    timestamps[i] = in.readFloat();
                }
            }
            if ((b & 2) != 0) {
                int sz = in.readInt();
                keyframes = new Keyframe[sz];
                for (int i = 0; i != sz; ++i) {
                    keyframes[i] = new Keyframe();
                    keyframes[i].readExternal(in);
                }
            }
            if ((b & 4) != 0) {
                int sz = in.readInt();
                tangentIn = new Keyframe[sz];
                for (int i = 0; i != sz; ++i) {
                    tangentIn[i] = new Keyframe();
                    tangentIn[i].readExternal(in);
                }
            }
            if ((b & 8) != 0) {
                int sz = in.readInt();
                tangentOut = new Keyframe[sz];
                for (int i = 0; i != sz; ++i) {
                    tangentOut[i] = new Keyframe();
                    tangentOut[i].readExternal(in);
                }
            }
            if ((b & 16) != 0) {
                int sz = in.readInt();
                children = new ArrayList<Animation>();
                for (int i = 0; i != sz; ++i) {
                    children.add(new Animation());
                    children.get(i).readExternal(in);
                }
            }
        } catch (IOException iox) {
            throw iox;
        } catch (ClassNotFoundException cnfx) {
            throw cnfx;
        } catch (Exception x) {
            throw new IOException("Corrupt data in Animation.readExternal()");
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        out.writeUTF(name);
        out.writeFloat(duration);
        byte b = 0;
        if (timestamps != null) b |= 1;
        if (keyframes != null) b |= 2;
        if (tangentIn != null) b |= 4;
        if (tangentOut != null) b |= 8;
        if (children != null) b |= 16;
        out.writeByte(b);
        if (timestamps != null) {
            out.writeInt(timestamps.length);
            for (int i = 0, n = timestamps.length; i != n; ++i) out.writeFloat(timestamps[i]);
        }
        if (keyframes != null) {
            out.writeInt(keyframes.length);
            for (int i = 0, n = keyframes.length; i != n; ++i) keyframes[i].writeExternal(out);
        }
        if (tangentIn != null) {
            out.writeInt(tangentIn.length);
            for (int i = 0, n = tangentIn.length; i != n; ++i) tangentIn[i].writeExternal(out);
        }
        if (tangentOut != null) {
            out.writeInt(tangentOut.length);
            for (int i = 0, n = tangentOut.length; i != n; ++i) tangentOut[i].writeExternal(out);
        }
        if (children != null) {
            out.writeInt(children.size());
            for (int i = 0, n = children.size(); i != n; ++i) {
                children.get(i).writeExternal(out);
            }
        }
    }

    public final void sample(float position, Keyframe output) {
        assert output != null;
        assert output.translation != null;
        assert output.rotation != null;
        assert output.scale != null;
        if (timestamps == null || keyframes == null) return;
        int min = 0;
        int max = timestamps.length;
        while (min < max - 1) {
            int pos = (min + max) / 2;
            if (timestamps[pos] > position) max = pos; else min = pos;
        }
        float maxValue;
        if (min == timestamps.length - 1) {
            max = 0;
            maxValue = duration;
        } else maxValue = timestamps[max];
        float minValue = timestamps[min];
        float interpolate = (position - minValue) / (maxValue - minValue);
        Keyframe a = keyframes[min];
        Keyframe b = keyframes[max];
        lerp(a.translation, b.translation, interpolate, output.translation);
        lerp(a.scale, b.scale, interpolate, output.scale);
        lerp(a.rotation, b.rotation, interpolate, output.rotation);
    }

    private static final void lerp(Vector3f a, Vector3f b, float rho, Vector3f out) {
        if (a == null) if (b == null) return; else out.set(b); else if (b == null) out.set(a); else out.set(a.x * (1 - rho) + b.x * rho, a.y * (1 - rho) + b.y * rho, a.z * (1 - rho) + b.z * rho);
    }

    private static final void lerp(Quaternion a, Quaternion b, float rho, Quaternion out) {
        if (a == null) if (b == null) return; else out.set(b); else if (b == null) out.set(a); else {
            if (a.dot(b) > 0) out.set(a.x * (1 - rho) + b.x * rho, a.y * (1 - rho) + b.y * rho, a.z * (1 - rho) + b.z * rho, a.w * (1 - rho) + b.w * rho); else out.set(a.x * (rho - 1) + b.x * rho, a.y * (rho - 1) + b.y * rho, a.z * (rho - 1) + b.z * rho, a.w * (rho - 1) + b.w * rho);
            out.normalize();
        }
    }

    final float calcDuration() {
        float max = duration;
        for (Animation a : children) {
            float d = a.calcDuration();
            if (d > max) max = d;
        }
        return max;
    }

    final void applyDuration(float nu) {
        duration = nu;
        for (Animation a : children) {
            a.applyDuration(nu);
        }
    }

    private static final int VERSION = 2;
}
