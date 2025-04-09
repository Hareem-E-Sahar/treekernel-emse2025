import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.util.BitSet;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This is the main program file.
 * The program reads in the layer text files as needed
 * and reconstructs the 3D model.
 * It then produces output in the form of text files.
 * The text files are generated per layer and gives the 
 * type of blocks and where to place them.
 * 
 * @author Eugene Smal
 * 
 *
 *	TO DO:
 *	- Make it possible to choose different sets of lego bricks.(done)
 *	- Can also create and save a list of bricks. (not  done)
 *	- 1x1 and 1x2 bricks must be in set. (just warning)
 *	- make it possible to order squares by edge first as well (done)
 *	- allow building with a baseplate and without (done)
 *
 *	TO FIX!!!!!!: FIX IT SO THAT THERE IS NO NULL POSSIBLE JUST BECAUSE NO CHILDREN WHERE ADDED
 *	(Happens almost never now)
 */
public class ModelReconstructer {

    boolean finishedGUISettings = false;

    boolean interactingWithGUI = false;

    public BitSet currentLayer = null;

    public Vector<LBrick> previousLayerBricks;

    public Vector<LBrick> currentLayerBricks;

    public int[][] previousLayerGrid;

    public BitSet previousLayerBit = null;

    public BitSet nextLayerBit = null;

    public Hashtable bitsetLookUpTable;

    public int fillingOrderSquaresToFill[];

    public Hashtable layersMade;

    public int xSize = -1;

    public int ySize = -1;

    public int numberOfNodesAdded = 0;

    public float currentLayerCost = 0;

    public long totalCost = 0;

    public LBricks legoBricks;

    public int[] totalNumLegoBricksUsed = new int[LBricks.numLegoBricks];

    public String layersDirectory = "";

    public int firstLayer = 0;

    public int lastLayer = 0;

    public boolean DELETEDUPLICATES = true;

    public boolean ALLOWDUPLICATESPERLAYER = true;

    public boolean RESTRICTCHILDREN = false;

    public int MAXCHILDREN = 5;

    public int MAXTREEWIDTH = 10000;

    public int MAXTREEWIDTHUSED = MAXTREEWIDTH;

    public boolean INTERACTIVE = false;

    public int skipTOLayer = -1;

    public int BRICKCOST = 200;

    public int PERPENDCOST = 200;

    public int EDGECOST = 300;

    public int OTHERBRICKSCOST = 200;

    public int UNCOVEREDBRICK = 5000;

    public int[] bricksToUseList;

    public byte orderSquaresMethod = 0;

    /**
	 * The Constructer. It creates an instance of the modelReconstructer.
	 */
    public ModelReconstructer(LPicToSlice bitmapsToLayers) {
        legoBricks = new LBricks(this);
        firstLayer = bitmapsToLayers.startIndex;
        lastLayer = bitmapsToLayers.stopIndex;
        layersDirectory = bitmapsToLayers.directory;
        ReconstructGUI constructSettings = new ReconstructGUI(this);
        try {
            while (finishedGUISettings == false) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
        }
        startConstruction();
    }

    /**
	 * This function creates an array of squares that need to be filled.
	 * Each square has a value called it's bitset value. 
	 * This value uniquely identifies the square.
	 * 
	 * The function first determines the squares that are dangerous. 
	 * Dangerous squares are squares that can possibly be disconnected from
	 * the sculpture if not connected early they could result in unconnected bricks.
	 * Dangerous squares are squares which are not covered by the bottom or top layers.
	 * 
	 * The final list of squares to be filled will therefore first contain
	 * the dangerous squares from top top bottom , left to right order.
	 * It will then contain all the other squares also in order from top to bottom,
	 * left to right.
	 * 
	 * The function creates the array and a hash table so that lookup for a square in the array
	 * is as quick as possible required for testing whether a brick can be placed at 
	 * a specific possition in the grid.
	 * 
	 * @param usePrevious - This is whether the previous layer must be used to 
	 * check for dangerous squares. (It depends whether model is build on a base plate)
	 * @param useNext - This is whether the last layer must be used to check for dangerous squares.(
	 * It is needed for last layer since there is no next layer)
	 * @param useRandom - This is whether the nondangerous squares must be filled in order or random.
	 * 
	 * @return void
	 */
    public void getInOrderSquaresList(boolean usePrevious, boolean useNext, boolean useRandom) {
        int j;
        int i;
        int k = 0;
        int size;
        BitSet union = new BitSet(xSize * ySize);
        Vector<String> dangerous = new Vector<String>();
        Vector<String> nonDangerous = new Vector<String>();
        if (usePrevious && useNext) {
            union.or(previousLayerBit);
            union.or(nextLayerBit);
        } else if (!useNext) {
            union.or(previousLayerBit);
        } else {
            union.or(nextLayerBit);
        }
        for (j = 0; j < ySize; j++) {
            for (i = 0; i < xSize; i++) {
                if (currentLayer.get((j * xSize) + i)) {
                    if (union.get((j * xSize) + i)) {
                        nonDangerous.add("" + ((j * xSize) + i));
                    } else {
                        dangerous.add("" + ((j * xSize) + i));
                    }
                }
            }
        }
        fillingOrderSquaresToFill = new int[dangerous.size() + nonDangerous.size()];
        bitsetLookUpTable = new Hashtable(dangerous.size() + nonDangerous.size(), 0.25f);
        k = 0;
        if (dangerous.size() > 0) {
            size = dangerous.size();
            while (dangerous.size() > 0) {
                fillingOrderSquaresToFill[k] = Integer.parseInt(dangerous.remove(0));
                bitsetLookUpTable.put(fillingOrderSquaresToFill[k], k);
                k++;
                size--;
            }
        }
        if (!useRandom) {
            if (nonDangerous.size() > 0) {
                size = nonDangerous.size();
                while (nonDangerous.size() > 0) {
                    fillingOrderSquaresToFill[k] = Integer.parseInt(nonDangerous.remove(0));
                    bitsetLookUpTable.put(fillingOrderSquaresToFill[k], k);
                    k++;
                    size--;
                }
            }
        } else {
            if (nonDangerous.size() > 0) {
                Random randomNumber = new Random();
                size = nonDangerous.size();
                while (nonDangerous.size() > 0) {
                    fillingOrderSquaresToFill[k] = Integer.parseInt(nonDangerous.remove(randomNumber.nextInt(size)));
                    bitsetLookUpTable.put(fillingOrderSquaresToFill[k], k);
                    k++;
                    size--;
                }
            }
        }
    }

