package org.fudaa.fudaa.sinavi;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import com.memoire.dja.DjaControl;
import com.memoire.dja.DjaLib;
import com.memoire.dja.DjaObject;
import com.memoire.dja.DjaText;
import com.memoire.dja.DjaZigZagArrow;
import org.fudaa.dodico.corba.navigation.IEcluseFluviale;

/**
 * impl�mentation de l'objet Dja pour repr�senter une �cluse sur le r�seau
 * 
 * @version $Revision: 1.9 $ $Date: 2006-09-19 15:09:00 $ by $Author: deniger $
 * @author Aline Marechalle , Franck Lejeune
 */
public class SinaviReseauEcluse extends DjaZigZagArrow {

    /**
   * Image d'une �cluse manuelle portes ferm�es.
   */
    private static final Image imageManPortesFerm_ = SinaviResource.SINAVI.getImage("sinaviimagereseaueclusemanferme");

    /**
   * Image d'une �cluse manuelle portes ouvertes en aval.
   */
    private static final Image imageManPortesOuvAval_ = SinaviResource.SINAVI.getImage("sinaviimagereseaueclusemanouvaval");

    /**
   * Image d'une �cluse manuelle portes ouvertes en amont.
   */
    private static final Image imageManPortesOuvAmont_ = SinaviResource.SINAVI.getImage("sinaviimagereseaueclusemanouvamont");

    /**
   * Image d'une �cluse automatique portes ferm�es.
   */
    private static final Image imageAutoPortesFerm_ = SinaviResource.SINAVI.getImage("sinaviimagereseauecluseautoferme");

    /**
   * Image d'une �cluse automatique portes ouvertes en aval.
   */
    private static final Image imageAutoPortesOuvAval_ = SinaviResource.SINAVI.getImage("sinaviimagereseauecluseautoouvaval");

    /**
   * Image d'une �cluse automatique portes ouvertes en amont.
   */
    private static final Image imageAutoPortesOuvAmont_ = SinaviResource.SINAVI.getImage("sinaviimagereseauecluseautoouvamont");

    /**
   * Objet m�tier �cluse fluvial.
   */
    private transient IEcluseFluviale ecluse_;

    int yimage_;

    /**
   * Coordonn�es de l'image d'une �cluse
   */
    private int wimage_;

    int himage_;

    /**
   * Cadre o� les informations concernant l'�cluses du cot� montant sont report�es pour l'animation.
   */
    private DjaText textMontEcluse_;

    /**
   * Cadre o� les informations concernant l'�cluses du cot� avalant sont report�es pour l'animation.
   */
    private DjaText textAvalEcluse_;

    /**
   * Cadre o� est affich�e le nom de l'�cluse
   */
    private DjaText nomEcluse_;

    /**
   * Rectangle � dessiner autour des informations
   */
    private Rectangle rect_;

    /**
   * Couleur du rectangle � dessine autour des informations. rouge pour automatique - vert pour manuel.
   */
    private Color coul_;

    /**
   * Position des portes d'une ecluse.
   */
    private String positionPortes_ = "F";

    /**
   * Numero d'enregistrement, la position de l'�cluse dans le tableau d'�cluses de l'�tude
   */
    private int numeroEnregistrement_;

    /**
   * SinaviReseauEcluse Constructeur sp�cifique pour YAPOD
   */
    public SinaviReseauEcluse() {
        ecluse_ = null;
        yimage_ = 0;
        wimage_ = imageManPortesFerm_.getWidth(HELPER);
        himage_ = imageManPortesFerm_.getHeight(HELPER);
        setEndType(NONE);
        setBeginType(NONE);
        setForeground(new Color(122, 220, 255));
        putProperty("epaisseur", "17");
        final String nom = "";
        nomEcluse_ = new DjaText(this, 0, nom, false, 0, 20);
        nomEcluse_.setPosition(CENTER);
        textMontEcluse_ = new DjaText(this, 0, "      \n     \n     \n      \n     ", true, 0, -50);
        textMontEcluse_.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textMontEcluse_.setPosition(CENTER);
        textMontEcluse_.setAlignment(RIGHT);
        textAvalEcluse_ = new DjaText(this, 0, "      \n     \n     \n      \n     ", true, 0, 60);
        textAvalEcluse_.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textAvalEcluse_.setPosition(CENTER);
        textAvalEcluse_.setAlignment(RIGHT);
        setTextArray(new DjaText[] { nomEcluse_, textMontEcluse_, textAvalEcluse_ });
    }

