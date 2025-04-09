package formula.display;

import java.awt.*;
import java.awt.geom.*;

/**
Klasse mit Methoden zur Ausrichtung von Objekten in Tabellen.
Copyright (C) 2006, Rene Heuer, Salingtwiete 4g. (D)20535 Hamburg
eMail: heuer@exmpl.de
Dieses Programm ist freie Software. Sie können es unter den Bedingungen der
GNU General Public License, wie von der Free Software Foundation veröffentlicht,
weitergeben und/oder modifizieren, entweder gemäß Version 2 der Lizenz oder
(nach Ihrer Option) jeder späteren Version. 
Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von
Nutzen sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite
Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK.
Details finden Sie in der GNU General Public License. 
Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem
Programm erhalten haben. Falls nicht, schreiben Sie an die 
Free Software Foundation, Inc.,
59 Temple Place, 
Suite 330, 
Boston, MA 02111-1307, 
USA.
*/
public class Align {

    /**
Kassenvariable zur Kodierung zentrierter Ausrichtung (horizontal und vertikal).
 */
    public static final int CENTER = 0;

    /**
Kassenvariable zur Kodierung linksbündiger Ausrichtung.
 */
    public static final int LEFT = 1;

    /**
Kassenvariable zur Kodierung rechtsbündiger Ausrichtung.
 */
    public static final int RIGHT = 2;

    /**
Kassenvariable zur Kodierung der Ausrichtung an oberer Seite des umgebenden Rechecks.
 */
    public static final int TOP = 3;

    /**
Kassenvariable zur Kodierung der Ausrichtung an unterer Seite des umgebenden Rechecks.
 */
    public static final int BOTTOM = 4;

    /**
 Zeilenvektor einer Matrix übergeben.
 Der Vektor ist eine Matrix mit nur einer Spalte und muss streng vom Tupel (einfaches Array) unterschieden werden.
 @param r Index der Zeile.
 @param a Matrix als (Rectangle2D)Array[][]
 @return v der Zeilenvektor als (Rectangle2D)Array[][]
 @see #toTupel(Rectangle2D[][])
 */
    public static Rectangle2D[][] getRow(int r, Rectangle2D[][] a) {
        int n = a[r].length;
        Rectangle2D[][] v = new Rectangle2D[1][n];
        for (int i = 0; i < n; i++) v[0][i] = a[r][i];
        return v;
    }

    /**
 Bestimmte Zeile einer Matrix mit Vector belegen.
 Anzahl der Zeilenelemente der Matrix muss der Elementanzahl des Vektors entsprechen.
 @param r Index der Zeile.
 @param a Matrix deren Zeile ersetzt wird.
 @param v Vektor der die Matrixzeile ersetzt.
 */
    public static void setRow(int r, Rectangle2D[][] a, Rectangle2D[][] v) {
        int n = a[r].length;
        for (int i = 0; i < n; i++) a[r][i] = v[i][0];
    }

    /**
 Spaltenvektor einer Matrix übergeben.
 Der Vektor ist eine Matrix mit nur einer Spalte und muss streng vom Tupel (einfaches Array) unterschieden werden.
 * @param c the column index
 @param a Matrix als (Rectangle2D)Array[][]
 @return v der Spaltenvektor als (Rectangle2D)Array[][]
 @see #toTupel(Rectangle2D[][])
 */
    public static Rectangle2D[][] getColumn(int c, Rectangle2D[][] a) {
        int n = a.length;
        Rectangle2D[][] v = new Rectangle2D[n][1];
        for (int i = 0; i < n; i++) v[i][0] = a[i][c];
        return v;
    }

    /**
 Bestimmte Spalte einer Matrix mit Vector belegen.
 Anzahl der Spaltenelemente der Matrix muss der Elementanzahl des Vektors entsprechen.
 @param c Index der Spalte.
 @param a Matrix deren Spalte ersetzt wird.
 @param v Vektor der die Matrixspalte ersetzt.
 */
    public static void setColumn(int c, Rectangle2D[][] a, Rectangle2D[][] v) {
        int n = a.length;
        for (int i = 0; i < n; i++) a[i][c] = v[i][0];
    }

    /**
 Transponieren einer Matrix.
 @param a zu transponierende Matrix als (Rectangle2D)Array[][]
 @return t die transponierte Matrix als (Rectangle2D)Array[][]
 */
    public static Rectangle2D[][] transpose(Rectangle2D[][] a) {
        int m = a.length;
        int n = a[0].length;
        Rectangle2D[][] t = new Rectangle2D[n][m];
        for (int i = 0; i < m; i++) for (int j = 0; j < n; j++) t[j][i] = a[i][j];
        return t;
    }