    /**
	 * This function creates an array of squares that need to be filled.
	 * Each square has a value called it's bitset value. 
	 * This value uniquely identifies the square.
	 * 
	 * The function first determines the squares that are dangerous. 
	 * Dangerous squares are squares that can possibly be disconnected from
	 * the sculpture if not connected early they could result in unconnected bricks.
	 * Dangerous squares are squares which are not covered by the bottom or top layers.
	 * 
	 * The final list of squares to be filled will therefore first contain
	 * the dangerous squares from top top bottom , left to right order.
	 * It will then contain all the edge squares either ordered from top to bottom,
	 * left to right or in random order. The rest of the squares will then be in 
	 * either the same order or in random order.
	 * 
	 * The function creates the array and a hash table so that lookup for a square in the array
	 * is as quick as possible required for testing whether a brick can be placed at 
	 * a specific possition in the grid.
	 * 
	 * @param usePrevious - This is whether the previous layer must be used to 
	 * check for dangerous squares. (It depends whether model is build on a base plate)
	 * @param useNext - This is whether the last layer must be used to check for dangerous squares.(
	 * It is needed for last layer since there is no next layer)
	 * @param useRandom - This is whether the edges and nondangerous squares must be filled in order or random.
	 * 
	 * @return void
	 */
    public void getEdgeFirstSquaresList(boolean usePrevious, boolean useNext, boolean useRandom) {
        Vector<String> squaresEdges = new Vector<String>();
        Vector<String> squares = new Vector<String>();
        int i;
        int j;
        int k;
        int size;
        Hashtable hash = new Hashtable();
        BitSet union = new BitSet(xSize * ySize);
        Vector<String> squaresDangerous = new Vector<String>();
        if (usePrevious && useNext) {
            union.or(previousLayerBit);
            union.or(nextLayerBit);
        } else if (!useNext) {
            union.or(previousLayerBit);
        } else {
            union.or(nextLayerBit);
        }
        for (i = 0; i < xSize; i++) {
            for (j = 0; j < ySize; j++) {
                if (currentLayer.get(j * xSize + i)) {
                    if (union.get(j * xSize + i)) {
                        squaresEdges.add("" + (j * xSize + i));
                    } else {
                        squaresDangerous.add("" + (j * xSize + i));
                    }
                    hash.put(new Integer((j * xSize + i)), new Integer((j * xSize + i)));
                    break;
                }
            }
        }
        for (j = 0; j < ySize; j++) {
            for (i = xSize - 1; i >= 0; i--) {
                if (currentLayer.get(j * xSize + i)) {
                    if (hash.get(new Integer((j * xSize + i))) == null) {
                        if (union.get(j * xSize + i)) {
                            squaresEdges.add("" + (j * xSize + i));
                        } else {
                            squaresDangerous.add("" + (j * xSize + i));
                        }
                        hash.put(new Integer((j * xSize + i)), new Integer((j * xSize + i)));
                    }
                    break;
                }
            }
        }
        for (i = xSize - 1; i >= 0; i--) {
            for (j = ySize - 1; j >= 0; j--) {
                if (currentLayer.get(j * xSize + i)) {
                    if (hash.get(new Integer((j * xSize + i))) == null) {
                        if (union.get(j * xSize + i)) {
                            squaresEdges.add("" + (j * xSize + i));
                        } else {
                            squaresDangerous.add("" + (j * xSize + i));
                        }
                        hash.put(new Integer((j * xSize + i)), new Integer((j * xSize + i)));
                    }
                    break;
                }
            }
        }
        for (j = ySize - 1; j >= 0; j--) {
            for (i = 0; i < xSize; i++) {
                if (currentLayer.get(j * xSize + i)) {
                    if (hash.get(new Integer((j * xSize + i))) == null) {
                        if (union.get(j * xSize + i)) {
                            squaresEdges.add("" + (j * xSize + i));
                        } else {
                            squaresDangerous.add("" + (j * xSize + i));
                        }
                        hash.put(new Integer((j * xSize + i)), new Integer((j * xSize + i)));
                    }
                    break;
                }
            }
        }
        for (j = 1; j < ySize - 1; j++) {
            for (i = 1; i < xSize - 1; i++) {
                if (currentLayer.get((j * xSize) + i)) {
                    if (hash.get(new Integer((j * xSize + i))) == null) {
                        if (union.get(j * xSize + i)) {
                            squares.add("" + (j * xSize + i));
                        } else {
                            squaresDangerous.add("" + (j * xSize + i));
                        }
                    }
                }
            }
        }
        fillingOrderSquaresToFill = new int[squaresDangerous.size() + squaresEdges.size() + squares.size()];
        bitsetLookUpTable = new Hashtable(squaresDangerous.size() + squaresEdges.size() + squares.size(), 0.25f);
        k = 0;
        if (squaresDangerous.size() > 0) {
            size = squaresDangerous.size();
            while (squaresDangerous.size() > 0) {
                fillingOrderSquaresToFill[k] = Integer.parseInt(squaresDangerous.remove(0));
                bitsetLookUpTable.put(fillingOrderSquaresToFill[k], k);
                k++;
                size--;
            }
        }
        if (!useRandom) {
            if (squaresEdges.size() > 0) {
                size = squaresEdges.size();
                while (squaresEdges.size() > 0) {
                    fillingOrderSquaresToFill[k] = Integer.parseInt(squaresEdges.remove(0));
                    bitsetLookUpTable.put(fillingOrderSquaresToFill[k], k);
                    k++;
                    size--;
                }
            }
        } else {
            if (squaresEdges.size() > 0) {
                Random randomNumber = new Random();
                size = squaresEdges.size();
                while (squaresEdges.size() > 0) {
                    fillingOrderSquaresToFill[k] = Integer.parseInt(squaresEdges.remove(randomNumber.nextInt(size)));
                    bitsetLookUpTable.put(fillingOrderSquaresToFill[k], k);
                    k++;
                    size--;
                }
            }
        }
        if (!useRandom) {
            if (squares.size() > 0) {
                size = squares.size();
                while (squares.size() > 0) {
                    fillingOrderSquaresToFill[k] = Integer.parseInt(squares.remove(0));
                    bitsetLookUpTable.put(fillingOrderSquaresToFill[k], k);
                    k++;
                    size--;
                }
            }
        } else {
            if (squares.size() > 0) {
                Random randomNumber = new Random();
                size = squares.size();
                while (squares.size() > 0) {
                    fillingOrderSquaresToFill[k] = Integer.parseInt(squares.remove(randomNumber.nextInt(size)));
                    bitsetLookUpTable.put(fillingOrderSquaresToFill[k], k);
                    k++;
                    size--;
                }
            }
        }
    }

    /**
	 * The function loads creates the list of all squares that must be filled for the current layer.
	 * @param usePrevious - Whether to use previous layer in calculating dangerous squares
	 * @param useNext - Whether to use next layer in calculating dangerous squares
	 */
    public void createSquaresList(boolean usePrevious, boolean useNext) {
        switch(orderSquaresMethod) {
            case 0:
                getInOrderSquaresList(usePrevious, useNext, false);
                break;
            case 1:
                getInOrderSquaresList(usePrevious, useNext, true);
                break;
            case 2:
                getEdgeFirstSquaresList(usePrevious, useNext, false);
                break;
            case 3:
                getEdgeFirstSquaresList(usePrevious, useNext, true);
                break;
            default:
                System.out.println("The square ordering method value is invalid. Please change it at the top of ModelReconstructer.java file.");
                System.exit(0);
        }
    }

    /**
	 * This function loads the layer grid into the bitset fot layerLabel.
	 * @param layer	- the layer number
	 * @param layerLabel- into which array to load. (PREVIOUS,CURRENT,NEXT)
	 * @return void
	 */
    public void loadLayerGrid(int layer, String layerlabel) {
        String line;
        int value;
        int i;
        int j;
        char ch;
        try {
            BufferedReader file = new BufferedReader(new FileReader(layersDirectory + "slice" + layer + ".sl"));
            value = Integer.parseInt(file.readLine());
            if (value != layer) {
                System.out.println("The slice number does not correspond to the layer wanted.");
                System.out.println("slice" + layer + ".sl contains slice : " + value);
                System.exit(0);
            }
            line = file.readLine();
            String[] values = line.split(" ");
            if (xSize == -1) {
                xSize = Integer.parseInt(values[1]);
                ySize = Integer.parseInt(values[0]);
            }
            if (layerlabel.equals("CURRENT")) {
                currentLayer = new BitSet(xSize * ySize);
                for (j = 0; j < ySize; j++) {
                    for (i = 0; i < xSize; i++) {
                        if ((ch = (char) file.read()) == '1') {
                            currentLayer.set(j * xSize + i);
                        }
                    }
                    file.readLine();
                }
            } else if (layerlabel.equals("PREVIOUS")) {
                previousLayerBit = new BitSet(xSize * ySize);
                for (j = 0; j < ySize; j++) {
                    for (i = 0; i < xSize; i++) {
                        if ((ch = (char) file.read()) == '1') {
                            previousLayerBit.set(j * xSize + i);
                        }
                    }
                    file.readLine();
                }
            } else if (layerlabel.equals("NEXT")) {
                nextLayerBit = new BitSet(xSize * ySize);
                for (j = 0; j < ySize; j++) {
                    for (i = 0; i < xSize; i++) {
                        if ((ch = (char) file.read()) == '1') {
                            nextLayerBit.set(j * xSize + i);
                        }
                    }
                    file.readLine();
                }
            }
        } catch (Exception e) {
            System.out.println("An error occured while reading " + layersDirectory + "slice" + layer + ".sl");
            System.exit(0);
        }
    }

