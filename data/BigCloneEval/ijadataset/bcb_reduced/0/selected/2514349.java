package org.fudaa.ctulu;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TLongArrayList;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import com.memoire.fu.FuEmptyArrays;

/**
 * Des methodes statiques concernant les tableaux.
 * 
 * @version $Id: CtuluLibArray.java,v 1.37 2007-06-29 15:09:35 deniger Exp $
 * @author Fred Deniger
 */
public final class CtuluLibArray {

    /**
   * @param _array le tableau a verifier
   * @return true si null ou vide
   */
    public static boolean isEmpty(final Object[] _array) {
        return _array == null || _array.length == 0;
    }

    public static int getNbItem(final Object[] _o) {
        return _o == null ? 0 : _o.length;
    }

    /**
   * @param _array la collection a tester: si null
   * @return true si vide ou null
   */
    @SuppressWarnings("unchecked")
    public static boolean isEmpty(final Collection _array) {
        return _array == null || _array.isEmpty();
    }

    /**
   * @param _array la collection a tester: si null
   * @return true si non vide
   */
    @SuppressWarnings("unchecked")
    public static boolean isNotEmpty(final Collection _array) {
        return !isEmpty(_array);
    }

    public static boolean isSameSize(final Object[] _i1, final Object[] _i2) {
        return (_i1 == _i2) || ((_i1 != null && _i2 != null) && (_i1.length == _i2.length));
    }

    public static int getNbItems(final Object[][] _o) {
        int r = 0;
        if (_o != null) {
            for (int i = _o.length - 1; i >= 0; i--) {
                if (_o[i] != null) {
                    r += _o[i].length;
                }
            }
        }
        return r;
    }

    /**
   * Teste l'egalit� de 2 tableaux. Les 2 tableaux doivent contenir dans le m�me ordre des objets
   * respectant o1.equals(o2). Les types des tableaux peuvent �tre diff�rents.
   * @param _t1 Le premier tableau.
   * @param _t2 Le deuxieme tableau.
   * @return True si les crit�res indiqu�s sont respect�s
   */
    public static <T1, T2> boolean isEquals(final T1[] _t1, final T2[] _t2) {
        if (_t1 == _t2) {
            return true;
        }
        if (_t1 == null || _t2 == null) {
            return false;
        }
        if (_t1.length != _t2.length) {
            return false;
        }
        for (int i = 0; i < _t1.length; i++) {
            final T1 oi = _t1[i];
            final T2 o2i = _t2[i];
            if (oi != o2i && (oi == null || !oi.equals(o2i))) {
                return false;
            }
        }
        return true;
    }

    /**
   * @deprecated Use {@link #isEquals(Object[], Object[])} instead
   */
    public static boolean isEquals(final Object _o1, final Object _o2) {
        if (_o1 == _o2) {
            return true;
        }
        if (_o1 == null || _o2 == null || !_o1.getClass().isArray() || !_o2.getClass().isArray()) {
            return false;
        }
        final int nb = Array.getLength(_o1);
        if (Array.getLength(_o2) != nb) {
            return false;
        }
        for (int i = 0; i < nb; i++) {
            final Object oi = Array.get(_o1, i);
            final Object o2i = Array.get(_o2, i);
            if (oi != o2i && (oi == null || !oi.equals(o2i))) {
                return false;
            }
        }
        return true;
    }

    /**
   * @param _intArrays liste contenant des tableaux d'entiers
   * @return le tableau correspondant
   */
    public static int[][] toArray(final List _intArrays) {
        final int[][] res = new int[_intArrays.size()][];
        for (int i = res.length - 1; i >= 0; i--) {
            res[i] = (int[]) _intArrays.get(i);
        }
        return res;
    }

    public static int[][] toArray(final TIntArrayList[] _intArrays) {
        final int[][] res = new int[_intArrays.length][];
        for (int i = res.length - 1; i >= 0; i--) {
            res[i] = _intArrays[i].toNativeArray();
        }
        return res;
    }

