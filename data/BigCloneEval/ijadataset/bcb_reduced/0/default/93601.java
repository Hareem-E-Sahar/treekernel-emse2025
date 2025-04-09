import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.net.URL;

class SaveUI extends JPanel implements IView {

    CoreModel model;

    JPanel cards;

    JLabel titleLabel;

    JPanel data;

    JPanel image;

    JButton exportCsvButton;

    JButton doneButton;

    public SaveUI(CoreModel m, JPanel c) {
        assert m != null;
        model = m;
        assert c != null;
        cards = c;
        createWidgets();
        layoutView();
        registerControllers();
        model.addView(this);
    }

    private void createWidgets() {
        titleLabel = new JLabel("<html><b>Save BiasViz Output</b></html>");
        data = new JPanel();
        image = new JPanel();
        exportCsvButton = new JButton("Export Data as CSV File");
        doneButton = new JButton("<< Back to Graphical View");
    }

    private void layoutView() {
        this.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        data.setLayout(new BoxLayout(data, BoxLayout.Y_AXIS));
        data.setBorder(BorderFactory.createTitledBorder("Save Data"));
        data.add(exportCsvButton);
        image.setLayout(new BoxLayout(image, BoxLayout.Y_AXIS));
        image.setBorder(BorderFactory.createTitledBorder("Save Image"));
        data.setAlignmentX(Component.LEFT_ALIGNMENT);
        image.setAlignmentX(Component.LEFT_ALIGNMENT);
        doneButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(titleLabel);
        this.add(Box.createVerticalStrut(12));
        this.add(data);
        this.add(Box.createVerticalStrut(12));
        this.add(doneButton);
        Dimension minsize = new Dimension(1, 1);
        Dimension maxsize = new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
        this.add(new Box.Filler(minsize, maxsize, maxsize));
    }

    public void updateView() {
    }

    private void registerControllers() {
        this.exportCsvButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("biasviz-output.csv"));
                int retval = fc.showSaveDialog(SaveUI.this);
                if (retval == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        if (file.exists()) {
                            int choice = JOptionPane.showConfirmDialog(SaveUI.this, "The file \"" + file.getName() + "\" already exists in that location.\n" + "Do you want to replace it with the one you are saving?", "Replace Existing File?", JOptionPane.YES_NO_OPTION);
                            if (choice != JOptionPane.YES_OPTION) {
                                return;
                            }
                        }
                        file.createNewFile();
                        BufferedWriter out = new BufferedWriter(new FileWriter(file));
                        out.write(model.getCSV());
                        out.close();
                    } catch (IOException io) {
                        System.err.println("Error writing file " + io.getMessage());
                    }
                }
            }
        });
        this.doneButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) (SaveUI.this.cards.getLayout());
                cl.previous(SaveUI.this.cards);
            }
        });
    }
}
