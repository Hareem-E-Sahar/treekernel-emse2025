package icc.lut;

/**
 * A Linear 32 bit SRGB to SRGB lut
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
public class LookUpTable32LinearSRGBtoSRGB extends LookUpTable32 {

    /**
	 * Factory method for creating the lut.
	 * 
	 * @param wShadowCutoff
	 *            size of shadow region
	 * @param dfShadowSlope
	 *            shadow region parameter
	 * @param ksRGBLinearMaxValue
	 *            size of lut
	 * @param ksRGB8ScaleAfterExp
	 *            post shadow region parameter
	 * @param ksRGBExponent
	 *            post shadow region parameter
	 * @param ksRGB8ReduceAfterEx
	 *            post shadow region parameter
	 * @return the lut
	 */
    public static LookUpTable32LinearSRGBtoSRGB createInstance(int inMax, int outMax, double shadowCutoff, double shadowSlope, double scaleAfterExp, double exponent, double reduceAfterExp) {
        return new LookUpTable32LinearSRGBtoSRGB(inMax, outMax, shadowCutoff, shadowSlope, scaleAfterExp, exponent, reduceAfterExp);
    }

    /**
	 * Construct the lut
	 * 
	 * @param wShadowCutoff
	 *            size of shadow region
	 * @param dfShadowSlope
	 *            shadow region parameter
	 * @param ksRGBLinearMaxValue
	 *            size of lut
	 * @param ksRGB8ScaleAfterExp
	 *            post shadow region parameter
	 * @param ksRGBExponent
	 *            post shadow region parameter
	 * @param ksRGB8ReduceAfterExp
	 *            post shadow region parameter
	 */
    protected LookUpTable32LinearSRGBtoSRGB(int inMax, int outMax, double shadowCutoff, double shadowSlope, double scaleAfterExp, double exponent, double reduceAfterExp) {
        super(inMax + 1, outMax);
        int i = -1;
        double normalize = 1.0 / inMax;
        int cutOff = (int) Math.floor(shadowCutoff * inMax);
        shadowSlope *= outMax;
        int shift = (outMax + 1) / 2;
        for (i = 0; i <= cutOff; i++) lut[i] = (int) (Math.floor(shadowSlope * (i * normalize) + 0.5) - shift);
        scaleAfterExp *= outMax;
        reduceAfterExp *= outMax;
        for (; i <= inMax; i++) lut[i] = (int) (Math.floor(scaleAfterExp * Math.pow(i * normalize, exponent) - reduceAfterExp + 0.5) - shift);
    }

    @Override
    public String toString() {
        StringBuffer rep = new StringBuffer("[LookUpTable32LinearSRGBtoSRGB:");
        return rep.append("]").toString();
    }
}
