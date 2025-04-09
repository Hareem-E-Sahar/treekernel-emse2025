package ds.ihm.gui.ctrl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import javax.swing.JPanel;
import ds.moteur.Scene;
import ds.moteur.Terrain;
import ds.moteur.geometrie.Point;
import ds.moteur.route.Section;
import ds.moteur.route.cc.CourbeConduite;
import ds.moteur.route.cc.PointEntree;
import ds.moteur.voiture.Voiture;
import ds.moteur.voiture.VoiturePilotee;

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

    private Scene scene;

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
        if (scene != null) {
            Terrain terrain = scene.getTerrain();
            for (Section section : terrain.getSections()) {
                paintFrontiere(g2d, section.getFrontiere().getSommets());
                for (CourbeConduite cc : section.getCourbesConduites()) {
                    paintCourbe(g2d, cc);
                }
                for (PointEntree entree : section.getEntrees()) {
                    int[] point = getPoint(entree);
                    g2d.setColor(Color.PINK);
                    dessinerPoint(g2d, point[0], point[1]);
                }
            }
            g2d.setColor(Color.BLACK);
            VoiturePilotee voiture = scene.getVoitureJoueur();
            int[] pointVoitureAv = getPoint(voiture.getPositionAvant());
            dessinerPoint(g2d, pointVoitureAv[0], pointVoitureAv[1]);
            g2d.setColor(Color.BLUE);
            int[] pointProjete = getPoint(voiture.getPositionProjetee());
            dessinerPoint(g2d, pointProjete[0], pointProjete[1]);
            paintFrontiere(g2d, voiture.getCarrosserie());
            for (Voiture voiturePNJ : scene.getVoitures()) {
                g2d.setColor(Color.YELLOW);
                int[] pointVoitureAvPNJ = getPoint(voiturePNJ.getPositionAvant());
                dessinerPoint(g2d, pointVoitureAvPNJ[0], pointVoitureAvPNJ[1]);
                paintFrontiere(g2d, voiturePNJ.getCarrosserie());
            }
        }
    }

    public void paintCourbe(Graphics2D g2d, CourbeConduite cc) {
        g2d.setColor(Color.BLUE);
        int[] pe = getPoint(cc.getEntree());
        int[] ps = getPoint(cc.getSortie());
        GradientPaint rougeBarre = new GradientPaint(pe[0], pe[1], BLEU, ps[0], ps[1], ROUGE);
        g2d.setPaint(rougeBarre);
        List<Point> positions = cc.getPositionsIntermediaires();
        for (int i = 0; i < positions.size() - 1; i++) {
            int[] p1 = getPoint(positions.get(i));
            int[] p2 = getPoint(positions.get(i + 1));
            g2d.drawLine(p1[0], p1[1], p2[0], p2[1]);
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
        g2d.fillRect(x - 2, y - 2, 5, 5);
    }

    private int[] getPoint(Point p) {
        int[] point = new int[2];
        point[0] = (int) ((p.x - xMin) * X / (xMax - xMin)) + xDecal;
        point[1] = (int) (Y - (p.y - yMin) * Y / (yMax - yMin)) + yDecal;
        return point;
    }

    protected void getPoint(Point p, int x, int y) {
    }

    public void setScene(Scene scene) {
        this.scene = scene;
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
