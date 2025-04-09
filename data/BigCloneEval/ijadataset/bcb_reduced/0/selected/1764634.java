package org.fudaa.fudaa.sipor.ui.draw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import com.memoire.bu.BuButton;
import com.memoire.bu.BuDialogChoice;
import org.fudaa.ctulu.CtuluLibImage;
import org.fudaa.ctulu.image.CtuluImageExport;
import org.fudaa.ctulu.image.CtuluImageProducer;
import org.fudaa.dodico.corba.sipor.SParametresGrapheTopologie;
import org.fudaa.dodico.corba.sipor.SParametresGrapheTopologies;
import org.fudaa.fudaa.commun.impl.FudaaCommonImplementation;
import org.fudaa.fudaa.ressource.FudaaResource;
import org.fudaa.fudaa.sipor.SiporImplementation;
import org.fudaa.fudaa.sipor.structures.SiporDataSimulation;

class PointPort {

    int x_;

    int y_;

    PointPort(final int _x, final int _y) {
        x_ = _x;
        y_ = _y;
    }
}

/**
 * Classe qui permet de dessinner les �l�ments saisies du port plusieurs �tapes sont necessaires avant la realisation du
 * dessin.<br>
 * 1) la saisie des donn�es doit etre finie <br>
 * 2) il faut transformer les donn�es de SiporDataSimulation en une liste d'arc, chaque extr�mit�s de l'arc �tant une
 * gare <br>
 * 3) Il faut quadriller la zone de dessin en n carr�s, avec n �tant el nombre de gares du port on r�alise alors un
 * graphe dont les sommets seront les gares, et ces gares seront bien distinctes les unes des autres puisque chaque gare
 * sera ins�r�e dans un emplacement libre "carr�" de la zone quadrill�e le poids des arcs sera le nom et le ype du
 * dessin reliant les 2 gares aux extr�mit�s: par exemple, l'arc d'extremit�s gare 1 et 2 aura pour poids "mon ecluse"
 * et le type ECLUSE donc le dessin ECLUSE <br>
 * 4) on parcours la lsite d'arcs et on dessine chaque gare
 * 
 * @author Adrien Hadoux.
 */
public class SiporDessinerPort extends JPanel implements MouseListener, MouseMotionListener, CtuluImageProducer {

    protected static final int GARE = 0;

    protected static final int CHENAL = 1;

    protected static final int ECLUSE = 2;

    protected static final int CERCLE = 3;

    protected static final int BASSIN = 4;

    protected int x_;

    protected int y_;

    protected int oldX_;

    protected int oldY_;

    protected int indiceNbEcluses_ = 2;

    /**
   * Donn�es de la simulation datatsimulation.
   */
    SiporDataSimulation donnees_;

    /**
   * le nombre de gares.
   */
    int nbGares_;

    /**
   * le compteur de gares deja ecrites.
   */
    int compteur_;

    /**
   * Tableau des coordonn�es des gares.Ce tableau fonctionne de la maniere suivante: si on veut les coordonn�es du point
   * 5 il sufit de retourner le contenu du tableau a la position 5: ce contenu est un objet de type PointPort qui
   * contient X_ et Y_ les coordonn�es recherch�es.
   */
    ArrayList tableauGare_ = new ArrayList();

    /**
   * fenetre permettant d afficher le panel de dessin du port. Cette fenetre contient un bouton de validation ainsi
   * qu'un label d affichge du nombre de gares a saisir:
   */
    SiporDessinerPortFrame fenetre_;

    /**
   * Booleen d affichage des connections. Si true alors affiche les noms des connections(ie ecluses,cheneaux...) sinon n
   * affiche que le sch�ma du port
   */
    boolean afficheConnections_ = true;

    boolean firstime = true;

    FudaaCommonImplementation application_;

    JLabel[] tableauLabel_ = new JLabel[50];

    int dernierArcSaisi = 0;

    int derniereGareSaisie = 0;

    Graphics graphique_;

    SiporDessinerPort(final SiporDataSimulation _d, final int _compteur, final SiporDessinerPortFrame _fenetre, final FudaaCommonImplementation _application) {
        this.setPreferredSize(new Dimension(1024, 768));
        donnees_ = _d;
        application_ = _application;
        fenetre_ = _fenetre;
        setLayout(new GridLayout(5, 10));
        for (int i = 0; i < 50; i++) {
            tableauLabel_[i] = new JLabel("");
            this.add(tableauLabel_[i]);
        }
        compteur_ = _compteur;
        compteur_ = -2;
        nbGares_ = this.donnees_.getListeGare_().getListeGares_().size();
        if (compteur_ >= this.nbGares_ - 1) compteur_ = nbGares_ - 1;
        this.compteur_ = -2 + this.donnees_.getParams_().grapheTopologie.nbGaresDessinnees;
        if (compteur_ > this.nbGares_) compteur_ = nbGares_;
        this.fenetre_.labelCompteur_.setText("" + compteur_ + "/" + this.nbGares_ + " gares.");
        if (compteur_ < this.nbGares_) {
            fenetre_.labelMessage1_.setText("Cliquez sur le dessin pour positionner les gares");
        } else {
            fenetre_.labelMessage1_.setText("Pour d�placer les bassins, cliquez sur les gares rattach�es. ");
            fenetre_.labelMessage2_.setText("Pour avoir plus de d�tails, cliquez sur les �l�ments. ");
        }
        this.setSize(1200, 800);
        this.addMouseListener(this);
    }

