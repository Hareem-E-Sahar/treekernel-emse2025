package joelib.math;

import joelib.util.JOERandom;

/**
 * 3x3 Matrix.
 *
 * @author     wegnerj
 * @license GPL
 * @cvsversion    $Revision: 1.7 $, $Date: 2003/08/19 13:11:27 $
 */
public class Matrix3x3 implements java.io.Serializable {

    /**
     *  Description of the Field
     */
    public static final double RAD_TO_DEG = 180.0f / Math.PI;

    /**
     *  Description of the Field
     */
    public static final double DEG_TO_RAD = Math.PI / 180.0f;

    /**
     *  Description of the Field
     */
    public double[][] ele = new double[3][3];

    /**
     *  Constructor for the Matrix3x3 object
     */
    public Matrix3x3() {
        ele[0][0] = 0.0;
        ele[0][1] = 0.0;
        ele[0][2] = 0.0;
        ele[1][0] = 0.0;
        ele[1][1] = 0.0;
        ele[1][2] = 0.0;
        ele[2][0] = 0.0;
        ele[2][1] = 0.0;
        ele[2][2] = 0.0;
    }

    /**
     *  Constructor for the Matrix3x3 object
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @param  c  Description of the Parameter
     */
    public Matrix3x3(XYZVector a, XYZVector b, XYZVector c) {
        ele[0][0] = a.x();
        ele[0][1] = a.y();
        ele[0][2] = a.z();
        ele[1][0] = b.x();
        ele[1][1] = b.y();
        ele[1][2] = b.z();
        ele[2][0] = c.x();
        ele[2][1] = c.y();
        ele[2][2] = c.z();
    }

    /**
     * @param  d  3x3 of type double
     */
    public Matrix3x3(double[][] d) {
        ele[0][0] = d[0][0];
        ele[0][1] = d[0][1];
        ele[0][2] = d[0][2];
        ele[1][0] = d[1][0];
        ele[1][1] = d[1][1];
        ele[1][2] = d[1][2];
        ele[2][0] = d[2][0];
        ele[2][1] = d[2][1];
        ele[2][2] = d[2][2];
    }

