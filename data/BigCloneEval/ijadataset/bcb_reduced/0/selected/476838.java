package memodivx.gui;

import memodivx.gui.image.Img;
import memodivx.memolution.DataRecord;
import memodivx.memolution.Database;
import memodivx.memolution.MEMOexception;
import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 *	Cette �l�ment graphique permet � l'utilisateur d'exporter une s�lection de film
 * 	sous forme de ZIP
 *
 * Memodivx helps you managing your film database.
 * Copyright (C) 2004  Yann Biancheri, Thomas Rollinger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * @author Rollinger Thomas
 */
public class ExportationView extends JFrame {

    /**
	 * La base de donn�e dans laquelle �a se passe
	 */
    private Database data;

    /**
	 * JList contenant tous les films de la database
	 */
    private JList allFilms;

    /**
	 * JList contenant les films selectionn�s
	 */
    private JList filmsChoisis;

    /**
	 * Permet de remplir la JList des films selectionn�s
	 */
    private Vector filmsSelect;

    /**
	 * nb de film selectionn�e
	 */
    private int nbFilm;

    /**
	 * Construit la fen�tre permettant les exportations
	 * @param View : La fen�tre dans lequel �a se passe
	 */
    public ExportationView(Database data) {
        this.data = data;
        Font font = new Font("Arial", 16, 16);
        filmsSelect = new Vector();
        filmsChoisis = new JList();
        filmsChoisis.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allFilms = new JList(this.data.getDatabase().toArray());
        JLabel lFilm = new JLabel("Liste des films :");
        JLabel lFilmChoisi = new JLabel("Films choisis :");
        JButton choisir = new JButton(new ImageIcon(Img.class.getResource("droite.gif")));
        JButton retirer = new JButton(new ImageIcon(Img.class.getResource("gauche.gif")));
        JButton exporter = new JButton("Exporter");
        JButton quitter = new JButton("Quitter");
        allFilms.setSelectedIndex(-1);
        filmsChoisis.setSelectedIndex(-1);
        allFilms.setFont(font);
        filmsChoisis.setFont(font);
        lFilmChoisi.setFont(font);
        lFilm.setFont(font);
        filmsChoisis.setFixedCellHeight(25);
        allFilms.setFixedCellHeight(25);
        filmsChoisis.setFixedCellWidth(175);
        allFilms.setFixedCellWidth(175);
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints contraintes = new GridBagConstraints();
        contraintes.fill = GridBagConstraints.HORIZONTAL;
        modifContraintes(contraintes, 1, 0, 1, 1, 0, 0);
        getContentPane().add(lFilm, contraintes);
        modifContraintes(contraintes, 4, 0, 1, 1, 0, 0);
        getContentPane().add(lFilmChoisi, contraintes);
        modifContraintes(contraintes, 0, 1, 1, 1, 1, 0);
        getContentPane().add(new JLabel(" "), contraintes);
        modifContraintes(contraintes, 1, 1, 1, 7, 0, 0);
        getContentPane().add(allFilms, contraintes);
        getContentPane().add(new JScrollPane(allFilms), contraintes);
        modifContraintes(contraintes, 2, 2, 1, 1, 1, 0);
        getContentPane().add(new JLabel(" "), contraintes);
        modifContraintes(contraintes, 2, 3, 1, 1, 0, 0);
        getContentPane().add(choisir, contraintes);
        modifContraintes(contraintes, 2, 4, 1, 2, 1, 0);
        getContentPane().add(new JLabel(" "), contraintes);
        modifContraintes(contraintes, 2, 6, 1, 1, 1, 0);
        getContentPane().add(new JLabel(" "), contraintes);
        modifContraintes(contraintes, 2, 7, 1, 1, 0, 0);
        getContentPane().add(retirer, contraintes);
        modifContraintes(contraintes, 3, 1, 3, 7, 0, 0);
        getContentPane().add(filmsChoisis, contraintes);
        getContentPane().add(new JScrollPane(filmsChoisis), contraintes);
        modifContraintes(contraintes, 6, 1, 2, 1, 1, 0);
        getContentPane().add(new JLabel(" "), contraintes);
        contraintes.fill = GridBagConstraints.NONE;
        modifContraintes(contraintes, 4, 8, 1, 1, 0, 0);
        getContentPane().add(exporter, contraintes);
        modifContraintes(contraintes, 5, 8, 1, 1, 0, 0);
        getContentPane().add(quitter, contraintes);
        choisir.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (allFilms.getSelectedIndex() != -1) {
                    Object[] selectedValues = allFilms.getSelectedValues();
                    for (int i = 0; i < selectedValues.length; i++) {
                        filmsSelect.add(selectedValues[i]);
                        nbFilm++;
                    }
                    filmsChoisis.setListData(filmsSelect);
                    allFilms.setSelectedIndex(-1);
                    filmsChoisis.setSelectedIndex(-1);
                    filmsChoisis.repaint();
                    allFilms.repaint();
                }
            }
        });
        retirer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                filmsSelect.remove(filmsChoisis.getSelectedIndex());
                filmsChoisis.setListData(filmsSelect);
                filmsChoisis.setSelectedIndex(-1);
            }
        });
        quitter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        exporter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JFileChooser choix = new JFileChooser();
                choix.setFileFilter(new MyFileFilter("zip"));
                choix.setFileSelectionMode(JFileChooser.FILES_ONLY);
                choix.showSaveDialog(choix);
                File file = choix.getSelectedFile();
                if (file != null) {
                    Database e = new Database();
                    for (int i = 0; i < filmsSelect.size(); i++) {
                        try {
                            e.add(((DataRecord) filmsSelect.get(i)).getData());
                        } catch (MEMOexception me) {
                            me.printStackTrace();
                        }
                    }
                    File f = new File("Base.xml");
                    e.saveAs(f);
                    try {
                        ajouterAuZip(file.getAbsolutePath() + ".zip", filmsSelect, nbFilm);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    f.delete();
                    dispose();
                }
            }
        });
        setTitle("Exportation des Films");
        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public static void modifContraintes(GridBagConstraints contraintes, int x, int y, int width, int height, int weightx, int weighty) {
        contraintes.gridx = x;
        contraintes.gridy = y;
        contraintes.gridwidth = width;
        contraintes.gridheight = height;
        contraintes.weightx = weightx;
        contraintes.weighty = weighty;
        contraintes.anchor = GridBagConstraints.EAST;
    }

    /**
	 * Permet d'exporter sous forme de zip
	 * @param nomFile : nom du fichier zip
	 * @param nomFilms : vector contenant le nom des films � ajouter<br>
	 * Nous sert pour savoir qu'elle sont les photos � mettre
	 * @param nbFilm
	 * @throws FileNotFoundException
	 */
    public void ajouterAuZip(String nomFile, Vector nomFilms, int nbFilm) throws FileNotFoundException {
        FileOutputStream fichierZip = new FileOutputStream(nomFile);
        ZipOutputStream zin = new ZipOutputStream(fichierZip);
        try {
            ZipEntry base = new ZipEntry("Base.xml");
            zin.putNextEntry(base);
            ZipEntry[] photo = new ZipEntry[nbFilm];
            zin.setLevel(9);
            byte[] buffer = new byte[512 * 1024];
            int nbLecture;
            java.io.FileInputStream sourceFile = new java.io.FileInputStream(new File("Base.xml"));
            while ((nbLecture = sourceFile.read(buffer)) != -1) {
                zin.write(buffer, 0, nbLecture);
            }
            zin.closeEntry();
            base = new ZipEntry("film.xsl");
            zin.putNextEntry(base);
            nbLecture = 0;
            sourceFile = new java.io.FileInputStream(new File("film.xsl"));
            while ((nbLecture = sourceFile.read(buffer)) != -1) {
                zin.write(buffer, 0, nbLecture);
            }
            zin.closeEntry();
            int i = 0;
            while (i < nbFilm) {
                File image = new File("image/" + nomFilms.get(i).toString() + ".jpg");
                if (image.exists()) {
                    java.io.FileInputStream photoFile = new java.io.FileInputStream(image);
                    photo[i] = new ZipEntry("image/" + nomFilms.get(i).toString() + ".jpg");
                    zin.putNextEntry(photo[i]);
                    while ((nbLecture = photoFile.read(buffer)) != -1) {
                        zin.write(buffer, 0, nbLecture);
                    }
                    zin.closeEntry();
                    photoFile.close();
                }
                i++;
            }
        } catch (java.io.FileNotFoundException fe) {
        } catch (java.io.IOException e) {
        } finally {
            try {
                zin.close();
            } catch (Exception e) {
            }
        }
    }

    class MyFileFilter extends FileFilter {

        String type;

        public MyFileFilter(String type) {
            this.type = type;
        }

        public boolean accept(File f) {
            String file = " ";
            try {
                file = f.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return f.isDirectory() || ((file.length() > 3) && file.substring(file.length() - 3, file.length()).toLowerCase().equals(type));
        }

        public String getDescription() {
            return type;
        }
    }

    public class Element {

        public int numInList;

        public String titre;

        public Element(String titre, int numInList) {
            this.numInList = numInList;
            this.titre = titre;
        }

        public String toString() {
            return titre;
        }
    }
}
