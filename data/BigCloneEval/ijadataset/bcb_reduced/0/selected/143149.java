package com.magicpwd.e.tray;

import com.magicpwd.__a.tray.ATrayAction;
import com.magicpwd._cons.LangRes;
import com.magicpwd._util.Lang;
import com.magicpwd._util.Logs;

/**
 *
 * @author Amon
 */
public class HelpAction extends ATrayAction {

    public HelpAction() {
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            Lang.showMesg(trayPtn.getMpwdPtn(), LangRes.P30F7A0F, "");
        }
        java.io.File help = new java.io.File("help", "index.html");
        if (!help.exists()) {
            Lang.showMesg(trayPtn.getMpwdPtn(), LangRes.P30F7A10, "");
            return;
        }
        try {
            java.awt.Desktop.getDesktop().browse(help.toURI());
        } catch (java.io.IOException exp) {
            Logs.exception(exp);
        }
    }

    @Override
    public void doInit(String value) {
    }

    @Override
    public void reInit(javax.swing.AbstractButton button, String value) {
    }
}