    public static double[][] getCorrectValue(final String[][] _values, final CtuluDoubleParser _parser) {
        final int nbValues = _values.length;
        if (nbValues == 0) {
            return null;
        }
        final int nbLine = _values[0].length;
        final TDoubleArrayList[] newValues = new TDoubleArrayList[nbValues];
        for (int i = 0; i < nbValues; i++) {
            newValues[i] = new TDoubleArrayList(nbLine);
        }
        for (int j = 0; j < nbLine; j++) {
            boolean ok = true;
            for (int i = 0; i < nbValues && ok; i++) {
                ok = _parser.isValid(_values[i][j]);
            }
            if (ok) {
                for (int k = nbValues - 1; k >= 0; k--) {
                    newValues[k].add(_parser.parse(_values[k][j]));
                }
            }
        }
        final double[][] res = new double[nbValues][newValues[0].size()];
        for (int k = nbValues - 1; k >= 0; k--) {
            res[k] = newValues[k].toNativeArray();
        }
        return res;
    }

    /**
   * Permet de ranger dans des listes, les valeurs passees en parametres. Les listes sont cr�er des qu'une ligne
   * rencontree ne contient pas un double.
   * 
   * @param _values les valeurs
   * @param _parser le parseur
   * @return une liste de tableau TDoubleArrayList[][]
   */
    public static List getCorrectValueSplit(final String[][] _values, final CtuluDoubleParser _parser) {
        final int nbValues = _values.length;
        if (nbValues == 0) {
            return null;
        }
        final int nbLine = _values[0].length;
        final List res = new ArrayList();
        TDoubleArrayList[] newValues = new TDoubleArrayList[nbValues];
        res.add(newValues);
        for (int i = 0; i < nbValues; i++) {
            newValues[i] = new TDoubleArrayList(nbLine);
        }
        for (int j = 0; j < nbLine; j++) {
            boolean ok = true;
            for (int i = 0; i < nbValues && ok; i++) {
                ok = _parser.isValid(_values[i][j]);
            }
            if (ok) {
                for (int k = nbValues - 1; k >= 0; k--) {
                    newValues[k].add(_parser.parse(_values[k][j]));
                }
            } else if (newValues[0].size() > 0) {
                newValues = new TDoubleArrayList[nbValues];
                res.add(newValues);
                for (int i = 0; i < nbValues; i++) {
                    newValues[i] = new TDoubleArrayList(nbLine);
                }
            }
        }
        return res;
    }

    /**
   * @param _array le tableau a verifier
   * @return true si null ou vide
   */
    public static boolean isEmpty(final double[] _array) {
        return _array == null || _array.length == 0;
    }

    /**
   * @param _array le tableau a verifier
   * @return true si null ou vide
   */
    public static boolean isEmpty(final int[] _array) {
        return _array == null || _array.length == 0;
    }

    /**
   * @param _array le tableau a parcourir DEPUIS LA FIN
   * @param _intToSearch l'entier cherche
   * @return l'index de l'entier dans le tableau. -1 si non trouvee
   */
    public static int findInt(final int[] _array, final int _intToSearch) {
        if (_array == null) {
            return -1;
        }
        for (int i = _array.length - 1; i >= 0; i--) {
            if (_array[i] == _intToSearch) {
                return i;
            }
        }
        return -1;
    }

    /**
   * @param _array le tableau a parcourir DEPUIS LA FIN
   * @param _intToSearch l'entier cherche
   * @return l'index de l'entier dans le tableau. -1 si non trouvee
   */
    public static int findLong(final long[] _array, final long _intToSearch) {
        if (_array == null) {
            return -1;
        }
        for (int i = _array.length - 1; i >= 0; i--) {
            if (_array[i] == _intToSearch) {
                return i;
            }
        }
        return -1;
    }

    /**
   * @param _dest la liste a modifier
   * @param _offset le premier � garder
   * @param _length la taille a enlever
   */
    public static void remove(final java.util.List _dest, final int _offset, final int _length) {
        if (_dest == null) {
            return;
        }
        if (_offset < 0 || _offset >= _dest.size()) {
            throw new ArrayIndexOutOfBoundsException(_offset);
        }
        final Object[] data = _dest.toArray();
        final int pos = data.length;
        _dest.clear();
        if (_offset == 0) {
            System.arraycopy(data, _length, data, 0, pos - _length);
        } else if (pos - _length == _offset) {
        } else {
            System.arraycopy(data, _offset + _length, data, _offset, pos - (_offset + _length));
        }
        final int newSize = pos - _length;
        for (int i = 0; i < newSize; i++) {
            _dest.add(data[i]);
        }
    }

