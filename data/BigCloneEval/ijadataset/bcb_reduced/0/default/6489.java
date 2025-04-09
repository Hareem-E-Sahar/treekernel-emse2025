import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This class represents the main execution class. It creates a GUI window in
 * which the user can select to open a given LEGO sculpture.
 * 
 * @author Eugene Smal
 * 
 */
public class LVisual extends JFrame implements ActionListener {

    public static final int numLegoBricks = 24;

    public static String[] blockNames = { "1 x 1", "1 x 2", "2 x 1", "1 x 3", "3 x 1", "1 x 4", "4 x 1", "1 x 6", "6 x 1", "1 x 8", "8 x 1", "L-Shaped corner", "L-Shape (rotated 180)", "L-Shape (rotated 90)", "L-Shape (rotated -90)", "2 x 2", "2 x 3", "3 x 2", "2 x 4", "4 x 2", "2 x 6", "6 x 2", "2 x 8", "8 x 2" };

    JMenuBar menuBar;

    JMenu menu;

    JMenuItem genInstructMenuItem;

    JMenuItem showFullSculptureMenuItem;

    JMenuItem menuItem;

    JMenuItem loadItem;

    JMenuItem closeItem;

    String layoutFileExtension;

    String directoryPath;

    int firstLayer;

    int lastLayer;

    int xSize = -5;

    int ySize;

    Vector<LBrick> previousLayer = null;

    Vector<LBrick> currentLayer = null;

    Vector<LBrick> temp3DLayer = null;

    int currentLayerNum = 0;

    controlPane mainPane;

    boolean mustDrawPrevious = true;

    boolean mustDrawGrid = false;

    public int[] totalNumLegoBricksUsed = new int[numLegoBricks];

    View2D view2D;

    JFrame fullSculptureView = null;

    /**
	 * @category Constructor It creates a LEGOVisual GUI window which the user
	 *           can use to load a LEGO sculpture.
	 */
    public LVisual() {
        this.setTitle("LSculpturer: LVisual");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        this.setSize(500, 500);
        this.setResizable(false);
        mainPane = new controlPane(this);
        setContentPane(mainPane);
        mainPane.setVisible(false);
        createMenuBar();
        this.setVisible(true);
    }