    /**
     *  Gets the array attribute of the Matrix3x3 object
     *
     * @param  m  Description of the Parameter
     */
    public void getArray(double[] m) {
        m[0] = ele[0][0];
        m[1] = ele[0][1];
        m[2] = ele[0][2];
        m[3] = ele[1][0];
        m[4] = ele[1][1];
        m[5] = ele[1][2];
        m[6] = ele[2][0];
        m[7] = ele[2][1];
        m[8] = ele[2][2];
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public double determinant() {
        double x;
        double y;
        double z;
        x = ele[0][0] * ((ele[1][1] * ele[2][2]) - (ele[1][2] * ele[2][1]));
        y = ele[0][1] * ((ele[1][2] * ele[2][0]) - (ele[1][0] * ele[2][2]));
        z = ele[0][2] * ((ele[1][0] * ele[2][1]) - (ele[1][1] * ele[2][0]));
        return (x + y + z);
    }

    /**
     *  Description of the Method
     *
     * @param  i  Description of the Parameter
     * @param  j  Description of the Parameter
     * @return    Description of the Return Value
     */
    public final double get(int i, int j) {
        return (ele[i][j]);
    }

    /**
     *  Description of the Method
     *
     * @param  c  Description of the Parameter
     * @return    Description of the Return Value
     */
    public Matrix3x3 diving(final double c) {
        int i;
        int j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                ele[i][j] /= c;
            }
        }
        return this;
    }

    /**
     *  Description of the Method
     *
     * @param  alpha  Description of the Parameter
     * @param  beta   Description of the Parameter
     * @param  gamma  Description of the Parameter
     * @param  a      Description of the Parameter
     * @param  b      Description of the Parameter
     * @param  c      Description of the Parameter
     */
    public void fillOrth(double alpha, double beta, double gamma, double a, double b, double c) {
        double v;
        alpha *= DEG_TO_RAD;
        beta *= DEG_TO_RAD;
        gamma *= DEG_TO_RAD;
        double ca = Math.cos(alpha);
        double cb = Math.cos(beta);
        double cg = Math.cos(gamma);
        double sg = Math.sin(gamma);
        v = 1.0f - (ca * ca) - (cb * cb) - (cg * cg) + (2.0f * ca * cb * cg);
        v = Math.sqrt(Math.abs(v)) / sg;
        ele[0][0] = a;
        ele[0][1] = b * cg;
        ele[0][2] = c * cb;
        ele[1][0] = 0.0f;
        ele[1][1] = b * sg;
        ele[1][2] = (c * (ca - (cb * cg))) / sg;
        ele[2][0] = 0.0f;
        ele[2][1] = 0.0f;
        ele[2][2] = c * v;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public Matrix3x3 invert() {
        double[][] t = new double[3][3];
        double det;
        det = determinant();
        if (det != 0.0) {
            t[0][0] = (ele[1][1] * ele[2][2]) - (ele[1][2] * ele[2][1]);
            t[1][0] = (ele[1][2] * ele[2][0]) - (ele[1][0] * ele[2][2]);
            t[2][0] = (ele[1][0] * ele[2][1]) - (ele[1][1] * ele[2][0]);
            t[0][1] = (ele[2][1] * ele[0][2]) - (ele[2][2] * ele[0][1]);
            t[1][1] = (ele[2][2] * ele[0][0]) - (ele[2][0] * ele[0][2]);
            t[2][1] = (ele[2][0] * ele[0][1]) - (ele[2][1] * ele[0][0]);
            t[0][2] = (ele[0][1] * ele[1][2]) - (ele[0][2] * ele[1][1]);
            t[1][2] = (ele[0][2] * ele[1][0]) - (ele[0][0] * ele[1][2]);
            t[2][2] = (ele[0][0] * ele[1][1]) - (ele[0][1] * ele[1][0]);
            int i;
            int j;
            for (i = 0; i < 3; i++) {
                for (j = 0; j < 3; j++) {
                    ele[i][j] = t[i][j];
                }
            }
            this.diving(det);
        }
        return this;
    }

    /**
     *  Description of the Method
     *
     * @param  v  Description of the Parameter
     * @param  m  Description of the Parameter
     * @return    Description of the Return Value
     */
    public XYZVector mul(final XYZVector v, final Matrix3x3 m) {
        XYZVector vv = new XYZVector();
        vv._vx = (v._vx * m.ele[0][0]) + (v._vy * m.ele[0][1]) + (v._vz * m.ele[0][2]);
        vv._vy = (v._vx * m.ele[1][0]) + (v._vy * m.ele[1][1]) + (v._vz * m.ele[1][2]);
        vv._vz = (v._vx * m.ele[2][0]) + (v._vy * m.ele[2][1]) + (v._vz * m.ele[2][2]);
        return vv;
    }

    /**
     *  Description of the Method
     *
     * @param  m  Description of the Parameter
     * @param  v  Description of the Parameter
     * @return    Description of the Return Value
     */
    public XYZVector mul(final Matrix3x3 m, final XYZVector v) {
        XYZVector vv = new XYZVector();
        vv._vx = (v._vx * m.ele[0][0]) + (v._vy * m.ele[0][1]) + (v._vz * m.ele[0][2]);
        vv._vy = (v._vx * m.ele[1][0]) + (v._vy * m.ele[1][1]) + (v._vz * m.ele[1][2]);
        vv._vz = (v._vx * m.ele[2][0]) + (v._vy * m.ele[2][1]) + (v._vz * m.ele[2][2]);
        return vv;
    }

    /**
     *  Description of the Method
     *
     * @param  rnd  Description of the Parameter
     */
    public void randomRotation(JOERandom rnd) {
        XYZVector v1 = new XYZVector();
        v1.randomUnitXYZVector(rnd);
        double rotAngle = (double) (rnd.nextInt() % 36000) / 100.0;
        if ((rnd.nextInt() % 2) == 0) {
            rotAngle *= -1.0f;
        }
        this.rotAboutAxisByAngle(v1, rotAngle);
    }

    /**
     *  Description of the Method
     *
     * @param  v      Description of the Parameter
     * @param  angle  Description of the Parameter
     */
    public void rotAboutAxisByAngle(final XYZVector v, final double angle) {
        double theta = angle * DEG_TO_RAD;
        double s = Math.sin(theta);
        double c = Math.cos(theta);
        double t = 1 - c;
        XYZVector vtmp = new XYZVector(v);
        vtmp.normalize();
        ele[0][0] = (t * vtmp.x() * vtmp.x()) + c;
        ele[0][1] = (t * vtmp.x() * vtmp.y()) + (s * vtmp.z());
        ele[0][2] = (t * vtmp.x() * vtmp.z()) - (s * vtmp.y());
        ele[1][0] = (t * vtmp.x() * vtmp.y()) - (s * vtmp.z());
        ele[1][1] = (t * vtmp.y() * vtmp.y()) + c;
        ele[1][2] = (t * vtmp.y() * vtmp.z()) + (s * vtmp.x());
        ele[2][0] = (t * vtmp.x() * vtmp.z()) + (s * vtmp.y());
        ele[2][1] = (t * vtmp.y() * vtmp.z()) - (s * vtmp.x());
        ele[2][2] = (t * vtmp.z() * vtmp.z()) + c;
    }

    /**
     *  Description of the Method
     *
     * @param  c        Description of the Parameter
     * @param  noatoms  Description of the Parameter
     */
    public void rotateCoords(double[] c, int noatoms) {
        int i;
        int idx;
        double x;
        double y;
        double z;
        for (i = 0; i < noatoms; i++) {
            idx = i * 3;
            x = (c[idx] * ele[0][0]) + (c[idx + 1] * ele[0][1]) + (c[idx + 2] * ele[0][2]);
            y = (c[idx] * ele[1][0]) + (c[idx + 1] * ele[1][1]) + (c[idx + 2] * ele[1][2]);
            z = (c[idx] * ele[2][0]) + (c[idx + 1] * ele[2][1]) + (c[idx + 2] * ele[2][2]);
            c[idx] = x;
            c[idx + 1] = y;
            c[idx + 2] = z;
        }
    }

    /**
     *  Description of the Method
     *
     * @param  i  Description of the Parameter
     * @param  j  Description of the Parameter
     * @param  v  Description of the Parameter
     */
    public void set(int i, int j, double v) {
        ele[i][j] = v;
    }

    /**
     *  Description of the Method
     *
     * @param  phi    Description of the Parameter
     * @param  theta  Description of the Parameter
     * @param  psi    Description of the Parameter
     */
    public void setupRotMat(double phi, double theta, double psi) {
        double p = phi * DEG_TO_RAD;
        double h = theta * DEG_TO_RAD;
        double b = psi * DEG_TO_RAD;
        double cx = Math.cos(p);
        double sx = Math.sin(p);
        double cy = Math.cos(h);
        double sy = Math.sin(h);
        double cz = Math.cos(b);
        double sz = Math.sin(b);
        ele[0][0] = cy * cz;
        ele[0][1] = cy * sz;
        ele[0][2] = -sy;
        ele[1][0] = (sx * sy * cz) - (cx * sz);
        ele[1][1] = (sx * sy * sz) + (cx * cz);
        ele[1][2] = sx * cy;
        ele[2][0] = (cx * sy * cz) + (sx * sz);
        ele[2][1] = (cx * sy * sz) - (sx * cz);
        ele[2][2] = cx * cy;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString() {
        return toString(this);
    }

    /**
     *  Description of the Method
     *
     * @param  m  Description of the Parameter
     * @return    Description of the Return Value
     */
    public String toString(Matrix3x3 m) {
        StringBuffer sb = new StringBuffer(100);
        sb.append("[ ");
        sb.append(m.ele[0][0]);
        sb.append(", ");
        sb.append(m.ele[0][1]);
        sb.append(", ");
        sb.append(m.ele[0][2]);
        sb.append(" ]");
        sb.append("[ ");
        sb.append(m.ele[1][0]);
        sb.append(", ");
        sb.append(m.ele[1][1]);
        sb.append(", ");
        sb.append(m.ele[1][2]);
        sb.append(" ]");
        sb.append("[ ");
        sb.append(m.ele[2][0]);
        sb.append(", ");
        sb.append(m.ele[2][1]);
        sb.append(", ");
        sb.append(m.ele[2][2]);
        sb.append(" ]");
        return sb.toString();
    }
}
