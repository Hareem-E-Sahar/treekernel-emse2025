package jts.ihm.gui.ctrl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import javax.swing.JPanel;
import jts.moteur.geometrie.Point;
import jts.moteur.ligne.CircuitSections;
import jts.moteur.ligne.Ligne;
import jts.moteur.ligne.voie.Section;
import jts.moteur.ligne.voie.elements.Arc;
import jts.moteur.ligne.voie.elements.CourbeElementaire;
import jts.moteur.ligne.voie.elements.TypeElement;
import jts.moteur.ligne.voie.points.Divergence;
import jts.moteur.ligne.voie.points.PointFrontiere;
import jts.moteur.ligne.voie.points.PointExtremite;
import jts.moteur.train.Train;

/**Ce panel permet de visualiser la situation de la ligne, vue de dessus en 2D.
 * 
 * @author Yannick BISIAUX
 *
 */
@SuppressWarnings("serial")
public class PanelVisuRoute extends JPanel {

    private static final int X = 1024;

    private static final int Y = 768;

    private static final Dimension DIMENSION = new Dimension(X, Y);

    private static Color BLEU = new Color(0, 0, 255);

    private static Color ROUGE = new Color(255, 0, 0);

    private double xMin = -80;

    private double xMax = 80;

    private double yMin = -60;

    private double yMax = 60;

    private int xDecal;

    private int yDecal;

    private VisuCtrlMouseListener ml;

    private Ligne ligne;

    public PanelVisuRoute() {
        super();
        this.setLayout(null);
        this.ml = new VisuCtrlMouseListener(this);
        this.addMouseListener(ml);
        this.addMouseWheelListener(ml);
        this.addMouseMotionListener(ml);
        this.setPreferredSize(DIMENSION);
        this.repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        if (ligne != null) {
            CircuitSections circuit = ligne.getCircuit();
            for (Section section : circuit.getSections()) {
                if (section.getFrontiere() != null) {
                    paintFrontiere(g2d, section.getFrontiere().getSommets());
                }
                for (CourbeElementaire element : section.getElements()) {
                    paintElement(g2d, element);
                }
                for (PointExtremite pp : section.getPointsPassages()) {
                    int[] point = getPoint(pp);
                    if (pp instanceof PointFrontiere) {
                        if (((PointFrontiere) pp).getConnexion() == null) {
                            g2d.setColor(Color.BLACK);
                            dessinerPoint(g2d, point[0], point[1]);
                        }
                    } else if (pp instanceof Divergence) {
                        g2d.setColor(Color.RED);
                        dessinerPoint(g2d, point[0], point[1]);
                    }
                }
            }
            g2d.setColor(Color.BLACK);
            Train trainJ = ligne.getCircuit().getTrainJoueur();
            int[] pointLoco = getPoint(trainJ.getLocomotiveTete().getPosition());
            dessinerPoint(g2d, pointLoco[0], pointLoco[1]);
            g2d.setColor(Color.GREEN);
            int[] pointBAv = getPoint(trainJ.getLocomotiveTete().getBogieAvant().getPoint());
            dessinerPoint(g2d, pointBAv[0], pointBAv[1]);
            int[] pointBAr = getPoint(trainJ.getLocomotiveTete().getBogieArriere().getPoint());
            dessinerPoint(g2d, pointBAr[0], pointBAr[1]);
        }
    }

    public void paintElement(Graphics2D g2d, CourbeElementaire element) {
        g2d.setColor(Color.BLUE);
        int[] pe = getPoint(element.getP1());
        int[] ps = getPoint(element.getP2());
        GradientPaint rougeBarre = new GradientPaint(pe[0], pe[1], BLEU, ps[0], ps[1], ROUGE);
        g2d.setPaint(rougeBarre);
        g2d.setColor(Color.BLUE);
        if (element.getType().equals(TypeElement.ARC)) {
            Point pt = new Point();
            Arc arc = (Arc) element;
            arc.recupererPoint(pt, 0.3);
            int[] pi1 = getPoint(pt);
            arc.recupererPoint(pt, 0.7);
            int[] pi2 = getPoint(pt);
            g2d.drawLine(pe[0], pe[1], pi1[0], pi1[1]);
            g2d.drawLine(pi1[0], pi1[1], pi2[0], pi2[1]);
            g2d.drawLine(pi2[0], pi2[1], ps[0], ps[1]);
        } else {
            g2d.drawLine(pe[0], pe[1], ps[0], ps[1]);
        }
    }

    public void paintFrontiere(Graphics2D g2d, List<Point> frontiere) {
        g2d.setColor(Color.GRAY);
        if (frontiere.size() > 2) {
            int[] p1 = getPoint(frontiere.get(0));
            int[] p2 = getPoint(frontiere.get(1));
            for (int i = 0; i < frontiere.size() - 1; i++) {
                p1 = getPoint(frontiere.get(i));
                p2 = getPoint(frontiere.get(i + 1));
                g2d.drawLine(p1[0], p1[1], p2[0], p2[1]);
            }
            p1 = getPoint(frontiere.get(frontiere.size() - 1));
            p2 = getPoint(frontiere.get(0));
            g2d.drawLine(p1[0], p1[1], p2[0], p2[1]);
        }
    }

    private void dessinerPoint(Graphics2D g2d, int x, int y) {
        g2d.fillOval(x - 3, y - 3, 7, 7);
    }

    private int[] getPoint(Point p) {
        int[] point = new int[2];
        point[0] = (int) ((p.getX() - xMin) * X / (xMax - xMin)) + xDecal;
        point[1] = (int) (Y - (p.getY() - yMin) * Y / (yMax - yMin)) + yDecal;
        return point;
    }

    protected void getPoint(Point p, int x, int y) {
    }

    public void setLigne(Ligne ligne) {
        this.ligne = ligne;
    }

    protected void appliquerFacteur(double facteur) {
        double xCentre = (xMin + xMax) / 2;
        double yCentre = (yMin + yMax) / 2;
        xMax = (xMax - xCentre) * facteur + xCentre;
        xMin = (xMin - xCentre) * facteur + xCentre;
        yMax = (yMax - yCentre) * facteur + yCentre;
        yMin = (yMin - yCentre) * facteur + yCentre;
    }

    protected void appliquerDecalage(int x, int y) {
        xDecal = x;
        yDecal = y;
    }

    protected void appliquerChangementPDV() {
        double dx = (double) xDecal * ((xMax - xMin) / X);
        double dy = (double) yDecal * ((yMax - yMin) / Y);
        xMax -= dx;
        xMin -= dx;
        yMax += dy;
        yMin += dy;
        xDecal = 0;
        yDecal = 0;
    }
}
