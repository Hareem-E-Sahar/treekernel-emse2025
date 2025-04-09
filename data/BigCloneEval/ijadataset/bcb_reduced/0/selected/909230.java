package org.xaware.ide.xadev.wizard;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Element;
import org.xaware.ide.shared.UserPrefs;
import org.xaware.ide.xadev.XA_Designer_Plugin;
import org.xaware.ide.xadev.datamodel.JDOMContentFactory;
import org.xaware.ide.xadev.datamodel.XMLTreeNode;
import org.xaware.ide.xadev.gui.XADialog;
import org.xaware.ide.xadev.gui.XADialogOperation;
import org.xaware.shared.util.logging.XAwareLogger;

/**
 * WizardFactory class loads the wizard definitions from configuration file and create the wizards from the loaded
 * definitions.
 * 
 * @author Srinivas Ch
 * @version 1.0
 */
public class WizardFactory {

    /** XAwareLogger instance */
    private static final XAwareLogger logger = XAwareLogger.getXAwareLogger(WizardFactory.class.getName());

    /** Templates directory path */
    public static final String TEMPLATE_DIR = "/templates";

    /** WizardFactory instance */
    private static WizardFactory defaultInstance;

    /** Wizard definitions hashtable */
    private final Hashtable wizardDefs;

    /** available wizards array */
    private String[] availWizArray;

    /** wizard type Vs wizard definitions hashtable */
    private Hashtable typeVsDefVector;

    /** Template wizard definitions hashtable */
    private final Hashtable templateWizardDefs;

    /** Template Type Vs Template Definitions hashtable */
    private Hashtable templateTypeVsDefVector;

    /** Instance of WizardController */
    private WizardController controller;

    /** Boolean instance representing stand-alone BizComponent */
    private boolean isStandAloneBizComponent;

    /**
     * Creates a new WizardFactory object.
     */
    public WizardFactory() {
        wizardDefs = new Hashtable();
        typeVsDefVector = new Hashtable();
        templateWizardDefs = new Hashtable();
        templateTypeVsDefVector = new Hashtable();
    }

    /**
     * Creates a new WizardFactory object.
     * 
     * @param inDefs
     *            Wizard Definitions Vector
     * 
     * @throws WizardException
     *             WizardException
     */
    public WizardFactory(final Vector inDefs) throws WizardException {
        typeVsDefVector = new Hashtable();
        wizardDefs = new Hashtable();
        templateWizardDefs = new Hashtable();
        templateTypeVsDefVector = new Hashtable();
        final Iterator itr = inDefs.iterator();
        while (itr.hasNext()) {
            final WizardDefinition def = (WizardDefinition) itr.next();
            if (wizardDefs.get(def.getName()) != null) {
                throw new WizardException("WizardDefinition : " + def.getName() + " already defined.");
            }
            final String curType = def.getType();
            Vector typeVec = (Vector) typeVsDefVector.get(curType);
            if (typeVec == null) {
                typeVec = new Vector();
                typeVsDefVector.put(curType, typeVec);
            }
            typeVec.add(def.getName());
            wizardDefs.put(def.getName(), def);
        }
    }

    /**
     * Creates a new WizardFactory object.
     * 
     * @param definitionsNode
     *            Wizard Definitions XML Tree Element
     * 
     * @throws WizardException
     *             WizardException
     */
    public WizardFactory(final Element definitionsNode) {
        wizardDefs = new Hashtable();
        typeVsDefVector = new Hashtable();
        templateWizardDefs = new Hashtable();
        templateTypeVsDefVector = new Hashtable();
        final Iterator itr = definitionsNode.getChildren(WizardConstants.WIZARD_DEFINITION).iterator();
        while (itr.hasNext()) {
            final Element defElem = (Element) itr.next();
            try {
                final WizardDefinition def = new WizardDefinition(defElem);
                final String curType = def.getType();
                Vector typeVec = (Vector) typeVsDefVector.get(curType);
                if (typeVec == null) {
                    typeVec = new Vector();
                    typeVsDefVector.put(curType, typeVec);
                }
                typeVec.add(def.getName());
                wizardDefs.put(def.getName(), def);
            } catch (final Exception e) {
                logger.warning("Exception reading wizard def : " + defElem.getName());
                logger.warning("Exception : " + e);
            }
        }
        try {
            final Iterator itr1 = definitionsNode.getChildren(WizardConstants.TEMPLATE).iterator();
            while (itr1.hasNext()) {
                final Element defElem = (Element) itr1.next();
                try {
                    final WizardDefinition def = new WizardDefinition(defElem);
                    final String curType = def.getType();
                    Vector typeVec = (Vector) templateTypeVsDefVector.get(curType);
                    if (typeVec == null) {
                        typeVec = new Vector();
                        templateTypeVsDefVector.put(curType, typeVec);
                    }
                    typeVec.add(def.getName());
                    templateWizardDefs.put(def.getName(), def);
                } catch (final Exception e) {
                    logger.warning("Exception reading wizard def : " + defElem.getName());
                    logger.warning("Exception : " + e);
                }
            }
        } catch (final Exception e) {
            logger.warning("Exception creating template wizards : " + e);
        }
    }