    /**
	 * The function deletes the node given from hashtable and 
	 * tree. It goes further by checking if , when node deleted if 
	 * parent node has any children. If not the node is also deleted.
	 * This procedure is therfore done recusively.
	 * 
	 * @param layouts - a hash table of pointers of all unique layouts in tree
	 * @param node - the node to delete(prune) in the tree
	 * 
	 * @return void
	 */
    public void deleteNodeUpwards(Hashtable layouts, LayerStateNode node) {
        layouts.remove(node.squares);
        LayerStateNode parent;
        StateEdge edge;
        parent = node.parent;
        if (parent != null) {
            parent.removeChildWithParent(node);
            if (parent.childrenStates.size() == 0) {
                deleteNodeUpwards(layouts, parent);
            }
        }
    }

    /**
	 * The function receives a node and then recursively deletes
	 * all the children nodes down the tree.
	 * 
	 * @param layouts -a hash table of pointers of all unique layouts in tree
	 * @param node - the node to delete(prune) in the tree
	 * @param root - The root of the tree. To see where to stop pruning
	 */
    public void deleteNodeDownwards(Hashtable layouts, LayerStateNode node, LayerStateNode root) {
        StateEdge edge;
        for (int i = 0; i < node.childrenStates.size(); i++) {
            edge = node.childrenStates.get(i);
            deleteNodeDownwards(layouts, edge.childState, root);
        }
        node.childrenStates.clear();
        if (!node.equals(root)) {
            node.parent.childrenStates.remove(node);
            layouts.remove(node.squares);
        }
    }

    /**
	 * This function searches in the neighbourhood of position in the bestNodes list to find where to insert the node with statecost
	 * according to its brick type. Bigger brick type is perfered if same cost.
	 * 
	 * @param bestNodes - the best layout nodes found so far.
	 * @param pos - the position near where the new node must be placed
	 * @param stateCost - the new layout cost
	 * @param brickType - The brick that was used to create the new layout. 
	 * 
	 * @return int - position where to insert
	 */
    public static int findPosByBlockSize(Vector<possibleLayout> bestNodes, int pos, float stateCost, int brickType) {
        possibleLayout temp;
        temp = bestNodes.get(pos);
        if (temp.legoBlockType > brickType) {
            pos++;
            if (pos > bestNodes.size()) {
                return pos;
            }
            while ((pos < bestNodes.size()) && ((temp = bestNodes.get(pos)).newLayout.stateCost == stateCost)) {
                if (temp.legoBlockType < brickType) {
                    return pos;
                }
                pos++;
            }
            return pos;
        } else {
            pos--;
            while ((pos > 0) && ((temp = bestNodes.get(pos)).newLayout.stateCost == stateCost)) {
                if (temp.legoBlockType >= brickType) {
                    return pos + 1;
                }
                pos--;
            }
            return pos + 1;
        }
    }

    /**
	 * This function takes the nodes in the TOAdd list and tries to add it to the bestNodes.
	 * The bestNodes list must never be larger than MaxSize. The worst cost is largest, thus list is sorted 
	 * accending according to cost value. List to add is also sorted.
	 * FUNCTION HAD ERROR THAT IT WOULD ADD IN ORDER OF COST BUT THEN DID NOT TAKE INTO
	 * ACCOUNT THE BLOCKS SIZE ORDER WITH RESPECT TO PARENTS.
	 * 
	 * @param duplicatesLayer - A hashtable to keep track of unique layout nodes for the layer.
	 * @param bestNodes - the list of best layout nodes found so far for current layer
	 * @param toAdd - A list of new nodes for a parent to add if possible.
	 * @param MaxSize - The max layouts allowed for the layer.
	 * 
	 * @return void
	 */
    public void addToListSortedCost(Hashtable duplicatesLayer, Vector<possibleLayout> bestNodes, Vector<possibleLayout> toAdd) {
        possibleLayout possibleNode;
        possibleLayout tempDuplicate;
        int size = bestNodes.size();
        int pos;
        if (size == 0) {
            bestNodes.addAll(toAdd);
            if (bestNodes.size() > MAXTREEWIDTHUSED) {
                bestNodes.setSize(MAXTREEWIDTHUSED);
            }
            return;
        }
        while (toAdd.size() > 0) {
            possibleNode = toAdd.remove(0);
            if (ALLOWDUPLICATESPERLAYER) {
                tempDuplicate = null;
            } else {
                tempDuplicate = (possibleLayout) duplicatesLayer.get(possibleNode.newLayout.squares);
            }
            if (tempDuplicate == null) {
                pos = binarySearch(bestNodes, possibleNode.newLayout.stateCost, 0, size - 1);
                if (pos >= size) {
                    if (size == MAXTREEWIDTHUSED) {
                        return;
                    } else {
                        bestNodes.add(possibleNode);
                        size++;
                        while ((size < MAXTREEWIDTHUSED) && (toAdd.size() > 0)) {
                            possibleNode = toAdd.remove(0);
                            bestNodes.add(possibleNode);
                            size++;
                        }
                        return;
                    }
                } else {
                    if (bestNodes.get(pos).newLayout.stateCost != possibleNode.newLayout.stateCost) {
                        if (size == MAXTREEWIDTHUSED) {
                            bestNodes.add(pos, possibleNode);
                            bestNodes.remove(bestNodes.size() - 1);
                        } else {
                            bestNodes.add(pos, possibleNode);
                            size++;
                        }
                    } else {
                        pos = findPosByBlockSize(bestNodes, pos, possibleNode.newLayout.stateCost, possibleNode.legoBlockType);
                        if (size == MAXTREEWIDTHUSED) {
                            bestNodes.add(pos, possibleNode);
                            bestNodes.remove(bestNodes.size() - 1);
                        } else {
                            bestNodes.add(pos, possibleNode);
                            size++;
                        }
                    }
                }
            } else {
                if (tempDuplicate.newLayout.stateCost > possibleNode.newLayout.stateCost) {
                    bestNodes.remove(tempDuplicate);
                    pos = binarySearch(bestNodes, possibleNode.newLayout.stateCost, 0, size - 1);
                    bestNodes.add(pos, possibleNode);
                }
            }
        }
    }

    /**
	 * This function  creates a list of all the children nodes for the given subtree.
	 * A pointer to the root of the subtree is provided.
	 * There must always be leaf nodes,otherwise there is an error in deletion.
	 * 
	 * @param root - The parent node for which we want the children.
	 * @param leafnodes - a list to store the leaf nodes of root.
	 * 
	 * @return void
	 */
    public void getLeafNodesOf(LayerStateNode root, Vector<LayerStateNode> leafNodes) {
        if (root.childrenStates.size() == 0) {
            leafNodes.add(root);
            return;
        }
        for (int i = 0; i < root.childrenStates.size(); i++) {
            getLeafNodesOf(root.childrenStates.get(i).childState, leafNodes);
        }
    }

