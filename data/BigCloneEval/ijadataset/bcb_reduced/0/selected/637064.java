package org.fudaa.ebli.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.memoire.bu.*;
import org.fudaa.ctulu.CtuluLib;
import org.fudaa.ctulu.CtuluLibString;
import org.fudaa.ctulu.CtuluRange;
import org.fudaa.ctulu.gui.CtuluDialog;
import org.fudaa.ctulu.gui.CtuluDialogPanel;
import org.fudaa.ctulu.gui.CtuluPopupMenu;
import org.fudaa.ctulu.gui.CtuluValueEditorDefaults;
import org.fudaa.ctulu.gui.CtuluValueEditorI;
import org.fudaa.ebli.commun.EbliLib;
import org.fudaa.ebli.controle.BSelectionDecimalFormat;
import org.fudaa.ebli.palette.BPaletteInterface;
import org.fudaa.ebli.palette.BPalettePlageAbstract;
import org.fudaa.ebli.palette.BPalettePlageLegende;
import org.fudaa.ebli.palette.BPalettePlageTarget;
import org.fudaa.ebli.palette.BPlage;
import org.fudaa.ebli.palette.BPlageAbstract;
import org.fudaa.ebli.ressource.EbliResource;
import org.fudaa.ebli.trace.BPalettePlageDefault;
import org.fudaa.ebli.trace.BPalettePlageInterface;
import org.fudaa.ebli.trace.BPlageInterface;
import org.fudaa.ebli.trace.TraceIcon;

/**
 * @author Fred Deniger
 * @version $Id: PaletteSelecteurCouleurPlage.java,v 1.21 2006-09-19 14:55:52 deniger Exp $
 */
public class PaletteSelecteurCouleurPlage extends JPanel implements PropertyChangeListener, ListSelectionListener, ActionListener, MouseListener, BPaletteInterface, BuBorders {

    class PlageTableModel extends AbstractListModel {

        public PlageTableModel() {
            plages_ = new ArrayList();
        }

        protected void fireAdd(final int _min, final int _max) {
            fireIntervalAdded(this, _min, _max);
        }

        protected void fireAllSupr(final int _oldSize) {
            if (_oldSize > 0) {
                fireIntervalRemoved(this, 0, _oldSize - 1);
            }
        }

        protected void fireModified() {
            final int i = list_.getSelectedIndex();
            fireContentsChanged(this, i, i);
        }

        protected void fireModified(final Object _o) {
            final int i = plages_.indexOf(_o);
            if (i >= 0) {
                fireContentsChanged(this, i, i);
            }
        }

        protected void fireRemoved(final int _idx) {
            fireIntervalRemoved(this, _idx, _idx);
        }

        public Object getElementAt(final int _index) {
            return plages_.get(_index);
        }

        public int getSize() {
            return getRealSize();
        }

        private int getRealSize() {
            return plages_ == null ? 0 : plages_.size();
        }

        public void modifyColors(final PaletteCouleur _palette) {
            if (_palette != null) {
                _palette.updatePlages(plages_);
                plagesUpdated();
            }
        }

        public void plagesUpdated() {
            final boolean enable = plages_.size() > 0;
            btApply_.setEnabled(enable);
            btColor_.setEnabled(enable);
            txtMax_.setEnabled(enable);
            txtMin_.setEnabled(enable);
        }

        /**
     * @param _pal la palette qui sert pour initialiser le composant
     */
        public void setPlages(final BPalettePlageInterface _pal) {
            plageEnCours_ = null;
            isAdjusting_ = true;
            if (_pal == null || _pal.getNbPlages() == 0) {
                final int oldSize = plages_.size();
                plages_.clear();
                fireAllSupr(oldSize);
                updatePanel();
            } else {
                final int nb = _pal.getNbPlages();
                final int oldSize = plages_.size();
                plages_.clear();
                fireAllSupr(oldSize);
                for (int i = 0; i < nb; i++) {
                    plages_.add(_pal.getPlageInterface(i).copy());
                }
                fireAdd(0, nb - 1);
                isAdjusting_ = false;
                list_.setSelectedIndex(0);
            }
            isAdjusting_ = false;
            plagesUpdated();
        }
    }

