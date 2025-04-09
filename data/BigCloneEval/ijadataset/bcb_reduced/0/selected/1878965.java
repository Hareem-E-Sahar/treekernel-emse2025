package org.fudaa.ebli.calque.edition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.fudaa.ctulu.CtuluAnalyze;
import org.fudaa.ctulu.CtuluCommandComposite;
import org.fudaa.ctulu.CtuluCommandContainer;
import org.fudaa.ctulu.CtuluListSelection;
import org.fudaa.ctulu.editor.CtuluValueEditorDefaults;
import org.fudaa.ctulu.editor.CtuluValueEditorI;
import org.fudaa.ctulu.gis.GISAttributeConstants;
import org.fudaa.ctulu.gis.GISAttributeInterface;
import org.fudaa.ctulu.gis.GISAttributeModel;
import org.fudaa.ctulu.gis.GISCoordinateSequenceFactory;
import org.fudaa.ctulu.gis.GISGeometryFactory;
import org.fudaa.ctulu.gis.GISReprojectInterpolateurI;
import org.fudaa.ctulu.gis.GISZoneAttributeFactory;
import org.fudaa.ctulu.gis.GISZoneCollection;
import org.fudaa.ctulu.gis.GISZoneCollectionGeometry;
import org.fudaa.ctulu.gui.CtuluCellBooleanRenderer;
import org.fudaa.ctulu.gui.CtuluCellDoubleRenderer;
import org.fudaa.ebli.calque.ZModelGeometryListener;
import org.fudaa.ebli.calque.ZModeleGeometry;
import org.fudaa.ebli.commun.EbliCoordinateDefinition;
import org.fudaa.ebli.commun.EbliLib;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Cette classe permet d'adapter l'interface d'un ZModeleGeometry sur une
 * g�om�trie sp�cifique. C'est � dire que l'adapter se greffe sur le mod�le
 * et selectionne une des g�om�tries. Toutes les m�thodes propos�es par cette
 * interface seront r�alis�es sur cette g�om�trie.
 * 
 * Ce model contient :
 * en premi�re colonne : index 
 * en seconde colonne : x
 * en troisi�me colonne : y en
 * quatri�me colonne : z (si il existe et qu'il est atomique)
 * les autres attributs atomiques dans l'ordre de la GISZoneCollection
 * etc...
 * 
 * Deux modes d'application des modifications sont pr�sents :
 * - un mode imm�diate : les modifications sont imm�diatements appliqu�s � la gis.
 * - un mode diff�r� : les modifications sont mises en cache et aplliqu� quand flushData() est appel�.
 * 
 * Le model se charge de mettre la g�o�mtrie � 'Modifi�' lorsque celle-ci l'est.
 * 
 * @author Emmanuel MARTIN
 * @version $Id: EbliSingleObjectTableModel.java 6746 2011-12-01 13:43:22Z bmarchan $
 */
public class EbliSingleObjectTableModel implements TableModel {

    /**
   * Classe interface permettant de faire le lien entre la classe m�re et les
   * classes g�rant les �tats sp�cifiques.
   * Utilisation du design pattern Stat.
   * 
   * @author Emmanuel MARTIN
   * @version $Id: EbliSingleObjectTableModel.java 6746 2011-12-01 13:43:22Z bmarchan $
   */
    protected interface TableModelState {

        public Object getValueAt(int rowIndex, int columnIndex);

        public void setValueAt(Object value, int rowIndex, int columnIndex);

        public int getRowCount();

        /**
     * Inverse la position de deux des points de la g�om�trie.
     */
        public void switchPoints(int _idx1, int _idx2, CtuluCommandContainer _cmd);

        /**
     * Supprime les sommets d'indice donn�es.
     * @param _idx Les indices, dans l'ordre croissant.
     * @param _cmd Le container de commandes.
     */
        public void removePoints(int[] _idx, CtuluCommandContainer _cmd);

        /**
     * Ajoute un sommet � la g�om�trie
     * @param _idxBefore L'indice apr�s lequel le sommet sera ins�r�. Si -1, le sommet est ins�r� en 0.
     * @param _x La coordonn�e X du sommet.
     * @param _y La coordonn�e Y du sommet
     * @param _cmd Le container de commandes.
     * @return L'index du nouveau point inser�
     */
        public int addPoint(int _idxBefore, double _x, double _y, CtuluCommandContainer _cmd);

        /**
     * Met � jour les caches utilis�s dans l'instance. Cette m�thode est
     * g�n�ralement utilis� lors d'une modification de mod�le.
     */
        public void updateFromModele();

        /**
     * Applique si n�cessaire les modifications diff�r�es.
     */
        public void flushData();
    }

    /**
   * R�alise les op�rations imm�diatement sur le Modele.
   * 
   * @author Emmanuel MARTIN
   * @version $Id: EbliSingleObjectTableModel.java 6746 2011-12-01 13:43:22Z bmarchan $
   */
    protected class ModificationOnTheFly implements TableModelState {

        public ModificationOnTheFly() {
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case 0:
                    return rowIndex;
                case 1:
                    return coordSeq_.getOrdinate(rowIndex, 0);
                case 2:
                    return coordSeq_.getOrdinate(rowIndex, 1);
                default:
                    return getModel(columnIndex - 3).getObjectValueAt(rowIndex);
            }
        }

        public GISAttributeModel getModel(int _idx) {
            return (GISAttributeModel) zone_.getModel(lattrs_.get(_idx)).getObjectValueAt(idxSelected_);
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            CtuluCommandComposite cmd = new CtuluCommandComposite(EbliLib.getS("Modification d'une g�om�trie"));
            if (columnIndex == 1 || columnIndex == 2) {
                putGeomModified(cmd);
                clearError();
                coordSeq_.setOrdinate(rowIndex, columnIndex - 1, (Double) value);
                if (isClosed_ && rowIndex == 0) {
                    coordSeq_.setOrdinate(coordSeq_.size() - 1, columnIndex - 1, (Double) value);
                }
                zone_.setCoordinateSequence(idxSelected_, coordSeq_, cmd);
            } else {
                getModel(columnIndex - 3).setObject(rowIndex, value, cmd);
                fireTableModelListeners();
            }
            if (cmd_ != null) {
                cmd_.addCmd(cmd.getSimplify());
            }
        }

        public int getRowCount() {
            if (isClosed_) return coordSeq_.size() - 1; else return coordSeq_.size();
        }

        public void switchPoints(int _idx1, int _idx2, CtuluCommandContainer _cmd) {
            putGeomModified(_cmd);
            CoordinateSequence newseq = new GISCoordinateSequenceFactory().create(coordSeq_);
            for (int i = 0; i < 3; i++) {
                newseq.setOrdinate(_idx1, i, coordSeq_.getOrdinate(_idx2, i));
                if (isClosed_ && _idx1 == 0) {
                    newseq.setOrdinate(newseq.size() - 1, i, coordSeq_.getOrdinate(_idx2, i));
                }
            }
            for (int i = 0; i < 3; i++) {
                newseq.setOrdinate(_idx2, i, coordSeq_.getOrdinate(_idx1, i));
                if (isClosed_ && _idx2 == 0) {
                    newseq.setOrdinate(newseq.size() - 1, i, coordSeq_.getOrdinate(_idx1, i));
                }
            }
            coordSeq_ = newseq;
            zone_.setCoordinateSequence(idxSelected_, coordSeq_, _cmd);
            for (int i = 0; i < lattrs_.size(); i++) {
                Object value = getModel(i).getObjectValueAt(_idx1);
                getModel(i).setObject(_idx1, getModel(i).getObjectValueAt(_idx2), _cmd);
                getModel(i).setObject(_idx2, value, _cmd);
            }
        }

        public void flushData() {
        }

        public void updateFromModele() {
        }

        @Override
        public void removePoints(int[] _idx, CtuluCommandContainer _cmd) {
            if (!(zone_ instanceof GISZoneCollectionGeometry)) return;
            GISZoneCollectionGeometry zone = (GISZoneCollectionGeometry) zone_;
            CtuluListSelection sel = new CtuluListSelection(_idx);
            zone.removeAtomics(idxSelected_, sel, null, _cmd);
            coordSeq_ = (CoordinateSequence) zone.getCoordinateSequence(idxSelected_).clone();
            fireTableModelListeners();
        }

        @Override
        public int addPoint(int _idxBefore, double _x, double _y, CtuluCommandContainer _cmd) {
            if (!(zone_ instanceof GISZoneCollectionGeometry)) return -1;
            GISZoneCollectionGeometry zone = (GISZoneCollectionGeometry) zone_;
            int inew = zone.addAtomic(idxSelected_, _idxBefore, _x, _y, _cmd);
            coordSeq_ = (CoordinateSequence) zone.getCoordinateSequence(idxSelected_).clone();
            fireTableModelListeners();
            return inew;
        }
    }

    /**
   * R�alise les op�rations en diff�r� sur le Modele.
   * 
   * @author Emmanuel MARTIN
   * @version $Id: EbliSingleObjectTableModel.java 6746 2011-12-01 13:43:22Z bmarchan $
   */
    protected class ModificationDeferred implements TableModelState {

        /** Les modeles pour la g�om�trie, pour chaque colonne */
        protected List<GISAttributeModel> lattmdls = new ArrayList<GISAttributeModel>();

        /** Vrai si il y a eu une modification. */
        protected boolean modificationDone_;

        public ModificationDeferred() {
            for (int i = 0; i < lattrs_.size(); i++) {
                lattmdls.add(((GISAttributeModel) zone_.getModel(lattrs_.get(i)).getObjectValueAt(idxSelected_)).createSubModel(new int[0]));
            }
        }

        public GISAttributeModel getModel(int _idx) {
            return lattmdls.get(_idx);
        }

        @Override
        public void setValueAt(Object _value, int _idxRow, int _idxCol) {
            if (_idxCol == 1 || _idxCol == 2) {
                modificationDone_ = true;
                clearError();
                coordSeq_.setOrdinate(_idxRow, _idxCol - 1, (Double) _value);
                if (isClosed_ && _idxRow == 0) {
                    coordSeq_.setOrdinate(coordSeq_.size() - 1, _idxCol - 1, (Double) _value);
                }
            } else if (_idxCol > 2) {
                lattmdls.get(_idxCol - 3).setObject(_idxRow, _value, null);
            }
            fireTableModelListeners();
        }

        public void flushData() {
            if (modificationDone_) {
                if (!(zone_ instanceof GISZoneCollectionGeometry)) return;
                GISZoneCollectionGeometry zone = (GISZoneCollectionGeometry) zone_;
                modeleListener_.setInactive(true);
                CtuluCommandComposite cmd = new CtuluCommandComposite("Modification d'une g�om�trie");
                Geometry old = zone.getGeometry(idxSelected_);
                zone.setGeometry(idxSelected_, GISGeometryFactory.INSTANCE.createGeometry(old.getClass(), coordSeq_), cmd);
                for (int i = 0; i < lattrs_.size(); i++) {
                    zone_.getModel(lattrs_.get(i)).setObject(idxSelected_, lattmdls.get(i), cmd);
                }
                modeleListener_.setInactive(false);
                putGeomModified(cmd);
                modificationDone_ = false;
                if (cmd_ != null) {
                    cmd_.addCmd(cmd.getSimplify());
                }
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case 0:
                    return rowIndex;
                case 1:
                    return coordSeq_.getOrdinate(rowIndex, 0);
                case 2:
                    return coordSeq_.getOrdinate(rowIndex, 1);
                default:
                    return lattmdls.get(columnIndex - 3).getObjectValueAt(rowIndex);
            }
        }

        @Override
        public int getRowCount() {
            if (isClosed_) return coordSeq_.size() - 1; else return coordSeq_.size();
        }

        @Override
        public void switchPoints(int _idx1, int _idx2, CtuluCommandContainer _cmd) {
            modificationDone_ = true;
            for (int i = 1; i < getColumnCount(); i++) {
                Object val = getValueAt(_idx1, i);
                setValueAt(getValueAt(_idx2, i), _idx1, i);
                setValueAt(val, _idx2, i);
            }
            fireTableModelListeners();
        }

        public void updateFromModele() {
            lattmdls.clear();
            for (int i = 0; i < lattrs_.size(); i++) {
                lattmdls.add(((GISAttributeModel) zone_.getModel(lattrs_.get(i)).getObjectValueAt(idxSelected_)).createSubModel(new int[0]));
            }
        }

        @Override
        public void removePoints(int[] _idx, CtuluCommandContainer _cmd) {
            Coordinate[] coords = new Coordinate[coordSeq_.size() - _idx.length];
            int ind = 0;
            int icpt = 0;
            for (int i = 0; i < coordSeq_.size(); i++) {
                if (icpt < _idx.length && _idx[icpt] == i) {
                    icpt++;
                } else {
                    coords[ind] = coordSeq_.getCoordinateCopy(i);
                    ind++;
                }
            }
            if (isClosed_) {
                coords[coords.length - 1] = coords[0];
            }
            CoordinateSequence seq = new GISCoordinateSequenceFactory().create(coords);
            if (!modele_.isCoordinateValid(seq, analyzer_)) return;
            coordSeq_ = seq;
            modificationDone_ = true;
            for (int i = 0; i < lattmdls.size(); i++) {
                lattmdls.set(i, lattmdls.get(i).createSubModel(_idx));
            }
            fireTableModelListeners();
        }

        @Override
        public int addPoint(int _idxBefore, double _x, double _y, CtuluCommandContainer _cmd) {
            Coordinate[] coords = new Coordinate[coordSeq_.size() + 1];
            int ind = 0;
            for (int i = 0; i < coordSeq_.size() + 1; i++) {
                if (i == _idxBefore + 1) {
                    coords[i] = new Coordinate(_x, _y, 0);
                } else {
                    coords[i] = coordSeq_.getCoordinateCopy(ind);
                    ind++;
                }
            }
            if (isClosed_) {
                coords[coords.length - 1] = coords[0];
            }
            CoordinateSequence seq = new GISCoordinateSequenceFactory().create(coords);
            if (!modele_.isCoordinateValid(seq, analyzer_)) return -1;
            modificationDone_ = true;
            for (int i = 0; i < lattmdls.size(); i++) {
                final GISAttributeModel m = getModel(i);
                final GISReprojectInterpolateurI interpolateur = GISZoneAttributeFactory.create1DInterpolateur(lattmdls.get(i).getAttribute(), coordSeq_, seq, m);
                lattmdls.set(i, m.deriveNewModel(seq.size(), interpolateur));
            }
            coordSeq_ = seq;
            fireTableModelListeners();
            return _idxBefore + 1;
        }
    }

    /**
   * Une classe d'adaptation du modele pour les attributs
   * @author Bertrand Marchand (marchand@deltacad.fr)
   */
    public class AttributesDataModelAdapter implements ZEditionAttributesDataI {

        Map<GISAttributeInterface, Integer> attr2Col = new HashMap<GISAttributeInterface, Integer>();

        public AttributesDataModelAdapter() {
            for (int i = 0; i < lattrs_.size(); i++) {
                attr2Col.put(lattrs_.get(i), i + 3);
            }
        }

        @Override
        public int getNbVertex() {
            return getRowCount();
        }

        @Override
        public int getNbValues() {
            return getColumnCount() - 3;
        }

        @Override
        public GISAttributeInterface getAttribute(int _i) {
            return lattrs_.get(_i);
        }

        @Override
        public Object getValue(GISAttributeInterface _attr, int _idxVertex) {
            return getValueAt(_idxVertex, attr2Col.get(_attr));
        }

        @Override
        public void setValue(GISAttributeInterface _attr, int _idxVertex, Object _val) {
            setValueAt(_val, _idxVertex, attr2Col.get(_attr));
        }
    }

    /** La ZModeleGeometry contenant la g�om�trie. */
    protected ZModeleEditable modele_;

    /** La g�om�trie selectionn�e. */
    protected int idxSelected_;

    /** Le CtuluCommandContainer pour g�rer l'undo/redo. */
    protected CtuluCommandContainer cmd_;

    /** Le container de listener pour le tableau. */
    protected Set<TableModelListener> listenersTable_ = new HashSet<TableModelListener>();

    /** Le container de listener pour les erreurs. */
    protected Set<EbliSingleObjectTableModelErrorListener> listenersError_ = new HashSet<EbliSingleObjectTableModelErrorListener>();

    /** Le listener de la zone. */
    protected ZModeleGeometryListener modeleListener_ = new ZModeleGeometryListener();

    /** Les d�finitions de coordonn�es */
    protected EbliCoordinateDefinition[] coordDefs_;

    /** L'�tat actuel de l'instance. */
    protected TableModelState stat_;

    /** Container d'erreurs. */
    protected CtuluAnalyze analyzer_;

    /** Si � faux les attributs atomiques ne doivent pas �tre visible. */
    protected boolean showAttributes_;

    /** La GISZoneCollection contenue dans le mod�le. */
    protected GISZoneCollection zone_;

    /** Liste des attributs atomiques dans l'ordre des colonnes */
    protected List<GISAttributeInterface> lattrs_ = new ArrayList<GISAttributeInterface>();

    /** La CoordinateSequence de la g�om�trie. */
    protected CoordinateSequence coordSeq_;

    /** Vrai si la g�om�trie est d�j� en 'modifi�'. */
    protected boolean alreadyModified_ = false;

    /** La g�om�trie est ferm�e => 1 point de moins affich�. */
    protected boolean isClosed_;

    /**
   * L'�couteur de la GISZoneCollection. En cas de modification dans celle-ci,
   * les caches sont mises � jour et un fire est lanc�.
   */
    protected class ZModeleGeometryListener implements ZModelGeometryListener {

        boolean isActive_;

        public void attributeAction(Object _source, int att, GISAttributeInterface _att, int _action) {
            if (!isActive_) return;
            updateCaches();
            fireTableModelListeners();
        }

        public void attributeValueChangeAction(Object _source, int att, GISAttributeInterface _att, int geom, Object value) {
            if (!isActive_) return;
            if (_att == GISAttributeConstants.ETAT_GEOM) {
                alreadyModified_ = false;
            }
            updateCaches();
            fireTableModelListeners();
        }

        public void geometryAction(Object _source, int geom, Geometry _geom, int _action) {
            if (!isActive_) return;
            updateCaches();
            fireTableModelListeners();
        }

        /**
     * Desactive temporairement le listener pour eviter des evenements en boucle.
     * @param _b
     */
        public void setInactive(boolean _b) {
            isActive_ = !_b;
        }
    }

    /**
   * @param _modele
   *          le modele contenant la g�om�trie. Peut �tre null.
   * @param _idxSelected
   *          l'index de la g�om�trie selectionn�. -1 si aucune de selectionn�.
   * @param _defs
   *          Les definitions de coordonn�es x et y. Peut �tre null.
   * @param _cmd
   *          le gestionnaire d'undo/redo. Peut �tre null
   * @param _showAttributes
   *          Si faux les attributs atomiques ne seront pas visibles.
   * @exception IllegalArgumentException
   *              si _idxSelected n'appartient pas � _zone.
   */
    public EbliSingleObjectTableModel(ZModeleEditable _modele, int _idxSelected, EbliCoordinateDefinition[] _defs, CtuluCommandContainer _cmd, boolean _showAttributes) {
        if ((_modele == null && _idxSelected != -1) || (_modele != null && _idxSelected != -1 && (_idxSelected < 0 || _idxSelected >= _modele.getNombre()))) {
            throw new IllegalArgumentException("L'index de g�om�trie n'appartient pas � la zone.");
        }
        modele_ = _modele;
        idxSelected_ = _idxSelected;
        coordDefs_ = _defs;
        cmd_ = _cmd;
        showAttributes_ = _showAttributes;
        analyzer_ = new CtuluAnalyze();
        if (modele_ != null) {
            modele_.addModelListener(modeleListener_);
        }
        stat_ = new ModificationOnTheFly();
        updateCaches();
    }

    /**
   * Bloque ou d�bloque la visibilit� des attributs atomiques.
   */
    public void setShowAttributesAtomics(boolean _b) {
        if (showAttributes_ != _b) {
            showAttributes_ = _b;
            updateCaches();
            fireTableModelListeners();
        }
    }

    /**
   * Retourne vrai si les attributs atomiques sont visibles.
   */
    public boolean isAtomicsAttributesShowed() {
        return showAttributes_;
    }

    /**
   * Permet de changer la source des donn�es.
   * 
   * @param _modele
   *          le nouveau modele
   * @param _idxSelected
   *          le nouvelle index
   * @exception IllegalArgumentException
   *              si _idxSelected n'appartient pas � _zone.
   */
    public void setSource(ZModeleEditable _modele, int _idxSelected) {
        if ((_modele == null && _idxSelected != -1) || (_modele != null && _idxSelected != -1 && (_idxSelected < 0 || _idxSelected >= modele_.getNombre()))) {
            throw new IllegalArgumentException("L'index de g�om�trie n'appartient pas � la zone.");
        }
        if (modele_ != _modele || idxSelected_ != _idxSelected) {
            if (modele_ != null) {
                modele_.removeModelListener(modeleListener_);
            }
            modele_ = _modele;
            if (modele_ != null) {
                modele_.addModelListener(modeleListener_);
            }
            idxSelected_ = _idxSelected;
            updateCaches();
            fireTableModelListeners();
        }
    }

    /**
   * Active les modifications en diff�r�es.
   * Les modifications en cours sont effetu�es.
   */
    public void setDeferredModifications(boolean _active) {
        if (_active && !(stat_ instanceof ModificationDeferred)) {
            stat_.flushData();
            stat_ = new ModificationDeferred();
        } else if (!_active && !(stat_ instanceof ModificationOnTheFly)) {
            stat_.flushData();
            stat_ = new ModificationOnTheFly();
        }
    }

    /**
   * Provoque l'�criture des op�rations en cours.
   * Inutile si en mode simultan�.
   */
    public void flushData() {
        stat_.flushData();
    }

    /**
   * Changement de la g�om�trie selectionn�e.
   * 
   * @param _idxselected
   *          l'index de la g�om�trie
   * @exception IllegalArgumentException
   *              si _idxSelected n'appartient pas � _zone.
   */
    public void setSelectionGeometry(int _idxSelected) {
        if (_idxSelected != -1 && (_idxSelected < 0 || _idxSelected >= modele_.getNombre())) {
            throw new IllegalArgumentException("L'index de g�om�trie n'appartient pas � la zone.");
        }
        if (idxSelected_ != _idxSelected) {
            idxSelected_ = _idxSelected;
            fireTableModelListeners();
        }
    }

    /**
   * Retourne les d�finitions de coordonn�es.
   */
    public EbliCoordinateDefinition[] getCoordinateDefs() {
        return coordDefs_;
    }

    /**
   * Retourne le mod�le contenant les g�o�mtries.
   */
    public ZModeleGeometry getModeleDonnees() {
        return modele_;
    }

    /**
   * Retourne l'attribut pour la colonne
   */
    public GISAttributeInterface getAttribute(int _columnIndex) {
        return lattrs_.get(_columnIndex - 3);
    }

    /**
   * Retourne l'index de la g�om�trie selectionn�e.
   */
    public int getSelectedGeometry() {
        return idxSelected_;
    }

    /**
   * Changement du gestionnaire d'undo/redo.
   */
    public void setUndoRedoContainer(CtuluCommandContainer _cmd) {
        cmd_ = _cmd;
    }

    /**
   * Retourne le gestionnaire d'undo/redo.
   */
    public CtuluCommandContainer getUndoRedoContainer() {
        return cmd_;
    }

    public void addTableModelListener(TableModelListener l) {
        if (l != null && !listenersTable_.contains(l)) {
            listenersTable_.add(l);
        }
    }

    public void addTableModelModeleAdapterErrorListener(EbliSingleObjectTableModelErrorListener l) {
        if (l != null && !listenersError_.contains(l)) {
            listenersError_.add(l);
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return Integer.class;
            case 1:
                return Double.class;
            case 2:
                return Double.class;
            default:
                if (lattrs_ != null) {
                    return lattrs_.get(columnIndex - 3).getDataClass();
                } else {
                    return null;
                }
        }
    }

    public int getColumnCount() {
        if (lattrs_ != null) {
            return 3 + lattrs_.size();
        } else {
            return 3;
        }
    }

    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return EbliLib.getS("Index");
            case 1:
                return coordDefs_ == null ? "X" : coordDefs_[0].getName();
            case 2:
                return coordDefs_ == null ? "Y" : coordDefs_[1].getName();
            default:
                if (lattrs_ != null) {
                    return lattrs_.get(columnIndex - 3).getName();
                } else {
                    return null;
                }
        }
    }

    public int getRowCount() {
        if (zone_ != null && idxSelected_ != -1) {
            return stat_.getRowCount();
        } else {
            return 0;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (modele_ == null || idxSelected_ == -1) {
            throw new IllegalArgumentException("Aucun modele ou aucune g�om�trie selectionn�e.");
        }
        if (columnIndex == 0) {
            return rowIndex + 1;
        }
        return stat_.getValueAt(rowIndex, columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (modele_ == null || idxSelected_ == -1) {
            throw new IllegalArgumentException("Aucun modele ou aucune g�om�trie selectionn�e.");
        }
        switch(columnIndex) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return true;
            default:
                return lattrs_.get(columnIndex - 3).isEditable();
        }
    }

    public void removeTableModelListener(TableModelListener l) {
        if (l != null && listenersTable_.contains(l)) {
            listenersTable_.remove(l);
        }
    }

    public void removeTableModelModeleAdapterErrorListener(EbliSingleObjectTableModelErrorListener l) {
        if (l != null && listenersError_.contains(l)) {
            listenersError_.remove(l);
        }
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (modele_ == null || idxSelected_ == -1) {
            throw new IllegalArgumentException("Aucun modele ou aucune g�om�trie selectionn�e.");
        }
        if (!getValueAt(rowIndex, columnIndex).equals(value) && value != null) {
            stat_.setValueAt(value, rowIndex, columnIndex);
        }
    }

    /**
   * Incr�mente de n les index donn�s.
   * Pour d�cr�menter donner un indice n�gatif.
   * _idx est modifi� dans l'op�ration
   * @return le tableau des nouveaux index.
   */
    public int[] movePoints(int[] _idx, int n) {
        if (_idx == null) {
            throw new IllegalArgumentException("_idx ne doit pas �tre null");
        }
        for (int i = 0; i < _idx.length; i++) {
            if (_idx[i] < 0 || _idx[i] >= getRowCount()) {
                throw new IllegalArgumentException("Au moins un des index n'est pas valide.");
            }
        }
        CtuluCommandComposite cmd = new CtuluCommandComposite("Switch points");
        Arrays.sort(_idx);
        if (n > 0) {
            reverse(_idx);
        }
        for (int i = 0; i < _idx.length; i++) {
            int idx = _idx[i];
            if (idx + n >= 0 && idx + n < getRowCount() && !in(idx + n, _idx)) {
                _idx[i] = idx + n;
                stat_.switchPoints(idx, idx + n, cmd);
            }
        }
        if (cmd_ != null) {
            cmd_.addCmd(cmd.getSimplify());
        }
        return _idx;
    }

    /**
   * Inverse la position de deux des points de la g�om�trie.
   */
    public void switchPoints(int _idx1, int _idx2) {
        if (_idx1 < 0 || _idx1 >= getRowCount() || _idx2 < 0 || _idx2 >= getRowCount()) {
            throw new IllegalArgumentException("Au moins un des deux index n'est pas valide.");
        }
        CtuluCommandComposite cmd = new CtuluCommandComposite(EbliLib.getS("Switch de deux points"));
        stat_.switchPoints(_idx1, _idx2, cmd);
        if (cmd_ != null) {
            cmd_.addCmd(cmd.getSimplify());
        }
    }

    /**
   * Ajoute un point avant l'indice pass� en argument.
   * @param _idxAfter L'indice. Peut �tre -1, pour ajouter un point en fin de tableau.
   */
    public int addPoint(int _idxAfter) {
        CtuluCommandComposite cmd = new CtuluCommandComposite(EbliLib.getS("Ajout de sommet(s)"));
        int idxBefore = _idxAfter - 1;
        if (_idxAfter == -1) idxBefore = getRowCount() - 1;
        double x;
        double y;
        if (idxBefore == -1 && !isClosed_) {
            double x1 = (Double) getValueAt(idxBefore + 1, 1);
            double y1 = (Double) getValueAt(idxBefore + 1, 2);
            double x2 = (Double) getValueAt((idxBefore + 2) % getRowCount(), 1);
            double y2 = (Double) getValueAt((idxBefore + 2) % getRowCount(), 2);
            x = (x1 - (x2 - x1) / 2);
            y = (y1 - (y2 - y1) / 2);
        } else if (idxBefore == getRowCount() - 1 && !isClosed_) {
            double x1 = (Double) getValueAt((idxBefore - 1 + getRowCount()) % getRowCount(), 1);
            double y1 = (Double) getValueAt((idxBefore - 1 + getRowCount()) % getRowCount(), 2);
            double x2 = (Double) getValueAt(idxBefore, 1);
            double y2 = (Double) getValueAt(idxBefore, 2);
            x = (x2 + (x2 - x1) / 2);
            y = (y2 + (y2 - y1) / 2);
        } else {
            double x1 = (Double) getValueAt((idxBefore + getRowCount()) % getRowCount(), 1);
            double y1 = (Double) getValueAt((idxBefore + getRowCount()) % getRowCount(), 2);
            double x2 = (Double) getValueAt((idxBefore + 1) % getRowCount(), 1);
            double y2 = (Double) getValueAt((idxBefore + 1) % getRowCount(), 2);
            x = (x1 + x2) / 2;
            y = (y1 + y2) / 2;
        }
        int inew = stat_.addPoint(idxBefore, x, y, cmd);
        if (cmd_ != null) {
            cmd_.addCmd(cmd.getSimplify());
        }
        return inew;
    }

    /**
   * Supprime les points s�lectionn�s 
   */
    public void removePoints(int[] _idx) {
        CtuluCommandComposite cmd = new CtuluCommandComposite(EbliLib.getS("Suppression de sommet(s)"));
        stat_.removePoints(_idx, cmd);
        if (cmd_ != null) {
            cmd_.addCmd(cmd.getSimplify());
        }
    }

    public AttributesDataModelAdapter getAttributDataModelAdapter() {
        return new AttributesDataModelAdapter();
    }

    /**
   * Met � jour les caches utilis�s dans l'instance. Cette m�thode est
   * g�n�ralement utilis� lors d'une modification de mod�le.
   */
    private void updateCaches() {
        zone_ = null;
        lattrs_.clear();
        coordSeq_ = null;
        if (modele_ != null) {
            zone_ = modele_.getGeomData();
            isClosed_ = (zone_.getGeometry(idxSelected_) instanceof LinearRing);
            if (showAttributes_) {
                GISAttributeInterface attrZ = zone_.getAttributeIsZ();
                if (attrZ != null && attrZ.isAtomicValue()) {
                    lattrs_.add(attrZ);
                }
                for (int i = 0; i < zone_.getNbAttributes(); i++) {
                    GISAttributeInterface attribute = zone_.getAttribute(i);
                    if (attribute.isAtomicValue() && (attrZ == null || attrZ != attribute)) {
                        lattrs_.add(attribute);
                    }
                }
            }
            coordSeq_ = new GISCoordinateSequenceFactory().create(zone_.getCoordinateSequence(idxSelected_));
            stat_.updateFromModele();
        }
    }

    /**
   * Met les editors et les renderer correcte sur le JTable.
   */
    public void updateEditorAndRenderer(JTable _table) {
        TableColumnModel cols = _table.getColumnModel();
        TableCellEditor editorXY = CtuluValueEditorDefaults.DOUBLE_EDITOR.createTableEditorComponent();
        cols.getColumn(1).setCellEditor(editorXY);
        cols.getColumn(2).setCellEditor(editorXY);
        cols.getColumn(1).setCellRenderer(new CtuluCellDoubleRenderer(coordDefs_[0].getFormatter().getXYFormatter()));
        cols.getColumn(2).setCellRenderer(new CtuluCellDoubleRenderer(coordDefs_[1].getFormatter().getXYFormatter()));
        if (modele_ != null) {
            for (int i = 0; i < lattrs_.size(); i++) {
                CtuluValueEditorI editor = lattrs_.get(i).getEditor();
                if (editor != null) {
                    cols.getColumn(3 + i).setCellEditor(editor.createTableEditorComponent());
                }
                if (lattrs_.get(i).getDataClass().equals(Double.class)) {
                    cols.getColumn(3 + i).setCellRenderer(new CtuluCellDoubleRenderer(coordDefs_[2].getFormatter().getXYFormatter()));
                } else if (lattrs_.get(i).getDataClass().equals(Boolean.class)) {
                    cols.getColumn(3 + i).setCellRenderer(new CtuluCellBooleanRenderer());
                }
            }
        }
    }

    /**
   * Vide le container d'erreur et envoie un �v�nemen si besoin.
   */
    private void clearError() {
        if (analyzer_.containsFatalError()) {
            analyzer_.clear();
            fireTableModelModeleAdapterNoError();
        }
    }

    /**
   * Retourne un message d'erreur si la g�o�mtrie en cours de cr�ation est
   * invalide.
   */
    public String getErrorMessage() {
        analyzer_.clear();
        if (modele_.isCoordinateValid(coordSeq_, analyzer_) && modele_.isDataValid(coordSeq_, getAttributDataModelAdapter(), analyzer_)) {
            return null;
        } else {
            return analyzer_.getFatalError();
        }
    }

    protected void fireTableModelListeners() {
        for (TableModelListener listener : listenersTable_) {
            listener.tableChanged(new TableModelEvent(this));
        }
    }

    protected void fireTableModelModeleAdapterNoError() {
        for (EbliSingleObjectTableModelErrorListener listener : listenersError_) {
            listener.modeleAdpaterNoError();
        }
    }

    protected void fireTableModelModeleAdapterError() {
        for (EbliSingleObjectTableModelErrorListener listener : listenersError_) {
            listener.modeleAdapterError(analyzer_.getFatalError());
        }
    }

    /**
   * Met l'�tat de la g�om�trie (GISAttributeConstants.ETAT_GEOM) � modifi�.
   */
    private void putGeomModified(CtuluCommandContainer _cmd) {
        if (!alreadyModified_) {
            int idxEtatGeom = zone_.getIndiceOf(GISAttributeConstants.ETAT_GEOM);
            if (idxEtatGeom != -1) {
                zone_.setAttributValue(idxEtatGeom, idxSelected_, GISAttributeConstants.ATT_VAL_ETAT_MODI, _cmd);
            }
            alreadyModified_ = true;
        }
    }

    /**
   * Retourne vrai si _value est dans _table
   * @param _value
   * @param _table
   * @return
   */
    private boolean in(int _value, int[] _table) {
        boolean found = false;
        int i = -1;
        while (!found && ++i < _table.length) {
            found = _table[i] == _value;
        }
        return found;
    }

    /**
   * Reverse the table.
   */
    private void reverse(int[] _table) {
        for (int i = 0; i < _table.length / 2; i++) {
            int tmp = _table[i];
            _table[i] = _table[_table.length - i - 1];
            _table[_table.length - i - 1] = tmp;
        }
    }
}
