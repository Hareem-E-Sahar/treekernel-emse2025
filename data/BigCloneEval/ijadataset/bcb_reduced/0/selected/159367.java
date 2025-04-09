package org.fudaa.ebli.calque.edition;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import org.fudaa.ctulu.CtuluLib;
import org.fudaa.ctulu.CtuluLibString;
import org.fudaa.ctulu.gis.GISAttributeInterface;
import org.fudaa.ebli.calque.BCalqueInteraction;
import org.fudaa.ebli.calque.ZCatchEvent;
import org.fudaa.ebli.calque.ZCatchListener;
import org.fudaa.ebli.calque.dessin.DeForme;
import org.fudaa.ebli.calque.dessin.DeLigneBrisee;
import org.fudaa.ebli.calque.dessin.DeMultiPoint;
import org.fudaa.ebli.calque.edition.ZEditionAttibutesContainer.PointData;
import org.fudaa.ebli.commun.EbliLib;
import org.fudaa.ebli.geometrie.GrObjet;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
import org.fudaa.ebli.geometrie.GrPolyligne;
import org.fudaa.ebli.geometrie.VecteurGrPoint;
import org.fudaa.ebli.trace.TraceGeometrie;
import org.fudaa.ebli.trace.TraceLigne;
import com.memoire.bu.BuResource;
import java.util.HashSet;
import java.util.List;
import org.fudaa.ctulu.gis.CtuluLibGeometrie;
import org.fudaa.ebli.geometrie.GrSegment;
import org.fudaa.ebli.trace.TraceIcon;
import org.fudaa.ebli.trace.TraceIconModel;
import org.fudaa.ebli.trace.TraceLigneModel;

/**
 * Permet la saisie interactive de formes g�om�triques.
 * @version $Id: ZCalqueEditionInteraction.java 6743 2011-12-01 10:17:04Z bmarchan $
 * @author
 */
public class ZCalqueEditionInteraction extends BCalqueInteraction implements KeyListener, MouseListener, MouseMotionListener, ZCatchListener {

    /**
   * La classe deleguee pour la saisie de forme.
   * 
   * @author Fred Deniger
   * @version $Id: ZCalqueEditionInteraction.java 6743 2011-12-01 10:17:04Z bmarchan $
   */
    public abstract class FormDelegate extends MouseAdapter implements KeyListener, MouseMotionListener {

        /** LE dernier point d'accrochage definit */
        GrPoint ptAccro_ = null;

        /**
     * Enleve le dernier point saisie.
     */
        public void removeLastSaisie() {
            cancelCurrentForme();
        }

        public void effaceDessin() {
        }

        public void mouseDragged(final MouseEvent _e) {
        }

        public void mouseMoved(final MouseEvent _e) {
        }

        abstract void cancelCurrentForme();

        abstract void addCurrentForme();

        /**
     * @return true si saisie en cours
     */
        public abstract boolean enCours();

        /**
     * @return les donn�es associ�es a chaque point
     */
        public abstract ZEditionAttributesDataI getData();

        /**
     * @return l'indice de la forme
     * @see DeForme
     */
        public abstract int getForme();

        /**
     * @return la forme en cours
     */
        public abstract GrObjet getFormeEnCours();

        /**
     * @return la description de la forme
     */
        public abstract String getFormeDesc();

        /**
     * @param _g le grahics support
     */
        public void paintComponent(final Graphics _g) {
        }

        public void paint(final Graphics _g) {
        }

        public void keyPressed(KeyEvent _e) {
        }

        public void keyReleased(KeyEvent _e) {
        }

        public void keyTyped(KeyEvent _e) {
        }

        public void setAccroche(GrPoint _ptReel) {
            ptAccro_ = _ptReel;
        }
    }

    class FormDelegateLigneBrisee extends FormDelegate {

        private HashSet<Integer> keyPressed_ = new HashSet<Integer>();

        ZEditionAttibutesContainer.LigneBriseeData data_;

        boolean isFerme_;

        DeLigneBrisee ligne_;

        GrPoint pointDep_;

        GrPoint pointFin_;

        int x1_;

        int x2_;

        int xPivot_;

        int y1_;

        int y2_;

        int yPivot_;

        /**
     * @param _isFerme Si la polyligne est ferm�e.
     * @param _isReliee Si la polyligne est reli�e.
     */
        public FormDelegateLigneBrisee(final boolean _isFerme) {
            super();
            isFerme_ = _isFerme;
        }

        @Override
        public void removeLastSaisie() {
            if (ligne_.getNombre() <= 1) {
                cancelCurrentForme();
            } else {
                final int x = xPivot_;
                final int y = yPivot_;
                effaceDessin();
                final Graphics2D g2d = (Graphics2D) getGraphics();
                g2d.setXORMode(getBackground());
                ligne_.affiche(g2d, tl_, isRapide(), tmp_, getVersEcran());
                ligne_.enleveDernier();
                data_.removeLastInfo();
                if (pointFin_ != null) {
                    pointFin_ = ligne_.getDernier();
                }
                updateMouseMoved(x, y);
                if (support_ != null) support_.atomicChanged();
            }
        }

        private boolean polyAdded() {
            if (support_ == null) return false;
            support_.unsetMessage();
            final GrObjet o = ligne_.getGeometrie();
            final DeLigneBrisee old = ligne_;
            ligne_ = null;
            boolean ok = true;
            if (isFerme_) {
                final GrPolyligne poly = (GrPolyligne) o;
                ok = support_.addNewPolygone(poly.toGrPolygone(), data_);
            } else {
                ok = support_.addNewPolyligne((GrPolyligne) o, data_);
            }
            if (ok) {
                repaint();
            } else {
                ligne_ = old;
            }
            return ok;
        }

        void cancelCurrentForme() {
            effaceDessin();
            ligne_ = null;
            data_ = null;
            if (support_ != null) {
                support_.unsetMessage();
                support_.atomicChanged();
            }
        }