    /**
   * SinaviReseauEcluse Constructeur d'une �cluse au point de vue graphique.
   * 
   * @param _ecluse : objet m�tier �cluse
   */
    public SinaviReseauEcluse(final IEcluseFluviale _ecluse) {
        ecluse_ = _ecluse;
        yimage_ = 0;
        wimage_ = imageManPortesFerm_.getWidth(HELPER);
        himage_ = imageManPortesFerm_.getHeight(HELPER);
        setEndType(NONE);
        setBeginType(NONE);
        setForeground(new Color(122, 220, 255));
        putProperty("epaisseur", "17");
        putProperty("numeroEnregistrement", (new Integer(-1)).toString());
        nomEcluse_ = new DjaText(this, 0, ecluse_.nom(), false, 0, 20);
        nomEcluse_.setPosition(CENTER);
        textMontEcluse_ = new DjaText(this, 0, "      \n     \n     \n      \n     ", true, 0, -50);
        textMontEcluse_.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textMontEcluse_.setPosition(CENTER);
        textMontEcluse_.setAlignment(RIGHT);
        textAvalEcluse_ = new DjaText(this, 0, "      \n     \n     \n      \n     ", true, 0, 60);
        textAvalEcluse_.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textAvalEcluse_.setPosition(CENTER);
        textAvalEcluse_.setAlignment(RIGHT);
        setTextArray(new DjaText[] { nomEcluse_, textMontEcluse_, textAvalEcluse_ });
    }

    /**
   * methode appel�e avant la sauvegarde du r�seau sous forme de String Elle permet d'attribuer le num�ro
   * d'enregistrement de l'�cluse
   */
    public void beforeSaving() {
        super.beforeSaving();
        for (int indexecluse = 0; indexecluse < SinaviImplementation.ETUDE_SINAVI.reseau().ecluses().length; indexecluse++) {
            if ((SinaviImplementation.ETUDE_SINAVI.reseau().ecluses()[indexecluse]).egale(ecluse_)) {
                putProperty("numeroEnregistrement", (new Integer(indexecluse)).toString());
            }
        }
    }

    /**
   * methode appel�e apres l'ouverture d'une etude et la construction physique du r�seau Elle permet de d�finir l'�cluse
   * � partir du num�ro d'enregistrement de l'�cluse
   */
    public void afterLoading() {
        super.afterLoading();
        numeroEnregistrement_ = new Integer(getProperty("numeroEnregistrement")).intValue();
        if (numeroEnregistrement_ != -1) {
            ecluse_ = SinaviImplementation.ETUDE_SINAVI.reseau().ecluses()[numeroEnregistrement_];
        } else {
        }
    }

    public void setBeginObject(final DjaObject _begin) {
        if (_begin != null) {
            super.setBeginObject(_begin);
        }
    }

    public void setEndObject(final DjaObject _begin) {
        if (_begin != null) {
            super.setEndObject(_begin);
        }
    }

    /**
   * retourne la valeur Controls de SinaviReseauEcluse object
   * 
   * @return La valeur Controls
   */
    public DjaControl[] getControls() {
        final int x = (xbegin_ + xend_) / 2;
        final int y = (ybegin_ + yend_ - himage_) / 2 - yimage_;
        final DjaControl[] r = new DjaControl[] { new PC(this, 0, VERTICAL, x, y - 3) };
        return r;
    }

    /**
   * retourne la valeur Ecluse de SinaviReseauEcluse object
   * 
   * @return La valeur Ecluse
   */
    public IEcluseFluviale getEcluse() {
        return ecluse_;
    }

    /**
   * retourne la valeur TextMontEcluse de SinaviReseauEcluse object
   * 
   * @return La valeur TextMontEcluse
   */
    public DjaText getTextMontEcluse() {
        return textMontEcluse_;
    }