    /**
   * @param _o la cible a tester
   * @return true si la cible est correcte
   */
    public static final boolean isTargetValid(final Object _o) {
        return (_o instanceof BPalettePlageTarget) && ((BPalettePlageTarget) _o).isPaletteModifiable();
    }

    private transient BPalettePlageDefault default_;

    JButton btApply_;

    BuToolButton btAssombrir_;

    JButton btColor_;

    BuToolButton btEcl_;

    BuButton btFormat_;

    JButton btRefresh_;

    BuCheckBox cbChangedLeg_;

    CtuluRange dataBoite_;

    TraceIcon ic_;

    boolean isAdjusting_;

    BuMenuItem itemRemove_;

    BuMenuItem itemSplit_;

    JLabel lbPalTitle_;

    JList list_;

    CtuluPopupMenu menu_;

    PlageTableModel model_;

    JPanel panelData_;

    BPlageAbstract plageEnCours_;

    List plages_;

    BuScrollPane sp_;

    BPalettePlageTarget target_;

    JTextField tfPlageLeg_;

    JTextField tfTitlePalette_;

    JComponent txtMax_;

    JComponent txtMin_;

    CtuluValueEditorI valueEditor_ = CtuluValueEditorDefaults.DOUBLE_EDITOR;

    protected boolean isDiscreteTarget_;

    public PaletteSelecteurCouleurPlage() {
        super();
        final BuBorderLayout lay = new BuBorderLayout(2, 2);
        setLayout(lay);
        final BuPanel top = new BuPanel(lay);
        lbPalTitle_ = new BuLabel();
        lbPalTitle_.setHorizontalTextPosition(SwingConstants.CENTER);
        lbPalTitle_.setHorizontalAlignment(SwingConstants.CENTER);
        lbPalTitle_.setFont(BuLib.deriveFont(lbPalTitle_.getFont(), Font.BOLD, 0));
        top.add(lbPalTitle_, BuBorderLayout.NORTH);
        tfTitlePalette_ = new BuTextField(10);
        final BuPanel pnTitle = new BuPanel(new BuGridLayout(2));
        pnTitle.add(new BuLabel(EbliLib.getS("Titre:")));
        pnTitle.add(tfTitlePalette_);
        top.add(pnTitle, BuBorderLayout.CENTER);
        top.setBorder(EMPTY3333);
        add(top, BuBorderLayout.NORTH);
        list_ = new BuEmptyList();
        model_ = new PlageTableModel();
        list_.setModel(model_);
        list_.setCellRenderer(new BPalettePlageLegende.PlageCellRenderer());
        list_.getSelectionModel().addListSelectionListener(this);
        list_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_.addMouseListener(this);
        list_.setFocusable(false);
        sp_ = new BuScrollPane(list_);
        sp_.setPreferredWidth(70);
        add(sp_, BuBorderLayout.CENTER);
        final BuPanel east = new BuPanel();
        east.setLayout(lay);
        final BuPanel pnColor = new BuPanel();
        pnColor.setLayout(new BuButtonLayout(1, SwingConstants.LEFT));
        btAssombrir_ = new BuToolButton();
        btAssombrir_.setIcon(EbliResource.EBLI.getIcon("assombrir"));
        btAssombrir_.setToolTipText(EbliLib.getS("Assombrir"));
        btAssombrir_.addActionListener(this);
        btEcl_ = new BuToolButton();
        btEcl_.setIcon(EbliResource.EBLI.getIcon("eclaircir"));
        btEcl_.setToolTipText(EbliLib.getS("Eclaircir"));
        btEcl_.addActionListener(this);
        pnColor.add(btAssombrir_);
        pnColor.add(btEcl_);
        east.add(pnColor, BuBorderLayout.SOUTH);
        updatePanelData(east);
        add(east, BorderLayout.EAST);
        btApply_ = new BuButton(BuResource.BU.getIcon("appliquer"));
        btApply_.setText(BuResource.BU.getString("Appliquer"));
        btApply_.setToolTipText(EbliLib.getS("Appliquer les modifications"));
        btApply_.addActionListener(this);
        btApply_.setEnabled(false);
        btRefresh_ = new BuButton(BuResource.BU.getIcon("rafraichir"));
        btRefresh_.setText(EbliLib.getS("initialiser"));
        btRefresh_.setToolTipText(EbliLib.getS("Initialiser les plages des couleurs"));
        btRefresh_.addActionListener(this);
        btRefresh_.setEnabled(true);
        btFormat_ = new BuButton();
        btFormat_.setText(EbliLib.getS("Format des labels"));
        btFormat_.setToolTipText(EbliLib.getS("Permet de modifier le formattage des nombres et le s�parateur de valeurs"));
        btFormat_.addActionListener(this);
        btFormat_.setEnabled(false);
        final BuPanel btpn = new BuPanel();
        btpn.setLayout(new BuButtonLayout(1, SwingConstants.RIGHT));
        btpn.add(btFormat_);
        btpn.add(btRefresh_);
        btpn.add(btApply_);
        add(btpn, BuBorderLayout.SOUTH);
    }

