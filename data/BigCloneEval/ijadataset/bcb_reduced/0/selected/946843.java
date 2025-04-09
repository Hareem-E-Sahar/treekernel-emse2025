package ra.lajolla.geometry;

import javax.vecmath.Point3d;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Aligns two structures to minimize the RMSD using the Kabsch algorithm.
 *
 * <p>This class is an implementation of the Kabsch algorithm ({@cdk.cite KAB76}, {@cdk.cite KAB78})
 * and evaluates the optimal rotation matrix (U) to minimize the RMSD between the two structures.
 * Since the algorithm assumes that the number of points are the same in the two structures
 * it is the job of the caller to pass the proper number of atoms from the two structures. Constructors
 * which take whole <code>AtomContainer</code>'s are provided but they should have the same number
 * of atoms.
 * The algorithm allows for the use of atom weightings and by default all points are given a weight of 1.0
 *
 * <p>Example usage can be:
 * <pre>
 * AtomContainer ac1, ac2;
 *
 * try {
 *    KabschAlignment sa = new KabschAlignment(ac1.getAtoms(),ac2.getAtoms());
 *    sa.align();
 *    System.out.println(sa.getRMSD());
 * } catch (CDKException e){}
 * </pre>
 * In many cases, molecules will be aligned based on some common substructure.
 * In this case the center of masses calculated during alignment refer to these
 * substructures rather than the whole molecules. To superimpose the molecules
 * for display, the second molecule must be rotated and translated by calling
 * <code>rotateAtomContainer</code>. However, since this will also translate the
 * second molecule, the first molecule should also be translated to the center of mass
 * of the substructure specifed for this molecule. This center of mass can be obtained
 * by a call to <code>getCenterOfMass</code> and then manually translating the coordinates.
 * Thus an example would be
 * <pre>
 * AtomContainer ac1, ac2;  // whole molecules
 * Atom[] a1, a2;           // some subset of atoms from the two molecules
 * KabschAlignment sa;
 * 
 * try {
 *    sa = new KabschAlignment(a1,a2);
 *    sa.align();
 * } catch (CDKException e){}
 *
 * Point3d cm1 = sa.getCenterOfMass();
 * for (int i = 0; i &lt; ac1.getAtomCount(); i++) {
 *    Atom a = ac1.getAtomAt(i);
 *    a.setX3d( a.getPoint3d().x - cm1.x );
 *    a.setY3d( a.getPoint3d().y - cm1.y );
 *    a.setY3d( a.getPoint3d().z - cm1.z );
 * }
 * sa.rotateAtomContainer(ac2);
 *
 * // display the two AtomContainer's
 *</pre>
 * 
 * @author           Rajarshi Guha
 * @cdk.created      2004-12-11
 * @cdk.builddepends Jama-1.0.1.jar
 * @cdk.depends      Jama-1.0.1.jar
 * @cdk.dictref      blue-obelisk:alignmentKabsch
 * @cdk.svnrev  $Revision: 9172 $
 */
public class KabschAlignment {

    private double[][] U;

    private double rmsd = -1.0;

    private Point3d[] p1, p2, rp;

    private double[] wts;

    private int npoint;

    private Point3d cm1, cm2;

    private Point3d[] getPoint3dArray(Atom[] a) {
        Point3d[] p = new Point3d[a.length];
        for (int i = 0; i < a.length; i++) {
            p[i] = new Point3d(a[i].getCoords());
        }
        return (p);
    }

    /**
     * Sets up variables for the alignment algorithm.
     *
     * The algorithm allows for atom weighting and the default is 1.0 for all
     * atoms.
     *
     * @param al1 An array of {@link Atom} objects
     * @param al2 An array of {@link Atom} objects. This array will have its coordinates rotated
     *            so that the RMDS is minimzed to the coordinates of the first array
     * @throws CDKException if the number of Atom's are not the same in the two arrays
     */
    public KabschAlignment(Atom[] al1, Atom[] al2) throws Exception {
        if (al1.length != al2.length) {
            throw new Exception("The Atom[]'s being aligned must have the same numebr of atoms");
        }
        this.npoint = al1.length;
        this.p1 = getPoint3dArray(al1);
        this.p2 = getPoint3dArray(al2);
        this.wts = new double[this.npoint];
        for (int i = 0; i < this.npoint; i++) this.wts[i] = 1.0;
    }

