import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

public class InstructorUI {

    private JFrame window;

    private JButton addMap;

    private JButton deleteMap;

    private JButton labelMap;

    private JButton addPin;

    private JButton addRegion;

    private JButton editObject;

    private JButton zoomIn;

    private JButton zoomOut;

    private JButton undo;

    private JButton redo;

    private JButton help;

    private JButton viewObjects;

    private JPanel buttonPanel;

    private JPanel outerMapPanel;

    private MapPanel mapPanel;

    private JScrollPane mapScrollArea;

    private JSlider slider;

    private JMenuBar menuBar;

    private JMenu fileMenu;

    private JMenu helpMenu;

    private JMenuItem newFile;

    private JMenuItem openFile;

    private JMenuItem saveFile;

    private JMenuItem saveAsFile;

    private JMenuItem exitFile;

    private JMenuItem writeHelp;

    private JMenuItem viewHelp;

    private JTextArea contextHelp;

    private String syllabusName;

    private boolean isInstructor = true;

    public InstructorUI() {
        this.window = new JFrame("Interactive Syllabus");
        this.addMap = new JButton("Add Map");
        this.deleteMap = new JButton("Delete Map");
        this.labelMap = new JButton("Label Map");
        this.addPin = new JButton("Add Pin");
        this.addRegion = new JButton("Add Region");
        this.editObject = new JButton("Edit Object");
        this.zoomIn = new JButton("Zoom In");
        this.zoomOut = new JButton("Zoom Out");
        this.undo = new JButton("Undo");
        this.redo = new JButton("Redo");
        this.help = new JButton("Help");
        this.viewObjects = new JButton("View Objects");
        this.buttonPanel = new JPanel();
        this.slider = new JSlider();
        this.outerMapPanel = new JPanel();
        this.mapPanel = new MapPanel(this.slider);
        this.mapScrollArea = new JScrollPane(this.mapPanel);
        this.menuBar = new JMenuBar();
        this.fileMenu = new JMenu("File");
        this.helpMenu = new JMenu("Help");
        this.newFile = new JMenuItem("New");
        this.openFile = new JMenuItem("Open");
        this.saveFile = new JMenuItem("Save");
        this.saveAsFile = new JMenuItem("Save As");
        this.exitFile = new JMenuItem("Quit");
        this.writeHelp = new JMenuItem("Create Help");
        this.viewHelp = new JMenuItem("Help");
        this.contextHelp = new JTextArea();
    }

