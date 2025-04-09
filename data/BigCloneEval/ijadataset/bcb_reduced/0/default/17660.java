import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class MiniPOVEditor {

    private JFrame frame;

    private JPanel ledPanel;

    private JFileChooser fileChooser;

    private JDialog changeColumnDialog;

    private JSpinner columnNumSpinner;

    private JScrollPane scrollPane;

    private MiniViewPanel miniViewPanel;

    private JFrame miniViewWindow;

    private ArrayList<Column> columns;

    private int cellSpace = 2;

    private int cellSize = 20;

    private int numCols = 255;

    private int numColsShown = 20;

    public static void main(String[] args) {
        new MiniPOVEditor();
    }

    public MiniPOVEditor() {
        frame = new JFrame("MiniPOVEditor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(new POVMenu());
        ledPanel = new JPanel(null);
        scrollPane = new JScrollPane(ledPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(800, 8 * (cellSize + cellSpace) + 25));
        frame.getContentPane().add(scrollPane);
        fileChooser = new JFileChooser();
        columns = new ArrayList<Column>();
        for (int i = 0; i < numCols; i++) {
            Column curColumn = new Column(this, cellSize, cellSpace);
            curColumn.setLocation(i * (cellSize + cellSpace), 0);
            columns.add(curColumn);
        }
        columns.get(0).setNumColumnsShown(numColsShown);
        ledPanel.setPreferredSize(new Dimension(numColsShown * (cellSize + cellSpace), 8 * (cellSize + cellSpace) + 5));
        frame.pack();
        frame.setVisible(true);
        miniViewWindow = new JFrame("MiniView");
        miniViewPanel = new MiniViewPanel();
        miniViewWindow.getContentPane().add(miniViewPanel);
        miniViewWindow.setLocation(0, 8 * (cellSize + cellSpace) + 100);
        Dimension frameSize = miniViewPanel.getPreferredSize();
        frameSize.height += 20;
        miniViewWindow.setSize(frameSize);
        this.setNumColsShown(numColsShown);
    }

    private String getImgCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("const uint8_t large_image[] PROGMEM = {\n");
        for (int i = 0; i < numColsShown; i++) {
            Column c = columns.get(i);
            sb.append(c.getColumnImgCode() + "\n");
        }
        sb.append("};");
        return sb.toString();
    }

    private void setNumColsShown(int num) {
        numColsShown = num;
        columns.get(0).setNumColumnsShown(num);
        ledPanel.removeAll();
        for (int i = 0; i < num; i++) {
            ledPanel.add(columns.get(i));
        }
        ledPanel.setPreferredSize(new Dimension(numColsShown * (cellSize + cellSpace), 8 * (cellSize + cellSpace) + 25));
        frame.pack();
        miniViewPanel.autoResize();
        Dimension frameSize = miniViewPanel.getPreferredSize();
        frameSize.height += 20;
        miniViewWindow.setSize(frameSize);
    }

    @SuppressWarnings("unchecked")
    private void doOpenCommand() {
        int returnVal = fileChooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String filename = fileChooser.getSelectedFile().getCanonicalPath();
                FileInputStream fin = new FileInputStream(filename);
                ObjectInputStream oin = new ObjectInputStream(fin);
                ArrayList<Column> openedColumns = (ArrayList<Column>) oin.readObject();
                oin.close();
                doNewCommand();
                for (int i = 0; i < openedColumns.size(); i++) {
                    columns.get(i).setTo(openedColumns.get(i));
                }
                setNumColsShown(openedColumns.get(0).getNumColumnsShown());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "There was an error while reading the file.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(frame, "This isn't the right file type.  You can only load files saved by this program.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ClassCastException e) {
                JOptionPane.showMessageDialog(frame, "This isn't the right file type.  You can only load files saved by this program.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doNewCommand() {
        for (Column c : columns) {
            c.clear();
        }
    }

    private void doSaveCommand() {
        fileChooser.setSelectedFile(new File("MyDrawing.mpov"));
        int returnVal = fileChooser.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String filename = fileChooser.getSelectedFile().getCanonicalPath();
                FileOutputStream fout = new FileOutputStream(filename);
                ObjectOutputStream oout = new ObjectOutputStream(fout);
                oout.writeObject(columns);
                oout.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Could not write the file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doExportCommand() {
        fileChooser.setSelectedFile(new File("image_code.c"));
        int returnVal = fileChooser.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String filename = fileChooser.getSelectedFile().getCanonicalPath();
                FileWriter fw = new FileWriter(filename);
                fw.write(this.getImgCode());
                fw.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Could not write the file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doChangeColumnCommand() {
        changeColumnDialog = new JDialog(frame, "Change number of columns", true);
        changeColumnDialog.setResizable(false);
        changeColumnDialog.getContentPane().setLayout(new FlowLayout());
        changeColumnDialog.add(new JLabel("How many columns should be shown?"));
        SpinnerModel model = new SpinnerNumberModel(numColsShown, 1, numCols, 1);
        columnNumSpinner = new JSpinner(model);
        changeColumnDialog.add(columnNumSpinner);
        JButton setButton = new JButton("Set");
        setButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeColumnDialog.setVisible(false);
                setNumColsShown(Integer.parseInt(columnNumSpinner.getValue().toString()));
            }
        });
        changeColumnDialog.add(setButton);
        changeColumnDialog.pack();
        changeColumnDialog.setVisible(true);
    }

    private void doAboutCommand() {
        JOptionPane.showMessageDialog(frame, "MiniPOVEditor by Andy Isaacson 10/08\nandy@cs.uoregon.edu\nFor use with the MiniPOV kit by LadyAda (http://ladyada.net/make/minipov3/index.html)", "About this program", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doHelpCommand() {
        String msg = "This program is a tool to create images for use with the MiniPOV kit.\n\n" + "Each black box represents a pixel of the final image.  Click or drag to switch pixels on\n" + "or off.  When you are finished, you can export the image as AVR C code that can then be\n" + "pasted into your MiniPOV program.  See the MiniPOV website and forums for more information.";
        JOptionPane.showMessageDialog(frame, msg, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    @SuppressWarnings("serial")
    private class POVMenu extends JMenuBar {

        public POVMenu() {
            JMenu fileMenu = new JMenu("File");
            this.add(fileMenu);
            JMenuItem newItem = new JMenuItem("New Drawing");
            newItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    doNewCommand();
                }
            });
            fileMenu.add(newItem);
            fileMenu.addSeparator();
            JMenuItem openItem = new JMenuItem("Open...");
            openItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    doOpenCommand();
                }
            });
            fileMenu.add(openItem);
            JMenuItem saveItem = new JMenuItem("Save...");
            saveItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    doSaveCommand();
                }
            });
            fileMenu.add(saveItem);
            fileMenu.addSeparator();
            JMenuItem exportItem = new JMenuItem("Export to AVR C...");
            exportItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    doExportCommand();
                }
            });
            fileMenu.add(exportItem);
            fileMenu.addSeparator();
            JMenuItem quitItem = new JMenuItem("Quit");
            quitItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            fileMenu.add(quitItem);
            JMenu drawingMenu = new JMenu("Drawing");
            this.add(drawingMenu);
            JMenuItem changeColumnItem = new JMenuItem("Change number of columns...");
            changeColumnItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    doChangeColumnCommand();
                }
            });
            drawingMenu.add(changeColumnItem);
            JMenuItem miniViewItem = new JMenuItem("Show MiniView");
            miniViewItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    miniViewWindow.setVisible(true);
                }
            });
            drawingMenu.add(miniViewItem);
            JMenu helpMenu = new JMenu("Help");
            this.add(helpMenu);
            JMenuItem aboutItem = new JMenuItem("About this program...");
            aboutItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    doAboutCommand();
                }
            });
            helpMenu.add(aboutItem);
            helpMenu.addSeparator();
            JMenuItem helpItem = new JMenuItem("How to use...");
            helpItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    doHelpCommand();
                }
            });
            helpMenu.add(helpItem);
        }
    }

    @SuppressWarnings("serial")
    private class MiniViewPanel extends JPanel {

        private int miniCellSize = 3;

        private int miniCellSpace = 1;

        public MiniViewPanel() {
            super();
            this.setPreferredSize(new Dimension(numColsShown * (miniCellSize + miniCellSpace) + 5, 8 * (miniCellSize + miniCellSpace) + 5));
        }

        public void autoResize() {
            this.setPreferredSize(new Dimension(numColsShown * (miniCellSize + miniCellSpace) + 5, 8 * (miniCellSize + miniCellSpace) + 5));
            repaint();
        }

        public void paintComponent(Graphics g) {
            for (int i = 0; i < numColsShown; i++) {
                ArrayList<Cell> cells = columns.get(i).getCells();
                for (int j = 0; j < cells.size(); j++) {
                    if (cells.get(j).isOn()) g.setColor(Color.red); else g.setColor(Color.black);
                    g.fillRect(i * (miniCellSize + miniCellSpace), j * (miniCellSize + miniCellSpace), miniCellSize, miniCellSize);
                }
            }
        }
    }

    public void stateChanged() {
        miniViewPanel.repaint();
    }
}