    /**
   * Methode d implementation de CtuluImageProducer afin de pouvoir exporter.
   */
    public BufferedImage produceImage(final Map _params) {
        return CtuluLibImage.produceImageForComponent(this, _params);
    }

    public BufferedImage produceImage(final int _w, final int _h, final Map _params) {
        return CtuluLibImage.produceImageForComponent(this, _w, _h, _params);
    }

    /**
   * Methode d implementation de CtuluImageProducer retourne null on ne s'en pr�occupe pas.
   */
    public Dimension getDefaultImageDimension() {
        return this.getSize();
    }

    /**
   * methode dessin du panel: dans cette m�thode , s'affichera les donn�es.
   */
    public void paint(final Graphics _g) {
        graphique_ = _g;
        paintEcranPort(_g);
        paintLegende(_g);
        if (compteur_ < 0) {
            compteur_++;
            return;
        }
        if (compteur_ > this.nbGares_) {
        } else {
            if (compteur_ < this.nbGares_) {
                fenetre_.labelMessage1_.setText("Cliquez sur le dessin pour positionner les gares");
                fenetre_.labelMessage2_.setText("");
            } else {
                fenetre_.labelMessage1_.setText("Pour d�placer les bassins, cliquez sur les gares rattach�es.");
                fenetre_.labelMessage2_.setText("Pour avoir plus de d�tails, cliquez sur les �l�ments.");
            }
            if (compteur_ < this.nbGares_) {
                this.fenetre_.labelCompteur_.setText("" + compteur_ + "/" + this.nbGares_ + " gares.");
            } else {
                this.fenetre_.labelCompteur_.setText("" + this.nbGares_ + "/" + this.nbGares_ + " gares.");
            }
            oldX_ = x_;
            oldY_ = y_;
        }
        paintPort(_g);
    }

