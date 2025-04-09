import computational.geometry.*;
import computational.geometry.rendering3d.*;
import java.util.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.util.Collection;
import java.util.ArrayList;

public class Rendering3DCanvas extends CGCanvas {

    protected Rendering3D rendering3D;

    private int mouseX, mouseY;

    private final Random rnd = new Random();

    public Rendering3DCanvas(CGDesktop desktop, Rendering3D tl) {
        super(desktop);
        this.rendering3D = tl;
        rendering3D.update();
    }

    public Rendering3D getRendering3D() {
        return rendering3D;
    }

    public void mousePressed(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }

    public void mouseDragged(MouseEvent event) {
        int newX = event.getX();
        int newY = event.getY();
        double scale = 1.0 / Math.min((double) getWidth(), (double) getHeight());
        rendering3D.rotateFree((double) (newX - mouseX) * scale, (double) (newY - mouseY) * scale);
        update();
        mouseX = newX;
        mouseY = newY;
    }

    public void generateTorus(int n) {
        Point3D A, B, C;
        ArrayList a = new ArrayList();
        double d = 0.3;
        double r = 0.1;
        if (n < 3) n = 3;
        if (n > 100) n = 100;
        int m = (n + 1) / 2;
        if (m < 3) m = 3;
        double aa = 2.0 * Math.PI / n;
        double bb = 2.0 * Math.PI / m;
        for (int i = 0; i < n; i++) {
            double alpha = 2.0 * Math.PI * i / n;
            for (int j = 0; j < m; j++) {
                double beta = 2.0 * Math.PI * j / m;
                A = new Point3D((d + r * Math.cos(beta)) * Math.cos(alpha), (d + r * Math.cos(beta)) * Math.sin(alpha), r * Math.sin(beta));
                B = new Point3D((d + r * Math.cos(beta + bb)) * Math.cos(alpha), (d + r * Math.cos(beta + bb)) * Math.sin(alpha), r * Math.sin(beta + bb));
                C = new Point3D((d + r * Math.cos(beta + bb)) * Math.cos(alpha + aa), (d + r * Math.cos(beta + bb)) * Math.sin(alpha + aa), r * Math.sin(beta + bb));
                a.add(new Triangle3D(A, B, C, 6));
                A = new Point3D((d + r * Math.cos(beta + bb)) * Math.cos(alpha + aa), (d + r * Math.cos(beta + bb)) * Math.sin(alpha + aa), r * Math.sin(beta + bb));
                B = new Point3D((d + r * Math.cos(beta)) * Math.cos(alpha + aa), (d + r * Math.cos(beta)) * Math.sin(alpha + aa), r * Math.sin(beta));
                C = new Point3D((d + r * Math.cos(beta)) * Math.cos(alpha), (d + r * Math.cos(beta)) * Math.sin(alpha), r * Math.sin(beta));
                a.add(new Triangle3D(A, B, C, 8));
            }
        }
        rendering3D.resetRotation();
        rendering3D.setTriangles(a);
        rendering3D.update();
    }