    /**
   * retourne la valeur TextAvalEcluse de SinaviReseauEcluse object
   * 
   * @return La valeur TextAvalEcluse
   */
    public DjaText getTextAvalEcluse() {
        return textAvalEcluse_;
    }

    /**
   * retourne la valeur PositionPortes de SinaviReseauEcluse object
   * 
   * @return La valeur PositionPortes
   */
    public String getPositionPortes() {
        return positionPortes_;
    }

    public boolean contains(final int _x, final int _y) {
        final int x = (xbegin_ + xend_ - wimage_) / 2;
        final int y = (ybegin_ + yend_ - himage_) / 2 - yimage_;
        return super.contains(_x, _y) || new Rectangle(x, y, wimage_, himage_).contains(_x, _y);
    }

    /**
   * Dessine un objet graphique �cluse avec ses dif�rentes propri�t�s.
   * 
   * @param g
   */
    public void paintObject(final Graphics g) {
        final int x = (xbegin_ + xend_ - wimage_) / 2;
        final int y = (ybegin_ + yend_ - himage_) / 2 - yimage_;
        setPoint(1, x + wimage_ - 1, y + himage_ / 2);
        setPoint(2, x, y + himage_ / 2);
        super.paintObject(g);
        nomEcluse_.setText(ecluse_.nom());
        if (ecluse_.automatique()) {
            if (getPositionPortes().equals("F")) {
                g.drawImage(imageAutoPortesFerm_, x, y, HELPER);
            }
            if (getPositionPortes().equals("A")) {
                g.drawImage(imageAutoPortesOuvAval_, x, y, HELPER);
            }
            if (getPositionPortes().equals("M")) {
                g.drawImage(imageAutoPortesOuvAmont_, x, y, HELPER);
            }
            coul_ = new Color(248, 0, 0);
        } else {
            if (getPositionPortes().equals("F")) {
                g.drawImage(imageManPortesFerm_, x, y, HELPER);
            }
            if (getPositionPortes().equals("A")) {
                g.drawImage(imageManPortesOuvAval_, x, y, HELPER);
            }
            if (getPositionPortes().equals("M")) {
                g.drawImage(imageManPortesOuvAmont_, x, y, HELPER);
            }
            coul_ = new Color(0, 252, 0);
        }
        SinaviAnimation.contourDjaText(g, textMontEcluse_, rect_, coul_);
        SinaviAnimation.contourDjaText(g, textAvalEcluse_, rect_, coul_);
    }

    /**
   * Ecrit les informations pour les sens montant et descendant d'une �cluse.
   * 
   * @param _str : chaine de caract�re � �crire
   * @param mont : sera = true si sens montant
   */
    public void ecrireTextEcluse(final String _str, final boolean mont) {
        if (mont) {
            textMontEcluse_.setText(_str);
        } else {
            textAvalEcluse_.setText(_str);
        }
    }

    /**
   * Affecte la valeur PositionPortes de SinaviReseauEcluse object
   * 
   * @param _pos La nouvelle valeur PositionPortes
   */
    public void setPositionPortes(final String _pos) {
        positionPortes_ = _pos;
    }

    /**
   * ....
   * 
   * @file $RCSfile: SinaviReseauEcluse.java,v $
   * @creation 17 mai 2001 Fred DENIGER
   * @statut $State: Exp $
   * @modification $Date: 2006-09-19 15:09:00 $
   * @author $Author: deniger $
   * @version $Id: SinaviReseauEcluse.java,v 1.9 2006-09-19 15:09:00 deniger Exp $
   */
    private static class PC extends DjaControl {

        /**
     * PC
     * 
     * @param _f
     * @param _p
     * @param _o
     * @param _x
     * @param _y
     */
        public PC(final DjaObject _f, final int _p, final int _o, final int _x, final int _y) {
            super(_f, _p, _o, _x, _y);
        }

        /**
     * ....
     * 
     * @param _x
     * @param _y
     */
        public void draggedTo(final int _x, final int _y) {
            final Point p = new Point(_x, _y);
            DjaLib.snap(p);
            final SinaviReseauEcluse e = (SinaviReseauEcluse) getParent();
            final int y = (e.getBeginY() + e.getEndY() - e.himage_) / 2;
            e.yimage_ = y - (p.y + 3);
        }
    }
}
