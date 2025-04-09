package compton.physics;

import compton.util.Debug;
import compton.physics.*;
import compton.physics.beam.*;
import java.util.*;

/**
 * This class calculates the probablity sphere for angular cross section
 * @author Andrew Garner Feb 2010
 **/
public class AngularCrossSection {

    private double particleEnergyMeV;

    private double particleRestMass;

    private BeamDirection electronDirection;

    private double photonEnergy;

    private BeamDirection laserDirection;

    private double normalizedPhotonEnergy;

    private double photonEnergyRF;

    private double totalCrossSection;

    private Booster frameBooster;

    private Vector3D incomingPhotonRF;

    private double[][] scatteringRotation;

    private List<Sphericule> sphericules;

    private double integralCrossSection = 0;

    private double minRestFrameEnergy = 0;

    private double maxRestFrameEnergy = 0;

    private double averageRestFrameEnergy = 0;

    /**
	 * Construcst an object to calculate the angular cross section
	 *
	 * @param particleEnergyMev the energy of the particle in MeV in the absolute frame
	 * @param particleRestMass the rest mass energy of the particle in MeV/c^2
	 * @param particleDirection the direction of the particle (ie particle beam direction) in the absolute frame
	 * @param photonEnergy the energy of a single laser photon in the absolute frame (in MeV)
	 * @param photonDirection the direction of the incoming laser photon in the absolute frame
	 */
    public AngularCrossSection(double particleEnergyMeV, double particleRestMass, BeamDirection electronDirection, double photonEnergy, BeamDirection laserDirection) {
        this.particleEnergyMeV = particleEnergyMeV;
        this.particleRestMass = particleRestMass;
        this.electronDirection = electronDirection;
        this.photonEnergy = photonEnergy;
        this.laserDirection = laserDirection;
        this.normalizedPhotonEnergy = PhysicsFunctions.photonNormalisedEnergy(this.particleEnergyMeV, this.particleRestMass, this.electronDirection, this.photonEnergy, this.laserDirection);
        this.photonEnergyRF = (this.normalizedPhotonEnergy * this.particleRestMass);
        Debug.INFO.print("Photon energy in rest frame: " + this.photonEnergyRF);
        this.totalCrossSection = PhysicsFunctions.comptonCrossSectionUm(this.normalizedPhotonEnergy);
        this.frameBooster = new Booster(this.electronDirection, this.particleEnergyMeV, this.particleRestMass);
        this.incomingPhotonRF = frameBooster.boostIntoRestFrame(laserDirection.getAxisW()).normalize();
        Vector3D zAxis = new Vector3D(0, 0, 1);
        double rotAngle = Math.acos(this.incomingPhotonRF.innerProduct(zAxis));
        if (Math.abs(rotAngle) < Vector3D.MIN_VECTOR3D_PRECISION) {
            Debug.DISPLAY.print("No rotation");
            this.scatteringRotation = new double[3][3];
            this.scatteringRotation[0][0] = 1;
            this.scatteringRotation[0][1] = 0;
            this.scatteringRotation[0][2] = 0;
            this.scatteringRotation[1][0] = 0;
            this.scatteringRotation[1][1] = 1;
            this.scatteringRotation[1][2] = 0;
            this.scatteringRotation[2][0] = 0;
            this.scatteringRotation[2][1] = 0;
            this.scatteringRotation[2][2] = 1;
        } else if (Math.abs(rotAngle) > (Math.PI - Vector3D.MIN_VECTOR3D_PRECISION)) {
            Debug.DISPLAY.print("180 rotation");
            this.scatteringRotation = new double[3][3];
            this.scatteringRotation[0][0] = 1;
            this.scatteringRotation[0][1] = 0;
            this.scatteringRotation[0][2] = 0;
            this.scatteringRotation[1][0] = 0;
            this.scatteringRotation[1][1] = -1;
            this.scatteringRotation[1][2] = 0;
            this.scatteringRotation[2][0] = 0;
            this.scatteringRotation[2][1] = 0;
            this.scatteringRotation[2][2] = -1;
        } else {
            Vector3D rotAxis = this.incomingPhotonRF.crossProduct(zAxis);
            this.scatteringRotation = Vector3D.rotationMatrix(rotAxis, rotAngle);
        }
        this.clear();
    }

