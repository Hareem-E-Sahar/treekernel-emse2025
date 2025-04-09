package gui;

import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;
import com.l2fprod.common.swing.LookAndFeelTweaks;
import com.l2fprod.common.swing.plaf.metal.MetalLookAndFeelAddons;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;

/**
 *
 * @author  Sherif
 */
public class ImageColorization extends javax.swing.JFrame {

    ImageColoringPanel colorizer;

    ImageFrame workingFrame = new ImageFrame();

    /** Creates new form ColorBurg2 */
    public ImageColorization() {
        initComponents();
        colorizer = new ImageColoringPanel();
        Viewport viewport = new Viewport();
        View startPage = createStartPage();
        viewport.dock(startPage);
        add(viewport, BorderLayout.CENTER);
        View tasks = createTasksPage();
        View colors = createColorsPalette();
        View tools = createToolssPalette();
        tasks.setSize(200, getHeight());
        tasks.setMinimumSize(new Dimension(200, getHeight()));
        tasks.setMaximumSize(new Dimension(200, getHeight()));
        colors.setSize(200, getHeight());
        colors.setMinimumSize(new Dimension(200, getHeight()));
        colors.setMaximumSize(new Dimension(200, getHeight()));
        tools.setSize(200, getHeight());
        tools.setMinimumSize(new Dimension(200, getHeight()));
        tools.setMaximumSize(new Dimension(200, getHeight()));
        viewport.dock((Dockable) tasks, "WEST");
        viewport.dock((Dockable) colors, "WEST");
        viewport.dock((Dockable) tools, "EAST");
        DockingManager.registerDockable((Dockable) tasks);
        DockingManager.registerDockable((Dockable) startPage);
        DockingManager.registerDockable((Dockable) colors);
        DockingManager.registerDockable((Dockable) tools);
        DockingManager.setSplitProportion((Dockable) startPage, 0.8f);
        DockingManager.setSplitProportion((Dockable) tasks, 0.4f);
        DockingManager.setSplitProportion((Dockable) colors, 0.5f);
        pack();
        repaint();
        DockingManager.setMinimized((Dockable) tools, true);
        DockingManager.setMinimized((Dockable) colors, true);
        this.pack();
    }

    private View createTasksPage() {
        View view = new View("Tasks", "Tasks");
        view.addAction("pin");
        JPanel panel = new JPanel();
        JTaskPane taskPaneContainer = new JTaskPane();
        JTaskPaneGroup actionPane = createActionPaneImage();
        taskPaneContainer.add(actionPane);
        actionPane = createActionPaneProject();
        taskPaneContainer.add(actionPane);
        actionPane = createActionPaneColorization();
        taskPaneContainer.add(actionPane);
        panel.setLayout(new BorderLayout());
        panel.add(taskPaneContainer, BorderLayout.CENTER);
        view.setContentPane(new JScrollPane(panel));
        return view;
    }

    private JTaskPaneGroup createActionPaneProject() {
        JTaskPaneGroup actionPane = new JTaskPaneGroup();
        actionPane.setTitle("Project");
        actionPane.setSpecial(true);
        Action action = new AbstractAction("Load Project") {

            public void actionPerformed(ActionEvent e) {
                loadProjectMenuActionPerformed(e);
            }
        };
        action.putValue(Action.SMALL_ICON, new ImageIcon("./Resources/icons/open-20x20.png"));
        action.putValue(Action.SHORT_DESCRIPTION, "Loads project to continue working on colorization");
        actionPane.add(action);
        action = new AbstractAction("Save Project") {

            public void actionPerformed(ActionEvent e) {
                saveProjectMenuActionPerformed(e);
            }
        };
        action.putValue(Action.SMALL_ICON, new ImageIcon("./Resources/icons/save-20x20.png"));
        action.putValue(Action.SHORT_DESCRIPTION, "Saves project to a file");
        actionPane.add(action);
        return actionPane;
    }

    private JTaskPaneGroup createActionPaneColorization() {
        JTaskPaneGroup actionPane = new JTaskPaneGroup();
        actionPane.setTitle("Colorization");
        actionPane.setSpecial(true);
        Action action = new AbstractAction("Colorize") {

            public void actionPerformed(ActionEvent e) {
            }
        };
        action.putValue(Action.SMALL_ICON, new ImageIcon("./Resources/icons/propertysheet20x20.png"));
        action.putValue(Action.SHORT_DESCRIPTION, "Press to colorize the black and white pictures");
        actionPane.add(action);
        actionPane.add(colorizer.getProgressPanel());
        return actionPane;
    }

    private JTaskPaneGroup createActionPaneImage() {
        JTaskPaneGroup actionPane = new JTaskPaneGroup();
        actionPane.setTitle("Image");
        actionPane.setSpecial(true);
        Action action = new AbstractAction("Load Image") {

            public void actionPerformed(ActionEvent e) {
                loadMenuActionPerformed(e);
            }
        };
        action.putValue(Action.SMALL_ICON, new ImageIcon("./Resources/icons/image-file-20x20.png"));
        action.putValue(Action.SHORT_DESCRIPTION, "Loads image to colorize");
        actionPane.add(action);
        action = new AbstractAction("Save Image") {

            public void actionPerformed(ActionEvent e) {
                saveMenuActionPerformed(e);
            }
        };
        action.putValue(Action.SMALL_ICON, new ImageIcon("./Resources/icons/save-20x20.png"));
        action.putValue(Action.SHORT_DESCRIPTION, "Saves image to to image file");
        actionPane.add(action);
        return actionPane;
    }