    /**
     * Perform an alignment.
     *
     * This method aligns to set of atoms which should have been specified
     * prior to this call
     */
    public void align() {
        Matrix tmp;
        this.cm1 = new Point3d();
        this.cm2 = new Point3d();
        this.cm1 = getMittelvektorOfPunktWolke(p1);
        this.cm2 = getMittelvektorOfPunktWolke(p2);
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
            wts[i] = 1.0;
        }
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
        tmp = new Matrix(tR);
        R = tmp.transpose().getArray();
        double[][] RtR = new double[3][3];
        Matrix jamaR = new Matrix(R);
        tmp = tmp.times(jamaR);
        RtR = tmp.getArray();
        Matrix jamaRtR = new Matrix(RtR);
        EigenvalueDecomposition ed = jamaRtR.eig();
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
     * Returns the RMSD from the alignment.
     *
     * If align() has not been called the return value is -1.0
     *
     * @return The RMSD for this alignment
     * @see #align
     */
    public double getRMSD() {
        return (this.rmsd);
    }

    /**
     * Returns the rotation matrix (u).
     *
     * @return A double[][] representing the rotation matrix
     * @see #align
     */
    public double[][] getRotationMatrix() {
        return (this.U);
    }

    /**
     * Returns the center of mass for the first molecule or fragment used in the calculation.
     *
     * This method is useful when using this class to align the coordinates
     * of two molecules and them displaying them superimposed. Since the center of
     * mass used during the alignment may not be based on the whole molecule (in 
     * general common substructures are aligned), when preparing molecules for display
     * the first molecule should be translated to the center of mass. Then displaying the
     * first molecule and the rotated version of the second one will result in superimposed
     * structures.
     * 
     * @return A Point3d containing the coordinates of the center of mass
     */
    public Point3d getCenterOfMass() {
        return (this.cm1);
    }

    /**
     * Rotates the {@link IAtomContainer} coordinates by the rotation matrix.
     *
     * In general if you align a subset of atoms in a AtomContainer
     * this function can be applied to the whole AtomContainer to rotate all
     * atoms. This should be called with the second AtomContainer (or Atom[])
     * that was passed to the constructor.
     *
     * Note that the AtomContainer coordinates also get translated such that the
     * center of mass of the original fragment used to calculate the alignment is at the origin.
     *
     * @param ac The {@link IAtomContainer} whose coordinates are to be rotated
     */
    public void rotateAtoms(Atom[] atomArray) {
        for (int i = 0; i < atomArray.length; i++) {
            System.out.println("before: " + atomArray[i]);
            atomArray[i].setX(atomArray[i].getX() - this.cm2.x);
            atomArray[i].setY(atomArray[i].getY() - this.cm2.y);
            atomArray[i].setZ(atomArray[i].getZ() - this.cm2.z);
            System.out.println("middle: " + atomArray[i]);
            double[] coords = { U[0][0] * atomArray[i].getX() + U[0][1] * atomArray[i].getY() + U[0][2] * atomArray[i].getZ(), U[1][0] * atomArray[i].getX() + U[1][1] * atomArray[i].getY() + U[1][2] * atomArray[i].getZ(), U[2][0] * atomArray[i].getX() + U[2][1] * atomArray[i].getY() + U[2][2] * atomArray[i].getZ() };
            atomArray[i].setCoords(coords);
            System.out.println("after: " + atomArray[i]);
        }
    }

    /**
     * Rotates the {@link IAtomContainer} coordinates by the rotation matrix.
     *
     * In general if you align a subset of atoms in a AtomContainer
     * this function can be applied to the whole AtomContainer to rotate all
     * atoms. This should be called with the second AtomContainer (or Atom[])
     * that was passed to the constructor.
     *
     * Note that the AtomContainer coordinates also get translated such that the
     * center of mass of the original fragment used to calculate the alignment is at the origin.
     *
     * @param ac The {@link IAtomContainer} whose coordinates are to be rotated
     */
    public void rotateChain(Chain chain) {
        for (Group group : chain.getAtomGroups()) {
            for (Atom atom : group.getAtoms()) {
                atom.setX(atom.getX() - this.cm2.x);
                atom.setY(atom.getY() - this.cm2.y);
                atom.setZ(atom.getZ() - this.cm2.z);
                double[] coords = { U[0][0] * atom.getX() + U[0][1] * atom.getY() + U[0][2] * atom.getZ(), U[1][0] * atom.getX() + U[1][1] * atom.getY() + U[1][2] * atom.getZ(), U[2][0] * atom.getX() + U[2][1] * atom.getY() + U[2][2] * atom.getZ() };
                atom.setCoords(coords);
            }
        }
    }

    /**
	 * 
	 * @return
	 */
    public static Point3d getMittelvektorOfPunktWolke(Point3d[] punktWolke) {
        double x = 0d;
        double y = 0d;
        double z = 0d;
        for (Point3d pdbAtom : punktWolke) {
            x += pdbAtom.x;
        }
        x /= punktWolke.length;
        for (Point3d pdbAtom : punktWolke) {
            y += pdbAtom.y;
        }
        y /= punktWolke.length;
        for (Point3d pdbAtom : punktWolke) {
            z += pdbAtom.z;
        }
        z /= punktWolke.length;
        return new Point3d(x, y, z);
    }
}