    private void paintPort(final Graphics _g) {
        _g.setColor(Color.black);
        if (tableauGare_.size() == 1) {
            PointPort p = (PointPort) tableauGare_.get(0);
            _g.fillOval(p.x_ - 7, p.y_ - 7, 15, 15);
            _g.drawString("" + this.donnees_.getListeGare_().retournerGare(0), p.x_ + 10, p.y_ - 15);
        }
        for (int i = 0; i < this.donnees_.getParams_().grapheTopologie.nbArcs; i++) {
            final int xg1 = this.donnees_.getParams_().grapheTopologie.graphe[i].xGare1;
            final int yg1 = this.donnees_.getParams_().grapheTopologie.graphe[i].yGare1;
            final int xg2 = this.donnees_.getParams_().grapheTopologie.graphe[i].xGare2;
            final int yg2 = this.donnees_.getParams_().grapheTopologie.graphe[i].yGare2;
            paintGares(_g, i, xg1, yg1, xg2, yg2);
            _g.setColor(Color.black);
            final float xi1 = (xg1 + 10 + xg2) / 2;
            final float yi1 = (yg1 + yg2 + 10) / 2;
            final float xi2 = (xg1 + xg2 - 10) / 2;
            final float yi2 = (yg1 + yg2 - 10) / 2;
            final float xi11 = (xi1 + xg1 + 5) / 2;
            final float yi11 = (yi1 + yg1 + 5) / 2;
            final float xi21 = (xi2 + xg1 - 5) / 2;
            final float yi21 = (yi2 + yg1 - 5) / 2;
            final float xi31 = (xi11 + xi21) / 2;
            final float yi31 = (yi11 + yi21) / 2;
            final float xi12 = (xi1 + 5 + xg2) / 2;
            final float yi12 = (yi1 + yg2 + 5) / 2;
            final float xi22 = (xi2 - 5 + xg2) / 2;
            final float yi22 = (yi2 + yg2 - 5) / 2;
            final float xi32 = (xi12 + xi22) / 2;
            final float yi32 = (yi12 + yi22) / 2;
            switch(this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection) {
                case 0:
                    if (xg1 != 0 && yg1 != 0) {
                        _g.drawLine(xg1 + 5, yg1, xg1 + 25 * 3, yg1);
                        _g.drawRect(xg1 + 25 * 3, yg1 - 45, 180, 90);
                        if (afficheConnections_) {
                            _g.setColor(Color.red);
                            _g.drawString("" + this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection, xg1 + 25 * 3 + 60, yg1 - 50);
                            _g.setColor(Color.blue);
                            final int initx = xg1 + 125 / 2 + 25 + 10;
                            int inity = yg1 - 35;
                            for (int k = 0; k < this.donnees_.getlQuais_().getlQuais_().size(); k++) {
                                if (donnees_.getlQuais_().retournerQuais(k).getNomBassin_() == this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection) {
                                    _g.drawString("" + this.donnees_.getlQuais_().retournerQuais(k).getNom(), initx + 25, inity);
                                }
                                inity = inity + 10;
                            }
                        }
                    }
                    break;
                case 10:
                    if (xg1 != 0 && yg1 != 0) {
                        _g.drawLine(xg1, yg1, xg1, yg1 + 25 * 3);
                        _g.drawRect(xg1 - 90, yg1 + 25 * 3, 180, 90);
                        if (afficheConnections_) {
                            _g.setColor(Color.red);
                            final int initx = xg1 - 90 + 50;
                            int inity = yg1 + 25 * 3;
                            _g.drawString("" + this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection, initx + 10, inity - 3);
                            _g.setColor(Color.blue);
                            for (int k = 0; k < this.donnees_.getlQuais_().getlQuais_().size(); k++) {
                                if (donnees_.getlQuais_().retournerQuais(k).getNomBassin_() == this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection) {
                                    _g.drawString("" + this.donnees_.getlQuais_().retournerQuais(k).getNom(), initx, inity + 15);
                                }
                                inity = inity + 10;
                            }
                        }
                    }
                    break;
                case 1000:
                    if (xg1 != 0 && yg1 != 0) {
                        _g.drawLine(xg1, yg1, xg1, yg1 - 25 * 3);
                        _g.drawRect(xg1 - 90, yg1 - 115 - 50, 180, 90);
                        if (afficheConnections_) {
                            _g.setColor(Color.red);
                            final int initx = xg1 - 90 + 50;
                            int inity = yg1 - 100;
                            _g.drawString("" + this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection, initx + 10, inity - 15 - 25 * 2);
                            _g.setColor(Color.blue);
                            for (int k = 0; k < this.donnees_.getlQuais_().getlQuais_().size(); k++) {
                                if (donnees_.getlQuais_().retournerQuais(k).getNomBassin_() == this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection) {
                                    _g.drawString("" + this.donnees_.getlQuais_().retournerQuais(k).getNom(), initx, inity - 25 * 2);
                                }
                                inity = inity + 10;
                            }
                        }
                    }
                    break;
                case 100:
                    if (xg1 != 0 && yg1 != 0) {
                        _g.drawLine(xg1, yg1, xg1 - 25 * 3, yg1);
                        _g.drawRect(xg1 - 205 - 50, yg1 - 45, 180, 90);
                        if (afficheConnections_) {
                            _g.setColor(Color.red);
                            final int initx = xg1 - 190;
                            int inity = yg1 - 20;
                            _g.drawString("" + this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection, initx, inity - 15);
                            _g.setColor(Color.blue);
                            for (int k = 0; k < this.donnees_.getlQuais_().getlQuais_().size(); k++) {
                                if (donnees_.getlQuais_().retournerQuais(k).getNomBassin_() == this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection) {
                                    _g.drawString("" + this.donnees_.getlQuais_().retournerQuais(k).getNom(), initx, inity);
                                }
                                inity = inity + 10;
                            }
                        }
                    }
                    break;
                case 1:
                    _g.drawLine(xg1, yg1 + 5, xg2, yg2 + 5);
                    _g.drawLine(xg1, yg1 - 5, xg2, yg2 - 5);
                    break;
                case 2:
                    _g.setColor(Color.blue);
                    if (afficheConnections_) {
                        _g.drawString("" + this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection, (xg2 + xg1) / 2 + 10, (yg2 + yg1) / 2 - 15);
                    }
                    _g.drawLine((int) xi31, (int) yi31, (int) xi1, (int) yi1);
                    _g.drawLine((int) xi31, (int) yi31, (int) xi2, (int) yi2);
                    _g.drawLine((int) xi32, (int) yi32, (int) xi1, (int) yi1);
                    _g.drawLine((int) xi32, (int) yi32, (int) xi2, (int) yi2);
                    _g.drawLine((int) xi32, (int) yi32, xg2, yg2);
                    _g.drawLine((int) xi31, (int) yi31, xg1, yg1);
                    _g.setColor(Color.black);
                    break;
                case 12:
                    _g.setColor(Color.blue);
                    if (afficheConnections_) {
                        _g.drawString("" + this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection, (xg2 + xg1) / 2 + 10, (yg2 + yg1) / 2 + 35);
                    }
                    _g.drawLine((int) xi31, (int) yi31 + 15, (int) xi1, (int) yi1 + 15);
                    _g.drawLine((int) xi31, (int) yi31 + 15, (int) xi2, (int) yi2 + 15);
                    _g.drawLine((int) xi32, (int) yi32 + 15, (int) xi1, (int) yi1 + 15);
                    _g.drawLine((int) xi32, (int) yi32 + 15, (int) xi2, (int) yi2 + 15);
                    _g.drawLine((int) xi32, (int) yi32 + 15, xg2, yg2);
                    _g.drawLine((int) xi31, (int) yi31 + 15, xg1, yg1);
                    _g.setColor(Color.black);
                    break;
                case 22:
                    _g.setColor(Color.blue);
                    if (afficheConnections_) {
                        _g.drawString("" + this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection, (xg2 + xg1) / 2 + 10, (yg2 + yg1) / 2 - 45);
                    }
                    _g.drawLine((int) xi31, (int) yi31 - 15, (int) xi1, (int) yi1 - 15);
                    _g.drawLine((int) xi31, (int) yi31 - 15, (int) xi2, (int) yi2 - 15);
                    _g.drawLine((int) xi32, (int) yi32 - 15, (int) xi1, (int) yi1 - 15);
                    _g.drawLine((int) xi32, (int) yi32 - 15, (int) xi2, (int) yi2 - 15);
                    _g.drawLine((int) xi32, (int) yi32 - 15, xg2, yg2);
                    _g.drawLine((int) xi31, (int) yi31 - 15, xg1, yg1);
                    _g.setColor(Color.black);
                    break;
                case 3:
                    _g.drawLine(xg1, yg1, xg2, yg2);
                    _g.drawOval(((xg2 + xg1) / 2) - 15, ((yg2 + yg1) / 2) - 15, 30, 30);
                    break;
            }
            _g.setColor(Color.white);
            _g.fillOval(0 - 7, 0 - 7, 15, 15);
            _g.setColor(Color.black);
        }
    }

