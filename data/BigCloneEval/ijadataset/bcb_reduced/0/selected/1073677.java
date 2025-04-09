package org.proteinshader.structure;

import org.proteinshader.structure.enums.*;
import org.proteinshader.structure.exceptions.*;
import org.proteinshader.structure.visitor.exceptions.*;
import org.proteinshader.structure.visitor.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

/*******************************************************************************
A Model serves as a container for one or more Chains.

<br/><br/>
If a protein structure is  determined by x-ray crystallography, there
is usually only a single model.  However, if NMR (Nuclear Magnetic
Resonance imaging) is used, there are normally several possible models
that fit the data.  The atomic coordinates will differ between the
models, but all other information is the same, so the models are all
stored in one PDB structure entry.
*******************************************************************************/
public class Model implements IDTest, Visitable {

    private final String m_modelID, m_structureID;

    private final LinkedHashMap<String, Chain> m_chains;

    private final LinkedHashMap<String, Helix> m_helices;

    private LinkedHashMap<String, BetaStrand> m_betaStrands;

    private final LinkedHashMap<String, Loop> m_loops;

    private final VisibilityVisitor m_visibilityVisitor;

    private final List<Segment> m_opaqueSegments;

    private final List<Drawable> m_opaqueAtomsAndBonds, m_translucentList;

    private double m_maxDimension, m_width, m_height, m_depth, m_minX, m_maxX, m_minY, m_maxY, m_minZ, m_maxZ, m_x, m_y, m_z;

    /***************************************************************************
    Constructs a Model with the requested modelID and structureID.

    The modelID is the model number from the PDB structure entry.  The
    numbering of models in a PDB entry is sequential and always starts
    at 1.  This constructor will check that the modelID can be
    converted to a positive integer, but the ID will be stored as a
    String representation so that it can be used as a hash key.  The
    structureID is not checked because it must have already been
    checked by the Structure that creates this Model.

    @param modelID  the Model number.
    @param structureID  the ID code assigned by the Protein Data Bank.
    @throws InvalidIDException  if the Model ID is null or cannot be
                                converted to a positive integer.
    ***************************************************************************/
    public Model(String modelID, String structureID) throws InvalidIDException {
        m_modelID = processID(modelID, "model ID");
        m_structureID = structureID;
        m_chains = new LinkedHashMap<String, Chain>();
        m_helices = new LinkedHashMap<String, Helix>();
        m_betaStrands = new LinkedHashMap<String, BetaStrand>();
        m_loops = new LinkedHashMap<String, Loop>();
        m_visibilityVisitor = new VisibilityVisitor();
        m_opaqueSegments = new LinkedList<Segment>();
        m_opaqueAtomsAndBonds = new LinkedList<Drawable>();
        m_translucentList = new LinkedList<Drawable>();
        m_minX = m_maxX = m_minY = m_maxY = m_minZ = m_maxZ = 0.0;
        m_maxDimension = m_width = m_height = m_depth = 0.0;
        m_x = m_y = m_z = 0.0;
    }

    /***************************************************************************
    Accepts a Visitor and does a callback.

    @param visitor  the Visitor to do a callback with.
    @throws VisitorException  if an error occurs while an object is
                              being visited.
    ***************************************************************************/
    public void accept(Visitor visitor) throws VisitorException {
        visitor.visit(this);
    }

    /***************************************************************************
    Finds visible Drawable objects and transfers them to a list of opaque
    Segments, opaque Atoms and Bonds, or a list of translucent Drawables.

    @param includeAtoms  true if Atoms should be included in lists.
    @param includeBonds  true if Bonds should be included in lists.
    @param includeAminoAcids  true if AminoAcids should be included.
    @param includeHeterogens  true if Heterogens should be included.
    @param includeWaters      true if Waters should be included.
    @param includeSegments    true if Segments should be included.
    ***************************************************************************/
    public void updateListsOfVisibles(boolean includeAtoms, boolean includeBonds, boolean includeAminoAcids, boolean includeHeterogens, boolean includeWaters, boolean includeSegments) throws VisitorException {
        m_visibilityVisitor.includeAtoms(includeAtoms);
        m_visibilityVisitor.includeBonds(includeBonds);
        m_visibilityVisitor.includeSegments(includeSegments);
        m_visibilityVisitor.includeAAHetAndWater(includeAminoAcids, includeHeterogens, includeWaters);
        m_visibilityVisitor.setListsToFill(m_opaqueSegments, m_opaqueAtomsAndBonds, m_translucentList);
        accept(m_visibilityVisitor);
    }

