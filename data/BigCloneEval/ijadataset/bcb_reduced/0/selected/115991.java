package ui.menus.popup;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.event.MouseInputListener;
import fc.Categorie;
import fc.MyType;
import ui.AireDeDessin;
import ui.arbreUI.TacheUI;
import ui.boutons.PopupBouton;
import ui.fenetres.FenetrePrincipale;

/**
 * @author Bavilavel
 * Pie Menu de manipulation de tache
 */
@SuppressWarnings("serial")
public class PieMenu extends MyPopupMenu implements MouseInputListener, MouseWheelListener {

    private FenetrePrincipale f;

    private int max = 35, min = 25, rayon = 40;

    private float opacite = 0.7f;

    /**
	 * Instance du pie menu
	 * @param aire : aire de dessin dans lequel est affich� ce menu
	 */
    public PieMenu(AireDeDessin aire) {
        super(aire);
        opacite = 0.5f;
        largeur = (int) ((2 * rayon) + max + 10);
        hauteur = largeur;
        Point centre = new Point(largeur / 2, hauteur / 2);
        double angle = 2 * Math.PI / 8;
        double angleInitial = -Math.PI / 2;
        PopupBouton[] listeBoutons = { new PopupBouton(this, Categorie.PARAMETRES, calculPosition(centre, rayon, angleInitial)), new PopupBouton(this, Categorie.ADDNOEUD, calculPosition(centre, rayon, angleInitial + angle)), new PopupBouton(this, Categorie.ADDLINK, calculPosition(centre, rayon, angleInitial + 2 * angle)), new PopupBouton(this, Categorie.DELNOEUD, calculPosition(centre, rayon, angleInitial + 3 * angle)), new PopupBouton(this, Categorie.CACHE, calculPosition(centre, rayon, angleInitial + 4 * angle)), new PopupBouton(this, Categorie.COPIER, calculPosition(centre, rayon, angleInitial + 5 * angle)), new PopupBouton(this, Categorie.COUPER, calculPosition(centre, rayon, angleInitial + 6 * angle)), new PopupBouton(this, Categorie.COLLER, calculPosition(centre, rayon, angleInitial + 7 * angle)) };
        initBoutons(listeBoutons);
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);
    }

    /**
	 * Affiche le menu
	 */
    public void show() {
        TacheUI tacheCourante = (TacheUI) aireDeDessin.getObjetSurvole();
        listeBoutons[2].activer(tacheCourante.getPere() != null && tacheCourante.getPere().getListefils().size() > 1);
        listeBoutons[3].activer(tacheCourante.getPere() != null);
        listeBoutons[7].activer(tacheCourante.getPere() != null);
        listeBoutons[4].setTypeAction(tacheCourante.estDeroule() ? Categorie.CACHE : Categorie.VUE);
        if (tacheCourante != null) {
            int x = (int) tacheCourante.getLocationOnScreen().getX() + aireDeDessin.getObjetSurvole().getWidth() / 2;
            int y = (int) tacheCourante.getLocationOnScreen().getY() + aireDeDessin.getObjetSurvole().getHeight() / 2;
            positionScreen = new Point(x - (largeur / 2), y - (hauteur / 2));
            backgroundImage = robot.createScreenCapture(new Rectangle(positionScreen.x, positionScreen.y, largeur, hauteur));
            repaint();
            popup = factory.getPopup(f, panel, positionScreen.x, positionScreen.y);
            popup.show();
        }
    }

    public void hide() {
        if (isVisible()) super.hide();
    }

    private Point calculPosition(Point centre, int rayon, double angle) {
        return new Point(centre.x + (int) ((Math.cos(angle) * rayon) - (max / 2)), centre.y + (int) ((Math.sin(angle) * rayon) - (max / 2)));
    }

    /**
	 * @return l'opacit� du pie menu
	 */
    public float getOpacite() {
        return opacite;
    }

    /**
	 * Change l'opacit� du pie menu
	 * @param opacite : nouvel opacit� du pie menu
	 */
    public void setOpacite(float opacite) {
        this.opacite = opacite;
    }

    /**
	 * @return le cote maximum d'un bouton
	 */
    public int getMax() {
        return max;
    }

    /**
	 * @return le cot� minimum d'un bouton
	 */
    public int getMin() {
        return min;
    }

    public void mouseClicked(MouseEvent e) {
        if (aireDeDessin.getMenu(MyType.LINK_LINE_MENU).isVisible() || aireDeDessin.getMenu(MyType.TACHE_LINE_MENU).isVisible()) aireDeDessin.cacherTousLesMenus(); else {
            setOpacite(0.5f + ((2 * opacite) % 2) / 2);
            for (PopupBouton b : listeBoutons) b.repaint();
            if (opacite == 1) aireDeDessin.select(aireDeDessin.getObjetSurvole()); else aireDeDessin.unselect(aireDeDessin.getObjetSurvole());
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        if (opacite < 1) {
            aireDeDessin.cacherTousLesMenus();
            if (isVisible()) super.hide();
        }
    }

    public void mousePressed(MouseEvent e) {
        ((TacheUI) aireDeDessin.getObjetSurvole()).mousePressed(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (aireDeDessin.getObjetSurvole().getWidth() / 2) + e.getX() - (getWidth() / 2), (aireDeDessin.getObjetSurvole().getHeight() / 2) + e.getY() - (getHeight() / 2), e.getClickCount(), e.isPopupTrigger()));
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (isVisible()) hide();
        ((TacheUI) aireDeDessin.getObjetSurvole()).mouseDragged(e);
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        hide();
        ((TacheUI) aireDeDessin.getObjetSurvole()).mouseWheelMoved(e);
    }
}
