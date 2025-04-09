package org.expasy.jpl.commons.ms;

import java.lang.reflect.Constructor;
import org.expasy.jpl.bio.molecule.JPLMass;
import org.expasy.jpl.commons.ms.peak.JPLIMSPeak;

/**
 * Utility methods for ms peaks
 * 
 * @author nikitin
 *
 */
public class JPLMSPeaks {

    /**
	 * Compute mz from mass, charge and mass type.
	 * 
	 * @param mass the mass
	 * @param charge the charge
	 * @param massType the mass type
	 * @return mz
	 */
    public static final double computeMZ(double mass, int charge, JPLMassAccuracy massType) {
        double hMass;
        if (massType == JPLMassAccuracy.MONOISOTOPIC) {
            hMass = JPLMass.H_MASS_MONO.getValue();
        } else {
            hMass = JPLMass.H_MASS_AVG.getValue();
        }
        return (mass + charge * hMass) / charge;
    }

    /**
	 * Compute mass from mz, charge and mass type.
	 * 
	 * @param mz the mz
	 * @param charge the charge
	 * @param massType the mass type
	 * @return mass
	 */
    public static final double computeMass(double mz, int charge, JPLMassAccuracy massType) {
        if (charge == 0) {
            throw new IllegalArgumentException("cannot compute mass from an invalid charge");
        }
        double hMass;
        if (massType == JPLMassAccuracy.MONOISOTOPIC) {
            hMass = JPLMass.H_MASS_MONO.getValue();
        } else {
            hMass = JPLMass.H_MASS_AVG.getValue();
        }
        return mz * charge - charge * hMass;
    }

    public static <T extends JPLIMSPeak> T newEmptyPeakInstance(Class<T> clazz) {
        try {
            Constructor<T> ct = clazz.getConstructor();
            T instance = ct.newInstance();
            return instance;
        } catch (Throwable e) {
            System.err.println("error in newEmptyPeakInstance(" + clazz + ": " + e);
            return null;
        }
    }
}