    private void actionApply() {
        savePanel();
        final BPlageInterface[] vp = new BPlageInterface[plages_.size()];
        plages_.toArray(vp);
        if (target_ != null) {
            default_.pls_ = vp;
            default_.titre_ = tfTitlePalette_.getText();
            target_.setPaletteCouleurPlages(default_);
        }
    }

    /**
   * @param _bt
   */
    private void actionDarken(final Object _bt) {
        final boolean dark = (_bt == btAssombrir_);
        for (int i = plages_.size() - 1; i >= 0; i--) {
            final BPlageAbstract p = (BPlageAbstract) plages_.get(i);
            p.setCouleur(dark ? p.getCouleur().darker() : p.getCouleur().brighter());
        }
        updatePanel();
        model_.fireModified();
    }

    private void actionFormat() {
        DecimalFormat fmt = getDefaultFormat();
        if (fmt == null) {
            fmt = CtuluLib.THREE_DIGITS_FORMAT;
        }
        final BSelectionDecimalFormat fmtSelect = new BSelectionDecimalFormat(fmt);
        final CtuluDialogPanel pn = new CtuluDialogPanel();
        pn.setLayout(new BuVerticalLayout(4));
        final JPanel pnSep = new BuPanel(new BuGridLayout(2, 2, 2));
        pnSep.add(new BuLabel(EbliLib.getS("S�parateur")));
        final BuTextField tf = new BuTextField();
        tf.setText(default_.getSep());
        pnSep.add(tf);
        pn.add(pnSep);
        fmtSelect.setBorder(BorderFactory.createTitledBorder(EbliLib.getS("Format d�cimal")));
        pn.add(fmtSelect);
        if (CtuluDialogPanel.isOkResponse(pn.afficheModale(this, EbliLib.getS("Format")))) {
            default_.formatter_ = fmtSelect.getCurrentFmt();
            default_.sep_ = tf.getText();
            ajusteAllLegendes();
        }
    }

    private void actionRemove() {
        int i = list_.getSelectedIndex();
        plages_.remove(i);
        model_.fireRemoved(i);
        plageEnCours_ = null;
        final int nb = plages_.size();
        if (i >= nb) {
            i = nb - 1;
        }
        if (nb != 0) {
            list_.setSelectedIndex(i);
            plageEnCours_ = getSelectedPlage();
            updatePanel();
        }
    }

    private void actionSplit() {
        if (plageEnCours_ == null) {
            return;
        }
        final double min = plageEnCours_.getMin();
        final double max = plageEnCours_.getMax();
        final double nmilieu = (min + max) / 2;
        ((BPlage) plageEnCours_).setMax(nmilieu);
        final BPlage nplage = new BPlage(plageEnCours_);
        nplage.setMinMax(nmilieu, max);
        final int i = list_.getSelectedIndex();
        Color sup = Color.MAGENTA;
        if (i < (plages_.size() - 1)) {
            sup = ((BPlage) plages_.get(i + 1)).getCouleur();
            plages_.add(i + 1, nplage);
        } else {
            plages_.add(nplage);
        }
        if (plageEnCours_.getCouleur() == sup) {
            sup = Color.gray;
        }
        nplage.setCouleur(BPalettePlageAbstract.getCouleur(plageEnCours_.getCouleur(), sup, 0.5));
        model_.fireAdd(i + 1, i + 1);
        updatePanel();
    }