    public static void remove(final List _l, final int[] _idx) {
        if (_l == null || _idx == null) {
            return;
        }
        Arrays.sort(_idx);
        for (int i = _idx.length - 1; i >= 0; i--) {
            final int idx = _idx[i];
            if (idx <= 0) {
                break;
            }
            if (idx < _l.size()) {
                _l.remove(i);
            }
        }
    }

    /**
   * @param _model le modele
   * @return le nombre d'indices selectionnes ou 0 si null.
   */
    public static int getSelectedIdxNb(final CtuluListSelectionInterface _model) {
        return _model == null ? 0 : _model.getNbSelectedIndex();
    }

    public static boolean isUniqueIdxSelected(final CtuluListSelectionInterface _model) {
        return _model == null ? false : _model.isOnlyOnIndexSelected();
    }

    /**
   * @param _model les indices selectionnes
   * @return tableau vide si selection vide. indice selectionne dans l'ordre croissant
   */
    public static int getSelectedIdxNb(final ListSelectionModel _model) {
        if (_model == null || _model.isSelectionEmpty()) {
            return 0;
        }
        int r = 0;
        final int max = _model.getMaxSelectionIndex();
        for (int i = 0; i <= max; i++) {
            if (_model.isSelectedIndex(i)) {
                r++;
            }
        }
        return r;
    }

    /**
   * @param _model les indices selectionnes
   * @return tableau vide si selection vide. indice selectionne dans l'ordre croissant
   */
    public static int[] getSelectedIdx(final ListSelectionModel _model) {
        if (_model == null) {
            return null;
        }
        if (_model.isSelectionEmpty()) {
            return FuEmptyArrays.INT0;
        }
        final TIntArrayList l = new TIntArrayList();
        final int max = _model.getMaxSelectionIndex();
        for (int i = 0; i <= max; i++) {
            if (_model.isSelectedIndex(i)) {
                l.add(i);
            }
        }
        return l.toNativeArray();
    }

    public static int[] getSelectedIdx(final BitSet _set) {
        if (_set == null || _set.isEmpty()) {
            return FuEmptyArrays.INT0;
        }
        final int max = _set.length();
        final TIntArrayList list = new TIntArrayList(max);
        for (int i = 0; i < max; i++) {
            if (_set.get(i)) {
                list.add(i);
            }
        }
        return list.toNativeArray();
    }

    /**
   * @param _a le premier tableau a tester
   * @param _a2 le deuxieme
   * @param _eps la marge d'erreur autorise
   * @return true si les tableaux sont de meme taille et si les valeurs de ceux-ci sont egales.
   */
    public static boolean isDoubleEquals(final double[] _a, final double[] _a2, final double _eps) {
        if (_a == _a2) {
            return true;
        }
        if ((_a == null || _a2 == null) || (_a.length != _a2.length)) {
            return false;
        }
        for (int i = _a.length - 1; i >= 0; i--) {
            if (Math.abs(_a[i] - _a2[i]) > _eps) {
                return false;
            }
        }
        return true;
    }

    /**
   * @param _d le tableau a moyenne
   * @return la moyenne des valeurs du tableau _d. "0" si null ou vide
   */
    public static double getMoyenne(final double[] _d) {
        if (_d == null || _d.length == 0) {
            return 0;
        }
        double r = 0;
        for (int i = _d.length - 1; i >= 0; i--) {
            r += _d[i];
        }
        return r / _d.length;
    }

    public static double getMoyenne(final int[] _d) {
        if (_d == null || _d.length == 0) {
            return 0;
        }
        return ((double) getSum(_d)) / ((double) _d.length);
    }

    public static double getMoyenne(final long[] _d) {
        if (_d == null || _d.length == 0) {
            return 0;
        }
        return ((double) getSum(_d)) / ((double) _d.length);
    }

    public static int getSum(final int[] _d) {
        if (_d == null || _d.length == 0) {
            return 0;
        }
        int r = 0;
        for (int i = _d.length - 1; i >= 0; i--) {
            r += _d[i];
        }
        return r;
    }

    public static double getSum(final double[] _d) {
        if (_d == null || _d.length == 0) {
            return 0;
        }
        double r = 0;
        for (int i = _d.length - 1; i >= 0; i--) {
            r += _d[i];
        }
        return r;
    }

    public static long getSum(final long[] _d) {
        if (_d == null || _d.length == 0) {
            return 0;
        }
        int r = 0;
        for (int i = _d.length - 1; i >= 0; i--) {
            r += _d[i];
        }
        return r;
    }

