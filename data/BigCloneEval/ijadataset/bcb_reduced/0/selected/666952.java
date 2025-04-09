package com.imagingbook.color;

/**
 * Methods for converting between RGB and HLS color spaces.
 */
public class ColorSpaceConversion {

    public static float[] RGBtoHLS(float R, float G, float B) {
        float cHi = Math.max(R, Math.max(G, B));
        float cLo = Math.min(R, Math.min(G, B));
        float cRng = cHi - cLo;
        float L = (cHi + cLo) / 2;
        float S = 0;
        if (0 < L && L < 1) {
            float d = (L <= 0.5f) ? L : (1 - L);
            S = 0.5f * cRng / d;
        }
        float H = 0;
        if (cHi > 0 && cRng > 0) {
            float rr = (cHi - R) / cRng;
            float gg = (cHi - G) / cRng;
            float bb = (cHi - B) / cRng;
            float hh;
            if (R == cHi) hh = bb - gg; else if (G == cHi) hh = rr - bb + 2.0f; else hh = gg - rr + 4.0f;
            if (hh < 0) hh = hh + 6;
            H = hh / 6;
        }
        return new float[] { H, L, S };
    }

    public static float[] HLStoRGB(float H, float L, float S) {
        float R = 0, G = 0, B = 0;
        if (L <= 0) R = G = B = 0; else if (L >= 1) R = G = B = 1; else {
            float hh = (6 * H) % 6;
            int c1 = (int) hh;
            float c2 = hh - c1;
            float d = (L <= 0.5f) ? (S * L) : (S * (1 - L));
            float w = L + d;
            float x = L - d;
            float y = w - (w - x) * c2;
            float z = x + (w - x) * c2;
            switch(c1) {
                case 0:
                    R = w;
                    G = z;
                    B = x;
                    break;
                case 1:
                    R = y;
                    G = w;
                    B = x;
                    break;
                case 2:
                    R = x;
                    G = w;
                    B = z;
                    break;
                case 3:
                    R = x;
                    G = y;
                    B = w;
                    break;
                case 4:
                    R = z;
                    G = x;
                    B = w;
                    break;
                case 5:
                    R = w;
                    G = x;
                    B = y;
                    break;
            }
        }
        return new float[] { R, G, B };
    }

    public static float[] RGBtoHSV(int R, int G, int B, float[] HSV) {
        float H = 0, S = 0, V = 0;
        float cMax = 255.0f;
        int cHi = Math.max(R, Math.max(G, B));
        int cLo = Math.min(R, Math.min(G, B));
        int cRng = cHi - cLo;
        V = cHi / cMax;
        if (cHi > 0) S = (float) cRng / cHi;
        if (cRng > 0) {
            float rr = (float) (cHi - R) / cRng;
            float gg = (float) (cHi - G) / cRng;
            float bb = (float) (cHi - B) / cRng;
            float hh;
            if (R == cHi) hh = bb - gg; else if (G == cHi) hh = rr - bb + 2.0f; else hh = gg - rr + 4.0f;
            if (hh < 0) hh = hh + 6;
            H = hh / 6;
        }
        if (HSV == null) HSV = new float[3];
        HSV[0] = H;
        HSV[1] = S;
        HSV[2] = V;
        return HSV;
    }

    public static int HSVtoRGB(float h, float s, float v) {
        float rr = 0, gg = 0, bb = 0;
        float hh = (6 * h) % 6;
        int c1 = (int) hh;
        float c2 = hh - c1;
        float x = (1 - s) * v;
        float y = (1 - (s * c2)) * v;
        float z = (1 - (s * (1 - c2))) * v;
        switch(c1) {
            case 0:
                rr = v;
                gg = z;
                bb = x;
                break;
            case 1:
                rr = y;
                gg = v;
                bb = x;
                break;
            case 2:
                rr = x;
                gg = v;
                bb = z;
                break;
            case 3:
                rr = x;
                gg = y;
                bb = v;
                break;
            case 4:
                rr = z;
                gg = x;
                bb = v;
                break;
            case 5:
                rr = v;
                gg = x;
                bb = y;
                break;
        }
        int N = 256;
        int r = Math.min(Math.round(rr * N), N - 1);
        int g = Math.min(Math.round(gg * N), N - 1);
        int b = Math.min(Math.round(bb * N), N - 1);
        int rgb = ((r & 0xff) << 16) | ((g & 0xff) << 8) | b & 0xff;
        return rgb;
    }
}
