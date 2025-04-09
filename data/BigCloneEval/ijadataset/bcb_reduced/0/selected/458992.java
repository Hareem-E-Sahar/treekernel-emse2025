package ds.moteur.route.personnalise;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import ds.moteur.geometrie.Angle3D;
import ds.moteur.geometrie.Point;
import ds.moteur.route.Carrefour;
import ds.moteur.route.cc.CourbeConduite;
import ds.moteur.route.cc.Direction;
import ds.moteur.route.cc.PointEntree;
import ds.moteur.route.cc.PointSortie;

@SuppressWarnings("serial")
public class Carrefour3Branches extends Carrefour implements Serializable {

    public Carrefour3Branches() {
        super();
    }

    public Carrefour3Branches(Point position, Angle3D angle) {
        super(position, angle);
    }

    public Carrefour3Branches(Point position, Angle3D angle, double ecartement) {
        super(position, angle);
        PointSortie p1 = new PointSortie(-2 * ecartement, ecartement / 2);
        PointEntree p2 = new PointEntree(2 * ecartement, ecartement / 2);
        PointEntree p3 = new PointEntree(-2 * ecartement, -ecartement / 2);
        PointSortie p4 = new PointSortie(2 * ecartement, -ecartement / 2);
        PointSortie p5 = new PointSortie(-ecartement / 2, -2 * ecartement);
        PointEntree p6 = new PointEntree(ecartement / 2, -2 * ecartement);
        CourbeConduite cc1 = new CourbeConduite(this, p2, p1);
        cc1.addSegment();
        CourbeConduite cc2 = new CourbeConduite(this, p3, p4);
        cc2.addSegment();
        CourbeConduite cc3 = new CourbeConduite(this, p3, p5, Direction.DROITE);
        cc3.addPointIntermediaire(new Point(-ecartement, -ecartement / 2));
        cc3.addPointIntermediaire(new Point(-ecartement + ecartement / 2 / Math.sqrt(2), -ecartement + ecartement / 2 / Math.sqrt(2)));
        cc3.addPointIntermediaire(new Point(-ecartement / 2, -ecartement));
        cc3.addSegment();
        cc3.addArc(new Point(-ecartement, -ecartement), ecartement / 2, 0, Math.PI / 2, 1);
        cc3.addSegment();
        CourbeConduite cc4 = new CourbeConduite(this, p6, p1, Direction.GAUCHE);
        cc4.addPointIntermediaire(new Point(ecartement / 2, -ecartement / 2));
        cc4.addPointIntermediaire(new Point(-ecartement / 2 + ecartement / Math.sqrt(2), -ecartement / 2 + ecartement / Math.sqrt(2)));
        cc4.addPointIntermediaire(new Point(-ecartement / 2, ecartement / 2));
        cc4.addSegment();
        cc4.addArc(new Point(-ecartement / 2, -ecartement / 2), ecartement, Math.PI / 2, -Math.PI / 2, 1);
        cc4.addSegment();
        CourbeConduite cc5 = new CourbeConduite(this, p2, p5, Direction.GAUCHE);
        cc5.addPointIntermediaire(new Point(ecartement / 2, ecartement / 2));
        cc5.addPointIntermediaire(new Point(ecartement / 2 - ecartement / Math.sqrt(2), -ecartement / 2 + ecartement / Math.sqrt(2)));
        cc5.addPointIntermediaire(new Point(-ecartement / 2, -ecartement / 2));
        cc5.addSegment();
        cc5.addArc(new Point(ecartement / 2, -ecartement / 2), ecartement, 0, -Math.PI / 2, 1);
        cc5.addSegment();
        CourbeConduite cc6 = new CourbeConduite(this, p6, p4, Direction.DROITE);
        cc6.addPointIntermediaire(new Point(ecartement / 2, -ecartement));
        cc6.addPointIntermediaire(new Point(ecartement - ecartement / 2 / Math.sqrt(2), -ecartement + ecartement / 2 / Math.sqrt(2)));
        cc6.addPointIntermediaire(new Point(ecartement, -ecartement / 2));
        cc6.addSegment();
        cc6.addArc(new Point(ecartement, -ecartement), ecartement / 2, -Math.PI / 2, Math.PI / 2, 1);
        cc6.addSegment();
        this.addCourbeConduite(cc1);
        this.addCourbeConduite(cc2);
        this.addCourbeConduite(cc3);
        this.addCourbeConduite(cc4);
        this.addCourbeConduite(cc5);
        this.addCourbeConduite(cc6);
        this.addEntree(p2);
        this.addEntree(p3);
        this.addEntree(p6);
        this.addSortie(p1);
        this.addSortie(p4);
        this.addSortie(p5);
        this.creerFrontiere(ecartement, ecartement);
    }

    private void creerFrontiere(double ecartementPrincipal, double ecartementSecondaire) {
        Point p1 = new Point(-2 * ecartementSecondaire, ecartementPrincipal);
        Point p2 = new Point(-2 * ecartementSecondaire, -ecartementPrincipal);
        Point p3 = new Point(-ecartementSecondaire, -ecartementPrincipal);
        Point p4 = new Point(-ecartementSecondaire, -2 * ecartementPrincipal);
        Point p5 = new Point(ecartementSecondaire, -2 * ecartementPrincipal);
        Point p6 = new Point(ecartementSecondaire, -ecartementPrincipal);
        Point p7 = new Point(2 * ecartementSecondaire, -ecartementPrincipal);
        Point p8 = new Point(2 * ecartementSecondaire, ecartementPrincipal);
        List<Point> sommets = new ArrayList<Point>();
        sommets.add(p1);
        sommets.add(p2);
        sommets.add(p3);
        sommets.add(p4);
        sommets.add(p5);
        sommets.add(p6);
        sommets.add(p7);
        sommets.add(p8);
        this.creerFrontiere(sommets);
    }