    /***************************************************************************
    Copies the list of opaque Segments into an array and returns the array.
    A reference to the array is not kept by this Model.

    @return An array of opaque Segments.
    ***************************************************************************/
    public Segment[] getOpaqueSegments() {
        Segment[] segments = new Segment[m_opaqueSegments.size()];
        return m_opaqueSegments.toArray(segments);
    }

    /***************************************************************************
    Copies the list of opaque Atoms and Bonds into an array and returns the
    array.  A reference to the array is not kept by this Model.

    @return An array of opaque Atoms and Bonds.
    ***************************************************************************/
    public Drawable[] getOpaqueAtomsAndBonds() {
        Drawable[] drawables = new Drawable[m_opaqueAtomsAndBonds.size()];
        return m_opaqueAtomsAndBonds.toArray(drawables);
    }

    /***************************************************************************
    Copies the list of translucent Drawables into an array and returns
    the array.  A reference to the array is not kept by this Model.

    @return An array of translucent Drawables.
    ***************************************************************************/
    public Drawable[] getTranslucentDrawables() {
        Drawable[] drawables = new Drawable[m_translucentList.size()];
        return m_translucentList.toArray(drawables);
    }

    /***************************************************************************
    The Helix will be added to the Chain only if the sequence of
    Residues that the Helix refers to has already been added.  In
    addition to adding the Helix to the Chain, the Model will also
    keep a reference to the Helix, so that it can return an iterator
    to all Helices in the Model.

    <br/><br/>
    Region (the superclass of Helix) will cache the Residues from
    startResidueID to endResidueID (or throw an exception) so that
    an iterator to the Region's sequence can be easily obtained.

    <br/><br/>
    The Helix shape (a HelixShapeEnum) will be set to
    Helix.DEFAULT_SHAPE. If the type argument is null, the type will
    be set to Helix.DEFAULT_TYPE.

    <br/><br/>
    Any leading or trailing whitespace in the helixID, serialNo
    startResidueID, or endResidueID will be trimmed.  The Helix will
    be stamped with the chainID, modelID, and structureID of the Chain
    it belongs to.

    @param chainID        ID of the Chain to add the Helix to.
    @param helixID        Helix identifier from a PDB HELIX record.
    @param serialNo       serial number of the Helix.
    @param startResidueID ID of the first AminoAcid in the sequence.
    @param endResidueID   ID of the last AminoAcid in the sequence.
    @param type           type of Helix as a HelixEnum.
    @throws InvalidRegionException  if the sequence of AminoAcids
                                    (with at least two Residues)
                                    cannot be found on the Chain.
    @throws InvalidIDException if the helixID, serialNo,
                               startResidueID, or endResidueID is
                               null or does not have at least
                               one non-whitespace character.
    ***************************************************************************/
    public void addNewHelix(String chainID, String helixID, String serialNo, String startResidueID, String endResidueID, HelixEnum type) throws InvalidRegionException, InvalidIDException {
        Chain chain = getChain(chainID);
        if (chain == null) {
            throw new InvalidRegionException("Chain " + chainID + " for Helix " + helixID + "could not be found.");
        }
        Helix helix = chain.addNewHelix(helixID, serialNo, startResidueID, endResidueID, type);
        m_helices.put(helix.getHelixID(), helix);
    }

