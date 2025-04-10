package org.openscience.jmol.app;

import org.openscience.jmol.*;
import org.openscience.jmol.io.SharcReader;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.AbstractAction;

/**
 *  @author  Bradley A. Smith (bradley@baysmith.com)
 */
class CalculateChemicalShifts extends AbstractAction implements PropertyChangeListener {

    ChemFile chemFile;

    AtomPropsMenu propertiesMenu;

    Hashtable shieldings;

    SelectSharcReference dialog;

    static String RF = "org/openscience/jmol/Data/refs_c4h12si1_data.sharc";

    static final String propertyLabel = "Chemical shift";

    public CalculateChemicalShifts() {
        super("chemicalShifts");
        setEnabled(false);
    }

    public void initialize(AtomPropsMenu menu) {
        propertiesMenu = menu;
        if (shieldings == null) {
            try {
                URL url = this.getClass().getClassLoader().getResource(RF);
                InputStreamReader isr = new InputStreamReader(url.openStream());
                BufferedReader br = new BufferedReader(isr);
                SharcReader sr1 = new SharcReader(br);
                shieldings = new Hashtable();
                while (sr1.hasNext()) {
                    SharcShielding ss1 = sr1.next();
                    shieldings.put(ss1.getMethod(), ss1);
                }
                String[] shieldingNames = new String[shieldings.size()];
                int i = 0;
                Enumeration k = shieldings.keys();
                while (k.hasMoreElements()) {
                    shieldingNames[i] = (String) k.nextElement();
                    i++;
                }
                dialog = new SelectSharcReference(null, shieldingNames, true);
            } catch (Exception ex) {
                shieldings = null;
            }
        }
    }

    private void setChemFile(ChemFile file) {
        setEnabled(false);
        chemFile = file;
        if (chemFile == null) {
            return;
        }
        if (shieldings == null) {
            return;
        }
        boolean foundShielding = false;
        for (int frameIndex = 0; !foundShielding && (frameIndex < chemFile.getNumberOfFrames()); ++frameIndex) {
            ChemFrame frame = chemFile.getFrame(frameIndex);
            for (int atomIndex = 0; !foundShielding && (atomIndex < frame.getAtomCount()); ++atomIndex) {
                Atom atom = frame.getJmolAtomAt(atomIndex);
                Vector properties = atom.getAtomicProperties();
                for (int propertyIndex = 0; !foundShielding && (propertyIndex < properties.size()); ++propertyIndex) {
                    if (properties.elementAt(propertyIndex) instanceof NMRShielding) {
                        foundShielding = true;
                    }
                }
            }
        }
        setEnabled(foundShielding);
    }

    public void actionPerformed(ActionEvent e) {
        dialog.show();
        SharcShielding referenceShielding = (SharcShielding) shieldings.get(dialog.getValue());
        if (referenceShielding == null) {
            return;
        }
        for (int f = 0; f < chemFile.getNumberOfFrames(); ++f) {
            ChemFrame frame = chemFile.getFrame(f);
            for (int i = 0; i < frame.getAtomCount(); ++i) {
                String element = frame.getAtomAt(i).getAtomTypeName();
                Vector properties = (frame.getJmolAtomAt(i)).getAtomicProperties();
                Enumeration propIter = properties.elements();
                while (propIter.hasMoreElements()) {
                    Object prop = propIter.nextElement();
                    if (prop instanceof NMRShielding) {
                        NMRShielding shield1 = (NMRShielding) prop;
                        double value = ((Double) shield1.getProperty()).doubleValue();
                        value -= referenceShielding.getShielding(element);
                        NMRShielding newShield = new NMRShielding(value);
                        newShield.descriptor = propertyLabel;
                        (frame.getJmolAtomAt(i)).addProperty(newShield);
                        break;
                    }
                }
            }
        }
        Vector filePL = chemFile.getAtomPropertyList();
        if (filePL.indexOf(propertyLabel) < 0) {
            filePL.addElement(propertyLabel);
        }
        propertiesMenu.replaceList(chemFile.getAtomPropertyList());
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(DisplayControl.PROP_CHEM_FILE)) {
            setChemFile((ChemFile) event.getNewValue());
        }
    }
}
