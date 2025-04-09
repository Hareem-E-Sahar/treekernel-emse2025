package org.openscience.cdk.qsar;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.HydrogenAdder;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.charges.GasteigerMarsiliPartialCharges;
import org.openscience.cdk.charges.Polarizability;
import org.openscience.cdk.aromaticity.HueckelAromaticityDetector;
import org.openscience.cdk.qsar.result.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;
import Jama.Matrix;
import Jama.EigenvalueDecomposition;

/**
 * Eigenvalue based descriptor noted for its utility in chemical diversity.
 * Described by Pearlman et al. {@cdk.cite PEA99}.
 * 
 * The descriptor is based on a weighted version of the Burden matrix {@cdk.cite BUR89, BUR97}
 * which takes into account both the connectivity as well as atomic
 * properties of a molecule. The weights are a variety of atom properties placed along the 
 * diagonal of the Burden matrix. Currently three weighting schemes are employed
 * <ul>
 * <li>atomic weight
 * <li>partial charge (Gasteiger Marsilli)
 * <li>polarizability {@cdk.cite KJ81}
 * </ul>
 * By default, the descriptor will return the 2 highest and lowest eigenvalues for the three
 * classes of descriptor in a single ArrayList (in the order shown above). However it is also
 * possible to supply a parameter list indicating how many of the highest and lowest eigenvalues
 * (for each class of descriptor) are required.
 * <p>
 * The descriptor works with the hydrogen depleted molecule and thus the maximum number
 * of eigenvalues calculated for any class of BCUT descriptor is equal to the number
 * of heavy atoms present.
 * 
 * @author      Rajarshi Guha
 * @cdk.created     2004-11-30
 * 
 * @cdk.builddepends Jama-1.0.1.jar
 * @cdk.depends Jama-1.0.1.jar
 *
 * @cdk.module qsar
 * @cdk.set    qsar-descriptors
 */
public class BCUTDescriptor implements Descriptor {

    private int nhigh;

    private int nlow;

    public BCUTDescriptor() {
        this.nhigh = 2;
        this.nlow = 2;
    }

    public DescriptorSpecification getSpecification() {
        return new DescriptorSpecification("http://qsar.sourceforge.net/dicts/qsar-descriptors:BCUT", this.getClass().getName(), "$Id: BCUTDescriptor.java 3617 2005-01-12 15:22:38Z egonw $", "The Chemistry Development Kit");
    }

    ;

    /**
     *  Sets the parameters attribute of the BCUTDescriptor object.
     *
     *@param  params            The new parameter values. This descriptor takes 2 parameters: number of highest
     *                          eigenvalues and number of lowest eigenvalues. If 0 is specified for either (the default)
     *                          then all calculated eigenvalues are returned.
     *@exception  CDKException  Description of the Exception
     */
    public void setParameters(Object[] params) throws CDKException {
        if (params.length != 2) {
            throw new CDKException("BCUTDescriptor requires 2 parameters");
        }
        if (!(params[0] instanceof Integer) || !(params[1] instanceof Integer)) {
            throw new CDKException("Parameters must be of type Integer");
        }
        this.nhigh = ((Integer) params[0]).intValue();
        this.nlow = ((Integer) params[1]).intValue();
        if (this.nhigh < 0 || this.nlow < 0) {
            throw new CDKException("Number of eigenvalues to return must be positive or 0");
        }
    }

    /**
     *  Gets the parameters attribute of the BCUTDescriptor object.
     *
     *@return    Two element array of Integer representing number of highest and lowest eigenvalues
     *           to return respectively
     */
    public Object[] getParameters() {
        Object params[] = new Object[2];
        params[0] = new Integer(this.nhigh);
        params[1] = new Integer(this.nlow);
        return (params);
    }

    /**
     *  Gets the parameterNames attribute of the BCUTDescriptor object
     *
     *@return    The parameterNames value
     */
    public String[] getParameterNames() {
        String[] params = new String[2];
        params[0] = "Number of highest eigenvalues for each class";
        params[1] = "Number of lowest eigenvalues for each class";
        return (params);
    }

    /**
     *  Gets the parameterType attribute of the BCUTDescriptor object.
     *
     *@param  name  Description of the Parameter (can be either 'nhigh' or 'nlow')
     *@return       The parameterType value
     */
    public Object getParameterType(String name) {
        Object o = null;
        if (name.equals("nhigh")) o = new Integer(1);
        if (name.equals("nlow")) o = new Integer(1);
        return (o);
    }

    private static class BurdenMatrix {

        void BurdenMatrix() {
        }

        ;