    private void reinitPlages() {
        final PaletteRefreshPanel pn = new PaletteRefreshPanel(this);
        final Frame f = CtuluLib.getFrameAncestor(this);
        final CtuluDialog dial = new CtuluDialog(f, pn);
        pn.setOwner(dial);
        dial.setTitle(EbliLib.getS("Initialiser"));
        dial.afficheDialogModal();
    }

    /**
   * @param _pal la palette qui sert pour initialiser le composant
   */
    private void setPlages(final BPalettePlageInterface _pal) {
        model_.setPlages(_pal);
        btApply_.setEnabled(_pal != null);
        btRefresh_.setEnabled(target_ != null && (target_.isDonneesBoiteAvailable()));
        btColor_.setEnabled(_pal != null);
        if (target_ == null) {
            setTitre(CtuluLibString.EMPTY_STRING);
        } else {
            final String dataDesc = target_.getDataDescription();
            setTitre(dataDesc == null ? CtuluLibString.EMPTY_STRING : dataDesc);
        }
    }

    /**
   * Met a jour le panneau des donn�es.
   */
    private void updatePanelData(final BuPanel _dest) {
        final String suff = ": ";
        if (panelData_ == null) {
            panelData_ = new BuPanel();
            panelData_.setLayout(new BuGridLayout(2));
        } else {
            panelData_.removeAll();
        }
        panelData_.add(new BuLabel(EbliLib.getS("Couleur") + suff));
        btColor_ = new JButton();
        btColor_.addActionListener(this);
        ic_ = new TraceIcon();
        btColor_.setIcon(ic_);
        panelData_.add(btColor_);
        panelData_.add(new BuLabel(EbliLib.getS("Min") + suff));
        txtMin_ = valueEditor_.createEditorComponent();
        panelData_.add(txtMin_);
        panelData_.add(new BuLabel(EbliLib.getS("Max") + suff));
        txtMax_ = valueEditor_.createEditorComponent();
        panelData_.add(txtMax_);
        if (!isDiscreteTarget_) {
            if (cbChangedLeg_ == null) {
                cbChangedLeg_ = new BuCheckBox(EbliLib.getS("Label") + suff);
                cbChangedLeg_.addItemListener(new ItemListener() {

                    public void itemStateChanged(final ItemEvent _e) {
                        if (cbChangedLeg_.isSelected()) {
                            if (plageEnCours_ != null && tfPlageLeg_ != null) {
                                ajusteSelectedPlageLegende(true);
                                tfPlageLeg_.setText(plageEnCours_.getLegende());
                            }
                        } else if (tfPlageLeg_ != null) {
                            tfPlageLeg_.setText(CtuluLibString.EMPTY_STRING);
                        }
                        tfPlageLeg_.setEnabled(cbChangedLeg_.isSelected());
                    }
                });
            }
            panelData_.add(cbChangedLeg_);
            tfPlageLeg_ = new BuTextField();
            tfPlageLeg_.setEnabled(false);
            panelData_.add(tfPlageLeg_);
        }
        if (_dest == null) {
            panelData_.revalidate();
        } else {
            _dest.add(panelData_, BuBorderLayout.CENTER);
        }
    }

    BPlageAbstract getMaxPlage() {
        if (list_.getModel().getSize() == 0) {
            return null;
        }
        return (BPlageAbstract) list_.getModel().getElementAt(list_.getModel().getSize() - 1);
    }

    double[] getMinMax() {
        final double[] r = new double[2];
        if ((plages_ == null) || (plages_.size() == 0)) {
            return null;
        }
        BPlage p = (BPlage) plages_.get(0);
        r[0] = p.getMin();
        r[1] = p.getMax();
        for (int i = plages_.size() - 1; i > 0; i--) {
            p = (BPlage) plages_.get(i);
            double d = p.getMin();
            if (d < r[0]) {
                r[0] = d;
            }
            d = p.getMax();
            if (d > r[1]) {
                r[1] = d;
            }
        }
        return r;
    }

    BPlageAbstract getMinPlage() {
        if (list_.getModel().getSize() == 0) {
            return null;
        }
        return (BPlageAbstract) list_.getModel().getElementAt(0);
    }