    /**
   * @param _d le tableau a moyenne
   * @return la moyenne des valeurs du tableau _d. "0" si null ou vide
   */
    public static double getMoyenne(final TDoubleArrayList _d) {
        if (_d == null || _d.size() == 0) {
            return 0;
        }
        double r = 0;
        for (int i = _d.size() - 1; i >= 0; i--) {
            r += _d.getQuick(i);
        }
        return r / _d.size();
    }

    /**
   * @param _d le tableau a moyenne
   * @return la moyenne des valeurs du tableau _d. "0" si null ou vide
   */
    public static double getMoyenne(final TIntArrayList _d) {
        if (_d == null || _d.size() == 0) {
            return 0;
        }
        double r = 0;
        for (int i = _d.size() - 1; i >= 0; i--) {
            r += _d.getQuick(i);
        }
        return r / _d.size();
    }

    /**
   * @param _d le tableau a moyenne
   * @return la moyenne des valeurs du tableau _d. "0" si null ou vide
   */
    public static double getMoyenne(final TLongArrayList _d) {
        if (_d == null || _d.size() == 0) {
            return 0;
        }
        double r = 0;
        for (int i = _d.size() - 1; i >= 0; i--) {
            r += _d.getQuick(i);
        }
        return r / _d.size();
    }

    /**
   * @param _d le tableau de double a parcourir
   * @return la valeur min contenue par ce tableau . 0 si aucune valeur.
   */
    public static double getMin(final double[] _d) {
        if (_d == null || _d.length == 0) {
            return 0;
        }
        double r = _d[0];
        for (int i = _d.length - 1; i > 0; i--) {
            final double ri = _d[i];
            if (ri < r) {
                r = ri;
            }
        }
        return r;
    }

    /**
   * @param _d le tableau de double a parcourir
   * @return la valeur max contenue par ce tableau . 0 si aucune valeur.
   */
    public static double getMax(final double[] _d) {
        if (_d == null || _d.length == 0) {
            return 0;
        }
        double r = _d[0];
        for (int i = _d.length - 1; i > 0; i--) {
            final double ri = _d[i];
            if (ri > r) {
                r = ri;
            }
        }
        return r;
    }

