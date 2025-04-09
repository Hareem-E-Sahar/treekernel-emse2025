package org.fudaa.ebli.calque;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.fudaa.ebli.calque.dessin.*;
import org.fudaa.ebli.commun.EbliLib;
import org.fudaa.ebli.geometrie.GrMorphisme;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrVecteur;
import org.fudaa.ebli.trace.TraceLigne;

/**
 * @version $Id: BCalqueFormeInteraction.java,v 1.18 2006-09-19 14:55:45 deniger Exp $
 * @author
 */
public class BCalqueFormeInteraction extends BCalqueInteraction implements MouseListener, MouseMotionListener {

    BCalque calque_;

    private int formeCourante_;

    private GrPoint pointDep_;

    private GrPoint pointFinPrec_;

    private boolean enCreation_;

    private DeForme formeCreation_;

    private final List listeners_;

    /**
   * Constructeur.
   */
    public BCalqueFormeInteraction(final BCalque _support) {
        super();
        calque_ = _support;
        formeCourante_ = DeForme.TRAIT;
        enCreation_ = false;
        formeCreation_ = null;
        listeners_ = new Vector();
    }

    int typeTrait_ = TraceLigne.POINTILLE;

    public void setTypeTrait(final int _t) {
        typeTrait_ = _t;
    }

    public int getTypeTrait() {
        return typeTrait_;
    }

    /**
   * Affectation de la propriete <I>typeForme</I>.
   */
    public void setTypeForme(final int _typeForme) {
        formeCourante_ = _typeForme;
    }

    /**
   * Accesseur de la propriete <I>typeForme</I>. Elle fixe la prochaine forme cree par le calque d'interaction
   * (Rectangle, Cercle, ...) en prenant ses valeurs dans les champs statiques de <I>DeForme</I>.
   */
    public int getTypeForme() {
        return formeCourante_;
    }

    public void addFormeEventListener(final FormeEventListener _l) {
        if (!listeners_.contains(_l)) {
            listeners_.add(_l);
        }
    }

    public void removeFormeEventListener(final FormeEventListener _l) {
        if (listeners_.contains(_l)) {
            listeners_.remove(_l);
        }
    }

    public void removeAllFormeEventListener() {
        if (listeners_ != null) {
            listeners_.clear();
        }
    }

    public void fireFormeEvent(final FormeEvent _evt) {
        for (int i = 0; i < listeners_.size(); i++) {
            ((FormeEventListener) listeners_.get(i)).formeSaisie(_evt);
        }
    }

    /**
   * Methode invoquee lors d'un click de souris (simple ou double). Si la forme courante est:<BR>
   * <UL>
   * <LI>un polygone ou une ligne brisee:<BR>
   * <UL>
   * <LI>simple click: on est en cours de creation, on ajoute un point</LI>
   * <LI>double click: fin de creation, ajout du point et validation de la forme dans le calque dessin.</LI>
   * </UL>
   * <LI>un texte: saisie du texte par boite de dialogue et validation dans le calque dessin.</LI>
   * </UL>
   */
    public void mouseClicked(final MouseEvent _evt) {
        if (isGele()) {
            return;
        }
        final Graphics g = calque_.getGraphics();
        g.setXORMode(calque_.getBackground());
        final GrPoint pointFin = new GrPoint(_evt.getX(), _evt.getY(), 0.);
        final GrMorphisme versReel = getVersReel();
        switch(_evt.getModifiers()) {
            case InputEvent.BUTTON1_MASK:
                switch(_evt.getClickCount()) {
                    case 1:
                        switch(formeCourante_) {
                            case DeForme.POLYGONE:
                            case DeForme.LIGNE_BRISEE:
                                ((DeLigneBrisee) formeCreation_).ajoute(pointFin.applique(versReel));
                                pointDep_ = pointFin;
                                break;
                            case DeForme.TEXTE:
                                formeCreation_ = new DeTexte();
                                ((DeTexte) formeCreation_).setPosition(pointDep_.applique(versReel));
                                final String text = JOptionPane.showInputDialog(EbliLib.getS("Texte"));
                                if (text != null) {
                                    ((DeTexte) formeCreation_).setText(text);
                                    ((DeTexte) formeCreation_).setFont(getFont());
                                    fireFormeEvent(new FormeEvent(this, formeCreation_));
                                }
                                enCreation_ = false;
                                formeCreation_ = null;
                                break;
                            default:
                                break;
                        }
                        break;
                    case 2:
                        switch(formeCourante_) {
                            case DeForme.POLYGONE:
                                if (formeCreation_ == null) {
                                    return;
                                }
                                ((DeLigneBrisee) formeCreation_).ajoute(pointFin.applique(versReel));
                                formeCreation_ = new DePolygone((DeLigneBrisee) formeCreation_);
                                break;
                            case DeForme.LIGNE_BRISEE:
                                if (formeCreation_ == null) {
                                    return;
                                }
                                ((DeLigneBrisee) formeCreation_).ajoute(pointFin.applique(versReel));
                                break;
                            default:
                                break;
                        }
                        fireFormeEvent(new FormeEvent(this, formeCreation_));
                        enCreation_ = false;
                        formeCreation_ = null;
                        break;
                    default:
                }
            default:
        }
    }

