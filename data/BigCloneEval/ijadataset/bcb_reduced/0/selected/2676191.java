package org.primordion.cellontro.io.sbml;

import org.primordion.xholon.io.ICellontro2Sbml;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import org.primordion.cellontro.base.BioXholonClass;
import org.primordion.cellontro.base.IBioXholon;
import org.primordion.cellontro.base.IBioXholonClass;
import org.primordion.xholon.base.IXholon;
import org.primordion.xholon.base.IXholonClass;
import org.primordion.xholon.base.Xholon;
import org.primordion.xholon.util.MiscIo;

/**
 * <p>Cellontro2Sbml transforms Xholon runtime models into SBML Level 2 files.
 * The exported file contains SBML compartments, species, and reactions.</p>
 * 
 * <p>TODO Do a better job of exporting partial Cellontro models to SBML. For partial models,
 * create an external compartment that's the actual root compartment. Any species
 * references that are outside the subtree being exported will have this as their compartment.
 * To implement this, possibly store species in a temporary data structure first, so that
 * additional external species can be added while processing the reactions.</p>
 * 
 * <p>TODO Be able to export the kinetics, in addition to the structure.</p>
 * 
 * <p>TODO [February 19, 2007]
 * Be able to export models that only implement IXholon and IXholonClass,
 * rather than requiring that they implement the interface extensions IBioXholon and IBioXholonClass.
 * The following incompatible methods are used in Cellontro2Sbml:
 *   getPheneVal()
 *   isReversible()
 *   getNumReactants()
 *   getNumProducts()
 *   getNumModifiers()
 *   IBioXholonClass.SIZE_ARRAY_GENEVAL
 * </p>
 * 
 * @author <a href="mailto:ken@primordion.com">Ken Webb</a>
 * @see <a href="http://www.primordion.com/Xholon">Xholon Project website</a>
 * @since 0.2 (Created on December 11, 2005)
 */
public class Cellontro2Sbml implements ICellontro2Sbml {

    protected static final String CMPT = "c";

    protected static final String SPEC = "s";

    protected static final String REAC = "r";

    protected static final String DUMMY_SPECIES = "DUMMY_000s";

    private String sbmlFileName;

    private Writer sbmlOut;

    private String modelName;

    private IXholon root;

    /**
	 * Is the root being exported the true outermost root of the Xholon composite structure tree?
	 * If it's not, then we need to be careful not to export anything not contained within this subtree.
	 */
    private boolean rootIsTrueRoot = true;

    private Date timeNow;

    /**
	 * Constructor.
	 */
    public Cellontro2Sbml() {
    }

    /**
	 * Constructor.
	 * @param sbmlFileName Name of the output SBML XML file.
	 * @param modelName Name of the model.
	 * @param root Root of the tree that will be written out.
	 */
    public Cellontro2Sbml(String sbmlFileName, String modelName, IXholon root) {
        initialize(sbmlFileName, modelName, root);
    }

    public boolean initialize(String sbmlFileName, String modelName, IXholon root) {
        if (IBioXholon.class.isAssignableFrom(root.getClass())) {
            this.sbmlFileName = sbmlFileName;
            this.modelName = modelName;
            this.root = root;
            rootIsTrueRoot = root.isRootNode() ? true : false;
            timeNow = new Date();
            return true;
        } else {
            return false;
        }
    }