    public void generateTrefoil(int n) {
        Point3D A, B, C, P1, P2, D1, D2, Q1, Q2, T1, T2, T3, T4;
        double x, y, z;
        ArrayList a = new ArrayList();
        double d = 0.13;
        double r = 0.05;
        if (n < 9) n = 9;
        if (n > 100) n = 100;
        int m;
        if (n < 50) m = n / 4; else if (n < 60) m = n / 5; else m = n / 6;
        if (m < 4) m = 4;
        double aa = 2.0 * Math.PI / n;
        double bb = 2.0 * Math.PI / m;
        for (int i = 0; i < n; i++) {
            double alpha = 2.0 * Math.PI * i / n;
            x = Math.sin(alpha) + 2.0 * Math.sin(2.0 * alpha);
            y = Math.cos(alpha) - 2.0 * Math.cos(2.0 * alpha);
            z = Math.sin(3.0 * alpha);
            P1 = new Point3D(d * x, d * y, d * z);
            x = Math.cos(alpha) + 4.0 * Math.cos(2.0 * alpha);
            y = -Math.sin(alpha) + 4.0 * Math.sin(2.0 * alpha);
            z = 3.0 * Math.cos(3.0 * alpha);
            D1 = new Point3D(x, y, z);
            D1.normalize();
            Q1 = Point3D.crossProduct(D1, P1);
            Q1.normalize();
            Q1.scale(r);
            x = Math.sin(alpha + aa) + 2.0 * Math.sin(2.0 * (alpha + aa));
            y = Math.cos(alpha + aa) - 2.0 * Math.cos(2.0 * (alpha + aa));
            z = Math.sin(3.0 * (alpha + aa));
            P2 = new Point3D(d * x, d * y, d * z);
            x = Math.cos(alpha + aa) + 4.0 * Math.cos(2.0 * (alpha + aa));
            y = -Math.sin(alpha + aa) + 4.0 * Math.sin(2.0 * (alpha + aa));
            z = 3.0 * Math.cos(3.0 * (alpha + aa));
            D2 = new Point3D(x, y, z);
            D2.normalize();
            Q2 = Point3D.crossProduct(D2, P2);
            Q2.normalize();
            Q2.scale(r);
            for (int j = 0; j < m; j++) {
                double beta = 2.0 * Math.PI * j / m;
                T1 = new Point3D(Q1);
                T1.rotateAroundAxis(D1, beta);
                T2 = new Point3D(Q1);
                T2.rotateAroundAxis(D1, beta + bb);
                T3 = new Point3D(Q2);
                T3.rotateAroundAxis(D2, beta);
                T4 = new Point3D(Q2);
                T4.rotateAroundAxis(D2, beta + bb);
                A = new Point3D(P1.getx() + T1.getx(), P1.gety() + T1.gety(), P1.getz() + T1.getz());
                B = new Point3D(P1.getx() + T2.getx(), P1.gety() + T2.gety(), P1.getz() + T2.getz());
                C = new Point3D(P2.getx() + T4.getx(), P2.gety() + T4.gety(), P2.getz() + T4.getz());
                a.add(new Triangle3D(A, B, C, 0));
                A = new Point3D(P1.getx() + T1.getx(), P1.gety() + T1.gety(), P1.getz() + T1.getz());
                B = new Point3D(P2.getx() + T4.getx(), P2.gety() + T4.gety(), P2.getz() + T4.getz());
                C = new Point3D(P2.getx() + T3.getx(), P2.gety() + T3.gety(), P2.getz() + T3.getz());
                a.add(new Triangle3D(A, B, C, 1));
            }
        }
        rendering3D.resetRotation();
        rendering3D.setTriangles(a);
        rendering3D.update();
    }

    public void generateSierpinski(int n) {
        Point3D A, B, C, D, A0, B0, C0, D0, P;
        ArrayList a = new ArrayList();
        double d = 0.43;
        if (n < 0) n = 0;
        if (n > 6) n = 6;
        int m = 1;
        double dd = 0.5;
        for (int i = 0; i < n; i++) {
            m *= 4;
            dd *= 0.5;
        }
        m -= 1;
        A0 = new Point3D(0.0, d / 3.0, -Math.sqrt(8.0) * d / 3.0);
        B0 = new Point3D(Math.sqrt(6.0) * d / 3.0, d / 3.0, Math.sqrt(2.0) * d / 3.0);
        C0 = new Point3D(-Math.sqrt(6.0) * d / 3.0, d / 3.0, Math.sqrt(2.0) * d / 3.0);
        D0 = new Point3D(0.0, -d, 0.0);
        for (; m >= 0; m--) {
            A = new Point3D(A0);
            B = new Point3D(B0);
            C = new Point3D(C0);
            D = new Point3D(D0);
            A.scale(dd * 2.0);
            B.scale(dd * 2.0);
            C.scale(dd * 2.0);
            D.scale(dd * 2.0);
            P = new Point3D();
            int mm = m;
            for (int i = 0; i < n; i++) {
                switch(mm % 4) {
                    case 0:
                        P.add(A0);
                        break;
                    case 1:
                        P.add(B0);
                        break;
                    case 2:
                        P.add(C0);
                        break;
                    default:
                        P.add(D0);
                        break;
                }
                P.scale(2.0);
                mm /= 4;
            }
            P.scale(dd);
            A.add(P);
            B.add(P);
            C.add(P);
            D.add(P);
            a.add(new Triangle3D(A, B, C, 1));
            a.add(new Triangle3D(B, C, D, 6));
            a.add(new Triangle3D(C, D, A, 8));
            a.add(new Triangle3D(D, A, B, 10));
        }
        rendering3D.resetRotation();
        rendering3D.setTriangles(a);
        rendering3D.update();
    }