    /***************************************************************************
    The BetaStrand will be added to the Chain only if the sequence of
    Residues that the BetaStrand refers to has already been added.  In
    addition to adding the BetaStrand to the Chain, the Model will
    also keep a reference to the BetaStrand, so that it can return an
    iterator to all BetaStrands in the Model.

    <br/><br/>
    Region (the superclass of BetaStrand) will cache the Residues from
    startResidueID to endResidueID (or throw an exception) so that an
    iterator to the Region's sequence can be easily obtained.

    <br/><br/>
    The sense (orientation) of an individual BetaStrand is 0 if it is
    the first strand in a sheet, 1 if the strand is parallel to the
    previous strand in the sheet, and -1 if the strand is
    anti-parallel to the previous strand in the sheet.

    <br/><br/>
    Any leading or trailing whitespace in the betaStrandID, sheetID,
    startResidueID, or endResidueID will be trimmed.  The BetaStrand
    will be stamped with the chainID, modelID, and structureID of the
    Chain it belongs to.

    @param chainID           ID of the Chain to add the BetaStrand to.
    @param betaStrandID      unique ID (betaStrandNumber plus sheetID).
    @param betaStrandNumber  Strand identifier from PDB SHEET record.
    @param sheetID           sheet identifier from a PDB SHEET record.
    @param startResidueID    ID of the first AminoAcid in the sequence.
    @param endResidueID      ID of the last AminoAcid in the sequence.
    @param sense             strand sense (0, 1, or -1).
    @param strandsInSheet   total number of BetaStrands in the sheet.
    @throws InvalidRegionException  if the sequence of AminoAcids
                                    (with at least two Residues)
                                    cannot be found on the Chain.
    @throws InvalidIDException  if the betaStrandID, sheetID,
                                startResidueID, or endResidueID is
                                null or does not have at least one
                                non-whitespace character.
    ***************************************************************************/
    public void addNewBetaStrand(String chainID, String betaStrandID, String betaStrandNumber, String sheetID, String startResidueID, String endResidueID, int sense, int strandsInSheet) throws InvalidRegionException, InvalidIDException {
        Chain chain = getChain(chainID);
        if (chain == null) {
            throw new InvalidRegionException("Chain " + chainID + " for BetaStrand " + betaStrandID + "could not be found.");
        }
        BetaStrand strand = chain.addNewBetaStrand(betaStrandID, betaStrandNumber, sheetID, startResidueID, endResidueID, sense, strandsInSheet);
        m_betaStrands.put(strand.getBetaStrandID(), strand);
    }

    /***************************************************************************
    Adds a Loop to the Model.

    <br/><br/>
    This method will be called by the LoopGeneratorVisitor that is
    used to add Loops to each Chain.  When a Loop is added to a Chain,
    a reference to the Loop should also be added here to the Model so
    that the Model can return an iterator to all of its Loops.

    @param loop  the Loop reference to add to the Loop hash.
    ***************************************************************************/
    public void addLoop(Loop loop) {
        m_loops.put(loop.getLoopID(), loop);
    }

    /***************************************************************************
    Clears the lists of opaque and translucent Drawable objects.
    ***************************************************************************/
    public void clearListsOfVisibles() {
        m_opaqueSegments.clear();
        m_opaqueAtomsAndBonds.clear();
        m_translucentList.clear();
    }

    /***************************************************************************
    Returns the modelID, which is the Model's number.

    The String returned cannot be null or empty, because the
    constructor checks that this read-only attribute has at least
    one non-whitespace character.

    @return The Model's ID number.
    ***************************************************************************/
    public String getModelID() {
        return m_modelID;
    }

    /***************************************************************************
    Returns the structureID of the Structure this Model belongs to.

    @return The ID of the Structure.
    ***************************************************************************/
    public String getStructureID() {
        return m_structureID;
    }

    /***************************************************************************
    Creates a new Chain with the chainID given as an argument, adds
    the Chain to the Model's collection of Chains, and returns a
    reference to the new Chain.

    The new Chain will be stamped with the modelID and structureID of
    the Model.

    @param chainID  the ID of the Chain.
    @return The new Chain.
    @throws InvalidIDException  if chainID is null or does not have at
                                least one non-whitespace character.
    ***************************************************************************/
    public Chain addNewChain(String chainID) throws InvalidIDException {
        Chain chain = new Chain(chainID, m_modelID, m_structureID);
        m_chains.put(chain.getChainID(), chain);
        return chain;
    }

