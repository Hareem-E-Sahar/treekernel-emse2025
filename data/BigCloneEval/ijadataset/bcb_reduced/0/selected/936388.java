package org.fudaa.fudaa.commun.exec;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import com.memoire.bu.BuEmptyIcon;
import com.memoire.bu.BuIcon;
import com.memoire.bu.BuPreferences;
import com.memoire.bu.BuResource;
import com.memoire.fu.Fu;
import com.memoire.fu.FuLib;
import com.memoire.fu.FuLog;
import org.fudaa.ctulu.CtuluLibFile;
import org.fudaa.ctulu.CtuluLibString;
import org.fudaa.dodico.calcul.CalculExec;
import org.fudaa.dodico.calcul.CalculExecBatchCommon;
import org.fudaa.dodico.calcul.CalculExecDefault;
import org.fudaa.dodico.commun.DodicoPreferences;
import org.fudaa.dodico.objet.CExec;
import org.fudaa.fudaa.commun.FudaaLib;
import org.fudaa.fudaa.commun.FudaaPreferences;
import org.fudaa.fudaa.commun.FudaaUI;
import org.fudaa.fudaa.ressource.FudaaResource;

/**
 * @author deniger
 * @version $Id: FudaaExec.java,v 1.24 2007-02-02 11:22:15 deniger Exp $
 */
public class FudaaExec implements Comparable {

    /**
   * @author Fred Deniger
   * @version $Id: FudaaExec.java,v 1.24 2007-02-02 11:22:15 deniger Exp $
   */
    public class LaunchAction extends AbstractAction {

        File dir_;

        Runnable nextProcess_;

        FudaaUI ui_;

        public LaunchAction() {
            updateExec();
        }

        protected final void updateExec() {
            putValue(Action.SMALL_ICON, FudaaExec.this.getIcon());
            putValue(Action.NAME, FudaaExec.this.getShownName());
        }

        /**
     * Appelle la fonction execInDir avec comme parametres le rep de l'utilisateur.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
        public final void actionPerformed(final ActionEvent _e) {
            execInDir(dir_ == null ? new File(FuLib.getUserHome()) : dir_, ui_, nextProcess_);
        }

        public File getDir() {
            return dir_;
        }

        public Runnable getNextProcess() {
            return nextProcess_;
        }

        public FudaaUI getUi() {
            return ui_;
        }

        public void setDir(final File _dir) {
            dir_ = _dir;
        }

        public void setNextProcess(final Runnable _nextProcess) {
            nextProcess_ = _nextProcess;
        }

        public void setUi(final FudaaUI _ui) {
            ui_ = _ui;
        }
    }

    public static final String ARGS = "args";

    public static final String BATCH = "batch";

    public static final Icon EMPTY_TOOL_ICON = getNoToolIcon();

    public static final String EXE = "exe";

    public static final String ICON = "icon";

    public static final String INTERN_PREFIX = "(intern)";

    public static final String KEY_PREFIXE = "ext.executable";

    public static final String NAME = "name";

    public static final BuPreferences PREF = FudaaPreferences.FUDAA;

    public static boolean containsIdName(final List _ex, final String _idToCheck) {
        FudaaExec ex;
        for (final Iterator it = _ex.iterator(); it.hasNext(); ) {
            ex = (FudaaExec) it.next();
            if (ex.getIDName().equals(_idToCheck)) {
                return true;
            }
        }
        return false;
    }

    public static Icon getIcon(final String _p) {
        if (_p == null) {
            return EMPTY_TOOL_ICON;
        }
        if (_p.startsWith(INTERN_PREFIX)) {
            return FudaaResource.FUDAA.getToolIcon(_p.substring(INTERN_PREFIX.length()));
        }
        return BuResource.BU.reduceToolIcon(new BuIcon(_p));
    }

    public static int getIdNameIndex(final FudaaExec[] _ex, final String _idName) {
        if (_idName == null) {
            return -1;
        }
        final int n = _ex.length - 1;
        for (int i = n; i >= 0; i--) {
            if ((_ex[i] != null) && (_ex[i].getIDName().equals(_idName))) {
                return i;
            }
        }
        return -1;
    }

    public static int getIdNameIndex(final List _ex, final String _idToCheck) {
        FudaaExec ex;
        final int n = _ex.size() - 1;
        for (int i = n; i >= 0; i--) {
            ex = (FudaaExec) _ex.get(i);
            if (ex.getIDName().equals(_idToCheck)) {
                return i;
            }
        }
        return -1;
    }

    /**
   * Le tableau doit etre trie selon l'ordre naturel des FudaaExec.
   */
    public static int getIdNameIndexSort(final FudaaExec[] _ex, final String _idName) {
        if (_idName == null) {
            return -1;
        }
        int lowIndex = 0;
        int highIndex = _ex.length;
        int temp, tempMid;
        while (lowIndex <= highIndex) {
            tempMid = (lowIndex + highIndex) / 2;
            temp = _ex[tempMid].getIDName().compareTo(_idName);
            if (temp < 0) {
                lowIndex = tempMid + 1;
            } else if (temp > 0) {
                highIndex = tempMid - 1;
            } else {
                return tempMid;
            }
        }
        return -1;
    }