    public void randomTriangles(int n) {
        Point3D A, B, C;
        ArrayList a = new ArrayList();
        double d = 0.4;
        if (n < 0) n = 0;
        if (n > 100) n = 100;
        for (int i = 0; i < n; i++) {
            A = randomPointInSphere(d);
            B = randomPointInSphere(d);
            C = randomPointInSphere(d);
            a.add(new Triangle3D(A, B, C));
        }
        rendering3D.resetRotation();
        rendering3D.setTriangles(a);
        rendering3D.update();
    }

    public void randomTetrahedra(int n) {
        Point3D A, B, C, D;
        ArrayList a = new ArrayList();
        double d = 0.4;
        if (n < 0) n = 0;
        if (n > 25) n = 25;
        for (int i = 0; i < n; i++) {
            A = randomPointInSphere(d);
            B = randomPointInSphere(d);
            C = randomPointInSphere(d);
            D = randomPointInSphere(d);
            a.add(new Triangle3D(A, B, C, rnd.nextInt(10)));
            a.add(new Triangle3D(B, C, D, rnd.nextInt(10)));
            a.add(new Triangle3D(C, D, A, rnd.nextInt(10)));
            a.add(new Triangle3D(D, A, B, rnd.nextInt(10)));
        }
        rendering3D.resetRotation();
        rendering3D.setTriangles(a);
        rendering3D.update();
    }