    public void run() {
        this.mapScrollArea.setPreferredSize(new Dimension(590, 540));
        this.outerMapPanel.setPreferredSize(new Dimension(590, 590));
        this.buttonPanel.setPreferredSize(new Dimension(195, 600));
        this.mapPanel.addMouseListener(this.mapPanel);
        this.mapScrollArea.getVerticalScrollBar().setUnitIncrement(10);
        GridLayout buttonGrid = new GridLayout(0, 1);
        this.buttonPanel.setLayout(buttonGrid);
        this.addMap.addActionListener(new AddMapListener());
        this.deleteMap.addActionListener(new DeleteMapListener());
        this.labelMap.addActionListener(new LabelMapListener());
        this.addPin.addActionListener(new AddPinListener());
        this.addRegion.addActionListener(new AddRegionListener());
        this.zoomIn.addActionListener(new ZoomInListener());
        this.zoomOut.addActionListener(new ZoomOutListener());
        this.editObject.addActionListener(new EditMapObjectListener());
        this.saveAsFile.addActionListener(new SaveAsListener());
        this.openFile.addActionListener(new OpenListener(this.mapScrollArea));
        this.saveFile.addActionListener(new SaveListener());
        this.undo.addActionListener(new UndoListener());
        this.redo.addActionListener(new RedoListener());
        this.exitFile.addActionListener(new QuitListener());
        this.viewObjects.addActionListener(new ViewObjectsListener());
        this.help.addActionListener(new HelpListener());
        this.viewHelp.addActionListener(new HelpListener());
        this.writeHelp.addActionListener(new CreateHelpListener());
        if (this.isInstructor) {
            this.buttonPanel.add(this.addMap);
            this.buttonPanel.add(this.deleteMap);
            this.buttonPanel.add(this.labelMap);
            this.buttonPanel.add(this.addPin);
            this.buttonPanel.add(this.addRegion);
            this.buttonPanel.add(this.editObject);
            this.buttonPanel.add(this.undo);
            this.buttonPanel.add(this.redo);
        }
        this.buttonPanel.add(this.viewObjects);
        this.buttonPanel.add(this.zoomIn);
        this.buttonPanel.add(this.zoomOut);
        this.buttonPanel.add(this.help);
        this.outerMapPanel.add(this.mapScrollArea);
        this.outerMapPanel.add(this.slider);
        this.window.add(this.outerMapPanel, BorderLayout.WEST);
        this.window.add(this.buttonPanel, BorderLayout.EAST);
        this.window.add(this.contextHelp, BorderLayout.PAGE_END);
        this.window.addComponentListener(new ResizeListener());
        this.fileMenu.getAccessibleContext().setAccessibleDescription("File Menu");
        this.menuBar.add(this.fileMenu);
        this.menuBar.add(this.helpMenu);
        this.newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        this.openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        this.saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        this.viewHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        if (isInstructor) {
            this.fileMenu.add(this.newFile);
        }
        this.fileMenu.add(this.openFile);
        if (isInstructor) {
            this.fileMenu.add(this.saveFile);
            this.fileMenu.add(this.saveAsFile);
        }
        this.fileMenu.add(this.exitFile);
        this.helpMenu.add(this.viewHelp);
        if (isInstructor) {
            this.helpMenu.add(this.writeHelp);
        }
        this.newFile.addActionListener(new NewFileListener());
        this.window.setJMenuBar(this.menuBar);
        this.contextHelp.setEditable(false);
        this.contextHelp.setBackground(Color.LIGHT_GRAY);
        this.contextHelp.setBorder(BorderFactory.createLineBorder(Color.black));
        this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.window.pack();
        this.window.setLocationRelativeTo(null);
        this.window.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        InstructorUI GUI = new InstructorUI();
        GUI.run();
    }

