package org.fudaa.fudaa.tr.telemac;

import gnu.trove.TIntArrayList;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import com.memoire.bu.BuComboBox;
import com.vividsolutions.jts.algorithm.SIRtreePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LinearRing;
import org.fudaa.ctulu.CtuluCommandCompositeInverse;
import org.fudaa.ctulu.CtuluLibString;
import org.fudaa.ctulu.CtuluListSelection;
import org.fudaa.ctulu.CtuluListSelectionInterface;
import org.fudaa.ctulu.gis.CtuluLibGeometrie;
import org.fudaa.ctulu.gui.CtuluDialog;
import org.fudaa.ctulu.gui.CtuluDialogPanel;
import org.fudaa.dodico.ef.EfFrontierInterface;
import org.fudaa.dodico.ef.EfGridInterface;
import org.fudaa.dodico.ef.impl.EfFrontier;
import org.fudaa.dodico.h2d.telemac.H2dTelemacSeuil;
import org.fudaa.dodico.h2d.telemac.H2dTelemacSeuilListener;
import org.fudaa.dodico.h2d.telemac.H2dTelemacSeuilMng;
import org.fudaa.ebli.calque.ZSelectionTrace;
import org.fudaa.ebli.commun.EbliActionChangeState;
import org.fudaa.ebli.commun.EbliActionInterface;
import org.fudaa.ebli.commun.EbliActionSimple;
import org.fudaa.ebli.commun.EbliLib;
import org.fudaa.ebli.commun.EbliListeSelectionMulti;
import org.fudaa.ebli.commun.EbliListeSelectionMultiInterface;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrMorphisme;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
import org.fudaa.ebli.geometrie.GrSegment;
import org.fudaa.ebli.trace.TraceBox;
import org.fudaa.ebli.trace.TraceIcon;
import org.fudaa.ebli.trace.TraceLigne;
import org.fudaa.fudaa.meshviewer.layer.MvFrontierPointLayer;
import org.fudaa.fudaa.tr.common.TrLib;
import org.fudaa.fudaa.tr.common.TrResource;
import org.fudaa.fudaa.tr.data.TrVisuPanel;

/**
 * @author Fred Deniger
 * @version $Id: TrTelemacWeirLayer.java,v 1.21 2007-05-04 14:01:50 deniger Exp $
 */
public class TrTelemacWeirLayer extends MvFrontierPointLayer implements H2dTelemacSeuilListener {

    static String getBr() {
        return "<br>";
    }

    private class AddSeuil extends EbliActionSimple {

        TIntArrayList l1_ = new TIntArrayList();

        TIntArrayList l2_ = new TIntArrayList();

        protected AddSeuil() {
            super(TrResource.getS("Ajouter un seuil"), null, "DELETE_WEIRS");
        }

        @Override
        public String getEnableCondition() {
            return TrResource.getS("Activer le mode \"Noeuds fronti�res\"") + getBr() + TrResource.getS("S�lectionner des noeuds fronti�res");
        }

        @Override
        public void actionPerformed(final ActionEvent _e) {
            if (l1_.size() <= 0) {
                TrTelemacWeirLayer.this.pn_.getImpl().error(TrResource.getS("Ajouter un seuil"), getUserMessage(), false);
            } else {
                addSeuil(l1_.toNativeArray(), l2_.toNativeArray());
            }
        }

        @Override
        public void updateStateBeforeShow() {
            final boolean workOnPt = getM().isWorkOnFrontierPt();
            l1_.clear();
            l2_.clear();
            boolean enable = false;
            String message = null;
            if (workOnPt) {
                message = guessSeuil(l1_, l2_);
                if (l1_.size() <= 0 && message == null) {
                    message = TrResource.getS("S�lectionner des noeuds fronti�res");
                } else if (message != null) {
                    l1_.clear();
                }
                enable = (message == null);
            } else {
                message = TrResource.getS("Activer le mode \"Noeuds fronti�res\"");
            }
            super.setUserMessage(message);
            setEnabled(enable);
        }
    }

    private class ChangeTypeDonnees extends EbliActionChangeState {

        protected ChangeTypeDonnees() {
            super(TrResource.getS("Travailler sur les noeuds fronti�res"), null, "WORK_ON_FR_NODE");
        }

        @Override
        public void changeAction() {
            setWorkOnPtFr(super.isSelected());
        }
    }

    private class CoteDebitSeuil extends EbliActionSimple {

        protected CoteDebitSeuil() {
            super(TrResource.getS("D�bit/Cote le long du seuil"), null, "EDIT_FLOW_WEIRS");
        }

