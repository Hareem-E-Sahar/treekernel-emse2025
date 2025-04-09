package org.arpenteur.common.manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.arpenteur.common.IItem;
import org.arpenteur.common.ident.IIdentifiedObject;
import org.arpenteur.common.math.geometry.Geometry;
import org.arpenteur.common.math.geometry.point.IPoint2D;
import org.arpenteur.common.math.geometry.point.IPoint3D;
import org.arpenteur.common.math.geometry.point.Pt;
import org.arpenteur.common.math.geometry.primitive.Line;
import org.arpenteur.common.math.geometry.primitive.Plane;
import org.arpenteur.common.misc.DoubleFormatter;
import org.arpenteur.common.misc.comparator.IComparator;

public class ManagerAction {

    static String charset = "UTF-8";

    /**
	 * * Echange_elem !! ===> Exceptions !!
	 */
    public static void exchangeDatasElementsAt(int i1, int i2, Object[] datas, int datasCount) {
        Object obj;
        if ((i1 >= datasCount) || (i2 >= datasCount)) {
            throw new ArrayIndexOutOfBoundsException(i1 + "||" + i2 + " >= " + datasCount + " Manager ");
        }
        obj = datas[i1];
        datas[i1] = datas[i2];
        datas[i2] = obj;
    }

    /*****************************************************************************
	 * <pre>
	 * tri_rapide : methode du pivot --&gt; d'apres
	 * STRUCTURES DE DONNEES EN C++, JAVA ET ADA95
	 * Auteur: Christian CARREZ, InterEditions.
	 * Institution: CNAM 292 rue Saint Martin, 75141 Paris 03 - 01.07.1997
	 * ALGORITHMES DE TRI RAPIDE
	 * preconditions
	 *   plus-petits(l), plus-grands(l), pivot(l):
	 *   non estvide(l)
	 * propriete fonctionnelle
	 *   tri-rapide(l) =
	 *        si l = listevide alors listevide
	 *        sinon tri-rapide(plus-petits(l)) &amp; [pivot(l)] &amp;
	 *                           tri-rapide(plus-grands(l)) fsi
	 * </pre>
	 */
    public static void quickSort(Object[] datas, int datasCount, IComparator comparator) {
        int debut = 0;
        int fin = datasCount - 1;
        int presumee;
        int I, J;
        Object pivot;
        ArrayList<Integer> memo = new ArrayList<Integer>();
        if (datasCount < 2) {
            return;
        }
        for (; ; ) {
            while (fin - debut > 1) {
                presumee = (debut + fin) / 2;
                I = debut;
                if (comparator.compare(datas[presumee], datas[I]) < 0) {
                    I = presumee;
                }
                if (comparator.compare(datas[fin], datas[I]) < 0) {
                    I = fin;
                }
                if (debut != I) {
                    ManagerAction.exchangeDatasElementsAt(debut, I, datas, datasCount);
                }
                if (comparator.compare(datas[presumee], datas[fin]) < 0) {
                    pivot = datas[presumee];
                    datas[presumee] = null;
                } else {
                    pivot = datas[fin];
                    datas[fin] = datas[presumee];
                    datas[presumee] = null;
                }
                I = debut + 1;
                J = fin - 1;
                while (I < J) {
                    while (I < presumee) {
                        if (comparator.compare(pivot, datas[I]) < 0) {
                            break;
                        }
                        I = I + 1;
                    }
                    while (J > presumee) {
                        if (comparator.compare(datas[J], pivot) < 0) {
                            break;
                        }
                        J = J - 1;
                    }
                    if (I == J) {
                        break;
                    }
                    if (I == presumee) {
                        datas[I] = datas[J];
                        datas[J] = null;
                        presumee = J;
                        I = I + 1;
                    } else if (J == presumee) {
                        datas[J] = datas[I];
                        datas[I] = null;
                        presumee = I;
                        J = J - 1;
                    } else {
                        ManagerAction.exchangeDatasElementsAt(I, J, datas, datasCount);
                        I = I + 1;
                        J = J - 1;
                    }
                }
                datas[presumee] = pivot;
                pivot = null;
                if (presumee - debut < fin - presumee) {
                    memo.add(new Integer(presumee + 1));
                    memo.add(new Integer(fin));
                    fin = presumee - 1;
                } else {
                    memo.add(new Integer(debut));
                    memo.add(new Integer(presumee - 1));
                    debut = presumee + 1;
                }
            }
            if (fin == debut + 1) {
                if (comparator.compare(datas[fin], datas[debut]) < 0) {
                    ManagerAction.exchangeDatasElementsAt(debut, fin, datas, datasCount);
                }
            }
            if (memo.size() == 0) {
                return;
            }
            fin = ((Integer) memo.get(memo.size() - 1)).intValue();
            memo.remove(memo.size() - 1);
            debut = ((Integer) memo.get(memo.size() - 1)).intValue();
            memo.remove(memo.size() - 1);
        }
    }

    /**
	 * Reverse the point order in this Manager Reference on point are conserved.
	 * 
	 */
    public static void reverseOrder(List<IIdentifiedObject> list) {
        Object[] cpy = list.toArray();
        list.clear();
        for (int i = cpy.length - 1; i >= 0; i--) {
            if (cpy[i] instanceof IIdentifiedObject) list.add((IIdentifiedObject) cpy[i]);
        }
    }