    public static Icon getNoToolIcon() {
        final int w = BuResource.BU.getDefaultToolSize();
        return new BuEmptyIcon(w, w);
    }

    public static FudaaExec loadFromPref(final String _name) {
        final String prefixe = DodicoPreferences.buildPrefKey(KEY_PREFIXE, _name, true);
        String exe = PREF.getStringProperty(prefixe + EXE, null);
        if (exe == null) {
            return null;
        }
        final String ico = PREF.getStringProperty(prefixe + ICON, "(intern)appli/" + _name);
        final FudaaExec r = new FudaaExec(_name, exe, ico);
        exe = PREF.getStringProperty(prefixe + NAME, null);
        if (exe != null) {
            r.setShownName(exe);
        }
        r.reloadFromPref();
        return r;
    }

    String args_;

    private boolean batchMode_;

    Icon icon_;

    String iconURL_;

    String idName_;

    String shownName_;

    protected String execCommand_;

    public FudaaExec(final String _idName) {
        this(_idName, null, null);
    }

    public FudaaExec(final String _idName, final String _exec, final String _iconUrl) {
        idName_ = _idName;
        execCommand_ = _exec;
        if (execCommand_ == null) {
            execCommand_ = idName_;
        }
        iconURL_ = _iconUrl;
        icon_ = getIcon(iconURL_);
        shownName_ = _idName;
    }

    protected final void exec(final CalculExec _exec, final File _f, final Runnable _nexProcess) {
        new Thread() {

            public void run() {
                try {
                    _exec.launch(_f, null);
                } catch (final RuntimeException _io) {
                    execNotFound(_exec.getUI().getParentComponent());
                }
            }
        }.start();
    }

    protected final void exec(final Component _cmp, final CExec _exec) {
        new Thread() {

            public void run() {
                try {
                    _exec.exec();
                } catch (final RuntimeException _io) {
                    execNotFound(_cmp);
                }
            }
        }.start();
    }

    protected void execNotFound(final Component _impl) {
        final FudaaExecPanel pn = new FudaaExecPanel(this);
        pn.setErrorText(FudaaLib.getS("Ex�cutable non trouv�"));
        pn.afficheModale(_impl, BuResource.BU.getString("Erreur"));
    }

    /**
   * @return les parametres a passer a l'exe
   */
    protected String[] getParam() {
        return CtuluLibString.parseString(args_, CtuluLibString.ESPACE);
    }

    protected String[] getParamForFile(final File _f) {
        return new String[] { _f.getAbsolutePath() };
    }

    /**
   * @param _ex l'exe a supprimer des prefs
   */
    protected void razPref() {
        final String prefixe = DodicoPreferences.buildPrefKey(KEY_PREFIXE, getIDName(), true);
        PREF.removeProperty(prefixe + EXE);
        PREF.removeProperty(prefixe + ICON);
        PREF.removeProperty(prefixe + NAME);
        PREF.removeProperty(prefixe + BATCH);
        PREF.removeProperty(prefixe + ARGS);
    }

    /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
    public int compareTo(final Object _o) {
        if ((_o == null) || (!(_o instanceof FudaaExec))) {
            throw new IllegalArgumentException("objet a comparer non valide");
        }
        return idName_.compareTo(((FudaaExec) _o).getIDName());
    }

    /**
   * @param _args les arguments a passer a l'executable
   * @param _f le repertoire dans lequel l'exe doit se lancer
   * @return instance de cexec correctement initialisee
   */
    public CalculExec createExecutant(final String[] _args) {
        final int n = _args == null ? 0 : _args.length;
        CalculExec ex = null;
        if (getExecCommand() != null && getExecCommand().indexOf(' ') > 0) {
            ex = new CalculExecBatchCommon(idName_, getExecCommand());
            ((CalculExecBatchCommon) ex).setArgs(CtuluLibString.arrayToString(_args, CtuluLibString.ESPACE));
            ex.setLauchInNewTerm(true);
            ex.setStartCmdUse(true);
        } else {
            final String[] cmd = new String[n + 1];
            cmd[0] = getExecCommand();
            if (_args != null) {
                for (int i = 0; i < n; i++) {
                    cmd[i + 1] = _args[i];
                }
            }
            ex = new CalculExecDefault(cmd);
            ex.setLauchInNewTerm(isBatchMode());
            ex.setStartCmdUse(isBatchMode());
        }
        ex.setChangeWorkingDirectory(true);
        return ex;
    }

    public boolean equals(final FudaaExec _obj) {
        if (_obj == null) {
            return false;
        }
        if (this == _obj) {
            return true;
        }
        return _obj.getIDName().equals(idName_);
    }

    public boolean equals(final Object _obj) {
        if (this == _obj) {
            return true;
        }
        if (_obj instanceof FudaaExec) {
            return idName_.equals(((FudaaExec) _obj).getIDName());
        }
        return false;
    }

