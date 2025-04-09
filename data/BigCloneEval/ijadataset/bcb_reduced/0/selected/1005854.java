package org.fudaa.dodico.dico;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import org.fudaa.ctulu.CtuluLibArray;
import org.fudaa.ctulu.CtuluLibString;
import org.fudaa.ctulu.CtuluPermanentList;
import org.fudaa.ctulu.fileformat.FileFormat;

/**
 * @author deniger
 * @version $Id: DicoManager.java,v 1.17 2006-09-19 14:42:27 deniger Exp $
 */
public abstract class DicoManager {

    /**
   * le repertoire contenant des fichiers dico a charger dynamiquement.
   */
    public static final String DICO_DIR = "dicos";

    Map formatVersions_;

    String packageName_;

    /**
   * les formats geres.
   */
    private CtuluPermanentList formats_;

    protected DicoManager(final String _packageName) {
        packageName_ = _packageName;
    }

    /**
   * @return les formats geres
   */
    public CtuluPermanentList getFormats() {
        return formats_;
    }

    protected DicoModelAbstract createDico(final String _name, final String _version, final int _language) {
        try {
            final Class c = Class.forName(packageName_ + DicoGenerator.getClassName(_name, _version));
            return (DicoModelAbstract) c.getConstructor(new Class[] { int.class }).newInstance(new Object[] { new Integer(_language) });
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract DicoCasFileFormat createFormat(String _s);

    protected final void load() {
        final File f = new File(DICO_DIR, packageName_.replace('.', '/'));
        final FileFilter filter = new FileFilter() {

            public boolean accept(File _f) {
                String s = _f.getName();
                if ((s.startsWith(DicoGenerator.DICO_PREFIX)) && (s.endsWith(".class"))) {
                    return true;
                }
                return false;
            }
        };
        final Map formatNameList = new HashMap();
        final File[] dicoExt = f.listFiles(filter);
        if ((dicoExt != null) && (dicoExt.length > 0)) {
            final int n = dicoExt.length - 1;
            for (int i = n; i >= 0; i--) {
                String string = dicoExt[i].getName();
                final int index = string.indexOf(".class");
                if (index > 0) {
                    string = string.substring(DicoGenerator.DICO_PREFIX.length(), index);
                    final String[] nameVersion = DicoAnalyzer.getNameAndVersion(string);
                    List l = (List) formatNameList.get(nameVersion[0]);
                    if (l == null) {
                        l = new ArrayList(5);
                        l.add(nameVersion[1]);
                        formatNameList.put(nameVersion[0], l);
                    } else {
                        l.add(nameVersion[1]);
                    }
                }
            }
        }
        boolean formatAjoute = false;
        if (formatNameList.size() != 0) {
            for (final Iterator it = formatNameList.entrySet().iterator(); it.hasNext(); ) {
                final Map.Entry e = (Map.Entry) it.next();
                final String formatName = (String) e.getKey();
                final DicoCasFileFormat format = getFileFormat(formatName);
                if (format == null) {
                    formatAjoute = true;
                    final String[] version = CtuluLibString.enTableau((List) e.getValue());
                    Arrays.sort(version);
                    formatVersions_.put(createFormat(formatName), version);
                } else {
                    final String[] version = mergeAndSort(getVersions(format), ((List) e.getValue()));
                    Arrays.sort(version);
                    formatVersions_.put(format, version);
                }
            }
        }
        if (formatAjoute) {
            formats_ = new CtuluPermanentList(formatVersions_.keySet());
        }
    }

    /**
   * @param _f le format
   * @return l'instance avec la derniere version et le language courant.
   */
    public DicoCasFileFormatVersion createLastVersionImpl(final DicoCasFileFormat _f) {
        return createLastVersionImpl(_f, DicoLanguage.getCurrentID());
    }

    /**
   * Choisit la derniere version.
   *
   * @param _f le format
   * @param _language le langage
   * @return l'instance correspondante
   */
    public DicoCasFileFormatVersion createLastVersionImpl(final DicoCasFileFormat _f, final int _language) {
        return createVersionImpl(_f, getLastVersion(_f), _language);
    }

    /**
   * Choisit le langage courant.
   *
   * @param _ft le format
   * @param _v la version
   * @return l'instance correspondante
   */
    public DicoCasFileFormatVersion createVersionImpl(final DicoCasFileFormat _ft, final String _v) {
        return createVersionImpl(_ft, _v, DicoLanguage.getCurrentID());
    }

    /**
   * Rassemble dans le meme tableau les chaine du tableau <code>_init1</code> et de la collection. Le tableau est
   * ensuite range.
   *
   * @param _init1 le tableau a fusionner
   * @param _init2 la collection a fusionner
   * @return la fusion des 2 !
   */
    public static String[] mergeAndSort(final String[] _init1, final Collection _init2) {
        if (CtuluLibArray.isEmpty(_init1)) {
            return null;
        }
        final int nb = _init1.length + (_init2 == null ? 0 : _init2.size());
        final Set r = new HashSet(nb);
        r.addAll(Arrays.asList(_init1));
        if (_init2 != null) {
            r.addAll(_init2);
        }
        final String[] rf = (String[]) r.toArray(new String[r.size()]);
        Arrays.sort(rf);
        return rf;
    }

    /**
   * @param _ft le format
   * @param _v la version
   * @param _language le language
   * @return l'instance correspondante
   */
    public DicoCasFileFormatVersion createVersionImpl(final DicoCasFileFormat _ft, final String _v, final int _language) {
        final DicoModelAbstract model = createDico(_ft.getName(), _v, _language);
        return model == null ? null : new DicoCasFileFormatVersion(_ft, model);
    }

    /**
   * @param _nom le nom du format voulu
   * @return le format ou null si aucun
   */
    public DicoCasFileFormat getFileFormat(final String _nom) {
        for (final Iterator it = formats_.iterator(); it.hasNext(); ) {
            final DicoCasFileFormat r = (DicoCasFileFormat) it.next();
            if (r.getName().equals(_nom)) {
                return r;
            }
        }
        return null;
    }

    /**
   * @param _f le format voulu
   * @return la derniere version pour ce format
   */
    public String getLastVersion(final DicoCasFileFormat _f) {
        final String[] vs = getVersions(_f);
        return ((vs == null) || (vs.length == 0)) ? null : vs[vs.length - 1];
    }

    /**
   * @param _f le format a tester
   * @return les versions gerees pour ce format
   */
    public String[] getVersions(final DicoCasFileFormat _f) {
        return (String[]) formatVersions_.get(_f);
    }

    /**
   * @param _f le format a tester
   * @return le nombre de version pour ce format
   */
    public int getVersionsNb(final DicoCasFileFormat _f) {
        final String[] v = (String[]) formatVersions_.get(_f);
        return v == null ? 0 : v.length;
    }

    /**
   * @param _init la table contenant les formats geres (nom, version[])
   */
    public void init(final Map _init) {
        formatVersions_ = new TreeMap(new FileFormat.FileFormatNameComparator());
        formatVersions_.putAll(_init);
        formats_ = new CtuluPermanentList(formatVersions_.keySet());
    }
}
