package fr.insa.rennes.pelias.pcreator.views;

import java.util.List;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import fr.insa.rennes.pelias.framework.Chain;
import fr.insa.rennes.pelias.pcreator.Application;
import fr.insa.rennes.pelias.pcreator.action.NewChainAction;
import fr.insa.rennes.pelias.pcreator.action.OpenChainAction;
import fr.insa.rennes.pelias.pcreator.editors.ChainEditor;
import fr.insa.rennes.pelias.pcreator.editors.ChainEditorInput;
import fr.insa.rennes.pelias.pcreator.wizards.NewChainWizard;
import fr.insa.rennes.pelias.platform.ISxSRepository;
import fr.insa.rennes.pelias.platform.PObjectNotFoundException;
import fr.insa.rennes.pelias.platform.PObjectReference;
import fr.insa.rennes.pelias.platform.PSxSObjectReference;

/**
 * 
 * @author otilia damian
 *
 */
public class ChainNavigator extends Navigator<Object> {

    public static final String ID = "fr.insa.rennes.pelias.pcreator.views.chains";

    /**
	 * Action de création d'une chaîne
	 */
    private Action newChainAction = null;

    /**
	 * Action de suppression d'une chaîne
	 */
    private Action deleteChainAction = null;

    /**
	 * Action de sauvegarde d'une chaîne
	 */
    private Action saveChainAction = null;

    private IAdapterFactory adapterFactory = new PCreatorAdapterFactory() {
    };