    /***************************************************************************
    Returns the Chain with the chainID given as an argument.

    @param chainID  the ID for the desired Chain.
    @return  The requested Chain (or null if not found).
    ***************************************************************************/
    public Chain getChain(String chainID) {
        if (chainID != null) {
            return m_chains.get(chainID);
        }
        return null;
    }

    /***************************************************************************
    Returns an Iterator for the Chains held by this Model.

    The order of iteration is the same as the order in which Chains
    were added to the Model.  In the rare case where a Chans was
    replaced (by adding a Chain with the same chainID), the
    replacement would not change the iteration order in any way.

    @return  An Iterator for the Chains held by this Model.
    ***************************************************************************/
    public Iterator<Chain> iteratorChains() {
        return m_chains.values().iterator();
    }

    /***************************************************************************
    Returns the number of Chains held by this Model.

    @return  The total number of Chains.
    ***************************************************************************/
    public int numberOfChains() {
        return m_chains.size();
    }

    /***************************************************************************
    Returns the Helix with the helixID given as an argument.

    @param helixID  the ID for the desired Helix.
    @return  The requested Helix (or null if not found).
    ***************************************************************************/
    public Helix getHelix(String helixID) {
        if (helixID != null) {
            return m_helices.get(helixID);
        }
        return null;
    }

    /***************************************************************************
    Returns an Iterator for the Helices held by this Model.

    The order of iteration is the same as the order in which Helices
    were added to the Model.  In the rare case where a Helix was
    replaced (by adding a Helix with the same helixID), the
    replacement would not change the iteration order in any way.

    @return  An Iterator for the Helices held by this Model.
    ***************************************************************************/
    public Iterator<Helix> iteratorHelices() {
        return m_helices.values().iterator();
    }

    /***************************************************************************
    Returns the number of Helices held by this Model.

    @return  The total number of Helices.
    ***************************************************************************/
    public int numberOfHelices() {
        return m_helices.size();
    }

    /***************************************************************************
    Returns the BetaStrand with the betaStrandID given as an argument.

    @param betaStrandID  the ID for the desired BetaStrand.
    @return  The requested BetaStrand (or null if not found).
    ***************************************************************************/
    public BetaStrand getBetaStrand(String betaStrandID) {
        if (betaStrandID != null) {
            return m_betaStrands.get(betaStrandID);
        }
        return null;
    }

    /***************************************************************************
    Returns an Iterator for the BetaStrands held by this Model.

    The order of iteration is the same as the order in which
    BetaStrands were added to the Model.  In the rare case where a
    BetaStrand was replaced (by adding a BetaStrand with the same
    betaStrandID), the replacement would not change the iteration
    order in any way.

    @return  An Iterator for the BetaStrands held by this Model.
    ***************************************************************************/
    public Iterator<BetaStrand> iteratorBetaStrands() {
        return m_betaStrands.values().iterator();
    }

    /***************************************************************************
    Returns the number of BetaStrands held by this Model.

    @return  The total number of BetaStrands.
    ***************************************************************************/
    public int numberOfBetaStrands() {
        return m_betaStrands.size();
    }

    /***************************************************************************
    Iterates through all Chains of the Model and modifies the linked hash map
    of beta-strands so that when an iterator for beta-strands is handed over
    (based on the linked list part) the beta-strands will be ordered according
    to the starting amino acid.
    ***************************************************************************/
    public void sortBetaStrands() {
        LinkedHashMap<String, BetaStrand> tempList = new LinkedHashMap<String, BetaStrand>();
        Iterator<Chain> iterChains = iteratorChains();
        while (iterChains.hasNext()) {
            Chain chain = iterChains.next();
            chain.sortBetaStrands();
            Iterator<BetaStrand> iterBetaStrands = chain.iteratorBetaStrands();
            while (iterBetaStrands.hasNext()) {
                BetaStrand betaStrand = iterBetaStrands.next();
                tempList.put(betaStrand.getBetaStrandID(), betaStrand);
            }
        }
        m_betaStrands = tempList;
    }

    /***************************************************************************
    Returns the Loop with the loopID given as an argument.

    @param loopID  the ID for the desired Loop.
    @return  The requested Loop (or null if not found).
    ***************************************************************************/
    public Loop getLoop(String loopID) {
        if (loopID != null) {
            return m_loops.get(loopID);
        }
        return null;
    }

