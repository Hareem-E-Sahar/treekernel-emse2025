package salto.fwk.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import salto.fwk.exception.TechniqueException;

/**
 * Classe utilitaire pour les tableaux � dimensions variables (Vector - Array)
 */
public final class ArrayUti {

    /**
	 * pas de constructeur pour les classes utilitaires
	 */
    private ArrayUti() {
    }

    /**
	 * Classe permettant de convertir une String en objet par s�rialization Java
	 */
    private class BlobConverter implements ArrayUtiConverter {

        /**
		 * converti une cha�ne de caract�re (provenant d'un objet java s�ializ�)
		 * en un objet java
		 * 
		 * @param param
		 *            ch�ine correspondant � un objet java
		 * @return objet java
		 * @throws TechniqueException
		 *             exception si la cha�ne ne correspond pas � un objet java
		 *             s�rializ�
		 */
        public Object convertToObj(String param) throws TechniqueException {
            return BlobUti.blobToObj(param.getBytes());
        }
    }

    /**
	 * permet de convertir une liste d'objet serializable en une cha�ne de
	 * caract�res <b>Cette fonction ne fonctionne que si votre composant na
	 * comporte pas le caract�re '#' dans un de ses attributs </b>
	 * 
	 * @param list
	 *            liste d'objets java � s�rializ�
	 * @return la cha�ne de caract�re correspondant aux objets s�riliz�s
	 * @throws TechniqueException
	 *             exception de type IOException ne devant normalement pas avoir
	 *             lieu
	 * @see ArrayUti#blobStringToArray(String)
	 */
    public static String arrayToBlobString(List list) throws TechniqueException {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        Iterator i = list.iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
            Object o = i.next();
            if (o == list) {
                buf.append("(this Collection)");
            } else if (o == null) {
                buf.append("null");
            } else {
                buf.append(new String(BlobUti.objToBlob((Serializable) o)));
            }
            hasNext = i.hasNext();
            if (hasNext) buf.append('#');
        }
        buf.append("]");
        return buf.toString();
    }

    /**
	 * Permet de convertir une cha�ne de caract�re en tableau d'objet
	 * <p>
	 * La cha�ne de caract�re doit correspondre � certaines normes (s�parateur -
	 * valeur nulle )
	 * 
	 * @param param
	 *            cha�ne comportant une liste d'objets javas (s�parateur '#')
	 * @return liste d'objet java
	 * @throws TechniqueException
	 *             exception de type IOException ne devant normalement pas avoir
	 *             lieu
	 * @see BlobUti#blobToObj(byte[])
	 * @see ArrayUti#arrayToBlobString(List)
	 */
    public static ArrayList blobStringToArray(String param) throws TechniqueException {
        return stringToArray(param, '#', "null", new ArrayUti().new BlobConverter());
    }

    /**
	 * Converti une liste d'objet java en une cha�ne de caract�re avec le
	 * s�parateur '#'
	 * 
	 * @see #arrayToString(List, char, String)
	 */
    public static String arrayToString(List list) {
        return arrayToString(list, '#', "null");
    }

    /**
	 * permet de convertir un tableau en chaine de caract�re
	 * <p>
	 * Cette m�thode applique la m�thode toString sur les objects
	 * 
	 * @param list
	 *            liste d'objet java
	 * @param separator
	 *            caract�re de s�paration entre les diff�rents �l�ments de la
	 *            liste
	 * @param nullValue
	 *            valeur de la cha�ne lorsqu'un �l�ment est null
	 * @return cha�ne comprenant les �l�ments du tableau
	 * @see ArrayUti#stringToArray(String, char, String)
	 */
    public static String arrayToString(List list, char separator, String nullValue) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        Iterator i = list.iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
            Object o = i.next();
            if (o == list) {
                buf.append("(this Collection)");
            } else if (o == null) {
                buf.append(nullValue);
            } else {
                buf.append(o.toString());
            }
            hasNext = i.hasNext();
            if (hasNext) buf.append(separator);
        }
        buf.append("]");
        return buf.toString();
    }

    /**
	 * Permet de convertir une cha�ne de caract�re en tableau contenant des
	 * cha�nes de caract�re
	 * 
	 * @param list
	 *            cha�ne de caract�re ayant �t� pr�alablement cr�� avec la
	 *            m�thode arrayToString
	 * @return tableau de String
	 * @see ArrayUti#arrayToString(List)
	 */
    public static List stringToArray(String list) {
        return stringToArray(list, '#', "null");
    }

    /**
	 * Permet de convertir une cha�ne de caract�re en tableau contenant des
	 * cha�nes de caract�re
	 * 
	 * @param list
	 *            cha�ne de caract�re ayant �t� pr�alablement cr�� avec la
	 *            m�thode arrayToString
	 * @param separator
	 *            caract�re de s�paration
	 * @param nullValue
	 *            valeur lorsque l'objet est null
	 * @return tableau de String
	 * @see ArrayUti#arrayToString(List, char, String)
	 */
    public static ArrayList stringToArray(String list, char separator, String nullValue) {
        ArrayList result = new ArrayList();
        int len = list.length() - 1;
        int deb = 1;
        String temp;
        for (int i = 1; i < len; i++) {
            if (list.charAt(i) == separator) {
                temp = list.substring(deb, i - 1);
                if (temp.equals(nullValue)) result.add(null); else result.add(temp);
                deb = i + 1;
            }
        }
        temp = list.substring(deb, len);
        if (temp.equals(nullValue)) result.add(null); else result.add(temp);
        return result;
    }

    /**
	 * Permet de convertir une cha�ne de caract�re en tableau contenant des
	 * cha�nes de caract�re
	 * <p>
	 * Pour chaque cha�ne de caract�re trouv�e, la m�thode convertToObj est
	 * appel�e
	 * 
	 * @param list
	 *            cha�ne de caract�re ayant �t� pr�alablement cr�� avec la
	 *            m�thode arrayToString
	 * @param separator
	 *            caract�re de s�paration
	 * @param nullValue
	 *            valeur lorsque l'objet est null
	 * @param convertObj
	 *            objet permettant de convertir les �l�ments de la cha�ne en
	 *            objet
	 * @return tableau d'objet
	 * @throws TechniqueException
	 *             uniquement lorsque la conversion est impossible
	 * @see ArrayUti#arrayToString(List, char, String)
	 */
    public static ArrayList stringToArray(String list, char separator, String nullValue, ArrayUtiConverter convertObj) throws TechniqueException {
        ArrayList result = new ArrayList();
        int len = list.length() - 1;
        int deb = 1;
        String temp;
        for (int i = 1; i < len; i++) {
            if (list.charAt(i) == separator) {
                temp = list.substring(deb, i - 1);
                if (temp.equals(nullValue)) result.add(null); else result.add(convertObj.convertToObj(temp));
                deb = i + 1;
            }
        }
        temp = list.substring(deb, len);
        if (temp.equals(nullValue)) result.add(null); else result.add(convertObj.convertToObj(temp));
        return result;
    }

    /**
	 * Permet d'obtenir un tableau de String � partir d'une cha�ne de caract�res
	 * 
	 * @param list
	 *            la chaine de caract�re correspondant � un tableau
	 * @param token
	 *            le caract�re de s�paration
	 * @return le tableau de string
	 */
    public static String[] list2String(String list, String token) {
        StringTokenizer tokenizer = new StringTokenizer(list, token);
        ArrayList listVal = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            listVal.add(tokenizer.nextToken());
        }
        return (String[]) listVal.toArray(new String[listVal.size()]);
    }

    /**
	 * Permet d'obtenir un tableau de int � partir d'une cha�ne de caract�res
	 * 
	 * @param list
	 *            la chaine de caract�re correspondant � un tableau
	 * @param token
	 *            le caract�re de s�paration
	 * @return tableau d'entier
	 */
    public static int[] lst2int(String list, String token) {
        StringTokenizer tokenizer = new StringTokenizer(list, token);
        ArrayList listVal = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            listVal.add(Integer.valueOf(tokenizer.nextToken()));
        }
        int[] res = new int[listVal.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = ((Integer) listVal.get(i)).intValue();
        }
        return res;
    }

    /**
	 *  Ajoute un �l�ment � un tableau existant.
	 * 
	 * @return java.lang.Object[] Le tableau mis � jour
	 * @param table java.lang.Object[] Le tableau � mettre � jour.
	 * @param newObject java.lang.Object L'�l�ment � ins�rer.
	 * @exception TechniqueException Lev�e si le tableau ou l'�l�ment pass�s en param�tre sont � null.
	 */
    public static final Object[] addToArray(Object[] table, Object newObject) throws TechniqueException {
        if (table == null) throw new TechniqueException("Impossible d'ajouter un �l�ment � un tableau qui n'existe pas");
        if (newObject == null) throw new TechniqueException("Impossible d'ajouter un �l�ment null � un tableau");
        Class c = table.getClass();
        Object newArray = java.lang.reflect.Array.newInstance(c.getComponentType(), table.length + 1);
        System.arraycopy(table, 0, newArray, 0, table.length);
        java.lang.reflect.Array.set(newArray, table.length, newObject);
        return (Object[]) newArray;
    }
}