    public void randomCubes(int n) {
        Point3D A1, B1, C1, D1, A2, B2, C2, D2, P, Q;
        ArrayList a = new ArrayList();
        int col;
        double dmax = 0.12;
        double dmin = 0.04;
        double r = 0.43;
        if (n < 0) n = 0;
        if (n > 50) n = 50;
        for (int i = 0; i < n; i++) {
            Q = randomPointInSphere(r - Math.sqrt(3.0) * dmin);
            double dd = (rnd.nextDouble() * (Math.min(dmax, (r - Q.norm()) / Math.sqrt(3.0)) - dmin)) + dmin;
            A1 = new Point3D(dd, dd, dd);
            B1 = new Point3D(dd, -dd, dd);
            C1 = new Point3D(-dd, -dd, dd);
            D1 = new Point3D(-dd, dd, dd);
            A2 = new Point3D(dd, dd, -dd);
            B2 = new Point3D(dd, -dd, -dd);
            C2 = new Point3D(-dd, -dd, -dd);
            D2 = new Point3D(-dd, dd, -dd);
            P = new Point3D(1.0, 0.0, 0.0);
            dd = rnd.nextDouble() * 2.0 * Math.PI;
            A1.rotateAroundAxis(P, dd);
            B1.rotateAroundAxis(P, dd);
            C1.rotateAroundAxis(P, dd);
            D1.rotateAroundAxis(P, dd);
            A2.rotateAroundAxis(P, dd);
            B2.rotateAroundAxis(P, dd);
            C2.rotateAroundAxis(P, dd);
            D2.rotateAroundAxis(P, dd);
            P = new Point3D(0.0, 1.0, 0.0);
            dd = rnd.nextDouble() * 2.0 * Math.PI;
            A1.rotateAroundAxis(P, dd);
            B1.rotateAroundAxis(P, dd);
            C1.rotateAroundAxis(P, dd);
            D1.rotateAroundAxis(P, dd);
            A2.rotateAroundAxis(P, dd);
            B2.rotateAroundAxis(P, dd);
            C2.rotateAroundAxis(P, dd);
            D2.rotateAroundAxis(P, dd);
            P = new Point3D(0.0, 0.0, 1.0);
            dd = rnd.nextDouble() * 2.0 * Math.PI;
            A1.rotateAroundAxis(P, dd);
            B1.rotateAroundAxis(P, dd);
            C1.rotateAroundAxis(P, dd);
            D1.rotateAroundAxis(P, dd);
            A2.rotateAroundAxis(P, dd);
            B2.rotateAroundAxis(P, dd);
            C2.rotateAroundAxis(P, dd);
            D2.rotateAroundAxis(P, dd);
            A1.add(Q);
            B1.add(Q);
            C1.add(Q);
            D1.add(Q);
            A2.add(Q);
            B2.add(Q);
            C2.add(Q);
            D2.add(Q);
            col = rnd.nextInt(10);
            a.add(new Triangle3D(A1, B1, C1, col));
            a.add(new Triangle3D(C1, D1, A1, col));
            col = rnd.nextInt(10);
            a.add(new Triangle3D(A2, B2, C2, col));
            a.add(new Triangle3D(C2, D2, A2, col));
            col = rnd.nextInt(10);
            a.add(new Triangle3D(A1, B1, A2, col));
            a.add(new Triangle3D(B1, A2, B2, col));
            col = rnd.nextInt(10);
            a.add(new Triangle3D(B1, C1, B2, col));
            a.add(new Triangle3D(C1, B2, C2, col));
            col = rnd.nextInt(10);
            a.add(new Triangle3D(C1, D1, C2, col));
            a.add(new Triangle3D(D1, C2, D2, col));
            col = rnd.nextInt(10);
            a.add(new Triangle3D(D1, A1, D2, col));
            a.add(new Triangle3D(A1, D2, A2, col));
        }
        rendering3D.resetRotation();
        rendering3D.setTriangles(a);
        rendering3D.update();
    }

    private Point3D randomPointInSphere(double radius) {
        double x = rnd.nextDouble() * 2.0 - 1.0;
        double y = rnd.nextDouble() * 2.0 - 1.0;
        double z = rnd.nextDouble() * 2.0 - 1.0;
        double t;
        if (Math.abs(z) < Math.abs(y)) {
            t = z;
            z = y;
            y = t;
        }
        if (Math.abs(z) < Math.abs(x)) {
            t = z;
            z = x;
            x = t;
        }
        if (z == 0.0) z = 1.0;
        x = (x / z + 1.0) * Math.PI;
        y = Math.acos(y / z);
        if (z < 0.0) z = -z;
        Point3D P = new Point3D(Math.cos(x) * Math.sin(y), Math.sin(x) * Math.sin(y), Math.cos(y));
        P.scale(radius * z);
        return P;
    }

    /************************************************************
	 *                     CONCRETIZATION
	 ************************************************************/
    public void update() {
        repaint();
    }

    public Collection getShapes() {
        Collection shapes = new ArrayList();
        return shapes;
    }

    public void setShapes(Collection triangles) {
        update();
    }

    public void clear() {
        rendering3D.clearTriangles();
        rendering3D.update();
        update();
    }

    /************************************************************
	 *                        PAINTING
	 ************************************************************/
    public void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        rendering3D.draw((Graphics2D) gfx, getWidth(), getHeight());
    }
}