        void dessineTrait(final int _xPivot, final int _yPivot, final int _x1, final int _y1, final int _x2, final int _y2) {
            if (enCours()) {
                xPivot_ = _xPivot;
                yPivot_ = _yPivot;
                x1_ = _x1;
                y1_ = _y1;
                x2_ = _x2;
                y2_ = _y2;
                final Graphics2D g = (Graphics2D) getGraphics();
                g.setXORMode(Color.white);
                tl_.dessineTrait(g, xPivot_, yPivot_, x1_, y1_);
                if (isFerme_ && ligne_.getNombre() > 1) {
                    tl_.dessineTrait(g, xPivot_, yPivot_, x2_, y2_);
                }
                isPaint_ = true;
            }
        }

        @Override
        public void effaceDessin() {
            if (isPaint_ && enCours()) {
                final Graphics g = getGraphics();
                g.setXORMode(Color.white);
                tl_.dessineTrait((Graphics2D) g, xPivot_, yPivot_, x1_, y1_);
                if (isFerme_ && ligne_.getNombre() > 1) {
                    tl_.dessineTrait((Graphics2D) g, xPivot_, yPivot_, x2_, y2_);
                }
                isPaint_ = false;
            }
        }

        public ZEditionAttributesDataI getData() {
            return data_;
        }

        public GrObjet getFormeEnCours() {
            return ligne_ == null ? null : ligne_.getGeometrie();
        }

        public boolean enCours() {
            return ligne_ != null;
        }

        public int getForme() {
            return isFerme_ ? DeForme.POLYGONE : DeForme.LIGNE_BRISEE;
        }

        public String getFormeDesc() {
            return EbliLib.getS(isFerme_ ? "Polygone" : "Polyligne");
        }

        @Override
        public void mouseDragged(final MouseEvent _e) {
            mouseMoved(_e);
        }

        @Override
        public void mouseEntered(final MouseEvent _e) {
            if (enCours()) {
                effaceDessin();
            }
        }

        @Override
        public void mouseExited(final MouseEvent _e) {
            if (enCours()) {
                effaceDessin();
            }
        }

        @Override
        public void mouseMoved(final MouseEvent _e) {
            updateMouseMoved(_e.getX(), _e.getY());
        }

        @Override
        public void keyPressed(KeyEvent _e) {
            keyPressed_.add(_e.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent _e) {
            keyPressed_.remove(_e.getKeyCode());
        }

        /**
     * Corrige les coordonn�es ecran de la souris si on force l'alignement. 
     * Sinon retourne le point en entr�e.
     * @param _pt Le point avant correction.
     * @return Le point apr�s correction.
     */
        private GrPoint alignPoint(GrPoint _pt) {
            if (ligne_.getNombre() < 2) return _pt;
            if (keyModifiers_.contains(KeyEvent.VK_SHIFT) || keyPressed_.contains(KeyEvent.VK_SHIFT)) {
                GrSegment sgprec = new GrSegment();
                sgprec.o_ = ligne_.getSommet(ligne_.getNombre() - 2).applique(getVersEcran());
                sgprec.e_ = ligne_.getSommet(ligne_.getNombre() - 1).applique(getVersEcran());
                return sgprec.pointPlusProcheSurDroiteXY(_pt);
            }
            return _pt;
        }

        protected void updateMouseMoved(final int _x, final int _y) {
            if (enCours()) {
                effaceDessin();
                ligne_.affiche((Graphics2D) getGraphics(), tl_, isRapide(), tmp_, getVersEcran());
                tmp_.initialiseAvec(pointFin_);
                tmp_.autoApplique(getVersEcran());
                GrPoint pt = alignPoint(new GrPoint(_x, _y, 0));
                if (!isFerme_ || ligne_.getNombre() == 1) {
                    dessineTrait((int) pt.x_, (int) pt.y_, (int) tmp_.x_, (int) tmp_.y_, -1, -1);
                } else {
                    final int x1Tmp = (int) tmp_.x_;
                    final int y1Tmp = (int) tmp_.y_;
                    tmp_.initialiseAvec(pointDep_);
                    tmp_.autoApplique(getVersEcran());
                    dessineTrait((int) pt.x_, (int) pt.y_, x1Tmp, y1Tmp, (int) tmp_.x_, (int) tmp_.y_);
                }
                tmp_.x_ = pt.x_;
                tmp_.y_ = pt.y_;
                tmp_.autoApplique(getVersReel());
                if (support_ != null && ptAccro_ == null) {
                    support_.setMessage(CtuluLib.getS("Distance:") + CtuluLibString.ESPACE + CtuluLib.DEFAULT_NUMBER_FORMAT.format(CtuluLibGeometrie.getDistance(tmp_.x_, tmp_.y_, pointFin_.x_, pointFin_.y_)));
                }
            } else {
                if (support_ != null && ptAccro_ == null) support_.unsetMessage();
            }
        }

        @Override
        void addCurrentForme() {
            if (polyAdded() && ligne_ != null) ligne_.affiche((Graphics2D) getGraphics(), tl_, isRapide(), tmp_, getVersEcran());
        }

        @Override
        public void mouseReleased(final MouseEvent _e) {
            effaceDessin();
            if (ligne_ == null) {
                ligne_ = new DeLigneBrisee();
                if (features_ == null) {
                    data_ = null;
                } else {
                    data_ = features_.createLigneBriseeData();
                }
                if (ptAccro_ == null) pointDep_ = getPointReel(_e); else pointDep_ = ptAccro_;
                pointDep_.autoApplique(getVersEcran());
                pointDep_ = alignPoint(pointDep_).applique(getVersReel());
                pointFin_ = pointDep_;
                ligne_.ajoute(pointDep_);
                if (data_ != null) {
                    data_.addPoint(ptAccro_);
                }
                if (support_ != null) {
                    support_.atomicChanged();
                }
            } else {
                boolean afficheLigne = true;
                if (_e.getClickCount() < 2) {
                    if (ptAccro_ == null) pointFin_ = getPointReel(_e); else pointFin_ = ptAccro_;
                    pointFin_.autoApplique(getVersEcran());
                    pointFin_ = alignPoint(pointFin_).applique(getVersReel());
                    ligne_.ajoute(pointFin_);
                    if (data_ != null) {
                        data_.addPoint(ptAccro_);
                    }
                    if (_e.isControlDown()) {
                        afficheLigne = polyAdded();
                    } else {
                        if (support_ != null) {
                            support_.atomicChanged();
                        }
                    }
                } else if (_e.getClickCount() == 2) {
                    afficheLigne = polyAdded();
                }
                if (afficheLigne && ligne_ != null) {
                    ligne_.affiche((Graphics2D) getGraphics(), tl_, isRapide(), tmp_, getVersEcran());
                }
            }
            mouseMoved(_e);
        }

        @Override
        public void paintComponent(final Graphics _g) {
            effaceDessin();
            if (ligne_ != null) {
                ligne_.affiche((Graphics2D) _g, tl_, isRapide(), tmp_, getVersEcran());
            }
        }
    }

