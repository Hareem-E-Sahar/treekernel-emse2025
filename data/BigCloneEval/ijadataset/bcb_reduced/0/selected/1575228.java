package org.expasy.jpl.bio.molecule;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.*;
import org.expasy.jpl.commons.ms.JPLMassAccuracy;
import static org.expasy.jpl.commons.ms.JPLMassAccuracy.*;

/**
 * The periodic table of elements catalogs all atoms and their respective
 * isotopes and mass abundance.
 * 
 * This is a singleton class.
 * 
 * <p>
 * TODO: THIS CLASS WILL BE OBSOLETE SOON - PRINCIPALY TO CHANGE THE
 *       AUTO CODE GENERATING ABILITY !!
 * <p>      
 * This class is a simple and light automata to get periodic table
 * element masses.
 * A static function of the class uses the commented data inside
 * this source file.)
 * and generates the automata code at the end of the file
 * to update the data (that should not be so often), just update the
 * commented data, and run the main().
 * Take care of the format and don't remove the '//START' and '//END'
 * tags used for parsing.
 * Then remove the old method implementation with the new one generated
 * at the end of the file.
 * <p>
 * Atomic mass units :
 * <ul>
 * <li>1 amu (or 1 dalton) = exactly 1/12 the mass of a carbon-12 nucleus</li>
 * <li>1 dalton = 1.67 x 10-24 g</li>
 * </ul>
 * 
 * @author Marc Tuloup.
 */
public class JPLPeriodicTable {

    /** e- mass : 9.10953x10-31 kg or 0.000548 daltons */
    public static final double Electron = 0.0005489243;

    /**
	 * Private inner class only used to generate the code of the
	 * functions getMw().
	 */
    private class Element {

        String name;

        String label;

        double averageMass;

        List<Isotope> isotopeList;

        Isotope mainIsotope;

        private Element(String name) {
            this.name = name;
            this.isotopeList = new ArrayList<Isotope>();
        }

        private void addIsotope(Isotope isotope) throws Exception {
            for (Isotope i : isotopeList) {
                if (i.index == isotope.index) {
                    throw new Exception("isotope " + Integer.toString(isotope.index) + "already exist in Atom " + name);
                }
            }
            isotopeList.add(isotope);
        }

        private void init() throws IllegalArgumentException {
            if (isotopeList.size() == 0) {
                throw new IllegalArgumentException("Atome " + label + " has no Isotope defined");
            }
            mainIsotope = isotopeList.get(0);
            averageMass = 0;
            for (Isotope isotope : isotopeList) {
                averageMass += isotope.mass * isotope.abundance;
                if (mainIsotope.abundance < isotope.abundance) {
                    mainIsotope = isotope;
                }
            }
            averageMass /= 100;
        }
    }

    /**
	 * Private inner class only used to generate the code of the functions getMw()
	 */
    private class Isotope {

        int index;

        String strMass;

        double mass;

        double abundance;

        private Isotope(int index, String strMass, double mass, double abundance) {
            this.index = index;
            this.strMass = strMass;
            this.mass = mass;
            this.abundance = abundance;
        }
    }

