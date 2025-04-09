package org.fudaa.fudaa.commun.impl;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import com.memoire.bu.BuApplication;
import com.memoire.bu.BuInformationsSoftware;
import com.memoire.bu.BuPreferences;
import com.memoire.fu.FuLib;
import org.fudaa.ctulu.CtuluLibString;
import org.fudaa.fudaa.commun.FudaaLib;
import org.fudaa.fudaa.commun.FudaaTee;

/**
 * Le main de base de Fudaa.
 * 
 * @version $Revision: 1.3 $ $Date: 2008-02-29 16:47:10 $ by $Author: opasteur $
 * @author Axel von Arnim
 */
public final class Fudaa {

    private final FudaaCommandLineParser flags_ = new FudaaCommandLineParser();

    private FudaaSplashScreen splash_;

    private File toOpen_;

    private boolean splashTextVisible_ = true;

    public boolean isDoNotUseFudaaTee() {
        return doNotUseFudaaTee_;
    }

    public void setDoNotUseFudaaTee(final boolean _doNotUseFudaaTee) {
        doNotUseFudaaTee_ = _doNotUseFudaaTee;
    }

    public boolean isApplyLanguage() {
        return applyLanguage_;
    }

    public void setSplashTextVisible(boolean b) {
        splashTextVisible_ = b;
    }

    public void setApplyLanguage(final boolean _applyLanguage) {
        applyLanguage_ = _applyLanguage;
    }

    public boolean isApplyLookAndFeel() {
        return applyLookAndFeel_;
    }

    public void setApplyLookAndFeel(final boolean _applyLookAndFeel) {
        applyLookAndFeel_ = _applyLookAndFeel;
    }

    private boolean applyLanguage_ = true;

    private boolean applyLookAndFeel_ = true;

    private boolean doNotUseFudaaTee_;

    private boolean addWelcomeMessage_ = true;

    public Fudaa() {
    }

    public FudaaCommandLineParser getFlags() {
        return flags_;
    }

    /**
   * @param _args les arguments
   * @param _soft le soft
   * @param _canOpenAFile true si l'appli peut ouvrir un fichier au demarrage.
   */
    public void launch(final String[] _args, final BuInformationsSoftware _soft, final boolean _canOpenAFile) {
        launch(_args, _soft, true, _canOpenAFile);
    }

    /**
   * @param _args les argument recu
   * @param _soft l'info du logiciel
   * @param _printFudaaVersion true si la version de fudaa doit etre ecrite sur la sortie standard
   * @param _canOpenAFile si true, le parametres non reconnu (le seul) est considere comme un fichier a ouvrir.
   */
    public void launch(final String[] _args, final BuInformationsSoftware _soft, final boolean _printFudaaVersion, boolean _canOpenAFile) {
        final String[] r = flags_.parse(_args);
        if ((!_canOpenAFile && (r.length > 0)) || (_canOpenAFile && (r.length > 1))) {
            System.err.println("The flag " + r[0] + " is unknown");
            System.err.println("Flags: " + flags_.flagTotalText());
            System.exit(1);
        }
        if (flags_.version_) {
            System.out.println("Fudaa-" + _soft.name + ". " + _soft.rights);
            System.out.println("version " + _soft.version + " - " + _soft.date);
            System.exit(0);
        }
        if (_canOpenAFile && (r.length == 1)) {
            toOpen_ = new File(r[0]);
            if (!toOpen_.exists()) {
                toOpen_ = null;
            }
        }
        if (isApplyLookAndFeel()) {
            BuPreferences.BU.applyLookAndFeel();
        }
        if (isApplyLanguage()) {
            BuPreferences.BU.applyLanguage(_soft.languages);
        }
        BuPreferences.BU.applyNetwork();
        final String ls = CtuluLibString.LINE_SEP;
        if (!doNotUseFudaaTee_ && !flags_.noLog_) {
            FudaaTee.createFudaaTee();
            if (addWelcomeMessage_) {
                String wlcmsg = CtuluLibString.EMPTY_STRING;
                final String line = "******************************************************************************";
                if ("fr".equals(Locale.getDefault().getLanguage())) {
                    wlcmsg = line + ls + "*                                 Bienvenue                                  " + ls + "*                                 ---------                                  " + ls + "* Ceci est la console texte. Elle affiche tous les messages systeme:         " + ls + "* erreurs, taches en cours. Consultez-la regulierement pour savoir           " + ls + "* si le programme est actif, si une erreur s'est produite, ...               " + ls + "* En cas d'erreur, joignez son contenu (enregistre dans le fichier ts.log)   " + ls + "* au mail de notification de bogue, ceci nous aidera a comprendre.           " + ls + line + ls + ls;
                } else {
                    wlcmsg = line + ls + "*                                  Welcome                                   " + ls + "*                                 ---------                                  " + ls + "* This a text console displaying system messages (errors and running tasks). " + ls + "* If an error occurs, please, send the file \"ts.log\" with your bug report. " + ls + line + ls + ls;
                }
                System.out.println(wlcmsg);
            }
        }
        if (_printFudaaVersion) {
            System.out.println("***************" + ls + "* Fudaa-" + _soft.name + ls + "* version " + _soft.version + " - " + _soft.date + ls + "* Date " + Calendar.getInstance().getTime().toString() + ls + "* java version:" + FuLib.getSystemProperty("java.version") + ls + "* Mem usable: " + Runtime.getRuntime().maxMemory() / 1048576 + " Mo" + ls + "***************");
        }
        if (!flags_.noSplash_ && FudaaStartupExitPreferencesPanel.isSplashActivated()) {
            splash_ = new FudaaSplashScreen(_soft, 2000, new String[0][0]);
            splash_.setVisible(splashTextVisible_);
            splash_.start();
        }
    }

    public void startApp(final FudaaCommonImplementation _impl) {
        startApp(_impl, null);
    }

    /**
   * Lance l'implementation. Si un fichier valide a ete passe en parametre ,il est ouvert avec la commande
   * FudaaCommonImplementation.cmdOuvrirFile
   * 
   * @param _impl l'impl a initialiser
   */
    public void startApp(final FudaaCommonImplementation _impl, final Class _application) {
        if (splash_ != null) {
            splash_.setProgression(60);
        }
        BuApplication app = null;
        if (_application == null) {
            app = new BuApplication();
        } else {
            try {
                app = (BuApplication) _application.getConstructors()[0].newInstance(new Object[0]);
            } catch (final Exception ex) {
                throw new IllegalArgumentException("application n'est pas une BuApplication");
            }
        }
        app.setImplementation(_impl);
        final String s = FudaaLib.getS("Initialisation") + "...";
        System.out.println(s);
        if (splash_ != null) {
            splash_.setText(s);
            splash_.setProgression(80);
        }
        app.init();
        if (splash_ != null) {
            splash_.setProgression(100);
            splash_.setVisible(false);
            splash_.dispose();
        }
        app.start();
        if (toOpen_ != null) {
            _impl.cmdOuvrirFile(toOpen_);
        }
    }

    public boolean isAddWelcomeMessage() {
        return addWelcomeMessage_;
    }

    public void setAddWelcomeMessage(final boolean _addWelcomeMessage) {
        addWelcomeMessage_ = _addWelcomeMessage;
    }
}