    class FormDelegateMultiPoint extends FormDelegate {

        ZEditionAttibutesContainer.MultiPointData data_;

        DeMultiPoint multipoint_;

        public FormDelegateMultiPoint() {
            super();
        }

        public void removeLastSaisie() {
            final Graphics2D g2d = (Graphics2D) getGraphics();
            g2d.setXORMode(getBackground());
            multipoint_.affiche(g2d, tp_, isRapide(), tmp_, getVersEcran());
            if (multipoint_.getNombre() <= 1) {
                cancelCurrentForme();
            } else {
                multipoint_.enleveDernier();
                data_.removeLastInfo();
                multipoint_.affiche(g2d, tp_, isRapide(), tmp_, getVersEcran());
                if (support_ != null) support_.atomicChanged();
            }
        }

        private boolean polyAdded() {
            if (support_ == null) return false;
            support_.unsetMessage();
            final GrObjet o = multipoint_.getGeometrie();
            final DeMultiPoint old = multipoint_;
            multipoint_ = null;
            boolean ok = support_.addNewMultiPoint((GrPolyligne) o, data_);
            if (ok) {
                repaint();
            } else {
                multipoint_ = old;
            }
            return ok;
        }

        void cancelCurrentForme() {
            effaceDessin();
            multipoint_ = null;
            data_ = null;
            if (support_ != null) {
                support_.unsetMessage();
                support_.atomicChanged();
            }
        }

        public ZEditionAttributesDataI getData() {
            return data_;
        }

        public GrObjet getFormeEnCours() {
            return multipoint_ == null ? null : multipoint_.getGeometrie();
        }

        public boolean enCours() {
            return multipoint_ != null;
        }

        public int getForme() {
            return DeForme.MULTI_POINT;
        }

        public String getFormeDesc() {
            return EbliLib.getS("Multipoint");
        }

        public void mouseEntered(final MouseEvent _e) {
            if (enCours()) {
                multipoint_.affiche((Graphics2D) getGraphics(), tp_, isRapide(), tmp_, getVersEcran());
            }
        }

        void addCurrentForme() {
            if (polyAdded() && multipoint_ != null) multipoint_.affiche((Graphics2D) getGraphics(), tp_, isRapide(), tmp_, getVersEcran());
        }

        public void mouseReleased(final MouseEvent _e) {
            if (multipoint_ == null) {
                multipoint_ = new DeMultiPoint();
                if (features_ == null) {
                    data_ = null;
                } else {
                    data_ = features_.createMultiPointData();
                }
            }
            boolean afficheLigne = true;
            if (_e.getClickCount() < 2) {
                GrPoint pt = ptAccro_ == null ? getPointReel(_e) : ptAccro_;
                multipoint_.ajoute(pt);
                if (data_ != null) {
                    data_.addPoint(ptAccro_);
                }
                if (_e.isControlDown()) {
                    afficheLigne = polyAdded();
                } else {
                    if (support_ != null) {
                        support_.atomicChanged();
                    }
                }
            } else if (_e.getClickCount() == 2) {
                afficheLigne = polyAdded();
            }
            if (afficheLigne && multipoint_ != null) {
                multipoint_.affiche((Graphics2D) getGraphics(), tp_, isRapide(), tmp_, getVersEcran());
            }
        }

        public void paintComponent(final Graphics _g) {
            if (multipoint_ != null) {
                multipoint_.affiche((Graphics2D) _g, tp_, isRapide(), tmp_, getVersEcran());
            }
        }
    }

    class FormDelegatePoint extends FormDelegate {

        private GrPoint pt_ = null;

        void cancelCurrentForme() {
        }

        public ZEditionAttributesDataI getData() {
            return null;
        }

        public GrObjet getFormeEnCours() {
            return null;
        }

        public boolean enCours() {
            return false;
        }

        public int getForme() {
            return DeForme.POINT;
        }

        public String getFormeDesc() {
            return EbliLib.getS("Point");
        }

        public void mouseReleased(final MouseEvent _e) {
            pt_ = new GrPoint(_e.getX(), _e.getY(), 0);
            pt_.autoApplique(getVersReel());
            addCurrentForme();
        }

        void addCurrentForme() {
            if (pt_ == null) return;
            if (support_ == null) return;
            final PointData d = features_ == null ? null : features_.createPointData();
            if (ptAccro_ != null) pt_ = ptAccro_;
            if (d != null) d.addPoint(ptAccro_);
            support_.addNewPoint(pt_, d);
            pt_ = null;
        }
    }

    class FormDelegateEllipse extends FormDelegate {

        private GrPoint origine_;

        private double grandRayon_, petitRayon_;

        private int nbPoints_;

        private ZEditionAttibutesContainer.LigneBriseeData data_;

