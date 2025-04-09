package Steganography.BlakleyScheme.ImageTools;

/**
 * Class of define method to embding Bit to pixel
 * @author Олеся
 */
public class PixelBuilding {

    public static double m_energy = 0.095;

    private static double m_delta = 0;

    private static boolean m_inverse = false;

    /**
     * constructor to define energy of embding sygnal
     * @param energy
     */
    public PixelBuilding(double energy) {
        m_energy = energy;
    }

    /**
     * Set bit to pixel
     * @param centerRed
     * @param centerGreen
     * @param centerBlue
     * @param bit
     * @return the mean of centerBlue spector
     */
    public static byte setBit(Cross c, byte centerRed, byte centerGreen, byte centerBlue, boolean bit) {
        int help = 0;
        float lum = luminance(centerRed, centerGreen, centerBlue);
        if (bit) {
            if ((help = (int) (c.getEstimation() + m_energy * lum)) > 255) return (byte) (-1);
            return (byte) help;
        } else {
            if ((help = (int) (c.getEstimation() - m_energy * lum)) < 0) return 0;
            return (byte) help;
        }
    }

    /**
     * Get energy of embding sygnal
     * @return energy of embding sygnal
     */
    public static double getEnergy() {
        return m_energy;
    }

    public static void defineDelta(Cross c0[], Cross c1[]) {
        float delta_0 = c0[0].get_centre_Blue() - c0[0].getEstimation();
        float delta_1 = c1[0].get_centre_Blue() - c1[0].getEstimation();
        for (int i = 1; i < c0.length; i++) {
            float help = c0[i].get_centre_Blue() - c0[i].getEstimation();
            delta_0 += help;
            help = c1[i].get_centre_Blue() - c1[i].getEstimation();
            delta_1 += help;
        }
        if (delta_0 < delta_1) m_inverse = false; else m_inverse = true;
        delta_0 /= c0.length;
        delta_1 /= c1.length;
        m_delta = (delta_0 + delta_1) / 2;
    }

    /**
     * Get bit from pixel with help of crosses pixel
     * @param c the array of crosses helping to getting
     * @return resulted bit
     */
    public static boolean getBit(Cross c[]) {
        float delta = c[0].get_centre_Blue() - c[0].getEstimation();
        for (int i = 1; i < c.length; i++) {
            float help = c[i].get_centre_Blue() - c[i].getEstimation();
            delta += help;
        }
        delta = delta / (c.length);
        if (delta > m_delta) {
            if (m_inverse) return false;
            return true;
        }
        if (delta < m_delta) {
            if (m_inverse) return true;
            return false;
        }
        return false;
    }

    /**
     * Return the luminace of RGB-pixel
     */
    public static float luminance(byte red, byte green, byte blue) {
        return ((float) (0.299 * UN(red) + 0.587 * UN(green) + 0.114 * UN(blue)));
    }

    private static float UN(byte value) {
        if (value < 0) return ((float) value + 256);
        return (float) value;
    }
}