    /**
	 * Gets the normalized incoming photon energy
	 * @return normalized energy as a ratio
	 */
    public double getNormalizedPhotonEnergy() {
        return this.normalizedPhotonEnergy;
    }

    /**
	 * Gets the energy of the incoming photon in the particle rest frame
	 * @return the incoming photon energy in MeV
	 */
    public double getIncomingPhotonEnergyRF() {
        return this.photonEnergyRF;
    }

    /**
	 * Gets total cross section associated with this sphere
	 * @return cross section in um^2
	 */
    public double getCrossSection() {
        return this.totalCrossSection;
    }

    /**
	 * Gets the boost transformer associated with this
	 * @return a Booster describing the transformation into the particle rest frame
	 */
    public Booster getBooster() {
        return this.frameBooster;
    }

    /**
	 * Gets the direction of the incoming photon as a normalized 3-vector in
	 * the particle rest frame
	 * @return a Vector3D representing this direction
	 */
    public Vector3D getIncomingPhotonDirectionRF() {
        return this.incomingPhotonRF;
    }

    /**
	 * Clears output
	 */
    public synchronized void clear() {
        this.sphericules = new LinkedList<Sphericule>();
        this.integralCrossSection = 0;
    }

    /**
	 * Generates a sphere in the e- rest frame
	 */
    public synchronized void generateSphere(int totalSteps) {
        this.clear();
        int thetaSteps = (int) (Math.PI / Math.sqrt(2 * Math.PI / totalSteps));
        double thetaDiff = Math.PI / thetaSteps;
        this.minRestFrameEnergy = Double.POSITIVE_INFINITY;
        this.maxRestFrameEnergy = Double.NEGATIVE_INFINITY;
        double averageEnergy = 0;
        double upperScatterAngle = 0;
        double upperSigma = PhysicsFunctions.angularCrossSectionUm(normalizedPhotonEnergy, upperScatterAngle);
        for (int i = 0; i < thetaSteps; i++) {
            double lowerScatterAngle = upperScatterAngle;
            double lowerSigma = upperSigma;
            upperScatterAngle = (i + 1) * Math.PI / (thetaSteps);
            upperSigma = PhysicsFunctions.angularCrossSectionUm(normalizedPhotonEnergy, upperScatterAngle);
            double averageScatterAngle = (lowerScatterAngle + upperScatterAngle) / 2;
            double sigmaSection = 0.5 * (lowerSigma + upperSigma) * (Math.PI / thetaSteps) * Math.sin(averageScatterAngle);
            this.integralCrossSection += sigmaSection;
            int phiSteps = (int) Math.round(totalSteps * 0.5 * Math.sin((lowerScatterAngle + upperScatterAngle) / 2) * thetaDiff);
            if (phiSteps < 1) phiSteps = 1;
            double proportion = (sigmaSection / phiSteps) / totalCrossSection;
            double upperPhiAngle = 0;
            for (int j = 0; j < phiSteps; j++) {
                double lowerPhiAngle = upperPhiAngle;
                upperPhiAngle = 2 * (j + 1) * Math.PI / (phiSteps);
                Sphericule section = new Sphericule(lowerScatterAngle, upperScatterAngle, lowerPhiAngle, upperPhiAngle, proportion);
                double rfEng = section.getPhotonEnergyRF();
                if (this.minRestFrameEnergy > rfEng) this.minRestFrameEnergy = rfEng;
                if (this.maxRestFrameEnergy < rfEng) this.maxRestFrameEnergy = rfEng;
                averageEnergy += proportion * section.getPhotonEnergyRF();
                this.sphericules.add(section);
            }
        }
        double checkSum = 0;
        for (Sphericule check : this.sphericules) checkSum += check.getProportion();
        Debug.INFO.print("Checksum on sphere: " + checkSum);
        Debug.INFO.print("Min RF energy: " + this.minRestFrameEnergy);
        Debug.INFO.print("Max RF energy: " + this.maxRestFrameEnergy);
        Debug.INFO.print("Average RF energy: " + averageEnergy);
        this.averageRestFrameEnergy = averageEnergy;
    }

    public double getMinRFEnergy() {
        return this.minRestFrameEnergy;
    }

    public double getMaxRFEnergy() {
        return this.maxRestFrameEnergy;
    }