    /**
   * Methode inactive.
   */
    public void mouseEntered(final MouseEvent _evt) {
    }

    /**
   * Methode inactive.
   */
    public void mouseExited(final MouseEvent _evt) {
    }

    /**
   * Methode invoquee quand on appuie sur un bouton de la souris. Si la forme courante est une courbe fermee, une main
   * levee, un polygone, ou une ligne brisee, on entre en mode creation pour la nouvelle forme.
   */
    public void mousePressed(final MouseEvent _evt) {
        if (isGele()) {
            return;
        }
        enCreation_ = true;
        pointDep_ = new GrPoint(_evt.getX(), _evt.getY(), 0.);
        pointFinPrec_ = pointDep_;
        if (formeCreation_ == null) {
            switch(formeCourante_) {
                case DeForme.POINT:
                    formeCreation_ = new DePoint(pointDep_.applique(getVersReel()));
                    fireFormeEvent(new FormeEvent(this, formeCreation_));
                    enCreation_ = false;
                    formeCreation_ = null;
                    break;
                case DeForme.COURBE_FERMEE:
                case DeForme.MAIN_LEVEE:
                case DeForme.POLYGONE:
                case DeForme.LIGNE_BRISEE:
                    formeCreation_ = new DeLigneBrisee();
                    ((DeLigneBrisee) formeCreation_).ajoute(pointDep_.applique(getVersReel()));
                    break;
                default:
                    break;
            }
        }
    }

    /**
   * Methode invoquee quand on lache un bouton de la souris. Si la forme courante est un trait, un rectangle, une
   * ellipse, un carre, un cercle, une main levee ou une courbe fermee, on sort du mode creation et on valide la forme
   * dans le calque dessin.
   */
    public void mouseReleased(final MouseEvent _evt) {
        if (isGele()) {
            return;
        }
        final GrPoint pointFin = new GrPoint(_evt.getX(), _evt.getY(), 0.);
        final GrMorphisme versReel = getVersReel();
        switch(formeCourante_) {
            case DeForme.TRAIT:
                formeCreation_ = new DeTrait(pointDep_.applique(versReel), pointFin.applique(versReel));
                break;
            case DeForme.RECTANGLE:
                formeCreation_ = new DeRectangle();
                ((DeRectangle) formeCreation_).ajoute(pointDep_.applique(versReel));
                ((DeRectangle) formeCreation_).ajoute(new GrPoint(pointFin.x_, pointDep_.y_, 0.).applique(versReel));
                ((DeRectangle) formeCreation_).ajoute(pointFin.applique(versReel));
                ((DeRectangle) formeCreation_).ajoute(new GrPoint(pointDep_.x_, pointFin.y_, 0.).applique(versReel));
                break;
            case DeForme.ELLIPSE:
                formeCreation_ = new DeEllipse();
                ((DeEllipse) formeCreation_).ajoute(pointDep_.applique(versReel));
                ((DeEllipse) formeCreation_).ajoute(new GrPoint(pointFin.x_, pointDep_.y_, 0.).applique(versReel));
                ((DeEllipse) formeCreation_).ajoute(pointFin.applique(versReel));
                ((DeEllipse) formeCreation_).ajoute(new GrPoint(pointDep_.x_, pointFin.y_, 0.).applique(versReel));
                break;
            case DeForme.CARRE:
                double dx = pointFin.x_ - pointDep_.x_;
                double dy = pointFin.y_ - pointDep_.y_;
                double adx = Math.abs(dx);
                double ady = Math.abs(dy);
                int signx = dx > 0 ? 1 : -1;
                int signy = dy > 0 ? 1 : -1;
                if (adx < ady) {
                    formeCreation_ = new DeCarre(pointDep_.applique(versReel), new GrVecteur(signx * adx, 0., 0.).applique(versReel), new GrVecteur(0., signy * adx, 0.).applique(versReel));
                } else {
                    formeCreation_ = new DeCarre(pointDep_.applique(versReel), new GrVecteur(signx * ady, 0., 0.).applique(versReel), new GrVecteur(0., signy * ady, 0.).applique(versReel));
                }
                break;
            case DeForme.CERCLE:
                dx = pointFin.x_ - pointDep_.x_;
                dy = pointFin.y_ - pointDep_.y_;
                adx = Math.abs(dx);
                ady = Math.abs(dy);
                signx = dx > 0 ? 1 : -1;
                signy = dy > 0 ? 1 : -1;
                if (adx < ady) {
                    formeCreation_ = new DeCercle(pointDep_.applique(versReel), new GrVecteur(signx * adx, 0., 0.).applique(versReel), new GrVecteur(0., signy * adx, 0.).applique(versReel));
                } else {
                    formeCreation_ = new DeCercle(pointDep_.applique(versReel), new GrVecteur(signx * ady, 0., 0.).applique(versReel), new GrVecteur(0., signy * ady, 0.).applique(versReel));
                }
                break;
            case DeForme.MAIN_LEVEE:
                ((DeLigneBrisee) formeCreation_).ajoute(pointFin.applique(versReel));
                formeCreation_ = new DeMainLevee((DeLigneBrisee) formeCreation_);
                break;
            case DeForme.COURBE_FERMEE:
                ((DeLigneBrisee) formeCreation_).ajoute(pointFin.applique(versReel));
                formeCreation_ = new DeCourbeFermee((DeLigneBrisee) formeCreation_);
                break;
            default:
                return;
        }
        fireFormeEvent(new FormeEvent(this, formeCreation_));
        enCreation_ = false;
        formeCreation_ = null;
    }