    /***************************************************************************
    Returns an Iterator for the Loops held by this Model.

    The order of iteration is the same as the order in which Loops
    were added to the Model.  In the rare case where a Loop was
    replaced (by adding a Loop with the same loopID), the replacement
    would not change the iteration order in any way.

    @return  An Iterator for the Loops held by this Model.
    ***************************************************************************/
    public Iterator<Loop> iteratorLoops() {
        return m_loops.values().iterator();
    }

    /***************************************************************************
    Returns the number of Loops held by this Model.

    @return  The total number of Loops.
    ***************************************************************************/
    public int numberOfLoops() {
        return m_loops.size();
    }

    /***************************************************************************
    Returns true if the model holds any AminoAcids.  Otherwise,
    returns false.

    @return  True if the Model holds any AminoAcids.
    ***************************************************************************/
    public boolean hasAminoAcids() {
        Iterator<Chain> iter = iteratorChains();
        while (iter.hasNext()) {
            if (iter.next().numberOfAminoAcids() > 0) {
                return true;
            }
        }
        return false;
    }

    /***************************************************************************
    Returns true if the model holds any Heterogens.  Otherwise,
    returns false.

    @return  True if the Model holds any Heterogens.
    ***************************************************************************/
    public boolean hasHeterogens() {
        Iterator<Chain> iter = iteratorChains();
        while (iter.hasNext()) {
            if (iter.next().numberOfHeterogens() > 0) {
                return true;
            }
        }
        return false;
    }

    /***************************************************************************
    Returns true if the model holds any Waters.  Otherwise, returns
    false.

    @return  True if the Model holds any Waters.
    ***************************************************************************/
    public boolean hasWaters() {
        Iterator<Chain> iter = iteratorChains();
        while (iter.hasNext()) {
            if (iter.next().numberOfWaters() > 0) {
                return true;
            }
        }
        return false;
    }

    /***************************************************************************
    Returns the Model ID after trimming any leading or trailing
    whitespace.  The Model ID should be the Model serial number (a
    positive integer) from a PDB file, so it will be tested to see
    that it can be converted to an Integer (but the value returned
    is still the ID as a String).

    @param id  Model ID to process.
    @param typeOfID  type of ID (for possible use in error message).
    @return The trimmed Model ID.
    @throws InvalidIDException  if the Model ID cannot be converted
                                to a positive integer.
    ***************************************************************************/
    public String processID(String id, String typeOfID) throws InvalidIDException {
        try {
            int serialNumber = Integer.parseInt(id);
            if (serialNumber < 1) {
                throw new InvalidIDException("'" + id + "' cannot be used as a " + typeOfID + ".");
            }
            return "" + serialNumber;
        } catch (NullPointerException e) {
            throw new InvalidIDException("A " + typeOfID + " cannot be null.");
        } catch (NumberFormatException e) {
            throw new InvalidIDException("'" + id + "' cannot be used as a " + typeOfID + ".");
        }
    }

    /***************************************************************************
    Returns the modelID, which is the Model's number.

    The String returned cannot be null or empty, because the
    constructor checks that this read-only attribute has at least
    one non-whitespace character.

    @return The Model's ID number as a String.
    ***************************************************************************/
    public String toString() {
        return m_modelID;
    }

    /***************************************************************************
    Sets the minimum and maximum values for the xyz-coordinates.  The
    center xyz-coordinate for the Model will also be calculated, along
    with the overall dimensions and maximum dimension.

    @param minX  the minimum x-coordinate.
    @param maxX  the maximum x-coordinate.
    @param minY  the minimum y-coordinate.
    @param maxY  the maximum y-coordinate.
    @param minZ  the minimum z-coordinate.
    @param maxZ  the maximum z-coordinate.
    ***************************************************************************/
    public void setMinMaxXYZ(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        m_minX = minX;
        m_maxX = maxX;
        m_minY = minY;
        m_maxY = maxY;
        m_minZ = minZ;
        m_maxZ = maxZ;
        calculateCenterAndDimensions();
    }