    private View createColorsPalette() {
        View view = new View("colors.palette", "Color Palette");
        view.addAction("pin");
        view.setLayout(new BorderLayout());
        JPanel panel = colorizer.getColorPalette();
        view.add(panel, BorderLayout.CENTER);
        return view;
    }

    private View createToolssPalette() {
        View view = new View("tools.palette", "Tree Algorithm");
        view.addAction("pin");
        JPanel panel = colorizer.getToolsPalette();
        view.setContentPane(panel);
        return view;
    }

    private View createStartPage() {
        String id = "startPage";
        View view = new View(id, null, null);
        view.setTitlebar(null);
        view.setContentPane(colorizer);
        return view;
    }

    private void initComponents() {
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        loadMenu = new javax.swing.JMenuItem();
        saveMenu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenu = new javax.swing.JMenuItem();
        projectMenu = new javax.swing.JMenu();
        saveProjectMenu = new javax.swing.JMenuItem();
        loadProjectMenu = new javax.swing.JMenuItem();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 102, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        fileMenu.setText("Image");
        loadMenu.setText("Load Image");
        loadMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMenuActionPerformed(evt);
            }
        });
        fileMenu.add(loadMenu);
        saveMenu.setText("Save Image");
        saveMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenu);
        fileMenu.add(jSeparator1);
        exitMenu.setText("Exit");
        exitMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenu);
        jMenuBar1.add(fileMenu);
        projectMenu.setText("Project");
        saveProjectMenu.setText("Save Project");
        saveProjectMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectMenuActionPerformed(evt);
            }
        });
        projectMenu.add(saveProjectMenu);
        loadProjectMenu.setText("Load Project");
        loadProjectMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProjectMenuActionPerformed(evt);
            }
        });
        projectMenu.add(loadProjectMenu);
        jMenuBar1.add(projectMenu);
        setJMenuBar(jMenuBar1);
        pack();
    }

    private void loadProjectMenuActionPerformed(java.awt.event.ActionEvent evt) {
        loadProject();
    }

    private void saveProjectMenuActionPerformed(java.awt.event.ActionEvent evt) {
        saveProject();
    }

    private void saveProject() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        String path = "c:\\";
        String fileName = "TestSave";
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            path = fc.getSelectedFile().getParent() + "\\";
            fileName = fc.getSelectedFile().getName();
        } else {
            return;
        }
        ImageManip.save(path + fileName + "-Map.jpg", colorizer.getColorMap());
        ImageManip.save(path + fileName + "-Marked.jpg", colorizer.getMarked());
        ImageManip.save(path + fileName + "-original.jpg", colorizer.getOriginal());
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

    private void loadProject() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);
        String path = "c:\\";
        String fileName = "TestSave";
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            path = fc.getSelectedFile().getParent() + "\\";
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
            colorizer.setColorMap(ImageIO.read(mapFile));
            workingFrame.setImage(ImageIO.read(originalFile));
            colorizer.colorizeNextFrame(workingFrame);
            originalFile.delete();
            mapFile.delete();
            markedFile.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveMenuActionPerformed(java.awt.event.ActionEvent evt) {
        ImageFrame coloredFrame = (ImageFrame) colorizer.getColoredFrame();
        if (coloredFrame == null) {
            JOptionPane.showMessageDialog(this, "No Colored Image To Save");
            return;
        }
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        String path = "c:\\";
        String fileName = "TestSave";
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ImageManip.save(fc.getSelectedFile().getAbsolutePath(), coloredFrame.getColoredImage());
        } else {
            return;
        }
    }

    private void exitMenuActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void loadMenuActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            OpenFileDialog ofd = new OpenFileDialog(this, true);
            ofd.setVisible(true);
            String imagePath = ofd.getSelectedFilePath();
            if (imagePath == null) return;
            BufferedImage loaded = ImageIO.read(new File(imagePath));
            workingFrame.setImage(loaded);
            colorizer.setImageToColorize(workingFrame);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground", "true");
        try {
            Thread.sleep(300);
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println("Metal Look & Feel not supported on this platform. \nProgram Terminated");
        } catch (InterruptedException x) {
            x.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                ImageColorization frame = new ImageColorization();
                frame.setExtendedState(frame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }
        });
    }

    private javax.swing.JMenuItem exitMenu;

    private javax.swing.JMenu fileMenu;

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JMenuItem loadMenu;

    private javax.swing.JMenuItem loadProjectMenu;

    private javax.swing.JMenu projectMenu;

    private javax.swing.JMenuItem saveMenu;

    private javax.swing.JMenuItem saveProjectMenu;
}