        private boolean moved_;

        private List<Integer> keyPresse_;

        private List<JComponent> editorComp_;

        private List<GISAttributeInterface> attEditable_;

        public FormDelegateEllipse() {
            keyPresse_ = new ArrayList<Integer>();
            grandRayon_ = 0;
            petitRayon_ = 0;
            nbPoints_ = 3;
            moved_ = false;
        }

        public GrPoint getPointOrigine() {
            return origine_;
        }

        public void setPointOrigine(GrPoint _p) {
            if (_p != null) {
                effaceDessin();
                origine_ = _p;
            }
        }

        public double getRayonX() {
            return grandRayon_;
        }

        public double getRayonY() {
            return petitRayon_;
        }

        /**
     * Perme de renseigner le rayon sur les X de l'ellipse.
     * @param _rayon
     */
        public void setRayonX(double _rayon) {
            effaceDessin();
            if (_rayon >= 0) grandRayon_ = _rayon; else grandRayon_ = 0;
        }

        /**
     * Perme de renseigner le rayon sur les Y de l'ellipse.
     * @param _rayon
     */
        public void setRayonY(double _rayon) {
            effaceDessin();
            if (_rayon >= 0) petitRayon_ = _rayon; else petitRayon_ = 0;
        }

        public void setNbPoints(int _nb) {
            effaceDessin();
            if (_nb >= 3) nbPoints_ = _nb; else nbPoints_ = 3;
        }

        /**
     * Permet � un �l�ment ext�rieur de forcer la construction de la forme.
     */
        public boolean buildEllipse() {
            if (origine_ != null && grandRayon_ > 0 && petitRayon_ > 0) {
                effaceDessin();
                addCurrentForme();
                if (support_ != null) {
                    support_.atomicChanged();
                }
                return true;
            }
            return false;
        }

        public void mouseDragged(final MouseEvent _e) {
            mouseMoved(_e);
        }

        public void mousePressed(final MouseEvent _e) {
            if (origine_ == null) {
                origine_ = getPointReel(_e);
                if (ptAccro_ != null) origine_ = ptAccro_;
                if (support_ != null) support_.atomicChanged();
                moved_ = false;
            }
        }

        public void mouseMoved(final MouseEvent _e) {
            if (enCours()) {
                moved_ = true;
                effaceDessin();
                GrPoint pt = getPointReel(_e);
                if (ptAccro_ != null) pt = ptAccro_;
                grandRayon_ = Math.abs(pt.x_ - origine_.x_);
                petitRayon_ = Math.abs(pt.y_ - origine_.y_);
                if (keyPresse_.contains(KeyEvent.VK_SHIFT) || keyModifiers_.contains(KeyEvent.VK_SHIFT)) {
                    grandRayon_ = (grandRayon_ + petitRayon_) / 2;
                    petitRayon_ = grandRayon_;
                }
                dessineEllipse();
                if (support_ != null) support_.pointMove(origine_.x_ + grandRayon_, origine_.y_ + petitRayon_);
            }
        }

        public void mouseReleased(final MouseEvent _e) {
            if (origine_ != null && moved_ && (_e.getClickCount() < 2) && grandRayon_ > 0 && petitRayon_ > 0) {
                effaceDessin();
                addCurrentForme();
                if (support_ != null) support_.atomicChanged();
            }
        }

        public void mouseExited(final MouseEvent _e) {
            if (enCours()) effaceDessin();
        }

        @Override
        public void keyPressed(KeyEvent _e) {
            if (!keyPresse_.contains(_e.getKeyCode())) {
                keyPresse_.add(_e.getKeyCode());
            }
        }

        @Override
        public void keyReleased(KeyEvent _e) {
            if (keyPresse_.contains(_e.getKeyCode())) keyPresse_.remove((Integer) _e.getKeyCode());
        }

        @Override
        public void keyTyped(KeyEvent _e) {
        }

        public void setDataAttributs(List<GISAttributeInterface> _attEditable, List<JComponent> _comps) {
            attEditable_ = _attEditable;
            editorComp_ = _comps;
        }

        public boolean isSetDataAttributs() {
            return attEditable_ != null && editorComp_ != null;
        }

        private void addData() {
            if (data_ != null && attEditable_ != null) {
                for (int j = 0; j < attEditable_.size(); j++) {
                    GISAttributeInterface key = attEditable_.get(j);
                    Object value = key.getEditor().getValue(editorComp_.get(j));
                    if (data_.atomicAttribute_.containsKey(key)) {
                        ArrayList<Object> values = data_.atomicAttribute_.get(key);
                        for (int i = 0; i < values.size(); i++) values.set(i, value);
                    } else {
                        ArrayList<Object> values = new ArrayList<Object>(data_.nbGeom_);
                        for (int i = 0; i < data_.nbGeom_; i++) values.add(value);
                        data_.atomicAttribute_.put(key, values);
                    }
                }
            }
        }

        private GrPolygone getPolygone(boolean _withData) {
            if (data_ == null && features_ != null) data_ = features_.createEllipseData();
            if (data_ == null) _withData = false;
            GrPolygone poly = new GrPolygone();
            if (origine_ == null) return poly;
            if (grandRayon_ == 0 || petitRayon_ == 0) {
                poly.sommets_.ajoute(origine_);
                return poly;
            }
            final double incT = (2 * Math.PI) / nbPoints_;
            if (_withData) {
                data_.atomicAttribute_.clear();
                data_.nbGeom_ = 0;
            }
            for (double t = 0; t < 2 * Math.PI; t += incT) {
                poly.sommets_.ajoute(getXEllipse(t) + origine_.x_, getYEllipse(t) + origine_.y_, 0);
                if (_withData) data_.addPoint(null);
            }
            if (_withData) addData();
            return poly;
        }

