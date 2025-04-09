package jat.attitude;

/**
 * Reference: "Spacecraft Dynamics & Control" by Marcel J. Sidi (p.323+)
 *            "Spacecraft Vehicle Dynamics and Control" by Bong Wie (p.318+)
 *            "Cal Poly Aero 451 Project #2, December 2001" 
 * 
 * Theory: Define e = eigenvector of rotation
 *                  = [e1, e2, e3]
 *                theta = the angle of rotation
 *         Then   q1 = e1*sin(theta/2)
 *				  q2 = e2*sin(theta/2)
 *                q3 = e3*sin(theta/2)
 *                q4 = cos(theta/2)
 *
 * How to find quarternions [q1, q2, q3, q4] from the initial condition
 *         1. Calcurate the axis of rotation
 *		   2.
 *         3
 */
public class QuatToDeg {

    public double q1;

    public double q2;

    public double q3;

    public double q4;

    public double[][] element = new double[3][3];

    public double[][] rotation_matrix = new double[3][3];

    public double theta = 10;

    public double psi, phi;

    public QuatToDeg(double e1, double e2, double e3, double e4) {
        q1 = e1;
        q2 = e2;
        q3 = e3;
        q4 = e4;
    }

    public QuatToDeg() {
        q1 = 0.0;
        q2 = 0.0;
        q3 = 0.0;
        q4 = 1.0;
    }

    public double[] calculateAngle() {
        double[] out = new double[3];
        rotation_matrix[0][0] = 1 - 2 * (q2 * q2 + q3 * q3);
        rotation_matrix[0][1] = 2 * (q1 * q2 + q3 * q4);
        rotation_matrix[0][2] = 2 * (q1 * q3 - q2 * q4);
        rotation_matrix[1][0] = 2 * (q1 * q2 - q3 * q4);
        rotation_matrix[1][1] = 1 - 2 * (q1 * q1 + q3 * q3);
        rotation_matrix[1][2] = 2 * (q2 * q3 + q1 * q4);
        rotation_matrix[2][0] = 2 * (q1 * q3 + q2 * q4);
        rotation_matrix[2][1] = 2 * (q2 * q3 - q1 * q4);
        rotation_matrix[2][2] = 1 - 2 * (q1 * q1 + q2 * q2);
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                element[j][i] = rotation_matrix[j][i];
                rotation_matrix[j][i] = rotation_matrix[i][j];
            }
        }
        double c_psi;
        double s_psi;
        double c_phi;
        double s_phi;
        Quad_check checkPsi;
        Quad_check checkPhi;
        theta = (-1) * Math.asin(element[0][2]) * 180 / Math.PI;
        c_psi = element[0][0] / Math.cos(theta * Math.PI / 180);
        s_psi = element[0][1] / Math.cos(theta * Math.PI / 180);
        System.out.println("c_psi=" + c_psi);
        System.out.println("s_psi=" + s_psi);
        checkPsi = new Quad_check(c_psi, s_psi);
        checkPsi.determineAngle();
        psi = checkPsi.angle;
        s_phi = element[1][2] / Math.cos(theta * Math.PI / 180);
        c_phi = element[2][2] / Math.cos(theta * Math.PI / 180);
        checkPhi = new Quad_check(c_phi, s_phi);
        checkPhi.determineAngle();
        phi = checkPhi.angle;
        out[0] = theta;
        out[1] = psi;
        out[2] = phi;
        return out;
    }
}

class Quad_check {

    double c_angle;

    double s_angle;

    double angle;

    public Quad_check(double cos, double sin) {
        c_angle = cos;
        s_angle = sin;
    }

    public void determineAngle() {
        if (c_angle > 0) {
            if (s_angle > 0) angle = Math.acos(c_angle) * 180 / Math.PI; else if (s_angle < 0) angle = Math.asin(s_angle) * 180 / Math.PI; else if (s_angle == 0) angle = 0; else System.out.println("error!");
        } else if (c_angle < 0) {
            if (s_angle > 0) angle = Math.acos(c_angle) * 180 / Math.PI; else if (s_angle < 0) angle = (Math.acos(c_angle) + Math.PI) * 180 / Math.PI; else if (s_angle == 0) angle = 180;
        }
    }
}