    public double getAverageRFEnergy() {
        return this.averageRestFrameEnergy;
    }

    /**
	 * Adds macroparticles to MPBeam
	 *
	 * @param charge the total number of photons in the beam
	 * @param iv the position of the beams in the AFR
	 * @param time the time to propagate the particles by in ns
	 */
    public void exportToBeam(MacroParticleBeam output, double charge, Volume volumeBase, double time) {
        Vector3D center = volumeBase.getCenter();
        for (Sphericule sphericule : this.sphericules) {
            Vector3D momentum = new Vector3D(sphericule.getDirectionAbsoluteFrame(), sphericule.getPhotonEnergyAFR());
            Vector3D location = center.add(new Vector3D(sphericule.getDirectionAbsoluteFrame(), PhysicsConstants.C_LIGHT_UM_NS * time));
            Volume volume = volumeBase.translate(new Vector3D(sphericule.getDirectionAbsoluteFrame(), PhysicsConstants.C_LIGHT_UM_NS * time));
            MacroParticle particle = new MacroParticle(0, charge * sphericule.getProportion(), location, volume, momentum, sphericule.getMinPhotonEnergyAFR(), sphericule.getMaxPhotonEnergyAFR());
            output.add(particle);
        }
    }

    /**
	 * Returns the number of sphericules in this sphere.
	 * Note this function will block if calculation is ongoing
	 */
    public int size() {
        synchronized (this) {
            return this.sphericules.size();
        }
    }

    /**
	 * Returns the calculated integral cross section as it stands
	 * @param the integrated cross section in um^2
	 */
    public double getIntegralCrossSection() {
        return this.integralCrossSection;
    }

    /**
	 * This class represents the proportion of scattering in a particular direction on a spherical surface
	 */
    private class Sphericule {

        private double minTheta;

        private double maxTheta;

        private double averageTheta;

        private double minPhi;

        private double maxPhi;

        private double averagePhi;

        private double propSigma;

        private Vector3D pointerRF;

        private Vector3D pointerAFR;

        private double scatterEnergyRF;

        private double scatterEnergyAFR;

        private Vector3D[] cornersRF;

        private Vector3D[] cornersARF;

        private double[] cornerEnergies;

        private double minEnergy;

        private double maxEnergy;

