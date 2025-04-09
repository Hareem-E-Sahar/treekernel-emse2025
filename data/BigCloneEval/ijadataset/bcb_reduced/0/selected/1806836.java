package com.csol.chem.util.conformation;

import java.util.*;
import javax.vecmath.*;
import com.csol.chem.core.Atom;
import com.csol.chem.core.Molecule;
import com.csol.chem.util.analysis.AtomWeightFunction;
import jama.EigenvalueDecomposition;
import jama.Matrix;

/**
 *
 */
public class WeightedKabschAlignment {

    public Molecule molecule1;

    public Molecule molecule2;

    public Collection<Atom> atoms1;

    public Collection<Atom> atoms2;

    public AtomWeightFunction weightFunction = null;

    /**
     * 
     */
    public WeightedKabschAlignment(AtomWeightFunction awf) {
        super();
        this.weightFunction = awf;
    }

    public static void align(Molecule molecule1, Molecule molecule2, AtomWeightFunction weightFunction) {
        WeightedKabschAlignment wka = new WeightedKabschAlignment(weightFunction);
        wka.align(molecule1, molecule2);
    }

    public static void align(Molecule molecule1, Molecule molecule2, Collection<Atom> atoms1, Collection<Atom> atoms2, AtomWeightFunction weightFunction) {
        WeightedKabschAlignment wka = new WeightedKabschAlignment(weightFunction);
        wka.align(molecule1, molecule2, atoms1, atoms2);
    }

    public void align(Molecule molecule1, Molecule molecule2) {
        align(molecule1, molecule2, molecule1.getAtoms(), molecule2.getAtoms());
    }

    public void align(Molecule molecule1, Molecule molecule2, Collection<Atom> atoms1, Collection<Atom> atoms2) {
        this.molecule1 = molecule1;
        this.molecule2 = molecule2;
        this.atoms1 = atoms1;
        this.atoms2 = atoms2;
        setup();
        align();
        Matrix3d m = new Matrix3d((new Matrix(U)).getRowPackedCopy());
        Vector3d com1 = new Vector3d(MassCenter.massCenter(molecule1));
        Vector3d com2 = new Vector3d(MassCenter.massCenter(molecule2));
        Vector3d diff = new Vector3d(com1);
        diff.sub(com2);
        molecule2.translate(diff);
        molecule2.rotate(m);
    }

    protected double[][] U;

    protected double rmsd = -1.0;

    protected Point3d[] p1, p2, rp;

    protected double[] wts;

    protected int npoint;

    protected Point3d cm1, cm2;

    protected double[] atwt1, atwt2;

    /**
     * Setup the internal members before call to align().
     * It is recommended that you use only the static or parametrized align
     * methods instead of the dirty innards like setup() + align().
     * If you change any members, then call this function again before calling
     * a new align().
     */
    public void setup() {
        this.npoint = atoms1.size();
        this.p1 = getPoint3dArray(atoms1);
        this.p2 = getPoint3dArray(atoms2);
        this.atwt1 = getAtomicMasses(atoms1);
        this.atwt2 = getAtomicMasses(atoms2);
        this.wts = new double[this.npoint];
        for (int i = 0; i < this.npoint; i++) this.wts[i] = 0.5 * (atwt1[i] + atwt2[i]);
    }