    /**
 Zeilen- oder Spaltenvektor als Tupel übergeben
 @param v Vektor als einzeilige oder einspaltige Matrix.
 @return t den Tupel als (Rectangle2D)Array[]
 */
    public static Rectangle2D[] toTupel(Rectangle2D[][] v) {
        boolean row = (v.length >= v[0].length);
        int n = row ? v.length : v[0].length;
        Rectangle2D[] t = new Rectangle2D[n];
        for (int i = 0; i < n; i++) t[i] = row ? v[i][0] : v[0][i];
        return t;
    }

    /**
 Tupel in Vektor wandeln.
 @param t zu wandelnder Tupel.
 @return v: Vektor des Tupels als (Rectangle2D)Array[][].
 */
    public static Rectangle2D[][] toVector(Rectangle2D[] t) {
        Rectangle2D[][] v = new Rectangle2D[t.length][1];
        for (int i = 0; i < t.length; i++) v[i][0] = t[i];
        return v;
    }

    /**
 Neues, translatiertes Shape übergeben.
 @param shape Das zu verschiebende Shape.
 @param xpos Neue horizontale Position des Shapes.
 @param ypos Neue vertikale Position des Shapes.
 * @return the translated Shape
 */
    public static Shape translate(Shape shape, double xpos, double ypos) {
        AffineTransform trans = new AffineTransform();
        trans.setToTranslation(xpos, ypos);
        return trans.createTransformedShape(shape);
    }