        private void realiseDessin() {
            if (origine_ != null && grandRayon_ != 0 && petitRayon_ != 0) {
                final Graphics2D g = (Graphics2D) getGraphics();
                g.setXORMode(Color.white);
                VecteurGrPoint points = getPolygone(false).sommets_;
                if (points.nombre() < 10) {
                    GrPoint tmp = new GrPoint();
                    tmp.initialiseAvec(origine_);
                    tmp.autoApplique(getVersEcran());
                    TraceGeometrie tg = new TraceGeometrie(getVersEcran());
                    tg.setForeground(tp_.getCouleur());
                    tg.dessineEllipse(g, new GrPoint(origine_.x_ + grandRayon_, origine_.y_ + petitRayon_, 0), new GrPoint(origine_.x_ - grandRayon_, origine_.y_ + petitRayon_, 0), new GrPoint(origine_.x_ - grandRayon_, origine_.y_ - petitRayon_, 0), new GrPoint(origine_.x_ + grandRayon_, origine_.y_ - petitRayon_, 0), false, false);
                }
                GrPoint pointPre = points.renvoie(0);
                pointPre.autoApplique(getVersEcran());
                GrPoint pointAct;
                for (int i = 1; i < points.nombre(); i++) {
                    pointAct = points.renvoie(i);
                    pointAct.autoApplique(getVersEcran());
                    tl_.dessineTrait(g, pointPre.x_, pointPre.y_, pointAct.x_, pointAct.y_);
                    pointPre = pointAct;
                }
                pointAct = points.renvoie(0);
                tl_.dessineTrait(g, pointPre.x_, pointPre.y_, pointAct.x_, pointAct.y_);
            }
        }

        public void effaceDessin() {
            if (isPaint_ && enCours()) {
                realiseDessin();
                isPaint_ = false;
            }
        }

        private void dessineEllipse() {
            realiseDessin();
            isPaint_ = true;
        }

        private double getXEllipse(double t) {
            return grandRayon_ * Math.cos(t);
        }

        private double getYEllipse(double t) {
            return petitRayon_ * Math.sin(t);
        }

        @Override
        void addCurrentForme() {
            if (origine_ != null && grandRayon_ > 0 && petitRayon_ > 0) {
                if (support_ != null) support_.addNewPolygone(getPolygone(true), data_);
                origine_ = null;
                grandRayon_ = 0;
                petitRayon_ = 0;
                data_ = null;
            }
        }

        @Override
        void cancelCurrentForme() {
            effaceDessin();
            origine_ = null;
            grandRayon_ = 0;
            petitRayon_ = 0;
            moved_ = false;
            data_ = null;
            if (support_ != null) support_.atomicChanged();
        }

        @Override
        public boolean enCours() {
            return origine_ != null;
        }

        @Override
        public ZEditionAttributesDataI getData() {
            return data_;
        }

        @Override
        public int getForme() {
            return DeForme.ELLIPSE;
        }

        @Override
        public String getFormeDesc() {
            return EbliLib.getS("Ellipse");
        }

        @Override
        public GrObjet getFormeEnCours() {
            return getPolygone(true);
        }
    }

    class FormDelegateRectangle extends FormDelegate {

        private ZEditionAttibutesContainer.RectangleData data_;

        private boolean moved_;

        private GrPoint pointDep_, pointFin_;

        private List<Integer> keyPresse_;

        private int nbPointsLargeur_, nbPointsHauteur_;

        private List<JComponent> editorComp_;

        private List<GISAttributeInterface> attEditable_;

        public FormDelegateRectangle() {
            super();
            keyPresse_ = new ArrayList<Integer>();
            nbPointsLargeur_ = 0;
            nbPointsHauteur_ = 0;
        }

        public void setDataAttributs(List<GISAttributeInterface> _attEditable, List<JComponent> _comps) {
            attEditable_ = _attEditable;
            editorComp_ = _comps;
        }

        public boolean isSetDataAttributs() {
            return attEditable_ != null && editorComp_ != null;
        }

        /**
     * Permet de renseigner le point d'origine de la forme.
     * 
     * @param _origine
     */
        public void setPointOrigine(GrPoint _origine) {
            if (_origine != null) {
                effaceDessin();
                pointDep_ = _origine;
            }
        }

        /**
     * Permet de renseigner le point final de la forme.
     * 
     * @param _fin
     */
        public void setPointFin(GrPoint _fin) {
            if (_fin != null) {
                effaceDessin();
                pointFin_ = _fin;
            }
        }

        /**
     * Permet � un �l�ment ext�rieur de forcer la construction de la forme.
     */
        public boolean buildRectangle() {
            if (pointDep_ != null && pointFin_ != null) {
                effaceDessin();
                addCurrentForme();
                if (support_ != null) {
                    support_.atomicChanged();
                }
                return true;
            } else return false;
        }

        public void setNbPointsLargeur(int _nb) {
            effaceDessin();
            if (_nb >= 0) nbPointsLargeur_ = _nb; else nbPointsLargeur_ = 0;
        }

        public void setNbPointsHauteur(int _nb) {
            effaceDessin();
            if (_nb >= 0) nbPointsHauteur_ = _nb; else nbPointsHauteur_ = 0;
        }

        public void cancelCurrentForme() {
            effaceDessin();
            pointDep_ = null;
            pointFin_ = null;
            moved_ = false;
            data_ = null;
            nbPointsLargeur_ = 0;
            nbPointsHauteur_ = 0;
            if (support_ != null) support_.atomicChanged();
        }

        private void realiseDessin() {
            final Graphics2D g = (Graphics2D) getGraphics();
            g.setXORMode(Color.white);
            VecteurGrPoint points = getPolygone(false).sommets_;
            GrPoint pointPre = points.renvoie(0);
            pointPre.autoApplique(getVersEcran());
            GrPoint pointAct;
            for (int i = 1; i < points.nombre(); i++) {
                pointAct = points.renvoie(i);
                pointAct.autoApplique(getVersEcran());
                tl_.dessineTrait(g, pointPre.x_, pointPre.y_, pointAct.x_, pointAct.y_);
                pointPre = pointAct;
            }
            pointAct = points.renvoie(0);
            tl_.dessineTrait(g, pointPre.x_, pointPre.y_, pointAct.x_, pointAct.y_);
        }

