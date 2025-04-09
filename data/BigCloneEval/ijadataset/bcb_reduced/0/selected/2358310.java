package fr.inria.zvtm.lens;

import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**Profile: linear - Distance metric: L(2) (circular shape)<br>Rendering enhanced through alpha blending between focus and context in spatially-distorted transition zone.<br>Size expressed as an absolute value in pixels*/
public class XGaussianLens extends XLinearLens {

    protected double c = 0;

    protected double e = 0;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public XGaussianLens() {
        this(2.0f, 0, 1, 100, 50, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm magnification factor, mm in [0,+inf[
     */
    public XGaussianLens(float mm) {
        this(mm, 0, 1, 100, 50, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param tc translucency value (at junction between transition and context), tc in [0,1.0]
     *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public XGaussianLens(float mm, float tc, float tf, int outerRadius, int innerRadius) {
        this(mm, tc, tf, outerRadius, innerRadius, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param tc translucency value (at junction between transition and context), tc in [0,1.0]
     *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public XGaussianLens(float mm, float tc, float tf, int outerRadius, int innerRadius, int x, int y) {
        super(mm, tc, tf, outerRadius, innerRadius, x, y);
        a = Math.PI / (LR1 - LR2);
        b = -Math.PI * LR2 / (LR1 - LR2);
        c = (MM - 1) / 2;
        e = (1 + MM) / 2;
    }

    public void gf(float x, float y, float[] g) {
        dd = Math.sqrt(Math.pow(x - sw - lx, 2) + Math.pow(y - sh - ly, 2));
        if (dd <= LR2) {
            g[0] = g[1] = MM;
        } else if (dd <= LR1) {
            g[0] = g[1] = (float) (c * Math.cos(a * dd + b) + e);
        } else {
            g[0] = g[1] = 1;
        }
    }

    void computeDropoffFactors() {
        aT = (MMTc - MMTf) / ((float) (LR1 - LR2));
        bT = (MMTf * LR1 - MMTc * LR2) / ((float) (LR1 - LR2));
        a = Math.PI / (LR1 - LR2);
        b = -Math.PI * LR2 / (LR1 - LR2);
        c = (MM - 1) / 2;
        e = (1 + MM) / 2;
    }

    synchronized void transformI(WritableRaster iwr, WritableRaster ewr) {
        synchronized (this) {
            if (BMl == null) {
                SinglePixelPackedSampleModel SMl = (SinglePixelPackedSampleModel) ewr.getSampleModel();
                SinglePixelPackedSampleModel SMm = (SinglePixelPackedSampleModel) iwr.getSampleModel();
                BMl = SMl.getBitMasks();
                BMm = SMm.getBitMasks();
                BOl = SMl.getBitOffsets();
                BOm = SMm.getBitOffsets();
            }
            iwr.getDataElements(lurd[0], lurd[1], lensWidth, lensHeight, oPixelsI);
            ewr.getDataElements(0, 0, mbw, mbh, mPixelsI);
            if (BMl.length == 4) {
                for (int x = lurd[0]; x < lurd[2]; x++) {
                    for (int y = lurd[1]; y < lurd[3]; y++) {
                        this.gf(x, y, gain);
                        tmPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])] = mPixelsI[Math.round(((y - lurd[1]) * MM - hmbh) / gain[1] + hmbh) * mbw + Math.round(((x - lurd[0]) * MM - hmbw) / gain[0] + hmbw)];
                        toPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])] = oPixelsI[(Math.round((((float) y - sh - ly) / gain[1]) + sh + ly) - lurd[1]) * (lensWidth) + (Math.round((((float) x - sw - lx) / gain[0]) + sw + lx) - lurd[0])];
                        Pl = tmPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])];
                        Rl = (Pl & BMl[0]) >>> BOl[0];
                        Gl = (Pl & BMl[1]) >>> BOl[1];
                        Bl = (Pl & BMl[2]) >>> BOl[2];
                        Pm = toPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])];
                        Rm = (Pm & BMm[0]) >>> BOm[0];
                        Gm = (Pm & BMm[1]) >>> BOm[1];
                        Bm = (Pm & BMm[2]) >>> BOm[2];
                        Am = (Pm & BMm[3]) >>> BOm[3];
                        this.gfT(x, y, gainT);
                        Rr = Math.round(Rl * gainT[0] + Rm * (1 - gainT[0]));
                        Gr = Math.round(Gl * gainT[0] + Gm * (1 - gainT[0]));
                        Br = Math.round(Bl * gainT[0] + Bm * (1 - gainT[0]));
                        tPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])] = (Rr << BOm[0]) | (Gr << BOl[1]) | (Br << BOl[2]) | (Am << BOl[3]);
                    }
                }
            } else {
                for (int x = lurd[0]; x < lurd[2]; x++) {
                    for (int y = lurd[1]; y < lurd[3]; y++) {
                        this.gf(x, y, gain);
                        tmPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])] = mPixelsI[Math.round(((y - lurd[1]) * MM - hmbh) / gain[1] + hmbh) * mbw + Math.round(((x - lurd[0]) * MM - hmbw) / gain[0] + hmbw)];
                        toPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])] = oPixelsI[(Math.round((((float) y - sh - ly) / gain[1]) + sh + ly) - lurd[1]) * (lensWidth) + (Math.round((((float) x - sw - lx) / gain[0]) + sw + lx) - lurd[0])];
                        Pl = tmPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])];
                        Rl = (Pl & BMl[0]) >>> BOl[0];
                        Gl = (Pl & BMl[1]) >>> BOl[1];
                        Bl = (Pl & BMl[2]) >>> BOl[2];
                        Pm = toPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])];
                        Rm = (Pm & BMm[0]) >>> BOm[0];
                        Gm = (Pm & BMm[1]) >>> BOm[1];
                        Bm = (Pm & BMm[2]) >>> BOm[2];
                        this.gfT(x, y, gainT);
                        Rr = Math.round(Rl * gainT[0] + Rm * (1 - gainT[0]));
                        Gr = Math.round(Gl * gainT[0] + Gm * (1 - gainT[0]));
                        Br = Math.round(Bl * gainT[0] + Bm * (1 - gainT[0]));
                        tPixelsI[(y - lurd[1]) * (lensWidth) + (x - lurd[0])] = (Rr << BOm[0]) | (Gr << BOl[1]) | (Br << BOl[2]);
                    }
                }
            }
            iwr.setDataElements(lurd[0], lurd[1], lensWidth, lensHeight, tPixelsI);
        }
    }

    synchronized void transformS(WritableRaster iwr, WritableRaster ewr) {
        System.err.println("Error: translucent lens: Sample model not supported yet");
    }

    synchronized void transformB(WritableRaster iwr, WritableRaster ewr) {
        System.err.println("Error: translucent lens: Sample model not supported yet");
    }
}