    /**
	 * 
	 * @param strElement	String representing the atom.
	 * @param isotope		isotope number (12 or 13 for Carbon...)
	 * 						default value is zero.
	 * @param massAccuracy 	If isotope==0 and massAccuracy==AVERAGE returns the average mass of the isotopes 
	 * taking into account their abondancy.<br>
	 * If isotope==0 and massAccuracy==MONOISOTOPIC returns the mass of the most abundant isotope.
	 * If isotope!=0 massAccuracy should be MONOISOTOPIC otherwise throws IllegalArgumentException. 
	 * @return the mass of the atom
	 * @throws IllegalArgumentException
	 */
    public static double getElementMass(String strElement, Integer isotope, JPLMassAccuracy massAccuracy) throws IllegalArgumentException {
        if (strElement == null) throw new IllegalArgumentException("element string is null");
        if (strElement.length() > 2) throw new IllegalArgumentException("element label can only be one or two characters long");
        if (isotope != 0 && massAccuracy == AVERAGE) throw new IllegalArgumentException(strElement + "[" + isotope + "]" + " incompatible with " + massAccuracy + " calculation");
        switch(strElement.charAt(0)) {
            case 'A':
                if (strElement.charAt(1) == 'g') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 106.905093 : 107.868151;
                    if (isotope == 107) return 106.905093;
                    if (isotope == 109) return 108.904756;
                    break;
                }
                if (strElement.charAt(1) == 'l') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 26.9815384 : 26.981538;
                    if (isotope == 27) return 26.9815384;
                    break;
                }
                if (strElement.charAt(1) == 'r') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 39.962383123 : 39.947661;
                    if (isotope == 36) return 35.9675463;
                    if (isotope == 37) return 36.9667759;
                    if (isotope == 38) return 37.9627322;
                    if (isotope == 39) return 38.964313;
                    if (isotope == 40) return 39.962383123;
                    break;
                }
                if (strElement.charAt(1) == 's') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 74.921596 : 74.921596;
                    if (isotope == 75) return 74.921596;
                    break;
                }
                if (strElement.charAt(1) == 't') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 209.987131 : 0.000000;
                    if (isotope == 210) return 209.987131;
                    break;
                }
                if (strElement.charAt(1) == 'u') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 196.966552 : 196.966552;
                    if (isotope == 197) return 196.966552;
                    break;
                }
                break;
            case 'B':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 11.0093055 : 10.811028;
                    if (isotope == 10) return 10.0129370;
                    if (isotope == 11) return 11.0093055;
                    break;
                }
                if (strElement.charAt(1) == 'a') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 137.905241 : 137.326906;
                    if (isotope == 130) return 129.906310;
                    if (isotope == 132) return 131.905056;
                    if (isotope == 134) return 133.904503;
                    if (isotope == 135) return 134.905683;
                    if (isotope == 136) return 135.904570;
                    if (isotope == 137) return 136.905821;
                    if (isotope == 138) return 137.905241;
                    break;
                }
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 9.0121821 : 9.012182;
                    if (isotope == 9) return 9.0121821;
                    break;
                }
                if (strElement.charAt(1) == 'i') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 208.980383 : 208.980383;
                    if (isotope == 209) return 208.980383;
                    break;
                }
                if (strElement.charAt(1) == 'r') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 78.918338 : 79.903529;
                    if (isotope == 79) return 78.918338;
                    if (isotope == 81) return 80.916291;
                    break;
                }
                break;
            case 'C':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 12 : 12.011037;
                    if (isotope == 12) return 12;
                    if (isotope == 13) return 13.003354838;
                    if (isotope == 14) return 14.003241988;
                    break;
                }
                if (strElement.charAt(1) == 'a') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 39.9625911 : 40.078023;
                    if (isotope == 40) return 39.9625911;
                    if (isotope == 42) return 41.9586183;
                    if (isotope == 43) return 42.9587668;
                    if (isotope == 44) return 43.9554811;
                    if (isotope == 46) return 45.953693;
                    if (isotope == 48) return 47.952534;
                    break;
                }
                if (strElement.charAt(1) == 'd') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 113.903358 : 112.411553;
                    if (isotope == 106) return 105.906458;
                    if (isotope == 108) return 107.904183;
                    if (isotope == 110) return 109.903006;
                    if (isotope == 111) return 110.904182;
                    if (isotope == 112) return 111.902757;
                    if (isotope == 113) return 112.904401;
                    if (isotope == 114) return 113.903358;
                    if (isotope == 116) return 115.904755;
                    break;
                }
                if (strElement.charAt(1) == 'l') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 34.96885271 : 35.452738;
                    if (isotope == 35) return 34.96885271;
                    if (isotope == 37) return 36.96590260;
                    break;
                }
                if (strElement.charAt(1) == 'o') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 58.933200 : 58.933200;
                    if (isotope == 59) return 58.933200;
                    break;
                }
                if (strElement.charAt(1) == 'r') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 51.940512 : 51.996138;
                    if (isotope == 50) return 49.946050;
                    if (isotope == 52) return 51.940512;
                    if (isotope == 53) return 52.940654;
                    if (isotope == 54) return 53.938885;
                    break;
                }
                if (strElement.charAt(1) == 's') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 132.905447 : 132.905447;
                    if (isotope == 133) return 132.905447;
                    break;
                }
                if (strElement.charAt(1) == 'u') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 62.929601 : 63.545644;
                    if (isotope == 63) return 62.929601;
                    if (isotope == 65) return 64.927794;
                    break;
                }
                break;
            case 'F':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 18.99840321 : 18.998403;
                    if (isotope == 19) return 18.99840321;
                    break;
                }
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 55.934942 : 55.789883;
                    if (isotope == 54) return 53.939615;
                    if (isotope == 56) return 55.934942;
                    if (isotope == 57) return 56.935399;
                    if (isotope == 58) return 57.933280;
                    break;
                }
                if (strElement.charAt(1) == 'r') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 223.019731 : 0.000000;
                    if (isotope == 223) return 223.019731;
                    break;
                }
                break;
            case 'G':
                if (strElement.charAt(1) == 'a') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 68.925581 : 69.723072;
                    if (isotope == 69) return 68.925581;
                    if (isotope == 71) return 70.924705;
                    break;
                }
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 73.921178 : 72.591372;
                    if (isotope == 70) return 69.924250;
                    if (isotope == 72) return 71.922076;
                    if (isotope == 73) return 72.923459;
                    if (isotope == 74) return 73.921178;
                    if (isotope == 76) return 75.921403;
                    break;
                }
                break;
            case 'H':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 1.007825032 : 1.007976;
                    if (isotope == 1) return 1.007825032;
                    if (isotope == 2) return 2.014101778;
                    if (isotope == 3) return 3.016049268;
                    break;
                }
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 4.002603250 : 4.002602;
                    if (isotope == 3) return 3.016029310;
                    if (isotope == 4) return 4.002603250;
                    break;
                }
                if (strElement.charAt(1) == 'f') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 179.946549 : 178.486403;
                    if (isotope == 174) return 173.940040;
                    if (isotope == 176) return 175.941402;
                    if (isotope == 177) return 176.943220;
                    if (isotope == 178) return 177.943698;
                    if (isotope == 179) return 178.945815;
                    if (isotope == 180) return 179.946549;
                    break;
                }
                if (strElement.charAt(1) == 'g') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 201.970626 : 200.599149;
                    if (isotope == 196) return 195.965815;
                    if (isotope == 198) return 197.966752;
                    if (isotope == 199) return 198.968262;
                    if (isotope == 200) return 199.968309;
                    if (isotope == 201) return 200.970285;
                    if (isotope == 202) return 201.970626;
                    if (isotope == 204) return 203.973476;
                    break;
                }
                break;
            case 'I':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 126.904468 : 126.904468;
                    if (isotope == 127) return 126.904468;
                    break;
                }
                if (strElement.charAt(1) == 'n') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 114.903878 : 114.817886;
                    if (isotope == 113) return 112.904061;
                    if (isotope == 115) return 114.903878;
                    break;
                }
                if (strElement.charAt(1) == 'r') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 192.962924 : 192.216054;
                    if (isotope == 191) return 190.960591;
                    if (isotope == 193) return 192.962924;
                    break;
                }
                break;
            case 'K':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 38.9637068 : 39.098301;
                    if (isotope == 39) return 38.9637068;
                    if (isotope == 40) return 39.9639987;
                    if (isotope == 41) return 40.9618260;
                    break;
                }
                if (strElement.charAt(1) == 'r') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 83.911507 : 83.800024;
                    if (isotope == 78) return 77.920386;
                    if (isotope == 80) return 79.916378;
                    if (isotope == 82) return 81.913485;
                    if (isotope == 83) return 82.914136;
                    if (isotope == 84) return 83.911507;
                    if (isotope == 86) return 85.910610;
                    break;
                }
                break;
            case 'L':
                if (strElement.charAt(1) == 'i') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 7.0160040 : 6.940938;
                    if (isotope == 6) return 6.0151223;
                    if (isotope == 7) return 7.0160040;
                    break;
                }
                break;
            case 'M':
                if (strElement.charAt(1) == 'g') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 23.9850419 : 24.305052;
                    if (isotope == 24) return 23.9850419;
                    if (isotope == 25) return 24.9858370;
                    if (isotope == 26) return 25.9825930;
                    break;
                }
                if (strElement.charAt(1) == 'n') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 54.938050 : 54.938050;
                    if (isotope == 55) return 54.938050;
                    break;
                }
                if (strElement.charAt(1) == 'o') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 97.905408 : 95.931292;
                    if (isotope == 92) return 91.906810;
                    if (isotope == 94) return 93.905088;
                    if (isotope == 95) return 94.905841;
                    if (isotope == 96) return 95.904679;
                    if (isotope == 97) return 96.906021;
                    if (isotope == 98) return 97.905408;
                    if (isotope == 100) return 99.907477;
                    break;
                }
                break;
            case 'N':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 14.003074005 : 14.006723;
                    if (isotope == 14) return 14.003074005;
                    if (isotope == 15) return 15.000108898;
                    break;
                }
                if (strElement.charAt(1) == 'a') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 22.9897697 : 22.989770;
                    if (isotope == 23) return 22.9897697;
                    break;
                }
                if (strElement.charAt(1) == 'b') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 92.906378 : 92.906378;
                    if (isotope == 93) return 92.906378;
                    break;
                }
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 19.992440176 : 20.180046;
                    if (isotope == 20) return 19.992440176;
                    if (isotope == 21) return 20.99384674;
                    if (isotope == 22) return 21.9913855;
                    break;
                }
                if (strElement.charAt(1) == 'i') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 57.935348 : 58.693361;
                    if (isotope == 58) return 57.935348;
                    if (isotope == 60) return 59.930791;
                    if (isotope == 61) return 60.931060;
                    if (isotope == 62) return 61.928349;
                    if (isotope == 64) return 63.927970;
                    break;
                }
                break;
            case 'O':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 15.994914622 : 15.999305;
                    if (isotope == 16) return 15.994914622;
                    if (isotope == 17) return 16.9991315;
                    if (isotope == 18) return 17.9991604;
                    break;
                }
                if (strElement.charAt(1) == 's') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 191.961479 : 190.239777;
                    if (isotope == 184) return 183.952491;
                    if (isotope == 186) return 185.953838;
                    if (isotope == 187) return 186.955748;
                    if (isotope == 188) return 187.955836;
                    if (isotope == 189) return 188.958145;
                    if (isotope == 190) return 189.958445;
                    if (isotope == 192) return 191.961479;
                    break;
                }
                break;
            case 'P':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 30.9737615 : 30.973762;
                    if (isotope == 31) return 30.9737615;
                    break;
                }
                if (strElement.charAt(1) == 'b') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 207.976636 : 207.216892;
                    if (isotope == 204) return 203.973029;
                    if (isotope == 206) return 205.974449;
                    if (isotope == 207) return 206.975881;
                    if (isotope == 208) return 207.976636;
                    break;
                }
                if (strElement.charAt(1) == 'd') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 105.903483 : 106.415328;
                    if (isotope == 102) return 101.905608;
                    if (isotope == 104) return 103.904035;
                    if (isotope == 105) return 104.905084;
                    if (isotope == 106) return 105.903483;
                    if (isotope == 108) return 107.903894;
                    if (isotope == 110) return 109.90515;
                    break;
                }
                if (strElement.charAt(1) == 'o') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 208.982416 : 0.000000;
                    if (isotope == 209) return 208.982416;
                    break;
                }
                if (strElement.charAt(1) == 't') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 194.964774 : 195.080114;
                    if (isotope == 190) return 189.959930;
                    if (isotope == 192) return 191.961035;
                    if (isotope == 194) return 193.962664;
                    if (isotope == 195) return 194.964774;
                    if (isotope == 196) return 195.964935;
                    if (isotope == 198) return 197.967876;
                    break;
                }
                break;
            case 'R':
                if (strElement.charAt(1) == 'a') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 223.018497 : 0.000000;
                    if (isotope == 223) return 223.018497;
                    if (isotope == 225) return 225.023604;
                    if (isotope == 226) return 226.025403;
                    break;
                }
                if (strElement.charAt(1) == 'b') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 84.911789 : 85.467764;
                    if (isotope == 85) return 84.911789;
                    if (isotope == 87) return 86.909183;
                    break;
                }
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 186.955751 : 186.206706;
                    if (isotope == 185) return 184.952956;
                    if (isotope == 187) return 186.955751;
                    break;
                }
                if (strElement.charAt(1) == 'h') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 102.905504 : 102.905504;
                    if (isotope == 103) return 102.905504;
                    break;
                }
                if (strElement.charAt(1) == 'n') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 210.990585 : 0.000000;
                    if (isotope == 211) return 210.990585;
                    if (isotope == 222) return 222.017570;
                    break;
                }
                if (strElement.charAt(1) == 'u') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 101.904350 : 101.069744;
                    if (isotope == 96) return 95.907598;
                    if (isotope == 98) return 97.905287;
                    if (isotope == 99) return 98.905939;
                    if (isotope == 100) return 99.904220;
                    if (isotope == 101) return 100.905582;
                    if (isotope == 102) return 101.904350;
                    if (isotope == 104) return 103.905430;
                    break;
                }
                break;
            case 'S':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 31.9720707 : 32.064388;
                    if (isotope == 32) return 31.9720707;
                    if (isotope == 33) return 32.9714585;
                    if (isotope == 34) return 33.9678668;
                    if (isotope == 36) return 35.9670809;
                    break;
                }
                if (strElement.charAt(1) == 'b') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 120.903818 : 121.756788;
                    if (isotope == 121) return 120.903818;
                    if (isotope == 123) return 122.904216;
                    break;
                }
                if (strElement.charAt(1) == 'c') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 44.955910 : 44.955910;
                    if (isotope == 45) return 44.955910;
                    break;
                }
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 79.916522 : 78.959589;
                    if (isotope == 74) return 73.922477;
                    if (isotope == 76) return 75.919214;
                    if (isotope == 77) return 76.919915;
                    if (isotope == 78) return 77.917310;
                    if (isotope == 80) return 79.916522;
                    if (isotope == 82) return 81.916700;
                    break;
                }
                if (strElement.charAt(1) == 'i') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 27.976926533 : 28.085509;
                    if (isotope == 28) return 27.976926533;
                    if (isotope == 29) return 28.97649472;
                    if (isotope == 30) return 29.97377022;
                    break;
                }
                if (strElement.charAt(1) == 'n') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 119.902197 : 118.710911;
                    if (isotope == 112) return 111.904821;
                    if (isotope == 114) return 113.902782;
                    if (isotope == 115) return 114.903346;
                    if (isotope == 116) return 115.901744;
                    if (isotope == 117) return 116.902954;
                    if (isotope == 118) return 117.901606;
                    if (isotope == 119) return 118.903309;
                    if (isotope == 120) return 119.902197;
                    if (isotope == 122) return 121.903440;
                    if (isotope == 124) return 123.905275;
                    break;
                }
                if (strElement.charAt(1) == 'r') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 87.905614 : 87.616646;
                    if (isotope == 84) return 83.913425;
                    if (isotope == 86) return 85.909262;
                    if (isotope == 87) return 86.908879;
                    if (isotope == 88) return 87.905614;
                    break;
                }
                break;
            case 'T':
                if (strElement.charAt(1) == 'a') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 180.947996 : 180.947876;
                    if (isotope == 180) return 179.947466;
                    if (isotope == 181) return 180.947996;
                    break;
                }
                if (strElement.charAt(1) == 'c') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 97.907216 : 0.000000;
                    if (isotope == 98) return 97.907216;
                    if (isotope == 99) return 98.906255;
                    break;
                }
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 129.906223 : 127.588205;
                    if (isotope == 120) return 119.90402;
                    if (isotope == 122) return 121.903047;
                    if (isotope == 123) return 122.904273;
                    if (isotope == 124) return 123.902819;
                    if (isotope == 125) return 124.904425;
                    if (isotope == 126) return 125.903306;
                    if (isotope == 128) return 127.904461;
                    if (isotope == 130) return 129.906223;
                    break;
                }
                if (strElement.charAt(1) == 'i') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 47.947947 : 47.878426;
                    if (isotope == 46) return 45.952629;
                    if (isotope == 47) return 46.951764;
                    if (isotope == 48) return 47.947947;
                    if (isotope == 49) return 48.947871;
                    if (isotope == 50) return 49.944792;
                    break;
                }
                if (strElement.charAt(1) == 'l') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 204.974412 : 204.383317;
                    if (isotope == 203) return 202.972329;
                    if (isotope == 205) return 204.974412;
                    break;
                }
                break;
            case 'V':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 50.943964 : 50.941472;
                    if (isotope == 50) return 49.947163;
                    if (isotope == 51) return 50.943964;
                    break;
                }
                break;
            case 'W':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 183.950933 : 183.848890;
                    if (isotope == 180) return 179.946706;
                    if (isotope == 182) return 181.948206;
                    if (isotope == 183) return 182.950224;
                    if (isotope == 184) return 183.950933;
                    if (isotope == 186) return 185.954362;
                    break;
                }
                break;
            case 'X':
                if (strElement.charAt(1) == 'e') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 131.904154 : 131.293081;
                    if (isotope == 124) return 123.905896;
                    if (isotope == 126) return 125.904269;
                    if (isotope == 128) return 127.903530;
                    if (isotope == 129) return 128.9047794;
                    if (isotope == 130) return 129.903508;
                    if (isotope == 131) return 130.905082;
                    if (isotope == 132) return 131.904154;
                    if (isotope == 134) return 133.9053945;
                    if (isotope == 136) return 135.907220;
                    break;
                }
                break;
            case 'Y':
                if (strElement.length() == 1) {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 88.905848 : 88.905848;
                    if (isotope == 89) return 88.905848;
                    break;
                }
                break;
            case 'Z':
                if (strElement.charAt(1) == 'n') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 63.929147 : 65.396366;
                    if (isotope == 64) return 63.929147;
                    if (isotope == 66) return 65.926037;
                    if (isotope == 67) return 66.927131;
                    if (isotope == 68) return 67.924848;
                    if (isotope == 70) return 69.925325;
                    break;
                }
                if (strElement.charAt(1) == 'r') {
                    if (isotope == 0) return (massAccuracy == MONOISOTOPIC) ? 89.904704 : 91.223647;
                    if (isotope == 90) return 89.904704;
                    if (isotope == 91) return 90.905645;
                    if (isotope == 92) return 91.905040;
                    if (isotope == 94) return 93.906316;
                    if (isotope == 96) return 95.908276;
                    break;
                }
        }
        if (isotope == 0) throw new IllegalArgumentException("Unknown element " + strElement); else throw new IllegalArgumentException("Unknown isotope " + strElement + "[" + isotope + "]");
    }

    /**
	 * Call this function to generate the code of the functions getMw()
	 */
    public static void main(String[] args) {
        try {
            new JPLPeriodicTable().generateCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Private function to generate the code of the functions getMw()
	 */
    private void generateCode() throws Exception {
        List<Element> eltList = ReadAtomListFromCommentedLines("./src/org/expasy/formula/JPLPeriodicTable.java");
        writeAutomataCode(eltList, "./src/org/expasy/formula/JPLPeriodicTable.java");
    }

    /**
	 * Private function to generate the code of the functions getMw()
	 */
    private List<Element> ReadAtomListFromCommentedLines(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
        List<Element> eltList = new ArrayList<Element>();
        String line;
        Matcher matcher;
        Pattern namePattern = Pattern.compile("^([A-Z]+)\\s+");
        Pattern isotopePattern = Pattern.compile("^([A-Z][a-z]?)\\((\\d+)\\)\\s+(\\S+)\\s+(\\S+)\\s*");
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null && !line.equals("//START")) ;
        if (line == null) throw new Exception("Unable to find the START tag");
        while ((line = br.readLine()) != null && !line.equals("//END")) {
            sb.setLength(0);
            sb.append(line);
            matcher = namePattern.matcher(sb);
            if (!matcher.find()) throw new Exception("The atom commented line should start by the name in uppercase");
            Element elt = new Element(matcher.group(1));
            sb.delete(matcher.start(), matcher.end());
            matcher = isotopePattern.matcher(sb);
            while (matcher.find()) {
                String atomLabel = matcher.group(1);
                Integer isotopeIdx = new Integer(matcher.group(2));
                String strMass = matcher.group(3);
                Double mass = new Double(strMass);
                Double abundance = new Double(matcher.group(4));
                if (elt.label == null) elt.label = atomLabel; else if (!elt.label.equals(atomLabel)) throw new Exception("The isotopes of the atom " + elt.name + "should have the same label");
                elt.addIsotope(new Isotope(isotopeIdx, strMass, mass, abundance));
                sb.delete(matcher.start(), matcher.end());
                matcher = isotopePattern.matcher(sb);
            }
            elt.init();
            eltList.add(elt);
        }
        if (line == null) throw new Exception("Reached EOF before '//END' tag");
        br.close();
        return eltList;
    }

    /**
	 * Private function to generate the code of the function getElementMass()
	 */
    private void writeAutomataCode(List<Element> eltList, String fileName) throws Exception {
        Collections.sort(eltList, new Comparator<Element>() {

            public int compare(Element a, Element b) {
                return a.label.compareTo(b.label);
            }
        });
        PrintWriter pw = new PrintWriter(new FileWriter(new File(fileName), true));
        pw.printf("\n\n");
        pw.printf("public static double getElementMass(String strElement, Integer isotope, JPLMassAccuracy massAccuracy) throws IllegalArgumentException{\n");
        pw.printf("\tif(strElement == null )\n");
        pw.printf("\t\tthrow new IllegalArgumentException(\"element string is null\");\n");
        pw.printf("\tif(strElement.length() > 2)\n");
        pw.printf("\t\tthrow new IllegalArgumentException(\"element label can only be one or two characters long\");\n");
        pw.printf("\tif(isotope != 0 && massAccuracy == AVERAGE)\n");
        pw.printf("\t\tthrow new IllegalArgumentException(strElement + \"[\" + isotope + \"]\" + \" incompatible with \" + massAccuracy + \" calculation\");\n\n");
        pw.printf("\tswitch(strElement.charAt(0)){\n");
        char previousFirstLetter = '\0';
        for (Element elt : eltList) {
            char firstLetter = elt.label.charAt(0);
            char secondLetter = (elt.label.length() == 2) ? elt.label.charAt(1) : '\0';
            if (previousFirstLetter != firstLetter) {
                if (previousFirstLetter != '\0') pw.printf("\t\t\tbreak;\n");
                pw.printf("\t\tcase '%c':\n", firstLetter);
            }
            if (secondLetter == '\0') pw.printf("\t\t\tif(strElement.length() == 1){\n"); else pw.printf("\t\t\tif(strElement.charAt(1) == '%c'){\n", secondLetter);
            pw.printf("\t\t\t\tif(isotope == 0) return (massAccuracy==MONOISOTOPIC)? ");
            pw.printf("%s : %.6f;\n", elt.mainIsotope.strMass, elt.averageMass);
            for (Isotope i : elt.isotopeList) pw.printf("\t\t\t\tif(isotope == %d) return %s;\n", i.index, i.strMass);
            pw.printf("\t\t\t\tbreak;\n\t\t\t}\n");
            previousFirstLetter = firstLetter;
        }
        pw.printf("\t}\n\n");
        pw.printf("\tif(isotope == 0)\n");
        pw.printf("\t\tthrow new IllegalArgumentException(\"Unknown element \" + strElement);\n");
        pw.printf("\telse\n");
        pw.printf("\t\tthrow new IllegalArgumentException(\"Unknown isotope \" + strElement + \"[\" + isotope + \"]\");\n");
        pw.printf("}\n");
        pw.close();
    }
}