        private void dessineRect() {
            if (enCours()) {
                realiseDessin();
                isPaint_ = true;
            }
        }

        public void effaceDessin() {
            if (isPaint_ && enCours()) {
                realiseDessin();
                isPaint_ = false;
            }
        }

        public ZEditionAttributesDataI getData() {
            return data_;
        }

        public GrObjet getFormeEnCours() {
            return pointDep_;
        }

        public GrObjet getPointTmp() {
            return pointFin_;
        }

        public boolean enCours() {
            return pointDep_ != null;
        }

        public int getForme() {
            return DeForme.RECTANGLE;
        }

        public String getFormeDesc() {
            return EbliLib.getS("Rectangle");
        }

        public void mouseDragged(final MouseEvent _e) {
            mouseMoved(_e);
        }

        public void mouseExited(final MouseEvent _e) {
            if (enCours()) {
                effaceDessin();
            }
        }

        public void mousePressed(final MouseEvent _e) {
            if (pointDep_ == null) {
                pointDep_ = getPointReel(_e);
                if (ptAccro_ != null) pointDep_ = ptAccro_;
                if (support_ != null) support_.atomicChanged();
                moved_ = false;
            }
        }

        public void mouseMoved(MouseEvent _e) {
            if (enCours()) {
                effaceDessin();
                moved_ = true;
                pointFin_ = getPointReel(_e);
                if (ptAccro_ != null) pointFin_ = ptAccro_;
                if (keyPresse_.contains(KeyEvent.VK_SHIFT) || keyModifiers_.contains(KeyEvent.VK_SHIFT)) {
                    double cote = (Math.abs(pointDep_.x_ - pointFin_.x_) + Math.abs(pointDep_.y_ - pointFin_.y_)) / 2;
                    if (pointFin_.x_ < pointDep_.x_) pointFin_.x_ = pointDep_.x_ - cote; else pointFin_.x_ = pointDep_.x_ + cote;
                    if (pointFin_.y_ < pointDep_.y_) pointFin_.y_ = pointDep_.y_ - cote; else pointFin_.y_ = pointDep_.y_ + cote;
                }
                dessineRect();
                if (support_ != null) support_.pointMove(pointFin_.x_, pointFin_.y_);
            }
        }

        public void mouseReleased(final MouseEvent _e) {
            if (pointDep_ != null && pointFin_ != null && moved_ && (_e.getClickCount() < 2) && pointDep_.x_ != pointFin_.x_ && pointDep_.y_ != pointFin_.y_) {
                effaceDessin();
                addCurrentForme();
                if (support_ != null) {
                    support_.atomicChanged();
                }
            }
        }

        public void keyPressed(KeyEvent _e) {
            if (!keyPresse_.contains(_e.getKeyCode())) {
                keyPresse_.add(_e.getKeyCode());
            }
        }

        public void keyReleased(KeyEvent _e) {
            if (keyPresse_.contains(_e.getKeyCode())) keyPresse_.remove((Integer) _e.getKeyCode());
        }

        public void keyTyped(KeyEvent _e) {
        }

        private void addData() {
            if (data_ != null && attEditable_ != null) {
                for (int j = 0; j < attEditable_.size(); j++) {
                    GISAttributeInterface key = attEditable_.get(j);
                    Object value = key.getEditor().getValue(editorComp_.get(j));
                    if (data_.atomicAttribute_.containsKey(key)) {
                        ArrayList<Object> values = data_.atomicAttribute_.get(key);
                        for (int i = 0; i < values.size(); i++) values.set(i, value);
                    } else {
                        ArrayList<Object> values = new ArrayList<Object>(data_.nbGeom_);
                        for (int i = 0; i < data_.nbGeom_; i++) values.add(value);
                        data_.atomicAttribute_.put(key, values);
                    }
                }
            }
        }

        private GrPolygone getPolygone(boolean _withData) {
            if (data_ == null && features_ != null) data_ = features_.createRectangleData();
            if (data_ == null) _withData = false;
            GrPolygone poly = new GrPolygone();
            if (pointDep_ == null) return poly;
            if (pointFin_ == null) {
                poly.sommets_.ajoute(pointDep_);
                return poly;
            }
            if (_withData) {
                data_.atomicAttribute_.clear();
                data_.nbGeom_ = 0;
            }
            double x0 = Math.min(pointDep_.x_, pointFin_.x_), y0 = Math.min(pointDep_.y_, pointFin_.y_), z0 = pointDep_.z_, x1 = Math.max(pointDep_.x_, pointFin_.x_), y1 = Math.max(pointDep_.y_, pointFin_.y_);
            double largeur = Math.abs(x1 - x0), hauteur = Math.abs(y1 - y0);
            poly.sommets_.ajoute(x0, y0, z0);
            if (_withData) data_.addPoint(null);
            for (int i = 0; i < nbPointsHauteur_; i++) {
                poly.sommets_.ajoute(x0, y0 + (i + 1) * (hauteur / (nbPointsHauteur_ + 1)), z0);
                if (_withData) data_.addPoint(null);
            }
            poly.sommets_.ajoute(x0, y1, z0);
            if (_withData) data_.addPoint(null);
            for (int i = 0; i < nbPointsLargeur_; i++) {
                poly.sommets_.ajoute(x0 + (i + 1) * (largeur / (nbPointsLargeur_ + 1)), y1, z0);
                if (_withData) data_.addPoint(null);
            }
            poly.sommets_.ajoute(x1, y1, z0);
            if (_withData) data_.addPoint(null);
            for (int i = nbPointsHauteur_; i > 0; i--) {
                poly.sommets_.ajoute(x1, y1 - (nbPointsHauteur_ - i + 1) * hauteur / (nbPointsHauteur_ + 1), z0);
                if (_withData) data_.addPoint(null);
            }
            poly.sommets_.ajoute(x1, y0, z0);
            if (_withData) data_.addPoint(null);
            for (int i = nbPointsLargeur_; i > 0; i--) {
                poly.sommets_.ajoute(x1 - (nbPointsLargeur_ - i + 1) * largeur / (nbPointsLargeur_ + 1), y0, z0);
                if (_withData) data_.addPoint(null);
            }
            if (_withData) addData();
            return poly;
        }