    /**
	 * Give the index in the manager of the object which identifiers are ida and
	 * num
	 * 
	 * @param ida
	 *            string identifier of the object
	 * @param num
	 *            numeric identifier of the object
	 * @return index of the object in the manager, -1 if there is no object with
	 *         the specified identifiers in the manager
	 */
    public static int getIndexOfId(List<IIdentifiedObject> list, String ida, int num) {
        for (int i = 0; i < list.size(); i++) {
            IIdentifiedObject p = list.get(i);
            if (p.getName().equals(ida) && p.getIdn() == num) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * Give the index in the manager of the object which identifiers are ida and
	 * num
	 * 
	 * @param ida
	 *            string identifier of the object
	 * @param num
	 *            numeric identifier of the object
	 * @return index of the object in the manager, -1 if there is no object with
	 *         the specified identifiers in the manager
	 */
    public static int getIndexOfId(PointManager list, String ida, int num) {
        for (int i = 0; i < list.size(); i++) {
            IIdentifiedObject p = list.get(i);
            if (p.getName().equals(ida) && p.getIdn() == num) {
                return i;
            }
        }
        return -1;
    }

    public static IPoint3D getPtById(List<IPoint3D> list, String ida, int num) {
        for (int i = 0; i < list.size(); i++) {
            IPoint3D p = list.get(i);
            if (p.getName().equals(ida) && p.getIdn() == num) {
                return p;
            }
        }
        return null;
    }

    public static IIdentifiedObject getObjectById(List list, String ida, int num) {
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof IIdentifiedObject) {
                IIdentifiedObject p = (IIdentifiedObject) list.get(i);
                if (p.getName().equals(ida) && p.getIdn() == num) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
	 * Return a manager containing all the objects matching the key
	 * 
	 * @param key
	 *            string identifier of the objects
	 * @return a sub manager
	 */
    public static ArrayList<IIdentifiedObject> getSubManagerFromId(List<? extends IIdentifiedObject> list, String key) {
        ArrayList<IIdentifiedObject> extrait = new ArrayList<IIdentifiedObject>();
        Iterator<? extends IIdentifiedObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            IIdentifiedObject id = iterator.next();
            if (id.getName().equals(key)) {
                extrait.add(id);
            }
        }
        return extrait;
    }

    /**
	 * Return a manager containing all the objects matching the key
	 * 
	 * @param key
	 *            string identifier of the objects
	 * @return a sub manager
	 */
    public static ArrayList<IIdentifiedObject> getSubManagerFromId(List<IIdentifiedObject> list, int key) {
        ArrayList<IIdentifiedObject> extrait = new ArrayList<IIdentifiedObject>();
        Iterator<IIdentifiedObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            IIdentifiedObject id = iterator.next();
            if (id.getIdn() == key) {
                extrait.add(id);
            }
        }
        return extrait;
    }

    /**
	 * Give the next free identification number for an object with a string
	 * identifier
	 * 
	 * @param ida
	 *            string identifier of the object
	 * @return the first free identification number ine the manager for this
	 *         object
	 */
    public static int getNextIdN(List<IIdentifiedObject> list, String ida) {
        int max = 0;
        for (int i = 0; i < list.size(); i++) {
            IIdentifiedObject io = list.get(i);
            if (io.getName().equals(ida)) {
                if (max < io.getIdn()) {
                    max = io.getIdn();
                }
            }
        }
        return max + 1;
    }

    /**
	 * Return the object in the manager specified with specified string
	 * identifier and numeric identifier.
	 * 
	 * @param ida
	 *            string identifier of the object searched
	 * @param idn
	 *            integer identifier of the object searched
	 * @return the object searched or null if the identifiers do not match an
	 *         object in the manager
	 */
    public static IIdentifiedObject getObject(List<? extends IIdentifiedObject> list, String ida, int idn) {
        for (int i = 0; i < list.size(); i++) {
            IIdentifiedObject p = list.get(i);
            if (p.getName().equals(ida) && p.getIdn() == idn) {
                return p;
            }
        }
        return null;
    }

    /**
	 * Return the object in the manager specified with specified string
	 * identifier and numeric identifier.
	 * 
	 * @param ida
	 *            string identifier of the object searched
	 * @param idn
	 *            integer identifier of the object searched
	 * @return the object searched or null if the identifiers do not match an
	 *         object in the manager
	
	public static IPoint3D getObject(List<? extends IPoint3D> list, String ida, int idn) {
		for (int i = 0; i < list.size(); i++) {
			IPoint3D p = list.get(i);
			if (p.getName().equals(ida) && p.getIdn() == idn) {
				return p;
			}
		}
		return null;
	}
 */
    public static boolean hasPoint3D(List<IPoint3D> list, IPoint3D p) {
        if (list.size() > 0 && p != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
	 * Get the more far point from an input one contained in the PoitManager.
	 * 
	 * @param origine
	 *            : origin point
	 * @param lookAll
	 *            : true if we want to use all the points, false if we want to
	 *            use only Active and Valid points
	 * @return more far point
	 */
    public static IPoint3D getMoreFarPointFrom(List<IPoint3D> list, IPoint3D origine, boolean lookAll) {
        IPoint3D result = null;
        if (origine == null) return null;
        double dMax = -1.0;
        for (int i = 0; i < list.size(); i++) {
            IPoint3D p = list.get(i);
            if (p == null) {
                break;
            }
            if ((p != null) && (lookAll || (p.isValid() && p.isActive()))) {
                double d = origine.dist(p);
                if (d > dMax) {
                    dMax = d;
                    result = p;
                }
            }
        }
        return result;
    }

    /**
	 * Get the more far point from an input one contained in the PoitManager,
	 * using all the points (active and not, valid and not).
	 * 
	 * @param origine
	 *            : origin point
	 * @return more far point
	 */
    public static IPoint3D getMoreFarPointFrom(List<IPoint3D> list, IPoint3D origine) {
        return ManagerAction.getMoreFarPointFrom(list, origine, true);
    }

    /**
	 * Get the largest dimensions segment between the points contained into the
	 * PointManager
	 * 
	 * @param lookAll
	 *            : true if we want to use all the points, false if we want to
	 *            use only Active and Valid points
	 * @return largest dimensions segment or null if there are problems
	 */
    public static IPoint3D[] getLargestSegment(List<IPoint3D> list, boolean lookAll) {
        IPoint3D[] result = null;
        if (list.size() >= 2) {
            IPoint3D r0 = ManagerAction.getPointACoordInf(list, lookAll);
            if (r0 != null) {
                result = new IPoint3D[2];
                result[0] = r0;
                int k = ManagerAction.getFirstActiveValidPoint(list, lookAll);
                result[1] = list.get(k);
                double dMax = result[0].dist(result[1]);
                for (int i = k + 1; i < list.size(); i++) {
                    IPoint3D p = list.get(i);
                    if (p == null) {
                        break;
                    }
                    if ((p != null) && (p != result[0]) && (lookAll || (p.isValid() && p.isActive()))) {
                        double d = result[0].dist(p);
                        if (d > dMax) {
                            dMax = d;
                            result[1] = p;
                        }
                    }
                }
                if (result[1] == null) {
                    result = null;
                }
            }
        }
        return result;
    }

    /**
	 * Get the largest triangle of maximum area containing the largest segment
	 * 
	 * @param lookAll
	 *            : true if we want to use all the points, false if we want to
	 *            use only Active and Valid points
	 * @return largest triangle points or null if there are problems
	 */
    public static IPoint3D[] getLargestTriangle(List<IPoint3D> list, boolean lookAll) {
        IPoint3D[] result = null;
        if (list.size() >= 3) {
            IPoint3D[] line = ManagerAction.getLargestSegment(list, lookAll);
            if ((line != null) && (line[0].vectorFromP(line[1]).getNorm() > IPoint3D.EPSILON7)) {
                result = new IPoint3D[3];
                for (int i = 0; i < 2; i++) {
                    result[i] = line[i];
                }
                IPoint3D p1p2 = result[0].vectorFromP(result[1]).normer();
                int k = ManagerAction.getFirstActiveValidPoint(list, lookAll);
                IPoint3D p = list.get(k);
                result[2] = p;
                IPoint3D p1p = result[0].vectorFromP(p);
                double nor = p1p.getNorm();
                nor *= nor;
                double prodska = p1p.scalarProd(p1p2);
                prodska *= prodska;
                double dmax = nor - prodska;
                for (int i = k + 1; i < list.size(); i++) {
                    p = list.get(i);
                    if (p == null) {
                        break;
                    }
                    if ((p != result[0]) && (p != result[1]) && (lookAll || (p.isValid() && p.isActive()))) {
                        p1p = result[0].vectorFromP(p);
                        nor = p1p.getNorm();
                        nor *= nor;
                        prodska = p1p.scalarProd(p1p2);
                        prodska *= prodska;
                        double dis = nor - prodska;
                        if (dis > dmax) {
                            dmax = dis;
                            result[2] = p;
                        }
                    }
                }
                if (result[2] == null) {
                    result = null;
                }
            }
        }
        return result;
    }

    /**
	 * Get the largest dimensions segment between the points contained into the
	 * PointManager, using all the points (active and not, valid and not).
	 * 
	 * @return largest dimensions segment or null if there are problems
	 */
    public static IPoint3D[] getLargestSegment(List<IPoint3D> list) {
        return ManagerAction.getLargestSegment(list, true);
    }

    /**
	 * Get the largest dimensions segment value between the points contained
	 * into the PointManager, using all the points (active and not, valid and
	 * not).
	 * 
	 * @return largest dimensions or -1 if there are problems
	 * @see substitute of Point3DManager.getLargestSegmentLength
	 */
    public static double getLargestSegmentDim(List<IPoint3D> list) {
        return ManagerAction.getLargestSegmentDim(list, true);
    }

    /**
	 * Get the largest dimensions segment value between the points contained
	 * into the PointManager
	 * 
	 * @param lookAll
	 *            : true if we want to use all the points, false if we want to
	 *            use only Active and Valid points
	 * @return largest dimensions or -1 if there are problems
	 */
    public static double getLargestSegmentDim(List<IPoint3D> list, boolean lookAll) {
        IPoint3D[] dim = ManagerAction.getLargestSegment(list, lookAll);
        IPoint3D p1 = dim[0];
        IPoint3D p2 = dim[1];
        if ((p1 != null) && (p2 != null)) return p1.dist(p2); else return -1;
    }

    /**
	 * Get the largest triangle of maximum area containing the largest segment,
	 * using all the points (active and not, valid and not).
	 * 
	 * @return largest triangle points or null if there are problems
	 */
    public static IPoint3D[] getLargestTriangle(List<IPoint3D> list) {
        return ManagerAction.getLargestTriangle(list, true);
    }

    /**
	 * Get the first active and valid point contained into the PointManager
	 * 
	 * @param lookAll
	 *            : if it is true we use all the points (active and not, valid
	 *            and not) and return the first point.
	 * @return the first point ( active and valid only if the input parameter
	 *         lookAll is false)
	 */
    public static int getFirstActiveValidPoint(List<IPoint3D> list, boolean lookAll) {
        IPoint3D result = null;
        int k = 0;
        while ((result == null) && (k < list.size())) {
            IPoint3D p = list.get(k);
            if ((!(lookAll)) && (!p.isActive() || !p.isValid())) {
                k++;
            } else {
                result = p;
            }
        }
        return k;
    }

    /**
	 * Get the number of valid and active points in "this" PointManager
	 * 
	 * @return
	 */
    public static int getNbPoint3DActif(List<IPoint3D> list) {
        int nb = 0;
        for (int i = 0; i < list.size(); i++) {
            IPoint3D pr = list.get(i);
            if (pr.isActive() && pr.isValid()) {
                nb++;
            }
        }
        return nb;
    }

    /**
	 * Get a point with xMin or yMin or zMin
	 * 
	 * @param lookAll
	 * @return
	 */
    public static IPoint3D getPointACoordInf(List<IPoint3D> list, boolean lookAll) {
        IPoint3D result = null;
        int k = ManagerAction.getFirstActiveValidPoint(list, lookAll);
        for (int i = k + 1; i < list.size(); i++) {
            IPoint3D p = list.get(i);
            if (p == null) {
                break;
            }
            if (!(lookAll) && (!p.isActive() || !p.isValid())) {
                break;
            }
            if (result == null) {
                result = p;
            } else if ((p.getX() < result.getX()) || (p.getY() < result.getY()) || (p.getZ() < result.getZ())) {
                result = p;
            }
        }
        return result;
    }

    /**
	 * Get the smallest dimensions segment between the points contained into the
	 * PointManager, using all the points (active and not, valid and not).
	 * 
	 * @return smallest dimensions segment or null if there are problems
	 */
    public static IPoint3D[] getSmallestSegment(List<IPoint3D> list) {
        IPoint3D[] result = null;
        if (list.size() >= 2) {
            result = new IPoint3D[2];
            result[0] = ManagerAction.getPointACoordInf(list, true);
            double dMin = 0;
            for (int i = 0; i < list.size(); i++) {
                IPoint3D p = list.get(i);
                if (p == null) {
                    break;
                }
                if (p != result[0]) {
                    double d = result[0].dist(p);
                    if ((result[1] == null) || (d < dMin)) {
                        result[1] = p;
                        dMin = d;
                    }
                }
            }
        }
        return result;
    }

    /**
	 * Get the smallest triangle of maximum area containing the smallest
	 * segment, using all the points (active and not, valid and not).
	 * 
	 * @return smallest triangle points or null if there are problems
	 */
    public static IPoint3D[] getSmallestTriangle(List<IPoint3D> list) {
        IPoint3D[] result = null;
        if (list.size() >= 3) {
            IPoint3D[] line = ManagerAction.getSmallestSegment(list);
            if ((line != null) && (line[0].vectorFromP(line[1]).getNorm() > IPoint3D.EPSILON7)) {
                result = new IPoint3D[3];
                for (int i = 0; i < 2; i++) {
                    result[i] = line[i];
                }
                IPoint3D p1p2 = result[0].vectorFromP(result[1]).normer();
                double dMin = 0;
                for (int i = 0; i < list.size(); i++) {
                    IPoint3D p = list.get(i);
                    if (p == null) {
                        break;
                    }
                    IPoint3D p1p = result[0].vectorFromP(p).normer();
                    if (p1p.getNorm() < IPoint3D.EPSILON7) {
                        break;
                    }
                    double prodska = p1p.scalarProd(p1p2);
                    double d = 1 - (prodska * prodska);
                    if (1 - prodska > IPoint3D.EPSILON7) {
                        if ((result[2] == null) || (d < dMin)) {
                            result[2] = p;
                            dMin = d;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
	 * Get the index of the next valid and active point near from the point in
	 * the input index position
	 * 
	 * @param indice
	 *            : index of the point
	 * @return index of the valid and active nearest point
	 */
    private static int getNextPtDifferent(List<IPoint3D> list, int indice) {
        int j = indice;
        boolean fin = false;
        while (fin != true) {
            boolean ptsegaux = false;
            if (j != list.size() - 1) {
                j++;
            } else {
                j = 0;
            }
            IPoint3D ptest = list.get(j);
            if ((ptest.getX() == list.get(indice).getX()) && (ptest.getY() == list.get(indice).getY()) && (ptest.getZ() == list.get(indice).getZ())) {
                ptsegaux = true;
            }
            if ((ptsegaux != true) && (ptest.isActive()) && (ptest.isValid())) {
                fin = true;
            }
            if (j == indice) {
                fin = true;
            }
        }
        return (j);
    }

    /**
	 * Get the first three points which can describe a triangle (not aligned)
	 * and fill with these point the input array.
	 * 
	 * @param fac
	 *            : three vertex indices
	 * @return false if the points are considered aligned or there are not three
	 *         valid and active points
	 */
    public static boolean getFirstTriangle(List<IPoint3D> list, int[] fac) {
        boolean ok = false;
        int i = 0;
        int p0, p1, p2;
        while (i <= list.size() - 1) {
            if ((i <= list.size()) && (list.get(i).isValid()) && (list.get(i).isActive())) {
                p0 = i;
                p1 = ManagerAction.getNextPtDifferent(list, p0);
                p2 = ManagerAction.getNextPtDifferent(list, p1);
                IPoint3D v1 = list.get(p0).vectorFromP(list.get(p1));
                IPoint3D v2 = list.get(p0).vectorFromP(list.get(p2));
                if ((v1.vectorialProd(v2).getNorm()) / (v1.getNorm() * v2.getNorm()) >= 0.01) {
                    fac[0] = p0;
                    fac[1] = p1;
                    fac[2] = p2;
                    return true;
                }
            }
            i++;
        }
        return (ok);
    }

    /**
	 * Get the index of nearest point from an input one
	 * 
	 * @param p1
	 *            : input point
	 * @return the index or -1 if there are problems
	 */
    public static int getIndexOfNearestPointFromPoint(List<IPoint3D> list, IPoint3D p1) {
        int result = -1;
        double min = Double.MAX_VALUE;
        double d = min;
        for (int i = 0; i < list.size(); i++) {
            d = p1.dist(list.get(i));
            if (d < min) {
                min = d;
                result = i;
            }
        }
        return result;
    }

    /**
	 * Get the nearest point from an input one
	 * 
	 * @param p1
	 *            : input point
	 * @return the nearest point or null if there are problems
	 */
    public static IPoint3D getNearestPointFromPoint(List<IPoint3D> list, IPoint3D p1) {
        IPoint3D result = null;
        double min = Double.MAX_VALUE;
        double d = min;
        for (int i = 0; i < list.size(); i++) {
            d = p1.dist(list.get(i));
            if (d < min) {
                min = d;
                result = list.get(i);
            }
        }
        return result;
    }

    /**
	 * Get the nearest point from an input line described by two points on it
	 * 
	 * @param p1
	 *            : first line point
	 * @param p2
	 *            : second line point
	 * @return the nearest point or null if there are problems
	 */
    public static IPoint3D getNearestPointFromLine(List<IPoint3D> list, IPoint3D p1, IPoint3D p2) {
        IPoint3D result = null;
        Line de = new Line(p1, p2, false);
        double min = Double.MAX_VALUE;
        double d = min;
        for (int i = 0; i < list.size(); i++) {
            d = de.distanceFrom(list.get(i));
            if (d < min) {
                min = d;
                result = de.projectOn(list.get(i));
            }
        }
        return result;
    }

    /**
	 * Get the index of nearest point from an input line described by two points
	 * on it
	 * 
	 * @param p1
	 *            : first line point
	 * @param p2
	 *            : second line point
	 * @return the index of nearest point or -1 if there are problems
	 */
    public static int getIndexOfNearestPointFromLine(List<IPoint3D> list, IPoint3D p1, IPoint3D p2) {
        int result = -1;
        Line de = new Line(p1, p2, false);
        double min = Double.MAX_VALUE;
        double d = min;
        for (int i = 0; i < list.size(); i++) {
            d = de.distanceFrom(list.get(i));
            if (d < min) {
                min = d;
                result = i;
            }
        }
        return result;
    }

    /**
	 * Find the nearest points of each point of the manager with respect to a
	 * given point manager
	 * 
	 * @param pm
	 *            a point manager
	 * @return PointManager
	 */
    public static PointManager findNearestPoints(List<IPoint3D> list, List<IPoint3D> pm) {
        PointManager result = new PointManager();
        double distanceMin = Double.MAX_VALUE;
        double distanceTemp = 0;
        IPoint3D tempPoint, tempPoint2;
        for (int i = 0; i < list.size(); i++) {
            distanceMin = Double.MAX_VALUE;
            distanceTemp = 0;
            tempPoint2 = Geometry.newPoint3D();
            for (int j = 0; j < pm.size(); j++) {
                tempPoint = list.get(i).minus(pm.get(j));
                distanceTemp = tempPoint.scalarProd(tempPoint);
                if (distanceTemp < distanceMin) {
                    distanceMin = distanceTemp;
                    tempPoint2 = pm.get(j);
                    tempPoint2.setIdn(i);
                }
            }
            result.addPoint3D(tempPoint2);
        }
        return result;
    }

    /**
	 * Found all the points inside a rectangle parallel to the input plane axis.<br>
	 * This rectangle is defined in the plane reference system. The PointManager
	 * is transformed in this reference system and then compared to min and max
	 * coordinates passed as parameters.<br>
	 * Special case: if plan parameter is null the x,y point coordinates are
	 * directly compared to x and y max/min
	 * 
	 * @param plan
	 *            : input plane
	 * @param xMin
	 *            : xMin rectangle coordinate on the plane
	 * @param yMin
	 *            : yMin rectangle coordinate on the plane
	 * @param xMax
	 *            : xMax rectangle coordinate on the plane
	 * @param yMax
	 *            : yMax rectangle coordinate on the plane
	 * @return the PointManager containing the found points
	 */
    public static PointManager getAllPointInside(List<IPoint3D> list, Plane plan, double xMin, double yMin, double xMax, double yMax) {
        PointManager pm = new PointManager();
        boolean identity = (plan == null);
        for (int i = 0; i < list.size(); i++) {
            IPoint3D p = list.get(i);
            IPoint3D q = p;
            if (!identity) {
                q = plan.getTransformedPoint(p);
            }
            if ((q.getX() > xMin) && (q.getX() < xMax) && (q.getY() > yMin) && (q.getY() < yMax)) pm.addPoint3D(p);
        }
        return pm;
    }

    /**
	 * Add points given from the input buffer
	 * 
	 * @param buffer
	 *            : input string
	 * @return false if we have problem to decode the string
	 */
    public static boolean readFromString(List<IPoint3D> list, String buffer) {
        if (list == null) return false;
        boolean result = true;
        IPoint3D p;
        if (buffer == null) {
            return true;
        }
        StringTokenizer st = new StringTokenizer(buffer, " ,;\t\n\r:");
        while (st.hasMoreTokens()) {
            p = Geometry.newPoint3D();
            if (p.readFromString(st)) {
                list.add(p);
            } else {
                return false;
            }
        }
        return result;
    }

    /**
	 * Read points from an input buffer.<br>
	 * Set all the point id : the name is set to ida input parameter and the
	 * number is set to start + n
	 * 
	 * @param buffer
	 *            : input string
	 * @param ida
	 *            : input name
	 * @param start
	 *            : initial start value
	 * @return false if we have problem with reading the string
	 */
    public static boolean readFromString(List<IPoint3D> list, String buffer, String ida, int start) {
        boolean result = true;
        IPoint3D p;
        if (buffer == null) {
            return true;
        }
        StringTokenizer st = new StringTokenizer(buffer, " ,;\t\n\r:");
        while (st.hasMoreTokens()) {
            p = Geometry.newPoint3D();
            if (p.readFromString(st)) {
                p.setName(ida);
                p.setIdn(start + list.size());
                list.add(p);
            } else {
                return false;
            }
        }
        return result;
    }

    /**
	 * 
	 * @param filePath
	 */
    public static void readFromFile(List<IPoint3D> list, String filePath) {
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String ligne;
            int nbLigne = 0;
            while ((ligne = bufferedReader.readLine()) != null) {
                IPoint3D p = Geometry.newPoint3D();
                nbLigne++;
                if (p.readFromString(ligne)) list.add(p);
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Get the min and max coordinated of the points contained into the
	 * PointManager.
	 * 
	 * @return an array with the values xmin, ymin, zmin, xmax, ymax, zmax in
	 *         this order
	 */
    public static double[] getCoordMinMax(List<IPoint3D> list) {
        double[] minMax = new double[6];
        PointManager pm = ManagerAction.get8PtBoundingBox(list);
        double xMax = Double.MIN_VALUE;
        double xMin = Double.MAX_VALUE;
        double zMax = Double.MIN_VALUE;
        double zMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;
        double yMin = Double.MAX_VALUE;
        if (list.size() == 0) {
            return null;
        }
        for (int i = 0; i < pm.size(); i++) {
            IPoint3D p = list.get(i);
            if (p.getX() > xMax) {
                xMax = p.getX();
            }
            if (p.getX() < xMin) {
                xMin = p.getX();
            }
            if (p.getY() > yMax) {
                yMax = p.getY();
            }
            if (p.getY() < yMin) {
                yMin = p.getY();
            }
            if (p.getZ() > zMax) {
                zMax = p.getZ();
            }
            if (p.getZ() < zMin) {
                zMin = p.getZ();
            }
        }
        minMax[0] = xMin;
        minMax[1] = yMin;
        minMax[2] = zMin;
        minMax[3] = xMax;
        minMax[4] = yMax;
        minMax[5] = zMax;
        return minMax;
    }

    /**
	 * Get a 3D box with the coordinates of the max and minimum values contained
	 * into the PointManager.<br>
	 * This box can contain in the 3D space all the points in the PointManager
	 * 
	 * @return the eight vertex into a new PointManager
	 */
    public static PointManager get8PtBoundingBox(List<IPoint3D> list) {
        PointManager result = new PointManager();
        double xMax = Double.MIN_VALUE;
        double xMin = Double.MAX_VALUE;
        double zMax = Double.MIN_VALUE;
        double zMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;
        double yMin = Double.MAX_VALUE;
        if (list.size() == 0) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            IPoint3D p = list.get(i);
            if (p.getX() > xMax) {
                xMax = p.getX();
            }
            if (p.getX() < xMin) {
                xMin = p.getX();
            }
            if (p.getY() > yMax) {
                yMax = p.getY();
            }
            if (p.getY() < yMin) {
                yMin = p.getY();
            }
            if (p.getZ() > zMax) {
                zMax = p.getZ();
            }
            if (p.getZ() < zMin) {
                zMin = p.getZ();
            }
        }
        result.addElement(Geometry.newPoint3D(xMin, yMin, zMax));
        result.addElement(Geometry.newPoint3D(xMax, yMin, zMax));
        result.addElement(Geometry.newPoint3D(xMax, yMax, zMax));
        result.addElement(Geometry.newPoint3D(xMin, yMax, zMax));
        result.addElement(Geometry.newPoint3D(xMin, yMin, zMin));
        result.addElement(Geometry.newPoint3D(xMax, yMin, zMin));
        result.addElement(Geometry.newPoint3D(xMax, yMax, zMin));
        result.addElement(Geometry.newPoint3D(xMin, yMax, zMin));
        return result;
    }

    /**
	 * Split "this" PointManager into two input/output PointManager: one for the
	 * selected points and one for the unselected
	 * 
	 * @param pmSel
	 *            : PointManager to fill with the selected points
	 * @param pmUnSel
	 *            : PointManager to fill with the unselected points
	 */
    public static void splitSelectedPoints(List<IPoint3D> list, PointManager pmSel, PointManager pmUnSel) {
        if ((pmSel == null) || (pmUnSel == null)) return;
        for (int i = 0; i < list.size(); i++) {
            IPoint3D p = list.get(i);
            if (p.isSelecteded()) pmSel.addPoint3D(p); else pmUnSel.addPoint3D(p);
        }
    }

    /**
	 * Write Formatted Text in the OutputStream passed as input.<br>
	 * Format:<br>
	 * -> 20 integer digits<br>
	 * -> name + idn + x + y + z
	 * 
	 * @param os
	 *            : OutputStream
	 */
    public static void writeFormattedText(List<IPoint3D> list, OutputStream os) {
        try {
            IPoint3D p = Geometry.newPoint3D();
            DoubleFormatter db = new DoubleFormatter(20, p.getNbDecimal());
            for (int i = 0; i < list.size(); i++) {
                p = list.get(i);
                os.write((p.getName() + " " + p.getIdn() + "  " + db.toString(p.getX(), p.getY(), p.getZ()) + "").getBytes(charset));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Write Formatted Text in the OutputStream passed as input.<br>
	 * Format:<br>
	 * -> 20 integer digits<br>
	 * -> x + y + z
	 * 
	 * @param os
	 *            : OutputStream
	 */
    public static void writeFormattedTextXYZ(List<IPoint3D> list, OutputStream os) {
        try {
            IPoint3D p = Geometry.newPoint3D();
            DoubleFormatter db = new DoubleFormatter(20, p.getNbDecimal());
            for (int i = 0; i < list.size(); i++) {
                p = list.get(i);
                os.write((db.toString(p.getX(), p.getY(), p.getZ()) + "").getBytes(charset));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Format:<br>
	 * -> 20 integer digits<br>
	 * -> name + idn + x + y + z
	 * 
	 * @return toString
	 */
    public static String toStringANXYZ(List<IPoint3D> list) {
        StringBuffer sb = new StringBuffer();
        IPoint3D p = Geometry.newPoint3D();
        DoubleFormatter db = new DoubleFormatter(20, p.getNbDecimal());
        for (int i = 0; i < list.size(); i++) {
            p = list.get(i);
            sb.append(p.getName() + " " + p.getIdn() + "  " + db.toString(p.getX(), p.getY(), p.getZ()) + "");
        }
        return sb.toString();
    }

    /**
	 * Format:<br>
	 * -> 20 integer digits<br>
	 * -> idn + x + y + z
	 * 
	 * @return toString
	 */
    public static String toStringNXYZ(List<IPoint3D> list) {
        StringBuffer sb = new StringBuffer();
        IPoint3D p = Geometry.newPoint3D();
        DoubleFormatter db = new DoubleFormatter(20, p.getNbDecimal());
        for (int i = 0; i < list.size(); i++) {
            p = list.get(i);
            sb.append(" " + p.getIdn() + "  " + db.toString(p.getX(), p.getY(), p.getZ()) + "");
        }
        return sb.toString();
    }

    /**
	 * Format:<br>
	 * -> 15 integer digits<br>
	 * -> x + y + z
	 * 
	 * @return toString
	 */
    public static String toStringXYZ(List<IPoint3D> list) {
        StringBuffer sb = new StringBuffer();
        IPoint3D p = Geometry.newPoint3D();
        DoubleFormatter db = new DoubleFormatter(15, p.getNbDecimal());
        for (int i = 0; i < list.size(); i++) {
            p = list.get(i);
            sb.append(db.toString(p.getX(), p.getY(), p.getZ()) + "");
        }
        return sb.toString();
    }

    /**
	 * Get the center of gravity
	 * 
	 * @param lookAll
	 *            : true if we want to use all the points, false if we want to
	 *            use only Active and Valid points
	 * @return
	 */
    public static IPoint3D getBarycentre(List<IPoint3D> list, boolean lookAll) {
        IPoint3D bary = null;
        if (list.size() > 0) {
            int nb = 0;
            for (int i = 0; i < list.size(); i++) {
                IPoint3D p = list.get(i);
                if (p == null) {
                    break;
                }
                if ((p != null) && (lookAll || (p.isValid() && p.isActive()))) {
                    nb++;
                    if (bary == null) {
                        bary = p.newPoint3D(p);
                    } else {
                        bary = bary.plus(p);
                    }
                }
            }
            if ((nb > 0) && (bary != null)) {
                bary.multiplyEqual(1 / (float) nb);
            }
        }
        return bary;
    }

    /**
	 * Delete redundant 3D points (JCC)
	 * 
	 * @return PointManager
	 */
    public static PointManager deleteRedundant3DPoints(List<IPoint3D> list) {
        int nbOfVertices = list.size();
        int numberOfSortedPoints = nbOfVertices;
        boolean[] tempBarray = new boolean[nbOfVertices];
        for (int i = 0; i < nbOfVertices; i++) tempBarray[i] = false;
        for (int i = 0; i < nbOfVertices; i++) if (tempBarray[i] == false) for (int j = 0; j < nbOfVertices; j++) if ((i != j) && list.get(i).equals(list.get(j)) == true) {
            tempBarray[j] = true;
            numberOfSortedPoints--;
        }
        IPoint3D[] filteredPointArray = new IPoint3D[numberOfSortedPoints];
        numberOfSortedPoints = 0;
        for (int i = 0; i < nbOfVertices; i++) if (tempBarray[i] == false) filteredPointArray[numberOfSortedPoints++] = list.get(i);
        return new PointManager(filteredPointArray);
    }

    /**
	 * Subdivide the point manager. (JCC) Each segment defined by consecutive
	 * points are splitted in two equal parts. For a meaningfull utilization,
	 * consecutive points of the manager are assumed to described a non
	 * self-intersecting polyline.
	 * 
	 * @return PointManager
	 */
    public static PointManager subdivide(List<IPoint3D> list) {
        int numberOfPoints = list.size();
        IPoint3D[] tempArray = new IPoint3D[(numberOfPoints - 1) * 2];
        int k = 0;
        for (int i = 0; i < numberOfPoints - 2; i++) {
            tempArray[k] = list.get(i);
            tempArray[k + 1] = (list.get(i).plus(list.get(i + 1)).divide(2.0));
            tempArray[k + 1].setIdn(k + 1);
            k += 2;
        }
        tempArray[k++] = list.get(numberOfPoints - 2);
        tempArray[k] = list.get(numberOfPoints - 1);
        return new PointManager(tempArray);
    }

    /**
	 * Return a quintuplet featuring the point manager (JCC)
	 * 
	 * @return pointManager
	 */
    public static IPoint3D[] getCharacteristicQuintuplet(List<IPoint3D> list) {
        double distanceMax = 0.0;
        double distanceTemp = 0.0;
        double areaMax = 0.0;
        double areaMaxTemp = 0.0;
        int areaMaxIndice = 0;
        int indice1 = 0, indice2 = 0;
        IPoint3D p1 = Geometry.newPoint3D();
        IPoint3D p2 = Geometry.newPoint3D();
        IPoint3D p3 = Geometry.newPoint3D();
        IPoint3D v1 = Geometry.newPoint3D();
        IPoint3D v2 = Geometry.newPoint3D();
        IPoint3D crossProduct = new Pt();
        IPoint3D[] tempIPointArray = new IPoint3D[5];
        int size = list.size();
        for (int i = 0; i < size; i++) for (int j = 0; j < size; j++) {
            distanceTemp = list.get(i).distSquare(list.get(j));
            if (distanceTemp > distanceMax) {
                distanceMax = distanceTemp;
                indice1 = i;
                indice2 = j;
            }
        }
        p1 = list.get(indice1);
        p2 = list.get(indice2);
        v1 = p2.minus(p1);
        for (int i = 0; i < size; i++) {
            p3 = list.get(i);
            v2 = p3.minus(p1);
            crossProduct = v1.vectorialProd(v2);
            areaMaxTemp = crossProduct.scalarProd(crossProduct);
            if (areaMaxTemp > areaMax) {
                areaMax = areaMaxTemp;
                areaMaxIndice = i;
            }
        }
        tempIPointArray[0] = p1;
        tempIPointArray[0].setIdn(1);
        tempIPointArray[0].setName("");
        tempIPointArray[1] = list.get((indice1 + indice2) / 2);
        tempIPointArray[1].setIdn(2);
        tempIPointArray[1].setName("");
        tempIPointArray[2] = p2;
        tempIPointArray[2].setIdn(3);
        tempIPointArray[2].setName("");
        tempIPointArray[3] = list.get(areaMaxIndice);
        tempIPointArray[3].setIdn(4);
        tempIPointArray[3].setName("");
        tempIPointArray[4] = new Pt(list.get(size - 1));
        tempIPointArray[4].setIdn(5);
        tempIPointArray[4].setName("");
        return tempIPointArray;
    }

    public static void setSelectAllIItem(ArrayList<IItem> list, boolean sel) {
        if (list.size() > 0) for (int i = 0; i < list.size(); i++) list.get(i).setSelected(sel);
    }

    public static void setSelectAllIPoint2D(ArrayList<IPoint2D> list, boolean sel) {
        if (list.size() > 0) for (int i = 0; i < list.size(); i++) list.get(i).setSelected(sel);
    }

    public static void setSelectAllIPoint3D(List<IPoint3D> list, boolean sel) {
        if (list.size() > 0) for (int i = 0; i < list.size(); i++) list.get(i).setSelected(sel);
    }

    /**
	 * Reset all the points into the Pont3DManager as selected (false)
	 */
    public static void setSelectAll(List<IItem> list, boolean sel) {
        if (list.size() > 0) for (int i = 0; i < list.size(); i++) list.get(i).setSelected(sel);
    }

    /**
	 * Reset all the points into the Pont3DManager as active (true)
	 */
    public static void setActiveAll(List<IItem> list, boolean act) {
        if (list.size() > 0) for (int i = 0; i < list.size(); i++) list.get(i).setActive(act);
    }

    /**
	 * Reset all the points into the Pont3DManager as active (true)
	 */
    public static void setActiveAllIPoint3D(List<IPoint3D> list, boolean act) {
        if (list.size() > 0) for (int i = 0; i < list.size(); i++) list.get(i).setActive(act);
    }
}