    /**
   * Methode invoquee quand on deplace la souris avec un bouton appuye. Si la forme courante est un trait, un rectangle,
   * une ellipse, un carre, un cercle, une main levee ou une courbe fermee, on est en mode creation et on dessine une
   * forme temporaire en pointille qui bouge avec la souris.
   */
    public void mouseDragged(final MouseEvent _evt) {
        if (isGele()) {
            return;
        }
        if (!enCreation_) {
            return;
        }
        final Graphics2D g = (Graphics2D) calque_.getGraphics();
        g.setXORMode(calque_.getBackground());
        final TraceLigne trace = new TraceLigne();
        trace.setCouleur(getForeground());
        trace.setTypeTrait(typeTrait_);
        final int xi = (int) pointDep_.x_;
        final int yi = (int) pointDep_.y_;
        final int xfp = (int) pointFinPrec_.x_;
        final int yfp = (int) pointFinPrec_.y_;
        final int xf = _evt.getX();
        final int yf = _evt.getY();
        switch(formeCourante_) {
            case DeForme.TRAIT:
                trace.dessineTrait(g, xi, yi, xfp, yfp);
                trace.dessineTrait(g, xi, yi, xf, yf);
                break;
            case DeForme.RECTANGLE:
                trace.dessineRectangle(g, Math.min(xi, xfp), Math.min(yi, yfp), Math.abs(xfp - xi), Math.abs(yfp - yi));
                trace.dessineRectangle(g, Math.min(xi, xf), Math.min(yi, yf), Math.abs(xf - xi), Math.abs(yf - yi));
                break;
            case DeForme.ELLIPSE:
                int xm = (xi + xfp) / 2;
                int ym = (yi + yfp) / 2;
                int vxm = (int) ((xfp - xi) * DeEllipse.C_MAGIC);
                int vym = (int) ((yfp - yi) * DeEllipse.C_MAGIC);
                trace.dessineArc(g, xm, yi, xfp, ym, vxm, 0, 0, vym);
                trace.dessineArc(g, xfp, ym, xm, yfp, 0, vym, -vxm, 0);
                trace.dessineArc(g, xm, yfp, xi, ym, -vxm, 0, 0, -vym);
                trace.dessineArc(g, xi, ym, xm, yi, 0, -vym, vxm, 0);
                xm = (xi + xf) / 2;
                ym = (yi + yf) / 2;
                vxm = (int) ((xf - xi) * DeEllipse.C_MAGIC);
                vym = (int) ((yf - yi) * DeEllipse.C_MAGIC);
                trace.dessineArc(g, xm, yi, xf, ym, vxm, 0, 0, vym);
                trace.dessineArc(g, xf, ym, xm, yf, 0, vym, -vxm, 0);
                trace.dessineArc(g, xm, yf, xi, ym, -vxm, 0, 0, -vym);
                trace.dessineArc(g, xi, ym, xm, yi, 0, -vym, vxm, 0);
                break;
            case DeForme.CARRE:
                int dx = xfp - xi;
                int dy = yfp - yi;
                int adx = Math.abs(dx);
                int ady = Math.abs(dy);
                int signx = dx > 0 ? 1 : -1;
                int signy = dy > 0 ? 1 : -1;
                if (adx < ady) {
                    dessineCarreTmp(trace, xi, yi, signx, signy, adx);
                } else {
                    dessineCarreTmp(trace, xi, yi, signx, signy, ady);
                }
                dx = xf - xi;
                dy = yf - yi;
                adx = Math.abs(dx);
                ady = Math.abs(dy);
                signx = dx > 0 ? 1 : -1;
                signy = dy > 0 ? 1 : -1;
                if (adx < ady) {
                    dessineCarreTmp(trace, xi, yi, signx, signy, adx);
                } else {
                    dessineCarreTmp(trace, xi, yi, signx, signy, ady);
                }
                break;
            case DeForme.CERCLE:
                dx = xfp - xi;
                dy = yfp - yi;
                adx = Math.abs(dx);
                ady = Math.abs(dy);
                signx = dx > 0 ? 1 : -1;
                signy = dy > 0 ? 1 : -1;
                if (adx < ady) {
                    dessineCercleTmp(trace, xi, yi, signx, signy, adx);
                } else {
                    dessineCercleTmp(trace, xi, yi, signx, signy, ady);
                }
                dx = xf - xi;
                dy = yf - yi;
                adx = Math.abs(dx);
                ady = Math.abs(dy);
                signx = dx > 0 ? 1 : -1;
                signy = dy > 0 ? 1 : -1;
                if (adx < ady) {
                    dessineCercleTmp(trace, xi, yi, signx, signy, adx);
                } else {
                    dessineCercleTmp(trace, xi, yi, signx, signy, ady);
                }
                break;
            case DeForme.COURBE_FERMEE:
            case DeForme.MAIN_LEVEE:
                final GrPoint pointFin = new GrPoint(xf, yf, 0.);
                ((DeLigneBrisee) formeCreation_).ajoute(pointFin.applique(getVersReel()));
                pointDep_ = pointFin;
                trace.dessineTrait(g, xi, yi, xfp, yfp);
                trace.dessineTrait(g, xi, yi, xf, yf);
                break;
            default:
                break;
        }
        pointFinPrec_ = new GrPoint(xf, yf, 0.);
    }