    BPlageAbstract getSelectedPlage() {
        return (BPlageAbstract) list_.getSelectedValue();
    }

    void savePanel() {
        boolean modified = ajusteSelectedPlageLegende(false);
        if (!isDiscreteTarget_ && plageEnCours_ != null) {
            final BPlage p = (BPlage) plageEnCours_;
            if (cbChangedLeg_ != null && cbChangedLeg_.isSelected()) {
                modified |= p.setLegende(tfPlageLeg_.getText());
            } else {
                modified |= p.ajusteLegendes(default_.getFormatter(), default_.getSep());
            }
        }
        if (modified) {
            model_.fireModified(plageEnCours_);
        }
    }

    void updatePanel() {
        if (plageEnCours_ == null) {
            ic_.setCouleur(btColor_.getForeground());
            valueEditor_.setValue(CtuluLibString.EMPTY_STRING, txtMax_);
            valueEditor_.setValue(CtuluLibString.EMPTY_STRING, txtMin_);
        } else {
            ic_.setCouleur(plageEnCours_.getCouleur());
            ic_.setTaille(plageEnCours_.getIconeTaille());
            ic_.setType(plageEnCours_.getIconeType());
            final DecimalFormat fmt = getDefaultFormat();
            String s = fmt.format(plageEnCours_.getMin());
            valueEditor_.setValue(s, txtMin_);
            txtMin_.setToolTipText(s);
            s = fmt.format(plageEnCours_.getMax());
            valueEditor_.setValue(s, txtMax_);
            txtMax_.setToolTipText(s);
        }
        if (cbChangedLeg_ != null) {
            cbChangedLeg_.setSelected(false);
        }
        btColor_.repaint();
    }

    protected void ajusteAllLegendes() {
        for (int i = model_.getSize() - 1; i >= 0; i--) {
            ((BPlageInterface) model_.getElementAt(i)).ajusteLegendes(default_.formatter_, default_.sep_);
        }
        model_.fireModified();
    }