    public final void execInDir(final File _dir, final FudaaUI _parent) {
        execInDir(_dir, _parent, null);
    }

    /**
   * Lance l'exe dans le rep _dir et recupere les param a partir de la methode getParam.
   * 
   * @see #getParam()
   * @param _dir le repertoire dans lequel l'exe doit se lancer
   * @param _nexProcess le processus a lancer des la fin de l'exe. peut etre null.
   */
    public void execInDir(final File _dir, final FudaaUI _parent, final Runnable _nexProcess) {
        if (CtuluLibFile.exists(_dir) && (_dir.isDirectory())) {
            if (Fu.DEBUG && FuLog.isDebug()) {
                FuLog.debug("FCM: launch " + getShownName() + " in " + _dir.getAbsolutePath());
            }
            exec(createExecutant(getParam()), _dir, _nexProcess);
        }
    }

    public final void execOnFile(final File _target, final FudaaUI _parent) {
        execOnFile(_target, _parent, null);
    }

    public void execOnFile(final File _target, final FudaaUI _parent, final Runnable _nextProcess) {
        exec(createExecutant(getParam()), _target, _nextProcess);
    }

    /**
   * @return l'action associee a cette appli
   */
    public LaunchAction getAction() {
        return new LaunchAction();
    }

    public String getArgs() {
        return args_;
    }

    /**
   * @return l'exe utilisee
   */
    public String getExecCommand() {
        return execCommand_;
    }

    /**
   * @return l'icone de l'exe
   */
    public Icon getIcon() {
        return icon_;
    }

    public String getIconURL() {
        return iconURL_;
    }

    /**
   * Renvoie la chaine de caractere identifiant cet executable.
   */
    public String getIDName() {
        return idName_;
    }

    /**
   * Renvoie la chaine utilisee pour l'affichage.
   */
    public String getShownName() {
        return shownName_;
    }

    /**
   * Renvoie un nom 'non nul' pour l'affichage. Si le nom d'affichage est nul, renvoie l'identifiant.
   */
    public String getViewedName() {
        return shownName_ == null ? idName_ : shownName_;
    }

    public int hashCode() {
        return idName_.hashCode();
    }

    public boolean isBatchMode() {
        return batchMode_;
    }

    public boolean isIconInterne() {
        return iconURL_.startsWith(INTERN_PREFIX);
    }

    public void reloadFromPref() {
        final String prefixe = DodicoPreferences.buildPrefKey(KEY_PREFIXE, getIDName(), true);
        String s = PREF.getStringProperty(prefixe + EXE, null);
        if (s != null) {
            setExecCommand(s);
        }
        s = PREF.getStringProperty(prefixe + ICON, null);
        if (s != null) {
            setIconURL(s);
        }
        s = PREF.getStringProperty(prefixe + NAME, null);
        if (s != null) {
            setShownName(s);
        }
        batchMode_ = PREF.getBooleanProperty(prefixe + BATCH, false);
        args_ = PREF.getStringProperty(prefixe + ARGS, null);
    }

    /**
   * Permet de sauvegarder les donnees de cet exe dans les preferences <code>_pref</code>. La cle commencera par
   * <code>_idPref._ex.getIDname()</code>.
   */
    public void savePref() {
        final String prefixe = DodicoPreferences.buildPrefKey(KEY_PREFIXE, getIDName(), true);
        PREF.putStringProperty(prefixe + EXE, getExecCommand());
        String s = getIconURL();
        if (s != null) {
            PREF.putStringProperty(prefixe + ICON, s);
        } else {
            PREF.removeProperty(prefixe + ICON);
        }
        s = getShownName();
        if (s != null) {
            PREF.putStringProperty(prefixe + NAME, s);
        } else {
            PREF.removeProperty(prefixe + NAME);
        }
        s = getArgs();
        if (s != null) {
            PREF.putStringProperty(prefixe + ARGS, s);
        } else {
            PREF.removeProperty(prefixe + ARGS);
        }
        if (isBatchMode()) {
            PREF.putBooleanProperty(prefixe + BATCH, true);
        } else {
            PREF.removeProperty(prefixe + BATCH);
        }
    }

    public void setArgs(final String _args) {
        args_ = _args;
    }

    public void setBatchMode(final boolean _batchMode) {
        batchMode_ = _batchMode;
    }

    public void setExecCommand(final String _string) {
        if (_string != null) {
            execCommand_ = _string;
        }
    }

    public void setIconURL(final String _iconURL) {
        if ((_iconURL == null) || (_iconURL.length() == 0)) {
            iconURL_ = CtuluLibString.EMPTY_STRING;
            icon_ = EMPTY_TOOL_ICON;
        } else {
            if (!_iconURL.equals(iconURL_)) {
                iconURL_ = _iconURL;
                icon_ = getIcon(iconURL_);
            }
        }
    }

    public void setShownName(final String _string) {
        if (_string == null) {
            shownName_ = idName_;
        } else if (!_string.equals(shownName_)) {
            shownName_ = _string;
        }
    }
}