    /**
   * Recherche dans le tableau, la derniere occurrence de _objectToFound.
   * 
   * @param _array le tableau a parcourir
   * @param _objectToFound l'entier cherche
   * @return l'index de l'objet dans le tableau. null si non trouve.
   */
    public static int findObject(final Object[] _array, final Object _objectToFound) {
        if ((_array == null) || (_objectToFound == null)) {
            return -1;
        }
        for (int i = _array.length - 1; i >= 0; i--) {
            if (_objectToFound.equals(_array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static boolean containsObject(final Object[] _array, final Object _objectToFound) {
        if ((_array == null) || (_objectToFound == null)) {
            return false;
        }
        for (int i = _array.length - 1; i >= 0; i--) {
            if (_objectToFound.equals(_array[i])) {
                return true;
            }
        }
        return false;
    }

    /**
   * Recherche dans le tableau, la derniere occurrence de _objectToFound.
   * 
   * @param _array le tableau a parcourir
   * @param _objectToFound l'entier cherche
   * @return l'index de l'objet dans le tableau. -1 si non trouve
   */
    public static int findObjectEgalEgal(final Object[] _array, final Object _objectToFound) {
        if ((_array == null) || (_objectToFound == null)) {
            return -1;
        }
        for (int i = _array.length - 1; i >= 0; i--) {
            if (_objectToFound == _array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int findObjectEgalEgal(final List _list, final Object _objectToFound) {
        if ((_list == null) || (_objectToFound == null)) {
            return -1;
        }
        for (int i = _list.size() - 1; i >= 0; i--) {
            if (_objectToFound == _list.get(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
   * @param _t le tableau a inverser
   */
    public static void invert(final int[] _t) {
        final int n = _t.length - 1;
        final int n2 = n / 2;
        int temp;
        for (int i = n; i > n2; i--) {
            temp = _t[i];
            _t[i] = _t[n - i];
            _t[n - i] = temp;
        }
    }

    /**
   * Permute les donnees du tableau a partir de l'index donne.
   * 
   * @param _t le tableau a permuter
   * @param _firstIndex l'index a partir duquel la permutation commence
   */
    public static void invert(final int[] _t, final int _firstIndex) {
        final int n = _t.length - 1;
        final int nOffset = n + _firstIndex;
        final int n2 = nOffset / 2;
        int temp;
        for (int i = n; i > n2; i--) {
            temp = _t[i];
            _t[i] = _t[nOffset - i];
            _t[nOffset - i] = temp;
        }
    }

    public static void invert(final Object[] _t, final int _firstIndex) {
        final int n = _t.length - 1;
        final int nOffset = n + _firstIndex;
        final int n2 = nOffset / 2;
        Object temp;
        for (int i = n; i > n2; i--) {
            temp = _t[i];
            _t[i] = _t[nOffset - i];
            _t[nOffset - i] = temp;
        }
    }

    /**
   * @param _o le tableau a trie
   * @return le tableau trie
   */
    public static Object[] sort(final Object[] _o) {
        Arrays.sort(_o);
        return _o;
    }

    private CtuluLibArray() {
    }

    /**
   * @param _init le tableau a copier
   * @return le tableau copie
   */
    public static String[] copy(final String[] _init) {
        if (_init == null) {
            return null;
        }
        final String[] r = new String[_init.length];
        System.arraycopy(_init, 0, r, 0, _init.length);
        return r;
    }

    /**
   * @param _init le tableau a copier
   * @return le tableau copie
   */
    public static Object[] copy(final Object[] _init) {
        if (_init == null) {
            return null;
        }
        final Object[] r = new Object[_init.length];
        System.arraycopy(_init, 0, r, 0, _init.length);
        return r;
    }

    public static Boolean[] copy(final Boolean[] _init) {
        if (_init == null) {
            return null;
        }
        final Boolean[] r = new Boolean[_init.length];
        System.arraycopy(_init, 0, r, 0, _init.length);
        return r;
    }

    /**
   * @param _init le tableau a copier
   * @return le tableau copie
   */
    public static boolean[] copy(final boolean[] _init) {
        if (_init == null) {
            return null;
        }
        final boolean[] r = new boolean[_init.length];
        System.arraycopy(_init, 0, r, 0, _init.length);
        return r;
    }

    /**
   * @param _init le tableau a copier
   * @return le tableau copie
   */
    public static double[] copy(final double[] _init) {
        if (_init == null) {
            return null;
        }
        final double[] r = new double[_init.length];
        System.arraycopy(_init, 0, r, 0, _init.length);
        return r;
    }

    public static double[][] copy(final double[][] _init) {
        if (_init == null) {
            return null;
        }
        final double[][] r = new double[_init.length][];
        for (int i = r.length - 1; i >= 0; i--) {
            r[i] = copy(_init[i]);
        }
        return r;
    }

    public static void copyIn(final double[][] _init, final double[][] _dest) {
        if (_init == null) {
            return;
        }
        for (int i = _init.length - 1; i >= 0; i--) {
            for (int j = _init[i].length - 1; j >= 0; j--) {
                _dest[i][j] = _init[i][j];
            }
        }
    }

    /**
   * @param _init le tableau a copier
   * @return le tableau copie
   */
    public static int[] copy(final int[] _init) {
        if (_init == null) {
            return null;
        }
        final int[] r = new int[_init.length];
        System.arraycopy(_init, 0, r, 0, _init.length);
        return r;
    }

    /**
   * @param _init le tableau a copier
   * @return le tableau copie
   */
    public static long[] copy(final long[] _init) {
        if (_init == null) {
            return null;
        }
        final long[] r = new long[_init.length];
        System.arraycopy(_init, 0, r, 0, _init.length);
        return r;
    }

    /**
   * Cherche l'index de la chaine <code>_string</code> dans le tableau <code>_stringArray</code>. Renvoie
   * <code>-1</code> si l'un des deux parametres est nuls.
   * 
   * @return index si l'action du composant est trouvee dans le tableau, -1 sinon
   * @param _obj objet a trouver
   * @param _objArray tableau a parcourir
   */
    public static int getIndex(final Object _obj, final Object[] _objArray) {
        if ((_obj == null) || (_objArray == null)) {
            return -1;
        }
        for (int i = _objArray.length - 1; i >= 0; i--) {
            if (_obj.equals(_objArray[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int getIndex(final Object _obj, final List _objArray) {
        if ((_obj == null) || (_objArray == null)) {
            return -1;
        }
        for (int i = _objArray.size() - 1; i >= 0; i--) {
            if (_obj.equals(_objArray.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static int getFirstIndex(final Object _obj, final List _objArray) {
        if ((_obj == null) || (_objArray == null)) {
            return -1;
        }
        final int nb = _objArray.size();
        for (int i = 0; i < nb; i++) {
            if (_obj.equals(_objArray.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static int getIndex(final Object _obj, final ListModel _objArray) {
        if (_objArray == null) {
            return -1;
        }
        for (int i = _objArray.getSize() - 1; i >= 0; i--) {
            final Object com = _objArray.getElementAt(i);
            if (com == _obj || (com != null && com.equals(_obj))) {
                return i;
            }
        }
        return -1;
    }

    /**
   * Transpose un tableau de double � 2 dimensions.
   * 
   * @param _matrix Le tableau � transposer.
   * @return Le tableau transpos�.
   */
    public static double[][] transpose(final double[][] _matrix) {
        if (_matrix == null) {
            return null;
        }
        if (_matrix.length == 0) {
            return new double[0][0];
        }
        final double[][] res = new double[_matrix[0].length][_matrix.length];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[i].length; j++) {
                res[i][j] = _matrix[j][i];
            }
        }
        return res;
    }

    /**
   * Test l'�galit� de 2 tableaux de double � 2 dimension.
   * 
   * @param _t Le premier tableau � tester.
   * @param _t2 Le deuxi�me tableau � tester.
   * @return Vrai si tous les �l�ments de t sont �gaux � ceux de t2.
   */
    public static boolean equals(final double[][] _t, final double[][] _t2) {
        if (_t == _t2) {
            return true;
        }
        if (_t == null || _t2 == null) {
            return false;
        }
        final int length = _t.length;
        if (_t2.length != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!Arrays.equals(_t[i], _t2[i])) {
                return false;
            }
        }
        return true;
    }

    public static void concat(final double[][] _source, final double[] _dest) {
        int destPos = 0;
        for (int i = 0; i < _source.length; i++) {
            final int l = _source[i].length;
            System.arraycopy(_source[i], 0, _dest, destPos, l);
            destPos += l;
        }
    }

    /**
   * Test l'�galit� � un epsilon de 2 tableaux de double � 2 dimension.
   * 
   * @param _t Le premier tableau � tester.
   * @param _t2 Le deuxi�me tableau � tester.
   * @return Vrai si tous les �l�ments de t sont �gaux � ceux de t2 � +-epsilon.
   */
    public static boolean equals(final double[] _t, final double[] _t2, final double _epsilon) {
        if (_t == _t2) {
            return true;
        }
        if (_t == null || _t2 == null) {
            return false;
        }
        final int length = _t.length;
        if (_t2.length != length) {
            return false;
        }
        final double epsilonAbs = Math.abs(_epsilon);
        for (int i = 0; i < length; i++) {
            if (Math.abs(_t[i] - _t2[i]) > epsilonAbs) {
                return false;
            }
        }
        return true;
    }

    /**
   * Test la precision des valeurs du tableau.
   * 
   * @param _t Le tableau � tester.
   * @return _precision : la longueur moyenne des valeurs du tableau
   */
    public static double precision(final double[] _t) {
        if (_t == null || _t.length == 0) {
            return 0;
        }
        double somme = 0;
        for (int i = 0; i < _t.length; i++) {
            somme += Double.toString(_t[i]).length();
        }
        return somme / _t.length;
    }

    /**
   * Remplit le tableau pass� de facon incr�mentale, depuis une valeur initiale.
   * @param _t Le tableau
   * @param _initVal La valeur initiale pour le premier indice.
   * @return Le tableau pass�, initialis�, de 1 en 1
   */
    public static double[] fillIncremental(final double[] _t, double _initVal) {
        for (int i = 0; i < 0; i++) _t[i] = i + _initVal;
        return _t;
    }

    /**
   * Remplit le tableau pass� de facon incr�mentale, depuis une valeur initiale.
   * @param _t Le tableau
   * @param _initVal La valeur initiale pour le premier indice.
   * @return Le tableau pass�, initialis� de 1 en 1.
   */
    public static int[] fillIncremental(final int[] _t, int _initVal) {
        for (int i = 0; i < 0; i++) _t[i] = i + _initVal;
        return _t;
    }
}