    /**
   * Methode invoquee quand on deplace la souris sans appuyer sur aucun bouton. Si la forme courante est un polygone, ou
   * une ligne brisee, si on est en mode creation, on dessine un segment temporaire en pointille qui bouge avec la
   * souris.
   */
    public void mouseMoved(final MouseEvent _evt) {
        if (isGele()) {
            return;
        }
        if (!enCreation_) {
            return;
        }
        final Graphics2D g = (Graphics2D) calque_.getGraphics();
        g.setXORMode(calque_.getBackground());
        final TraceLigne trace = new TraceLigne();
        trace.setCouleur(getForeground());
        trace.setTypeTrait(typeTrait_);
        final int xi = (int) pointDep_.x_;
        final int yi = (int) pointDep_.y_;
        final int xfp = (int) pointFinPrec_.x_;
        final int yfp = (int) pointFinPrec_.y_;
        final int xf = _evt.getX();
        final int yf = _evt.getY();
        switch(formeCourante_) {
            case DeForme.POLYGONE:
            case DeForme.LIGNE_BRISEE:
                trace.dessineTrait(g, xi, yi, xfp, yfp);
                trace.dessineTrait(g, xi, yi, xf, yf);
                break;
            default:
                break;
        }
        pointFinPrec_ = new GrPoint(xf, yf, 0.);
    }

    /**
   * Trace d'un carre temporaire.
   */
    private void dessineCarreTmp(final TraceLigne _trace, final int _xi, final int _yi, final int _signx, final int _signy, final int _cote) {
        final Graphics2D g = (Graphics2D) calque_.getGraphics();
        _trace.dessineTrait(g, _xi, _yi, _xi + _signx * _cote, _yi);
        _trace.dessineTrait(g, _xi + _signx * _cote, _yi, _xi + _signx * _cote, _yi + _signy * _cote);
        _trace.dessineTrait(g, _xi + _signx * _cote, _yi + _signy * _cote, _xi, _yi + _signy * _cote);
        _trace.dessineTrait(g, _xi, _yi + _signy * _cote, _xi, _yi);
    }

    /**
   * Trace d'un cercle temporaire.
   */
    private void dessineCercleTmp(final TraceLigne _trace, final int _xi, final int _yi, final int _signx, final int _signy, final int _cote) {
        final int xf = _xi + _signx * _cote;
        final int yf = _yi + _signy * _cote;
        final int xm = (_xi + xf) / 2;
        final int ym = (_yi + yf) / 2;
        final int vxm = (int) ((xf - _xi) * DeEllipse.C_MAGIC);
        final int vym = (int) ((yf - _yi) * DeEllipse.C_MAGIC);
        final Graphics2D g = (Graphics2D) calque_.getGraphics();
        _trace.dessineArc(g, xm, _yi, xf, ym, vxm, 0, 0, vym);
        _trace.dessineArc(g, xf, ym, xm, yf, 0, vym, -vxm, 0);
        _trace.dessineArc(g, xm, yf, _xi, ym, -vxm, 0, 0, -vym);
        _trace.dessineArc(g, _xi, ym, xm, _yi, 0, -vym, vxm, 0);
    }
}