        /**
		 * Constructs a direction with a proportion of scattering
		 * @param minTheta the lower bound scattering angle value
		 * @param maxTheta the higher bound scattering angle value
		 * @param minPhi the lower bound azimurthal angle value
		 * @param minPhi the higher bound azimurthal angle value
		 * @param energy the energy of the scattered photons (MeV) in the electron rest mass frame
		 * @param propSigma the proportion of sigma this area represents 
		 *
		 */
        public Sphericule(double minTheta, double maxTheta, double minPhi, double maxPhi, double propSigma) {
            this.minTheta = minTheta;
            this.maxTheta = maxTheta;
            this.minPhi = minPhi;
            this.maxPhi = maxPhi;
            this.averageTheta = (minTheta + maxTheta) / 2.0;
            this.averagePhi = (minPhi + maxPhi) / 2.0;
            this.propSigma = propSigma;
            Vector3D scatterFrame = new Vector3D(Math.sin(averageTheta) * Math.cos(averagePhi), Math.sin(averageTheta) * Math.sin(averagePhi), Math.cos(averageTheta));
            this.pointerRF = scatterFrame.transform(scatteringRotation[0][0], scatteringRotation[0][1], scatteringRotation[0][2], scatteringRotation[1][0], scatteringRotation[1][1], scatteringRotation[1][2], scatteringRotation[2][0], scatteringRotation[2][1], scatteringRotation[2][2]);
            this.pointerAFR = frameBooster.boostIntoAbsoluteFrame(this.pointerRF);
            double minThetaEng = photonEnergyRF * PhysicsFunctions.getComptonEnergyRatioRMF(normalizedPhotonEnergy, minTheta);
            double maxThetaEng = photonEnergyRF * PhysicsFunctions.getComptonEnergyRatioRMF(normalizedPhotonEnergy, maxTheta);
            this.scatterEnergyRF = (minThetaEng + maxThetaEng) / 2;
            Vector3D[] corners = new Vector3D[4];
            corners[0] = new Vector3D(Math.sin(minTheta) * Math.cos(minPhi), Math.sin(minTheta) * Math.sin(minPhi), Math.cos(minTheta));
            corners[1] = new Vector3D(Math.sin(minTheta) * Math.cos(maxPhi), Math.sin(minTheta) * Math.sin(maxPhi), Math.cos(minTheta));
            corners[2] = new Vector3D(Math.sin(maxTheta) * Math.cos(minPhi), Math.sin(maxTheta) * Math.sin(minPhi), Math.cos(maxTheta));
            corners[3] = new Vector3D(Math.sin(maxTheta) * Math.cos(maxPhi), Math.sin(maxTheta) * Math.sin(maxPhi), Math.cos(maxTheta));
            this.cornersRF = new Vector3D[4];
            for (int i = 0; i < 4; i++) cornersRF[i] = corners[i].transform(scatteringRotation[0][0], scatteringRotation[0][1], scatteringRotation[0][2], scatteringRotation[1][0], scatteringRotation[1][1], scatteringRotation[1][2], scatteringRotation[2][0], scatteringRotation[2][1], scatteringRotation[2][2]);
            this.cornersARF = new Vector3D[4];
            for (int i = 0; i < 4; i++) cornersARF[i] = frameBooster.boostIntoAbsoluteFrame(cornersRF[i]);
            this.cornerEnergies = new double[4];
            cornerEnergies[0] = frameBooster.getEnergyAFR(minThetaEng, cornersRF[0].getZ());
            cornerEnergies[1] = frameBooster.getEnergyAFR(minThetaEng, cornersRF[1].getZ());
            cornerEnergies[2] = frameBooster.getEnergyAFR(maxThetaEng, cornersRF[2].getZ());
            cornerEnergies[3] = frameBooster.getEnergyAFR(maxThetaEng, cornersRF[3].getZ());
            double minEng = Double.POSITIVE_INFINITY;
            double maxEng = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < 4; i++) {
                if (cornerEnergies[i] > maxEng) maxEng = cornerEnergies[i];
                if (cornerEnergies[i] < minEng) minEng = cornerEnergies[i];
            }
            this.minEnergy = minEng;
            this.maxEnergy = maxEng;
            this.scatterEnergyAFR = (minEng + maxEng) / 2.0;
        }

        /** Gets the minimum scattering angle in rest frame associated with this slice */
        public double getMinTheta() {
            return this.minTheta;
        }

        /** Gets the maximum scattering angle in rest frame associated with this slice */
        public double getMaxTheta() {
            return this.maxTheta;
        }

        /** Gets the average scattering angle in rest frame associated with this slice */
        public double getAverageTheta() {
            return this.averageTheta;
        }

        /** Gets the minimum azimurthal angle in rest frame associated with this slice */
        public double getMinPhi() {
            return this.minPhi;
        }

        /** Gets the maxmimum azimurthal angle in rest frame associated with this slice */
        public double getMaxPhi() {
            return this.maxPhi;
        }

        /** Gets the average azimurthal angle in rest frame associated with this slice */
        public double getAveragePhi() {
            return this.averagePhi;
        }

        /** Gets the proportion of the probability sphere associated with this slice */
        public double getProportion() {
            return this.propSigma;
        }

        public double getEnergySpread() {
            return this.maxEnergy - this.minEnergy;
        }

        /** 
		 * Gets the energy of the scattered photon in particle rest mass frame
		 * @return rest frame energy in MeV
		 */
        public double getPhotonEnergyRF() {
            return this.scatterEnergyRF;
        }

        /**
		 * Gets the energy of the scattered photon in the absolute frame
		 * @return absolute frame energy in MeV
		 */
        public double getPhotonEnergyAFR() {
            return this.scatterEnergyAFR;
        }

        public double getMinPhotonEnergyAFR() {
            return this.minEnergy;
        }

        public double getMaxPhotonEnergyAFR() {
            return this.maxEnergy;
        }

        /**
		 * Returns the direction on the sphere in cartesian co-ordinates in the rest frame
		 * where the z-axis points along the incoming photon direction
		 */
        public Vector3D getDirectionRestFrame() {
            return this.pointerRF;
        }

        /**
		 * Returns the direction on the sphere in cartesian co-ordinates in the absolute frame
		 */
        public Vector3D getDirectionAbsoluteFrame() {
            return this.pointerAFR;
        }
    }
}