    private void paintGares(final Graphics _g, int _i, final int _xg1, final int _yg1, final int _xg2, final int _yg2) {
        _g.setColor(Color.black);
        _g.fillOval(_xg1 - 7, _yg1 - 7, 15, 15);
        _g.drawString("" + this.donnees_.getListeGare_().retournerGare(this.donnees_.getParams_().grapheTopologie.graphe[_i].numGare1), _xg1 + 10, _yg1 - 15);
        _g.drawString("" + this.donnees_.getListeGare_().retournerGare(this.donnees_.getParams_().grapheTopologie.graphe[_i].numGare2), _xg2 + 10, _yg2 - 15);
        _g.fillOval(_xg2 - 7, _yg2 - 7, 15, 15);
        if (afficheConnections_) {
            _g.setColor(Color.red);
            if (this.donnees_.getParams_().grapheTopologie.graphe[_i].typeConnection != 0 && this.donnees_.getParams_().grapheTopologie.graphe[_i].typeConnection != 10 && this.donnees_.getParams_().grapheTopologie.graphe[_i].typeConnection != 100 && this.donnees_.getParams_().grapheTopologie.graphe[_i].typeConnection != 1000 && this.donnees_.getParams_().grapheTopologie.graphe[_i].typeConnection != 2 && this.donnees_.getParams_().grapheTopologie.graphe[_i].typeConnection != 12 && this.donnees_.getParams_().grapheTopologie.graphe[_i].typeConnection != 22) {
                _g.drawString("" + this.donnees_.getParams_().grapheTopologie.graphe[_i].nomConnection, (_xg2 + _xg1) / 2 + 10, (_yg2 + _yg1) / 2 - 15);
            }
            _g.setColor(Color.black);
        }
    }

    private void findInChenal() {
        for (int i = 0; i < this.donnees_.getListeChenal_().getListeChenaux_().size(); i++) {
            final int gareAmont = this.donnees_.getListeChenal_().retournerChenal(i).getGareAmont_();
            final int gareAval = this.donnees_.getListeChenal_().retournerChenal(i).getGareAval_();
            testGareAval(i, gareAmont, gareAval);
            testGareAmont(i, gareAmont, gareAval);
        }
    }