    public ChainNavigator() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        getTreeViewer().setInput(PCreatorAdapterFactory.chainROOT);
        getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                OpenChainAction openAction = new OpenChainAction();
                openAction.init(getSite().getWorkbenchWindow());
                openAction.selectionChanged(openAction, (IStructuredSelection) event.getSelection());
                openAction.run(openAction);
            }
        });
        getTreeViewer().refresh();
        Platform.getAdapterManager().registerAdapters(adapterFactory, PObjectReference.class);
        createActions();
        hookContextMenu();
    }

    public void dispose() {
        Platform.getAdapterManager().unregisterAdapters(adapterFactory);
        super.dispose();
    }

    public void setFocus() {
        IContextService contextService = (IContextService) getSite().getService(IContextService.class);
        contextService.activateContext("fr.insa.rennes.pelias.pcreator.chainNavigatorContext");
    }

    /**
	 * Méthode qui crée le menu contextuel
	 */
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                ChainNavigator.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(getTreeViewer().getControl());
        getTreeViewer().getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, getTreeViewer());
    }

    /**
	 * Méthode qui remplit le menu contextuel
	 * @param manager
	 */
    private void fillContextMenu(IMenuManager manager) {
        manager.add(newChainAction);
        manager.add(deleteChainAction);
        manager.add(saveChainAction);
    }

    /**
	 * Méthode qui crée et initialise toutes les actions
	 */
    private void createActions() {
        newChainAction = new Action() {

            public void run() {
                Wizard wizard = new NewChainWizard();
                IWorkbench workbench = PlatformUI.getWorkbench();
                IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
                WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                dialog.open();
            }

            ;

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        newChainAction.setText("Nouvelle Chaine");
        newChainAction.setToolTipText("Nouvelle Chaine tool tip");
        deleteChainAction = new Action() {

            public void run() {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
                IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
                if (selection.getFirstElement() instanceof PObjectReference) {
                    if (((PObjectReference) selection.getFirstElement()).getReferencedClass() == Chain.class) {
                        if (selection.getFirstElement() instanceof PSxSObjectReference) {
                            PSxSObjectReference ref = (PSxSObjectReference) selection.getFirstElement();
                            MessageBox box = new MessageBox(window.getShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
                            box.setMessage("Etes vous sûr de vouloir supprimer \"" + ref.getLabel() + " " + ref.getVersion() + "\" ?");
                            box.setText("Confirmation de suppression de chaine");
                            int res = box.open();
                            if (res == SWT.NO) {
                                return;
                            }
                            boolean reponse = avertissementChaineUtilise(ref);
                            if (reponse) {
                                Chain suppr = ref.resolve(Application.getCurrentChainRepository(), false, false, false);
                                actualiseEditeursChaines(suppr);
                                Application.getCurrentChainRepository().removeObject(ref.getId(), ref.getVersion());
                                IWorkbenchPage page = window.getActivePage();
                                IEditorReference[] tabEditorsRefs = page.getEditorReferences();
                                for (IEditorReference refEditor : tabEditorsRefs) {
                                    IEditorPart editor = refEditor.getEditor(false);
                                    if (editor instanceof ChainEditor) {
                                        Chain c = ((ChainEditor) editor).getChain();
                                        if (c.getId().equals(ref.getId()) && c.getVersion().equals(ref.getVersion())) {
                                            page.closeEditor(editor, false);
                                        }
                                    }
                                }
                                System.out.println("PCREATOR - Suppression de la chaine \"" + ref.getLabel() + " " + ref.getVersion() + "\"");
                            }
                        } else {
                            PObjectReference ref = (PObjectReference) selection.getFirstElement();
                            MessageBox box = new MessageBox(window.getShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
                            box.setMessage("Etes vous sûr de vouloir supprimer toutes les versions de \"" + ref.getLabel() + "\" ?");
                            box.setText("Confirmation de suppression de chaine");
                            int res = box.open();
                            if (res == SWT.NO) {
                                return;
                            }
                            List<PSxSObjectReference> listeVersion = Application.getCurrentChainRepository().enumerateObjectVersions(ref.getId());
                            for (PSxSObjectReference reference : listeVersion) {
                                boolean reponse = avertissementChaineUtilise(reference);
                                if (reponse) {
                                    Chain suppr = reference.resolve(Application.getCurrentChainRepository(), false, false, false);
                                    actualiseEditeursChaines(suppr);
                                    Application.getCurrentChainRepository().removeObject(reference.getId(), reference.getVersion());
                                    IWorkbenchPage page = window.getActivePage();
                                    IEditorReference[] tabEditorsRefs = page.getEditorReferences();
                                    for (IEditorReference refEditor : tabEditorsRefs) {
                                        IEditorPart editor = refEditor.getEditor(false);
                                        if (editor instanceof ChainEditor) {
                                            Chain c = ((ChainEditor) editor).getChain();
                                            if (c.getId().equals(reference.getId()) && c.getVersion().equals(reference.getVersion())) {
                                                page.closeEditor(editor, false);
                                            }
                                        }
                                    }
                                }
                            }
                            System.out.println("PCREATOR - Suppression de toutes les versions de la chaine \"" + ref.getLabel() + "\"");
                        }
                        ((ChainNavigator) window.getActivePage().findView(ChainNavigator.ID)).getTreeViewer().refresh();
                    }
                }
            }

            private boolean avertissementChaineUtilise(PSxSObjectReference ref) {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
                Chain chaine = ref.resolve(Application.getCurrentChainRepository(), false, false);
                if (chaine == null) {
                    MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK | SWT.ICON_ERROR);
                    messageBox.setText("Erreur");
                    messageBox.setMessage("Impossible de supprimer la chaine car celle-ci n'existe plus.");
                    messageBox.open();
                    return false;
                }
                try {
                    List<PObjectReference> list = Application.getCurrentChainRepository().getObjectRegisteredConsumers(chaine.getId(), chaine.getVersion(), false);
                    if (list.size() > 0) {
                        MessageBox box = new MessageBox(window.getShell(), SWT.YES | SWT.NO | SWT.ICON_INFORMATION);
                        String listChain = "";
                        for (PObjectReference reference : list) {
                            Chain c = reference.resolve(Application.getCurrentChainRepository(), false, false);
                            if (c != null) listChain += c.getLabel() + " " + c.getVersion() + "\n";
                        }
                        box.setMessage("La chaîne \"" + chaine.getLabel() + " " + chaine.getVersion() + "\" est utilisée par les chaînes suivantes :\n\n" + listChain + "\nÊtes-vous sûr de vouloir continuer ?  (si oui, il est préférable d'éditer à nouveau ces chaînes pour éviter tout conflit)");
                        box.setText("Avertissement de chaîne utilisée");
                        int res = box.open();
                        if (res == SWT.NO) {
                            return false;
                        }
                        return true;
                    }
                } catch (PObjectNotFoundException e) {
                    System.out.println("Aucune chaîne n'utilise ce service");
                }
                return true;
            }

            public void actualiseEditeursChaines(Chain oldChain) {
                try {
                    List<PObjectReference> chaineAMAJ = Application.getCurrentChainRepository().getObjectRegisteredConsumers(oldChain.getId(), oldChain.getVersion(), false);
                    if (chaineAMAJ.size() > 0) {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IEditorReference[] tabEditorsRefs = page.getEditorReferences();
                        for (IEditorReference refEditor : tabEditorsRefs) {
                            IEditorPart editor = refEditor.getEditor(false);
                            if (editor instanceof ChainEditor) {
                                Chain c = ((ChainEditor) editor).getChain();
                                for (PObjectReference ref : chaineAMAJ) {
                                    Chain dep = ref.resolve(Application.getCurrentChainRepository(), false, false);
                                    if (dep != null && dep.getId().equals(c.getId()) && dep.getVersion().equals(c.getVersion())) {
                                        System.out.println("Cet éditeur " + editor.getTitle() + " utilise notre chaine");
                                        ((ChainEditor) editor).initializeGraphicalViewer();
                                    }
                                }
                            }
                        }
                    }
                } catch (PObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        deleteChainAction.setText("Supprimer Chaine");
        deleteChainAction.setToolTipText("Supprimer Chaine tool tip");
        saveChainAction = new Action() {

            public void run() {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
                IEditorPart editeurActif = window.getActivePage().getActiveEditor();
                if (editeurActif instanceof ChainEditor) {
                    if (((ChainEditor) editeurActif).testServiceSeul()) {
                        javax.swing.JOptionPane.showMessageDialog(null, "Attention - L'un des services présent dans la chaîne dispose d'une entrée reliée à aucune entité (entrée de chaîne ou service).\n Veuillez corriger ce problème afin de pouvoir sauvegarder.");
                        return;
                    }
                    ISxSRepository<Chain> oldRepository = ((ChainEditor) editeurActif).getChainRepository();
                    boolean major = ((ChainEditor) editeurActif).getChainModel().getMajor();
                    boolean minor = ((ChainEditor) editeurActif).getChainModel().getMinor();
                    boolean dirty = ((ChainEditor) editeurActif).getChainModel().isDirty();
                    if (major || minor || dirty) {
                        editeurActif.doSave(null);
                        if (major || minor) {
                            Chain nouvelleChaine = ((ChainEditor) editeurActif).getChaineModifie();
                            window.getActivePage().closeEditor(editeurActif, false);
                            try {
                                if (Application.getCurrentChainRepository() == oldRepository) {
                                    window.getActivePage().openEditor(new ChainEditorInput(nouvelleChaine.getSelfSxSReference()), ChainEditor.ID);
                                } else {
                                    if (Application.isConnected()) {
                                        System.out.println("PCREATOR - Veuillez vous déconnecter pour ouvrir la nouvelle chaine");
                                        MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK | SWT.ICON_INFORMATION);
                                        messageBox.setText("Information");
                                        messageBox.setMessage("Nouvelle chaine créée avec succès. Veuillez vous déconnecter pour l'ouvrir.");
                                        messageBox.open();
                                    } else {
                                        System.out.println("PCREATOR - Veuillez vous connecter pour ouvrir la nouvelle chaine");
                                        MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK | SWT.ICON_INFORMATION);
                                        messageBox.setText("Information");
                                        messageBox.setMessage("Nouvelle chaine créée avec succès. Veuillez vous connecter pour l'ouvrir.");
                                        messageBox.open();
                                    }
                                }
                            } catch (PartInitException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("PCREATOR - Aucune modification à sauvegarder");
                    }
                }
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        saveChainAction.setText("Enregistrer Chaine");
        saveChainAction.setToolTipText("Enregistrer Chaine tool tip");
    }
}
