package memodivx.divx;

import java.util.ArrayList;

/**
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
 * @author BIANCHERI Yann
 *
 */
public class Genre {

    /**
   	 * Contient tous les genres disponibles
   	 */
    private static ArrayList tousLesGenres;

    /**
     * Repr�sente le genre ou les genres du film
     */
    protected ArrayList monGenre;

    /**
	 * Construit un genre et initialise monGenre
	 *
	 */
    public Genre() {
        if (tousLesGenres == null) {
            initTousLesGenres();
        }
        monGenre = new ArrayList();
    }

    /**
	 * Construit un genre
	 * @param s : genre du film
	 */
    public Genre(String s) {
        this();
        addGenre(s);
    }

    /**
	 * Ajoute un film � monGenre
	 * @param genre : nouveau genre
	 */
    public void addGenre(String genre) {
        monGenre.add(genre);
        if (tousLesGenres.size() == 0) {
            tousLesGenres.add(genre);
        } else {
            int d = 0;
            int f = tousLesGenres.size();
            while (d <= f) {
                int m = (d + f) / 2;
                if (((String) tousLesGenres.get(m)).compareTo(genre) < 0) {
                    d = m + 1;
                }
                if (((String) tousLesGenres.get(m)).compareTo(genre) > 0) {
                    f = m - 1;
                } else {
                    return;
                }
            }
            if (d >= tousLesGenres.size()) {
                tousLesGenres.add(genre);
            } else {
                tousLesGenres.add(d, genre);
            }
        }
    }

    /**
	 * Retourne le genre
	 */
    public String getGenre(int i) {
        return (String) monGenre.get(i);
    }

    /**
	 * Retourne le nbr de genre du film
	 * @return nbr de genre du film
	 */
    public int size() {
        return monGenre.size();
    }

    public String toString() {
        String temp = "";
        if (size() > 0) {
            temp += getGenre(0);
        }
        for (int i = 1; i < size(); i++) temp += (" , " + getGenre(i));
        return temp;
    }

    /**
     * Retourne tous les genres
     * @return tous les genres
     */
    public static ArrayList getTousLesGenres() {
        return tousLesGenres;
    }

    /**
	 * Initialise tousLesGenres
	 *
	 */
    public void initTousLesGenres() {
        tousLesGenres = new ArrayList();
        tousLesGenres.add("Action");
        tousLesGenres.add("Animation");
        tousLesGenres.add("Aventure");
        tousLesGenres.add("Biographique");
        tousLesGenres.add("Catastrophe");
        tousLesGenres.add("Com�die");
        tousLesGenres.add("Com�die Dramatique");
        tousLesGenres.add("Com�die Musicale");
        tousLesGenres.add("Court M�trage");
        tousLesGenres.add("Dessin Anim�");
        tousLesGenres.add("Documentaire");
        tousLesGenres.add("Drame");
        tousLesGenres.add("Drame Phychologique");
        tousLesGenres.add("Epouvante");
        tousLesGenres.add("Espionnage");
        tousLesGenres.add("Fantastique");
        tousLesGenres.add("Film Musical");
        tousLesGenres.add("Grand Spectacle");
        tousLesGenres.add("Guerre");
        tousLesGenres.add("Historique");
        tousLesGenres.add("Horreur");
        tousLesGenres.add("Karat�");
        tousLesGenres.add("Manga");
        tousLesGenres.add("M�lodrame");
        tousLesGenres.add("Musical");
        tousLesGenres.add("Policier");
        tousLesGenres.add("Romance");
        tousLesGenres.add("Science Fiction");
        tousLesGenres.add("Spectacle");
        tousLesGenres.add("Suspense");
        tousLesGenres.add("T�l�film");
        tousLesGenres.add("Th��tre");
        tousLesGenres.add("Thriller");
        tousLesGenres.add("Western");
    }
}