    /***************************************************************************
    Returns the x-coordinate for the center of gravity of the Model.

    @return  the x-coordinate of the center.
    ***************************************************************************/
    public double getX() {
        return m_x;
    }

    /***************************************************************************
    Returns the y-coordinate for the center of gravity of the Model.

    @return  the y-coordinate of the center.
    ***************************************************************************/
    public double getY() {
        return m_y;
    }

    /***************************************************************************
    Returns the z-coordinate for the center of gravity of the Model.

    @return  the z-coordinate of the center.
    ***************************************************************************/
    public double getZ() {
        return m_z;
    }

    /***************************************************************************
    Returns the minimum x-coordinate value.

    @return  the min x-coordinate.
    ***************************************************************************/
    public double getMinX() {
        return m_minX;
    }

    /***************************************************************************
    Returns the maximum x-coordinate value.

    @return  the max x-coordinate.
    ***************************************************************************/
    public double getMaxX() {
        return m_maxX;
    }

    /***************************************************************************
    Returns the minimum y-coordinate value.

    @return  the min y-coordinate.
    ***************************************************************************/
    public double getMinY() {
        return m_minY;
    }

    /***************************************************************************
    Returns the maximum y-coordinate value.

    @return  the max y-coordinate.
    ***************************************************************************/
    public double getMaxY() {
        return m_maxY;
    }

    /***************************************************************************
    Returns the minimum z-coordinate value.

    @return  the min z-coordinate.
    ***************************************************************************/
    public double getMinZ() {
        return m_minZ;
    }

    /***************************************************************************
    Returns the maximum z-coordinate value.

    @return  the max z-coordinate.
    ***************************************************************************/
    public double getMaxZ() {
        return m_maxZ;
    }

    /***************************************************************************
    Returns the width of the Model (maxX - minX).

    @return  the width as a double.
    ***************************************************************************/
    public double getWidth() {
        return m_width;
    }

    /***************************************************************************
    Returns the height of the Model (maxY - minY).

    @return  the height as a double.
    ***************************************************************************/
    public double getHeight() {
        return m_height;
    }

    /***************************************************************************
    Returns the depth of the Model (maxZ - minZ).

    @return  the depth as a double.
    ***************************************************************************/
    public double getDepth() {
        return m_depth;
    }

    /***************************************************************************
    Returns the maximum dimension (the greatest of width, height, or
    depth).

    @return  the max dimension as a double.
    ***************************************************************************/
    public double getMaxDimension() {
        return m_maxDimension;
    }

    /***************************************************************************
    Returns the requested Segment if it exists.

    @param segmentID   the ID of the requested Segment.
    @param regionID    the ID of the Region the Segment belongs to.
    @param regionType  the type of Region the Segment belongs to.
    @return  The requested Segment (or null if it does not exist).
    ***************************************************************************/
    public Segment getSegment(String segmentID, String regionID, RegionEnum regionType) {
        Region region = null;
        if (regionType != null && segmentID != null && regionID != null) {
            switch(regionType) {
                case LOOP:
                    region = m_loops.get(regionID);
                    break;
                case HELIX:
                    region = m_helices.get(regionID);
                    break;
                case BETA_STRAND:
                    region = m_betaStrands.get(regionID);
                    break;
            }
            if (region != null) {
                return region.getSegment(segmentID);
            }
        }
        return null;
    }

    /***************************************************************************
    Helper method for setMinMaxXYZ() takes the min and max xyz-values
    and uses them to calculate the center of gravity of the Model and
    its overall dimensions.
    ***************************************************************************/
    private void calculateCenterAndDimensions() {
        m_x = (m_minX + m_maxX) / 2;
        m_y = (m_minY + m_maxY) / 2;
        m_z = (m_minZ + m_maxZ) / 2;
        m_width = m_maxX - m_minX;
        m_height = m_maxY - m_minY;
        m_depth = m_maxZ - m_minZ;
        m_maxDimension = (m_width > m_height) ? m_width : m_height;
        if (m_depth > m_maxDimension) {
            m_maxDimension = m_depth;
        }
    }
}
