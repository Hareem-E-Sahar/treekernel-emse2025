package engine.graphics.synthesis.texture;

import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.BoolParam;

/**
 * This filter computes the output image as
 * Cout = Cin + (1-Cout.alpha) + Cout*Cout.alpha;
 * @author Holger Dammertz
 *
 */
public final class FilterMask extends Channel {

    BoolParam invert = CreateLocalBoolParam("Invert", false);

    public String getHelpText() {
        return "Blends two images based on an grayscale mask.\n" + "Without invert, the node computes\n" + "   output = (1-input3)*input1 + input3*input2";
    }

    public String getName() {
        return "Mask";
    }

    public FilterMask() {
        super(3);
    }

    public OutputType getOutputType() {
        return OutputType.RGBA;
    }

    public OutputType getChannelInputType(int idx) {
        if (idx == 0) return OutputType.RGBA; else if (idx == 1) return OutputType.RGBA; else if (idx == 2) return OutputType.SCALAR; else System.err.println("Invalid channel access in " + this);
        return OutputType.SCALAR;
    }

    private final void _function(Vector4 out, Vector4 in0, Vector4 in1, Vector4 in2) {
        out.set(in0);
        float w = in2.XYZto1f();
        if (invert.get()) w = 1.0f - w;
        out.mult_ip(1.0f - w);
        out.mult_add_ip(w, in1);
    }

    protected void cache_function(Vector4 out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
        _function(out, caches[0].sample(localX, localY), caches[1].sample(localX, localY), caches[2].sample(localX, localY));
    }

    protected float _value1f(float u, float v) {
        Vector4 val = valueRGBA(u, v);
        return (val.x + val.y + val.z) * (1.0f / 3.0f);
    }

    protected Vector4 _valueRGBA(float u, float v) {
        Vector4 c0 = inputChannels[0].valueRGBA(u, v);
        Vector4 c1 = inputChannels[1].valueRGBA(u, v);
        Vector4 c2 = inputChannels[2].valueRGBA(u, v);
        Vector4 ret = new Vector4();
        _function(ret, c0, c1, c2);
        return ret;
    }
}