    /**
     * Perform an alignment based on the current configuration.
     * This is probably not the function call you want to make. 
     * Use the static alignment functions instead, or the ones with the
     * parameters. This method assumes that all internal parameters have been
     * setup correctly before calling it. Public for power users.
     */
    public void align() {
        jama.Matrix tmp;
        this.cm1 = new Point3d();
        this.cm2 = new Point3d();
        this.cm1 = getCenterOfMass(p1, atwt1);
        this.cm2 = getCenterOfMass(p2, atwt2);
        for (int i = 0; i < this.npoint; i++) {
            p1[i].x = p1[i].x - this.cm1.x;
            p1[i].y = p1[i].y - this.cm1.y;
            p1[i].z = p1[i].z - this.cm1.z;
            p2[i].x = p2[i].x - this.cm2.x;
            p2[i].y = p2[i].y - this.cm2.y;
            p2[i].z = p2[i].z - this.cm2.z;
        }
        double[][] tR = new double[3][3];
        for (int i = 0; i < this.npoint; i++) {
            tR[0][0] += p1[i].x * p2[i].x * wts[i];
            tR[0][1] += p1[i].x * p2[i].y * wts[i];
            tR[0][2] += p1[i].x * p2[i].z * wts[i];
            tR[1][0] += p1[i].y * p2[i].x * wts[i];
            tR[1][1] += p1[i].y * p2[i].y * wts[i];
            tR[1][2] += p1[i].y * p2[i].z * wts[i];
            tR[2][0] += p1[i].z * p2[i].x * wts[i];
            tR[2][1] += p1[i].z * p2[i].y * wts[i];
            tR[2][2] += p1[i].z * p2[i].z * wts[i];
        }
        double[][] R = new double[3][3];
        tmp = new jama.Matrix(tR);
        R = tmp.transpose().getArray();
        double[][] RtR = new double[3][3];
        jama.Matrix jamaR = new jama.Matrix(R);
        tmp = tmp.times(jamaR);
        RtR = tmp.getArray();
        jama.Matrix jamaRtR = new jama.Matrix(RtR);
        jama.EigenvalueDecomposition ed = jamaRtR.eig();
        double[] mu = ed.getRealEigenvalues();
        double[][] a = ed.getV().getArray();
        double tmp2 = mu[2];
        mu[2] = mu[0];
        mu[0] = tmp2;
        for (int i = 0; i < 3; i++) {
            tmp2 = a[i][2];
            a[i][2] = a[i][0];
            a[i][0] = tmp2;
        }
        a[0][2] = (a[1][0] * a[2][1]) - (a[1][1] * a[2][0]);
        a[1][2] = (a[0][1] * a[2][0]) - (a[0][0] * a[2][1]);
        a[2][2] = (a[0][0] * a[1][1]) - (a[0][1] * a[1][0]);
        double[][] b = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    b[i][j] += R[i][k] * a[k][j];
                }
                b[i][j] = b[i][j] / Math.sqrt(mu[j]);
            }
        }
        double norm1 = 0.;
        double norm2 = 0.;
        for (int i = 0; i < 3; i++) {
            norm1 += b[i][0] * b[i][0];
            norm2 += b[i][1] * b[i][1];
        }
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        for (int i = 0; i < 3; i++) {
            b[i][0] = b[i][0] / norm1;
            b[i][1] = b[i][1] / norm2;
        }
        b[0][2] = (b[1][0] * b[2][1]) - (b[1][1] * b[2][0]);
        b[1][2] = (b[0][1] * b[2][0]) - (b[0][0] * b[2][1]);
        b[2][2] = (b[0][0] * b[1][1]) - (b[0][1] * b[1][0]);
        double[][] tU = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    tU[i][j] += b[i][k] * a[j][k];
                }
            }
        }
        U = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                U[i][j] = tU[j][i];
            }
        }
        this.rp = new Point3d[this.npoint];
        for (int i = 0; i < this.npoint; i++) {
            this.rp[i] = new Point3d(U[0][0] * p2[i].x + U[0][1] * p2[i].y + U[0][2] * p2[i].z, U[1][0] * p2[i].x + U[1][1] * p2[i].y + U[1][2] * p2[i].z, U[2][0] * p2[i].x + U[2][1] * p2[i].y + U[2][2] * p2[i].z);
        }
        double rms = 0.;
        for (int i = 0; i < this.npoint; i++) {
            rms += (p1[i].x - this.rp[i].x) * (p1[i].x - this.rp[i].x) + (p1[i].y - this.rp[i].y) * (p1[i].y - this.rp[i].y) + (p1[i].z - this.rp[i].z) * (p1[i].z - this.rp[i].z);
        }
        this.rmsd = Math.sqrt(rms / this.npoint);
    }

    /**
     * Csol lib version of getPoint3dArray, which is to take a
     * csol atoms collection instead of a cdk atoms array.
     */
    protected Point3d[] getPoint3dArray(Collection<Atom> atoms) {
        Point3d[] p = new Point3d[atoms.size()];
        int c = 0;
        for (Atom atom : atoms) {
            p[c++] = new Point3d(atom.getPosition());
        }
        return p;
    }

    protected double[] getAtomicMasses(Collection<Atom> atoms) {
        double[] am = new double[atoms.size()];
        int c = 0;
        for (Atom atom : atoms) {
            am[c++] = weightFunction.weight(atom);
        }
        return am;
    }

    private Point3d getCenterOfMass(Point3d[] p, double[] atwt) {
        double x = 0.;
        double y = 0.;
        double z = 0.;
        double totalmass = 0.;
        for (int i = 0; i < p.length; i++) {
            x += atwt[i] * p[i].x;
            y += atwt[i] * p[i].y;
            z += atwt[i] * p[i].z;
            totalmass += atwt[i];
        }
        return (new Point3d(x / totalmass, y / totalmass, z / totalmass));
    }
}