        void addCurrentForme() {
            if (pointDep_ != null && pointFin_ != null) {
                if (support_ != null) support_.addNewPolygone(getPolygone(true), data_);
                pointDep_ = null;
                pointFin_ = null;
                data_ = null;
            }
        }
    }

    /**
   * Valeur a lie au coordonn�es ajout�es.
   */
    ZEditionAttibutesContainer features_;

    /** Forme en cours de cr�ation */
    FormDelegate formeCourante_;

    boolean isPaint_;

    ZCalqueEditionInteractionTargetI support_;

    final TraceLigne tl_ = new TraceLigne(TraceLigne.LISSE, 1.5f, Color.ORANGE);

    final TraceIcon tp_ = new TraceIcon(TraceIcon.PLUS, 3, Color.ORANGE);

    GrPoint tmp_ = new GrPoint();

    /** Le double clic termine-t-il la forme en cours ? */
    boolean isFormEndedByDoubleClic = true;

    /** Les keys modifiers fix�s par programme */
    HashSet<Integer> keyModifiers_ = new HashSet<Integer>();

    /**
   * Constructeur, sans listener associ�.
   */
    public ZCalqueEditionInteraction() {
        super();
    }

    /**
   * Constructeur, avec listener des op�rations de saisie.
   * @param _listener le listener recevant les evts
   */
    public ZCalqueEditionInteraction(final ZCalqueEditionInteractionTargetI _listener) {
        super();
        support_ = _listener;
    }

    /**
   * D�finition du listener recevant les op�rations de saisie.
   * @param _listener Le listener.
   */
    public void setListener(ZCalqueEditionInteractionTargetI _listener) {
        support_ = _listener;
    }

    public ZCalqueEditionInteractionTargetI getListener() {
        return support_;
    }

    public Cursor getSpecificCursor() {
        return new Cursor(Cursor.CROSSHAIR_CURSOR);
    }

    private boolean isOk(final MouseEvent _evt) {
        return !isGele() && formeCourante_ != null && support_ != null && !_evt.isPopupTrigger() && !_evt.isConsumed();
    }

    GrPoint getPointReel(final MouseEvent _e) {
        final GrPoint pt = new GrPoint(_e.getX(), _e.getY(), 0);
        pt.autoApplique(getVersReel());
        return pt;
    }

    public void setGele(final boolean _gele) {
        if (_gele && formeCourante_ != null) {
            formeCourante_.effaceDessin();
        }
        super.setGele(_gele);
    }

    /**
   * Definit si la saisie peut �tre termin�e par un double clic ou CTRL+clic.
   * @param _b True : Le double clic termine la sisie. False : sinon.
   */
    public void setFormEndedByDoubleClic(boolean _b) {
        isFormEndedByDoubleClic = _b;
    }

    /**
   * force les modifiers pour la saisie souris (CTRL, SHIFT, etc.).
   * @param _keyModifier Le modifier � ajouter.
   */
    public void forceKeyModifier(int _keyModifier) {
        keyModifiers_.add(_keyModifier);
    }

    /**
   * release les modifiers pour la saisie souris (CTRL, SHIFT, etc.).
   * @param _keyModifier Le modifier � supprimer.
   */
    public void releaseKeyModifier(int _keyModifier) {
        keyModifiers_.remove(_keyModifier);
    }

    public boolean alwaysPaint() {
        return true;
    }

    /**
   * Annule l'�dition en cours.
   */
    public void cancelEdition() {
        if (formeCourante_ != null && formeCourante_.enCours()) {
            formeCourante_.cancelCurrentForme();
            repaint();
        }
    }

    /**
   * Termine l'edition en cours, et ajoute la forme.
   */
    public void endEdition() {
        if (formeCourante_ != null && formeCourante_.enCours()) {
            formeCourante_.addCurrentForme();
            repaint();
        }
    }

    public String getDescription() {
        if (formeCourante_ == null) {
            return CtuluLibString.EMPTY_STRING;
        }
        return BuResource.BU.getString("Ajouter:") + CtuluLibString.ESPACE + formeCourante_.getFormeDesc();
    }

    public final ZEditionAttibutesContainer getFeatures() {
        return features_;
    }

    /**
   * @return la forme en cours
   */
    public FormDelegate getFormeEnCours() {
        return formeCourante_;
    }

    /**
   * Accesseur de la propriete <I>typeForme </I>. Elle fixe la prochaine forme cree par le calque d'interaction
   * (Rectangle, Cercle, ...) en prenant ses valeurs dans les champs statiques de <I>DeForme </I>.
   * 
   * @return la forme en cours ou -1 si aucune
   * @see org.fudaa.ebli.calque.dessin.DeForme
   */
    public int getTypeForme() {
        return formeCourante_ == null ? -1 : formeCourante_.getForme();
    }

    public boolean isLigneEncours() {
        return isEnCours() && (getTypeForme() == DeForme.LIGNE_BRISEE || getTypeForme() == DeForme.POLYGONE);
    }

    /**
   * @return le type de trait en cours.
   */
    public int getTypeTrait() {
        return tl_.getTypeTrait();
    }

    public void setLineModel(TraceLigneModel _md) {
        tl_.setModel(_md);
    }

    public void setIconModel(TraceIconModel _md) {
        tp_.setModel(_md);
    }