    protected final boolean ajusteSelectedPlageLegende(final boolean _fireEvent) {
        if (!isDiscreteTarget_ && plageEnCours_ != null) {
            final BPlage p = (BPlage) plageEnCours_;
            if (p != null && (valueEditor_.isValueValidFromComponent(txtMin_)) && (valueEditor_.isValueValidFromComponent(txtMax_))) {
                double nmin = Double.parseDouble(valueEditor_.getStringValue(txtMin_));
                double nmax = Double.parseDouble(valueEditor_.getStringValue(txtMax_));
                if ((nmin != p.getMin()) || (nmax != p.getMax())) {
                    if (nmin > nmax) {
                        final double t = nmin;
                        nmin = nmax;
                        nmax = t;
                    }
                    if (p.setMinMax(nmin, nmax)) {
                        p.ajusteLegendes(default_.getFormatter(), default_.getSep());
                        if (_fireEvent) {
                            model_.fireModified(plageEnCours_);
                        }
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    protected void updateDiscreteTarget() {
        txtMax_.setVisible(!isDiscreteTarget_);
        txtMin_.setVisible(!isDiscreteTarget_);
    }

    public void actionPerformed(final ActionEvent _e) {
        final Object s = _e.getSource();
        if ((s == btColor_) && (getSelectedPlage() != null)) {
            final Color n = JColorChooser.showDialog(this, EbliResource.EBLI.getString("Couleur"), ic_.getCouleur());
            if ((n != null) && (n != ic_.getCouleur())) {
                ic_.setCouleur(n);
                plageEnCours_.setCouleur(n);
                model_.fireModified();
            }
        } else if (s == btApply_) {
            actionApply();
        } else if (s == btRefresh_) {
            reinitPlages();
        } else if (s == btAssombrir_ || s == btEcl_) {
            actionDarken(s);
        } else if (!isDiscreteTarget_ && s == itemRemove_) {
            actionRemove();
        } else if (!isDiscreteTarget_ && s == itemSplit_) {
            actionSplit();
        } else if (btFormat_ == _e.getSource()) {
            actionFormat();
        }
    }

    public void doAfterDisplay() {
    }

    public JComponent getComponent() {
        return this;
    }

    public DecimalFormat getDefaultFormat() {
        return (default_ == null || default_.getFormatter() == null) ? CtuluLib.THREE_DIGITS_FORMAT : default_.getFormatter();
    }

    public void mouseClicked(final MouseEvent _e) {
    }

    public void mouseEntered(final MouseEvent _e) {
    }

    public void mouseExited(final MouseEvent _e) {
    }

    public void mousePressed(final MouseEvent _e) {
    }

    public void mouseReleased(final MouseEvent _e) {
        if (!isDiscreteTarget_ && EbliLib.isPopupMouseEvent(_e)) {
            if (menu_ == null) {
                menu_ = new CtuluPopupMenu();
                itemSplit_ = menu_.addMenuItem(EbliLib.getS("scinder"), "DECOUPER", BuResource.BU.getIcon("couper"), true);
                itemSplit_.addActionListener(this);
                itemRemove_ = menu_.addMenuItem(EbliLib.getS("enlever"), "DELETE", BuResource.BU.getIcon("enlever"), true);
                itemRemove_.addActionListener(this);
            }
            final boolean enable = !list_.isSelectionEmpty();
            itemSplit_.setEnabled(enable);
            itemRemove_.setEnabled(enable);
            menu_.show(this, _e.getX(), _e.getY());
        }
    }

    public void paletteDeactivated() {
    }

    public void propertyChange(final PropertyChangeEvent _evt) {
        setTargetPalette(target_, true);
    }

    public boolean setTarget(final Object _target) {
        if (_target instanceof BPalettePlageTarget) {
            return setTargetPalette((BPalettePlageTarget) _target);
        }
        return setTargetPalette(null);
    }

    public boolean setTargetPalette(final BPalettePlageTarget _m) {
        return setTargetPalette(_m, true);
    }

    /**
   * @param _m le noveau modele
   * @return true si la cible est prise en compte
   */
    public boolean setTargetPalette(final BPalettePlageTarget _m, final boolean _forceUpdate) {
        if (_forceUpdate || _m != target_) {
            if (target_ != null) {
                target_.removePropertyChangeListener("paletteCouleur", this);
            }
            target_ = _m;
            if (_m != null && !_m.isPaletteModifiable()) {
                target_ = null;
            }
            if (target_ == null) {
                setPlages(null);
                setTitre(CtuluLibString.EMPTY_STRING);
                tfTitlePalette_.setText(CtuluLibString.EMPTY_STRING);
                btFormat_.setEnabled(false);
            } else if (_m != null) {
                final CtuluValueEditorI old = valueEditor_;
                valueEditor_ = CtuluValueEditorDefaults.DOUBLE_EDITOR;
                default_ = new BPalettePlageDefault(target_.getPaletteCouleur());
                if (target_.getPaletteCouleur() == null) {
                    tfTitlePalette_.setText(target_.getDataDescription());
                } else {
                    valueEditor_ = target_.getPaletteCouleur().getValueEditor();
                    tfTitlePalette_.setText(target_.getPaletteCouleur().getTitre());
                }
                if (valueEditor_ != old) {
                    updatePanelData(null);
                }
                isDiscreteTarget_ = _m.isDiscrete();
                if (!isDiscreteTarget_) {
                    btFormat_.setEnabled(true);
                }
                setPlages(_m.getPaletteCouleur());
                final String dataDesc = _m.getDataDescription();
                setTitre(_m.getTitle() + dataDesc == null ? CtuluLibString.EMPTY_STRING : (CtuluLibString.ESPACE + dataDesc));
                target_.addPropertyChangeListener("paletteCouleur", this);
            }
            updateDiscreteTarget();
        }
        return target_ != null;
    }

    /**
   * @param _t le nouveau titre
   */
    public void setTitre(final String _t) {
        lbPalTitle_.setText(_t);
    }

    public void valueChanged(final ListSelectionEvent _e) {
        if (!isAdjusting_ && !_e.getValueIsAdjusting()) {
            savePanel();
            final BPlageAbstract old = plageEnCours_;
            plageEnCours_ = getSelectedPlage();
            if (plageEnCours_ != old) {
                updatePanel();
            }
        }
    }
}