        static double[][] evalBurdenMatrix(AtomContainer ac, double[] vsd) {
            AtomContainer local = AtomContainerManipulator.removeHydrogens(ac);
            int natom = local.getAtomCount();
            double[][] m = new double[natom][natom];
            for (int i = 0; i < natom - 1; i++) {
                for (int j = i + 1; j < natom; j++) {
                    for (int k = 0; k < local.getBondCount(); k++) {
                        Bond b = local.getBondAt(k);
                        if (b.contains(local.getAtomAt(i)) && b.contains(local.getAtomAt(j))) {
                            if (b.getOrder() == CDKConstants.BONDORDER_SINGLE) m[i][j] = 0.1; else if (b.getOrder() == CDKConstants.BONDORDER_DOUBLE) m[i][j] = 0.2; else if (b.getOrder() == CDKConstants.BONDORDER_TRIPLE) m[i][j] = 0.3; else if (b.getOrder() == CDKConstants.BONDORDER_AROMATIC) m[i][j] = 0.15;
                            if (local.getBondCount(i) == 1 || local.getBondCount(j) == 1) {
                                m[i][j] += 0.01;
                            }
                            m[j][i] = m[i][j];
                        } else {
                            m[i][j] = 0.001;
                            m[j][i] = 0.001;
                        }
                    }
                }
            }
            for (int i = 0; i < natom; i++) {
                if (vsd != null) m[i][i] = vsd[i]; else m[i][i] = 0.0;
            }
            return (m);
        }
    }

    /**
     *  Calculates the three classes of BCUT descriptors.
     *
     *@param  container  Parameter is the atom container.
     *@return            An ArrayList containing the descriptors. The default is to return
     *                   all calculated eigenvalues of the Burden matrices in the order described
     *                   above. If a parameter list was supplied, then only the specified number
     *                   of highest and lowest eigenvalues (for each class of BCUT) will be returned.
     */
    public DescriptorResult calculate(AtomContainer container) throws CDKException {
        int j = 0;
        Molecule ac = new Molecule(container);
        HydrogenAdder ha = new HydrogenAdder();
        try {
            ha.addExplicitHydrogensToSatisfyValency(ac);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        HueckelAromaticityDetector had = new HueckelAromaticityDetector();
        had.detectAromaticity(ac);
        int nheavy = 0;
        for (int i = 0; i < ac.getAtomCount(); i++) {
            if (ac.getAtomAt(i).getSymbol().equals("H")) continue; else nheavy++;
        }
        if (this.nhigh > nheavy || this.nlow > nheavy) {
            throw new CDKException("Number of negative or positive eigenvalues cannot be more than number of heavy atoms");
        }
        double[] diagvalue = new double[nheavy];
        j = 0;
        try {
            for (int i = 0; i < ac.getAtomCount(); i++) {
                if (ac.getAtomAt(i).getSymbol().equals("H")) continue;
                diagvalue[j] = IsotopeFactory.getInstance().getMajorIsotope(ac.getAtomAt(i).getSymbol()).getExactMass();
                j++;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        double[][] bm = BurdenMatrix.evalBurdenMatrix(ac, diagvalue);
        Matrix m = new Matrix(bm);
        EigenvalueDecomposition ed = new EigenvalueDecomposition(m);
        double[] eval1 = ed.getRealEigenvalues();
        GasteigerMarsiliPartialCharges peoe = null;
        try {
            peoe = new GasteigerMarsiliPartialCharges();
            peoe.assignGasteigerMarsiliPartialCharges(ac, true);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        j = 0;
        for (int i = 0; i < ac.getAtomCount(); i++) {
            if (ac.getAtomAt(i).getSymbol().equals("H")) continue;
            diagvalue[j] = ac.getAtomAt(i).getCharge();
            j++;
        }
        bm = BurdenMatrix.evalBurdenMatrix(ac, diagvalue);
        m = new Matrix(bm);
        ed = new EigenvalueDecomposition(m);
        double[] eval2 = ed.getRealEigenvalues();
        Polarizability pol = new Polarizability();
        j = 0;
        for (int i = 0; i < ac.getAtomCount(); i++) {
            if (ac.getAtomAt(i).getSymbol().equals("H")) continue;
            diagvalue[j] = pol.calculateGHEffectiveAtomPolarizability(ac, ac.getAtomAt(i), 1000);
            j++;
        }
        bm = BurdenMatrix.evalBurdenMatrix(ac, diagvalue);
        m = new Matrix(bm);
        ed = new EigenvalueDecomposition(m);
        double[] eval3 = ed.getRealEigenvalues();
        DoubleArrayResult retval = new DoubleArrayResult(eval1.length + eval2.length + eval3.length);
        if (nhigh == 0 || nlow == 0) {
            for (int i = 0; i < eval1.length; i++) retval.add(eval1[i]);
            for (int i = 0; i < eval2.length; i++) retval.add(eval2[i]);
            for (int i = 0; i < eval3.length; i++) retval.add(eval3[i]);
        } else {
            for (int i = 0; i < nlow; i++) retval.add(eval1[i]);
            for (int i = 0; i < nhigh; i++) retval.add(eval1[eval1.length - i - 1]);
            for (int i = 0; i < nlow; i++) retval.add(eval2[i]);
            for (int i = 0; i < nhigh; i++) retval.add(eval2[eval2.length - i - 1]);
            for (int i = 0; i < nlow; i++) retval.add(eval3[i]);
            for (int i = 0; i < nhigh; i++) retval.add(eval3[eval3.length - i - 1]);
        }
        return (retval);
    }
}