    /**
 Maximale Höhe eines Vektors übergeben.
 @param vec Vector mit Rechtecken.
 @return max Maximale vertikale Ausdehnung der Rechtecke.
 */
    public static double getMaxHeight(Rectangle2D[] vec) {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < vec.length; i++) max = Math.max(max, vec[i].getHeight());
        return max;
    }

    /**
 Maximale Breite eines Vektorelements übergeben.
 @param vec Vector mit Rechtecken.
 @return max Maximale horizontale Ausdehnung der Rechtecke.
 */
    public static double getMaxWidth(Rectangle2D[] vec) {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < vec.length; i++) max = Math.max(max, vec[i].getWidth());
        return max;
    }

    /**
 Zellen einer Spalte auf einheitliche vertikale Position bringen.
 @param vec Vektor mit zu verschiebenden Zellen.
 @param pos Gemeinsame, vertikale Position.
 @return vec Der verschobene Vektor.
 */
    public static Rectangle2D[] setRowPos(Rectangle2D[] vec, double pos) {
        for (int j = 0; j < vec.length; j++) vec[j] = translate(vec[j], 0, -vec[j].getY() + pos).getBounds2D();
        return vec;
    }

    /**
 Zellen innerhalb der maximalen Zeilenhöhe ausrichten.
 @param vec Zeilenvektor mit auzurichtenden Zellen.
 @param max Maximale Höhe, innerhalb der die Ausrichtung stattfindet.
 @param how Art der Ausrichtung.
 @return den übergebenen, ausgerichteten Zeilenvektor.
 */
    public static Rectangle2D[] horizontal(Rectangle2D[] vec, double max, int how) {
        double loc = 0;
        for (int j = 0; j < vec.length; j++) {
            switch(how) {
                case CENTER:
                    loc = (max - vec[j].getHeight()) / 2.0;
                    break;
                case BOTTOM:
                    loc = max - vec[j].getHeight();
                    break;
                case TOP:
                    loc = 0;
            }
            vec[j] = translate(vec[j], 0, loc).getBounds2D();
        }
        return vec;
    }

    /**
 Alle Zellen einer Matrix vertikal ausrichten.
 @param tab Matrix mit auszurichtenden Elementen.
 @param how Art der Ausrichtung.
 @param rowsp Minimaler Abstand zwischen den ausgerichteten Zeilen.
 @return die übergebene, ausgerichtete Tabelle.
 */
    public static Rectangle2D[][] row(Rectangle2D[][] tab, int how, double rowsp) {
        double rowpos = 0;
        double height = 0;
        for (int i = 0; i < tab.length; i++) {
            Rectangle2D[] vec = toTupel(getRow(i, tab));
            height = getMaxHeight(vec);
            setRow(i, tab, toVector(horizontal(setRowPos(vec, rowpos), height, how)));
            rowpos += height + rowsp;
        }
        return tab;
    }

    /**
 Zellen einer Spalte auf einheitliche horizontale Position bringen.
 @param vec Vektor mit zu verschiebenden Zellen.
 @param pos Gemeinsame horizontalen Position.
 @return vec Den übergebenen, verschobene Vektor.
 */
    public static Rectangle2D[] setColPos(Rectangle2D[] vec, double pos) {
        for (int j = 0; j < vec.length; j++) vec[j] = translate(vec[j], -vec[j].getX() + pos, 0).getBounds2D();
        return vec;
    }

    /**
 Zellen innerhalb der maximalen Spaltenbreite ausrichten.
 @param vec Vektor mit auzurichtenden Zellen.
 @param max Maximale Breite, innerhalb der die Ausrichtung stattfindet.
 @param how Art der Ausrichtung.
 @return den übergebenen, ausgerichteten Vektor.
 */
    public static Rectangle2D[] vertical(Rectangle2D[] vec, double max, int how) {
        double loc = 0;
        for (int j = 0; j < vec.length; j++) {
            switch(how) {
                case CENTER:
                    loc = (max - vec[j].getWidth()) / 2.0;
                    break;
                case RIGHT:
                    loc = max - vec[j].getWidth();
                    break;
                case LEFT:
                    loc = 0;
            }
            vec[j] = translate(vec[j], loc, 0).getBounds2D();
        }
        return vec;
    }

    /**
 Alle Spalten einer Matrix horizontal ausrichten.
 @param tab Matrix mit auszurichtenden Elementen.
 @param how Art der Ausrichtung.
 @param colsp Minimaler Abstand zwischen den ausgerichteten Spalten.
 @return die übergebene, ausgerichtete Tabelle
 */
    public static Rectangle2D[][] col(Rectangle2D[][] tab, int how, double colsp) {
        double colpos = 0;
        double width = 0;
        for (int i = 0; i < tab[0].length; i++) {
            Rectangle2D[] vec = toTupel(getColumn(i, tab));
            width = getMaxWidth(vec);
            setColumn(i, tab, toVector(vertical(setColPos(vec, colpos), width, how)));
            colpos += width + colsp;
        }
        return tab;
    }

    /**
 Alle Elemente innerhalb der Matrix anordnen und ausrichten.
 @param tab Matrix mit auszurichtenden Elementen.
 @param horhow Art der horizontalen Ausrichtung.
 @param rowsp Minimaler Abstand zwischen den ausgerichteten Zeilen.
 @param verhow Art der vertikalen Ausrichtung.
 @param colsp Minimaler Abstand zwischen den ausgerichteten Spalten.
 @return die übergebene Tabelle mit usgerichteten Zeilen und Spalten.
 */
    public static Rectangle2D[][] alignTable(Rectangle2D[][] tab, int horhow, double rowsp, int verhow, double colsp) {
        tab = row(tab, horhow, rowsp);
        tab = col(tab, verhow, colsp);
        return tab;
    }

    /**
 Elemente horizontal anordnen mit bestimmter Ausrichtung und bestimmtem Abstand.
 @param tup Tupel (als einfaches Array zu interprtieren) mit auszurichtenden Elementen.
 @param how Art der horizontalen Ausrichtung (CENTER, BOTTOM,  TOP).
 @param space Abstand zwischen den Elementen.
 @return Tupel mit angeordneten und ausgerichteten Eelementen.
 */
    public static Rectangle2D[] alignRow(Rectangle2D[] tup, int how, double space) {
        Rectangle2D[][] tab;
        tab = transpose(toVector(tup));
        tab = Align.alignTable(tab, how, 0, CENTER, space);
        return toTupel(Align.getRow(0, tab));
    }

    /**
 Elemente vertikal anordnen mit bestimmter Ausrichtung und bestimmtem Abstand.
 @param tup Tupel (als einfaches Array zu interprtieren) mit auszurichtenden Elementen.
 @param how Art der horizontalen Ausrichtung (CENTER, RIGHT, LEFT).
 @param space Abstand zwischen den Elementen.
 @return Tupel mit angeordneten und ausgerichteten Eelementen.
 */
    public static Rectangle2D[] alignCol(Rectangle2D[] tup, int how, double space) {
        Rectangle2D[][] tab;
        tab = toVector(tup);
        tab = Align.alignTable(tab, CENTER, space, how, 0);
        return toTupel(Align.getColumn(0, tab));
    }
}