    /**
	 * This function creates the menu bar containing options to load a brick
	 * sculpture, help menu, generate brick building instructions, and displaying
	 * a 3D view of entire statue.
	 */
    public void createMenuBar() {
        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        loadItem = new JMenuItem("Load LSculpture", KeyEvent.VK_L);
        loadItem.addActionListener(this);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, java.awt.Event.CTRL_MASK));
        menu.add(loadItem);
        closeItem = new JMenuItem("Close Current Sculpture", KeyEvent.VK_C);
        closeItem.addActionListener(this);
        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.Event.CTRL_MASK));
        closeItem.setEnabled(false);
        menu.add(closeItem);
        menu.addSeparator();
        menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, java.awt.Event.CTRL_MASK, false));
        menu.add(menuItem);
        menu = new JMenu("Options");
        menu.setMnemonic(KeyEvent.VK_O);
        menuBar.add(menu);
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        showFullSculptureMenuItem = new JMenuItem("Show complete 3D sculpture", KeyEvent.VK_V);
        showFullSculptureMenuItem.addActionListener(this);
        showFullSculptureMenuItem.setEnabled(false);
        menu.add(showFullSculptureMenuItem);
        genInstructMenuItem = new JMenuItem("Generate LEGO Building Instructions", KeyEvent.VK_G);
        genInstructMenuItem.addActionListener(this);
        genInstructMenuItem.setEnabled(false);
        menu.add(genInstructMenuItem);
        menuBar.add(Box.createHorizontalGlue());
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);
        menuItem = new JMenuItem("General Information", KeyEvent.VK_I);
        menuItem.setIcon(new ImageIcon("images\\helpIcon.gif"));
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menu.addSeparator();
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        menuItem = new JMenuItem("About", KeyEvent.VK_A);
        menuItem.setIcon(new ImageIcon("images\\aboutIcon.gif"));
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        setJMenuBar(menuBar);
    }

    /**
	 * This function check in the directory whether there exist layer layout
	 * files (layerLayout.ll or .ll2). It also determines the largest continuous
	 * sequence.
	 * 
	 * @param extension
	 *            The file extension to check for. colour or normal layout
	 *            files.
	 * @return true if found some layers, else false
	 */
    public boolean foundLayers(String extension) {
        int i;
        FileTypeFilter filter = new FileTypeFilter(extension);
        File directory = new File(directoryPath);
        File[] layerFiles = directory.listFiles(filter);
        if (layerFiles.length == 0) {
            return false;
        }
        int[] numFound = new int[layerFiles.length];
        int numFoundInt = 0;
        String temp;
        for (i = 0; i < layerFiles.length; i++) {
            if (layerFiles[i].getName().startsWith("layerLayout")) {
                temp = layerFiles[i].getName();
                temp = temp.replaceAll("layerLayout", "");
                temp = temp.replaceAll("." + extension, "");
                try {
                    numFound[i] = Integer.parseInt(temp);
                    numFoundInt++;
                } catch (Exception e) {
                }
            }
        }
        Arrays.sort(numFound);
        if (numFound[numFoundInt - 1] - numFound[0] + 1 == numFoundInt) {
            firstLayer = numFound[0];
            lastLayer = numFound[layerFiles.length - 1];
            layoutFileExtension = extension;
            return true;
        } else {
            return false;
        }
    }

    /**
	 * This function loads the bricks for the given layer into one of the three
	 * lists. Will mostly only be used to load into next or previous, since will
	 * just swap the list around instead of reloading. If an error occurs, it
	 * will prompt the user to either retry after fixing or to close the
	 * sculpture.It returns True if success or else false;
	 * 
	 * @param layer
	 *            The layer to load.
	 * @param whichLayer
	 *            Which layer to load the bricks into. PREVIOUS,CURRENT,NEXT
	 *            lists
	 * @return true if load successful, else false.
	 */
    public boolean loadLayerBricks(int layer, String whichLayer) {
        FileReader reader;
        BufferedReader file;
        Vector<LBrick> layerBricks;
        String line;
        String[] values;
        int lineNum = 0;
        LBrick newBrick;
        try {
            reader = new FileReader(directoryPath + "\\layerLayout" + layer + "." + layoutFileExtension);
            file = new BufferedReader(reader);
            if (whichLayer.equals("CURRENT")) {
                currentLayer = new Vector<LBrick>();
                layerBricks = currentLayer;
            } else if (whichLayer.equals("PREVIOUS")) {
                previousLayer = new Vector<LBrick>();
                layerBricks = previousLayer;
            } else {
                temp3DLayer = new Vector<LBrick>();
                layerBricks = temp3DLayer;
            }
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
                if (layoutFileExtension.equals("ll2")) {
                    newBrick = new LBrick(Integer.parseInt(values[0]), Integer.parseInt(values[2]), Integer.parseInt(values[3]), Integer.parseInt(values[1]));
                } else {
                    newBrick = new LBrick(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), -1);
                }
                layerBricks.add(newBrick);
                lineNum++;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while reading layer : " + layer + " on line " + lineNum, "Layout file error occured", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
	 * This function closes and frees all resources for sculpture viewed.
	 */
    public void closeCurrentSculpture() {
        view2D.dispose();
        mainPane.setVisible(false);
        closeItem.setEnabled(false);
        loadItem.setEnabled(true);
        showFullSculptureMenuItem.setEnabled(false);
        genInstructMenuItem.setEnabled(false);
        mustDrawPrevious = true;
        mustDrawGrid = false;
        previousLayer = null;
        currentLayer = null;
        xSize = -5;
    }

    /**
	 * This function adds all the layers to the control pane list
	 */
    public void createLayerList() {
        int i = firstLayer;
        Vector<String> listData = new Vector<String>();
        while (i <= lastLayer) {
            listData.add("Layer " + i);
            i++;
        }
        mainPane.layerList.setListData(listData);
        mainPane.layerList.setSelectedIndex(0);
    }

    /**
	 * This function is called when an layer is selected for view in List
	 * LayerTodisplay is the number of the current layer
	 * 
	 * @param layerToDisplay
	 *            The number of the layer to display
	 * @param loadPrevious
	 *            Whether to load the previous layer as well
	 * @param loadCurrent
	 *            Whether to load the next layer as well.
	 */
    public void displayLayer(int layerToDisplay, boolean loadPrevious, boolean loadCurrent) {
        Vector<LBrick> tempLayer = previousLayer;
        if (loadPrevious) {
            if (!loadLayerBricks(layerToDisplay - 1, "PREVIOUS")) {
                closeCurrentSculpture();
            }
        } else {
            previousLayer = currentLayer;
        }
        if (loadCurrent) {
            if (!loadLayerBricks(layerToDisplay, "CURRENT")) {
                closeCurrentSculpture();
            }
        } else {
            currentLayer = tempLayer;
        }
        mainPane.currentLayerNumLab.setText("" + layerToDisplay);
        currentLayerNum = layerToDisplay;
        mainPane.numBricksLab.setText("" + currentLayer.size());
        mainPane.layerList.setSelectedIndex(layerToDisplay - firstLayer);
        view2D.reloadBricksTab(currentLayer);
        view2D.refresh3DLayers();
        view2D.canvas.repaint();
    }

    /**
	 * This function handles all user interaction.
	 * 
	 * @param evt
	 *            The event that was triggered.
	 */
    public void actionPerformed(ActionEvent evt) {
        String name = evt.getActionCommand();
        if (name.equals("Load LSculpture")) {
            JFileChooser chooser = new JFileChooser(System.getProperties().getProperty("user.dir"));
            chooser.setDialogTitle("Select the directory containing the layer layout files");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                directoryPath = chooser.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }
            if (foundLayers("ll") || foundLayers("ll2")) {
                currentLayerNum = firstLayer;
                createLayerList();
                mainPane.totalNumLayersLab.setText("" + (lastLayer + 1 - firstLayer));
                mainPane.currentLayerNumLab.setText("" + 1);
                if (firstLayer == lastLayer) {
                    mainPane.previousLayerBut.setEnabled(false);
                    mainPane.nextLayerBut.setEnabled(false);
                } else {
                    mainPane.previousLayerBut.setEnabled(false);
                    mainPane.nextLayerBut.setEnabled(true);
                }
                if (!loadLayerBricks(currentLayerNum, "CURRENT")) {
                    closeCurrentSculpture();
                }
                mainPane.numBricksLab.setText("" + currentLayer.size());
                view2D = new View2D(this, layoutFileExtension);
                mainPane.setVisible(true);
                showFullSculptureMenuItem.setEnabled(true);
                genInstructMenuItem.setEnabled(true);
                loadItem.setEnabled(false);
                closeItem.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(null, "No layout files where found. (layerLayout?.ll)", "No Layouts Found", JOptionPane.ERROR_MESSAGE);
            }
        } else if (name.equals("Close Current Sculpture")) {
            closeCurrentSculpture();
        } else if (name.equals("Exit")) {
            System.exit(0);
        } else if (evt.getSource().equals(showFullSculptureMenuItem)) {
            if (fullSculptureView == null) {
                fullSculptureView = new JFrame();
                fullSculptureView.setTitle("Complete 3D view of the LEGO sculpture");
                fullSculptureView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                fullSculptureView.setSize(400, 400);
                fullSculptureView.add(new View3D(this));
                fullSculptureView.setVisible(true);
            } else {
                if (!fullSculptureView.isActive()) {
                    fullSculptureView = new JFrame();
                    fullSculptureView.setTitle("Complete 3D view of the LEGO sculpture");
                    fullSculptureView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    fullSculptureView.setSize(400, 400);
                    fullSculptureView.add(new View3D(this));
                    fullSculptureView.setVisible(true);
                }
            }
        } else if (evt.getSource().equals(genInstructMenuItem)) {
            saveBuildingInstructionstoPDF(true);
        } else if (name.equals("General Information")) {
            helpGUI helpWindow = new helpGUI();
        } else if (name.equals("About")) {
            JOptionPane.showMessageDialog(null, "LSculpturer: LVisual \nVersion 1.0 \nAuthor : Eugene Smal \nContact: eugene.smal@gmail.com \nStellenbosch University Master's Student" + " \n2008 \n \n", "About LSculpturer: LVisual", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("images\\about.gif"));
        } else if (evt.getSource().equals(mainPane.nextLayerBut)) {
            mainPane.previousLayerBut.setEnabled(true);
            displayLayer(currentLayerNum + 1, false, true);
            mainPane.layerList.ensureIndexIsVisible(currentLayerNum);
            if (currentLayerNum == lastLayer) {
                mainPane.nextLayerBut.setEnabled(false);
            }
        } else if (evt.getSource().equals(mainPane.previousLayerBut)) {
            mainPane.nextLayerBut.setEnabled(true);
            if (currentLayerNum - 1 == firstLayer) {
                currentLayer = null;
                displayLayer(currentLayerNum - 1, false, false);
                mainPane.previousLayerBut.setEnabled(false);
                previousLayer = null;
            } else {
                displayLayer(currentLayerNum - 1, true, false);
            }
            mainPane.layerList.ensureIndexIsVisible(currentLayerNum);
        } else if (evt.getSource().equals(mainPane.showPreviousCheck)) {
            mustDrawPrevious = mainPane.showPreviousCheck.isSelected();
            view2D.canvas.repaint();
            view2D.refresh3DLayers();
        } else if (evt.getSource().equals(mainPane.showGridCheck)) {
            mustDrawGrid = mainPane.showGridCheck.isSelected();
            view2D.canvas.repaint();
        }
    }

    /**
	 * This function draws a grid similar to graph paper.
	 * 
	 * @param g  Graphics2D object to draw on
	 * @param width The width of a unit brick (square)
	 * @return void
	 */
    public void drawGrid(Graphics2D g, int width) {
        try {
            int colLength = width * xSize;
            int rowLength = width * ySize;
            int i = ySize;
            int x = 40;
            int y = 0;
            while (i >= 0) {
                g.drawLine(x, y, x + colLength, y);
                y += width;
                i--;
            }
            x = 40;
            y = 0;
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
	 * This function is used only when multicoloured sculptures are viewed. It
	 * returns the colour of the brick.
	 * @param colType The brick colour type value. 
	 */
    Color getBrickColor(int colType) {
        switch(colType) {
            case 0:
                return Color.magenta;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.YELLOW;
            case 3:
                return Color.red;
            case 4:
                return Color.GREEN;
            case 5:
                return Color.black;
            case 6:
                return Color.WHITE;
            case 7:
                return Color.ORANGE;
            case 8:
                return Color.GRAY;
            default:
                return Color.GRAY;
        }
    }

    /**
	 * This function draws each brick block filled.
	 * 
	 * @param g   graphics2D object to draw on
	 * @param gridX  x position to start drawing (can change to shift)
	 * @param gridY  y position to start drawing (can change to shift)
	 * @param bricks list of all bricks to draw
	 * @param width  the width of a single unit brick
	 * @return void
	 */
    public void drawBlocks(Graphics g, int gridX, int gridY, Vector<LBrick> bricks, int width, boolean countBricks) {
        int colLength = width * xSize;
        int rowLength = width * ySize;
        int i = 0;
        int size = bricks.size();
        LBrick brick;
        while (i < size) {
            brick = bricks.get(i);
            if (countBricks) {
                totalNumLegoBricksUsed[brick.brickType]++;
            }
            if (layoutFileExtension.equals("ll2")) {
                g.setColor(getBrickColor(brick.colour));
            }
            switch(brick.brickType) {
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
	 * This function draws only an outline of a rectangle. I used it to draw a
	 * border around the bricks.
	 * 
	 * @param g graphics2D object to draw on
	 * @param gridX x position to start drawing (can change to shift)
	 * @param gridY y position to start drawing (can change to shift)
	 * @param bricks list of all bricks to draw
	 * @param width the width of a single unit brick
	 * @return void
	 */
    public void drawBlocksOutline(Graphics g, int gridX, int gridY, Vector<LBrick> bricks, int width) {
        int i = 0;
        int size = bricks.size();
        LBrick brick;
        while (i < size) {
            brick = bricks.get(i);
            switch(brick.brickType) {
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
	 * This function creates the LEGO building instructions from the layout
	 * files saved during model creation. It also adds a list of all the bricks
	 * that was used to build it.
	 * 
	 * @param mustWrite Whether the instructions must be generated. Used in debugging.
	 * @return int The total amount of bricks used in sculpture
	 */
    public int saveBuildingInstructionstoPDF(boolean mustWrite) {
        int totalbricks = 0;
        Paragraph p;
        Chunk chunk;
        int width;
        int i;
        if (mustWrite) {
            try {
                Document document = new Document(PageSize.A0, 50, 50, 50, 50);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "\\" + "BuildingInstructions" + firstLayer + "_" + lastLayer + ".pdf"));
                writer.setPageEvent(new pdfListener());
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                int canvasWidth = (int) PageSize.A0.width() - 200;
                int canvasHeight = (int) PageSize.A0.height() - 200;
                if (xSize != ySize) {
                    if (xSize > ySize) {
                        width = (int) Math.floor((double) (canvasWidth - 40) / (double) xSize);
                    } else {
                        width = (int) Math.floor((double) (canvasWidth - 40) / (double) ySize);
                    }
                } else {
                    width = (int) Math.floor((double) (canvasWidth - 40) / (double) xSize);
                }
                for (i = firstLayer; i <= lastLayer; i++) {
                    p = new Paragraph();
                    chunk = new Chunk("Layout Layer " + i + " :", FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD, Color.black));
                    chunk.setUnderline(1.5f, -2.5f);
                    p.add(chunk);
                    document.add(p);
                    Graphics2D g = cb.createGraphics(canvasWidth, canvasHeight);
                    g.setColor(Color.LIGHT_GRAY);
                    drawGrid(g, width);
                    if (!layoutFileExtension.equals("ll2")) {
                        if (i > firstLayer) {
                            g.setColor(Color.gray);
                            loadLayerBricks(i - 1, "CURRENT");
                            drawBlocks(g, 40, 0, currentLayer, width, false);
                            g.setColor(Color.DARK_GRAY);
                            drawBlocksOutline(g, 40, 0, currentLayer, width);
                        }
                    }
                    if (!layoutFileExtension.equals(".sl2")) {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    loadLayerBricks(i, "CURRENT");
                    drawBlocks(g, 40, 0, currentLayer, width, true);
                    g.setColor(Color.BLACK);
                    drawBlocksOutline(g, 40, 0, currentLayer, width);
                    g.setColor(Color.BLACK);
                    g.dispose();
                    cb.saveState();
                    document.newPage();
                }
                p = new Paragraph();
                chunk = new Chunk("LEGO bricks required to build sculpture:", FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD, Color.black));
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
                table.addCell("" + blockNames[0]);
                table.addCell("" + totalNumLegoBricksUsed[0]);
                for (i = 1; i < 10; i = i + 2) {
                    totalbricks += totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1];
                    table.addCell("" + blockNames[i]);
                    table.addCell("" + (totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1]));
                }
                for (i = 11; i < 16; i++) {
                    totalbricks += totalNumLegoBricksUsed[i];
                    table.addCell("" + blockNames[i]);
                    table.addCell("" + totalNumLegoBricksUsed[i]);
                }
                for (i = 16; i < numLegoBricks - 1; i = i + 2) {
                    totalbricks += totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1];
                    table.addCell("" + blockNames[i]);
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
                JOptionPane.showMessageDialog(null, "An error occured while trying to save the list of LEGO bricks used.", "Error occured", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } else {
            for (i = 0; i < numLegoBricks; i++) {
                totalbricks += totalNumLegoBricksUsed[i];
            }
        }
        JOptionPane.showMessageDialog(null, "Brick sculpture building instructions saved to \n" + directoryPath + "\\" + "BuildingInstructions" + firstLayer + "_" + lastLayer + ".pdf", "LEGO instructions generated", JOptionPane.INFORMATION_MESSAGE);
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
	 * The main executable function
	 * @param args Command line input variables. (ignored if any)
	 */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        new LVisual();
    }
}