    private void creerFrontiere2(double longueur, double largeur) {
        Point p1 = new Point(-longueur / 2, largeur / 2);
        Point p2 = new Point(-longueur / 2, -largeur / 2);
        Point p3 = new Point(longueur / 2, -largeur / 2);
        Point p4 = new Point(longueur / 2, largeur / 2);
        List<Point> sommets = new ArrayList<Point>();
        sommets.add(p1);
        sommets.add(p2);
        sommets.add(p3);
        sommets.add(p4);
        this.creerFrontiere(sommets);
    }

    public static Carrefour createCarrefourStandard(Point position, Angle3D angle, double ecartementPrincipal, double ecartementSecondaire) {
        return new Carrefour3Branches(position, angle);
    }

    public static Carrefour createCarrefour_4V_2V(Point position, Angle3D angle, double ecartementPrincipal, double ecartementSecondaire) {
        Carrefour3Branches carrefour = new Carrefour3Branches(position, angle);
        double ecartMoy = (ecartementSecondaire + ecartementPrincipal) / 2;
        PointSortie p1 = new PointSortie(-ecartMoy, 3 * ecartementPrincipal / 2);
        PointEntree p2 = new PointEntree(ecartMoy, 3 * ecartementPrincipal / 2);
        PointSortie p3 = new PointSortie(-ecartMoy, ecartementPrincipal / 2);
        PointEntree p4 = new PointEntree(ecartMoy, ecartementPrincipal / 2);
        PointEntree p5 = new PointEntree(-ecartMoy, -ecartementPrincipal / 2);
        PointSortie p6 = new PointSortie(ecartMoy, -ecartementPrincipal / 2);
        PointEntree p7 = new PointEntree(-ecartMoy, -3 * ecartementPrincipal / 2);
        PointSortie p8 = new PointSortie(ecartMoy, -3 * ecartementPrincipal / 2);
        PointSortie p9 = new PointSortie(-ecartementSecondaire / 2, -2 * ecartementPrincipal);
        PointEntree p10 = new PointEntree(ecartementSecondaire / 2, -2 * ecartementPrincipal);
        CourbeConduite cc1 = new CourbeConduite(carrefour, p2, p1);
        cc1.addSegment();
        CourbeConduite cc2 = new CourbeConduite(carrefour, p4, p3);
        cc2.addSegment();
        CourbeConduite cc3 = new CourbeConduite(carrefour, p5, p6);
        cc3.addSegment();
        CourbeConduite cc4 = new CourbeConduite(carrefour, p7, p8);
        cc4.addSegment();
        CourbeConduite cc5 = new CourbeConduite(carrefour, p10, p8);
        cc5.addArc(new Point(ecartMoy, -2 * ecartementPrincipal), ecartementPrincipal / 2, -Math.PI / 2, Math.PI / 2, 0);
        CourbeConduite cc6 = new CourbeConduite(carrefour, p10, p6);
        cc6.addPointIntermediaire(new Point(ecartementSecondaire / 2, -ecartementPrincipal));
        cc6.addSegment();
        cc6.addArc(new Point(ecartMoy, -ecartementPrincipal), ecartementPrincipal / 2, -Math.PI / 2, Math.PI / 2, 0);
        CourbeConduite cc7 = new CourbeConduite(carrefour, p10, p1);
        cc7.addSegment();
        CourbeConduite cc8 = new CourbeConduite(carrefour, p10, p3);
        cc8.addSegment();
        CourbeConduite cc9 = new CourbeConduite(carrefour, p2, p9);
        cc9.addSegment();
        CourbeConduite cc10 = new CourbeConduite(carrefour, p4, p9);
        cc10.addSegment();
        CourbeConduite cc11 = new CourbeConduite(carrefour, p5, p9);
        cc11.addSegment();
        CourbeConduite cc12 = new CourbeConduite(carrefour, p7, p9);
        cc12.addArc(new Point(-ecartMoy, -2 * ecartementPrincipal), ecartementPrincipal / 2, 0, Math.PI / 2, 0);
        carrefour.addCourbeConduite(cc1);
        carrefour.addCourbeConduite(cc2);
        carrefour.addCourbeConduite(cc3);
        carrefour.addCourbeConduite(cc4);
        carrefour.addCourbeConduite(cc5);
        carrefour.addCourbeConduite(cc6);
        carrefour.addCourbeConduite(cc7);
        carrefour.addCourbeConduite(cc8);
        carrefour.addCourbeConduite(cc9);
        carrefour.addCourbeConduite(cc10);
        carrefour.addCourbeConduite(cc11);
        carrefour.addCourbeConduite(cc12);
        carrefour.addEntree(p2);
        carrefour.addEntree(p4);
        carrefour.addEntree(p5);
        carrefour.addEntree(p7);
        carrefour.addEntree(p10);
        carrefour.addSortie(p1);
        carrefour.addSortie(p3);
        carrefour.addSortie(p6);
        carrefour.addSortie(p8);
        carrefour.addSortie(p9);
        carrefour.creerFrontiere2(ecartementSecondaire + ecartementPrincipal, 4 * ecartementPrincipal);
        return carrefour;
    }
}