    private void findInBassins() {
        for (int i = 0; i < this.donnees_.getListebassin_().getListeBassins_().size(); i++) {
            final int gareAmont = this.donnees_.getListebassin_().retournerBassin2(i).getGareAmont();
            if (gareAmont == compteur_) {
                this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs] = new SParametresGrapheTopologie();
                this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare1 = x_;
                this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare1 = y_;
                this.donnees_.getParams_().grapheTopologie.nbGaresDessinnees++;
                this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare1 = gareAmont;
                this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].nomConnection = this.donnees_.getListebassin_().retournerBassin(i);
                this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].typeConnection = 0;
                this.donnees_.getParams_().grapheTopologie.nbArcs++;
            }
        }
    }

    private void findInCercles() {
        for (int i = 0; i < this.donnees_.getListeCercle_().getListeCercles_().size(); i++) {
            final int gareAmont = this.donnees_.getListeCercle_().retournerCercle(i).getGareAmont_();
            final int gareAval = this.donnees_.getListeCercle_().retournerCercle(i).getGareAval_();
            if (gareAmont == compteur_) {
                if (gareAval < compteur_) {
                    final int xGareArelier = ((PointPort) this.tableauGare_.get(gareAval)).x_;
                    final int yGareArelier = ((PointPort) this.tableauGare_.get(gareAval)).y_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs] = new SParametresGrapheTopologie();
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare1 = x_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare1 = y_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare2 = xGareArelier;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare2 = yGareArelier;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].typeConnection = 3;
                    this.donnees_.getParams_().grapheTopologie.nbGaresDessinnees++;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare2 = gareAval;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare1 = gareAmont;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].nomConnection = this.donnees_.getListeCercle_().retournerCercle(i).getNom_();
                    this.donnees_.getParams_().grapheTopologie.nbArcs++;
                }
            }
            if (gareAval == compteur_) {
                if (gareAmont < compteur_) {
                    final int xGareArelier = ((PointPort) this.tableauGare_.get(gareAmont)).x_;
                    final int yGareArelier = ((PointPort) this.tableauGare_.get(gareAmont)).y_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs] = new SParametresGrapheTopologie();
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare1 = x_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare1 = y_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare2 = xGareArelier;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare2 = yGareArelier;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].typeConnection = 3;
                    this.donnees_.getParams_().grapheTopologie.nbGaresDessinnees++;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare1 = gareAval;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare2 = gareAmont;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].nomConnection = this.donnees_.getListeCercle_().retournerCercle(i).getNom_();
                    this.donnees_.getParams_().grapheTopologie.nbArcs++;
                }
            }
        }
    }

    private void testGareAmont(int _i, final int _gareAmont, final int _gareAval) {
        if (_gareAmont < this.tableauGare_.size()) {
            if (_gareAval == compteur_) {
                if (_gareAmont < compteur_) {
                    final int xGareArelier = ((PointPort) this.tableauGare_.get(_gareAmont)).x_;
                    final int yGareArelier = ((PointPort) this.tableauGare_.get(_gareAmont)).y_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs] = new SParametresGrapheTopologie();
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare1 = x_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare1 = y_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare2 = xGareArelier;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare2 = yGareArelier;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].typeConnection = 1;
                    this.donnees_.getParams_().grapheTopologie.nbGaresDessinnees++;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare1 = _gareAval;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare2 = _gareAmont;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].nomConnection = this.donnees_.getListeChenal_().retournerChenal(_i).getNom_();
                    this.donnees_.getParams_().grapheTopologie.nbArcs++;
                }
            }
        }
    }

    private void testGareAval(int _i, final int _gareAmont, final int _gareAval) {
        if (_gareAval < this.tableauGare_.size()) {
            if (_gareAmont == compteur_) {
                if (_gareAval < compteur_) {
                    final int xGareArelier = ((PointPort) this.tableauGare_.get(_gareAval)).x_;
                    final int yGareArelier = ((PointPort) this.tableauGare_.get(_gareAval)).y_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs] = new SParametresGrapheTopologie();
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare1 = x_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare1 = y_;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare2 = xGareArelier;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare2 = yGareArelier;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].typeConnection = 1;
                    this.donnees_.getParams_().grapheTopologie.nbGaresDessinnees++;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare2 = _gareAval;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare1 = _gareAmont;
                    this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].nomConnection = this.donnees_.getListeChenal_().retournerChenal(_i).getNom_();
                    this.donnees_.getParams_().grapheTopologie.nbArcs++;
                }
            }
        }
    }

    private void findInEcluses() {
        for (int i = 0; i < this.donnees_.getListeEcluse_().getListeEcluses_().size(); i++) {
            final int gareAmont = this.donnees_.getListeEcluse_().retournerEcluse(i).getGareAmont_();
            final int gareAval = this.donnees_.getListeEcluse_().retournerEcluse(i).getGareAval_();
            if (gareAval < this.tableauGare_.size()) {
                if (gareAmont == compteur_) {
                    if (gareAval < compteur_) {
                        final int xGareArelier = ((PointPort) this.tableauGare_.get(gareAval)).x_;
                        final int yGareArelier = ((PointPort) this.tableauGare_.get(gareAval)).y_;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs] = new SParametresGrapheTopologie();
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare1 = x_;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare1 = y_;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare2 = xGareArelier;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare2 = yGareArelier;
                        this.donnees_.getParams_().grapheTopologie.nbGaresDessinnees++;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare2 = gareAval;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare1 = gareAmont;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].nomConnection = this.donnees_.getListeEcluse_().retournerEcluse(i).getNom_();
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].typeConnection = nombreEcluses2arcsIdentiques(this.donnees_.getParams_().grapheTopologie.nbArcs);
                        this.donnees_.getParams_().grapheTopologie.nbArcs++;
                    }
                }
            }
            if (gareAmont < this.tableauGare_.size()) {
                if (gareAval == compteur_) {
                    if (gareAmont < compteur_) {
                        final int xGareArelier = ((PointPort) this.tableauGare_.get(gareAmont)).x_;
                        final int yGareArelier = ((PointPort) this.tableauGare_.get(gareAmont)).y_;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs] = new SParametresGrapheTopologie();
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare1 = x_;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare1 = y_;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].xGare2 = xGareArelier;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].yGare2 = yGareArelier;
                        this.donnees_.getParams_().grapheTopologie.nbGaresDessinnees++;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare1 = gareAval;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].numGare2 = gareAmont;
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].typeConnection = nombreEcluses2arcsIdentiques(this.donnees_.getParams_().grapheTopologie.nbArcs);
                        this.donnees_.getParams_().grapheTopologie.graphe[this.donnees_.getParams_().grapheTopologie.nbArcs].nomConnection = this.donnees_.getListeEcluse_().retournerEcluse(i).getNom_();
                        this.donnees_.getParams_().grapheTopologie.nbArcs++;
                    }
                }
            }
        }
    }

    private void paintLegende(final Graphics _g) {
        final int legx = 15;
        final int legy = 15;
        _g.setColor(Color.black);
        _g.drawRect(legx, legy, 120, 133);
        _g.fillOval(legx + 30, legy + 10, 15, 15);
        _g.drawString("Gare", legx + 65, legy + 22);
        _g.drawLine(legx + 7, legy + 30, legx + 60, legy + 30);
        _g.drawLine(legx + 7, legy + 40, legx + 60, legy + 40);
        _g.drawString("chenal", legx + 65, legy + 37);
        _g.drawLine(legx + 7, legy + 60, legx + 60, legy + 60);
        _g.drawOval(legx + 67 / 2 - 12, legy + 60 - 12, 24, 24);
        _g.drawString("cercle", legx + 65, legy + 63);
        _g.drawRect(legx + 7, legy + 75, 53, 20);
        _g.drawString("bassin", legx + 65, legy + 87);
        _g.drawLine(legx + 7, legy + 120, (legx + (67 / 2 + 7) / 2), legy + 120);
        _g.drawLine(legx + (67 / 2 + 60) / 2, legy + 120, legx + 60, legy + 120);
        _g.drawLine((legx + (67 / 2 + 7) / 2), legy + 120, legx + 67 / 2, legy + 110);
        _g.drawLine((legx + (67 / 2 + 7) / 2), legy + 120, legx + 67 / 2, legy + 130);
        _g.drawLine((legx + 67 / 2), legy + 110, legx + (67 / 2 + 60) / 2, legy + 120);
        _g.drawLine((legx + 67 / 2), legy + 130, legx + (67 / 2 + 60) / 2, legy + 120);
        _g.drawString("ecluse", legx + 65, legy + 123);
        _g.setColor(Color.black);
    }

    private void paintEcranPort(final Graphics _g) {
        _g.setColor(Color.black);
        _g.fillRect(0, 0, 1600, 1600);
        _g.setColor(Color.white);
        _g.fillRect(0, 0, 1600, 1600);
        _g.setColor(Color.black);
        _g.setColor(Color.white);
        _g.fillRect(20, 20, 1600, 1600);
        _g.setColor(Color.black);
    }

    /**
   * Methode qui permet d'exporter le graphique en image.
   */
    void exportation() {
        CtuluImageExport.exportImageFor(this.getApplication_(), this);
    }

    /**
   * Methode de quadrillage et de dessin des gares cette methode est necessaire , comme une initialisation pour le
   * dessin du port.
   */
    void quadrillage() {
    }

    public void mouseClicked(final MouseEvent _ev) {
        if (this.donnees_.getParams_().grapheTopologie.nbArcs != 0) dernierArcSaisi = this.donnees_.getParams_().grapheTopologie.nbArcs - 1; else dernierArcSaisi = 0;
        if (!tableauGare_.isEmpty()) derniereGareSaisie = tableauGare_.size() - 1; else derniereGareSaisie = 0;
        final int x = _ev.getX();
        final int y = _ev.getY();
        x_ = x;
        y_ = y;
        if (compteur_ < this.nbGares_) {
            gestionClicGare();
        } else {
            gestionClicDetailElement();
        }
        afficher_graphe();
    }

    /**
   * Methode qui gere les actions consequence du clic de l'utilsiateur pour positionner une gare.
   */
    private void gestionClicGare() {
        if (x_ != oldX_ || y_ != oldY_) {
            graphique_.fillOval(x_ - 7, y_ - 7, 15, 15);
            graphique_.drawString("" + this.donnees_.getListeGare_().retournerGare(compteur_), x_ + 10, y_ - 15);
        }
        if (x_ != 0 || y_ != 0) {
            this.tableauGare_.add(new PointPort(x_, y_));
        }
        findInEcluses();
        findInChenal();
        findInCercles();
        findInBassins();
        if (x_ != oldX_ || y_ != oldY_) {
            compteur_++;
        }
        repaint();
        this.fenetre_.enregistrerAction();
    }

    /**
   * Methode qui recherche l'element a partir du clic de l'utilisateur
   * et construit une interface d'affichage.
   * Recherche par position relative.
   */
    private void gestionClicDetailElement() {
        for (int i = 0; i < this.donnees_.getParams_().grapheTopologie.nbArcs; i++) {
            final int xg1 = this.donnees_.getParams_().grapheTopologie.graphe[i].xGare1;
            final int yg1 = this.donnees_.getParams_().grapheTopologie.graphe[i].yGare1;
            final int xg2 = this.donnees_.getParams_().grapheTopologie.graphe[i].xGare2;
            final int yg2 = this.donnees_.getParams_().grapheTopologie.graphe[i].yGare2;
            if (this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection == 0 || this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection == 10 || this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection == 100 || this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection == 1000) {
                System.out.println("zone de recherche du clic: " + Math.sqrt((xg1 + x_) * (xg1 + x_) + (yg1 + y_) * (yg1 + y_)));
                if (Math.sqrt((xg1 - x_) * (xg1 - x_) + (yg1 - y_) * (yg1 - y_)) <= 10) {
                    final int rattachement = positionBassin(this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection);
                    this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection = rattachement;
                    repaint();
                    this.fenetre_.enregistrerAction();
                }
            } else {
                int d1, d2;
                if (yg1 < yg2) {
                    d2 = yg2;
                    d1 = yg1;
                } else {
                    d2 = yg1;
                    d1 = yg2;
                }
                int d3, d4;
                if (xg1 < xg2) {
                    d3 = xg1;
                    d4 = xg2;
                } else {
                    d3 = xg2;
                    d4 = xg1;
                }
                if (y_ > d1 && y_ < d2 && x_ > d3 && x_ < d4 && this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection != 0 && this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection != 10 && this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection != 100 && this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection != 1000) {
                    creationInterfaceAffichage(this.donnees_.getParams_().grapheTopologie.graphe[i].nomConnection, this.donnees_.getParams_().grapheTopologie.graphe[i].typeConnection, x_, y_, i);
                }
            }
        }
    }

    /**
   * methode ermetant de determiner l indice d emplacement du tooltiptext destin� a aficher els donn�es de l element.
   * 
   * @return
   */
    int choisirLabel(final int _x, final int _y) {
        for (int k = 0; k < 50; k++) {
            final int xl = this.tableauLabel_[k].getLocation().x;
            final int yl = this.tableauLabel_[k].getLocation().y;
            if (_y > yl && _y < yl + 128 && _x > xl && _x < xl + 80) {
                return k;
            }
        }
        return 0;
    }

    public void mouseDragged(final MouseEvent _ev) {
    }

    public void mouseEntered(final MouseEvent _ev) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void mouseExited(final MouseEvent _ev) {
    }

    public void mousePressed(final MouseEvent _ev) {
    }

    public void mouseMoved(final MouseEvent _ev) {
    }

    public void mouseReleased(final MouseEvent _ev) {
    }

    /**
   * methode qui retourne la configuration du bassin (choix par l utilisateur).
   * 
   * @return
   */
    int positionBassin(final String _nom) {
        final String[] values = { "haut", "bas", "droite", "gauche" };
        final BuDialogChoice choix = new BuDialogChoice(donnees_.getApplication().getApp(), SiporImplementation.INFORMATION_SOFT, "position du bassin par rapport � la gare", "rattachement du bassin " + _nom + " � la gare par:", values);
        final int reponse = choix.activate();
        int rattachement = 0;
        if (reponse == 0) {
            if (choix.getValue().equals("haut")) {
                rattachement = 10;
            } else if (choix.getValue().equals("bas")) {
                rattachement = 1000;
            } else if (choix.getValue().equals("droite")) {
                rattachement = 100;
            }
        }
        return rattachement;
    }

    /**
   * methode permettant de retouner l indice de l element a modifier.
   */
    int indiceElement(final String _nomElement, final int _typeConnection) {
        int indiceElement = -1;
        if (_typeConnection == 1) {
            indiceElement = this.donnees_.getListeChenal_().retourneIndice(_nomElement);
        } else if (_typeConnection == 2) {
            indiceElement = this.donnees_.getListeEcluse_().retourneIndice(_nomElement);
        } else if (_typeConnection == 3) {
            indiceElement = this.donnees_.getListeCercle_().retourneIndice(_nomElement);
        }
        return indiceElement;
    }

    /**
   * Methode qui g�n�re une interface comportant les informations de l element selectionn�.
   */
    void creationInterfaceAffichage(final String _nomElement, final int _connection, final int _x, final int _y, final int _arc) {
        final int indiceArc = _arc;
        final int typeConnection = _connection;
        final JInternalFrame fenetreAffichage = new JInternalFrame("", true, true, true, true);
        fenetreAffichage.setLocation(_x, _y);
        final int indiceElement = indiceElement(_nomElement, _connection);
        String[] contenuElement = { "" };
        if (typeConnection == 1) {
            if (indiceElement == -1) {
                return;
            }
            contenuElement = this.donnees_.getListeChenal_().retournerChenal(indiceElement).affichage();
            fenetreAffichage.setSize(200, 150);
        } else if (typeConnection == 2) {
            if (indiceElement == -1) {
                return;
            }
            contenuElement = this.donnees_.getListeEcluse_().retournerEcluse(indiceElement).affichage();
            fenetreAffichage.setSize(200, 196);
        } else if (typeConnection == 3) {
            if (indiceElement == -1) {
                return;
            }
            contenuElement = this.donnees_.getListeCercle_().retournerCercle(indiceElement).affichage();
            fenetreAffichage.setSize(200, 150);
        }
        final JPanel affichagedonnees = new JPanel();
        final JPanel controlPanel = new JPanel();
        final BuButton modification = new BuButton("modif", FudaaResource.FUDAA.getIcon("crystal_maj"));
        final BuButton quitter = new BuButton("", FudaaResource.FUDAA.getIcon("crystal_quitter"));
        final JScrollPane asc = new JScrollPane(affichagedonnees);
        fenetreAffichage.setLayout(new BorderLayout());
        fenetreAffichage.getContentPane().add(asc, BorderLayout.CENTER);
        affichagedonnees.setLayout(new GridLayout(contenuElement.length, 1));
        for (int k = 0; k < contenuElement.length; k++) {
            affichagedonnees.add(new JLabel(contenuElement[k]));
        }
        controlPanel.add(quitter);
        controlPanel.add(modification);
        fenetreAffichage.getContentPane().add(controlPanel, BorderLayout.SOUTH);
        quitter.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent _e) {
                if (typeConnection == 1) {
                    donnees_.getParams_().grapheTopologie.graphe[indiceArc].nomConnection = donnees_.getListeChenal_().retournerChenal(indiceElement).getNom_();
                } else if (typeConnection == 2) {
                    donnees_.getParams_().grapheTopologie.graphe[indiceArc].nomConnection = donnees_.getListeEcluse_().retournerEcluse(indiceElement).getNom_();
                } else if (typeConnection == 3) {
                    donnees_.getParams_().grapheTopologie.graphe[indiceArc].nomConnection = donnees_.getListeCercle_().retournerCercle(indiceElement).getNom_();
                }
                repaint();
                fenetreAffichage.dispose();
            }
        });
        modification.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent _e) {
                if (typeConnection == 1) {
                    donnees_.getApplication().parametreChenal();
                    donnees_.getApplication().getGestionChenaux_().getPile_().last(donnees_.getApplication().getGestionChenaux_().principalPanel_);
                    donnees_.getApplication().getGestionChenaux_().setTitle("Modification d'un chenal");
                    donnees_.getApplication().getGestionChenaux_().validate();
                    donnees_.getApplication().getGestionChenaux_().SaisieChenalPanel_.MODE_MODIFICATION(indiceElement);
                } else if (typeConnection == 2) {
                    donnees_.getApplication().parametreEcluse();
                    donnees_.getApplication().gestionEcluses_.pile_.last(donnees_.getApplication().gestionEcluses_.principalPanel_);
                    donnees_.getApplication().gestionEcluses_.setTitle("Modification d'une ecluse");
                    donnees_.getApplication().gestionEcluses_.validate();
                    donnees_.getApplication().gestionEcluses_.SaisieEclusePanel_.MODE_MODIFICATION(indiceElement);
                } else if (typeConnection == 3) {
                }
                quitter.doClick();
            }
        });
        fenetreAffichage.setTitle(_nomElement);
        fenetreAffichage.setVisible(true);
        this.getApplication_().addInternalFrame(fenetreAffichage);
    }

    /**
   * methode qui retourne l 'indice de coloriage de l ecluse dans le cas ou il y en a plusieur sinon retourne 2 retourne
   * 12 si 2 ecluses et retourne 22 si 3 ecluses.
   * 
   * @param _arcEcluse
   * @return
   */
    int nombreEcluses2arcsIdentiques(final int _arcEcluse) {
        int compteurEcluses = 0;
        final int gare1 = this.donnees_.getParams_().grapheTopologie.graphe[_arcEcluse].numGare1;
        final int gare2 = this.donnees_.getParams_().grapheTopologie.graphe[_arcEcluse].numGare2;
        for (int i = 0; i < this.donnees_.getParams_().grapheTopologie.nbArcs; i++) {
            if (i != _arcEcluse) {
                if ((this.donnees_.getParams_().grapheTopologie.graphe[i].numGare1 == gare1 && this.donnees_.getParams_().grapheTopologie.graphe[i].numGare2 == gare2) || (this.donnees_.getParams_().grapheTopologie.graphe[i].numGare1 == gare2 && this.donnees_.getParams_().grapheTopologie.graphe[i].numGare2 == gare1)) {
                    compteurEcluses++;
                }
            }
        }
        if (compteurEcluses == 1) {
            return 12;
        } else if (compteurEcluses == 2) {
            return 22;
        } else {
            return 2;
        }
    }

    public void afficher_graphe() {
        SParametresGrapheTopologies graphe = donnees_.getParams_().grapheTopologie;
        System.out.println("********************************\n");
        for (int i = 0; i < graphe.nbArcs; i++) {
            System.out.println("details Arc " + i + ": connection:" + graphe.graphe[i].nomConnection + " gareA: " + graphe.graphe[i].numGare1 + " gareB: " + graphe.graphe[i].numGare2);
        }
    }

    public FudaaCommonImplementation getApplication_() {
        return application_;
    }

    public void setApplication_(FudaaCommonImplementation application_) {
        this.application_ = application_;
    }
}