    /**
	 * This function builds the lattice/tree in breath first order.
	 * It does not use recursion to save memory.
	 * The function returns the leaf node of the best path in the tree.
	 * 
	 * @param rootState - the root node of the layouts tree to start building
	 * @param usePreviousLayer - Whether to use the previous layer in construction of the current layer
	 * 
	 * @return LayerStateNode - The bestNode found. Null if no complete layers found.
	 */
    public LayerStateNode buildBreathFirstTree(LayerStateNode rootState, boolean usePreviousLayer) {
        int i;
        int j;
        layersMade = new Hashtable(MAXTREEWIDTHUSED * 2, 0.25f);
        Hashtable duplicatesLayer;
        Vector<LayerStateNode> parentNodes;
        Vector<LayerStateNode> childrenAddedNodes = new Vector<LayerStateNode>();
        Vector<possibleLayout> possibleChildrenNodes;
        Vector<possibleLayout> bestChildrenToAdd;
        Vector<LayerStateNode> leafNodes;
        LayerStateNode leaf;
        LayerStateNode rootNode;
        LayerStateNode temp;
        possibleLayout possibleNode;
        LayerStateNode bestNode = null;
        int num = 0;
        int duplicatesBetterFound = 0;
        int duplicatesWORST = 0;
        int k = 0;
        childrenAddedNodes.add(rootState);
        do {
            parentNodes = childrenAddedNodes;
            childrenAddedNodes = new Vector<LayerStateNode>();
            bestChildrenToAdd = new Vector<possibleLayout>();
            num = 0;
            duplicatesLayer = new Hashtable(MAXTREEWIDTHUSED, 0.25f);
            while (num < parentNodes.size()) {
                rootNode = parentNodes.get(num);
                possibleChildrenNodes = legoBricks.getPossibleLayouts(rootNode, xSize, ySize, k, usePreviousLayer);
                if (possibleChildrenNodes != null) {
                    if ((RESTRICTCHILDREN) && (possibleChildrenNodes.size() > MAXCHILDREN)) {
                        possibleChildrenNodes.setSize(MAXCHILDREN);
                    }
                    addToListSortedCost(duplicatesLayer, bestChildrenToAdd, possibleChildrenNodes);
                    num++;
                } else {
                    if (bestNode == null) {
                        bestNode = rootNode;
                    } else {
                        if (bestNode.squares.cardinality() < rootNode.squares.cardinality()) {
                            bestNode = rootNode;
                        } else if (bestNode.stateCost > rootNode.stateCost) {
                            bestNode = rootNode;
                        }
                    }
                    parentNodes.remove(rootNode);
                }
            }
            duplicatesLayer.clear();
            if (DELETEDUPLICATES) {
                while (bestChildrenToAdd.size() > 0) {
                    possibleNode = bestChildrenToAdd.remove(0);
                    temp = (LayerStateNode) layersMade.get(possibleNode.newLayout.squares);
                    if (temp == null) {
                        rootNode = possibleNode.newLayout.parent;
                        rootNode.childrenStates.add(new StateEdge(possibleNode));
                        childrenAddedNodes.add(possibleNode.newLayout);
                        layersMade.put(possibleNode.newLayout.squares, possibleNode.newLayout);
                        numberOfNodesAdded++;
                    } else {
                        if (possibleNode.newLayout.stateCost < temp.stateCost) {
                            leafNodes = new Vector<LayerStateNode>();
                            getLeafNodesOf(temp, leafNodes);
                            if (leafNodes.size() == 0) {
                                System.out.println("SOMETHING WRONG, NO LEAF NODES FOUND");
                            } else {
                                j = 0;
                                while (j < leafNodes.size()) {
                                    leaf = leafNodes.get(j);
                                    if (childrenAddedNodes.contains(leaf)) {
                                        childrenAddedNodes.remove(leaf);
                                        leafNodes.add(j, leaf.parent);
                                    }
                                    j++;
                                }
                                j = 0;
                                while (j < bestChildrenToAdd.size()) {
                                    if (leafNodes.contains(bestChildrenToAdd.get(j).newLayout.parent)) {
                                        bestChildrenToAdd.remove(j);
                                    } else {
                                        j++;
                                    }
                                }
                            }
                            deleteNodeDownwards(layersMade, temp, temp);
                            deleteNodeUpwards(layersMade, temp);
                            rootNode = possibleNode.newLayout.parent;
                            rootNode.childrenStates.add(new StateEdge(possibleNode));
                            childrenAddedNodes.add(possibleNode.newLayout);
                            layersMade.put(possibleNode.newLayout.squares, possibleNode.newLayout);
                            numberOfNodesAdded++;
                            duplicatesBetterFound++;
                        } else {
                            duplicatesWORST++;
                        }
                    }
                }
            } else {
                while (bestChildrenToAdd.size() > 0) {
                    possibleNode = bestChildrenToAdd.remove(0);
                    rootNode = possibleNode.newLayout.parent;
                    rootNode.childrenStates.add(new StateEdge(possibleNode));
                    childrenAddedNodes.add(possibleNode.newLayout);
                }
            }
            int sizeP = parentNodes.size();
            while (parentNodes.size() > 0) {
                rootNode = parentNodes.remove(0);
                if (rootNode.childrenStates.size() == 0) {
                    deleteNodeUpwards(layersMade, rootNode);
                    sizeP--;
                }
            }
            k++;
        } while (childrenAddedNodes.size() > 0);
        return bestNode;
    }