    /**
   * @return true si en cours de saisie.
   */
    public boolean isEnCours() {
        return formeCourante_ != null && formeCourante_.enCours();
    }

    /**
   * @return Returns the isPaint.
   */
    public boolean isPaint() {
        return true;
    }

    /**
   * Methode invoquee lors d'un click de souris (simple ou double). Si la forme courante est: <BR>
   * <UL>
   * <LI>un polygone ou une ligne brisee: <BR>
   * <UL>
   * <LI>simple click: on est en cours de creation, on ajoute un point</LI>
   * <LI>double click: fin de creation, ajout du point et validation de la forme dans le calque dessin.</LI>
   * </UL>
   * <LI>un texte: saisie du texte par boite de dialogue et validation dans le calque dessin.</LI>
   * </UL>
   */
    public void mouseClicked(final MouseEvent _evt) {
        if (isOk(_evt)) {
            formeCourante_.mouseClicked(_evt);
        }
    }

    /**
   * Methode invoquee quand on deplace la souris avec un bouton appuye. Si la forme courante est un trait, un rectangle,
   * une ellipse, un carre, un cercle, une main levee ou une courbe fermee, on est en mode creation et on dessine une
   * forme temporaire en pointille qui bouge avec la souris.
   */
    public void mouseDragged(final MouseEvent _evt) {
        if (isOk(_evt)) {
            formeCourante_.mouseDragged(_evt);
        }
    }

    /**
   * Methode inactive.
   */
    public void mouseEntered(final MouseEvent _evt) {
        if (!isGele() && formeCourante_ != null && support_ != null) {
            formeCourante_.mouseEntered(_evt);
        }
    }

    /**
   * Methode inactive.
   */
    public void mouseExited(final MouseEvent _evt) {
        if (!isGele() && formeCourante_ != null && support_ != null) {
            formeCourante_.mouseExited(_evt);
        }
    }

    /**
   * Methode invoquee quand on deplace la souris sans appuyer sur aucun bouton. Si la forme courante est un polygone, ou
   * une ligne brisee, si on est en mode creation, on dessine un segment temporaire en pointille qui bouge avec la
   * souris.
   */
    public void mouseMoved(final MouseEvent _evt) {
        if (isOk(_evt)) {
            formeCourante_.mouseMoved(_evt);
        }
    }

    /**
   * Methode invoquee quand on appuie sur un bouton de la souris. Si la forme courante est une courbe fermee, une main
   * levee, un polygone, ou une ligne brisee, on entre en mode creation pour la nouvelle forme.
   */
    public void mousePressed(final MouseEvent _evt) {
        if (_evt.getButton() == MouseEvent.BUTTON1 && isOk(_evt)) {
            formeCourante_.mousePressed(_evt);
        }
    }

    /**
   * Methode invoquee quand on lache un bouton de la souris. Si la forme courante est un trait, un rectangle, une
   * ellipse, un carre, un cercle, une main levee ou une courbe fermee, on sort du mode creation et on valide la forme
   * dans le calque dessin.
   */
    public void mouseReleased(final MouseEvent _evt) {
        if (isOk(_evt)) {
            if ((_evt.isControlDown() || _evt.getClickCount() == 2) && !isFormEndedByDoubleClic) {
                return;
            }
            if (_evt.isPopupTrigger()) {
                _evt.consume();
                if (formeCourante_ != null) {
                    formeCourante_.cancelCurrentForme();
                }
            } else if (_evt.getButton() == MouseEvent.BUTTON1 && isOk(_evt)) {
                formeCourante_.mouseReleased(_evt);
            }
        }
    }

    public void paintComponent(final Graphics _g) {
        super.paintComponent(_g);
        if (formeCourante_ != null) {
            formeCourante_.paintComponent(_g);
        }
    }

    public final void setFeatures(final ZEditionAttibutesContainer _features) {
        features_ = _features;
    }

    /**
   * Affectation de la propriete <I>typeForme </I>.
   * 
   * @param _typeForme le type de forme a saisir
   */
    public void setTypeForme(final int _typeForme) {
        if (formeCourante_ != null && formeCourante_.getForme() == _typeForme) {
            return;
        }
        if (_typeForme == DeForme.POINT) {
            formeCourante_ = new FormDelegatePoint();
        } else if (_typeForme == DeForme.MULTI_POINT) {
            formeCourante_ = new FormDelegateMultiPoint();
        } else if (_typeForme == DeForme.LIGNE_BRISEE) {
            formeCourante_ = new FormDelegateLigneBrisee(false);
        } else if (_typeForme == DeForme.POLYGONE) {
            formeCourante_ = new FormDelegateLigneBrisee(true);
        } else if (_typeForme == DeForme.RECTANGLE) {
            formeCourante_ = new FormDelegateRectangle();
        } else if (_typeForme == DeForme.ELLIPSE) {
            formeCourante_ = new FormDelegateEllipse();
        } else {
            formeCourante_ = null;
        }
        repaint();
    }

    /**
   * @param _t le type de trait
   */
    public void setTypeTrait(final int _t) {
        tl_.setTypeTrait(_t);
        repaint();
    }

    public void keyPressed(KeyEvent _e) {
        if (getFormeEnCours() != null) getFormeEnCours().keyPressed(_e);
    }

    public void keyReleased(KeyEvent _e) {
        if (getFormeEnCours() != null) getFormeEnCours().keyReleased(_e);
    }

    public void keyTyped(KeyEvent _e) {
        if (getFormeEnCours() != null) getFormeEnCours().keyTyped(_e);
    }

    public void catchChanged(ZCatchEvent _evt) {
        if (isGele()) return;
        GrPoint pt = null;
        if (_evt.type == ZCatchEvent.CAUGHT) {
            pt = _evt.selection.getScene().getVertex(_evt.idxGeom, _evt.idxVertex);
        }
        getFormeEnCours().setAccroche(pt);
    }

    public boolean isCachingEnabled() {
        return true;
    }
}