    /**
     * Returns the WizardFactory instance
     * 
     * @return WizardFactory instance
     * 
     * @throws WizardException
     *             WizardException
     */
    public static WizardFactory getDefaultInstance() throws WizardException {
        if (defaultInstance == null) {
            defaultInstance = new WizardFactory(UserPrefs.getWizardDefsElement());
        }
        return defaultInstance;
    }

    /**
     * Returns The Wizard object created from the definition node and input data
     * 
     * @param parent
     *            parent shell
     * @param definitionName
     *            wizard definition name
     * @param inputData
     *            input data
     * 
     * @return Wizard instance
     * 
     * @throws WizardException
     *             WizardException
     */
    public Wizard createWizard(final Shell parent, final String definitionName, final Object inputData) throws WizardException {
        WizardDefinition def = (WizardDefinition) wizardDefs.get(definitionName);
        if (def == null) {
            def = (WizardDefinition) templateWizardDefs.get(definitionName);
        }
        controller = new WizardController(def, inputData);
        Wizard theWiz = null;
        try {
            final Class[] argTypes = { Shell.class, WizardController.class, WizardDefinition.class };
            final Object[] args = { parent, controller, def };
            theWiz = (Wizard) def.getWizardClass().getConstructor(argTypes).newInstance(args);
        } catch (final Exception e) {
            throw new WizardException("Error creating new Wizard instance : " + e.toString());
        }
        return theWiz;
    }

    /**
     * Returns the Enumeration of available wizards
     * 
     * @param type
     *            BizComponent type or Template type
     * 
     * @return Enumeration object
     */
    public Enumeration availableWizards(final String type) {
        return availableWizards(type, false);
    }

    /**
     * Returns the object array of the available wizards
     * 
     * @param type
     *            BizComponent type or Template type
     * 
     * @return Array of Objects
     */
    public Object[] availableWizardsAsArray(final String type) {
        return availableWizardsAsArray(type, false);
    }

    /**
     * Returns the Enumeration of available wizards with Templates
     * 
     * @param type
     *            BizComponent type or Template type
     * @param withTemplates
     *            withTemplates boolean
     * 
     * @return Enumeration object
     */
    public Enumeration availableWizards(final String type, final boolean withTemplates) {
        if (withTemplates) {
            final Vector allWizards = new Vector();
            if (type.equals(WizardDefinition.ALL)) {
                allWizards.addAll(templateWizardDefs.keySet());
                allWizards.addAll(wizardDefs.keySet());
            } else {
                if ((Vector) templateTypeVsDefVector.get(type) != null) {
                    allWizards.addAll((Vector) templateTypeVsDefVector.get(type));
                }
                if ((Vector) typeVsDefVector.get(type) != null) {
                    allWizards.addAll((Vector) typeVsDefVector.get(type));
                }
            }
            return allWizards.elements();
        } else {
            if (type.equals(WizardDefinition.ALL)) {
                return wizardDefs.keys();
            } else {
                Vector tmp = (Vector) typeVsDefVector.get(type);
                if (tmp == null) {
                    tmp = new Vector();
                }
                return tmp.elements();
            }
        }
    }