    private class NewFileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            InstructorUI GUI = new InstructorUI();
            GUI.run();
        }
    }

    private class ViewObjectsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() == null) {
                return;
            }
            ViewObjects newView = new ViewObjects(InstructorUI.this.mapPanel.getMap(), InstructorUI.this.mapPanel);
            newView.displayRadioBoxSelector();
            InstructorUI.this.mapPanel.repaint();
        }
    }

    private class AddMapListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            InstructorUI.this.contextHelp.setText("Please select an image file for the new map");
            FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("JPEGs", "jpg", "jpeg", "gif", "png");
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(fileFilter);
            int userChoice = fileChooser.showOpenDialog(InstructorUI.this.window);
            if (userChoice == JFileChooser.APPROVE_OPTION) {
                try {
                    Image image = ImageIO.read(fileChooser.getSelectedFile());
                    if (image == null) {
                        JOptionPane.showMessageDialog(null, "Please select an image file", "Open Error", JOptionPane.ERROR_MESSAGE);
                        this.actionPerformed(arg0);
                    } else {
                        InstructorUI.this.mapPanel.addMapToPanel(image);
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "There was an error opening the image file", "Open Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
                InstructorUI.this.mapPanel.revalidate();
                InstructorUI.this.mapPanel.repaint();
            }
            InstructorUI.this.contextHelp.setText("");
        }
    }

    private class DeleteMapListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() == null) {
                InstructorUI.this.contextHelp.setText("You must first add a map to use this feature");
                return;
            }
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the map?", "Deletion Confirmation", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                InstructorUI.this.mapPanel.deleteCurrentMap();
            }
        }
    }

    private class LabelMapListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() == null) {
                return;
            }
            JDialog frame = new JDialog((JFrame) null, "Enter Map Label", true);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            JTextField titleField = new JTextField("Enter the display name here");
            titleField.addFocusListener(new InstructorUI.ClearTextListener(titleField.getText()));
            JButton save = new JButton("Save");
            save.addActionListener(new SaveLabelListener(frame, titleField));
            frame.add(titleField, BorderLayout.NORTH);
            frame.add(save, BorderLayout.SOUTH);
            save.requestFocusInWindow();
            frame.setSize(new Dimension(250, 80));
            frame.setLocationRelativeTo((Component) arg0.getSource());
            frame.setVisible(true);
        }
    }

    private class SaveLabelListener implements ActionListener {

        private JDialog frame;

        private JTextField title;

        public SaveLabelListener(JDialog frame, JTextField title) {
            this.frame = frame;
            this.title = title;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
            int mapIndex = InstructorUI.this.mapPanel.getMapList().indexOf(InstructorUI.this.mapPanel.getMap());
            Enumeration<Integer> enumer = InstructorUI.this.slider.getLabelTable().keys();
            while (enumer.hasMoreElements()) {
                int key = enumer.nextElement();
                String label;
                if (key == mapIndex) {
                    label = this.title.getText();
                    InstructorUI.this.mapPanel.getMap().title = this.title.getText();
                } else {
                    label = ((JLabel) InstructorUI.this.slider.getLabelTable().get(key)).getText();
                    InstructorUI.this.mapPanel.getMap().title = this.title.getText();
                }
                labelTable.put(new Integer(key), new JLabel(label));
            }
            InstructorUI.this.slider.setLabelTable(labelTable);
            this.frame.dispose();
        }
    }

    private class AddPinListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() == null) {
                InstructorUI.this.contextHelp.setText("You must first add a map to use this feature");
                return;
            }
            InstructorUI.this.contextHelp.setText("Click anywhere on the map to create a pin");
            InstructorUI.this.mapPanel.setSystemState(SystemState.ADDOBJECT);
            Pin newPin = new Pin(InstructorUI.this.mapPanel.getMap(), InstructorUI.this.mapPanel);
            newPin.displayCreatePrompt(false);
            InstructorUI.this.mapPanel.addIncompleteObject(newPin);
        }
    }

    private class AddRegionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() == null) {
                InstructorUI.this.contextHelp.setText("You must first add a map to use this feature");
                return;
            }
            InstructorUI.this.contextHelp.setText("Click anywhere on the map to create a point of the region");
            InstructorUI.this.mapPanel.setSystemState(SystemState.ADDOBJECT);
            Region newRegion = new Region(InstructorUI.this.mapPanel.getMap(), InstructorUI.this.mapPanel);
            newRegion.displayCreatePrompt(false);
            InstructorUI.this.mapPanel.addIncompleteObject(newRegion);
        }
    }

    private class EditMapObjectListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() == null) {
                InstructorUI.this.contextHelp.setText("You must first add a map to use this feature");
                return;
            }
            InstructorUI.this.contextHelp.setText("Click on an object to edit it");
            InstructorUI.this.mapPanel.setSystemState(SystemState.EDITMAPOBJECT);
        }
    }

    private class ZoomInListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() == null) {
                InstructorUI.this.contextHelp.setText("You must first add a map to use this feature");
                return;
            }
            InstructorUI.this.mapPanel.getMap().increaseZoom();
            InstructorUI.this.mapPanel.repaint();
        }
    }

    private class ZoomOutListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() == null) {
                InstructorUI.this.contextHelp.setText("You must first add a map to use this feature");
                return;
            }
            InstructorUI.this.mapPanel.getMap().decreaseZoom();
            InstructorUI.this.mapPanel.repaint();
        }
    }

    private class SaveAsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            try {
                InstructorUI.this.contextHelp.setText("Please enter a file name under which the syllabus will be saved");
                JFileChooser fileChooser = new JFileChooser();
                int userChoice = fileChooser.showSaveDialog(InstructorUI.this.window);
                if (userChoice == JFileChooser.APPROVE_OPTION) {
                    FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile().getAbsolutePath() + ".is");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(InstructorUI.this.mapPanel);
                    oos.close();
                    fos.close();
                }
                InstructorUI.this.contextHelp.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class OpenListener implements ActionListener {

        JScrollPane scrollPanel;

        public OpenListener(JScrollPane jsp) {
            this.scrollPanel = jsp;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.mapPanel.getMap() != null) {
                int choice = JOptionPane.showConfirmDialog(null, "Do you want to save?", "Save Project Confirmation", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    SaveListener thisIsABadIdea = new SaveListener();
                    thisIsABadIdea.actionPerformed(null);
                }
            }
            try {
                InstructorUI.this.contextHelp.setText("Please select syllabus file to open");
                FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Syllabus Files", "is");
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(fileFilter);
                int userChoice = fileChooser.showOpenDialog(InstructorUI.this.window);
                if (userChoice == JFileChooser.APPROVE_OPTION) {
                    File tempFile = fileChooser.getSelectedFile();
                    FileInputStream fis = new FileInputStream(tempFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    InstructorUI.this.mapPanel = (MapPanel) ois.readObject();
                    if (InstructorUI.this.mapPanel.getHelpHandler() == null) {
                        InstructorUI.this.mapPanel.createHelpHandler();
                    }
                    this.scrollPanel.getViewport().add(InstructorUI.this.mapPanel);
                    InstructorUI.this.outerMapPanel.remove(InstructorUI.this.slider);
                    InstructorUI.this.slider = InstructorUI.this.mapPanel.getSlider();
                    InstructorUI.this.outerMapPanel.add(InstructorUI.this.slider);
                    InstructorUI.this.outerMapPanel.repaint();
                    InstructorUI.this.syllabusName = fileChooser.getSelectedFile().getAbsolutePath();
                    ois.close();
                    fis.close();
                }
                InstructorUI.this.contextHelp.setText("");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not open the specified file", "Open Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Could not open the specified file", "Open Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private class SaveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (InstructorUI.this.syllabusName != null) {
                try {
                    FileOutputStream fos;
                    fos = new FileOutputStream(InstructorUI.this.syllabusName);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(InstructorUI.this.mapPanel);
                    oos.close();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    InstructorUI.this.contextHelp.setText("Please enter a file name under which the syllabus will be saved");
                    JFileChooser fileChooser = new JFileChooser();
                    int userChoice = fileChooser.showSaveDialog(InstructorUI.this.window);
                    if (userChoice == JFileChooser.APPROVE_OPTION) {
                        FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile().getAbsolutePath() + ".is");
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(InstructorUI.this.mapPanel);
                        oos.close();
                        fos.close();
                    }
                    InstructorUI.this.contextHelp.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UndoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            InstructorUI.this.mapPanel.undo();
        }
    }

    private class RedoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            InstructorUI.this.mapPanel.redo();
        }
    }

    private class QuitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            InstructorUI.this.window.dispose();
        }
    }

    private class ResizeListener implements ComponentListener {

        @Override
        public void componentHidden(ComponentEvent arg0) {
        }

        @Override
        public void componentMoved(ComponentEvent arg0) {
        }

        @Override
        public void componentResized(ComponentEvent arg0) {
            int newOuterWidth = InstructorUI.this.window.getWidth() - (InstructorUI.this.buttonPanel.getWidth() + 15);
            int newOuterHeight = InstructorUI.this.window.getHeight() - (InstructorUI.this.contextHelp.getHeight() + 90);
            InstructorUI.this.outerMapPanel.setPreferredSize(new Dimension(newOuterWidth, newOuterHeight));
            InstructorUI.this.outerMapPanel.setSize(new Dimension(newOuterWidth, newOuterHeight));
            int newScrollWidth = InstructorUI.this.outerMapPanel.getWidth() - 20;
            int newScrollHeight = InstructorUI.this.outerMapPanel.getHeight() - 50;
            InstructorUI.this.mapScrollArea.setPreferredSize(new Dimension(newScrollWidth, newScrollHeight));
            InstructorUI.this.mapScrollArea.setSize(new Dimension(newScrollWidth, newScrollHeight));
            InstructorUI.this.slider.setPreferredSize(new Dimension(newScrollWidth, 70));
            InstructorUI.this.window.validate();
        }

        @Override
        public void componentShown(ComponentEvent arg0) {
        }
    }

    private class HelpListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            new HelpWindow(InstructorUI.this.mapPanel.getHelpHandler());
        }
    }

    private class CreateHelpListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            HelpTopicDialog htd = new HelpTopicDialog(InstructorUI.this.mapPanel.getHelpHandler());
        }
    }

    public static class ClearTextListener implements FocusListener {

        private String text;

        public ClearTextListener(String text) {
            this.text = text;
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (e.getComponent() instanceof JTextComponent && ((JTextComponent) e.getComponent()).getText().equals(this.text)) {
                ((JTextComponent) e.getComponent()).setText("");
            }
        }

        @Override
        public void focusLost(FocusEvent arg0) {
        }
    }
}