        @Override
        public void actionPerformed(final ActionEvent _e) {
            final TrTelemacWeirCourbeFille fille = TrTelemacWeirCourbeFille.createCourbeFille(getM().getSeuils(), getSelection().getSelection(0).getMaxIndex(), pn_.getImpl());
            fille.pack();
            TrLib.initFrameDimensionWithPref(fille, "weirTelemac", pn_.getImpl().getMainPanel().getDesktop().getSize());
            pn_.getImpl().addInternalFrame(fille);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    fille.getGraphe().restore();
                }
            });
        }

        @Override
        public String getEnableCondition() {
            return TrResource.getS("D�sactiver le mode \"Points fronti�res\"") + getBr() + TrResource.getS("S�lectionner uniquement un seuil");
        }

        @Override
        public void updateStateBeforeShow() {
            super.setEnabled(!getM().isWorkOnFrontierPt() && !isSelectionEmpty() && getSelection().getSelection(0).isOnlyOnIndexSelected());
        }
    }

    private class EditSeuil extends EbliActionSimple {

        protected EditSeuil() {
            super(TrResource.getS("Editer seuil(s)"), null, "EDIT_WEIRS");
        }

        @Override
        public String getEnableCondition() {
            return TrResource.getS("D�sactiver le mode \"Points fronti�res\"") + getBr() + TrResource.getS("S�lectionner au moins un seuil");
        }

        @Override
        public void actionPerformed(final ActionEvent _e) {
            new TrTelemacWeirValuesEditor(TrTelemacWeirLayer.this).afficheModale(pn_.getFrame(), (String) getValue(Action.NAME));
        }

        @Override
        public void updateStateBeforeShow() {
            super.setEnabled(!getM().isWorkOnFrontierPt() && !isSelectionEmpty());
        }
    }

    private class RemoveAction extends EbliActionSimple {

        protected RemoveAction() {
            super(TrResource.getS("Enlever seuil(s)"), null, "DELETE_WEIRS");
            setDefaultToolTip(TrResource.getS("Enlever les seuils s�lectionn�s"));
        }

        @Override
        public void actionPerformed(final ActionEvent _e) {
            final int[] selectIdx = getSelection().getSelection(0).getSelectedIndex();
            TrTelemacWeirLayer.this.clearSelection();
            getM().getSeuils().removeSeuil(selectIdx, pn_.getCmdMng());
        }

        @Override
        public String getEnableCondition() {
            return TrResource.getS("D�sactiver le mode \"Points fronti�res\"") + getBr() + TrResource.getS("S�lectionner au moins un seuil");
        }

        @Override
        public void updateStateBeforeShow() {
            super.setEnabled(!getM().isWorkOnFrontierPt() && !TrTelemacWeirLayer.this.isSelectionEmpty());
        }
    }

    private class TangentVitesseAction extends EbliActionSimple {

        protected TangentVitesseAction() {
            super(TrResource.getS("Traitement vitesses tangentielles"), null, "EDIT_VELOCITY_WEIRS");
        }

        @Override
        public void actionPerformed(final ActionEvent _e) {
            final BuComboBox cb = new BuComboBox(getM().getSeuils().getOptionsForVelocity());
            cb.setSelectedIndex(getM().getSeuils().getSeuilVitesseTangentielle());
            final CtuluDialogPanel pn = new CtuluDialogPanel();
            pn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            pn.add(cb);
            final int resp = pn.afficheModale(pn_.getFrame(), (String) getValue(Action.NAME));
            if (CtuluDialogPanel.isOkResponse(resp)) {
                getM().getSeuils().modifyVitesseTangentielles(cb.getSelectedIndex(), pn_.getCmdMng());
            }
        }

        @Override
        public String getEnableCondition() {
            return TrResource.getS("D�finir au moins un seuil");
        }

        @Override
        public void updateStateBeforeShow() {
            super.setEnabled(getM().getSeuils().getNbSeuil() > 0);
        }
    }

    private class LayerDisplayAction extends EbliActionSimple {

        protected LayerDisplayAction() {
            super(TrResource.getS("Propri�t�s d'affichage"), null, "WEIR_LAYER_DISPLAY");
        }

        @Override
        public void actionPerformed(final ActionEvent _e) {
            new TrTelemacWeirLayerDisplay(TrTelemacWeirLayer.this).afficheModale(pn_.getFrame(), (String) getValue(Action.NAME), CtuluDialog.OK_APPLY_OPTION);
        }
    }

    boolean afficheDebFin_;

    TraceBox boxDebFin_;

    TrTelemacWeirInput.CoteSaisie c1Temp_;

    TrTelemacWeirInput.CoteSaisie c2Temp_;

    TrVisuPanel pn_;

    int pourcentageErreur_ = -1;

    TraceLigne tl_;

    TraceLigne tlInterne_;

    boolean traceSeuilWithBadIdx_;

    protected final boolean isAfficheDebFin() {
        return afficheDebFin_;
    }

    protected final void setAfficheDebFin(final boolean _afficheDebFin) {
        afficheDebFin_ = _afficheDebFin;
    }

    protected final int getPourcentageErreur() {
        return pourcentageErreur_;
    }

    protected final void setPourcentageErreur(final int _pourcentageErreur) {
        pourcentageErreur_ = _pourcentageErreur;
    }

    protected final boolean isTraceSeuilWithBadIdx() {
        return traceSeuilWithBadIdx_;
    }

    protected final void setTraceSeuilWithBadIdx(final boolean _traceSeuilWithBadIdx) {
        traceSeuilWithBadIdx_ = _traceSeuilWithBadIdx;
    }

    TrTelemacWeirLayer(final TrTelemacWeirModel _m) {
        super(_m);
        setForeground(Color.GREEN);
        _m.getSeuils().addListener(this);
    }

    protected void addSeuil(final int[] _c1, final int[] _c2) {
        final CtuluCommandCompositeInverse cmd = new CtuluCommandCompositeInverse();
        getM().getSeuils().addSeuil(_c1, _c2, cmd);
        final int[] idx = new int[] { getM().getSeuils().getNbSeuil() - 1 };
        final TrTelemacWeirValuesEditor ed = new TrTelemacWeirValuesEditor(TrTelemacWeirLayer.this, idx, cmd);
        if (CtuluDialogPanel.isOkResponse(ed.afficheModale(pn_.getFrame()))) {
            pn_.getCmdMng().addCmd(cmd);
        } else {
            cmd.undo();
        }
    }

    protected EbliActionInterface[] getActions(final TrVisuPanel _pn) {
        pn_ = _pn;
        return new EbliActionInterface[] { new EditSeuil(), new CoteDebitSeuil(), new RemoveAction(), new TangentVitesseAction(), new LayerDisplayAction(), null, new ChangeTypeDonnees(), new AddSeuil() };
    }

    protected TrTelemacWeirModel getM() {
        return (TrTelemacWeirModel) super.modeleDonnees();
    }

    protected EbliListeSelectionMultiInterface getSelection() {
        return selection_;
    }

    protected TraceBox getTraceBox() {
        if (boxDebFin_ == null) {
            boxDebFin_ = new TraceBox();
            boxDebFin_.setVMargin(2);
            boxDebFin_.setHMargin(2);
            boxDebFin_.setVPosition(SwingConstants.CENTER);
            boxDebFin_.setHPosition(SwingConstants.CENTER);
        }
        return boxDebFin_;
    }

    /**
   * Recherche des deux blocs du seuil.
   * 
   * @return null si ok. erreur sinon.
   */
    protected String guessSeuil(final TIntArrayList _l1, final TIntArrayList _l2) {
        if (selection_ == null) {
            return TrResource.getS("S�lection vide");
        }
        final int i = selection_.isSelectionInOneBloc();
        if (i >= 0) {
            final CtuluListSelection s = selection_.get(i);
            final int nbSelected = s.getNbSelectedIndex();
            if (nbSelected % 2 == 1) {
                return TrResource.getS("Le nombre de noeuds s�lectionn�s est impair.");
            }
            final int[] idx = CtuluListSelection.isSelectionContiguous(s, m_.getNbElementIn(i));
            if (idx != null) {
                final EfFrontierInterface fr = getM().getSeuils().getGrid().getFrontiers();
                final EfFrontier.FrontierIterator it1 = fr.getFrontierIterator(i, idx[0]);
                boolean reverse = false;
                if (!s.isSelected(it1.next())) {
                    reverse = true;
                }
                it1.setReverse(true);
                it1.next();
                it1.setReverse(reverse);
                final EfFrontier.FrontierIterator it2 = fr.getFrontierIterator(i, idx[1]);
                it2.setReverse(!it1.isReverse());
                for (int coteIdx = nbSelected / 2; coteIdx > 0; coteIdx--) {
                    _l1.add(it1.getGlobalFrIdx());
                    _l2.add(it2.getGlobalFrIdx());
                    it1.next();
                    it2.next();
                }
                return null;
            }
            final EfFrontierInterface fr = getM().getSeuils().getGrid().getFrontiers();
            EfFrontier.FrontierIterator it1 = fr.getFrontierIterator(i, s.getMinIndex());
            final CtuluListSelection s2 = new CtuluListSelection();
            s2.add(s.getMinIndex());
            if (s.getMinIndex() == 0) {
                it1.setReverse(true);
                int k = it1.next();
                while (s.isSelected(k)) {
                    s2.add(k);
                    k = it1.next();
                }
            }
            it1 = fr.getFrontierIterator(i, s.getMinIndex());
            int k = it1.next();
            while (s.isSelected(k)) {
                s2.add(k);
                k = it1.next();
            }
            if (s2.getNbSelectedIndex() != (nbSelected / 2)) {
                return TrResource.getS("Le nombre de noeuds s�lectionn�s n'est pas le m�me dans les deux s�lections");
            }
            final CtuluListSelection s1 = new CtuluListSelection(s2);
            s2.xor(s);
            final int[] s2Extr = CtuluListSelection.isSelectionContiguous(s2, fr.getNbPt(i));
            if (s2Extr != null) {
                final int[] s1Ext = CtuluListSelection.isSelectionContiguous(s1, fr.getNbPt(i));
                it1 = fr.getFrontierIterator(i, s1Ext[0]);
                boolean reverse = false;
                if (!s1.isSelected(it1.next())) {
                    reverse = true;
                }
                it1.setReverse(true);
                it1.next();
                it1.setReverse(reverse);
                final EfFrontier.FrontierIterator it2 = fr.getFrontierIterator(i, s2Extr[1]);
                it2.setReverse(!it1.isReverse());
                for (int coteIdx = nbSelected / 2; coteIdx > 0; coteIdx--) {
                    _l1.add(it1.getGlobalFrIdx());
                    _l2.add(it2.getGlobalFrIdx());
                    it1.next();
                    it2.next();
                }
                return null;
            }
        } else if (selection_.getNbListSelected() == 2) {
            final int[] frIdx = selection_.getIdxSelected();
            final CtuluListSelection s1 = selection_.get(frIdx[0]);
            final CtuluListSelection s2 = selection_.get(frIdx[1]);
            if (s1.getNbSelectedIndex() != s2.getNbSelectedIndex()) {
                return TrResource.getS("Le nombre de noeuds s�lectionn�s n'est pas le m�me dans les deux s�lections");
            }
            final EfGridInterface grid = getM().getSeuils().getGrid();
            final EfFrontierInterface fr = grid.getFrontiers();
            final int[] s1Extr = CtuluListSelection.isSelectionContiguous(s1, fr.getNbPt(frIdx[0]));
            if (s1Extr == null) {
                return TrResource.getS("Les noeuds s�lectionn�s ne sont pas contigus") + " (" + frIdx[0] + ")";
            }
            final int[] s2Extr = CtuluListSelection.isSelectionContiguous(s2, fr.getNbPt(frIdx[1]));
            if (s2Extr == null) {
                return TrResource.getS("Les noeuds s�lectionn�s ne sont pas contigus") + " (" + frIdx[1] + ")";
            }
            final boolean s1full = s1.getNbSelectedIndex() == fr.getNbPt(frIdx[0]);
            final boolean s2full = s2.getNbSelectedIndex() == fr.getNbPt(frIdx[1]);
            if (s2full) {
                final Coordinate pt1 = grid.getCoor(fr.getIdxGlobal(frIdx[0], s1Extr[0]));
                final Coordinate pt2 = grid.getCoor(fr.getIdxGlobal(frIdx[1], 0));
                int idx = 0;
                double dmin = CtuluLibGeometrie.getD2(pt1.x, pt1.y, pt2.x, pt2.y);
                for (int k = fr.getNbPt(frIdx[1]) - 1; k > 0; k--) {
                    grid.getPt(fr.getIdxGlobal(frIdx[1], k), pt2);
                    final double d = CtuluLibGeometrie.getD2(pt1.x, pt1.y, pt2.x, pt2.y);
                    if (d < dmin) {
                        dmin = d;
                        idx = k;
                    }
                }
                s2Extr[1] = idx;
            }
            final EfFrontier.FrontierIterator it1 = fr.getFrontierIterator(frIdx[0], s1Extr[0]);
            boolean reverse = false;
            if (!s1.isSelected(it1.next())) {
                reverse = true;
            }
            it1.setReverse(true);
            it1.next();
            it1.setReverse(reverse);
            final EfFrontier.FrontierIterator it2 = fr.getFrontierIterator(frIdx[1], s2Extr[1]);
            it2.setReverse(!it1.isReverse());
            for (int coteIdx = s1.getNbSelectedIndex(); coteIdx > 0; coteIdx--) {
                _l1.add(it1.getGlobalFrIdx());
                _l2.add(it2.getGlobalFrIdx());
                it1.next();
                it2.next();
            }
            if (s1full && s2full) {
                _l1.add(fr.getFrontiereIndice(frIdx[0], s1Extr[0]));
                _l2.add(fr.getFrontiereIndice(frIdx[1], s2Extr[1]));
            }
            return null;
        }
        return TrResource.getS("Les noeuds s�lectionn�s ne sont pas contigus");
    }

    protected void paintTemporaire(final Graphics _g) {
        if (c1Temp_ != null) {
            c1Temp_.paintTempo(_g);
        }
        if (c2Temp_ != null) {
            c2Temp_.paintTempo(_g);
        }
    }

    protected EbliListeSelectionMulti selectWeir(final GrPoint _pt, final int _tolerance) {
        if (getM().getNombre() == 0) {
            return null;
        }
        final GrBoite bClip = getDomaine();
        final double distanceReel = GrMorphisme.convertDistanceXY(getVersReel(), _tolerance);
        if ((!bClip.contientXY(_pt)) && (bClip.distanceXY(_pt) > distanceReel)) {
            return null;
        }
        final GrBoite b = new GrBoite(new GrPoint(), new GrPoint());
        final GrPoint p1 = new GrPoint();
        final GrPoint p2 = new GrPoint();
        final GrPoint p3 = new GrPoint();
        final GrPoint p4 = new GrPoint();
        final GrSegment s = new GrSegment(p3, p4);
        final GrSegment c1 = new GrSegment(p1, p3);
        final GrSegment c2 = new GrSegment(p2, p4);
        final GrPolygone pol = new GrPolygone();
        pol.sommets_.ajoute(p1);
        pol.sommets_.ajoute(p2);
        pol.sommets_.ajoute(p4);
        pol.sommets_.ajoute(p3);
        int selected = -1;
        double dist = -1;
        final GrBoite clipReel = getClipReel(getGraphics());
        final TrTelemacWeirModel model = getM();
        for (int i = model.getNbBox() - 1; i >= 0; i--) {
            model.getBoiteForBox(i, b);
            if (clipReel.intersectXY(b) && (b.contientXY(_pt) || (b.distanceXY(_pt) <= distanceReel))) {
                boolean first = true;
                for (int j = model.getNbPointInBox(i) - 1; j >= 0; j--) {
                    model.getPoint1For(i, j, s.e_);
                    model.getPoint2For(i, j, s.o_);
                    if (first || j == 0) {
                        final double d = s.distanceXY(_pt);
                        if ((d <= distanceReel) && (selected < 0 || d < dist)) {
                            dist = d;
                            selected = i;
                        }
                    }
                    if (!first) {
                        model.getPoint1For(i, j + 1, p1);
                        model.getPoint1For(i, j, p3);
                        model.getPoint2For(i, j + 1, p2);
                        model.getPoint2For(i, j, p4);
                        if (pol.contientXY(_pt)) {
                            final EbliListeSelectionMulti r = new EbliListeSelectionMulti(1);
                            r.set(0, i);
                            return r;
                        }
                        double d = c1.distance(_pt);
                        if ((d <= distanceReel) && (selected < 0 || d < dist)) {
                            dist = d;
                            selected = i;
                        }
                        d = c2.distance(_pt);
                        if ((d <= distanceReel) && (selected < 0 || d < dist)) {
                            dist = d;
                            selected = i;
                        }
                    }
                    first = false;
                }
            }
        }
        if (selected >= 0) {
            final CtuluListSelection r = new CtuluListSelection();
            r.setSelectionInterval(selected, selected);
            final EbliListeSelectionMulti rf = new EbliListeSelectionMulti(1);
            rf.set(0, selected);
            return rf;
        }
        return null;
    }

    protected EbliListeSelectionMulti selecWeir(final LinearRing _poly) {
        if (getM().getNombre() == 0) {
            return null;
        }
        final Envelope polyEnv = _poly.getEnvelopeInternal();
        final GrBoite domaineBoite = getDomaine();
        final Envelope domaine = new Envelope(domaineBoite.e_.x_, domaineBoite.o_.x_, domaineBoite.e_.y_, domaineBoite.o_.y_);
        if (!polyEnv.intersects(domaine)) {
            return null;
        }
        final GrBoite b = new GrBoite(new GrPoint(), new GrPoint());
        final GrPoint p = new GrPoint();
        final Coordinate c = new Coordinate();
        final CtuluListSelection r = new CtuluListSelection();
        final TrTelemacWeirModel model = getM();
        final SIRtreePointInRing tester = new SIRtreePointInRing(_poly);
        for (int i = model.getNbBox() - 1; i >= 0; i--) {
            model.getBoiteForBox(i, b);
            if (polyEnv.contains(b.e_.x_, b.e_.y_) && polyEnv.contains(b.o_.x_, b.o_.y_)) {
                boolean allContained = true;
                for (int j = model.getNbPointInBox(i) - 1; (j >= 0) && allContained; j--) {
                    model.getPoint1For(i, j, p);
                    c.x = p.x_;
                    c.y = p.y_;
                    if (tester.isInside(c)) {
                        model.getPoint2For(i, j, p);
                        c.x = p.x_;
                        c.y = p.y_;
                        if (!tester.isInside(c)) {
                            allContained = false;
                        }
                    } else {
                        allContained = false;
                    }
                }
                if (allContained) {
                    r.add(i);
                }
            }
        }
        if (!r.isEmpty()) {
            final EbliListeSelectionMulti rf = new EbliListeSelectionMulti(1);
            rf.set(0, r);
            return rf;
        }
        return null;
    }

    @Override
    public void selectAll() {
        if (getM().isWorkOnFrontierPt()) {
            super.selectAll();
        }
        CtuluListSelection s = selection_.get(0);
        if (s == null) {
            s = new CtuluListSelection();
            selection_.set(0, s);
        }
        s.setSelectionInterval(0, getM().getNbBox() - 1);
        fireSelectionEvent();
    }

    @Override
    public void inverseSelection() {
        if (isSelectionEmpty()) {
            return;
        }
        if (getM().isWorkOnFrontierPt()) {
            super.inverseSelection();
        }
        selection_.get(0).inverse(getM().getNbBox());
        fireSelectionEvent();
    }

    protected final void setC1Temp(final TrTelemacWeirInput.CoteSaisie _temp) {
        c1Temp_ = _temp;
        repaint();
    }

    protected final void setC2Temp(final TrTelemacWeirInput.CoteSaisie _temp) {
        c2Temp_ = _temp;
        repaint();
    }

    protected void setWorkOnPtFr(final boolean _r) {
        if (_r != getM().isWorkOnFrontierPt()) {
            clearSelection();
            getM().setWorkOnFrontierPt(_r);
            firePropertyChange("iconeChanged", true, false);
            fireSelectionEvent();
            repaint();
        }
    }

    protected final void unsetCoteTemp() {
        c1Temp_ = null;
        c2Temp_ = null;
        repaint();
    }

    @Override
    public int[] getSelectedElementIdx() {
        return null;
    }

    @Override
    public int[] getSelectedObjectInTable() {
        if (getM().isWorkOnFrontierPt()) {
            return super.getSelectedObjectInTable();
        }
        return null;
    }

    @Override
    public int[] getSelectedPtIdx() {
        if (getM().isWorkOnFrontierPt()) {
            return super.getSelectedPtIdx();
        }
        return null;
    }

    @Override
    public GrBoite getDomaineOnSelected() {
        if (getM().isWorkOnFrontierPt()) {
            return super.getDomaineOnSelected();
        }
        return null;
    }

    @Override
    public boolean isSelectionElementEmpty() {
        return true;
    }

    @Override
    public boolean isSelectionPointEmpty() {
        if (getM().isWorkOnFrontierPt()) {
            return super.isSelectionPointEmpty();
        }
        return true;
    }

    @Override
    public void paintDonnees(final Graphics2D _g, final GrMorphisme _versEcran, final GrMorphisme _versReel, final GrBoite _clipReel) {
        if (getM().isWorkOnFrontierPt()) {
            super.paintDonnees(_g, _versEcran, _versReel, _clipReel);
        }
        final TrTelemacWeirModel model = getM();
        if (model.getNombre() == 0) {
            return;
        }
        Color foreground = getForeground();
        if (isAttenue()) {
            foreground = EbliLib.getAlphaColor(attenueCouleur(foreground), alpha_);
        } else if (EbliLib.isAlphaChanged(alpha_)) {
            foreground = EbliLib.getAlphaColor(foreground, alpha_);
        }
        if (tl_ == null) {
            tl_ = new TraceLigne();
            tl_.setEpaisseur(2);
            tlInterne_ = new TraceLigne();
        }
        tl_.setCouleur(foreground);
        tlInterne_.setCouleur(foreground);
        final GrBoite b = new GrBoite(new GrPoint(), new GrPoint());
        final GrSegment s = new GrSegment(new GrPoint(), new GrPoint());
        final boolean isRapide = isRapide();
        final H2dTelemacSeuilMng mng = model.getSeuils();
        TraceIcon erreurDist = null;
        for (int i = model.getNbBox() - 1; i >= 0; i--) {
            model.getBoiteForBox(i, b);
            if (traceSeuilWithBadIdx_ && !mng.getTelemacSeuil(i).isIdxValide()) {
                tl_.setCouleur(Color.RED);
                tlInterne_.setCouleur(Color.RED);
            } else {
                tl_.setCouleur(null);
                tlInterne_.setCouleur(null);
            }
            boolean first = true;
            double xo = 0, yo = 0, xe = 0, ye = 0;
            if (_clipReel.intersectXY(b)) {
                for (int j = model.getNbPointInBox(i) - 1; j >= 0; j--) {
                    model.getPoint1For(i, j, s.e_);
                    model.getPoint2For(i, j, s.o_);
                    if (_clipReel.intersectXYBoite(s)) {
                        s.autoApplique(_versEcran);
                        if (first || j == 0) {
                            tl_.dessineTrait(_g, s.o_.x_, s.o_.y_, s.e_.x_, s.e_.y_);
                            xo = s.o_.x_;
                            yo = s.o_.y_;
                            xe = s.e_.x_;
                            ye = s.e_.y_;
                        } else {
                            tlInterne_.dessineTrait(_g, s.o_.x_, s.o_.y_, s.e_.x_, s.e_.y_);
                        }
                    }
                    if (!isRapide) {
                        if (!first) {
                            model.getPoint1For(i, j, s.e_);
                            model.getPoint1For(i, j + 1, s.o_);
                            boolean traceErr = false;
                            if (pourcentageErreur_ >= 0) {
                                final H2dTelemacSeuil seuil = model.getSeuils().getTelemacSeuil(i);
                                final double d1 = seuil.getDistanceXY(true, j, mng.getGrid());
                                final double d2 = seuil.getDistanceXY(false, j, mng.getGrid());
                                double err = 100 * (d1 - d2) / d1;
                                if (err < 0) {
                                    err = -err;
                                }
                                if (err > pourcentageErreur_) {
                                    if (erreurDist == null) {
                                        erreurDist = new TraceIcon(TraceIcon.CROIX, 4);
                                        erreurDist.setCouleur(Color.RED);
                                    }
                                    traceErr = true;
                                }
                            }
                            if (_clipReel.intersectXYBoite(s)) {
                                s.autoApplique(_versEcran);
                                tl_.dessineTrait(_g, s.o_.x_, s.o_.y_, s.e_.x_, s.e_.y_);
                                if (traceErr && erreurDist != null) {
                                    final Color old = _g.getColor();
                                    erreurDist.paintIconCentre(_g, (s.o_.x_ + s.e_.x_) / 2, (s.o_.y_ + s.e_.y_) / 2);
                                    _g.setColor(old);
                                }
                            }
                            model.getPoint2For(i, j, s.e_);
                            model.getPoint2For(i, j + 1, s.o_);
                            if (_clipReel.intersectXYBoite(s)) {
                                s.autoApplique(_versEcran);
                                tl_.dessineTrait(_g, s.o_.x_, s.o_.y_, s.e_.x_, s.e_.y_);
                                if (traceErr && erreurDist != null) {
                                    final Color old = _g.getColor();
                                    erreurDist.paintIconCentre(_g, (s.o_.x_ + s.e_.x_) / 2, (s.o_.y_ + s.e_.y_) / 2);
                                    _g.setColor(old);
                                }
                            }
                        }
                        if (afficheDebFin_ && (first || j == 0)) {
                            final double x = (xe + xo) / 2;
                            final double y = (ye + yo) / 2;
                            final Color old = _g.getColor();
                            final TraceBox tb = getTraceBox();
                            if (model.isCycle(i) && first) {
                                tb.paintBox(_g, (int) x, (int) y, "1-2");
                            } else if (first) {
                                tb.paintBox(_g, (int) x, (int) y, CtuluLibString.DEUX);
                            } else {
                                tb.paintBox(_g, (int) x, (int) y, CtuluLibString.UN);
                            }
                            _g.setColor(old);
                        }
                    }
                    first = false;
                }
            }
        }
        paintTemporaire(_g);
    }

    @Override
    public void paintIcon(final Component _c, final Graphics _g, final int _x, final int _y) {
        final double w = getIconWidth();
        final double h = getIconHeight();
        final Graphics2D g2d = (Graphics2D) _g;
        final Color old = _g.getColor();
        _g.setColor(Color.white);
        _g.fillRect(_x + 1, _y + 1, (int) w - 1, (int) h - 1);
        _g.setColor(getForeground());
        _g.drawRect(_x, _y, (int) w, (int) h);
        final boolean point = getM().isWorkOnFrontierPt();
        if (isAttenue()) {
            _g.setColor(attenueCouleur(getForeground()));
        } else {
            _g.setColor(getForeground());
        }
        final boolean empty = (!point) && (modeleDonnees() == null || (modeleDonnees().getNombre() == 0));
        final Polygon p = new Polygon();
        double x1 = _x + w / 5;
        double y1 = _y + 4 * h / 5;
        if (point) {
            g2d.drawLine((int) x1 - 1, (int) y1, (int) x1 + 1, (int) y1);
            g2d.drawLine((int) x1, (int) y1 - 1, (int) x1, (int) y1 + 1);
        } else {
            p.addPoint((int) x1, (int) y1);
        }
        final double largeurBase = 3 * w / 5;
        x1 = x1 + largeurBase / 4;
        if (point) {
            g2d.drawLine((int) x1 - 1, (int) y1, (int) x1 + 1, (int) y1);
            g2d.drawLine((int) x1, (int) y1 - 1, (int) x1, (int) y1 + 1);
        } else {
            p.addPoint((int) x1, (int) y1);
        }
        x1 = x1 + largeurBase / 8;
        y1 = _y + h / 2;
        if (point) {
            g2d.drawLine((int) x1 - 1, (int) y1, (int) x1 + 1, (int) y1);
            g2d.drawLine((int) x1, (int) y1 - 1, (int) x1, (int) y1 + 1);
        } else {
            p.addPoint((int) x1, (int) y1);
        }
        x1 = x1 + largeurBase / 4;
        if (point) {
            g2d.drawLine((int) x1 - 1, (int) y1, (int) x1 + 1, (int) y1);
            g2d.drawLine((int) x1, (int) y1 - 1, (int) x1, (int) y1 + 1);
        } else {
            p.addPoint((int) x1, (int) y1);
        }
        y1 = _y + 4 * h / 5;
        x1 = x1 + largeurBase / 8;
        if (point) {
            g2d.drawLine((int) x1 - 1, (int) y1, (int) x1 + 1, (int) y1);
            g2d.drawLine((int) x1, (int) y1 - 1, (int) x1, (int) y1 + 1);
        } else {
            p.addPoint((int) x1, (int) y1);
        }
        x1 = x1 + largeurBase / 4;
        if (point) {
            g2d.drawLine((int) x1 - 1, (int) y1, (int) x1 + 1, (int) y1);
            g2d.drawLine((int) x1, (int) y1 - 1, (int) x1, (int) y1 + 1);
        } else {
            p.addPoint((int) x1, (int) y1);
        }
        x1 = x1 - largeurBase / 8;
        y1 = _y + h / 5;
        if (point) {
            g2d.drawLine((int) x1 - 1, (int) y1, (int) x1 + 1, (int) y1);
            g2d.drawLine((int) x1, (int) y1 - 1, (int) x1, (int) y1 + 1);
        } else {
            p.addPoint((int) x1, (int) y1);
        }
        x1 = _x + w / 5 + largeurBase / 8;
        if (point) {
            g2d.drawLine((int) x1 - 1, (int) y1, (int) x1 + 1, (int) y1);
            g2d.drawLine((int) x1, (int) y1 - 1, (int) x1, (int) y1 + 1);
        } else {
            p.addPoint((int) x1, (int) y1);
        }
        if (empty) {
            ((Graphics2D) _g).draw(p);
        } else {
            ((Graphics2D) _g).fill(p);
        }
        _g.setColor(old);
    }

    @Override
    public void paintSelection(final Graphics2D _g, final ZSelectionTrace _trace, final GrMorphisme _versEcran, final GrBoite _clipReel) {
        if (getM().isWorkOnFrontierPt()) {
            super.paintSelection(_g, _trace, _versEcran, _clipReel);
            return;
        }
        Color cs = _trace.getColor();
        if (isAttenue()) {
            cs = attenueCouleur(cs);
        }
        _g.setColor(cs);
        final TraceLigne tlSelection = _trace.getLigne();
        final CtuluListSelectionInterface selection = selection_.getSelection(0);
        final TrTelemacWeirModel model = getM();
        final int min = selection.getMinIndex();
        final GrBoite b = new GrBoite(new GrPoint(), new GrPoint());
        final GrSegment s = new GrSegment(new GrPoint(), new GrPoint());
        for (int i = selection.getMaxIndex(); i >= min; i--) {
            if (!selection.isSelected(i)) {
                continue;
            }
            model.getBoiteForBox(i, b);
            if (_clipReel.intersectXY(b)) {
                boolean first = true;
                for (int j = model.getNbPointInBox(i) - 1; j >= 0; j--) {
                    model.getPoint1For(i, j, s.e_);
                    model.getPoint2For(i, j, s.o_);
                    if (_clipReel.intersectXYBoite(s)) {
                        s.autoApplique(_versEcran);
                        if (first || j == 0) {
                            tlSelection.dessineTrait(_g, s.o_.x_, s.o_.y_, s.e_.x_, s.e_.y_);
                        }
                    }
                    if (!first) {
                        model.getPoint1For(i, j, s.e_);
                        model.getPoint1For(i, j + 1, s.o_);
                        if (_clipReel.intersectXYBoite(s)) {
                            s.autoApplique(_versEcran);
                            tlSelection.dessineTrait(_g, s.o_.x_, s.o_.y_, s.e_.x_, s.e_.y_);
                        }
                        model.getPoint2For(i, j, s.e_);
                        model.getPoint2For(i, j + 1, s.o_);
                        if (_clipReel.intersectXYBoite(s)) {
                            s.autoApplique(_versEcran);
                            tlSelection.dessineTrait(_g, s.o_.x_, s.o_.y_, s.e_.x_, s.e_.y_);
                        }
                    }
                    first = false;
                }
            }
        }
    }

    @Override
    public EbliListeSelectionMulti selection(final GrPoint _pt, final int _tolerance) {
        if (getM().isWorkOnFrontierPt()) {
            return super.selection(_pt, _tolerance);
        }
        return selectWeir(_pt, _tolerance);
    }

    @Override
    public EbliListeSelectionMulti selection(final LinearRing _poly) {
        if (getM().isWorkOnFrontierPt()) {
            return super.selection(_poly);
        }
        return selecWeir(_poly);
    }

    public void seuilAdded() {
        repaint();
        if (modeleDonnees().getNombre() == 1) {
            firePropertyChange("iconeChanged", true, false);
        }
    }

    public void seuilChanged(final boolean _xyChanged) {
        if (_xyChanged) {
            repaint();
        }
    }

    public void seuilRemoved() {
        if (selection_ != null) {
            selection_.clear();
        }
        if (modeleDonnees().getNombre() == 0) {
            firePropertyChange("iconeChanged", true, false);
        }
        repaint();
    }
}