    /**
     * Returns the object array of the available wizards
     * 
     * @param type
     *            BizComponent type or Template type
     * @param withTemplates
     *            withTemplates boolean
     * 
     * @return Array of Objects
     */
    public Object[] availableWizardsAsArray(final String type, final boolean withTemplates) {
        if (withTemplates) {
            if (type.equals(WizardDefinition.ALL)) {
                availWizArray = new String[wizardDefs.size() + templateWizardDefs.size()];
                Enumeration e = wizardDefs.keys();
                int i = 0;
                while (e.hasMoreElements()) {
                    availWizArray[i] = (String) e.nextElement();
                    i++;
                }
                e = templateWizardDefs.keys();
                while (e.hasMoreElements()) {
                    availWizArray[i] = (String) e.nextElement();
                    i++;
                }
                return availWizArray;
            } else {
                final Vector defs = new Vector();
                if ((Vector) typeVsDefVector.get(type) != null) {
                    defs.addAll((Vector) typeVsDefVector.get(type));
                }
                if ((Vector) templateTypeVsDefVector.get(type) != null) {
                    defs.addAll((Vector) templateTypeVsDefVector.get(type));
                }
                return defs.toArray();
            }
        } else {
            if (type.equals(WizardDefinition.ALL)) {
                availWizArray = new String[wizardDefs.size()];
                final Enumeration e = wizardDefs.keys();
                int i = 0;
                while (e.hasMoreElements()) {
                    availWizArray[i] = (String) e.nextElement();
                    i++;
                }
                return availWizArray;
            } else {
                final Vector defs = (Vector) typeVsDefVector.get(type);
                if (defs == null) {
                    return new String[0];
                } else {
                    return defs.toArray();
                }
            }
        }
    }

    /**
     * For use in debugging only.returns all the WizardDefinitions as string
     * 
     * @return String
     */
    @Override
    public String toString() {
        String retVal = "WizardFactory, WizardDefinitions : \n";
        final Enumeration e = availableWizards(WizardDefinition.ALL);
        while (e.hasMoreElements()) {
            retVal = ((WizardDefinition) wizardDefs.get(e.nextElement())).toString();
        }
        return retVal;
    }

    /**
     * For use in debugging only
     * 
     * @param args
     *            Arguments array
     * 
     * @throws Exception
     *             Exception
     */
    public static void main(final String[] args) throws Exception {
        final Composite composite = new Composite(XA_Designer_Plugin.getShell(), SWT.NONE);
        composite.setLayout(new GridLayout());
        final WizardFactory wf = WizardFactory.getDefaultInstance();
        final Object[] availableWizards = wf.availableWizardsAsArray(WizardDefinition.BIZ_COMP);
        if (availableWizards.length == 0) {
            logger.info("No available wizards of type : " + WizardDefinition.BIZ_COMP);
            return;
        }
        final List wizList = new List(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        for (int i = 0; i < availableWizards.length; i++) {
            wizList.add(availableWizards[i].toString());
        }
        wizList.setLayoutData(new GridData(GridData.FILL_BOTH));
        final XADialog dialog = new XADialog(XA_Designer_Plugin.getShell(), new XADialogOperation() {

            public boolean okPressed() {
                System.out.println("Selected Biz Component: " + wizList.getItem(wizList.getSelectionIndex()));
                int selIndex = wizList.getSelectionIndex();
                XMLTreeNode globalInputNode = new XMLTreeNode(JDOMContentFactory.createJDOMContent(new Element("JUNK")));
                try {
                    Wizard theWiz = wf.createWizard(XA_Designer_Plugin.getShell(), wizList.getItem(selIndex), globalInputNode);
                    theWiz.open();
                } catch (Exception e) {
                    (XAwareLogger.getXAwareLogger(getClass().toString())).printStackTrace(e);
                }
                return true;
            }

            public boolean cancelPressed() {
                return true;
            }
        }, composite, "Select BizComponent: ", true, true);
        dialog.open();
    }

    /**
     * Returns the instance of WizardController
     * 
     * @return instance of WizardController
     */
    public WizardController getController() {
        return controller;
    }

    /**
     * Returns the flag element indicating if the Traget is from Document or StandaloneComponent
     * 
     * @return flag value
     */
    public boolean isBizComponentStandAlone() {
        return isStandAloneBizComponent;
    }

    /**
     * Sets the value of the flag variable.
     * 
     * @param isTargetTreeFromBizComponent
     *            indicates if the Traget is from Document or StandaloneComponent
     */
    public void setBizComponentStandAlone(final boolean _isStandAloneBizComponent) {
        this.isStandAloneBizComponent = _isStandAloneBizComponent;
    }
}