    public void writeAll() {
        sbmlOut = MiscIo.openOutputFile(sbmlFileName);
        try {
            sbmlOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            sbmlOut.write("<!--\nAutomatically generated by Xholon (Cellontro) version 0.5, using Cellontro2Sbml.java\n" + new Date() + "\nwww.primordion.com/Xholon\n-->\n");
            sbmlOut.write("<sbml xmlns=\"http://www.sbml.org/sbml/level2\" level=\"2\" version=\"1\">\n");
            sbmlOut.write("<model id=\"" + modelName + "_" + timeNow.getTime() + "\" name=\"" + modelName + "\">\n");
            writeNotes();
            writeUnits();
            writeCompartments();
            writeSpecies();
            writeReactions();
            sbmlOut.write("</model>\n");
            sbmlOut.write("</sbml>\n");
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
        MiscIo.closeOutputFile(sbmlOut);
    }

    /**
	 * Write notes, in XHTML format.
	 */
    protected void writeNotes() {
        try {
            sbmlOut.write("<notes>\n");
            sbmlOut.write("<body xmlns=\"http://www.w3.org/1999/xhtml\">\n");
            sbmlOut.write("<p>");
            sbmlOut.write("This SBML file has been generated by an early version of the Cellontro SBML exporter.\n");
            sbmlOut.write("The file contains only structure, and no real kinetics.\n");
            sbmlOut.write("KineticLaw parameters are included, along with dummy plus functions\n");
            sbmlOut.write("that may make it easier for other software to read in the parameters.\n");
            sbmlOut.write("</p>");
            sbmlOut.write("</body>\n");
            sbmlOut.write("</notes>\n");
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
    }

    /**
	 * Write default units.
	 * In Cellontro, all species units are amounts of individual molecules.
	 */
    protected void writeUnits() {
        try {
            sbmlOut.write("\n<!-- Unit Definitions -->\n");
            sbmlOut.write("<listOfUnitDefinitions>\n");
            sbmlOut.write(" <unitDefinition id=\"volume\">\n");
            sbmlOut.write("  <listOfUnits>\n");
            sbmlOut.write("   <unit kind=\"litre\" scale=\"-3\" multiplier=\"1\" offset=\"0\"/>\n");
            sbmlOut.write("  </listOfUnits>\n");
            sbmlOut.write(" </unitDefinition>\n");
            sbmlOut.write(" <unitDefinition id=\"substance\">\n");
            sbmlOut.write("  <listOfUnits>\n");
            sbmlOut.write("   <unit kind=\"item\" exponent=\"1\"/>\n");
            sbmlOut.write("  </listOfUnits>\n");
            sbmlOut.write(" </unitDefinition>\n");
            sbmlOut.write("</listOfUnitDefinitions>\n");
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
    }

    /**
	 * Write out all SBML compartments.
	 * An SBML compartment corresponds to a Xholon container.
	 */
    protected void writeCompartments() {
        try {
            sbmlOut.write("\n<!-- Lists of Compartments -->\n");
            sbmlOut.write("<listOfCompartments>\n");
            sbmlOut.write("<compartment id=\"" + root.getName() + CMPT + "\" name=\"" + root.getXhcName() + "\"");
            sbmlOut.write(" size=\"1\"/>\n");
            if (root.getFirstChild() != null) {
                writeCompartment(root.getFirstChild());
            }
            sbmlOut.write("</listOfCompartments>\n");
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
    }

    /**
	 * Write out a single SBML compartment, and recursively write out all of its children and siblings.
	 * @param The current node in the tree.
	 */
    protected void writeCompartment(IXholon node) {
        if (!IBioXholon.class.isAssignableFrom(node.getClass())) {
            return;
        }
        try {
            if (node.hasChildNodes()) {
                sbmlOut.write("<compartment id=\"" + node.getName() + CMPT + "\" name=\"" + node.getXhcName() + "\"");
                sbmlOut.write(" outside=\"" + node.getParentNode().getName() + CMPT + "\"");
                sbmlOut.write(" size=\"1\"/>\n");
            }
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
        if (node.getFirstChild() != null) {
            writeCompartment(node.getFirstChild());
        }
        if (node.getNextSibling() != null) {
            writeCompartment(node.getNextSibling());
        }
    }

    /**
	 * Write out all SBML species.
	 * An SBML species corresponds to a Xholon passive object.
	 */
    protected void writeSpecies() {
        try {
            sbmlOut.write("\n<!-- Lists of Species -->\n");
            sbmlOut.write("<listOfSpecies>\n");
            writeSpecies(root);
            if (!rootIsTrueRoot) {
                sbmlOut.write("<species id=\"" + DUMMY_SPECIES + "\"");
                sbmlOut.write(" compartment=\"" + root.getName() + CMPT + "\"");
                sbmlOut.write(" initialAmount=\"" + "0.0" + "\"/>\n");
            }
            sbmlOut.write("</listOfSpecies>\n");
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
    }

    /**
	 * Write out a single SBML species, and recursively write out all of its children and siblings.
	 * @param The current node in the tree.
	 */
    protected void writeSpecies(IXholon node) {
        if (!IBioXholon.class.isAssignableFrom(node.getClass())) {
            return;
        }
        try {
            if ((node.getXhType() == IXholonClass.XhtypePurePassiveObject) || (node.getXhType() == IXholonClass.XhtypexxxFgsCon)) {
                sbmlOut.write("<species id=\"" + node.getName() + SPEC + "\" name=\"" + node.getXhcName() + "\"");
                if (!node.isRootNode()) {
                    sbmlOut.write(" compartment=\"" + node.getParentNode().getName() + CMPT + "\"");
                }
                sbmlOut.write(" initialAmount=\"" + ((IBioXholon) node).getPheneVal() + "\"/>\n");
            }
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
        if (node.getFirstChild() != null) {
            writeSpecies(node.getFirstChild());
        }
        if (node.getNextSibling() != null) {
            writeSpecies(node.getNextSibling());
        }
    }

    /**
	 * Write out all SBML reactions.
	 * An SBML reaction corresponds to a Xholon active object.
	 */
    protected void writeReactions() {
        try {
            sbmlOut.write("\n<!-- Lists of Reactions -->\n");
            sbmlOut.write("<listOfReactions>\n");
            writeReaction(root);
            sbmlOut.write("</listOfReactions>\n");
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
    }

    /**
	 * Write out a single SBML reaction, and recursively write out all of its children and siblings.
	 * @param The current node in the tree.
	 */
    protected void writeReaction(IXholon node) {
        if (!IBioXholon.class.isAssignableFrom(node.getClass())) {
            return;
        }
        int i, j;
        IXholon speciesRef;
        boolean foundOne = false;
        int numFound = 0;
        try {
            if (node.isActiveObject()) {
                sbmlOut.write("<!-- Reaction -->\n");
                sbmlOut.write("<reaction id=\"" + node.getName() + REAC + "\" name=\"" + node.getXhcName() + "\"");
                if (((IBioXholonClass) node.getXhc()).isReversible()) {
                    sbmlOut.write(" reversible=\"true\">\n");
                } else {
                    sbmlOut.write(" reversible=\"false\">\n");
                }
                int maxReactants = ((IBioXholon) node).getNumReactants();
                int maxProducts = ((IBioXholon) node).getNumProducts();
                int maxModifiers = ((IBioXholon) node).getNumModifiers();
                for (i = 0, j = 0; i < maxReactants; i++, j++) {
                    speciesRef = node.getPort(j);
                    if (speciesRef != null && (rootIsTrueRoot || (speciesRef.hasAncestor(root.getName())))) {
                        if (!foundOne) {
                            sbmlOut.write("<listOfReactants>\n");
                            foundOne = true;
                        }
                        sbmlOut.write("<speciesReference species=\"" + speciesRef.getName() + SPEC + "\"/>\n");
                        numFound++;
                    }
                }
                if (foundOne) {
                    sbmlOut.write("</listOfReactants>\n");
                    foundOne = false;
                }
                for (i = 0; i < maxProducts; i++, j++) {
                    speciesRef = node.getPort(j);
                    if (speciesRef != null && (rootIsTrueRoot || (speciesRef.hasAncestor(root.getName())))) {
                        if (!foundOne) {
                            sbmlOut.write("<listOfProducts>\n");
                            foundOne = true;
                        }
                        sbmlOut.write("<speciesReference species=\"" + speciesRef.getName() + SPEC + "\"/>\n");
                        numFound++;
                    }
                }
                if (foundOne) {
                    sbmlOut.write("</listOfProducts>\n");
                    foundOne = false;
                }
                if (numFound == 0 && !rootIsTrueRoot) {
                    sbmlOut.write("<listOfReactants>\n");
                    sbmlOut.write("<speciesReference species=\"" + DUMMY_SPECIES + "\"/>\n");
                    sbmlOut.write("</listOfReactants>\n");
                }
                for (i = 0; i < maxModifiers; i++, j++) {
                    speciesRef = node.getPort(j);
                    if (speciesRef != null && (rootIsTrueRoot || (speciesRef.hasAncestor(root.getName())))) {
                        if (!foundOne) {
                            sbmlOut.write("<listOfModifiers>\n");
                            foundOne = true;
                        }
                        sbmlOut.write("<modifierSpeciesReference species=\"" + speciesRef.getName() + SPEC + "\"/>\n");
                    }
                }
                if (foundOne) {
                    sbmlOut.write("</listOfModifiers>\n");
                    foundOne = false;
                }
                writeKinetics(node);
                sbmlOut.write("</reaction>\n");
            }
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
        if (node.getFirstChild() != null) {
            writeReaction(node.getFirstChild());
        }
        if (node.getNextSibling() != null) {
            writeReaction(node.getNextSibling());
        }
    }

    /**
	 * Write kinetics.
	 * @param The current node in the tree.
	 */
    protected void writeKinetics(IXholon node) {
        int i;
        int geneVal;
        boolean foundOne = false;
        try {
            sbmlOut.write("<kineticLaw>\n");
            sbmlOut.write("<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n");
            sbmlOut.write("<apply>\n");
            sbmlOut.write("<plus/>\n");
            sbmlOut.write("<cn> 0 </cn>\n");
            for (i = 0; i < IBioXholonClass.SIZE_ARRAY_GENEVAL; i++) {
                if (((BioXholonClass) node.getXhc()).geneVal[i] != Integer.MAX_VALUE) {
                    sbmlOut.write("<ci> Param_" + i + " </ci>\n");
                }
            }
            sbmlOut.write("</apply>\n");
            sbmlOut.write("</math>\n");
            for (i = 0; i < IBioXholonClass.SIZE_ARRAY_GENEVAL; i++) {
                geneVal = ((BioXholonClass) node.getXhc()).geneVal[i];
                if (geneVal != Integer.MAX_VALUE) {
                    if (!foundOne) {
                        sbmlOut.write("<listOfParameters>\n");
                        foundOne = true;
                    }
                    sbmlOut.write("<parameter id=\"Param_" + i + "\" value=\"" + geneVal + "\"/>\n");
                }
            }
            if (foundOne) {
                sbmlOut.write("</listOfParameters>\n");
                foundOne = false;
            }
            sbmlOut.write("</kineticLaw>\n");
        } catch (IOException e) {
            Xholon.getLogger().error("", e);
        }
    }
}
