package com.iver.cit.gvsig.gui.panels;

import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.cresques.cts.IProjection;
import org.gvsig.gui.beans.swing.JButton;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.gui.panels.crs.CrsUIFactory;
import com.iver.cit.gvsig.gui.panels.crs.ICrsUIFactory;
import com.iver.cit.gvsig.gui.panels.crs.ISelectCRSButton;
import com.iver.cit.gvsig.gui.panels.crs.ISelectCrsPanel;
import com.iver.cit.gvsig.project.documents.view.info.gui.CSSelectionDialog;

/**
 * 
 * @author Luis W. Sevilla <sevilla_lui@gva.es>
 */
public abstract class CRSSelectPanel extends JPanel implements ISelectCRSButton {

    private static Class panelClass = ProjChooserPanel.class;

    private static Class uiFactory = CrsUIFactory.class;

    private boolean transPanelActive = false;

    protected ActionListener actionListener = null;

    public static void registerPanelClass(Class panelClass) {
        CRSSelectPanel.panelClass = panelClass;
    }

    public static void registerUIFactory(Class uiFactory) {
        CRSSelectPanel.uiFactory = uiFactory;
    }

    public static CRSSelectPanel getPanel(IProjection proj) {
        CRSSelectPanel panel = null;
        Class[] args = { IProjection.class };
        Object[] params = { proj };
        try {
            panel = (CRSSelectPanel) panelClass.getConstructor(args).newInstance(params);
        } catch (IllegalArgumentException e) {
            PluginServices.getLogger().error("Error creating CRS selection button", e);
        } catch (SecurityException e) {
            PluginServices.getLogger().error("Error creating CRS selection button", e);
        } catch (InstantiationException e) {
            PluginServices.getLogger().error("Error creating CRS selection button", e);
        } catch (IllegalAccessException e) {
            PluginServices.getLogger().error("Error creating CRS selection button", e);
        } catch (InvocationTargetException e) {
            PluginServices.getLogger().error("Error creating CRS selection button", e);
        } catch (NoSuchMethodException e) {
            PluginServices.getLogger().error("Error creating CRS selection button", e);
        }
        return panel;
    }

    public CRSSelectPanel(IProjection proj) {
        super();
    }

    public abstract JButton getJBtnChangeProj();

    public abstract JLabel getJLabel();

    public abstract IProjection getCurProj();

    public abstract boolean isOkPressed();

    /**
	 * @param actionListener The actionListener to set.
	 */
    public void addActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public boolean isTransPanelActive() {
        return transPanelActive;
    }

    public void setTransPanelActive(boolean transPanelActive) {
        this.transPanelActive = transPanelActive;
    }

    public static ICrsUIFactory getUIFactory() {
        ICrsUIFactory factory;
        try {
            factory = (ICrsUIFactory) uiFactory.newInstance();
        } catch (InstantiationException e) {
            PluginServices.getLogger().error("Error creating CRS UI factory. Switching to default factory", e);
            factory = new CrsUIFactory();
        } catch (IllegalAccessException e) {
            PluginServices.getLogger().error("Error creating CRS UI factory. Switching to default factory", e);
            factory = new CrsUIFactory();
        }
        return factory;
    }
}
