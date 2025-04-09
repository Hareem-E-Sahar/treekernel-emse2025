import colorizer.ColorConverter;
import colorizer.TreeNode;
import fastcol.ImageMatrix;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import net.sourceforge.jiu.codecs.BMPCodec;
import net.sourceforge.jiu.codecs.CodecMode;
import net.sourceforge.jiu.codecs.UnsupportedCodecModeException;
import net.sourceforge.jiu.gui.awt.ImageCreator;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 *
 * @author  Gouda
 */
public class ColorizeIt extends javax.swing.JFrame {

    /** Creates new form ColorizeIt */
    public ColorizeIt() {
        initComponents();
        setSize(800, 500);
        initializeColorsMenu();
        progress = new ProgressForm();
        task = new Task(this);
        CardLayout cards = (CardLayout) cardPanel.getLayout();
        cards.next(cardPanel);
        brushBtn.setSelected(true);
        brushBtnActionPerformed(null);
        workingColor = new Color(-1);
        initTimer();
    }

    private void initComponents() {
        buttonGroup = new javax.swing.ButtonGroup();
        toolBox = new javax.swing.JToolBar();
        brushBtn = new javax.swing.JToggleButton();
        eraserBtn = new javax.swing.JToggleButton();
        pickerBtn = new javax.swing.JToggleButton();
        treeBtn = new javax.swing.JToggleButton();
        splitPane = new javax.swing.JSplitPane();
        rightPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        originalScrollPane = new javax.swing.JScrollPane();
        originalLabel = new javax.swing.JLabel();
        coloredScrollPane = new javax.swing.JScrollPane();
        coloredLabel = new javax.swing.JLabel();
        leftPanel = new javax.swing.JPanel();
        toolboxPanel = new javax.swing.JPanel();
        btnToolPanel = new javax.swing.JPanel();
        colorPaletteBtn = new javax.swing.JToggleButton();
        toolsBtn = new javax.swing.JToggleButton();
        cardPanel = new javax.swing.JPanel();
        colorPalettePanel = new javax.swing.JPanel();
        toolsPanel = new javax.swing.JPanel();
        treesCombo = new javax.swing.JComboBox();
        emptyPanel = new javax.swing.JPanel();
        toolBar = new javax.swing.JToolBar();
        mouseLabel = new javax.swing.JLabel();
        colorizeProgressBar = new javax.swing.JProgressBar();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        saveProjectMenuItem = new javax.swing.JMenuItem();
        loadProjectMenuItem = new javax.swing.JMenuItem();
        Colorize = new javax.swing.JMenu();
        colorize = new javax.swing.JMenuItem();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Colorize your Life !!");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        buttonGroup.add(brushBtn);
        brushBtn.setText("Brush");
        brushBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        brushBtn.setMaximumSize(new java.awt.Dimension(50, 30));
        brushBtn.setMinimumSize(new java.awt.Dimension(35, 30));
        brushBtn.setPreferredSize(new java.awt.Dimension(35, 30));
        brushBtn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brushBtnActionPerformed(evt);
            }
        });
        toolBox.add(brushBtn);
        buttonGroup.add(eraserBtn);
        eraserBtn.setIcon(new javax.swing.ImageIcon(".\\Resources\\eraser.gif"));
        eraserBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        eraserBtn.setMaximumSize(new java.awt.Dimension(30, 30));
        eraserBtn.setMinimumSize(new java.awt.Dimension(30, 30));
        eraserBtn.setPreferredSize(new java.awt.Dimension(30, 30));
        eraserBtn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eraserBtnActionPerformed(evt);
            }
        });
        toolBox.add(eraserBtn);
        buttonGroup.add(pickerBtn);
        pickerBtn.setText("ColorPicker");
        pickerBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pickerBtn.setMaximumSize(new java.awt.Dimension(70, 30));
        pickerBtn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pickerBtnActionPerformed(evt);
            }
        });
        toolBox.add(pickerBtn);
        treeBtn.setText("Tree");
        treeBtn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeBtnActionPerformed(evt);
            }
        });
        toolBox.add(treeBtn);
        getContentPane().add(toolBox, java.awt.BorderLayout.NORTH);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(4);
        splitPane.setEnabled(false);
        rightPanel.setLayout(new java.awt.GridLayout(1, 0));
        rightPanel.setPreferredSize(new java.awt.Dimension(500, 200));
        jSplitPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPane1PropertyChange(evt);
            }
        });
        originalScrollPane.setViewportBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Original")));
        originalScrollPane.setAutoscrolls(true);
        originalScrollPane.setMaximumSize(new java.awt.Dimension(200, 200));
        originalScrollPane.setMinimumSize(new java.awt.Dimension(200, 200));
        originalScrollPane.setPreferredSize(new java.awt.Dimension(200, 200));
        originalScrollPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            public void mouseMoved(java.awt.event.MouseEvent evt) {
                originalScrollPaneMouseMoved(evt);
            }
        });
        originalLabel.setAutoscrolls(true);
        originalLabel.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                originalLabelMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                originalLabelMouseReleased(evt);
            }
        });
        originalLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            public void mouseDragged(java.awt.event.MouseEvent evt) {
                originalLabelMouseDragged(evt);
            }

            public void mouseMoved(java.awt.event.MouseEvent evt) {
                originalLabelMouseMoved(evt);
            }
        });
        originalScrollPane.setViewportView(originalLabel);
        jSplitPane1.setLeftComponent(originalScrollPane);
        coloredScrollPane.setViewportBorder(javax.swing.BorderFactory.createTitledBorder("Colored"));
        coloredScrollPane.setMinimumSize(new java.awt.Dimension(200, 200));
        coloredScrollPane.setPreferredSize(new java.awt.Dimension(300, 200));
        coloredLabel.setAutoscrolls(true);
        coloredLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            public void mouseDragged(java.awt.event.MouseEvent evt) {
                coloredLabelMouseDragged(evt);
            }

            public void mouseMoved(java.awt.event.MouseEvent evt) {
                coloredLabelMouseMoved(evt);
            }
        });
        coloredLabel.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                coloredLabelMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                coloredLabelMouseReleased(evt);
            }
        });
        coloredScrollPane.setViewportView(coloredLabel);
        jSplitPane1.setRightComponent(coloredScrollPane);
        rightPanel.add(jSplitPane1);
        splitPane.setRightComponent(rightPanel);
        leftPanel.setLayout(new java.awt.BorderLayout());
        leftPanel.setPreferredSize(new java.awt.Dimension(100, 100));
        toolboxPanel.setLayout(new java.awt.BorderLayout());
        toolboxPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        btnToolPanel.setLayout(new java.awt.GridLayout(2, 2));
        colorPaletteBtn.setFont(new java.awt.Font("Franklin Gothic Medium", 1, 11));
        colorPaletteBtn.setText("Colors");
        colorPaletteBtn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorPaletteBtnActionPerformed(evt);
            }
        });
        btnToolPanel.add(colorPaletteBtn);
        toolsBtn.setFont(new java.awt.Font("Franklin Gothic Medium", 1, 11));
        toolsBtn.setText("Tools");
        toolsBtn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolsBtnActionPerformed(evt);
            }
        });
        btnToolPanel.add(toolsBtn);
        toolboxPanel.add(btnToolPanel, java.awt.BorderLayout.NORTH);
        leftPanel.add(toolboxPanel, java.awt.BorderLayout.WEST);
        cardPanel.setLayout(new java.awt.CardLayout());
        colorPalettePanel.setLayout(new java.awt.GridBagLayout());
        colorPalettePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        colorPalettePanel.setMaximumSize(new java.awt.Dimension(210, 2147483647));
        colorPalettePanel.setMinimumSize(new java.awt.Dimension(150, 2));
        colorPalettePanel.setPreferredSize(new java.awt.Dimension(150, 2));
        cardPanel.add(colorPalettePanel, "card1");
        treesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        toolsPanel.add(treesCombo);
        cardPanel.add(toolsPanel, "card2");
        cardPanel.add(emptyPanel, "card3");
        leftPanel.add(cardPanel, java.awt.BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);
        getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);
        toolBar.setMaximumSize(new java.awt.Dimension(20, 5));
        toolBar.setMinimumSize(new java.awt.Dimension(20, 5));
        mouseLabel.setText("X: ?? Y: ??");
        mouseLabel.setMaximumSize(new java.awt.Dimension(5000, 14));
        toolBar.add(mouseLabel);
        colorizeProgressBar.setMaximumSize(new java.awt.Dimension(100, 18));
        toolBar.add(colorizeProgressBar);
        getContentPane().add(toolBar, java.awt.BorderLayout.SOUTH);
        fileMenu.setText("File");
        openMenuItem.setText("Open Image");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);
        fileMenu.add(jSeparator1);
        saveProjectMenuItem.setText("Save Project");
        saveProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveProjectMenuItem);
        loadProjectMenuItem.setText("Load Project");
        loadProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadProjectMenuItem);
        menuBar.add(fileMenu);
        Colorize.setText("Run");
        colorize.setText("Colorize");
        colorize.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorizeActionPerformed(evt);
            }
        });
        Colorize.add(colorize);
        menuBar.add(Colorize);
        setJMenuBar(menuBar);
        pack();
    }

    private void treeBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (!treeBtn.isSelected()) {
            pickMode = false;
        }
    }

    private void loadProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);
        String path = "c:\\";
        String fileName = "TestSave";
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            path = fc.getSelectedFile().getParent();
            fileName = fc.getSelectedFile().getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return;
        }
        try {
            int BUFFER = 2048;
            String inFileName = path + fileName + ".zip";
            String destinationDirectory = path;
            File sourceZipFile = new File(inFileName);
            File unzipDestinationDirectory = new File(destinationDirectory);
            ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
            Enumeration zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(unzipDestinationDirectory, currentEntry);
                File destinationParent = destFile.getParentFile();
                destinationParent.mkdirs();
                if (!entry.isDirectory()) {
                    BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                    int currentByte;
                    byte data[] = new byte[BUFFER];
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            zipFile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            File originalFile = new File(path + fileName + "-original.jpg");
            File mapFile = new File(path + fileName + "-Map.jpg");
            File markedFile = new File(path + fileName + "-Marked.jpg");
            if (im == null) im = new ImageMatrix(1, 1, 1);
            im.initializeFromImage(ImageIO.read(originalFile));
            colorMapImage = ImageIO.read(mapFile);
            originalLabel.setIcon(new ImageIcon(ImageIO.read(markedFile)));
            originalFile.delete();
            mapFile.delete();
            markedFile.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        String path = "c:\\";
        String fileName = "TestSave";
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            path = fc.getSelectedFile().getParent();
            fileName = fc.getSelectedFile().getName();
        } else {
            return;
        }
        save(path + fileName + "-Map.jpg", colorMapImage);
        ImageIcon originalimage = (ImageIcon) originalLabel.getIcon();
        BufferedImage oi = (BufferedImage) originalimage.getImage();
        save(path + fileName + "-Marked.jpg", oi);
        save(path + fileName + "-original.jpg", im.getImage());
        byte[] buffer = new byte[18024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path + fileName + ".zip"));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            FileInputStream in1 = new FileInputStream(path + fileName + "-Map.jpg");
            FileInputStream in2 = new FileInputStream(path + fileName + "-Marked.jpg");
            FileInputStream in3 = new FileInputStream(path + fileName + "-original.jpg");
            out.putNextEntry(new ZipEntry(fileName + "-Map.jpg"));
            int len;
            while ((len = in1.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.putNextEntry(new ZipEntry(fileName + "-Marked.jpg"));
            len = 0;
            while ((len = in2.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.putNextEntry(new ZipEntry(fileName + "-original.jpg"));
            len = 0;
            while ((len = in3.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in1.close();
            in2.close();
            in3.close();
            File originalFile = new File(path + fileName + "-original.jpg");
            File mapFile = new File(path + fileName + "-Map.jpg");
            File markedFile = new File(path + fileName + "-Marked.jpg");
            originalFile.delete();
            mapFile.delete();
            markedFile.delete();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void save(String path, Image img) {
        try {
            BMPCodec scodec = new BMPCodec();
            scodec.setImage(ImageCreator.convertImageToRGB24Image(img));
            scodec.setFile(path, CodecMode.SAVE);
            scodec.process();
            scodec.close();
        } catch (UnsupportedCodecModeException ex) {
            ex.printStackTrace();
        } catch (MissingParameterException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (OperationFailedException ex) {
            ex.printStackTrace();
        }
    }

    boolean brushFlag = false;

    boolean eraseFlag = false;

    boolean pickerFlag = false;

    private Timer timer;

    void resetFlags() {
        brushFlag = false;
        eraseFlag = false;
        pickerFlag = false;
    }

    private void pickerBtnActionPerformed(java.awt.event.ActionEvent evt) {
        resetFlags();
        pickerFlag = pickerBtn.isSelected();
    }

    private void eraserBtnActionPerformed(java.awt.event.ActionEvent evt) {
        resetFlags();
        eraseFlag = eraserBtn.isSelected();
    }

    private void brushBtnActionPerformed(java.awt.event.ActionEvent evt) {
        resetFlags();
        brushFlag = brushBtn.isSelected();
    }

    private void toolsBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (toolsBtn.isSelected()) {
            CardLayout cards = (CardLayout) cardPanel.getLayout();
            cards.show(cardPanel, "card2");
            formComponentResized(null);
        } else {
            CardLayout cards = (CardLayout) cardPanel.getLayout();
            cards.show(cardPanel, "card3");
            formComponentResized(null);
        }
    }

    private void jSplitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {
        if (im != null) {
            if (jSplitPane1.getDividerLocation() > im.width) {
                jSplitPane1.setDividerLocation(im.width + 30);
            }
        }
    }

    private void coloredLabelMouseReleased(java.awt.event.MouseEvent evt) {
    }

    private void coloredLabelMousePressed(java.awt.event.MouseEvent evt) {
    }

    private void coloredLabelMouseMoved(java.awt.event.MouseEvent evt) {
    }

    private void coloredLabelMouseDragged(java.awt.event.MouseEvent evt) {
    }

    private void colorizeActionPerformed(java.awt.event.ActionEvent evt) {
        task.setImageMatrix(im);
        task.setcolorMap(colorMapImage);
        task.setTrees(trees);
        progress.setLocation(this.getWidth() / 2, this.getHeight() / 2);
        progress.setVisible(true);
        (new Thread(task)).start();
        timer.start();
    }

    private void colorPaletteBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (colorPaletteBtn.isSelected()) {
            CardLayout cards = (CardLayout) cardPanel.getLayout();
            cards.show(cardPanel, "card1");
            formComponentResized(null);
        } else {
            CardLayout cards = (CardLayout) cardPanel.getLayout();
            cards.show(cardPanel, "card3");
            formComponentResized(null);
        }
    }

    private void originalLabelMouseDragged(java.awt.event.MouseEvent evt) {
        if (treeBtn.isSelected()) {
            ImageIcon originalimage = (ImageIcon) originalLabel.getIcon();
            int position = 0;
            BufferedImage oi = (BufferedImage) originalimage.getImage();
            if (originalLabel.getHeight() - oi.getHeight() > 0) position = (originalLabel.getHeight() - oi.getHeight()) / 2; else position = 0;
            Graphics g = originalLabel.getGraphics();
            g.drawImage(oi, 0, position, null);
            g.setColor(Color.RED);
            drawRectangle(g, treeSelectionAreaStartX, treeSelectionAreaStartY, evt.getX(), evt.getY());
            return;
        }
        if (brushFlag) drawMarkers(evt);
        if (eraseFlag) eraseMarkers(evt);
    }

    boolean pickMode = false;

    ArrayList<TreeNode> trees = new ArrayList<TreeNode>();

    private void originalLabelMouseReleased(java.awt.event.MouseEvent evt) {
        if (treeBtn.isSelected() && !pickMode) {
            ImageIcon originalimage = (ImageIcon) originalLabel.getIcon();
            int position = 0;
            BufferedImage oi = (BufferedImage) originalimage.getImage();
            if (originalLabel.getHeight() - oi.getHeight() > 0) position = (originalLabel.getHeight() - oi.getHeight()) / 2; else position = 0;
            treeSelectionAreaEndX = evt.getX();
            treeSelectionAreaEndY = evt.getY();
            pickMode = true;
            trees.add(new TreeNode(treeSelectionAreaStartX, treeSelectionAreaStartY - position, treeSelectionAreaEndX, treeSelectionAreaEndY - position));
            Graphics g = oi.getGraphics();
            g.setColor(Color.RED);
            drawRectangle(g, treeSelectionAreaStartX, treeSelectionAreaStartY - position, treeSelectionAreaEndX, treeSelectionAreaEndY - position);
            originalLabel.repaint();
        }
        if (evt.getButton() == evt.BUTTON1) isDrawing = false;
    }

    void drawRectangle(Graphics g, int x1, int y1, int x2, int y2) {
        int startx = x1;
        if (startx > x2) startx = x2;
        int starty = y1;
        if (starty > y2) starty = y2;
        g.drawRect(startx, starty, Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    int treeSelectionAreaStartX = 0;

    int treeSelectionAreaStartY = 0;

    int treeSelectionAreaEndX = 0;

    int treeSelectionAreaEndY = 0;

    boolean secondPress = false;

    private void originalLabelMousePressed(java.awt.event.MouseEvent evt) {
        if (treeBtn.isSelected()) {
            if (pickMode) {
                ImageIcon originalimage = (ImageIcon) originalLabel.getIcon();
                int position = 0;
                BufferedImage oi = (BufferedImage) originalimage.getImage();
                if (originalLabel.getHeight() - oi.getHeight() > 0) position = (originalLabel.getHeight() - oi.getHeight()) / 2; else position = 0;
                int RGB = oi.getRGB(evt.getX(), evt.getY() - position);
                Color pickedColor = new Color(RGB);
                trees.get(trees.size() - 1).add(workingColor, pickedColor);
                System.err.println("Picked " + pickedColor.toString() + "-" + workingColor.toString() + "------" + trees.get(0).colors.size());
                return;
            } else {
                treeSelectionAreaStartX = evt.getX();
                treeSelectionAreaStartY = evt.getY();
                secondPress = true;
                System.out.println("first");
            }
        }
        if (pickerFlag) {
            ImageIcon originalimage = (ImageIcon) originalLabel.getIcon();
            int position = 0;
            BufferedImage oi = (BufferedImage) originalimage.getImage();
            if (originalLabel.getHeight() - oi.getHeight() > 0) position = (originalLabel.getHeight() - oi.getHeight()) / 2; else position = 0;
            int RGB = colorMapImage.getRGB(evt.getX(), evt.getY() - position);
            if (RGB != -1) {
                Color pickedColor = new Color(RGB);
                colorPicker.setColor(pickedColor);
                workingColor = pickedColor;
            }
        }
        if (evt.getButton() == evt.BUTTON1) {
            isDrawing = true;
            if (brushFlag) drawMarkers(evt);
            if (eraseFlag) eraseMarkers(evt);
        }
    }

    private void drawMarkers(MouseEvent evt) {
        if (isDrawing) {
            colorPicker.addrecentColor();
            ImageIcon originalimage = (ImageIcon) originalLabel.getIcon();
            Graphics g = originalimage.getImage().getGraphics();
            Graphics colorMapg = colorMapImage.getGraphics();
            Color drawingColor = workingColor;
            if (workingColor == null) return;
            int position = 0;
            BufferedImage oi = (BufferedImage) originalimage.getImage();
            if (originalLabel.getHeight() - oi.getHeight() > 0) position = (originalLabel.getHeight() - oi.getHeight()) / 2; else position = 0;
            byte[] wantedColor = ColorConverter.rgb2ycbcr((short) workingColor.getRed(), (short) workingColor.getGreen(), (short) workingColor.getBlue());
            int brushSize = 5;
            int halfBruch = 5 / 2;
            for (int i = -halfBruch; i < halfBruch; i++) for (int j = -halfBruch; j < halfBruch; j++) {
                int x = evt.getX() + i;
                int y = evt.getY() + j - position;
                byte[] GrayColor = ColorConverter.rgb2ycbcr(im.imageData[0][y][x], im.imageData[0][y][x], im.imageData[0][y][x]);
                short[] drawingColorComp = ColorConverter.ycbcr2rgb(GrayColor[0], wantedColor[1], wantedColor[2]);
                Color actualColor = new Color(drawingColorComp[0], drawingColorComp[1], drawingColorComp[2]);
                g.setColor(actualColor);
                colorMapg.setColor(drawingColor);
                g.drawLine(x, y, x, y);
                colorMapg.drawLine(x, y, x, y);
            }
            originalimage.getImage().flush();
            originalLabel.repaint();
            originalScrollPane.repaint();
        }
    }

    private void eraseMarkers(MouseEvent evt) {
        if (isDrawing) {
            ImageIcon originalimage = (ImageIcon) originalLabel.getIcon();
            Graphics g = originalimage.getImage().getGraphics();
            Graphics colorMapg = colorMapImage.getGraphics();
            int position = 0;
            BufferedImage oi = (BufferedImage) originalimage.getImage();
            if (originalLabel.getHeight() - oi.getHeight() > 0) position = (originalLabel.getHeight() - oi.getHeight()) / 2; else position = 0;
            int brushSize = 5;
            int halfBruch = 5 / 2;
            for (int i = -halfBruch; i < halfBruch; i++) for (int j = -halfBruch; j < halfBruch; j++) {
                int x = evt.getX() + i;
                int y = evt.getY() + j - position;
                Color actualColor = new Color(im.imageData[0][y][x], im.imageData[0][y][x], im.imageData[0][y][x]);
                g.setColor(actualColor);
                colorMapg.setColor(new Color(-1));
                colorMapg.drawLine(x, y, x, y);
                g.drawLine(x, y, x, y);
            }
            originalimage.getImage().flush();
            originalLabel.repaint();
            originalScrollPane.repaint();
        }
    }

    private void originalLabelMouseMoved(java.awt.event.MouseEvent evt) {
        mouseLabel.setText("X: " + evt.getX() + " Y: " + evt.getY());
    }

    private void originalScrollPaneMouseMoved(java.awt.event.MouseEvent evt) {
    }

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            OpenFileDialog ofd = new OpenFileDialog(this, true);
            ofd.setVisible(true);
            String imagePath = ofd.getSelectedFilePath();
            if (imagePath == null) return;
            im = new ImageMatrix(getToolkit().getImage(imagePath));
            BufferedImage bm = new BufferedImage(im.width, im.height, BufferedImage.TYPE_INT_ARGB);
            bm.getGraphics().drawImage(ImageIO.read(new File(imagePath)), 0, 0, null);
            originalLabel.setIcon(new ImageIcon(bm));
            originalLabel.setAutoscrolls(true);
            originalScrollPane.add(originalLabel);
            originalScrollPane.setViewportView(originalLabel);
            colorMapImage = new BufferedImage(im.width, im.height, BufferedImage.TYPE_INT_RGB);
            colorMapImage.getGraphics().setColor(new Color(0, 0, 0, 0));
            colorMapImage.getGraphics().fillRect(0, 0, im.width, im.height);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }

    private void formComponentResized(java.awt.event.ComponentEvent evt) {
        if (colorPaletteBtn.isSelected()) {
            System.err.println(colorPalettePanel.getWidth());
            splitPane.setDividerLocation(toolboxPanel.getWidth() + 240);
        } else {
            splitPane.setDividerLocation(toolboxPanel.getWidth());
        }
    }

    public final void ColorChoosed(Color c) {
        workingColor = c;
    }

    private void initializeColorsMenu() {
        colorChoosingMenu = new JPopupMenu("Color Chooser");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Colors");
        final JTree preDefinedColorsTree = new JTree(root);
        preDefinedColorsTree.setCellRenderer(new TreeCellRenderer() {

            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();
                Component c = dtcr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (((DefaultMutableTreeNode) value).getUserObject() instanceof String) return c;
                final Color selectedColor = (Color) ((DefaultMutableTreeNode) value).getUserObject();
                ((JLabel) c).setText("(" + selectedColor.getRed() + "," + selectedColor.getGreen() + "," + selectedColor.getBlue() + ")");
                ((JLabel) c).setHorizontalTextPosition(0);
                ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                Icon ic = new Icon() {

                    public int getIconHeight() {
                        return 15;
                    }

                    public int getIconWidth() {
                        return 90;
                    }

                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        g.setColor(selectedColor);
                        g.fillRect(x, y, 200, 100);
                    }
                };
                ((JLabel) c).setIcon(ic);
                return c;
            }
        });
        initializeTree(root);
        JPanel colorTreePanel = new JPanel();
        JScrollPane colorTreescroll = new JScrollPane();
        colorTreescroll.setViewportView(preDefinedColorsTree);
        colorTreescroll.setPreferredSize(new Dimension(200, 100));
        colorTreePanel.add(colorTreescroll);
        JMenu customColorMenu = new JMenu("Custom Colors");
        final JColorChooser customColorChooser = new JColorChooser();
        customColorChooser.setPreviewPanel(new JPanel());
        preDefinedColorsTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                Object selectedNode = ((DefaultMutableTreeNode) preDefinedColorsTree.getSelectionPath().getLastPathComponent()).getUserObject();
                if (selectedNode instanceof Color) {
                    ColorChoosed((Color) selectedNode);
                    colorPicker.setColor((Color) selectedNode);
                    colorChoosingMenu.setVisible(false);
                }
            }
        });
        customColorMenu.add(customColorChooser);
        colorChoosingMenu.add(colorTreePanel);
        colorChoosingMenu.add(customColorMenu);
        colorPicker = new colorpicker(this);
        java.awt.GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        colorPalettePanel.add(colorPicker, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        colorPalettePanel.add(colorTreePanel, gridBagConstraints);
        preDefinedColorsTree.expandPath(new TreePath(root));
    }

    public void initializeTree(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode skinColor = new DefaultMutableTreeNode("Skin");
        skinColor.add(new DefaultMutableTreeNode(new Color(247, 235, 231)));
        skinColor.add(new DefaultMutableTreeNode(new Color(246, 227, 212)));
        skinColor.add(new DefaultMutableTreeNode(new Color(245, 220, 195)));
        skinColor.add(new DefaultMutableTreeNode(new Color(245, 199, 187)));
        skinColor.add(new DefaultMutableTreeNode(new Color(245, 194, 191)));
        skinColor.add(new DefaultMutableTreeNode(new Color(214, 158, 155)));
        skinColor.add(new DefaultMutableTreeNode(new Color(245, 230, 195)));
        skinColor.add(new DefaultMutableTreeNode(new Color(226, 191, 160)));
        skinColor.add(new DefaultMutableTreeNode(new Color(196, 165, 152)));
        skinColor.add(new DefaultMutableTreeNode(new Color(191, 134, 132)));
        skinColor.add(new DefaultMutableTreeNode(new Color(155, 105, 100)));
        skinColor.add(new DefaultMutableTreeNode(new Color(145, 106, 93)));
        skinColor.add(new DefaultMutableTreeNode(new Color(159, 124, 82)));
        skinColor.add(new DefaultMutableTreeNode(new Color(138, 110, 93)));
        skinColor.add(new DefaultMutableTreeNode(new Color(140, 105, 86)));
        skinColor.add(new DefaultMutableTreeNode(new Color(123, 100, 82)));
        skinColor.add(new DefaultMutableTreeNode(new Color(122, 88, 74)));
        skinColor.add(new DefaultMutableTreeNode(new Color(108, 66, 39)));
        root.add(skinColor);
        DefaultMutableTreeNode hairColor = new DefaultMutableTreeNode("Hair");
        hairColor.add(new DefaultMutableTreeNode(new Color(206, 203, 183)));
        hairColor.add(new DefaultMutableTreeNode(new Color(176, 150, 129)));
        hairColor.add(new DefaultMutableTreeNode(new Color(132, 116, 97)));
        hairColor.add(new DefaultMutableTreeNode(new Color(232, 141, 88)));
        hairColor.add(new DefaultMutableTreeNode(new Color(184, 98, 46)));
        hairColor.add(new DefaultMutableTreeNode(new Color(175, 85, 54)));
        hairColor.add(new DefaultMutableTreeNode(new Color(175, 82, 43)));
        hairColor.add(new DefaultMutableTreeNode(new Color(133, 78, 43)));
        hairColor.add(new DefaultMutableTreeNode(new Color(144, 105, 91)));
        hairColor.add(new DefaultMutableTreeNode(new Color(190, 126, 94)));
        hairColor.add(new DefaultMutableTreeNode(new Color(95, 7, 7)));
        hairColor.add(new DefaultMutableTreeNode(new Color(64, 62, 63)));
        hairColor.add(new DefaultMutableTreeNode(new Color(25, 19, 15)));
        root.add(hairColor);
        DefaultMutableTreeNode lipsColor = new DefaultMutableTreeNode("Lips");
        lipsColor.add(new DefaultMutableTreeNode(new Color(222, 74, 91)));
        lipsColor.add(new DefaultMutableTreeNode(new Color(236, 16, 64)));
        lipsColor.add(new DefaultMutableTreeNode(new Color(184, 107, 111)));
        lipsColor.add(new DefaultMutableTreeNode(new Color(159, 117, 120)));
        lipsColor.add(new DefaultMutableTreeNode(new Color(230, 151, 148)));
        lipsColor.add(new DefaultMutableTreeNode(new Color(204, 90, 67)));
        lipsColor.add(new DefaultMutableTreeNode(new Color(196, 51, 53)));
        lipsColor.add(new DefaultMutableTreeNode(new Color(140, 44, 48)));
        lipsColor.add(new DefaultMutableTreeNode(new Color(114, 23, 1)));
        root.add(lipsColor);
        DefaultMutableTreeNode eyesColor = new DefaultMutableTreeNode("Eyes");
        eyesColor.add(new DefaultMutableTreeNode(new Color(122, 119, 137)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(90, 105, 122)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(95, 104, 141)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(71, 71, 104)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(39, 38, 70)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(117, 124, 125)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(93, 85, 86)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(99, 98, 92)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(77, 74, 62)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(99, 66, 42)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(93, 69, 45)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(85, 69, 42)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(79, 111, 103)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(243, 244, 246)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(228, 235, 232)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(140, 151, 157)));
        eyesColor.add(new DefaultMutableTreeNode(new Color(33, 10, 14)));
        root.add(eyesColor);
        DefaultMutableTreeNode waterColor = new DefaultMutableTreeNode("Water");
        waterColor.add(new DefaultMutableTreeNode(new Color(39, 61, 72)));
        waterColor.add(new DefaultMutableTreeNode(new Color(74, 93, 91)));
        waterColor.add(new DefaultMutableTreeNode(new Color(125, 164, 181)));
        waterColor.add(new DefaultMutableTreeNode(new Color(118, 147, 158)));
        waterColor.add(new DefaultMutableTreeNode(new Color(137, 159, 171)));
        waterColor.add(new DefaultMutableTreeNode(new Color(133, 154, 183)));
        waterColor.add(new DefaultMutableTreeNode(new Color(145, 179, 206)));
        waterColor.add(new DefaultMutableTreeNode(new Color(120, 127, 129)));
        waterColor.add(new DefaultMutableTreeNode(new Color(202, 204, 211)));
        waterColor.add(new DefaultMutableTreeNode(new Color(241, 242, 240)));
        waterColor.add(new DefaultMutableTreeNode(new Color(216, 221, 205)));
        waterColor.add(new DefaultMutableTreeNode(new Color(55, 67, 29)));
        waterColor.add(new DefaultMutableTreeNode(new Color(7, 54, 113)));
        waterColor.add(new DefaultMutableTreeNode(new Color(68, 120, 114)));
        root.add(waterColor);
        DefaultMutableTreeNode skyColor = new DefaultMutableTreeNode("Sky");
        skyColor.add(new DefaultMutableTreeNode(new Color(196, 245, 250)));
        skyColor.add(new DefaultMutableTreeNode(new Color(141, 214, 249)));
        skyColor.add(new DefaultMutableTreeNode(new Color(121, 199, 255)));
        skyColor.add(new DefaultMutableTreeNode(new Color(8, 116, 194)));
        skyColor.add(new DefaultMutableTreeNode(new Color(68, 117, 175)));
        skyColor.add(new DefaultMutableTreeNode(new Color(89, 132, 184)));
        skyColor.add(new DefaultMutableTreeNode(new Color(107, 148, 212)));
        skyColor.add(new DefaultMutableTreeNode(new Color(178, 207, 211)));
        skyColor.add(new DefaultMutableTreeNode(new Color(223, 244, 249)));
        skyColor.add(new DefaultMutableTreeNode(new Color(229, 254, 255)));
        skyColor.add(new DefaultMutableTreeNode(new Color(224, 229, 233)));
        skyColor.add(new DefaultMutableTreeNode(new Color(246, 253, 237)));
        skyColor.add(new DefaultMutableTreeNode(new Color(58, 81, 101)));
        skyColor.add(new DefaultMutableTreeNode(new Color(120, 132, 128)));
        skyColor.add(new DefaultMutableTreeNode(new Color(46, 172, 161)));
        skyColor.add(new DefaultMutableTreeNode(new Color(231, 171, 98)));
        skyColor.add(new DefaultMutableTreeNode(new Color(206, 184, 143)));
        root.add(skyColor);
        DefaultMutableTreeNode cloudsColor = new DefaultMutableTreeNode("Clouds");
        cloudsColor.add(new DefaultMutableTreeNode(new Color(249, 248, 250)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(222, 230, 239)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(240, 238, 238)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(222, 217, 221)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(171, 176, 182)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(186, 188, 174)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(176, 195, 198)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(244, 227, 214)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(231, 216, 204)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(225, 222, 209)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(241, 220, 240)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(121, 112, 114)));
        cloudsColor.add(new DefaultMutableTreeNode(new Color(52, 51, 45)));
        root.add(cloudsColor);
        DefaultMutableTreeNode grassColor = new DefaultMutableTreeNode("Grass");
        grassColor.add(new DefaultMutableTreeNode(new Color(184, 230, 124)));
        grassColor.add(new DefaultMutableTreeNode(new Color(147, 159, 81)));
        grassColor.add(new DefaultMutableTreeNode(new Color(101, 161, 16)));
        grassColor.add(new DefaultMutableTreeNode(new Color(110, 157, 18)));
        grassColor.add(new DefaultMutableTreeNode(new Color(96, 132, 95)));
        grassColor.add(new DefaultMutableTreeNode(new Color(72, 111, 61)));
        grassColor.add(new DefaultMutableTreeNode(new Color(48, 75, 32)));
        grassColor.add(new DefaultMutableTreeNode(new Color(57, 62, 27)));
        grassColor.add(new DefaultMutableTreeNode(new Color(252, 231, 155)));
        grassColor.add(new DefaultMutableTreeNode(new Color(225, 178, 110)));
        grassColor.add(new DefaultMutableTreeNode(new Color(237, 203, 72)));
        grassColor.add(new DefaultMutableTreeNode(new Color(200, 164, 120)));
        grassColor.add(new DefaultMutableTreeNode(new Color(148, 118, 66)));
        grassColor.add(new DefaultMutableTreeNode(new Color(128, 127, 91)));
        root.add(grassColor);
        DefaultMutableTreeNode flowersColor = new DefaultMutableTreeNode("Flowers");
        flowersColor.add(new DefaultMutableTreeNode(new Color(217, 49, 186)));
        flowersColor.add(new DefaultMutableTreeNode(new Color(133, 40, 148)));
        flowersColor.add(new DefaultMutableTreeNode(new Color(239, 19, 46)));
        flowersColor.add(new DefaultMutableTreeNode(new Color(245, 215, 250)));
        flowersColor.add(new DefaultMutableTreeNode(new Color(168, 145, 221)));
        flowersColor.add(new DefaultMutableTreeNode(new Color(226, 223, 242)));
        flowersColor.add(new DefaultMutableTreeNode(new Color(246, 232, 81)));
        flowersColor.add(new DefaultMutableTreeNode(new Color(238, 188, 2)));
        root.add(flowersColor);
        DefaultMutableTreeNode woodColor = new DefaultMutableTreeNode("Wood");
        woodColor.add(new DefaultMutableTreeNode(new Color(232, 166, 58)));
        woodColor.add(new DefaultMutableTreeNode(new Color(221, 168, 125)));
        woodColor.add(new DefaultMutableTreeNode(new Color(182, 127, 63)));
        woodColor.add(new DefaultMutableTreeNode(new Color(177, 110, 49)));
        woodColor.add(new DefaultMutableTreeNode(new Color(165, 134, 94)));
        woodColor.add(new DefaultMutableTreeNode(new Color(115, 93, 49)));
        woodColor.add(new DefaultMutableTreeNode(new Color(166, 151, 151)));
        woodColor.add(new DefaultMutableTreeNode(new Color(154, 159, 153)));
        woodColor.add(new DefaultMutableTreeNode(new Color(115, 113, 99)));
        woodColor.add(new DefaultMutableTreeNode(new Color(120, 108, 94)));
        woodColor.add(new DefaultMutableTreeNode(new Color(85, 74, 61)));
        woodColor.add(new DefaultMutableTreeNode(new Color(118, 62, 53)));
        woodColor.add(new DefaultMutableTreeNode(new Color(74, 24, 18)));
        root.add(woodColor);
        DefaultMutableTreeNode stonesColor = new DefaultMutableTreeNode("Stones");
        stonesColor.add(new DefaultMutableTreeNode(new Color(170, 173, 186)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(139, 175, 190)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(108, 137, 160)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(104, 106, 132)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(225, 234, 213)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(156, 171, 155)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(79, 123, 90)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(242, 237, 237)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(164, 164, 154)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(125, 130, 136)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(89, 95, 101)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(232, 205, 189)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(219, 207, 180)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(220, 213, 195)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(175, 147, 118)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(211, 180, 176)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(217, 141, 124)));
        stonesColor.add(new DefaultMutableTreeNode(new Color(193, 139, 139)));
        root.add(stonesColor);
        DefaultMutableTreeNode jeansColor = new DefaultMutableTreeNode("Jeans");
        jeansColor.add(new DefaultMutableTreeNode(new Color(194, 198, 201)));
        jeansColor.add(new DefaultMutableTreeNode(new Color(173, 180, 194)));
        jeansColor.add(new DefaultMutableTreeNode(new Color(123, 126, 145)));
        jeansColor.add(new DefaultMutableTreeNode(new Color(91, 136, 195)));
        jeansColor.add(new DefaultMutableTreeNode(new Color(41, 52, 74)));
        jeansColor.add(new DefaultMutableTreeNode(new Color(20, 25, 37)));
        jeansColor.add(new DefaultMutableTreeNode(new Color(164, 156, 145)));
        jeansColor.add(new DefaultMutableTreeNode(new Color(103, 92, 82)));
        jeansColor.add(new DefaultMutableTreeNode(new Color(87, 58, 107)));
        root.add(jeansColor);
        DefaultMutableTreeNode miscColor = new DefaultMutableTreeNode("Misc Fabrics");
        miscColor.add(new DefaultMutableTreeNode(new Color(213, 63, 60)));
        miscColor.add(new DefaultMutableTreeNode(new Color(118, 30, 23)));
        miscColor.add(new DefaultMutableTreeNode(new Color(93, 20, 25)));
        miscColor.add(new DefaultMutableTreeNode(new Color(219, 111, 135)));
        miscColor.add(new DefaultMutableTreeNode(new Color(158, 79, 96)));
        miscColor.add(new DefaultMutableTreeNode(new Color(213, 96, 29)));
        miscColor.add(new DefaultMutableTreeNode(new Color(207, 164, 0)));
        miscColor.add(new DefaultMutableTreeNode(new Color(241, 245, 90)));
        miscColor.add(new DefaultMutableTreeNode(new Color(228, 250, 112)));
        miscColor.add(new DefaultMutableTreeNode(new Color(225, 231, 94)));
        miscColor.add(new DefaultMutableTreeNode(new Color(125, 160, 128)));
        miscColor.add(new DefaultMutableTreeNode(new Color(78, 89, 21)));
        miscColor.add(new DefaultMutableTreeNode(new Color(8, 117, 59)));
        miscColor.add(new DefaultMutableTreeNode(new Color(50, 76, 55)));
        miscColor.add(new DefaultMutableTreeNode(new Color(128, 160, 241)));
        miscColor.add(new DefaultMutableTreeNode(new Color(53, 88, 208)));
        miscColor.add(new DefaultMutableTreeNode(new Color(23, 107, 200)));
        miscColor.add(new DefaultMutableTreeNode(new Color(64, 56, 162)));
        miscColor.add(new DefaultMutableTreeNode(new Color(52, 70, 74)));
        miscColor.add(new DefaultMutableTreeNode(new Color(208, 229, 230)));
        miscColor.add(new DefaultMutableTreeNode(new Color(197, 198, 193)));
        miscColor.add(new DefaultMutableTreeNode(new Color(169, 154, 143)));
        miscColor.add(new DefaultMutableTreeNode(new Color(54, 57, 61)));
        miscColor.add(new DefaultMutableTreeNode(new Color(51, 45, 49)));
        root.add(miscColor);
        DefaultMutableTreeNode metalsColor = new DefaultMutableTreeNode("Metals");
        metalsColor.add(new DefaultMutableTreeNode(new Color(208, 114, 85)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(236, 177, 137)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(252, 166, 133)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(240, 178, 157)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(100, 62, 51)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(66, 37, 41)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(123, 111, 121)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(133, 122, 136)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(167, 146, 153)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(147, 146, 160)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(181, 174, 181)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(196, 186, 197)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(243, 237, 241)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(217, 196, 172)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(241, 164, 56)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(185, 111, 16)));
        metalsColor.add(new DefaultMutableTreeNode(new Color(255, 255, 206)));
        root.add(metalsColor);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ColorizeIt().setVisible(true);
            }
        });
    }

    private void initTimer() {
        timer = new Timer(10, new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                progress.setProgressValue(task.getCurrent());
                progress.setText(task.getMessage());
                if (task.isDone()) {
                    Toolkit.getDefaultToolkit().beep();
                    timer.stop();
                    progress.setProgressValue(0);
                    progress.setVisible(false);
                    BufferedImage coloredImage = task.getColored();
                    coloredImage.flush();
                    coloredLabel.setIcon(new ImageIcon(coloredImage));
                    coloredLabel.setAutoscrolls(true);
                    coloredScrollPane.add(coloredLabel);
                    coloredScrollPane.setViewportView(coloredLabel);
                }
            }
        });
    }

    private ImageMatrix im;

    private BufferedImage colorMapImage;

    private JPopupMenu colorChoosingMenu;

    private Color workingColor;

    private boolean isDrawing = false;

    private colorpicker colorPicker;

    private ProgressForm progress = null;

    private Task task;

    private javax.swing.JMenu Colorize;

    private javax.swing.JToggleButton brushBtn;

    private javax.swing.JPanel btnToolPanel;

    private javax.swing.ButtonGroup buttonGroup;

    private javax.swing.JPanel cardPanel;

    private javax.swing.JToggleButton colorPaletteBtn;

    private javax.swing.JPanel colorPalettePanel;

    private javax.swing.JLabel coloredLabel;

    private javax.swing.JScrollPane coloredScrollPane;

    private javax.swing.JMenuItem colorize;

    private javax.swing.JProgressBar colorizeProgressBar;

    private javax.swing.JPanel emptyPanel;

    private javax.swing.JToggleButton eraserBtn;

    private javax.swing.JMenu fileMenu;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JSplitPane jSplitPane1;

    private javax.swing.JPanel leftPanel;

    private javax.swing.JMenuItem loadProjectMenuItem;

    private javax.swing.JMenuBar menuBar;

    private javax.swing.JLabel mouseLabel;

    private javax.swing.JMenuItem openMenuItem;

    private javax.swing.JLabel originalLabel;

    private javax.swing.JScrollPane originalScrollPane;

    private javax.swing.JToggleButton pickerBtn;

    private javax.swing.JPanel rightPanel;

    private javax.swing.JMenuItem saveProjectMenuItem;

    private javax.swing.JSplitPane splitPane;

    private javax.swing.JToolBar toolBar;

    private javax.swing.JToolBar toolBox;

    private javax.swing.JPanel toolboxPanel;

    private javax.swing.JToggleButton toolsBtn;

    private javax.swing.JPanel toolsPanel;

    private javax.swing.JToggleButton treeBtn;

    private javax.swing.JComboBox treesCombo;
}