    /**
	 * The function writes the layer layout to file and saves it as the last layer constructed.
	 * It is writen to a normal text file with a .ll extension.
	 * The first line has the layer number
	 * The second has the cost of layout
	 * The file has the grid size on the third line ( rows, columns)
	 * Each line contains the block type as int value, row and column value where the left corner must be placed.
	 * 
	 * @param layer - The layer number to be written to file
	 * @return void
	 */
    public void saveLayerToFileAndSetPrevious(int layer) {
        int i;
        try {
            FileWriter fileW = new FileWriter(layersDirectory + "layerLayout" + layer + ".ll");
            BufferedWriter file = new BufferedWriter(fileW);
            file.write("" + layer);
            file.newLine();
            file.write("" + Math.round(currentLayerCost));
            totalCost += currentLayerCost;
            file.newLine();
            file.write("" + xSize);
            file.write(" ");
            file.write("" + ySize);
            file.newLine();
            if (currentLayerBricks == null) {
                return;
            }
            LBrick brick;
            i = 0;
            int size = currentLayerBricks.size();
            while (i < size) {
                brick = currentLayerBricks.get(i);
                file.write("" + brick.LegoBlockUsed);
                file.write(" " + brick.legoXpos + " " + brick.legoYpos);
                file.newLine();
                totalNumLegoBricksUsed[brick.LegoBlockUsed]++;
                i++;
            }
            file.close();
        } catch (Exception e) {
            System.out.println("An error occured while trying to save layout " + layer);
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
	 * This function saves the bricks of the layer to a grid, where each block is represented by 
	 * a number. This is necessary to help with the cost function for when the edge comparison is done
	 * with that of the previous layer. FOR QUICK COMPARRESON!!!!
	 * 
	 * @param bricks - the bricks of the layer to save to previous grid
	 * @param print	- whether to output grid in command line
	 * 
	 * @return void
	 */
    public void savePreviousGrid(Vector<LBrick> bricks, boolean print) {
        previousLayerGrid = new int[ySize][xSize];
        LBrick brick;
        int legoBrick[][];
        for (int i = 0; i < bricks.size(); i++) {
            brick = bricks.get(i);
            legoBrick = LBricks.getBlock(brick.LegoBlockUsed);
            for (int k = 0; k < legoBrick.length; k++) {
                for (int j = 0; j < legoBrick[0].length; j++) {
                    if (legoBrick[k][j] == 1) {
                        previousLayerGrid[brick.legoYpos + k][brick.legoXpos + j] = i + 1;
                    }
                }
            }
        }
        if (print) {
            for (int p = 0; p < ySize; p++) {
                for (int l = 0; l < xSize; l++) {
                    System.out.print(previousLayerGrid[p][l] + "\t");
                }
                System.out.println();
            }
        }
    }

    /**
	 * This function constructs the specified layer's lattice.
	 * The lattice contains all possible layout configurations for the layer.
	 * If usePreviousLayer false then the layer is constructed without knowledge
	 * of previous layer.
	 * If it's true it does not alter the previous layer.(Bitset and final layout) 
	 * @param layer	- the layer number to build
	 * @param usePreviousLayer - whether to use the previous layer when calculating cost function
	 * 
	 * @return void
	 */
    public void constructLayerLattice(int layer, boolean usePreviousLayer, boolean loadNext) {
        LayerStateNode bestNode;
        previousLayerBit = currentLayer;
        if (previousLayerBit == null) {
            previousLayerBit = new BitSet(xSize * ySize);
        }
        if (currentLayer != null) {
            currentLayer = nextLayerBit;
            if (loadNext) {
                loadLayerGrid(layer + 1, "NEXT");
            } else {
                nextLayerBit = new BitSet(xSize * ySize);
            }
        } else {
            loadLayerGrid(layer, "CURRENT");
            if (loadNext) {
                loadLayerGrid(layer + 1, "NEXT");
            } else {
                nextLayerBit = new BitSet(xSize * ySize);
            }
        }
        createSquaresList(usePreviousLayer, loadNext);
        if ((fillingOrderSquaresToFill == null) || (fillingOrderSquaresToFill.length == 0)) {
            currentLayerBricks = null;
            saveLayerToFileAndSetPrevious(layer);
        }
        LayerStateNode rootState = new LayerStateNode(fillingOrderSquaresToFill.length);
        bestNode = buildBreathFirstTree(rootState, usePreviousLayer);
        if (bestNode == null) {
            MAXTREEWIDTHUSED = 1;
            do {
                bestNode = buildBreathFirstTree(rootState, usePreviousLayer);
                MAXTREEWIDTHUSED += 10;
            } while (bestNode == null);
            MAXTREEWIDTHUSED = MAXTREEWIDTH;
        }
        getTheBestLayout(rootState, bestNode);
        if (INTERACTIVE) {
            if (layer >= skipTOLayer) {
                interactingWithGUI = true;
                interactiveGUI interaction = new interactiveGUI(this, layer);
                try {
                    while (interactingWithGUI == true) {
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                }
            }
        }
        saveLayerToFileAndSetPrevious(layer);
        if (previousLayerBricks != null) {
            previousLayerBricks.clear();
        }
        previousLayerBricks = currentLayerBricks;
        savePreviousGrid(previousLayerBricks, false);
    }

    /**
	 * This function finds the best layer layout and saves the bricks to the currentlayer.
	 * The function runs through the tree and build the list of bricks used.
	 * @param rootState - the root of the layouts tree
	 * @param bestNode - the best final completed layout found.
	 * @return void
	 */
    public void getTheBestLayout(LayerStateNode rootState, LayerStateNode bestNode) {
        LayerStateNode temp;
        StateEdge edge;
        int i;
        currentLayerBricks = new Vector<LBrick>();
        if (bestNode == null) {
            currentLayerCost = 0;
            return;
        }
        currentLayerCost = bestNode.stateCost;
        if (!DELETEDUPLICATES) {
            temp = bestNode;
            Vector<LBrick> tempList = new Vector<LBrick>();
            LayerStateNode parent;
            StateEdge ed;
            while (temp.parent != null) {
                parent = temp.parent;
                for (i = 0; i < parent.childrenStates.size(); i++) {
                    ed = parent.childrenStates.get(i);
                    if (ed.childState.equals(temp)) {
                        tempList.add(ed.brickUsed);
                        ed.brickUsed = null;
                        break;
                    }
                }
                temp = parent;
            }
            LBrick brick;
            while (tempList.size() > 0) {
                brick = tempList.remove(tempList.size() - 1);
                currentLayerBricks.add(brick);
            }
        } else {
            temp = rootState;
            while (temp != null) {
                if (temp.childrenStates.size() == 0) {
                    break;
                }
                edge = temp.childrenStates.get(0);
                temp = edge.childState;
                currentLayerBricks.add(edge.brickUsed);
                edge.brickUsed = null;
            }
        }
    }

    /**
	 * The function calculates how many different blocks it covers in previous layer
	 * This function is used by the cost function.
	 * @param block - a integer matrix of 0 and 1's representing the block
	 * @param x - the x position in grid to place block
	 * @param y - the y position in grid to place block
	 * @return The number of different bricks that will be covered by this brick in  
	 * the previous layer
	 */
    public int getAmountBlocksItCovers(int[][] block, int x, int y) {
        int i;
        int j;
        Hashtable found = new Hashtable();
        int blockWidth = block[0].length;
        int blockHeight = block.length;
        int val;
        for (j = 0; j < blockHeight; j++) {
            for (i = 0; i < blockWidth; i++) {
                if (block[j][i] == 1) {
                    val = previousLayerGrid[y + j][x + i];
                    if (val != 0) {
                        if (found.get(new Integer(val)) != null) {
                        } else {
                            found.put(new Integer(val), 1);
                        }
                    }
                }
            }
        }
        return found.size();
    }

    /**
	 * This function computes the cost of a given layout. It is made such that it can either use the 
	 * previous layer or not to be able to adapt to changes easily.
	 * 
	 * @param root	The parent node of the new layout.
	 * @param legoBrick	The integer number defining which block is inserted.
	 * @param x	The x position where the top left corner of the legoblock matrix is placed.
	 * @param y	The y position where the top left corner of the legoBlock matrix is placed.
	 * @param usePreviousLayer Whether to use previous layer or not.
	 * @return float - Cost of new layout with brick added
	 */
    public float getLayoutCost(float oldCostWithoutNewBrick, int legoBrick, int x, int y, boolean usePreviousLayer) {
        LBrick tempBrick;
        int i, j;
        int r, c;
        float perpendicular = 0;
        float covered = 0;
        int otherbricks = 0;
        float edge = 0;
        int block[][] = LBricks.getBlock(legoBrick);
        ;
        int blockWidth = block[0].length;
        int blockHeight = block.length;
        float layoutCost = oldCostWithoutNewBrick;
        layoutCost += BRICKCOST;
        if (!usePreviousLayer) {
            layoutCost += (OTHERBRICKSCOST * (1 - ((float) 1 / (float) (LBricks.blockArea[legoBrick]))));
            for (j = 0; j < blockHeight; j++) {
                for (i = 0; i < blockWidth; i++) {
                    if (block[j][i] == 1) {
                        if (previousLayerBit.get(((y + j) * xSize) + (x + i))) {
                            covered++;
                        }
                        if (nextLayerBit.get(((y + j) * xSize) + (x + i))) {
                            covered++;
                        }
                    }
                }
            }
            if (covered < 2 * LBricks.blockArea[legoBrick]) {
                covered = (2 * LBricks.blockArea[legoBrick] - covered) / (2 * LBricks.blockArea[legoBrick]);
                covered *= covered;
                covered *= covered;
                layoutCost += UNCOVEREDBRICK * covered;
            }
        } else {
            otherbricks = getAmountBlocksItCovers(block, x, y);
            if (otherbricks > 0) {
                layoutCost += (OTHERBRICKSCOST * (1 - (1.0f / (float) (otherbricks))));
            } else {
                layoutCost -= OTHERBRICKSCOST * 1.0;
            }
            if ((legoBrick < 11) || (legoBrick > 14)) {
                c = x + blockWidth - 1;
                for (i = 0; i < blockHeight; i++) {
                    r = y + i;
                    if ((x - 1 > 0) && (previousLayerGrid[r][x - 1] != previousLayerGrid[r][x])) {
                        edge++;
                    }
                    if ((c + 1 < xSize) && (previousLayerGrid[r][c] != previousLayerGrid[r][c + 1])) {
                        edge++;
                    }
                }
                r = y + blockHeight - 1;
                for (i = 0; i < blockWidth; i++) {
                    c = x + i;
                    if ((y - 1 > 0) && (previousLayerGrid[y - 1][c] != previousLayerGrid[y][c])) {
                        edge++;
                    }
                    if ((r + 1 < ySize) && (previousLayerGrid[r][c] != previousLayerGrid[r + 1][c])) {
                        edge++;
                    }
                }
                int direction = LBricks.getDirection(legoBrick);
                if (legoBrick != 0) {
                    for (j = 0; j < blockHeight; j++) {
                        for (i = 0; i < blockWidth; i++) {
                            if (previousLayerGrid[y + j][x + i] != 0) {
                                tempBrick = previousLayerBricks.get(previousLayerGrid[y + j][x + i] - 1);
                                if ((tempBrick.LegoBlockUsed < 11) || (tempBrick.LegoBlockUsed > 14)) {
                                    if ((direction != LBricks.getDirection(tempBrick.LegoBlockUsed)) && (LBricks.getDirection(tempBrick.LegoBlockUsed) != LBricks.NODIRECT)) {
                                        perpendicular++;
                                        perpendicular /= 2;
                                    }
                                }
                            }
                        }
                    }
                    perpendicular /= LBricks.blockArea[legoBrick];
                    layoutCost += PERPENDCOST * perpendicular;
                }
            } else {
                switch(legoBrick) {
                    case 11:
                        if ((y - 1 > 0) && (previousLayerGrid[y][x] != previousLayerGrid[y - 1][x])) edge++;
                        if ((x - 1 > 0) && (previousLayerGrid[y][x] != previousLayerGrid[y][x - 1])) edge++;
                        if (previousLayerGrid[y][x] != previousLayerGrid[y][x + 1]) edge++;
                        if ((x - 1 > 0) && (previousLayerGrid[y + 1][x] != previousLayerGrid[y + 1][x - 1])) edge++;
                        if ((y + 2 < ySize) && (previousLayerGrid[y + 1][x] != previousLayerGrid[y + 2][x])) edge++;
                        if ((y + 2 < ySize) && (previousLayerGrid[y + 1][x + 1] != previousLayerGrid[y + 2][x + 1])) edge++;
                        if ((x + 2 < xSize) && (previousLayerGrid[y + 1][x + 1] != previousLayerGrid[y + 1][x + 2])) edge++;
                        if (previousLayerGrid[y + 1][x + 1] != previousLayerGrid[y][x + 1]) edge++;
                        break;
                    case 12:
                        if ((y - 1 > 0) && (previousLayerGrid[y][x] != previousLayerGrid[y - 1][x])) edge++;
                        if ((x - 1 > 0) && (previousLayerGrid[y][x] != previousLayerGrid[y][x - 1])) edge++;
                        if (previousLayerGrid[y][x] != previousLayerGrid[y + 1][x]) edge++;
                        if ((x + 2 < xSize) && (previousLayerGrid[y][x + 1] != previousLayerGrid[y][x + 2])) edge++;
                        if ((y - 1 > 0) && (previousLayerGrid[y][x + 1] != previousLayerGrid[y - 1][x + 1])) edge++;
                        if ((x + 2 < xSize) && (previousLayerGrid[y + 1][x + 1] != previousLayerGrid[y + 1][x + 2])) edge++;
                        if ((y + 2 < ySize) && (previousLayerGrid[y + 1][x + 1] != previousLayerGrid[y + 2][x + 1])) edge++;
                        if (previousLayerGrid[y + 1][x + 1] != previousLayerGrid[y + 1][x]) edge++;
                        break;
                    case 13:
                        if (previousLayerGrid[y][x + 1] != previousLayerGrid[y][x]) edge++;
                        if ((y - 1 > 0) && (previousLayerGrid[y][x + 1] != previousLayerGrid[y - 1][x + 1])) edge++;
                        if ((x + 2 < xSize) && (previousLayerGrid[y][x + 1] != previousLayerGrid[y][x + 2])) edge++;
                        if ((x + 2 < xSize) && (previousLayerGrid[y + 1][x + 1] != previousLayerGrid[y + 1][x + 2])) edge++;
                        if ((y + 2 < ySize) && (previousLayerGrid[y + 1][x + 1] != previousLayerGrid[y + 2][x + 1])) edge++;
                        if ((y + 2 < ySize) && (previousLayerGrid[y + 1][x] != previousLayerGrid[y + 2][x])) edge++;
                        if ((x - 1 > 0) && (previousLayerGrid[y + 1][x] != previousLayerGrid[y + 1][x - 1])) edge++;
                        if (previousLayerGrid[y + 1][x] != previousLayerGrid[y][x]) edge++;
                        break;
                    case 14:
                        if ((y - 1 > 0) && (previousLayerGrid[y][x] != previousLayerGrid[y - 1][x])) edge++;
                        if ((x - 1 > 0) && (previousLayerGrid[y][x] != previousLayerGrid[y][x - 1])) edge++;
                        if ((y + 2 < ySize) && (previousLayerGrid[y + 1][x] != previousLayerGrid[y + 2][x])) edge++;
                        if ((x - 1 > 0) && (previousLayerGrid[y + 1][x] != previousLayerGrid[y + 1][x - 1])) edge++;
                        if (previousLayerGrid[y + 1][x] != previousLayerGrid[y + 1][x + 1]) edge++;
                        if ((x + 2 < xSize) && (previousLayerGrid[y][x + 1] != previousLayerGrid[y][x + 2])) edge++;
                        if ((y - 1 > 0) && (previousLayerGrid[y][x + 1] != previousLayerGrid[y - 1][x + 1])) edge++;
                        if (previousLayerGrid[y][x + 1] != previousLayerGrid[y + 1][x + 1]) edge++;
                        break;
                }
            }
            edge /= 2 * (blockWidth + blockHeight);
            layoutCost += EDGECOST * edge;
            for (j = 0; j < blockHeight; j++) {
                for (i = 0; i < blockWidth; i++) {
                    if (block[j][i] == 1) {
                        if (previousLayerBit.get(((y + j) * xSize) + (x + i))) {
                            covered++;
                        }
                        if (nextLayerBit.get(((y + j) * xSize) + (x + i))) {
                            covered++;
                        }
                    }
                }
            }
            if (covered < 2 * LBricks.blockArea[legoBrick]) {
                covered = (2 * LBricks.blockArea[legoBrick] - covered) / (2 * LBricks.blockArea[legoBrick]);
                covered *= covered;
                covered *= covered;
                layoutCost += UNCOVEREDBRICK * covered;
            }
        }
        return layoutCost;
    }

    /**
	* This function looks for the position ***where to place*** the cost. 
	* @param layouts - list of layouts to search through
	* @param cost - the cost of layout that must be inserted
	* @param low - the low position of the list
	* @paran high - the position of the last element in list
	*  
	* @return int - The position where to insert in the list 
	**/
    public static int binarySearch(Vector<possibleLayout> layouts, float cost, int low, int high) {
        int middle;
        float costTest;
        while (low <= high) {
            middle = (low + high) / 2;
            costTest = layouts.get(middle).newLayout.stateCost;
            if (costTest == cost) {
                return middle + 1;
            } else if (costTest > cost) {
                high = middle - 1;
            } else {
                low = middle + 1;
            }
        }
        if (low >= (layouts.size() - 1)) {
            costTest = layouts.get(layouts.size() - 1).newLayout.stateCost;
            if (costTest > cost) {
                return (layouts.size());
            } else {
                return layouts.size() - 1;
            }
        } else if (high <= 0) {
            costTest = layouts.get(0).newLayout.stateCost;
            if (costTest > cost) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (layouts.get(low).newLayout.stateCost < cost) {
                return low + 1;
            } else {
                return low;
            }
        }
    }

    /**
	 * This function inserts the new Layout into the list sorted by cost.(small to large)
	 * DID NOT TAKE INTO ACCOUNT THAT YOU COULD HAVE NODES WITH SAME COST BUT DIFFERENT BLOCKS ADDED
	 * FROM SAME PARENT. (DEPENDS ON COST FUNCTION SCALE VALUES)
	 * @param layouts - the list in which the new layout must be placed
	 * @param newLayout - A new brick layout for the layer
	 * 
	 * @return void
	 */
    public static void insertSortedList(Vector<possibleLayout> layouts, possibleLayout newLayout) {
        if (layouts.size() == 0) {
            layouts.add(newLayout);
            return;
        }
        int pos = binarySearch(layouts, newLayout.newLayout.stateCost, 0, layouts.size() - 1);
        if ((pos >= layouts.size()) || (layouts.get(pos).newLayout.stateCost != newLayout.newLayout.stateCost)) {
            layouts.add(pos, newLayout);
        } else {
            pos = findPosByBlockSize(layouts, pos, newLayout.newLayout.stateCost, newLayout.legoBlockType);
            layouts.add(pos, newLayout);
        }
    }

    /**
	 * This function build sthe layers. If interactive is true it will prompt the user to change the layer
	 * after it has been constructed.
	 */
    public void startConstruction() {
        long startTime = new Date().getTime();
        JProgressBar progress = null;
        JFrame progressFrame = null;
        JPanel progressPane = null;
        int count = firstLayer;
        progressFrame = new JFrame();
        progressFrame.setSize(300, 100);
        progressFrame.setTitle("Progress");
        progressFrame.setLocation(400, 300);
        progressFrame.setVisible(true);
        JLabel pro = new JLabel("Construction progress...");
        pro.setBounds(50, 10, 250, 20);
        progressPane = (JPanel) progressFrame.getContentPane();
        progressPane.setLayout(null);
        progressPane.add(pro);
        progress = new JProgressBar(firstLayer, lastLayer);
        progress.setBounds(50, 40, 200, 20);
        progress.setString("Building layer " + count);
        progress.setStringPainted(true);
        progressPane.add(progress);
        if (firstLayer != lastLayer) {
            constructLayerLattice(count, false, true);
        } else {
            constructLayerLattice(count, false, false);
        }
        count++;
        progress.setValue(count);
        progress.setString("Layer " + count + " complete");
        progressPane.repaint();
        while (count < lastLayer) {
            constructLayerLattice(count, true, true);
            count++;
            progress.setValue(count);
            progress.setString("Layer " + count + " complete");
            progressPane.repaint();
        }
        if (firstLayer != lastLayer) {
            constructLayerLattice(count, true, false);
            count++;
            progress.setValue(count);
            progress.setString("Layer " + count + " complete");
            progressPane.repaint();
        }
        if (!INTERACTIVE) {
            progressFrame.dispose();
        }
        long finishTime = new Date().getTime();
        int totalbricks = saveBuildingInstructionstoPDF(true);
        JOptionPane.showMessageDialog(null, "================================" + "\n" + "Total number of bricks used: " + totalbricks + "\n" + "Total cost of LSculpture: " + totalCost + "\n" + "Total time of execution : " + ((finishTime - startTime) / 1000) + "seconds" + "\n" + "================================", "Brick Sculpture construction complete", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
	 * This function draws a grid similar to graph paper.
	 * @param g - graphics2D object to draw on
	 * @param width - the width of a unit brick (sqaure)
	 * @return void
	 */
    public void drawGrid(Graphics2D g, int width) {
        try {
            int colLength = width * xSize;
            int rowLength = width * ySize;
            int i = ySize;
            int x = 100;
            int y = 150;
            while (i >= 0) {
                g.drawLine(x, y, x + colLength, y);
                y += width;
                i--;
            }
            x = 100;
            y = 150;
            i = xSize;
            while (i >= 0) {
                g.drawLine(x, y, x, y + rowLength);
                x += width;
                i--;
            }
        } catch (Exception de) {
            System.err.println(de.getMessage());
        }
    }

    /**
     *  This function draws each legoblock filled.
     *  @param g - graphics2D object to draw on
     *  @param gridX - x position to start drawing (can change to shift)
     *  @param gridY - y position to start drawing (can change to shift)
     *  @param bricks - list of all bricks to draw
     *  @param width  - the width of a single unit brick
	 *	@return void
     */
    public void drawBlocks(Graphics g, int gridX, int gridY, Vector<LBrick> bricks, int width) {
        int colLength = width * xSize;
        int rowLength = width * ySize;
        int i = 0;
        int size = bricks.size();
        LBrick brick;
        while (i < size) {
            brick = bricks.get(i);
            switch(brick.LegoBlockUsed) {
                case 0:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width);
                    break;
                case 1:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width);
                    break;
                case 2:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 2);
                    break;
                case 3:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 3, width);
                    break;
                case 4:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 3);
                    break;
                case 5:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 4, width);
                    break;
                case 6:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 4);
                    break;
                case 7:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 6, width);
                    break;
                case 8:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 6);
                    break;
                case 9:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 8, width);
                    break;
                case 10:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 8);
                    break;
                case 11:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 2);
                    g.fillRect(gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width, width, width);
                    break;
                case 12:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width);
                    g.fillRect(gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width, width, width);
                    break;
                case 13:
                    g.fillRect(gridX + (brick.legoXpos + 1) * width, gridY + brick.legoYpos * width, width, width * 2);
                    g.fillRect(gridX + (brick.legoXpos) * width, gridY + (brick.legoYpos + 1) * width, width, width);
                    break;
                case 14:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width);
                    g.fillRect(gridX + (brick.legoXpos) * width, gridY + (brick.legoYpos + 1) * width, width, width);
                    break;
                case 15:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 2);
                    break;
                case 16:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 3, width * 2);
                    break;
                case 17:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 3);
                    break;
                case 18:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 4, width * 2);
                    break;
                case 19:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 4);
                    break;
                case 20:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 6, width * 2);
                    break;
                case 21:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 6);
                    break;
                case 22:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 8, width * 2);
                    break;
                case 23:
                    g.fillRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 8);
                    break;
                default:
                    System.out.println("UNKNOWN BLOCK");
            }
            i++;
        }
    }

    /**
     * This function draws only an outline of a rectangle. I used it to draw a border around the leog bricks.
     *  @param g - graphics2D object to draw on
     *  @param gridX - x position to start drawing (can change to shift)
     *  @param gridY - y position to start drawing (can change to shift)
     *  @param bricks - list of all bricks to draw
     *  @param width  - the width of a single unit brick
	 *	@return void
     */
    public void drawBlocksOutline(Graphics g, int gridX, int gridY, Vector<LBrick> bricks, int width) {
        int i = 0;
        int size = bricks.size();
        LBrick brick;
        while (i < size) {
            brick = bricks.get(i);
            switch(brick.LegoBlockUsed) {
                case 0:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width);
                    break;
                case 1:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width);
                    break;
                case 2:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 2);
                    break;
                case 3:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 3, width);
                    break;
                case 4:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 3);
                    break;
                case 5:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 4, width);
                    break;
                case 6:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 4);
                    break;
                case 7:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 6, width);
                    break;
                case 8:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 6);
                    break;
                case 9:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 8, width);
                    break;
                case 10:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width, width * 8);
                    break;
                case 11:
                    g.drawLine(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 2) * width);
                    g.drawLine(gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 2) * width, gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 2) * width);
                    g.drawLine(gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 2) * width, gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 1) * width);
                    g.drawLine(gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 1) * width, gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width);
                    g.drawLine(gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width, gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos) * width);
                    g.drawLine(gridX + (brick.legoXpos + 1) * width, gridY + brick.legoYpos * width, gridX + brick.legoXpos * width, gridY + brick.legoYpos * width);
                    break;
                case 12:
                    g.drawLine(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 1) * width);
                    g.drawLine(gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 1) * width, gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width);
                    g.drawLine(gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width, gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 2) * width);
                    g.drawLine(gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 2) * width, gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 2) * width);
                    g.drawLine(gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 2) * width, gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos) * width);
                    g.drawLine(gridX + (brick.legoXpos + 2) * width, gridY + brick.legoYpos * width, gridX + brick.legoXpos * width, gridY + brick.legoYpos * width);
                    break;
                case 13:
                    g.drawLine(gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 2) * width, gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 2) * width);
                    g.drawLine(gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 2) * width, gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos) * width);
                    g.drawLine(gridX + (brick.legoXpos + 2) * width, gridY + brick.legoYpos * width, gridX + (brick.legoXpos + 1) * width, gridY + brick.legoYpos * width);
                    g.drawLine(gridX + (brick.legoXpos + 1) * width, gridY + brick.legoYpos * width, gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width);
                    g.drawLine(gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width, gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 1) * width);
                    g.drawLine(gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 1) * width, gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 2) * width);
                    break;
                case 14:
                    g.drawLine(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 2) * width);
                    g.drawLine(gridX + brick.legoXpos * width, gridY + (brick.legoYpos + 2) * width, gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 2) * width);
                    g.drawLine(gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 2) * width, gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width);
                    g.drawLine(gridX + (brick.legoXpos + 1) * width, gridY + (brick.legoYpos + 1) * width, gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 1) * width);
                    g.drawLine(gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos + 1) * width, gridX + (brick.legoXpos + 2) * width, gridY + (brick.legoYpos) * width);
                    g.drawLine(gridX + (brick.legoXpos + 2) * width, gridY + brick.legoYpos * width, gridX + brick.legoXpos * width, gridY + brick.legoYpos * width);
                    break;
                case 15:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 2);
                    break;
                case 16:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 3, width * 2);
                    break;
                case 17:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 3);
                    break;
                case 18:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 4, width * 2);
                    break;
                case 19:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 4);
                    break;
                case 20:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 6, width * 2);
                    break;
                case 21:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 6);
                    break;
                case 22:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 8, width * 2);
                    break;
                case 23:
                    g.drawRect(gridX + brick.legoXpos * width, gridY + brick.legoYpos * width, width * 2, width * 8);
                    break;
                default:
                    System.out.println("UNKNOWN BLOCK");
            }
            i++;
        }
    }

    /**
	 * This function creates the LEGO building instructions from the 
	 * layout files saved during model creation.
	 * It also adds a list of all the bricks that was used to build it.
	 * @param whether the instructions must be generated. Used in debugging.
	 * @return void
	 */
    public int saveBuildingInstructionstoPDF(boolean mustWrite) {
        int totalbricks = 0;
        Paragraph p;
        Chunk chunk;
        int width;
        int i;
        File fileToDelete;
        if (mustWrite) {
            try {
                Document document = new Document(PageSize.A0, 50, 50, 50, 50);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(layersDirectory + "BuildingInstructions" + firstLayer + "_" + lastLayer + ".pdf"));
                writer.setPageEvent(new pdfListener());
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                int canvasWidth = (int) PageSize.A0.width() - 200;
                int canvasHeight = (int) PageSize.A0.height() - 200;
                if (xSize != ySize) {
                    if (xSize > ySize) {
                        width = (int) Math.floor((double) (canvasWidth - 100) / (double) xSize);
                    } else {
                        width = (int) Math.floor((double) (canvasWidth - 100) / (double) ySize);
                    }
                } else {
                    width = (int) Math.floor((double) (canvasWidth - 100) / (double) xSize);
                }
                for (i = firstLayer; i <= lastLayer; i++) {
                    p = new Paragraph();
                    chunk = new Chunk("Layout Layer " + i + " :", FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD, Color.black));
                    chunk.setUnderline(1.5f, -2.5f);
                    p.add(chunk);
                    document.add(p);
                    Graphics2D g = cb.createGraphics(PageSize.A0.width(), PageSize.A0.height());
                    g.setColor(Color.LIGHT_GRAY);
                    drawGrid(g, width);
                    if (i > firstLayer) {
                        g.setColor(Color.gray);
                        loadLayerBricks(i - 1);
                        drawBlocks(g, 100, 150, currentLayerBricks, width);
                        g.setColor(Color.DARK_GRAY);
                        drawBlocksOutline(g, 100, 150, currentLayerBricks, width);
                    }
                    g.setColor(Color.LIGHT_GRAY);
                    loadLayerBricks(i);
                    drawBlocks(g, 100, 150, currentLayerBricks, width);
                    g.setColor(Color.BLACK);
                    drawBlocksOutline(g, 100, 150, currentLayerBricks, width);
                    g.setColor(Color.BLACK);
                    g.dispose();
                    cb.saveState();
                    document.newPage();
                }
                p = new Paragraph();
                chunk = new Chunk("Bricks required to build sculpture:", FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD, Color.black));
                chunk.setUnderline(1.5f, -2.5f);
                p.add(chunk);
                document.add(p);
                float[] widths = { 0.60f, 0.40f };
                PdfPTable table = new PdfPTable(widths);
                table.setTotalWidth(250);
                table.setLockedWidth(true);
                table.setSpacingBefore(25f);
                table.getDefaultCell().setBackgroundColor(new Color(0.59f, 0.6f, 0.97f));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.setHorizontalAlignment(Element.ALIGN_LEFT);
                PdfPCell cell = new PdfPCell(new Paragraph("Type", FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD)));
                cell.setBackgroundColor(new Color(0.34f, 0.41f, 0.98f));
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph("Amount required", FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD)));
                cell.setBackgroundColor(new Color(0.34f, 0.41f, 0.98f));
                table.addCell(cell);
                totalbricks += totalNumLegoBricksUsed[0];
                table.addCell("" + LBricks.blockNames[0]);
                table.addCell("" + totalNumLegoBricksUsed[0]);
                for (i = 1; i < 10; i = i + 2) {
                    totalbricks += totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1];
                    table.addCell("" + LBricks.blockNames[i]);
                    table.addCell("" + (totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1]));
                }
                for (i = 11; i < 16; i++) {
                    totalbricks += totalNumLegoBricksUsed[i];
                    table.addCell("" + LBricks.blockNames[i]);
                    table.addCell("" + totalNumLegoBricksUsed[i]);
                }
                for (i = 16; i < LBricks.numLegoBricks - 1; i = i + 2) {
                    totalbricks += totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1];
                    table.addCell("" + LBricks.blockNames[i]);
                    table.addCell("" + (totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1]));
                }
                cell = new PdfPCell(new Paragraph("Total", FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD)));
                cell.setBackgroundColor(Color.YELLOW);
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph("" + totalbricks, FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD)));
                cell.setBackgroundColor(Color.YELLOW);
                table.addCell(cell);
                document.add(table);
                document.close();
            } catch (Exception e) {
                System.out.println("An error occured while trying to save the list of LEGO bricks used.");
                System.out.println(e.getMessage());
                System.exit(0);
            }
        } else {
            for (i = 0; i < LBricks.numLegoBricks; i++) {
                totalbricks += totalNumLegoBricksUsed[i];
            }
        }
        return totalbricks;
    }

    public void onStartPage(PdfWriter writer, Document document) {
        if (writer.getPageNumber() < 3) {
            PdfContentByte cb = writer.getDirectContentUnder();
            cb.saveState();
            cb.setColorFill(Color.pink);
            cb.beginText();
            cb.showTextAligned(Element.ALIGN_CENTER, "My Watermark Under " + writer.getPageNumber(), document.getPageSize().width() / 2, document.getPageSize().height() / 2, 45);
            cb.endText();
            cb.restoreState();
        }
    }

    /**
	 * This function loads the bricks from file and stores them in currentLayerBricks.
	 * The funtion is used for pdf output to load the wanted layer's bricks.
	 * 
	 * @param layer - The layer to laod from file.
	 * @return boolean - Whether succesful in loading bricks
	 */
    public boolean loadLayerBricks(int layer) {
        FileReader reader;
        BufferedReader file;
        Vector<LBrick> layerBricks;
        String line;
        String[] values;
        int lineNum = 0;
        LBrick newBrick;
        try {
            reader = new FileReader(layersDirectory + "\\layerLayout" + layer + ".ll");
            file = new BufferedReader(reader);
            currentLayerBricks = new Vector<LBrick>();
            layerBricks = currentLayerBricks;
            line = file.readLine();
            lineNum++;
            line = file.readLine();
            lineNum++;
            line = file.readLine();
            lineNum++;
            if (xSize < 0) {
                values = line.split(" ");
                ySize = Integer.parseInt(values[0]);
                xSize = Integer.parseInt(values[1]);
            }
            while ((line = file.readLine()) != null) {
                values = line.split(" ");
                newBrick = new LBrick(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                layerBricks.add(newBrick);
                lineNum++;
            }
            file.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while reading layer : " + layer + " on line " + lineNum, "Layout file error occured", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
	 * (DEBUGGING)
	 * Uset to print out a given bitSet. 
	 * Mostly used for debugging purposes
	 * @param set	- the bitset to print
	 * @param size - how far to print in bitset
	 * @return void
	 */
    public void printBitset(BitSet set, int size) {
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i)) {
                System.out.print("" + 1);
            } else System.out.print("" + 0);
        }
        System.out.println();
        System.out.println();
    }

    /**
	 * This function prints the LTL formula tree (DEBUGGING)
	 * @param root - The root of the tree to print
	 * @return void 
	 */
    public void printTreeConsole(LayerStateNode root) {
        StateEdge edge;
        LayerStateNode rootNode;
        Vector<LayerStateNode> parentNodes;
        Vector<LayerStateNode> childrenAddedNodes = new Vector<LayerStateNode>();
        childrenAddedNodes.add(root);
        int k = 0;
        System.out.println();
        while (childrenAddedNodes.size() > 0) {
            System.out.println("Layer : " + k);
            parentNodes = childrenAddedNodes;
            childrenAddedNodes = new Vector<LayerStateNode>();
            while (parentNodes.size() > 0) {
                rootNode = parentNodes.remove(0);
                printBitset((rootNode).squares, fillingOrderSquaresToFill.length);
                for (int j = 0; j < rootNode.childrenStates.size(); j++) {
                    edge = rootNode.childrenStates.get(j);
                    childrenAddedNodes.add(edge.childState);
                }
            }
            k++;
        }
    }

    /**
	 * The function uses a depth first search to find all possible building patterns 
	 * constructed. It prints out a list of blocks used. 
	 * @param root - the root of the tree to print
	 * @param listBlocks - string to identify list
	 * @return void
	 */
    public void printAllBuildLayouts(LayerStateNode root, String listBlocks) {
        StateEdge edge;
        int size = root.childrenStates.size();
        if (size == 0) {
            System.out.println(listBlocks);
            System.out.println("Cost :" + root.stateCost);
            System.out.println();
        }
        for (int i = 0; i < size; i++) {
            edge = root.childrenStates.get(i);
            printAllBuildLayouts(edge.childState, listBlocks + edge.brickUsed.LegoBlockUsed + ",");
        }
    }
}
